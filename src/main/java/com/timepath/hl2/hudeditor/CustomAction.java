package com.timepath.hl2.hudeditor;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author TimePath
 */
public abstract class CustomAction extends AbstractAction {

    CustomAction(String s, int mnemonic, KeyStroke shortcut, Icon icon) {
        super(Main.getString(s), icon);
        putValue(Action.MNEMONIC_KEY, mnemonic);
        putValue(Action.ACCELERATOR_KEY, shortcut);
    }

    CustomAction(String s, int mnemonic, KeyStroke shortcut) {
        this(s, mnemonic, shortcut, null);
    }

    CustomAction(String s, int mnemonic) {
        this(s, mnemonic, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        action(e);
    }

    abstract void action(ActionEvent e);
}
