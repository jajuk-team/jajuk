/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
 * $Revision$
 */

package org.jajuk.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.PatternSyntaxException;

import org.jajuk.base.ObservationManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  Table model used for logical table view
 * @author     Bertrand Florat
 * @created    29 feb. 2004
 */
public class TracksTableModel extends JajukTableModel{
	
	/**
	 * Model constructor
	 * @param iColNum number of rows
	 * @param sColName columns names
	 */
	public TracksTableModel(){
	    super(8);
        
        //Columns names
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_NAME));
        vId.add(XML_NAME);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_ALBUM));
        vId.add(XML_ALBUM);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_AUTHOR));
        vId.add(XML_AUTHOR);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_STYLE));
        vId.add(XML_STYLE);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_LENGTH));
        vId.add(XML_TRACK_LENGTH);
                
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_COMMENT));
        vId.add(XML_COMMENT);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_RATE));
        vId.add(XML_TRACK_RATE);
    
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_ADDED));
        vId.add(XML_TRACK_ADDED);
    
        //custom properties now
        Iterator it = TrackManager.getInstance().getCustomProperties().iterator();
        while (it.hasNext()){
            String sProperty = (String)it.next();
            vColNames.add(sProperty);
            vId.add(sProperty);
        }
    }
    
     /**
     * Fill model with data using an optionnal filter property and pattern
     */
    public void populateModel(String sPropertyName,String sPattern){
        //Filter mounted files if needed and apply sync table with tree option if needed
        boolean bShowWithTree = true;
        HashSet hs = (HashSet)ObservationManager.getDetailLastOccurence(EVENT_SYNC_TREE_TABLE,DETAIL_SELECTION);//look at selection
        boolean bSyncWithTreeOption = ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE);
        ArrayList alTracks = TrackManager.getInstance().getSortedTracks();
        ArrayList alToShow = new ArrayList(alTracks.size());
        Iterator it = alTracks.iterator();
        while ( it.hasNext()){
            Track track = (Track)it.next();
            bShowWithTree =  !bSyncWithTreeOption || ((hs != null && hs.size() > 0 
                    && hs.contains(track))); //show it if no sync option or if item is in the selection
            if ( !track.shouldBeHidden() && bShowWithTree){
                alToShow.add(track);
            }
        }
        //Filter values using given pattern
        if (sPropertyName != null && sPattern != null){ //null means no filtering
            it = alToShow.iterator();
            //Prepare filter pattern
            String sNewPattern = sPattern;
            if ( !ConfigurationManager.getBoolean(CONF_REGEXP) && sNewPattern != null){ //do we use regular expression or not? if not, we allow user to use '*'
                sNewPattern = sNewPattern.replaceAll("\\*",".*"); //$NON-NLS-1$ //$NON-NLS-2$
                sNewPattern = ".*"+sNewPattern+".*"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else if ("".equals(sNewPattern)){//in regexp mode, if none selection, display all rows //$NON-NLS-1$
                sNewPattern = ".*"; //$NON-NLS-1$
            }
            while (it.hasNext()){
                Track track = (Track)it.next();
                if ( sPropertyName != null && sNewPattern != null ){ //if name or value is null, means there is no filter
                    String sValue = track.getHumanValue(sPropertyName);
                    if ( sValue == null){ //try to filter on a unknown property, don't take this file
                        continue;
                    }
                    else { 
                        boolean bMatch = false;
                        try{  //test using regular expressions
                            bMatch = sValue.toLowerCase().matches(sNewPattern.toLowerCase());  // test if the file property contains this property value (ignore case)
                        }
                        catch(PatternSyntaxException pse){ //wrong pattern syntax
                            bMatch = false;
                        }
                        if (!bMatch){
                            it.remove(); //no? remove it
                        }
                    }   
                }
            }
        }
        it = alToShow.iterator();
        int iColNum = iNumberStandardRows + 
            TrackManager.getInstance().getCustomProperties().size();
        iRowNum = alToShow.size();
        it = alToShow.iterator();
        oValues = new Object[iRowNum][iColNum];
        bCellEditable = new boolean[iRowNum][iColNum];
        for (int iRow = 0;it.hasNext();iRow++){
            Track track = (Track)it.next();
            LinkedHashMap smProperties = track.getProperties();
            //Track name
            oValues[iRow][0] = track.getName();
            bCellEditable[iRow][0] = true;
            //Album
            oValues[iRow][1] = track.getAlbum().getName2();
            bCellEditable[iRow][1] = true;
            //Author
            oValues[iRow][2] = track.getAuthor().getName2();
            bCellEditable[iRow][2] = true;
            //Style
            oValues[iRow][3] = track.getStyle().getName2();
            bCellEditable[iRow][3] = true;
            //Length
            oValues[iRow][4] = Util.formatTimeBySec(track.getLength(),false);
            bCellEditable[iRow][4] = false;
            //Comment
            oValues[iRow][5] = track.getValue(XML_COMMENT);
            bCellEditable[iRow][5] = true;
            //Rate
            oValues[iRow][6] = new Long(track.getRate());
            bCellEditable[iRow][6] = true;
            //Date discovery
            try {
                oValues[iRow][7] = new SimpleDateFormat(DATE_FILE).parse(track.getAdditionDate());;
                bCellEditable[iRow][7] = false;
            }
            catch (ParseException e1) {
                Log.error(e1);
            }
            //Custom properties now
            Iterator it2 = TrackManager.getInstance().getCustomProperties().iterator();
            for (int i=0;it2.hasNext();i++){
                String sProperty = (String)it2.next();
                String sFormat = TrackManager.getInstance().getFormat(sProperty);
                if ("Property_Format_Number".equals(sFormat)){
                    try{
                        oValues[iRow][iNumberStandardRows+i] = Double.valueOf((String)smProperties.get(sProperty));
                    }
                    catch(Exception e){ //catch wrong formats
                        oValues[iRow][iNumberStandardRows+i] = new Double(0);
                        track.setProperty(sProperty,"0");
                    }
                }
                else if ("Property_Format_String".equals(sFormat)){
                    oValues[iRow][iNumberStandardRows+i] = (String)smProperties.get(sProperty);
                }
                else if ("Property_Format_Boolean".equals(sFormat)){
                    try{
                        oValues[iRow][iNumberStandardRows+i] = Boolean.valueOf((String)smProperties.get(sProperty));
                    }
                    catch(Exception e){ //catch wrong formats
                        oValues[iRow][iNumberStandardRows+i] = Boolean.FALSE;
                        track.setProperty(sProperty,"false");
                    }
                }
                bCellEditable[iRow][iNumberStandardRows+i] = true;
            }
        }
    }
  	
}
