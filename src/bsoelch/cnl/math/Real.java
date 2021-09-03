package bsoelch.cnl.math;

import bsoelch.cnl.Constants;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static bsoelch.cnl.Constants.*;

/**Real number, ist {@link Int} oder {@link Fraction}*/
public abstract class Real extends NumericValue {

    Real() {}//package private Constructor


    static final String DIGITS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "αβγδεζηθικλμνξοπρςστυφχψωΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ" +
            "абвгдежзиклмнопрстуфхцчшщыэюяАБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЫЭЮЯ" +//addLater remove similar characters
            "աբգդեզէըթժիլխծկհձղճմյնշոչպջռսվտրցւփքօֆևԱԲԳԴԵԶԷԸԹԺԻԼԽԾԿՀՁՂՃՄՅՆՇՈՉՊՋՌՍՎՏՐՑՒՓՔՕՖ";
    static final BigInteger MAX_BASE_I = BigInteger.valueOf(DIGITS.toLowerCase(Locale.ROOT).indexOf('i') + 1);
    static char digitSeparator = ':';
    static BigInteger digitBase = BigInteger.TEN;

    private static final double LOG2TO10 = (Math.log(2) / Math.log(digitBase.doubleValue()));

    public static Int from(long value){
        return from(BigInteger.valueOf(value));
    }
    public static Int from(BigInteger a) {
        if (a.equals(BigInteger.ZERO)) {
            return Int.ZERO;
        } else if (a.equals(BigInteger.ONE)) {
            return Int.ONE;
        } else if (a.equals(BIG_INT_NEG_ONE)) {
            return Int.NEGATIVE_ONE;
        }  else if (a.equals(BIG_INT_TWO)) {
            return Int.TWO;
        } else {
            return new Int(a);
        }
    }

    public static Real from(BigInteger a, BigInteger b) {
        if (b.signum() == 0)
            if(a.signum()==0){
                return Int.ZERO;// 0/0=0 necessary for consistent handling of empty map entries
            }else {
                throw new ArithmeticException("Division by zero");
            }
        if (b.signum() < 0) {
            a = a.negate();
            b = b.negate();
        }
        BigInteger g = a.gcd(b);
        a = a.divide(g);
        b = b.divide(g);
        if (b.equals(BigInteger.ONE)) {
            return from(a);
        } else {
            return new Fraction(a, b);
        }
    }

    static public Real add(Real a, Real b) {
        if (a.isInt() && b.isInt()) {
            return from(a.num().add(b.num()));
        } else {
            return from(a.num().multiply(b.den()).add(a.den().multiply(b.num())), a.den().multiply(b.den()));
        }
    }

    static public Real subtract(Real a, Real b) {
        if (a.isInt() && b.isInt()) {
            return from(a.num().subtract(b.num()));
        } else {
            return from(a.num().multiply(b.den()).subtract(a.den().multiply(b.num())), a.den().multiply(b.den()));
        }
    }

    static public Real multiply(Real a, Real b) {
        if (a.isInt() && b.isInt()) {
            return from(a.num().multiply(b.num()));
        } else {
            return from(a.num().multiply(b.num()), a.den().multiply(b.den()));
        }
    }

    static public Real divide(Real a, Real b) {
        return from(a.num().multiply(b.den()), a.den().multiply(b.num()));
    }

    static public Int concat(Real a, Real b) {
        return from(stringAsBigInt(bigIntAsString(a.round(MathObject.FLOOR).num())+bigIntAsString(b.round(MathObject.FLOOR).num())));
    }

    /**a&floor b*/
    public static Real floorAnd(Real a, Real b) {
        Real l = a.round(-1);
        Real f=subtract(a,l);
        b = b.round(-1);
        return add(from(l.num().and(b.num())),f);
    }
    /**a|floor b*/
    public static Real floorOr(Real a, Real b) {
        Real l = a.round(-1);
        Real f=subtract(a,l);
        b = b.round(-1);
        return add(from(l.num().or(b.num())),f);
    }
    /**a^floor b*/
    public static Real floorXor(Real a, Real b) {
        Real l = a.round(-1);
        Real f=subtract(a,l);
        b = b.round(-1);
        return add(from(l.num().xor(b.num())),f);
    }
    /**a&!floor b*/
    public static Real floorAndNot(Real a, Real b) {
        Real l = a.round(-1);
        Real f=subtract(a,l);
        b = b.round(-1);
        return add(from(l.num().andNot(b.num())),f);
    }

    public static Real fAdd(Real a, Real b) {
        return from(a.num().add(b.num()), a.den().add(b.den()));
    }

    @NotNull
    public static String toString(BigInteger base, BigInteger a, boolean useSmallBase) {
        if (base.compareTo(BIG_INT_TWO)<0)
            throw new IllegalArgumentException("base has to be at least 2");
        if (useSmallBase&&base.compareTo(Constants.MAX_INT)<0&&base.intValueExact() < DIGITS.length()) {
            if (base.intValueExact() < 36)
                return a.toString(base.intValueExact());
            ArrayList<BigInteger> digits = toBaseN(a, base);
            StringBuilder str = new StringBuilder(digits.size());
            for (int i = digits.size() - 1; i >= 0; i--) {
                str.append(DIGITS.charAt(digits.get(i).intValueExact()));
            }
            return str.toString();
        } else {
            return toStringBigBase(base, a);
        }
    }
    public static String toStringBigBase(BigInteger base, BigInteger a) {
        if (base.compareTo(BIG_INT_TWO)<0)
            throw new IllegalArgumentException("base has to be at least 2");
        ArrayList<BigInteger> digits = toBaseN(a, base);
        StringBuilder str = new StringBuilder(digits.size() * (1 + (int) Math.ceil(base.bitLength()* LOG2TO10)));
        for (int i = digits.size() - 1; i >= 0; i--) {
            if (str.length() > 0)
                str.append(digitSeparator);
            str.append(toString(digitBase, digits.get(i), true));
        }
        return str.toString();
    }

    static ArrayList<BigInteger> toBaseN(BigInteger value, BigInteger base) {
        if (base.compareTo(BIG_INT_TWO) < 0)
            throw new IllegalArgumentException("Base has to be at least 2");
        value = value.abs();
        ArrayList<BigInteger> ret = new ArrayList<>((int) Math.ceil(value.bitLength() * Math.log(2) / Math.log(base.doubleValue())));
        BigInteger[] tmp;
        while (value.signum() != 0) {
            tmp = value.divideAndRemainder(base);
            ret.add(tmp[1]);
            value = tmp[0];
        }
        return ret;
    }

    public static Real bigIntFromString(String value, BigInteger base) {
        if(value.isEmpty())
            return Int.ZERO;
        if(value.contains(".")){
            int index=value.indexOf(".");
            String tail = value.substring(index + 1);
            if(tail.contains(".")){
                throw new IllegalArgumentException("Unexpected .");
            }
            if(value.contains(""+ digitSeparator)||base.compareTo(MAX_INT)>=0||base.intValueExact()> DIGITS.length()){//big base
                int count=1;
                for(char c:tail.toCharArray())
                    if(c== digitSeparator)
                        count++;
                Real fromString=bigBaseFromString(value.substring(0,index)+ digitSeparator +tail,base);
                return divide(fromString, from(base.pow(count)));
            }else{
                Real fromString=bigIntFromString(value.substring(0,index)+ tail,base);
                return divide(fromString, from(base.pow(tail.length())));
            }
        }else if(value.contains(""+ digitSeparator)||base.compareTo(MAX_INT)>=0||base.intValueExact()> DIGITS.length()){
            return bigBaseFromString(value,base);
        }else{
            int smallBase=base.intValueExact();
            if(smallBase<=36){
                return from(new BigInteger(value,smallBase));
            }else{
                BigInteger res=BigInteger.ZERO;
                int index;
                for(char c:value.toCharArray()){
                    index = DIGITS.indexOf(c);
                    if(index==-1||index>=smallBase)
                        throw new IllegalArgumentException("Unexpected character:"+c);
                    res=res.multiply(base);
                    res=res.add(BigInteger.valueOf(index));
                }
                return from(res);
            }
        }
    }

    public static Real bigBaseFromString(String value, BigInteger base) {
        String[] parts=value.split(""+ digitSeparator,-1);//allow empty tails
        BigInteger res=BigInteger.ZERO;
        for(String s:parts){
            res=res.multiply(base);
            res=res.add(bigIntFromString(s, digitBase).num());
        }
        return from(res);
    }

    public abstract BigInteger num();

    public abstract BigInteger den();

    @Override
    public final Real realPart() {
        return this;
    }
    @Override
    public Real imaginaryPart() {
        return Int.ZERO;
    }
    @Override
    public Real conjugate() {
        return this;
    }

    @Override
    public Real sqAbs() {
        return multiply(this,this);
    }

    @Override
    public final boolean isReal() {
        return true;
    }

    public abstract Real negate();

    public abstract Real invert();

    abstract public Int round(int mode);

    public abstract Real approx(Real precision);

    public abstract Real abs();

    public abstract double doubleValue();

    public static final class Int extends Real {
        public static final Int ZERO = new Int(BigInteger.ZERO);
        public static final Int ONE = new Int(BigInteger.ONE);
        public static final Int NEGATIVE_ONE = new Int(BIG_INT_NEG_ONE) ;
        public static final Int TWO = new Int(BIG_INT_TWO) ;

        private final BigInteger value;

        private Int(BigInteger value) {
            this.value = value;
        }

        @Override
        public BigInteger num() {
            return value;
        }

        @Override
        public BigInteger den() {
            return BigInteger.ONE;
        }

        @Override
        public Real abs() {
            return from(value.abs());
        }

        @Override
        public double doubleValue() {
            return value.doubleValue();
        }

        @Override
        public Int round(int roundMode) {
            return this;
        }

        @Override
        public boolean isInt() {
            return true;
        }

        @Override
        public Real approx(Real precision) {
            return this;
        }

        @Override
        public Real negate() {
            return from(value.negate());
        }

        @Override
        public Real invert() {
            return from(BigInteger.ONE, value);
        }

        @Override
        public int compareTo(@NotNull NumericValue o) {
            if (o instanceof Int) {
                Int anInt = (Int) o;
                return value.compareTo(anInt.value);
            } else {
                return -o.compareTo(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NumericValue)) return false;
            if (o instanceof Int) {
                Int anInt = (Int) o;
                return value.equals(anInt.value);
            } else {
                return o.equals(this);
            }
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString(BigInteger base, boolean useSmallBase) {
            return toString(base,value, useSmallBase);
        }

        @Override
        public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
            return Real.toStringFixedPoint(value,BigInteger.ONE,base,precision, useSmallBase);
        }
        @Override
        public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
            return Real.toStringFloat(value,BigInteger.ONE,base,precision, useSmallBase);
        }

        public String asString(){
            return bigIntAsString(value.abs());
        }

        @Override
        public String intsAsString() {
            return value.signum()<0?"-'":"'"+asString()+"'";
        }
    }

    public static final class Fraction extends Real {
        private final BigInteger a, b;

        private Fraction(BigInteger a, BigInteger b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public BigInteger num() {
            return a;
        }

        @Override
        public BigInteger den() {
            return b;
        }

        @Override
        public Real abs() {
            return from(a.abs(),b.abs());
        }

        @Override
        public double doubleValue() {
            return a.doubleValue()/b.doubleValue();
        }

        @Override
        public Int round(int mode) {
            BigInteger[] tmp = a.divideAndRemainder(b);
            if (mode == FLOOR) {
                if (tmp[1].compareTo(BigInteger.ZERO) < 0)
                    tmp[0] = tmp[0].subtract(BigInteger.ONE);
            } else if (mode == ROUND) {
                if (tmp[1].abs().shiftLeft(1).compareTo(b) >= 0)
                    tmp[0] = tmp[0].add(BigInteger.valueOf(tmp[1].signum()));
            } else if (mode == CIEL) {
                if (tmp[1].compareTo(BigInteger.ZERO) > 0)
                    tmp[0] = tmp[0].add(BigInteger.ONE);
            }
            return from(tmp[0]);
        }

        public boolean isInt() {
            return a.signum()==0||b.equals(BigInteger.ONE);
        }

        @Override
        public Real negate() {
            return from(a.negate(), b);
        }

        @Override
        public Real invert() {
            return from(b, a);
        }

        public Real approx(Real delta) {
            delta=delta.abs();
            BigInteger[] tmp=a.divideAndRemainder(b);
            BigInteger intPart=tmp[0];
            if(tmp[1].signum()==0) {
                return from(intPart);
            }else {
                Real approx;
                if(tmp[1].signum()<0){
                    intPart=intPart.subtract(BigInteger.ONE);
                    tmp[1]=tmp[1].add(b);//ensure fractionalPart>0
                }
                BigInteger a1 = tmp[1], a2 = a1, b2 = b;
                Real fractionalPart=from(a1,b);
                ArrayDeque<BigInteger> coefficients = new ArrayDeque<>();
                do {
                    tmp = b2.divideAndRemainder(a2);
                    coefficients.addFirst(tmp[0]);
                    b2 = a2;
                    a2 = tmp[1];
                    approx=eval(coefficients,a2.shiftLeft(1).compareTo(b2)>0);
                } while (a2.signum() > 0 && (Real.subtract(approx,fractionalPart)
                        .abs().compareTo(delta)>0));
                return Real.add(from(intPart), approx);
            }
        }
        private static Real eval(ArrayDeque<BigInteger> coefficients,boolean roundUp){
            Real val=null;
            for(BigInteger i:coefficients){
                if(val==null){
                    if(roundUp){
                        val=from(BigInteger.ONE,i.add(BigInteger.ONE));
                    }else{
                        val=from(BigInteger.ONE,i);
                    }
                }else{
                        val=Real.add(val,from(i)).invert();
                }
            }
            return val;
        }

        @Override
        public int compareTo(@NotNull NumericValue o) {
            if (o instanceof Int) {
                return a.compareTo(b.multiply(((Int) o).value));
            } else if (o instanceof Fraction) {
                Fraction fraction = (Fraction) o;
                return a.multiply(fraction.b).compareTo(b.multiply(fraction.a));
            } else {
                return -o.compareTo(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NumericValue)) return false;
            if (o instanceof Int) {
                return Objects.equals(a, ((Int) o).num()) && Objects.equals(b, BigInteger.ONE);
            } else if (o instanceof Fraction) {
                Fraction fraction = (Fraction) o;
                return Objects.equals(a, fraction.a) && Objects.equals(b, fraction.b);
            } else {
                return o.equals(this);
            }
        }

        @Override
        public int hashCode() {
            //hashCode compatible with Int.hashCode
            return b.equals(BigInteger.ONE) ? a.hashCode() : Objects.hash(a, b);
        }

        @Override
        public String toString(BigInteger base, boolean useSmallBase) {
            if (b.equals(BigInteger.ONE)) {
                return (a.signum() == -1 ? "-" : "") +
                        Real.toString(base, a.abs(),useSmallBase);
            } else {
                return (a.signum() == -1 ? "-" : "") +
                        Real.toString(base, a.abs(), useSmallBase)
                        + "/" + Real.toString(base, b,useSmallBase);
            }
        }

        @Override
        public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
            return toStringFixedPoint(a,b,base, precision, useSmallBase);
        }

        public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
            return toStringFloat(a,b,base, precision, useSmallBase);
        }

        public String asString(){
            return bigIntAsString(a.abs().divide(b));
        }

        @Override
        public String intsAsString() {
            return from(a).asString()+"/"+from(b).asString();
        }
    }


    /**calculates the number of digits (after the decimal point)
     * needed to represent numbers in the given base up to the given precision */
    private static int calculateNumDigits(BigInteger base, Real precision) {
        //offset of 0.1 to allow more digits for numbers with nearly high enough precision
        //TODO dont use double
        return (int) (0.1 - Math.log(precision.doubleValue()) / Math.log(base.doubleValue()));
    }

    @NotNull
    static String toStringFixedPoint(BigInteger num, BigInteger den, BigInteger base, Real precision, boolean useSmallBase) {
        //calculate number of
        if (den.equals(BigInteger.ONE)) {
            return (num.signum() == -1 ? "-" : "") + Real.toString(base, num.abs(), useSmallBase);
        } else {
            if(precision.equals(Int.ZERO)){
                return fractionToPeriodicString(num.abs(), den, base, useSmallBase);
            }else{
                int numDigits= calculateNumDigits(base, precision);
                return (num.signum() == -1 ? "-" : "")
                        + fractionToApproxString(num.abs(), den, base, numDigits, useSmallBase);
            }
        }
    }
    @NotNull
    static String toStringFloat(BigInteger num, BigInteger den, BigInteger base, Real precision, boolean useSmallBase) {
        StringBuilder ret=new StringBuilder(num.signum() == -1 ? "-" : "");
        num = num.abs();
        int shift=0;
        if(num.compareTo(den)<0){
            while (num.compareTo(den)<0){
                num = num.multiply(base);
                shift--;
            }
        }else{
            BigInteger gcd;
            while (num.compareTo(den.multiply(base))>0){
                den = den.multiply(base);
                gcd= num.gcd(den);
                num = num.divide(gcd);
                den = den.divide(gcd);
                shift++;
            }
        }

        if(precision.equals(Int.ZERO)){
            ret.append(fractionToPeriodicString(num, den, base, useSmallBase));
        }else{
            int numDigits= calculateNumDigits(base, precision);
            //apply shift to precision
            numDigits+=shift;
            ret.append(fractionToApproxString(num, den, base, numDigits, useSmallBase));
        }
        if(shift!=0){
            ret.append("*").append(toString(base,base, useSmallBase)).append('^')
                    .append(toString(base,BigInteger.valueOf(shift), useSmallBase));
        }
        return ret.toString();
    }

    //addLater? handle negative precision
    private static String fractionToApproxString(BigInteger num, BigInteger den, BigInteger base, int numDigits,boolean useSmallBase) {
        StringBuilder ret = new StringBuilder(num.signum() < 0 ? "-" : "");
        num = num.abs();
        BigInteger[] div = num.divideAndRemainder(den);
        ret.append(Real.toString(base, div[0], useSmallBase));
        if(numDigits>0&&!div[1].equals(BigInteger.ZERO)) {
            ret.append(".");
            num = div[1].multiply(base);
            ret.ensureCapacity(numDigits);
            boolean smallBase = useSmallBase && base.compareTo(MAX_INT) < 0 && base.intValueExact() < DIGITS.length();
            for (int i = 0; i < numDigits; i++) {
                div = num.divideAndRemainder(den);
                if (smallBase) {
                    ret.append(DIGITS.charAt(div[0].intValueExact()));
                } else {
                    ret.append(div[0].intValueExact()).append(":");
                }
                if (div[1].equals(BigInteger.ZERO)) {
                    break;
                } else {
                    num = div[1].multiply(base);
                }
            }
            if (ret.charAt(ret.length() - 1) == ':')
                ret.setLength(ret.length() - 1);
        }
        return ret.toString();
    }

    private static String fractionToPeriodicString(BigInteger num, BigInteger den, BigInteger base,boolean useSmallBase){
        StringBuilder ret=new StringBuilder();
        if(num.signum()<0){
            ret.append('-');
            num=num.abs();
        }
        BigInteger[] tmp=num.divideAndRemainder(den);
        ret.append(toString(base,tmp[0], useSmallBase));
        if(tmp[1].signum()!=0) {
            ret.append('.');
            num = tmp[1].multiply(base);
            ret.ensureCapacity(den.intValue() & 0x7fffffff);
            HashMap<BigInteger, Integer> remainders = new HashMap<>(den.intValue() & 0x7fffffff);
            boolean periodic=true;
            boolean smallBase = useSmallBase && base.compareTo(MAX_INT) < 0 && base.intValueExact() < DIGITS.length();
            while(periodic){
                tmp=num.divideAndRemainder(den);
                if(tmp[1].equals(BigInteger.ZERO)){
                    periodic=false;
                }else{
                    if(!remainders.containsKey(num)){
                        remainders.put(num,ret.length());
                        num = tmp[1].multiply(base);
                    }else{
                        break;//end of periodic part
                    }
                }
                if(smallBase){
                    ret.append(DIGITS.charAt(tmp[0].intValueExact()));
                }else{
                    ret.append(tmp[0].intValueExact()).append(":");
                }
            }
            if(periodic) {
                int periodStart = remainders.get(num);
                ret.insert(periodStart, "[");
                if (ret.charAt(ret.length() - 1) == ':') {
                    ret.setCharAt(ret.length() - 1, ']');
                } else {
                    ret.append(']');
                }
            }else{
                if(ret.charAt(ret.length() - 1)==':')
                    ret.setLength(ret.length() - 1);
            }
        }
        return ret.toString();
    }

    @NotNull
    private static String bigIntAsString(BigInteger bigInt) {
        byte[] bytes=bigInt.toByteArray();
        int i0=0;
        if(bytes.length>1&&bytes[0]==0){
            i0=1;
        }
        byte[] reversedBytes=new byte[bytes.length-i0];
        for(int i=i0;i<bytes.length;i++){
            reversedBytes[bytes.length-(i+1)]=bytes[i];
        }
        return new String(reversedBytes,StandardCharsets.UTF_8);
    }
    @NotNull
    public static BigInteger stringAsBigInt(String str) {
        byte[] bytes=str.getBytes(StandardCharsets.UTF_8);
        byte[] reversedBytes=new byte[bytes.length+1];
        for(int i=0;i<bytes.length;i++){
            reversedBytes[bytes.length-i]=bytes[i];
        }
        return new BigInteger(reversedBytes);
    }

}
