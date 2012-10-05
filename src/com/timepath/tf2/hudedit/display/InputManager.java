package com.timepath.tf2.hudedit.display;

import com.timepath.tf2.hudedit.util.Element;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

/**
 *
 * @author andrew
 */
class InputManager implements MouseListener, MouseMotionListener, MouseWheelListener {

    private boolean isDragSelecting;

    private boolean isDragMoving;

    HudCanvas canvas;

    private Point dragStart;

    InputManager(final HudCanvas canvas, HudCanvas outer) {
        this.canvas = outer;
    }

    void init() {
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        //                canvas.addMouseWheelListener(this);
    }

    //<editor-fold defaultstate="collapsed" desc="For later use">
    @Override
    public void mouseEntered(MouseEvent e) {
    } // Needed for showing mouse coordinates later

    @Override
    public void mouseExited(MouseEvent e) {
    } // Needed for hiding mouse coordinates later

    @Override
    public void mouseClicked(MouseEvent event) {
    } // May be needed for double clicks later on

    @Override
    public void mouseWheelMoved(MouseWheelEvent event) {
    } // TODO: zooming
    //</editor-fold>

    @Override
    public void mouseMoved(MouseEvent event) {
        Point p = new Point(event.getPoint());
        p.translate(-HudCanvas.offX, -HudCanvas.offY);
        ArrayList<Element> elements = canvas.getElements(); // TODO: recursion - the nth child where n is infinite
        for(int i = 0; i < elements.size(); i++) {
            if(!elements.get(i).children.isEmpty()) {
                elements.addAll(elements.get(i).children);
            }
        }
        ArrayList<Element> potentials = canvas.pick(p, elements);
        canvas.hover(canvas.smallest(potentials));
    }

    @Override
    public void mousePressed(MouseEvent event) {
        Point p = new Point(event.getPoint());
        p.translate(-HudCanvas.offX, -HudCanvas.offY);
        dragStart = new Point(p.x, p.y);
        canvas.selectRect.x = p.x;
        canvas.selectRect.y = p.y;
        int button = event.getButton();
        if(button == MouseEvent.BUTTON1) {
            if(canvas.getHovered() == null) {
                // clicked nothing
                if(!event.isControlDown()) {
                    canvas.deselectAll();
                }
                isDragSelecting = true;
                isDragMoving = false;
            } else {
                isDragSelecting = false;
                isDragMoving = true;
                if(event.isControlDown()) {
                    // always select
                    if(canvas.isSelected(canvas.getHovered())) {
                        canvas.deselect(canvas.getHovered());
                    } else {
                        canvas.select(canvas.getHovered());
                    }
                } else {
                    if(!canvas.isSelected(canvas.getHovered())) {
                        // If the thing I'm hovering isn't selected already
                        canvas.deselectAll();
                    }
                    canvas.select(canvas.getHovered());
                    ArrayList<Element> potentials = canvas.getHovered().children;
                    for(int i = 0; i < potentials.size(); i++) {
                        canvas.select(potentials.get(i));
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        isDragSelecting = false;
        isDragMoving = false;
        dragStart = null;
        Rectangle original = new Rectangle(canvas.selectRect);
        canvas.selectRect.width = 0;
        canvas.selectRect.height = 0;
        canvas.doRepaint(new Rectangle(original.x, original.y, original.width + 1, original.height + 1));
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        Point p = new Point(event.getPoint());
        p.translate(-HudCanvas.offX, -HudCanvas.offY);
        if(isDragSelecting) {
            canvas.select(dragStart, p, event.isControlDown());
        } else if(isDragMoving) {
            if(dragStart == null) {
                dragStart = new Point();
            }
            Point v = new Point(p.x - dragStart.x, p.y - dragStart.y);
            ArrayList<Element> elements = canvas.getSelected();
            for(int i = 0; i < elements.size(); i++) {
                if(!(elements.get(i).getParent() != null && canvas.getSelected().contains(elements.get(i).getParent()))) {
                    // if child of parent is not selected, move it anyway
                    canvas.translate(elements.get(i), v.x, v.y);
                }
            }
            dragStart = p;
        }
    }

}
