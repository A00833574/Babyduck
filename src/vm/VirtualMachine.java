package vm;

import sem.exps.QuadrupleGenerator.Quadruple;
import mem.VirtualMemoryManager;
import java.util.*;

public class VirtualMachine {
    private final List<Quadruple> quads;
    private final VirtualMemoryManager memoryManager;

    private final Map<Integer,Object> globalMem = new HashMap<>();
    private final Deque<Map<Integer,Object>> localStack = new ArrayDeque<>();
    private final Map<Integer,Object> tempMem   = new HashMap<>();
    private final Map<Integer,Object> constMem  = new HashMap<>();

    private int IP = 0;

    public VirtualMachine(List<Quadruple> quads, VirtualMemoryManager vm) {
        this.quads = quads;
        this.memoryManager = vm;

        for (Map.Entry<String,Integer> e : memoryManager.getConstantsMap().entrySet()) {
            String[] parts = e.getKey().split(":",2);
            String type = parts[0], lit = parts[1];
            Integer addr = e.getValue();
            Object val;
            switch(type) {
                case "int":    val = Integer.parseInt(lit); break;
                case "float":  val = Float.parseFloat(lit); break;
                case "string":
                    val = (lit.startsWith("\"")&&lit.endsWith("\""))
                          ? lit.substring(1,lit.length()-1)
                          : lit;
                    break;
                default: throw new RuntimeException("Tipo desconocido: "+type);
            }
            constMem.put(addr, val);
        }

        localStack.push(new HashMap<>());
    }

    private Object getValue(int addr) {
        if (addr >= VirtualMemoryManager.GLOBAL_INT_BASE
         && addr <  VirtualMemoryManager.LOCAL_INT_BASE)
            return globalMem.get(addr);
        if (addr >= VirtualMemoryManager.LOCAL_INT_BASE
         && addr <  VirtualMemoryManager.TEMP_INT_BASE)
            return localStack.peek().get(addr);
        if (addr >= VirtualMemoryManager.TEMP_INT_BASE
         && addr <  VirtualMemoryManager.CONST_INT_BASE)
            return tempMem.get(addr);
        if (addr >= VirtualMemoryManager.CONST_INT_BASE)
            return constMem.get(addr);
        throw new RuntimeException("Dirección inválida: "+addr);
    }

    private void setValue(int addr, Object val) {
        if (addr >= VirtualMemoryManager.GLOBAL_INT_BASE
         && addr <  VirtualMemoryManager.LOCAL_INT_BASE)
            globalMem.put(addr, val);
        else if (addr >= VirtualMemoryManager.LOCAL_INT_BASE
              && addr <  VirtualMemoryManager.TEMP_INT_BASE)
            localStack.peek().put(addr, val);
        else if (addr >= VirtualMemoryManager.TEMP_INT_BASE
              && addr <  VirtualMemoryManager.CONST_INT_BASE)
            tempMem.put(addr, val);
        else if (addr >= VirtualMemoryManager.CONST_INT_BASE)
            throw new RuntimeException("No puedes sobrescribir constantes: " + addr);
        else
            throw new RuntimeException("Dirección inválida: " + addr);
    }

    public void run() {
        while (IP < quads.size()) {
            Quadruple q = quads.get(IP);
            switch(q.op) {
                case "+":
                case "-":
                case "*":
                case "/": {
                    int l = Integer.parseInt(q.left);
                    int r = Integer.parseInt(q.right);
                    int a = (Integer)getValue(l);
                    int b = (Integer)getValue(r);
                    int res = switch(q.op) {
                        case "+" -> a + b;
                        case "-" -> a - b;
                        case "*" -> a * b;
                        default  -> a / b;
                    };
                    setValue(Integer.parseInt(q.result), res);
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
                    boolean cond = !((Boolean)getValue(Integer.parseInt(q.left)));
                    if (cond) IP = Integer.parseInt(q.result);
                    else      IP++;
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
                    IP = Integer.parseInt(q.result);
                    break;
                }
                default:
                    IP++;
            }
        }
    }
}