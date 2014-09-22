package com.timepath.hl2.hudeditor;

import com.timepath.steam.SteamID;
import com.timepath.steam.SteamUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author TimePath
 */
public abstract class BackgroundLoader {

    private static final Logger LOG = Logger.getLogger(BackgroundLoader.class.getName());

    public static Image fetch() {
        SteamID user = SteamUtils.getUser();
        LOG.log(Level.INFO, "Current user: {0}", user);
        if (user == null) {
            LOG.log(Level.WARNING, "Steam not found");
            return null;
        }
        File screenshotDir = new File(SteamUtils.getUserData(), "760/remote/440/screenshots/");
        File[] files = screenshotDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg");
            }
        });
        if (files != null) {
            try {
                return new ImageIcon(files[(int) (Math.random() * (files.length - 1))].toURI().toURL()).getImage();
            } catch (MalformedURLException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        LOG.log(Level.INFO, "No screenshots in {0}", screenshotDir);
        return null;
    }
}
