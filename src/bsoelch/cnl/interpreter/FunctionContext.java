package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.NumericValue;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

public class FunctionContext extends Context{
    final Context parent;

    final HashMap<BigInteger,FunctionContext> children=new HashMap<>();
    final HashMap<NumericValue,MathObject> vars=new HashMap<>();


    public FunctionContext(Context parent, ArgumentData fData) {
        super(fData);
        this.parent = parent;
    }

    public void reset(ArgumentData args) {
        super.reset(args);
        vars.clear();
        children.clear();
    }

    @Override
    public FunctionContext getChild(BigInteger id) {
        FunctionContext child = children.get(id);
        if (child == null) {
            children.put(id, child = new FunctionContext(parent.getChild(id),getArgs()));
        }
        return child;
    }

    @Override
    public MathObject getVar(NumericValue id) {
        id=id.numericValue();
        MathObject value = vars.get(id);
        if (value == null)
            return parent.getVar(id);//read values from parent layer if not assigned
        return value;
    }

    @Override
    public void putVar(NumericValue id, MathObject value) {
        id=id.numericValue();
        vars.put(id,value);
    }

    @Override
    public Function getFunction(BigInteger id) {
        return parent.getFunction(id);
    }

    @Override
    public void putFunction(BigInteger id, Function function) {
        throw new IllegalStateException("cannot declare Functions in Functions");
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
