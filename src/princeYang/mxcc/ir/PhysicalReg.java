package princeYang.mxcc.ir;

public abstract class PhysicalReg extends IRReg
{
    @Override
    public void accept(IRVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public IRValue copy()
    {
        return null;
    }

    public abstract boolean isCallerSave();
    public abstract boolean isCalleeSave();
    public abstract boolean isGeneral();
    public abstract boolean isArgForced();
    public abstract int getFuncArgIndex();
    public abstract String getName();
}
