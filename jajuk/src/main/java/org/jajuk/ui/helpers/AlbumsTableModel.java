/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 * $Revision: 2921 $
 */

package org.jajuk.ui.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Author;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Style;
import org.jajuk.base.Year;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Filter;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

/**
 * Table model used for albums table view
 */
public class AlbumsTableModel extends JajukTableModel {

  private static final long serialVersionUID = 1L;

  /**
   * Model constructor
   * 
   * @param iColNum
   *          number of rows
   * @param sColName
   *          columns names
   */
  public AlbumsTableModel() {
    super(10);
    setEditable(ConfigurationManager.getBoolean(CONF_ALBUMS_TABLE_EDITION));
    // Columns names
    // First column is play icon, need to set a space character
    // for proper display in some look and feel
    vColNames.add(" ");
    vId.add(XML_PLAY);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_ALBUM));
    vId.add(XML_ALBUM);

    // First track found author. If different authors in album, will be
    // displayed in italic
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_AUTHOR));
    vId.add(XML_AUTHOR);

    // First track found style. If different styles in album, will be
    // displayed in italic
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_STYLE));
    vId.add(XML_STYLE);

    // First found track year, italic if different values
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_YEAR));
    vId.add(XML_YEAR);

    // Album rate (average of its tracks rate)
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_RATE));
    vId.add(XML_TRACK_RATE);

    // Total album length
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_LENGTH));
    vId.add(XML_TRACK_LENGTH);

    // Number of tracks
    vColNames.add(Messages.getString("AlbumsTableView.1"));
    vId.add(XML_TRACKS);

    // First found track discovery date
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_ADDED));
    vId.add(XML_TRACK_ADDED);

    // Sum of all tracks hits
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_HITS));
    vId.add(XML_TRACK_HITS);

    // custom properties now
    for (PropertyMetaInformation meta : AlbumManager.getInstance().getCustomProperties()) {
      vColNames.add(meta.getName());
      vId.add(meta.getName());
    }
  }

  /**
   * Fill model with data using an optional filter property and pattern
   * <p>
   * For now, this table will not be editable (except for custom properties) for
   * complexity reasons. This may be implemented in the future if required
   * </p>
   */
  @SuppressWarnings("unchecked")
  public synchronized void populateModel(String sPropertyName, String sPattern) {
    List<Album> alToShow = new ArrayList<Album>(AlbumManager.getInstance().getAlbums());
    // OK, begin by filtering using any provided pattern
    Filter filter = new Filter(sPropertyName, sPattern, true, ConfigurationManager
        .getBoolean(CONF_REGEXP));
    Filter.filterItems(alToShow, filter);

    // Filter unmounted files if required
    Iterator<Album> it = alToShow.iterator();
    while (it.hasNext()) {
      Album album = it.next();
      if (ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)
          && !album.containsReadyFiles()) {
        it.remove();
      }
    }
    int iColNum = iNumberStandardCols + AlbumManager.getInstance().getCustomProperties().size();
    iRowNum = alToShow.size();
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];
    // Allow only custom properties edition
    bEditable = true;
    it = alToShow.iterator();
    for (int iRow = 0; it.hasNext(); iRow++) {
      Album album = it.next();
      setItemAt(iRow, album);
      LinkedHashMap properties = album.getProperties();
      // Id
      oItems[iRow] = album;
      // Play
      IconLabel il = null;
      if (album.containsReadyFiles()) {
        il = new IconLabel(PLAY_ICON, "", null, null, null, Messages.getString("TracksTableView.7"));
      } else {
        il = new IconLabel(UNMOUNT_PLAY_ICON, "", null, null, null, Messages
            .getString("TracksTableView.7")
            + Messages.getString("AbstractTableView.10"));
      }
      // Note: if you want to add an image, use an ImageIcon class and
      // change
      oValues[iRow][0] = il;
      bCellEditable[iRow][0] = false;
      // Album name
      oValues[iRow][1] = album.getName2();
      bCellEditable[iRow][1] = false;
      // Author
      Author author = album.getAuthor();
      if (author != null) {
        oValues[iRow][2] = author.getName2();
      } else {
        oValues[iRow][2] = "";
      }
      bCellEditable[iRow][2] = false;
      // Style
      Style style = album.getStyle();
      if (style != null) {
        oValues[iRow][3] = style.getName2();
      } else {
        oValues[iRow][3] = "";
      }
      bCellEditable[iRow][3] = false;
      // Year
      Year year = album.getYear();
      if (year != null) {
        oValues[iRow][4] = year.getValue();
      } else {
        oValues[iRow][4] = "";
      }
      bCellEditable[iRow][4] = false;
      // Rate
      IconLabel ilRate = Util.getStars(album);
      oValues[iRow][5] = ilRate;
      ilRate.setInteger(true);
      // Length
      oValues[iRow][6] = new Duration(album.getDuration());
      bCellEditable[iRow][6] = false;
      // Number of tracks
      oValues[iRow][7] = album.getNbOfTracks();
      bCellEditable[iRow][7] = false;
      // Date discovery
      oValues[iRow][8] = album.getDiscoveryDate();
      bCellEditable[iRow][8] = false;
      // Hits
      oValues[iRow][9] = album.getHits();
      bCellEditable[iRow][9] = false;
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