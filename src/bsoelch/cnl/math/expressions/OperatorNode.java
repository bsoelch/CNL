package bsoelch.cnl.math.expressions;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;
import bsoelch.cnl.interpreter.Translator;
import bsoelch.cnl.math.LambdaExpression;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;

import java.io.IOException;
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
        if((!operator.isNary)&&params.length>operator.minArgs){
            throw new IllegalArgumentException("To much arguments for operator"+operator.name+": "+params.length+
                    " expected:"+operator.minArgs);
        }
        if(operator== ID){
            return params[0];
        }
        NAryInfo nAryInfo=nAryInfo(operator);
        if(nAryInfo!=null&&nAryInfo.isAssociative){//simplify associative n-ary operators
            ArrayList<MathObject> objects=new ArrayList<>(params.length);
            ArrayList<ExpressionNode> nodes=new ArrayList<>(params.length);
            simplify(operator, nAryInfo, params, objects, nodes);
            if(objects.size()>0){//evaluate remaining objects
                evaluateObjects(operator, nAryInfo, objects, nodes);
            }
            if(nodes.isEmpty()){
                return nAryInfo.nilaryReplacement;
            }else {
                OperatorInfo op = nAryInfo.getShortCut(nodes.size());
                if (op != null) {
                    if(op==ID){//unwrap ID
                        return nodes.get(0);
                    }else{
                        return new OperatorNode(op, nodes.toArray(new ExpressionNode[0]));
                    }
                } else {
                    op = nAryInfo.nAryVariant;
                    if (nodes.size() >= op.minArgs) {
                        return new OperatorNode(op, nodes.toArray(new ExpressionNode[0]));
                    } else {
                        throw new IllegalArgumentException("Not enough Arguments for nArg operator:"+op.name);
                    }
                }
            }
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
                        continue;//go to start of loop
                    }
                }
                if(objects.size()>0&&(!nAryInfo.isCommutative)){
                    evaluateObjects(operator, nAryInfo, objects, nodes);
                    objects.clear();
                }
                nodes.add(node);
            }
        }
    }
    private static void evaluateObjects(OperatorInfo operator, NAryInfo nAryInfo, ArrayList<MathObject> objects,
                                        ArrayList<ExpressionNode> nodes) {
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
    public Set<LambdaVariable> freeVariables() {
        HashSet<LambdaVariable> vars=new HashSet<>();
        for(ExpressionNode node:params)
            vars.addAll(node.freeVariables());
        return Collections.unmodifiableSet(vars);
    }

    @Override
    public ExpressionNode renameVariables(Map<LambdaVariable, LambdaVariable> replace) {
        ExpressionNode[] newParams=new ExpressionNode[params.length];
        for(int i=0;i< params.length;i++){
            newParams[i]=params[i].renameVariables(replace);
        }
        return from(operator,newParams);
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
    public MathObject asMathObject() {
        MathObject[] values=new MathObject[params.length];
        for(int i=0;i< params.length;i++)
            values[i]=params[i].asMathObject();
        return operator.execute(null,values);
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
        if(operator.isNary){
            sb.append(':').append(params.length);
        }
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

    public boolean isOperatorReference(LambdaVariable[] boundVariables) {
        NAryInfo info= Constants.Operators.nAryInfo(operator);
        if(info!=null&&info.isCommutative){
            //params are (ignoring their order) the first elements of boundVariables
            HashSet<ExpressionNode> unorderedParams=new HashSet<>(Arrays.asList(params));
            for (LambdaVariable var : boundVariables) {
                if (!unorderedParams.remove(var))
                    return false;
            }
            return unorderedParams.isEmpty();
        }else{
            //params are exactly the first elements of boundVariables
            for(int i=0;i<params.length&&i<boundVariables.length;i++){
                if(!params[i].equals(boundVariables[i]))
                    return false;
            }
            return true;
        }
    }

    public String refString() {
        return "&"+operator.name+(operator.isNary?":"+params.length:"");
    }

    public void writeTo(BitRandomAccessStream target,boolean isOperatorReference) throws IOException {
        if(isOperatorReference){
            Translator.writeOperator(target, operator, params.length, true);
        }else {
            Translator.writeOperator(target, operator, params.length, false);
            for (ExpressionNode param : params)
                LambdaExpression.writeNode(target, param);
        }
    }

    public static int compare(OperatorNode a, Map<LambdaVariable, Integer> boundA, OperatorNode b, Map<LambdaVariable, Integer> boundB) {
        int c=a.operator.id-b.operator.id;
        if(c!=0)
            return c;
        c=a.params.length-b.params.length;
        if(c!=0)
            return c;
        for(int i=0;i<a.params.length;i++){
            c=LambdaExpression.compare(a.params[i],boundA,b.params[i],boundB);
            if(c!=0)
                return c;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperatorNode)) return false;
        OperatorNode that = (OperatorNode) o;
        return operator.id==that.operator.id&&Arrays.equals(params, that.params);
    }

    /**helper class for commutative equals:
     * wraps the logic for comparing node in relation to their parentVars for usage in HashMap*/
    static private class NodeWithVars{
        final ExpressionNode node;
        final Map<LambdaVariable,Integer> parentVars;
        private NodeWithVars(ExpressionNode node, Map<LambdaVariable, Integer> parentVars) {
            this.node = node;
            this.parentVars = parentVars;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NodeWithVars)) return false;
            NodeWithVars that = (NodeWithVars) o;
            return node.equals(parentVars,that.node,that.parentVars);
        }
        @Override
        public int hashCode() {
            return node.hashCode(parentVars);
        }
    }
    @Override
    public boolean equals(Map<LambdaVariable,Integer> parentVars, ExpressionNode other, Map<LambdaVariable,Integer> otherVars) {
        if (this == other) return true;
        if (!(other instanceof OperatorNode)) return false;
        OperatorNode that = (OperatorNode) other;
        if(operator.id!=that.operator.id)
            return false;
        if(params.length!=that.params.length)
            return false;
        NAryInfo info= nAryInfo(operator);
        if(info!=null&&info.isCommutative){//compare commutative operators independent of order
            HashMap<NodeWithVars,Integer> delta=new HashMap<>();
            for (int i = 0; i < params.length; i++) {
                if (!params[i].equals(parentVars, that.params[i], otherVars)) {//ignore equal elements with same position
                    NodeWithVars key = new NodeWithVars(params[i], parentVars);
                    Integer t = delta.get(key);
                    t = t == null ? 1 : t + 1;
                    if (t != 0) {
                        delta.put(key, t);
                    } else {
                        delta.remove(key);
                    }
                    key = new NodeWithVars(that.params[i], otherVars);
                    t = delta.get(key);
                    t = t == null ? -1 : t - 1;
                    if (t != 0) {
                        delta.put(key, t);
                    } else {
                        delta.remove(key);
                    }
                }
            }
            return delta.isEmpty();
        }else {
            for (int i = 0; i < params.length; i++) {
                if (!params[i].equals(parentVars, that.params[i], otherVars))
                    return false;
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        int result = operator.id;
        for(ExpressionNode e:params){
            result=31*result+e.hashCode();
        }
        return result;
    }
    @Override
    public int hashCode(Map<LambdaVariable,Integer> boundVars) {
        int result = operator.id;
        for(ExpressionNode e:params){
            result=31*result+e.hashCode(boundVars);
        }
        return result;
    }

}
