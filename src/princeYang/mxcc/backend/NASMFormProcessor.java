package princeYang.mxcc.backend;

import princeYang.mxcc.Config;
import princeYang.mxcc.ir.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static princeYang.mxcc.backend.NASMRegSet.*;

public class NASMFormProcessor
{
    private IRROOT irRoot;
    private Map<IRFunction, RegFormFuncInfo> targetFuncInfoMap = new HashMap<IRFunction, RegFormFuncInfo>();

    public NASMFormProcessor(IRROOT irRoot)
    {
        this.irRoot = irRoot;
    }

    public void processNASM()
    {
        targetFuncInfoProcess();
        // pre-process for recursive
        for (IRFunction irFunction : targetFuncInfoMap.keySet())
        {
            RegFormFuncInfo funcInfo = targetFuncInfoMap.get(irFunction);
            funcInfo.recursiveUsedRegSet.addAll(irFunction.getUsedGeneralPReg());
            for (IRFunction recurCalleeFunc : irFunction.recurCalleeSet)
                funcInfo.recursiveUsedRegSet.addAll(recurCalleeFunc.getUsedGeneralPReg());
        }
        targetRegFormProcess();
    }

    private void targetFuncInfoProcess()
    {
        for (IRFunction irFunction : irRoot.getFunctionMap().values())
        {
            RegFormFuncInfo funcInfo = new RegFormFuncInfo();

//            int usedParaRegNum = Math.min(irFunction.getParavRegList().size(), 6);
//            for (int i = 0; i < usedParaRegNum; i++)
//            {
//                irFunction.getUsedGeneralPReg().add(NASMRegSet.funcParaRegs.get(i));
//            }

            // calleeSave & callerSave Regs
            funcInfo.usedCalleeSaveRegs.add(rbx);
            funcInfo.usedCalleeSaveRegs.add(rbp);
            for (PhysicalReg pReg : irFunction.getUsedGeneralPReg())
            {
                if (pReg.isCalleeSave())
                    funcInfo.usedCalleeSaveRegs.add(pReg);
                if (pReg.isCallerSave())
                    funcInfo.usedCallerSaveRegs.add(pReg);
            }

            // stacks
            funcInfo.stackSlotNum = irFunction.getStackSlotList().size();
            for (int i = 0; i < funcInfo.stackSlotNum; i++)
                funcInfo.stackSlotOffsetMap.put(irFunction.getStackSlotList().get(i), i * Config.regSize);

            // WARNING: Align rsp !!!
            // 8 bytes for a reg, stack need align to 16 bytes

            if ((funcInfo.usedCalleeSaveRegs.size() + funcInfo.stackSlotNum) % 2 == 0)
                funcInfo.stackSlotNum++;

            // all paras are pushed to stack for stupid allocator
            funcInfo.extraParasNum = max(0, irFunction.getParavRegList().size() - 6);

            int extraArgsOffset = (funcInfo.usedCalleeSaveRegs.size() + funcInfo.stackSlotNum + 1) * Config.regSize;

            for (int i = 6; i < irFunction.getParavRegList().size(); i++)
            {
                funcInfo.stackSlotOffsetMap.put(irFunction.getParaSlotMap().get(irFunction.getParavRegList().get(i)), extraArgsOffset);
                extraArgsOffset += Config.regSize;
            }
            targetFuncInfoMap.put(irFunction, funcInfo);
        }

        // buildin functions
        for (IRFunction buildinFunc : irRoot.getBuildInFuncMap().values())
        {
            targetFuncInfoMap.put(buildinFunc, new RegFormFuncInfo());
        }
    }

    private void targetRegFormProcess()
    {
        for (IRFunction irFunction : irRoot.getFunctionMap().values())
        {
            RegFormFuncInfo funcInfo = targetFuncInfoMap.get(irFunction);

            // modify function entry, push calleeSave regs
            BasicBlock blockEnter = irFunction.getBlockEnter();
            IRInstruction headInst = blockEnter.getHeadInst();
            for (PhysicalReg pReg : funcInfo.usedCalleeSaveRegs)
                headInst.prepend(new Push(blockEnter, pReg));
            if (funcInfo.stackSlotNum > 0)
            {
                // allocate stack frame
                headInst.prepend(new BinaryOperation(blockEnter, rsp, rsp, IRBinaryOp.SUB, new Immediate(funcInfo.stackSlotNum * Config.regSize)));
            }
            // set new stack frame's rbp to stack top
            headInst.prepend(new Move(blockEnter, rbp, rsp));

            // modify instructions
            for (BasicBlock basicBlock : irFunction.getReversePostOrder())
            {
                for (IRInstruction instruction = basicBlock.getHeadInst(); instruction != null; instruction = instruction.getNext())
                {
                    if (instruction instanceof Load)
                    {
                        // load something in now stack frame, make addr is rbp + offset
                        if (((Load) instruction).getAddr() instanceof StackSlot)
                        {
                            ((Load) instruction).setOffset(funcInfo.stackSlotOffsetMap.get(((Load) instruction).getAddr()));
                            ((Load) instruction).setAddr(rbp);
                        }
                    }
                    else if (instruction instanceof Store)
                    {
                        // same as load
                        if (((Store) instruction).getAddr() instanceof StackSlot)
                        {
                            ((Store) instruction).setOffset(funcInfo.stackSlotOffsetMap.get(((Store) instruction).getAddr()));
                            ((Store) instruction).setAddr(rbp);
                        }
                    }
                    else if (instruction instanceof HeapAllocate)
                    {
                        // WARNING: prepend & append is based on this inst. attention to reverise
                        // call malloc to allocate heap
                        int callerSaveNum = 0;
                        for (PhysicalReg pReg : funcInfo.usedCallerSaveRegs)
                        {
                            // push 1 2 3 4 5
                            instruction.prepend(new Push(instruction.getFatherBlock(), pReg));
                            callerSaveNum++;
                        }
                        // move size to rdi, for malloc() arg
                        instruction.prepend(new Move(instruction.getFatherBlock(), rdi, ((HeapAllocate) instruction).getAllocateSize()));
                        if (callerSaveNum % 2 == 1)
                            instruction.prepend(new Push(instruction.getFatherBlock(), new Immediate(0)));
                        // processing malloc...... oh yh malloc() return, return value rax is the address!!!
                        instruction.append(new Move(instruction.getFatherBlock(), ((HeapAllocate) instruction).getDestReg() ,rax));

                        // pop callersave regs
                        // pop 5 4 3 2 1
                        // inst pop to 5 4 3 2 1.....OK!
                        for (PhysicalReg pReg : funcInfo.usedCallerSaveRegs)
                            instruction.append(new Pop(instruction.getFatherBlock(), pReg));
                        // align stack before pop
                        if (callerSaveNum % 2 == 1)
                            instruction.append(new BinaryOperation(instruction.getFatherBlock(), rsp, rsp, IRBinaryOp.ADD, new Immediate(Config.regSize)));

                    }
                    else if (instruction instanceof FuncCall)
                    {
                        IRFunction calleeFunc = ((FuncCall) instruction).getFunction();
                        RegFormFuncInfo calleeInfo = targetFuncInfoMap.get(calleeFunc);
                        int callerSaveNum = 0;

                        for (PhysicalReg pReg : funcInfo.usedCallerSaveRegs)
                        {
                            // save the caller save regs who will be changed in the callee function :)
                            if (!(pReg.isArgForced() && pReg.getFuncArgIndex() < irFunction.getParavRegList().size()) && calleeInfo.recursiveUsedRegSet.contains(pReg))
                            {
                                callerSaveNum++;
                                instruction.prepend(new Push(instruction.getFatherBlock(), pReg));
                            }
                        }

                        // save parament reg values
                        int regParaNum = min(irFunction.getParavRegList().size(), 6);
                        // push 6 5 4 3 2 1
                        for (int i = 0; i < regParaNum; i++)
                            instruction.prepend(new Push(instruction.getFatherBlock(), funcParaRegs.get(i)));

                        callerSaveNum += regParaNum;

                        // prepare function paraments
                        List<IRValue> paras = ((FuncCall) instruction).getParas();
                        int paraBackOffset = 0;
                        List<Integer> stackParasBackOffset = new ArrayList<Integer>();
                        Map<PhysicalReg, Integer> stackParasBackOffsetMap = new HashMap<PhysicalReg, Integer>();
                        // align stack
                        boolean alignPush = false;
                        if ((callerSaveNum + calleeInfo.extraParasNum) % 2 == 1)
                        {
                            alignPush = true;
                            instruction.prepend(new Push(instruction.getFatherBlock(), new Immediate(0)));
                        }
                        for (int i = paras.size() - 1; i >= 6; i--)
                        {
                            if(paras.get(i) instanceof StackSlot)
                            {
                                // use rax as a tmp. it's safe.
                                instruction.prepend(new Load(instruction.getFatherBlock(), rax, rbp, Config.regSize, funcInfo.stackSlotOffsetMap.get(paras.get(i))));
                                instruction.prepend(new Push(instruction.getFatherBlock(), rax));
                            }
                            else
                                instruction.prepend(new Push(instruction.getFatherBlock(), paras.get(i)));
                        }

                        int calleeRegParaNum = min(6, paras.size());
                        // update Map of parasReg-offset that need to back up to stack
                        // all para reg used by callee func need to be backup
                        for (int i = 0; i < calleeRegParaNum; i++)
                        {
                            IRValue para = paras.get(i);
                            if (para instanceof PhysicalReg && ((PhysicalReg) para).isArgForced() && ((PhysicalReg) para).getFuncArgIndex() < paras.size())
                            {
                                PhysicalReg paraPReg = (PhysicalReg) para;
                                if (!stackParasBackOffsetMap.containsKey(paraPReg))
                                {
                                    stackParasBackOffset.add(paraBackOffset);
                                    stackParasBackOffsetMap.put(paraPReg, paraBackOffset);
                                    instruction.prepend(new Push(instruction.getFatherBlock(), paraPReg));
                                    paraBackOffset++;
                                }
                                else
                                    stackParasBackOffset.add(stackParasBackOffsetMap.get(paraPReg));
                            }
                            else
                                stackParasBackOffset.add(-1);
                        }

                        // put first 6 paras into para regs
                        for (int i = 0; i < calleeRegParaNum; i++)
                        {
                            // these paras are not in paraReg
                            if (stackParasBackOffset.get(i) == -1)
                            {
                                if (paras.get(i) instanceof StackSlot)
                                {
                                    instruction.prepend(new Load(instruction.getFatherBlock(), rax, rbp, Config.regSize, funcInfo.stackSlotOffsetMap.get(paras.get(i))));
                                    instruction.prepend(new Move(instruction.getFatherBlock(), funcParaRegs.get(i), rax));
                                }
                                else
                                    instruction.prepend(new Move(instruction.getFatherBlock(), funcParaRegs.get(i), paras.get(i)));
                            }
                            else // paras are in paraReg, but we push them to stack before
                                instruction.prepend(new Load(instruction.getFatherBlock(), funcParaRegs.get(i), rsp, Config.regSize, (paraBackOffset - stackParasBackOffset.get(i) - 1)* Config.regSize));
                        }

                        // process stack frame
                        if (paraBackOffset > 0)
                            instruction.prepend(new BinaryOperation(instruction.getFatherBlock(), rsp, rsp, IRBinaryOp.ADD, new Immediate(paraBackOffset * Config.regSize)));

                        // Calling function ..... it returns ! :)

                        if (((FuncCall) instruction).getRetReg() != null)
                            instruction.append(new Move(instruction.getFatherBlock(), ((FuncCall) instruction).getRetReg(), rax));

                        // restore caller save
                        for (PhysicalReg pReg : funcInfo.usedCallerSaveRegs)
                        {
                            if (!(pReg.isArgForced() && pReg.getFuncArgIndex() < irFunction.getParavRegList().size()) && calleeInfo.recursiveUsedRegSet.contains(pReg))
                                instruction.append(new Pop(instruction.getFatherBlock(), pReg));
                        }

                        // restore paraRegs
                        for (int i = 0; i < regParaNum; i++)
                        {
                            instruction.append(new Pop(instruction.getFatherBlock(), funcParaRegs.get(i)));
                        }

                        //  remove extra push of stack
                        if (alignPush || calleeInfo.extraParasNum > 0)
                        {
                            int offset = calleeInfo.extraParasNum + (alignPush ? 1 : 0);
                            instruction.append(new BinaryOperation(instruction.getFatherBlock(), rsp, rsp, IRBinaryOp.ADD, new Immediate(offset * Config.regSize)));
                        }
                    }
                    else if (instruction instanceof Move)
                    {
                        if (((Move) instruction).getDestReg() == ((Move) instruction).getValue())
                            instruction.remove();
                    }
                }
            }

            // process function return
            Return retInst = irFunction.getReturnInstList().get(0); // return should be the only inst in a block
            if (retInst.getRetValue() != null)
                retInst.prepend(new Move(retInst.getFatherBlock(), rax, retInst.getRetValue()));
            BasicBlock blockLeave = irFunction.getBlockLeave();
            IRInstruction tailInst = blockLeave.getTailInst();
            if (funcInfo.stackSlotNum > 0)
                tailInst.prepend(new BinaryOperation(tailInst.getFatherBlock(), rsp, rsp, IRBinaryOp.ADD ,new Immediate(Config.regSize * funcInfo.stackSlotNum)));
            // pop stack..... need reversed
            for (int i = funcInfo.usedCalleeSaveRegs.size() - 1; i >= 0; i--)
                tailInst.prepend(new Pop(blockEnter, funcInfo.usedCalleeSaveRegs.get(i)));
        }
    }

}
