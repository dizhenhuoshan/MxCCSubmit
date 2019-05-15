package princeYang.mxcc.ast;

public class ArrayAccessExprNode extends ExprNode
{
    private ExprNode arrExpr;
    private ExprNode subExpr;

    public ArrayAccessExprNode(Location location, ExprNode arrExpr, ExprNode subExpr)
    {
        this.location = location;
        this.assocType = AssocType.LEFT;
        this.arrExpr = arrExpr;
        this.subExpr = subExpr;
    }

    public ExprNode getArrExpr()
    {
        return arrExpr;
    }

    public ExprNode getSubExpr()
    {
        return subExpr;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
