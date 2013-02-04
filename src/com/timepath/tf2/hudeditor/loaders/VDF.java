package com.timepath.tf2.hudeditor.loaders;

import com.timepath.tf2.hudeditor.util.Element;
import com.timepath.tf2.hudeditor.util.Property;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * 
 * Standard KeyValues format loader
 * 
 * @author TimePath
 */
public class VDF {

    static final Logger logger = Logger.getLogger(VDF.class.getName());

    public VDF(String file) {
        this.analyze(new File(file));
    }
    
    public void analyze(final File file) {
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
                    DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode();
                    processAnalyze(s, dmtn, new ArrayList<Property>(), file);
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

    private void processAnalyze(Scanner scanner, DefaultMutableTreeNode parent, ArrayList<Property> carried, File file) {
        while(scanner.hasNext()) {
            // Read values
            String line = scanner.nextLine().trim(); // TODO: What if the line looks like "Scheme{Colors{"? Damn you Broesel...
            String key = line.split("[ \t]+")[0];
            String val = line.substring(key.length()).trim();
            String info = null;
            
            // not the best - what if both are used? ... splits at //, then [
            int idx = val.contains("//") ? val.indexOf("//") : (val.contains("[") ? val.indexOf('[') : -1);
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

}