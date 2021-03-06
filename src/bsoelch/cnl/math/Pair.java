package bsoelch.cnl.math;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class Pair extends Tuple{
    public final MathObject a,b;

    public Pair(MathObject a, MathObject b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public NumericValue numericValue() {
        return a.numericValue();
    }

    @Override
    public FiniteSet domain() {
        TreeSet<Real.Int> keys=new TreeSet<>();
        if(!a.equals(Real.Int.ZERO))
            keys.add(Real.Int.ZERO);
        if(!b.equals(Real.Int.ZERO))
            keys.add(Real.Int.ONE);
        return FiniteSet.from(keys);
    }



    @Override
    public FiniteSet values() {
        return FiniteSet.from(a,b);
    }


    @Override
    public MathObject evaluateAt(MathObject a) {
        if(a.equals(Real.Int.ZERO))
            return this.a;
        if(a.equals(Real.Int.ONE))
            return this.b;
        return Real.Int.ZERO;
    }

    @Override
    public int size() {
        return (a.equals(Real.Int.ZERO)?0:1)+(b.equals(Real.Int.ZERO)?0:1);
    }

    @Override
    public int length() {
        return 2;
    }

    @Override
    public MathObject get(int i) {
        if(i==0)
            return this.a;
        if(i==1)
            return this.b;
        return Real.Int.ZERO;
    }
    @Override
    public MathObject insert(MathObject value) {
        MathObject[] newValues= new MathObject[3];
        newValues[0]=a;
        newValues[1]=b;
        newValues[2]=value;
        return Tuple.create(newValues);
    }
    @Override
    public FiniteMap put(MathObject key, MathObject value) {
        TreeMap<MathObject,MathObject> map=new TreeMap<>(MathObject::compare);
        map.put(Real.Int.ZERO,a);
        map.put(Real.Int.ONE,b);
        map.put(key,value);
        return FiniteMap.from(map, FiniteMap.TUPLE_WRAP_ZERO_TERMINATED);
    }
    @Override
    public Tuple insert(MathObject value, int index) {
        if(index<0)
            throw new ArithmeticException("Negative index in Tuple");
        TreeMap<MathObject,MathObject> map=new TreeMap<>(MathObject::compare);
        map.put(Real.from(index),value);
        map.put(index>0?Real.Int.ZERO:Real.Int.ONE,a);
        map.put(index>1?Real.Int.ONE:Real.Int.TWO,b);
        return FiniteMap.createTuple(map,Math.max(3,index+1));
    }
    @Override
    public FiniteMap remove(int index) {
        if(index<0)
            throw new ArithmeticException("Negative index in Tuple");
        TreeMap<MathObject,MathObject> map=new TreeMap<>(MathObject::compare);
        map.put(Real.Int.ZERO,a);
        map.put(Real.Int.ONE,b);
        map.remove(Real.from(index));
        return FiniteMap.createTuple(map,map.size());
    }
    @Override
    public FiniteMap headMap(MathObject last, boolean include) {
        int c = MathObject.compare(last, Real.Int.ONE);
        if(c>0||(include&&c==0)){
            return this;
        }else {
            c = MathObject.compare(last, Real.Int.ZERO);
            if (c > 0 || (include && c == 0)) {
                return Tuple.create(new MathObject[]{a});
            }
            return Tuple.EMPTY_MAP;
        }
    }
    @Override
    public FiniteMap tailMap(MathObject first, boolean include) {
        int c = MathObject.compare(first, Real.Int.ZERO);
        if(c<0||(include&&c==0)){
            return this;
        }else {
            c = MathObject.compare(first, Real.Int.ONE);
            if (c < 0 || (include && c == 0)) {
                return FiniteMap.from(Collections.singletonMap(Real.Int.ONE, b), TUPLE_WRAP_NONE);
            }
            return Tuple.EMPTY_MAP;
        }
    }
    @Override
    public FiniteMap range(MathObject first, boolean includeFirst, MathObject last, boolean includeLast) {
        int c = MathObject.compare(first, Real.Int.ZERO);
        if(c<0||(includeFirst&&c==0)){
            c = MathObject.compare(last, Real.Int.ONE);
            if(c>0||(includeLast&&c==0)){
                return this;
            }else {
                c = MathObject.compare(last, Real.Int.ZERO);
                if (c > 0 || (includeLast && c == 0)) {
                    return Tuple.create(new MathObject[]{a});
                }
            }
        }else {
            c = MathObject.compare(first, Real.Int.ONE);
            if (c < 0 || (includeFirst && c == 0)) {
                c = MathObject.compare(last, Real.Int.ONE);
                if (c > 0 || (includeLast && c == 0)) {
                    return FiniteMap.from(Collections.singletonMap(Real.Int.ONE, b), TUPLE_WRAP_NONE);
                }
            }
        }
        return Tuple.EMPTY_MAP;
    }
    @Override
    public FiniteMap remove(MathObject value) {
        if(a.equals(value)) {
            if(b.equals(value)){
                return Tuple.EMPTY_MAP;
            }else{
                return FiniteMap.from(Collections.singletonMap(Real.Int.ONE,b), TUPLE_WRAP_NONE);
            }
        }else{
            if(b.equals(value)){
                return Tuple.create(new MathObject[]{a});
            }else{
                return this;
            }
        }
    }
    @Override
    public Tuple tupleRemove(MathObject value) {
        if(a.equals(value)) {
            if(b.equals(value)){
                return Tuple.EMPTY_MAP;
            }else{
                return Tuple.create(new MathObject[]{b});
            }
        }else{
            if(b.equals(value)){
                return Tuple.create(new MathObject[]{a});
            }else{
                return this;
            }
        }
    }
    @Override
    public FiniteMap removeKey(MathObject key) {
        if(Real.Int.ZERO.equals(key)) {
            return FiniteMap.from(Collections.singletonMap(Real.Int.ONE,b), TUPLE_WRAP_NONE);
        }else if(Real.Int.ONE.equals(key)){
            return Tuple.create(new MathObject[]{a});
        }else{
            return this;
        }
    }
    @Override
    public FiniteMap removeIf(BinaryOperator<MathObject> condition) {
        if(MathObject.isTrue(condition.apply(Real.Int.ZERO,a))) {
            if(MathObject.isTrue(condition.apply(Real.Int.ONE,b))){
                return Tuple.EMPTY_MAP;
            }else{
                return FiniteMap.from(Collections.singletonMap(Real.Int.ONE,b), TUPLE_WRAP_NONE);
            }
        }else{
            if(MathObject.isTrue(condition.apply(Real.Int.ONE,b))){
                return Tuple.create(new MathObject[]{a});
            }else{
                return this;
            }
        }
    }

    @Override
    public MathObject[] toArray() {
        return new MathObject[]{a,b};
    }
    @Override
    @SuppressWarnings("unchecked")
    public <T extends MathObject> T[] toArray(Class<T[]> cls) {
        T[] ret= (T[])Array.newInstance(cls.getComponentType(),2);
        ret[0]=(T)a;
        ret[1]=(T)b;
        return ret;
    }

    @Override
    public FiniteMap replace(Function<MathObject, MathObject> f) {
        return new Pair(f.apply(a),f.apply(b));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof Matrix)
            o=((Matrix) o).asMap();
        if (!(o instanceof FiniteMap)) return false;
        if(o instanceof Pair){
            Pair pair = (Pair) o;
            return Objects.equals(a, pair.a) && Objects.equals(b, pair.b);
        }else if(o instanceof Tuple&&((Tuple) o).length()==2){
            return Objects.equals(a, ((Tuple) o).get(0)) && Objects.equals(b,((Tuple) o).get(1));
        }else {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return (a.equals(Real.Int.ZERO)?0:Objects.hash(Real.Int.ZERO,a))+
                (b.equals(Real.Int.ZERO)?0:Objects.hash(Real.Int.ONE,b));
    }

    @Override
    public String toString(BigInteger base, boolean useSmallBase) {
        return "["+a.toString(base, useSmallBase)+", "+b.toString(base, useSmallBase)+"]";
    }
    @Override
    public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
        return "["+a.toStringFixedPoint(base,precision, useSmallBase)+", "+
                b.toStringFixedPoint(base,precision, useSmallBase)+"]";
    }
    @Override
    public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
        return "["+a.toStringFloat(base,precision, useSmallBase)+", "+
                b.toStringFloat(base,precision, useSmallBase)+"]";
    }
    @Override
    public String intsAsString() {
        return "["+a.intsAsString()+", "+b.intsAsString()+"]";
    }
}
