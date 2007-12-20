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

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Author;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

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

    // First found track discovery date, italic if different values
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
   */
  @SuppressWarnings("unchecked")
  public synchronized void populateModel(String sPropertyName, String sPattern) {
    Set<Track> allTracks = TrackManager.getInstance().getTracks();
    ArrayList<Track> alToShow = null;
    // OK, begin by filtering by pattern
    if (!Util.isVoid(sPattern)) {

    }

    // Filter mounted files if needed and apply sync table with tree option
    // if needed
    // look at selection
    Set<Track> alTracks = TrackManager.getInstance().getTracks();
    alToShow = new ArrayList<Track>(alTracks.size());
    for (Track track : alTracks) {
      if (!track.shouldBeHidden()) {
        alToShow.add(track);
      }
    }
    Set<Album> albumsSet = new HashSet<Album>(alToShow.size() / 10);
    // Build the album list
    for (Track track : alToShow) {
      albumsSet.add(track.getAlbum());
    }
    // Now filter albums using given pattern
    if (sPropertyName != null && sPattern != null) {
      // Prepare filter pattern
      String sNewPattern = sPattern;
      if (!ConfigurationManager.getBoolean(CONF_REGEXP)) {
        // do we use regular expression or not? if not, we allow
        // user to use '*'
        sNewPattern = sNewPattern.replaceAll("\\*", ".*");
        sNewPattern = ".*" + sNewPattern + ".*";
      } else if ("".equals(sNewPattern)) {
        // in regexp mode, if none
        // selection, display all rows
        sNewPattern = ".*";
      }
      // Now filter albums
      Iterator<Album> it = albumsSet.iterator();
      // use an iterator to be able to delete elements
      while (it.hasNext()) {
        Album album = it.next();
        if (sPropertyName != null && sNewPattern != null) {
          // if name or value is null, means there is no filter
          String sValue = null;
          if (XML_ALBUM.equals(sPropertyName)) {
            sValue = album.getName2();
          } else if (XML_STYLE.equals(sPropertyName)) {
            sValue = album.getName2();
          }

          if (sValue == null) {
            // try to filter on a unknown property, don't take
            // this item
            continue;
          } else {
            boolean bMatch = false;
            try { // test using regular expressions
              bMatch = sValue.toLowerCase().matches(sNewPattern.toLowerCase());
              // test if the item property contains this
              // property value (ignore case)
            } catch (PatternSyntaxException pse) {
              // wrong pattern syntax
              bMatch = false;
            }
            if (!bMatch) {
              it.remove(); // no? remove it
            }
          }
        }
      }
    }
    // sort by album
    List<Album> albumsList = new ArrayList<Album>(albumsSet);
    // sort albums by name
    Collections.sort(albumsList);
    int iColNum = iNumberStandardCols + AlbumManager.getInstance().getCustomProperties().size();
    iRowNum = alToShow.size();
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];
    // For perfs, store a map album-> list of tracks
    HashMap<Album, Set<Track>> hmAlbumTracks = new HashMap<Album, Set<Track>>(albumsList.size());
    // Map album-> whether it contains at least one available file to
    // play
    HashMap<Album, Boolean> hmAlbumAvailable = new HashMap<Album, Boolean>(albumsList.size());
    // Map album -> style (null if different styles)
    HashMap<Album, Style> hmAlbumStyle = new HashMap<Album, Style>(albumsList.size());
    // Map album -> author (null if different authors)
    HashMap<Album, Author> hmAlbumAuthor = new HashMap<Album, Author>(albumsList.size());
    // Map album -> discovery date (null if different among items)
    HashMap<Album, Date> hmAlbumDicoveryDate = new HashMap<Album, Date>(albumsList.size());
    // Map album -> year (null if different among items)
    HashMap<Album, Year> hmAlbumYear = new HashMap<Album, Year>(albumsList.size());

    for (Album album : albumsList) {
      // This line is costly
      Set<Track> tracksSet = TrackManager.getInstance().getAssociatedTracks(album);
      hmAlbumTracks.put(album, tracksSet);
      boolean bOneAvailableFile = false;
      // Take first style found
      Style style = tracksSet.iterator().next().getStyle();
      // Take first author found
      Author author = tracksSet.iterator().next().getAuthor();
      // Same for discovery date
      Date discovery = tracksSet.iterator().next().getDiscoveryDate();
      // Same for years
      Year year = tracksSet.iterator().next().getYear();
      for (Track track : tracksSet) {
        if (track.getPlayeableFile(true) != null) {
          bOneAvailableFile = true;
        }
        // check if next track is different (don't test if style is
        // already null for perf)
        if (style != null && !track.getStyle().equals(style)) {
          style = null;
        }
        // Same for authors
        if (author != null && !track.getAuthor().equals(author)) {
          author = null;
        }
        // Same for discovery date
        if (discovery != null && !track.getDiscoveryDate().equals(discovery)) {
          discovery = null;
        }
        // Same for year
        if (year != null && !track.getYear().equals(year)) {
          year = null;
        }
      }
      hmAlbumAvailable.put(album, bOneAvailableFile);
      hmAlbumAuthor.put(album, author);
      hmAlbumStyle.put(album, style);
    }
    Iterator<Album> it = albumsList.iterator();
    for (int iRow = 0; it.hasNext(); iRow++) {
      Album album = it.next();
      Set<Track> tracks = hmAlbumTracks.get(album);
      setItemAt(iRow, album);
      LinkedHashMap properties = album.getProperties();
      // Id
      oItems[iRow] = album;
      // Compute editable state :
      // Check all track has an associated tag editor (not null)
      // We make album editable only if all tracks are editable
      boolean bHasATagEditor = true;
      for (Track track : tracks) {
        File file = track.getFiles().get(0);
        // all files have the same type
        Type type = file.getType();
        if (type.getTaggerClass() == null) {
          bHasATagEditor = false;
          break;
        }
      }
      // Play
      IconLabel il = null;
      if (hmAlbumAvailable.get(album)) {
        il = new IconLabel(PLAY_ICON, "", null, null, null, Messages.getString("TracksTreeView.1"));
      } else {
        il = new IconLabel(UNMOUNT_PLAY_ICON, "", null, null, null, Messages
            .getString("TracksTreeView.1")
            + Messages.getString("AbstractTableView.10"));
      }
      // Note: if you want to add an image, use an ImageIcon class and
      // change
      oValues[iRow][0] = il;
      bCellEditable[iRow][0] = false;
      // Album name
      oValues[iRow][1] = album.getName2();
      bCellEditable[iRow][1] = bHasATagEditor;
      // Author
      Author author = hmAlbumAuthor.get(album);
      if (author != null) {
        oValues[iRow][2] = author.getName2();
      } else {
        oValues[iRow][2] = "";
      }
      bCellEditable[iRow][2] = bHasATagEditor;
      // Style
      Style style = hmAlbumStyle.get(album);
      if (style != null) {
        oValues[iRow][3] = style.getName2();
      } else {
        oValues[iRow][3] = "";
      }
      bCellEditable[iRow][3] = bHasATagEditor;
      // Year
      Year year = hmAlbumYear.get(album);
      if (year != null) {
        oValues[iRow][4] = year.getValue();
      } else {
        oValues[iRow][4] = "";
      }
      bCellEditable[iRow][4] = bHasATagEditor;
      // Rate
      IconLabel ilRate = new IconLabel(IconLoader.ICON_STAR_1, ""); // TODO
      // Util.getStars(album);
      oValues[iRow][5] = ilRate;
      bCellEditable[iRow][5] = false;
      ilRate.setInteger(true);
      // Length
      long length = 0;
      for (Track track : tracks) {
        length += track.getDuration();
      }
      oValues[iRow][6] = Util.formatTimeBySec(length, false);
      bCellEditable[iRow][6] = false;
      // Number of tracks
      oValues[iRow][7] = tracks.size();
      bCellEditable[iRow][7] = bHasATagEditor;
      // Date discovery
      oValues[iRow][8] = hmAlbumDicoveryDate.get(album);
      bCellEditable[iRow][8] = false;
      // Hits
      long hits = 0;
      for (Track track : tracks) {
        hits += track.getHits();
      }
      oValues[iRow][9] = hits;
      bCellEditable[iRow][9] = false;
      // Custom properties now
      Iterator it2 = TrackManager.getInstance().getCustomProperties().iterator();
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