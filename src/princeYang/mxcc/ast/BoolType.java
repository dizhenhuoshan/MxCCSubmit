package princeYang.mxcc.ast;

public class BoolType extends Type
{
    public BoolType()
    {
        this.baseType = BaseType.DTYPE_BOOL;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj.getClass().equals(BoolType.class);
    }
}
