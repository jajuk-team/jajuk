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
import java.util.List;
import java.util.Map;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Filter;
import org.jajuk.util.Messages;

/**
 * Table model used holding playlist repository data
 */
public class PlaylistRepositoryTableModel extends JajukTableModel {

  private static final long serialVersionUID = 1L;

  /**
   * Model constructor
   * 
   * @param iColNum
   *          number of rows
   * @param sColName
   *          columns names
   */
  public PlaylistRepositoryTableModel() {
    super(5);
    setEditable(false);
    // Columns names
    // First column is play icon, need to set a space character
    // for proper display in some look and feel
    vColNames.add(" ");
    vId.add(XML_PLAY);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_NAME));
    vId.add(XML_NAME);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_DEVICE));
    vId.add(XML_DEVICE);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_DIRECTORY));
    vId.add(XML_DIRECTORY);

    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_PATH));
    vId.add(XML_PATH);

    // custom properties now
    for (PropertyMetaInformation meta : PlaylistManager.getInstance().getCustomProperties()) {
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
  @Override
  @SuppressWarnings("unchecked")
  public synchronized void populateModel(String sPropertyName, String sPattern,
      ArrayList<String> columnsToShow) {
    List<Playlist> alToShow = new ArrayList<Playlist>(PlaylistManager.getInstance().getPlaylists());
    // OK, begin by filtering using any provided pattern
    Filter filter = new Filter(sPropertyName, sPattern, true, ConfigurationManager
        .getBoolean(CONF_REGEXP));
    Filter.filterItems(alToShow, filter);

    Iterator<Playlist> it = null;

    // Filter unmounted files if required
    if (ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)) {
      it = alToShow.iterator();
      while (it.hasNext()) {
        Playlist plf = it.next();
        if (!plf.getDirectory().getDevice().isMounted()) {
          // Note: don't check .exists() or .canRead() here : it takes
          // a long time for unmounted network drive
          it.remove();
        }
      }
    }
    int iColNum = iNumberStandardCols + PlaylistManager.getInstance().getCustomProperties().size();
    iRowNum = alToShow.size();
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];
    // Allow only custom properties edition
    bEditable = true;

    // For perfs, prepare columns visibility
    boolean bName = (columnsToShow != null && columnsToShow.contains(XML_NAME));
    boolean bDevice = (columnsToShow != null && columnsToShow.contains(XML_DEVICE));
    boolean bDirectory = (columnsToShow != null && columnsToShow.contains(XML_DIRECTORY));
    boolean bPath = (columnsToShow != null && columnsToShow.contains(XML_PATH));

    it = alToShow.iterator();
    for (int iRow = 0; it.hasNext(); iRow++) {
      Playlist plf = it.next();
      setItemAt(iRow, plf);
      Map<String, Object> properties = plf.getProperties();
      // Id
      oItems[iRow] = plf;
      // Play
      IconLabel il = null;
      if (plf.getDirectory().getDevice().isMounted()) {
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

      // Playlist Name
      if (bName) {
        oValues[iRow][1] = plf.getName();
      } else {
        oValues[iRow][1] = "";
      }
      bCellEditable[iRow][1] = false;

      // Device
      if (bDevice) {
        Device device = plf.getDirectory().getDevice();
        oValues[iRow][2] = device.getName();
      } else {
        oValues[iRow][2] = "";
      }
      bCellEditable[iRow][2] = false;

      // Directory
      if (bDirectory) {
        Directory directory = plf.getDirectory();
        oValues[iRow][3] = directory.getName();
      } else {
        oValues[iRow][3] = "";
      }
      bCellEditable[iRow][3] = false;

      // PATH
      if (bPath) {
        String path = plf.getAbsolutePath();
        oValues[iRow][4] = path;
      } else {
        oValues[iRow][4] = "";
      }
      bCellEditable[iRow][4] = false;

      // Custom properties now
      Iterator it2 = PlaylistManager.getInstance().getCustomProperties().iterator();
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