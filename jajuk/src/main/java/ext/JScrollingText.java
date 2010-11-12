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
package ext;

/**
 * Scrolling text component Code found at
 * http://www.developpez.net/forums/archive/index.php/t-41622.html Thanks
 * "herve91"
 */
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

/**
 * DOCUMENT_ME.
 */
public class JScrollingText extends JLabel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 3068213731703270035L;

  /** DOCUMENT_ME. */
  private final int speed;

  /** DOCUMENT_ME. */
  private final int period;

  /** DOCUMENT_ME. */
  private final int offset;

  /** DOCUMENT_ME. */
  private int x = 300;

  /**
   * Instantiates a new j scrolling text.
   * 
   * @param text DOCUMENT_ME
   */
  public JScrollingText(String text) {
    this(text, 1);
  }

  /**
   * Instantiates a new j scrolling text.
   * 
   * @param text DOCUMENT_ME
   * @param speed DOCUMENT_ME
   */
  public JScrollingText(String text, int speed) {
    this(text, speed, 100);
  }

  /**
   * Instantiates a new j scrolling text.
   * 
   * @param text DOCUMENT_ME
   * @param speed DOCUMENT_ME
   * @param period DOCUMENT_ME
   */
  public JScrollingText(String text, int speed, int period) {
    this(text, speed, period, 0);
  }

  /**
   * Instantiates a new j scrolling text.
   * 
   * @param text DOCUMENT_ME
   * @param speed DOCUMENT_ME
   * @param period DOCUMENT_ME
   * @param offset DOCUMENT_ME
   */
  public JScrollingText(String text, int speed, int period, int offset) {
    super(text);
    this.speed = speed;
    this.period = period;
    this.offset = offset;
  }

  /* (non-Javadoc)
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  public void paintComponent(Graphics g) {
    if (isOpaque()) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
    }
    g.setColor(getForeground());

    FontMetrics fm = g.getFontMetrics();
    Insets insets = getInsets();

    int width = getWidth() - (insets.left + insets.right);
    int height = getHeight() - (insets.top + insets.bottom);

    int textWidth = fm.stringWidth(getText());
    if (width < textWidth) {
      width = textWidth + offset;
    }
    x %= width;

    int textX = insets.left + x;
    int textY = insets.top + (height - fm.getHeight()) / 2 + fm.getAscent();

    g.drawString(getText(), textX, textY);
    g.drawString(getText(), textX + (speed > 0 ? -width : width), textY);
  }

  Timer timer;

  /**
   * Start.
   * DOCUMENT_ME
   */
  public void start() {
    timer = new Timer("Scrolling Text Timer");
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        x += speed;
        repaint();
      }
    };
    timer.scheduleAtFixedRate(task, 1000, period);
  }

  /* (non-Javadoc)
   * @see javax.swing.JComponent#removeNotify()
   */
  @Override
  public void removeNotify() {
    //    System.out.println("Removing Notify in JSCrollingText");

    // clean out the timer
    if (timer != null) {
      timer.cancel();
      timer = null;
    }

    super.removeNotify();
  }
}
