package bsoelch.cnl.interpreter;


import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;

import java.io.IOException;
import java.math.BigInteger;

public class Import implements Action {

    final private Context parent;
    final private BigInteger childId;
    private String source;

    public Import(Context parent, BigInteger childId) {
        this.parent =parent;
        this.childId=childId;
    }

    public Context getTarget() {
        return parent.getChild(childId);
    }

    public String getSource() {
        return source;
    }

    @Override
    public boolean requiresArg() {
        return source==null;
    }
    @Override
    public void pushArg(ValuePointer arg) {
        if(source==null){
            source=arg.getValue().asString();
        }else{
            throw new IllegalStateException("No Argument required");
        }
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        target.write(new long[]{Constants.HEADER_IMPORT},0,Constants.HEADER_IMPORT_LENGTH);
        target.writeBigInt(childId,Constants.ENVIRONMENTS_INT_HEADER,Constants.ENVIRONMENTS_INT_BLOCK,Constants.ENVIRONMENTS_INT_BIG_BLOCK);
    }

    @Override
    public String stringRepresentation() {
        return "IMPORT_TO:"+childId;
    }
}
