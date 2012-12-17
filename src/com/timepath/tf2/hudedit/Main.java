package com.timepath.tf2.hudedit;

import com.timepath.tf2.hudedit.plaf.linux.GtkFixer;
import com.timepath.tf2.hudedit.swing.EditorFrame;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.tomahawk.XFileDialog;

/**
 *
 * @author andrew
 */
public class Main {
    
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    
    public final static String appName = "TF2 HUD Editor";
    
    public final static String javaName = "com-timepath-tf2-hudedit";
    
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
            LOG.log(Level.WARNING, "Unrecognised OS: {0}", osVer);
        }
        
        if(os == OS.Mac) {
            System.setProperty("apple.awt.brushMetalLook", "false");
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
                System.out.println("java.library.path = " + System.getProperty("java.library.path"));
            } catch(UnsupportedClassVersionError e) {
                
            }
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
    
    public static void main(String[] args) {
        System.out.println("Reading: " + System.getenv());
        // http://www.ailis.de/~k/archives/64-How-to-implement-a-Single-Instance-Application-in-Java.html
        // http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
        // http://www.javaworld.com/jw-12-1996/jw-12-sockets.html?page=3
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
            byte[] buffer = new byte[8192]; // 8K buffer
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
    
    private static void createLinuxLauncher() {
        File sourceFile = new File("res/bin/" + javaName + ".desktop");
        File destFile = new File(System.getProperty("user.home") + "/.local/share/applications/" + javaName + ".desktop");
        if(sourceFile.exists() && !destFile.exists()) {
            try {
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
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private File fileSelection(boolean savemode) {
        String os = System.getProperty("os.name");
        File input = null;
        String zenity = "zenity --file-selection --title=Open";
        String filestring;
        if ((os.indexOf("nix")!=-1 || os.indexOf("nux")!=-1)) {
            //Use native Linux file selection.
            try {
                if (savemode) {
                    zenity = "zenity --file-selection --title=Save --save";
                }
                Process p = Runtime.getRuntime().exec(zenity);  
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));  
                StringBuffer sb = new StringBuffer();  
                String line;
                /*while ((line = br.readLine()) != null) {  
                  sb.append(line).append("\n");  
                } */
                sb.append(br.readLine());
                filestring = sb.toString();  
                if (filestring.equals("null")) {
                    return null;
                }
                System.out.println(filestring);
                input = new File(filestring);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } else {
            final JFileChooser fc = new JFileChooser();
            int returnVal;
            if (savemode) {
                returnVal = fc.showSaveDialog(fc);
            } else {
                returnVal = fc.showOpenDialog(fc);  
            }
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                input = fc.getSelectedFile();
            }
        }
        return input;
    }

    public final static int shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    private static Preferences p = Preferences.userRoot().node(projectName);
    
    private static void initLaf() {
        if(System.getProperty("swing.defaultlaf") == null) { // Do not ovveride user specified theme
            try {
                if(os == OS.Mac) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // TODO: quaqua
                } else if(os == OS.Linux) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    GtkFixer.installGtkPopupBugWorkaround();
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
