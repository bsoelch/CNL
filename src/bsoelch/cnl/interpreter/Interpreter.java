package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessFile;
import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Main;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;


import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static bsoelch.cnl.Constants.*;

public class Interpreter implements Closeable {
    /**All Actions in Stack are Operators*/
    final static int FLAG_OPERATOR_CHAIN =1;
    /**All Actions in Stack are Operators with one Argument*/
    final static int FLAG_SINGLE_ARG_CHAIN_CHAIN =2;
    /**All Actions in Stack are Var declarations*/
    final static int FLAG_VAR_DECLARATION_CHAIN =4;
    final static int FLAG_ROOT =-1;



    static class CodePosition{
        final BitRandomAccessStream file;
        final boolean isScript;
        final long position;

        CodePosition(BitRandomAccessStream file, boolean isScript) {
            this.file = file;
            this.position = file==null?-1:file.bitPos();
            this.isScript = isScript;
        }

        /**checks if this CodePosition points to the same position as other<br>
         * !!! this method return false if one of the COdePositions is invalid,
         * even if they point to the same object!!!
         * @return true iff this and other point to the same <b>valid</b> CodePosition*/
        public boolean isEqual(CodePosition other) {
            if(file==null||position==-1||other.position==-1||other.file==null)
                return false;//NOP!=NOP
            else {
                return position == other.position &&
                        Objects.equals(file.getSourceId(), other.file.getSourceId());
            }
        }
    }
    static class ImportReturnPosition{
        final CodePosition codePos;
        final File codeFile;
        final Context prevEnv;

        ImportReturnPosition(File codeFile, CodePosition codePos, Context prevEnv) {
            this.codePos = codePos;
            this.codeFile = codeFile;
            this.prevEnv = prevEnv;
        }
    }

    private final ExecutionEnvironment exEnv;
    private File codeDir;
    private BitRandomAccessStream code;
    private boolean isScript;

    final private Context root;

    private final ArrayDeque<Context> envStack=new ArrayDeque<>();

    /**stack containing all open BracketEnvironments, including running loops/if's and function calls*/
    final private ArrayDeque<BracketInfo> brackets=new ArrayDeque<>();
    /**stack containing all current function calls*/
    final private ArrayDeque<FunctionInfo> callStack=new ArrayDeque<>();
    /**stack for the return positions of all open imports*/
    private final ArrayDeque<ImportReturnPosition> importReturnStack=new ArrayDeque<>();

    private final HashSet<String> importDejaVu=new HashSet<>();
    private final HashSet<Context> importEnvironments=new HashSet<>();

    private ArrayDeque<Action> actionStack=new ArrayDeque<>();
    private StringBuilder currentLine=new StringBuilder();
    private String prevLine;

    /**@param codeFile sourceFile for the code
     * @param args program arguments, may be null
     * @param forceRunLibs if true the program will run libraries as executables without arguments*/
    public Interpreter(File codeFile, MathObject[] args, boolean forceRunLibs) throws IOException {
        this(args, true, codeFile, forceRunLibs);
    }
    /**Internal constructor that allows the creation of Interpreter instances which are
     * not attached to a BitRandomAccessFile
     * @param args program arguments, may be null
     * @param useCode if false the code in codeFile is not ignored,
     *                the codeFile is still used to find the local directory
     * @param codeFile sourceFile for the code, is assumed to be non-null
     * @param forceRunLibs if true the program will run libraries as executables without arguments
     * */
    Interpreter(MathObject[] args, boolean useCode, File codeFile, boolean forceRunLibs) throws IOException {
        this.codeDir = codeFile.getParentFile();
        if(useCode){
            this.code =new BitRandomAccessFile(codeFile,"r");
            Translator.FileHeader header=Translator.readCodeFileHeader(code);
            if(header.type==Translator.FILE_TYPE_INVALID)
                throw new IOException(codeFile+" is no valid code-file");
            if((header.type&Translator.FILE_TYPE_ASSEMBLY)!=0)
                isScript=true;
            if((header.type&Translator.FILE_TYPE_EXECUTABLE)!=0){
                int c = header.codeVersion.compareTo(CODE_VERSION);
                if(c<0){
                    throw new IOException(codeFile+" uses an older Version of the binary Encoding");
                }else if(c>0){
                    throw new IOException(codeFile+" uses a newer Version of the binary Encoding");
                }
                if(args==null){
                    args= Main.getArgs(header.argCount.intValueExact());
                }else if(args.length!=header.argCount.intValueExact())
                    throw new IOException("wrong number of Arguments: "+args.length+" expected: "+header.argCount);
            }else if(!forceRunLibs){
                throw new IOException("Unable to run library Files");
            }else{
                args=new MathObject[0];
            }
        }
        root=new RootContext(new Context.ArgumentData(args));
        envStack.add(root);
        importDejaVu.add(codeFile.getPath());
        exEnv =new ExecutionEnvironment(codeDir);
    }

    Context programEnvironment() {
        return envStack.getLast();
    }

    ExecutionEnvironment executionEnvironment() {
        return exEnv;
    }

    boolean isTopLayer() {
        return brackets.isEmpty();
    }

    boolean lineStart() {
        return actionStack.isEmpty();
    }

    public boolean isImporting() {
        return importReturnStack.size()>0;
    }

    Iterator<String> lines(){
        return new Iterator<String>() {
            final Iterator<FunctionInfo> itr=callStack.descendingIterator();
            boolean first=true;
            @Override
            public boolean hasNext() {
                return first||itr.hasNext();
            }

            @Override
            public String next() {
                if(first){
                    first=false;
                    return currentLine.length()==0?prevLine:currentLine.toString();
                }else{
                    return itr.next().currentLine;
                }
            }
        };
    }

    public MathObject run() throws IOException, SyntaxError, CNL_RuntimeException {
        return run(true);
    }
    public void test() throws IOException, SyntaxError, CNL_RuntimeException {
        run(false);
    }

    MathObject run(boolean doBranching) throws IOException, SyntaxError, CNL_RuntimeException {
        long count=0;
        while (doStep(doBranching)) {
            count++;
        }
        Main.executeFinished(count, doBranching);
        return envStack.getLast().getRes();
    }

    void flatStep() throws IOException, SyntaxError, CNL_RuntimeException {
        doStep(false);
    }

    private boolean doStep(boolean doBranching) throws IOException, SyntaxError, CNL_RuntimeException {
        Action a;
        if(isScript){
            a= Translator.nextAction(code.reader(),code, programEnvironment(), executionEnvironment(), isTopLayer());
        }else{
            a= Translator.nextAction(code, programEnvironment(), executionEnvironment(), isTopLayer());
        }
        try {
            return stepInternal(a, doBranching);
        }catch (IllegalArgumentException|IllegalStateException| IndexOutOfBoundsException e){
            throw new SyntaxError(this,e);
        }catch (ArithmeticException e){
            throw new CNL_RuntimeException(this,e);
        }
    }

    boolean stepInternal(Action a,boolean doBranching) throws IOException, SyntaxError {
        if(actionStack.isEmpty()) {//clear line
            prevLine = currentLine.toString();
            currentLine.setLength(0);
        }
        currentLine.append(a.stringRepresentation()).append(' ');
        if(a == Translator.EOF|| a == Translator.EXIT){
            return exit(doBranching);
        }else if(a instanceof RunIn){//Unwrap RunIn
            a =((RunIn) a).childId==null?brackets.isEmpty()?root:brackets.getLast().getLocalRoot():envStack.getLast().getChild(((RunIn) a).childId);
            envStack.add((Context) a);
        }else if(a instanceof BracketDeclaration){//bracket operations
            ensureRoot();//brackets only on rootLevel
            switch (((BracketDeclaration) a).type) {
                case BRACKET_FLAG_ELIF_EQ:
                case BRACKET_FLAG_ELIF_NE:
                case BRACKET_FLAG_ELSE: {//else reached in step -> end of if-branch
                        BracketInfo top = brackets.peekLast();
                        if (top instanceof LoopInfo) {
                            switch (((LoopInfo) top).state) {
                                case BRACKET_FLAG_IF_EQ:
                                case BRACKET_FLAG_IF_NE:
                                case BRACKET_FLAG_ELIF_EQ:
                                case BRACKET_FLAG_ELIF_NE: {
                                    ((LoopInfo) top).state = ((BracketDeclaration) a).type;
                                    if(doBranching) {
                                        skipTo(SKIP_END_IF, 1);
                                        removeBracket();
                                        return true;
                                    }else if(((BracketDeclaration) a).type==BRACKET_FLAG_ELSE){
                                        ((LoopInfo) top).state=BRACKET_FLAG_ELSE;
                                        return true;
                                    }else{
                                        break;//elif will be added to stack
                                    }
                                }
                                default:
                                    throw new SyntaxError(this,"Unexpected ELSE statement");
                            }
                        } else {
                            throw new SyntaxError(this,"Unexpected ELSE statement");
                        }
                    }
                    break;
                case BRACKET_FLAG_END: {
                        BracketInfo top = brackets.peekLast();
                        if (top instanceof LoopInfo) {
                            if(doBranching) {
                                switch (((LoopInfo) top).state) {
                                    case BRACKET_FLAG_WHILE_EQ:
                                    case BRACKET_FLAG_WHILE_NE: //jump to start of loop
                                        goTo(top.bracketStart);
                                        break;
                                    default:
                                        removeBracket();
                                }
                            }else{
                                removeBracket();
                            }
                        } else if (top instanceof FunctionInfo) {//exit Function
                            exitFunction(callStack.removeLast(),doBranching);
                        } else {
                            if(top==null){
                                throw new SyntaxError(this, "Unexpected End of loop");
                            }else{
                                throw new RuntimeException("Unexpected type of BracketEnvironment:"+top.getClass());
                            }
                        }
                    }
                    return true;
                case BRACKET_FLAG_BREAK: {
                        BracketInfo top;
                        ArrayDeque<BracketInfo> reconstruct=new ArrayDeque<>(brackets.size());
                        int count=0;
                        do {//calculate number of open non breakable brackets
                            if(brackets.isEmpty())
                                throw new SyntaxError(this,"BREAK statement outside of loop");
                            top = brackets.removeLast();//get next bracket
                            reconstruct.addFirst(top);//addFirst to keep order of brackets
                            count++;
                        }while (!(top instanceof LoopInfo &&((((LoopInfo) top).state==BRACKET_FLAG_WHILE_EQ)
                                ||(((LoopInfo) top).state==BRACKET_FLAG_WHILE_NE)||(((LoopInfo) top).state==BRACKET_FLAG_DO))));
                        if(doBranching){
                            brackets.add(top);//re-add loop-environment that is ended by break
                            skipTo(SKIP_LOOP,count);
                        }else{//don't break in flat mode
                            brackets.addAll(reconstruct);//reset state of brackets
                        }
                    }
                    return true;
                case BRACKET_FLAG_DO:
                    addBracket(new LoopInfo(((BracketDeclaration) a).declarationEnvironment
                            ,((BracketDeclaration) a).declarationStart, BRACKET_FLAG_DO));
                    return true;
            }
        }
        addToStack(a,doBranching);
        return true;
    }

    private final static int SKIP_LOOP=1,SKIP_IF=2,SKIP_END_IF=3,SKIP_FUNCTION=5;

    /**Skips to the next element in the structure given by {@code type}*/
    private void skipTo(int stepType, int openBrackets) throws IOException, SyntaxError {
        int bracketCount=openBrackets;
        Action a;
        while (bracketCount>0){
            if(isScript){
                a= Translator.nextAction(code.reader(),code,envStack.getLast(), exEnv,brackets.isEmpty());
            }else{
                a= Translator.nextAction(code,envStack.getLast(), exEnv,brackets.isEmpty());
            }
            if(a== Translator.EOF){//ignore EXIT statements
                throw new SyntaxError(this,"Unfinished Loop");
            }else if(a instanceof FunctionDeclaration){
                bracketCount++;
            }else if(a instanceof BracketDeclaration){
                switch (((BracketDeclaration) a).type){
                    case BRACKET_FLAG_WHILE_EQ:
                    case BRACKET_FLAG_WHILE_NE:
                    case BRACKET_FLAG_DO:
                    case BRACKET_FLAG_IF_EQ:
                    case BRACKET_FLAG_IF_NE:bracketCount++;break;
                    case BRACKET_FLAG_ELIF_EQ:
                    case BRACKET_FLAG_ELIF_NE:
                    case BRACKET_FLAG_ELSE:if(bracketCount==1){
                        if(stepType==SKIP_IF){
                            evaluateStack(true,true);
                            if(((BracketDeclaration) a).type!= BRACKET_FLAG_ELSE){
                                BracketInfo bracket = brackets.peekLast();
                                if(!(bracket instanceof LoopInfo))
                                    throw new SyntaxError(this,"Unexpected ELSE-statement");
                                switch (((LoopInfo) bracket).state){
                                    case BRACKET_FLAG_IF_EQ:
                                    case BRACKET_FLAG_IF_NE:
                                    case BRACKET_FLAG_ELIF_EQ:
                                    case BRACKET_FLAG_ELIF_NE:break;
                                    default:
                                        throw new SyntaxError(this,"Unexpected ELSE-statement");
                                }//inherit environment from parent
                                addToStack(a, true);
                            }
                            return;
                        }else if(stepType!=SKIP_END_IF){
                            throw new SyntaxError(this,"Unexpected ELSE-statement");
                        }
                    }break;
                    case BRACKET_FLAG_END_WHILE_EQ:
                    case BRACKET_FLAG_END_WHILE_NE:{
                        bracketCount--;
                        if(bracketCount==0&&stepType==SKIP_LOOP){
                            removeBracket();
                        }else if(bracketCount<=0)//do while should not be end of a skip statement
                            throw new SyntaxError(this,"Unexpected end of DO-WHILE");
                    }break;
                    case BRACKET_FLAG_END:{
                        bracketCount--;
                        if(bracketCount<0)
                            throw new SyntaxError(this,"Unexpected end of bracket");
                        if(bracketCount==0) {
                            switch (stepType){
                                case SKIP_LOOP:
                                case SKIP_IF:{//unexpected end of if
                                    removeBracket();
                                }//fallthrough
                                case SKIP_END_IF:
                                case SKIP_FUNCTION:
                                    break;
                            }
                        }
                    }break;
                }
            }
        }
    }

    private void exitFunction(FunctionInfo function, boolean doBranching) throws IOException, SyntaxError {
        BracketInfo env = removeBracket();
        while (env != function) {//close all brackets
            if(brackets.isEmpty())
                throw new RuntimeException("brackets and callStack out of sync");
            env = removeBracket();
        }
        actionStack= function.prevStack;//exit function
        currentLine = new StringBuilder(function.currentLine);
        if(doBranching) {
            goTo(function.bracketStart);
        }
        addToStack(Translator.wrap(function.getRes()), doBranching);
    }


    private boolean exit(boolean doBranching) throws IOException, SyntaxError {
        evaluateStack(true,doBranching);
        if(callStack.size()>0){
            if(doBranching) {//exit functions on exit only when in branching mode
                exitFunction(callStack.removeLast(), true);
            }
            return true;
        }else if(importReturnStack.size()>0){
            importDejaVu.remove(code.getSourceId());//remove from dejaVu stack after closing
            ImportReturnPosition ret=importReturnStack.removeLast();
            codeDir =ret.codeFile;
            if(ret.prevEnv!=envStack.removeLast())
                throw new RuntimeException("envStack out of sync with Imports");
            goTo(ret.codePos);
            return true;
        }else{
            try{
                close();
            }catch (IOException io){
                System.err.println("\nException while closing codeFile:");
                io.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        if(code!=null) {
            code.close();
        }
    }

    /**go To the given CodePosition*/
    private void goTo(CodePosition newPos) throws IOException {
        code = newPos.file;
        isScript=newPos.isScript;
        code.seek(newPos.position);
    }


    private void addToStack(Action action, boolean doBranching) throws IOException, SyntaxError {
        actionStack.add(action);
        evaluateStack(false,doBranching);//simplify after adding
    }

    private void evaluateStack(boolean forceEvaluate,boolean doBranching) throws IOException, SyntaxError {
        Action last;
        ValuePointer value;
        while (actionStack.size()>0){
            last=actionStack.getLast();
            int flags = stackFlags();
            if(!forceEvaluate&&last.acceptsArg(flags))
                return;//stack can accept further arguments
            if(last.requiresArg())
                throw new SyntaxError(this,"Missing Argument");
            actionStack.removeLast();
            if(last instanceof ValuePointer){
                if(last instanceof ArgPointer&&callStack.isEmpty()&&importReturnStack.size()>0){
                    throw new SyntaxError(this,"Cannot use Program Arguments in Imported files");
                }
                value=(ValuePointer) last;
            }else if(last instanceof Operator){
                if(doBranching) {
                    value = ((Operator) last).preformOperation(flags);
                }else{
                    value= Translator.ZERO;
                }
            }else if(last instanceof BindLambda){
                if(doBranching) {
                    value = ((BindLambda) last).preformOperation();
                }else{
                    value= Translator.ZERO;
                }
            }else if(last instanceof Input){
                if(doBranching) {
                    value=((Input) last).read();
                }else{
                    value= Translator.ZERO;//DefaultValue
                }
            }else if(last instanceof Output){
                if(doBranching) {
                    value=((Output) last).write();
                }else{
                    value= Translator.ZERO;//DefaultValue
                }
            }else if(last instanceof CallFunction){
                if(forceEvaluate)
                    throw new SyntaxError(this,"Waiting for Function call");
                FunctionInfo call = new FunctionInfo(((CallFunction) last).getDeclarationEnvironment(),
                        new CodePosition(code, isScript), actionStack,new Context.ArgumentData(((CallFunction) last).getArgs()), currentLine.toString());
                if(doBranching) {
                    addBracket(call);//add function call to brackets to allow easy management of open structures
                    callStack.addLast(call);
                    actionStack = new ArrayDeque<>();
                    currentLine=new StringBuilder();
                    goTo(((CallFunction) last).getFunctionStart());
                    return;
                }else{
                    value= Translator.ZERO;//DefaultValue
                }
            }else if(last instanceof BracketDeclaration){
                switch (((BracketDeclaration) last).type){
                    case BRACKET_FLAG_IF_EQ:
                    case BRACKET_FLAG_ELIF_EQ:
                        if(((BracketDeclaration) last).type ==BRACKET_FLAG_IF_EQ) {
                            addBracket(new LoopInfo(((BracketDeclaration) last).declarationEnvironment,
                                    new CodePosition(code, isScript),BRACKET_FLAG_IF_EQ));
                        }else{//update state
                            updateIfState(BRACKET_FLAG_ELIF_EQ);
                        }
                        if(doBranching&&(!((BracketDeclaration) last).param.equals(Real.Int.ZERO))){
                            ensureRoot();
                            skipTo(SKIP_IF, 1);
                            return;
                        }break;
                    case BRACKET_FLAG_IF_NE:
                    case BRACKET_FLAG_ELIF_NE:
                        if(((BracketDeclaration) last).type ==BRACKET_FLAG_IF_NE) {
                            addBracket(new LoopInfo(((BracketDeclaration) last).declarationEnvironment,
                                    new CodePosition(code, isScript),BRACKET_FLAG_IF_NE));
                        }else{//update state
                            updateIfState(BRACKET_FLAG_ELIF_NE);
                        }
                        if(doBranching&&((BracketDeclaration) last).param.equals(Real.Int.ZERO)){
                            ensureRoot();
                            skipTo(SKIP_IF, 1);
                            return;
                        }break;
                    case BRACKET_FLAG_WHILE_EQ:
                        //ensure there is exactly one while-loop bracket
                        if(brackets.size()==0||!(brackets.peekLast().bracketStart
                                .isEqual(((BracketDeclaration) last).declarationStart))){
                            addBracket(new LoopInfo(((BracketDeclaration) last).declarationEnvironment,
                                    ((BracketDeclaration) last).declarationStart, BRACKET_FLAG_WHILE_EQ));
                        }
                        if ((doBranching) && !((BracketDeclaration) last).param.equals(Real.Int.ZERO)) {
                            skipTo(SKIP_LOOP, 1);
                            return;
                        }
                        break;
                    case BRACKET_FLAG_WHILE_NE:
                        //ensure there is exactly one while-loop bracket
                        if(brackets.size()==0||!(brackets.peekLast().bracketStart
                                .isEqual(((BracketDeclaration) last).declarationStart))){
                            addBracket(new LoopInfo(((BracketDeclaration) last).declarationEnvironment,
                                    ((BracketDeclaration) last).declarationStart, BRACKET_FLAG_WHILE_NE));
                        }
                        if ((doBranching) && ((BracketDeclaration) last).param.equals(Real.Int.ZERO)) {
                            skipTo(SKIP_LOOP, 1);
                            return;
                        }
                        break;
                    case BRACKET_FLAG_END_WHILE_EQ:
                        if(doBranching&&((BracketDeclaration) last).param.equals(Real.Int.ZERO)){
                            BracketInfo loop=brackets.peekLast();
                            if(!(loop instanceof LoopInfo)||((LoopInfo)loop).state!= BRACKET_FLAG_DO)
                                throw new SyntaxError(this,"Unexpected end of DO-WHILE loop");
                            goTo(loop.bracketStart);
                        }else{
                            BracketInfo closed= removeBracket();
                            if(!(closed instanceof LoopInfo)||((LoopInfo)closed).state!= BRACKET_FLAG_DO)
                                throw new SyntaxError(this,"Unexpected end of DO-WHILE loop");
                        }break;
                    case BRACKET_FLAG_END_WHILE_NE:
                        if(doBranching&&!((BracketDeclaration) last).param.equals(Real.Int.ZERO)){
                            BracketInfo loop=brackets.peekLast();
                            if(!(loop instanceof LoopInfo)||((LoopInfo)loop).state!= BRACKET_FLAG_DO)
                                throw new SyntaxError(this,"Unexpected end of DO-WHILE loop");
                            goTo(loop.bracketStart);
                        }else{
                            BracketInfo closed= Interpreter.this.removeBracket();
                            if(!(closed instanceof LoopInfo)||((LoopInfo)closed).state!= BRACKET_FLAG_DO)
                                throw new SyntaxError(this,"Unexpected end of DO-WHILE loop");
                        }break;
                    default:throw new SyntaxError(this,"Unexpected state of Bracket");
                }
                ensureRoot();
                return;
            }else if(last instanceof FunctionDeclaration){
                envStack.getLast().putFunction(((FunctionDeclaration) last).fktId
                        ,((FunctionDeclaration) last).function);
                ensureRoot();
                if(doBranching) {
                    skipTo(SKIP_FUNCTION, 1);
                }else{
                    ValuePointer[] args=new ValuePointer[((FunctionDeclaration) last).function.argCount];
                    Arrays.fill(args,Translator.ZERO);//Default Value
                    FunctionInfo call = new FunctionInfo(((FunctionDeclaration) last).function.declarationEnvironment,
                            new CodePosition(code, isScript), actionStack,new Context.ArgumentData(args), currentLine.toString());
                    addBracket(call);//add function call to brackets to allow easy management of open structures
                    callStack.addLast(call);
                    actionStack = new ArrayDeque<>();
                    currentLine=new StringBuilder();
                }
                return;
            }else if(last instanceof Import){
                importReturnStack.addLast(new ImportReturnPosition(codeDir, new CodePosition(code, isScript),((Import) last).getTarget()));
                if(!importEnvironments.add(((Import) last).getTarget()))
                    throw new SyntaxError(this,"Multiple Imports in the same Environment");
                envStack.addLast(((Import) last).getTarget());
                File target=new File(codeDir.getAbsolutePath()+File.separator+((Import) last).getSource());
                codeDir =target.getParentFile();
                code =new BitRandomAccessFile(target,"r");
                Translator.FileHeader header=Translator.readCodeFileHeader(code);
                if(header.type==Translator.FILE_TYPE_INVALID){
                    throw new SyntaxError(this,"Invalid import-File: "+target.getAbsolutePath());
                }else if((header.type&Translator.FILE_TYPE_EXECUTABLE)!=0){
                    throw new SyntaxError(this,"Invalid import-File: "+target.getAbsolutePath()+" cannot import Executables");
                }else{
                    if(header.type==Translator.FILE_TYPE_CODE) {
                        int c = header.codeVersion.compareTo(CODE_VERSION);
                        if (c < 0) {
                            throw new IOException("Invalid import-File: "+target.getAbsolutePath()
                                    + " uses an older Version of the binary Encoding");
                        } else if (c > 0) {
                            throw new IOException("Invalid import-File: "+target.getAbsolutePath()
                                    + " uses a newer Version of the binary Encoding");
                        }
                    }
                    isScript=((header.type&Translator.FILE_TYPE_ASSEMBLY)!=0);
                }
                if(!importDejaVu.add(code.getSourceId())) {//check if codeFile is already imported in this direct hierarchy
                    throw new SyntaxError(this,"Cyclic Import in: "+importDejaVu);
                }
                ensureRoot();
                return;
            }else if(last instanceof Context){
                throw new SyntaxError(this,"Unfinished Expression");
            }else{
                throw new RuntimeException("Unexpected type of Action:"+last.getClass());
            }
            //close open Environments
            while (actionStack.size()>0&&actionStack.getLast() instanceof Context){
                actionStack.removeLast();
                envStack.removeLast();
            }
            if(actionStack.size()>0){
                //pass value to next action
                actionStack.getLast().pushArg(value);
            }else{
                //handle return values
                envStack.getLast().setRes(value.getValue());
            }
        }
    }

    /**@return removed Bracket (non-null)
     * @throws RuntimeException if there is no bracket to remove (this should never happen)*/
    private BracketInfo removeBracket() {
        Context env=envStack.removeLast();//remove Bracket Root
        BracketInfo bracket = brackets.removeLast();
        if(env!=bracket.getLocalRoot())
            throw new RuntimeException("envStack out of sync with bracketStack");
        return bracket;
    }

    private void addBracket(BracketInfo env) {
        envStack.add(env.getLocalRoot());//add Bracket Root
        brackets.addLast(env);
    }

    private void ensureRoot() throws SyntaxError {
        while (actionStack.size()>0){
            if(actionStack.removeLast() instanceof Context){
                envStack.removeLast();//synchronize envStack and actionStack
            }else{
                throw new SyntaxError(this,"Bracket declaration on non-root Level");
            }
        }
    }

    private void updateIfState(int newState) throws SyntaxError {
        BracketInfo env = brackets.peekLast();
        if (env instanceof LoopInfo) {
            switch (((LoopInfo) env).state) {
                case BRACKET_FLAG_IF_EQ:
                case BRACKET_FLAG_IF_NE:
                case BRACKET_FLAG_ELIF_EQ:
                case BRACKET_FLAG_ELIF_NE:
                    ((LoopInfo) env).state = newState;
                    break;
                default:
                    throw new SyntaxError(this,"unexpected elif-statement");
            }
        } else {
            throw new SyntaxError(this,"unexpected elif-statement");
        }
    }

    private int stackFlags() {
        int flags= FLAG_ROOT,prev= FLAG_ROOT;//addLater? better flag-management
        for(Action a:actionStack){//iterate through elements of actionStack
            prev=flags;
            if (!(a instanceof Context)) {//No change for program environments
                if(a instanceof VarPointer){
                    flags&=FLAG_VAR_DECLARATION_CHAIN;
                    if(flags==0)
                        return 0;
                }else if(a instanceof Operator){
                    if(((Operator) a).args.length==1){
                        flags&=(((Operator) a).operatorInfo.storeMode==Operators.MODIFY_ARG0_NEVER) ? 0:
                                FLAG_SINGLE_ARG_CHAIN_CHAIN|FLAG_OPERATOR_CHAIN;
                    }else{//root-flag only in first multi-arg operator
                        flags&=(((Operator) a).operatorInfo.storeMode==Operators.MODIFY_ARG0_NEVER) ? 0:
                                ((flags&FLAG_SINGLE_ARG_CHAIN_CHAIN)!=0)?FLAG_OPERATOR_CHAIN: 0;
                    }
                    if(flags==0)
                        return 0;
                }else{
                    return 0;
                }
            }
        }
        return prev;//ignore last element
    }

}
