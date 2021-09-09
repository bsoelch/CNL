package bsoelch.cnl.math;

import bsoelch.cnl.Constants;
import bsoelch.cnl.math.expressions.ExpressionNode;
import bsoelch.cnl.math.expressions.LambdaVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

/**Root of all MathObjects*/
public abstract class MathObject implements ExpressionNode {//addLater? ElementView -> view one ELement of set/map
    MathObject(){}//package private constructor to ensure that there are no MathObject classes outside this package

    public static int FLOOR = -1;
    public static int ROUND = 0;
    public static int CIEL = 1;


    public abstract NumericValue numericValue();

    public abstract String toString(BigInteger base, boolean useSmallBase);
    public abstract String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase);
    public abstract String toStringFloat(BigInteger base, Real precision, boolean useSmallBase);

    /**String representing this Object*/
    public abstract String asString();
    /**String representation of this if all ints are replaced with their String-value*/
    public abstract String intsAsString();

    @Override
    public Set<LambdaVariable> variables() {
        return Collections.emptySet();
    }
    @Override
    public Set<LambdaVariable> freeVariables() {
        return Collections.emptySet();
    }
    @Override
    public MathObject renameVariables(Map<LambdaVariable, LambdaVariable> replace) {
        return this;
    }
    @Override
    public MathObject evaluate(Map<LambdaVariable, ? extends ExpressionNode> replace) {
        return this;
    }
    @Override
    public MathObject asMathObject() {
        return this;
    }

    @Override
    public String toString() {
        return toString(Constants.DEFAULT_BASE,true);
    }
    //all MathObject have to Override equals and hashCOde
    /**{@inheritDoc}*/
    abstract public boolean equals(Object o);

    @Override
    public boolean equals(Map<LambdaVariable,Integer> parentVars, ExpressionNode other, Map<LambdaVariable,Integer> otherVars) {
        return equals(other);
    }
    /**{@inheritDoc}*/
    abstract public int hashCode();
    @Override
    public int hashCode(Map<LambdaVariable,Integer> boundVars) {
        return hashCode();
    }

    public static MathObject not(MathObject o) {
        return deepReplaceNumbers(o,NumericValue::not);
    }

    /**@return the boolean value of the supplied MathObject,
     * the only objects resulting in false are 0, empty sets and empty maps/tuples */
    public static boolean isTrue(MathObject arg) {
        if(arg instanceof LambdaExpression)
            arg=arg.asMathObject();
        return !(arg.equals(Real.Int.ZERO)||arg.equals(FiniteSet.EMPTY_SET)
                ||arg.equals(Tuple.EMPTY_MAP));
    }

    public static FiniteSet asSet(MathObject o){
        if(o instanceof LambdaExpression)
            o=o.asMathObject();
        if(o instanceof NumericValue){
            return FiniteSet.from(o);
        }else if(o instanceof Matrix){
            return ((Matrix)o).asMap().asSet();
        }else if(o instanceof FiniteSet){
            return (FiniteSet)o;
        }else if(o instanceof FiniteMap){
            return ((FiniteMap)o).asSet();
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }
    public static FiniteMap asMap(MathObject o){
        if(o instanceof LambdaExpression)
            o=o.asMathObject();
        if(o instanceof NumericValue){
            return Tuple.create(new MathObject[]{o});
        }else if(o instanceof Matrix){
            return ((Matrix)o).asMap();
        }else if(o instanceof FiniteSet){
            return ((FiniteSet)o).asMap();
        }else if(o instanceof FiniteMap){
            return ((FiniteMap)o);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }
    public static Tuple asTuple(MathObject o) {
        if(o instanceof LambdaExpression)
            o=o.asMathObject();
        if(o instanceof Matrix)
            o=((Matrix) o).asMap();
        if(o instanceof NumericValue){
            return Tuple.create(new MathObject[]{o});
        }else if(o instanceof FiniteSet){
            return ((FiniteSet)o).asTuple();
        }else if(o instanceof FiniteMap){
            return ((FiniteMap)o).asTuple();
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }
    public static Tuple nonzeroElements(MathObject o) {
        if(o instanceof LambdaExpression)
            o=o.asMathObject();
        if(o instanceof Matrix)
            o=((Matrix) o).asMap();
        if(o instanceof NumericValue){
            return Tuple.create(new MathObject[]{o});
        }else if(o instanceof FiniteSet){
            return ((FiniteSet)o).asTuple();
        }else if(o instanceof FiniteMap){
            return ((FiniteMap)o).nonzeroElements();
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }

    public static MathObject deepReplaceNumbers(MathObject o, Function<NumericValue, NumericValue> scalarFunction){
        if(o instanceof NumericValue){
            return scalarFunction.apply((NumericValue) o);
        }else if(o instanceof Matrix){
            return ((Matrix) o).applyToAll(scalarFunction);
        }else if(o instanceof FiniteSet){
            return FiniteSet.replace((FiniteSet) o, e-> deepReplaceNumbers(e,scalarFunction));
        }else if(o instanceof FiniteMap){
            return ((FiniteMap) o).replace(e-> deepReplaceNumbers(e,scalarFunction));
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }
    public static MathObject deepReplace(MathObject o, Function<NumericValue, MathObject> scalarFunction){
        if(o instanceof Matrix)
            o=((Matrix) o).asMap();
        if(o instanceof NumericValue){
            return scalarFunction.apply((NumericValue) o);
        }else if(o instanceof FiniteSet){
            return FiniteSet.replace((FiniteSet) o, e-> deepReplace(e,scalarFunction));
        }else if(o instanceof FiniteMap){
            return ((FiniteMap) o).replace(e-> deepReplace(e,scalarFunction));
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }
    public static MathObject replaceAll(MathObject o, Function<MathObject, MathObject> replace){
        if(o instanceof Matrix)
            o=((Matrix) o).asMap();
        if(o instanceof NumericValue){
            return replace.apply(o);
        }else if(o instanceof FiniteSet){
            return FiniteSet.replace((FiniteSet) o,replace);
        }else if(o instanceof FiniteMap){
            return ((FiniteMap) o).replace(replace);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }
    public static void forEachElement(MathObject o, Consumer<MathObject> action){
        if(o instanceof NumericValue){
            action.accept(o);
        }else if(o instanceof Matrix){
            for(NumericValue e:(Matrix) o){
                action.accept(e);
            }
        }else if(o instanceof FiniteSet){
            for(MathObject e:(FiniteSet)o){
                action.accept(e);
            }
        }else if(o instanceof Tuple){
            for(MathObject e:(Tuple)o){
                action.accept(e);
            }
        }else if(o instanceof FiniteMap){
            for(MathObject e:((FiniteMap) o).values()){
                action.accept(e);
            }
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }
    public static void deepForEach(MathObject o, Consumer<NumericValue> action){
        if(o instanceof NumericValue){
            action.accept((NumericValue) o);
        }else if(o instanceof Matrix){
            for(NumericValue e:(Matrix) o){
                action.accept(e);
            }
        }else if(o instanceof FiniteSet){
            for(MathObject e:(FiniteSet)o){
                deepForEach(e,action);
            }
        }else if(o instanceof Tuple){
            for(MathObject e:(Tuple)o){
                deepForEach(e,action);
            }
        }else if(o instanceof FiniteMap){
            for(MathObject e:((FiniteMap) o).values()){
                deepForEach(e,action);
            }
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }

    public static MathObject mapAll(MathObject o, Function<MathObject, MathObject> replace){
        if(o instanceof Matrix)
            o=((Matrix) o).asMap();
        if(o instanceof NumericValue){
            return replace.apply(o);
        }else if(o instanceof FiniteSet){
            return FiniteSet.replace((FiniteSet) o, e->new Pair(e,replace.apply(e))).asMap();
        }else if(o instanceof FiniteMap){
            return ((FiniteMap) o).replace(replace);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }
    public static MathObject deepCombine(MathObject l, MathObject r,
                                                BiFunction<NumericValue,NumericValue,MathObject> combine){
        if(l instanceof Matrix)
            l=((Matrix) l).asMap();
        if(r instanceof Matrix)
            r=((Matrix) r).asMap();
        if(l instanceof NumericValue){
            if(r instanceof NumericValue){
                return combine.apply((NumericValue) l,((NumericValue)r));
            }else if(r instanceof FiniteSet){
                MathObject finalL = l;
                return FiniteSet.replace((FiniteSet) r, o-> deepCombine(finalL,o,combine));
            }else if(r instanceof FiniteMap){
                MathObject finalL1 = l;
                return ((FiniteMap) r).replace(o-> deepCombine(finalL1,o,combine));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteSet){
            if(r instanceof NumericValue||r instanceof FiniteMap){
                MathObject finalR = r;
                return FiniteSet.replace((FiniteSet) l, o-> deepCombine(o, finalR,combine));
            }else if(r instanceof FiniteSet){
                return FiniteSet.combineAll((FiniteSet) l,(FiniteSet) r, (a, b)-> deepCombine(a,b,combine));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteMap){
            if(r instanceof NumericValue){
                MathObject finalR1 = r;
                return ((FiniteMap) l).replace(o-> deepCombine(o, finalR1,combine));
            }else if(r instanceof FiniteSet){
                MathObject finalL2 = l;
                return FiniteSet.replace((FiniteSet) r, o-> deepCombine(finalL2,o,combine));
            }else if(r instanceof FiniteMap){
                return FiniteMap.combineAll((FiniteMap) l,(FiniteMap) r, (a, b)-> deepCombine(a,b,combine));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+l.getClass());
        }
    }
    public static MathObject deepCombineNumbers(MathObject l, MathObject r,
                                                BinaryOperator<NumericValue> scalarOperation){
        if(l instanceof NumericValue){
            if(r instanceof NumericValue){
                return scalarOperation.apply((NumericValue) l,((NumericValue)r));
            }else if(r instanceof Matrix){
                return ((Matrix) r).applyToAll(e->scalarOperation.apply((NumericValue)l,e));
            }else if(r instanceof FiniteSet){
                return FiniteSet.replace((FiniteSet) r, o-> deepCombineNumbers(l,o,scalarOperation));
            }else if(r instanceof FiniteMap){
                return ((FiniteMap) r).replace(o-> deepCombineNumbers(l,o,scalarOperation));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof Matrix){
            if(r instanceof NumericValue){
                return ((Matrix) l).applyToAll(e->scalarOperation.apply(e,(NumericValue)r));
            }else if(r instanceof Matrix){
                return Matrix.combine((Matrix) l,(Matrix) r,scalarOperation);
            }else if(r instanceof FiniteSet){
                return FiniteSet.replace((FiniteSet) r, o-> deepCombineNumbers(l,o,scalarOperation));
            }else if(r instanceof FiniteMap){
                return ((FiniteMap) r).replace(o-> deepCombineNumbers(l,o,scalarOperation));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteSet){
            if(r instanceof NumericValue||r instanceof Matrix||r instanceof FiniteMap){
                return FiniteSet.replace((FiniteSet) l, o-> deepCombineNumbers(o,r,scalarOperation));
            }else if(r instanceof FiniteSet){
                return FiniteSet.combineAll((FiniteSet) l,(FiniteSet) r, (a, b)-> deepCombineNumbers(a,b,scalarOperation));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteMap){
            if(r instanceof NumericValue||r instanceof Matrix){
                return ((FiniteMap) l).replace(o-> deepCombineNumbers(o,r,scalarOperation));
            }else if(r instanceof FiniteSet){
                return FiniteSet.replace((FiniteSet) r, o-> deepCombineNumbers(l,o,scalarOperation));
            }else if(r instanceof FiniteMap){
                return FiniteMap.combineAll((FiniteMap) l,(FiniteMap) r, (a, b)-> deepCombineNumbers(a,b,scalarOperation));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+l.getClass());
        }
    }
    public static MathObject combine(MathObject l, MathObject r,
                                         BinaryOperator<MathObject> combine){
        if(l instanceof Matrix)
            l=((Matrix) l).asMap();
        if(r instanceof Matrix)
            r=((Matrix) r).asMap();
        if(l instanceof NumericValue){
            if(r instanceof NumericValue){
                return combine.apply(l,r);
            }else if(r instanceof FiniteSet){
                MathObject finalL = l;
                return FiniteSet.replace((FiniteSet) r, o-> combine.apply(finalL,o));
            }else if(r instanceof FiniteMap){
                MathObject finalL1 = l;
                return ((FiniteMap) r).replace(o-> combine.apply(finalL1,o));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteSet){
            if(r instanceof NumericValue){
                MathObject finalR = r;
                return FiniteSet.replace((FiniteSet) l, o-> combine.apply(o, finalR));
            }else if(r instanceof FiniteSet){
                return FiniteSet.combineAll((FiniteSet) l,(FiniteSet) r, combine);
            }else if(r instanceof FiniteMap){
                return FiniteSet.combineAll((FiniteSet) l,((FiniteMap) r).asSet(), (a, b)->combine.apply(a,((Pair)b).b));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteMap){
            if(r instanceof NumericValue){
                MathObject finalR1 = r;
                return ((FiniteMap) l).replace(o->combine.apply(o, finalR1));
            }else if(r instanceof FiniteSet){
                return FiniteSet.combineAll(((FiniteMap) l).asSet(),(FiniteSet) r,  (a, b)->combine.apply(((Pair)a).b,b));
            }else if(r instanceof FiniteMap){
                return FiniteMap.combineAll((FiniteMap) l,(FiniteMap) r, combine);
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+l.getClass());
        }
    }

    /**reduces a set/map with a binary operation
     * @param target the targeted MathObject
     * @param prev result of the previous operation (for deep reduce) or null is the is the first operation in the reduction
     * @param reduce the reduction function
     * @param isDeep if true the reduction goes recursive through all contained sets/maps*/
    public static MathObject reduce(MathObject target,@Nullable MathObject prev,BinaryOperator<MathObject> reduce,boolean isDeep){
        if(target instanceof Matrix)
            target=((Matrix) target).asMap();
        if(target instanceof NumericValue){
            return prev==null?target:reduce.apply(prev,target);
        }else if(target instanceof FiniteSet){
            for(MathObject e:(FiniteSet)target){
                prev=prev==null?e:(isDeep?reduce(target,prev,reduce,true):reduce.apply(prev,e));
            }
            return prev;
        }else if(target instanceof Tuple){
            for(MathObject e:(Tuple) target){
                prev=prev==null?e:(isDeep?reduce(target,prev,reduce,true):reduce.apply(prev,e));
            }
            return prev;
        }else if(target instanceof FiniteMap){
            for(MathObject e:((FiniteMap) target).values()){
                prev=prev==null?e:(isDeep?reduce(target,prev,reduce,true):reduce.apply(prev,e));
            }
            return prev;
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+target.getClass());
        }
    }

    public static MathObject nAryReduce(MathObject[] objects, MathObject nilaryValue, BinaryOperator<MathObject> reduce){
        if(objects.length==0) {
            return nilaryValue;
        }else if(objects.length==1) {
            return objects[0];
        }else{
            MathObject ret=objects[0];
            for (int i=1;i<objects.length;i++) {
                ret = reduce.apply(ret, objects[i]);
            }
            return ret;
        }
    }
    public static NumericValue deepNAryReduce(MathObject[] objects, NumericValue nilaryValue, BinaryOperator<NumericValue> reduce){
        if(objects.length==0) {
            return nilaryValue;
        }else if(objects.length==1) {
            if(objects[0] instanceof NumericValue) {
                return (NumericValue)objects[0];
            }else if(objects[0] instanceof Matrix){
                NumericValue tmp=null;
                for (Iterator<NumericValue> it = ((Matrix) objects[0]).sparseIterator(); it.hasNext();) {
                    NumericValue o = it.next();
                    if(tmp==null){
                        tmp=o;
                    }else{
                        tmp=reduce.apply(tmp,o);
                    }
                }
                return tmp==null?nilaryValue:tmp;
            }else if(objects[0] instanceof FiniteSet){
                NumericValue tmp=null,next;
                for(MathObject o:((FiniteSet) objects[0])){
                    next=deepNAryReduce(new MathObject[]{o},nilaryValue,reduce);
                    if(tmp==null){
                        tmp=next;
                    }else{
                        tmp=reduce.apply(tmp,next);
                    }
                }
                return tmp==null?nilaryValue:tmp;
            }else if(objects[0] instanceof FiniteMap){
                NumericValue tmp=null,next;
                for(MathObject o:((FiniteMap) objects[0]).values()){
                    next=deepNAryReduce(new MathObject[]{o},nilaryValue,reduce);
                    if(tmp==null){
                        tmp=next;
                    }else{
                        tmp=reduce.apply(tmp,next);
                    }
                }
                return tmp==null?nilaryValue:tmp;
            }else{
                throw new RuntimeException("Unexpected MathObject class:"+objects[0].getClass());
            }
        }else{
            NumericValue tmp=null,next;
            for(MathObject o:objects){
                next=deepNAryReduce(new MathObject[]{o},nilaryValue,reduce);
                if(tmp==null){
                    tmp=next;
                }else{
                    tmp=reduce.apply(tmp,next);
                }
            }
            return tmp==null?nilaryValue:tmp;
        }
    }

    public static MathObject add(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::add);
    }
    public static MathObject subtract(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::subtract);
    }
    public static MathObject negate(MathObject o) {
        return deepReplaceNumbers(o, e->e.negate());
    }


    public static MathObject multiply(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::multiply);
    }

    public static Real sqAbs(MathObject o) {
        if(o instanceof NumericValue){
            return ((NumericValue) o).sqAbs();
        }else if(o instanceof Matrix){
            Real sqAbs=Real.Int.ZERO;
            for (Iterator<NumericValue> it = ((Matrix) o).sparseIterator(); it.hasNext();) {
                NumericValue e = it.next();
                sqAbs=Real.add(sqAbs,e.sqAbs());
            }
            return sqAbs;
        }else if(o instanceof FiniteSet){
            Real sqAbs=Real.Int.ZERO;
            for(MathObject e:(FiniteSet)o){
                sqAbs=Real.add(sqAbs,sqAbs(e));
            }
            return sqAbs;
        }else if(o instanceof FiniteMap){
            Real sqAbs=Real.Int.ZERO;
            for (Iterator<MathObject> it = ((FiniteMap) o).valueIterator(); it.hasNext(); ) {
                MathObject e = it.next();
                sqAbs=Real.add(sqAbs,sqAbs(e));
            }
            return sqAbs;
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }


    public static MathObject divide(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::divide);
    }

    public static MathObject pow(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::pow);
    }
    public static MathObject round(MathObject o, int mode) {
        return deepReplaceNumbers(o, e->e.round(mode));
    }

    public static MathObject floorAnd(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::floorAnd);
    }
    public static MathObject floorOr(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::floorOr);
    }
    public static MathObject floorXor(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::floorXor);
    }
    public static MathObject floorAndNot(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::floorAndNot);
    }

    /**@param l Left operand (maps will be converted to their set representation)
     * @param r right operand (maps will be converted to their set representation)
     * @param setOp operation that is applied to sets
     * @param atomicOperation operation that is applied to Matrices and NumericValues
     * @param unwrapMaps if this value if true the program tries
     *                    to convert the result to a map if one of the arguments was a map
     * */
    static MathObject setOperation(MathObject l, MathObject r,
                                   BinaryOperator<FiniteSet> setOp,
                                   BinaryOperator<MathObject> atomicOperation, boolean unwrapMaps){
        if(l instanceof NumericValue||l instanceof Matrix||((!unwrapMaps)&&(l instanceof FiniteMap))){
            if(r instanceof NumericValue||r instanceof Matrix||((!unwrapMaps)&&(r instanceof FiniteMap))){
                return atomicOperation.apply(l,r);
            }else if(r instanceof FiniteSet){
                return setOp.apply(FiniteSet.from(l),(FiniteSet) r);
            }else if(r instanceof FiniteMap){
                FiniteSet set = setOp.apply(FiniteMap.indicatorMap(FiniteSet.from(l)).asSet(), ((FiniteMap) r).asSet());
                return set.asMap();
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteSet){
            if(r instanceof NumericValue||r instanceof Matrix||((!unwrapMaps)&&(r instanceof FiniteMap))){
                return setOp.apply((FiniteSet) l, FiniteSet.from(r));
            }else if(r instanceof FiniteSet){
                return setOp.apply((FiniteSet) l,(FiniteSet) r);
            }else if(r instanceof FiniteMap){
                FiniteSet set = setOp.apply(FiniteMap.indicatorMap((FiniteSet) l).asSet(), ((FiniteMap) r).asSet());
                return set.asMap();
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteMap){
            if(r instanceof NumericValue || r instanceof Matrix){
                FiniteSet set = setOp.apply(((FiniteMap) l).asSet(), FiniteMap.indicatorMap(FiniteSet.from(r)).asSet());
                return set.asMap();
            }else if(r instanceof FiniteSet){
                FiniteSet set = setOp.apply(((FiniteMap) l).asSet(), FiniteMap.indicatorMap((FiniteSet) r).asSet());
                return set.asMap();
            }else if(r instanceof FiniteMap){
                FiniteSet set = setOp.apply(((FiniteMap) l).asSet(), ((FiniteMap) r).asSet());
                return set.asMap();
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+l.getClass());
        }
    }

    public static MathObject intersect(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::intersect,
                (a,b)->a.equals(b)?FiniteSet.from(a):FiniteSet.EMPTY_SET, true);
    }
    public static MathObject unite(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::unite,FiniteSet::from, true);
    }
    public static MathObject symmetricDifference(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::symmetricDifference,
                (a,b)->a.equals(b)?FiniteSet.EMPTY_SET:FiniteSet.from(a,b), true);
    }
    public static MathObject difference(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::difference,
                (a,b)->a.equals(b)?FiniteSet.EMPTY_SET:FiniteSet.from(a), true);
    }

    public static MathObject times(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::product,Pair::new, false);
    }
    /**calculates the n-ary cartesian product of the objects in the given set
     * @param objects operands of the n-ary product, maps are converted to sets,
     *                Matrices and NumericValues are wrapped in sets */
    public static MathObject nAryTimes(MathObject[] objects){
        //direct implementation, since result is different from repeated binary times
        FiniteSet[] sets=new FiniteSet[objects.length];
        for (int i=0;i<objects.length;i++) {
            if(objects[i] instanceof NumericValue||objects[i] instanceof Matrix){
                sets[i]=FiniteSet.from(objects[i]);
            }else if(objects[i] instanceof FiniteSet){
                sets[i]=(FiniteSet) objects[i];
            }else if(objects[i] instanceof FiniteMap){
                sets[i]=((FiniteMap) objects[i]).asSet();
                throw new UnsupportedOperationException("Not implemented");
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+objects[i].getClass());
            }
        }
        return FiniteSet.product(sets);
    }
    public static MathObject fAdd(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::fAdd);
    }
    public static MathObject strConcat(MathObject l, MathObject r) {
        return deepCombineNumbers(l,r, NumericValue::strConcat);
    }

    public static MathObject min(MathObject l, MathObject r) {
        return compare(l,r)<0?l:r;
    }
    public static MathObject max(MathObject l, MathObject r) {
        return compare(l,r)>0?l:r;
    }

    public static Tuple tupleConcat(MathObject a,MathObject b) {
        //convert Maps to Tuples
        if(a instanceof FiniteMap){
            a=((FiniteMap) a).asTuple();
        }//no else
        if(b instanceof FiniteMap){
            b=((FiniteMap) b).asTuple();
        }

        int l,s;
        if(a instanceof Tuple){
            l=((Tuple) a).length();
            s=((Tuple) a).size();
        }else{
            l=1;
            s=1;
        }
        if(b instanceof Tuple){
            l+=((Tuple) b).length();
            s+=((Tuple) b).size();
        }else{
            l+=1;
            s+=1;
        }
        if(l<Tuple.SPARSE_FACTOR*s){
            MathObject[] objects=new MathObject[l];
            if(a instanceof Tuple){
                System.arraycopy(((Tuple) a).toArray(),0,objects,0,l=((Tuple) a).length());
            }else{
                objects[0]=a;
                l=1;
            }
            if(b instanceof Tuple){
                System.arraycopy(((Tuple) b).toArray(),0,objects,l,((Tuple) b).length());
            }else{
                objects[l]=b;
            }
            return Tuple.create(objects);
        }else{
            TreeMap<MathObject,MathObject> mapData=new TreeMap<>(MathObject::compare);
            Real.Int offset,next;
            if(a instanceof Tuple){
                for (Iterator<Pair> it = ((Tuple) a).mapIterator(); it.hasNext(); ) {
                    Pair e = it.next();
                    next=e.a.numericValue().realPart().round(ROUND);
                    mapData.put(next,e.b);
                }
                offset=Real.from(((Tuple) a).length());
            }else{
                mapData.put(Real.Int.ZERO,a);
                offset=Real.Int.ONE;
            }
            if(b instanceof Tuple){
                for (Iterator<Pair> it = ((Tuple) b).mapIterator(); it.hasNext(); ) {
                    Pair e = it.next();
                    next=e.a.numericValue().realPart().round(ROUND);
                    mapData.put(Real.add(offset,next),e.b);
                }
            }else{
                mapData.put(offset,a);
            }
            return FiniteMap.createTuple(mapData,l);
        }
    }

    /**gets the first or last element of a given MathObject*/
    public static MathObject firstOrLast(MathObject source, boolean first, boolean getKey){
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            return source;
        }else if(source instanceof FiniteSet){
            return first?((FiniteSet) source).getFirst():((FiniteSet) source).getLast();
        }else if(source instanceof FiniteMap){
            return getKey?(first?((FiniteMap) source).firstKey():((FiniteMap) source).lastKey()):
                    (first?((FiniteMap) source).firstValue():((FiniteMap) source).lastValue());
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }
    /**gets the domain or range element of a given MathObject*/
    public static FiniteSet domainOrRange(MathObject source, boolean domain){
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            return FiniteSet.from(source);
        }else if(source instanceof FiniteSet){
            return (FiniteSet)source;
        }else if(source instanceof FiniteMap){
            return domain?((FiniteMap) source).domain():((FiniteMap) source).values();
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }
    /**gets the element in source at the given key*/
    public static MathObject getElement(MathObject source,MathObject key){
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            return source.equals(key)?Real.Int.ONE:Real.Int.ZERO;
        }else if(source instanceof FiniteSet){
            return ((FiniteSet) source).contains(key)?Real.Int.ONE:Real.Int.ZERO;
        }else if(source instanceof FiniteMap){
            return ((FiniteMap) source).evaluateAt(key);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }
    /**inserts an element in the given MathObject */
    public static MathObject insert(MathObject source,MathObject value){
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            return FiniteSet.from(source,value);
        }else if(source instanceof FiniteSet){
            return unite(source,FiniteSet.from(value));
        }else if(source instanceof Tuple){
            return tupleConcat(source,Tuple.create(new MathObject[]{value}));
        }else if(source instanceof FiniteMap){
            return ((FiniteMap) source).insert(value);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }
    /**inserts an element in the given MathObject at the specified position */
    public static MathObject put(MathObject source,MathObject key,MathObject value){
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            return asMap(source).put(key, value);
        }else if(source instanceof FiniteSet){
            return FiniteMap.indicatorMap(((FiniteSet) source)).put(key, value);
        }else if(source instanceof Tuple){
            return ((Tuple) source).put(key, value);
        }else if(source instanceof FiniteMap){
            return ((FiniteMap) source).put(key, value);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }
    /**all elements in source that are less/greater that slice
     * @param include if true elements equal to slice are allowed*/
    public static MathObject slice(MathObject source, MathObject slice, boolean include, boolean less) {
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            source=asSet(source);
        }
        if(source instanceof FiniteSet){
            return less?((FiniteSet) source).headSet(slice, include):((FiniteSet) source).tailSet(slice, include);
        }else if(source instanceof FiniteMap){
            return less?((FiniteMap) source).headMap(slice, include):((FiniteMap) source).tailMap(slice, include);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }
    /**all elements in source that are between from and to
     * @param includeFrom true if elements equal to from are allowed
     * @param includeTo true if elements equal to to are allowed*/
    public static MathObject range(MathObject source, MathObject from, boolean includeFrom, MathObject to, boolean includeTo) {
        if(compare(from,to)>0){//swap if from>to
            MathObject tmp=to;
            to=from;
            from=tmp;
        }
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            source=asSet(source);
        }
        if(source instanceof FiniteSet){
            return ((FiniteSet) source).range(from,includeFrom,to,includeTo);
        }else if(source instanceof FiniteMap){
            return ((FiniteMap) source).range(from,includeFrom,to,includeTo);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }
    /**removes the first or last element of a given MathObject*/
    public static MathObject removeEnd(MathObject source, boolean first){
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            return FiniteSet.EMPTY_SET;
        }else if(source instanceof FiniteSet){
            return first?((FiniteSet) source).removeFirst():((FiniteSet) source).removeLast();
        }else if(source instanceof FiniteMap){
            return first?((FiniteMap) source).removeFirst():((FiniteMap) source).removeLast();
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }
    /**removes all occurrences of value in the given MathObject,
     * @param isKey if true the removed elements is selected by key instead of value*/
    public static MathObject remove(MathObject source,MathObject value,boolean isKey){
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            return FiniteSet.difference(FiniteSet.from(source),FiniteSet.from(value));
        }else if(source instanceof FiniteSet){
            return difference(source,FiniteSet.from(value));
        }else if(source instanceof FiniteMap){
            return isKey?((FiniteMap) source).removeKey(value):((FiniteMap) source).remove(value);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }
    /**removes all values in source that satisfy the given condition*/
    public static MathObject removeIf(MathObject source,Function<MathObject,MathObject> condition){
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            return isTrue(condition.apply(source))?FiniteSet.EMPTY_SET:FiniteSet.from(source);
        }else if(source instanceof FiniteSet){
            return ((FiniteSet) source).removeIf(condition);
        }else if(source instanceof FiniteMap){
            return ((FiniteMap) source).removeIf((k,v)->condition.apply(v));
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }
    /**removes all (key,value) pairs in source that satisfy the given condition*/
    public static MathObject removeIfMap(MathObject source,BinaryOperator<MathObject> condition){
        if(source instanceof LambdaExpression)
            source=source.asMathObject();
        //no else
        if(source instanceof Matrix)
            source=((Matrix) source).asMap();
        //no else
        if(source instanceof NumericValue){
            return isTrue(condition.apply(source,Real.Int.ONE))?FiniteSet.EMPTY_SET:FiniteSet.from(source);
        }else if(source instanceof FiniteSet){
            return ((FiniteSet) source).removeIf(e->condition.apply(e,Real.Int.ONE));
        }else if(source instanceof FiniteMap){
            return ((FiniteMap) source).removeIf(condition);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+source.getClass());
        }
    }


    public static int compare(MathObject a, MathObject b){
        if(a instanceof LambdaExpression){
            if(b instanceof LambdaExpression){
                return ((LambdaExpression)a).compareTo((LambdaExpression)b);
            }else{
                return 1;
            }
        }else if(b instanceof LambdaExpression){
            return -1;
        }
        //compare Matrices as Maps
        if(a instanceof Matrix)
            a=((Matrix) a).asMap();
        if(b instanceof Matrix)
            b=((Matrix) b).asMap();

        if(a instanceof NumericValue){
            if(b instanceof NumericValue){
                return ((NumericValue) a).compareTo((NumericValue) b);
            }else if(b instanceof FiniteSet){
                if(((FiniteSet) b).size()==0){
                    return 1;
                }else{
                    int c;
                    for(MathObject e:(FiniteSet)b){
                        c=compare(a,e);
                        if(c!=0)
                            return c;
                    }
                    return -1;// a < {a}
                }
            }else if(b instanceof FiniteMap){
                Iterator<MathObject> itr=((FiniteMap)b).valueIterator();
                int c;
                while(itr.hasNext()){
                    c=compare(a,itr.next());
                    if(c!=0)
                        return c;
                }
                return -1;// a < {a}
            }
        }else if(a instanceof FiniteSet){
            if(b instanceof NumericValue){
                return -compare(b,a);
            }else if(b instanceof FiniteSet){
                Iterator<MathObject> itr1=((FiniteSet) a).iterator();
                Iterator<MathObject> itr2=((FiniteSet) b).iterator();
                return compareItr(itr1,itr2);
            }else if(b instanceof FiniteMap){
                Iterator<MathObject> itr1=((FiniteSet) a).iterator();
                Iterator<MathObject> itr2=((FiniteMap) b).domain().iterator();
                int c=compareItr(itr1,itr2);
                return c==0?-1:c;
            }
        }else if(a instanceof FiniteMap){
            if(b instanceof NumericValue){
                return -compare(b,a);
            }else if(b instanceof FiniteSet){
                Iterator<MathObject> itr1=((FiniteMap) a).domain().iterator();
                Iterator<MathObject> itr2=((FiniteSet) b).iterator();
                int c=compareItr(itr1,itr2);
                return c==0?1:c;
            }else if(b instanceof FiniteMap){
                Iterator<Pair> itr1=((FiniteMap) a).mapIterator();
                Iterator<Pair> itr2=((FiniteMap) b).mapIterator();
                return compareItrMap(itr1,itr2);
            }
        }
        if(a==null||b==null){
            return a==null?b==null?0:1:-1;
        }else{
            throw new RuntimeException("Cannot compare MathObjects of types:"+a.getClass()+" and "+b.getClass());
        }
    }
    static int compareItr(Iterator<MathObject> itr1, Iterator<MathObject> itr2) {
        while(itr1.hasNext()||itr2.hasNext()) {
            if (itr1.hasNext()) {
                if (itr2.hasNext()) {
                    int c=compare(itr1.next(), itr2.next());
                    if(c!=0)
                        return c;
                } else {
                    return 1;
                }
            } else {
                return -1;
            }
        }
        return 0;
    }
    static int compareItrMap(Iterator<Pair> itr1, Iterator<Pair> itr2) {
        while(itr1.hasNext()||itr2.hasNext()) {
            if (itr1.hasNext()) {
                if (itr2.hasNext()) {
                    Pair next1 = itr1.next(),next2 = itr2.next();
                    int c=compare(next1.a, next2.a);
                    if(c!=0)
                        return c;
                    c=compare(next1.b, next2.b);
                    if(c!=0)
                        return c;
                } else {
                    return 1;
                }
            } else {
                return -1;
            }
        }
        return 0;
    }


    static public class FromString{
        static private final int STATE_NUMBER=0,STATE_TUPLE=1,STATE_STRING=2,STATE_VALUE=3,STATE_SET=4;
        static private final int MODE_SET=1, MODE_VALUE =0,MODE_TUPLE=-1;

        private interface Node{}
        private static class OperatorNode implements Node{
            final String value;
            private OperatorNode(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return "Operator:("+value+")";
            }
        }
        private static class ValueNode implements Node{
            final MathObject value;
            private ValueNode(MathObject value) {
                this.value = value;
            }
            @Override
            public String toString() {
                return "("+value+")";
            }
        }

        static public MathObject fromString(String input, BigInteger base){
            return fromString(input, base, MODE_VALUE, false);
        }
        static public MathObject safeFromString(String input, BigInteger base){
            return fromString(input, base, MODE_VALUE, true);
        }

        /**Initiates a MathObject from the given string value
         * @param input String that should be converted
         * @param base Base for the conversion
         * @param mode Type for the wrapping Object
         * @param safeMode when safeMode is true the method will not throw an IllegalArgumentException,
         *                and try to fix syntax errors in the input string
         * @throws IllegalArgumentException if input represents no MathObject*/
        private static MathObject fromString(String input, BigInteger base, int mode, boolean safeMode) {
            if(input.isEmpty())
                switch (mode){
                    case MODE_TUPLE:return Tuple.EMPTY_MAP;
                    case MODE_VALUE:return Real.Int.ZERO;
                    case MODE_SET:return FiniteSet.EMPTY_SET;
                    default:throw new IllegalArgumentException("Unknown mode:"+mode);
                }
            ArrayList<Node> parts=new ArrayList<>();
            int p0=0,state=0,layer=0;
            for(int p = 0; p< input.length(); p++){
                switch (state) {
                    case STATE_NUMBER:{
                        if(!Real.DIGITS.contains(""+ input.charAt(p))){
                            switch (input.charAt(p)){
                                case ':'://additional number characters
                                case '.':
                                case '@'://base modifiers
                                case '$':
                                case '§':
                                case '#':break;

                                case ';':
                                case ',':
                                case '+':
                                case '*':
                                case '|':
                                case '/':
                                case '^':
                                case '→':
                                case '↦': {//1 char Operators
                                    //end section
                                    String tmp = input.substring(p0, p).trim();
                                    if (tmp.length() > 0) {
                                        parts.add(new ValueNode(numberFromString(base, tmp, safeMode)));
                                    }
                                    //save Operator
                                    parts.add(new OperatorNode("" + input.charAt(p)));
                                    p0 = p + 1;
                                }break;
                                case '-': {//multi-char operators
                                    //end section
                                    String tmp = input.substring(p0, p).trim();
                                    if (tmp.length() > 0) {
                                        parts.add(new ValueNode(numberFromString(base, tmp, safeMode)));
                                    }
                                    // save Operator
                                    if (p < input.length() - 1 && input.charAt(p + 1) == '>') {//-> operator
                                        p++;//move by offset
                                        parts.add(new OperatorNode("->"));
                                    } else {//- Operator
                                        parts.add(new OperatorNode("-"));
                                    }
                                    p0 = p + 1;
                                }break;
                                case '\''://string
                                case '"': {
                                    //end section
                                    String tmp = input.substring(p0, p).trim();
                                    if (tmp.length() > 0) {
                                        parts.add(new ValueNode(numberFromString(base, tmp, safeMode)));
                                    }
                                    p0 = p;//p instead of p0 to save start of string
                                    state = STATE_STRING;
                                }break;
                                case '[':
                                case '(':
                                case '{': {
                                    //end section
                                    String tmp = input.substring(p0, p).trim();
                                    if (tmp.length() > 0) {
                                        parts.add(new ValueNode(numberFromString(base, tmp, safeMode)));
                                    }
                                    p0 = p+1;//p instead of p0 to save start of bracket
                                    layer=1;
                                    state= input.charAt(p)=='{'?STATE_SET:input.charAt(p)=='['?STATE_TUPLE:STATE_VALUE;
                                }break;
                                default:{
                                    if(Character.isWhitespace(input.charAt(p))) {
                                        //end section at every illegal character
                                        String tmp = input.substring(p0, p).trim();
                                        if (tmp.length() > 0) {
                                            parts.add(new ValueNode(numberFromString(base, tmp, safeMode)));
                                        }
                                        p0 = p + 1;
                                    }else if(safeMode){//ignore illegal characters
                                        input=input.substring(0,p)+input.substring(p+1);
                                        p--;
                                    }else{
                                        throw new IllegalArgumentException("illegal Character: "+input.charAt(p));
                                    }
                                } break;
                            }
                        }
                    }break;
                    case STATE_TUPLE: {
                        if (input.charAt(p) == ']') {//end tuple
                            layer--;
                            if(layer==0){
                                String tmp = input.substring(p0, p);
                                parts.add(new ValueNode(fromString(tmp,base,MODE_TUPLE,safeMode)));
                                p0 = p + 1;
                                state = STATE_NUMBER;
                            }
                        }else if(input.charAt(p) == '['){
                            layer++;
                        }
                    }break;
                    case STATE_STRING: {
                        if (input.charAt(p) == input.charAt(p0)) {//end string
                            String tmp = input.substring(p0 + 1, p);
                            parts.add(new ValueNode(Real.from(Real.stringAsBigInt(tmp))));
                            p0 = p + 1;
                            state = STATE_NUMBER;
                        } else if (input.charAt(p) == '\\') {//skip next char
                            if(p+1<input.length()){
                                String escaped=null;
                                int next;
                                switch (input.charAt(p+1)){
                                    case 'n':escaped="\n";next=p+2;break;
                                    case 't':escaped="\t";next=p+2;break;
                                    case 'b':escaped="\b";next=p+2;break;
                                    case 'f':escaped="\f";next=p+2;break;
                                    case 'r':escaped="\r";next=p+2;break;
                                    case '"':escaped="\"";next=p+2;break;
                                    case '\'':escaped="'";next=p+2;break;
                                    case '\\':escaped="\\";next=p+2;break;
                                    case 'u':
                                    case 'U': {//Unicode Characters
                                        StringBuilder str=new StringBuilder();
                                        next=p+2;
                                        int rem;
                                        if(input.charAt(p+1)=='U'){
                                            char c=input.charAt(next);
                                            if(c=='0'){//U0_____
                                                str.append(c);
                                                next++;
                                                rem=5;
                                            }else if(c=='1'){
                                                str.append(c);
                                                next++;
                                                c=input.charAt(next);
                                                if(c=='0'){//U10____
                                                    str.append(c);
                                                    next++;
                                                    rem=4;
                                                }else if(safeMode){
                                                    switch (Character.toLowerCase(c)){
                                                        case '2': case '3':
                                                        case '4': case '5': case '6': case '7':
                                                        case '8': case '9': case 'a': case 'b':
                                                        case 'c': case 'd': case 'e': case 'f':
                                                            rem=3;
                                                            break;
                                                        default:
                                                            rem=0;
                                                            break;
                                                    }
                                                }else{
                                                    throw new IllegalArgumentException("Illegal escape sequence: \\U1"+c);
                                                }
                                            }else if(safeMode){
                                                switch (Character.toLowerCase(c)){
                                                    case '2': case '3':
                                                    case '4': case '5': case '6': case '7':
                                                    case '8': case '9': case 'a': case 'b':
                                                    case 'c': case 'd': case 'e': case 'f':
                                                        rem=4;
                                                        break;
                                                    default:
                                                        rem=0;
                                                        break;
                                                }
                                            }else{
                                                throw new IllegalArgumentException("illegal escape sequence: \\U"+c);
                                            }
                                        }else{
                                            rem=4;
                                        }
                                        for(int i=0;i<rem;i++){
                                            char c=input.charAt(next);
                                            switch (Character.toLowerCase(c)){
                                                case '0': case '1': case '2': case '3':
                                                case '4': case '5': case '6': case '7':
                                                case '8': case '9': case 'a': case 'b':
                                                case 'c': case 'd': case 'e': case 'f':
                                                    str.append(c);
                                                    next++;
                                                    break;
                                                default:
                                                    if(safeMode){
                                                        i=rem;//end loop
                                                        break;
                                                    }else{
                                                        throw new IllegalArgumentException("Illegal escape sequence: \\u"+str+c);
                                                    }
                                            }
                                        }
                                        escaped=String.valueOf(Character.toChars(
                                                Integer.parseInt(str.toString(),16)));
                                    }break;
                                    default:if(!safeMode){
                                        throw new IllegalArgumentException("illegal escape Sequence:\\"
                                                +input.charAt(p+1));
                                    }else{
                                        p++;//skip escaped character
                                        next=p+2;
                                    }
                                }
                                if(escaped!=null){
                                    input=input.substring(0,p)+escaped+
                                            input.substring(next);
                                    p+=escaped.length()-1;//balance p++
                                }
                            }
                        }
                    }break;
                    case STATE_VALUE: {
                        if (input.charAt(p) == ')') {//end bracket
                            layer--;
                            if(layer==0) {
                                String tmp = input.substring(p0, p);
                                parts.add(new ValueNode(
                                        fromString(tmp, base, MODE_VALUE, safeMode)));
                                p0 = p + 1;
                                state = STATE_NUMBER;
                            }
                        }else if(input.charAt(p) == '('){
                            layer++;
                        }
                    }break;
                    case STATE_SET: {
                        if (input.charAt(p) == '}') {//end bracket
                            layer--;
                            if(layer==0){
                                String tmp = input.substring(p0, p);
                                parts.add(new ValueNode(
                                        fromString(tmp, base, MODE_SET, safeMode)));
                                p0 = p + 1;
                                state = STATE_NUMBER;
                            }
                        }else if(input.charAt(p) == '{'){
                            layer++;
                        }
                    }break;
                }
            }
            switch (state){
                case STATE_VALUE:
                case STATE_TUPLE:
                case STATE_SET:
                    if(safeMode) {
                        parts.add(new ValueNode(fromString(input.substring(p0).trim()
                                , base, state == STATE_SET ? MODE_SET : state == STATE_TUPLE ? MODE_TUPLE:MODE_VALUE, true)));
                    }else{
                        throw new IllegalArgumentException("Input String contains unfinished Bracket: "+input);
                    }
                    break;
                case STATE_STRING:
                    if(safeMode) {
                        parts.add(new ValueNode(Real.from(Real.stringAsBigInt(
                                input.substring(p0 + 1).trim()))));
                    }else{
                            throw new IllegalArgumentException("Input String contains unfinished String: "+input);
                    }
                    break;
                case STATE_NUMBER:
                    if(p0< input.length())
                        parts.add(new ValueNode(numberFromString(base, input.substring(p0), safeMode)));break;
                default: throw new RuntimeException("Unexpected State:"+state);
            }

            //1. ^
            for(int i=parts.size()-2;i>0;i--){
                if(parts.get(i) instanceof OperatorNode&&((OperatorNode) parts.get(i)).value.equals("^")){
                    MathObject l,r;
                    r = nextValue(parts, i+1,'^',Real.Int.ZERO, safeMode);
                    parts.remove(i);
                    if(parts.get(i-1) instanceof ValueNode){
                        l=((ValueNode) parts.get(i-1)).value;
                        parts.set(i-1,new ValueNode(pow(l,r)));
                    }else{
                        throw new IllegalArgumentException("Missing 1st argument for A^B");
                    }
                    i--;//update position
                }
            }
            //2. | * / \
            for(int i=0;i<parts.size();i++){
                if(parts.get(i) instanceof OperatorNode){
                    switch (((OperatorNode) parts.get(i)).value) {
                        case "|": {
                            MathObject l, r = nextValue(parts, i+1, '|',Real.Int.ZERO, safeMode);
                            if (i > 0 && parts.get(i - 1) instanceof ValueNode) {
                                parts.remove(i);
                                l = ((ValueNode) parts.get(i - 1)).value;
                                parts.set(i - 1, new ValueNode(add(l, multiply(r, Complex.I))));
                                i--;//update position
                            } else {
                                parts.set(i, new ValueNode(multiply(r, Complex.I)));
                            }
                            break;
                        }
                        case "*": {
                            MathObject l, r = nextValue(parts, i+1, '*',Real.Int.ZERO, safeMode);
                            parts.remove(i);
                            if (parts.get(i - 1) instanceof ValueNode) {
                                l = ((ValueNode) parts.get(i - 1)).value;
                            } else {
                                throw new IllegalArgumentException("Missing 1st argument for A*B");
                            }
                            parts.set(i - 1, new ValueNode(multiply(l, r)));
                            i--;//update position

                            break;
                        }
                        case "/": {
                            MathObject l, r = nextValue(parts, i+1, '/',Real.Int.ZERO, safeMode);
                            parts.remove(i);
                            if (parts.get(i - 1) instanceof ValueNode) {
                                l = ((ValueNode) parts.get(i - 1)).value;
                            } else {
                                throw new IllegalArgumentException("Missing 1st argument for A/B");
                            }
                            try {
                                parts.set(i - 1, new ValueNode(divide(l, r)));
                            }catch (ArithmeticException div0){
                                if(safeMode){
                                    parts.set(i - 1, new ValueNode(Real.Int.ZERO));
                                }else{
                                    throw div0;
                                }
                            }
                            i--;//update position

                            break;
                        }
                    }
                }else if(parts.get(i) instanceof ValueNode){//implicit multiplication i.e. a(b)
                    if(i>0&&parts.get(i-1) instanceof ValueNode){
                        parts.set(i-1,new ValueNode(multiply(((ValueNode) parts.get(i-1)).value
                                ,((ValueNode) parts.get(i)).value)));
                        parts.remove(i--);
                    }
                }
            }
            //4. + -
            for(int i=0;i<parts.size();i++){
                if(parts.get(i) instanceof OperatorNode){
                    if(((OperatorNode) parts.get(i)).value.equals("+")){
                        MathObject l,r=nextValue(parts,i+1,'+',Real.Int.ZERO, safeMode);
                        if(i>0&&parts.get(i-1) instanceof ValueNode){
                            parts.remove(i);
                            l=((ValueNode) parts.get(i-1)).value;
                            parts.set(i-1,new ValueNode(add(l,r)));
                            i--;//update position
                        }else{
                            parts.set(i,new ValueNode(r));
                        }
                    }else if(((OperatorNode) parts.get(i)).value.equals("-")){
                        MathObject l,r=nextValue(parts,i+1,'-',Real.Int.ZERO, safeMode);
                        if(i>0&&parts.get(i-1) instanceof ValueNode){
                            parts.remove(i);
                            l=((ValueNode) parts.get(i-1)).value;
                            parts.set(i-1,new ValueNode(subtract(l,r)));
                            i--;//update position
                        }else{
                            parts.set(i,new ValueNode(negate(r)));
                        }
                    }
                }
            }
            boolean isMap=false,topLevel=true;
            //5. ->
            for(int i=parts.size()-1;i>=0;i--){//addLater? detect multi-maps: {1->2->3,1->3->4} -> {1->{2->3,3->4}}
                if(parts.get(i) instanceof OperatorNode){
                    switch (((OperatorNode) parts.get(i)).value) {
                        case "->":
                        case "→":
                        case "↦":{
                            if(mode==MODE_SET||safeMode) {
                                isMap = true;
                                MathObject l, r = nextValue(parts, i + 1, '↦', Real.Int.ZERO, safeMode);
                                if (i > 0 && parts.get(i - 1) instanceof ValueNode) {
                                    parts.remove(i);
                                    l = ((ValueNode) parts.get(i - 1)).value;
                                    if(mode==MODE_SET) {
                                        parts.set(i - 1, new ValueNode(new Pair(l, r)));
                                    }else{
                                        parts.set(i - 1, new ValueNode(FiniteMap
                                                .from(Collections.singletonMap(l, r), FiniteMap.TUPLE_WRAP_ZERO_TERMINATED)));
                                    }
                                    i--;//update position
                                } else {
                                    if(mode==MODE_SET){
                                        parts.set(i,
                                            new ValueNode(new Pair(FiniteSet.EMPTY_SET, r)));
                                    }else{
                                        parts.set(i,new ValueNode(FiniteMap
                                                .from(Collections.singletonMap(FiniteSet.EMPTY_SET, r), FiniteMap.TUPLE_WRAP_ZERO_TERMINATED)));

                                    }
                                }
                            }else{
                                throw new IllegalArgumentException("MapElement in Tuple");
                            }
                            break;


                        }
                    }
                }
            }
            //6. , ;
            for(int i=0;i<parts.size();i++){
                if(parts.get(i) instanceof OperatorNode){
                    switch (((OperatorNode) parts.get(i)).value) {
                        case ",":
                        case ";": {
                            MathObject l, r = nextValue(parts, i + 1, ',',
                                        isMap ? new Pair(FiniteSet.EMPTY_SET, Real.Int.ZERO)
                                                : Real.Int.ZERO, safeMode||mode==MODE_TUPLE);
                            if (i > 0 && parts.get(i - 1) instanceof ValueNode) {
                                parts.remove(i);
                                l = ((ValueNode) parts.get(i - 1)).value;
                                if(topLevel){
                                    if(mode==MODE_SET){
                                        parts.set(i - 1, new ValueNode(FiniteSet.from(l,r)));
                                    }else if(safeMode||mode==MODE_TUPLE){
                                        parts.set(i - 1, new ValueNode(Tuple.create(
                                                new MathObject[]{l,r})));
                                    }else{
                                        throw new IllegalArgumentException("unexpected ,");
                                    }
                                    topLevel=false;
                                }else{
                                    if(mode==MODE_SET){
                                        parts.set(i - 1, new ValueNode(unite(l, FiniteSet.from(r))));
                                    }else  if(safeMode||mode==MODE_TUPLE){
                                        parts.set(i - 1, new ValueNode(tupleConcat(l,
                                                Tuple.create(new MathObject[]{r}))));
                                    }else{
                                        throw new IllegalArgumentException("unexpected ,");
                                    }
                                }
                                i--;//update position
                            } else {
                                if(safeMode||mode==MODE_TUPLE) {
                                    parts.set(i, new ValueNode(mode == MODE_SET ? FiniteSet.from(Real.Int.ZERO, r) :
                                            Tuple.create(new MathObject[]{Real.Int.ZERO, r})));
                                    topLevel = false;
                                }else{
                                    throw new IllegalArgumentException("unexpected ,");
                                }
                            }
                            break;
                        }
                    }
                }
            }
            if(parts.size()!=1)
                if(safeMode) {
                    System.err.println("Unable to resolve Value: " + parts);
                }else{
                    throw new IllegalArgumentException("Unable to resolve Value of \""+input
                            +"\": " + parts);
                }
            for (Node part : parts) {
                if (part instanceof ValueNode){
                    MathObject returnValue=((ValueNode) parts.get(0)).value;
                    if (topLevel){
                        if(mode==MODE_SET||(safeMode&&isMap)) {
                            returnValue=FiniteSet.from(returnValue);
                        }else if(mode==MODE_TUPLE) {
                            returnValue=Tuple.create(new MathObject[]{returnValue});
                        }
                    }
                    if(isMap){
                        if(returnValue instanceof FiniteSet){
                            return ((FiniteSet) returnValue).asMap();
                        }else{
                            throw new RuntimeException("isMap is true, expected value:false");
                        }
                    }
                    return returnValue;
                }
            }
            return Real.Int.ZERO;
        }


        private static NumericValue numberFromString(BigInteger base, String str, boolean safeMode) {
            //dynamic base: $-> bin #->hex §->doz @base: ->BaseN //addLater? dec prefix
            if(str.startsWith("$")){//bin
                str=str.substring(1);
                base= Constants.BIG_INT_TWO;
            }else if(str.startsWith("#")){//hex
                str=str.substring(1);
                base=Constants.BIG_INT_SIXTEEN;
            }else if(str.startsWith("§")){//doz
                str=str.substring(1);
                base=Constants.BIG_INT_TWELVE;
            }else if(str.startsWith("@")){//baseN
                str=str.substring(1);
                int tmp=str.indexOf(':');
                if(tmp<0){
                    if(safeMode){
                        return Real.Int.ZERO;
                    }else{
                        throw new IllegalArgumentException("Unfinished BaseN number: @"+str);
                    }
                }
                String baseStr = str.substring(0, tmp);
                if(safeMode) {
                    baseStr = removeIllegalCharacters(Real.digitBase, baseStr, false);
                }
                base= Real.bigIntFromString(baseStr,Real.digitBase).num();
                str=str.substring(tmp+1);
            }
            int iCount=0;
            boolean has_i=base.compareTo(Constants.MAX_INT)<0&&base.intValueExact()<Real.DIGITS.indexOf("i");
            boolean has_I=base.compareTo(Constants.MAX_INT)<0&&base.intValueExact()<Real.DIGITS.indexOf("I");
            if(has_i||has_I){
                int i=str.length()-1;
                for(;i>=0;i--){
                    if(has_i&&str.charAt(i)=='i'){
                        iCount++;
                    }else if(has_I&&str.charAt(i)=='I'){
                        iCount++;
                    }else{
                        break;
                    }
                }
                str=str.substring(0,i+1);
                if(iCount>0&&str.length()==0){
                    str="1";
                }
                iCount=iCount%4;
            }
            if(safeMode){
                str = removeIllegalCharacters(base, str, true);
            }else{
                str=str.trim();
                if(str.isEmpty())
                    throw new IllegalArgumentException("empty input String in numberFromString");
            }
            Real real=Real.bigIntFromString(str, base);
            switch (iCount){
                case 0:return real;
                case 1:return Complex.from(Real.Int.ZERO,real);
                case 2:return real.negate();
                case 3:return Complex.from(Real.Int.ZERO,real.negate());
                default:throw new RuntimeException("Unexpected iCount:"+iCount);
            }
        }

        @NotNull
        private static String removeIllegalCharacters(BigInteger base, String str, boolean allowPoint) {
            String allowedCharacters;
            if(str.contains(":")|| base.compareTo(Constants.MAX_INT)>=0||base.intValueExact() >Real.DIGITS.length()){
                allowedCharacters=Real.DIGITS.substring(0,Real.digitBase.intValueExact())+":";
            }else{
                allowedCharacters=Real.DIGITS.substring(0, base.intValueExact());
            }
            StringBuilder filteredCharacters=new StringBuilder(str.length());
            for(char c: str.toCharArray()){
                if(allowedCharacters.contains(""+c)) {
                    filteredCharacters.append(c);
                }else if(c=='.'&&allowPoint){
                    filteredCharacters.append('.');
                    allowPoint =false;
                }
            }
            str =filteredCharacters.toString();
            return str;
        }


        private static MathObject nextValue(ArrayList<Node> parts, int i, char operator, MathObject replaceEmpty, boolean safeMode) {
            MathObject r;
            if(i>=parts.size()){
                if(safeMode) {
                    return replaceEmpty;
                }else{
                    throw new IllegalArgumentException("Missing 2nd argument for A"+operator+"B");
                }
            }else if(parts.get(i) instanceof ValueNode){
                r=((ValueNode) parts.remove(i)).value;
            }else{
                boolean sgn=false;
                while(i<parts.size()&&parts.get(i) instanceof OperatorNode){
                    if(((OperatorNode) parts.get(i)).value.equals("+")){
                        parts.remove(i);
                    }else if(((OperatorNode) parts.get(i)).value.equals("-")){
                        sgn=!sgn;
                        parts.remove(i);
                    }else {
                        if(safeMode) {
                            return replaceEmpty;
                        }else{
                            throw new IllegalArgumentException("Unexpected operator:"
                                    +((OperatorNode) parts.get(i)).value);
                        }
                    }
                }
                if(i<parts.size()&&parts.get(i) instanceof ValueNode){
                    r=((ValueNode) parts.remove(i)).value;
                    if(sgn){
                        r=negate(r);
                    }
                }else if(safeMode){
                    return replaceEmpty;
                }else {
                    throw new IllegalArgumentException("Missing 2nd argument for A" + operator + "B");
                }
            }
            return r;
        }

    }
}
