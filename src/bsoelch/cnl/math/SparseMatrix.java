package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.function.Function;

public final class SparseMatrix extends Matrix{
    static class MatrixEntry{
        final BigInteger x,y;
        final NumericValue value;

        MatrixEntry(BigInteger x, BigInteger y, NumericValue value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }
    final FiniteMap data;

    final BigInteger rows, columns;

    SparseMatrix(FiniteMap data){
        if(!data.isMatrix())
            throw new ArithmeticException("data does not represent a Matrix");
        this.data=data;
        BigInteger tmpR=null,tmpC=null,tmp;
        for (Iterator<Pair> it = data.mapIterator(); it.hasNext(); ) {
            Pair e = it.next();
            if(e.b instanceof FiniteMap) {
                if(tmpR==null){
                    tmpR=e.a.numericValue().realPart().num();
                }else{
                    tmpR=tmpR.max(e.a.numericValue().realPart().num());
                }
                tmp=null;
                for (Iterator<Pair> it2 = ((FiniteMap) e.b).mapIterator(); it2.hasNext(); ) {
                    Pair f = it2.next();
                    if (tmp == null) {
                        tmp = f.a.numericValue().realPart().num();
                    } else {
                        tmp = tmp.max(f.a.numericValue().realPart().num());
                    }
                }
                if (tmp != null)
                    tmpC = tmpC == null ? tmp : tmpC.max(tmp);
            }else if(!e.b.equals(Real.Int.ZERO)){
                throw new IllegalArgumentException("data does not represent a Matrix");
            }
        }
        rows=tmpR==null?BigInteger.ONE:tmpR.add(BigInteger.ONE);
        columns =tmpC==null?BigInteger.ONE:tmpC.add(BigInteger.ONE);
    }

    @Override
    public NumericValue numericValue() {
        return entryAt(0,0);
    }

    @Override
    public FiniteMap asMap() {
        return data;
    }

    @Override
    public NumericValue entryAt(int i, int j) {
        MathObject row=data.evaluateAt(Real.from(i));
        return row instanceof FiniteMap?((FiniteMap)row).evaluateAt(Real.from(j)).numericValue():Real.Int.ZERO;
    }
    @Override
    public Matrix setEntry(int x, int y, NumericValue v) {
        TreeMap<Real.Int,TreeMap<Real.Int,NumericValue>> newData=new TreeMap<>();
        boolean added=false;
        for (Iterator<Pair> it = data.mapIterator(); it.hasNext(); ) {
            Pair e = it.next();
            if(e.b instanceof FiniteMap) {
                @SuppressWarnings("SuspiciousMethodCalls") TreeMap<Real.Int,NumericValue> row=newData.get(e.a);
                if(row==null){
                    row=new TreeMap<>();
                }
                for (Iterator<Pair> iter = ((FiniteMap) e.b).mapIterator(); iter.hasNext(); ) {
                    Pair p = iter.next();
                    row.put((Real.Int)p.a,p.b.numericValue());
                }
                if(e.a.equals(Real.from(x))){
                    row.put(Real.from(y),v);
                    added=true;
                }
                newData.put((Real.Int)e.a,row);
            }
        }
        if(!added){
            TreeMap<Real.Int,NumericValue> row=new TreeMap<>();
            row.put(Real.from(y),v);
            newData.put(Real.from(x),row);
        }
        return Matrix.asMatrix(newData,columns);
    }

    @Override
    public int size() {
        return rows.multiply(columns).intValueExact();
    }
    @Override
    public int[] dimensions() {
        return new int[]{rows.intValueExact(), columns.intValueExact()};
    }

    @Override
    public Matrix applyToAll(Function<NumericValue, NumericValue> f) {
        if(f.apply(Real.Int.ZERO).equals(Real.Int.ZERO)){
            TreeMap<Real.Int,TreeMap<Real.Int,NumericValue>> newData=new TreeMap<>();
            for (Iterator<Pair> it = data.mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                if(e.b instanceof FiniteMap) {
                    @SuppressWarnings("SuspiciousMethodCalls") TreeMap<Real.Int,NumericValue> row=newData.get(e.a);
                    if(row==null){
                        row=new TreeMap<>();
                    }
                    for (Iterator<Pair> iter = ((FiniteMap) e.b).mapIterator(); iter.hasNext(); ) {
                        Pair p = iter.next();
                        row.put((Real.Int)p.a,f.apply((NumericValue) p.b));
                    }
                    newData.put((Real.Int)e.a,row);
                }
            }
            return Matrix.asMatrix(newData,columns);
        }else{
            return new FullMatrix(arrayData(f));
        }
    }

    private NumericValue[][] arrayData(Function<NumericValue,NumericValue> transform){
        NumericValue[][] data=new NumericValue[rows.intValueExact()][columns.intValueExact()];
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[i].length;j++){
                NumericValue e=entryAt(i,j);
                if(transform!=null){
                    e=transform.apply(e);
                }
                data[i][j]=e;
            }
        }
        return data;
    }

    @Override
    public Matrix transpose() {
        TreeMap<Real.Int,TreeMap<Real.Int,NumericValue>> data=new TreeMap<>();
        for (Iterator<MatrixEntry> it = matrixIterator(); it.hasNext(); ) {
            MatrixEntry e = it.next();
            TreeMap<Real.Int,NumericValue> row=data.get(Real.from(e.x));
            if(row==null){
                row=new TreeMap<>();
            }
            row.put(Real.from(e.y),e.value);
            data.put(Real.from(e.x),row);
        }
        return Matrix.asMatrix(data,rows);
    }

    @Override
    public NumericValue determinant() {
        //TODO sparse determinant
        return toFullMatrix().determinant();
    }

    @Override
    public Matrix invert() {
        //addLater? sparse invert
        return toFullMatrix().invert();
    }

    FullMatrix toFullMatrix(){
        return new FullMatrix(arrayData(null));
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


    @NotNull
    @Override
    public Iterator<NumericValue> iterator() {
        return new Iterator<NumericValue>() {
            BigInteger r=BigInteger.ZERO,c=BigInteger.ZERO;
            @Override
            public boolean hasNext() {
                return r.compareTo(rows)<0;
            }

            @Override
            public NumericValue next() {
                if(c.compareTo(columns)>=0) {
                    c = BigInteger.ZERO;
                    r=r.add(BigInteger.ONE);
                }
                MathObject row = data.evaluateAt(Real.from(r));
                return row instanceof FiniteMap?((FiniteMap) row).evaluateAt(Real.from(c)).numericValue():Real.Int.ZERO;
            }
        };
    }
    @Override
    public @NotNull Iterator<MatrixEntry> matrixIterator() {
        return new Iterator<MatrixEntry>() {
            final Iterator<Pair> rowItr=data.mapIterator();
            Iterator<Pair> columnItr=nextColumn();
            BigInteger r;

            private Iterator<Pair> nextColumn() {
                columnItr=null;
                while (columnItr==null&&rowItr.hasNext()){
                    Pair next = rowItr.next();
                    if (next.b instanceof FiniteMap) {
                        r=next.a.numericValue().realPart().num();
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
                return new MatrixEntry(r,next.a.numericValue().realPart().num(),(NumericValue) next.b);
            }
        };
    }

    @Override
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