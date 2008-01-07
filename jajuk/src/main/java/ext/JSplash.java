/*
 * JSplash.java
 *
 * This file has been adapted to Jajuk by the Jajuk Team.
 * Jajuk Copyright (C) 2007 The Jajuk Team
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
 */
package ext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

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
 *        <p>
 *        Adapted to Jajuk by The Jajuk Team
 */
public final class JSplash extends JFrame implements ITechnicalStrings {

  private static final long serialVersionUID = 1L;

  private static final String JAJUK_ICON = "icons/64x64/jajuk-icon_64x64.png";

  /**
   * Progress bar to use in the splash screen.
   */
  private JProgressBar m_progress = null;

  /**
   * Check for whether to use the progress bar or not.
   */
  private boolean m_progressBar = false;

  /**
   * Check for whether to use progress bar messages or not.
   */
  private boolean m_progressBarMessages = false;

  /**
   * Check for whether to use precentage values or not.
   */
  private boolean m_progressBarPercent = false;

  /**
   * Constructor for the splash window.
   * <p>
   * 
   * @param url
   *          Image for ImageIcon.
   * @param progress
   *          Do we want a progress bar at all?
   * @param messages
   *          If we want a progress bar, do we want to display messages inside
   *          the progress bar?
   * @param percent
   *          If we want a progress bar, do we want to display the percent?
   * @param CopyrightString
   *          Copyright notice
   * @param versionString
   *          If null no string is displayed on the bottom-right of the splash
   *          window.
   * @param versionStringFont
   *          Font for version string, if null default.
   * @param versionStringColor
   *          Color for version string, if null default.
   *          <p>
   *          <b>NOTE:</b> Use only one flag for: messages / percent (one or
   *          the other, NOT both).
   */
  public JSplash(URL url, boolean progress, boolean messages, boolean percent,
      String copyrightString, String versionString, Font versionStringFont, Color versionStringColor) {
    super();
    setTitle(Messages.getString("JajukWindow.17"));
	
    // check if we can load the icon
    URL icon = Util.getResource(JAJUK_ICON); 
    if(null == icon)
      throw new RuntimeException(Messages.getString("JajukWindow.38") + JAJUK_ICON);
	
    // Do not use IconLoader class here to avoid loading all icons now
    setIconImage(new ImageIcon(icon).getImage());
    setUndecorated(true);

    m_progressBar = progress;
    m_progressBarMessages = messages;
    m_progressBarPercent = percent;

    // build a panel with a black line for border,
    // and set it as the content pane
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    setContentPane(panel);

    // build a label and set it's icon
    //
    JSplashLabel label = new JSplashLabel(url, copyrightString, versionString, versionStringFont,
        versionStringColor);

    // build a progress bar
    //
    if (m_progressBar) {
      m_progress = new JProgressBar();

      if (m_progressBarMessages || m_progressBarPercent) {
        m_progress.setStringPainted(true);
      } else {
        m_progress.setStringPainted(false);
      }

      if (m_progressBarMessages && !m_progressBarPercent) {
        m_progress.setString("");
      }

      m_progress.setMaximum(100);
      m_progress.setMinimum(0);
      m_progress.setValue(0);
      m_progress.setFont(new Font("verdana", Font.BOLD, 15));
    }

    // add the components to the panel
    //
    getContentPane().add(label, BorderLayout.CENTER);

    if (m_progressBar) {
      getContentPane().add(m_progress, BorderLayout.SOUTH);
    }

    // validate, and display the components
    pack();

    // center on screen
    setLocationRelativeTo(this);

    // hide the panel for now...
    setVisible(false);
  }

  /**
   * Displays the splash screen
   */
  public void splashOn() {
    setVisible(true);

  }

  /**
   * Hides and disposes the splash screen
   */
  public void splashOff() {
    setVisible(false);
    dispose();
  }

  /**
   * Sets the progress indicator (values: 0 - 100).
   * <p>
   * 
   * @param value
   *          The progress indicator value.
   */
  public void setProgress(int value) {
    if (m_progressBar && value >= 0 && value <= 100) {
      m_progress.setValue(value);
    }
  }

  /**
   * Sets the progress indicator (values: 0 - 100) and a label to print inside
   * the progress bar.
   * <p>
   * 
   * @param value
   *          The progress indicator value.
   * @param msg
   *          The message to print.
   */
  public void setProgress(int value, String msg) {
    setProgress(value);

    if (m_progressBarMessages && !m_progressBarPercent && msg != null) {
      m_progress.setString(msg);
    }
  }

}
