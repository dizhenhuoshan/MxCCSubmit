package princeYang.mxcc.ast;
import princeYang.mxcc.scope.Scope;

import java.util.List;

public class FuncBlockNode extends StateNode
{
    private List<Node> stateList;
    private Scope scope;

    public FuncBlockNode(Location location, List<Node> stateList)
    {
        this.location = location;
        this.stateList = stateList;
    }

    public List<Node> getStateList()
    {
        return stateList;
    }

    public Scope getScope()
    {
        return scope;
    }

    public void setScope(Scope scope)
    {
        this.scope = scope;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
