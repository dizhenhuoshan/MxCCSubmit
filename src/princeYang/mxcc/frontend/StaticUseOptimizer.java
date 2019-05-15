package princeYang.mxcc.frontend;

import princeYang.mxcc.ast.*;
import princeYang.mxcc.scope.ClassEntity;
import princeYang.mxcc.scope.Scope;
import princeYang.mxcc.scope.VarEntity;

import java.util.HashSet;
import java.util.Set;

public class StaticUseOptimizer extends ScopeScanner
{

    private Set<VarEntity> usedVarSet = new HashSet<VarEntity>();
    private Set<VarEntity> unUsedVarSet = new HashSet<VarEntity>();
    private Scope globalScope;
    private Scope currentScope;
    private boolean inDefine = false;

    public StaticUseOptimizer(Scope globalScope)
    {
        this.globalScope = globalScope;
    }

    @Override
    public void visit(MxProgNode node)
    {
        currentScope = globalScope;
        for (DeclNode declNode : node.getDeclNodesList())
        {
            if (declNode instanceof FuncDeclNode)
                declNode.accept(this);
            else if (declNode instanceof ClassDeclNode)
            {
                ClassEntity classEntity = currentScope.getClass(declNode.getIdentName());
                currentScope = classEntity.getClassScope();
                for (FuncDeclNode memFunc : ((ClassDeclNode) declNode).getFuncDeclList())
                    memFunc.accept(this);
                currentScope = currentScope.getFather();
            }
            else if (declNode instanceof VarDeclNode)
                declNode.accept(this);
        }
        for (VarEntity varEntity : unUsedVarSet)
        {
            if (!usedVarSet.contains(varEntity))
            {
                varEntity.setUnUsed(true);
            }
        }
    }

    @Override
    public void visit(DeclarationsNode node)
    {
        super.visit(node);
    }

    @Override
    public void visit(VarDeclNode node)
    {
        if (node.getInitValue() != null)
        {
            VarEntity varEntity = currentScope.getVar(node.getIdentName());
            if (varEntity.getType() instanceof ArrayType || varEntity.isInGlobal())
                unUsedVarSet.add(varEntity);
            node.getInitValue().accept(this);
        }
    }

    @Override
    public void visit(FuncDeclNode node)
    {
        node.getFuncBlock().accept(this);
    }

    @Override
    public void visit(ClassDeclNode node)
    {
        super.visit(node);
    }

    @Override
    public void visit(ExprStateNode node)
    {
        node.getExprState().accept(this);
    }

    @Override
    public void visit(ForStateNode node)
    {
        if (node.getStartExpr() != null)
            node.getStartExpr().accept(this);
        if (node.getStopExpr() != null)
            node.getStopExpr().accept(this);
        if (node.getStepExpr() != null)
            node.getStepExpr().accept(this);
        if (node.getLoopState() != null)
            node.getLoopState().accept(this);
    }

    @Override
    public void visit(WhileStateNode node)
    {
        node.getConditionExpr().accept(this);
        if (node.getLoopState() != null)
            node.getLoopState().accept(this);
    }

    @Override
    public void visit(ReturnStateNode node)
    {
        if (node.getRetExpr() != null)
            node.getRetExpr().accept(this);
    }

    @Override
    public void visit(IfStateNode node)
    {
        node.getConditionExpr().accept(this);
        if (node.getThenState() != null)
            node.getThenState().accept(this);
        if (node.getElseState() != null)
            node.getElseState().accept(this);
    }

    @Override
    public void visit(FuncBlockNode node)
    {
        currentScope = node.getScope();
        for (Node stateNode : node.getStateList())
            stateNode.accept(this);
        currentScope = currentScope.getFather();
    }

    @Override
    public void visit(MemoryAccessExprNode node)
    {
        node.getHostExpr().accept(this);
    }

    @Override
    public void visit(FunctionCallExprNode node)
    {
        node.getFuncExpr().accept(this);
        for (ExprNode para : node.getParaList())
            para.accept(this);
    }

    @Override
    public void visit(ArrayAccessExprNode node)
    {
        if (inDefine)
        {
            node.getArrExpr().accept(this);
            inDefine = false;
            node.getSubExpr().accept(this);
            inDefine = true;
        }
        else
        {
            node.getArrExpr().accept(this);
            node.getSubExpr().accept(this);
        }
    }

    @Override
    public void visit(PostFixExprNode node)
    {
        node.getPreExpr().accept(this);
    }

    @Override
    public void visit(PreFixExprNode node)
    {
        node.getPostExpr().accept(this);
    }

    @Override
    public void visit(NewExprNode node)
    {
        if (node.getKnownDims() != null)
        {
            for (ExprNode dim : node.getKnownDims())
                dim.accept(this);
        }
    }

    @Override
    public void visit(BinaryExprNode node)
    {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
    }

    @Override
    public void visit(AssignExprNode node)
    {
        if (node.getRhs().getType() instanceof ArrayType && !(node.getRhs() instanceof NewExprNode))
        {
            node.getLhs().accept(this);
            node.getRhs().accept(this);
        }
        inDefine = true;
        node.getLhs().accept(this);
        inDefine = false;
        node.getRhs().accept(this);
    }

    @Override
    public void visit(IdentExprNode node)
    {
        VarEntity varEntity = currentScope.getVar(node.getIdentName());
        if (varEntity != null)
        {
            if (varEntity.getType() instanceof ArrayType || varEntity.isInGlobal())
            {
                if (inDefine)
                    unUsedVarSet.add(varEntity);
                else
                    usedVarSet.add(varEntity);
            }
        }
    }
}
