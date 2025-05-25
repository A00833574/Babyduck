package sem.funcs;

import sem.vars.VariableTable;
import java.util.*;

public class FunctionDirectory {
    private Map<String,FunctionInfo> directory = new HashMap<>();
    private String currentFunction = null;

    public void addFunction(String name, String returnType) {
        if (directory.containsKey(name))
            throw new RuntimeException("Función '"+name+"' ya declarada.");
        directory.put(name, new FunctionInfo(returnType));
        currentFunction = name;
    }

    public void setCurrentFunction(String name) {
        if (!directory.containsKey(name))
            throw new RuntimeException("Función '"+name+"' no encontrada.");
        currentFunction = name;
    }

    public void addParam(String name, String type) {
        getCurrentFunction().addParam(name, type);
    }

    public void addVariable(String name, String type) {
        getCurrentFunction().addVariable(name, type);
    }

    public String getVariableType(String name) {
        String t = directory.get(currentFunction).getVariableType(name);
        if (t != null) return t;
        return directory.get("global").getVariableType(name);
    }

    public String getFunctionReturnType(String name) {
        FunctionInfo fi = directory.get(name);
        if (fi == null) throw new RuntimeException("Función no encontrada: "+name);
        return fi.getReturnType();
    }

    /** 
     * Returns the ordered list of parameter names for a function. 
     */
    public List<String> getParameterNames(String name) {
        FunctionInfo fi = directory.get(name);
        if (fi == null) throw new RuntimeException("Función no encontrada: " + name);
        return fi.paramNames;
    }

    @Override
    public String toString() {
        return directory.toString();
    }

    private FunctionInfo getCurrentFunction() {
        if (currentFunction == null || !directory.containsKey(currentFunction))
            throw new RuntimeException("No hay función activa.");
        return directory.get(currentFunction);
    }

    static class FunctionInfo {
        private String returnType;
        private List<String> params      = new ArrayList<>();
        private List<String> paramNames  = new ArrayList<>();
        private VariableTable variables  = new VariableTable();

        FunctionInfo(String returnType) {
            this.returnType = returnType;
        }

        public void addParam(String name, String type) {
            paramNames.add(name);
            params.add(type);
            variables.add(name, type);
        }

        public void addVariable(String name, String type) {
            variables.add(name, type);
        }

        public String getVariableType(String name) {
            return variables.get(name);
        }

        public String getReturnType() {
            return returnType;
        }

        @Override
        public String toString() {
            return "ReturnType: "+returnType+", Params: "+params+", Vars: "+variables;
        }
    }
}