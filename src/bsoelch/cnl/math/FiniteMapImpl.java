package bsoelch.cnl.math;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

public final class FiniteMapImpl extends FiniteMap {
    final TreeMap<MathObject, MathObject> map;

    /**constructor of FiniteMapImpl, this method should only be called from {@link FiniteMap#from(Map)}*/
    FiniteMapImpl(Map<? extends MathObject, MathObject> map) {
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
    public MathObject evaluateAt(MathObject a) {
        MathObject o=map.get(a);
        return o==null?Real.Int.ZERO:o;
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
