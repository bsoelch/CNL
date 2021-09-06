package bsoelch.cnl.math.expressions;

import bsoelch.cnl.math.NumericValue;
import bsoelch.cnl.math.Real;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LambdaVariable implements ExpressionNode, Comparable<LambdaVariable>{
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
    public Set<LambdaVariable> freeVariables() {
        return Collections.singleton(this);
    }

    @Override
    public ExpressionNode renameVariables(Map<LambdaVariable, LambdaVariable> replace) {
        LambdaVariable tmp = replace.get(this);
        return tmp == null ? this : tmp;
    }

    @Override
    public ExpressionNode evaluate(Map<LambdaVariable, ? extends ExpressionNode> replace) {
        ExpressionNode tmp = replace.get(this);
        return tmp == null ? this : tmp;
    }

    @Override
    public NumericValue asMathObject() {
        return Real.Int.ZERO;
    }

    @Override
    public String toString() {
        return "x" + id;
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
    public int compareTo(@NotNull LambdaVariable o) {
        return id.compareTo(o.id);
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

    @Override
    public boolean equals(Map<LambdaVariable,Integer> parentVars, ExpressionNode other, Map<LambdaVariable,Integer> otherVars) {
        if (!(other instanceof LambdaVariable)) return false;
        Integer iA=parentVars.get(this);
        if(iA!=null){
            return iA.equals(otherVars.get(other));
        }
        return (!otherVars.containsKey(other))&&Objects.equals(id, ((LambdaVariable) other).id);
    }

    @Override
    public int hashCode(Map<LambdaVariable,Integer> parentVars) {
        Integer iA=parentVars.get(this);
        return iA!=null?iA:id.hashCode();
    }

}

