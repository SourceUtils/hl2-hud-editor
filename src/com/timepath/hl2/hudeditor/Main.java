package com.timepath.hl2.hudeditor;

import com.timepath.Utils;
import com.timepath.plaf.OS;
import com.timepath.plaf.linux.WindowToolkit;
import com.timepath.plaf.mac.OSXProps;
import com.timepath.plaf.x.filechooser.XFileDialogFileChooser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;

/**
 *
 * @author timepath
 */
public class Main {

    public static final ResourceBundle strings = ResourceBundle.getBundle(
            "com/timepath/hl2/hudeditor/res/lang");

    public static final String appName = "TF2 HUD Editor";

    /**
     * Used for storing preferences. Do not localize
     * The window class on Linux systems
     * The app name on Mac systems
     */
    public static final String projectName = "tf2-hud-editor"; // in xfce, window grouping show this, unfortunately

    public static final Preferences prefs = Preferences.userRoot().node(projectName);

    public static final long myVer = getVer();

    private static long getVer() {
        String impl = Main.class.getPackage().getImplementationVersion();
        if(impl == null) {
            return 0;
        }
        return Long.parseLong(impl);
    }

    public static final File logFile;

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    /**
     *
     * 'return Main.strings.containsKey(key) ? Main.strings.getString(key) : key' is
     * unavailable prior to 1.6
     *
     * @param key
     * @param fallback
     *
     * @return
     */
    public static String getString(String key, String fallback) {
        return Collections.list(Main.strings.getKeys()).contains(key) ? Main.strings.getString(key) : fallback;
    }

    public static String getString(String key) {
        return getString(key, key);
    }

    public static Level consoleLevel = Level.INFO;

    public static Level logfileLevel = Level.INFO;

    static {
        //<editor-fold defaultstate="collapsed" desc="Debugging">
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable thrwbl) {
                Logger.getLogger(thread.getName()).log(Level.SEVERE, "Uncaught Exception", thrwbl);
            }
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Logging">
        try {
            consoleLevel = Level.parse(prefs.get("consoleLevel", "FINE"));
        } catch(IllegalArgumentException ex) {
        }
        LOG.info("Console level: " + consoleLevel);
        try {
            logfileLevel = Level.parse(prefs.get("logfileLevel", "FINE"));
        } catch(IllegalArgumentException ex) {
        }
        LOG.info("Logfile level: " + logfileLevel);
        Level packageLevel = consoleLevel;
        if(consoleLevel != Level.OFF && logfileLevel != Level.OFF) {
            if(logfileLevel.intValue() > consoleLevel.intValue()) {
                packageLevel = logfileLevel;
            }
        }
        Logger.getLogger("com.timepath").setLevel(packageLevel);

        SimpleFormatter consoleFormatter = new SimpleFormatter();
        SimpleFormatter fileFormatter = new SimpleFormatter();

        if(consoleLevel != Level.OFF) {
            Handler[] hs = Logger.getLogger("").getHandlers();
            for(Handler h : hs) {
                if(h instanceof ConsoleHandler) {
                    h.setLevel(consoleLevel);
                    h.setFormatter(consoleFormatter);
                }
            }
        }

        if(logfileLevel != Level.OFF) {
            logFile = new File(Utils.workingDirectory(Main.class),
                               "logs/" + System.currentTimeMillis() / 1000 + "_log.txt");
            try {
                logFile.getParentFile().mkdirs();
                FileHandler fh = new FileHandler(logFile.getPath(), 0, 1, false);
                fh.setLevel(logfileLevel);
                fh.setFormatter(fileFormatter);
                Logger.getLogger("").addHandler(fh);
                LOG.log(Level.INFO, "Logging to {0}", logFile.getPath());
            } catch(IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch(SecurityException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } else {
            logFile = null;
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="OS tweaks">
        if(OS.isWindows()) {
            XFileDialogFileChooser.setTraceLevel(0);
        } else if(OS.isLinux()) {
            WindowToolkit.setWindowClass(projectName); // Wrapper.class.getName().replaceAll("\\.", "-");
        } else if(OS.isMac()) {
            OSXProps.metal(false);
            OSXProps.quartz(true);
            OSXProps.growBox(true);
            OSXProps.globalMenu(true);
            OSXProps.smallTabs(true);
            OSXProps.fileDialogPackages(true);
            OSXProps.name(appName);
            OSXProps.growBoxIntrudes(false);
            OSXProps.liveResize(true);
        }
        //</editor-fold>
    }

    public static void main(String... args) {
        LOG.log(Level.INFO, "Current version = {0}", myVer);
        LOG.log(Level.INFO, "Args = {0}", Arrays.toString(args));
        String cwd = Utils.workingDirectory(Main.class);
        LOG.log(Level.INFO, "Working directory = {0}", cwd);
        LOG.log(Level.CONFIG, "Env = {0}", System.getenv().toString());
        LOG.log(Level.CONFIG, "Properties = {0}", System.getProperties().toString());

        boolean daemon = false;
        if(daemon) {
            int port = prefs.getInt("port", -1);
            if(port == -1) { // Was removed on shutdown
                port = 0;
            } else {
                LOG.info("Communicating with daemon...");
            }
            for(;;) {
                if(startClient(port, args)) {
                    break;
                }
                LOG.info("Daemon not running, starting...");
                if(startServer(port)) {
                    start(args);
                    break;
                }
                LOG.info("Daemon already running, conecting...");
            }
        } else {
            start(args);
        }
    }

    /**
     * Attempts to listen on the specified port
     *
     * @param port the port to listen on
     *
     * @return true if a server was started
     */
    private static boolean startServer(int port) {
        try {
            final ServerSocket sock = new ServerSocket(port, 0, InetAddress.getByName(null)); // cannot use java7 InetAddress.getLoopbackAddress(). On windows, this prevents firewall warnings. It's also good for security in general
            int truePort = sock.getLocalPort();
            prefs.putInt("port", truePort);
            prefs.flush();

            LOG.log(Level.INFO, "Daemon listening on port {0}", truePort);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    LOG.info("Daemon shutting down...");
                    prefs.remove("port");
                    try {
                        prefs.flush();
                    } catch(BackingStoreException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.WARNING, null, ex);
                    }
                }
            });

            Thread server = new Thread(new ServerRunnable(sock), "Process Listener");
//            server.setDaemon(!OS.isMac()); // non-daemon threads work in the background. Stick around if on a mac until manually terminated
//            server.setDaemon(false); // hang around
            server.setDaemon(true); // die immediately
            server.start();
        } catch(BindException ex) {
            return false;
        } catch(Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    private static class ServerRunnable implements Runnable {

        private ServerSocket sock;

        ServerRunnable(ServerSocket sock) {
            this.sock = sock;
        }

        public void run() {
            while(!sock.isClosed()) {
                try {
                    Socket client = sock.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                    long cVer = Long.parseLong(in.readLine());
                    LOG.log(Level.INFO, "client {0} vs host {1}", new Object[] {cVer, myVer});
                    String request = in.readLine();
                    LOG.log(Level.INFO, "Request: {0}", request);
                    out.println(myVer);

                    if(cVer > myVer || cVer == 0) {
                        LOG.info("Daemon surrendering control to other process");
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

    }

    /**
     *
     * @param port
     * @param args
     *
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
            long sVer = Long.parseLong(in.readLine());
            if(myVer > sVer || myVer == 0) {
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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HUDEditor().setVisible(true);
            }
        });
    }

}
