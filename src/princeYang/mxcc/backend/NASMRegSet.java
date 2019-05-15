package princeYang.mxcc.backend;

import princeYang.mxcc.ir.PhysicalReg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NASMRegSet
{
    public static final Collection<PhysicalReg> allRegs;
    public static final Collection<PhysicalReg> callerSaveRegs;
    public static final Collection<PhysicalReg> calleeSaveRegs;
    public static final Collection<PhysicalReg> generalRegs;
    public static final List<PhysicalReg> funcParaRegs = new ArrayList<PhysicalReg>();

    public static NASMReg rax, rcx, rdx, rbx, rsp, rbp, rsi, rdi, r8, r9, r10, r11, r12, r13, r14, r15;

    static
    {
        rax = new NASMReg("rax", -1, true, false, false);
        rcx = new NASMReg("rcx", 3, true, false, false);
        rdx = new NASMReg("rdx", 2, true, false, false);
        rbx = new NASMReg("rbx", 3, false, true, false);
        rsp = new NASMReg("rsp", -1, true, false, false);
        rbp = new NASMReg("rbp", -1, false, true, false);
        rsi = new NASMReg("rsi", 1, true, false, false);
        rdi = new NASMReg("rdi", 0, true, false, false);
        r8 = new NASMReg("r8", 4, true, false, true);
        r9 = new NASMReg("r9", 5, true, false, true);
        r10 = new NASMReg("r10", -1, true, false, true);
        r11 = new NASMReg("r11", -1, true, false, true);
        r12 = new NASMReg("r12", -1, false, true, true);
        r13 = new NASMReg("r13", -1, false, true, true);
        r14 = new NASMReg("r14", -1, false, true, true);
        r15 = new NASMReg("r15", -1, false, true, true);

        List<NASMReg> allReg = new ArrayList<NASMReg>();
        List<NASMReg> callerSaveReg = new ArrayList<NASMReg>();
        List<NASMReg> calleeSaveReg = new ArrayList<NASMReg>();
        List<NASMReg> generalReg = new ArrayList<NASMReg>();

        allReg.add(rax);
        allReg.add(rcx);
        allReg.add(rdx);
        allReg.add(rbx);
        allReg.add(rsp);
        allReg.add(rbp);
        allReg.add(rsi);
        allReg.add(rdi);
        allReg.add(r8);
        allReg.add(r9);
        allReg.add(r10);
        allReg.add(r11);
        allReg.add(r12);
        allReg.add(r13);
        allReg.add(r14);
        allReg.add(r15);

        funcParaRegs.add(rdi);
        funcParaRegs.add(rsi);
        funcParaRegs.add(rdx);
        funcParaRegs.add(rcx);
        funcParaRegs.add(r8);
        funcParaRegs.add(r9);

        for(NASMReg nasmReg : allReg)
        {
            if (nasmReg.isGeneral())
                generalReg.add(nasmReg);
            if (nasmReg.isCalleeSave())
                calleeSaveReg.add(nasmReg);
            if (nasmReg.isCallerSave())
                callerSaveReg.add(nasmReg);
        }

        allRegs = Collections.unmodifiableCollection(allReg);
        calleeSaveRegs = Collections.unmodifiableCollection(calleeSaveReg);
        callerSaveRegs = Collections.unmodifiableCollection(callerSaveReg);
        generalRegs = Collections.unmodifiableCollection(generalReg);
    }
}
