package bsoelch.cnl.interpreter;

class LoopEnvironment extends BracketEnvironment {
    final Interpreter.CodePosition loopStart;
    //IF -> ELIF-> ELSE -> END
    //WHILE -> END
    //DO -> END_WHILE
    int state;

    LoopEnvironment(ProgramEnvironment localRoot, Interpreter.CodePosition loopStart, int state) {
        super(localRoot);
        this.loopStart = loopStart;
        this.state = state;
    }
}
