package princeYang.mxcc.backend;

import princeYang.mxcc.ast.BinaryExprNode;
import princeYang.mxcc.ir.*;

public class NASMRegFormProcessor
{
    private IRROOT irRoot;

    public NASMRegFormProcessor(IRROOT irRoot)
    {
        this.irRoot = irRoot;
    }

    public void transRegToNASMForm()
    {
        for (IRFunction function : irRoot.getFunctionMap().values())
        {
            for (BasicBlock basicBlock : function.getReversePostOrder())
            {
                for (IRInstruction inst = basicBlock.getHeadInst(); inst != null; inst = inst.getNext())
                {
                    if (inst instanceof BinaryOperation)
                    {
                        BinaryOperation binst = (BinaryOperation) inst;
                        if (binst.getResReg() != binst.getLhs())
                        {
                            // des bop op1 des
                            if (binst.getResReg() == binst.getRhs())
                            {
                                if (binst.isExchangeable())
                                {
                                    binst.setRhs(binst.getLhs());
                                    binst.setLhs(binst.getResReg());
                                }
                                else
                                {
                                    VirtualReg rhsBackup = new VirtualReg("rhs_copy");
                                    binst.prepend(new Move(binst.getFatherBlock(), rhsBackup, binst.getRhs()));
                                    binst.prepend(new Move(binst.getFatherBlock(), binst.getResReg(), binst.getLhs()));
                                    binst.setRhs(rhsBackup);
                                    binst.setLhs(binst.getResReg());
                                }
                            }
                            else
                            {
                                // des op1 op2
                                if (!binst.isDiverUsed())
                                {
                                    binst.prepend(new Move(binst.getFatherBlock(), binst.getResReg(), binst.getLhs()));
                                    binst.setLhs(binst.getResReg());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
