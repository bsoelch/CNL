package bsoelch.cnl.interpreter;

class Function {
    final Interpreter.CodePosition start;
    final int argCount;

    final ProgramEnvironment declarationEnvironment;

    Function(ProgramEnvironment declarationEnvironment, Interpreter.CodePosition start, int argCount) {
        this.declarationEnvironment=declarationEnvironment;
        this.start = start;
        this.argCount = argCount;
    }
}
