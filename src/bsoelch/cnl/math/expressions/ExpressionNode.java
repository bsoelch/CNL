package bsoelch.cnl.math.expressions;

import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.NumericValue;
import bsoelch.cnl.math.Real;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

public interface ExpressionNode {
    Set<LambdaVariable> variables();
    Set<LambdaVariable> freeVariables();

    ExpressionNode renameVariables(Map<LambdaVariable,LambdaVariable> replace);

    ExpressionNode evaluate(Map<LambdaVariable,? extends ExpressionNode> replace);

    MathObject asMathObject();

    String toString(BigInteger base, boolean useSmallBase);
    String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase);
    String toStringFloat(BigInteger base, Real precision, boolean useSmallBase);

    /**String representing this Object*/
    String asString();
    /**String representation of this if all ints are replaced with their String-value*/
    String intsAsString();

}
