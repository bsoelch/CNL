package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;

import java.io.IOException;

public class Input implements Action {
    final int type;
    public Input(int type) {
        this.type=type;
    }

    @Override
    public boolean requiresArg() {
        return false;
    }

    @Override
    public void pushArg(ValuePointer arg) {
        throw new IllegalStateException("No arguments required");
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        target.write(new long[]{Constants.HEADER_IN},0,Constants.HEADER_IN_LENGTH);

        target.write(new long[]{type},0,2);//TODO better length detection

    }

    @Override
    public String stringRepresentation() {
        return "IN_"+type;//TODO In Types
    }

    public ValuePointer read() {
        return Translator.ZERO;//TODO handle input
    }
}
