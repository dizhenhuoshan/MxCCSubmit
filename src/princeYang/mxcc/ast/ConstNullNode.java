package princeYang.mxcc.ast;

public class ConstNullNode extends ConstNode
{
    public ConstNullNode(Location location)
    {
        this.location = location;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
