package princeYang.mxcc.scope;

import princeYang.mxcc.ast.*;

import java.util.ArrayList;
import java.util.List;

public class FuncEntity extends Entity
{
    private Type retType;
    private String classIdent = null;
    private ConstructType constructType = ConstructType.NORMAL;
    private boolean inClass = false, isBuildIn = false;
    private Scope funcScope;
    private List<VarEntity> funcParas; // for function call

    private void constructByNode(FuncDeclNode funcDeclNode, Scope father)
    {
        this.type = new FuncType(funcDeclNode.getIdentName());
        this.ident = funcDeclNode.getIdentName();
        if (funcDeclNode.getRetType() != null)
            retType = funcDeclNode.getRetType().getType();
        else retType = null;
        funcScope = new Scope(father);
        funcParas = new ArrayList<VarEntity>();
        for (VarDeclNode varDeclNode : funcDeclNode.getParaDeclList())
        {
            VarEntity varEntity = new VarEntity(varDeclNode);
            funcScope.insertVar(varEntity);
            funcParas.add(varEntity);
        }
    }

    public FuncEntity(String ident, Type type)
    {
        super(ident, type);
    }

    public FuncEntity(FuncDeclNode funcDeclNode, Scope father)
    {
        super(funcDeclNode.getIdentName(), new FuncType(funcDeclNode.getIdentName()));
        constructByNode(funcDeclNode, father);
    }

    public FuncEntity(String classIdent, FuncDeclNode funcDeclNode, Scope father)
    {
        super(funcDeclNode.getIdentName(), new FuncType(funcDeclNode.getIdentName()));
        constructByNode(funcDeclNode, father);
        this.classIdent = classIdent;
        this.constructType = funcDeclNode.getConstructType();
        this.inClass = true;
    }

    public Scope getFuncScope()
    {
        return funcScope;
    }

    public Type getRetType()
    {
        return retType;
    }

    public String getClassIdent()
    {
        return classIdent;
    }

    public List<VarEntity> getFuncParas()
    {
        return funcParas;
    }

    public boolean isBuildIn()
    {
        return isBuildIn;
    }

    public boolean isInClass()
    {
        return inClass;
    }

    public boolean isConstruct()
    {
        return this.constructType == ConstructType.CONSTRUCT;
    }

    public void setBuildIn(boolean buildIn)
    {
        isBuildIn = buildIn;
    }

    public void setConstructType(ConstructType constructType)
    {
        this.constructType = constructType;
    }

    public void setRetType(Type retType)
    {
        this.retType = retType;
    }

    public void setInClass(boolean inClass)
    {
        this.inClass = inClass;
    }

    public void setFuncParas(List<VarEntity> funcParas)
    {
        this.funcParas = funcParas;
    }
}
