package princeYang.mxcc.ir;

import java.util.Map;

public class Return extends BranchBaseInst
{
    private IRValue retValue;

    public Return(BasicBlock basicBlock, IRValue retValue)
    {
        super(basicBlock);
        this.retValue = retValue;
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
        usedIRReg.clear();
        usedIRValue.clear();
        if (retValue != null)
            usedIRValue.add(retValue);
        if (retValue != null && retValue instanceof IRReg)
            usedIRReg.add((IRReg) retValue);
    }

    @Override
    public void setUsedIRReg(Map<IRReg, IRReg> renameMap)
    {
        if (retValue != null && retValue instanceof IRReg)
            this.retValue = renameMap.get(retValue);
        reloadUsedRV();
    }

    @Override
    public BranchBaseInst copyAndRename(Map<Object, Object> renameMap)
    {
        return new Return((BasicBlock) renameMap.getOrDefault(getFatherBlock(), getFatherBlock()),
                (IRValue) renameMap.getOrDefault(retValue, retValue));
    }

    @Override
    public IRReg getDefinedReg()
    {
        return null;
    }

    @Override
    public void setDefinedReg(IRReg vIRReg)
    {
        this.retValue = vIRReg;
    }

    public IRValue getRetValue()
    {
        return retValue;
    }

    public void setRetValue(IRValue retValue)
    {
        this.retValue = retValue;
    }
}
