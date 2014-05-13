package com.timepath.hl2.hudeditor;

import com.apple.OSXAdapter;
import com.timepath.Utils;
import com.timepath.hl2.io.RES;
import com.timepath.hl2.io.VMT;
import com.timepath.hl2.io.image.VTF;
import com.timepath.hl2.io.util.Element;
import com.timepath.hl2.swing.VGUICanvas;
import com.timepath.plaf.IconList;
import com.timepath.plaf.OS;
import com.timepath.plaf.linux.Ayatana;
import com.timepath.plaf.mac.Application;
import com.timepath.plaf.mac.Application.*;
import com.timepath.plaf.x.filechooser.BaseFileChooser;
import com.timepath.plaf.x.filechooser.NativeFileChooser;
import com.timepath.steam.SteamID;
import com.timepath.steam.SteamUtils;
import com.timepath.steam.io.VDF1;
import com.timepath.steam.io.storage.ACF;
import com.timepath.steam.io.storage.Files;
import com.timepath.steam.io.storage.VPK;
import com.timepath.steam.io.storage.util.ExtendedVFile;
import com.timepath.steam.io.util.Property;
import com.timepath.steam.io.util.VDFNode1;
import com.timepath.swing.BlendedToolBar;
import com.timepath.swing.StatusBar;
import com.timepath.vfs.SimpleVFile;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author TimePath
 */
@SuppressWarnings("serial")
public class HUDEditor extends JFrame {

    private static final Logger LOG = Logger.getLogger(HUDEditor.class.getName());
    private final EditorMenuBar          jmb;
    private final DefaultMutableTreeNode fileSystemRoot, archiveRoot;
    private final ProjectTree   fileTree;
    private final PropertyTable propTable;
    private       VGUICanvas    canvas;
    private       File          lastLoaded;
    private       JSpinner      spinnerWidth;
    private       JSpinner      spinnerHeight;
    private HyperlinkListener linkListener = Utils.getLinkListener();
    private JSplitPane     rootSplit;
    private JSplitPane     sideSplit;
    private StatusBar      status;
    private JTabbedPane    tabbedContent;
    private BlendedToolBar tools;

    public HUDEditor() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        }); setIconImages(new IconList("/com/timepath/hl2/hudeditor/res/Icon", "png", new int[] {
                16, 22, 24, 32, 40, 48, 64, 128, 512, 1024
        }
        ).getIcons()); setTitle(Main.getString("Title"));
        getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE); // Mac tweak
        boolean buggyWM = true; if(OS.isLinux() && buggyWM) {
            addComponentListener(new ComponentAdapter() {
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
                    Rectangle b = getBounds(); Rectangle s = getGraphicsConfiguration().getBounds(); if(moved) {
                        moved = false; return;
                    } if(updateReal) {
                        real.x = b.x; real.y = b.y;
                    } updateReal = true; b.x = real.x; b.y = real.y; if(( b.x + b.width ) > s.width) {
                        b.x -= ( b.x + b.width ) - s.width; updateReal = false;
                    } if(( b.y + b.height ) > s.height) {
                        b.y = 0; updateReal = false;
                    } setBounds(b);
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    Rectangle b = getBounds(); moved = true; real.x = b.x; real.y = b.y;
                }
            });
        } setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE); Transferable t = dtde.getTransferable(); File file = null;
                    if(OS.isLinux()) {
                        DataFlavor nixFileDataFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
                        String data = (String) t.getTransferData(nixFileDataFlavor);
                        for(StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens(); ) {
                            String token = st.nextToken().trim(); if(token.startsWith("#") || ( token.isEmpty() )) {
                                // comment line, by RFC 2483
                                continue;
                            } try {
                                file = new File(new URI(token));
                            } catch(Exception ignored) {
                            }
                        }
                    } else {
                        Object data = t.getTransferData(DataFlavor.javaFileListFlavor); if(data instanceof Iterable) {
                            for(Object o : (Iterable<? extends Object>) data) {
                                if(o instanceof File) {
                                    file = (File) o;
                                }
                            }
                        }
                    } if(file != null) {
                        load(file);
                    }
                } catch(ClassNotFoundException | InvalidDnDOperationException | UnsupportedFlavorException | IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } finally {
                    dtde.dropComplete(true); repaint();
                }
            }
        }); GraphicsConfiguration gc = getGraphicsConfiguration(); Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = getToolkit().getScreenInsets(gc);
        Dimension workspace = new Dimension(screenBounds.width - screenInsets.left - screenInsets.right,
                                            screenBounds.height - screenInsets.top - screenInsets.bottom);
        setMinimumSize(new Dimension(Math.max(workspace.width / 2, 640), Math.max(( 3 * workspace.height ) / 4, 480)));
        setPreferredSize(new Dimension((int) ( workspace.getWidth() / 1.5 ), (int) ( workspace.getHeight() / 1.5 )));
        setLocationRelativeTo(null); jmb = new EditorMenuBar(); setJMenuBar(jmb); String str = Main.prefs.get("lastLoaded", null);
        if(str != null) {
            setLastLoaded(new File(str));
        } initComponents(); tools.setWindow(this); tools.putClientProperty("Quaqua.ToolBar.style", "title");
        status.putClientProperty("Quaqua.ToolBar.style", "bottom"); archiveRoot = new DefaultMutableTreeNode("Archives");
        fileSystemRoot = new DefaultMutableTreeNode("Projects"); fileTree = new ProjectTree();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(); ( (DefaultTreeModel) fileTree.getModel() ).setRoot(root);
        root.add(archiveRoot); root.add(fileSystemRoot); ( (DefaultTreeModel) fileTree.getModel() ).reload();
        JScrollPane fileTreePane = new JScrollPane(fileTree);
        fileTreePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        sideSplit.setTopComponent(fileTreePane); fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent(); if(node == null) {
                    return;
                } propTable.clear(); DefaultTableModel model = (DefaultTableModel) propTable.getModel();
                Object nodeInfo = node.getUserObject();
                // TODO: introspection
                if(nodeInfo instanceof VDFNode1) {
                    Element element = Element.importVdf((VDFNode1) nodeInfo); loadProps(element); try {
                        canvas.load(element);
                    } catch(NullPointerException ex) {
                        ex.printStackTrace();
                    }
                } else if(nodeInfo instanceof VTF) {
                    VTF v = (VTF) nodeInfo;
                    for(int i = Math.max(v.getMipCount() - 8, 0); i < Math.max(v.getMipCount() - 5, v.getMipCount()); i++) {
                        try {
                            ImageIcon img = new ImageIcon(v.getImage(i));
                            model.insertRow(model.getRowCount(), new Object[] { "mip[" + i + "]", img, "" });
                        } catch(IOException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    } model.insertRow(model.getRowCount(), new Object[] { "version", v.getVersion(), "" });
                    model.insertRow(model.getRowCount(), new Object[] {
                            "headerSize", v.getHeaderSize(), ""
                    }); model.insertRow(model.getRowCount(), new Object[] { "width", v.getWidth(), "" });
                    model.insertRow(model.getRowCount(), new Object[] { "height", v.getHeight(), "" });
                    model.insertRow(model.getRowCount(), new Object[] { "flags", v.getFlags(), "" });
                    model.insertRow(model.getRowCount(), new Object[] {
                            "frameFirst", v.getFrameFirst(), ""
                    }); model.insertRow(model.getRowCount(), new Object[] {
                            "reflectivity", v.getReflectivity(), ""
                    }); model.insertRow(model.getRowCount(), new Object[] { "bumpScale", v.getBumpScale(), "" });
                    model.insertRow(model.getRowCount(), new Object[] { "format", v.getFormat(), "" });
                    model.insertRow(model.getRowCount(), new Object[] { "mipCount", v.getMipCount(), "" });
                    model.insertRow(model.getRowCount(), new Object[] {
                            "thumbFormat", v.getThumbFormat(), ""
                    }); model.insertRow(model.getRowCount(), new Object[] {
                            "thumbWidth", v.getThumbWidth(), ""
                    }); model.insertRow(model.getRowCount(), new Object[] {
                            "thumbHeight", v.getThumbHeight(), ""
                    }); model.insertRow(model.getRowCount(), new Object[] { "depth", v.getDepth(), "" });
                }
            }
        }); propTable = new PropertyTable(); JScrollPane propTablePane = new JScrollPane(propTable);
        sideSplit.setBottomComponent(propTablePane); canvas = new VGUICanvas() {
            @Override
            public void placed() {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent(); if(node == null) {
                    return;
                } Object nodeInfo = node.getUserObject(); if(nodeInfo instanceof Element) {
                    Element element = (Element) nodeInfo; loadProps(element);
                }
            }
        }; SteamID user = SteamUtils.getUser(); if(user != null) {
            LOG.log(Level.INFO, "Current user: {0}", user); SwingWorker<Image, Void> worker = new SwingWorker<Image, Void>() {
                @Override
                public Image doInBackground() {
                    File screenshotDir = new File(SteamUtils.getUserData(), "760/remote/440/screenshots/");
                    File[] files = screenshotDir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".jpg");
                        }
                    }); if(files != null) {
                        try {
                            return new ImageIcon(files[(int) ( Math.random() * ( files.length - 1 ) )].toURI()
                                                                                                      .toURL()).getImage();
                        } catch(MalformedURLException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    } LOG.log(Level.INFO, "No screenshots in {0}", screenshotDir); return null;
                }

                @Override
                public void done() {
                    try {
                        canvas.setBackgroundImage(get());
                    } catch(InterruptedException | ExecutionException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }; worker.execute();
        } else {
            LOG.log(Level.WARNING, "Steam not found");
        } JScrollPane canvasPane = new JScrollPane(canvas);
        //        canvasPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        canvasPane.getVerticalScrollBar().setBlockIncrement(30); canvasPane.getVerticalScrollBar().setUnitIncrement(20);
        //        canvasPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        canvasPane.getHorizontalScrollBar().setBlockIncrement(30); canvasPane.getHorizontalScrollBar().setUnitIncrement(20);
        tabbedContent.add(Main.getString("Canvas"), canvasPane); canvas.requestFocusInWindow(); mount(440);
    }

    public static void analyze(final DefaultMutableTreeNode top, final boolean leaves) {
        if(!( top.getUserObject() instanceof ExtendedVFile )) {
            return;
        } ExecutorService es = Executors.newCachedThreadPool(); ExtendedVFile e = (ExtendedVFile) top.getUserObject();
        for(final SimpleVFile n : e.children()) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    DefaultMutableTreeNode child = new DefaultMutableTreeNode(n); if(n.isDirectory()) {
                        analyze(child, leaves); top.add(child);
                    } else if(leaves) {
                        InputStream is = n.stream(); LOG.info(n.getName());
                        if(n.getName().endsWith(".vdf") || n.getName().endsWith(".pop") || n.getName().endsWith(".layout") ||
                           n.getName().endsWith(".menu") || n.getName().endsWith(".styles")) {
                            VDF1 v = new VDF1(); v.readExternal(n.stream()); child.add(new DefaultMutableTreeNode(v.getRoot()));
                        } else if(n.getName().endsWith(".res")) {
                            RES v = new RES(); v.readExternal(is); child.add(new DefaultMutableTreeNode(v.getRoot()));
                        } else if(n.getName().endsWith(".vmt")) {
                            VMT v = new VMT(); v.readExternal(is);
                        } else if(n.getName().endsWith(".vtf")) {
                            VTF v = null; try {
                                v = VTF.load(is);
                            } catch(IOException ex) {
                                LOG.log(Level.SEVERE, null, ex);
                            } if(v != null) {
                                child.setUserObject(v);
                            }
                        } top.add(child);
                    }
                }
            }; es.submit(r);
        } es.shutdown(); try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch(InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static void recurseDirectoryToNode(ExtendedVFile ar, DefaultMutableTreeNode project) {
        project.setUserObject(ar); analyze(project, true);
    }

    private void error(Object msg) {
        error(msg, Main.getString("Error"));
    }

    private void warn(Object msg) {
        error(msg, Main.getString("Warning"));
    }

    private void error(Object msg, String title) {
        LOG.log(Level.SEVERE, "{0}:{1}", new Object[] { title, msg });
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private void warn(Object msg, String title) {
        LOG.log(Level.WARNING, "{0}:{1}", new Object[] { title, msg });
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    private void info(Object msg) {
        info(msg, Main.getString("Info"));
    }

    private void info(Object msg, String title) {
        LOG.log(Level.INFO, "{0}:{1}", new Object[] { title, msg });
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void preferences() {
        info("No app-specific preferences yet", "Preferences");
    }

    public void about() {
        JEditorPane pane = new JEditorPane("text/html", ""); pane.setEditable(false); pane.setOpaque(false);
        pane.setBackground(new Color(0, 0, 0, 0)); pane.addHyperlinkListener(linkListener);
        String aboutText = "<html><h2>This is to be a What You See Is What You Get HUD Editor for TF2,</h2>";
        aboutText += "for graphically editing TF2 HUDs!";
        aboutText += "<p>Author: TimePath (<a href=\"http://steamcommunity.com/id/TimePath/\">steam</a>|<a href=\"http://www" +
                     ".reddit.com/user/TimePath/\">reddit</a>)<br>"; String p1 = aboutText; pane.setText(p1); info(pane, "About");
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
            File[] selection = new NativeFileChooser().setParent(this)
                                                      .setTitle(Main.getString("LoadHudDir"))
                                                      .setFile(lastLoaded)
                                                      .setFileMode(BaseFileChooser.FileMode.DIRECTORIES_ONLY)
                                                      .choose(); if(selection == null) {
                return;
            } load(selection[0]);
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void locateZippedHud() {
        try {
            File[] selection = new NativeFileChooser().setParent(this)
                                                      .setTitle(Main.getString("OpenArchive"))
                                                      .setFile(lastLoaded)
                                                      .setFileMode(BaseFileChooser.FileMode.FILES_ONLY)
                                                      .choose(); if(selection == null) {
                return;
            } load(selection[0]);
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void load(final File f) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); new SwingWorker<DefaultMutableTreeNode, Void>() {
            long start = System.currentTimeMillis();

            @Override
            public DefaultMutableTreeNode doInBackground() {
                return doLoad(f);
            }

            @Override
            protected void done() {
                try {
                    DefaultMutableTreeNode project = get(); setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    if(project != null) {
                        LOG.log(Level.INFO, "Loaded hud - took {0}ms", System.currentTimeMillis() - start);
                        fileSystemRoot.add(project); fileTree.expandPath(new TreePath(project.getPath()));
                        fileTree.setSelectionRow(fileSystemRoot.getIndex(project)); fileTree.requestFocusInWindow();
                    }
                } catch(Throwable t) {
                    LOG.log(Level.SEVERE, null, t);
                }
            }
        }.execute();
    }

    private void changeResolution() {
        //<editor-fold defaultstate="collapsed" desc="Number filter">
        //    private static class NumericDocumentFilter extends DocumentFilter {
        //
        //        @Override
        //        public void insertString(FilterBypass fb, int offset, String string,
        // AttributeSet attr) throws BadLocationException {
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
        //        public void replace(FilterBypass fb, int offset, int length, String text,
        // AttributeSet attrs) throws BadLocationException {
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
        List<String> listItems = new LinkedList<String>(); for(GraphicsDevice device : env.getScreenDevices()) {
            for(DisplayMode resolution : device.getDisplayModes()) { // TF2 has different resolutions
                String item = resolution.getWidth() + "x" + resolution.getHeight(); // TODO: Work out aspect ratios
                if(!listItems.contains(item)) {
                    listItems.add(item);
                }
            }
        } dropDown.addItem("Custom"); for(String listItem : listItems) {
            dropDown.addItem(listItem);
        } dropDown.setSelectedIndex(1); dropDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = dropDown.getSelectedItem().toString(); boolean isRes = item.contains("x");
                spinnerWidth.setEnabled(!isRes); spinnerHeight.setEnabled(!isRes); if(isRes) {
                    String[] xy = item.split("x"); spinnerWidth.setValue(Integer.parseInt(xy[0]));
                    spinnerHeight.setValue(Integer.parseInt(xy[1]));
                }
            }
        }); Object[] message = {
                "Presets: ", dropDown, "Width: ", spinnerWidth, "Height: ", spinnerHeight
        }; JOptionPane optionPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null);
        JDialog dialog = optionPane.createDialog(this, "Change resolution..."); dialog.setContentPane(optionPane); dialog.pack();
        dialog.setVisible(true); if(optionPane.getValue() != null) {
            int value = ( (Number) optionPane.getValue() ).intValue(); if(value == JOptionPane.YES_OPTION) {
                canvas.setPreferredSize(new Dimension(Integer.parseInt(spinnerWidth.getValue().toString()),
                                                      Integer.parseInt(spinnerHeight.getValue().toString())));
            }
        }
    }

    @Override
    public void setJMenuBar(JMenuBar menubar) {
        LOG.log(Level.INFO, "Setting menubar for {0}", OS.get()); super.setJMenuBar(menubar); if(OS.isMac()) {
            try {
                //<editor-fold defaultstate="collapsed" desc="Deprecated">
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
                OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[]) null));
                //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="Unimplemented">
                Application app = Application.getApplication(); app.setAboutHandler(new AboutHandler() {
                    @Override
                    public void handleAbout(AboutEvent e) {
                        about();
                    }
                }); app.setPreferencesHandler(new PreferencesHandler() {
                    @Override
                    public void handlePreferences(PreferencesEvent e) {
                        preferences();
                    }
                }); app.setQuitHandler(new QuitHandler() {
                    @Override
                    public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
                        quit();
                    }
                }); URL url = getClass().getResource("/com/timepath/hl2/hudeditor/res/Icon.png");
                Image icon = Toolkit.getDefaultToolkit().getImage(url); app.setDockIconImage(icon);
                //</editor-fold>
            } catch(Exception e) {
                LOG.severe(e.toString());
            }
        } else if(OS.isLinux()) {
            if(!Ayatana.tryInstallMenu(this, menubar)) {
                LOG.log(Level.WARNING, "AyatanaDesktop failed to load for {0}", System.getenv("XDG_CURRENT_DESKTOP"));
            }
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b); createBufferStrategy(2);
    }

    private DefaultMutableTreeNode doLoad(File root) {
        if(root == null) {
            return null;
        } if(!root.exists()) {
            error(new MessageFormat(Main.getString("FileAccessError")).format(new Object[] { root }));
        } setLastLoaded(root); LOG.log(Level.INFO, "You have selected: {0}", root.getAbsolutePath()); if(root.isDirectory()) {
            File[] folders = root.listFiles();
            boolean valid = true; // TODO: find resource and scripts if there is a parent directory
            for(File folder : folders) {
                if(folder.isDirectory() &&
                   ( "resource".equalsIgnoreCase(folder.getName()) || "scripts".equalsIgnoreCase(folder.getName()) )) {
                    valid = true; break;
                }
            } if(!valid) {
                error("Selection not valid. Please choose a folder containing \'resources\' or \'scripts\'.");
                locateHudDirectory(); return null;
            } DefaultMutableTreeNode project = new DefaultMutableTreeNode(root.getName());
            recurseDirectoryToNode(new Files(root), project); return project;
        } if(root.getName().endsWith(".zip")) {
            try {
                ZipInputStream zin = new ZipInputStream(new FileInputStream(root)); ZipEntry entry;
                while(( entry = zin.getNextEntry() ) != null) {
                    LOG.log(Level.INFO, "{0}", entry.getName()); zin.closeEntry();
                } zin.close();
            } catch(IOException e) {
            } return null;
        } if(root.getName().endsWith("_dir.vpk")) {
            DefaultMutableTreeNode project = new DefaultMutableTreeNode(root.getName());
            recurseDirectoryToNode(VPK.loadArchive(root), project); return project;
        } return null;
    }

    private void mount(final int appID) {
        new SwingWorker<DefaultMutableTreeNode, Void>() {
            @Override
            protected DefaultMutableTreeNode doInBackground() throws Exception {
                LOG.log(Level.INFO, "Mounting {0}", appID); ExtendedVFile a = ACF.fromManifest(appID);
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(a); a.analyze(child, true); return child;
            }

            @Override
            protected void done() {
                try {
                    DefaultMutableTreeNode g = get(); if(g != null) {
                        archiveRoot.add(g); ( (DefaultTreeModel) fileTree.getModel() ).reload(archiveRoot);
                        LOG.log(Level.INFO, "Mounted {0}", appID);
                    }
                } catch(InterruptedException | ExecutionException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }.execute();
    }

    public void quit() {
        LOG.info("Closing..."); dispose();
    }

    private void setLastLoaded(File root) {
        jmb.reloadItem.setEnabled(root != null); if(( root == null ) || !root.exists()) {
            return;
        } lastLoaded = root; Main.prefs.put("lastLoaded", root.getPath());
    }

    private void loadProps(Element element) {
        propTable.clear(); DefaultTableModel model = (DefaultTableModel) propTable.getModel(); if(!element.getProps().isEmpty()) {
            element.validateDisplay(); for(int i = 0; i < element.getProps().size(); i++) {
                Property entry = element.getProps().get(i); if("\\n".equals(entry.getKey())) {
                    continue;
                } model.addRow(new Object[] { entry.getKey(), entry.getValue(), entry.getInfo() });
            } model.fireTableDataChanged(); propTable.repaint();
        }
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints; tools = new BlendedToolBar(); rootSplit = new JSplitPane();
        sideSplit = new JSplitPane(); tabbedContent = new JTabbedPane(); status = new StatusBar();
        getContentPane().add(tools, BorderLayout.PAGE_START); rootSplit.setDividerLocation(180);
        rootSplit.setContinuousLayout(true); rootSplit.setOneTouchExpandable(true); sideSplit.setBorder(null);
        sideSplit.setOrientation(JSplitPane.VERTICAL_SPLIT); sideSplit.setResizeWeight(0.5); sideSplit.setContinuousLayout(true);
        sideSplit.setOneTouchExpandable(true); rootSplit.setLeftComponent(sideSplit); rootSplit.setRightComponent(tabbedContent);
        getContentPane().add(rootSplit, BorderLayout.CENTER); getContentPane().add(status, BorderLayout.PAGE_END); pack();
    }

    private abstract static class CustomAction extends AbstractAction {

        private CustomAction(String s, Icon icon, int mnemonic, KeyStroke shortcut) {
            super(Main.getString(s), icon); putValue(Action.MNEMONIC_KEY, mnemonic); putValue(Action.ACCELERATOR_KEY, shortcut);
        }
    }

    private class EditorMenuBar extends JMenuBar {

        private final JMenuItem newItem, openItem, openZippedItem, saveItem, saveAsItem, reloadItem, closeItem, exitItem;
        private final JMenuItem undoItem, redoItem, cutItem, copyItem, pasteItem, deleteItem, selectAllItem, preferencesItem;
        private final JMenuItem resolutionItem, previewItem;
        private final int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        private JMenuItem aboutItem;

        EditorMenuBar() {
            JMenu fileMenu = new JMenu(Main.getString("File")); fileMenu.setMnemonic(KeyEvent.VK_F); add(fileMenu);
            newItem = new JMenuItem(new CustomAction(Main.getString("New"),
                                                     null,
                                                     KeyEvent.VK_N,
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_N, modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }); newItem.setEnabled(false); fileMenu.add(newItem); openItem = new JMenuItem(new CustomAction("Open",
                                                                                                            null,
                                                                                                            KeyEvent.VK_O,
                                                                                                            KeyStroke
                                                                                                                    .getKeyStroke(
                                                                                                                    KeyEvent.VK_O,
                                                                                                                    modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    locateHudDirectory();
                }
            }); fileMenu.add(openItem); openZippedItem = new JMenuItem(new CustomAction("OpenArchive",
                                                                                        null,
                                                                                        KeyEvent.VK_Z,
                                                                                        KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                                                                               modifier +
                                                                                                               ActionEvent
                                                                                                                       .SHIFT_MASK)
            )
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    locateZippedHud();
                }
            }); fileMenu.add(openZippedItem); fileMenu.addSeparator(); closeItem = new JMenuItem(new CustomAction("Close",
                                                                                                                  null,
                                                                                                                  KeyEvent.VK_C,
                                                                                                                  KeyStroke
                                                                                                                          .getKeyStroke(
                                                                                                                          KeyEvent.VK_W,
                                                                                                                          modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //                    close();
                }
            }); if(OS.isMac()) {
                fileMenu.add(closeItem);
            } saveItem = new JMenuItem(new CustomAction("Save",
                                                        null,
                                                        KeyEvent.VK_S,
                                                        KeyStroke.getKeyStroke(KeyEvent.VK_S, modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!canvas.getElements().isEmpty()) {
                        info(canvas.getElements().get(canvas.getElements().size() - 1).save());
                    }
                }
            }); saveItem.setEnabled(false); fileMenu.add(saveItem); saveAsItem = new JMenuItem(new CustomAction("Save As...",
                                                                                                                null,
                                                                                                                KeyEvent.VK_A,
                                                                                                                KeyStroke
                                                                                                                        .getKeyStroke(
                                                                                                                        KeyEvent.VK_S,
                                                                                                                        modifier +
                                                                                                                        ActionEvent.SHIFT_MASK))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            }); saveAsItem.setEnabled(false); fileMenu.add(saveAsItem); reloadItem = new JMenuItem(new CustomAction("Revert",
                                                                                                                    null,
                                                                                                                    KeyEvent.VK_R,
                                                                                                                    KeyStroke
                                                                                                                            .getKeyStroke(
                                                                                                                            KeyEvent.VK_F5,
                                                                                                                            0))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    load(lastLoaded);
                }
            }); reloadItem.setEnabled(false); fileMenu.add(reloadItem); exitItem = new JMenuItem(new CustomAction("Exit",
                                                                                                                  null,
                                                                                                                  KeyEvent.VK_X,
                                                                                                                  KeyStroke
                                                                                                                          .getKeyStroke(
                                                                                                                          KeyEvent.VK_Q,
                                                                                                                          modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    quit();
                }
            }); if(!OS.isMac()) {
                fileMenu.addSeparator(); fileMenu.add(closeItem); fileMenu.add(exitItem);
            } JMenu editMenu = new JMenu("Edit"); editMenu.setMnemonic(KeyEvent.VK_E); add(editMenu);
            undoItem = new JMenuItem(new CustomAction("Undo",
                                                      null,
                                                      KeyEvent.VK_U,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            }); undoItem.setEnabled(false); editMenu.add(undoItem); redoItem = new JMenuItem(new CustomAction("Redo",
                                                                                                              null,
                                                                                                              KeyEvent.VK_R,
                                                                                                              KeyStroke
                                                                                                                      .getKeyStroke(
                                                                                                                      KeyEvent.VK_Y,
                                                                                                                      modifier))
            { // TODO: ctrl + shift + z
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            }); redoItem.setEnabled(false); editMenu.add(redoItem); editMenu.addSeparator();
            cutItem = new JMenuItem(new CustomAction("Cut",
                                                     null,
                                                     KeyEvent.VK_T,
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_X, modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            }); cutItem.setEnabled(false); editMenu.add(cutItem); copyItem = new JMenuItem(new CustomAction("Copy",
                                                                                                            null,
                                                                                                            KeyEvent.VK_C,
                                                                                                            KeyStroke
                                                                                                                    .getKeyStroke(
                                                                                                                    KeyEvent.VK_C,
                                                                                                                    modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            }); copyItem.setEnabled(false); editMenu.add(copyItem); pasteItem = new JMenuItem(new CustomAction("Paste",
                                                                                                               null,
                                                                                                               KeyEvent.VK_P,
                                                                                                               KeyStroke
                                                                                                                       .getKeyStroke(
                                                                                                                       KeyEvent.VK_V,
                                                                                                                       modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            }); pasteItem.setEnabled(false); editMenu.add(pasteItem); deleteItem = new JMenuItem(new CustomAction("Delete",
                                                                                                                  null,
                                                                                                                  KeyEvent.VK_D,
                                                                                                                  KeyStroke
                                                                                                                          .getKeyStroke(
                                                                                                                          KeyEvent.VK_DELETE,
                                                                                                                          0))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    canvas.removeElements(canvas.getSelected());
                }
            }); editMenu.add(deleteItem); editMenu.addSeparator(); selectAllItem = new JMenuItem(new CustomAction("Select All",
                                                                                                                  null,
                                                                                                                  KeyEvent.VK_A,
                                                                                                                  KeyStroke
                                                                                                                          .getKeyStroke(
                                                                                                                          KeyEvent.VK_A,
                                                                                                                          modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for(int i = 0; i < canvas.getElements().size(); i++) {
                        canvas.select(canvas.getElements().get(i));
                    }
                }
            }); editMenu.add(selectAllItem); editMenu.addSeparator();
            preferencesItem = new JMenuItem(new CustomAction("Preferences", null, KeyEvent.VK_E, null) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    preferences();
                }
            }); if(!OS.isMac()) {
                editMenu.add(preferencesItem);
            } JMenu viewMenu = new JMenu("View"); viewMenu.setMnemonic(KeyEvent.VK_V); add(viewMenu);
            resolutionItem = new JMenuItem(new CustomAction("Change Resolution",
                                                            null,
                                                            KeyEvent.VK_R,
                                                            KeyStroke.getKeyStroke(KeyEvent.VK_R, modifier))
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    changeResolution();
                }
            }
            ); resolutionItem.setEnabled(false); viewMenu.add(resolutionItem); previewItem = new JMenuItem(new CustomAction(
                    "Full Screen Preview",
                    null,
                    KeyEvent.VK_F,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0))
            {
                private boolean fullscreen;

                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose(); setUndecorated(!fullscreen); setExtendedState(fullscreen ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
                    HUDEditor.this.setVisible(true); setJMenuBar(jmb); pack(); toFront(); fullscreen = !fullscreen;
                }
            }); viewMenu.add(previewItem); viewMenu.addSeparator(); JMenuItem viewItem1 = new JMenuItem("Main Menu");
            viewItem1.setEnabled(false); viewMenu.add(viewItem1);
            JMenuItem viewItem2 = new JMenuItem("In-game (Health and ammo)"); viewItem2.setEnabled(false);
            viewMenu.add(viewItem2); JMenuItem viewItem3 = new JMenuItem("Scoreboard"); viewItem3.setEnabled(false);
            viewMenu.add(viewItem3); JMenuItem viewItem4 = new JMenuItem("CTF HUD"); viewItem4.setEnabled(false);
            viewMenu.add(viewItem4); if(!OS.isMac()) {
                JMenu helpMenu = new JMenu("Help"); helpMenu.setMnemonic(KeyEvent.VK_H); add(helpMenu);
                aboutItem = new JMenuItem(new CustomAction("About", null, KeyEvent.VK_A, null) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        about();
                    }
                }); helpMenu.add(aboutItem);
            }
        }
    }
}
