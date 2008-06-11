/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision$$
 */
package org.jajuk.ui.helpers;

import entagged.freedb.FreedbReadResult;

import java.util.Iterator;
import java.util.List;

import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.services.cddb.CDDBTrack;
import org.jajuk.util.Messages;

public class CDDBTableModel extends JajukTableModel {

  private static final long serialVersionUID = 1L;

  List<CDDBTrack> cddbTracks;

  /**
   * Model constructor
   * 
   * @param iColNum
   *          number of rows
   * @param sColName
   *          columns names
   */
  public CDDBTableModel(List<CDDBTrack> alItems) {
    super(5);
    this.cddbTracks = alItems;

    // Current Album title
    vColNames.add(Messages.getString("CDDBWizard.3"));
    idList.add("CDDBWizard.1");

    // Current Track title
    vColNames.add(Messages.getString("CDDBWizard.2"));
    idList.add("CDDBWizard.3");

    // Proposed Track Name
    vColNames.add(Messages.getString("CDDBWizard.4"));
    idList.add("CDDBWizard.4");
  }

  /**
   * Fill model with tracks
   */
  public void populateModel(FreedbReadResult fdbReader) {
    iRowNum = cddbTracks.size();
    int iColNum = iNumberStandardCols;
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];
    Iterator<CDDBTrack> it = cddbTracks.iterator();
    for (int iRow = 0; it.hasNext(); iRow++) {
      Track track = it.next().getTrack();
      setItemAt(iRow, track);
      // Id
      oItems[iRow] = track;
      // File name
      oValues[iRow][0] = track.getAlbum().getName2();
      bCellEditable[iRow][0] = false;
      // Album
      oValues[iRow][1] = track.getName();
      bCellEditable[iRow][1] = false;
      // Author
      oValues[iRow][2] = fdbReader.getTrackTitle(iRow);
      bCellEditable[iRow][2] = false;
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.helpers.JajukTableModel#populateModel(java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public void populateModel(String property, String pattern, List<String> columnsToShow) {
    // TODO Auto-generated method stub
    
  }
  
}
