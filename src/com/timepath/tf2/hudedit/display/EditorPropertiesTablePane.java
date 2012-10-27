package com.timepath.tf2.hudedit.display;


import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class EditorPropertiesTablePane extends JScrollPane {
    
    private final EditorPropertiesTable propTable;
    
    public EditorPropertiesTablePane() {
        super();
        this.setPreferredSize(new Dimension(400, 400));
        propTable = new EditorPropertiesTable();
        this.setViewportView(propTable);
    }
    
    public EditorPropertiesTable getPropTable() {
        return propTable;
    }
        
    public class EditorPropertiesTable extends JTable {

        public EditorPropertiesTable() {
            super();
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Key");
            model.addColumn("Value");
            model.addColumn("Info");

            model.insertRow(0, new String[]{"", "", ""});

            this.setModel(model);
            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            this.setColumnSelectionAllowed(false);
            this.setRowSelectionAllowed(true);
            this.getTableHeader().setReorderingAllowed(false);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return (column != 0); // deny editing of key
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
        
}