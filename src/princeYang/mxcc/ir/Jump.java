package princeYang.mxcc.ir;

import java.util.Map;

public class Jump extends BranchBaseInst
{
    private BasicBlock targetBlock;

    public Jump(BasicBlock parentBlock, BasicBlock targetBlock)
    {
        super(parentBlock);
        this.targetBlock = targetBlock;
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
    }

    @Override
    public void setUsedIRReg(Map<IRReg, IRReg> renameMap)
    {
    }

    @Override
    public BranchBaseInst copyAndRename(Map<Object, Object> renameMap)
    {
        return new Jump((BasicBlock) renameMap.getOrDefault(getFatherBlock(), getFatherBlock()),
                (BasicBlock) renameMap.getOrDefault(targetBlock, targetBlock));
    }

    @Override
    public IRReg getDefinedReg()
    {
        return null;
    }

    @Override
    public void setDefinedReg(IRReg vIRReg)
    {
    }

    public BasicBlock getTargetBlock()
    {
        return targetBlock;
    }

    public void setTargetBlock(BasicBlock targetBlock)
    {
        this.targetBlock = targetBlock;
    }
}
