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

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;
import com.melloware.jintellitype.JIntellitypeConstants;

import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * This manager contains all windows-specific code dealing with hotkeys
 * <p>
 * Delete this class before compiling if you want to build jajuk without
 * Jintellitype jar
 * </p>.
 */
public abstract class WindowsHotKeyManager extends AbstractAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -6948447651091264530L;

  /** Maps hotkeylisteners with the event ID. */
  private static Map<Integer, JajukAction> hmIndexAction = new HashMap<Integer, JajukAction>(20);

  /** Jintellitype object used for hotkeys and intellitype events management under windows only. */
  private static JIntellitype jintellitype;

  /**
   * Register j intellitype.
   * DOCUMENT_ME
   */
  public static void registerJIntellitype() {
    jintellitype = JIntellitype.getInstance();
    // assign this class to be a IntellitypeListener
    jintellitype.addIntellitypeListener(new IntellitypeListener() {

      /*
       * (non-Javadoc)
       * 
       * @see com.melloware.jintellitype.IntellitypeListener#onIntellitype(int)
       */
      @Override
      public void onIntellitype(int aCommand) {
        try {
          // Perform right action according to intellitype command
          switch (aCommand) {
          case JIntellitypeConstants.APPCOMMAND_MEDIA_NEXTTRACK:
            ActionManager.getAction(JajukActions.NEXT_TRACK).perform(null);
            break;
          case JIntellitypeConstants.APPCOMMAND_MEDIA_PLAY_PAUSE:
            ActionManager.getAction(JajukActions.PAUSE_RESUME_TRACK).perform(null);
            break;
          case JIntellitypeConstants.APPCOMMAND_MEDIA_PREVIOUSTRACK:
            ActionManager.getAction(JajukActions.PREVIOUS_TRACK).perform(null);
            break;
          case JIntellitypeConstants.APPCOMMAND_MEDIA_STOP:
            ActionManager.getAction(JajukActions.STOP_TRACK).perform(null);
            break;
          case JIntellitypeConstants.APPCOMMAND_VOLUME_DOWN:
            ActionManager.getAction(JajukActions.DECREASE_VOLUME).perform(null);
            break;
          case JIntellitypeConstants.APPCOMMAND_VOLUME_UP:
            ActionManager.getAction(JajukActions.INCREASE_VOLUME).perform(null);
            break;
          case JIntellitypeConstants.APPCOMMAND_VOLUME_MUTE:
            Log.debug("System mute");
            // Ignore this to fix issue #1042, mute concurrency between the OS
            // key trapper and JIntellitype
            break;
          default:
            Log.debug("Undefined INTELLITYPE message caught " + Integer.toString(aCommand));
            break;
          }
        } catch (Throwable e2) {
          Log.error(e2);
        }
      }
    });

  }

  /**
   * Register hot key.
   * DOCUMENT_ME
   * 
   * @param stroke DOCUMENT_ME
   * @param ab DOCUMENT_ME
   */
  public static void registerHotKey(KeyStroke stroke, final JajukAction ab) {
    // under windows, use hotkey that can be used even when window
    // has not the focus. Note that all keys are nor hotkeys (given
    // by bHotkey flag)
    int index = hmIndexAction.size() - 1;
    jintellitype.registerSwingHotKey(index + 1, stroke.getModifiers(), stroke.getKeyCode());
    // register the action with its index
    hmIndexAction.put(index + 1, ab);
    // add the listener
    jintellitype.addHotKeyListener(new HotkeyListener() {

      @Override
      public void onHotKey(int key) {
        // Leave if user disabled hotkeys
        if (!Conf.getBoolean(Const.CONF_OPTIONS_HOTKEYS)) {
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
            ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
          }
        }
      }

    });
  }

  /**
   * Free Jintellipad resources.
   */
  public static void cleanup() {
    if (jintellitype != null) {
      jintellitype.cleanUp();
    }
  }
}
