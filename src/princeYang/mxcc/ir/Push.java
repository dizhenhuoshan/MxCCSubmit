package princeYang.mxcc.ir;

import java.util.Map;

public class Push extends IRInstruction
{

    private IRValue sourceValue;

    public Push(BasicBlock basicBlock, IRValue sourceValue)
    {
        super(basicBlock);
        this.sourceValue = sourceValue;
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
        usedIRValue.add(sourceValue);
        if (sourceValue instanceof IRReg)
            usedIRReg.add((IRReg) sourceValue);
    }

    @Override
    public void setUsedIRReg(Map<IRReg, IRReg> renameMap)
    {
        if (!(sourceValue instanceof StackSlot) && sourceValue instanceof IRReg)
            this.sourceValue = renameMap.get(sourceValue);
        reloadUsedRV();
    }

    @Override
    public IRInstruction copyAndRename(Map<Object, Object> renameMap)
    {
        return null;
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

    public IRValue getSourceValue()
    {
        return sourceValue;
    }
}
