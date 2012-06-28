/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Common super class for Swing actions. This class provides useful construction
 * options to create actions, just leaving open the necessity of implementing
 * the {@link #actionPerformed(java.awt.event.ActionEvent)} method.
 */
public abstract class JajukAction extends AbstractAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -2535230785022978297L;
  /** Is this action an hotkey ?. */
  private boolean bHotkey = false;
  /** enable state. */
  private boolean bEnable = true;
  // Instantiate a static JIntellitype object if under Windows
  // BEWARE ! don't use direct call to JIntellitype like
  // JIntellitype.isJIntellitypeSupported()) because we don't want to create a linkage
  // dependency for non Windows OS. The JIntellitype jar is not available under Debian for ie
  static {
    if (UtilSystem.isUnderWindows()) {
      try {
        Class.forName("org.jajuk.ui.actions.WindowsHotKeyManager")
            .getMethod("registerJIntellitype").invoke(null, (Object[]) null);
      } catch (Exception e) {
        Log.error(e);
      }
    }
  }

  /**
   * Construct an action with the given name, icon and accelerator keystroke.
   *
   * @param pName 
   * @param icon The icon to use for visualization of the action.
   * @param stroke The keystroke to use.
   * @param enabled By default enable or disable the action.
   * @param bHotkey 
   */
  protected JajukAction(String pName, Icon icon, KeyStroke stroke, boolean enabled, boolean bHotkey) {
    // check hotkeys are enabled (false by default)
    this.bHotkey = UtilSystem.isUnderWindows() && bHotkey
        && Conf.getBoolean(Const.CONF_OPTIONS_HOTKEYS);
    String name = pName;
    if (name != null) {
      int mnemonic = ActionUtil.getMnemonic(name);
      name = ActionUtil.strip(name);
      if (mnemonic >= 0) {
        setMnemonic(mnemonic);
      }
      setName(name);
    }
    if (icon != null) {
      setIcon(icon);
    }
    if (stroke != null) {
      if (this.bHotkey) {
        try {
          Class.forName("org.jajuk.ui.actions.WindowsHotKeyManager")
              .getMethod("registerHotKey", new Class[] { KeyStroke.class, JajukAction.class })
              .invoke(null, new Object[] { stroke, this });
        } catch (ClassNotFoundException e) {
          Log.error(e);
        } catch (IllegalArgumentException e) {
          Log.error(e);
        } catch (SecurityException e) {
          Log.error(e);
        } catch (IllegalAccessException e) {
          Log.error(e);
        } catch (InvocationTargetException e) {
          Log.error(e);
        } catch (NoSuchMethodException e) {
          Log.error(e);
        }
      }
      // else use standard swing keystroke feature
      setAcceleratorKey(stroke);
    }
    setEnabled(enabled);
  }

  /**
   * Construct an action with the given name, icon and accelerator keystroke.
   *
   * @param name The unique name for the action. This name is used in the labels
   * for visualization. If the name contains a '_' (underscore)
   * character. The character following this underscore is used as
   * mnemonic key for the action.
   * @param icon The icon to use for visualization of the action.
   * @param stroke The keystroke to use. If the keystroke given is not a valid
   * keystroke using the rules describe in
   * @param enabled By default enable or disable the action.
   * @param bHotkey is it a hotkey (available even when window has not the focus) ?
   * {@link javax.swing.KeyStroke#getKeyStroke(String)}, null is used
   * instead.
   */
  protected JajukAction(String name, Icon icon, String stroke, boolean enabled, boolean bHotkey) {
    this(name, icon, KeyStroke.getKeyStroke(stroke), enabled, bHotkey);
  }

  /**
   * Construct an action with the given name and accelerator keystroke, no icon.
   * 
   * @param name The unique name for the action. This name is used in the labels
   * for visualization. If the name contains a '_' (underscore)
   * character. The character following this underscore is used as
   * mnemonic key for the action.
   * @param stroke The keystroke to use.
   * @param enabled By default enable or disable the action.
   * @param bHotkey is it a hotkey (available even when window has not the focus) ?
   */
  protected JajukAction(String name, KeyStroke stroke, boolean enabled, boolean bHotkey) {
    this(name, null, stroke, enabled, bHotkey);
  }

  /**
   * Construct an action with the given name and accelerator keystroke, no icon.
   *
   * @param name The unique name for the action. This name is used in the labels
   * for visualization. If the name contains a '_' (underscore)
   * character. The character following this underscore is used as
   * mnemonic key for the action.
   * @param stroke The keystroke to use. If the keystroke given is not a valid
   * keystroke using the rules describe in
   * @param enabled By default enable or disable the action.
   * @param bHotkey is it a hotkey (available even when window has not the focus) ?
   * {@link javax.swing.KeyStroke#getKeyStroke(String)}, null is used
   * instead.
   */
  protected JajukAction(String name, String stroke, boolean enabled, boolean bHotkey) {
    this(name, null, stroke, enabled, bHotkey);
  }

  /**
   * Construct an action with the given icon and accelerator keystroke, no name.
   * 
   * @param icon The icon to use for visualization of the action.
   * @param stroke The keystroke to use.
   * @param enabled By default enable or disable the action.
   * @param bHotkey is it a hotkey (available even when window has not the focus) ?
   * 
   * @see javax.swing.KeyStroke#getKeyStroke(String)
   */
  protected JajukAction(Icon icon, KeyStroke stroke, boolean enabled, boolean bHotkey) {
    this(null, icon, stroke, enabled, bHotkey);
  }

  /**
   * Construct an action with the given icon and accelerator keystroke, no name.
   *
   * @param icon The icon to use for visualization of the action.
   * @param stroke The keystroke to use. If the keystroke given is not a valid
   * keystroke using the rules describe in
   * @param enabled By default enable or disable the action.
   * @param bHotkey is it a hotkey (available even when window has not the focus) ?
   * {@link javax.swing.KeyStroke#getKeyStroke(String)}, null is used
   * instead.
   * @see javax.swing.KeyStroke#getKeyStroke(String)
   */
  protected JajukAction(Icon icon, String stroke, boolean enabled, boolean bHotkey) {
    this(null, icon, stroke, enabled, bHotkey);
  }

  /**
   * Construct an action with the given name and icon, no accelerator keystroke.
   * 
   * @param name The unique name for the action. This name is used in the labels
   * for visualization. If the name contains a '_' (underscore)
   * character. The character following this underscore is used as
   * mnemonic key for the action.
   * @param icon The icon to use for visualization of the action.
   * @param enabled By default enable or disable the action.
   */
  protected JajukAction(String name, Icon icon, boolean enabled) {
    this(name, icon, (KeyStroke) null, enabled, false);
  }

  /**
   * Construct an action with the given icon, no name, no accelerator keystroke.
   * 
   * @param icon The icon to use for visualization of the action.
   * @param enabled By default enable or disable the action.
   */
  protected JajukAction(Icon icon, boolean enabled) {
    this(null, icon, (KeyStroke) null, enabled, false);
  }

  /**
   * Construct an action with the given name, no icon, no accelerator keystroke.
   * 
   * @param name The unique name for the action. This name is used in the labels
   * for visualization. If the name contains a '_' (underscore)
   * character. The character following this underscore is used as
   * mnemonic key for the action.
   * @param enabled By default enable or disable the action.
   */
  protected JajukAction(String name, boolean enabled) {
    this(name, null, (KeyStroke) null, enabled, false);
  }

  /**
   * Sets the name.
   * 
   * @param name The name for the action. This name is used for a menu or a button.
   */
  public final void setName(String name) {
    putValue(NAME, name);
  }

  /**
   * Sets the short description.
   * 
   * @param description The short description for the action. This is used for tooltip
   * text.
   */
  public void setShortDescription(String description) {
    putValue(SHORT_DESCRIPTION, description);
  }

  /**
   * Sets the long description.
   * 
   * @param description The long description for the action. This can be used for
   * context-sensitive help.
   */
  public void setLongDescription(String description) {
    putValue(LONG_DESCRIPTION, description);
  }

  /**
   * Sets the icon.
   * 
   * @param icon The small icon for the action. Use for toolbar buttons.
   */
  public final void setIcon(Icon icon) {
    putValue(SMALL_ICON, icon);
  }

  /**
   * Sets the action command.
   * 
   * @param actionCommand The action command for this action. This is used for creating the
   * <code>ActionEvent</code>.
   */
  public void setActionCommand(String actionCommand) {
    putValue(ACCELERATOR_KEY, actionCommand);
  }

  /**
   * Sets the accelerator key.
   * 
   * @param stroke The keystroke for the action. This is used as a shortcut key.
   */
  public final void setAcceleratorKey(KeyStroke stroke) {
    putValue(ACCELERATOR_KEY, stroke);
  }

  /**
   * Sets the accelerator key.
   * 
   * @param stroke The keystroke for the action. If the keystroke given is not a
   * valid keystroke using the rules described in
   * {@link javax.swing.KeyStroke#getKeyStroke(String)},
   * <code>null</code> is used instead. This is used as a shortcut
   * key.
   */
  public final void setAcceleratorKey(String stroke) {
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(stroke));
  }

  /**
   * Sets the keyboard mnemonic on the current action. <p/> A mnemonic must
   * correspond to a single key on the keyboard and should be specified using
   * one of the <code>VK_XXX</code> keycodes defined in
   * <code>java.awt.event.KeyEvent</code>. Mnemonics are case-insensitive,
   * therefore a key event with the corresponding keycode would cause the button
   * to be activated whether or not the Shift modifier was pressed. <p/> If the
   * character defined by the mnemonic is found within the button's label
   * string, the first occurrence of it will be underlined to indicate the
   * mnemonic to the user.
   * 
   * @param mnemonic The key code which represents the mnemonic. The mnemonic is the
   * key which when combined with the look and feel's mouseless
   * modifier (usually <b>Alt</b>) will activate this button if focus
   * is contained somewhere within this action's ancestor window.
   * 
   * @see java.awt.event.KeyEvent
   */
  public final void setMnemonic(int mnemonic) {
    putValue(MNEMONIC_KEY, mnemonic);
  }

  /**
   * Invoked when an action occurs. This implementation calls
   *
   * @param evt The event.
   * {@link #perform(java.awt.event.ActionEvent)} to add error handling and
   * logging to the action system.
   */
  @Override
  public final void actionPerformed(ActionEvent evt) {
    try {
      perform(evt);
    } catch (Throwable e2) {//NOSONAR
      // We want to catch even throwables because there is no point to 
      // throw them again, caller can't do anything anyway.
      Log.error(e2);
    }
  }

  /**
   * Perform the action.
   * 
   * @param evt 
   * 
   * @throws Exception When anything goes wrong when performing the action.
   */
  public abstract void perform(ActionEvent evt) throws Exception;

  /**
   * Free intellipad resources.
   * 
   * @throws Exception the exception
   */
  public static void cleanup() throws Exception {
    if (UtilSystem.isUnderWindows()) {
      Class.forName("org.jajuk.ui.actions.WindowsHotKeyManager").getMethod("cleanup")
          .invoke(null, (Object[]) null);
    }
  }

  /**
   * Checks if is hotkey.
   * 
   * @return whether it is an hotkey
   */
  public boolean isHotkey() {
    return this.bHotkey;
  }

  /**
   * Enable or disable the action.
   * 
   * @param enable 
   */
  protected void enable(boolean enable) {
    this.bEnable = enable;
  }

  /**
   * Checks if is enable.
   * 
   * @return enable state for the action
   */
  protected boolean isEnable() {
    return this.bEnable;
  }
}
