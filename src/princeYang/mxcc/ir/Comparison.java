package princeYang.mxcc.ir;

import java.util.Map;

public class Comparison extends IRInstruction
{

    private ComparisonOp comparisonOp;
    private IRReg resReg;
    private IRValue lhs, rhs;

    public Comparison(BasicBlock basicBlock, ComparisonOp comparisonOp, IRReg resReg, IRValue lhs, IRValue rhs)
    {
        super(basicBlock);
        this.comparisonOp = comparisonOp;
        this.resReg = resReg;
        this.lhs = lhs;
        this.rhs = rhs;
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
        if (rhs instanceof IRReg)
            rhs = renameMap.get(rhs);
        reloadUsedRV();
    }

    @Override
    public IRInstruction copyAndRename(Map<Object, Object> renameMap)
    {
        return new Comparison((BasicBlock) renameMap.getOrDefault(getFatherBlock(), getFatherBlock()),
                comparisonOp, (IRReg) renameMap.getOrDefault(resReg, resReg),
                (IRValue) renameMap.getOrDefault(lhs, lhs), (IRValue) renameMap.getOrDefault(rhs, rhs));
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

    public IRReg getResReg()
    {
        return resReg;
    }

    public IRValue getLhs()
    {
        return lhs;
    }

    public IRValue getRhs()
    {
        return rhs;
    }

    public ComparisonOp getComparisonOp()
    {
        return comparisonOp;
    }
}
