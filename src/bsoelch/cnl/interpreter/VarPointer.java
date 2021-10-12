package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.NumericValue;
import bsoelch.cnl.math.Real;

import java.io.IOException;
import java.math.BigInteger;

import static bsoelch.cnl.Constants.*;

class VarPointer implements ValuePointer {
    private final Context myEnv;
    private MathObject varId;
    private final boolean forceWrite;
    private boolean active = true;

    /**Creates a Pointer to a Variable with a specific id
     * @param env program context
     * @param id id of this Var or null if this is a dynamic-var
     * @param forceWrite true if this operation has to modify a variable operations*/
    VarPointer(Context env, NumericValue id,boolean forceWrite) {
        myEnv = env;
        this.varId = id;
        this.forceWrite=forceWrite;
    }

    public MathObject getValue() {
        return myEnv.getVar(varId);
    }

    @Override
    public boolean requiresArg() {
        return varId == null||(forceWrite&&active);
    }

    @Override
    public boolean acceptsArg(int flags) {//allow second argument if root
        return requiresArg() || (active &&(forceWrite||(flags & Interpreter.FLAG_ROOT) == Interpreter.FLAG_ROOT));
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
            if(forceWrite){
                target.write(new long[]{HEADER_OPERATOR}, 0, HEADER_OPERATOR_LENGTH);
                target.writeBigInt(BigInteger.valueOf(Operators.byName(Operators.WRITE_VAR).id),
                        Constants.OPERATOR_INT_HEADER,Constants.OPERATOR_INT_BLOCK,Constants.OPERATOR_INT_BIG_BLOCK);
            }else {
                target.write(new long[]{HEADER_VAR}, 0, HEADER_VAR_LENGTH);
            }
            target.writeBigInt(((Real.Int)varId).num(), VAR_INT_HEADER, VAR_INT_BLOCK
                    , VAR_INT_BIG_BLOCK);
        }else {
            target.write(new long[]{HEADER_OPERATOR}, 0, HEADER_OPERATOR_LENGTH);
            target.writeBigInt(BigInteger.valueOf(Operators.byName(forceWrite?Operators.WRITE_DYNAMIC_VAR:Operators.DYNAMIC_VAR).id),
                    Constants.OPERATOR_INT_HEADER,Constants.OPERATOR_INT_BLOCK,Constants.OPERATOR_INT_BIG_BLOCK);
            if(varId!=null){
                Translator.writeValue(target, varId);
            }
        }
    }

    @Override
    public String stringRepresentation() {
        if(varId!=null&&varId instanceof Real.Int&&((Real.Int) varId).compareTo(Real.Int.ZERO)>=0){
            return forceWrite?Operators.WRITE_VAR:"VAR" + varId;
        }else {
            String ret=forceWrite?Operators.WRITE_DYNAMIC_VAR:Operators.DYNAMIC_VAR;
            if(varId!=null){
                ret+=" ("+varId.toString()+")";
            }
            return ret;
        }
    }
}
