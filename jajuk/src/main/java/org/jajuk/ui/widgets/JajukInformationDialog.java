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
package org.jajuk.ui.widgets;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.log.Log;

/**
 * Dialog displayed by slimbar and tray or notificators.
 */
public class JajukInformationDialog extends JDialog {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** Has this dialog already been hidden ?. */
  private boolean hasAlreadyBeenHidden = false;

  /**
   * The Constructor.
   * 
   * @param text : text to display
   * @param owner parent owner, see  #1582 ([Linux] Void entry in task bar for information dialog)
   */
  public JajukInformationDialog(String text, Window owner) {
    // An annoying entry appears under linux in the taskbar. We have no way so far to fix it.
    //see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7078460
    super(owner);
    setFocusableWindowState(false);
    setFocusable(false);
    setUndecorated(true);
    // Don't use setAlwaysOnTop or the toast steals the focus, see #1636
    //setAlwaysOnTop(true);
    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    getRootPane().setBorder(new LineBorder(Color.BLACK));
    JLabel jl = new JLabel(text);
    jl.setFont(FontManager.getInstance().getFont(JajukFont.DEFAULT));
    jl.setBorder(new EmptyBorder(5, 5, 5, 5));
    // Allow user to close the dialog by clicking on it
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        dispose();
      }
    });
    // Fix for #1778 : the Window.dispose() method doesn't seem to work (under Linux at least) if the component is not visible.
    // We add a component listener to detect hide/show. If the dialog has already been hidden once, it is disposed.
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent e) {
        if (hasAlreadyBeenHidden) {
          dispose();
        }
      }

      @Override
      public void componentHidden(ComponentEvent e) {
        hasAlreadyBeenHidden = true;
      }
    });
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
          close();
        } catch (InterruptedException e) {
          Log.error(e);
        }
      }
    }.start();
  }

  /**
   * Close the dialog.
   */
  public void close() {
    // Call dispose from the EDT, otherwise, it seems to block in some rare cases under
    // Windows, see #1514
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        dispose();
      }
    });
  }
}
