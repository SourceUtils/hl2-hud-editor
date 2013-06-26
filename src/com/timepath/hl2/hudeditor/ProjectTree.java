package com.timepath.hl2.hudeditor;

import com.timepath.backports.javax.swing.SwingWorker;
import com.timepath.io.utils.ViewableData;
import com.timepath.plaf.x.filechooser.BaseFileChooser;
import com.timepath.plaf.x.filechooser.NativeFileChooser;
import com.timepath.steam.io.storage.GCF;
import com.timepath.steam.io.storage.GCF.GCFDirectoryEntry;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author timepath
 */
@SuppressWarnings("serial")
public class ProjectTree extends javax.swing.JTree implements ActionListener, MouseListener {

    public ProjectTree() {
        initComponents();
        this.setRootVisible(false);
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.setCellRenderer(new CustomTreeCellRenderer());
    }

    private GCFDirectoryEntry directoryEntryContext;

    private GCF gcfContext;

    private DefaultMutableTreeNode projectContext;

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

        Color sameColor = Color.BLACK;

        Color diffColor = Color.BLUE;

        Color newColor = Color.GREEN.darker();

        /**
         * Configures the renderer based on the passed in components.
         * The value is set from messaging the tree with
         * <code>convertValueToText</code>, which ultimately invokes
         * <code>toString</code> on
         * <code>value</code>.
         * The foreground color is set based on the selection and the icon
         * is set based on on leaf and expanded.
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            String valueText = value.toString();

            Color tColor = null;

            if(value instanceof DefaultMutableTreeNode) {
                Object nodeValue = ((DefaultMutableTreeNode) value).getUserObject();
                if(nodeValue instanceof String) {
                    setIcons(tree, UIManager.getIcon("FileView.computerIcon"));
                } else if(nodeValue instanceof File) {
                    File f = ((File) nodeValue);
                    valueText = f.getName();
                    if(f.isDirectory()) {
                        setIcons(tree, UIManager.getIcon("FileView.directoryIcon"));
                    } else {
                        setIcons(tree, UIManager.getIcon("FileView.fileIcon"));
                    }
                } else if(nodeValue instanceof ViewableData) {
                    ViewableData v = (ViewableData) nodeValue;
                    setIcons(tree, v.getIcon());
                } else {
                    if(nodeValue != null) {
                        LOG.log(Level.FINE, "Node class: {0}", nodeValue.getClass());
                    }
                    setIcons(tree, null);
                }
            }
            String stringValue = tree.convertValueToText(valueText, sel, expanded, leaf, row,
                                                         hasFocus);
            this.hasFocus = hasFocus;
            this.setText(stringValue);
            if(tColor != null) {
                this.setForeground(sel ? (tColor != newColor ? new Color(-tColor.getRed() + 255,
                                                                         -tColor.getGreen() + 255,
                                                                         -tColor.getBlue() + 255) : tColor.brighter()) : tColor);
            } else {
                this.setForeground(sel ? getTextSelectionColor() : getTextNonSelectionColor());
            }
            this.setEnabled(tree.isEnabled());
            this.setComponentOrientation(tree.getComponentOrientation());
            this.selected = sel;
            return this;
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        defaultMenu = new JPopupMenu();
        nullAction = new JMenuItem();
        fileMenu = new JPopupMenu();
        extractAction = new JMenuItem();
        projectMenu = new JPopupMenu();
        closeAction = new JMenuItem();

        nullAction.setText("No action");
        nullAction.setEnabled(false);
        defaultMenu.add(nullAction);

        extractAction.setText("Extract");
        extractAction.addActionListener(this);
        fileMenu.add(extractAction);

        closeAction.setText("Close");
        closeAction.addActionListener(this);
        projectMenu.add(closeAction);

        setBorder(null);
        DefaultMutableTreeNode treeNode1 = new DefaultMutableTreeNode("root");
        setModel(new DefaultTreeModel(treeNode1));
        addMouseListener(this);
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == extractAction) {
            ProjectTree.this.extractActionActionPerformed(evt);
        }
        else if (evt.getSource() == closeAction) {
            ProjectTree.this.closeActionActionPerformed(evt);
        }
    }

    public void mouseClicked(MouseEvent evt) {
        if (evt.getSource() == ProjectTree.this) {
            ProjectTree.this.formMouseClicked(evt);
        }
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    public void mousePressed(MouseEvent evt) {
    }

    public void mouseReleased(MouseEvent evt) {
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        if(SwingUtilities.isRightMouseButton(evt)) {
            int row = this.getClosestRowForLocation(evt.getX(), evt.getY());
            if(row != -1) {
                Object clicked = this.getPathForRow(row).getLastPathComponent();
                this.setSelectionRow(row);
                if(clicked instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = ((DefaultMutableTreeNode) clicked);
                    Object obj = node.getUserObject();
                    if(obj instanceof GCF) {
                        gcfContext = (GCF) obj;
                        directoryEntryContext = null;
                        fileMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                        return;
                    } else if(obj instanceof GCFDirectoryEntry) {
                        directoryEntryContext = (GCFDirectoryEntry) obj;
                        gcfContext = directoryEntryContext.getArchive();
                        fileMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                        return;
                    } else if(obj instanceof String) {
                        projectContext = node;
                        projectMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                        return;
                    } else if(obj instanceof File) {
                    } else {
                        LOG.log(Level.WARNING, "Unknown user object {0}", obj.getClass());
                    }
                } else {
                    LOG.log(Level.WARNING, "Unknown tree node {0}", clicked.getClass());
                }
            }
            defaultMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_formMouseClicked

    private void extractActionActionPerformed(ActionEvent evt) {//GEN-FIRST:event_extractActionActionPerformed
        if(gcfContext == null) {
            return;
        }
        final GCF context = gcfContext;
        LOG.log(Level.INFO, "GCF: {0}", context);
        final int index;
        if(directoryEntryContext == null) {
            index = 0;
        } else {
            index = directoryEntryContext.index;
            LOG.log(Level.INFO, "DirectoryEntry: {0}", directoryEntryContext);
        }
        try {
            final File[] fs = new NativeFileChooser().setTitle("Extract").setDialogType(
                    BaseFileChooser.DialogType.SAVE_DIALOG).setFileMode(
                    BaseFileChooser.FileMode.DIRECTORIES_ONLY).choose();
            if(fs == null) {
                return;
            }
            LOG.log(Level.INFO, "Extracting to {0}", fs[0]);
            new SwingWorker<File, Integer>() {
                @Override
                protected File doInBackground() throws Exception {
                    File ret = null;

                    try {
                        ret = context.extract(index, fs[0]);
                    } catch(IOException ex) {
                        Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    return ret;
                }

                @Override
                protected void done() {
                    try {
                        LOG.log(Level.INFO, "Extracted {0}", new Object[] {get()});
                    } catch(InterruptedException ex) {
                        Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, ex);
                    } catch(ExecutionException ex) {
                        Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }.execute();

        } catch(IOException ex) {
            Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_extractActionActionPerformed

    private void closeActionActionPerformed(ActionEvent evt) {//GEN-FIRST:event_closeActionActionPerformed
        ((DefaultTreeModel) this.treeModel).removeNodeFromParent(projectContext);
    }//GEN-LAST:event_closeActionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JMenuItem closeAction;
    private JPopupMenu defaultMenu;
    private JMenuItem extractAction;
    private JPopupMenu fileMenu;
    private JMenuItem nullAction;
    private JPopupMenu projectMenu;
    // End of variables declaration//GEN-END:variables

    private static final Logger LOG = Logger.getLogger(ProjectTree.class.getName());

}
