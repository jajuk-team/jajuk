/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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

import javax.swing.SwingUtilities;

import org.jajuk.util.log.Log;

/**
 * Fade animation implementation.
 */
public class FadeAnimation extends AbstractAnimation {
  private Direction opacity;
  /** Number of frames. */
  private static final int FRAME_NUMBER = 50;

  /**
   * Instantiates a new fade animation.
   * 
   * @param window 
   * @param opacity 
   */
  public FadeAnimation(Window window, Direction opacity) {
    super(window);
    this.opacity = opacity;
  }

  /*
   * (non-Javadoc)
   * @see org.jajuk.ui.helpers.animations.IAnimation#animate(int)
   */
  @Override
  public void animate(final int animationTime) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          window.setVisible(true);
          for (int i = 1; i < FRAME_NUMBER; i++) {
            final float progress = i / (float) FRAME_NUMBER;
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                AWTUtilities.setWindowOpacity(window, opacity.getOpacity(progress));
                window.repaint();
              }
            });
            Thread.sleep(animationTime / FRAME_NUMBER);
          }
        } catch (Exception ex) {
          Log.error(ex);
        }
        animationCompleted();
      }
    }).start();
  }

  /**
   * .
   */
  public interface Direction {
    /**
     * Gets the opacity.
     * 
     * @param progress 
     * 
     * @return the opacity
     */
    float getOpacity(float progress);
  }

  /**
   * .
   */
  public enum Directions implements Direction {
    IN {
      @Override
      public float getOpacity(float progress) {
        return progress;
      }
    },
    OUT {
      @Override
      public float getOpacity(float progress) {
        return 1 - progress;
      }
    };
  }
}
