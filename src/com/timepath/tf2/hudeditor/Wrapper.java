package com.timepath.tf2.hudeditor;

import java.util.logging.Level;
import java.util.logging.Logger;
import jdotsoft.JarClassLoader;

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