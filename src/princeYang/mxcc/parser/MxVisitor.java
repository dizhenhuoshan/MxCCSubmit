// Generated from /home/wymt/Code/Compiler2019/MxCompiler/src/princeYang/mxcc/parser/Mx.g4 by ANTLR 4.7.2
package princeYang.mxcc.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MxParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MxVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MxParser#mxprogram}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMxprogram(MxParser.MxprogramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#declarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarations(MxParser.DeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(MxParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#withVoidType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithVoidType(MxParser.WithVoidTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ArrayType}
	 * labeled alternative in {@link MxParser#nonVoidType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayType(MxParser.ArrayTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NonArrayType}
	 * labeled alternative in {@link MxParser#nonVoidType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonArrayType(MxParser.NonArrayTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#nonVoidnonArrayType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonVoidnonArrayType(MxParser.NonVoidnonArrayTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#functionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDeclaration(MxParser.FunctionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#paramentDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamentDeclarations(MxParser.ParamentDeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#paramentDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamentDeclaration(MxParser.ParamentDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#functionBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionBlock(MxParser.FunctionBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#classDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDeclaration(MxParser.ClassDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#classStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassStatement(MxParser.ClassStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncState}
	 * labeled alternative in {@link MxParser#functionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncState(MxParser.FuncStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code VarDecl}
	 * labeled alternative in {@link MxParser#functionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDecl(MxParser.VarDeclContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExprState}
	 * labeled alternative in {@link MxParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprState(MxParser.ExprStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LoopState}
	 * labeled alternative in {@link MxParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopState(MxParser.LoopStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code JumpState}
	 * labeled alternative in {@link MxParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJumpState(MxParser.JumpStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CondState}
	 * labeled alternative in {@link MxParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondState(MxParser.CondStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncBlockState}
	 * labeled alternative in {@link MxParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncBlockState(MxParser.FuncBlockStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EmptyState}
	 * labeled alternative in {@link MxParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyState(MxParser.EmptyStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ForState}
	 * labeled alternative in {@link MxParser#loopStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForState(MxParser.ForStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code WhileState}
	 * labeled alternative in {@link MxParser#loopStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileState(MxParser.WhileStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ReturnState}
	 * labeled alternative in {@link MxParser#jumpStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnState(MxParser.ReturnStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BreakState}
	 * labeled alternative in {@link MxParser#jumpStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakState(MxParser.BreakStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ContinueState}
	 * labeled alternative in {@link MxParser#jumpStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueState(MxParser.ContinueStateContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#conditionalStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalStatement(MxParser.ConditionalStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FunctionCallExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCallExpr(MxParser.FunctionCallExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IdentifierExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierExpr(MxParser.IdentifierExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ArrayAccessExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayAccessExpr(MxParser.ArrayAccessExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PreFixExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreFixExpr(MxParser.PreFixExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SubExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubExpr(MxParser.SubExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BinaryExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryExpr(MxParser.BinaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NewExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewExpr(MxParser.NewExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ConstantExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantExpr(MxParser.ConstantExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PostFixExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostFixExpr(MxParser.PostFixExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ThisExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThisExpr(MxParser.ThisExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MemeryAccessExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemeryAccessExpr(MxParser.MemeryAccessExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AssignExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignExpr(MxParser.AssignExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Immediate}
	 * labeled alternative in {@link MxParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstInt(MxParser.ConstIntContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ConstBool}
	 * labeled alternative in {@link MxParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstBool(MxParser.ConstBoolContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ConstStr}
	 * labeled alternative in {@link MxParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstStr(MxParser.ConstStrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ConstNull}
	 * labeled alternative in {@link MxParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstNull(MxParser.ConstNullContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#paramentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamentList(MxParser.ParamentListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ArrayCreator}
	 * labeled alternative in {@link MxParser#creator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayCreator(MxParser.ArrayCreatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NonArrayCreator}
	 * labeled alternative in {@link MxParser#creator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonArrayCreator(MxParser.NonArrayCreatorContext ctx);
}