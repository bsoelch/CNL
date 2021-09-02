package bsoelch.cnl.interpreter;

abstract class BracketInfo {
    final protected Context localRoot;
    final protected Interpreter.CodePosition bracketStart;
    BracketInfo(Context localRoot, Interpreter.CodePosition bracketStart){
        this.localRoot = localRoot;
        this.bracketStart = bracketStart;
    }

    public Context getLocalRoot() {
        return localRoot;
    }
}
