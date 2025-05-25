package src.lex;
// Generated from BabyDuck.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BabyDuckParser}.
 */
public interface BabyDuckListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#programa}.
	 * @param ctx the parse tree
	 */
	void enterPrograma(BabyDuckParser.ProgramaContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#programa}.
	 * @param ctx the parse tree
	 */
	void exitPrograma(BabyDuckParser.ProgramaContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#vars}.
	 * @param ctx the parse tree
	 */
	void enterVars(BabyDuckParser.VarsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#vars}.
	 * @param ctx the parse tree
	 */
	void exitVars(BabyDuckParser.VarsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl(BabyDuckParser.Var_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl(BabyDuckParser.Var_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#tipo}.
	 * @param ctx the parse tree
	 */
	void enterTipo(BabyDuckParser.TipoContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#tipo}.
	 * @param ctx the parse tree
	 */
	void exitTipo(BabyDuckParser.TipoContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#funcs}.
	 * @param ctx the parse tree
	 */
	void enterFuncs(BabyDuckParser.FuncsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#funcs}.
	 * @param ctx the parse tree
	 */
	void exitFuncs(BabyDuckParser.FuncsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#funcion}.
	 * @param ctx the parse tree
	 */
	void enterFuncion(BabyDuckParser.FuncionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#funcion}.
	 * @param ctx the parse tree
	 */
	void exitFuncion(BabyDuckParser.FuncionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#parametros}.
	 * @param ctx the parse tree
	 */
	void enterParametros(BabyDuckParser.ParametrosContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#parametros}.
	 * @param ctx the parse tree
	 */
	void exitParametros(BabyDuckParser.ParametrosContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#body}.
	 * @param ctx the parse tree
	 */
	void enterBody(BabyDuckParser.BodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#body}.
	 * @param ctx the parse tree
	 */
	void exitBody(BabyDuckParser.BodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#statements}.
	 * @param ctx the parse tree
	 */
	void enterStatements(BabyDuckParser.StatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#statements}.
	 * @param ctx the parse tree
	 */
	void exitStatements(BabyDuckParser.StatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(BabyDuckParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(BabyDuckParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#assign}.
	 * @param ctx the parse tree
	 */
	void enterAssign(BabyDuckParser.AssignContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#assign}.
	 * @param ctx the parse tree
	 */
	void exitAssign(BabyDuckParser.AssignContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#print}.
	 * @param ctx the parse tree
	 */
	void enterPrint(BabyDuckParser.PrintContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#print}.
	 * @param ctx the parse tree
	 */
	void exitPrint(BabyDuckParser.PrintContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(BabyDuckParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(BabyDuckParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#cycle}.
	 * @param ctx the parse tree
	 */
	void enterCycle(BabyDuckParser.CycleContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#cycle}.
	 * @param ctx the parse tree
	 */
	void exitCycle(BabyDuckParser.CycleContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpresion(BabyDuckParser.ExpresionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpresion(BabyDuckParser.ExpresionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#relop}.
	 * @param ctx the parse tree
	 */
	void enterRelop(BabyDuckParser.RelopContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#relop}.
	 * @param ctx the parse tree
	 */
	void exitRelop(BabyDuckParser.RelopContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExp(BabyDuckParser.ExpContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExp(BabyDuckParser.ExpContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#termino}.
	 * @param ctx the parse tree
	 */
	void enterTermino(BabyDuckParser.TerminoContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#termino}.
	 * @param ctx the parse tree
	 */
	void exitTermino(BabyDuckParser.TerminoContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#factor}.
	 * @param ctx the parse tree
	 */
	void enterFactor(BabyDuckParser.FactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#factor}.
	 * @param ctx the parse tree
	 */
	void exitFactor(BabyDuckParser.FactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BabyDuckParser#f_call}.
	 * @param ctx the parse tree
	 */
	void enterF_call(BabyDuckParser.F_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link BabyDuckParser#f_call}.
	 * @param ctx the parse tree
	 */
	void exitF_call(BabyDuckParser.F_callContext ctx);
}