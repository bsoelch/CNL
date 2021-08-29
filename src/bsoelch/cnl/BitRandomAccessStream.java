package bsoelch.cnl;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

//TODO separate interfaces for read and write?
public interface BitRandomAccessStream extends Closeable {
    /**goes to the given bit-position*/
    void seek(long newBitPos) throws IOException;

    /**current bit-position in this stream*/
    long bitPos();

    /**Length of this stream in bytes*/
    long byteLength() throws IOException;

    /**truncates the file to the current file pointer fills the remaining space of the last byte with zeros or ones*/
    void truncateToSize(boolean byteFillOne) throws IOException;

    /**reads one bit from the stream
     * @return 0,1 value of the bit or -1 if the end of file is reached*/
    byte readBit() throws IOException;

    /**writes one bit the this stream*/
    void writeBit(boolean isSet) throws IOException;

    /**reads one byte from the stream
     * @return 0-255 value of the byte or -1 if the end of file is reached while reading the byte*/
    default int readByte() throws IOException {
        long[] cache=new long[1];
        try {
            readFully(cache, 0, 8);
            return (int)(cache[0]&0xff);
        }catch (EOFException eof){
            return -1;
        }
    }
    /**writes one byte to the stream
     * @param aByte byte to write only the lower eight bits are used*/
    default void writeByte(int aByte) throws IOException {
        long[] cache=new long[]{aByte};
        write(cache,0,8);
    }

    /**reads one UTF-8 CodePoint*/
    default int readUTF8() throws IOException {
        int cp=readByte();
        if(cp==-1)
            return -1;
        if((cp&0x80)==0){
            return cp;
        }else {
            int count=0,mask=0x40;
            while((cp&mask)!=0){
                count++;
                mask>>>=1;
            }
            byte[] bytes=new byte[count+1];
            bytes[0]= (byte) cp;
            for(int i=1;i<bytes.length;i++){//TODO readBytes Methode
                cp=readByte();
                if(cp==-1)
                    return -1;
                bytes[i]=(byte)cp;
            }
            return new String(bytes).codePointAt(0);
        }
    }
    default void writeUTF8(int cp) throws IOException {
        byte[] bytes=String.valueOf(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8);
        for (byte aByte : bytes) {//TODO writeBytes Methode
            writeByte(aByte);
        }
    }

    /**writes all cached changes*/
    void writeChanges() throws IOException;

    /** reads up to len bits and stores them in the long-array starting at bit-index off
     *  @param target long[] in which the bit-data is stored (small-endian)
     *  @param off stating bit-index in the array
     *  @param len maximal number of bits read
     *  @return total number of bits read or -1 for end of file
     *  @see #readFully(long[], long, long) */ //TODO? check if endian is correct
    long read(long[] target, long off, long len) throws IOException;

    /** reads exactly len bits and stores them in the long-array starting at bit-index off
     *  @param target long[] in which the bit-data is stored (small-endian)
     *  @param off stating bit-index in the array
     *  @param len maximal number of bits read
     *  @throws java.io.EOFException if the file ends
     *  @see #read(long[], long, long) */
    void readFully(long[] target, long off, long len) throws IOException;

    /** writes len bits from the source-array to the stream
     *  @param source long[] in which the bit-data is stored (little-endian)
     *  @param off stating bit-index in the array
     *  @param len number of bits to write*/
    void write(long[] source, long off, long len) throws IOException;

    /**reads count bits and returns the result as an unsigned integer
     * @param count number of bits to read
     * @return an unsigned BigInteger containing the read bits (little-endian)*/
    BigInteger readBits(int count) throws IOException;

    /**writes count bits from data to the stream
     * @param data an unsigned BigInteger containing the bit-data
     * @param count number of bits to write*/
    void writeBits(BigInteger data, int count) throws IOException;

    /**reads a BigInteger from this file, going by the following algorithm:
     * <pre>
     * {@code
     *  header=readBits(headerLength)
     *  if(header<(1<<headerLength-1)){
     *      return header
     *  }else{
     *      len=header^(1<<headerLength-1)
     *      off=1<<headerLength-1
     *      blockSize=1<<blockLength
     *      if(len==0){
     *          blockOff=((off-1)*blockLength-1)/bigBlockLength+1
     *          block=readBits((readBigInt(headerLength,blockLength,bigBlockLength)+blockOff)*bigBlockLength)
     *          bigBlockSize=1<<bigBlockLength
     *          return block+off+((pow(blockSize,off)-1)/(blockSize-1)-1+
     *          (((pow(bigBlockSize,len)-1)/(bigBlockSize-1))-(((pow(bigBlockSize,blockOff)-1)/(bigBlockSize-1))
     *      }else{
     *          block=readBits(len*blockLength)
     *          return block+off+(((pow(blockSize,len)-1)/(blockSize-1))-1
     *      }
     *  }
     * }
     * </pre>
     * For instance the first Numbers in (4,8,16) encoding are (with the lowest bit written on the left):
     * <li>0->0000 </li>
     * <li>1->1000 </li>
     * <li>...</li>
     * <li>7->1110 </li>
     * <li>8->1001 00000000 </li>
     * <li>9->1001 10000000 </li>
     * <li>...</li>
     * <lI>263->1001 11111111 </lI>
     * <lI>264->0101 00000000 00000000 </lI>
     * <li>...</li>
     * <lI>?->1111 00000000 00000000 00000000 00000000 00000000 00000000 00000000 </lI>
     * <li>...</li>
     * <lI>?->1111 11111111 11111111 11111111 11111111 11111111 11111111 11111111 </lI>
     * <lI>?->0001 0000 0000000000000000 0000000000000000 0000000000000000 0000000000000000 </lI>
     *
     * @param headerLength length of the BigInt-header has to be at least 2
     * @param blockLength length of the BigInt-blocks has to be at least 2
     * @param bigBlockLength length of the blocks in BigIntegers with recursive headers has to be >= blockLength
     *
     * @return the BigInteger that was read form the File
     * */
    BigInteger readBigInt(int headerLength, int blockLength, int bigBlockLength) throws IOException;

    /** Writes a BigInteger to this stream that can be read with the algorithm described in {@link #readBigInt(int, int, int)}
     * @param value value to write to this file
     * @param headerLength then of the BigInt-header
     * @param blockLength then of the BigInt-blocks
     * @param bigBlockLength then of the blocks in BigIntegers with recursive headers
     */
    void writeBigInt(BigInteger value, int headerLength, int blockLength, int bigBlockLength) throws IOException;

    /**closes this Stream*/
    void close() throws IOException;

    /**Unique identifier of this streams source*/
    String getSourceId();


    /**returns a UTF-8 Reader that is backed up by this stream*/
    Reader reader();
    /**returns a UTF-8 Writer that is backed up by this stream*/
    Writer writer();

}
