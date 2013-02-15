package com.timepath.tf2.hudeditor;

import com.timepath.plaf.OS;
import com.timepath.plaf.linux.DesktopLauncher;
import com.timepath.tf2.hudeditor.gui.EditorFrame;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import net.tomahawk.XFileDialog;

/**
 * Link dump:
 * https://docs.google.com/document/d/19jk3L-kyduz_AvTOhMXk4agh5gUYM9gWQCHafbMl3wY/edit
 *
 * @author timepath
 */
public class Main {

    public static final ResourceBundle strings = ResourceBundle.getBundle("com/timepath/tf2/hudeditor/resources/lang");

    public static final String appName = "TF2 HUD Editor";

    /**
     * Used for storing preferences. Do not localize
     * The window class on Linux systems
     * The app name on Mac systems
     */
    public static final String projectName = "tf2-hud-editor"; // in xfce, window grouping show this, unfortunately

    public static final OS os;

    public static final Preferences prefs = Preferences.userRoot().node(projectName);

    public static final String myVer = Main.class.getPackage().getImplementationVersion();// = calcMD5();

    public static final String logFile;

    public static final String runPath;

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    static {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            public void uncaughtException(Thread thread, Throwable thrwbl) {
                Logger.getLogger(thread.getName()).log(Level.SEVERE, "Uncaught Exception", thrwbl);
            }
        });

        Logger.getLogger("").setLevel(Level.INFO);

        //<editor-fold defaultstate="collapsed" desc="logfile">
        logFile = Utils.workingDirectory() + "logs/"+System.currentTimeMillis()/1000+".log";
        LOG.log(Level.INFO, "Logging to {0}", logFile);
        try {
            new File(logFile).getParentFile().mkdirs();
            FileHandler fh = new FileHandler(logFile, 0, 1, false);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            Logger.getLogger("").addHandler(fh);
        } catch(IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch(SecurityException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Working directory">
        String path = "download.jar";
        try {
            path = URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
        } catch(UnsupportedEncodingException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        runPath = path;
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="OS detection">
        String osVer = System.getProperty("os.name").toLowerCase();
        if(osVer.indexOf("windows") != -1) {
            os = OS.Windows;
        } else if(osVer.indexOf("mac os x") != -1 || osVer.indexOf("OS X") != -1 || osVer.indexOf("mac") != -1) {
            os = OS.Mac;
        } else if(osVer.indexOf("Linux") != -1 || osVer.indexOf("nix") != -1 || osVer.indexOf("nux") != -1) {
            os = OS.Linux;
        } else {
            os = OS.Other;
            LOG.log(Level.WARNING, "Unrecognised OS: {0}", osVer);
        }
        //</editor-fold>

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
            String n = projectName;
//            n = Wrapper.class.getName().replaceAll("\\.", "-");
            //<editor-fold defaultstate="collapsed" desc="Global menu">
            System.setProperty("jayatana.startupWMClass", n);
            //            boolean force = "Unity".equals(System.getenv("XDG_CURRENT_DESKTOP")); // UBUNTU_MENUPROXY=libappmenu.so
            //            if(force) {
            //                System.setProperty("jayatana.force", "true");
            //            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Window class">
            // Doesn't seem to work all the time
            try {
                Toolkit xToolkit = Toolkit.getDefaultToolkit();
                Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
                awtAppClassNameField.setAccessible(true);
                awtAppClassNameField.set(xToolkit, n);
            } catch(Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Launcher">
            DesktopLauncher.create(n, "/com/timepath/tf2/hudeditor/resources",
                          new String[]{"Icon.png", "Icon.svg"},
                          new String[]{projectName, projectName});
            //</editor-fold>
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Entry point">
    public static void main(String... args) {
        LOG.log(Level.CONFIG, "Env = {0}", System.getenv().toString());
        LOG.log(Level.CONFIG, "Properties = {0}", System.getProperties().toString());

        int port = prefs.getInt("port", 0);
        if(port != 0) { // May not have been removed on shutdown
            LOG.info("Checking for daemon...");
            if(!startClient(port, args)) {
                LOG.info("Daemon not running, starting...");
                for(;;) {
                    if(startServer(port)) {
                        break;
                    } else if(startClient(port, args)) {
                        return;
                    }
                }
            }
        }
        start(args);
    }

    /**
     * Attempts to listen on the specified port
     *
     * @param port the port to listen on
     * @return true if a server was started
     */
    private static boolean startServer(int port) {
        try {
            final ServerSocket sock = new ServerSocket(port, 0, InetAddress.getByName(null)); // cannot use java7 InetAddress.getLoopbackAddress(). On windows, this prevents firewall warnings. It's also good for security in general
            port = sock.getLocalPort();
            prefs.putInt("port", port);

            LOG.log(Level.INFO, "Listening on port {0}", port);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    LOG.info("Server shutting down...");
                    prefs.remove("port");
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
                            LOG.log(Level.INFO, "client {0} vs host {1}", new Object[]{cVer, myVer});
                            String request = "-noupdate " + in.readLine();
                            LOG.log(Level.INFO, "Request: {0}", request);
                            out.println(myVer);

                            if(cVer.equals("null") || (myVer != null && cVer.compareTo(myVer) > 0)) {
                                LOG.info("Surrendering control to other process");
                                out.flush();
                                sock.close();
                            } else {
                                start(request.split(" "));
                            }
                        } catch(Exception ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                    LOG.info("Exiting...");
                    System.exit(0);
                }
            }, "Process Listener");
            server.setDaemon(os != OS.Mac); // non-daemon threads work in the background. Stick around if on a mac until manually terminated
            //            server.setDaemon(false);
            server.start();
        } catch(BindException ex) {
            return false;
        } catch(Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     *
     * @param port
     * @param args
     * @return true if connected
     */
    private static boolean startClient(int port, String... args) {
        try {
            Socket client = new Socket(InetAddress.getByName(null), port);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println(myVer);
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            out.println(sb.toString());
            String sVer = in.readLine();
            if(myVer == null || (!sVer.equals("null") && sVer.compareTo(myVer) > 0)) {
                return false;
            } else {
                return true;
            }
        } catch(SocketException ex) {
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private static void start(String... args) {
        boolean flag = false;
        for(int i = 0; i < args.length; i++) {
            String cmd = args[i].toLowerCase();
            if("-noupdate".equals(cmd)) {
                flag = true;
                break;
            }
        }

        final boolean autoCheck = !flag;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EditorFrame frame = new EditorFrame();
                frame.autoCheck = autoCheck;
                frame.setVisible(true);
            }
        });
    }
    //</editor-fold>

    /**
     *
     * 'return Main.strings.containsKey(key) ? Main.strings.getString(key) : key' is unavailable prior to 1.6
     *
     * @param key
     * @param fallback
     * @return
     */
    public static String getString(String key, String fallback) {
        return Collections.list(Main.strings.getKeys()).contains(key) ? Main.strings.getString(key) : fallback;
    }

    public static String getString(String key) {
        return getString(key, key);
    }

    public static String selfCheck() {
        String md5 = null;
        if(runPath.endsWith(".jar")) {
            try {
                md5 = Utils.takeMD5(Utils.loadFile(new File(runPath)));
            } catch(Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return md5;
    }

}
