package bsoelch.cnl.math;

import java.util.Iterator;

public interface Tuple extends FiniteMap {
    int size();
    MathObject get(int i);

    @Override
    default Iterator<Pair> mapIterator() {
        return new Iterator<Pair>() {
            int i=0;
            @Override
            public boolean hasNext() {
                return i<size();
            }
            @Override
            public Pair next() {
                return new Pair(Real.from(i),get(i++));
            }
        };
    }

    static Tuple create(MathObject[] objects){
        for(MathObject o:objects) {
            if (o == null)
                throw new NullPointerException("Elements of Tuple must not be null");
        }
        if(objects.length==0){
            return EMPTY_MAP;
        }else if(objects.length==2){
            return new Pair(objects[0],objects[1]);
        }else{
            return new NTuple(objects);
        }
    }

}
