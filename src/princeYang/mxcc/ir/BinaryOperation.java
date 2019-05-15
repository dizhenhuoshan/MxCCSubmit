package princeYang.mxcc.ir;

import java.util.Map;

import static princeYang.mxcc.ir.IRBinaryOp.*;

public class BinaryOperation extends IRInstruction
{

    private IRValue lhs, rhs;
    private IRBinaryOp bop;
    private IRReg resReg;
    private boolean exchangeable;
    private boolean diverUsed;

    public BinaryOperation(BasicBlock basicBlock, IRReg resReg, IRValue lhs, IRBinaryOp bop, IRValue rhs)
    {
        super(basicBlock);
        this.lhs = lhs;
        this.rhs = rhs;
        this.bop = bop;
        this.resReg = resReg;
        this.exchangeable = ((bop == ADD) || (bop == MUL) || (bop == BITWISE_AND) || (bop == BITWISE_OR) || (bop == BITWISE_XOR));
        this.diverUsed = ((bop == DIV) || (bop == MOD));
        reloadUsedRV();
    }

    @Override
    public void accept(IRVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public void reloadUsedRV()
    {
        usedIRReg.clear();
        usedIRValue.clear();
        usedIRValue.add(lhs);
        usedIRValue.add(rhs);
        if (lhs instanceof IRReg)
            usedIRReg.add((IRReg) lhs);
        if (rhs instanceof IRReg)
            usedIRReg.add((IRReg) rhs);
    }

    @Override
    public void setUsedIRReg(Map<IRReg, IRReg> renameMap)
    {
        if (lhs instanceof IRReg)
            lhs = renameMap.get(lhs);
        if (rhs instanceof  IRReg)
            rhs = renameMap.get(rhs);
        reloadUsedRV();
    }

    @Override
    public IRInstruction copyAndRename(Map<Object, Object> renameMap)
    {
        return new BinaryOperation((BasicBlock) renameMap.getOrDefault(getFatherBlock(), getFatherBlock()), (IRReg) renameMap.getOrDefault(resReg, resReg), (IRValue) renameMap.getOrDefault(lhs, lhs), bop, (IRValue) renameMap.getOrDefault(rhs, rhs));
    }

    @Override
    public IRReg getDefinedReg()
    {
        return resReg;
    }

    @Override
    public void setDefinedReg(IRReg vIRReg)
    {
        this.resReg = vIRReg;
    }

    public IRValue getLhs()
    {
        return lhs;
    }

    public IRBinaryOp getBop()
    {
        return bop;
    }

    public IRValue getRhs()
    {
        return rhs;
    }

    public IRReg getResReg()
    {
        return resReg;
    }

    public void setLhs(IRValue lhs)
    {
        this.lhs = lhs;
        reloadUsedRV();
    }

    public void setRhs(IRValue rhs)
    {
        this.rhs = rhs;
        reloadUsedRV();
    }

    public boolean isExchangeable()
    {
        return exchangeable;
    }

    public boolean isDiverUsed()
    {
        return diverUsed;
    }
}
