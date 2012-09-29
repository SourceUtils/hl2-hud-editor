package com.timepath.tf2;

import javax.swing.JTable;

/**
 *
 * @author Andrew
 */
class MyJTable extends JTable {

    MyJTable(MyJTableModel model) {
        super(model);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return (column != 0);
    }
    
}
