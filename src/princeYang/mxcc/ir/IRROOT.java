package princeYang.mxcc.ir;

import princeYang.mxcc.ast.ForStateNode;
import princeYang.mxcc.ast.StateNode;
import princeYang.mxcc.backend.NASMRegSet;

import java.util.*;

public class IRROOT
{
    private Map<String, IRFunction> functionMap = new HashMap<String, IRFunction>();
    private Map<String, IRFunction> buildInFuncMap = new HashMap<String, IRFunction>();
    private Map<String, StaticStr> staticStrMap = new HashMap<String, StaticStr>();
    private List<StaticData> staticDataList = new ArrayList<StaticData>();
    private Map<ForStateNode, IRFor> IRForMap = new HashMap<ForStateNode, IRFor>();
    private boolean containShiftDiv = false;
    private PhysicalReg preg0, preg1;

    static public final String buildInPrint = "print";
    static public final String buildInPrintLable = "__print";
    static public final String buildInPrintln = "println";
    static public final String buildInPrintlnLable = "__println";
    static public final String buildInGetString = "getString";
    static public final String buildInGetStringLable = "__getString";
    static public final String buildInGetInt = "getInt";
    static public final String buildInGetIntLable = "__getInt";
    static public final String buildInToString = "toString";
    static public final String buildInToStringLable = "__toString";

    static public final String buildInStringConcat = "string.concat";
    static public final String buildInStringConcatLable = "__string__concat";
    static public final String buildInStringEqual = "string.equal";
    static public final String buildInStringEqualLable = "__string__equal";
    static public final String buildInStringNequal = "string.nequal";
    static public final String buildInStringNequalLable = "__string__nequal";
    static public final String buildInStringLess = "string.less";
    static public final String buildInStringLessLable = "__string__less";
    static public final String buildInStringLessEqual = "string.lessEqual";
    static public final String buildInStringLessEqualLable = "__string__lessEqual";

    static public final String buildInClassStringLength = "__class__string__length";
    static public final String buildInClassStringSubString = "__class__string__substring";
    static public final String buildInClassStringParseInt = "__class__string__parseInt";
    static public final String buildInClassStringOrd = "__class__string__ord";
    static public final String buildInClassArraySize = "__class____array__size";

    // for spot optim
    static public final String buildInPrintForInt = "printForInt";
    static public final String buildInPrintForIntLable = "__printForInt";
    static public final String buildInPrintlnForInt = "printlnForInt";
    static public final String buildInPrintlnForIntLable = "__printlnForInt";

    private List<String> buildInList = new ArrayList<String>();
    private List<String> buildInLableList = new ArrayList<String>();

    public IRROOT()
    {
        buildInList.clear();
        buildInLableList.clear();
        buildInList.add(buildInPrint);
        buildInLableList.add(buildInPrintLable);

        buildInList.add(buildInPrintln);
        buildInLableList.add(buildInPrintlnLable);

        buildInList.add(buildInGetString);
        buildInLableList.add(buildInGetStringLable);

        buildInList.add(buildInGetInt);
        buildInLableList.add(buildInGetIntLable);

        buildInList.add(buildInToString);
        buildInLableList.add(buildInToStringLable);

        buildInList.add(buildInStringConcat);
        buildInLableList.add(buildInStringConcatLable);

        buildInList.add(buildInStringEqual);
        buildInLableList.add(buildInStringEqualLable);

        buildInList.add(buildInStringNequal);
        buildInLableList.add(buildInStringNequalLable);

        buildInList.add(buildInStringLess);
        buildInLableList.add(buildInStringLessLable);

        buildInList.add(buildInStringLessEqual);
        buildInLableList.add(buildInStringLessEqualLable);

        buildInList.add(buildInClassStringLength);
        buildInLableList.add(buildInClassStringLength);

        buildInList.add(buildInClassStringSubString);
        buildInLableList.add(buildInClassStringSubString);

        buildInList.add(buildInClassStringParseInt);
        buildInLableList.add(buildInClassStringParseInt);

        buildInList.add(buildInClassStringOrd);
        buildInLableList.add(buildInClassStringOrd);

        buildInList.add(buildInClassArraySize);
        buildInLableList.add(buildInClassArraySize);

        // for spot optim
        buildInList.add(buildInPrintForInt);
        buildInLableList.add(buildInPrintForIntLable);

        buildInList.add(buildInPrintlnForInt);
        buildInLableList.add(buildInPrintlnForIntLable);

        IRFunction buildInFunc;

        for (int i = 0; i < buildInList.size(); i++)
        {
            buildInFunc = new IRFunction(buildInList.get(i), buildInLableList.get(i));
            buildInFunc.getUsedGeneralPReg().addAll(NASMRegSet.generalRegs);
            processBuildIn(buildInFunc);
        }
    }

    private void processBuildIn(IRFunction buildInFunc)
    {
        this.buildInFuncMap.put(buildInFunc.getFuncName(), buildInFunc);
    }

    public void addFunction(IRFunction function)
    {
        this.functionMap.put(function.getFuncName(), function);
    }

    public Map<String, IRFunction> getFunctionMap()
    {
        return functionMap;
    }

    public Map<String, IRFunction> getBuildInFuncMap()
    {
        return buildInFuncMap;
    }

    public Map<ForStateNode, IRFor> getIRForMap()
    {
        return IRForMap;
    }

    public Map<String, StaticStr> getStaticStrMap()
    {
        return staticStrMap;
    }

    public void addStaticData(StaticData staticData)
    {
        this.staticDataList.add(staticData);
    }

    public boolean isContainShiftDiv()
    {
        return containShiftDiv;
    }

    public void setContainShiftDiv(boolean containShiftDiv)
    {
        this.containShiftDiv = containShiftDiv;
    }

    public List<StaticData> getStaticDataList()
    {
        return staticDataList;
    }

    public void setPreg0(PhysicalReg preg0)
    {
        this.preg0 = preg0;
    }

    public void setPreg1(PhysicalReg preg1)
    {
        this.preg1 = preg1;
    }

    public PhysicalReg getPreg0()
    {
        return preg0;
    }

    public PhysicalReg getPreg1()
    {
        return preg1;
    }
}
