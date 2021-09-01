package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;
import bsoelch.cnl.Main;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;

import java.io.IOException;
import java.math.BigInteger;

public class Input implements Action {
    final int type;
    final BigInteger base;

    public Input(int type, BigInteger base) {
        this.type=type;
        switch (type){
            case Constants.IN_TYPE_CHAR:
            case Constants.IN_TYPE_WORD:
            case Constants.IN_TYPE_LINE:this.base=null;break;
            case Constants.IN_TYPE_BIN:this.base=Constants.BIG_INT_TWO;break;
            case Constants.IN_TYPE_DEC:this.base=BigInteger.TEN;break;
            case Constants.IN_TYPE_DOZ:this.base=Constants.BIG_INT_TWELVE;break;
            case Constants.IN_TYPE_HEX:this.base=Constants.BIG_INT_SIXTEEN;break;
            case Constants.IN_TYPE_BASE_N:
                if(base==null)
                    throw new IllegalArgumentException("Missing Base for BaseN Input");
                if(base.compareTo(Constants.BIG_INT_TWO)<0)
                    throw new IllegalArgumentException("base has to be at least 2");
                this.base=base;
            break;
            default:
                throw new RuntimeException("Unexpected InputType:"+type);
        }
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
        if(base!=null){
            if(base.equals(Constants.BIG_INT_TWO)){
                target.write(new long[]{Constants.IN_TYPE_BIN},0,Constants.IN_TYPES_LENGTH);
            }else if(base.equals(BigInteger.TEN)){
                target.write(new long[]{Constants.IN_TYPE_DEC},0,Constants.IN_TYPES_LENGTH);
            }else if(base.equals(Constants.BIG_INT_TWELVE)){
                target.write(new long[]{Constants.IN_TYPE_DOZ},0,Constants.IN_TYPES_LENGTH);
            }else if(base.equals(Constants.BIG_INT_SIXTEEN)){
                target.write(new long[]{Constants.IN_TYPE_HEX},0,Constants.IN_TYPES_LENGTH);
            }else{
                target.write(new long[]{Constants.IN_TYPE_BASE_N},0,Constants.IN_TYPES_LENGTH);
                target.writeBigInt(base,Constants.IO_INT_HEADER,Constants.IO_INT_BLOCK,Constants.IO_INT_BIG_BLOCK);
            }
        }else{
            switch (type){
                case Constants.IN_TYPE_CHAR:
                case Constants.IN_TYPE_WORD:
                case Constants.IN_TYPE_LINE:
                    target.write(new long[]{type},0,Constants.IN_TYPES_LENGTH);
                    break;
                default:
                    throw new RuntimeException("Unexpected string Input-type:"+type);
            }
        }
    }

    @Override
    public String stringRepresentation() {
        if(base!=null){
            if(base.equals(Constants.BIG_INT_TWO)){
                return "IN_BIN";
            }else if(base.equals(BigInteger.TEN)){
                return "IN_DEC";
            }else if(base.equals(Constants.BIG_INT_TWELVE)){
                return "IN_DOZ";
            }else if(base.equals(Constants.BIG_INT_SIXTEEN)){
                return "IN_HEX";
            }else{
                return "IN_BASE"+base.toString();
            }
        }else{
            switch (type){
                case Constants.IN_TYPE_CHAR:return "IN_CHAR";
                case Constants.IN_TYPE_WORD:return "IN_WORD";
                case Constants.IN_TYPE_LINE:return "IN_LINE";
                default:
                    throw new RuntimeException("Unexpected string Input-type:"+type);
            }
        }
    }

    public ValuePointer read() {
        MathObject value;
        switch (type){
            case Constants.IN_TYPE_CHAR:value=Real.from(Real.stringAsBigInt(Main.readUnicodeChar()));break;
            case Constants.IN_TYPE_WORD:value=Real.from(Real.stringAsBigInt(Main.readWord()));break;
            case Constants.IN_TYPE_LINE:value=Real.from(Real.stringAsBigInt(Main.readLine()));break;
            case Constants.IN_TYPE_BIN:
            case Constants.IN_TYPE_DEC:
            case Constants.IN_TYPE_DOZ:
            case Constants.IN_TYPE_HEX:
            case Constants.IN_TYPE_BASE_N:value=MathObject.FromString.safeFromString(Main.readValue(),base);break;
            default:
                throw new RuntimeException("Unexpected string Input-type:"+type);
        }
        return Translator.wrap(value);
    }
}
