package bsoelch.cnl.interpreter;

import java.util.ArrayList;
import java.util.Iterator;

public class SyntaxError extends Exception{
    final ArrayList<String> stack=new ArrayList<>();

    public SyntaxError(Interpreter state,String message){
        super(message);
        initStack(state);
    }

    public SyntaxError(Interpreter state, Throwable e) {
        super(e.getMessage());
        initStack(state);
    }

    private void initStack(Interpreter state) {
        for (Iterator<String> it = state.lines(); it.hasNext(); ) {
            stack.add(it.next());
        }
    }

    public ArrayList<String> getStack() {
        return stack;
    }
}
