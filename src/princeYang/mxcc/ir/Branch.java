package princeYang.mxcc.ir;

import java.util.Map;

public class Branch extends BranchBaseInst
{

    private IRValue cond;
    private BasicBlock thenBlock, elseBlock;

    public Branch(BasicBlock fatherBlock, IRValue cond, BasicBlock thenBlock, BasicBlock elseBlock)
    {
        super(fatherBlock);
        this.cond = cond;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        reloadUsedRV();
    }

    @Override
    public void accept(IRVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public void reloadUsedRV()
    {
        usedIRValue.clear();
        usedIRReg.clear();
        usedIRValue.add(cond);
        if (cond instanceof IRReg)
            usedIRReg.add((IRReg) cond);
    }

    @Override
    public void setUsedIRReg(Map<IRReg, IRReg> renameMap)
    {
        if (cond instanceof IRReg)
            cond = renameMap.get(cond);
        reloadUsedRV();
    }

    @Override
    public Branch copyAndRename(Map<Object, Object> renameMap)
    {
        return new Branch((BasicBlock) renameMap.getOrDefault(getFatherBlock(), getFatherBlock()),
                (IRValue) renameMap.getOrDefault(cond, cond),
                (BasicBlock) renameMap.getOrDefault(thenBlock, thenBlock),
                (BasicBlock) renameMap.getOrDefault(elseBlock, elseBlock));
    }

    @Override
    public IRReg getDefinedReg()
    {
        return null;
    }

    @Override
    public void setDefinedReg(IRReg vIRReg)
    {
        return;
    }

    public IRValue getCond()
    {
        return cond;
    }

    public BasicBlock getThenBlock()
    {
        return thenBlock;
    }

    public BasicBlock getElseBlock()
    {
        return elseBlock;
    }

    public void setCond(IRValue cond)
    {
        this.cond = cond;
    }

    public void setThenBlock(BasicBlock thenBlock)
    {
        this.thenBlock = thenBlock;
    }

    public void setElseBlock(BasicBlock elseBlock)
    {
        this.elseBlock = elseBlock;
    }
}
