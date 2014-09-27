package com.timepath.hl2.hudeditor;

import com.timepath.plaf.OS;
import com.timepath.plaf.linux.DesktopLauncher;
import com.timepath.plaf.linux.WindowToolkit;
import com.timepath.plaf.mac.OSXProps;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * @author TimePath
 */
class Main {

    private static final ResourceBundle strings = ResourceBundle.getBundle("com/timepath/hl2/hudeditor/res/lang");
    private static final String appName = "TF2 HUD Editor";
    /**
     * Used for storing preferences, do not localize.
     * The window class on Linux systems
     * The app name on Mac systems
     */
    private static final String projectName = "tf2-hud-editor";
    /**
     * in xfce, window grouping show this, unfortunately
     */
    public static final Preferences prefs = Preferences.userRoot().node(projectName);

    static {
        if (OS.isLinux()) {
            WindowToolkit.setWindowClass(projectName);
            DesktopLauncher.create(projectName,
                    "/com/timepath/hl2/hudeditor/res/",
                    new String[]{"Icon.png", "Icon.svg"},
                    projectName,
                    projectName);
        } else if (OS.isMac()) {
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

    public static String getString(@NotNull String key) {
        return getString(key, key);
    }

    public static String getString(@NotNull String key, String fallback) {
        return strings.containsKey(key) ? strings.getString(key) : fallback;
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
