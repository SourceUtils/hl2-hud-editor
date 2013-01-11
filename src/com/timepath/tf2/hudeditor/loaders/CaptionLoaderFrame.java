package com.timepath.tf2.hudeditor.loaders;

import com.timepath.tf2.hudeditor.Utils;
import com.timepath.tf2.hudeditor.util.DataUtils;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author TimePath
 */
public class CaptionLoaderFrame extends javax.swing.JFrame {
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Caption Reader");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Key (or CRC32)", "Value"
            }
        ));
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("CRC32"));

        jTextField4.setEditable(false);
        jTextField4.setText("0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField4)
                    .addComponent(jTextField3))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 14, Short.MAX_VALUE))
        );

        jMenu1.setText("File");

        jMenuItem1.setText("Open");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                load(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Save");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void load(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_load
        JFileChooser fc = new JFileChooser();
        
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return (file.getName().startsWith("closecaption_") && (file.getName().endsWith(".dat") || file.getName().endsWith(".txt"))) || file.isDirectory();
            }
            public String getDescription() {
                return "VCCD Files";
            }
        };
        fc.setFileFilter(filter);
        
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            ArrayList<String> s = cl.load(file.getAbsolutePath().toString());
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for(int i = model.getRowCount() - 1; i >= 0 ; i--) {
                model.removeRow(i);
            }

            for(int i = 0; i < s.size(); i+=2) {
                model.addRow(new Object[]{s.get(i), s.get(i + 1)});
            } 
        } else {
            
        }
    }//GEN-LAST:event_load

    private void save(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save
        
    }//GEN-LAST:event_save
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables

    private static final Logger logger = Logger.getLogger(CaptionLoader.class.getName());
    
    private final CaptionLoader cl;

    /**
     * Creates new form CaptionLoaderFrame
     */
    public CaptionLoaderFrame() {
        initComponents();
        
        jTextField3.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                update();
            }
            public void removeUpdate(DocumentEvent e) {
                update();
            }
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void update() {
                jTextField4.setText(takeCRC32(jTextField3.getText()));
            }
        });
        
        cl = new CaptionLoader();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String... args) {
        final JFrame f = new JFrame("Loading caption reader...");
        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        f.add(pb);
        f.setMinimumSize(new Dimension(300, 50));
        f.setLocationRelativeTo(null);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                f.setVisible(true);
            }
        }).start();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                CaptionLoaderFrame c = new CaptionLoaderFrame();
                c.setLocationRelativeTo(null);
                c.setVisible(true);
                f.dispose();
            }
        }).start();
    }
    
    private static byte[] captionId = "VCCD".getBytes();
    private static int captionVer = 1;
    
    public static String takeCRC32(String in) {
        CRC32 crc = new CRC32();
        crc.update(in.toLowerCase().getBytes());
        return Integer.toHexString((int)crc.getValue()).toUpperCase();
    }

    /**
     * Used for writing captions
     * @param curr
     * @param round
     * @return 
     */
    private static long alignValue(long curr, int round) {
        return (long) (Math.ceil(curr/round) * round);
    }
    
    private class CaptionLoader {

        public CaptionLoader() {
            generateHash();
        }

        private HashMap<Integer, String> hashmap = new HashMap<Integer, String>();

        private void generateHash() {
            logger.info("Generating hash codes ...");
            try {
                GcfFile gcf = GcfFile.load(new File(Utils.locateSteamAppsDirectory() + "team fortress 2 content.gcf"));

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
//                        System.out.println(str);
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
                logger.log(Level.INFO, "Header: {0}, Version: {1}, Blocks: {2}, BlockSize: {3}, DirectorySize: {4}, DataOffset: {5}", new Object[]{magic, ver, blocks, blockSize, directorySize, dataOffset}); // blocks = currblock + 1 (from writing)
                Entry[] entries = new Entry[directorySize];
                for(int i = 0; i < directorySize; i++) {
                    entries[i] = new Entry(DataUtils.readULong(rf), DataUtils.readLEInt(rf), DataUtils.readUShort(rf), DataUtils.readUShort(rf));
                }
                rf.seek(dataOffset);
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
        
        public void save(String file, ArrayList<String> s) {
            if(file == null) {
                return;
            }
            try {
                int blocks = 0;
                int blockSize = 8192;
                int directorySize = 1;
                int ptrAfterEntries = (6 * 4) + (directorySize * 12);
                int dataOffset = ptrAfterEntries + ((int)alignValue(ptrAfterEntries, 512) - ptrAfterEntries);
                
                RandomAccessFile rf = new RandomAccessFile(file, "r");
                rf.write(captionId); // header
                rf.writeInt(1); // version
                rf.writeInt(blocks);
                rf.writeInt(blockSize);
                rf.writeInt(directorySize);
                rf.writeInt(dataOffset);
                
    //            logger.log(Level.INFO, "Header: {0}, Version: {1}, Blocks: {2}, BlockSize: {3}, DirectorySize: {4}, DataOffset: {5}", new Object[]{magic, ver, blocks, blockSize, directorySize, dataOffset}); // blocks = currblock + 1 (from writing)
                Entry[] entries = new Entry[directorySize];
                for(int i = 0; i < directorySize; i++) {
                    Entry e = entries[i];
                    e = new Entry(0, 0, 0, 5); // Hello
                    rf.writeInt(e.hash);
                    rf.writeInt(e.block);
                    rf.writeShort(e.offset);
                    rf.writeShort(e.length);
                }
                rf.seek(dataOffset);
                for(int i = 0; i < directorySize; i++) {
                    rf.seek(dataOffset + (entries[i].block * blockSize) + entries[i].offset);
                    String string = "Hello";
                    byte[] out = string.getBytes();
                    for(int j = 0; j < out.length; j++) {
                        rf.write(out[i]);
                        rf.skipBytes(1);
                    }
                    rf.skipBytes(2); // \0\0 padding
    //                logger.log(Level.INFO, "{0} = {1}", new Object[]{entries[i], sb.toString()});
                }
                rf.close(); // The rest of the file is garbage
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
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

}
