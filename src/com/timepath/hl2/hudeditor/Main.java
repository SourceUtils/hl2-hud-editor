package com.timepath.hl2.hudeditor;

import com.timepath.Utils;
import com.timepath.plaf.OS;
import com.timepath.plaf.linux.WindowToolkit;
import com.timepath.plaf.mac.OSXProps;
import com.timepath.plaf.x.filechooser.XFileDialogFileChooser;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
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

    public static final ResourceBundle strings = ResourceBundle.getBundle("com/timepath/hl2/hudeditor/res/lang");

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

    static {
        //<editor-fold defaultstate="collapsed" desc="Debugging">
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable thrwbl) {
                Logger.getLogger(thread.getName()).log(Level.SEVERE, "Uncaught Exception", thrwbl);
            }
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Logging">
        Logger.getLogger("com.timepath").setLevel(Level.ALL);

        logFile = new File(Utils.workingDirectory(Main.class), "logs/" + System.currentTimeMillis() / 1000 + "_log.txt");
        try {
            logFile.getParentFile().mkdirs();
            FileHandler fh = new FileHandler(logFile.getPath(), 0, 1, false);
            fh.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            Logger.getLogger("").addHandler(fh);
            LOG.log(Level.INFO, "Logging to {0}", logFile.getPath());
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch(SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
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
        //<editor-fold defaultstate="collapsed" desc="Initialize">
        LOG.log(Level.INFO, "Args = {0}", Arrays.toString(args));
        String cwd = Utils.workingDirectory(Main.class);
        LOG.log(Level.INFO, "Working directory = {0}", cwd);
        File current = Utils.currentFile(Main.class);
        LOG.log(Level.INFO, "Current file = {0}", current);
        File update = new File(cwd, "update.tmp");
        if(update.exists()) {
            LOG.log(Level.INFO, "Update file = {0}", update);
        }
        LOG.log(Level.CONFIG, "Env = {0}", System.getenv().toString());
        LOG.log(Level.CONFIG, "Properties = {0}", System.getProperties().toString());
        for(int i = 0; i < args.length; i++) {
            if(args[i].equalsIgnoreCase("updated")) {
                update.delete();
            }
        }
        if(!current.equals(update)) {
            startTheOther(update);
        }

        for(int i = 0; i < args.length; i++) {
            if(args[i].equalsIgnoreCase("-u")) {
                try {
                    File destFile = new File(args[i + 1]);
                    LOG.log(Level.INFO, "Updating {0}", destFile);
                    File sourceFile = Utils.currentFile(Main.class);
                    if(!destFile.exists()) {
                        destFile.createNewFile();
                    }

                    FileChannel source = null;
                    FileChannel destination = null;
                    try {
                        source = new RandomAccessFile(sourceFile, "rw").getChannel();
                        destination = new RandomAccessFile(destFile, "rw").getChannel();

                        long position = 0;
                        long count = source.size();

                        source.transferTo(position, count, destination);
                    } finally {
                        if(source != null) {
                            source.close();
                        }
                        if(destination != null) {
                            destination.force(true);
                            destination.close();
                        }
                    }
                    final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                    final ArrayList<String> cmd = new ArrayList<String>();
                    cmd.add(javaBin);
                    cmd.add("-jar");
                    cmd.add(destFile.getPath());
                    cmd.add("updated");
                    // TODO: carry other args
                    String[] exec = new String[cmd.size()];
                    cmd.toArray(exec);
                    final ProcessBuilder process = new ProcessBuilder(exec);
                    process.start();
                    sourceFile.deleteOnExit();
                    System.exit(0);
                } catch(IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //</editor-fold>
        
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
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                    long cVer = Long.parseLong(in.readLine());
                    LOG.log(Level.INFO, "client {0} vs host {1}", new Object[]{cVer, myVer});
                    String request = "-noupdate " + in.readLine();
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

    private static void startTheOther(File update) {
        if(!update.exists()) {
            return;
        }
        try {
            String md5 = Utils.takeMD5(Utils.loadFile(update));
            File updateChecksum = new File(update.getPath() + ".MD5");
            String expectedMd5 = new String(Utils.loadFile(updateChecksum));
            if(!md5.equals(expectedMd5)) {
                LOG.log(Level.WARNING, "Corrupt update file");
                updateChecksum.delete();
                update.delete();
                return;
            }
        } catch(IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch(NoSuchAlgorithmException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        LOG.log(Level.INFO, "Updating from {0}", update);
        try {
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

            final ArrayList<String> cmd = new ArrayList<String>();
            cmd.add(javaBin);
            cmd.add("-jar");
            cmd.add(update.getPath());
            cmd.add("-u");
            cmd.add(Utils.currentFile(Main.class).getPath());
            String[] exec = new String[cmd.size()];
            cmd.toArray(exec);
            LOG.log(Level.INFO, "Invoking other: {0}", Arrays.toString(exec));
            final ProcessBuilder process = new ProcessBuilder(exec);
            process.start();
            System.exit(0);
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static void start(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HUDEditor().setVisible(true);
            }
        });
    }
}
