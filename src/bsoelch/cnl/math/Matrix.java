package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import static bsoelch.cnl.math.Real.Int;

public final class Matrix implements MathObject,Iterable<NumericValue>{
    private final NumericValue[][] matrix;

    static public Matrix identityMatrix(int n){
        return diagonalMatrix(n,Int.ONE);
    }
    static public Matrix diagonalMatrix(int n, NumericValue value) {
        if(n <=0)throw new IllegalArgumentException("n has to be >0");
        NumericValue[][] data=new NumericValue[n][n];
        for(int i = 0; i< n; i++)
            data[i][i]= value;
        return new Matrix(data);
    }

    /**Converts the given MathObject to a Matrix*/
    static public Matrix asMatrix(MathObject o){//TODO? handle spares Matrices
        if(o instanceof NumericValue){
            return new Matrix(new NumericValue[][]{{(NumericValue) o}});
        }else if(o instanceof Matrix){
            return (Matrix) o;
        }else if(o instanceof FiniteSet){
            NumericValue[][] rows=new NumericValue[((FiniteSet) o).size()][];
            int i=0,maxSize=0;
            for(MathObject row:(FiniteSet)o){
                rows[i]=rowFrom(row);
                maxSize=Math.max(maxSize,rows[i++].length);
            }
            return fromRows(rows,maxSize);
        }else if(o instanceof Tuple){
            NumericValue[][] rows=new NumericValue[((Tuple) o).size()][];
            int maxSize=0;
            for(int i=0;i<((Tuple) o).size();i++){
                rows[i]=rowFrom(((Tuple) o).get(i));
                maxSize=Math.max(maxSize,rows[i].length);
            }
            return fromRows(rows,maxSize);
        }else if(o instanceof FiniteMap){
            ArrayList<NumericValue[]> rows = new ArrayList<>(((FiniteMap) o).size());
            Real lastKey=null;
            int maxSize=0;
            for (Iterator<Pair> it = ((FiniteMap) o).mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                if(lastKey!=null){
                    Real key = e.a.numericValue().realPart();
                    Real delta=Real.subtract(key,lastKey);
                    int intDelta=delta.abs().round(FLOOR).num().intValueExact();
                    while(intDelta>0){//fill empty entries with zeros
                        rows.add(new NumericValue[0]);
                        intDelta--;
                    }
                    lastKey=key;
                }else{
                    lastKey=e.a.numericValue().realPart();
                }
                rows.add(rowFrom(e.b));
                maxSize=Math.max(maxSize,rows.get(rows.size()-1).length);
            }
            return fromRows(rows.toArray(new NumericValue[0][]),maxSize);
        }else{
            throw new RuntimeException("Unexpected MathObject class:"+o.getClass());
        }
    }
    /**creates a Matrix from the given rows, all rows are cut/extended to have exactly rowSize elements */
    private static Matrix fromRows(NumericValue[][] rows,int rowSize) {
        for(int i=0;i<rows.length;i++){//adjust size of rows
            NumericValue[] copy=new NumericValue[rowSize];
            System.arraycopy(rows[i],0,copy,0,Math.min(rows[i].length,rowSize));
            if(rows[i].length<rowSize) {
                Arrays.fill(copy, rows[i].length, rowSize, Int.ZERO);
            }
            rows[i]=copy;
        }
        return new Matrix(rows);
    }
    private static NumericValue[] rowFrom(MathObject row) {
        if(row instanceof NumericValue){
            return new NumericValue[]{(NumericValue) row};
        }else if(row instanceof Matrix){
            ArrayList<NumericValue> values = new ArrayList<>(((Matrix) row).size());
            for(NumericValue nv:(Matrix)row){
                values.add(nv);
            }
            return values.toArray(new NumericValue[0]);
        }else if(row instanceof FiniteSet){
            ArrayList<NumericValue> values = new ArrayList<>();
            for(MathObject o:(FiniteSet)row){
                values.addAll(Arrays.asList(rowFrom(o)));
            }
            return values.toArray(new NumericValue[0]);
        }else if(row instanceof Tuple){
            ArrayList<NumericValue> values = new ArrayList<>();
            for(MathObject o:(Tuple)row){
                values.addAll(Arrays.asList(rowFrom(o)));
            }
            return values.toArray(new NumericValue[0]);
        }else if(row instanceof FiniteMap){
            ArrayList<NumericValue> values = new ArrayList<>();
            Real lastKey=null;
            for (Iterator<Pair> it = ((FiniteMap) row).mapIterator(); it.hasNext(); ) {
                Pair e = it.next();
                if(lastKey!=null){
                    Real key = e.a.numericValue().realPart();
                    Real delta=Real.subtract(key,lastKey);
                    int intDelta=delta.abs().round(FLOOR).num().intValueExact();
                    while(intDelta>0){//fill empty entries with zeros
                        values.add(Int.ZERO);
                        intDelta--;
                    }
                    lastKey=key;
                }else{
                    lastKey=e.a.numericValue().realPart();
                }
                values.addAll(Arrays.asList(rowFrom(e.b)));
            }
            return values.toArray(new NumericValue[0]);
        }else{
            throw new RuntimeException("Unexpected MathObject class:"+row.getClass());
        }
    }


    private Matrix(NumericValue[][] matrix) {
        this.matrix = matrix;
    }

    public NumericValue numericValue() {
        return matrix[0][0];
    }

    public FiniteMap asMap(){
        Tuple[] rows=new Tuple[matrix.length];
        for(int i=0;i<matrix.length;i++){
            rows[i]=Tuple.create(matrix[i]);
        }
        return Tuple.create(rows);
    }

    public NumericValue entryAt(int i, int j){
        return entryAtInternal(i, j,true);
    }

    NumericValue entryAtInternal(int i, int j, boolean throwException) {
        if (i >= 0 && i < matrix.length && j >= 0 && j < matrix[i].length) {
            return matrix[i][j] == null ? Int.ZERO : matrix[i][j];
        }else if(throwException){
            throw new IndexOutOfBoundsException("("+i+","+j+") out of Bounds, size:"+matrix.length+" x "+matrix[0].length);
        }else{
            return Int.ZERO;
        }
    }

    public NumericValue entryAt(int index){
        return entryAt(index% matrix.length,index/ matrix.length);
    }

    public Matrix setEntry(int x, int y, NumericValue v){
        NumericValue[][] data=new NumericValue[Math.max(x+1,matrix.length)][Math.max(y+1,matrix[0].length)];
        data[x][y]=v;
        for(int i=0;i<matrix.length;i++){
            System.arraycopy(matrix[i], 0, data[i], 0, matrix.length);
        }
        return new Matrix(data);
    }
    public Matrix setEntry(int index, NumericValue v){
        return setEntry(index% matrix.length,index/ matrix.length,v);
    }

    /**Number of Entries in Matrix*/
    public int size(){
        return matrix.length*matrix[0].length;
    }
    /**size of this Matrix as int[]*/
    public int[] dimensions(){
        return new int[]{matrix.length, matrix[0].length};
    }

    public Matrix forEach(Matrix m2, BiFunction<NumericValue, NumericValue, NumericValue> f){
        NumericValue[][] ret=new NumericValue[Math.max(matrix.length,m2.matrix.length)][Math.max(matrix[0].length,m2.matrix[0].length)];
        for(int i=0;i<ret.length;i++){
            for(int j=0;j<ret.length;j++){
                ret[i][j]=f.apply(entryAtInternal(i,j,false),m2.entryAtInternal(i,j,false));
            }
        }
        return new Matrix(ret);
    }

    public Matrix multiply(Matrix m2){
        NumericValue[][] ret=new NumericValue[matrix.length][m2.matrix[0].length];
        int s=Math.min(matrix[0].length,m2.matrix.length);
        NumericValue v;
        for(int i=0;i<ret.length;i++){
            for(int j=0;j<ret[i].length;j++){
                v= Int.ZERO;
                for(int k=0;k< s;k++){
                    v= NumericValue.add(v, NumericValue.multiply(matrix[i][k],m2.matrix[k][j]));
                }
                ret[i][j]=v;
            }
        }
        return new Matrix(ret);
    }

    public Matrix transpose(){
        NumericValue[][] data=new NumericValue[matrix[0].length][matrix.length];
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[i].length;j++){
                data[j][i]=matrix[i][j];
            }
        }
        return new Matrix(data);
    }

    public NumericValue determinant(){
        if(matrix.length!=matrix[0].length)
            throw new ArithmeticException("Determinant of non square matrix");
        NumericValue[][] data=new NumericValue[matrix.length][matrix.length];
        for(int i=0;i<matrix.length;i++) {
            data[i]=matrix[i].clone();
        }
        gaussianAlgorithm(data,null,false);
        NumericValue det= Int.ONE;
        for(int i=0;i<matrix.length;i++) {
            if(data[i][i]==null||data[i][i].equals(Int.ZERO)){
                return Int.ZERO;
            }else{
                det= NumericValue.multiply(det,data[i][i]);
            }
        }
        return det;
    }

    public Matrix invert(){
        if(matrix.length!=matrix[0].length)
            throw new ArithmeticException("Inversion of non square matrix");
        int s=matrix.length;
        NumericValue[][] data=new NumericValue[s][s],ret=new NumericValue[s][s];
        for(int i=0;i<s;i++) {
            ret[i][i] = Int.ONE;
            data[i]=matrix[i].clone();
        }
        gaussianAlgorithm(data,ret,true);
        for(int i=0;i<s;i++) {
            for(int j=0;j<ret.length;j++){
                ret[j][i]=ret[j][i]==null? Int.ZERO: NumericValue.divide(ret[j][i],data[i][i]);
            }
        }
        return new Matrix(ret);
    }

    public Matrix applyToAll(Function<NumericValue, NumericValue> operation){
        NumericValue[][] data=new NumericValue[matrix.length][matrix[0].length];
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[i].length;j++){
                data[i][j]=operation.apply(matrix[i][j]);
            }
        }
        return new Matrix(data);
    }

    /**@return Iterator that goes through the elements of this matrix*/
    @NotNull
    @Override
    public Iterator<NumericValue> iterator() {
        return new Iterator<NumericValue>() {
            int r=0,c=0;
            @Override
            public boolean hasNext() {
                return r<matrix.length;
            }

            @Override
            public NumericValue next() {
                if(c>=matrix[r].length) {
                    c = 0;
                    r++;
                }
                return matrix[r][c++];
            }
        };
    }

    public String toString(BigInteger base, boolean useSmallBase) {
        return toString(s->s.toString(base, useSmallBase));
    }

    public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
        return toString(s->s.toStringFixedPoint(base,precision, useSmallBase));
    }

    public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
        return toString(s->s.toStringFloat(base,precision, useSmallBase));
    }
    @Override
    public String intsAsString() {
        return toString(MathObject::intsAsString);
    }
    @Override
    public String asString() {
        return numericValue().asString();
    }

    private String toString(Function<NumericValue,String> entryToString){
        StringBuilder str=new StringBuilder("[");
        boolean firstRow=true,firstColumn;
        for(NumericValue[] row:matrix){
            if(firstRow){
                firstRow=false;
            }else{
                str.append(", ");
            }
            str.append("[");
            firstColumn=true;
            for(NumericValue s:row){
                if (firstColumn) {
                    firstColumn=false;
                } else {
                    str.append(", ");
                }
                str.append(entryToString.apply(s));
            }
            str.append("]");
        }
        return str.append("]").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matrix)) return false;
        Matrix matrix1 = (Matrix) o;
        return Arrays.deepEquals(matrix, matrix1.matrix);
    }
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(matrix);
    }

    static void gaussianAlgorithm(NumericValue[][] target, @Nullable NumericValue[][] applicant, boolean total) {
        int y=0,k0;
        for (NumericValue[] numericValues : target) {
            k0 = -1;
            for (int k = y; k < target.length; k++) {
                if (numericValues[k]!=null&&!Int.ZERO.equals(numericValues[k])) {
                    k0 = k;
                }
            }
            if (k0 != -1) {
                if (k0 != y)
                    addLine(target, applicant, k0, numericValues[k0].invert(), y);
                for (int k = y+1; k < target.length; k++) {
                    if (!Int.ZERO.equals(numericValues[k])) {
                        addLine(target, applicant, y, NumericValue.divide(numericValues[k], numericValues[y]).negate(), k);
                    }
                }
                if (total) {
                    for (int k = 0; k < y; k++) {
                        if (!Int.ZERO.equals(numericValues[k])) {
                            addLine(target, applicant, y, NumericValue.divide(numericValues[k], numericValues[y]).negate(), k);
                        }
                    }
                }
                y++;
            }
        }
    }

    /**adds f times line a to line b*/
    private static void addLine(NumericValue[][] target, @Nullable NumericValue[][] applicant, int a, NumericValue f, int b) {
        for(int i=0;i<target.length;i++){
            target[i][b]= NumericValue.add(target[i][a]==null? Int.ZERO: NumericValue.multiply(target[i][a],f),
                    target[i][b]==null? Int.ZERO:target[i][b]);
        }
        if(applicant!=null){
            for(int i=0;i<applicant.length;i++){
                applicant[i][b]= NumericValue.add(applicant[i][a]==null? Int.ZERO: NumericValue.multiply(applicant[i][a],f),
                        applicant[i][b]==null? Int.ZERO:applicant[i][b]);
            }
        }
    }

}