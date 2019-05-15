package princeYang.mxcc.ast;

public class ArrayType extends Type
{
    private Type arrType;

    public ArrayType(Type arrType)
    {
        this.baseType = BaseType.STYPE_ARRAY;
        this.arrType = arrType;
    }

    public Type getArrType()
    {
        return arrType;
    }

    @Override
    public boolean equals(Object obj)
    {
        return (obj.getClass() == ArrayType.class) &&
                (((ArrayType) obj).getArrType().equals(arrType));
    }

    @Override
    public String toString() {
        return String.format("ArrayType(%s)", arrType.toString());
    }
}
