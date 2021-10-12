package bsoelch.cnl.interpreter;

import bsoelch.cnl.*;
import bsoelch.cnl.math.*;
import bsoelch.cnl.math.expressions.LambdaVariable;
import bsoelch.cnl.math.expressions.OperatorNode;

import static bsoelch.cnl.Constants.*;

import java.io.*;
import java.math.BigInteger;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Translator {
    /**Action for End of File*/
    public static final Action EOF=new Action() {
        @Override
        public boolean requiresArg() {
            return false;
        }
        @Override
        public void pushArg(ValuePointer arg) {
            throw new IllegalStateException("No arguments required");
        }

        @Override
        public void writeTo(BitRandomAccessStream target){}

        @Override
        public String stringRepresentation() {
            return "EOF";
        }
    };
    /**Action for End of Program*/
    public static final Action EXIT =new Action() {
        @Override
        public boolean requiresArg() {
            return false;
        }
        @Override
        public void pushArg(ValuePointer arg) {
            throw new IllegalStateException("No arguments required");
        }

        @Override
        public void writeTo(BitRandomAccessStream target) throws IOException {
            target.write(new long[]{Constants.HEADER_CONSTANTS},0,Constants.HEADER_CONSTANTS_LENGTH);
            target.writeBigInt(BigInteger.valueOf(CONSTANT_EXIT),Constants.CONSTANTS_INT_HEADER
                        ,Constants.CONSTANTS_INT_BLOCK,Constants.CONSTANTS_INT_BIG_BLOCK);
        }

        @Override
        public String stringRepresentation() {
            return "EXIT";
        }
    };

    static private final Interpreter.CodePosition NO_POS=new Interpreter.CodePosition(null, false);

    static final ValuePointer ZERO=new ValuePointerImpl(Real.Int.ZERO);
    static final ValuePointer ONE=new ValuePointerImpl(Real.Int.ONE);
    static final ValuePointer I=new ValuePointerImpl(Complex.I);
    static final ValuePointer EMPTY_SET=new ValuePointerImpl(FiniteSet.EMPTY_SET);
    static final ValuePointer EMPTY_MAP=new ValuePointerImpl(Tuple.EMPTY_MAP);

    static ValuePointer wrap(MathObject toWrap){
        if(toWrap==null||toWrap.equals(Real.Int.ZERO))
            return ZERO;
        if(toWrap.equals(Real.Int.ONE))
            return ONE;
        if(toWrap.equals(Complex.I))
            return I;
        return new ValuePointerImpl(toWrap);
    }

    public static int readHeader(BitRandomAccessStream file) throws IOException {
        int header=0;
        int next,count=0,mask=1;
        while((next=file.readBit())==1){
            count++;
            header|=mask;
            mask<<=1;
            if(count>15){
                throw new RuntimeException("Unable to read Arguments with more that 15 bits");
            }
        }
        if(next==-1){
            return -1;
        }else if(count>1){//mask
            long[] tmp=new long[]{header};
            file.readFully(tmp,count+1,count-1);
            header=(int)tmp[0];
        }
        return header;
    }

    /**reads an outId from the given File:
     * OutIds are of the Form:
     * <li> id>=OUT_STR_START -> STR
     * <li> Number: (bigBase?OUT_BIG_BASE_START:0)+[BASE_ID]*OUT_NUMBER_BLOCK_LENGTH+[TYPE_ID]
     * @see #writeOutId(BitRandomAccessStream, int) */
    public static int readOutId(BitRandomAccessStream file) throws IOException {
        int r=file.readBit();
        if(r==0){
            return 0;
        }else if(r==1){
            r=file.readBit();
            long[] tmp=new long[1];
            if(r==0){//10__
                file.readFully(tmp,0,2);
                return (int)(tmp[0]+1);
            }else if(r==1){
                r=file.readBit();
                if(r==0){//110____
                    file.readFully(tmp,0,4);
                    return (int)(tmp[0]+5);
                }else if(r==1){//111_____
                    file.readFully(tmp,0,5);
                    return (int)(tmp[0]+21);
                }
            }
        }
        return -1;//END of File
    }

    /**writes the outId to the given File:
     * OutIds are of the Form:
     * <li> id>=OUT_STR_START -> STR
     * <li> Number: (bigBase?OUT_BIG_BASE_START:0)+[BASE_ID]*OUT_NUMBER_BLOCK_LENGTH+[TYPE_ID]
     * @see #readOutId(BitRandomAccessStream) */
    public static void writeOutId(BitRandomAccessStream file, int id) throws IOException {
        if(id==0){//0
            file.writeBit(false);
        }else{
            id--;
            if(id<4){//10__
                file.write(new long[]{((id&0b11)<<2)|0b01},0,4);
            }else {
                id -= 4;
                if(id<16){//110____
                    file.write(new long[]{((id&0b1111)<<3)|0b011},0,7);
                }else{
                    id-=16;
                    if(id<32){//111_____
                        file.write(new long[]{((id&0b11111)<<3)|0b111},0,8);
                    }else{
                        throw new IllegalArgumentException("Id out out range:"+(id+1+4+16));
                    }
                }
            }
        }
    }

    static public int getBracketID(BitRandomAccessStream file) throws IOException {
        long[] tmp=new long[1];
        int next;
        if((next=file.readBit())==1){//improved BitUsage of 12 possible values
            tmp[0]=1;
            file.readFully(tmp,1,BRACKET_FLAG_LENGTH_ODD-1);
        }else if(next==0){
            file.readFully(tmp,1,BRACKET_FLAG_LENGTH_EVEN-1);
        }else{
            throw new IllegalStateException("Unexpected End of File");
        }
        return (int)tmp[0];
    }

    public static Action nextAction(BitRandomAccessStream code, Context context,
                                    ExecutionEnvironment executionEnvironment, boolean isTopLayer) throws IOException {
        Interpreter.CodePosition prevPos=new Interpreter.CodePosition(code, false);
        int header= readHeader(code);
        if(header==-1){//End of file
            return EOF;
        }
        switch (header){
            case HEADER_OPERATOR:
            case HEADER_OPERATOR_REFERENCE:{
                boolean isRef=(header==HEADER_OPERATOR_REFERENCE);
                BigInteger id= code.readBigInt(OPERATOR_INT_HEADER,OPERATOR_INT_BLOCK,OPERATOR_INT_BIG_BLOCK);
                int intID = id.intValueExact();
                Operators.OperatorInfo operatorInfo=Operators.byId(intID);
                if(isRef&&(operatorInfo.needsEnvironment()||operatorInfo.isRuntimeOperator()))
                    throw new IllegalArgumentException("Cannot create OperatorReference from: "+operatorInfo.name);
                if(operatorInfo.isRuntimeOperator()){
                    switch (operatorInfo.name) {
                        case Operators.DYNAMIC_VAR:
                            return new VarPointer(context,null, false);
                        case Operators.WRITE_DYNAMIC_VAR:
                            return new VarPointer(context,null, true);
                        case Operators.WRITE_VAR:
                            BigInteger varId = code.readBigInt(VAR_INT_HEADER, VAR_INT_BLOCK, VAR_INT_BIG_BLOCK);
                            return new VarPointer(context,Real.from(varId), true);
                        case Operators.CALL_FUNCTION:
                            BigInteger fId = code.readBigInt(FUNCTION_ID_INT_HEADER, FUNCTION_ID_INT_BLOCK, FUNCTION_ID_INT_BIG_BLOCK);
                            return new CallFunction(context, fId);
                        default:
                            throw new IllegalArgumentException("Unknown Dynamic Operator: " + operatorInfo.name);
                    }
                } else if(operatorInfo.isNary){
                    BigInteger numArgs=code.readBigInt(NARY_INT_HEADER,NARY_INT_BLOCK,NARY_INT_BIG_BLOCK);
                    if(isRef){
                        return createOperatorReference(operatorInfo,numArgs.intValueExact()+operatorInfo.minArgs);
                    }else {
                        return new Operator(operatorInfo, numArgs.intValueExact(), executionEnvironment);
                    }
                }else{
                    if(isRef){
                        return createOperatorReference(operatorInfo, operatorInfo.minArgs);
                    }else {
                        return new Operator(operatorInfo, executionEnvironment);
                    }
                }
            }
            case HEADER_VAR:{
                BigInteger id = code.readBigInt(VAR_INT_HEADER, VAR_INT_BLOCK, VAR_INT_BIG_BLOCK);
                return new VarPointer(context,Real.from(id),false);
            }
            case HEADER_INT:{
                BigInteger value = code.readBigInt(INT_HEADER, INT_BLOCK, INT_BIG_BLOCK);
                return wrap(Real.from(value));
            }
            case HEADER_FRACTION:{
                BigInteger a = code.readBigInt(INT_HEADER, INT_BLOCK, INT_BIG_BLOCK).add(BigInteger.ONE);//a always >=1
                BigInteger b = code.readBigInt(INT_HEADER, INT_BLOCK, INT_BIG_BLOCK).add(BIG_INT_TWO);//b always >=2
                return wrap(Real.from(a,b));
            }
            case HEADER_BRACKET:{
                int id = getBracketID(code);
                switch (id) {
                    case BRACKET_FLAG_IF_EQ:
                    case BRACKET_FLAG_IF_NE:
                    case BRACKET_FLAG_WHILE_EQ:
                    case BRACKET_FLAG_WHILE_NE:
                    case BRACKET_FLAG_END_WHILE_EQ:
                    case BRACKET_FLAG_END_WHILE_NE:
                    case BRACKET_FLAG_ELSE:
                    case BRACKET_FLAG_ELIF_EQ:
                    case BRACKET_FLAG_ELIF_NE:
                    case BRACKET_FLAG_END:
                    case BRACKET_FLAG_BREAK:
                        return new BracketDeclaration(context, id, prevPos);
                    case BRACKET_FLAG_DO:
                        return new BracketDeclaration(context, id, new Interpreter.CodePosition(code, false));

                }
            }
            case HEADER_CONSTANTS:{
                BigInteger id= code.readBigInt(CONSTANTS_INT_HEADER,CONSTANTS_INT_BLOCK,CONSTANTS_INT_BIG_BLOCK);
                switch (id.intValueExact()){
                    case CONSTANT_RES:return context.RES;
                    case CONSTANT_ARG_COUNT:return context.COUNT;
                    case CONSTANT_I:return I;
                    case CONSTANT_EXIT:return EXIT;
                    case CONSTANT_EMPTY_SET:return EMPTY_SET;
                    case CONSTANT_EMPTY_MAP:return EMPTY_MAP;
                }
            }
            case HEADER_FUNCTION_ARG:{
                BigInteger id= code.readBigInt(FUNCTION_ARG_INT_HEADER, FUNCTION_ARG_INT_BLOCK, FUNCTION_ARG_INT_BIG_BLOCK);
                return context.argPointer(id);
            }
            case HEADER_FUNCTION_DECLARATION:{
                if(!isTopLayer)
                    throw new IllegalStateException("Function Declarations in Brackets are not allowed");
                BigInteger argCount = code.readBigInt(FUNCTION_ARG_INT_HEADER, FUNCTION_ARG_INT_BLOCK, FUNCTION_ARG_INT_BIG_BLOCK);
                BigInteger id = code.readBigInt(FUNCTION_ID_INT_HEADER, FUNCTION_ID_INT_BLOCK, FUNCTION_ID_INT_BIG_BLOCK);
                return new FunctionDeclaration(id,new Function(context,new Interpreter.CodePosition(code, false)
                        ,argCount.intValueExact()));
            }
            case HEADER_ENVIRONMENT:{
                BigInteger id= code.readBigInt(ENVIRONMENTS_INT_HEADER,ENVIRONMENTS_INT_BLOCK,ENVIRONMENTS_INT_BIG_BLOCK);
                if (id.equals(BigInteger.ZERO)) {
                    return new RunIn();
                } else {
                    return new RunIn(id.subtract(BigInteger.ONE));//offset by one for synchronization with IMPORT
                }
            }
            case HEADER_IMPORT:{
                if(!isTopLayer)
                    throw new IllegalStateException("Imports in Brackets are not allowed");
                BigInteger id= code.readBigInt(ENVIRONMENTS_INT_HEADER,ENVIRONMENTS_INT_BLOCK,ENVIRONMENTS_INT_BIG_BLOCK);
                return new Import(context,id);
            }
            case HEADER_LAMBDA:{
                BigInteger argCount=code.readBigInt(LAMBDA_COUNT_INT_HEADER,LAMBDA_COUNT_INT_BLOCK,LAMBDA_COUNT_INT_BIG_BLOCK);
                int count=argCount.intValueExact()+1;
                LambdaVariable[] vars=new LambdaVariable[count];
                for(int i=0;i<count;i++){
                    BigInteger id = code.readBigInt(LAMBDA_VAR_INT_HEADER, LAMBDA_VAR_INT_BLOCK, LAMBDA_VAR_INT_BIG_BLOCK);
                    for(int j=0;j<i;j++){//skip already existing ids
                        if(id.compareTo(vars[j].getId())>=0)
                            id=id.add(BigInteger.ONE);
                    }
                    vars[i]=new LambdaVariable(id);
                }
                return new BindLambda(vars);
            }
            case HEADER_LAMBDA_VARIABLE:{
                BigInteger id = code.readBigInt(LAMBDA_VAR_INT_HEADER, LAMBDA_VAR_INT_BLOCK, LAMBDA_VAR_INT_BIG_BLOCK);
                return wrap(LambdaExpression.from(new LambdaVariable(id),new LambdaVariable[0]));
            }
            case HEADER_IN:{
                long[] tmp=new long[1];
                code.readFully(tmp,0, IN_TYPES_LENGTH);
                BigInteger base=null;
                if(tmp[0]==IN_TYPE_BASE_N){
                    base=code.readBigInt(IO_INT_HEADER,IO_INT_BLOCK,IO_INT_BIG_BLOCK);
                }
                return new Input((int)tmp[0],base);
            }
            case HEADER_OUT:
            case HEADER_OUT_NEW_LINE:{
                int id= readOutId(code);
                boolean isNumber,useSmallBase;
                BigInteger base;
                int type;
                if(id>= OUT_STR){
                    isNumber=false;
                    useSmallBase=false;
                    base=BigInteger.ZERO;
                    type=id;
                }else{
                    isNumber=true;
                    useSmallBase=id<OUT_BIG_BASE_START;
                    id%=OUT_BIG_BASE_START;
                    switch (id/OUT_NUMBER_BLOCK_LENGTH){
                        case OUT_BASE_FLAG_DEC:
                            base=BigInteger.TEN;break;
                        case OUT_BASE_FLAG_BIN:
                            base=BIG_INT_TWO;break;
                        case OUT_BASE_FLAG_DOZ:
                            base=BIG_INT_TWELVE;break;
                        case OUT_BASE_FLAG_HEX:
                            base=BIG_INT_SIXTEEN;break;
                        case OUT_BASE_FLAG_BASE_N:
                            base=code.readBigInt(IO_INT_HEADER, IO_INT_BLOCK, IO_INT_BIG_BLOCK)
                                    .add(OUT_BASE_OFFSET);
                            break;
                        default:throw new RuntimeException("Unknown BaseType:"+id/OUT_NUMBER_BLOCK_LENGTH);
                    }
                    type=id%OUT_NUMBER_BLOCK_LENGTH;
                }
                return new Output(isNumber,useSmallBase, header==HEADER_OUT_NEW_LINE, base,type);
            }
            default:throw new IllegalArgumentException("Unknown Header: 0b"+Integer.toBinaryString(header));
        }
    }


    static private final int STATE_CODE=0,STATE_VALUE=1,STATE_MATRIX=2,STATE_STRING=3,STATE_COMMENT=4;

    private static String nextElement(Reader code) throws IOException {
        StringBuilder cache=new StringBuilder();
        int s0=0;
        int state=STATE_CODE,layer=0;
        for(int r=code.read();r!=-1;r=code.read()){
            char c=(char) r;
            cache.append(c);
            switch (state) {
                case STATE_CODE:
                    if (Character.isWhitespace(c)) {
                        String tmp=cache.toString().trim();
                        if (tmp.isEmpty()) {
                            cache.setLength(0);
                        } else {
                            return tmp;
                        }
                    } else {
                        switch (c) {
                            case '{':
                                layer = 1;
                                state = STATE_MATRIX;
                                break;
                            case '(':
                                layer = 1;
                                state = STATE_VALUE;
                                break;
                            case '"'://String
                            case '\'':
                                state = STATE_STRING;
                                s0=cache.length()-1;
                                break;
                            case '%'://Comment
                                String tmp=cache.substring(0,cache.length()-1).trim();
                                if (tmp.isEmpty()) {
                                    cache.setLength(0);
                                    cache.append(c);
                                } else {
                                    return tmp;
                                }
                                state = STATE_COMMENT;
                                break;
                        }
                    }
                    break;
                case STATE_STRING:
                    if (c == cache.charAt(s0)) {//end string
                        state = STATE_CODE;
                    } else if (c == '\\') {//skip next char
                        r=code.read();
                        if (r == -1) {
                            throw new IllegalArgumentException("Unexpected End of File");
                        } else {
                            cache.append((char)r);
                        }
                    }
                    break;
                case STATE_VALUE:
                    if (c == ')') {//end value
                        layer--;
                        if (layer == 0) {
                            state = STATE_CODE;
                        }
                    } else if (c == '(') {
                        layer++;
                    }
                    break;
                case STATE_MATRIX:
                    if (c == '}') {//end matrix
                        layer--;
                        if (layer == 0) {
                            state = STATE_CODE;
                        }
                    } else if (c == '{') {
                        layer++;
                    }
                    break;
                case STATE_COMMENT:
                    if (c == '\n' || c == '\r') {//end comment with line
                        cache.setLength(0);//ignore comment
                        state = STATE_CODE;
                    }
                    break;
            }
        }
        if (state != STATE_COMMENT) {
            if(state!=STATE_CODE){
                throw new IllegalArgumentException("Unfinished Bracket or String");
            }else {
                String tmp=cache.toString().trim();
                if(tmp.isEmpty()){
                    return null;
                }else{
                    return tmp;
                }
            }
        }else{
            return null;
        }
    }

    /**reads the next action from the given Reader
     * @param bitCode BitRandomAccessStream used for remembering dode positions,
     *                may be null if code positions are not relevant*/
    static Action nextAction(Reader code, BitRandomAccessStream bitCode, Context context,
                             ExecutionEnvironment executionEnvironment, boolean isTopLayer) throws IOException {
        String str;
        Interpreter.CodePosition prevPos=bitCode==null?NO_POS:new Interpreter.CodePosition(bitCode, true);
        do {
            str=nextElement(code);
            if (str == null)
                return EOF;
        }while(str.isEmpty());
        if(Character.isDigit(str.charAt(0))||str.charAt(0)=='\''||str.charAt(0)=='"'||str.charAt(0)=='('
                ||str.charAt(0)=='{'||str.charAt(0)=='§'||str.charAt(0)=='#'
                ||str.charAt(0)=='$'||str.charAt(0)=='@'||str.charAt(0)=='-'||(str.charAt(0)=='['&&str.endsWith("]"))){//Number
            return wrap(MathObject.FromString.fromString(str, DEFAULT_BASE));
        }else if(str.toUpperCase(Locale.ROOT).startsWith("OUT_")){//Output
            str=str.substring(4);//remove Out from String
            boolean newLine=false;
            if(str.startsWith("LINE_")){
                str=str.substring(5);
                newLine=true;
            }else if(str.startsWith("LN_")){
                str=str.substring(3);
                newLine=true;
            }
            if(str.equals("STR")||str.equals("STRING")){
               return new Output(false,true, newLine, BigInteger.ZERO,OUT_STR);
            }else if(str.equals("STR_INTS")||str.equals("STRING_INTS")){
                return new Output(false,true, newLine, BigInteger.ZERO,OUT_STR_INT);
            }else if(str.startsWith("NUMBER")){
                str=str.substring(6);
                boolean bigBase;
                if(str.startsWith("_BIG_BASE")){
                    bigBase=true;
                    str=str.substring(9);
                }else{
                    bigBase=false;
                }
                BigInteger baseValue=BigInteger.TEN;
                if(str.startsWith("_BIN")){
                    str=str.substring(4);
                    baseValue=BIG_INT_TWO;
                }else if(str.startsWith("_DOZ")){
                    str=str.substring(4);
                    baseValue=BIG_INT_TWELVE;
                }else if(str.startsWith("_HEX")){
                    str=str.substring(4);
                    baseValue=BIG_INT_SIXTEEN;
                }else if(str.startsWith("_BASE")){
                    str=str.substring(5);
                    int i=str.indexOf('_');
                    if(i==-1)
                        i=str.length();
                    baseValue=new BigInteger(str.substring(0,i));
                    str=str.substring(i);
                    if(baseValue.compareTo(BigInteger.ONE)<=0){//detect invalid bases
                        throw new IllegalArgumentException(baseValue+" is not a valid base, has to be at least 2");
                    }
                }
                int type;
                switch (str){
                    case "":{
                        type=OUT_FLAG_FRACTION;
                    }break;
                    case "_FIXED":{
                        type=OUT_FLAG_FIXED_POINT_EXACT;
                    }break;
                    case "_FLOAT":{
                        type=OUT_FLAG_FLOAT_EXACT;
                    }break;
                    case "_FIXED_APPROX":{
                        type= OUT_FLAG_FIXED_POINT_APPROX;
                    }break;
                    case "_FLOAT_APPROX":{
                        type= OUT_FLAG_FLOAT_APPROX;
                    }break;
                    default:
                        throw new IllegalArgumentException("Unexpected NumberType:"+str);
                }
                return new Output(true,!bigBase, newLine, baseValue,type);
            }else{
                throw new IllegalArgumentException("Unknown OutputType:"+str);
            }
        }else if(str.toUpperCase(Locale.ROOT).startsWith("IN_")){//Input
            if(str.startsWith("IN_BASE")){
                BigInteger base=new BigInteger(str.substring(7));
                return new Input(IN_TYPE_BASE_N,base);
            }else{
                switch (str){
                    case "IN_CHAR":return new Input(IN_TYPE_CHAR,null);
                    case "IN_WORD":return new Input(IN_TYPE_WORD,null);
                    case "IN_LINE":return new Input(IN_TYPE_LINE,null);
                    case "IN_BIN":return new Input(IN_TYPE_BIN,null);
                    case "IN_DEC":return new Input(IN_TYPE_DEC,null);
                    case "IN_DOZ":return new Input(IN_TYPE_DOZ,null);
                    case "IN_HEX":return new Input(IN_TYPE_HEX,null);
                    default:throw new IllegalStateException("Unexpected InputType:"+str);
                }
            }
        }else if(str.toUpperCase(Locale.ROOT).startsWith("VAR")){//Var
            BigInteger id = new BigInteger(str.substring(3));
            if(id.signum()==-1)
                throw new IllegalArgumentException("Negative Id");
            return new VarPointer(context,Real.from(id),false);
        }else if(str.length()>3&&str.toUpperCase(Locale.ROOT).startsWith("ARG")){//Function Argument
            BigInteger id = new BigInteger(str.substring(3));
            if(id.signum()==-1)
                throw new IllegalArgumentException("Negative Id");
            return context.argPointer(id);
        }else if(str.toUpperCase(Locale.ROOT).startsWith("X")&&
                (str.length()>1&&Character.isDigit(str.charAt(1)))){//Lambda-Var
            BigInteger id = new BigInteger(str.substring(1));
            if(id.signum()==-1)
                throw new IllegalArgumentException("Negative Id");
            return wrap(LambdaExpression.from(new LambdaVariable(id),new LambdaVariable[0]));
        }else if(str.toUpperCase(Locale.ROOT).startsWith("NEW_FUNC:")&&str.endsWith("[")){//Function declaration
            if(!isTopLayer)
                throw new IllegalStateException("Function declarations in Brackets are not allowed");
            String params=str.substring(9,str.length()-1);
            BigInteger args=new BigInteger(params.substring(0,params.indexOf(',')));
            BigInteger id=new BigInteger(params.substring(params.indexOf(',')+1));
            if(args.signum()==-1)
                throw new IllegalArgumentException("Negative Id");
            return new FunctionDeclaration(id,new Function(context,bitCode==null?NO_POS:new Interpreter.CodePosition(bitCode, true),args.intValueExact()));
        }else if(str.toUpperCase(Locale.ROOT).startsWith("LAMBDA:")){//Lambda expression
            String[] params=str.substring(7).split(",");
            LambdaVariable[] vars=new LambdaVariable[params.length];
            for(int i=0;i< params.length;i++){
                if(params[i].toUpperCase(Locale.ROOT).startsWith("X")){
                    params[i]=params[i].substring(1);
                }
                vars[i]=new LambdaVariable(new BigInteger(params[i]));
            }
            return new BindLambda(vars);
        }else if(str.toUpperCase(Locale.ROOT).startsWith("RUN_IN:")){//Environment
            BigInteger id=new BigInteger(str.substring(7));
            if(id.signum()==-1)
                throw new IllegalArgumentException("Negative Id");
            id=id.add(BigInteger.ONE);//offset
            return new RunIn(id);
        }else if(str.toUpperCase(Locale.ROOT).startsWith("IMPORT_TO:")){//Import
            if(!isTopLayer)
                throw new IllegalStateException("Imports declarations in Brackets are not allowed");
            BigInteger id=new BigInteger(str.substring(10));
            if(id.signum()==-1)
                throw new IllegalArgumentException("Negative Id");
            return new Import(context,id);
        }else{
            boolean isRef=false;
            if(str.startsWith("&")){
                isRef=true;
                str=str.substring(1);
            }
            if(str.contains(":")){
                //detect N-ary operators
                String name=str.substring(0,str.lastIndexOf(':'));
                String[] counts=str.substring(str.lastIndexOf(':')+1).split(",");
                Operators.OperatorInfo operatorInfo=Operators.byNameOrNull(name.toUpperCase(Locale.ROOT));
                if(operatorInfo!=null) {
                    if(isRef&&(operatorInfo.isRuntimeOperator()||operatorInfo.needsEnvironment()))
                        throw new IllegalArgumentException("Cannot create OperatorReference from: "+operatorInfo.name);
                    if (counts.length > 1) {
                        throw new UnsupportedOperationException("Multidimensional N-ary not yet supported");
                    } else {
                        if(operatorInfo.name.equals(Operators.WRITE_VAR)) {
                            return new VarPointer(context, Real.from(new BigInteger(counts[0])),true);
                        } else if(operatorInfo.name.equals(Operators.CALL_FUNCTION)) {
                            return new CallFunction(context, new BigInteger(counts[0]));
                        } else if (!operatorInfo.isNary) {
                            throw new IllegalArgumentException("N-ary arguments for non N-ary operator");
                        } else {
                            Operators.NAryInfo nAryInfo = Operators.nAryInfo(operatorInfo);
                            if (nAryInfo == null)
                                throw new RuntimeException("N-Ary Operator without N-AryInfo:" + operatorInfo.name);
                            int count = new BigInteger(counts[0]).intValueExact();
                            if (count == 0) {
                                return wrap(nAryInfo.nilaryReplacement);
                            } else {
                                int minArgs = operatorInfo.minArgs;
                                Operators.OperatorInfo replace = nAryInfo.getShortCut(count);
                                if (replace != null) {
                                    if(isRef){
                                        return createOperatorReference(replace,count);
                                    }else {
                                        return new Operator(replace, executionEnvironment);
                                    }
                                } else if (count < minArgs) {
                                    throw new IllegalArgumentException("Number of Arguments for NAry operator " + name + " is less than " +
                                            "the minimum allowed value " + minArgs);
                                } else {
                                    if(isRef){
                                        return createOperatorReference(operatorInfo,count);
                                    }else {
                                        return new Operator(operatorInfo, count - minArgs, executionEnvironment);
                                    }
                                }
                            }
                        }
                    }
                }
            }else{//operator without n-ary args -> exactly minArgs arguments
                Operators.OperatorInfo operatorInfo = Operators.byNameOrNull(str.toUpperCase(Locale.ROOT));
                if(operatorInfo!=null) {
                    if(isRef){
                        return createOperatorReference(operatorInfo,operatorInfo.minArgs);
                    }else {
                        if(operatorInfo.isRuntimeOperator()){
                            if(operatorInfo.name.equals(Operators.DYNAMIC_VAR)) {
                                return new VarPointer(context, null,false);
                            }else if(operatorInfo.name.equals(Operators.WRITE_DYNAMIC_VAR)) {
                                return new VarPointer(context, null,true);
                            }else{
                                throw new IllegalArgumentException("unexpected name for runtime-operator:"+operatorInfo);
                            }
                        }else {
                            return new Operator(operatorInfo, executionEnvironment);
                        }
                    }
                }
            }
            switch (str.toUpperCase(Locale.ROOT)){
                //Brackets
                case "[?": {
                    return new BracketDeclaration(context,BRACKET_FLAG_IF_NE,prevPos);
                }
                case "[!":{
                    return new BracketDeclaration(context,BRACKET_FLAG_IF_EQ,prevPos);
                }
                case "[.?":{
                    return new BracketDeclaration(context,BRACKET_FLAG_WHILE_NE,prevPos);
                }
                case "[.!":{
                    return new BracketDeclaration(context,BRACKET_FLAG_WHILE_EQ,prevPos);
                }
                case "[":{
                    return new BracketDeclaration(context,BRACKET_FLAG_DO,bitCode==null?NO_POS:new Interpreter.CodePosition(bitCode, true));
                }
                case "|":{
                    return new BracketDeclaration(context,BRACKET_FLAG_ELSE,prevPos);
                }
                case "|!":{
                    return new BracketDeclaration(context,BRACKET_FLAG_ELIF_EQ,prevPos);
                }
                case "|?":{
                    return new BracketDeclaration(context,BRACKET_FLAG_ELIF_NE,prevPos);
                }
                case "]":{
                    return new BracketDeclaration(context,BRACKET_FLAG_END,prevPos);
                }
                case "]!":{
                    return new BracketDeclaration(context,BRACKET_FLAG_END_WHILE_EQ,prevPos);
                }
                case "]?":{
                    return new BracketDeclaration(context,BRACKET_FLAG_END_WHILE_NE,prevPos);
                }
                case "BREAK":{
                    return new BracketDeclaration(context,BRACKET_FLAG_BREAK,prevPos);
                }

                //Constants
                case "RES":
                    return context.RES;
                case "ARG_COUNT":
                    return context.COUNT;
                case "I":
                    return I;
                case "EMPTY_SET":
                    return wrap(FiniteSet.EMPTY_SET);
                case "EMPTY_MAP":
                    return wrap(Tuple.EMPTY_MAP);
                case "EXIT":
                    return EXIT;
                //Import/Environment in ROOT
                case "RUN_ROOT":
                    return new RunIn();

                default:throw new IllegalArgumentException("Syntax Error: Illegal Command '"+str+"'");
            }
        }
    }

    private static ValuePointer createOperatorReference(Operators.OperatorInfo operatorInfo, int argCount) {
        LambdaVariable[] args=new LambdaVariable[argCount];
        for(int i=0;i<args.length;i++){
            args[i]=new LambdaVariable(BigInteger.valueOf(i));
        }
        return wrap(LambdaExpression.from(OperatorNode.from(operatorInfo,args),args));
    }

    /** Writes an  Operator to a BitRandomAccessStream
     * @param target Stream the Operator should be written to
     * @param operatorInfo the Operator that should be written
     * @param argCount the number of Arguments (for N-ary Operators)
     * @param isReference if true the operator is written as an operator-reference
     * */
    public static void writeOperator(BitRandomAccessStream target, Operators.OperatorInfo operatorInfo, int argCount,
                                     boolean isReference) throws IOException {
        if(isReference){
            target.write(new long[]{HEADER_OPERATOR_REFERENCE},0, HEADER_OPERATOR_REFERENCE_LENGTH);
        }else{
            target.write(new long[]{HEADER_OPERATOR},0, HEADER_OPERATOR_LENGTH);
        }
        target.writeBigInt(BigInteger.valueOf(operatorInfo.id), OPERATOR_INT_HEADER,OPERATOR_INT_BLOCK,OPERATOR_INT_BIG_BLOCK);
        if(operatorInfo.isNary){
            target.writeBigInt(BigInteger.valueOf(argCount-operatorInfo.minArgs), NARY_INT_HEADER,NARY_INT_BLOCK,NARY_INT_BIG_BLOCK);
        }
    }

    public static void writeValue(BitRandomAccessStream target, MathObject value) throws IOException {
        if(value instanceof LambdaExpression){
            ((LambdaExpression)value).writeTo(target);
        }else if(value instanceof NumericValue){
            writeNumeric(target, (NumericValue) value);
        }else if(value instanceof FiniteSet){
            int size=((FiniteSet) value).size();
            if(size==0){
                target.write(new long[]{Constants.HEADER_CONSTANTS},0,Constants.HEADER_CONSTANTS_LENGTH);
                target.writeBigInt(BigInteger.valueOf(CONSTANT_EMPTY_SET),Constants.CONSTANTS_INT_HEADER
                        ,Constants.CONSTANTS_INT_BLOCK,Constants.CONSTANTS_INT_BIG_BLOCK);
            }else{
                Operators.OperatorInfo opNewSet = Operators.byName(Operators.NEW_SET);
                Operators.NAryInfo nAryInfo=Operators.nAryInfo(opNewSet);
                if(nAryInfo==null)
                    throw new RuntimeException("No NAryInfo for N-Ary operator:"+opNewSet);
                Operators.OperatorInfo replace=nAryInfo.getShortCut(size);
                target.write(new long[]{HEADER_OPERATOR}, 0, HEADER_OPERATOR_LENGTH);
                if(replace!=null) {
                    target.writeBigInt(BigInteger.valueOf(replace.id), OPERATOR_INT_HEADER
                            , OPERATOR_INT_BLOCK, OPERATOR_INT_BIG_BLOCK);
                }else{
                    target.writeBigInt(BigInteger.valueOf(opNewSet.id), OPERATOR_INT_HEADER
                            , OPERATOR_INT_BLOCK, OPERATOR_INT_BIG_BLOCK);
                    target.writeBigInt(BigInteger.valueOf(size - opNewSet.minArgs), NARY_INT_HEADER
                            , NARY_INT_BLOCK, NARY_INT_BIG_BLOCK);
                }
                for (MathObject o : (FiniteSet) value) {
                    writeValue(target, o);
                }
            }
        }else if(value instanceof Tuple&&((Tuple) value).isFullTuple()){
            int length=((Tuple) value).length();
            if(length==0){
                target.write(new long[]{Constants.HEADER_CONSTANTS},0,Constants.HEADER_CONSTANTS_LENGTH);
                target.writeBigInt(BigInteger.valueOf(CONSTANT_EMPTY_MAP),Constants.CONSTANTS_INT_HEADER
                        ,Constants.CONSTANTS_INT_BLOCK,Constants.CONSTANTS_INT_BIG_BLOCK);
            }else{
                Operators.OperatorInfo opNewTuple=Operators.byName(Operators.NEW_TUPLE);
                Operators.NAryInfo nAryInfo=Operators.nAryInfo(opNewTuple);
                if(nAryInfo==null)
                    throw new RuntimeException("No NAryInfo for N-Ary operator:"+opNewTuple);
                Operators.OperatorInfo replace=nAryInfo.getShortCut(length);
                target.write(new long[]{HEADER_OPERATOR}, 0, HEADER_OPERATOR_LENGTH);
                if (replace != null) {
                    target.writeBigInt(BigInteger.valueOf(replace.id), OPERATOR_INT_HEADER
                            , OPERATOR_INT_BLOCK, OPERATOR_INT_BIG_BLOCK);
                } else {
                    long id = opNewTuple.id;
                    target.writeBigInt(BigInteger.valueOf(id), OPERATOR_INT_HEADER
                            , OPERATOR_INT_BLOCK, OPERATOR_INT_BIG_BLOCK);
                    target.writeBigInt(BigInteger.valueOf(length - opNewTuple.minArgs), NARY_INT_HEADER
                            , NARY_INT_BLOCK, NARY_INT_BIG_BLOCK);
                }
                for(MathObject e:(Tuple)value){
                    writeValue(target,e);
                }
            }
        }else if(value instanceof FiniteMap){
            int size=((FiniteMap) value).size();
            if(value instanceof Tuple.SparseTuple){
                if(((Tuple.SparseTuple) value).evaluateAt(Real.from(((Tuple.SparseTuple) value).length()-1)).equals(Real.Int.ZERO)){
                    size++;//save tuples with last entry zero in correct size
                }
            }
            if(size==0){
                target.write(new long[]{Constants.HEADER_CONSTANTS},0,Constants.HEADER_CONSTANTS_LENGTH);
                target.writeBigInt(BigInteger.valueOf(CONSTANT_EMPTY_MAP),Constants.CONSTANTS_INT_HEADER
                        ,Constants.CONSTANTS_INT_BLOCK,Constants.CONSTANTS_INT_BIG_BLOCK);
            }else{
                Operators.OperatorInfo opNewMap=Operators.byName(value instanceof Tuple?Operators.SPARSE_TUPLE:Operators.NEW_MAP);
                Operators.NAryInfo nAryInfo=Operators.nAryInfo(opNewMap);
                if(nAryInfo==null)
                    throw new RuntimeException("No NAryInfo for N-Ary operator:"+opNewMap.name);
                Operators.OperatorInfo replace=nAryInfo.getShortCut(2*size);
                target.write(new long[]{HEADER_OPERATOR}, 0, HEADER_OPERATOR_LENGTH);
                if (replace != null) {
                    target.writeBigInt(BigInteger.valueOf(replace.id), OPERATOR_INT_HEADER
                            , OPERATOR_INT_BLOCK, OPERATOR_INT_BIG_BLOCK);
                } else {
                    long id = opNewMap.id;
                    target.writeBigInt(BigInteger.valueOf(id), OPERATOR_INT_HEADER
                            , OPERATOR_INT_BLOCK, OPERATOR_INT_BIG_BLOCK);
                    target.writeBigInt(BigInteger.valueOf(2L*size - opNewMap.minArgs), NARY_INT_HEADER
                            , NARY_INT_BLOCK, NARY_INT_BIG_BLOCK);
                }
                for (Iterator<Pair> it = ((FiniteMap) value).mapIterator(); it.hasNext(); ) {
                    Pair e = it.next();
                    writeValue(target,e.a);
                    writeValue(target,e.b);
                }
            }
        }else{
            throw new IllegalArgumentException("Unknown valueType:"+value.getClass());
        }
    }


    private static void writeNumeric(BitRandomAccessStream target, NumericValue value) throws IOException {
        if(value.isReal()){
            writeReal(target,value.realPart());
        }else if(value instanceof Complex){
            if(value.equals(Complex.I)){
                target.write(new long[]{Constants.HEADER_CONSTANTS},0,Constants.HEADER_CONSTANTS_LENGTH);
                target.writeBigInt(BigInteger.valueOf(CONSTANT_I),Constants.CONSTANTS_INT_HEADER
                        ,Constants.CONSTANTS_INT_BLOCK,Constants.CONSTANTS_INT_BIG_BLOCK);
            }else if(value.equals(Complex.I.negate())){
                target.write(new long[]{HEADER_OPERATOR},0,HEADER_OPERATOR_LENGTH);
                target.writeBigInt(BigInteger.valueOf(Operators.byName(Operators.NEGATE).id),OPERATOR_INT_HEADER
                        ,OPERATOR_INT_BLOCK,OPERATOR_INT_BIG_BLOCK);

                target.write(new long[]{Constants.HEADER_CONSTANTS},0,Constants.HEADER_CONSTANTS_LENGTH);
                target.writeBigInt(BigInteger.valueOf(CONSTANT_I),Constants.CONSTANTS_INT_HEADER
                        ,Constants.CONSTANTS_INT_BLOCK,Constants.CONSTANTS_INT_BIG_BLOCK);
            }else{//len(MULT A I)>=len(CMPLX 0 A) -> encode all other complex number with CMPLX
                if(value.realPart().compareTo(Real.Int.ZERO)<0
                        && value.imaginaryPart().compareTo(Real.Int.ZERO)<0){
                    //real and int both negative -> move NEG in front of CMPLX
                    target.write(new long[]{HEADER_OPERATOR},0,HEADER_OPERATOR_LENGTH);
                    target.writeBigInt(BigInteger.valueOf(Operators.byName(Operators.NEGATE).id),OPERATOR_INT_HEADER
                            ,OPERATOR_INT_BLOCK,OPERATOR_INT_BIG_BLOCK);

                    target.write(new long[]{HEADER_OPERATOR},0,HEADER_OPERATOR_LENGTH);
                    target.writeBigInt(BigInteger.valueOf(Operators.byName(Operators.COMPLEX).id),OPERATOR_INT_HEADER
                            ,OPERATOR_INT_BLOCK,OPERATOR_INT_BIG_BLOCK);
                    value = value.negate();
                }else{
                    target.write(new long[]{HEADER_OPERATOR},0,HEADER_OPERATOR_LENGTH);
                    target.writeBigInt(BigInteger.valueOf(Operators.byName(Operators.COMPLEX).id),OPERATOR_INT_HEADER
                            ,OPERATOR_INT_BLOCK,OPERATOR_INT_BIG_BLOCK);
                }
                writeReal(target,value.realPart());
                writeReal(target,value.imaginaryPart());
            }
        }
    }

    private static void writeReal(BitRandomAccessStream target, Real value) throws IOException {
        if(value.compareTo(Real.Int.ZERO)<0){
            target.write(new long[]{HEADER_OPERATOR},0,HEADER_OPERATOR_LENGTH);
            target.writeBigInt(BigInteger.valueOf(Operators.byName(Operators.NEGATE).id),OPERATOR_INT_HEADER,OPERATOR_INT_BLOCK,OPERATOR_INT_BIG_BLOCK);
            value=value.abs();
        }
        if(value.isInt()){
            target.write(new long[]{HEADER_INT},0,HEADER_INT_LENGTH);
            target.writeBigInt(value.num(),INT_HEADER,INT_BLOCK,INT_BIG_BLOCK);
        }else{//invert int. -> 11bits   fraction[1]. -> 10 bits
            target.write(new long[]{HEADER_FRACTION},0,HEADER_FRACTION_LENGTH);
            target.writeBigInt(value.num().subtract(BigInteger.ONE),INT_HEADER,INT_BLOCK,INT_BIG_BLOCK);
            target.writeBigInt(value.den().subtract(BIG_INT_TWO),INT_HEADER,INT_BLOCK,INT_BIG_BLOCK);
        }
    }
    public static void writeLambdaHeader(BitRandomAccessStream target, LambdaVariable[] boundVariables) throws IOException {
        target.write(new long[]{HEADER_LAMBDA},0,HEADER_LAMBDA_LENGTH);
        target.writeBigInt(BigInteger.valueOf(boundVariables.length-1),//offset by 1 because vars.length==0 is not possible
                LAMBDA_COUNT_INT_HEADER,LAMBDA_COUNT_INT_BLOCK, LAMBDA_COUNT_INT_BIG_BLOCK);
        for(int i=0;i<boundVariables.length;i++){
            BigInteger id=boundVariables[i].getId();
            for(int j=0;j<i;j++){//skip already existing ids
                if(id.compareTo(boundVariables[j].getId())>=0)
                    id=id.subtract(BigInteger.ONE);
            }
            target.writeBigInt(id,LAMBDA_VAR_INT_HEADER, LAMBDA_VAR_INT_BLOCK, LAMBDA_VAR_INT_BIG_BLOCK);
        }
    }
    public static void writeLambdaVariable(BitRandomAccessStream target, LambdaVariable var) throws IOException {
        target.write(new long[]{HEADER_LAMBDA_VARIABLE},0,HEADER_LAMBDA_VARIABLE_LENGTH);
        target.writeBigInt(var.getId(),LAMBDA_VAR_INT_HEADER, LAMBDA_VAR_INT_BLOCK, LAMBDA_VAR_INT_BIG_BLOCK);
    }

    //addLater compile/decompile directory (all files ending with .cnl / .cnla)

    //addLater? compile-time name definitions
    public static void compile(File sourceFile,File targetFile) throws IOException, SyntaxError, CNL_RuntimeException {
        if(targetFile.exists()) {//check if target is CNL code-File
            try (FileInputStream in = new FileInputStream(targetFile)) {
                int c=in.read();//addLater merge with readHeader code
                if(c!=-1){
                    if (c != 'C' || in.read() != 'N' || in.read() != 'L') {
                        throw new IOException("target-file is no CNL file");
                    }
                    c=in.read();
                    if(c=='A'){
                        throw new IOException("cannot overwrite CNL-assembly with compile data");
                    }else if(!(c=='L'||c=='X')){
                        throw new IOException("compile target-file is no CNL file");
                    }
                }
            }
        }else if(!targetFile.createNewFile()){
            throw new IOException("target-file does not exists");
        }
        try (Reader source=new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8)) {
            FileHeader header = readAssemblyFileHeader(source);
            if (header.type == FILE_TYPE_INVALID)
                throw new IOException("Invalid source-file, all cnl-assembly files have to start with CNLA<whitespace> or CNLA:<argCount>");
            try (BitRandomAccessStream target = new BitRandomAccessFile(targetFile, "rw")) {
                MathObject[] args;
                if (header.type == FILE_TYPE_ASSEMBLY) {
                    writeCodeHeader(target, new FileHeader(FILE_TYPE_CODE, CODE_VERSION, null));
                    args = new MathObject[0];
                } else if (header.type == FILE_TYPE_EXECUTABLE_ASSEMBLY) {
                    writeCodeHeader(target, new FileHeader(FILE_TYPE_EXECUTABLE, CODE_VERSION, header.argCount));
                    args = new MathObject[header.argCount.intValueExact()];
                    Arrays.fill(args, Real.Int.ZERO);
                } else {
                    throw new IOException("Invalid source-file, all cnl-assembly files have to start with CNLA<whitespace> or CNLA:<argCount>");
                }
                //position after end of header
                long startPos = target.bitPos();
                try (Interpreter test = new Interpreter(args, false, targetFile, false)) {
                    Action a;
                    long actions = 0;
                    do {
                        while (test.isImporting())
                            test.flatStep();//flatRun Imports
                        try {
                            a = nextAction(source, null, test.programEnvironment(), test.executionEnvironment(), test.isTopLayer());
                        } catch (IllegalArgumentException | UnsupportedOperationException| IndexOutOfBoundsException e) {
                            throw new SyntaxError(test, e);
                        } catch (ArithmeticException e) {
                            throw new CNL_RuntimeException(test, e);
                        }
                        test.stepInternal(a, false);//flat run code to detect syntax errors
                        if (a == EOF) {
                            Main.compileFinished(actions, target.bitPos() - startPos);
                            target.truncateToSize(true);
                            return;
                        } else {
                            a.writeTo(target);
                            actions++;
                        }
                    } while (true);
                }
            }
        }
    }

    public static void decompile(File sourceFile,File targetFile) throws IOException, SyntaxError, CNL_RuntimeException {
        if(targetFile.exists()) {//check if target is CNL code-assembly files
            try (FileInputStream in = new FileInputStream(targetFile)) {
                int c=in.read();//addLater merge with readHeader code
                if(c!=-1){
                    if (c != 'C' || in.read() != 'N' || in.read() != 'L') {
                        throw new IOException("decompile target-file is no CNL file");
                    }
                    c=in.read();
                    if(c=='L'||c=='X'){
                        throw new IOException("cannot overwrite CNL-code with decompiled data");
                    }else if(c!='A'){
                        throw new IOException("target-file is no CNL file");
                    }
                }
            }
        }else if(!targetFile.createNewFile()){
            throw new IOException("target-file does not exists");
        }
        try(BitRandomAccessStream source=new BitRandomAccessFile(sourceFile,"rw")) {
            try (Writer target = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8)) {
                FileHeader header = readCodeFileHeader(source);
                if (header.type == FILE_TYPE_INVALID)
                    throw new IOException("Invalid code-file, cnl code-files have to start with CNLL or CNLX");
                MathObject[] args;
                if (header.type == FILE_TYPE_CODE) {
                    writeAssemblyHeader(target, FILE_HEADER_ASSEMBLY);
                    args = new MathObject[0];
                } else if (header.type == FILE_TYPE_EXECUTABLE) {
                    writeAssemblyHeader(target, new FileHeader(FILE_TYPE_EXECUTABLE_ASSEMBLY, null, header.argCount));
                    args = new MathObject[header.argCount.intValueExact()];
                    Arrays.fill(args, Real.Int.ZERO);
                } else {
                    throw new IOException("Invalid code-file, cnl code-files have to start with CNLL or CNLX");
                }
                try (Interpreter test = new Interpreter(sourceFile, args, false)) {
                    Action a;
                    long actions = 0, lines = 0;
                    do {
                        while (test.isImporting())
                            test.flatStep();//flatRun Imports
                        try {
                            a = nextAction(source, test.programEnvironment(), test.executionEnvironment(), test.isTopLayer());
                        } catch (UnsupportedOperationException | IllegalArgumentException| IndexOutOfBoundsException e) {
                            throw new SyntaxError(test, e);
                        }
                        test.stepInternal(a, false);//flat run code to detect syntax errors
                        if (a == EOF) {
                            Main.decompileFinished(lines, actions);
                            target.close();
                            return;
                        } else {
                            target.write(a.stringRepresentation());
                            if (test.lineStart()) {
                                target.write("\n");
                                lines++;
                            } else {
                                target.write(" ");
                            }
                            actions++;
                        }
                    } while (true);
                }
            }
        }
    }

    public static final int FILE_TYPE_INVALID =-1, FILE_TYPE_CODE =0,
            FILE_TYPE_EXECUTABLE =1, FILE_TYPE_ASSEMBLY =2, FILE_TYPE_EXECUTABLE_ASSEMBLY =3;
    public static final FileHeader FILE_HEADER_INVALID =new FileHeader(FILE_TYPE_INVALID, null, null);
    public static final FileHeader FILE_HEADER_ASSEMBLY =new FileHeader(FILE_TYPE_ASSEMBLY, null, null);


    public static class FileHeader{
        public final int type;
        public final BigInteger codeVersion;
        public final BigInteger argCount;
        public FileHeader(int type, BigInteger codeVersion, BigInteger argCount) {
            this.type=type;
            this.codeVersion = codeVersion;
            this.argCount = argCount;
        }
    }


    private static void writeCodeHeader(BitRandomAccessStream target, FileHeader header) throws IOException {
        if(header.type==FILE_TYPE_EXECUTABLE||header.type==FILE_TYPE_CODE){
            long[] bits=new long[1];
            bits[0]='C'|'N'<<8|'L'<<16|(header.type==FILE_TYPE_EXECUTABLE?'X':'L')<<24;
            target.write(bits,0,32);
            target.writeBigInt(header.codeVersion, FILE_HEADER_INT_HEADER, FILE_HEADER_INT_BLOCK, FILE_HEADER_INT_BIG_BLOCK);
            if(header.type==FILE_TYPE_EXECUTABLE){
                target.writeBigInt(header.argCount, FILE_HEADER_INT_HEADER, FILE_HEADER_INT_BLOCK, FILE_HEADER_INT_BIG_BLOCK);
            }
        }else{
            throw new IllegalArgumentException("Illegal type for codeFile header only FILE_TYPE_CODE and FILE_TYPE_EXECUTABLE are allowed");
        }
    }
    private static void writeAssemblyHeader(Writer target, FileHeader header) throws IOException {
        if(header.type== FILE_TYPE_ASSEMBLY ||header.type== FILE_TYPE_EXECUTABLE_ASSEMBLY){
            target.write("CNLA");
            if(header.type== FILE_TYPE_EXECUTABLE_ASSEMBLY){
                target.write(":");
                target.write(header.argCount.toString(16));
                target.write('\n');
            }else{
                target.write('\n');
            }
        }else{
            throw new IllegalArgumentException("Illegal type for codeFile header only FILE_TYPE_CODE and FILE_TYPE_EXECUTABLE are allowed");
        }
    }
    public static  FileHeader readCodeFileHeader(BitRandomAccessStream file) throws IOException {
        long[] header=new long[1];
        file.readFully(header,0,32);// read 4 bytes
        long h=header[0];
        if((h&0xff)!='C')
            return FILE_HEADER_INVALID;
        h>>>=8;
        if((h&0xff)!='N')
            return FILE_HEADER_INVALID;
        h>>>=8;
        if((h&0xff)!='L')
            return FILE_HEADER_INVALID;
        h>>>=8;
        if((h&0xff)=='L'||(h&0xff)=='X'){
            BigInteger codeVersion=file.readBigInt(FILE_HEADER_INT_HEADER, FILE_HEADER_INT_BLOCK, FILE_HEADER_INT_BIG_BLOCK);
            if((h&0xff)=='L')
                return new FileHeader(FILE_TYPE_CODE,codeVersion,null);
            if((h&0xff)=='X')
                return new FileHeader(FILE_TYPE_EXECUTABLE, codeVersion,
                        file.readBigInt(FILE_HEADER_INT_HEADER, FILE_HEADER_INT_BLOCK, FILE_HEADER_INT_BIG_BLOCK));
        }
        if((h&0xff)=='A'){//CNL-Assembly
            return finishAssemblyHeader(file.reader());
        }
        return FILE_HEADER_INVALID;
    }
    public static FileHeader readAssemblyFileHeader(Reader reader) throws IOException {
        if(reader.read()!='C')
            return FILE_HEADER_INVALID;
        if(reader.read()!='N')
            return FILE_HEADER_INVALID;
        if(reader.read()!='L')
            return FILE_HEADER_INVALID;
        if(reader.read()!='A')
            return FILE_HEADER_INVALID;
        return finishAssemblyHeader(reader);
    }

    /**@return the completed FileHeader (non-null)*/
    private static FileHeader finishAssemblyHeader(Reader reader) throws IOException {
        int next= reader.read();
        if(next==':'){
            StringBuilder arg=new StringBuilder();
            do{
                next= reader.read();
                if(next==-1||Character.isWhitespace(next)){
                    break;
                }else{
                    arg.append((char)next);
                }
            }while (true);
            try{
                return new FileHeader(FILE_TYPE_EXECUTABLE_ASSEMBLY, null, new BigInteger(arg.toString(),16));
            }catch (IllegalArgumentException iae){
                return FILE_HEADER_INVALID;
            }
        }else if(Character.isWhitespace(next)){
            return FILE_HEADER_ASSEMBLY;
        }else{
            return FILE_HEADER_INVALID;
        }
    }


}
