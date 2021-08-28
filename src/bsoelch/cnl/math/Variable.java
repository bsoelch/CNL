package bsoelch.cnl.math;

import bsoelch.cnl.math.expression.ExpressionNode;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Variable implements Comparable<Variable> , ExpressionNode {

    final BigInteger id;

    Variable(BigInteger id) {
        this.id=id;
    }

    @Override
    public String toString() {
        return "[" + id + "]";
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variable)) return false;
        Variable variable = (Variable) o;
        return id.equals(variable.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(@NotNull Variable o) {
        return id.compareTo(o.id);
    }

    @Override
    public Set<Variable> variables() {
        return Collections.singleton(this);
    }

    @Override
    public ExpressionNode evaluate(Map<Variable, ExpressionNode> replace) {
        return Objects.requireNonNull(replace.get(this));
    }

}

