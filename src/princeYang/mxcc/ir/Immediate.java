package princeYang.mxcc.ir;

public class Immediate extends IRValue
{
    private int value;

    public Immediate(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    public void accept(IRVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public IRValue copy()
    {
        return new Immediate(value);
    }
}
