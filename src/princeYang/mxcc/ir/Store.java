package princeYang.mxcc.ir;

import java.util.Map;

public class Store extends IRInstruction
{

    private IRValue src;
    private IRValue addr;
    private int size, offset;
    private boolean isStaticData = false;

    public Store(BasicBlock basicBlock, IRValue src, IRValue addr, int size, int offset)
    {
        super(basicBlock);
        this.src = src;
        this.addr = addr;
        this.size = size;
        this.offset = offset;
        reloadUsedRV();
    }

    public Store(BasicBlock basicBlock, IRValue src, StaticData addr, int size)
    {
        super(basicBlock);
        this.src = src;
        this.addr = addr;
        this.size = size;
        this.offset = 0;
        this.isStaticData = true;
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
        usedIRValue.add(src);
        usedIRValue.add(addr);
        if (!(src instanceof StackSlot) && (src instanceof  IRReg))
            usedIRReg.add((IRReg) src);
        if (!(addr instanceof StackSlot) && (addr instanceof IRReg))
            usedIRReg.add((IRReg) addr);
    }

    @Override
    public void setUsedIRReg(Map<IRReg, IRReg> renameMap)
    {
        if (!(src instanceof StackSlot) && (src instanceof  IRReg))
            src = renameMap.get(src);
        if (!(addr instanceof StackSlot) && (addr instanceof IRReg))
            addr = renameMap.get(addr);
        reloadUsedRV();
    }

    @Override
    public IRInstruction copyAndRename(Map<Object, Object> renameMap)
    {
        IRValue renamedAddr;
        if (isStaticData)
            renamedAddr = (StaticData) renameMap.getOrDefault(addr, addr);
        else
            renamedAddr = (IRValue) renameMap.getOrDefault(addr, addr);
        return new Store((BasicBlock) renameMap.getOrDefault(getFatherBlock(), getFatherBlock()),
                (IRValue) renameMap.getOrDefault(src, src), renamedAddr, size, offset);
    }

    @Override
    public IRReg getDefinedReg()
    {
        return null;
    }

    @Override
    public void setDefinedReg(IRReg vIRReg)
    {

    }

    public int getSize()
    {
        return size;
    }

    public int getOffset()
    {
        return offset;
    }

    public IRValue getAddr()
    {
        return addr;
    }

    public IRValue getSrc()
    {
        return src;
    }

    public boolean isStaticData()
    {
        return isStaticData;
    }

    public void setAddr(IRValue addr)
    {
        this.addr = addr;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }
}
