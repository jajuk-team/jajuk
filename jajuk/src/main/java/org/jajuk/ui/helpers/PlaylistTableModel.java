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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.TrackManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.Const;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Playlist model used in queue view or playlist view.
 */
public class PlaylistTableModel extends JajukTableModel {

  /** Values. */
  private List<StackItem> alItems = new ArrayList<StackItem>(10);

  /** Values planned. */
  private List<StackItem> alPlanned = new ArrayList<StackItem>(10);

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Whether this model is used by a Queue View. */
  private boolean bQueue = false;

  /**
   * Instantiates a new playlist table model.
   * 
   * @param bQueue DOCUMENT_ME
   */
  public PlaylistTableModel(boolean bQueue) {
    super(17);
    this.bQueue = bQueue;
    setEditable(false); // table not editable
    prepareColumns();
  }

  /**
   * Need to overwrite this method for drag and drop.
   * 
   * @param iRow DOCUMENT_ME
   * 
   * @return the item at
   */
  @Override
  public Item getItemAt(int iRow) {
    StackItem si = getStackItem(iRow);
    if (si != null) {
      return si.getFile();
    }
    return null;
  }

  /**
   * Return all stack items from this value to the end of selection.
   * 
   * @param index DOCUMENT_ME
   * 
   * @return an arraylist of stack items or null if index is out of bounds
   */
  public List<StackItem> getItemsFrom(int index) {
    if (index < alItems.size()) {
      return new ArrayList<StackItem>(alItems.subList(index, alItems.size()));
    } else {
      return null;
    }
  }

  /**
   * Return right stack item in normal or planned stacks.
   * 
   * @param index DOCUMENT_ME
   * 
   * @return the stack item
   */
  public StackItem getStackItem(int index) {
    if (alItems.size() == 0) {
      return null;
    }
    if (index < alItems.size()) {
      return alItems.get(index);
    } else if (index < (alItems.size() + alPlanned.size())) {
      return alPlanned.get(index - alItems.size());
    } else {
      return null;
    }
  }

  /**
   * Create columns configuration.
   */
  public final void prepareColumns() {
    vColNames.clear();
    idList.clear();

    // State icon (play/repeat/planned)
    vColNames.add("");
    idList.add(Const.XML_PLAY);

    // Track name
    // Note we display "title" and not "name" for this property for
    // clearness
    vColNames.add(Messages.getString("AbstractPlaylistEditorView.0"));
    idList.add(Const.XML_TRACK_NAME);

    // Album
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_ALBUM));
    idList.add(Const.XML_TRACK_ALBUM);

    // Artist
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_ARTIST));
    idList.add(Const.XML_TRACK_ARTIST);

    // Artist
    vColNames.add(Messages.getHumanPropertyName(Const.XML_ALBUM_ARTIST));
    idList.add(Const.XML_ALBUM_ARTIST);

    // Genre
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_GENRE));
    idList.add(Const.XML_TRACK_GENRE);

    // Stars
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_RATE));
    idList.add(Const.XML_TRACK_RATE);

    // Year
    vColNames.add(Messages.getHumanPropertyName(Const.XML_YEAR));
    idList.add(Const.XML_YEAR);

    // Length
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_LENGTH));
    idList.add(Const.XML_TRACK_LENGTH);

    // comments
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_COMMENT));
    idList.add(Const.XML_TRACK_COMMENT);

    // Added date
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_DISCOVERY_DATE));
    idList.add(Const.XML_TRACK_DISCOVERY_DATE);

    // Order
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_ORDER));
    idList.add(Const.XML_TRACK_ORDER);

    // Disc number
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_DISC_NUMBER));
    idList.add(Const.XML_TRACK_DISC_NUMBER);

    // Device
    vColNames.add(Messages.getHumanPropertyName(Const.XML_DEVICE));
    idList.add(Const.XML_DEVICE);

    // Directory
    vColNames.add(Messages.getHumanPropertyName(Const.XML_DIRECTORY));
    idList.add(Const.XML_DIRECTORY);

    // File name
    vColNames.add(Messages.getString("Property_filename"));
    idList.add(Const.XML_FILE);

    // Hits
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_HITS));
    idList.add(Const.XML_TRACK_HITS);

    // custom properties now
    // for tracks

    for (PropertyMetaInformation meta : TrackManager.getInstance().getCustomProperties()) {
      vColNames.add(meta.getName());
      idList.add(meta.getName());
    }
    // for files
    for (PropertyMetaInformation meta : FileManager.getInstance().getCustomProperties()) {
      vColNames.add(meta.getName());
      idList.add(meta.getName());
    }
  }

  /**
   * Fill model with data using an optional filter property.
   * 
   * @param sPropertyName DOCUMENT_ME
   * @param sPattern DOCUMENT_ME
   * @param columnsToShow DOCUMENT_ME
   */
  @Override
  public void populateModel(String sPropertyName, String sPattern, List<String> columnsToShow) {
    iRowNum = alItems.size() + alPlanned.size();
    oValues = new Object[iRowNum][iNumberStandardCols
        + TrackManager.getInstance().getCustomProperties().size()
        + FileManager.getInstance().getCustomProperties().size()];

    // For perfs, prepare columns visibility
    boolean bName = (columnsToShow != null && columnsToShow.contains(Const.XML_NAME));
    boolean bAlbum = (columnsToShow != null && columnsToShow.contains(Const.XML_ALBUM));
    boolean bArtist = (columnsToShow != null && columnsToShow.contains(Const.XML_ARTIST));
    boolean bAlbumArtist = (columnsToShow != null && columnsToShow.contains(Const.XML_ALBUM_ARTIST));
    boolean bGenre = (columnsToShow != null && columnsToShow.contains(Const.XML_GENRE));
    boolean bYear = (columnsToShow != null && columnsToShow.contains(Const.XML_YEAR));
    boolean bRate = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_RATE));
    boolean bLength = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_LENGTH));
    boolean bComment = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_COMMENT));
    boolean bDiscovery = (columnsToShow != null && columnsToShow
        .contains(Const.XML_TRACK_DISCOVERY_DATE));
    boolean bOrder = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_ORDER));
    boolean bDiscNumber = (columnsToShow != null && columnsToShow
        .contains(Const.XML_TRACK_DISC_NUMBER));
    boolean bHits = (columnsToShow != null && columnsToShow.contains(Const.XML_TRACK_HITS));
    boolean bDirectory = (columnsToShow != null && columnsToShow.contains(Const.XML_DIRECTORY));
    boolean bDevice = (columnsToShow != null && columnsToShow.contains(Const.XML_DEVICE));
    boolean bFileName = (columnsToShow != null && columnsToShow.contains(Const.XML_FILE));

    for (int iRow = 0; iRow < iRowNum; iRow++) {
      StackItem item = getStackItem(iRow);

      // Play
      if (bQueue) {
        if (item.isPlanned()) {
          oValues[iRow][0] = IconLabel.getIconLabel(JajukIcons.TRACK_FIFO_PLANNED);
        } else if (item.isRepeat()) {
          // normal file, repeated
          oValues[iRow][0] = IconLabel.getIconLabel(JajukIcons.TRACK_FIFO_REPEAT);
        } else if (iRow == QueueModel.getIndex()) {
          // Played file
          oValues[iRow][0] = IconLabel.getIconLabel(JajukIcons.TRACK_FIFO_PLAYING);
        } else {
          // normal file, not repeated
          oValues[iRow][0] = IconLabel.getIconLabel(JajukIcons.TRACK_FIFO_NORM);
        }
      } else {
        oValues[iRow][0] = IconLabel.getIconLabel(JajukIcons.TRACK_FIFO_NORM);
      }

      File bf = item.getFile();

      // Track name
      if (bName) {
        oValues[iRow][1] = bf.getTrack().getName();
      } else {
        oValues[iRow][1] = "";
      }

      // Album
      if (bAlbum) {
        oValues[iRow][2] = bf.getTrack().getAlbum().getName2();
      } else {
        oValues[iRow][2] = "";
      }

      // Artist
      if (bArtist) {
        oValues[iRow][3] = bf.getTrack().getArtist().getName2();
      } else {
        oValues[iRow][3] = "";
      }

      // AlbumArtist
      if (bAlbumArtist) {
        oValues[iRow][4] = bf.getTrack().getAlbumArtist().getName2();
      } else {
        oValues[iRow][4] = "";
      }

      // Genre
      if (bGenre) {
        oValues[iRow][5] = bf.getTrack().getGenre().getName2();
      } else {
        oValues[iRow][5] = "";
      }

      // Rate
      if (bRate) {
        oValues[iRow][6] = StarsHelper.getStarIconLabel(bf.getTrack());
      } else {
        oValues[iRow][6] = "";
      }

      // Year
      if (bYear) {
        oValues[iRow][7] = bf.getTrack().getYear();
      } else {
        oValues[iRow][7] = "";
      }

      // Length
      if (bLength) {
        oValues[iRow][8] = new Duration(bf.getTrack().getDuration());
      } else {
        oValues[iRow][8] = "";
      }

      // Comment
      if (bComment) {
        oValues[iRow][9] = bf.getTrack().getStringValue(Const.XML_TRACK_COMMENT);
      } else {
        oValues[iRow][9] = "";
      }

      // Date discovery
      if (bDiscovery) {
        oValues[iRow][10] = bf.getTrack().getDiscoveryDate();
      } else {
        oValues[iRow][10] = "";
      }

      // Order
      if (bOrder) {
        oValues[iRow][11] = bf.getTrack().getOrder();
      } else {
        oValues[iRow][11] = "";
      }

      // Disc number
      if (bDiscNumber) {
        oValues[iRow][12] = bf.getTrack().getDiscNumber();
      } else {
        oValues[iRow][12] = "";
      }

      // Device name
      if (bDevice) {
        oValues[iRow][13] = bf.getDevice().getName();
      } else {
        oValues[iRow][13] = "";
      }

      // directory name
      if (bDirectory) {
        oValues[iRow][14] = bf.getDirectory().getName();
      } else {
        oValues[iRow][14] = "";
      }

      // file name
      if (bFileName) {
        oValues[iRow][15] = bf.getName();
      } else {
        oValues[iRow][15] = "";
      }

      // Hits
      if (bHits) {
        oValues[iRow][16] = bf.getTrack().getHits();
      } else {
        oValues[iRow][16] = "";
      }

      // Custom properties now
      // for tracks
      Iterator<PropertyMetaInformation> it2 = TrackManager.getInstance().getCustomProperties()
          .iterator();
      for (int i = 0; it2.hasNext(); i++) {
        PropertyMetaInformation meta = it2.next();
        Map<String, Object> properties = bf.getTrack().getProperties();
        Object o = properties.get(meta.getName());
        if (o != null) {
          oValues[iRow][iNumberStandardCols + i] = o;
        } else {
          oValues[iRow][iNumberStandardCols + i] = meta.getDefaultValue();
        }
      }
      // for files
      it2 = FileManager.getInstance().getCustomProperties().iterator();
      // note that index lust start at custom track properties size
      for (int i = TrackManager.getInstance().getCustomProperties().size(); it2.hasNext(); i++) {
        PropertyMetaInformation meta = it2.next();
        Map<String, Object> properties = bf.getProperties();
        Object o = properties.get(meta.getName());
        if (o != null) {
          oValues[iRow][iNumberStandardCols + i] = o;
        } else {
          oValues[iRow][iNumberStandardCols + i] = meta.getDefaultValue();
        }
      }
    }
  }

  /**
   * Gets the items.
   * 
   * @return the items
   */
  public List<StackItem> getItems() {
    return this.alItems;
  }

  /**
   * Sets the items.
   * 
   * @param alItems the new items
   */
  public void setItems(List<StackItem> alItems) {
    this.alItems = alItems;
  }

  /**
   * Gets the planned.
   * 
   * @return the planned
   */
  public List<StackItem> getPlanned() {
    return this.alPlanned;
  }

  /**
   * Sets the planned.
   * 
   * @param alPlanned the new planned
   */
  public void setPlanned(List<StackItem> alPlanned) {
    this.alPlanned = alPlanned;
  }
}
