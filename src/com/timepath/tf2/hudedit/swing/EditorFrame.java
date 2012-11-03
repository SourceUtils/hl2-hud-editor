package com.timepath.tf2.hudedit.swing;

//<editor-fold defaultstate="collapsed" desc="imports">
import com.timepath.tf2.hudedit.Main;
import com.timepath.tf2.hudedit.temp.OSXAdapter;
import com.timepath.tf2.hudedit.Main.OS;
import com.timepath.tf2.hudedit.swing.EditorPropertiesTablePane.EditorPropertiesTable;
import com.timepath.tf2.hudedit.loaders.ResLoader;
import com.timepath.tf2.hudedit.loaders.VtfLoader;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FileDialog;
import java.awt.GraphicsEnvironment;
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import net.tomahawk.XFileDialog;
import org.java.ayatana.ApplicationMenu;
import org.java.ayatana.AyatanaDesktop;
//</editor-fold>

/**
 *
 * libs:
 * http://code.google.com/p/xfiledialog/ - windows "open folder" dialog
 * http://java.dzone.com/news/native-dialogs-swing-little - more native file dialogs on linux
 * http://java-gnome.sourceforge.net/get/
 * http://developer.apple.com/legacy/mac/library/#samplecode/OSXAdapter/Introduction/Intro.html#//apple_ref/doc/uid/DTS10000685-Intro-DontLinkElementID_2 - mac integration
 * 
 * Interface design:
 * http://stackoverflow.com/questions/1004239/swing-tweaks-for-mac-os-x
 * http://developer.apple.com/library/mac/#documentation/Java/Conceptual/Java14Development/07-NativePlatformIntegration/NativePlatformIntegration.html
 * http://today.java.net/pub/a/today/2003/12/08/swing.html
 * 
 * Deployment:
 * http://www2.sys-con.com/itsg/virtualcd/java/archives/0801/mcfarland/index.html
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
    
    public static void main(String[] args) {
        
        if(Main.os == OS.Windows) {
            try {
                XFileDialog.setTraceLevel(0);
            } catch(UnsatisfiedLinkError e) {
                System.out.println("java.library.path = " + System.getProperty("java.library.path"));
            }
        }
    
//        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        initialLaf();
        
        SwingUtilities.invokeLater(new Runnable() { // SwingUtilities vs EventQueue?
            
            @Override
            public void run() {
                EditorFrame frame = new EditorFrame();
                frame.setVisible(true);
            }
            
        });
        
    }
    
    private static void initialLaf() {
        try {
            for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            systemLaf();
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
    
    private boolean indev;
    
    private boolean updating;
    
    private String runPath;
    
    private String myMD5 = "";
    
    private boolean isMD5(String str) {
        return str.matches("[a-fA-F0-9]{32}");
    }
    
    //<editor-fold defaultstate="collapsed" desc="UI">
    private void changelog() {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://dl.dropbox.com/u/42745598/tf/Hud%20Editor/TF2%20HUD%20Editor.jar.changes");
                    URLConnection connection = url.openConnection();
                    //                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    System.out.println("getting filesize...");
                    int filesize = connection.getContentLength();
                    System.out.println(filesize);
                    
                    String text = "";
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while((line = reader.readLine()) != null) {
                        if(!indev && line.contains(myMD5)) { // dev build cannot MD5
                            String[] parts = line.split(myMD5);
                            if(parts[0] != null) {
                                text += parts[0];
                            }
                            text += "<b><u>" + myMD5 + "</u></b>";
                            if(parts[1] != null) {
                                text += parts[1];
                            }
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
                    if(Main.os == OS.Mac) {
                        window.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    } else {
                        window.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    }
                    info(window, "Changes");
                } catch(IOException ex) {
                    error(ex);
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }
    
    private void checkForUpdates() {
        //        if(inDev) {
        //            return;
        //        }
        new Thread() {
            
            int retries = 3;
            
            private void doCheckForUpdates() {
                try {
                    String md5;
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
                            URLConnection editor = latest.openConnection();
                            
                            JProgressBar pb = new JProgressBar(0,editor.getContentLength());
                            pb.setPreferredSize(new Dimension(175,20));
                            pb.setStringPainted(true);
                            pb.setValue(0);
                            
                            JLabel label = new JLabel("Update Progress: ");
                            
                            JPanel center_panel = new JPanel();
                            center_panel.add(label);
                            center_panel.add(pb);
                            
                            JDialog dialog = new JDialog((JFrame) null, "Updating...");
                            dialog.getContentPane().add(center_panel, BorderLayout.CENTER);
                            dialog.pack();
                            dialog.setVisible(true);
                            
                            dialog.setLocationRelativeTo(null); // center on screen
                            dialog.toFront(); // raise above other java windows
                            
                            InputStream in = latest.openStream();
                            
                            
                            //                            FileOutputStream writer = new FileOutputStream(runPath); // TODO: stop closing when this happens. Maybe make a backup..
                            byte[] buffer = new byte[153600]; // 150KB
                            int totalBytesRead = 0;
                            int bytesRead;
                            
                            System.out.println("Downloading JAR file in 150KB blocks at a time.\n");
                            
                            updating = true;
                            
                            while((bytesRead = in.read(buffer)) > 0) {
                                //                               writer.write(buffer, 0, bytesRead); // I don't want to write directly over the top until I have all the data..
                                buffer = new byte[153600];
                                totalBytesRead += bytesRead;
                                pb.setValue(totalBytesRead);
                            }
                            
                            long endTime = System.currentTimeMillis();
                            
                            System.out.println("Done. " + (new Integer(totalBytesRead).toString()) + " bytes read (" + (new Long(endTime - startTime).toString()) + " millseconds).\n");
                            //                            writer.close();
                            in.close();
                            
                            dialog.dispose();
                            
                            info("Downloaded the latest version. Please restart now.");
                            
                            updating = false;
                            try {
                                restart();
                            } catch (URISyntaxException ex) {
                                Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } else {
                        info("You have the latest version.");
                    }
                } catch (IOException ex) {
                    retries--;
                    if(retries > 0) {
                        doCheckForUpdates();
                    } else {
                        Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        updating = false;
                    }
                }
            }
            
            @Override
            public void run() {
                doCheckForUpdates();
            }
        }.start();
    }
    
    public void about() {
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
    }
    //</editor-fold>
    
    private void restart() throws URISyntaxException, IOException { // TODO: wrap this class in a launcher, rather than explicitly restarting
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(EditorFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        if(!currentJar.getName().endsWith(".jar")) {
            return;
        }

        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }
    
    private void calcMD5() {
        try {
            runPath = URLDecoder.decode(EditorFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
            if(!runPath.endsWith(".jar")) {
                indev = true;
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
    
    public void quit() {
        if(!updating) {
            System.out.println("Quitting...");
            System.exit(0);
        }
    }

    public EditorFrame() {
        calcMD5();
        
        this.setTitle(ResourceBundle.getBundle("com/timepath/tf2/hudedit/internationalization/lang").getString("Title"));
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
        
        this.setDropTarget(new DropTarget() {
            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    DropTargetContext context = e.getDropTargetContext();
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable t = e.getTransferable();
                    if(Main.os == OS.Linux) {
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
        
        canvas = new EditorCanvas();
        canvasPane = new JScrollPane(canvas);
        if(Main.os == OS.Mac) {
            canvasPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            canvasPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        } else {
            canvasPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            canvasPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        }
        splitPane.setLeftComponent(canvasPane);
        
        EditorPropertiesTablePane propTablePane = new EditorPropertiesTablePane();
        propTable = propTablePane.getPropTable();
        
        hudFilesRoot = new DefaultMutableTreeNode(null);
        fileSystem = new JTree(hudFilesRoot);
        EditorFileTreePane fileTreePane = new EditorFileTreePane(canvas, propTable, fileSystem);
        
        JSplitPane browser = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fileTreePane, propTablePane);
        browser.setResizeWeight(0.5);
        splitPane.setRightComponent(browser);
        
        this.getContentPane().add(splitPane);

        this.pack();
        this.setFocusableWindowState(true);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down...");
//                System.exit(0);
            }
        });
        
        JMenuBar jmb = new EditorMenuBar();
        this.setJMenuBar(jmb);
        if(AyatanaDesktop.isSupported()) {
            boolean worked = ApplicationMenu.tryInstall(this, jmb);
            if(worked) {
                this.setJMenuBar(null);
            } else {
                error("AyatanaDesktop failed to load" + "\n" + System.getenv("XDG_CURRENT_DESKTOP"));
            }
        }
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        this.createBufferStrategy(3); // Triple buffered, any more sees minimal gain.
        if(!indev) {
            this.checkForUpdates();
        }
    }
    
    
    private JScrollPane canvasPane;
    
    public static EditorCanvas canvas; // should not be static

    private ResLoader resloader;

    private JTree fileSystem;

    private DefaultMutableTreeNode hudFilesRoot;

    private EditorPropertiesTable propTable;
    
    private String hudSelectionDir;
    
    private File lastLoaded = new File("/home/andrew/TF2 HUDS/frankenhudr47"); // convenience
    
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
        new Thread(new Runnable() {
            public void run() {
                String selection = null;
                if(Main.os == OS.Mac) {
                    System.setProperty("apple.awt.fileDialogForDirectories", "true");
                    FileDialog fd = new FileDialog(EditorFrame.this, "Open a HUD folder");
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
                } else
                if(Main.os == OS.Windows) {
                    XFileDialog fd = new XFileDialog(EditorFrame.this);
                    fd.setTitle("Open a HUD folder");
                    selection = fd.getFolder();
                    fd.dispose();
        //        } else
        //        if(HudEditor.os == OS.Linux) {
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
//                    new Thread() {
//                        @Override
//                        public void run() {
                            loadHud(f);
//                        }
//                    }.start();
                } else {
                    // Throw error or load archive
                }
            }
        }).start();
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
            error("Could not access file " + file);
        }
        lastLoaded = file;
        System.out.println("You have selected: " + file.getAbsolutePath());
        
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
            spinnerWidth = new JSpinner(new SpinnerNumberModel(canvas.screen.width, 640, 7680, 1)); // WHUXGA
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
            spinnerHeight = new JSpinner(new SpinnerNumberModel(canvas.screen.height, 480, 4800, 1)); // WHUXGA
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
    
    
    
    private class EditorMenuBar extends JMenuBar {
        
        private JMenuItem newItem;
        private JMenuItem openItem;
        private JMenuItem openZippedItem;
        private JMenuItem saveItem;
        private JMenuItem saveAsItem;
        private JMenuItem reloadItem;
        private JMenuItem closeItem;
        private JMenuItem exitItem;
        private JMenuItem undoItem;
        private JMenuItem redoItem;
        private JMenuItem cutItem;
        private JMenuItem copyItem;
        private JMenuItem pasteItem;
        private JMenuItem deleteItem;
        private JMenuItem selectAllItem;
        private JMenuItem preferencesItem;
        private JMenuItem resolutionItem;
        private JMenuItem previewItem;
        private JMenuItem updateItem;
        private JMenuItem aboutItem;
        private JMenuItem changeLogItem;
        
        private JMenuItem vtfItem;

        EditorMenuBar() {
            super();
            
            EditorActionListener al = new EditorActionListener();
            
            JMenu fileMenu = new JMenu("File");
            fileMenu.setMnemonic(KeyEvent.VK_F);
            this.add(fileMenu);
            
            newItem = new JMenuItem("New", KeyEvent.VK_N);
            newItem.setEnabled(false);
            newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Main.shortcutKey));
            newItem.addActionListener(al);
            fileMenu.add(newItem);
            
            openItem = new JMenuItem("Open...", KeyEvent.VK_O);
            openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Main.shortcutKey));
            openItem.addActionListener(al);
            fileMenu.add(openItem);
            
            openZippedItem = new JMenuItem("Open Zip...", KeyEvent.VK_Z);
            openZippedItem.setEnabled(false);
            openZippedItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Main.shortcutKey + ActionEvent.SHIFT_MASK));
            openZippedItem.addActionListener(al);
            fileMenu.add(openZippedItem);
            
            if(Main.os == OS.Mac) {
                fileMenu.addSeparator();
            
                closeItem = new JMenuItem("Close", KeyEvent.VK_C);
                closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Main.shortcutKey));
                closeItem.addActionListener(al);
                fileMenu.add(closeItem);
            }
            
            saveItem = new JMenuItem("Save", KeyEvent.VK_S);
            saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Main.shortcutKey));
            saveItem.addActionListener(al);
            fileMenu.add(saveItem);
            
            saveAsItem = new JMenuItem("Save As...", KeyEvent.VK_A);
            saveAsItem.setEnabled(false);
            saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Main.shortcutKey + ActionEvent.SHIFT_MASK));
            saveAsItem.addActionListener(al);
            fileMenu.add(saveAsItem);
            
            reloadItem = new JMenuItem("Revert", KeyEvent.VK_R);
            reloadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
            reloadItem.addActionListener(al);
            fileMenu.add(reloadItem);

            if(Main.os != OS.Mac) {
                fileMenu.addSeparator();
                
                closeItem = new JMenuItem("Close", KeyEvent.VK_C);
                closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Main.shortcutKey));
                closeItem.addActionListener(al);
                fileMenu.add(closeItem);
            
                fileMenu.addSeparator();

                exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
                exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Main.shortcutKey));
                exitItem.addActionListener(al);
                fileMenu.add(exitItem);
            }

            JMenu editMenu = new JMenu("Edit");
            editMenu.setMnemonic(KeyEvent.VK_E);
            this.add(editMenu);
            
            undoItem = new JMenuItem("Undo", KeyEvent.VK_U);
            undoItem.setEnabled(false);
            undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Main.shortcutKey));
            undoItem.addActionListener(al);
            editMenu.add(undoItem);
            
            redoItem = new JMenuItem("Redo", KeyEvent.VK_R);
            redoItem.setEnabled(false);
            redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Main.shortcutKey + ActionEvent.SHIFT_MASK));
            redoItem.addActionListener(al);
            editMenu.add(redoItem);
            
            editMenu.addSeparator();
            
            cutItem = new JMenuItem("Cut", KeyEvent.VK_T);
            cutItem.setEnabled(false);
            cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Main.shortcutKey));
            cutItem.addActionListener(al);
            editMenu.add(cutItem);
            
            copyItem = new JMenuItem("Copy", KeyEvent.VK_C);
            copyItem.setEnabled(false);
            copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Main.shortcutKey));
            copyItem.addActionListener(al);
            editMenu.add(copyItem);
            
            pasteItem = new JMenuItem("Paste", KeyEvent.VK_P);
            pasteItem.setEnabled(false);
            pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Main.shortcutKey));
            pasteItem.addActionListener(al);
            editMenu.add(pasteItem);
            
            deleteItem = new JMenuItem("Delete");
            deleteItem.setEnabled(false);
            deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            deleteItem.addActionListener(al);
            editMenu.add(deleteItem);
            
            editMenu.addSeparator();

            selectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
            selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Main.shortcutKey));
            selectAllItem.addActionListener(al);
            editMenu.add(selectAllItem);
            
            if(Main.os == OS.Mac) {
                editMenu.addSeparator();

                preferencesItem = new JMenuItem("Preferences", KeyEvent.VK_E);
                preferencesItem.setEnabled(false);
                preferencesItem.addActionListener(al);
                editMenu.add(preferencesItem);
            }

            JMenu viewMenu = new JMenu("View");
            viewMenu.setMnemonic(KeyEvent.VK_V);
            this.add(viewMenu);

            resolutionItem = new JMenuItem("Change Resolution", KeyEvent.VK_R);
            resolutionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Main.shortcutKey));
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
//            updateItem.setEnabled(!inDev);
            updateItem.addActionListener(al);
            helpMenu.add(updateItem);

            changeLogItem = new JMenuItem("Changelog");
            changeLogItem.addActionListener(al);
            helpMenu.add(changeLogItem);
            
            if(Main.os != OS.Mac) {
                aboutItem = new JMenuItem("About", KeyEvent.VK_A);
                aboutItem.addActionListener(al);
                helpMenu.add(aboutItem);
            }
            
            JMenu extrasMenu = new JMenu("Extras");
            extrasMenu.setMnemonic(KeyEvent.VK_X);
            this.add(extrasMenu);

            vtfItem = new JMenuItem("VTF Loader", KeyEvent.VK_V);
            vtfItem.addActionListener(al);
            extrasMenu.add(vtfItem);
            
            if(Main.os == OS.Mac) {
                createMacMenus();
            }
        }
        
        private void createMacMenus() {
            try {
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[])null));
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[])null));
    //            OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[])null));
    //            OSXAdapter.setFileHandler(this, getClass().getDeclaredMethod("loadImageFile", new Class[] { String.class }));

    //            com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
        //        app.setEnabledPreferencesMenu(true);
        //        app.setEnabledAboutMenu(true);
    //            app.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
    //            app.setAboutHandler(new com.apple.eawt.AboutHandler() {
    //                public void handleAbout(AboutEvent e) {
    //                    about();
    //                }
    //            });
    //            app.setQuitHandler(new com.apple.eawt.QuitHandler() {
    //                public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
    //                    quit();
    //                }
    //            });
    //            ImageIcon icon = ... // your code to load your icon
    //            application.setDockIconImage(icon.getImage());
            } catch(Exception e) {
                e.printStackTrace();
            }
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
                    if(canvas.getElements().size() > 0) {
                        error(canvas.getElements().get(canvas.getElements().size() - 1).save());
                    }
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
                    about();
                } else if(cmd == updateItem) {
                    EditorFrame.this.checkForUpdates();
                } else if(cmd == changeLogItem) {
                    changelog();
                } else if(cmd == vtfItem) {
                    VtfLoader.main("");
                }
            }
        }
    }
}