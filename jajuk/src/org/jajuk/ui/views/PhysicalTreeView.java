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
import java.util.HashMap;
import java.util.Iterator;

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

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

/**
 * Physical tree view
 * 
 * @author bflorat @created 28 nov. 2003
 */
public class PhysicalTreeView extends ViewAdapter implements ActionListener{

	/** Self instance */
	private static PhysicalTreeView ptv;

	/** The phyical tree */
	JTree jtree;
	
	/** Top tree node */
	DefaultMutableTreeNode top;
	
	/** Files selection*/
	ArrayList alFiles;
	
	/** Directories selection*/
	ArrayList alDirs;
	
	/** Devices selection*/
	ArrayList alDevices;
	
	/** Files menu item */
	JPopupMenu jmenuFile;
		JMenuItem jmiFilePlay;
		JMenuItem jmiFilePush;
		JMenuItem jmiFileCopy;
		JMenuItem jmiFileCut;
		JMenuItem jmiFilePaste;
		JMenuItem jmiFileRename;
		JMenuItem jmiFileDelete;
		JMenuItem jmiFileSetProperty;
		JMenuItem jmiFileProperties;
	JPopupMenu jmenuDir;
		JMenuItem jmiDirPlay;
		JMenuItem jmiDirPush;
		JMenuItem jmiDirPlayShuffle;
		JMenuItem jmiDirPlayRepeat;
		JMenuItem jmiDirDesynchro;
		JMenuItem jmiDirCreatePlaylist;
		JMenuItem jmiDirCopy;
		JMenuItem jmiDirCut;
		JMenuItem jmiDirPaste;
		JMenuItem jmiDirRename;
		JMenuItem jmiDirDelete;
		JMenuItem jmiDirSetProperty;
		JMenuItem jmiDirProperties;
		

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Physical tree view";
	}

	/** Return singleton */
	public static PhysicalTreeView getInstance() {
		if (ptv == null) {
			ptv = new PhysicalTreeView();
		}
		return ptv;
	}

	/** Constructor */
	public PhysicalTreeView(){
		//**Menu items**
		//File menu
		jmenuFile = new JPopupMenu();
		jmiFilePlay = new JMenuItem("Play");
		jmiFilePlay.addActionListener(this);
		jmiFilePush = new JMenuItem("Push");
		jmiFilePush.addActionListener(this);
		jmiFileCopy = new JMenuItem("Copy");
		jmiFileCopy.addActionListener(this);
		jmiFileCut = new JMenuItem("Cut");
		jmiFileCut.addActionListener(this);
		jmiFilePaste = new JMenuItem("Paste");
		jmiFilePaste.addActionListener(this);
		jmiFileRename = new JMenuItem("Rename");
		jmiFileRename.addActionListener(this);
		jmiFileDelete = new JMenuItem("Delete");
		jmiFileDelete.addActionListener(this);
		jmiFileSetProperty = new JMenuItem("Set a property");
		jmiFileSetProperty.addActionListener(this);
		jmiFileProperties = new JMenuItem("Properties");
		jmiFileProperties.addActionListener(this);
		jmenuFile.add(jmiFilePlay);
		jmenuFile.add(jmiFilePush);
		jmenuFile.add(jmiFileCopy);
		jmenuFile.add(jmiFileCut);
		jmenuFile.add(jmiFilePaste);
		jmenuFile.add(jmiFileRename);
		jmenuFile.add(jmiFileDelete);
		jmenuFile.add(jmiFileSetProperty);
		jmenuFile.add(jmiFileProperties);
		//Directory menu
		jmenuDir = new JPopupMenu();
		jmiDirPlay = new JMenuItem("Play");
		jmiDirPlay.addActionListener(this);
		jmiDirPush = new JMenuItem("Push");
		jmiDirPush.addActionListener(this);
		jmiDirPlayShuffle = new JMenuItem("Play Shuffle");
		jmiDirPlayShuffle.addActionListener(this);
		jmiDirPlayRepeat = new JMenuItem("Play repeat");
		jmiDirPlayRepeat.addActionListener(this);
		jmiDirDesynchro = new JMenuItem("Desynchronize");
		jmiDirDesynchro.addActionListener(this);
		jmiDirCreatePlaylist = new JMenuItem("Create playlist");
		jmiDirCreatePlaylist.addActionListener(this);
		jmiDirCopy = new JMenuItem("Copy");
		jmiDirCopy.addActionListener(this);
		jmiDirCut = new JMenuItem("Cut");
		jmiDirCut.addActionListener(this);
		jmiDirPaste = new JMenuItem("Paste");
		jmiDirPaste.addActionListener(this);
		jmiDirRename = new JMenuItem("Rename");
		jmiDirRename.addActionListener(this);
		jmiDirDelete = new JMenuItem("Delete");
		jmiDirDelete.addActionListener(this);
		jmiDirSetProperty = new JMenuItem("Set a property");
		jmiDirSetProperty.addActionListener(this);
		jmiDirProperties = new JMenuItem("Properties");
		jmiDirProperties.addActionListener(this);
		jmenuDir.add(jmiDirPlay);
		jmenuDir.add(jmiDirPush);
		jmenuDir.add(jmiDirPlayShuffle);
		jmenuDir.add(jmiDirPlayRepeat);
		jmenuDir.add(jmiDirDesynchro);
		jmenuDir.add(jmiDirCreatePlaylist);
		jmenuDir.add(jmiDirCopy);
		jmenuDir.add(jmiDirCut);
		jmenuDir.add(jmiDirPaste);
		jmenuDir.add(jmiDirRename);
		jmenuDir.add(jmiDirDelete);
		jmenuDir.add(jmiDirSetProperty);
		jmenuDir.add(jmiDirProperties);
		
		
		
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
				if (value instanceof FileNode ){
					setIcon(new ImageIcon(ICON_FILE));
					//TODO set real icons
				}
				else if (value instanceof PlaylistFileNode){
					setIcon(new ImageIcon(ICON_PLAYLIST_FILE));
				}
				else if (value instanceof DeviceNode){
					switch (((DeviceNode)value).getDevice().getDeviceType()){
						case 0 : 
							setIcon(new ImageIcon(ICON_DEVICE_DIRECTORY_MOUNTED));
							break;
					case 1 : 
						setIcon(new ImageIcon(ICON_DEVICE_CD_MOUNTED));
						break;
					case 2 : 
						setIcon(new ImageIcon(ICON_DEVICE_CD_AUDIO_MOUNTED));
						break;
					case 3 : 
						setIcon(new ImageIcon(ICON_DEVICE_REMOTE_MOUNTED));
						break;
					case 4 : 
						setIcon(new ImageIcon(ICON_DEVICE_EXT_DD_MOUNTED));
						break;
					case 5 : 
						setIcon(new ImageIcon(ICON_DEVICE_PLAYER_MOUNTED));
						break;
				
					}
				}
				else if (value instanceof DirectoryNode){
					setIcon(new ImageIcon(ICON_DIRECTORY));
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
					if (o instanceof FileNode){
						File file = ((FileNode)o).getFile();
						if (file.getDirectory().getDevice().isMounted()){
							FIFO.getInstance().push(file,false);
						}
						else{
							Messages.showErrorMessage("120",file.getDirectory().getDevice().getName());
						}
					}
				}
				else if ( jtree.getSelectionCount() > 0 && e.getClickCount() == 1 && e.getButton()==MouseEvent.BUTTON3){  //right clic on a selected node set
					//Only keep files
					//TODO accept playlists
					TreePath[] paths = jtree.getSelectionModel().getSelectionPaths();
					alFiles = new ArrayList(paths.length);
					alDirs = new ArrayList(paths.length);
					alDevices = new ArrayList(paths.length);
					Object o = paths[0].getLastPathComponent();
					//File or playlist selection
					if ( o instanceof FileNode || o instanceof PlaylistFileNode ){
						for (int i=0;i<paths.length;i++){
							o = paths[i].getLastPathComponent();
							if ( o instanceof FileNode || o instanceof PlaylistFileNode){
								File file = ((FileNode)o).getFile();
								if (file.getDirectory().getDevice().isMounted()){
									alFiles.add(((FileNode)o).getFile());
								}
								else{
									Messages.showErrorMessage("120");
									return; //show only one error message
								}
							}
							else{
								return; //we don't accept different type selections
							}
						}
						jmenuFile.show(jtree,e.getX(),e.getY());
					}
					//directory selection
					if ( o instanceof DirectoryNode ){
						for (int i=0;i<paths.length;i++){
							o = paths[i].getLastPathComponent();
							if ( o instanceof DirectoryNode){
								Directory dir = ((DirectoryNode)o).getDirectory();
								if (dir.getDevice().isMounted()){
									alDirs.add(((DirectoryNode)o).getDirectory());
								}
								else{
									Messages.showErrorMessage("120");
									return; //show only one error message
								}
							}
							else{
								return; //we don't accept different type selections
							}
						}
						jmenuDir.show(jtree,e.getX(),e.getY());
					}
				}
				
			}
		 };
		 jtree.addMouseListener(ml);
		
		
		//expand all
		for (int i=0;i<jtree.getRowCount();i++){
			Object o = jtree.getPathForRow(i).getLastPathComponent(); 
			if ( o instanceof DeviceNode && ((DeviceNode)o).getDevice().isMounted()){
				jtree.expandRow(i); 
			}
			else if (o instanceof DirectoryNode && ((DirectoryNode)o).getDirectory().getFiles().size()==0){
				jtree.expandRow(i);
			}
		}
		add(new JScrollPane(jtree));
	}
	
	/**Fill the tree */
	public void populate(){
		//add devices
		Iterator it1 = DeviceManager.getDevices().iterator();
		while ( it1.hasNext()){
			Device device = (Device)it1.next();
			DefaultMutableTreeNode nodeDevice = new DeviceNode(device);
			top.add(nodeDevice);
		}
		//add directories
		Iterator it2 = DirectoryManager.getDirectories().iterator();
		while (it2.hasNext()){
			Directory directory = (Directory)it2.next();
			if (!directory.getName().equals("")){ //device root directory, do not display
				if (directory.getParentDirectory().getName().equals("")){  //parent directory is a device
					DeviceNode deviceNode = DeviceNode.getDeviceNode(directory.getDevice());
					if ( deviceNode != null){
						deviceNode.add(new DirectoryNode(directory));
					}
				}
				else{  //parent directory not root 
					DirectoryNode parentDirectoryNode = DirectoryNode.getDirectoryNode(directory.getParentDirectory());
					if (parentDirectoryNode != null){ //paranoia check
						parentDirectoryNode.add(new DirectoryNode(directory));
					}
				}
			}
		}
		//add files
		Iterator it3 = FileManager.getFiles().iterator();
		while (it3.hasNext()){
			File file = (File)it3.next();
			DirectoryNode directoryNode = DirectoryNode.getDirectoryNode(file.getDirectory());
			if (directoryNode != null){
				directoryNode.add(new FileNode(file));
			}
		}
		//add playlist files
		Iterator it4 = PlaylistFileManager.getPlaylistFiles().iterator();
		while (it4.hasNext()){
			PlaylistFile playlistFile = (PlaylistFile)it4.next();
			DirectoryNode directoryNode = DirectoryNode.getDirectoryNode(playlistFile.getDirectory());
			if (directoryNode != null){
				directoryNode.add(new PlaylistFileNode(playlistFile));
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jmiFilePlay ){
			FIFO.getInstance().push(alFiles,false);
		}
		else if (e.getSource() == jmiFilePush ){
			FIFO.getInstance().push(alFiles,true);
		}
		else if (e.getSource() == jmiDirPlay  || e.getSource() == jmiDirPush || e.getSource() == jmiDirPlayShuffle){
			ArrayList alFiles = new ArrayList(100); //files to be played
			Iterator it = alDirs.iterator();
			while (it.hasNext()){
				Directory dir = (Directory)it.next();
				Iterator it2  = FileManager.getFiles().iterator();
				while (it2.hasNext()){
					File file = (File)it2.next();
					if ( file.getDirectory().getDevice().isMounted() && file.hasAncestor(dir)){  //mount test is only for performance reasons 
						alFiles.add(file);
					}
				}
			}
			if (e.getSource() == jmiDirPlay){
				FIFO.getInstance().push(alFiles,false);
			}
			else if (e.getSource() == jmiDirPush){
				FIFO.getInstance().push(alFiles,true);
			}
			else if (e.getSource() == jmiDirPlayShuffle){
				FIFO.getInstance().push(Util.randomize(alFiles),false);
			}
		}
	}

	

}


/**
 * File node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class FileNode extends DefaultMutableTreeNode{
	
	/**Associated file*/
	private File file;
	
	/**
	 * Constructor
	 * @param file
	 */
	public FileNode(File file){
		this.file = file;
	}
	
	/**
	 * return a string representation of this file node
	 */
	public String toString(){
		return file.getName();
	}
	/**
	 * @return Returns the file.
	 */
	public File getFile() {
		return file;
	}

}

/**
 * Device node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class DeviceNode extends DefaultMutableTreeNode{
	
	/**Associated device*/
	private Device device;
	
	/**device -> deviceNode hashmap */
	public static HashMap hmDeviceDeviceNode = new HashMap(100);
	
	/**
	 * Constructor
	 * @param device
	 */
	public DeviceNode(Device device){
		this.device = device;
		hmDeviceDeviceNode.put(device,this);
	}
	
	/**Return associated device node */
	public static DeviceNode getDeviceNode(Device device){
		return (DeviceNode)hmDeviceDeviceNode.get(device);
	}
	
	/**
	 * return a string representation of this device node
	 */
	public String toString(){
		return device.getName();
	}
	/**
	 * @return Returns the device.
	 */
	public Device getDevice() {
		return device;
	}

}


/**
 * Directory node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class DirectoryNode  extends DefaultMutableTreeNode{
	
	/**Associated Directory*/
	private Directory directory;
	
	/**directory -> directoryNode hashmap */
	public static HashMap hmDirectoryDirectoryNode = new HashMap(100);
	
	/**
	 * Constructor
	 * @param Directory
	 */
	public DirectoryNode(Directory directory){
		this.directory = directory;
		hmDirectoryDirectoryNode.put(directory,this);
	}
	
	/**Return associated directory node */
	public static DirectoryNode getDirectoryNode(Directory directory){
		return (DirectoryNode)hmDirectoryDirectoryNode.get(directory);
	}
	
	/**
	 * return a string representation of this directory node
	 */
	public String toString(){
		return directory.getName();
	}
	/**
	 * @return Returns the directory.
	 */
	public Directory getDirectory() {
		return directory;
	}

}

/**
 * PlaylistFile node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class PlaylistFileNode  extends DefaultMutableTreeNode{
	
	/**Associated PlaylistFile*/
	private PlaylistFile playlistFile;
	
	/**
	 * Constructor
	 * @param PlaylistFile
	 */
	public PlaylistFileNode(PlaylistFile playlistFile){
		this.playlistFile = playlistFile;
	}
	
	/**
	 * return a string representation of this playlistFile node
	 */
	public String toString(){
		return playlistFile.getName();
	}
}
