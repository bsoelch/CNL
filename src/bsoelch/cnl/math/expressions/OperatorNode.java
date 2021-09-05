package bsoelch.cnl.math.expressions;

import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.NumericValue;
import bsoelch.cnl.math.Real;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

import static bsoelch.cnl.Constants.Operators.*;

public class OperatorNode implements ExpressionNode{
    final ExpressionNode[] params;
    final OperatorInfo operator;

    public static ExpressionNode from(OperatorInfo operator, ExpressionNode[] params){
        if(operator.isRuntimeOperator())
            throw new IllegalArgumentException("runtime-operators not allowed in Lambda-Expressions");
        if(operator.needsEnvironment())
            throw new IllegalArgumentException("environment dependent-Operators not allowed in Lambda-Expressions");
        if(params.length< operator.minArgs)
            throw new IllegalArgumentException("No enough arguments for operator"+operator.name+": "+params.length+
                    " expected:"+operator.minArgs);
        if(operator.isNary&&params.length>operator.minArgs){
            throw new IllegalArgumentException("To much arguments for operator"+operator.name+": "+params.length+
                    " expected:"+operator.minArgs);
        }
        NAryInfo nAryInfo=nAryInfo(operator);
        if(nAryInfo!=null&&nAryInfo.isAssociative){//simplify associative n-ary operators
            ArrayList<MathObject> objects=new ArrayList<>(params.length);
            ArrayList<ExpressionNode> nodes=new ArrayList<>(params.length);
            simplify(operator, nAryInfo, params, objects, nodes);
            return new OperatorNode(operator, nodes.toArray(new ExpressionNode[0]));
        }else {//check if all elements are MathObjects
            boolean onlyMathObjects = true;
            for (ExpressionNode node : params) {
                if (!(node instanceof MathObject)) {
                    onlyMathObjects = false;
                    break;
                }
            }
            if (onlyMathObjects) {
                return operator.execute(null,Arrays.copyOf(params,params.length,MathObject[].class));
            }
        }
        return new OperatorNode(operator, params);
    }
    private static void simplify(OperatorInfo operator, NAryInfo nAryInfo, ExpressionNode[] params, ArrayList<MathObject> objects, ArrayList<ExpressionNode> nodes) {
        for (ExpressionNode node : params) {
            if (node instanceof MathObject) {
                objects.add((MathObject)node);
            }else{
                if(node instanceof OperatorNode){
                    NAryInfo other=nAryInfo(((OperatorNode) node).operator);
                    if(other==nAryInfo){
                        simplify(operator,nAryInfo,((OperatorNode) node).params,objects,nodes);
                    }
                }
                if(objects.size()>0&&(!nAryInfo.isCommutative)){
                    evaluateObjects(operator, nAryInfo, objects, nodes);
                    objects.clear();
                }
                nodes.add(node);
            }
        }
        if(objects.size()>0){
            evaluateObjects(operator, nAryInfo, objects, nodes);
        }
    }
    private static void evaluateObjects(OperatorInfo operator, NAryInfo nAryInfo, ArrayList<MathObject> objects, ArrayList<ExpressionNode> nodes) {
        OperatorInfo replace = nAryInfo.getShortCut(objects.size());
        MathObject[] mathObjects = objects.toArray(new MathObject[0]);
        if (replace != null) {
            nodes.add(replace.execute(null, mathObjects));
        } else {
            nodes.add(operator.execute(null, mathObjects));
        }
    }

    private OperatorNode(OperatorInfo operator, ExpressionNode[] params) {
        this.operator = operator;
        this.params = params;
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
        for(int i=0;i< params.length;i++){
            newParams[i]=params[i].evaluate(replace);
        }
        return from(operator,newParams);
    }

    @Override
    public NumericValue numericValue() {
        MathObject[] numValues=new MathObject[params.length];
        for(int i=0;i< params.length;i++)
            numValues[i]=params[i].numericValue();
        return operator.execute(null,numValues).numericValue();
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
