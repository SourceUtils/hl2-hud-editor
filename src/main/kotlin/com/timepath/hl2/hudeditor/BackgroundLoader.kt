package com.timepath.hl2.hudeditor

import com.timepath.steam.SteamUtils
import java.awt.Image
import java.io.File
import java.io.FilenameFilter
import java.net.MalformedURLException
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.ImageIcon

/**
 * @author TimePath
 */
public object BackgroundLoader {

    private val LOG = Logger.getLogger(javaClass<BackgroundLoader>().getName())

    public fun fetch(): Image? {
        val user = SteamUtils.getUser()
        LOG.log(Level.INFO, "Current user: {0}", user)
        if (user == null) {
            LOG.log(Level.WARNING, "Steam not found")
            return null
        }
        val screenshotDir = File(SteamUtils.getUserData(), "760/remote/440/screenshots/")
        val files = screenshotDir.listFiles(object : FilenameFilter {
            override fun accept(dir: File, name: String): Boolean {
                return name.toLowerCase().endsWith(".jpg")
            }
        })
        files?.let {
            try {
                return ImageIcon(it[(Math.random() * (it.size() - 1).toDouble()).toInt()].toURI().toURL()).getImage()
            } catch (ex: MalformedURLException) {
                LOG.log(Level.SEVERE, null, ex)
            }
        }
        LOG.log(Level.INFO, "No screenshots in {0}", screenshotDir)
        return null
    }
}
