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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
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
import org.jajuk.ui.ObservationManager;
import org.jajuk.util.Util;

/**
 * Physical tree view
 * 
 * @author bflorat 
 * @created 28 nov. 2003
 */
public class PhysicalTreeView extends ViewAdapter implements ActionListener,org.jajuk.ui.Observer{
	
	/** Self instance */
	private static PhysicalTreeView ptv;
	
	/** The tree scrollpane*/
	JScrollPane jspTree;
	
	/** The phyical tree */
	JTree jtree;
	
	/** Top tree node */
	DefaultMutableTreeNode top;
	
	/** Files selection*/
	ArrayList alFiles;
	
	/** Current selection */
	TreePath[] paths;
	
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
		JMenuItem jmiDirResynchro;
		JMenuItem jmiDirCreatePlaylist;
		JMenuItem jmiDirCopy;
		JMenuItem jmiDirCut;
		JMenuItem jmiDirPaste;
		JMenuItem jmiDirRename;
		JMenuItem jmiDirDelete;
		JMenuItem jmiDirSetProperty;
		JMenuItem jmiDirProperties;
	JPopupMenu jmenuDev;
		JMenuItem jmiDevPlay;
		JMenuItem jmiDevPush;
		JMenuItem jmiDevPlayShuffle;
		JMenuItem jmiDevPlayRepeat;
		JMenuItem jmiDevCreatePlaylist;
		JMenuItem jmiDevMount;
		JMenuItem jmiDevUnmount;
		JMenuItem jmiDevRefresh;
		JMenuItem jmiDevSynchronize;
		JMenuItem jmiDevTest;
		JMenuItem jmiDevSetProperty;
		JMenuItem jmiDevProperties;
		
		
	
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
		jmiDirResynchro = new JMenuItem("Resynchronize");
		jmiDirResynchro.addActionListener(this);
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

		//Device menu
		jmenuDev = new JPopupMenu();
		jmiDevPlay = new JMenuItem("Play");
		jmiDevPlay.addActionListener(this);
		jmiDevPush = new JMenuItem("Push");
		jmiDevPush.addActionListener(this);
		jmiDevPlayShuffle = new JMenuItem("Play Shuffle");
		jmiDevPlayShuffle.addActionListener(this);
		jmiDevPlayRepeat = new JMenuItem("Play repeat");
		jmiDevPlayRepeat.addActionListener(this);
		jmiDevMount = new JMenuItem("Mount");
		jmiDevMount.addActionListener(this);
		jmiDevUnmount = new JMenuItem("Unmount");
		jmiDevUnmount.addActionListener(this);
		jmiDevRefresh = new JMenuItem("Refresh");
		jmiDevRefresh.addActionListener(this);
		jmiDevSynchronize = new JMenuItem("Synchronize");
		jmiDevSynchronize.addActionListener(this);
		jmiDevTest = new JMenuItem("Test");
		jmiDevTest.addActionListener(this);
		jmiDevCreatePlaylist = new JMenuItem("Create playlists");
		jmiDevCreatePlaylist.addActionListener(this);
		jmiDevSetProperty = new JMenuItem("Set a property");
		jmiDevSetProperty.addActionListener(this);
		jmiDevProperties = new JMenuItem("Properties");
		jmiDevProperties.addActionListener(this);
		jmenuDev.add(jmiDevPlay);
		jmenuDev.add(jmiDevPush);
		jmenuDev.add(jmiDevPlayShuffle);
		jmenuDev.add(jmiDevPlayRepeat);
		jmenuDev.add(jmiDevMount);
		jmenuDev.add(jmiDevUnmount);
		jmenuDev.add(jmiDevRefresh);
		jmenuDev.add(jmiDevSynchronize);
		jmenuDev.add(jmiDevTest);
		jmenuDev.add(jmiDevCreatePlaylist);
		jmenuDev.add(jmiDevSetProperty);
		jmenuDev.add(jmiDevProperties);
		
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//fill the tree
		top = new DefaultMutableTreeNode("Collection");
		populate();
			
		jtree = new JTree(top);
		jtree.putClientProperty("JTree.lineStyle", "Angled");
		jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		jtree.setCellRenderer(new DefaultTreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				setFont(new Font("Dialog",Font.PLAIN,10));
				if (value instanceof FileNode ){
					setIcon(new ImageIcon(ICON_FILE));
				}
				else if (value instanceof PlaylistFileNode){
					setIcon(new ImageIcon(ICON_PLAYLIST_FILE));
				}
				else if (value instanceof DeviceNode){
					Device device = (Device)((DeviceNode)value).getDevice();
					switch ( device.getDeviceType()){
						case 0 : 
							if ( device.isMounted())	setIcon(new ImageIcon(ICON_DEVICE_DIRECTORY_MOUNTED_SMALL));
							else setIcon(new ImageIcon(ICON_DEVICE_DIRECTORY_UNMOUNTED_SMALL));
							break;
						case 1 : 
							if ( device.isMounted())	setIcon(new ImageIcon(ICON_DEVICE_CD_MOUNTED_SMALL));
							else setIcon(new ImageIcon(ICON_DEVICE_CD_UNMOUNTED_SMALL));
							break;
						case 2 : 
							if ( device.isMounted())	setIcon(new ImageIcon(ICON_DEVICE_CD_AUDIO_MOUNTED_SMALL));
							else setIcon(new ImageIcon(ICON_DEVICE_CD_AUDIO_UNMOUNTED_SMALL));
							break;
						case 3 : 
							if ( device.isMounted())	setIcon(new ImageIcon(ICON_DEVICE_REMOTE_MOUNTED_SMALL));
							else setIcon(new ImageIcon(ICON_DEVICE_REMOTE_UNMOUNTED_SMALL));
							break;
						case 4 : 
							if ( device.isMounted())	setIcon(new ImageIcon(ICON_DEVICE_EXT_DD_MOUNTED_SMALL));
							else setIcon(new ImageIcon(ICON_DEVICE_EXT_DD_UNMOUNTED_SMALL));
							break;
						case 5 : 
							if ( device.isMounted())	setIcon(new ImageIcon(ICON_DEVICE_PLAYER_MOUNTED_SMALL));
							else setIcon(new ImageIcon(ICON_DEVICE_PLAYER_UNMOUNTED_SMALL));
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
						if (file.getDirectory().getDevice().isMounted() && !file.getDirectory().getDevice().isRefreshing()){
							FIFO.getInstance().push(file,false);
						}
						else{
							Messages.showErrorMessage("120",file.getDirectory().getDevice().getName());
						}
					}
				}
				else if ( jtree.getSelectionCount() > 0 && e.getClickCount() == 1 && e.getButton()==MouseEvent.BUTTON3){  //right clic on a selected node set
					//Only keep files
					//TODO TBI accept playlists
					paths = jtree.getSelectionModel().getSelectionPaths();
					alFiles = new ArrayList(100);
					//test mix between types ( not allowed )
					String sClass = paths[0].getLastPathComponent().getClass().toString();
					for (int i=0;i<paths.length;i++){
						if (!paths[i].getLastPathComponent().getClass().toString().equals(sClass)){
							return;
						}	
					}
					//get all components recursively
					for (int i=0;i<paths.length;i++){
						Object o = paths[i].getLastPathComponent();
						Enumeration e2 = ((DefaultMutableTreeNode)o).depthFirstEnumeration(); //return all childs nodes recursively
						while ( e2.hasMoreElements()){
							DefaultMutableTreeNode node = (DefaultMutableTreeNode)e2.nextElement();
							if (node instanceof FileNode){
								File file = ((FileNode)node).getFile();
								if (file.getDirectory().getDevice().isMounted() && !file.getDirectory().getDevice().isRefreshing()){
									alFiles.add(((FileNode)node).getFile());
								}
								else{
									if (!(o instanceof DeviceNode)){
										Messages.showErrorMessage("120");
										return; //show only one error message
									}
								}
							}
						}
					}
					//display menus according node type
					if (paths[0].getLastPathComponent() instanceof FileNode ){
						jmenuFile.show(jtree,e.getX(),e.getY());
					}
					else if (paths[0].getLastPathComponent() instanceof DirectoryNode){
						jmenuDir.show(jtree,e.getX(),e.getY());
					}
					else if (paths[0].getLastPathComponent() instanceof DeviceNode){
						jmenuDev.show(jtree,e.getX(),e.getY()); 
					}
				}
			}
		};
		jtree.addMouseListener(ml);
		
		
		//expand all
		for (int i=0;i<jtree.getRowCount();i++){
			Object o = jtree.getPathForRow(i).getLastPathComponent(); 
			if ( o instanceof DeviceNode && ((DeviceNode)o).getDevice().isMounted()  && !((DeviceNode)o).getDevice().isRefreshing()){
				jtree.expandRow(i); 
			}
			else if (o instanceof DirectoryNode && ((DirectoryNode)o).getDirectory().getFiles().size()==0){
				jtree.expandRow(i);
			}
		}
		jspTree = new JScrollPane(jtree);
		add(jspTree);
		//Register on the list for subject we are interrested in
		ObservationManager.register(EVENT_DEVICE_MOUNT,this);
		ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
		jtree.setRowHeight(25);
		
	}
	
	/**Fill the tree */
	public void populate(){
		//add devices
		ArrayList alDevices = DeviceManager.getDevices();
		Collections.sort(alDevices);
		Iterator it1 = alDevices.iterator();
		while ( it1.hasNext()){
			Device device = (Device)it1.next();
			DefaultMutableTreeNode nodeDevice = new DeviceNode(device);
			top.add(nodeDevice);
		}
		//add directories
		ArrayList alDirectories = DirectoryManager.getDirectories();
		Collections.sort(alDirectories);
		Iterator it2 = alDirectories.iterator();
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
		ArrayList alFiles = FileManager.getFiles();
		Collections.sort(alFiles);
		Iterator it3 = alFiles.iterator();
		while (it3.hasNext()){
			File file = (File)it3.next();
			DirectoryNode directoryNode = DirectoryNode.getDirectoryNode(file.getDirectory());
			if (directoryNode != null){
				directoryNode.add(new FileNode(file));
			}
		}
		//add playlist files
		ArrayList alPlaylistFiles = PlaylistFileManager.getPlaylistFiles();
		Collections.sort(alPlaylistFiles);
		Iterator it4 = alPlaylistFiles.iterator();
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
		if (e.getSource() == jmiFilePlay && alFiles.size() > 0 ){
			FIFO.getInstance().push(alFiles,false);
		}
		else if (e.getSource() == jmiFilePush  && alFiles.size() > 0){
			FIFO.getInstance().push(alFiles,true);
		}
		else if ( alFiles.size() > 0 && (e.getSource() == jmiDirPlay || e.getSource() == jmiDevPlay)){  
			FIFO.getInstance().push(alFiles,false);
		}
		else if (alFiles.size() > 0 && (e.getSource() == jmiDirPush || e.getSource() == jmiDevPush)){
			FIFO.getInstance().push(alFiles,true);
		}
		else if (alFiles.size() > 0 && (e.getSource() == jmiDirPlayShuffle || e.getSource() == jmiDevPlayShuffle)){
			FIFO.getInstance().push(Util.randomize(alFiles),false);
		}
		else if (alFiles.size() > 0 && (e.getSource() == jmiDirPlayRepeat || e.getSource() == jmiDevPlayRepeat)){
			FIFO.getInstance().push(alFiles,false,false,true);
		}
		else if ( e.getSource() == jmiDevMount){
			for (int i=0;i<paths.length;i++){
				Device device = ((DeviceNode)(paths[i].getLastPathComponent())).getDevice();
				try{
					device.mount();
					ObservationManager.notify(EVENT_DEVICE_MOUNT);
				}
				catch(Exception ex){
					Messages.showErrorMessage("011");
				}
			}
		}
		else if ( e.getSource() == jmiDevUnmount){
			for (int i=0;i<paths.length;i++){
				Device device = ((DeviceNode)(paths[i].getLastPathComponent())).getDevice();
				try{
					device.unmount();
				}
				catch(Exception ex){
					Messages.showErrorMessage("012");
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject) {
		if ( subject.equals(EVENT_DEVICE_UNMOUNT) || subject.equals(EVENT_DEVICE_UNMOUNT)){
			SwingUtilities.updateComponentTreeUI(jspTree);
			jtree.setRowHeight(25);
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
