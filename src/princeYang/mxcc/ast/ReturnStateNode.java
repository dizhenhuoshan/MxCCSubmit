package princeYang.mxcc.ast;

public class ReturnStateNode extends StateNode
{
    private ExprNode retExpr;

    public ReturnStateNode(Location location, ExprNode retExpr)
    {
        this.location = location;
        this.retExpr = retExpr;
    }

    public ExprNode getRetExpr()
    {
        return retExpr;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
