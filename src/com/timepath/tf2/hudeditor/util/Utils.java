package com.timepath.tf2.hudeditor.util;

import com.timepath.tf2.hudeditor.Main;
import com.timepath.tf2.hudeditor.gui.EditorFrame;
import com.timepath.tf2.hudeditor.plaf.OS;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 *
 * @author TimePath
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

}
