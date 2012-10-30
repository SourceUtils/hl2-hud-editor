package com.timepath.tf2.hudedit.loaders;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

/**
 * common: DXT5, DXT1, BGRA8888, UV88
 * http://www.piksel.org/frei0r/1.1/spec/group__COLOR__MODEL.html
 * http://code.google.com/p/gimp-vtf/source/browse/trunk/gimp-vtf/?r=16
 * http://msdn.microsoft.com/en-us/library/windows/desktop/bb205578(v=vs.85).aspx
 * http://msdn.microsoft.com/en-us/library/aa920432.aspx
 * http://atlantica.wikia.com/wiki/Forum:Ndoors_DDS_image_format_dissection
 * http://doc.51windows.net/Directx9_SDK/?url=/directx9_sdk/graphics/reference/DDSFileReference/ddstextures.htm
 * 
 * @author andrew
 */
public class VtfLoader {
    
    public VtfLoader() {
        
    }
    
    public void test() {
        class ImagePreviewPanel extends JPanel implements PropertyChangeListener {

            private int width, height;
            private Image image;
            private static final int ACCSIZE = 256;
            private Color bg;

            public ImagePreviewPanel() {
                setPreferredSize(new Dimension(ACCSIZE, -1));
                bg = getBackground();
            }

            public void propertyChange(PropertyChangeEvent e) {
                String propertyName = e.getPropertyName();

                // Make sure we are responding to the right event.
                if(propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    File selection = (File)e.getNewValue();
                    String name;

                    if(selection == null) {
                        return;
                    } else {
                        name = selection.getAbsolutePath();
                    }

                    /*
                     * Make reasonably sure we have an image format that AWT can
                     * handle so we don't try to draw something silly.
                     */
                    if((name != null) && name.toLowerCase().endsWith(".vtf")) {
                        Image i = new VtfLoader().load(name);
                        image = (i != null ? i : new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB));
                        scaleImage();
                        repaint();
                    }
                }
            }

            private void scaleImage() {
                width = image.getWidth(this);
                height = image.getHeight(this);
                double ratio = 1.0;

                /* 
                 * Determine how to scale the image. Since the accessory can expand
                 * vertically make sure we don't go larger than ACCSIZE when scaling
                 * vertically.
                 */
                if (width >= height) {
                    ratio = (double)(ACCSIZE-5) / width;
                    width = ACCSIZE-5;
                    height = (int)(height * ratio);
                }
                else {
                    if (getHeight() > ACCSIZE) {
                        ratio = (double)(ACCSIZE-5) / height;
                        height = ACCSIZE-5;
                        width = (int)(width * ratio);
                    }
                    else {
                        ratio = (double)getHeight() / height;
                        height = getHeight();
                        width = (int)(width * ratio);
                    }
                }

                image = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
            }

            @Override
            public void paintComponent(Graphics g) {
                g.setColor(bg);

                /*
                 * If we don't do this, we will end up with garbage from previous
                 * images if they have larger sizes than the one we are currently
                 * drawing. Also, it seems that the file list can paint outside
                 * of its rectangle, and will cause odd behavior if we don't clear
                 * or fill the rectangle for the accessory before drawing. This might
                 * be a bug in JFileChooser.
                 */
                g.fillRect(0, 0, ACCSIZE, getHeight());
                g.drawImage(image, getWidth() / 2 - width / 2 + 5, getHeight() / 2 - height / 2, this);
            }

        }
        
        JFileChooser chooser = new JFileChooser("./res/vtf/hud/");
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.getName().toLowerCase().endsWith(".vtf") || file.isDirectory();
            }

            @Override
            public String getDescription() {
                return "VTF";
            }
            
        });
        ImagePreviewPanel preview = new ImagePreviewPanel();
        chooser.setAccessory(preview);
        chooser.addPropertyChangeListener(preview);
        chooser.showOpenDialog(null);
    }
    
    public void test2() {
        JFrame f = new JFrame("Vtf Loader");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f.setLayout(new FlowLayout(FlowLayout.LEFT));
//        f.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        
        JScrollPane jsp = new JScrollPane();
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        f.add(jsp);
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        jsp.setViewportView(pane);
        
        boolean init = false;
        File root = new File("./res/vtf/hud/");
        File[] subs = root.listFiles();
        for(int i = 0; i < subs.length; i++) {
            if(subs[i].getName().endsWith(".vtf")) {
                Image image = new VtfLoader().load(subs[i].getPath());
                if(image != null) {
                    JPanel p = new JPanel(new BorderLayout());
                    p.setBackground(Color.GRAY);
                    p.setSize(image.getWidth(null), image.getHeight(null));
                    JLabel l = new JLabel();
                    p.setToolTipText(subs[i].getName());
                    l.setIcon(new ImageIcon(image));
                    p.add(l, BorderLayout.CENTER);
                    p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    pane.add(p);
                    jsp.invalidate();
                    jsp.validate();
                    jsp.repaint();
                    
                    if(!init) {
                        f.setVisible(true);
                        f.pack();
                        init = true;
                    }
//                    Thread.sleep(5000);
                }
            }
        }
        if(!init) {
            f.setVisible(true);
            f.pack();
            init = true;
        }
    }
    
    public static void main(String... args) throws InterruptedException {
        new VtfLoader().test();
    }
    
    public class VtfFile {
        
        public VtfFile() {
            
        }
        
        /**
         * 8 bytes
         */
        int[] version;
        
        /**
         * 2 bytes
         */
        int width;
        
        /**
         * 2 bytes
         */
        int height;
        
        /**
         * 4 bytes
         */
        int flags;
        
        /**
         * 2 bytes
         */
        int frameCount;
        
        /**
         * 2 bytes
         * Zero indexed
         */
        int frameFirst;
        
        /**
         * 12 bytes
         */
        float[] reflectivity;
        
        /**
         * 4 bytes
         */
        float bumpScale;
        
        /**
         * 4 bytes
         */
        Format format;
        
        /**
         * 1 byte
         */
        int mipCount;
        
        /**
         * 4 bytes
         */
        Format thumbFormat;
        
        /**
         * 1 byte
         */
        int thumbWidth;
        
        /**
         * 1 byte
         */
        int thumbHeight;
        
        /**
         * 1 byte
         * The documentation says 2, but I don't think so...
         */
        int depth;
        
    }
    
    public Image load(String path) {
        RandomAccessFile file;
        try {
            file = new RandomAccessFile(path, "r");
            System.out.println("Loading " + path + "...");
            String signature = new String(new byte[] {readChar(file), readChar(file), readChar(file), readChar(file)});
            if(!(signature.equals("VTF\0"))) {
                System.err.println("Invalid VTF file " + path);
            }
            VtfFile vtf = new VtfFile();
            vtf.version = new int[] {readUInt(file), readUInt(file)};
            int headerSize = readUInt(file);
            vtf.width = readUShort(file);
            vtf.height = readUShort(file);
            vtf.flags = readUInt(file);
            vtf.frameCount = readUShort(file);
            vtf.frameFirst = readUShort(file);
            file.skipBytes(4); // padding
            vtf.reflectivity = new float[] {readFloat(file), readFloat(file), readFloat(file)};
            file.skipBytes(4); // padding
            vtf.bumpScale = readFloat(file);
            vtf.format = Format.getEnumForIndex(readUInt(file));
            vtf.mipCount = readUChar(file);
            vtf.thumbFormat = Format.getEnumForIndex(readUInt(file));
            vtf.thumbWidth = readUChar(file);
            vtf.thumbHeight = readUChar(file);
            vtf.depth = readUByte(file); // the 64th byte

            if(vtf.format != Format.IMAGE_FORMAT_DXT1 && vtf.format != Format.IMAGE_FORMAT_DXT5) {
                System.err.println("Unrecognised VTF format " + vtf.format);
                return null;
            }
//            System.err.println("USEMIPS=" + !((flags & Flags.TEXTUREFLAGS_NOMIP.getMask()) == 0xff));
          if(vtf.frameCount > 1) {
              System.err.println("FRAMES = " + vtf.frameCount); // zero indexed
              if(vtf.frameFirst != 0) {
                  System.err.println("FIRSTFRAME = " + vtf.frameFirst); // zero indexed
              }
          }
          
//          file.seek(headerSize);
          file.skipBytes(headerSize - 64 - 8); // 64 for all the above info, 8 for CRC or other things. I have no idea what the data inbetween does          
          
          //<editor-fold defaultstate="collapsed" desc="CRC">
          String crcHead = new String(new byte[] {readChar(file), readChar(file), readChar(file), readChar(file)});
          int crc = readLong(file);
          
          if(!(crcHead.equals("CRC\2"))) {
              System.err.println("CRC=" + crcHead + ", invalid");
          } else {
//              System.err.println("CRC=0x" + Integer.toHexString(crc).toUpperCase());
          }
          //</editor-fold>
          
          //<editor-fold defaultstate="collapsed" desc="Thumbnail">
          byte[] thumbData = new byte[(vtf.thumbWidth * vtf.thumbHeight) / 2]; // DXT1. Each 'block' is 4*4 pixels. 16 pixels become 8 bytes
          file.read(thumbData);
          BufferedImage thumbImage = loadDXT1(thumbData, vtf.thumbWidth, vtf.thumbHeight);
//          System.out.println(thumbData.length);
          //</editor-fold>
          
          BufferedImage image = null;
          
          int scale = 2;
          
          int[] sizesX = new int[vtf.mipCount]; // largest -> smallest {64, 32, 16, 8, 4, 2, 1}
          int[] sizesY = new int[vtf.mipCount];
          for(int n = 0; n < vtf.mipCount; n++) {
              sizesX[n] = Math.max((vtf.width >> n), 1);
              sizesY[n] = Math.max((vtf.height >> n), 1);
          }
          for(int i = 0; i < vtf.mipCount; i++) {
              int w = sizesX[vtf.mipCount - i - 1];
              int h = sizesY[vtf.mipCount - i - 1];
              
              if(vtf.format == Format.IMAGE_FORMAT_DXT1) {
                  byte[] imageData = new byte[Math.max((w * h) / 2, 8)]; // DXT1. Each 'block' is 4*4 pixels + some other data. 16 pixels become 8 bytes [64 bits] (2 * 16 bit colours, 4*4 2 bit indicies)
                  file.read(imageData);
                  
                  image = new BufferedImage(w * scale, h * scale, BufferedImage.TYPE_INT_ARGB);
                  Graphics2D g = (Graphics2D) image.getGraphics();
                  g.drawImage(loadDXT1(imageData, w, h), 0, 0, w * scale, h * scale, null);
//                  System.out.println(imageData.length);
              } else if(vtf.format == Format.IMAGE_FORMAT_DXT5) {
                  byte[] imageData = new byte[Math.max(w * h, 16)]; // DXT5. Each 'block' is 4*4 pixels + some other data. 16 pixels become 16 bytes [128 bits] (2 * 8 bit alpha values, 4x4 3 bit alpha indicies, 2 * 16 bit colours, 4*4 2 bit indicies)
                  file.read(imageData);
                  
                  image = new BufferedImage(w * scale, h * scale, BufferedImage.TYPE_INT_ARGB);
                  Graphics2D g = (Graphics2D) image.getGraphics();
                  g.drawImage(loadDXT5(imageData, w, h), 0, 0, w * scale, h * scale, null);
//                  System.out.println(imageData.length);
              }
              
          }
          
          System.err.println("L:" + file.length() + ", P:" + file.getFilePointer() + ", R:" + (file.length() - file.getFilePointer()));
          System.err.print("\n");
          
          file.close();
          
          System.out.println("Loaded " + path + "!");
          
          return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    static int nextPowerOf2(int n) {
       n |= (n >> 16);
       n |= (n >> 8);
       n |= (n >> 4);
       n |= (n >> 2);
       n |= (n >> 1);
       ++n;
       return n;
    }
    
    /**
     * http://en.wikipedia.org/wiki/S3_Texture_Compression
     * http://www.fsdeveloper.com/wiki/index.php?title=DXT_compression_explained
     * http://msdn.microsoft.com/en-us/library/aa920432.aspx
     * 
     * 8 bytes per 4*4
     */
    BufferedImage loadDXT1(byte[] b, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        int pos = 0;
        
        int red_mask_565 = 0xF800; // first 5 bits
        int green_mask_565 = 0x7E0; // next 6 bits
        int blue_mask_565 = 0x1F;
        
        int red_mask_555 = 0x7C00; // first 5 bits
        int green_mask_555 = 0x3E0; // next 5 bits
        int blue_mask_555 = 0x1F;
        
        int alpha_mask_555 = 0x1;
        
        int bits_12 = 0xC0; // first 2 bits
        int bits_34 = 0x30; // next 2 bits
        int bits_56 = 0xC; // next 2 bits
        int bits_78 = 0x3; // last 2 bits

//        RGB 565: WORD pixel565 = (red_value << 11) | (green_value << 5) | blue_value;
//        RGB 555: WORD pixel565 = (red << 10) | (green << 5) | blue;
        
        for(int y = 0; y < (height / 4); y++) {
            for(int x = 0; x < (width / 4); x++) {
                int color_0 = (b[pos++] & 0xff) + ((b[pos++] & 0xff) << 8); // 2 bytes
                int color_1 = (b[pos++] & 0xff) + ((b[pos++] & 0xff) << 8); // 2 bytes
                
                int red1, green1, blue1, red2, green2, blue2;
                Color c1, c2;
                
                if(color_0 < color_1) { // 3 colours + transparency : 5551 rgba
                    red1   = (int) (((color_0 & red_mask_555) >> 10) << 3);
                    green1 = (int) (((color_0 & green_mask_555) >> 5) << 3);
                    blue1  = (int) ((color_0 & blue_mask_555) << 3);
                    int alpha1 = (int) ((color_0 & alpha_mask_555) << 7);
                    c1 = new Color(red1, green1, blue1, alpha1);

                    red2   = (int) (((color_1 & red_mask_555) >> 10) << 3);
                    green2 = (int) (((color_1 & green_mask_555) >> 5) << 3);
                    blue2  = (int) ((color_1 & alpha_mask_555) << 7);
                    int alpha2 = 0;
                    c2 = new Color(red2, green2, blue2, alpha2);
                } else { // 4 colours : 565 rgb
                    red1   = (int) (((color_0 & red_mask_565) >> 11) << 3);
                    green1 = (int) (((color_0 & green_mask_565) >> 5) << 2);
                    blue1  = (int) ((color_0 & blue_mask_565) << 3);
                    c1 = new Color(red1, green1, blue1);

                    red2   = (int) (((color_1 & red_mask_565) >> 11) << 3);
                    green2 = (int) (((color_1 & green_mask_565) >> 5) << 2);
                    blue2  = (int) ((color_1 & blue_mask_565) << 3);
                    c2 = new Color(red2, green2, blue2);
                }

                
                
                // remaining 4 bytes
                for(int y1 = 0; y1 < 4; y1++) { // 16 bits / 4 lines = 4 bits/line = 1 byte/line
                    byte next4 = b[pos++];
                    int[] bits = new int[]{(next4 & bits_12) >> 6, (next4 & bits_34) >> 4, (next4 & bits_56) >> 2, next4 & bits_78};
                    
                    for(int i = 0; i < 4; i++) { // horizontal scan
                        int bit = bits[i];
                        if(bit == 0) {
                            g.setColor(c1);
                        } else if(bit == 1) {
                            g.setColor(c2);
                        } else if(color_0 < color_1) { // transparent
                            if(bit == 2) {                            
                                int cred = (c1.getRed() / 2) + (c2.getRed() / 2);
                                int cgrn = (c1.getGreen() / 2) + (c2.getGreen() / 2);
                                int cblu = (c1.getBlue() / 2) + (c2.getBlue() / 2);
                                int calp = (c1.getAlpha() / 2) + (c2.getAlpha() / 2);
                                Color c = new Color(cred, cgrn, cblu, calp);
                                g.setColor(c);
                            } else if(bit == 3) {
                                Color c = new Color(0, 0, 0, 0); // transparent
                                g.setColor(c);
                            }
                        } else {
                            if(bit == 2) {                            
                                int cred = (2 * (c1.getRed() / 3)) + (c2.getRed() / 3);
                                int cgrn = (2 * (c1.getGreen() / 3)) + (c2.getGreen() / 3);
                                int cblu = (2 * (c1.getBlue() / 3)) + (c2.getBlue() / 3);
                                Color c = new Color(cred, cgrn, cblu);
                                g.setColor(c);
                            } else if(bit == 3) {
                                int cred = (c1.getRed() / 3) + (2 * (c2.getRed() / 3));
                                int cgrn = (c1.getGreen() / 3) + (2 * (c2.getGreen() / 3));
                                int cblu = (c1.getBlue() / 3) + (2 * (c2.getBlue() / 3));
                                Color c = new Color(cred, cgrn, cblu);
                                g.setColor(c);
                            }
                        }
                        g.drawLine((x * 4) + 4 - i, (y * 4) + y1,
                                   (x * 4) + 4 - i, (y * 4) + y1);
                    }
                }
            }
        }
        return bi;
    }
    
    /**
     * http://en.wikipedia.org/wiki/S3_Texture_Compression
     * http://www.fsdeveloper.com/wiki/index.php?title=DXT_compression_explained
     * http://msdn.microsoft.com/en-us/library/aa920432.aspx
     * 
     * 8 bytes for alpha channel, additional 8 per 4*4 chunk
     * 
     * TODO: fully implement correct colours
     */
    BufferedImage loadDXT5(byte[] b, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        int pos = 0;
        
        int red_mask_565 = 0xF800; // first 5 bits
        int green_mask_565 = 0x7E0; // next 6 bits
        int blue_mask_565 = 0x1F;
        
        int bits_12 = 0xC0; // first 2 bits
        int bits_34 = 0x30; // next 2 bits
        int bits_56 = 0xC; // next 2 bits
        int bits_78 = 0x3; // last 2 bits
//        RGB 565: WORD pixel565 = (red_value << 11) | (green_value << 5) | blue_value;
        
        int xBlocks = (width / 4);
        if(xBlocks < 1) {
            xBlocks = 1;
        }
        int yBlocks = (height / 4);
        if(yBlocks < 1) {
            yBlocks = 1;
        }
        
//        System.err.println("SIZE="+xBlocks+", "+yBlocks+" = " + b.length);
        for(int y = 0; y < yBlocks; y++) {
            for(int x = 0; x < xBlocks; x++) {
                pos += 8; // 64 bits of alpha channel data (two 8 bit alpha values and a 4x4 3 bit lookup table) 
                int color_0 = (b[pos] & 0xff) + ((b[pos+1] & 0xff) << 8); // 2 bytes
                pos += 2;
                int color_1 = (b[pos] & 0xff) + ((b[pos+1] & 0xff) << 8); // 2 bytes
                pos += 2;
                
                int red1, green1, blue1, red2, green2, blue2;
                Color c1, c2;
                
                red1 = (int) (((color_0 & red_mask_565) >> 11) << 3);
                green1 = (int) (((color_0 & green_mask_565) >> 5) << 2);
                blue1 = (int) ((color_0 & blue_mask_565) << 3);
                c1 = new Color(red1, green1, blue1);

                red2 = (int) (((color_1 & red_mask_565) >> 11) << 3);
                green2 = (int) (((color_1 & green_mask_565) >> 5) << 2);
                blue2 = (int) ((color_1 & blue_mask_565) << 3);
                c2 = new Color(red2, green2, blue2);
                
                // remaining 4 bytes
                byte[] next4 = {b[pos], b[pos+1], b[pos+2], b[pos+3]};
                pos += 4;
                for(int y1 = 0; y1 < 4; y1++) { // 16 bits / 4 lines = 4 bits/line = 1 byte/line
                    int[] bits = new int[]{(next4[y1] & bits_12) >> 6, (next4[y1] & bits_34) >> 4, (next4[y1] & bits_56) >> 2, next4[y1] & bits_78};
                    
                    for(int i = 0; i < 4; i++) { // horizontal scan
                        int bit = bits[i];
                        if(bit == 0) {
                            g.setColor(c1);
                        } else if(bit == 1) {
                            g.setColor(c2);
                        } else if(bit == 2) {                            
                            int cred = (2 * (c1.getRed() / 3)) + (c2.getRed() / 3);
                            int cgrn = (2 * (c1.getGreen() / 3)) + (c2.getGreen() / 3);
                            int cblu = (2 * (c1.getBlue() / 3)) + (c2.getBlue() / 3);
                            Color c = new Color(cred, cgrn, cblu);
                            g.setColor(c);
                        } else if(bit == 3) {
                            int cred = (c1.getRed() / 3) + (2 * (c2.getRed() / 3));
                            int cgrn = (c1.getGreen() / 3) + (2 * (c2.getGreen() / 3));
                            int cblu = (c1.getBlue() / 3) + (2 * (c2.getBlue() / 3));
                            Color c = new Color(cred, cgrn, cblu);
                            g.setColor(c);
                        }
                        g.drawLine((x * 4) + 4 - i, (y * 4) + y1,
                                   (x * 4) + 4 - i, (y * 4) + y1);
                    }
                }
            }
        }
        return bi;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Helpers">
    private static enum Format {
        IMAGE_FORMAT_NONE(-1),
        IMAGE_FORMAT_RGBA8888(0),
        IMAGE_FORMAT_ABGR8888(1),
        IMAGE_FORMAT_RGB888(2),
        IMAGE_FORMAT_BGR888(3),
        IMAGE_FORMAT_RGB565(4),
        IMAGE_FORMAT_I8(5),
        IMAGE_FORMAT_IA88(6),
        IMAGE_FORMAT_P8(7),
        IMAGE_FORMAT_A8(8),
        IMAGE_FORMAT_RGB888_BLUESCREEN(9),
        IMAGE_FORMAT_BGR888_BLUESCREEN(10),
        IMAGE_FORMAT_ARGB8888(11),
        IMAGE_FORMAT_BGRA8888(12),
        IMAGE_FORMAT_DXT1(13),
        IMAGE_FORMAT_DXT3(14),
        IMAGE_FORMAT_DXT5(15),
        IMAGE_FORMAT_BGRX8888(16),
        IMAGE_FORMAT_BGR565(17),
        IMAGE_FORMAT_BGRX5551(18),
        IMAGE_FORMAT_BGRA4444(19),
        IMAGE_FORMAT_DXT1_ONEBITALPHA(20),
        IMAGE_FORMAT_BGRA5551(21),
        IMAGE_FORMAT_UV88(22),
        IMAGE_FORMAT_UVWQ8888(23),
        IMAGE_FORMAT_RGBA16161616F(24),
        IMAGE_FORMAT_RGBA16161616(25),
        IMAGE_FORMAT_UVLX8888(26);
        
        private int index;
        
        private Format(int index) {
            this.index = index;
        }
        
        public int getIndex() {
            return index;
        }
        
        public static Format getEnumForIndex(int index) {
            Format[] values = Format.values();
            for(Format eachValue : values) {
                if(eachValue.getIndex() == index) {
                    return eachValue;
                }
            }
            return null;
        }
        
    }
    
    /**
     * https://developer.valvesoftware.com/wiki/Valve_Texture_Format#Image_flags
     */
    private static enum Flags {
        // Flags from the *.txt config file
        TEXTUREFLAGS_POINTSAMPLE(0x00000001),
        TEXTUREFLAGS_TRILINEAR(0x00000002),
        TEXTUREFLAGS_CLAMPS(0x00000004),
        TEXTUREFLAGS_CLAMPT(0x00000008),
        TEXTUREFLAGS_ANISOTROPIC(0x00000010),
        TEXTUREFLAGS_HINT_DXT5(0x00000020),
        TEXTUREFLAGS_PWL_CORRECTED(0x00000040),
        TEXTUREFLAGS_NORMAL(0x00000080),
        TEXTUREFLAGS_NOMIP(0x00000100),
        TEXTUREFLAGS_NOLOD(0x00000200),
        TEXTUREFLAGS_ALL_MIPS(0x00000400),
        TEXTUREFLAGS_PROCEDURAL(0x00000800),
        
        // These are automatically generated by vtex from the texture data.
        TEXTUREFLAGS_ONEBITALPHA(0x00001000),
        TEXTUREFLAGS_EIGHTBITALPHA(0x00002000),
        
        // Newer flags from the *.txt config file
        TEXTUREFLAGS_ENVMAP(0x00004000),
        TEXTUREFLAGS_RENDERTARGET(0x00008000),
        TEXTUREFLAGS_DEPTHRENDERTARGET(0x00010000),
        TEXTUREFLAGS_NODEBUGOVERRIDE(0x00020000),
        TEXTUREFLAGS_SINGLECOPY(0x00040000),
        TEXTUREFLAGS_PRE_SRGB(0x00080000),
        
        TEXTUREFLAGS_UNUSED_00100000(0x00100000),
        TEXTUREFLAGS_UNUSED_00200000(0x00200000),
        TEXTUREFLAGS_UNUSED_00400000(0x00400000),
        
        TEXTUREFLAGS_NODEPTHBUFFER(0x00800000),
        
        TEXTUREFLAGS_UNUSED_01000000(0x01000000),
        
        TEXTUREFLAGS_CLAMPU(0x02000000),
        TEXTUREFLAGS_VERTEXTEXTURE(0x04000000),
        TEXTUREFLAGS_SSBUMP(0x08000000),
        
        TEXTUREFLAGS_UNUSED_10000000(0x10000000),
        
        TEXTUREFLAGS_BORDER(0x20000000),
        
        TEXTUREFLAGS_UNUSED_40000000(0x40000000),
        TEXTUREFLAGS_UNUSED_80000000(0x80000000);
        
        private int mask;
        
        private Flags(int mask) {
            this.mask = mask;
        }
        
        public int getMask() {
            return mask;
        }
        
        public static Flags getEnumForMask(int mask) {
            Flags[] values = Flags.values();
            for(Flags eachValue : values) {
                if(eachValue.getMask() == mask) {
                    return eachValue;
                }
            }
            return null;
        }
    }
    
    byte readByte(RandomAccessFile f) throws IOException {
        byte b = f.readByte();
        //        System.err.println(Integer.toHexString(new Byte(b)).toUpperCase());
        return b;
    }
    
    int readUByte(RandomAccessFile f) throws IOException {
        return readByte(f) & 0xff;
    }
    
    byte readChar(RandomAccessFile f) throws IOException {
        return readByte(f);
    }
    
    int readUChar(RandomAccessFile f) throws IOException {
        return readUByte(f);
    }
    
    /**
     * all Little endian - least significant bits first
     * @param f
     * @return
     * @throws IOException
     */
    int readUInt(RandomAccessFile f) throws IOException {
        return readUByte(f) + (readUByte(f) << 8) + (readUByte(f) << 16) + (readUByte(f) << 24);
    }
    
    float readFloat(RandomAccessFile f) throws IOException {
        int intBits = readUByte(f) + (readUByte(f) << 8) + (readUByte(f) << 16) + (readUByte(f) << 24);
        return Float.intBitsToFloat(intBits);
    }
    
    int readUShort(RandomAccessFile f) throws IOException {
        return readUByte(f) + (readUByte(f) << 8);
    }
    
    int readLong(RandomAccessFile f) throws IOException {
        return readByte(f) + (readByte(f) << 8) + (readByte(f) << 16) + (readByte(f) << 24);
    }
    
    String toBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    }
    //</editor-fold>

}