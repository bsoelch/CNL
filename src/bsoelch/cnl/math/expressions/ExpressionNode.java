package bsoelch.cnl.math.expressions;

import bsoelch.cnl.math.MathObject;
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

    int hashCode(Map<LambdaVariable,Integer> boundVars);
    /**returns true if this Expression node has the same structure and unbound variables as other
     * @param other the Expression node with that Equality should be checked
     * @param parentVars Variables bound to a LambdaExpression containing this element
     *                   (in the form var->Index)
     * @param otherVars Variables bound to a LambdaExpression containing other element
     *                   (in the form var->Index)
     * */
    boolean equals(Map<LambdaVariable,Integer> parentVars,ExpressionNode other,Map<LambdaVariable,Integer> otherVars);
}
