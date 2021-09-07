package bsoelch.cnl.math;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class FiniteMap extends MathObject {
    FiniteMap(){}//package private constructor
    public static FiniteMap from(Map<? extends MathObject,? extends MathObject> mapData){
        //copy data to TreeMap
        TreeMap<MathObject,MathObject> map=new TreeMap<>(MathObject::compare);
        map.putAll(mapData);
        //test map for Tuple structure
        boolean isTuple=true;
        BigInteger length=null;
        for(MathObject k:map.keySet()){
            if(k instanceof Real.Int&&((Real.Int) k).num().signum()>=0){
                length=length==null?((Real.Int) k).num():length.max(((Real.Int) k).num());
            }else{
                isTuple=false;
                break;
            }
        }
        //remove zero-entries as all nonexistent entries are mapped to 0 by default
        map.entrySet().removeIf(e->e.getValue().equals(Real.Int.ZERO));
        if(map.isEmpty()){
            return Tuple.EMPTY_MAP;
        }else{
            if(isTuple){//tuple detection
                return createTuple(map, length==null?BigInteger.ZERO:length.add(BigInteger.ONE));
            }else {
                return new FiniteMapImpl(map);
            }
        }
    }

    /**creates a Tuple of the given length from the supplied map<br>
     * !!! this method does not preform any checks on the data in the map!!! */
    static Tuple createTuple(Map<? extends MathObject, MathObject> map, BigInteger length) {
        if(length.compareTo(BigInteger.valueOf(Tuple.SPARSE_FACTOR* map.size()))<0){
            MathObject[] tupleData=new MathObject[length.intValueExact()];
            for(int i = 0; i< tupleData.length; i++){
                tupleData[i]= map.get(Real.from(i));
                if(tupleData[i]==null)
                    tupleData[i]=Real.Int.ZERO;
            }
            return Tuple.create(tupleData);
        }else{//sparse Tuples
            return new Tuple.SparseTuple(new FiniteMapImpl(map),length);
        }
    }

    public static FiniteMap forEach(FiniteMap m1, FiniteMap m2, BiFunction<MathObject, MathObject, MathObject> f) {
        TreeMap<MathObject,MathObject> entries=new TreeMap<>(MathObject::compare);
        for (Iterator<Pair> it = m1.mapIterator(); it.hasNext(); ) {
            Pair p = it.next();
            if(m2.isKey(p.a)){
                entries.put(p.a,f.apply(p.b,m2.evaluateAt(p.a)));
            }else{
                entries.put(p.a,f.apply(p.b,Real.Int.ZERO));
            }
        }
        for (Iterator<Pair> it = m2.mapIterator(); it.hasNext(); ) {
            Pair p = it.next();
            if(!m1.isKey(p.a)){
                entries.put(p.a,f.apply(Real.Int.ZERO,p.b));
            }
        }
        return from(entries);
    }

    public static FiniteMap indicatorMap(FiniteSet s) {
        return constantMap(s,Real.Int.ONE);
    }
    public static FiniteMap constantMap(FiniteSet keys, MathObject value) {
        return value.equals(Real.Int.ZERO)?Tuple.EMPTY_MAP:new FiniteMap() {
            @Override
            public FiniteMap forEach(Function<MathObject, MathObject> f) {
                return constantMap(keys,f.apply(value));
            }

            @Override
            public int size() {
                return keys.size();
            }

            @Override
            public FiniteSet domain() {
                return keys;
            }
            @Override
            public FiniteSet values() {
                return FiniteSet.from(value);
            }
            @Override
            public MathObject evaluateAt(MathObject a) {
                if(keys.contains(a))
                    return value;
                else
                    return Real.Int.ZERO;
            }
            @Override
            public NumericValue numericValue() {
                return value.numericValue();
            }

            @Override
            public boolean isTuple() {
                for(MathObject o:keys){
                    if(!(o instanceof Real.Int&&((Real.Int) o).num().signum()>=0))
                        return false;
                }
                return true;
            }
            @Override
            public boolean isMatrix() {
                return isTuple()&&value instanceof FiniteMap&&((FiniteMap) value).isNumericTuple();
            }
            @Override
            public boolean isNumericTuple() {
                return isTuple()&&value instanceof NumericValue;
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
                for(MathObject o:keys){
                    if(sb.length()>1)
                        sb.append(", ");
                    sb.append(objectToString.apply(o)).append(" -> ").append(objectToString.apply(o));
                }
                return sb.append('}').toString();
            }

            @Override
            public int hashCode() {
                int hash=0;
                for (MathObject k:keys) {
                    hash+=Objects.hash(k,value);
                }
                return hash;
            }
        };
    }

    @Override
    public String asString() {
        StringBuilder sb=new StringBuilder();
        for (Iterator<MathObject> it = valueIterator(); it.hasNext(); ) {
            MathObject e = it.next();
            sb.append(e.asString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(o==this)
            return true;
        if(o instanceof Matrix)
            o=((Matrix) o).asMap();
        if(o instanceof FiniteMap){
            Iterator<Pair> itr1= mapIterator();
            Iterator<Pair> itr2= ((FiniteMap) o).mapIterator();
            while(itr1.hasNext()&&itr2.hasNext()){
                if(!itr1.next().equals(itr2.next()))
                    return false;
            }
            return !(itr1.hasNext()||itr2.hasNext());
        }else{
            return false;
        }
    }

    /**Number of (non-zero) values in this map*/
    public abstract int size();

    public abstract FiniteMap forEach(Function<MathObject, MathObject> f);

    public abstract FiniteSet domain();

    public abstract FiniteSet values();

    public FiniteSet asSet() {
        TreeSet<MathObject> pairs=new TreeSet<>(MathObject::compare);
        for (Iterator<Pair> it = mapIterator(); it.hasNext(); ) {
            Pair p = it.next();
            pairs.add(p);
        }
        return FiniteSet.from(pairs);
    }

    private Iterator<Pair> wrapItr(Iterator<MathObject> keyItr){
        return new Iterator<Pair>() {
            Pair nextEntry=nextEntry();
            private Pair nextEntry() {
                nextEntry=null;
                while (nextEntry==null&&keyItr.hasNext()) {
                    MathObject next = keyItr.next();
                    Pair p = new Pair(next, evaluateAt(next));
                    if(!p.b.equals(Real.Int.ZERO)){
                        nextEntry=p;
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
    /**Iterator over the key-value pairs (with non-zero value) in this map*/
    public Iterator<Pair> mapIterator(){
        return wrapItr(domain().iterator());
    }
    public Iterator<Pair> tailIterator(MathObject slice, boolean inclusive) {
        return wrapItr(domain().tailIterator(slice,inclusive));
    }
    public Iterator<Pair> headIterator(MathObject slice, boolean inclusive) {
        return wrapItr(domain().headIterator(slice,inclusive));
    }
    /**Iterator over the non-zero values in this map*/
    public Iterator<MathObject> valueIterator(){
        return new Iterator<MathObject>() {
            final Iterator<Pair> mapItr=mapIterator();
            @Override
            public boolean hasNext() {
                return mapItr.hasNext();
            }
            @Override
            public MathObject next() {
                return mapItr.next().b;
            }
        };
    }

    @Override
    public String intsAsString(){
        StringBuilder sb=new StringBuilder("{");
        for (Iterator<Pair> it = mapIterator(); it.hasNext(); ) {
            Pair e = it.next();
            if(sb.length()>1)
                sb.append(", ");
            sb.append(e.a.intsAsString()).append("->")
                    .append(e.b.intsAsString());
        }
        return sb.append('}').toString();
    }

    public boolean isKey(MathObject key) {
        return domain().contains(key);
    }

    /**true is this Map is a (sparse) Matrix
     * i.e all Keys are non-negative integers and all
     * Values are mapping non-negative Integers to NumericValues*/
    public abstract boolean isMatrix();
    /**true is this Map is a (sparse) Tuple and all Values are NumericValues*/
    public abstract boolean isNumericTuple();

    /**true is this Map is a (sparse) Tuple
     * i.e all Keys are non-negative integers*/
    public abstract boolean isTuple();

    public abstract MathObject evaluateAt(MathObject a);

}
