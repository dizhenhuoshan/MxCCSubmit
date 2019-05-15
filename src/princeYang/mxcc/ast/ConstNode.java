package princeYang.mxcc.ast;

abstract public class ConstNode extends ExprNode
{
    Type constType;
    AssocType assocType = AssocType.LEFT;
    public Type getConstType()
    {
        return constType;
    }
}
