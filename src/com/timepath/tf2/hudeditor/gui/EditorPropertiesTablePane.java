package com.timepath.tf2.hudeditor.gui;


import java.awt.Dimension;
import java.util.logging.Logger;
import javax.swing.JScrollPane;

public class EditorPropertiesTablePane extends JScrollPane {
    
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
    private static final Logger logger = Logger.getLogger(EditorPropertiesTablePane.class.getName());
        
}