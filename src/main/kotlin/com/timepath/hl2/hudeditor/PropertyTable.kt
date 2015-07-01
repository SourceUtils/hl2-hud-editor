package com.timepath.hl2.hudeditor


import java.awt.Component
import java.awt.Rectangle
import javax.swing.ImageIcon
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer

public class PropertyTable : JTable() {

    private val renderer: CustomTableCellRenderer
    private val model: DefaultTableModel

    init {
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        model = DefaultTableModel()
        model.addColumn(Main.getString("Key"))
        model.addColumn(Main.getString("Value"))
        model.addColumn(Main.getString("Info"))
        this.setModel(model)
        renderer = CustomTableCellRenderer()
    }

    override fun getModel(): DefaultTableModel {
        return model
    }

    public fun clear() {
        run {
            var i = model.getRowCount() - 1
            while (i >= 0) {
                model.removeRow(i)
                i--
            }
        }
        scrollRectToVisible(Rectangle(0, 0, 0, 0))
    }

    /**
     * Prevent editing of key
     */
    override fun isCellEditable(row: Int, column: Int): Boolean {
        return column != 0
    }

    override fun getCellRenderer(row: Int, column: Int): TableCellRenderer {
        return renderer
    }

    private inner class CustomTableCellRenderer : DefaultTableCellRenderer() {

        override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            if (value is ImageIcon) {
                setText("")
                setIcon(value)
                setRowHeight(row, Math.max(value.getIconHeight(), rowHeight))
            } else {
                setText(when (value) {
                    null -> ""
                    else -> value.toString()
                })
                setIcon(null)
            }
            return this
        }
    }
}
