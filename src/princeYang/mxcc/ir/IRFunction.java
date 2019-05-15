package princeYang.mxcc.ir;

import princeYang.mxcc.frontend.IRBuilder;
import princeYang.mxcc.scope.FuncEntity;

import java.util.*;

public class IRFunction
{
    private String funcName;
    private IRFuncType funcType;
    private BasicBlock blockEnter;
    private BasicBlock blockLeave;
    private boolean hasRetValue = false, hasRecursiveCall = false, isInClass = false;
    private String buildInName;
    private boolean isBuildIn = false;
    private FuncEntity funcEntity;

    private List<VirtualReg> paravRegList = new ArrayList<VirtualReg>();
    private List<BasicBlock> reversePreOrder = null, reversePostOrder = null;
    private List<StackSlot> stackSlotList = new ArrayList<StackSlot>();
    private Set<BasicBlock> dfsVisitedBlock;
    private Map<VirtualReg, StackSlot> paraSlotMap = new HashMap<VirtualReg, StackSlot>();
    private Set<PhysicalReg> usedGeneralPReg = new HashSet<PhysicalReg>();

    public List<Return> returnInstList = new ArrayList<Return>();
    public Set<IRFunction> calleeSet = new HashSet<IRFunction>();
    public Set<IRFunction> recurCalleeSet = new HashSet<IRFunction>();


    public IRFunction() {}

    public IRFunction(String funcName, String buildInName)
    {
        this.funcName = funcName;
        this.buildInName = buildInName;
        this.isBuildIn = true;
        this.funcEntity = null;
        this.funcType = IRFuncType.BuildIn;
    }

    public IRFunction(FuncEntity funcEntity)
    {
        this.funcName = funcEntity.getIdent();
        this.funcEntity = funcEntity;
        if (funcEntity.isInClass())
        {
            funcName = IRBuilder.genClassFuncName(funcEntity.getClassIdent(), funcName);
            this.isInClass = true;
        }
    }

    public List<StackSlot> getStackSlotList()
    {
        return stackSlotList;
    }

    public String getFuncName()
    {
        return funcName;
    }

    public boolean isBuildIn()
    {
        return isBuildIn;
    }

    public String getBuildInName()
    {
        return buildInName;
    }

    public FuncEntity getFuncEntity()
    {
        return funcEntity;
    }

    public void updateCalleeSet()
    {
        calleeSet.clear();
        for (BasicBlock basicBlock : getReversePostOrder())
        {
            IRInstruction instruction = null;
            for (instruction = basicBlock.getHeadInst(); instruction != null;
                 instruction = instruction.getNext())
                if (instruction instanceof FuncCall)
                    calleeSet.add(((FuncCall) instruction).getFunction());
        }
    }

    public void insertArgReg(VirtualReg virtualReg)
    {
        paravRegList.add(virtualReg);
    }

    public void generateEntry()
    {
        this.blockEnter = new BasicBlock(this, "__entry__" + funcName);
    }

    public BasicBlock getBlockEnter()
    {
        return blockEnter;
    }

    public BasicBlock getBlockLeave()
    {
        return blockLeave;
    }

    public void setBlockEnter(BasicBlock blockEnter)
    {
        this.blockEnter = blockEnter;
    }

    public void setBlockLeave(BasicBlock blockLeave)
    {
        this.blockLeave = blockLeave;
    }

    public void setParavRegList(List<VirtualReg> paravRegList)
    {
        this.paravRegList = paravRegList;
    }

    public void setRecursive(boolean hasRecursive)
    {
        this.hasRecursiveCall = hasRecursive;
    }

    public boolean isRecursive()
    {
        return hasRecursiveCall;
    }

    public void setInClass(boolean inClass)
    {
        isInClass = inClass;
    }

    public boolean isInClass()
    {
        return isInClass;
    }

    public List<VirtualReg> getParavRegList()
    {
        return paravRegList;
    }

    public List<Return> getReturnInstList()
    {
        return returnInstList;
    }

    public Map<VirtualReg, StackSlot> getParaSlotMap()
    {
        return paraSlotMap;
    }

    public Set<PhysicalReg> getUsedGeneralPReg()
    {
        return usedGeneralPReg;
    }

    private void dfsPostBlock(BasicBlock basicBlock)
    {
        if (dfsVisitedBlock.contains(basicBlock))
            return;
        dfsVisitedBlock.add(basicBlock);
        for (BasicBlock block : basicBlock.getNextBlocks())
            dfsPostBlock(block);
        reversePostOrder.add(basicBlock);
    }

    public void postOrderProcessor()
    {
        dfsVisitedBlock = new HashSet<BasicBlock>();
        reversePostOrder = new ArrayList<BasicBlock>();
        dfsPostBlock(blockEnter);
        dfsVisitedBlock = null;
        for (int i = 0; i < reversePostOrder.size(); i++)
            reversePostOrder.get(i).setPostOrderIndex(i);
        Collections.reverse(reversePostOrder);
    }

    public List<BasicBlock> getReversePostOrder()
    {
        if (reversePostOrder == null)
            postOrderProcessor();
        return reversePostOrder;
    }

    private void dfsPreBlock(BasicBlock basicBlock)
    {
        if (dfsVisitedBlock.contains(basicBlock))
            return;
        dfsVisitedBlock.add(basicBlock);
        reversePreOrder.add(basicBlock);
        for (BasicBlock block : basicBlock.getNextBlocks())
            dfsPreBlock(block);
    }

    private void preOrderProcessor()
    {
        dfsVisitedBlock = new HashSet<BasicBlock>();
        reversePreOrder = new ArrayList<BasicBlock>();
        dfsPreBlock(blockEnter);
        dfsVisitedBlock = null;
        for (int i = 0; i < reversePreOrder.size(); i++)
            reversePreOrder.get(i).setPreOrderIndex(i);
        Collections.reverse(reversePreOrder);
    }

    public List<BasicBlock> getReversePreOrder()
    {
        if (reversePreOrder == null)
            preOrderProcessor();
        return reversePreOrder;
    }

    public void refreshCFG(BasicBlock newBlockEnter, BasicBlock newBlockLeave)
    {
        reversePostOrder = null;
        reversePreOrder = null;
        this.blockEnter = newBlockEnter;
        this.blockLeave = newBlockLeave;
    }

    public void accept(IRVisitor visitor)
    {
        visitor.visit(this);
    }
}
