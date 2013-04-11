package com.timepath.tf2.hudeditor;

import apple.OSXAdapter;
import com.timepath.Utils;
import com.timepath.hl2.io.VTF;
import com.timepath.hl2.io.test.VBFTest;
import com.timepath.hl2.io.test.VCCDTest;
import com.timepath.hl2.io.test.VTFTest;
import com.timepath.hl2.io.util.Element;
import com.timepath.hl2.io.util.Property;
import com.timepath.plaf.OS;
import com.timepath.plaf.linux.Ayatana;
import com.timepath.plaf.linux.GtkFixer;
import com.timepath.plaf.mac.Application;
import com.timepath.plaf.mac.Application.AboutEvent;
import com.timepath.plaf.mac.Application.AboutHandler;
import com.timepath.plaf.mac.Application.PreferencesEvent;
import com.timepath.plaf.mac.Application.PreferencesHandler;
import com.timepath.plaf.mac.Application.QuitEvent;
import com.timepath.plaf.mac.Application.QuitHandler;
import com.timepath.plaf.mac.Application.QuitResponse;
import com.timepath.plaf.x.filechooser.BaseFileChooser;
import com.timepath.plaf.x.filechooser.FileChooserTest;
import com.timepath.plaf.x.filechooser.NativeFileChooser;
import com.timepath.steam.SteamUtils;
import com.timepath.steam.io.GCF;
import com.timepath.steam.io.GCF.DirectoryEntry;
import com.timepath.steam.io.RES;
import com.timepath.steam.io.VDF;
import com.timepath.steam.io.test.ArchiveTest;
import com.timepath.steam.io.test.DataTest;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author timepath
 */
@SuppressWarnings("serial")
public class EditorFrame extends javax.swing.JFrame {

    private static final Logger LOG = Logger.getLogger(EditorFrame.class.getName());

    //<editor-fold defaultstate="collapsed" desc="Variables">
    private final EditorMenuBar jmb;

    private final DefaultMutableTreeNode fileSystemRoot;

    private final FileTree fileTree;

    private final PropertyTable propTable;

    private static Canvas canvas;

    private boolean updating;

    public boolean autoCheck = true;

    private File lastLoaded;

    private JSpinner spinnerWidth;

    private JSpinner spinnerHeight;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Dialogs">
    //<editor-fold defaultstate="collapsed" desc="Updates">
    private BufferedReader getPage(String s) throws IOException {
        URL u = new URL(s);
        URLConnection c = u.openConnection();
//        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        LOG.log(Level.INFO, "{0} size: {1}", new Object[]{s, c.getContentLength()});
        InputStream is = c.getInputStream();
        return new BufferedReader(new InputStreamReader(is));
    }

    private String currentVersion() throws IOException {
        BufferedReader r = getPage("https://dl.dropbox.com/u/42745598/tf/Hud%20Editor/TF2%20HUD%20Editor.jar.current");
        String l = r.readLine();
        r.close();
        return l;
    }

    private String checksum() throws IOException {
        BufferedReader r = getPage("https://dl.dropbox.com/u/42745598/tf/Hud%20Editor/TF2%20HUD%20Editor.jar.MD5");
        String l = r.readLine();
        r.close();
        return l;
    }

    private String changelog() throws IOException {
        BufferedReader r = getPage("https://dl.dropbox.com/u/42745598/tf/Hud%20Editor/TF2%20HUD%20Editor.jar.changes");
        String text = "";
        String grep = Main.myVer;
        String line;
        while((line = r.readLine()) != null) {
            if(grep != null && line.contains(grep)) {
                text += line.replace(grep, "<b><u>" + grep + "</u></b>");
            } else {
                text += line;
            }
        }
        r.close();
        return text;
    }

    private void checkForUpdates(final boolean force) {
        new Thread() {
            int retries = 3;

            private String calculateTime(long diffInSeconds) {
                StringBuilder sb = new StringBuilder();

                long sec = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
                long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
                long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
                //<editor-fold defaultstate="collapsed" desc="approximate d/m/y">
                
                //                long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
                //                long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
                //                long years = (diffInSeconds = (diffInSeconds / 12));
                
                //                if(years > 0) {
                //                    if(years == 1) {
                //                        sb.append("a year");
                //                    } else {
                //                        sb.append(years + " years");
                //                    }
                //                    if(years <= 6 && months > 0) {
                //                        if(months == 1) {
                //                            sb.append(" and a month");
                //                        } else {
                //                            sb.append(" and " + months + " months");
                //                        }
                //                    }
                //                } else if(months > 0) {
                //                    if(months == 1) {
                //                        sb.append("a month");
                //                    } else {
                //                        sb.append(months + " months");
                //                    }
                //                    if(months <= 6 && days > 0) {
                //                        if(days == 1) {
                //                            sb.append(" and a day");
                //                        } else {
                //                            sb.append(" and " + days + " days");
                //                        }
                //                    }
                //                } else if(days > 0) {
                //                    if(days == 1) {
                //                        sb.append("a day");
                //                    } else {
                //                        sb.append(days + " days");
                //                    }
                //                    if(days <= 3 && hrs > 0) {
                //                        if(hrs == 1) {
                //                            sb.append(" and an hour");
                //                        } else {
                //                            sb.append(" and " + hrs + " hours");
                //                        }
                //                    }
                //                } else
                //</editor-fold>
                    if(hrs > 0) {
                    if(hrs == 1) {
                        sb.append("an hour");
                    } else {
                        sb.append(hrs + " hours");
                    }
                    if(min > 1) {
                        sb.append(" and " + min + " minutes");
                    }
                } else if(min > 0) {
                    if(min == 1) {
                        sb.append("a minute");
                    } else {
                        sb.append(min + " minutes");
                    }
                    if(sec > 1) {
                        sb.append(" and " + sec + " seconds");
                    }
                } else {
                    if(sec <= 1) {
                        sb.append("about a second");
                    } else {
                        sb.append("about " + sec + " seconds");
                    }
                }

                sb.append(" ago");

                return sb.toString();
            }

            private void doCheckForUpdates() {
                try {
                    String current = currentVersion();
                    final long lastUpdate = Long.parseLong(current);
                    boolean equal = current.equals(Main.myVer);
                    LOG.log(Level.INFO, "{0} ={1}= {2}", new Object[]{current, equal ? "" : "/", Main.myVer});

                    if(current.compareTo(Main.myVer) <= 0 && !force) {
                        return;
                    }
                    
                    String text = changelog();

                    final JEditorPane pane = new JEditorPane("text/html", text);
                    Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
                    pane.setPreferredSize(new Dimension(s.width / 4, s.height / 2));
                    pane.setEditable(false);
                    pane.setOpaque(false);
                    pane.setBackground(new Color(0, 0, 0, 0));
                    pane.addHyperlinkListener(linkListener);
                    JScrollPane window = new JScrollPane(pane);
                    window.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

                    final JLabel lastUpdated = new JLabel();

                    new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            lastUpdated.setText(calculateTime((System.currentTimeMillis() - lastUpdate) / 1000));
                        }
                    }).start();

                    if((Main.myVer == null || current.compareTo(Main.myVer) > 0)) {
//                        updateButton.setEnabled(true);
                        int returnCode = JOptionPane.showConfirmDialog(EditorFrame.this, new JComponent[]{new JLabel("Would you like to update to the latest version?"), new JLabel(), window, lastUpdated}, "A new update is available", JOptionPane.YES_NO_OPTION);
                        if(returnCode == JOptionPane.YES_OPTION) {
                            String md5 = checksum();

                            final File downloaded = new File(Utils.workingDirectory(EditorFrame.class) + "update.tmp");
                            for(int attempt = 1;; attempt++) {
                                LOG.log(Level.INFO, "Checking for {0}", downloaded);
                                if(!downloaded.exists()) {
                                    long startTime = System.currentTimeMillis();

                                    LOG.info("Connecting to Dropbox...");

                                    URL latest = new URL("https://dl.dropbox.com/u/42745598/tf/Hud%20Editor/TF2%20HUD%20Editor.jar");
                                    URLConnection editor = latest.openConnection();

                                    JProgressBar pb = new JProgressBar(0, editor.getContentLength());
                                    pb.setPreferredSize(new Dimension(175, 20));
                                    pb.setStringPainted(true);
                                    pb.setValue(0);

//                                    JLabel label = new JLabel("Update Progress: ");

//                                    JPanel center_panel = new JPanel();
//                                    center_panel.add(label);
//                                    center_panel.add(pb);

//                                    statusBar.remove(updateButton);
                                    status.add(pb);
                                    status.revalidate();

                                    InputStream in = latest.openStream();

//                                    Utils.workingDirectory(EditorFrame.class)

                                    updating = true;

                                    LOG.info("Downloading JAR file in 150KB blocks at a time.\n");
                                    FileOutputStream writer = new FileOutputStream(downloaded);
                                    byte[] buffer = new byte[153600]; // 150KB
                                    int totalBytesRead = 0;
                                    int bytesRead;
                                    while((bytesRead = in.read(buffer)) > 0) {
                                        writer.write(buffer, 0, bytesRead);
                                        buffer = new byte[153600];
                                        totalBytesRead += bytesRead;
                                        pb.setValue(totalBytesRead);
                                    }

                                    long endTime = System.currentTimeMillis();

                                    LOG.log(Level.INFO, "Done. {0} kilobytes downloaded ({1} seconds).\n", new Object[]{new Integer(totalBytesRead / 1000).toString(), new Long((endTime - startTime) / 1000).toString()});
                                    writer.close();
                                    in.close();
//                                    dialog.dispose();
                                    status.remove(pb);
                                } else {
                                    LOG.info("Exists");
                                }

                                LOG.info("Checking MD5...");
                                if(!Utils.takeMD5(Utils.loadFile(downloaded)).equalsIgnoreCase(md5)) {
                                    LOG.warning("Corrupt or old download");
                                } else {
                                    LOG.info("MD5 matches");
                                    break;
                                }

                                if(attempt == retries) {
                                    LOG.warning("Update failed");
                                    return;
                                }
                            }

                            info("Restart to apply update to " + Utils.workingDirectory(EditorFrame.class));

                            updating = false;
                        }
                    } else if(force) {
                        JOptionPane.showMessageDialog(EditorFrame.this, new JComponent[]{new JLabel("You have the latest version."), new JLabel(""), window, lastUpdated}, "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch(NoSuchAlgorithmException ex) {
                } catch(IOException ex) {
                    retries--;
                    if(retries > 0) {
                        doCheckForUpdates();
                    } else {
                        LOG.log(Level.SEVERE, null, ex);
                        updating = false;
                    }
                }
            }

            @Override
            public void run() {
                doCheckForUpdates();
                autoCheck = false;
            }
        }.start();
    }
    //</editor-fold>

    public void preferences() {
        String aboutText = "This is where preferences will go for the editor.\n";
        aboutText += "There are none at this time";
        JEditorPane pane = new JEditorPane("text/html", aboutText);
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setBackground(new Color(0, 0, 0, 0));
        info(pane, "About");
    }

    public void about() {
        String latestThread = "http://www.reddit.com/r/truetf2/comments/186krr/wysiwyg_hud_editor_not_dead/";
        String aboutText = "<html><h2>This is a What You See Is What You Get HUD Editor for TF2.</h2>";
        aboutText += "<p>You can graphically edit TF2 HUDs with it!<br>";
        aboutText += "<p>It was written by <a href=\"http://www.reddit.com/user/TimePath/\">TimePath</a></p>";
        aboutText += "<p>Source available on <a href=\"http://code.google.com/p/tf2-hud-editor/\">Google code</a></p>";
        aboutText += "<p>I have an <a href=\"http://code.google.com/feeds/p/tf2-hud-editor/hgchanges/basic\">Atom feed</a> set up listing source commits</p>";
        aboutText += "<p>Please leave feedback or suggestions on <a href=\"" + latestThread + "\">the latest update thread</a></p>";
        aboutText += "<p>Logging to <a href=\"" + new File(Main.logFile).toURI() + "\">" + Main.logFile + "</a></p>";
        if(Main.myVer != null) {
            long time = Long.parseLong(Main.myVer);
            DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            aboutText += "<p>Build date: " + df.format(new Date(time * 1000)) + " (" + time + ")</p>";
        }
        aboutText += "</html>";
        JEditorPane pane = new JEditorPane("text/html", aboutText);
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setBackground(new Color(0, 0, 0, 0));
        pane.addHyperlinkListener(linkListener);
        info(pane, "About");
    }

    private void locateUserDirectory() {
        FilenameFilter dirFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        };

        JComboBox dropDown = new JComboBox(); // <String>
        String location = SteamUtils.locateSteamAppsDirectory();
        if(location == null) {
            error("Could not find Steam install directory!", "Steam not found");
            return;
        }
        File steamappsFolder = new File(location);
        File[] userFolders = steamappsFolder.listFiles(dirFilter);
        if(userFolders == null) {
            error("SteamApps is empty!", "Empty SteamApps directory");
            return;
        }
        for(int i = 0; i < userFolders.length; i++) {
            if(userFolders[i].getName().equalsIgnoreCase("common") || userFolders[i].getName().equalsIgnoreCase("sourcemods")) {
                continue;
            }
            File[] gameFolders = userFolders[i].listFiles(dirFilter);
            for(int j = 0; j < gameFolders.length; j++) {
                if(gameFolders[j].getName().equalsIgnoreCase("Team Fortress 2")) {
                    dropDown.addItem(userFolders[i].getName());
                    break;
                }
            }
        }
        if(dropDown.getItemCount() == 0) {
            error("No users have TF2 installed!", "TF2 not found");
            return;
        }
        if(dropDown.getItemCount() > 1) {
            JPanel dialogPanel = new JPanel();
            dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
            dialogPanel.add(new JLabel("Please choose for which user you want to install the HUD"));
            dialogPanel.add(dropDown);
            JOptionPane.showMessageDialog(this, dialogPanel, "Select user", JOptionPane.QUESTION_MESSAGE);
        }
        File installDir = new File(steamappsFolder, dropDown.getSelectedItem() + "/Team Fortress 2/tf");
        if(installDir.isDirectory() && installDir.exists()) {
            info("Install path: " + installDir, "Install path");
        }
    }

    /**
     * Start in the home directory
     * System.getProperty("user.home")
     * linux = ~
     * windows = %userprofile%
     * mac = ?
     */
    private void locateHudDirectory() {
        new Thread() {
            @Override
            public void run() {
                try {
                    final File selection = new NativeFileChooser().setParent(EditorFrame.this).setTitle(Main.getString("LoadHudDir")).setDirectory(lastLoaded != null ? lastLoaded.getPath() : null).setFileMode(BaseFileChooser.FileMode.DIRECTORIES_ONLY).choose();
                    if(selection != null) {
                        new Thread() {
                            @Override
                            public void run() {
                                load(selection);
                            }
                        }.start();
                    } else {
                    }
                } catch(IOException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    private void locateZippedHud() {
    }

    //<editor-fold defaultstate="collapsed" desc="Messages">
    private void error(Object msg) {
        error(msg, Main.getString("Error"));
    }

    private void error(Object msg, String title) {
        LOG.log(Level.SEVERE, "{0}:{1}", new Object[]{title, msg});
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private void warn(Object msg) {
        error(msg, Main.getString("Warning"));
    }

    private void warn(Object msg, String title) {
        LOG.log(Level.WARNING, "{0}:{1}", new Object[]{title, msg});
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    private void info(Object msg) {
        info(msg, Main.getString("Info"));
    }

    private void info(Object msg, String title) {
        LOG.log(Level.INFO, "{0}:{1}", new Object[]{title, msg});
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }
    //</editor-fold>

    private void changeResolution() {

        //<editor-fold defaultstate="collapsed" desc="Number filter">
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
        //</editor-fold>

//            spinnerWidth = new JSpinner(new SpinnerNumberModel(canvas.screen.width, 640, 7680, 1)); // WHUXGA
        spinnerWidth.setEnabled(false);
//            NumberEditor jsWidth = (NumberEditor) spinnerWidth.getEditor();
//            final Document jsWidthDoc = jsWidth.getTextField().getDocument();
//            if(jsWidthDoc instanceof PlainDocument) {
//                AbstractDocument docWidth = new PlainDocument() {
//
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
//            spinnerHeight = new JSpinner(new SpinnerNumberModel(canvas.screen.height, 480, 4800, 1)); // WHUXGA
        spinnerHeight.setEnabled(false);
//            NumberEditor jsHeight = (NumberEditor) spinnerHeight.getEditor();
//            final Document jsHeightDoc = jsHeight.getTextField().getDocument();
//            if(jsHeightDoc instanceof PlainDocument) {
//                AbstractDocument docHeight = new PlainDocument() {
//
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
        final JComboBox dropDown = new JComboBox(); // <String>

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();
        ArrayList<String> listItems = new ArrayList<String>();
        for(int i = 0; i < devices.length; i++) {
            DisplayMode[] resolutions = devices[i].getDisplayModes(); // TF2 has different resolutions
            for(int j = 0; j < resolutions.length; j++) {
                String item = resolutions[j].getWidth() + "x" + resolutions[j].getHeight(); // TODO: Work out aspect ratios
                if(!listItems.contains(item)) {
                    listItems.add(item);
                }
            }
        }
        dropDown.addItem("Custom");
        for(int i = 0; i < listItems.size(); i++) {
            dropDown.addItem(listItems.get(i));
        }
        dropDown.setSelectedIndex(1);
        dropDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String item = dropDown.getSelectedItem().toString();
                boolean isRes = item.contains("x");
                spinnerWidth.setEnabled(!isRes);
                spinnerHeight.setEnabled(!isRes);
                if(isRes) {
                    String[] xy = item.split("x");
                    spinnerWidth.setValue(Integer.parseInt(xy[0]));
                    spinnerHeight.setValue(Integer.parseInt(xy[1]));
                }
            }
        });

        Object[] message = {"Presets: ", dropDown, "Width: ", spinnerWidth, "Height: ", spinnerHeight};

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
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Overrides">
    @Override
    public void setJMenuBar(JMenuBar menubar) {
        LOG.log(Level.INFO, "Setting menubar for {0}", OS.get());
        super.setJMenuBar(menubar);
        if(OS.isMac()) {
            try {
                //<editor-fold defaultstate="collapsed" desc="Deprecated">
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
                OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[]) null));
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Unimplemented">
                Application app = Application.getApplication();

                app.setAboutHandler(new AboutHandler() {
                    public void handleAbout(AboutEvent e) {
                        about();
                    }
                });
                app.setPreferencesHandler(new PreferencesHandler() {
                    public void handlePreferences(PreferencesEvent e) {
                        preferences();
                    }
                });
                app.setQuitHandler(new QuitHandler() {
                    public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
                        quit();
                    }
                });
                URL url = getClass().getResource("/com/timepath/tf2/hudeditor/resources/Icon.png");
                Image icon = Toolkit.getDefaultToolkit().getImage(url);
                app.setDockIconImage(icon);
                //</editor-fold>
            } catch(Exception e) {
                LOG.severe(e.toString());
            }
        } else if(OS.isLinux()) {
            if(!Ayatana.installMenu((JFrame) this, menubar)) {
                LOG.log(Level.WARNING, "AyatanaDesktop failed to load for {0}", System.getenv("XDG_CURRENT_DESKTOP"));
            }
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        this.createBufferStrategy(2);
        track("ProgramLoad");
        if(Main.myVer != null && autoCheck) {
            this.checkForUpdates(false);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Actions">
//    private void close() {
//        canvas.removeAllElements();
//
//        fileSystemRoot.removeAllChildren();
//        fileSystemRoot.setUserObject(null);
//        DefaultTreeModel model1 = (DefaultTreeModel) fileTree.getModel();
//        model1.reload();
//        fileTree.setSelectionRow(0);
//
//        propTable.clear();
//    }
    private void load(final File root) {
        if(root == null) {
            return;
        }
        if(!root.exists()) {
            error(new MessageFormat(Main.getString("FileAccessError")).format(new Object[]{root}));
        }
        setLastLoaded(root);
        LOG.log(Level.INFO, "You have selected: {0}", root.getAbsolutePath());

        if(root.getName().endsWith(".zip")) {
            try {
                ZipInputStream zin = new ZipInputStream(new FileInputStream(root));
                ZipEntry entry;
                while((entry = zin.getNextEntry()) != null) {
                    LOG.log(Level.INFO, "{0}", entry.getName());
                    zin.closeEntry();
                }
                zin.close();
            } catch(IOException e) {
            }
            return;
        }

        if(root.isDirectory()) {
            File[] folders = root.listFiles();
            boolean valid = true; // TODO: find resource and scripts if there is a parent directory
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
//            close();

            new Thread(new Runnable() {
                public void run() {
//                    SwingUtilities.invokeLater(new Runnable() {
//                        @Override
//                        public void run() {
                    EditorFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//                        }
//                    });
                    final DefaultMutableTreeNode project = new DefaultMutableTreeNode();
                    project.setUserObject(root.getName());
                    connectNodes(fileSystemRoot, project);
                    DefaultTreeModel model = (DefaultTreeModel) fileTree.getModel();
                    model.reload();
                    final long start = System.currentTimeMillis();
                    recurseDirectoryToNode(root, project);
                    LOG.log(Level.INFO, "Loaded hud - took {0}ms", (System.currentTimeMillis() - start));
                    fileTree.expandPath(new TreePath(project.getPath()));
                    fileTree.setSelectionRow(fileSystemRoot.getIndex(project));
                    fileTree.requestFocusInWindow();
                    EditorFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }).start();
        }
    }

    private void connectNodes(final DefaultMutableTreeNode parent, final DefaultMutableTreeNode child) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
//                    parent.add(child);
                    DefaultTreeModel model = (DefaultTreeModel) fileTree.getModel();
                    synchronized(model) {
                        model.insertNodeInto(child, parent, parent.getChildCount());
                    }
                }
            });
        } catch(InterruptedException ex) {
            Logger.getLogger(SteamUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch(InvocationTargetException ex) {
            Logger.getLogger(SteamUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void recurseDirectoryToNode(File root, final DefaultMutableTreeNode parent) {
        final String[] blacklist = {".mp3", ".exe", ".sh", ".dll", ".dylib", ".so",
                                    ".ttf", ".bik", ".mov", ".cfg", ".cache", ".manifest",
                                    ".frag", ".vert", ".tga", ".png", ".html", ".wav",
                                    ".ico", ".uifont", ".xml", ".css", ".dic", ".conf",
                                    ".pak", ".py", ".flt", ".mix", ".asi", ".checksum",
                                    ".xz", ".log", ".doc", ".webm", ".jpg", ".psd", ".avi",
                                    ".zip", ".bin"};
        final File[] fileList = root.listFiles();
        final Thread[] threads = new Thread[fileList.length];
        if(fileList.length == 0) {
            return;
        }
        Arrays.sort(fileList, Utils.ALPHA_COMPARATOR);
        for(int i = 0; i < fileList.length; i++) {
            final File f = fileList[i];
            final DefaultMutableTreeNode child = new DefaultMutableTreeNode();
            child.setUserObject(f); // Unknown = File
            if(f.isDirectory()) {
                //<editor-fold defaultstate="collapsed" desc="Validate">
                if(f.getName().toLowerCase().equals("common")
                   || f.getName().toLowerCase().equals("downloading")
                   || f.getName().toLowerCase().equals("temp")
                   || f.getName().toLowerCase().equals("sourcemods")) {
                    continue;
                }
                if(f.listFiles().length == 0) {
                    continue;
                }
                //</editor-fold>
                connectNodes(parent, child);
                recurseDirectoryToNode(f, child);
            } else {
                //<editor-fold defaultstate="collapsed" desc="Validate">
                boolean flag = false;
                for(int j = 0; j < blacklist.length; j++) {
                    if(f.getName().endsWith(blacklist[j])) {
                        flag = true;
                        break;
                    }
                }
                if(flag) {
                    continue;
                }
                //</editor-fold>
                connectNodes(parent, child);
                threads[i] = new Thread(new Runnable() {
                    public void run() {
                        if(f.getName().endsWith(".txt")
                           || f.getName().endsWith(".vdf")
                           || f.getName().endsWith(".pop")
                           || f.getName().endsWith(".layout")
                           || f.getName().endsWith(".menu")
                           || f.getName().endsWith(".styles")) {
                            VDF.analyze(f, child);
                        } else if(f.getName().endsWith(".res")) {
                            RES.analyze(f, child);
                        } else if(f.getName().endsWith(".vmt")) {
//                            VDF.analyze(f, child);
                        } else if(f.getName().endsWith(".vtf")) {
                            VTF v = null;
                            try {
                                v = VTF.load(new FileInputStream(f));
                            } catch(IOException ex) {
                                Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            if(v != null) {
                                child.setUserObject(v);
                            }
                        } else if(f.getName().endsWith(".gcf")) {
                            try {
                                GCF g = new GCF(f);
                                child.setUserObject(g);
                                g.analyze(child);
                            } catch(IOException ex) {
                                Logger.getLogger(SteamUtils.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                });
                threads[i].start();
            }
        }
        for(int i = 0; i < threads.length; i++) {
            try {
                if(threads[i] != null) {
                    threads[i].join();
                }
            } catch(InterruptedException ex) {
                Logger.getLogger(SteamUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void quit() {
        if(!updating) {
            LOG.info("Quitting...");
            this.dispose();
            if(OS.isMac()) {
//                JFrame f = new JFrame();
//                f.setUndecorated(true);
//                f.setJMenuBar(this.getJMenuBar());
//                f.setLocation(-Integer.MAX_VALUE, -Integer.MAX_VALUE); // Hacky - should just use the OSX Application calls...
//                f.setVisible(true);
            } else {
//                System.exit(0);
            }
        }
    }

    private void setLastLoaded(File root) {
        jmb.reloadItem.setEnabled(root != null);
        if(root == null || !root.exists()) {
            return;
        }
        lastLoaded = root;
        Main.prefs.put("lastLoaded", root.getPath());
    }

    private HyperlinkListener linkListener = new HyperlinkListener() {
        public void hyperlinkUpdate(HyperlinkEvent he) {
            if(he.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                if(Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(he.getURL().toURI());
                    } catch(Exception e) {
                        error(e);
                    }
                } else {
                    warn("Unable to follow link");
                    // http://stackoverflow.com/questions/5116473/linux-command-to-open-url-in-default-browser
                }
            }
        }
    };
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Interface">
    /**
     * Creates new form EditorFrame
     */
    public EditorFrame() {
        EditorFrame.lookAndFeel();
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        URL url = getClass().getResource("/com/timepath/tf2/hudeditor/resources/Icon.png");
        Image icon = Toolkit.getDefaultToolkit().getImage(url);
        this.setIconImage(icon);
        this.setTitle(Main.getString("Title"));

        this.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE); // Mac tweak

        boolean buggyWM = true;
        //<editor-fold defaultstate="collapsed" desc="Menu fix for window managers that don't set position on resize">
        if(OS.isLinux() && buggyWM) {
            this.addComponentListener(new ComponentAdapter() {
                private boolean moved;

                private Point real = new Point();

                private boolean updateReal = true;

                /**
                 * When maximizing windows on linux under gnome-shell and possibly others, the
                 * JMenuBar
                 * menus appear not to work. This is because the position of the
                 * window never updates. This is an attempt to make them usable again.
                 */
                @Override
                public void componentResized(ComponentEvent e) {
                    Rectangle b = EditorFrame.this.getBounds();
                    Rectangle s = EditorFrame.this.getGraphicsConfiguration().getBounds();

                    if(moved) {
                        moved = false;
                        return;
                    }

                    if(updateReal) {
                        real.x = b.x;
                        real.y = b.y;
                    }
                    updateReal = true;
                    b.x = real.x;
                    b.y = real.y;
                    if(b.x + b.width > s.width) {
                        b.x -= ((b.x + b.width) - s.width);
                        updateReal = false;
                    }
                    if(b.y + b.height > s.height) {
                        b.y = 0;
                        updateReal = false;
                    }
                    EditorFrame.this.setBounds(b);
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    Rectangle b = EditorFrame.this.getBounds();
                    moved = true;
                    real.x = b.x;
                    real.y = b.y;
                }
            });
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Drag+drop">
        this.setDropTarget(new DropTarget() {
            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    DropTargetContext context = e.getDropTargetContext();
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable t = e.getTransferable();
                    if(OS.isLinux()) {
                        DataFlavor nixFileDataFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
                        String data = (String) t.getTransferData(nixFileDataFlavor);
                        for(StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();) {
                            String token = st.nextToken().trim();
                            if(token.startsWith("#") || token.length() == 0) {
                                // comment line, by RFC 2483
                                continue;
                            }
                            try {
                                File file = new File(new URI(token));
                                load(file);
                            } catch(Exception ex) {
                            }
                        }
                    } else {
                        Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
                        if(data instanceof List) {
                            for(Iterator<?> it = ((List<?>) data).iterator(); it.hasNext();) {
                                Object o = it.next();
                                if(o instanceof File) {
                                    load((File) o);
                                }
                            }
                        }
                    }
                    context.dropComplete(true);
                } catch(ClassNotFoundException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } catch(InvalidDnDOperationException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } catch(UnsupportedFlavorException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } catch(IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } finally {
                    e.dropComplete(true);
                    repaint();
                }
            }
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Dimensions">
        GraphicsConfiguration gc = this.getGraphicsConfiguration();
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = this.getToolkit().getScreenInsets(gc);
        Dimension workspace = new Dimension(screenBounds.width - screenInsets.left - screenInsets.right, screenBounds.height - screenInsets.top - screenInsets.bottom);
        DisplayMode d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        this.setMinimumSize(new Dimension(Math.max(workspace.width / 2, 640), Math.max(3 * workspace.height / 4, 480)));
        this.setPreferredSize(new Dimension((int) (workspace.getWidth() / 1.5), (int) (workspace.getHeight() / 1.5)));

        this.setLocation((d.getWidth() / 2) - (this.getPreferredSize().width / 2), (d.getHeight() / 2) - (this.getPreferredSize().height / 2));
        this.setLocationRelativeTo(null);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Menubar">
        jmb = new EditorMenuBar();
        this.setJMenuBar(jmb);

        String str = Main.prefs.get("lastLoaded", null);
        if(str != null) {
            this.setLastLoaded(new File(str));
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Base">
        initComponents();
        tools.setWindow(this);
        tools.putClientProperty("Quaqua.ToolBar.style", "title");
        status.putClientProperty("Quaqua.ToolBar.style", "bottom");
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Tree">
        fileSystemRoot = new DefaultMutableTreeNode("root");
        fileTree = new FileTree(fileSystemRoot);
        JScrollPane fileTreePane = new JScrollPane(fileTree);
//        fileTreePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sideSplit.setTopComponent(fileTreePane);
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if(node == null) {
                    return;
                }
                propTable.clear();

                DefaultTableModel model = (DefaultTableModel) propTable.getModel();
                Object nodeInfo = node.getUserObject();
                if(nodeInfo instanceof Element) {
                    Element element = (Element) nodeInfo;
                    canvas.load(element);
                    loadProps(element);
                } else if(nodeInfo instanceof VTF) {
                    VTF v = (VTF) nodeInfo;
                    for(int i = Math.max(v.mipCount - 8, 0); i < Math.max(v.mipCount - 5, v.mipCount); i++) {
                        try {
                            ImageIcon img = new ImageIcon(v.getImage(i));
                            model.insertRow(model.getRowCount(), new Object[]{"mip[" + i + "]", img, ""});
                        } catch(IOException ex) {
                            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    model.insertRow(model.getRowCount(), new Object[]{"version", v.version, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"headerSize", v.headerSize, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"width", v.width, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"height", v.height, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"flags", v.flags, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"frameFirst", v.frameFirst, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"reflectivity", v.reflectivity, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"bumpScale", v.bumpScale, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"format", v.format, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"mipCount", v.mipCount, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"thumbFormat", v.thumbFormat, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"thumbWidth", v.thumbWidth, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"thumbHeight", v.thumbHeight, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"depth", v.depth, ""});

                } else if(nodeInfo instanceof GCF) {
                    GCF g = (GCF) nodeInfo;
                    Object[][] rows = {
                        //                                       {"headerVersion", g.header.headerVersion, g.header.getClass().getSimpleName()},
                        //                                       {"cacheType", g.header.cacheType},
                        //                                       {"formatVersion", g.header.formatVersion},
                        {"applicationID", g.header.applicationID, g.header.getClass().getSimpleName()},
                        {"applicationVersion", g.header.applicationVersion},
                        {"isMounted", g.header.isMounted},
                        //                                       {"dummy0", g.header.dummy0},
                        {"fileSize", g.header.fileSize},
                        //                                       {"clusterSize", g.header.clusterSize},
                        {"clusterCount", g.header.clusterCount},
                        {"checksum", g.header.checksum + " vs " + g.header.check()},
                        {"blockCount", g.blockAllocationTableHeader.blockCount, g.blockAllocationTableHeader.getClass().getSimpleName()},
                        {"blocksUsed", g.blockAllocationTableHeader.blocksUsed},
                        {"lastBlockUsed", g.blockAllocationTableHeader.lastBlockUsed},
                        //                                       {"dummy0", g.blockAllocationTableHeader.dummy0},
                        //                                       {"dummy1", g.blockAllocationTableHeader.dummy1},
                        //                                       {"dummy2", g.blockAllocationTableHeader.dummy2},
                        //                                       {"dummy3", g.blockAllocationTableHeader.dummy3},
                        {"checksum", g.blockAllocationTableHeader.checksum + " vs " + g.blockAllocationTableHeader.check()},
                        {"clusterCount", g.fragMap.clusterCount, g.fragMap.getClass().getSimpleName()},
                        {"firstUnusedEntry", g.fragMap.firstUnusedEntry},
                        //                                       {"isLongTerminator", g.fragMap.isLongTerminator},
                        {"checksum", g.fragMap.checksum + " vs " + g.fragMap.check()},
                        //                                       {"headerVersion", g.manifestHeader.headerVersion, g.manifestHeader.getClass().getSimpleName()},
                        //                                       {"applicationID", g.manifestHeader.applicationID},
                        //                                       {"applicationVersion", g.manifestHeader.applicationVersion},
                        {"nodeCount", g.manifestHeader.nodeCount, g.manifestHeader.getClass().getSimpleName()},
                        {"fileCount", g.manifestHeader.fileCount},
                        //                                       {"compressionBlockSize", g.manifestHeader.compressionBlockSize},
                        {"binarySize", g.manifestHeader.binarySize},
                        {"nameSize", g.manifestHeader.nameSize},
                        {"hashTableKeyCount", g.manifestHeader.hashTableKeyCount},
                        {"minimumFootprintCount", g.manifestHeader.minimumFootprintCount},
                        {"userConfigCount", g.manifestHeader.userConfigCount},
                        {"bitmask", g.manifestHeader.bitmask},
                        {"fingerprint", (((long) (g.manifestHeader.fingerprint)) & 0xFFFFFFFF)},
                        {"checksum", g.manifestHeader.checksum + " vs " + g.manifestHeader.check()},
                        //                                       {"headerVersion", g.directoryMapHeader.headerVersion, g.directoryMapHeader.getClass().getSimpleName()},
                        //                                       {"dummy0", g.directoryMapHeader.dummy0},
                        //                                       {"headerVersion", g.checksumHeader.headerVersion, g.checksumHeader.getClass().getSimpleName()},
                        {"checksumSize", g.checksumHeader.checksumSize, g.checksumHeader.getClass().getSimpleName()},
                        //                                       {"formatCode", g.checksumMapHeader.formatCode, g.checksumMapHeader.getClass().getSimpleName()},
                        //                                       {"dummy0", g.checksumMapHeader.dummy0},
                        {"itemCount", g.checksumMapHeader.itemCount, g.checksumMapHeader.getClass().getSimpleName()},
                        {"checksumCount", g.checksumMapHeader.checksumCount},
                        //                                       {"gcfRevision", g.dataBlockHeader.gcfRevision, g.dataBlockHeader.getClass().getSimpleName()},
                        {"blockCount", g.dataBlockHeader.blockCount, g.dataBlockHeader.getClass().getSimpleName()},
                        //                                       {"blockSize", g.dataBlockHeader.blockSize},
                        {"firstBlockOffset", g.dataBlockHeader.firstBlockOffset},
                        {"blocksUsed", g.dataBlockHeader.blocksUsed},
                        {"checksum", g.dataBlockHeader.checksum + " vs " + g.dataBlockHeader.check()}};
                    for(int i = 0; i < rows.length; i++) {
                        model.insertRow(model.getRowCount(), rows[i]);
                    }
                } else if(nodeInfo instanceof DirectoryEntry) {
                    DirectoryEntry d = (DirectoryEntry) nodeInfo;
                    model.insertRow(model.getRowCount(), new Object[]{"index", d.index, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"itemSize", d.itemSize, ""});
                    model.insertRow(model.getRowCount(), new Object[]{"attributes", d.attributes, ""});
                }
            }
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Table">
        propTable = new PropertyTable();
        JScrollPane propTablePane = new JScrollPane(propTable);
        sideSplit.setBottomComponent(propTablePane);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Canvas">
        canvas = new Canvas() {
            @Override
            public void placed() {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if(node == null) {
                    return;
                }

                Object nodeInfo = node.getUserObject();
                if(nodeInfo instanceof Element) {
                    Element element = (Element) nodeInfo;
                    loadProps(element);
                }
            }
        };
        JScrollPane canvasPane = new JScrollPane(canvas);
//        canvasPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        canvasPane.getVerticalScrollBar().setBlockIncrement(30);
        canvasPane.getVerticalScrollBar().setUnitIncrement(20);
//        canvasPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        canvasPane.getHorizontalScrollBar().setBlockIncrement(30);
        canvasPane.getHorizontalScrollBar().setUnitIncrement(20);
        tabbedContent.add(Main.getString("Canvas"), canvasPane);
        canvas.requestFocusInWindow();
        //</editor-fold>

    }

    private void loadProps(final Element element) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                propTable.clear();
                DefaultTableModel model = (DefaultTableModel) propTable.getModel();
                if(!element.getProps().isEmpty()) {
                    element.validateDisplay();
                    for(int i = 0; i < element.getProps().size(); i++) {
                        Property entry = element.getProps().get(i);
                        if(entry.getKey().equals("\\n")) {
                            continue;
                        }
                        model.addRow(new Object[]{entry.getKey(), entry.getValue(), entry.getInfo()});
                    }
                    model.fireTableDataChanged();
                    propTable.repaint();
                }
            }
        });
    }

    public static void restart(File file) throws URISyntaxException, IOException {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

        if(!file.getName().endsWith(".jar")) {
            return;
        }

        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(file.getPath());

        final ProcessBuilder process = new ProcessBuilder(command);
        process.start();
        System.exit(0);
    }

    /**
     * TODO: wrap this class in a launcher, rather than explicitly restarting
     */
    public static void restart() throws URISyntaxException, IOException {
        restart(new File(EditorFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
    }

    /**
     * Sets the look and feel
     */
    private static void lookAndFeel() {
        LOG.log(Level.INFO, "LaF: {0}", System.getProperty("swing.defaultlaf"));
        if(System.getProperty("swing.defaultlaf") == null) { // Do not override user specified theme
            boolean nimbus = false;
            //<editor-fold defaultstate="collapsed" desc="Attempt to apply nimbus">
            if(nimbus) {
                try {
                    for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            return;
                        }
                    }
                } catch(Exception ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
            //</editor-fold>

            boolean metal = false;
            //<editor-fold defaultstate="collapsed" desc="Fall back to metal">
            if(metal) {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    return;
                } catch(ClassNotFoundException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch(InstantiationException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch(IllegalAccessException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch(UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Fall back to native">
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(ClassNotFoundException ex) {
                Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch(InstantiationException ex) {
                Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch(IllegalAccessException ex) {
                Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch(UnsupportedLookAndFeelException ex) {
                Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            //</editor-fold>
        }

        //<editor-fold defaultstate="collapsed" desc="Improve native LaF">
        if(UIManager.getLookAndFeel().isNativeLookAndFeel()) {
            try {
                LOG.log(Level.INFO, "Adding swing enhancements for {0}", new Object[]{OS.get()});
                if(OS.isMac()) {
                    UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel"); // Apply quaqua if available
                } else if(OS.isLinux()) {
                    if(UIManager.getLookAndFeel().getClass().getName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
                        GtkFixer.installGtkPopupBugWorkaround(); // Apply clearlooks java menu fix if applicable
                        UIManager.setLookAndFeel("org.gtk.laf.extended.GTKLookAndFeelExtended"); // Apply extended gtk theme is available. http://danjared.wordpress.com/2012/05/21/mejorando-la-integracion-de-javaswing-con-gtk/
                    }
                }
                LOG.info("All swing enhancements installed");
            } catch(InstantiationException ex) {
                Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch(IllegalAccessException ex) {
                Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch(UnsupportedLookAndFeelException ex) {
                Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch(ClassNotFoundException ex) {
//                Logger.getLogger(EditorFrame.class.getName()).log(Level.INFO, null, ex);
                LOG.warning("Unable to load custom LaF");
            }
        }
        //</editor-fold>
    }

    /**
     * Google analytics tracking code
     * https://developers.google.com/analytics/resources/concepts/gaConceptsTrackingOverview
     *
     * @param state
     */
    private void track(String state) {
//        LOG.log(Level.INFO, "Tracking {0}", state);
//        if(Main.myVer == null) {
//            return;
//        }
//
//        String appID = "UA-35189411-2";
//        String title = "TF2 HUD Editor";
//        
//        com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker track = new com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker(title, "1", appID);
//        com.boxysystems.jgoogleanalytics.FocusPoint focusPoint = new com.boxysystems.jgoogleanalytics.FocusPoint(state);
//        track.trackAsynchronously(focusPoint);
//
//        com.dmurph.tracking.AnalyticsConfigData config = new com.dmurph.tracking.AnalyticsConfigData(appID);
//        com.dmurph.tracking.JGoogleAnalyticsTracker tracker = new com.dmurph.tracking.JGoogleAnalyticsTracker(config, com.dmurph.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion.V_4_7_2);
//        tracker.setEnabled(true);
//        tracker.trackPageView(state, title, "");
//        
//        EasyTracker.getInstance().activityStart(this);
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

        private JMenuItem locateUserItem;

        private JMenuItem resolutionItem;

        private JMenuItem previewItem;

        private JMenuItem updateItem;

        private JMenuItem aboutItem;

        private JMenuItem vtfItem;

        private JMenuItem captionItem;

        private JMenuItem vdfItem;

        private JMenuItem gcfItem;

        private JMenuItem bitmapItem;

        private int state = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        EditorMenuBar() {
            super();

            //<editor-fold defaultstate="collapsed" desc="File">
            JMenu fileMenu = new JMenu(Main.getString("File"));
            fileMenu.setMnemonic(KeyEvent.VK_F);
            this.add(fileMenu);

            newItem = new JMenuItem(new CustomAction(Main.getString("New"), null, KeyEvent.VK_N,
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_N, state)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
            newItem.setEnabled(false);
            fileMenu.add(newItem);

            openItem = new JMenuItem(new CustomAction("Open", null, KeyEvent.VK_O,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_O, state)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    locateHudDirectory();
                }
            });
            fileMenu.add(openItem);

            openZippedItem = new JMenuItem(new CustomAction("OpenArchive", null, KeyEvent.VK_Z,
                                                            KeyStroke.getKeyStroke(KeyEvent.VK_O, state + ActionEvent.SHIFT_MASK)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    locateZippedHud();
                }
            });
            openZippedItem.setEnabled(false);
            fileMenu.add(openZippedItem);

            fileMenu.addSeparator();

            closeItem = new JMenuItem(new CustomAction("Close", null, KeyEvent.VK_C,
                                                       KeyStroke.getKeyStroke(KeyEvent.VK_W, state)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
//                    close();
                }
            });

            if(OS.isMac()) {
                fileMenu.add(closeItem);
            }

            saveItem = new JMenuItem(new CustomAction("Save", null, KeyEvent.VK_S,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_S, state)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    if(canvas.getElements().size() > 0) {
                        info(canvas.getElements().get(canvas.getElements().size() - 1).save());
                    }
                }
            });
            saveItem.setEnabled(false);
            fileMenu.add(saveItem);

            saveAsItem = new JMenuItem(new CustomAction("Save As...", null, KeyEvent.VK_A,
                                                        KeyStroke.getKeyStroke(KeyEvent.VK_S, state + ActionEvent.SHIFT_MASK)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                }
            });
            saveAsItem.setEnabled(false);
            fileMenu.add(saveAsItem);

            reloadItem = new JMenuItem(new CustomAction("Revert", null, KeyEvent.VK_R,
                                                        KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    new Thread() {
                        @Override
                        public void run() {
                            load(lastLoaded);
                        }
                    }.start();
                }
            });
            reloadItem.setEnabled(false);
            fileMenu.add(reloadItem);

            if(!OS.isMac()) {
                fileMenu.addSeparator();
                fileMenu.add(closeItem);

                exitItem = new JMenuItem(new CustomAction("Exit", null, KeyEvent.VK_X,
                                                          KeyStroke.getKeyStroke(KeyEvent.VK_Q, state)) {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        quit();
                    }
                });
                fileMenu.add(exitItem);
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Edit">
            JMenu editMenu = new JMenu("Edit");
            editMenu.setMnemonic(KeyEvent.VK_E);
            this.add(editMenu);

            undoItem = new JMenuItem(new CustomAction("Undo", null, KeyEvent.VK_U,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_Z, state)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                }
            });
            undoItem.setEnabled(false);
            editMenu.add(undoItem);

            redoItem = new JMenuItem(new CustomAction("Redo", null, KeyEvent.VK_R,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_Y, state)) { // TODO: ctrl + shift + z
                @Override
                public void actionPerformed(ActionEvent ae) {
                }
            });
            redoItem.setEnabled(false);
            editMenu.add(redoItem);

            editMenu.addSeparator();

            cutItem = new JMenuItem(new CustomAction("Cut", null, KeyEvent.VK_T,
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_X, state)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                }
            });
            cutItem.setEnabled(false);
            editMenu.add(cutItem);

            copyItem = new JMenuItem(new CustomAction("Copy", null, KeyEvent.VK_C,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_C, state)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                }
            });
            copyItem.setEnabled(false);
            editMenu.add(copyItem);

            pasteItem = new JMenuItem(new CustomAction("Paste", null, KeyEvent.VK_P,
                                                       KeyStroke.getKeyStroke(KeyEvent.VK_V, state)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                }
            });
            pasteItem.setEnabled(false);
            editMenu.add(pasteItem);

            deleteItem = new JMenuItem(new CustomAction("Delete", null, KeyEvent.VK_D,
                                                        KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    canvas.removeElements(canvas.getSelected());
                }
            });
            editMenu.add(deleteItem);
            editMenu.addSeparator();

            selectAllItem = new JMenuItem(new CustomAction("Select All", null, KeyEvent.VK_A,
                                                           KeyStroke.getKeyStroke(KeyEvent.VK_A, state)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    for(int i = 0; i < canvas.getElements().size(); i++) {
                        canvas.select(canvas.getElements().get(i));
                    }
                }
            });
            editMenu.add(selectAllItem);

            editMenu.addSeparator();

            if(!OS.isMac()) {
                preferencesItem = new JMenuItem(new CustomAction("Preferences", null, KeyEvent.VK_E, null) {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        preferences();
                    }
                });
                editMenu.add(preferencesItem);
            }

            locateUserItem = new JMenuItem(new CustomAction("Select user folder", null, KeyEvent.VK_S, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    locateUserDirectory();
                }
            });
            editMenu.add(locateUserItem);
            //</editor-fold>

            JMenu viewMenu = new JMenu("View");
            viewMenu.setMnemonic(KeyEvent.VK_V);
            this.add(viewMenu);

            resolutionItem = new JMenuItem(new CustomAction("Change Resolution", null, KeyEvent.VK_R,
                                                            KeyStroke.getKeyStroke(KeyEvent.VK_R, state)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    changeResolution();
                }
            });
            resolutionItem.setEnabled(false);
            viewMenu.add(resolutionItem);

            previewItem = new JMenuItem(new CustomAction("Full Screen Preview", null, KeyEvent.VK_F,
                                                         KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0)) {
                private boolean fullscreen;

                @Override
                public void actionPerformed(ActionEvent ae) {
                    EditorFrame.this.dispose();
                    EditorFrame.this.setUndecorated(!fullscreen);
                    EditorFrame.this.setExtendedState(fullscreen ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH);
                    EditorFrame.this.setVisible(true);
                    EditorFrame.this.setJMenuBar(jmb);
                    EditorFrame.this.pack();
                    EditorFrame.this.toFront();
                    fullscreen = !fullscreen;
                }
            });
            viewMenu.add(previewItem);

            viewMenu.addSeparator();

            //<editor-fold defaultstate="collapsed" desc="Views">
            JMenuItem viewItem1 = new JMenuItem("Main Menu");

            viewItem1.setEnabled(false);

            viewMenu.add(viewItem1);

            JMenuItem viewItem2 = new JMenuItem("In-game (Health and ammo)");

            viewItem2.setEnabled(false);

            viewMenu.add(viewItem2);

            JMenuItem viewItem3 = new JMenuItem("Scoreboard");

            viewItem3.setEnabled(false);

            viewMenu.add(viewItem3);

            JMenuItem viewItem4 = new JMenuItem("CTF HUD");

            viewItem4.setEnabled(false);

            viewMenu.add(viewItem4);
            //</editor-fold>

            extras();

            JMenu helpMenu = new JMenu("Help");

            helpMenu.setMnemonic(KeyEvent.VK_H);

            this.add(helpMenu);

            updateItem = new JMenuItem(new CustomAction("Updates", null, KeyEvent.VK_U, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    EditorFrame.this.checkForUpdates(true);
                }
            });
            updateItem.setEnabled(Main.myVer != null); // XXX
            updateItem.setEnabled(true);
            helpMenu.add(updateItem);

            if(OS.isMac()) {
                aboutItem = new JMenuItem(new CustomAction("About", null, KeyEvent.VK_A, null) {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        about();
                    }
                });
                helpMenu.add(aboutItem);
            }
        }

        private void extras() {
            JMenu extrasMenu = new JMenu("Extras");
            extrasMenu.setMnemonic(KeyEvent.VK_X);
            this.add(extrasMenu);

            vtfItem = new JMenuItem(new CustomAction("VTF Viewer", null, KeyEvent.VK_T, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    VTFTest.main("");
                }
            });
            extrasMenu.add(vtfItem);

            captionItem = new JMenuItem(new CustomAction("Caption Editor", null, KeyEvent.VK_C, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    VCCDTest.main("");
                }
            });
            extrasMenu.add(captionItem);

            vdfItem = new JMenuItem(new CustomAction("VDF Viewer", null, KeyEvent.VK_D, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    DataTest.main("");
                }
            });
            extrasMenu.add(vdfItem);

            gcfItem = new JMenuItem(new CustomAction("GCF Viewer", null, KeyEvent.VK_G, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ArchiveTest.main("");
                }
            });
            extrasMenu.add(gcfItem);

            bitmapItem = new JMenuItem(new CustomAction("Bitmap Font Glyph Editor", null, KeyEvent.VK_C, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    VBFTest.main("");
                }
            });
            extrasMenu.add(bitmapItem);

            JMenuItem i = new JMenuItem(new CustomAction("File chooser test", null, KeyEvent.VK_F, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    FileChooserTest.main("");
                }
            });
            extrasMenu.add(i);
        }
    }

    private class CustomAction extends AbstractAction {

        private CustomAction(String string, Icon icon, int mnemonic, KeyStroke shortcut) {
            super(Main.getString(string), icon);
            this.putValue(Action.MNEMONIC_KEY, mnemonic);
            this.putValue(Action.ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent ae) {
        }
    }

//<editor-fold defaultstate="collapsed" desc="Generated Code">
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tools = new com.timepath.swing.BlendedToolBar();
        rootSplit = new javax.swing.JSplitPane();
        sideSplit = new javax.swing.JSplitPane();
        tabbedContent = new javax.swing.JTabbedPane();
        status = new com.timepath.swing.StatusBar();

        getContentPane().add(tools, java.awt.BorderLayout.PAGE_START);

        rootSplit.setDividerLocation(180);
        rootSplit.setContinuousLayout(true);
        rootSplit.setOneTouchExpandable(true);

        sideSplit.setBorder(null);
        sideSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        sideSplit.setResizeWeight(0.5);
        sideSplit.setContinuousLayout(true);
        sideSplit.setOneTouchExpandable(true);
        rootSplit.setLeftComponent(sideSplit);
        rootSplit.setRightComponent(tabbedContent);

        getContentPane().add(rootSplit, java.awt.BorderLayout.CENTER);
        getContentPane().add(status, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane rootSplit;
    private javax.swing.JSplitPane sideSplit;
    private com.timepath.swing.StatusBar status;
    private javax.swing.JTabbedPane tabbedContent;
    private com.timepath.swing.BlendedToolBar tools;
    // End of variables declaration//GEN-END:variables
    //</editor-fold>
    //</editor-fold>
}
