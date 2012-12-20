package com.timepath.tf2.hudeditor.gui;

//<editor-fold defaultstate="collapsed" desc="imports">
import com.timepath.tf2.hudeditor.Main;
import com.timepath.tf2.hudeditor.plaf.OS;
import com.timepath.tf2.hudeditor.loaders.CaptionLoaderFrame;
import com.timepath.tf2.hudeditor.loaders.ResLoader;
import com.timepath.tf2.hudeditor.loaders.VtfLoader;
import com.timepath.tf2.hudeditor.plaf.mac.OSXAdapter;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FileDialog;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
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
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.Action;
import javax.swing.JButton;
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
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
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
 * @author TimePath
 */
public class EditorFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private boolean updating;
    
    public boolean autoCheck = true;
    private final EditorMenuBar jmb;
    private final JButton updateButton;
    private final JToolBar statusBar;
    
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
                    logger.info("getting filesize...");
                    int filesize = connection.getContentLength();
                    System.out.println(filesize);
                    
                    String text = "";
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while((line = reader.readLine()) != null) {
                        if(!Main.indev && line.contains(Main.myMD5)) { // dev build cannot MD5
                            String[] parts = line.split(Main.myMD5);
                            if(parts[0] != null) {
                                text += parts[0];
                            }
                            text += "<b><u>" + Main.myMD5 + "</u></b>";
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
                                    Desktop.getDesktop().browse(he.getURL().toURI());
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
        if(Main.indev) {
            return;
        }
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
//                        error("Could not obtain latest changelog from internet.");
                        return;
                    }
                    reader.close();
                    
                    boolean equal = md5.equals(Main.myMD5);
                    
                    logger.log(Level.INFO, "{0} ={1}= {2}", new Object[]{md5, equal ? "" : "/", Main.myMD5});
                    
                    if(!equal) {
                        updateButton.setEnabled(true);
                        int returnCode = JOptionPane.showConfirmDialog(null, "Would you like to update to the latest version?", "A new update is available", JOptionPane.YES_NO_OPTION);
                        if(returnCode == JOptionPane.YES_OPTION) {
                            long startTime = System.currentTimeMillis();
                            
                            System.out.println("Connecting to Dropbox...\n");
                            
                            URL latest = new URL("https://dl.dropbox.com/u/42745598/tf/Hud%20Editor/TF2%20HUD%20Editor.jar");
                            URLConnection editor = latest.openConnection();
                            
                            JProgressBar pb = new JProgressBar(0,editor.getContentLength());
//                            pb.setPreferredSize(new Dimension(175,20));
                            pb.setStringPainted(true);
                            pb.setValue(0);
                            
//                            JLabel label = new JLabel("Update Progress: ");
                            
//                            JPanel center_panel = new JPanel();
//                            center_panel.add(label);
//                            center_panel.add(pb);
                            
//                            JDialog dialog = new JDialog((JFrame) null, "Updating...");
//                            dialog.getContentPane().add(center_panel, BorderLayout.CENTER);
//                            dialog.pack();
//                            dialog.setVisible(true);
                            
                            statusBar.remove(updateButton);
                            statusBar.add(pb, BorderLayout.EAST);
                            
//                            dialog.setLocationRelativeTo(null); // center on screen
//                            dialog.toFront(); // raise above other java windows
                            
                            InputStream in = latest.openStream();
                            
                            
                            FileOutputStream writer = new FileOutputStream(Main.runPath); // TODO: stop closing when this happens. Maybe make a backup..
                            byte[] buffer = new byte[153600]; // 150KB
                            int totalBytesRead = 0;
                            int bytesRead;
                            
                            logger.info("Downloading JAR file in 150KB blocks at a time.\n");
                            
                            updating = true;
                            
                            while((bytesRead = in.read(buffer)) > 0) {
                                writer.write(buffer, 0, bytesRead); // I don't want to write directly over the top until I have all the data..
                                buffer = new byte[153600];
                                totalBytesRead += bytesRead;
                                pb.setValue(totalBytesRead);
                            }
                            
                            long endTime = System.currentTimeMillis();
                            
                            logger.log(Level.INFO, "Done. {0} bytes read ({1} millseconds).\n", new Object[]{new Integer(totalBytesRead).toString(), new Long(endTime - startTime).toString()});
                            writer.close();
                            in.close();
                            
//                            dialog.dispose();
                            
                            info("Downloaded the latest version. Please restart now.");
                            
                            updating = false;
                            
                            final JButton rb = new JButton("Restart");
                            rb.setAction(new Action() {

                                public Object getValue(String string) {
                                    throw new UnsupportedOperationException("Not supported yet.");
                                }

                                public void putValue(String string, Object o) {
                                    throw new UnsupportedOperationException("Not supported yet.");
                                }

                                public void setEnabled(boolean bln) {
                                    rb.setEnabled(bln);
                                }

                                public boolean isEnabled() {
                                    return rb.isEnabled();
                                }

                                public void addPropertyChangeListener(PropertyChangeListener pl) {
                                    throw new UnsupportedOperationException("Not supported yet.");
                                }

                                public void removePropertyChangeListener(PropertyChangeListener pl) {
                                    throw new UnsupportedOperationException("Not supported yet.");
                                }

                                public void actionPerformed(ActionEvent ae) {
                                    try {
                                        restart();
                                    } catch (URISyntaxException ex) {
                                        Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (IOException ex) {
                                        Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                            });
                            statusBar.remove(pb);
                            statusBar.add(rb, BorderLayout.EAST);
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
    
    public void preferences() {
        String aboutText = "This is where preferences will go for the editor.\n";
        aboutText += "There are currently none at the moment";
        final JEditorPane panel = new JEditorPane("text/html", aboutText);
        panel.setEditable(false);
        panel.setOpaque(false);
        info(panel, "About");
    }
    
    public void about() {
        String latestThread = "http://www.reddit.com/r/truetf2/comments/11xtwz/wysiwyg_hud_editor_coming_together/";
        String aboutText = "<html><h2>This is a <u>W</u>hat <u>Y</u>ou <u>S</u>ee <u>I</u>s <u>W</u>hat <u>Y</u>ou <u>G</u>et HUD Editor for TF2.</h2>";
        aboutText += "<p>You can graphically edit TF2 HUDs with it!<br>";
        aboutText += "<p>It was written by <a href=\"http://www.reddit.com/user/TimePath/\">TimePath</a></p>";
        aboutText += "<p>Source available on <a href=\"http://code.google.com/p/tf2-hud-editor/\">Google code</a></p>";
        aboutText += "<p>I have an <a href=\"http://code.google.com/feeds/p/tf2-hud-editor/hgchanges/basic\">Atom feed</a> set up listing source commits</p>";
        aboutText += "<p>Please give feedback or suggestions on <a href=\""+latestThread+"\">the latest update thread</a></p>";
        aboutText += "<p>Current version: " + Main.myMD5 + "</p>";
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
    
    public void quit() {
        if(!updating) {
            System.out.println("Quitting...");
            if(Main.os == OS.Mac) {
                JFrame f = new JFrame();
                f.setUndecorated(true);
                f.setJMenuBar(this.getJMenuBar());
                f.setLocation(-Integer.MAX_VALUE, -Integer.MAX_VALUE); // Hacky - should just use the Application calls...
                f.setVisible(true);
            }
            this.dispose();
        }
    }

    public EditorFrame() {
        URL url = getClass().getResource("/com/timepath/tf2/hudeditor/resources/Icon.png");
        Image icon = Toolkit.getDefaultToolkit().getImage(url);
        this.setIconImage(icon);
        this.setTitle(Main.rb.getString("Title"));
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
            private static final long serialVersionUID = 1L;
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
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        this.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
        
        //<editor-fold defaultstate="collapsed" desc="Tree">
        EditorPropertiesTablePane propTablePane = new EditorPropertiesTablePane();
        propTable = propTablePane.getPropTable();
        
        hudFilesRoot = new DefaultMutableTreeNode(null);
        fileSystem = new JTree(hudFilesRoot);
        EditorFileTreePane fileTreePane = new EditorFileTreePane(canvas, propTable, fileSystem);
        
        JSplitPane browser = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fileTreePane, propTablePane);
        browser.setResizeWeight(0.5);
        browser.setMinimumSize(new Dimension(100, 0));
        browser.setPreferredSize(new Dimension(300, 0));
        browser.setSize(browser.getPreferredSize());
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Canvas">
        canvas = new EditorCanvas();
        canvasPane = new JScrollPane(canvas);
        if(Main.os == OS.Mac) {
            canvasPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            canvasPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        } else {
            canvasPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            canvasPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        }
        //</editor-fold>
        
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, browser, canvasPane);
        splitPane.setDividerSize(3);
        splitPane.setResizeWeight(0);
        panel.add(splitPane, BorderLayout.CENTER);

        this.pack();
        this.setFocusableWindowState(true);
        
        jmb = new EditorMenuBar();
        this.setJMenuBar(jmb);
        
        if(Main.os == OS.Mac) {
            createMacMenus();
        } else if(Main.os == OS.Linux) {
            try {
                if(AyatanaDesktop.isSupported()) {
                    boolean worked = ApplicationMenu.tryInstall(this, jmb);
                    if(worked) {
                        this.setJMenuBar(null);
                    } else {
                        error("AyatanaDesktop failed to load" + "\n" + System.getenv("XDG_CURRENT_DESKTOP"));
                    }
                }
            } catch(UnsupportedClassVersionError e) { // crashes earlier versions of the JVM - particularly old macs
    //            e.printStackTrace();
            }
        }
        
        statusBar = new JToolBar();
        statusBar.putClientProperty("Quaqua.ToolBar.style", "bottom");
        statusBar.setFloatable(false);
        statusBar.setLayout(new BorderLayout());
        
        statusBar.add(new JLabel("By TimePath"));
        
        updateButton = new JButton(Main.rb.getString("Update"));
        updateButton.setEnabled(false);
        
        statusBar.add(updateButton, BorderLayout.EAST);
        
        panel.add(statusBar, BorderLayout.SOUTH);
        
        this.add(panel);
    }
    
    private void createMacMenus() {
        try {
            OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));
            OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
            OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[]) null));
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
            logger.severe(e.toString());
        }
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        this.createBufferStrategy(3); // Triple buffered, any more sees minimal gain.
        if(!Main.indev && autoCheck) {
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
    
    private File lastLoaded; // convenience
    
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
    
    public static String locateSteamAppsDirectory() {
        String str;
        if(Main.os == OS.Windows) {
            str = System.getenv("PROGRAMFILES(x86)");
            if(str == null) {
                str = System.getenv("PROGRAMFILES");
            }
        } else if(Main.os == OS.Mac) {
            str = "~/Library/Application Support";
        } else if(Main.os == OS.Linux) {
            str = System.getenv("HOME") + "/.local/share"; // XDG_DATA_HOME ?
        } else {
            return null;
        }
        return str + "/Steam/SteamApps/";
    }

    /**
     * Start in the home directory
     * System.getProperty("user.home")
     * linux = ~
     * windows = %userprofile%
     * mac = ?
     */
    private void locateHudDirectory() {
//        NativeFileChooser nc = new NativeFileChooser(EditorFrame.this, "Open a HUD folder", hudSelectionDir);
//        final File selection = nc.getFolder();
//        if(selection != null) {
//            hudSelectionDir = selection.getParent();
//
//            new Thread() {
//                @Override
//                public void run() {
//                    loadHud(selection);
//                }
//            }.start();
//        } else {
//            // Throw error or load archive
//        }
        
        if(Main.os == OS.Mac) {
            locateMac();
        } else if(Main.os == OS.Windows) {
            try {
                new Thread(new Runnable() {
                    public void run() {
                        String selection = null;
                        try {
                            XFileDialog fd = new XFileDialog(EditorFrame.this);
                            fd.setTitle(Main.rb.getString("LoadHudDir"));
                            selection = fd.getFolder();
                            fd.dispose();
                        } catch(UnsupportedClassVersionError e) {

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
            } catch(UnsupportedClassVersionError e) {
                
            }
        } else {
            locate();
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Locate">
    private void locateMac() {
        new Thread(new Runnable() {
            public void run() {
                String selection = null;
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
                FileDialog fd = new FileDialog(EditorFrame.this, Main.rb.getString("LoadHudDir"));
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
                if(fd.getDirectory() == null || fd.getFile() == null) {
                    return;
                }
                String file = fd.getDirectory() + fd.getFile();
                if(file != null) {
                    hudSelectionDir = new File(file).getParent();
                    selection = file;
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
    
    private void locate() {
        new Thread(new Runnable() {
            public void run() {
                String selection = null;
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
                //        } else { // Fall back to swing
                JFileChooser fd = new JFileChooser();
                fd.setDialogTitle(Main.rb.getString("LoadHudDir"));
                fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if(hudSelectionDir != null) {
                    fd.setCurrentDirectory(new File(hudSelectionDir));
                }
                if(fd.showOpenDialog(EditorFrame.this) == JFileChooser.APPROVE_OPTION) {
                    hudSelectionDir = fd.getSelectedFile().getParent();
                    selection = fd.getSelectedFile().getPath();
                }
                //        }
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
    //</editor-fold>
    
    private void error(Object msg) {
        error(msg, Main.rb.getString("Error"));
    }
    
    private void error(Object msg, String title) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }
    
    private void info(Object msg) {
        info(msg, Main.rb.getString("Info"));
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
            error(new MessageFormat(Main.rb.getString("FileAccessError")).format(new Object[]{file}));
        }
        setLastLoaded(file);
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
        
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();
        for (int i = 0; i < devices.length; i++) {
            DisplayMode[] resolutions = devices[i].getDisplayModes();
            for(int j = 0; j < resolutions.length; j++) {
                System.out.println(resolutions[j].getWidth() + "x" + resolutions[j].getHeight());
            }
        }
        
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

    private void setLastLoaded(File file) {
        lastLoaded = file;
        jmb.reloadItem.setEnabled(file != null);
    }
    
    private class EditorMenuBar extends JMenuBar {
        private static final long serialVersionUID = 1L;
        
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
        private JMenuItem captionItem;
        
        EditorActionListener al = new EditorActionListener();

        EditorMenuBar() {
            super();
            
            JMenu fileMenu = new JMenu("File");
            fileMenu.setMnemonic(KeyEvent.VK_F);
            this.add(fileMenu);
            
            newItem = new JMenuItem("New", KeyEvent.VK_N);
            newItem.setEnabled(false);
            newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            newItem.addActionListener(al);
            fileMenu.add(newItem);
            
            openItem = new JMenuItem("Open...", KeyEvent.VK_O);
            openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            openItem.addActionListener(al);
            fileMenu.add(openItem);
            
            openZippedItem = new JMenuItem("Open Zip...", KeyEvent.VK_Z);
            openZippedItem.setEnabled(false);
            openZippedItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + ActionEvent.SHIFT_MASK));
            openZippedItem.addActionListener(al);
            fileMenu.add(openZippedItem);
            
            if(Main.os == OS.Mac) {
                fileMenu.addSeparator();
            
                closeItem = new JMenuItem("Close", KeyEvent.VK_C);
                closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                closeItem.addActionListener(al);
                fileMenu.add(closeItem);
            }
            
            saveItem = new JMenuItem("Save", KeyEvent.VK_S);
            saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            saveItem.addActionListener(al);
            fileMenu.add(saveItem);
            
            saveAsItem = new JMenuItem("Save As...", KeyEvent.VK_A);
            saveAsItem.setEnabled(false);
            saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + ActionEvent.SHIFT_MASK));
            saveAsItem.addActionListener(al);
            fileMenu.add(saveAsItem);
            
            reloadItem = new JMenuItem("Revert", KeyEvent.VK_R);
            reloadItem.setEnabled(false);
            reloadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
            reloadItem.addActionListener(al);
            fileMenu.add(reloadItem);

            if(Main.os != OS.Mac) {
                fileMenu.addSeparator();
                
                closeItem = new JMenuItem("Close", KeyEvent.VK_C);
                closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                closeItem.addActionListener(al);
                fileMenu.add(closeItem);
            
                fileMenu.addSeparator();

                exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
                exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                exitItem.addActionListener(al);
                fileMenu.add(exitItem);
            }

            JMenu editMenu = new JMenu("Edit");
            editMenu.setMnemonic(KeyEvent.VK_E);
            this.add(editMenu);
            
            undoItem = new JMenuItem("Undo", KeyEvent.VK_U);
            undoItem.setEnabled(false);
            undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            undoItem.addActionListener(al);
            editMenu.add(undoItem);
            
            redoItem = new JMenuItem("Redo", KeyEvent.VK_R);
            redoItem.setEnabled(false);
            redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + ActionEvent.SHIFT_MASK));
            redoItem.addActionListener(al);
            editMenu.add(redoItem);
            
            editMenu.addSeparator();
            
            cutItem = new JMenuItem("Cut", KeyEvent.VK_T);
            cutItem.setEnabled(false);
            cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            cutItem.addActionListener(al);
            editMenu.add(cutItem);
            
            copyItem = new JMenuItem("Copy", KeyEvent.VK_C);
            copyItem.setEnabled(false);
            copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            copyItem.addActionListener(al);
            editMenu.add(copyItem);
            
            pasteItem = new JMenuItem("Paste", KeyEvent.VK_P);
            pasteItem.setEnabled(false);
            pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            pasteItem.addActionListener(al);
            editMenu.add(pasteItem);
            
            deleteItem = new JMenuItem("Delete");
            deleteItem.setEnabled(false);
            deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            deleteItem.addActionListener(al);
            editMenu.add(deleteItem);
            
            editMenu.addSeparator();

            selectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
            selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            selectAllItem.addActionListener(al);
            editMenu.add(selectAllItem);
            
            if(Main.os != OS.Mac) {
                editMenu.addSeparator();

                preferencesItem = new JMenuItem("Preferences", KeyEvent.VK_E);
                preferencesItem.addActionListener(al);
                editMenu.add(preferencesItem);
            }

            JMenu viewMenu = new JMenu("View");
            viewMenu.setMnemonic(KeyEvent.VK_V);
            this.add(viewMenu);

            resolutionItem = new JMenuItem("Change Resolution", KeyEvent.VK_R);
            resolutionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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
            
            extras();

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
        }
        
        private void extras() {
            JMenu extrasMenu = new JMenu("Extras");
            extrasMenu.setMnemonic(KeyEvent.VK_X);
            this.add(extrasMenu);

            vtfItem = new JMenuItem("VTF Loader", KeyEvent.VK_V);
            vtfItem.addActionListener(al);
            extrasMenu.add(vtfItem);
            
            captionItem = new JMenuItem("Caption Viewer", KeyEvent.VK_C);
            captionItem.addActionListener(al);
            extrasMenu.add(captionItem);
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
                } else if(cmd == preferencesItem) {
                    preferences();
                } else if(cmd == updateItem) {
                    EditorFrame.this.checkForUpdates();
                } else if(cmd == changeLogItem) {
                    changelog();
                } else if(cmd == vtfItem) {
                    VtfLoader.main("");
                } else if(cmd == captionItem) {
                    CaptionLoaderFrame.main("");
                }
            }
        }
    }
    private static final Logger logger = Logger.getLogger(EditorFrame.class.getName());
}
