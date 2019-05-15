package princeYang.mxcc.backend;

import princeYang.mxcc.ir.*;

public class FinalInstructionOptimizer
{
    private IRROOT irRoot;

    public FinalInstructionOptimizer(IRROOT irRoot)
    {
        this.irRoot = irRoot;
    }

    public void optimize()
    {
        for (IRFunction function : irRoot.getFunctionMap().values())
        {
            for (BasicBlock basicBlock : function.getReversePostOrder())
            {
                IRInstruction prevInst = null;
                for (IRInstruction instruction = basicBlock.getHeadInst(); instruction != null; instruction = instruction.getNext())
                {
                    boolean remove = false;
                    if (instruction instanceof Load)
                    {
                        if ((prevInst instanceof Store)
                                && (((Store) prevInst).getSrc() == ((Load) instruction).getDestReg())
                                && (((Store) prevInst).getAddr() == ((Load) instruction).getAddr())
                                && (((Store) prevInst).getOffset() == ((Load) instruction).getOffset())
                                && ((Store) prevInst).getSize() == ((Load) instruction).getSize())
                        {
                            remove = true;
                        }

                    }
                    else if (instruction instanceof Store)
                    {
                        if ((prevInst instanceof Load)
                                && (((Load) prevInst).getDestReg() == ((Store) instruction).getSrc())
                                && (((Load) prevInst).getAddr() == ((Store) instruction).getAddr())
                                && (((Load) prevInst).getOffset() == ((Store) instruction).getOffset())
                                && (((Load) prevInst).getSize() == ((Store) instruction).getSize()))
                        {
                            remove = true;
                        }
                    }
                    else if (instruction instanceof Move)
                    {
                        if (((Move) instruction).getDestReg() == ((Move) instruction).getValue())
                            remove = true;
                        else if ((prevInst instanceof Move)
                                && ((Move) instruction).getDestReg() == ((Move) prevInst).getValue()
                                && ((Move) instruction).getValue() == ((Move) prevInst).getDestReg())
                            remove = true;
                    }
                    if (remove)
                        instruction.remove();
                    else
                        prevInst = instruction;
                }
            }
        }
    }

}
