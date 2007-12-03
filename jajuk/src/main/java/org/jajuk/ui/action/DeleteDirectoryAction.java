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

import javax.swing.JComponent;
import javax.swing.JOptionPane;

public class DeleteDirectoryAction extends ActionBase {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	DeleteDirectoryAction() {
		super(Messages.getString("FilesTreeView.7"), IconLoader.ICON_DELETE, true);
		setShortDescription(Messages.getString("FilesTreeView.7"));
	}

	public void perform(ActionEvent e) {
		JComponent source = (JComponent) e.getSource();
		// Get required data from the tree (selected node and node type)
		final ArrayList<Item> alSelected = (ArrayList<Item>) source
		    .getClientProperty(DETAIL_SELECTION);
		ArrayList<Directory> alDirs = new ArrayList<Directory>(alSelected.size());
		ArrayList<Directory> rejDirs = new ArrayList<Directory>(alSelected.size());
         
		for (Item item : alSelected) {
		    if (item instanceof Directory) {
        		alDirs.add((Directory) item);
		    }
		}
         
		// Ask if a confirmation is required
		if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE)) {
		    String sFiles = "";
		    for (Directory d : alDirs) {
			sFiles += d.getName() + "\n";
		    }
		    int iResu = Messages.getChoice(Messages
						   .getString("Confirmation_delete_dirs")
						   + " : \n" + sFiles, JOptionPane.YES_NO_CANCEL_OPTION,
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
		ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
		return;
	}
}
