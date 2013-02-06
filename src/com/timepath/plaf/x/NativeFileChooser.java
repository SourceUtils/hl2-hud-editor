package com.timepath.plaf.x;

import com.timepath.plaf.OS;
import com.timepath.plaf.linux.LinuxDesktopLauncher;
import com.timepath.tf2.hudeditor.Main;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import net.tomahawk.XFileDialog;

/**
 *
 * @author timepath
 */
public class NativeFileChooser {
    
    private static final Logger logger = Logger.getLogger(NativeFileChooser.class.getName());
    
    private Frame parent;
    private String title;
    private File directory;
    
    public NativeFileChooser(Frame parent, String title, File directory) {
        this.parent = parent;
        this.title = title;
        this.directory = directory;
    }
    
    public File getFolder() {
        String selection;
        if(Main.os == OS.Windows) {
            XFileDialog fd = new XFileDialog(parent);
            fd.setTitle(title);
            fd.setDirectory(directory.getPath());
            selection = fd.getFolder();
            fd.dispose();
        } else if(Main.os == OS.Mac) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            selection = awt();
        } else if(Main.os == OS.Linux) {
            try {
                selection = zenity();  
            } catch(IOException ex) {
//                UIManager.put("FileChooserUI", "eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI");
                selection = swing();
            }
        } else {
            selection = swing();
        }
        if(selection == null) {
            return null;
        } else {
            return new File(selection);
        }
    }

    private String swing() {
        JFileChooser fd = new JFileChooser(directory);
        fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fd.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return fd.getSelectedFile().getPath();
        }
        return null;
    }
    
    private String zenity() throws IOException {
        StringBuilder cmd = new StringBuilder();
        cmd.append("zenity ");
        cmd.append("--file-selection ");
        cmd.append("--directory ");
        cmd.append("--title=Open ");
        String folder = (directory != null ? ("--filename=" + directory.getPath()) + "" : ""); // FIXME: does not work when directory has spaces
        String folder2 = "";
        for(int i = 0; i < folder.length(); i++) {
            String s = "" + folder.charAt(i);
            if(" ".equals(s)) {
                s = "\\ ";
            }
            folder2 += s;
        }
        folder2 += "/ ";
        cmd.append(folder2);
        cmd.append("--class=" + Main.projectName + " ");
        cmd.append("--name=" + Main.projectName + " ");
        cmd.append("--window-icon=").append(LinuxDesktopLauncher.getStore()).append("/icons/" + Main.projectName + ".png");
//        cmd.append("--ok-label=TEXT ");
//        cmd.append("--cancel-label=TEXT ");

        String zenity = cmd.toString();
        logger.log(Level.INFO, "zenity: {0}", zenity);
        
        final Process proc = Runtime.getRuntime().exec(zenity);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                proc.destroy();
            }
        });
        BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        return br.readLine();
    }
    
    private String awt() {
        FileDialog fd = new FileDialog(parent, title);
        fd.setFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });
        if(directory != null) {
            fd.setDirectory(directory.getPath());
        }
        fd.setMode(FileDialog.LOAD);
        fd.setVisible(true);
        if(fd.getDirectory() == null || fd.getFile() == null) {
            return null;
        }
        return fd.getDirectory() + fd.getFile();
    }
    
}
