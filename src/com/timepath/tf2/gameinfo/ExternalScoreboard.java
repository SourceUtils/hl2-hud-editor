package com.timepath.tf2.gameinfo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
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
public class ExternalScoreboard extends JFrame {
    
//    @Override
//    public void paint(Graphics graphics) {
//        super.paint(graphics);
//        Graphics2D g = (Graphics2D) graphics;
//        
//        g.drawString("Test", 0, 0);
//    }
    
//    
    
    JTextArea output;
    JTextField input;
    JScrollPane jsp;
    
    public ExternalScoreboard() {
        output = new JTextArea();
        output.setFont(new Font("Monospaced", Font.PLAIN, 15));
        
        jsp = new JScrollPane(output);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        input = new JTextField();
        
        this.setTitle("External killfeed");
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
            parse(sb.toString());
            
            JScrollBar vertical = jsp.getVerticalScrollBar();
            if(vertical.getValue() == vertical.getMaximum()) {
                output.setCaretPosition(output.getDocument().getLength());
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ExternalConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    int currentParseLine = 0;
    
    void parse(String lines) {
        String[] strings = lines.split("\n");
        for(int i = currentParseLine; i < strings.length; i++) {
            String s = strings[i];
//            if(s.length() != 0) {
                currentParseLine++;
//            }
            if(s.contains(" killed ")) {
                notify(s);
            }
            if(s.endsWith(" suicided.")) {
                // possible team/class switch
            }
            if(s.endsWith(" connected")) {
                
            }
            if(s.startsWith("Dropped") && s.contains("from server")) {
                
            }
            // names defended/captured 'capname' for team#
            if(s.contains(" for team #")) {
                // team 0 = spectator, team 2 = red, team 3 = blu
            }
            if(s.equals("Teams have been switched.")) {
                
            }
        }
        
        output.append("\nPlayers:\n");
        for(int i = 0; i < players.size(); i++) {
           output.append(players.get(i).toString() + "\n");
        }
        
        Player me = getPlayer("TimePath");
        output.append("\nAllies:\n");
        for(int i = 0; i < me.allies.size(); i++) {
            output.append(me.allies.get(i) + "\n");
        }
        output.append("\nEnemies:\n");
        for(int i = 0; i < me.enemies.size(); i++) {
            output.append(me.enemies.get(i) + "\n");
        }
        output.append("\n");
    }
    
    ArrayList<Player> players = new ArrayList<Player>();
    
    Player getPlayer(String name) {
        for(int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if(p.name.equals(name)) {
                return p;
            }
        }
        players.add(new Player(name));
        Player p = getPlayer(name);
        return p;
    }
    
    void notify(String s) {
        Player killer = getPlayer(s.split(" killed ")[0]);
        Player victim = getPlayer(s.split(" killed ")[1].split(" with ")[0]);
        String weapon = s.split(" killed ")[1].split(" with ")[1];
        
        Player.exchangeInfo(victim, killer);
        
        boolean crit = weapon.endsWith("(crit)");
        weapon = weapon.substring(0, weapon.indexOf("."));
        if(crit) {
            weapon = "*" + weapon + "*";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(killer.name).append(" = ").append(weapon).append(" -> ").append(victim.name);
        output.append(sb.toString() + "\n");
    }
    
    public static void main(String... args) {
        try {
            new ExternalScoreboard().start();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExternalConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
