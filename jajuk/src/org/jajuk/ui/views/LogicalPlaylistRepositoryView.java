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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.jajuk.base.Observer;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistManager;
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;

/**
 * Shows logical playlists
 * <p>Logical perspective
 *  * <p>Singleton
 * @author     Bertrand Florat
 * @created   29 dec. 2003
 */
public class LogicalPlaylistRepositoryView extends AbstractPlaylistRepositoryView implements Observer{

	/**Self instance*/
	static LogicalPlaylistRepositoryView lpr;
		
	/**Return self instance*/
	public static synchronized LogicalPlaylistRepositoryView getInstance(){
		if (lpr == null){
			lpr = new LogicalPlaylistRepositoryView();
		}
		return lpr;
	}
	
	/**
	 * Constructor
	 */
	public LogicalPlaylistRepositoryView() {
		lpr = this;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void populate(){
		super.populate();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "LogicalPlaylistRepositoryView.0";	 //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getID()
	 */
	public String getID() {
	    return "org.jajuk.ui.views.LogicalPlaylistRepositoryView"; //$NON-NLS-1$
	}
	
	/**
	 * Create playlists from collection 
	 */
	public synchronized void  populatePlaylists(){
	    synchronized(PlaylistManager.getInstance().getLock()){
	        super.populatePlaylists();
	        //normal playlists
	        ArrayList alItems = new ArrayList(PlaylistManager.getInstance().getItems());
	        Collections.sort(alItems);
	        Iterator it = alItems.iterator();
	        while ( it.hasNext()){
	            Playlist pl = (Playlist)it.next();
	            PlaylistFile plf = pl.getPlayeablePlaylistFile();
	            //if none accessible and hide devices unmounted, continue
	            if (plf == null && ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)){
	                continue;
	            }
	            //if none accessible playlist, keep a chance to mount the first playlist file found
	            if ( plf == null && pl.getPlaylistFiles().size()>0){
	                plf = pl.getPlaylistFiles().get(0);
	            }
	            PlaylistFileItem plfi = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NORMAL,ICON_PLAYLIST_NORMAL,plf,plf.getName());
	            alPlaylistFileItems.add(plfi);
	            plfi.addMouseListener(ma);
	            plfi.setToolTipText(plf.getName());
	            jpRoot.add(plfi);
	            if (plfiSelected!=null && plfi.getPlaylistFile().equals(plfiSelected.getPlaylistFile())){
	                plfiSelected = plfi;
	            }
	        }
	    }
	}

	public synchronized void removeItem (PlaylistFileItem plfiSelected){
	    Playlist pl = PlaylistManager.getInstance().getPlayList(plfiSelected.getPlaylistFile());
	    if (pl != null){
	        PlaylistManager.getInstance().removePlaylist(pl);
        }
	}
	
	public void play(PlaylistFileItem plfi) throws JajukException{
	    Playlist pl = PlaylistManager.getInstance().getPlayList(plfiSelected.getPlaylistFile());
	    if (pl != null){
	        PlaylistFile plf = pl.getPlayeablePlaylistFile();
	        if (plf == null){
	            throw new NoneAccessibleFileException("010"); //$NON-NLS-1$
	        }
	        plf.play();
	    }
	}
	
	
}
