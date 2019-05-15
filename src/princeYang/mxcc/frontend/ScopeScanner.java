package princeYang.mxcc.frontend;

import princeYang.mxcc.ast.*;
import princeYang.mxcc.scope.Scope;

abstract public class ScopeScanner implements AstVisitor
{

    Scope globalScope;
    public Type voidType = new VoidType();
    public Type intType = new IntType();
    public Type stringType = new StringType();
    public Type boolType = new BoolType();
    public Type nullType = new NullType();

    @Override
    public void visit(MxProgNode node)
    {

    }

    @Override
    public void visit(DeclarationsNode node)
    {

    }

    @Override
    public void visit(VarDeclNode node)
    {

    }

    @Override
    public void visit(FuncDeclNode node)
    {

    }

    @Override
    public void visit(ClassDeclNode node)
    {

    }

    @Override
    public void visit(ExprStateNode node)
    {

    }

    @Override
    public void visit(ForStateNode node)
    {

    }

    @Override
    public void visit(WhileStateNode node)
    {

    }

    @Override
    public void visit(ContinueStateNode node)
    {

    }

    @Override
    public void visit(BreakStateNode node)
    {

    }

    @Override
    public void visit(ReturnStateNode node)
    {

    }

    @Override
    public void visit(IfStateNode node)
    {

    }

    @Override
    public void visit(FuncBlockNode node)
    {

    }

    @Override
    public void visit(MemoryAccessExprNode node)
    {

    }

    @Override
    public void visit(FunctionCallExprNode node)
    {

    }

    @Override
    public void visit(ArrayAccessExprNode node)
    {

    }

    @Override
    public void visit(PostFixExprNode node)
    {

    }

    @Override
    public void visit(PreFixExprNode node)
    {

    }

    @Override
    public void visit(NewExprNode node)
    {

    }

    @Override
    public void visit(BinaryExprNode node)
    {

    }

    @Override
    public void visit(AssignExprNode node)
    {

    }

    @Override
    public void visit(ThisExprNode node)
    {

    }

    @Override
    public void visit(IdentExprNode node)
    {

    }

    @Override
    public void visit(ConstIntNode node)
    {

    }

    @Override
    public void visit(ConstStringNode node)
    {

    }

    @Override
    public void visit(ConstBoolNode node)
    {

    }

    @Override
    public void visit(ConstNullNode node)
    {

    }

    @Override
    public void visit(TypeNode node)
    {

    }
}
