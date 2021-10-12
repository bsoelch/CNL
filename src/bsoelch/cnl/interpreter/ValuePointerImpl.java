package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.math.LambdaExpression;
import bsoelch.cnl.math.MathObject;

import java.io.IOException;

class ValuePointerImpl implements ValuePointer {
    final MathObject value;

    ValuePointerImpl(MathObject value) {
        this.value = value;
    }

    public MathObject getValue() {
        return value;
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        Translator.writeValue(target,value);
    }

    @Override
    public String stringRepresentation() {
        if(value instanceof LambdaExpression){
            return value.toString();
        }else {
            return "(" + value.toString() + ")";
        }
    }
}
