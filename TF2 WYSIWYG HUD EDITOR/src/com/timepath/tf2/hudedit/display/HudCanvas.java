package com.timepath.tf2.hudedit.display;

import com.timepath.tf2.hudedit.util.Element;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author andrew
 */
@SuppressWarnings("serial")
public class HudCanvas extends JPanel {

    Rectangle selectRect = new Rectangle();
    Image background;

    public HudCanvas() {
        new InputManager(this, this).init();
        loadBackground();
//        test();
    }

    void test() {
//        for(int i = 0; i < 8; i++) {
//            Element rect = new Element(this);
//            rect.setX((int) (10 + (Math.random() * 500)));
//            rect.setY((int) (10 + (Math.random() * 500)));
//            rect.setWidth((int) (10 + (Math.random() * 500)));
//            rect.setHeight((int) (10 + (Math.random() * 500)));
//
//            this.addElement(rect);
//        }

        // dragging same size objects
        Element rect1 = new Element(this);
        rect1.setLabelText("parent");
        rect1.setX(10);
        rect1.setY(10);
        rect1.setWidth(200);
        rect1.setHeight(200);
        this.addElement(rect1);

        Element rect2 = new Element(this);
        rect2.setLabelText("other");
        rect2.setX(10);
        rect2.setY(10);
        rect2.setWidth(25);
        rect2.setHeight(25);
        this.addElement(rect2);

        Element rect3 = new Element(this);
        rect3.setLabelText("child");
        rect3.setX(10);
        rect3.setY(10);
        rect3.setWidth(50);
        rect3.setHeight(50);
        rect1.addElement(rect3);
    }

    // List of elements

    private ArrayList<Element> elements = new ArrayList<Element>();

    public ArrayList<Element> getElements() {
        return elements;
    }

    public void addElement(Element e) {
        if(!elements.contains(e)) {
            int x = 0;
            if(e.getXAlignment() == Element.Alignment.Left) {
                x = e.getX();
            } else if(e.getXAlignment() == Element.Alignment.Center) {
                x = (this.getPreferredSize().width / 2) + e.getX();
            } else if(e.getXAlignment() == Element.Alignment.Right) {
                x = (this.getPreferredSize().width) - e.getX();
            }
            e.setX(x);

            int y = 0;
            if(e.getXAlignment() == Element.Alignment.Left) {
                y = e.getY();
            } else if(e.getXAlignment() == Element.Alignment.Center) {
                y = (this.getPreferredSize().height / 2) + e.getY();
            } else if(e.getXAlignment() == Element.Alignment.Right) {
                y = (this.getPreferredSize().height) - e.getY();
            }
            e.setY(y);

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

    public void clearElements() {
        for(int i = 0; i < elements.size(); i++) {
            removeElement(elements.get(i));
        }
    }

    public void load(Element element) {
//        System.out.println(element + " - " + element.getParent());
        element.validate();
        if(element.isEnabled() || element.isVisible()) {
            element.setCanvas(this);
            this.addElement(element);
        }
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

    void deselect(Element e) {
        if(selectedElements.contains(e) && e != null && !selectedElements.contains(e.getParent())) {
            selectedElements.remove(e);
            this.doRepaint(e.getBounds());
        }
    }

    void deselectAll() {
        for(int i = 0; i < elements.size(); i++) {
            deselect(elements.get(i));
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

    private void loadBackground() {
        URL url = getClass().getResource("/images/bg.png");
        background = Toolkit.getDefaultToolkit().getImage(url);
        this.prepareImage(background, this); // this is handy
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;

        if(background == null) {
            loadBackground();
        }

//        g.drawImage(background, 0, 0, null);
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, this.getPreferredSize().width, this.getPreferredSize().height);

        for(int i = 0; i < elements.size(); i++) {
            paintElement(elements.get(i), g);
        }
        AlphaComposite ac =  AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.25f);
        g.setComposite(ac);
        g.setColor(Color.CYAN.darker());
        g.fillRect(selectRect.x, selectRect.y, selectRect.width, selectRect.height);
        g.setColor(Color.BLUE);
        g.drawRect(selectRect.x, selectRect.y, selectRect.width, selectRect.height);
    }

    private void paintElement(Element e, Graphics2D g) {
        int offX = 0;
        int offY = 0;

        if(e.getParent() != null) {
            offX = e.getParent().getX();
            offY = e.getParent().getY();
        }

        g.setColor(Color.CYAN.darker());
        g.drawRect(e.getBounds().x - 1, e.getBounds().y - 1, e.getBounds().width + 1, e.getBounds().height + 1);

        if(selectedElements.contains(e)) {
            g.setColor(Color.RED);
        } else if(hoveredElement == e) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.GREEN);
        }

        if(e.getParent() != null) {
            g.setColor(g.getColor().brighter());
        } else {
            g.setColor(g.getColor().darker());
        }

        g.drawRect(e.getX() + offX, e.getY() + offY, e.getWidth(), e.getHeight());

        if(e.getLabelText() != null && !e.getLabelText().isEmpty()) {
            g.drawString(e.getLabelText(), e.getX() + offX, e.getY() + offY);
        }

        for(int i = 0; i < e.children.size(); i++) {
            paintElement(e.children.get(i), g);
        }
    }

    public void doRepaint(Rectangle bounds) { // override method
        this.repaint(bounds);
//        this.repaint();
    }

    // Checks if poing p is inside the bounds of any element
    public ArrayList<Element> pick(Point p, ArrayList<Element> elements) {
        ArrayList<Element> potential = new ArrayList<Element>();
        for(int i = 0; i < elements.size(); i++) {
            Element e = elements.get(i);
            Point p2 = new Point(p);
            if(e.getBounds().contains(p2)) {
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
            this.doRepaint(new Rectangle(originalSelectRect.x, originalSelectRect.y, originalSelectRect.width + 1, originalSelectRect.height + 1)); // TODO: optimize further - doRepaint 3 segments : overlapping, original, changed
            this.doRepaint(new Rectangle(this.selectRect.x, this.selectRect.y, this.selectRect.width + 1, this.selectRect.height + 1));
        }
    }

    private Rectangle fitRect(Point p1, Point p2, Rectangle r) {
        Rectangle result = r;
        result.x = Math.min(p1.x, p2.x);
        result.y = Math.min(p1.y, p2.y);
        result.width = Math.abs(p2.x - p1.x);
        result.height = Math.abs(p2.y - p1.y);
        return result;
    }

    public void translate(Element get, int diffX, int diffY) {
        Rectangle originalBounds = get.getBounds();
        get.setX(get.getX() + diffX);
        get.setY(get.getY() + diffY);
        this.doRepaint(originalBounds);
        this.doRepaint(get.getBounds());
        this.repaint(); // help
    }
}