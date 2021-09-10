package bsoelch.cnl;

import bsoelch.cnl.interpreter.ExecutionEnvironment;
import bsoelch.cnl.math.*;
import bsoelch.cnl.math.expressions.LambdaVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {
    public static final double LOG2_DOUBLE = Math.log(2) ;

    public static final BigInteger BIG_INT_NEG_ONE = BigInteger.valueOf(-1);
    public static final BigInteger BIG_INT_TWO=BigInteger.valueOf(2);
    public static final BigInteger BIG_INT_THREE=BigInteger.valueOf(3);
    public static final BigInteger BIG_INT_TWELVE=BigInteger.valueOf(12);
    public static final BigInteger BIG_INT_SIXTEEN=BigInteger.valueOf(16);
    public static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    public static final BigInteger DEFAULT_BASE = BigInteger.TEN;

    /**Current version of the Binary Output, should be increased every time there are significant changes to the encoding*/
    public static final BigInteger CODE_VERSION = BigInteger.ZERO;

    /**length of BigInt header for argCount in file-header*/
    public static final int FILE_HEADER_INT_HEADER =8;
    /**length of BigInt header for argCount in file-header*/
    public static final int FILE_HEADER_INT_BLOCK =8;
    /**length of BigInt header for argCount in file-header*/
    public static final int FILE_HEADER_INT_BIG_BLOCK =8;

    private Constants(){}

    //0.[BigInt] -> Operator (2,4,4)
    public static final int HEADER_OPERATOR =0b0;
    public static final int HEADER_OPERATOR_LENGTH =1;
    /**length of BigInt header for Operators*/
    public static final int OPERATOR_INT_HEADER=2;
    /**length of BigInt block for Operators*/
    public static final int OPERATOR_INT_BLOCK=4;
    /**length of BigInt bigBlock for Operators*/
    public static final int OPERATOR_INT_BIG_BLOCK=4;
    /**length of BigInt header for N-ary Operators*/
    public static final int NARY_INT_HEADER=4;
    /**length of BigInt block for N-ary Operators*/
    public static final int NARY_INT_BLOCK=8;
    /**length of BigInt bigBlock for N-ary Operators*/
    public static final int NARY_INT_BIG_BLOCK=16;

    //10.[BigInt] -> Var (4,8,16)
    public static final int HEADER_VAR =0b01;
    public static final int HEADER_VAR_LENGTH =2;
    /**length of BigInt header for Variables*/
    public static final int VAR_INT_HEADER=4;
    /**length of BigInt block for Variables*/
    public static final int VAR_INT_BLOCK=8;
    /**length of BigInt bigBlock for Variables*/
    public static final int VAR_INT_BIG_BLOCK=16;

    //1100.[BigInt] -> Int (4,8,16)
    public static final int HEADER_INT =0b0011;
    public static final int HEADER_INT_LENGTH =4;
    /**length of BigInt header for Ints*/
    public static final int INT_HEADER=4;
    /**length of BigInt block for Ints*/
    public static final int INT_BLOCK=8;
    /**length of BigInt bigBlock for Ints*/
    public static final int INT_BIG_BLOCK=16;
    //1101.++++ -> Bracket (0++/1+++)
    public static final int HEADER_BRACKET =0b1011;
    public static final int HEADER_BRACKET_LENGTH =4;

    //Low Bit 0
    public static final int BRACKET_FLAG_LENGTH_EVEN =3;
    public static final int BRACKET_FLAG_IF_NE =0b000;
    public static final int BRACKET_FLAG_WHILE_NE =0b010;
    public static final int BRACKET_FLAG_DO =0b100;
    public static final int BRACKET_FLAG_END =0b110;
    //Low Bit 1
    public static final int BRACKET_FLAG_LENGTH_ODD =4;
    public static final int BRACKET_FLAG_IF_EQ =0b0001;
    public static final int BRACKET_FLAG_WHILE_EQ =0b0011;
    public static final int BRACKET_FLAG_ELIF_EQ =0b0101;
    public static final int BRACKET_FLAG_ELIF_NE =0b0111;
    public static final int BRACKET_FLAG_ELSE =0b1001;
    public static final int BRACKET_FLAG_END_WHILE_EQ =0b1011;
    public static final int BRACKET_FLAG_END_WHILE_NE =0b1101;
    public static final int BRACKET_FLAG_BREAK =0b1111;
    //addLater? TRY,CATCH,THROW
    // move EXIT to brackets?

    //111000.[BigInt] -> Constants
    public static final int HEADER_CONSTANTS = 0b000111;
    public static final int HEADER_CONSTANTS_LENGTH =6;

    /**length of BigInt header for Constants*/
    public static final int CONSTANTS_INT_HEADER=3;
    /**length of BigInt block for Constants*/
    public static final int CONSTANTS_INT_BLOCK=5;
    /**length of BigInt bigBlock for Constants*/
    public static final int CONSTANTS_INT_BIG_BLOCK=16;

    public static final int CONSTANT_RES = 0;
    public static final int CONSTANT_ARG_COUNT = 1;
    public static final int CONSTANT_I = 2;
    public static final int CONSTANT_EXIT = 3;
    public static final int CONSTANT_EMPTY_SET = 4;
    public static final int CONSTANT_EMPTY_MAP = 5;

    //111001.[BigInt][BigInt] -> Fraction (4,8,16)
    public static final int HEADER_FRACTION =0b100111;
    public static final int HEADER_FRACTION_LENGTH =6;

    //111010.[BigInt] -> Function(Arg) (4,8,16)
    public static final int HEADER_FUNCTION_ARG = 0b010111;
    public static final int HEADER_FUNCTION_ARG_LENGTH = 6;
    /**length of BigInt header for Function arguments*/
    public static final int FUNCTION_ARG_INT_HEADER =4;
    /**length of BigInt block for Function arguments*/
    public static final int FUNCTION_ARG_INT_BLOCK =8;
    /**length of BigInt bigBlock for Function arguments*/
    public static final int FUNCTION_ARG_INT_BIG_BLOCK =16;
    /**length of BigInt header for Function ids*/
    public static final int FUNCTION_ID_INT_HEADER =4;
    /**length of BigInt block for Function ids*/
    public static final int FUNCTION_ID_INT_BLOCK =8;
    /**length of BigInt bigBlock for Function ids*/
    public static final int FUNCTION_ID_INT_BIG_BLOCK =16;

    //111011.[BigInt] -> Environment (4,8,16)
    public static final int HEADER_ENVIRONMENT = 0b110111;
    public static final int HEADER_ENVIRONMENT_LENGTH = 6;
    /**length of BigInt header for Environments*/
    public static final int ENVIRONMENTS_INT_HEADER=4;
    /**length of BigInt block for Environments*/
    public static final int ENVIRONMENTS_INT_BLOCK=8;
    /**length of BigInt bigBlock for Environments*/
    public static final int ENVIRONMENTS_INT_BIG_BLOCK=16;
    //11110000.[BigInt] -> Import (4,8,16)
    public static final int HEADER_IMPORT = 0b00001111;
    public static final int HEADER_IMPORT_LENGTH = 8;

    //11110001.[BigInt] -> Function(Declaration) (4,8,16)
    public static final int HEADER_FUNCTION_DECLARATION = 0b00101111;
    public static final int HEADER_FUNCTION_DECLARATION_LENGTH = 8;

    //11110010.[BigInt]([BigInt])+ -> Lambda Header   Lambda:N [expr(LambdaArg(0),...LambdaArg(N-1)]
    public static final int HEADER_LAMBDA = 0b01001111;
    public static final int HEADER_LAMBDA_LENGTH = 8;
    /**length of BigInt header for argCount of Lambda-Expression*/
    public static final int LAMBDA_COUNT_INT_HEADER=2;
    /**length of BigInt block for argCount of Lambda-Expression*/
    public static final int LAMBDA_COUNT_INT_BLOCK=2;
    /**length of BigInt bigBlock for argCount of Lambda-Expression*/
    public static final int LAMBDA_COUNT_INT_BIG_BLOCK=4;
    //11110011.[BigInt] -> Lambda Argument
    public static final int HEADER_LAMBDA_VARIABLE = 0b01101111;
    public static final int HEADER_LAMBDA_VARIABLE_LENGTH = 8;
    /**length of BigInt header for Lambda-Variable*/
    public static final int LAMBDA_VAR_INT_HEADER=4;
    /**length of BigInt block for Lambda-Variable*/
    public static final int LAMBDA_VAR_INT_BLOCK=8;
    /**length of BigInt bigBlock for Lambda-Variable*/
    public static final int LAMBDA_VAR_INT_BIG_BLOCK=16;
    //11110100.[BigInt] -> Operator-reference &OP -> Lambda:[ArgCount] OP LambdaArg(0) ... LambdaArg(N-1)
    public static final int HEADER_OPERATOR_REFERENCE = 0b10001111;
    public static final int HEADER_OPERATOR_REFERENCE_LENGTH = 8;

    //11110101.[outID]{BASE} ->Out
    public static final int HEADER_OUT = 0b10101111;
    public static final int HEADER_OUT_LENGTH = 8;
    //11110110.[outID]{BASE} ->Out_NewLine
    public static final int HEADER_OUT_NEW_LINE = 0b11001111;
    public static final int HEADER_OUT_NEW_LINE_LENGTH = 8;

    public static final int IO_INT_HEADER =6;//3-34 in one block
    public static final int IO_INT_BLOCK =8;
    public static final int IO_INT_BIG_BLOCK =16;
    public static final BigInteger OUT_BASE_OFFSET=BIG_INT_THREE;//smallest base without baseType

    public static final int OUT_BIG_BASE_START=25;
    public static final int OUT_STR =50;
    public static final int OUT_STR_INT=51;

    public static final int OUT_NUMBER_BLOCK_LENGTH=5;
    public static final int OUT_FLAG_FRACTION=0;
    public static final int OUT_FLAG_FIXED_POINT_APPROX =1;
    public static final int OUT_FLAG_FLOAT_APPROX =2;
    public static final int OUT_FLAG_FIXED_POINT_EXACT=3;
    public static final int OUT_FLAG_FLOAT_EXACT=4;
    public static final int OUT_BASE_FLAG_DEC=0;
    public static final int OUT_BASE_FLAG_BIN=1;
    public static final int OUT_BASE_FLAG_DOZ=2;
    public static final int OUT_BASE_FLAG_HEX=3;
    public static final int OUT_BASE_FLAG_BASE_N=4;

    //11110111.++ ->In
    public static final int HEADER_IN = 0b11101111;
    public static final int HEADER_IN_LENGTH = 8;

    public static final int IN_TYPES_LENGTH = 3;
    public static final int IN_TYPE_CHAR = 0b000;
    public static final int IN_TYPE_WORD= 0b001;
    public static final int IN_TYPE_LINE = 0b010;
    public static final int IN_TYPE_BIN = 0b011;
    public static final int IN_TYPE_DEC = 0b100;
    public static final int IN_TYPE_DOZ = 0b101;
    public static final int IN_TYPE_HEX = 0b110;
    public static final int IN_TYPE_BASE_N = 0b111;

    public static class Operators {



        private Operators(){}
        public static final int MODIFY_ARG0_ALWAYS=1, MODIFY_ARG0_ROOT=0,MODIFY_ARG0_NEVER=-1;

        /** -A */
        public static final String NEGATE = "NEGATE";
        /**Var:N := A*/
        public static final String WRITE_VAR = "WRITE_VAR";
        /**Var[A]*/
        public static final String DYNAMIC_VAR = "DYNAMIC_VAR";
        /**Var[A] := B*/
        public static final String WRITE_DYNAMIC_VAR = "WRITE_DYNAMIC_VAR";
        /**A+Bi*/
        public static final String COMPLEX = "COMPLEX";
        /**calls function [A]*/
        public static final String CALL_FUNCTION = "CALL";
        /**trinary if operator A?B:C*/
        public static final String IF = "IF";
        /**creates a new set for the following elements (nary, minArgs: 3)*/
        public static final String NEW_SET = "NEW_SET";
        /**creates a new Tuple for the following elements (nary, minArgs: 3)*/
        public static final String NEW_TUPLE = "NEW_TUPLE";
        /**creates a new map for the following elements, with each pair of two consecutive Elements being a key-value pair (nary, minArgs: 4)*/
        public static final String NEW_MAP = "NEW_MAP";
        /**creates a new sparse-tuple for the following elements, with each pair of two consecutive Elements being a key-value pair (nary, minArgs: 2)*/
        public static final String SPARSE_TUPLE = "SPARSE_TUPLE";

        public static final OperatorInfo ID=new OperatorInfo(-1,"ID",MODIFY_ARG0_NEVER,false,1,args->args[0], OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);

        public static final class OperatorInfo{
            public static final int LAMBDA_FLAG_ALLOW_ALL=2,LAMBDA_FLAG_ALLOW_BOUND=1;
            final public int id;
            final public String name;
            final public int storeMode;
            final public boolean isNary;
            final public int lambdaFlags;
            final public int minArgs;
            final private Function<MathObject[],MathObject> eval;
            final private BiFunction<ExecutionEnvironment,MathObject[],MathObject> evalEnv;

            private OperatorInfo(int id, String name, int storeMode, boolean isNary, int minArgs, Function<MathObject[], MathObject> eval, int lambdaFlags) {
                this.id = id;
                this.name = name;
                this.storeMode=storeMode;
                this.isNary=isNary;
                this.minArgs = minArgs;
                this.eval=eval;
                this.lambdaFlags=lambdaFlags;
                this.evalEnv=null;
            }
            private OperatorInfo(int id, String name,int storeMode,boolean isNary,int minArgs,BiFunction<ExecutionEnvironment,MathObject[],MathObject> eval) {
                this.id = id;
                this.name = name;
                this.storeMode=storeMode;
                this.isNary=isNary;
                this.minArgs = minArgs;
                this.eval=null;
                this.evalEnv=eval;
                lambdaFlags =0;
            }

            public boolean isRuntimeOperator(){
                return eval==null&&evalEnv==null;
            }

            public boolean needsEnvironment(){
                return eval==null&&evalEnv!=null;
            }

            /**Executes this Operator for the given Arguments
             * @param env ExecutionEnvironment in which the Operator should be executed
             * @param args Arguments for the Operator*/
            public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                if(args.length<minArgs)
                    throw new IllegalArgumentException("Missing Argument: "+name+" needs at least "+minArgs+" Arguments");
                if((!isNary)&&args.length>minArgs)
                    throw new IllegalArgumentException("Too much Arguments: "+name+" needs at most "+minArgs+" Arguments");
                if(evalEnv!=null){
                    return evalEnv.apply(env,args);
                }else{
                    assert eval != null;
                    if((lambdaFlags&LAMBDA_FLAG_ALLOW_ALL)!=0){
                        return eval.apply(args);
                    }else {
                        boolean hasExpression = false,unwrapBoundLambdas=(lambdaFlags&LAMBDA_FLAG_ALLOW_BOUND)==0;
                        for (MathObject o : args) {
                            if (o instanceof LambdaExpression) {
                                if(unwrapBoundLambdas||!o.freeVariables().isEmpty()) {
                                    hasExpression=true;
                                    break;
                                }
                            }
                        }
                        if (hasExpression) {
                            return LambdaExpression.from(this, args);
                        } else {
                            return eval.apply(args);
                        }
                    }
                }
            }
        }

        private static final HashMap<OperatorInfo, NAryInfo> nAryInfos=new HashMap<>();
        public static final class NAryInfo{
            public final MathObject nilaryReplacement;
            final HashMap<Integer,OperatorInfo> shortCuts=new HashMap<>();
            public final OperatorInfo nAryVariant;
            public final boolean isCommutative,isAssociative;

            public NAryInfo(MathObject nilaryReplacement, OperatorInfo nAryVariant, boolean isCommutative, boolean isAssociative) {
                this.nilaryReplacement = nilaryReplacement;
                this.nAryVariant = nAryVariant;
                if(nAryInfos.put(nAryVariant,this)!=null){
                    throw new RuntimeException("There already is a NAry info for the specific name");
                }
                this.isCommutative = isCommutative;
                this.isAssociative = isAssociative;
            }

            public @Nullable OperatorInfo getShortCut(int argCount) {
                return shortCuts.get(argCount);
            }

            public void addShortCut(OperatorInfo shortCut) {
                if(shortCut.isNary)
                    throw new RuntimeException("Cannot declare a Nary operator as an shortcut");
                if(shortCuts.put(shortCut.minArgs,shortCut)!=null)
                    throw new RuntimeException("shortcut for "+shortCut.minArgs+" arguments is already defined");
                if(shortCut!=ID&&nAryInfos.put(shortCut,this)!=null){
                    throw new RuntimeException("shortcut "+shortCut.name+" is already attached to another NAryInfo");
                }
            }
        }

        private static boolean operatorsInitialized=false;

        /**ensures that all Operators are initialized*/
        private static void ensureOperatorsInitialized(){
            synchronized (operators){
                if(operatorsInitialized)
                    return;
                //set true at start to allow the usage of byName()  in this function
                operatorsInitialized=true;
                //3bit Operators

                //negates A (element-wise)
                declareUnaryOperator(NEGATE, "NEG", MODIFY_ARG0_ROOT, MathObject::negate, 0);
                //replaces A with !A (1 if A==0 else 0) (element-wise)
                declareUnaryOperator("NOT", null, MODIFY_ARG0_ROOT, MathObject::not, 0);
                //7bit operators

                //inverts A (element-wise)
                declareUnaryOperator("INVERT", "INV", MODIFY_ARG0_ROOT, a->MathObject.deepReplaceNumbers(a, NumericValue::invert), 0);
                //returns 1 if A and B are equal 0 otherwise
                declareBinaryOperator("EQUAL", "EQ", MODIFY_ARG0_NEVER,
                        (a,b)-> a.equals(b)? Real.Int.ONE:Real.Int.ZERO, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                //returns 1 if A and B are not equal 0 otherwise
                declareBinaryOperator("NOT_EQUAL", "NE", MODIFY_ARG0_NEVER,
                        (a,b)-> a.equals(b)?Real.Int.ZERO:Real.Int.ONE, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                //returns 1 if A > B, 0 otherwise
                declareBinaryOperator("GREATER_THAN", "GT", MODIFY_ARG0_NEVER,
                                (a,b)-> MathObject.compare(a,b)>0?Real.Int.ONE:Real.Int.ZERO, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                //returns 1 if A >= B, 0 otherwise
                declareBinaryOperator("GREATER_EQUAL", "GE", MODIFY_ARG0_NEVER,
                                (a,b)-> MathObject.compare(a,b)>=0?Real.Int.ONE:Real.Int.ZERO, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                //adds A and B (element-wise)
                OperatorInfo add=declareBinaryOperator("ADD", null, MODIFY_ARG0_ROOT, MathObject::add, 0);
                //subtracts B from A (element-wise)
                declareBinaryOperator("SUBTRACT", "SUBT", MODIFY_ARG0_ROOT, MathObject::subtract, 0);
                //multiplies A and B (element-wise)
                OperatorInfo mult=declareBinaryOperator("MULTIPLY", "MULT", MODIFY_ARG0_ROOT, MathObject::multiply, 0);
                //divides A by B (element-wise)
                declareBinaryOperator("DIVIVDE", "DIV", MODIFY_ARG0_ROOT, MathObject::divide, 0);
                //calculates the int-value (floor A/B if both real otherwise round A/B) of A divided by B (element-wise)
                declareBinaryOperator("INT_DIVIDE", "INT_DIV", MODIFY_ARG0_ROOT,
                        (a,b)->MathObject.deepCombineNumbers(a,b,(x, y)-> NumericValue.divideAndRemainder(x,y)[0]), 0);
                //calculates the remainder of A divided by B (element-wise)
                declareBinaryOperator("REMAINDER", null, MODIFY_ARG0_ROOT,
                        (a,b)->MathObject.deepCombineNumbers(a,b,(x, y)-> NumericValue.divideAndRemainder(x,y)[1]), 0);
                //shortcut of A+Bi (to simplify creation of complex numbers)
                declareBinaryOperator(COMPLEX, "CMPLX", MODIFY_ARG0_NEVER,(a, b)->MathObject.deepCombineNumbers(a,b,
                        (l,r)->NumericValue.add(l, NumericValue.multiply(r, Complex.I))), 0);
                //2-norm of the given MathObject
                declareUnaryOperator("SQUARE_ABS", "SQ_ABS", MODIFY_ARG0_ROOT, MathObject::sqAbs, 0);
                //real part of A (element-wise)
                declareUnaryOperator("REAL_PART", "RE", MODIFY_ARG0_ROOT, a->MathObject.deepReplaceNumbers(a, NumericValue::realPart), 0);
                //imaginary part of A (element-wise)
                declareUnaryOperator("IMAGINARY_PART", "IM", MODIFY_ARG0_ROOT, a->MathObject.deepReplaceNumbers(a, NumericValue::imaginaryPart), 0);
                //complex conjugate of A (element-wise)
                declareUnaryOperator("CONJUGATE", "CONJ", MODIFY_ARG0_ROOT, a->MathObject.deepReplaceNumbers(a, NumericValue::conjugate), 0);

                //9bit operators

                //writes A to a variable an returns A
                declareRuntimeOperator(WRITE_VAR,1);
                //bitwise logical-and of A and floor B (element-wise)
                OperatorInfo and=declareBinaryOperator("AND", null, MODIFY_ARG0_ROOT, MathObject::floorAnd, 0);
                //bitwise logical-or of A and floor B (element-wise)
                OperatorInfo or=declareBinaryOperator("OR", null, MODIFY_ARG0_ROOT, MathObject::floorOr, 0);
                //bitwise logical-xor of A and floor B (element-wise)
                declareBinaryOperator("XOR", null, MODIFY_ARG0_ROOT, MathObject::floorXor, 0);
                //bitwise logical-andNot of A and floor B (element-wise)
                declareBinaryOperator("AND_NOT", null, MODIFY_ARG0_ROOT, MathObject::floorAndNot, 0);
                //increments A (element-wise) if A is a variable the value of A is changed
                declareUnaryOperator("INCREMENT", "INC", MODIFY_ARG0_ALWAYS, a-> MathObject.add(a,Real.Int.ONE), 0);
                //decrements A (element-wise) if A is a variable the value of A is changed
                declareUnaryOperator("DECREMENT", "DEC", MODIFY_ARG0_ALWAYS, a-> MathObject.subtract(a,Real.Int.ONE), 0);
                //(element-wise) floor A
                declareUnaryOperator("FLOOR", null, MODIFY_ARG0_ROOT,
                                a-> MathObject.round(a, MathObject.FLOOR), 0);
                //(element-wise) ciel A
                declareUnaryOperator("CIEL", null, MODIFY_ARG0_ROOT,
                                a-> MathObject.round(a, MathObject.CIEL), 0);
                //(element-wise) round A
                declareUnaryOperator("ROUND", null, MODIFY_ARG0_ROOT,
                                a-> MathObject.round(a, MathObject.ROUND), 0);
                //1 if A>0, 0 otherwise
                declareUnaryOperator("GREATER_THAN0", "GT0", MODIFY_ARG0_NEVER,
                        a-> MathObject.compare(a,Real.Int.ZERO)>0?Real.Int.ONE:Real.Int.ZERO,
                        OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                //1 if A>=0, 0 otherwise
                declareUnaryOperator("GREATER_EQUAL0", "GE0", MODIFY_ARG0_NEVER,
                                a-> MathObject.compare(a,Real.Int.ZERO)>=0?Real.Int.ONE:Real.Int.ZERO,
                        OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                //min A,B
                OperatorInfo min=declareBinaryOperator("MIN", null, MODIFY_ARG0_ROOT, MathObject::min,
                        OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                //max A,B
                OperatorInfo max=declareBinaryOperator("MAX", null, MODIFY_ARG0_ROOT, MathObject::max,
                        OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                //calls a function
                declareRuntimeOperator(CALL_FUNCTION,0);
                //replaces A with a simpler Fraction with distance to A at most real B
                declareBinaryOperator("APPROXIMATE", "APPROX", MODIFY_ARG0_ROOT,
                        (a,b)-> MathObject.deepReplaceNumbers(a, e->e.approx(b.numericValue().realPart())), 0);

                //13bit Operators

                //trinary if-Operator isTrue(args[0])? args[1] :args[2]
                declareOperator(IF, null, 3, false, MODIFY_ARG0_NEVER, (args)->
                        MathObject.isTrue(args[0])?args[1]:args[2],OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                //A <sup>floor real B</sup> (element-wise)
                declareBinaryOperator("POW", null, MODIFY_ARG0_ROOT, MathObject::pow, 0);
                //replaces all fractions in A with their numerator
                declareUnaryOperator("NUMERATOR", "NUM", MODIFY_ARG0_ROOT,
                        (a)-> MathObject.deepReplaceNumbers(a,NumericValue::numerators), 0);
                //replaces all fractions in A with their denominator
                declareUnaryOperator("DENOMINATOR", "DEN", MODIFY_ARG0_ROOT,
                        (a)-> MathObject.deepReplaceNumbers(a,NumericValue::denominators), 0);
                //replaces all reals in A with their sign
                declareUnaryOperator("SIGNUM", "SGN", MODIFY_ARG0_ROOT,
                        (a)-> MathObject.deepReplaceNumbers(a,NumericValue::signum), 0);
                //returns a Pair q,r with A=q*B+r and  sqrAbs r < sqrAbs B (element-wise)
                declareBinaryOperator("DIVIDE_AND_REMAINDER", "DIV_REM", MODIFY_ARG0_ROOT,
                        (a,b)-> MathObject.deepCombine(a,b,(x, y)->{
                            NumericValue[] tmp=NumericValue.divideAndRemainder(x,y);
                            return new Pair(tmp[0],tmp[1]);
                        }), 0);
                //greatest common divisor of A and B (element-wise)
                declareBinaryOperator("GCD", null, MODIFY_ARG0_ROOT,
                        (a,b)-> MathObject.deepCombineNumbers(a,b,NumericValue::gcd), 0);
                //least common multiple of A and B (element-wise)
                declareBinaryOperator("LCM", null, MODIFY_ARG0_ROOT,
                        (a,b)-> MathObject.deepCombineNumbers(a,b,NumericValue::lcm), 0);

                //addLater MIN_MAX
                // CONCAT_BINARY

                //A (+) B with a/b(+)c/d=(a+c)/(b+d) (element-wise)
                declareBinaryOperator("F_ADD", null, MODIFY_ARG0_ROOT,
                        MathObject::fAdd, 0);
                //F2X_MULT

                //replaces each Number A with bigLen(floor re A)) (element-wise)
                declareUnaryOperator("BIT_LENGTH", null, MODIFY_ARG0_ROOT,
                        MathObject::bitLength, 0);
                //replaces A with with the Ath variable in the current context
                declareRuntimeOperator(DYNAMIC_VAR,1);
                //writes B to Var[A] returns B
                declareRuntimeOperator(WRITE_DYNAMIC_VAR,2);
                //LambdaExpressions
                {
                    //binds all free vars in A
                    declareUnaryOperator("BIND_ALL", null, MODIFY_ARG0_NEVER,
                            (a) -> {
                                if(a instanceof LambdaExpression){
                                    LambdaVariable[] bind=a.freeVariables().toArray(new LambdaVariable[0]);
                                    return LambdaExpression.from(a,bind);
                                }else{
                                    return a;
                                }
                            }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                    {
                        //evaluates the lambda-expression A at B
                        OperatorInfo unEval=declareBinaryOperator("EVAL", null, MODIFY_ARG0_NEVER,
                                (a,b) -> {
                                    if(a instanceof LambdaExpression){
                                        return ((LambdaExpression)a).evaluate(new MathObject[]{b});
                                    }else{
                                        return a;
                                    }
                                }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                        //evaluates the lambda-expression args[0] at args[1] and then at args[2]
                        OperatorInfo biEval=declareOperator("BI_EVAL", null, 3,false,MODIFY_ARG0_NEVER,
                                (args) -> {
                                    if(args[0] instanceof LambdaExpression){
                                        return ((LambdaExpression)args[0]).evaluate(Arrays.copyOfRange(args,1,args.length));
                                    }else{
                                        return args[0];
                                    }
                                }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                        //evaluates the lambda-expression args[0] at args[1],args[2],...
                        OperatorInfo eval=declareOperator("N_EVAL", null, 4, true, MODIFY_ARG0_NEVER,
                                (args) -> {
                                    if (args[0] instanceof LambdaExpression) {
                                        return ((LambdaExpression) args[0]).evaluate(Arrays.copyOfRange(args, 1, args.length));
                                    } else {
                                        return args[0];
                                    }
                                }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,eval,true,false);
                        info.addShortCut(unEval);
                        info.addShortCut(biEval);
                    }
                    //evaluates the lambda-expression A with the elements of the Tuple B as arguments
                    declareBinaryOperator("TUPLE_EVAL", null, MODIFY_ARG0_NEVER,
                            (a,b) -> {
                                if(a instanceof LambdaExpression){
                                    return ((LambdaExpression)a).evaluate(MathObject.asTuple(b).toArray());
                                }else{
                                    return a;
                                }
                            }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                    //replaces all elements to the given MathObject with a their result in the given LambdaFunction
                    declareBinaryOperator("REPLACE_ALL", null, MODIFY_ARG0_NEVER,
                            (a,b) -> {
                                if(b instanceof LambdaExpression){
                                    return MathObject.replaceAll(a,e->((LambdaExpression)b).evaluate(new MathObject[]{e}));
                                }else{
                                    return MathObject.replaceAll(a,e->b);
                                }
                            }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                    //replaces all elements to the given MathObject with a their result in the given LambdaFunction
                    //if the argument is a set the result is a map that maps each element to its value under the given function
                    declareBinaryOperator("MAP_ALL", null, MODIFY_ARG0_NEVER,
                            (a,b) -> {
                                if(b instanceof LambdaExpression){
                                    return MathObject.mapAll(a,e->((LambdaExpression)b).evaluate(new MathObject[]{e}));
                                }else{
                                    return MathObject.mapAll(a,e->b);
                                }
                            }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                    //combines the elements to two MathObjects with a Binary LambdaFunction
                    declareOperator("COMBINE", null, 3,false,MODIFY_ARG0_NEVER,
                            (args) -> {
                                if(args[2] instanceof LambdaExpression){
                                    return MathObject.combine(args[0],args[1],
                                            (e,f)->((LambdaExpression)args[2]).evaluate(new MathObject[]{e,f}));
                                }else{
                                    return MathObject.combine(args[0],args[1],(e,f)->args[2]);
                                }
                            }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                    //(LtR) reduction of the MathObject A with the lambda-expression B
                    declareBinaryOperator("REDUCE", null, MODIFY_ARG0_NEVER,
                            (a,b) -> {
                                if(b instanceof LambdaExpression){
                                    return MathObject.reduce(a,null,(e,f)->((LambdaExpression)b).evaluate(new MathObject[]{e,f}),false);
                                }else{
                                    return MathObject.reduce(a,null,(e,f)->b,false);
                                }
                            }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                    //deep (LtR) reduction of the MathObject A with the lambda-expression B
                    declareBinaryOperator("DEEP_REDUCE", null, MODIFY_ARG0_NEVER,
                            (a,b) -> {
                                if(b instanceof LambdaExpression){
                                    return MathObject.reduce(a,null,(e,f)->((LambdaExpression)b).evaluate(new MathObject[]{e,f}),true);
                                }else{
                                    return MathObject.reduce(a,null,(e,f)->b,true);
                                }
                            }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                    //addLater REDUCE_RTL
                    //DEEP_REDUCE_RTL
                }
                //Strings
                OperatorInfo strConcat;
                {
                    //length of A as String
                    declareUnaryOperator("STRING_LENGTH", null, MODIFY_ARG0_ROOT,
                                    (a) -> Real.Int.from(a.asString().length()), 0);
                    //concatenates the Strings A and B
                    strConcat=declareBinaryOperator("STRING_CONCAT", null, MODIFY_ARG0_ROOT, MathObject::strConcat, 0);
                    //returns the string representation of A in base B
                    declareBinaryOperator("TO_STRING", null, MODIFY_ARG0_ROOT,
                                    (a, b) -> Real.from(Real.stringAsBigInt(
                                            a.toString(MathObject.round(b, MathObject.FLOOR).numericValue()
                                                    .realPart().num(), true))), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //returns the fixed point string representation of A in base B
                    declareBinaryOperator("TO_STRING_FIXED", null, MODIFY_ARG0_ROOT,
                            (a, b) -> Real.from(Real.stringAsBigInt(
                                    a.toStringFixedPoint(MathObject.round(b, MathObject.FLOOR).numericValue().realPart().num(),
                                            Real.Int.ZERO, true))), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //returns the floating point string representation of A in base B
                    declareBinaryOperator("TO_STRING_FLOAT", null, MODIFY_ARG0_ROOT,
                            (a, b) -> Real.from(Real.stringAsBigInt(
                                    a.toStringFloat(MathObject.round(b, MathObject.FLOOR).numericValue().realPart().num(),
                                            Real.Int.ZERO,true))), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //returns the fixed point string representation of args[0] in base args[1] with precision args[2]
                    declareOperator("TO_STRING_FIXED_APPROX", null, 3,false,MODIFY_ARG0_ROOT,
                            (args) -> Real.from(Real.stringAsBigInt(
                                    args[0].toStringFixedPoint(MathObject.round(args[1], MathObject.FLOOR).numericValue().realPart().num(),
                                            args[2].numericValue().realPart(), true))), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //returns the floating point string representation of args[0] in base args[1] with precision args[2]
                    declareOperator("TO_STRING_FLOAT_APPROX", null, 3,false,MODIFY_ARG0_ROOT,
                            (args) -> Real.from(Real.stringAsBigInt(
                                    args[0].toStringFloat(MathObject.round(args[1], MathObject.FLOOR).numericValue().realPart().num(),
                                            args[2].numericValue().realPart(),true))), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //addLater? shortcuts for bases 2,10,12,16 / out static-base
                    //creates a MathObject from the String A in base B
                    declareBinaryOperator("FROM_STRING", null, MODIFY_ARG0_ROOT,
                                    (a, b) -> MathObject.FromString.safeFromString(
                                            a.asString(), MathObject.round(b, MathObject.FLOOR)
                                                    .numericValue().realPart().num()), 0);

                    declareBinaryOperator("STRING_COMPARE", null, MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().compareTo(b.asString())), 0);
                    declareUnaryOperator("STRING_LOWERCASE", null, MODIFY_ARG0_ROOT,
                                    (a) -> Real.from(Real.stringAsBigInt(a.asString().toLowerCase(Locale.ROOT))), 0);
                    declareUnaryOperator("STRING_UPPERCASE", null, MODIFY_ARG0_ROOT,
                                    (a) -> Real.from(Real.stringAsBigInt(a.asString().toUpperCase(Locale.ROOT))), 0);
                    declareBinaryOperator("STRING_STARTS_WITH", null, MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().startsWith(b.asString())?1:0), 0);
                    declareBinaryOperator("STRING_ENDS_WITH", null, MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().endsWith(b.asString())?1:0), 0);
                    declareBinaryOperator("STRING_CONTAINS", null, MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().contains(b.asString())?1:0), 0);
                    declareBinaryOperator("STRING_STARTS_WITH_IGNORE_CASE", null, MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().toLowerCase(Locale.ROOT)
                                            .startsWith(b.asString().toLowerCase(Locale.ROOT))?1:0), 0);
                    declareBinaryOperator("STRING_ENDS_WITH_IGNORE_CASE", null, MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().toLowerCase(Locale.ROOT)
                                            .endsWith(b.asString().toLowerCase(Locale.ROOT))?1:0), 0);
                    declareBinaryOperator("STRING_CONTAINS_IGNORE_CASE", null, MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().toLowerCase(Locale.ROOT)
                                            .contains(b.asString().toLowerCase(Locale.ROOT))?1:0), 0);
                    declareBinaryOperator("STRING_INDEX_OF", null, MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().indexOf(b.asString())), 0);
                    declareBinaryOperator("SUBSTRING_FROM", null, MODIFY_ARG0_ROOT,
                                    (a,i) -> Real.from(Real.stringAsBigInt(a.asString()
                                            .substring(i.numericValue().realPart().num().intValueExact()))), 0);
                    declareBinaryOperator("SUBSTRING_TO", null, MODIFY_ARG0_ROOT,
                                    (a,i) -> Real.from(Real.stringAsBigInt(a.asString()
                                            .substring(0,i.numericValue().realPart().num().intValueExact()+1))), 0);
                    declareOperator("SUBSTRING", null, 3, false, MODIFY_ARG0_ROOT,
                            (args)->{
                                String str = args[0].asString();
                                try {
                                    return Real.from(Real.stringAsBigInt(str.substring(
                                            args[1].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact(),
                                            args[2].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact())));
                                }catch (IndexOutOfBoundsException oob){
                                    //rethrow as Arithmetic exception for handling as CNL_RuntimeException in Interpreter
                                    throw new ArithmeticException(oob.getMessage());
                                }
                            }, 0);


                    //creates a Tuple containing all the (Unicode) characters in arg
                    declareUnaryOperator("STRING_CHARS", null, MODIFY_ARG0_ROOT,
                            (arg)->{
                                String str = arg.asString();
                                int[] cps=str.codePoints().toArray();
                                MathObject[] chars=new MathObject[cps.length];
                                for(int i=0;i<chars.length;i++){
                                    chars[i]=Real.from(Real.stringAsBigInt(new String(Character.toChars(cps[i]))));
                                }
                                return Tuple.create(chars);
                            }, 0);
                    //splits the supplied string at the given regex (like String.split())
                    declareBinaryOperator("STRING_SPLIT", null, MODIFY_ARG0_ROOT,
                            (arg,regex)->{
                                String[] parts = arg.asString().split(regex.asString());
                                MathObject[] chars=new MathObject[parts.length];
                                for(int i=0;i<chars.length;i++){
                                    chars[i]=Real.from(Real.stringAsBigInt(parts[i]));
                                }
                                return Tuple.create(chars);
                            }, 0);
                    //removes all occurrences of the given regex from the String arg
                    //has the same effect as STRING JOIN STRING_SPLIT arg regex ''
                    declareBinaryOperator("STRING_REMOVE_ALL", null, MODIFY_ARG0_ROOT,
                            (arg,regex)->{
                                String str = arg.asString();
                                String[] parts = str.split(regex.asString());
                                StringBuilder newString=new StringBuilder(str.length());
                                for (String part : parts) {
                                    newString.append(part);
                                }
                                return Real.from(Real.stringAsBigInt(newString.toString()));
                            }, 0);
                    //Tuple of all occurrences of the given regex in the string arg
                    declareBinaryOperator("STRING_FIND_ALL", null, MODIFY_ARG0_ROOT,
                            (arg,regex)->{
                                ArrayList<String> matches=new ArrayList<>();
                                Matcher m = Pattern.compile(regex.asString())
                                        .matcher(arg.asString());
                                while (m.find()) {
                                    matches.add(m.group());
                                }
                                String[] parts = matches.toArray(new String[0]);
                                MathObject[] chars=new MathObject[parts.length];
                                for(int i=0;i<chars.length;i++){
                                    chars[i]=Real.from(Real.stringAsBigInt(parts[i]));
                                }
                                return Tuple.create(chars);
                            }, 0);
                    //collects all occurrences of the given regex from the String arg
                    //has the same effect as STRING JOIN STRING_FIND_ALL arg regex ''
                    declareBinaryOperator("STRING_FILTER", null, MODIFY_ARG0_ROOT,
                            (arg,regex)->{
                                String str = arg.asString();
                                StringBuilder newString=new StringBuilder(str.length());
                                Matcher m = Pattern.compile(regex.asString())
                                        .matcher(str);
                                while (m.find()) {
                                    newString.append(m.group());
                                }
                                return Real.from(Real.stringAsBigInt(newString.toString()));
                            }, 0);
                    //joins all the elements of strings (as Strings) with the separator connect
                    declareBinaryOperator("STRING_JOIN", null, MODIFY_ARG0_ROOT,
                            (strings,connect)->{
                                String connector= connect.asString();
                                StringBuilder res=new StringBuilder();
                                MathObject.forEachElement(strings,e->{
                                    res.append(e.asString());
                                    res.append(connector);
                                });
                                res.setLength(res.length()-connector.length());//remove last connector
                                return Real.from(Real.stringAsBigInt(res.toString()));
                            }, 0);

                }
                //sets/tuples/maps
                OperatorInfo cut,unite,times,tupleConcat;
                {
                    //Type Conversion
                    declareUnaryOperator("NUMERIC_VALUE", null, MODIFY_ARG0_ROOT,
                                    MathObject::numericValue, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("AS_SET", null, MODIFY_ARG0_ROOT,
                                    MathObject::asSet, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("AS_MAP", null, MODIFY_ARG0_ROOT,
                                    MathObject::asMap, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("AS_MATRIX", null, MODIFY_ARG0_ROOT,
                                    Matrix::asMatrix, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("AS_TUPLE", null, MODIFY_ARG0_ROOT,
                            MathObject::asTuple, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("NONZERO_ELEMENTS", null, MODIFY_ARG0_ROOT,
                            MathObject::nonzeroElements, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //Type Checking
                    declareUnaryOperator("IS_INTEGER", null, MODIFY_ARG0_ROOT,
                                    o-> (o instanceof Real.Int?Real.Int.ONE:Real.Int.ZERO), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("IS_NUMERIC", null, MODIFY_ARG0_ROOT,
                                    o-> (o instanceof NumericValue?Real.Int.ONE:Real.Int.ZERO), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("IS_SET", null, MODIFY_ARG0_ROOT,
                                            o-> (o instanceof FiniteSet?Real.Int.ONE:Real.Int.ZERO), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("IS_MAP", null, MODIFY_ARG0_ROOT,
                                    o-> (o instanceof FiniteMap?Real.Int.ONE:Real.Int.ZERO), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("IS_TUPLE", null, MODIFY_ARG0_ROOT,
                                    o-> ((o instanceof FiniteMap&&((FiniteMap) o).isTuple())?Real.Int.ONE:Real.Int.ZERO), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("IS_MATRIX", null, MODIFY_ARG0_ROOT,
                                    o-> ((o instanceof Matrix ||(o instanceof FiniteMap&&((FiniteMap) o).isMatrix()))
                                            ?Real.Int.ONE:Real.Int.ZERO), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    declareUnaryOperator("IS_LAMBDA", null, MODIFY_ARG0_ROOT,
                            o-> (o instanceof LambdaExpression?Real.Int.ONE:Real.Int.ZERO), OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);

                    declareUnaryOperator("SIZE", null, MODIFY_ARG0_ROOT,
                                    (o)-> {
                                        if(o instanceof Matrix) {
                                            return Real.from(((Matrix) o).size());
                                        }else if(o instanceof FiniteSet){
                                            return Real.from(((FiniteSet)o).size());
                                        }else if(o instanceof FiniteMap){
                                            return Real.from(((FiniteMap)o).size());
                                        }
                                        return Real.Int.ONE;
                                    }, 0);
                    declareUnaryOperator("LENGTH", "LEN", MODIFY_ARG0_ROOT,
                                    (o)-> {
                                        if(o instanceof Matrix) {
                                            return Real.from(((Matrix) o).dimensions()[0]);
                                        }else if(o instanceof FiniteSet){
                                            return Real.from(((FiniteSet)o).size());
                                        }else if(o instanceof Tuple){
                                            return Real.from(((Tuple)o).length());
                                        }
                                        return Real.Int.ONE;
                                    }, 0);

                    {
                        //wraps the given MathObject in a Set
                        OperatorInfo newSet1=declareUnaryOperator("WRAP_IN_SET", null, MODIFY_ARG0_ROOT,
                                FiniteSet::from, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                        //creates a Set from the given MathObjects
                        OperatorInfo newSet2=declareBinaryOperator("WRAP2_IN_SET", null, MODIFY_ARG0_ROOT,
                                FiniteSet::from, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                        //creates a Set from the given MathObjects
                        OperatorInfo newSet=declareOperator(NEW_SET, null, 3, true, MODIFY_ARG0_ROOT
                                , FiniteSet::from, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                        NAryInfo info=new NAryInfo(FiniteSet.EMPTY_SET,newSet,true,false);
                        info.addShortCut(newSet1);
                        info.addShortCut(newSet2);
                    }
                    {
                        //wraps the given MathObject in a Tuple
                        OperatorInfo newTuple1=declareUnaryOperator("WRAP_IN_TUPLE", null, MODIFY_ARG0_ROOT,
                                o -> Tuple.create(new MathObject[]{o}), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                        //creates a Pair from the given MathObjects
                        OperatorInfo newPair=declareBinaryOperator("NEW_PAIR", null, MODIFY_ARG0_ROOT,
                                Pair::new,OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                        //creates a new Tuple from the given MathObjects
                        OperatorInfo newTuple=declareOperator(NEW_TUPLE, null, 3, true, MODIFY_ARG0_ROOT,
                                Tuple::create, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                        NAryInfo info=new NAryInfo(Tuple.EMPTY_MAP,newTuple,false,false);
                        info.addShortCut(newTuple1);
                        info.addShortCut(newPair);
                        //creates a SparseTuple from the given MapObject, with each 2 consecutive arguments being a key-value pair
                        OperatorInfo sparseTuple=declareOperator(SPARSE_TUPLE, null, 2, true, MODIFY_ARG0_ROOT,
                                (args)->{
                                    HashMap<MathObject, MathObject> map = constructMapFromArgs(args);
                                    return FiniteMap.from(map, FiniteMap.TUPLE_WRAP_ALL);
                                }, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                        new NAryInfo(Tuple.EMPTY_MAP,sparseTuple,false,false);
                    }
                    {
                        //returns the map {A->B}
                        OperatorInfo newMap1=declareBinaryOperator("SINGLETON_MAP", null, MODIFY_ARG0_ROOT,
                                (k,v)->FiniteMap.from(Collections.singletonMap(k,v), FiniteMap.TUPLE_WRAP_NONE), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                        //creates a Map from the given MapObject, with each 2 consecutive arguments being a key-value pair
                        OperatorInfo newMap=declareOperator(NEW_MAP, null, 4, true, MODIFY_ARG0_ROOT,
                                (args)->{
                                    HashMap<MathObject, MathObject> map = constructMapFromArgs(args);
                                    return FiniteMap.from(map, FiniteMap.TUPLE_WRAP_NONE);
                                }, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                        NAryInfo info=new NAryInfo(Tuple.EMPTY_MAP,newMap,false,false);
                        info.addShortCut(newMap1);
                    }

                    declareUnaryOperator("IDENTITY_MATRIX", null, MODIFY_ARG0_ROOT,
                                    (l)-> Matrix.identityMatrix(l.numericValue().realPart().num().intValueExact()), 0);
                    declareBinaryOperator("DIAGONAL_MATRIX", null, MODIFY_ARG0_ROOT,
                                    (l,v)-> Matrix.diagonalMatrix(l.numericValue().realPart().num().intValueExact()
                                            ,v.numericValue()), 0);
                    declareBinaryOperator("INT_RANGE", null, MODIFY_ARG0_ROOT,
                                    (l,u)->FiniteSet.range(l.numericValue().realPart().round(MathObject.FLOOR),
                                            u.numericValue().realPart().round(MathObject.FLOOR)), 0);
                    //TO_DIAGONAL_MATRIX    MathObject -> "row" -> diagonal
                    //addLater? NEW_MATRIX (Bi-Nary)

                    //intersection of A and B (A cap B)
                    cut=declareBinaryOperator("CUT", null, MODIFY_ARG0_ROOT, MathObject::intersect, 0);
                    //union of A and B (A cup B)
                    unite=declareBinaryOperator("UNITE", null, MODIFY_ARG0_ROOT, MathObject::unite, 0);
                    //symmetric-difference of A and B (A delta B)
                    declareBinaryOperator("SYMMETRIC_DIFFERENCE", "SYM_DIFF", MODIFY_ARG0_ROOT, MathObject::symmetricDifference, 0);
                    //difference of A and B (A\B)
                    declareBinaryOperator("DIFFERENCE", "DIFF", MODIFY_ARG0_ROOT, MathObject::difference, 0);
                    //cartesian product of A and B
                    times=declareBinaryOperator("TIMES", null, MODIFY_ARG0_ROOT, MathObject::times, 0);

                    //returns the first value in A
                    declareUnaryOperator("GET_FIRST", null, MODIFY_ARG0_ROOT,
                            o->MathObject.firstOrLast(o,true, false), 0);
                    //returns the last value in A
                    declareUnaryOperator("GET_LAST", null, MODIFY_ARG0_ROOT,
                            o->MathObject.firstOrLast(o,false, false), 0);
                    //returns the first key in A
                    declareUnaryOperator("FIRST_KEY", null, MODIFY_ARG0_ROOT,
                            o->MathObject.firstOrLast(o,true, true), 0);
                    //returns the last key in A
                    declareUnaryOperator("LAST_KEY", null, MODIFY_ARG0_ROOT,
                            o->MathObject.firstOrLast(o,false, true), 0);
                    //returns the domain of A
                    declareUnaryOperator("DOMAIN", null, MODIFY_ARG0_ROOT,
                            o->MathObject.domainOrRange(o,true), 0);
                    //return all values in A
                    declareUnaryOperator("VALUES", null, MODIFY_ARG0_ROOT,
                            o->MathObject.domainOrRange(o,false), 0);
                    //returns the element with the given key B in the map A
                    //if A is a set the Operator returns 1 iff B is an element of A
                    declareBinaryOperator("GET_ELEMENT", null, MODIFY_ARG0_ROOT,
                            MathObject::getElement, 0);//TODO? allow Lambda in 2nd slot
                    //adds B to A for consistency with Tuples if A is a map the key is assumed to be 1 more
                    // than the largest numeric value of a key in A
                    declareBinaryOperator("INSERT", null, MODIFY_ARG0_ROOT,
                            MathObject::insert, 0);//? allow Lambda in 2nd slot
                    //adds args[2] at position args[1] in args[0]
                    declareOperator("PUT", null, 3,false,MODIFY_ARG0_ROOT,
                            (args)->MathObject.put(args[0],args[1],args[2]), 0);//? allow Lambda in 2nd/3rd slot
                    //concatenates two tuples (maps are automatically converted to their tuple representation)
                    tupleConcat=declareBinaryOperator("TUPLE_CONCAT", null, MODIFY_ARG0_ROOT,MathObject::tupleConcat, OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //adds A as First element to the Tuple B
                    declareBinaryOperator("TUPLE_PUSH_FIRST", null, MODIFY_ARG0_ROOT,
                                    (l,r)->MathObject.tupleConcat(Tuple.create(new MathObject[]{l}),MathObject.asTuple(r)), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //adds B as Last element to the Tuple A
                    declareBinaryOperator("TUPLE_PUSH_LAST", null, MODIFY_ARG0_ROOT,
                                    (l,r)->MathObject.tupleConcat(MathObject.asTuple(l),Tuple.create(new MathObject[]{r})), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //inserts args[1] in the Tuple args[0] at index args[2]
                    declareOperator("TUPLE_INSERT", null, 3,false,MODIFY_ARG0_ROOT,
                            (args)->MathObject.asTuple(args[0])//? allow lambda in 2nd parameter
                                    .insert(args[1],args[2].numericValue().realPart().num().intValueExact()), 0);
                    //all elements of A with key >= B
                    declareBinaryOperator("RANGE_FROM", null, MODIFY_ARG0_ROOT,
                            (l,r)->MathObject.slice(l,r,true,false), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //all elements of A with key > B
                    declareBinaryOperator("RANGE_ABOVE", null, MODIFY_ARG0_ROOT,
                            (l,r)->MathObject.slice(l,r,false,false), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //all elements of A with key <= B
                    declareBinaryOperator("RANGE_TO", null, MODIFY_ARG0_ROOT,
                            (l,r)->MathObject.slice(l,r,true,true), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //all elements of A with key < B
                    declareBinaryOperator("RANGE_BELOW", null, MODIFY_ARG0_ROOT,
                            (l,r)->MathObject.slice(l,r,false,true), OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //all elements of args[0] with key between args[1] and args[2] (both included)
                    declareOperator("RANGE", null, 3,false,MODIFY_ARG0_ROOT,
                            (args)->MathObject.range(args[0],args[1],true,args[2],true), 0);
                    //all elements of args[0] with key between args[1] included and args[2] excluded
                    declareOperator("RANGE_L", null, 3,false,MODIFY_ARG0_ROOT,
                            (args)->MathObject.range(args[0],args[1],true,args[2],false), 0);
                    //all elements of args[0] with key between args[1] excluded and args[2] included
                    declareOperator("RANGE_R", null, 3,false,MODIFY_ARG0_ROOT,
                            (args)->MathObject.range(args[0],args[1],false,args[2],true), 0);
                    //all elements of args[0] with key between args[1] and args[2] (both excluded)
                    declareOperator("RANGE_BETWEEN", null, 3,false,MODIFY_ARG0_ROOT,
                            (args)->MathObject.range(args[0],args[1],false,args[2],false), 0);
                    //Pair of all elements of A with key <= B and all elements of A with key > B
                    declareBinaryOperator("SPLIT_ABOVE", null, MODIFY_ARG0_ROOT,
                            (l,r)->new Pair(MathObject.slice(l,r,true,true),MathObject.slice(l,r,false,false)),
                            OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //Pair of all elements of A with key < B and all elements of A with key >= B
                    declareBinaryOperator("SPLIT_BELOW", null, MODIFY_ARG0_ROOT,
                            (l,r)->new Pair(MathObject.slice(l,r,false,true),MathObject.slice(l,r,true,false)),
                            OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                    //returns A without its first element
                    declareUnaryOperator("REMOVE_FIRST", null, MODIFY_ARG0_ROOT,
                            (s)-> MathObject.removeEnd(s,true), 0);
                    //returns A without its last element
                    declareUnaryOperator("REMOVE_LAST", null, MODIFY_ARG0_ROOT,
                            (s)-> MathObject.removeEnd(s,false), 0);
                    //returns A without all elements with value B
                    declareBinaryOperator("REMOVE", null, MODIFY_ARG0_ROOT,
                            (c,e)->MathObject.remove(c,e,false),0);
                    //returns A without all elements with key B
                    declareBinaryOperator("REMOVE_KEY", null, MODIFY_ARG0_ROOT,
                            (c,e)->MathObject.remove(c,e,true),0);
                    //returns the Tuple A with the entry at index B removed
                    declareBinaryOperator("REMOVE_INDEX", null, MODIFY_ARG0_ROOT,
                            (t,e)->MathObject.asTuple(t).remove(e.numericValue().realPart().num().intValueExact()),0);
                    //returns the Tuple A with the entries with value B removed
                    declareBinaryOperator("TUPLE_REMOVE", null, MODIFY_ARG0_ROOT,
                            (t,e)->MathObject.asTuple(t).tupleRemove(e),0);
                    //addLater removeAll
                    //REMOVE_ALL
                    //REMOVE_KEYS

                    //removes all values a in A for which B(a)!=0
                    declareBinaryOperator("REMOVE_IF", null, MODIFY_ARG0_NEVER,
                            (a,b) -> {
                                if(b instanceof LambdaExpression){
                                    return MathObject.removeIf(a,e->((LambdaExpression)b).evaluate(new MathObject[]{e}));
                                }else{
                                    return MathObject.removeIf(a,e->b);
                                }
                            }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                    //removes all key-value Pairs (a,b) in A for which B(a,b)!=0
                    declareBinaryOperator("REMOVE_IF2", null, MODIFY_ARG0_NEVER,
                            (a,b) -> {
                                if(b instanceof LambdaExpression){
                                    return MathObject.removeIfMap(a,(e,f)->((LambdaExpression)b).evaluate(new MathObject[]{e,f}));
                                }else{
                                    return MathObject.removeIfMap(a,(e,f)->b);
                                }
                            }, OperatorInfo.LAMBDA_FLAG_ALLOW_ALL);
                }
                //Matrix Operations
                {
                    //transposes the Matrix A
                    declareUnaryOperator("MAT_TRANSPOSE", null, MODIFY_ARG0_ROOT,
                                    (m)->Matrix.asMatrix(m).transpose(), 0);
                    //multiplies the Matrices A and B
                    declareBinaryOperator("MAT_MULTIPLY", "MAT_MULT", MODIFY_ARG0_ROOT,
                                    (l,r)->Matrix.matrixMultiply(Matrix.asMatrix(l),Matrix.asMatrix(r)), 0);
                    //inverts the Matrix A
                    declareUnaryOperator("MAT_INV", null, MODIFY_ARG0_ROOT,
                                    (m)->Matrix.asMatrix(m).invert(), 0);
                    //multiplies the Matrices A and B^-1
                    declareBinaryOperator("MAT_RDIVIDE", "MAT_RDIV", MODIFY_ARG0_ROOT,
                                    (l,r)->Matrix.matrixMultiply(Matrix.asMatrix(l),Matrix.asMatrix(r).invert()), 0);
                    //multiplies the Matrices A^-1 and B
                    declareBinaryOperator("MAT_LDIVIDE", "MAT_LDIV", MODIFY_ARG0_ROOT,
                                    (l,r)->Matrix.matrixMultiply(Matrix.asMatrix(l).invert(),Matrix.asMatrix(r)), 0);
                    //determinant of the Matrix A
                    declareUnaryOperator("MAT_DETERMINATE", "MAT_DET", MODIFY_ARG0_ROOT,
                                    (m)->Matrix.asMatrix(m).determinant(), 0);
                    //Pair(rows,columns) containing the dimensions of the matrix A
                    declareUnaryOperator("MAT_DIMENSIONS", "MAT_DIM", MODIFY_ARG0_ROOT,
                                    (m)->{
                                int[] dim=Matrix.asMatrix(m).dimensions();
                                return new Pair(Real.from(dim[0]),Real.from(dim[1]));
                            }, 0);
                    // gets entry args[1], args[2] in Matrix args[0]
                    // if args[1] or args[2] are maps/sets all pairs of numeric values are replaced with the respective entry
                    // i.e. MAT_GET mat [i1,i2] {j,k} => [{mat[i1][j],mat[i1][k]},{mat[i2][j],mat[i2][k]}]
                    declareOperator("MAT_GET", null, 3, false, MODIFY_ARG0_NEVER, args->
                                MathObject.deepReplace(args[1],e->MathObject.deepReplace(args[2],
                                        f->Matrix.asMatrix(args[0]).entryAt(e.realPart().num().intValueExact()
                                        ,f.realPart().num().intValueExact())))
                                ,0);
                    //sets entry args[1], args[2] in Matrix args[0] to the value args[3]
                    // if args[1] or args[2] are maps/sets all pairs of numeric values are set to the new value
                    // i.e. MAT_SET mat [i1,i2] {j,k} v =>
                    // MAT_SET mat i1 j v   MAT_SET mat i1 k v   MAT_SET mat i2 j v   MAT_SET mat i2 k v
                    declareOperator("MAT_SET", null, 4, false, MODIFY_ARG0_NEVER, args->
                            {
                                Matrix[] mat=new Matrix[]{Matrix.asMatrix(args[0])};
                                MathObject.deepForEach(args[1],e->MathObject.deepForEach(args[2],f->
                                    mat[0]=mat[0].setEntry(e.realPart().num().intValueExact()
                                ,f.realPart().num().intValueExact(),args[3].numericValue())));
                                return mat[0];
                            }, 0);
                }
                //Nary Operations
                {
                    {
                        //sum of the given elements
                        OperatorInfo sum=declareOperator("SUM", null, 3, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::add), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,sum,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(add);
                    }
                    {
                        //deep sum of all elements in A
                        OperatorInfo deepSum1=declareOperator("DEEP_SUM1", null, 1, false, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::add), 0);
                        //deep sum of the given elements
                        OperatorInfo deepSum=declareOperator("DEEP_SUM", null, 2, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::add), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,deepSum,true,true);
                        info.addShortCut(deepSum1);
                    }
                    {
                        //product of the given elements
                        OperatorInfo prod=declareOperator("PRODUCT", "PROD", 3, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.nAryReduce(args,Real.Int.ONE,MathObject::multiply), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ONE,prod,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(mult);
                    }
                    {
                        //deep product of all elements in A
                        OperatorInfo deepProd1=declareOperator("DEEP_PRODUCT1", "DEEP_PROD1", 1, false, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ONE, NumericValue::multiply), 0);
                        //deep product of the given elements
                        OperatorInfo deepProd=declareOperator("DEEP_PRODUCT", "DEEP_PROD", 2, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ONE, NumericValue::multiply), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ONE,deepProd,true,true);
                        info.addShortCut(deepProd1);
                    }
                    {
                        //logical and of the given elements
                        OperatorInfo nAnd=declareOperator("NARY_AND", null, 3, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.nAryReduce(args,Real.Int.ONE,MathObject::floorAnd), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ONE,nAnd,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(and);
                    }
                    {
                        //deep logical-and of all elements in A
                        OperatorInfo deepAnd1=declareOperator("DEEP_AND1", null, 1, false, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ONE, NumericValue::floorAnd), 0);
                        //deep logical-and of the given elements
                        OperatorInfo deepAnd=declareOperator("DEEP_AND", null, 2, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ONE, NumericValue::floorAnd), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ONE,deepAnd,true,true);
                        info.addShortCut(deepAnd1);
                    }
                    {
                        //logical-or of the given elements
                        OperatorInfo nOr=declareOperator("NARY_OR", null, 3, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::floorOr), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,nOr,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(or);
                    }
                    {
                        //deep logical-and of all elements in A
                        OperatorInfo deepOr1=declareOperator("DEEP_OR1", null, 1, false, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::floorOr), 0);
                        //deep logical-or of the given elements
                        OperatorInfo deepOr=declareOperator("DEEP_OR", null, 2, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::floorOr), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,deepOr,true,true);
                        info.addShortCut(deepOr1);
                    }
                    {
                        //concatenates the string representations of the given elements
                        OperatorInfo nStrConcat=declareOperator("NARY_STR_CONCAT", null, 3, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::strConcat), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,nStrConcat,false,true);
                        info.addShortCut(ID);
                        info.addShortCut(strConcat);
                    }
                    {
                        //concatenates the string representations of all elements in A
                        OperatorInfo deepStrConcat1=declareOperator("DEEP_STR_CONCAT1", null, 1, false, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::deepStrConcat), 0);
                        //deep-concatenation the string representations of the given elements
                        OperatorInfo deepStrConcat=declareOperator("DEEP_STR_CONCAT", null, 2, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::deepStrConcat), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,deepStrConcat,false,true);
                        info.addShortCut(deepStrConcat1);
                    }
                    {
                        //minimum of the given elements
                        OperatorInfo nMin=declareOperator("NARY_MIN", null, 3, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::min), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,nMin,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(min);
                    }
                    {
                        //deep minimum of the elements in A
                        OperatorInfo deepMin1=declareOperator("DEEP_MIN1", null, 1, false, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::min), 0);
                        //deep minimum of the given elements
                        OperatorInfo deepMin=declareOperator("DEEP_MIN", null, 2, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::min), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,deepMin,true,true);
                        info.addShortCut(deepMin1);
                    }
                    {
                        //maximum of the given elements
                        OperatorInfo nMax=declareOperator("NARY_MAX", null, 3, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::max), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,nMax,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(max);
                    }
                    {
                        //deep maximum of the elements in A
                        OperatorInfo deepMax1=declareOperator("DEEP_MAX1", null, 1, false, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::max), 0);
                        //deep maximum of the given elements
                        OperatorInfo deepMax=declareOperator("DEEP_MAX", null, 2, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::max), 0);
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,deepMax,true,true);
                        info.addShortCut(deepMax1);
                    }

                    //NARY_BIN_CONCAT

                    {
                        //union of the given elements
                        OperatorInfo union=declareOperator("UNION", null, 3, true, MODIFY_ARG0_ROOT,
                                MathObject::nAryTimes, 0);
                        NAryInfo info=new NAryInfo(FiniteSet.EMPTY_SET,union,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(unite);
                    }
                    {
                        //intersection of the given elements
                        OperatorInfo union=declareOperator("INTERSECTION", "N_CUT", 3, true, MODIFY_ARG0_ROOT,
                                MathObject::nAryTimes, 0);
                        NAryInfo info=new NAryInfo(FiniteSet.EMPTY_SET,union,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(cut);
                    }
                    {
                        //n-ary cartesian product of the given elements
                        OperatorInfo nTimes=declareOperator("NARY_TIMES", null, 3, true, MODIFY_ARG0_ROOT,
                                MathObject::nAryTimes, 0);
                        NAryInfo info=new NAryInfo(FiniteSet.EMPTY_SET,nTimes,false,true);
                        info.addShortCut(ID);
                        info.addShortCut(times);
                    }
                    {
                        //n-ary tuple-concatenation of the given elements
                        OperatorInfo nTupleConcat=declareOperator("NARY_TUPLE_CONCAT", null, 1, true, MODIFY_ARG0_ROOT,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::tupleConcat),
                                OperatorInfo.LAMBDA_FLAG_ALLOW_BOUND);
                        NAryInfo info=new NAryInfo(Tuple.EMPTY_MAP,nTupleConcat,false,true);
                        info.addShortCut(ID);
                        info.addShortCut(tupleConcat);
                    }
                }
                //FileIO
                {
                    //FILE_GOTO_BIT <path> <count>
                    declareBinaryEnvOperator("FILE_GOTO_BIT", env -> (file, pos) -> {
                                try {
                                    String path = file.asString();
                                    env.fileAt(path).seek(pos.numericValue()
                                            .round(MathObject.FLOOR).realPart().num().longValueExact());
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_GOTO_BYTE <path> <count>
                    declareBinaryEnvOperator("FILE_GOTO_BYTE", env -> (file, pos) -> {
                                try {
                                    String path = file.asString();
                                    env.fileAt(path).seek(8 * pos.numericValue()
                                            .round(MathObject.FLOOR).realPart().num().longValueExact());
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_GOTO_BYTE_START <path>
                    declareUnaryEnvOperator("FILE_GOTO_BYTE_START", (env, file) -> {
                                try {
                                    String path = file.asString();
                                    BitRandomAccessStream file1 = env.fileAt(path);
                                    file1.seek(8 * ((file1.bitPos() / 8)));
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_BIT_POSITION <path>
                    declareUnaryEnvOperator("FILE_BIT_POS", (env, file) -> {
                                String path = file.asString();
                                try {
                                    return Real.Int.from(env.fileAt(path).bitPos());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_BYTE_POSITION <path>
                    declareUnaryEnvOperator("FILE_BYTE_POS", (env, file) -> {
                                String path = file.asString();
                                try {
                                    return Real.divide(Real.Int.from(env.fileAt(path).bitPos()),
                                            Real.from(BigInteger.valueOf(8)));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_SIZE <path>
                    declareUnaryEnvOperator("FILE_SIZE", (env, file) -> {
                                String path = file.asString();
                                try {
                                    return Real.Int.from(env.fileAt(path).byteLength());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_BIT_SIZE <path>
                    declareUnaryEnvOperator("FILE_BIT_SIZE", (env, file) -> {
                                String path = file.asString();
                                try {
                                    return Real.Int.from(8L * env.fileAt(path).byteLength());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_MOVE_BITS <path> <count>
                    declareBinaryEnvOperator("FILE_MOVE_BITS", env -> (file, pos) -> {
                                try {
                                    String path = file.asString();
                                    BitRandomAccessStream file1 = env.fileAt(path);
                                    file1.seek(file1.bitPos() + pos.numericValue()
                                            .round(MathObject.FLOOR).realPart().num().longValueExact());
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_MOVE_BYTES <path> <count>
                    declareBinaryEnvOperator("FILE_MOVE_BYTES", env -> (file, pos) -> {
                                try {
                                    String path = file.asString();
                                    BitRandomAccessStream file1 = env.fileAt(path);
                                    file1.seek(file1.bitPos() + 8 * pos.numericValue()
                                            .round(MathObject.FLOOR).realPart().num().longValueExact());
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_READ_BIT <path>
                    declareUnaryEnvOperator("FILE_READ_BIT", (env, file) -> {
                                try {
                                    String path = file.asString();
                                    return Real.Int.from(env.fileAt(path).readBit());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_READ_BYTE <path>
                    declareUnaryEnvOperator("FILE_READ_BYTE", (env, file) -> {
                                try {
                                    String path = file.asString();
                                    return Real.Int.from(env.fileAt(path).readBits(8));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_READ_BITS <path> <count>
                    declareBinaryEnvOperator("FILE_READ_BITS", env -> (file, count) -> {
                                try {
                                    String path = file.asString();
                                    return Real.Int.from(env.fileAt(path).readBits(count.numericValue()
                                            .round(MathObject.FLOOR).realPart().num().intValueExact()));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_READ_CHAR <path>
                    declareUnaryEnvOperator("FILE_READ_CHAR", (env, file) -> {
                                try {
                                    String path = file.asString();
                                    return Real.from(Real.stringAsBigInt(new String(Character.toChars(env.fileAt(path).readUTF8()))));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_READ_STRING <path> <len> //addLater String IO
                    //FILE_READ_LINE <path>
                    //FILE_READ_BYTES <path> <count>
                    declareBinaryEnvOperator("FILE_READ_BYTES", env -> (file, count) -> {
                                try {
                                    String path = file.asString();
                                    return Real.Int.from(env.fileAt(path).readBits(8 * count.numericValue()
                                            .round(MathObject.FLOOR).realPart().num().intValueExact()));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_READ_BIG_INT <path> <header> <block> <bigBlock>
                    declareEnvOperator("FILE_READ_BIG_INT",4,(env,args)->{
                                try {
                                    String path = args[0].asString();
                                    return Real.Int.from(env.fileAt(path).readBigInt(
                                            args[1].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact(),
                                            args[2].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact(),
                                            args[3].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact()));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_READ_FULLY <path>  reads complete File into memory
                    //FILE_WRITE_BIT <path> <value>
                    declareBinaryEnvOperator("FILE_WRITE_BIT", env -> (file, value) -> {
                                try {
                                    String path = file.asString();
                                    env.fileAt(path).writeBit(!value.equals(Real.Int.ZERO));
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_WRITE_BYTE <path> <value>
                    declareBinaryEnvOperator("FILE_WRITE_BYTE", env -> (file, value) -> {
                                try {
                                    String path = file.asString();
                                    env.fileAt(path).writeByte(value.numericValue().round(MathObject.FLOOR).
                                            realPart().num().intValue() & 0xff);
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_WRITE_CHAR <path> <value>
                    declareBinaryEnvOperator("FILE_WRITE_CHAR", env->(file, value) -> {
                                try {
                                    String path = file.asString();
                                    env.fileAt(path).writeUTF8(value.asString().codePointAt(0));
                                    return Real.Int.ZERO;
                                } catch (IOException|IndexOutOfBoundsException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_WRITE_BITS <path> <value> <count>
                    declareEnvOperator("FILE_WRITE_BITS",3,(env,args)->{
                        try {
                            String path = args[0].asString();
                            env.fileAt(path).writeBits(args[1].numericValue().round(MathObject.FLOOR).realPart().num(),
                                    args[2].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact());
                            return Real.Int.ZERO;
                        } catch (IOException io) {
                            System.err.println('\n' + io.toString());
                            return Real.Int.NEGATIVE_ONE;
                        }
                    });
                    //FILE_WRITE_BYTES <path> <value> <count>
                    declareEnvOperator("FILE_WRITE_BYTES",3,(env,args)-> {
                        try {
                            String path = args[0].asString();
                            env.fileAt(path).writeBits(args[1].numericValue().round(MathObject.FLOOR)
                                    .realPart().num(),8 * args[2].numericValue().round(MathObject.FLOOR)
                                    .realPart().num().intValueExact());
                            return Real.Int.ZERO;
                        } catch (IOException io) {
                            System.err.println('\n' + io.toString());
                            return Real.Int.NEGATIVE_ONE;
                        }
                    });
                    //FILE_WRITE_STRING <path> <value> <len>
                    //FILE_WRITE_NEW_LINE <path>
                    //FILE_WRITE_ALL_BITS <path> <value>
                    declareBinaryEnvOperator("FILE_WRITE_ALL_BITS", env -> (file, value) -> {
                                try {
                                    String path = file.asString();
                                    BigInteger bigInt = value.numericValue().round(MathObject.FLOOR).realPart().num();
                                    env.fileAt(path).writeBits(bigInt, bigInt.bitLength());
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_WRITE_BIG_INT <path> <value> <header> <block> <bigBlock>
                    declareEnvOperator("FILE_WRITE_BIG_INT",5,(env,args)->{
                        try {
                            String path = args[0].asString();
                            env.fileAt(path).writeBigInt(
                                    args[1].numericValue().round(MathObject.FLOOR).realPart().num(),
                                    args[2].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact(),
                                    args[3].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact(),
                                    args[4].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact());
                            return Real.Int.ZERO;
                        } catch (IOException io) {
                            System.err.println('\n' + io.toString());
                            return Real.Int.NEGATIVE_ONE;
                        }
                    });
                    //FILE_TRUNCATE <path>
                    declareUnaryEnvOperator("FILE_0TRUNCATE", (env, file) -> {
                                try {
                                    String path = file.asString();
                                    BitRandomAccessStream file1 = env.fileAt(path);
                                    file1.truncateToSize(false);
                                    return Real.from(file1.byteLength());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    declareUnaryEnvOperator("FILE_1TRUNCATE", (env, file) -> {
                                try {
                                    String path = file.asString();
                                    BitRandomAccessStream file1 = env.fileAt(path);
                                    file1.truncateToSize(true);
                                    return Real.from(file1.byteLength());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                    //FILE_CLOSE <path>
                    declareUnaryEnvOperator("FILE_CLOSE", (env, file) -> {
                                try {
                                    String path = file.asString();
                                    env.closeFileAt(path);
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            });
                }
                //? GUI

            }
        }

        @NotNull
        private static HashMap<MathObject, MathObject> constructMapFromArgs(MathObject[] args) {
            if (args.length % 2 == 1)
                throw new IllegalArgumentException("NEW_MAP needs an even Number of Arguments");
            HashMap<MathObject, MathObject> map = new HashMap<>(args.length / 2);
            for (int i = 0; i < args.length; i += 2) {
                if (map.put(args[i], args[i + 1]) != null) {
                    throw new IllegalArgumentException("duplicate key in NEW_MAP: " + args[i]);
                }
            }
            return map;
        }


        private static final HashMap<Integer, OperatorInfo> operators=new HashMap<>();
        private static final HashMap<String, OperatorInfo> operatorNames=new HashMap<>();

        /**@return the OperatorInfo for the Operator with the given Name or null if there is no operator with the given Name*/
        public static @Nullable OperatorInfo byNameOrNull(String name){
            ensureOperatorsInitialized();
            return operatorNames.get(name);
        }
        /**@return the OperatorInfo for the Operator with the given Name
         * @throws IllegalArgumentException if there is no Operator with the given name*/
        public static OperatorInfo byName(String name){
            OperatorInfo ret=byNameOrNull(name);
            if(ret==null)
                throw new IllegalArgumentException("There is no Operator with the given name:"+name);
            return ret;
        }
        /**@return the OperatorInfo for the Operator with the given id
         * @throws IllegalArgumentException if there is no Operator with the given id*/
        public static OperatorInfo byId(int id){
            ensureOperatorsInitialized();
            OperatorInfo ret=operators.get(id);
            if(ret==null)
                throw new IllegalArgumentException("There is no Operator with the given id:"+id);
            return ret;
        }

        /**@return The nAry info for the given Operator or null if there is none*/
        public static @Nullable NAryInfo nAryInfo(OperatorInfo operator){
            return nAryInfos.get(operator);
        }

        /**checks operator names, is only used for the initial initialisation of the operators*/
        private static void checkName(String name) {
            if(!name.toUpperCase(Locale.ROOT).equals(name))
                throw new RuntimeException("name has to be Uppercase:"+ name +"!="+ name.toUpperCase(Locale.ROOT));
            if(!name.matches("[A-Z]([0-9A-Z_]*[A-Z0-9])?"))
                throw new RuntimeException("illegal operatorName:"+ name +
                        "\n names can only contain uppercase latin letters, digits and underscores," +
                        " names cannot start with a digit an underscore cannot be the first or last character");
            if(operatorNames.containsKey(name))
                throw new RuntimeException("There is already an Operator with the given name");
        }

        /**Declares a new operator with the given name and number of Arguments,
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param shortName short name for the operator is ignored if null
         * @param numArgs Expected Number of Arguments
         * @param storeMode StoreMode for handling variables in the first slot one of
         * {@link #MODIFY_ARG0_NEVER}, {@link #MODIFY_ARG0_ROOT}, {@link #MODIFY_ARG0_ALWAYS}
         * @param eval Function for preforming the Evaluation
         * @param lambdaFlags flags for allowing the direct calculation of specific kinds of LambdaExpressions,
         *                   by default the calculation is redirected to {@link LambdaExpression}
         *                        when there are any LambdaExpressions in the Arguments
         * @throws IllegalArgumentException If there is already an Operator with the given name*/
        private static OperatorInfo declareOperator(String name, @Nullable String shortName, int numArgs, boolean isNary, int storeMode,
                                                    Function<MathObject[], MathObject> eval, int lambdaFlags){
            checkName(name);
            int id= operators.size();
            OperatorInfo info=new OperatorInfo(id,name,storeMode,isNary,numArgs,eval, lambdaFlags);
            operators.put(id,info);
            operatorNames.put(name,info);
            if(shortName!=null){
                checkName(shortName);
                operatorNames.put(shortName,info);
            }
            return info;
        }
        /**Declares a new environment-dependent operator with the given name and number of Arguments,
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * {@link #MODIFY_ARG0_NEVER}, {@link #MODIFY_ARG0_ROOT}, {@link #MODIFY_ARG0_ALWAYS}
         * @param numArgs Expected Number of Arguments
         * @param eval Function for preforming the Evaluation
         * @throws IllegalArgumentException If there is already an Operator with the given name*/
        private static void declareEnvOperator(String name,int numArgs,BiFunction<ExecutionEnvironment,MathObject[],MathObject> eval){
            checkName(name);
            int id= operators.size();
            OperatorInfo info=new OperatorInfo(id,name,MODIFY_ARG0_NEVER,false,numArgs,eval);
            operators.put(id,info);
            operatorNames.put(name,info);
        }

        /**shortcut for the declaration of an unary Operator
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param minArgs Minimum Expected number of Arguments
         * @see #declareOperator(String, String, int, boolean, int, Function, int)  */
        private static void declareRuntimeOperator(String name,int minArgs){
            checkName(name);
            int id= operators.size();
            OperatorInfo info=new OperatorInfo(id,name,MODIFY_ARG0_NEVER,false,minArgs,null, 0);
            operators.put(id,info);
            operatorNames.put(name,info);
        }

        /**shortcut for the declaration of an unary Operator
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param shortName short name for the operator is ignored if null
         * @param storeMode StoreMode for handling variables in the first slot one of
         * {@link #MODIFY_ARG0_NEVER}, {@link #MODIFY_ARG0_ROOT}, {@link #MODIFY_ARG0_ALWAYS}
         * @param unEval unary Evaluation Function
         * @param lambdaFlags flags for allowing the direct calculation of specific kinds of LambdaExpressions,
         *                   by default the calculation is redirected to {@link LambdaExpression}
         *                        when there are any LambdaExpressions in the Arguments
         * @see #declareOperator(String, String, int, boolean, int, Function, int)  */
        private static OperatorInfo declareUnaryOperator(String name, @Nullable String shortName, int storeMode, Function<MathObject, MathObject> unEval, int lambdaFlags){
            return declareOperator(name, shortName, 1, false, storeMode, (args)->unEval.apply(args[0]), lambdaFlags);
        }
        /**shortcut for the declaration of a binary Operator
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param shortName short name for the operator is ignored if null
         * @param storeMode StoreMode for handling variables in the first slot one of
         * {@link #MODIFY_ARG0_NEVER}, {@link #MODIFY_ARG0_ROOT}, {@link #MODIFY_ARG0_ALWAYS}
         * @param biEval binary Evaluation Function
         * @param lambdaFlags flags for allowing the direct calculation of specific kinds of LambdaExpressions,
         *                   by default the calculation is redirected to {@link LambdaExpression}
         *                        when there are any LambdaExpressions in the Arguments
         * @see #declareOperator(String, String, int, boolean, int, Function, int)  */
        private static OperatorInfo declareBinaryOperator(String name, @Nullable String shortName, int storeMode, BinaryOperator<MathObject> biEval, int lambdaFlags){
            return declareOperator(name, shortName, 2, false, storeMode, (args)->biEval.apply(args[0],args[1]), lambdaFlags);
        }
        /**shortcut for the declaration of an unary environment-dependent Operator
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param unEval unary Evaluation Function
         * @see #declareEnvOperator(String,  int, BiFunction)  */
        private static void declareUnaryEnvOperator(String name, BiFunction<ExecutionEnvironment, MathObject, MathObject> unEval){
            declareEnvOperator(name, 1,(env, args)->unEval.apply(env,args[0]));
        }
        /**shortcut for the declaration of a binary environment-dependent Operator
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param biEval binary Evaluation Function
         * @see #declareEnvOperator(String,  int, BiFunction)  */
        private static void declareBinaryEnvOperator(String name, Function<ExecutionEnvironment, BinaryOperator<MathObject>> biEval){
            declareEnvOperator(name,  2, (env, args) -> biEval.apply(env).apply(args[0], args[1]));
        }
    }
}
