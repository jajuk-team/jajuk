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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;

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
						jpmenu.show(e.getComponent(),e.getX(),e.getY());
						return;
					}
					else { //right button again lauch it 
						ArrayList alFiles = Util.parsePlaylist(plfi.getPlaylistFile());
						if ( alFiles.size() == 0){
							Messages.showErrorMessage("018");	
						}
						else{
							FIFO.getInstance().push(alFiles,false);
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
			}
		};
		
		//refresh
		populate();
		jpRoot.add(Box.createVerticalStrut(1000));  //make sure playlists items are packed to the top
		JScrollPane jsp = new JScrollPane(jpRoot);
		add(jsp);
		//Register on the list for subject we are interrested in
		ObservationManager.register(EVENT_DEVICE_MOUNT,this);
		ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
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
			jpRoot.removeAll();
			populate();
			jpRoot.add(Box.createVerticalStrut(1000));
			SwingUtilities.updateComponentTreeUI(this);
		}
	}
	
	/**
	 * Create playlists from collection 
	 */
	private void populate(){
		//special playlists
		
		//new
		PlaylistFileItem plfi = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NEW,ICON_PLAYLIST_NEW,null,"New");
		plfi.setToolTipText("Dynamic playlist : drag and drop into for playing");
		plfi.addMouseListener(ma);
		jpRoot.add(plfi);
		//bookmark
		plfi = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK,ICON_PLAYLIST_BOOKMARK,null,"Bookmarks");
		plfi.setToolTipText("Bookmark playlist : drag and drop into for keeping trace");
		plfi.addMouseListener(ma);
		jpRoot.add(plfi);
		//Best of
		plfi = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BESTOF,ICON_PLAYLIST_BESTOF,null,"Best of");
		plfi.setToolTipText("Best of playlist : contains top tracks");
		plfi.addMouseListener(ma);
		jpRoot.add(plfi);
	
		//normal playlists
		ArrayList al = PlaylistFileManager.getPlaylistFiles();
		Collections.sort(al);
		Iterator it = al.iterator();
		while ( it.hasNext()){
			PlaylistFile plf = (PlaylistFile)it.next();
			plfi = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NORMAL,ICON_PLAYLIST_NORMAL,plf,plf.getName());
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
			if ( ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE)){  //file delete confirmation
				String sFileToDelete = plfiSelected.getPlaylistFile().getDirectory().getFio().getAbsoluteFile()+"/"+plfiSelected.getPlaylistFile().getName();
				String sMessage = Messages.getString("Confirmation_delete")+"\n"+sFileToDelete;
				int i = JOptionPane.showConfirmDialog(Main.jframe,sMessage,Messages.getString("Warning"),JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
				if ( i == JOptionPane.OK_OPTION){
					File fileToDelete = new File(sFileToDelete);
					if ( fileToDelete.exists()){
						fileToDelete.delete();
						PlaylistFileManager.delete(plfiSelected.getPlaylistFile().getId());
						ObservationManager.notify(EVENT_DEVICE_REFRESH);  //requires device refresh
					}
				}
			}
		}
		else if(ae.getSource() == jmiEdit){
			
		}
		else if(ae.getSource() == jmiPlay){
			ArrayList alFiles = null;
			if ( plfiSelected.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF){
				alFiles = FileManager.getBestOfFiles();
			}
			else{
				alFiles = Util.parsePlaylist(plfiSelected.getPlaylistFile());
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


	/**
	 * A physical playlist icon + text
	 *
	 * @author     bflorat
	 * @created    31 dec. 2004	 
	 */
	class PlaylistFileItem extends JPanel{
		
		/** Associated playlist file*/
		PlaylistFile plf;
		
		/**Associated name*/
		String sName;
		
		/** Playlist file type : 0: normal, 1:new, 2:bookmarks, 3:bestif*/
		int iType = 0;
		
		public static final int PLAYLIST_TYPE_NORMAL = 0;
		public static final int PLAYLIST_TYPE_NEW = 1;
		public static final int PLAYLIST_TYPE_BOOKMARK = 2;
		public static final int PLAYLIST_TYPE_BESTOF = 3;
		
		
		/**
		 * Constructor
		 * @param iType : Playlist file type : 0: normal, 1:new, 2:bookmarks, 3:bestif
		 * @param sIcon : icon to be shown
		 * @param sName : nom of the playlist file to be displayed
		 */
		PlaylistFileItem(int iType,String sIcon,PlaylistFile plf, String sName){
			this.iType = iType;
			this.plf = plf;
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
			setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			JPanel jpIcon  = new JPanel();
			jpIcon.setLayout(new BoxLayout(jpIcon,BoxLayout.X_AXIS));
			JLabel jlIcon = new JLabel(Util.getIcon(sIcon)); 
			jpIcon.add(Box.createGlue());
			jpIcon.add(jlIcon);
			jpIcon.add(Box.createGlue());
			add(jpIcon);
			JLabel jlName = new JLabel(sName);
			jlName.setFont(new Font("Dialog",Font.PLAIN,10));
			JPanel jpName  = new JPanel();
			jpName.setLayout(new BoxLayout(jpName,BoxLayout.X_AXIS));
			jpName.add(Box.createGlue());
			jpName.add(jlName);
			jpName.add(Box.createGlue());
			jpName.setPreferredSize(new Dimension(40,10));
			add(jpName);
			add(Box.createVerticalGlue());
		}
		
				
		/**
		 * @return Returns the playlist file.
		 */
		public PlaylistFile getPlaylistFile() {
			return plf;
		}

		/**
		 * @return Returns the Type.
		 */
		public int getType() {
			return iType;
		}

	}
