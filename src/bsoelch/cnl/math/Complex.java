package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;

/**Zahl in Q[i]*/
public final class Complex extends Scalar.NumericScalar {
    public static final Complex I = new Complex(Real.Int.ZERO, Real.Int.ONE);

    /** a/b + * c/d */
    private final Real re,im;

    public static NumericScalar from(Real re,Real im){
        if(im.equals(Real.Int.ZERO))
            return re;
        if(re.equals(Real.Int.ZERO)&&im.equals(Real.Int.ONE))
            return I;
        return new Complex(re, im);
    }

    private Complex(Real re, Real im) {
        this.re=re;
        this.im=im;
    }

    public NumericScalar negate() {
        return from(re.negate(),im.negate());
    }
    public NumericScalar conjugate() {
        return from(re,im.negate());
    }

    public NumericScalar invert() {
        return (NumericScalar)Scalar.multiply(conjugate(),sqAbs().invert());
    }

    public NumericScalar round(int mode) {
        return from(re.round(mode),im.round(mode));
    }

    public boolean isReal(){
        return im.equals(Real.Int.ZERO);
    }
    public boolean isInt(){
        return re.isInt()&&im.isInt();
    }

    public Real realPart() {
        return re;
    }

    public Real imaginaryPart() {
        return im;
    }

    public NumericScalar approx(Real precision) {
        return from(re.approx(precision),im.approx(precision));
    }


    public Real sqAbs() {
        return Real.add(Real.multiply(re,re), Real.multiply(im,im));
    }

    @Override
    public int compareTo(@NotNull Scalar s) {
        if(s instanceof Real){
            int c=re.compareTo(s);
            if(c!=0)
                return c;
            return im.compareTo(Real.Int.ZERO);
        }else if(s instanceof Complex){
            int t=re.compareTo(((Complex) s).re);
            if(t!=0)
                return t;
            return im.compareTo(((Complex) s).im);
        }else{
            return -s.compareTo(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumericScalar)) return false;
        if(o instanceof Real){
            return im.equals(Real.Int.ZERO)&&re.equals(o);
        }else if(o instanceof Complex){
            Complex that = (Complex) o;
            return Objects.equals(re, that.re) && Objects.equals(im, that.im);
        }else{
            return o.equals(this);
        }
    }
    @Override
    public int hashCode() {
        return im.equals(Real.Int.ZERO)?re.hashCode():Objects.hash(re,im);
    }


    private String toString(BigInteger base, Function<Real,String> realToString){
        String ret;
        if(re.equals(Real.Int.ZERO)){
            ret="";
        }else {
            ret=realToString.apply(re);
        }
        if(im.equals(Real.Int.ZERO)){
            if(ret.length()==0)
                return "0";
            else
                return ret;
        }else{
            if(base.compareTo(Real.MAX_BASE_I)<0) {
                return ret+(im.num().signum() != -1?"+":"")+realToString.apply(im)+"i";
            }else{
                return ret+(im.num().signum() == -1 ? "|-":"|")+ realToString.apply(im.abs());
            }
        }
    }
    public String toString(BigInteger base, boolean useSmallBase) {
        return toString(base,r->r.toString(base, useSmallBase));
    }
    public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase){
        return toString(base,r->r.toStringFixedPoint(base,precision, useSmallBase));
    }
    public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase){
        return toString(base,r->r.toStringFloat(base,precision, useSmallBase));
    }

    @Override
    public String asString() {
        return realPart().asString();
    }

    @Override
    public String intsAsString() {
        return re.intsAsString()+"|"+im.intsAsString();
    }
}
