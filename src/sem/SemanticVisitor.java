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
    private final Map<String, BabyDuckParser.FuncionContext> funcContexts = new HashMap<>();
    private int gotoMainQuadIndex = -1;

    public SemanticVisitor() {
        functionDirectory.addFunction("global", "void");
        functionDirectory.setCurrentFunction("global");
        quadGen = new QuadrupleGenerator(memory);
    }

    // Regla raíz del árbol de sintaxis
    @Override
    public String visitPrograma(BabyDuckParser.ProgramaContext ctx) {
        gotoMainQuadIndex = quadGen.generateGoto();
        visit(ctx.vars());
        visit(ctx.funcs());
        int mainStartIndex = quadGen.nextQuad();
        quadGen.fillGoto(gotoMainQuadIndex, mainStartIndex);
        visit(ctx.body());

        return null;
    }

    @Override
    public String visitFuncion(BabyDuckParser.FuncionContext ctx) {
        // Adquiere el contexto activo de la función
        String funcName = ctx.ID().getText();
        // Añade la función al directorio de funciones
        functionDirectory.addFunction(funcName, "void");
        functionDirectory.setCurrentFunction(funcName);
        currentFunction = funcName;
        // Visita parametros, añadiendolos al directorio de funciones y asigna la
        // memoria
        visit(ctx.parametros());
        // Visita variables locales, añadiendolas al directorio de funciones y asigna la
        // memoria
        visit(ctx.vars());
        // Genera el cuádruplo de inicio de función
        funcStartQuad.put(funcName, quadGen.nextQuad());
        funcContexts.put(funcName, ctx);
        // Visita el cuerpo de la función, lo que generará los cuádruplos
        // correspondientes
        visit(ctx.body());
        // Genera el cuádruplo de fin de función
        quadGen.getQuadruples().add(new QuadrupleGenerator.Quadruple("ENDFUNC", null, null, null));
        // Regresa al contexto global
        functionDirectory.setCurrentFunction("global");
        currentFunction = "global";
        return null;
    }

    @Override
    public String visitParametros(BabyDuckParser.ParametrosContext ctx) {
        for (int i = 0; i < ctx.tipo().size(); i++) {
            String type = ctx.tipo(i).getText();
            String name = ctx.ID(i).getText();
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
        }
        return lt;
    }

    @Override
    public String visitTermino(BabyDuckParser.TerminoContext ctx) {
        String leftType = visit(ctx.factor(0));
        String leftAddr = quadGen.popLastOperand();
        for (int i = 1; i < ctx.factor().size(); i++) {
            String op = ctx.getChild(2 * i - 1).getText();
            String rightType = visit(ctx.factor(i));
            String rightAddr = quadGen.popLastOperand();
            quadGen.pushOperand(leftAddr, leftType);
            quadGen.pushOperand(rightAddr, rightType);
            quadGen.pushOperator(op);
            quadGen.generateExpressionQuadruple();
            leftAddr = quadGen.peekLastOperand();
        }
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
        if (ctx.expresion() != null) {
            return visit(ctx.expresion());
        }
        return null;
    }

    @Override
    public String visitPrint(BabyDuckParser.PrintContext ctx) {
        for (ParseTree child : ctx.children) {
            if (child instanceof TerminalNode
                    && ((TerminalNode) child).getSymbol().getType() == lex.BabyDuckParser.CTE_STRING) {
                String literal = ((TerminalNode) child).getText();
                int addr = memory.getConstantAddress(literal, "string");
                quadGen.pushOperand(String.valueOf(addr), "string");
                quadGen.generatePrint();
            } else if (child instanceof lex.BabyDuckParser.ExpresionContext) {
                String tp = visit((lex.BabyDuckParser.ExpresionContext) child);
                String addr = quadGen.popLastOperand();
                quadGen.pushOperand(addr, tp);
                quadGen.generatePrint();
            }
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
        quadGen.generateEra(fn);
        
        List<String> argAddrs = new ArrayList<>();
        List<String> argTypes = new ArrayList<>();
        for (int i = 0; i < ctx.expresion().size(); i++) {
            String tipo = visit(ctx.expresion(i));
            String dir = quadGen.popLastOperand();
            argAddrs.add(dir);
            argTypes.add(tipo);
        }
        
        List<String> paramNames = functionDirectory.getParameterNames(fn);
        
        for (int i = 0; i < argAddrs.size(); i++) {
            String actualAddr = argAddrs.get(i);
            String paramName = paramNames.get(i);
            int dstAddr = memory.getVariableAddress(fn, paramName);
            quadGen.generateParam(actualAddr, dstAddr);
        }
        
        int target = funcStartQuad.get(fn);
        quadGen.generateGoSub(fn, target);
        return null;
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