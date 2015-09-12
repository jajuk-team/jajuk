/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
 *  http://jajuk.info
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
 *  
 */
package org.jajuk.ui.widgets;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;

/**
 * A specialized button, firing successive <code>ActionEvent</code>'s as long
 * as the button remains pressed.
 */
public class JPressButton extends JajukButton {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** The Constant DEFAULT_INTERVAL.   */
  private static final long DEFAULT_INTERVAL = 250;
  /** The interval between successive fireActionPerformed() calls. */
  private long actionInterval;
  /** Re-use the L&F component of a normal <code>JButton</code>. */
  private static final String UI_CLASS_ID = "ButtonUI";

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
   * @param actionInterval 
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
   * @param actionInterval 
   */
  public JPressButton(String text, long actionInterval) {
    this(text, null, actionInterval);
  }

  /**
   * Creates a button where properties are taken from the <code>Action</code>
   * supplied. The default action interval is used.
   * 
   * @param a the <code>Action</code> used to specify the new button
   */
  public JPressButton(Action a) {
    this(a, DEFAULT_INTERVAL);
    setAction(a);
  }

  /**
   * Creates a button where properties are taken from the <code>Action</code>
   * supplied.
   * 
   * @param a the <code>Action</code> used to specify the new button
   * @param actionInterval 
   */
  public JPressButton(Action a, long actionInterval) {
    this();
    this.actionInterval = actionInterval;
    setAction(a);
  }

  /**
   * Creates a button with initial text and an icon and a default action
   * interval.
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
   * @param actionInterval 
   */
  public JPressButton(String text, Icon icon, long actionInterval) {
    this.actionInterval = actionInterval;
    // Create the model
    setModel(new PressButtonModel(this));
    // initialize
    init(text, icon);
    // Set border
    setRolloverEnabled(true);
  }

  /**
   * Gets the action interval.
   * 
   * @return the used interval between two successive calls to actionPerformed.
   */
  public long getActionInterval() {
    return actionInterval;
  }

  /**
   * Sets the action interval.
   * 
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
  @Override
  public void updateUI() {
    setUI((ButtonUI) UIManager.getUI(this));
  }

  /**
   * Returns a string that specifies the name of the L&F class that renders this
   * component.
   * 
   * @return the string "ButtonUI"
   * 
   * @see javax.swing.JComponent#getUIClassID
   * @see javax.swing.UIDefaults#getUI
   */
  @Override
  public String getUIClassID() {
    return UI_CLASS_ID;
  }

  /**
   * Button model for the <code>PressButton</code>. The model launches a
   * thread when the button remains pressed. The ends whenever the button
   * releases.
   * 
   * @see ActionThread
   */
  public static class PressButtonModel extends DefaultButtonModel {
    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;
    private ActionThread thread;
    private final JPressButton button;

    /**
     * Instantiates a new press button model.
     * 
     * @param button 
     */
    public PressButtonModel(JPressButton button) {
      this.button = button;
    }

    /* (non-Javadoc)
     * @see javax.swing.DefaultButtonModel#setPressed(boolean)
     */
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
          thread = button.new ActionThread(new ActionEvent(button, ActionEvent.ACTION_PERFORMED,
              getActionCommand(), EventQueue.getMostRecentEventTime(), modifiers));
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
   * Thread extension. While alive, fires an <code>actionPerformed</code>
   * event at a certain interval.
   */
  private class ActionThread extends Thread {
    private final ActionEvent evt;
    private boolean active = true;
    private final long interval;

    /**
     * Instantiates a new action thread.
     * 
     * @param evt 
     */
    public ActionThread(ActionEvent evt) {
      this(evt, DEFAULT_INTERVAL);
    }

    /**
     * Instantiates a new action thread.
     * 
     * @param evt 
     * @param interval 
     */
    public ActionThread(ActionEvent evt, long interval) {
      super("JPressButton Action Thread");
      this.interval = interval;
      this.evt = evt;
    }

    /**
     * Sets the active.
     * 
     * @param active the new active
     */
    public void setActive(boolean active) {
      this.active = active;
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
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