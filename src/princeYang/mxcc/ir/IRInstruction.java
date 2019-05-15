package princeYang.mxcc.ir;

import princeYang.mxcc.errors.MxError;

import java.util.*;

public abstract class IRInstruction
{
    private BasicBlock fatherBlock = null;
    private IRInstruction prev = null, next = null;
    boolean hasRemoved = false;
    public List<IRValue> usedIRValue = new ArrayList<IRValue>();
    public List<IRReg> usedIRReg = new ArrayList<IRReg>();
    public Set<VirtualReg> liveIn = null;
    public Set<VirtualReg> liveOut = null;

    public IRInstruction() {}

    public IRInstruction(BasicBlock fatherBlock)
    {
        this.fatherBlock = fatherBlock;
    }

    public IRInstruction(BasicBlock fatherBlock, IRInstruction prev, IRInstruction next)
    {
        this.fatherBlock = fatherBlock;
        this.prev = prev;
        this.next = next;
    }

    public void append(IRInstruction nextInst)
    {
        if (next == null)
        {
            this.next = nextInst;
            nextInst.prev = this;
            this.fatherBlock.setTailInst(nextInst);
        }
        else
        {
            nextInst.next = this.next;
            this.next.prev = nextInst;
            this.next = nextInst;
            nextInst.prev = this;
        }
    }

    public void prepend(IRInstruction prevInst)
    {
        if (prev == null)
        {
            this.prev = prevInst;
            prevInst.next = this;
            this.fatherBlock.setHeadInst(prevInst);
        }
        else
        {
            this.prev.next = prevInst;
            prevInst.prev = this.prev;
            prevInst.next = this;
            this.prev = prevInst;
        }
    }

    public void replace(IRInstruction newInst)
    {
        if (hasRemoved)
            throw new MxError("Trying to replace a removed inst!\n");
        hasRemoved = true;
        if (prev == null && next ==  null)
        {
            this.fatherBlock.setHeadInst(newInst);
            this.fatherBlock.setTailInst(newInst);
            newInst.prev = newInst.next = null;
        }
        else if (prev == null)
        {
            this.fatherBlock.setHeadInst(newInst);
            this.next.prev = newInst;
            newInst.prev = null;
            newInst.next = this.next;
        }
        else if (next == null)
        {
            this.fatherBlock.setTailInst(newInst);
            this.prev.next = newInst;
            newInst.prev = this;
            newInst.next = null;
        }
        else
        {
            newInst.prev = this.prev;
            newInst.next = this.next;
            this.prev.next = newInst;
            this.next.prev = newInst;
        }
    }

    public void remove()
    {
        if (hasRemoved)
            throw new MxError("Trying to remove a removed inst!\n");
        hasRemoved = true;
        if (this instanceof BranchBaseInst)
            this.fatherBlock.deleteJumpInst();
        if (prev == null && next == null)
        {
            this.fatherBlock.setHeadInst(null);
            this.fatherBlock.setTailInst(null);
        }
        else if (prev == null)
        {
            this.fatherBlock.setHeadInst(next);
            this.next.prev = null;
        }
        else if (next == null)
        {
            this.fatherBlock.setTailInst(prev);
            this.prev.next = null;
        }
        else
        {
            this.prev.next = next;
            this.next.prev = prev;
        }
    }

    public IRInstruction getPrev()
    {
        return prev;
    }

    public IRInstruction getNext()
    {
        return next;
    }

    public List<IRValue> getUsedIRValue()
    {
        return usedIRValue;
    }

    public List<IRReg> getUsedIRReg()
    {
        return usedIRReg;
    }

    public BasicBlock getFatherBlock()
    {
        return fatherBlock;
    }

    public boolean isRemoved()
    {
        return hasRemoved;
    }

    public abstract void accept(IRVisitor visitor);

    public abstract void reloadUsedRV();

    public abstract void setUsedIRReg(Map<IRReg, IRReg> renameMap);

    public abstract IRInstruction copyAndRename(Map<Object, Object> renameMap);

    public abstract IRReg getDefinedReg();

    public abstract void setDefinedReg(IRReg vIRReg);
}
