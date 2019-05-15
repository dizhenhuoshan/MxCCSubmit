package princeYang.mxcc.ir;

import princeYang.mxcc.ast.ForStateNode;
import princeYang.mxcc.errors.MxError;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class BasicBlock
{
    private IRFunction parentFunction;
    private String blockName;
    private IRInstruction headInst = null, tailInst = null;
    private Set<BasicBlock> prevBlocks = new HashSet<BasicBlock>();
    private Set<BasicBlock> nextBlocks = new HashSet<BasicBlock>();
    private boolean containJump = false;
    private ForStateNode forStateNode;

    private static int globalBlockID = 0;
    private int localBlockID;

    private int preOrderIndex, postOrderIndex;

    public BasicBlock(IRFunction parentFunction, String blockName)
    {
        this.parentFunction = parentFunction;
        this.blockName = blockName;
        this.localBlockID = globalBlockID++;
    }

    public int getLocalBlockID()
    {
        return localBlockID;
    }

    public String getBlockName()
    {
        return blockName;
    }

    public IRInstruction getHeadInst()
    {
        return headInst;
    }

    public IRInstruction getTailInst()
    {
        return tailInst;
    }

    public void setHeadInst(IRInstruction headInst)
    {
        this.headInst = headInst;
    }

    public void setTailInst(IRInstruction tailInst)
    {
        this.tailInst = tailInst;
    }

    public void appendInst(IRInstruction newTail)
    {
        if (containJump)
            throw new MxError("IR BasicBlock: this block is finished, don't accept inst any more :)\n");
//            return;
        if (headInst == null)
            headInst = tailInst = newTail;
        else
        {
            tailInst.append(newTail);
            this.tailInst = newTail;
        }
    }

    public Set<BasicBlock> getPrevBlocks()
    {
        return prevBlocks;
    }

    public Set<BasicBlock> getNextBlocks()
    {
        return nextBlocks;
    }

    public ForStateNode getForStateNode()
    {
        return forStateNode;
    }

    public IRFunction getParentFunction()
    {
        return parentFunction;
    }

    public void setForStateNode(ForStateNode forStateNode)
    {
        this.forStateNode = forStateNode;
    }

    public int getPostOrderIndex()
    {
        return postOrderIndex;
    }

    public void setPostOrderIndex(int postOrderIndex)
    {
        this.postOrderIndex = postOrderIndex;
    }

    public int getPreOrderIndex()
    {
        return preOrderIndex;
    }

    public void setPreOrderIndex(int preOrderIndex)
    {
        this.preOrderIndex = preOrderIndex;
    }

    public void addNextBlock(BasicBlock nextBlock)
    {
        this.nextBlocks.add(nextBlock);
        if (nextBlock != null)
            nextBlock.getPrevBlocks().add(this);
    }

    public void deleteNextBlock(BasicBlock nextBlock)
    {
        this.nextBlocks.remove(nextBlock);
        if (nextBlock != null)
            nextBlock.getPrevBlocks().remove(this);
    }

    public boolean isContainJump()
    {
        return containJump;
    }

    public void setJumpInst (BranchBaseInst jumpInst)
    {
        appendInst(jumpInst);
        this.containJump = true;
        if (jumpInst instanceof Return)
            this.parentFunction.getReturnInstList().add((Return) jumpInst);
        else if (jumpInst instanceof Jump)
            addNextBlock(((Jump) jumpInst).getTargetBlock());
        else if (jumpInst instanceof Branch)
        {
            addNextBlock(((Branch) jumpInst).getThenBlock());
            addNextBlock(((Branch) jumpInst).getElseBlock());
        }
        else throw new MxError("IR BasicBlock: jumpInst is invalid in setJumpInst!\n");
    }

    public void deleteJumpInst()
    {
        this.containJump = false;
        if (tailInst instanceof Return)
            parentFunction.getReturnInstList().remove((Return) tailInst);
        else if (tailInst instanceof Jump)
            deleteNextBlock(((Jump) tailInst).getTargetBlock());
        else if (tailInst instanceof Branch)
        {
            deleteNextBlock(((Branch) tailInst).getThenBlock());
            deleteNextBlock(((Branch) tailInst).getElseBlock());
        }
        else
            throw new MxError("IR BasicBlock: jumpInst is invalid in deleteJumpInst!\n");
    }

    public void reset()
    {
        headInst = null;
        tailInst = null;
        containJump = false;
    }

    public void accept(IRVisitor visitor)
    {
        visitor.visit(this);
    }
}
