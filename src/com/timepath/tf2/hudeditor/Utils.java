package com.timepath.tf2.hudeditor;

import com.timepath.tf2.hudeditor.plaf.OS;

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

}
