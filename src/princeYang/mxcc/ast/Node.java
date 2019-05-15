package princeYang.mxcc.ast;

abstract public class Node
{
    Location location;

    public Location getLocation()
    {
        return location;
    }

    abstract public void accept(AstVisitor visitor);
}
