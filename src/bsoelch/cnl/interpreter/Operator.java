package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.math.MathObject;

import java.io.IOException;
import java.math.BigInteger;

import static bsoelch.cnl.Constants.*;

public class Operator implements Action {
    final ValuePointer[] args;
    final int type;
    final ExecutionEnvironment env;

    Operator(int type, ExecutionEnvironment env) {
        this.env = env;
        int numArgs=Operators.argCountById(type);
        args = new ValuePointer[numArgs];
        this.type = type;
    }
    /**Constructor for N-ary operators*/
    public Operator(int type, int additionalArgs, ExecutionEnvironment env) {
        if((Operators.flags(type)& Operators.FLAG_NARY)==0)
            throw new IllegalArgumentException(type+" is no N-ary operator");
        this.env = env;
        int numArgs=Operators.argCountById(type)+additionalArgs;
        args = new ValuePointer[numArgs];
        this.type = type;
    }

    public ValuePointer preformOperation(int flags) {
        if (requiresArg())
            throw new IllegalStateException("Missing Argument");
        MathObject res;
        MathObject[] values=new MathObject[args.length];
        for(int i=0;i<args.length;i++)
            values[i]=args[i].getValue();
        res=Operators.execute(type,env,values);
        if(args[0] instanceof VarPointer){
            int storeMode=Operators.storeMode(type);
            if(storeMode==Operators.MODIFY_ARG0_ALWAYS||
                    (storeMode==Operators.MODIFY_ARG0_ROOT&&(flags & Interpreter.FLAG_OPERATOR_CHAIN) != 0)){
                //store result in arg0
                ((VarPointer) args[0]).setValue(res);
                return args[0];
            }
        }
        return Translator.wrap(res);
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

    @Override
    public boolean requiresArg() {
        for (ValuePointer p : args)
            if (p == null)
                return true;
        return false;
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        target.write(new long[]{HEADER_OPERATOR},0, HEADER_OPERATOR_LENGTH);
        target.writeBigInt(BigInteger.valueOf(type), OPERATOR_INT_HEADER,OPERATOR_INT_BLOCK,OPERATOR_INT_BIG_BLOCK);
        int flags=Operators.flags(type);
        if((flags& Operators.FLAG_NARY)!=0){
            int defArgs=Operators.argCountById(type);
            target.writeBigInt(BigInteger.valueOf(args.length-defArgs), NARY_INT_HEADER,NARY_INT_BLOCK,NARY_INT_BIG_BLOCK);
        }
    }

    @Override
    public String stringRepresentation() {
        String name=Operators.nameById(type);
        int flags=Operators.flags(type);
        if((flags& Operators.FLAG_NARY)!=0){
            int defArgs=Operators.argCountById(type);
            return name+":"+(args.length-defArgs);
        }else{
            return name;
        }
    }
}
