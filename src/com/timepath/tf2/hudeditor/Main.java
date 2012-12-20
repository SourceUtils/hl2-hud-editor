package com.timepath.tf2.hudeditor;

//<editor-fold defaultstate="collapsed" desc="imports">
import com.timepath.tf2.hudeditor.gui.EditorFrame;
import com.timepath.tf2.hudeditor.plaf.OS;
import com.timepath.tf2.hudeditor.plaf.linux.GtkFixer;
import com.timepath.tf2.hudeditor.plaf.linux.LinuxDesktopLauncher;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.tomahawk.XFileDialog;
//</editor-fold>

/**
 *
 * @author TimePath
 */
public class Main {
    
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    public final static String appName = "TF2 HUD Editor";
    
    public final static String javaName = "com-timepath-tf2-hudeditor";
    
    public final static String projectName = "tf2-hud-editor";
    
    public final static OS os;
    
    static {
        String osVer = System.getProperty("os.name").toLowerCase();
        if(osVer.indexOf("windows") != -1) {
            os = OS.Windows;
        } else if(osVer.indexOf("OS X") != -1 || osVer.indexOf("mac") != -1) {
            os = OS.Mac;
        } else if(osVer.indexOf("nix") != -1 || osVer.indexOf("nux") != -1) {
            os = OS.Linux;
        } else {
            os = OS.Other;
            logger.log(Level.WARNING, "Unrecognised OS: {0}", osVer);
        }
        
        if(os == OS.Mac) {
            System.setProperty("apple.awt.brushMetalLook", "false");
            System.setProperty("apple.awt.graphics.EnableQ2DX", "true"); 
            System.setProperty("apple.awt.showGrowBox", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.smallTabs", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        } else if(os == OS.Windows) {
            try {
                XFileDialog.setTraceLevel(0);
            } catch(UnsatisfiedLinkError e) {
                logger.warning(e.toString());
            } catch(UnsupportedClassVersionError e) {
            }
        } else if(os == OS.Linux) {
            boolean force = "Unity".equals(System.getenv("XDG_CURRENT_DESKTOP"));
            if(force) {
                System.setProperty("jayatana.force", "true");
            }
            System.setProperty("jayatana.startupWMClass", javaName);
            
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
            LinuxDesktopLauncher.create();
        } else {
            // Nothing special -- unsupported OS
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Reading: " + System.getenv());
        // http://www.ailis.de/~k/archives/64-How-to-implement-a-Single-Instance-Application-in-Java.html
        int port = p.getInt("port", 0);
        if(startServer(port, args)) { // If this was the first instance
            initLaf();
            createUI(args);
        } else { // TODO: should loop infinitely
            startClient(port, args);
        }
    }
    
    public static boolean indev;
    
    public static String runPath;
    
    public static String myMD5 = calcMD5();
    
    private static String calcMD5() {
        String md5 = "";
        try {
            runPath = URLDecoder.decode(EditorFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
            if(!runPath.endsWith(".jar")) {
                indev = true;
                return "indev";
            }
            InputStream fis = new FileInputStream(runPath);
            byte[] buffer = new byte[8192];
            MessageDigest md = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = fis.read(buffer);
                if(numRead > 0) {
                    md.update(buffer, 0, numRead);
                }
            } while(numRead != -1);
            fis.close();
            byte[] b = md.digest();
            for(int i = 0; i < b.length; i++) {
                md5 += Integer.toString((b[i] & 255) + 256, 16).substring(1);
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return md5;
    }
    
    private static Preferences p = Preferences.userRoot().node(projectName);
    
    private static void initLaf() {
        if(System.getProperty("swing.defaultlaf") == null) { // Do not override user specified theme
            try {
                if(os == OS.Mac) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
                } else if(os == OS.Linux) {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                } else {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                // Will eventually be removed in favour of native appearance
                for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch(Exception ex) {
                Logger.getLogger(EditorFrame.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        GtkFixer.installGtkPopupBugWorkaround();
    }
    
    private static boolean startServer(int port, final String[] args) {        
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
            
            Thread listener = new Thread(new Runnable() {
                public void run() {
                    while(!server.isClosed()) {
                        try {
                            Socket client = server.accept();
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                            
                            String iMD5 = in.readLine();
                            System.out.println("A client connected!");
                            if(iMD5.equals("indev")) {
                                server.close();
                                out.println("indev");
                                System.exit(0);
                            }
                            if(iMD5.equals(myMD5)) {
                                createUI(args);
                            }
                        } catch(SocketTimeoutException e) {
                            
                        } catch(IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            listener.setDaemon(os != OS.Mac); // non-daemon threads work in the background. Stick around if on a mac until manually terminated
            listener.start();
        } catch (IOException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    private static boolean startClient(int port, String[] args) {
        System.out.println("Program already running, communicating...");
        try {
            Socket client = new Socket("localhost", port);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println(myMD5); // -1 = close
            String returnCode = in.readLine(); // for dev purposes
            System.out.println(returnCode);
            if(returnCode.equals("indev")) {
                main(new String[] {""}); // should carry args
            }
        } catch (IOException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex1);
            System.out.println("Program closed suddenly");
            return false;
        }
        return true;
    }
    
    private static void createUI(String[] args) {
        boolean flag = true;
        for(int i = 0; i < args.length; i++) {
            String cmd = args[i].toLowerCase();
            if("noupdate".equals(cmd)) {
                flag = false;
                break;
            }
        }

        final boolean autoCheck = flag;

        SwingUtilities.invokeLater(new Runnable() { // SwingUtilities vs EventQueue?

            @Override
            public void run() {
                EditorFrame frame = new EditorFrame();
                frame.autoCheck = autoCheck;
                frame.setVisible(true);
            }

        });
    }
    
}
