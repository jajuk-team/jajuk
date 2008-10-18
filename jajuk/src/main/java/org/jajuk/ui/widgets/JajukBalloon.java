/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.widgets;

import java.awt.Color;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.log.Log;

/**
 * Balloon frame displayed by slimbar and tray. Contains current track
 * information.
 */
public class JajukBalloon extends JDialog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param title :
   *          text to display
   */
  public JajukBalloon(String title) {
    super();
    setUndecorated(true);
    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    getRootPane().setBorder(new LineBorder(Color.BLACK));
    JLabel jl = new JLabel(title);
    jl.setFont(FontManager.getInstance().getFont(JajukFont.DEFAULT));
    jl.setBorder(new EmptyBorder(5, 5, 5, 5));
    add(jl);
    pack();
  }

  /**
   * Show the baloon and hide it after few secs
   */
  public void display() {
    setVisible(true);
    // The toFront() is required under windows when main window is not
    // visible
    toFront();
    // Dispose the dialog after 5 seconds
    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(3000);
          dispose();
        } catch (InterruptedException e) {
          Log.error(e);
        }
      }
    }.start();
  }

}
