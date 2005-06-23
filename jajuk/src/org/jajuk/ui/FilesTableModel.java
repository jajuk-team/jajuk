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
import java.util.Iterator;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.SequentialMap;
import org.jajuk.util.Util;

/**
 *  Table model used for physical table view
 * @author     Bertrand Florat
 * @created    29 feb. 2004
 */
public class FilesTableModel extends JajukTableModel{
	
	
	/**
	 * Model constructor
	 * @param iColNum number of rows
	 * @param sColName columns names
	 */
	public FilesTableModel(){
		super(9);
        //Columns names
        vColNames.add(Messages.getString("Property_id"));
        vId.add("Property_id");
        
        vColNames.add(Messages.getString("Property_track"));
        vId.add("Property_track");
        
        vColNames.add(Messages.getString("Property_album"));
        vId.add("Property_album");
        
        vColNames.add(Messages.getString("Property_author"));
        vId.add("Property_author");
        
        vColNames.add(Messages.getString("Property_length"));
        vId.add("Property_length");
        
        vColNames.add(Messages.getString("Property_style"));
        vId.add("Property_style");
        
        vColNames.add(Messages.getString("Property_directory"));
        vId.add("Property_directory");
        
        vColNames.add(Messages.getString("Property_name"));
        vId.add("Property_name");
        
        vColNames.add(Messages.getString("Property_rate"));
        vId.add("Property_rate");
        
        //Custom properties now
        ArrayList alCustomProperties = FileManager.getInstance().getCustomProperties();
        Iterator it = alCustomProperties.iterator();
        while (it.hasNext()){
            String sProperty = (String)it.next();
            vColNames.add(sProperty);
            vId.add(sProperty);
        }   
        //Values
        ArrayList alFiles = FileManager.getFiles();
        ArrayList alToShow = new ArrayList(alFiles.size());
        it = alFiles.iterator();
        while ( it.hasNext()){
            File file = (File)it.next(); 
            if ( !file.shouldBeHidden()){
                alToShow.add(file);
            }
        }
        int iColNum = iNumberStandardRows + FileManager.getInstance().getCustomProperties().size();
        iRowNum = alToShow.size();
        it = alToShow.iterator();
        oValues = new Object[iRowNum][iColNum];
        bCellEditable = new boolean[iRowNum][iColNum];
        for (int iRow = 0;it.hasNext();iRow++){
            File file = (File)it.next();
            SequentialMap smProperties = file.getProperties();
            //ID
            oValues[iRow][0] = file.getId();
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
            //Length
            oValues[iRow][4] = Util.formatTimeBySec(file.getTrack().getLength(),false);
            bCellEditable[iRow][4] = false;
            //Style
            oValues[iRow][5] = file.getTrack().getStyle().getName2();
            bCellEditable[iRow][5] = true;
            //Directory
            oValues[iRow][6] = file.getDirectory().getDevice().getName();
            bCellEditable[iRow][6] = true;
            //File name
            oValues[iRow][7] = file.getName();
            bCellEditable[iRow][7] = true;
            //Rate
            oValues[iRow][8] = new Long(file.getTrack().getRate());
            bCellEditable[iRow][8] = true;
            //Custom properties now
            Iterator it2 = alCustomProperties.iterator();
            for (int i=0;it2.hasNext();i++){
                String sProperty = (String)it2.next();
                String sFormat = FileManager.getInstance().getFormat(sProperty);
                if ("Property_Format_Number".equals(sFormat)){
                    try{
                        oValues[iRow][iNumberStandardRows+i] = Double.valueOf((String)smProperties.get(sProperty));
                    }
                    catch(Exception e){ //catch wrong formats
                        oValues[iRow][iNumberStandardRows+i] = new Double(0);
                        file.setProperty(sProperty,"0");
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
                        file.setProperty(sProperty,"false");
                    }
                }
                bCellEditable[iRow][iNumberStandardRows+i] = true;
            }
        }
    }
}
