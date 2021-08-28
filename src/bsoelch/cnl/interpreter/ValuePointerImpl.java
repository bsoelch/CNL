package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.math.MathObject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

class ValuePointerImpl implements ValuePointer {
    final MathObject value;

    ValuePointerImpl(MathObject value) {
        this.value = value;
    }

    public @NotNull MathObject getValue() {
        return value;
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        Translator.writeValue(target,value);
    }

    @Override
    public String stringRepresentation() {
        return "("+value.toString()+")";
    }
}
