package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class FiniteSet extends MathObject implements Iterable<MathObject>{
    final static public FiniteSet EMPTY_SET =new FiniteSet(Collections.emptySet());


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

    //addLater? range class
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
    public static FiniteSet from(Set<? extends MathObject> objects){
        if(objects.isEmpty()){
            return EMPTY_SET;
        }else{
            return new FiniteSet(objects);
        }
    }

    final TreeSet<MathObject> contents;

    FiniteSet(Set<? extends MathObject> contents) {
        this.contents = new TreeSet<>(MathObject::compare);
        this.contents.addAll(contents);
    }
    @NotNull
    @Override
    public Iterator<MathObject> iterator() {
        return contents.iterator();
    }
    @NotNull
    public Iterator<MathObject> tailIterator(MathObject slice,boolean inclusive) {
        return contents.tailSet(slice,inclusive).iterator();
    }
    @NotNull
    public Iterator<MathObject> headIterator(MathObject slice,boolean inclusive) {
        return contents.headSet(slice,inclusive).iterator();
    }

    public int size() {
        return contents.size();
    }

    public boolean contains(MathObject aMathObject) {
        return contents.contains(aMathObject);
    }

    @Override
    public NumericValue numericValue() {
        return contents.isEmpty()?Real.Int.ZERO: contents.last().numericValue();
    }

    public  FiniteMap asMap(){
        TreeMap<MathObject,TreeSet<MathObject>> entries=new TreeMap<>(MathObject::compare);
        for(MathObject e:contents){
            if (e instanceof Tuple&&((Tuple) e).length()>0) {
                TreeSet<MathObject> prev=entries.get(((Tuple)e).get(0));
                if(prev==null){
                    prev=new TreeSet<>(MathObject::compare);
                }
                if(((Tuple) e).length()>2) {
                    MathObject[] subTuple =Arrays.copyOfRange(((Tuple) e).toArray(),1,((Tuple) e).length());
                    prev.add(Tuple.create(subTuple));
                }else{
                    prev.add(((Tuple) e).get(1));
                }
                entries.put(((Tuple)e).get(0),prev);
            }
        }
        //unwrap values
        TreeMap<MathObject,MathObject> objects=new TreeMap<>(MathObject::compare);
        for(Map.Entry<MathObject, TreeSet<MathObject>> e:entries.entrySet()){
            if(e.getValue().size()==1){//unwrap single Element values
                objects.put(e.getKey(),e.getValue().iterator().next());
            }else{
                objects.put(e.getKey(),FiniteSet.from(e.getValue()));
            }
        }
        return FiniteMap.from(objects);
    }

    public Tuple asTuple() {
        return Tuple.create(contents.toArray(new MathObject[0]));
    }

    public MathObject getFirst(){
        return contents.first();
    }
    public MathObject getLast(){
        return contents.first();
    }
    public FiniteSet removeFirst(){
        TreeSet<MathObject> newContents=new TreeSet<>(MathObject::compare);
        newContents.addAll(contents);
        newContents.remove(newContents.first());
        return from(newContents);
    }
    public FiniteSet removeLast(){
        TreeSet<MathObject> newContents=new TreeSet<>(MathObject::compare);
        newContents.addAll(contents);
        newContents.remove(newContents.last());
        return from(newContents);
    }
    public FiniteSet headSet(MathObject slice,boolean include){
        return from(contents.headSet(slice,include));
    }
    public FiniteSet tailSet(MathObject slice,boolean include){
        return from(contents.tailSet(slice,include));
    }
    public FiniteSet range(MathObject first,boolean includeFirst,MathObject last,boolean includeLast){
        if(MathObject.compare(last,first)<0)
            return EMPTY_SET;
        return from(contents.subSet(first,includeFirst,last,includeLast));
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
    public String asString() {
        StringBuilder sb=new StringBuilder();
        for (MathObject e:this) {
            sb.append(e.asString());
        }
        return sb.toString();
    }

    @Override
    public String intsAsString() {
        return toString(MathObject::intsAsString);
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
