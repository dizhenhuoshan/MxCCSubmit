package princeYang.mxcc.ir;

public interface IRVisitor
{
    void visit(IRROOT IRNode);
    void visit(IRFunction IRNode);
    void visit(BasicBlock IRNode);
    void visit(VirtualReg IRNode);
    void visit(Immediate IRNode);
    void visit(Pop IRNode);
    void visit(Push IRNode);
    void visit(Return IRNode);
    void visit(BinaryOperation IRNode);
    void visit(UnaryOperation IRNode);
    void visit(HeapAllocate IRNode);
    void visit(Jump IRNode);
    void visit(Load IRNode);
    void visit(Store IRNode);
    void visit(Move IRNode);
    void visit(PhysicalReg IRNode);
    void visit(Branch IRNode);
    void visit(Comparison IRNode);
    void visit(FuncCall IRNode);
    void visit(StaticStr IRNode);
    void visit(StaticVar IRNode);
}
