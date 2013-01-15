package com.timepath.tf2.hudeditor.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class DataUtils {
    
    public static byte readByte(RandomAccessFile f) throws IOException {
        byte b = f.readByte();
        return b;
    }
    
    public static int readUByte(RandomAccessFile f) throws IOException {
        return readByte(f) & 0xff;
    }
    
    public static byte readChar(RandomAccessFile f) throws IOException {
        return readByte(f);
    }
    
    public static int readUChar(RandomAccessFile f) throws IOException {
        return readUByte(f);
    }
    
    public static char readUTFChar(RandomAccessFile f) throws IOException {
        return (char)(readByte(f) + (readByte(f) << 8));
    }
    
    /**
     * aka DWORD
     * @param f
     * @return
     * @throws IOException
     */
    public static int readLEInt(RandomAccessFile f) throws IOException {
        return readUByte(f) + (readUByte(f) << 8) + (readUByte(f) << 16) + (readUByte(f) << 24);
    }
    
    public static void writeLEInt(RandomAccessFile f, int value) throws IOException {
//        ByteBuffer buffer = ByteBuffer.allocate(1000);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//        buffer.putInt(i);
//        f.write(buffer.array());
        f.writeByte(value & 0xFF);
        f.writeByte((value >> 8) & 0xFF);
        f.writeByte((value >> 16) & 0xFF);
        f.writeByte((value >> 24) & 0xFF);
    }
    
    public static float readFloat(RandomAccessFile f) throws IOException {
        int intBits = readUByte(f) + (readUByte(f) << 8) + (readUByte(f) << 16) + (readUByte(f) << 24);
        return Float.intBitsToFloat(intBits);
    }
    
    /**
     * aka WORD
     * @param f
     * @return
     * @throws IOException 
     */
    public static int readUShort(RandomAccessFile f) throws IOException {
        return readUByte(f) + (readUByte(f) << 8);
    }
    
    public static void writeUShort(RandomAccessFile f, short value) throws IOException {
        f.writeByte(value & 0xFF);
        f.writeByte((value >> 8) & 0xFF);
    }
    
    /**
     * aka DWORD
     * @param f
     * @return
     * @throws IOException 
     */
    public static int readULong(RandomAccessFile f) throws IOException {
        return readUByte(f) + (readUByte(f) << 8) + (readUByte(f) << 16) + (readUByte(f) << 24);
    }
    
    public static void writeULong(RandomAccessFile f, int value) throws IOException {
        writeLEInt(f, value);
    }
    
    public static String toBinaryString(short n) {
        StringBuilder sb = new StringBuilder("0000000000000000");
        for (int bit = 0; bit < 16; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(15 - bit, '1');
            }
        }
        return sb.toString();
    }
    private static final Logger LOG = Logger.getLogger(DataUtils.class.getName());
    
}
