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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.PlaylistFile;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.util.Util;

import com.sun.SwingWorker;

/**
 * Shows playlist files
 * <p>Physical perspective
 *  * <p>Singleton
 * @author     Bertrand Florat
 * @created   29 dec. 2003
 */
abstract public class AbstractPlaylistRepositoryView extends ViewAdapter implements Observer,ActionListener{
	
	
	/**Selected playlist file item */
	PlaylistFileItem plfiSelected;
	
	/**Queue playlist*/
	PlaylistFileItem plfiQueue;
	
	/**New playlist*/
	PlaylistFileItem plfiNew;
	
	/**Queue playlist*/
	PlaylistFileItem plfiBookmarks;
	
	/**Bestof playlist*/
	PlaylistFileItem plfiBestof;
    
    /**Novelties playlist*/
    PlaylistFileItem plfiNovelties;
    
	/**List of playlistfile item*/
	ArrayList alPlaylistFileItems = new ArrayList(10);
	
	
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
	public void populate(){
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
		
		jmiSaveAs = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.2"));  //$NON-NLS-1$
		jmiSaveAs.addActionListener(this);
		jpmenu.add(jmiSaveAs);
		
		jmiProperties = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.4"));  //$NON-NLS-1$
		jmiProperties.addActionListener(this);
		jmiProperties.setEnabled(false);
		jpmenu.add(jmiProperties);
		
		//mouse adapter
		ma = new MouseAdapter() {
			public void mouseClicked(final MouseEvent e) {
				PlaylistFileItem plfi = (PlaylistFileItem)e.getComponent();
				if (plfi == plfiSelected){
					if (e.getButton() == MouseEvent.BUTTON3){  //right button
						showMenu(plfi,e);
						return;
					}
					else { //left button again launch it 
						plfi.getPlaylistFile().play();
					}
				}
				else{ //we selected a new playlist file
					Util.waiting();
					selectPlaylistFileItem(plfi);
					Properties properties = new Properties();
					properties.put(DETAIL_SELECTION,plfi);
					properties.put(DETAIL_ORIGIN,this);
					ObservationManager.notify(new Event(EVENT_PLAYLIST_CHANGED,properties)); 
					if (e.getButton() == MouseEvent.BUTTON3){  //right button
						showMenu(plfi,e);
					}
				}		
			}
		};
		//refresh
		populatePlaylists();
		jpRoot.add(Box.createVerticalStrut(500));  //make sure playlists items are packed to the top
		JScrollPane jsp = new JScrollPane(jpRoot);
        jsp.getVerticalScrollBar().setUnitIncrement(60);
        add(jsp);
		//Register on the list for subject we are interrested in
		ObservationManager.register(EVENT_DEVICE_MOUNT,this);
		ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
		//set queue playlist as default in playlist editor
		selectPlaylistFileItem(plfiQueue);	
		Properties properties = new Properties();
		properties.put(DETAIL_SELECTION,plfiQueue);
		properties.put(DETAIL_ORIGIN,this);
		ObservationManager.notify(new Event(EVENT_PLAYLIST_CHANGED,properties));
	}
	
	
	/**
	 * Display the playlist menu
	 * @param plfi
	 * @param e
	 */
	private void showMenu(PlaylistFileItem plfi,MouseEvent e){
		if (plfi.getType() != PlaylistFileItem.PLAYLIST_TYPE_NORMAL){
			//cannot delete special playlists
			jmiDelete.setEnabled(false); 
			//Save as is only for special playlists
			jmiSaveAs.setEnabled(true);
		}
		else{
			jmiDelete.setEnabled(true);
			jmiSaveAs.setEnabled(false);
		}
		//cannot play the queue playlist : nonsense
		if ( plfiSelected.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
			jmiPlay.setEnabled(false);  
		}
		else{
			jmiPlay.setEnabled(true);
		}
		jpmenu.show(e.getComponent(),e.getX(),e.getY());
		
	}
	
	/**
	 * Set current playlist file item
	 * @param plfi
	 */
	void selectPlaylistFileItem(PlaylistFileItem plfi){
		//remove item border
		if (plfiSelected!=null){
			plfiSelected.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		}
		plfi.setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
		//set new item
		this.plfiSelected = plfi;
        FIFO.getInstance().setPlaylist(plfi.getPlaylistFile());
  }
	
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "PhysicalPlaylistRepositoryView.6";	 //$NON-NLS-1$
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
	public synchronized void update(Event event) {
		String subject = event.getSubject();
		if ( subject.equals(EVENT_DEVICE_MOUNT) 
                || subject.equals(EVENT_DEVICE_UNMOUNT) 
                || subject.equals(EVENT_DEVICE_REFRESH) ) {
			SwingWorker sw = new SwingWorker() {
				public Object  construct(){
				    if (jpRoot.getComponentCount() > 0){
						jpRoot.removeAll();
				    }
					populatePlaylists();
					return null;
				}
				public void finished() {
					jpRoot.add(Box.createVerticalStrut(500));  //make sure specials playlists are paked to the top
					//set queue playlist as default in playlist editor
					selectPlaylistFileItem(plfiQueue);
					Properties properties = new Properties();
					properties.put(DETAIL_SELECTION,plfiQueue);
					properties.put(DETAIL_ORIGIN,this);
					ObservationManager.notify(new Event(EVENT_PLAYLIST_CHANGED,properties));
					AbstractPlaylistRepositoryView.this.revalidate();
					AbstractPlaylistRepositoryView.this.repaint();
				}
			};
			sw.start();
		}
	}
	
	/**
	 * Create special playlists from collection, this is called by both logicial and physical populate methods
	 */
	void populatePlaylists(){
		alPlaylistFileItems.clear();
		//special playlists
		JPanel jpSpecials = new JPanel();
		jpSpecials.setBorder(BorderFactory.createTitledBorder(Messages.getString("PhysicalPlaylistRepositoryView.8"))); //$NON-NLS-1$
		jpSpecials.setLayout(new BoxLayout(jpSpecials,BoxLayout.Y_AXIS));
		//queue
		//note we give an id : this id is only used to match current playlist
		plfiQueue = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_QUEUE,ICON_PLAYLIST_QUEUE,new PlaylistFile(PlaylistFileItem.PLAYLIST_TYPE_QUEUE,"1",null,null,null),Messages.getString("PhysicalPlaylistRepositoryView.9")); //$NON-NLS-1$ //$NON-NLS-2$
		plfiQueue.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.10")); //$NON-NLS-1$
		plfiQueue.addMouseListener(ma);
		jpSpecials.add(plfiQueue);
		alPlaylistFileItems.add(plfiQueue);
		//new
		plfiNew = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NEW,ICON_PLAYLIST_NEW,new PlaylistFile(PlaylistFileItem.PLAYLIST_TYPE_NEW,"2",null,null,null),Messages.getString("PhysicalPlaylistRepositoryView.11")); //$NON-NLS-1$ //$NON-NLS-2$
		plfiNew.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.12")); //$NON-NLS-1$
		plfiNew.addMouseListener(ma);
		jpSpecials.add(plfiNew);
		alPlaylistFileItems.add(plfiNew);
		//bookmark
		plfiBookmarks = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK,ICON_PLAYLIST_BOOKMARK,new PlaylistFile(PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK,"3",null,null,null),Messages.getString("PhysicalPlaylistRepositoryView.13")); //$NON-NLS-1$ //$NON-NLS-2$
		plfiBookmarks.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.14")); //$NON-NLS-1$
		plfiBookmarks.addMouseListener(ma);
		jpSpecials.add(plfiBookmarks);
		alPlaylistFileItems.add(plfiBookmarks);
		//Best of
		plfiBestof = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BESTOF,ICON_PLAYLIST_BESTOF,new PlaylistFile(PlaylistFileItem.PLAYLIST_TYPE_BESTOF,"4",null,null,null),Messages.getString("PhysicalPlaylistRepositoryView.15")); //$NON-NLS-1$ //$NON-NLS-2$
		plfiBestof.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.16")); //$NON-NLS-1$
		plfiBestof.addMouseListener(ma);
        jpSpecials.add(plfiBestof);
        alPlaylistFileItems.add(plfiBestof);
        //Novelties
        plfiNovelties = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES,ICON_PLAYLIST_NOVELTIES,new PlaylistFile(PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES,"1",null,null,null),Messages.getString("PhysicalPlaylistRepositoryView.17")); //$NON-NLS-1$ //$NON-NLS-2$
        plfiNovelties.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.18")); //$NON-NLS-1$
        plfiNovelties.addMouseListener(ma);
        jpSpecials.add(plfiNovelties);
		alPlaylistFileItems.add(plfiNovelties);
		
		jpRoot.add(jpSpecials);
		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent ae) {
		new Thread(){
			public void run(){
				if ( ae.getSource() == jmiDelete){
					plfiSelected.getPlaylistFile().delete();
				}
				else if(ae.getSource() == jmiPlay){
					plfiSelected.getPlaylistFile().play();
				}
				else if(ae.getSource() == jmiProperties){
					//TBI
				}
				else if(ae.getSource() == jmiSaveAs){ //save as
					plfiSelected.getPlaylistFile().saveAs();
				}
			}
		}.start();
	}
	
	
	
	/**
	 * @return Returns the alPlaylistFileItems.
	 */
	public ArrayList getPlaylistFileItems() {
		return alPlaylistFileItems;
	}
	
	/**
	 * @return Returns the plfiQueue.
	 */
	public PlaylistFileItem getQueue() {
		return plfiQueue;
	}
	
}



