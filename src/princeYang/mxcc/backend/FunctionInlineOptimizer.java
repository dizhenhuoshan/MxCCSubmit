package princeYang.mxcc.backend;

import princeYang.mxcc.Config;
import princeYang.mxcc.ir.*;

import java.util.*;

import princeYang.mxcc.frontend.IRBuilder;

public class FunctionInlineOptimizer
{

    private class InlineInfo
    {
        int instNum = 0;
        int callerNum = 0;
        boolean isClassFunc = false;
        boolean recursiveCall = false;
    }

    private IRROOT irRoot;
    private Map<IRFunction, InlineInfo> functionInlineInfoMap = new HashMap<IRFunction, InlineInfo>();
    private Map<IRFunction, IRFunction> functionBackupMap = new HashMap<IRFunction, IRFunction>();

    public FunctionInlineOptimizer(IRROOT irRoot)
    {
        this.irRoot = irRoot;
    }


    private void buildFunctionInfo()
    {
        for (IRFunction function : irRoot.getFunctionMap().values())
        {
            InlineInfo inlineInfo = new InlineInfo();
            function.setRecursive(function.recurCalleeSet.contains(function));
            inlineInfo.recursiveCall = function.isRecursive();
            inlineInfo.isClassFunc = function.isInClass();
            functionInlineInfoMap.put(function, inlineInfo);
        }
        for (IRFunction function : irRoot.getFunctionMap().values())
        {
            InlineInfo inlineInfo = functionInlineInfoMap.get(function);
            for (BasicBlock basicBlock : function.getReversePostOrder())
            {
                for (IRInstruction instruction = basicBlock.getHeadInst(); instruction != null; instruction = instruction.getNext())
                {
                    inlineInfo.instNum++;
                    if (instruction instanceof FuncCall)
                    {
                        InlineInfo calleeInlineInfo = functionInlineInfoMap.get(((FuncCall) instruction).getFunction());
                        if (calleeInlineInfo != null)
                            calleeInlineInfo.callerNum++;
                    }
                }
            }
        }
    }

    private void copyValue(Map<Object, Object> renameMap, IRValue value)
    {
        if (!renameMap.containsKey(value))
            renameMap.put(value, value.copy());
    }

    private IRFunction backupFunc(IRFunction function)
    {
        Map<Object, Object> blockRenameMap = new HashMap<Object, Object>();
        IRFunction backupFunction = new IRFunction();
        for (BasicBlock basicBlock : function.getReversePostOrder())
            blockRenameMap.put(basicBlock, new BasicBlock(backupFunction, basicBlock.getBlockName()));
        for (BasicBlock basicBlock : function.getReversePostOrder())
        {
            BasicBlock backupBlock = (BasicBlock) blockRenameMap.get(basicBlock);
            for (IRInstruction instruction = basicBlock.getHeadInst(); instruction != null; instruction = instruction.getNext())
            {
                if (instruction instanceof BranchBaseInst)
                    backupBlock.setJumpInst(((BranchBaseInst) instruction).copyAndRename(blockRenameMap));
                else
                    backupBlock.appendInst(instruction.copyAndRename(blockRenameMap));
            }
        }
        backupFunction.setBlockEnter((BasicBlock) blockRenameMap.get(function.getBlockEnter()));
        backupFunction.setBlockLeave((BasicBlock) blockRenameMap.get(function.getBlockLeave()));
        backupFunction.setParavRegList(function.getParavRegList());
        return backupFunction;
    }

    private IRInstruction modifyFuncCall(FuncCall funcCall)
    {
        Map<Object, Object> renameMap = new HashMap<Object, Object>();
        IRFunction callerFunc = funcCall.getFatherBlock().getParentFunction();
        IRFunction calleeFunc = functionBackupMap.getOrDefault(funcCall.getFunction(), funcCall.getFunction());
        BasicBlock oldBlockLeave = calleeFunc.getBlockLeave();
        BasicBlock newBlockLeave = new BasicBlock(callerFunc, oldBlockLeave.getBlockName());
        List<BasicBlock> calleeReversePostOrder = calleeFunc.getReversePostOrder();

        renameMap.put(oldBlockLeave, newBlockLeave);
        renameMap.put(calleeFunc.getBlockEnter(), funcCall.getFatherBlock());
        if (callerFunc.getBlockLeave() == funcCall.getFatherBlock())
            callerFunc.setBlockLeave(newBlockLeave);

        // move inst after func call to new block
        Map<Object, Object> callBlockRenameMap = Collections.singletonMap(funcCall.getFatherBlock(), newBlockLeave);
        for (IRInstruction instruction = funcCall.getNext(); instruction != null; instruction = instruction.getNext())
        {
            if (instruction instanceof BranchBaseInst)
                newBlockLeave.setJumpInst(((BranchBaseInst) instruction).copyAndRename(callBlockRenameMap));
            else
                newBlockLeave.appendInst(instruction.copyAndRename(callBlockRenameMap));
            instruction.remove();
        }

        // process virtual regs
        for (int i = 0; i < funcCall.getParas().size(); i++)
        {
            VirtualReg oldParaReg = calleeFunc.getParavRegList().get(i);
            VirtualReg newParaReg = oldParaReg.copy();
            renameMap.put(oldParaReg, newParaReg);
            funcCall.prepend(new Move(funcCall.getFatherBlock(), newParaReg, funcCall.getParas().get(i)));
        }
        funcCall.remove();

        // process callee function
        IRInstruction newBlockLeaveHeadInst = newBlockLeave.getHeadInst();
        for (BasicBlock basicBlock : calleeReversePostOrder)
        {
            if (!renameMap.containsKey(basicBlock))
                renameMap.put(basicBlock, new BasicBlock(callerFunc, basicBlock.getBlockName()));
        }
        for (BasicBlock oldBlock : calleeReversePostOrder)
        {
            BasicBlock newBlock = (BasicBlock) renameMap.get(oldBlock);
            // process for() in old block
            if (oldBlock.getForStateNode() != null)
            {
                IRFor irFor = irRoot.getIRForMap().get(oldBlock.getForStateNode());
                if (irFor.stopBlock == oldBlock)
                    irFor.stopBlock = newBlock;
                if (irFor.stepBlock == oldBlock)
                    irFor.stepBlock = newBlock;
                if (irFor.loopBodyBlock == oldBlock)
                    irFor.loopBodyBlock = newBlock;
                if (irFor.loopAfterBlock == oldBlock)
                    irFor.loopAfterBlock = newBlock;
            }

            // move instructions
            for (IRInstruction instruction = oldBlock.getHeadInst(); instruction != null; instruction = instruction.getNext())
            {
                for (IRValue usedValue : instruction.getUsedIRValue())
                    copyValue(renameMap, usedValue);
                if (instruction.getDefinedReg() != null)
                    copyValue(renameMap, instruction.getDefinedReg());
                if (newBlock != newBlockLeave)
                {
                    // not leave block, check jump / ordinary inst
                    if (instruction instanceof BranchBaseInst)
                    {
                        if(!(instruction instanceof Return))
                            newBlock.setJumpInst(((BranchBaseInst) instruction).copyAndRename(renameMap));
                    }
                    else
                        newBlock.appendInst(instruction.copyAndRename(renameMap));
                }
                else
                {
                    // remove return
                    if (!(instruction instanceof Return))
                        newBlockLeaveHeadInst.prepend(instruction.copyAndRename(renameMap));
                }
            }
        }

        if (!funcCall.getFatherBlock().isContainJump())
            funcCall.getFatherBlock().setJumpInst(new Jump(funcCall.getFatherBlock(), newBlockLeave));

        Return calleeReturnInst = calleeFunc.getReturnInstList().get(0);
        if (calleeReturnInst.getRetValue() != null)
            newBlockLeaveHeadInst.prepend(new Move(newBlockLeave, funcCall.getRetReg(), (IRValue) renameMap.get(calleeReturnInst.getRetValue())));
        return newBlockLeave.getHeadInst();
    }

    private void updateRecursiveCalleeSet()
    {
        Set<IRFunction> recursiveCalleeSet = new HashSet<IRFunction>();
        boolean flag = true;
        for (IRFunction irFunction : irRoot.getFunctionMap().values())
            irFunction.recurCalleeSet.clear();
        while (flag)
        {
            flag = false;
            for (IRFunction irFunction : irRoot.getFunctionMap().values())
            {
                recursiveCalleeSet.clear();
                recursiveCalleeSet.addAll(irFunction.calleeSet);
                for (IRFunction calleeFunc : irFunction.calleeSet)
                    recursiveCalleeSet.addAll(calleeFunc.recurCalleeSet);
                if (!(irFunction.recurCalleeSet.equals(recursiveCalleeSet)))
                {
                    irFunction.recurCalleeSet.clear();
                    irFunction.recurCalleeSet.addAll(recursiveCalleeSet);
                    flag = true;
                }
            }
        }
    }

    public void processInline()
    {
        buildFunctionInfo();

        // process nonRecursive functions
        List<String> nonCalledFuncs = new ArrayList<String>();
        List<BasicBlock> reversePostOrder = new ArrayList<BasicBlock>();
        boolean changeFlag = true;
        boolean nowChanged;
        while (changeFlag)
        {
            changeFlag = false;
            nonCalledFuncs.clear();
            for (IRFunction function : irRoot.getFunctionMap().values())
            {
                InlineInfo funcInlineInfo = functionInlineInfoMap.get(function);
                nowChanged = false;
                reversePostOrder.clear();
                reversePostOrder.addAll(function.getReversePostOrder());
                for (BasicBlock basicBlock : reversePostOrder)
                {
                    IRInstruction nextInst;
                    for(IRInstruction instruction = basicBlock.getHeadInst(); instruction != null; instruction = nextInst)
                    {
                        nextInst = instruction.getNext();
                        if (instruction instanceof FuncCall)
                        {
                            InlineInfo calleeInfo = functionInlineInfoMap.get(((FuncCall) instruction).getFunction());
                            if (calleeInfo == null)
                                continue;
                            if (calleeInfo.recursiveCall)
                                continue;
                            if (calleeInfo.isClassFunc)
                                continue;
                            if (calleeInfo.instNum > Config.INLINE_MAX_INST || calleeInfo.instNum + funcInlineInfo.instNum > Config.INLINE_FUNC_INST)
                                continue;

                            nextInst = modifyFuncCall((FuncCall) instruction);
                            funcInlineInfo.instNum += calleeInfo.instNum;
                            calleeInfo.callerNum--;
                            if (calleeInfo.callerNum == 0)
                                nonCalledFuncs.add(((FuncCall) instruction).getFunction().getFuncName());
                            nowChanged = true;
                            changeFlag = true;
                        }
                    }
                }
                if (nowChanged)
                    function.postOrderProcessor();
            }
            for (String nonCalledFunc : nonCalledFuncs)
                irRoot.getFunctionMap().remove(nonCalledFunc);
        }
        for (IRFunction irFunction : irRoot.getFunctionMap().values())
            irFunction.updateCalleeSet();
        updateRecursiveCalleeSet();

        // process recursive inline
        changeFlag = true;
        for(int i = 0; i < Config.INLINE_MAX_DEPTH && changeFlag; i++)
        {
            changeFlag = false;
            functionBackupMap.clear();
            for (IRFunction function : irRoot.getFunctionMap().values())
            {
                InlineInfo inlineInfo = functionInlineInfoMap.get(function);
                if (inlineInfo.recursiveCall)
                    functionBackupMap.put(function, backupFunc(function));
            }

            for (IRFunction function : irRoot.getFunctionMap().values())
            {
                InlineInfo funcInlineInfo = functionInlineInfoMap.get(function);
                nowChanged = false;
                reversePostOrder.clear();
                reversePostOrder.addAll(function.getReversePostOrder());
                for (BasicBlock basicBlock : function.getReversePostOrder())
                {
                    IRInstruction nextInst;
                    for(IRInstruction instruction = basicBlock.getHeadInst(); instruction != null; instruction = nextInst)
                    {
                        nextInst = instruction.getNext();
                        if (instruction instanceof FuncCall)
                        {
                            InlineInfo calleeInfo = functionInlineInfoMap.get(((FuncCall) instruction).getFunction());
                            if (calleeInfo == null)
                                continue;
                            if (calleeInfo.isClassFunc)
                                continue;
                            if (calleeInfo.instNum > Config.INLINE_MAX_INST || calleeInfo.instNum + funcInlineInfo.instNum > Config.INLINE_FUNC_INST)
                                continue;

                            nextInst = modifyFuncCall((FuncCall) instruction);
                            funcInlineInfo.instNum += calleeInfo.instNum;
                            nowChanged = true;
                            changeFlag = true;
                        }
                    }
                }
                if (nowChanged)
                    function.postOrderProcessor();
            }
        }
        for (IRFunction irFunction : irRoot.getFunctionMap().values())
            irFunction.updateCalleeSet();
        updateRecursiveCalleeSet();
    }
}
