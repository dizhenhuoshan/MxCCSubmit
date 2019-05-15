package princeYang.mxcc.ir;

import java.util.Map;

public class Pop extends IRInstruction
{

    private PhysicalReg targetReg;

    public Pop(BasicBlock basicBlock, PhysicalReg targetReg)
    {
        super(basicBlock);
        this.targetReg = targetReg;
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

    public PhysicalReg getTargetReg()
    {
        return targetReg;
    }

    public void setTargetReg(PhysicalReg targetReg)
    {
        this.targetReg = targetReg;
    }
}
