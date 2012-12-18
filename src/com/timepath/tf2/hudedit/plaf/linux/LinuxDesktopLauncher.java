package com.timepath.tf2.hudedit.plaf.linux;

import com.timepath.tf2.hudedit.Main;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class LinuxDesktopLauncher {
    
    private static final Logger logger = Logger.getLogger(LinuxDesktopLauncher.class.getName());
    
    public static void create() {
        File sourceFile = new File("res/bin/" + Main.javaName + ".desktop");
        File destFile = new File(System.getProperty("user.home") + "/.local/share/applications/" + Main.javaName + ".desktop");
        if(sourceFile.exists() && !destFile.exists()) {
            try {
                if(!destFile.exists()) {
                    destFile.createNewFile();
                }

                FileChannel source = null;
                FileChannel destination = null;

                try {
                    source = new FileInputStream(sourceFile).getChannel();
                    destination = new FileOutputStream(destFile).getChannel();
                    destination.transferFrom(source, 0, source.size());
                }
                finally {
                    if(source != null) {
                        source.close();
                    }
                    if(destination != null) {
                        destination.close();
                    }
                    destFile.setExecutable(false);
                }
            } catch(IOException ex) {
                logger.log(Level.WARNING, null, ex);
            }
        }
    }

}