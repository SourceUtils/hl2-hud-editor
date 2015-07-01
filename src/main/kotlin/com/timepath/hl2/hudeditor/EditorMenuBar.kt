package com.timepath.hl2.hudeditor

import com.timepath.plaf.OS
import java.awt.Frame
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.KeyStroke

class EditorMenuBar(var hudEditor: HUDEditor) : JMenuBar() {
    var reloadItem = JMenuItem(object : CustomAction("Revert", KeyEvent.VK_R, ks(KeyEvent.VK_F5)) {
        override fun action(e: ActionEvent) {
            hudEditor.lastLoaded?.let {
                hudEditor.loadAsync(it)
            }
        }
    })
    val mod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()

    init {
        add(object : JMenu(Main.getString("File")) {
            init {
                setMnemonic(KeyEvent.VK_F)
                var newItem = JMenuItem(object : CustomAction(Main.getString("New"), KeyEvent.VK_N, ks(KeyEvent.VK_N, mod)) {
                    override fun action(e: ActionEvent) {
                        throw UnsupportedOperationException("Not supported yet.")
                    }
                })
                add(newItem)
                newItem.setEnabled(false)
                var openItem = JMenuItem(object : CustomAction("Open", KeyEvent.VK_O, ks(KeyEvent.VK_O, mod)) {
                    override fun action(e: ActionEvent) {
                        hudEditor.locateHudDirectory()
                    }
                })
                add(openItem)
                addSeparator()
                var closeItem = JMenuItem(object : CustomAction("Close", KeyEvent.VK_C, ks(KeyEvent.VK_W, mod)) {
                    override fun action(e: ActionEvent) {
                        //                    close();
                    }
                })
                if (OS.isMac()) add(closeItem)
                var saveItem = JMenuItem(object : CustomAction("Save", KeyEvent.VK_S, ks(KeyEvent.VK_S, mod)) {
                    override fun action(e: ActionEvent) {
                        if (hudEditor.canvas!!.elements.isEmpty()) return
                        hudEditor.info(hudEditor.canvas!!.elements.getLast().save())
                    }
                })
                add(saveItem)
                saveItem.setEnabled(false)
                var saveAsItem = JMenuItem(object : CustomAction("Save As...", KeyEvent.VK_A, ks(KeyEvent.VK_S, mod or ActionEvent.SHIFT_MASK)) {
                    override fun action(e: ActionEvent) {
                    }
                })
                add(saveAsItem)
                saveAsItem.setEnabled(false)

                add(reloadItem)
                reloadItem.setEnabled(false)
                var exitItem = JMenuItem(object : CustomAction("Exit", KeyEvent.VK_X, ks(KeyEvent.VK_Q, mod)) {
                    override fun action(e: ActionEvent) {
                        hudEditor.quit()
                    }
                })
                if (!OS.isMac()) {
                    addSeparator()
                    add(closeItem)
                    add(exitItem)
                }
            }
        })
        add(object : JMenu("Edit") {
            init {
                setMnemonic(KeyEvent.VK_E)
                var undoItem = JMenuItem(object : CustomAction("Undo", KeyEvent.VK_U, ks(KeyEvent.VK_Z, mod)) {
                    override fun action(e: ActionEvent) {
                    }
                })
                add(undoItem)
                undoItem.setEnabled(false)
                // TODO: ctrl + shift + z
                var redoItem = JMenuItem(object : CustomAction("Redo", KeyEvent.VK_R, ks(KeyEvent.VK_Y, mod)) {
                    override fun action(e: ActionEvent) {
                    }
                })
                add(redoItem)
                redoItem.setEnabled(false)
                addSeparator()
                var cutItem = JMenuItem(object : CustomAction("Cut", KeyEvent.VK_T, ks(KeyEvent.VK_X, mod)) {
                    override fun action(e: ActionEvent) {
                    }
                })
                add(cutItem)
                cutItem.setEnabled(false)
                var copyItem = JMenuItem(object : CustomAction("Copy", KeyEvent.VK_C, ks(KeyEvent.VK_C, mod)) {
                    override fun action(e: ActionEvent) {
                    }
                })
                add(copyItem)
                copyItem.setEnabled(false)
                var pasteItem = JMenuItem(object : CustomAction("Paste", KeyEvent.VK_P, ks(KeyEvent.VK_V, mod)) {
                    override fun action(e: ActionEvent) {
                    }
                })
                add(pasteItem)
                pasteItem.setEnabled(false)
                var deleteItem = JMenuItem(object : CustomAction("Delete", KeyEvent.VK_D, ks(KeyEvent.VK_DELETE)) {
                    override fun action(e: ActionEvent) {
                        hudEditor.canvas!!.r.removeElements(hudEditor.canvas!!.r.selected)
                    }
                })
                add(deleteItem)
                addSeparator()
                var selectAllItem = JMenuItem(object : CustomAction("Select All", KeyEvent.VK_A, ks(KeyEvent.VK_A, mod)) {
                    override fun action(e: ActionEvent) {
                        for (elem in hudEditor.canvas!!.elements) {
                            hudEditor.canvas!!.r.select(elem)
                        }
                    }
                })
                add(selectAllItem)
                addSeparator()
                if (!OS.isMac()) {
                    var preferencesItem = JMenuItem(object : CustomAction("Preferences", KeyEvent.VK_E) {
                        override fun action(e: ActionEvent) {
                            hudEditor.preferences()
                        }
                    })
                    add(preferencesItem)
                }
            }
        })
        add(object : JMenu("View") {
            init {
                setMnemonic(KeyEvent.VK_V)
                var resolutionItem = JMenuItem(object : CustomAction("Change Resolution", KeyEvent.VK_R, ks(KeyEvent.VK_R, mod)) {
                    override fun action(e: ActionEvent) {
                        hudEditor.changeResolution()
                    }
                })
                add(resolutionItem)
                resolutionItem.setEnabled(false)
                var previewItem = JMenuItem(object : CustomAction("Full Screen Preview", KeyEvent.VK_F, ks(KeyEvent.VK_F11)) {
                    var fullscreen: Boolean = false

                    override fun action(e: ActionEvent) {
                        hudEditor.dispose()
                        hudEditor.setUndecorated(!fullscreen)
                        hudEditor.setExtendedState(if (fullscreen) Frame.NORMAL else Frame.MAXIMIZED_BOTH)
                        hudEditor.setVisible(true)
                        hudEditor.setJMenuBar(hudEditor.editorMenuBar)
                        hudEditor.pack()
                        hudEditor.toFront()
                        fullscreen = !fullscreen
                    }
                })
                add(previewItem)
                addSeparator()
                val viewItem1 = JMenuItem("Main Menu")
                viewItem1.setEnabled(false)
                add(viewItem1)
                val viewItem2 = JMenuItem("In-game (Health and ammo)")
                viewItem2.setEnabled(false)
                add(viewItem2)
                val viewItem3 = JMenuItem("Scoreboard")
                viewItem3.setEnabled(false)
                add(viewItem3)
                val viewItem4 = JMenuItem("CTF HUD")
                viewItem4.setEnabled(false)
                add(viewItem4)
            }
        })
        if (!OS.isMac()) {
            add(object : JMenu("Help") {
                init {
                    setMnemonic(KeyEvent.VK_H)
                    var aboutItem = JMenuItem(object : CustomAction("About", KeyEvent.VK_A) {
                        override fun action(e: ActionEvent) {
                            hudEditor.about()
                        }
                    })
                    add(aboutItem)
                }
            })
        }
    }

    fun ks(keyCode: Int, modifiers: Int): KeyStroke {
        return KeyStroke.getKeyStroke(keyCode, modifiers)
    }

    fun ks(keyCode: Int): KeyStroke {
        return ks(keyCode, 0)
    }
}
