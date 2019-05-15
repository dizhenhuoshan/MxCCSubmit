package princeYang.mxcc.ir;

import java.util.Map;

public abstract class BranchBaseInst extends IRInstruction
{
    public BranchBaseInst(BasicBlock basicBlock, IRInstruction prev, IRInstruction next)
    {
        super(basicBlock, prev, next);
    }

    public BranchBaseInst(BasicBlock basicBlock)
    {
        super(basicBlock);
    }

    @Override
    public abstract BranchBaseInst copyAndRename(Map<Object, Object> renameMap);
}
