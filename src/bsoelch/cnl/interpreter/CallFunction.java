package bsoelch.cnl.interpreter;


import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;

import java.io.IOException;
import java.math.BigInteger;

class CallFunction implements Action {
    private final ProgramEnvironment env;
    private final BigInteger fktId;

    private final ValuePointer[] args;

    CallFunction(ProgramEnvironment env, BigInteger fktId) {
        this.env=env;
        this.fktId=fktId;

        args = new ValuePointer[env.getFunction(fktId).argCount];
    }

    @Override
    public boolean requiresArg() {
        for (ValuePointer p : args)
            if (p == null)
                return true;
        return false;
    }

    public ValuePointer[] getArgs() {
        if(requiresArg())
            throw new IllegalStateException("Missing Argument");
        return args;
    }

    @Override
    public void pushArg(ValuePointer arg) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                args[i] = arg;
                return;
            }
        }
        throw new IllegalStateException("No Argument required");
    }

    public ProgramEnvironment getDeclarationEnvironment() {
        return env.getFunction(fktId).declarationEnvironment;
    }
    public Interpreter.CodePosition getFunctionStart() {
        return env.getFunction(fktId).start;
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        target.write(new long[]{Constants.HEADER_OPERATOR},0,Constants.HEADER_OPERATOR_LENGTH);
        target.writeBigInt(BigInteger.valueOf(Constants.Operators.idByName(Constants.Operators.CALL_FUNCTION)),
                Constants.OPERATOR_INT_HEADER,Constants.OPERATOR_INT_BLOCK,Constants.OPERATOR_INT_BIG_BLOCK);
        target.writeBigInt(fktId,Constants.FUNCTION_ID_INT_HEADER,Constants.FUNCTION_ID_INT_BLOCK,Constants.FUNCTION_ID_INT_BIG_BLOCK);
    }

    @Override
    public String stringRepresentation() {
        return "CALL:"+fktId.toString();
    }
}
