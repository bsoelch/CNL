package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;
import bsoelch.cnl.math.MathObject;

import java.io.IOException;

class BracketDeclaration implements Action {
    int type;
    MathObject param;
    boolean needsArg;

    Interpreter.CodePosition declarationStart;

    final ProgramEnvironment declarationEnvironment;

    BracketDeclaration(ProgramEnvironment decEnv, int type, Interpreter.CodePosition declarationStart) {
        this.declarationEnvironment=decEnv;
        this.type = type;
        switch (type){
            case Constants.BRACKET_FLAG_DO:
            case Constants.BRACKET_FLAG_END:
            case Constants.BRACKET_FLAG_ELSE:needsArg=false;break;
            default:needsArg=true;
        }
        this.declarationStart = declarationStart;
    }

    @Override
    public boolean requiresArg() {
        return needsArg&&param == null;
    }

    @Override
    public void pushArg(ValuePointer arg) {
        if (needsArg&&param == null) {
            this.param = arg.getValue();
        } else {
            throw new IllegalStateException("No Argument required");
        }
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        target.write(new long[]{Constants.HEADER_BRACKET},0,Constants.HEADER_BRACKET_LENGTH);
        if((type&1)==0){
            target.write(new long[]{type},0,3);//TODO? Length Constants
        }else{
            target.write(new long[]{type},0,4);
        }
    }

    @Override
    public String stringRepresentation() {
        switch (type){
            case Constants.BRACKET_FLAG_DO:return "[";
            case Constants.BRACKET_FLAG_IF_EQ:return "[!";
            case Constants.BRACKET_FLAG_IF_NE:return "[?";
            case Constants.BRACKET_FLAG_WHILE_EQ:return "[.!";
            case Constants.BRACKET_FLAG_WHILE_NE:return "[.?";
            case Constants.BRACKET_FLAG_ELSE:return "|";
            case Constants.BRACKET_FLAG_ELIF_EQ:return "|!";
            case Constants.BRACKET_FLAG_ELIF_NE:return "|?";
            case Constants.BRACKET_FLAG_END_WHILE_EQ:return "]!";
            case Constants.BRACKET_FLAG_END_WHILE_NE:return "]?";
            case Constants.BRACKET_FLAG_END:return "]";
            case Constants.BRACKET_FLAG_BREAK:return "BREAK";
            default:throw new IllegalArgumentException("Unknown Bracket Type:"+type);
        }
    }
}
