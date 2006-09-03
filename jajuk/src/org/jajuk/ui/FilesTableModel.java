/*
 *  Jajuk
 *  Copyright (C) 2005 Bertrand Florat
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.PatternSyntaxException;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  Table model used for physical table view
 * @author     Bertrand Florat
 * @created    29 feb. 2004
 */
public class FilesTableModel extends JajukTableModel implements ITechnicalStrings{
	
	/**
	 * Model constructor
	 * @param iColNum number of rows
	 * @param sColName columns names
	 */
	public FilesTableModel(){
		super(17);
        //Columns names
        //play column
        vColNames.add(""); //$NON-NLS-1$
        vId.add(XML_PLAY);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_NAME));
        vId.add(XML_TRACK);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_ALBUM));
        vId.add(XML_ALBUM);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_AUTHOR));
        vId.add(XML_AUTHOR);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_STYLE));
        vId.add(XML_STYLE);
     
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_RATE));
        vId.add(XML_TRACK_RATE);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_LENGTH));
        vId.add(XML_TRACK_LENGTH);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_DEVICE));
        vId.add(XML_DEVICE);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_FILE_NAME));
        vId.add(XML_NAME);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_COMMENT));
        vId.add(XML_TRACK_COMMENT);
                
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_QUALITY));
        vId.add(XML_QUALITY);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_SIZE));
        vId.add(XML_SIZE);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_ORDER));
        vId.add(XML_TRACK_ORDER);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_YEAR));
        vId.add(XML_TRACK_YEAR);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_FILE_DATE));
        vId.add(XML_FILE_DATE);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_DIRECTORY));
        vId.add(XML_DIRECTORY);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_HITS));
        vId.add(XML_TRACK_HITS);
        
        //custom properties now
        //for tracks
        Iterator it = TrackManager.getInstance().getCustomProperties().iterator();
        while (it.hasNext()){
            PropertyMetaInformation meta = (PropertyMetaInformation)it.next();
            vColNames.add(meta.getName());
            vId.add(meta.getName());
        }   
        //for files
        it = FileManager.getInstance().getCustomProperties().iterator();
        while (it.hasNext()){
            PropertyMetaInformation meta = (PropertyMetaInformation)it.next();
            vColNames.add(meta.getName());
            vId.add(meta.getName());
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
        Collection files = FileManager.getInstance().getItems();
        ArrayList alToShow = new ArrayList(files.size());
        oItems = new Item[iRowNum];
        Iterator it = files.iterator();
        while ( it.hasNext()){
            File file = (File)it.next();
            bShowWithTree =  !bSyncWithTreeOption 
                || ((hs != null && hs.size() > 0 && hs.contains(file))); //show it if no sync option or if item is in the selection
            if ( !file.shouldBeHidden() && bShowWithTree){
                alToShow.add(file);
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
                File file = (File)it.next();
                if ( sPropertyName != null && sNewPattern != null ){ //if name or value is null, means there is no filter
                    String sValue = file.getHumanValue(sPropertyName);
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
        int iColNum = iNumberStandardCols 
            + FileManager.getInstance().getCustomProperties().size()
            + TrackManager.getInstance().getCustomProperties().size();
        iRowNum = alToShow.size();
        it = alToShow.iterator();
        oValues = new Object[iRowNum][iColNum];
        oItems = new Item[iRowNum];
        bCellEditable = new boolean[iRowNum][iColNum];
        for (int iRow = 0;it.hasNext();iRow++){
            File file = (File)it.next();
            setItemAt(iRow,file);
            LinkedHashMap properties = file.getProperties();
            //Id
            oItems[iRow] = file;
            //Play
            IconLabel il = null;
            if (file.isReady()){
                il = new IconLabel(PLAY_ICON,"",null,null,null,Messages.getString("PhysicalTreeView.1")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else{
                il = new IconLabel(UNMOUNT_PLAY_ICON,"",null,null,null,Messages.getString("PhysicalTreeView.1")+Messages.getString("AbstractTableView.10")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            oValues[iRow][0] = il;
            bCellEditable[iRow][0] = false;
            //Track name
            oValues[iRow][1] = file.getTrack().getName();
            bCellEditable[iRow][1] = true;
            //Album
            oValues[iRow][2] = file.getTrack().getAlbum().getName2();
            bCellEditable[iRow][2] = true;
            //Author
            oValues[iRow][3] = file.getTrack().getAuthor().getName2();
            bCellEditable[iRow][3] = true;
            //Style
            oValues[iRow][4] = file.getTrack().getStyle().getName2();
            bCellEditable[iRow][4] = true;
            //Rate
            IconLabel ilRate = file.getTrack().getStars();
            oValues[iRow][5] = ilRate;
            bCellEditable[iRow][5] = false;
            //Length
            oValues[iRow][6] = Util.formatTimeBySec(file.getTrack().getLength(),false);
            bCellEditable[iRow][6] = false;
            //Device
            oValues[iRow][7] = file.getDirectory().getDevice().getName();
            bCellEditable[iRow][7] = false;
            //File name
            oValues[iRow][8] = file.getName();
            bCellEditable[iRow][8] = true;
            //Comment
            oValues[iRow][9] = file.getTrack().getValue(XML_TRACK_COMMENT);
            bCellEditable[iRow][9] = true;
            //Quality
            long lQuality = file.getQuality();
            oValues[iRow][10] = lQuality;
            bCellEditable[iRow][10] = false;
            //Size
            oValues[iRow][11] = ((double)file.getSize())/1048576;
            bCellEditable[iRow][11] = false;
            //Order
            oValues[iRow][12] = file.getTrack().getOrder();
            bCellEditable[iRow][12] = true;
            //year
            oValues[iRow][13] = file.getTrack().getYear();
            bCellEditable[iRow][13] = true;
            //file date
            oValues[iRow][14] = file.getDateValue(XML_FILE_DATE);
            bCellEditable[iRow][14] = false;
            //directory full path
            oValues[iRow][15] = file.getDirectory().getAbsolutePath();
            bCellEditable[iRow][15] = false;
            //Hits
            oValues[iRow][16] = file.getTrack().getHits();
            bCellEditable[iRow][16] = false;
            
            //Custom properties now
            //files
            Iterator it2 = FileManager.getInstance().getCustomProperties().iterator();
            for (int i=0;it2.hasNext();i++){
                PropertyMetaInformation meta = (PropertyMetaInformation)it2.next();
                Object o = properties.get(meta.getName());
                if (o != null){
                    oValues[iRow][iNumberStandardCols+i] = properties.get(meta.getName());    
                }
                else{
                    oValues[iRow][iNumberStandardCols+i] = meta.getDefaultValue();
                }
                //For date format, just display date conversion
                if (meta.getType().equals(Date.class)){
                    try {
                        oValues[iRow][iNumberStandardCols+i] = Util.format(oValues[iRow][iNumberStandardCols+i],meta);
                    } catch (Exception e) {
                        Log.error(e);
                    }
                }
                //Date values not editable, use properties panel instead to edit
                if (meta.getType().equals(Date.class)){
                    bCellEditable[iRow][iNumberStandardCols+i] = false;    
                }
                else{
                    bCellEditable[iRow][iNumberStandardCols+i] = true;
                }
            }   
            //tracks
            it2 = TrackManager.getInstance().getCustomProperties().iterator();
            for (int i=FileManager.getInstance().getCustomProperties().size();it2.hasNext();i++){
                PropertyMetaInformation meta = (PropertyMetaInformation)it2.next();
                Object o = properties.get(meta.getName());
                if (o != null){
                    oValues[iRow][iNumberStandardCols+i] = properties.get(meta.getName());    
                }
                else{
                    oValues[iRow][iNumberStandardCols+i] = meta.getDefaultValue();
                }
                //For date format, just display date conversion
                if (meta.getType().equals(Date.class)){
                    try {
                        oValues[iRow][iNumberStandardCols+i] = Util.format(oValues[iRow][iNumberStandardCols+i],meta);
                    } catch (Exception e) {
                        Log.error(e);
                    }
                }
                //Date values not editable, use properties panel instead to edit
                if (meta.getType().equals(Date.class)){
                    bCellEditable[iRow][iNumberStandardCols+i] = false;    
                }
                else{
                    bCellEditable[iRow][iNumberStandardCols+i] = true;
                }
            }   
        }
    }
    
    
}
