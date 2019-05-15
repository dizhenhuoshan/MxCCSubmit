package princeYang.mxcc.backend;

import princeYang.mxcc.Config;
import princeYang.mxcc.ir.*;

import java.io.PrintStream;
import java.util.*;

public class LivenessAnalyst
{
    private IRROOT irRoot;
    private Map<BasicBlock, BasicBlock> jumpTargetReplaceMap = new HashMap<BasicBlock, BasicBlock>();

    public LivenessAnalyst(IRROOT irRoot)
    {
        this.irRoot = irRoot;
    }

    public void analyseLiveness(IRFunction function)
    {
        Set<VirtualReg> liveIn = new HashSet<VirtualReg>();
        Set<VirtualReg> liveOut = new HashSet<VirtualReg>();
        boolean done = false;
        // init liveness set
        // cautious: reversed PRE order need.
        List<BasicBlock> reversePreOrder = function.getReversePreOrder();
        for (BasicBlock basicBlock : reversePreOrder)
        {
            for (IRInstruction instruction = basicBlock.getHeadInst(); instruction != null; instruction = instruction.getNext())
            {
                if (instruction.liveIn == null)
                    instruction.liveIn = new HashSet<VirtualReg>();
                else
                    instruction.liveIn.clear();
                if (instruction.liveOut == null)
                    instruction.liveOut = new HashSet<VirtualReg>();
                else
                    instruction.liveOut.clear();
            }
        }

        while (!done)
        {
            done = true;
            for (BasicBlock basicBlock : reversePreOrder)
            {
                for (IRInstruction instruction = basicBlock.getTailInst(); instruction != null; instruction = instruction.getPrev())
                {
                    liveIn.clear();
                    liveOut.clear();
                    if (instruction instanceof BranchBaseInst)
                    {
                        if (instruction instanceof Branch)
                        {
                            liveOut.addAll(((Branch) instruction).getThenBlock().getHeadInst().liveIn);
                            liveOut.addAll(((Branch) instruction).getElseBlock().getHeadInst().liveIn);
                        }
                        if(instruction instanceof Jump)
                            liveOut.addAll(((Jump) instruction).getTargetBlock().getHeadInst().liveIn);
                    }
                    else
                    {
                        if (instruction.getNext() != null)
                            liveOut.addAll(instruction.getNext().liveIn);
                    }
                    liveIn.addAll(liveOut);
                    IRReg definedReg = instruction.getDefinedReg();
                    if (definedReg instanceof VirtualReg)
                        liveIn.remove(definedReg);
                    for (IRReg usedReg : instruction.getUsedIRReg())
                    {
                        if (usedReg instanceof VirtualReg)
                            liveIn.add((VirtualReg) usedReg);
                    }
                    if (!instruction.liveIn.equals(liveIn))
                    {
                        done = false;
                        instruction.liveIn.clear();
                        instruction.liveIn.addAll(liveIn);
                    }
                    if (!instruction.liveOut.equals(liveOut))
                    {
                        done = false;
                        instruction.liveOut.clear();
                        instruction.liveOut.addAll(liveOut);
                    }
                }
            }
        }
    }

    private boolean checkHaveDefine(IRInstruction instruction)
    {
        return ((instruction instanceof BinaryOperation)
                || (instruction instanceof UnaryOperation)
                || (instruction instanceof Comparison)
                || (instruction instanceof Move)
                || (instruction instanceof Load)
                || (instruction instanceof HeapAllocate));
    }

    private boolean eliminateFlag;

    public void processEliminate(IRFunction function)
    {
        List<BasicBlock> reversePreOrder = function.getReversePreOrder();
        for (BasicBlock basicBlock : reversePreOrder)
        {
            IRInstruction prevInst;
            for (IRInstruction instruction = basicBlock.getTailInst(); instruction != null; instruction = prevInst)
            {
                prevInst = instruction.getPrev();
                if (checkHaveDefine(instruction))
                {
                    IRReg definedReg = instruction.getDefinedReg();
                    if (definedReg == null || !instruction.liveOut.contains(definedReg))
                    {
                        instruction.remove();
                        eliminateFlag = true;
                    }
                }
            }
        }

        for (IRFor irFor : irRoot.getIRForMap().values())
        {
            if (!irFor.isProcessed)
            {
                boolean outAccessFlag = false;
                if (irFor.stepBlock != null && irFor.stopBlock != null && irFor.loopBodyBlock != null && irFor.loopAfterBlock != null)
                {
                    List<BasicBlock> forBlockList = new ArrayList<>();
                    forBlockList.add(irFor.stopBlock);
                    forBlockList.add(irFor.stepBlock);
                    forBlockList.add(irFor.loopBodyBlock);
                    forBlockList.add(irFor.loopAfterBlock);
                    IRInstruction afterForHeadInst = irFor.loopAfterBlock.getHeadInst();

                    for (int i = 0; i < 3; i++)
                    {
                        for (IRInstruction instruction = forBlockList.get(i).getHeadInst(); instruction != null; instruction = instruction.getNext())
                        {
                            if (instruction instanceof FuncCall)
                            {
                                outAccessFlag = true;
                                continue;
                            }
                            if (instruction.getDefinedReg() != null)
                            {
                                if (afterForHeadInst.liveIn.contains(instruction.getDefinedReg()))
                                    outAccessFlag = true;
                                continue;
                            }
                            if (instruction instanceof Store)
                            {
                                outAccessFlag = true;
                                continue;
                            }
                            if (instruction instanceof Jump)
                            {
                                if (!forBlockList.contains(((Jump) instruction).getTargetBlock()))
                                    outAccessFlag = true;
                                continue;
                            }
                            if (instruction instanceof Branch)
                            {
                                if (!forBlockList.contains(((Branch) instruction).getThenBlock()) || !forBlockList.contains(((Branch) instruction).getElseBlock()))
                                    outAccessFlag = true;
                                continue;
                            }
                            if (instruction instanceof Push || instruction instanceof Return)
                            {
                                outAccessFlag = true;
                                continue;
                            }
                        }
                    }
                    if (!outAccessFlag)
                    {
                        irFor.stopBlock.reset();
                        irFor.stopBlock.setJumpInst(new Jump(irFor.stopBlock, irFor.loopAfterBlock));
                        irFor.isProcessed = true;
                    }
                }
            }
        }

    }

    private BasicBlock replaceJumpTarget(BasicBlock basicBlock)
    {
        BasicBlock jumpTarget = basicBlock;
        BasicBlock newJumpTarget = jumpTargetReplaceMap.get(basicBlock);
        while (newJumpTarget != null)
        {
            jumpTarget = newJumpTarget;
            newJumpTarget = jumpTargetReplaceMap.get(newJumpTarget);
        }
        return jumpTarget;
    }

    public void removeBlankBlock(IRFunction function)
    {
        jumpTargetReplaceMap.clear();
        for (BasicBlock basicBlock : function.getReversePostOrder())
        {
            if (basicBlock.getHeadInst() == basicBlock.getTailInst())
            {
                IRInstruction instruction = basicBlock.getHeadInst();
                if (instruction instanceof Jump)
                    jumpTargetReplaceMap.put(basicBlock, ((Jump) instruction).getTargetBlock());
            }
        }
        for (BasicBlock basicBlock : function.getReversePostOrder())
        {
            if (basicBlock.getTailInst() instanceof Branch)
            {
                Branch branchInst = (Branch) basicBlock.getTailInst();
                branchInst.setThenBlock(replaceJumpTarget(branchInst.getThenBlock()));
                branchInst.setElseBlock(replaceJumpTarget(branchInst.getElseBlock()));
                if (branchInst.getThenBlock() == branchInst.getElseBlock())
                    branchInst.replace(new Jump(basicBlock, branchInst.getThenBlock()));
            }
            else if (basicBlock.getTailInst() instanceof Jump)
            {
                Jump jumpInst = (Jump) basicBlock.getTailInst();
                jumpInst.setTargetBlock(replaceJumpTarget(jumpInst.getTargetBlock()));
            }
        }
    }


    public void processLivenessWithEliminate()
    {
        for (IRFunction function : irRoot.getFunctionMap().values())
            analyseLiveness(function);
        eliminateFlag = true;
        while (eliminateFlag)
        {
            eliminateFlag = false;
            for (IRFunction function : irRoot.getFunctionMap().values())
            {
                if (!function.isBuildIn())
                {
                    processEliminate(function);
                    removeBlankBlock(function);
                    analyseLiveness(function);
                }
            }
        }
    }


}
