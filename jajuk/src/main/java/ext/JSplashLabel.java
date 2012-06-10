/*
 * JSplash.java
 *
 * This file has been adapted to Jajuk by the Jajuk Team.
 *
 * The original copyrights and license follow:
 *
 * Copyright (c) 2004,2005 Gregory Kotsaftis
 * gregkotsaftis@yahoo.com
 * http://zeus-jscl.sourceforge.net/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
package ext;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.MediaTracker;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Extends JLabel to provide support for custom text drawing inside image used
 * for JSplash component.
 * <p>
 * 
 * @author Gregory Kotsaftis
 * @since 1.06
 */
public final class JSplashLabel extends JLabel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Used to draw the text string. */
  private String mText = null;

  /** Used to draw the copyright notice. */
  private String mCopyright = null;

  /** Font to use when drawing the text. */
  private Font mFont = null;

  /**
   * Constructor.
   * <p>
   *
   * @param url The location of the image (<b>it cannot be null</b>).
   * @param copyright 
   * @param text The string to draw (can be null).
   * @param font The font to use (can be null).
   */
  public JSplashLabel(URL url, String copyright, String text, Font font) {
    super();

    ImageIcon icon = new ImageIcon(url);
    if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
      System.err.println("Cannot load splash screen: " + url);
      setText("Cannot load splash screen: " + url);
    } else {
      setIcon(icon);
      mCopyright = copyright;
      mText = text;
      mFont = font;
      if (mFont != null) {
        setFont(mFont);
      }
    }
  }

  /**
   * Overrides paint in order to draw the version number on the splash screen.
   * <p>
   * 
   * @param g The graphics context to use.
   */
  @Override
  public void paint(Graphics g) {
    super.paint(g);

    if (mText != null) {
      g.setColor(Color.BLACK);
      // Draw copyright notice
      FontMetrics fm = g.getFontMetrics();
      int width = fm.stringWidth(mCopyright) + 50;
      int height = fm.getHeight();
      g.drawString(mCopyright, getWidth() - width, (getHeight() - height) - 20);

      // Draw release
      g.drawString(mText, getWidth() - width, (getHeight() - height));
    }
  }

}
