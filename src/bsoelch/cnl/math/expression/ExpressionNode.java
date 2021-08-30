package bsoelch.cnl.math.expression;

import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;
import bsoelch.cnl.math.Variable;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

public interface ExpressionNode {//TODO? make Math Object?
    Set<Variable> variables();
    MathObject evaluate(Map<Variable, MathObject> replace);

    String toString(BigInteger base, boolean useSmallBase);
    String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase);
    String toStringFloat(BigInteger base, Real precision, boolean useSmallBase);
}
