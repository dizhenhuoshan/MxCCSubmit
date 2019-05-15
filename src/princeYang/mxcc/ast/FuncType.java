package princeYang.mxcc.ast;

public class FuncType extends Type
{
    String funcName;

    public FuncType(String funcName)
    {
        this.baseType = BaseType.STYPE_FUNC;
        this.funcName = funcName;
    }

    public String getFuncName()
    {
        return funcName;
    }

    @Override
    public boolean equals(Object obj)
    {
        return (obj.getClass() == FuncType.class) &&
                (((FuncType) obj).funcName.equals(funcName));
    }

    @Override
    public String toString()
    {
        return String.format("FuncType(%s)", funcName);
    }
}
