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

package org.jajuk.ui.helpers.animations;

import java.awt.Window;

/**
 * Event thrown when the naimation is done.
 */
public class AnimationCompletedEvent {

  private IAnimation source;

  private Window window;

  /**
   * Instantiates a new animation completed event.
   * 
   * @param source 
   * @param window 
   */
  public AnimationCompletedEvent(IAnimation source, Window window) {
    this.source = source;
    this.window = window;
  }

  /**
   * Gets the source.
   * 
   * @return the source
   */
  public IAnimation getSource() {
    return source;
  }

  /**
   * Gets the window.
   * 
   * @return the window
   */
  public Window getWindow() {
    return window;
  }
}
