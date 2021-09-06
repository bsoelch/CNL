package bsoelch.cnl.math;

import bsoelch.cnl.Constants;
import bsoelch.cnl.math.expressions.ExpressionNode;
import bsoelch.cnl.math.expressions.LambdaVariable;
import bsoelch.cnl.math.expressions.OperatorNode;

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
        HashSet<LambdaVariable> freeVars=new HashSet<>(node.variables());
        freeVars.removeAll(Arrays.asList(bindVars));
        if(freeVars.isEmpty()){
            return (MathObject) node;
        }else{
            HashMap<LambdaVariable,LambdaVariable> remap=new HashMap<>();
            LambdaVariable prev;
            long j=0;
            for(int i=0;i<bindVars.length;i++){
                prev=bindVars[i];
                do {
                    bindVars[i] = new LambdaVariable(BigInteger.valueOf(j++));
                }while (freeVars.contains(bindVars[i]));
                remap.put(prev,bindVars[i]);
            }
            return new LambdaExpression(node.renameVariables(remap), bindVars);
        }
    }

    public static MathObject from(Constants.Operators.OperatorInfo operatorInfo, MathObject[] params){
        HashSet<LambdaVariable> vars=new HashSet<>();
        HashSet<LambdaVariable> tmp;
        int maxLambdaArgs=0;
        for(MathObject o:params){
            if(o instanceof LambdaExpression){
                tmp=new HashSet<>(o.variables());
                tmp.removeAll(Arrays.asList(((LambdaExpression) o).boundVariables));
                vars.addAll(tmp);//add all not directly bound variables
                maxLambdaArgs=Math.max(maxLambdaArgs,((LambdaExpression) o).boundVariables.length);
            }
        }
        if(vars.isEmpty()&&!operatorInfo.unwrapBoundLambdas){
            return operatorInfo.execute(null,params);
        }
        LambdaVariable[] boundVars=new LambdaVariable[maxLambdaArgs];
        long j=0;
        for(int i=0;i<boundVars.length;i++){
            do {
                boundVars[i] = new LambdaVariable(BigInteger.valueOf(j++));
            }while (vars.contains(boundVars[i]));
        }
        ExpressionNode[] unwrap=new ExpressionNode[params.length];
        for(int i=0;i<params.length;i++){
            if(params[i] instanceof LambdaExpression){
                unwrap[i]=((LambdaExpression) params[i]).node;
                HashMap<LambdaVariable,LambdaVariable> remap=new HashMap<>();
                for(int k=0;k<((LambdaExpression) params[i]).boundVariables.length;k++){
                    remap.put(((LambdaExpression) params[i]).boundVariables[k],boundVars[k]);
                }
                unwrap[i]=unwrap[i].renameVariables(remap);
            }else{
                unwrap[i]=params[i];
            }
        }
        return from(OperatorNode.from(operatorInfo,unwrap),boundVars);
    }

    private LambdaExpression(ExpressionNode node, LambdaVariable[] boundVariables) {
        this.node = node;
        Set<LambdaVariable> nodeVars=node.variables();
        int lastIn=boundVariables.length-1;
        while(!nodeVars.contains(boundVariables[lastIn])){
            lastIn--;
        }//remove bindings to nonexistent variables (at end)
        //key all other bindings to not disturb the positions of the existing bindings
        this.boundVariables = Arrays.copyOf(boundVariables,lastIn);
    }

    public static int compare(ExpressionNode a, ExpressionNode b) {
        if(a instanceof MathObject){
            if(b instanceof MathObject){
                return MathObject.compare((MathObject)a,(MathObject)b);
            }else{
                return -1;
            }
        }else if(a instanceof LambdaVariable){
            if(b instanceof MathObject){
                return 1;
            }else if(b instanceof LambdaVariable){
                return ((LambdaVariable) a).getId().compareTo(((LambdaVariable) b).getId());
            }else {
                return -1;
            }
        }else if(a instanceof OperatorNode){
            if(b instanceof MathObject||b instanceof LambdaVariable){
                return 1;
            }else if(b instanceof OperatorNode){
               return ((OperatorNode)a).compareTo((OperatorNode)b);
            }else{
                throw new IllegalArgumentException("Unexpected ExpressionNode class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected ExpressionNode class:"+a.getClass());
        }
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
        StringBuilder ret=new StringBuilder("LAMBDA:");
        for(LambdaVariable var:boundVariables){
            if(ret.length()>7)
                ret.append(",");
            ret.append(var.getId());
        }
        return ret.append(" (").append(nodeString).append(')').toString();
    }

    @Override
    public String asString() {
        return node.asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LambdaExpression)) return false;
        //equality of similar structures through standard form of Expressions
        return node.equals(((LambdaExpression) o).node)&&Arrays.equals(boundVariables,((LambdaExpression) o).boundVariables);
    }
    @Override
    public int hashCode() {
        return Objects.hash(node.hashCode(),Arrays.hashCode(boundVariables));
    }
}
