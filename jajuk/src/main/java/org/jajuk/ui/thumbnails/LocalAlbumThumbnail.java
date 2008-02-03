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

import com.vlsolutions.swing.docking.ShadowBorder;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator;
import org.jajuk.base.TrackManager;
import org.jajuk.ui.helpers.CatalogViewTransferHandler;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.VerticalLayout;
import org.jvnet.substance.SubstanceLookAndFeel;

/**
 * Album thumb represented as album cover + (optionally) others text information
 * and some features like dnd, menu item to play, search cover, album popup
 * display...
 */
public class LocalAlbumThumbnail extends AbstractThumbnail {

  private static final long serialVersionUID = -282669695411453802L;

  /** Associated album */
  Album album;

  /** No cover flag */
  boolean bNoCover = false;

  JTextArea jlAuthor;

  JTextArea jlAlbum;

  private boolean bShowFullText;

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
    this.fCover = Util.getConfFileByPath(FILE_THUMBS + '/' + size + 'x' + size + '/'
        + album.getID() + '.' + EXT_THUMB);
  }

  public void populate() {
    // create the thumbnail if it doesn't exist
    ThumbnailManager.refreshThumbnail(album, size + "x" + size);
    if (!fCover.exists() || fCover.length() == 0) {
      bNoCover = true;
      this.fCover = null;
    }
    double[][] dMain = null;
    jlIcon = new JLabel();
    ImageIcon ii = album.getThumbnail(size + "x" + size);
    if (!bNoCover) {
      jlIcon.setBorder(new ShadowBorder());
      ii.getImage().flush(); // flush image buffer to avoid JRE to
      // use old image
    }
    jlIcon.setIcon(ii);
    if (bShowFullText) {
      dMain = new double[][] { { TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL },
          { size + 10, 10, TableLayout.PREFERRED, 5, TableLayout.PREFERRED } };
      setLayout(new TableLayout(dMain));
      int iRows = 7 + 3 * (size / 50 - 1);
      Color mediumGray = new Color(172, 172, 172);

      Author author = AuthorManager.getInstance().getAssociatedAuthors(album).iterator().next();
      jlAuthor = new JTextArea(author.getName2(), 1, iRows);
      jlAuthor.setLineWrap(true);
      jlAuthor.setWrapStyleWord(true);
      jlAuthor.setEditable(false);
      jlAuthor.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      jlAuthor.setForeground(mediumGray);
      // we have to use a empty border to avoid getting default border
      jlAuthor.setBorder(new EmptyBorder(0, 0, 0, 0));

      jlAlbum = new JTextArea(album.getName2(), 1, iRows);
      jlAlbum.setLineWrap(true);
      jlAlbum.setWrapStyleWord(true);
      jlAlbum.setEditable(false);

      jlAuthor.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      jlAlbum.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      jlAlbum.setForeground(mediumGray);
      jlAlbum.setBorder(new EmptyBorder(0, 0, 0, 0));
      add(jlIcon, "1,0,c,c");
      add(jlAuthor, "1,2");
      add(jlAlbum, "1,4");
    } else {
      setLayout(new VerticalLayout(2));
      add(Util.getCentredPanel(jlIcon));
      JLabel jlTitle = new JLabel(Util.getLimitedString(album.getName2(), 15));
      jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      jlTitle.setToolTipText(album.getName2());
      add(jlTitle);
    }
    // Add dnd support
    jlIcon.setTransferHandler(new CatalogViewTransferHandler(this));
    postPopulate();
    // disable inadequate menu items
    jmenu.remove(jmiOpenLastFMSite);
  }

  public boolean isNoCover() {
    return bNoCover;
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
    String size = "200x200";
    ThumbnailManager.refreshThumbnail(album, size);
    java.io.File cover = Util.getConfFileByPath(FILE_THUMBS + '/' + size + '/' + album.getID()
        + '.' + EXT_THUMB);
    List<Track> tracks = new ArrayList<Track>(TrackManager.getInstance().getAssociatedTracks(album));
    Collections.sort(tracks, new TrackComparator(TrackComparator.ORDER));
    Track firstTrack = tracks.iterator().next();
    Color bgcolor = SubstanceLookAndFeel.getActiveColorScheme().getUltraLightColor();
    Color fgcolor = SubstanceLookAndFeel.getActiveColorScheme().getForegroundColor();
    String sOut = "<html bgcolor='#" + Util.getHTMLColor(bgcolor) + "'><TABLE color='"
        + Util.getHTMLColor(fgcolor) + "'><TR><TD VALIGN='TOP'> <b>" + album.getName2()
        + "</b><br><br>";
    // display cover if available
    if (cover.canRead()) {
      sOut += "<img src='file:" + cover.getAbsolutePath() + "'><br>";
    }
    // Display author as global value only if it is a single author album
    // We use file://<item type>?<item id> as HTML hyperlink format
    if (album.getAuthor() != null) {
      sOut += "<br>" + Messages.getString("Property_author") + ": <a href='file://" + XML_AUTHOR
          + '?' + firstTrack.getAuthor().getID() + "'>" + firstTrack.getAuthor().getName2()
          + "</a>";
    }
    // Display style
    if (album.getStyle() != null) {
      sOut += "<br>" + Messages.getString("Property_style") + ": <a href='file://" + XML_STYLE
          + '?' + firstTrack.getStyle().getID() + "'>" + firstTrack.getStyle().getName2() + "</a>";
    }
    // Display year
    if (album.getYear() != null) {
      sOut += "<br>" + Messages.getString("Property_year") + ": <a href='file://" + XML_YEAR + '?'
          + firstTrack.getYear().getID() + "'>" + firstTrack.getYear().getName() + "</a>";
    }
    // display rating (average of each track rating)
    try {
      long lRate = album.getRate();
      sOut += "<br>"
          + Messages.getString("Property_rate")
          + ": <img src='"
          + Util.getConfFileByPath(
              "cache/internal/star" + Util.getAlbumStarsNumber(lRate) + "_16x16.png").toURL()
              .toExternalForm() + "'>";
    } catch (MalformedURLException e) {
      Log.error(e);
    }
    // Compute total length in secs
    long length = album.getDuration();
    sOut += "<br>" + Messages.getString("Property_length") + ": "
        + Util.formatTimeBySec(length, false) + "</TD><TD VALIGN='TOP'><br>";

    // Show each track detail
    for (Track track : tracks) {
      sOut += "<br>";
      if (track.getOrder() > 0) {
        sOut += Util.padNumber(track.getOrder(), 2) + ": ";
      }
      sOut += "<b>" + "<a href='file://" + XML_TRACK + '?' + track.getID() + "'>" + track.getName()
          + "</a>" + " (";
      sOut += Util.formatTimeBySec(track.getDuration(), false) + ") </b>";
      if (album.getYear() == null && track.getYear().getValue() != 0) {
        sOut += " - " + track.getYear().getValue() + "   ";
      }
      // Show author if known and if it is not already shown at album
      // level
      if (album.getAuthor() == null
          && !track.getAuthor().getName2().equals(Messages.getString(UNKNOWN_AUTHOR))) {
        sOut += " - " + track.getAuthor().getName2() + "   ";
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

}
