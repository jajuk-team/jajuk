/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision: 2920 $$
 */
package org.jajuk.ui.action;

import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.ObservationManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class DeleteAction extends ActionBase {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	DeleteAction() {
		super(Messages.getString("FilesTreeView.7"), IconLoader.ICON_DELETE, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 1, true), true, false);
		setShortDescription(Messages.getString("FilesTreeView.7"));
	}

	public void perform(ActionEvent e) {
		JComponent source = (JComponent) e.getSource();
        // Get required data from the tree (selected node and node type)
        final ArrayList<Item> alSelected = (ArrayList<Item>) source
                        .getClientProperty(DETAIL_SELECTION);
        ArrayList<File> alFiles = new ArrayList<File>(alSelected.size());
        ArrayList<File> rejFiles = new ArrayList<File>(alSelected.size());
        ArrayList<Directory> alDirs = new ArrayList<Directory>(alSelected.size());
		ArrayList<Directory> rejDirs = new ArrayList<Directory>(alSelected.size());
         
        for (Item item : alSelected) {
        	if (item instanceof File) {
        		alFiles.add((File) item);
        	} else if (item instanceof Track){
                alFiles.addAll(((Track)item).getFiles());
        	} else if (item instanceof Directory) {
        		alDirs.add((Directory) item);
		    }
        }
         
        if (alFiles.size() > 0){
        	 // Ask if a confirmation is required
    		if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE)) {
    			String sFiles = "";
    			for (File f : alFiles) {
    				sFiles += f.getName() + "\n";
    			}
    			int iResu = Messages.getChoice(Messages
    					.getString("Confirmation_delete_files")
                        	+ " : \n\n" + sFiles, JOptionPane.YES_NO_CANCEL_OPTION,
                        	JOptionPane.INFORMATION_MESSAGE);
    			if (iResu != JOptionPane.YES_OPTION) {
    				return;
    			}
    		}

    		for (File f : alFiles) {
    			try {
    				Util.deleteFile(f.getIO());
    				FileManager.getInstance().removeFile(f);
    			} catch (Exception ioe) {
    				Log.error(131, ioe);
    				rejFiles.add(f);
    			}
    		}
    		if(rejFiles.size() > 0){
    			String rejString = "";
    			for (File f : rejFiles){
    				rejString += f.getName() + "\n";
    			}
    			Messages.showWarningMessage(Messages.getErrorMessage(172) + "\n\n" + rejString);
    		}
        }
        
        if(alDirs.size() > 0){
        	// Ask if a confirmation is required
    		if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE)) {
    		    String sFiles = "";
    		    int Count = 0;
    		    for (Directory d : alDirs) {
    		    	sFiles += d.getName() + "\n";
    		    	Count += d.getFilesRecursively().size();
    		    	for (File f : d.getFilesRecursively()){
    		    		sFiles += "  + " + f.getName() + "\n";
    		    	}
    			
    		    }
    		    int iResu = Messages.getChoice(Messages
    						   .getString("Confirmation_delete_dirs")
    						   + " : \n" + sFiles + "\n" + Count + " files will be deleted.", JOptionPane.YES_NO_CANCEL_OPTION,
    						   JOptionPane.INFORMATION_MESSAGE);
    		    if (iResu != JOptionPane.YES_OPTION) {
    			return;
    		    }
    		}
    		for (Directory d : alDirs) {
    		    try {
    		    	Util.deleteDir(new java.io.File(d.getAbsolutePath()));
    		    	DirectoryManager.getInstance().removeDirectory(d.getID());
    		    }catch (Exception ioe) {
    		    	Log.error(131, ioe);
    		    	rejDirs.add(d);
    		    }
    		}
    		if(rejDirs.size() > 0){
    			String rejString = "";
    			for (Directory d : rejDirs){
    				rejString += d.getName() + "\n";
    			}
    			Messages.showWarningMessage(Messages.getErrorMessage(173) + "\n\n" + rejString);
    		}
        }
    	ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
	}
}
