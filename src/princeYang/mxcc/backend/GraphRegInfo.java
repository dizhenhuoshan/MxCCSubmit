package princeYang.mxcc.backend;

import princeYang.mxcc.ir.IRReg;
import princeYang.mxcc.ir.VirtualReg;

import java.util.HashSet;
import java.util.Set;

public class GraphRegInfo
{
    public int degree = 0;
    public IRReg colorReg;
    boolean removed = false;
    public Set<VirtualReg> neighbors = new HashSet<VirtualReg>();
    public Set<VirtualReg> suggestSame = new HashSet<VirtualReg>();
}
