package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessFile;
import bsoelch.cnl.BitRandomAccessStream;
import bsoelch.cnl.Main;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;

import static bsoelch.cnl.Constants.*;

public class Interpreter {
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

        CodePosition(BitRandomAccessStream file, long position, boolean isScript) {
            this.file = file;
            this.position = position;
            this.isScript = isScript;
        }
    }
    static class ImportReturnPosition{
        final CodePosition codePos;
        final File codeFile;
        final ProgramEnvironment prevEnv;
        final ExecutionEnvironment exEnv;

        ImportReturnPosition(File codeFile, CodePosition codePos, ProgramEnvironment prevEnv,ExecutionEnvironment exEnv) {
            this.codePos = codePos;
            this.codeFile = codeFile;
            this.prevEnv = prevEnv;
            this.exEnv=exEnv;
        }
    }

    private ExecutionEnvironment exEnv;
    private File codeDir;
    private BitRandomAccessStream code;
    private boolean isScript;

    private long lineCounter=0;

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

    private final StringBuilder currentLine =new StringBuilder();

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

    public MathObject run() throws IOException {
        return run(true);
    }
    public void test() throws IOException {
        run(false);
    }

    MathObject run(boolean doBranching) throws IOException {
        long count=0;
        while(doStep(doBranching)){
            count++;
        }
        Main.executeFinished(count,doBranching);
        return envStack.getLast().getRes();
    }

    void flatStep() throws IOException {
        doStep(false);
    }

    private boolean doStep(boolean doBranching) throws IOException {
        if(actionStack.isEmpty()){
            currentLine.setLength(0);
            lineCounter++;
        }
        Action a;
        if(isScript){
            if(doBranching)
                throw new IllegalArgumentException("Cannot run scripts");
            a= Translator.nextAction(code.reader(), programEnvironment(), executionEnvironment(), isTopLayer());
        }else{
            a= Translator.nextAction(code, programEnvironment(), executionEnvironment(), isTopLayer());
        }
        currentLine.append(a.stringRepresentation()).append(' ');
        try {
            return stepInternal(a, true);
        }catch (IllegalArgumentException|IllegalStateException e){
            throw new IllegalStateException("Error while execution line "+lineCounter+": "+currentLine,e);
        }
    }


    boolean stepInternal(Action a,boolean doBranching) throws IOException {
        //TODO better error messages
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
                                    skipTo(SKIP_END_IF);
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
                                throw new IllegalStateException("Unexpected ELSE statement");
                        }
                    } else {
                        throw new IllegalStateException("Unexpected ELSE statement");
                    }
                }
                case BRACKET_FLAG_END: {
                    BracketEnvironment top = brackets.peekLast();
                    if (top instanceof LoopEnvironment) {
                        if(doBranching) {
                            switch (((LoopEnvironment) top).state) {
                                case BRACKET_FLAG_WHILE_EQ:
                                case BRACKET_FLAG_WHILE_NE: //jump to start of loop
                                    goTo(((LoopEnvironment) top).loopStart);
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
                        throw new IllegalStateException(top == null ? "Unexpected End of loop" : "Unexpected BracketEnvironment");
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
    private void skipTo(int stepType) throws IOException {
        if(isScript)
            throw new IllegalStateException("cannot run Scripts");
        int bracketCount=1;
        Action a;
        while (bracketCount>0){
            a= Translator.nextAction(code,envStack.getLast(), exEnv,brackets.isEmpty());
            if(a== Translator.EOF){//ignore EXIT statements
                throw new IllegalStateException("Unfinished Loop");
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
                                    throw new IllegalStateException("Unexpected ELSE-statement");
                                switch (((LoopEnvironment) bracket).state){
                                    case BRACKET_FLAG_IF_EQ:
                                    case BRACKET_FLAG_IF_NE:
                                    case BRACKET_FLAG_ELIF_EQ:
                                    case BRACKET_FLAG_ELIF_NE:break;
                                    default:
                                        throw new IllegalStateException("Unexpected ELSE-statement");
                                }//inherit environment from parent
                                addToStack(a, true);
                                break;
                            }
                        }else{
                            throw new IllegalStateException("Unexpected ELSE-statement");
                        }
                    }break;
                    case BRACKET_FLAG_END_WHILE_EQ:
                    case BRACKET_FLAG_END_WHILE_NE:{
                        bracketCount--;
                        if(bracketCount<=0)//do while should not be end of a skip statement
                            throw new IllegalStateException("Unexpected end of DO-WHILE");
                    }break;
                    case BRACKET_FLAG_END:{
                        bracketCount--;
                        if(bracketCount<0)
                            throw new IllegalStateException("SyntaxError: Unexpected end of bracket");
                        if(bracketCount==0) {
                            switch (stepType){
                                case SKIP_IF:{//unexpected end of if
                                    removeBracket();
                                }//fallthrough
                                case SKIP_END_IF:
                                case SKIP_LOOP:
                                case SKIP_FUNCTION:
                                    break;
                            }
                        }
                    }break;
                }
            }
        }
    }

    private void exitFunction(FunctionEnvironment function,boolean doBranching) throws IOException {
        BracketEnvironment env = removeBracket();
        if (env != function) {//close all brackets
            throw new RuntimeException("brackets and callStack out of sync");
        }
        actionStack= function.prevStack;//exit function
        if(doBranching) {
            goTo(function.prev);
        }
        addToStack(Translator.wrap(function.getRes()), doBranching);
    }


    private boolean exit(boolean doBranching) throws IOException {
        evaluateStack(true,doBranching);
        if(callStack.size()>0){
            exitFunction(callStack.removeLast(),doBranching);
            return true;
        }else if(importReturnStack.size()>0){
            importDejaVu.remove(code.getSourceId());//remove from dejaVu stack after closing
            ImportReturnPosition ret=importReturnStack.removeLast();
            codeDir =ret.codeFile;
            exEnv=ret.exEnv;
            if(ret.prevEnv!=envStack.removeLast())
                throw new RuntimeException("envStack out of sync with Imports");
            goTo(ret.codePos);
            return true;
        }else{
            try{
                code.close();
            }catch (IOException io){
                System.err.println("\nException while closing codeFile:");
                io.printStackTrace();
            }
            return false;
        }
    }

    /**go To the given CodePosition*/
    private void goTo(CodePosition newPos) throws IOException {
        code = newPos.file;
        isScript=newPos.isScript;
        code.seek(newPos.position);
    }


    private void addToStack(Action action, boolean doBranching) throws IOException {
        actionStack.add(action);
        evaluateStack(false,doBranching);//simplify after adding
    }

    private void evaluateStack(boolean forceEvaluate,boolean doBranching) throws IOException {
        Action last;
        ValuePointer value;
        while (actionStack.size()>0){
            last=actionStack.getLast();
            int flags = stackFlags();
            if(!forceEvaluate&&last.acceptsArg(flags))
                return;//stack can accept further arguments
            if(last.requiresArg())
                throw new IllegalStateException("Missing Argument");
            actionStack.removeLast();
            if(last instanceof ValuePointer){
                if(last instanceof ArgPointer&&callStack.isEmpty()&&importReturnStack.size()>0){
                    throw new IllegalStateException("Cannot use Program Arguments within Imports");
                }
                value=(ValuePointer) last;
            }else if(last instanceof Operator){
                if(doBranching) {
                    value = ((Operator) last).preformOperation(flags);
                }else{
                    value= Translator.ZERO;//TODO? specific DefaultValue
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
                    throw new IllegalStateException("Waiting for Function call");
                FunctionEnvironment call = new FunctionEnvironment(((CallFunction) last).getDeclarationEnvironment(),
                        new CodePosition(code, code.bitPos(), isScript), actionStack,new ProgramEnvironment.ArgumentData(((CallFunction) last).getArgs()));
                if(doBranching) {
                    addBracket(call);//add function call to brackets to allow easy management of open structures
                    callStack.addLast(call);
                    actionStack = new ArrayDeque<>();
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
                                    new CodePosition(code, code.bitPos(), isScript),BRACKET_FLAG_IF_EQ));
                        }else{//update state
                            updateIfState(BRACKET_FLAG_ELIF_EQ);
                        }
                        if(doBranching&&(!((BracketDeclaration) last).param.equals(Real.Int.ZERO))){
                            ensureRoot();
                            skipTo(SKIP_IF);
                            return;
                        }break;
                    case BRACKET_FLAG_IF_NE:
                    case BRACKET_FLAG_ELIF_NE:
                        if(((BracketDeclaration) last).type ==BRACKET_FLAG_IF_NE) {
                            addBracket(new LoopEnvironment(((BracketDeclaration) last).declarationEnvironment,
                                    new CodePosition(code, code.bitPos(), isScript),BRACKET_FLAG_IF_NE));
                        }else{//update state
                            updateIfState(BRACKET_FLAG_ELIF_NE);
                        }
                        if(doBranching&&((BracketDeclaration) last).param.equals(Real.Int.ZERO)){
                            ensureRoot();
                            skipTo(SKIP_IF);
                            return;
                        }break;
                    case BRACKET_FLAG_WHILE_EQ:if((!doBranching)||((BracketDeclaration) last).param.equals(Real.Int.ZERO)){
                        addBracket(new LoopEnvironment(((BracketDeclaration) last).declarationEnvironment,
                                ((BracketDeclaration) last).declarationStart, BRACKET_FLAG_WHILE_EQ));
                    }else{
                        skipTo(SKIP_LOOP);
                        return;
                    }break;
                    case BRACKET_FLAG_WHILE_NE:if((!doBranching)||!((BracketDeclaration) last).param.equals(Real.Int.ZERO)){
                        addBracket(new LoopEnvironment(((BracketDeclaration) last).declarationEnvironment,
                                ((BracketDeclaration) last).declarationStart, BRACKET_FLAG_WHILE_NE));
                    }else{
                        skipTo(SKIP_LOOP);
                        return;
                    }break;
                    case BRACKET_FLAG_END_WHILE_EQ: if(doBranching&&((BracketDeclaration) last).param.equals(Real.Int.ZERO)){
                        BracketEnvironment loop=brackets.peekLast();
                        if(!(loop instanceof LoopEnvironment)||((LoopEnvironment)loop).state!= BRACKET_FLAG_DO)
                            throw new IllegalStateException("Unexpected end of DO-WHILE loop");
                        goTo(((LoopEnvironment) loop).loopStart);
                    }else{
                        BracketEnvironment closed= removeBracket();
                        if(!(closed instanceof LoopEnvironment)||((LoopEnvironment)closed).state!= BRACKET_FLAG_DO)
                            throw new IllegalStateException("Unexpected end of DO-WHILE loop");
                    }break;
                    case BRACKET_FLAG_END_WHILE_NE: if(doBranching&&!((BracketDeclaration) last).param.equals(Real.Int.ZERO)){
                        BracketEnvironment loop=brackets.peekLast();
                        if(!(loop instanceof LoopEnvironment)||((LoopEnvironment)loop).state!= BRACKET_FLAG_DO)
                            throw new IllegalStateException("Unexpected end of DO-WHILE loop");
                        goTo(((LoopEnvironment) loop).loopStart);
                    }else{
                        BracketEnvironment closed= Interpreter.this.removeBracket();
                        if(!(closed instanceof LoopEnvironment)||((LoopEnvironment)closed).state!= BRACKET_FLAG_DO)
                            throw new IllegalStateException("Unexpected end of DO-WHILE loop");
                    }break;
                    default:throw new IllegalStateException("Unexpected state of Bracket");
                }
                ensureRoot();
                return;
            }else if(last instanceof FunctionDeclaration){
                envStack.getLast().putFunction(((FunctionDeclaration) last).fktId
                        ,((FunctionDeclaration) last).function);
                ensureRoot();
                if(doBranching) {
                    skipTo(SKIP_FUNCTION);
                }else{
                    ValuePointer[] args=new ValuePointer[((FunctionDeclaration) last).function.argCount];
                    Arrays.fill(args,Translator.ZERO);//Default Value
                    FunctionEnvironment call = new FunctionEnvironment(((FunctionDeclaration) last).function.declarationEnvironment,
                            new CodePosition(code, code.bitPos(), isScript), actionStack,new ProgramEnvironment.ArgumentData(args));
                    addBracket(call);//add function call to brackets to allow easy management of open structures
                    callStack.addLast(call);
                    actionStack = new ArrayDeque<>();
                }
                return;
            }else if(last instanceof Import){
                importReturnStack.addLast(new ImportReturnPosition(codeDir, new CodePosition(code, code.bitPos(), isScript),((Import) last).getTarget(),exEnv));
                if(!importEnvironments.add(((Import) last).getTarget()))
                    throw new IllegalStateException("Multiple Imports in the same Environment");
                envStack.addLast(((Import) last).getTarget());
                File target=new File(codeDir.getAbsolutePath()+File.separator+((Import) last).getSource());
                codeDir =target.getParentFile();
                exEnv=new ExecutionEnvironment(codeDir);//change execution environment
                code =new BitRandomAccessFile(target,"r");
                Translator.FileHeader header=Translator.readCodeFileHeader(code);
                if(header.type==Translator.FILE_TYPE_INVALID){
                    throw new IllegalArgumentException("Invalid import-File: "+target.getAbsolutePath());
                }else if((header.type&Translator.FILE_TYPE_EXECUTABLE)!=0){
                    throw new IllegalArgumentException("Cannot import Executables: "+target.getAbsolutePath());
                }else{
                    isScript=((header.type&Translator.FILE_TYPE_SCRIPT)!=0);
                }
                if(!importDejaVu.add(code.getSourceId())) {//check if codeFile is already imported in this direct hierarchy
                    throw new IllegalStateException("Cyclic Import in: "+importDejaVu);
                }
                ensureRoot();
                return;
            }else if(last instanceof ProgramEnvironment){
                throw new IllegalStateException("Unfinished Expression");
            }else{
                throw new RuntimeException("Unexpected Action");
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

    private void ensureRoot() {
        while (actionStack.size()>0){
            if(actionStack.removeLast() instanceof ProgramEnvironment){
                envStack.removeLast();//synchronize envStack and actionStack
            }else{
                throw new IllegalStateException("Bracket declaration on non-root Level");
            }
        }
    }

    private void updateIfState(int newState) {
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
                    throw new IllegalStateException("unexpected elif");
            }
        } else {
            throw new IllegalStateException("unexpected elif");
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
