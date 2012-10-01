package com.timepath.tf2.hudedit.util;

import com.timepath.tf2.hudedit.display.HudCanvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public Element(HudCanvas canvas) {
        this.canvas = canvas;
    }

    public Element(String key, String info) {
        this.key = key;
        this.info = info;
    }

    public void addElement(Element e) { // TODO: check for child = parent and child already has parent
        if(!children.contains(e)) {
            int absX = 0;
            if(e.getXAlignment() == Element.Alignment.Left) {
                absX = e.getX();
            } else if(e.getXAlignment() == Element.Alignment.Center) {
                absX = (this.getWidth() / 2) + e.getX();
            } else if(e.getXAlignment() == Element.Alignment.Right) {
                absX = (this.getWidth()) - e.getX();
            }
//            e.setX(absX);

            int absY = 0;
            if(e.getXAlignment() == Element.Alignment.Left) {
                absY = e.getY();
            } else if(e.getXAlignment() == Element.Alignment.Center) {
                absY = (this.getHeight() / 2) + e.getY();
            } else if(e.getXAlignment() == Element.Alignment.Right) {
                absY = (this.getHeight()) - e.getY();
            }
//            e.setY(absY);

            children.add(e);
            e.setParent(this);
        }
    }

    private Map<KVPair<String, String>, String> propMap = new HashMap<KVPair<String, String>, String>();

    public void addProp(String key, String val, String info) {
        propMap.put(new KVPair<String, String>(key, val), info);
    }

    public Map<KVPair<String, String>, String> getProps() {
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
        if(parent != null) {
            minX += parent.getX();
            minY += parent.getY();
        }
        return new Rectangle(minX, minY, maxX + 1, maxY + 1);
    }

    @Override
    public String toString() {
        String displayInfo = (info != null ? (" ~ " + info) : "");
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
        return xPos + (getParent() != null ? getParent().getX() : 0);
    }

    private int yPos;

    public int getLocalY() {
        return yPos;
    }

    public void setLocalY(int y) {
        this.yPos = y;
    }

    public int getY() {
        return yPos + (getParent() != null ? getParent().getY() : 0);
    }

    private int zPos;

    public int getLayer() {
        return zPos;
    }

    public void setLayer(int z) {
        this.zPos = z;
    }

    private int wide;

    public int getWidth() {
        return wide;
    }

    public void setWidth(int wide) {
        this.wide = wide;
    }

    private int tall;

    public int getHeight() {
        return tall;
    }

    public void setHeight(int tall) {
        this.tall = tall;
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

    public void validate() {
//        System.out.println("validate()");
        for(Map.Entry<KVPair<String, String>, String> entry : this.getProps().entrySet()) {
            String k = entry.getKey().getKey();
            if(k != null && k.contains("\"")) {
                k = k.substring(1, k.length() - 1);
            }
            String v = entry.getKey().getValue();
            if(v != null && v.contains("\"")) {
                v = v.substring(1, v.length() - 1);
            }
            String i = entry.getValue();
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
                }
                this.setWidth(Integer.parseInt(v));
            } else if("tall".equalsIgnoreCase(k)) {
                if(v.startsWith("f")) {
                    v = v.substring(1);
                }
                this.setHeight(Integer.parseInt(v));
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