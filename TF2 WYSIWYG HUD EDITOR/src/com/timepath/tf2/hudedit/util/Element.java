package com.timepath.tf2.hudedit.util;

import com.timepath.tf2.hudedit.EditorFrame;
import com.timepath.tf2.hudedit.display.HudCanvas;
import com.timepath.tf2.hudedit.loaders.ResLoader;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * _minmode
 * [blue/red]_active_[x/y]pos
 * _ [$WIN32]
 * _ [$X360]
 * _ [$OSX]
 *
 * @author andrew
 */
public class Element {

    HudCanvas canvas;

    private String key;

    private String info;

    public ArrayList<Element> children = new ArrayList<Element>();

    private Element parent;
    static final Logger logger = Logger.getLogger(Element.class.getName());

    public Element(HudCanvas canvas) {
        this.canvas = canvas;
    }

    public Element(String key, String info) {
        this.key = key;
        this.info = info;
    }

    public void addElement(Element e) { // TODO: check for child = parent and child already has parent
        if(!children.contains(e)) {
            children.add(e);
            e.setParent(this);
        }
    }

    private ArrayList<Property> propMap = new ArrayList<Property>();

    public void addProp(Property p) {
        logger.log(ResLoader.loaderLevel, "Adding prop: {0} to: {1}", new Object[] {p, this});
        propMap.add(p);
    }

    public void addProps(ArrayList<Property> p) {
        for(int i = 0; i < p.size(); i++) {
            addProp(p.get(i));
        }
        p.clear();
    }

    public ArrayList<Property> getProps() {
        return propMap;
    }

    // Extras
    public int getSize() { // works well unless they are exactly the same size
        return wide * tall;
    }

    public Rectangle getBounds() {
        int minX = this.getX();
        int minY = this.getY();
        int maxX = this.getWidth();
        int maxY = this.getHeight();
        return new Rectangle(minX, minY, maxX + 1, maxY + 1);
    }

    @Override
    public String toString() {
        String displayInfo = (info != null ? (" ~ " + info) : ""); // elements cannot have a value, only info
        return key + displayInfo;
    }

    private String controlName;

    private String fieldName;

    private int xPos;

    public int getLocalX() {
        return xPos;
    }

    public void setLocalX(int x) {
        this.xPos = x;
    }

    public int getX() {
        int x = 0;
        if(this.getXAlignment() == Element.Alignment.Left) {
            x = xPos;
        } else if(this.getXAlignment() == Element.Alignment.Center) {
            x = (EditorFrame.hudRes.width / 2) + xPos;
        } else if(this.getXAlignment() == Element.Alignment.Right) {
            x = (EditorFrame.hudRes.width) - xPos;
        }
        
        return x + (getParent() != null ? getParent().getX() : 0);
    }

    private int yPos;

    public int getLocalY() {
        return yPos;
    }

    public void setLocalY(int y) {
        this.yPos = y;
    }

    public int getY() {
        int y = 0;
        if(this.getYAlignment() == Element.Alignment.Left) {
            y = yPos;
        } else if(this.getYAlignment() == Element.Alignment.Center) {
            y = (EditorFrame.hudRes.height / 2) + yPos;
        } else if(this.getYAlignment() == Element.Alignment.Right) {
            y = (EditorFrame.hudRes.height) - yPos;
        }
        return y + (getParent() != null ? getParent().getY() : 0);
    }

    private int zPos;

    public int getLayer() {
        return zPos;
    }

    public void setLayer(int z) {
        this.zPos = z;
    }

    private int wide;
    
    public int getLocalWidth() {
        return wide;
    }

    public void setLocalWidth(int wide) {
        this.wide = wide;
    }
    
    public int getWidth() {
        if(this.getWidthMode() == DimensionMode.Mode2) {
//            if(this.parent != null) {
//                return this.parent.getWidth() - wide;
//            } else {
                return EditorFrame.hudRes.width - wide;
//            }
        } else {
            return wide;
        }
    }
    
    private DimensionMode _wideMode = DimensionMode.Mode1;
    
    public DimensionMode getWidthMode() {
        return _wideMode;
    }

    public void setWidthMode(DimensionMode mode) {
        this._wideMode = mode;
    }
        
    private int tall;

    public int getLocalHeight() {
        return tall;
    }

    public void setLocalHeight(int tall) {
        this.tall = tall;
    }
    
    public int getHeight() {
        return (this.getHeightMode() == DimensionMode.Mode2 ? (this.parent != null ? this.parent.getHeight() - tall: EditorFrame.hudRes.height - tall) : tall);
    }
    
    private DimensionMode _tallMode = DimensionMode.Mode1;
    
    public DimensionMode getHeightMode() {
        return _tallMode;
    }

    public void setHeightMode(DimensionMode mode) {
        this._tallMode = mode;
    }

    private boolean visible;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private Font font; // http://www.3rd-evolution.de/tkrammer/docs/java_font_size.html

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    private Color fgColor;

    public Color getFgColor() {
        return fgColor;
    }

    public void setFgColor(Color fgColor) {
        this.fgColor = fgColor;
    }

    public void validate() { // TODO: remove duplicate keys (remove the earliest first, or let the user know or something)
        for(int n = 0; n < this.getProps().size(); n++) {
            Property entry = this.getProps().get(n);
            String k = entry.getKey();
            if(k != null && k.contains("\"")) {
                k = k.substring(1, k.length() - 1);
            }
            String v = entry.getValue();
            if(v != null && v.contains("\"")) {
                v = v.substring(1, v.length() - 1);
            }
            String i = entry.getInfo();
            if(i != null && i.contains("\"")) {
                i = i.substring(1, i.length() - 1);
            }

            if("enabled".equalsIgnoreCase(k)) {
                this.setEnabled(Integer.parseInt(v) == 1);
            } else if("visible".equalsIgnoreCase(k)) {
                this.setVisible(Integer.parseInt(v) == 1);
            } else if("xpos".equalsIgnoreCase(k)) {
                if(v.startsWith("c")) {
                    this.setXAlignment(Alignment.Center);
                    v = v.substring(1);
                } else if(v.startsWith("r")) {
                    this.setXAlignment(Alignment.Right);
                    v = v.substring(1);
                } else {
                    this.setXAlignment(Alignment.Left);
                }
                this.setLocalX(Integer.parseInt(v));
            } else if("ypos".equalsIgnoreCase(k)) {
                if(v.startsWith("c")) {
                    this.setYAlignment(Alignment.Center);
                    v = v.substring(1);
                } else if(v.startsWith("r")) {
                    this.setYAlignment(Alignment.Right);
                    v = v.substring(1);
                } else {
                    this.setYAlignment(Alignment.Left);
                }
                this.setLocalY(Integer.parseInt(v));
            } else if("wide".equalsIgnoreCase(k)) {
                if(v.startsWith("f")) {
                    v = v.substring(1);
                    this.setWidthMode(DimensionMode.Mode2);
                }
                this.setLocalWidth(Integer.parseInt(v));
            } else if("tall".equalsIgnoreCase(k)) {
                if(v.startsWith("f")) {
                    v = v.substring(1);
                    this.setHeightMode(DimensionMode.Mode2);
                }
                this.setLocalHeight(Integer.parseInt(v));
            } else if("labelText".equalsIgnoreCase(k)) {
                this.setLabelText(v);
            }
        }
    }

    public HudCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(HudCanvas canvas) {
        this.canvas = canvas;
    }

    public Element getParent() {
        return parent;
    }

    public void setParent(Element newParent) {
        this.parent = newParent;
    }

    public enum Alignment {

        Left, Center, Right

    }
    
    public enum DimensionMode {

        Mode1, Mode2

    }

    private Alignment _xAlignment = Alignment.Left;

    public Alignment getXAlignment() {
        return _xAlignment;
    }

    public void setXAlignment(Alignment _xAlignment) {
        this._xAlignment = _xAlignment;
    }

    private Alignment _yAlignment = Alignment.Left;

    public Alignment getYAlignment() {
        return _yAlignment;
    }

    public void setYAlignment(Alignment _yAlignment) {
        this._yAlignment = _yAlignment;
    }

    private String labelText;

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }

    private Image image;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    private boolean scaleImage;

    private boolean pinCorner;

}