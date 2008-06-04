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
package org.jajuk.ui.wizard;

import entagged.freedb.FreedbReadResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.wizard.CDDBWizard.CDDBTrack;
import org.jajuk.util.Messages;

public class CDDBTableModel extends JajukTableModel {

  private static final long serialVersionUID = 1L;

  ArrayList<CDDBTrack> alItems;

  /**
   * Model constructor
   * 
   * @param iColNum
   *          number of rows
   * @param sColName
   *          columns names
   */
  public CDDBTableModel(ArrayList<CDDBTrack> alItems) {
    super(5);
    this.alItems = alItems;

    // Current Album title
    vColNames.add(Messages.getString("CDDBWizard.3"));
    vId.add("CDDBWizard.1");

    // Filename
    vColNames.add(Messages.getString("CDDBWizard.1"));
    vId.add("CDDBWizard.2");

    // Current Track title
    vColNames.add(Messages.getString("CDDBWizard.2"));
    vId.add("CDDBWizard.3");

    // Proposed Track Name
    vColNames.add(Messages.getString("CDDBWizard.4"));
    vId.add("CDDBWizard.4");
  }

  /**
   * Fill model with tracks
   */
  public void populateModel(FreedbReadResult fdbReader) {
    iRowNum = alItems.size();
    int iColNum = iNumberStandardCols;
    Iterator<CDDBTrack> it = alItems.iterator();
    oValues = new Object[iRowNum][iColNum];
    oItems = new Item[iRowNum];
    bCellEditable = new boolean[iRowNum][iColNum];
    for (int iRow = 0; it.hasNext(); iRow++) {
      Track track = ((CDDBTrack) it.next()).track;
      setItemAt(iRow, track);
      List<File> file = track.getFiles();
      Iterator<File> ifi = file.iterator();
      String filename = "";
      while (ifi.hasNext()) {
        File f = (File) ifi.next();
        filename = f.getName();
        if (filename != null)
          break;
      }

      // Id
      oItems[iRow] = track;
      // File name
      oValues[iRow][0] = track.getAlbum().getName2();
      bCellEditable[iRow][0] = false;
      // Track name
      oValues[iRow][1] = filename;
      bCellEditable[iRow][1] = false;
      // Album
      oValues[iRow][2] = track.getName();
      bCellEditable[iRow][2] = false;
      // Author
      oValues[iRow][3] = fdbReader.getTrackTitle(iRow);
      bCellEditable[iRow][3] = false;
    }
  }

  @Override
  public void populateModel(String sProperty, String sPattern, ArrayList<String> columnsToShow) {
  }
}
