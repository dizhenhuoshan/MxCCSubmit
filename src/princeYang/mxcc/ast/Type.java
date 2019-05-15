package princeYang.mxcc.ast;

import princeYang.mxcc.Config;

public class Type
{
    BaseType baseType;

    public BaseType getBaseType()
    {
        return baseType;
    }

    public int getSize()
    {
        return Config.regSize;
    }
}
