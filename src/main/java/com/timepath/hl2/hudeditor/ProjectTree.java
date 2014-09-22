package com.timepath.hl2.hudeditor;

import com.timepath.io.utils.ViewableData;
import com.timepath.plaf.x.filechooser.BaseFileChooser;
import com.timepath.plaf.x.filechooser.NativeFileChooser;
import com.timepath.steam.io.util.ExtendedVFile;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author TimePath
 */
@SuppressWarnings("serial")
public class ProjectTree extends JTree implements ActionListener, MouseListener {

    private static final Logger LOG = Logger.getLogger(ProjectTree.class.getName());
    private ExtendedVFile directoryEntryContext;
    private ExtendedVFile archiveContext;
    private DefaultMutableTreeNode projectContext;
    private JMenuItem closeAction;
    private JPopupMenu defaultMenu;
    private JMenuItem extractAction;
    private JPopupMenu fileMenu;
    private JPopupMenu projectMenu;

    public ProjectTree() {
        initComponents();
        setRootVisible(false);
        setShowsRootHandles(true);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new CustomTreeCellRenderer());
    }

    private void initComponents() {
        defaultMenu = new JPopupMenu();
        JMenuItem nullAction = new JMenuItem();
        nullAction.setText("No action");
        nullAction.setEnabled(false);
        defaultMenu.add(nullAction);

        fileMenu = new JPopupMenu();
        extractAction = new JMenuItem();
        extractAction.setText("Extract");
        extractAction.addActionListener(this);
        fileMenu.add(extractAction);

        projectMenu = new JPopupMenu();
        closeAction = new JMenuItem();
        closeAction.setText("Close");
        closeAction.addActionListener(this);
        projectMenu.add(closeAction);

        setBorder(null);
        setModel(new DefaultTreeModel(new DefaultMutableTreeNode("root")));
        addMouseListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == extractAction) {
            extractActionActionPerformed(e);
        } else if (e.getSource() == closeAction) {
            closeActionActionPerformed(e);
        }
    }

    private void extractActionActionPerformed(ActionEvent e) {
        if (archiveContext == null) return;
        ExtendedVFile context = archiveContext;
        LOG.log(Level.INFO, "Archive: {0}", context);
        try {
            final File[] fs = new NativeFileChooser().setTitle("Extract")
                    .setDialogType(BaseFileChooser.DialogType.SAVE_DIALOG)
                    .setFileMode(BaseFileChooser.FileMode.DIRECTORIES_ONLY)
                    .choose();
            if (fs == null) return;
            LOG.log(Level.INFO, "Extracting to {0}", fs[0]);
            new SwingWorker<File, Integer>() {
                @Override
                protected File doInBackground() throws Exception {
                    File ret = null;
                    try {
                        directoryEntryContext.extract(fs[0]);
                        ret = new File(fs[0], directoryEntryContext.getName());
                    } catch (IOException e) {
                        LOG.log(Level.SEVERE, null, e);
                    }
                    return ret;
                }

                @Override
                protected void done() {
                    try {
                        LOG.log(Level.INFO, "Extracted {0}", new Object[]{get()});
                    } catch (InterruptedException | ExecutionException e) {
                        LOG.log(Level.SEVERE, null, e);
                    }
                }
            }.execute();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void closeActionActionPerformed(ActionEvent e) {
        getModel().removeNodeFromParent(projectContext);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == this) formMouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public DefaultTreeModel getModel() {
        return (DefaultTreeModel) super.getModel();
    }

    @Override
    public DefaultMutableTreeNode getLastSelectedPathComponent() {
        return (DefaultMutableTreeNode) super.getLastSelectedPathComponent();
    }

    private void formMouseClicked(MouseEvent e) {
        if (!SwingUtilities.isRightMouseButton(e)) return;
        int row = getClosestRowForLocation(e.getX(), e.getY());
        if (row != -1) {
            Object clicked = getPathForRow(row).getLastPathComponent();
            setSelectionRow(row);
            if (clicked instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) clicked;
                Object obj = node.getUserObject();
                if (obj instanceof ExtendedVFile) {
                    directoryEntryContext = (ExtendedVFile) obj;
                    archiveContext = directoryEntryContext.getRoot();
                    fileMenu.show(e.getComponent(), e.getX(), e.getY());
                    return;
                } else if (obj instanceof String) {
                    projectContext = node;
                    projectMenu.show(e.getComponent(), e.getX(), e.getY());
                    return;
                } else {
                    LOG.log(Level.WARNING, "Unknown user object {0}", obj.getClass());
                }
            } else {
                LOG.log(Level.WARNING, "Unknown tree node {0}", clicked.getClass());
            }
        }
        defaultMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private static class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

        Color sameColor = Color.BLACK;
        Color diffColor = Color.BLUE;
        Color newColor = Color.GREEN.darker();

        CustomTreeCellRenderer() {
        }

        /**
         * Configures the renderer based on the passed in components.
         * The value is set from messaging the tree with
         * {@code convertValueToText}, which ultimately invokes
         * {@code toString} on
         * {@code value}.
         * The foreground color is set based on the selection and the icon
         * is set based on on leaf and expanded.
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            String valueText = value.toString();
            if (value instanceof DefaultMutableTreeNode) {
                Object nodeValue = ((DefaultMutableTreeNode) value).getUserObject();
                if (nodeValue instanceof String) {
                    setIcon(tree, UIManager.getIcon("FileView.computerIcon"));
                } else if (nodeValue instanceof File) {
                    File f = (File) nodeValue;
                    valueText = f.getName();
                    setIcon(tree, UIManager.getIcon("FileView." + (f.isDirectory() ? "directoryIcon" : "fileIcon")));
                } else if (nodeValue instanceof ViewableData) {
                    ViewableData v = (ViewableData) nodeValue;
                    setIcon(tree, v.getIcon());
                } else {
                    if (nodeValue != null) {
                        LOG.log(Level.FINE, "Node class: {0}", nodeValue.getClass());
                    }
                    setIcon(tree, null);
                }
            }
            String stringValue = tree.convertValueToText(valueText, sel, expanded, leaf, row, hasFocus);
            this.hasFocus = hasFocus;
            setText(stringValue);
            Color tColor = null;
            if (tColor != null) {
                setForeground(sel
                        ? ((tColor != newColor) ? new Color(-tColor.getRed() + 255,
                        -tColor.getGreen() + 255,
                        -tColor.getBlue() + 255) : tColor.brighter())
                        : tColor);
            } else {
                setForeground(sel ? getTextSelectionColor() : getTextNonSelectionColor());
            }
            setEnabled(tree.isEnabled());
            setComponentOrientation(tree.getComponentOrientation());
            selected = sel;
            return this;
        }

        private void setIcon(JTree tree, Icon icon) {
            if (tree.isEnabled()) {
                setIcon(icon);
            } else {
                setDisabledIcon(icon);
            }
        }
    }
}
