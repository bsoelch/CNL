package bsoelch.cnl.interpreter;

import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.NumericValue;
import bsoelch.cnl.math.Real;

import java.math.BigInteger;
import java.util.HashMap;

public abstract class Context implements Action {
    //VarPointer/CallFunction cannot be cached, since they can be modified during Execution
    //cache ArgPointers to save Memory
    private final HashMap<BigInteger,ArgPointer> argPointers =new HashMap<>();

    final ArgPointer RES=new ArgPointer(this,true);
    final ArgPointer COUNT=new ArgPointer(this,false);

    /**@param id Argument id, has to be non-null
     * @return a ArgPointer to arg id in this context*/
    final ArgPointer argPointer( BigInteger id){
        ArgPointer prev = argPointers.get(id);
        if (prev == null) {
            prev = new ArgPointer(this, id);
            argPointers.put(id, prev);
        }
        return prev;
    }

    private Context.ArgumentData args;

    Context(ArgumentData args){
        this.args=args;
    }

    public void reset(ArgumentData args) {
        this.args=args;
    }

    public ArgumentData getArgs() {
        return args;
    }

    /**@return Child context, has to be non-null*/
    abstract Context getChild(BigInteger id);

    /**@return Variable with given id, has to be non-null*/
    abstract MathObject getVar(NumericValue id);
    /**@return Variable with given id (non-null)*/
    final MathObject getVar(MathObject id) {
        if(id instanceof NumericValue){
            return getVar((NumericValue)id);
        }else{
            return MathObject.deepReplace(id, this::getVar);
        }
    }

    abstract void putVar(NumericValue id, MathObject value);
    final void putVar(MathObject id, MathObject value) {
        if(id instanceof NumericValue){
            putVar((NumericValue) id,value);
        }else{
            MathObject.deepForEach(id,e->putVar(e,value));
        }
    }

    /**@return Function with given id, has to be non-null*/
    abstract  Function getFunction(BigInteger id);

    abstract void putFunction(BigInteger id, Function function);

    @Override
    public boolean requiresArg() {
        return false;
    }

    @Override
    public boolean acceptsArg(int flags) {
        return true;//returns true to simplify code in interpreter
    }

    @Override
    public void pushArg(ValuePointer arg) {
        throw new IllegalStateException("Cannot push Argument to ProgramEnvironment");
    }


    public MathObject getRes() {
        return args.getRes();
    }

    public void setRes(MathObject o) {
        args.setRes(o);
    }

    public Real.Int argCount() {
        return args.argCount();
    }

    public ValuePointer getArg(BigInteger id) {
        return args.getArg(id);
    }



    static class ArgumentData {
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
            return res!=null?res:Real.Int.ZERO;
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
