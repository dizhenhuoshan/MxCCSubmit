package princeYang.mxcc.backend;

import princeYang.mxcc.ir.PhysicalReg;

public class NASMReg extends PhysicalReg
{

    private String regName;
    private int funcArgIndex;
    private boolean isCallerSave;
    private boolean isCalleeSave;
    private boolean isGeneral;

    public NASMReg(String regName, int funcArgIndex, boolean isCallerSave, boolean isCalleeSave, boolean isGeneral)
    {
        this.regName = regName;
        this.funcArgIndex = funcArgIndex;
        this.isCalleeSave = isCalleeSave;
        this.isCallerSave = isCallerSave;
        this.isGeneral = isGeneral;
    }

    @Override
    public boolean isCallerSave()
    {
        return isCallerSave;
    }

    @Override
    public boolean isCalleeSave()
    {
        return isCalleeSave;
    }

    @Override
    public boolean isGeneral()
    {
        return isGeneral;
    }

    public boolean isArgForced()
    {
        return funcArgIndex != -1;
    }

    public int getFuncArgIndex()
    {
        return funcArgIndex;
    }

    @Override
    public String getName()
    {
        return regName;
    }
}
