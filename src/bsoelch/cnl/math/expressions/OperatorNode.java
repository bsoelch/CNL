package bsoelch.cnl.math.expressions;

import bsoelch.cnl.Constants;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.NumericValue;
import bsoelch.cnl.math.Real;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

public class OperatorNode implements ExpressionNode{
    final ExpressionNode[] params;
    final Constants.Operators.OperatorInfo operator;

    public OperatorNode(Constants.Operators.OperatorInfo operator, ExpressionNode[] params) {
        int flags=operator.executionInfo.flags();
        if((flags& Constants.Operators.FLAG_DYNAMIC)!=0)
            throw new IllegalArgumentException("dynamic Operators not allowed in Lambda-Expressions");
        if((flags& Constants.Operators.FLAG_NEEDS_ENVIRONMENT)!=0)
            throw new IllegalArgumentException("environment dependent-Operators not allowed in Lambda-Expressions");
        this.operator = operator;
        this.params = params;
        if(params.length< operator.executionInfo.argCount())
            throw new IllegalArgumentException("No enough arguments for operator"+operator.name+": "+params.length+
                    " expected:"+operator.executionInfo.argCount());
        if((flags& Constants.Operators.FLAG_NARY)==0&&params.length>operator.executionInfo.argCount()){
            throw new IllegalArgumentException("To much arguments for operator"+operator.name+": "+params.length+
                    " expected:"+operator.executionInfo.argCount());
        }
        //TODO ensure that no further evaluation of params is possible
    }

    @Override
    public Set<LambdaVariable> variables() {
        HashSet<LambdaVariable> vars=new HashSet<>();
        for(ExpressionNode node:params)
            vars.addAll(node.variables());
        return Collections.unmodifiableSet(vars);
    }

    @Override
    public ExpressionNode evaluate(Map<LambdaVariable, ? extends ExpressionNode> replace) {
        ExpressionNode[] newParams=new ExpressionNode[params.length];
        boolean noVars=true;
        for(int i=0;i< params.length;i++){
            newParams[i]=params[i].evaluate(replace);
            if(!(newParams[i] instanceof MathObject))
                noVars=false;
        }
        if(noVars){
            return operator.executionInfo.execute(null, Arrays.copyOf(newParams,newParams.length,MathObject[].class));
        }
        //TODO simplify nAry operators where possible
        return new OperatorNode(operator,newParams);
    }

    @Override
    public NumericValue numericValue() {
        MathObject[] numValues=new MathObject[params.length];
        for(int i=0;i< params.length;i++)
            numValues[i]=params[i].numericValue();
        return operator.executionInfo.execute(null,numValues).numericValue();
    }

    @Override
    public String toString(BigInteger base, boolean useSmallBase) {
        return toString(n->n.toString(base, useSmallBase));
    }

    @Override
    public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
        return toString(n->n.toStringFixedPoint(base,precision, useSmallBase));
    }

    @Override
    public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
        return toString(n->n.toStringFloat(base,precision, useSmallBase));
    }

    @Override
    public String intsAsString() {
        return toString(ExpressionNode::intsAsString);
    }

    private String toString(Function<ExpressionNode,String> nodeToString){
        StringBuilder sb=new StringBuilder(operator.name);
        for (ExpressionNode param : params) {
            sb.append(' ').append(nodeToString.apply(param));
        }
        return sb.toString();
    }

    @Override
    public String asString() {
        StringBuilder sb=new StringBuilder();
        for (ExpressionNode param : params) {
            sb.append(param.asString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(LambdaVariable[] boundVars, ExpressionNode node, LambdaVariable[] nodeVars) {
        if (this == node) return true;
        if (!(node instanceof OperatorNode)) return false;
        OperatorNode that = (OperatorNode) node;
        if(operator.id!=that.operator.id)
            return false;
        if(params.length!=that.params.length)
            return false;
        for(int i=0;i< params.length;i++){
            if(!params[i].equals(boundVars,((OperatorNode) node).params[i],nodeVars))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode(LambdaVariable[] boundVars) {
        int result = operator.id;
        for(ExpressionNode e:params){
            result=31*result+e.hashCode(boundVars);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperatorNode)) return false;
        OperatorNode that = (OperatorNode) o;
        return operator.id==that.operator.id&&Arrays.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        int result = operator.id;
        for(ExpressionNode e:params){
            result=31*result+e.hashCode();
        }
        return result;
    }
}
