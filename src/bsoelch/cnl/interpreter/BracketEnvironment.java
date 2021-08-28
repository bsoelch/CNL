package bsoelch.cnl.interpreter;

abstract class BracketEnvironment {
    final protected ProgramEnvironment localRoot;
    BracketEnvironment(ProgramEnvironment localRoot){
        this.localRoot = localRoot;
    }

    public ProgramEnvironment getLocalRoot() {
        return localRoot;
    }
}
