package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.math.LambdaExpression;
import bsoelch.cnl.math.expressions.LambdaVariable;

import java.io.IOException;


public class BindLambda implements Action {
    ValuePointer arg;
    final LambdaVariable[] vars;

    public BindLambda(LambdaVariable[] vars) {
        this.vars=vars;
        if(vars.length==0)
            throw new IllegalArgumentException("Bind Lambda has to bind at least one Variable");
    }

    public ValuePointer preformOperation() {
        if (requiresArg())
            throw new IllegalStateException("Missing Argument");
        return Translator.wrap(LambdaExpression.from(arg.getValue(),vars));
    }

    @Override
    public void pushArg(ValuePointer arg) {
        if(this.arg==null)
            this.arg=arg;
        else
            throw new IllegalStateException("No Argument required");
    }

    @Override
    public boolean requiresArg() {
        return arg==null;
    }

    @Override
    public void writeTo(BitRandomAccessStream target) throws IOException {
        Translator.writeLambdaHeader(target, vars);
    }

    @Override
    public String stringRepresentation() {
        StringBuilder sb=new StringBuilder("LAMBDA:");
        for (LambdaVariable var : vars) {
            if(sb.length()>7)
                sb.append(',');
            sb.append(var.getId());
        }
        return sb.toString();
    }
}
