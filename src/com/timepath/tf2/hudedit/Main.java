package com.timepath.tf2.hudedit;

import com.timepath.tf2.hudedit.temp.JarClassLoader;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 *
 * @author andrew
 */
public class Main {
    
    public final static String appName = "TF2 HUD Editor";
    
    public final static String projectName = "tf2-hud-editor";
    
    public final static String javaName = "com-timepath-tf2-hudedit";
    
    public final static OS os;

    public final static int shortcutKey;
    
    private static Preferences p = Preferences.userRoot().node(projectName);

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
        
        shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        
        if(os == OS.Windows) {
            
        } else if(os == OS.Mac) {
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
            boolean force = "Unity".equals(System.getenv("XDG_CURRENT_DESKTOP"));
            if(force) {
                System.setProperty("jayatana.force", "true");
            }
            System.setProperty("jayatana.startupWMClass", javaName);
            
            // http://www.ailis.de/~k/archives/67-Workaround-for-borderless-Java-Swing-menus-on-Linux.html
            
            try {
                Toolkit xToolkit = Toolkit.getDefaultToolkit();
                java.lang.reflect.Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
                awtAppClassNameField.setAccessible(true);
                awtAppClassNameField.set(xToolkit, javaName);
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
            
        }
    }
    
    private static void createLinuxLauncher() {
        File src = new File("res/bin/" + javaName + ".desktop");
        File dest = new File(System.getProperty("user.home") + "/.local/share/applications/" + javaName + ".desktop");
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
    
    private static void startServer(int port, String[] args) {        
        try {
            final ServerSocket server = new ServerSocket(port);
//            server.setSoTimeout(1);
            port = server.getLocalPort(); // if the port was 0, update it
            p.putInt("port", port);
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("Shutting down...");
//                    System.exit(0);
                }
            });
            
            startProgram(args);
            
            Thread listener = new Thread(new Runnable() {
                public void run() {
                    while(!server.isClosed()) {
                        try {
                            server.accept();
                            System.out.println("A client connected!");
                            startProgram("noupdate");
                        } catch(SocketTimeoutException e) {
                            
                        } catch(IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            listener.setDaemon(true);
            listener.start();
            
        } catch (IOException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Program already running, communicating...");
            startClient(port, args);
        }
    }
    
    private static void startClient(int port, String[] args) {
        try {
            Socket client = new Socket("localhost", port);
            client.sendUrgentData(1);
        } catch (IOException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex1);
            System.out.println("Program closed suddenly");
            startServer(port, args);
        }
    }
    
    private static void startProgram(String... args) {
        JarClassLoader cl = new JarClassLoader();
        try {
            cl.invokeMain("com.timepath.tf2.hudedit.swing.EditorFrame", args);
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        // http://www.ailis.de/~k/archives/64-How-to-implement-a-Single-Instance-Application-in-Java.html
        // http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
        // http://www.javaworld.com/jw-12-1996/jw-12-sockets.html?page=3
        startServer(p.getInt("port", 0), args);
    }
    
}