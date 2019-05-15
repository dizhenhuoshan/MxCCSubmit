package princeYang.mxcc.backend;

import princeYang.mxcc.Config;
import princeYang.mxcc.ir.*;

public class FuncParaForcer
{
    IRROOT irRoot;
    public FuncParaForcer(IRROOT irRoot)
    {
        this.irRoot = irRoot;
    }

    private void setFuncArgReg(IRFunction currentFunc)
    {
        IRInstruction headInst = currentFunc.getBlockEnter().getHeadInst();
//             if args > 6, get them from stack
        for (int i = 6; i < currentFunc.getParavRegList().size(); i++)
        {
            VirtualReg virtualArgReg = currentFunc.getParavRegList().get(i);
            StackSlot paraSlot = new StackSlot(String.format("arg%d", i), currentFunc, true);
            currentFunc.getParaSlotMap().put(virtualArgReg, paraSlot);
            headInst.prepend(new Load(currentFunc.getBlockEnter(), virtualArgReg, paraSlot, Config.regSize, 0));
        }
        // for args less than 6, get them from the register
        if (currentFunc.getParavRegList().size() >= 1)
            currentFunc.getParavRegList().get(0).setEnforcedReg(NASMRegSet.rdi);
        if (currentFunc.getParavRegList().size() >= 2)
            currentFunc.getParavRegList().get(1).setEnforcedReg(NASMRegSet.rsi);
        if (currentFunc.getParavRegList().size() >= 3)
            currentFunc.getParavRegList().get(2).setEnforcedReg(NASMRegSet.rdx);
        if (currentFunc.getParavRegList().size() >= 4)
            currentFunc.getParavRegList().get(3).setEnforcedReg(NASMRegSet.rcx);
        if (currentFunc.getParavRegList().size() >= 5)
            currentFunc.getParavRegList().get(4).setEnforcedReg(NASMRegSet.r8);
        if (currentFunc.getParavRegList().size() >= 6)
            currentFunc.getParavRegList().get(5).setEnforcedReg(NASMRegSet.r9);
    }

    public void processForcePara()
    {
        for (IRFunction function : irRoot.getFunctionMap().values())
            setFuncArgReg(function);
    }
}
