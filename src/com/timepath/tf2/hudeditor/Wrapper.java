package com.timepath.tf2.hudeditor;

import com.jdotsoft.jarloader.JarClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timepath
 */
public class Wrapper {
    
    private static final Logger logger = Logger.getLogger(Wrapper.class.getName());
    
    public static void main(String[] args) {
        JarClassLoader cl = new JarClassLoader();
        try {
            cl.invokeMain(Main.class.getName(), args);
        } catch(Throwable e) {
            logger.log(Level.SEVERE, "Uncaught Exception", e);
        }
    }
    
}