package com.timepath.tf2.hudeditor;

//<editor-fold defaultstate="collapsed" desc="imports">
import com.timepath.tf2.hudeditor.gui.EditorFrame;
import com.timepath.tf2.hudeditor.plaf.OS;
import com.timepath.tf2.hudeditor.plaf.linux.GtkFixer;
import com.timepath.tf2.hudeditor.plaf.linux.LinuxDesktopLauncher;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import net.tomahawk.XFileDialog;
//</editor-fold>

/**
 * Link dump: https://docs.google.com/document/d/19jk3L-kyduz_AvTOhMXk4agh5gUYM9gWQCHafbMl3wY/edit
 * 
 * @author TimePath
 */
public class Main {
    
    public static final ResourceBundle rb = ResourceBundle.getBundle("com/timepath/tf2/hudeditor/resources/lang");
    
    public final static String appName = "TF2 HUD Editor";
    
    /**
     * Used for storing preferences. Do not localize
     * The window class on Linux systems
     * The app name on Mac systems
     */
    public final static String projectName = "tf2-hud-editor"; // in xfce, window grouping show this, unfortunately
    
    public final static OS os;
    
    private final static int arch;
    
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    private static Preferences p = Preferences.userRoot().node(projectName);
    
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
        
        if(System.getProperty("os.arch").indexOf("64") >= 0) {
            arch = 64;
        } else {
            arch = 32;
        }
        
        if(os == OS.Windows) {
            XFileDialog.setTraceLevel(0);
        } else if(os == OS.Mac) {
            System.setProperty("apple.awt.brushMetalLook", "false");
            System.setProperty("apple.awt.graphics.EnableQ2DX", "true"); 
            System.setProperty("apple.awt.showGrowBox", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.smallTabs", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        } else if(os == OS.Linux) {
//            boolean force = "Unity".equals(System.getenv("XDG_CURRENT_DESKTOP")); // UBUNTU_MENUPROXY=libappmenu.so
//            if(force) {
//                System.setProperty("jayatana.force", "true");
//            }
            System.setProperty("jayatana.startupWMClass", projectName);
            
            try {
                Toolkit xToolkit = Toolkit.getDefaultToolkit();
                Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
                awtAppClassNameField.setAccessible(true);
                awtAppClassNameField.set(xToolkit, projectName);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            LinuxDesktopLauncher.create();
        }
    }
    
    public static void main(String... args) {
        logger.log(Level.INFO, "Env: {0}", System.getenv());
        logger.log(Level.INFO, "awt.useSystemAAFontSettings: {0}", System.getProperty("awt.useSystemAAFontSettings", ""));
        logger.log(Level.INFO, "swing.aatext: {0}", System.getProperty("swing.aatext", "")); 
        logger.log(Level.INFO, "swing.defaultlaf: {0}", System.getProperty("swing.defaultlaf", UIManager.getLookAndFeel().toString()));
        logger.log(Level.INFO, "swing.crossplatformlaf: {0}", System.getProperty("swing.crossplatformlaf", UIManager.getCrossPlatformLookAndFeelClassName()));
        
        init(args);
    }
    
    private static void init(String... args) {
        int port = p.getInt("port", 0);
        if(startServer(port, args)) {
            lookAndFeel();
            start(args);
            return;
        }
        if(!startClient(port, args)) {
            init(args);
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="TODO: Replace with timestamp system">
    public static boolean indev;
    
    public static String runPath;
    
    public static String myVer = calcMD5();
    
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
    //</editor-fold>
    
    private static void lookAndFeel() {
        if(System.getProperty("swing.defaultlaf") == null) { // Do not override user specified theme
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                if(os == OS.Mac) {
                    UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel"); // Apply quaqua if available
                } else if(os == OS.Linux) {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); // Apply gtk+ theme if available
                }
                
                //<editor-fold defaultstate="collapsed" desc="Nimbus will eventually be removed in favour of native appearance">
                for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
                //</editor-fold>
            } catch(Exception ex) {
                logger.log(Level.WARNING, null, ex);
            }
        }
        GtkFixer.installGtkPopupBugWorkaround(); // Apply clearlooks java menu fix if applicable
    }
    
    private static boolean startServer(int port, final String... args) {        
        try {
            final ServerSocket sock = new ServerSocket(port, 0, InetAddress.getByName(null)); // cannot use java7 InetAddress.getLoopbackAddress(). On windows, this prevents firewall warnings. It's also good for security in general
            port = sock.getLocalPort();
            p.putInt("port", port);
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    logger.info("Server shutting down...");
                    p.remove("port");
//                    System.exit(0);
                }
            });
            
            Thread server = new Thread(new Runnable() {
                public void run() {
                    while(!sock.isClosed()) {
                        try {
                            Socket client = sock.accept();
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                            
                            String cVer = in.readLine();
                            logger.log(Level.INFO, "client {0} vs host {1}", new Object[] {cVer, myVer});
                            String request = "-noupdate " + in.readLine();
                            logger.log(Level.INFO, "Request: {0}", request);
                            out.println(myVer);
                            
                            if(cVer.equals("indev") || !cVer.equals(myVer)) { // Or if timestamp is greater when timestamps are implemented
                                sock.close();
                                System.exit(0);
                            } else {
                                start(request.split(" "));
                            }
                        } catch(Exception ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            server.setDaemon(os != OS.Mac); // non-daemon threads work in the background. Stick around if on a mac until manually terminated
            server.start();
        } catch(BindException ex) {
            return false;
        } catch(Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    private static boolean startClient(int port, String... args) {
        logger.info("Communicating with other running instance");
        try {
            Socket client = new Socket("localhost", port);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println(myVer);
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            out.println(sb.toString());
            String sVer = in.readLine();
            if(sVer.equals("indev") || !sVer.equals(myVer)) {
                main(args);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    private static void start(String... args) {
        boolean flag = true;
        for(int i = 0; i < args.length; i++) {
            String cmd = args[i].toLowerCase();
            if("-noupdate".equals(cmd)) {
                flag = false;
                break;
            }
        }

        final boolean autoCheck = flag;

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                EditorFrame frame = new EditorFrame();
                frame.autoCheck = autoCheck;
                frame.setVisible(true);
            }

        });
    }
    
}
