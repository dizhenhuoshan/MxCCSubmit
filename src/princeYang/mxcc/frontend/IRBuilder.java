package princeYang.mxcc.frontend;

import princeYang.mxcc.Config;
import princeYang.mxcc.ast.*;
import princeYang.mxcc.errors.MxError;
import princeYang.mxcc.ir.*;
import princeYang.mxcc.scope.ClassEntity;
import princeYang.mxcc.scope.FuncEntity;
import princeYang.mxcc.scope.Scope;
import princeYang.mxcc.scope.VarEntity;

import java.util.*;
import java.util.function.UnaryOperator;

public class IRBuilder extends ScopeScanner
{
    private IRROOT irRoot = new IRROOT();
    private Scope globalScope, currentScope;
    private List<GlobalVarInit> globalVarInitList = new ArrayList<GlobalVarInit>();
    private boolean isArgDecl = false, memAccessing = false;
    private BasicBlock currentBlock;
    private IRFunction currentFunc;
    private String currentClass;
    private BasicBlock currentLoopStepBlock = null, currentLoopAfterBlock = null;
    private boolean uselessVar = false;

    public IRBuilder(Scope globalScope)
    {
        this.globalScope = globalScope;
    }

    @Override
    public void visit(MxProgNode node)
    {
        currentScope = globalScope;
        for (DeclNode declNode : node.getDeclNodesList())
        {
            if (declNode instanceof VarDeclNode)
                declNode.accept(this);
            else if (declNode instanceof FuncDeclNode)
            {
                FuncEntity funcEntity = currentScope.getFunc(declNode.getIdentName());
                irRoot.addFunction(new IRFunction(funcEntity));
            }
            else if (declNode instanceof ClassDeclNode)
            {
                ClassEntity classEntity = currentScope.getClass(declNode.getIdentName());
                currentScope = classEntity.getClassScope();
                for (FuncDeclNode classFuncDecl : ((ClassDeclNode) declNode).getFuncDeclList())
                {
                    FuncEntity funcEntity = currentScope.getFunc(classFuncDecl.getIdentName());
                    irRoot.addFunction(new IRFunction(funcEntity));
                }
                currentScope = currentScope.getFather();
            }
        }

        FuncDeclNode varInitFunc = globalVarInitFunc();
        varInitFunc.accept(this);

        for (DeclNode declNode: node.getDeclNodesList())
        {
            if (declNode instanceof FuncDeclNode)
                declNode.accept(this);
            else if (declNode instanceof ClassDeclNode)
                declNode.accept(this);
            else if (!(declNode instanceof VarDeclNode))
                throw new MxError("IRBuilder: declNode Type is invalid in visiting MxProgNode!\n");
        }
        for (IRFunction irFunction : irRoot.getFunctionMap().values())
            irFunction.updateCalleeSet();
        updateRecursiveCalleeSet();
    }

    @Override
    public void visit(VarDeclNode node)
    {
        VarEntity varEntity = currentScope.getVar(node.getIdentName());
        if (varEntity.isUnUsed())
            return;
        if (!currentScope.isGlobalScope())
        {
            // Not global variable
            VirtualReg virtualReg = new VirtualReg(varEntity.getIdent());
            varEntity.setIrReg(virtualReg);
            if (isArgDecl)
                currentFunc.insertArgReg(virtualReg);
            if (node.getInitValue() == null)
            {
                if (!isArgDecl)
                    currentBlock.appendInst(new Move(currentBlock, virtualReg, new Immediate(0)));
            }
            else
            {
                if (!(node.getInitValue() instanceof ConstBoolNode) && node.getInitValue().getType() instanceof BoolType)
                {
                    node.getInitValue().setBoolTrueBlock(new BasicBlock(currentFunc, "boolTrueBlock"));
                    node.getInitValue().setBoolFalseBlock(new BasicBlock(currentFunc, "boolFalseBlock"));
                }
                node.getInitValue().accept(this);
                assignProcessor(virtualReg, node.getInitValue(), 0, Config.regSize, false);
            }
        }
        else
        {
            // Global variable
            Type varType = node.getVarType().getType();
            StaticVar staticVar = new StaticVar(node.getIdentName(), Config.regSize);
            irRoot.addStaticData(staticVar);
            varEntity.setIrReg(staticVar);
            if (node.getInitValue() != null)
            {
                GlobalVarInit globalVarInit = new GlobalVarInit(node.getIdentName(), node.getInitValue());
                globalVarInitList.add(globalVarInit);
            }
        }
    }

    @Override
    public void visit(FuncDeclNode node)
    {
        String funcIdent = node.getIdentName();
        if (currentClass != null)
            funcIdent = genClassFuncName(currentClass, funcIdent);
        currentFunc = irRoot.getFunctionMap().get(funcIdent);
        currentFunc.generateEntry();
        currentBlock = currentFunc.getBlockEnter();
        // function para
        Scope prevScope = currentScope;
        currentScope = node.getFuncBlock().getScope();
        if (currentClass != null)
        {
            // this is the first para of a class function
            VirtualReg virtualReg = new VirtualReg("this");
            currentScope.getVar("this").setIrReg(virtualReg);
            currentFunc.insertArgReg(virtualReg);
        }
        isArgDecl = true;
        for (VarDeclNode varDeclNode : node.getParaDeclList())
            varDeclNode.accept(this);
        isArgDecl = false;
        currentScope = prevScope;
        // for main function, make global init.
        if (node.getIdentName().equals("main"))
            currentBlock.appendInst(new FuncCall(currentBlock, irRoot.getFunctionMap().get("__globalVarInit"),
                    null, new ArrayList<IRValue>()));
        node.getFuncBlock().accept(this);
        // for defalut return value
        if (!currentBlock.isContainJump())
        {
            if (node.getRetType() == null || node.getRetType().getType() instanceof VoidType)
                currentBlock.setJumpInst(new Return(currentBlock, null));
            else
                currentBlock.setJumpInst(new Return(currentBlock, new Immediate(0)));
        }
        // if function have multiply return, merge them to one block
        if (currentFunc.getReturnInstList().size() > 1)
        {
            BasicBlock mergeRetBlock = new BasicBlock(currentFunc, "__mergeReturn__" + currentFunc.getFuncName());
            VirtualReg returnReg;
            if (node.getRetType() == null || node.getRetType().getType() instanceof VoidType)
                returnReg = null;
            else
                returnReg = new VirtualReg("returnReg");
            List<Return> returnList = new ArrayList<Return>(currentFunc.getReturnInstList());
            for (Return returnInst : returnList)
            {
                BasicBlock parentBlock = returnInst.getFatherBlock();
                if (returnInst.getRetValue() != null)
                    returnInst.prepend(new Move(parentBlock, returnReg, returnInst.getRetValue()));
                returnInst.remove();
                parentBlock.setJumpInst(new Jump(parentBlock, mergeRetBlock));
            }
            mergeRetBlock.setJumpInst(new Return(mergeRetBlock, returnReg));
            currentFunc.setBlockLeave(mergeRetBlock);
        }
        else
            // only one return inst, use it for final return block
            currentFunc.setBlockLeave(currentFunc.getReturnInstList().get(0).getFatherBlock());

        currentFunc = null;
    }

    @Override
    public void visit(ClassDeclNode node)
    {
        currentClass = node.getIdentName();
        currentScope = globalScope;
        for (FuncDeclNode funcDecl: node.getFuncDeclList())
            funcDecl.accept(this);
        currentClass = null;
    }

    @Override
    public void visit(FuncBlockNode node)
    {
        currentScope = node.getScope();
        for (Node subNode : node.getStateList())
        {
            if (subNode instanceof StateNode || subNode instanceof VarDeclNode)
                subNode.accept(this);
            else
                throw new MxError("IR FuncBlockNode: node in StateList is invalid!\n");
            if (currentBlock.isContainJump())
                break;
        }
        currentScope = currentScope.getFather();
    }

    @Override
    public void visit(WhileStateNode node)
    {
        BasicBlock loopBodyBlock = new BasicBlock(currentFunc, "__loop__while_body");
        BasicBlock loopAfterBlock = new BasicBlock(currentFunc, "__loop__while_after");
        BasicBlock condBlock = new BasicBlock(currentFunc, "__loop__while_cond");
        BasicBlock prevLoopCondBlock = currentLoopStepBlock, prevLoopAfterBlock = currentLoopAfterBlock;
        currentLoopStepBlock = condBlock;
        currentLoopAfterBlock = loopAfterBlock;

        // check cond -> loop -> check cond -> loop -> ......
        currentBlock.setJumpInst(new Jump(currentBlock, condBlock));
        currentBlock = condBlock;
        node.getConditionExpr().setBoolTrueBlock(loopBodyBlock);
        node.getConditionExpr().setBoolFalseBlock(loopAfterBlock);
        node.getConditionExpr().accept(this);

        // for while(1/0) simply use branch of condition block
        if (node.getConditionExpr() instanceof ConstBoolNode)
            currentBlock.setJumpInst(new Branch(currentBlock, node.getConditionExpr().getRegValue(),
                    node.getConditionExpr().getBoolTrueBlock(), node.getConditionExpr().getBoolFalseBlock()));

        // proocess loop body
        currentBlock = loopBodyBlock;
        node.getLoopState().accept(this);
        if (!currentBlock.isContainJump())
            currentBlock.setJumpInst(new Jump(currentBlock, condBlock));

        // done, escape while
        currentLoopStepBlock = prevLoopCondBlock;
        currentLoopAfterBlock = prevLoopAfterBlock;
        currentBlock = loopAfterBlock;
    }

    @Override
    public void visit(ForStateNode node)
    {
        BasicBlock stopBlock, stepBlock;
        BasicBlock loopBodyBlock = new BasicBlock(currentFunc, "__loop__for_body");
        BasicBlock loopAfterBlock = new BasicBlock(currentFunc, "__loop__for_after");
        // check condition(stop block) -> loop(body block) -> step(step block) -> check condition -> loop -> ......
        if (node.getStopExpr() != null)
            stopBlock = new BasicBlock(currentFunc, "__loop__for_stop");
        else stopBlock = loopBodyBlock;
        if (node.getStepExpr() != null)
            stepBlock = new BasicBlock(currentFunc, "__loop__for_step");
        else stepBlock = stopBlock;

        stopBlock.setForStateNode(node);
        stepBlock.setForStateNode(node);
        loopBodyBlock.setForStateNode(node);
        loopAfterBlock.setForStateNode(node);
        irRoot.getIRForMap().put(node, new IRFor(stopBlock, stepBlock, loopBodyBlock, loopAfterBlock));

        // for multi level for, backup current loop info and enter next level.
        BasicBlock prevLoopStepBlock = currentLoopStepBlock, prevLoopAfterBlock = currentLoopAfterBlock;
        currentLoopStepBlock = stepBlock;
        currentLoopAfterBlock = loopAfterBlock;
        if (node.getStartExpr() != null)
            node.getStartExpr().accept(this);
        currentBlock.setJumpInst(new Jump(currentBlock, stopBlock));

        // condition check
        if (node.getStopExpr() != null)
        {
            currentBlock = stopBlock;
            // condition true -> continue loop
            node.getStopExpr().setBoolTrueBlock(loopBodyBlock);
            // condition false -> escape loop
            node.getStopExpr().setBoolFalseBlock(loopAfterBlock);
            node.getStopExpr().accept(this);
            // for const bool state, simply use if branch escape the loop
            if (node.getStopExpr() instanceof ConstBoolNode)
                currentBlock.setJumpInst(new Branch(currentBlock, node.getStopExpr().getRegValue(),
                        node.getStopExpr().getBoolTrueBlock(), node.getStopExpr().getBoolFalseBlock()));
        }

        // step process
        if (node.getStepExpr() != null)
        {
            currentBlock = stepBlock;
            node.getStepExpr().accept(this);
            // after step check condition
            currentBlock.setJumpInst(new Jump(currentBlock, stopBlock));
        }

        // loop body process
        currentBlock = loopBodyBlock;
        if (node.getLoopState() != null)
        {
            node.getLoopState().accept(this);
            if (!currentBlock.isContainJump())
                currentBlock.setJumpInst(new Jump(currentBlock, stepBlock));
        }
        else
        {
            if (!currentBlock.isContainJump())
                currentBlock.setJumpInst(new Jump(currentBlock, stepBlock));
        }

        // escape for
        currentLoopStepBlock = prevLoopStepBlock;
        currentLoopAfterBlock = prevLoopAfterBlock;
        currentBlock = loopAfterBlock;
    }

    @Override
    public void visit(IfStateNode node)
    {
        BasicBlock thenBlock = new BasicBlock(currentFunc, "__branch__if_then");
        BasicBlock afterBlock = new BasicBlock(currentFunc, "__branch_if_after");
        BasicBlock elseBlock = null;
        if (node.getElseState() == null)
        {
            // true -> then  false -> escape if
            node.getConditionExpr().setBoolTrueBlock(thenBlock);
            node.getConditionExpr().setBoolFalseBlock(afterBlock);
            node.getConditionExpr().accept(this);
            if (node.getConditionExpr() instanceof ConstBoolNode)
                currentBlock.setJumpInst(new Branch(currentBlock, node.getConditionExpr().getRegValue(),
                        node.getConditionExpr().getBoolTrueBlock(), node.getConditionExpr().getBoolFalseBlock()));
        }
        else
        {
            // true -> then   false -> else
            elseBlock = new BasicBlock(currentFunc, "__branch__if_else");
            node.getConditionExpr().setBoolTrueBlock(thenBlock);
            node.getConditionExpr().setBoolFalseBlock(elseBlock);
            node.getConditionExpr().accept(this);
            if (node.getConditionExpr() instanceof ConstBoolNode)
                currentBlock.setJumpInst(new Branch(currentBlock, node.getConditionExpr().getRegValue(),
                        node.getConditionExpr().getBoolTrueBlock(), node.getConditionExpr().getBoolFalseBlock()));
        }

        currentBlock = thenBlock;
        node.getThenState().accept(this);
        if (!currentBlock.isContainJump())
            currentBlock.setJumpInst(new Jump(currentBlock, afterBlock));
        if (node.getElseState() != null)
        {
            currentBlock = elseBlock;
            node.getElseState().accept(this);
            if (!currentBlock.isContainJump())
                currentBlock.setJumpInst(new Jump(currentBlock, afterBlock));
        }
        currentBlock = afterBlock;
    }

    @Override
    public void visit(ReturnStateNode node)
    {
        Type returnType = currentFunc.getFuncEntity().getRetType();
        // void return
        if (returnType == null || returnType instanceof VoidType)
            currentBlock.setJumpInst(new Return(currentBlock, null));
        else if (!(node.getRetExpr() instanceof ConstBoolNode) && returnType instanceof BoolType)
        {
            // bool return type
            VirtualReg retReg = new VirtualReg("boolReturnReg");
            node.getRetExpr().setBoolFalseBlock(new BasicBlock(currentFunc, null));
            node.getRetExpr().setBoolTrueBlock(new BasicBlock(currentFunc, null));
            node.getRetExpr().accept(this);
            assignProcessor(retReg, node.getRetExpr(), 0, Config.regSize, false);
            currentBlock.setJumpInst(new Return(currentBlock, retReg));
        }
        else
        {
            // normal return type
            node.getRetExpr().accept(this);
            currentBlock.setJumpInst(new Return(currentBlock, node.getRetExpr().getRegValue()));
        }
    }

    @Override
    public void visit(BreakStateNode node)
    {
        currentBlock.setJumpInst(new Jump(currentBlock, currentLoopAfterBlock));
    }

    @Override
    public void visit(ContinueStateNode node)
    {
        currentBlock.setJumpInst(new Jump(currentBlock, currentLoopStepBlock));
    }

    @Override
    public void visit(ExprStateNode node)
    {
        node.getExprState().accept(this);
    }

    @Override
    public void visit(FunctionCallExprNode node)
    {
        FuncEntity funcEntity = node.getFuncEntity();
        String targetFuncName = funcEntity.getIdent();
        ExprNode thisExpr = null;
        List<IRValue> paraList = new ArrayList<IRValue>();
        if (funcEntity.isInClass())
        {
            if (node.getFuncExpr() instanceof MemoryAccessExprNode)
                thisExpr = ((MemoryAccessExprNode) node.getFuncExpr()).getHostExpr();
            else
            {
                if (currentClass != null)
                {
                    thisExpr = new ThisExprNode(null);
                    thisExpr.setType(new ClassType(currentClass));
                }
                else throw new MxError("IR Builder: In FunctionCall node this para is invalid!\n");
            }
            thisExpr.accept(this);
            String hostClassName;
            if (thisExpr.getType() instanceof ClassType)
                hostClassName = ((ClassType) thisExpr.getType()).getClassIdent();
            else if (thisExpr.getType() instanceof ArrayType)
                hostClassName = "__array";
            else if (thisExpr.getType() instanceof StringType)
                hostClassName = "string";
            else throw new MxError("IR Builder: In FunctionCall node thisHostClassName is invalid!\n");
            targetFuncName = genClassFuncName(hostClassName, targetFuncName);
            paraList.add(thisExpr.getRegValue());
        }
        // build in functions
        if (funcEntity.isBuildIn())
            buildInFunctionProcessor(node, targetFuncName, funcEntity, thisExpr);
        else
        {
            for (ExprNode para : node.getParaList())
            {
                para.accept(this);
                paraList.add(para.getRegValue());
            }
            VirtualReg destReg = new VirtualReg(null);
            IRFunction calleeFunc = irRoot.getFunctionMap().get(targetFuncName);
            currentBlock.appendInst(new FuncCall(currentBlock, calleeFunc, destReg, paraList));
            node.setRegValue(destReg);
            if (node.getBoolFalseBlock() != null)
                currentBlock.setJumpInst(new Branch(currentBlock, destReg, node.getBoolTrueBlock(), node.getBoolFalseBlock()));
        }
    }

    @Override
    public void visit(NewExprNode node)
    {
        Type newType = node.getNewType();
        VirtualReg destReg = new VirtualReg(null);
        if (newType instanceof ArrayType)
            arrayNewProcessor(node, null, destReg, 0);
        else if (newType instanceof ClassType)
        {
            String classIdent = ((ClassType) newType).getClassIdent();
            ClassEntity classEntity = globalScope.getClass(classIdent);
            currentBlock.appendInst(new HeapAllocate(currentBlock, destReg, new Immediate(classEntity.getMemSize())));
            String constructFunc = genClassFuncName(classIdent, classIdent);
            IRFunction consFunc = irRoot.getFunctionMap().get(constructFunc);
            if (consFunc != null)
            {
                List<IRValue> consArgs = new ArrayList<IRValue>();
                consArgs.add(destReg);
                currentBlock.appendInst(new FuncCall(currentBlock, consFunc, null, consArgs));
            }
        }
        else
            throw new MxError("IR Builder: in NewExpr newType is not invalid\n");
        node.setRegValue(destReg);
    }

    @Override
    public void visit(MemoryAccessExprNode node)
    {
        boolean prevMemAccessing = memAccessing;
        this.memAccessing = false;
        node.getHostExpr().accept(this);
        this.memAccessing = prevMemAccessing;

        String classIdent = ((ClassType) node.getHostExpr().getType()).getClassIdent();
        IRValue classAddr = node.getHostExpr().getRegValue();
        ClassEntity classEntity = currentScope.getClass(classIdent);
        VarEntity memberVarEntity = classEntity.getClassScope().getSelfVar(node.getMemberStr());

        if (memAccessing)
        {
            node.setAddrValue(classAddr);
            node.setAddrOffset(memberVarEntity.getMemOffset());
        }
        else
        {
            VirtualReg destReg = new VirtualReg(null);
            node.setRegValue(destReg);
            currentBlock.appendInst(new Load(currentBlock, destReg, classAddr, memberVarEntity.getType().getSize(), memberVarEntity.getMemOffset()));
            if (node.getBoolFalseBlock() != null)
                currentBlock.setJumpInst(new Branch(currentBlock, destReg, node.getBoolTrueBlock(), node.getBoolFalseBlock()));
        }
    }

    @Override
    public void visit(ArrayAccessExprNode node)
    {
        boolean prevMemAccessing = memAccessing;
        this.memAccessing = false;
        node.getArrExpr().accept(this);
        if (uselessVar)
            return;
        node.getSubExpr().accept(this);
        this.memAccessing = prevMemAccessing;

        VirtualReg destReg = new VirtualReg(null);
        Immediate elemSize = new Immediate(node.getArrExpr().getType().getSize());
        currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, node.getSubExpr().getRegValue(), IRBinaryOp.MUL, elemSize));
        currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, destReg, IRBinaryOp.ADD, node.getArrExpr().getRegValue()));
        if (memAccessing)
        {
            node.setAddrValue(destReg);
            node.setAddrOffset(Config.regSize);
        }
        else
        {
            node.setRegValue(destReg);
            // WRANING: first 8 bytes are array size, skip it.
            currentBlock.appendInst(new Load(currentBlock, destReg, destReg, node.getArrExpr().getType().getSize(), Config.regSize));
            if (node.getBoolFalseBlock() != null)
                currentBlock.setJumpInst(new Branch(currentBlock, destReg, node.getBoolTrueBlock(), node.getBoolFalseBlock()));
        }
    }

    @Override
    public void visit(AssignExprNode node)
    {
        boolean memAccessingOp = checkMemAccessing(node.getLhs());
        this.memAccessing = memAccessingOp;
        uselessVar = false;
        node.getLhs().accept(this);
        this.memAccessing = false;

        if (uselessVar)
        {
            uselessVar = false;
            return;
        }

        if (node.getRhs().getType() instanceof BoolType && !(node.getRhs() instanceof ConstBoolNode))
        {
            node.getRhs().setBoolTrueBlock(new BasicBlock(currentFunc, null));
            node.getRhs().setBoolFalseBlock(new BasicBlock(currentFunc, null));
        }
        node.getRhs().accept(this);

        int memOffset = 0;
        IRValue destValue;
        if (memAccessingOp)
        {
            memOffset = node.getLhs().getAddrOffset();
            destValue = node.getLhs().getAddrValue();
        }
        else
            destValue = node.getLhs().getRegValue();
        assignProcessor(destValue, node.getRhs(), memOffset, Config.regSize,  memAccessingOp);
        node.setRegValue(node.getRhs().getRegValue());
    }

    @Override
    public void visit(IdentExprNode node)
    {
        VarEntity varEntity = node.getVarEntity();
        if ((varEntity.getType() instanceof ArrayType || varEntity.isInGlobal()) && varEntity.isUnUsed())
        {
            uselessVar = true;
            return;
        }
        if (varEntity.getIrReg() != null)
        {
            node.setRegValue(varEntity.getIrReg());
            if (node.getBoolFalseBlock() != null)
                currentBlock.setJumpInst(new Branch(currentBlock, varEntity.getIrReg(), node.getBoolTrueBlock(), node.getBoolFalseBlock()));
        }
        else
        {
            ThisExprNode thisExprNode = new ThisExprNode(null);
            thisExprNode.setType(new ClassType(currentClass));
            MemoryAccessExprNode memoryAccessExprNode = new MemoryAccessExprNode(null, thisExprNode, node.getIdentName());
            memoryAccessExprNode.accept(this);
            if (memAccessing)
            {
                node.setAddrOffset(memoryAccessExprNode.getAddrOffset());
                node.setAddrValue(memoryAccessExprNode.getAddrValue());
            }
            else
            {
                node.setRegValue(memoryAccessExprNode.getRegValue());
                if (node.getBoolFalseBlock() != null)
                    currentBlock.setJumpInst(new Branch(currentBlock, memoryAccessExprNode.getRegValue(), node.getBoolTrueBlock(), node.getBoolFalseBlock()));
            }
            node.setMemAccessing(true); // It's actually this.identifier
        }
    }

    @Override
    public void visit(PreFixExprNode node)
    {
        VirtualReg destReg;
        switch (node.getPreFixOp())
        {
            case INC:
                selfDecIncProcessor(node, IRBinaryOp.ADD);
                break;
            case DEC:
                selfDecIncProcessor(node, IRBinaryOp.SUB);
                break;
            case NEG:
                destReg = new VirtualReg(null);
                node.setRegValue(destReg);
                node.getPostExpr().accept(this);
                currentBlock.appendInst(new UnaryOperation(currentBlock, IRUnaryOp.NEG, node.getPostExpr().getRegValue(), destReg));
                break;
            case POS:
                node.setRegValue(node.getPostExpr().getRegValue());
                break;
            case LOGIC_NOT:
                node.getPostExpr().setBoolTrueBlock(node.getBoolFalseBlock());
                node.getPostExpr().setBoolFalseBlock(node.getBoolTrueBlock());
                node.getPostExpr().accept(this);
                // Really value change missing?
                break;
            case BITWISE_NOT:
                destReg = new VirtualReg(null);
                node.setRegValue(destReg);
                node.getPostExpr().accept(this);
                currentBlock.appendInst(new UnaryOperation(currentBlock, IRUnaryOp.BITWISE_NOT, node.getPostExpr().getRegValue(), destReg));
                break;
        }
    }

    @Override
    public void visit(PostFixExprNode node)
    {
        switch (node.getPostFixOp())
        {
            case DEC:
                selfDecIncProcessor(node, IRBinaryOp.SUB);
                break;
            case INC:
                selfDecIncProcessor(node, IRBinaryOp.ADD);
                break;
        }
    }

    @Override
    public void visit(BinaryExprNode node)
    {
        switch (node.getBop())
        {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case SHL:
            case SHR:
            case BITWISE_AND:
            case BITWISE_OR:
            case BITWISE_XOR:
                binaryArithStringProcessor(node);
                break;
            case EQUAL:
            case NEQUAL:
            case LESS:
            case LESS_EQUAL:
            case GREATER:
            case GREATER_EQUAL:
                binaryCompareStringProcessor(node);
                break;
            case LOGIC_OR:
            case LOGIC_AND:
                binaryLogicProcessor(node);
        }
    }

    @Override
    public void visit(ThisExprNode node)
    {
        VarEntity thisEntity = currentScope.getVar("this");
        node.setRegValue(thisEntity.getIrReg());
        if (node.getBoolFalseBlock() != null)
            currentBlock.setJumpInst(new Branch(currentBlock, thisEntity.getIrReg(), node.getBoolTrueBlock(), node.getBoolFalseBlock()));
    }

    @Override
    public void visit(ConstIntNode node)
    {
        node.setRegValue(new Immediate(node.getValue()));
    }

    @Override
    public void visit(ConstBoolNode node)
    {
        if (node.getValue())
            node.setRegValue(new Immediate(1));
        else
            node.setRegValue(new Immediate(0));
    }

    @Override
    public void visit(ConstNullNode node)
    {
        node.setRegValue(new Immediate(0));
    }

    @Override
    public void visit(ConstStringNode node)
    {
        StaticStr constString = irRoot.getStaticStrMap().get(node.getValue());
        if (constString != null)
            node.setRegValue(constString);
        else
        {
            constString = new StaticStr(node.getValue(), Config.regSize);
            irRoot.getStaticStrMap().put(node.getValue(), constString);
            node.setRegValue(constString);
        }
    }

    @Override
    public void visit(TypeNode node)
    {
    }

    public IRROOT getIrRoot()
    {
        return irRoot;
    }

    public static String genClassFuncName(String className, String funcName)
    {
        return "__class__" + className + "__" + funcName;
    }

    private FuncDeclNode globalVarInitFunc()
    {
        String globalInitName = "__globalVarInit";
        List<Node> varInitStateList = new ArrayList<Node>();
        for (GlobalVarInit globalVarInit : globalVarInitList)
        {
            IdentExprNode lhs = new IdentExprNode(null, globalVarInit.getVarName());
            VarEntity varEntity = globalScope.getVar(globalVarInit.getVarName());
            lhs.setVarEntity(varEntity);
            varInitStateList.add(new ExprStateNode(null, new AssignExprNode(null, lhs, globalVarInit.getInitValue())));
        }

        FuncBlockNode globalInitBlock = new FuncBlockNode(null, varInitStateList);
        globalInitBlock.setScope(new Scope(globalScope));
        FuncDeclNode globalVarInitFunc = new FuncDeclNode(null, globalInitName,
                new TypeNode(null, voidType), new ArrayList<VarDeclNode>(), globalInitBlock);
        FuncEntity funcEntity = new FuncEntity(globalVarInitFunc, globalInitBlock.getScope());
        globalScope.insertFunc(funcEntity);
        IRFunction irGlobalInitFunc = new IRFunction(funcEntity);
        irRoot.addFunction(irGlobalInitFunc);
        return globalVarInitFunc;
    }

    private void updateRecursiveCalleeSet()
    {
        Set<IRFunction> recursiveCalleeSet = new HashSet<IRFunction>();
        boolean flag = true;
        for (IRFunction irFunction : irRoot.getFunctionMap().values())
            irFunction.recurCalleeSet.clear();
        while (flag)
        {
            flag = false;
            for (IRFunction irFunction : irRoot.getFunctionMap().values())
            {
                recursiveCalleeSet.clear();
                recursiveCalleeSet.addAll(irFunction.calleeSet);
                for (IRFunction calleeFunc : irFunction.calleeSet)
                    recursiveCalleeSet.addAll(calleeFunc.recurCalleeSet);
                if (!(irFunction.recurCalleeSet.equals(recursiveCalleeSet)))
                {
                    irFunction.recurCalleeSet.clear();
                    irFunction.recurCalleeSet.addAll(recursiveCalleeSet);
                    flag = true;
                }
            }
        }
    }

    private void assignProcessor(IRValue dest, ExprNode src, int offset, int size, boolean accessMem)
    {
        if (src.getBoolTrueBlock() == null)
        {
            if (accessMem)
                currentBlock.appendInst(new Store(currentBlock, src.getRegValue(), dest, size, offset));
            else
                currentBlock.appendInst(new Move(currentBlock, (IRReg) dest, src.getRegValue()));
        }
        else
        {
            BasicBlock mergeBlock = new BasicBlock(currentFunc, "merge_block");
            if (accessMem)
            {
                src.getBoolFalseBlock().appendInst(new Store(src.getBoolFalseBlock(), new Immediate(0),
                        dest, Config.regSize, offset));
                src.getBoolTrueBlock().appendInst(new Store(src.getBoolTrueBlock(), new Immediate(1),
                        dest, Config.regSize, offset));
            }
            else
            {
                src.getBoolFalseBlock().appendInst(new Move(src.getBoolFalseBlock(), (VirtualReg) dest, new Immediate(0)));
                src.getBoolTrueBlock().appendInst(new Move(src.getBoolTrueBlock(), (VirtualReg) dest, new Immediate(1)));
            }
            if (!src.getBoolFalseBlock().isContainJump())
                src.getBoolFalseBlock().setJumpInst(new Jump(src.getBoolFalseBlock(), mergeBlock));
            if (!src.getBoolTrueBlock().isContainJump())
                src.getBoolTrueBlock().setJumpInst(new Jump(src.getBoolTrueBlock(), mergeBlock));
            currentBlock = mergeBlock;
        }
    }

    private void buildInFunctionProcessor(FunctionCallExprNode callExprNode, String targetFuncName, FuncEntity funcEntity, ExprNode thisExpr)
    {
        IRFunction calleeFunc;
        List<IRValue> paras = new ArrayList<IRValue>();
        ExprNode para0, para1;
        VirtualReg destVReg;
        boolean prevMemAccessing = this.memAccessing;
        switch (targetFuncName)
        {
            case IRROOT.buildInPrint:
            case IRROOT.buildInPrintln:
                para0 = callExprNode.getParaList().get(0);
                printfProcessor(targetFuncName, para0);
                break;

            case IRROOT.buildInGetString:
                destVReg = new VirtualReg("getStringBuffer");
                calleeFunc = irRoot.getBuildInFuncMap().get(targetFuncName);
                currentBlock.appendInst(new FuncCall(currentBlock, calleeFunc, destVReg, paras));
                callExprNode.setRegValue(destVReg);
                break;

            case IRROOT.buildInGetInt:
                destVReg = new VirtualReg("getIntBuffer");
                calleeFunc = irRoot.getBuildInFuncMap().get(targetFuncName);
                currentBlock.appendInst(new FuncCall(currentBlock, calleeFunc, destVReg, paras));
                callExprNode.setRegValue(destVReg);
                break;

            case IRROOT.buildInToString:
                destVReg = new VirtualReg("toStringRes");
                calleeFunc = irRoot.getBuildInFuncMap().get(targetFuncName);
                para0 = callExprNode.getParaList().get(0);
                para0.accept(this);
                paras.add(para0.getRegValue());
                currentBlock.appendInst(new FuncCall(currentBlock, calleeFunc, destVReg, paras));
                callExprNode.setRegValue(destVReg);
                break;

            case IRROOT.buildInClassStringLength:
                destVReg = new VirtualReg("stringLengthRes");
                currentBlock.appendInst(new Load(currentBlock, destVReg, thisExpr.getRegValue(), Config.regSize, 0));
                callExprNode.setRegValue(destVReg);
                break;

            case IRROOT.buildInClassStringSubString:
                destVReg = new VirtualReg("subStringRes");
                para0 = callExprNode.getParaList().get(0);
                para1 = callExprNode.getParaList().get(1);
                para0.accept(this);
                para1.accept(this);
                paras.add(thisExpr.getRegValue());
                paras.add(para0.getRegValue());
                paras.add(para1.getRegValue());
                calleeFunc = irRoot.getBuildInFuncMap().get(targetFuncName);
                currentBlock.appendInst(new FuncCall(currentBlock, calleeFunc, destVReg, paras));
                callExprNode.setRegValue(destVReg);
                break;

            case IRROOT.buildInClassStringParseInt:
                destVReg = new VirtualReg("paserIntRes");
                paras.add(thisExpr.getRegValue());
                calleeFunc = irRoot.getBuildInFuncMap().get(targetFuncName);
                currentBlock.appendInst(new FuncCall(currentBlock, calleeFunc, destVReg, paras));
                callExprNode.setRegValue(destVReg);
                break;

            case IRROOT.buildInClassStringOrd:
                destVReg = new VirtualReg("ordRes");
                para0 = callExprNode.getParaList().get(0);
                para0.accept(this);
                paras.add(thisExpr.getRegValue());
                paras.add(para0.getRegValue());
                calleeFunc = irRoot.getBuildInFuncMap().get(targetFuncName);
                currentBlock.appendInst(new FuncCall(currentBlock, calleeFunc, destVReg, paras));
                callExprNode.setRegValue(destVReg);
                break;

            case IRROOT.buildInClassArraySize:
                destVReg = new VirtualReg("arraySizeRes");
                currentBlock.appendInst(new Load(currentBlock, destVReg, thisExpr.getRegValue(), Config.regSize, 0));
                callExprNode.setRegValue(destVReg);
                break;
        }
        this.memAccessing = prevMemAccessing;
    }

    private void printfProcessor(String funcName, ExprNode printValue)
    {
        if (!(printValue.getType() instanceof StringType))
            throw new MxError("IR Builder: printProcessor value is not String!\n");
        // print (a + b) / println(a + b)
        if (printValue instanceof BinaryExprNode)
        {
            printfProcessor("print", ((BinaryExprNode) printValue).getLhs());
            printfProcessor(funcName, ((BinaryExprNode) printValue).getRhs());
        }
        else
        {
            List<IRValue> paras = new ArrayList<IRValue>();
            IRFunction calleeFunc;
            // print(toString(i)) -> printInt(i)
            if (printValue instanceof FunctionCallExprNode && ((FunctionCallExprNode) printValue).getFuncEntity().getIdent() == "toString")
            {
                ExprNode intValue = ((FunctionCallExprNode) printValue).getParaList().get(0);
                intValue.accept(this);
                paras.add(intValue.getRegValue());
                calleeFunc = irRoot.getBuildInFuncMap().get(funcName + "ForInt");
            }
            else
            {
                printValue.accept(this);
                paras.add(printValue.getRegValue());
                calleeFunc = irRoot.getBuildInFuncMap().get(funcName);
            }
            currentBlock.appendInst(new FuncCall(currentBlock, calleeFunc, null, paras));
        }
    }

    private boolean checkIdentMemAccess(IdentExprNode node)
    {
        if (node.hasMemAccessChecked())
            return node.isMemAccessing();
        else
        {
            if (currentClass != null)
            {
                VarEntity varEntity = currentScope.getVar(node.getIdentName());
                node.setMemAccessing(varEntity.getIrReg() == null);
            }
            else
                node.setMemAccessing(false);
            node.setMemAccessChecked(true);
            return node.isMemAccessing();
        }
    }

    private boolean checkMemAccessing(ExprNode node)
    {
        return node instanceof ArrayAccessExprNode || node instanceof MemoryAccessExprNode
                || (node instanceof IdentExprNode && checkIdentMemAccess((IdentExprNode) node));
    }

    private void arrayNewProcessor(NewExprNode newExprNode, IRValue addr, VirtualReg prevDestReg, int index)
    {
        ExprNode nowDim = newExprNode.getKnownDims().get(index);
        VirtualReg destReg = new VirtualReg(null);
        boolean prevMemAccessing = memAccessing;
        this.memAccessing = false;
        nowDim.accept(this);
        this.memAccessing = prevMemAccessing;
        // calculate the memory size
        currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, nowDim.getRegValue(), IRBinaryOp.MUL, new Immediate(newExprNode.getNewType().getSize())));
        currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, destReg, IRBinaryOp.ADD, new Immediate(newExprNode.getNewType().getSize())));
        // allocate memory
        currentBlock.appendInst(new HeapAllocate(currentBlock, destReg, destReg));
        // save each dim size to memory
        currentBlock.appendInst(new Store(currentBlock, nowDim.getRegValue(), destReg, Config.regSize, 0));

        // for each dims, allocate memory using a loop
        if (index < newExprNode.getKnownDims().size() - 1)
        {
            VirtualReg nowAddr = new VirtualReg(null);
            VirtualReg loopIndex = new VirtualReg(null);
            currentBlock.appendInst(new Move(currentBlock, loopIndex, new Immediate(0)));
            currentBlock.appendInst(new Move(currentBlock, nowAddr, destReg));

            // construct the loop
            BasicBlock bodyBlock = new BasicBlock(currentFunc, "new_loop_body");
            BasicBlock afterBlock = new BasicBlock(currentFunc, "new_loop_after");
            BasicBlock condBlock = new BasicBlock(currentFunc, "new_loop_cond");

            // check cond
            currentBlock.setJumpInst(new Jump(currentBlock, condBlock));
            currentBlock = condBlock;
            VirtualReg condReg = new VirtualReg(null);
            currentBlock.appendInst(new Comparison(currentBlock, ComparisonOp.L, condReg, loopIndex, nowDim.getRegValue()));
            currentBlock.setJumpInst(new Branch(currentBlock, condReg, bodyBlock, afterBlock));

            // construct body
            currentBlock = bodyBlock;
            // move nowAddr to the beginning of next dim
            currentBlock.appendInst(new BinaryOperation(currentBlock, nowAddr, nowAddr, IRBinaryOp.ADD, new Immediate(newExprNode.getNewType().getSize())));
            // use recursive to allocate next dim memory
            arrayNewProcessor(newExprNode, nowAddr, null, index + 1);
            currentBlock.appendInst(new BinaryOperation(currentBlock, loopIndex, loopIndex, IRBinaryOp.ADD, new Immediate(1)));

            // Jump to check cond
            currentBlock.setJumpInst(new Jump(currentBlock, condBlock));
            // escape the allocate loop
            currentBlock = afterBlock;
        }
        if (index != 0)
            currentBlock.appendInst(new Store(currentBlock, destReg, addr, Config.regSize, 0));
        else
            currentBlock.appendInst(new Move(currentBlock, prevDestReg, destReg));

    }

    private void selfDecIncProcessor(ExprNode exprNode, IRBinaryOp selfOp)
    {
        ExprNode expr;
        if (exprNode instanceof PostFixExprNode)
            expr = ((PostFixExprNode) exprNode).getPreExpr();
        else if (exprNode instanceof PreFixExprNode)
            expr = ((PreFixExprNode) exprNode).getPostExpr();
        else
            throw new MxError("IR Builder: selfDecIncProcessor get wrong exprNode\n");

        boolean prevmemAccessing = this.memAccessing;
        boolean currMemAccesssing = checkMemAccessing(expr);
        this.memAccessing = false;
        expr.accept(this);

        if (exprNode instanceof PreFixExprNode)
            exprNode.setRegValue(expr.getRegValue());
        else
        {
            VirtualReg destReg = new VirtualReg("");
            currentBlock.appendInst(new Move(currentBlock, destReg, expr.getRegValue()));
            exprNode.setRegValue(destReg);
        }

        Immediate tmpNum1 = new Immediate(1);
        if (currMemAccesssing)
        {
            VirtualReg destReg = new VirtualReg("");
            // need revisit expr to get the address of the expr
            this.memAccessing = true;
            expr.accept(this);
            // for prefix expr
            currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, expr.getRegValue(), selfOp, tmpNum1));
            currentBlock.appendInst(new Store(currentBlock, destReg, expr.getAddrValue(), Config.regSize, expr.getAddrOffset()));
            // for postfix expr, correct it's regValue
            if (exprNode instanceof PostFixExprNode)
                expr.setRegValue(destReg);
        }
        else
            currentBlock.appendInst(new BinaryOperation(currentBlock, (IRReg)expr.getRegValue(), expr.getRegValue(), selfOp, tmpNum1));
        this.memAccessing = prevmemAccessing;
    }

    private void binaryArithStringProcessor(BinaryExprNode exprNode)
    {
        if (exprNode.getLhs().getType() instanceof StringType)
            binaryStringProcessor(exprNode);
        else
            binaryArithProcessor(exprNode);
    }

    private void binaryCompareStringProcessor(BinaryExprNode exprNode)
    {
        if (exprNode.getLhs().getType() instanceof StringType)
            binaryStringProcessor(exprNode);
        else
            binaryCompareProcessor(exprNode);
    }

    private void binaryArithProcessor(BinaryExprNode exprNode)
    {
        // visit lhs & rhs
        exprNode.getLhs().accept(this);
        exprNode.getRhs().accept(this);
        IRValue lhsValue = exprNode.getLhs().getRegValue();
        IRValue rhsValue = exprNode.getRhs().getRegValue();

        int immLhs = 0, immRhs = 0;
        if (lhsValue instanceof Immediate)
            immLhs = ((Immediate) lhsValue).getValue();
        if (rhsValue instanceof Immediate)
            immRhs = ((Immediate) rhsValue).getValue();
        // For const folding
        boolean constRes = (lhsValue instanceof Immediate) && (rhsValue instanceof Immediate);
        IRBinaryOp irBop = null;
        switch (exprNode.getBop())
        {
            case ADD:
                irBop = IRBinaryOp.ADD;
                if (constRes)
                {
                    exprNode.setRegValue(new Immediate(immLhs + immRhs));
                    return;
                }
                break;
            case SUB:
                irBop = IRBinaryOp.SUB;
                if (constRes)
                {
                    exprNode.setRegValue(new Immediate(immLhs - immRhs));
                    return;
                }
                break;
            case MUL:
                irBop = IRBinaryOp.MUL;
                if (constRes)
                {
                    exprNode.setRegValue(new Immediate(immLhs * immRhs));
                    return;
                }
                break;
            case DIV:
                irBop = IRBinaryOp.DIV;
                if (constRes)
                {
                    if (immRhs == 0)
                        throw new MxError(exprNode.getLocation(), "Div by 0 is invalid!\n");
                    exprNode.setRegValue(new Immediate(immLhs / immRhs));
                    return;
                }
                irRoot.setContainShiftDiv(true);
                break;
            case MOD:
                irBop = IRBinaryOp.MOD;
                if (constRes)
                {
                    if (immRhs == 0)
                        throw new MxError(exprNode.getLocation(), "Mod by 0 is invalid!\n");
                    exprNode.setRegValue(new Immediate(immLhs % immRhs));
                    return;
                }
                irRoot.setContainShiftDiv(true);
                break;
            case SHL:
                irBop = IRBinaryOp.SHL;
                if (constRes)
                {
                    exprNode.setRegValue(new Immediate(immLhs << immRhs));
                    return;
                }
                irRoot.setContainShiftDiv(true);
                break;
            case SHR:
                irBop = IRBinaryOp.SHR;
                if (constRes)
                {
                    exprNode.setRegValue(new Immediate(immLhs >> immRhs));
                    return;
                }
                irRoot.setContainShiftDiv(true);
                break;
            case BITWISE_AND:
                irBop = IRBinaryOp.BITWISE_AND;
                if (constRes)
                {
                    exprNode.setRegValue(new Immediate(immLhs & immRhs));
                    return;
                }
                break;
            case BITWISE_OR:
                irBop = IRBinaryOp.BITWISE_OR;
                if (constRes)
                {
                    exprNode.setRegValue(new Immediate(immLhs | immRhs));
                    return;
                }
                break;
            case BITWISE_XOR:
                irBop = IRBinaryOp.BITWISE_XOR;
                if (constRes)
                {
                    exprNode.setRegValue(new Immediate(immLhs ^ immRhs));
                    return;
                }
                break;
            default:
                throw new MxError("IR Builder: binaryArithProcessor Op is invalid\n");
        }

        VirtualReg destReg = new VirtualReg(null);
        if (rhsValue instanceof Immediate)
        {
            if (immRhs == 1)
            {
                if (irBop == IRBinaryOp.MOD)
                {
                    currentBlock.appendInst(new Move(currentBlock, destReg, new Immediate(0)));
                    exprNode.setRegValue(destReg);
                    return;
                }
                if (irBop == IRBinaryOp.DIV || irBop == IRBinaryOp.MUL)
                {
                    currentBlock.appendInst(new Move(currentBlock, destReg, lhsValue));
                    exprNode.setRegValue(destReg);
                    return;
                }
            }

            // for immRhs = 2^n
            if (((immRhs & (immRhs - 1)) == 0))
            {
                int cnt = 0;
                long val = (long) immRhs;
                while (val > 1)
                {
                    cnt++;
                    val = val >> 1;
                }
                if (irBop == IRBinaryOp.MOD)
                {
                    currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, lhsValue, IRBinaryOp.BITWISE_AND, new Immediate(immRhs - 1)));
                    exprNode.setRegValue(destReg);
                    return;
                }
                if (irBop == IRBinaryOp.MUL)
                {
                    currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, lhsValue, IRBinaryOp.SHL, new Immediate(cnt)));
                    exprNode.setRegValue(destReg);
                    return;
                }
                if (irBop == IRBinaryOp.DIV)
                {
                    currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, lhsValue, IRBinaryOp.SHR, new Immediate(cnt)));
                    exprNode.setRegValue(destReg);
                    return;
                }
            }

            if ((irBop == IRBinaryOp.MOD || irBop == IRBinaryOp.DIV) && immRhs > 10)
            {
                int cnt = 0;
                long val = (long) immRhs;
                while (val % 2 == 0)
                {
                    cnt++;
                    val = val / 2;
                }

                if (val != 1)
                {
                    // may be wrong
                    long mod = 1L << 32;
                    int o = (int) ((mod - 1) / val + 1);
                    currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, lhsValue, IRBinaryOp.SHR, new Immediate(cnt)));
                    currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, destReg, IRBinaryOp.MUL, new Immediate(o)));
                    currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, destReg, IRBinaryOp.SHR, new Immediate(32)));
                    if (irBop == IRBinaryOp.MOD)
                    {
                        currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, destReg, IRBinaryOp.MUL, rhsValue));
                        currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, lhsValue, IRBinaryOp.SUB, destReg));
                    }
                    exprNode.setRegValue(destReg);
                    return;
                }
            }

        }

        currentBlock.appendInst(new BinaryOperation(currentBlock, destReg, lhsValue, irBop, rhsValue));
        exprNode.setRegValue(destReg);
    }

    private void binaryStringProcessor(BinaryExprNode exprNode)
    {
        if (exprNode.getLhs().getType() instanceof StringType)
        {
            exprNode.getLhs().accept(this);
            exprNode.getRhs().accept(this);
            ExprNode tmp = null;
            IRFunction stringProcessFunc = null;
            switch (exprNode.getBop())
            {
                case ADD:
                    stringProcessFunc = irRoot.getBuildInFuncMap().get(IRROOT.buildInStringConcat);
                    break;
                case EQUAL:
                    stringProcessFunc = irRoot.getBuildInFuncMap().get(IRROOT.buildInStringEqual);
                    break;
                case NEQUAL:
                    stringProcessFunc = irRoot.getBuildInFuncMap().get(IRROOT.buildInStringNequal);
                    break;
                case LESS:
                    stringProcessFunc = irRoot.getBuildInFuncMap().get(IRROOT.buildInStringLess);
                    break;
                case GREATER:
                    tmp = exprNode.getLhs();
                    exprNode.setLhs(exprNode.getRhs());
                    exprNode.setBop(Operators.BinaryOp.LESS);
                    exprNode.setRhs(tmp);
                    stringProcessFunc = irRoot.getBuildInFuncMap().get(IRROOT.buildInStringLess);
                    break;
                case LESS_EQUAL:
                    stringProcessFunc = irRoot.getBuildInFuncMap().get(IRROOT.buildInStringLessEqual);
                    break;
                case GREATER_EQUAL:
                    tmp = exprNode.getLhs();
                    exprNode.setLhs(exprNode.getRhs());
                    exprNode.setBop(Operators.BinaryOp.LESS_EQUAL);
                    exprNode.setRhs(tmp);
                    stringProcessFunc = irRoot.getBuildInFuncMap().get(IRROOT.buildInStringLessEqual);
                    break;
                default:
                    throw new MxError("IR Builder: binary String Operation node op is invalid\n");
            }

            VirtualReg destReg = new VirtualReg(null);
            List<IRValue> funcArgs = new ArrayList<IRValue>();
            funcArgs.add(exprNode.getLhs().getRegValue());
            funcArgs.add(exprNode.getRhs().getRegValue());
            currentBlock.appendInst(new FuncCall(currentBlock, stringProcessFunc, destReg, funcArgs));

            if (exprNode.getBoolFalseBlock() != null)
                currentBlock.setJumpInst(new Branch(currentBlock, destReg, exprNode.getBoolTrueBlock(), exprNode.getBoolFalseBlock()));
            else
                exprNode.setRegValue(destReg);
        }
        else
            throw new MxError("IR Builder: binary String Operation node type invalid\n");
    }

    private void binaryCompareProcessor(BinaryExprNode exprNode)
    {
        exprNode.getLhs().accept(this);
        exprNode.getRhs().accept(this);
        IRValue lhsValue = exprNode.getLhs().getRegValue();
        IRValue rhsValue = exprNode.getRhs().getRegValue();
        IRValue tmp;
        int immLhs = 0, immRhs = 0;
        if (lhsValue instanceof Immediate)
            immLhs = ((Immediate) lhsValue).getValue();
        if (rhsValue instanceof Immediate)
            immRhs = ((Immediate) rhsValue).getValue();
        boolean constRes = (lhsValue instanceof Immediate) && (rhsValue instanceof Immediate);
        ComparisonOp cop = null;
        switch (exprNode.getBop())
        {
            case EQUAL:
                cop = ComparisonOp.E;
                if (constRes)
                {
                    if (immLhs == immRhs)
                        exprNode.setRegValue(new Immediate(1));
                    else
                        exprNode.setRegValue(new Immediate(0));
                    return;
                }
                // Extra Optim, WARNING might be wrong!
                else if (lhsValue instanceof Immediate)
                {
                    tmp = rhsValue;
                    rhsValue = lhsValue;
                    lhsValue  = tmp;
                }
                break;
            case NEQUAL:
                cop = ComparisonOp.NE;
                if (constRes)
                {
                    if (immLhs != immRhs)
                        exprNode.setRegValue(new Immediate(1));
                    else
                        exprNode.setRegValue(new Immediate(0));
                    return;
                }
                else if (lhsValue instanceof Immediate)
                {
                    tmp = rhsValue;
                    rhsValue = lhsValue;
                    lhsValue  = tmp;
                }
                break;
            case LESS:
                cop = ComparisonOp.L;
                if (constRes)
                {
                    if (immLhs < immRhs)
                        exprNode.setRegValue(new Immediate(1));
                    else
                        exprNode.setRegValue(new Immediate(0));
                    return;
                }
                else if (lhsValue instanceof Immediate)
                {
                    tmp = rhsValue;
                    rhsValue = lhsValue;
                    lhsValue  = tmp;
                    cop = ComparisonOp.G;
                }
                break;
            case GREATER:
                cop = ComparisonOp.G;
                if (constRes)
                {
                    if (immLhs > immRhs)
                        exprNode.setRegValue(new Immediate(1));
                    else
                        exprNode.setRegValue(new Immediate(0));
                    return;
                }
                else if (lhsValue instanceof Immediate)
                {
                    tmp = rhsValue;
                    rhsValue = lhsValue;
                    lhsValue  = tmp;
                    cop = ComparisonOp.L;
                }
                break;
            case LESS_EQUAL:
                cop = ComparisonOp.LE;
                if (constRes)
                {
                    if (immLhs <= immRhs)
                        exprNode.setRegValue(new Immediate(1));
                    else
                        exprNode.setRegValue(new Immediate(0));
                    return;
                }
                else if (lhsValue instanceof Immediate)
                {
                    tmp = rhsValue;
                    rhsValue = lhsValue;
                    lhsValue  = tmp;
                    cop = ComparisonOp.GE;
                }
                break;
            case GREATER_EQUAL:
                cop = ComparisonOp.GE;
                if (constRes)
                {
                    if (immLhs >= immRhs)
                        exprNode.setRegValue(new Immediate(1));
                    else
                        exprNode.setRegValue(new Immediate(0));
                    return;
                }
                else if (lhsValue instanceof Immediate)
                {
                    tmp = rhsValue;
                    rhsValue = lhsValue;
                    lhsValue  = tmp;
                    cop = ComparisonOp.LE;
                }
                break;
        }
        VirtualReg destReg = new VirtualReg(null);
        currentBlock.appendInst(new Comparison(currentBlock, cop, destReg, lhsValue, rhsValue));
        if (exprNode.getBoolFalseBlock() != null)
            currentBlock.setJumpInst(new Branch(currentBlock, destReg, exprNode.getBoolTrueBlock(), exprNode.getBoolFalseBlock()));
        else
            exprNode.setRegValue(destReg);
    }

    private void binaryLogicProcessor(BinaryExprNode exprNode)
    {
        // short circuit optim
        if (exprNode.getBop() == Operators.BinaryOp.LOGIC_OR)
        {
            exprNode.getLhs().setBoolTrueBlock(exprNode.getBoolTrueBlock());
            exprNode.getLhs().setBoolFalseBlock(new BasicBlock(currentFunc, "logic_or_lhs_false"));
            exprNode.getLhs().accept(this);
            currentBlock = exprNode.getLhs().getBoolFalseBlock();
        }
        else if (exprNode.getBop() == Operators.BinaryOp.LOGIC_AND)
        {
            exprNode.getLhs().setBoolFalseBlock(exprNode.getBoolFalseBlock());
            exprNode.getLhs().setBoolTrueBlock(new BasicBlock(currentFunc, "logic_and_lhs_true"));
            exprNode.getLhs().accept(this);
            currentBlock = exprNode.getLhs().getBoolTrueBlock();
        }
        else
            throw new MxError("IR Builder: binary Logic node bop invalid\n");
        exprNode.getRhs().setBoolTrueBlock(exprNode.getBoolTrueBlock());
        exprNode.getRhs().setBoolFalseBlock(exprNode.getBoolFalseBlock());
        exprNode.getRhs().accept(this);
    }


}
