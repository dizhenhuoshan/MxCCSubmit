package princeYang.mxcc.ast;

public class ThisExprNode extends ExprNode
{
    public ThisExprNode(Location location)
    {
        this.location = location;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
