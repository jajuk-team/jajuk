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

package org.jajuk.ui.helpers;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Filter;
import org.jajuk.util.Messages;

/**
 * Table model used for physical table view.
 */
public class FilesTableModel extends JajukTableModel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Associated view ID */
  private String viewID;

  /**
   * Model constructor.
   * 
   * @param viewID Associated view ID
   */
  public FilesTableModel(String viewID) {
    super(20);
    this.viewID = viewID;
    setEditable(Conf.getBoolean(Const.CONF_FILES_TABLE_EDITION));
    // Columns names
    // First column is play icon, need to set a space character
    // for proper display in some look and feel
    vColNames.add(" ");
    idList.add(Const.XML_PLAY);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK));
    idList.add(Const.XML_TRACK);

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

    vColNames.add(Messages.getHumanPropertyName(Const.XML_DEVICE));
    idList.add(Const.XML_DEVICE);

    vColNames.add(Messages.getString("Property_filename"));
    idList.add(Const.XML_NAME);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_COMMENT));
    idList.add(Const.XML_TRACK_COMMENT);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_QUALITY));
    idList.add(Const.XML_QUALITY);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_SIZE));
    idList.add(Const.XML_SIZE);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_ORDER));
    idList.add(Const.XML_TRACK_ORDER);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_DISC_NUMBER));
    idList.add(Const.XML_TRACK_DISC_NUMBER);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_YEAR));
    idList.add(Const.XML_YEAR);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_DIRECTORY));
    idList.add(Const.XML_DIRECTORY);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_FILE_DATE));
    idList.add(Const.XML_FILE_DATE);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_HITS));
    idList.add(Const.XML_TRACK_HITS);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_DISCOVERY_DATE));
    idList.add(Const.XML_TRACK_DISCOVERY_DATE);

    // -- Custom properties now--
    // for files
    Iterator<PropertyMetaInformation> it = FileManager.getInstance().getCustomProperties()
        .iterator();
    while (it.hasNext()) {
      PropertyMetaInformation meta = it.next();
      vColNames.add(meta.getName());
      idList.add(meta.getName());
    }
    // for tracks
    it = TrackManager.getInstance().getCustomProperties().iterator();
    while (it.hasNext()) {
      PropertyMetaInformation meta = it.next();
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
  public void populateModel(String sPropertyName, String sPattern, List<String> columnsToShow) {
    // This should be monitor file manager to avoid NPE when changing items
    List<File> alToShow = FileManager.getInstance().getFiles();

    // Filter mounted files if needed and apply sync table with tree
    // option if needed
    final boolean syncTreeTable = Conf.getBoolean(Const.CONF_SYNC_TABLE_TREE + "." + viewID);

    oItems = new Item[iRowNum];
    CollectionUtils.filter(alToShow, new Predicate() {

      @Override
      public boolean evaluate(Object o) {
        File file = (File) o;
        // show it if no sync option or if item is in the selection
        boolean bShowWithTree = !syncTreeTable
        // tree selection = null means none selection have been
            // done in tree so far
            || treeSelection == null
            // check if the tree selection contains the current file
            || (treeSelection.size() > 0 && treeSelection.contains(file));
        return (!file.shouldBeHidden() && bShowWithTree);
      }
    });
    // Filter files
    Filter filter = new Filter(sPropertyName, sPattern, true, Conf.getBoolean(Const.CONF_REGEXP));
    Filter.filterItems(alToShow, filter);

    Iterator<File> it = alToShow.iterator();
    int iColNum = iNumberStandardCols + FileManager.getInstance().getCustomProperties().size()
        + TrackManager.getInstance().getCustomProperties().size();
    iRowNum = alToShow.size();
    it = alToShow.iterator();
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];

    // For perfs, prepare columns visibility
    boolean bTrackName = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK));
    boolean bAlbum = (columnsToShow != null && columnsToShow.contains(Const.XML_ALBUM));
    boolean bArtist = (columnsToShow != null && columnsToShow.contains(Const.XML_ARTIST));
    boolean bAlbumArtist = (columnsToShow != null && columnsToShow.contains(Const.XML_ALBUM_ARTIST));
    boolean bGenre = (columnsToShow != null && columnsToShow.contains(Const.XML_GENRE));
    boolean bRate = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_RATE));
    boolean bLength = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_LENGTH));
    boolean bDevice = (columnsToShow != null && columnsToShow.contains(Const.XML_DEVICE));
    boolean bFileName = (columnsToShow != null && columnsToShow.contains(Const.XML_NAME));
    boolean bComment = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_COMMENT));
    boolean bQuality = (columnsToShow != null && columnsToShow.contains(Const.XML_QUALITY));
    boolean bSize = (columnsToShow != null && columnsToShow.contains(Const.XML_SIZE));
    boolean bDiscovery = (columnsToShow != null && columnsToShow
        .contains(Const.XML_TRACK_DISCOVERY_DATE));
    boolean bOrder = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_ORDER));
    boolean bDiscNumber = (columnsToShow != null && columnsToShow
        .contains(Const.XML_TRACK_DISC_NUMBER));
    boolean bYear = (columnsToShow != null && columnsToShow.contains(Const.XML_YEAR));
    boolean bDirectory = (columnsToShow != null && columnsToShow.contains(Const.XML_DIRECTORY));
    boolean bFileDate = (columnsToShow != null && columnsToShow.contains(Const.XML_FILE_DATE));
    boolean bHits = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_HITS));

    for (int iRow = 0; it.hasNext(); iRow++) {
      File file = it.next();
      setItemAt(iRow, file);
      Map<String, Object> properties = file.getProperties();
      // Id
      oItems[iRow] = file;
      // Play
      IconLabel il = null;
      if (file.isReady()) {
        il = getIcon(false);
      } else {
        il = getIcon(true);
      }
      oValues[iRow][0] = il;
      bCellEditable[iRow][0] = false;
      // check track has an associated tag editor (not null)
      boolean bHasATagEditor = false;
      Type type = file.getType();
      if (type != null) {
        bHasATagEditor = (type.getTaggerClass() != null);
      }

      // Track name
      if (bTrackName) {
        oValues[iRow][1] = file.getTrack().getName();
      } else {
        oValues[iRow][1] = "";
      }
      bCellEditable[iRow][1] = bHasATagEditor;

      // Album
      if (bAlbum) {
        oValues[iRow][2] = file.getTrack().getAlbum().getName2();
      } else {
        oValues[iRow][2] = "";
      }
      bCellEditable[iRow][2] = bHasATagEditor;

      // Artist
      if (bArtist) {
        oValues[iRow][3] = file.getTrack().getArtist().getName2();
      } else {
        oValues[iRow][3] = "";
      }
      bCellEditable[iRow][3] = bHasATagEditor;

      // AlbumArtist
      if (bAlbumArtist) {
        oValues[iRow][4] = file.getTrack().getAlbumArtist().getName2();
      } else {
        oValues[iRow][4] = "";
      }
      bCellEditable[iRow][4] = bHasATagEditor;

      // Genre
      if (bGenre) {
        oValues[iRow][5] = file.getTrack().getGenre().getName2();
      } else {
        oValues[iRow][5] = "";
      }
      bCellEditable[iRow][5] = bHasATagEditor;

      // Rate
      if (bRate) {
        oValues[iRow][6] = StarsHelper.getStarIconLabel(file.getTrack());
      } else {
        oValues[iRow][6] = "";
      }
      bCellEditable[iRow][6] = false;

      // Length
      if (bLength) {
        oValues[iRow][7] = new Duration(file.getTrack().getDuration());
      } else {
        oValues[iRow][7] = "";
      }
      bCellEditable[iRow][7] = false;

      // Device
      if (bDevice) {
        oValues[iRow][8] = file.getDirectory().getDevice().getName();
      } else {
        oValues[iRow][8] = "";
      }
      bCellEditable[iRow][8] = false;

      // File name
      if (bFileName) {
        oValues[iRow][9] = file.getName();
      } else {
        oValues[iRow][9] = "";
      }
      bCellEditable[iRow][9] = true;

      // Comment
      if (bComment) {
        oValues[iRow][10] = file.getTrack().getValue(Const.XML_TRACK_COMMENT);
      } else {
        oValues[iRow][10] = "";
      }
      bCellEditable[iRow][10] = bHasATagEditor;

      // Quality
      if (bQuality) {
        long lQuality = file.getQuality();
        oValues[iRow][11] = lQuality;
      } else {
        oValues[iRow][11] = 0l;
      }
      bCellEditable[iRow][11] = false;

      // Size, we want to keep 2 decimals to the value in MB
      if (bSize) {
        oValues[iRow][12] = Math.round(file.getSize() / 10485.76) / 100f;
      } else {
        oValues[iRow][12] = 0l;
      }
      bCellEditable[iRow][12] = false;

      // Order
      if (bOrder) {
        oValues[iRow][13] = file.getTrack().getOrder();
      } else {
        oValues[iRow][13] = "";
      }
      bCellEditable[iRow][13] = bHasATagEditor;

      // Disc number
      if (bDiscNumber) {
        oValues[iRow][14] = file.getTrack().getDiscNumber();
      } else {
        oValues[iRow][14] = "";
      }
      bCellEditable[iRow][14] = bHasATagEditor;

      // year
      if (bYear) {
        oValues[iRow][15] = file.getTrack().getYear().getValue();
      } else {
        oValues[iRow][15] = "";
      }
      bCellEditable[iRow][15] = bHasATagEditor;

      // directory full path
      if (bDirectory) {
        oValues[iRow][16] = file.getDirectory().getAbsolutePath();
      } else {
        oValues[iRow][16] = "";
      }
      bCellEditable[iRow][16] = false;

      // file date
      if (bFileDate) {
        oValues[iRow][17] = file.getDateValue(Const.XML_FILE_DATE);
      } else {
        oValues[iRow][17] = "";
      }
      bCellEditable[iRow][17] = false;

      // Hits
      if (bHits) {
        oValues[iRow][18] = file.getTrack().getHits();
      } else {
        oValues[iRow][18] = "";
      }
      bCellEditable[iRow][18] = false;

      // Discovery date
      if (bDiscovery) {
        oValues[iRow][19] = file.getTrack().getDiscoveryDate();
      } else {
        oValues[iRow][19] = "";
      }
      bCellEditable[iRow][19] = false;

      // -- Custom properties now --
      // files custom tags
      Iterator<PropertyMetaInformation> it2 = FileManager.getInstance().getCustomProperties()
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
        if (meta.getType().equals(Date.class)) {
          bCellEditable[iRow][iNumberStandardCols + i] = false;
        } else {
          bCellEditable[iRow][iNumberStandardCols + i] = true;
        }
      }
      // tracks custom properties
      it2 = TrackManager.getInstance().getCustomProperties().iterator();
      for (int i = FileManager.getInstance().getCustomProperties().size(); it2.hasNext(); i++) {
        PropertyMetaInformation meta = it2.next();
        properties = file.getTrack().getProperties();
        Object o = properties.get(meta.getName());
        if (o != null) {
          oValues[iRow][iNumberStandardCols + i] = properties.get(meta.getName());
        } else {
          oValues[iRow][iNumberStandardCols + i] = meta.getDefaultValue();
        }
        // Date values not editable, use properties panel instead to
        // edit
        if (meta.getType().equals(Date.class)) {
          bCellEditable[iRow][iNumberStandardCols + i] = false;
        } else {
          bCellEditable[iRow][iNumberStandardCols + i] = true;
        }
      }
    }
  }

}
