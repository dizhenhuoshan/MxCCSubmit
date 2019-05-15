package princeYang.mxcc.ast;

import princeYang.mxcc.scope.VarEntity;

public class IdentExprNode extends ExprNode
{
    private String identName;
    private VarEntity varEntity;
    private boolean memAccessChecked, memAccessing;

    public IdentExprNode(Location location, String identName)
    {
        this.location = location;
        this.assocType = AssocType.LEFT;
        this.identName = identName;
    }

    public String getIdentName()
    {
        return identName;
    }

    public VarEntity getVarEntity()
    {
        return varEntity;
    }

    public void setVarEntity(VarEntity varEntity)
    {
        this.varEntity = varEntity;
    }

    public boolean hasMemAccessChecked()
    {
        return memAccessChecked;
    }

    public void setMemAccessChecked(boolean memAccessChecked)
    {
        this.memAccessChecked = memAccessChecked;
    }

    public boolean isMemAccessing()
    {
        return memAccessing;
    }

    public void setMemAccessing(boolean memAccessing)
    {
        this.memAccessing = memAccessing;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
