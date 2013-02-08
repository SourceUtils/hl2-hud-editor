package com.timepath.swing;

import java.awt.Graphics;
import java.util.logging.Logger;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 *
 * @author timepath
 */
@SuppressWarnings("serial")
public final class BlendedToolBar extends JToolBar {

    /**
     * Creates new form BlendedToolBar
     */
    public BlendedToolBar() {
        initComponents();
        mb = new JMenuBar();
        this.add(mb);
        mb.setVisible(false);
    }

    private final JMenuBar mb;

    @Override
    protected void paintComponent(Graphics g) {
        this.setForeground(mb.getForeground());
        this.setBackground(mb.getBackground());

        g.setColor(this.getBackground());
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
        setPreferredSize(new java.awt.Dimension(2, 16));
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    private static final Logger LOG = Logger.getLogger(BlendedToolBar.class.getName());
}