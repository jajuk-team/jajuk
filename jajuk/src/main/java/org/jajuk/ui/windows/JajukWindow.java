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
package org.jajuk.ui.windows;

/**
 * Global interface for jajuk frames (main window, fullscreen, tray, slimbar...)
 * A window has four states : - Instanciated (the constructor contains general
 * initialization or nothing) - Build (the initUI() method has been called ) -
 * Shown (the display(true) method has been called - Hidden (the display(true)
 * has not yet been called or the display(false) has been called) These states
 * are listed in the WindowState enum These states are managed by the
 * WindowStateDecorator
 */
public interface JajukWindow {

  /**
   * Build the GUI (widgets)
   * <p>
   * Must be called from the EDT
   * </p>
   */
  public void initUI();

  /** Return the window state decorator* */
  public WindowStateDecorator getWindowStateDecorator();

}
