/*
 *  Jajuk
 *  Copyright (C) 2003 Bart Cremers
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * $Revision$
 */
package org.jajuk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.EventQueue;
import java.awt.AWTEvent;
import javax.swing.*;
import javax.swing.plaf.ButtonUI;

/**
 * A specialized button, firing successive <code>ActionEvent</code>'s as long as the button remains
 * pressed.
 *
 * @author Bart Cremers
 * @version 18-dec-2005
 */
public class JPressButton extends JajukButton {

    private static final long DEFAULT_INTERVAL = 250;

    /**
     * The interval between successive fireActionPerformed() calls.
     */
    private long actionInterval;

    /**
     * Re-use the L&F component of a normal <code>JButton</code>.
     *
     * @see #getUIClassID
     * @see #readObject
     */
    private static final String uiClassID = "ButtonUI";

    /**
     * Creates a button with no set text or icon and a default action interval.
     */
    public JPressButton() {
        this(null, null, DEFAULT_INTERVAL);
    }

    /**
     * Creates a button with no set text or icon.
     *
     * @param actionInterval the interval between 2 successive actionperformed calls.
     */
    public JPressButton(long actionInterval) {
        this(null, null, actionInterval);
    }

    /**
     * Creates a button with an icon and a default action interval.
     *
     * @param icon the Icon image to display on the button
     */
    public JPressButton(Icon icon) {
        this(null, icon, DEFAULT_INTERVAL);
    }

    /**
     * Creates a button with an icon.
     *
     * @param icon the Icon image to display on the button
     */
    public JPressButton(Icon icon, long actionInterval) {
        this(null, icon, actionInterval);
    }

    /**
     * Creates a button with text and a default action interval.
     *
     * @param text the text of the button
     */
    public JPressButton(String text) {
        this(text, null, DEFAULT_INTERVAL);
    }

    /**
     * Creates a button with text.
     *
     * @param text the text of the button
     */
    public JPressButton(String text, long actionInterval) {
        this(text, null, actionInterval);
    }

    /**
     * Creates a button where properties are taken from the <code>Action</code> supplied. The default
     * action interval is used.
     *
     * @param a the <code>Action</code> used to specify the new button
     */
    public JPressButton(Action a) {
        this(a, DEFAULT_INTERVAL);
        setAction(a);
    }

    /**
     * Creates a button where properties are taken from the <code>Action</code> supplied.
     *
     * @param a the <code>Action</code> used to specify the new button
     */
    public JPressButton(Action a, long actionInterval) {
        this();
        this.actionInterval = actionInterval;
        setAction(a);
    }

    /**
     * Creates a button with initial text and an icon and a default action interval.
     *
     * @param text the text of the button
     * @param icon the Icon image to display on the button
     */
    public JPressButton(String text, Icon icon) {
        this(text, icon, DEFAULT_INTERVAL);
    }

    /**
     * Creates a button with initial text and an icon.
     *
     * @param text the text of the button
     * @param icon the Icon image to display on the button
     */
    public JPressButton(String text, Icon icon, long actionInterval) {
        this.actionInterval = actionInterval;

        // Create the model
        setModel(new PressButtonModel(this));

        // initialize
        init(text, icon);
    }

    /**
     * @return the used interval between two successive calls to actionPerformed.
     */
    public long getActionInterval() {
        return actionInterval;
    }

    /**
     * @param actionInterval sets the interval between two successive calls to actionPerformed.
     */
    public void setActionInterval(long actionInterval) {
        this.actionInterval = actionInterval;
    }

    /**
     * Resets the UI property to a value from the current look and feel.
     *
     * @see javax.swing.JComponent#updateUI
     */
    public void updateUI() {
        setUI((ButtonUI) UIManager.getUI(this));
    }

    /**
     * Returns a string that specifies the name of the L&F class that renders this component.
     *
     * @return the string "ButtonUI"
     * @see javax.swing.JComponent#getUIClassID
     * @see javax.swing.UIDefaults#getUI
     */
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Button model for the <code>PressButton</code>. The model launches a thread when the
     * button remains pressed. The ends whenever the button releases.
     * @see ActionThread
     */
    public static class PressButtonModel extends DefaultButtonModel {

        private ActionThread thread;
        private JPressButton button;

        public PressButtonModel(JPressButton button) {
            this.button = button;
        }

        @Override
        public void setPressed(boolean b) {
            if ((isPressed() == b) || !isEnabled()) {
                return;
            }

            if (b) {
                stateMask |= PRESSED;
            } else {
                stateMask &= ~PRESSED;
            }

            if (isArmed()) {
                int modifiers = 0;
                AWTEvent currentEvent = EventQueue.getCurrentEvent();
                if (currentEvent instanceof InputEvent) {
                    modifiers = ((InputEvent) currentEvent).getModifiers();
                } else if (currentEvent instanceof ActionEvent) {
                    modifiers = ((ActionEvent) currentEvent).getModifiers();
                }

                if (isPressed() && thread == null) {
                    thread = button.new ActionThread(new ActionEvent(button,
                                                                     ActionEvent.ACTION_PERFORMED,
                                                                     getActionCommand(),
                                                                     EventQueue.getMostRecentEventTime(),
                                                                     modifiers));
                    thread.start();
                } else if (!isPressed() && thread != null) {
                    thread.setActive(false);
                    thread.interrupt();
                    thread = null;
                }
            }
            fireStateChanged();
        }
    }

    /**
     * Thread extension. While alive, fires an <code>actionPerformed</code> event at a certain
     * interval.
     */
    private class ActionThread extends Thread {

        private ActionEvent evt;
        private boolean active = true;
        private long interval;

        public ActionThread(ActionEvent evt) {
            this(evt, DEFAULT_INTERVAL);
        }

        public ActionThread(ActionEvent evt, long interval) {
            this.interval = interval;
            this.evt = evt;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public void run() {
            while (active) {
                try {
                    fireActionPerformed(evt);
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }
   }
}