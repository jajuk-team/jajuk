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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * DOCUMENT_ME.
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
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      // required by interface, but nothing to do here...
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      dialog.setVisible(false);
    }

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
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        showJPopupMenu(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        showJPopupMenu(e);
      }
    });
  }

  /**
   * Show j popup menu.
   * DOCUMENT_ME
   * 
   * @param e DOCUMENT_ME
   */
  private void showJPopupMenu(MouseEvent e) {
    if (e.isPopupTrigger() && menu != null) {
      Dimension size = menu.getPreferredSize();
      dialog.setLocation(e.getX(), e.getY() - size.height);
      dialog.setVisible(true);
      menu.show(dialog.getContentPane(), 0, 0);
      // popup works only for focused windows
      dialog.toFront();
    }
  }

  /**
   * Gets the j popu menu.
   * 
   * @return the j popu menu
   */
  public JPopupMenu getJPopuMenu() {
    return menu;
  }

  /**
   * Sets the j popu menu.
   * 
   * @param menu the new j popu menu
   */
  public void setJPopuMenu(JPopupMenu menu) {
    if (this.menu != null) {
      this.menu.removePopupMenuListener(popupListener);
    }
    this.menu = menu;
    menu.addPopupMenuListener(popupListener);
  }

  /**
   * Creates the gui.
   * DOCUMENT_ME
   */
  private static void createGui() {
    JXTrayIcon tray = new JXTrayIcon(createImage());
    tray.setJPopuMenu(createJPopupMenu());
    try {
      SystemTray.getSystemTray().add(tray);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  /**
   * The main method.
   * 
   * @param args the arguments
   * 
   * @throws Exception the exception
   */
  public static void main(String[] args) throws Exception {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createGui();
      }
    });
  }

  /**
   * Creates the image.
   * DOCUMENT_ME
   * 
   * @return the image
   */
  static Image createImage() {
    BufferedImage i = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = (Graphics2D) i.getGraphics();
    g2.setColor(Color.RED);
    g2.fill(new Ellipse2D.Float(0, 0, i.getWidth(), i.getHeight()));
    g2.dispose();
    return i;
  }

  /**
   * Creates the j popup menu.
   * DOCUMENT_ME
   * 
   * @return the j popup menu
   */
  static JPopupMenu createJPopupMenu() {
    final JPopupMenu m = new JPopupMenu();
    m.add(new JMenuItem("Item 1"));
    m.add(new JMenuItem("Item 2"));
    JMenu submenu = new JMenu("Submenu");
    submenu.add(new JMenuItem("item 1"));
    submenu.add(new JMenuItem("item 2"));
    submenu.add(new JMenuItem("item 3"));
    m.add(submenu);
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    m.add(exitItem);
    return m;
  }
}
