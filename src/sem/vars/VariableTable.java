package src.sem.vars;
import java.util.*;

public class VariableTable {
    private Map<String,String> table = new HashMap<>();

    public void add(String name, String type) {
        if (table.containsKey(name)) {
            throw new RuntimeException("Variable '"+name+"' ya declarada.");
        }
        table.put(name, type);
    }

    public String get(String name) {
        return table.get(name);
    }

    @Override
    public String toString() {
        return table.toString();
    }
}