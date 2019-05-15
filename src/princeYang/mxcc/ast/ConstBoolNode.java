package princeYang.mxcc.ast;

public class ConstBoolNode extends ConstNode
{
    private boolean value;

    public ConstBoolNode(Location location, boolean value)
    {
        this.location = location;
        this.constType = new BoolType();
        this.value = value;
    }

    public boolean getValue()
    {
        return value;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
