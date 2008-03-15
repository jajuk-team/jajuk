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
import java.util.LinkedHashMap;
import java.util.List;

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
    populateModel();
  }

  /**
   * Need to overwrite this method for drag and drop
   */
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
    vId.add("0");

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

    // order
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
  public synchronized void populateModel(String sPropertyName, String sPattern) {
    iRowNum = alItems.size() + alPlanned.size();
    oValues = new Object[iRowNum][iNumberStandardCols
        + TrackManager.getInstance().getCustomProperties().size()
        + FileManager.getInstance().getCustomProperties().size()];
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
        oValues[iRow][0] = new IconLabel(IconLoader.ICON_PLAYLIST_FILE, "", null, null, font,
            Messages.getString("AbstractPlaylistEditorView.21"));
      }
      // Track name
      oValues[iRow][1] = bf.getTrack().getName();
      // Album
      oValues[iRow][2] = bf.getTrack().getAlbum().getName2();
      // Author
      oValues[iRow][3] = bf.getTrack().getAuthor().getName2();
      // Style
      oValues[iRow][4] = bf.getTrack().getStyle().getName2();
      // Rate
      oValues[iRow][5] = Util.getStars(bf.getTrack());
      // Year
      oValues[iRow][6] = bf.getTrack().getYear();
      // Length
      oValues[iRow][7] = new Duration(bf.getTrack().getDuration());
      // Comment
      oValues[iRow][8] = bf.getTrack().getStringValue(XML_TRACK_COMMENT);
      // Date discovery
      oValues[iRow][9] = bf.getTrack().getDiscoveryDate();
      // show date using default local format and not technical
      // representation Order
      oValues[iRow][10] = bf.getTrack().getOrder();
      // Device name
      oValues[iRow][11] = bf.getDevice().getName();
      // directory name
      oValues[iRow][12] = bf.getDirectory().getName();
      // file name
      oValues[iRow][13] = bf.getName();
      // Hits
      oValues[iRow][14] = bf.getTrack().getHits();
      // Custom properties now
      // for tracks
      Iterator<PropertyMetaInformation> it2 = TrackManager.getInstance().getCustomProperties()
          .iterator();
      for (int i = 0; it2.hasNext(); i++) {
        PropertyMetaInformation meta = it2.next();
        LinkedHashMap<String, Object> properties = bf.getTrack().getProperties();
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
        LinkedHashMap<String, Object> properties = bf.getProperties();
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
