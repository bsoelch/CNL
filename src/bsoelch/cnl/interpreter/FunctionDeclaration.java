package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;

import java.io.IOException;
import java.math.BigInteger;

public class FunctionDeclaration implements Action {
    final BigInteger fktId;
    final Function function;
    public FunctionDeclaration(BigInteger fktId, Function function) {
        this.fktId = fktId;
        this.function = function;
    }

    @Override
    public boolean requiresArg() {
        return false;
    }
    @Override
    public void pushArg(ValuePointer arg) {
        throw new IllegalStateException("No argument Required");
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        target.write(new long[]{Constants.HEADER_FUNCTION_DECLARATION},0,Constants.HEADER_FUNCTION_DECLARATION_LENGTH);

        target.writeBigInt(BigInteger.valueOf(function.argCount),Constants.FUNCTION_ARG_INT_HEADER,Constants.FUNCTION_ARG_INT_BLOCK
                ,Constants.FUNCTION_ARG_INT_BIG_BLOCK);
        target.writeBigInt(fktId,Constants.FUNCTION_ID_INT_HEADER,Constants.FUNCTION_ID_INT_BLOCK,Constants.FUNCTION_ID_INT_BIG_BLOCK);
    }

    @Override
    public String stringRepresentation() {
        return "NEW_FUNC:"+function.argCount+","+fktId.toString()+"[";
    }
}
