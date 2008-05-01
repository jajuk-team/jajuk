/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.helpers;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.TrackManager;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

/**
 * Playlist model used in queue view or playlist view
 */
public class PlaylistTableModel extends JajukTableModel {

  /** Values */
  public List<StackItem> alItems = new ArrayList<StackItem>(10);

  /** Values planned */
  public ArrayList<StackItem> alPlanned = new ArrayList<StackItem>(10);

  private static final long serialVersionUID = 1L;

  /** Whether this model is used by a Queue View */
  private boolean bQueue = false;

  public PlaylistTableModel(boolean bQueue) {
    super(15);
    this.bQueue = bQueue;
    setEditable(false); // table not editable
    prepareColumns();
  }

  /**
   * Need to overwrite this method for drag and drop
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
   * Return all stack items from this value to the end of selection
   * 
   * @param index
   * @return an arraylist of stack items or null if index is out of bounds
   */
  public ArrayList<StackItem> getItemsFrom(int index) {
    if (index < alItems.size()) {
      return new ArrayList<StackItem>(alItems.subList(index, alItems.size()));
    } else {
      return null;
    }
  }

  /**
   * Return right stack item in normal or planned stacks
   * 
   * @param index
   * @return
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
   * Create columns configuration
   * 
   */
  public synchronized void prepareColumns() {
    vColNames.clear();
    vId.clear();

    // State icon (play/repeat/planned)
    vColNames.add("");
    vId.add(XML_PLAY);

    // Track name
    // Note we display "title" and not "name" for this property for
    // clearness
    vColNames.add(Messages.getString("AbstractPlaylistEditorView.0"));
    vId.add(XML_TRACK_NAME);

    // Album
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_ALBUM));
    vId.add(XML_TRACK_ALBUM);

    // Author
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_AUTHOR));
    vId.add(XML_TRACK_AUTHOR);

    // Style
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_STYLE));
    vId.add(XML_TRACK_STYLE);

    // Stars
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_RATE));
    vId.add(XML_TRACK_RATE);

    // Year
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_YEAR));
    vId.add(XML_YEAR);

    // Length
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_LENGTH));
    vId.add(XML_TRACK_LENGTH);

    // comments
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_COMMENT));
    vId.add(XML_TRACK_COMMENT);

    // Added date
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_DISCOVERY_DATE));
    vId.add(XML_TRACK_DISCOVERY_DATE);

    // Order
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_ORDER));
    vId.add(XML_TRACK_ORDER);

    // Device
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_DEVICE));
    vId.add(XML_DEVICE);

    // Directory
    vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_DIRECTORY));
    vId.add(XML_DIRECTORY);

    // File name
    vColNames.add(Messages.getString("Property_filename"));
    vId.add(XML_FILE);

    // Hits
    vColNames.add(Messages.getString("Property_hits"));
    vId.add(XML_TRACK_HITS);

    // custom properties now
    // for tracks

    for (PropertyMetaInformation meta : TrackManager.getInstance().getCustomProperties()) {
      vColNames.add(meta.getName());
      vId.add(meta.getName());
    }
    // for files
    for (PropertyMetaInformation meta : FileManager.getInstance().getCustomProperties()) {
      vColNames.add(meta.getName());
      vId.add(meta.getName());
    }
  }

  /**
   * Fill model with data using an optional filter property
   */
  @Override
  public synchronized void populateModel(String sPropertyName, String sPattern,
      ArrayList<String> columnsToShow) {
    iRowNum = alItems.size() + alPlanned.size();
    oValues = new Object[iRowNum][iNumberStandardCols
        + TrackManager.getInstance().getCustomProperties().size()
        + FileManager.getInstance().getCustomProperties().size()];

    // For perfs, prepare columns visibility
    boolean bName = (columnsToShow != null && columnsToShow.contains(XML_NAME));
    boolean bAlbum = (columnsToShow != null && columnsToShow.contains(XML_ALBUM));
    boolean bAuthor = (columnsToShow != null && columnsToShow.contains(XML_AUTHOR));
    boolean bStyle = (columnsToShow != null && columnsToShow.contains(XML_STYLE));
    boolean bYear = (columnsToShow != null && columnsToShow.contains(XML_YEAR));
    boolean bRate = (columnsToShow != null && columnsToShow.contains(XML_TRACK_RATE));
    boolean bLength = (columnsToShow != null && columnsToShow.contains(XML_TRACK_LENGTH));
    boolean bComment = (columnsToShow != null && columnsToShow.contains(XML_TRACK_COMMENT));
    boolean bDiscovery = (columnsToShow != null && columnsToShow.contains(XML_TRACK_DISCOVERY_DATE));
    boolean bOrder = (columnsToShow != null && columnsToShow.contains(XML_TRACK_ORDER));
    boolean bHits = (columnsToShow != null && columnsToShow.contains(XML_TRACK_HITS));
    boolean bDirectory = (columnsToShow != null && columnsToShow.contains(XML_DIRECTORY));
    boolean bDevice = (columnsToShow != null && columnsToShow.contains(XML_DEVICE));
    boolean bFileName = (columnsToShow != null && columnsToShow.contains(XML_FILE));
      
    for (int iRow = 0; iRow < iRowNum; iRow++) {
      boolean bPlanned = false;
      Font font = null;
      StackItem item = getStackItem(iRow);
      if (item.isPlanned()) { // it is a planned file
        bPlanned = true;
        font = FontManager.getInstance().getFont(JajukFont.PLANNED);
      }
      File bf = item.getFile();

      // Play
      if (bQueue) {
        if (bPlanned) {
          oValues[iRow][0] = new IconLabel(IconLoader.ICON_TRACK_FIFO_PLANNED, "", null, null,
              font, Messages.getString("AbstractPlaylistEditorView.20"));
        } else {
          if (item.isRepeat()) {
            // normal file, repeated
            oValues[iRow][0] = new IconLabel(IconLoader.ICON_TRACK_FIFO_REPEAT, "", null, null,
                font, Messages.getString("AbstractPlaylistEditorView.19"));
          } else {
            // normal file, not repeated
            oValues[iRow][0] = new IconLabel(IconLoader.ICON_TRACK_FIFO_NORM, "", null, null, font,
                Messages.getString("AbstractPlaylistEditorView.18"));
          }
        }
      } else {
        oValues[iRow][0] = new IconLabel(IconLoader.ICON_TRACK_FIFO_NORM, "", null, null, font,
                Messages.getString("AbstractPlaylistEditorView.18"));
      }
      
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

      // Author
      if (bAuthor) {
        oValues[iRow][3] = bf.getTrack().getAuthor().getName2();
      } else {
        oValues[iRow][3] = "";
      }

      // Style
      if (bStyle) {
        oValues[iRow][4] = bf.getTrack().getStyle().getName2();
      } else {
        oValues[iRow][4] = "";
      }

      // Rate
      if (bRate) {
        oValues[iRow][5] = Util.getStars(bf.getTrack());
      } else {
        oValues[iRow][5] = "";
      }

      // Year
      if (bYear) {
        oValues[iRow][6] = bf.getTrack().getYear();
      } else {
        oValues[iRow][6] = "";
      }

      // Length
      if (bLength) {
        oValues[iRow][7] = new Duration(bf.getTrack().getDuration());
      } else {
        oValues[iRow][7] = "";
      }

      // Comment
      if (bComment) {
        oValues[iRow][8] = bf.getTrack().getStringValue(XML_TRACK_COMMENT);
      } else {
        oValues[iRow][8] = "";
      }

      // Date discovery
      if (bDiscovery) {
        oValues[iRow][9] = bf.getTrack().getDiscoveryDate();
      } else {
        oValues[iRow][9] = "";
      }

      // Order
      if (bOrder) {
        oValues[iRow][10] = bf.getTrack().getOrder();
      } else {
        oValues[iRow][10] = "";
      }

      // Device name
      if (bDevice) {
        oValues[iRow][11] = bf.getDevice().getName();
      } else {
        oValues[iRow][11] = "";
      }

      // directory name
      if (bDirectory) {
        oValues[iRow][12] = bf.getDirectory().getName();
      } else {
        oValues[iRow][12] = "";
      }

      // file name
      if (bFileName) {
        oValues[iRow][13] = bf.getName();
      } else {
        oValues[iRow][13] = "";
      }

      // Hits
      if (bHits) {
        oValues[iRow][14] = bf.getTrack().getHits();
      } else {
        oValues[iRow][14] = "";
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
}
