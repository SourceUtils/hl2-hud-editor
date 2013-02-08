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

    private static final Logger LOG = Logger.getLogger(GCF.class.getName());

    //<editor-fold defaultstate="collapsed" desc="Utils">
    public static void analyze(final File file, final DefaultMutableTreeNode top) {
        try {
            GCF g = new GCF(file);
            String[] entries = g.getEntries();
            for(int i = 0; i < entries.length; i++) {
                top.add(new DefaultMutableTreeNode(entries[i]));
            }
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
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
        LOG.log(Level.INFO, "Extracting {0}", search);
        for(int i = 0; i < directoryEntries.length; i++) {
            String str = nameForDirectoryIndex(i);
            if(!str.equals(search)) {
                continue;
            }
            return extract(i, dest);
        }
        LOG.log(Level.INFO, "Could not extract {0}", search);
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
            int idx = this.directoryMapEntries(index).firstBlockIndex;
            //                logger.log(Level.INFO, "\n\np:{0}\nblockidx:{1}\n", new Object[]{f.getPath(), idx});
            //                logger.log(Level.INFO, "\n\nb:{0}\n", new Object[]{block.toString()});
            outFile.getParentFile().mkdirs();
            outFile.createNewFile();
            if(idx >= blocks.length) {
                LOG.log(Level.WARNING, "Block out of range for {0} : {1}. Is the size 0?", new Object[]{outFile.getPath(), index});
                return null;
            }
            BlockAllocationTableEntry block = this.getBlock(idx);
            RandomAccessFile out = new RandomAccessFile(outFile, "rw");
            int dataIdx = block.firstClusterIndex;
            for(int q = 0;; q++) {
                long pos = ((long) dataBlockHeader.firstBlockOffset + ((long) dataIdx * (long) dataBlockHeader.blockSize));
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
                dataIdx = this.getEntry(dataIdx).nextClusterIndex;
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

    //<editor-fold defaultstate="collapsed" desc="Core utils">
    public BlockAllocationTableEntry getBlock(int i) throws IOException {
        BlockAllocationTableEntry bae = blocks[i];
        if(bae == null) {
            rf.seek((blockAllocationTableHeader.pos + BlockAllocationTableHeader.SIZE) + (i * BlockAllocationTableEntry.SIZE));
            bae = new BlockAllocationTableEntry();
            blocks[i] = bae;
        }
        return bae;
    }

    public FileAllocationTableEntry getEntry(int i) throws IOException {
        FileAllocationTableEntry fae = fragMapEntries[i];
        if(fae == null) {
            rf.seek(fragMap.pos + FileAllocationTableHeader.SIZE + (i * FileAllocationTableEntry.SIZE));
            return (fragMapEntries[i] = new FileAllocationTableEntry());
        }
        return fae;
    }

    public DirectoryMapEntry directoryMapEntries(int i) throws IOException {
        DirectoryMapEntry dme = directoryMapEntries[i];
        if(dme == null) {
            rf.seek(directoryMapHeader.pos + DirectoryMapHeader.SIZE + (i * DirectoryMapEntry.SIZE));
            return (directoryMapEntries[i] = new DirectoryMapEntry());
        }
        return dme;
    }

    public ChecksumMapEntry checksumMapEntries(int i) throws IOException {
        ChecksumMapEntry cme = checksumMapEntries[i];
        if(cme == null) {
            rf.seek(directoryMapHeader.pos + ChecksumMapHeader.SIZE + (i * ChecksumMapEntry.SIZE));
            return (checksumMapEntries[i] = new ChecksumMapEntry());
        }
        return cme;
    }

    public ChecksumEntry checksumEntries(int i) throws IOException {
        ChecksumEntry ce = checksumEntries[i];
        if(ce == null) {
            rf.seek(directoryMapHeader.pos + ChecksumMapHeader.SIZE + (checksumMapEntries.length * ChecksumMapEntry.SIZE) + (i * ChecksumEntry.SIZE));
            return (checksumEntries[i] = new ChecksumEntry());
        }
        return ce;
    }
    //</editor-fold>

    private final RandomAccessFile rf;

    private final File file;

    public byte[] ls = null;

    public GCF(File file) throws IOException {
        this.file = file;
        rf = new RandomAccessFile(file, "r");

        header = new FileHeader();
        blockAllocationTableHeader = new BlockAllocationTableHeader();
        fragMap = new FileAllocationTableHeader();

        //<editor-fold defaultstate="collapsed" desc="Manifest">
        manifestHeader = new ManifestHeader();
        boolean skipManifest = false;
        if(skipManifest) {
            rf.skipBytes(manifestHeader.binarySize - ManifestHeader.SIZE);
        } else {
            directoryEntries = new DirectoryEntry[manifestHeader.nodeCount];
            for(int i = 0; i < manifestHeader.nodeCount; i++) {
                directoryEntries[i] = new DirectoryEntry();
            }

            ls = DataUtils.readBytes(rf, manifestHeader.nameSize);

            info1Entries = new tagGCFDIRECTORYINFO1ENTRY[manifestHeader.hashTableKeyCount];
            for(int i = 0; i < manifestHeader.hashTableKeyCount; i++) {
                info1Entries[i] = new tagGCFDIRECTORYINFO1ENTRY();
            }

            info2Entries = new tagGCFDIRECTORYINFO2ENTRY[manifestHeader.nodeCount];
            for(int i = 0; i < manifestHeader.nodeCount; i++) {
                info2Entries[i] = new tagGCFDIRECTORYINFO2ENTRY();
            }

            copyEntries = new tagGCFDIRECTORYCOPYENTRY[manifestHeader.minimumFootprintCount];
            for(int i = 0; i < manifestHeader.minimumFootprintCount; i++) {
                tagGCFDIRECTORYCOPYENTRY f = new tagGCFDIRECTORYCOPYENTRY();
                f.DirectoryIndex = DataUtils.readULEInt(rf);

                copyEntries[i] = f;
            }

            localEntries = new tagGCFDIRECTORYLOCALENTRY[manifestHeader.userConfigCount];
            for(int i = 0; i < manifestHeader.userConfigCount; i++) {
                tagGCFDIRECTORYLOCALENTRY f = new tagGCFDIRECTORYLOCALENTRY();
                f.DirectoryIndex = DataUtils.readULEInt(rf);

                localEntries[i] = f;
            }
        }

        //</editor-fold>

        directoryMapHeader = new DirectoryMapHeader();

        checksumHeader = new GCF.ChecksumHeader();

        checksumMapHeader = new GCF.ChecksumMapHeader();

        // TODO: Slow. Takes about 73 seconds
        //<editor-fold defaultstate="collapsed" desc="Data">
        dataBlockHeader = new DataBlockHeader();

        boolean skipRead = true;
        if(skipRead) {
            rf.seek(dataBlockHeader.firstBlockOffset + (dataBlockHeader.blockCount * dataBlockHeader.blockSize));
        } else {
            LOG.info("Loading Data ...");
            rf.seek(dataBlockHeader.firstBlockOffset);
            byte[] b = new byte[dataBlockHeader.blockSize];
            for(int i = 0; i < dataBlockHeader.blockCount; i++) {
                rf.read(b);
            }
        }
        //</editor-fold>

        LOG.log(Level.INFO, "{0}\n{1}\n{2}\n{3}\n{4}\n{5}\n{6}\n{7}\n", new Object[]{file.getPath(), "header:\t" + header.toString(), "blockHchecksumeader:\t" + blockAllocationTableHeader.toString(), "fragMap:\t" + fragMap.toString(), "directoryHeader:\t" + manifestHeader.toString(), "directoryMapHeader:\t" + directoryMapHeader.toString(), "checksumHeader:\t" + checksumHeader.toString(), "dataBlockHeader:\t" + dataBlockHeader.toString()});
    }

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
        public final int headerVersion;

        /**
         * Always 0x00000001 for GCF, 0x00000002 for NCF
         */
        public final int cacheType;

        /**
         * Container version
         */
        public final int formatVersion;

        /**
         * ID of the cache
         * Can be found in the CDR section of ClientRegistry.blob
         * TF2 is 440
         */
        public final int applicationID;

        /**
         * Current revision
         */
        public final int applicationVersion;

        /**
         * Unsure
         */
        public final int isMounted;

        /**
         * Padding?
         */
        public final int dummy0;

        /**
         * Total size of GCF file in bytes
         */
        public final int fileSize;

        /**
         * Size of each data cluster in bytes
         */
        public final int clusterSize;

        /**
         * Number of data blocks
         */
        public final int clusterCount;

        /**
         * 'special' sum of all previous fields
         */
        public final int checksum;

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

        public int check() {
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
            return checked;
        }

        @Override
        public String toString() {
            int checked = check();
            String checkState = (checksum == checked) ? "OK" : checksum + "vs" + checked;
            return "id:" + applicationID + ", ver:" + formatVersion + ", rev:" + applicationVersion + ", mounted?: " + isMounted + ", size:" + fileSize + ", blockSize:" + clusterSize + ", blocks:" + clusterCount + ", checksum:" + checkState;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Block">
    public BlockAllocationTableHeader blockAllocationTableHeader;

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
        public final int blockCount;

        /**
         * Number of data blocks that point to data
         */
        public final int blocksUsed;

        /**
         *
         */
        public final int lastBlockUsed;

        /**
         *
         */
        public final int dummy0;

        /**
         *
         */
        public final int dummy1;

        /**
         *
         */
        public final int dummy2;

        /**
         *
         */
        public final int dummy3;

        /**
         * Header checksum
         * The checksum is simply the sum total of all the preceeding DWORDs in the header
         */
        public final int checksum;

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
            rf.skipBytes(blocks.length * BlockAllocationTableEntry.SIZE);
        }

        public int check() {
            int checked = 0;
            checked += DataUtils.updateChecksumAdd(blockCount);
            checked += DataUtils.updateChecksumAdd(blocksUsed);
            checked += DataUtils.updateChecksumAdd(lastBlockUsed);
            checked += DataUtils.updateChecksumAdd(dummy0);
            checked += DataUtils.updateChecksumAdd(dummy1);
            checked += DataUtils.updateChecksumAdd(dummy2);
            checked += DataUtils.updateChecksumAdd(dummy3);
            return checked;
        }

        @Override
        public String toString() {
            int checked = check();
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
        public final int entryType;

        /**
         * The offset for the data contained in this block entry in the file
         */
        public final int fileDataOffset;

        /**
         * The length of the data in this block entry
         */
        public final int fileDataSize;

        /**
         * The index to the first data block of this block entry's data
         */
        public final int firstClusterIndex;

        /**
         * The next block entry in the series
         * (N/A if == BlockCount)
         */
        public final int nextBlockEntryIndex;

        /**
         * The previous block entry in the series
         * (N/A if == BlockCount)
         */
        public final int previousBlockEntryIndex;

        /**
         * The index of the block entry in the manifest
         */
        public final int manifestIndex;

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
        public final int clusterCount;

        /**
         * Index of 1st unused GCFFRAGMAP entry?
         */
        public final int firstUnusedEntry;

        /**
         * Defines the end of block chain terminator
         * If the value is 0, then the terminator is 0x0000FFFF; if the value is 1, then the
         * terminator is 0xFFFFFFFF
         */
        public final int isLongTerminator;

        /**
         * Header checksum
         */
        public final int checksum;

        private FileAllocationTableHeader() throws IOException {
            pos = rf.getFilePointer();
            clusterCount = DataUtils.readULEInt(rf);
            firstUnusedEntry = DataUtils.readULEInt(rf);
            isLongTerminator = DataUtils.readULEInt(rf);
            checksum = DataUtils.readULEInt(rf);

            fragMapEntries = new FileAllocationTableEntry[clusterCount];
            rf.skipBytes(fragMapEntries.length * FileAllocationTableEntry.SIZE);
        }

        public int check() {
            int checked = 0;
            checked += DataUtils.updateChecksumAdd(clusterCount);
            checked += DataUtils.updateChecksumAdd(firstUnusedEntry);
            checked += DataUtils.updateChecksumAdd(isLongTerminator);
            return checked;
        }

        @Override
        public String toString() {
            int checked = check();
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
         * If == FileAllocationTableHeader.isLongTerminator, there are no more clusters in the
         * file
         */
        public final int nextClusterIndex;

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
    public ManifestHeader manifestHeader;

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

        public final int headerVersion;		// Always 0x00000004

        public final int applicationID;		// Cache ID.

        public final int applicationVersion;        // GCF file version.

        public final int nodeCount;          // Number of items in the directory.

        public final int fileCount;          // Number of files in the directory.

        public final int compressionBlockSize;		// Always 0x00008000

        /**
         * Inclusive of header
         */
        public final int binarySize;	// Size of lpGCFDirectoryEntries & lpGCFDirectoryNames & lpGCFDirectoryInfo1Entries & lpGCFDirectoryInfo2Entries & lpGCFDirectoryCopyEntries & lpGCFDirectoryLocalEntries in bytes.

        public final int nameSize;		// Size of the directory names in bytes.

        public final int hashTableKeyCount;         // Number of Info1 entires.

        public final int minimumFootprintCount;          // Number of files to copy.

        public final int userConfigCount;         // Number of files to keep local.
//        ManifestHeaderBitmask bitmask;

        public final int bitmask;

        public final int fingerprint;

        public final int checksum;

        private ManifestHeader() throws IOException {
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

        public int check() {
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
                int checked = (int) (adler32.getValue() & 0xFFFFFFFF);
                return checked;
            } catch(IOException ex) {
                Logger.getLogger(GCF.class.getName()).log(Level.SEVERE, null, ex);
            }
            return 0;
        }

        @Override
        public String toString() {
            int checked = check();
            String checkState = (checksum == checked) ? "OK" : DataUtils.toBinaryString(checksum) + " vs " + DataUtils.toBinaryString(checked);
            return Long.toHexString(pos) + " : id:" + applicationID + ", ver:" + applicationVersion + ", bitmask:0x" + Integer.toHexString(bitmask).toUpperCase() + ", items:" + nodeCount + ", files:" + fileCount + ", dsize:" + binarySize + ", nsize:" + nameSize + ", info1:" + hashTableKeyCount + ", copy:" + minimumFootprintCount + ", local:" + userConfigCount + ", check:" + checkState;
        }
    }

    private DirectoryEntry[] directoryEntries;

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

    public class DirectoryEntry {

        public final int nameOffset;         // Offset to the directory item name from the end of the directory items.

        public final int itemSize;		// Size of the item.  (If file, file size.  If folder, num items.)

        public final int checksumIndex;	// Checksum index / file ID. (0xFFFFFFFF == None).

        public final int attributes;

        public final int parentIndex;	// Index of the parent directory item.  (0xFFFFFFFF == None).

        public final int nextIndex;          // Index of the next directory item.  (0x00000000 == None).

        public final int firstChildIndex;         // Index of the first directory item.  (0x00000000 == None).

        private DirectoryEntry() throws IOException {
            nameOffset = DataUtils.readULEInt(rf);
            itemSize = DataUtils.readULEInt(rf);
            checksumIndex = DataUtils.readULEInt(rf);
            attributes = DataUtils.readULEInt(rf);
            parentIndex = DataUtils.readULEInt(rf);
            nextIndex = DataUtils.readULEInt(rf);
            firstChildIndex = DataUtils.readULEInt(rf);
        }

        @Override
        public String toString() {
            return nameOffset + ", " + itemSize + ", " + checksumIndex + ", " + attributes + ", " + parentIndex + ", " + nextIndex + ", " + firstChildIndex;
        }
    }

    private tagGCFDIRECTORYINFO1ENTRY[] info1Entries; // nameTable

    //GCF Directory Info 1 Entry
    public class tagGCFDIRECTORYINFO1ENTRY {

        /**
         * 1 * 4
         */
        public static final int SIZE = 4;

        public final int Dummy0;

        private tagGCFDIRECTORYINFO1ENTRY() throws IOException {
            Dummy0 = DataUtils.readULEInt(rf);
        }

        @Override
        public String toString() {
            return "" + (char) Dummy0;
        }
    }

    private tagGCFDIRECTORYINFO2ENTRY[] info2Entries; // hashTable

    //GCF Directory Info 2 Entry
    public class tagGCFDIRECTORYINFO2ENTRY {

        /**
         * 1 * 4
         */
        public static final int SIZE = 4;

        public final int Dummy0;

        private tagGCFDIRECTORYINFO2ENTRY() throws IOException {
            Dummy0 = DataUtils.readULEInt(rf);
        }

        @Override
        public String toString() {
            return "" + (char) Dummy0;
        }
    }

    private tagGCFDIRECTORYCOPYENTRY[] copyEntries; // TODO

    //GCF Directory Copy Entry
    class tagGCFDIRECTORYCOPYENTRY {

        int DirectoryIndex;	// Index of the directory item.

    }

    private tagGCFDIRECTORYLOCALENTRY[] localEntries; // TODO

    //GCF Directory Local Entry
    class tagGCFDIRECTORYLOCALENTRY {

        int DirectoryIndex;	// Index of the directory item.

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Directory Map">
    public DirectoryMapHeader directoryMapHeader;

    //GCF Directory Map Header
    public class DirectoryMapHeader {

        /**
         * 2 * 4
         */
        public static final int SIZE = 8;

        public final long pos;

        public final int headerVersion;     // Always 0x00000001

        public final int dummy0;     // Always 0x00000000

        private DirectoryMapHeader() throws IOException {
            pos = rf.getFilePointer();
            headerVersion = DataUtils.readULEInt(rf);
            dummy0 = DataUtils.readULEInt(rf);

            directoryMapEntries = new DirectoryMapEntry[manifestHeader.nodeCount];
            rf.skipBytes(directoryMapEntries.length * DirectoryMapEntry.SIZE);
        }

        @Override
        public String toString() {
            return "headerVersion:" + headerVersion + ", Dummy0:" + dummy0;
        }
    }

    private DirectoryMapEntry[] directoryMapEntries;

    //GCF Directory Map Entry
    public class DirectoryMapEntry {

        /**
         * 1 * 4
         */
        public static final int SIZE = 4;

        public final int firstBlockIndex;    // Index of the first data block. (N/A if == BlockCount.)

        private DirectoryMapEntry() throws IOException {
            firstBlockIndex = DataUtils.readULEInt(rf);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Checksum Header">
    public ChecksumHeader checksumHeader;

    //GCF Checksum Header
    public class ChecksumHeader {

        public final int headerVersion;			// Always 0x00000001

        public final int ChecksumSize;		// Size of LPGCFCHECKSUMHEADER & LPGCFCHECKSUMMAPHEADER & in bytes.
        // the number of bytes in the checksum section (excluding this structure and the following LatestApplicationVersion structure).

        private ChecksumHeader() throws IOException {
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
    public ChecksumMapHeader checksumMapHeader;

    //GCF Checksum Map Header
    public class ChecksumMapHeader {

        /**
         * 4 * 4
         */
        public static final int SIZE = 16;

        public final long pos;

        public final int formatCode;			// Always 0x14893721

        public final int Dummy1;			// Always 0x00000001

        public final int ItemCount;		// Number of items.

        public final int ChecksumCount;		// Number of checksums.

        private ChecksumMapHeader() throws IOException {
            pos = rf.getFilePointer();
            formatCode = DataUtils.readULEInt(rf);
            Dummy1 = DataUtils.readULEInt(rf);
            ItemCount = DataUtils.readULEInt(rf);
            ChecksumCount = DataUtils.readULEInt(rf);

            checksumMapEntries = new ChecksumMapEntry[ItemCount];
            rf.skipBytes(checksumMapEntries.length * ChecksumMapEntry.SIZE);

            checksumEntries = new ChecksumEntry[ChecksumCount + 0x20];
            rf.skipBytes(checksumEntries.length * ChecksumEntry.SIZE);
        }
    }

    private ChecksumMapEntry[] checksumMapEntries;

    //GCF Checksum Map Entry
    public class ChecksumMapEntry {

        /**
         * 2 * 4
         */
        public static final int SIZE = 8;

        public final int checksumCount;		// Number of checksums.

        public final int firstChecksumIndex;	// Index of first checksum.

        private ChecksumMapEntry() throws IOException {
            checksumCount = DataUtils.readULEInt(rf);
            firstChecksumIndex = DataUtils.readULEInt(rf);
        }

        @Override
        public String toString() {
            return "checkCount:" + checksumCount + ", first:" + firstChecksumIndex;
        }
    }

    private ChecksumEntry[] checksumEntries;

    //GCF Checksum Entry
    public class ChecksumEntry {

        /**
         * 1 * 4
         */
        public static final int SIZE = 4;

        public final int checksum;				// Checksum.

        private ChecksumEntry() throws IOException {
            checksum = DataUtils.readULEInt(rf);
        }

        @Override
        public String toString() {
            return "check:" + checksum;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Data blocks">
    public DataBlockHeader dataBlockHeader;

    /**
     *
     */
    public class DataBlockHeader {

        /**
         * GCF file version
         */
        public final int gcfRevision;

        /**
         * Number of data blocks
         */
        public final int blockCount;

        /**
         * Size of each data block in bytes
         */
        public final int blockSize;

        /**
         * Offset to first data block
         */
        public final int firstBlockOffset;

        /**
         * Number of data blocks that contain data
         */
        public final int blocksUsed;

        /**
         * Header checksum
         */
        public final int checksum;

        private DataBlockHeader() throws IOException {
            gcfRevision = DataUtils.readULEInt(rf);
            blockCount = DataUtils.readULEInt(rf);
            blockSize = DataUtils.readULEInt(rf);
            firstBlockOffset = DataUtils.readULEInt(rf);
            blocksUsed = DataUtils.readULEInt(rf);
            checksum = DataUtils.readULEInt(rf);
        }

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
}