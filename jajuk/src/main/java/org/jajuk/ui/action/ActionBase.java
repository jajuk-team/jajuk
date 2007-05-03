/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */
package org.jajuk.ui.action;

import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;

/**
 * Common super class for Swing actions. This class provides useful construction
 * options to create actions, just leaving open the necessity of implementing
 * the {@link #actionPerformed(java.awt.event.ActionEvent)} method.
 */
public abstract class ActionBase extends AbstractAction implements ITechnicalStrings,
		HotkeyListener {

	/**
	 * Shared mutex for locking.
	 */
	protected static final byte[] MUTEX = new byte[0];

	/** Maps hotkeylisteners with the event ID */
	private static HashMap<Integer, ActionBase> hmIndexAction = new HashMap<Integer, ActionBase>(20);

	/**
	 * Is this action an hotkey ?
	 */
	private boolean bHotkey = false;

	/**
	 * Jintellitype object used for hotkeys and intellitype events management
	 * under winodws only
	 */
	private static JIntellitype jintellitype;

	// Instantiate a static jintellitype object
	static {
		if (Util.isUnderWindows()) {
			jintellitype = JIntellitype.getInstance();
			// assign this class to be a IntellitypeListener
			jintellitype.addIntellitypeListener(new IntellitypeListener() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.melloware.jintellitype.IntellitypeListener#onIntellitype(int)
				 */
				public void onIntellitype(int aCommand) {
					try {
						// Perform right action according to intellitype command
						switch (aCommand) {
						case JIntellitype.APPCOMMAND_MEDIA_NEXTTRACK:
							ActionManager.getAction(JajukAction.NEXT_TRACK).perform(null);
							break;
						case JIntellitype.APPCOMMAND_MEDIA_PLAY_PAUSE:
							ActionManager.getAction(JajukAction.PLAY_PAUSE_TRACK).perform(null);
							break;
						case JIntellitype.APPCOMMAND_MEDIA_PREVIOUSTRACK:
							ActionManager.getAction(JajukAction.PREVIOUS_TRACK).perform(null);
							break;
						case JIntellitype.APPCOMMAND_MEDIA_STOP:
							ActionManager.getAction(JajukAction.STOP_TRACK).perform(null);
							break;
						case JIntellitype.APPCOMMAND_VOLUME_DOWN:
							ActionManager.getAction(JajukAction.DECREASE_VOLUME).perform(null);
							break;
						case JIntellitype.APPCOMMAND_VOLUME_UP:
							ActionManager.getAction(JajukAction.INCREASE_VOLUME).perform(null);
							break;
						case JIntellitype.APPCOMMAND_VOLUME_MUTE:
							ActionManager.getAction(JajukAction.MUTE_STATE).perform(null);
							break;
						default:
							Log.debug("Undefined INTELLITYPE message caught "
									+ Integer.toString(aCommand));
							break;
						}
					} catch (Throwable e2) {
						Log.error(e2);
					} finally {
						ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
					}
				}

			});

		}
	}

	/**
	 * Construct an action with the given name, icon and accelerator keystroke.
	 * 
	 * @param name
	 *            The unique name for the action. This name is used in the
	 *            labels for visualization. If the name contains a '_'
	 *            (underscore) character. The character following this
	 *            underscore is used as mnemonic key for the action.
	 * @param icon
	 *            The icon to use for visualization of the action.
	 * @param stroke
	 *            The keystroke to use.
	 * @param enabled
	 *            By default enable or disable the action.
	 * @param hotkey
	 *            Is this command should be enabled even when jajuk has not the
	 *            focus (has a effect under windows only)
	 */
	protected ActionBase(String pName, Icon icon, KeyStroke stroke, boolean enabled, boolean bHotkey) {
		// check hotkeys are enabled (false by default)
		this.bHotkey = bHotkey && ConfigurationManager.getBoolean(CONF_OPTIONS_HOTKEYS);
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
			if (Util.isUnderWindows() && this.bHotkey) {
				// under windows, use hotkey that can be used even when window
				// has not the focus. Note that all keys are nor hotkeys (given
				// by bHotkey flag)
				int index = hmIndexAction.size() - 1;
				jintellitype.registerSwingHotKey(index + 1, stroke.getModifiers(), stroke
						.getKeyCode());
				// register the action with its index
				hmIndexAction.put(index + 1, this);
				// add the listener
				jintellitype.addHotKeyListener(this);
			} else {
				// else use standard swing keystroke feature
				setAcceleratorKey(stroke);
			}
		}
		setEnabled(enabled);
	}

	
	/**
	 * Construct an action with the given name, icon and accelerator keystroke.
	 * 
	 * @param name
	 *            The unique name for the action. This name is used in the
	 *            labels for visualization. If the name contains a '_'
	 *            (underscore) character. The character following this
	 *            underscore is used as mnemonic key for the action.
	 * @param icon
	 *            The icon to use for visualization of the action.
	 * @param stroke
	 *            The keystroke to use. If the keystroke given is not a valid
	 *            keystroke using the rules describe in
	 *            {@link javax.swing.KeyStroke#getKeyStroke(String)}, null is
	 *            used instead.
	 * @param enabled
	 *            By default enable or disable the action.
	 * @param hotkey
	 *            Is this command should be enabled even when jajuk has not the
	 *            focus (has a effect under windows only)
	 */
	protected ActionBase(String name, Icon icon, String stroke, boolean enabled, boolean bHotkey) {
		this(name, icon, KeyStroke.getKeyStroke(stroke), enabled, bHotkey);
	}

	/**
	 * Construct an action with the given name and accelerator keystroke, no
	 * icon.
	 * 
	 * @param name
	 *            The unique name for the action. This name is used in the
	 *            labels for visualization. If the name contains a '_'
	 *            (underscore) character. The character following this
	 *            underscore is used as mnemonic key for the action.
	 * @param stroke
	 *            The keystroke to use.
	 * @param enabled
	 *            By default enable or disable the action.
	 * @param hotkey
	 *            Is this command should be enabled even when jajuk has not the
	 *            focus (has a effect under windows only)
	 */
	protected ActionBase(String name, KeyStroke stroke, boolean enabled, boolean bHotkey) {
		this(name, null, stroke, enabled, bHotkey);
	}

	/**
	 * Construct an action with the given name and accelerator keystroke, no
	 * icon.
	 * 
	 * @param name
	 *            The unique name for the action. This name is used in the
	 *            labels for visualization. If the name contains a '_'
	 *            (underscore) character. The character following this
	 *            underscore is used as mnemonic key for the action.
	 * @param stroke
	 *            The keystroke to use. If the keystroke given is not a valid
	 *            keystroke using the rules describe in
	 *            {@link javax.swing.KeyStroke#getKeyStroke(String)}, null is
	 *            used instead.
	 * @param enabled
	 *            By default enable or disable the action.
	 * @param hotkey
	 *            Is this command should be enabled even when jajuk has not the
	 *            focus (has a effect under windows only)
	 */
	protected ActionBase(String name, String stroke, boolean enabled, boolean bHotkey) {
		this(name, null, stroke, enabled, bHotkey);
	}

	/**
	 * Construct an action with the given icon and accelerator keystroke, no
	 * name.
	 * 
	 * @param icon
	 *            The icon to use for visualization of the action.
	 * @param stroke
	 *            The keystroke to use.
	 * @param enabled
	 *            By default enable or disable the action.
	 * @param hotkey
	 *            Is this command should be enabled even when jajuk has not the
	 *            focus (has a effect under windows only)
	 * @see javax.swing.KeyStroke#getKeyStroke(String)
	 */
	protected ActionBase(Icon icon, KeyStroke stroke, boolean enabled, boolean bHotkey) {
		this(null, icon, stroke, enabled, bHotkey);
	}

	/**
	 * Construct an action with the given icon and accelerator keystroke, no
	 * name.
	 * 
	 * @param icon
	 *            The icon to use for visualization of the action.
	 * @param stroke
	 *            The keystroke to use. If the keystroke given is not a valid
	 *            keystroke using the rules describe in
	 *            {@link javax.swing.KeyStroke#getKeyStroke(String)}, null is
	 *            used instead.
	 * @param enabled
	 *            By default enable or disable the action.
	 * @param hotkey
	 *            Is this command should be enabled even when jajuk has not the
	 *            focus (has a effect under windows only)
	 * @see javax.swing.KeyStroke#getKeyStroke(String)
	 */
	protected ActionBase(Icon icon, String stroke, boolean enabled, boolean bHotkey) {
		this(null, icon, stroke, enabled, bHotkey);
	}

	/**
	 * Construct an action with the given name and icon, no accelerator
	 * keystroke.
	 * 
	 * @param name
	 *            The unique name for the action. This name is used in the
	 *            labels for visualization. If the name contains a '_'
	 *            (underscore) character. The character following this
	 *            underscore is used as mnemonic key for the action.
	 * @param icon
	 *            The icon to use for visualization of the action.
	 * @param enabled
	 *            By default enable or disable the action.
	 */
	protected ActionBase(String name, Icon icon, boolean enabled) {
		this(name, icon, (KeyStroke) null, enabled, false);
	}

	/**
	 * Construct an action with the given icon, no name, no accelerator
	 * keystroke.
	 * 
	 * @param icon
	 *            The icon to use for visualization of the action.
	 * @param enabled
	 *            By default enable or disable the action.
	 */
	protected ActionBase(Icon icon, boolean enabled) {
		this(null, icon, (KeyStroke) null, enabled, false);
	}

	/**
	 * Construct an action with the given name, no icon, no accelerator
	 * keystroke.
	 * 
	 * @param name
	 *            The unique name for the action. This name is used in the
	 *            labels for visualization. If the name contains a '_'
	 *            (underscore) character. The character following this
	 *            underscore is used as mnemonic key for the action.
	 * @param enabled
	 *            By default enable or disable the action.
	 */
	protected ActionBase(String name, boolean enabled) {
		this(name, null, (KeyStroke) null, enabled, false);
	}

	/**
	 * @param name
	 *            The name for the action. This name is used for a menu or a
	 *            button.
	 */
	public void setName(String name) {
		putValue(NAME, name);
	}

	/**
	 * @param description
	 *            The short description for the action. This is used for tooltip
	 *            text.
	 */
	public void setShortDescription(String description) {
		putValue(SHORT_DESCRIPTION, description);
	}

	/**
	 * @param description
	 *            The long description for the action. This can be used for
	 *            context-sensitive help.
	 */
	public void setLongDescription(String description) {
		putValue(LONG_DESCRIPTION, description);
	}

	/**
	 * @param icon
	 *            The small icon for the action. Use for toolbar buttons.
	 */
	public void setIcon(Icon icon) {
		putValue(SMALL_ICON, icon);
	}

	/**
	 * @param actionCommand
	 *            The action command for this action. This is used for creating
	 *            the <code>ActionEvent</code>.
	 */

	public void setActionCommand(String actionCommand) {
		putValue(ACCELERATOR_KEY, actionCommand);
	}

	/**
	 * @param stroke
	 *            The keystroke for the action. This is used as a shortcut key.
	 */
	public void setAcceleratorKey(KeyStroke stroke) {
		putValue(ACCELERATOR_KEY, stroke);
	}

	/**
	 * @param stroke
	 *            The keystroke for the action. If the keystroke given is not a
	 *            valid keystroke using the rules described in
	 *            {@link javax.swing.KeyStroke#getKeyStroke(String)},
	 *            <code>null</code> is used instead. This is used as a
	 *            shortcut key.
	 */
	public void setAcceleratorKey(String stroke) {
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(stroke));
	}

	/**
	 * Sets the keyboard mnemonic on the current action. <p/> A mnemonic must
	 * correspond to a single key on the keyboard and should be specified using
	 * one of the <code>VK_XXX</code> keycodes defined in
	 * <code>java.awt.event.KeyEvent</code>. Mnemonics are case-insensitive,
	 * therefore a key event with the corresponding keycode would cause the
	 * button to be activated whether or not the Shift modifier was pressed.
	 * <p/> If the character defined by the mnemonic is found within the
	 * button's label string, the first occurrence of it will be underlined to
	 * indicate the mnemonic to the user.
	 * 
	 * @param mnemonic
	 *            The key code which represents the mnemonic. The mnemonic is
	 *            the key which when combined with the look and feel's mouseless
	 *            modifier (usually <b>Alt</b>) will activate this button if
	 *            focus is contained somewhere within this action's ancestor
	 *            window.
	 * @see java.awt.event.KeyEvent
	 */
	public void setMnemonic(int mnemonic) {
		putValue(MNEMONIC_KEY, mnemonic);
	}

	/**
	 * Invoked when an action occurs. This implementation calls
	 * {@link #perform(java.awt.event.ActionEvent)} to add error handling and
	 * logging to the action system.
	 * 
	 * @param evt
	 *            The event.
	 */
	public final void actionPerformed(ActionEvent evt) {
		try {
			perform(evt);
		} catch (Throwable e2) {
			Log.error(e2);
		} finally {
			ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
		}
	}

	/**
	 * Perform the action.
	 * 
	 * @throws Exception
	 *             When anything goes wrong when performing the action.
	 * @param evt
	 */
	protected abstract void perform(ActionEvent evt) throws Exception;

	// listen for hotkey
	public void onHotKey(int aIdentifier) {
		// Leave if user disabled hotkeys
		if (!ConfigurationManager.getBoolean(CONF_OPTIONS_HOTKEYS)) {
			return;
		}
		// check it is the right listener that caught the event
		if (this.equals(hmIndexAction.get(aIdentifier))) {
			try {
				// Call action itself
				perform(null);
			} catch (Throwable e2) {
				Log.error(e2);
			} finally {
				ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
			}
		}
	}

	/**
	 * Free intellipad ressources
	 */
	public static void cleanup() {
		jintellitype.cleanUp();
	}

	/**
	 * 
	 * @return whether it is an hotkey
	 */
	public boolean isHotkey() {
		return this.bHotkey;
	}

}
