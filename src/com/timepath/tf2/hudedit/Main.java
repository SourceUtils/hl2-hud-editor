package com.timepath.tf2.hudedit;

import com.timepath.tf2.hudedit.temp.JarClassLoader;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrew
 */
public class Main {
    
    public final static String appName = "TF2 HUD Editor";
    
    public final static OS os;

    public final static int shortcutKey;

    public enum OS {

        Windows, Mac, Linux, Other

    }

    // Executed on class load
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
        } else if(os == OS.Mac) {
            shortcutKey = ActionEvent.META_MASK;
            System.setProperty("apple.awt.brushMetalLook", "false"); // looks stupid
            System.setProperty("apple.awt.showGrowBox", "false"); // looks stupid
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.smallTabs", "true");
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName); // -Xdock:name="TF2 HUD Editor"
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        } else if(os == OS.Linux) {
            shortcutKey = ActionEvent.CTRL_MASK;
            boolean force = "Unity".equals(System.getenv("XDG_CURRENT_DESKTOP"));
            if(force) {
                System.setProperty("jayatana.force", "true");
            }
            System.setProperty("jayatana.startupWMClass", "com-timepath-tf2-hudedit");
            
            try {
                Toolkit xToolkit = Toolkit.getDefaultToolkit();
                java.lang.reflect.Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
                awtAppClassNameField.setAccessible(true);
                awtAppClassNameField.set(xToolkit, "com-timepath-tf2-hudedit");
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            createLinuxLauncher();
        } else {
            shortcutKey = ActionEvent.CTRL_MASK;
        }
    }
    
    private static void createLinuxLauncher() {
        File src = new File("res/bin/com-timepath-tf2-hudedit.desktop");
        File dest = new File(System.getProperty("user.home") + "/.local/share/applications/com-timepath-tf2-hudedit.desktop");
        if(src.exists() && !dest.exists()) {
            try {
                copyFile(src, dest);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
            destFile.setExecutable(false);
        }
    }
    
    public static void main(final String[] args) {
        JarClassLoader cl = new JarClassLoader();
        try {
            cl.invokeMain("com.timepath.tf2.hudedit.swing.EditorFrame", args);
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
    
}