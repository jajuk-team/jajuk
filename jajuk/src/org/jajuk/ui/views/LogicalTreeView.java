/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA. 
 * $Revision$
 */

package org.jajuk.ui.views;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

/**
 * Logical tree view
 * 
 * @author bflorat 
 * @created 28 nov. 2003
 */
public class LogicalTreeView extends ViewAdapter implements ActionListener{

	/** Self instance */
	private static LogicalTreeView ltv;

	/** The logical tree */
	JTree jtree;
	
	/** Top tree node */
	DefaultMutableTreeNode top;
	
	/** Track selection*/
	TreeSet tsTracks;
	
	/** Style selection*/
	TreeSet tsStyles;
	
	/** author selection*/
	TreeSet tsAuthors;

	/** album selection*/
	TreeSet tsAlbums;

	JPopupMenu jmenuStyle;
		JMenuItem jmiStylePlay;
		JMenuItem jmiStylePush;
		JMenuItem jmiStylePlayShuffle;
		JMenuItem jmiStylePlayRepeat;
		JMenuItem jmiStyleDelete;
		JMenuItem jmiStyleSetProperty;
		JMenuItem jmiStyleProperties;
		
	JPopupMenu jmenuAuthor;
		JMenuItem jmiAuthorPlay;
		JMenuItem jmiAuthorPush;
		JMenuItem jmiAuthorPlayShuffle;
		JMenuItem jmiAuthorPlayRepeat;
		JMenuItem jmiAuthorDelete;
		JMenuItem jmiAuthorSetProperty;
		JMenuItem jmiAuthorProperties;
		
	JPopupMenu jmenuAlbum;
		JMenuItem jmiAlbumPlay;
		JMenuItem jmiAlbumPush;
		JMenuItem jmiAlbumPlayShuffle;
		JMenuItem jmiAlbumPlayRepeat;
		JMenuItem jmiAlbumDelete;
		JMenuItem jmiAlbumSetProperty;
		JMenuItem jmiAlbumProperties;
		
	JPopupMenu jmenuTrack;
		JMenuItem jmiTrackPlay;
		JMenuItem jmiTrackPush;
		JMenuItem jmiTrackDelete;
		JMenuItem jmiTrackSetProperty;
		JMenuItem jmiTrackProperties;
		
		
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Logical tree view";
	}

	/** Return singleton */
	public static LogicalTreeView getInstance() {
		if (ltv == null) {
			ltv = new LogicalTreeView();
		}
		return ltv;
	}

	/** Constructor */
	public LogicalTreeView(){
		//**Menu items**
		//Style menu
		jmenuStyle = new JPopupMenu();
		jmiStylePlay = new JMenuItem("Play");
		jmiStylePlay.addActionListener(this);
		jmiStylePush = new JMenuItem("Push");
		jmiStylePush.addActionListener(this);
		jmiStylePlayShuffle = new JMenuItem("Play shuffle");
		jmiStylePlayShuffle.addActionListener(this);
		jmiStylePlayRepeat = new JMenuItem("Play repeat");
		jmiStylePlayRepeat.addActionListener(this);
		jmiStyleDelete = new JMenuItem("Delete");
		jmiStyleDelete.addActionListener(this);
		jmiStyleSetProperty = new JMenuItem("Set a property");
		jmiStyleSetProperty.addActionListener(this);
		jmiStyleProperties = new JMenuItem("Properties");
		jmiStyleProperties.addActionListener(this);
		jmenuStyle.add(jmiStylePlay);
		jmenuStyle.add(jmiStylePush);
		jmenuStyle.add(jmiStylePlayShuffle);
		jmenuStyle.add(jmiStylePlayRepeat);
		jmenuStyle.add(jmiStyleDelete);
		jmenuStyle.add(jmiStyleSetProperty);
		jmenuStyle.add(jmiStyleProperties);
		
		//Author menu
		jmenuAuthor = new JPopupMenu();
		jmiAuthorPlay = new JMenuItem("Play");
		jmiAuthorPlay.addActionListener(this);
		jmiAuthorPush = new JMenuItem("Push");
		jmiAuthorPush.addActionListener(this);
		jmiAuthorPlayShuffle = new JMenuItem("Play shuffle");
		jmiAuthorPlayShuffle.addActionListener(this);
		jmiAuthorPlayRepeat = new JMenuItem("Play repeat");
		jmiAuthorPlayRepeat.addActionListener(this);
		jmiAuthorDelete = new JMenuItem("Delete");
		jmiAuthorDelete.addActionListener(this);
		jmiAuthorSetProperty = new JMenuItem("Set a property");
		jmiAuthorSetProperty.addActionListener(this);
		jmiAuthorProperties = new JMenuItem("Properties");
		jmiAuthorProperties.addActionListener(this);
		jmenuAuthor.add(jmiAuthorPlay);
		jmenuAuthor.add(jmiAuthorPush);
		jmenuAuthor.add(jmiAuthorPlayShuffle);
		jmenuAuthor.add(jmiAuthorPlayRepeat);
		jmenuAuthor.add(jmiAuthorDelete);
		jmenuAuthor.add(jmiAuthorSetProperty);
		jmenuAuthor.add(jmiAuthorProperties);
		
		//Album menu
		jmenuAlbum = new JPopupMenu();
		jmiAlbumPlay = new JMenuItem("Play");
		jmiAlbumPlay.addActionListener(this);
		jmiAlbumPush = new JMenuItem("Push");
		jmiAlbumPush.addActionListener(this);
		jmiAlbumPlayShuffle = new JMenuItem("Play shuffle");
		jmiAlbumPlayShuffle.addActionListener(this);
		jmiAlbumPlayRepeat = new JMenuItem("Play repeat");
		jmiAlbumPlayRepeat.addActionListener(this);
		jmiAlbumDelete = new JMenuItem("Delete");
		jmiAlbumDelete.addActionListener(this);
		jmiAlbumSetProperty = new JMenuItem("Set a property");
		jmiAlbumSetProperty.addActionListener(this);
		jmiAlbumProperties = new JMenuItem("Properties");
		jmiAlbumProperties.addActionListener(this);
		jmenuAlbum.add(jmiAlbumPlay);
		jmenuAlbum.add(jmiAlbumPush);
		jmenuAlbum.add(jmiAlbumPlayShuffle);
		jmenuAlbum.add(jmiAlbumPlayRepeat);
		jmenuAlbum.add(jmiAlbumDelete);
		jmenuAlbum.add(jmiAlbumSetProperty);
		jmenuAlbum.add(jmiAlbumProperties);
		
		//Track menu
		jmenuTrack = new JPopupMenu();
		jmiTrackPlay = new JMenuItem("Play");
		jmiTrackPlay.addActionListener(this);
		jmiTrackPush = new JMenuItem("Push");
		jmiTrackPush.addActionListener(this);
		jmiTrackDelete = new JMenuItem("Delete");
		jmiTrackDelete.addActionListener(this);
		jmiTrackSetProperty = new JMenuItem("Set a property");
		jmiTrackSetProperty.addActionListener(this);
		jmiTrackProperties = new JMenuItem("Properties");
		jmiTrackProperties.addActionListener(this);
		jmenuTrack.add(jmiTrackPlay);
		jmenuTrack.add(jmiTrackPush);
		jmenuTrack.add(jmiTrackDelete);
		jmenuTrack.add(jmiTrackSetProperty);
		jmenuTrack.add(jmiTrackProperties);

		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//fill the tree
		top = new DefaultMutableTreeNode("Collection");
		populate();
		
		jtree = new JTree(top);
		jtree.putClientProperty("JTree.lineStyle", "Angled");
		jtree.setRowHeight(25);
		jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		jtree.setCellRenderer(new DefaultTreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				setFont(new Font("Dialog",Font.PLAIN,10));
				if (value instanceof StyleNode ){
					setIcon(new ImageIcon(ICON_STYLE));
				}
				else if (value instanceof AuthorNode){
					setIcon(new ImageIcon(ICON_AUTHOR));
				}
				else if (value instanceof AlbumNode){
					setIcon(new ImageIcon(ICON_ALBUM));
				}
				else if (value instanceof TrackNode){
					setIcon(new ImageIcon(ICON_FILE));
				}
				return this;
			}
		});
		DefaultTreeModel treeModel = new DefaultTreeModel(top);
		//Tree model listener to detect changes in the tree structure
		treeModel.addTreeModelListener(new TreeModelListener(){
			
			public void treeNodesChanged(TreeModelEvent e) {
				DefaultMutableTreeNode node;
				node = (DefaultMutableTreeNode)
				(e.getTreePath().getLastPathComponent());
				
				try {
					int index = e.getChildIndices()[0];
					node = (DefaultMutableTreeNode)
					(node.getChildAt(index));
				} catch (NullPointerException exc) {}
				
			}
			
			public void treeNodesInserted(TreeModelEvent e) {
			}
			
			public void treeNodesRemoved(TreeModelEvent e) {
			}
			
			public void treeStructureChanged(TreeModelEvent e) {
			}
			
		});
		
		//Tree selection listener to detect a selection
		jtree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
			}
		});
		//Listen for double clic
		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
				if ( e.getClickCount() == 2){
					Object o = path.getLastPathComponent();
					if (o instanceof TrackNode){
						Track track = ((TrackNode)o).getTrack();
						File file = track.getPlayeableFile();
						if (file != null){
							FIFO.getInstance().push(file,false);
						}
						else{
							Messages.showErrorMessage("010",track.getName());
						}
					}
				}
				else if ( jtree.getSelectionCount() > 0 && e.getClickCount() == 1 && e.getButton()==MouseEvent.BUTTON3){  //right clic on a selected node set
					TreePath[] paths = jtree.getSelectionModel().getSelectionPaths();
					tsTracks = new TreeSet();
					tsStyles = new TreeSet();
					tsAuthors = new TreeSet();
					tsAlbums = new TreeSet();
					Object o = paths[0].getLastPathComponent();
					//Track selection
					if ( o instanceof TrackNode  ){
						for (int i=0;i<paths.length;i++){
							o = paths[i].getLastPathComponent();
							if ( o instanceof TrackNode){
								Track track = ((TrackNode)o).getTrack();
								if (track.getPlayeableFile() != null){
									tsTracks.add(((TrackNode)o).getTrack());
								}
								else{
									Messages.showErrorMessage("010");
									return; //show no mounted file error
								}
							}
							else{
								return; //we don't accept different type selections
							}
						}
						jmenuTrack.show(jtree,e.getX(),e.getY());
					}
					//style selection
					else if ( o instanceof StyleNode ){
						for (int i=0;i<paths.length;i++){
							o = paths[i].getLastPathComponent();
							if ( o instanceof StyleNode){
								Style style = ((StyleNode)o).getStyle();
								tsStyles.add(((StyleNode)o).getStyle());
							}
							else{
								return; //we don't accept different type selections
							}
						}
						jmenuStyle.show(jtree,e.getX(),e.getY());
					}
					//				author selection
					else if ( o instanceof AuthorNode ){
						for (int i=0;i<paths.length;i++){
							o = paths[i].getLastPathComponent();
							if ( o instanceof AuthorNode){
								Author author = ((AuthorNode)o).getAuthor();
								tsAuthors.add(((AuthorNode)o).getAuthor());
							}
							else{
								return; //we don't accept different type selections
							}
						}
						jmenuAuthor.show(jtree,e.getX(),e.getY());
					}
					//			album selection
					else if ( o instanceof AlbumNode ){
						for (int i=0;i<paths.length;i++){
							o = paths[i].getLastPathComponent();
							if ( o instanceof AlbumNode){
								Album album = ((AlbumNode)o).getAlbum();
								tsAlbums.add(((AlbumNode)o).getAlbum());
							}
							else{
								return; //we don't accept different type selections
							}
						}
						jmenuAlbum.show(jtree,e.getX(),e.getY());
					}
				}
			}
		};
		jtree.addMouseListener(ml);
		
		
		//expand all
		for (int i=0;i<jtree.getRowCount();i++){
			Object o = jtree.getPathForRow(i).getLastPathComponent(); 
			if ( !(o instanceof AlbumNode) && !(o instanceof TrackNode)){
				jtree.expandRow(i); 
			}
		}
		add(new JScrollPane(jtree));
	}
	
	/**Fill the tree */
	public void populate(){
		ArrayList alTracks = TrackManager.getSortedTracks();
		Iterator it1 = alTracks.iterator();
		while ( it1.hasNext()){
			Track track = (Track)it1.next();
			StyleNode styleNode = null;
			Style style=track.getStyle();
			AuthorNode authorNode = null;
			Author author = track.getAuthor();
			AlbumNode albumNode = null;
			Album album = track.getAlbum();
			
			//create style
			Enumeration e = top.children();
			boolean b = false;
			while (e.hasMoreElements()){  //check the style doesn't already exist
				StyleNode sn = (StyleNode)e.nextElement();
				if ( sn.getStyle().equals(style)){
					b = true;
					styleNode = sn;
					break;
				}
			}
			if ( !b){
				styleNode = new StyleNode(style);
				top.add(styleNode);
			}
			//create author
			e = styleNode.children();
			b = false;
			while (e.hasMoreElements()){  //check if the author doesn't already exist
				AuthorNode an = (AuthorNode)e.nextElement();
				if ( an.getAuthor().equals(author)){
					b = true;
					authorNode = an;
					break;
				}
			}
			if ( !b){
				authorNode = new AuthorNode(author);
				styleNode.add(authorNode);
			}
			//create album
			e = authorNode.children();
			b = false;
			while (e.hasMoreElements()){  //check if the album doesn't already exist
				AlbumNode an = (AlbumNode)e.nextElement();
				if ( an.getAlbum().equals(album)){
					b = true;
					albumNode = an;
					break;
				}
			}
			if ( !b){
				albumNode = new AlbumNode(album);
				authorNode.add(albumNode);
			}
			//create track
			albumNode.add(new TrackNode(track));
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		//Play or push on a set of tracks
		if (e.getSource() == jmiTrackPlay || e.getSource() == jmiTrackPush){
			ArrayList alFilesToPlay = new ArrayList(tsTracks.size());
			Iterator it = tsTracks.iterator();
			while ( it.hasNext()){
				File file = ((Track)it.next()).getPlayeableFile();
				if ( file != null){
					alFilesToPlay.add(file);
				}
			}
			Collections.sort(alFilesToPlay);
			if (e.getSource() == jmiTrackPlay){
				FIFO.getInstance().push(alFilesToPlay,false);
			}
			else {
				FIFO.getInstance().push(alFilesToPlay,true);
			}
		}
		//play or push on a set of styles
		else if (e.getSource() == jmiStylePlay || e.getSource() == jmiStylePush){
			ArrayList alFilesToPlay = new ArrayList(100); //files to be played
			TreeSet tsTracksToPlay = new TreeSet();
			Iterator itStyles = tsStyles.iterator();
			while (itStyles.hasNext()){
				Style style = (Style)itStyles.next();
				ArrayList alAllTracks = TrackManager.getTracks();
				Iterator itTracks = alAllTracks.iterator();
				while (itTracks.hasNext()){
					Track track = (Track)itTracks.next();
					if (track.getStyle().equals(style)){
						tsTracksToPlay.add(track);
					}
				}
				Iterator it = tsTracksToPlay.iterator();
				while ( it.hasNext()){
					alFilesToPlay.add(((Track)it.next()).getPlayeableFile());
				}
				if (e.getSource() == jmiStylePlay){
					FIFO.getInstance().push(alFilesToPlay,false);
				}
				else {
					FIFO.getInstance().push(alFilesToPlay,true);
				}
			}	
			
		}
		//		play or push on a set of authors
		else if (e.getSource() == jmiAuthorPlay || e.getSource() == jmiAuthorPush){
			ArrayList alFilesToPlay = new ArrayList(100); //files to be played
			TreeSet tsTracksToPlay = new TreeSet();
			Iterator itAuthors = tsAuthors.iterator();
			while (itAuthors.hasNext()){
				Author author = (Author)itAuthors.next();
				Iterator itTracks = TrackManager.getTracks().iterator();
				while (itTracks.hasNext()){
					Track track = (Track)itTracks.next();
					if (track.getAuthor().equals(author)){
						tsTracksToPlay.add(track);
					}
				}
				Iterator it = tsTracksToPlay.iterator();
				while ( it.hasNext()){
					alFilesToPlay.add(((Track)it.next()).getPlayeableFile());
				}
				if (e.getSource() == jmiAuthorPlay){
					FIFO.getInstance().push(alFilesToPlay,false);
				}
				else {
					FIFO.getInstance().push(alFilesToPlay,true);
				}
			}	
			
		}
		//		play or push on a set of albums
		else if (e.getSource() == jmiAlbumPlay || e.getSource() == jmiAlbumPush){
			ArrayList alFilesToPlay = new ArrayList(100); //files to be played
			TreeSet tsTracksToPlay = new TreeSet();
			Iterator itAlbums = tsAlbums.iterator();
			while (itAlbums.hasNext()){
				Album album = (Album)itAlbums.next();
				Iterator itTracks = TrackManager.getTracks().iterator();
				while (itTracks.hasNext()){
					Track track = (Track)itTracks.next();
					if (track.getAlbum().equals(album)){
						tsTracksToPlay.add(track);
					}
				}
				Iterator it = tsTracksToPlay.iterator();
				while ( it.hasNext()){
					alFilesToPlay.add(((Track)it.next()).getPlayeableFile());
				}
				if (e.getSource() == jmiAlbumPlay){
					FIFO.getInstance().push(alFilesToPlay,false);
				}
				else {
					FIFO.getInstance().push(alFilesToPlay,true);
				}
			}	
		}
	}	
}


/**
 * Style node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class StyleNode extends DefaultMutableTreeNode{
	
	/**Associated style*/
	private Style style;
	
	/**
	 * Constructor
	 * @param file
	 */
	public StyleNode(Style style){
		this.style = style;
	}
	
	/**
	 * return a string representation of this style node
	 */
	public String toString(){
		return style.getName2();
	}
	/**
	 * @return Returns the style
	 */
	public Style getStyle() {
		return style;
	}

}

/**
 * Author node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class AuthorNode extends DefaultMutableTreeNode{
	
	/**Associated device*/
	private Author author;
	
	/**device -> deviceNode hashmap */
	public static HashMap hmAuthorAuthorNode = new HashMap(100);
	
	/**
	 * Constructor
	 * @param device
	 */
	public AuthorNode(Author author){
		this.author = author;
		hmAuthorAuthorNode.put(author,this);
	}
	
	/**Return associated device node */
	public static AuthorNode getAuthorNode(Author device){
		return (AuthorNode)hmAuthorAuthorNode.get(device);
	}
	
	/**
	 * return a string representation of this device node
	 */
	public String toString(){
		return author.getName2();
	}
	/**
	 * @return Returns the device.
	 */
	public Author getAuthor() {
		return author;
	}

}


/**
 * Album node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class AlbumNode  extends DefaultMutableTreeNode{
	
	/**Associated Album*/
	private Album album;
	
	/**album -> albumNode hashmap */
	public static HashMap hmAlbumAlbumNode = new HashMap(100);
	
	/**
	 * Constructor
	 * @param Album
	 */
	public AlbumNode(Album album){
		this.album = album;
		hmAlbumAlbumNode.put(album,this);
	}
	
	/**Return associated album node */
	public static AlbumNode getAlbumNode(Album album){
		return (AlbumNode)hmAlbumAlbumNode.get(album);
	}
	
	/**
	 * return a string representation of this album node
	 */
	public String toString(){
		return album.getName2();
	}
	/**
	 * @return Returns the album.
	 */
	public Album getAlbum() {
		return album;
	}

}

/**
 * Track node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class TrackNode  extends DefaultMutableTreeNode{
	
	/**Associated Track*/
	private Track track;
	
	/**track -> trackNode hashmap */
	public static HashMap hmTrackTrackNode = new HashMap(100);
	
	/**
	 * Constructor
	 * @param Track
	 */
	public TrackNode(Track track){
		this.track = track;
		hmTrackTrackNode.put(track,this);
	}
	
	/**Return associated track node */
	public static TrackNode getTrackNode(Track track){
		return (TrackNode)hmTrackTrackNode.get(track);
	}
	
	/**
	 * return a string representation of this track node
	 */
	public String toString(){
		return track.getName();
	}
	/**
	 * @return Returns the track.
	 */
	public Track getTrack() {
		return track;
	}

}



