package bsoelch.cnl.math;

import bsoelch.cnl.Constants;
import bsoelch.cnl.math.expression.ExpressionNode;

import java.math.BigInteger;

/**scalar value, either a {@link NumericScalar} or a {@link Polynomial}*/
public abstract class Scalar implements ExpressionNode, MathObject,Comparable<Scalar> {
    Scalar(){}//package private constructor

    public abstract boolean isInt();
    public abstract boolean isReal();

    public abstract Scalar realPart();
    public abstract Scalar imaginaryPart();

    public abstract Scalar conjugate();
    public abstract Real sqAbs();

    @Override
    public Scalar scalarValue() {
        return this;
    }

    public abstract Scalar negate();

    public abstract Scalar invert();

    abstract public Scalar round(int mode);

    public abstract Scalar approx(Real precision);

    @Override
    public String toString() {
        return toString(Constants.DEFAULT_BASE, true);
    }


    /**{@link Scalar} representing a number has to be either a {@link Real} or a {@link Complex}*/
    public abstract static class NumericScalar extends Scalar {
        NumericScalar(){}//package-private Constructor
        public abstract Real realPart();
        public abstract Real imaginaryPart();
        public abstract NumericScalar conjugate();

        public abstract NumericScalar negate();
        public abstract NumericScalar invert();

        abstract public NumericScalar round(int mode);
        public abstract NumericScalar approx(Real precision);
        @Override
        public NumericScalar numericValue() {
            return this;
        }


        static public NumericScalar add(NumericScalar a, NumericScalar b){
            if(a instanceof Real){
                if(b instanceof Real){
                    return Real.add((Real)a,(Real)b);
                }else if(b instanceof Complex){
                    return Complex.from(Real.add((Real)a,b.realPart()),b.imaginaryPart());
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else if(a instanceof Complex){
                if(b instanceof Real){
                    return Complex.from(Real.add(a.realPart(),(Real)b),a.imaginaryPart());
                }else if(b instanceof Complex){
                    return Complex.from(Real.add(a.realPart(),b.realPart()),Real.add(a.imaginaryPart(),b.imaginaryPart()));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else{
                throw new IllegalArgumentException("Unexpected NumericScalar-class:"+a.getClass());
            }
        }

        static public NumericScalar subtract(NumericScalar a, NumericScalar b){
            if(a instanceof Real){
                if(b instanceof Real){
                    return Real.subtract((Real)a,(Real)b);
                }else if(b instanceof Complex){
                    return Complex.from(Real.subtract((Real)a,b.realPart()),b.imaginaryPart().negate());
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else if(a instanceof Complex){
                if(b instanceof Real){
                    return Complex.from(Real.subtract(a.realPart(),(Real)b),a.imaginaryPart());
                }else if(b instanceof Complex){
                    return Complex.from(Real.subtract(a.realPart(),b.realPart()),Real.subtract(a.imaginaryPart(),b.imaginaryPart()));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else{
                throw new IllegalArgumentException("Unexpected NumericScalar-class:"+a.getClass());
            }
        }

        static public NumericScalar multiply(NumericScalar a, NumericScalar b){
            if(a instanceof Real){
                if(b instanceof Real){
                    return Real.multiply((Real)a,(Real)b);
                }else if(b instanceof Complex){
                    return Complex.from(Real.multiply((Real)a,b.realPart()),Real.multiply((Real)a,b.imaginaryPart()));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else if(a instanceof Complex){
                if(b instanceof Real){
                    return Complex.from(Real.multiply(a.realPart(),(Real)b),Real.multiply(a.imaginaryPart(),(Real)b));
                }else if(b instanceof Complex){
                    return Complex.from(Real.subtract(Real.multiply(a.realPart(),b.realPart()), Real.multiply(a.imaginaryPart(),b.imaginaryPart())),
                            Real.add(Real.multiply(a.realPart(),b.imaginaryPart()), Real.multiply(a.imaginaryPart(),b.realPart())));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else{
                throw new IllegalArgumentException("Unexpected NumericScalar-class:"+a.getClass());
            }
        }

        static public NumericScalar divide(NumericScalar a, NumericScalar b){
            if(a instanceof Real){
                if(b instanceof Real){
                    return Real.divide((Real)a,(Real)b);
                }else if(b instanceof Complex){
                    return multiply(a,b.invert());
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else if(a instanceof Complex){
                if(b instanceof Real){
                    return Complex.from(Real.divide(a.realPart(),(Real)b),Real.divide(a.imaginaryPart(),(Real)b));
                }else if(b instanceof Complex){
                    return multiply(a,b.invert());
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else{
                throw new IllegalArgumentException("Unexpected NumericScalar-class:"+a.getClass());
            }
        }

        /**a&floor b*/
        public static NumericScalar floorAnd(NumericScalar a, NumericScalar b) {
            if(a instanceof Real){
                if(b instanceof Real){
                    return Real.floorAnd((Real)a,(Real)b);
                }else if(b instanceof Complex){
                    return Real.floorAnd((Real)a,b.realPart());
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else if(a instanceof Complex){
                if(b instanceof Real){
                    return Real.floorAnd((Real)b,a.realPart());
                }else if(b instanceof Complex){
                    return Complex.from(Real.floorAnd(a.realPart(),b.realPart()),Real.floorAnd(a.imaginaryPart(),b.imaginaryPart()));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else{
                throw new IllegalArgumentException("Unexpected NumericScalar-class:"+a.getClass());
            }
        }

        /**a|floor b*/
        public static NumericScalar floorOr(NumericScalar a, NumericScalar b) {
            if(a instanceof Real){
                if(b instanceof Real){
                    return Real.floorOr((Real)a,(Real)b);
                }else if(b instanceof Complex){
                    return Complex.from(Real.floorOr((Real)a,b.realPart()),b.imaginaryPart());
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else if(a instanceof Complex){
                if(b instanceof Real){
                    return Complex.from(Real.floorOr((Real)b,a.realPart()),a.imaginaryPart());
                }else if(b instanceof Complex){
                    return Complex.from(Real.floorOr(a.realPart(),b.realPart()),Real.floorOr(a.imaginaryPart(),b.imaginaryPart()));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else{
                throw new IllegalArgumentException("Unexpected NumericScalar-class:"+a.getClass());
            }
        }
        /**a^floor b*/
        public static NumericScalar floorXor(NumericScalar a, NumericScalar b) {
            if(a instanceof Real){
                if(b instanceof Real){
                    return Real.floorXor((Real)a,(Real)b);
                }else if(b instanceof Complex){
                    return Complex.from(Real.floorXor((Real)a,b.realPart()),b.imaginaryPart());
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else if(a instanceof Complex){
                if(b instanceof Real){
                    return Complex.from(Real.floorXor((Real)b,a.realPart()),a.imaginaryPart());
                }else if(b instanceof Complex){
                    return Complex.from(Real.floorXor(a.realPart(),b.realPart()),Real.floorXor(a.imaginaryPart(),b.imaginaryPart()));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else{
                throw new IllegalArgumentException("Unexpected NumericScalar-class:"+a.getClass());
            }
        }
        /**a&!floor b*/
        public static NumericScalar floorAndNot(NumericScalar a, NumericScalar b) {
            if(a instanceof Real){
                if(b instanceof Real){
                    return Real.floorAndNot((Real)a,(Real)b);
                }else if(b instanceof Complex){
                    return Complex.from(Real.floorAndNot((Real)a,b.realPart()),b.imaginaryPart());
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else if(a instanceof Complex){
                if(b instanceof Real){
                    return Complex.from(Real.floorAndNot((Real)b,a.realPart()),a.imaginaryPart());
                }else if(b instanceof Complex){
                    return Complex.from(Real.floorAndNot(a.realPart(),b.realPart()),Real.floorAndNot(a.imaginaryPart(),b.imaginaryPart()));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else{
                throw new IllegalArgumentException("Unexpected NumericScalar-class:"+a.getClass());
            }
        }
        public static NumericScalar fAdd(NumericScalar a, NumericScalar b) {
            if(a instanceof Real){
                if(b instanceof Real){
                    return Real.fAdd((Real)a,(Real)b);
                }else if(b instanceof Complex){
                    return Complex.from(Real.fAdd(((Real)a),b.realPart()),Real.fAdd(Real.Int.ZERO,b.imaginaryPart()));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else if(a instanceof Complex){
                if(b instanceof Real){
                    return Complex.from(Real.fAdd(a.realPart(),((Real)b)),Real.fAdd(a.imaginaryPart(),Real.Int.ZERO));
                }else if(b instanceof Complex){
                    return Complex.from(Real.fAdd(a.realPart(),b.realPart()),Real.fAdd(a.imaginaryPart(),b.imaginaryPart()));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else{
                throw new IllegalArgumentException("Unexpected NumericScalar-class:"+a.getClass());
            }
        }
        public static NumericScalar concat(NumericScalar a, NumericScalar b) {
            if(a instanceof Real){
                if(b instanceof Real){
                    return Real.concat((Real)a,(Real)b);
                }else if(b instanceof Complex){
                    return Complex.from(Real.concat((Real)a,b.realPart()),b.imaginaryPart());
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else if(a instanceof Complex){
                if(b instanceof Real){
                    return Complex.from(Real.concat(a.realPart(),(Real)b),a.imaginaryPart());
                }else if(b instanceof Complex){
                    return Complex.from(Real.concat(a.realPart(),b.realPart()),Real.concat(a.imaginaryPart(),b.imaginaryPart()));
                }else{
                    throw new IllegalArgumentException("Unexpected NumericScalar-class:"+b.getClass());
                }
            }else{
                throw new IllegalArgumentException("Unexpected NumericScalar-class:"+a.getClass());
            }
        }
    }
    public static Scalar min(Scalar a,Scalar b) {
        return a.compareTo(b)<=0?a:b;
    }
    public static Scalar max(Scalar a, Scalar b) {
        return a.compareTo(b)>=0?a:b;
    }

    static public Scalar add(Scalar a, Scalar b){
        if(a instanceof NumericScalar){
            if(b instanceof NumericScalar){
                return NumericScalar.add((NumericScalar) a,(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return ((Polynomial) b).forNumericElement(s->NumericScalar.add((NumericScalar)a,s));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Polynomial){
            if(b instanceof NumericScalar){
                return ((Polynomial)a).forNumericElement(s->NumericScalar.add(s,(NumericScalar)b));
            }else if(b instanceof Polynomial){
                return ((Polynomial) a).forEachElement((Polynomial) b,NumericScalar::add);
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
        }
    }

    static public Scalar subtract(Scalar a, Scalar b){
        if(a instanceof NumericScalar){
            if(b instanceof NumericScalar){
                return NumericScalar.subtract((NumericScalar) a,(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return ((Polynomial) b).forNumericElement(s->NumericScalar.subtract((NumericScalar)a,s));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Polynomial){
            if(b instanceof NumericScalar){
                return ((Polynomial)a).forNumericElement(s->NumericScalar.subtract(s,(NumericScalar)b));
            }else if(b instanceof Polynomial){
                return ((Polynomial) a).forEachElement((Polynomial) b,NumericScalar::subtract);
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
        }
    }

    static public Scalar multiply(Scalar a, Scalar b){
        if(a instanceof NumericScalar){
            if(b instanceof NumericScalar){
                return NumericScalar.multiply((NumericScalar) a,(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return ((Polynomial) b).forEachElement(s->NumericScalar.multiply((NumericScalar) a,s));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Polynomial){
            if(b instanceof NumericScalar){
                return ((Polynomial) a).forEachElement(s->NumericScalar.multiply(s,(NumericScalar) b));
            }else if(b instanceof Polynomial){
                return ((Polynomial) a).multiply((Polynomial) b);
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
        }
    }

    static public Scalar divide(Scalar a, Scalar b){
        if(a instanceof NumericScalar){
            if(b instanceof NumericScalar){
                return NumericScalar.divide((NumericScalar) a,(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return ((Polynomial) b).forEachElement(s->NumericScalar.divide((NumericScalar) a,s));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Polynomial){
            if(b instanceof NumericScalar){
                return ((Polynomial) a).forEachElement(s->NumericScalar.divide(s,(NumericScalar) b));
            }else if(b instanceof Polynomial){//TODO Polynomial Fraction
                throw new UnsupportedOperationException("Not Yet Implemented");
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
        }
    }
    public static Scalar mod(Scalar l,Scalar r){
        return subtract(l, multiply(r, divide(l,r).round(FLOOR)));
    }
    public static Scalar pow(Scalar l,Scalar r){
        return powBigInt(l,r.numericValue().round(-1).realPart().num());
    }
    static Scalar powBigInt(Scalar l, BigInteger power) {
        if(power.signum()==0){
            return Real.Int.ONE;
        }else if(power.signum()<0){
            return powBigInt(l,power.negate()).invert();
        }else{
            Scalar ret= Real.Int.ONE,s= l;
            while(power.signum()>0){
                if(power.and(BigInteger.ONE).equals(BigInteger.ONE)){
                    ret= multiply(ret,s);
                }
                s= multiply(s,s);
                power = power.shiftRight(1);
            }
            return ret;
        }
    }

    /**a&floor b*/
    public static Scalar floorAnd(Scalar a, Scalar b) {
        if(a instanceof NumericScalar){
            if(b instanceof NumericScalar){
                return NumericScalar.floorAnd((NumericScalar) a,(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return NumericScalar.floorAnd((NumericScalar) a,b.numericValue());
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Polynomial){
            if(b instanceof NumericScalar){
                return NumericScalar.floorAnd(a.numericValue(),(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return ((Polynomial) a).forEachElement((Polynomial) b,NumericScalar::floorAnd);
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
        }
    }
    /**a|floor b*/
    public static Scalar floorOr(Scalar a, Scalar b) {
        if(a instanceof NumericScalar){
            if(b instanceof NumericScalar){
                return NumericScalar.floorOr((NumericScalar) a,(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return ((Polynomial) b).forNumericElement(s->NumericScalar.floorOr((NumericScalar)a,s));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Polynomial){
            if(b instanceof NumericScalar){
                return ((Polynomial)a).forNumericElement(s->NumericScalar.floorOr(s,(NumericScalar)b));
            }else if(b instanceof Polynomial){
                return ((Polynomial) a).forEachElement((Polynomial) b,NumericScalar::floorOr);
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
        }
    }

    /**a^floor b*/
    public static Scalar floorXor(Scalar a, Scalar b) {
        if(a instanceof NumericScalar){
            if(b instanceof NumericScalar){
                return NumericScalar.floorXor((NumericScalar) a,(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return ((Polynomial) b).forNumericElement(s->NumericScalar.floorXor((NumericScalar)a,s));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Polynomial){
            if(b instanceof NumericScalar){
                return ((Polynomial)a).forNumericElement(s->NumericScalar.floorXor(s,(NumericScalar)b));
            }else if(b instanceof Polynomial){
                return ((Polynomial) a).forEachElement((Polynomial) b,NumericScalar::floorXor);
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
        }
    }
    /**a&!floor b*/
    public static Scalar floorAndNot(Scalar a, Scalar b) {
        if(a instanceof NumericScalar){
            if(b instanceof NumericScalar){
                return NumericScalar.floorAndNot((NumericScalar) a,(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return ((Polynomial) b).forNumericElement(s->NumericScalar.floorAndNot((NumericScalar)a,s));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Polynomial){
            if(b instanceof NumericScalar){
                return ((Polynomial)a).forNumericElement(s->NumericScalar.floorAndNot(s,(NumericScalar)b));
            }else if(b instanceof Polynomial){
                return ((Polynomial) a).forEachElement((Polynomial) b,NumericScalar::floorAndNot);
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
        }
    }
    public static Scalar fAdd(Scalar a, Scalar b) {
        if(a instanceof NumericScalar){
            if(b instanceof NumericScalar){
                return NumericScalar.fAdd((NumericScalar) a,(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return ((Polynomial) b).forEachElement(n->NumericScalar.fAdd((NumericScalar) a,n)
                        ,n->NumericScalar.fAdd(Real.Int.ZERO,n));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Polynomial){
            if(b instanceof NumericScalar){
                return ((Polynomial) a).forEachElement(n->NumericScalar.fAdd(n,(NumericScalar) b)
                        ,n->NumericScalar.fAdd(n,Real.Int.ZERO));
            }else if(b instanceof Polynomial){
                return ((Polynomial) a).forEachElement((Polynomial) b,NumericScalar::fAdd);
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
        }
    }
    public static Scalar strConcat(Scalar a, Scalar b) {
        if(a instanceof NumericScalar){
            if(b instanceof NumericScalar){
                return NumericScalar.concat((NumericScalar) a,(NumericScalar) b);
            }else if(b instanceof Polynomial){
                return ((Polynomial) b).forNumericElement(s->NumericScalar.concat((NumericScalar)a,s));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Polynomial){
            if(b instanceof NumericScalar){
                return ((Polynomial)a).forNumericElement(s->NumericScalar.concat(s,(NumericScalar)b));
            }else if(b instanceof Polynomial){
                return ((Polynomial) a).forEachElement((Polynomial) b,NumericScalar::concat);
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
        }
    }

}
