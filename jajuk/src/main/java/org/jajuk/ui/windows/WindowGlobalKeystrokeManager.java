/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.windows;

import static org.jajuk.ui.actions.JajukActions.DECREASE_VOLUME;
import static org.jajuk.ui.actions.JajukActions.FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukActions.HELP_REQUIRED;
import static org.jajuk.ui.actions.JajukActions.INCREASE_VOLUME;
import static org.jajuk.ui.actions.JajukActions.MUTE_STATE;
import static org.jajuk.ui.actions.JajukActions.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.PAUSE_RESUME_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.REPEAT_MODE;
import static org.jajuk.ui.actions.JajukActions.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukActions.SHUFFLE_MODE;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.ActionUtil;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.util.log.Log;

/**
 * Keystrokes manager for any window type
 * <p>
 * Singleton
 * </p>
 */
public class WindowGlobalKeystrokeManager {

  private static WindowGlobalKeystrokeManager self;

  /** List of actions to enable globaly * */
  private JajukActions[] globalActions = new JajukActions[] { NEXT_ALBUM, PREVIOUS_ALBUM,
      PREVIOUS_TRACK, NEXT_TRACK, MUTE_STATE, PAUSE_RESUME_TRACK, STOP_TRACK, DECREASE_VOLUME,
      INCREASE_VOLUME, SHUFFLE_MODE, REPEAT_MODE, REWIND_TRACK, FORWARD_TRACK, HELP_REQUIRED };

  static WindowGlobalKeystrokeManager getInstance() {
    if (self == null) {
      self = new WindowGlobalKeystrokeManager();
    }
    return self;
  }

  public WindowGlobalKeystrokeManager() {
    KeyEventDispatcher ked = new KeyEventDispatcher() {

      public boolean dispatchKeyEvent(KeyEvent ke) {
        // Add all global keys to this dispatcher
        for (JajukActions actionName : globalActions) {
          JajukAction action = ActionManager.getAction(actionName);
          if (ke.getID() == KeyEvent.KEY_PRESSED && ActionUtil.matches(action, ke)) {
            try {
              action.perform(null);
            } catch (Exception e) {
              Log.error(e);
            }
          }
        }
        // No need to dispatch to components
        return false;
      }
    };

    // Attach the event dispatcher
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ked);
  }
}
