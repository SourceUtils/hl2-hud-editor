package com.timepath.plaf.x;

import com.timepath.io.FileUtils;
import com.timepath.plaf.OS;
import com.timepath.tf2.hudeditor.Main;
import java.awt.Component;
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

    private File root;

    public NativeFileChooser(Frame parent, String title, File directory) {
        this.parent = parent;
        this.title = title;
        this.root = directory;
    }

    public File choose(boolean directoryMode, boolean saveDialog) {
        String selection;
        if(Main.os == OS.Windows) {
            selection = xfd(directoryMode, saveDialog);
        } else if(Main.os == OS.Mac) {
            selection = awt(directoryMode, saveDialog);
        } else if(Main.os == OS.Linux) {
            try {
                selection = zenity(directoryMode, saveDialog);
            } catch(IOException ex) {
                selection = swing(directoryMode, saveDialog);
            }
        } else {
            selection = swing(directoryMode, saveDialog);
        }
        if(selection == null) {
            return null;
        } else {
            return new File(selection);
        }
    }

    private String awt(boolean directoryMode, boolean saveDialog) {
        if(Main.os == OS.Mac) {
            System.setProperty("apple.awt.fileDialogForDirectories", Boolean.toString(directoryMode));
        }
        FileDialog fd = new FileDialog(parent, title);
        if(directoryMode) {
            fd.setFilenameFilter(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            });
        }
        if(root != null) {
            fd.setDirectory(root.getPath());
        }
        fd.setMode(saveDialog ? FileDialog.SAVE : FileDialog.LOAD);
        fd.setVisible(true);
        if(fd.getDirectory() == null || fd.getFile() == null) {
            return null;
        }
        return fd.getDirectory() + fd.getFile();
    }

    private String swing(boolean directoryMode, boolean saveDialog) {
        if(Main.os == OS.Linux) {
//            UIManager.put("FileChooserUI", "eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI");
        }
        JFileChooser fd = new JFileChooser(root);
        fd.setFileSelectionMode(directoryMode ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES);
        fd.setDialogType(saveDialog ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG);
        if(fd.showDialog(parent, null) == JFileChooser.APPROVE_OPTION) {
            return fd.getSelectedFile().getPath();
        }
        return null;
    }

    private String xfd(boolean directoryMode, boolean saveDialog) {
        String selection;
        XFileDialog fd = new XFileDialog(parent);
        fd.setTitle(title);
        if(root != null) {
            fd.setDirectory(root.getPath());
        }
        if(directoryMode) {
            selection = fd.getFolder();
        } else {
            if(saveDialog) {
                selection = fd.getSaveFile();
            } else {
                selection = fd.getFile();
            }
        }
        fd.dispose();
        return selection;
    }

    private String zenity(boolean directoryMode, boolean saveDialog) throws IOException {
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("zenity");
        //cmd.add("--multiple")
        cmd.add("--file-selection");
        if(directoryMode) {
            cmd.add("--directory");
        }
        if(saveDialog) {
            cmd.add("--save");
        }
        cmd.add(root != null ? "--filename=" + root.getPath() : "");
        String windowClass = Main.projectName;
        try {
            Toolkit xToolkit = Toolkit.getDefaultToolkit();
            Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
            boolean accessible = awtAppClassNameField.isAccessible();
            awtAppClassNameField.setAccessible(true);
            windowClass = (String) awtAppClassNameField.get(xToolkit);
            awtAppClassNameField.setAccessible(accessible);
        } catch(Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        cmd.add("--class=" + windowClass);
//        cmd.add("--name=" + Main.projectName + " ");
        cmd.add("--window-icon=" + FileUtils.getLinuxStore() + "icons/" + Main.projectName + ".png");
        cmd.add("--title=" + (saveDialog ? "Save" : "Open"));
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
}
