package com.timepath.tf2.hudeditor.loaders;

import com.timepath.tf2.hudeditor.Utils;
import com.timepath.tf2.hudeditor.util.DataUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class GcfFile {
    
    private static final Logger logger = Logger.getLogger(GcfFile.class.getName());
    
    private final RandomAccessFile rf;
    
    private final String name;
    
    //<editor-fold defaultstate="collapsed" desc="Header info">
    //<editor-fold defaultstate="collapsed" desc="Header">
    GcfHeader header;
    
    class GcfHeader {
        
        int dummy0;     // Always 0x00000001
        int dummy1;     // Always 0x00000001
        int gcfVersion; // TF2 is 0x00000006
        int CacheId;
        int gcfRevision;
        int dummy3;
        int dummy4;
        int fileSize;	// Total size of GCF file in bytes.
        int blockSize;  // Size of each data block in bytes.
        int blockCount; // Number of data blocks.
        int dummy5;
        
        static final int size = 11 * 4;
        
        GcfHeader() throws IOException {
            dummy0 = DataUtils.readLEInt(rf);
            dummy1 = DataUtils.readLEInt(rf);
            gcfVersion = DataUtils.readLEInt(rf);
            CacheId = DataUtils.readLEInt(rf);
            gcfRevision = DataUtils.readLEInt(rf);
            dummy3 = DataUtils.readLEInt(rf);
            dummy4 = DataUtils.readLEInt(rf);
            fileSize = DataUtils.readLEInt(rf);
            blockSize = DataUtils.readLEInt(rf);
            blockCount = DataUtils.readLEInt(rf);
            dummy5 = DataUtils.readLEInt(rf);
        }
        
        @Override
        public String toString() {
            return "id:" + CacheId + ", ver:" + gcfVersion + ", rev:" + gcfRevision + ", size:" + fileSize + ", blockSize:" + blockSize + ", blocks:" + blockCount;
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Block">
    BlockHeader blockHeader;
    
    class BlockHeader {
        
        int blockCount;	// Number of data blocks.
        int blocksUsed;	// Number of data blocks that point to data.
        int dummy0;
        int dummy1;
        int dummy2;
        int dummy3;
        int dummy4;
        int checksum;   // Header checksum. The checksum is simply the sum total of all the preceeding DWORDs in the header.
        
        static final int size = 8 * 4;
        
        final long pos;
        
        BlockHeader() throws IOException {
            this.pos = rf.getFilePointer();
            blockCount = DataUtils.readLEInt(rf);
            blocksUsed = DataUtils.readLEInt(rf);
            dummy0 = DataUtils.readLEInt(rf);
            dummy1 = DataUtils.readLEInt(rf);
            dummy2 = DataUtils.readLEInt(rf);
            dummy3 = DataUtils.readLEInt(rf);
            dummy4 = DataUtils.readLEInt(rf);
            checksum = DataUtils.readLEInt(rf);
            
            blocks = new Block[blockCount];
            rf.skipBytes(blockCount * Block.size);
        }
        
        @Override
        public String toString() {
            return "blockCount:" + blockCount + ", blocksUsed:" + blocksUsed + ", check:" + checksum + " vs " + (blockCount + blocksUsed + dummy0 + dummy1 + dummy2 + dummy3 + dummy4);
        }
    }
    
    Block[] blocks;
    
    class Block {
        
        static final int size = 7 * 4;
        
        int entryType;                  // Flags for the block entry.  0x200F0000 == Not used.
        int fileDataOffset;		// The offset for the data contained in this block entry in the file.
        int fileDataSize;		// The length of the data in this block entry.
        int firstDataBlockIndex;	// The index to the first data block of this block entry's data.
        int nextBlockEntryIndex;	// The next block entry in the series.  (N/A if == BlockCount.)
        int previousBlockEntryIndex;	// The previous block entry in the series.  (N/A if == BlockCount.)
        int directoryIndex;		// The index of the block entry in the directory.
        
        private final RandomAccessFile rf;
        
        Block(RandomAccessFile rf) {
            this.rf = rf;
        }
        
        Block read() throws IOException {
            entryType = DataUtils.readLEInt(rf);
            fileDataOffset = DataUtils.readLEInt(rf);
            fileDataSize = DataUtils.readLEInt(rf);
            firstDataBlockIndex = DataUtils.readLEInt(rf);
            nextBlockEntryIndex = DataUtils.readLEInt(rf);
            previousBlockEntryIndex = DataUtils.readLEInt(rf);
            directoryIndex = DataUtils.readLEInt(rf);
            return this;
        }
        
        @Override
        public String toString() {
            return "type:" + entryType + ", off:" + fileDataOffset + ", size:" + fileDataSize + ", firstidx:" + firstDataBlockIndex + ", nextidx:" + nextBlockEntryIndex + ", previdx:" + previousBlockEntryIndex + ", di:" + directoryIndex;
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Frag Map">
    private FragMapHeader fragMap;
    
    private class FragMapHeader {
        
        static final int size = 4 * 4;
        
        int blockCount;	// Number of data blocks.
        int dummy0;	// index of 1st unused GCFFRAGMAP entry?
        int dummy1;
        int checksum;   // Header checksum.
        
        private long pos;
        
        FragMapHeader() throws IOException {
            this.pos = rf.getFilePointer();
            blockCount = DataUtils.readLEInt(rf);
            dummy0 = DataUtils.readLEInt(rf);
            dummy1 = DataUtils.readLEInt(rf);
            checksum = DataUtils.readLEInt(rf);
            fragMapEntries = new FragMapEntry[blockCount];
            rf.skipBytes(blockCount * FragMapEntry.size);
        }
        
        FragMapEntry getEntry(int i) throws IOException {
            if(fragMapEntries[i] != null) {
                return fragMapEntries[i];
            }
            rf.seek(pos + size + (i * FragMapEntry.size));
            FragMapEntry fe = new FragMapEntry();
            fragMapEntries[i] = fe;
            return fe;
        }
        
        @Override
        public String toString() {
            return "blockCount:" + blockCount + ", dummy0:" + dummy0 + ", dummy1:" + dummy1 + ", checksum:" + checksum + " vs " + (blockCount + dummy0 + dummy1);
        }
        
    }
    
    FragMapEntry[] fragMapEntries;
    
    class FragMapEntry {
        
        static final int size = 1 * 4;
        
        int nextDataBlockIndex;	// The index of the next data block.
        
        FragMapEntry() throws IOException {
            nextDataBlockIndex = DataUtils.readLEInt(rf);
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Directories">
    private DirectoryHeader directoryHeader;
    
    private class DirectoryHeader {
        int dummy0;		// Always 0x00000004
        int cacheId;		// Cache ID.
        int gcfRevision;        // GCF file version.
        int itemCount;          // Number of items in the directory.
        int fileCount;          // Number of files in the directory.
        int dummy1;		// Always 0x00008000
        int directorySize;	// Size of lpGCFDirectoryEntries & lpGCFDirectoryNames & lpGCFDirectoryInfo1Entries & lpGCFDirectoryInfo2Entries & lpGCFDirectoryCopyEntries & lpGCFDirectoryLocalEntries in bytes.
        int nameSize;		// Size of the directory names in bytes.
        int info1Count;         // Number of Info1 entires.
        int copyCount;          // Number of files to copy.
        int localCount;         // Number of files to keep local.
        int dummy2;
        int dummy3;
        int checksum;		// Header checksum. How is this calculated?
        
        DirectoryHeader() throws IOException {
            dummy0 = DataUtils.readLEInt(rf);
            cacheId = DataUtils.readLEInt(rf);
            gcfRevision = DataUtils.readLEInt(rf);
            itemCount = DataUtils.readLEInt(rf);
            fileCount = DataUtils.readLEInt(rf);
            dummy1 = DataUtils.readLEInt(rf);
            directorySize = DataUtils.readLEInt(rf);
            nameSize = DataUtils.readLEInt(rf);
            info1Count = DataUtils.readLEInt(rf);
            copyCount = DataUtils.readLEInt(rf);
            localCount = DataUtils.readLEInt(rf);
            dummy2 = DataUtils.readLEInt(rf);
            dummy3 = DataUtils.readLEInt(rf);
            checksum = DataUtils.readLEInt(rf);
        }
        
        @Override
        public String toString() {
            int checked = dummy0 + cacheId + gcfRevision + itemCount + fileCount + dummy1 + directorySize + nameSize + info1Count + copyCount + localCount + dummy2 + dummy3;
            return "id:" + cacheId + ", ver:" + gcfRevision + ", items:" + itemCount + ", files:" + fileCount + ", dsize:" + directorySize + ", nsize:" + nameSize + ", info1:" + info1Count + ", copy:" + copyCount + ", local:" + localCount + ", check:" + checksum + " vs " + checked;
        }
    }
    
    DirectoryEntry[] directoryEntries;
    
    class DirectoryEntry {
        int nameOffset;         // Offset to the directory item name from the end of the directory items.
        int itemSize;		// Size of the item.  (If file, file size.  If folder, num items.)
        int checksumIndex;	// Checksum index. (0xFFFFFFFF == None).
        int directoryType;	// Flags for the directory item.  (0x00000000 == Folder).
        int parentIndex;	// Index of the parent directory item.  (0xFFFFFFFF == None).
        int nextIndex;          // Index of the next directory item.  (0x00000000 == None).
        int firstIndex;         // Index of the first directory item.  (0x00000000 == None).
        
        @Override
        public String toString() {
            return nameOffset + ", " + itemSize + ", " + checksumIndex + ", " + directoryType + ", " + parentIndex + ", " + nextIndex + ", " + firstIndex;
        }
    }
    
    tagGCFDIRECTORYINFO1ENTRY[] info1Entries;
    
    //GCF Directory Info 1 Entry
    class tagGCFDIRECTORYINFO1ENTRY {
        int Dummy0;
        
        @Override
        public String toString() {
            return "" + (char) Dummy0;
        }
    }
    
    tagGCFDIRECTORYINFO2ENTRY[] info2Entries;
    
    //GCF Directory Info 2 Entry
    class tagGCFDIRECTORYINFO2ENTRY {
        int Dummy0;
    }
    
    tagGCFDIRECTORYCOPYENTRY[] copyEntries;
    
    //GCF Directory Copy Entry
    class tagGCFDIRECTORYCOPYENTRY {
        int DirectoryIndex;	// Index of the directory item.
    }
    
    tagGCFDIRECTORYLOCALENTRY[] localEntries;
    
    //GCF Directory Local Entry
    class tagGCFDIRECTORYLOCALENTRY {
        int DirectoryIndex;	// Index of the directory item.
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Directory Map">
    DirectoryMapHeader directoryMapHeader;
    
    //GCF Directory Map Header
    class DirectoryMapHeader {
        int Dummy0;     // Always 0x00000001
        int Dummy1;     // Always 0x00000000
        
        DirectoryMapHeader() throws IOException {
            Dummy0 = DataUtils.readLEInt(rf);
            Dummy1 = DataUtils.readLEInt(rf);
        }
        
        @Override
        public String toString() {
            return "Dummy0:" + Dummy0 + ", Dummy1:" + Dummy1;
        }
    }
    
    tagGCFDIRECTORYMAPENTRY[] directoryMapEntries;
    
    //GCF Directory Map Entry
    class tagGCFDIRECTORYMAPENTRY {
        int FirstBlockIndex;    // Index of the first data block. (N/A if == BlockCount.)
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Checksum Header">
    ChecksumHeader checksumHeader;
    
    //GCF Checksum Header
    class ChecksumHeader {
        int Dummy0;			// Always 0x00000001
        int ChecksumSize;		// Size of LPGCFCHECKSUMHEADER & LPGCFCHECKSUMMAPHEADER & in bytes.
        
        ChecksumHeader() throws IOException {
            Dummy0 = DataUtils.readLEInt(rf);
            ChecksumSize = DataUtils.readLEInt(rf);
        }
        
        @Override
        public String toString() {
            return "Dummy0:" + Dummy0 + ", ChecksumSize:" + ChecksumSize;
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Checksum map">
    ChecksumMapHeader checksumMapHeader;
    
    //GCF Checksum Map Header
    class ChecksumMapHeader {
        int Dummy0;			// Always 0x14893721
        int Dummy1;			// Always 0x00000001
        int ItemCount;		// Number of items.
        int ChecksumCount;		// Number of checksums.
    }
    
    ChecksumMapEntry[] checksumMapEntries;
        
        //GCF Checksum Map Entry
        class ChecksumMapEntry {
            int checksumCount;		// Number of checksums.
            int firstChecksumIndex;	// Index of first checksum.
            
            @Override
            public String toString() {
                return "checkCount:" + checksumCount + ", first:" + firstChecksumIndex;
            }
        }
    
    ChecksumEntry[] checksumEntries;
    
    //GCF Checksum Entry
    class ChecksumEntry {
        int checksum;				// Checksum.
        
        @Override
        public String toString() {
            return "check:" + checksum;
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Data blocks">
    DateBlockHeader dataBlockHeader;
    
    //GCF Data Header
    class DateBlockHeader {
        int gcfRevision;	// GCF file version.
        int blockCount;	// Number of data blocks.
        int blockSize;	// Size of each data block in bytes.
        int firstBlockOffset; // Offset to first data block.
        int blocksUsed;	// Number of data blocks that contain data.
        int checksum;		// Header checksum.
        
        @Override
        public String toString() {
            int check = blockCount + blockSize + firstBlockOffset + blocksUsed;
            return "v:" + gcfRevision + ", blocks:" + blockCount + ", size:" + blockSize + ", ofset:0x" + Integer.toHexString(firstBlockOffset) + ", used:" + blocksUsed + ", check:" + checksum + " vs " + check;
        }
    }
    //</editor-fold>
    //</editor-fold>
    
    Block getBlock(int i) {
        Block b = blocks[i];
        if(b == null) {
            try {
                long orig = rf.getFilePointer();
                rf.seek((blockHeader.pos + BlockHeader.size) + (i * Block.size));
                b = new Block(rf).read();
                rf.seek(orig);
                blocks[i] = b;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return b;
    }
    
    public byte[] ls = null;
    
    public GcfFile(File file) throws IOException {
        this.name = file.getName();
        rf = new RandomAccessFile(file, "r");
        
        header = new GcfHeader();        
        blockHeader = new BlockHeader();
        fragMap = new FragMapHeader();

        directoryHeader = new DirectoryHeader();
        logger.info(directoryHeader.toString());
        //<editor-fold defaultstate="collapsed" desc="Directories">
        boolean skipDirs = false;
        if(skipDirs) {
            rf.skipBytes(directoryHeader.directorySize - (14 * 4));
        } else {
            directoryEntries = new DirectoryEntry[directoryHeader.itemCount];
            for(int i = 0; i < directoryHeader.itemCount; i++) {
//                if(i % 10000 == 0) {
//                    logger.log(Level.INFO, "Loading entry {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
//                }
                DirectoryEntry de = new DirectoryEntry();
                de.nameOffset = DataUtils.readLEInt(rf);
                de.itemSize = DataUtils.readLEInt(rf);
                de.checksumIndex = DataUtils.readLEInt(rf);
                de.directoryType = DataUtils.readLEInt(rf);
                de.parentIndex = DataUtils.readLEInt(rf);
                de.nextIndex = DataUtils.readLEInt(rf);
                de.firstIndex = DataUtils.readLEInt(rf);
                
                directoryEntries[i] = de;
            }
            
//            logger.log(Level.INFO, "Loading names ({0}) bytes", directoryHeader.nameSize);
            ls = new byte[directoryHeader.nameSize];
            rf.read(ls);
            
            info1Entries = new tagGCFDIRECTORYINFO1ENTRY[directoryHeader.info1Count];
            for(int i = 0; i < directoryHeader.info1Count; i++) {
//                if(i % 10000 == 0) {
//                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.info1Count});
//                }
                tagGCFDIRECTORYINFO1ENTRY f = new tagGCFDIRECTORYINFO1ENTRY();
                f.Dummy0 = DataUtils.readLEInt(rf);
                
                info1Entries[i] = f;
            }
            
            info2Entries = new tagGCFDIRECTORYINFO2ENTRY[directoryHeader.itemCount];
            for(int i = 0; i < directoryHeader.itemCount; i++) {
//                if(i % 10000 == 0) {
//                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
//                }
                tagGCFDIRECTORYINFO2ENTRY f = new tagGCFDIRECTORYINFO2ENTRY();
                f.Dummy0 = DataUtils.readLEInt(rf);
                
                info2Entries[i] = f;
            }
            
            copyEntries = new tagGCFDIRECTORYCOPYENTRY[directoryHeader.copyCount];
            for(int i = 0; i < directoryHeader.copyCount; i++) {
//                if(i % 10000 == 0) {
//                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.copyCount});
//                }
                tagGCFDIRECTORYCOPYENTRY f = new tagGCFDIRECTORYCOPYENTRY();
                f.DirectoryIndex = DataUtils.readLEInt(rf);
                
                copyEntries[i] = f;
            }
            
            localEntries = new tagGCFDIRECTORYLOCALENTRY[directoryHeader.localCount];
            for(int i = 0; i < directoryHeader.localCount; i++) {
//                if(i % 10000 == 0) {
//                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.localCount});
//                }
                tagGCFDIRECTORYLOCALENTRY f = new tagGCFDIRECTORYLOCALENTRY();
                f.DirectoryIndex = DataUtils.readLEInt(rf);
                
                localEntries[i] = f;
            }
        }
        
        //</editor-fold>
        

        directoryMapHeader = new GcfFile.DirectoryMapHeader();
        logger.info(directoryMapHeader.toString());
        //<editor-fold defaultstate="collapsed" desc="Directory Map">
        directoryMapEntries = new tagGCFDIRECTORYMAPENTRY[directoryHeader.itemCount];
        for(int i = 0; i < directoryHeader.itemCount; i++) {
//            if(i % 10000 == 0) {
//                logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
//            }
            tagGCFDIRECTORYMAPENTRY dme = new tagGCFDIRECTORYMAPENTRY();
            dme.FirstBlockIndex = DataUtils.readLEInt(rf);

            directoryMapEntries[i] = dme;
        }
        //</editor-fold>
        
        checksumHeader = new GcfFile.ChecksumHeader();
        logger.info(checksumHeader.toString());
        //<editor-fold defaultstate="collapsed" desc="Checksums">
//        logger.log(Level.INFO, "csize:{0}", checksumHeader.ChecksumSize);
        
        checksumMapHeader = new GcfFile.ChecksumMapHeader();
        checksumMapHeader.Dummy0 = DataUtils.readLEInt(rf);
        checksumMapHeader.Dummy1 = DataUtils.readLEInt(rf);
        checksumMapHeader.ItemCount = DataUtils.readLEInt(rf);
        checksumMapHeader.ChecksumCount = DataUtils.readLEInt(rf);
//        logger.log(Level.INFO, "items:{0}, checks:{1}", new Object[]{checksumMapHeader.ItemCount, checksumMapHeader.ChecksumCount});
        
        checksumMapEntries = new ChecksumMapEntry[checksumMapHeader.ItemCount];
        for(int i = 0; i < checksumMapHeader.ItemCount; i++) {
            ChecksumMapEntry cme = new GcfFile.ChecksumMapEntry();
            cme.checksumCount = DataUtils.readLEInt(rf);
            cme.firstChecksumIndex = DataUtils.readLEInt(rf);
            checksumMapEntries[i] = cme;
        }
        
        checksumEntries = new ChecksumEntry[checksumMapHeader.ChecksumCount + 0x20];
        for(int i = 0; i < checksumEntries.length; i++) {
//            if(i % 10000 == 0) {
//                logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, checksumMapEntries.ChecksumCount});
//            }
            ChecksumEntry ce = new ChecksumEntry();
            ce.checksum = DataUtils.readLEInt(rf);
            checksumEntries[i] = ce;
        }
        //</editor-fold>

        // TODO: Slow. Takes about 73 seconds
        //<editor-fold defaultstate="collapsed" desc="Data">
        dataBlockHeader = new DateBlockHeader();
        dataBlockHeader.gcfRevision = DataUtils.readLEInt(rf);
        dataBlockHeader.blockCount = DataUtils.readLEInt(rf);
        dataBlockHeader.blockSize = DataUtils.readLEInt(rf);
        dataBlockHeader.firstBlockOffset = DataUtils.readLEInt(rf);
        dataBlockHeader.blocksUsed = DataUtils.readLEInt(rf);
        dataBlockHeader.checksum = DataUtils.readLEInt(rf);
        logger.info(dataBlockHeader.toString());
        
        boolean skipRead = true;
        if(skipRead) {
            rf.seek(dataBlockHeader.firstBlockOffset + (dataBlockHeader.blockCount * dataBlockHeader.blockSize));
        } else {
            logger.info("Loading Data ...");
            rf.seek(dataBlockHeader.firstBlockOffset);
            byte[] b = new byte[dataBlockHeader.blockSize];
            for(int i = 0; i < dataBlockHeader.blockCount; i++) {
                rf.read(b);
            }
        }
        //</editor-fold>
        
        logger.log(Level.INFO, "{0}\n{1}\n{2}", new Object[]{header.toString(), blockHeader.toString(), fragMap.toString()});
        
//        rf.close();
    }
    
    String nameForDirectoryIndex(byte[] b, int idx) {
        String str;
        if(idx != -1 && directoryEntries[idx].parentIndex != -1 && directoryEntries[idx].parentIndex != 0xFFFFFFFF) {
            str = nameForDirectoryIndex(b, directoryEntries[idx].parentIndex);
        } else {
            return "/";
        }
        int off = directoryEntries[idx].nameOffset;
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        while(b[off] != 0) {
            s.write(b[off++]);
        }
        str += "/" + new String(s.toByteArray());
        return str;
    }
    
    void extract() throws IOException {
        logger.log(Level.INFO, "Extracting {0}", name);
        File root = new File(System.getenv("HOME") + "/test");
        if(root.exists()) {
            root.renameTo(new File(System.getenv("HOME") + "/deleteme"));
        }
        root.mkdir();
        for(int i = 0; i < directoryEntries.length; i++) {
            String str = nameForDirectoryIndex(ls, i);
            if(directoryEntries[i].directoryType == 0) { // is a directory.
                File dir = new File(root.getPath(), str);
                dir.mkdirs();
            } else {                
                File f = new File(root, str);
                int idx = directoryMapEntries[i].FirstBlockIndex;
//                logger.log(Level.INFO, "\n\np:{0}\nblockidx:{1}\n", new Object[]{f.getPath(), idx});
//                logger.log(Level.INFO, "\n\nb:{0}\n", new Object[]{block.toString()});
                f.getParentFile().mkdirs();
                f.createNewFile();
                if(idx >= blocks.length) {
                    logger.log(Level.WARNING, "Block out of range for {0} : {1}. Is the size 0?", new Object[]{f.getPath(), i});
                    continue;
                }
                Block block = getBlock(idx);
                RandomAccessFile out = new RandomAccessFile(f, "rw");
                int dataIdx = block.firstDataBlockIndex;
                for(int q = 0; ; q++) {
                    long pos = ((long)dataBlockHeader.firstBlockOffset + ((long)dataIdx * (long)dataBlockHeader.blockSize));
                    rf.seek(pos);
                    byte[] buf = new byte[dataBlockHeader.blockSize];
                    if(block.fileDataOffset != 0) {
                        logger.log(Level.INFO, "off = {0}", block.fileDataOffset);
                    }
                    rf.read(buf);
                    out.seek(block.fileDataOffset + (q * dataBlockHeader.blockSize));
                    if(out.getFilePointer() + buf.length > block.fileDataSize) {
                        out.write(buf, 0, (block.fileDataSize % dataBlockHeader.blockSize));
                    } else {
                        out.write(buf);
                    }
                    if(dataIdx == 65535) {
                        break;
                    }
                    dataIdx = fragMap.getEntry(dataIdx).nextDataBlockIndex;
                    if(dataIdx == -1) {
                        break;
                    }
                }
                out.close();
            }
        }
        logger.log(Level.INFO, "Extracted {0}", name);
    }
    
    public static GcfFile load(File file) throws IOException {
        return new GcfFile(file);
    }
    
    public static void main(String[] args) {
        try {
            File[] f = new File(Utils.locateSteamAppsDirectory()).listFiles(new FilenameFilter() {

                public boolean accept(File parent, String name) {
                    return name.toLowerCase().endsWith(".gcf");
                }
                
            });
            for(int i = 0; i < f.length; i++) {
                logger.log(Level.INFO, "loading {0}", f[i].getName());
                load(f[i]);//.extract();
                logger.info("\n");
            }
//            load(new File(Utils.locateSteamAppsDirectory() + "team fortress 2 content.gcf"));//.extract();
//            logger.info("----------");
//            load(new File(Utils.locateSteamAppsDirectory() + "team fortress 2 materials.gcf"));
        } catch (IOException ex) {
            Logger.getLogger(GcfFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}