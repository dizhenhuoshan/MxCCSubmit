package princeYang.mxcc.ast;

public class WhileStateNode extends StateNode
{
    private ExprNode conditionExpr;
    private StateNode loopState;

    public WhileStateNode(Location location, ExprNode conditionExpr, StateNode loopState)
    {
        this.location = location;
        this.conditionExpr = conditionExpr;
        this.loopState = loopState;
    }

    public ExprNode getConditionExpr()
    {
        return conditionExpr;
    }

    public StateNode getLoopState()
    {
        return loopState;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
