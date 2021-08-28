package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;

import java.io.IOException;

interface Action {
    boolean requiresArg();

    default boolean acceptsArg(int flags) {
        return requiresArg();
    }

    void pushArg(ValuePointer arg);

    void writeTo(BitRandomAccessStream target) throws IOException;
    String stringRepresentation();
}
