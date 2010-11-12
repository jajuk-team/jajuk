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

package org.jajuk.ui.helpers.animations;

import java.awt.Window;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base implementation of IAnimation.
 */
public abstract class AbstractAnimation implements IAnimation {

  /** DOCUMENT_ME. */
  protected Window window;

  /** DOCUMENT_ME. */
  private CopyOnWriteArrayList<AnimationCompletedListener> listeners = new CopyOnWriteArrayList<AnimationCompletedListener>();

  /**
   * Instantiates a new abstract animation.
   * 
   * @param window DOCUMENT_ME
   */
  protected AbstractAnimation(Window window) {
    this.window = window;
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.helpers.animations.IAnimation#getWindow()
   */
  @Override
  public Window getWindow() {
    return window;
  }

  /**
   * Adds the animation completed listener.
   * DOCUMENT_ME
   * 
   * @param listener DOCUMENT_ME
   */
  public void addAnimationCompletedListener(AnimationCompletedListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes the animation completed listener.
   * DOCUMENT_ME
   * 
   * @param listener DOCUMENT_ME
   */
  public void removeAnimationCompletedListener(AnimationCompletedListener listener) {
    listeners.remove(listener);
  }

  /**
   * Animation completed.
   * DOCUMENT_ME
   */
  protected void animationCompleted() {
    AnimationCompletedEvent event = new AnimationCompletedEvent(this, window);
    for (AnimationCompletedListener listener : listeners) {
      listener.animationCompleted(event);
    }
  }
}
