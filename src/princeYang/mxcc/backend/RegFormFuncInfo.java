package princeYang.mxcc.backend;

import princeYang.mxcc.ir.IRValue;
import princeYang.mxcc.ir.PhysicalReg;
import princeYang.mxcc.ir.StackSlot;

import java.util.*;

public class RegFormFuncInfo
{
    public int stackSlotNum = 0, extraParasNum = 0;
    public List<PhysicalReg> usedCalleeSaveRegs;
    public List<PhysicalReg> usedCallerSaveRegs;
    public Map<StackSlot, Integer> stackSlotOffsetMap;
    public Set<PhysicalReg> recursiveUsedRegSet;

    public RegFormFuncInfo()
    {
        usedCalleeSaveRegs = new ArrayList<PhysicalReg>();
        usedCallerSaveRegs = new ArrayList<PhysicalReg>();
        stackSlotOffsetMap = new HashMap<StackSlot, Integer>();
        recursiveUsedRegSet = new HashSet<PhysicalReg>();
    }
}
