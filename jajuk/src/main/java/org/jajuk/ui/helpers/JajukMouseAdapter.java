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
package org.jajuk.ui.helpers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Specific implementation of a mouse adapter that handle properly the various
 * OS behaviors dealing with right/left click, isPopup ...
 * <p>
 * <li>We don't use mouseClicked() method as the event can be thrown or not
 * among different OS, specifications are not clear. It can be thrown in
 * addition with mousePressed and mouseReleased events, so we just don't use it
 * but mousePressed() and mouseReleased() methods instead.</li>
 * <li>Popup request detection cannot be simply handled by a right click
 * (Windows, Linux) as it can be thrown by others interactions like CTRL + click
 * under OS X with a classic one touch mouse, so we use the isPopupTrigger()
 * method to recognize such event.</li>
 * <li>We have to check mousePressed and mouseReleased() methods as the popup
 * request is recognized on the pressed event under most OS and on the release
 * event under Mac OS.</li>
 * </p>
 */
public class JajukMouseAdapter extends MouseAdapter {

  /** Whether a popup request gesture has been recognized. */
  private boolean popupTrigger = false;

  /* (non-Javadoc)
   * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(final MouseEvent e) {
    // Note that this method is not final as we sometimes need additional tests
    // or actions but overriding it is discouraged

    // reset the popup gesture state
    popupTrigger = false;
    // popupTrigger can be false here even if the user performed a right click
    // because the event returns true only when the button is released
    if (e.isPopupTrigger()) {
      popupTrigger = true;
    }
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(final MouseEvent e) {
    // Note that this method is not final as we sometimes need additional tests
    // or actions but overriding it is discouraged

    // Actual popup request is an 'or' between state during mousePressed and
    // mouseReleased methods to deal with every JRE implementation
    popupTrigger = popupTrigger || e.isPopupTrigger();
    if (popupTrigger) {
      handlePopup(e);
    } else {
      handleAction(e);
      if (e.getClickCount() == 1) {
        handleActionSingleClick(e);
      } else if (e.getClickCount() > 1) {
        handleActionSeveralClicks(e);
      }
    }
  }

  /**
   * What do do when a popup has been required ?.
   * 
   * @param e the mouse event
   */
  public void handlePopup(final MouseEvent e) {
    // Not abstract as we don't want to force user to override every methods
  }

  /**
   * What do do when an action (left click under Windows/Linux) has been
   * required with a single click ?.
   * 
   * @param e the mouse event
   */
  public void handleActionSingleClick(final MouseEvent e) {
    // Not abstract as we don't want to force user to override every methods
  }

  /**
   * What do do when an action (left click under Windows/Linux) has been
   * required with several clicks ?.
   * 
   * @param e the mouse event
   */
  public void handleActionSeveralClicks(final MouseEvent e) {
    // Not abstract as we don't want to force user to override every methods
  }

  /**
   * What do do when an action (left click under Windows/Linux) has been
   * required with any number of clicks ?
   * <p>
   * Beware : do not use this method in addition to handleActionSeveralClicks()
   * or handleActionSingleClick() as both will be executed.
   * <p>
   * 
   * @param e the mouse event
   */
  public void handleAction(final MouseEvent e) {
    // Not abstract as we don't want to force user to override every methods
  }

}
