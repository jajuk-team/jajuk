/**
 * 
 */
package org.jajuk.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.base.File;
import org.jajuk.base.IPropertyable;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;

import entagged.freedb.Freedb;
import entagged.freedb.FreedbException;
import entagged.freedb.FreedbQueryResult;
import entagged.freedb.FreedbReadResult;

/**
 * @author dhalsim
 */
public class CDDBTableModel extends JajukTableModel {

    ArrayList alItems;

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

        // Filename
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_ALBUM));
        vId.add(XML_ALBUM);

        // Current Track Name
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_FILE_NAME));
        vId.add(XML_FILE_NAME);

        // Current Album Name
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK));
        vId.add(XML_TRACK);

        // Proposed Track Name
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_CDDB_TRACK));
        vId.add(XML_CDDB_TRACK);

        // custom properties now
        Iterator it = TrackManager.getInstance().getCustomProperties().iterator();
        while (it.hasNext()) {
            PropertyMetaInformation meta = (PropertyMetaInformation) it.next();
            vColNames.add(meta.getName());
            vId.add(meta.getName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.JajukTableModel#populateModel(java.lang.String, java.lang.String)
     */

    /**
     * Fill model with tracks
     */
    public void populateModel(FreedbReadResult fdbReader) {
        // Filter mounted files if needed and apply sync table with tree option if needed
        
        iRowNum = alItems.size();
        int iColNum = iNumberStandardRows;
        Iterator it = alItems.iterator();
        oValues = new Object[iRowNum][iColNum];
        oItems = new IPropertyable[iRowNum];
        bCellEditable = new boolean[iRowNum][iColNum];
        for (int iRow = 0; it.hasNext(); iRow++) {
            Track track = (Track) it.next();
            setItemAt(iRow, track);
            ArrayList file = track.getFiles();
            Iterator ifi = file.iterator();
            String filename = "";
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
