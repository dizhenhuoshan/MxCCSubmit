package princeYang.mxcc.ast;

import java.util.List;

public class NewExprNode extends ExprNode
{
    private Type newType;
    private int totalDim;
    private int knownDim;
    private List<ExprNode> knownDims;

    public NewExprNode(Location location, Type newType, int totalDim, int knownDim, List<ExprNode> knownDims)
    {
        this.location = location;
        this.assocType = AssocType.LEFT;
        this.newType = newType;
        this.totalDim = totalDim;
        this.knownDim = knownDim;
        this.knownDims = knownDims;
    }

    public Type getNewType()
    {
        return newType;
    }

    public int getTotalDim()
    {
        return totalDim;
    }

    public int getKnownDim()
    {
        return knownDim;
    }

    public List<ExprNode> getKnownDims()
    {
        return knownDims;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }

}
