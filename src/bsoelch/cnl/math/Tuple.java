package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

public abstract class Tuple extends FiniteMap implements Iterable<MathObject>{
    /**value of length/size above which SparseTuple are used*/
    static final long SPARSE_FACTOR=3;

    Tuple(){}//package private constructor
    public static Tuple EMPTY_MAP=new Tuple() {
        @Override
        public int size() {
            return 0;
        }
        @Override
        public int length() {
            return 0;
        }
        @Override
        public MathObject get(int i) {
            return Real.Int.ZERO;
        }
        @Override
        public boolean isKey(MathObject key) {
            return false;
        }
        @Override
        public FiniteMap forEach(Function<MathObject, MathObject> f) {
            return this;
        }
        @Override
        public FiniteSet domain() {
            return FiniteSet.EMPTY_SET;
        }
        @Override
        public FiniteSet values() {
            return FiniteSet.EMPTY_SET;
        }
        @Override
        public MathObject evaluateAt(MathObject a) {
            return Real.Int.ZERO;
        }
        @Override
        public NumericValue numericValue() {
            return Real.Int.ZERO;
        }
        @Override
        public FiniteSet asSet() {
            return FiniteSet.EMPTY_SET;
        }
        @Override
        public boolean equals(Object obj) {
            return obj instanceof FiniteMap&&((FiniteMap) obj).size()==0;
        }
        @Override
        public int hashCode() {
            return 0;
        }
        @Override
        public String toString() {
            return "[]";
        }
        @Override
        public String toString(BigInteger base, boolean useSmallBase) {
            return toString();
        }
        @Override
        public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
            return toString();
        }
        @Override
        public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
            return toString();
        }
    };

    public static Tuple create(MathObject[] objects){
        int zeros=0;
        for(int i=0;i<objects.length;i++) {
            if (objects[i] == null){
                objects[i]=Real.Int.ZERO;
                zeros++;
            }else if (objects[i].equals(Real.Int.ZERO)){
                zeros++;
            }
        }
        //addLater convert to SparseTuples if necessary
        if(objects.length==0){
            return EMPTY_MAP;
        }else if(objects.length==2){
            return new Pair(objects[0],objects[1]);
        }else{
            return new NTuple(objects);
        }
    }

    public abstract int size();
    public abstract int length();
    public abstract MathObject get(int i);

    @Override
    public Iterator<Pair> mapIterator() {
        return new Iterator<Pair>() {
            int i=0;
            Pair nextEntry=nextEntry();

            private Pair nextEntry() {
                nextEntry=null;
                while (nextEntry==null&&i<length()) {
                    Pair p = new Pair(Real.from(i), get(i++));
                    if(!p.b.equals(Real.Int.ZERO)){
                        nextEntry=p;
                        break;
                    }
                }
                return nextEntry;
            }
            @Override
            public boolean hasNext() {
                return nextEntry!=null;
            }
            @Override
            public Pair next() {
                Pair ret=nextEntry;
                nextEntry=nextEntry();
                return ret;
            }
        };
    }
    @Override
    public @NotNull Iterator<MathObject> iterator() {
        return new Iterator<MathObject>() {
            int i=0;
            @Override
            public boolean hasNext() {
                return i<length();
            }
            @Override
            public MathObject next() {
                return get(i++);
            }
        };
    }
    public @NotNull Iterator<MathObject> sparseIterator() {
        return new Iterator<MathObject>() {
            final Iterator<MathObject> itr=iterator();
            MathObject next=nextNonZero();

            private MathObject nextNonZero() {
                next=Real.Int.ZERO;
                while(itr.hasNext()&&next.equals(Real.Int.ZERO)){
                    next=itr.next();
                }
                return next;
            }
            @Override
            public boolean hasNext() {
                return !next.equals(Real.Int.ZERO);
            }
            @Override
            public MathObject next() {
                MathObject tmp=next;
                next=nextNonZero();
                return tmp;
            }
        };
    }

    @Override
    public boolean isMatrix() {
        for (@NotNull Iterator<MathObject> it = sparseIterator(); it.hasNext(); ) {
            MathObject o = it.next();
            if(!(o instanceof FiniteMap&&((FiniteMap) o).isNumericTuple())){
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean isNumericTuple() {
        for (Iterator<MathObject> it = this.sparseIterator(); it.hasNext(); ) {
            MathObject o = it.next();
            if(!(o instanceof NumericValue)){
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean isTuple() {
        return true;
    }

    public boolean isFullTuple(){
        return true;
    }


    /**wrapper class that wraps a FiniteMap (which is assumed to be a SparseTuple) in the Tuple interface*/
    public static final class SparseTuple extends Tuple{
        final FiniteMap map;
        final BigInteger length;

        /**creates a sparse tuple of the supplied length, wrapping the given FiniteMap
         * this constructor should not be directly called, use {@link FiniteMap#createTuple(Map, BigInteger)} instead*/
        SparseTuple(FiniteMap map, BigInteger length){
            if(!map.isTuple())
                throw new IllegalArgumentException("Contents of SparseTuple have to represent a tuple");
            this.map=map;
            this.length =length;
        }

        @Override
        public boolean isFullTuple() {
            return false;
        }

        @Override
        public FiniteMap forEach(Function<MathObject, MathObject> f) {
            return map.forEach(f);
        }

        @Override
        public FiniteSet domain() {
            return map.domain();
        }

        @Override
        public FiniteSet values() {
            return FiniteSet.unite(map.values(),FiniteSet.from(Real.Int.ZERO));
        }

        @Override
        public MathObject evaluateAt(MathObject a) {
            return map.evaluateAt(a);
        }

        @Override
        public NumericValue numericValue() {
            return evaluateAt(Real.Int.ZERO).numericValue();
        }

        @Override
        public Iterator<Pair> mapIterator() {
                return new Iterator<Pair>() {
                final Iterator<Pair> mapItr=map.mapIterator();
                final Real.Int last=Real.from(length.subtract(BigInteger.ONE));
                boolean hasLast=true;
                @Override
                public boolean hasNext() {
                    return mapItr.hasNext()||hasLast;
                }

                @Override
                public Pair next() {
                    if(mapItr.hasNext()){
                        Pair p=mapItr.next();
                        if(p.a.equals(last))
                            hasLast=false;
                        return p;
                    }else if(hasLast){
                        hasLast=false;
                        return new Pair(last,Real.Int.ZERO);
                    }else{
                        throw new NoSuchElementException();
                    }
                }
            };
        }

        @Override
        public @NotNull Iterator<MathObject> sparseIterator() {
            return map.valueIterator();
        }

        //addLater? BigIntSize/keys
        @Override
        public int length() {
            return length.intValue();
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public MathObject get(int i) {
            return evaluateAt(Real.from(i));
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
            StringBuilder sb=new StringBuilder("{");
            for (Iterator<Pair> it = mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                if(sb.length()>1)
                    sb.append(", ");
                sb.append(objectToString.apply(e.a));
                sb.append(" -> ");
                sb.append(objectToString.apply(e.b));
            }
            return sb.append('}').toString();
        }

        //handling of equals and hash in wrapped map
        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }
        @Override
        public int hashCode() {
            return map.hashCode();
        }
    }
}
