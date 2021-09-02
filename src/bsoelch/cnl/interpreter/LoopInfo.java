package bsoelch.cnl.interpreter;

class LoopInfo extends BracketInfo {
    //IF -> ELIF-> ELSE -> END
    //WHILE -> END
    //DO -> END_WHILE
    int state;

    LoopInfo(Context localRoot, Interpreter.CodePosition loopStart, int state) {
        super(localRoot,loopStart);
        this.state = state;
    }
}
