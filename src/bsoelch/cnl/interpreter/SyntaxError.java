package bsoelch.cnl.interpreter;

public class SyntaxError extends CNL_Exception {

    public SyntaxError(Interpreter state,String message){
        super(state,message);
    }

    public SyntaxError(Interpreter state, Throwable e) {
        super(state,e);
    }

}
