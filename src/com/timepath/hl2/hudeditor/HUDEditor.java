package com.timepath.hl2.hudeditor;

import apple.OSXAdapter;
import com.timepath.DateUtils;
import com.timepath.Utils;
import com.timepath.backports.javax.swing.SwingWorker;
import com.timepath.hl2.io.RES;
import com.timepath.hl2.io.VMT;
import com.timepath.hl2.io.VTF;
import com.timepath.hl2.io.swing.VGUICanvas;
import com.timepath.hl2.io.test.DataTest;
import com.timepath.hl2.io.test.VBFTest;
import com.timepath.hl2.io.test.VCCDTest;
import com.timepath.hl2.io.test.VTFTest;
import com.timepath.hl2.io.util.Element;
import com.timepath.steam.io.util.Property;
import com.timepath.plaf.IconList;
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
import com.timepath.plaf.x.filechooser.NativeFileChooser;
import com.timepath.steam.SteamID;
import com.timepath.steam.SteamUtils;
import com.timepath.steam.io.ArchiveExplorer;
import com.timepath.steam.io.storage.GCF;
import com.timepath.steam.io.storage.GCF.GCFDirectoryEntry;
import com.timepath.steam.io.VDF;
import com.timepath.steam.io.util.VDFNode;
import com.timepath.swing.TreeUtils;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
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
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
public class HUDEditor extends javax.swing.JFrame {

    private static final Logger LOG = Logger.getLogger(HUDEditor.class.getName());

    private final EditorMenuBar jmb;

    private final DefaultMutableTreeNode fileSystemRoot, archiveRoot;

    private final ProjectTree fileTree;

    private final PropertyTable propTable;

    private static VGUICanvas canvas;

    private File lastLoaded;

    private JSpinner spinnerWidth;

    private JSpinner spinnerHeight;

    private HyperlinkListener linkListener = Utils.getLinkListener();

    //<editor-fold defaultstate="collapsed" desc="Updates">
    private BufferedReader getPage(String s) throws IOException {
        URL u = new URL(s);
        URLConnection c = u.openConnection();
//        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        LOG.log(Level.INFO, "{0} size: {1}", new Object[] {s, c.getContentLength()});
        InputStream is = c.getInputStream();
        return new BufferedReader(new InputStreamReader(is));
    }

    private String currentVersion() throws IOException {
        BufferedReader r = getPage(
                "https://dl.dropbox.com/u/42745598/tf/HUD%20Editor/TF2%20HUD%20Editor.jar.current");
        String l = r.readLine();
        r.close();
        return l;
    }

    private String checksum() throws IOException {
        BufferedReader r = getPage(
                "https://dl.dropbox.com/u/42745598/tf/HUD%20Editor/TF2%20HUD%20Editor.jar.MD5");
        String l = r.readLine();
        r.close();
        return l;
    }

    private static boolean checked;

    private long lastUpdate;

    private void checkForUpdates(final boolean force) {
        final JEditorPane pane = new JEditorPane("text/html", "");
        Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        pane.setPreferredSize(new Dimension(s.width / 4, s.height / 2));
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setBackground(new Color(0, 0, 0, 0));
        pane.addHyperlinkListener(HUDEditor.this.linkListener);
        final JScrollPane scroll = new JScrollPane(pane);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        final JLabel lastUpdated = new JLabel("\n");

        final Object[] components = {scroll, lastUpdated};
        final JButton[] options = {new JButton("Close"), new JButton("Update")};
        options[1].setEnabled(true);

        final JOptionPane optionPane = new JOptionPane(components,
                                                       JOptionPane.INFORMATION_MESSAGE,
                                                       JOptionPane.YES_NO_OPTION,
                                                       null,
                                                       options,
                                                       options[0]);

        final JDialog d = new JDialog(HUDEditor.this, "Updates", false);
        d.setContentPane(optionPane);
        d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                d.setVisible(!options[0].isEnabled());
            }
        });

        options[0].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                d.setVisible(false);
            }
        });

        if(force) {
            d.pack();
            d.setVisible(true);
        }

        new SwingWorker<Boolean, Void>() {
            private int retries = 3;

            private boolean checkUpdates() {
                boolean updateAvailable = false;
                try {
                    String current = currentVersion();
                    lastUpdate = Long.parseLong(current);
                    updateAvailable = lastUpdate > Main.myVer;
                    String sign = "==";
                    if(Main.myVer > lastUpdate) {
                        sign = ">";
                    } else if(Main.myVer < lastUpdate) {
                        sign = "<";
                    }
                    LOG.log(Level.INFO, "{0} {2} {1}", new Object[] {Main.myVer, lastUpdate, sign});
                } catch(IOException ex) {
                    retries--;
                    if(retries > 0) {
                        updateAvailable = checkUpdates();
                    } else {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
                return updateAvailable;
            }

            @Override
            protected Boolean doInBackground() throws Exception {
                return checkUpdates();
            }

            @Override
            protected void done() {
                try {
                    boolean updateAvailable = get();
                    if(updateAvailable) {
                        options[1].setEnabled(true);
                        if(!force) {
                            d.pack();
                            d.setVisible(true);
                        }
                    }
                } catch(InterruptedException ex) {
                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch(ExecutionException ex) {
                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.execute();

        final JProgressBar changelogBar = new JProgressBar(0, 100);
        changelogBar.setPreferredSize(new Dimension(175, 20));
        changelogBar.setMinimumSize(new Dimension(175, 20));
        changelogBar.setStringPainted(true);
        changelogBar.setString("Fetching changelog");
        changelogBar.setIndeterminate(true);
        scroll.getParent().add(changelogBar);

        new SwingWorker<String, Void>() {
            private int retries = 3;

            private String fetchChangelog() {
                String changelog = "Unable to fetch changelog";
                try {
                    BufferedReader r = getPage(
                            "https://dl.dropbox.com/u/42745598/tf/HUD%20Editor/TF2%20HUD%20Editor.jar.changes");
                    String text = "";
                    String grep = null;
                    if(Main.myVer != 0) {
                        grep = "" + Main.myVer;
                    }
                    String line;
                    while((line = r.readLine()) != null) {
                        if(grep != null && line.contains(grep)) {
                            text += line.replace(grep, "<b><u>" + grep + "</u></b>");
                        } else {
                            text += line;
                        }
                    }
                    r.close();
                    changelog = text;
                } catch(IOException ex) {
                    retries--;
                    if(retries > 0) {
                        changelog = fetchChangelog();
                    } else {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
                return changelog;
            }

            @Override
            protected String doInBackground() throws Exception {
                return fetchChangelog();
            }

            @Override
            protected void done() {
                try {
                    Container c = changelogBar.getParent();
                    c.remove(changelogBar);
                    c.repaint();
                    pane.setText(get());
                    pane.setCaretPosition(0);
                    new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            lastUpdated.setText("Last updated " + DateUtils.timePeriod(
                                    (System.currentTimeMillis() / 1000) - lastUpdate) + " ago");
                        }
                    }).start();
                } catch(InterruptedException ex) {
                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch(ExecutionException ex) {
                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.execute();

        final JProgressBar updateBar = new JProgressBar();
        options[1].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                //<editor-fold defaultstate="collapsed" desc="Update">
                options[1].setEnabled(false);
                updateBar.setPreferredSize(new Dimension(175, 20));
                updateBar.setMinimumSize(new Dimension(175, 20));
                updateBar.setStringPainted(true);
                updateBar.setIndeterminate(true);
                updateBar.setValue(0);
                scroll.getParent().add(updateBar);
                scroll.getParent().repaint();
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        String md5 = checksum();
                        final File md5File = new File(Utils.workingDirectory(HUDEditor.class),
                                                      "update.tmp.MD5");
                        if(md5File.exists()) {
                            md5File.delete();
                        }
                        md5File.createNewFile();
                        FileOutputStream md5Writer = new FileOutputStream(md5File);
                        md5Writer.write(md5.getBytes());
                        final File downloaded = new File(Utils.workingDirectory(HUDEditor.class),
                                                         "update.tmp");
                        int retries = 3;
                        for(int attempt = 1; attempt < retries; attempt++) {
                            LOG.log(Level.INFO, "Checking for {0}", downloaded);
                            if(!downloaded.exists()) {
                                long startTime = System.currentTimeMillis();

                                LOG.info("Connecting to Dropbox...");

                                URL latest = new URL(
                                        "https://dl.dropbox.com/u/42745598/tf/HUD%20Editor/TF2%20HUD%20Editor.jar");
                                URLConnection editor = latest.openConnection();
                                updateBar.setMaximum(editor.getContentLength());
                                updateBar.setIndeterminate(false);

                                InputStream in = latest.openStream();

                                LOG.info("Downloading JAR file in 150KB blocks at a time.\n");
                                FileOutputStream writer = new FileOutputStream(downloaded);
                                byte[] buffer = new byte[153600]; // 150KB
                                int totalBytesRead = 0;
                                int bytesRead;
                                while((bytesRead = in.read(buffer)) > 0) {
                                    writer.write(buffer, 0, bytesRead);
                                    buffer = new byte[153600];
                                    totalBytesRead += bytesRead;
                                    updateBar.setValue(totalBytesRead);
                                }

                                long endTime = System.currentTimeMillis();

                                LOG.log(Level.INFO,
                                        "Done. {0} kilobytes downloaded ({1} seconds).\n",
                                        new Object[] {new Integer(totalBytesRead / 1000).toString(),
                                                      new Long((endTime - startTime) / 1000).toString()});
                                writer.close();
                                in.close();
                                status.remove(updateBar);
                            } else {
                                LOG.info("Exists");
                            }

                            LOG.info("Checking MD5...");
                            if(!Utils.takeMD5(Utils.loadFile(downloaded)).equalsIgnoreCase(md5)) {
                                LOG.warning("Corrupt or old download");
                                continue;
                            }
                            LOG.info("MD5 matches");

                            info("Restart to apply update to " + Utils.currentFile(Main.class));
                            return true;
                        }
                        LOG.warning("Update failed");
                        return false;
                    }

                    @Override
                    protected void done() {
                        try {
                            boolean success = get();
                            if(!success) {
                                updateBar.setString("Update failed");
                                updateBar.setIndeterminate(false);
                                updateBar.setValue(0);
                                options[1].setEnabled(true);
                            } else {
                                updateBar.setString("Downloaded successfully");
                            }
                        } catch(InterruptedException ex) {
                            Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch(ExecutionException ex) {
                            Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }.execute();
                //</editor-fold>
            }
        });
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Messages">
    private void error(Object msg) {
        error(msg, Main.getString("Error"));
    }

    private void error(Object msg, String title) {
        LOG.log(Level.SEVERE, "{0}:{1}", new Object[] {title, msg});
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private void warn(Object msg) {
        error(msg, Main.getString("Warning"));
    }

    private void warn(Object msg, String title) {
        LOG.log(Level.WARNING, "{0}:{1}", new Object[] {title, msg});
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    private void info(Object msg) {
        info(msg, Main.getString("Info"));
    }

    private void info(Object msg, String title) {
        LOG.log(Level.INFO, "{0}:{1}", new Object[] {title, msg});
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Dialogs">
    public void preferences() {
        jDialog1.setVisible(true);
    }

    public void about() {
        final String latestThread = "http://steamcommunity.com/groups/hudeditor/discussions";
        final JEditorPane pane = new JEditorPane("text/html", "");
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setBackground(new Color(0, 0, 0, 0));
        pane.addHyperlinkListener(HUDEditor.this.linkListener);
        String aboutText = "<html><h2>This is to be a What You See Is What You Get HUD Editor for TF2,</h2>";
        aboutText += "for graphically editing TF2 HUDs!";
        aboutText += "<p>Author: TimePath (<a href=\"http://www.reddit.com/user/TimePath/\">reddit</a>|<a href=\"http://steamcommunity.com/id/TimePath/\">steam</a>)<br>";
        String local = "</p>";
        String aboutText2 = "<p>Source available on <a href=\"https://github.com/TimePath/hl2-hud-editor\">GitHub</a>";
        aboutText2 += "<br><a href=\"https://github.com/TimePath/hl2-hud-editor/commits/master.atom\">Atom feed</a> available for source commits</p>";
        aboutText2 += "<p>Please leave feedback or suggestions on <a href=\"" + latestThread + "\">the steam group forum</a>";
        aboutText2 += "<br>You might be able to catch me <a href=\"steam://friends/joinchat/103582791433759131\">here</a> (<a href=\"http://steamcommunity.com/groups/hudeditor\">web link</a>)</p>"; // TODO: http://steamredirect.heroku.com or Runtime.exec() on older versions of java
        aboutText2 += "<p>Logging to <a href=\"" + Main.logFile.toURI() + "\">" + Main.logFile + "</a></p>";
        if(Main.myVer != 0) {
            long time = Main.myVer;
            aboutText2 += "<p>Build date: " + DateUtils.parse(time) + " (" + time + ")</p>";
        }
        aboutText2 += "<br></html>";
        final String p1 = aboutText;
        final String p2 = aboutText2;
        pane.setText(p1 + local + p2);
        Timer t = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Calendar devCal = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
                String local = "My (presumed) local time: " + DateUtils.parse(
                        devCal.getTimeInMillis() / 1000) + "</p>";
                pane.setText(p1 + local + p2);
            }
        });
        t.setInitialDelay(0);
        t.start();
        info(pane, "About");

    }

    private void locateUserDirectory() {
        FilenameFilter dirFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        };

        JComboBox dropDown = new JComboBox(); // <String>
        File steamappsFolder = SteamUtils.getSteamApps();
        File[] userFolders = steamappsFolder.listFiles(dirFilter);
        if(userFolders == null) {
            error("SteamApps is empty!", "Empty SteamApps directory");
            return;
        }
        for(int i = 0; i < userFolders.length; i++) {
            if(userFolders[i].getName().equalsIgnoreCase("common") || userFolders[i].getName().equalsIgnoreCase(
                    "sourcemods")) {
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
            JOptionPane.showMessageDialog(this, dialogPanel, "Select user",
                                          JOptionPane.QUESTION_MESSAGE);
        }
        File installDir = new File(steamappsFolder,
                                   dropDown.getSelectedItem() + "/Team Fortress 2/tf");
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
        try {
            final File[] selection = new NativeFileChooser().setParent(HUDEditor.this).setTitle(
                    Main.getString("LoadHudDir")).setFile(lastLoaded).setFileMode(
                    BaseFileChooser.FileMode.DIRECTORIES_ONLY).choose();
            if(selection == null) {
                return;
            }
            load(selection[0]);
        } catch(IOException ex) {
            Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void load(final File f) {
        HUDEditor.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<DefaultMutableTreeNode, Void>() {
            @Override
            public DefaultMutableTreeNode doInBackground() {
                return doLoad(f);
            }

            @Override
            protected void done() {
                try {
                    DefaultMutableTreeNode project = get();
                    HUDEditor.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    if(project != null) {
                        fileSystemRoot.add(project);
                        fileTree.expandPath(new TreePath(project.getPath()));
                        fileTree.setSelectionRow(fileSystemRoot.getIndex(project));
                        fileTree.requestFocusInWindow();
                    }
                } catch(InterruptedException ex) {
                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch(ExecutionException ex) {
                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.execute();

    }

    private void locateZippedHud() {
    }

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

        Object[] message = {"Presets: ", dropDown, "Width: ", spinnerWidth, "Height: ",
                            spinnerHeight};

        final JOptionPane optionPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE,
                                                       JOptionPane.OK_CANCEL_OPTION, null, null);
        final JDialog dialog = optionPane.createDialog(this, "Change resolution...");
        dialog.setContentPane(optionPane);
        dialog.pack();
        dialog.setVisible(true);
        if(optionPane.getValue() != null) {
            int value = ((Integer) optionPane.getValue()).intValue();
            if(value == JOptionPane.YES_OPTION) {
                canvas.setPreferredSize(new Dimension(Integer.parseInt(
                        spinnerWidth.getValue().toString()), Integer.parseInt(
                        spinnerHeight.getValue().toString())));
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
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about",
                                                                              (Class[]) null));
                OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences",
                                                                                    (Class[]) null));
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
                URL url = getClass().getResource("/com/timepath/hl2/hudeditor/res/Icon.png");
                Image icon = Toolkit.getDefaultToolkit().getImage(url);
                app.setDockIconImage(icon);
                //</editor-fold>
            } catch(Exception e) {
                LOG.severe(e.toString());
            }
        } else if(OS.isLinux()) {
            if(!Ayatana.installMenu((JFrame) this, menubar)) {
                LOG.log(Level.WARNING, "AyatanaDesktop failed to load for {0}", System.getenv(
                        "XDG_CURRENT_DESKTOP"));
            }
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        this.createBufferStrategy(2);
        track("ProgramLoad");
        if(Main.myVer != 0 && Main.prefs.getBoolean("autoupdate", true) && !checked) {
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
    private DefaultMutableTreeNode doLoad(final File root) {
        if(root == null) {
            return null;
        }
        if(!root.exists()) {
            error(new MessageFormat(Main.getString("FileAccessError")).format(new Object[] {root}));
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
            return null;
        }

        if(root.isDirectory()) {
            File[] folders = root.listFiles();
            boolean valid = true; // TODO: find resource and scripts if there is a parent directory
            for(int i = 0; i < folders.length; i++) {
                if(folders[i].isDirectory() && ("resource".equalsIgnoreCase(folders[i].getName()) || "scripts".equalsIgnoreCase(
                                                folders[i].getName()))) {
                    valid = true;
                    break;
                }
            }
            if(!valid) {
                error("Selection not valid. Please choose a folder containing \'resources\' or \'scripts\'.");
                locateHudDirectory();
                return null;
            }

//            new SwingWorker<DefaultMutableTreeNode, Void>() {

            long start = System.currentTimeMillis();

//                @Override
//                protected DefaultMutableTreeNode doInBackground() throws Exception {
            final DefaultMutableTreeNode project = new DefaultMutableTreeNode();
            project.setUserObject(root.getName());
            recurseDirectoryToNode(root, project);
//                    return project;
//                }

//                @Override
//                protected void done() {
            LOG.log(Level.INFO, "Loaded hud - took {0}ms", (System.currentTimeMillis() - start));
//                }

//            }.execute();

            return project;
        }
        return null;
    }

    private void mount() {
        File r = SteamUtils.getSteamApps();
        if(r == null) {
            return;
        }
        File[] gcf = r.listFiles(new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".gcf");
            }
        });
        if(gcf == null) {
            return;
        }
        for(final File f : gcf) {
            LOG.log(Level.INFO, "Mounting {0}", f);
            new SwingWorker<DefaultMutableTreeNode, Void>() {
                @Override
                protected DefaultMutableTreeNode doInBackground() throws Exception {
                    DefaultMutableTreeNode child = null;
                    try {
                        GCF g = new GCF(f);
                        child = new DefaultMutableTreeNode();
                        g.analyze(child);
                        child.setUserObject(g);

                    } catch(IOException ex) {
                        Logger.getLogger(SteamUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return child;
                }

                @Override
                protected void done() {
                    try {
                        DefaultMutableTreeNode g = get();
                        if(g != null) {
                            archiveRoot.add(g);
                            ((DefaultTreeModel) fileTree.getModel()).reload(archiveRoot);
                            LOG.log(Level.INFO, "Mounted {0}", f);
                        }
                    } catch(InterruptedException ex) {
                        Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch(ExecutionException ex) {
                        Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }.execute();
        }
    }

    private void recurseDirectoryToNode(File root, final DefaultMutableTreeNode parent) {

        FilenameFilter ff = new FilenameFilter() {
            final String[] blacklist = {".mp3", ".exe", ".sh", ".dll", ".dylib", ".so",
                                        ".ttf", ".bik", ".mov", ".cfg", ".cache", ".manifest",
                                        ".frag", ".vert", ".tga", ".png", ".html", ".wav",
                                        ".ico", ".uifont", ".xml", ".css", ".dic", ".conf",
                                        ".pak", ".py", ".flt", ".mix", ".asi", ".checksum",
                                        ".xz", ".log", ".doc", ".webm", ".jpg", ".psd", ".avi",
                                        ".zip", ".bin", ".vpk", ".bsp", ".txt", ".inf", ".bmp",
                                        ".icns"};

            public boolean accept(File dir, String name) {
                for(int j = 0; j < blacklist.length; j++) {
                    if(name.endsWith(blacklist[j])) {
                        return false;
                    }
                }
                return true;
            }
        };
        final File[] fileList = root.listFiles(ff);
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
                if(f.getName().toLowerCase().equals("common")
                   || f.getName().toLowerCase().equals("downloading")
                   || f.getName().toLowerCase().equals("temp")
                   || f.getName().toLowerCase().equals("sourcemods")) {
                    continue;
                }
                recurseDirectoryToNode(f, child);
                if(child.getChildCount() > 0) {
                    parent.add(child);
                }
            } else {
                parent.add(child);
                threads[i] = new Thread(new Runnable() {
                    public void run() {
                        LOG.log(Level.FINE, "Loading {0}...", f);
//                        if(f.getName().endsWith(".txt")
                        if(f.getName().endsWith(".vdf")
                           //                           || f.getName().endsWith(".vdf")
                           || f.getName().endsWith(".pop")
                           || f.getName().endsWith(".layout")
                           || f.getName().endsWith(".menu")
                           || f.getName().endsWith(".styles")) {
                            VDF v = new VDF();
                            try {
                                v.readExternal(new FileInputStream(f));
                            } catch(FileNotFoundException ex) {
                                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null,
                                                                                ex);
                            }
                            TreeUtils.moveChildren(v.getRoot(), child);
                        } else if(f.getName().endsWith(".res")) {
                            RES v = new RES();
                            try {
                                v.readExternal(new FileInputStream(f));
                            } catch(FileNotFoundException ex) {
                                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null,
                                                                                ex);
                            }
                            TreeUtils.moveChildren(v.getRoot(), child);
                        } else if(f.getName().endsWith(".vmt")) {
                            VMT v = new VMT();
                            try {
                                v.readExternal(new FileInputStream(f));
                            } catch(FileNotFoundException ex) {
                                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null,
                                                                                ex);
                            }
                            TreeUtils.moveChildren(v.getRoot(), child);
                        } else if(f.getName().endsWith(".vtf")) {
                            VTF v = null;
                            try {
                                v = VTF.load(new FileInputStream(f));
                            } catch(IOException ex) {
                                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null,
                                                                                ex);
                            }
                            if(v != null) {
                                child.setUserObject(v);
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
//        if(!updating) {
        LOG.info("Closing...");
        this.dispose();
//        if(OS.isMac()) {
//                JFrame f = new JFrame();
//                f.setUndecorated(true);
//                f.setJMenuBar(this.getJMenuBar());
//                f.setLocation(-Integer.MAX_VALUE, -Integer.MAX_VALUE); // Hacky - should just use the OSX Application calls...
//                f.setVisible(true);
//        } else {
        System.exit(0);
//        }
//        }
    }

    private void setLastLoaded(File root) {
        jmb.reloadItem.setEnabled(root != null);
        if(root == null || !root.exists()) {
            return;
        }
        lastLoaded = root;
        Main.prefs.put("lastLoaded", root.getPath());
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Interface">
    public HUDEditor() {
        HUDEditor.lookAndFeel();
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        HUDEditor.this.setIconImages(new IconList("/com/timepath/hl2/hudeditor/res/Icon", "png",
                                                  new int[] {16, 22, 24, 32, 40, 48, 64, 128, 512,
                                                             1024}).getIcons());

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
                    Rectangle b = HUDEditor.this.getBounds();
                    Rectangle s = HUDEditor.this.getGraphicsConfiguration().getBounds();

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
                    HUDEditor.this.setBounds(b);
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    Rectangle b = HUDEditor.this.getBounds();
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
                    File file = null;
                    if(OS.isLinux()) {
                        DataFlavor nixFileDataFlavor = new DataFlavor(
                                "text/uri-list;class=java.lang.String");
                        String data = (String) t.getTransferData(nixFileDataFlavor);
                        for(StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();) {
                            String token = st.nextToken().trim();
                            if(token.startsWith("#") || token.length() == 0) {
                                // comment line, by RFC 2483
                                continue;
                            }
                            try {
                                file = new File(new URI(token));
                            } catch(Exception ex) {
                            }
                        }
                    } else {
                        Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
                        if(data instanceof List) {
                            for(Iterator<?> it = ((List<?>) data).iterator(); it.hasNext();) {
                                Object o = it.next();
                                if(o instanceof File) {
                                    file = (File) o;
                                }
                            }
                        }
                    }
                    if(file != null) {
                        load(file);
                    }
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
        Dimension workspace = new Dimension(
                screenBounds.width - screenInsets.left - screenInsets.right,
                screenBounds.height - screenInsets.top - screenInsets.bottom);
        DisplayMode d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        this.setMinimumSize(new Dimension(Math.max(workspace.width / 2, 640), Math.max(
                3 * workspace.height / 4, 480)));
        this.setPreferredSize(new Dimension((int) (workspace.getWidth() / 1.5),
                                            (int) (workspace.getHeight() / 1.5)));

//        this.setLocation((d.getWidth() / 2) - (this.getSize().width / 2), (d.getHeight() / 2) - (this.getSize().height / 2));
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

        jCheckBox1.setSelected(Main.prefs.getBoolean("autoupdate", true));

        tools.setWindow(this);
        tools.putClientProperty("Quaqua.ToolBar.style", "title");
        status.putClientProperty("Quaqua.ToolBar.style", "bottom");
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Tree">
        archiveRoot = new DefaultMutableTreeNode("Archives");
        fileSystemRoot = new DefaultMutableTreeNode("Projects");
        fileTree = new ProjectTree();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        ((DefaultTreeModel) fileTree.getModel()).setRoot(root);
        root.add(archiveRoot);
        root.add(fileSystemRoot);
        ((DefaultTreeModel) fileTree.getModel()).reload();

        JScrollPane fileTreePane = new JScrollPane(fileTree);
        fileTreePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
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
                if(node instanceof VDFNode) {
                    Element element = Element.importVdf((VDFNode) node);
                    loadProps(element);
                    try {
                        canvas.load(element);
                    } catch(NullPointerException ex) {
                        ex.printStackTrace();
                    }
                } else if(nodeInfo instanceof VTF) {
                    VTF v = (VTF) nodeInfo;
                    for(int i = Math.max(v.mipCount - 8, 0); i < Math.max(v.mipCount - 5, v.mipCount); i++) {
                        try {
                            ImageIcon img = new ImageIcon(v.getImage(i));
                            model.insertRow(model.getRowCount(),
                                            new Object[] {"mip[" + i + "]", img, ""});
                        } catch(IOException ex) {
                            Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    model.insertRow(model.getRowCount(), new Object[] {"version", v.version, ""});
                    model.insertRow(model.getRowCount(), new Object[] {"headerSize", v.headerSize,
                                                                       ""});
                    model.insertRow(model.getRowCount(), new Object[] {"width", v.width, ""});
                    model.insertRow(model.getRowCount(), new Object[] {"height", v.height, ""});
                    model.insertRow(model.getRowCount(), new Object[] {"flags", v.flags, ""});
                    model.insertRow(model.getRowCount(), new Object[] {"frameFirst", v.frameFirst,
                                                                       ""});
                    model.insertRow(model.getRowCount(), new Object[] {"reflectivity",
                                                                       v.reflectivity, ""});
                    model.insertRow(model.getRowCount(), new Object[] {"bumpScale", v.bumpScale, ""});
                    model.insertRow(model.getRowCount(), new Object[] {"format", v.format, ""});
                    model.insertRow(model.getRowCount(), new Object[] {"mipCount", v.mipCount, ""});
                    model.insertRow(model.getRowCount(), new Object[] {"thumbFormat", v.thumbFormat,
                                                                       ""});
                    model.insertRow(model.getRowCount(), new Object[] {"thumbWidth", v.thumbWidth,
                                                                       ""});
                    model.insertRow(model.getRowCount(), new Object[] {"thumbHeight", v.thumbHeight,
                                                                       ""});
                    model.insertRow(model.getRowCount(), new Object[] {"depth", v.depth, ""});

                } else if(nodeInfo instanceof GCF) {
                    GCF g = (GCF) nodeInfo;
                    Object[][] rows = {
                        //                                       {"headerVersion", g.header.headerVersion, g.header.getClass().getSimpleName()},
                        //                                       {"cacheType", g.header.cacheType},
                        //                                       {"formatVersion", g.header.formatVersion},
                        {"applicationID", g.header.applicationID,
                         g.header.getClass().getSimpleName()},
                        {"applicationVersion", g.header.applicationVersion},
                        {"isMounted", g.header.isMounted},
                        //                                       {"dummy0", g.header.dummy0},
                        {"fileSize", g.header.fileSize},
                        //                                       {"clusterSize", g.header.clusterSize},
                        {"clusterCount", g.header.clusterCount},
                        {"checksum", g.header.checksum + " vs " + g.header.check()},
                        {"blockCount", g.blockAllocationTableHeader.blockCount,
                         g.blockAllocationTableHeader.getClass().getSimpleName()},
                        {"blocksUsed", g.blockAllocationTableHeader.blocksUsed},
                        {"lastBlockUsed", g.blockAllocationTableHeader.lastBlockUsed},
                        //                                       {"dummy0", g.blockAllocationTableHeader.dummy0},
                        //                                       {"dummy1", g.blockAllocationTableHeader.dummy1},
                        //                                       {"dummy2", g.blockAllocationTableHeader.dummy2},
                        //                                       {"dummy3", g.blockAllocationTableHeader.dummy3},
                        {"checksum",
                         g.blockAllocationTableHeader.checksum + " vs " + g.blockAllocationTableHeader.check()},
                        {"clusterCount", g.fragMap.clusterCount,
                         g.fragMap.getClass().getSimpleName()},
                        {"firstUnusedEntry", g.fragMap.firstUnusedEntry},
                        //                                       {"isLongTerminator", g.fragMap.isLongTerminator},
                        {"checksum", g.fragMap.checksum + " vs " + g.fragMap.check()},
                        //                                       {"headerVersion", g.manifestHeader.headerVersion, g.manifestHeader.getClass().getSimpleName()},
                        //                                       {"applicationID", g.manifestHeader.applicationID},
                        //                                       {"applicationVersion", g.manifestHeader.applicationVersion},
                        {"nodeCount", g.manifestHeader.nodeCount,
                         g.manifestHeader.getClass().getSimpleName()},
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
                        {"checksumSize", g.checksumHeader.checksumSize,
                         g.checksumHeader.getClass().getSimpleName()},
                        //                                       {"formatCode", g.checksumMapHeader.formatCode, g.checksumMapHeader.getClass().getSimpleName()},
                        //                                       {"dummy0", g.checksumMapHeader.dummy0},
                        {"itemCount", g.checksumMapHeader.itemCount,
                         g.checksumMapHeader.getClass().getSimpleName()},
                        {"checksumCount", g.checksumMapHeader.checksumCount},
                        //                                       {"gcfRevision", g.dataBlockHeader.gcfRevision, g.dataBlockHeader.getClass().getSimpleName()},
                        {"blockCount", g.dataBlockHeader.blockCount,
                         g.dataBlockHeader.getClass().getSimpleName()},
                        //                                       {"blockSize", g.dataBlockHeader.blockSize},
                        {"firstBlockOffset", g.dataBlockHeader.firstBlockOffset},
                        {"blocksUsed", g.dataBlockHeader.blocksUsed},
                        {"checksum", g.dataBlockHeader.checksum + " vs " + g.dataBlockHeader.check()}};
                    for(int i = 0; i < rows.length; i++) {
                        model.insertRow(model.getRowCount(), rows[i]);
                    }
                } else if(nodeInfo instanceof GCFDirectoryEntry) {
                    GCFDirectoryEntry d = (GCFDirectoryEntry) nodeInfo;
                    model.insertRow(model.getRowCount(), new Object[] {"index", d.index, ""});
                    model.insertRow(model.getRowCount(), new Object[] {"itemSize", d.itemSize, ""});
                    model.insertRow(model.getRowCount(), new Object[] {"attributes", d.attributes,
                                                                       ""});
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
        canvas = new VGUICanvas() {
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
        final SteamID user = SteamUtils.getUser();
        if(user != null) {
            LOG.log(Level.INFO, "Current user: {0}", user);

            SwingWorker<Image, Void> worker = new SwingWorker<Image, Void>() {
                @Override
                public Image doInBackground() {
                    File screenshotDir = new File(SteamUtils.getUserData(),
                                                  "760/remote/440/screenshots/");
                    File[] files = screenshotDir.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".jpg");
                        }
                    });
                    if(files != null) {
                        try {
                            return new ImageIcon(
                                    files[(int) (Math.random() * (files.length - 1))].toURI().toURL()).getImage();
                        } catch(MalformedURLException ex) {
                            Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    LOG.log(Level.INFO, "No screenshots in {0}", screenshotDir);
                    return null;
                }

                @Override
                public void done() {
                    try {
                        canvas.setBackgroundImage(get());
                    } catch(InterruptedException ex) {
                        Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch(ExecutionException ex) {
                        Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            worker.execute();
        } else {
            LOG.log(Level.WARNING, "Steam not found");
        }

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

        mount();

    }

    private void loadProps(final Element element) {
        propTable.clear();
        DefaultTableModel model = (DefaultTableModel) propTable.getModel();
        if(!element.getProps().isEmpty()) {
            element.validateDisplay();
            for(int i = 0; i < element.getProps().size(); i++) {
                Property entry = element.getProps().get(i);
                if(entry.getKey().equals("\\n")) {
                    continue;
                }
                model.addRow(new Object[] {entry.getKey(), entry.getValue(), entry.getInfo()});
            }
            model.fireTableDataChanged();
            propTable.repaint();
        }
    }

    /**
     * Sets the look and feel
     */
    private static void lookAndFeel() {
        LOG.log(Level.INFO, "L&F: {0} | {1}", new Object[] {System.getProperty("swing.defaultlaf"),
                                                            Main.prefs.get("theme", null)});
        switch(OS.get()) {
            case OSX:
                UIManager.installLookAndFeel("Quaqua", "ch.randelshofer.quaqua.QuaquaLookAndFeel");
                break;
            case Linux:
                UIManager.installLookAndFeel("GTK extended",
                                             "org.gtk.laf.extended.GTKLookAndFeelExtended");
                break;
        }

        if(System.getProperty("swing.defaultlaf") == null && Main.prefs.get("theme", null) == null) { // Do not override user specified theme
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
                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch(InstantiationException ex) {
                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch(IllegalAccessException ex) {
                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch(UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Fall back to native">
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(ClassNotFoundException ex) {
                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch(InstantiationException ex) {
                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch(IllegalAccessException ex) {
                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch(UnsupportedLookAndFeelException ex) {
                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
            //</editor-fold>
        } else {
            String theme = System.getProperty("swing.defaultlaf");
            if(theme == null) {
                theme = Main.prefs.get("theme", null);
            }
            try {
                UIManager.setLookAndFeel(theme);
            } catch(InstantiationException ex) {
                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch(IllegalAccessException ex) {
                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch(UnsupportedLookAndFeelException ex) {
                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch(ClassNotFoundException ex) {
//                    Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
                LOG.warning("Unable to load user L&F");
            }
        }

        //<editor-fold defaultstate="collapsed" desc="Improve native LaF">
        if(UIManager.getLookAndFeel()
                .isNativeLookAndFeel()) {
            try {
                LOG.log(Level.INFO, "Adding swing enhancements for {0}", new Object[] {OS.get()});
                if(OS.isMac()) {
                    UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel"); // Apply quaqua if available
                } else if(OS.isLinux()) {
                    if(UIManager.getLookAndFeel().getClass().getName().equals(
                            "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
                        GtkFixer.installGtkPopupBugWorkaround(); // Apply clearlooks java menu fix if applicable
                        UIManager.setLookAndFeel("org.gtk.laf.extended.GTKLookAndFeelExtended"); // Apply extended gtk theme is available. http://danjared.wordpress.com/2012/05/21/mejorando-la-integracion-de-javaswing-con-gtk/
                    }
                }
                LOG.info("All swing enhancements installed");
            } catch(InstantiationException ex) {
                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch(IllegalAccessException ex) {
                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch(UnsupportedLookAndFeelException ex) {
                Logger.getLogger(HUDEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch(ClassNotFoundException ex) {
//                Logger.getLogger(EditorFrame.class.getName()).log(Level.INFO, null, ex);
                LOG.warning("Unable to load enhanced L&F");
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
    //<editor-fold defaultstate="collapsed" desc="Menu Bar">

    private class EditorMenuBar extends JMenuBar {

        private JMenuItem newItem, openItem, openZippedItem, saveItem, saveAsItem, reloadItem, closeItem, exitItem;

        private JMenuItem undoItem, redoItem, cutItem, copyItem, pasteItem, deleteItem, selectAllItem, preferencesItem, locateUserItem;

        private JMenuItem resolutionItem, previewItem;

        private JMenuItem updateItem, aboutItem;

        private int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        EditorMenuBar() {
            super();

            //<editor-fold defaultstate="collapsed" desc="File">
            JMenu fileMenu = new JMenu(Main.getString("File"));
            fileMenu.setMnemonic(KeyEvent.VK_F);
            this.add(fileMenu);

            newItem = new JMenuItem(new CustomAction(Main.getString("New"), null, KeyEvent.VK_N,
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_N, modifier)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
            newItem.setEnabled(false);
            fileMenu.add(newItem);

            openItem = new JMenuItem(new CustomAction("Open", null, KeyEvent.VK_O,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_O, modifier)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    locateHudDirectory();
                }
            });
            fileMenu.add(openItem);

            openZippedItem = new JMenuItem(new CustomAction("OpenArchive", null, KeyEvent.VK_Z,
                                                            KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                                                   modifier + ActionEvent.SHIFT_MASK)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    locateZippedHud();
                }
            });
            openZippedItem.setEnabled(false);
            fileMenu.add(openZippedItem);

            fileMenu.addSeparator();

            closeItem = new JMenuItem(new CustomAction("Close", null, KeyEvent.VK_C,
                                                       KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                                              modifier)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    //                    close();
                }
            });

            if(OS.isMac()) {
                fileMenu.add(closeItem);
            }

            saveItem = new JMenuItem(new CustomAction("Save", null, KeyEvent.VK_S,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_S, modifier)) {
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
                                                        KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                                               modifier + ActionEvent.SHIFT_MASK)) {
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
                    load(lastLoaded);
                }
            });
            reloadItem.setEnabled(false);
            fileMenu.add(reloadItem);

            if(!OS.isMac()) {
                fileMenu.addSeparator();
                fileMenu.add(closeItem);

                exitItem = new JMenuItem(new CustomAction("Exit", null, KeyEvent.VK_X,
                                                          KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                                                                                 modifier)) {
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
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifier)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                }
            });
            undoItem.setEnabled(false);
            editMenu.add(undoItem);

            redoItem = new JMenuItem(new CustomAction("Redo", null, KeyEvent.VK_R,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_Y, modifier)) { // TODO: ctrl + shift + z
                @Override
                public void actionPerformed(ActionEvent ae) {
                }
            });
            redoItem.setEnabled(false);
            editMenu.add(redoItem);

            editMenu.addSeparator();

            cutItem = new JMenuItem(new CustomAction("Cut", null, KeyEvent.VK_T,
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_X, modifier)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                }
            });
            cutItem.setEnabled(false);
            editMenu.add(cutItem);

            copyItem = new JMenuItem(new CustomAction("Copy", null, KeyEvent.VK_C,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_C, modifier)) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                }
            });
            copyItem.setEnabled(false);
            editMenu.add(copyItem);

            pasteItem = new JMenuItem(new CustomAction("Paste", null, KeyEvent.VK_P,
                                                       KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                                              modifier)) {
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
                                                           KeyStroke.getKeyStroke(KeyEvent.VK_A,
                                                                                  modifier)) {
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
                preferencesItem = new JMenuItem(new CustomAction("Preferences", null, KeyEvent.VK_E,
                                                                 null) {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        preferences();
                    }
                });
                editMenu.add(preferencesItem);
            }

            locateUserItem = new JMenuItem(new CustomAction("Select user folder", null,
                                                            KeyEvent.VK_S, null) {
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

            resolutionItem = new JMenuItem(
                    new CustomAction("Change Resolution", null, KeyEvent.VK_R,
                                     KeyStroke.getKeyStroke(KeyEvent.VK_R, modifier)) {
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
                    HUDEditor.this.dispose();
                    HUDEditor.this.setUndecorated(!fullscreen);
                    HUDEditor.this.setExtendedState(
                            fullscreen ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH);
                    HUDEditor.this.setVisible(true);
                    HUDEditor.this.setJMenuBar(jmb);
                    HUDEditor.this.pack();
                    HUDEditor.this.toFront();
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
                    HUDEditor.this.checkForUpdates(true);
                }
            });
            updateItem.setEnabled(Main.myVer != 0); // XXX
            updateItem.setEnabled(true);
            helpMenu.add(updateItem);

            if(!OS.isMac()) {
                aboutItem = new JMenuItem(new CustomAction("About", null, KeyEvent.VK_A, null) {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        about();
                    }
                });
                helpMenu.add(aboutItem);
            }
        }

        //<editor-fold defaultstate="collapsed" desc="Extras">
        private void extras() {
            JMenu extrasMenu = new JMenu("Extras");
            extrasMenu.setMnemonic(KeyEvent.VK_X);
            this.add(extrasMenu);

            JMenuItem vtfItem = new JMenuItem(new CustomAction("VTF Viewer", null, KeyEvent.VK_T,
                                                               null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    VTFTest.main("");
                }
            });
            extrasMenu.add(vtfItem);

            JMenuItem captionItem = new JMenuItem(new CustomAction("Caption Editor", null,
                                                                   KeyEvent.VK_C, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    VCCDTest.main("");
                }
            });
            extrasMenu.add(captionItem);

            JMenuItem vdfItem = new JMenuItem(new CustomAction("VDF Viewer", null, KeyEvent.VK_D,
                                                               null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    DataTest.main("");
                }
            });
            extrasMenu.add(vdfItem);

            JMenuItem gcfItem = new JMenuItem(new CustomAction("Archive Explorer", null,
                                                               KeyEvent.VK_E, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ArchiveExplorer.main("");
                }
            });
            extrasMenu.add(gcfItem);

            JMenuItem bitmapItem = new JMenuItem(new CustomAction("Bitmap Font Glyph Editor", null,
                                                                  KeyEvent.VK_G, null) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    VBFTest.main("");
                }
            });
            extrasMenu.add(bitmapItem);
        }
        //</editor-fold>

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
    //</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Generated Code">

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jDialog1 = new javax.swing.JDialog(this);
        jLabel1 = new javax.swing.JLabel();
        themeSelector1 = new com.timepath.swing.ThemeSelector();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        tools = new com.timepath.swing.BlendedToolBar();
        rootSplit = new javax.swing.JSplitPane();
        sideSplit = new javax.swing.JSplitPane();
        tabbedContent = new javax.swing.JTabbedPane();
        status = new com.timepath.swing.StatusBar();

        jDialog1.setTitle("Preferences");
        jDialog1.setMinimumSize(new java.awt.Dimension(400, 300));
        jDialog1.setModalityType(java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
        jDialog1.getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Theme:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jDialog1.getContentPane().add(jLabel1, gridBagConstraints);

        themeSelector1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                themeSelector1PropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jDialog1.getContentPane().add(themeSelector1, gridBagConstraints);

        jLabel2.setText("Auto update:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jDialog1.getContentPane().add(jLabel2, gridBagConstraints);

        jCheckBox1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jCheckBox1PropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jDialog1.getContentPane().add(jCheckBox1, gridBagConstraints);

        jLabel3.setText("Console Detail:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jDialog1.getContentPane().add(jLabel3, gridBagConstraints);

        jLabel4.setText("Logfile Detail:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jDialog1.getContentPane().add(jLabel4, gridBagConstraints);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new Object[] {Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL}));
        jComboBox1.setSelectedItem(Main.consoleLevel);
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jDialog1.getContentPane().add(jComboBox1, gridBagConstraints);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new Object[] {Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL}));
        jComboBox2.setSelectedItem(Main.logfileLevel);
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jDialog1.getContentPane().add(jComboBox2, gridBagConstraints);

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

    private void jCheckBox1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jCheckBox1PropertyChange
        Main.prefs.putBoolean("autoupdate", jCheckBox1.isSelected());
    }//GEN-LAST:event_jCheckBox1PropertyChange

    private void themeSelector1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_themeSelector1PropertyChange
        Main.prefs.put("theme", UIManager.getLookAndFeel().getClass().getName());
    }//GEN-LAST:event_themeSelector1PropertyChange

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        Main.prefs.put("consoleLevel", jComboBox1.getSelectedItem().toString().toUpperCase());
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        Main.prefs.put("logfileLevel", jComboBox2.getSelectedItem().toString().toUpperCase());
    }//GEN-LAST:event_jComboBox2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSplitPane rootSplit;
    private javax.swing.JSplitPane sideSplit;
    private com.timepath.swing.StatusBar status;
    private javax.swing.JTabbedPane tabbedContent;
    private com.timepath.swing.ThemeSelector themeSelector1;
    private com.timepath.swing.BlendedToolBar tools;
    // End of variables declaration//GEN-END:variables
    //</editor-fold>
    //</editor-fold>
}
