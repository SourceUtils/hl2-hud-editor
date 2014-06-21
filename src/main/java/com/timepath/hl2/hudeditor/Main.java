package com.timepath.hl2.hudeditor;

import com.timepath.plaf.OS;
import com.timepath.plaf.linux.DesktopLauncher;
import com.timepath.plaf.linux.WindowToolkit;
import com.timepath.plaf.mac.OSXProps;

import javax.swing.*;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * @author TimePath
 */
class Main {

    private static final ResourceBundle strings     = ResourceBundle.getBundle("com/timepath/hl2/hudeditor/res/lang");
    private static final String         appName     = "TF2 HUD Editor";
    /**
     * Used for storing preferences. Do not localize
     * The window class on Linux systems
     * The app name on Mac systems
     */
    private static final String         projectName = "tf2-hud-editor";
            // in xfce, window grouping show this, unfortunately
    public static final  Preferences    prefs       = Preferences.userRoot().node(projectName);

    static {
        if(OS.isLinux()) {
            WindowToolkit.setWindowClass(projectName); // Main.class.getName().replace(".", "-");
            DesktopLauncher.create(projectName,
                                   "/com/timepath/hl2/hudeditor/res/",
                                   new String[] { "Icon.png", "Icon.svg" },
                                   projectName,
                                   projectName);
        } else if(OS.isMac()) {
            OSXProps.setMetal(false);
            OSXProps.setQuartz(true);
            OSXProps.setShowGrowBox(true);
            OSXProps.useGlobalMenu(true);
            OSXProps.setSmallTabs(true);
            OSXProps.useFileDialogPackages(true);
            OSXProps.setName(appName);
            OSXProps.setGrowBoxIntrudes(false);
            OSXProps.setLiveResize(true);
        }
    }

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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new HUDEditor().setVisible(true);
            }
        });
    }
}
