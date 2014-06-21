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
import com.timepath.plaf.linux.WindowMoveFix;
import com.timepath.plaf.mac.Application;
import com.timepath.plaf.mac.Application.*;
import com.timepath.plaf.x.filechooser.BaseFileChooser;
import com.timepath.plaf.x.filechooser.NativeFileChooser;
import com.timepath.steam.io.VDF;
import com.timepath.steam.io.VDFNode;
import com.timepath.steam.io.VDFNode.VDFProperty;
import com.timepath.steam.io.storage.ACF;
import com.timepath.steam.io.storage.Files;
import com.timepath.steam.io.storage.VPK;
import com.timepath.steam.io.util.ExtendedVFile;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
//import java.awt.*;

/**
 * @author TimePath
 */
@SuppressWarnings("serial")
public class HUDEditor extends JFrame {

    static final         Pattern         VDF_PATTERN = Pattern.compile("^\\.(vdf|pop|layout|menu|styles)");
    static final         ExecutorService es          = Executors.newFixedThreadPool(1);
    // Runtime.getRuntime().availableProcessors() * 5
    private static final Logger          LOG         = Logger.getLogger(HUDEditor.class.getName());
    EditorMenuBar          jmb;
    DefaultMutableTreeNode fileSystemRoot, archiveRoot;
    ProjectTree   fileTree;
    PropertyTable propTable;
    VGUICanvas    canvas;
    File          lastLoaded;
    JSpinner      spinnerWidth;
    JSpinner      spinnerHeight;
    HyperlinkListener linkListener = Utils.getLinkListener();
    JSplitPane     sideSplit;
    StatusBar      status;
    JTabbedPane    tabbedContent;
    BlendedToolBar tools;
    private DefaultTreeModel fileModel;

    public HUDEditor() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
        setIconImages(new IconList("/com/timepath/hl2/hudeditor/res/Icon",
                                   "png",
                                   new int[] { 16, 22, 24, 32, 40, 48, 64, 128, 512, 1024 }).getIcons());
        setTitle(Main.getString("Title"));
        getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE); // Mac tweak
        WindowMoveFix.install(this);
        setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable t = dtde.getTransferable();
                    File file = null;
                    if(OS.isLinux()) {
                        DataFlavor nixFileDataFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
                        String data = (String) t.getTransferData(nixFileDataFlavor);
                        for(StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens(); ) {
                            String token = st.nextToken().trim();
                            if(token.startsWith("#") || token.isEmpty()) {
                                // comment line, by RFC 2483
                                continue;
                            }
                            try {
                                file = new File(new URI(token));
                            } catch(Exception ignored) {
                            }
                        }
                    } else {
                        Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
                        if(data instanceof Iterable) {
                            for(Object o : (Iterable<? extends Object>) data) {
                                if(o instanceof File) {
                                    file = (File) o;
                                }
                            }
                        }
                    }
                    if(file != null) {
                        loadAsync(file);
                    }
                } catch(ClassNotFoundException | InvalidDnDOperationException | UnsupportedFlavorException | IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } finally {
                    dtde.dropComplete(true);
                    repaint();
                }
            }
        });
        GraphicsConfiguration gc = getGraphicsConfiguration();
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = getToolkit().getScreenInsets(gc);
        Dimension workspace = new Dimension(screenBounds.width - screenInsets.left - screenInsets.right,
                                            screenBounds.height - screenInsets.top - screenInsets.bottom);
        setMinimumSize(new Dimension(Math.max(workspace.width / 2, 640), Math.max(( 3 * workspace.height ) / 4, 480)));
        setPreferredSize(new Dimension((int) ( workspace.getWidth() / 1.5 ), (int) ( workspace.getHeight() / 1.5 )));
        setJMenuBar(jmb = new EditorMenuBar(this));
        String str = Main.prefs.get("lastLoaded", null);
        if(str != null) {
            setLastLoaded(new File(str));
        }
        initComponents();
        new SwingWorker<Image, Void>() {
            @Override
            public Image doInBackground() {
                return BackgroundLoader.fetch();
            }

            @Override
            public void done() {
                try {
                    canvas.setBackgroundImage(get());
                } catch(InterruptedException | ExecutionException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }.execute();
        canvas.requestFocusInWindow();
        mount(440);
        pack();
        setLocationRelativeTo(null);
    }

    public static void analyze(final DefaultMutableTreeNode top, final boolean leaves) {
        if(!( top.getUserObject() instanceof ExtendedVFile )) {
            return;
        }
        ExtendedVFile root = (ExtendedVFile) top.getUserObject();
        List<Future<?>> tasks = new LinkedList<>();
        for(final SimpleVFile n : root.list()) {
            LOG.log(Level.FINE, "Loading {0}", n.getName());
            //            tasks.add(es.submit(new Runnable() {
            //                @Override
            //                public void run() {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(n);
            if(n.isDirectory()) {
                analyze(child, leaves);
                top.add(child);
            } else if(leaves) {
                try(InputStream is = n.openStream()) {
                    if(VDF_PATTERN.matcher(n.getName()).matches()) {
                        child.add(VDF.load(is).toTreeNode());
                    } else if(n.getName().endsWith(".res")) {
                        child.add(RES.load(is).toTreeNode());
                    } else if(n.getName().endsWith(".vmt")) {
                        child.add(VMT.load(is).toTreeNode());
                    } else if(n.getName().endsWith(".vtf")) {
                        VTF v = null;
                        try {
                            v = VTF.load(is);
                        } catch(IOException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                        if(v != null) {
                            child.setUserObject(v);
                        }
                    }
                } catch(IOException e) {
                    LOG.log(Level.SEVERE, null, e);
                }
                top.add(child);
            }
            //                }
            //            }));
        }
        //        for (Future<?> f : tasks) {
        //            try {
        //                f.get();
        //            } catch (InterruptedException | ExecutionException e) {
        //                LOG.log(Level.SEVERE, null, e);
        //            }
        //        }
    }

    private static void recurseDirectoryToNode(ExtendedVFile ar, DefaultMutableTreeNode project) {
        project.setUserObject(ar);
        analyze(project, true);
    }

    private void error(Object msg) {
        error(msg, Main.getString("Error"));
    }

    private void error(Object msg, String title) {
        LOG.log(Level.SEVERE, "{0}:{1}", new Object[] { title, msg });
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    void info(Object msg) {
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
        JEditorPane pane = new JEditorPane("text/html", "");
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setBackground(new Color(0, 0, 0, 0));
        pane.addHyperlinkListener(linkListener);
        String aboutText = "<html><h2>This is to be a What You See Is What You Get HUD Editor for TF2,</h2>";
        aboutText += "for graphically editing TF2 HUDs!";
        String p1 = aboutText;
        pane.setText(p1);
        info(pane, "About");
    }

    void locateHudDirectory() {
        try {
            File[] selection = new NativeFileChooser().setParent(this)
                                                      .setTitle(Main.getString("LoadHudDir"))
                                                      .setDirectory(lastLoaded)
                                                      .setFileMode(BaseFileChooser.FileMode.DIRECTORIES_ONLY)
                                                      .choose();
            if(selection != null) {
                loadAsync(selection[0]);
            }
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    void loadAsync(final File f) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final long start = System.currentTimeMillis();
        new SwingWorker<DefaultMutableTreeNode, Void>() {
            @Override
            public DefaultMutableTreeNode doInBackground() {
                return load(f);
            }

            @Override
            protected void done() {
                try {
                    DefaultMutableTreeNode project = get();
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    if(project != null) {
                        LOG.log(Level.INFO, "Loaded hud - took {0}ms", System.currentTimeMillis() - start);
                        fileSystemRoot.add(project);
                        fileTree.expandPath(new TreePath(project.getPath()));
                        fileTree.setSelectionRow(fileSystemRoot.getIndex(project));
                        fileTree.requestFocusInWindow();
                    }
                } catch(Throwable t) {
                    LOG.log(Level.SEVERE, null, t);
                }
            }
        }.execute();
    }

    DefaultMutableTreeNode load(File root) {
        if(root == null) {
            return null;
        }
        if(!root.exists()) {
            error(new MessageFormat(Main.getString("FileAccessError")).format(new Object[] { root }));
        }
        setLastLoaded(root);
        LOG.log(Level.INFO, "You have selected: {0}", root.getAbsolutePath());
        if(root.isDirectory()) {
            File[] folders = root.listFiles();
            boolean valid = true; // TODO: find resource and scripts if there is a parent directory
            for(File folder : folders) {
                if(folder.isDirectory() &&
                   ( "resource".equalsIgnoreCase(folder.getName()) || "scripts".equalsIgnoreCase(folder.getName()) )) {
                    valid = true;
                    break;
                }
            }
            if(!valid) {
                error("Selection not valid. Please choose a folder containing \'resources\' or \'scripts\'.");
                locateHudDirectory();
                return null;
            }
            DefaultMutableTreeNode project = new DefaultMutableTreeNode(root.getName());
            recurseDirectoryToNode(new Files(root), project);
            return project;
        }
        if(root.getName().endsWith("_dir.vpk")) {
            DefaultMutableTreeNode project = new DefaultMutableTreeNode(root.getName());
            recurseDirectoryToNode(VPK.loadArchive(root), project);
            return project;
        }
        return null;
    }

    void changeResolution() {
        spinnerWidth.setEnabled(false);
        spinnerHeight.setEnabled(false);
        final JComboBox dropDown = new JComboBox(); // <String>
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Collection<String> listItems = new LinkedList<>();
        for(GraphicsDevice device : env.getScreenDevices()) {
            for(DisplayMode resolution : device.getDisplayModes()) { // TF2 has different resolutions
                String item = resolution.getWidth() + "x" + resolution.getHeight(); // TODO: Work out aspect ratios
                if(!listItems.contains(item)) {
                    listItems.add(item);
                }
            }
        }
        dropDown.addItem("Custom");
        for(String listItem : listItems) {
            dropDown.addItem(listItem);
        }
        dropDown.setSelectedIndex(1);
        dropDown.addActionListener(new ActionListener() {
            @Override
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
        Object[] message = {
                "Presets: ", dropDown, "Width: ", spinnerWidth, "Height: ", spinnerHeight
        };
        JOptionPane optionPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null);
        JDialog dialog = optionPane.createDialog(this, "Change resolution...");
        dialog.setContentPane(optionPane);
        dialog.pack();
        dialog.setVisible(true);
        if(optionPane.getValue() != null) {
            int value = ( (Number) optionPane.getValue() ).intValue();
            if(value == JOptionPane.YES_OPTION) {
                canvas.setPreferredSize(new Dimension(Integer.parseInt(spinnerWidth.getValue().toString()),
                                                      Integer.parseInt(spinnerHeight.getValue().toString())));
            }
        }
    }

    @Override
    public void setJMenuBar(JMenuBar menubar) {
        LOG.log(Level.INFO, "Setting menubar for {0}", OS.get());
        super.setJMenuBar(menubar);
        if(OS.isMac()) {
            try {
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
                OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[]) null));
                Application app = Application.getApplication();
                app.setAboutHandler(new AboutHandler() {
                    @Override
                    public void handleAbout(AboutEvent e) {
                        about();
                    }
                });
                app.setPreferencesHandler(new PreferencesHandler() {
                    @Override
                    public void handlePreferences(PreferencesEvent e) {
                        preferences();
                    }
                });
                app.setQuitHandler(new QuitHandler() {
                    @Override
                    public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
                        quit();
                    }
                });
                URL url = getClass().getResource("/com/timepath/hl2/hudeditor/res/Icon.png");
                Image icon = Toolkit.getDefaultToolkit().getImage(url);
                app.setDockIconImage(icon);
            } catch(Exception e) {
                LOG.severe(e.toString());
            }
        }
    }

    private void mount(final int appID) {
        new SwingWorker<DefaultMutableTreeNode, Void>() {
            @Override
            protected DefaultMutableTreeNode doInBackground() throws Exception {
                LOG.log(Level.INFO, "Mounting {0}", appID);
                ExtendedVFile a = ACF.fromManifest(appID);
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(a);
                a.analyze(child, true);
                return child;
            }

            @Override
            protected void done() {
                try {
                    DefaultMutableTreeNode g = get();
                    if(g != null) {
                        archiveRoot.add(g);
                        fileModel.reload(archiveRoot);
                        LOG.log(Level.INFO, "Mounted {0}", appID);
                    }
                } catch(InterruptedException | ExecutionException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }.execute();
    }

    public void quit() {
        LOG.info("Closing...");
        dispose();
    }

    private void setLastLoaded(File root) {
        jmb.reloadItem.setEnabled(root != null);
        if(( root == null ) || !root.exists()) {
            return;
        }
        lastLoaded = root;
        Main.prefs.put("lastLoaded", root.getPath());
    }

    private void loadProps(Element element) {
        propTable.clear();
        DefaultTableModel model = (DefaultTableModel) propTable.getModel();
        if(!element.getProps().isEmpty()) {
            element.validateDisplay();
            for(int i = 0; i < element.getProps().size(); i++) {
                VDFProperty entry = element.getProps().get(i);
                if("\\n".equals(entry.getKey())) {
                    continue;
                }
                model.addRow(new Object[] { entry.getKey(), entry.getValue(), entry.getInfo() });
            }
            model.fireTableDataChanged();
            propTable.repaint();
        }
    }

    private void initComponents() {
        getContentPane().add(tools = new BlendedToolBar(), BorderLayout.PAGE_START);
        JSplitPane rootSplit = new JSplitPane();
        rootSplit.setDividerLocation(180);
        rootSplit.setContinuousLayout(true);
        rootSplit.setOneTouchExpandable(true);
        rootSplit.setLeftComponent(sideSplit = new JSplitPane() {{
            setBorder(null);
            setOrientation(JSplitPane.VERTICAL_SPLIT);
            setResizeWeight(0.5);
            setContinuousLayout(true);
            setOneTouchExpandable(true);
        }});
        rootSplit.setRightComponent(tabbedContent = new JTabbedPane());
        getContentPane().add(rootSplit, BorderLayout.CENTER);
        getContentPane().add(status = new StatusBar(), BorderLayout.PAGE_END);
        tools.setWindow(this);
        tools.putClientProperty("Quaqua.ToolBar.style", "title");
        status.putClientProperty("Quaqua.ToolBar.style", "bottom");
        archiveRoot = new DefaultMutableTreeNode("Archives");
        fileSystemRoot = new DefaultMutableTreeNode("Projects");
        fileTree = new ProjectTree();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        root.add(archiveRoot);
        root.add(fileSystemRoot);
        fileModel = ( (DefaultTreeModel) fileTree.getModel() );
        fileModel.setRoot(root);
        fileModel.reload();
        sideSplit.setTopComponent(new JScrollPane(fileTree) {{
            setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        }});
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if(node == null) {
                    return;
                }
                propTable.clear();
                DefaultTableModel model = (DefaultTableModel) propTable.getModel();
                Object nodeInfo = node.getUserObject();
                // TODO: introspection
                if(nodeInfo instanceof VDFNode) {
                    Element element = Element.importVdf((VDFNode) nodeInfo);
                    loadProps(element);
                    try {
                        canvas.load(element);
                    } catch(NullPointerException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                } else if(nodeInfo instanceof VTF) {
                    VTF v = (VTF) nodeInfo;
                    for(int i = Math.max(v.getMipCount() - 8, 0); i < Math.max(v.getMipCount() - 5, v.getMipCount()); i++) {
                        try {
                            ImageIcon img = new ImageIcon(v.getImage(i));
                            model.insertRow(model.getRowCount(), new Object[] { "mip[" + i + ']', img, "" });
                        } catch(IOException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                    model.insertRow(model.getRowCount(), new Object[] { "version", v.getVersion(), "" });
                    model.insertRow(model.getRowCount(), new Object[] {
                            "headerSize", v.getHeaderSize(), ""
                    });
                    model.insertRow(model.getRowCount(), new Object[] { "width", v.getWidth(), "" });
                    model.insertRow(model.getRowCount(), new Object[] { "height", v.getHeight(), "" });
                    model.insertRow(model.getRowCount(), new Object[] { "flags", v.getFlags(), "" });
                    model.insertRow(model.getRowCount(), new Object[] {
                            "frameFirst", v.getFrameFirst(), ""
                    });
                    model.insertRow(model.getRowCount(), new Object[] {
                            "reflectivity", v.getReflectivity(), ""
                    });
                    model.insertRow(model.getRowCount(), new Object[] { "bumpScale", v.getBumpScale(), "" });
                    model.insertRow(model.getRowCount(), new Object[] { "format", v.getFormat(), "" });
                    model.insertRow(model.getRowCount(), new Object[] { "mipCount", v.getMipCount(), "" });
                    model.insertRow(model.getRowCount(), new Object[] {
                            "thumbFormat", v.getThumbFormat(), ""
                    });
                    model.insertRow(model.getRowCount(), new Object[] {
                            "thumbWidth", v.getThumbWidth(), ""
                    });
                    model.insertRow(model.getRowCount(), new Object[] {
                            "thumbHeight", v.getThumbHeight(), ""
                    });
                    model.insertRow(model.getRowCount(), new Object[] { "depth", v.getDepth(), "" });
                }
            }
        });
        sideSplit.setBottomComponent(new JScrollPane(propTable = new PropertyTable()));
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
        tabbedContent.add(Main.getString("Canvas"), new JScrollPane(canvas) {{
            //        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            getVerticalScrollBar().setBlockIncrement(30);
            getVerticalScrollBar().setUnitIncrement(20);
            //        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            getHorizontalScrollBar().setBlockIncrement(30);
            getHorizontalScrollBar().setUnitIncrement(20);
        }});
    }
}
