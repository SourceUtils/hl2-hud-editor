package com.timepath.tf2.hudedit.swing;

import com.timepath.tf2.hudedit.util.Element;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JPanel;

/**
 *
 * @author andrew
 */
@SuppressWarnings("serial")
public class EditorCanvas extends JPanel implements MouseListener, MouseMotionListener {

    public EditorCanvas() {
        super();
        init();
    }

    private void init() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setPreferredSize(new Dimension(853, 480));
        loadBackground();
    }
    
    public Dimension internalRes;
    public Dimension hudRes;
    public double scale = 1;

    //<editor-fold defaultstate="collapsed" desc="Background image">
    private Image background;
    
    private void loadBackground() {
        URL url = getClass().getResource("/Badlands1.png");
        background = Toolkit.getDefaultToolkit().getImage(url);
        this.prepareImage(background, this);
    }
    //</editor-fold>

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        
        currentbg = null;
        gridbg = null;
        hudRes = preferredSize;
        
//        long gcm = gcm(hudRes.width, hudRes.height);
        long resX = hudRes.width;
        long resY = hudRes.height;
        double m = (double)resX / (double)resY;
//        System.out.println(resX + "/" + resY + "=" + m);
//        System.out.println((resX / gcm) + ":" + (resY / gcm) + " = " + Math.round(m * 480) + "x" + 480);

        internalRes = new Dimension((int)Math.round(m * 480), 480);
        this.repaint();
    }
    
    //<editor-fold defaultstate="collapsed" desc="Utility methods">
    /**
     * Finds the greatest common multiple
     * @param a
     * @param b
     * @return
     */
    public static long gcm(long a, long b) {
        return b == 0 ? a : gcm(b, a % b);
    }
    
    public static Rectangle fitRect(Point p1, Point p2, Rectangle r) {
        Rectangle result = new Rectangle(r);
        result.x = Math.min(p1.x, p2.x);
        result.y = Math.min(p1.y, p2.y);
        result.width = Math.abs(p2.x - p1.x);
        result.height = Math.abs(p2.y - p1.y);
        return result;
    }
    //</editor-fold>

    private Color BG_COLOR = Color.GRAY;
    private Color GRID_COLOR = Color.GRAY.brighter();

    private static int offX = 0; // left
    private static int offY = 0; // top

    private Rectangle selectRect = new Rectangle();
    
    private Image currentbg;
    private Image gridbg;

    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);
    
    //<editor-fold defaultstate="collapsed" desc="Paint methods">
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        
        if(background != null) {
            if(currentbg == null) {
                currentbg = resizeImage(background);
            }
            g.drawImage(currentbg, offX, offY, this);
        } else {
            g.setColor(BG_COLOR);
            g.fillRect(offX, offY, (int)Math.round(hudRes.width * scale), (int)Math.round(hudRes.height * scale));
        }
        if(true) {
            if(gridbg == null) {
                gridbg = drawGrid();
            }
            g.drawImage(gridbg, offX, offY, this);
        }
        
        Collections.sort(elements, layerSort);
        
        for(int i = 0; i < elements.size(); i++) {
            paintElement(elements.get(i), g);
            //            System.out.println(elements.get(i).getParent());
        }
        
        //<editor-fold defaultstate="collapsed" desc="Selection rectangle">
        g.setComposite(ac);
        g.setColor(Color.CYAN.darker());
        g.fillRect(offX + selectRect.x + 1, offY + selectRect.y + 1, selectRect.width - 2, selectRect.height - 2);
        g.setColor(Color.BLUE);
        g.drawRect(offX + selectRect.x, offY + selectRect.y, selectRect.width - 1, selectRect.height - 1);
        //</editor-fold>
    }
    
    private static Comparator<Element> layerSort = new Comparator<Element>() {
        @Override
        public int compare(Element e1, Element e2) {
            return e1.getLayer() - e2.getLayer();
        }
    };
    
    private Image resizeImage(Image i) { // TODO: aspect ratio tuning
        int w = hudRes.width;
        int h = hudRes.height;        
        int type = BufferedImage.TYPE_INT_ARGB;

        BufferedImage resizedImage = new BufferedImage(w, h, type);
        Graphics2D g = resizedImage.createGraphics();

        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int proposedWidth = Math.round((float)h / (float)i.getHeight(null) * (float)i.getWidth(null));
        int excess = Math.abs(proposedWidth - w) / 2;
        g.drawImage(i, -excess, 0, w + (2 * excess), h, this); // should scale most images correctly
        g.dispose();

        return resizedImage;
    }
    
    private int minGridSpacing = 10;
    
    // as soon as the height drops below 480, stops rendering
    private Image drawGrid() {
        BufferedImage img = new BufferedImage(hudRes.width, hudRes.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

//        g.setComposite(AlphaComposite.Src);
        g.setComposite(ac);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        
        g.setColor(GRID_COLOR);
        
        int w = hudRes.width;
        int h = hudRes.height;
        int i = minGridSpacing; 
        if(i < 0) {
            return img;
        }
        if(i < 2) { // optimize for small numbers, stop division by zero
            g.fillRect(offX, offY, hudRes.width, hudRes.height);
            return img;
        }
        int cross = 0;
        int maxX = w - (w % i);
        int maxY = h - (h % i);
        double multX = (hudRes.width / internalRes.width);
        double multY = (hudRes.height / internalRes.height);
        for(int y = 0; y <= (maxY / i); y++) {
            for(int x = 0; x <= (maxX / i); x++) {
                int dx = (int) Math.round(((maxX * x * i * multX) / maxX) + offX);
                int dy = (int) Math.round(((maxY * y * i * multY) / maxY) + offY);
                g.drawLine(dx + cross, dy + cross, dx - (1 + cross), dy - (1 + cross));
                g.drawLine(dx - (1 + cross), dy + cross, dx + cross, dy - (1 + cross));
            }
        }
        g.dispose();
        return img;
    }
    
    private void paintElement(Element e, Graphics2D g) {
        if(e.getWidth() > 0 && e.getHeight() > 0) { // invisible? don't waste time
            int elementX = (int) Math.round((double) e.getX() * ((double)hudRes.width / (double)internalRes.width) * scale);
            int elementY = (int) Math.round((double) e.getY() * ((double)hudRes.height / (double)internalRes.height) * scale);
            int elementW = (int) Math.round((double) e.getWidth() * ((double)hudRes.width / (double)internalRes.width) * scale);
            int elementH = (int) Math.round((double) e.getHeight() * ((double)hudRes.height / (double)internalRes.height) * scale);
            
            if(e.getFgColor() != null) {
                g.setColor(e.getFgColor());
                g.fillRect(elementX + offX, elementY + offY, elementW - 1, elementH - 1);
            }
            
            if(selectedElements.contains(e)) {
                g.setColor(Color.CYAN);
            } else {
                g.setColor(Color.GREEN);
            }
            g.drawRect(elementX + offX, elementY + offY, elementW - 1, elementH - 1);
            
            
            if(hoveredElement == e) {
                g.setColor(new Color(255-g.getColor().getRed(), 255-g.getColor().getGreen(), 255-g.getColor().getBlue()));
                //                g.drawRect(elementX + offX, elementY + offY, e.getWidth() - 1, e.getHeight() - 1); // border
                g.drawRect(elementX + offX + 1, elementY + offY + 1, elementW - 3, elementH - 3); // inner
                //                g.drawRect(elementX + offX - 1, elementY + offY - 1, e.getWidth() + 1, e.getHeight() + 1); // outer
            }
            
            if(e.getLabelText() != null && !e.getLabelText().isEmpty()) {
                g.drawString(e.getLabelText(), elementX + offX, elementY + offY);
            }
        }
        
        //        for(int i = 0; i < e.children.size(); i++) {
        //            paintElement(e.children.get(i), g);
        //        }
    }
    
    public void doRepaint(Rectangle bounds) {
        this.repaint(offX + bounds.x, offY + bounds.y, bounds.width - 1, bounds.height - 1); // repaint the bare minimum
        //        this.repaint();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Input methods">
    @Override
    public void mouseMoved(MouseEvent event) {
        Point p = new Point(event.getPoint());
        p.translate(-EditorCanvas.offX, -EditorCanvas.offY);
        
        hover(chooseBest(pick(p, elements)));
    }
    
    @Override
    public void mousePressed(MouseEvent event) {
        Point p = new Point(event.getPoint());
        p.translate(-EditorCanvas.offX, -EditorCanvas.offY);
        int button = event.getButton(); // Button2 is middle click on linux. How odd.
        
        if(button == MouseEvent.BUTTON1) {
            dragStart = new Point(p.x, p.y);
            selectRect.x = p.x;
            selectRect.y = p.y;
            if(getHovered() == null) { // clicked nothing
                if(!event.isControlDown()) {
                    deselectAll();
                }
                isDragSelecting = true;
                isDragMoving = false;
            } else { // hovering over something
                isDragSelecting = false;
                isDragMoving = true;
                if(event.isControlDown()) { // always select
                    if(isSelected(getHovered())) {
                        deselect(getHovered());
                    } else {
                        select(getHovered());
                    }
                } else {
                    if(!isSelected(getHovered())) { // If the thing I'm hovering isn't selected already
                        deselectAll();
                        select(getHovered());
                    }
                }
            }
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent event) {
        Point p = new Point(event.getPoint());
        p.translate(-EditorCanvas.offX, -EditorCanvas.offY);
        int button = event.getButton();
        
        if(button == MouseEvent.BUTTON1) {
            isDragSelecting = false;
            isDragMoving = false;
            dragStart = null;
            Rectangle original = new Rectangle(selectRect);
            selectRect.width = 0;
            selectRect.height = 0;
            doRepaint(new Rectangle(original.x, original.y, original.width + 1, original.height + 1));
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent event) { 
        Point p = new Point(event.getPoint());
        p.translate(-EditorCanvas.offX, -EditorCanvas.offY);
        int button = event.getButton();
        
        //        if(button == MouseEvent.BUTTON1) {
        if(isDragSelecting) {
            select(dragStart, p, event.isControlDown());
        } else if(isDragMoving) {
            for(int i = 0; i < selectedElements.size(); i++) {
                Element e = selectedElements.get(i);
                if(selectedElements.contains(e.getParent()) && !e.getParent().getName().replaceAll("\"", "").endsWith(".res")) { // XXX: hacky
                    continue;
                }
                translate(e, p.x - dragStart.x, p.y - dragStart.y);
            }
            dragStart = p; // hacky
        }
        //        }
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

    //</editor-fold>
    
    //</editor-fold>

    private boolean isDragSelecting;

    private boolean isDragMoving;

    private Point dragStart;

    //<editor-fold defaultstate="collapsed" desc="Element management">
    // List of elements
    private ArrayList<Element> elements = new ArrayList<Element>();
    
    public ArrayList<Element> getElements() {
        return elements;
    }
    
    public void addElement(Element e) {
        if(!elements.contains(e)) {
            e.validateLoad();
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
        if(Element.areas.containsKey(element.getFile())) {
            Element p = Element.areas.get(element.getFile());
            p.addChild(element);
            this.addElement(p);
        }
        else if(element.getFile().equalsIgnoreCase("HudPlayerHealth")) { // better, but still not perfect
            // move by "CHealthAccountPanel" delta_item_x" and "delta_item_start_y"
            Element p = Element.areas.get("CHealthAccountPanel");
            p.addChild(element);
            this.addElement(p);
        }
        this.addElement(element); // weird but it has to be done
    }
    
    // List of currently selected elements
    private ArrayList<Element> selectedElements = new ArrayList<Element>();
    
    private ArrayList<Element> getSelected() {
        return selectedElements;
    }
    
    public boolean isSelected(Element e) {
        return selectedElements.contains(e);
    }
    
    public void select(Element e) {
        if(e != null) {
            if(selectedElements.contains(e)) {
                return;
            }
            
            selectedElements.add(e);
            
            //            if(e.children != null) {
            //                for(int i = 0; i < e.children.size(); i++) {
            //                    select(e.children.get(i));
            //                }
            //            }
            
            this.doRepaint(e.getBounds());
        }
    }
    
    public void deselect(Element e) {
        if(e != null) {
            if(!selectedElements.contains(e)) {
                return;
            }
            
            selectedElements.remove(e);
            
            //            if(e.children != null) {
            //                for(int i = 0; i < e.children.size(); i++) {
            //                    deselect(e.children.get(i));
            //                }
            //            }
            
            this.doRepaint(e.getBounds());
        }
    }
    
    public void deselectAll() {
        ArrayList<Element> temp = new ArrayList<Element>(selectedElements);
        selectedElements.clear();
        for(int i = 0; i < temp.size(); i++) {
            this.doRepaint(temp.get(i).getBounds());
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
    
    public Element chooseBest(ArrayList<Element> potential) {
        int pSize = potential.size();
        if(pSize == 0) {
            return null;
        }
        if(pSize == 1) {
            return potential.get(0);
        }
        Element smallest = potential.get(0);
        for(int i = 1; i < potential.size(); i++) {
            Element e = potential.get(i); // sort by layer, then by size
            if(e.getLayer() > smallest.getLayer()) {
                smallest = e;
            } else if(e.getLayer() == smallest.getLayer()) {
                if(e.getSize() < smallest.getSize()) {
                    smallest = e;
                }
            }
        }
        return smallest;
    }
    
    public void select(Point p1, Point p2, boolean ctrl) {
        if(p1 != null && p2 != null) {
            Rectangle originalSelectRect = new Rectangle(selectRect);
            selectRect = fitRect(p1, p2, selectRect);
            for(int i = 0; i < elements.size(); i++) {
                Element e = elements.get(i);
                if(selectRect.intersects(e.getBounds())) {
                    select(e); // TODO: not perfect, I want the selection inverted as it goes over
                } else {
                    if(!ctrl) {
                        deselect(e);
                    }
                }
            }
            // This repaints the overlap twice. A minor inefficiency...
            this.doRepaint(new Rectangle(originalSelectRect.x, originalSelectRect.y, originalSelectRect.width + 1, originalSelectRect.height + 1));
            this.doRepaint(new Rectangle(this.selectRect.x, this.selectRect.y, this.selectRect.width + 1, this.selectRect.height + 1));
        }
    }
    
    public void translate(Element e, int dx, int dy) { // todo: scaling (scale 5 = 5 pixels to move 1 x/y co-ord)
//        Rectangle originalBounds = new Rectangle(e.getBounds());
        if(e.getXAlignment() == Element.Alignment.Right) {
            dx *= -1;
        }
        if(e.getYAlignment() == Element.Alignment.Right) {
            dy *= -1;
        }
        e.setLocalX(e.getLocalX() + (int)(dx / scale));
        e.setLocalY(e.getLocalY() + (int)(dy / scale));
//        this.doRepaint(originalBounds);
//        this.doRepaint(e.getBounds());
        this.repaint(); // helps
    }

    public void removeAllElements() {
        ArrayList<Element> temp = new ArrayList<Element>(elements);
        elements.removeAll(elements);
        for(int i = 0; i < temp.size(); i++) {
            Element e = temp.get(i);
            this.doRepaint(e.getBounds());
        }   
    }
    //</editor-fold>

}