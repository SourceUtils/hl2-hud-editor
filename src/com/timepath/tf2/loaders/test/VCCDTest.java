package com.timepath.tf2.loaders.test;

import com.timepath.tf2.hudeditor.util.Utils;
import com.timepath.tf2.loaders.GCF;
import com.timepath.tf2.loaders.VCCD;
import com.timepath.tf2.loaders.VCCD.Entry;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author timepath
 */
@SuppressWarnings("serial")
public class VCCDTest extends javax.swing.JFrame {

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
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Caption Reader");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CRC32", "Key", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTable1.setRowHeight(24);
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getColumn(0).setMinWidth(85);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(85);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(85);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(160);
        jTable1.getColumnModel().getColumn(1).setCellEditor(getKeyEditor());
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(160);

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
                loadCaptions(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem3.setText("Import");
        jMenuItem3.setEnabled(false);
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importCaptions(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem2.setText("Save");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveCaptions(evt);
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
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadCaptions(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadCaptions
        JFileChooser fc = new JFileChooser();

        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return (file.getName().startsWith("closecaption_") && (file.getName().endsWith(".dat"))) || file.isDirectory();
            }

            public String getDescription() {
                return "VCCD Files (.dat)";
            }
        };
        fc.setFileFilter(filter);

        int returnVal = fc.showOpenDialog(this);

        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            ArrayList<Entry> entries = cl.loadFile(file.getAbsolutePath().toString());
            logger.log(Level.INFO, "Entries: {0}", entries.size());

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for(int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }
            for(int i = 0; i < entries.size(); i++) {
                model.addRow(new Object[]{hexFormat(entries.get(i).getKey()), attemptDecode(entries.get(i).getKey()), entries.get(i).getValue()});
            }
        }
    }//GEN-LAST:event_loadCaptions

    private void saveCaptions(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveCaptions
        JFileChooser fc = new JFileChooser();

        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return (file.getName().startsWith("closecaption_") && (file.getName().endsWith(".dat"))) || file.isDirectory();
            }

            public String getDescription() {
                return "VCCD Files (.dat)";
            }
        };
        fc.setFileFilter(filter);

        int returnVal = fc.showSaveDialog(this);

        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            ArrayList<Entry> entries = new ArrayList<Entry>();
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for(int i = 0; i < model.getRowCount(); i++) {
                Entry e = cl.getNewEntry();
                e.setKey(Long.parseLong(model.getValueAt(i, 0).toString().toLowerCase(), 16));
                e.setValue(model.getValueAt(i, 2).toString());
                entries.add(e);
            }
            cl.saveFile(file.getAbsolutePath().toString(), entries);
        }
    }//GEN-LAST:event_saveCaptions

    private void importCaptions(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importCaptions
        JFileChooser fc = new JFileChooser();

        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return (file.getName().startsWith("closecaption_") && (file.getName().endsWith(".txt"))) || file.isDirectory();
            }

            public String getDescription() {
                return "VCCD Source Files (.txt)";
            }
        };
        fc.setFileFilter(filter);

        int returnVal = fc.showOpenDialog(this);

        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            ArrayList<Entry> entries = cl.importFile(file.getAbsolutePath().toString());
            logger.log(Level.INFO, "Entries: {0}", entries.size());

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for(int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }
            for(int i = 0; i < entries.size(); i++) {
                model.addRow(new Object[]{hexFormat(entries.get(i).getKey()), attemptDecode(entries.get(i).getKey()), entries.get(i).getValue()});
            }
        }
    }//GEN-LAST:event_importCaptions

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables

    private VCCD cl;

    //<editor-fold defaultstate="collapsed" desc="Entry point">
    /**
     * Creates new form CaptionLoaderFrame
     */
    public VCCDTest() {
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
                jTextField4.setText(hexFormat(takeCRC32(jTextField3.getText())));
            }
        });

        cl = new VCCD();
    }

    TableCellEditor getKeyEditor() {
        JComboBox comboBox = new JComboBox();
        comboBox.setEditable(true);
        hashmap = generateHash();
        Object[] vals = hashmap.values().toArray();
        Arrays.sort(vals);
        for(int i = 0; i < vals.length; i++) {
            comboBox.addItem(vals[i]);
        }
        return new DefaultCellEditor(comboBox);
    }

    public class EditorPaneRenderer extends JPanel implements TableCellRenderer {

        private int curX;

        private String text;

        public EditorPaneRenderer() {
            super();
        }

        @Override
        public void paint(Graphics g) {
            g.setFont(this.getFont());
            FontMetrics fm = g.getFontMetrics();
            g.setColor(this.getBackground());
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.setColor(this.getForeground());

            for(int i = 0; i < text.length(); i++) {
                if(text.charAt(i) == '<') {
                }
            }

            drawWords(fm, g, text);
            curX = 0;
        }

        public void drawWords(FontMetrics fm, Graphics g, String str) {
            g.drawString(text, curX, fm.getHeight());
            curX += fm.stringWidth(str);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            if(isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            return this;
        }

        private void setText(String string) {
            this.text = string;
        }
    }

    private TableCellRenderer valueRenderer = new EditorPaneRenderer();

    private static final Logger logger = Logger.getLogger(VCCDTest.class.getName());

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
        f.setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final VCCDTest c = new VCCDTest();
                c.setLocationRelativeTo(null);
                c.setVisible(true);
                f.dispose();
            }
        }).start();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Hash codes">
    private HashMap<Integer, String> hashmap;

    private HashMap<Integer, String> generateHash() {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        logger.info("Generating hash codes ...");
        try {
            GCF gcf = new GCF(new File(Utils.locateSteamAppsDirectory() + "/Team Fortress 2 Content.gcf"));

            CRC32 crc = new CRC32();

            String all = new String(gcf.ls);
            String[] ls = gcf.getEntryNames();
            for(int i = 0; i < ls.length; i++) {
                int end = ls[i].length();
                int ext = ls[i].lastIndexOf('.');
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
//                    System.out.println(str);
                    crc.update(str.toLowerCase().getBytes());
                    map.put((int) crc.getValue(), str.toLowerCase()); // enforce lowercase for consistency
//                    logger.log(Level.INFO, "{0} > {1}", new Object[]{crc.getValue(), str});
                    crc.reset();
                } else {
//                    logger.info(ls[i]);
                }
            }
        } catch(IOException ex) {
            logger.log(Level.WARNING, "Error generating hash codes", ex);
        }
        return map;
    }

    public static int takeCRC32(String in) {
        CRC32 crc = new CRC32();
        crc.update(in.toLowerCase().getBytes());
        return (int) crc.getValue();
    }

    public static String hexFormat(int in) {
        return Integer.toHexString(in).toUpperCase();
    }

    private String attemptDecode(int hash) {
        if(!hashmap.containsKey(hash)) {
//            logger.log(Level.INFO, "hashmap does not contain {0}", hash);
            return null;
        }
        return hashmap.get(hash);
    }
    //</editor-fold>
}