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
package org.jajuk.ui.widgets;

import java.awt.Color;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.log.Log;

/**
 * Dialog displayed by slimbar and tray or notificators.
 */
public class JajukInformationDialog extends JDialog {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * The Constructor.
   * 
   * @param text : text to display
   */
  public JajukInformationDialog(String text) {
    // We have to set the owner window to fix a JRE issue under Linux only
    // See
    // http://www.velocityreviews.com/forums/t125048-re-jdialog-in-taskbar-under-linux.html
    // Otherwise, if no owner window is given or if the owner window is not
    // displayed, an ugly task entry appears on the task bar
    super(UtilGUI.getActiveWindow());
    setUndecorated(true);
    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    getRootPane().setBorder(new LineBorder(Color.BLACK));
    JLabel jl = new JLabel(text);
    jl.setFont(FontManager.getInstance().getFont(JajukFont.DEFAULT));
    jl.setBorder(new EmptyBorder(5, 5, 5, 5));
    add(jl);
    pack();
  }

  /**
   * Show the balloon and hide it after few secs.
   */
  public void display() {
    setVisible(true);
    // The toFront() is required under windows when main window is not
    // visible
    toFront();
    // Dispose the dialog after 5 seconds
    new Thread("Balloon Display Thread") {
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
