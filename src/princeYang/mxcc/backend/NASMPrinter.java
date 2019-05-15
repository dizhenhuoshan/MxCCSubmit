package princeYang.mxcc.backend;

import princeYang.mxcc.Config;
import princeYang.mxcc.errors.MxError;
import princeYang.mxcc.ir.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class NASMPrinter implements IRVisitor
{

    private PrintStream out;
    private Map<Object, String> identMap = new HashMap<Object, String>();
    private Map<String, Integer> identCnt = new HashMap<String, Integer>();
    private PhysicalReg preg0, preg1;
    private boolean inBSS, inData;

    public NASMPrinter(PrintStream out)
    {
        this.out = out;
    }

    private String genIdentCnt(String ident)
    {
        int currentCnt = identCnt.getOrDefault(ident, 0) + 1;
        identCnt.put(ident, currentCnt);
        return String.format("%s_%d", ident, currentCnt);
    }

    private String genBlockIdent(BasicBlock basicBlock)
    {
        String ident = identMap.get(basicBlock);
        if (ident == null)
        {
            ident = "__block_" + genIdentCnt(basicBlock.getBlockName());
            identMap.put(basicBlock, ident);
        }
        return ident;
    }

    private String genDataIdent(StaticData staticData)
    {
        String ident = identMap.get(staticData);
        if (ident == null)
        {
            ident = "__static_" + genIdentCnt(staticData.getIdent());
            identMap.put(staticData, ident);
        }
        return ident;
    }

    private String genSize(int size)
    {
        switch (size)
        {
            case 1:
                return "byte";
            case 2:
                return "word";
            case 4:
                return "dword";
            case 8:
                return "qword";
            default:
                throw new MxError("NASM Printer: genSize size invalid\n");
        }
    }

    private String genStaticString(String str)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);
            stringBuilder.append((int) ch);
            stringBuilder.append(", ");
        }
        stringBuilder.append(0);
        return stringBuilder.toString();
    }

    @Override
    public void visit(IRROOT IRNode)
    {
        preg0 = IRNode.getPreg0();
        preg1 = IRNode.getPreg1();
        identMap.put(IRNode.getFunctionMap().get("main").getBlockEnter(), "main");

        out.println("\t\tglobal\tmain");
        out.println();
        out.println("\t\textern\tmalloc");
        out.println();

        if (IRNode.getStaticDataList().size() > 0)
        {
            inBSS = true;
            out.println("\t\tsection\t.bss");
            for (StaticData data : IRNode.getStaticDataList())
                data.accept(this);
            out.println();
            inBSS = false;
        }

        if (IRNode.getStaticStrMap().values().size() > 0)
        {
            inData = true;
            out.println("\t\tsection\t.data");
            for (StaticStr staticStr : IRNode.getStaticStrMap().values())
                staticStr.accept(this);
            out.println();
            inData = false;
        }

        out.println("\t\tsection\t.text");
        out.println();

        for (IRFunction function : IRNode.getFunctionMap().values())
            function.accept(this);
        out.println();

        try
        {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("lib/buildin.asm"));
            String fileLine;
            while ((fileLine = bufferedReader.readLine()) != null)
                out.println(fileLine);
        }
        catch (Throwable th)
        {
            throw new MxError("Error when putting buildin asm\n");
        }
    }

    @Override
    public void visit(IRFunction IRNode)
    {
        out.printf("# function %s\n\n", IRNode.getFuncName());
        for (BasicBlock basicBlock : IRNode.getReversePostOrder())
            basicBlock.accept(this);
    }

    @Override
    public void visit(BasicBlock IRNode)
    {
        out.printf("%s:\n", genBlockIdent(IRNode));
        for (IRInstruction instruction = IRNode.getHeadInst(); instruction != null; instruction = instruction.getNext())
            instruction.accept(this);
        out.println();
    }

    @Override
    public void visit(VirtualReg IRNode)
    {
        throw new MxError("VirtualReg appear in NASM Printer!\n");
    }

    @Override
    public void visit(Immediate IRNode)
    {
        out.print(IRNode.getValue());
    }

    @Override
    public void visit(Pop IRNode)
    {
        out.print("\t\tpop\t\t");
        IRNode.getTargetReg().accept(this);
        out.println();
    }

    @Override
    public void visit(Push IRNode)
    {
        out.print("\t\tpush\t");
        IRNode.getSourceValue().accept(this);
        out.println();
    }

    @Override
    public void visit(Return IRNode)
    {
        out.println("\t\tret\t\t");
    }

    @Override
    public void visit(BinaryOperation IRNode)
    {
        if (IRNode.getBop() == IRBinaryOp.DIV || IRNode.getBop() == IRBinaryOp.MOD)
        {
            out.print("\t\tmov\t\trbx, ");
            IRNode.getRhs().accept(this);
            out.println();
            out.print("\t\tmov\t\trax, ");
            IRNode.getLhs().accept(this);
            out.println();
            out.println("\t\tmov\t\t" + preg0.getName() + ", rdx");
            out.println("\t\tcdq");
            out.println("\t\tidiv\trbx");
            out.print("\t\tmov\t\t");
            IRNode.getResReg().accept(this);
            if (IRNode.getBop() == IRBinaryOp.DIV)
                out.println(", rax");
            else
                out.println(", rdx");
            out.println("\t\tmov\t\trdx, " + preg0.getName());
        }
        else if (IRNode.getBop() == IRBinaryOp.SHL || IRNode.getBop() == IRBinaryOp.SHR)
        {
            out.println("\t\tmov\t\trbx, rcx");
            out.print("\t\tmov\t\trcx, ");
            IRNode.getRhs().accept(this);
            out.println();
            if (IRNode.getBop() == IRBinaryOp.SHL)
                out.print("\t\tsal\t\t");
            else
                out.print("\t\tsar\t\t");
            IRNode.getLhs().accept(this);
            out.println(", cl");
            out.println("\t\tmov\t\trcx, rbx");
            out.print("\t\tand\t\t");
            IRNode.getLhs().accept(this);
            out.println(", -1");
        }
        else
        {
            if (IRNode.getResReg() != IRNode.getLhs())
                throw new MxError("NASM binary format error! \n");
            String bop;
            switch (IRNode.getBop())
            {
                case ADD:
                    if (IRNode.getRhs() instanceof Immediate && ((Immediate) IRNode.getRhs()).getValue() == 1)
                    {
                        out.print("\t\tinc\t\t");
                        IRNode.getLhs().accept(this);
                        out.println();
                        return;
                    }
                    bop = "add\t\t";
                    break;
                case SUB:
                    if (IRNode.getRhs() instanceof Immediate && ((Immediate) IRNode.getRhs()).getValue() == 1)
                    {
                        out.print("\t\tdec\t\t");
                        IRNode.getLhs().accept(this);
                        out.println();
                        return;
                    }
                    bop = "sub\t\t";
                    break;
                case MUL:
                    if (IRNode.getRhs() instanceof Immediate && ((Immediate) IRNode.getRhs()).getValue() == 1)
                        return;
                    bop = "imul\t";
                    break;
                case BITWISE_AND:
                    bop = "and\t\t";
                    break;
                case BITWISE_OR:
                    bop = "or\t\t";
                    break;
                case BITWISE_XOR:
                    bop = "xor\t\t";
                    break;
                default:
                    throw new MxError("NASM Printer: Binary Opreator Error\n");
            }
            out.print("\t\t" + bop);
            IRNode.getLhs().accept(this);
            out.print(", ");
            IRNode.getRhs().accept(this);
            out.println();
        }
    }

    @Override
    public void visit(UnaryOperation IRNode)
    {
        String uop;
        switch (IRNode.getUop())
        {
            case BITWISE_NOT:
                uop = "\t\tnot\t\t";
                break;
            case NEG:
                uop = "\t\tneg\t\t";
                break;
            default:
                throw new MxError("NASM Printer: Unary Opreator Error\n");
        }
        out.print("\t\tmov\t\t");
        IRNode.getResReg().accept(this);
        out.print(", ");
        IRNode.getSrcValue().accept(this);
        out.println();
        out.print("\t\t" + uop + "\t\t");
        IRNode.getResReg().accept(this);
        out.println();
    }

    @Override
    public void visit(HeapAllocate IRNode)
    {
        out.println("\t\tcall\tmalloc");
    }

    @Override
    public void visit(Jump IRNode)
    {
        if (IRNode.getTargetBlock().getPostOrderIndex() == IRNode.getFatherBlock().getPostOrderIndex() - 1)
            return;
        else
            out.printf("\t\tjmp\t\t%s\n", genBlockIdent(IRNode.getTargetBlock()));
    }

    @Override
    public void visit(Load IRNode)
    {
        if (IRNode.getAddr() instanceof StaticStr)
        {
            out.println("\t\tmov\t\t");
            IRNode.getDestReg().accept(this);
            out.print(", " + genSize(IRNode.getSize()) + " ");
            IRNode.getAddr().accept(this);
            out.println();
        }
        else
        {
            out.print("\t\tmov\t\t");
            IRNode.getDestReg().accept(this);
            out.print(", " + genSize(IRNode.getSize()) + " [");
            IRNode.getAddr().accept(this);
            if (IRNode.getOffset() > 0)
                out.print("+" + IRNode.getOffset());
            else if(IRNode.getOffset() < 0)
                out.print(IRNode.getOffset());
            out.println("]");
        }
    }

    @Override
    public void visit(Store IRNode)
    {
        if (IRNode.getAddr() instanceof StaticStr)
        {
            out.print("\t\tmov\t\t" + genSize(IRNode.getSize()) + " [");
            IRNode.getAddr().accept(this);
            out.print(" ");
            IRNode.getSrc().accept(this);
            out.println();
        }
        else
        {
            out.print("\t\tmov\t\t" + genSize(IRNode.getSize()) + " [");
            IRNode.getAddr().accept(this);
            if (IRNode.getOffset() > 0)
                out.print("+" + IRNode.getOffset());
            else if(IRNode.getOffset() < 0)
                out.print(IRNode.getOffset());
            out.print("], ");
            IRNode.getSrc().accept(this);
            out.println();
        }
    }

    @Override
    public void visit(Move IRNode)
    {
        out.print("\t\tmov\t\t");
        IRNode.getDestReg().accept(this);
        out.print(", ");
        IRNode.getValue().accept(this);
        out.println();
    }

    @Override
    public void visit(PhysicalReg IRNode)
    {
        out.print(IRNode.getName());
    }

    @Override
    public void visit(Branch IRNode)
    {
        if (IRNode.getCond() instanceof Immediate)
        {
            int boolValue = ((Immediate) IRNode.getCond()).getValue();
            if (boolValue == 1)
                out.printf("\t\tjmp\t\t%s\n", genBlockIdent(IRNode.getThenBlock()));
            else
                out.printf("\t\tjmp\t\t%s\n", genBlockIdent(IRNode.getElseBlock()));
        }
        else
        {
            out.print("\t\tcmp\t\t");
            IRNode.getCond().accept(this);
            out.println(", 1");
            out.printf("\t\tje\t\t%s\n", genBlockIdent(IRNode.getThenBlock()));
            if (IRNode.getElseBlock().getPostOrderIndex() == IRNode.getFatherBlock().getPostOrderIndex() - 1)
                return;
            out.printf("\t\tjmp\t\t%s\n", genBlockIdent(IRNode.getElseBlock()));
        }
    }

    @Override
    public void visit(Comparison IRNode)
    {
        if (IRNode.getLhs() instanceof PhysicalReg)
        {
            out.print("\t\tand\t\t");
            IRNode.getLhs().accept(this);
            out.println(", -1");
        }
        if (IRNode.getRhs() instanceof PhysicalReg)
        {
            out.print("\t\tand\t\t");
            IRNode.getRhs().accept(this);
            out.println(", -1");
        }
        out.println("\t\txor\t\trax, rax");
        out.print("\t\tcmp\t\t");
        IRNode.getLhs().accept(this);
        out.print(", ");
        IRNode.getRhs().accept(this);
        out.println();
        String cop;
        switch (IRNode.getComparisonOp())
        {
            case E:
                cop = "\t\tsete\t";
                break;
            case L:
                cop = "\t\tsetl\t";
                break;
            case G:
                cop = "\t\tsetg\t";
                break;
            case NE:
                cop = "\t\tsetne\t";
                break;
            case LE:
                cop = "\t\tsetle\t";
                break;
            case GE:
                cop = "\t\tsetge\t";
                break;
            default:
                throw new MxError("NASM Printer: Comparision Operator Error!\n");
        }
        out.println(cop + "al");
        out.print("\t\tmov\t\t");
        IRNode.getResReg().accept(this);
        out.println(", rax");
    }

    @Override
    public void visit(FuncCall IRNode)
    {
        if (IRNode.getFunction().isBuildIn())
            out.println("\t\tcall\t" + IRNode.getFunction().getBuildInName());
        else
            out.println("\t\tcall\t" + genBlockIdent(IRNode.getFunction().getBlockEnter()));
    }

    @Override
    public void visit(StaticStr IRNode)
    {
        if (inData)
        {
            out.printf("%s:\n", genDataIdent(IRNode));
            out.printf("\t\tdq\t\t%d\n", IRNode.getStaticValue().length());
            out.printf("\t\tdb\t\t%s\n", genStaticString(IRNode.getStaticValue()));
        }
        else
            out.print(genDataIdent(IRNode));
    }

    @Override
    public void visit(StaticVar IRNode)
    {
        if (inBSS)
        {
            String lengthOp;
            switch (IRNode.getSize())
            {
                case 1:
                    lengthOp = "resb";
                    break;
                case 2:
                    lengthOp = "resw";
                    break;
                case 4:
                    lengthOp = "resd";
                    break;
                case 8:
                    lengthOp = "resq";
                    break;
                default:
                    throw new MxError("NASM Printer: Global Var size invalid\n");
            }
            out.printf("%s:\t%s\t1\n", genDataIdent(IRNode), lengthOp);
        }
        else
            out.print(genDataIdent(IRNode));
    }
}
