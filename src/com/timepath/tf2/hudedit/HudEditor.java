package com.timepath.tf2.hudedit;

import java.awt.event.ActionEvent;
import net.tomahawk.XFileDialog;

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
            try {
                XFileDialog.setTraceLevel(0);
            } catch(UnsatisfiedLinkError e) {
                System.out.println("java.library.path = " + System.getProperty("java.library.path"));
            }
        } else if(os == OS.Mac) {
            shortcutKey = ActionEvent.META_MASK;
            System.setProperty("apple.awt.brushMetalLook", "false"); // looks stupid
            System.setProperty("apple.awt.showGrowBox", "false"); // looks stupid
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
        MyClassLoader cl = new MyClassLoader();
        try {
            cl.run("com.timepath.tf2.hudedit.EditorFrame", args);
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
    
}
