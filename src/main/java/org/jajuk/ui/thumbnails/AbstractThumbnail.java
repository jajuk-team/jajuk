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
package org.jajuk.ui.thumbnails;

import com.vlsolutions.swing.docking.ShadowBorder;
import org.apache.commons.lang3.StringUtils;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.views.CoverView;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.*;
import org.jajuk.util.log.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serial;
import java.net.URL;
import java.util.List;

/**
 * Album thumb represented as album cover + (optionally) others text information
 * and some features like dnd, menu item to play, search cover, album popup
 * display...
 */
public abstract class AbstractThumbnail extends JPanel implements ActionListener, Transferable {
  /** Generated serialVersionUID. */
  @Serial
  private static final long serialVersionUID = -6396225563540281695L;
  /** Size. */
  int size;
  protected JLabel jlIcon;
  private static long lDateLastMove;
  private static Point lastPosition;
  JPopupMenu jmenu;
  JMenuItem jmiPlay;
  JMenuItem jmiPush;
  JMenuItem jmiFrontPush;
  JMenuItem jmiDelete;
  JMenuItem jmiPlayShuffle;
  JMenuItem jmiPlayRepeat;
  JMenuItem jmiGetCovers;
  private JMenuItem jmiShowPopup;
  JMenuItem jmiCDDBWizard;
  JMenuItem jmiProperties;
  JMenuItem jmiOpenLastFMSite;
  /** Dragging flag used to disable simple click behavior. */
  private static boolean bDragging = false;
  /** Current details dialog. */
  private static ThumbnailPopup details;
  private static AbstractThumbnail last;
  private static AbstractThumbnail mouseOverItem = null;
  /** Whether this thumb is used in artist view *. */
  private boolean artistView;
  /** Associated file. */
  File fCover;
  /* Timer used to launch popup. */
  static {
    Timer timerPopup = new Timer(200, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        try {
          // Close popup ASAP when over none catalog item
          if (mouseOverItem == null) {
            if (details != null) {
              details.closeIfAutoclose();
              details = null;
            }
            last = null;
            // display a popup after n seconds only if item changed
          } else if ((System.currentTimeMillis() - lDateLastMove >= 700) && mouseOverItem != last
              && !bDragging) {
            // Store current item
            last = mouseOverItem;
            // Finally display the popup (Leave if user unselected
            // the option "Show catalog popups"
            if (Conf.getBoolean(Const.CONF_SHOW_POPUPS)) {
              mouseOverItem.displayPopup(true);
            }
          }
          bDragging = false;
        } catch (Exception e) {
          // Make sure not to exit
          Log.error(e);
        }
      }
    });
    timerPopup.start();
  }

  /**
   * Constructor.
   * 
   * @param size :
   * size of the thumbnail
   */
  protected AbstractThumbnail(int size) {
    this.size = size;
    setSelected(false);
  }

  /**
   * Checks if is artist view.
   * 
   * @return true, if is artist view
   */
  protected boolean isArtistView() {
    return artistView;
  }

  /**
   * Sets the artist view.
   * 
   * @param artistBioThumb the new artist view
   */
  public void setArtistView(boolean artistBioThumb) {
    this.artistView = artistBioThumb;
  }

  /**
   * Display a popup over the catalog item.
   * @param autoclose should the popup close by itself or stuck on the screen ?
   */
  private void displayPopup(boolean autoclose) {
    // close popup if any visible
    if (details != null) {
      details.closeIfAutoclose();
      details = null;
    }
    // don't show details if the contextual popup menu
    // is visible
    if (jmenu.isVisible()) {
      return;
    }
    String description = getDescription();
    if (description != null) {
      details = new ThumbnailPopup(description, new Rectangle(jlIcon.getLocationOnScreen(),
          new Dimension(jlIcon.getWidth(), jlIcon.getHeight())), autoclose);
    }
  }

  /**
   * Populate. 
   */
  public abstract void populate();

  /**
   * Return HTML text to display in the popup.
   * 
   * @return the description
   */
  public abstract String getDescription();

  /**
   * Performs common UI operations for any kind of thumb.
   */
  void postPopulate() {
    // do this only once as it might be a costly operation...
    Item item = getItem();
    // Album menu
    jmenu = new JPopupMenu();
    jmiPlay = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SELECTION));
    jmiPlay.putClientProperty(Const.DETAIL_SELECTION, item);
    jmiPush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_SELECTION));
    jmiPush.putClientProperty(Const.DETAIL_SELECTION, item);
    jmiFrontPush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_FRONT_SELECTION));
    jmiFrontPush.putClientProperty(Const.DETAIL_SELECTION, item);
    jmiDelete = new JMenuItem(ActionManager.getAction(JajukActions.DELETE));
    jmiDelete.putClientProperty(Const.DETAIL_SELECTION, item);
    jmiPlayShuffle = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SHUFFLE_SELECTION));
    jmiPlayShuffle.putClientProperty(Const.DETAIL_SELECTION, item);
    jmiPlayRepeat = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_REPEAT_SELECTION));
    jmiPlayRepeat.putClientProperty(Const.DETAIL_SELECTION, item);
    jmiGetCovers = new JMenuItem(Messages.getString("CatalogView.7"),
        IconLoader.getIcon(JajukIcons.COVER_16X16));
    jmiGetCovers.addActionListener(this);
    jmiShowPopup = new JMenuItem(Messages.getString("CatalogView.20"),
        IconLoader.getIcon(JajukIcons.POPUP));
    jmiShowPopup.addActionListener(this);
    jmiCDDBWizard = new JMenuItem(ActionManager.getAction(JajukActions.CDDB_SELECTION));
    jmiCDDBWizard.putClientProperty(Const.DETAIL_SELECTION, item);
    jmiProperties = new JMenuItem(ActionManager.getAction(JajukActions.SHOW_PROPERTIES));
    jmiProperties.putClientProperty(Const.DETAIL_SELECTION, item);
    if (UtilSystem.isBrowserSupported()) {
      JajukAction actionOpenLastFM = ActionManager.getAction(JajukActions.LAUNCH_IN_BROWSER);
      // Change action label
      jmiOpenLastFMSite = new JMenuItem(actionOpenLastFM);
      jmiOpenLastFMSite.setText(Messages.getString("AbstractThumbnail.0"));
      jmiOpenLastFMSite.setToolTipText(Messages.getString("AbstractThumbnail.0"));
    }
    // We add all menu items, each implementation of this class should hide
    // (setVisible(false)) menu items that are not available in their
    // context
    jmenu.add(jmiPlay);
    jmenu.add(jmiFrontPush);
    jmenu.add(jmiPush);
    jmenu.add(jmiPlayShuffle);
    jmenu.add(jmiPlayRepeat);
    jmenu.addSeparator();
    jmenu.add(jmiDelete);
    jmenu.addSeparator();
    jmenu.add(jmiCDDBWizard);
    jmenu.add(jmiGetCovers);
    jmenu.add(jmiShowPopup);
    if (UtilSystem.isBrowserSupported()) {
      jmenu.add(jmiOpenLastFMSite);
    }
    jmenu.addSeparator();
    jmenu.add(jmiProperties);
    if (jlIcon != null) {
      jlIcon.addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
          // Notify the mouse listener that we are dragging
          bDragging = true;
          JComponent c = (JComponent) e.getSource();
          TransferHandler handler = c.getTransferHandler();
          // handler may be null for thumbnails that don't support DnD
          if (handler != null) {
            handler.exportAsDrag(c, e, TransferHandler.COPY);
          } else {
            // No transfer handler: ignore drag action to avoid NPE
            Log.debug("No TransferHandler for component " + c + ", skipping exportAsDrag");
          }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
          super.mouseMoved(e);
          lDateLastMove = System.currentTimeMillis();
          lastPosition = e.getPoint();
        }
      });
      jlIcon.addMouseListener(new JajukMouseAdapter() {
        @Override
        public void handlePopup(MouseEvent e) {
          if (e.getSource() == jlIcon) {
            // Show contextual menu
            jmenu.show(jlIcon, e.getX(), e.getY());
            // Hide any details frame
            if (details != null) {
              details.dispose();
              details = null;
            }
          }
        }

        @Override
        public void handleActionSeveralClicks(MouseEvent e) {
          launch();
        }

        @Override
        public void mousePressed(MouseEvent e) {
          if (bDragging) {
            return;
          }
          super.mousePressed(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
          mouseOverItem = AbstractThumbnail.this;
        }

        @Override
        public void mouseExited(MouseEvent e) {
          // Consider an exit only if mouse really moved to avoid
          // closing popup when popup appears over the mouse cursor
          // (then, a mouseExit event is thrown)
          if (!e.getPoint().equals(lastPosition) &&
                  // Don't close popup if user is still over it
                  !(details != null && details.contains(e.getPoint()))) {
            mouseOverItem = null;
          }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          // Leave if already dragging
          if (bDragging) {
            return;
          }
          super.mouseReleased(e);
        }
      });
    }
  }

  /**
   * Sets the selected.
   * 
   * @param b is selected ?
   */
  public final void setSelected(boolean b) {
    requestFocusInWindow();
    // Add a shadow for selected items
    if (b) {
      setBorder(new ShadowBorder(false));
    } else {
      // add an empty border of the same size than the border to avoid
      // image moves when setting borders
      setBorder(BorderFactory.createEmptyBorder(1, 1, 5, 5));
    }
  }

  /**
   * Launch. 
   */
  public abstract void launch();

  /**
   * If the thumb represents something (album, artist...) known in the
   * collection, the implementation of this method should return the associated
   * item
   * 
   * @return the collection item
   */
  public abstract Item getItem();

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == jmiGetCovers) {
      // This item is enabled only for albums
      JDialog jd = new JDialog(JajukMainWindow.getInstance(), Messages.getString("CatalogView.18"));
      org.jajuk.base.File file = null;
      // We sort associated tracks because we want to analyze the first file of the set
      // as it is more likely to contain global cover tag.
      List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(getItem(), true);
      if (!tracks.isEmpty()) {
        // Take first track found
        Track track = tracks.iterator().next();
        file = track.getBestFile(false);
      }
      CoverView cv;
      if (file != null) {
        cv = new CoverView(file, jd);
        cv.setID("catalog/0");
        cv.initUI();
        jd.add(cv);
        jd.setSize(600, 450);
        // Keep it unresizable to keep things simple with cover view
        // resizing issues, see @CoverView.CoverResetThread comments.
        jd.setResizable(false);
        UtilGUI.centerWindow(jd);
        jd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jd.setVisible(true);
      } else {
        Messages.showErrorMessage(166);
      }
    } else if (e.getSource() == jmiShowPopup) {
      displayPopup(false);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
   */
  @Override
  public Object getTransferData(DataFlavor flavor) {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
   */
  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
   */
  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return false;
  }

  /**
   * Gets the icon.
   * 
   * @return the icon
   */
  public JLabel getIcon() {
    return jlIcon;
  }

  /**
   * Download a remote URL to cache and perform basic validation.
   * The cached file is stored in the protected field {@link #fCover}.
   *
   * @param remote remote URL to download
   * @return the cached File or null if unavailable or invalid
   */
  protected java.io.File downloadToCache(URL remote) {
    try {
      fCover = DownloadManager.downloadToCache(remote);
      if (fCover == null) {
        Log.warn("No cache file for {{" + remote.toString() + "}}");
        return null;
      }
      if (!fCover.exists()) {
        Log.warn("Cache file not found: {{" + fCover.getAbsolutePath() + "}}");
        return null;
      }
      if (fCover.length() == 0) {
        Log.warn("Cache file has zero bytes: {{" + fCover.getAbsolutePath() + "}}");
        return null;
      }
      return fCover;
    } catch (Exception e) {
      Log.warn("Could not read remote file: {{" + (remote != null ? remote.toString() : "null") + "}}", e.getMessage());
      return null;
    }
  }

  /**
   * Create a scaled ImageIcon from a remote URL. The method downloads the
   * remote resource to the cache (using {@link #downloadToCache(URL)}), reads
   * it as an image and returns a scaled icon using {@link UtilGUI#getScaledImage}.
   *
   * @param remote remote image URL
   * @param size desired icon size (passed to UtilGUI.getScaledImage)
   * @return scaled ImageIcon or null if unavailable
   */
  protected ImageIcon createScaledIconFromRemote(URL remote, int size) {
    if (remote == null) {
      return null;
    }
    try {
      java.io.File cache = downloadToCache(remote);
      if (cache == null) {
        return null;
      }
      BufferedImage image = ImageIO.read(cache);
      if (image == null) {
        Log.warn("Could not read image data in file: {{" + cache + "}}");
        return null;
      }
      return createScaledIcon(image, size);
    } catch (Exception e) {
      Log.warn("Could not load image from remote: {{" + remote + "}}", e.getMessage());
      return null;
    }
  }

  protected ImageIcon createScaledIcon(Image image, int size) {
    ImageIcon downloadedImage = new ImageIcon(image);
    ImageIcon scaled = UtilGUI.getScaledImage(downloadedImage, size);
    // Free images memory
    downloadedImage.getImage().flush();
    image.flush();
    return scaled;
  }

  /**
   * Escapes HTML special characters to prevent XSS vulnerabilities.
   *
   * @param input the input string to escape
   * @return escaped HTML string
   */
  protected String escapeHtml(String input) {
    if (StringUtils.isBlank(input)) {
      return "";
    }
    return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
  }

  /**
   * Helper method to create HTML hyperlinks in Last.FM format.
   *
   * @param text the display text
   * @param url  the Last.FM url (item id)
   * @return HTML link in format: &lt;a href='file://URL_SCHEME?url'&gt;text&lt;/a&gt;
   */
  protected String createLink(String text, String url) {
    return "<a href='file://" + Const.XML_URL + '?' + url + "'>" + escapeHtml(text) + "</a>";
    //return "<a href='" + url + "'>" + escapeHtml(text) + "</a>";
  }
}
