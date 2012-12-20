package com.timepath.tf2.hudeditor.loaders;

import com.timepath.tf2.hudeditor.util.Element;
import com.timepath.tf2.hudeditor.util.HudFont;
import com.timepath.tf2.hudeditor.util.Property;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * 
 * TODO: Threading. This class can probably be executed as a thread.
 * If there are multiple values with platform tags, all the values become the last loaded value tag, but only if the variable is recognised
 * 
 * @author TimePath
 */
public class ResLoader {

    static final Logger logger = Logger.getLogger(ResLoader.class.getName());

    private String hudFolder;

    public ResLoader(String hudFolder) {
        this.hudFolder = hudFolder;
    }

    public void populate(DefaultMutableTreeNode top) {
        processPopulate(new File(hudFolder), -1, top);
    }
    
    /**
     * @param f
     * @param depth recursive: -1 = infinite, 0 = nothing, 1 = immediate
     * @param top
     */
    private void processPopulate(File f, int depth, DefaultMutableTreeNode top) {
        if(depth == 0) {
            return;
        }
        File[] fileList = f.listFiles();
        Arrays.sort(fileList, dirAlphaComparator);

        for(int i = 0; i < fileList.length; i++) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode();
            child.setUserObject(fileList[i]);
            if(fileList[i].isDirectory()) {
                processPopulate(fileList[i], depth - 1, child);
                if(child.getChildCount() > 0) { // got sick of seeing empty folders
                    top.add(child);
                }
            } else if(fileList[i].getName().endsWith(".res")) {
                analyze(fileList[i], child);
                top.add(child);
            }
        }
    }
    
    public static HashMap<String,HudFont> fonts = new HashMap<String,HudFont>(); 

    // TODO: Special exceptions for *scheme.res, hudlayout.res, 
    public void analyze(final File file, final DefaultMutableTreeNode top) {
        if(file.isDirectory()) {
            return;
        }
        new Thread() { // threading this cuts loading times in half
            @Override
            public void run() {
                Scanner s = null;
                try {
                    RandomAccessFile rf = new RandomAccessFile(file.getPath(), "r");
                    s = new Scanner(rf.getChannel());
                    processAnalyze(s, top, new ArrayList<Property>(), file);
                    if(file.getName().equalsIgnoreCase("ClientScheme.res")) {
                        clientScheme(top);
                    }
                } catch(FileNotFoundException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } finally {
                    if(s != null) {
                        s.close();
                    }
                }
            }

            private void clientScheme(DefaultMutableTreeNode props) {
                System.out.println("Found clientscheme");
                TreeNode fontNode = props.getChildAt(0).getChildAt(3); // XXX: hardcoded
                for(int i = 0; i < fontNode.getChildCount(); i++) {
                    TreeNode font = fontNode.getChildAt(i);
                    TreeNode detailFont = font.getChildAt(0); // XXX: hardcoded detail level
                    Element fontElement = (Element) ((DefaultMutableTreeNode) detailFont).getUserObject();
                    String fontName = font.toString().replaceAll("\"", ""); // Some use quotes.. oh well
                    fonts.put(fontName, new HudFont(fontName, fontElement));
                }
                System.out.println("Loaded clientscheme");
            }
        }.start();
    }

    private void processAnalyze(Scanner scanner, DefaultMutableTreeNode parent, ArrayList<Property> carried, File file) {
        while(scanner.hasNext()) {
            // Read values
            String line = scanner.nextLine().trim(); // TODO: What if the line looks like "Scheme{Colors{"? Damn you Broesel...
            String key = line.split("[ \t]+")[0];
            String val = line.substring(key.length()).trim();
            String info = null;
            
            // not the best - what if both are used? ... splits at //, then [
            int idx = val.contains("//") ? val.indexOf("//") : (val.contains("[") ? val.indexOf("[") : -1);
            if(idx >= 0) {
                info = val.substring(idx).trim();
                val = val.substring(0, idx).trim();
            }
            if(val.length() == 0) { // very good assumption
                val = "{";
            }
            
            // Process values
            
            Property p = new Property(key, val, info);

            if(line.equals("}")) { // for returning out of recursion: analyze: processAnalyze > processAnalyze < break < break
                Object obj = parent.getUserObject();
                if(obj instanceof Element) {
                    Element e = (Element) obj;
                    e.addProps(carried);
//                    e.validate(); // TODO: Thread safety. oops
                }
                logger.log(Level.FINE, "Returning");
                break;
            } else if(line.length() == 0) {
                p.setKey("\\n");
                p.setValue("\\n");
                p.setInfo("");
                logger.log(Level.FINE, "Carrying: {0}", line);
                carried.add(p);
                continue;
            } else if(line.equals("{")) { // just a { on its own line
                continue;
            } else if(line.startsWith("#")) {
                p.setKey("#");
                p.setValue(line.substring(line.indexOf('#') + 1));
                p.setInfo("");
                logger.log(Level.INFO, "Carrying: {0}", line);
                carried.add(p);
                continue;
            } else if(line.startsWith("//")) {
                p.setKey("//");
                p.setValue(line.substring(line.indexOf("//") + 2)); // display this with .trim()
                p.setInfo("");
                logger.log(Level.FINE, "Carrying: {0}", line);
                carried.add(p);
                continue;
            }

            if(p.getValue().equals("{")) { // make new sub
                Element childElement = new Element(p.getKey(), p.getInfo());
                childElement.setParentFile(file);
                logger.log(Level.FINE, "Subbing: {0}", childElement);
                // If setting the properties of a section, put put the value in the info spot
                for(int i = 0; i < carried.size(); i++) {
                    Property prop = carried.get(i);
                    prop.setInfo(prop.getValue());
                    prop.setValue("");
                }
                childElement.addProps(carried);

                Object obj = parent.getUserObject();
                if(obj instanceof Element) {
                    Element e = (Element) obj;
                    e.addChild(childElement);
                }

                DefaultMutableTreeNode child = new DefaultMutableTreeNode(childElement);
//                child.setUserObject(childElement);
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

    private static Comparator<File> dirAlphaComparator = new Comparator<File>() {
        
        /**
         * Alphabetically sorts directories before files ignoring case.
         */
        @Override
        public int compare(File a, File b) {
            if(a.isDirectory() && !b.isDirectory()) {
                return -1;
            } else if(!a.isDirectory() && b.isDirectory()) {
                return 1;
            } else {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        }

    };

}