package bsoelch.cnl.math;

import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Constants;
import bsoelch.cnl.interpreter.Translator;
import bsoelch.cnl.math.expressions.ExpressionNode;
import bsoelch.cnl.math.expressions.LambdaVariable;
import bsoelch.cnl.math.expressions.OperatorNode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**LambdaExpression is a class for representing a lambda-expressions,
 * which in this Program are Trees of {@link bsoelch.cnl.math.expressions.OperatorNode OperatorNodes}
 * with {@link bsoelch.cnl.math.expressions.LambdaVariable LambdaVariables}
 * and {@link bsoelch.cnl.math.MathObject MathObjects} as leaves.
 * <br>
 * Operators with a LambdaExpression as an parameter directly redirect the calculation
 * to the {@link #from(Constants.Operators.OperatorInfo, MathObject[])} Method in this class
 * */
public final class LambdaExpression extends MathObject {
    final ExpressionNode node;
    final LambdaVariable[] boundVariables;

   public static MathObject from(ExpressionNode node, LambdaVariable[] bindVars){
       if(node instanceof MathObject&&bindVars.length==0){
           return (MathObject) node;//unwrap unbound lambdas
       }else if(node instanceof LambdaExpression&&((LambdaExpression) node).boundVariables.length==0){
           node=((LambdaExpression) node).node;//unwrap unbound lambdas
       }
       return new LambdaExpression(node, bindVars);
    }

    public static MathObject from(Constants.Operators.OperatorInfo operatorInfo, MathObject[] params){
        //vars: variables that are unbound in at least one subexpression
        TreeSet<LambdaVariable> vars=new TreeSet<>(),tmp;
        int maxBound=0;
        for(MathObject o:params){
            if(o instanceof LambdaExpression){
                tmp=new TreeSet<>(o.variables());
                tmp.removeAll(Arrays.asList(((LambdaExpression) o).boundVariables));
                vars.addAll(tmp);
                maxBound=Math.max(maxBound,((LambdaExpression) o).boundVariables.length);
            }
        }
        LambdaVariable[] bindVars=new LambdaVariable[maxBound];
        long j=0;
        for(int i=0;i<bindVars.length;i++){
            do {
                bindVars[i] = new LambdaVariable(BigInteger.valueOf(j++));
            }while (vars.contains(bindVars[i]));
        }
        ExpressionNode[] unwrap=new ExpressionNode[params.length];
        for(int i=0;i<params.length;i++){
            if(params[i] instanceof LambdaExpression){
                if(((LambdaExpression) params[i]).boundVariables.length>0) {
                    HashMap<LambdaVariable, LambdaVariable> replace = new HashMap<>();
                    for (int k = 0; k < ((LambdaExpression) params[i]).boundVariables.length; k++) {
                        if(!((LambdaExpression) params[i]).boundVariables[k].equals(bindVars[k])) {
                            replace.put(((LambdaExpression) params[i]).boundVariables[k], bindVars[k]);
                        }
                    }
                    unwrap[i] = ((LambdaExpression) params[i]).node.renameVariables(replace);
                }else{
                    unwrap[i]=((LambdaExpression) params[i]).node;
                }
            }else{
                unwrap[i]=params[i];
            }
        }
        return from(OperatorNode.from(operatorInfo,unwrap),bindVars);
    }

    private LambdaExpression(ExpressionNode node, LambdaVariable[] boundVariables) {
        this.node = node;
        this.boundVariables=boundVariables;//unused bound vars  are necessary for lambda calculus
    }



    @Override
    public Set<LambdaVariable> variables() {
        HashSet<LambdaVariable> vars=new HashSet<>(node.variables());
        vars.addAll(Arrays.asList(boundVariables));
        return vars;
    }
    @Override
    public Set<LambdaVariable> freeVariables() {
        HashSet<LambdaVariable> vars=new HashSet<>(node.freeVariables());
        vars.removeAll(Arrays.asList(boundVariables));
        return Collections.unmodifiableSet(vars);
    }
    @Override
    public MathObject renameVariables(Map<LambdaVariable, LambdaVariable> replace) {
        HashMap<LambdaVariable,LambdaVariable> replaceUnbound=new HashMap<>(replace);
        for(LambdaVariable var:boundVariables)
            replaceUnbound.remove(var);
        return from(node.renameVariables(replaceUnbound),boundVariables);
    }

    @Override
    public MathObject evaluate(Map<LambdaVariable, ? extends ExpressionNode> replace) {
        HashMap<LambdaVariable,? extends ExpressionNode> replaceUnbound=new HashMap<>(replace);
        for(LambdaVariable var:boundVariables)
            replaceUnbound.remove(var);
        return from(node.evaluate(replaceUnbound),boundVariables);
    }
    /**evaluates this LambdaExpression with the specific input values
     * @param mathObjects the values used as parameters for the evaluation*/
    public MathObject evaluate(MathObject[] mathObjects) {
        HashMap<LambdaVariable,ExpressionNode> replace=new HashMap<>(mathObjects.length);
        ExpressionNode tmp;
        for(int i=0;i<mathObjects.length;i++){
            if(i>= boundVariables.length)
                break;
            tmp=mathObjects[i];
            if(tmp instanceof LambdaExpression&&((LambdaExpression) tmp).boundVariables.length==0){
                tmp=((LambdaExpression) tmp).node;//unwrap unbound lambdas
            }
            replace.put(boundVariables[i],tmp );
        }
        LambdaVariable[] newParams;
        if(mathObjects.length<boundVariables.length) {
            newParams = Arrays.copyOfRange(boundVariables, mathObjects.length, boundVariables.length);
            mathObjects=new MathObject[0];
        }else{
            newParams=new LambdaVariable[0];
            mathObjects=Arrays.copyOfRange(mathObjects, boundVariables.length, mathObjects.length);
        }
        MathObject res=from(node.evaluate(replace), newParams);
        if(mathObjects.length>0&&res instanceof LambdaExpression&&((LambdaExpression) res).boundVariables.length>0){
            return ((LambdaExpression) res).evaluate(mathObjects);
        }else {
            return res;
        }
    }

    @Override
    public NumericValue numericValue() {
        return node.asMathObject().numericValue();
    }
    @Override
    public MathObject asMathObject(){
        return node.asMathObject();
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
        if(boundVariables.length==0){
            return nodeString;
        }else {
            StringBuilder ret = new StringBuilder("LAMBDA:");
            for (LambdaVariable var : boundVariables) {
                if (ret.length() > 7)
                    ret.append(",");
                ret.append(var.getId());
            }
            return ret.append(" (").append(nodeString).append(')').toString();
        }
    }
    @Override
    public String asString() {
        return node.asString();
    }
    @Override
    public String toString() {
        if(node instanceof OperatorNode&&((OperatorNode)node).isOperatorReference(boundVariables)){
            return ((OperatorNode)node).refString();
        }else{
            return super.toString();
        }
    }

    public void writeTo(BitRandomAccessStream target) throws IOException {
        if(node instanceof OperatorNode&&((OperatorNode)node).isOperatorReference(boundVariables)){
            ((OperatorNode) node).writeTo(target,true);
        }else {
            if (boundVariables.length > 0) {
                Translator.writeLambdaHeader(target, boundVariables);
            }
            writeNode(target, node);
        }
    }
    public static void writeNode(BitRandomAccessStream target, ExpressionNode node) throws IOException {
        if(node instanceof MathObject){
            Translator.writeValue(target,(MathObject) node);
        }else if(node instanceof LambdaVariable){
            Translator.writeLambdaVariable(target,(LambdaVariable) node);
        }else if(node instanceof OperatorNode){
            ((OperatorNode)node).writeTo(target,false);
        }else{
            throw new IllegalArgumentException("Unexpected ExpressionNode class:"+node.getClass());
        }
    }

    public int compareTo(LambdaExpression b) {
        return compare(node,toMap(boundVariables),b.node,toMap(b.boundVariables));
    }
    public static int compare(ExpressionNode a,Map<LambdaVariable,Integer> boundA, ExpressionNode b,Map<LambdaVariable,Integer> boundB) {
       if(a instanceof MathObject){
            if(b instanceof MathObject){
                if(a instanceof LambdaExpression){
                    if(b instanceof LambdaExpression){
                        return compare(((LambdaExpression) a).node,concat(((LambdaExpression) a).boundVariables,boundA),
                                ((LambdaExpression) b).node,concat(((LambdaExpression) b).boundVariables,boundB));
                    }else{
                        return 1;
                    }
                }else if(b instanceof LambdaExpression){
                    return -1;
                }else {
                    return MathObject.compare((MathObject) a, (MathObject) b);
                }
            }else{
                return -1;
            }
        }else if(a instanceof LambdaVariable){
            if(b instanceof MathObject){
                return 1;
            }else if(b instanceof LambdaVariable){
                Integer iA=boundA.get(a),iB=boundB.get(b);
                if(iA!=null){
                    if(iB!=null){
                        return iA.compareTo(iB);
                    }else {
                        return -1;
                    }
                }else if(iB!=null){
                    return 1;
                }else {
                    return ((LambdaVariable) a).getId().compareTo(((LambdaVariable) b).getId());
                }
            }else {
                return -1;
            }
        }else if(a instanceof OperatorNode){
            if(b instanceof MathObject||b instanceof LambdaVariable){
                return 1;
            }else if(b instanceof OperatorNode){
                return OperatorNode.compare(((OperatorNode)a),boundA,(OperatorNode)b,boundB);
            }else{
                throw new IllegalArgumentException("Unexpected ExpressionNode class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected ExpressionNode class:"+a.getClass());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LambdaExpression)) return false;
        return node.equals(toMap(boundVariables),((LambdaExpression) o).node,toMap(((LambdaExpression) o).boundVariables));
    }

    @Override
    public boolean equals(Map<LambdaVariable,Integer> parentVars, ExpressionNode other, Map<LambdaVariable,Integer> otherVars) {
        if(!(other instanceof LambdaExpression)) return false;
        if(boundVariables.length!=((LambdaExpression) other).boundVariables.length)
            return false;
        return node.equals(concat(boundVariables,parentVars),
                ((LambdaExpression) other).node,concat(((LambdaExpression) other).boundVariables,otherVars));
    }

    @Override
    public int hashCode() {
        return node.hashCode(toMap(boundVariables));
    }
    @Override
    public int hashCode(Map<LambdaVariable,Integer> parentVars) {
        return node.hashCode(concat(boundVariables,parentVars));
    }

    private static HashMap<LambdaVariable,Integer> toMap(LambdaVariable[] vars){
       HashMap<LambdaVariable,Integer> map=new HashMap<>(vars.length);
       for(int i=0;i<vars.length;i++){
           map.put(vars[i],i);
       }
       return map;
    }

    private static Map<LambdaVariable,Integer> concat(LambdaVariable[] vars, Map<LambdaVariable,Integer> parent) {
        int parentSize = parent.size();
        HashMap<LambdaVariable,Integer> map=new HashMap<>(parentSize+vars.length);
        map.putAll(parent);
        for(int i=0;i<vars.length;i++){
            map.put(vars[i],i+ parentSize);
        }
        return map;
    }

}
