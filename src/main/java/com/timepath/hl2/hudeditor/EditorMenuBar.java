package com.timepath.hl2.hudeditor;

import com.timepath.plaf.OS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author TimePath
 */
class EditorMenuBar extends JMenuBar {

    HUDEditor hudEditor;
    JMenuItem newItem, openItem, saveItem, saveAsItem, reloadItem, closeItem, exitItem;
    JMenuItem undoItem, redoItem, cutItem, copyItem, pasteItem, deleteItem, selectAllItem, preferencesItem;
    JMenuItem resolutionItem, previewItem;
    int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    JMenuItem aboutItem;

    EditorMenuBar(final HUDEditor hudEditor) {
        this.hudEditor = hudEditor;
        JMenu fileMenu = new JMenu(Main.getString("File"));
        fileMenu.setMnemonic(KeyEvent.VK_F);
        add(fileMenu);
        newItem = new JMenuItem(new CustomAction(Main.getString("New"),
                                                 null,
                                                 KeyEvent.VK_N,
                                                 KeyStroke.getKeyStroke(KeyEvent.VK_N, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        newItem.setEnabled(false);
        fileMenu.add(newItem);
        openItem = new JMenuItem(new CustomAction("Open",
                                                  null,
                                                  KeyEvent.VK_O,
                                                  KeyStroke.getKeyStroke(KeyEvent.VK_O, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                hudEditor.locateHudDirectory();
            }
        });
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        closeItem = new JMenuItem(new CustomAction("Close",
                                                   null,
                                                   KeyEvent.VK_C,
                                                   KeyStroke.getKeyStroke(KeyEvent.VK_W, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                //                    close();
            }
        });
        if(OS.isMac()) {
            fileMenu.add(closeItem);
        }
        saveItem = new JMenuItem(new CustomAction("Save",
                                                  null,
                                                  KeyEvent.VK_S,
                                                  KeyStroke.getKeyStroke(KeyEvent.VK_S, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!hudEditor.canvas.getElements().isEmpty()) {
                    hudEditor.info(hudEditor.canvas.getElements()
                                                   .get(hudEditor.canvas.getElements().size() - 1)
                                                   .save());
                }
            }
        });
        saveItem.setEnabled(false);
        fileMenu.add(saveItem);
        saveAsItem = new JMenuItem(new CustomAction("Save As...",
                                                    null,
                                                    KeyEvent.VK_A,
                                                    KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                                           modifier + ActionEvent.SHIFT_MASK))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        saveAsItem.setEnabled(false);
        fileMenu.add(saveAsItem);
        reloadItem = new JMenuItem(new CustomAction("Revert",
                                                    null,
                                                    KeyEvent.VK_R,
                                                    KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                hudEditor.loadAsync(hudEditor.lastLoaded);
            }
        });
        reloadItem.setEnabled(false);
        fileMenu.add(reloadItem);
        exitItem = new JMenuItem(new CustomAction("Exit",
                                                  null,
                                                  KeyEvent.VK_X,
                                                  KeyStroke.getKeyStroke(KeyEvent.VK_Q, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                hudEditor.quit();
            }
        });
        if(!OS.isMac()) {
            fileMenu.addSeparator();
            fileMenu.add(closeItem);
            fileMenu.add(exitItem);
        }
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        add(editMenu);
        undoItem = new JMenuItem(new CustomAction("Undo",
                                                  null,
                                                  KeyEvent.VK_U,
                                                  KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        undoItem.setEnabled(false);
        editMenu.add(undoItem);
        redoItem = new JMenuItem(new CustomAction("Redo",
                                                  null,
                                                  KeyEvent.VK_R,
                                                  KeyStroke.getKeyStroke(KeyEvent.VK_Y, modifier))
        { // TODO: ctrl + shift + z
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        redoItem.setEnabled(false);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        cutItem = new JMenuItem(new CustomAction("Cut",
                                                 null,
                                                 KeyEvent.VK_T,
                                                 KeyStroke.getKeyStroke(KeyEvent.VK_X, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        cutItem.setEnabled(false);
        editMenu.add(cutItem);
        copyItem = new JMenuItem(new CustomAction("Copy",
                                                  null,
                                                  KeyEvent.VK_C,
                                                  KeyStroke.getKeyStroke(KeyEvent.VK_C, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        copyItem.setEnabled(false);
        editMenu.add(copyItem);
        pasteItem = new JMenuItem(new CustomAction("Paste",
                                                   null,
                                                   KeyEvent.VK_P,
                                                   KeyStroke.getKeyStroke(KeyEvent.VK_V, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        pasteItem.setEnabled(false);
        editMenu.add(pasteItem);
        deleteItem = new JMenuItem(new CustomAction("Delete",
                                                    null,
                                                    KeyEvent.VK_D,
                                                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                hudEditor.canvas.removeElements(hudEditor.canvas.getSelected());
            }
        });
        editMenu.add(deleteItem);
        editMenu.addSeparator();
        selectAllItem = new JMenuItem(new CustomAction("Select All",
                                                       null,
                                                       KeyEvent.VK_A,
                                                       KeyStroke.getKeyStroke(KeyEvent.VK_A, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(int i = 0; i < hudEditor.canvas.getElements().size(); i++) {
                    hudEditor.canvas.select(hudEditor.canvas.getElements().get(i));
                }
            }
        });
        editMenu.add(selectAllItem);
        editMenu.addSeparator();
        preferencesItem = new JMenuItem(new CustomAction("Preferences", null, KeyEvent.VK_E, null) {
            @Override
            public void actionPerformed(ActionEvent e) {
                hudEditor.preferences();
            }
        });
        if(!OS.isMac()) {
            editMenu.add(preferencesItem);
        }
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        add(viewMenu);
        resolutionItem = new JMenuItem(new CustomAction("Change Resolution",
                                                        null,
                                                        KeyEvent.VK_R,
                                                        KeyStroke.getKeyStroke(KeyEvent.VK_R, modifier))
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                hudEditor.changeResolution();
            }
        });
        resolutionItem.setEnabled(false);
        viewMenu.add(resolutionItem);
        previewItem = new JMenuItem(new CustomAction("Full Screen Preview",
                                                     null,
                                                     KeyEvent.VK_F,
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0))
        {
            private boolean fullscreen;

            @Override
            public void actionPerformed(ActionEvent e) {
                hudEditor.dispose();
                hudEditor.setUndecorated(!fullscreen);
                hudEditor.setExtendedState(fullscreen ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
                hudEditor.setVisible(true);
                hudEditor.setJMenuBar(hudEditor.jmb);
                hudEditor.pack();
                hudEditor.toFront();
                fullscreen = !fullscreen;
            }
        });
        viewMenu.add(previewItem);
        viewMenu.addSeparator();
        JMenuItem viewItem1 = new JMenuItem("Main Menu");
        viewItem1.setEnabled(false);
        viewMenu.add(viewItem1);
        JMenuItem viewItem2 = new JMenuItem("In-game (Health and ammo)");
        viewItem2.setEnabled(false);
        viewMenu.add(viewItem2);
        JMenuItem viewItem3 = new JMenuItem("Scoreboard");
        viewItem3.setEnabled(false);
        viewMenu.add(viewItem3);
        JMenuItem viewItem4 = new JMenuItem("CTF HUD");
        viewItem4.setEnabled(false);
        viewMenu.add(viewItem4);
        if(!OS.isMac()) {
            JMenu helpMenu = new JMenu("Help");
            helpMenu.setMnemonic(KeyEvent.VK_H);
            add(helpMenu);
            aboutItem = new JMenuItem(new CustomAction("About", null, KeyEvent.VK_A, null) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    hudEditor.about();
                }
            });
            helpMenu.add(aboutItem);
        }
    }
}
