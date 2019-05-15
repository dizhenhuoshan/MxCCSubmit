package princeYang.mxcc.ast;

public class NullType extends Type
{
    public NullType()
    {
        this.baseType = BaseType.DTYPE_NULL;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj.getClass().equals(NullType.class);
    }

    @Override
    public String toString()
    {
        return "NullType";
    }
}
