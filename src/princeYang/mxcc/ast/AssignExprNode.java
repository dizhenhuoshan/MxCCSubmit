package princeYang.mxcc.ast;

public class AssignExprNode extends ExprNode
{
    private ExprNode lhs;
    private ExprNode rhs;

    public AssignExprNode(Location location, ExprNode lhs, ExprNode rhs)
    {
        this.location = location;
        this.assocType = AssocType.RIGHT;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public ExprNode getLhs()
    {
        return lhs;
    }

    public ExprNode getRhs()
    {
        return rhs;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
