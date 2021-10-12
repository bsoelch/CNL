package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;
import bsoelch.cnl.math.MathObject;

import java.io.IOException;
import java.math.BigInteger;

/**Code action the either points to an Function/Program Argument or to RES or ARG_COUNT*/
public class ArgPointer implements ValuePointer {
    private final Context env;
    private final BigInteger id;

    public ArgPointer(Context env, BigInteger id) {
        this.env = env;
        this.id = id;
        if(id.compareTo(BigInteger.ZERO)<0||id.compareTo(env.argCount().num())>=0){
            throw new IllegalArgumentException("argId out of Range:"+id+" argCount:"+env.argCount());
        }
    }
    /**ArgPointer to ARG_COUNT and RES*/
    public ArgPointer(Context env, boolean res) {
        this.env = env;
        this.id = res?BigInteger.valueOf(-2): Constants.BIG_INT_NEG_ONE;
    }

    @Override
    public MathObject getValue() {
        if(id.compareTo(BigInteger.ZERO)<0){
            switch (id.intValueExact()){
                case -1:return env.argCount();
                case -2:return env.getRes();
                default:throw new RuntimeException("Unexpected Value for Id: "+id);
            }
        }else{
            return env.getArg(id).getValue();
        }
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        if(id.compareTo(BigInteger.ZERO)<0){
            target.write(new long[]{Constants.HEADER_CONSTANTS},0,Constants.HEADER_CONSTANTS_LENGTH);
            switch (id.intValueExact()){
                case -1:target.writeBigInt(BigInteger.valueOf(Constants.CONSTANT_ARG_COUNT),Constants.CONSTANTS_INT_HEADER
                        ,Constants.CONSTANTS_INT_BLOCK,Constants.CONSTANTS_INT_BIG_BLOCK);
                case -2:target.writeBigInt(BigInteger.valueOf(Constants.CONSTANT_RES),Constants.CONSTANTS_INT_HEADER
                        ,Constants.CONSTANTS_INT_BLOCK,Constants.CONSTANTS_INT_BIG_BLOCK);
                default:throw new RuntimeException("Unexpected Value for Id: "+id);
            }
        }else{
            target.write(new long[]{Constants.HEADER_FUNCTION_ARG},0,Constants.HEADER_FUNCTION_ARG_LENGTH);
            target.writeBigInt(id,Constants.FUNCTION_ARG_INT_HEADER
                    ,Constants.FUNCTION_ARG_INT_BLOCK,Constants.FUNCTION_ARG_INT_BIG_BLOCK);
        }
    }

    @Override
    public String stringRepresentation() {
        if(id.compareTo(BigInteger.ZERO)<0){
            switch (id.intValueExact()){
                case -1:return "ARG_COUNT";
                case -2:return "RES";
                default:throw new RuntimeException("Unexpected Value for Id: "+id);
            }
        }else {
            return "ARG" + id.toString();
        }
    }
}
