package com.timepath.tf2.hudedit;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author andrew
 */
public class HudEditor {
    
    public final static OS os;

    public final static int shortcutKey;

    public enum OS {

        Windows, Mac, Linux, Other

    }

    static {
        String osVer = System.getProperty("os.name").toLowerCase();
        if(osVer.indexOf("windows") != -1) {
            os = OS.Windows;
        } else if(osVer.indexOf("OS X") != -1 || osVer.indexOf("mac") != -1) {
            os = OS.Mac;
        } else if(osVer.indexOf("linux") != -1) {
            os = OS.Linux;
        } else {
            os = OS.Other;
            System.out.println("Unrecognised OS: " + osVer);
        }
        
        if(os == OS.Windows) {
            shortcutKey = ActionEvent.CTRL_MASK;
//            XFileDialog.setTraceLevel(0);
        } else if(os == OS.Mac) {
            shortcutKey = ActionEvent.META_MASK;
//            System.setProperty("apple.awt.brushMetalLook", "false");
            System.setProperty("apple.awt.showGrowBox", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.smallTabs", "true");
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TF2 HUD Editor"); // -Xdock:name="TF2 HUD Editor"
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        } else if(os == OS.Linux) {
            shortcutKey = ActionEvent.CTRL_MASK;
        } else {
            shortcutKey = ActionEvent.CTRL_MASK;
        }
    }
    
    public static void main(String... args) {
        //<editor-fold defaultstate="collapsed" desc="Try and get nimbus look and feel, if it is installed.">
//        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        initialLaf();
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Display the editor">
        SwingUtilities.invokeLater(new Runnable() { // SwingUtilities vs EventQueue?
            
            @Override
            public void run() {
                EditorFrame frame = new EditorFrame();
                frame.setVisible(true);
            }
            
        });
        //</editor-fold>
    }
    
    private static void initialLaf() {
        try {
            for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            systemLaf();
        } catch(Exception ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    private static void systemLaf() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
}
