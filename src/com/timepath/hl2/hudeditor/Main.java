package com.timepath.hl2.hudeditor;

import com.timepath.plaf.OS;
import com.timepath.plaf.linux.DesktopLauncher;
import com.timepath.plaf.linux.WindowToolkit;
import com.timepath.plaf.mac.OSXProps;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author TimePath
 */
class Main {

    public static final  long           myVer       = getVer();
    private static final ResourceBundle strings     = ResourceBundle.getBundle("com/timepath/hl2/hudeditor/res/lang");
    private static final String         appName     = "TF2 HUD Editor";
    /**
     * Used for storing preferences. Do not localize
     * The window class on Linux systems
     * The app name on Mac systems
     */
    private static final String         projectName = "tf2-hud-editor"; // in xfce, window grouping show this, unfortunately
    public static final  Preferences    prefs       = Preferences.userRoot().node(projectName);
    private static final Logger         LOG         = Logger.getLogger(Main.class.getName());

    //<editor-fold defaultstate="collapsed" desc="OS tweaks">
    static {
        if(OS.isWindows()) {
        } else if(OS.isLinux()) {
            WindowToolkit.setWindowClass(projectName); // Main.class.getName().replace(".", "-");
            DesktopLauncher.create(projectName,
                                   "/com/timepath/hl2/hudeditor/res/",
                                   new String[] { "Icon.png", "Icon.svg" },
                                   projectName,
                                   projectName);
        } else if(OS.isMac()) {
            OSXProps.setMetal(false); OSXProps.setQuartz(true); OSXProps.setShowGrowBox(true); OSXProps.useGlobalMenu(true);
            OSXProps.setSmallTabs(true); OSXProps.useFileDialogPackages(true); OSXProps.setName(appName);
            OSXProps.setGrowBoxIntrudes(false); OSXProps.setLiveResize(true);
        }
    }
    //</editor-fold>

    public static String getString(String key) {
        return getString(key, key);
    }

    /**
     * 'return Main.strings.containsKey(key) ? Main.strings.getString(key) : key' is
     * unavailable prior to 1.6
     *
     * @param key
     * @param fallback
     *
     * @return
     */
    private static String getString(String key, String fallback) {
        return Collections.list(strings.getKeys()).contains(key) ? strings.getString(key) : fallback;
    }

    public static void main(String... args) {
        boolean daemon = false; if(daemon) {
            LOG.log(Level.INFO, "Current version = {0}", myVer); int port = prefs.getInt("port", -1);
            if(port == -1) { // Was removed on shutdown
                port = 0;
            } else {
                LOG.info("Communicating with daemon...");
            } for(; ; ) {
                if(startClient(port, args)) {
                    break;
                } LOG.info("Daemon not running, starting..."); if(startServer(port)) {
                    start(args); break;
                } LOG.info("Daemon already running, conecting...");
            }
        } else {
            start(args);
        }
    }

    /**
     * Attempts to listen on the specified port
     *
     * @param port
     *         the port to listen on
     *
     * @return true if a server was started
     */
    private static boolean startServer(int port) {
        try {
            ServerSocket sock = new ServerSocket(port, 0, InetAddress.getByName(null)); // cannot use java7 InetAddress
            // .getLoopbackAddress(). On windows, this prevents firewall warnings. It's
            // also good for security in general
            int truePort = sock.getLocalPort(); prefs.putInt("port", truePort); prefs.flush();
            LOG.log(Level.INFO, "Daemon listening on port {0}", truePort); Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    LOG.info("Daemon shutting down..."); prefs.remove("port"); try {
                        prefs.flush();
                    } catch(BackingStoreException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.WARNING, null, ex);
                    }
                }
            }); Thread server = new Thread(new ServerRunnable(sock), "Process Listener");
            //            server.setDaemon(!OS.isMac()); // non-daemon threads work in the background. Stick around if on a mac
            // until manually terminated
            //            server.setDaemon(false); // hang around
            server.setDaemon(true); // die immediately
            server.start();
        } catch(BindException ex) {
            return false;
        } catch(Exception ex) {
            LOG.log(Level.SEVERE, null, ex); return false;
        } return true;
    }

    /**
     * @param port
     * @param args
     *
     * @return true if connected
     */
    private static boolean startClient(int port, String... args) {
        try {
            Socket client = new Socket(InetAddress.getByName(null), port);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true); out.println(myVer);
            StringBuilder sb = new StringBuilder(); for(String arg : args) {
                sb.append(arg).append(' ');
            } out.println(sb); long sVer = Long.parseLong(in.readLine()); return ( myVer <= sVer ) && ( myVer != 0 );
        } catch(SocketException ex) {
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } return false;
    }

    private static void start(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HUDEditor().setVisible(true);
            }
        });
    }

    private static long getVer() {
        String impl = Main.class.getPackage().getImplementationVersion(); if(impl == null) {
            return 0;
        } return Long.parseLong(impl);
    }

    private static class ServerRunnable implements Runnable {

        private final ServerSocket sock;

        ServerRunnable(ServerSocket sock) {
            this.sock = sock;
        }

        public void run() {
            while(!sock.isClosed()) {
                try {
                    Socket client = sock.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true); long cVer = Long.parseLong(in.readLine());
                    LOG.log(Level.INFO, "client {0} vs host {1}", new Object[] { cVer, myVer }); String request = in.readLine();
                    LOG.log(Level.INFO, "Request: {0}", request); out.println(myVer); if(( cVer > myVer ) || ( cVer == 0 )) {
                        LOG.info("Daemon surrendering control to other process"); out.flush(); sock.close();
                    } else {
                        start(request.split(" "));
                    }
                } catch(Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } LOG.info("Exiting...");
        }
    }
}
