package princeYang.mxcc.errors;

import princeYang.mxcc.ast.Location;

public class MxError extends Error
{

    public MxError(Location location, String msg)
    {
        super(String.format("Compiler ERROR at %d:%d, with msg: %s", location.getLine(), location.getColumn(), msg));
    }

    public MxError(String msg)
    {
        super(String.format("Compiler ERROR with msg: %s", msg));
    }
}
