package princeYang.mxcc.ir;

public abstract class StaticData extends IRReg
{
    private int size;
    private String ident;

    public StaticData(String ident, int size)
    {
        this.ident = ident;
        this.size = size;
    }

    @Override
    public abstract void accept(IRVisitor visitor);

    @Override
    public StaticData copy()
    {
        return this;
    }

    public int getSize()
    {
        return size;
    }

    public String getIdent()
    {
        return ident;
    }
}
