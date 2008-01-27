/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.ui.thumbnails;

import info.clearthought.layout.TableLayout;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import org.jajuk.base.AuthorManager;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.YearManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.desktop.Desktop;
import org.jvnet.substance.SubstanceLookAndFeel;

/**
 * HTML popup displayed over a thumbnail, it details album informations and
 * tracks
 * <p>
 * It is displayed nicely from provided jlabel position
 * </p>
 */
public class ThumbnailPopup extends JDialog implements ITechnicalStrings {

  private static final long serialVersionUID = -8131528719972829954L;

  JPanel jp;

  /**
   * 
   * @param description
   *          HTML text to display (HTML 3.0)
   * @param origin :
   *          coordonates of the origin item on whish we want to display the
   *          popup
   * @param autoclose :
   *          whether the popup should close when mouse leave the origin item or
   *          is displayed as a regular Dialog
   */
  public ThumbnailPopup(String description, Rectangle origin, boolean autoclose) {
    if (autoclose) {
      setUndecorated(true);
      getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    }
    jp = new JPanel();
    double[][] size = { { TableLayout.FILL }, { TableLayout.FILL } };
    jp.setLayout(new TableLayout(size));
    final JEditorPane text = new JEditorPane("text/html", description);
    text.setEditable(false);
    text.setBackground(SubstanceLookAndFeel.getActiveColorScheme().getUltraLightColor());
    text.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == EventType.ACTIVATED) {
          URL url = e.getURL();
          if (XML_AUTHOR.equals(url.getHost())) {
            ArrayList<Item> items = new ArrayList<Item>(1);
            items.add(AuthorManager.getInstance().getItemByID(url.getQuery()));
            new PropertiesWizard(items);
          } else if (XML_STYLE.equals(url.getHost())) {
            ArrayList<Item> items = new ArrayList<Item>(1);
            items.add(StyleManager.getInstance().getItemByID(url.getQuery()));
            new PropertiesWizard(items);
          } else if (XML_YEAR.equals(url.getHost())) {
            ArrayList<Item> items = new ArrayList<Item>(1);
            items.add(YearManager.getInstance().getItemByID(url.getQuery()));
            new PropertiesWizard(items);
          } else if (XML_URL.equals(url.getHost())) {
            try {
              Desktop.browse(new URL(url.getQuery()));
            } catch (Exception e1) {
              Log.error(e1);
            }
          } else if (XML_TRACK.equals(url.getHost())) {
            ArrayList<Item> items = new ArrayList<Item>(1);
            Track track = (Track) TrackManager.getInstance().getItemByID(url.getQuery());
            items.add(track);
            ArrayList<org.jajuk.base.File> toPlay = new ArrayList<org.jajuk.base.File>(1);
            File file = track.getPlayeableFile(true);
            toPlay.add(file);
            FIFO.getInstance().push(
                Util.createStackItems(Util.applyPlayOption(toPlay), ConfigurationManager
                    .getBoolean(CONF_STATE_REPEAT), true), false);
          }
        }
        // change cursor on entering or leaving
        // hyperlinks
        // This doesn't work under JRE 1.5 (at least
        // under Linux), Sun issue ?
        else if (e.getEventType() == EventType.ENTERED) {
          text.setCursor(Util.LINK_CURSOR);
        } else if (e.getEventType() == EventType.EXITED) {
          text.setCursor(Util.DEFAULT_CURSOR);
        }
      }
    });
    final JScrollPane jspText = new JScrollPane(text);
    jspText.getVerticalScrollBar().setValue(0);
    jp.add(jspText, "0,0");
    setContentPane(jp);
    if (autoclose) {
      // Make sure to close this popup when it lost focus
      jspText.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
          // Test if mouse is really outside the popup, for unknown reason,
          // this event is catch when entering the popup (Windows)
          if (!jspText.contains(e.getPoint())) {
            dispose();
          }
        }
      });
    }
    if (origin != null) {
      // compute dialog position ( note that setRelativeTo
      // is buggy and that we need more advanced positioning)
      int x = (int) origin.getX() + (int) (0.6 * origin.getWidth());
      // set position at 60 % of the picture
      int y = (int) origin.getY() + (int) (0.6 * origin.getHeight());
      int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
      int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
      // Adjust position if details are located outside
      // the screen
      // in x-axis
      if ((x + 500) > screenWidth) {
        x = screenWidth - 510;
      }
      if ((y + 400) > screenHeight) {
        x = (int) origin.getX() + (int) (0.6 * origin.getWidth());
        if ((x + 500) > screenWidth) {
          x = screenWidth - 510;
        }
        y = (int) origin.getY() + (int) (0.4 * origin.getHeight()) - 400;
      }
      setLocation(x, y);
    } else {
      setLocationByPlatform(true);
    }
    setSize(500, 400);
    setVisible(true);
    // Force scrollbar to stay on top
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        jspText.getVerticalScrollBar().setValue(0);
      }
    });
  }

  /**
   * Allow closing the window when pressing escape key Thanks
   * http://www.javaworld.com/javaworld/javatips/javatip72/EscapeDialog.java
   */
  protected JRootPane createRootPane() {
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        dispose();
      }
    };
    JRootPane rootPane = new JRootPane();
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    return rootPane;
  }

}
