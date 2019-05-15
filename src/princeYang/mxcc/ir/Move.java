package princeYang.mxcc.ir;

import java.util.Map;

public class Move extends IRInstruction
{
    private IRReg destReg;
    private IRValue value;

    public Move(BasicBlock basicBlock, IRReg destReg, IRValue value)
    {
        super(basicBlock);
        this.destReg = destReg;
        this.value = value;
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
        usedIRValue.add(value);
        if (value instanceof IRReg)
            usedIRReg.add((IRReg) value);
    }

    @Override
    public void setUsedIRReg(Map<IRReg, IRReg> renameMap)
    {
        if (value instanceof IRReg)
            value = renameMap.get(value);
        reloadUsedRV();
    }

    @Override
    public IRInstruction copyAndRename(Map<Object, Object> renameMap)
    {
        return new Move((BasicBlock) renameMap.getOrDefault(getFatherBlock(), getFatherBlock()),
                (IRReg) renameMap.getOrDefault(destReg, destReg), (IRValue) renameMap.getOrDefault(value, value));
    }

    @Override
    public IRReg getDefinedReg()
    {
        return destReg;
    }

    @Override
    public void setDefinedReg(IRReg vIRReg)
    {
        this.destReg = vIRReg;
    }

    public IRReg getDestReg()
    {
        return destReg;
    }

    public IRValue getValue()
    {
        return value;
    }
}
