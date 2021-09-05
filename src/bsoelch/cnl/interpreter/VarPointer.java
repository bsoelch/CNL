package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigInteger;

import static bsoelch.cnl.Constants.*;

class VarPointer implements ValuePointer {
    private final Context myEnv;
    private MathObject varId;
    private final boolean isStatic;
    private boolean active = true;

    VarPointer(Context env, @Nullable MathObject id) {
        myEnv = env;
        this.varId = id;
        isStatic = (id != null);
    }

    public @NotNull MathObject getValue() {
        return myEnv.getVar(varId);
    }

    @Override
    public boolean requiresArg() {
        return varId == null;
    }

    @Override
    public boolean acceptsArg(int flags) {//allow second argument if root or empty and in var-chain
        return requiresArg() || ((active && (flags & Interpreter.FLAG_ROOT) == Interpreter.FLAG_ROOT) ||
                (isStatic && ((flags & Interpreter.FLAG_VAR_DECLARATION_CHAIN) == Interpreter.FLAG_VAR_DECLARATION_CHAIN && !myEnv.hasVar(varId))));
    }

    public void setValue(MathObject newValue) {
        myEnv.putVar(varId, newValue);
    }

    @Override
    public void pushArg(ValuePointer arg) {
        if (varId == null) {
            varId = arg.getValue();
        } else {
            myEnv.putVar(varId, arg.getValue());
            active = false;
        }
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        if(varId!=null&&varId instanceof Real.Int &&((Real.Int) varId).compareTo(Real.Int.ZERO)>=0){//dynamic var
            target.write(new long[]{HEADER_VAR}, 0, HEADER_VAR_LENGTH);
            target.writeBigInt(((Real.Int) varId).num(), VAR_INT_HEADER, VAR_INT_BLOCK
                    , VAR_INT_BIG_BLOCK);
        }else {
            target.write(new long[]{HEADER_OPERATOR}, 0, HEADER_OPERATOR_LENGTH);
            target.writeBigInt(BigInteger.valueOf(Operators.byName(Operators.DYNAMIC_VAR).id),
                    Constants.OPERATOR_INT_HEADER,Constants.OPERATOR_INT_BLOCK,Constants.OPERATOR_INT_BIG_BLOCK);
            if(varId!=null){
                Translator.writeValue(target, varId);
            }
        }
    }

    @Override
    public String stringRepresentation() {
        if(varId!=null&&varId instanceof Real.Int&&((Real.Int) varId).compareTo(Real.Int.ZERO)>=0){
            return "VAR" + varId;
        }else {
            String ret="DYNAMIC_VAR";
            if(varId!=null){
                ret+=" ("+varId.toString()+")";
            }
            return ret;
        }
    }
}
