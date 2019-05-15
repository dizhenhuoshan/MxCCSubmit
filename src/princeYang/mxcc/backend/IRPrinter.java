package princeYang.mxcc.backend;

import princeYang.mxcc.ir.*;

import java.io.PrintStream;
import java.util.*;

public class IRPrinter implements IRVisitor
{

    private PrintStream out;
    private Map<BasicBlock, String> blockMap = new HashMap<BasicBlock, String>();
    private Map<VirtualReg, String> virtualRegMap = new HashMap<VirtualReg, String>();
    private Map<StaticData, String> staticDataMap = new HashMap<StaticData, String>();
    private Map<String, Integer> blockCnt = new HashMap<String, Integer>();
    private Map<String, Integer> vRegCnt = new HashMap<String, Integer>();
    private Map<String, Integer> staticDataCnt = new HashMap<String, Integer>();
    private Set<BasicBlock> visitedBlocks = new HashSet<BasicBlock>();
    private boolean isStaticDef = false;

    public IRPrinter(PrintStream out)
    {
        this.out = out;
    }

    private String genID(String name, Map<String, Integer> cnt)
    {
        int cntName = cnt.getOrDefault(name, 0) + 1;
        cnt.put(name, cntName);
        if (cntName == 1) return name;
        return name + "_" + cntName;
    }

    private String getBBID(BasicBlock basicBlock)
    {
        String id = blockMap.get(basicBlock);
        if (id == null)
        {
            if (basicBlock.getBlockName() == null)
                id = genID("bb", blockCnt);
            else
                id = genID(basicBlock.getBlockName(), blockCnt);
            blockMap.put(basicBlock, id);
        }
        return id;
    }

    private String getVRegID(VirtualReg vreg)
    {
        String id = virtualRegMap.get(vreg);
        if (id == null)
        {
            if (vreg.getvRegName() == null)
                id = genID("vreg", vRegCnt);
            else
                id = genID(vreg.getvRegName(), vRegCnt);
            virtualRegMap.put(vreg, id);
        }
        return id;
    }

    private String getStaticDataID(StaticData staticData)
    {
        String id = staticDataMap.get(staticData);
        if (id == null)
        {
            if (staticData.getIdent() == null)
                id = genID("staticData", staticDataCnt);
            else
                id = genID(staticData.getIdent(), staticDataCnt);
            staticDataMap.put(staticData, id);
        }
        return id;
    }


    @Override
    public void visit(IRROOT IRNode)
    {
        isStaticDef = true;
        for (StaticData staticData : IRNode.getStaticDataList())
            staticData.accept(this);
        isStaticDef = false;

        for (StaticStr staticStr : IRNode.getStaticStrMap().values())
            staticStr.accept(this);

        out.println();

        for (IRFunction function : IRNode.getFunctionMap().values())
            function.accept(this);
    }

    @Override
    public void visit(IRFunction IRNode)
    {
        virtualRegMap = new IdentityHashMap<>();
        vRegCnt = new HashMap<>();
        out.printf("func %s ", IRNode.getFuncName());
        for (VirtualReg paraVReg : IRNode.getParavRegList())
            out.printf("$%s ", getVRegID(paraVReg));
        out.print("{\n");
        for (BasicBlock bb : IRNode.getReversePostOrder())
            bb.accept(this);
        out.print("}\n\n");
    }

    @Override
    public void visit(BasicBlock IRNode)
    {
        if (visitedBlocks.contains(IRNode)) return;
        visitedBlocks.add(IRNode);
        out.println("%" + getBBID(IRNode) + ":");
        for (IRInstruction inst = IRNode.getHeadInst(); inst != null; inst = inst.getNext())
            inst.accept(this);
    }

    @Override
    public void visit(VirtualReg IRNode)
    {
        out.print("$" + getVRegID(IRNode));
    }

    @Override
    public void visit(Immediate IRNode)
    {
        out.print(IRNode.getValue());
    }

    @Override
    public void visit(Pop IRNode)
    {

    }

    @Override
    public void visit(Push IRNode)
    {

    }

    @Override
    public void visit(Return IRNode)
    {
        out.print("    ret ");
        if (IRNode.getRetValue() != null)
            IRNode.getRetValue().accept(this);
        else
            out.print("0");
        out.println();
        out.println();
    }

    @Override
    public void visit(BinaryOperation IRNode)
    {
        out.print("    ");
        String op = null;
        switch (IRNode.getBop())
        {
            case ADD:
                op = "add";
                break;
            case SUB:
                op = "sub";
                break;
            case MUL:
                op = "mul";
                break;
            case DIV:
                op = "div";
                break;
            case MOD:
                op = "rem";
                break;
            case SHL:
                op = "shl";
                break;
            case SHR:
                op = "shr";
                break;
            case BITWISE_AND:
                op = "and";
                break;
            case BITWISE_OR:
                op = "or";
                break;
            case BITWISE_XOR:
                op = "xor";
                break;
        }
        IRNode.getResReg().accept(this);
        out.printf(" = %s ", op);
        IRNode.getLhs().accept(this);
        out.print(" ");
        IRNode.getRhs().accept(this);
        out.println();
    }

    @Override
    public void visit(UnaryOperation IRNode)
    {
        out.print("    ");
        String op = null;
        switch (IRNode.getUop())
        {
            case NEG:
                op = "neg";
                break;
            case BITWISE_NOT:
                op = "not";
                break;
        }
        IRNode.getResReg().accept(this);
        out.printf(" = %s ", op);
        IRNode.getSrcValue().accept(this);
        out.println();
    }

    @Override
    public void visit(HeapAllocate IRNode)
    {
        out.print("    ");
        IRNode.getDestReg().accept(this);
        out.print(" = alloc ");
        IRNode.getAllocateSize().accept(this);
        out.println();
    }

    @Override
    public void visit(Jump IRNode)
    {
        out.printf("    jump %%%s\n\n", getBBID(IRNode.getTargetBlock()));
    }

    @Override
    public void visit(Load IRNode)
    {
        out.print("    ");
        IRNode.getDestReg().accept(this);
        out.printf(" = load %d ", IRNode.getSize());
        IRNode.getAddr().accept(this);
        out.println(" " + IRNode.getOffset());
    }

    @Override
    public void visit(Store IRNode)
    {
        out.printf("    store %d ", IRNode.getSize());
        IRNode.getAddr().accept(this);
        out.print(" ");
        IRNode.getSrc().accept(this);
        out.println(" " + IRNode.getOffset());
    }

    @Override
    public void visit(Move IRNode)
    {
        out.print("    ");
        IRNode.getDestReg().accept(this);
        out.print(" = move ");
        IRNode.getValue().accept(this);
        out.println();
    }

    @Override
    public void visit(PhysicalReg IRNode)
    {

    }

    @Override
    public void visit(Branch IRNode)
    {
        out.print("    br ");
        IRNode.getCond().accept(this);
        out.println(" %" + getBBID(IRNode.getThenBlock()) + " %" + getBBID(IRNode.getElseBlock()));
        out.println();
    }

    @Override
    public void visit(Comparison IRNode)
    {
        out.print("    ");
        String op = null;
        switch (IRNode.getComparisonOp())
        {
            case E:
                op = "seq";
                break;
            case NE:
                op = "sne";
                break;
            case G:
                op = "sgt";
                break;
            case GE:
                op = "sge";
                break;
            case L:
                op = "slt";
                break;
            case LE:
                op = "sle";
                break;
        }
        IRNode.getResReg().accept(this);
        out.printf(" = %s ", op);
        IRNode.getLhs().accept(this);
        out.print(" ");
        IRNode.getRhs().accept(this);
        out.println();
    }

    @Override
    public void visit(FuncCall IRNode)
    {
        out.print("    ");
        if (IRNode.getRetReg() != null)
        {
            IRNode.getRetReg().accept(this);
            out.print(" = ");
        }
        out.printf("call %s ", IRNode.getFunction().getFuncName());
        for (IRValue arg : IRNode.getParas())
        {
            arg.accept(this);
            out.print(" ");
        }
        out.println();
    }

    @Override
    public void visit(StaticStr IRNode)
    {
        if (isStaticDef)
            out.printf("asciiz @%s %s\n", getStaticDataID(IRNode), IRNode.getStaticValue());
        else
            out.print("@" + getStaticDataID(IRNode));
    }

    @Override
    public void visit(StaticVar IRNode)
    {
        if (isStaticDef)
            out.printf("space @%s %d\n", getStaticDataID(IRNode), IRNode.getSize());
        else
            out.print("@" + getStaticDataID(IRNode));
    }
}
