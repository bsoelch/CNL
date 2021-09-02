package bsoelch.cnl.interpreter;

public class CNL_RuntimeException extends CNL_Exception {

    public CNL_RuntimeException(Interpreter state, String message){
        super(state, message);
    }

    public CNL_RuntimeException(Interpreter state, Throwable e) {
        super(state, e.getMessage());
    }

}
