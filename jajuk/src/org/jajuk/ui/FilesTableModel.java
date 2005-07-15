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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.PatternSyntaxException;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.IPropertyable;
import org.jajuk.base.ObservationManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.views.PhysicalTableView;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

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
		super(12);
        //Columns names
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_NAME));
        vId.add(XML_TRACK);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_ALBUM));
        vId.add(XML_ALBUM);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_AUTHOR));
        vId.add(XML_AUTHOR);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_STYLE));
        vId.add(XML_STYLE);
     
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_LENGTH));
        vId.add(XML_TRACK_LENGTH);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_DEVICE));
        vId.add(XML_DEVICE);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_FILE_NAME));
        vId.add(XML_NAME);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_COMMENT));
        vId.add(XML_COMMENT);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_RATE));
        vId.add(XML_TRACK_RATE);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_QUALITY));
        vId.add(XML_QUALITY);
        
        vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_SIZE));
        vId.add(XML_SIZE);
        
         vColNames.add(Messages.getString(PROPERTY_SEPARATOR+XML_TRACK_ORDER));
        vId.add(XML_TRACK_ORDER);
        
        //Custom properties now
        Iterator it = FileManager.getInstance().getCustomProperties().iterator();
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
        ArrayList alFiles = FileManager.getInstance().getItems();
        ArrayList alToShow = new ArrayList(alFiles.size());
        oItems = new IPropertyable[iRowNum];
        Iterator it = alFiles.iterator();
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
        int iColNum = iNumberStandardRows + FileManager.getInstance().getCustomProperties().size();
        iRowNum = alToShow.size();
        it = alToShow.iterator();
        oValues = new Object[iRowNum][iColNum];
        oItems = new IPropertyable[iRowNum];
        bCellEditable = new boolean[iRowNum][iColNum];
        for (int iRow = 0;it.hasNext();iRow++){
            File file = (File)it.next();
            LinkedHashMap properties = file.getProperties();
            //Id
            oItems[iRow] = file;
            //Track name
            oValues[iRow][0] = file.getTrack().getName();
            bCellEditable[iRow][0] = true;
            //Album
            oValues[iRow][1] = file.getTrack().getAlbum().getName2();
            bCellEditable[iRow][1] = true;
            //Author
            oValues[iRow][2] = file.getTrack().getAuthor().getName2();
            bCellEditable[iRow][2] = true;
            //Style
            oValues[iRow][3] = file.getTrack().getStyle().getName2();
            bCellEditable[iRow][3] = true;
            //Length
            oValues[iRow][4] = Util.formatTimeBySec(file.getTrack().getLength(),false);
            bCellEditable[iRow][4] = false;
            //Device
            oValues[iRow][5] = file.getDirectory().getDevice().getName();
            bCellEditable[iRow][5] = true;
            //File name
            oValues[iRow][6] = file.getName();
            bCellEditable[iRow][6] = true;
            //Comment
            oValues[iRow][7] = file.getTrack().getValue(XML_COMMENT);
            bCellEditable[iRow][7] = true;
            //Rate
            oValues[iRow][8] = new Long(file.getTrack().getRate());
            bCellEditable[iRow][8] = true;
            //Quality
            String size = file.getQuality();
            if (UNKNOWN_QUALITY.equals(size)){
                size = "0";
            }
            oValues[iRow][9] = new Integer(size);
            bCellEditable[iRow][9] = false;
            //Size
            oValues[iRow][10] = new Integer((int)(file.getSize()>>20));
            bCellEditable[iRow][10] = false;
            //Order
            oValues[iRow][11] = file.getTrack().getOrder2();
            bCellEditable[iRow][11] = true;
            //Custom properties now
            Iterator it2 = FileManager.getInstance().getCustomProperties().iterator();
            for (int i=0;it2.hasNext();i++){
                String sProperty = (String)it2.next();
                String sFormat = FileManager.getInstance().getFormat(sProperty);
                if ("Property_Format_Number".equals(sFormat)){
                    try{
                        oValues[iRow][iNumberStandardRows+i] = Double.valueOf((String)properties.get(sProperty));
                    }
                    catch(Exception e){ //catch wrong formats
                        oValues[iRow][iNumberStandardRows+i] = new Double(0);
                        file.setProperty(sProperty,"0");
                    }
                }
                else if ("Property_Format_String".equals(sFormat)){
                    oValues[iRow][iNumberStandardRows+i] = (String)properties.get(sProperty);
                }
                else if ("Property_Format_Boolean".equals(sFormat)){
                    try{
                        oValues[iRow][iNumberStandardRows+i] = Boolean.valueOf((String)properties.get(sProperty));
                    }
                    catch(Exception e){ //catch wrong formats
                        oValues[iRow][iNumberStandardRows+i] = Boolean.FALSE;
                        file.setProperty(sProperty,"false");
                    }
                }
                bCellEditable[iRow][iNumberStandardRows+i] = true;
            }
        }
    }
    
     /* (non-Javadoc)
     * @see org.jajuk.ui.JajukTableModel#getEditableMode()
     */
    @Override
    public boolean isEditable() {
        return PhysicalTableView.getInstance().isEditable();
    }
    
}
