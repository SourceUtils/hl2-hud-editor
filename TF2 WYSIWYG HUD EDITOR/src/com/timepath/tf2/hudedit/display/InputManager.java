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

    private HudCanvas canvas;

    private boolean isDragSelecting;

    private boolean isDragMoving;

    HudCanvas outer;

    private Point dragStart;

    InputManager(final HudCanvas canvas, HudCanvas outer) {
        this.outer = outer;
        this.canvas = canvas;
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
        Point p = event.getPoint();
        p.translate(outer.offX, outer.offY);
        ArrayList<Element> elements = outer.getElements(); // TODO: recursion - the nth child where n is infinite
        for(int i = 0; i < elements.size(); i++) {
            if(!elements.get(i).children.isEmpty()) {
                elements.addAll(elements.get(i).children);
            }
        }
        ArrayList<Element> potentials = outer.pick(p, elements);
        outer.hover(outer.smallest(potentials));
    }

    @Override
    public void mousePressed(MouseEvent event) {
        Point p = event.getPoint();
        p.translate(outer.offX, outer.offY);
        dragStart = new Point(p.x, p.y);
        outer.selectRect.x = p.x;
        outer.selectRect.y = p.y;
        int button = event.getButton();
        if(button == MouseEvent.BUTTON1) {
            if(outer.getHovered() == null) {
                // clicked nothing
                if(!event.isControlDown()) {
                    outer.deselectAll();
                }
                isDragSelecting = true;
                isDragMoving = false;
            } else {
                isDragSelecting = false;
                isDragMoving = true;
                if(event.isControlDown()) {
                    // always select
                    if(outer.isSelected(outer.getHovered())) {
                        outer.deselect(outer.getHovered());
                    } else {
                        outer.select(outer.getHovered());
                    }
                } else {
                    if(!outer.isSelected(outer.getHovered())) {
                        // If the thing I'm hovering isn't selected already
                        outer.deselectAll();
                    }
                    outer.select(outer.getHovered());
                    ArrayList<Element> potentials = outer.getHovered().children;
                    for(int i = 0; i < potentials.size(); i++) {
                        outer.select(potentials.get(i));
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
        Rectangle original = new Rectangle(outer.selectRect);
        outer.selectRect.width = 0;
        outer.selectRect.height = 0;
        outer.doRepaint(new Rectangle(original.x, original.y, original.width + 1, original.height + 1));
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        Point p = event.getPoint();
        p.translate(outer.offX, outer.offY);
        if(isDragSelecting) {
            outer.select(dragStart, p, event.isControlDown());
        } else if(isDragMoving) {
            if(dragStart == null) {
                dragStart = new Point();
            }
            Point v = new Point(p.x - dragStart.x, p.y - dragStart.y);
            ArrayList<Element> elements = outer.getSelected();
            for(int i = 0; i < elements.size(); i++) {
                if(!(elements.get(i).getParent() != null && outer.getSelected().contains(elements.get(i).getParent()))) {
                    // if child of parent is not selected, move it anyway
                    outer.translate(elements.get(i), v.x, v.y);
                }
            }
            dragStart = p;
        }
    }

}
