package princeYang.mxcc.ast;

public class MemoryAccessExprNode extends ExprNode
{
    private ExprNode hostExpr;
    private String memberStr;

    public MemoryAccessExprNode(Location location, ExprNode hostExpr, String memberStr)
    {
        this.location = location;
        this.assocType = AssocType.LEFT;
        this.hostExpr = hostExpr;
        this.memberStr = memberStr;
    }

    public ExprNode getHostExpr()
    {
        return hostExpr;
    }

    public String getMemberStr()
    {
        return memberStr;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
