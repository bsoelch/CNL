package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import static bsoelch.cnl.math.Real.Int;

public final class FullMatrix extends Matrix {

    private final NumericValue[][] matrix;

    FullMatrix(NumericValue[][] matrix) {
        this.matrix = matrix;
    }

    @Override
    public NumericValue numericValue() {
        return matrix[0][0];
    }

    @Override
    public FiniteMap asMap(){
        Tuple[] rows=new Tuple[matrix.length];
        for(int i=0;i<matrix.length;i++){
            rows[i]=Tuple.create(matrix[i]);
        }
        return Tuple.create(rows);
    }

    @Override
    public NumericValue entryAt(int i, int j){
        if (i >= 0 && i < matrix.length && j >= 0 && j < matrix[i].length) {
            return matrix[i][j] == null ? Int.ZERO : matrix[i][j];
        }else{
            return Int.ZERO;
        }
    }

    @Override
    public Matrix setEntry(int x, int y, NumericValue v){
        NumericValue[][] data=new NumericValue[Math.max(x+1,matrix.length)][Math.max(y+1,matrix[0].length)];
        data[x][y]=v;
        for(int i=0;i<matrix.length;i++){
            System.arraycopy(matrix[i], 0, data[i], 0, matrix.length);
        }
        return new FullMatrix(data);
    }
    /**Number of Entries in Matrix*/
    @Override
    public int size(){
        return matrix.length*matrix[0].length;
    }
    /**size of this Matrix as int[]*/
    @Override
    public int[] dimensions(){
        return new int[]{matrix.length, matrix[0].length};
    }

    public Matrix forEach(FullMatrix m2, BiFunction<NumericValue, NumericValue, NumericValue> f){
        NumericValue[][] ret=new NumericValue[Math.max(matrix.length,m2.matrix.length)][Math.max(matrix[0].length,m2.matrix[0].length)];
        for(int i=0;i<ret.length;i++){
            for(int j=0;j<ret.length;j++){
                ret[i][j]=f.apply(entryAt(i,j),m2.entryAt(i,j));
            }
        }
        return new FullMatrix(ret);
    }

    //TODO move binary operations to Matrix
    public Matrix multiply(FullMatrix m2){
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
        return new FullMatrix(ret);
    }

    @Override
    public Matrix transpose(){
        NumericValue[][] data=new NumericValue[matrix[0].length][matrix.length];
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[i].length;j++){
                data[j][i]=matrix[i][j];
            }
        }
        return new FullMatrix(data);
    }
    @Override
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

    @Override
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
        return new FullMatrix(ret);
    }

    @Override
    public Matrix applyToAll(Function<NumericValue, NumericValue> operation){
        NumericValue[][] data=new NumericValue[matrix.length][matrix[0].length];
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[i].length;j++){
                data[i][j]=operation.apply(matrix[i][j]);
            }
        }
        return new FullMatrix(data);
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
                NumericValue tmp = matrix[r][c++];
                return tmp==null? Int.ZERO:tmp;
            }
        };
    }

    @NotNull
    @Override
    public Iterator<SparseMatrix.MatrixEntry> matrixIterator() {
        return new Iterator<SparseMatrix.MatrixEntry>() {
            int r=0,c=0;
            SparseMatrix.MatrixEntry next=getNext();

            private SparseMatrix.MatrixEntry getNext() {
                NumericValue nextValue;
                do{
                    nextValue = matrix[r][c++];
                    if(c>=matrix[r].length) {
                        c = 0;
                        r++;
                    }
                }while (nextValue==null&&r<matrix.length);
                return nextValue==null?null:new SparseMatrix.MatrixEntry(BigInteger.valueOf(r),BigInteger.valueOf(c),nextValue);
            }
            @Override
            public boolean hasNext() {
                return next!=null;
            }
            @Override
            public SparseMatrix.MatrixEntry next() {
                SparseMatrix.MatrixEntry tmp = next;
                next=getNext();
                return tmp;
            }
        };
    }

    @Override
    public String toString(BigInteger base, boolean useSmallBase) {
        return toString(s->s.toString(base, useSmallBase));
    }

    @Override
    public String toStringFixedPoint(BigInteger base, Real precision, boolean useSmallBase) {
        return toString(s->s.toStringFixedPoint(base,precision, useSmallBase));
    }

    @Override
    public String toStringFloat(BigInteger base, Real precision, boolean useSmallBase) {
        return toString(s->s.toStringFloat(base,precision, useSmallBase));
    }
    @Override
    public String intsAsString() {
        return toString(MathObject::intsAsString);
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

    //TODO make equals and hash compatible with SparseMatrix/FiniteMap
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FullMatrix)) return false;
        FullMatrix matrix1 = (FullMatrix) o;
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