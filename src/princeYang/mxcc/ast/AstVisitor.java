package princeYang.mxcc.ast;

public interface AstVisitor
{
    public void visit(MxProgNode node);
    public void visit(DeclarationsNode node);
    public void visit(VarDeclNode node);
    public void visit(FuncDeclNode node);
    public void visit(ClassDeclNode node);
    public void visit(ExprStateNode node);
    public void visit(ForStateNode node);
    public void visit(WhileStateNode node);
    public void visit(ContinueStateNode node);
    public void visit(BreakStateNode node);
    public void visit(ReturnStateNode node);
    public void visit(IfStateNode node);
    public void visit(FuncBlockNode node);
    public void visit(MemoryAccessExprNode node);
    public void visit(FunctionCallExprNode node);
    public void visit(ArrayAccessExprNode node);
    public void visit(PostFixExprNode node);
    public void visit(PreFixExprNode node);
    public void visit(NewExprNode node);
    public void visit(BinaryExprNode node);
    public void visit(AssignExprNode node);
    public void visit(ThisExprNode node);
    public void visit(IdentExprNode node);
    public void visit(ConstIntNode node);
    public void visit(ConstStringNode node);
    public void visit(ConstBoolNode node);
    public void visit(ConstNullNode node);
    public void visit(TypeNode node);
}
