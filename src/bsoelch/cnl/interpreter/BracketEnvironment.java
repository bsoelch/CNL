package bsoelch.cnl.interpreter;

abstract class BracketEnvironment {
    final protected ProgramEnvironment localRoot;
    final protected Interpreter.CodePosition bracketStart;
    BracketEnvironment(ProgramEnvironment localRoot, Interpreter.CodePosition bracketStart){
        this.localRoot = localRoot;
        this.bracketStart = bracketStart;
    }

    public ProgramEnvironment getLocalRoot() {
        return localRoot;
    }
}
