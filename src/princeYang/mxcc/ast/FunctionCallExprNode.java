package princeYang.mxcc.ast;
import princeYang.mxcc.scope.FuncEntity;

import java.util.List;

public class FunctionCallExprNode extends ExprNode
{
    private ExprNode funcExpr;
    private List<ExprNode> paraList;
    private FuncEntity funcEntity;

    public FunctionCallExprNode(Location location, ExprNode funcExpr, List<ExprNode> paraList)
    {
        this.location = location;
        this.assocType = AssocType.LEFT;
        this.funcExpr = funcExpr;
        this.paraList = paraList;
    }

    public ExprNode getFuncExpr()
    {
        return funcExpr;
    }

    public List<ExprNode> getParaList()
    {
        return paraList;
    }

    public FuncEntity getFuncEntity()
    {
        return funcEntity;
    }

    public void setFuncEntity(FuncEntity funcEntity)
    {
        this.funcEntity = funcEntity;
    }

    @Override
    public void accept(AstVisitor visitor)
    {
        visitor.visit(this);
    }
}
