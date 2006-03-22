/*
 *  Jajuk
 *  Copyright (C) 2006 Administrateur
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

package org.jajuk.ui.action;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Track;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

public class RefactorAction implements ITechnicalStrings{
    
    ArrayList <File> alFiles;
    String filename;      
    
    public RefactorAction (ArrayList<File> al){
        alFiles = al;
        if (Boolean.valueOf(ConfigurationManager.getProperty(CONF_CONFIRMATIONS_REFACTOR_FILES)).booleanValue()){
            int iResu = Messages.getChoice(Messages.getString("Confirmation_refactor_files"),JOptionPane.INFORMATION_MESSAGE);  //$NON-NLS-1$ //$NON-NLS-2$
            if (iResu != JOptionPane.YES_OPTION){
            	Util.stopWaiting();
            	return;                       
            }
        } 
        new Thread() {
            public void run() {                
                refactor();
            }
        }.start();
        Util.stopWaiting();   
        ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH));
    }
    
    class AudioFileFilter implements FilenameFilter,ITechnicalStrings{
    	
    	public boolean accept (java.io.File dir, String name){
    		return !(name.endsWith(EXT_MP3) ||    				
    				name.endsWith(EXT_OGG) ||
    				name.endsWith(EXT_AU) ||
    				name.endsWith(EXT_AIFF) ||
    				name.endsWith(EXT_FLAC));    		
    	}
    }
    
    public void refactor(){               
        Iterator it = alFiles.iterator();
        
        while (it.hasNext()){
            File fCurrent = (File) it.next();
            Track tCurrent = fCurrent.getTrack(); 
            filename = ConfigurationManager.getProperty(CONF_REFACTOR_PATTERN);
            filename = filename.replace("%a",tCurrent.getAuthor().getName2());
            filename = filename.replace("%s",tCurrent.getStyle().getName2());
            filename = filename.replace("%A",tCurrent.getAlbum().getName2());                     
            if (tCurrent.getOrder() < 10) {
                filename = filename.replace("%n","0"+tCurrent.getOrder());
            } else {
                filename = filename.replace("%n",tCurrent.getOrder()+"");
            }
            filename = filename.replace("%t",tCurrent.getName());
            
            if (tCurrent.getYear() != 0){
                filename = filename.replace("%y",tCurrent.getYear()+"");
            } else {
                filename = filename.replace("%y - ","");
            }                      
            filename += "."+tCurrent.getType().getExtension();
            
            // Compute the new filename
            java.io.File fOld = fCurrent.getIO();            
            java.io.File fNew = new java.io.File(fCurrent.getDevice().getUrl()+"/"+filename);
            java.io.File dOld = new java.io.File(fOld.getParent());            
            java.io.File fCover = tCurrent.getAlbum().getCoverFile();
            java.io.File dir = fNew.getParentFile();
            
            String sTarget = dir.getPath();
            String sList[];
            // Create Directories
            while (!dir.getPath().equals(fCurrent.getDevice().getUrl())){
            	
            	if (dir.isDirectory()){
            		dir.mkdirs();
            		break;
            	} else {
            		dir = dir.getParentFile();
            	}
            }
            
            
            // Move file and related cover but save old Directory pathname for futur deletion            
            if (fCover != null) {
            	fCover.renameTo(new java.io.File(fNew.getParent()+"/"+fCover.getName()));
            }
            
            //boolean bState = fOld.renameTo(fNew);            
            
            boolean bState = true;
            // Put some message
            if (bState){
            	InformationJPanel.getInstance().setMessage("File " + Messages.getString(fNew.getAbsolutePath()+" moved"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
            } else {
            	InformationJPanel.getInstance().setMessage("File " + Messages.getString(fNew.getAbsolutePath()+" could not be moved !"),InformationJPanel.ERROR); //$NON-NLS-1$
            }                       
                    
            // See if old directory contain other files and move them
            java.io.File[] list = dOld.listFiles(new AudioFileFilter());
            if (list != null && list.length != 0){
            	for (java.io.File f:list){
            		f.renameTo(new java.io.File(fNew.getParent()+"/"+f.getName()));
            	}
            } else if (list.length == 0){
            	dOld.delete();
            }                                            
            
            // Debug log
            Log.debug("[Refactoring] {{"+ fNew.getAbsolutePath() +"}} Success ? " + bState);            
            
        }       
    }
}
