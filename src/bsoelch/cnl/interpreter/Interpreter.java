package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessFile;
import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Main;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static bsoelch.cnl.Constants.*;

public class Interpreter implements Closeable {
    /**All Actions in Stack are Operators*/
    final static int FLAG_OPERATOR_CHAIN =1;
    /**All Actions in Stack only need one Argument*/
    final static int FLAG_SINGLE_ARG_CHAIN =2;
    /**All Actions in Stack are Var declarations*/
    final static int FLAG_VAR_DECLARATION_CHAIN =4|FLAG_SINGLE_ARG_CHAIN;
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
        final ProgramEnvironment prevEnv;

        ImportReturnPosition(File codeFile, CodePosition codePos, ProgramEnvironment prevEnv) {
            this.codePos = codePos;
            this.codeFile = codeFile;
            this.prevEnv = prevEnv;
        }
    }

    private final ExecutionEnvironment exEnv;
    private File codeDir;
    private BitRandomAccessStream code;
    private boolean isScript;

    final private ProgramEnvironment root;

    private final ArrayDeque<ProgramEnvironment> envStack=new ArrayDeque<>();

    /**stack containing all open BracketEnvironments, including running loops/if's and function calls*/
    final private ArrayDeque<BracketEnvironment> brackets=new ArrayDeque<>();
    /**stack containing all current function calls*/
    final private ArrayDeque<FunctionEnvironment> callStack=new ArrayDeque<>();
    /**stack for the return positions of all open imports*/
    private final ArrayDeque<ImportReturnPosition> importReturnStack=new ArrayDeque<>();

    private final HashSet<String> importDejaVu=new HashSet<>();
    private final HashSet<ProgramEnvironment> importEnvironments=new HashSet<>();

    private ArrayDeque<Action> actionStack=new ArrayDeque<>();
    private StringBuilder currentLine=new StringBuilder();

    public Interpreter(File codeFile, @Nullable MathObject[] args, boolean forceRunLibs) throws IOException {
        this(args, true, codeFile, forceRunLibs);
    }
    /**Internal constructor that allows the creation of Interpreter instances which are not attached to a BitRandomAccessFile*/
    Interpreter(@Nullable MathObject[] args, boolean useCode, File codeFile, boolean forceRunLib) throws IOException {
        this.codeDir = codeFile.getParentFile();
        if(useCode){
            this.code =new BitRandomAccessFile(codeFile,"r");
            Translator.FileHeader header=Translator.readCodeFileHeader(code);
            if(header.type==Translator.FILE_TYPE_INVALID)
                throw new IOException(codeFile+" is no valid code-file");
            if((header.type&Translator.FILE_TYPE_SCRIPT)!=0)
                isScript=true;
            if((header.type&Translator.FILE_TYPE_EXECUTABLE)!=0){
                if(args==null){
                    args= Main.getArgs(header.argCount.intValueExact());
                }else if(args.length!=header.argCount.intValueExact())
                    throw new IOException("wrong number of Arguments: "+args.length+" expected: "+header.argCount);
            }else if(!forceRunLib){
                throw new IOException("Unable to run library Files");
            }else{
                args=new MathObject[0];
            }
        }
        root=new GlobalEnvironment(new ProgramEnvironment.ArgumentData(args));
        envStack.add(root);
        importDejaVu.add(codeFile.getPath());
        exEnv =new ExecutionEnvironment(codeDir);
    }

    ProgramEnvironment programEnvironment() {
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
            final Iterator<FunctionEnvironment> itr=callStack.iterator();
            boolean iterating=true;
            @Override
            public boolean hasNext() {
                return iterating;
            }

            @Override
            public String next() {
                if(itr.hasNext()){
                    return itr.next().currentLine;
                }else{
                    iterating=false;
                    return currentLine.toString();
                }
            }
        };
    }

    public MathObject run() throws IOException, SyntaxError {
        return run(true);
    }
    public void test() throws IOException, SyntaxError {
        run(false);
    }

    MathObject run(boolean doBranching) throws IOException, SyntaxError {
        long count=0;
        while (doStep(doBranching)) {
            count++;
        }
        Main.executeFinished(count, doBranching);
        return envStack.getLast().getRes();
    }

    void flatStep() throws IOException, SyntaxError {
        doStep(false);
    }

    private boolean doStep(boolean doBranching) throws IOException, SyntaxError {
        if(actionStack.isEmpty())//clear line
            currentLine.setLength(0);
        Action a;
        if(isScript){
            a= Translator.nextAction(code.reader(),code, programEnvironment(), executionEnvironment(), isTopLayer());
        }else{
            a= Translator.nextAction(code, programEnvironment(), executionEnvironment(), isTopLayer());
        }
        try {
            return stepInternal(a, doBranching);
        }catch (IllegalArgumentException|IllegalStateException e){
            throw new SyntaxError(this,e);
        }
    }

    boolean stepInternal(Action a,boolean doBranching) throws IOException, SyntaxError {
        if(a == Translator.EOF|| a == Translator.EXIT){
            return exit(doBranching);
        }else if(a instanceof RunIn){//Unwrap RunIn
            a =((RunIn) a).childId==null?brackets.isEmpty()?root:brackets.getLast().getLocalRoot():envStack.getLast().getChild(((RunIn) a).childId);
            envStack.add((ProgramEnvironment) a);
        }else if(a instanceof BracketDeclaration){//bracket operations
            ensureRoot();//brackets only on rootLevel
            switch (((BracketDeclaration) a).type) {
                case BRACKET_FLAG_ELIF_EQ:
                case BRACKET_FLAG_ELIF_NE:
                case BRACKET_FLAG_ELSE: {//else reached in step -> end of if-branch
                        BracketEnvironment top = brackets.peekLast();
                        if (top instanceof LoopEnvironment) {
                            switch (((LoopEnvironment) top).state) {
                                case BRACKET_FLAG_IF_EQ:
                                case BRACKET_FLAG_IF_NE:
                                case BRACKET_FLAG_ELIF_EQ:
                                case BRACKET_FLAG_ELIF_NE: {
                                    ((LoopEnvironment) top).state = BRACKET_FLAG_ELSE;
                                    if(doBranching) {
                                        skipTo(SKIP_END_IF, 1);
                                        removeBracket();
                                        return true;
                                    }else if(((BracketDeclaration) a).type==BRACKET_FLAG_ELSE){
                                        ((LoopEnvironment) top).state=BRACKET_FLAG_ELSE;
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
                        BracketEnvironment top = brackets.peekLast();
                        if (top instanceof LoopEnvironment) {
                            if(doBranching) {
                                switch (((LoopEnvironment) top).state) {
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
                        } else if (top instanceof FunctionEnvironment) {//exit Function
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
                        BracketEnvironment top;
                        ArrayDeque<BracketEnvironment> reconstruct=new ArrayDeque<>(brackets.size());
                        int count=0;
                        do {//calculate number of open non breakable brackets
                            if(brackets.isEmpty())
                                throw new SyntaxError(this,"BREAK statement outside of loop");
                            top = brackets.removeLast();//get next bracket
                            reconstruct.addFirst(top);//addFirst to keep order of brackets
                            count++;
                        }while (!(top instanceof LoopEnvironment&&((((LoopEnvironment) top).state==BRACKET_FLAG_WHILE_EQ)
                                ||(((LoopEnvironment) top).state==BRACKET_FLAG_WHILE_NE)||(((LoopEnvironment) top).state==BRACKET_FLAG_DO))));
                        if(doBranching){
                            brackets.add(top);//re-add loop-environment that is ended by break
                            skipTo(SKIP_LOOP,count);
                        }else{//don't break in flat mode
                            brackets.addAll(reconstruct);//reset state of brackets
                        }
                    }
                    return true;
                case BRACKET_FLAG_DO:
                    addBracket(new LoopEnvironment(((BracketDeclaration) a).declarationEnvironment
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
                                BracketEnvironment bracket = brackets.peekLast();
                                if(!(bracket instanceof LoopEnvironment))
                                    throw new SyntaxError(this,"Unexpected ELSE-statement");
                                switch (((LoopEnvironment) bracket).state){
                                    case BRACKET_FLAG_IF_EQ:
                                    case BRACKET_FLAG_IF_NE:
                                    case BRACKET_FLAG_ELIF_EQ:
                                    case BRACKET_FLAG_ELIF_NE:break;
                                    default:
                                        throw new SyntaxError(this,"Unexpected ELSE-statement");
                                }//inherit environment from parent
                                addToStack(a, true);
                                break;
                            }
                        }else{
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

    private void exitFunction(FunctionEnvironment function,boolean doBranching) throws IOException, SyntaxError {
        BracketEnvironment env = removeBracket();
        if (env != function) {//close all brackets
            throw new RuntimeException("brackets and callStack out of sync");
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
            exitFunction(callStack.removeLast(),doBranching);
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
                FunctionEnvironment call = new FunctionEnvironment(((CallFunction) last).getDeclarationEnvironment(),
                        new CodePosition(code, isScript), actionStack,new ProgramEnvironment.ArgumentData(((CallFunction) last).getArgs()), currentLine.toString());
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
                            addBracket(new LoopEnvironment(((BracketDeclaration) last).declarationEnvironment,
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
                            addBracket(new LoopEnvironment(((BracketDeclaration) last).declarationEnvironment,
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
                            addBracket(new LoopEnvironment(((BracketDeclaration) last).declarationEnvironment,
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
                            addBracket(new LoopEnvironment(((BracketDeclaration) last).declarationEnvironment,
                                    ((BracketDeclaration) last).declarationStart, BRACKET_FLAG_WHILE_NE));
                        }
                        if ((doBranching) && ((BracketDeclaration) last).param.equals(Real.Int.ZERO)) {
                            skipTo(SKIP_LOOP, 1);
                            return;
                        }
                        break;
                    case BRACKET_FLAG_END_WHILE_EQ:
                        if(doBranching&&((BracketDeclaration) last).param.equals(Real.Int.ZERO)){
                            BracketEnvironment loop=brackets.peekLast();
                            if(!(loop instanceof LoopEnvironment)||((LoopEnvironment)loop).state!= BRACKET_FLAG_DO)
                                throw new SyntaxError(this,"Unexpected end of DO-WHILE loop");
                            goTo(loop.bracketStart);
                        }else{
                            BracketEnvironment closed= removeBracket();
                            if(!(closed instanceof LoopEnvironment)||((LoopEnvironment)closed).state!= BRACKET_FLAG_DO)
                                throw new SyntaxError(this,"Unexpected end of DO-WHILE loop");
                        }break;
                    case BRACKET_FLAG_END_WHILE_NE:
                        if(doBranching&&!((BracketDeclaration) last).param.equals(Real.Int.ZERO)){
                            BracketEnvironment loop=brackets.peekLast();
                            if(!(loop instanceof LoopEnvironment)||((LoopEnvironment)loop).state!= BRACKET_FLAG_DO)
                                throw new SyntaxError(this,"Unexpected end of DO-WHILE loop");
                            goTo(loop.bracketStart);
                        }else{
                            BracketEnvironment closed= Interpreter.this.removeBracket();
                            if(!(closed instanceof LoopEnvironment)||((LoopEnvironment)closed).state!= BRACKET_FLAG_DO)
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
                    FunctionEnvironment call = new FunctionEnvironment(((FunctionDeclaration) last).function.declarationEnvironment,
                            new CodePosition(code, isScript), actionStack,new ProgramEnvironment.ArgumentData(args), currentLine.toString());
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
                    isScript=((header.type&Translator.FILE_TYPE_SCRIPT)!=0);
                }
                if(!importDejaVu.add(code.getSourceId())) {//check if codeFile is already imported in this direct hierarchy
                    throw new SyntaxError(this,"Cyclic Import in: "+importDejaVu);
                }
                ensureRoot();
                return;
            }else if(last instanceof ProgramEnvironment){
                throw new SyntaxError(this,"Unfinished Expression");
            }else{
                throw new RuntimeException("Unexpected type of Action:"+last.getClass());
            }
            //Value weiterleiten
            while (actionStack.size()>0&&actionStack.getLast() instanceof ProgramEnvironment){
                actionStack.removeLast();
                envStack.removeLast();
            }
            if(actionStack.size()>0){
                actionStack.getLast().pushArg(value);
            }else{
                //handle return values
                envStack.getLast().setRes(value.getValue());
            }
        }
    }


    @NotNull
    private BracketEnvironment removeBracket() {
        ProgramEnvironment env=envStack.removeLast();//remove Bracket Root
        BracketEnvironment bracket = brackets.removeLast();
        if(env!=bracket.getLocalRoot())
            throw new RuntimeException("envStack out of sync with bracketStack");
        return bracket;
    }

    private void addBracket(BracketEnvironment env) {
        envStack.add(env.getLocalRoot());//add Bracket Root
        brackets.addLast(env);
    }

    private void ensureRoot() throws SyntaxError {
        while (actionStack.size()>0){
            if(actionStack.removeLast() instanceof ProgramEnvironment){
                envStack.removeLast();//synchronize envStack and actionStack
            }else{
                throw new SyntaxError(this,"Bracket declaration on non-root Level");
            }
        }
    }

    private void updateIfState(int newState) throws SyntaxError {
        BracketEnvironment env = brackets.peekLast();
        if (env instanceof LoopEnvironment) {
            switch (((LoopEnvironment) env).state) {
                case BRACKET_FLAG_IF_EQ:
                case BRACKET_FLAG_IF_NE:
                case BRACKET_FLAG_ELIF_EQ:
                case BRACKET_FLAG_ELIF_NE:
                    ((LoopEnvironment) env).state = newState;
                    break;
                default:
                    throw new SyntaxError(this,"unexpected elif-statement");
            }
        } else {
            throw new SyntaxError(this,"unexpected elif-statement");
        }
    }

    private int stackFlags() {
        int flags= FLAG_ROOT,prev= FLAG_ROOT;//TODO better flag-management
        for(Action a:actionStack){//iterate through elements of actionStack
            prev=flags;
            if (!(a instanceof ProgramEnvironment)) {//No change for program environments
                if(a instanceof VarPointer){
                    flags&=FLAG_VAR_DECLARATION_CHAIN;
                    if(flags==0)
                        return 0;
                }else if(a instanceof Operator||a instanceof Output){
                    flags&=((a instanceof Operator&&((Operator) a).args.length>1)?0:FLAG_SINGLE_ARG_CHAIN)
                            |FLAG_OPERATOR_CHAIN;
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
