/*
 *  Jajuk
 *  Copyright (C) 2009 The Jajuk Team
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
 * $Revision: 2921 $
 */

package org.jajuk.ui.helpers.animations;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * Slide animation implementation
 */
public class SlideAnimation extends AbstractAnimation {
  private ScreenPosition screenPosition;
  private StartingPosition startingPosition;
  private Direction direction;

  private Timer animationTimer;
  private long animationStart;
  private Rectangle start;
  private Rectangle windowBounds;

  
  public SlideAnimation(Window window, ScreenPosition screenPosition,
      StartingPosition startingPosition, Direction direction) {
    super(window);
    this.screenPosition = screenPosition;
    this.startingPosition = startingPosition;
    this.direction = direction;
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.helpers.animations.IAnimation#animate(int)
   */
  @Override
  public void animate(final int animationTime) {
    window.pack();
    windowBounds = window.getBounds();

    start = startingPosition.getStartingPosition(screenPosition.getScreenPosition(windowBounds));
    start.width = windowBounds.width;
    start.height = windowBounds.height;

    if (!AWTUtilities.isAvailable()) {
      java.awt.EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          window.setLocation(direction.getCurrentLocation(start, 1));
          new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                Thread.sleep(animationTime);
              } catch (Exception ex) {

              }
              animationCompleted();
            }
          }).start();
        }
      });
      return;
    }

    ActionListener animationLogic = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        long elapsed = System.currentTimeMillis() - animationStart;

        if (elapsed > animationTime) {
          window.pack();

          Point position = direction.getCurrentLocation(start, 1);
          window.setLocation(position);

          Rectangle bounds = direction.getShowingBounds(start, 1);
          boolean visible = !(bounds.width == 0 || bounds.height == 0);
          if (visible) {
            AWTUtilities.setWindowShape(window, bounds);
          }

          animationTimer.stop();
          window.pack();
          window.repaint();

          window.setVisible(visible);
          animationCompleted();
        } else {
          float progress = (float) elapsed / animationTime;

          if (progress > 1) {
            progress = 1;
          }

          Rectangle bounds = direction.getShowingBounds(start, progress);
          boolean visible = !(bounds.width == 0 || bounds.height == 0);
          if (visible) {
            AWTUtilities.setWindowShape(window, bounds);
          }

          window.setLocation(direction.getCurrentLocation(start, progress));

          window.pack();
          window.setVisible(true);
          window.repaint();
        }
      }
    };

    animationTimer = new Timer(50, animationLogic);
    animationStart = System.currentTimeMillis();
    animationTimer.start();
  }

  public Direction getDirection() {
    return direction;
  }

  public ScreenPosition getScreenPosition() {
    return screenPosition;
  }

  public StartingPosition getStartingPosition() {
    return startingPosition;
  }

  public interface ScreenPosition {
    Rectangle getScreenPosition(Rectangle size);
  }

  public interface StartingPosition {
    Rectangle getStartingPosition(Rectangle position);
  }

  public interface Direction {
    Point getCurrentLocation(Rectangle start, float progress);

    Rectangle getShowingBounds(Rectangle start, float progress);
  }

  public enum ScreenPositions implements ScreenPosition {
    TOP_LEFT {
      @Override
      public Rectangle getScreenPosition(Rectangle size) {
        Rectangle bounds = getDesktopBounds();
        Rectangle position = new Rectangle();
        position.x = bounds.x;
        position.y = bounds.y;
        position.width = size.width;
        position.height = size.height;
        return position;
      }
    },
    TOP_RIGHT {
      @Override
      public Rectangle getScreenPosition(Rectangle size) {
        Rectangle bounds = getDesktopBounds();
        Rectangle position = new Rectangle();
        position.x = bounds.x + bounds.width - size.width;
        position.y = bounds.y;
        position.width = size.width;
        position.height = size.height;
        return position;
      }
    },
    BOTTOM_LEFT {
      @Override
      public Rectangle getScreenPosition(Rectangle size) {
        Rectangle bounds = getDesktopBounds();
        Rectangle position = new Rectangle();
        position.x = bounds.x;
        position.y = bounds.y + bounds.height - size.height;
        position.width = size.width;
        position.height = size.height;
        return position;
      }
    },
    BOTTOM_RIGHT {
      @Override
      public Rectangle getScreenPosition(Rectangle size) {
        Rectangle bounds = getDesktopBounds();
        Rectangle position = new Rectangle();
        position.x = bounds.x + bounds.width - size.width;
        position.y = bounds.y + bounds.height - size.height;
        position.width = size.width;
        position.height = size.height;
        return position;
      }
    },
    CURRENT {
      @Override
      public Rectangle getScreenPosition(Rectangle size) {
        return new Rectangle(size);
      }
    };

    protected Rectangle getDesktopBounds() {
      return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    }
  }

  public enum StartingPositions implements StartingPosition {
    TOP {
      @Override
      public Rectangle getStartingPosition(Rectangle position) {
        Rectangle size = new Rectangle();
        size.x = position.x;
        size.y = position.y;
        size.width = position.width;
        size.height = 0;
        return size;
      }
    },
    BOTTOM {
      @Override
      public Rectangle getStartingPosition(Rectangle position) {
        Rectangle size = new Rectangle();
        size.x = position.x;
        size.y = position.y + position.height;
        size.width = position.width;
        size.height = 0;
        return size;
      }
    },
    LEFT {
      @Override
      public Rectangle getStartingPosition(Rectangle position) {
        Rectangle size = new Rectangle();
        size.x = position.x;
        size.y = position.y;
        size.width = 0;
        size.height = position.height;
        return size;
      }
    },
    RIGHT {
      @Override
      public Rectangle getStartingPosition(Rectangle position) {
        Rectangle size = new Rectangle();
        size.x = position.x + position.width;
        size.y = position.y;
        size.width = 0;
        size.height = position.height;
        return size;
      }
    },
    TOP_LEFT {
      @Override
      public Rectangle getStartingPosition(Rectangle position) {
        Rectangle size = new Rectangle();
        size.x = position.x;
        size.y = position.y;
        size.width = 0;
        size.height = 0;
        return size;
      }
    },
    TOP_RIGHT {
      @Override
      public Rectangle getStartingPosition(Rectangle position) {
        Rectangle size = new Rectangle();
        size.x = position.x + position.width;
        size.y = position.y;
        size.width = 0;
        size.height = 0;
        return size;
      }
    },
    BOTTOM_LEFT {
      @Override
      public Rectangle getStartingPosition(Rectangle position) {
        Rectangle size = new Rectangle();
        size.x = position.x;
        size.y = position.y + position.height;
        size.width = 0;
        size.height = 0;
        return size;
      }
    },
    BOTTOM_RIGHT {
      @Override
      public Rectangle getStartingPosition(Rectangle position) {
        Rectangle size = new Rectangle();
        size.x = position.x + position.width;
        size.y = position.y + position.height;
        size.width = 0;
        size.height = 0;
        return size;
      }
    },
    FULL {
      @Override
      public Rectangle getStartingPosition(Rectangle position) {
        return new Rectangle(position);
      }
    };
  }

  public enum InDirections implements Direction {
    UP {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x;
        current.y = start.y - (int) (start.height * progress);
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = start.width;
        current.height = (int) (start.height * progress);
        current.x = 0;
        current.y = 0;
        return current;
      }
    },
    DOWN {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x;
        current.y = start.y - (start.height - (int) (start.height * progress));
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = start.width;
        current.height = (int) (start.height * progress);
        current.x = 0;
        current.y = start.height - current.height;
        return current;
      }
    },
    LEFT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x - (int) (start.width * progress);
        current.y = start.y;
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * progress);
        current.height = start.height;
        current.x = 0;
        current.y = 0;
        return current;
      }
    },
    RIGHT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x - (start.width - (int) (start.width * progress));
        current.y = start.y;
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * progress);
        current.height = start.height;
        current.x = start.width - current.width;
        current.y = 0;
        return current;
      }
    },
    UP_LEFT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x - (int) (start.width * progress);
        current.y = start.y - (int) (start.height * progress);
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * progress);
        current.height = (int) (start.height * progress);
        current.x = 0;
        current.y = 0;
        return current;
      }
    },
    UP_RIGHT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x - (start.width - (int) (start.width * progress));
        current.y = start.y - (int) (start.height * progress);
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * progress);
        current.height = (int) (start.height * progress);
        current.x = start.width - current.width;
        current.y = 0;
        return current;
      }
    },
    DOWN_LEFT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x - (int) (start.width * progress);
        current.y = start.y - (start.height - (int) (start.height * progress));
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * progress);
        current.height = (int) (start.height * progress);
        current.x = 0;
        current.y = start.height - current.height;
        return current;
      }
    },
    DOWN_RIGHT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x - (start.width - (int) (start.width * progress));
        current.y = start.y - (start.height - (int) (start.height * progress));
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * progress);
        current.height = (int) (start.height * progress);
        current.x = start.width - current.width;
        current.y = start.height - current.height;
        return current;
      }
    };
  }

  public enum OutDirections implements Direction {
    UP {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x;
        current.y = start.y - (int) (start.height * progress);
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = start.width;
        current.height = (int) (start.height * (1 - progress));
        current.x = 0;
        current.y = start.height - current.height;
        return current;
      }
    },
    DOWN {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x;
        current.y = start.y + (int) (start.height * progress);
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = start.width;
        current.height = (int) (start.height * (1 - progress));
        current.x = 0;
        current.y = 0;
        return current;
      }
    },
    LEFT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x - (int) (start.width * progress);
        current.y = start.y;
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * (1 - progress));
        current.height = start.height;
        current.x = start.width - current.width;
        current.y = 0;
        return current;
      }
    },
    RIGHT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x + (int) (start.width * progress);
        current.y = start.y;
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * (1 - progress));
        current.height = start.height;
        current.x = 0;
        current.y = 0;
        return current;
      }
    },
    UP_LEFT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x - (int) (start.width * progress);
        current.y = start.y - (int) (start.height * progress);
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * (1 - progress));
        current.height = (int) (start.height * (1 - progress));
        current.x = start.width - current.width;
        current.y = start.height - current.height;
        return current;
      }
    },
    UP_RIGHT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x + (int) (start.width * progress);
        current.y = start.y - (int) (start.height * progress);
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * (1 - progress));
        current.height = (int) (start.height * (1 - progress));
        current.x = 0;
        current.y = start.height - current.height;
        return current;
      }
    },
    DOWN_LEFT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x - (int) (start.width * progress);
        current.y = start.y + (int) (start.height * progress);
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * (1 - progress));
        current.height = (int) (start.height * (1 - progress));
        current.x = start.width - current.width;
        current.y = 0;
        return current;
      }
    },
    DOWN_RIGHT {
      @Override
      public Point getCurrentLocation(Rectangle start, float progress) {
        Point current = new Point();
        current.x = start.x + (int) (start.width * progress);
        current.y = start.y + (int) (start.height * progress);
        return current;
      }

      @Override
      public Rectangle getShowingBounds(Rectangle start, float progress) {
        Rectangle current = new Rectangle();
        current.width = (int) (start.width * (1 - progress));
        current.height = (int) (start.height * (1 - progress));
        current.x = 0;
        current.y = 0;
        return current;
      }
    };
  }
}
