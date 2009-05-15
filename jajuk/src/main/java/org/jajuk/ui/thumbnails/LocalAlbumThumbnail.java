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
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator;
import org.jajuk.base.TrackManager;
import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.services.core.SessionService;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.CatalogViewTransferHandler;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.PreferencesJMenu;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.border.DropShadowBorder;

/**
 * Album thumb represented as album cover + (optionally) others text information
 * and some features like dnd, menu item to play, search cover, album popup
 * display...
 */
public class LocalAlbumThumbnail extends AbstractThumbnail {

  private static final long serialVersionUID = -282669695411453802L;

  /** Associated album */
  Album album;

  JLabel jlAuthor;

  JLabel jlAlbum;

  private final boolean bShowFullText;

  private PreferencesJMenu pjmFiles;

  /**
   * Constructor
   * 
   * @param album :
   *          associated album
   * @param size :
   *          size of the thumbnail
   * @param bShowText:
   *          Display full album / author information under the icon or not ?
   */
  public LocalAlbumThumbnail(Album album, int size, boolean bShowText) {
    super(size);
    this.album = album;
    this.bShowFullText = bShowText;
    this.fCover = ThumbnailManager.getThumbBySize(album, size);
  }

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
      jlIcon.setBorder(new DropShadowBorder(Color.BLACK, 5, 0.5f, 5, false, true, false, true));
    }
    if (bShowFullText) {
      int iRows = 7 + 7 * ((size / 50) - 1);

      Author author = AuthorManager.getInstance().getAssociatedAuthors(album).iterator().next();
      jlAuthor = new JLabel(UtilString.getLimitedString(author.getName2(), iRows));
      jlAuthor.setToolTipText(author.getName2());
      jlAuthor.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));

      // we have to use a empty border to avoid getting default border
      jlAuthor.setBorder(new EmptyBorder(0, 0, 0, 0));
      jlAlbum = new JLabel(UtilString.getLimitedString(album.getName2(), iRows));
      jlAlbum.setToolTipText(album.getName2());
      jlAlbum.setBorder(new EmptyBorder(0, 0, 0, 0));
      
      // Add items
      setLayout(new MigLayout("", "[grow]", "[" + (size + 10) + "!][grow][grow]"));
      add(jlIcon, "wrap,center");
      add(jlAuthor, "wrap,center");
      add(jlAlbum, "wrap,center");
    } else {
      setLayout(new VerticalLayout(2));
      add(UtilGUI.getCentredPanel(jlIcon));
      int iRows = 7 + 6 * (size / 50 - 1);
      JLabel jlTitle = new JLabel(UtilString.getLimitedString(album.getName2(), iRows));
      jlTitle.setToolTipText(album.getName2());
      jlTitle.setToolTipText(album.getName2());
      add(jlTitle);
    }
    // Add dnd support
    jlIcon.setTransferHandler(new CatalogViewTransferHandler(this));
    postPopulate();
    // Add the preference menu in popup
    pjmFiles = new PreferencesJMenu(getItem());
    jmenu.add(pjmFiles, 9);
    // disable inadequate menu items
    jmenu.remove(jmiOpenLastFMSite);
    // Set keystrokes
    setKeystrokes();
  }

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
    String sOut = "<html bgcolor='#" + UtilGUI.getHTMLColor(bgcolor) + "'><TABLE color='"
        + UtilGUI.getHTMLColor(fgcolor) + "'><TR><TD VALIGN='TOP'> <b>" + album.getName2()
        + "</b><br><br>";
    // display cover
    if (cover.exists()) {
      sOut += "<img src='file:" + cover.getAbsolutePath() + "'><br>";
    }
    // Display author as global value only if it is a single author album
    // We use file://<item type>?<item id> as HTML hyperlink format
    if (album.getAuthor() != null) {
      sOut += "<br>" + Messages.getString("Property_author") + ": <a href='file://"
          + Const.XML_AUTHOR + '?' + firstTrack.getAuthor().getID() + "'>"
          + firstTrack.getAuthor().getName2() + "</a>";
    }
    // Display style
    if (album.getStyle() != null) {
      sOut += "<br>" + Messages.getString("Property_style") + ": <a href='file://"
          + Const.XML_STYLE + '?' + firstTrack.getStyle().getID() + "'>"
          + UtilString.getLimitedString(firstTrack.getStyle().getName2(), 20) + "</a>";
    }
    // Display year
    if (album.getYear() != null) {
      sOut += "<br>" + Messages.getString("Property_year") + ": <a href='file://" + Const.XML_YEAR
          + '?' + firstTrack.getYear().getID() + "'>" + firstTrack.getYear().getName() + "</a>";
    }
    // display rating (sum of all tracks rating)
    try {
      sOut += "<br>"
          + Messages.getString("Property_rate")
          + ": <img src='"
          + SessionService.getConfFileByPath(
              "cache/internal/star" + album.getStarsNumber() + "_16x16.png").toURI().toURL()
              .toExternalForm() + "'> (" + album.getRate() + ")";
    } catch (MalformedURLException e) {
      Log.error(e);
    }
    // Compute total length in secs
    long length = album.getDuration();
    sOut += "<br>" + Messages.getString("Property_length") + ": "
        + UtilString.formatTimeBySec(length) + "</TD><TD VALIGN='TOP'><br>";

    // Show each track detail
    for (Track track : tracks) {
      sOut += "<br>";
      if (track.getOrder() > 0) {
        sOut += UtilString.padNumber(track.getOrder(), 2) + ": ";
      }
      sOut += "<b>" + "<a href='file://" + Const.XML_TRACK + '?' + track.getID() + "'>"
          + UtilString.getLimitedString(track.getName(), 30) + "</a>" + " (";
      sOut += UtilString.formatTimeBySec(track.getDuration()) + ") </b>";
      if (album.getYear() == null && track.getYear().getValue() != 0) {
        sOut += " - " + track.getYear().getValue() + "   ";
      }
      // Show author if known and if it is not already shown at album
      // level
      if (album.getAuthor() == null
          && !track.getAuthor().getName2().equals(Messages.getString(Const.UNKNOWN_AUTHOR))) {
        sOut += " - " + UtilString.getLimitedString(track.getAuthor().getName2(), 20) + "   ";
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
    jmiPlay.doClick();
  }

  /**
   * Add keystroke support on the tree
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
