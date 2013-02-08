package com.timepath.tf2.gameinfo;

import com.timepath.util.file.FileChangeListener;
import com.timepath.util.file.FileMonitor;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author timepath
 */
@SuppressWarnings("serial")
public class ExternalConsole extends JFrame {

    private static final Logger LOG = Logger.getLogger(ExternalConsole.class.getName());

    JTextArea output;

    JTextField input;

    JScrollPane jsp;

    public ExternalConsole() {
        output = new JTextArea();
        output.setFont(new Font("Monospaced", Font.PLAIN, 15));

        jsp = new JScrollPane(output);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        input = new JTextField();

        this.setTitle("External console");
//        setAlwaysOnTop(true);
//        setUndecorated(true);
        this.setPreferredSize(new Dimension(800, 600));

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.getContentPane().add(jsp, BorderLayout.CENTER);
//        this.getContentPane().add(input, BorderLayout.SOUTH); // TODO: work out better way of sending input

        this.pack();
    }

    public void start() throws FileNotFoundException {
        this.setVisible(true);
        File log = new File("/home/timepath/.local/share/Steam/SteamApps/timepath/Team Fortress 2/tf/out.log");
        FileMonitor.getInstance().addFileChangeListener(new FileChangeListener() {
            public void fileChanged(File file) {
                update(file);
            }
        }, log, 500);
        update(log);
    }

    int currentUpdateLine;

    public void update(File file) {
        try {
            RandomAccessFile rf = new RandomAccessFile(file, "r");
            for(int i = 0; i < currentUpdateLine; i++) {
                rf.readLine();
            }
            String str;
            StringBuilder sb = new StringBuilder();
            while((str = rf.readLine()) != null) {
                sb.append(str).append("\n");
                currentUpdateLine++;
            }
            output.append(sb.toString());

            JScrollBar vertical = jsp.getVerticalScrollBar();
            if(vertical.getValue() == vertical.getMaximum()) {
                output.setCaretPosition(output.getDocument().getLength());
            }

        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String... args) {
        try {
            new ExternalConsole().start();
        } catch(FileNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}