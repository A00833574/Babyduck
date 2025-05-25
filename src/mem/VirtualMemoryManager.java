package mem;
import java.util.HashMap;
import java.util.Map;

/**
 * Asigna direcciones virtuales para globals, locals, consts y temps.
 */
public class VirtualMemoryManager {
    // Bases de memoria
    private static final int GLOBAL_INT_BASE    = 1000;
    private static final int GLOBAL_FLOAT_BASE  = 2000;
    private static final int GLOBAL_STRING_BASE = 3000;
    private static final int LOCAL_INT_BASE     = 5000;
    private static final int LOCAL_FLOAT_BASE   = 6000;
    private static final int LOCAL_STRING_BASE  = 7000;
    private static final int TEMP_INT_BASE      = 9000;
    private static final int TEMP_FLOAT_BASE    = 10000;
    private static final int TEMP_STRING_BASE   = 11000;
    private static final int CONST_INT_BASE     = 13000;
    private static final int CONST_FLOAT_BASE   = 14000;
    private static final int CONST_STRING_BASE  = 15000;

    // Contadores de offsets
    private int nextGlobalInt    = 0, nextGlobalFloat  = 0, nextGlobalString = 0;
    private int nextLocalInt     = 0, nextLocalFloat   = 0, nextLocalString  = 0;
    private int nextTempInt      = 0, nextTempFloat    = 0, nextTempString   = 0;
    private int nextConstInt     = 0, nextConstFloat   = 0, nextConstString  = 0;

    // Tablas de direcciones
    private Map<String,Integer> globalVars = new HashMap<>();
    private Map<String,Map<String,Integer>> localVars = new HashMap<>();
    private Map<String,Integer> constants = new HashMap<>();
    private Map<String,Integer> temporals = new HashMap<>();

    public int allocateGlobalVariable(String name, String type) {
        if (globalVars.containsKey(name)) return globalVars.get(name);
        int addr;
        switch (type) {
            case "int":    addr = GLOBAL_INT_BASE    + nextGlobalInt++;    break;
            case "float":  addr = GLOBAL_FLOAT_BASE  + nextGlobalFloat++;  break;
            case "string": addr = GLOBAL_STRING_BASE + nextGlobalString++; break;
            default: throw new RuntimeException("Tipo desconocido: " + type);
        }
        globalVars.put(name, addr);
        return addr;
    }

    public int allocateLocalVariable(String funcName, String name, String type) {
        localVars.putIfAbsent(funcName, new HashMap<>());
        Map<String,Integer> table = localVars.get(funcName);
        if (table.containsKey(name)) return table.get(name);
        int addr;
        switch (type) {
            case "int":    addr = LOCAL_INT_BASE    + nextLocalInt++;    break;
            case "float":  addr = LOCAL_FLOAT_BASE  + nextLocalFloat++;  break;
            case "string": addr = LOCAL_STRING_BASE + nextLocalString++; break;
            default: throw new RuntimeException("Tipo desconocido: " + type);
        }
        table.put(name, addr);
        return addr;
    }

    public int getConstantAddress(String literal, String type) {
        String key = type + ":" + literal;
        if (constants.containsKey(key)) return constants.get(key);
        int addr;
        switch (type) {
            case "int":    addr = CONST_INT_BASE    + nextConstInt++;    break;
            case "float":  addr = CONST_FLOAT_BASE  + nextConstFloat++;  break;
            case "string": addr = CONST_STRING_BASE + nextConstString++; break;
            default: throw new RuntimeException("Tipo desconocido: " + type);
        }
        constants.put(key, addr);
        return addr;
    }

    public int newTemporal(String type) {
        int addr;
        switch (type) {
            case "int":    addr = TEMP_INT_BASE    + nextTempInt++;    break;
            case "float":  addr = TEMP_FLOAT_BASE  + nextTempFloat++;  break;
            case "string": addr = TEMP_STRING_BASE + nextTempString++; break;
            default: throw new RuntimeException("Tipo desconocido: " + type);
        }
        temporals.put(type + "#" + addr, addr);
        return addr;
    }

    public Integer getVariableAddress(String funcName, String name) {
        Map<String,Integer> table = localVars.get(funcName);
        if (table != null && table.containsKey(name)) return table.get(name);
        return globalVars.get(name);
    }

    @Override
    public String toString() {
        return "Globals=" + globalVars + ", Locals=" + localVars +
               ", Consts=" + constants + ", Temps=" + temporals;
    }
}