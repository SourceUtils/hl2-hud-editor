package com.timepath.hl2.hudeditor

import com.timepath.plaf.OS
import com.timepath.plaf.linux.DesktopLauncher
import com.timepath.plaf.linux.WindowToolkit
import com.timepath.plaf.mac.OSXProps
import java.util.ResourceBundle
import java.util.prefs.Preferences
import javax.swing.SwingUtilities
import kotlin.platform.platformStatic

object Main {

    private val strings = ResourceBundle.getBundle("com/timepath/hl2/hudeditor/res/lang")
    private val appName = "TF2 HUD Editor"
    /**
     * Used for storing preferences, do not localize.
     * The window class on Linux systems
     * The app name on Mac systems
     */
    private val projectName = "tf2-hud-editor"
    /**
     * in xfce, window grouping show this, unfortunately
     */
    public val prefs: Preferences = Preferences.userRoot().node(projectName)

    init {
        if (OS.isLinux()) {
            WindowToolkit.setWindowClass(projectName)
            DesktopLauncher.create(projectName, "/com/timepath/hl2/hudeditor/res/", arrayOf("Icon.png", "Icon.svg"), projectName, projectName)
        } else if (OS.isMac()) {
            OSXProps.setMetal(false)
            OSXProps.setQuartz(true)
            OSXProps.setShowGrowBox(true)
            OSXProps.useGlobalMenu(true)
            OSXProps.setSmallTabs(true)
            OSXProps.useFileDialogPackages(true)
            OSXProps.setName(appName)
            OSXProps.setGrowBoxIntrudes(false)
            OSXProps.setLiveResize(true)
        }
    }

    public fun getString(key: String): String {
        return getString(key, key)
    }

    public fun getString(key: String, fallback: String): String {
        return if (strings.containsKey(key)) strings.getString(key) else fallback
    }

    public platformStatic fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            HUDEditor().setVisible(true)
        }
    }
}
