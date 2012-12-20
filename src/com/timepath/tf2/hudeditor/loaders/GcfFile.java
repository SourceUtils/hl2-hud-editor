package com.timepath.tf2.hudeditor.loaders;

import com.timepath.tf2.hudeditor.gui.EditorFrame;
import com.timepath.tf2.hudeditor.util.DataUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
    
    //<editor-fold defaultstate="collapsed" desc="Header info">
    Header header;
    
    private class Header {
        
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
        
        @Override
        public String toString() {
            return "id:" + CacheId + ", ver:" + gcfRevision + ", size:" + fileSize + ", blockSize:" + blockSize + ", blocks:" + blockCount;
        }
        
    }
    
    BlockHeader blockHeader;
    
    class BlockHeader {
        
        final long pos;
        static final int size = 8 * 4;
        
        BlockHeader(long pos) {
            this.pos = pos;
        }
        
        int blockCount;	// Number of data blocks.
        int blocksUsed;	// Number of data blocks that point to data.
        int dummy0;
        int dummy1;
        int dummy2;
        int dummy3;
        int dummy4;
        int checksum;   // Header checksum. The checksum is simply the sum total of all the preceeding DWORDs in the header.
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
        
        @Override
        public String toString() {
            return "type:" + entryType + ", off:" + fileDataOffset + ", size:" + fileDataSize + ", firstidx:" + firstDataBlockIndex + ", nextidx:" + nextBlockEntryIndex + ", previdx:" + previousBlockEntryIndex + ", di:" + directoryIndex;
        }
        
    }
    
    GcfFragMapHeader fragMap;
    
    class GcfFragMapHeader {
        
        int blockCount;	// Number of data blocks.
        int dummy0;	// index of 1st unused GCFFRAGMAP entry?
        int dummy1;
        int checksum;   // Header checksum.
        
    }
    
    GcfFragMapEntry[] fragMapEntries;
    
    class GcfFragMapEntry {
        
        int nextDataBlockIndex;	// The index of the next data block.
        
    }
    
    GcfDirectoryHeader directoryHeader;
    
    class GcfDirectoryHeader {
        int dummy0;		// Always 0x00000004
        int cacheId;		// Cache ID.
        int gcfRevision;         // GCF file version.
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
        int checksum;		// Header checksum.
        
        @Override
        public String toString() {
            return "id:" + cacheId + ", ver:" + gcfRevision + ", items:" + itemCount + ", files:" + fileCount + ", dsize:" + directorySize + ", nsize:" + nameSize + ", info1:" + info1Count + ", copy:" + copyCount + ", local:" + localCount;
        }
    }
    
    GcfDirectoryEntry[] directoryEntries;
    
    class GcfDirectoryEntry {
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
    
    tagGCFDIRECTORYMAPHEADER directoryMapHeader;
    
    //GCF Directory Map Header
    class tagGCFDIRECTORYMAPHEADER {
        int Dummy0;     // Always 0x00000001
        int Dummy1;     // Always 0x00000000
    }
    
    tagGCFDIRECTORYMAPENTRY[] directoryMapEntries;
    
    //GCF Directory Map Entry
    class tagGCFDIRECTORYMAPENTRY {
        int FirstBlockIndex;    // Index of the first data block. (N/A if == BlockCount.)
    }
    
    tagGCFCHECKSUMHEADER checksumHeader;
    
    //GCF Checksum Header
    class tagGCFCHECKSUMHEADER {
        int Dummy0;			// Always 0x00000001
        int ChecksumSize;		// Size of LPGCFCHECKSUMHEADER & LPGCFCHECKSUMMAPHEADER & in bytes.
    }
    
    tagGCFCHECKSUMMAPHEADER checksumMapHeader;
    
    //GCF Checksum Map Header
    class tagGCFCHECKSUMMAPHEADER {
        int Dummy0;			// Always 0x14893721
        int Dummy1;			// Always 0x00000001
        int ItemCount;		// Number of items.
        int ChecksumCount;		// Number of checksums.
    }
    
    tagGCFCHECKSUMMAPENTRY[] checksumMapEntries;
    
    //GCF Checksum Map Entry
    class tagGCFCHECKSUMMAPENTRY {
        int ChecksumCount;		// Number of checksums.
        int FirstChecksumIndex;	// Index of first checksum.
    }
    
    tagGCFCHECKSUMENTRY[] checksumEntries;
    
    //GCF Checksum Entry
    class tagGCFCHECKSUMENTRY {
        int Checksum;				// Checksum.
    }
    
    tagGCFDATABLOCKHEADER dataBlockHeader;
    
    //GCF Data Header
    class tagGCFDATABLOCKHEADER {
        int GCFRevision;	// GCF file version.
        int BlockCount;	// Number of data blocks.
        int BlockSize;	// Size of each data block in bytes.
        int FirstBlockOffset; // Offset to first data block.
        int BlocksUsed;	// Number of data blocks that contain data.
        int Checksum;		// Header checksum.
        
        @Override
        public String toString() {
            return "v:" + GCFRevision + ", blocks:" + BlockCount + ", size:" + BlockSize + ", ofset:0x" + Integer.toHexString(FirstBlockOffset) + ", used:" + BlocksUsed + ", check:" + Checksum;
        }
    }
    //</editor-fold>
    
    private Block getBlock(int i) {
        Block b = blocks[i];
        if(b == null) {
            try {
                long orig = rf.getFilePointer();
                rf.seek((blockHeader.pos + BlockHeader.size) + (i * Block.size));
                b = new Block();
                b.entryType = DataUtils.readLEInt(rf);
                b.fileDataOffset = DataUtils.readLEInt(rf);
                b.fileDataSize = DataUtils.readLEInt(rf);
                b.firstDataBlockIndex = DataUtils.readLEInt(rf);
                b.nextBlockEntryIndex = DataUtils.readLEInt(rf);
                b.previousBlockEntryIndex = DataUtils.readLEInt(rf);
                b.directoryIndex = DataUtils.readLEInt(rf);
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
        rf = new RandomAccessFile(file, "r");
        
        //<editor-fold defaultstate="collapsed" desc="Header">
        header = new Header();
        header.dummy0 = DataUtils.readLEInt(rf);
        header.dummy1 = DataUtils.readLEInt(rf);
        header.gcfVersion = DataUtils.readLEInt(rf);
        header.CacheId = DataUtils.readLEInt(rf);
        header.gcfRevision = DataUtils.readLEInt(rf);
        header.dummy3 = DataUtils.readLEInt(rf);
        header.dummy4 = DataUtils.readLEInt(rf);
        header.fileSize = DataUtils.readLEInt(rf);
        header.blockSize = DataUtils.readLEInt(rf);
        header.blockCount = DataUtils.readLEInt(rf);
        header.dummy5 = DataUtils.readLEInt(rf);
        //</editor-fold>

        // TODO: make faster - generally takes around 15 seconds. Store location and read on demand?
        //<editor-fold defaultstate="collapsed" desc="Blocks">
        blockHeader = new BlockHeader(rf.getFilePointer());
        blockHeader.blockCount = DataUtils.readLEInt(rf);
        blockHeader.blocksUsed = DataUtils.readLEInt(rf);
        blockHeader.dummy0 = DataUtils.readLEInt(rf);
        blockHeader.dummy1 = DataUtils.readLEInt(rf);
        blockHeader.dummy2 = DataUtils.readLEInt(rf);
        blockHeader.dummy3 = DataUtils.readLEInt(rf);
        blockHeader.dummy4 = DataUtils.readLEInt(rf);
        blockHeader.checksum = DataUtils.readLEInt(rf);
        
        blocks = new Block[blockHeader.blockCount];

        rf.skipBytes(Block.size * blockHeader.blockCount);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Fragmap">
        fragMap = new GcfFragMapHeader();
        fragMap.blockCount = DataUtils.readLEInt(rf);
        fragMap.dummy0 = DataUtils.readLEInt(rf);
        fragMap.dummy1 = DataUtils.readLEInt(rf);
        fragMap.checksum = DataUtils.readLEInt(rf);
        
        boolean skipFrag = false;
        if(skipFrag) {
            rf.skipBytes(4 * fragMap.blockCount);
        } else {
            fragMapEntries = new GcfFragMapEntry[fragMap.blockCount];
            for(int i = 0; i < fragMap.blockCount; i++) {
//                if(i % 10000 == 0) {
//                    logger.log(Level.INFO, "Loading Fragmap entry {0}/{1}", new Object[]{i + 1, fragMapHeader.blockCount});
//                }
                GcfFragMapEntry fe = new GcfFragMapEntry();
                fe.nextDataBlockIndex = DataUtils.readLEInt(rf);
                
                fragMapEntries[i] = fe;
            }
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Directories">
        directoryHeader = new GcfDirectoryHeader();
        directoryHeader.dummy0 = DataUtils.readLEInt(rf);
        directoryHeader.cacheId = DataUtils.readLEInt(rf);
        directoryHeader.gcfRevision = DataUtils.readLEInt(rf);
        directoryHeader.itemCount = DataUtils.readLEInt(rf);
        directoryHeader.fileCount = DataUtils.readLEInt(rf);
        directoryHeader.dummy1 = DataUtils.readLEInt(rf);
        directoryHeader.directorySize = DataUtils.readLEInt(rf);
        directoryHeader.nameSize = DataUtils.readLEInt(rf);
        directoryHeader.info1Count = DataUtils.readLEInt(rf);
        directoryHeader.copyCount = DataUtils.readLEInt(rf);
        directoryHeader.localCount = DataUtils.readLEInt(rf);
        directoryHeader.dummy2 = DataUtils.readLEInt(rf);
        directoryHeader.dummy3 = DataUtils.readLEInt(rf);
        directoryHeader.checksum = DataUtils.readLEInt(rf);
        
//        logger.info(directoryHeader.toString());
        
        boolean skipDirs = false;
        if(skipDirs) {
            rf.skipBytes(directoryHeader.directorySize - (14 * 4));
        } else {
            directoryEntries = new GcfDirectoryEntry[directoryHeader.itemCount];
            for(int i = 0; i < directoryHeader.itemCount; i++) {
//                if(i % 10000 == 0) {
//                    logger.log(Level.INFO, "Loading entry {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
//                }
                GcfDirectoryEntry de = new GcfDirectoryEntry();
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
        
        //<editor-fold defaultstate="collapsed" desc="Directory Map">
        directoryMapHeader = new GcfFile.tagGCFDIRECTORYMAPHEADER();
        directoryMapHeader.Dummy0 = DataUtils.readLEInt(rf);
        directoryMapHeader.Dummy1 = DataUtils.readLEInt(rf);
        
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
        
        //<editor-fold defaultstate="collapsed" desc="Checksums">
        checksumHeader = new GcfFile.tagGCFCHECKSUMHEADER();
        checksumHeader.Dummy0 = DataUtils.readLEInt(rf);
        checksumHeader.ChecksumSize = DataUtils.readLEInt(rf);
//        logger.log(Level.INFO, "csize:{0}", checksumHeader.ChecksumSize);
        
        checksumMapHeader = new GcfFile.tagGCFCHECKSUMMAPHEADER();
        checksumMapHeader.Dummy0 = DataUtils.readLEInt(rf);
        checksumMapHeader.Dummy1 = DataUtils.readLEInt(rf);
        checksumMapHeader.ItemCount = DataUtils.readLEInt(rf);
        checksumMapHeader.ChecksumCount = DataUtils.readLEInt(rf);
//        logger.log(Level.INFO, "items:{0}, checks:{1}", new Object[]{checksumMapHeader.ItemCount, checksumMapHeader.ChecksumCount});
        
        checksumMapEntries = new tagGCFCHECKSUMMAPENTRY[checksumMapHeader.ItemCount];
        for(int i = 0; i < checksumMapHeader.ItemCount; i++) {
            tagGCFCHECKSUMMAPENTRY cme = new GcfFile.tagGCFCHECKSUMMAPENTRY();
            cme.ChecksumCount = DataUtils.readLEInt(rf);
            cme.FirstChecksumIndex = DataUtils.readLEInt(rf);
            checksumMapEntries[i] = cme;
        }
        
        checksumEntries = new tagGCFCHECKSUMENTRY[checksumMapHeader.ChecksumCount + 0x20];
        for(int i = 0; i < checksumEntries.length; i++) {
//            if(i % 10000 == 0) {
//                logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, checksumMapEntries.ChecksumCount});
//            }
            tagGCFCHECKSUMENTRY ce = new tagGCFCHECKSUMENTRY();
            ce.Checksum = DataUtils.readLEInt(rf);
            checksumEntries[i] = ce;
        }
        //</editor-fold>

        // TODO: Slow. Takes about 73 seconds
        //<editor-fold defaultstate="collapsed" desc="Data">
        dataBlockHeader = new tagGCFDATABLOCKHEADER();
        dataBlockHeader.GCFRevision = DataUtils.readLEInt(rf);
        dataBlockHeader.BlockCount = DataUtils.readLEInt(rf);
        dataBlockHeader.BlockSize = DataUtils.readLEInt(rf);
        dataBlockHeader.FirstBlockOffset = DataUtils.readLEInt(rf);
        dataBlockHeader.BlocksUsed = DataUtils.readLEInt(rf);
        dataBlockHeader.Checksum = DataUtils.readLEInt(rf);
        
        boolean skipRead = true;
        if(skipRead) {
            rf.seek(dataBlockHeader.FirstBlockOffset + (dataBlockHeader.BlockCount * dataBlockHeader.BlockSize));
        } else {
            logger.info("Loading Data ...");
            rf.seek(dataBlockHeader.FirstBlockOffset);
            byte[] b = new byte[dataBlockHeader.BlockSize];
            for(int i = 0; i < dataBlockHeader.BlockCount; i++) {
                rf.read(b);
            }
        }
        //</editor-fold>
        
//        rf.close();
    }
    
    private String nameForDirectoryIndex(byte[] b, int idx) {
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
    
    private void test() throws IOException {
        File root = new File("/home/andrew/test/");
        root.renameTo(new File("/home/andrew/deleteme"));
        root.mkdir();
        for(int i = 0; i < directoryEntries.length; i++) {
//        for(int i = 320; i < 400; i++) {
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
                    long pos = ((long)dataBlockHeader.FirstBlockOffset + ((long)dataIdx * (long)dataBlockHeader.BlockSize));
                    rf.seek(pos);
                    byte[] buf = new byte[dataBlockHeader.BlockSize];
                    if(block.fileDataOffset != 0) {
                        logger.log(Level.INFO, "off = {0}", block.fileDataOffset);
                    }
                    rf.read(buf);
                    out.seek(block.fileDataOffset + (q * dataBlockHeader.BlockSize));
                    if(out.getFilePointer() + buf.length > block.fileDataSize) {
//                        out.write(buf, 0, (block.fileDataSize % h.BlockSize));
                    } else {
//                        out.write(buf);
                    }

                    dataIdx = fragMapEntries[dataIdx].nextDataBlockIndex;
                    if(dataIdx == -1) {
                        break;
                    }
                }
                out.close();
            }
        }
    }
    
    public static GcfFile load(File file) throws IOException {
        return new GcfFile(file);
    }
    
    public static void main(String[] args) {
        try {
            load(new File(EditorFrame.locateSteamAppsDirectory() + "team fortress 2 content.gcf")).test();
    //        load(new File(EditorFrame.locateSteamAppsDirectory() + "team fortress 2 materials.gcf"));
        } catch (IOException ex) {
            Logger.getLogger(GcfFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}