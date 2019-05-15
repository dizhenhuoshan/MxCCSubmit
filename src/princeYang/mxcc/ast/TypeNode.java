package princeYang.mxcc.ast;

public class TypeNode extends Node
{
    Type type;
    public TypeNode(Location location, Type type)
    {
        this.location = location;
        this.type = type;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
