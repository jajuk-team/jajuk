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

import org.apache.commons.collections.CollectionUtils;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Filter;
import org.jajuk.util.Messages;
import org.jajuk.util.filters.JajukPredicates;

/**
 * Table model used holding playlist repository data.
 */
public class PlaylistRepositoryTableModel extends JajukTableModel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Model constructor.
   */
  public PlaylistRepositoryTableModel() {
    super(5);
    setEditable(false);
    // Columns names
    // First column is play icon, need to set a space character
    // for proper display in some look and feel
    vColNames.add(" ");
    idList.add(Const.XML_PLAY);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_NAME));
    idList.add(Const.XML_NAME);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_DEVICE));
    idList.add(Const.XML_DEVICE);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_DIRECTORY));
    idList.add(Const.XML_DIRECTORY);

    vColNames.add(Messages.getHumanPropertyName(Const.XML_PATH));
    idList.add(Const.XML_PATH);

    // custom properties now
    for (PropertyMetaInformation meta : PlaylistManager.getInstance().getCustomProperties()) {
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
  public void populateModel(String sPropertyName, String sPattern, List<String> columnsToShow) {
    List<Playlist> alToShow = PlaylistManager.getInstance().getPlaylists();
    // OK, begin by filtering using any provided pattern
    Filter filter = new Filter(sPropertyName, sPattern, true, Conf.getBoolean(Const.CONF_REGEXP));
    alToShow = Filter.filterItems(alToShow, filter, Playlist.class);

    // filter unavailable playlists
    if (Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)) {
      CollectionUtils.filter(alToShow, new JajukPredicates.ReadyPlaylistPredicate());
    }

    Iterator<Playlist> it = null;

    int iColNum = iNumberStandardCols + PlaylistManager.getInstance().getCustomProperties().size();
    iRowNum = alToShow.size();
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];
    // Allow only custom properties edition
    bEditable = true;

    // For perfs, prepare columns visibility
    boolean bName = (columnsToShow != null && columnsToShow.contains(Const.XML_NAME));
    boolean bDevice = (columnsToShow != null && columnsToShow.contains(Const.XML_DEVICE));
    boolean bDirectory = (columnsToShow != null && columnsToShow.contains(Const.XML_DIRECTORY));
    boolean bPath = (columnsToShow != null && columnsToShow.contains(Const.XML_PATH));

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
        il = getIcon(false);
      } else {
        il = getIcon(true);
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
      Iterator<PropertyMetaInformation> it2 = PlaylistManager.getInstance().getCustomProperties().iterator();
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