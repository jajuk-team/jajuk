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
import net.miginfocom.swing.MigLayout;
import org.jajuk.base.*;
import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.services.core.SessionService;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.CatalogViewTransferHandler;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.helpers.PreferencesJMenu;
import org.jajuk.ui.helpers.StarsHelper;
import org.jajuk.util.*;
import org.jajuk.util.log.Log;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.Serial;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Album thumb represented as album cover + (optionally) others text information
 * and some features like dnd, menu item to play, search cover, album popup
 * display...
 */
public class LocalAlbumThumbnail extends AbstractThumbnail {
  /** Generated serialVersionUID. */
  @Serial
  private static final long serialVersionUID = -282669695411453802L;
  /** Associated album. */
  private final Album album;
  private final boolean bShowFullText;

  /**
   * Constructor.
   *
   * @param album     :
   *                  associated album
   * @param size      :
   *                  size of the thumbnail
   * @param bShowText :
   *                  Display full album / artist information under the icon or not ?
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
      JLabel jlArtist = new JLabel(UtilString.getLimitedString(artistName, iRows));
      jlArtist.setToolTipText(artistName);
      jlArtist.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      // we have to use a empty border to avoid getting default border
      jlArtist.setBorder(new EmptyBorder(0, 0, 0, 0));
      JLabel jlAlbum = new JLabel(UtilString.getLimitedString(album.getName2(), iRows));
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
    PreferencesJMenu pjmFiles = new PreferencesJMenu(getItem());
    jmenu.add(pjmFiles, 9);
    // disable inadequate menu items
    if (UtilSystem.isBrowserSupported()) {
      jmenu.remove(jmiOpenLastFMSite);
    }
    // Set keystrokes
    setKeystrokes();
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
    // Cache color values to avoid repeated method calls
    String bgColorHex = UtilGUI.getHTMLColor(UtilGUI.getUltraLightColor());
    String fgColorHex = UtilGUI.getHTMLColor(UtilGUI.getForegroundColor());

    int lSize = 200;
    ThumbnailManager.refreshThumbnail(album, lSize);
    java.io.File cover = ThumbnailManager.getThumbBySize(album, lSize);
    List<Track> tracks = new ArrayList<>(TrackManager.getInstance().getAssociatedTracks(album,
            true));
    tracks.sort(new TrackComparator(TrackComparatorType.ORDER));
    Track firstTrack = tracks.iterator().next();
    Color bgcolor = UtilGUI.getUltraLightColor();
    Color fgcolor = UtilGUI.getForegroundColor();
    // Wrap description in a constrained container so very large covers don't make the popup huge
    StringBuilder html = new StringBuilder();
    html.append("<html>");
    html.append("<body style='background-color: #").append(bgColorHex).append("'>");
    html.append("<div style='width:400px; height:400px; margin:0 auto; border:1px solid #ccc;'>");

    html.append("<b>").append(album.getName2()).append("</b>");
    html.append("<br><table style='color:#").append(fgColorHex).append(";'>");
    html.append("<tr><td style='vertical-align: top;'>");

    // display cover
    if (cover.exists()) {
      html.append("<img src='file:")
              .append(cover.getAbsolutePath())
              .append("' width='300'/>") // Fixed width
              .append("<br>");
    }

    // Display artist as global value only if it is a single artist album
    // We use file://<item type>?<item id> as HTML hyperlink format
    if (album.getArtist() != null) {
      html.append("<br>").append(Messages.getHumanPropertyName(Const.XML_ARTIST)).append(": <a href='file://");
      html.append(Const.XML_ARTIST).append('?').append(firstTrack.getArtist().getID()).append("'>");
      html.append(firstTrack.getArtist().getName2()).append("</a>");
    }
    // Display genre
    if (album.getGenre() != null) {
      html.append("<br>").append(Messages.getHumanPropertyName(Const.XML_GENRE)).append(": <a href='file://");
      html.append(Const.XML_GENRE).append('?').append(firstTrack.getGenre().getID()).append("'>");
      html.append(UtilString.getLimitedString(firstTrack.getGenre().getName2(), 20)).append("</a>");
    }
    // Display year
    if (album.getYear() != null) {
      html.append("<br>").append(Messages.getHumanPropertyName(Const.XML_YEAR)).append(": <a href='file://");
      html.append(Const.XML_YEAR).append('?').append(firstTrack.getYear().getID()).append("'>");
      html.append(firstTrack.getYear().getName()).append("</a>");
    }
    // display rating (sum of all tracks rating)
    try {
      html.append("<br>").append(Messages.getHumanPropertyName(Const.XML_TRACK_RATE));
      html.append(": <img src='");
      html.append(SessionService.getConfFileByPath(
                      "cache/internal/star" + StarsHelper.getStarsNumber(album) + "_16x16.png").toURI()
              .toURL().toExternalForm());
      html.append("' style='max-width:200px;height:auto;vertical-align:middle;'> (").append(album.getRate()).append(")");
    } catch (MalformedURLException e) {
      Log.error(e);
    }
    // Compute total length in secs
    long length = album.getDuration();
    html.append("<br>").append(Messages.getHumanPropertyName(Const.XML_TRACK_LENGTH)).append(": ");
    html.append(UtilString.formatTimeBySec(length)).append("</td>");
    html.append("<td style='vertical-align: top;'><br>");
    // Show each track detail
    for (Track track : tracks) {
      html.append("<br>");
      if (track.getOrder() > 0) {
        html.append(UtilString.padNumber(track.getOrder(), 2)).append(": ");
      }
      html.append("<b>").append("<a href='file://").append(Const.XML_TRACK).append('?').append(track.getID()).append("'>");
      html.append(UtilString.getLimitedString(track.getName(), 50)).append("</a>").append(" (");
      html.append(UtilString.formatTimeBySec(track.getDuration())).append(") </b>");
      if (album.getYear() == null && track.getYear().getValue() != 0) {
        html.append(" - ").append(track.getYear().getValue()).append("   ");
      }
      // Show artist if known and if it is not already shown at album
      // level
      if (album.getArtist() == null
              && !track.getArtist().getName2().equals(Messages.getString(Const.UNKNOWN_ARTIST))) {
        html.append(" - ").append(UtilString.getLimitedString(track.getArtist().getName2(), 20)).append("   ");
      }
    }
    html.append("</td></tr></table>");
    html.append("</div>");
    html.append("</body>");
    html.append("</html>");
    return html.toString();
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
