package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

class RootContext implements Context {
    private final HashMap<BigInteger, Context> children = new HashMap<>();
    private final HashMap<MathObject, MathObject> vars = new HashMap<>();
    private final HashMap<BigInteger, Function> functions = new HashMap<>();

    private final ArgumentData fData;

    public RootContext(@NotNull Context.ArgumentData fData){
        this.fData=fData;
    }

    @Override
    @NotNull
    public Context getChild(BigInteger id) {
        Context child = children.get(id);
        if (child == null) {
            children.put(id, child = new RootContext(fData));
        }
        return child;
    }

    @Override
    @NotNull
    public MathObject getVar(MathObject id) {
        id=id.numericValue();
        MathObject value = vars.get(id);
        if (value == null)
            value = Real.Int.ZERO;
        return value;
    }

    @Override
    public boolean hasVar(MathObject id) {
        id=id.numericValue();
        return vars.containsKey(id);
    }

    @Override
    public void putVar(MathObject id, MathObject value) {
        id=id.numericValue();
        vars.put(id, value);
    }

    @Override
    @NotNull
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
    public MathObject getRes() {
        return fData.getRes();
    }

    @Override
    public void setRes(MathObject o) {
        fData.setRes(o);
    }

    @Override
    public Real.Int argCount() {
        return fData.argCount();
    }

    @Override
    public ValuePointer getArg(BigInteger id) {
        return fData.getArg(id);
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
