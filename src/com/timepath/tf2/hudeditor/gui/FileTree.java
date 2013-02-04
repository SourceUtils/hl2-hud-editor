/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.timepath.tf2.hudeditor.gui;

import com.timepath.tf2.hudeditor.util.Element;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author timepath
 */
public class FileTree extends javax.swing.JTree {

    public FileTree(TreeNode root) {
        super(root);
        initComponents();
        setShowsRootHandles(true);
        setSelectionRow(0);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new CustomTreeCellRenderer());
//        fileSystem.addTreeSelectionListener(new TreeSelectionListener() {
//            @Override
//            public void valueChanged(TreeSelectionEvent e) {
//                DefaultTableModel model = (DefaultTableModel) propTable.getModel();
//                model.getDataVector().removeAllElements();
//                model.insertRow(0, new String[]{"", "", ""});
//                propTable.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
//
//                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileSystem.getLastSelectedPathComponent();
//                if(node == null) {
//                    return;
//                }
//
//                Object nodeInfo = node.getUserObject();
//                if(nodeInfo instanceof Element) {
//                    Element element = (Element) nodeInfo;
//                    canvas.load(element);
//                    if(!element.getProps().isEmpty()) {
//                        model.getDataVector().removeAllElements();
//                        element.validateDisplay();
//                        for(int i = 0; i < element.getProps().size(); i++) {
//                            Property entry = element.getProps().get(i);
//                            if(entry.getKey().equals("\\n")) {
//                                continue;
//                            }
//                            model.insertRow(model.getRowCount(), new Object[] {entry.getKey(), entry.getValue(), entry.getInfo()});
//                        }
//                    }
//                }
//            }
//
//        });
    }
    
    private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;

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

        JFileChooser iconFinder = new JFileChooser();
        Color sameColor = Color.BLACK;
        Color diffColor = Color.BLUE;
        Color newColor = Color.GREEN.darker(); 

        /**
          * Configures the renderer based on the passed in components.
          * The value is set from messaging the tree with
          * <code>convertValueToText</code>, which ultimately invokes
          * <code>toString</code> on <code>value</code>.
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
                    setIcons(tree, iconFinder.getIcon(f));
                } else if(nodeValue instanceof Element) {
                    tColor = newColor;
                    Element e = (Element) nodeValue;
                    if(e.getProps().isEmpty() && leaf) { // If no properties, warn because irrelevant. Only care if leaves are empty
                        setIcons(tree, UIManager.getIcon("FileChooser.detailsViewIcon"));
                    } else {
                        setIcons(tree, UIManager.getIcon("FileChooser.listViewIcon"));
                    }
                } else {
                    if(nodeValue != null) {
                        System.out.println(nodeValue.getClass());
                    }
                    setIcons(tree, null);
                }
            }
            String stringValue = tree.convertValueToText(valueText, sel, expanded, leaf, row, hasFocus);
            this.hasFocus = hasFocus;
            this.setText(stringValue);
            if(tColor != null) {
                this.setForeground(sel ? tColor != newColor ? new Color(-tColor.getRed() + 255, -tColor.getGreen() + 255, -tColor.getBlue() + 255) : tColor.brighter() : tColor);
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
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
