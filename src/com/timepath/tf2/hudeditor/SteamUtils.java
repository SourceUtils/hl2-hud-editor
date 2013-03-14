package com.timepath.tf2.hudeditor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.timepath.plaf.OS;
import com.timepath.tf2.hudeditor.gui.EditorFrame;

/**
 *
 * @author timepath
 */
public class SteamUtils {

    private static final Logger LOG = Logger.getLogger(SteamUtils.class.getName());

    private SteamUtils() {
    }

    public static String locateSteamAppsDirectory() {
        if(OS.isWindows()) {
            String str = System.getenv("PROGRAMFILES(x86)");
            if(str == null) {
                str = System.getenv("PROGRAMFILES");
            }
            return str + "/Steam/steamapps/";
        } else if(OS.isMac()) {
            return "~/Library/Application Support/Steam/SteamApps/";
        } else if(OS.isLinux()) {
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
    
}
