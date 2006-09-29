/*
 *  Jajuk
 *  Copyright (C) 2006 Erwan Richard
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
package org.jajuk.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CDDBWizard.CDDBTrack;

import entagged.freedb.FreedbReadResult;

/**
 * @author Erwan Richard
 * @created 15 december 2005
 */

public class CDDBTableModel extends JajukTableModel {

    private static final long serialVersionUID = 1L;
    
    ArrayList<CDDBTrack> alItems;

    /**
     * Model constructor
     * 
     * @param iColNum
     *            number of rows
     * @param sColName
     *            columns names
     */
    public CDDBTableModel(ArrayList alItems) {
        super(5);
        this.alItems = alItems;

        // Current Album title 
        vColNames.add(Messages.getString("CDDBWizard.3")); //$NON-NLS-1$
        vId.add("CDDBWizard.1"); //$NON-NLS-1$

        // Filename
        vColNames.add(Messages.getString("CDDBWizard.1")); //$NON-NLS-1$
        vId.add("CDDBWizard.2"); //$NON-NLS-1$

        // Current Track title
        vColNames.add(Messages.getString("CDDBWizard.2")); //$NON-NLS-1$
        vId.add("CDDBWizard.3"); //$NON-NLS-1$

        // Proposed Track Name
        vColNames.add(Messages.getString("CDDBWizard.4")); //$NON-NLS-1$
        vId.add("CDDBWizard.4"); //$NON-NLS-1$
    }

    /**
     * Fill model with tracks
     */
    public void populateModel(FreedbReadResult fdbReader) {               
        iRowNum = alItems.size();
        int iColNum = iNumberStandardCols;
        Iterator it = alItems.iterator();
        oValues = new Object[iRowNum][iColNum];
        oItems = new Item[iRowNum];
        bCellEditable = new boolean[iRowNum][iColNum];
        for (int iRow = 0; it.hasNext(); iRow++) {
            Track track = ((CDDBTrack) it.next()).track;
            setItemAt(iRow, track);
            ArrayList file = track.getFiles();
            Iterator ifi = file.iterator();
            String filename = ""; //$NON-NLS-1$
            while(ifi.hasNext()){
                File f = (File) ifi.next();
                filename = f.getName();
                if (filename!=null) break;
            }                       
                        
            // Id
            oItems[iRow] = track;
            //  File name            
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
    public void populateModel(String sProperty, String sPattern) {
    }
}
