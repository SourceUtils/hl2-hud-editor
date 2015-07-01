package com.timepath.hl2.hudeditor


import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.Icon
import javax.swing.KeyStroke

public abstract class CustomAction(s: String, mnemonic: Int, shortcut: KeyStroke? = null, icon: Icon? = null)
: AbstractAction(Main.getString(s), icon) {

    init {
        putValue(Action.MNEMONIC_KEY, mnemonic)
        putValue(Action.ACCELERATOR_KEY, shortcut)
    }

    override fun actionPerformed(e: ActionEvent) {
        action(e)
    }

    abstract fun action(e: ActionEvent)
}
