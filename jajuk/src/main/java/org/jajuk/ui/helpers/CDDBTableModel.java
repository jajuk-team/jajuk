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
 *  
 */
package org.jajuk.ui.helpers;

import entagged.freedb.FreedbReadResult;

import java.util.Iterator;
import java.util.List;

import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.services.cddb.CDDBTrack;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;

/**
 * DOCUMENT_ME.
 */
public class CDDBTableModel extends JajukTableModel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Model constructor.
   */
  public CDDBTableModel() {
    super(8);

    // Current Album title
    vColNames.add(Messages.getString("CDDBWizard.3"));
    idList.add("CDDBWizard.1");

    // Filename
    vColNames.add(Messages.getString("CDDBWizard.1"));
    idList.add("CDDBWizard.2");

    // Current Track title
    vColNames.add(Messages.getString("CDDBWizard.2"));
    idList.add("CDDBWizard.3");

    // Proposed Track Name
    vColNames.add(Messages.getString("CDDBWizard.4"));
    idList.add("CDDBWizard.4");

    // Proposed Track Artist
    vColNames.add(Messages.getHumanPropertyName(Const.XML_ARTIST));
    idList.add(Const.XML_ARTIST);

    // Proposed Track genre
    vColNames.add(Messages.getHumanPropertyName(Const.XML_GENRE));
    idList.add(Const.XML_GENRE);

    // Proposed Track year
    vColNames.add(Messages.getHumanPropertyName(Const.XML_YEAR));
    idList.add(Const.XML_YEAR);

    // Proposed Track number
    vColNames.add(Messages.getHumanPropertyName(Const.XML_TRACK_ORDER));
    idList.add(Const.XML_TRACK_ORDER);
  }

  /**
   * Fill model with tracks.
   * 
   * @param currentTracks DOCUMENT_ME
   * @param fdbReader DOCUMENT_ME
   */
  public void populateModel(List<CDDBTrack> currentTracks, FreedbReadResult fdbReader) {
    iRowNum = currentTracks.size();
    int iColNum = iNumberStandardCols;
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];
    Iterator<CDDBTrack> it = currentTracks.iterator();
    for (int iRow = 0; it.hasNext(); iRow++) {
      Track track = it.next().getTrack();
      setItemAt(iRow, track);
      // Id
      oItems[iRow] = track;
      // Album name
      oValues[iRow][0] = track.getAlbum().getName2();
      bCellEditable[iRow][0] = false;
      // files name
      oValues[iRow][1] = UtilString.getLimitedString(track.getFilesString(), 40);
      bCellEditable[iRow][1] = false;
      // Current Track name
      oValues[iRow][2] = track.getName();
      bCellEditable[iRow][2] = false;
      // Proposed track name
      oValues[iRow][3] = fdbReader.getTrackTitle(iRow);
      bCellEditable[iRow][3] = false;
      // Proposed track artist
      oValues[iRow][4] = fdbReader.getArtist();
      bCellEditable[iRow][4] = false;
      // Proposed track genre
      oValues[iRow][5] = fdbReader.getGenre();
      bCellEditable[iRow][5] = false;
      // Proposed track year
      oValues[iRow][6] = fdbReader.getYear();
      bCellEditable[iRow][6] = false;
      // Proposed track order
      oValues[iRow][7] = fdbReader.getTrackNumber(iRow);
      bCellEditable[iRow][7] = false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.helpers.JajukTableModel#populateModel(java.lang.String,
   *      java.lang.String, java.util.List)
   */
  @Override
  public void populateModel(String property, String pattern, List<String> columnsToShow) {
    // Doesn't apply here
  }

}
