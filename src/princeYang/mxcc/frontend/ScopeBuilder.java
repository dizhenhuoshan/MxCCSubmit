package princeYang.mxcc.frontend;

import princeYang.mxcc.ast.*;
import princeYang.mxcc.errors.MxError;
import princeYang.mxcc.scope.*;

public class ScopeBuilder extends ScopeScanner
{
    private int loopLevel;
    private Scope globalScope, currentScope;
    private FuncEntity currentFuncCallEntity;
    private Type currentReturnType;

    public ScopeBuilder(Scope globalScope)
    {
        this.globalScope = globalScope;
        this.currentScope = globalScope;
        this.loopLevel = 0;
    }

    private void varInitCheck(VarDeclNode varDeclNode)
    {
        varDeclNode.getInitValue().accept(this);
        if (varDeclNode.getVarType().getType() instanceof VoidType || varDeclNode.getInitValue().getType() instanceof VoidType)
            throw new MxError(varDeclNode.getLocation(), "void type shouldn't be in var declaration!\n");
        if (varDeclNode.getVarType().getType().equals(varDeclNode.getInitValue().getType()))
            return;
        if (varDeclNode.getInitValue().getType() instanceof NullType)
        {
            if (!(varDeclNode.getVarType().getType() instanceof ClassType || varDeclNode.getVarType().getType() instanceof ArrayType))
                throw new MxError(varDeclNode.getLocation(), "null type shouldn't be init value here!\n");
        }
        else
            throw new MxError(varDeclNode.getLocation(), "Other error appear in varDecl!\n");
    }

    @Override
    public void visit(MxProgNode mxProgNode)
    {
        for (DeclNode declNode : mxProgNode.getDeclNodesList())
        {
            if (declNode instanceof VarDeclNode || declNode instanceof FuncDeclNode || declNode instanceof ClassDeclNode)
                declNode.accept(this);
            else
                throw new MxError(declNode.getLocation(), "declNode gets fucking wrong!\n");
        }
    }

    @Override
    public void visit(ClassDeclNode classDeclNode)
    {
        ClassEntity classEntity = (ClassEntity) currentScope.getClass(classDeclNode.getIdentName());
        if (classEntity == null)
            throw new MxError(classDeclNode.getLocation(), "class declNode entity boomed!\n");
        currentScope = classEntity.getClassScope();
        for (FuncDeclNode funcDeclNode : classDeclNode.getFuncDeclList())
            funcDeclNode.accept(this);
        for (VarDeclNode varDeclNode : classDeclNode.getVarDeclList())
        {
            if (varDeclNode.getInitValue() != null)
                varInitCheck(varDeclNode);
        }
        currentScope = currentScope.getFather();
        if (currentScope != globalScope)
            throw new MxError(classDeclNode.getLocation(), "Scope: class scope parent NOT GLOBAL\n");
    }

    @Override
    public void visit(FuncDeclNode funcDeclNode)
    {
        FuncEntity funcEntity = (FuncEntity) currentScope.getFunc(funcDeclNode.getIdentName());
        if (funcEntity == null)
            throw new MxError(funcDeclNode.getLocation(), "function declNode Entity boomed!\n");
        if (funcEntity.getRetType() instanceof ClassType)
        {
            if (currentScope.getClass(((ClassType) (funcEntity.getRetType())).getClassIdent()) == null)
                throw new MxError(funcDeclNode.getLocation(), "function return type undefined!\n");
        }
        currentScope = funcEntity.getFuncScope();
        currentReturnType = funcEntity.getRetType();
        for (VarDeclNode varDeclNode : funcDeclNode.getParaDeclList())
        {
            if (varDeclNode.getInitValue() != null)
                varInitCheck(varDeclNode);
        }
        funcDeclNode.getFuncBlock().accept(this);
        currentScope = currentScope.getFather();
    }

    @Override
    public void visit(VarDeclNode varDeclNode)
    {
        if (varDeclNode.getVarType().getType() instanceof ClassType)
        {
            if (currentScope.getClass(((ClassType)(varDeclNode.getVarType().getType())).getClassIdent()) == null)
                throw new MxError(varDeclNode.getLocation(), String.format("Scope: class %s is not defined\n",
                        ((ClassType)varDeclNode.getVarType().getType()).getClassIdent()));
        }
        if (varDeclNode.getInitValue() != null)
            varInitCheck(varDeclNode);
        VarEntity entity = new VarEntity(varDeclNode);
        if (currentScope.isGlobalScope())
            entity.setInGlobal(true);
        currentScope.insertVar(entity);
    }

    @Override
    public void visit(ExprStateNode exprStateNode)
    {
        exprStateNode.getExprState().accept(this);
    }

    @Override
    public void visit(ForStateNode forStateNode)
    {
        loopLevel++;
        if (forStateNode.getStartExpr() != null)
            forStateNode.getStartExpr().accept(this);
        if (forStateNode.getStopExpr() != null)
        {
            forStateNode.getStopExpr().accept(this);
            if (!(forStateNode.getStopExpr().getType() instanceof BoolType))
                throw new MxError(forStateNode.getLocation(), "stop condition type should be bool!\n");
        }
        if (forStateNode.getStepExpr() != null)
            forStateNode.getStepExpr().accept(this);
        if (forStateNode.getLoopState() != null)
        {
            forStateNode.getLoopState().accept(this);
        }
        loopLevel--;
    }

    @Override
    public void visit(WhileStateNode whileStateNode)
    {
        loopLevel++;
        if (whileStateNode.getConditionExpr() != null)
            whileStateNode.getConditionExpr().accept(this);
        else
            throw new MxError(whileStateNode.getLocation(), "condition expr is null!\n");
        if (whileStateNode.getLoopState() != null)
        {
            whileStateNode.getLoopState().accept(this);
        }
        else
            throw new MxError(whileStateNode.getLocation(), "while LoopStatement is empty!\n");
        loopLevel--;
    }

    @Override
    public void visit(ReturnStateNode returnStateNode)
    {
        if (returnStateNode.getRetExpr() != null)
        {
            returnStateNode.getRetExpr().accept(this);
            Type retType = returnStateNode.getRetExpr().getType();
            if (retType instanceof VoidType || retType == null)
                throw new MxError(returnStateNode.getLocation(), "shouldn't return void !\n");
            if (retType instanceof NullType)
            {
                if (!(currentReturnType instanceof ClassType || currentReturnType instanceof ArrayType))
                    throw new MxError(returnStateNode.getLocation(), "shouldn't return null !\n");
            }
            else if (!retType.equals(currentReturnType))
                throw new MxError(returnStateNode.getLocation(), "return type ERROR !\n");
        }
        else
        {
            if (!(currentReturnType instanceof VoidType || currentReturnType == null))
                throw new MxError(returnStateNode.getLocation(), "shouldn't return void! \n");
        }
    }

    @Override
    public void visit(BreakStateNode breakStateNode)
    {
        if (loopLevel <= 0)
            throw new MxError(breakStateNode.getLocation(), "break must in loop! \n");
    }

    @Override
    public void visit(ContinueStateNode continueStateNode)
    {
        if (loopLevel <= 0)
            throw new MxError(continueStateNode.getLocation(), "continue must in loop! \n");
    }

    @Override
    public void visit(IfStateNode ifStateNode)
    {
        ifStateNode.getConditionExpr().accept(this);
        if (!(ifStateNode.getConditionExpr().getType() instanceof BoolType))
            throw new MxError(ifStateNode.getLocation(), "condition expression must be bool type!\n");
        if (ifStateNode.getThenState() instanceof FuncBlockNode)
        {
            Scope blockScope = new Scope(currentScope);
            ((FuncBlockNode) ifStateNode.getThenState()).setScope(blockScope);
            currentScope = blockScope;
            ifStateNode.getThenState().accept(this);
            currentScope = currentScope.getFather();
        }
        else
            ifStateNode.getThenState().accept(this);
        if (ifStateNode.getElseState() != null)
        {
            if (ifStateNode.getElseState() instanceof FuncBlockNode)
            {
                Scope blockScope = new Scope(currentScope);
                ((FuncBlockNode) ifStateNode.getElseState()).setScope(blockScope);
                currentScope = blockScope;
                ifStateNode.getElseState().accept(this);
                currentScope = currentScope.getFather();
            }
            else
                ifStateNode.getElseState().accept(this);
        }
    }

    @Override
    public void visit(FuncBlockNode funcBlockNode)
    {
        Scope blockScope = new Scope(currentScope);
        funcBlockNode.setScope(blockScope);
        currentScope = blockScope;
        for (Node funcState : funcBlockNode.getStateList())
            funcState.accept(this);
        currentScope = currentScope.getFather();
    }

    @Override
    public void visit(MemoryAccessExprNode memoryAccessExprNode)
    {
        memoryAccessExprNode.getHostExpr().accept(this);
        String hostID;
        Entity entity;
        ClassEntity classEntity;
        if (memoryAccessExprNode.getHostExpr().getType() instanceof ArrayType)
            hostID = "__array";
        else if (memoryAccessExprNode.getHostExpr().getType() instanceof StringType)
            hostID = "string";
        else if (memoryAccessExprNode.getHostExpr().getType() instanceof ClassType)
            hostID = ((ClassType) (memoryAccessExprNode.getHostExpr().getType())).getClassIdent();
        else
            throw new MxError(memoryAccessExprNode.getLocation(), "hostExpr type is not suitable for MA!\n");
        classEntity = currentScope.getClass(hostID);
        if (classEntity == null)
            throw new MxError(memoryAccessExprNode.getLocation(), "hostExpr class is not defined! (PACNIC)! \n");
        entity = classEntity.getClassScope().getSelfVarOrFunc(memoryAccessExprNode.getMemberStr());
        if (entity instanceof VarEntity)
            memoryAccessExprNode.setType(entity.getType());
        else if (entity instanceof FuncEntity)
        {
            currentFuncCallEntity = (FuncEntity)entity;
            memoryAccessExprNode.setType(entity.getType());
        }
        else
            throw new MxError(memoryAccessExprNode.getLocation(), String.format("%s is not defined in this class!",
                    memoryAccessExprNode.getMemberStr()));
        memoryAccessExprNode.setLeftValue(true);
    }

    @Override
    public void visit(FunctionCallExprNode functionCallExprNode)
    {
        functionCallExprNode.getFuncExpr().accept(this);
        if (!(functionCallExprNode.getFuncExpr().getType() instanceof FuncType))
            throw new MxError(functionCallExprNode.getLocation(), "Function expression invalid!\n");
        FuncEntity funcEntity = currentFuncCallEntity;
        functionCallExprNode.setFuncEntity(funcEntity);
        int paraNum;
        Type requiredType;
        if (funcEntity.getFuncParas() != null)
        {
            if (funcEntity.isInClass())
                paraNum = funcEntity.getFuncParas().size() - 1;
            else
                paraNum = funcEntity.getFuncParas().size();
            if (paraNum != functionCallExprNode.getParaList().size())
                throw new MxError(functionCallExprNode.getLocation(), "Function call paraments number error!\n");
            for (int i = 0; i < paraNum; i++)
            {
                requiredType = funcEntity.getFuncParas().get(i).getType();
                functionCallExprNode.getParaList().get(i).accept(this);
                if (functionCallExprNode.getParaList().get(i).getType() instanceof VoidType)
                    throw new MxError(functionCallExprNode.getLocation(), "Function para cannot be void type!\n");
                if (functionCallExprNode.getParaList().get(i).getType() instanceof NullType)
                {
                    if (!(requiredType instanceof ClassType || requiredType instanceof ArrayType))
                        throw new MxError(functionCallExprNode.getLocation(), String.format("para %d cannot be null!\n", i));
                }
                else if (!functionCallExprNode.getParaList().get(i).getType().equals(requiredType))
                    throw new MxError(functionCallExprNode.getLocation(), String.format("para %d type not meet the reauired!\n", i));
            }
        }
        functionCallExprNode.setLeftValue(false);
        functionCallExprNode.setType(funcEntity.getRetType());
    }

    @Override
    public void visit(ArrayAccessExprNode accessExprNode)
    {
        accessExprNode.getArrExpr().accept(this);
        accessExprNode.getSubExpr().accept(this);
        if (!(accessExprNode.getArrExpr().getType() instanceof ArrayType))
            throw new MxError(accessExprNode.getLocation(), "Array access can only use in array!\n");
        if (!(accessExprNode.getSubExpr().getType() instanceof IntType))
            throw new MxError(accessExprNode.getLocation(), "Array index must be intenger!\n");
        accessExprNode.setLeftValue(true);
        accessExprNode.setType(((ArrayType) accessExprNode.getArrExpr().getType()).getArrType());
    }

    @Override
    public void visit(PostFixExprNode postFixExprNode)
    {
        postFixExprNode.getPreExpr().accept(this);
        if (!postFixExprNode.getPreExpr().isLeftValue())
            throw new MxError(postFixExprNode.getLocation(), String.format("op %s can only use after leftValue!\n",
                    postFixExprNode.getPostFixOp().toString()));
        if (!(postFixExprNode.getPreExpr().getType() instanceof IntType))
            throw new MxError(postFixExprNode.getLocation(), String.format("op %s can only use after IntValue!\n",
                    postFixExprNode.getPostFixOp().toString()));
        postFixExprNode.setLeftValue(false);
        postFixExprNode.setType(intType);
    }

    @Override
    public void visit(NewExprNode newExprNode)
    {
        if (newExprNode.getTotalDim() != 0)
        {
            if (newExprNode.getKnownDims() == null)
                System.err.print("new Expr Error fucking\n");
            for (ExprNode dims : newExprNode.getKnownDims())
            {
                dims.accept(this);
                if (!(dims.getType() instanceof IntType))
                    throw new MxError(newExprNode.getLocation(), "Array index must be intenger!\n");
            }
        }
        newExprNode.setLeftValue(false);
        newExprNode.setType(newExprNode.getNewType());
    }

    @Override
    public void visit(PreFixExprNode preFixExprNode)
    {
        preFixExprNode.getPostExpr().accept(this);
        Type postType = preFixExprNode.getPostExpr().getType();
        Operators.PreFixOp pop = preFixExprNode.getPreFixOp();
        switch (pop)
        {
            case POS:
            case NEG:
            case BITWISE_NOT:
                if (!(postType instanceof IntType))
                    throw new MxError(preFixExprNode.getLocation(), String.format("op %s must before intenger!\n",
                            pop.toString()));
                preFixExprNode.setLeftValue(false);
                preFixExprNode.setType(intType);
                break;
            case INC:
            case DEC:
                if (!(postType instanceof IntType))
                    throw new MxError(preFixExprNode.getLocation(), String.format("op %s must before intenger!\n",
                            pop.toString()));
                if (!preFixExprNode.getPostExpr().isLeftValue())
                    throw new MxError(preFixExprNode.getLocation(), String.format("op %s can only use before " +
                            "leftValue!\n", preFixExprNode.getPreFixOp().toString()));
                preFixExprNode.setLeftValue(true);
                preFixExprNode.setType(intType);
                break;
            case LOGIC_NOT:
                if (!(postType instanceof BoolType))
                    throw new MxError(preFixExprNode.getLocation(), String.format("op %s must before bool!\n",
                            pop.toString()));
                preFixExprNode.setLeftValue(false);
                preFixExprNode.setType(boolType);
                break;
        }
    }

    @Override
    public void visit(BinaryExprNode binaryExprNode)
    {
        binaryExprNode.getLhs().accept(this);
        binaryExprNode.getRhs().accept(this);
        Type LType = binaryExprNode.getLhs().getType();
        Type RType = binaryExprNode.getRhs().getType();
        if (LType instanceof VoidType || RType instanceof VoidType)
            throw new MxError(binaryExprNode.getLocation(), "Binary op cannot use on void!\n");
        Operators.BinaryOp bop = binaryExprNode.getBop();
        binaryExprNode.setLeftValue(false);
        switch (bop)
        {

            case ADD:
                if (LType instanceof StringType && RType instanceof StringType)
                {
                    binaryExprNode.setType(stringType);
                    break;
                }
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case SHL:
            case SHR:
            case BITWISE_AND:
            case BITWISE_OR:
            case BITWISE_XOR:
                if (!(LType instanceof IntType || RType instanceof IntType))
                    throw new MxError(binaryExprNode.getLocation(), String.format("bop %s can only connect " +
                            "intenger!\n", bop.toString()));
                binaryExprNode.setType(intType);
                break;
            case GREATER_EQUAL:
            case LESS_EQUAL:
            case GREATER:
            case LESS:
                if (!LType.equals(RType))
                    throw new MxError(binaryExprNode.getLocation(), "Binary expression shuold have same type!\n");
                if (!(LType instanceof StringType || LType instanceof IntType))
                    throw new MxError(binaryExprNode.getLocation(),
                            String.format("bop %s can only connect intenger " + "or string!\n", bop.toString()));
                binaryExprNode.setType(boolType);
                break;
            case EQUAL:
            case NEQUAL:
                if (!(LType.equals(RType)))
                {
                    if (RType instanceof NullType && !(LType instanceof ArrayType || LType instanceof ClassType))
                        throw new MxError(binaryExprNode.getLocation(),"null can only assign to class or array!\n");
                }
                binaryExprNode.setType(boolType);
                break;
            case LOGIC_AND:
            case LOGIC_OR:
                if (!LType.equals(RType))
                    throw new MxError(binaryExprNode.getLocation(), "Binary expression shuold have same type!\n");
                if (!(LType instanceof BoolType))
                    throw new MxError(binaryExprNode.getLocation(), String.format("bop %s can only connect bool !\n",
                            bop.toString()));
                binaryExprNode.setType(boolType);
                break;
        }
    }

    @Override
    public void visit(AssignExprNode assignExprNode)
    {
        assignExprNode.getLhs().accept(this);
        assignExprNode.getRhs().accept(this);
        if (!assignExprNode.getLhs().isLeftValue())
            throw new MxError(assignExprNode.getLocation(), "Assign left expression is not left value!\n");
        if (assignExprNode.getLhs().getType() instanceof VoidType)
            throw new MxError(assignExprNode.getLocation(), "Assign type cannot be void!\n");
        if (assignExprNode.getRhs().getType() instanceof NullType)
        {
            if (!(assignExprNode.getLhs().getType() instanceof ClassType || assignExprNode.getLhs().getType() instanceof ArrayType))
                throw new MxError(assignExprNode.getLocation(), "null type can only assigned to class or array!\n");
        }
        else if (!assignExprNode.getLhs().getType().equals(assignExprNode.getRhs().getType()))
            throw new MxError(assignExprNode.getLocation(), "Assign type are not euqal!\n");
        assignExprNode.setLeftValue(false);
        assignExprNode.setType(voidType);
    }

    @Override
    public void visit(ConstIntNode constIntNode)
    {
        constIntNode.setLeftValue(false);
        constIntNode.setType(intType);
    }

    @Override
    public void visit(ConstBoolNode constBoolNode)
    {
        constBoolNode.setLeftValue(false);
        constBoolNode.setType(boolType);
    }

    @Override
    public void visit(ConstStringNode constStringNode)
    {
        constStringNode.setLeftValue(false);
        constStringNode.setType(stringType);
    }

    @Override
    public void visit(ConstNullNode constNullNode)
    {
        constNullNode.setLeftValue(false);
        constNullNode.setType(nullType);
    }

    @Override
    public void visit(IdentExprNode identExprNode)
    {
        String ident = identExprNode.getIdentName();
        Entity entity = currentScope.getVarOrFunc(ident);
        if (entity instanceof FuncEntity)
        {
            currentFuncCallEntity = (FuncEntity) entity; // function call will use this.
            identExprNode.setLeftValue(false);
        }
        else if (entity instanceof VarEntity)
        {
            identExprNode.setVarEntity((VarEntity) entity);
            identExprNode.setLeftValue(true);
        }
        else
            throw new MxError(identExprNode.getLocation(), String.format("identifier %s undefined!\n", ident));
        identExprNode.setType(entity.getType());
    }

    @Override
    public void visit(ThisExprNode thisExprNode)
    {
        VarEntity varEntity = currentScope.getVar("this");
        if (varEntity == null)
            throw new MxError(thisExprNode.getLocation(), "identfier this shouldn't appear here!\n");
        thisExprNode.setLeftValue(false);
        thisExprNode.setType(varEntity.getType());
    }
}
