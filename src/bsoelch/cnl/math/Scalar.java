package bsoelch.cnl.math;

import bsoelch.cnl.Constants;

import java.math.BigInteger;

/**{@link MathObject} representing a number has to be either a {@link Real} or a {@link Complex}*/
public abstract class Scalar implements MathObject,Comparable<Scalar> {
    Scalar(){}//package private constructor

    /**@return true iff all Real's in this Scalar are Integers*/
    public abstract boolean isInt();
    /**@return true iff all Scalar's in this Scalar are Integers*/
    public abstract boolean isReal();

    /**@return element-wise real-part of this Scalar*/
    public abstract Real realPart();
    /**@return element-wise imaginary-part of this Scalar*/
    public abstract Real imaginaryPart();
    /**@return element-wise complex conjugate of this Scalar*/
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



    static public Scalar add(Scalar a, Scalar b){
        if(a instanceof Real){
            if(b instanceof Real){
                return Real.add((Real)a,(Real)b);
            }else if(b instanceof Complex){
                return Complex.from(Real.add((Real)a,b.realPart()),b.imaginaryPart());
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Complex){
            if(b instanceof Real){
                return Complex.from(Real.add(a.realPart(),(Real)b),a.imaginaryPart());
            }else if(b instanceof Complex){
                return Complex.from(Real.add(a.realPart(),b.realPart()),Real.add(a.imaginaryPart(),b.imaginaryPart()));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+a.getClass());
        }
    }

    static public Scalar subtract(Scalar a, Scalar b){
        if(a instanceof Real){
            if(b instanceof Real){
                return Real.subtract((Real)a,(Real)b);
            }else if(b instanceof Complex){
                return Complex.from(Real.subtract((Real)a,b.realPart()),b.imaginaryPart().negate());
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Complex){
            if(b instanceof Real){
                return Complex.from(Real.subtract(a.realPart(),(Real)b),a.imaginaryPart());
            }else if(b instanceof Complex){
                return Complex.from(Real.subtract(a.realPart(),b.realPart()),Real.subtract(a.imaginaryPart(),b.imaginaryPart()));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+a.getClass());
        }
    }

    static public Scalar multiply(Scalar a, Scalar b){
        if(a instanceof Real){
            if(b instanceof Real){
                return Real.multiply((Real)a,(Real)b);
            }else if(b instanceof Complex){
                return Complex.from(Real.multiply((Real)a,b.realPart()),Real.multiply((Real)a,b.imaginaryPart()));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Complex){
            if(b instanceof Real){
                return Complex.from(Real.multiply(a.realPart(),(Real)b),Real.multiply(a.imaginaryPart(),(Real)b));
            }else if(b instanceof Complex){
                return Complex.from(Real.subtract(Real.multiply(a.realPart(),b.realPart()), Real.multiply(a.imaginaryPart(),b.imaginaryPart())),
                        Real.add(Real.multiply(a.realPart(),b.imaginaryPart()), Real.multiply(a.imaginaryPart(),b.realPart())));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+a.getClass());
        }
    }

    static public Scalar divide(Scalar a, Scalar b){
        if(a instanceof Real){
            if(b instanceof Real){
                return Real.divide((Real)a,(Real)b);
            }else if(b instanceof Complex){
                return multiply(a,b.invert());
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Complex){
            if(b instanceof Real){
                return Complex.from(Real.divide(a.realPart(),(Real)b),Real.divide(a.imaginaryPart(),(Real)b));
            }else if(b instanceof Complex){
                return multiply(a,b.invert());
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+a.getClass());
        }
    }

    /**a&floor b*/
    public static Scalar floorAnd(Scalar a, Scalar b) {
        if(a instanceof Real){
            if(b instanceof Real){
                return Real.floorAnd((Real)a,(Real)b);
            }else if(b instanceof Complex){
                return Real.floorAnd((Real)a,b.realPart());
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Complex){
            if(b instanceof Real){
                return Real.floorAnd((Real)b,a.realPart());
            }else if(b instanceof Complex){
                return Complex.from(Real.floorAnd(a.realPart(),b.realPart()),Real.floorAnd(a.imaginaryPart(),b.imaginaryPart()));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+a.getClass());
        }
    }

    /**a|floor b*/
    public static Scalar floorOr(Scalar a, Scalar b) {
        if(a instanceof Real){
            if(b instanceof Real){
                return Real.floorOr((Real)a,(Real)b);
            }else if(b instanceof Complex){
                return Complex.from(Real.floorOr((Real)a,b.realPart()),b.imaginaryPart());
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Complex){
            if(b instanceof Real){
                return Complex.from(Real.floorOr((Real)b,a.realPart()),a.imaginaryPart());
            }else if(b instanceof Complex){
                return Complex.from(Real.floorOr(a.realPart(),b.realPart()),Real.floorOr(a.imaginaryPart(),b.imaginaryPart()));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+a.getClass());
        }
    }
    /**a^floor b*/
    public static Scalar floorXor(Scalar a, Scalar b) {
        if(a instanceof Real){
            if(b instanceof Real){
                return Real.floorXor((Real)a,(Real)b);
            }else if(b instanceof Complex){
                return Complex.from(Real.floorXor((Real)a,b.realPart()),b.imaginaryPart());
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Complex){
            if(b instanceof Real){
                return Complex.from(Real.floorXor((Real)b,a.realPart()),a.imaginaryPart());
            }else if(b instanceof Complex){
                return Complex.from(Real.floorXor(a.realPart(),b.realPart()),Real.floorXor(a.imaginaryPart(),b.imaginaryPart()));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+a.getClass());
        }
    }
    /**a&!floor b*/
    public static Scalar floorAndNot(Scalar a, Scalar b) {
        if(a instanceof Real){
            if(b instanceof Real){
                return Real.floorAndNot((Real)a,(Real)b);
            }else if(b instanceof Complex){
                return Complex.from(Real.floorAndNot((Real)a,b.realPart()),b.imaginaryPart());
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Complex){
            if(b instanceof Real){
                return Complex.from(Real.floorAndNot((Real)b,a.realPart()),a.imaginaryPart());
            }else if(b instanceof Complex){
                return Complex.from(Real.floorAndNot(a.realPart(),b.realPart()),Real.floorAndNot(a.imaginaryPart(),b.imaginaryPart()));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+a.getClass());
        }
    }
    public static Scalar fAdd(Scalar a, Scalar b) {
        if(a instanceof Real){
            if(b instanceof Real){
                return Real.fAdd((Real)a,(Real)b);
            }else if(b instanceof Complex){
                return Complex.from(Real.fAdd(((Real)a),b.realPart()),Real.fAdd(Real.Int.ZERO,b.imaginaryPart()));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Complex){
            if(b instanceof Real){
                return Complex.from(Real.fAdd(a.realPart(),((Real)b)),Real.fAdd(a.imaginaryPart(),Real.Int.ZERO));
            }else if(b instanceof Complex){
                return Complex.from(Real.fAdd(a.realPart(),b.realPart()),Real.fAdd(a.imaginaryPart(),b.imaginaryPart()));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+a.getClass());
        }
    }
    public static Scalar strConcat(Scalar a, Scalar b) {
        if(a instanceof Real){
            if(b instanceof Real){
                return Real.concat((Real)a,(Real)b);
            }else if(b instanceof Complex){
                return Complex.from(Real.concat((Real)a,b.realPart()),b.imaginaryPart());
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else if(a instanceof Complex){
            if(b instanceof Real){
                return Complex.from(Real.concat(a.realPart(),(Real)b),a.imaginaryPart());
            }else if(b instanceof Complex){
                return Complex.from(Real.concat(a.realPart(),b.realPart()),Real.concat(a.imaginaryPart(),b.imaginaryPart()));
            }else{
                throw new IllegalArgumentException("Unexpected Scalar-class:"+b.getClass());
            }
        }else{
            throw new IllegalArgumentException("Unexpected Scalar-class:"+a.getClass());
        }
    }

    public static Scalar min(Scalar a,Scalar b) {
        return a.compareTo(b)<=0?a:b;
    }
    public static Scalar max(Scalar a, Scalar b) {
        return a.compareTo(b)>=0?a:b;
    }


    public static Scalar mod(Scalar l,Scalar r){
        return subtract(l, multiply(r, divide(l,r).round(FLOOR)));
    }
    public static Scalar pow(Scalar l,Scalar r){
        return powBigInt(l,r.round(-1).realPart().num());
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

}
