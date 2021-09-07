package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**class representing a modifiable array of type T
 * for storage/access efficiency the array may potentially be saved as a Map omitting all null entries
 * the size of the array is automatically adjusted if a element outside of the array is set
 * */
class MutableTuple<T> implements Iterable<MutableTuple.TupleEntry<T>>{
    static class TupleEntry<T>{
        final int index;
        final T value;
        TupleEntry(int index, T value) {
            this.index = index;
            this.value = value;
        }
    }
    /**creates a MutableTuple wrapping the given Array
     * !!! the Tuple directly modifies the Elements of the Array !!!*/
    static <T> MutableTuple<T> from(T[] elements) {
        return new MutableTuple<>(new Array<>(elements));
    }
    /**creates a MutableTuple wrapping the given Map
     * !!! the Tuple directly modifies the Elements of the Map !!!*/
    static <T> MutableTuple<T> from(TreeMap<Integer, T> elements) {
        return new MutableTuple<>(new Sparse<>(elements));
    }

    private static abstract class TupleData<T> implements Iterable<MutableTuple.TupleEntry<T>>{
        abstract Iterator<TupleEntry<T>> head(int to,boolean include);
        abstract Iterator<TupleEntry<T>> tail(int from,boolean include);
        abstract int length();
        abstract TupleData<T> ensureLength(int length);
        abstract TupleData<T> set(int index,T value);
        abstract T get(int index);
        /**converts this Mutable tuple to a Tuple
         * @throws ClassCastException if the class of this Mutable tuple is no MathObject*/
        abstract Tuple toTuple();
    }

    private TupleData<T> tupleData;
    private MutableTuple(TupleData<T> tupleData){
        this.tupleData=tupleData;
    }

    @NotNull
    @Override
    public Iterator<TupleEntry<T>> iterator() {
        return tupleData.iterator();
    }
    Iterator<TupleEntry<T>> head(int to, boolean include) {
        return tupleData.head(to, include);
    }
    Iterator<TupleEntry<T>> tail(int from,boolean include) {
        return tupleData.tail(from, include);
    }
    int length() {
        return tupleData.length();
    }
    void ensureLength(int minLength) {
        tupleData=tupleData.ensureLength(minLength);
    }
    void set(int index,T value) {
        tupleData=tupleData.set(index, value);
    }
    T get(int index) {
        return tupleData.get(index);
    }
    /**converts this Mutable tuple to a Tuple
     * @throws ClassCastException if the class of this Mutable tuple is not a MathObject*/
    Tuple toTuple() {
        return tupleData.toTuple();
    }

    @Override
    public String toString() {
        return tupleData.toString();
    }

    //TODO? convert type in (set/ensureLength) if number of entries gets large/small
    private static class Array<T> extends TupleData<T>{
        final Object[] data;
        Array(T[] data) {
            this.data = data;
        }

        private Iterator<TupleEntry<T>> rangeIterator(int off,int to){
            return new Iterator<TupleEntry<T>>() {
                int i=off;
                @Override
                public boolean hasNext() {
                    return i<to;
                }
                @Override
                public TupleEntry<T> next() {
                    return new TupleEntry<>(i,get(i++));
                }
            };
        }
        @NotNull
        @Override
        public Iterator<TupleEntry<T>> iterator() {
            return rangeIterator(0,length());
        }
        @Override
        Iterator<TupleEntry<T>> head(int to, boolean include) {
            return rangeIterator(0,include?to+1:to);
        }
        @Override
        Iterator<TupleEntry<T>> tail(int from, boolean include) {
            return rangeIterator(include?from:from+1,length());
        }
        @Override
        int length() {
            return data.length;
        }

        @Override
        TupleData<T> ensureLength(int length) {
            if(length<=data.length)
                return this;
            @SuppressWarnings("unchecked")
            T[] copy= (T[])Arrays.copyOf(data,length);
            return new Array<>(copy);
        }

        @Override
        TupleData<T> set(int index, T value) {
            if(index<data.length) {
                data[index] = value;
                return this;
            }else {
                return ensureLength(index + 1).set(index, value);
            }
        }
        @Override
        @SuppressWarnings("unchecked")
        T get(int index) {
            return (T)data[index];
        }

        @Override
        public String toString() {
            return Arrays.toString(data);
        }

        @Override
        Tuple toTuple() {
            return Tuple.create((MathObject[]) data);
        }
    }
    private static class Sparse<T> extends TupleData<T> {
        final TreeMap<Integer,T> data;
        int length=0;

        Sparse(TreeMap<Integer, T> data) {
            this.data = data;
            for(int k:data.keySet()){
                length=Math.max(length,k);
            }
            length++;//length=(maxIndex+1)
        }

        private Iterator<TupleEntry<T>> convert(Iterator<Map.Entry<Integer,T>> mapItr){
            return new Iterator<TupleEntry<T>>() {
                @Override
                public boolean hasNext() {
                    return mapItr.hasNext();
                }

                @Override
                public TupleEntry<T> next() {
                    Map.Entry<Integer, T> e=mapItr.next();
                    return new TupleEntry<>(e.getKey(),e.getValue());
                }
            };
        }
        @Override
        public @NotNull Iterator<TupleEntry<T>> iterator() {
            return convert(data.entrySet().iterator());
        }
        @Override
        Iterator<TupleEntry<T>> head(int to, boolean include) {
            return convert(data.headMap(to,include).entrySet().iterator());
        }

        @Override
        Iterator<TupleEntry<T>> tail(int from, boolean include) {
            return convert(data.tailMap(from,include).entrySet().iterator());
        }

        @Override
        int length() {
            return length;
        }

        @Override
        TupleData<T> ensureLength(int length) {
            this.length=Math.max(this.length,length);
            return this;
        }

        @Override
        TupleData<T> set(int index, T value) {
            data.put(index,value);
            length=Math.max(length,index+1);
            return this;
        }
        @Override
        T get(int index) {
            return data.get(index);
        }

        @Override
        public String toString() {
            return data.toString();
        }

        @Override
        Tuple toTuple() {
            TreeMap<Real.Int,MathObject> mapData=new TreeMap<>();
            for(Map.Entry<Integer, T> e:data.entrySet()){
                mapData.put(Real.from(e.getKey()),(MathObject)e.getValue());
            }
            return FiniteMap.createTuple(mapData, BigInteger.valueOf(length));
        }
    }
}
