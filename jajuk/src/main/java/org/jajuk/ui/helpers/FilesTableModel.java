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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Filter;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

/**
 * Table model used for physical table view
 */
public class FilesTableModel extends JajukTableModel implements ITechnicalStrings {

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
    setEditable(ConfigurationManager.getBoolean(CONF_FILES_TABLE_EDITION));
    // Columns names
    // First column is play icon, need to set a space character
    // for proper display in some look and feel
    vColNames.add(" ");
    vId.add(XML_PLAY);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_NAME));
    vId.add(XML_TRACK);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_ALBUM));
    vId.add(XML_ALBUM);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_AUTHOR));
    vId.add(XML_AUTHOR);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_STYLE));
    vId.add(XML_STYLE);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_RATE));
    vId.add(XML_TRACK_RATE);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_LENGTH));
    vId.add(XML_TRACK_LENGTH);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_DEVICE));
    vId.add(XML_DEVICE);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_FILE_NAME));
    vId.add(XML_NAME);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_COMMENT));
    vId.add(XML_TRACK_COMMENT);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_QUALITY));
    vId.add(XML_QUALITY);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_SIZE));
    vId.add(XML_SIZE);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_ORDER));
    vId.add(XML_TRACK_ORDER);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_YEAR));
    vId.add(XML_YEAR);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_DIRECTORY));
    vId.add(XML_DIRECTORY);

    vColNames.add(Messages.getString("Property_file_date"));
    vId.add(XML_FILE_DATE);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_HITS));
    vId.add(XML_TRACK_HITS);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_ADDED));
    vId.add(XML_TRACK_ADDED);

    // -- Custom properties now--
    // for files
    Iterator it = FileManager.getInstance().getCustomProperties().iterator();
    while (it.hasNext()) {
      PropertyMetaInformation meta = (PropertyMetaInformation) it.next();
      vColNames.add(meta.getName());
      vId.add(meta.getName());
    }
    // for tracks
    it = TrackManager.getInstance().getCustomProperties().iterator();
    while (it.hasNext()) {
      PropertyMetaInformation meta = (PropertyMetaInformation) it.next();
      vColNames.add(meta.getName());
      vId.add(meta.getName());
    }
  }

  /**
   * Fill model with data using an optional filter property and pattern
   */
  @SuppressWarnings("unchecked")
  public synchronized void populateModel(String sPropertyName, String sPattern) {
    ArrayList<File> alToShow = null;
    // Filter mounted files if needed and apply sync table with tree
    // option if needed
    boolean bShowWithTree = true;
    // look at selection
    boolean bSyncWithTreeOption = ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE);
    Set<File> files = FileManager.getInstance().getFiles();
    alToShow = new ArrayList<File>(files.size() / 2);
    oItems = new Item[iRowNum];
    Iterator it = files.iterator();
    while (it.hasNext()) {
      File file = (File) it.next();
      // show it if no sync option or if item is in the selection
      bShowWithTree = !bSyncWithTreeOption // no tree/table sync option
          // tree selection = null means none election have been
          // selected in tree so far
          || treeSelection == null
          // check if the tree selection contains the current file
          || (treeSelection.size() > 0 && treeSelection.contains(file));
      if (!file.shouldBeHidden() && bShowWithTree) {
        alToShow.add(file);
      }
    }
    //Filter files
    Filter filter = new Filter(sPropertyName,sPattern,true,ConfigurationManager.getBoolean(CONF_REGEXP));
    Util.filterItems(alToShow, filter);
    
    it = alToShow.iterator();
    int iColNum = iNumberStandardCols + FileManager.getInstance().getCustomProperties().size()
        + TrackManager.getInstance().getCustomProperties().size();
    iRowNum = alToShow.size();
    it = alToShow.iterator();
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];
    for (int iRow = 0; it.hasNext(); iRow++) {
      File file = (File) it.next();
      setItemAt(iRow, file);
      LinkedHashMap properties = file.getProperties();
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
      oValues[iRow][1] = file.getTrack().getName();
      bCellEditable[iRow][1] = bHasATagEditor;
      // Album
      oValues[iRow][2] = file.getTrack().getAlbum().getName2();
      bCellEditable[iRow][2] = bHasATagEditor;
      // Author
      oValues[iRow][3] = file.getTrack().getAuthor().getName2();
      bCellEditable[iRow][3] = bHasATagEditor;
      // Style
      oValues[iRow][4] = file.getTrack().getStyle().getName2();
      bCellEditable[iRow][4] = bHasATagEditor;
      // Rate
      IconLabel ilRate = Util.getStars(file.getTrack());
      oValues[iRow][5] = ilRate;
      bCellEditable[iRow][5] = false;
      // Length
      oValues[iRow][6] = new Duration(file.getTrack().getDuration());
      bCellEditable[iRow][6] = false;
      // Device
      oValues[iRow][7] = file.getDirectory().getDevice().getName();
      bCellEditable[iRow][7] = false;
      // File name
      oValues[iRow][8] = file.getName();
      bCellEditable[iRow][8] = true;
      // Comment
      oValues[iRow][9] = file.getTrack().getValue(XML_TRACK_COMMENT);
      bCellEditable[iRow][9] = bHasATagEditor;
      // Quality
      long lQuality = file.getQuality();
      oValues[iRow][10] = lQuality;
      bCellEditable[iRow][10] = false;
      // Size, we want to keep 2 decimals to the value in MB
      oValues[iRow][11] = Math.round(file.getSize()/10485.76) / 100f;
      bCellEditable[iRow][11] = false;
      // Order
      oValues[iRow][12] = file.getTrack().getOrder();
      bCellEditable[iRow][12] = bHasATagEditor;
      // year
      oValues[iRow][13] = file.getTrack().getYear();
      bCellEditable[iRow][13] = bHasATagEditor;
      // directory full path
      oValues[iRow][14] = file.getDirectory().getAbsolutePath();
      bCellEditable[iRow][14] = false;
      // file date
      oValues[iRow][15] = file.getDateValue(XML_FILE_DATE);
      bCellEditable[iRow][15] = false;
      // Hits
      oValues[iRow][16] = file.getTrack().getHits();
      bCellEditable[iRow][16] = false;
      // Discovery date
      oValues[iRow][17] = file.getTrack().getDiscoveryDate();
      bCellEditable[iRow][17] = false;

      // -- Custom properties now --
      // files custom tags
      Iterator it2 = FileManager.getInstance().getCustomProperties().iterator();
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
        if (meta.getType().equals(Date.class)) {
          bCellEditable[iRow][iNumberStandardCols + i] = false;
        } else {
          bCellEditable[iRow][iNumberStandardCols + i] = true;
        }
      }
      // tracks custom properties
      it2 = TrackManager.getInstance().getCustomProperties().iterator();
      for (int i = FileManager.getInstance().getCustomProperties().size(); it2.hasNext(); i++) {
        PropertyMetaInformation meta = (PropertyMetaInformation) it2.next();
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
