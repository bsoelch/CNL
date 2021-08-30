package bsoelch.cnl;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigInteger;

import java.nio.charset.StandardCharsets;

public class BitRandomAccessFile implements BitRandomAccessStream {
    final Object ioLock=new Object();
    final RandomAccessFile byteFile;
    long bytePos;
    /**Maske des Aktuellen bits*/
    int bitMask=1,bitIndex=0;
    /**the byte the current bit position is in*/
    int currentByte=0,cachedBits=0;
    /**true if the current byte is in cache (and the bytePointer is after the current byte) */
    boolean cacheChanged=false;

    final private String path;

    private Reader readerCache=null;
    private Writer writerCache=null;

    public BitRandomAccessFile(@NotNull File file,String mode) throws IOException {
        this.byteFile = new RandomAccessFile(file,mode);
        bytePos=byteFile.getFilePointer();
        this.path=file.getAbsolutePath();
    }

    @Override
    public void seek(long newBitPos) throws IOException {
        if(newBitPos<0){
            throw new IOException("Negative bitPos");
        }else{
            synchronized (ioLock) {
                long newBytePos=newBitPos /8;
                if(bytePos!=newBytePos){
                    writeChanges();
                    cachedBits = 0;
                    cacheChanged=false;
                }
                bytePos= newBytePos;
                bitIndex=(int)(newBitPos %8);
                bitMask=1<<bitIndex;
                byteFile.seek(bytePos);
            }
        }
    }
    @Override
    public void truncateToSize(boolean byteFillOne) throws IOException {
        if(bitIndex!=0) {
            ensureCached(0xff);
            if (byteFillOne) {
                currentByte |= 0xff & (0xff << bitIndex);
            } else {
                currentByte &= 0xff >> 8 - bitIndex;
            }
            cacheChanged = true;
        }
        writeChanges();
        byteFile.getChannel().truncate(bytePos+(bitIndex==0?0:1));
    }
    @Override
    public long bitPos() {
        return bytePos*8+bitIndex;
    }

    @Override
    public long byteLength() throws IOException {
        return byteFile.length();
    }

    /**reads one bit from the file
     * @return
     * <li>0 ist the current bit is 0</li>
     * <li>1 if the current bit is 1</li>
     * <li>-1 if the end of file is reached</li>
     * @throws IOException if an I/O error occurs.*/
    @Override
    public byte readBit() throws IOException {
        synchronized (ioLock) {
            if(ensureCached(bitMask)){
                return -1;
            }
            int bit = ((currentByte & bitMask) != 0 ? 1 : 0);
            bitMask <<= 1;
            bitIndex++;
            if (bitIndex>7) {
                writeChanges();
                bitMask = 1;
                bitIndex=0;
                bytePos++;
                cachedBits = 0;
                cacheChanged=false;
            }
            return (byte) bit;
        }
    }
    /**@return true iff the end of file is reached*/
    private boolean ensureCached(int bitsMask) throws IOException {
        if((cachedBits&bitsMask)!=(bitsMask&0xff)) {
            byteFile.seek(bytePos);
            int read = byteFile.read();
            if(read==-1){
                cachedBits|=bitsMask&~cachedBits;
                return true;
            }else{
                currentByte=(currentByte&cachedBits)|(read &~cachedBits);
                cachedBits=0xff;
            }
        }
        return false;
    }
    /**writes one bit to the file
     * @throws IOException if an I/O error occurs.*/
    @Override
    public void writeBit(boolean isSet) throws IOException {
        synchronized (ioLock){
            if(isSet){
                currentByte|=bitMask;
            }else{
                currentByte&=~bitMask;
            }
            cachedBits|=bitMask;
            cacheChanged=true;
            bitMask<<=1;
            bitIndex++;
            if(bitIndex>7){
                bitMask=1;
                bitIndex=0;
                byteFile.seek(bytePos++);
                ensureCached(0xff);
                byteFile.write(currentByte & 0xff);
                cachedBits=0;
                cacheChanged=false;
            }
        }
    }

    @Override
    public int readByte() throws IOException {
        if(bitIndex==0){//redirect call if aligned to file-bytes
            synchronized (ioLock) {
                byteFile.seek(bytePos);
                return byteFile.read();
            }
        }else{
            return BitRandomAccessStream.super.readByte();
        }
    }

    @Override
    public void writeByte(int aByte) throws IOException {
        if(bitIndex==0){//redirect call if aligned to file-bytes
            synchronized (ioLock) {
                byteFile.seek(bytePos);
                byteFile.write(aByte);
            }
        }else{
            BitRandomAccessStream.super.writeByte(aByte);
        }
    }

    /**writes all cached changes to the file*/
    @Override
    public void writeChanges() throws IOException{
        synchronized (ioLock) {
            if(cachedBits!=0&&cacheChanged){
                ensureCached(0xff);
                byteFile.seek(bytePos);
                byteFile.write(currentByte & 0xff);
            }
        }
    }

    /** reads multiple bits starting at the current file pointer and stores them in the long-array
     *  @param off stating bitIndex in the array
     *  @param len maximal number of bits read
     *  @return total number of bits read or -1 for end of file*/
    @Override
    public long read(long[] target, long off, long len) throws IOException {
        byte[] bytes;
        int l,bitShift;
        synchronized (ioLock){
            bytes=new byte[Math.toIntExact((len+7)/8+(bitIndex==0?0:1))];
            int i0=0;
            if(cachedBits!=0){//TODO? write cache before read
                ensureCached(0xff<<bitIndex);
                bytes[i0++]=(byte)currentByte;
                byteFile.seek(bytePos+1);
            }else{
                byteFile.seek(bytePos);
            }
            l=byteFile.read(bytes,i0,bytes.length-i0);
            if(l==-1) {
                if(i0==1){
                    l=i0;
                }else{
                    return -1;
                }
            }
            bitShift=bitIndex;//remember shift of first bit
            long pos= bitPos();
            pos+=Math.min(len,8L*l-bitIndex);
            seek(pos);//update file position
            //TODO update cache
        }
        //copy bits to array (does not need IO-Lock)
        long totalBits=0,bitsRead;
        int outIndex=(int)(off/64),outShift=(int)(off%64);
        for(int i=0;i<l&&totalBits<len&&outIndex<target.length;){
            if(len-totalBits>8-bitShift) {
                target[outIndex] |= ((bytes[i]&0xffL) >>> bitShift) << outShift;
            }else{
                target[outIndex] |= (((bytes[i]&0xffL) >>> bitShift)&(0xff>>(8-(len-totalBits)))) << outShift;
            }
            bitsRead=Math.min(len-totalBits,Math.min(64-outShift,8-bitShift));
            totalBits+=bitsRead;
            outShift+=bitsRead;
            bitShift+=bitsRead;
            if(bitShift>=8){
                bitShift=0;
                i++;
            }//no else
            if(outShift>=64){
                outShift=0;
                outIndex++;
            }
        }
        return Math.min(len,totalBits);
    }

    /** reads exactly len bits starting at the current file pointer and stores them in the long-array
     *  @param off stating bitIndex in the array
     *  @param len maximal number of bits read
     *  @throws java.io.EOFException if the file ends*/
    @Override
    public void readFully(long[] target, long off, long len) throws IOException {
        long n = 0;
        do {
            long count = read(target, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        } while (n < len);
    }


    /** writes multiple bits starting at the current file pointer
     *  @param off stating bitIndex in the array
     *  @param len number of bits to write*/
    @Override
    public void write(long[] source, long off, long len) throws IOException {
        synchronized (ioLock) {
            byte[] bytes = new byte[Math.toIntExact((len + 7) / 8 + (bitIndex == 0 ? 0 : 1))];
            if(bitIndex!=0){
                ensureCached(0xff>>8-bitIndex);
            }
            if(cachedBits!=0){
                bytes[0] = (byte) (currentByte&(0xff>>8-bitIndex));
                cachedBits=0;//currentByte no longer a valid cache
            }
            long totalBitsWritten=0,bitsWritten;
            int tIndex = 0,sIndex=(int)(off/64),sOffset=(int)(off%64),tOffset=bitIndex;
            //copyBits
            while(totalBitsWritten<len){
                if(len-totalBitsWritten>=64-sOffset) {
                    bytes[tIndex] |= ((source[sIndex]) >>> sOffset) << tOffset;
                }else{
                    bytes[tIndex] |= (((source[sIndex]) >>> sOffset)&(-1L>>>(64-(len-totalBitsWritten)))) << tOffset;
                }
                bitsWritten=Math.min(len-totalBitsWritten,Math.min(64-sOffset,8-tOffset));
                totalBitsWritten+=bitsWritten;
                tOffset+=bitsWritten;
                sOffset+=bitsWritten;
                if(tOffset>=8){
                    tOffset=0;
                    tIndex++;
                }//no else
                if(sOffset>=64){
                    sOffset=0;
                    sIndex++;
                }
            }
            //write to file
            byteFile.seek(bytePos);
            byteFile.write(bytes,0,tIndex);
            //update pos
            long pos=bitPos()+totalBitsWritten;
            seek(pos);
            if(tOffset!=0){//update cache byte
                cachedBits=0xff>>(8-tOffset);
                currentByte=bytes[tIndex]&cachedBits;
                cacheChanged=true;
            }else{
                cachedBits=0;
                cacheChanged=false;
            }
        }
    }

    @Override
    public BigInteger readBits(int count) throws IOException {
        long[] data=new long[(count+63)/64];
        readFully(data,0,count);
        return fromBits(data);
    }
    @Override
    public void writeBits(BigInteger data, int count) throws IOException {
        long[] bits=new long[(count+63)/64];
        copyBits(data.and(BigInteger.ONE.shiftLeft(count).subtract(BigInteger.ONE)),bits);
        write(bits,0,count);
    }

    @Override
    public void writeBigInt(BigInteger value, int headerLength, int blockLength, int bigBlockLength) throws IOException {
        if(value.signum()<0)
            throw new IllegalArgumentException("value has to be at least 0");
        if(headerLength<2)
            throw new IllegalArgumentException("headerLength has to be at least 2");
        if(blockLength<2)
            throw new IllegalArgumentException("blockLength has to be at least 2");
        if(bigBlockLength<blockLength)
            throw new IllegalArgumentException("bigBlockLength has to be greater than or equal to blockLength");
        long[] numberBits=new long[(headerLength+63)/64];
        if(value.bitLength()<headerLength){//small number
            copyBits(value,numberBits);
            write(numberBits,0,headerLength);
        }else{
            BigInteger maxHeaderPlusOne=BigInteger.ONE.shiftLeft(headerLength-1);
            BigInteger blockSize=BigInteger.ONE.shiftLeft(blockLength),sectionSize=blockSize;
            value=value.subtract(maxHeaderPlusOne);//
            BigInteger blocks=BigInteger.ONE;
            while(blocks.compareTo(maxHeaderPlusOne)<0&&value.compareTo(sectionSize)>=0){
                value=value.subtract(sectionSize);
                sectionSize=sectionSize.multiply(blockSize);
                blocks=blocks.add(BigInteger.ONE);
            }
            if(blocks.compareTo(maxHeaderPlusOne)<0){//large Number
                copyBits(blocks.or(maxHeaderPlusOne),numberBits);
                write(numberBits,0,headerLength);
                long bitLength = blocks.intValueExact() * ((long)blockLength);
                numberBits=new long[(int)((bitLength+63)/64)];
                copyBits(value,numberBits);
                write(numberBits,0,bitLength);
            }else{//very large number
                blockSize=BigInteger.ONE.shiftLeft(bigBlockLength);
                //size of the minimal section
                int blocksOffset = (int) (((maxHeaderPlusOne.longValueExact()-1) * blockLength-1) / bigBlockLength)+1;
                sectionSize=blockSize.pow(blocksOffset);
                blocks=BigInteger.ZERO; //block counter offset by minimal size
                while(value.compareTo(sectionSize)>=0){
                    value=value.subtract(sectionSize);
                    sectionSize=sectionSize.multiply(blockSize);
                    blocks=blocks.add(BigInteger.ONE);
                }
                copyBits(maxHeaderPlusOne,numberBits);
                write(numberBits,0,headerLength);
                writeBigInt(blocks,headerLength,blockLength,bigBlockLength);
                long bitLength = (blocks.intValueExact()+blocksOffset) * ((long)bigBlockLength);
                numberBits=new long[(int)((bitLength+63)/64)];
                copyBits(value,numberBits);
                write(numberBits,0,bitLength);
            }
        }
    }
    /**copies bits from a BigInteger to a longArray,
     * the data will be written in small-endian order
     * @param source BigInteger from which the bits are read
     * @param target long[] to that the bits are copied*/
    private void copyBits(BigInteger source, long[] target){
        if(target.length*64<source.bitLength())
            throw new IllegalArgumentException("target is to small:"+target.length+" required: "+((source.bitLength()+63)/64));
        byte[] bytes=source.toByteArray();
        for(int i = 0;i<bytes.length; i++){
            for(int j=0;j<8&&8*i+j<bytes.length;j++){
                target[i]|=(0xffL&bytes[bytes.length-(8*i+j+1)])<<8*j;
            }
        }
    }

    @Override
    public BigInteger readBigInt(int headerLength, int blockLength, int bigBlockLength) throws IOException {
        if(headerLength<2)
            throw new IllegalArgumentException("headerLength has to be at least 2");
        if(blockLength<2)
            throw new IllegalArgumentException("blockLength has to be at least 2");
        if(bigBlockLength<blockLength)
            throw new IllegalArgumentException("bigBlockLength has to be greater than or equal to blockLength");
        long[] numberBits=new long[(headerLength+63)/64];
        readFully(numberBits,0,headerLength);
        int hiInd=(headerLength-1)/64;
        int hiMask=1<<((headerLength-1)%64);
        if((numberBits[hiInd]&hiMask)==0){//small number
            return fromBits(numberBits);
        }else{//large number
            numberBits[hiInd]^=hiMask;//set hiBit to 0
            BigInteger len=fromBits(numberBits);
            long maxHeaderPlus1 = 1L << headerLength-1;
            BigInteger offset=BigInteger.valueOf(maxHeaderPlus1);//offset for block Numbers
            BigInteger blockSize=BigInteger.valueOf(1).shiftLeft(blockLength);
            long bitsToRead,blockCount;
            if(len.signum()==0){//very large number
                len= readBigInt(headerLength,blockLength,bigBlockLength);
                long blockOffset = (((maxHeaderPlus1-1) * blockLength-1) / bigBlockLength)+1;
                blockCount = len.intValueExact() + blockOffset;
                bitsToRead = ((long)bigBlockLength)* blockCount;

                //starting offset for very large numbers
                offset=offset.add((blockSize.pow((int)maxHeaderPlus1).subtract(BigInteger.ONE)).
                        divide(blockSize.subtract(BigInteger.ONE))).subtract(BigInteger.ONE);
                if(blockCount>blockOffset){//offset for each layer
                    blockSize=BigInteger.valueOf(1).shiftLeft(bigBlockLength);
                    offset=offset.add((blockSize.pow((int)blockCount).subtract(BigInteger.ONE)).
                            divide(blockSize.subtract(BigInteger.ONE))).subtract(
                                    (blockSize.pow((int)blockOffset).subtract(BigInteger.ONE)).
                            divide(blockSize.subtract(BigInteger.ONE)));
                }
            }else{
                blockCount = len.intValueExact() ;
                bitsToRead = ((long)blockLength)* blockCount;

                if(blockCount>1){//offset for each layer
                    offset=offset.add((blockSize.pow((int)blockCount).subtract(BigInteger.ONE)).
                            divide(blockSize.subtract(BigInteger.ONE))).subtract(BigInteger.ONE);
                }
            }
            numberBits=new long[(int)((bitsToRead +63)/64)];
            readFully(numberBits,0,bitsToRead);
            //offset to reduce duplicate numbers
            return fromBits(numberBits).add(offset);
        }
    }
    /**@param numberBits bits in the number (in small endian format)
     * @return an unsigned BigInteger with the binary representation given by {@code numberBits}
     * */
    @NotNull
    private BigInteger fromBits(long[] numberBits) {
        byte[] bytes=new byte[8* numberBits.length+1];
        for(int i = 0; i< numberBits.length; i++){
            for(int j=0;j<8;j++){
                bytes[bytes.length-(8*i+j+1)]=(byte)((numberBits[i]>>>8*j)&0xff);
            }
        }
        return new BigInteger(bytes);
    }



    public Reader reader(){
        if (readerCache == null) {
            readerCache = new Reader() {
                int surrogateCache=-1;
                @Override
                public int read() throws IOException {
                    if(surrogateCache==-1){
                        int cp=readUTF8();
                        if(cp==-1){
                            return -1;
                        }else{
                            char[] chars=Character.toChars(cp);
                            if(chars.length>1)//cannot be greater than 2
                                surrogateCache=chars[1];
                            return chars[0];
                        }
                    }else{
                        char tmp= (char) surrogateCache;
                        surrogateCache=-1;
                        return tmp;
                    }
                }

                @Override
                public int read(char[] cbuf, int off, int len) throws IOException {
                    //TODO readMultipleChars support
                    int r;
                    for(int i=0;i<len;i++){
                        r=read();
                        if(r==-1){
                            return i==0?-1:i;
                        }
                        cbuf[i+off]=(char)r;
                    }
                    return len;
                }

                //TODO? mark/reset

                @Override
                public void close() throws IOException {
                    BitRandomAccessFile.this.close();
                }
            };
        }
        return readerCache;
    }
    public Writer writer(){
        if (writerCache == null) {
            writerCache =new Writer(){
                int surrogateCache=-1;
                @Override
                public void write(int c) throws IOException {
                    if(surrogateCache==-1){
                        if(Character.isHighSurrogate((char)c)){
                            surrogateCache=c&0xffff;
                        }else{
                            writeUTF8(c&0xffff);
                        }
                    }else{
                        if(Character.isLowSurrogate((char) c)){
                            int cp=Character.toCodePoint((char)surrogateCache,(char)c);
                            writeUTF8(cp);
                            surrogateCache=-1;
                        }
                    }
                }

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    byte[] bytes=new String(cbuf,off,len).getBytes(StandardCharsets.UTF_8);
                    //TODO writeMultipleBytes
                    for(byte b:bytes)
                        writeByte(b&0xff);
                }

                @Override
                public void flush() throws IOException {
                    writeChanges();
                }

                @Override
                public void close() throws IOException {
                    BitRandomAccessFile.this.close();
                }
            };
        }
        return writerCache;
    }


    @Override
    public void close() throws IOException {
        try {
            writeChanges();
        }finally{
            byteFile.close();
        }
    }

    /**path of this streams source-File*/
    @Override
    public String getSourceId() {
        return path;
    }
}
