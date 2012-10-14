package com.timepath.tf2.hudedit.properties;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author Andrew
 */
@SuppressWarnings("serial")
public class PropertiesTable extends JTable {
	
    public PropertiesTable() {
        super();
    }

    public PropertiesTable(TableModel model) {
        super(model);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return (column != 0);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        return super.getCellEditor(row, column);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return super.getCellRenderer(row, column);
    }

}
