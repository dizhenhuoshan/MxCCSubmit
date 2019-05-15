package princeYang.mxcc.scope;

import princeYang.mxcc.ast.*;
import princeYang.mxcc.ir.IRReg;

public class VarEntity extends Entity
{
    private Location location;
    private String classIdent = null;
    private boolean inClass = false;
    private boolean inGlobal = false;
    private boolean unUsed = false;
    private IRReg irReg = null;
    private int memOffset = 0;

    public VarEntity(String ident, Type type)
    {
        super(ident, type);
    }

    public VarEntity(VarDeclNode varDeclNode)
    {
        super(varDeclNode.getIdentName(), varDeclNode.getVarType().getType());
        this.location = varDeclNode.getLocation();
    }

    public VarEntity(String classIdent, String ident, Type type)
    {
        super(ident, type);
        this.classIdent = classIdent;
        this.inClass = true;
    }

    public VarEntity(String classIdent, VarDeclNode varDeclNode)
    {
        super(varDeclNode.getIdentName(), varDeclNode.getVarType().getType());
        this.classIdent = classIdent;
        this.inClass = true;
    }

    public Location getLocation()
    {
        return location;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }

    public boolean isInGlobal()
    {
        return inGlobal;
    }

    public void setInGlobal(boolean inGlobal)
    {
        this.inGlobal = inGlobal;
    }

    public boolean isUnUsed()
    {
        return unUsed;
    }

    public void setUnUsed(boolean unUsed)
    {
        this.unUsed = unUsed;
    }

    public IRReg getIrReg()
    {
        return irReg;
    }

    public void setIrReg(IRReg irReg)
    {
        this.irReg = irReg;
    }

    public int getMemOffset()
    {
        return memOffset;
    }

    public void setMemOffset(int memOffset)
    {
        this.memOffset = memOffset;
    }
}
