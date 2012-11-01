package com.timepath.tf2.hudedit.loaders;

import com.timepath.tf2.hudedit.loaders.VtfFile.Format;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    public void test() {
        class ImagePreviewPanel extends JPanel implements PropertyChangeListener {
            
            private int width, height;
            private Image image;
            private static final int ACCSIZE = 128;
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
                        try {
                            Image i = VtfFile.load(selection).getImage(0);
                            image = (i != null ? i : new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB));
                            scaleImage();
                            repaint();
                        } catch (IOException ex) {
                            Logger.getLogger(VtfLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }
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
        
        class VtfFileFilter extends FileFilter {
            
            VtfFileFilter(Format format) {
                this.vtfFormat = format;
            }
            
            Format vtfFormat;
            
            @Override
            public boolean accept(File file) {
                return (file.isDirectory() || (file.getName().toLowerCase().endsWith(".vtf") && (vtfFormat == VtfFile.Format.IMAGE_FORMAT_NONE || VtfFile.load(file).format == vtfFormat)));
            }
            
            @Override
            public String getDescription() {
                return "VTF (" + (vtfFormat != Format.IMAGE_FORMAT_NONE ? vtfFormat.name() : "All") + ")";
            }
        }
        
        JFileChooser chooser = new JFileChooser("./res/vtf/hud/");
        chooser.setFileFilter(new VtfFileFilter(Format.IMAGE_FORMAT_NONE));
        chooser.addChoosableFileFilter(new VtfFileFilter(Format.IMAGE_FORMAT_DXT1));
        chooser.addChoosableFileFilter(new VtfFileFilter(Format.IMAGE_FORMAT_DXT5));
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
                try {
                    Image image = VtfFile.load(subs[i]).getImage(0);
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
                } catch (IOException ex) {
                    Logger.getLogger(VtfLoader.class.getName()).log(Level.SEVERE, null, ex);
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

}