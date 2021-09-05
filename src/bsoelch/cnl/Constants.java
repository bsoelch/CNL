package bsoelch.cnl;

import bsoelch.cnl.interpreter.ExecutionEnvironment;
import bsoelch.cnl.math.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

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

    //11110010.[BigInt]([BigInt])+ -> Lambda Header   Lambda:N [expr(LambdaArg(0),...LambdaArg(N-1)]
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
        private Operators(){}
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

        public static final OperatorInfo ID=new OperatorInfo(-1,"ID",MODIFY_ARG0_NEVER,false,1,args->args[0]);

        public static final class OperatorInfo{
            final public int id;
            final public String name;
            final public int storeMode;
            final public boolean isNary;
            final public int minArgs;
            final private Function<MathObject[],MathObject> eval;
            final private BiFunction<ExecutionEnvironment,MathObject[],MathObject> evalEnv;

            private OperatorInfo(int id, String name,int storeMode,boolean isNary,int minArgs,Function<MathObject[],MathObject> eval) {
                this.id = id;
                this.name = name;
                this.storeMode=storeMode;
                this.isNary=isNary;
                this.minArgs = minArgs;
                this.eval=eval;
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
            }

            public boolean isRuntimeOperator(){
                return eval==null&&evalEnv==null;
            }

            public boolean needsEnvironment(){
                return eval==null&&evalEnv!=null;
            }

            public MathObject execute(ExecutionEnvironment env, MathObject[] args) {
                if(args.length<minArgs)
                    throw new IllegalArgumentException("Missing Argument: "+name+" needs at least "+minArgs+" Arguments");
                if((!isNary)&&args.length>minArgs)
                    throw new IllegalArgumentException("Too much Arguments: "+name+" needs at most "+minArgs+" Arguments");
                if(evalEnv!=null){
                    return evalEnv.apply(env,args);
                }else{
                    assert eval != null;
                    return eval.apply(args);
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
                    throw new IllegalArgumentException("There already is a NAry info for the specific name");
                }
                this.isCommutative = isCommutative;
                this.isAssociative = isAssociative;
            }

            public @Nullable OperatorInfo getShortCut(int argCount) {
                return shortCuts.get(argCount);
            }

            public void addShortCut(OperatorInfo shortCut) {
                if(shortCut.isNary)
                    throw new IllegalArgumentException("Cannot declare a Nary operator as an shortcut");
                if(shortCuts.put(shortCut.minArgs,shortCut)!=null)
                    throw new IllegalArgumentException("shortcut for "+shortCut.minArgs+" arguments is already defined");
                if(nAryInfos.put(shortCut,this)!=null){
                    throw new IllegalArgumentException("shortcut "+shortCut.name+" is already attached to another NAryInfo");
                }
            }
        }

        private static boolean operatorsInitialized=false;

        /**ensures that all Operators are initialized*/
        private static void ensureOperatorsInitialized(){
            synchronized (operators){
                if(operatorsInitialized)
                    return;
                //3bit Operators
                declareUnaryOperator(NEGATE,MODIFY_ARG0_ROOT, MathObject::negate);
                declareUnaryOperator(NOT,MODIFY_ARG0_ROOT, MathObject::not);
                //7bit operators
                declareUnaryOperator(INVERT,MODIFY_ARG0_ROOT, MathObject::invert);
                declareBinaryOperator(EQUAL,MODIFY_ARG0_NEVER,
                        (a,b)-> a.equals(b)? Real.Int.ONE:Real.Int.ZERO);
                declareBinaryOperator(NOT_EQUAL,MODIFY_ARG0_NEVER,
                        (a,b)-> a.equals(b)?Real.Int.ZERO:Real.Int.ONE);
                declareBinaryOperator(GREATER_THAN,MODIFY_ARG0_NEVER,
                                (a,b)-> MathObject.compare(a,b)>0?Real.Int.ONE:Real.Int.ZERO);
                declareBinaryOperator(GREATER_EQUAL,MODIFY_ARG0_NEVER,
                                (a,b)-> MathObject.compare(a,b)>=0?Real.Int.ONE:Real.Int.ZERO);
                declareBinaryOperator(ADD,MODIFY_ARG0_ROOT, MathObject::add);
                declareBinaryOperator(SUBTRACT,MODIFY_ARG0_ROOT, MathObject::subtract);
                declareBinaryOperator(MULTIPLY,MODIFY_ARG0_ROOT, MathObject::multiply);
                declareBinaryOperator(DIVIDE,MODIFY_ARG0_ROOT, MathObject::divide);
                declareBinaryOperator(MODULO,MODIFY_ARG0_ROOT, MathObject::mod);
                declareBinaryOperator(COMPLEX,MODIFY_ARG0_NEVER,(a, b)->
                                MathObject.add(a, MathObject.multiply(b, Complex.I)));
                declareUnaryOperator(SQUARE_ABS,MODIFY_ARG0_ROOT, MathObject::sqAbs);
                declareUnaryOperator(CONJUGATE,MODIFY_ARG0_ROOT, MathObject::conjugate);
                declareUnaryOperator(REAL_PART,MODIFY_ARG0_ROOT, MathObject::realPart);
                declareUnaryOperator(IMAGINARY_PART,MODIFY_ARG0_ROOT, MathObject::imaginaryPart);
                declareRuntimeOperator(DYNAMIC_VAR,1);
                //9bit operators
                declareBinaryOperator(AND,MODIFY_ARG0_ROOT, MathObject::floorAnd);
                declareBinaryOperator(OR,MODIFY_ARG0_ROOT, MathObject::floorOr);
                declareBinaryOperator(XOR,MODIFY_ARG0_ROOT, MathObject::floorXor);
                declareBinaryOperator(AND_NOT,MODIFY_ARG0_ROOT, MathObject::floorAndNot);
                declareUnaryOperator(INCREMENT,MODIFY_ARG0_ALWAYS, a-> MathObject.add(a,Real.Int.ONE));
                declareUnaryOperator(DECREMENT,MODIFY_ARG0_ALWAYS, a-> MathObject.subtract(a,Real.Int.ONE));
                declareUnaryOperator(FLOOR,MODIFY_ARG0_ROOT,
                                a-> MathObject.round(a, MathObject.FLOOR));
                declareUnaryOperator(CIEL,MODIFY_ARG0_ROOT,
                                a-> MathObject.round(a, MathObject.CIEL));
                declareUnaryOperator(ROUND,MODIFY_ARG0_ROOT,
                                a-> MathObject.round(a, MathObject.ROUND));
                declareUnaryOperator(GREATER_THAN_0,MODIFY_ARG0_NEVER,
                                a-> MathObject.compare(a,Real.Int.ZERO)>0?Real.Int.ONE:Real.Int.ZERO);
                declareUnaryOperator(GREATER_EQUAL_0,MODIFY_ARG0_NEVER,
                                a-> MathObject.compare(a,Real.Int.ZERO)>=0?Real.Int.ONE:Real.Int.ZERO);
                declareBinaryOperator(POW,MODIFY_ARG0_ROOT, MathObject::pow);
                declareBinaryOperator(MIN,MODIFY_ARG0_ROOT, MathObject::min);
                declareBinaryOperator(MAX,MODIFY_ARG0_ROOT, MathObject::max);
                declareRuntimeOperator(CALL_FUNCTION,0);
                declareOperator(OPTIONAL,MODIFY_ARG0_NEVER,3,false,(args)->
                            MathObject.isTrue(args[0])?args[1]:args[2]
                        );
                //13bit Operators

                //NUM DEN
                //CONCAT_BINARY
                declareBinaryOperator(F_ADD,MODIFY_ARG0_ROOT, MathObject::fAdd);
                //F2X_MULT

                declareBinaryOperator(APPROXIMATE,MODIFY_ARG0_ROOT,
                                (a,b)-> MathObject.approximate(a,b.numericValue().realPart()));
                declareUnaryOperator(BIT_LENGTH,MODIFY_ARG0_ROOT,
                                (a)-> Real.from(a.numericValue().realPart()
                                        .num().abs().bitLength()
                                        - a.numericValue().realPart().den().bitLength()));

                //Strings
                {
                    declareUnaryOperator(STRING_LENGTH,MODIFY_ARG0_ROOT,
                                    (a) -> Real.Int.from(a.asString().length()));
                    declareBinaryOperator(STRING_CONCAT,MODIFY_ARG0_ROOT, MathObject::strConcat);
                    declareBinaryOperator(INT_TO_STRING,MODIFY_ARG0_ROOT,
                                    (a, b) -> Real.from(Real.stringAsBigInt(
                                            a.toString(MathObject.round(b, MathObject.FLOOR)
                                                    .numericValue().realPart().num(), true))));
                    declareBinaryOperator(STRING_TO_INT,MODIFY_ARG0_ROOT,
                                    (a, b) -> MathObject.FromString.safeFromString(
                                            a.asString(), MathObject.round(b, MathObject.FLOOR)
                                                    .numericValue().realPart().num()));

                    declareBinaryOperator("STRING_COMPARE",MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().compareTo(b.asString())));
                    declareUnaryOperator("STRING_LOWERCASE",MODIFY_ARG0_ROOT,
                                    (a) -> Real.from(Real.stringAsBigInt(a.asString().toLowerCase(Locale.ROOT))));
                    declareUnaryOperator("STRING_UPPERCASE",MODIFY_ARG0_ROOT,
                                    (a) -> Real.from(Real.stringAsBigInt(a.asString().toUpperCase(Locale.ROOT))));
                    declareBinaryOperator("STRING_STARTS_WITH",MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().startsWith(b.asString())?1:0));
                    declareBinaryOperator("STRING_ENDS_WITH",MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().endsWith(b.asString())?1:0));
                    declareBinaryOperator("STRING_CONTAINS",MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().contains(b.asString())?1:0));
                    declareBinaryOperator("STRING_STARTS_WITH_IGNORE_CASE",MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().toLowerCase(Locale.ROOT)
                                            .startsWith(b.asString().toLowerCase(Locale.ROOT))?1:0));
                    declareBinaryOperator("STRING_ENDS_WITH_IGNORE_CASE",MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().toLowerCase(Locale.ROOT)
                                            .endsWith(b.asString().toLowerCase(Locale.ROOT))?1:0));
                    declareBinaryOperator("STRING_CONTAINS_IGNORE_CASE",MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().toLowerCase(Locale.ROOT)
                                            .contains(b.asString().toLowerCase(Locale.ROOT))?1:0));
                    declareBinaryOperator("STRING_INDEX_OF",MODIFY_ARG0_ROOT,
                                    (a,b) -> Real.Int.from(a.asString().indexOf(b.asString())));
                    declareBinaryOperator("SUBSTRING_FROM",MODIFY_ARG0_ROOT,
                                    (a,i) -> Real.from(Real.stringAsBigInt(a.asString()
                                            .substring(i.numericValue().realPart().num().intValueExact()))));
                    declareBinaryOperator("SUBSTRING_TO",MODIFY_ARG0_ROOT,
                                    (a,i) -> Real.from(Real.stringAsBigInt(a.asString()
                                            .substring(0,i.numericValue().realPart().num().intValueExact()+1))));
                    declareOperator("SUBSTRING",MODIFY_ARG0_NEVER,3,false,
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
                            });

                    //STRING_CHARS <str>
                    //STRING_SPLIT <str> <regex>

                    //REGEX_...
                }
                //sets/tuples/maps
                {
                    //Type Conversion
                    declareUnaryOperator("NUMERIC_VALUE",MODIFY_ARG0_ROOT,
                                    MathObject::numericValue);
                    declareUnaryOperator("AS_SET",MODIFY_ARG0_ROOT,
                                    MathObject::asSet);
                    declareUnaryOperator("AS_MAP",MODIFY_ARG0_ROOT,
                                    MathObject::asMap);
                    declareUnaryOperator("AS_MATRIX",MODIFY_ARG0_ROOT,
                                    Matrix::asMatrix);
                    //Type Checking
                    declareUnaryOperator("IS_INTEGER",MODIFY_ARG0_ROOT,
                                    o-> (o instanceof Real.Int?Real.Int.ONE:Real.Int.ZERO));
                    declareUnaryOperator("IS_NUMERIC",MODIFY_ARG0_ROOT,
                                    o-> (o instanceof NumericValue?Real.Int.ONE:Real.Int.ZERO));
                    declareUnaryOperator("IS_SET",MODIFY_ARG0_ROOT,
                                            o-> (o instanceof FiniteSet?Real.Int.ONE:Real.Int.ZERO));
                    declareUnaryOperator("IS_MAP",MODIFY_ARG0_ROOT,
                                    o-> (o instanceof FiniteMap?Real.Int.ONE:Real.Int.ZERO));
                    declareUnaryOperator("IS_TUPLE",MODIFY_ARG0_ROOT,
                                    o-> ((o instanceof FiniteMap&&((FiniteMap) o).isTuple())?Real.Int.ONE:Real.Int.ZERO));
                    declareUnaryOperator("IS_MATRIX",MODIFY_ARG0_ROOT,
                                    o-> ((o instanceof FullMatrix ||(o instanceof FiniteMap&&((FiniteMap) o).isMatrix()))
                                            ?Real.Int.ONE:Real.Int.ZERO));

                    declareUnaryOperator("SIZE",MODIFY_ARG0_ROOT,
                                    (o)-> {
                                        if(o instanceof Matrix) {
                                            return Real.from(((Matrix) o).size());
                                        }else if(o instanceof FiniteSet){
                                            return Real.from(((FiniteSet)o).size());
                                        }else if(o instanceof FiniteMap){
                                            return Real.from(((FiniteMap)o).size());
                                        }
                                        return Real.Int.ONE;
                                    });
                    declareUnaryOperator("LEN",MODIFY_ARG0_ROOT,
                                    (o)-> {
                                        if(o instanceof Matrix) {
                                            return Real.from(((Matrix) o).dimensions()[0]);
                                        }else if(o instanceof FiniteSet){
                                            return Real.from(((FiniteSet)o).size());
                                        }else if(o instanceof Tuple){
                                            return Real.from(((Tuple)o).length());
                                        }
                                        return Real.Int.ONE;
                                    });

                    //simple creators (nary creators in Nary section)
                    declareUnaryOperator(WRAP_IN_TUPLE,MODIFY_ARG0_ROOT,
                                    o -> Tuple.create(new MathObject[]{o}));
                    declareUnaryOperator(WRAP_IN_SET,MODIFY_ARG0_ROOT, FiniteSet::from);
                    declareBinaryOperator(NEW_PAIR,MODIFY_ARG0_ROOT,Pair::new);
                    declareBinaryOperator(WRAP2_IN_SET,MODIFY_ARG0_ROOT,FiniteSet::from);
                    declareBinaryOperator(SINGLETON_MAP,MODIFY_ARG0_ROOT, (k,v)->FiniteMap.from(Collections.singletonMap(k,v)));

                    {
                        OperatorInfo newSet=declareOperator(NEW_SET,MODIFY_ARG0_ROOT, 3,true,FiniteSet::from);
                        NAryInfo info=new NAryInfo(FiniteSet.EMPTY_SET,newSet,true,false);
                        info.addShortCut(byName(WRAP_IN_SET));
                        info.addShortCut(byName(WRAP2_IN_SET));
                    }
                    {
                        OperatorInfo newTuple=declareOperator(NEW_TUPLE,MODIFY_ARG0_ROOT, 3,true,Tuple::create);
                        NAryInfo info=new NAryInfo(Tuple.EMPTY_MAP,newTuple,false,false);
                        info.addShortCut(byName(WRAP_IN_TUPLE));
                        info.addShortCut(byName(NEW_PAIR));
                    }
                    {
                        OperatorInfo newMap=declareOperator(NEW_MAP,MODIFY_ARG0_ROOT, 4,true,
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
                                });
                        NAryInfo info=new NAryInfo(Tuple.EMPTY_MAP,newMap,false,false);
                        info.addShortCut(byName(SINGLETON_MAP));
                    }

                    declareUnaryOperator("IDENTITY_MATRIX",MODIFY_ARG0_ROOT,
                                    (l)-> Matrix.identityMatrix(l.numericValue().realPart().num().intValueExact()));
                    declareBinaryOperator("DIAGONAL_MATRIX",MODIFY_ARG0_ROOT,
                                    (l,v)-> Matrix.diagonalMatrix(l.numericValue().realPart().num().intValueExact()
                                            ,v.numericValue()));
                    declareBinaryOperator("INT_RANGE",MODIFY_ARG0_ROOT,
                                    (l,u)->FiniteSet.range(l.numericValue().realPart().round(MathObject.FLOOR),
                                            u.numericValue().realPart().round(MathObject.FLOOR)));
                    //TO_DIAGONAL_MATRIX    MathObject -> "row" -> diagonal
                    //addLater? NEW_MATRIX (Bi-Nary)

                    declareBinaryOperator(CUT,MODIFY_ARG0_ROOT, MathObject::intersect);
                    declareBinaryOperator(UNITE,MODIFY_ARG0_ROOT, MathObject::unite);
                    declareBinaryOperator(SYM_DIFF,MODIFY_ARG0_ROOT, MathObject::symmetricDifference);
                    declareBinaryOperator(DIFF,MODIFY_ARG0_ROOT, MathObject::difference);
                    declareBinaryOperator(TIMES,MODIFY_ARG0_ROOT, MathObject::times);
                    //CONTAINS (Value)
                    //SET_INSERT
                    //SET_REMOVE

                    //CONTAINS_KEY
                    declareBinaryOperator(TUPLE_CONCAT,MODIFY_ARG0_ROOT,MathObject::tupleConcat);
                    //TODO Set/Map/Matrix edit
                    declareBinaryOperator("TUPLE_PUSH_FIRST",MODIFY_ARG0_ROOT,
                                    (l,r)->MathObject.tupleConcat(Tuple.create(new MathObject[]{l}),r));
                    declareBinaryOperator("TUPLE_PUSH_LAST",MODIFY_ARG0_ROOT,
                                    (l,r)->MathObject.tupleConcat(l,Tuple.create(new MathObject[]{r})));
                    //MAP_PUT
                    //TUPLE_INSERT <index>
                    //MAP_GET_FIRST
                    //MAP_GET_LAST
                    //MAP_GET
                    declareBinaryOperator("MAP_GET",MODIFY_ARG0_ROOT,(m,k)->MathObject.asMap(k).evaluateAt(k));
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
                    declareUnaryOperator("MAT_TRANSPOSE",MODIFY_ARG0_ROOT,
                                    (m)->Matrix.asMatrix(m).transpose());
                    declareBinaryOperator("MAT_MULT",MODIFY_ARG0_ROOT,
                                    (l,r)->Matrix.matrixMultiply(Matrix.asMatrix(l),Matrix.asMatrix(r)));
                    declareUnaryOperator("MAT_INV",MODIFY_ARG0_ROOT,
                                    (m)->Matrix.asMatrix(m).invert());
                    declareBinaryOperator("MAT_RDIV",MODIFY_ARG0_ROOT,
                                    (l,r)->Matrix.matrixMultiply(Matrix.asMatrix(l),Matrix.asMatrix(r).invert()));
                    declareBinaryOperator("MAT_LDIV",MODIFY_ARG0_ROOT,
                                    (l,r)->Matrix.matrixMultiply(Matrix.asMatrix(l).invert(),Matrix.asMatrix(r)));
                    declareUnaryOperator("MAT_DET",MODIFY_ARG0_ROOT,
                                    (m)->Matrix.asMatrix(m).determinant());
                    declareUnaryOperator("MAT_DIM",MODIFY_ARG0_ROOT,
                                    (m)->{
                                int[] dim=Matrix.asMatrix(m).dimensions();
                                return new Pair(Real.from(dim[0]),Real.from(dim[1]));
                            });
                    //addLater? allow ranges in GET and SET
                    //MAT_GET mat [i1,i2] {j,k} => [{mat[i1][j],mat[i1][k]},{mat[i2][j],mat[i2][k]}]
                    declareOperator("MAT_GET",MODIFY_ARG0_NEVER,3,false,args->
                                Matrix.asMatrix(args[0]).entryAt(args[1].numericValue().realPart().num().intValueExact()
                                    ,args[2].numericValue().realPart().num().intValueExact())
                            );
                    declareOperator("MAT_SET",MODIFY_ARG0_NEVER,4,false,args->
                            Matrix.asMatrix(args[0]).setEntry(args[1].numericValue().realPart().num().intValueExact()
                                ,args[2].numericValue().realPart().num().intValueExact(),args[3].numericValue()));
                }
                //Nary Operations
                {
                    //addLater? unaryDeep...
                    {
                        OperatorInfo sum=declareOperator("SUM",MODIFY_ARG0_ROOT, 3,true,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::add));
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,sum,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(byName(ADD));
                    }
                    {
                        OperatorInfo deepSum=declareOperator("DEEP_SUM",MODIFY_ARG0_ROOT, 1,true,
                                        (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::add));
                        new NAryInfo(Real.Int.ZERO,deepSum,true,true);
                    }
                    {
                        OperatorInfo prod=declareOperator("PROD",MODIFY_ARG0_ROOT, 3,true,
                                (args)->MathObject.nAryReduce(args,Real.Int.ONE,MathObject::multiply));
                        NAryInfo info=new NAryInfo(Real.Int.ONE,prod,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(byName(MULTIPLY));
                    }
                    {
                        OperatorInfo deepProd=declareOperator("DEEP_PROD",MODIFY_ARG0_ROOT, 1,true,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ONE, NumericValue::multiply));
                        new NAryInfo(Real.Int.ONE,deepProd,true,true);
                    }
                    {
                        OperatorInfo and=declareOperator("NARY_AND",MODIFY_ARG0_ROOT, 3,true,
                                (args)->MathObject.nAryReduce(args,Real.Int.ONE,MathObject::floorAnd));
                        NAryInfo info=new NAryInfo(Real.Int.ONE,and,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(byName(AND));
                    }
                    {
                        OperatorInfo deepAnd=declareOperator("DEEP_AND",MODIFY_ARG0_ROOT, 1,true,
                                        (args)->MathObject.deepNAryReduce(args,Real.Int.ONE, NumericValue::floorAnd));
                        new NAryInfo(Real.Int.ONE,deepAnd,true,true);
                    }
                    {
                        OperatorInfo or=declareOperator("NARY_OR",MODIFY_ARG0_ROOT, 3,true,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::floorOr));
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,or,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(byName(OR));
                    }
                    {
                        OperatorInfo deepOr=declareOperator("DEEP_OR",MODIFY_ARG0_ROOT, 1,true,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::floorOr));
                        new NAryInfo(Real.Int.ZERO,deepOr,true,true);
                    }
                    {
                        OperatorInfo strConcat=declareOperator("NARY_STR_CONCAT",MODIFY_ARG0_ROOT, 3,true,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::strConcat));
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,strConcat,false,true);
                        info.addShortCut(ID);
                        info.addShortCut(byName(STRING_CONCAT));
                    }
                    {
                        OperatorInfo deepStrConcat=declareOperator("DEEP_STR_CONCAT",MODIFY_ARG0_ROOT, 1,true,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::deepStrConcat));
                        new NAryInfo(Real.Int.ZERO,deepStrConcat,false,true);
                    }
                    {
                        OperatorInfo min=declareOperator("NARY_MIN",MODIFY_ARG0_ROOT, 3,true,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::min));
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,min,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(byName(MIN));
                    }
                    {
                        OperatorInfo deepMin=declareOperator("DEEP_MIN",MODIFY_ARG0_ROOT, 1,true,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::min));
                        new NAryInfo(Real.Int.ZERO,deepMin,true,true);
                    }
                    {
                        OperatorInfo max=declareOperator("NARY_MAX",MODIFY_ARG0_ROOT, 3,true,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::max));
                        NAryInfo info=new NAryInfo(Real.Int.ZERO,max,true,true);
                        info.addShortCut(ID);
                        info.addShortCut(byName(MAX));
                    }
                    {
                        OperatorInfo deepMax=declareOperator("DEEP_MAX",MODIFY_ARG0_ROOT, 1,true,
                                (args)->MathObject.deepNAryReduce(args,Real.Int.ZERO, NumericValue::max));
                        new NAryInfo(Real.Int.ZERO,deepMax,true,true);
                    }

                    //NARY_BIN_CONCAT

                    {
                        OperatorInfo times=declareOperator("NARY_TIMES",MODIFY_ARG0_ROOT, 3,true, MathObject::nAryTimes);
                        NAryInfo info=new NAryInfo(FiniteSet.EMPTY_SET,times,false,true);
                        info.addShortCut(ID);
                        info.addShortCut(byName(TIMES));
                    }
                    {
                        OperatorInfo times=declareOperator("NARY_TUPLE_CONCAT",MODIFY_ARG0_ROOT, 1,true,
                                (args)->MathObject.nAryReduce(args,Real.Int.ZERO,MathObject::tupleConcat));
                        NAryInfo info=new NAryInfo(Tuple.EMPTY_MAP,times,false,true);
                        info.addShortCut(ID);
                        info.addShortCut(byName(TUPLE_CONCAT));
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

                operatorsInitialized=true;
            }
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

        private static void checkName(String name) {
            if(!name.toUpperCase(Locale.ROOT).equals(name))
                throw new IllegalArgumentException("name has to be Uppercase:"+ name +"!="+ name.toUpperCase(Locale.ROOT));
            if(!name.matches("[A-Z]([0-9A-Z_]*[A-Z0-9])?"))
                throw new IllegalArgumentException("illegal operatorName:"+ name +
                        "\n names can only contain uppercase latin letters, digits and underscores," +
                        " names cannot start with a digit an underscore cannot be the first or last character");
            if(operatorNames.containsKey(name))
                throw new IllegalArgumentException("There is already an Operator with the given name");
        }

        /**Declares a new operator with the given name and number of Arguments,
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param storeMode StoreMode for handling variables in the first slot one of
         * {@link #MODIFY_ARG0_NEVER}, {@link #MODIFY_ARG0_ROOT}, {@link #MODIFY_ARG0_ALWAYS}
         * @param numArgs Expected Number of Arguments
         * @param eval Function for preforming the Evaluation
         * @throws IllegalArgumentException If there is already an Operator with the given name*/
        private static OperatorInfo declareOperator(String name, int storeMode, int numArgs, boolean isNary, Function<MathObject[],MathObject> eval){
            checkName(name);
            int id= operators.size();
            OperatorInfo info=new OperatorInfo(id,name,storeMode,isNary,numArgs,eval);
            operators.put(id,info);
            operatorNames.put(name,info);
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
         * @see #declareOperator(String, int, int, boolean, Function)  */
        private static void declareRuntimeOperator(String name,int minArgs){
            checkName(name);
            int id= operators.size();
            OperatorInfo info=new OperatorInfo(id,name,MODIFY_ARG0_NEVER,false,minArgs,(Function<MathObject[], MathObject>)null);
            operators.put(id,info);
            operatorNames.put(name,info);
        }

        /**shortcut for the declaration of an unary Operator
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param storeMode StoreMode for handling variables in the first slot one of
         * {@link #MODIFY_ARG0_NEVER}, {@link #MODIFY_ARG0_ROOT}, {@link #MODIFY_ARG0_ALWAYS}
         * @param unEval unary Evaluation Function
         * @see #declareOperator(String, int, int, boolean, Function)  */
        private static void declareUnaryOperator(String name,int storeMode,Function<MathObject,MathObject> unEval){
            declareOperator(name,storeMode,1,false,(args)->unEval.apply(args[0]));
        }
        /**shortcut for the declaration of a binary Operator
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param storeMode StoreMode for handling variables in the first slot one of
         * {@link #MODIFY_ARG0_NEVER}, {@link #MODIFY_ARG0_ROOT}, {@link #MODIFY_ARG0_ALWAYS}
         * @param biEval binary Evaluation Function
         * @see #declareOperator(String, int, int, boolean, Function)  */
        private static void declareBinaryOperator(String name, int storeMode, BinaryOperator<MathObject> biEval){
            declareOperator(name,storeMode,2,false,(args)->biEval.apply(args[0],args[1]));
        }
        /**shortcut for the declaration of an unary environment-dependent Operator
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param unEval unary Evaluation Function
         * @see #declareEnvOperator(String,  int, BiFunction)  */
        private static void declareUnaryEnvOperator(String name, BiFunction<ExecutionEnvironment, MathObject, MathObject> unEval){
            declareEnvOperator(name, 1,(env, args)->unEval.apply(env,args[0]));
        }
        /**shortcut for the declaration of a biary environment-dependent Operator
         * @param name name of the Operator, should be in uppercase and must not contain any whitespace characters nor @:,
         * @param biEval biary Evaluation Function
         * @see #declareEnvOperator(String,  int, BiFunction)  */
        private static void declareBinaryEnvOperator(String name, Function<ExecutionEnvironment, BinaryOperator<MathObject>> biEval){
            declareEnvOperator(name,  2, (env, args) -> biEval.apply(env).apply(args[0], args[1]));
        }
    }
}
