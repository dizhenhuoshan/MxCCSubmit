package princeYang.mxcc.ir;

import princeYang.mxcc.ast.ExprNode;

public class GlobalVarInit
{
    private String varName;
    private ExprNode initValue;

    public GlobalVarInit(String varName, ExprNode initValue)
    {
        this.varName = varName;
        this.initValue = initValue;
    }

    public ExprNode getInitValue()
    {
        return initValue;
    }

    public String getVarName()
    {
        return varName;
    }
}
