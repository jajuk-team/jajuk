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
 *  $Revision$
 */

package org.jajuk.ui.views;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.PlaylistFileItem;

/**
 * Shows playlist files
 * <p>Physical perspective
 *  * <p>Singleton
 * @author     Bertrand Florat
 * @created   29 dec. 2003
 */
public class PhysicalPlaylistRepositoryView extends AbstractPlaylistRepositoryView implements Observer,ActionListener{
	
	/**Self instance*/
	static PhysicalPlaylistRepositoryView ppr;
	
	/**Return self instance*/
	public static synchronized PhysicalPlaylistRepositoryView getInstance(){
		if (ppr == null){
			ppr = new PhysicalPlaylistRepositoryView();
		}
		return ppr;
	}	
	/**
	 * Constructor
	 */
	public PhysicalPlaylistRepositoryView() {
		ppr = this;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void populate(){
		//commons
		super.populate();
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "PhysicalPlaylistRepositoryView.6";	 //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getID()
	 */
	public String getID() {
	    return "org.jajuk.ui.views.PhysicalPlaylistRepositoryView"; //$NON-NLS-1$
	}
	
	
	/**
	 * Create playlists from collection 
	 */
	void populatePlaylists(){
		super.populatePlaylists();
		//normal playlists
		ArrayList al = PlaylistFileManager.getPlaylistFiles();
		Collections.sort(al);
		Iterator it = al.iterator();
		while ( it.hasNext()){
			PlaylistFile plf = (PlaylistFile)it.next();
			if ( !plf.isReady()){  //don't show playlist files on unmounted devices
				continue;
			}
			PlaylistFileItem plfi = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NORMAL,ICON_PLAYLIST_NORMAL,plf,plf.getName());
			alPlaylistFileItems.add(plfi);
			plfi.addMouseListener(ma);
			plfi.setToolTipText(plf.getName());
			jpRoot.add(plfi);
		}
	}
	
}
