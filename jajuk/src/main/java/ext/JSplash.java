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
 * 
 */
package ext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;

import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilSystem;

/**
 * A simple, yet nice splash screen implementation for java applications.
 * Follows Sun recommendations for splash screen and logos: see <a
 * href="http://java.sun.com/products/jlf/ed2/book/HIG.Graphics7.html">
 * <i>"Designing Graphics for Corporate and Product Identity"</i></a>. Draws a
 * black border of one pixel wide around the splash image. Also uses a simple
 * progress bar that the user must "progress" manually in his code in order for
 * it to work. Also, it has options for percent display, custom loading messages
 * display and application version string display at the bottom-right corner of
 * the image.
 * <p>
 * 
 * @author Gregory Kotsaftis
 * @since 1.0
 * <p>
 * Adapted to Jajuk by The Jajuk Team
 */
public final class JSplash extends JFrame {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant JAJUK_ICON.   */
  private static final String JAJUK_ICON = "icons/64x64/jajuk-icon_64x64.png";

  /** Progress bar to use in the splash screen. */
  private JProgressBar mProgress = null;

  /** Check for whether to use the progress bar or not. */
  private boolean mProgressBar = false;

  /** Check for whether to use progress bar messages or not. */
  private boolean mProgressBarMessages = false;

  /** Check for whether to use percentage values or not. */
  private boolean mProgressBarPercent = false;

  /**
   * Constructor for the splash window.
   * <p>
   * 
   * @param url Image for ImageIcon.
   * @param progress Do we want a progress bar at all?
   * @param messages If we want a progress bar, do we want to display messages inside
   * the progress bar?
   * @param percent If we want a progress bar, do we want to display the percent?
   * @param copyrightString Copyright notice
   * @param versionString If null no string is displayed on the bottom-right of the splash
   * window.
   * @param versionStringFont Font for version string, if null default.
   * <p>
   * <b>NOTE:</b> Use only one flag for: messages / percent (one or
   * the other, NOT both).
   */
  public JSplash(URL url, boolean progress, boolean messages, boolean percent,
      String copyrightString, String versionString, Font versionStringFont) {
    super();
    setTitle(Messages.getString("JajukWindow.17"));

    // check if we can load the icon
    URL icon = UtilSystem.getResource(JAJUK_ICON);
    if (null == icon) {
      throw new IllegalArgumentException(
          "Resource not found in Classpath. Can not load icon from location: " + JAJUK_ICON);
    }

    // Do not use IconLoader class here to avoid loading all icons now
    setIconImage(new ImageIcon(icon).getImage());
    setUndecorated(true);
    getRootPane().setWindowDecorationStyle(JRootPane.NONE);

    mProgressBar = progress;
    mProgressBarMessages = messages;
    mProgressBarPercent = percent;

    // build a panel with a black line for border,
    // and set it as the content pane
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    setContentPane(panel);

    if (url == null) {
      throw new IllegalArgumentException("Invalid URL specified for Splashscreen");
    }
    // build a label and set it's icon
    JSplashLabel label = new JSplashLabel(url, copyrightString, versionString, versionStringFont);

    // build a progress bar and a tips of the day scrolling text
    String totd = Messages.getShuffleTipOfTheDay();
    // Remove pictures urls
    if (totd.matches(".*<a.*")) {
      totd = totd.substring(0, totd.indexOf("<a"));
    }
    totd += "     ";
    JScrollingText scrollingText = new JScrollingText(totd, -5);
    scrollingText.setPreferredSize(new Dimension(400, 20));
    scrollingText.setMaximumSize(new Dimension(400, 20));
    GridLayout layout = new GridLayout(2, 1, 0, 0);
    JPanel jpTotdAndProgress = new JPanel(layout);
    jpTotdAndProgress.setBorder(new EmptyBorder(4, 5, 0, 5));
    scrollingText.start();
    if (mProgressBar) {
      mProgress = new JProgressBar();

      if (mProgressBarMessages || mProgressBarPercent) {
        mProgress.setStringPainted(true);
      } else {
        mProgress.setStringPainted(false);
      }

      if (mProgressBarMessages && !mProgressBarPercent) {
        mProgress.setString("");
      }

      mProgress.setMaximum(100);
      mProgress.setMinimum(0);
      mProgress.setValue(0);
      mProgress.setFont(FontManager.getInstance().getFont(JajukFont.SPLASH_PROGRESS));
      jpTotdAndProgress.add(mProgress);
      jpTotdAndProgress.add(scrollingText);

    }

    // add the components to the panel
    getContentPane().add(label, BorderLayout.CENTER);

    if (mProgressBar) {
      getContentPane().add(jpTotdAndProgress, BorderLayout.SOUTH);
    }

    // validate, and display the components
    pack();

    // center on screen
    setLocationRelativeTo(this);

  }

  /**
   * Displays the splash screen.
   */
  public void splashOn() {
    setVisible(true);

  }

  /**
   * Hides and disposes the splash screen.
   */
  public void splashOff() {
    setVisible(false);
    dispose();
  }

  /**
   * Sets the progress indicator (values: 0 - 100).
   * <p>
   * 
   * @param value The progress indicator value.
   */
  public void setProgress(int value) {
    if (mProgressBar && value >= 0 && value <= 100) {
      mProgress.setValue(value);
    }
  }

  /**
   * Sets the progress indicator (values: 0 - 100) and a label to print inside
   * the progress bar.
   * <p>
   * 
   * @param value The progress indicator value.
   * @param msg The message to print.
   */
  public void setProgress(int value, String msg) {
    setProgress(value);
    repaint();

    if (mProgressBarMessages && !mProgressBarPercent && msg != null) {
      mProgress.setString(msg);
    }
  }

}
