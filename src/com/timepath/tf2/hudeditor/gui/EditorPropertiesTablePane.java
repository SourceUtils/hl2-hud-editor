package com.timepath.tf2.hudeditor.gui;


import java.awt.Dimension;
import java.util.logging.Logger;
import javax.swing.JScrollPane;

public class EditorPropertiesTablePane extends JScrollPane {
    private static final long serialVersionUID = 1L;
    
    private final EditorPropertiesTable propTable;
    
    public EditorPropertiesTablePane() {
        super();
        this.setPreferredSize(new Dimension(400, 400));
        propTable = new EditorPropertiesTable(this);
        this.setViewportView(propTable);
    }
    
    public EditorPropertiesTable getPropTable() {
        return propTable;
    }
    private static final Logger LOG = Logger.getLogger(EditorPropertiesTablePane.class.getName());
        
}