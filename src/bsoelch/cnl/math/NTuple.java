package bsoelch.cnl.math;


import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;
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
    public MathObject[] toArray() {
        return Arrays.copyOf(objects,objects.length);
    }

    @Override
    public <T extends MathObject> T[] toArray(Class<T[]> cls) {
        return Arrays.copyOf(objects,objects.length,cls);
    }

    @Override
    public Tuple forEach(Function<MathObject, MathObject> f) {
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
    public boolean isKey(MathObject key) {
        if(key instanceof Real.Int){
            BigInteger value=((Real.Int) key).num();
            return value.compareTo(BigInteger.ZERO) >= 0 && value.compareTo(BigInteger.valueOf(objects.length)) < 0;
        }
        return false;
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
        for(MathObject o:objects){
            if(sb.length()>1)
                sb.append(", ");
            if(!o.equals(Real.Int.ZERO)) {
                sb.append(objectToString.apply(o));
            }
        }
        return sb.append(']').toString();
    }

}
