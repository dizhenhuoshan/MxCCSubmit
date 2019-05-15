package princeYang.mxcc.ir;

public abstract class IRReg extends IRValue
{
    public abstract void accept(IRVisitor visitor);
}
