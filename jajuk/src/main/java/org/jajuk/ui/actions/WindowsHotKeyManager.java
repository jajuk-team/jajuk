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
 *  $$Revision: 2359 $$
 */
package org.jajuk.ui.actions;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;

import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

/**
 * This manager contains all windows-specific code dealing with hotkeys
 * <p>
 * Delete this class before compiling if you want to build jajuk without
 * Jintellitype jar
 * </p>
 */
public abstract class WindowsHotKeyManager extends AbstractAction implements ITechnicalStrings {

  /** Maps hotkeylisteners with the event ID */
  private static HashMap<Integer, ActionBase> hmIndexAction = new HashMap<Integer, ActionBase>(20);

  /**
   * Jintellitype object used for hotkeys and intellitype events management
   * under windows only
   */
  private static JIntellitype jintellitype;

  public static void registerJIntellitype() {
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
            Log.debug("Undefined INTELLITYPE message caught " + Integer.toString(aCommand));
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

  public static void registerHotKey(KeyStroke stroke, final ActionBase ab) {
    // under windows, use hotkey that can be used even when window
    // has not the focus. Note that all keys are nor hotkeys (given
    // by bHotkey flag)
    int index = hmIndexAction.size() - 1;
    jintellitype.registerSwingHotKey(index + 1, stroke.getModifiers(), stroke.getKeyCode());
    // register the action with its index
    hmIndexAction.put(index + 1, ab);
    // add the listener
    jintellitype.addHotKeyListener(new HotkeyListener() {

      public void onHotKey(int key) {
        // Leave if user disabled hotkeys
        if (!ConfigurationManager.getBoolean(CONF_OPTIONS_HOTKEYS)) {
          return;
        }
        // Check it is the right listener that caught the event
        if (ab.equals(hmIndexAction.get(key))) {
          try {
            // Call action itself
            ab.perform(null);
          } catch (Throwable e2) {
            Log.error(e2);
          } finally {
            ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
          }
        }
      }

    });
  }

  /**
   * Free Jintellipad ressources
   */
  public static void cleanup() {
    jintellitype.cleanUp();
  }
}
