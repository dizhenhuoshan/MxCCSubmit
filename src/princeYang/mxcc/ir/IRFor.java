package princeYang.mxcc.ir;

public class IRFor
{
    public BasicBlock stopBlock, stepBlock, loopBodyBlock, loopAfterBlock;
    public boolean isProcessed = false;

    public IRFor(BasicBlock stopBlock, BasicBlock stepBlock, BasicBlock loopBodyBlock, BasicBlock loopAfterBlock)
    {
        this.stopBlock = stopBlock;
        this.stepBlock = stepBlock;
        this.loopBodyBlock = loopBodyBlock;
        this.loopAfterBlock = loopAfterBlock;
    }
}
