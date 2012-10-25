package com.timepath.tf2.hudedit;

import com.timepath.tf2.hudedit.display.HudCanvas;
import com.timepath.tf2.hudedit.loaders.ResLoader;
import com.timepath.tf2.hudedit.util.Element;
import com.timepath.tf2.hudedit.util.Property;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FileDialog;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
//import net.tomahawk.XFileDialog;

/**
 *
 * libs:
 * http://code.google.com/p/xfiledialog/ - windows "open folder" dialog
 * http://java.dzone.com/news/native-dialogs-swing-little - more native file dialogs on linux
 * http://java-gnome.sourceforge.net/get/
 *
 * Reference editors:
 * https://developers.google.com/java-dev-tools/wbpro/
 * http://visualhud.pk69.com/
 * http://gamebanana.com/css/tools/4483
 * http://img13.imageshack.us/img13/210/hudmanagerss.png
 * http://plrf.org/superhudeditor/screens/0.3.0/superhudeditor-0.3.0-linux.jpg
 *
 * @author andrew
 */
@SuppressWarnings("serial")
public class EditorFrame extends JFrame {

    //<editor-fold defaultstate="collapsed" desc="Display">
    public static void main(String... args) {
        //<editor-fold defaultstate="collapsed" desc="Try and get nimbus look and feel, if it is installed.">
//        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        initialLaf();
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Display the editor">
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                EditorFrame frame = new EditorFrame();
                frame.start();
            }
            
        });
        //</editor-fold>
    }
    
    private static void initialLaf() {
        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    private static void systemLaf() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="OS specific code">
    private final static OS os;

    private final static int shortcutKey;

    private enum OS {

        Windows, Mac, Linux, Other

    }
    
    private static Object initFCUILinux;

    static {
        String osVer = System.getProperty("os.name").toLowerCase();
        if(osVer.indexOf("windows") != -1) {
            os = OS.Windows;
        } else if(osVer.indexOf("mac") != -1 || osVer.indexOf("OS X") != -1) {
            os = OS.Mac;
        } else if(osVer.indexOf("linux") != -1) {
            os = OS.Linux;
            initFCUILinux = UIManager.get("FileChooserUI");
//            if("GTK look and feel".equals(UIManager.getLookAndFeel().getName())) {
//                UIManager.put("FileChooserUI", "eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI");
//            }
        } else {
            os = OS.Other;
            System.out.println("Unrecognised OS: " + osVer);
        }
        
        if(os == OS.Windows) {
            shortcutKey = ActionEvent.CTRL_MASK;
//            XFileDialog.setTraceLevel(0);
        } else if(os == OS.Mac) {
            shortcutKey = ActionEvent.META_MASK;
            System.setProperty("apple.awt.showGrowBox", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name application property", "TF2 HUD Editor");
        } else if(os == OS.Linux) {
            shortcutKey = ActionEvent.CTRL_MASK;
        } else {
            shortcutKey = ActionEvent.CTRL_MASK;
        }
    }
    //</editor-fold>
    
    private boolean inDev;
    
    private void changelog() {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://dl.dropbox.com/u/42745598/tf/Hud%20Editor/TF2%20HUD%20Editor.jar.changes");
                    URLConnection connection = url.openConnection();
                    
                    String text = "";
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while((line = reader.readLine()) != null) {
                        if(!inDev && line.contains(myMD5)) { // dev build cannot MD5
                            String[] parts = line.split(myMD5);
                            text += parts[0] + "<b><u>" + myMD5 + "</u></b>" + parts[1];
                        } else {
                            text += line;
                        }
                    }
                    reader.close();
                    
                    final JEditorPane panel = new JEditorPane("text/html", text);
                    Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
                    panel.setPreferredSize(new Dimension(s.width / 4, s.height / 2));
                    panel.setEditable(false);
                    panel.setOpaque(false);
                    panel.addHyperlinkListener(new HyperlinkListener() {

                        @Override
                        public void hyperlinkUpdate(HyperlinkEvent he) {
                            if (he.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                                try {
                                    Desktop.getDesktop().browse(he.getURL().toURI()); // http://stackoverflow.com/questions/5116473/linux-command-to-open-url-in-default-browser
                                } catch(Exception e) {
    //                                e.printStackTrace();
                                }
                            }
                        }

                    });
                    JScrollPane window = new JScrollPane(panel);
                    window.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    info(window, "Changes");
                } catch (IOException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }
    
    private boolean isMD5(String str) {
        return str.matches("[a-fA-F0-9]{32}");
    }
    
    private boolean updating;
    private void checkForUpdates() {
        if(inDev) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    String md5 = "";
                    URL url = new URL("https://dl.dropbox.com/u/42745598/tf/Hud%20Editor/TF2%20HUD%20Editor.jar.MD5");
                    URLConnection connection = url.openConnection();

                    // read from internet
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = reader.readLine();
                    if(line != null && isMD5(line)) { // MD5's are only 32 characters long
                        md5 = line;
                        reader.close();
                    } else {
                        error("Could not obtain latest changelog from internet.");
                        return;
                    }
                    reader.close();
                    
                    boolean equal = md5.equals(myMD5);

                    System.out.println(md5 + " =" + (equal ? "" : "/") + "= " + myMD5);
                    
                    if(!equal) {
                        int returnCode = JOptionPane.showConfirmDialog(null, "Would you like to update to the latest version?", "A new update is available", JOptionPane.YES_NO_OPTION);
                        if(returnCode == JOptionPane.YES_OPTION) {
                            long startTime = System.currentTimeMillis();

                            System.out.println("Connecting to Dropbox...\n");

                            URL latest = new URL("https://dl.dropbox.com/u/42745598/tf/Hud%20Editor/TF2%20HUD%20Editor.jar");
                            latest.openConnection();
                            InputStream in = latest.openStream();

                            FileOutputStream writer = new FileOutputStream(runPath); // TODO: stop closing when this happens. Maybe make a backup..
                            byte[] buffer = new byte[153600]; // 150KB
                            int totalBytesRead = 0;
                            int bytesRead = 0;

                            System.out.println("Downloading JAR file in 150KB blocks at a time.\n");
                            
                            updating = true;
                            
                            while((bytesRead = in.read(buffer)) > 0) {  
                               writer.write(buffer, 0, bytesRead); // I don't want to write directly over the top until I have all the data..
                               buffer = new byte[153600];
                               totalBytesRead += bytesRead;
                            }

                            long endTime = System.currentTimeMillis();

                            System.out.println("Done. " + (new Integer(totalBytesRead).toString()) + " bytes read (" + (new Long(endTime - startTime).toString()) + " millseconds).\n");
                            writer.close();
                            in.close();

                            info("Downloaded the latest version.");
                            
                            updating = false;
                        }
                    } else {
                        info("You have the latest version.");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                    updating = false;
                }
            }
        }.start();
    }
    
    private String runPath;
    private String myMD5 = "";
    
    private void calcMD5() {
        try {
            runPath = URLDecoder.decode(EditorFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
            if(!runPath.endsWith(".jar")) {
                inDev = true;
                return;
            }
            InputStream fis = new FileInputStream(runPath);
            byte[] buffer = new byte[8192]; // 8K buffer
            MessageDigest md = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = fis.read(buffer);
                if(numRead > 0) {
                    md.update(buffer, 0, numRead);
                }
            } while(numRead != -1);
            fis.close();
            byte[] b = md.digest();
            for(int i = 0; i < b.length; i++) {
                myMD5 += Integer.toString((b[i] & 255) + 256, 16).substring(1);
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void quit() {
        if(!inDev) {
            System.exit(0);
        }
    }

    public EditorFrame() {
        super();
        
        calcMD5();
        
        this.setTitle(ResourceBundle.getBundle("com/timepath/tf2/hudedit/lang").getString("Title"));
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }

        });
        
        DisplayMode d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        
        this.setMinimumSize(new Dimension(640, 480));
        this.setPreferredSize(new Dimension((int) (d.getWidth() / 1.5), (int) (d.getHeight() / 1.5)));
        
        this.setLocation((d.getWidth() / 2) - (this.getPreferredSize().width / 2), (d.getHeight() / 2) - (this.getPreferredSize().height / 2));

        this.setJMenuBar(new EditorMenuBar());
        
        this.setDropTarget(new DropTarget() {
            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    DropTargetContext context = e.getDropTargetContext();
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable t = e.getTransferable();
                    if(os == OS.Linux) {
                        DataFlavor nixFileDataFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
                        String data = (String) t.getTransferData(nixFileDataFlavor);
                        for(StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();) {
                            String token = st.nextToken().trim();
                            if(token.startsWith("#") || token.isEmpty()) {
                                 // comment line, by RFC 2483
                                 continue;
                            }
                            try {
                                 File file = new File(new URI(token));
                                 loadHud(file);
                            } catch(Exception ex) {
                            }
                        }
                    } else { 
                        Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
                        if(data instanceof List) {
                            for( Iterator<?> it = ((List<?>)data).iterator(); it.hasNext(); ) {
                                Object o = it.next();
                                if(o instanceof File) {
                                    loadHud((File)o);
                                }
                            }
                        }
                    }
                    context.dropComplete(true);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidDnDOperationException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedFlavorException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    e.dropComplete(true);
                    repaint();
                }
            }
        });
        
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.8);
        
        canvas = new HudCanvas();
        canvasPane = new JScrollPane(canvas);
        canvasPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        canvasPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        splitPane.setLeftComponent(canvasPane);
        
        JSplitPane browser = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new EditorFileTree(), new EditorPropertiesTable());
        browser.setResizeWeight(0.5);
        splitPane.setRightComponent(browser);
        
        this.add(splitPane);

        this.pack();
        this.setFocusableWindowState(true);
    }
    
    public void start() {
        this.createBufferStrategy(3); // Triple buffered, any more sees minimal gain.
        this.setVisible(true);
        this.checkForUpdates();
    }
    
    
    private JScrollPane canvasPane;
    
    public static HudCanvas canvas; // should not be static

    private ResLoader resloader;

    private JTree fileSystem;

    private DefaultMutableTreeNode hudFilesRoot;

    private PropertiesTable propTable;
    
    private String hudSelectionDir;
    
    private File lastLoaded;
    
    //<editor-fold defaultstate="collapsed" desc="Broesel's stuff">
    //    private void selectSteamLocation() {
    //        boolean installPathValid = false;
    //            File steamFolder = new File("");
    //        File installDir;
    //            if (installDir != null && installDir.exists()) {
    //                    steamFolder = installDir.getParentFile().getParentFile().getParentFile().getParentFile();
    //            }
    //            final JFileChooser chooser = new JFileChooser(steamFolder);
    //            chooser.setDialogTitle("Select Steam\\ folder");
    //            chooser.setToolTipText("Please select you Steam\\ folder! Not any subfolders of it.");
    //            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
    //            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    //
    //            final int returnVal = chooser.showOpenDialog(this);
    //        File zipFile = null;
    //            if (returnVal == JFileChooser.APPROVE_OPTION) {
    //                    final File steamappsFolder = new File(chooser.getSelectedFile(), "SteamApps");
    //                    if (!steamappsFolder.exists()) {
    //                            showErrorDialog("Invalid path to ...\\Steam\\SteamApps\\: " + steamappsFolder.getAbsolutePath(), "No SteamApps\\ Folder");
    //                    }
    //                    else if (!steamappsFolder.isDirectory()) {
    //                            showErrorDialog("The entered path is not a folder: " + steamappsFolder.getAbsolutePath(), "This is not a Folder");
    //                    }
    //                    else {
    //                            // Steam-User ausw�hlen lassen
    //                            // DropDown erstellen
    //                            final JComboBox dropDown = new JComboBox();
    //                            final File[] userFolders = steamappsFolder.listFiles();
    //                            for (int i = 0; i < userFolders.length; i++) {
    //                                    if (userFolders[i].isDirectory() && !userFolders[i].getName().equalsIgnoreCase("common")
    //                                                    && !userFolders[i].getName().equalsIgnoreCase("sourcemods")) {
    //                                            // �berpr�fen, ob in dem User-Ordner ein tf2 Ordner
    //                                            // vorhanden ist
    //                                            final Collection<String> gameFolders = Arrays.asList(userFolders[i].list());
    //                                            if (gameFolders.contains("team fortress 2")) {
    //                                                    dropDown.addItem(userFolders[i].getName());
    //                                            }
    //                                    }
    //                            }
    //
    //                            // �berpr�fen ob dropdown elemente hat und dialog anzeigen
    //                            if (dropDown.getItemCount() > 0) {
    //                                    final JPanel dialogPanel = new JPanel();
    //                                    dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
    //                                    dialogPanel.add(new JLabel("Please choose for which user you want to install the HUD"));
    //                                    dialogPanel.add(dropDown);
    //                                    JOptionPane.showMessageDialog(this, dialogPanel, "Select user", JOptionPane.QUESTION_MESSAGE);
    //                            }
    //                            else {
    //                                    showErrorDialog("No users have TF2 installed!", "No TF2 found");
    //                                    return;
    //                            }
    //
    //                            installDir = new File(steamappsFolder, dropDown.getSelectedItem() + File.separator + "team fortress 2" + File.separator + "tf");
    //                            if (installDir.isDirectory() && installDir.exists()) {
    //                                    installPathValid = true;
    //                                    steamInput.setText(installDir.getAbsolutePath());
    //                                    try {
    //                                            String zipFilePath = "";
    //                                            if (zipFile != null && zipFileValid) {
    //                                                    zipFilePath = zipFile.getAbsolutePath();
    //                                            }
    //                                            saveInstallPath(installDir.getAbsolutePath(), zipFilePath);
    //                                    }
    //                                    catch (final IOException e1) {
    //                                            showErrorDialog(e1.getMessage(), "Could not save installpath");
    //                                            e1.printStackTrace();
    //                                    }
    //                            }
    //                            else {
    //                                    showErrorDialog("This is not a valid install location for broeselhud", "No valid installpath");
    //                            }
    //                    }
    //            }
    //    }
    //</editor-fold>

    /**
     * Start in the home directory
     * System.getProperty("user.home")
     * linux = ~
     * windows = %userprofile%
     * mac = ?
     */
    private void locateHudDirectory() {
        String selection = null;
        if(os == OS.Mac) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            FileDialog fd = new FileDialog(this, "Open a HUD folder");
            if(hudSelectionDir != null) {
                fd.setDirectory(hudSelectionDir);
            }
            fd.setFilenameFilter(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            });
            fd.setMode(FileDialog.LOAD);
            fd.setVisible(true);
            String file = fd.getDirectory() + fd.getFile();
            if(file != null) {
                hudSelectionDir = new File(file).getParent();
                selection = file;
            }
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "false");
//        } else
//        if(os == OS.Windows) {
//            XFileDialog fd = new XFileDialog(this); // was EditorFrame.this
//            fd.setTitle("Open a HUD folder");
//            selection = fd.getFolder();
//            fd.dispose();
//        } else
//        if(os == OS.Linux) {
//            EditorFrame.systemLaf();
//            UIManager.put("FileChooserUI", "eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI");
//            JFileChooser fd = new JFileChooser();
//            fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//            if(hudSelectionDir != null) {
//                fd.setCurrentDirectory(new File(hudSelectionDir));
//            }
//            if(fd.showOpenDialog(EditorFrame.this) == JFileChooser.APPROVE_OPTION) {
//                hudSelectionDir = fd.getSelectedFile().getParent();
//                selection = fd.getSelectedFile().getPath();
//            }
//            EditorFrame.initialLaf();
//            UIManager.put("FileChooserUI", initFCUILinux);
        } else { // Fall back to swing
            JFileChooser fd = new JFileChooser();
            fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(hudSelectionDir != null) {
                fd.setCurrentDirectory(new File(hudSelectionDir));
            }
            if(fd.showOpenDialog(EditorFrame.this) == JFileChooser.APPROVE_OPTION) {
                hudSelectionDir = fd.getSelectedFile().getParent();
                selection = fd.getSelectedFile().getPath();
            }
        }

        if(selection != null) {
            final File f = new File(selection);
            new Thread() {
                @Override
                public void run() {
                    loadHud(f);
                }
            }.start();
        } else {
            // Throw error or load archive
        }
    }
    
    private void error(Object msg) {
        error(msg, "Error");
    }
    
    private void error(Object msg, String title) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }
    
    private void info(Object msg) {
        info(msg, "Info");
    }
    
    private void info(Object msg, String title) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void closeHud() {
        canvas.removeAllElements();

        hudFilesRoot.removeAllChildren();
        hudFilesRoot.setUserObject(null);
        DefaultTreeModel model1 = (DefaultTreeModel) fileSystem.getModel();
        model1.reload();
        fileSystem.setSelectionRow(0);

        DefaultTableModel model2 = (DefaultTableModel) propTable.getModel();
        model2.setRowCount(0);
        model2.insertRow(0, new String[]{"", "", ""});
        propTable.repaint();
    }

    private void loadHud(final File file) {
        if(file == null) {
            return;
        }
        if(!file.exists()) {
            error("Could not access file " + file + "\nSorry mac users! Currently working on a fix. For now, drag and drop the hud folder onto the program.ma");
        }
        lastLoaded = file;
        System.out.println("You have selected: " + file.getAbsolutePath());
        try {
            System.out.println("You have canonical: " + file.getCanonicalPath());
        } catch (IOException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(file.getName().endsWith(".zip")) {
            try {
                ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
                ZipEntry entry;
                while((entry = zin.getNextEntry()) != null) {
                    System.out.println(entry.getName());
                    zin.closeEntry();
                }
                zin.close();
            } catch (IOException e) {
            }
            return;
        }

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
                error("Selection not valid. Please choose a folder containing \'resources\' or \'scripts\'.");
                locateHudDirectory();
                return;
            }
            closeHud();

//            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
//
//                @Override
//                public Void doInBackground() {
//                    while(!isCancelled()) {
//                    }
//                    return null;
//                }
//
//                @Override
//                public void done() {
//                }
//
//                @Override
//                protected void process(List<Void> chunks) {
//                }
//
//            };
//            worker.execute();

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            final long start = System.currentTimeMillis();

            resloader = new ResLoader(file.getPath());
            hudFilesRoot.setUserObject(file.getName()); // The only time a String is added to the Tree, that way I can treat it differently
            resloader.populate(hudFilesRoot);

            DefaultTreeModel model = (DefaultTreeModel) fileSystem.getModel();
            model.reload();
            fileSystem.setSelectionRow(0);
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    System.out.println(System.currentTimeMillis()-start);
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    System.out.println("loaded hud");
                }
            });
        }
    }

    private JSpinner spinnerWidth;
    private JSpinner spinnerHeight;
    
//    private static class NumericDocumentFilter extends DocumentFilter {
//
//        @Override
//        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
//            if(stringContainsOnlyDigits(string)) {
//                super.insertString(fb, offset, string, attr);
//            }
//        }
//
//        @Override
//        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
//            super.remove(fb, offset, length);
//        }
//
//        @Override
//        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//            if(stringContainsOnlyDigits(text)) {
//                super.replace(fb, offset, length, text, attrs);
//            }
//        }
//
//        private boolean stringContainsOnlyDigits(String text) {
//            for (int i = 0; i < text.length(); i++) {
//                if (!Character.isDigit(text.charAt(i))) {
//                    return false;
//                }
//            }
//            return true;
//        }
//    }
    
    private void changeResolution() {
        if(spinnerWidth == null) {
            spinnerWidth = new JSpinner(new SpinnerNumberModel(canvas.hudRes.width, 640, 7680, 1)); // WHUXGA
//            NumberEditor jsWidth = (NumberEditor) spinnerWidth.getEditor();
//            final Document jsWidthDoc = jsWidth.getTextField().getDocument();
//            if(jsWidthDoc instanceof PlainDocument) {
//                AbstractDocument docWidth = new PlainDocument() {
//
//                    private static final long serialVersionUID = 1L;
//
//                    @Override
//                    public void setDocumentFilter(DocumentFilter filter) {
//                        if(filter instanceof NumericDocumentFilter) {
//                            super.setDocumentFilter(filter);
//                        }
//                    }
//                };
//                docWidth.setDocumentFilter(new NumericDocumentFilter());
//                jsWidth.getTextField().setDocument(docWidth);
//            }
        }
        if(spinnerHeight == null) {
            spinnerHeight = new JSpinner(new SpinnerNumberModel(canvas.hudRes.height, 480, 4800, 1)); // WHUXGA
//            NumberEditor jsHeight = (NumberEditor) spinnerHeight.getEditor();
//            final Document jsHeightDoc = jsHeight.getTextField().getDocument();
//            if(jsHeightDoc instanceof PlainDocument) {
//                AbstractDocument docHeight = new PlainDocument() {
//
//                    private static final long serialVersionUID = 1L;
//
//                    @Override
//                    public void setDocumentFilter(DocumentFilter filter) {
//                        if (filter instanceof NumericDocumentFilter) {
//                            super.setDocumentFilter(filter);
//                        }
//                    }
//                };
//                docHeight.setDocumentFilter(new NumericDocumentFilter());
//                jsHeight.getTextField().setDocument(docHeight);
//            }
        }
        Object[] message = {"Width: ", spinnerWidth, "Height: ", spinnerHeight};
        
        final JOptionPane optionPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null);
        final JDialog dialog = optionPane.createDialog(this, "Change resolution...");
        dialog.setContentPane(optionPane);
        dialog.pack();
        dialog.setVisible(true);
        if(optionPane.getValue() != null) {
            int value = ((Integer) optionPane.getValue()).intValue();
            if(value == JOptionPane.YES_OPTION) {
                canvas.setPreferredSize(new Dimension(Integer.parseInt(spinnerWidth.getValue().toString()), Integer.parseInt(spinnerHeight.getValue().toString())));
            }
        }
    }
    
    private class PropertiesTable extends JTable {

        PropertiesTable() {
            super();
        }

        PropertiesTable(TableModel model) {
            super(model);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return (column != 0); // deny editing of key
        }

        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            return super.getCellEditor(row, column);
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            return super.getCellRenderer(row, column);
        }

    }
    
    private class EditorPropertiesTable extends JScrollPane {
        
        public EditorPropertiesTable() {
            super();
            
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Key");
            model.addColumn("Value");
            model.addColumn("Info");
            
            model.insertRow(0, new String[]{"", "", ""});

            propTable = new PropertiesTable(model);
            propTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            propTable.setColumnSelectionAllowed(false);
            propTable.setRowSelectionAllowed(true);
            propTable.getTableHeader().setReorderingAllowed(false);

            this.setViewportView(propTable);
            this.setPreferredSize(new Dimension(400, 400));
        }
        
    }
    
    private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

        CustomTreeCellRenderer() {
            super();
        }

        private void setIcons(JTree tree, Icon ico) {
            if(tree.isEnabled()) {
                this.setIcon(ico);
            } else {
                this.setDisabledIcon(ico);
            }
        }

        JFileChooser iconFinder = new JFileChooser();
        Color sameColor = Color.BLACK;
        Color diffColor = Color.BLUE;
        Color newColor = Color.GREEN.darker(); 

        /**
          * Configures the renderer based on the passed in components.
          * The value is set from messaging the tree with
          * <code>convertValueToText</code>, which ultimately invokes
          * <code>toString</code> on <code>value</code>.
          * The foreground color is set based on the selection and the icon
          * is set based on on leaf and expanded.
          */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            String valueText = value.toString();

            Color tColor = null;

            if(value instanceof DefaultMutableTreeNode) {
                Object nodeValue = ((DefaultMutableTreeNode) value).getUserObject();
                if(nodeValue instanceof String) {
                    tColor = sameColor;
                    setIcons(tree, UIManager.getIcon("FileView.computerIcon"));
                } else if(nodeValue instanceof File) { // this will either be an actual file on the system (directories included), or an element within a file
                    tColor = diffColor;
                    File f = ((File) nodeValue);
                    valueText = f.getName();
                    setIcons(tree, iconFinder.getIcon(f));
                } else if(nodeValue instanceof Element) {
                    tColor = newColor;
                    Element e = (Element) nodeValue;
                    if(e.getProps().isEmpty() && leaf) { // If no properties, warn because irrelevant. Only care if leaves are empty
                        setIcons(tree, UIManager.getIcon("FileChooser.detailsViewIcon"));
                    } else {
                        setIcons(tree, UIManager.getIcon("FileChooser.listViewIcon"));
                    }
                } else {
                    if(nodeValue != null) {
                        System.out.println(nodeValue.getClass());
                    }
                    setIcons(tree, null);
                }
            }
            String stringValue = tree.convertValueToText(valueText, sel, expanded, leaf, row, hasFocus);
            this.hasFocus = hasFocus;
            this.setText(stringValue);
            if(tColor != null) {
                this.setForeground(sel ? tColor != newColor ? new Color(-tColor.getRed() + 255, -tColor.getGreen() + 255, -tColor.getBlue() + 255) : tColor.brighter() : tColor);
            } else {
                this.setForeground(sel ? getTextSelectionColor() : getTextNonSelectionColor());
            }
            this.setEnabled(tree.isEnabled());
            this.setComponentOrientation(tree.getComponentOrientation());
            this.selected = sel;
            return this;
        }
    }
    
    private class EditorFileTree extends JScrollPane {

        EditorFileTree() {
            super();
            
            hudFilesRoot = new DefaultMutableTreeNode(null);

            fileSystem = new JTree(hudFilesRoot);
            fileSystem.setShowsRootHandles(true);
            fileSystem.setSelectionRow(0);
            fileSystem.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            fileSystem.setCellRenderer(new CustomTreeCellRenderer());
            fileSystem.addTreeSelectionListener(new TreeSelectionListener() {

                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultTableModel model = (DefaultTableModel) propTable.getModel();
                    model.getDataVector().removeAllElements();
                    model.insertRow(0, new String[]{"", "", ""});
                    propTable.scrollRectToVisible(new Rectangle(0, 0, 0, 0));

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileSystem.getLastSelectedPathComponent();
                    if(node == null) {
                        return;
                    }

                    Object nodeInfo = node.getUserObject();
                    if(nodeInfo instanceof Element) {
                        Element element = (Element) nodeInfo;
                        canvas.load(element);
                        if(!element.getProps().isEmpty()) {
                            model.getDataVector().removeAllElements();
                            element.validateDisplay();
                            for(int i = 0; i < element.getProps().size(); i++) {
                                Property entry = element.getProps().get(i);
                                if(entry.getKey().equals("\\n")) {
                                    continue;
                                }
                                model.insertRow(model.getRowCount(), new Object[] {entry.getKey(), entry.getValue(), entry.getInfo()});
                            }
                        }
                    }
                }

            });
            
            this.setViewportView(fileSystem);
            this.setPreferredSize(new Dimension(400, 400));
        }
    }
    
    private class EditorMenuBar extends JMenuBar {
        
        private final JMenuItem newItem;
        private final JMenuItem openItem;
        private final JMenuItem openZippedItem;
        private final JMenuItem saveItem;
        private final JMenuItem saveAsItem;
        private final JMenuItem reloadItem;
        private final JMenuItem closeItem;
        private final JMenuItem exitItem;
        private final JMenuItem undoItem;
        private final JMenuItem redoItem;
        private final JMenuItem cutItem;
        private final JMenuItem copyItem;
        private final JMenuItem pasteItem;
        private final JMenuItem deleteItem;
        private final JMenuItem selectAllItem;
        private final JMenuItem preferencesItem;
        private final JMenuItem resolutionItem;
        private final JMenuItem previewItem;
        private final JMenuItem updateItem;
        private final JMenuItem aboutItem;
        private final JMenuItem changeLogItem;

        EditorMenuBar() {
            super();
            
            EditorActionListener al = new EditorActionListener();
            
            JMenu fileMenu = new JMenu("File");
            fileMenu.setMnemonic(KeyEvent.VK_F);
            this.add(fileMenu);
            
            newItem = new JMenuItem("New", KeyEvent.VK_N);
            newItem.setEnabled(false);
            newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutKey));
            newItem.addActionListener(al);
            fileMenu.add(newItem);
            
            openItem = new JMenuItem("Open...", KeyEvent.VK_O);
            openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKey));
            openItem.addActionListener(al);
            fileMenu.add(openItem);
            
            openZippedItem = new JMenuItem("Open Zip...", KeyEvent.VK_Z);
            openZippedItem.setEnabled(false);
            openZippedItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKey + ActionEvent.SHIFT_MASK));
            openZippedItem.addActionListener(al);
            fileMenu.add(openZippedItem);
            
            fileMenu.addSeparator();
            
            saveItem = new JMenuItem("Save", KeyEvent.VK_S);
            saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutKey));
            saveItem.addActionListener(al);
            fileMenu.add(saveItem);
            
            saveAsItem = new JMenuItem("Save As...", KeyEvent.VK_A);
            saveAsItem.setEnabled(false);
            saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutKey + ActionEvent.SHIFT_MASK));
            saveAsItem.addActionListener(al);
            fileMenu.add(saveAsItem);
            
            reloadItem = new JMenuItem("Revert", KeyEvent.VK_R);
            reloadItem.addActionListener(al);
            fileMenu.add(reloadItem);
            
            fileMenu.addSeparator();

            closeItem = new JMenuItem("Close", KeyEvent.VK_C);
            closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcutKey));
            closeItem.addActionListener(al);
            fileMenu.add(closeItem);

            fileMenu.addSeparator();

            exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
            exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcutKey));
            exitItem.addActionListener(al);
            fileMenu.add(exitItem);

            JMenu editMenu = new JMenu("Edit");
            editMenu.setMnemonic(KeyEvent.VK_E);
            this.add(editMenu);
            
            undoItem = new JMenuItem("Undo", KeyEvent.VK_U);
            undoItem.setEnabled(false);
            undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutKey));
            undoItem.addActionListener(al);
            editMenu.add(undoItem);
            
            redoItem = new JMenuItem("Redo", KeyEvent.VK_R);
            redoItem.setEnabled(false);
            redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutKey + ActionEvent.SHIFT_MASK));
            redoItem.addActionListener(al);
            editMenu.add(redoItem);
            
            editMenu.addSeparator();
            
            cutItem = new JMenuItem("Cut", KeyEvent.VK_T);
            cutItem.setEnabled(false);
            cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutKey));
            cutItem.addActionListener(al);
            editMenu.add(cutItem);
            
            copyItem = new JMenuItem("Copy", KeyEvent.VK_C);
            copyItem.setEnabled(false);
            copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutKey));
            copyItem.addActionListener(al);
            editMenu.add(copyItem);
            
            pasteItem = new JMenuItem("Paste", KeyEvent.VK_P);
            pasteItem.setEnabled(false);
            pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutKey));
            pasteItem.addActionListener(al);
            editMenu.add(pasteItem);
            
            deleteItem = new JMenuItem("Delete");
            deleteItem.setEnabled(false);
            deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            deleteItem.addActionListener(al);
            editMenu.add(deleteItem);
            
            editMenu.addSeparator();

            selectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
            selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcutKey));
            selectAllItem.addActionListener(al);
            editMenu.add(selectAllItem);
            
            editMenu.addSeparator();

            preferencesItem = new JMenuItem("Preferences", KeyEvent.VK_E);
            preferencesItem.setEnabled(false);
            preferencesItem.addActionListener(al);
            editMenu.add(preferencesItem);

            JMenu viewMenu = new JMenu("View");
            viewMenu.setMnemonic(KeyEvent.VK_V);
            this.add(viewMenu);

            resolutionItem = new JMenuItem("Change Resolution", KeyEvent.VK_R);
            resolutionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcutKey));
            resolutionItem.addActionListener(al);
            viewMenu.add(resolutionItem);
            
            previewItem = new JMenuItem("Full Screen Preview", KeyEvent.VK_F);
            previewItem.setEnabled(false);
//            previewItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcutKey)); // alt + shift + enter on linux
            previewItem.addActionListener(al);
            viewMenu.add(previewItem);
            
            viewMenu.addSeparator();
            
            JMenuItem viewItem1 = new JMenuItem("Main Menu");
            viewItem1.setEnabled(false);
            viewItem1.addActionListener(al);
            viewMenu.add(viewItem1);
            
            JMenuItem viewItem2 = new JMenuItem("In-game (Health and ammo)");
            viewItem2.setEnabled(false);
            viewItem2.addActionListener(al);
            viewMenu.add(viewItem2);
            
            JMenuItem viewItem3 = new JMenuItem("Scoreboard");
            viewItem3.setEnabled(false);
            viewItem3.addActionListener(al);
            viewMenu.add(viewItem3);
            
            JMenuItem viewItem4 = new JMenuItem("CTF HUD");
            viewItem4.setEnabled(false);
            viewItem4.addActionListener(al);
            viewMenu.add(viewItem4);

            JMenu helpMenu = new JMenu("Help");
            helpMenu.setMnemonic(KeyEvent.VK_H);
            this.add(helpMenu);
            
            updateItem = new JMenuItem("Check for Updates", KeyEvent.VK_U);
            updateItem.setEnabled(!inDev);
            updateItem.addActionListener(al);
            helpMenu.add(updateItem);

            changeLogItem = new JMenuItem("Changelog");
            changeLogItem.addActionListener(al);
            helpMenu.add(changeLogItem);
            
            aboutItem = new JMenuItem("About", KeyEvent.VK_A);
            aboutItem.addActionListener(al);
            helpMenu.add(aboutItem);
        }
        
        private class EditorActionListener implements ActionListener {
        
            EditorActionListener() {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Object cmd = e.getSource();
                
                if(cmd == openItem || cmd == openZippedItem) {
                    locateHudDirectory();
                } else if(cmd == closeItem) {
                    closeHud();
                } else if(cmd == saveItem) {
                    if(canvas.getElements().size() > 0)
                        error(canvas.getElements().get(canvas.getElements().size() - 1).save());
                } else if(cmd == reloadItem) {
                    loadHud(lastLoaded);
                } else if(cmd == exitItem) {
                    quit();
                } else if(cmd == resolutionItem) {
                    changeResolution();
                } else if(cmd == selectAllItem) {
                    for(int i = 0; i < canvas.getElements().size(); i++) {
                        canvas.select(canvas.getElements().get(i));
                    }
                } else if(cmd == aboutItem) {
                    String latestThread = "http://www.reddit.com/r/truetf2/comments/11xtwz/wysiwyg_hud_editor_coming_together/";
                    String aboutText = "<html><h2>This is a <u>W</u>hat <u>Y</u>ou <u>S</u>ee <u>I</u>s <u>W</u>hat <u>Y</u>ou <u>G</u>et HUD Editor for TF2.</h2>";
                    aboutText += "<p>You can graphically edit TF2 HUDs with it!<br>";
                    aboutText += "<p>It was written by <a href=\"http://www.reddit.com/user/TimePath/\">TimePath</a></p>";
                    aboutText += "<p>Source available on <a href=\"http://code.google.com/p/tf2-hud-editor/\">Google code</a></p>";
                    aboutText += "<p>I have an <a href=\"http://code.google.com/feeds/p/tf2-hud-editor/hgchanges/basic\">Atom feed</a> set up listing source commits</p>";
                    aboutText += "<p>Please give feedback or suggestions on <a href=\""+latestThread+"\">the latest update thread</a></p>";
                    aboutText += "<p>Current version: " + myMD5 + "</p>";
                    aboutText += "</html>";
                    final JEditorPane panel = new JEditorPane("text/html", aboutText);
                    panel.setEditable(false);
                    panel.setOpaque(false);
                    panel.addHyperlinkListener(new HyperlinkListener() {

                        @Override
                        public void hyperlinkUpdate(HyperlinkEvent he) {
                            if (he.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                                try {
                                    Desktop.getDesktop().browse(he.getURL().toURI()); // http://stackoverflow.com/questions/5116473/linux-command-to-open-url-in-default-browser
                                } catch(Exception e) {
    //                                e.printStackTrace();
                                }
                            }
                        }

                    });
                    info(panel, "About");
                } else if(cmd == updateItem) {
                    EditorFrame.this.checkForUpdates();
                } else if(cmd == changeLogItem) {
                    changelog();
                }
            }

        }
        
    }

}