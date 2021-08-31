package bsoelch.cnl.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
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

    public Matrix(NumericValue[][] matrix) {
        this.matrix = matrix;
        //TODO check bounds
    }

    public NumericValue numericValue() {
        return entryAt(0);//TODO? better Implementation
    }

    public FiniteMap asMap(){
        HashMap<MathObject,MathObject> mapData=new HashMap<>(size());
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix.length;j++){
                mapData.put(new Pair(Real.from(i),Real.from(j)),matrix[i][j]==null? Int.ZERO:matrix[i][j]);
            }
        }
        return FiniteMap.from(mapData,2);
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

    public Matrix fromRange(int i0,int i1){
        NumericValue[][] data=new NumericValue[i1-i0+1][1];
        for(int i=i0;i<=i1;i++){
            data[i][0]=entryAt(i);
        }
        return new Matrix(data);
    }
    public Matrix fromRange(int x0,int y0,int x1,int y1){
        //TODO rangeCheck
        NumericValue[][] data=new NumericValue[x1-x0+1][y1-y0+1];
        for(int i=x0;i<=x1;i++){
            if (y1 + 1 - y0 >= 0)
                System.arraycopy(matrix[i], y0, data[i], y0, y1 + 1 - y0);
        }
        return new Matrix(data);
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