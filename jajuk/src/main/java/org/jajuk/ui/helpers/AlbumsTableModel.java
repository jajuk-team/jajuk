/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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

package org.jajuk.ui.helpers;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Artist;
import org.jajuk.base.Genre;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Year;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Filter;
import org.jajuk.util.Messages;

/**
 * Table model used for albums table view.
 */
public class AlbumsTableModel extends JajukTableModel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Model constructor.
   */
  public AlbumsTableModel() {
    super(11);
    setEditable(Conf.getBoolean(Const.CONF_ALBUMS_TABLE_EDITION));
    // Columns names
    // First column is play icon, need to set a space character
    // for proper display in some look and feel
    vColNames.add(" ");
    idList.add(Const.XML_PLAY);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_ALBUM));
    idList.add(Const.XML_ALBUM);

    // First track found artist. If different artists in album, will be
    // displayed in italic
    vColNames.add(Messages.getHumanPropertyName(Const.XML_ARTIST));
    idList.add(Const.XML_ARTIST);

    // First track found genre. If different genres in album, will be
    // displayed in italic
    vColNames.add(Messages.getHumanPropertyName(Const.XML_GENRE));
    idList.add(Const.XML_GENRE);

    // First found track year, italic if different values
    vColNames.add(Messages.getHumanPropertyName(Const.XML_YEAR));
    idList.add(Const.XML_YEAR);

    // Album rate (average of its tracks rate)
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_RATE));
    idList.add(Const.XML_TRACK_RATE);

    // Total album length
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_LENGTH));
    idList.add(Const.XML_TRACK_LENGTH);

    // Number of tracks
    vColNames.add(Messages.getString("AlbumsTableView.1"));
    idList.add(Const.XML_TRACKS);

    // First found track discovery date
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_DISCOVERY_DATE));
    idList.add(Const.XML_TRACK_DISCOVERY_DATE);

    // Sum of all tracks hits
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_HITS));
    idList.add(Const.XML_TRACK_HITS);

    // Disc ID
    vColNames.add(Messages.getHumanPropertyName(Const.XML_ALBUM_DISC_ID));
    idList.add(Const.XML_ALBUM_DISC_ID);

    // custom properties now
    for (PropertyMetaInformation meta : AlbumManager.getInstance().getCustomProperties()) {
      vColNames.add(meta.getName());
      idList.add(meta.getName());
    }
  }

  /**
   * Fill model with data using an optional filter property and pattern
   * <p>
   * For now, this table will not be editable (except for custom properties) for
   * complexity reasons. This may be implemented in the future if required
   * </p>
   * 
   * @param sPropertyName DOCUMENT_ME
   * @param sPattern DOCUMENT_ME
   * @param columnsToShow DOCUMENT_ME
   */
  @Override
  @SuppressWarnings("unchecked")
  public void populateModel(String sPropertyName, String sPattern, List<String> columnsToShow) {
    List<Album> alToShow = AlbumManager.getInstance().getAlbums();
    // OK, begin by filtering using any provided pattern
    Filter filter = new Filter(sPropertyName, sPattern, true, Conf.getBoolean(Const.CONF_REGEXP));
    Filter.filterItems(alToShow, filter);

    // Filter unmounted files if required
    if (Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)) {
      Iterator<Album> it = alToShow.iterator();
      while (it.hasNext()) {
        Album album = it.next();
        if (!album.containsReadyFiles()) {
          it.remove();
        }
      }
    }

    // Sort the result
    int iColNum = iNumberStandardCols + AlbumManager.getInstance().getCustomProperties().size();
    iRowNum = alToShow.size();
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];
    // Allow only custom properties edition
    bEditable = true;
    Iterator<Album> it = alToShow.iterator();

    // For perfs, prepare columns visibility
    boolean bAlbum = (columnsToShow != null && columnsToShow.contains(Const.XML_ALBUM));
    boolean bArtist = (columnsToShow != null && columnsToShow.contains(Const.XML_ARTIST));
    boolean bGenre = (columnsToShow != null && columnsToShow.contains(Const.XML_GENRE));
    boolean bYear = (columnsToShow != null && columnsToShow.contains(Const.XML_YEAR));
    boolean bRate = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_RATE));
    boolean bLength = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_LENGTH));
    boolean bTrackNb = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACKS));
    boolean bDiscovery = (columnsToShow != null && columnsToShow
        .contains(Const.XML_TRACK_DISCOVERY_DATE));
    boolean bHits = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_HITS));
    boolean bAlbumDiscID = (columnsToShow != null && columnsToShow
        .contains(Const.XML_ALBUM_DISC_ID));

    for (int iRow = 0; it.hasNext(); iRow++) {
      Album album = it.next();
      setItemAt(iRow, album);
      Map<String, Object> properties = album.getProperties();
      // Id
      oItems[iRow] = album;
      // Play
      IconLabel il = null;
      if (album.containsReadyFiles()) {
        il = getIcon(false);
      } else {
        il = getIcon(true);
      }
      // Note: if you want to add an image, use an ImageIcon class and
      // change
      oValues[iRow][0] = il;
      bCellEditable[iRow][0] = false;

      // Album name
      if (bAlbum) {
        oValues[iRow][1] = album.getName2();
      } else {
        oValues[iRow][1] = "";
      }
      bCellEditable[iRow][1] = false;

      // Artist
      if (bArtist) {
        Artist artist = album.getArtist();
        if (artist != null) {
          oValues[iRow][2] = artist.getName2();
        } else {
          oValues[iRow][2] = "";
        }
      } else {
        oValues[iRow][2] = "";
      }
      bCellEditable[iRow][2] = false;

      // Genre
      if (bGenre) {
        Genre genre = album.getGenre();
        if (genre != null) {
          oValues[iRow][3] = genre.getName2();
        } else {
          oValues[iRow][3] = "";
        }
      } else {
        oValues[iRow][3] = "";
      }
      bCellEditable[iRow][3] = false;

      // Year
      if (bYear) {
        Year year = album.getYear();
        if (year != null) {
          oValues[iRow][4] = year.getValue();
        } else {
          oValues[iRow][4] = 0l;
        }
      } else {
        oValues[iRow][4] = "";
      }
      bCellEditable[iRow][4] = false;

      // Rate
      if (bRate) {
        oValues[iRow][5] = StarsHelper.getStarIconLabel(album);
      } else {
        oValues[iRow][5] = "";
      }
      bCellEditable[iRow][5] = false;

      // Length
      if (bLength) {
        oValues[iRow][6] = new Duration(album.getDuration());
      } else {
        oValues[iRow][6] = "";
      }
      bCellEditable[iRow][6] = false;

      // Number of tracks
      if (bTrackNb) {
        oValues[iRow][7] = album.getNbOfTracks();
      } else {
        oValues[iRow][7] = "";
      }
      bCellEditable[iRow][7] = false;

      // Date discovery
      if (bDiscovery) {
        oValues[iRow][8] = album.getDiscoveryDate();
      } else {
        oValues[iRow][8] = "";
      }
      bCellEditable[iRow][8] = false;

      // Hits
      if (bHits) {
        oValues[iRow][9] = album.getHits();
      } else {
        oValues[iRow][9] = "";
      }
      bCellEditable[iRow][9] = false;

      // disc id
      if (bAlbumDiscID) {
        oValues[iRow][10] = Long.toHexString(album.getDiscID());
      } else {
        oValues[iRow][10] = "";
      }
      bCellEditable[iRow][10] = false;

      // Custom properties now
      Iterator it2 = AlbumManager.getInstance().getCustomProperties().iterator();
      for (int i = 0; it2.hasNext(); i++) {
        PropertyMetaInformation meta = (PropertyMetaInformation) it2.next();
        Object o = properties.get(meta.getName());
        if (o != null) {
          oValues[iRow][iNumberStandardCols + i] = o;
        } else {
          oValues[iRow][iNumberStandardCols + i] = meta.getDefaultValue();
        }
        // Date values not editable, use properties panel instead to
        // edit
        bCellEditable[iRow][iNumberStandardCols + i] = !(meta.getType().equals(Date.class));
      }
    }
  }
}