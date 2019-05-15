package princeYang.mxcc.ir;

import princeYang.mxcc.ast.StateNode;
import princeYang.mxcc.errors.MxError;

import java.util.Map;

public class Load extends IRInstruction
{
    private IRReg destReg;
    private IRValue addr;
    private int size, offset;
    private boolean isLoadAddr = false, isStaticData = false;

    public Load(BasicBlock basicBlock, IRReg destReg, IRValue addr, int size, int offset)
    {
        super(basicBlock);
        this.destReg = destReg;
        this.addr = addr;
        if (size == 0)
            throw new MxError("IR Load constructor get a bad size 0 \n");
        this.size = size;
        this.offset = offset;
        reloadUsedRV();
    }

    public Load(BasicBlock basicBlock, IRReg destReg, StaticData addr, int size, boolean isLoadAddr)
    {
        super(basicBlock);
        this.destReg = destReg;
        this.addr = addr;
        if (size == 0)
            throw new MxError("IR Load constructor get a bad size 0 \n");
        this.size = size;
        this.offset = 0;
        this.isStaticData = true;
        this.isLoadAddr = isLoadAddr;
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
        usedIRValue.add(addr);
        if (!(addr instanceof StackSlot) && (addr instanceof IRReg))
            usedIRReg.add((IRReg) addr);
    }

    @Override
    public void setUsedIRReg(Map<IRReg, IRReg> renameMap)
    {
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
            renamedAddr = (IRReg) renameMap.getOrDefault(addr, addr);
        return new Load((BasicBlock) renameMap.getOrDefault(getFatherBlock(), getFatherBlock()),
                (IRReg) renameMap.getOrDefault(destReg, destReg), renamedAddr, size, offset);

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

    public IRValue getAddr()
    {
        return addr;
    }

    public int getSize()
    {
        return size;
    }

    public int getOffset()
    {
        return offset;
    }

    public boolean isLoadAddr()
    {
        return isLoadAddr;
    }

    public boolean isStaticData()
    {
        return isStaticData;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public void setAddr(IRValue addr)
    {
        this.addr = addr;
    }
}
