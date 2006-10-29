/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:01:42
 */
package org.jajuk.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.border.Border;

/**
 * @author Bart Cremers
 * @since 13-dec-2005
 */
public class JajukButton extends JButton {

    private static final long serialVersionUID = 1L;

    private static final Border JAJUK_BORDER = BorderFactory.createEmptyBorder(
	    4, 4, 4, 4);

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
	    putClientProperty("hideActionText", Boolean.TRUE); //$NON-NLS-1$
	}
	super.init(text, icon);
	setBorder(JAJUK_BORDER);
    }

    @Override
    protected void configurePropertiesFromAction(Action action) {
	if (action.getValue(Action.SMALL_ICON) != null) {
	    putClientProperty("hideActionText", Boolean.TRUE); //$NON-NLS-1$
	}

	super.configurePropertiesFromAction(action);

	KeyStroke stroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
	if (stroke != null) {
	    InputMap keyMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
	    keyMap.put(stroke, "action"); //$NON-NLS-1$

	    ActionMap actionMap = getActionMap();
	    actionMap.put("action", new ActionWrapper()); //$NON-NLS-1$
	}
    }

    private class ActionWrapper extends AbstractAction {
	private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e) {
	    fireActionPerformed(e);
	}
    }
}
