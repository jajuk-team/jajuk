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
import java.util.Collections;
import java.util.Iterator;

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
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.PlaylistFileItem;

import com.sun.SwingWorker;

/**
 * Shows playlist files
 * <p>Physical perspective
 *  * <p>Singleton
 * @author     bflorat
 * @created   29 dec. 2003
 */
public class PhysicalPlaylistRepositoryView extends ViewAdapter implements Observer,ActionListener{

	/**Self instance*/
	private static PhysicalPlaylistRepositoryView ppr;
	
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
		JMenuItem jmiEdit;
		JMenuItem jmiSaveAs;
		JMenuItem jmiDelete;
		JMenuItem jmiProperties;
		
	MouseAdapter ma;
		
	/**Return self instance*/
	public static PhysicalPlaylistRepositoryView getInstance(){
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
	public void display(){
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		
		//root pane
		jpRoot = new JPanel();
		jpRoot.setLayout(new BoxLayout(jpRoot,BoxLayout.Y_AXIS));
		
		//Popup menus
		jpmenu =  new JPopupMenu();
	
		jmiPlay = new JMenuItem("Play"); 
		jmiPlay.addActionListener(this);
		jpmenu.add(jmiPlay);
		
		jmiEdit = new JMenuItem("Edit"); 
		jmiEdit.addActionListener(this);
		jpmenu.add(jmiEdit);

		jmiSaveAs = new JMenuItem("Save as"); 
		jmiSaveAs.addActionListener(this);
		jpmenu.add(jmiSaveAs);
	
		jmiDelete = new JMenuItem("Delete"); 
		jmiDelete.addActionListener(this);
		jpmenu.add(jmiDelete);

		jmiProperties = new JMenuItem("Properties"); 
		jmiProperties.addActionListener(this);
		jmiProperties.setEnabled(false);
		jpmenu.add(jmiProperties);
		
		//mouse adapter
		ma = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				PlaylistFileItem plfi = (PlaylistFileItem)e.getComponent();
				if (plfi == plfiSelected){
					if (e.getButton() == 3){  //left button
						if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NEW 
								|| plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK
								|| plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF){
							jmiDelete.setEnabled(false); //cannot delete special playlists
						}
						else{
							jmiDelete.setEnabled(true);
						}
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
							ArrayList alFiles =  plfi.getPlaylistFile().getBasicFiles();
							if ( alFiles.size() == 0){
								Messages.showErrorMessage("018");	
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
				//remove item border
				if (plfiSelected!=null){
					plfiSelected.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
				}
				//set new item
				plfiSelected = plfi;
				plfiSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
				PhysicalPlaylistEditorView.getInstance().setCurrentPlayListFile(plfiSelected);
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
		PhysicalPlaylistEditorView.getInstance().setCurrentPlayListFile(plfiQueue);
		plfiQueue.setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Playlists view";	
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.PhysicalPlaylistRepositoryView";
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
					PhysicalPlaylistEditorView.getInstance().setCurrentPlayListFile(plfiQueue);
					plfiQueue.setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
					return null;
				}
				public void finished() {
					SwingUtilities.updateComponentTreeUI(PhysicalPlaylistRepositoryView.this);
				}
			};
			sw.start();
		}
	}
	
	/**
	 * Create playlists from collection 
	 */
	private void populate(){
		//special playlists
		JPanel jpSpecials = new JPanel();
		jpSpecials.setBorder(BorderFactory.createTitledBorder("Specials"));
		jpSpecials.setLayout(new BoxLayout(jpSpecials,BoxLayout.Y_AXIS));
		//queue
		plfiQueue = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_QUEUE,ICON_PLAYLIST_QUEUE,null,"Queue");
		plfiQueue.setToolTipText("Current queue : drag and drop into for playing");
		plfiQueue.addMouseListener(ma);
		jpSpecials.add(plfiQueue);
		//new
		plfiNew = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NEW,ICON_PLAYLIST_NEW,null,"New");
		plfiNew.setToolTipText("New playlist : drag and drop into for adding files");
		plfiNew.addMouseListener(ma);
		jpSpecials.add(plfiNew);
		//bookmark
		plfiBookmarks = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK,ICON_PLAYLIST_BOOKMARK,null,"Bookmarks");
		plfiBookmarks.setToolTipText("Bookmark playlist : drag and drop into for keeping trace");
		plfiBookmarks.addMouseListener(ma);
		jpSpecials.add(plfiBookmarks);
		//Best of
		plfiBestof = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BESTOF,ICON_PLAYLIST_BESTOF,null,"Best of");
		plfiBestof.setToolTipText("Best of playlist : contains top tracks");
		plfiBestof.addMouseListener(ma);
		jpSpecials.add(plfiBestof);
		
		jpRoot.add(jpSpecials);
	
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
			plfi.addMouseListener(ma);
			plfi.setToolTipText(plf.getName());
			jpRoot.add(plfi);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		if ( ae.getSource() == jmiDelete){
			plfiSelected.getPlaylistFile().delete();
		}
		else if(ae.getSource() == jmiEdit){
			PhysicalPlaylistEditorView.getInstance().setCurrentPlayListFile(plfiSelected);
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
				alFiles = plfiSelected.getPlaylistFile().getBasicFiles();
			}
			if ( alFiles.size() == 0){
				Messages.showErrorMessage("018");	
			}
			else{
				FIFO.getInstance().push(alFiles,false);
			}
		}
		else if(ae.getSource() == jmiProperties){
			
		}
		else if(ae.getSource() == jmiSaveAs){
			
		}
	}

}


	
