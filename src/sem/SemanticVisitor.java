package sem;

import lex.BabyDuckParser;
import lex.BabyDuckBaseVisitor;
import mem.VirtualMemoryManager;
import sem.exps.QuadrupleGenerator;
import sem.funcs.FunctionDirectory;

import org.antlr.v4.runtime.tree.*;
import java.util.*;

public class SemanticVisitor extends BabyDuckBaseVisitor<String> {
    private final FunctionDirectory functionDirectory = new FunctionDirectory();
    private String currentFunction = "global";
    private final VirtualMemoryManager memory = new VirtualMemoryManager();
    private final QuadrupleGenerator quadGen;
    private final Map<String, Integer> funcStartQuad = new HashMap<>();
    // Store each function's parse context for deferred body generation
    private final Map<String, BabyDuckParser.FuncionContext> funcContexts = new HashMap<>();

    public SemanticVisitor() {
        functionDirectory.addFunction("global", "void");
        functionDirectory.setCurrentFunction("global");
        quadGen = new QuadrupleGenerator(memory);
    }

    @Override
    public String visitPrograma(BabyDuckParser.ProgramaContext ctx) {
        return super.visitPrograma(ctx);
    }

    @Override
    public String visitFuncion(BabyDuckParser.FuncionContext ctx) {
        String returnType = ctx.tipo().getText();
        String funcName = ctx.ID().getText();
        functionDirectory.addFunction(funcName, returnType);
        functionDirectory.setCurrentFunction(funcName);
        currentFunction = funcName;

        // Process parameters and variable declarations
        visit(ctx.parametros());
        visit(ctx.vars());

        // Record start quad and context for later body generation
        funcStartQuad.put(funcName, quadGen.nextQuad());
        funcContexts.put(funcName, ctx);

        // Return to global context
        functionDirectory.setCurrentFunction("global");
        currentFunction = "global";
        return null;
    }

    @Override
    public String visitParametros(BabyDuckParser.ParametrosContext ctx) {
        for (int i = 0; i < ctx.tipo().size(); i++) {
            String type = ctx.tipo(i).getText();
            String name = ctx.ID(i).getText();
            // Register parameter (name and type) and allocate local variable
            functionDirectory.addParam(name, type);
            memory.allocateLocalVariable(currentFunction, name, type);
        }
        return null;
    }

    @Override
    public String visitVar_decl(BabyDuckParser.Var_declContext ctx) {
        String type = ctx.tipo().getText();
        for (var idCtx : ctx.ID()) {
            String name = idCtx.getText();
            functionDirectory.addVariable(name, type);
            if ("global".equals(currentFunction)) {
                memory.allocateGlobalVariable(name, type);
            } else {
                memory.allocateLocalVariable(currentFunction, name, type);
            }
        }
        return null;
    }

    @Override
    public String visitAssign(BabyDuckParser.AssignContext ctx) {
        String name = ctx.ID().getText();
        if (functionDirectory.getVariableType(name) == null)
            throw new RuntimeException("Var no declarada: " + name);
        String t = visit(ctx.expresion());
        String addr = quadGen.popLastOperand();
        quadGen.pushOperand(addr, t);
        int varAddr = memory.getVariableAddress(currentFunction, name);
        quadGen.generateAssignment(String.valueOf(varAddr));
        return null;
    }

    @Override
    public String visitExpresion(BabyDuckParser.ExpresionContext ctx) {
        if (ctx.relop() != null) {
            String lt = visit(ctx.exp(0));
            String la = quadGen.popLastOperand();
            String rt = visit(ctx.exp(1));
            String ra = quadGen.popLastOperand();
            String op = ctx.relop().getText();
            quadGen.pushOperand(la, lt);
            quadGen.pushOperand(ra, rt);
            quadGen.pushOperator(op);
            quadGen.generateExpressionQuadruple();
            return "bool";
        }
        return visit(ctx.exp(0));
    }

    @Override
    public String visitExp(BabyDuckParser.ExpContext ctx) {
        if (ctx.termino().size() == 1)
            return visit(ctx.termino(0));
        String lt = visit(ctx.termino(0));
        String la = quadGen.popLastOperand();
        for (int i = 1; i < ctx.termino().size(); i++) {
            String rt = visit(ctx.termino(i));
            String ra = quadGen.popLastOperand();
            String op = ctx.getChild(2 * i - 1).getText();
            quadGen.pushOperand(la, lt);
            quadGen.pushOperand(ra, rt);
            quadGen.pushOperator(op);
            quadGen.generateExpressionQuadruple();
            la = quadGen.peekLastOperand();
            lt = lt;
        }
        return lt;
    }

    @Override
    public String visitTermino(BabyDuckParser.TerminoContext ctx) {
        // Handle multiplication and division with correct precedence
        // Evaluate first factor
        String leftType = visit(ctx.factor(0));
        String leftAddr = quadGen.popLastOperand();
        // For each subsequent factor, apply the operator and generate quadruple
        for (int i = 1; i < ctx.factor().size(); i++) {
            // Operator in between factors is at position 2*i-1 in the parse tree
            String op = ctx.getChild(2 * i - 1).getText();
            // Evaluate next factor
            String rightType = visit(ctx.factor(i));
            String rightAddr = quadGen.popLastOperand();
            // Push left and right operands back, then the operator
            quadGen.pushOperand(leftAddr, leftType);
            quadGen.pushOperand(rightAddr, rightType);
            quadGen.pushOperator(op);
            // Generate the multiplication/division quadruple
            quadGen.generateExpressionQuadruple();
            // The result becomes the new left operand for chaining
            leftAddr = quadGen.peekLastOperand();
        }
        // Push final result for the term
        quadGen.pushOperand(leftAddr, leftType);
        return leftType;
    }

    @Override
    public String visitFactor(BabyDuckParser.FactorContext ctx) {
        if (ctx.ID() != null) {
            String id = ctx.ID().getText();
            String tp = functionDirectory.getVariableType(id);
            if (tp == null)
                throw new RuntimeException("Var no decl: " + id);
            int addr = memory.getVariableAddress(currentFunction, id);
            quadGen.pushOperand(String.valueOf(addr), tp);
            return tp;
        }
        if (ctx.CTE_INT() != null) {
            String v = ctx.CTE_INT().getText();
            int a = memory.getConstantAddress(v, "int");
            quadGen.pushOperand(String.valueOf(a), "int");
            return "int";
        }
        if (ctx.CTE_FLOAT() != null) {
            String v = ctx.CTE_FLOAT().getText();
            int a = memory.getConstantAddress(v, "float");
            quadGen.pushOperand(String.valueOf(a), "float");
            return "float";
        }
        if (ctx.CTE_STRING() != null) {
            String v = ctx.CTE_STRING().getText();
            int a = memory.getConstantAddress(v, "string");
            quadGen.pushOperand(String.valueOf(a), "string");
            return "string";
        }
        if (ctx.expresion() != null) {
            return visit(ctx.expresion());
        }
        return null;
    }

    @Override
    public String visitPrint(BabyDuckParser.PrintContext ctx) {
        for (var ec : ctx.expresion()) {
            String tp = visit(ec);
            String addr = quadGen.popLastOperand();
            quadGen.pushOperand(addr, tp);
            quadGen.generatePrint();
        }
        return null;
    }

    @Override
    public String visitCondition(BabyDuckParser.ConditionContext ctx) {
        String tp = visit(ctx.expresion());
        String addr = quadGen.popLastOperand();
        int fj = quadGen.generateGotoF(addr);
        visit(ctx.body(0));
        if (ctx.body().size() > 1) {
            int gend = quadGen.generateGoto();
            int ei = quadGen.nextQuad();
            quadGen.fillGoto(fj, ei);
            visit(ctx.body(1));
            int endi = quadGen.nextQuad();
            quadGen.fillGoto(gend, endi);
        } else {
            int endi = quadGen.nextQuad();
            quadGen.fillGoto(fj, endi);
        }
        return null;
    }

    @Override
    public String visitCycle(BabyDuckParser.CycleContext ctx) {
        int ls = quadGen.nextQuad();
        String tp = visit(ctx.expresion());
        String addr = quadGen.popLastOperand();
        int fj = quadGen.generateGotoF(addr);
        visit(ctx.body());
        int gl = quadGen.generateGoto();
        quadGen.fillGoto(gl, ls);
        int ex = quadGen.nextQuad();
        quadGen.fillGoto(fj, ex);
        return null;
    }

    @Override
    public String visitF_call(BabyDuckParser.F_callContext ctx) {
        String fn = ctx.ID().getText();
        // 1. Prepare call
        quadGen.generateEra(fn);

        // 2. Evaluate and pass each argument
        List<String> paramNames = functionDirectory.getParameterNames(fn);
        for (int i = 0; i < ctx.expresion().size(); i++) {
            String type = visit(ctx.expresion(i));
            String addr = quadGen.popLastOperand();
            // destination address is the parameter variable address
            int dstAddr = memory.getVariableAddress(fn, paramNames.get(i));
            quadGen.generateParam(addr, dstAddr);
        }

        // 3. Emit GOSUB pointing to the first body quad
        int target = quadGen.nextQuad() + 1;
        quadGen.generateGoSub(fn, target);

        // 4. Now emit the function body quads
        // Switch context to the called function for body generation
        functionDirectory.setCurrentFunction(fn);
        currentFunction = fn;
        BabyDuckParser.FuncionContext fctx = funcContexts.get(fn);
        visit(fctx.body());
        // Restore global context after generating function body
        functionDirectory.setCurrentFunction("global");
        currentFunction = "global";

        return functionDirectory.getFunctionReturnType(fn);
    }

    public FunctionDirectory getFunctionDirectory() {
        return functionDirectory;
    }

    public QuadrupleGenerator getQuadrupleGenerator() {
        return quadGen;
    }

    public VirtualMemoryManager getMemoryManager() {
        return memory;
    }
}
