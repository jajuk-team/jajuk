/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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
package org.jajuk.ui.widgets;

import java.awt.Window;

import org.jajuk.ui.helpers.animations.AnimationCompletedEvent;
import org.jajuk.ui.helpers.animations.AnimationCompletedListener;
import org.jajuk.ui.helpers.animations.FadeAnimation;
import org.jajuk.ui.helpers.animations.SlideAnimation;
import org.jajuk.ui.helpers.animations.SlideAnimation.InDirections;
import org.jajuk.ui.helpers.animations.SlideAnimation.ScreenPositions;
import org.jajuk.ui.helpers.animations.SlideAnimation.StartingPositions;
import org.jajuk.util.log.Log;

/**
 * Animated information dialog that appears and disappears by itself.
 */
public class JajukToast extends JajukInformationDialog {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new jajuk toast.
   * 
   * @param text DOCUMENT_ME
   * @param owner parent owner, see  #1582 ([Linux] Void entry in task bar for information dialog)
   */
  public JajukToast(String text, Window owner) {
    super(text, owner);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.widgets.JajukInformationDialog#display()
   */
  @Override
  public void display() {
    SlideAnimation slide = new SlideAnimation(this, ScreenPositions.BOTTOM_RIGHT,
        StartingPositions.RIGHT, InDirections.LEFT);
    slide.addAnimationCompletedListener(new AnimationCompletedListener() {
      @Override
      public void animationCompleted(final AnimationCompletedEvent e) {
        final FadeAnimation fade = new FadeAnimation(e.getWindow(), FadeAnimation.Directions.OUT);
        fade.addAnimationCompletedListener(new AnimationCompletedListener() {
          @Override
          public void animationCompleted(AnimationCompletedEvent e) {
            JajukToast.this.dispose();
          }
        });
        new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              // Keep toast visible some times before beginning the fading (windows only)
              Thread.sleep(3000);
              // No fading if user already closed the toast by clicking on it
              if (!JajukToast.this.isVisible()) {
                return;
              }
            } catch (InterruptedException ex) {
              Log.error(ex);
            }
            fade.animate(1000);
          }
        }).start();
      }
    });
    slide.animate(2000);
  }
}
