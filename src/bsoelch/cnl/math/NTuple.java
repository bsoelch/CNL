package bsoelch.cnl.math;

import bsoelch.cnl.Constants;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

final class NTuple implements Tuple{
    final MathObject[] objects;

    NTuple(MathObject[] objects) {
        this.objects = objects;
    }

    @Override
    public NumbericValue numericValue() {
        return objects.length>0?objects[0].numericValue():Real.Int.ZERO;
    }

    public int size(){
        return objects.length;
    }

    public MathObject get(int i){
        if(i<0||i>=objects.length)
            throw new IndexOutOfBoundsException("Index out of Bounds:"+i+" size:"+objects.length);
        return objects[i];
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
        return FiniteSet.range(Real.Int.ZERO,Real.from(objects.length-1));
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
        throw new IndexOutOfBoundsException(a+" is no Element of the Domain of this Function");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiniteMap)) return false;
        if(o instanceof Tuple) {
            if (size() != ((Tuple) o).size())
                return false;
            if (o instanceof NTuple) {
                NTuple nTuple = (NTuple) o;
                return Arrays.equals(objects, nTuple.objects);
            } else {
                for (int i = 0; i < objects.length; i++) {
                    if (!objects[i].equals(((Tuple) o).get(i)))
                        return false;
                }
                return true;
            }
        }else if(((FiniteMap) o).domain().equals(domain())){
            for (int i = 0; i < objects.length; i++) {
                if (!objects[i].equals(((FiniteMap) o).evaluateAt(Real.from(i))))
                    return false;
            }
            return true;
        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash=0;
        for (int i=0;i<objects.length;i++ ) {
            hash+= Objects.hash(Real.from(i),objects[i]);
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
        StringBuilder sb=new StringBuilder("[");
        for(MathObject o:objects){
            if(sb.length()>1)
                sb.append(", ");
            sb.append(objectToString.apply(o));
        }
        return sb.append(']').toString();
    }

}
