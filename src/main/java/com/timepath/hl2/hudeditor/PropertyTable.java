package com.timepath.hl2.hudeditor;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author TimePath
 */
@SuppressWarnings("serial")
public class PropertyTable extends JTable {

    @NotNull
    private final CustomTableCellRenderer renderer;
    @NotNull
    private final DefaultTableModel model;

    public PropertyTable() {
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        model = new DefaultTableModel();
        model.addColumn(Main.getString("Key"));
        model.addColumn(Main.getString("Value"));
        model.addColumn(Main.getString("Info"));
        this.setModel(model);
        renderer = new CustomTableCellRenderer();
    }

    @NotNull
    public DefaultTableModel getModel() {
        return model;
    }

    public void clear() {
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        scrollRectToVisible(new Rectangle(0, 0, 0, 0));
    }

    /**
     * Prevent editing of key
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0;
    }

    @NotNull
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return renderer;
    }

    private class CustomTableCellRenderer extends DefaultTableCellRenderer {

        @NotNull
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof ImageIcon) {
                @NotNull Icon icon = (Icon) value;
                setText("");
                setIcon(icon);
                setRowHeight(row, Math.max(icon.getIconHeight(), rowHeight));
            } else {
                setText((value == null) ? "" : value.toString());
                setIcon(null);
            }
            return this;
        }
    }
}
