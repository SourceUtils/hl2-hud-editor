package com.timepath.tf2.hudeditor.plaf.linux;

import com.timepath.tf2.hudeditor.Main;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class LinuxDesktopLauncher {
    
    private static final Logger logger = Logger.getLogger(LinuxDesktopLauncher.class.getName());
    
    public static void create() {
        createLauncher();
        createIcon();
    }
    
    private static void createLauncher() {
        File destFile = new File(System.getenv("XDG_DATA_HOME") + "/applications/" + Main.appName + ".desktop");
        try {
            destFile.delete();
            destFile.createNewFile();

            PrintWriter destination = null;
            try {
                destination = new PrintWriter(new FileOutputStream(destFile));
                destination.println("[Desktop Entry]");
                destination.println("Version=1.0");
                destination.println("StartupWMClass=" + Main.appName);
                destination.println("Exec=java -jar Dropbox/Public/tf/Hud\\ Editor/TF2\\ HUD\\ Editor.jar %U"); // TODO: fixme. Get a dedicated install directory.
                destination.println("Icon=" + Main.appName);
                destination.println("Type=Application");
                destination.println("StartupNotify=true");
                destination.println("Terminal=false");
                destination.println("Keywords=TF2;HUD;Editor;WYSIWYG;");
                destination.println("Categories=Game;");
                destination.println("Name=TF2 HUD Editor");
                destination.println("GenericName=TF2 HUD Editor");
                destination.println("Comment=Edit TF2 HUDs");
                destination.println("Actions=New;");
                destination.println("[Desktop Action New]");
                destination.println("Name=New HUD");
                destination.println("Exec=java -jar Dropbox/Public/tf/Hud\\ Editor/TF2\\ HUD\\ Editor.jar"); // TODO: fixme
            } finally {
                if(destination != null) {
                    destination.close();
                }
                destFile.setExecutable(false);
            }
        } catch(IOException ex) {
            logger.log(Level.WARNING, null, ex);
        }
    }
    
    private static void createIcon() {
        String[] icons = {"png", "svg"};
        for(int i = 0; i < icons.length; i++) {
            try {
                File destFile = new File(System.getenv("XDG_DATA_HOME") + "/icons/" + Main.appName + "." + icons[i]);

                if(!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                if(!destFile.exists()) {
                    destFile.createNewFile();
                }

                InputStream in = LinuxDesktopLauncher.class.getResourceAsStream("/com/timepath/tf2/hudeditor/resources/Icon." + icons[i]);
                if(in == null) {
                    continue;
                }
                FileOutputStream out = new FileOutputStream(destFile);

                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    if(in != null) {
                        in.close();
                    }
                    if(out != null) {
                        out.close();
                    }
                    destFile.setExecutable(false);
                }
            } catch(IOException ex) {
                logger.log(Level.WARNING, null, ex);
                continue;
            }
        }
    }

}