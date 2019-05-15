package princeYang.mxcc.ast;

import princeYang.mxcc.scope.Scope;

import java.util.List;

public class MxProgNode extends Node
{
    private List<DeclNode> declNodesList;

    public MxProgNode(Location location, List<DeclNode> declNodesList)
    {
        this.location = location;
        this.declNodesList = declNodesList;
    }

    public List<DeclNode> getDeclNodesList()
    {
        return declNodesList;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
