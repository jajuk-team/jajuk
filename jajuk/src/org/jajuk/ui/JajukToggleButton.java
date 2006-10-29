/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:01:42
 */
package org.jajuk.ui;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.border.Border;

/**
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class JajukToggleButton extends JajukButton {
    private static final long serialVersionUID = 1L;

    private static final Border PRESSED_BORDER = BorderFactory
	    .createLoweredBevelBorder();

    private static final Border UNPRESSED_BORDER = BorderFactory
	    .createRaisedBevelBorder();

    public JajukToggleButton() {
	this(false);
    }

    public JajukToggleButton(boolean selected) {
	this(null, null, selected);
    }

    public JajukToggleButton(Icon icon) {
	this(icon, false);
    }

    public JajukToggleButton(Icon icon, boolean selected) {
	this(null, icon, selected);
    }

    public JajukToggleButton(String text) {
	this(text, false);
    }

    public JajukToggleButton(String text, boolean selected) {
	this(text, null, selected);
    }

    public JajukToggleButton(Action a) {
	this(a, false);
    }

    public JajukToggleButton(Action a, boolean selected) {
	super(a);
	setSelected(selected);
    }

    public JajukToggleButton(String text, Icon icon) {
	this(text, icon, false);
    }

    public JajukToggleButton(String text, Icon icon, boolean selected) {
	super(text, icon);
	setSelected(selected);
    }

    @Override
    public void setSelected(boolean b) {
	super.setSelected(b);
	setBorder(b ? PRESSED_BORDER : UNPRESSED_BORDER);
    }

    @Override
    protected void fireActionPerformed(ActionEvent event) {
	setSelected(!isSelected());
	super.fireActionPerformed(event);
    }
}
