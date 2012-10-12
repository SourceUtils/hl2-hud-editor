package com.timepath.tf2.hudedit.display;

import com.timepath.tf2.hudedit.EditorFrame;
import com.timepath.tf2.hudedit.util.Element;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * TODO: Investigate why 'deleting' elements doesn't deselect them
 *
 * @author andrew
 */
@SuppressWarnings("serial")
public class HudCanvas extends JPanel implements MouseListener, MouseMotionListener {

    public HudCanvas() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
//        loadBackground();
    }

    private Color BG_COLOR = Color.GRAY;

    public static int offX = 0; // left
    public static int offY = 0; // top

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;

//        g.drawImage(background, 0, 0, null);
        g.setColor(BG_COLOR);
        g.fillRect(offX, offY, EditorFrame.hudRes.width, EditorFrame.hudRes.height);

        for(int i = 0; i < elements.size(); i++) {
            paintElement(elements.get(i), g);
        }
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);
        g.setComposite(ac);
        g.setColor(Color.CYAN.darker());
        g.fillRect(offX + selectRect.x, offY + selectRect.y, selectRect.width + 1, selectRect.height + 1);
        g.setColor(Color.BLUE);
        g.drawRect(offX + selectRect.x, offY + selectRect.y, selectRect.width + 1, selectRect.height + 1);

//        if(repainted != null) {
//            g.setColor(Color.PINK.darker());
//            g.fillRect(offX + repainted.x, offY + repainted.y, repainted.width, repainted.height);
//            g.setColor(Color.RED);
//            g.drawRect(offX + repainted.x, offY + repainted.y, repainted.width, repainted.height);
//        }
    }

    private void paintElement(Element e, Graphics2D g) {

        if(selectedElements.contains(e)) {
            g.setColor(Color.CYAN);
        } else {
            g.setColor(Color.GREEN);
        }

        g.drawRect(e.getX() + offX, e.getY() + offY, e.getWidth(), e.getHeight());

//        g.setColor(Color.PINK);
//        g.drawRect(e.getBounds().x, e.getBounds().y, e.getBounds().width, e.getBounds().height);

        if(hoveredElement == e) {
            g.setColor(new Color(255-g.getColor().getRed(), 255-g.getColor().getGreen(), 255-g.getColor().getBlue()));
            g.drawRect(e.getX() + offX + 1, e.getY() + offY + 1, e.getWidth() - 2, e.getHeight() - 2);
            g.drawRect(e.getX() + offX - 1, e.getY() + offY - 1, e.getWidth() + 2, e.getHeight() + 2);
        }

        if(e.getLabelText() != null && !e.getLabelText().isEmpty()) {
            g.drawString(e.getLabelText(), e.getX() + offX, e.getY() + offY);
        }

        for(int i = 0; i < e.children.size(); i++) {
            paintElement(e.children.get(i), g);
        }
    }

    public void doRepaint(Rectangle bounds) { // override method
//        this.repainted = bounds;
        this.repaint(bounds.x + offX, bounds.y + offX, bounds.width, bounds.height);
//        this.repaint();
    }

        private boolean isDragSelecting;

        private boolean isDragMoving;

        private Point dragStart;

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

        //</editor-fold>

        @Override
        public void mouseMoved(MouseEvent event) {
            Point p = new Point(event.getPoint());
            p.translate(-HudCanvas.offX, -HudCanvas.offY);
            ArrayList<Element> elements = getElements(); // TODO: recursion - the nth child where n is infinite
            for(int i = 0; i < elements.size(); i++) {
                if(!elements.get(i).children.isEmpty()) {
                    elements.addAll(elements.get(i).children);
                }
            }
            ArrayList<Element> potentials = pick(p, elements);
            hover(smallest(potentials));
        }

        @Override
        public void mousePressed(MouseEvent event) {
            Point p = new Point(event.getPoint());
            p.translate(-HudCanvas.offX, -HudCanvas.offY);
            dragStart = new Point(p.x, p.y);
            selectRect.x = p.x;
            selectRect.y = p.y;
            int button = event.getButton();
            if(button == MouseEvent.BUTTON1) {
                if(getHovered() == null) {
                    // clicked nothing
                    if(!event.isControlDown()) {
                        deselectAll();
                    }
                    isDragSelecting = true;
                    isDragMoving = false;
                } else {
                    isDragSelecting = false;
                    isDragMoving = true;
                    if(event.isControlDown()) {
                        // always select
                        if(isSelected(getHovered())) {
                            deselect(getHovered());
                        } else {
                            select(getHovered());
                        }
                    } else {
                        if(!isSelected(getHovered())) {
                            // If the thing I'm hovering isn't selected already
                            deselectAll();
                        }
                        select(getHovered());
                        ArrayList<Element> potentials = getHovered().children;
                        for(int i = 0; i < potentials.size(); i++) {
                            select(potentials.get(i));
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
            Rectangle original = new Rectangle(selectRect);
            selectRect.width = 0;
            selectRect.height = 0;
            doRepaint(new Rectangle(original.x, original.y, original.width + 1, original.height + 1));
        }

        @Override
        public void mouseDragged(MouseEvent event) {
            Point p = new Point(event.getPoint());
            p.translate(-HudCanvas.offX, -HudCanvas.offY);
            if(isDragSelecting) {
                select(dragStart, p, event.isControlDown());
            } else if(isDragMoving) {
                if(dragStart == null) {
                    dragStart = new Point();
                }
                Point v = new Point(p.x - dragStart.x, p.y - dragStart.y);
                ArrayList<Element> elements = getSelected();
                for(int i = 0; i < elements.size(); i++) {
                    if(!(elements.get(i).getParent() != null && getSelected().contains(elements.get(i).getParent()))) {
                        // if child of parent is not selected, move it anyway
                        translate(elements.get(i), v.x, v.y);
                    }
                }
                dragStart = p;
            }
        }



    Rectangle selectRect = new Rectangle();

    Image background;

    // List of elements
    private ArrayList<Element> elements = new ArrayList<Element>();

    public ArrayList<Element> getElements() {
        return elements;
    }

    public void addElement(Element e) {
        if(!elements.contains(e)) {
            e.setCanvas(this);
            elements.add(e);
            this.doRepaint(e.getBounds());
        }
    }

    public void removeElement(Element e) {
        if(elements.contains(e)) {
            elements.remove(e);
            this.doRepaint(e.getBounds());
        }
    }

    public void removeElements(ArrayList<Element> e) {
        for(int i = 0; i < e.size(); i++) {
            removeElement(e.get(i));
        }
    }

    public void clearElements() {
        for(int i = 0; i < elements.size(); i++) {
            removeElement(elements.get(i));
        }
    }

    public void load(Element element) {
        element.validate();
//        if(element.isEnabled() || element.isVisible()) {
            this.addElement(element);
//        }
    }

    // List of currently selected elements
    private ArrayList<Element> selectedElements = new ArrayList<Element>();

    public ArrayList<Element> getSelected() {
        return selectedElements;
    }

    public boolean isSelected(Element e) {
        return selectedElements.contains(e);
    }

    public void select(Element e) {
        if(!selectedElements.contains(e) && e != null) {
            selectedElements.add(e);

            if(e.children != null) {
                for(int i = 0; i < e.children.size(); i++) {
                    select(e.children.get(i));
                }
            }

            this.doRepaint(e.getBounds());
        }
    }

    public void deselect(Element e) {
        if(selectedElements.contains(e)) { // && e != null && !selectedElements.contains(e.getParent())) {
            selectedElements.remove(e);
            this.doRepaint(e.getBounds());
        }
    }

    public void deselectAll() {
        for(int i = 0; i < selectedElements.size(); i++) {
            Element e = selectedElements.get(i);
            selectedElements.remove(i);
            this.doRepaint(e.getBounds());
        }
    }

    // List of currently selected elements
    private Element hoveredElement;

    public Element getHovered() {
        return hoveredElement;
    }

    void hover(Element e) {
        if(hoveredElement != e) { // don't waste time re-drawing
            Rectangle oldBounds = null;
            if(hoveredElement != null) { // there is something to clean up
                oldBounds = hoveredElement.getBounds();
            }
            hoveredElement = e;
            if(oldBounds != null) {
                this.doRepaint(oldBounds);
            }
            if(e != null) {
                this.doRepaint(e.getBounds());
            }
        }
    }

//    private void loadBackground() {
//        URL url = getClass().getResource("/com/timepath/tf2/hudedit/images/bg.png");
//        background = Toolkit.getDefaultToolkit().getImage(url);
//        this.prepareImage(background, this);
//    }

    // Checks if poing p is inside the bounds of any element
    public ArrayList<Element> pick(Point p, ArrayList<Element> elements) {
        ArrayList<Element> potential = new ArrayList<Element>();
        for(int i = 0; i < elements.size(); i++) {
            Element e = elements.get(i);
            if(e.getBounds().contains(p)) {
                potential.add(e);
            }
        }
        return potential;
    }

    public Element smallest(ArrayList<Element> potential) {
        int pSize = potential.size();
        if(pSize == 0) {
            return null;
        }
        if(pSize == 1) {
            return potential.get(0);
        }
        Element smallest = potential.get(0);
        for(int i = 1; i < potential.size(); i++) {
            Element e = potential.get(i);
            if(e.getSize() < smallest.getSize()) {
                smallest = e;
            }
        }
        return smallest;
    }

    public void select(Point p1, Point p2, boolean ctrl) {
        if(p1 != null && p2 != null) {
            Rectangle originalSelectRect = new Rectangle(this.selectRect);
            this.selectRect = fitRect(p1, p2, this.selectRect);
            for(int i = 0; i < this.getElements().size(); i++) {
                Element e = this.getElements().get(i);
                if(this.selectRect.intersects(e.getBounds())) {
                    this.select(e); // TODO: not perfect, I want the selection inverted as it goes over
                } else {
                    if(!ctrl) {
                        this.deselect(e);
                    }
                }
            }
            this.doRepaint(new Rectangle(originalSelectRect.x, originalSelectRect.y, originalSelectRect.width + 2, originalSelectRect.height + 2)); // TODO: optimize further - doRepaint 3 segments : overlapping, original, changed
            this.doRepaint(new Rectangle(this.selectRect.x, this.selectRect.y, this.selectRect.width + 2, this.selectRect.height + 2)); // why are these +2?
        }
    }

    private Rectangle fitRect(Point p1, Point p2, Rectangle r) {
        Rectangle result = new Rectangle(r);
        result.x = Math.min(p1.x, p2.x);
        result.y = Math.min(p1.y, p2.y);
        result.width = Math.abs(p2.x - p1.x);
        result.height = Math.abs(p2.y - p1.y);
        return result;
    }

    public void translate(Element get, int diffX, int diffY) {
        Rectangle originalBounds = get.getBounds();
        get.setLocalX(get.getLocalX() + diffX);
        get.setLocalY(get.getLocalY() + diffY);
        this.doRepaint(originalBounds);
        this.doRepaint(get.getBounds());
        this.repaint(); // help
    }

    public void removeAllElements() {
        for(int i = 0; i < elements.size(); i++) {
            elements.remove(i);
        }
    }

}