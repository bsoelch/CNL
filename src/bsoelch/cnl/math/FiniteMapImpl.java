package bsoelch.cnl.math;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class FiniteMapImpl extends FiniteMap {
    final TreeMap<MathObject, MathObject> map;

    /**constructor of FiniteMapImpl, this method should only be called from {@link FiniteMap#from(Map)}*/
    FiniteMapImpl(Map<? extends MathObject, MathObject> map) {
        this.map=new TreeMap<>(map);
    }



    @Override
    public int size() {
        return map.size();
    }

    @Override
    public FiniteSet domain() {
        return FiniteSet.from(map.keySet());
    }
    @Override
    public FiniteSet values() {
        return FiniteSet.from(new HashSet<>(map.values()));
    }

    @Override
    public MathObject firstKey() {
        return map.firstKey();
    }
    @Override
    public MathObject firstValue() {
        return map.firstEntry().getValue();
    }
    @Override
    public MathObject lastKey() {
        return map.lastKey();
    }
    @Override
    public MathObject lastValue() {
        return map.lastEntry().getValue();
    }
    @Override
    public FiniteMap removeFirst() {
        TreeMap<MathObject,MathObject> newMap=new TreeMap<>(MathObject::compare);
        newMap.putAll(map);
        newMap.remove(newMap.firstKey());
        return FiniteMap.from(newMap);
    }
    @Override
    public FiniteMap removeLast() {
        TreeMap<MathObject,MathObject> newMap=new TreeMap<>(MathObject::compare);
        newMap.putAll(map);
        newMap.remove(newMap.lastKey());
        return FiniteMap.from(newMap);
    }

    @Override
    public MathObject insert(MathObject value) {
        TreeMap<MathObject,MathObject> newMap=new TreeMap<>(MathObject::compare);
        NumericValue maxKey=null;
        for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
            if(maxKey==null){
                maxKey=e.getKey().numericValue();
            }else{
                maxKey=NumericValue.max(maxKey,e.getKey().numericValue());
            }
            newMap.put(e.getKey(),e.getValue());
        }
        newMap.put(NumericValue.add(maxKey,Real.Int.ONE),value);
        return FiniteMap.from(newMap);
    }
    @Override
    public MathObject put(MathObject key, MathObject value) {
        TreeMap<MathObject,MathObject> newMap=new TreeMap<>(MathObject::compare);
        newMap.putAll(map);
        newMap.put(key, value);
        return FiniteMap.from(newMap);
    }
    @Override
    public MathObject remove(MathObject value) {
        TreeMap<MathObject,MathObject> newMap=new TreeMap<>(MathObject::compare);
        newMap.putAll(map);
        newMap.values().remove(value);
        return FiniteMap.from(newMap);
    }
    @Override
    public MathObject removeKey(MathObject key) {
        TreeMap<MathObject,MathObject> newMap=new TreeMap<>(MathObject::compare);
        newMap.putAll(map);
        newMap.remove(key);
        return FiniteMap.from(newMap);
    }
    @Override
    public MathObject removeIf(BinaryOperator<MathObject> condition) {
        TreeMap<MathObject,MathObject> newMap=new TreeMap<>(MathObject::compare);
        newMap.putAll(map);
        newMap.entrySet().removeIf(e->MathObject.isTrue(condition.apply(e.getKey(),e.getValue())));
        return FiniteMap.from(newMap);
    }

    @Override
    public FiniteMap headMap(MathObject last, boolean include) {
        return FiniteMap.from(map.headMap(last, include));
    }
    @Override
    public FiniteMap tailMap(MathObject first, boolean include) {
        return FiniteMap.from(map.tailMap(first, include));
    }
    @Override
    public FiniteMap range(MathObject first, boolean includeFirst, MathObject last, boolean includeLast) {
        if(MathObject.compare(last,first)<0)
            return Tuple.EMPTY_MAP;
        return FiniteMap.from(map.subMap(first, includeFirst, last, includeLast));
    }

    private Iterator<Pair> wrapIterator(Iterator<Map.Entry<MathObject, MathObject>> mapItr){
        return new Iterator<Pair>() {
            Pair nextEntry=nextEntry();
            private Pair nextEntry() {
                nextEntry=null;
                while (nextEntry==null&&mapItr.hasNext()) {
                    Map.Entry<MathObject, MathObject> next = mapItr.next();
                    Pair p = new Pair(next.getKey(),next.getValue());
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
    @Override
    public Iterator<Pair> mapIterator() {
        return wrapIterator(map.entrySet().iterator());
    }
    @Override
    public Iterator<Pair> headIterator(MathObject slice,boolean inclusive) {
        return wrapIterator(map.headMap(slice,inclusive).entrySet().iterator());
    }
    @Override
    public Iterator<Pair> tailIterator(MathObject slice,boolean inclusive) {
        return wrapIterator(map.tailMap(slice,inclusive).entrySet().iterator());
    }

    @Override
    public NumericValue numericValue() {
        if(map.isEmpty())
            return Real.Int.ZERO;
        return map.firstEntry().getValue().numericValue();
    }

    @Override
    public boolean isTuple() {
        for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
            if(!(e.getKey() instanceof Real.Int&&((Real.Int) e.getKey()).num().signum()>=0))
                return false;
        }
        return true;
    }
    @Override
    public boolean isNumericTuple() {
        for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
            if(!(e.getKey() instanceof Real.Int&&((Real.Int) e.getKey()).num().signum()>=0))
                return false;
            if(!(e.getValue() instanceof NumericValue))
                return false;
        }
        return true;
    }
    @Override
    public boolean isMatrix() {
        for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
            if(!(e.getKey() instanceof Real.Int&&((Real.Int) e.getKey()).num().signum()>=0))
                return false;
            if(!(e.getValue() instanceof FiniteMap&&((FiniteMap) e.getValue()).isNumericTuple()))
                return false;
        }
        return true;
    }

    @Override
    public Tuple asTuple() {
        TreeMap<Real.Int,MathObject> tuple=new TreeMap<>();
        BigInteger maxElement=BigInteger.ZERO;
        int offSet=0;
        Real.Int key;
        for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
            key=e.getKey().numericValue().realPart().round(FLOOR);
            if(key.compareTo(Real.Int.ZERO)<0){
                key=Real.from(offSet++);
            }else{
                key=(Real.Int)Real.add(key,Real.from(offSet));
                if(tuple.containsKey(key)){
                    offSet++;
                    key=(Real.Int) Real.add(key,Real.Int.ONE);
                }
            }
            tuple.put(key,e.getValue());
            maxElement=key.num();
        }
        return FiniteMap.createTuple(tuple,maxElement.add(BigInteger.ONE));
    }
    @Override
    public Tuple nonzeroElements() {
        TreeMap<MathObject,MathObject> elements=new TreeMap<>(MathObject::compare);
        elements.putAll(map);
        elements.entrySet().removeIf(e->e.getKey().equals(Real.Int.ZERO));
        return Tuple.create(elements.values().toArray(new MathObject[0]));
    }

    @Override
    public MathObject evaluateAt(MathObject a) {
        MathObject o=map.get(a);
        return o==null?Real.Int.ZERO:o;
    }

    @Override
    public FiniteMap replace(Function<MathObject, MathObject> f) {
        HashMap<MathObject, MathObject> newElements=new HashMap<>(map.size());
        for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
            newElements.put(e.getKey(),f.apply(e.getValue()));
        }
        return FiniteMap.from(newElements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof Matrix)
            o=((Matrix) o).asMap();
        if (!(o instanceof FiniteMap)) return false;
        if(o instanceof FiniteMapImpl){
            FiniteMapImpl that = (FiniteMapImpl) o;
            return Objects.equals(map, that.map);
        }else{
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        int hash=0;
        for (Map.Entry<MathObject, MathObject> e:map.entrySet() ) {
            hash+=Objects.hash(e.getKey(),e.getValue());
        }
        return hash;
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
        for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
            if(sb.length()>1)
                sb.append(", ");
            sb.append(objectToString.apply(e.getKey()));
            sb.append(" -> ");
            sb.append(objectToString.apply(e.getValue()));
        }
        return sb.append('}').toString();
    }
}
