package com.timepath.tf2.loaders;

//<editor-fold defaultstate="collapsed" desc="Imports">
import com.timepath.tf2.loaders.GCF.ManifestHeaderBitmask;
import com.timepath.util.io.DataUtils;
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
import javax.swing.tree.DefaultMutableTreeNode;
//</editor-fold>

/**
 * 
 * http://wiki.singul4rity.com/steam:filestructures:gcf
 *
 * @author timepath
 */
public class GCF {
    
    private static final Logger logger = Logger.getLogger(GCF.class.getName());

    //<editor-fold defaultstate="collapsed" desc="Utils">
    public static void analyze(File file, DefaultMutableTreeNode child) {
        try {
            GCF g = new GCF(file);
            String[] entries = g.getEntries();
            for(int i = 0; i < entries.length; i++) {
                child.add(new DefaultMutableTreeNode(entries[i]));
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    public String[] getEntries() {
        String[] out = new String[directoryEntries.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = nameForDirectoryIndex(i);
        }
        return out;
    }
    
    public String nameForDirectoryIndex(int idx) {
        String str = nameForDirectoryIndexRecurse(idx);
        return str.length() > 0 ? str : "/";
    }
    
    private String nameForDirectoryIndexRecurse(int idx) {
        String str;
        if(idx != -1 && directoryEntries[idx].parentIndex != -1 && directoryEntries[idx].parentIndex != 0xFFFFFFFF) {
            str = nameForDirectoryIndexRecurse(directoryEntries[idx].parentIndex);
        } else {
            return "";
        }
        int off = directoryEntries[idx].nameOffset;
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        while(ls[off] != 0) {
            s.write(ls[off]);
            off++;
        }
        str += "/" + new String(s.toByteArray());
        return str;
    }
    
    public File extract(String search, File dest) throws IOException {
        logger.log(Level.INFO, "Extracting {0}", search);
        for(int i = 0; i < directoryEntries.length; i++) {
            String str = nameForDirectoryIndex(i);
            if(!str.equals(search)) {
                continue;
            }
            return extract(i, dest);
        }
        logger.log(Level.INFO, "Could not extract {0}", search);
        return null;
    }
    
    public File extract(int index, File dest) throws IOException {
        String str = nameForDirectoryIndex(index);
        File outFile;
        if(directoryEntries[index].attributes == 0) {
            //<editor-fold defaultstate="collapsed" desc="Extract directory">
            outFile = new File(dest.getPath(), str);
            outFile.mkdirs();
            //</editor-fold>
        } else {
            //<editor-fold defaultstate="collapsed" desc="Extract file">
            outFile = new File(dest, str);
            int idx = directoryMapEntries[index].firstBlockIndex;
            //                logger.log(Level.INFO, "\n\np:{0}\nblockidx:{1}\n", new Object[]{f.getPath(), idx});
            //                logger.log(Level.INFO, "\n\nb:{0}\n", new Object[]{block.toString()});
            outFile.getParentFile().mkdirs();
            outFile.createNewFile();
            if(idx >= blocks.length) {
                logger.log(Level.WARNING, "Block out of range for {0} : {1}. Is the size 0?", new Object[]{outFile.getPath(), index});
                return null;
            }
            BlockAllocationTableEntry block = blockHeader.getBlock(idx);
            RandomAccessFile out = new RandomAccessFile(outFile, "rw");
            int dataIdx = block.firstClusterIndex;
            for(int q = 0; ; q++) {
                long pos = ((long)dataBlockHeader.firstBlockOffset + ((long)dataIdx * (long)dataBlockHeader.blockSize));
                out.seek(pos);
                byte[] buf = new byte[dataBlockHeader.blockSize];
                if(block.fileDataOffset != 0) {
//                    logger.log(Level.INFO, "off = {0}", block.fileDataOffset);
                }
                out.read(buf);
                out.seek(block.fileDataOffset + (q * dataBlockHeader.blockSize));
                if(out.getFilePointer() + buf.length > block.fileDataSize) {
                    out.write(buf, 0, (block.fileDataSize % dataBlockHeader.blockSize));
                } else {
                    out.write(buf);
                }
                if(dataIdx == 65535) {
                    break;
                }
                dataIdx = fragMap.getEntry(dataIdx).nextClusterIndex;
                if(dataIdx == -1) {
                    break;
                }
            }
            out.close();
            //</editor-fold>
        }
        return outFile;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Reading">
    private final RandomAccessFile rf;
    
    private final File file;
    
    //<editor-fold defaultstate="collapsed" desc="Header info">
    //<editor-fold defaultstate="collapsed" desc="Header">
    public FileHeader header;
    
    /**
     * 
     */
    public class FileHeader {
        
        /**
         * 11 * 4
         */
        public static final int SIZE = 44;
        
        public final long pos;
        
        /**
         * Always 0x00000001
         * Probably the version number for the structure
         */
        public int headerVersion;
        
        /**
         * Always 0x00000001 for GCF, 0x00000002 for NCF
         */
        public int cacheType;
        
        /**
         * Container version
         */
        public int formatVersion;
        
        /**
         * ID of the cache
         * Can be found in the CDR section of ClientRegistry.blob
         * TF2 is 440
         */
        public int applicationID;
        
        /**
         * Current revision
         */
        public int applicationVersion;
        
        /**
         * Unsure
         */
        public int isMounted;
        
        /**
         * Always 0x00000000
         */
        public int dummy0;
        
        /**
         * Total size of GCF file in bytes
         */
        public int fileSize;
        
        /**
         * Size of each data cluster in bytes
         */
        public int clusterSize;
        
        /**
         * Number of data blocks
         */
        public int clusterCount;
        
        /**
         * 
         */
        public int checksum;
        
        private FileHeader() throws IOException {
            pos = rf.getFilePointer();
            headerVersion = DataUtils.readULEInt(rf);
            cacheType = DataUtils.readULEInt(rf);
            formatVersion = DataUtils.readULEInt(rf);
            applicationID = DataUtils.readULEInt(rf);
            applicationVersion = DataUtils.readULEInt(rf);
            isMounted = DataUtils.readULEInt(rf);
            dummy0 = DataUtils.readULEInt(rf);
            fileSize = DataUtils.readULEInt(rf);
            clusterSize = DataUtils.readULEInt(rf);
            clusterCount = DataUtils.readULEInt(rf);
            checksum = DataUtils.readULEInt(rf);
        }
        
        @Override
        public String toString() {
            int checked = 0;
            checked += DataUtils.updateChecksumAddSpecial(headerVersion);
            checked += DataUtils.updateChecksumAddSpecial(cacheType);
            checked += DataUtils.updateChecksumAddSpecial(formatVersion);
            checked += DataUtils.updateChecksumAddSpecial(applicationID);
            checked += DataUtils.updateChecksumAddSpecial(applicationVersion);
            checked += DataUtils.updateChecksumAddSpecial(isMounted);
            checked += DataUtils.updateChecksumAddSpecial(dummy0);
            checked += DataUtils.updateChecksumAddSpecial(fileSize);
            checked += DataUtils.updateChecksumAddSpecial(clusterSize);
            checked += DataUtils.updateChecksumAddSpecial(clusterCount);
            String checkState = (checksum == checked) ? "OK" : checksum + "vs" + checked;
            return "id:" + applicationID + ", ver:" + formatVersion + ", rev:" + applicationVersion + ", mounted?: " + isMounted + ", size:" + fileSize + ", blockSize:" + clusterSize + ", blocks:" + clusterCount + ", checksum:" + checkState;
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Block">
    public BlockAllocationTableHeader blockHeader;
    
    /**
     * 
     */
    public class BlockAllocationTableHeader {
        
        /**
         * 8 * 4
         */
        public static final int SIZE = 32;
        
        public final long pos;
        
        /**
         * Number of data blocks
         */
        public int blockCount;
        
        /**
         * Number of data blocks that point to data
         */
        public int blocksUsed;
        
        /**
         * 
         */
        public int lastBlockUsed;
        
        /**
         * 
         */
        public int dummy0;
        
        /**
         * 
         */
        public int dummy1;
        
        /**
         * 
         */
        public int dummy2;
        
        /**
         * 
         */
        public int dummy3;
        
        /**
         * Header checksum
         * The checksum is simply the sum total of all the preceeding DWORDs in the header
         */
        public int checksum;
        
        private BlockAllocationTableHeader() throws IOException {
            pos = rf.getFilePointer();
            blockCount = DataUtils.readULEInt(rf);
            blocksUsed = DataUtils.readULEInt(rf);
            lastBlockUsed = DataUtils.readULEInt(rf);
            dummy0 = DataUtils.readULEInt(rf);
            dummy1 = DataUtils.readULEInt(rf);
            dummy2 = DataUtils.readULEInt(rf);
            dummy3 = DataUtils.readULEInt(rf);
            checksum = DataUtils.readULEInt(rf);
            
            blocks = new BlockAllocationTableEntry[blockCount];
            rf.skipBytes(blockCount * BlockAllocationTableEntry.SIZE);
        }
        
        public BlockAllocationTableEntry getBlock(int i) {
            BlockAllocationTableEntry b = blocks[i];
            if(b == null) {
                try {
//                    long orig = rf.getFilePointer();
                    rf.seek((pos + SIZE) + (i * BlockAllocationTableEntry.SIZE));
                    b = new BlockAllocationTableEntry();
//                    rf.seek(orig);
//                    blocks[i] = b; // ???
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
            return b;
        }
        
        @Override
        public String toString() {
            int checked = 0;
            checked += DataUtils.updateChecksumAdd(blockCount);
            checked += DataUtils.updateChecksumAdd(blocksUsed);
            checked += DataUtils.updateChecksumAdd(lastBlockUsed);
            checked += DataUtils.updateChecksumAdd(dummy0);
            checked += DataUtils.updateChecksumAdd(dummy1);
            checked += DataUtils.updateChecksumAdd(dummy2);
            checked += DataUtils.updateChecksumAdd(dummy3);
            String checkState = (checksum == checked) ? "OK" : checksum + " vs " + checked;
            return "blockCount:" + blockCount + ", blocksUsed:" + blocksUsed + ", check:" + checkState;
        }
    }
    
    private BlockAllocationTableEntry[] blocks;
    
    /**
     * 
     */
    public class BlockAllocationTableEntry {
        
        /**
         * 7 * 4
         */
        public static final int SIZE = 28;
        
//        public final long pos; // unneccesary information
        
        /**
         * Flags for the block entry
         * 0x200F0000 == Not used
         */
        int entryType;
        
        /**
         * The offset for the data contained in this block entry in the file
         */
        int fileDataOffset;
        
        /**
         * The length of the data in this block entry
         */
        int fileDataSize;
        
        /**
         * The index to the first data block of this block entry's data
         */
        int firstClusterIndex;
        
        /**
         * The next block entry in the series
         * (N/A if == BlockCount)
         */
        int nextBlockEntryIndex;
        
        /**
         * The previous block entry in the series
         * (N/A if == BlockCount)
         */
        int previousBlockEntryIndex;
        
        /**
         * The index of the block entry in the manifest
         */
        int manifestIndex;
        
        private BlockAllocationTableEntry() throws IOException {            
            entryType = DataUtils.readULEInt(rf);
            fileDataOffset = DataUtils.readULEInt(rf);
            fileDataSize = DataUtils.readULEInt(rf);
            firstClusterIndex = DataUtils.readULEInt(rf);
            nextBlockEntryIndex = DataUtils.readULEInt(rf);
            previousBlockEntryIndex = DataUtils.readULEInt(rf);
            manifestIndex = DataUtils.readULEInt(rf);
        }
        
        @Override
        public String toString() {
            return "type:" + entryType + ", off:" + fileDataOffset + ", size:" + fileDataSize + ", firstidx:" + firstClusterIndex + ", nextidx:" + nextBlockEntryIndex + ", previdx:" + previousBlockEntryIndex + ", di:" + manifestIndex;
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Frag Map">
    public FileAllocationTableHeader fragMap;
    
    /**
     * 
     */
    public class FileAllocationTableHeader {
        
        /**
         * 4 * 4
         */
        public static final int SIZE = 16;
        
        public final long pos;
        
        /**
         * Number of data blocks
         */
        public int clusterCount;
        
        /**
         * Index of 1st unused GCFFRAGMAP entry?
         */
        public int firstUnusedEntry;
        
        /**
         * Defines the end of block chain terminator
         * If the value is 0, then the terminator is 0x0000FFFF; if the value is 1, then the terminator is 0xFFFFFFFF
         */
        public int isLongTerminator;
        
        /**
         * Header checksum
         */
        public int checksum;
        
        private FileAllocationTableHeader() throws IOException {
            pos = rf.getFilePointer();
            clusterCount = DataUtils.readULEInt(rf);
            firstUnusedEntry = DataUtils.readULEInt(rf);
            isLongTerminator = DataUtils.readULEInt(rf);
            checksum = DataUtils.readULEInt(rf);
            fragMapEntries = new FileAllocationTableEntry[clusterCount];
            rf.skipBytes(clusterCount * FileAllocationTableEntry.SIZE);
        }
        
        public FileAllocationTableEntry getEntry(int i) throws IOException {
            FileAllocationTableEntry fe = fragMapEntries[i];
            if(fe == null) {
                rf.seek(pos + SIZE + (i * FileAllocationTableEntry.SIZE));
                fe = new FileAllocationTableEntry();
//                fragMapEntries[i] = fe; // ???
            }
            return fe;
        }
        
        @Override
        public String toString() {
            int checked = 0;
            checked += DataUtils.updateChecksumAdd(clusterCount);
            checked += DataUtils.updateChecksumAdd(firstUnusedEntry);
            checked += DataUtils.updateChecksumAdd(isLongTerminator);
            String checkState = (checksum == checked) ? "OK" : checksum + "vs" + checked;
            return "blockCount:" + clusterCount + ", firstUnusedEntry:" + firstUnusedEntry + ", isLongTerminator:" + isLongTerminator + ", checksum:" + checkState;
        }
        
    }
    
    private FileAllocationTableEntry[] fragMapEntries;
    
    /**
     * 
     */
    public class FileAllocationTableEntry {
        
        /**
         * 1 * 4
         */
        public static final int SIZE = 4;
        
        /**
         * The index of the next data block
         * If == FileAllocationTableHeader.isLongTerminator, there are no more clusters in the file
         */
        public int nextClusterIndex;
        
        private FileAllocationTableEntry() throws IOException {
            nextClusterIndex = DataUtils.readULEInt(rf);
        }

        @Override
        public String toString() {
            return "nextDataBlockIndex:" + nextClusterIndex;
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Directories">
    public ManifestHeader directoryHeader;
    
    public enum ManifestHeaderBitmask {
        
        Build_Mode(0x1),
        Is_Purge_All(0x2),
        Is_Long_Roll(0x4),
        Depot_Key(0xFFFFFF00);
        
        int mask;
        
        ManifestHeaderBitmask(int mask) {
            this.mask = mask;
        }
        
        private static final ManifestHeaderBitmask[] flags = ManifestHeaderBitmask.values();
        
        public static ManifestHeaderBitmask get(int mask) {
            ManifestHeaderBitmask m = flags[mask];
            return m;
        }
    };
    
    public class ManifestHeader {
        
        /**
         * 14 * 4
         */
        public static final int SIZE = 56;
        
        public final long pos;
        
        int headerVersion;		// Always 0x00000004
        int applicationID;		// Cache ID.
        int applicationVersion;        // GCF file version.
        int nodeCount;          // Number of items in the directory.
        int fileCount;          // Number of files in the directory.
        int compressionBlockSize;		// Always 0x00008000
        
        /**
         * Inclusive of header
         */
        int binarySize;	// Size of lpGCFDirectoryEntries & lpGCFDirectoryNames & lpGCFDirectoryInfo1Entries & lpGCFDirectoryInfo2Entries & lpGCFDirectoryCopyEntries & lpGCFDirectoryLocalEntries in bytes.
        int nameSize;		// Size of the directory names in bytes.
        int hashTableKeyCount;         // Number of Info1 entires.
        int minimumFootprintCount;          // Number of files to copy.
        int userConfigCount;         // Number of files to keep local.
//        ManifestHeaderBitmask bitmask;
        int bitmask;
        int fingerprint;
        int checksum;
        
        ManifestHeader() throws IOException {
            pos = rf.getFilePointer();
            headerVersion = DataUtils.readULEInt(rf);
            applicationID = DataUtils.readULEInt(rf);
            applicationVersion = DataUtils.readULEInt(rf);
            nodeCount = DataUtils.readULEInt(rf);
            fileCount = DataUtils.readULEInt(rf);
            compressionBlockSize = DataUtils.readULEInt(rf);
            binarySize = DataUtils.readULEInt(rf);
            nameSize = DataUtils.readULEInt(rf);
            hashTableKeyCount = DataUtils.readULEInt(rf);
            minimumFootprintCount = DataUtils.readULEInt(rf);
            userConfigCount = DataUtils.readULEInt(rf);
//            bitmask = ManifestHeaderBitmask.get(DataUtils.readULEInt(rf));
            bitmask = DataUtils.readULEInt(rf);
            fingerprint = DataUtils.readULEInt(rf);
            checksum = DataUtils.readULEInt(rf);
        }
        
        @Override
        public String toString() {
            try {
                ByteBuffer bbh = ByteBuffer.allocate(SIZE);
                bbh.order(ByteOrder.LITTLE_ENDIAN);
                bbh.putInt(headerVersion);
                bbh.putInt(applicationID);
                bbh.putInt(applicationVersion);
                bbh.putInt(nodeCount);
                bbh.putInt(fileCount);
                bbh.putInt(compressionBlockSize);
                bbh.putInt(binarySize);
                bbh.putInt(nameSize);
                bbh.putInt(hashTableKeyCount);
                bbh.putInt(minimumFootprintCount);
                bbh.putInt(userConfigCount);
                bbh.putInt(bitmask);
                bbh.putInt(0);
                bbh.putInt(0);
                bbh.flip();
                byte[] bytes1 = bbh.array();
                
                rf.seek(pos + SIZE);
                ByteBuffer bb = ByteBuffer.allocate(binarySize);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                bb.put(bytes1);
                bb.put(DataUtils.readBytes(rf, binarySize - SIZE));
                bb.flip();
                byte[] bytes = bb.array();
                Checksum adler32 = new Adler32();
                adler32.update(bytes, 0, bytes.length);
                int checked = (int)(adler32.getValue() & 0xFFFFFFFF);
                String checkState = (checksum == checked) ? "OK" : DataUtils.toBinaryString(checksum) + " vs " + DataUtils.toBinaryString(checked);
                return Long.toHexString(pos) + " : id:" + applicationID + ", ver:" + applicationVersion + ", bitmask:0x"+Integer.toHexString(bitmask).toUpperCase()+", items:" + nodeCount + ", files:" + fileCount + ", dsize:" + binarySize + ", nsize:" + nameSize + ", info1:" + hashTableKeyCount + ", copy:" + minimumFootprintCount + ", local:" + userConfigCount + ", check:" + checkState;
            } catch (IOException ex) {
                Logger.getLogger(GCF.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
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
            headerVersion = DataUtils.readULEInt(rf);
            dummy0 = DataUtils.readULEInt(rf);
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
            headerVersion = DataUtils.readULEInt(rf);
            ChecksumSize = DataUtils.readULEInt(rf);
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
    public DataBlockHeader dataBlockHeader;
    
    //GCF Data Header
    /**
     * 
     */
    public class DataBlockHeader {
        
        /**
         * GCF file version
         */
        public int gcfRevision;
        
        /**
         * Number of data blocks
         */
        public int blockCount;
        
        /**
         * Size of each data block in bytes
         */
        public int blockSize;
        
        /**
         * Offset to first data block
         */
        public int firstBlockOffset;
        
        /**
         * Number of data blocks that contain data
         */
        public int blocksUsed;
        
        /**
         * Header checksum
         */
        public int checksum;
        
        @Override
        public String toString() {
            int checked = 0;
            checked += DataUtils.updateChecksumAdd(blockCount);
            checked += DataUtils.updateChecksumAdd(blockSize);
            checked += DataUtils.updateChecksumAdd(firstBlockOffset);
            checked += DataUtils.updateChecksumAdd(blocksUsed);
            String checkState = (checksum == checked) ? "OK" : checksum + "vs" + checked;
            return "v:" + gcfRevision + ", blocks:" + blockCount + ", size:" + blockSize + ", offset:0x" + Integer.toHexString(firstBlockOffset) + ", used:" + blocksUsed + ", check:" + checkState;
        }
    }
    //</editor-fold>
    //</editor-fold>
    
    public byte[] ls = null;
    
    public GCF(File file) throws IOException {
        this.file = file;
        rf = new RandomAccessFile(file, "r");
        
        header = new FileHeader();
        blockHeader = new BlockAllocationTableHeader();
        fragMap = new FileAllocationTableHeader();
        
        directoryHeader = new ManifestHeader();
        //<editor-fold defaultstate="collapsed" desc="Directories">
        boolean skipDirs = false;
        if(skipDirs) {
            rf.skipBytes(directoryHeader.binarySize - ManifestHeader.SIZE);
        } else {
            directoryEntries = new DirectoryEntry[directoryHeader.nodeCount];
            for(int i = 0; i < directoryHeader.nodeCount; i++) {
                //                if(i % 10000 == 0) {
                //                    logger.log(Level.INFO, "Loading entry {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
                //                }
                DirectoryEntry de = new DirectoryEntry();
                de.nameOffset = DataUtils.readULEInt(rf);
                de.itemSize = DataUtils.readULEInt(rf);
                de.checksumIndex = DataUtils.readULEInt(rf);
                de.attributes = DataUtils.readULEInt(rf);
                de.parentIndex = DataUtils.readULEInt(rf);
                de.nextIndex = DataUtils.readULEInt(rf);
                de.firstChildIndex = DataUtils.readULEInt(rf);
                
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
                f.Dummy0 = DataUtils.readULEInt(rf);
                
                info1Entries[i] = f;
            }
            
            info2Entries = new tagGCFDIRECTORYINFO2ENTRY[directoryHeader.nodeCount];
            for(int i = 0; i < directoryHeader.nodeCount; i++) {
                //                if(i % 10000 == 0) {
                //                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
                //                }
                tagGCFDIRECTORYINFO2ENTRY f = new tagGCFDIRECTORYINFO2ENTRY();
                f.Dummy0 = DataUtils.readULEInt(rf);
                
                info2Entries[i] = f;
            }
            
            copyEntries = new tagGCFDIRECTORYCOPYENTRY[directoryHeader.minimumFootprintCount];
            for(int i = 0; i < directoryHeader.minimumFootprintCount; i++) {
                //                if(i % 10000 == 0) {
                //                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.copyCount});
                //                }
                tagGCFDIRECTORYCOPYENTRY f = new tagGCFDIRECTORYCOPYENTRY();
                f.DirectoryIndex = DataUtils.readULEInt(rf);
                
                copyEntries[i] = f;
            }
            
            localEntries = new tagGCFDIRECTORYLOCALENTRY[directoryHeader.userConfigCount];
            for(int i = 0; i < directoryHeader.userConfigCount; i++) {
                //                if(i % 10000 == 0) {
                //                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.localCount});
                //                }
                tagGCFDIRECTORYLOCALENTRY f = new tagGCFDIRECTORYLOCALENTRY();
                f.DirectoryIndex = DataUtils.readULEInt(rf);
                
                localEntries[i] = f;
            }
        }
        
        //</editor-fold>
        
        directoryMapHeader = new GCF.DirectoryMapHeader();
        //<editor-fold defaultstate="collapsed" desc="Directory Map">
        directoryMapEntries = new DirectoryMapEntry[directoryHeader.nodeCount];
        for(int i = 0; i < directoryHeader.nodeCount; i++) {
            //            if(i % 10000 == 0) {
            //                logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
            //            }
            DirectoryMapEntry dme = new DirectoryMapEntry();
            dme.firstBlockIndex = DataUtils.readULEInt(rf);
            
            directoryMapEntries[i] = dme;
        }
        //</editor-fold>
        
        checksumHeader = new GCF.ChecksumHeader();
        //<editor-fold defaultstate="collapsed" desc="Checksums">
        //        logger.log(Level.INFO, "csize:{0}", checksumHeader.ChecksumSize);
        
        checksumMapHeader = new GCF.ChecksumMapHeader();
        checksumMapHeader.formatCode = DataUtils.readULEInt(rf);
        checksumMapHeader.Dummy1 = DataUtils.readULEInt(rf);
        checksumMapHeader.ItemCount = DataUtils.readULEInt(rf);
        checksumMapHeader.ChecksumCount = DataUtils.readULEInt(rf);
        //        logger.log(Level.INFO, "items:{0}, checks:{1}", new Object[]{checksumMapHeader.ItemCount, checksumMapHeader.ChecksumCount});
        
        checksumMapEntries = new ChecksumMapEntry[checksumMapHeader.ItemCount];
        for(int i = 0; i < checksumMapHeader.ItemCount; i++) {
            ChecksumMapEntry cme = new GCF.ChecksumMapEntry();
            cme.checksumCount = DataUtils.readULEInt(rf);
            cme.firstChecksumIndex = DataUtils.readULEInt(rf);
            checksumMapEntries[i] = cme;
        }
        
        checksumEntries = new ChecksumEntry[checksumMapHeader.ChecksumCount + 0x20];
        for(int i = 0; i < checksumEntries.length; i++) {
            //            if(i % 10000 == 0) {
            //                logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, checksumMapEntries.ChecksumCount});
            //            }
            ChecksumEntry ce = new ChecksumEntry();
            ce.checksum = DataUtils.readULEInt(rf);
            checksumEntries[i] = ce;
        }
        //</editor-fold>
        
        // TODO: Slow. Takes about 73 seconds
        //<editor-fold defaultstate="collapsed" desc="Data">
        dataBlockHeader = new DataBlockHeader();
        dataBlockHeader.gcfRevision = DataUtils.readULEInt(rf);
        dataBlockHeader.blockCount = DataUtils.readULEInt(rf);
        dataBlockHeader.blockSize = DataUtils.readULEInt(rf);
        dataBlockHeader.firstBlockOffset = DataUtils.readULEInt(rf);
        dataBlockHeader.blocksUsed = DataUtils.readULEInt(rf);
        dataBlockHeader.checksum = DataUtils.readULEInt(rf);
        
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
        
        logger.log(Level.INFO, "{0}\n{1}\n{2}\n{3}\n{4}\n{5}\n{6}\n{7}\n", new Object[]{file.getPath(), "header:\t" + header.toString(), "blockHchecksumeader:\t" + blockHeader.toString(), "fragMap:\t" + fragMap.toString(), "directoryHeader:\t" + directoryHeader.toString(), "directoryMapHeader:\t" + directoryMapHeader.toString(), "checksumHeader:\t" + checksumHeader.toString(), "dataBlockHeader:\t" + dataBlockHeader.toString()});
        
        //        rf.close();
    }
    //</editor-fold>
    
}