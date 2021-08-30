package bsoelch.cnl.math;

import bsoelch.cnl.Constants;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class FiniteSet implements MathObject,Iterable<MathObject>{
    final static public FiniteSet EMPTY_SET =new FiniteSet(Collections.emptySet());
    final static public FiniteSet PAIR_KEY = new FiniteSet(new HashSet<>(Arrays.asList(Real.Int.ZERO,Real.Int.ONE)));


    public static FiniteSet intersect(FiniteSet a, FiniteSet b){
        HashSet<MathObject> newContents=new HashSet<>(a.contents);
        newContents.removeIf((o->!b.contains(o)));
        return from(newContents);
    }
    public static FiniteSet unite(FiniteSet a, FiniteSet b){
        HashSet<MathObject> newContents = new HashSet<>(a.contents);
        newContents.addAll(b.contents);
        return from(newContents);
    }
    public static FiniteSet symmetricDifference(FiniteSet a, FiniteSet b){
        HashSet<MathObject> newContents=new HashSet<>(a.contents);
        for(MathObject o: b.contents){
            if(a.contains(o)){
                newContents.remove(o);
            }else{
                newContents.add(o);
            }
        }
        return from(newContents);
    }
    public static FiniteSet difference(FiniteSet a, FiniteSet b){
        HashSet<MathObject> newContents=new HashSet<>(a.contents);
        newContents.removeIf(b::contains);
        return from(newContents);
    }
    public static FiniteSet product(FiniteSet a, FiniteSet b){
        HashSet<MathObject> newContents=new HashSet<>(a.size()
                *b.size());
        for(MathObject o1: a.contents){
            for(MathObject o2: b.contents){
                newContents.add(new Pair(o1,o2));
            }
        }
        return from(newContents);
    }

    @SuppressWarnings("unchecked")
    public static FiniteSet product(FiniteSet[] sets) {
        long size=1;
        for(FiniteSet s:sets){
            size*=s.size();
        }
        HashSet<MathObject> newContents=new HashSet<>((int)size);
        if(size>0){
            Iterator<MathObject>[] itrs=(Iterator<MathObject>[])new Iterator[sets.length];
            MathObject[] tuple=new MathObject[sets.length];
            for(int i=0;i<sets.length;i++){
                itrs[i]=sets[i].iterator();
                tuple[i]=itrs[i].next();
            }
            newContents.add(Tuple.create(tuple));
            int i=0;
            while (i<sets.length){
                if(itrs[i].hasNext()) {
                    tuple[i] = itrs[i].next();
                    newContents.add(Tuple.create(tuple));
                    i=0;
                }else{
                    itrs[i]=sets[i].iterator();
                    i++;
                }
            }
        }
        return from(newContents);
    }

    public static FiniteSet forEach(FiniteSet s, Function<MathObject, MathObject> f){
        HashSet<MathObject> newContents=new HashSet<>();
        for (MathObject o : s) {
            newContents.add(f.apply(o));
        }
        return from(newContents);
    }
    public static FiniteSet forEachPair(FiniteSet s1, FiniteSet s2, BiFunction<MathObject, MathObject, MathObject> f){
        HashSet<MathObject> newContents=new HashSet<>();
        for (MathObject o1 : s1) {
            for (MathObject o2 : s2) {
                newContents.add(f.apply(o1,o2));
            }
        }
        return from(newContents);
    }

    //TODO? range class
    public static FiniteSet range(Real.Int min, Real.Int max) {
        if(min.compareTo(max)>0)
            return EMPTY_SET;
        Real val=min;
        HashSet<MathObject> values=new HashSet<>((max.num().subtract(min.num())).intValueExact());
        while (val.compareTo(max)<=0){
            values.add(val);
            val=Real.add(val,Real.Int.ONE);
        }
        return from(values);
    }

    public static FiniteSet from(MathObject... objects){
        return from(new HashSet<>(Arrays.asList(objects)));
    }
    public static FiniteSet from(Set<MathObject> objects){
        if(objects.isEmpty()){
            return EMPTY_SET;
        }else if(PAIR_KEY.contents.equals(objects)){
            return PAIR_KEY;
        }else{
            return new FiniteSet(objects);
        }
    }

    final TreeSet<MathObject> contents;

    FiniteSet(Set<MathObject> contents) {
        if(contents.contains(null))
            throw new NullPointerException("null cannot be an element of a MathSet");
        this.contents = new TreeSet<>(MathObject::compare);
        this.contents.addAll(contents);
    }
    @NotNull
    @Override
    public Iterator<MathObject> iterator() {
        return contents.iterator();
    }

    public int size() {
        return contents.size();
    }

    public boolean contains(MathObject aMathObject) {
        return contents.contains(aMathObject);
    }

    @Override
    public NumbericValue numericValue() {
        return contents.isEmpty()?Real.Int.ZERO: contents.last().numericValue();
    }
    public MathObject asMapIfPossible(){
        TreeMap<MathObject,MathObject> objects=new TreeMap<>(MathObject::compare);
        for(MathObject e:contents){
            if (!(e instanceof Tuple) || ((Tuple) e).size() != 2 ||
                    objects.put(((Tuple) e).get(0), ((Tuple) e).get(1)) != null) {
                        return this;
            }
        }
        return FiniteMap.from(objects,1);
    }
    public  FiniteMap asMap(){
        int minLen=-1;
        TreeMap<MathObject,MathObject> objects=new TreeMap<>(MathObject::compare);
        for(MathObject e:contents){
            if (e instanceof Tuple) {
                if(((Tuple) e).size()>2){
                    MathObject[] keys=new MathObject[((Tuple) e).size()-1];
                    int i=0;
                    for(;i<keys.length;i++)
                        keys[i]=((Tuple) e).get(i);
                    objects.put(Tuple.create(keys),((Tuple) e).get(i));
                    minLen=Math.min(minLen,keys.length);
                }else{
                    objects.put(((Tuple) e).get(0),((Tuple) e).get(1));
                    minLen=1;
                }
            }
        }
        return FiniteMap.from(objects,minLen);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiniteSet)) return false;
        FiniteSet that = (FiniteSet) o;
        return Objects.equals(contents, that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contents);
    }


    @Override
    public String intsAsString() {
        return toString(MathObject::intsAsString);
    }

    @Override
    public String toString() {
        return toString(Constants.DEFAULT_BASE,true);
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
        for(MathObject o:contents){
            if(sb.length()>1)
                sb.append(", ");
            sb.append(objectToString.apply(o));
        }
        return sb.append('}').toString();
    }
}
