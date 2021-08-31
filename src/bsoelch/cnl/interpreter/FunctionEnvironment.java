package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.HashMap;

class FunctionEnvironment extends BracketEnvironment implements ProgramEnvironment {

    ArrayDeque<Action> prevStack;


    final HashMap<BigInteger,FunctionEnvironment> children=new HashMap<>();
    final HashMap<MathObject,MathObject> vars=new HashMap<>();
    ProgramEnvironment.ArgumentData fData;
    final String currentLine;


    FunctionEnvironment(ProgramEnvironment localRoot, Interpreter.CodePosition prevPos, ArrayDeque<Action> prevStack, ArgumentData args, String currentLine) {
        super(localRoot,prevPos);
        this.fData=args;
        this.prevStack = prevStack;
        this.currentLine = currentLine;
    }


    @Override
    public @NotNull ProgramEnvironment getChild(BigInteger id) {
        FunctionEnvironment child = children.get(id);
        if (child == null) {
            children.put(id, child = new FunctionEnvironment(localRoot.getChild(id), bracketStart,prevStack,fData, currentLine));
        }
        return child;
    }

    @Override
    public @NotNull MathObject getVar(MathObject id) {
        id=id.numericValue();
        MathObject value = vars.get(id);
        if (value == null)
            return localRoot.getVar(id);//read values from parent layer if not assigned
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
        vars.put(id,value);
    }

    @Override
    public @NotNull Function getFunction(BigInteger id) {
        return localRoot.getFunction(id);
    }

    @Override
    public void putFunction(BigInteger id, Function function) {
        throw new IllegalStateException("cannot declare Functions in Functions");
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
    public ProgramEnvironment getLocalRoot() {
        return this;
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
