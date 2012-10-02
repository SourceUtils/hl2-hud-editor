*** Install the new FileChooserUI ***

The library gtkjfilechooser.jar contains the new GTKFile. Ensure that you're 
using the GTK Laf and then simply set the UI Property "FileChooserUI" to 
"eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI":

  if ("GTK look and feel".equals(UIManager.getLookAndFeel().getName())){
    UIManager.put("FileChooserUI", "eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI");
  }

You're now ready to start the file chooser:

  JFileChooser fileChooser = new JFileChooser();
  fileChooser.showOpenDialog(null);

*** Run a demo ***

The executable jar gtkjfilechooser-demo.jar demonstrates some of the 
capabilities of the JFileChooser object.  It brings up a window displaying 
several configuration controls that allow you to play with the JFileChooser 
options dynamically.
 
To run the gtkjfilechooser-demo demo:

  java -jar gtkjfilechooser-demo.jar

These instructions assume that this installation's version of the java
command is in your path.  If it isn't, then you should either
specify the complete path to the java command or update your
PATH environment variable as described in the installation
instructions for the Java(TM) SE Development Kit.
