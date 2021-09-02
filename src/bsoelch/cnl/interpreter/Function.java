package bsoelch.cnl.interpreter;

import bsoelch.cnl.math.MathObject;

class Function {
    final Interpreter.CodePosition start;
    final int argCount;

    final FunctionContext declarationEnvironment;

    Function(Context declarationEnvironment, Interpreter.CodePosition start, int argCount) {
        this.declarationEnvironment=new FunctionContext(declarationEnvironment,new Context.ArgumentData(new MathObject[argCount]));
        this.start = start;
        this.argCount = argCount;
    }
}
