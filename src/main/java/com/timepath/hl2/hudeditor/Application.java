package com.timepath.hl2.hudeditor;

import com.apple.OSXAdapter;
import com.timepath.plaf.OS;
import com.timepath.plaf.mac.Application.*;
import com.timepath.swing.BlendedToolBar;
import com.timepath.swing.StatusBar;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author TimePath
 */
@SuppressWarnings("serial")
public abstract class Application extends JFrame {

    private static final Logger LOG = Logger.getLogger(Application.class.getName());
    protected DefaultMutableTreeNode fileSystemRoot, archiveRoot;
    protected ProjectTree      fileTree;
    protected PropertyTable    propTable;
    protected JSplitPane       sideSplit;
    protected StatusBar        status;
    protected JTabbedPane      tabbedContent;
    protected BlendedToolBar   tools;
    protected DefaultTreeModel fileModel;

    public Application() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
        getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE); // Mac tweak
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
                            if(token.startsWith("#") || token.isEmpty()) continue; // comment line, by RFC 2483
                            try {
                                file = new File(new URI(token));
                            } catch(URISyntaxException e) { return; }
                        }
                    } else {
                        Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
                        if(data instanceof Iterable) {
                            for(Object o : (Iterable<?>) data) {
                                if(o instanceof File) file = (File) o;
                            }
                        }
                    }
                    if(file != null) fileDropped(file);
                } catch(ClassNotFoundException | InvalidDnDOperationException | UnsupportedFlavorException |
                        IOException e) {
                    LOG.log(Level.SEVERE, null, e);
                } finally {
                    dtde.dropComplete(true);
                    repaint();
                }
            }
        });
        initComponents();
    }

    public abstract void preferences();

    public abstract void about();

    public abstract Image getDockIconImage();

    public abstract void fileDropped(File f);

    @Override
    public void setJMenuBar(JMenuBar menubar) {
        LOG.log(Level.INFO, "Setting menubar for {0}", OS.get());
        super.setJMenuBar(menubar);
        if(OS.isMac()) {
            try {
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
                OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[]) null));
                com.timepath.plaf.mac.Application app = com.timepath.plaf.mac.Application.getApplication();
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
                app.setDockIconImage(getDockIconImage());
            } catch(Exception e) {
                LOG.severe(e.toString());
            }
        }
    }

    public void quit() {
        LOG.info("Closing...");
        dispose();
    }

    protected void initComponents() {
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
        fileModel = fileTree.getModel();
        fileModel.setRoot(root);
        fileModel.reload();
        sideSplit.setTopComponent(new JScrollPane(fileTree) {{
            setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        }});
        sideSplit.setBottomComponent(new JScrollPane(propTable = new PropertyTable()));
    }

    public void error(Object msg) { error(msg, Main.getString("Error")); }

    public void error(Object msg, String title) {
        LOG.log(Level.SEVERE, "{0}:{1}", new Object[] { title, msg });
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    public void info(Object msg) { info(msg, Main.getString("Info")); }

    public void info(Object msg, String title) {
        LOG.log(Level.INFO, "{0}:{1}", new Object[] { title, msg });
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

}
