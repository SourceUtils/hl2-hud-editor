package com.timepath.tf2.hudedit.loaders;

import com.timepath.tf2.hudedit.util.DataUtils;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import javax.swing.JFrame;

/**
 *
 * @author TimePath
 */
public class CaptionLoader {
    
    private static final Logger logger = Logger.getLogger(CaptionLoader.class.getName());
    
    public static void main(String... args) {
        makeGUI();
        new CaptionLoader().load("./res/closecaption_english.dat");
        logger.info(takeCRC32("Spy.ThanksForTheTeleporter01"));
    }
    
    public static void makeGUI() {
        JFrame f = new JFrame();
        
        f.setVisible(true);
    }
    
    public static String takeCRC32(String in) {
        CRC32 crc = new CRC32();
        crc.update(in.toLowerCase().getBytes());
        return Long.toHexString(crc.getValue()).toUpperCase();
    }
    
    private static String captionId = "VCCD";
    private static int captionVer = 1;
    
    /**
     * Used for writing captions
     * @param curr
     * @param round
     * @return 
     */
    private static long alignValue(long curr, int round) {
        return (long) (Math.ceil(curr/round) * round);
    }
    
    /**
     * Entries are stored alphabetically by original value of hash
     */
    private class Entry {
        
        Entry() {
            
        }
        
        Entry(int hash, int block, int offset, int length) {
            this.hash = hash;
            this.block = block;
            this.offset = offset;
            this.length = length;
        }
        
        int hash; // 1760959566 = 68F61C4E, "Hit it doc." = A977D537. Take string, copy into new string of length n+1, lowercase, crc
        int block;
        int offset;
        int length;
        
        @Override
        public String toString() {
            return new StringBuilder().append("H: ").append(Integer.toHexString(hash).toUpperCase()).append(", b: ").append(block).append(", o: ").append(offset).append(", l: ").append(length).toString();
        }
        
    }

    public ArrayList<String> load(String file) {
        if(file == null) {
            return null;
        }
        ArrayList<String> s = new ArrayList<String>();
        try {
            RandomAccessFile rf = new RandomAccessFile(file, "r");
            String magic = new String(new byte[] {DataUtils.readChar(rf), DataUtils.readChar(rf), DataUtils.readChar(rf),DataUtils.readChar(rf)});
            int ver = DataUtils.readLEInt(rf);
            int blocks = DataUtils.readLEInt(rf);
            int blockSize = DataUtils.readLEInt(rf);
            int directorySize = DataUtils.readLEInt(rf);
            int dataOffset = DataUtils.readLEInt(rf);
            logger.log(Level.INFO, "Header: {0}, Version: {1}, Blocks: {2}, BlockSize: {3}, DirectorySize: {4}, DataOffset: {5}", new Object[]{magic, ver, blocks, blockSize, directorySize, dataOffset}); // blocks = currblock + 1 (from writing)
            Entry[] entries = new Entry[directorySize];
            for(int i = 0; i < directorySize; i++) {
                entries[i] = new Entry(DataUtils.readLEInt(rf), DataUtils.readLEInt(rf), DataUtils.readUShort(rf), DataUtils.readUShort(rf));
            }
            rf.seek(dataOffset); // trustable, otherwise do: rf.seek(rf.getFilePointer() + (alignValue(rf.getFilePointer(), 512) - rf.getFilePointer()));
            for(int i = 0; i < directorySize; i++) {
                rf.seek(dataOffset + (entries[i].block * blockSize) + entries[i].offset);
                StringBuilder sb = new StringBuilder(entries[i].length / 2);
                for(int x = 0; x + 1 < (entries[i].length / 2); x++) {
                    sb.append(DataUtils.readUTFChar(rf));
                }
                rf.skipBytes(2); // 0 padding
                logger.log(Level.INFO, "{0} = {1}", new Object[]{entries[i], sb.toString()});
                s.add(Integer.toHexString(entries[i].hash).toUpperCase());
                s.add(sb.toString());
            }
            rf.close(); // The rest of the file is garbage
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return s;
    }
    
}