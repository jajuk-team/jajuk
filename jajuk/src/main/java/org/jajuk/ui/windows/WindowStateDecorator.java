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
package org.jajuk.ui.windows;

import java.awt.Component;
import java.awt.Window;

import org.jajuk.util.log.Log;

/**
 * Add display and state storage functionalities to jajuk windows (main window,
 * fullscreen, tray, slimbar...)
 */
public abstract class WindowStateDecorator {

  /** Current state. */
  private WindowState state = WindowState.NOT_BUILT;

  /** Decorated window *. */
  private IJajukWindow window;

  /**
   * Builds the decorator.
   * 
   * @param window decorated window
   */
  public WindowStateDecorator(IJajukWindow window) {
    this.window = window;
  }

  /**
   * Gets the window state.
   * 
   * @return Returns the bVisible.
   */
  public WindowState getWindowState() {
    return state;
  }

  /**
   * Set the display state to the window.
   * 
   * @param state Window state
   */
  public void setWindowState(WindowState state) {
    this.state = state;
  }

  /**
   * Show or hide the frame
   * <p>
   * Must be called within the EDT
   * </p>.
   * 
   * @param show whether the window should be shown or hidden
   */
  public void display(boolean show) {
    try {
      // mode is already ok, leave
      if ((!show && state != WindowState.BUILT_DISPLAYED)
          || (show && state == WindowState.BUILT_DISPLAYED)) {
        return;
      }
      // Build the GUI is not already done, executes window-specific behavior
      if (show && state == WindowState.NOT_BUILT) {
        window.initUI();
      }

      // Show or hide specific code before the window is made visible
      if (show) {
        window.getWindowStateDecorator().specificBeforeShown();
        ((Component) window).validate();
      } else {
        window.getWindowStateDecorator().specificBeforeHidden();
      }

      // Display or hide the window
      ((Component) window).setVisible(show);

      // Show or hide specific code after the window is made visible
      if (show) {
        window.getWindowStateDecorator().specificAfterShown();
        ((Component) window).validate();
      } else {
        window.getWindowStateDecorator().specificAfterHidden();
      }

      // store the new state
      if (show) {
        state = WindowState.BUILT_DISPLAYED;
      } else {
        state = WindowState.BUILT_NOT_DISPLAYED;
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Bring window to front if it is a java.awt.Window component or does nothing otherwise.
   * @throws IllegalStateException if the component has not yet been displayed
   */
  public void toFront() {
    if (state != WindowState.BUILT_DISPLAYED) {
      throw new IllegalStateException("Can't call toFront() on non-displayed windows");
    }
    if (window instanceof Window) {
      ((Window) window).toFront();
    }
  }

  /**
   * convenient method to get build status of the window.
   * 
   * @return whether the initUI() has run successfully or not
   */
  public boolean isInitialized() {
    return getWindowState() == WindowState.BUILT_DISPLAYED
        || getWindowState() == WindowState.BUILT_NOT_DISPLAYED;
  }

  /**
   * convenient method to get visible status of the window.
   * 
   * @return whether the window is displayed
   */
  public boolean isDisplayed() {
    return getWindowState() == WindowState.BUILT_DISPLAYED;
  }

  /**
   * Specific actions before the window is shown To be overridden in each Window
   * class WindowDecorator.
   */
  abstract public void specificBeforeShown();

  /**
   * Specific actions after the window is shown To be overridden in each Window
   * class WindowDecorator.
   */
  abstract public void specificAfterShown();

  /**
   * Specific actions before the window is hidden To be overridden in each
   * Window class WindowDecorator.
   */
  abstract public void specificBeforeHidden();

  /**
   * Specific actions after the window is hidden To be overridden in each Window
   * class WindowDecorator.
   */
  abstract public void specificAfterHidden();

}
