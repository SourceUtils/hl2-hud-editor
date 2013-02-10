package com.timepath.tf2.hudeditor;

import com.jdotsoft.jarloader.JarClassLoader;
import com.timepath.plaf.linux.LinuxDesktopLauncher;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timepath
 */
public class Wrapper {

    private static final Logger LOG = Logger.getLogger(Wrapper.class.getName());

    public static void main(String... args) {
        JarClassLoader cl = new JarClassLoader();
        try {
            cl.invokeMain(Main.class.getName(), args);
        } catch(Throwable e) {
            LOG.log(Level.SEVERE, "Uncaught Exception", e);
        }
    }
}