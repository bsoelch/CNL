package bsoelch.cnl.math;

import bsoelch.cnl.Constants;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

public class FiniteMapImpl implements FiniteMap {
    final TreeMap<MathObject, MathObject> map;

    protected FiniteMapImpl(Map<MathObject, MathObject> map) {
        this.map=new TreeMap<>(map);
    }

    @Override
    public boolean isKey(MathObject key) {
        return map.containsKey(key);
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
    public Iterator<Pair> mapIterator() {
        return new Iterator<Pair>() {
            final Iterator<Map.Entry<MathObject, MathObject>> mapItr=map.entrySet().iterator();
            @Override
            public boolean hasNext() {
                return mapItr.hasNext();
            }
            @Override
            public Pair next() {
                Map.Entry<MathObject, MathObject> e=mapItr.next();
                return new Pair(e.getKey(),e.getValue());
            }
        };
    }

    @Override
    public NumericValue numericValue() {
        if(map.isEmpty())
            return Real.Int.ZERO;
        return map.firstEntry().getValue().numericValue();
    }


    @Override
    public final MathObject evaluateAt(MathObject a) {
        return map.get(a);
    }

    @Override
    public FiniteMap forEach(Function<MathObject, MathObject> f) {
        HashMap<MathObject, MathObject> newElements=new HashMap<>(map.size());
        for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
            newElements.put(e.getKey(),f.apply(e.getValue()));
        }
        return FiniteMap.from(newElements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiniteMap)) return false;
        if(o instanceof FiniteMapImpl){
            FiniteMapImpl that = (FiniteMapImpl) o;
            return Objects.equals(map, that.map);
        }else{
            Iterator<Pair> itr1= mapIterator();
            Iterator<Pair> itr2= ((FiniteMap) o).mapIterator();
            while(itr1.hasNext()&&itr2.hasNext()){
                if(!itr1.next().equals(itr2.next()))
                    return false;
            }
            return !(itr1.hasNext()||itr2.hasNext());
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
    public String toString() {
        return toString(Constants.DEFAULT_BASE,true);
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
