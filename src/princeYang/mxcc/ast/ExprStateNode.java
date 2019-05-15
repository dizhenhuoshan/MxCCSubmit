package princeYang.mxcc.ast;

public class ExprStateNode extends StateNode
{
    private ExprNode exprState;
    public ExprStateNode(Location location, ExprNode exprState)
    {
        this.location = location;
        this.exprState = exprState;
    }

    public ExprNode getExprState()
    {
        return exprState;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
