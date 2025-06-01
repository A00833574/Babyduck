package sem.exps;

import java.util.*;

import mem.VirtualMemoryManager;

public class QuadrupleGenerator {
    private final Stack<String> operandStack = new Stack<>();
    private final Stack<String> operatorStack = new Stack<>();
    private final Stack<String> typeStack = new Stack<>();
    private final List<Quadruple> quadruples = new ArrayList<>();
    private final VirtualMemoryManager memory;
    private final Stack<Integer> jumpStack = new Stack<>();

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
        String rightType = typeStack.pop();
        String leftOperand = operandStack.pop();
        String leftType = typeStack.pop();
        String op = operatorStack.pop();
        String resultType = leftType;
        int addr = memory.newTemporal(resultType);
        String resultTemp = String.valueOf(addr);
        quadruples.add(new Quadruple(op, leftOperand, rightOperand, resultTemp));
        operandStack.push(resultTemp);
        typeStack.push(resultType);
    }

    public void generateAssignment(String variable) {
        // variable ya es la dirección destino (String)
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

    // --------------------------------------------------------
    // Métodos para manejo de llamada a función
    // --------------------------------------------------------
    public void generateEra(String funcName) {
        quadruples.add(new Quadruple("ERA", funcName, null, null));
    }

    public void generateParam(String argAddr, int index) {
        quadruples.add(new Quadruple("PARAM", argAddr, null, String.valueOf(index)));
    }

    public void generateGoSub(String funcName, int target) {
        quadruples.add(new Quadruple("GOSUB", funcName, null, String.valueOf(target)));
    }

    // --------------------------------------------------------
    // Mostrar cuadruplos (con nombres@dirección)
    // --------------------------------------------------------
    private String getNameByAddress(int addr) {
        // Buscar en variables globales
        for (var entry : memory.getGlobalVars().entrySet()) {
            if (entry.getValue() == addr) {
                return entry.getKey();
            }
        }
        // Buscar en variables locales
        for (var funcTable : memory.getLocalVars().values()) {
            for (var entry : funcTable.entrySet()) {
                if (entry.getValue() == addr) {
                    return entry.getKey();
                }
            }
        }
        // Buscar en constantes
        for (var entry : memory.getConstantsMap().entrySet()) {
            if (entry.getValue() == addr) {
                // La clave es "tipo:literal"
                return entry.getKey().split(":", 2)[1];
            }
        }
        // Buscar en temporales
        for (var entry : memory.getTemporalsMap().entrySet()) {
            if (entry.getValue() == addr) {
                // Temporales los nombramos t<número>
                return "t" + (addr - VirtualMemoryManager.TEMP_INT_BASE);
            }
        }
        return null;
    }

    private String formatOperand(String op) {
        if (op == null)
            return "null";
        try {
            int addr = Integer.parseInt(op);
            String name = getNameByAddress(addr);
            return (name != null) ? name : String.valueOf(addr);
        } catch (NumberFormatException e) {
            // No es número: podría ser nombre de función en ERA/GOSUB
            return op;
        }
    }

    public void displayQuadruplesByName() {
        System.out.println("\n--- Cuádruplos (con nombres) ---");
        for (int i = 0; i < quadruples.size(); i++) {
            Quadruple q = quadruples.get(i);
            String leftFmt = formatOperand(q.left);
            String rightFmt = formatOperand(q.right);
            String resultFmt = formatOperand(q.result);
            System.out.printf("%d:\t(%s, %s, %s, %s)%n",
                    i, q.op, leftFmt, rightFmt, resultFmt);
        }
    }

    public void displayQuadruplesByAddress() {
        System.out.println("\n--- Cuádruplos (solo direcciones) ---");
        for (int i = 0; i < quadruples.size(); i++) {
            Quadruple q = quadruples.get(i);
            String leftRaw = (q.left != null ? q.left : "null");
            String rightRaw = (q.right != null ? q.right : "null");
            String resultRaw = (q.result != null ? q.result : "null");
            System.out.printf("%d:\t(%s, %s, %s, %s)%n",
                    i, q.op, leftRaw, rightRaw, resultRaw);
        }
    }

    public String popLastOperand() {
        return operandStack.pop();
    }

    public String peekLastOperand() {
        return operandStack.peek();
    }

    public List<Quadruple> getQuadruples() {
        return quadruples;
    }

    public static void displayOnProgramEnd(QuadrupleGenerator qg) {
        qg.displayQuadruplesByName();
        qg.displayQuadruplesByAddress();
    }

    public static class Quadruple {
        public String op;
        public String left;
        public String right;
        public String result;

        public Quadruple(String op, String l, String r, String res) {
            this.op = op;
            this.left = l;
            this.right = r;
            this.result = res;
        }

        @Override
        public String toString() {
            return "(" + op + ", " + left + ", " + right + ", " + result + ")";
        }
    }
}