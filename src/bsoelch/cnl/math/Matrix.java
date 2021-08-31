package bsoelch.cnl.math;

import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import static bsoelch.cnl.math.Real.Int;

public final class Matrix implements MathObject{//TODO add to operations in MathObject
    private final NumbericValue[][] matrix;

    static public Matrix identityMatrix(int n){
        return diagonalMatrix(n,Int.ONE);
    }
    static public Matrix diagonalMatrix(int n, NumbericValue value) {
        if(n <=0)throw new IllegalArgumentException("n has to be >0");
        NumbericValue[][] data=new NumbericValue[n][n];
        for(int i = 0; i< n; i++)
            data[i][i]= value;
        return new Matrix(data);
    }

    public Matrix(NumbericValue[][] matrix) {
        this.matrix = matrix;
        //TODO check bounds
    }

    public boolean isInt() {
        for(NumbericValue[] row:matrix){
            for(NumbericValue entry:row){
                if(!entry.isInt())
                    return false;
            }
        }
        return true;
    }

    public boolean isReal() {
        for(NumbericValue[] row:matrix){
            for(NumbericValue entry:row){
                if(!entry.isReal())
                    return false;
            }
        }
        return true;
    }

    public Matrix realPart() {
        return applyToAll(NumbericValue::realPart);
    }

    public Matrix imaginaryPart() {
        return applyToAll(NumbericValue::imaginaryPart);
    }

    public Matrix conjugate() {
        return applyToAll(NumbericValue::conjugate);
    }

    public NumbericValue numericValue() {
        return entryAt(0);//TODO? better Implementation
    }

    public Matrix negate() {
        return applyToAll(NumbericValue::negate);
    }


    public Matrix round(int mode) {
        return applyToAll(s->s.round(mode));
    }


    public Matrix approx(Real precision) {
        return applyToAll(s->s.approx(precision));
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

    private String toString(Function<NumbericValue,String> entryToString){
        StringBuilder str=new StringBuilder("[");
        boolean firstRow=true,firstColumn;
        for(NumbericValue[] row:matrix){
            if(firstRow){
                firstRow=false;
            }else{
                str.append(", ");
            }
            str.append("[");
            firstColumn=true;
            for(NumbericValue s:row){
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

    public NumbericValue entryAt(int i, int j){
        return entryAtInternal(i, j,true);
    }

    NumbericValue entryAtInternal(int i, int j, boolean throwException) {
        if (i >= 0 && i < matrix.length && j >= 0 && j < matrix[i].length) {
            return matrix[i][j] == null ? Int.ZERO : matrix[i][j];
        }else if(throwException){
            throw new IndexOutOfBoundsException("("+i+","+j+") out of Bounds, size:"+matrix.length+" x "+matrix[0].length);
        }else{
            return Int.ZERO;
        }
    }

    public NumbericValue entryAt(int index){
        return entryAt(index% matrix.length,index/ matrix.length);
    }

    public Matrix fromRange(int i0,int i1){
        NumbericValue[][] data=new NumbericValue[i1-i0+1][1];
        for(int i=i0;i<=i1;i++){
            data[i][0]=entryAt(i);
        }
        return new Matrix(data);
    }
    public Matrix fromRange(int x0,int y0,int x1,int y1){
        //TODO rangeCheck
        NumbericValue[][] data=new NumbericValue[x1-x0+1][y1-y0+1];
        for(int i=x0;i<=x1;i++){
            if (y1 + 1 - y0 >= 0)
                System.arraycopy(matrix[i], y0, data[i], y0, y1 + 1 - y0);
        }
        return new Matrix(data);
    }

    public Matrix setEntry(int x, int y, NumbericValue v){
        NumbericValue[][] data=new NumbericValue[Math.max(x+1,matrix.length)][Math.max(y+1,matrix[0].length)];
        data[x][y]=v;
        for(int i=0;i<matrix.length;i++){
            System.arraycopy(matrix[i], 0, data[i], 0, matrix.length);
        }
        return new Matrix(data);
    }
    public Matrix setEntry(int index, NumbericValue v){
        return setEntry(index% matrix.length,index/ matrix.length,v);
    }

    /**Number of Entries in Matrix*/
    public int entryCount(){
        return matrix.length*matrix[0].length;
    }
    /**size of this Matrix as int[]*/
    public int[] size(){
        return new int[]{matrix.length, matrix[0].length};
    }

    public Matrix forEach(Matrix m2, BiFunction<NumbericValue, NumbericValue, NumbericValue> f){
        NumbericValue[][] ret=new NumbericValue[Math.max(matrix.length,m2.matrix.length)][Math.max(matrix[0].length,m2.matrix[0].length)];
        for(int i=0;i<ret.length;i++){
            for(int j=0;j<ret.length;j++){
                ret[i][j]=f.apply(entryAtInternal(i,j,false),m2.entryAtInternal(i,j,false));
            }
        }
        return new Matrix(ret);
    }

    public Matrix add(Matrix m2){
        return forEach(m2, NumbericValue::add);
    }
    public Matrix subtract(Matrix m2){
        return forEach(m2, NumbericValue::subtract);
    }

    public Matrix multiply(Matrix m2){
        NumbericValue[][] ret=new NumbericValue[matrix.length][m2.matrix[0].length];
        int s=Math.min(matrix[0].length,m2.matrix.length);
        NumbericValue v;
        for(int i=0;i<ret.length;i++){
            for(int j=0;j<ret[i].length;j++){
                v= Int.ZERO;
                for(int k=0;k< s;k++){
                    v= NumbericValue.add(v, NumbericValue.multiply(matrix[i][k],m2.matrix[k][j]));
                }
                ret[i][j]=v;
            }
        }
        return new Matrix(ret);
    }

    /**@return  this * m2^-1*/
    public Matrix rDivide(Matrix m2){
        return multiply(m2.invert());
    }

    /**@return  m2^-1 * this */
    public Matrix lDivide(Matrix m2){
        return m2.invert().multiply(this);
    }

    public Matrix transpose(){
        NumbericValue[][] data=new NumbericValue[matrix[0].length][matrix.length];
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[i].length;j++){
                data[j][i]=matrix[i][j];
            }
        }
        return new Matrix(data);
    }

    public NumbericValue sqAbs(){
        if(matrix.length==matrix[0].length){
            NumbericValue ret=determinant();
            return NumbericValue.multiply(ret,ret);
        }else if(matrix.length<matrix[0].length){
            return multiply(transpose()).determinant();
        }else{
            return transpose().multiply(this).determinant();
        }
    }

    public NumbericValue determinant(){
        if(matrix.length!=matrix[0].length)
            throw new ArithmeticException("Determinant of non square matrix");
        NumbericValue[][] data=new NumbericValue[matrix.length][matrix.length];
        for(int i=0;i<matrix.length;i++) {
            data[i]=matrix[i].clone();
        }
        gaussianAlgorithm(data,null,false);
        NumbericValue det= Int.ONE;
        for(int i=0;i<matrix.length;i++) {
            if(data[i][i]==null||data[i][i].equals(Int.ZERO)){
                return Int.ZERO;
            }else{
                det= NumbericValue.multiply(det,data[i][i]);
            }
        }
        return det;
    }

    public Matrix invert(){
        if(matrix.length!=matrix[0].length)
            throw new ArithmeticException("Inversion of non square matrix");
        int s=matrix.length;
        NumbericValue[][] data=new NumbericValue[s][s],ret=new NumbericValue[s][s];
        for(int i=0;i<s;i++) {
            ret[i][i] = Int.ONE;
            data[i]=matrix[i].clone();
        }
        gaussianAlgorithm(data,ret,true);
        for(int i=0;i<s;i++) {
            for(int j=0;j<ret.length;j++){
                ret[j][i]=ret[j][i]==null? Int.ZERO: NumbericValue.divide(ret[j][i],data[i][i]);
            }
        }
        return new Matrix(ret);
    }

    public Matrix applyToAll(Function<NumbericValue, NumbericValue> operation){
        NumbericValue[][] data=new NumbericValue[matrix.length][matrix[0].length];
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[i].length;j++){
                data[i][j]=operation.apply(matrix[i][j]);
            }
        }
        return new Matrix(data);
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

    static void gaussianAlgorithm(NumbericValue[][] target, @Nullable NumbericValue[][] applicant, boolean total) {
        int y=0,k0;
        for (NumbericValue[] numbericValues : target) {
            k0 = -1;
            for (int k = y; k < target.length; k++) {
                if (numbericValues[k]!=null&&!Int.ZERO.equals(numbericValues[k])) {
                    k0 = k;
                }
            }
            if (k0 != -1) {
                if (k0 != y)
                    addLine(target, applicant, k0, numbericValues[k0].invert(), y);
                for (int k = y+1; k < target.length; k++) {
                    if (!Int.ZERO.equals(numbericValues[k])) {
                        addLine(target, applicant, y, NumbericValue.divide(numbericValues[k], numbericValues[y]).negate(), k);
                    }
                }
                if (total) {
                    for (int k = 0; k < y; k++) {
                        if (!Int.ZERO.equals(numbericValues[k])) {
                            addLine(target, applicant, y, NumbericValue.divide(numbericValues[k], numbericValues[y]).negate(), k);
                        }
                    }
                }
                y++;
            }
        }
    }

    /**adds f times line a to line b*/
    private static void addLine(NumbericValue[][] target, @Nullable NumbericValue[][] applicant, int a, NumbericValue f, int b) {
        for(int i=0;i<target.length;i++){
            target[i][b]= NumbericValue.add(target[i][a]==null? Int.ZERO: NumbericValue.multiply(target[i][a],f),
                    target[i][b]==null? Int.ZERO:target[i][b]);
        }
        if(applicant!=null){
            for(int i=0;i<applicant.length;i++){
                applicant[i][b]= NumbericValue.add(applicant[i][a]==null? Int.ZERO: NumbericValue.multiply(applicant[i][a],f),
                        applicant[i][b]==null? Int.ZERO:applicant[i][b]);
            }
        }
    }

}