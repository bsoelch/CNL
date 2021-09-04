package bsoelch.cnl.math;

import bsoelch.cnl.math.expressions.ExpressionNode;
import bsoelch.cnl.math.expressions.LambdaVariable;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

//TODO add to MathObject
public final class LambdaExpression extends MathObject {
    final ExpressionNode node;
    final LambdaVariable[] boundVariables;

    public LambdaExpression(ExpressionNode node, LambdaVariable[] boundVariables) {
        this.node = node;
        Set<LambdaVariable> nodeVars=node.variables();
        int lastIn=boundVariables.length-1;
        while(!nodeVars.contains(boundVariables[lastIn])){
            lastIn--;
        }//remove bindings to nonexistent variables (at end)
        //key all other bindings to not disturb the positions of the existing bindings
        this.boundVariables = Arrays.copyOf(boundVariables,lastIn);
    }

    @Override
    public Set<LambdaVariable> variables() {
        return super.variables();
    }
    @Override
    public MathObject evaluate(Map<LambdaVariable, ? extends ExpressionNode> replace) {
        return super.evaluate(replace);
    }

    @Override
    public NumericValue numericValue() {
        return node.numericValue();
    }

    @Override
    public String toString(BigInteger base, boolean useSmallBase) {
        return toString(node.toString(base,useSmallBase));
    }

    @Override
    public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
        return toString(node.toStringFixedPoint(base,precision,useSmallBase));
    }

    @Override
    public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
        return toString(node.toStringFloat(base,precision,useSmallBase));
    }
    @Override
    public String intsAsString() {
        return toString(node.intsAsString());
    }
    private String toString(String nodeString){
        StringBuilder ret=new StringBuilder("LAMBDA:");
        for(LambdaVariable var:boundVariables){
            if(ret.length()>7)
                ret.append(",");
            ret.append(var.getId());
        }
        return ret.append(" (").append(nodeString).append(')').toString();
    }

    @Override
    public String asString() {
        return node.asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LambdaExpression)) return false;
        //to expressions with same structure but different names for bound vars are equivalent
        return node.equals(boundVariables,((LambdaExpression) o).node,((LambdaExpression) o).boundVariables);
    }
    @Override
    public int hashCode() {
        //respect bound variables when calculating hashCode
        return node.hashCode(boundVariables);
    }

    @Override
    public boolean equals(LambdaVariable[] thisSupVars,ExpressionNode node, LambdaVariable[] nodeSupVars) {
        if (this == node) return true;
        if (!(node instanceof LambdaExpression)) return false;
        if(boundVariables.length!=((LambdaExpression) node).boundVariables.length)
            return false;
        return node.equals(concat(boundVariables,thisSupVars),
                ((LambdaExpression) node).node,concat(((LambdaExpression) node).boundVariables,nodeSupVars));
    }

    @Override
    public int hashCode(LambdaVariable[] superVars) {
        return node.hashCode(concat(boundVariables,superVars));
    }

    @NotNull
    private static LambdaVariable[] concat(LambdaVariable[] left, LambdaVariable[] right) {
        LambdaVariable[] target = new LambdaVariable[left.length + right.length];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(left, 0, target, left.length, right.length);
        return target;
    }
}
