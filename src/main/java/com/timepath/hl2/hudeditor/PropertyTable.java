package com.timepath.hl2.hudeditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.logging.Logger;

/**
 * @author TimePath
 */
@SuppressWarnings("serial")
public class PropertyTable extends JTable {

    private static final Logger LOG = Logger.getLogger(PropertyTable.class.getName());
    private final CustomTableCellRenderer renderer;

    /**
     * Creates new form PropertyTable
     */
    public PropertyTable() {
        initComponents();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn(Main.getString("Key"));
        model.addColumn(Main.getString("Value"));
        model.addColumn(Main.getString("Info"));
        setModel(model);
        renderer = new CustomTableCellRenderer();
    }

    private void initComponents() {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void clear() {
        DefaultTableModel model = (DefaultTableModel) getModel();
        for(int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        scrollRectToVisible(new Rectangle(0, 0, 0, 0));
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0; // deny editing of key
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return renderer;
    }

    private class CustomTableCellRenderer extends DefaultTableCellRenderer {

        private CustomTableCellRenderer() {}

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Icon icon = null;
            if(value instanceof ImageIcon) {
                icon = (Icon) value;
            }
            if(icon == null) {
                setText(( value == null ) ? "" : value.toString());
                setIcon(null);
            } else {
                setText("");
                setIcon(icon);
                setRowHeight(row, Math.max(icon.getIconHeight(), rowHeight));
            }
            return this;
        }
    }
}
