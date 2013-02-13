package com.timepath.tf2.hudeditor.gui;

import com.timepath.plaf.x.NativeFileChooser;
import com.timepath.tf2.hudeditor.element.Element;
import com.timepath.tf2.io.GCF;
import com.timepath.tf2.io.GCF.DirectoryEntry;
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
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new CustomTreeCellRenderer());
    }

    private DirectoryEntry directoryEntryContext;

    private GCF gcfContext;

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
                    tColor = sameColor;
                    setIcons(tree, UIManager.getIcon("FileView.computerIcon"));
                } else if(nodeValue instanceof File) { // this will either be an actual file on the system (directories included), or an element within a file
                    tColor = diffColor;
                    File f = ((File) nodeValue);
                    valueText = f.getName();
                    if(f.isDirectory()) {
                        setIcons(tree, UIManager.getIcon("FileView.directoryIcon"));
                    } else {
                        setIcons(tree, UIManager.getIcon("FileView.fileIcon"));
                    }
                } else if(nodeValue instanceof Element) {
                    tColor = newColor;
                    Element e = (Element) nodeValue;
                    if(e.getProps().isEmpty() && leaf) { // If no properties, warn because irrelevant. Only care if leaves are empty
                        setIcons(tree, UIManager.getIcon("FileChooser.detailsViewIcon"));
                    } else {
                        setIcons(tree, UIManager.getIcon("FileChooser.listViewIcon"));
                    }
                } else if(nodeValue instanceof GCF) {
                    Icon i = UIManager.getIcon("FileView.hardDriveIcon");
                    if(i == null) {
                        i = UIManager.getIcon("FileView.directoryIcon");
                    }
                    setIcons(tree, i);
                } else if(nodeValue instanceof DirectoryEntry) {
                    DirectoryEntry d = (DirectoryEntry) nodeValue;
                    if(d.attributes == 0) {
                        setIcons(tree, UIManager.getIcon("FileView.directoryIcon"));
                    } else {
                        setIcons(tree, UIManager.getIcon("FileView.fileIcon"));
                    }
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

        jMenuItem1.setText("No action");
        jMenuItem1.setEnabled(false);
        popupMenu.add(jMenuItem1);

        jMenuItem2.setLabel("Extract");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        popupMenuGCF.add(jMenuItem2);

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
            Object clicked = this.getPathForRow(row).getLastPathComponent();
            this.setSelectionRow(row);
            if(clicked instanceof DefaultMutableTreeNode) {
                Object obj = ((DefaultMutableTreeNode) clicked).getUserObject();
                if(obj instanceof GCF) {
                    gcfContext = (GCF) obj;
                    directoryEntryContext = null;
                    popupMenuGCF.show(evt.getComponent(), evt.getX(), evt.getY());
                } else if(obj instanceof DirectoryEntry) {
                    directoryEntryContext = (DirectoryEntry) obj;
                    gcfContext = directoryEntryContext.getGCF();
                    popupMenuGCF.show(evt.getComponent(), evt.getX(), evt.getY());
                    return;
                } else {
                    LOG.log(Level.WARNING, "Unknown user object {0}", obj.getClass());
                }
            } else {
                LOG.log(Level.WARNING, "Unknown tree node {0}", clicked.getClass());
            }
            popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_formMouseClicked

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        if(gcfContext != null) {
            LOG.log(Level.INFO, "GCF: {0}", gcfContext);
            if(directoryEntryContext != null) {
                LOG.log(Level.INFO, "DirectoryEntry: {0}", directoryEntryContext);

                new Thread() {
                    @Override
                    public void run() {
                        File f = new NativeFileChooser(null, "extract", new File("")).getFolder();
                        if(f != null) {
                            LOG.log(Level.INFO, "Extracting to {0}", f);
                            try {
                                File ret = gcfContext.extract(directoryEntryContext.index, f);
                                LOG.log(Level.INFO, "Extracted {0}", new Object[]{ret});
                            } catch(IOException ex) {
                                Logger.getLogger(FileTree.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }.start();
            }
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JPopupMenu popupMenuGCF;
    // End of variables declaration//GEN-END:variables

    private static final Logger LOG = Logger.getLogger(FileTree.class.getName());

}
