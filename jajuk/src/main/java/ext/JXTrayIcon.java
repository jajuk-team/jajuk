/*
 *  Code modified from Alexander Potochkin's JXTray class at 
 *  https://swinghelper.dev.java.net/source/browse/swinghelper/src/java/org/jdesktop/swinghelper/tray/JXTrayIcon.java?view=markup
 *  Copyright 2008 Sun Microsystems, Inc., 4150 Network Circle,
 *  Santa Clara, California 95054, U.S.A. All rights reserved.
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

import java.awt.Frame;
import java.awt.Image;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * This class allow to add a Swing JDialog into an AWT Systray
 * 
 * See http://weblogs.java.net/blog/alexfromsun/archive/2008/02/jtrayicon_updat.html
 */
public class JXTrayIcon extends TrayIcon {

  /** DOCUMENT_ME. */
  private JPopupMenu menu;

  /** DOCUMENT_ME. */
  private static JDialog dialog;
  static {
    dialog = new JDialog((Frame) null, "TrayDialog");
    dialog.setUndecorated(true);
    dialog.setAlwaysOnTop(true);
  }

  /** DOCUMENT_ME. */
  private static PopupMenuListener popupListener = new PopupMenuListener() {
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      // required by interface, but nothing to do here...
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      dialog.setVisible(false);
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
      dialog.setVisible(false);
    }
  };

  /**
   * Instantiates a new jX tray icon.
   * 
   * @param image DOCUMENT_ME
   */
  public JXTrayIcon(Image image) {
    super(image);
  }

  /**
   * Show j popup menu.
   * DOCUMENT_ME
   * 
   * @param e DOCUMENT_ME
   */
  public void showJPopupMenu(MouseEvent e) {
    if (menu != null) {
      dialog.setLocation(e.getXOnScreen(), e.getYOnScreen());
      dialog.setVisible(true);
      menu.show(dialog.getContentPane(), 0, 0);
      // popup works only for focused windows
      dialog.toFront();
    }
  }

  /**
   * Gets the JPopupMenu.
   * 
   * @return the JPopupMenu
   */
  public JPopupMenu getJPopuMenu() {
    return menu;
  }

  /**
   * Sets the JPopupMenu.
   * 
   * @param menu the new JPopupMenu
   */
  public void setJPopuMenu(JPopupMenu menu) {
    if (this.menu != null) {
      this.menu.removePopupMenuListener(popupListener);
    }
    this.menu = menu;
    menu.addPopupMenuListener(popupListener);
  }

}
