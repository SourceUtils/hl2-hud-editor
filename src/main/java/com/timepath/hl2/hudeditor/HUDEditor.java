package com.timepath.hl2.hudeditor;

import com.timepath.Utils;
import com.timepath.hl2.io.RES;
import com.timepath.hl2.io.VMT;
import com.timepath.hl2.io.image.VTF;
import com.timepath.plaf.IconList;
import com.timepath.plaf.x.filechooser.BaseFileChooser;
import com.timepath.plaf.x.filechooser.NativeFileChooser;
import com.timepath.steam.io.VDF;
import com.timepath.steam.io.VDFNode;
import com.timepath.steam.io.VDFNode.VDFProperty;
import com.timepath.steam.io.storage.ACF;
import com.timepath.steam.io.storage.Files;
import com.timepath.steam.io.storage.VPK;
import com.timepath.steam.io.util.ExtendedVFile;
import com.timepath.vfs.SimpleVFile;
import com.timepath.vgui.Element;
import com.timepath.vgui.VGUIRenderer.ResourceLocator;
import com.timepath.vgui.swing.VGUICanvas;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author TimePath
 */
@SuppressWarnings("serial")
public class HUDEditor extends Application {

    private static final Logger LOG = Logger.getLogger(HUDEditor.class.getName());
    private static final Pattern VDF_PATTERN = Pattern.compile("^\\.(vdf|pop|layout|menu|styles)");
    protected EditorMenuBar editorMenuBar;
    protected VGUICanvas canvas;
    protected File lastLoaded;
    protected JSpinner spinnerWidth, spinnerHeight;
    protected HyperlinkListener linkListener = Utils.getLinkListener();

    public HUDEditor() {
        super();
        setIconImages(new IconList("/com/timepath/hl2/hudeditor/res/Icon",
                "png",
                new int[]{16, 22, 24, 32, 40, 48, 64, 128, 512, 1024}).getIcons());
        setTitle(Main.getString("Title"));
        setJMenuBar(editorMenuBar = new EditorMenuBar(this));
        String str = Main.prefs.get("lastLoaded", null);
        if (str != null) setLastLoaded(new File(str));
        new SwingWorker<Image, Void>() {
            @Override
            public Image doInBackground() {
                return BackgroundLoader.fetch();
            }

            @Override
            public void done() {
                try {
                    canvas.setBackgroundImage(get());
                } catch (InterruptedException | ExecutionException e) {
                    LOG.log(Level.SEVERE, null, e);
                }
            }
        }.execute();
        mount(440);
        GraphicsConfiguration gc = getGraphicsConfiguration();
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = getToolkit().getScreenInsets(gc);
        Dimension workspace = new Dimension(screenBounds.width - screenInsets.left - screenInsets.right,
                screenBounds.height - screenInsets.top - screenInsets.bottom);
        setMinimumSize(new Dimension(Math.max(workspace.width / 2, 640), Math.max((3 * workspace.height) / 4, 480)));
        setPreferredSize(new Dimension((int) (workspace.getWidth() / 1.5), (int) (workspace.getHeight() / 1.5)));
        pack();
        setLocationRelativeTo(null);
    }

    static void analyze(DefaultMutableTreeNode top, boolean leaves) {
        if (!(top.getUserObject() instanceof ExtendedVFile)) return;
        ExtendedVFile root = (ExtendedVFile) top.getUserObject();
        for (SimpleVFile n : root.list()) {
            LOG.log(Level.FINE, "Loading {0}", n.getName());
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(n);
            if (n.isDirectory()) {
                if (n.list().size() > 0) {
                    top.add(child);
                    analyze(child, leaves);
                }
            } else if (leaves) {
                try (InputStream is = n.openStream()) {
                    if (VDF_PATTERN.matcher(n.getName()).matches()) {
                        child.add(VDF.load(is).toTreeNode());
                    } else if (n.getName().endsWith(".res")) {
                        child.add(RES.load(is).toTreeNode());
                    } else {
                        continue;
                    }
                    top.add(child);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, null, e);
                }
            }
        }
    }

    static void recurseDirectoryToNode(ExtendedVFile ar, DefaultMutableTreeNode project) {
        project.setUserObject(ar);
        analyze(project, true);
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = fileTree.getLastSelectedPathComponent();
                if (node == null) return;
                propTable.clear();
                DefaultTableModel model = propTable.getModel();
                Object nodeInfo = node.getUserObject();
                // TODO: introspection
                if (nodeInfo instanceof VDFNode) {
                    Element element = Element.importVdf((VDFNode) nodeInfo);
                    element.setFile(node.getParent().toString()); // TODO
                    loadProps(element);
                    canvas.load(element);
                }
            }
        });
        canvas = new VGUICanvas() {
            @Override
            public void placed() {
                DefaultMutableTreeNode node = fileTree.getLastSelectedPathComponent();
                if (node == null) return;
                Object nodeInfo = node.getUserObject();
                if (nodeInfo instanceof Element) {
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

    @Override
    public void preferences() {
        info("No app-specific preferences yet", "Preferences");
    }

    @Override
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

    @Override
    public void fileDropped(File f) {
        loadAsync(f);
    }

    @Override
    public Image getDockIconImage() {
        URL url = getClass().getResource("/com/timepath/hl2/hudeditor/res/Icon.png");
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    void locateHudDirectory() {
        try {
            File[] selection = new NativeFileChooser().setParent(this)
                    .setTitle(Main.getString("LoadHudDir"))
                    .setDirectory(lastLoaded)
                    .setFileMode(BaseFileChooser.FileMode.DIRECTORIES_ONLY)
                    .choose();
            if (selection != null) loadAsync(selection[0]);
        } catch (IOException ex) {
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
                    if (project == null) return;
                    LOG.log(Level.INFO, "Loaded hud - took {0}ms", System.currentTimeMillis() - start);
                    fileSystemRoot.add(project);
                    fileTree.expandPath(new TreePath(project.getPath()));
                    fileTree.setSelectionRow(fileSystemRoot.getIndex(project));
                    fileTree.requestFocusInWindow();
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, null, t);
                }
            }
        }.execute();
    }

    DefaultMutableTreeNode load(File root) {
        if (root == null) return null;
        if (!root.exists()) {
            error(new MessageFormat(Main.getString("FileAccessError")).format(new Object[]{root}));
        }
        setLastLoaded(root);
        LOG.log(Level.INFO, "You have selected: {0}", root.getAbsolutePath());
        if (root.isDirectory()) {
            File[] folders = root.listFiles();
            boolean valid = true; // TODO: find resource and scripts if there is a parent directory
            for (File folder : folders != null ? folders : new File[0]) {
                if (folder.isDirectory() &&
                        ("resource".equalsIgnoreCase(folder.getName()) || "scripts".equalsIgnoreCase(folder.getName()))) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                error("Selection not valid. Please choose a folder containing \'resources\' or \'scripts\'.");
                locateHudDirectory();
                return null;
            }
            DefaultMutableTreeNode project = new DefaultMutableTreeNode(root.getName());
            recurseDirectoryToNode(new Files(root), project);
            return project;
        }
        if (root.getName().endsWith("_dir.vpk")) {
            DefaultMutableTreeNode project = new DefaultMutableTreeNode(root.getName());
            recurseDirectoryToNode(VPK.loadArchive(root), project);
            return project;
        }
        return null;
    }

    void changeResolution() {
        spinnerWidth.setEnabled(false);
        spinnerHeight.setEnabled(false);
        final JComboBox<String> dropDown = new JComboBox<>();
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Collection<String> listItems = new LinkedList<>();
        for (GraphicsDevice device : env.getScreenDevices()) {
            for (DisplayMode resolution : device.getDisplayModes()) { // TF2 has different resolutions
                String item = resolution.getWidth() + "x" + resolution.getHeight(); // TODO: Work out aspect ratios
                if (!listItems.contains(item)) {
                    listItems.add(item);
                }
            }
        }
        dropDown.addItem("Custom");
        for (String listItem : listItems) {
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
                if (isRes) {
                    String[] xy = item.split("x");
                    spinnerWidth.setValue(Integer.parseInt(xy[0]));
                    spinnerHeight.setValue(Integer.parseInt(xy[1]));
                }
            }
        });
        Object[] message = {
                "Presets: ", dropDown, "Width: ", spinnerWidth, "Height: ", spinnerHeight
        };
        JOptionPane optionPane = new JOptionPane(message,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null);
        JDialog dialog = optionPane.createDialog(this, "Change resolution...");
        dialog.setContentPane(optionPane);
        dialog.pack();
        dialog.setVisible(true);
        if (optionPane.getValue() != null) {
            int value = ((Number) optionPane.getValue()).intValue();
            if (value == JOptionPane.YES_OPTION) {
                canvas.setPreferredSize(new Dimension(Integer.parseInt(spinnerWidth.getValue().toString()),
                        Integer.parseInt(spinnerHeight.getValue().toString())));
            }
        }
    }

    void mount(final int appID) {
        new SwingWorker<DefaultMutableTreeNode, Void>() {
            @Override
            protected DefaultMutableTreeNode doInBackground() throws Exception {
                LOG.log(Level.INFO, "Mounting {0}", appID);
                final ExtendedVFile a = ACF.fromManifest(appID);
                canvas.getR().registerLocator(new ResourceLocator() {
                    @Override
                    public InputStream locate(String path) {
                        path = path.replace('\\', '/').toLowerCase();
                        if (path.startsWith("..")) path = "vgui/" + path;
                        System.out.println("Looking for " + path);
                        SimpleVFile file = a.query("tf/materials/" + path);
                        if (file == null) return null;
                        return file.openStream();
                    }

                    @Override
                    public Image locateImage(String name) {
                        String vtfName = name;
                        if (!name.endsWith(".vtf")) { // It could be a vmt
                            vtfName += ".vtf";
                            InputStream vmtStream = locate(name + ".vmt");
                            if (vmtStream != null) {
                                try {
                                    VMT.VMTNode vmt = VMT.load(vmtStream);
                                    String next = (String) vmt.root.getValue("$baseTexture");
                                    if (!next.equals(name)) return locateImage(next); // Stop recursion
                                } catch (IOException e) {
                                    LOG.log(Level.SEVERE, null, e);
                                }
                            }
                        }
                        // It's a vtf
                        InputStream vtfStream = locate(vtfName);
                        if (vtfStream != null) {
                            try {
                                VTF vtf = VTF.load(vtfStream);
                                if (vtf == null) return null;
                                return vtf.getImage(0);
                            } catch (IOException e) {
                                LOG.log(Level.SEVERE, null, e);
                            }
                        }
                        return null;
                    }
                });
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(a);
                recurseDirectoryToNode(a, child);
                return child;
            }

            @Override
            protected void done() {
                try {
                    DefaultMutableTreeNode g = get();
                    if (g != null) {
                        archiveRoot.add(g);
                        fileModel.reload(archiveRoot);
                        LOG.log(Level.INFO, "Mounted {0}", appID);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }.execute();
    }

    void setLastLoaded(File root) {
        editorMenuBar.reloadItem.setEnabled(root != null);
        if ((root == null) || !root.exists()) return;
        lastLoaded = root;
        Main.prefs.put("lastLoaded", root.getPath());
    }

    void loadProps(Element element) {
        propTable.clear();
        DefaultTableModel model = propTable.getModel();
        if (!element.getProps().isEmpty()) {
            element.validateDisplay();
            for (int i = 0; i < element.getProps().size(); i++) {
                VDFProperty entry = element.getProps().get(i);
                if ("\\n".equals(entry.getKey())) continue;
                model.addRow(new Object[]{entry.getKey(), entry.getValue(), entry.getInfo()});
            }
            model.fireTableDataChanged();
            propTable.repaint();
        }
    }
}
