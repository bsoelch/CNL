package bsoelch.cnl.interpreter;

import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public interface ProgramEnvironment extends Action {
    @NotNull ProgramEnvironment getChild(BigInteger id);

    @NotNull MathObject getVar(MathObject id);

    boolean hasVar(MathObject id);

    void putVar(MathObject id, MathObject value);

    @NotNull Function getFunction(BigInteger id);

    void putFunction(BigInteger id, Function function);

    @Override
    default boolean requiresArg() {
        return false;
    }

    @Override
    default boolean acceptsArg(int flags) {
        return true;//tell interpreter, that line is not yet finished
    }

    @Override
    default void pushArg(ValuePointer arg) {
        throw new IllegalStateException("Cannot push Argument to ProgramEnvironment");
    }


    MathObject getRes();
    void setRes(MathObject o);
    Real.Int argCount();
    ValuePointer getArg(BigInteger id);


    class ArgumentData {
        private MathObject res;
        private final ValuePointer[] args;
        ArgumentData(ValuePointer[] args){
            this.args = new ValuePointer[args.length];
            for(int i=0;i<args.length;i++){
                if(args[i] instanceof ValuePointerImpl){
                    this.args[i]=args[i];
                }else{//call by value
                    this.args[i]= Translator.wrap(args[i].getValue());
                }
            }
        }
        public ArgumentData(MathObject[] args) {
            this.args = new ValuePointer[args.length];
            for(int i=0;i<args.length;i++){
                this.args[i]= Translator.wrap(args[i]);
            }
        }

        public MathObject getRes(){
            return res;
        }
        public void setRes(MathObject newValue){
            res=newValue;
        }

        public Real.Int argCount() {
            return Real.from(args.length);
        }
        public ValuePointer getArg(BigInteger id) {
            return args[id.intValueExact()];
        }
    }
}
