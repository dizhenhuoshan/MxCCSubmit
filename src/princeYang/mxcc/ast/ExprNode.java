package princeYang.mxcc.ast;

import princeYang.mxcc.ir.BasicBlock;
import princeYang.mxcc.ir.IRValue;

abstract public class ExprNode extends Node
{
    Type type;
    AssocType assocType;
    private boolean isLeftValue;
    private BasicBlock boolTrueBlock = null, boolFalseBlock = null;
    private IRValue addrValue = null, regValue = null;
    private int addrOffset;

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public boolean isLeftValue()
    {
        return isLeftValue;
    }

    public void setLeftValue(boolean leftValue)
    {
        isLeftValue = leftValue;
    }

    public AssocType getAssocType()
    {
        return assocType;
    }

    public BasicBlock getBoolTrueBlock()
    {
        return boolTrueBlock;
    }

    public BasicBlock getBoolFalseBlock()
    {
        return boolFalseBlock;
    }

    public void setBoolTrueBlock(BasicBlock boolTrueBlock)
    {
        this.boolTrueBlock = boolTrueBlock;
    }

    public void setBoolFalseBlock(BasicBlock boolFalseBlock)
    {
        this.boolFalseBlock = boolFalseBlock;
    }

    public IRValue getAddrValue()
    {
        return addrValue;
    }

    public IRValue getRegValue()
    {
        return regValue;
    }

    public void setAddrValue(IRValue addrValue)
    {
        this.addrValue = addrValue;
    }

    public void setRegValue(IRValue regValue)
    {
        this.regValue = regValue;
    }

    public int getAddrOffset()
    {
        return addrOffset;
    }

    public void setAddrOffset(int addrOffset)
    {
        this.addrOffset = addrOffset;
    }
}
