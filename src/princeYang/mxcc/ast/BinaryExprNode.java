package princeYang.mxcc.ast;

public class BinaryExprNode extends ExprNode
{
    private ExprNode lhs;
    private Operators.BinaryOp bop;
    private ExprNode rhs;

    public BinaryExprNode(Location location, ExprNode lhs, Operators.BinaryOp bop, ExprNode rhs)
    {
        this.location = location;
        this.assocType = AssocType.LEFT;
        this.lhs = lhs;
        this.bop = bop;
        this.rhs = rhs;
    }

    public ExprNode getLhs()
    {
        return lhs;
    }

    public Operators.BinaryOp getBop()
    {
        return bop;
    }

    public ExprNode getRhs()
    {
        return rhs;
    }

    public void setLhs(ExprNode lhs)
    {
        this.lhs = lhs;
    }

    public void setBop(Operators.BinaryOp bop)
    {
        this.bop = bop;
    }

    public void setRhs(ExprNode rhs)
    {
        this.rhs = rhs;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
