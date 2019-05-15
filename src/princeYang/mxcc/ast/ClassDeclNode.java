package princeYang.mxcc.ast;
import java.util.List;

public class ClassDeclNode extends DeclNode
{
    private List<VarDeclNode> varDeclList;
    private List<FuncDeclNode> funcDeclList;

    public ClassDeclNode(Location location, String identName, List<VarDeclNode> varDeclList, List<FuncDeclNode> funcDeclList)
    {
        this.location = location;
        this.identName = identName;
        this.varDeclList = varDeclList;
        this.funcDeclList = funcDeclList;
    }

    public List<FuncDeclNode> getFuncDeclList()
    {
        return funcDeclList;
    }

    public List<VarDeclNode> getVarDeclList()
    {
        return varDeclList;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
