package princeYang.mxcc.ast;

public class PostFixExprNode extends ExprNode
{
    private ExprNode preExpr;
    private Operators.PostFixOp postFixOp;

    public PostFixExprNode(Location location, ExprNode preExpr, Operators.PostFixOp postFixOp)
    {
        this.location = location;
        this.assocType = AssocType.LEFT;
        this.preExpr = preExpr;
        this.postFixOp = postFixOp;
    }

    public ExprNode getPreExpr()
    {
        return preExpr;
    }

    public Operators.PostFixOp getPostFixOp()
    {
        return postFixOp;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
