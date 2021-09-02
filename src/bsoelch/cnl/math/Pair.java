package bsoelch.cnl.math;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;

public final class Pair extends Tuple{
    public final MathObject a,b;

    public Pair(MathObject a, MathObject b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public NumericValue numericValue() {
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
        return Real.Int.ZERO;
    }

    @Override
    public int size() {
        return (a.equals(Real.Int.ZERO)?0:1)+(b.equals(Real.Int.ZERO)?0:1);
    }

    @Override
    public int length() {
        return 2;
    }

    @Override
    public MathObject get(int i) {
        if(i==0)
            return this.a;
        if(i==1)
            return this.b;
        return Real.Int.ZERO;
    }

    @Override
    public FiniteMap forEach(Function<MathObject, MathObject> f) {
        return new Pair(f.apply(a),f.apply(b));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof Matrix)
            o=((Matrix) o).asMap();
        if (!(o instanceof FiniteMap)) return false;
        if(o instanceof Pair){
            Pair pair = (Pair) o;
            return Objects.equals(a, pair.a) && Objects.equals(b, pair.b);
        }else if(o instanceof Tuple&&((Tuple) o).length()==2){
            return Objects.equals(a, ((Tuple) o).get(0)) && Objects.equals(b,((Tuple) o).get(1));
        }else {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return (a.equals(Real.Int.ZERO)?0:Objects.hash(Real.Int.ZERO,a))+
                (b.equals(Real.Int.ZERO)?0:Objects.hash(Real.Int.ONE,b));
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
}
