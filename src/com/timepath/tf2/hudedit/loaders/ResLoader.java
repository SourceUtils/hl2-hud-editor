package com.timepath.tf2.hudedit.loaders;

import com.timepath.tf2.hudedit.properties.HudFile;
import com.timepath.tf2.hudedit.util.Element;
import com.timepath.tf2.hudedit.util.Property;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * TODO: Do something nicer with entirely commented elements, hard to detect
 *
 * @author andrew
 */
public class ResLoader {

    static final Logger logger = Logger.getLogger(ResLoader.class.getName());
    public static Level loaderLevel = Level.FINE;

    private String hudFolder;

    public ResLoader(String hudFolder) {
        this.hudFolder = hudFolder;
    }

    public void populate(DefaultMutableTreeNode top) {
        processPopulate(new File(hudFolder), -1, top);
    }

    // TODO: Special exceptions for *scheme.res, hudlayout.res, 
    public static void analyze(final File file, final DefaultMutableTreeNode top) {
        if(file.isDirectory()) {
            return;
        }
        new Thread() { // threading this cuts loading times in half
            @Override
            public void run() {
                Scanner s = null;
                try {
                    s = new Scanner(new BufferedReader(new FileReader(file.getPath())));
                    // analyzing fileName
                    processAnalyze(s, top, new ArrayList<Property>(), file);
                } catch(FileNotFoundException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } finally {
                    if(s != null) {
                        s.close();
                    }
                }
            }
        }.start();
    }

    /**
     * @todo Sort alphabetically and by directory. directories first, files second
     * @param f
     * @param depth recursive: -1 = infinite, 0 = nothing, 1 = immediate
     * @param top
     */
    private static void processPopulate(File f, int depth, DefaultMutableTreeNode top) {
        if(depth == 0) {
            return;
        }
        File[] fileList = f.listFiles();
        Arrays.sort(fileList, dirAlphaComparator);

        for(int i = 0; i < fileList.length; i++) {
            boolean isDir = fileList[i].isDirectory();
            DefaultMutableTreeNode child = new DefaultMutableTreeNode();
            child.setUserObject(new HudFile(fileList[i]));
            if(isDir) {
                processPopulate(fileList[i], depth - 1, child);
                top.add(child);
            } else if(fileList[i].getName().endsWith(".res")) {
                analyze(fileList[i], child);
                top.add(child);
            }
        }
    }

    private static void processAnalyze(Scanner scanner, DefaultMutableTreeNode parent, ArrayList<Property> carried, File file) {
        while(scanner.hasNext()) {
            String line = scanner.nextLine().trim();
            String key = line.split("[ \t]+")[0];
            String val = line.substring(key.length()).trim();
            String info = null;

            if(line.equals("}")) { // for returning out of recursion: analyze: processAnalyze > processAnalyze < break < break
                Object obj = parent.getUserObject();
                if(obj instanceof Element) {
                    Element e = (Element) obj;
                    e.addProps(carried);
//                    e.validate();
                }
                logger.log(loaderLevel, "Returning");
                break;
            }

            if(line.equals("") || key.equals("{")) {
                continue;
            }

            // not the best
            int idx = val.contains("//") ? val.indexOf("//") : (val.contains("[") ? val.indexOf("[") : -1);
            if(idx >= 0) {
                info = val.substring(idx).trim();
                val = val.substring(0, idx).trim();
            }

            Property p = new Property(key, val, info);

            if(line.startsWith("#")) {
                p.setKey("#");
                p.setValue(line.substring(line.indexOf("#") + 1).trim());
                p.setInfo("");

//                logger.log(Level.INFO, "Carrying: {0}", line);
            } else if(line.startsWith("//")) {
                p.setKey("//");
                p.setValue(line.substring(line.indexOf("//") + 2)); // display this with .trim()
                p.setInfo("");

                logger.log(loaderLevel, "Carrying: {0}", line);

                carried.add(p);
            } else if(p.getValue().equals("")) { // very good assumption
                p.setValue("{");
            }

            if(!p.getKey().equals("//")) {
                if(p.getValue().equals("{")) { // make new sub
                    Element childElement = new Element(p.getKey(), p.getInfo());
                    childElement.setParentFile(file);
                    logger.log(loaderLevel, "Subbing: {0}", childElement);
                    for(int i = 0; i < carried.size(); i++) {
                        Property prop = carried.get(i);
                        prop.setInfo(prop.getValue());
                        prop.setValue("");
                    }
                    childElement.addProps(carried);

                    Object obj = parent.getUserObject();
                    if(obj instanceof Element) {
                        Element e = (Element) obj;
                        e.addElement(childElement);
//                        childElement.setParent(e);
                    }

                    DefaultMutableTreeNode child = new DefaultMutableTreeNode();
                    child.setUserObject(childElement);
                    parent.add(child);

                    processAnalyze(scanner, child, carried, file);
                } else { // properties
                    Object obj = parent.getUserObject();
                    if(obj instanceof Element) {
                        Element e = (Element) obj;
                        e.addProps(carried);
                        e.addProp(p);
                    }
                }
            }
        }
    }

    private static DirAlphaComparator dirAlphaComparator = new DirAlphaComparator();

    private static class DirAlphaComparator implements Comparator<File> {

        // Comparator interface requires defining compare method.
        @Override
        public int compare(File filea, File fileb) {
            //... Sort directories before files,
            //    otherwise alphabetical ignoring case.
            if(filea.isDirectory() && !fileb.isDirectory()) {
                return -1;

            } else if(!filea.isDirectory() && fileb.isDirectory()) {
                return 1;

            } else {
                return filea.getName().compareToIgnoreCase(fileb.getName());
            }
        }

    }

}