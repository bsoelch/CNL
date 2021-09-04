package bsoelch.cnl.math.expressions;

import bsoelch.cnl.math.NumericValue;
import bsoelch.cnl.math.Real;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LambdaVariable implements ExpressionNode {
    final BigInteger id;

    public LambdaVariable(BigInteger id) {
        this.id = id;
    }

    public BigInteger getId() {
        return id;
    }

    @Override
    public Set<LambdaVariable> variables() {
        return Collections.singleton(this);
    }

    @Override
    public ExpressionNode evaluate(Map<LambdaVariable, ? extends ExpressionNode> replace) {
        ExpressionNode tmp = replace.get(this);
        return tmp == null ? this : tmp;
    }

    @Override
    public NumericValue numericValue() {
        return Real.Int.ZERO;
    }

    @Override
    public String toString() {
        return "λ" + id;//TODO? better string
    }

    @Override
    public String toString(BigInteger base, boolean useSmallBase) {
        return toString();
    }

    @Override
    public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
        return toString();
    }

    @Override
    public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
        return toString();
    }

    @Override
    public String intsAsString() {
        return toString();
    }

    @Override
    public String asString() {
        return "";
    }

    @Override
    public boolean equals(LambdaVariable[] boundVars,ExpressionNode node, LambdaVariable[] nodeVars) {
        if (this == node) return true;
        if (!(node instanceof LambdaVariable)) return false;
        for(int i=0;i<boundVars.length;i++){
            if(id.equals(boundVars[i].id))
                return id.equals(nodeVars[i].id);
        }
        return id.equals(((LambdaVariable) node).id);
    }

    @Override
    public int hashCode(LambdaVariable[] boundVars) {
        for(int i=0;i<boundVars.length;i++){
            if(id.equals(boundVars[i].id))
                return i;
        }
        return hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LambdaVariable)) return false;
        LambdaVariable that = (LambdaVariable) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

