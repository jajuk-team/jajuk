/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Filter;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Table model used for logical table view.
 */
public class TracksTableModel extends JajukTableModel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Associated view ID */
  private String viewID;

  /**
   * Model constructor.
   * 
   * @param viewID Associated view ID
   */
  public TracksTableModel(String viewID) {
    super(15);
    this.viewID = viewID;
    setEditable(Conf.getBoolean(Const.CONF_TRACKS_TABLE_EDITION));
    // Columns names
    // First column is play icon, need to set a space character
    // for proper display in some look and feel
    vColNames.add(" ");
    idList.add(Const.XML_PLAY);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_NAME));
    idList.add(Const.XML_NAME);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_ALBUM));
    idList.add(Const.XML_ALBUM);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_ARTIST));
    idList.add(Const.XML_ARTIST);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_ALBUM_ARTIST));
    idList.add(Const.XML_ALBUM_ARTIST);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_GENRE));
    idList.add(Const.XML_GENRE);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_RATE));
    idList.add(Const.XML_TRACK_RATE);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_LENGTH));
    idList.add(Const.XML_TRACK_LENGTH);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_COMMENT));
    idList.add(Const.XML_TRACK_COMMENT);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_DISCOVERY_DATE));
    idList.add(Const.XML_TRACK_DISCOVERY_DATE);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_ORDER));
    idList.add(Const.XML_TRACK_ORDER);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_DISC_NUMBER));
    idList.add(Const.XML_TRACK_DISC_NUMBER);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_YEAR));
    idList.add(Const.XML_YEAR);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_HITS));
    idList.add(Const.XML_TRACK_HITS);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_FILES));
    idList.add(Const.XML_FILES);

    // custom properties now
    for (PropertyMetaInformation meta : TrackManager.getInstance().getCustomProperties()) {
      vColNames.add(meta.getName());
      idList.add(meta.getName());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.helpers.JajukTableModel#populateModel(java.lang.String, java.lang.String,
   * java.util.List)
   */
  @Override
  public void populateModel(String property, String sPattern, List<String> columnsToShow) {

    // This should be monitor file manager to avoid NPE when changing items
    List<Track> alToShow = TrackManager.getInstance().getTracks();

    // / Filter mounted files if needed and apply sync table with tree
    // option if needed
    final boolean syncTreeTable = Conf.getBoolean(Const.CONF_SYNC_TABLE_TREE + "." + viewID);

    CollectionUtils.filter(alToShow, new Predicate() {

      @Override
      public boolean evaluate(Object o) {
        Track track = (Track) o;
        // show it if no sync option or if item is in the selection
        boolean bShowWithTree = !syncTreeTable
        // tree selection = null means none election have been
            // selected in tree so far
            || treeSelection == null
            // check if the tree selection contains the current file
            || (treeSelection.size() > 0 && treeSelection.contains(track));
        return (!track.shouldBeHidden() && bShowWithTree);
      }
    });

    // Filter values using given pattern
    Filter filter = new Filter(property, sPattern, true, Conf.getBoolean(Const.CONF_REGEXP));
    Filter.filterItems(alToShow, filter);

    // sort by album
    long before = System.currentTimeMillis();
    Collections.sort(alToShow, new TrackComparator(TrackComparatorType.ALBUM));

    // Collections.sort(alToShow, new TrackComparator(TrackComparatorType.ALBUM));
    Log.debug("Sorting of " + alToShow.size() + " elements took: "
        + (System.currentTimeMillis() - before) + " mseconds");

    Iterator<Track> it = alToShow.iterator();
    int iColNum = iNumberStandardCols + TrackManager.getInstance().getCustomProperties().size();
    iRowNum = alToShow.size();
    it = alToShow.iterator();
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];

    // For perfs, prepare columns visibility
    boolean bName = (columnsToShow != null && columnsToShow.contains(Const.XML_NAME));
    boolean bAlbum = (columnsToShow != null && columnsToShow.contains(Const.XML_ALBUM));
    boolean bArtist = (columnsToShow != null && columnsToShow.contains(Const.XML_ARTIST));
    boolean bAlbumArtist = (columnsToShow != null && columnsToShow.contains(Const.XML_ALBUM_ARTIST));
    boolean bGenre = (columnsToShow != null && columnsToShow.contains(Const.XML_GENRE));
    boolean bRate = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_RATE));
    boolean bLength = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_LENGTH));
    boolean bComment = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_COMMENT));
    boolean bDiscovery = (columnsToShow != null && columnsToShow
        .contains(Const.XML_TRACK_DISCOVERY_DATE));
    boolean bOrder = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_ORDER));
    boolean bDiscNumber = (columnsToShow != null && columnsToShow
        .contains(Const.XML_TRACK_DISC_NUMBER));
    boolean bYear = (columnsToShow != null && columnsToShow.contains(Const.XML_YEAR));
    boolean bHits = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_HITS));
    boolean bFiles = (columnsToShow != null && columnsToShow.contains(Const.XML_FILES));

    for (int iRow = 0; it.hasNext(); iRow++) {
      Track track = it.next();
      setItemAt(iRow, track);
      Map<String, Object> properties = track.getProperties();
      // Id
      oItems[iRow] = track;
      // Play
      IconLabel il = null;
      if (track.getBestFile(true) != null) {
        il = getIcon(false);
      } else {
        il = getIcon(true);
      }
      // Note: if you want to add an image, use an ImageIcon class and
      // change
      oValues[iRow][0] = il;
      bCellEditable[iRow][0] = false;
      // check track has an associated tag editor (not null)
      boolean bHasATagEditor = false;
      File file = track.getFiles().get(0);
      // all files have the same type
      Type type = file.getType();
      if (type != null) {
        bHasATagEditor = (type.getTaggerClass() != null);
      }

      // Track name
      if (bName) {
        oValues[iRow][1] = track.getName();
      } else {
        oValues[iRow][1] = "";
      }
      bCellEditable[iRow][1] = bHasATagEditor;

      // Album
      if (bAlbum) {
        oValues[iRow][2] = track.getAlbum().getName2();
      } else {
        oValues[iRow][2] = "";
      }
      bCellEditable[iRow][2] = bHasATagEditor;

      // Artist
      if (bArtist) {
        oValues[iRow][3] = track.getArtist().getName2();
      } else {
        oValues[iRow][3] = "";
      }
      bCellEditable[iRow][3] = bHasATagEditor;

      // Album Artist
      if (bAlbumArtist) {
        oValues[iRow][4] = track.getAlbumArtist().getName2();
      } else {
        oValues[iRow][4] = "";
      }
      bCellEditable[iRow][4] = bHasATagEditor;

      // Genre
      if (bGenre) {
        oValues[iRow][5] = track.getGenre().getName2();
      } else {
        oValues[iRow][5] = "";
      }
      bCellEditable[iRow][5] = bHasATagEditor;

      // Rate
      if (bRate) {
        oValues[iRow][6] = StarsHelper.getStarIconLabel(track);
      } else {
        oValues[iRow][6] = "";
      }
      bCellEditable[iRow][6] = false;

      // Length
      if (bLength) {
        oValues[iRow][7] = new Duration(track.getDuration());
      } else {
        oValues[iRow][7] = "";
      }
      bCellEditable[iRow][7] = false;

      // Comment
      if (bComment) {
        oValues[iRow][8] = track.getValue(Const.XML_TRACK_COMMENT);
      } else {
        oValues[iRow][8] = "";
      }
      bCellEditable[iRow][8] = bHasATagEditor;

      // Date discovery
      if (bDiscovery) {
        oValues[iRow][9] = track.getDiscoveryDate(); // show date using
        // default local format
        // and not technical
        // representation
        bCellEditable[iRow][9] = false;
      } else {
        oValues[iRow][9] = "";
      }

      // Order
      if (bOrder) {
        oValues[iRow][10] = track.getOrder();
      } else {
        oValues[iRow][10] = "";
      }
      bCellEditable[iRow][10] = bHasATagEditor;

      // Disc number
      if (bDiscNumber) {
        oValues[iRow][11] = track.getDiscNumber();
      } else {
        oValues[iRow][11] = "";
      }
      bCellEditable[iRow][11] = bHasATagEditor;

      // Year
      if (bYear) {
        oValues[iRow][12] = track.getYear().getValue();
      } else {
        oValues[iRow][12] = "";
      }
      bCellEditable[iRow][12] = bHasATagEditor;

      // Hits
      if (bHits) {
        oValues[iRow][13] = track.getHits();
      } else {
        oValues[iRow][13] = "";
      }
      bCellEditable[iRow][13] = false;

      // Files
      if (bFiles) {
        List<File> alFiles = track.getFiles();
        StringBuilder files = new StringBuilder(50);
        // for perfs, we manage differently single file tracks and multi-files
        // tracks
        if (alFiles.size() == 1) {
          files.append(alFiles.get(0).getAbsolutePath());
        } else {
          for (File file2 : alFiles) {
            files.append(file2.getAbsolutePath()).append(',');
          }
          files.deleteCharAt(files.length() - 1);
        }
        oValues[iRow][14] = files.toString();

      } else {
        oValues[iRow][14] = "";
      }
      bCellEditable[iRow][14] = false;

      // Custom properties now
      Iterator<PropertyMetaInformation> it2 = TrackManager.getInstance().getCustomProperties()
          .iterator();
      for (int i = 0; it2.hasNext(); i++) {
        PropertyMetaInformation meta = it2.next();
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
