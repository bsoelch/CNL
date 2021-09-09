package bsoelch.cnl.interpreter;

import bsoelch.cnl.BitRandomAccessFile;
import bsoelch.cnl.BitRandomAccessStream;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ExecutionEnvironment {
    final File rootDir;

    final HashMap<String, BitRandomAccessStream> openFiles;

    /**Creates a new Execution Environment with the given root-Directory*/
    public ExecutionEnvironment(File rootDir) {
        this.rootDir=rootDir;
        openFiles=new HashMap<>();
    }

    public BitRandomAccessStream fileAt(String path) throws IOException{
        path = toAbsolutePath(path);
        BitRandomAccessStream cached=openFiles.get(path);
        if (cached == null) {
            cached = new BitRandomAccessFile(new File(path), "rw");
            openFiles.put(path, cached);
        }
        return cached;
    }

    public void closeFileAt(String path) throws IOException {
        path = toAbsolutePath(path);
        BitRandomAccessStream file=openFiles.remove(path);
        if(file!=null){
            file.close();
        }
    }

    @NotNull
    private String toAbsolutePath(String path) {
        path = rootDir.getAbsolutePath() + File.separator + path;
        return path;
    }


}
