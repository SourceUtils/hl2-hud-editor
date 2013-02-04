package com.timepath.tf2.hudeditor.loaders;

import com.timepath.tf2.hudeditor.util.DataUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

/**
 * 
 * http://wiki.singul4rity.com/steam:filestructures:gcf
 *
 * @author TimePath
 */
public class GCF {
    
    private static final Logger logger = Logger.getLogger(GCF.class.getName());
    
    private final RandomAccessFile rf;
    
    private final String name;
    
    //<editor-fold defaultstate="collapsed" desc="Header info">
    //<editor-fold defaultstate="collapsed" desc="Header">
    GcfHeader header;
    
    class GcfHeader {
        
        int headerVersion;     // Always 0x00000001
        int cacheType;     // Always 0x00000001 for GCF, 2 for NCF
        int formatVersion; // TF2 is 0x00000006
        int applicationID; // TF2 is 440
        int applicationVersion;
        int isMounted; // unsure
        int dummy0;
        int fileSize;	// Total size of GCF file in bytes.
        int blockSize;  // Size of each data block in bytes.
        int blockCount; // Number of data blocks.
        int checksum;
        
        static final int size = 11 * 4;
        
        GcfHeader() throws IOException {
            headerVersion = DataUtils.readLEInt(rf);
            cacheType = DataUtils.readLEInt(rf);
            formatVersion = DataUtils.readLEInt(rf);
            applicationID = DataUtils.readLEInt(rf);
            applicationVersion = DataUtils.readLEInt(rf);
            isMounted = DataUtils.readLEInt(rf);
            dummy0 = DataUtils.readLEInt(rf);
            fileSize = DataUtils.readLEInt(rf);
            blockSize = DataUtils.readLEInt(rf);
            blockCount = DataUtils.readLEInt(rf);
            checksum = DataUtils.readLEInt(rf);
        }
        
        @Override
        public String toString() {
            int checked = 0;
            checked += DataUtils.updateChecksum(headerVersion);
            checked += DataUtils.updateChecksum(cacheType);
            checked += DataUtils.updateChecksum(formatVersion);
            checked += DataUtils.updateChecksum(applicationID);
            checked += DataUtils.updateChecksum(applicationVersion);
            checked += DataUtils.updateChecksum(isMounted);
            checked += DataUtils.updateChecksum(dummy0);
            checked += DataUtils.updateChecksum(fileSize);
            checked += DataUtils.updateChecksum(blockSize);
            checked += DataUtils.updateChecksum(blockCount);
            return "id:" + applicationID + ", ver:" + formatVersion + ", rev:" + applicationVersion + ", mounted?: " + isMounted + ", size:" + fileSize + ", blockSize:" + blockSize + ", blocks:" + blockCount + ", checksum:" + checksum + "vs" + checked;
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Block">
    BlockHeader blockHeader;
    
    class BlockHeader {
        
        int blockCount;	// Number of data blocks.
        int blocksUsed;	// Number of data blocks that point to data.
        int lastBlockUsed;
        int dummy0;
        int dummy1;
        int dummy2;
        int dummy3;
        int checksum;   // Header checksum. The checksum is simply the sum total of all the preceeding DWORDs in the header.
        
        static final int size = 8 * 4;
        
        final long pos;
        
        BlockHeader() throws IOException {
            this.pos = rf.getFilePointer();
            blockCount = DataUtils.readLEInt(rf);
            blocksUsed = DataUtils.readLEInt(rf);
            lastBlockUsed = DataUtils.readLEInt(rf);
            dummy0 = DataUtils.readLEInt(rf);
            dummy1 = DataUtils.readLEInt(rf);
            dummy2 = DataUtils.readLEInt(rf);
            dummy3 = DataUtils.readLEInt(rf);
            checksum = DataUtils.readLEInt(rf);
            
            blocks = new Block[blockCount];
            rf.skipBytes(blockCount * Block.size);
        }
        
        @Override
        public String toString() {
            int checked = 0;
            checked += DataUtils.updateChecksum(blockCount);
            checked += DataUtils.updateChecksum(blocksUsed);
            checked += DataUtils.updateChecksum(lastBlockUsed);
            checked += DataUtils.updateChecksum(dummy0);
            checked += DataUtils.updateChecksum(dummy1);
            checked += DataUtils.updateChecksum(dummy2);
            checked += DataUtils.updateChecksum(dummy3);
            return "blockCount:" + blockCount + ", blocksUsed:" + blocksUsed + ", check:" + checksum + " vs " + checked;
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
        int firstUnusedEntry;	// index of 1st unused GCFFRAGMAP entry?
        int isLongTerminator; // defines the end of block chain terminator. If the value is 0, then the terminator is 0x0000FFFF; if the value is 1, then the terminator is 0xFFFFFFFF.
        int checksum;   // Header checksum.
        
        private long pos;
        
        FragMapHeader() throws IOException {
            this.pos = rf.getFilePointer();
            blockCount = DataUtils.readLEInt(rf);
            firstUnusedEntry = DataUtils.readLEInt(rf);
            isLongTerminator = DataUtils.readLEInt(rf);
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
            return "blockCount:" + blockCount + ", dummy0:" + firstUnusedEntry + ", dummy1:" + isLongTerminator + ", checksum:" + checksum + " vs " + (blockCount + firstUnusedEntry + isLongTerminator);
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
    
    enum DirectoryHeaderBitmask {
        
        Build_Mode(0x1),
        Is_Purge_All(0x2),
        Is_Long_Roll(0x4),
        Depot_Key(0xFFFFFF00);
        
        int mask;
        
        DirectoryHeaderBitmask(int mask) {
            this.mask = mask;
        }
    };
    
    private class DirectoryHeader {
        
        int headerVersion;		// Always 0x00000004
        int applicationID;		// Cache ID.
        int applicationVersion;        // GCF file version.
        int nodeCount;          // Number of items in the directory.
        int fileCount;          // Number of files in the directory.
        int compressedBlockSize;		// Always 0x00008000
        int binarySize;	// Size of lpGCFDirectoryEntries & lpGCFDirectoryNames & lpGCFDirectoryInfo1Entries & lpGCFDirectoryInfo2Entries & lpGCFDirectoryCopyEntries & lpGCFDirectoryLocalEntries in bytes.
        int nameSize;		// Size of the directory names in bytes.
        int hashTableKeyCount;         // Number of Info1 entires.
        int copyCount;          // Number of files to copy.
        int localCount;         // Number of files to keep local.
        int bitmask;
        int fingerprint;
        int checksum;
        
        DirectoryHeader() throws IOException {
            headerVersion = DataUtils.readLEInt(rf);
            applicationID = DataUtils.readLEInt(rf);
            applicationVersion = DataUtils.readLEInt(rf);
            nodeCount = DataUtils.readLEInt(rf);
            fileCount = DataUtils.readLEInt(rf);
            compressedBlockSize = DataUtils.readLEInt(rf);
            binarySize = DataUtils.readLEInt(rf);
            nameSize = DataUtils.readLEInt(rf);
            hashTableKeyCount = DataUtils.readLEInt(rf);
            copyCount = DataUtils.readLEInt(rf);
            localCount = DataUtils.readLEInt(rf);
            bitmask = DataUtils.readLEInt(rf);
            fingerprint = DataUtils.readLEInt(rf);
            checksum = DataUtils.readLEInt(rf);
        }
        
        @Override
        public String toString() {
            ByteBuffer bb = ByteBuffer.allocate(14 * 4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putInt(headerVersion);
            bb.putInt(applicationID);
            bb.putInt(applicationVersion);
            bb.putInt(nodeCount);
            bb.putInt(fileCount);
            bb.putInt(compressedBlockSize);
            bb.putInt(binarySize);
            bb.putInt(nameSize);
            bb.putInt(hashTableKeyCount);
            bb.putInt(copyCount);
            bb.putInt(localCount);
            bb.putInt(bitmask);
            bb.putInt(0);
            bb.putInt(0);
            bb.flip();
            byte[] bytes = bb.array();
            Checksum adler32 = new Adler32();
            adler32.update(bytes, 0, bytes.length);
            long checked = adler32.getValue();
            
            return "id:" + applicationID + ", ver:" + applicationVersion + ", items:" + nodeCount + ", files:" + fileCount + ", dsize:" + binarySize + ", nsize:" + nameSize + ", info1:" + hashTableKeyCount + ", copy:" + copyCount + ", local:" + localCount + ", check:" + checksum + " vs " + checked;
        }
    }
    
    DirectoryEntry[] directoryEntries;
    
    enum DirectoryEntryAttributes {
        File(0x4000),
        Executable_File(0x800),
        Hidden_File(0x400),
        ReadOnly_File(0x200),
        Encrypted_File(0x100),
        Purge_File(0x80),
        Backup_Before_Overwriting(0x40),
        NoCache_File(0x20),
        Locked_File(0x8),
        Launch_File(0x2),
        Configuration_File(0x1),
        Directory(0);
                
        int flags;
                
        DirectoryEntryAttributes(int flags) {
            this.flags = flags;
        }
    };
    
    class DirectoryEntry {
        int nameOffset;         // Offset to the directory item name from the end of the directory items.
        int itemSize;		// Size of the item.  (If file, file size.  If folder, num items.)
        int checksumIndex;	// Checksum index / file ID. (0xFFFFFFFF == None).
        int attributes;
        int parentIndex;	// Index of the parent directory item.  (0xFFFFFFFF == None).
        int nextIndex;          // Index of the next directory item.  (0x00000000 == None).
        int firstChildIndex;         // Index of the first directory item.  (0x00000000 == None).
        
        @Override
        public String toString() {
            return nameOffset + ", " + itemSize + ", " + checksumIndex + ", " + attributes + ", " + parentIndex + ", " + nextIndex + ", " + firstChildIndex;
        }
    }
    
    tagGCFDIRECTORYINFO1ENTRY[] info1Entries; // nameTable
    
    //GCF Directory Info 1 Entry
    class tagGCFDIRECTORYINFO1ENTRY {
        int Dummy0;
        
        @Override
        public String toString() {
            return "" + (char) Dummy0;
        }
    }
    
    tagGCFDIRECTORYINFO2ENTRY[] info2Entries; // hashTable
    
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
        int headerVersion;     // Always 0x00000001
        int dummy0;     // Always 0x00000000
        
        DirectoryMapHeader() throws IOException {
            headerVersion = DataUtils.readLEInt(rf);
            dummy0 = DataUtils.readLEInt(rf);
        }
        
        @Override
        public String toString() {
            return "headerVersion:" + headerVersion + ", Dummy0:" + dummy0;
        }
    }
    
    DirectoryMapEntry[] directoryMapEntries;
    
    //GCF Directory Map Entry
    class DirectoryMapEntry {
        int firstBlockIndex;    // Index of the first data block. (N/A if == BlockCount.)
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Checksum Header">
    ChecksumHeader checksumHeader;
    
    //GCF Checksum Header
    class ChecksumHeader {
        int headerVersion;			// Always 0x00000001
        int ChecksumSize;		// Size of LPGCFCHECKSUMHEADER & LPGCFCHECKSUMMAPHEADER & in bytes.
                                        // the number of bytes in the checksum section (excluding this structure and the following LatestApplicationVersion structure).
        
        ChecksumHeader() throws IOException {
            headerVersion = DataUtils.readLEInt(rf);
            ChecksumSize = DataUtils.readLEInt(rf);
        }
        
        @Override
        public String toString() {
            return "headerVersion:" + headerVersion + ", ChecksumSize:" + ChecksumSize;
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Checksum map">
    ChecksumMapHeader checksumMapHeader;
    
    //GCF Checksum Map Header
    class ChecksumMapHeader {
        int formatCode;			// Always 0x14893721
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
    
    public GCF(File file) throws IOException {
        this.name = file.getName();
        rf = new RandomAccessFile(file, "r");
        
        header = new GcfHeader();     
        logger.info(header.toString());
        blockHeader = new BlockHeader();
        fragMap = new FragMapHeader();

        directoryHeader = new DirectoryHeader();
        logger.info(directoryHeader.toString());
        //<editor-fold defaultstate="collapsed" desc="Directories">
        boolean skipDirs = false;
        if(skipDirs) {
            rf.skipBytes(directoryHeader.binarySize - (14 * 4));
        } else {
            directoryEntries = new DirectoryEntry[directoryHeader.nodeCount];
            for(int i = 0; i < directoryHeader.nodeCount; i++) {
//                if(i % 10000 == 0) {
//                    logger.log(Level.INFO, "Loading entry {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
//                }
                DirectoryEntry de = new DirectoryEntry();
                de.nameOffset = DataUtils.readLEInt(rf);
                de.itemSize = DataUtils.readLEInt(rf);
                de.checksumIndex = DataUtils.readLEInt(rf);
                de.attributes = DataUtils.readLEInt(rf);
                de.parentIndex = DataUtils.readLEInt(rf);
                de.nextIndex = DataUtils.readLEInt(rf);
                de.firstChildIndex = DataUtils.readLEInt(rf);
                
                directoryEntries[i] = de;
            }
            
//            logger.log(Level.INFO, "Loading names ({0}) bytes", directoryHeader.nameSize);
            ls = new byte[directoryHeader.nameSize];
            rf.read(ls);
            
            info1Entries = new tagGCFDIRECTORYINFO1ENTRY[directoryHeader.hashTableKeyCount];
            for(int i = 0; i < directoryHeader.hashTableKeyCount; i++) {
//                if(i % 10000 == 0) {
//                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.info1Count});
//                }
                tagGCFDIRECTORYINFO1ENTRY f = new tagGCFDIRECTORYINFO1ENTRY();
                f.Dummy0 = DataUtils.readLEInt(rf);
                
                info1Entries[i] = f;
            }
            
            info2Entries = new tagGCFDIRECTORYINFO2ENTRY[directoryHeader.nodeCount];
            for(int i = 0; i < directoryHeader.nodeCount; i++) {
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
        

        directoryMapHeader = new GCF.DirectoryMapHeader();
        logger.info(directoryMapHeader.toString());
        //<editor-fold defaultstate="collapsed" desc="Directory Map">
        directoryMapEntries = new DirectoryMapEntry[directoryHeader.nodeCount];
        for(int i = 0; i < directoryHeader.nodeCount; i++) {
//            if(i % 10000 == 0) {
//                logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
//            }
            DirectoryMapEntry dme = new DirectoryMapEntry();
            dme.firstBlockIndex = DataUtils.readLEInt(rf);

            directoryMapEntries[i] = dme;
        }
        //</editor-fold>
        
        checksumHeader = new GCF.ChecksumHeader();
        logger.info(checksumHeader.toString());
        //<editor-fold defaultstate="collapsed" desc="Checksums">
//        logger.log(Level.INFO, "csize:{0}", checksumHeader.ChecksumSize);
        
        checksumMapHeader = new GCF.ChecksumMapHeader();
        checksumMapHeader.formatCode = DataUtils.readLEInt(rf);
        checksumMapHeader.Dummy1 = DataUtils.readLEInt(rf);
        checksumMapHeader.ItemCount = DataUtils.readLEInt(rf);
        checksumMapHeader.ChecksumCount = DataUtils.readLEInt(rf);
//        logger.log(Level.INFO, "items:{0}, checks:{1}", new Object[]{checksumMapHeader.ItemCount, checksumMapHeader.ChecksumCount});
        
        checksumMapEntries = new ChecksumMapEntry[checksumMapHeader.ItemCount];
        for(int i = 0; i < checksumMapHeader.ItemCount; i++) {
            ChecksumMapEntry cme = new GCF.ChecksumMapEntry();
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
            s.write(b[off]);
            off++;
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
            if(directoryEntries[i].attributes == 0) { // is a directory.
                File dir = new File(root.getPath(), str);
                dir.mkdirs();
            } else {                
                File f = new File(root, str);
                int idx = directoryMapEntries[i].firstBlockIndex;
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
    
    public static GCF load(File file) throws IOException {
        return new GCF(file);
    }
    
}