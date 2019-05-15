package princeYang.mxcc.ir;

import princeYang.mxcc.Config;

public class StaticStr extends StaticData
{
    private String staticValue;

    public StaticStr(String staticValue, int size)
    {
        super("static_str", size);
        this.staticValue = staticValue;
    }

    @Override
    public void accept(IRVisitor visitor)
    {
        visitor.visit(this);
    }

    public String getStaticValue()
    {
        return staticValue;
    }

    public void setStaticValue(String staticValue)
    {
        this.staticValue = staticValue;
    }
}
