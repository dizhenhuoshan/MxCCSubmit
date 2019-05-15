package princeYang.mxcc.backend;

import princeYang.mxcc.ir.StaticData;
import princeYang.mxcc.ir.VirtualReg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GlobalVarFuncInfo
{
    public Map<StaticData, VirtualReg> globalVarRegMap = new HashMap<StaticData, VirtualReg>();
    public Set<StaticData> definedGlobalVar = new HashSet<StaticData>();
    public Set<StaticData> recurDefinedGlobalVar = new HashSet<StaticData>();
    public Set<StaticData> recurUsedGlobalVar = new HashSet<StaticData>();
}
