package bsoelch.cnl;

import bsoelch.cnl.interpreter.*;
import bsoelch.cnl.math.MathObject;
import bsoelch.cnl.math.Real;
import bsoelch.cnl.math.Tuple;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    static private final int ACTION_EXECUTE=0b1;
    static private final int ACTION_COMPILE=0b10;
    static private final int ACTION_DECOMPILE=0b100;
    static private final int ACTION_TEST=0b1000;

    static private final Reader in=new InputStreamReader(System.in);

    public static void compileFinished(long actions, long bits){
        System.out.println();
        System.out.println("Compiled: " + actions + " actions, " +bits + " bits");
    }
    public static void decompileFinished(long lines, long actions){
        System.out.println();
        System.out.println("Decompiled: " + lines + " lines, " + actions + " actions");
    }
    public static void executeFinished(long count, boolean doBranching){
        System.out.println();//line to finish eventually unfinished lines of code output
        System.out.println();
        System.out.println((doBranching?"Executed: ":"Tested: ") + count + " actions");
    }
    public static @NotNull MathObject[] getArgs(int argCount) {
        MathObject[] args=new MathObject[argCount];
        System.out.println();
        if(argCount>0) {
            System.out.println("Input Program Arguments:");
            try (Scanner scan = new Scanner(System.in)) {
                for (int i = 0; i < args.length; i++) {
                    System.out.print("arg" + i + ": ");
                    try {
                        args[i] = MathObject.FromString.fromString(scan.nextLine().trim(), Constants.DEFAULT_BASE);
                    } catch (IllegalArgumentException iae) {
                        System.out.println(iae.getMessage());
                        i--;
                    }
                }
            }
            System.out.println();
        }
        return args;
    }


    public static void test(File testFile) throws IOException, SyntaxError, CNL_RuntimeException {
        Interpreter ip=new Interpreter(testFile,null, true);
        ip.test();
    }
    public static void compile(Reader source, File target) throws IOException, SyntaxError, CNL_RuntimeException {
        Translator.compile(source,target);
    }
    public static void decompile(File source, Writer target) throws IOException, SyntaxError, CNL_RuntimeException {
        Translator.decompile(source,target);
    }
    public static MathObject execute(File code,MathObject[] args,boolean forceRunLibs) throws IOException, SyntaxError, CNL_RuntimeException {
        Interpreter ip=new Interpreter(code,args, forceRunLibs);
        System.out.println();//space to separate code output from rest of console
        return ip.run();
    }

    //addLater customizable IO-console, getFileByUrl, ...

    private static char nextChar() {
        try {
            int read = in.read();
            if (read == -1)
                throw new RuntimeException("Unexpected end of System.in");
            return (char) read;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String readUnicodeChar(){
            //read one codepoint
            char read = nextChar();
            if(Character.isHighSurrogate(read)){
                char low = nextChar();
                return new String(new char[]{read,low});
            }
            return ""+read;
    }

    public static String readWord(){
        StringBuilder word=new StringBuilder();
        char r;
        while(true) {//ignore leading whitespaces
            if (!Character.isWhitespace(r = nextChar())) break;
        }
        word.append(r);
        while(!Character.isWhitespace(r=nextChar())) {
            word.append(r);
        }
        return word.toString();
    }
    public static String readValue(){
        StringBuilder val=new StringBuilder();
        char r;
        int brackets=0,sqBrackets=0,setBrackets=0;
        boolean string1=false,string2=false;
        while(true) {//ignore leading whitespaces
            if (!Character.isWhitespace(r = nextChar())) break;
        }
        do{
            val.append(r);
            switch (r){
                case '(':if(!(string1|string2))brackets++;break;
                case '[':if(!(string1|string2))sqBrackets++;break;
                case '{':if(!(string1|string2))setBrackets++;break;
                case ')':if(!(string1|string2))brackets--;break;
                case ']':if(!(string1|string2))sqBrackets--;break;
                case '}':if(!(string1|string2))setBrackets--;break;
                case '\'':if(!string2)string1=!string1;break;
                case '"':if(!string1)string2=!string2;break;
                case '\\':if(string1||string2)val.append(readUnicodeChar());//skip next char
            }
            r=nextChar();
        }while (string1||string2||brackets>0||sqBrackets>0||setBrackets>0||(!Character.isWhitespace(r)));
        return val.toString();
    }
    public static String readLine(){
        StringBuilder word=new StringBuilder();
        char r;
        while(!((r=nextChar())=='\n'||r=='\r')) {
            word.append(r);
        }
        return word.toString();
    }

    //addLater compile/decompile directory (all files ending with .cnl / .cnls)

    public static void main(String[] args) {
        if(args.length==0){
            System.out.println("CNL Version 0.9");
            System.out.println();
            printExpectedArgs();
        }else{
            boolean runLibs=false;
            String source=null,decompile=null,main=null;
            MathObject[] programArgs=null;
            int actions=0;
            String last=null;
            for(int i=0;i<args.length;i++){
                if(last!=null&&last.equals("-args")){
                    StringBuilder argString=new StringBuilder();
                    argString.append(args[i]);
                    if(argString.charAt(0)!='('){
                        System.out.println("Unexpected parameter for -args: "+args[i]);
                        System.out.println("parameter of -args has to start with (");
                        return;
                    }
                    int brackets=1;
                    do {
                        for (int j = 1; j < argString.length(); j++) {
                            if (argString.charAt(j) == '(') {
                                brackets++;
                            } else if (argString.charAt(j) == ')') {
                                brackets--;
                                if (brackets == 0) {
                                    if(j<argString.length()-1){
                                        System.out.println("Unexpected parameter for -args: "+argString);
                                        System.out.println("parameter of -args has to end with ) matching to the starting (");
                                        return;
                                    }
                                }
                            }
                        }
                        if(brackets>0){
                            if(++i<args.length) {
                                argString.append(' ').append(args[i]);
                            }else {
                                System.out.println("Missing closing bracket in argument String");
                            }
                        }
                    }while (brackets>0);
                    if(argString.substring(1,argString.length()-1).trim().isEmpty()) {
                        programArgs = new MathObject[0];
                    }else {
                        MathObject value = MathObject.FromString.fromString(argString.toString(), Constants.DEFAULT_BASE);
                        if (value instanceof Tuple) {
                            programArgs = ((Tuple) value).toArray();
                            for(int j=0;j< programArgs.length;j++){
                                if(programArgs[j]==null)
                                    programArgs[j]= Real.Int.ZERO;
                            }
                        } else {
                            programArgs = new MathObject[]{value};
                        }
                    }
                }else{
                    switch (args[i].toLowerCase(Locale.ROOT)){
                        case "-x":last="-x";actions|=ACTION_EXECUTE;break;
                        case "-c":last="-c";actions|=ACTION_COMPILE;break;
                        case "-s":last="-s";actions|=ACTION_DECOMPILE;break;
                        case "-t":last="-t";actions|=ACTION_TEST;break;
                        case "-args":
                            if(programArgs!=null){
                                System.out.println(" duplicate -args statement");
                                return;
                            }
                            last="-args";
                            break;
                        case "-libx":
                            last="-libx";
                            runLibs=true;
                        break;
                        default:{
                            if(last!=null) {
                                switch (last) {
                                    case "-c":
                                        if(source==null){
                                            source=args[i];
                                        }else{
                                            System.out.println("Syntax Error: source is already defined");
                                            return;
                                        }
                                    break;
                                    case "-s":
                                        if(decompile==null){
                                            decompile=args[i];
                                        }else{
                                            System.out.println("Syntax Error: decompile-target is already defined");
                                            return;
                                        }
                                    break;
                                    default:
                                        if(main==null){
                                            main=args[i];
                                        }else{
                                            System.out.println("Syntax Error: main-file is already defined");
                                            return;
                                        }
                                        break;
                                }
                            }else{
                                main=args[i];break;
                            }
                        }
                    }
                }
            }

            if((actions&ACTION_TEST)!=0){
                if(main==null){
                    System.out.println("Missing argument for compile: No source file");
                    return;
                }
                try {
                    test(fileFromPath(main));
                }catch (IOException io){
                    System.out.println("IOException while execution Test:");
                    System.out.println(io.getMessage());
                    return;//end program on error
                }catch (CNL_Exception se){
                    System.out.println("Test failed:");
                    System.out.println(se.getMessage());
                    printStack(se);
                    return;//end program on error
                }
            }//no else
            if((actions&ACTION_COMPILE)!=0){
                if(source==null){
                    if(main==null){
                        System.out.println("Missing argument for compile: No source file");
                        return;
                    }
                    int lastDot=main.lastIndexOf(".");
                    if(lastDot>0&&lastDot>main.lastIndexOf(File.separatorChar)){
                        source=main;
                        main=main.substring(0,lastDot)+".cnls";
                    }else{
                        source=main;
                        main=main+".cnls";
                    }
                }
                try (Reader read=new InputStreamReader(new FileInputStream(fileFromPath(source)),StandardCharsets.UTF_8)){
                    compile(read, fileFromPath(main));
                }catch (IOException io){
                    System.out.println("IOException during Compiling:");
                    System.out.println(io.getMessage());
                    return;//end program on error
                }catch (CNL_Exception se){
                    System.out.println("Compiling failed:");
                    System.out.println(se.getMessage());
                    printStack(se);
                    return;//end program on error
                }
            }//no else
            if((actions&ACTION_DECOMPILE)!=0){
                if(decompile==null){
                    if(main==null){
                        System.out.println("Missing argument for compile: No source file");
                        return;
                    }
                    int lastDot=main.lastIndexOf(".");
                    if(lastDot>0&&lastDot>main.lastIndexOf(File.separatorChar)){
                        decompile=main.substring(0,lastDot)+".cnls";
                    }else{
                        decompile=main+".cnls";
                    }
                }
                try(Writer write=new OutputStreamWriter(new FileOutputStream(fileFromPath(decompile)),StandardCharsets.UTF_8) ){
                    decompile(fileFromPath(main),write);
                }catch (IOException io){
                    System.out.println("IOException during Decompiling:");
                    System.out.println(io.getMessage());
                    return;//end program on error
                }catch (CNL_Exception se){
                    System.out.println("Decompiling failed:");
                    System.out.println(se.getMessage());
                    printStack(se);
                    return;//end program on error
                }
            }//no else
            if((actions&ACTION_EXECUTE)!=0){
                if(main==null){
                    System.out.println("Missing argument for compile: No source file");
                    return;
                }

                try {
                    MathObject res=execute(fileFromPath(main),programArgs,runLibs);
                    System.out.println("Execution finished with return-value: "+res);
                }catch (IOException io){
                    System.out.println("IOException during Execution:");
                    System.out.println(io.getMessage());
                }catch (SyntaxError se){
                    System.out.println("Execution failed:");
                    System.out.println(se.getMessage());
                    printStack(se);
                }catch (CNL_RuntimeException se){
                    System.out.println("Uncaught Execution:");
                    System.out.println(se.getMessage());
                    printStack(se);
                }
            }//no else
        }
    }

    private static void printStack(CNL_Exception se) {
        boolean first=true;
        for(String line: se.getStack()){
            if(first){
                System.out.println("While executing Line:"+line);
                first=false;
            }else{
                System.out.println(" called from:"+line);
            }
        }
    }

    @NotNull
    private static File fileFromPath(String path) {
        if(path.startsWith(".")){
            path= Paths.get(System.getProperty("user.dir")+path.substring(1)).toString();
        }else if(path.startsWith("~")){
            path= Paths.get(System.getProperty("user.home")+path.substring(1)).toString();
        }
        return new File(path);
    }

    private static void printExpectedArgs() {
        System.out.println("Expected Argument: [flags] <mainFile>");
        System.out.println("file names starting with . are viewed as relative to <localDir> ");
        System.out.println("file names starting with ~ are viewed as relative to  <userDir> ");
        System.out.println("the order of operations is independent of the order of the supplied flags:" +
                " test before compile before decompile before execute");
        System.out.println("Flags:");
        System.out.println("-x\t execute the main file");
        System.out.println("-c\t compile the main file to a cnl-code file, the target file can be supplied as an optional Argument");
        System.out.println("-s\t decompile the main file to a cnl-script file, the target file can be supplied as an optional Argument");
        System.out.println("-t\t test the mainFile for syntax errors (is automatically included for -c and -s)");
        System.out.println("-libX\t allows the execution of library cnl files");
        System.out.println("-args\t supplies Program Arguments as a comma separated list surrounded with brackets");
        System.out.println("\nExamples:");
        System.out.println("cnl \"./name.cnl\"  \t\t runs the File \"name.cnl\" in the local directory");
        System.out.println("cnl -args (1 ,2) \"./name.cnl\"  \t\t runs the File \"name.cnl\" in the local directory with the arguments 1 and 2");
        System.out.println("cnl -c -s \"./decompile.txt\" \"./name.txt\"  \t\t compiles the file \"name.txt\" to the file \"name.cnl\" " +
                "and decompiles it afterwards to \"decompile.txt\"");
        System.out.println("cnl -c \"./source.txt\"  -s \"./name.cnl\"  \t\t compiles the file \"source.txt\" to the file \"name.cnl\" " +
                "and decompiles it afterwards to \"name.cnls\"");
        System.out.println("cnl -t \"./source.txt\"  \t\t checks if \"source.txt\" is a valid cnl-script");
        System.out.println("cnl -t \"./source.cnl\"  \t\t checks if \"source.txt\" is a valid cnl-code file");
    }

}
