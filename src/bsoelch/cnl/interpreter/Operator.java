package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.math.MathObject;

import java.io.IOException;

import static bsoelch.cnl.Constants.*;

public class Operator implements Action {
    final ValuePointer[] args;
    final Operators.OperatorInfo operatorInfo;
    final ExecutionEnvironment env;

    Operator(Operators.OperatorInfo operatorInfo, ExecutionEnvironment env) {
        this.env = env;
        this.operatorInfo=operatorInfo;
        args = new ValuePointer[operatorInfo.minArgs];
    }
    /**Constructor for N-ary operators*/
    public Operator(Operators.OperatorInfo operatorInfo, int additionalArgs, ExecutionEnvironment env) {
        this.operatorInfo=operatorInfo;
        if(!operatorInfo.isNary)
            throw new IllegalArgumentException("the operator  "+operatorInfo.name+" is no N-ary operator");
        this.env = env;
        int numArgs= operatorInfo.minArgs+additionalArgs;
        args = new ValuePointer[numArgs];
    }

    public ValuePointer preformOperation(int flags) {
        if (requiresArg())
            throw new IllegalStateException("Missing Argument");
        MathObject res;
        MathObject[] values=new MathObject[args.length];
        for(int i=0;i<args.length;i++)
            values[i]=args[i].getValue();
        res=operatorInfo.execute(env,values);
        if(args[0] instanceof VarPointer){
            if(operatorInfo.storeMode==Operators.MODIFY_ARG0_ALWAYS||
                    (operatorInfo.storeMode==Operators.MODIFY_ARG0_ROOT&&(flags & Interpreter.FLAG_OPERATOR_CHAIN) != 0)){
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
        if(operatorInfo.id!=-1)//don't write Operator ID
            Translator.writeOperator(target,operatorInfo,args.length, false);
    }

    @Override
    public String stringRepresentation() {
        if(operatorInfo.isNary){
            return operatorInfo.name+":"+args.length;
        }else{
            return operatorInfo.name;
        }
    }
}
