package princeYang.mxcc.ast;

public class BreakStateNode extends StateNode
{
    public BreakStateNode(Location location)
    {
        this.location = location;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
