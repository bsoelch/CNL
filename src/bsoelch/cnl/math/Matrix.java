package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class Matrix extends MathObject implements Iterable<NumericValue> {
    static class MatrixEntry{
        final int x,y;
        final NumericValue value;

        MatrixEntry(int x, int y, NumericValue value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }

    public static Matrix identityMatrix(int n){
        return diagonalMatrix(n, Real.Int.ONE);
    }

    public static Matrix diagonalMatrix(int n, NumericValue value) {
        return fromMutableTuple(diagonal(n, value));
    }
    @NotNull
    private static MutableTuple<MutableTuple<NumericValue>> diagonal(int n, NumericValue value) {
        if(n <=0)
            throw new IllegalArgumentException("n has to be >0");
        TreeMap<Integer,MutableTuple<NumericValue>> data=new TreeMap<>();
        for(int i = 0; i< n; i++)
            data.put(i,MutableTuple.from(new TreeMap<>(Collections.singletonMap(i, value)),n));
        return MutableTuple.from(data,n);
    }

    /**Converts the given MathObject to a Matrix*/
    public static Matrix asMatrix(MathObject o){
        if(o instanceof LambdaExpression)
            o=o.asMathObject();
        //no else here
        if(o instanceof Matrix){
            return (Matrix) o;
        }else if(o instanceof NumericValue){
            return new Matrix(Tuple.create(new MathObject[]{Tuple.create(new MathObject[]{o})}),1,1);
        }else if(o instanceof FiniteSet){
            Tuple[] rows=new Tuple[((FiniteSet) o).size()];
            int i=0,maxLength=0;
            for(MathObject row:(FiniteSet)o){
                rows[i]= rowFrom(row);
                maxLength=Math.max(maxLength,rows[i++].length());
            }
            return new Matrix(Tuple.create(rows),rows.length,maxLength);
        }else if(o instanceof Tuple&&((Tuple) o).isFullTuple()){
            Tuple[] rows=new Tuple[((Tuple) o).length()];
            int maxLength=0;
            for(int i=0;i<((Tuple) o).length();i++){
                rows[i]= rowFrom(((Tuple) o).get(i));
                maxLength=Math.max(maxLength,rows[i].length());
            }
            return new Matrix(Tuple.create(rows),rows.length,maxLength);
        }else if(o instanceof FiniteMap){
            TreeMap<MathObject,MathObject> rows = new TreeMap<>(MathObject::compare);
            int maxLength=0;
            int maxRow=0;
            for (Iterator<Pair> it = ((FiniteMap) o).mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                Tuple row = rowFrom(e.b);
                Real.Int rowNumber = e.a.numericValue().realPart().round(ROUND);
                maxRow=Math.max(maxRow,rowNumber.num().intValueExact());
                rows.put(rowNumber, row);
                maxLength=Math.max(maxLength,row.length());
            }
            return new Matrix(FiniteMap.from(rows),maxRow+1,maxLength);
        }else{
            throw new RuntimeException("Unexpected MathObject class:"+o.getClass());
        }
    }
    /**converts the given MathObject to a Tuple containing only NumericValues*/
    static Tuple rowFrom(MathObject row) {
        if(row instanceof NumericValue){
            return Tuple.create(new NumericValue[]{(NumericValue) row});
        }else if(row instanceof Matrix){
            ArrayList<NumericValue> values = new ArrayList<>(((Matrix) row).size());
            for (@NotNull Iterator<NumericValue> it = ((Matrix) row).sparseIterator(); it.hasNext(); ) {
                NumericValue nv = it.next();
                values.add(nv);
            }
            return Tuple.create(values.toArray(new NumericValue[0]));
        }else if(row instanceof FiniteSet){
            Tuple values= Tuple.EMPTY_MAP;
            for(MathObject o:(FiniteSet)row){
                values=MathObject.tupleConcat(values,rowFrom(o));
            }
            return values;
        }else if(row instanceof Tuple){
            Tuple values= Tuple.EMPTY_MAP;
            for(MathObject o:(Tuple)row){
                values=MathObject.tupleConcat(values,rowFrom(o));
            }
            return values;
        }else if(row instanceof FiniteMap){
            TreeMap<MathObject,MathObject> values=new TreeMap<>(MathObject::compare);
            Real.Int offset=Real.Int.ZERO,lastKey=Real.Int.ZERO;
            for (Iterator<Pair> it = ((FiniteMap) row).mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                if(!(e.b instanceof NumericValue)){
                    Tuple insert=rowFrom(e.b);
                    for (Iterator<Pair> iter = insert.mapIterator(); iter.hasNext(); ) {
                        Pair p = iter.next();
                        lastKey =(Real.Int)Real.add(offset, p.a.numericValue().realPart().round(ROUND));
                    }
                    offset= (Real.Int)Real.add(offset,Real.from(insert.length()-1));
                    lastKey=(Real.Int)Real.add(lastKey,Real.from(insert.length()));
                }else {
                    lastKey=(Real.Int)Real.add(offset, e.a.numericValue().realPart().round(ROUND));
                    values.put(lastKey, e.b.numericValue());
                }
            }
            return FiniteMap.createTuple(values,lastKey.num());
        }else{
            throw new RuntimeException("Unexpected MathObject class:"+row.getClass());
        }
    }

    static Matrix fromMutableTuple(MutableTuple<MutableTuple<NumericValue>> ret) {
        TreeMap<MathObject,MathObject> rows=new TreeMap<>(MathObject::compare);
        int maxLength=0;
        for(MutableTuple.TupleEntry<MutableTuple<NumericValue>> row:ret){
            rows.put(Real.from(row.index),row.value.toTuple());
            maxLength=Math.max(maxLength,row.value.length());
        }
        return new Matrix(FiniteMap.from(rows),ret.length(),maxLength);
    }

    public static Matrix forEach(Matrix a,Matrix b, BiFunction<NumericValue, NumericValue, NumericValue> f){
        MutableTuple<MutableTuple<NumericValue>> dataA=a.toMutableTuple(),dataB=b.toMutableTuple();
        if(f.apply(Real.Int.ZERO,Real.Int.ZERO).equals(Real.Int.ZERO)){
            dataA.ensureLength(dataB.length());
            Iterator<MutableTuple.TupleEntry<MutableTuple<NumericValue>>> itrA=dataA.iterator(),itrB=dataB.iterator();
            MutableTuple.TupleEntry<MutableTuple<NumericValue>> rowA,rowB;
            if(itrA.hasNext()){
                rowA=itrA.next();
            }else{
                return b.applyToAll(e->f.apply(Real.Int.ZERO,e));
            }
            if(itrB.hasNext()){
                rowB=itrB.next();
            }else{
                return a.applyToAll(e->f.apply(e,Real.Int.ZERO));
            }
            while(rowA!=null||rowB!=null){
                if(rowB==null||(rowA!=null&&rowB.index>rowA.index)){
                    for(MutableTuple.TupleEntry<NumericValue> e:rowA.value){
                        rowA.value.set(e.index,f.apply(e.value,Real.Int.ZERO));
                    }
                    rowA=itrA.hasNext()?itrA.next():null;
                }else if(rowA == null || rowA.index > rowB.index){
                    for(MutableTuple.TupleEntry<NumericValue> e:rowB.value){
                        rowB.value.set(e.index,f.apply(Real.Int.ZERO,e.value));
                    }
                    dataA.set(rowB.index, rowB.value);
                    rowB=itrB.hasNext()?itrB.next():null;
                }else{
                    for(MutableTuple.TupleEntry<NumericValue> e:rowA.value){
                        rowA.value.set(e.index,f.apply(e.value,rowB.value.get(e.index)));
                    }
                    rowA=itrA.hasNext()?itrA.next():null;
                    rowB=itrB.hasNext()?itrB.next():null;
                }
            }
        }else{
            int[] dimA=a.dimensions(),dimB=b.dimensions();
            int w = Math.max(dimA[0], dimB[0]);
            int h = Math.max(dimA[1], dimB[1]);
            for(int i=0;i<w;i++){
                MutableTuple<NumericValue> rowA=dataA.get(i),rowB=dataB.get(i);
                rowA.ensureLength(rowB.length());
                for(int j=0;j<h;j++){
                    rowA.set(j,f.apply(rowA.get(j),rowB.get(j)));
                }
            }
        }
        return fromMutableTuple(dataA);
    }

    public static Matrix matrixMultiply(Matrix a, Matrix b) {
        MutableTuple<MutableTuple<NumericValue>> res=MutableTuple.from(a.rows);
        boolean hasRow =false;//true if there is a nonempty row in the result (for ensuring correct size of result)
        for (Iterator<Pair> it = a.rowIterator(); it.hasNext(); ) {//rowA
            Pair rA = it.next();
            Iterator<Pair> colA = ((FiniteMap) rA.b).mapIterator();
            Pair eA=colA.hasNext()?colA.next():null;
            Iterator<Pair> rowB = b.rowIterator();
            Pair rB=rowB.hasNext()?rowB.next():null;
            while (eA!=null||rB!=null) {//colA/rowB
                if(rB==null||(!(rB.b instanceof FiniteMap))||(eA!=null&&MathObject.compare(eA.a,rB.a)<0)){
                    //no entry in row corresponding to entry in A
                    eA=colA.hasNext()?colA.next():null;
                }else if(eA==null||(eA.b.equals(Real.Int.ZERO))||MathObject.compare(eA.a,rB.a)>0){
                    //no entry in A corresponding to row
                    rB=rowB.hasNext()?rowB.next():null;
                }else{
                    int x = ((Real.Int) rA.a).num().intValueExact();
                    for (Iterator<Pair> iter = ((FiniteMap) rB.b).mapIterator(); iter.hasNext(); ) {//colB
                        Pair eB = iter.next();
                        int y = ((Real.Int) eB.a).num().intValueExact();
                        NumericValue v=NumericValue.multiply((NumericValue)eA.b,(NumericValue)eB.b);
                        if(!v.equals(Real.Int.ZERO)){
                            MutableTuple<NumericValue> row=res.get(x);
                            if(row==null){
                                row=MutableTuple.from(b.columns);
                                res.set(x,row);
                                hasRow=true;
                            }
                            NumericValue prev=row.get(y);
                            if(prev!=null){
                                v=NumericValue.add(v,prev);
                            }
                            row.set(y,v);
                        }
                    }
                    eA=colA.hasNext()?colA.next():null;
                    rB=rowB.hasNext()?rowB.next():null;
                }
            }
        }
        if(!hasRow){//add empty row with correct length
            MutableTuple<NumericValue> row=MutableTuple.from(b.columns);
            res.set(0,row);
        }
        return fromMutableTuple(res);
    }

    static void gaussianAlgorithm(MutableTuple<MutableTuple<NumericValue>> target,
                                  @Nullable MutableTuple<MutableTuple<NumericValue>> applicant, boolean total) {
        int y=0,k0;
        for (MutableTuple.TupleEntry<MutableTuple<NumericValue>> row : target) {
            k0 = -1;
            for (Iterator<MutableTuple.TupleEntry<NumericValue>> it = row.value.tail(y); it.hasNext(); ) {
                MutableTuple.TupleEntry<NumericValue> e = it.next();
                if (e.value!=null&&!Real.Int.ZERO.equals(e.value)) {
                    k0 = e.index;
                    break;
                }
            }
            if (k0 != -1) {
                if (k0 != y)
                    addLine(target, applicant, k0, row.value.get(k0).invert(), y);
                for (Iterator<MutableTuple.TupleEntry<NumericValue>> it = row.value.tail(y+1); it.hasNext(); ) {
                    MutableTuple.TupleEntry<NumericValue> e = it.next();
                    if (e.value!=null&&!Real.Int.ZERO.equals(e.value)) {
                        addLine(target, applicant, y, NumericValue.divide(e.value,row.value.get(y)).negate(), e.index);
                    }
                }
                if (total) {
                    for (Iterator<MutableTuple.TupleEntry<NumericValue>> it = row.value.head(y); it.hasNext(); ) {
                        MutableTuple.TupleEntry<NumericValue> e = it.next();
                        if (e.value!=null&&!Real.Int.ZERO.equals(e.value)) {
                            addLine(target, applicant, y, NumericValue.divide(e.value,row.value.get(y)).negate(), e.index);
                        }
                    }
                }
                y++;
            }
        }
    }
    /**adds f times line a to line b*/
    private static void addLine(MutableTuple<MutableTuple<NumericValue>> target,
                                @Nullable MutableTuple<MutableTuple<NumericValue>> applicant, int a, NumericValue f, int b) {
        for(MutableTuple.TupleEntry<MutableTuple<NumericValue>> e:target){
            NumericValue eA = e.value.get(a),eB = e.value.get(b);
            e.value.set(b,NumericValue.add(eA ==null? Real.Int.ZERO: NumericValue.multiply(eA,f),
                    eB ==null? Real.Int.ZERO: eB));
        }
        if(applicant!=null){
            for(MutableTuple.TupleEntry<MutableTuple<NumericValue>> e:applicant){
                NumericValue eA = e.value.get(a),eB = e.value.get(b);
                e.value.set(b,NumericValue.add(eA ==null? Real.Int.ZERO: NumericValue.multiply(eA,f),
                        eB ==null? Real.Int.ZERO: eB));
            }
        }
    }


    final FiniteMap data;

    final int rows, columns;

    Matrix(FiniteMap data,int rows,int columns){
        this.data=data;
        this.rows=rows;
        this.columns=columns;
    }

    public FiniteMap asMap() {
        return data;
    }


    public NumericValue numericValue() {
        return entryAt(0,0);
    }

    private MutableTuple<MutableTuple<NumericValue>> toMutableTuple() {
        if(data instanceof Tuple&&((Tuple) data).isFullTuple()){
            @SuppressWarnings("unchecked")
            MutableTuple<NumericValue>[] rows=(MutableTuple<NumericValue>[])new MutableTuple[((Tuple) data).length()];
            for(int i=0;i<rows.length;i++){
                MathObject t=((Tuple) data).get(i);
                if(t instanceof FiniteMap){
                    rows[i]= asMutableTuple((FiniteMap)t);
                }
            }
            return MutableTuple.from(rows,data.size());
        }else {
            TreeMap<Integer, MutableTuple<NumericValue>> map = new TreeMap<>();
            int maxIndex=0;
            for (Iterator<Pair> it = data.mapIterator(); it.hasNext(); ) {
                Pair p = it.next();
                if (p.b instanceof FiniteMap) {
                    int i = ((Real.Int) p.a).num().intValueExact();
                    map.put(i, asMutableTuple((FiniteMap)p.b));
                    maxIndex=Math.max(maxIndex,i);
                }
            }
            return MutableTuple.from(map,maxIndex+1);
        }
    }

    private static MutableTuple<NumericValue> asMutableTuple(FiniteMap row) {
        if(row instanceof Tuple&&((Tuple) row).isFullTuple()){
            NumericValue[] elements=((Tuple) row).toArray(NumericValue[].class);
            return MutableTuple.from(elements,row.size());
        }else {
            int maxIndex=0;
            TreeMap<Integer, NumericValue> map = new TreeMap<>();
            for (Iterator<Pair> it = row.mapIterator(); it.hasNext(); ) {
                Pair p = it.next();
                int i = ((Real.Int) p.a).num().intValueExact();
                map.put(i,(NumericValue)p.b);
                maxIndex=Math.max(maxIndex,i);
            }
            return MutableTuple.from(map,maxIndex+1);
        }
    }

    public int size() {
        return rows*columns;
    }

    public int[] dimensions() {
        return new int[]{rows, columns};
    }

    public NumericValue entryAt(int i, int j) {
        MathObject row=data.evaluateAt(Real.from(i));
        return row instanceof FiniteMap?((FiniteMap)row).evaluateAt(Real.from(j)).numericValue():Real.Int.ZERO;
    }

    public Matrix setEntry(int x, int y, NumericValue v) {
        MutableTuple<MutableTuple<NumericValue>> data=toMutableTuple();
        data.get(x).set(y,v);
        return Matrix.fromMutableTuple(data);
    }

    public Matrix applyToAll(Function<NumericValue, NumericValue> f) {
        MutableTuple<MutableTuple<NumericValue>> data= toMutableTuple();
        if(f.apply(Real.Int.ZERO).equals(Real.Int.ZERO)){
            for (MutableTuple.TupleEntry<MutableTuple<NumericValue>> r:data) {
                for(MutableTuple.TupleEntry<NumericValue> e:r.value){
                    r.value.set(e.index,f.apply(r.value.get(e.index)));
                }
            }
        }else{
            for (int i=0;i<data.length();i++) {
                MutableTuple<NumericValue> row=data.get(i);
                for(int j=0;j<row.length();j++){
                    row.set(j,f.apply(row.get(j)));
                }
            }
        }
        return fromMutableTuple(data);
    }
    public Matrix transpose() {
        MutableTuple<MutableTuple<NumericValue>> res=MutableTuple.from(columns);
        for (Iterator<Pair> it = data.mapIterator(); it.hasNext(); ) {
            Pair row = it.next();
            if(row.b instanceof FiniteMap){
                for (Iterator<Pair> iter = ((FiniteMap) row.b).mapIterator(); iter.hasNext(); ) {
                    Pair e = iter.next();
                    MutableTuple<NumericValue> column = res.get(((Real.Int) e.a).num().intValueExact());
                    if(column==null){
                        column=MutableTuple.from(rows);
                        res.set(((Real.Int) e.a).num().intValueExact(),column);
                    }
                    column.ensureLength(rows);
                    column.set(((Real.Int)row.a).num().intValueExact(),(NumericValue)e.b);
                }
            }
        }
        return fromMutableTuple(res);
    }

    public NumericValue determinant(){
        if(rows!=columns)
            throw new ArithmeticException("Determinant of non square matrix");
        MutableTuple<MutableTuple<NumericValue>> data= toMutableTuple();
        gaussianAlgorithm(data,null,false);
        NumericValue det= Real.Int.ONE;
        for(int i=0;i<rows;i++) {
            NumericValue e = data.get(i).get(i);
            if(e ==null|| e.equals(Real.Int.ZERO)){
                return Real.Int.ZERO;
            }else{
                det= NumericValue.multiply(det, e);
            }
        }
        return det;
    }
    public Matrix invert() {
        if(rows!=columns)
            throw new ArithmeticException("tried to invert non square matrix");
        MutableTuple<MutableTuple<NumericValue>> data= toMutableTuple();
        MutableTuple<MutableTuple<NumericValue>> ret=diagonal(rows,Real.Int.ONE);
        gaussianAlgorithm(data,ret,true);
        for(int i=0;i<rows;i++) {
            MutableTuple<NumericValue> row = ret.get(i);
            for(MutableTuple.TupleEntry<NumericValue> e:row){
                row.set(e.index, e.value==null? Real.Int.ZERO: NumericValue.divide(e.value, data.get(e.index).get(e.index)));
            }
        }
        return fromMutableTuple(ret);
    }





    @NotNull
    @Override
    public Iterator<NumericValue> iterator() {
        return new Iterator<NumericValue>() {
            int r=0,c=0;
            @Override
            public boolean hasNext() {
                return r<rows;
            }

            @Override
            public NumericValue next() {
                if(c>=columns) {
                    c = 0;
                    r++;
                }
                MathObject row = data.evaluateAt(Real.from(r));
                return row instanceof FiniteMap?((FiniteMap) row).evaluateAt(Real.from(c)).numericValue():Real.Int.ZERO;
            }
        };
    }
    /**Iterator over the Entries of this Matrix that skips elements with the value zero*/
    public @NotNull Iterator<MatrixEntry> matrixIterator() {
        return new Iterator<MatrixEntry>() {
            final Iterator<Pair> rowItr=data.mapIterator();
            Iterator<Pair> columnItr=nextColumn();
            int r;

            private Iterator<Pair> nextColumn() {
                columnItr=null;
                while (columnItr==null&&rowItr.hasNext()){
                    Pair next = rowItr.next();
                    if (next.b instanceof FiniteMap) {
                        r=next.a.numericValue().realPart().num().intValueExact();
                        columnItr = ((FiniteMap) next.b).mapIterator();
                    }
                }
                return columnItr;
            }

            @Override
            public boolean hasNext() {
                return columnItr.hasNext();
            }

            @Override
            public MatrixEntry next() {
                Pair next = columnItr.next();
                if(!columnItr.hasNext()){
                    columnItr=nextColumn();
                }
                return new MatrixEntry(r,next.a.numericValue().realPart().num().intValueExact(),(NumericValue) next.b);
            }
        };
    }
    /**Iterator over the Values in this Matrix values  that skips elements with the value zero*/
    @NotNull
    public Iterator<NumericValue> sparseIterator() {
        return new Iterator<NumericValue>() {
            final Iterator<MatrixEntry> entries=matrixIterator();
            @Override
            public boolean hasNext() {
                return entries.hasNext();
            }
            @Override
            public NumericValue next() {
                return entries.next().value;
            }
        };
    }
    /**Iterates over the non-empty rows of this Matrix,
     *  returns pairs with the rowid in the first component and the row in the second*/
    public @NotNull Iterator<Pair> rowIterator() {
        return new Iterator<Pair>() {
            final Iterator<Pair> rowItr=data.mapIterator();
            Pair nextRow=nextRow();

            private Pair nextRow(){
                nextRow=null;
                while(nextRow==null&&rowItr.hasNext()) {
                    Pair next = rowItr.next();
                    if (next.b instanceof FiniteMap) {
                        nextRow=next;
                    }
                }
                return nextRow;
            }

            @Override
            public boolean hasNext() {
                return nextRow!=null;
            }

            @Override
            public Pair next() {
                Pair tmp=nextRow;
                nextRow=nextRow();
                return tmp;
            }
        };
    }


    @Override
    public String toString(BigInteger base, boolean useSmallBase) {
        return data.toString(base,useSmallBase);
    }
    @Override
    public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
        return data.toStringFixedPoint(base,precision,useSmallBase);
    }
    @Override
    public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
        return data.toStringFloat(base,precision,useSmallBase);
    }
    @Override
    public String intsAsString() {
        return data.intsAsString();
    }
    @Override
    public String asString() {
        StringBuilder sb=new StringBuilder();
        for (Iterator<NumericValue> it = sparseIterator(); it.hasNext(); ) {
            NumericValue e = it.next();
            sb.append(e.asString());
        }
        return sb.toString();
    }

    //handling of equals and hash in wrapped map
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return data.equals(o);
    }
    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
