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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.ui.Observer;
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
	
	JPanel jpRoot;
	JPopupMenu jpmenu;
		JMenuItem jmiPlay;
		JMenuItem jmiEdit;
		JMenuItem jmiSave;
		JMenuItem jmiSaveAs;
		JMenuItem jmiDelete;
		JMenuItem jmiProperties;
		
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

		jmiSave = new JMenuItem("Save"); 
		jmiSave.addActionListener(this);
		jpmenu.add(jmiSave);
	
		jmiSaveAs = new JMenuItem("Save as"); 
		jmiSaveAs.addActionListener(this);
		jpmenu.add(jmiSaveAs);
	
		jmiDelete = new JMenuItem("Delete"); 
		jmiDelete.addActionListener(this);
		jpmenu.add(jmiDelete);

		jmiProperties = new JMenuItem("Properties"); 
		jmiProperties.addActionListener(this);
		jpmenu.add(jmiProperties);
		
		//refresh
		populate();
		jpRoot.add(Box.createVerticalGlue());
		JScrollPane jsp = new JScrollPane(jpRoot);
		add(jsp);
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
	}
	
	/**
	 * Create playlists from collection 
	 */
	private void populate(){
		//special playlists
		jpRoot.add(new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NEW,ICON_PLAYLIST_NEW,"New"));
		jpRoot.add(new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK,ICON_PLAYLIST_BOOKMARK,"Bookmarks"));
		jpRoot.add(new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NEW,ICON_PLAYLIST_BESTOF,"Best of"));
		//normal playlists
		Iterator it = PlaylistFileManager.getPlaylistFiles().iterator();
		while ( it.hasNext()){
			PlaylistFile plf = (PlaylistFile)it.next();
			jpRoot.add(new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NORMAL,ICON_PLAYLIST_NORMAL,plf.getName()));
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
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
		PlaylistFileItem(int iType,String sIcon,String sName){
			this.iType = iType;
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
			jpName.setPreferredSize(new Dimension(40,5));
			add(jpName);
			add(Box.createVerticalGlue());
			setToolTipText(sName);
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
