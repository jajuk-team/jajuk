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
 *  $Revision$
 */
package org.jajuk.ui.actions;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Utility class for swing actions.
 */
public final class ActionUtil {

  /** The character to use as mnemonic indicator. */
  public static final char INDICATOR = '_';

  /**
   * Instantiates a new action util.
   */
  private ActionUtil() {
    // Private access to disallow construction.
  }

  /**
   * Strips a mnemonic character out of a given text. A text's mnemonic is the
   * first character following a <code>'_'</code> character.
   * 
   * @param text The text to strip the mnemonic character from.
   * 
   * @return An <code>int</code> defining the mnemonic character for the given
   * text. If there was no mnemonic indicator found, <code>-1</code>
   * will be returned.
   */
  public static int getMnemonic(String text) {
    for (int i = 0; i < text.length() - 1; i++) {
      if (text.charAt(i) == INDICATOR) {
        return text.charAt(i + 1);
      }
    }
    return -1;
  }

  /**
   * Strips the text from mnemonic indicators.
   * 
   * @param text The text to work on.
   * 
   * @return The text with all mnemonic indicators stripped. If there are no
   * indicators in the given text, the original text will be returned.
   * 
   * @see #INDICATOR
   */
  public static String strip(String text) {
    return text.replace(String.valueOf(INDICATOR), "");
  }

  /**
   * Install the keystrokes for several actions on a single button. The
   * keystrokes are added with {@link JComponent#WHEN_IN_FOCUSED_WINDOW}
   * condition.
   * 
   * @param component The component to which the key stroke will be added.
   * @param actions The actions to add to the keystrokes.
   */
  public static void installKeystrokes(JComponent component, Action... actions) {
    for (Action action : actions) {
      KeyStroke stroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
      if (stroke != null) {
        InputMap keyMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        keyMap.put(stroke, action);

        ActionMap actionMap = component.getActionMap();
        actionMap.put(action, action);
      }
    }
  }

  /**
   * Return whether a key event matches the mnemonic of a provided action.
   * 
   * @param action DOCUMENT_ME
   * @param ke DOCUMENT_ME
   * 
   * @return whether a key event matches the mnemonic of a provided action
   */
  public static boolean matches(Action action, KeyEvent ke) {
    KeyStroke key = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
    return KeyStroke.getKeyStrokeForEvent(ke).equals(key);
  }
}
