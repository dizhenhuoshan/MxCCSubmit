package princeYang.mxcc.frontend;

import org.antlr.v4.runtime.ParserRuleContext;
import princeYang.mxcc.Config;
import princeYang.mxcc.ast.*;
import princeYang.mxcc.errors.MxError;
import princeYang.mxcc.parser.*;

import java.util.ArrayList;
import java.util.List;

public class AstBuilder extends MxBaseVisitor<Node>
{

    public Type voidType = new VoidType();
    public Type intType = new IntType();
    public Type stringType = new StringType();
    public Type boolType = new BoolType();
    public Type nullType = new NullType();

    @Override
    public Node visitMxprogram(MxParser.MxprogramContext ctx)
    {
        Location location = new Location(ctx);
        List<DeclNode> declarations = new ArrayList<DeclNode>();
        if (ctx.declarations() != null)
        {
            for (ParserRuleContext declaration : ctx.declarations())
            {
                Node decla = visit(declaration);
                declarations.add((DeclNode) decla);
            }
        }
        return new MxProgNode(location, declarations);
    }

    @Override
    public Node visitDeclarations(MxParser.DeclarationsContext ctx)
    {
        Location location = new Location(ctx);
        DeclNode decla;
        if (ctx.variableDeclaration() != null)
            return visit(ctx.variableDeclaration());
        else if (ctx.functionDeclaration() != null)
            return visit(ctx.functionDeclaration());
        else if (ctx.classDeclaration() != null)
            return visit(ctx.classDeclaration());
        else throw new MxError(location, "AstBuilder: Declarations Type Error! \n");
    }

    @Override
    public Node visitVariableDeclaration(MxParser.VariableDeclarationContext ctx)
    {
        Location location = new Location(ctx);
        TypeNode type = (TypeNode) visit(ctx.nonVoidType());
        String name = ctx.Identifier().getText();
        ExprNode init;
        if (ctx.expression() != null)
            init = (ExprNode) visit(ctx.expression());
        else init = null;
        return new VarDeclNode(location, name, type, init);
    }

    @Override
    public Node visitWithVoidType(MxParser.WithVoidTypeContext ctx)
    {
        Location location = new Location(ctx);
        if (ctx.nonVoidType() != null)
            return visit(ctx.nonVoidType());
        else
            return new TypeNode(location, voidType);
    }

    @Override
    public Node visitArrayType(MxParser.ArrayTypeContext ctx)
    {
        Location location = new Location(ctx);
        TypeNode arrType = (TypeNode) visit(ctx.nonVoidType());
        return new TypeNode(location, new ArrayType(arrType.getType()));
    }

    @Override
    public Node visitNonVoidnonArrayType(MxParser.NonVoidnonArrayTypeContext ctx)
    {
        Type type;
        Location location = new Location(ctx);
        if (ctx.Identifier() != null)
            return new TypeNode(location, new ClassType(ctx.Identifier().getText()));
        if (ctx.Int() != null)
            type = intType;
        else if (ctx.Bool() != null)
            type = boolType;
        else if (ctx.String() != null)
            type = stringType;
        else throw new MxError(location, "AstBuilder: NonVoidNonArrayType ERROR! \n");
        return new TypeNode(location, type);
    }

    @Override
    public Node visitFunctionDeclaration(MxParser.FunctionDeclarationContext ctx)
    {
        Location location = new Location(ctx);
        TypeNode retType = null;
        String identifier = ctx.Identifier().getText();
        VarDeclNode paraDecl;
        List<VarDeclNode> paramentDeclarations = new ArrayList<VarDeclNode>();
        FuncBlockNode funcBlock;

        if (ctx.withVoidType() != null)
            retType = (TypeNode) visit(ctx.withVoidType());
        if (ctx.paramentDeclarations() != null)
        {
            for (ParserRuleContext para : ctx.paramentDeclarations().paramentDeclaration())
            {
                paraDecl = (VarDeclNode) visit(para);
                paramentDeclarations.add(paraDecl);
            }
        }
        funcBlock = (FuncBlockNode) visit(ctx.functionBlock());
        return new FuncDeclNode(location, identifier, retType, paramentDeclarations, funcBlock);
    }

    @Override
    public Node visitParamentDeclaration(MxParser.ParamentDeclarationContext ctx)
    {
        Location location = new Location(ctx);
        TypeNode typeNode = (TypeNode) visit(ctx.nonVoidType());
        String ident = ctx.Identifier().getText();
        return new VarDeclNode(location, ident, typeNode, null);
    }

    @Override
    public Node visitClassDeclaration(MxParser.ClassDeclarationContext ctx)
    {
        Location location = new Location(ctx);
        String identifier = ctx.Identifier().getText();
        List<VarDeclNode> varDecls = new ArrayList<VarDeclNode>();
        List<FuncDeclNode> funDecls = new ArrayList<FuncDeclNode>();
        DeclNode classState;

        if (ctx.classStatement() != null)
        {
            for (ParserRuleContext state : ctx.classStatement())
            {
                classState = (DeclNode) visit(state);
                if (classState instanceof VarDeclNode)
                    varDecls.add((VarDeclNode) classState);
                else if (classState instanceof FuncDeclNode)
                    funDecls.add((FuncDeclNode) classState);
                else throw new MxError(location,"AstBuilder: ClassDeclaration classStatement ERROR! \n");
            }
        }
        return new ClassDeclNode(location, identifier, varDecls, funDecls);
    }

    @Override
    public Node visitFunctionBlock(MxParser.FunctionBlockContext ctx)
    {
        Location location = new Location(ctx);
        List<Node> stateList = new ArrayList<Node>();
        Node funcState;

        if (ctx.functionStatement() != null)
        {
            for (ParserRuleContext state : ctx.functionStatement())
            {
                funcState = visit(state);
                if (funcState != null)
                {
                    stateList.add(funcState);
                }
            }
        }
        return new FuncBlockNode(location, stateList);
    }

    @Override
    public Node visitFuncState(MxParser.FuncStateContext ctx)
    {
        return visit(ctx.statement());
    }

    @Override
    public Node visitVarDecl(MxParser.VarDeclContext ctx)
    {
        return visit(ctx.variableDeclaration());
    }

    @Override
    public Node visitExprState(MxParser.ExprStateContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode exprState = (ExprNode) visit(ctx.expression());
        return new ExprStateNode(location, exprState);
    }

    @Override
    public Node visitLoopState(MxParser.LoopStateContext ctx)
    {
        return visit(ctx.loopStatement());
    }

    @Override
    public Node visitForState(MxParser.ForStateContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode start = null, stop = null, step = null;
        StateNode loopState;
        if (ctx.start != null)
            start = (ExprNode) visit(ctx.start);
        if (ctx.stop != null)
            stop = (ExprNode) visit(ctx.stop);
        if (ctx.step != null)
            step = (ExprNode) visit(ctx.step);
        if (ctx.statement() != null)
            loopState = (StateNode) visit(ctx.statement());
        else throw new MxError(location, "AstBuilder: For statement loopBody ERROR! \n");
        return new ForStateNode(location, start, stop, step, loopState);
    }

    @Override
    public Node visitWhileState(MxParser.WhileStateContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode condExpr;
        StateNode loopState;
        if (ctx.expression() != null)
            condExpr = (ExprNode) visit(ctx.expression());
        else throw new MxError(location, "AstBuilder: While statement loop conditionExpr ERROR! \n");
        if (ctx.statement() != null)
            loopState = (StateNode) visit(ctx.statement());
        else throw new MxError(location, "AstBuilder: While statement loopBody ERROR! \n");

        return new WhileStateNode(location, condExpr, loopState);
    }

    @Override
    public Node visitJumpState(MxParser.JumpStateContext ctx)
    {
        return visit(ctx.jumpStatement());
    }

    @Override
    public Node visitReturnState(MxParser.ReturnStateContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode retExpr = null;
        if (ctx.expression() != null)
            retExpr = (ExprNode) visit(ctx.expression());
        return new ReturnStateNode(location, retExpr);
    }

    @Override
    public Node visitBreakState(MxParser.BreakStateContext ctx)
    {
        Location location = new Location(ctx);
        return new BreakStateNode(location);
    }

    @Override
    public Node visitContinueState(MxParser.ContinueStateContext ctx)
    {
        Location location = new Location(ctx);
        return new ContinueStateNode(location);
    }

    @Override
    public Node visitCondState(MxParser.CondStateContext ctx)
    {
        return visit(ctx.conditionalStatement());
    }

    @Override
    public Node visitConditionalStatement(MxParser.ConditionalStatementContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode condExpr = null;
        StateNode thenState = null, elseState = null;
        if (ctx.expression() != null)
            condExpr = (ExprNode) visit(ctx.expression());
        else throw new MxError(location, "AstBuilder: conditionalStatement expression missing ERROR! \n");
        if (ctx.thenStatement != null)
            thenState = (StateNode) visit(ctx.thenStatement);
        else throw new MxError(location, "AstBuilder: conditionalStatement thenState missing ERROR! \n");
        if (ctx.elseStatement != null)
            elseState = (StateNode) visit(ctx.elseStatement);
        return new IfStateNode(location, condExpr, thenState, elseState);
    }

    @Override
    public Node visitFuncBlockState(MxParser.FuncBlockStateContext ctx)
    {
        return visit(ctx.functionBlock());
    }

    @Override
    public Node visitEmptyState(MxParser.EmptyStateContext ctx)
    {
        return null;
    }

    @Override
    public Node visitMemeryAccessExpr(MxParser.MemeryAccessExprContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode hostExpr = (ExprNode) visit(ctx.expression());
        String memberStr = ctx.Identifier().getText();
        return new MemoryAccessExprNode(location, hostExpr, memberStr);
    }

    @Override
    public Node visitFunctionCallExpr(MxParser.FunctionCallExprContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode funcExpr;
        List<ExprNode> funcCallParaList = new ArrayList<ExprNode>();
        if (ctx.expression() != null)
            funcExpr = (ExprNode) visit(ctx.expression());
        else throw new MxError(location, "AstBuilder: function call expression funcExpr missing ERROR! \n");
        if (ctx.paramentList() != null)
        {
            for (ParserRuleContext parament : ctx.paramentList().expression())
            {
                funcCallParaList.add((ExprNode) visit(parament));
            }
        }
        return new FunctionCallExprNode(location, funcExpr, funcCallParaList);
    }

    @Override
    public Node visitArrayAccessExpr(MxParser.ArrayAccessExprContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode arrExpr = (ExprNode) visit(ctx.array);
        ExprNode subExpr = (ExprNode) visit(ctx.sub);
        return new ArrayAccessExprNode(location, arrExpr, subExpr);
    }

    @Override
    public Node visitPostFixExpr(MxParser.PostFixExprContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode preExpr = (ExprNode) visit(ctx.expression());
        Operators.PostFixOp postFixOp;
        switch (ctx.op.getText())
        {
            case "++": postFixOp = Operators.PostFixOp.INC; break;
            case "--": postFixOp = Operators.PostFixOp.DEC; break;
            default: throw new MxError(location, "AstBuilder: post fix expression operator ERROR! \n");
        }
        return new PostFixExprNode(location, preExpr, postFixOp);
    }

    @Override
    public Node visitNewExpr(MxParser.NewExprContext ctx)
    {
        return visit(ctx.creator());
    }

    @Override
    public Node visitArrayCreator(MxParser.ArrayCreatorContext ctx)
    {
        int totalDim = 0, knownDim = 0;
        Location location = new Location(ctx);
        Type newType = ((TypeNode) visit(ctx.nonVoidnonArrayType())).getType();
        List<ExprNode> knownDims = new ArrayList<ExprNode>();
        if (ctx.expression() != null)
        {
            for (ParserRuleContext dims : ctx.expression())
            {
                knownDim++;
                knownDims.add((ExprNode) visit(dims));
            }
        }
        if (ctx.Lbracket().size() != ctx.Rbracket().size())
            throw new MxError(location, "AstBuilder: Array Creator bracket pairing ERROR! \n");
        totalDim = ctx.Lbracket().size();
        for (int i = 0; i < totalDim; i++)
            newType = new ArrayType(newType);
        return new NewExprNode(location, newType, totalDim, knownDim, knownDims);
    }

    @Override
    public Node visitNonArrayCreator(MxParser.NonArrayCreatorContext ctx)
    {
        Location location = new Location(ctx);
        Type type = ((TypeNode) visit(ctx.nonVoidnonArrayType())).getType();
        return new NewExprNode(location, type, 0, 0, null);
    }

    @Override
    public Node visitPreFixExpr(MxParser.PreFixExprContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode postExpr = (ExprNode) visit(ctx.expression());
        Operators.PreFixOp preFixOp;
        switch (ctx.op.getText())
        {
            case "++": preFixOp = Operators.PreFixOp.INC; break;
            case "--": preFixOp = Operators.PreFixOp.DEC; break;
            case "+": preFixOp = Operators.PreFixOp.POS; break;
            case "-": preFixOp = Operators.PreFixOp.NEG; break;
            case "!": preFixOp = Operators.PreFixOp.LOGIC_NOT; break;
            case "~": preFixOp = Operators.PreFixOp.BITWISE_NOT; break;
            default: throw new MxError(location, "AstBuilder: pre fix expression operator ERROR! \n");
        }
        return new PreFixExprNode(location, postExpr, preFixOp);
    }

    @Override
    public Node visitBinaryExpr(MxParser.BinaryExprContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode lhsExpr = (ExprNode) visit(ctx.lhs);
        ExprNode rhsExpr = (ExprNode) visit(ctx.rhs);
        Operators.BinaryOp binaryOp;
        switch (ctx.op.getText())
        {
            case "*": binaryOp = Operators.BinaryOp.MUL; break;
            case "/": binaryOp = Operators.BinaryOp.DIV; break;
            case "%": binaryOp = Operators.BinaryOp.MOD; break;
            case "+": binaryOp = Operators.BinaryOp.ADD; break;
            case "-": binaryOp = Operators.BinaryOp.SUB; break;
            case "<<": binaryOp = Operators.BinaryOp.SHL; break;
            case ">>": binaryOp = Operators.BinaryOp.SHR; break;
            case "<=": binaryOp = Operators.BinaryOp.LESS_EQUAL; break;
            case ">=": binaryOp = Operators.BinaryOp.GREATER_EQUAL; break;
            case "<": binaryOp = Operators.BinaryOp.LESS; break;
            case ">": binaryOp = Operators.BinaryOp.GREATER; break;
            case "==": binaryOp = Operators.BinaryOp.EQUAL; break;
            case "!=": binaryOp = Operators.BinaryOp.NEQUAL; break;
            case "&": binaryOp = Operators.BinaryOp.BITWISE_AND; break;
            case "^": binaryOp = Operators.BinaryOp.BITWISE_XOR; break;
            case "|": binaryOp = Operators.BinaryOp.BITWISE_OR; break;
            case "&&": binaryOp = Operators.BinaryOp.LOGIC_AND; break;
            case "||": binaryOp = Operators.BinaryOp.LOGIC_OR; break;
            default: throw new MxError(location, "AstBuilder: Binary expression operator ERROR! \n");
        }
        return new BinaryExprNode(location, lhsExpr, binaryOp, rhsExpr);
    }

    @Override
    public Node visitAssignExpr(MxParser.AssignExprContext ctx)
    {
        Location location = new Location(ctx);
        ExprNode lhsExpr = (ExprNode) visit(ctx.lhs);
        ExprNode rhsExpr = (ExprNode) visit(ctx.rhs);
        return new AssignExprNode(location, lhsExpr, rhsExpr);
    }

    @Override
    public Node visitConstantExpr(MxParser.ConstantExprContext ctx)
    {
        return visit(ctx.constant());
    }

    @Override
    public Node visitConstInt(MxParser.ConstIntContext ctx)
    {
        Location location = new Location(ctx);
        int value;
        try
        {
            value = Integer.parseInt(ctx.ConstIntenger().getText());
        }
        catch (Throwable throwable)
        {
            throw new MxError(location, "AstBuilder: const int value parser ERROR! \n");
        }
        return new ConstIntNode(location, value);
    }

    @Override
    public Node visitConstBool(MxParser.ConstBoolContext ctx)
    {
        Location location = new Location(ctx);
        boolean value;
        switch (ctx.ConstBool().getText())
        {
            case "true": value = true; break;
            case "false": value = false; break;
            default: throw new MxError(location, "AstBuilder: const boolean value parser ERROR! \n");
        }
        return new ConstBoolNode(location, value);
    }

    @Override
    public Node visitConstStr(MxParser.ConstStrContext ctx)
    {
        Location location = new Location(ctx);
        String buffer, value;
        buffer = ctx.ConstString().getText();
        buffer = buffer.substring(1, buffer.length() - 1);
        buffer = buffer.replace("\\\\", "\\");
        buffer = buffer.replace("\\n", "\n");
        value = buffer.replace("\\\"", "\"");
        return new ConstStringNode(location, value);
    }

    @Override
    public Node visitConstNull(MxParser.ConstNullContext ctx)
    {
        Location location = new Location(ctx);
        return new ConstNullNode(location);
    }

    @Override
    public Node visitIdentifierExpr(MxParser.IdentifierExprContext ctx)
    {
        Location location = new Location(ctx);
        String identifier = ctx.Identifier().getText();
        return new IdentExprNode(location, identifier);
    }

    @Override
    public Node visitThisExpr(MxParser.ThisExprContext ctx)
    {
        Location location = new Location(ctx);
        return new ThisExprNode(location);
    }

    @Override
    public Node visitSubExpr(MxParser.SubExprContext ctx)
    {
        return visit(ctx.expression());
    }
}

