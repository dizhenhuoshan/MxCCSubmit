package princeYang.mxcc.scope;

import princeYang.mxcc.ast.Type;

abstract public class Entity
{
    Type type;
    String ident;

    public Entity(){}

    public Entity(String ident, Type type)
    {
        this.type = type;
        this.ident = ident;
    }

    public Type getType()
    {
        return type;
    }

    public String getIdent()
    {
        return ident;
    }
}
