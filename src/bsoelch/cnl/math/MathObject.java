package bsoelch.cnl.math;

import bsoelch.cnl.Constants;
import bsoelch.cnl.math.expression.ExpressionNode;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**Root of all MathObjects*/
public interface MathObject extends ExpressionNode {
    int FLOOR = -1;
    int ROUND = 0;
    int CIEL = 1;

    default Scalar.NumericScalar numericValue() {
        return scalarValue().numericValue();
    }
    Scalar scalarValue();

    String toString(BigInteger base, boolean useSmallBase);
    String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase);
    String toStringFloat(BigInteger base, Real precision, boolean useSmallBase);

    /**String representing this Object*/
    default String asString(){//TODO? better implementation (concat partial strings)
        return numericValue().asString();
    }
    String intsAsString();

    @Override
    default Set<Variable> variables() {
        return Collections.emptySet();
    }
    @Override
    default ExpressionNode evaluate(Map<Variable, ExpressionNode> replace) {
        return this;
    }

    static MathObject not(MathObject o) {
        return isTrue(o)?Real.Int.ZERO:Real.Int.ONE;
    }

    /**@return the boolean value of the supplied MathObject,
     * the only objects resulting in false are 0, empty sets and empty maps/tuples */
    static boolean isTrue(MathObject arg) {
        return !(arg.equals(Real.Int.ZERO)||arg.equals(FiniteSet.EMPTY_SET)
                ||arg.equals(FiniteMap.EMPTY_MAP));
    }

    static FiniteSet asSet(MathObject o){
        if(o instanceof Scalar){
            return FiniteSet.from(o);
        }else if(o instanceof FiniteSet){
            return (FiniteSet)o;
        }else if(o instanceof FiniteMap){
            return ((FiniteMap)o).asSet();
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }
    static FiniteMap asMap(MathObject o){
        if(o instanceof Scalar){
            return Tuple.create(new MathObject[]{o});
        }else if(o instanceof FiniteSet){
            return ((FiniteSet)o).asMap();
        }else if(o instanceof FiniteMap){
            return ((FiniteMap)o);
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }

    static MathObject elementWise(MathObject l, MathObject r,
                                  BiFunction<Scalar,Scalar,Scalar> scalarOperation){
        if(l instanceof Scalar){
            if(r instanceof Scalar){
                return scalarOperation.apply((Scalar) l,((Scalar)r));
            }else if(r instanceof FiniteSet){
                return FiniteSet.forEach((FiniteSet) r, o->elementWise(l,o,scalarOperation));
            }else if(r instanceof FiniteMap){
                return ((FiniteMap) r).forEach(o->elementWise(l,o,scalarOperation));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteSet){
            if(r instanceof Scalar){
                return FiniteSet.forEach((FiniteSet) l, o->elementWise(o,r,scalarOperation));
            }else if(r instanceof FiniteSet){
                return FiniteSet.forEachPair((FiniteSet) l,(FiniteSet) r, (a,b)->elementWise(a,b,scalarOperation));
            }else if(r instanceof FiniteMap){
                return FiniteMap.forEach(FiniteMap.indicatorMap((FiniteSet) l),(FiniteMap) r, (a,b)->elementWise(a,b,scalarOperation));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteMap){
            if(r instanceof Scalar){
                return ((FiniteMap) l).forEach(o->elementWise(o,r,scalarOperation));
            }else if(r instanceof FiniteSet){
                return FiniteMap.forEach((FiniteMap) l, FiniteMap.indicatorMap((FiniteSet) r), (a,b)->elementWise(a,b,scalarOperation));
            }else if(r instanceof FiniteMap){
                return FiniteMap.forEach((FiniteMap) l,(FiniteMap) r, (a,b)->elementWise(a,b,scalarOperation));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+l.getClass());
        }
    }
    static MathObject elementWise(MathObject o, Function<Scalar,Scalar> scalarFunction){
        if(o instanceof Scalar){
            return scalarFunction.apply((Scalar) o);
        }else if(o instanceof FiniteSet){
            return FiniteSet.forEach((FiniteSet) o, e->elementWise(e,scalarFunction));
        }else if(o instanceof FiniteMap){
            return ((FiniteMap) o).forEach(e->elementWise(e,scalarFunction));
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }

    static MathObject add(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::add);
    }
    static MathObject sum(MathObject[] objects){
        MathObject sum=Real.Int.ZERO;
        for (MathObject arg : objects) {
            sum = MathObject.add(sum, arg);
        }
        return sum;
    }

    static MathObject negate(MathObject o) {
        return elementWise(o,Scalar::negate);
    }
    static MathObject realPart(MathObject o) {
        return elementWise(o,Scalar::realPart);
    }
    static MathObject imaginaryPart(MathObject o) {
        return elementWise(o,Scalar::imaginaryPart);
    }
    static MathObject conjugate(MathObject o) {
        return elementWise(o,Scalar::conjugate);
    }
    static MathObject subtract(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::subtract);
    }
    static MathObject multiply(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::multiply);
    }
    static MathObject product(MathObject[] objects){
        MathObject prod=Real.Int.ONE;
        for (MathObject arg : objects) {
            prod = MathObject.multiply(prod, arg);
        }
        return prod;
    }

    static Real sqAbs(MathObject o) {
        if(o instanceof Scalar){
            return ((Scalar) o).sqAbs();
        }else if(o instanceof FiniteSet){
            Real sqAbs=Real.Int.ZERO;
            for(MathObject e:(FiniteSet)o){
                sqAbs=Real.add(sqAbs,sqAbs(e));
            }
            return sqAbs;
        }else if(o instanceof FiniteMap){
            Real sqAbs=Real.Int.ZERO;
            for (Iterator<Pair> it = ((FiniteMap) o).mapIterator(); it.hasNext(); ) {
                MathObject e = it.next();
                sqAbs=Real.add(sqAbs,sqAbs(e));
            }
            return sqAbs;
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }

    static MathObject invert(MathObject o) {
        return elementWise(o,Scalar::invert);
    }

    static MathObject divide(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::divide);
    }

    static MathObject mod(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::mod);
    }
    static MathObject pow(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::pow);
    }
    static MathObject round(MathObject o, int mode) {
        return elementWise(o,e->e.round(mode));
    }
    static MathObject approximate(MathObject o, Real precision) {
        return elementWise(o,e->e.approx(precision));
    }

    static MathObject floorAnd(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::floorAnd);
    }
    static MathObject floorOr(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::floorOr);
    }
    static MathObject floorXor(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::floorXor);
    }
    static MathObject floorAndNot(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::floorAndNot);
    }

    /**@param rewrapMaps if this value if true the program tries
     *                    to convert the result to a map if one of the arguments was a map
     * */
    static MathObject setOperation(MathObject l, MathObject r,
                   BiFunction<FiniteSet, FiniteSet, FiniteSet> setOp,
                   BiFunction<Scalar, Scalar, MathObject> scalarOp, boolean rewrapMaps){
        if(l instanceof Scalar){
            if(r instanceof Scalar){
                return scalarOp.apply((Scalar) l,(Scalar) r);
            }else if(r instanceof FiniteSet){
                return setOp.apply(FiniteSet.from(l),(FiniteSet) r);
            }else if(r instanceof FiniteMap){
                FiniteSet set = setOp.apply(FiniteSet.from(l), ((FiniteMap) r).asSet());
                return rewrapMaps?set.asMapIfPossible():set;
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteSet){
            if(r instanceof Scalar){
                return setOp.apply((FiniteSet) l, FiniteSet.from(r));
            }else if(r instanceof FiniteSet){
                return setOp.apply((FiniteSet) l,(FiniteSet) r);
            }else if(r instanceof FiniteMap){
                FiniteSet set = setOp.apply((FiniteSet) l, ((FiniteMap) r).asSet());
                return rewrapMaps?set.asMapIfPossible():set;
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteMap){
            if(r instanceof Scalar){
                FiniteSet set = setOp.apply(((FiniteMap) l).asSet(), FiniteSet.from(r));
                return rewrapMaps?set.asMapIfPossible():set;
            }else if(r instanceof FiniteSet){
                FiniteSet set = setOp.apply(((FiniteMap) l).asSet(), (FiniteSet) r);
                return rewrapMaps?set.asMapIfPossible():set;
            }else if(r instanceof FiniteMap){
                FiniteSet set = setOp.apply(((FiniteMap) l).asSet(), ((FiniteMap) r).asSet());
                return rewrapMaps?set.asMapIfPossible():set;
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+l.getClass());
        }
    }

    static MathObject intersect(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::intersect,
                (a,b)->a.equals(b)?FiniteSet.from(a):FiniteSet.EMPTY_SET, true);
    }
    static MathObject unite(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::unite,FiniteSet::from, true);
    }
    static MathObject symmetricDifference(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::symmetricDifference,
                (a,b)->a.equals(b)?FiniteSet.EMPTY_SET:FiniteSet.from(a,b), true);
    }
    static MathObject difference(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::difference,
                (a,b)->a.equals(b)?FiniteSet.EMPTY_SET:FiniteSet.from(a), true);
    }

    //TODO MatrixProduct
    static MathObject times(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::product,Pair::new, false);
    }
    static MathObject nAryTimes(MathObject[] objects){
        FiniteSet[] sets=new FiniteSet[objects.length];
        for (int i=0;i<objects.length;i++) {
            if(objects[i] instanceof Scalar){
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
    static MathObject fAdd(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::fAdd);
    }
    static MathObject strConcat(MathObject l, MathObject r) {
        return elementWise(l,r,Scalar::strConcat);
    }

    static Scalar min(MathObject l, MathObject r) {
        if(l instanceof Scalar){
            if(r instanceof Scalar){
                return Scalar.min((Scalar) l,((Scalar)r));
            }else if(r instanceof FiniteSet){
                Scalar ret=(Scalar) l;
                for(MathObject o:(FiniteSet) r){
                    ret=min(ret,o);
                }
                return ret;
            }else if(r instanceof FiniteMap){
                Scalar ret=(Scalar) l;
                for(MathObject o:(((FiniteMap) r).values())){
                    ret=min(ret,o);
                }
                return ret;
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteSet){
            if(r instanceof Scalar){
                Scalar ret=(Scalar) r;
                for(MathObject o:(FiniteSet) l){
                    ret=min(ret,o);
                }
                return ret;
            }else if(r instanceof FiniteSet){
                Scalar ret=null;
                for(MathObject o1:(FiniteSet) l){
                    for(MathObject o2:(FiniteSet) r){
                        if(ret==null){
                            ret=min(o1,o2);
                        }else{
                            ret=min(ret,min(o1,o2));
                        }
                    }
                }
                return ret==null?Real.Int.ZERO:ret;
            }else if(r instanceof FiniteMap){
                Scalar ret=null;
                for(MathObject o1:(FiniteSet) l){
                    for(MathObject o2:((FiniteMap)r).values()){
                        if(ret==null){
                            ret=min(o1,o2);
                        }else{
                            ret=min(ret,min(o1,o2));
                        }
                    }
                }
                return ret==null?Real.Int.ZERO:ret;
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteMap){
            if(r instanceof Scalar){
                Scalar ret=(Scalar) r;
                for(MathObject o:((FiniteMap) l).values()){
                    ret=min(ret,o);
                }
                return ret;
            }else if(r instanceof FiniteSet){
                Scalar ret=null;
                for(MathObject o1:((FiniteMap) l).values()){
                    for(MathObject o2:(FiniteSet) r){
                        if(ret==null){
                            ret=min(o1,o2);
                        }else{
                            ret=min(ret,min(o1,o2));
                        }
                    }
                }
                return ret==null?Real.Int.ZERO:ret;
            }else if(r instanceof FiniteMap){
                Scalar ret=null;
                for(MathObject o1:((FiniteMap) l).values()){
                    for(MathObject o2:((FiniteMap) r).values()){
                        if(ret==null){
                            ret=min(o1,o2);
                        }else{
                            ret=min(ret,min(o1,o2));
                        }
                    }
                }
                return ret==null?Real.Int.ZERO:ret;
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+l.getClass());
        }
    }
    static Scalar max(MathObject l, MathObject r) {
        if(l instanceof Scalar){
            if(r instanceof Scalar){
                return Scalar.max((Scalar) l,((Scalar)r));
            }else if(r instanceof FiniteSet){
                Scalar ret=(Scalar) l;
                for(MathObject o:(FiniteSet) r){
                    ret=max(ret,o);
                }
                return ret;
            }else if(r instanceof FiniteMap){
                Scalar ret=(Scalar) l;
                for(MathObject o:(((FiniteMap) r).values())){
                    ret=max(ret,o);
                }
                return ret;
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteSet){
            if(r instanceof Scalar){
                Scalar ret=(Scalar) r;
                for(MathObject o:(FiniteSet) l){
                    ret=max(ret,o);
                }
                return ret;
            }else if(r instanceof FiniteSet){
                Scalar ret=null;
                for(MathObject o1:(FiniteSet) l){
                    for(MathObject o2:(FiniteSet) r){
                        if(ret==null){
                            ret=max(o1,o2);
                        }else{
                            ret=max(ret,max(o1,o2));
                        }
                    }
                }
                return ret==null?Real.Int.ZERO:ret;
            }else if(r instanceof FiniteMap){
                Scalar ret=null;
                for(MathObject o1:(FiniteSet) l){
                    for(MathObject o2:((FiniteMap)r).values()){
                        if(ret==null){
                            ret=max(o1,o2);
                        }else{
                            ret=max(ret,max(o1,o2));
                        }
                    }
                }
                return ret==null?Real.Int.ZERO:ret;
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteMap){
            if(r instanceof Scalar){
                Scalar ret=(Scalar) r;
                for(MathObject o:((FiniteMap) l).values()){
                    ret=max(ret,o);
                }
                return ret;
            }else if(r instanceof FiniteSet){
                Scalar ret=null;
                for(MathObject o1:((FiniteMap) l).values()){
                    for(MathObject o2:(FiniteSet) r){
                        if(ret==null){
                            ret=max(o1,o2);
                        }else{
                            ret=max(ret,max(o1,o2));
                        }
                    }
                }
                return ret==null?Real.Int.ZERO:ret;
            }else if(r instanceof FiniteMap){
                Scalar ret=null;
                for(MathObject o1:((FiniteMap) l).values()){
                    for(MathObject o2:((FiniteMap) r).values()){
                        if(ret==null){
                            ret=max(o1,o2);
                        }else{
                            ret=max(ret,max(o1,o2));
                        }
                    }
                }
                return ret==null?Real.Int.ZERO:ret;
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+l.getClass());
        }
    }

    static Tuple tupleConcat(MathObject a,MathObject b) {
        int l;
        if(a instanceof Tuple){
            l=((Tuple) a).size();
        }else{
            l=1;
        }
        if(b instanceof Tuple){
            l+=((Tuple) b).size();
        }else{
            l+=1;
        }
        MathObject[] objects=new MathObject[l];
        if(a instanceof Tuple){
            for(l=0;l<((Tuple) a).size();l++){
                objects[l]=((Tuple) a).get(l);
            }
        }else{
            objects[0]=a;
            l=1;
        }
        if(b instanceof Tuple){
            for(int t=0;t<((Tuple) b).size();t++,l++){
                objects[l]=((Tuple) b).get(t);
            }
        }else{
            objects[l]=b;
        }
        return Tuple.create(objects);
    }


    static int compare(MathObject a, MathObject b){
        if(a instanceof Scalar){
            if(b instanceof Scalar){
                return ((Scalar) a).compareTo((Scalar) b);
            }else if(b instanceof FiniteSet){
                if(((FiniteSet) b).size()==0){
                    return 1;
                }else{
                    Iterator<MathObject> itr=((FiniteSet) b).iterator();
                    int c;
                    while(itr.hasNext()){
                        c=compare(a,itr.next());
                        if(c!=0)
                            return c;
                    }
                    return -1;// a < {a}
                }
            }else if(b instanceof FiniteMap){
                Iterator<Pair> itr=((FiniteMap)b).mapIterator();
                int c;
                while(itr.hasNext()){
                    c=compare(a,itr.next().b);
                    if(c!=0)
                        return c;
                }
                return -1;// a < {a}
            }
        }else if(a instanceof FiniteSet){
            if(b instanceof Scalar){
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
            if(b instanceof Scalar){
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
        return a==null?b==null?0:1:-1;
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




    class FromString{
        static private final int STATE_NUMBER=0,STATE_VARIABLE=1,STATE_STRING=2,STATE_VALUE=3,STATE_SET=4;
        static private final int MODE_SET=1,MODE_ROOT=0,MODE_TUPLE=-1;

        private interface Node{}
        private static class OperatorNode implements Node{
            final String value;
            private OperatorNode(String value) {
                this.value = value;
            }
        }
        private static class ValueNode implements Node{
            final MathObject value;
            private ValueNode(MathObject value) {
                this.value = value;
            }
        }
        private static class VariableNode implements Node{
            BigInteger power=BigInteger.ONE;
            final BigInteger varId;
            private VariableNode(BigInteger base,String str,boolean safeMode) {
                try {
                    this.varId = numberFromString(base, str, safeMode)
                            .round(FLOOR).realPart().num();
                }catch (IllegalArgumentException iae){
                    throw new IllegalArgumentException("invalid VariableId: "+str,iae);
                }
            }
        }

        static public MathObject fromString(String input, BigInteger base){
            return fromString(input, base,MODE_ROOT, false);
        }
        static public MathObject safeFromString(String input, BigInteger base){
            return fromString(input, base,MODE_ROOT, true);
        }

        /**Initiates a MathObject from the given string value
         * @param input String that should be converted
         * @param base Base for the conversion
         * @param mode Type for the wrapping Object
         * @param safeMode if true no Exceptions will be thrown
         * @throws IllegalArgumentException if input represents no MathObject*///TODO better explanation of safeMode
        private static MathObject fromString(String input, BigInteger base, int mode, boolean safeMode) {
            if(input.isEmpty())
                switch (mode){
                    case MODE_TUPLE:
                    case MODE_ROOT:return Real.Int.ZERO;
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
                                case '@'://base modifieres
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
                                case '[': {//Variable
                                    //end section
                                    String tmp = input.substring(p0, p).trim();
                                    if (tmp.length() > 0) {
                                        parts.add(new ValueNode(numberFromString(base, tmp, safeMode)));
                                    }
                                    p0 = p + 1;
                                    state = STATE_VARIABLE;
                                    break;
                                }
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
                                case '(':
                                case '{': {
                                    //end section
                                    String tmp = input.substring(p0, p).trim();
                                    if (tmp.length() > 0) {
                                        parts.add(new ValueNode(numberFromString(base, tmp, safeMode)));
                                    }
                                    p0 = p+1;//p instead of p0 to save start of bracket
                                    layer=1;
                                    state= input.charAt(p)=='{'?STATE_SET:STATE_VALUE;
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
                    case STATE_VARIABLE: {
                        if (input.charAt(p) == ']') {//end matrix
                            parts.add(new VariableNode(base,input.substring(p0,p).trim(),safeMode));
                            p0 = p + 1;
                            state = STATE_NUMBER;
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
                                        fromString(tmp, base, MODE_TUPLE, safeMode)));
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
                case STATE_SET:
                    if(safeMode) {
                        parts.add(new ValueNode(fromString(input.substring(p0).trim()
                                , base, state == STATE_SET ? MODE_SET : MODE_TUPLE, true)));
                    }else{
                        throw new IllegalArgumentException("Input String contains unfinished Bracket: "+input);
                    }
                    break;
                case STATE_VARIABLE:
                    if(safeMode) {
                        parts.add(new VariableNode(base,input.substring(p0).trim(),true));
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
                        parts.add(new ValueNode(
                                numberFromString(base, input.substring(p0), safeMode)));break;
                default: throw new RuntimeException("Unexpected State:"+state);
            }

            //1. ^
            for(int i=parts.size()-2;i>0;i--){
                if(parts.get(i) instanceof OperatorNode&&((OperatorNode) parts.get(i)).value.equals("^")){
                    MathObject l,r;
                    r = nextValue(parts, i+1,'^',Real.Int.ZERO, safeMode);
                    parts.remove(i);
                    if(parts.get(i-1) instanceof VariableNode){
                        ((VariableNode) parts.get(i-1)).power=r.numericValue().round(MathObject.FLOOR).realPart().num();
                    }else if(parts.get(i-1) instanceof ValueNode){
                        l=((ValueNode) parts.get(i-1)).value;
                        parts.set(i-1,new ValueNode(pow(l,r)));
                    }else{
                        throw new IllegalArgumentException("Missing 1st argument for A^B");
                    }
                    i--;//Positon anpassen
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
                                i--;//Positon anpassen
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
                            i--;//Positon anpassen

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
                            parts.set(i - 1, new ValueNode(divide(l, r)));
                            i--;//Positon anpassen

                            break;
                        }
                    }
                }else if(parts.get(i) instanceof VariableNode){
                    if(i>0&&parts.get(i-1) instanceof ValueNode){
                        parts.set(i-1,new ValueNode(multiply(((ValueNode) parts.get(i-1)).value
                                ,Polynomial.from(((VariableNode) parts.get(i)).varId,((VariableNode) parts.get(i)).power))));
                        parts.remove(i--);
                    }else{
                        parts.set(i,new ValueNode(Polynomial.from(((VariableNode) parts.get(i)).varId
                                ,((VariableNode) parts.get(i)).power)));
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
                            i--;//Positon anpassen
                        }else{
                            parts.set(i,new ValueNode(r));
                        }
                    }else if(((OperatorNode) parts.get(i)).value.equals("-")){
                        MathObject l,r=nextValue(parts,i+1,'-',Real.Int.ZERO, safeMode);
                        if(i>0&&parts.get(i-1) instanceof ValueNode){
                            parts.remove(i);
                            l=((ValueNode) parts.get(i-1)).value;
                            parts.set(i-1,new ValueNode(subtract(l,r)));
                            i--;//Positon anpassen
                        }else{
                            parts.set(i,new ValueNode(negate(r)));
                        }
                    }
                }
            }
            boolean isMap=false,topLevel=true;
            //5. ->
            for(int i=0;i<parts.size();i++){
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
                                                .from(Collections.singletonMap(l, r),1)));
                                    }
                                    i--;//update position
                                } else {
                                    if(mode==MODE_SET){
                                        parts.set(i,
                                            new ValueNode(new Pair(FiniteSet.EMPTY_SET, r)));
                                    }else{
                                        parts.set(i,new ValueNode(FiniteMap
                                                .from(Collections.singletonMap(FiniteSet.EMPTY_SET, r),1)));

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
                                    }else{
                                        parts.set(i - 1, new ValueNode(Tuple.create(
                                                new MathObject[]{l,r})));
                                    }
                                    topLevel=false;
                                }else{
                                    if(mode==MODE_SET){
                                        parts.set(i - 1, new ValueNode(unite(l, asSet(r))));
                                    }else{
                                        parts.set(i - 1, new ValueNode(tupleConcat(l,
                                                Tuple.create(new MathObject[]{r}))));
                                    }
                                }
                                i--;//Positon anpassen
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
                        if(mode==MODE_SET) {
                            returnValue=FiniteSet.from(returnValue);
                        }//interpret topLevel in tuple as number in brackets
                    }
                    if(isMap){
                        if(returnValue instanceof FiniteSet){
                            return ((FiniteSet) returnValue).asMapIfPossible();
                        }else if(returnValue instanceof Tuple){
                            MathObject map= (topLevel?FiniteSet.from(returnValue)
                                    :((Tuple) returnValue).values()).asMapIfPossible();
                            if(map instanceof FiniteMap) {
                                return map;
                            }else {
                                return returnValue;
                            }
                        }
                    }
                    return returnValue;
                }
            }
            return Real.Int.ZERO;
        }


        private static Scalar.NumericScalar numberFromString(BigInteger base, String str, boolean safeMode) {
            //dynamic base: $-> bin #->hex §->dec @base: ->BaseN
            //TODO? doz
            if(str.startsWith("$")){//bin
                str=str.substring(1);
                base= Constants.BIG_INT_TWO;
            }else if(str.startsWith("#")){//hex
                str=str.substring(1);
                base=Constants.BIG_INT_SIXTEEN;
            }else if(str.startsWith("§")){//dez
                str=str.substring(1);
                base=BigInteger.TEN;
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
            if(safeMode){
                str = removeIllegalCharacters(base, str, true);
            }
            return Real.bigIntFromString(str, base);
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
