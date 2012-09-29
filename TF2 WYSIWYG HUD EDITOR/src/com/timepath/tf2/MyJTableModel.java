package com.timepath.tf2;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Andrew
 */
public class MyJTableModel extends DefaultTableModel {

    @Override
    public Class<?> getColumnClass(int columnIndex) {
//        System.out.println(super.getColumnClass(columnIndex));
        return super.getColumnClass(columnIndex);
    }

}
