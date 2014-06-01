package com.timepath.hl2.hudeditor;

import javax.swing.*;

/**
 * @author TimePath
 */
public abstract class CustomAction extends AbstractAction {

    CustomAction(String s, Icon icon, int mnemonic, KeyStroke shortcut) {
        super(Main.getString(s), icon);
        putValue(Action.MNEMONIC_KEY, mnemonic);
        putValue(Action.ACCELERATOR_KEY, shortcut);
    }
}
