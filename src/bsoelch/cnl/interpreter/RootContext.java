package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.NumericValue;
import bsoelch.cnl.math.Real;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

class RootContext extends Context {
    private final HashMap<BigInteger, Context> children = new HashMap<>();
    private final HashMap<NumericValue, MathObject> vars = new HashMap<>();
    private final HashMap<BigInteger, Function> functions = new HashMap<>();


    /**@param fData Argument Data (non-null)*/
    public RootContext(Context.ArgumentData fData){
        super(fData);
    }

    @Override
    public Context getChild(BigInteger id) {
        Context child = children.get(id);
        if (child == null) {
            children.put(id, child = new RootContext(getArgs()));
        }
        return child;
    }

    @Override
    public MathObject getVar(NumericValue id) {
        id=id.numericValue();
        MathObject value = vars.get(id);
        if (value == null)
            value = Real.Int.ZERO;
        return value;
    }


    @Override
    public void putVar(NumericValue id, MathObject value) {
        id=id.numericValue();
        vars.put(id, value);
    }

    @Override
    public Function getFunction(BigInteger id) {
        Function f = functions.get(id);
        if (f == null)
            throw new IllegalArgumentException("Missing Function:" + id);
        return f;
    }

    @Override
    public void putFunction(BigInteger id, Function function) {
        if(functions.put(id, function)!=null){
            throw new IllegalStateException("Function "+id+" is already defied");
        }
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        throw new IllegalStateException("Cannot write unwrapped Environments to File");
    }
    @Override
    public String stringRepresentation() {
        return "RUN_IN:?";
    }
}

