package princeYang.mxcc.ir;

public class VirtualReg extends IRReg
{
    private String vRegName;
    private PhysicalReg enforcedReg = null;

    public VirtualReg(String vRegName)
    {
        this.vRegName = vRegName;
    }

    @Override
    public void accept(IRVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public VirtualReg copy()
    {
        return new VirtualReg(vRegName);
    }

    public String getvRegName()
    {
        return vRegName;
    }

    public PhysicalReg getEnforcedReg()
    {
        return enforcedReg;
    }

    public void setEnforcedReg(PhysicalReg enforcedReg)
    {
        this.enforcedReg = enforcedReg;
    }
}
