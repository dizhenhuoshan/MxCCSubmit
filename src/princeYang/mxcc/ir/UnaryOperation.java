package princeYang.mxcc.ir;

import java.util.Map;

public class UnaryOperation extends IRInstruction
{
    private IRUnaryOp uop;
    private IRValue srcValue;
    private IRReg resReg;

    public UnaryOperation(BasicBlock basicBlock, IRUnaryOp uop, IRValue srcValue, IRReg resReg)
    {
        super(basicBlock);
        this.uop = uop;
        this.srcValue = srcValue;
        this.resReg = resReg;
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
        usedIRValue.add(srcValue);
        if (srcValue instanceof IRReg)
            usedIRReg.add((IRReg) srcValue);
    }

    @Override
    public void setUsedIRReg(Map<IRReg, IRReg> renameMap)
    {
        if (srcValue instanceof IRReg)
            this.srcValue = renameMap.get(srcValue);
        reloadUsedRV();
    }

    @Override
    public IRInstruction copyAndRename(Map<Object, Object> renameMap)
    {
        return new UnaryOperation((BasicBlock) renameMap.getOrDefault(getFatherBlock(), getFatherBlock()), uop,
                 (IRValue) renameMap.getOrDefault(srcValue, srcValue), (IRReg) renameMap.getOrDefault(resReg, resReg));
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

    public IRUnaryOp getUop()
    {
        return uop;
    }

    public IRReg getResReg()
    {
        return resReg;
    }

    public IRValue getSrcValue()
    {
        return srcValue;
    }
}
