package princeYang.mxcc.ast;

public class PreFixExprNode extends ExprNode
{
    private ExprNode postExpr;
    private Operators.PreFixOp preFixOp;

    public PreFixExprNode(Location location, ExprNode postExpr, Operators.PreFixOp preFixOp)
    {
        this.location = location;
        this.assocType = AssocType.RIGHT;
        this.postExpr = postExpr;
        this.preFixOp = preFixOp;
    }

    public ExprNode getPostExpr()
    {
        return postExpr;
    }

    public Operators.PreFixOp getPreFixOp()
    {
        return preFixOp;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
