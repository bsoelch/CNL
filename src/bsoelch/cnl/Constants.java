package bsoelch.cnl;

import bsoelch.cnl.interpreter.ExecutionEnvironment;
import bsoelch.cnl.math.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

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
    //addLater? TRY,CATCH,THROW,EXIT

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

    //11110010.[BigInt] -> Lambda Header   Lambda:N [expr(LambdaArg(0),...LambdaArg(N-1)]
    //11110011.[BigInt] -> Lambda Argument
    //11110100.[BigInt] -> Operator-reference &OP -> Lambda:[ArgCount] OP LambdaArg(0) ... LambdaArg(N-1)

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
    public static final int OUT_FLAG_FIXED_POINT=1;
    public static final int OUT_FLAG_FLOAT=2;
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
        public static final int FLAG_DYNAMIC=1;
        public static final int FLAG_NARY=2;
        public static final int FLAG_NEEDS_ENVIRONMENT=4;

        private static final HashMap<Integer, OperatorInfo> operators=new HashMap<>();
        private static final HashMap<String, OperatorInfo> operatorNames=new HashMap<>();

        public static final int MODIFY_ARG0_ALWAYS=1, MODIFY_ARG0_ROOT=0,MODIFY_ARG0_NEVER=-1;

        /** -A */
        public static final String NEGATE = "NEG";
        /** 1/A */
        public static final String INVERT = "INV";
        /** ++A */
        public static final String INCREMENT = "INC";
        /** --A */
        public static final String DECREMENT = "DEC";
        /** A* */
        public static final String CONJUGATE = "CONJ";
        /** |A|² */
        public static final String SQUARE_ABS = "SQ_ABS";
        /** Re A */
        public static final String REAL_PART = "RE";
        /** Im A */
        public static final String IMAGINARY_PART = "IM";
        /** floor A */
        public static final String FLOOR = "FLOOR";
        /** ciel A */
        public static final String CIEL = "CIEL";
        /** round A */
        public static final String ROUND = "ROUND";
        /** !A  (1 if A==0 else 0)*/
        public static final String NOT = "NOT";
        /** A>0*/
        public static final String GREATER_THAN_0 = "GT0";
        /** A>=0*/
        public static final String GREATER_EQUAL_0 = "GE0";
        /**min A,B */
        public static final String MIN = "MIN";
        /**max A,B*/
        public static final String MAX = "MAX";
        /**highest one bit in the binary representation von A*/
        public static final String BIT_LENGTH = "BIT_LENGTH";
        /**Var[A]*/
        public static final String DYNAMIC_VAR = "DYNAMIC_VAR";
        /**A+B*/
        public static final String ADD = "ADD";
        /**A-B*/
        public static final String SUBTRACT = "SUBT";
        /**A*B*/
        public static final String MULTIPLY = "MULT";
        /**A/B*/
        public static final String DIVIDE = "DIV";
        /**A%B*/
        public static final String MODULO = "MOD";
        /**A+Bi*/
        public static final String COMPLEX = "CMPLX";
        /**A==B*/
        public static final String EQUAL = "EQ";
        /**A!=B*/
        public static final String NOT_EQUAL = "NE";
        /**A>B*/
        public static final String GREATER_THAN = "GT";
        /**A>=B*/
        public static final String GREATER_EQUAL = "GE";
        /**A&B*///set intersection
        public static final String AND = "AND";
        /**A|B*///set union
        public static final String OR = "OR";
        /**A xor B*///symmetric difference
        public static final String XOR = "XOR";
        /**A & !B*///difference set
        public static final String AND_NOT = "AND_NOT";
        /**A x B*///cartesian product,dot product, matrix product
        public static final String TIMES = "TIMES";
        /**A<sup> floor B</sup>*/
        public static final String POW = "POW";
        /**A(+)B mit a/b (+) c/d = (a+c)/(b+d)*/
        public static final String F_ADD = "F_ADD";
        /**calculates an approximate value for A with an error less than Re B*/
        public static final String APPROXIMATE = "APPROX";
        /**A?B:C*/
        public static final String OPTIONAL = "IF";
        /**calls function [A]*/
        public static final String CALL_FUNCTION = "CALL";
        /**number of chars in A (as String)*/
        public static final String STRING_LENGTH = "STR_LEN";
        /**int representation of the concatenation of s´the string representations of A and B*/
        public static final String STRING_CONCAT = "CONCAT";
        /**int representation of A converted to string representation (base B)*/
        public static final String INT_TO_STRING = "INT_TO_STRING";
        /**converts A (as string representation) to an int (base B), skips illegal characters*/
        public static final String STRING_TO_INT = "STRING_TO_INT";
        /**concatenates A and B as tuples*/
        private static final String TUPLE_CONCAT = "TUPLE_CONCAT";
        /**intersection of A and B as sets*/
        private static final String CUT = "CUT";
        /**union of A and B as sets*/
        private static final String UNITE = "UNITE";
        /**symmetric difference of A and B as sets*/
        private static final String SYM_DIFF = "SYM_DIFF";
        /**difference of A and B as sets*/
        private static final String DIFF = "DIFF";
        /**wraps the following value in a Set*/
        private static final String WRAP_IN_SET = "WRAP_IN_SET";
        /**wraps the following two values in a Set*/
        private static final String WRAP2_IN_SET = "WRAP2_IN_SET";
        /**creates a new set for the following elements (nary, minArgs: 3)*/
        public static final String NEW_SET = "NEW_SET";
        /**wraps the following value in a Tuple*/
        private static final String WRAP_IN_TUPLE = "WRAP_IN_TUPLE";
        /**creates a new Pair */
        public static final String NEW_PAIR = "NEW_PAIR" ;
        /**creates a new Tuple for the following elements (nary, minArgs: 3)*/
        public static final String NEW_TUPLE = "NEW_TUPLE";
        /**creates a new Map with one Entry {A->B} */
        public static final String SINGLETON_MAP = "SINGLETON_MAP" ;
        /**creates a new map for the following elements, with each pair of two consecutive Elements being a key-value pair (nary, minArgs: 4)*/
        public static final String NEW_MAP = "NEW_MAP";

        private static boolean operatorsInitialized=false;

        /**ensures that all Operators are initialized*/
        private static void ensureOperatorsInitialized(){
            synchronized (operators){
                if(operatorsInitialized)
                    return;
                //3bit Operators
                declareOperator(NEGATE,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT, MathObject::negate));
                declareOperator(NOT,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT, MathObject::not));
                //7bit operators
                declareOperator(INVERT,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT, MathObject::invert));
                declareOperator(EQUAL,
                        new ExecutionInfo.Binary(MODIFY_ARG0_NEVER,
                        (a,b)-> a.equals(b)? Real.Int.ONE:Real.Int.ZERO));
                declareOperator(NOT_EQUAL,
                        new ExecutionInfo.Binary(MODIFY_ARG0_NEVER,
                        (a,b)-> a.equals(b)?Real.Int.ZERO:Real.Int.ONE));
                declareOperator(GREATER_THAN,
                        new ExecutionInfo.Binary(MODIFY_ARG0_NEVER,
                                (a,b)-> MathObject.compare(a,b)>0?Real.Int.ONE:Real.Int.ZERO));
                declareOperator(GREATER_EQUAL,
                        new ExecutionInfo.Binary(MODIFY_ARG0_NEVER,
                                (a,b)-> MathObject.compare(a,b)>=0?Real.Int.ONE:Real.Int.ZERO));
                declareOperator(ADD,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::add));
                declareOperator(SUBTRACT,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::subtract));
                declareOperator(MULTIPLY,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::multiply));
                declareOperator(DIVIDE,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::divide));
                declareOperator(MODULO,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::mod));
                declareOperator(COMPLEX,
                        new ExecutionInfo.Binary(MODIFY_ARG0_NEVER,(a, b)->
                                MathObject.add(a, MathObject.multiply(b, Complex.I))));
                declareOperator(SQUARE_ABS,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT, MathObject::sqAbs));
                declareOperator(CONJUGATE,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT, MathObject::conjugate));
                declareOperator(REAL_PART,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT, MathObject::realPart));
                declareOperator(IMAGINARY_PART,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT, MathObject::imaginaryPart));
                declareOperator(DYNAMIC_VAR,
                        new ExecutionInfo.Dynamic(1));
                //9bit operators
                declareOperator(AND,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::floorAnd));
                declareOperator(OR,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::floorOr));
                declareOperator(XOR,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::floorXor));
                declareOperator(AND_NOT,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::floorAndNot));
                declareOperator(TIMES,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::times));
                declareOperator(INCREMENT,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ALWAYS, a-> MathObject.add(a,Real.Int.ONE)));
                declareOperator(DECREMENT,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ALWAYS, a-> MathObject.subtract(a,Real.Int.ONE)));
                declareOperator(FLOOR,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                a-> MathObject.round(a, MathObject.FLOOR)));
                declareOperator(CIEL,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                a-> MathObject.round(a, MathObject.CIEL)));
                declareOperator(ROUND,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                a-> MathObject.round(a, MathObject.ROUND)));
                declareOperator(GREATER_THAN_0,
                        new ExecutionInfo.Unary(MODIFY_ARG0_NEVER,
                                a-> MathObject.compare(a,Real.Int.ZERO)>0?Real.Int.ONE:Real.Int.ZERO));
                declareOperator(GREATER_EQUAL_0,
                        new ExecutionInfo.Unary(MODIFY_ARG0_NEVER,
                                a-> MathObject.compare(a,Real.Int.ZERO)>=0?Real.Int.ONE:Real.Int.ZERO));
                declareOperator(POW,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::pow));
                declareOperator(MIN,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::min));
                declareOperator(MAX,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::max));
                declareOperator(CALL_FUNCTION,new ExecutionInfo.Dynamic(0));
                //NUM DEN
                //13bit Operators
                declareOperator(OPTIONAL,
                        new ExecutionInfo(MODIFY_ARG0_NEVER,0) {
                            @Override
                            public int argCount() {
                                return 3;
                            }
                            @Override
                            public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                                if(args.length!=3)
                                    throw new IllegalArgumentException("Wrong Argument count: "+args.length+" expected: 3");
                                return MathObject.isTrue(args[0])?args[2]:args[1];
                            }
                        });
                //CONCAT_BINARY
                declareOperator(F_ADD,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::fAdd));
                //F2X_MULT

                declareOperator(APPROXIMATE,
                        new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                (a,b)-> MathObject.approximate(a,b.numericValue().realPart())));
                declareOperator(BIT_LENGTH,
                        new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                (a)-> Real.from(a.numericValue().realPart()
                                        .num().abs().bitLength()
                                        - a.numericValue().realPart().den().bitLength())));

                //Strings
                {
                    declareOperator(STRING_LENGTH,
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    (a) -> Real.Int.from(a.asString().length())));
                    declareOperator(STRING_CONCAT,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::strConcat));
                    declareOperator(INT_TO_STRING,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a, b) -> Real.from(Real.stringAsBigInt(
                                            a.toString(MathObject.round(b, MathObject.FLOOR)
                                                    .numericValue().realPart().num(), true)))));
                    declareOperator(STRING_TO_INT,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a, b) -> MathObject.FromString.safeFromString(
                                            a.asString(), MathObject.round(b, MathObject.FLOOR)
                                                    .numericValue().realPart().num())));

                    declareOperator("STRING_COMPARE",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().compareTo(b.asString()))));
                    declareOperator("STRING_LOWERCASE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    (a) -> Real.from(Real.stringAsBigInt(a.asString().toLowerCase(Locale.ROOT)))));
                    declareOperator("STRING_UPPERCASE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    (a) -> Real.from(Real.stringAsBigInt(a.asString().toUpperCase(Locale.ROOT)))));
                    declareOperator("STRING_STARTS_WITH",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().startsWith(b.asString())?1:0)));
                    declareOperator("STRING_ENDS_WITH",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().endsWith(b.asString())?1:0)));
                    declareOperator("STRING_CONTAINS",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().contains(b.asString())?1:0)));
                    declareOperator("STRING_STARTS_WITH_IGNORE_CASE",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().toLowerCase(Locale.ROOT)
                                            .startsWith(b.asString().toLowerCase(Locale.ROOT))?1:0)));
                    declareOperator("STRING_ENDS_WITH_IGNORE_CASE",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().toLowerCase(Locale.ROOT)
                                            .endsWith(b.asString().toLowerCase(Locale.ROOT))?1:0)));
                    declareOperator("STRING_CONTAINS_IGNORE_CASE",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().toLowerCase(Locale.ROOT)
                                            .contains(b.asString().toLowerCase(Locale.ROOT))?1:0)));
                    declareOperator("STRING_INDEX_OF",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().indexOf(b.asString()))));
                    declareOperator("SUBSTRING_FROM",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a,i) -> Real.from(Real.stringAsBigInt(a.asString()
                                            .substring(i.numericValue().realPart().num().intValueExact())))));
                    declareOperator("SUBSTRING_TO",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (a,i) -> Real.from(Real.stringAsBigInt(a.asString()
                                            .substring(0,i.numericValue().realPart().num().intValueExact()+1)))));
                    declareOperator("SUBSTRING",
                            new ExecutionInfo(MODIFY_ARG0_NEVER,0) {
                                @Override
                                public int argCount() {
                                    return 3;
                                }
                                @Override
                                public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                                    if(args.length!=3)
                                        throw new IllegalArgumentException("Wrong Argument count: "+args.length+" expected: 3");
                                    String str = args[0].asString();
                                    return Real.from(Real.stringAsBigInt(str.substring(
                                            args[1].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact(),
                                            args[2].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact())));
                                }
                            });

                    //STRING_CHARS <str>
                    //STRING_SPLIT <str> <regex>

                    //REGEX_...
                }
                //sets/tuples/maps
                {
                    //Type Conversion
                    declareOperator("NUMERIC_VALUE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    MathObject::numericValue));
                    declareOperator("AS_SET",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    MathObject::asSet));
                    declareOperator("AS_MAP",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    MathObject::asMap));
                    declareOperator("AS_MATRIX",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    Matrix::asMatrix));
                    //Type Checking
                    declareOperator("IS_INTEGER",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    o-> (o instanceof Real.Int?Real.Int.ONE:Real.Int.ZERO)));
                    declareOperator("IS_NUMERIC",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    o-> (o instanceof NumericValue?Real.Int.ONE:Real.Int.ZERO)));
                    declareOperator("IS_SET",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                            o-> (o instanceof FiniteSet?Real.Int.ONE:Real.Int.ZERO)));
                    declareOperator("IS_MAP",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    o-> (o instanceof FiniteMap?Real.Int.ONE:Real.Int.ZERO)));
                    declareOperator("IS_TUPLE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    o-> ((o instanceof FiniteMap&&((FiniteMap) o).isTuple())?Real.Int.ONE:Real.Int.ZERO)));
                    declareOperator("IS_MATRIX",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    o-> ((o instanceof FullMatrix ||(o instanceof FiniteMap&&((FiniteMap) o).isMatrix()))
                                            ?Real.Int.ONE:Real.Int.ZERO)));

                    declareOperator("SIZE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    (o)-> {
                                        if(o instanceof Matrix) {
                                            return Real.from(((Matrix) o).size());
                                        }else if(o instanceof FiniteSet){
                                            return Real.from(((FiniteSet)o).size());
                                        }else if(o instanceof FiniteMap){
                                            return Real.from(((FiniteMap)o).size());
                                        }
                                        return Real.Int.ONE;
                                    }));
                    declareOperator("LEN",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    (o)-> {
                                        if(o instanceof Matrix) {
                                            return Real.from(((Matrix) o).dimensions()[0]);
                                        }else if(o instanceof Tuple){
                                            return Real.from(((Tuple)o).length());
                                        }
                                        return Real.Int.ONE;
                                    }));

                    //simple creators (nary creators in Nary section)
                    declareOperator(WRAP_IN_TUPLE,
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    o -> Tuple.create(new MathObject[]{o})));
                    declareOperator(WRAP_IN_SET,
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT, (Function<MathObject, MathObject>) FiniteSet::from));
                    declareOperator(NEW_PAIR,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    Pair::new));
                    declareOperator(WRAP2_IN_SET,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, (BiFunction<MathObject, MathObject, MathObject>) FiniteSet::from));
                    declareOperator(SINGLETON_MAP,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, (k,v)->FiniteMap.from(Collections.singletonMap(k,v))));
                    declareOperator("IDENTITY_MATRIX",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    (l)-> Matrix.identityMatrix(l.numericValue().realPart().num().intValueExact())));
                    declareOperator("DIAGONAL_MATRIX",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (l,v)-> Matrix.diagonalMatrix(l.numericValue().realPart().num().intValueExact(),v.numericValue())));
                    declareOperator("INT_RANGE",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (l,u)->FiniteSet.range(l.numericValue().realPart().round(MathObject.FLOOR),
                                            u.numericValue().realPart().round(MathObject.FLOOR))));
                    //TO_DIAGONAL_MATRIX    MathObject -> "row" -> diagonal

                    declareOperator(CUT,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::intersect));
                    declareOperator(UNITE,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::unite));
                    declareOperator(SYM_DIFF,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::symmetricDifference));
                    declareOperator(DIFF,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT, MathObject::difference));
                    //CONTAINS (Value)
                    //SET_INSERT
                    //SET_REMOVE

                    //CONTAINS_KEY
                    declareOperator(TUPLE_CONCAT,
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    MathObject::tupleConcat));
                    //TODO Set/Map/Matrix edit
                    declareOperator("TUPLE_PUSH_FIRST",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (l,r)->MathObject.tupleConcat(Tuple.create(new MathObject[]{l}),r)));
                    declareOperator("TUPLE_PUSH_LAST",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (l,r)->MathObject.tupleConcat(l,Tuple.create(new MathObject[]{r}))));
                    //MAP_PUT
                    //TUPLE_INSERT <index>
                    //MAP_GET_FIRST
                    //MAP_GET_LAST
                    //MAP_GET
                    declareOperator("MAP_GET",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (m,k)->MathObject.asMap(k).evaluateAt(k)));
                    //MAP_RANGE_ABOVE
                    //MAP_RANGE_BELOW
                    //MAP_RANGE
                    //MAP_POP_FIRST
                    //MAP_POP_LAST
                    //TUPLE_REMOVE <index>
                    //MAP_REMOVE_ALL
                    //MAP_REMOVE_VALUE
                    //MAP_REMOVE_ALL_VALUE
                }
                //Matrix Operations
                {
                    declareOperator("MAT_TRANSPOSE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    (m)->Matrix.asMatrix(m).transpose()));
                    declareOperator("MAT_MULT",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (l,r)->Matrix.matrixMultiply(Matrix.asMatrix(l),Matrix.asMatrix(r))));
                    declareOperator("MAT_INV",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    (m)->Matrix.asMatrix(m).invert()));
                    declareOperator("MAT_RDIV",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (l,r)->Matrix.matrixMultiply(Matrix.asMatrix(l),Matrix.asMatrix(r).invert())));
                    declareOperator("MAT_LDIV",
                            new ExecutionInfo.Binary(MODIFY_ARG0_ROOT,
                                    (l,r)->Matrix.matrixMultiply(Matrix.asMatrix(l).invert(),Matrix.asMatrix(r))));
                    declareOperator("MAT_DET",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    (m)->Matrix.asMatrix(m).determinant()));
                    declareOperator("MAT_DIM",
                            new ExecutionInfo.Unary(MODIFY_ARG0_ROOT,
                                    (m)->{
                                int[] dim=Matrix.asMatrix(m).dimensions();
                                return new Pair(Real.from(dim[0]),Real.from(dim[1]));
                            }));
                    //addLater? allow ranges in GET and SET
                    //MAT_GET mat [i1,i2] {j,k} => [{mat[i1][j],mat[i1][k]},{mat[i2][j],mat[i2][k]}]
                    declareOperator("MAT_GET",
                            new ExecutionInfo(MODIFY_ARG0_NEVER,0) {
                                @Override
                                public int argCount() {
                                    return 3;
                                }
                                @Override
                                public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                                    if(args.length!=3)
                                        throw new IllegalArgumentException("Wrong Argument count: "+args.length+" expected: 3");
                                    return Matrix.asMatrix(args[0]).entryAt(args[1].numericValue().realPart().num().intValueExact()
                                            ,args[2].numericValue().realPart().num().intValueExact());
                                }
                            });
                    declareOperator("MAT_SET",
                            new ExecutionInfo(MODIFY_ARG0_NEVER,0) {
                                @Override
                                public int argCount() {
                                    return 4;
                                }
                                @Override
                                public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                                    if(args.length!=4)
                                        throw new IllegalArgumentException("Wrong Argument count: "+args.length+" expected: 4");
                                    return Matrix.asMatrix(args[0]).setEntry(args[1].numericValue().realPart().num().intValueExact()
                                            ,args[2].numericValue().realPart().num().intValueExact(),args[3].numericValue());
                                }
                            });
                }
                //Nary Operations
                {
                    declareOperator("SUM",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 3,
                                    (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::add)
                            , c -> c == 2 ? ADD : null, Real.Int.ZERO));
                    declareOperator("DEEP_SUM",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 1,
                                    (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::add)
                                    , c -> null, Real.Int.ZERO));
                    declareOperator("PROD",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 3,
                                    (args)->MathObject.nAryReduce(args,Real.Int.ONE,MathObject::multiply)
                                    , c -> c == 2 ? MULTIPLY : null, Real.Int.ONE));
                    declareOperator("DEEP_PROD",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 1,
                                    (args)->MathObject.deepNAryReduce(args,Real.Int.ONE, NumericValue::multiply)
                                    , c ->  null, Real.Int.ONE));
                    declareOperator("NARY_AND",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 3,
                                    (args)->MathObject.nAryReduce(args,Real.Int.ONE,MathObject::floorAnd)
                                    , c -> c == 2 ? AND : null, Real.Int.ONE));
                    declareOperator("DEEP_AND",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 1,
                                    (args)->MathObject.deepNAryReduce(args,Real.Int.ONE, NumericValue::floorAnd)
                                    , c ->  null, Real.Int.ONE));
                    declareOperator("NARY_OR",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 3,
                                    (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::floorOr)
                                    , c -> c == 2 ? OR : null, Real.Int.ZERO));
                    declareOperator("DEEP_OR",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 1,
                                    (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::floorOr)
                                    , c ->null, Real.Int.ZERO));
                    declareOperator("NARY_STR_CONCAT",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 3,
                                    (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::strConcat)
                                    , c -> c == 2 ? STRING_CONCAT : null, Real.Int.ZERO));
                    declareOperator("DEEP_STR_CONCAT",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 1,
                                    (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::deepStrConcat)
                                    , c -> null, Real.Int.ZERO));
                   declareOperator("NARY_MIN",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 3,
                                    (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::min)
                                    , c -> c == 2 ? MIN : null, FiniteSet.EMPTY_SET));
                    declareOperator("DEEP_MIN",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 1,
                                    (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::min)
                                    , c -> null, Real.Int.ZERO));
                    declareOperator("NARY_MAX",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 3,
                                    (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::max)
                                    , c -> c == 2 ? MAX : null, FiniteSet.EMPTY_SET));
                    declareOperator("DEEP_MAX",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 1,
                                    (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::max)
                                    , c -> null, Real.Int.ZERO));
                    //NARY_BIN_CONCAT

                    declareOperator(NEW_SET,
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 3,
                                    FiniteSet::from, c -> c==1?WRAP_IN_SET:c==2?WRAP2_IN_SET:null, FiniteSet.EMPTY_SET));
                    declareOperator(NEW_TUPLE,
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 3,
                                    Tuple::create, c -> c==1?WRAP_IN_TUPLE:c == 2 ? NEW_PAIR : null, Tuple.EMPTY_MAP));
                    declareOperator(NEW_MAP,
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 4,
                                    (args)->{
                                        if(args.length%2==1)
                                            throw new IllegalArgumentException("NEW_MAP needs an even Number of Arguments");
                                        HashMap<MathObject, MathObject> map=new HashMap<>(args.length/2);
                                        for(int i=0;i<args.length;i+=2){
                                            if(map.put(args[i],args[i+1])!=null){
                                                throw new IllegalArgumentException("duplicate key in NEW_MAP: "+args[i]);
                                            }
                                        }
                                        return FiniteMap.from(map);
                                    }, c -> c==2?SINGLETON_MAP:null, Tuple.EMPTY_MAP));
                    //addLater? NEW_MATRIX (Bi-Nary)

                    declareOperator("NARY_TIMES",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 3, MathObject::nAryTimes, c -> c == 2 ? TIMES : null, FiniteSet.EMPTY_SET));
                    declareOperator("NARY_TUPLE_CONCAT",
                            new ExecutionInfo.Nary(MODIFY_ARG0_ROOT, 1,
                                    (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::tupleConcat)
                                    , c -> null, Real.Int.ZERO));
                }
                //FileIO
                {
                    //FILE_GOTO_BIT <path> <count>
                    declareOperator("FILE_GOTO_BIT",
                            new ExecutionInfo.Binary(MODIFY_ARG0_NEVER, env -> (file, pos) -> {
                                try {
                                    String path = file.asString();
                                    env.fileAt(path).seek(pos.numericValue()
                                            .round(MathObject.FLOOR).realPart().num().longValueExact());
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_GOTO_BYTE <path> <count>
                    declareOperator("FILE_GOTO_BYTE",
                            new ExecutionInfo.Binary(MODIFY_ARG0_NEVER, env -> (file, pos) -> {
                                try {
                                    String path = file.asString();
                                    env.fileAt(path).seek(8 * pos.numericValue()
                                            .round(MathObject.FLOOR).realPart().num().longValueExact());
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_GOTO_BYTE_START <path>
                    declareOperator("FILE_GOTO_BYTE_START",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                try {
                                    String path = file.asString();
                                    BitRandomAccessStream file1 = env.fileAt(path);
                                    file1.seek(8 * ((file1.bitPos() / 8)));
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_BIT_POSITION <path>
                    declareOperator("FILE_BIT_POS",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                String path = file.asString();
                                try {
                                    return Real.Int.from(env.fileAt(path).bitPos());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_BYTE_POSITION <path>
                    declareOperator("FILE_BYTE_POS",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                String path = file.asString();
                                try {
                                    return Real.divide(Real.Int.from(env.fileAt(path).bitPos()),
                                            Real.from(BigInteger.valueOf(8)));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_SIZE <path>
                    declareOperator("FILE_SIZE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                String path = file.asString();
                                try {
                                    return Real.Int.from(env.fileAt(path).byteLength());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_BIT_SIZE <path>
                    declareOperator("FILE_BIT_SIZE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                String path = file.asString();
                                try {
                                    return Real.Int.from(8L * env.fileAt(path).byteLength());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_MOVE_BITS <path> <count>
                    declareOperator("FILE_MOVE_BITS",
                            new ExecutionInfo.Binary(MODIFY_ARG0_NEVER, env -> (file, pos) -> {
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
                            }));
                    //FILE_MOVE_BYTES <path> <count>
                    declareOperator("FILE_MOVE_BYTES",
                            new ExecutionInfo.Binary(MODIFY_ARG0_NEVER, env -> (file, pos) -> {
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
                            }));
                    //FILE_READ_BIT <path>
                    declareOperator("FILE_READ_BIT",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                try {
                                    String path = file.asString();
                                    return Real.Int.from(env.fileAt(path).readBit());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_READ_BYTE <path>
                    declareOperator("FILE_READ_BYTE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                try {
                                    String path = file.asString();
                                    return Real.Int.from(env.fileAt(path).readBits(8));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_READ_BITS <path> <count>
                    declareOperator("FILE_READ_BITS",
                            new ExecutionInfo.Binary(MODIFY_ARG0_NEVER, env -> (file, count) -> {
                                try {
                                    String path = file.asString();
                                    return Real.Int.from(env.fileAt(path).readBits(count.numericValue()
                                            .round(MathObject.FLOOR).realPart().num().intValueExact()));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_READ_CHAR <path>
                    declareOperator("FILE_READ_CHAR",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                try {
                                    String path = file.asString();
                                    return Real.from(Real.stringAsBigInt(new String(Character.toChars(env.fileAt(path).readUTF8()))));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_READ_STRING <path> <len> //addLater String IO
                    //FILE_READ_BYTES <path> <count>
                    declareOperator("FILE_READ_BYTES",
                            new ExecutionInfo.Binary(MODIFY_ARG0_NEVER, env -> (file, count) -> {
                                try {
                                    String path = file.asString();
                                    return Real.Int.from(env.fileAt(path).readBits(8 * count.numericValue()
                                            .round(MathObject.FLOOR).realPart().num().intValueExact()));
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_READ_BIG_INT <path> <header> <block> <bigBlock>
                    declareOperator("FILE_READ_BIG_INT",
                            new ExecutionInfo(MODIFY_ARG0_NEVER,FLAG_NEEDS_ENVIRONMENT) {
                                @Override
                                public int argCount() {
                                    return 4;
                                }
                                @Override
                                public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                                    if(args.length!=4)
                                        throw new IllegalArgumentException("Wrong Argument count: "+args.length+" expected: 4");
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
                                }
                            });
                    //FILE_READ_FULLY <path>  reads complete File into memory
                    //FILE_WRITE_BIT <path> <value>
                    declareOperator("FILE_WRITE_BIT",
                            new ExecutionInfo.Binary(MODIFY_ARG0_NEVER, env -> (file, value) -> {
                                try {
                                    String path = file.asString();
                                    env.fileAt(path).writeBit(!value.equals(Real.Int.ZERO));
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_WRITE_BYTE <path> <value>
                    declareOperator("FILE_WRITE_BYTE",
                            new ExecutionInfo.Binary(MODIFY_ARG0_NEVER, env -> (file, value) -> {
                                try {
                                    String path = file.asString();
                                    env.fileAt(path).writeByte(value.numericValue().round(MathObject.FLOOR).
                                            realPart().num().intValue() & 0xff);
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_WRITE_CHAR <path> <value>
                    declareOperator("FILE_WRITE_CHAR",
                            new ExecutionInfo.Binary(MODIFY_ARG0_NEVER, env->(file, value) -> {
                                try {
                                    String path = file.asString();
                                    env.fileAt(path).writeUTF8(value.asString().codePointAt(0));
                                    return Real.Int.ZERO;
                                } catch (IOException|IndexOutOfBoundsException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_WRITE_BITS <path> <value> <count>
                    declareOperator("FILE_WRITE_BITS",
                            new ExecutionInfo(MODIFY_ARG0_NEVER,FLAG_NEEDS_ENVIRONMENT) {
                                @Override
                                public int argCount() {
                                    return 3;
                                }
                                @Override
                                public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                                    if(args.length!=3)
                                        throw new IllegalArgumentException("Wrong Argument count: "+args.length+" expected: 3");
                                    try {
                                        String path = args[0].asString();
                                        env.fileAt(path).writeBits(args[1].numericValue().round(MathObject.FLOOR).realPart().num(),
                                                args[2].numericValue().round(MathObject.FLOOR).realPart().num().intValueExact());
                                        return Real.Int.ZERO;
                                    } catch (IOException io) {
                                        System.err.println('\n' + io.toString());
                                        return Real.Int.NEGATIVE_ONE;
                                    }
                                }
                            });
                    //FILE_WRITE_BYTES <path> <value> <count>
                    declareOperator("FILE_WRITE_BYTES",
                            new ExecutionInfo(MODIFY_ARG0_NEVER,FLAG_NEEDS_ENVIRONMENT) {
                                @Override
                                public int argCount() {
                                    return 3;
                                }
                                @Override
                                public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                                    if(args.length!=3)
                                        throw new IllegalArgumentException("Wrong Argument count: "+args.length+" expected: 3");
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
                                }
                            });
                    //FILE_WRITE_STRING <path> <value> <len>
                    //FILE_WRITE_ALL_BITS <path> <value>
                    declareOperator("FILE_WRITE_ALL_BITS",
                            new ExecutionInfo.Binary(MODIFY_ARG0_NEVER, env -> (file, value) -> {
                                try {
                                    String path = file.asString();
                                    BigInteger bigInt = value.numericValue().round(MathObject.FLOOR).realPart().num();
                                    env.fileAt(path).writeBits(bigInt, bigInt.bitLength());
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_WRITE_BIG_INT <path> <value> <header> <block> <bigBlock>
                    declareOperator("FILE_WRITE_BIG_INT",
                            new ExecutionInfo(MODIFY_ARG0_NEVER,FLAG_NEEDS_ENVIRONMENT) {
                                @Override
                                public int argCount() {
                                    return 5;
                                }
                                @Override
                                public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                                    if(args.length!=5)
                                        throw new IllegalArgumentException("Wrong Argument count: "+args.length+" expected: 5");
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
                                }
                            });
                    //FILE_TRUNCATE <path>
                    declareOperator("FILE_0TRUNCATE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                try {
                                    String path = file.asString();
                                    BitRandomAccessStream file1 = env.fileAt(path);
                                    file1.truncateToSize(false);
                                    return Real.from(file1.byteLength());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    declareOperator("FILE_1TRUNCATE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                try {
                                    String path = file.asString();
                                    BitRandomAccessStream file1 = env.fileAt(path);
                                    file1.truncateToSize(true);
                                    return Real.from(file1.byteLength());
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                    //FILE_CLOSE <path>
                    declareOperator("FILE_CLOSE",
                            new ExecutionInfo.Unary(MODIFY_ARG0_NEVER, (env, file) -> {
                                try {
                                    String path = file.asString();
                                    env.closeFileAt(path);
                                    return Real.Int.ZERO;
                                } catch (IOException io) {
                                    System.err.println('\n' + io.toString());
                                    return Real.Int.NEGATIVE_ONE;
                                }
                            }));
                }
                //? GUI

                operatorsInitialized=true;
            }
        }

        public static abstract class ExecutionInfo{
            final int storeMode;
            final int flags;
            protected ExecutionInfo(int storeMode,int flags) {
                this.storeMode = storeMode;
                this.flags=flags;
                if(((flags & FLAG_NARY) == 0) == (this instanceof Nary)){
                    throw new RuntimeException("FLAG_NARY should be exactly if this class is Nary");
                }
            }

            public abstract int argCount();
            public int flags(){
                return flags;
            }
            public abstract MathObject execute(ExecutionEnvironment env, MathObject[] objects);

            static class Dynamic extends ExecutionInfo{
                final int argCount;
                Dynamic(int argCount) {
                    super(MODIFY_ARG0_NEVER,FLAG_DYNAMIC);
                    this.argCount = argCount;
                }
                @Override
                public int argCount() {
                    return argCount;
                }


                @Override
                public MathObject execute(ExecutionEnvironment env, MathObject[] objects) {
                    throw new UnsupportedOperationException();
                }
            }
            static class Unary extends ExecutionInfo{
                final Function<MathObject, MathObject> f;
                final BiFunction<ExecutionEnvironment, MathObject, MathObject> fEnv;
                Unary(int storeMode,Function<MathObject, MathObject> f) {
                    super(storeMode,0);
                    this.f = f;
                    fEnv=null;
                }
                Unary(int storeMode,BiFunction<ExecutionEnvironment, MathObject, MathObject> fEnv) {
                    super(storeMode,FLAG_NEEDS_ENVIRONMENT);
                    f=null;
                    this.fEnv = fEnv;
                }

                @Override
                public int argCount() {
                    return 1;
                }

                @Override
                public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                    if(args.length!=1)
                        throw new IllegalArgumentException("This operator needs exactly one Argument");
                    if(fEnv!=null){
                        return fEnv.apply(env,args[0]);
                    }else {
                        assert f != null;
                        return f.apply(args[0]);
                    }
                }
            }
            static class Binary extends ExecutionInfo{
                final BiFunction<MathObject, MathObject, MathObject> f;
                final Function<ExecutionEnvironment,BiFunction<MathObject, MathObject, MathObject>> fEnv;

                Binary(int storeMode,BiFunction<MathObject, MathObject, MathObject> f) {
                    super(storeMode,0);
                    this.f = f;
                    fEnv=null;
                }
                Binary(int storeMode,Function<ExecutionEnvironment,BiFunction<MathObject, MathObject, MathObject>> fEnv) {
                    super(storeMode,FLAG_NEEDS_ENVIRONMENT);
                    this.fEnv=fEnv;
                    this.f = null;
                }
                @Override
                public int argCount() {
                    return 2;
                }
                @Override
                public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                    if(args.length!=2)
                        throw new IllegalArgumentException("This operator needs exactly two Arguments");
                    if(fEnv!=null){
                        return fEnv.apply(env).apply(args[0],args[1]);
                    }else{
                        assert f != null;
                        return f.apply(args[0],args[1]);
                    }
                }
            }

            static class Nary extends ExecutionInfo{
                final int minArgs;

                final Function<MathObject[], MathObject> f;

                final IntFunction<String> shortCuts;
                final MathObject nilaryValue;

                Nary(int storeMode, int minArgs, Function<MathObject[], MathObject> f, @NotNull IntFunction<String> shortCuts, MathObject nilaryValue){
                    super(storeMode,FLAG_NARY);
                    this.minArgs=minArgs;
                    if(minArgs<=0)
                        throw new RuntimeException("minArgs has to be at least 1");
                    this.f=f;
                    this.shortCuts = shortCuts;
                    this.nilaryValue = nilaryValue;
                }

                @Override
                public int argCount() {
                    return minArgs;
                }

                @Override
                public MathObject execute(ExecutionEnvironment env, MathObject[] objects) {
                    return f.apply(objects);
                }
            }
        }
        public static final class OperatorInfo{
            final public int id;
            final public String name;
            final public ExecutionInfo executionInfo;
            private OperatorInfo(int id, String name, ExecutionInfo executionInfo) {
                this.id = id;
                this.name = name;
                this.executionInfo =executionInfo;
            }
        }
        private Operators(){}
        /**@return OperatorId for the given name as an unsigned int, or -1 if there is no Operator wih the given name*/
        public static long idByName(String name){
            ensureOperatorsInitialized();
            OperatorInfo operatorInfo = operatorNames.get(name);
            if(operatorInfo==null){
                return -1L;
            }
            return operatorInfo.id&0xffffffffL;
        }
        /**@return The name of the Operator with the given id*/
        public static String nameById(int id){
            ensureOperatorsInitialized();
            OperatorInfo operatorInfo = operators.get(id);
            if(operatorInfo==null){
                throw new IllegalArgumentException("No Operator with given id:"+id);
            }
            return operatorInfo.name;
        }

        /**@return the value of the n-ary operator with the given id evaluated with zero arguments*/
        public static MathObject nilaryReplacement(int id){
            ensureOperatorsInitialized();
            OperatorInfo operatorInfo = operators.get(id);
            if ((operatorInfo.executionInfo.flags & FLAG_NARY) != 0) {
                return ((ExecutionInfo.Nary) operatorInfo.executionInfo).nilaryValue;
            } else {
                throw new IllegalArgumentException(operatorInfo.name + " is no N-ary operator");
            }
        }

        /**@return id for a simple replacement-operator to n-ary operator with the given id,
         *  when supplied with the given argCount or -1 if no such operator exists*/
        public static long nAryReplacementId(int id,int argCount){
            ensureOperatorsInitialized();
            OperatorInfo operatorInfo = operators.get(id);
            return getNAryReplacement(argCount, operatorInfo);
        }
        /**@return id for a simple replacement-operator to n-ary operator with the given name,
         *  when supplied with the given argCount  or -1 if no such operator exists*/
        public static long nAryReplacementId(String name,int argCount){
            ensureOperatorsInitialized();
            OperatorInfo operatorInfo = operatorNames.get(name);
            return getNAryReplacement(argCount, operatorInfo);
        }

        private static long getNAryReplacement(int argCount, OperatorInfo operatorInfo) {
            if ((operatorInfo.executionInfo.flags & FLAG_NARY) != 0) {
                String name = ((ExecutionInfo.Nary) operatorInfo.executionInfo).shortCuts.apply(argCount);
                String oldName=operatorInfo.name;
                operatorInfo =operatorNames.get(name);
                if(operatorInfo!=null){
                    if(operatorInfo.executionInfo.argCount()!= argCount){
                        throw new RuntimeException("replacement operator "+ operatorInfo.name+" for "+oldName+" has wrong number of arguments: "
                                + operatorInfo.executionInfo.argCount()+" expected: "+ argCount);
                    }
                    return operatorInfo.id;
                }else{
                    return -1L;
                }
            } else {
                throw new IllegalArgumentException(operatorInfo.name + " is no N-ary operator");
            }
        }

        /**@return The the result of the operator with the given id, under the given arguments*/
        public static MathObject execute(int id, ExecutionEnvironment env, MathObject[] args){
            ensureOperatorsInitialized();
            OperatorInfo operatorInfo = operators.get(id);
            if(operatorInfo==null){
                throw new IllegalArgumentException("No Operator with given id:"+id);
            }
            return operatorInfo.executionInfo.execute(env,args);
        }
        /**@return The the result of the operator with the given id, under the given arguments*/
        public static int storeMode(int id){
            ensureOperatorsInitialized();
            OperatorInfo operatorInfo = operators.get(id);
            if(operatorInfo==null){
                throw new IllegalArgumentException("No Operator with given id:"+id);
            }
            return operatorInfo.executionInfo.storeMode;
        }
        /**@return The number of arguments of the Operator with the given id*/
        public static int argCountById(int id){
            ensureOperatorsInitialized();
            OperatorInfo operatorInfo = operators.get(id);
            if(operatorInfo==null){
                throw new IllegalArgumentException("No Operator with given id:"+id);
            }
            return operatorInfo.executionInfo.argCount();
        }
        /**@return true iff the operator with the given index has dynamic Arguments*/
        public static int flags(int id){
            ensureOperatorsInitialized();
            OperatorInfo operatorInfo = operators.get(id);
            if(operatorInfo==null){
                throw new IllegalArgumentException("No Operator with given id:"+id);
            }
            return operatorInfo.executionInfo.flags();
        }
        /**Declares a new operator with the given name and number of Arguments,
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @throws IllegalArgumentException If there is already an Operator with the given name*/
        private static void declareOperator(String name,ExecutionInfo executionInfo){
            if(!name.toUpperCase(Locale.ROOT).equals(name))
                throw new IllegalArgumentException("name has to be Uppercase:"+name+"!="+name.toUpperCase(Locale.ROOT));
            if(!name.matches("[A-Z]([0-9A-Z_]*[A-Z0-9])?"))
                throw new IllegalArgumentException("illegal operatorName:"+name+
                        "\n names can only contain uppercase latin letters, digits and underscores," +
                        " names cannot start with a digit an underscore cannot be the first or last character");
            if(operatorNames.containsKey(name))
                throw new IllegalArgumentException("There is already an Operator with the given name");
            int id= operators.size();
            OperatorInfo info=new OperatorInfo(id,name,executionInfo);
            operators.put(id,info);
            operatorNames.put(name,info);
        }
    }
}
