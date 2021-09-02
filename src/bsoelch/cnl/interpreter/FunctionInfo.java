package bsoelch.cnl.interpreter;

import bsoelch.cnl.math.MathObject;

import java.util.ArrayDeque;

class FunctionInfo extends BracketInfo {

    ArrayDeque<Action> prevStack;
    final String currentLine;

    FunctionInfo(FunctionContext context, Interpreter.CodePosition prevPos, ArrayDeque<Action> prevStack, Context.ArgumentData args, String currentLine) {
        super(context,prevPos);
        context.reset(args);
        this.prevStack = prevStack;
        this.currentLine = currentLine;
    }

    MathObject getRes(){
        return localRoot.getRes();
    }
}
