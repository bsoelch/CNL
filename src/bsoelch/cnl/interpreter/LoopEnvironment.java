package bsoelch.cnl.interpreter;

class LoopEnvironment extends BracketEnvironment {
    //IF -> ELIF-> ELSE -> END
    //WHILE -> END
    //DO -> END_WHILE
    int state;

    LoopEnvironment(ProgramEnvironment localRoot, Interpreter.CodePosition loopStart, int state) {
        super(localRoot,loopStart);
        this.state = state;
    }
}
