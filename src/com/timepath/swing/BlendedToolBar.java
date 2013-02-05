package com.timepath.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.SystemColor;
import javax.swing.JToolBar;
import javax.swing.UIManager;

/**
 *
 * @author timepath
 */
public class BlendedToolBar extends JToolBar {

    /**
     * Creates new form BlendedToolBar
     */
    public BlendedToolBar() {
        initComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color c;
        if(UIManager.getDefaults().containsKey("MenuBar.foreground")) {
            c = UIManager.getDefaults().getColor("MenuBar.foreground");
//        } else if(SystemColor.menu != null) {
//            c = SystemColor.menu;
        } else {
            super.paintComponent(g);
            return;
        }
        g.setColor(c);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setFloatable(false);
        setMinimumSize(new java.awt.Dimension(2, 16));
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}