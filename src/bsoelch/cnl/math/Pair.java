package bsoelch.cnl.math;

import bsoelch.cnl.Constants;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;

public final class Pair implements Tuple{
    public final MathObject a,b;

    public Pair(MathObject a, MathObject b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public NumbericValue numericValue() {
        return a.numericValue();
    }

    @Override
    public FiniteSet domain() {
        return FiniteSet.PAIR_KEY;
    }

    @Override
    public boolean isKey(MathObject key) {
        return key.equals(Real.Int.ZERO)||key.equals(Real.Int.ONE);
    }

    @Override
    public FiniteSet values() {
        return FiniteSet.from(a,b);
    }


    @Override
    public MathObject evaluateAt(MathObject a) {
        if(a.equals(Real.Int.ZERO))
            return this.a;
        if(a.equals(Real.Int.ONE))
            return this.b;
        throw new IllegalArgumentException(a+" is not an Element of the domain of this Function");
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public MathObject get(int i) {
        if(i==0)
            return this.a;
        if(i==1)
            return this.b;
        throw new IndexOutOfBoundsException("Index out of Bounds: "+i+" size: 2");
    }

    @Override
    public FiniteMap forEach(Function<MathObject, MathObject> f) {
        return new Pair(f.apply(a),f.apply(b));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiniteMap)) return false;
        if(o instanceof Pair){
            Pair pair = (Pair) o;
            return Objects.equals(a, pair.a) && Objects.equals(b, pair.b);
        }else if(o instanceof Tuple){
            return ((Tuple) o).size()==2&&Objects.equals(a, ((Tuple) o).get(0)) && Objects.equals(b,((Tuple) o).get(1));
        }else{
            FiniteMap f=(FiniteMap) o;
            if(!f.domain().equals(FiniteSet.PAIR_KEY))
                return false;
            return a.equals(f.evaluateAt(Real.Int.ZERO))&&b.equals(f.evaluateAt(Real.Int.ONE));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(Real.Int.ZERO,a)+Objects.hash(Real.Int.ONE,b);
    }

    @Override
    public String toString(BigInteger base, boolean useSmallBase) {
        return "["+a.toString(base, useSmallBase)+", "+b.toString(base, useSmallBase)+"]";
    }
    @Override
    public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
        return "["+a.toStringFixedPoint(base,precision, useSmallBase)+", "+
                b.toStringFixedPoint(base,precision, useSmallBase)+"]";
    }
    @Override
    public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
        return "["+a.toStringFloat(base,precision, useSmallBase)+", "+
                b.toStringFloat(base,precision, useSmallBase)+"]";
    }

    @Override
    public String toString() {
        return toString(Constants.DEFAULT_BASE,true);
    }
}
