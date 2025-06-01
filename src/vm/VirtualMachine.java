package vm;

import sem.exps.QuadrupleGenerator.Quadruple;
import mem.VirtualMemoryManager;
import java.util.*;

public class VirtualMachine {
    private final List<Quadruple> quads;
    private final VirtualMemoryManager memoryManager;

    private final Map<Integer, Object> globalMem = new HashMap<>();
    private final Deque<Map<Integer, Object>> localStack = new ArrayDeque<>();
    private final Map<Integer, Object> tempMem = new HashMap<>();
    private final Map<Integer, Object> constMem = new HashMap<>();
    private final Deque<Integer> callStack = new ArrayDeque<>();

    private int IP = 0;

    public VirtualMachine(List<Quadruple> quads, VirtualMemoryManager vm) {
        this.quads = quads;
        this.memoryManager = vm;

        for (Map.Entry<String, Integer> e : memoryManager.getConstantsMap().entrySet()) {
            String[] parts = e.getKey().split(":", 2);
            String type = parts[0], lit = parts[1];
            Integer addr = e.getValue();
            Object val;
            switch (type) {
                case "int":
                    val = Integer.parseInt(lit);
                    break;
                case "float":
                    val = Float.parseFloat(lit);
                    break;
                case "string":
                    val = (lit.startsWith("\"") && lit.endsWith("\""))
                            ? lit.substring(1, lit.length() - 1)
                            : lit;
                    break;
                default:
                    throw new RuntimeException("Tipo desconocido: " + type);
            }
            constMem.put(addr, val);
        }

        localStack.push(new HashMap<>());
    }

    private Object getValue(int addr) {
        if (addr >= VirtualMemoryManager.GLOBAL_INT_BASE
                && addr < VirtualMemoryManager.LOCAL_INT_BASE) {
            return globalMem.get(addr);
        }
        if (addr >= VirtualMemoryManager.LOCAL_INT_BASE
                && addr < VirtualMemoryManager.TEMP_INT_BASE) {
            // Buscar en todos los marcos locales (de arriba a abajo)
            for (Map<Integer, Object> frame : localStack) {
                if (frame.containsKey(addr)) {
                    return frame.get(addr);
                }
            }
            return null;
        }
        if (addr >= VirtualMemoryManager.TEMP_INT_BASE
                && addr < VirtualMemoryManager.CONST_INT_BASE) {
            return tempMem.get(addr);
        }
        if (addr >= VirtualMemoryManager.CONST_INT_BASE) {
            return constMem.get(addr);
        }
        throw new RuntimeException("Dirección inválida: " + addr);
    }

    private void setValue(int addr, Object val) {
        if (addr >= VirtualMemoryManager.GLOBAL_INT_BASE
                && addr < VirtualMemoryManager.LOCAL_INT_BASE)
            globalMem.put(addr, val);
        else if (addr >= VirtualMemoryManager.LOCAL_INT_BASE
                && addr < VirtualMemoryManager.TEMP_INT_BASE)
            localStack.peek().put(addr, val);
        else if (addr >= VirtualMemoryManager.TEMP_INT_BASE
                && addr < VirtualMemoryManager.CONST_INT_BASE)
            tempMem.put(addr, val);
        else if (addr >= VirtualMemoryManager.CONST_INT_BASE)
            throw new RuntimeException("No puedes sobrescribir constantes: " + addr);
        else
            throw new RuntimeException("Dirección inválida: " + addr);
    }

    public void run() {
        while (IP < quads.size()) {
            Quadruple q = quads.get(IP);
            switch (q.op) {
                case "+":
                case "-":
                case "*":
                case "/": {
                    int lAddr = Integer.parseInt(q.left);
                    int rAddr = Integer.parseInt(q.right);
                    Object leftVal = getValue(lAddr);
                    Object rightVal = getValue(rAddr);
                    // Determine operation type: both int, both float, or mixed
                    if (leftVal instanceof Integer && rightVal instanceof Integer) {
                        int a = (Integer) leftVal;
                        int b = (Integer) rightVal;
                        int res = switch (q.op) {
                            case "+" -> a + b;
                            case "-" -> a - b;
                            case "*" -> a * b;
                            default -> a / b;
                        };
                        setValue(Integer.parseInt(q.result), res);
                    }
                    else {
                        // Treat operands as floats
                        float a = (leftVal instanceof Float) ? (Float) leftVal : (Integer) leftVal;
                        float b = (rightVal instanceof Float) ? (Float) rightVal : (Integer) rightVal;
                        float res = switch (q.op) {
                            case "+" -> a + b;
                            case "-" -> a - b;
                            case "*" -> a * b;
                            default -> a / b;
                        };
                        setValue(Integer.parseInt(q.result), res);
                    }
                    IP++;
                    break;
                }
                case "<":
                case "<=":
                case ">":
                case ">=":
                case "==":
                case "!=": {
                    int lAddr = Integer.parseInt(q.left);
                    int rAddr = Integer.parseInt(q.right);
                    Object leftObj = getValue(lAddr);
                    Object rightObj = getValue(rAddr);
                    boolean resBool;
                    // Both int
                    if (leftObj instanceof Integer && rightObj instanceof Integer) {
                        int a = (Integer) leftObj;
                        int b = (Integer) rightObj;
                        switch (q.op) {
                            case "<":  resBool = a < b; break;
                            case "<=": resBool = a <= b; break;
                            case ">":  resBool = a > b; break;
                            case ">=": resBool = a >= b; break;
                            case "==": resBool = a == b; break;
                            default:   resBool = a != b; // "!="
                        }
                    }
                    // Both float
                    else if (leftObj instanceof Float && rightObj instanceof Float) {
                        float a = (Float) leftObj;
                        float b = (Float) rightObj;
                        switch (q.op) {
                            case "<":  resBool = a < b; break;
                            case "<=": resBool = a <= b; break;
                            case ">":  resBool = a > b; break;
                            case ">=": resBool = a >= b; break;
                            case "==": resBool = a == b; break;
                            default:   resBool = a != b; // "!="
                        }
                    }
                    // Mixed: one int, one float
                    else if ((leftObj instanceof Integer && rightObj instanceof Float) ||
                             (leftObj instanceof Float && rightObj instanceof Integer)) {
                        float a = (leftObj instanceof Float) ? (Float) leftObj : (Integer) leftObj;
                        float b = (rightObj instanceof Float) ? (Float) rightObj : (Integer) rightObj;
                        switch (q.op) {
                            case "<":  resBool = a < b; break;
                            case "<=": resBool = a <= b; break;
                            case ">":  resBool = a > b; break;
                            case ">=": resBool = a >= b; break;
                            case "==": resBool = a == b; break;
                            default:   resBool = a != b; // "!="
                        }
                    }
                    // String comparison unchanged
                    else if (leftObj instanceof String && rightObj instanceof String) {
                        String a = (String) leftObj;
                        String b = (String) rightObj;
                        switch (q.op) {
                            case "<":  resBool = a.compareTo(b) < 0; break;
                            case "<=": resBool = a.compareTo(b) <= 0; break;
                            case ">":  resBool = a.compareTo(b) > 0; break;
                            case ">=": resBool = a.compareTo(b) >= 0; break;
                            case "==": resBool = a.equals(b); break;
                            default:   resBool = !a.equals(b); // "!="
                        }
                    }
                    else {
                        throw new RuntimeException("Tipos incompatibles para comparación: "
                                + leftObj.getClass() + " y " + rightObj.getClass());
                    }
                    int resAddr = Integer.parseInt(q.result);
                    setValue(resAddr, resBool);
                    IP++;
                    break;
                }
                case "=": {
                    int src = Integer.parseInt(q.left);
                    int dst = Integer.parseInt(q.result);
                    Object v = getValue(src);
                    setValue(dst, v);
                    IP++;
                    break;
                }
                case "GOTOF": {
                    boolean cond = !((Boolean) getValue(Integer.parseInt(q.left)));
                    if (cond)
                        IP = Integer.parseInt(q.result);
                    else
                        IP++;
                    break;
                }
                case "GOTO": {
                    IP = Integer.parseInt(q.result);
                    break;
                }
                case "print": {
                    System.out.println(getValue(Integer.parseInt(q.result)));
                    IP++;
                    break;
                }
                case "ERA": {
                    localStack.push(new HashMap<>());
                    IP++;
                    break;
                }
                case "PARAM": {
                    int arg = Integer.parseInt(q.left);
                    int dst = Integer.parseInt(q.result);
                    Object val = getValue(arg);
                    setValue(dst, val);
                    IP++;
                    break;
                }
                case "GOSUB": {
                    callStack.push(IP + 1);
                    IP = Integer.parseInt(q.result);
                    break;
                }
                case "ENDFUNC": {
                    if (callStack.isEmpty()) {
                        // End of program or invalid return
                        IP = quads.size();
                        break;
                    }
                    IP = callStack.pop();
                    break;
                }
                default:
                    IP++;
            }
        }
    }
}