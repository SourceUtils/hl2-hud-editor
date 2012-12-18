package com.timepath.tf2.hudedit.plaf;

import com.timepath.tf2.hudedit.Main;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JFileChooser;
import net.tomahawk.XFileDialog;

/**
 *
 * @author TimePath
 */
public class NativeFileChooser {
    
    private Frame parent;
    private String title;
    private String directory;
    
    public NativeFileChooser(Frame parent, String title, String directory) {
        this.parent = parent;
        this.title = title;
        this.directory = directory;
    }
    
    public File getFolder() {
        File f = null;
        String selection = null;
        if(Main.os == OS.Windows) {
            try {
                XFileDialog fd = new XFileDialog(parent);
                fd.setTitle(title);
                fd.setDirectory(directory);
                selection = fd.getFolder();
                fd.dispose();
            } catch(UnsupportedClassVersionError e) {

            }
        } else if(Main.os == OS.Mac) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            FileDialog fd = new FileDialog(parent, title);
            fd.setFilenameFilter(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            });
            fd.setMode(FileDialog.LOAD);
            fd.setVisible(true);
            if(fd.getDirectory() == null || fd.getFile() == null) {
                return null;
            }
            selection = fd.getDirectory() + fd.getFile();
        } else if(Main.os == OS.Linux) {
            File input = null;
            String zenity = "zenity --file-selection --title=Open";
            try {
                Process proc = Runtime.getRuntime().exec(zenity);  
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
                selection = br.readLine();  
            } catch (IOException e1) {
                JFileChooser fd = new JFileChooser();
                fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if(fd.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                    selection = fd.getSelectedFile().getPath();
                }
            }
        } else {
            JFileChooser fd = new JFileChooser();
            fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(fd.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                selection = fd.getSelectedFile().getPath();
            }
        }
        if(selection.equals("null")) {
            selection = null;
        }
        if(selection != null) {
            f = new File(selection);
        }
        return f;
    }
    
}
