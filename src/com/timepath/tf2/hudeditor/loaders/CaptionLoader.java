package com.timepath.tf2.hudeditor.loaders;

import com.timepath.tf2.hudeditor.gui.EditorFrame;
import com.timepath.tf2.hudeditor.util.DataUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

/**
 *
 * @author TimePath
 */
public class CaptionLoader {
    
    private static final Logger logger = Logger.getLogger(CaptionLoader.class.getName());

    public static String takeCRC32(String in) {
        CRC32 crc = new CRC32();
        crc.update(in.toLowerCase().getBytes());
        return Integer.toHexString((int)crc.getValue()).toUpperCase();
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
        
        int hash;
        int block;
        int offset;
        int length;
        
        @Override
        public String toString() {
            return new StringBuilder().append("H: ").append(hash).append(", b: ").append(block).append(", o: ").append(offset).append(", l: ").append(length).toString();
        }
        
    }
    
    public CaptionLoader() {
        generateHash();
    }

    private HashMap<Integer, String> hashmap = new HashMap<Integer, String>();
    
    private void generateHash() {
        logger.info("Generating hash codes ...");
        try {
            GcfFile gcf = GcfFile.load(new File(EditorFrame.locateSteamAppsDirectory() + "team fortress 2 content.gcf"));
        
            CRC32 crc = new CRC32();

            String all = new String(gcf.ls);
            String[] ls = all.split("\0");
            for(int i = 0; i < ls.length; i++) {
                int end = ls[i].length();
                int ext = ls[i].lastIndexOf(".");
                if(ext != -1) {
                    end = ext;
                }
                String sp = ls[i].substring(0, end);
                if(ls[i].toLowerCase().endsWith(".wav") || ls[i].toLowerCase().endsWith(".mp3")// || 
    //                    ls[i].toLowerCase().endsWith(".vcd") || ls[i].toLowerCase().endsWith(".bsp") || 
    //                    ls[i].toLowerCase().endsWith(".mp3") || ls[i].toLowerCase().endsWith(".bat") ||
    //                    ls[i].toLowerCase().endsWith(".doc") || ls[i].toLowerCase().endsWith(".raw") ||
    //                    ls[i].toLowerCase().endsWith(".pcf") || ls[i].toLowerCase().endsWith(".cfg") ||
    //                    ls[i].toLowerCase().endsWith(".vbsp") || ls[i].toLowerCase().endsWith(".inf") ||
    //                    ls[i].toLowerCase().endsWith(".rad") || ls[i].toLowerCase().endsWith(".vdf") ||
    //                    ls[i].toLowerCase().endsWith(".ctx") || ls[i].toLowerCase().endsWith(".vdf") ||
    //                    ls[i].toLowerCase().endsWith(".lst") || ls[i].toLowerCase().endsWith(".res") ||
    //                    ls[i].toLowerCase().endsWith(".pop") || ls[i].toLowerCase().endsWith(".dll") ||
    //                    ls[i].toLowerCase().endsWith(".dylib") || ls[i].toLowerCase().endsWith(".so") ||
    //                    ls[i].toLowerCase().endsWith(".scr") || ls[i].toLowerCase().endsWith(".rc") ||
    //                    ls[i].toLowerCase().endsWith(".vfe") || ls[i].toLowerCase().endsWith(".pre") ||
    //                    ls[i].toLowerCase().endsWith(".cache") || ls[i].toLowerCase().endsWith(".nav") ||
    //                    ls[i].toLowerCase().endsWith(".lmp") || ls[i].toLowerCase().endsWith(".bik") ||
    //                    ls[i].toLowerCase().endsWith(".mov") || ls[i].toLowerCase().endsWith(".snd") ||
    //                    ls[i].toLowerCase().endsWith(".midi") || ls[i].toLowerCase().endsWith(".png") ||
    //                    ls[i].toLowerCase().endsWith(".ttf") || ls[i].toLowerCase().endsWith(".ico") ||
    //                    ls[i].toLowerCase().endsWith(".dat") || ls[i].toLowerCase().endsWith(".pl") ||
    //                    ls[i].toLowerCase().endsWith(".ain") || ls[i].toLowerCase().endsWith(".db") ||
    //                    ls[i].toLowerCase().endsWith(".py") || ls[i].toLowerCase().endsWith(".xsc") ||
    //                    ls[i].toLowerCase().endsWith(".bmp") || ls[i].toLowerCase().endsWith(".icns") ||
    //                    ls[i].toLowerCase().endsWith(".txt") || ls[i].toLowerCase().endsWith(".manifest")
                     ) {
                    String str = sp;
                    if(str.split("_").length == 2) {
                        str = str.replaceAll("_", ".").replaceAll(" ", "");// + "\0";
                    }
                    System.out.println(str);
                    crc.update(str.toLowerCase().getBytes());
                    hashmap.put((int)crc.getValue(), str); // HASH >
//                    logger.log(Level.INFO, "{0} > {1}", new Object[]{crc.getValue(), str});
                    crc.reset();
                } else {
    //                logger.info(ls[i]);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CaptionLoader.class.getName()).log(Level.WARNING, "Error generating hash codes", ex);
        }
    }
    
    private String attemptDecode(int hash) {
        if(hashmap.containsKey(hash)) {
            return hashmap.get(hash);
        } else {
//            logger.log(Level.INFO, "hashmap does not contain {0}", hash);
            return Integer.toHexString(hash).toUpperCase();
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
//            logger.log(Level.INFO, "Header: {0}, Version: {1}, Blocks: {2}, BlockSize: {3}, DirectorySize: {4}, DataOffset: {5}", new Object[]{magic, ver, blocks, blockSize, directorySize, dataOffset}); // blocks = currblock + 1 (from writing)
            Entry[] entries = new Entry[directorySize];
            for(int i = 0; i < directorySize; i++) {
                entries[i] = new Entry(DataUtils.readULong(rf), DataUtils.readLEInt(rf), DataUtils.readUShort(rf), DataUtils.readUShort(rf));
            }
            rf.seek(dataOffset); // trustable, otherwise do: rf.seek(rf.getFilePointer() + (alignValue(rf.getFilePointer(), 512) - rf.getFilePointer()));
            for(int i = 0; i < directorySize; i++) {
                rf.seek(dataOffset + (entries[i].block * blockSize) + entries[i].offset);
                StringBuilder sb = new StringBuilder(entries[i].length / 2);
                for(int x = 0; x + 1 < (entries[i].length / 2); x++) {
                    sb.append(DataUtils.readUTFChar(rf));
                }
                rf.skipBytes(2); // \0\0 padding
//                logger.log(Level.INFO, "{0} = {1}", new Object[]{entries[i], sb.toString()});
                s.add(attemptDecode(entries[i].hash));
                s.add(sb.toString());
            }
            rf.close(); // The rest of the file is garbage
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return s;
    }
    
}