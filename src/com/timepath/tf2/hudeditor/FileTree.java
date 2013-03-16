package com.timepath.tf2.hudeditor;

import com.timepath.plaf.x.filechooser.NativeFileChooser;
import com.timepath.steam.io.GCF;
import com.timepath.steam.io.GCF.DirectoryEntry;
import com.timepath.hl2.io.util.ViewableData;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author timepath
 */
@SuppressWarnings("serial")
public class FileTree extends javax.swing.JTree {

    public FileTree(TreeNode root) {
        super(root);
        initComponents();
        this.setRootVisible(false);
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.setCellRenderer(new CustomTreeCellRenderer());
    }

    private DirectoryEntry directoryEntryContext;

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
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
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
            String stringValue = tree.convertValueToText(valueText, sel, expanded, leaf, row, hasFocus);
            this.hasFocus = hasFocus;
            this.setText(stringValue);
            if(tColor != null) {
                this.setForeground(sel ? (tColor != newColor ? new Color(-tColor.getRed() + 255, -tColor.getGreen() + 255, -tColor.getBlue() + 255) : tColor.brighter()) : tColor);
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

        popupMenu = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        popupMenuGCF = new javax.swing.JPopupMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        popupMenuProject = new javax.swing.JPopupMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        jMenuItem1.setText("No action");
        jMenuItem1.setEnabled(false);
        popupMenu.add(jMenuItem1);

        jMenuItem2.setText("Extract");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        popupMenuGCF.add(jMenuItem2);

        jMenuItem3.setText("Close");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        popupMenuProject.add(jMenuItem3);

        setBorder(null);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
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
                    popupMenuGCF.show(evt.getComponent(), evt.getX(), evt.getY());
                    return;
                } else if(obj instanceof DirectoryEntry) {
                    directoryEntryContext = (DirectoryEntry) obj;
                    gcfContext = directoryEntryContext.getGCF();
                    popupMenuGCF.show(evt.getComponent(), evt.getX(), evt.getY());
                    return;
                } else if(obj instanceof String) {
                    projectContext = node;
                    popupMenuProject.show(evt.getComponent(), evt.getX(), evt.getY());
                    return;
                } else if(obj instanceof File) {
                } else {
                    LOG.log(Level.WARNING, "Unknown user object {0}", obj.getClass());
                }
            } else {
                LOG.log(Level.WARNING, "Unknown tree node {0}", clicked.getClass());
            }
            }
            popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_formMouseClicked

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        new Thread() {
            @Override
            public void run() {
                if(gcfContext != null) {
                    LOG.log(Level.INFO, "GCF: {0}", gcfContext);
                    int index = 0;
                    if(directoryEntryContext != null) {
                        LOG.log(Level.INFO, "DirectoryEntry: {0}", directoryEntryContext);
                        index = directoryEntryContext.index;
                    }
                    File f = new NativeFileChooser(null, "extract", new File(new File("").getPath())).choose(true, true);
                    if(f != null) {
                        LOG.log(Level.INFO, "Extracting to {0}", f);
                        try {
                            File ret = gcfContext.extract(index, f);
                            LOG.log(Level.INFO, "Extracted {0}", new Object[]{ret});
                        } catch(IOException ex) {
                            Logger.getLogger(FileTree.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }.start();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed

        ((DefaultTreeModel)this.treeModel).removeNodeFromParent(projectContext);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JPopupMenu popupMenuGCF;
    private javax.swing.JPopupMenu popupMenuProject;
    // End of variables declaration//GEN-END:variables

    private static final Logger LOG = Logger.getLogger(FileTree.class.getName());

}