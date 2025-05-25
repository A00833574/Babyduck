package src.sem.exps;
import java.util.*;

import src.mem.VirtualMemoryManager;

/**
 * Generador de cuádruplos con back-patch para saltos y manejo de memoria virtual.
 */
public class QuadrupleGenerator {
    private final Stack<String> operandStack   = new Stack<>();
    private final Stack<String> operatorStack  = new Stack<>();
    private final Stack<String> typeStack      = new Stack<>();
    private final List<Quadruple> quadruples   = new ArrayList<>();
    private final VirtualMemoryManager memory;
    private final Stack<Integer> jumpStack     = new Stack<>();

    public QuadrupleGenerator(VirtualMemoryManager memory) {
        this.memory = memory;
    }

    public void pushOperand(String operand, String type) {
        operandStack.push(operand);
        typeStack.push(type);
    }

    public void pushOperator(String operator) {
        operatorStack.push(operator);
    }

    public void generateExpressionQuadruple() {
        String rightOperand = operandStack.pop();
        String rightType    = typeStack.pop();
        String leftOperand  = operandStack.pop();
        String leftType     = typeStack.pop();
        String op           = operatorStack.pop();
        String resultType   = leftType;
        int    addr         = memory.newTemporal(resultType);
        String resultTemp   = String.valueOf(addr);
        quadruples.add(new Quadruple(op, leftOperand, rightOperand, resultTemp));
        operandStack.push(resultTemp);
        typeStack.push(resultType);
    }

    public void generateAssignment(String variable) {
        String value = operandStack.pop();
        typeStack.pop();
        quadruples.add(new Quadruple("=", value, null, variable));
    }

    public void generatePrint() {
        String value = operandStack.pop();
        typeStack.pop();
        quadruples.add(new Quadruple("print", null, null, value));
    }

    public int generateGotoF(String condAddr) {
        quadruples.add(new Quadruple("GOTOF", condAddr, null, null));
        int idx = nextQuad() - 1;
        jumpStack.push(idx);
        return idx;
    }

    public int generateGoto() {
        quadruples.add(new Quadruple("GOTO", null, null, null));
        int idx = nextQuad() - 1;
        jumpStack.push(idx);
        return idx;
    }

    public void fillGoto(int quadIndex, int target) {
        quadruples.get(quadIndex).result = String.valueOf(target);
    }

    public int nextQuad() {
        return quadruples.size();
    }

    public void displayQuadruples() {
        System.out.println("\n--- Cuádruplos generados ---");
        for (int i = 0; i < quadruples.size(); i++) {
            System.out.println(i + ":\t" + quadruples.get(i));
        }
    }

    public String popLastOperand() {
        return operandStack.pop();
    }

    public String peekLastOperand() {
        return operandStack.peek();
    }

    // Funciones de llamada a función
    public void generateEra(String funcName) {
        quadruples.add(new Quadruple("ERA", funcName, null, null));
    }
    public void generateParam(String argAddr, int index) {
        quadruples.add(new Quadruple("PARAM", argAddr, null, String.valueOf(index)));
    }
    public void generateGoSub(String funcName, int target) {
        quadruples.add(new Quadruple("GOSUB", funcName, null, String.valueOf(target)));
    }

    public static void displayOnProgramEnd(QuadrupleGenerator qg) {
        qg.displayQuadruples();
    }

    public static class Quadruple {
        String op,left,right,result;
        Quadruple(String op, String l, String r, String res) {
            this.op=op; this.left=l; this.right=r; this.result=res;
        }
        @Override public String toString(){
            return "(" + op + ", " + left + ", " + right + ", " + result + ")";
        }
    }
}