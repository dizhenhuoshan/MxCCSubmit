package princeYang.mxcc.ast;

public class ConstIntNode extends ConstNode
{
    private int value;

    public ConstIntNode(Location location, int value)
    {
        this.location = location;
        this.constType = new IntType();
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
