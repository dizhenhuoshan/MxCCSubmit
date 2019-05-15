package princeYang.mxcc.ir;

import java.util.Map;

public class HeapAllocate extends IRInstruction
{
    private IRReg destReg;
    private IRValue allocateSize;

    public HeapAllocate(BasicBlock basicBlock, IRReg destReg, IRValue allocateSize)
    {
        super(basicBlock);
        this.destReg = destReg;
        this.allocateSize = allocateSize;
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
        usedIRValue.add(allocateSize);
        if (allocateSize instanceof IRReg)
            usedIRReg.add((IRReg) allocateSize);
    }

    @Override
    public void setUsedIRReg(Map<IRReg, IRReg> renameMap)
    {
        if (allocateSize instanceof IRReg)
            allocateSize = renameMap.get(allocateSize);
        reloadUsedRV();
    }

    @Override
    public IRInstruction copyAndRename(Map<Object, Object> renameMap)
    {
        return new HeapAllocate((BasicBlock) renameMap.getOrDefault(getFatherBlock(), getDefinedReg()),
                (IRReg) renameMap.getOrDefault(destReg, destReg),
                (IRValue) renameMap.getOrDefault(allocateSize, allocateSize));
    }

    @Override
    public IRReg getDefinedReg()
    {
        return destReg;
    }

    @Override
    public void setDefinedReg(IRReg vIRReg)
    {
        this.destReg = vIRReg;
    }

    public IRReg getDestReg()
    {
        return destReg;
    }

    public IRValue getAllocateSize()
    {
        return allocateSize;
    }
}
