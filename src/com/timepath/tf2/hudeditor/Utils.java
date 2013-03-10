package com.timepath.tf2.hudeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.timepath.plaf.OS;
import com.timepath.tf2.hudeditor.gui.EditorFrame;

/**
 *
 * @author timepath
 */
public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    private Utils() {
    }

    public static String locateSteamAppsDirectory() {
        if(Main.os == OS.Windows) {
            String str = System.getenv("PROGRAMFILES(x86)");
            if(str == null) {
                str = System.getenv("PROGRAMFILES");
            }
            return str + "/Steam/steamapps/";
        } else if(Main.os == OS.Mac) {
            return "~/Library/Application Support/Steam/SteamApps/";
        } else if(Main.os == OS.Linux) {
            return System.getenv("HOME") + "/.steam/root/SteamApps/";
        } else {
            return null;
        }
    }

    public static String normalisePath(String str) {
        LOG.log(Level.INFO, "Normalising {0}", str);
//        try {
//            return new URI(str).normalize().getPath();
//        } catch(URISyntaxException ex) {
//            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        if(str.indexOf('\\') != -1) {
//            str = str.replaceAll("\\\\", File.separator);
//        }
//        if(str.indexOf('/') != -1) {
//            str = str.replaceAll("/", File.separator); // slash consistency
//        }
        while(str.indexOf(File.separator + File.separator) != -1) {
            str = str.replaceAll(File.separator + File.separator, File.separator);
        }
        if(!str.endsWith(File.separator)) {
            str += File.separator;
        }
        return str;
    }

    public static String workingDirectory() {
        String ans;
        try {
            ans = new File(URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8")).getParentFile().getAbsolutePath();
        } catch(UnsupportedEncodingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            ans = System.getProperty("user.dir") + File.separator;
            String cmd = System.getProperty("sun.java.command");
            int idx = cmd.lastIndexOf(File.separator);
            if(idx != -1) {
                cmd = cmd.substring(0, idx + 1);
            } else {
                cmd = "";
            }
            ans += cmd;
        }
        return normalisePath(ans);
    }

    public static void restart() throws URISyntaxException, IOException { // TODO: wrap this class in a launcher, rather than explicitly restarting
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(EditorFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        if(!currentJar.getName().endsWith(".jar")) {
            return;
        }

        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }

    public static boolean isMD5(String str) {
        return str.matches("[a-fA-F0-9]{32}");
    }

    public static String takeMD5(byte[] bytes) throws NoSuchAlgorithmException {
        String md5 = "";
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        byte[] b = md.digest();
        for(int i = 0; i < b.length; i++) {
            md5 += Integer.toString((b[i] & 0xFF) + 256, 16).substring(1);
        }
        return md5;
    }

    public static String selfCheck() {
        String md5 = null;
        String runPath = workingDirectory();
        if(runPath.endsWith(".jar")) {
            try {
                md5 = Utils.takeMD5(Utils.loadFile(new File(runPath)));
            } catch(Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return md5;
    }

    public static byte[] loadFile(File f) throws FileNotFoundException, IOException {
        InputStream fis = new FileInputStream(f);
        byte[] buff = new byte[8192];
        int numRead;
        int size = 0;
        for(;;) {
            numRead = fis.read(buff);
            if(numRead == -1) {
                break;
            } else {
                size += numRead;
            }
        }
        fis.close();
        byte[] ret = new byte[size];
        System.arraycopy(buff, 0, ret, 0, ret.length);
        return ret;
    }

    
}
