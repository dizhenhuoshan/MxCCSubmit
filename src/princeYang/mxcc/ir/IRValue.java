package princeYang.mxcc.ir;

public abstract class IRValue
{
    public abstract void accept(IRVisitor visitor);
    public abstract IRValue copy();
}