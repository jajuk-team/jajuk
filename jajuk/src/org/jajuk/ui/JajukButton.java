/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:01:42
 */
package org.jajuk.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;

/**
 * @author Bart Cremers
 * @since 13-dec-2005
 */
public class JajukButton extends JButton {

    private static final Border JAJUK_BORDER = BorderFactory.createEmptyBorder(4, 4, 4, 4);

    public JajukButton() {
        this(null, null);
    }

    public JajukButton(Icon icon) {
        this(null, icon);
    }

    public JajukButton(String text) {
        this(text, null);
    }

    public JajukButton(Action a) {
        super(a);
    }

    public JajukButton(String text, Icon icon) {
        super(text, icon);
    }

    @Override
    protected void init(String text, Icon icon) {
        // Hide action text on button
        if (icon != null) {
            putClientProperty("hideActionText", Boolean.TRUE);
        }
        super.init(text, icon);
        setBorder(JAJUK_BORDER);
    }

    @Override
    protected void configurePropertiesFromAction(Action action) {
        if (action.getValue(Action.SMALL_ICON) != null) {
            putClientProperty("hideActionText", Boolean.TRUE);
        }

        super.configurePropertiesFromAction(action);

        KeyStroke stroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
        if (stroke != null) {
            InputMap keyMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
            keyMap.put(stroke, "action");

            ActionMap actionMap = getActionMap();
            actionMap.put("action", new ActionWrapper());
        }
    }

    private class ActionWrapper extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            fireActionPerformed(e);
        }
    }
}
