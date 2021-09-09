package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public abstract class Tuple extends FiniteMap implements Iterable<MathObject>{
    /**value of length/size above which SparseTuple are used*/
    static final long SPARSE_FACTOR=3;

    Tuple(){}//package private constructor
    public static Tuple EMPTY_MAP=new Tuple() {
        @Override
        public int size() {
            return 0;
        }
        @Override
        public int length() {
            return 0;
        }

        @Override
        public MathObject[] toArray() {
            return new MathObject[0];
        }
        @Override
        @SuppressWarnings("unchecked")
        public <T extends MathObject> T[] toArray(Class<T[]> cls) {
            return (T[])new MathObject[0];
        }

        @Override
        public MathObject get(int i) {
            return Real.Int.ZERO;
        }

        @Override
        public FiniteMap replace(Function<MathObject, MathObject> f) {
            return this;
        }
        @Override
        public FiniteSet domain() {
            return FiniteSet.EMPTY_SET;
        }
        @Override
        public FiniteSet values() {
            return FiniteSet.EMPTY_SET;
        }
        @Override
        public MathObject evaluateAt(MathObject a) {
            return Real.Int.ZERO;
        }

        @Override
        public MathObject insert(MathObject value) {
            return Tuple.create(new MathObject[]{value});
        }
        @Override
        public FiniteMap put(MathObject key, MathObject value) {
            return FiniteMap.from(Collections.singletonMap(key, value), FiniteMap.TUPLE_WRAP_ZERO_TERMINATED);
        }
        @Override
        public Tuple insert(MathObject value, int index) {
            if(index<0)
                throw new ArithmeticException("Negative index in Tuple");
            return FiniteMap.createTuple(Collections.singletonMap(Real.from(index),value),index+1);
        }
        @Override
        public FiniteMap remove(int index) {
            return this;
        }

        @Override
        public FiniteMap headMap(MathObject last, boolean include) {
            return this;
        }
        @Override
        public FiniteMap tailMap(MathObject first, boolean include) {
            return this;
        }
        @Override
        public FiniteMap range(MathObject first, boolean includeFirst, MathObject last, boolean includeLast) {
            return this;
        }
        @Override
        public FiniteMap remove(MathObject value) {
            return this;
        }
        @Override
        public Tuple tupleRemove(MathObject value) {
            return this;
        }
        @Override
        public FiniteMap removeKey(MathObject key) {
            return this;
        }
        @Override
        public FiniteMap removeIf(BinaryOperator<MathObject> condition) {
            return this;
        }
        @Override
        public Tuple nonzeroElements() {
            return this;
        }

        @Override
        public NumericValue numericValue() {
            return Real.Int.ZERO;
        }
        @Override
        public FiniteSet asSet() {
            return FiniteSet.EMPTY_SET;
        }
        @Override
        public boolean equals(Object obj) {
            return obj instanceof FiniteMap&&((FiniteMap) obj).size()==0;
        }
        @Override
        public int hashCode() {
            return 0;
        }
        @Override
        public String toString() {
            return "[]";
        }
        @Override
        public String toString(BigInteger base, boolean useSmallBase) {
            return toString();
        }
        @Override
        public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
            return toString();
        }
        @Override
        public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
            return toString();
        }
    };

    public static Tuple create(MathObject[] objects){
        int zeros=0;
        for(int i=0;i<objects.length;i++) {
            if (objects[i] == null){
                objects[i]=Real.Int.ZERO;
                zeros++;
            }else if (objects[i].equals(Real.Int.ZERO)){
                zeros++;
            }
        }
        if(objects.length>3&&SPARSE_FACTOR*(objects.length-zeros)<objects.length){
            TreeMap<Real.Int,MathObject> map=new TreeMap<>();
            for(int i=0;i<objects.length;i++) {
                if (!objects[i].equals(Real.Int.ZERO)){
                    map.put(Real.from(i),objects[i]);
                }
            }
            return FiniteMap.createTuple(map,objects.length);
        }else {
            if (objects.length == 0) {
                return EMPTY_MAP;
            } else if (objects.length == 2) {
                return new Pair(objects[0], objects[1]);
            } else {
                return new NTuple(objects);
            }
        }
    }

    public abstract int size();
    public abstract int length();
    public abstract MathObject get(int i);
    public abstract MathObject[] toArray();
    public abstract <T extends MathObject> T[] toArray(Class<T[]> cls);

    @Override
    public MathObject insert(MathObject value) {
        return MathObject.tupleConcat(this,create(new MathObject[]{value}));
    }
    public abstract Tuple insert(MathObject value, int index);
    public abstract FiniteMap remove(int index);
    public abstract Tuple tupleRemove(MathObject value);
    @Override
    public Tuple nonzeroElements() {
        return tupleRemove(Real.Int.ZERO);
    }
    //addLater? tupleRemoveIf

    @Override
    public FiniteMap removeFirst() {
        return remove(0);
    }
    @Override
    public FiniteMap removeLast() {
        return remove(length()-1);
    }

    /**mapIterator of this Tuple, iterates over all non-zero elements,
     * the last element is included in the iteration even if it is zero*/
    @Override
    public Iterator<Pair> mapIterator() {
        return new Iterator<Pair>() {
            int i= 0;
            Pair nextEntry=nextEntry();

            private Pair nextEntry() {
                nextEntry=null;
                while (nextEntry==null&&i<length()) {
                    MathObject t=get(i++);
                    if(i==length()-1||!t.equals(Real.Int.ZERO)){
                        nextEntry=new Pair(Real.from(i),t);
                        break;
                    }
                }
                return nextEntry;
            }
            @Override
            public boolean hasNext() {
                return nextEntry!=null;
            }
            @Override
            public Pair next() {
                Pair ret=nextEntry;
                nextEntry=nextEntry();
                return ret;
            }
        };
    }

    @Override
    public @NotNull Iterator<MathObject> iterator() {
        return new Iterator<MathObject>() {
            int i=0;
            @Override
            public boolean hasNext() {
                return i<length();
            }
            @Override
            public MathObject next() {
                return get(i++);
            }
        };
    }
    public @NotNull Iterator<MathObject> sparseIterator() {
        return new Iterator<MathObject>() {
            final Iterator<MathObject> itr=iterator();
            MathObject next=nextNonZero();

            private MathObject nextNonZero() {
                next=Real.Int.ZERO;
                while(itr.hasNext()&&next.equals(Real.Int.ZERO)){
                    next=itr.next();
                }
                return next;
            }
            @Override
            public boolean hasNext() {
                return !next.equals(Real.Int.ZERO);
            }
            @Override
            public MathObject next() {
                MathObject tmp=next;
                next=nextNonZero();
                return tmp;
            }
        };
    }

    @Override
    public boolean isMatrix() {
        for (@NotNull Iterator<MathObject> it = sparseIterator(); it.hasNext(); ) {
            MathObject o = it.next();
            if(!(o instanceof FiniteMap&&((FiniteMap) o).isNumericTuple())){
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean isNumericTuple() {
        for (Iterator<MathObject> it = this.sparseIterator(); it.hasNext(); ) {
            MathObject o = it.next();
            if(!(o instanceof NumericValue)){
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean isTuple() {
        return true;
    }

    @Override
    public Tuple asTuple() {
        return this;
    }

    public boolean isFullTuple(){
        return true;
    }

    @Override
    public MathObject firstKey() {
        return Real.Int.ZERO;
    }
    @Override
    public MathObject firstValue() {
        return get(0);
    }
    @Override
    public MathObject lastKey() {
        return Real.from(length()-1);
    }
    @Override
    public MathObject lastValue() {
        return get(length()-1);
    }

    /**wrapper class that wraps a FiniteMap (which is assumed to be a SparseTuple) in the Tuple interface*/
    public static final class SparseTuple extends Tuple{
        final FiniteMap map;
        final int length;

        /**creates a sparse tuple of the supplied length, wrapping the given FiniteMap
         * this constructor should not be directly called, use {@link FiniteMap#createTuple(Map, int)} instead*/
        SparseTuple(FiniteMap map, int length){
            if(!map.isTuple())
                throw new IllegalArgumentException("Contents of SparseTuple have to represent a tuple");
            this.map=map;
            this.length =length;
        }

        @Override
        public boolean isFullTuple() {
            return false;
        }

        @Override
        public FiniteMap replace(Function<MathObject, MathObject> f) {
            return new SparseTuple(map.replace(f),length);
        }

        @Override
        public FiniteSet domain() {
            return map.domain();
        }

        @Override
        public FiniteSet values() {
            return FiniteSet.unite(map.values(),FiniteSet.from(Real.Int.ZERO));
        }

        @Override
        public MathObject evaluateAt(MathObject a) {
            return map.evaluateAt(a);
        }

        @Override
        public FiniteMap put(MathObject key, MathObject value) {
            FiniteMap newMap=map.put(key, value);
            if(newMap instanceof Tuple){
                if(((Tuple) newMap).length()<length){
                    //ensure length
                    return newMap.put(Real.from(length-1),Real.Int.ZERO);
                }else{
                    return newMap;
                }
            }else if(newMap.isTuple()){
                return new SparseTuple(newMap,key instanceof Real.Int?Math.max(length,((Real.Int)key).num().intValueExact()+1):length);
            }else{
                return newMap;
            }
        }

        @Override
        public NumericValue numericValue() {
            return evaluateAt(Real.Int.ZERO).numericValue();
        }

        private Iterator<Pair> withTailElement(Iterator<Pair> mapItr){
            return new Iterator<Pair>() {
                final Real.Int last=Real.from(length-1);
                boolean hasLast=true;
                @Override
                public boolean hasNext() {
                    return mapItr.hasNext()||hasLast;
                }

                @Override
                public Pair next() {
                    if(mapItr.hasNext()){
                        Pair p=mapItr.next();
                        if(p.a.equals(last))
                            hasLast=false;
                        return p;
                    }else if(hasLast){
                        hasLast=false;
                        return new Pair(last,Real.Int.ZERO);
                    }else{
                        throw new NoSuchElementException();
                    }
                }
            };
        }
        @Override
        public Iterator<Pair> mapIterator() {
                return withTailElement(map.mapIterator());
        }

        @Override
        public @NotNull Iterator<MathObject> sparseIterator() {
            return map.valueIterator();
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public MathObject get(int i) {
            return evaluateAt(Real.from(i));
        }

        @Override
        public Tuple insert(MathObject value, int index) {
            if(index<0)
                throw new ArithmeticException("Negative index in Tuple");
            TreeMap<MathObject,MathObject> newMap=new TreeMap<>(MathObject::compare);
            MathObject realIndex=Real.from(index);
            newMap.put(realIndex,value);
            for (Iterator<Pair> it = map.mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                if(MathObject.compare(e.a,realIndex)<0){
                    newMap.put(e.a,e.b);
                }else{
                    newMap.put(MathObject.add(e.a,Real.Int.ONE),e.b);
                }
            }
            return FiniteMap.createTuple(newMap,Math.max(length+1,index+1));
        }
        @Override
        public FiniteMap remove(int index) {
            if(index<0)
                throw new ArithmeticException("Negative index in Tuple");
            if(index>=length)
                return this;
            TreeMap<MathObject,MathObject> newMap=new TreeMap<>(MathObject::compare);
            MathObject realIndex=Real.from(index);
            for (Iterator<Pair> it = map.mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                if (!e.a.equals(realIndex)) {
                    if (MathObject.compare(e.a, realIndex) < 0) {
                        newMap.put(e.a, e.b);
                    } else {
                        newMap.put(MathObject.subtract(e.a, Real.Int.ONE), e.b);
                    }
                }
            }
            return FiniteMap.createTuple(newMap,length-1);
        }
        @Override
        public FiniteMap headMap(MathObject last, boolean include) {
            return map.headMap(last, include);
        }
        @Override
        public FiniteMap tailMap(MathObject first, boolean include) {
            return map.tailMap(first, include);
        }
        @Override
        public FiniteMap range(MathObject first, boolean includeFirst, MathObject last, boolean includeLast) {
            return map.range(first, includeFirst, last, includeLast);
        }
        @Override
        public FiniteMap remove(MathObject value) {
            return map.remove(value);
        }
        @Override
        public Tuple tupleRemove(MathObject value) {
            if(value.equals(Real.Int.ZERO)){
                return map.nonzeroElements();
            }else {
                TreeMap<MathObject, MathObject> newMap = new TreeMap<>(MathObject::compare);
                Real.Int removed = Real.Int.ZERO;
                for (Iterator<Pair> it = map.mapIterator(); it.hasNext(); ) {
                    Pair e = it.next();
                    if (e.b.equals(value)) {
                        removed = (Real.Int) Real.add(removed, Real.Int.ONE);
                    } else {
                        newMap.put(MathObject.subtract(e.a, removed), e.b);
                    }
                }
                return FiniteMap.createTuple(newMap, length-removed.num().intValueExact());
            }
        }

        @Override
        public FiniteMap removeKey(MathObject key) {
            FiniteMap newMap=map.removeKey(key);
            if(!(newMap instanceof Tuple)){
                return new SparseTuple(newMap,length);
            }else if(((Tuple) newMap).length()<length){
                return ((Tuple) newMap).insert(Real.Int.ZERO,length-1);
            }
            return newMap;
        }
        @Override
        public FiniteMap removeIf(BinaryOperator<MathObject> condition) {
            FiniteMap newMap=map.removeIf(condition);
            if(!(newMap instanceof Tuple)){
                return new SparseTuple(newMap,length);
            }else if(((Tuple) newMap).length()<length){
                return ((Tuple) newMap).insert(Real.Int.ZERO,length-1);
            }
            return newMap;
        }

        @Override
        public MathObject[] toArray() {
            MathObject[] ret=new MathObject[length];
            fillArray(ret);
            return ret;
        }
        @Override
        public <T extends MathObject> T[] toArray(Class<T[]> cls) {
            @SuppressWarnings("unchecked")
            T[] ret= (T[]) Array.newInstance(cls.getComponentType(),length);
            fillArray(ret);
            return ret;
        }
        private void fillArray(MathObject[] ret) {
            for (Iterator<Pair> it = map.mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                ret[((Real.Int)e.a).num().intValueExact()]=e.b;
            }
        }

        @Override
        public String toString(BigInteger base, boolean useSmallBase) {
            return toString(o->o.toString(base,useSmallBase));
        }
        @Override
        public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
            return toString(o->o.toStringFixedPoint(base,precision,useSmallBase));
        }
        @Override
        public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
            return toString(o->o.toStringFloat(base,precision,useSmallBase));
        }

        private String toString(Function<MathObject,String> objectToString){
            StringBuilder sb=new StringBuilder("{");
            for (Iterator<Pair> it = mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                if(sb.length()>1)
                    sb.append(", ");
                sb.append(objectToString.apply(e.a));
                sb.append(" -> ");
                sb.append(objectToString.apply(e.b));
            }
            return sb.append('}').toString();
        }

        //handling of equals and hash in wrapped map
        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }
        @Override
        public int hashCode() {
            return map.hashCode();
        }
    }
}
