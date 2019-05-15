package princeYang.mxcc.ast;

public class IfStateNode extends StateNode
{
    private ExprNode conditionExpr;
    private StateNode thenState;
    private StateNode elseState;

    public IfStateNode(Location location, ExprNode conditionExpr, StateNode thenState, StateNode elseState)
    {
        this.location = location;
        this.conditionExpr = conditionExpr;
        this.thenState = thenState;
        this.elseState = elseState;
    }

    public ExprNode getConditionExpr()
    {
        return conditionExpr;
    }

    public StateNode getThenState()
    {
        return thenState;
    }

    public StateNode getElseState()
    {
        return elseState;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
