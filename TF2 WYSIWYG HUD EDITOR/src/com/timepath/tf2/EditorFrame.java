package com.timepath.tf2;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import net.tomahawk.XFileDialog;



/**
 *
 * @author andrew
 */
// ctrl + click and drag is weird
// http://gamebanana.com/css/tools/4483
// http://img13.imageshack.us/img13/210/hudmanagerss.png
// http://plrf.org/superhudeditor/screens/0.3.0/superhudeditor-0.3.0-linux.jpg
// http://visualhud.pk69.com/
// http://www.javaprogrammingforums.com/java-swing-tutorials/7944-how-use-jtree-create-file-system-viewer-tree.html
// http://www.horstmann.com/articles/Taming_the_GridBagLayout.html
// http://developer.apple.com/legacy/mac/library/#technotes/tn/tn2042.html
// http://today.java.net/pub/a/today/2004/01/29/swing.html

// Credits:
// http://code.google.com/p/xfiledialog/

public class EditorFrame extends JFrame implements ActionListener {
    
    private final static OS os;
    private final static int shortcutKey;
    
    private enum OS {
        Windows, Mac, Other
    }
    static {
        String vers = System.getProperty("os.name").toLowerCase();
        if(vers.indexOf("windows") != -1) {
            os = OS.Windows;
        } else if(vers.indexOf("mac") != -1) {
            os = OS.Mac;
        } else {
            os = OS.Other;
        }
        
        if(os == OS.Windows) {
            shortcutKey = ActionEvent.CTRL_MASK;
            XFileDialog.setTraceLevel(0);
        } else if(os == OS.Mac) {
            shortcutKey = ActionEvent.META_MASK;
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name application property", "TF2 HUD Editor");
        } else {
            shortcutKey = ActionEvent.CTRL_MASK;
        }
        
//        final JOptionPane optionPane = new JOptionPane(os, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
    }
    
    private static void systemLAF() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void programLAF() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String... args) {
        programLAF();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EditorFrame e = new EditorFrame();
                e.start();
            }
        });
    }
    
    private HudCanvas canvas;
    private ResLoader resloader;
    
    private JTree fileTree;
    
    private DefaultMutableTreeNode hudFilesRoot;
    private MyJTable propTable;
    
    public EditorFrame() {
        this.setTitle("TimePath's WYSIWYG TF2 HUD Editor");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(600, 400));
        this.setPreferredSize(new Dimension(600, 400));
        this.setLocationRelativeTo(null);
        
        createMenu(this);
        
//        JPanel browser = new JPanel(new GridBagLayout());
        JSplitPane browser = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        createTree(browser);
        createProperties(browser);
        browser.setResizeWeight(0.5);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createCanvas(), browser);
//        splitPane.setDividerLocation(f.getPreferredSize().width-350);
        splitPane.setResizeWeight(0.8);
        this.add(splitPane);
        
        this.pack();
    }

    public void start() {        
        this.setVisible(true);
        this.createBufferStrategy(2);
    }
    
    private void createMenu(JFrame f) {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
        menuBar.add(fileMenu);
        
        JMenuItem openItem = new JMenuItem("Open...", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKey));
        openItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        openItem.addActionListener(this);
        fileMenu.add(openItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        exitItem.addActionListener(this);
        fileMenu.add(exitItem);
        
        
        
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
        menuBar.add(editMenu);
        
        JMenuItem selectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcutKey));
        selectAllItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        selectAllItem.addActionListener(this);
        editMenu.add(selectAllItem);
        
        JMenuItem resolutionItem = new JMenuItem("Change Resolution", KeyEvent.VK_R);
        resolutionItem.setEnabled(false);
        resolutionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcutKey));
        resolutionItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        resolutionItem.addActionListener(this);
        editMenu.add(resolutionItem);
        
        f.setJMenuBar(menuBar);
    }
    
    private JScrollPane createCanvas() {
        canvas = new HudCanvas();
        canvas.setPreferredSize(new Dimension(800, 600));
        JScrollPane p = new JScrollPane(canvas);
        p.getHorizontalScrollBar().setUnitIncrement(16);
        p.getVerticalScrollBar().setUnitIncrement(16);
        p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        p.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return p;
    }
    
    private void createTree(Container p) {
        hudFilesRoot = new DefaultMutableTreeNode("HUD");
        
        fileTree = new JTree(hudFilesRoot);
        fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultTableModel model = (DefaultTableModel) propTable.getModel();
                model.getDataVector().removeAllElements();
                propTable.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
                
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                
                Object nodeInfo = node.getUserObject();
                if(nodeInfo instanceof Element) {
                    Element element = (Element) nodeInfo;
                    
                    for(Map.Entry<KVPair<String,String>,String> entry : element.getProps().entrySet()) {
                        model.insertRow(model.getRowCount(), new Object[] {entry.getKey().getKey(), entry.getKey().getValue(), entry.getValue()});
                    }
                    
                    canvas.load(element);
                    
//                    ColumnsAutoSizer.sizeColumnsToFit(propTable);
//                    
//                    for(int i = 0; i < propTable.getColumnCount(); i++) {
//                        propTable.getColumnModel().getColumn(i).setMinWidth(propTable.getColumnModel().getColumn(i).getMaxWidth());
//                    }
//        
//                    propTable.getColumnModel().getColumn(propTable.getColumnCount() - 1).setMinWidth(1000);
//                    propTable.getColumnModel().getColumn(propTable.getColumnCount() - 1).setMaxWidth(1000);
                    
                }
            }
        });
        
        JScrollPane treeView = new JScrollPane(fileTree);
        treeView.setPreferredSize(new Dimension(400, 400));
        
//        GridBagConstraints c = new GridBagConstraints();
//        c.gridx = 2;
//        c.gridy = 0;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx = 0.25;
//        c.weighty = 1;
//        
        p.add(treeView);//, c);
    }
    
    private void createProperties(Container p) {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Properties");
        
        MyJTableModel model = new MyJTableModel();
        model.addColumn("Key");
        model.addColumn("Value");
        model.addColumn("Info");
        propTable = new MyJTable(model);
        propTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propTable.setColumnSelectionAllowed(false);
        propTable.setRowSelectionAllowed(true);
        propTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane treeView = new JScrollPane(propTable);
        treeView.setPreferredSize(new Dimension(400, 400));
        
//        GridBagConstraints c = new GridBagConstraints();
//        c.gridx = 2;
//        c.gridy = 1;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx = 0.25;
//        c.weighty = 1;
        p.add(treeView);//, c);
    }

    private void locateHudDirectory() {
        String selection = null;
        
    
//        XFileDialog dlg=new XFileDialog(helloworld.this); // save file  

//    dlg.setTitle(title7);
//
//    // Important: we do not implement the setMode() method, 
//    // instead, we use the caller function "getSaveFile()" 
//    // to tell XFileDialog to use "Save File Mode" 
//    // 
//    //
//    String content =dlg.getSaveFile(); 
//    String folder = dlg.getDirectory();         
//    selectioncontent.setText("filename:" + content); 
//    
//    System.out.println("APP>>: current Dir: " + folder ); 
//
//    dlg.dispose(); 
        if(os == OS.Windows) {
            XFileDialog dlg = new XFileDialog(EditorFrame.this);    
            dlg.setTitle("Open HUD");
//            ArrayList<String> ext0= new ArrayList<String>(); ext0.add("*"); 
//            ExtensionsFilter filter = new ExtensionsFilter("All Files", ext0);
//            dlg.addFilters(filter); 
            selection = dlg.getFolder();
            dlg.dispose();
        } else if(os == OS.Mac) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            FileDialog fd = new FileDialog(this, "File Dialog");
            java.awt.FileDialog d = new java.awt.FileDialog();
//            d.setm
//            fd.setMultipleMode(false);
            fd.setVisible(true);
            selection = fd.getFile();
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "false");
        } else {
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(fc.showOpenDialog(EditorFrame.this) == JFileChooser.APPROVE_OPTION) {
                selection = fc.getSelectedFile().getPath();
            }
        }
        
        if(selection != null) {
            File file = new File(selection);

            System.out.println("You have selected: " + file);

            if(file.isDirectory()) {
                File[] folders = file.listFiles();
                boolean valid = false;
                for(int i = 0; i < folders.length; i++) {
                    if(folders[i].isDirectory() && ("resource".equalsIgnoreCase(folders[i].getName()) || "scripts".equalsIgnoreCase(folders[i].getName()))) {
                        valid = true;
                        break;
                    }
                }
                if(!valid) {
                    // throw error
                    return;
                }
                resloader = new ResLoader(file.getPath());
                hudFilesRoot.setUserObject(new MyTreeObject(file));
                resloader.populate(hudFilesRoot);

                DefaultTreeModel model = (DefaultTreeModel) fileTree.getModel();
                model.reload();
            } else {
                // Throw error or load archive
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if("Open...".equalsIgnoreCase(cmd)) {
            locateHudDirectory();
        } else if("Exit".equalsIgnoreCase(cmd)) {
            System.exit(0);
        } else if("Change Resolution".equalsIgnoreCase(cmd)) {
            changeResolution();
        } else if("Select All".equalsIgnoreCase(cmd)) {
            for(int i = 0; i < canvas.getElements().size(); i++)
            canvas.select(canvas.getElements().get(i));
        } else {
            System.out.println(e.getActionCommand());
        }
    }

    private void changeResolution() {
        final JOptionPane optionPane = new JOptionPane("Please enter a valid resolution", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

        final JDialog dialog = new JDialog(this, "Click a button", true);
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(
        JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
        public void windowClosing(WindowEvent we) {
        System.out.println("Thwarted user attempt to close window.");
        }
        });
        optionPane.addPropertyChangeListener(
        new PropertyChangeListener() {
            @Override
        public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (dialog.isVisible() 
        && (e.getSource() == optionPane)
        && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
        //If you were going to check something
        //before closing the window, you'd do
        //it here.
        dialog.setVisible(false);
        }
        }
        });
        dialog.pack();
        dialog.setVisible(true);

        int value = ((Integer)optionPane.getValue()).intValue();
        if (value == JOptionPane.YES_OPTION) {
        System.out.println("Good.");
        } else if (value == JOptionPane.NO_OPTION) {
        System.out.println("Try using the window decorations "
        + "to close the non-auto-closing dialog. "
        + "You can't!");
        }

        canvas.setPreferredSize(new Dimension(1920, 1080));
    }
    
}
