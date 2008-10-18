/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 * $Revision$
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
 * Table model used for physical table view
 */
public class FilesTableModel extends JajukTableModel {

  private static final long serialVersionUID = 1L;

  /**
   * Model constructor
   * 
   * @param iColNum
   *          number of rows
   * @param sColName
   *          columns names
   */
  public FilesTableModel() {
    super(18);
    setEditable(Conf.getBoolean(Const.CONF_FILES_TABLE_EDITION));
    // Columns names
    // First column is play icon, need to set a space character
    // for proper display in some look and feel
    vColNames.add(" ");
    idList.add(Const.XML_PLAY);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_NAME));
    idList.add(Const.XML_TRACK);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_ALBUM));
    idList.add(Const.XML_ALBUM);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_AUTHOR));
    idList.add(Const.XML_AUTHOR);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_STYLE));
    idList.add(Const.XML_STYLE);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_TRACK_RATE));
    idList.add(Const.XML_TRACK_RATE);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_TRACK_LENGTH));
    idList.add(Const.XML_TRACK_LENGTH);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_DEVICE));
    idList.add(Const.XML_DEVICE);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_FILE_NAME));
    idList.add(Const.XML_NAME);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_TRACK_COMMENT));
    idList.add(Const.XML_TRACK_COMMENT);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_QUALITY));
    idList.add(Const.XML_QUALITY);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_SIZE));
    idList.add(Const.XML_SIZE);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_TRACK_ORDER));
    idList.add(Const.XML_TRACK_ORDER);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_YEAR));
    idList.add(Const.XML_YEAR);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_DIRECTORY));
    idList.add(Const.XML_DIRECTORY);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_FILE_DATE));
    idList.add(Const.XML_FILE_DATE);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_TRACK_HITS));
    idList.add(Const.XML_TRACK_HITS);

    vColNames.add(Messages.getString(Const.PROPERTY_SEPARATOR + Const.XML_TRACK_DISCOVERY_DATE));
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

  /**
   * Fill model with data using an optional filter property and pattern
   */
  @Override
  public void populateModel(String sPropertyName, String sPattern, List<String> columnsToShow) {
    // This should be monitor file manager to avoid NPE when changing items
    List<File> alToShow = FileManager.getInstance().getFiles();
    // Collections.sort(alToShow);
    // Filter mounted files if needed and apply sync table with tree
    // option if needed
    final boolean bSyncWithTreeOption = Conf.getBoolean(Const.CONF_OPTIONS_SYNC_TABLE_TREE);
    oItems = new Item[iRowNum];
    CollectionUtils.filter(alToShow, new Predicate() {

      public boolean evaluate(Object o) {
        File file = (File) o;
        // show it if no sync option or if item is in the selection
        boolean bShowWithTree = !bSyncWithTreeOption
        // tree selection = null means none election have been
            // selected in tree so far
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
    boolean bAuthor = (columnsToShow != null && columnsToShow.contains(Const.XML_AUTHOR));
    boolean bStyle = (columnsToShow != null && columnsToShow.contains(Const.XML_STYLE));
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
        il = new IconLabel(PLAY_ICON, "", null, null, null, Messages.getString("TracksTableView.7"));
      } else {
        il = new IconLabel(UNMOUNT_PLAY_ICON, "", null, null, null, Messages
            .getString("TracksTableView.7")
            + Messages.getString("AbstractTableView.10"));
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

      // Author
      if (bAuthor) {
        oValues[iRow][3] = file.getTrack().getAuthor().getName2();
      } else {
        oValues[iRow][3] = "";
      }
      bCellEditable[iRow][3] = bHasATagEditor;

      // Style
      if (bStyle) {
        oValues[iRow][4] = file.getTrack().getStyle().getName2();
      } else {
        oValues[iRow][4] = "";
      }
      bCellEditable[iRow][4] = bHasATagEditor;

      // Rate
      if (bRate) {
        IconLabel ilRate = file.getTrack().getStars();
        oValues[iRow][5] = ilRate;
      } else {
        oValues[iRow][5] = "";
      }
      bCellEditable[iRow][5] = false;

      // Length
      if (bLength) {
        oValues[iRow][6] = new Duration(file.getTrack().getDuration());
      } else {
        oValues[iRow][6] = "";
      }
      bCellEditable[iRow][6] = false;

      // Device
      if (bDevice) {
        oValues[iRow][7] = file.getDirectory().getDevice().getName();
      } else {
        oValues[iRow][7] = "";
      }
      bCellEditable[iRow][7] = false;

      // File name
      if (bFileName) {
        oValues[iRow][8] = file.getName();
      } else {
        oValues[iRow][8] = "";
      }
      bCellEditable[iRow][8] = true;

      // Comment
      if (bComment) {
        oValues[iRow][9] = file.getTrack().getValue(Const.XML_TRACK_COMMENT);
      } else {
        oValues[iRow][9] = "";
      }
      bCellEditable[iRow][9] = bHasATagEditor;

      // Quality
      if (bQuality) {
        long lQuality = file.getQuality();
        oValues[iRow][10] = lQuality;
      } else {
        oValues[iRow][10] = "";
      }
      bCellEditable[iRow][10] = false;

      // Size, we want to keep 2 decimals to the value in MB
      if (bSize) {
        oValues[iRow][11] = Math.round(file.getSize() / 10485.76) / 100f;
      } else {
        oValues[iRow][11] = "";
      }
      bCellEditable[iRow][11] = false;

      // Order
      if (bOrder) {
        oValues[iRow][12] = file.getTrack().getOrder();
      } else {
        oValues[iRow][12] = "";
      }
      bCellEditable[iRow][12] = bHasATagEditor;

      // year
      if (bYear) {
        oValues[iRow][13] = file.getTrack().getYear();
      } else {
        oValues[iRow][13] = "";
      }
      bCellEditable[iRow][13] = bHasATagEditor;

      // directory full path
      if (bDirectory) {
        oValues[iRow][14] = file.getDirectory().getAbsolutePath();
      } else {
        oValues[iRow][14] = "";
      }
      bCellEditable[iRow][14] = false;

      // file date
      if (bFileDate) {
        oValues[iRow][15] = file.getDateValue(Const.XML_FILE_DATE);
      } else {
        oValues[iRow][15] = "";
      }
      bCellEditable[iRow][15] = false;

      // Hits
      if (bHits) {
        oValues[iRow][16] = file.getTrack().getHits();
      } else {
        oValues[iRow][16] = "";
      }
      bCellEditable[iRow][16] = false;

      // Discovery date
      if (bDiscovery) {
        oValues[iRow][17] = file.getTrack().getDiscoveryDate();
      } else {
        oValues[iRow][17] = "";
      }
      bCellEditable[iRow][17] = false;

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
