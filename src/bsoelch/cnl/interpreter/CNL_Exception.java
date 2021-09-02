package bsoelch.cnl.interpreter;

import java.util.ArrayList;
import java.util.Iterator;

public class CNL_Exception extends Exception {
    final ArrayList<String> stack = new ArrayList<>();

    public CNL_Exception(Interpreter state, String message) {
        super(message);
        initStack(state);
    }
    public CNL_Exception(Interpreter state, Throwable e) {
        super(e.getMessage());
        initStack(state);
    }

    protected void initStack(Interpreter state) {
        for (Iterator<String> it = state.lines(); it.hasNext(); ) {
            stack.add(it.next());
        }
    }

    public ArrayList<String> getStack() {
        return stack;
    }
}
