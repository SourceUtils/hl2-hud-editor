package com.timepath.tf2.gameinfo;

import java.util.ArrayList;

/**
 *
 * @author timepath
 */
public class Player {
        
    ArrayList<Player> enemies = new ArrayList<Player>();

    void addEnemy(Player other) {
        if(other == this) {
            return;
        }
        if(!enemies.contains(other)) {
            enemies.add(other);
        }
        if(!other.enemies.contains(this)) {
            other.enemies.add(this);
        }
    }
    
    void addEnemyR(Player other) {
        addEnemy(other);
        for(int j = 0; j < allies.size(); j++) {
            allies.get(j).addEnemy(other);
        }
    }

    void addEnemies(ArrayList<Player> list) {
        for(int i = 0; i < list.size(); i++) {
            addEnemyR(list.get(i));
            for(int j = 0; j < allies.size(); j++) {
                allies.get(j).addEnemyR(list.get(i));
            }
        }
    }

    ArrayList<Player> allies = new ArrayList<Player>();

    void addAlly(Player other) {
        if(other == this) {
            return;
        }
        if(!allies.contains(other)) {
            allies.add(other);
        }
        if(!other.allies.contains(this)) {
            other.allies.add(this);
        }
    }
    
    void addAllyR(Player other) {
        addAlly(other);
        for(int j = 0; j < allies.size(); j++) {
            allies.get(j).addAlly(other);
        }
    }

    void addAllies(ArrayList<Player> list) {
        for(int i = 0; i < list.size(); i++) {
            addAllyR(list.get(i));
            for(int j = 0; j < enemies.size(); j++) {
                enemies.get(j).addEnemyR(list.get(i));
            }
        }
    }

    String name;

    Player(String name) {
        this.name = name;
    }

     /**
     * Makes players enemies
     * 
     * Function:
     * Adds a new enemy.
     * Makes your enemy's enemies your allies.
     * Makes your enemy's allies your enemies.
     * Informs all your allies of your new enemy.
     * 
     * @param v Victim
     * @param k Killer
     * 
     * TODO: make the order unimportant
     */
    static void exchangeInfo(Player v, Player k) {        
        ArrayList<Player> vAllies = new ArrayList<Player>(v.allies);
        ArrayList<Player> vEnemies = new ArrayList<Player>(v.enemies);

        ArrayList<Player> kAllies = new ArrayList<Player>(k.allies);
        ArrayList<Player> kEnemies = new ArrayList<Player>(k.enemies);

        if(k.allies.contains(v) || v.allies.contains(k)) { // Traitor
            for(int i = 0; i < k.allies.size(); i++) {
                k.allies.get(i).allies.remove(k);
                
                k.allies.get(i).enemies.remove(k);
                k.allies.get(i).enemies.add(k);
            }
            for(int i = 0; i < v.allies.size(); i++) {
                v.allies.get(i).allies.remove(k);
                
                v.allies.get(i).enemies.remove(k);
                v.allies.get(i).enemies.add(k);
            }
            
            for(int i = 0; i < k.enemies.size(); i++) {
                k.enemies.get(i).enemies.remove(k);
                
                k.enemies.get(i).allies.remove(k);
                k.enemies.get(i).allies.add(k);
            }
            for(int i = 0; i < v.enemies.size(); i++) {
                v.enemies.get(i).enemies.remove(k);
                
                v.enemies.get(i).allies.remove(k);
                v.enemies.get(i).allies.add(k);
            }
            
            k.allies = new ArrayList<Player>();
            k.enemies = new ArrayList<Player>();
            
            k.allies.addAll(kEnemies);
//            k.allies.addAll(vEnemies);
            k.enemies.addAll(kAllies);
//            k.enemies.addAll(vAllies);
        } else {
            v.addEnemy(k);
            k.addEnemy(v);

            v.addAllies(kEnemies);
            v.addEnemies(kAllies);

            k.addAllies(vEnemies);
            k.addEnemies(vAllies);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="toString()">
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);

        sb.append(" (e:{");
        ArrayList<Player> myEnemies = enemies;
        for(int i = 0; i < enemies.size(); i++) {
            sb.append(myEnemies.get(i).name);
            if(i + 1 < enemies.size()) {
                sb.append(", ");
            }
        }
        sb.append("}, a:{");
        ArrayList<Player> myAllies = allies;
        for(int i = 0; i < allies.size(); i++) {
            sb.append(myAllies.get(i).name);
            if(i + 1 < allies.size()) {
                sb.append(", ");
            }
        }
        sb.append("})");

        return sb.toString();
    }
    //</editor-fold>
        
}