package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

//addLater merge FullMatrix and SparseMatrix classes
public abstract class Matrix extends MathObject implements Iterable<NumericValue> {
    Matrix(){}//packagePrivate constructor

    /**value of totalSize/elements above which SparseMatrices are used*/
    static private final int SPARSE_FACTOR =5;

    public static Matrix identityMatrix(int n){
        return diagonalMatrix(n, Real.Int.ONE);
    }

    //addLater use sparseVariant for large sizes
    public static Matrix diagonalMatrix(int n, NumericValue value) {
        if(n <=0)throw new IllegalArgumentException("n has to be >0");
        NumericValue[][] data=new NumericValue[n][n];
        for(int i = 0; i< n; i++)
            data[i][i]= value;
        return new FullMatrix(data);
    }

    /**Converts the given MathObject to a Matrix*/
    public static Matrix asMatrix(MathObject o){
        if(o instanceof NumericValue){
            return new FullMatrix(new NumericValue[][]{{(NumericValue) o}});
        }else if(o instanceof FullMatrix){
            return (Matrix) o;
        }else if(o instanceof FiniteSet){
            Tuple[] rows=new Tuple[((FiniteSet) o).size()];
            int i=0,maxLength=0;
            long size=0;
            for(MathObject row:(FiniteSet)o){
                rows[i]= rowFrom(row);
                maxLength=Math.max(maxLength,rows[i++].length());
                size+=rows[i].size();
            }
            if(((long)rows.length)*maxLength>SPARSE_FACTOR*size){
                return new SparseMatrix(Tuple.create(rows));
            }else{
                return new FullMatrix(fullData(rows,maxLength));
            }
        }else if(o instanceof Tuple&&((Tuple) o).isFullTuple()){
            Tuple[] rows=new Tuple[((Tuple) o).length()];
            int maxLength=0;
            long size=0;
            for(int i=0;i<((Tuple) o).length();i++){
                rows[i]= rowFrom(((Tuple) o).get(i));
                maxLength=Math.max(maxLength,rows[i].length());
                size+=rows[i].size();
            }
            if(((long)rows.length)*maxLength>SPARSE_FACTOR*size){
                return new SparseMatrix(Tuple.create(rows));
            }else{
                return new FullMatrix(fullData(rows,maxLength));
            }
        }else if(o instanceof FiniteMap){
            TreeMap<MathObject,MathObject> rows = new TreeMap<>(MathObject::compare);
            int maxLength=0;
            long size=0;
            Real.Int maxRow=Real.Int.ZERO;
            for (Iterator<Pair> it = ((FiniteMap) o).mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                Tuple row = rowFrom(e.b);
                Real.Int rowNumber = e.a.numericValue().realPart().round(ROUND);
                maxRow=(Real.Int)Real.max(maxRow,rowNumber);
                rows.put(rowNumber, row);
                maxLength=Math.max(maxLength,row.length());
                size+=row.size();
            }
            return toMatrix(rows, maxLength, size, maxRow);
        }else{
            throw new RuntimeException("Unexpected MathObject class:"+o.getClass());
        }
    }

    @NotNull
    private static Matrix toMatrix(TreeMap<MathObject, MathObject> rows, int maxLength, long size, Real.Int maxRow) {
        if(Real.multiply(maxRow,Real.from(maxLength)).compareTo(Real.from(SPARSE_FACTOR* size))>0){
            return new SparseMatrix(FiniteMap.from(rows));
        }else{
            NumericValue[][] arrayData=new NumericValue[maxRow.num().intValueExact()+1][maxLength];
            return new FullMatrix(arrayData);
        }
    }

    static Matrix asMatrix(TreeMap<Real.Int, TreeMap<Real.Int, NumericValue>> data,BigInteger rowLength) {
        TreeMap<MathObject,MathObject> rows=new TreeMap<>(MathObject::compare);
        TreeMap<Real.Int,NumericValue> row;
        for(Map.Entry<Real.Int, TreeMap<Real.Int, NumericValue>> e:data.entrySet()){
            row=e.getValue();
            if(row.size()<3L*rowLength.intValueExact()){
                MathObject[] fullData=new MathObject[rowLength.intValueExact()];
                for(int i=0;i<fullData.length;i++){
                    fullData[i]=row.get(Real.from(i));
                    if(fullData[i]==null)
                        fullData[i]=Real.Int.ZERO;
                }
                rows.put(e.getKey(),Tuple.create(fullData));
            }else{
                rows.put(e.getKey(),FiniteMap.from(row));
            }
        }
        return new SparseMatrix(FiniteMap.from(rows));
    }

    /**creates a Matrix from the given rows, all rows are cut/extended to have exactly rowSize elements */
    static NumericValue[][] fullData(Tuple[] rows, int rowSize) {
        NumericValue[][] data=new NumericValue[rows.length][];
        for(int i=0;i<rows.length;i++){//adjust size of rows
            NumericValue[] copy=new NumericValue[rowSize];
            for(int j=0;j<rows[i].length();j++){
                copy[j]=rows[i].get(j).numericValue();
            }
            if(rows[i].length()<rowSize) {
                Arrays.fill(copy, rows[i].length(), rowSize, Real.Int.ZERO);
            }
            data[i]=copy;
        }
        return data;
    }
    static Tuple rowFrom(MathObject row) {
        if(row instanceof NumericValue){
            return Tuple.create(new NumericValue[]{(NumericValue) row});
        }else if(row instanceof FullMatrix){
            ArrayList<NumericValue> values = new ArrayList<>(((Matrix) row).size());
            for(NumericValue nv:(Matrix)row){
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


    public static Matrix forEach(Matrix a,Matrix b, BiFunction<NumericValue, NumericValue, NumericValue> f){
        if(a instanceof FullMatrix&&b instanceof FullMatrix){
            return ((FullMatrix) a).forEach((FullMatrix)b,f);
        }else if(f.apply(Real.Int.ZERO,Real.Int.ZERO).equals(Real.Int.ZERO)){
            Iterator<SparseMatrix.MatrixEntry> itrA=a.matrixIterator();
            Iterator<SparseMatrix.MatrixEntry> itrB=b.matrixIterator();
            TreeMap<MathObject,MathObject> rows=new TreeMap<>(MathObject::compare);
            TreeMap<MathObject,MathObject> column=new TreeMap<>(MathObject::compare);
            if(!itrA.hasNext())
                return b;
            if(!itrB.hasNext())
                return a;
            SparseMatrix.MatrixEntry nextA=itrA.next(),nextB= itrB.next();
            BigInteger columnId=nextA.x.min(nextB.x);
            while(nextA!=null||nextB!=null){
                if(nextA!=null&&(nextB==null||nextA.x.compareTo(nextB.x)<0)){
                    column.put(Real.from(nextA.y), f.apply(nextA.value,Real.Int.ZERO));
                    nextA=itrA.hasNext()?itrA.next():null;
                }else if(nextA == null || nextB.x.compareTo(nextA.x) < 0){
                    column.put(Real.from(nextB.y), f.apply(Real.Int.ZERO,nextB.value));
                    nextB=itrB.hasNext()?itrB.next():null;
                }else{
                    int cp=nextA.y.compareTo(nextB.y);
                    if(cp<0){
                        column.put(Real.from(nextA.y), f.apply(nextA.value,Real.Int.ZERO));
                        nextA=itrA.hasNext()?itrA.next():null;
                    }else if(cp>0){
                        column.put(Real.from(nextB.y), f.apply(Real.Int.ZERO,nextB.value));
                        nextB=itrB.hasNext()?itrB.next():null;
                    }else{
                        column.put(Real.from(nextA.y), f.apply(nextA.value,nextB.value));
                        nextA=itrA.hasNext()?itrA.next():null;
                        nextB=itrB.hasNext()?itrB.next():null;
                    }
                }
                if((nextA==null||nextA.x.compareTo(columnId)>0)){
                    if((nextB==null||nextB.x.compareTo(columnId)>0)){
                        rows.put(Real.from(columnId),FiniteMap.from(column));
                        column=new TreeMap<>(MathObject::compare);
                        columnId=nextA==null?nextB==null?null:nextB.x:
                                nextB==null?nextA.x:nextA.x.min(nextB.x);
                    }
                }
            }
            return asMatrix(FiniteMap.from(rows));
        }else{
            int[] dimA=a.dimensions(),dimB=b.dimensions();
            NumericValue[][] data=new NumericValue[Math.max(dimA[0],dimB[0])][Math.max(dimA[1],dimB[1])];
            for(int i=0;i<data.length;i++){
                for(int j=0;j<data[i].length;j++){
                    NumericValue e=f.apply(a.entryAt(i,j),b.entryAt(i,j));
                    data[i][j]=e;
                }
            }
            return new FullMatrix(data);
        }
    }

    public static Matrix matrixMultiply(Matrix a, Matrix b) {
        if(a instanceof FullMatrix&&b instanceof FullMatrix){
            return ((FullMatrix)a).multiply(((FullMatrix)b));
        }else{//sparse multiply
            TreeMap<Real.Int,TreeMap<Real.Int,NumericValue>> res=new TreeMap<>();
            BigInteger rowLen=BigInteger.ZERO;
            for (Iterator<Pair> it = a.rowIterator(); it.hasNext(); ) {
                Pair r = it.next();
                for (Iterator<Pair> iter = ((FiniteMap) r.b).mapIterator(); iter.hasNext(); ) {
                    Pair e = iter.next();
                    for (Iterator<Pair> iterator = b.rowIterator(); iterator.hasNext(); ) {
                        Pair s = iterator.next();
                        Real.Int z = (Real.Int) s.a;
                        rowLen=rowLen.max(z.num());
                        NumericValue v=NumericValue.multiply((NumericValue)e.b,
                                b.entryAt(((Real.Int)e.a).num().intValueExact(), z.num().intValueExact()));
                        if(!v.equals(Real.Int.ZERO)){
                            Real.Int x = (Real.Int) r.a;
                            TreeMap<Real.Int,NumericValue> row=res.get(x);
                            if(row==null){
                                row=new TreeMap<>();
                            }
                            NumericValue prev=row.get(z);
                            if(prev!=null){
                                v=NumericValue.add(v,prev);
                            }
                            row.put(z,v);
                            res.put(x,row);
                        }
                    }
                }
            }
            //ensure full size
            Real.Int lastRow = Real.from(a.dimensions()[0] - 1);
            Real.Int lastColumn = Real.from(b.dimensions()[1] - 1);
            TreeMap<Real.Int,NumericValue> row=res.get(lastRow);
            if(row==null){
                row=new TreeMap<>();
            }
            NumericValue prev=row.get(lastColumn);
            if(prev==null){
                row.put(lastColumn,Real.Int.ZERO);
                res.put(lastRow,row);
            }
            return asMatrix(res,rowLen);
        }
    }

    abstract FullMatrix toFullMatrix();

    public abstract NumericValue numericValue();

    public abstract FiniteMap asMap();

    //addLater? BigInt keys/size/dimensions

    public abstract NumericValue entryAt(int i, int j);
    public abstract Matrix setEntry(int x, int y, NumericValue v);

    public abstract int size();
    public abstract int[] dimensions();

    public abstract Matrix transpose();
    public abstract NumericValue determinant();
    public abstract Matrix invert();
    public abstract Matrix applyToAll(Function<NumericValue, NumericValue> operation);


    @NotNull
    @Override
    public abstract Iterator<NumericValue> iterator();
    /**Iterator over the Values in this Matrix values  that skips elements with the value zero*/
    @NotNull
    public Iterator<NumericValue> sparseIterator() {
        return new Iterator<NumericValue>() {
            final Iterator<SparseMatrix.MatrixEntry> entries=matrixIterator();
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
    /**Iterator over the Entries of this Matrix that skips elements with the value zero*/
    @NotNull
    public abstract Iterator<SparseMatrix.MatrixEntry> matrixIterator();
    /**Iterates over the non-empty rows of this Matrix,
     *  returns pairs with the rowid in the first component and the row in the second*/
    @NotNull
    public abstract Iterator<Pair> rowIterator();


    @Override
    public abstract String toString(BigInteger base, boolean useSmallBase);
    @Override
    public abstract String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase);
    @Override
    public abstract String toStringFloat(BigInteger base, Real precision, boolean useSmallBase);
    @Override
    public abstract String intsAsString();
    @Override
    public String asString() {
        StringBuilder sb=new StringBuilder();
        for (Iterator<NumericValue> it = sparseIterator(); it.hasNext(); ) {
            NumericValue e = it.next();
            sb.append(e.asString());
        }
        return sb.toString();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o){
        return asMap().equals(o);
    }

    @Override
    public abstract int hashCode();
}
