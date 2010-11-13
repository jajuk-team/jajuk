/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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

package org.jajuk.ui.thumbnails;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import org.jajuk.base.Artist;
import org.jajuk.base.ArtistManager;
import org.jajuk.base.File;
import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Year;
import org.jajuk.base.YearManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.log.Log;

/**
 * HTML popup displayed over a thumbnail, it details album informations and
 * tracks
 * <p>
 * It is displayed nicely from provided jlabel position
 * </p>
 * <p>
 * We use a JWindow instead of a JDialog because the painting is faster
 * </p>.
 */
public class ThumbnailPopup extends JWindow {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -8131528719972829954L;

  /** DOCUMENT_ME. */
  private final JEditorPane text;

  /** DOCUMENT_ME. */
  private KeyEventDispatcher dispatcher = null;

  /**
   * Launch selection and set right cursor.
   * 
   * @param tracks DOCUMENT_ME
   */
  private void launchLink(List<Track> tracks) {
    List<org.jajuk.base.File> toPlay = new ArrayList<org.jajuk.base.File>(1);
    for (Track track : tracks) {
      File file = track.getBestFile(true);
      if (file != null) {
        toPlay.add(file);
      }
    }
    text.setCursor(UtilGUI.WAIT_CURSOR);
    QueueModel.push(UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(toPlay), Conf
        .getBoolean(Const.CONF_STATE_REPEAT_ALL), true), Conf
        .getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
    // Change icon cursor and wait a while so user can see it in case
    // the PUSH_ON_CLICK option is set, otherwise, user may think
    // nothing appened.
    try {
      Thread.sleep(250);
    } catch (InterruptedException e1) {
      Log.error(e1);
    }
    text.setCursor(UtilGUI.LINK_CURSOR);
  }

  /**
   * The Constructor.
   * 
   * @param description HTML text to display (HTML 3.0)
   * @param origin :
   * coordinates of the origin item on whish we want to display the
   * popup
   * @param autoclose :
   * whether the popup should close when mouse leave the origin item or
   * is displayed as a regular Dialog
   */
  public ThumbnailPopup(String description, Rectangle origin, boolean autoclose) {
    getRootPane().setOpaque(true);
    text = new JEditorPane("text/html", description);
    text.setEditable(false);
    text.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    text.setFont(FontManager.getInstance().getFont(JajukFont.DEFAULT));
    text.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == EventType.ACTIVATED) {
          URL url = e.getURL();
          if (Const.XML_ARTIST.equals(url.getHost())) {
            Artist artist = (Artist) ArtistManager.getInstance().getItemByID(url.getQuery());
            List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(artist, false);
            Collections.shuffle(tracks);
            launchLink(tracks);
          } else if (Const.XML_GENRE.equals(url.getHost())) {
            Genre genre = (Genre) GenreManager.getInstance().getItemByID(url.getQuery());
            List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(genre, false);
            Collections.shuffle(tracks);
            launchLink(tracks);
          } else if (Const.XML_YEAR.equals(url.getHost())) {
            Year year = (Year) YearManager.getInstance().getItemByID(url.getQuery());
            List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(year, false);
            Collections.shuffle(tracks);
            launchLink(tracks);
          } else if (Const.XML_URL.equals(url.getHost())) {
            try {
              java.awt.Desktop.getDesktop().browse(new URI(url.getQuery()));
            } catch (Exception e1) {
              Log.error(e1);
            }
          } else if (Const.XML_TRACK.equals(url.getHost())) {
            List<Track> tracks = new ArrayList<Track>(1);
            Track track = (Track) TrackManager.getInstance().getItemByID(url.getQuery());
            tracks.add(track);
            launchLink(tracks);
          }
        }
        // change cursor on entering or leaving
        // hyperlinks
        else if (e.getEventType() == EventType.ENTERED) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              text.setCursor(UtilGUI.LINK_CURSOR);
            }
          });
        } else if (e.getEventType() == EventType.EXITED) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              text.setCursor(UtilGUI.DEFAULT_CURSOR);
            }
          });
        }

      }
    });
    final JScrollPane jspText = new JScrollPane(text);
    add(jspText);
    if (autoclose) {
      // Make sure to close this popup when it lost focus
      text.addMouseListener(new MouseAdapter() {
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
        y = (int) origin.getY() + (int) (0.4 * origin.getHeight()) - 350;
      }
      setLocation(x, y);
    } else {
      setLocationByPlatform(true);
    }
    setSize(500, 400);
    setVisible(true);
    // Force scrollbar to stay on top
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        jspText.getVerticalScrollBar().setValue(0);
      }
    });
    setKeystrokes();
  }

  /**
   * Add keystroke to dispose the popup when escape is pressed For unknown
   * reasons, registerKeyboardAction() against JWindow doesn't work (it does for
   * JFrame) but we need to use JWindow for performance reasons. for that
   * reason, we add a keyboard focus manager which is called before any focus
   * consideration
   * 
   * Note that for a JFrame, we would use
   * rootPane.registerKeyboardAction(actionListener, stroke,
   * JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
   */
  private void setKeystrokes() {
    removeKeystrokes();

    dispatcher = new KeyEventDispatcher() {
      @Override
      public boolean dispatchKeyEvent(KeyEvent e) {
        dispose();
        return false;
      }
    };

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
  }

  /**
   * Removes the keystrokes.
   * DOCUMENT_ME
   */
  private void removeKeystrokes() {
    if (dispatcher != null) {
      KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
      dispatcher = null;
    }
  }

  /* (non-Javadoc)
   * @see java.awt.Window#dispose()
   */
  @Override
  public void dispose() {
    removeKeystrokes();
    super.dispose();
  }
}
