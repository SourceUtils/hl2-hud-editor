package com.timepath.hl2.hudeditor


import javax.swing.*
import java.awt.event.ActionEvent

/**
 * @author TimePath
 */
public abstract class CustomAction(s: String, mnemonic: Int, shortcut: KeyStroke? = null, icon: Icon? = null) : AbstractAction(Main.getString(s), icon) {

    {
        putValue(Action.MNEMONIC_KEY, mnemonic)
        putValue(Action.ACCELERATOR_KEY, shortcut)
    }

    override fun actionPerformed(e: ActionEvent) {
        action(e)
    }

    abstract fun action(e: ActionEvent)
}
