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

import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Album;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator;
import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.base.TrackManager;
import org.jajuk.services.core.SessionService;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.CatalogViewTransferHandler;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.helpers.PreferencesJMenu;
import org.jajuk.ui.helpers.StarsHelper;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Album thumb represented as album cover + (optionally) others text information
 * and some features like dnd, menu item to play, search cover, album popup
 * display...
 */
public class LocalAlbumThumbnail extends AbstractThumbnail {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -282669695411453802L;
  /** Associated album. */
  private Album album;
  private JLabel jlArtist;
  private JLabel jlAlbum;
  private final boolean bShowFullText;
  private PreferencesJMenu pjmFiles;

  /**
   * Constructor.
   * 
   * @param album :
   * associated album
   * @param size :
   * size of the thumbnail
   * @param bShowText :
   * Display full album / artist information under the icon or not ?
   */
  public LocalAlbumThumbnail(Album album, int size, boolean bShowText) {
    super(size);
    this.album = album;
    this.bShowFullText = bShowText;
    this.fCover = ThumbnailManager.getThumbBySize(album, size);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#populate()
   */
  @Override
  public synchronized void populate() {
    if (!album.isThumbAvailable(size)) {
      // create the thumbnail if it doesn't exist
      boolean thumbCreated = ThumbnailManager.refreshThumbnail(album, size);
      if (!thumbCreated) {
        this.fCover = null;
      }
    }
    ImageIcon ii = album.getThumbnail(size);
    jlIcon = new JLabel(ii);
    if (fCover != null) {
      jlIcon.setBorder(new ShadowBorder(false));
    }
    if (bShowFullText) {
      int iRows = 7 + 7 * ((size / 50) - 1);
      String artistName = album.getArtistOrALbumArtist();
      jlArtist = new JLabel(UtilString.getLimitedString(artistName, iRows));
      jlArtist.setToolTipText(artistName);
      jlArtist.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      // we have to use a empty border to avoid getting default border
      jlArtist.setBorder(new EmptyBorder(0, 0, 0, 0));
      jlAlbum = new JLabel(UtilString.getLimitedString(album.getName2(), iRows));
      jlAlbum.setToolTipText(album.getName2());
      jlAlbum.setBorder(new EmptyBorder(0, 0, 0, 0));
      jlIcon.setToolTipText(artistName + "/" + album.getName2());
      // Add items
      setLayout(new MigLayout("ins 0", "[grow]", "[" + (size + 10) + "!][grow][grow]"));
      add(jlIcon, "wrap,center");
      add(jlArtist, "wrap,center");
      add(jlAlbum, "wrap,center");
    } else {
      setLayout(new MigLayout("ins 0,gapy 2"));
      add(jlIcon, "center,wrap");
      int iRows = 7 + 6 * (size / 50 - 1);
      String fullTitle = album.getName2();
      JLabel jlTitle = new JLabel(UtilString.getLimitedString(fullTitle, iRows));
      jlTitle.setToolTipText(fullTitle);
      jlTitle.setToolTipText(fullTitle);
      add(jlTitle, "left");
    }
    // Add dnd support
    jlIcon.setTransferHandler(new CatalogViewTransferHandler(this));
    postPopulate();
    // Add the preference menu in popup
    pjmFiles = new PreferencesJMenu(getItem());
    jmenu.add(pjmFiles, 9);
    // disable inadequate menu items
    if (UtilSystem.isBrowserSupported()) {
      jmenu.remove(jmiOpenLastFMSite);
    }
    // Set keystrokes
    setKeystrokes();
  }

  /**
   * Gets the cover file.
   * 
   * @return the cover file
   */
  public File getCoverFile() {
    return fCover;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#getItem()
   */
  @Override
  public Item getItem() {
    return album;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#getDescription()
   */
  @Override
  public String getDescription() {
    int lSize = 200;
    ThumbnailManager.refreshThumbnail(album, lSize);
    java.io.File cover = ThumbnailManager.getThumbBySize(album, lSize);
    List<Track> tracks = new ArrayList<Track>(TrackManager.getInstance().getAssociatedTracks(album,
        true));
    Collections.sort(tracks, new TrackComparator(TrackComparatorType.ORDER));
    Track firstTrack = tracks.iterator().next();
    Color bgcolor = UtilGUI.getUltraLightColor();
    Color fgcolor = UtilGUI.getForegroundColor();
    String sOut = "<html bgcolor='#" + UtilGUI.getHTMLColor(bgcolor) + "'> <b>" + album.getName2()
        + "</b><br><TABLE color='" + UtilGUI.getHTMLColor(fgcolor) + "'><TR><TD VALIGN='TOP'>";
    // display cover
    if (cover.exists()) {
      sOut += "<img src='file:" + cover.getAbsolutePath() + "'><br>";
    }
    // TODO : add AlbumArtist value and hyperlink here
    // Display artist as global value only if it is a single artist album
    // We use file://<item type>?<item id> as HTML hyperlink format
    if (album.getArtist() != null) {
      sOut += "<br>" + Messages.getHumanPropertyName(Const.XML_ARTIST) + ": <a href='file://"
          + Const.XML_ARTIST + '?' + firstTrack.getArtist().getID() + "'>"
          + firstTrack.getArtist().getName2() + "</a>";
    }
    // Display genre
    if (album.getGenre() != null) {
      sOut += "<br>" + Messages.getHumanPropertyName(Const.XML_GENRE) + ": <a href='file://"
          + Const.XML_GENRE + '?' + firstTrack.getGenre().getID() + "'>"
          + UtilString.getLimitedString(firstTrack.getGenre().getName2(), 20) + "</a>";
    }
    // Display year
    if (album.getYear() != null) {
      sOut += "<br>" + Messages.getHumanPropertyName(Const.XML_YEAR) + ": <a href='file://"
          + Const.XML_YEAR + '?' + firstTrack.getYear().getID() + "'>"
          + firstTrack.getYear().getName() + "</a>";
    }
    // display rating (sum of all tracks rating)
    try {
      sOut += "<br>"
          + Messages.getHumanPropertyName(Const.XML_TRACK_RATE)
          + ": <img src='"
          + SessionService
              .getConfFileByPath(
                  "cache/internal/star" + StarsHelper.getStarsNumber(album) + "_16x16.png").toURI()
              .toURL().toExternalForm() + "'> (" + album.getRate() + ")";
    } catch (MalformedURLException e) {
      Log.error(e);
    }
    // Compute total length in secs
    long length = album.getDuration();
    sOut += "<br>" + Messages.getHumanPropertyName(Const.XML_TRACK_LENGTH) + ": "
        + UtilString.formatTimeBySec(length) + "</TD><TD VALIGN='TOP'><br>";
    // Show each track detail
    for (Track track : tracks) {
      sOut += "<br>";
      if (track.getOrder() > 0) {
        sOut += UtilString.padNumber(track.getOrder(), 2) + ": ";
      }
      sOut += "<b>" + "<a href='file://" + Const.XML_TRACK + '?' + track.getID() + "'>"
          + UtilString.getLimitedString(track.getName(), 50) + "</a>" + " (";
      sOut += UtilString.formatTimeBySec(track.getDuration()) + ") </b>";
      if (album.getYear() == null && track.getYear().getValue() != 0) {
        sOut += " - " + track.getYear().getValue() + "   ";
      }
      // Show artist if known and if it is not already shown at album
      // level
      if (album.getArtist() == null
          && !track.getArtist().getName2().equals(Messages.getString(Const.UNKNOWN_ARTIST))) {
        sOut += " - " + UtilString.getLimitedString(track.getArtist().getName2(), 20) + "   ";
      }
    }
    sOut += "</TD></TR></TABLE></html>";
    return sOut;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#launch()
   */
  @Override
  public void launch() {
    // play the album
    if (Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK)) {
      jmiPush.doClick();
    } else {
      jmiPlay.doClick();
    }
  }

  /**
   * Add keystroke support on the tree.
   */
  private void setKeystrokes() {
    putClientProperty(Const.DETAIL_SELECTION, album);
    InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap actionMap = getActionMap();
    // Delete
    Action action = ActionManager.getAction(JajukActions.DELETE);
    inputMap.put(KeyStroke.getKeyStroke("DELETE"), "delete");
    actionMap.put("delete", action);
    // Properties ALT/ENTER
    action = ActionManager.getAction(JajukActions.SHOW_PROPERTIES);
    inputMap.put(KeyStroke.getKeyStroke("alt ENTER"), "properties");
    actionMap.put("properties", action);
  }
}
