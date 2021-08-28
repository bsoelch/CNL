package bsoelch.cnl.math;

import bsoelch.cnl.Constants;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface FiniteMap extends MathObject {
    Tuple EMPTY_MAP=new Tuple() {
        @Override
        public int size() {
            return 0;
        }
        @Override
        public MathObject get(int i) {
            return Real.Int.ZERO;
        }
        @Override
        public boolean isKey(MathObject key) {
            return false;
        }
        @Override
        public FiniteMap forEach(Function<MathObject, MathObject> f) {
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
        public Scalar scalarValue() {
            return Real.Int.ZERO;
        }
        @Override
        public FiniteSet asSet() {
            return FiniteSet.EMPTY_SET;
        }
        @Override
        public boolean equals(Object obj) {
            return obj instanceof FiniteMap&&((FiniteMap) obj).domain().size()==0;
        }
        @Override
        public int hashCode() {
            return 0;
        }
        @Override
        public String toString() {
            return "()";
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

    int size();

    interface MultiMap extends FiniteMap{
        boolean hasKeyStartingWith(MathObject o);
        int argCount();
        MathObject evaluateAt(MathObject[] ars);
    }
    static MathObject[] splitArgument(MathObject a, int maxCount) {
        if(maxCount>1){
            if(a instanceof Pair){//split pairs
                return new MathObject[]{((Pair) a).a,((Pair) a).b};
            }else if(a instanceof Tuple){//split tuples
                if(((Tuple) a).size()<maxCount){
                    MathObject[] values=new MathObject[((Tuple) a).size()];
                    for(int i=0;i<((Tuple) a).size();i++) {
                        values[i]=((Tuple) a).get(i);
                    }
                    return values;
                }
            }else if(a instanceof FiniteMap){//detect mapArrays
                FiniteSet domain=((FiniteMap) a).domain();
                if(domain.size()<=maxCount){
                    boolean validArray=true;
                    MathObject[] values=new MathObject[domain.size()];
                    for(MathObject key:domain){
                        if(key instanceof Real.Int&&((Real.Int) key).compareTo(Real.Int.ZERO)>=0){
                            BigInteger intValue=((Real.Int) key).num();
                            if(intValue.compareTo(BigInteger.valueOf(maxCount))>0){
                                validArray=false;
                                break;
                            }else{
                                int keyInt = intValue.intValueExact();
                                if(values.length<=keyInt){
                                    values= Arrays.copyOf(values,keyInt+1);
                                }
                                values[keyInt]=((FiniteMap)a).evaluateAt(key);
                            }
                        }else{
                            validArray=false;
                            break;
                        }
                    }
                    if(validArray){
                        for(int i=0;i<values.length;i++){
                            if(values[i]==null)
                                values[i]=Real.Int.ZERO;
                        }
                        return values;
                    }
                }
            }
        }
        //a is no header of a key in this Map
        return new MathObject[]{a};
    }

    FiniteMap forEach(Function<MathObject, MathObject> f);

    static FiniteMap from(Map<MathObject,MathObject> map, int minLen){
        //TODO detect (partial) tuples
        if(map.isEmpty()){
            return FiniteMap.EMPTY_MAP;
        }else{
            return new FiniteMultiMap(map,minLen);
        }
    }
    static FiniteMap forEach(FiniteMap m1, FiniteMap m2, BiFunction<MathObject, MathObject, MathObject> f) {
        TreeMap<MathObject,MathObject> entries=new TreeMap<>(MathObject::compare);
        int minLen=-1;
        for (Iterator<Pair> it = m1.mapIterator(); it.hasNext(); ) {
            Pair p = it.next();
            if(p.a instanceof Tuple){
                minLen=Math.min(minLen,((Tuple) p.a).size());
            }else{
                minLen=1;
            }
            if(m2.isKey(p.a)){
                entries.put(p.a,f.apply(p.b,m2.evaluateAt(p.a)));
            }else{
                entries.put(p.a,f.apply(p.b,Real.Int.ZERO));
            }
        }
        for (Iterator<Pair> it = m1.mapIterator(); it.hasNext(); ) {
            Pair p = it.next();
            if(p.a instanceof Tuple){
                minLen=Math.min(minLen,((Tuple) p.a).size());
            }else{
                minLen=1;
            }
            if(!m1.isKey(p.a)){
                entries.put(p.a,f.apply(Real.Int.ZERO,p.b));
            }
        }
        return from(entries,minLen);
    }

    static FiniteMap indicatorMap(FiniteSet s) {
        return constantMap(s,Real.Int.ONE);
    }
    static FiniteMap constantMap(FiniteSet keys, MathObject o) {
        return new FiniteMap() {
            @Override
            public FiniteMap forEach(Function<MathObject, MathObject> f) {
                return constantMap(keys,f.apply(o));
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
                return FiniteSet.from(o);
            }
            @Override
            public MathObject evaluateAt(MathObject a) {
                if(keys.contains(a))
                    return o;
                else
                    return Real.Int.ZERO;
            }
            @Override
            public Scalar scalarValue() {
                return o.scalarValue();
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
                for(MathObject o:keys){
                    if(sb.length()>1)
                        sb.append(", ");
                    sb.append(objectToString.apply(o)).append(" -> ").append(objectToString.apply(o));
                }
                return sb.append('}').toString();
            }

        };
    }

    FiniteSet domain();

    FiniteSet values();

    default FiniteSet asSet() {
        TreeSet<MathObject> pairs=new TreeSet<>(MathObject::compare);
        for (Iterator<Pair> it = mapIterator(); it.hasNext(); ) {
            Pair p = it.next();
            pairs.add(p);
        }
        return FiniteSet.from(pairs);
    }

    default Iterator<Pair> mapIterator(){
        return new Iterator<Pair>() {
            final Iterator<MathObject> keyItr=domain().iterator();
            @Override
            public boolean hasNext() {
                return keyItr.hasNext();
            }
            @Override
            public Pair next() {
                MathObject next = keyItr.next();
                return new Pair(next,evaluateAt(next));
            }
        };
    }

    @Override
    default String intsAsString(){
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

    default boolean isKey(MathObject key) {
        return domain().contains(key);
    }

    MathObject evaluateAt(MathObject a);

}
