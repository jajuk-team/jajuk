package org.jajuk.ui.widgets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.jajuk.ui.helpers.animations.AnimationCompletedEvent;
import org.jajuk.ui.helpers.animations.AnimationCompletedListener;
import org.jajuk.ui.helpers.animations.FadeAnimation;
import org.jajuk.ui.helpers.animations.SlideAnimation;
import org.jajuk.ui.helpers.animations.SlideAnimation.InDirections;
import org.jajuk.ui.helpers.animations.SlideAnimation.ScreenPositions;
import org.jajuk.ui.helpers.animations.SlideAnimation.StartingPositions;
import org.jajuk.util.log.Log;

/**
 * Animated balloon with album text + cover
 */
public class JajukTrackChangeBalloon extends JajukBalloon {
  private static final long serialVersionUID = 1L;
  private int showTime;

  public JajukTrackChangeBalloon(String title, int showTime) {
    super(title);
    this.showTime = showTime;
    setAlwaysOnTop(true);
    setFocusableWindowState(false);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        dispose();
      }
    });
  }

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
            JajukTrackChangeBalloon.this.dispose();
          }
        });
        new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              Thread.sleep(showTime);
            } catch (InterruptedException ex) {
              Log.error(ex);
            }
            fade.animate(2000);
          }
        }).start();
      }
    });
    slide.animate(2000);
  }
}
