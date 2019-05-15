package princeYang.mxcc.ast;

public class ForStateNode extends StateNode
{
    private ExprNode startExpr;
    private ExprNode stopExpr;
    private ExprNode stepExpr;
    private StateNode loopState;

    public ForStateNode(Location location, ExprNode startExpr, ExprNode stopExpr, ExprNode stepExpr, StateNode loopState)
    {
        this.location = location;
        this.startExpr = startExpr;
        this.stopExpr = stopExpr;
        this.stepExpr = stepExpr;
        this.loopState = loopState;
    }

    public ExprNode getStartExpr()
    {
        return startExpr;
    }

    public ExprNode getStopExpr()
    {
        return stopExpr;
    }

    public ExprNode getStepExpr()
    {
        return stepExpr;
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
