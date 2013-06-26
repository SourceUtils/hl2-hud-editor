package com.timepath.hl2.hudeditor;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author timepath
 */
@SuppressWarnings("serial")
public class PropertyTable extends JTable {

    private static final Logger LOG = Logger.getLogger(PropertyTable.class.getName());

    /**
     * Creates new form PropertyTable
     */
    public PropertyTable() {
        initComponents();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn(Main.getString("Key"));
        model.addColumn(Main.getString("Value"));
        model.addColumn(Main.getString("Info"));
        this.setModel(model);
        this.renderer = new CustomTableCellRenderer();
    }

    public void clear() {
        DefaultTableModel model = (DefaultTableModel) this.getModel();
        for(int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        this.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
    }

    private final CustomTableCellRenderer renderer;

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0; // deny editing of key
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        return super.getCellEditor(row, column);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return renderer;
    }

    /**
     *
     */
    public class CustomTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Icon icon = null;
            if(value instanceof ImageIcon) {
                icon = (ImageIcon) value;
            }
            if(icon == null) {
                this.setText((value == null) ? "" : value.toString());
                this.setIcon(null);
            } else {
                this.setText("");
                this.setIcon(icon);
                PropertyTable.this.setRowHeight(row, Math.max(icon.getIconHeight(),
                                                              PropertyTable.this.rowHeight));
            }
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

        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
