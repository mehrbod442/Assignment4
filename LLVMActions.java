import java.util.*;

public class LLVMActions  {

    enum VarType {STRING, INT, REAL}

    static class Value {
        String content;
        VarType type;

        Value(String content, VarType type) {
            this.content = content;
            this.type = type;
        }
    }


    private HashMap<String, Value> variables = new HashMap<>();
    private HashSet<String> globalNames = new HashSet<>();
    private HashSet<String> localNames = new HashSet<>();
    private Queue<String> infixExpr = new LinkedList<>();
    private boolean global = true;
    private String function = "";



    private void assignVariable(String ID, Value value, int line, boolean isMathExpr) {
        if (global) {
            globalNames.add(ID);
        } else if (!globalNames.contains(ID)) {
            localNames.add(ID);
        }

        if (value.type == VarType.INT) {
            LLVMGenerator.assign_i32(ID, getValue(value, isMathExpr), globalNames);
        } else if (value.type == VarType.REAL) {
            LLVMGenerator.assign_double(ID, getValue(value, isMathExpr), globalNames);
        } else if (value.type == VarType.STRING) {
            assignString(ID, value, line);
        } else {
            error(line, "Assign error: " + ID);
        }
    }

    private String getValue(Value value, boolean isMathExpr) {
        if (isMathExpr) {
            return value.content;
        }
        if (MathUtils.isNumeric(value.content)) {
            return value.content;
        } else {
            if (value.type == VarType.REAL) {
                LLVMGenerator.load_double(value.content, globalNames);
            } else {
                LLVMGenerator.load_i32(value.content, globalNames);
            }
            return "%" + (LLVMGenerator.reg - 1);
        }
    }

    private void assignString(String ID, Value value, int line) {
        if (!variables.containsKey(ID)) {
            LLVMGenerator.assign_string(ID, value.content, global, function);
            variables.put(ID, value);
        } else {
            error(line, ID + " is constant value.");
        }
    }



    private void printVariable(String ID) {
        if (variables.get(ID).type == VarType.INT) {
            LLVMGenerator.printf_i32(ID, globalNames);
        } else if (variables.get(ID).type == VarType.REAL) {
            LLVMGenerator.printf_double(ID, globalNames);
        } else if (variables.get(ID).type == VarType.STRING) {
            LLVMGenerator.printf_string(ID, variables.get(ID).content.length(), globalNames, function);
        }
    }


    private void removeLocalVariables() {
        for (String id : localNames) {
            variables.remove(id);
        }
    }

    private void error(int line, String msg) {
        System.err.println("Error, line " + line + ", " + msg);
        System.exit(1);
    }

}
