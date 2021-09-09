package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.NumericValue;
import bsoelch.cnl.math.Real;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

public class FunctionContext implements Context{
    final Context parent;

    final HashMap<BigInteger,FunctionContext> children=new HashMap<>();
    final HashMap<NumericValue,MathObject> vars=new HashMap<>();

    private Context.ArgumentData args;

    public FunctionContext(Context parent, ArgumentData fData) {
        this.parent = parent;
        this.args = fData;
    }

    public void reset(ArgumentData args) {
        this.args=args;
        //addLater? allow persistent variables between function calls
        vars.clear();
        children.clear();
    }

    @Override
    public @NotNull FunctionContext getChild(BigInteger id) {
        FunctionContext child = children.get(id);
        if (child == null) {
            children.put(id, child = new FunctionContext(parent.getChild(id),args));
        }
        return child;
    }

    @Override
    public @NotNull MathObject getVar(NumericValue id) {
        id=id.numericValue();
        MathObject value = vars.get(id);
        if (value == null)
            return parent.getVar(id);//read values from parent layer if not assigned
        return value;
    }

    @Override
    public boolean hasVar(NumericValue id) {
        id=id.numericValue();
        return vars.containsKey(id);
    }

    @Override
    public void putVar(NumericValue id, MathObject value) {
        id=id.numericValue();
        vars.put(id,value);
    }

    @Override
    public @NotNull Function getFunction(BigInteger id) {
        return parent.getFunction(id);
    }

    @Override
    public void putFunction(BigInteger id, Function function) {
        throw new IllegalStateException("cannot declare Functions in Functions");
    }

    @Override
    public MathObject getRes() {
        return args.getRes();
    }

    @Override
    public void setRes(MathObject o) {
        args.setRes(o);
    }

    @Override
    public Real.Int argCount() {
        return args.argCount();
    }

    @Override
    public ValuePointer getArg(BigInteger id) {
        return args.getArg(id);
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
