package princeYang.mxcc.ast;

import java.util.List;

public class DeclarationsNode extends Node
{
    private List<DeclNode> declarationNodes;

    public DeclarationsNode(Location location, List<DeclNode> declarationNodes)
    {
        this.location = location;
        this.declarationNodes = declarationNodes;
    }

    public List<DeclNode> getDeclarationNodes()
    {
        return declarationNodes;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
