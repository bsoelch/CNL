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
    /**creates a new empty Mutable tuple with the given length*/
    @SuppressWarnings("unchecked")
    static <T> MutableTuple<T> from(int length) {
        if(length<10){
            return new MutableTuple<>(new Array<>((T[])new Object[10],length, 0));
        }else{
            return new MutableTuple<>(new Sparse<>(new TreeMap<>(),length));
        }
    }
    /**creates a MutableTuple wrapping the given Array
     * !!! the Tuple directly modifies the Elements of the Array !!!*/
    static <T> MutableTuple<T> from(T[] elements,int size) {
        return new MutableTuple<>(new Array<>(elements, elements.length,size));
    }
    /**creates a MutableTuple wrapping the given Map
     * !!! the Tuple directly modifies the Elements of the Map !!!*/
    static <T> MutableTuple<T> from(TreeMap<Integer, T> elements,int length) {
        return new MutableTuple<>(new Sparse<>(elements,length));
    }


    private static abstract class TupleData<T> implements Iterable<MutableTuple.TupleEntry<T>>{
        abstract Iterator<TupleEntry<T>> head(int to);
        abstract Iterator<TupleEntry<T>> tail(int from);
        abstract int length();
        abstract TupleData<T> ensureFreeSpace(int length, boolean setLength);
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
    /**iterator through the leading elements of this Tuple ending at to (excluded)*/
    Iterator<TupleEntry<T>> head(int to) {
        return tupleData.head(to);
    }
    /**iterator through the tailing elements of this Tuple starting at from (included)*/
    Iterator<TupleEntry<T>> tail(int from) {
        return tupleData.tail(from);
    }
    int length() {
        return tupleData.length();
    }
    void ensureLength(int minLength) {
        tupleData=tupleData.ensureFreeSpace(minLength, true);
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

    private static class Array<T> extends TupleData<T>{
        final Object[] data;
        int length,size;
        Array(T[] data, int length, int size) {
            this.data = data;
            this.length=length;
            this.size=size;
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
            return rangeIterator(0,length);
        }
        @Override
        Iterator<TupleEntry<T>> head(int to) {
            return rangeIterator(0,Math.min(to,length));
        }
        @Override
        Iterator<TupleEntry<T>> tail(int from) {
            return rangeIterator(Math.max(0, from),length);
        }
        @Override
        int length() {
            return length;
        }

        @Override
        @SuppressWarnings("unchecked")
        TupleData<T> ensureFreeSpace(int length, boolean setLength) {
            if(setLength)
                this.length=length;
            if(length<=data.length)
                return this;
            if(length>2*Tuple.SPARSE_FACTOR*size){
                return asMap();
            }else {
                T[] copy = (T[]) Arrays.copyOf(data, length);
                return new Array<>(copy, this.length, size);
            }
        }

        @Override
        TupleData<T> set(int index, T value) {
            length=Math.max(length,index+1);
            if(length<data.length) {
                if(data[index]==null)
                    size++;
                data[index] = value;
                return this;
            }else {
                if(length>2*Tuple.SPARSE_FACTOR*size){
                    return asMap().set(index, value);
                }else {
                    return ensureFreeSpace(length + 10, false).set(index, value);
                }
            }
        }
        @SuppressWarnings("unchecked")
        @NotNull
        private Sparse<T> asMap() {
            TreeMap<Integer,T> map=new TreeMap<>();
            for(int i=0;i<data.length;i++){
                if(data[i]!=null){
                    map.put(i,(T)data[i]);
                }
            }
            return new Sparse<>(map, length);
        }

        @Override
        @SuppressWarnings("unchecked")
        T get(int index) {
            if(index<data.length) {
                return (T)data[index];
            }else {
                return null;
            }
        }

        @Override
        public String toString() {
            return Arrays.toString(Arrays.copyOf(data,length));
        }

        @Override
        Tuple toTuple() {
            return Tuple.create(Arrays.copyOf(data,length,MathObject[].class));
        }
    }
    private static class Sparse<T> extends TupleData<T> {
        final TreeMap<Integer,T> data;
        int length;
        Sparse(TreeMap<Integer, T> data,int length) {
            this.data = data;
            this.length=length;
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
        Iterator<TupleEntry<T>> head(int to) {
            return convert(data.headMap(to, false).entrySet().iterator());
        }

        @Override
        Iterator<TupleEntry<T>> tail(int from) {
            return convert(data.tailMap(from, true).entrySet().iterator());
        }

        @Override
        int length() {
            return length;
        }

        @Override
        TupleData<T> ensureFreeSpace(int length, boolean setLength) {
            if(setLength) {
                this.length = Math.max(this.length, length);
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        TupleData<T> set(int index, T value) {
            data.put(index,value);
            length=Math.max(length,index+1);
            if(length<data.size()*Tuple.SPARSE_FACTOR){
                Object[] array=new Object[length];
                for(Map.Entry<Integer, T> e:data.entrySet()){
                    array[e.getKey()]=e.getValue();
                }
                return new Array<>((T[])array,length,data.size());
            }
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
            return FiniteMap.createTuple(mapData, length);
        }
    }
}
