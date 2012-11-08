package com.timepath.tf2.hudedit;

import com.timepath.tf2.hudedit.temp.JarClassLoader;
import java.util.logging.Logger;

/**
 *
 * @author andrew
 */
public class JarLoader {
    
    public static void main(String[] args) {
        JarClassLoader cl = new JarClassLoader();
        try {
            cl.invokeMain("com.timepath.tf2.hudedit.Main", args);
        } catch(Throwable e) {
            LOG.severe(e.toString());
        }
    }
    
    private static final Logger LOG = Logger.getLogger(JarLoader.class.getName());
    
}
