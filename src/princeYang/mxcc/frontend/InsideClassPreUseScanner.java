package princeYang.mxcc.frontend;

import princeYang.mxcc.ast.*;
import princeYang.mxcc.errors.MxError;
import princeYang.mxcc.scope.*;

public class InsideClassPreUseScanner extends ScopeScanner
{
    private Scope globalScope, currentScope;
    private ClassType classType;
    private int currentMemOffset = 0;

    public InsideClassPreUseScanner(Scope globalScope)
    {
        this.globalScope = globalScope;
        this.currentScope = globalScope;
    }

    @Override
    public void visit(MxProgNode mxProgNode)
    {
        for (DeclNode declNode : mxProgNode.getDeclNodesList())
        {
            if (declNode instanceof ClassDeclNode)
                declNode.accept(this);
        }
    }

    @Override
    public void visit(ClassDeclNode classDeclNode)
    {
        ClassEntity classEntity = (ClassEntity) globalScope.getClass(classDeclNode.getIdentName());
        currentScope = classEntity.getClassScope();
        classType = (ClassType) classEntity.getType();
        for (FuncDeclNode funcDeclNode : classDeclNode.getFuncDeclList())
        {
            funcDeclNode.accept(this);
        }
        currentMemOffset = 0;
        for(VarDeclNode varDeclNode : classDeclNode.getVarDeclList())
        {
            varDeclNode.accept(this);
        }
        classEntity.setMemSize(currentMemOffset);
        currentScope = currentScope.getFather();
        classType = null;
    }

    @Override
    public void visit(FuncDeclNode funcDeclNode)
    {
        FuncEntity funcEntity = currentScope.getFunc(funcDeclNode.getIdentName());
        currentScope = funcEntity.getFuncScope();
        VarEntity entity = new VarEntity("this", classType);
        currentScope.insertVar(entity);
        funcEntity.getFuncParas().add(entity);
        if (funcDeclNode.isConstruct() && !(funcDeclNode.getIdentName().equals(classType.getClassIdent())))
            throw new MxError(funcDeclNode.getLocation(), "Only construct function no return! \n");
        currentScope = currentScope.getFather();
    }

    @Override
    public void visit(VarDeclNode varDeclNode)
    {
        if (varDeclNode.getVarType().getType() instanceof ClassType)
        {
            if (currentScope.getClass(((ClassType)(varDeclNode.getVarType().getType())).getClassIdent()) == null)
                throw new MxError(varDeclNode.getLocation(), String.format("Scope: class %s is not defined\n",
                        ((ClassType)varDeclNode.getVarType().getType()).getClassIdent()));
        }
        VarEntity entity = new VarEntity(classType.getClassIdent() ,varDeclNode);
        entity.setMemOffset(currentMemOffset);
        currentMemOffset += varDeclNode.getVarType().getType().getSize();
        currentScope.insertVar(entity);
    }
}
