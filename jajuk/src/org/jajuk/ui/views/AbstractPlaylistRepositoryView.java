/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jajuk.base.Bookmarks;
import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.PlaylistFile;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import com.sun.SwingWorker;

/**
 * Shows playlist files
 * <p>Physical perspective
 *  * <p>Singleton
 * @author     bflorat
 * @created   29 dec. 2003
 */
abstract public class AbstractPlaylistRepositoryView extends ViewAdapter implements Observer,ActionListener{

	
	/**Selected playlist file item */
	private static PlaylistFileItem plfiSelected;
	
	/**Queue playlist*/
	PlaylistFileItem plfiQueue;
	
	/**New playlist*/
	PlaylistFileItem plfiNew;
	
	/**Queue playlist*/
	PlaylistFileItem plfiBookmarks;
	
	/**Bestof playlist*/
	PlaylistFileItem plfiBestof;
	
	
	JPanel jpRoot;
	JPopupMenu jpmenu;
		JMenuItem jmiPlay;
		JMenuItem jmiSaveAs;
		JMenuItem jmiDelete;
		JMenuItem jmiProperties;
		
	MouseAdapter ma;
		

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display(){
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		
		//root pane
		jpRoot = new JPanel();
		jpRoot.setLayout(new BoxLayout(jpRoot,BoxLayout.Y_AXIS));
		
		//Popup menus
		jpmenu =  new JPopupMenu();
	
		jmiPlay = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.0"));  //$NON-NLS-1$
		jmiPlay.addActionListener(this);
		jpmenu.add(jmiPlay);
		
		jmiDelete = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.3"));  //$NON-NLS-1$
		jmiDelete.addActionListener(this);
		jpmenu.add(jmiDelete);

		jmiProperties = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.4"));  //$NON-NLS-1$
		jmiProperties.addActionListener(this);
		jmiProperties.setEnabled(false);
		jpmenu.add(jmiProperties);
		
		//mouse adapter
		ma = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				PlaylistFileItem plfi = (PlaylistFileItem)e.getComponent();
				if (plfi == plfiSelected){
					if (e.getButton() == 3){  //left button
						if (plfi.getType() != PlaylistFileItem.PLAYLIST_TYPE_NORMAL){
							//cannot delete special playlists
							jmiDelete.setEnabled(false); 
							//Save as is only for special playlists
							jmiSaveAs.setEnabled(false);
						}
						else{
							jmiDelete.setEnabled(true);
							jmiSaveAs.setEnabled(true);
						}
						//cannot play the queue playlist : nonsense
						if ( plfiSelected.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
							jmiPlay.setEnabled(false);  
						}
						else{
							jmiPlay.setEnabled(true);
						}
						jpmenu.show(e.getComponent(),e.getX(),e.getY());
						return;
					}
					else { //right button again lauch it 
						if ( plfi != null){
							ArrayList alFiles =  new ArrayList(10);
							try{
								alFiles = plfi.getPlaylistFile().getBasicFiles();
							}
							catch(JajukException je){
								Log.error("009",plfi.getPlaylistFile().getName(),new Exception()); //$NON-NLS-1$
								Messages.showErrorMessage("009",plfi.getPlaylistFile().getName()); //$NON-NLS-1$
							}
							if ( alFiles.size() == 0){
								Messages.showErrorMessage("018");	 //$NON-NLS-1$
							}
							else{
								FIFO.getInstance().push(alFiles,false);
							}
						}
						else{
							return;
						}
					}
				}
				else{ //we selected a new playlist file
					Util.waiting(); //waiting cursor will be removed by the editor when it has refreshed itself
					//remove item border
					if (plfiSelected!=null){
						plfiSelected.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
					}
					//set new item
					plfiSelected = plfi;
					plfiSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
					setCurrentPlayListFileInEditor(plfiSelected);
				}
			}
		};
		
		//refresh
		populate();
		jpRoot.add(Box.createVerticalStrut(500));  //make sure playlists items are packed to the top
		JScrollPane jsp = new JScrollPane(jpRoot);
		add(jsp);
		//Register on the list for subject we are interrested in
		ObservationManager.register(EVENT_DEVICE_MOUNT,this);
		ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
		//set queue playlist as default in playlist editor
		plfiSelected = plfiQueue;
		setCurrentPlayListFileInEditor(plfiQueue);
		plfiQueue.setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
	}

	/**
	 * Set playlist file in the associated editor view
	 * @param plfi
	 */
	private void setCurrentPlayListFileInEditor(PlaylistFileItem plfi){
		if ( this instanceof PhysicalPlaylistRepositoryView ){ //means we are in physical perspective
			PhysicalPlaylistEditorView.getInstance().setCurrentPlayListFile(plfi);
		}
		else if ( this instanceof LogicalPlaylistRepositoryView ){ //means we are in logical perspective
			LogicalPlaylistEditorView.getInstance().setCurrentPlayListFile(plfi);
		} 
	}
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("PhysicalPlaylistRepositoryView.6");	 //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.PhysicalPlaylistRepositoryView"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject) {
		if ( subject.equals(EVENT_DEVICE_MOUNT) || subject.equals(EVENT_DEVICE_UNMOUNT) || subject.equals(EVENT_DEVICE_REFRESH) ) {
			SwingWorker sw = new SwingWorker() {
				public Object  construct(){
					jpRoot.removeAll();
					populate();
					jpRoot.add(Box.createVerticalStrut(500));  //make sure specials playlists are paked to the top
					//set queue playlist as default in playlist editor
					plfiSelected = plfiQueue; //reset queue as default queue, we can't assume playlist before refresh yet exists
					setCurrentPlayListFileInEditor(plfiQueue);
					plfiQueue.setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
					return null;
				}
				public void finished() {
					SwingUtilities.updateComponentTreeUI(AbstractPlaylistRepositoryView.this);
				}
			};
			sw.start();
		}
	}
	
	/**
	 * Create playlists from collection 
	 */
	void populate(){
		//special playlists
		JPanel jpSpecials = new JPanel();
		jpSpecials.setBorder(BorderFactory.createTitledBorder(Messages.getString("PhysicalPlaylistRepositoryView.8"))); //$NON-NLS-1$
		jpSpecials.setLayout(new BoxLayout(jpSpecials,BoxLayout.Y_AXIS));
		//queue
		plfiQueue = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_QUEUE,ICON_PLAYLIST_QUEUE,null,Messages.getString("PhysicalPlaylistRepositoryView.9")); //$NON-NLS-1$
		plfiQueue.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.10")); //$NON-NLS-1$
		plfiQueue.addMouseListener(ma);
		jpSpecials.add(plfiQueue);
		//new
		plfiNew = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NEW,ICON_PLAYLIST_NEW,new PlaylistFile("-1",null,null,null),Messages.getString("PhysicalPlaylistRepositoryView.11")); //$NON-NLS-1$
		plfiNew.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.12")); //$NON-NLS-1$
		plfiNew.addMouseListener(ma);
		jpSpecials.add(plfiNew);
		//bookmark
		plfiBookmarks = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK,ICON_PLAYLIST_BOOKMARK,null,Messages.getString("PhysicalPlaylistRepositoryView.13")); //$NON-NLS-1$
		plfiBookmarks.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.14")); //$NON-NLS-1$
		plfiBookmarks.addMouseListener(ma);
		jpSpecials.add(plfiBookmarks);
		//Best of
		plfiBestof = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BESTOF,ICON_PLAYLIST_BESTOF,null,Messages.getString("PhysicalPlaylistRepositoryView.15")); //$NON-NLS-1$
		plfiBestof.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.16")); //$NON-NLS-1$
		plfiBestof.addMouseListener(ma);
		jpSpecials.add(plfiBestof);
		
		jpRoot.add(jpSpecials);

	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		if ( ae.getSource() == jmiDelete){
			plfiSelected.getPlaylistFile().delete();
		}
		else if(ae.getSource() == jmiPlay){
			ArrayList alFiles = null;
			if ( plfiSelected.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF){
				alFiles = FileManager.getBestOfFiles();
			}
			else if ( plfiSelected.getType() == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
				alFiles = Bookmarks.getInstance().getFiles();
			}
			else{
				try{
					alFiles = plfiSelected.getPlaylistFile().getBasicFiles();
				}
				catch(JajukException je){
					Log.error("009",plfiSelected.getPlaylistFile().getName(),new Exception()); //$NON-NLS-1$
					Messages.showErrorMessage("009",plfiSelected.getPlaylistFile().getName()); //$NON-NLS-1$
				}
			}
			if ( alFiles== null || alFiles.size() == 0){
				Messages.showErrorMessage("018");	 //$NON-NLS-1$
			}
			else{
				FIFO.getInstance().push(alFiles,false);
			}
		}
		else if(ae.getSource() == jmiProperties){
			//TBI
		}
	}

}


	
