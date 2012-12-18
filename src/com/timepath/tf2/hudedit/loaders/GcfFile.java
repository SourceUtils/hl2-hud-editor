package com.timepath.tf2.hudedit.loaders;

import com.timepath.tf2.hudedit.gui.EditorFrame;
import com.timepath.tf2.hudedit.util.DataUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * http://www.wunderboy.org/docs/gcfformat.php
 * 
 * @author TimePath
 */
public class GcfFile {
    
    private static final Logger logger = Logger.getLogger(GcfFile.class.getName());
    
    Header header;
    
    private class Header {
    
        int dummy0;     // Always 0x00000001
        int dummy1;     // Always 0x00000001
        int dummy2;     // Always 0x00000005. Apparently - TF2 is 6
        int CacheId;
        int gcfVersion;
        int dummy3;
        int dummy4;
        int fileSize;	// Total size of GCF file in bytes.
        int blockSize;  // Size of each data block in bytes.
        int blockCount; // Number of data blocks.
        int dummy5;
        
        @Override
        public String toString() {
            return "id:" + CacheId + ", ver:" + gcfVersion + ", size:" + fileSize + ", blockSize:" + blockSize + ", blocks:" + blockCount;
        }
    
    }
    
    BlockHeader blockHeader;
    
    private class BlockHeader {
        
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
    
    public class Block {
        
        int entryType;                  // Flags for the block entry.  0x200F0000 == Not used.
        int fileDataOffset;		// The offset for the data contained in this block entry in the file.
        int fileDataSize;		// The length of the data in this block entry.
        int firstDataBlockIndex;	// The index to the first data block of this block entry's data.
        int nextBlockEntryIndex;	// The next block entry in the series.  (N/A if == BlockCount.)
        int previousBlockEntryIndex;	// The previous block entry in the series.  (N/A if == BlockCount.)
        int directoryIndex;		// The index of the block entry in the directory.
        
    }
    
    GcfFragMapHeader fragMapHeader;
    
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
	int gcfVersion;         // GCF file version.
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
            return "id:" + cacheId + ", ver:" + gcfVersion + ", items:" + itemCount + ", files:" + fileCount + ", dsize:" + directorySize + ", nsize:" + nameSize + ", info1:" + info1Count + ", copy:" + copyCount + ", local:" + localCount;
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

    //GCF Directory Info 1 Entry
    class tagGCFDIRECTORYINFO1ENTRY {
            int Dummy0;
            
            @Override
            public String toString() {
                return "" + (char) Dummy0;
            }
    }

    //GCF Directory Info 2 Entry
    class tagGCFDIRECTORYINFO2ENTRY {
	int Dummy0;
    }

    //GCF Directory Copy Entry
    class tagGCFDIRECTORYCOPYENTRY {
	int DirectoryIndex;	// Index of the directory item.
    }

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
    
    //GCF Data Header
    class tagGCFDATABLOCKHEADER {
	int GCFVersion;	// GCF file version.
	int BlockCount;	// Number of data blocks.
	int BlockSize;	// Size of each data block in bytes.
	int FirstBlockOffset; // Offset to first data block.
	int BlocksUsed;	// Number of data blocks that contain data.
	int Checksum;		// Header checksum.
        
        @Override
        public String toString() {
            return "v:" + GCFVersion + ", blocks:" + BlockCount + ", size:" + BlockSize + ", ofset:0x" + Long.toHexString(FirstBlockOffset) + ", used:" + BlocksUsed + ", check:" + Checksum;
        }
    }
    
    public GcfFile(RandomAccessFile rf) throws IOException {
        //<editor-fold defaultstate="collapsed" desc="Header">
        logger.info("Loading Header");
        header = new Header();
        header.dummy0 = DataUtils.readLEInt(rf);
        header.dummy1 = DataUtils.readLEInt(rf);
        header.dummy2 = DataUtils.readLEInt(rf);
        header.CacheId = DataUtils.readLEInt(rf);
        header.gcfVersion = DataUtils.readLEInt(rf);
        header.dummy3 = DataUtils.readLEInt(rf);
        header.dummy4 = DataUtils.readLEInt(rf);
        header.fileSize = DataUtils.readLEInt(rf);
        header.blockSize = DataUtils.readLEInt(rf);
        header.blockCount = DataUtils.readLEInt(rf);
        header.dummy5 = DataUtils.readLEInt(rf);
        logger.info(header.toString());
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Blocks">
        logger.info("Loading Blocks");
        blockHeader = new BlockHeader();
        blockHeader.blockCount = DataUtils.readLEInt(rf);
        blockHeader.blocksUsed = DataUtils.readLEInt(rf);
        blockHeader.dummy0 = DataUtils.readLEInt(rf);
        blockHeader.dummy1 = DataUtils.readLEInt(rf);
        blockHeader.dummy2 = DataUtils.readLEInt(rf);
        blockHeader.dummy3 = DataUtils.readLEInt(rf);
        blockHeader.dummy4 = DataUtils.readLEInt(rf);
        blockHeader.checksum = DataUtils.readLEInt(rf);

        boolean skipBlocks = true;
        if(skipBlocks) {
            rf.skipBytes(28 * blockHeader.blockCount);
        } else {
            blocks = new Block[blockHeader.blockCount];
            for(int i = 0; i < blockHeader.blockCount; i++) {
                if(i % 10000 == 0) {
                    logger.log(Level.INFO, "Loading block {0}/{1}", new Object[]{i + 1, blockHeader.blockCount});
                }
                Block b = new Block();
                b.entryType = DataUtils.readLEInt(rf);
                b.fileDataOffset = DataUtils.readLEInt(rf);
                b.fileDataSize = DataUtils.readLEInt(rf);
                b.firstDataBlockIndex = DataUtils.readLEInt(rf);
                b.nextBlockEntryIndex = DataUtils.readLEInt(rf);
                b.previousBlockEntryIndex = DataUtils.readLEInt(rf);
                b.directoryIndex = DataUtils.readLEInt(rf);
                
                blocks[i] = b;
            }
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Fragmap">
        logger.info("Loading Fragmap");
        fragMapHeader = new GcfFragMapHeader();
        fragMapHeader.blockCount = DataUtils.readLEInt(rf);
        fragMapHeader.dummy0 = DataUtils.readLEInt(rf);
        fragMapHeader.dummy1 = DataUtils.readLEInt(rf);
        fragMapHeader.checksum = DataUtils.readLEInt(rf);
        
        boolean skipFrag = true;
        if(skipFrag) {
            rf.skipBytes(4 * fragMapHeader.blockCount);
        } else {
            fragMapEntries = new GcfFragMapEntry[fragMapHeader.blockCount];
            for(int i = 0; i < fragMapHeader.blockCount; i++) {
                if(i % 10000 == 0) {
                    logger.log(Level.INFO, "Loading Fragmap entry {0}/{1}", new Object[]{i + 1, fragMapHeader.blockCount});
                }
                GcfFragMapEntry f = new GcfFragMapEntry();
                f.nextDataBlockIndex = DataUtils.readLEInt(rf);
                
                fragMapEntries[i] = f;
            }
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Directories">
        logger.info("Loading Directories");
        
        directoryHeader = new GcfDirectoryHeader();
        directoryHeader.dummy0 = DataUtils.readLEInt(rf);
        directoryHeader.cacheId = DataUtils.readLEInt(rf);
        directoryHeader.gcfVersion = DataUtils.readLEInt(rf);
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
        
        logger.info(directoryHeader.toString());
        
        boolean skipDirs = false;
        if(skipDirs) {
            rf.skipBytes(directoryHeader.directorySize - (14 * 4));
        } else {
            directoryEntries = new GcfDirectoryEntry[directoryHeader.itemCount];
            for(int i = 0; i < directoryHeader.itemCount; i++) {
                if(i % 10000 == 0) {
                    logger.log(Level.INFO, "Loading entry {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
                }
                GcfDirectoryEntry f = new GcfDirectoryEntry();
                f.nameOffset = DataUtils.readLEInt(rf);
                f.itemSize = DataUtils.readLEInt(rf);
                f.checksumIndex = DataUtils.readLEInt(rf);
                f.directoryType = DataUtils.readLEInt(rf);
                f.parentIndex = DataUtils.readLEInt(rf);
                f.nextIndex = DataUtils.readLEInt(rf);
                f.firstIndex = DataUtils.readLEInt(rf);
                
                directoryEntries[i] = f;
            }
            
            logger.log(Level.INFO, "Loading names ({0}) bytes", directoryHeader.nameSize);
            byte[] b = new byte[directoryHeader.nameSize];
            rf.read(b);
            boolean debugNames = false;
            if(debugNames) {
                logger.info("read, splitting...");
                String str = new String(b);
                String[] e = str.split("\0");
                for(int i = 0; i < e.length; i++) {
                    int idx = e[i].lastIndexOf('.');
                    String ext = "";
                    if(idx != -1) {
                        ext = e[i].substring(idx + 1);
                    }
                    if(!ext.equalsIgnoreCase("wav") && !ext.equalsIgnoreCase("mp3") && !ext.equalsIgnoreCase("vcd")) {
                        logger.info(e[i]);
                    }
                }
            }
            
            tagGCFDIRECTORYINFO1ENTRY[] info1Entries = new tagGCFDIRECTORYINFO1ENTRY[directoryHeader.info1Count];
            for(int i = 0; i < directoryHeader.info1Count; i++) {
                if(i % 10000 == 0) {
                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.info1Count});
                }
                tagGCFDIRECTORYINFO1ENTRY f = new tagGCFDIRECTORYINFO1ENTRY();
                f.Dummy0 = DataUtils.readLEInt(rf);
                
                info1Entries[i] = f;
            }
            
            tagGCFDIRECTORYINFO2ENTRY[] info2Entries = new tagGCFDIRECTORYINFO2ENTRY[directoryHeader.itemCount];
            for(int i = 0; i < directoryHeader.itemCount; i++) {
                if(i % 10000 == 0) {
                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
                }
                tagGCFDIRECTORYINFO2ENTRY f = new tagGCFDIRECTORYINFO2ENTRY();
                f.Dummy0 = DataUtils.readLEInt(rf);
                
                info2Entries[i] = f;
            }
            
            tagGCFDIRECTORYCOPYENTRY[] copyEntries = new tagGCFDIRECTORYCOPYENTRY[directoryHeader.copyCount];
            for(int i = 0; i < directoryHeader.copyCount; i++) {
                if(i % 10000 == 0) {
                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.copyCount});
                }
                tagGCFDIRECTORYCOPYENTRY f = new tagGCFDIRECTORYCOPYENTRY();
                f.DirectoryIndex = DataUtils.readLEInt(rf);
                
                copyEntries[i] = f;
            }
            
            tagGCFDIRECTORYLOCALENTRY[] localEntries = new tagGCFDIRECTORYLOCALENTRY[directoryHeader.localCount];
            for(int i = 0; i < directoryHeader.localCount; i++) {
                if(i % 10000 == 0) {
                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.localCount});
                }
                tagGCFDIRECTORYLOCALENTRY f = new tagGCFDIRECTORYLOCALENTRY();
                f.DirectoryIndex = DataUtils.readLEInt(rf);
                
                localEntries[i] = f;
            }
        }
        
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Directory Map">
        logger.info("Loading Directory Map");
        directoryMapHeader = new GcfFile.tagGCFDIRECTORYMAPHEADER();
        directoryMapHeader.Dummy0 = DataUtils.readLEInt(rf);
        directoryMapHeader.Dummy1 = DataUtils.readLEInt(rf);
        
        directoryMapEntries = new tagGCFDIRECTORYMAPENTRY[directoryHeader.itemCount];
        for(int i = 0; i < directoryHeader.itemCount; i++) {
            if(i % 10000 == 0) {
                logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, directoryHeader.itemCount});
            }
            tagGCFDIRECTORYMAPENTRY f = new tagGCFDIRECTORYMAPENTRY();
            f.FirstBlockIndex = DataUtils.readLEInt(rf);

            directoryMapEntries[i] = f;
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Checksums">
        logger.info("Loading Checksums");
        checksumHeader = new GcfFile.tagGCFCHECKSUMHEADER();
        checksumHeader.Dummy0 = DataUtils.readLEInt(rf);
        checksumHeader.ChecksumSize = DataUtils.readLEInt(rf);
        logger.log(Level.INFO, "csize:{0}", checksumHeader.ChecksumSize);
        
        checksumMapHeader = new GcfFile.tagGCFCHECKSUMMAPHEADER();
        checksumMapHeader.Dummy0 = DataUtils.readLEInt(rf);
        checksumMapHeader.Dummy1 = DataUtils.readLEInt(rf);
        checksumMapHeader.ItemCount = DataUtils.readLEInt(rf);
        checksumMapHeader.ChecksumCount = DataUtils.readLEInt(rf);
        logger.log(Level.INFO, "items:{0}, checks:{1}", new Object[]{checksumMapHeader.ItemCount, checksumMapHeader.ChecksumCount});
        
        checksumMapEntries = new tagGCFCHECKSUMMAPENTRY[checksumMapHeader.ItemCount];
        for(int i = 0; i < checksumMapHeader.ItemCount; i++) {
            checksumMapEntries[i] = new GcfFile.tagGCFCHECKSUMMAPENTRY();
            checksumMapEntries[i].ChecksumCount = DataUtils.readLEInt(rf);
            checksumMapEntries[i].FirstChecksumIndex = DataUtils.readLEInt(rf);
        }
        
        for(int i = 0; i < checksumMapHeader.ChecksumCount + 0x20; i++) {

//            checksumEntries = new tagGCFCHECKSUMENTRY[checksumMapEntries.ChecksumCount];
//            for(int i = 0; i < checksumMapEntries.ChecksumCount; i++) {
//                if(i % 10000 == 0) {
//                    logger.log(Level.INFO, "Loading info1 {0}/{1}", new Object[]{i + 1, checksumMapEntries.ChecksumCount});
//                }
                tagGCFCHECKSUMENTRY f = new tagGCFCHECKSUMENTRY();
//                f.Checksum = DataUtils.readLEInt(rf);
                rf.skipBytes(4);

//                checksumEntries[i] = f;
//            }
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Data">
        logger.info("Loading Data Header");
        tagGCFDATABLOCKHEADER h = new tagGCFDATABLOCKHEADER();
        h.GCFVersion = DataUtils.readLEInt(rf);
        h.BlockCount = DataUtils.readLEInt(rf);
        h.BlockSize = DataUtils.readLEInt(rf);
        h.FirstBlockOffset = DataUtils.readLEInt(rf);
        h.BlocksUsed = DataUtils.readLEInt(rf);
        h.Checksum = DataUtils.readLEInt(rf);
        logger.info(h.toString());
        
        rf.seek(h.FirstBlockOffset + (h.BlockCount * h.BlockSize));
        //</editor-fold>
        
        logger.log(Level.INFO, "0x{0} vs 0x{1}", new Object[]{Long.toHexString(rf.getFilePointer()), Long.toHexString(rf.length())});
        rf.close();
    }
    
    public static void load(File file) {
        RandomAccessFile rf;
        try {
            rf = new RandomAccessFile(file, "r");
            GcfFile g = new GcfFile(rf);
//            System.out.println("Loading " + path + "...");

//            logger.info(g.toString());
        } catch (Exception e) {
            logger.severe(e.toString());
        }
    }
    
    public static void main(String[] args) {
        load(new File(EditorFrame.locateSteamAppsDirectory() + "team fortress 2 content.gcf"));
    }
    
}
