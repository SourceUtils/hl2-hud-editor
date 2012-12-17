package com.timepath.tf2.hudedit.loaders;

import com.timepath.tf2.hudedit.util.DataUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * http://www.wunderboy.org/docs/gcfformat.php
 * 
 * @author andrew
 */
public class GcfFile {
    
    private static final Logger LOG = Logger.getLogger(GcfFile.class.getName());
    
    Header header;
    
    private class Header {
    
        int dummy0;		// Always 0x00000001
        int dummy1;		// Always 0x00000001
        int dummy2;		// Always 0x00000005
        int CacheId;
        int gcfVersion;
        int dummy3;
        int dummy4;
        int fileSize;		// Total size of GCF file in bytes.
        int blockSize;	// Size of each data block in bytes.
        int blockCount;	// Number of data blocks.
        int dummy5;
    
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
	int checksum;		// Header checksum. The checksum is simply the sum total of all the preceeding DWORDs in the header.
        
    }
    
    Block[] blocks;
    
    public class Block {
        
        int entryType;		// Flags for the block entry.  0x200F0000 == Not used.
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
	int checksum;		// Header checksum.
        
    }
    
    GcfFragMapEntry[] fragMapEntries;
    
    class GcfFragMapEntry {
        
	int nextDataBlockIndex;	// The index of the next data block.
        
    }
    
    GcfDirectoryHeader directoryHeader;
    
    class GcfDirectoryHeader {
	int dummy0;		// Always 0x00000004
	int cacheId;		// Cache ID.
	int gcfVersion;	// GCF file version.
	int itemCount;	// Number of items in the directory.	
	int fileCount;	// Number of files in the directory.
	int dummy1;		// Always 0x00008000
	int directorySize;	// Size of lpGCFDirectoryEntries & lpGCFDirectoryNames & lpGCFDirectoryInfo1Entries & lpGCFDirectoryInfo2Entries & lpGCFDirectoryCopyEntries & lpGCFDirectoryLocalEntries in bytes.
	int nameSize;		// Size of the directory names in bytes.
	int info1Count;	// Number of Info1 entires.
	int copyCount;	// Number of files to copy.
	int localCount;	// Number of files to keep local.
	int dummy2;
	int dummy3;
	int checksum;		// Header checksum.
    }
    
    GcfDirectoryEntry[] directoryEntries;

    class GcfDirectoryEntry {
	int nameOffset;	// Offset to the directory item name from the end of the directory items.
	int itemSize;		// Size of the item.  (If file, file size.  If folder, num items.)
	int checksumIndex;	// Checksum index. (0xFFFFFFFF == None).
	int directoryType;	// Flags for the directory item.  (0x00000000 == Folder).
	int parentIndex;	// Index of the parent directory item.  (0xFFFFFFFF == None).
	int nextIndex;	// Index of the next directory item.  (0x00000000 == None).
	int firstIndex;	// Index of the first directory item.  (0x00000000 == None).
    }

    //GCF Directory Info 1 Entry
    class tagGCFDIRECTORYINFO1ENTRY {
            int Dummy0;
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
    
    @Override
    public String toString() {
        String ret = "Dummy0: " + header.dummy0 + "\n";
        ret += "Dummy1: " + header.dummy1 + "\n";
        ret += "Dummy2: " + header.dummy2 + "\n";
        ret += "CacheID: " + header.CacheId + "\n";
        ret += "GCFVersion: " + header.gcfVersion + "\n";
        ret += "Dummy3: " + header.dummy3 + "\n";
        ret += "Dummy4: " + header.dummy4 + "\n";
        ret += "FileSize: " + header.fileSize + "\n";
        ret += "BlockSize: " + header.blockSize + "\n";
        ret += "BlockCount: " + header.blockCount + "\n";
        ret += "Dummy5: " + header.dummy5 + "\n";
        return ret;
    }
    
    public GcfFile(RandomAccessFile rf) throws IOException {
        //<editor-fold defaultstate="collapsed" desc="Header">
        LOG.info("Loading Header");
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
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Blocks">
        LOG.info("Loading Blocks");
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
                    LOG.log(Level.INFO, "Loading block {0}/{1}", new Object[]{i + 1, blockHeader.blockCount});
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
        LOG.info("Loading Fragmap");
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
                    LOG.log(Level.INFO, "Loading fragmap {0}/{1}", new Object[]{i + 1, fragMapHeader.blockCount});
                }
                GcfFragMapEntry f = new GcfFragMapEntry();
                f.nextDataBlockIndex = DataUtils.readLEInt(rf);
                
                fragMapEntries[i] = f;
            }
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Directories">
        LOG.info("Loading Directories");
        
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
        
        // TODO: entries
        rf.skipBytes(directoryHeader.directorySize - (14 * 4)); // from start of directoryHeader
        //</editor-fold>
        
        LOG.info(Long.toHexString(rf.getFilePointer()));
    }
    
    public static void load(File file) {
        RandomAccessFile rf;
        try {
            rf = new RandomAccessFile(file, "r");
            GcfFile g = new GcfFile(rf);
//            System.out.println("Loading " + path + "...");

            LOG.info(g.toString());
        } catch (Exception e) {
            LOG.severe(e.toString());
        }
    }
    
    public static void main(String[] args) {
        load(new File("/media/Storage/team fortress 2 materials.gcf"));
    }
    
}
