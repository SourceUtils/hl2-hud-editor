package com.timepath.hl2.hudeditor;

import com.timepath.plaf.OS;
import com.timepath.vgui.Element;

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
    int mod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    JMenuItem aboutItem;

    EditorMenuBar(final HUDEditor hudEditor) {
        this.hudEditor = hudEditor;
        add(new JMenu(Main.getString("File")) {{
            setMnemonic(KeyEvent.VK_F);
            add(newItem = new JMenuItem(new CustomAction(Main.getString("New"), KeyEvent.VK_N, ks(KeyEvent.VK_N, mod)) {
                @Override
                void action(ActionEvent e) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }));
            newItem.setEnabled(false);
            add(openItem = new JMenuItem(new CustomAction("Open", KeyEvent.VK_O, ks(KeyEvent.VK_O, mod)) {
                @Override
                void action(ActionEvent e) {
                    hudEditor.locateHudDirectory();
                }
            }));
            addSeparator();
            closeItem = new JMenuItem(new CustomAction("Close", KeyEvent.VK_C, ks(KeyEvent.VK_W, mod)) {
                @Override
                void action(ActionEvent e) {
                    //                    close();
                }
            });
            if (OS.isMac()) add(closeItem);
            add(saveItem = new JMenuItem(new CustomAction("Save", KeyEvent.VK_S, ks(KeyEvent.VK_S, mod)) {
                @Override
                void action(ActionEvent e) {
                    if (hudEditor.canvas.getElements().isEmpty()) return;
                    hudEditor.info(hudEditor.canvas.getElements().getLast().save());
                }
            }));
            saveItem.setEnabled(false);
            add(saveAsItem = new JMenuItem(new CustomAction("Save As...",
                    KeyEvent.VK_A,
                    ks(KeyEvent.VK_S, mod | ActionEvent.SHIFT_MASK)) {
                @Override
                void action(ActionEvent e) {
                }
            }));
            saveAsItem.setEnabled(false);
            add(reloadItem = new JMenuItem(new CustomAction("Revert", KeyEvent.VK_R, ks(KeyEvent.VK_F5)) {
                @Override
                void action(ActionEvent e) {
                    hudEditor.loadAsync(hudEditor.lastLoaded);
                }
            }));
            reloadItem.setEnabled(false);
            exitItem = new JMenuItem(new CustomAction("Exit", KeyEvent.VK_X, ks(KeyEvent.VK_Q, mod)) {
                @Override
                void action(ActionEvent e) {
                    hudEditor.quit();
                }
            });
            if (!OS.isMac()) {
                addSeparator();
                add(closeItem);
                add(exitItem);
            }
        }});
        add(new JMenu("Edit") {{
            setMnemonic(KeyEvent.VK_E);
            add(undoItem = new JMenuItem(new CustomAction("Undo", KeyEvent.VK_U, ks(KeyEvent.VK_Z, mod)) {
                @Override
                void action(ActionEvent e) {
                }
            }));
            undoItem.setEnabled(false);
            // TODO: ctrl + shift + z
            add(redoItem = new JMenuItem(new CustomAction("Redo", KeyEvent.VK_R, ks(KeyEvent.VK_Y, mod)) {
                @Override
                void action(ActionEvent e) {
                }
            }));
            redoItem.setEnabled(false);
            addSeparator();
            add(cutItem = new JMenuItem(new CustomAction("Cut", KeyEvent.VK_T, ks(KeyEvent.VK_X, mod)) {
                @Override
                void action(ActionEvent e) {
                }
            }));
            cutItem.setEnabled(false);
            add(copyItem = new JMenuItem(new CustomAction("Copy", KeyEvent.VK_C, ks(KeyEvent.VK_C, mod)) {
                @Override
                void action(ActionEvent e) {
                }
            }));
            copyItem.setEnabled(false);
            add(pasteItem = new JMenuItem(new CustomAction("Paste", KeyEvent.VK_P, ks(KeyEvent.VK_V, mod)) {
                @Override
                void action(ActionEvent e) {
                }
            }));
            pasteItem.setEnabled(false);
            add(deleteItem = new JMenuItem(new CustomAction("Delete", KeyEvent.VK_D, ks(KeyEvent.VK_DELETE)) {
                @Override
                void action(ActionEvent e) {
                    hudEditor.canvas.removeElements(hudEditor.canvas.getSelected());
                }
            }));
            addSeparator();
            add(selectAllItem = new JMenuItem(new CustomAction("Select All", KeyEvent.VK_A, ks(KeyEvent.VK_A, mod)) {
                @Override
                void action(ActionEvent e) {
                    for (Element elem : hudEditor.canvas.getElements()) {
                        hudEditor.canvas.select(elem);
                    }
                }
            }));
            addSeparator();
            if (!OS.isMac()) {
                add(preferencesItem = new JMenuItem(new CustomAction("Preferences", KeyEvent.VK_E) {
                    @Override
                    void action(ActionEvent e) {
                        hudEditor.preferences();
                    }
                }));
            }
        }});
        add(new JMenu("View") {{
            setMnemonic(KeyEvent.VK_V);
            add(resolutionItem = new JMenuItem(new CustomAction("Change Resolution",
                    KeyEvent.VK_R,
                    ks(KeyEvent.VK_R, mod)) {
                @Override
                void action(ActionEvent e) {
                    hudEditor.changeResolution();
                }
            }));
            resolutionItem.setEnabled(false);
            add(previewItem = new JMenuItem(new CustomAction("Full Screen Preview",
                    KeyEvent.VK_F,
                    ks(KeyEvent.VK_F11)) {
                boolean fullscreen;

                @Override
                void action(ActionEvent e) {
                    hudEditor.dispose();
                    hudEditor.setUndecorated(!fullscreen);
                    hudEditor.setExtendedState(fullscreen ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
                    hudEditor.setVisible(true);
                    hudEditor.setJMenuBar(hudEditor.editorMenuBar);
                    hudEditor.pack();
                    hudEditor.toFront();
                    fullscreen = !fullscreen;
                }
            }));
            addSeparator();
            JMenuItem viewItem1 = new JMenuItem("Main Menu");
            viewItem1.setEnabled(false);
            add(viewItem1);
            JMenuItem viewItem2 = new JMenuItem("In-game (Health and ammo)");
            viewItem2.setEnabled(false);
            add(viewItem2);
            JMenuItem viewItem3 = new JMenuItem("Scoreboard");
            viewItem3.setEnabled(false);
            add(viewItem3);
            JMenuItem viewItem4 = new JMenuItem("CTF HUD");
            viewItem4.setEnabled(false);
            add(viewItem4);
        }});
        if (!OS.isMac()) {
            add(new JMenu("Help") {{
                setMnemonic(KeyEvent.VK_H);
                add(aboutItem = new JMenuItem(new CustomAction("About", KeyEvent.VK_A) {
                    @Override
                    void action(ActionEvent e) {
                        hudEditor.about();
                    }
                }));
            }});
        }
    }

    KeyStroke ks(int keyCode, int modifiers) {
        return KeyStroke.getKeyStroke(keyCode, modifiers);
    }

    KeyStroke ks(int keyCode) {
        return ks(keyCode, 0);
    }
}
