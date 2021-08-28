package bsoelch.cnl.math;

import bsoelch.cnl.Constants;
import bsoelch.cnl.math.expression.ExpressionNode;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class Polynomial extends Scalar implements ExpressionNode {
    private static final Monomial EMPTY_MONOMIAL = new Monomial(new TreeMap<>());

    private static class Monomial implements Comparable<Monomial>{
        final TreeMap<Variable, BigInteger> variables;

        private Monomial(Map<Variable, BigInteger> variables) {
            this.variables = new TreeMap<>(variables);
            //remove zero powers
            this.variables.values().removeIf(e -> e.equals(BigInteger.ZERO));
        }

        Monomial divide(Monomial m){
            TreeMap<Variable,BigInteger> map=new TreeMap<>(variables);
            BigInteger prev;
            for(Map.Entry<Variable,BigInteger> e:m.variables.entrySet()){
                prev=map.get(e.getKey());
                if(prev!=null){
                    map.put(e.getKey(),prev.subtract(e.getValue()));
                }else{
                    map.put(e.getKey(),e.getValue().negate());
                }
            }
            return new Monomial(map);
        }
        Monomial multiply(Monomial m){
            TreeMap<Variable,BigInteger> v1=variables,v2=m.variables,map;
            if(v1.size()>v2.size()){
                map=new TreeMap<>(v1);
                v1=v2;
            }else{
                map=new TreeMap<>(v2);
            }
            BigInteger prev;
            for(Map.Entry<Variable,BigInteger> e:v1.entrySet()){
                prev=map.get(e.getKey());
                if(prev!=null){
                    map.put(e.getKey(),prev.add(e.getValue()));
                }else{
                    map.put(e.getKey(),e.getValue());
                }
            }
            return new Monomial(map);
        }

        @Override
        public String toString() {
            return toString(Constants.DEFAULT_BASE,true);
        }

        String toString(BigInteger base,boolean useSmallBase) {
            final StringBuilder sb = new StringBuilder();
            for(Map.Entry<Variable,BigInteger> e:variables.entrySet()){
                if(e.getValue().signum()!=0){
                    sb.append(e.getKey());
                    if(!e.getValue().equals(BigInteger.ONE)){
                        String str = Real.toString(base, e.getValue(), useSmallBase);
                        if(str.contains(":")){
                            sb.append("^(").append(str).append(")");
                        }else{
                            sb.append("^").append(str);
                        }
                    }
                }
            }
            return sb.toString();
        }

        @Override
        public int compareTo(@NotNull Polynomial.Monomial o) {
            Iterator<Map.Entry<Variable,BigInteger>> itr1=variables.entrySet().iterator(),
            itr2=o.variables.entrySet().iterator();
            Map.Entry<Variable,BigInteger> e1,e2;
            int c;
            while(itr1.hasNext()&&itr2.hasNext()){
                e1=itr1.next();
                e2=itr2.next();
                c=e1.getKey().compareTo(e2.getKey());
                if(c!=0)
                    return c;
                c=e1.getValue().compareTo(e2.getValue());
                if(c!=0)
                    return -c;
            }
            return itr1.hasNext()?-1:itr2.hasNext()?1:0;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Monomial)) return false;
            Monomial monomial = (Monomial) o;
            return Objects.equals(variables, monomial.variables);
        }
        @Override
        public int hashCode() {
            return Objects.hash(variables);
        }
    }

    public static Polynomial from(BigInteger varId,BigInteger power){
        return (Polynomial)from(Real.Int.ONE,varId,power);
    }
    public static Scalar from(NumericScalar factor,BigInteger varId,BigInteger power){
        Variable v=new Variable(varId);
        Monomial m=new Monomial(Collections.singletonMap(v,power));
        return from(Collections.singletonMap(m,factor));
    }

    private static Scalar from(Map<Monomial, NumericScalar> monomials1) {
        TreeMap<Monomial, NumericScalar> monomials=new TreeMap<>(monomials1);
        //Remove entries with prefix zero
        monomials.values().removeIf(n -> n.equals(Real.Int.ZERO));
        if(monomials.isEmpty()){
            return Real.Int.ZERO;
        }else if(monomials.size()==1&&monomials.containsKey(EMPTY_MONOMIAL)){
            return monomials.get(EMPTY_MONOMIAL);
        }
        return new Polynomial(monomials);
    }
    TreeMap<Monomial,NumericScalar> monomials;
    TreeMap<Variable,BigInteger> variables;
    private Polynomial(TreeMap<Monomial, NumericScalar> monomials){
        this.monomials=monomials;
        variables=new TreeMap<>();
        BigInteger prev;
        for(Monomial m:monomials.keySet()){
            for(Map.Entry<Variable, BigInteger> v:m.variables.entrySet()){
                prev=variables.get(v.getKey());
                if(prev==null){
                    variables.put(v.getKey(),v.getValue());
                }else{
                    variables.put(v.getKey(),prev.max(v.getValue()));
                }
            }
        }
    }
    @Override
    public Set<Variable> variables() {
        return Collections.unmodifiableSet(variables.keySet());
    }

    @Override
    public ExpressionNode evaluate(Map<Variable, ExpressionNode> replace) {
        //TODO Evaluate Polynomial
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public Scalar realPart() {
        return forEachElement(NumericScalar::realPart);
    }

    @Override
    public Scalar imaginaryPart() {
        return forEachElement(NumericScalar::imaginaryPart);
    }

    @Override
    public Scalar conjugate() {
        return forEachElement(NumericScalar::conjugate);
    }

    @Override
    public Real sqAbs() {
        Real res=Real.Int.ZERO;
        for(NumericScalar n: monomials.values()){
            res=Real.add(res,n.sqAbs());
        }
        return res;
    }

    @Override
    public Scalar negate() {
        return forEachElement(NumericScalar::negate);
    }
    @Override
    public Scalar invert() {
        return null;//TODO Polynomial Fraction
    }
    @Override
    public Scalar round(int mode) {
        return forEachElement(n->n.round(mode));
    }
    @Override
    public Scalar approx(Real precision) {
        return forEachElement(n->n.approx(precision));
    }

    public Scalar forNumericElement(Function<NumericScalar,NumericScalar> f){
        TreeMap<Monomial,NumericScalar> res=new TreeMap<>(monomials);
        res.put(EMPTY_MONOMIAL, f.apply(res.getOrDefault(EMPTY_MONOMIAL, Real.Int.ZERO)));
        return from(res);
    }
    public Scalar forEachElement(Function<NumericScalar,NumericScalar> f){
        TreeMap<Monomial,NumericScalar> res=new TreeMap<>(monomials);
        for(Map.Entry<Monomial,NumericScalar> e:res.entrySet()){
            e.setValue(f.apply(e.getValue()));
        }
        return from(res);
    }
    public Scalar forEachElement(Function<NumericScalar,NumericScalar> emptyMonomial,Function<NumericScalar,NumericScalar> nonemptyMonomial){
        TreeMap<Monomial,NumericScalar> res=new TreeMap<>(monomials);
        for(Map.Entry<Monomial,NumericScalar> e:res.entrySet()){
            if(e.getKey().equals(EMPTY_MONOMIAL)){
                e.setValue(emptyMonomial.apply(e.getValue()));
            }else {
                e.setValue(nonemptyMonomial.apply(e.getValue()));
            }
        }
        return from(res);
    }
    public Scalar forEachElement(Polynomial p, BiFunction<NumericScalar,NumericScalar,NumericScalar> f){
        TreeMap<Monomial,NumericScalar> res=new TreeMap<>();
        TreeSet<Monomial> entries=new TreeSet<>(monomials.keySet());
        entries.addAll(p.monomials.keySet());
        NumericScalar a,b;
        for(Monomial m:entries){
            a=monomials.get(m);
            b=p.monomials.get(m);
            if(a==null)
                a=Real.Int.ZERO;
            if(b==null)
                b=Real.Int.ZERO;
            res.put(m,f.apply(a,b));
        }
        return from(res);
    }

    public Scalar multiply(Polynomial p){
        HashMap<Monomial,NumericScalar> res=new HashMap<>(monomials.size()*p.monomials.size());
        NumericScalar prev;
        Monomial mult;
        for(Map.Entry<Monomial,NumericScalar> e:monomials.entrySet()){
            for(Map.Entry<Monomial,NumericScalar> f:p.monomials.entrySet()){
                mult=e.getKey().multiply(f.getKey());
                prev=res.get(mult);
                if(prev==null){
                    res.put(mult,NumericScalar.multiply(e.getValue(),f.getValue()));
                }else{
                    res.put(mult,NumericScalar.add(prev,NumericScalar.multiply(e.getValue(),f.getValue())));
                }
            }
        }
        return from(res);
    }


    private Monomial leadingCoefficient(Variable var) {
        if(!variables.containsKey(var)) {
            return EMPTY_MONOMIAL;
        }else{
            Monomial last=new Monomial(Collections.singletonMap(var,variables.get(var)));
            for (Monomial m : monomials.tailMap(last).keySet()) {
                if (m.variables.containsKey(var)) {
                    last = m;
                }else if(m.variables.firstKey().compareTo(var)>0){
                    break;//No further entries with var
                }
            }
            return last;
        }
    }
    private BigInteger subDeg(Variable var) {
        BigInteger subDeg=null;
        if (variables.containsKey(var)) {
            for (Monomial m : monomials.keySet()) {
                if (m.variables.containsKey(var)) {
                    subDeg=subDeg==null?m.variables.get(var) : subDeg.min(m.variables.get(var));
                }else{
                    subDeg=subDeg==null?BigInteger.ZERO: subDeg.min(BigInteger.ZERO);
                    if (m.variables.size()>0&&m.variables.firstKey().compareTo(var) > 0) {
                        break;//No further entries with var
                    }
                }
            }
        }
        return subDeg==null?BigInteger.ZERO:subDeg;
    }

    /**implementation of polynomial-division for the calculation of gcd*/
    private static Scalar[] divide(Polynomial a,Polynomial b,Variable var){
        Scalar q=Real.Int.ZERO,r=a,tmp;
        Monomial headB=b.leadingCoefficient(var),div,headR= ((Polynomial) r).leadingCoefficient(var);
        BigInteger degB= b.variables.get(var);
        BigInteger degR= ((Polynomial) r).variables.get(var);
        while(degR.compareTo(degB)>=0){
            div= headR.divide(headB);
            tmp = from(Collections.singletonMap(div, NumericScalar.divide(
                    (r instanceof Polynomial ? ((Polynomial) r).monomials.get(headR) : (NumericScalar) r), b.monomials.get(headB)
            )));
            q=Scalar.add(q,tmp);
            r=Scalar.subtract(r,Scalar.multiply(b, tmp));
            if(r instanceof Polynomial){
                headR=((Polynomial) r).leadingCoefficient(var);
                degR=headR.variables.get(var);
                if(degR==null)
                    degR=BigInteger.ZERO;
            }else{
                headR=EMPTY_MONOMIAL;
                degR=BigInteger.ZERO;
            }
        }
        return new Scalar[]{q,r};
    }

    //TODO mod

    //TODO test method
    public static Scalar[] reduce(Polynomial a, Polynomial b){
        Scalar a0=a,b0=b;
        TreeSet<Variable> commonVars=new TreeSet<>(a.variables.keySet());
        commonVars.retainAll(b.variables.keySet());
        BigInteger degA, degB;
        Variable var;
        if(commonVars.isEmpty()){
            return normalize(a,b);
        }else {
            var = commonVars.first();
            BigInteger subDeg=a.subDeg(var).min(b.subDeg(var));
            if(!subDeg.equals(BigInteger.ZERO)){//shared variable is common factor
                Monomial m=new Monomial(Collections.singletonMap(var,subDeg.negate()));
                Scalar factor = from(Collections.singletonMap(m, Real.Int.ONE));
                Scalar tmp1=a.multiply((Polynomial) factor);
                Scalar tmp2=b.multiply((Polynomial) factor);
                if(tmp1 instanceof Polynomial&&tmp2 instanceof Polynomial){
                    a=(Polynomial) tmp1;
                    b=(Polynomial) tmp2;
                }else{
                    return normalize(tmp1, tmp2);
                }
                if(!(a.variables.containsKey(var)&&b.variables.containsKey(var))){
                    return reduce(a,b);//run reduce for new variable
                }
            }
            degA = a.variables.get(var);
            degB = b.variables.get(var);
            if (degA.compareTo(degB) < 0) {
                Polynomial tmp = a;
                a = b;
                b = tmp;
            }
        }
        Scalar tmp=divide(a,b,var)[1];
        while (tmp instanceof Polynomial){
           a=b;
           b= (Polynomial) tmp;
           if(((Polynomial) tmp).variables.containsKey(var)){
               tmp=divide(a,b,var)[1];
           }else{
               break;
           }
       }
        if(tmp.equals(Real.Int.ZERO)){
           Scalar[] div=divide((Polynomial) a0,b,var);
           if(div[1].equals(Real.Int.ZERO)){
               a0=div[0];
           }else{
               throw new RuntimeException("division should yield remainder zero");
           }
           div=divide((Polynomial) b0,b,var);
           if(div[1].equals(Real.Int.ZERO)){
               b0=div[0];
           }else{
               throw new RuntimeException("division should yield remainder zero");
           }
        }
        return normalize(a0, b0);
    }

    @NotNull
    private static Scalar[] normalize(Scalar a, Scalar b) {
        if(b instanceof Polynomial){
            if(a instanceof Polynomial){
                HashMap<Variable,BigInteger> subDegs=new HashMap<>(((Polynomial) a).variables.size());
                for(Variable v:((Polynomial) a).variables.keySet()) {
                    subDegs.put(v, (((Polynomial) a).subDeg(v).min(((Polynomial) b).subDeg(v))).negate());
                }
                for(Variable v:((Polynomial) b).variables.keySet()) {
                    if(!subDegs.containsKey(v))
                        subDegs.put(v, (((Polynomial) a).subDeg(v).min(((Polynomial) b).subDeg(v))).negate());
                }
                Scalar div = from(Collections.singletonMap(new Monomial(subDegs),
                        ((Polynomial) b).monomials.firstEntry().getValue().invert()));
                a = Scalar.multiply(a,div);
                b = Scalar.multiply(b,div);
            }else{
                HashMap<Variable,BigInteger> subDegs=new HashMap<>(((Polynomial) b).variables.size());
                for(Variable v:((Polynomial) b).variables.keySet()){
                    BigInteger subDeg = ((Polynomial) b).subDeg(v).negate();
                    if(subDeg.compareTo(BigInteger.ZERO)<0){
                        subDegs.put(v, subDeg);
                    }
                }
                if(subDegs.size()>0) {
                    Polynomial div = (Polynomial) from(Collections.singletonMap(new Monomial(subDegs),
                            ((Polynomial) b).monomials.firstEntry().getValue().invert()));
                    b = ((Polynomial) b).multiply(div);
                    a = Scalar.multiply(a,div);
                }
            }
        }else{
            a=Scalar.divide(a,b);
            b=Real.Int.ONE;
            if(a instanceof Polynomial){
                HashMap<Variable,BigInteger> subDegs=new HashMap<>(((Polynomial) a).variables.size());
                for(Variable v:((Polynomial) a).variables.keySet()){
                    BigInteger subDeg = ((Polynomial) a).subDeg(v).negate();
                    if(subDeg.compareTo(BigInteger.ZERO)<0){
                        subDegs.put(v, subDeg);
                    }
                }
                if(subDegs.size()>0) {
                    Polynomial div = (Polynomial) from(Collections.singletonMap(new Monomial(subDegs), Real.Int.ONE));
                    a = ((Polynomial) a).multiply(div);
                    b=div;
                }
            }
        }
        return new Scalar[]{a, b};
    }

    @Override
    public boolean isInt() {
        for(NumericScalar n:monomials.values()){
            if(!n.isInt())
                return false;
        }
        return true;
    }

    @Override
    public boolean isReal() {
        for(NumericScalar n:monomials.values()){
            if(!n.isReal())
                return false;
        }
        return true;
    }

    @Override
    public NumericScalar numericValue() {
        NumericScalar n=monomials.get(EMPTY_MONOMIAL);
        return n==null?Real.Int.ZERO:n;
    }

    @Override
    public String intsAsString() {
        return toString(Constants.DEFAULT_BASE,true, MathObject::intsAsString);
    }

    @Override
    public String toString(BigInteger base, boolean useSmallBase) {
        return toString(base,useSmallBase, s->s.toString(base, true));
    }
    @Override
    public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
        return toString(base,useSmallBase,  s->s.toStringFixedPoint(base,precision, useSmallBase));
    }
    @Override
    public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
        return toString(base,useSmallBase,  s->s.toStringFloat(base,precision, useSmallBase));
    }
    private String toString(BigInteger base,boolean useSmallBase, Function<NumericScalar,String> entryToString) {
        final StringBuilder sb = new StringBuilder();
        boolean isOne;
        for(Map.Entry<Monomial, NumericScalar> e:monomials.entrySet()){
            if(!e.getValue().equals(Real.Int.ZERO)){
                if(e.getValue().equals(Real.Int.ONE)){
                    if(sb.length()>0)
                        sb.append('+');
                    isOne=true;
                }else if(e.getValue().equals(Real.Int.NEGATIVE_ONE)){
                    sb.append('-');
                    isOne=true;
                }else {
                    isOne=false;
                    String str = entryToString.apply(e.getValue());
                    if (str.startsWith("-") || str.startsWith("+")) {
                        sb.append(str.charAt(0));
                        str = str.substring(1);
                    } else if(sb.length()>0){
                        sb.append('+');
                    }
                    if (str.contains("+") || str.contains("-") || str.contains("|") || str.contains("/")) {
                        sb.append('(').append(str).append(')');
                    } else {
                        sb.append(str);
                    }
                }
                if(e.getKey().equals(EMPTY_MONOMIAL)){
                    if(isOne)
                        sb.append("1");
                }else{
                    sb.append(e.getKey().toString(base,useSmallBase));
                }
            }
        }
        return sb.toString();
    }

    @Override
    public int compareTo(@NotNull Scalar o) {
        if(o instanceof NumericScalar){
            if(monomials.isEmpty()){
                return Real.Int.ZERO.compareTo(o);
            }else {
                Map.Entry<Monomial, NumericScalar> firstMonomial = monomials.firstEntry();
                int c = -firstMonomial.getKey().compareTo(EMPTY_MONOMIAL);
                if (c != 0)
                    return c;
                return firstMonomial.getValue().compareTo(o);
            }
        }else if(o instanceof Polynomial){
            Iterator<Map.Entry<Monomial,NumericScalar>> itr1=monomials.entrySet().iterator();
            Iterator<Map.Entry<Monomial,NumericScalar>> itr2=((Polynomial) o).monomials.entrySet().iterator();
            while(itr1.hasNext()||itr2.hasNext()) {
                if (itr1.hasNext()) {
                    if (itr2.hasNext()) {
                        Map.Entry<Monomial,NumericScalar> next1 = itr1.next(),next2 = itr2.next();
                        int c=-next1.getKey().compareTo(next2.getKey());
                        if(c!=0)
                            return c;
                        c=next1.getValue().compareTo(next2.getValue());
                        if(c!=0)
                            return c;
                    } else {
                        return 1;
                    }
                } else {
                    return -1;
                }
            }
            return 0;
        }else{
            return -o.compareTo(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Scalar))
            return false;
        if(o instanceof NumericScalar){
            return variables.isEmpty()&&numericValue().equals(o);
        }else if(o instanceof Polynomial){
            return monomials.equals(((Polynomial) o).monomials);
        }else{
            return o.equals(this);
        }
    }

    @Override
    public int hashCode() {
        if(variables.isEmpty()){
            return numericValue().hashCode();
        }else{
            return monomials.hashCode();
        }
    }
}
