package com.timepath.tf2.hudeditor.util;

import com.timepath.plaf.OS;
import com.timepath.tf2.hudeditor.Main;
import com.timepath.tf2.hudeditor.gui.EditorFrame;
import com.timepath.tf2.loaders.GCF;
import com.timepath.tf2.loaders.RES;
import com.timepath.tf2.loaders.VDF;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author timepath
 */
public class Utils {

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

    public static String workingDirectory() {
        String ans;
        try {
            ans = new File(URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8")).getParent() + "/";
            return ans;
        } catch(UnsupportedEncodingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        String str = System.getProperty("user.dir") + "/" + System.getProperty("sun.java.command");
        int end = str.replaceAll("\\\\", "/").lastIndexOf('/');
        if(end == -1) {
            end = str.length();
        }
        return str.substring(0, end) + "/";
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

    private static Comparator<File> dirAlphaComparator = new Comparator<File>() {
        /**
         * Alphabetically sorts directories before files ignoring case.
         */
        @Override
        public int compare(File a, File b) {
            if(a.isDirectory() && !b.isDirectory()) {
                return -1;
            } else if(!a.isDirectory() && b.isDirectory()) {
                return 1;
            } else {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        }
    };

    private static String[] blacklist = {".mp3", ".exe", ".sh", ".dll", ".dylib", ".so",
                                         ".ttf", ".bik", ".mov", ".cfg", ".cache", ".manifest",
                                         ".frag", ".vert", ".tga", ".png", ".html", ".wav",
                                         ".ico", ".uifont", ".xml", ".css", ".dic", ".conf",
                                         ".pak", ".py", ".flt", ".mix", ".asi", ".checksum",
                                         ".xz", ".log", ".doc", ".webm", ".jpg", ".psd", ".avi",
                                         ".zip", ".bin"};

    public static void recurseDirectoryToNode(File root, final DefaultMutableTreeNode parent) {
        final File[] fileList = root.listFiles();
        if(fileList.length == 0) {
            return;
        }
        Arrays.sort(fileList, Utils.getDirAlphaComparator());
        for(int i = 0; i < fileList.length; i++) {
            final DefaultMutableTreeNode child = new DefaultMutableTreeNode();
            child.setUserObject(fileList[i]);
            if(fileList[i].isDirectory()) {
                recurseDirectoryToNode(fileList[i], child);
                if(child.getChildCount() == 0) {
                    continue;
                }
                parent.add(child);
            } else {
                boolean flag = false;
                for(int j = 0; j < blacklist.length; j++) {
                    if(fileList[i].getName().endsWith(blacklist[j])) {
                        flag = true;
                        break;
                    }
                }
                if(flag) {
                    continue;
                }
                final int idx = i;
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        if(fileList[idx].getName().endsWith(".txt")
                           || fileList[idx].getName().endsWith(".vdf")
                           || fileList[idx].getName().endsWith(".layout")
                           || fileList[idx].getName().endsWith(".menu")
                           || fileList[idx].getName().endsWith(".styles")) {
                            VDF.analyze(fileList[idx], child);
                        } else if(fileList[idx].getName().endsWith(".res")) {
                            RES.analyze(fileList[idx], child);
                        } else if(fileList[idx].getName().endsWith(".vmt")) {
//                            VDF.analyze(fileList[idx], child);
                        } else if(fileList[idx].getName().endsWith(".gcf")) {
                            try {
                                GCF g = new GCF(fileList[idx]);
                                child.setUserObject(g);
                                g.analyze(g, child);
                            } catch(IOException ex) {
                                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        parent.add(child);
                    }
                });
                boolean multi = false;
                if(multi) {
                    t.start();
                } else {
                    t.run();
                }
            }
        }
    }

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    /**
     * @return the dirAlphaComparator
     */
    public static Comparator<File> getDirAlphaComparator() {
        return dirAlphaComparator;
    }

    private Utils() {
    }
}
