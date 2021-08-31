package bsoelch.cnl.math;

import bsoelch.cnl.Constants;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**Root of all MathObjects*/
public interface MathObject {
    int FLOOR = -1;
    int ROUND = 0;
    int CIEL = 1;

    NumericValue numericValue();

    String toString(BigInteger base, boolean useSmallBase);
    String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase);
    String toStringFloat(BigInteger base, Real precision, boolean useSmallBase);

    /**String representing this Object*/
    default String asString(){//TODO? better implementation (concat partial strings)
        return numericValue().asString();
    }
    String intsAsString();


    static MathObject not(MathObject o) {//TODO? elementwise not
        return isTrue(o)?Real.Int.ZERO:Real.Int.ONE;
    }

    /**@return the boolean value of the supplied MathObject,
     * the only objects resulting in false are 0, empty sets and empty maps/tuples */
    static boolean isTrue(MathObject arg) {
        return !(arg.equals(Real.Int.ZERO)||arg.equals(FiniteSet.EMPTY_SET)
                ||arg.equals(FiniteMap.EMPTY_MAP));
    }

    static FiniteSet asSet(MathObject o){
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
    static FiniteMap asMap(MathObject o){
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
    //TODO convert ... to Matrix

    static MathObject elementWise(MathObject l, MathObject r,
                                  BinaryOperator<NumericValue> scalarOperation){
        if(l instanceof NumericValue){
            if(r instanceof NumericValue){
                return scalarOperation.apply((NumericValue) l,((NumericValue)r));
            }else if(r instanceof Matrix){
                return ((Matrix) r).applyToAll(e->scalarOperation.apply((NumericValue)l,e));
            }else if(r instanceof FiniteSet){
                return FiniteSet.forEach((FiniteSet) r, o->elementWise(l,o,scalarOperation));
            }else if(r instanceof FiniteMap){
                return ((FiniteMap) r).forEach(o->elementWise(l,o,scalarOperation));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof Matrix){
            if(r instanceof NumericValue){
                return ((Matrix) l).applyToAll(e->scalarOperation.apply(e,(NumericValue)r));
            }else if(r instanceof Matrix){
                return ((Matrix) l).forEach((Matrix) r,scalarOperation);
            }else if(r instanceof FiniteSet){
                return FiniteSet.forEach((FiniteSet) r, o->elementWise(l,o,scalarOperation));
            }else if(r instanceof FiniteMap){
                return ((FiniteMap) r).forEach(o->elementWise(l,o,scalarOperation));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteSet){
            if(r instanceof NumericValue||r instanceof Matrix){
                return FiniteSet.forEach((FiniteSet) l, o->elementWise(o,r,scalarOperation));
            }else if(r instanceof FiniteSet){
                return FiniteSet.forEachPair((FiniteSet) l,(FiniteSet) r, (a,b)->elementWise(a,b,scalarOperation));
            }else if(r instanceof FiniteMap){
                return FiniteMap.forEach(FiniteMap.indicatorMap((FiniteSet) l),(FiniteMap) r, (a,b)->elementWise(a,b,scalarOperation));
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteMap){
            if(r instanceof NumericValue||r instanceof Matrix){
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
    static MathObject elementWise(MathObject o, Function<NumericValue, NumericValue> scalarFunction){
        if(o instanceof NumericValue){
            return scalarFunction.apply((NumericValue) o);
        }else if(o instanceof Matrix){
            return ((Matrix) o).applyToAll(scalarFunction);
        }else if(o instanceof FiniteSet){
            return FiniteSet.forEach((FiniteSet) o, e->elementWise(e,scalarFunction));
        }else if(o instanceof FiniteMap){
            return ((FiniteMap) o).forEach(e->elementWise(e,scalarFunction));
        }else{
            throw new IllegalArgumentException("Unexpected MathObject:"+o.getClass());
        }
    }
    static MathObject nAryReduce(MathObject[] objects, MathObject nilaryValue, BinaryOperator<MathObject> reduce){
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
    static NumericValue deepNAryReduce(MathObject[] objects, NumericValue nilaryValue, BinaryOperator<NumericValue> reduce){
        if(objects.length==0) {
            return nilaryValue;
        }else if(objects.length==1) {
            if(objects[0] instanceof NumericValue) {
                return (NumericValue)objects[0];
            }else if(objects[0] instanceof Matrix){
                NumericValue tmp=null;
                for(NumericValue o:((Matrix) objects[0])){
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

    static MathObject add(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::add);
    }
    static MathObject subtract(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::subtract);
    }
    static MathObject negate(MathObject o) {
        return elementWise(o, NumericValue::negate);
    }
    static MathObject realPart(MathObject o) {
        return elementWise(o, NumericValue::realPart);
    }
    static MathObject imaginaryPart(MathObject o) {
        return elementWise(o, NumericValue::imaginaryPart);
    }
    static MathObject conjugate(MathObject o) {
        return elementWise(o, NumericValue::conjugate);
    }

    static MathObject multiply(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::multiply);
    }

    static Real sqAbs(MathObject o) {
        if(o instanceof NumericValue){
            return ((NumericValue) o).sqAbs();
        }else if(o instanceof Matrix){
            Real sqAbs=Real.Int.ZERO;
            for(NumericValue e:(Matrix)o){
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
        return elementWise(o, NumericValue::invert);
    }

    static MathObject divide(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::divide);
    }

    static MathObject mod(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::mod);
    }
    static MathObject pow(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::pow);
    }
    static MathObject round(MathObject o, int mode) {
        return elementWise(o,e->e.round(mode));
    }
    static MathObject approximate(MathObject o, Real precision) {
        return elementWise(o,e->e.approx(precision));
    }

    static MathObject floorAnd(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::floorAnd);
    }
    static MathObject floorOr(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::floorOr);
    }
    static MathObject floorXor(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::floorXor);
    }
    static MathObject floorAndNot(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::floorAndNot);
    }

    /**@param l Left operand (maps will be converted to their set representation)
     * @param r right operand (maps will be converted to their set representation)
     * @param setOp operation that is aplied to sets
     * @param atomicOperation operation that is applied to Matrices and NumericValues
     * @param rewrapMaps if this value if true the program tries
     *                    to convert the result to a map if one of the arguments was a map
     * */
    static MathObject setOperation(MathObject l, MathObject r,
                                   BinaryOperator<FiniteSet> setOp,
                                   BinaryOperator<MathObject> atomicOperation, boolean rewrapMaps){
        if(l instanceof NumericValue||l instanceof Matrix){
            if(r instanceof NumericValue||r instanceof Matrix){
                return atomicOperation.apply(l,r);
            }else if(r instanceof FiniteSet){
                return setOp.apply(FiniteSet.from(l),(FiniteSet) r);
            }else if(r instanceof FiniteMap){
                FiniteSet set = setOp.apply(FiniteSet.from(l), ((FiniteMap) r).asSet());
                return rewrapMaps?set.asMapIfPossible():set;
            }else{
                throw new IllegalArgumentException("Unexpected MathObject:"+r.getClass());
            }
        }else if(l instanceof FiniteSet){
            if(r instanceof NumericValue||r instanceof Matrix){
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
            if(r instanceof NumericValue||r instanceof Matrix){
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

    static MathObject times(MathObject l, MathObject r) {
        return setOperation(l,r,FiniteSet::product,Pair::new, false);
    }
    /**calculates the n-ary cartesian product of the objects in the given set
     * @param objects operands of the n-ary product, maps are converted to sets,
     *                Matrices and NumericValues are wrapped in sets */
    static MathObject nAryTimes(MathObject[] objects){
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
    static MathObject fAdd(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::fAdd);
    }
    static MathObject strConcat(MathObject l, MathObject r) {
        return elementWise(l,r, NumericValue::strConcat);
    }

    static MathObject min(MathObject l, MathObject r) {
        return compare(l,r)<0?l:r;
    }
    static MathObject max(MathObject l, MathObject r) {
        return compare(l,r)>0?l:r;
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
        if(a instanceof NumericValue){
            if(b instanceof NumericValue){
                return ((NumericValue) a).compareTo((NumericValue) b);
            }else if(b instanceof Matrix){
                int c;
                for(NumericValue e:(Matrix) b){
                    c=((NumericValue) a).compareTo(e);
                    if(c!=0)
                        return c;
                }
                return -1;// a < {a}
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
            if(b instanceof NumericValue||b instanceof Matrix){
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
            if(b instanceof NumericValue||b instanceof Matrix){
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




    class FromString{
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
                    case MODE_TUPLE:return FiniteMap.EMPTY_MAP;
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
                        }else if(mode==MODE_TUPLE) {
                            returnValue=Tuple.create(new MathObject[]{returnValue});
                        }
                    }
                    if(isMap){
                        if(returnValue instanceof FiniteSet){
                            MathObject map= ((FiniteSet) returnValue).asMapIfPossible();
                            if(safeMode||map instanceof FiniteMap) {
                                return map;
                            }else{
                                throw new IllegalArgumentException("Unable to resolve Map:"+part);
                            }
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


        private static NumericValue numberFromString(BigInteger base, String str, boolean safeMode) {
            //dynamic base: $-> bin #->hex §->doz @base: ->BaseN //TODO? dec prefix
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
            if(safeMode){
                str = removeIllegalCharacters(base, str, true);
            }else{
                str=str.trim();
                if(str.isEmpty())
                    throw new IllegalArgumentException("empty input String in numberFromString");
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
