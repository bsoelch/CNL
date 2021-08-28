package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;

import java.io.IOException;
import java.math.BigInteger;


public class RunIn implements Action{
    final BigInteger childId;

    public RunIn(BigInteger childId) {
        this.childId = childId;
    }
    /**runIn global*/
    public RunIn() {
        this.childId = null;
    }

    @Override
    public boolean requiresArg() {
        return false;
    }
    @Override
    public void pushArg(ValuePointer arg) {
        throw new IllegalStateException("No Argument required");
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        target.write(new long[]{Constants.HEADER_ENVIRONMENT},0, Constants.HEADER_ENVIRONMENT_LENGTH);
        if(childId==null){
            target.writeBigInt(BigInteger.ZERO, Constants.ENVIRONMENTS_INT_HEADER, Constants.ENVIRONMENTS_INT_BLOCK, Constants.ENVIRONMENTS_INT_BIG_BLOCK);
        }else{
            target.writeBigInt(childId.add(BigInteger.ONE), Constants.ENVIRONMENTS_INT_HEADER, Constants.ENVIRONMENTS_INT_BLOCK, Constants.ENVIRONMENTS_INT_BIG_BLOCK);
        }
    }

    @Override
    public String stringRepresentation() {
        if(childId==null){
            return "RUN_ROOT";
        }else{
            return "RUN_IN:"+childId.toString();
        }
    }
}
