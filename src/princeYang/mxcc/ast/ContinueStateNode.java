package princeYang.mxcc.ast;

public class ContinueStateNode extends StateNode
{
    public ContinueStateNode(Location location)
    {
        this.location = location;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
