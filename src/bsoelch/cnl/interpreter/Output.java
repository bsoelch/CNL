package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;
import bsoelch.cnl.math.Real;

import java.io.IOException;
import java.math.BigInteger;

import static bsoelch.cnl.Constants.*;

class Output implements Action {
    final boolean isNumber,useSmallBase;
    final int type;
    final BigInteger base;

    ValuePointer value;
    Real precision;

    public Output(boolean isNumber, boolean useSmallBase, BigInteger base, int type) {
        this.isNumber=isNumber;
        this.useSmallBase=useSmallBase;
        this.base=base;
        this.type=type;
        if(isNumber){
            switch (type){
                case OUT_FLAG_FIXED_POINT:
                case OUT_FLAG_FLOAT:break;
                default:precision=Real.Int.ZERO;
            }
        }
    }

    @Override
    public boolean requiresArg() {
        return value == null || (isNumber && precision == null);
    }

    @Override
    public void pushArg(ValuePointer arg) {
        if (value == null) {
            this.value = arg;
        } else if (isNumber&& precision == null) {
            precision = arg.getValue().scalarValue().realPart();
        } else {
            throw new IllegalStateException("No Argument required");
        }
    }

    ValuePointer write() {
        if (requiresArg())
            throw new IllegalStateException("Argument required");
        if (isNumber) {
            switch (type){
                case OUT_FLAG_FRACTION:{//fraction
                    System.out.print(value.getValue().toString(base, useSmallBase));
                    return value;
                }
                case OUT_FLAG_FIXED_POINT_EXACT:
                case OUT_FLAG_FIXED_POINT:{//fixed Point
                    System.out.print(value.getValue()
                            .toStringFixedPoint(base, precision, useSmallBase));
                    return value;
                }
                case OUT_FLAG_FLOAT_EXACT:
                case OUT_FLAG_FLOAT:{//floating Point
                    System.out.print(value.getValue()
                            .toStringFloat(base, precision, useSmallBase));
                    return value;
                }
                default:throw new IllegalStateException("Unknown OutputType:"+type);
            }
        } else {
            if (type == OUT_STR) {
                System.out.print(value.getValue().asString());
            } else if (type == OUT_STR_INT) {
                System.out.print(value.getValue().intsAsString());
            } else {
                throw new IllegalArgumentException("Unexpected StringOut type:" + type);
            }
            return value;
        }
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        target.write(new long[]{HEADER_OUT},0, HEADER_OUT_LENGTH);
        int baseId;
        if(base.equals(BIG_INT_TWO)){
            baseId=OUT_BASE_FLAG_BIN;
        }else if(base.equals(BigInteger.TEN)){
            baseId=OUT_BASE_FLAG_DEC;
        }else if(base.equals(BIG_INT_TWELVE)){
            baseId=OUT_BASE_FLAG_DOZ;
        }else if(base.equals(BIG_INT_SIXTEEN)){
            baseId=OUT_BASE_FLAG_HEX;
        }else{
            baseId=OUT_BASE_FLAG_BASE_N;
        }
        int type2=isNumber?(useSmallBase?0:OUT_BIG_BASE_START)+baseId*OUT_NUMBER_BLOCK_LENGTH+type:type;

        Constants.writeOutId(target,type2);
    }

    @Override
    public String stringRepresentation() {
        if(isNumber){
            String ret="OUT_NUMBER";
            if(!useSmallBase){
                ret+="_BIG_BASE";
            }
            if(base.equals(BIG_INT_TWO)){
                ret+="_BIN";
            }else if(base.equals(BIG_INT_TWELVE)){
                ret+="_DOZ";
            }else if(base.equals(BIG_INT_SIXTEEN)){
                ret+="_HEX";
            }else if(!base.equals(BigInteger.TEN)){
                ret+="_BASE"+base.toString();
            }
            switch (type){
                case OUT_FLAG_FRACTION:return ret;
                case OUT_FLAG_FIXED_POINT:return ret+"_FIXED_APPROX";
                case OUT_FLAG_FLOAT:return ret+"_FLOAT_APPROX";
                case OUT_FLAG_FIXED_POINT_EXACT:return ret+"_FIXED";
                case OUT_FLAG_FLOAT_EXACT:return ret+"_FLOAT";
                default:throw new IllegalArgumentException("Illegal output-Flag:"+type);
            }
        }else{
            switch (type){
                case OUT_STR:return "OUT_STR";
                case OUT_STR_INT:return "OUT_STR_INT";
                default:throw new IllegalArgumentException("Illegal type for OUT_STR:"+type);
            }

        }
    }
}
