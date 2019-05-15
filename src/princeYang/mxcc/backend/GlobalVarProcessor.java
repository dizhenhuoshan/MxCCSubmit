package princeYang.mxcc.backend;

import princeYang.mxcc.Config;
import princeYang.mxcc.ir.*;

import java.util.*;

public class GlobalVarProcessor
{
    IRROOT irRoot;
    private Map<IRFunction, GlobalVarFuncInfo> globalVarFuncInfoMap = new HashMap<IRFunction, GlobalVarFuncInfo>();

    private VirtualReg genVreg(GlobalVarFuncInfo funcInfo, StaticData staticData)
    {
        VirtualReg vReg = funcInfo.globalVarRegMap.get(staticData);
        if (vReg == null)
        {
            vReg = new VirtualReg(staticData.getIdent());
            funcInfo.globalVarRegMap.put(staticData, vReg);
        }
        return vReg;
    }

    private boolean isStaticMemAccess(IRInstruction instruction)
    {
        if (instruction instanceof Load)
            return ((Load) instruction).isStaticData();
        if (instruction instanceof Store)
            return ((Store) instruction).isStaticData();
        return false;
    }

    public GlobalVarProcessor(IRROOT irRoot)
    {
        this.irRoot = irRoot;
    }

    public void process()
    {
        // generate vreg for static data
        for (IRFunction function : irRoot.getFunctionMap().values())
        {
            GlobalVarFuncInfo globalVarFuncInfo = new GlobalVarFuncInfo();
            Map<IRReg, IRReg> renameMap = new HashMap<IRReg, IRReg>();
            globalVarFuncInfoMap.put(function, globalVarFuncInfo);
            for (BasicBlock basicBlock : function.getReversePostOrder())
            {
                for (IRInstruction instruction = basicBlock.getHeadInst(); instruction != null; instruction = instruction.getNext())
                {
                    if (!isStaticMemAccess(instruction))
                    {
                        List<IRReg> usedRegList = instruction.getUsedIRReg();
                        if (!usedRegList.isEmpty())
                        {
                            renameMap.clear();
                            for (IRReg reg : usedRegList)
                            {
                                if (reg instanceof StaticData && !(reg instanceof StaticStr))
                                    renameMap.put(reg, genVreg(globalVarFuncInfo, (StaticData) reg));
                                else
                                    renameMap.put(reg, reg);
                            }
                            instruction.setUsedIRReg(renameMap);
                        }
                        IRReg definedReg = instruction.getDefinedReg();
                        if (definedReg instanceof StaticData)
                        {
                            VirtualReg vReg = genVreg(globalVarFuncInfo, (StaticData) definedReg);
                            instruction.setDefinedReg(vReg);
                            globalVarFuncInfo.definedGlobalVar.add((StaticData) definedReg);
                        }
                    }
                }
            }

            // load static data when function start
            BasicBlock blockEnter = function.getBlockEnter();
            IRInstruction headInst = blockEnter.getHeadInst();
            for(Map.Entry<StaticData, VirtualReg> mapEntry : globalVarFuncInfo.globalVarRegMap.entrySet())
            {
                StaticData globalVar = mapEntry.getKey();
                VirtualReg vReg = mapEntry.getValue();
                headInst.prepend(new Load(blockEnter, vReg, globalVar, Config.regSize, globalVar instanceof StaticStr));
            }
        }

        for (IRFunction buildInFunc : irRoot.getBuildInFuncMap().values())
            globalVarFuncInfoMap.put(buildInFunc, new GlobalVarFuncInfo());

        // calculate recur used static data
        for (IRFunction function : irRoot.getFunctionMap().values())
        {
            GlobalVarFuncInfo funcInfo = globalVarFuncInfoMap.get(function);
            funcInfo.recurDefinedGlobalVar.addAll(funcInfo.definedGlobalVar);
            funcInfo.recurUsedGlobalVar.addAll(funcInfo.globalVarRegMap.keySet());
            for (IRFunction calleFunc : function.recurCalleeSet)
            {
                GlobalVarFuncInfo calleeFuncInfo = globalVarFuncInfoMap.get(calleFunc);
                funcInfo.recurUsedGlobalVar.addAll(calleeFuncInfo.globalVarRegMap.keySet());
                funcInfo.recurDefinedGlobalVar.addAll(calleeFuncInfo.recurDefinedGlobalVar);
            }
        }


        for (IRFunction function : irRoot.getFunctionMap().values())
        {
            GlobalVarFuncInfo funcInfo = globalVarFuncInfoMap.get(function);
            Set<StaticData> usedGlobalVar = funcInfo.globalVarRegMap.keySet();
            if (!usedGlobalVar.isEmpty())
            {
                for (BasicBlock basicBlock : function.getReversePostOrder())
                {
                    for (IRInstruction instruction = basicBlock.getHeadInst(); instruction != null; instruction = instruction.getNext())
                    {
                        if (instruction instanceof FuncCall)
                        {
                            IRFunction calleeFunc = ((FuncCall) instruction).getFunction();
                            GlobalVarFuncInfo  calleeFuncInfo = globalVarFuncInfoMap.get(calleeFunc);

                            // save defined globalVar before function call
                            for (StaticData globalDefinedVar : funcInfo.definedGlobalVar)
                            {
                                if (!(globalDefinedVar instanceof StaticStr))
                                    instruction.prepend(new Store(basicBlock, funcInfo.globalVarRegMap.get(globalDefinedVar), globalDefinedVar, Config.regSize));
                            }

                            // load recurDefined globalVar after function call
                            if (!calleeFuncInfo.recurDefinedGlobalVar.isEmpty())
                            {
                                Set<StaticData> changedVarSet =
                                        new HashSet<StaticData>(calleeFuncInfo.recurDefinedGlobalVar);
                                changedVarSet.retainAll(usedGlobalVar);
                                for (StaticData globalVar : changedVarSet)
                                {
                                    if (!(globalVar instanceof StaticStr))
                                        instruction.append(new Load(basicBlock, funcInfo.globalVarRegMap.get(globalVar), globalVar, Config.regSize, false));
                                }
                            }
                        }
                    }
                }
            }
        }

        // store Global Var before function end
        for (IRFunction function : irRoot.getFunctionMap().values())
        {
            GlobalVarFuncInfo funcInfo = globalVarFuncInfoMap.get(function);
            Return returnInst = function.getReturnInstList().get(0);
            for (StaticData globalVar : funcInfo.definedGlobalVar)
                returnInst.prepend(new Store(returnInst.getFatherBlock(), funcInfo.globalVarRegMap.get(globalVar), globalVar, Config.regSize));
        }

    }

}
