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

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jajuk.base.AuthorManager;
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
    
    class DirectorieFilter implements FileFilter{
    	
    	public boolean accept (java.io.File pathname){
    		return pathname.isDirectory();
    	}
    }
    
    public void refactor(){               
        Iterator it = alFiles.iterator();
        
        while (it.hasNext()){
            File fCurrent = (File) it.next();
            Track tCurrent = fCurrent.getTrack(); 
            filename = ConfigurationManager.getProperty(CONF_REFACTOR_PATTERN).toLowerCase();
            filename = filename.replace("%artist",tCurrent.getAuthor().getName2());
            filename = filename.replace("%genre",tCurrent.getStyle().getName2());
            filename = filename.replace("%album",tCurrent.getAlbum().getName2());                     
            if (tCurrent.getOrder() < 10) {
                filename = filename.replace("%track#","0"+tCurrent.getOrder());
            } else {
                filename = filename.replace("%track#",tCurrent.getOrder()+"");
            }
            filename = filename.replace("%track",tCurrent.getName());
            
            if (tCurrent.getYear() != 0){
                filename = filename.replace("%year",tCurrent.getYear()+"");
            } else {
                filename = filename.replace("%year","");
            }                      
            filename += "."+tCurrent.getType().getExtension();
            
            
            // Compute the new filename
            java.io.File fOld = fCurrent.getIO();         
            String sRoot = fCurrent.getDevice().getUrl();
            
            // Check if directories exists, and if not create them
            String sPathname = mkdirsIgnoreCase(sRoot,filename);                                                                                   
            java.io.File fNew = new java.io.File(sPathname);
            boolean bDirCreated = fNew.getParentFile().mkdirs();
            
            // Move file and related cover but save old Directory pathname for futur deletion
            java.io.File fCover = tCurrent.getAlbum().getCoverFile();
            if (fCover != null) {
            	fCover.renameTo(new java.io.File(fNew.getParent()+"/"+fCover.getName()));
            }
            
            Util.isValidFileName(fNew.getParentFile());
            boolean bState = fOld.renameTo(fNew);            
            
           
            // Put some message
            if (bState){
            	InformationJPanel.getInstance().setMessage("File " + Messages.getString(fNew.getAbsolutePath()+" moved"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
            } else {
            	InformationJPanel.getInstance().setMessage("File " + Messages.getString(fNew.getAbsolutePath()+" could not be moved !"),InformationJPanel.ERROR); //$NON-NLS-1$
            }                       
                    
            // See if old directory contain other files and move them
            java.io.File dOld = fOld.getParentFile();
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
    
    public String mkdirsIgnoreCase(String sRoot, String sPathname){
    	
    	java.io.File fioRoot = new java.io.File(sRoot);
    	java.io.File[] fioList = fioRoot.listFiles(new DirectorieFilter());
    	
    	String[] sPaths = sPathname.split("/");
    	String sReturn = sRoot;
    	for (int i=0;i<sPaths.length-1;i++){
    		String sPath = sPaths[i];
    		boolean bool = false;
    		for (java.io.File fio : fioList){
    			String s = fio.getPath();
    			if (s.equalsIgnoreCase(sReturn+sPath)){
    				sReturn += "/"+s.replace(sReturn,"");
    				bool=true;
    			}
    		}
    		if (bool == false){
    			sReturn+="/"+sPath;
    		}
    	}    	    	
    	return sReturn+"/"+sPaths[sPaths.length-1];
    }
}
