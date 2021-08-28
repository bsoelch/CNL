package bsoelch.cnl.math;

import bsoelch.cnl.Constants;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

public class FiniteMultiMap implements FiniteMap,FiniteMap.MultiMap {
    final TreeMap<MathObject, MathObject> map;
    final int argCount;

    private static int calculateArgCount(Map<MathObject, MathObject> map) {
        //true iff all keys are sparse keys
        boolean spareKeys=true;
        int count=0;
        for(MathObject o: map.keySet()){
            if(o instanceof Pair){
                count=(spareKeys||count==2)?2:1;
                spareKeys=false;
            }else if(o instanceof Tuple){
                int size=((Tuple) o).size();
                count=(spareKeys||count==size)?size:1;
                spareKeys=false;
            }else if(o instanceof FiniteMap){
                Real.Int maxInt=Real.Int.ZERO;
                for(MathObject k:((FiniteMap) o).domain()){
                    if(!(k instanceof Real.Int)){
                        count=1;
                        break;
                    }else if(((Real.Int) k).compareTo(maxInt)>0){
                        maxInt = (Real.Int) k;
                    }
                }
                if(spareKeys||count>1){
                    BigInteger bigValue=maxInt.num();
                    if(bigValue.compareTo(BigInteger.ZERO)<=0) {
                        count = 1;
                        break;
                    }else if(bigValue.compareTo(BigInteger.valueOf(count))>0){
                        count = 1;
                        break;
                    }else if(spareKeys){
                        count = Math.max(count,bigValue.intValueExact());
                    }
                }else{
                    break;
                }
            }else{
                count = 1;
                break;
            }
        }
        return spareKeys?-count:count;
    }

    private static TreeMap<MathObject, MathObject> standardForm(Map<MathObject, MathObject> map, int argCount) {
        TreeMap<MathObject, MathObject> newMap=new TreeMap<>(MathObject::compare);
        if(argCount==1){
            newMap.putAll(map);
        }else{
            //bing map into standard Form (all keys are Tuples of length argCount)
            for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
                if(e.getKey() instanceof Tuple){
                    if(((Tuple) e.getKey()).size()==argCount){
                        if(newMap.put(e.getKey(),e.getValue())!=null){
                            throw new IllegalArgumentException("duplicate Key:"+e.getKey());
                        }
                    }else{
                        throw new RuntimeException("Unexpected size form Tuple:"+((Tuple) e.getKey()).size());
                    }
                }else if(e instanceof FiniteMap){
                    MathObject[] target=new MathObject[argCount];
                    for(int i=0;i<argCount;i++){
                        target[i]=((FiniteMap)e.getKey()).evaluateAt(Real.from((i)));
                    }
                    Tuple tuple = Tuple.create(target);
                    if(newMap.put(tuple,e.getValue())!=null){
                        throw new IllegalArgumentException("duplicate Key:"+tuple);
                    }
                }else{
                    throw new IllegalArgumentException("All keys of MultiMap have to be Tuples or finite Maps");
                }
            }
        }
        return newMap;
    }

    protected FiniteMultiMap(Map<MathObject, MathObject> map, int argCount) {
        if(argCount<1)
            throw new IllegalArgumentException("argCount:" + argCount + " has to be at least one");
        if(argCount!=1) {
            int count = calculateArgCount(map);
            if (count <= 0) {//sparse keys
                count = -count;
                if (argCount < count)
                    throw new IllegalArgumentException("argCount:" + argCount + " less than than minimal possible value: " + count);
            } else {
                if (argCount != count) {
                    throw new IllegalArgumentException("argCount:" + argCount + " possible values for given data: 1 or " + count);
                }
            }
        }
        this.argCount = argCount;
        this.map =standardForm(map,argCount);
    }

    @Override
    public int argCount() {
        return argCount;
    }

    @Override
    public boolean isKey(MathObject key) {
        return map.containsKey(key);
    }

    @Override
    public boolean hasKeyStartingWith(MathObject o) {
        MathObject[] elements= FiniteMap.splitArgument(o,argCount());
        if(elements.length==0)
            return true;
        for(MathObject k:map.keySet()){
            if(k instanceof Tuple){
                boolean match=true;
                for(int i=0;i<elements.length;i++){
                    if(!elements[i].equals(((Tuple) k).get(i)))
                        match=false;
                }
                if(match)
                    return true;
            }else{
                if(elements.length==1&&k.equals(elements[0]))
                    return true;
            }
        }
        return false;
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
    public Scalar scalarValue() {
        if(map.isEmpty())
            return Real.Int.ZERO;
        return map.firstEntry().getValue().scalarValue();
    }


    @Override
    public final MathObject evaluateAt(MathObject a) {
        return evaluateAt(FiniteMap.splitArgument(a,argCount()));
    }

    /**evaluates is MultiMap at the given Key,
     * when the key-length is lower than the total number of arguments a PartialMultiMap is returned*/
    public MathObject evaluateAt(MathObject[] args) {
        if(args.length>argCount)
            throw new IllegalArgumentException("To many Arguments");
        if(argCount==0){
            return this;
        }else if(argCount==1){
            return map.get(args[0]);
        }else if(args.length<argCount){
            return partialMap(args);
        }else{
            return map.get(Tuple.create(args));
        }
    }

    @Override
    public FiniteMultiMap forEach(Function<MathObject, MathObject> f) {
        HashMap<MathObject, MathObject> newElements=new HashMap<>(map.size());
        for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
            newElements.put(e.getKey(),f.apply(e.getValue()));
        }
        return new FiniteMultiMap(newElements,argCount);
    }


    private FiniteMultiMap partialMap(MathObject[] header) {
        if(header.length==0)
            return this;
        HashMap<MathObject, MathObject> partialMap=new HashMap<>(map.size());
        int newCount=argCount-header.length;
        for(Map.Entry<MathObject, MathObject> e:map.entrySet()){
            if(e.getKey() instanceof Tuple){
                boolean match=true;
                for(int i=0;i<header.length;i++){
                    if(!header[i].equals(((Tuple) e.getKey()).get(i)))
                        match=false;
                }
                if(match) {
                    MathObject[] elements=new MathObject[newCount];
                    for(int i=header.length;i<argCount;i++){
                        elements[i-header.length]=((Tuple) e.getKey()).get(i);
                    }
                    if(elements.length==1){
                        partialMap.put(elements[0],e.getValue());
                    }else{
                        partialMap.put(Tuple.create(elements),e.getValue());
                    }
                }
            }else{
                throw new RuntimeException("Unexpected non-Tuple Key in Multimap");
            }
        }
        return new FiniteMultiMap(partialMap,newCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiniteMap)) return false;
        if(o instanceof FiniteMultiMap){
            FiniteMultiMap that = (FiniteMultiMap) o;
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
