package com.timepath.plaf.x;

import com.timepath.io.FileUtils;
import com.timepath.plaf.OS;
import com.timepath.tf2.hudeditor.Main;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import net.tomahawk.XFileDialog;

/**
 *
 * @author timepath
 */
public class NativeFileChooser {

    private static final Logger LOG = Logger.getLogger(NativeFileChooser.class.getName());

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
            if(directory != null) {
                fd.setDirectory(directory.getPath());
            }
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
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("zenity");
        cmd.add("--file-selection");
        cmd.add("--directory");
        cmd.add("--title=Open");
        cmd.add(directory != null ? "--filename=" + directory.getPath() : "");
        String windowClass = Main.projectName;
        try {
            Toolkit xToolkit = Toolkit.getDefaultToolkit();
            Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            windowClass = (String) awtAppClassNameField.get(xToolkit);
        } catch(Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        cmd.add("--class=" + windowClass);
//        cmd.add("--name=" + Main.projectName + " ");
        cmd.add("--window-icon=" + FileUtils.getLinuxStore() + "icons/" + Main.projectName + ".png");
//        cmd.add("--ok-label=TEXT ");
//        cmd.add("--cancel-label=TEXT ");

        String[] exec = new String[cmd.size()];
        cmd.toArray(exec);
        LOG.log(Level.INFO, "zenity: {0}", Arrays.toString(exec));
        final Process proc = Runtime.getRuntime().exec(exec);
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
