package bsoelch.cnl.math;


import java.math.BigInteger;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

final class NTuple extends Tuple{
    final MathObject[] objects;
    final int nonZeros;

    NTuple(MathObject[] objects) {
        this.objects = objects;
        int nz=0;
        for(MathObject o:objects) {
            if (!o.equals(Real.Int.ZERO))
                nz++;
        }
        nonZeros=nz;
    }

    @Override
    public NumericValue numericValue() {
        return objects.length>0?objects[0].numericValue():Real.Int.ZERO;
    }

    @Override
    public int size() {
        return nonZeros;
    }
    public int length(){
        return objects.length;
    }

    public MathObject get(int i){
        if(i<0||i>=objects.length)
            return Real.Int.ZERO;
        return objects[i];
    }

    @Override
    public MathObject insert(MathObject value) {
        MathObject[] newValues=Arrays.copyOf(objects,objects.length+1);
        newValues[objects.length]=value;
        return Tuple.create(newValues);
    }
    @Override
    public FiniteMap put(MathObject key, MathObject value) {
        TreeMap<MathObject,MathObject> map=new TreeMap<>(MathObject::compare);
        for(int i=0;i<objects.length;i++){
            if(objects[i]!=null&&!objects[i].equals(Real.Int.ZERO)){
                map.put(Real.from(i),objects[i]);
            }
        }
        map.put(key,value);
        return FiniteMap.from(map, FiniteMap.TUPLE_WRAP_ZERO_TERMINATED);
    }
    @Override
    public Tuple insert(MathObject value, int index) {
        if(index<0)
            throw new ArithmeticException("Negative index in Tuple");
        if(index<= objects.length){
            MathObject[] newValues=new MathObject[objects.length+1];
            System.arraycopy(objects,0,newValues,0,index);
            newValues[index]=value;
            System.arraycopy(objects,index,newValues,index+1,objects.length-index);
            return Tuple.create(newValues);
        }else if(index<Tuple.SPARSE_FACTOR* objects.length){
            MathObject[] newValues=new MathObject[index+1];
            System.arraycopy(objects,0,newValues,0,objects.length);
            newValues[index]=value;
            return Tuple.create(newValues);
        }else{
            TreeMap<MathObject,MathObject> newMap=new TreeMap<>(MathObject::compare);
            MathObject realIndex=Real.from(index);
            newMap.put(realIndex,value);
            for (int i=0;i<objects.length;i++) {
                newMap.put(Real.from(i),objects[i]);
            }
            return FiniteMap.createTuple(newMap,index+1);
        }
    }
    @Override
    public FiniteMap remove(int index) {
        if(index<0)
            throw new ArithmeticException("Negative index in Tuple");
        if(index<objects.length){
            MathObject[] newValues=new MathObject[objects.length-1];
            System.arraycopy(objects,0,newValues,0,index);
            System.arraycopy(objects,index+1,newValues,index,objects.length-index-1);
            return Tuple.create(newValues);
        }else{
            return this;
        }
    }

    @Override
    public FiniteMap remove(MathObject value) {
        MathObject[] newValues=new MathObject[objects.length];
        for(int i=0;i<objects.length;i++){
            if(objects[i].equals(value)){
                newValues[i] = Real.Int.ZERO;
            }else {
                newValues[i] = objects[i];
            }
        }
        return Tuple.create(Arrays.copyOf(newValues,objects.length));
    }
    @Override
    public Tuple tupleRemove(MathObject value) {
        MathObject[] newValues=new MathObject[objects.length];
        int removed=0;
        for(int i=0;i<objects.length;i++){
            if(objects[i].equals(value)){
                removed++;
            }else {
                newValues[i - removed] = objects[i];
            }
        }
        return Tuple.create(Arrays.copyOf(newValues,objects.length-removed));
    }
    @Override
    public FiniteMap removeKey(MathObject key) {
        MathObject[] newValues=new MathObject[objects.length];
        for(int i=0;i<objects.length;i++){
            if(Real.from(i).equals(key)){
                newValues[i] = Real.Int.ZERO;
            }else {
                newValues[i] = objects[i];
            }
        }
        return Tuple.create(newValues);
    }
    @Override
    public FiniteMap removeIf(BinaryOperator<MathObject> condition) {
        MathObject[] newValues=new MathObject[objects.length];
        for(int i=0;i<objects.length;i++){
            if(MathObject.isTrue(condition.apply(Real.from(i),objects[i]))){
                newValues[i] = Real.Int.ZERO;
            }else {
                newValues[i] = objects[i];
            }
        }
        return Tuple.create(newValues);
    }

    /**first Integer in this range that is greater that o
     * @param allowEq if true it is allowed that the returned index may be equal to o*/
    private int nextIndex(MathObject o, boolean allowEq) {
        int l=0,r=objects.length,c;
        do{
            c=(l+r)/2;
            int cp = MathObject.compare(o, Real.from(c));
            if(cp ==0){
                r= allowEq ?c:c+1;
                break;
            }else if(cp >0){
                l=c;
            }else{
                r=c;
            }
        }while (r-l>1);
        return r;
    }
    @Override
    public FiniteMap headMap(MathObject last, boolean include) {
        if(MathObject.compare(last,Real.Int.ZERO)<0)
            return EMPTY_MAP;
        if(MathObject.compare(last,Real.from(objects.length))>0)
            return this;
        int i = nextIndex(last, !include);
        MathObject[] newData=new MathObject[i];
        System.arraycopy(objects,0,newData,0,i);
        return Tuple.create(newData);
    }

    @Override
    public FiniteMap tailMap(MathObject first, boolean include) {
        if(MathObject.compare(first,Real.Int.ZERO)<0)
            return this;
        if(MathObject.compare(first,Real.from(objects.length))>0)
            return EMPTY_MAP;
        int i = nextIndex(first, include);
        MathObject[] newData=new MathObject[objects.length];
        System.arraycopy(objects,i,newData,i,objects.length-i);
        return Tuple.create(newData);
    }
    @Override
    public FiniteMap range(MathObject first, boolean includeFirst, MathObject last, boolean includeLast) {
        if(MathObject.compare(last,first)<0)
            return EMPTY_MAP;
        if(MathObject.compare(last,Real.Int.ZERO)<0)
            return EMPTY_MAP;
        if(MathObject.compare(first,Real.from(objects.length))>0)
            return EMPTY_MAP;
        int start = nextIndex(first, includeFirst);
        int end = nextIndex(last, !includeFirst);
        MathObject[] newData=new MathObject[end];
        System.arraycopy(objects,start,newData,start,end-start);
        return Tuple.create(newData);
    }

    @Override
    public MathObject[] toArray() {
        return Arrays.copyOf(objects,objects.length);
    }

    @Override
    public <T extends MathObject> T[] toArray(Class<T[]> cls) {
        return Arrays.copyOf(objects,objects.length,cls);
    }

    @Override
    public Tuple replace(Function<MathObject, MathObject> f) {
        MathObject[] newObjects=new MathObject[objects.length];
        for(int i=0;i<objects.length;i++)
            newObjects[i]=f.apply(objects[i]);
        return Tuple.create(newObjects);
    }

    @Override
    public FiniteSet domain() {
        TreeSet<Real.Int> keys=new TreeSet<>();
        for(int i=0;i<objects.length;i++){
            if(!objects[i].equals(Real.Int.ZERO))
                keys.add(Real.from(i));
        }
        return FiniteSet.from(keys);
    }

    @Override
    public FiniteSet values() {
        return FiniteSet.from(objects);
    }



    @Override
    public MathObject evaluateAt(MathObject a) {
        if(a instanceof Real.Int){
            BigInteger value=((Real.Int) a).num();
            if(value.compareTo(BigInteger.ZERO)>=0&&value.compareTo(BigInteger.valueOf(objects.length))<0){
                return objects[value.intValueExact()];
            }
        }
        return Real.Int.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof Matrix)
            o=((Matrix) o).asMap();
        if (!(o instanceof FiniteMap)) return false;
        if(o instanceof NTuple&&((NTuple) o).length()==length()) {
            NTuple nTuple = (NTuple) o;
            return Arrays.equals(objects, nTuple.objects);
        }else {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        int hash=0;
        for (int i=0;i<objects.length;i++ ) {
            if(!objects[i].equals(Real.Int.ZERO)) {
                hash += Objects.hash(Real.from(i), objects[i]);
            }
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
        StringBuilder sb=new StringBuilder("[");
        boolean first=true;
        for(MathObject o:objects){
            if(first){
                first=false;
            }else{
                sb.append(", ");
            }
            if(!o.equals(Real.Int.ZERO)) {
                sb.append(objectToString.apply(o));
            }
        }
        return sb.append(']').toString();
    }

}
