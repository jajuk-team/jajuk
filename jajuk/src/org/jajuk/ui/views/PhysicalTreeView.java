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
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
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
import org.jajuk.base.StackItem;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.TransferableTreeNode;
import org.jajuk.ui.TreeTransferHandler;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import com.sun.SwingWorker;

/**
 * Physical tree view
 * 
 * @author bflorat 
 * @created 28 nov. 2003
 */
public class PhysicalTreeView extends AbstractTreeView implements ActionListener,org.jajuk.ui.Observer{
	
	/** Self instance */
	private static PhysicalTreeView ptv;
	
	/** Top tree node */
	DefaultMutableTreeNode top;
	
	/** Files selection*/
	ArrayList alFiles;
	
	/** Directories selection*/
	ArrayList alDirs;
	
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
	JPopupMenu jmenuPlaylistFile;
	JMenuItem jmiPlaylistFilePlay;
	JMenuItem jmiPlaylistFilePush;
	JMenuItem jmiPlaylistFilePlayShuffle;
	JMenuItem jmiPlaylistFilePlayRepeat;
	JMenuItem jmiPlaylistFileCopy;
	JMenuItem jmiPlaylistFileCut;
	JMenuItem jmiPlaylistFilePaste;
	JMenuItem jmiPlaylistFileRename;
	JMenuItem jmiPlaylistFileDelete;
	JMenuItem jmiPlaylistFileSetProperty;
	JMenuItem jmiPlaylistFileProperties;
	
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "PhysicalTreeView.0"; //$NON-NLS-1$
	}
	
	/** Return singleton */
	public static synchronized PhysicalTreeView getInstance() {
		if (ptv == null) {
			ptv = new PhysicalTreeView();
		}
		return ptv;
	}
	
	/** Constructor */
	public PhysicalTreeView(){
		ptv = this;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void populate(){
		//**Menu items**
		//File menu
		jmenuFile = new JPopupMenu();
		jmiFilePlay = new JMenuItem(Messages.getString("PhysicalTreeView.1")); //$NON-NLS-1$
		jmiFilePlay.addActionListener(this);
		jmiFilePush = new JMenuItem(Messages.getString("PhysicalTreeView.2")); //$NON-NLS-1$
		jmiFilePush.addActionListener(this);
		jmiFileCopy = new JMenuItem(Messages.getString("PhysicalTreeView.3")); //$NON-NLS-1$
		jmiFileCopy.setEnabled(false);
		jmiFileCopy.addActionListener(this);
		jmiFileCut = new JMenuItem(Messages.getString("PhysicalTreeView.4")); //$NON-NLS-1$
		jmiFileCut.setEnabled(false);
		jmiFileCut.addActionListener(this);
		jmiFilePaste = new JMenuItem(Messages.getString("PhysicalTreeView.5")); //$NON-NLS-1$
		jmiFilePaste.setEnabled(false);
		jmiFilePaste.addActionListener(this);
		jmiFileRename = new JMenuItem(Messages.getString("PhysicalTreeView.6")); //$NON-NLS-1$
		jmiFileRename.setEnabled(false);
		jmiFileRename.addActionListener(this);
		jmiFileDelete = new JMenuItem(Messages.getString("PhysicalTreeView.7")); //$NON-NLS-1$
		jmiFileDelete.setEnabled(false);
		jmiFileDelete.addActionListener(this);
		jmiFileSetProperty = new JMenuItem(Messages.getString("PhysicalTreeView.8")); //$NON-NLS-1$
		jmiFileSetProperty.setEnabled(false);
		jmiFileSetProperty.addActionListener(this);
		jmiFileProperties = new JMenuItem(Messages.getString("PhysicalTreeView.9")); //$NON-NLS-1$
		jmiFileProperties.setEnabled(false);
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
		jmiDirPlay = new JMenuItem(Messages.getString("PhysicalTreeView.10")); //$NON-NLS-1$
		jmiDirPlay.addActionListener(this);
		jmiDirPush = new JMenuItem(Messages.getString("PhysicalTreeView.11")); //$NON-NLS-1$
		jmiDirPush.addActionListener(this);
		jmiDirPlayShuffle = new JMenuItem(Messages.getString("PhysicalTreeView.12")); //$NON-NLS-1$
		jmiDirPlayShuffle.addActionListener(this);
		jmiDirPlayRepeat = new JMenuItem(Messages.getString("PhysicalTreeView.13")); //$NON-NLS-1$
		jmiDirPlayRepeat.addActionListener(this);
		jmiDirDesynchro = new JMenuItem(Messages.getString("PhysicalTreeView.14")); //$NON-NLS-1$
		jmiDirDesynchro.addActionListener(this);
		jmiDirResynchro = new JMenuItem(Messages.getString("PhysicalTreeView.15")); //$NON-NLS-1$
		jmiDirResynchro.addActionListener(this);
		jmiDirCreatePlaylist = new JMenuItem(Messages.getString("PhysicalTreeView.16")); //$NON-NLS-1$
		jmiDirCreatePlaylist.setEnabled(false);
		jmiDirCreatePlaylist.addActionListener(this);
		jmiDirCopy = new JMenuItem(Messages.getString("PhysicalTreeView.17")); //$NON-NLS-1$
		jmiDirCopy.setEnabled(false);
		jmiDirCopy.addActionListener(this);
		jmiDirCut = new JMenuItem(Messages.getString("PhysicalTreeView.18")); //$NON-NLS-1$
		jmiDirCut.setEnabled(false);
		jmiDirCut.addActionListener(this);
		jmiDirPaste = new JMenuItem(Messages.getString("PhysicalTreeView.19")); //$NON-NLS-1$
		jmiDirPaste.setEnabled(false);
		jmiDirPaste.addActionListener(this);
		jmiDirRename = new JMenuItem(Messages.getString("PhysicalTreeView.20")); //$NON-NLS-1$
		jmiDirRename.setEnabled(false);
		jmiDirRename.addActionListener(this);
		jmiDirDelete = new JMenuItem(Messages.getString("PhysicalTreeView.21")); //$NON-NLS-1$
		jmiDirDelete.setEnabled(false);
		jmiDirDelete.addActionListener(this);
		jmiDirSetProperty = new JMenuItem(Messages.getString("PhysicalTreeView.22")); //$NON-NLS-1$
		jmiDirSetProperty.setEnabled(false);
		jmiDirSetProperty.addActionListener(this);
		jmiDirProperties = new JMenuItem(Messages.getString("PhysicalTreeView.23")); //$NON-NLS-1$
		jmiDirProperties.setEnabled(false);
		jmiDirProperties.addActionListener(this);
		jmenuDir.add(jmiDirPlay);
		jmenuDir.add(jmiDirPush);
		jmenuDir.add(jmiDirPlayShuffle);
		jmenuDir.add(jmiDirPlayRepeat);
		jmenuDir.add(jmiDirDesynchro);
		jmenuDir.add(jmiDirResynchro);
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
		jmiDevPlay = new JMenuItem(Messages.getString("PhysicalTreeView.24")); //$NON-NLS-1$
		jmiDevPlay.addActionListener(this);
		jmiDevPush = new JMenuItem(Messages.getString("PhysicalTreeView.25")); //$NON-NLS-1$
		jmiDevPush.addActionListener(this);
		jmiDevPlayShuffle = new JMenuItem(Messages.getString("PhysicalTreeView.26")); //$NON-NLS-1$
		jmiDevPlayShuffle.addActionListener(this);
		jmiDevPlayRepeat = new JMenuItem(Messages.getString("PhysicalTreeView.27")); //$NON-NLS-1$
		jmiDevPlayRepeat.addActionListener(this);
		jmiDevMount = new JMenuItem(Messages.getString("PhysicalTreeView.28")); //$NON-NLS-1$
		jmiDevMount.addActionListener(this);
		jmiDevUnmount = new JMenuItem(Messages.getString("PhysicalTreeView.29")); //$NON-NLS-1$
		jmiDevUnmount.addActionListener(this);
		jmiDevRefresh = new JMenuItem(Messages.getString("PhysicalTreeView.30")); //$NON-NLS-1$
		jmiDevRefresh.addActionListener(this);
		jmiDevSynchronize = new JMenuItem(Messages.getString("PhysicalTreeView.31")); //$NON-NLS-1$
		jmiDevSynchronize.addActionListener(this);
		jmiDevTest = new JMenuItem(Messages.getString("PhysicalTreeView.32")); //$NON-NLS-1$
		jmiDevTest.addActionListener(this);
		jmiDevCreatePlaylist = new JMenuItem(Messages.getString("PhysicalTreeView.33")); //$NON-NLS-1$
		jmiDevCreatePlaylist.setEnabled(false);
		jmiDevCreatePlaylist.addActionListener(this);
		jmiDevSetProperty = new JMenuItem(Messages.getString("PhysicalTreeView.34")); //$NON-NLS-1$
		jmiDevSetProperty.setEnabled(false);
		jmiDevSetProperty.addActionListener(this);
		jmiDevProperties = new JMenuItem(Messages.getString("PhysicalTreeView.35")); //$NON-NLS-1$
		jmiDevProperties.setEnabled(false);
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
		
		//Playlist file menu
		//File menu
		jmenuPlaylistFile = new JPopupMenu();
		jmiPlaylistFilePlay = new JMenuItem(Messages.getString("PhysicalTreeView.36")); //$NON-NLS-1$
		jmiPlaylistFilePlay.addActionListener(this);
		jmiPlaylistFilePush = new JMenuItem(Messages.getString("PhysicalTreeView.37")); //$NON-NLS-1$
		jmiPlaylistFilePush.addActionListener(this);
		jmiPlaylistFilePlayShuffle = new JMenuItem(Messages.getString("PhysicalTreeView.38")); //$NON-NLS-1$
		jmiPlaylistFilePlayShuffle.addActionListener(this);
		jmiPlaylistFilePlayRepeat = new JMenuItem(Messages.getString("PhysicalTreeView.39")); //$NON-NLS-1$
		jmiPlaylistFilePlayRepeat.addActionListener(this);
		jmiPlaylistFileCopy = new JMenuItem(Messages.getString("PhysicalTreeView.40")); //$NON-NLS-1$
		jmiPlaylistFileCopy.setEnabled(false);
		jmiPlaylistFileCopy.addActionListener(this);
		jmiPlaylistFileCut = new JMenuItem(Messages.getString("PhysicalTreeView.41")); //$NON-NLS-1$
		jmiPlaylistFileCut.setEnabled(false);
		jmiPlaylistFileCut.addActionListener(this);
		jmiPlaylistFilePaste = new JMenuItem(Messages.getString("PhysicalTreeView.42")); //$NON-NLS-1$
		jmiPlaylistFilePaste.setEnabled(false);
		jmiPlaylistFilePaste.addActionListener(this);
		jmiPlaylistFileRename = new JMenuItem(Messages.getString("PhysicalTreeView.43")); //$NON-NLS-1$
		jmiPlaylistFileRename.setEnabled(false);
		jmiPlaylistFileRename.addActionListener(this);
		jmiPlaylistFileDelete = new JMenuItem(Messages.getString("PhysicalTreeView.44")); //$NON-NLS-1$
		jmiPlaylistFileDelete.addActionListener(this);
		jmiPlaylistFileSetProperty = new JMenuItem(Messages.getString("PhysicalTreeView.45")); //$NON-NLS-1$
		jmiPlaylistFileSetProperty.setEnabled(false);
		jmiPlaylistFileSetProperty.addActionListener(this);
		jmiPlaylistFileProperties = new JMenuItem(Messages.getString("PhysicalTreeView.46")); //$NON-NLS-1$
		jmiPlaylistFileProperties.setEnabled(false);
		jmiPlaylistFileProperties.addActionListener(this);
		jmenuPlaylistFile.add(jmiPlaylistFilePlay);
		jmenuPlaylistFile.add(jmiPlaylistFilePush);
		jmenuPlaylistFile.add(jmiPlaylistFilePlayShuffle);
		jmenuPlaylistFile.add(jmiPlaylistFilePlayRepeat);
		jmenuPlaylistFile.add(jmiPlaylistFileCopy);
		jmenuPlaylistFile.add(jmiPlaylistFileCut);
		jmenuPlaylistFile.add(jmiPlaylistFilePaste);
		jmenuPlaylistFile.add(jmiPlaylistFileRename);
		jmenuPlaylistFile.add(jmiPlaylistFileDelete);
		jmenuPlaylistFile.add(jmiPlaylistFileSetProperty);
		jmenuPlaylistFile.add(jmiPlaylistFileProperties);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		top = new DefaultMutableTreeNode(Messages.getString("PhysicalTreeView.47")); //$NON-NLS-1$
		//Register on the list for subject we are interrested in
		ObservationManager.register(EVENT_DEVICE_MOUNT,this);
		ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
		
		//fill the tree
		populateTree();
		
		//create tree
		jtree = new JTree(top);
		jtree.putClientProperty("JTree.lineStyle", "Angled"); //$NON-NLS-1$ //$NON-NLS-2$
		jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		jtree.setCellRenderer(new DefaultTreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				setFont(new Font("Dialog",Font.PLAIN,10)); //$NON-NLS-1$
				if (value instanceof FileNode ){
					setIcon(Util.getIcon(ICON_FILE));
				}
				else if (value instanceof PlaylistFileNode){
					setIcon(Util.getIcon(ICON_PLAYLIST_FILE));
				}
				else if (value instanceof DeviceNode){
					Device device = (Device)((DeviceNode)value).getDevice();
					switch ( device.getDeviceType()){
					case 0 : 
						if ( device.isMounted())	setIcon(Util.getIcon(ICON_DEVICE_DIRECTORY_MOUNTED_SMALL));
						else setIcon(Util.getIcon(ICON_DEVICE_DIRECTORY_UNMOUNTED_SMALL));
						break;
					case 1 : 
						if ( device.isMounted())	setIcon(Util.getIcon(ICON_DEVICE_CD_MOUNTED_SMALL));
						else setIcon(Util.getIcon(ICON_DEVICE_CD_UNMOUNTED_SMALL));
						break;
					case 2 : 
						if ( device.isMounted())	setIcon(Util.getIcon(ICON_DEVICE_REMOTE_MOUNTED_SMALL));
						else setIcon(Util.getIcon(ICON_DEVICE_REMOTE_UNMOUNTED_SMALL));
						break;
					case 3 : 
						if ( device.isMounted())	setIcon(Util.getIcon(ICON_DEVICE_EXT_DD_MOUNTED_SMALL));
						else setIcon(Util.getIcon(ICON_DEVICE_EXT_DD_UNMOUNTED_SMALL));
						break;
					case 4 : 
						if ( device.isMounted())	setIcon(Util.getIcon(ICON_DEVICE_PLAYER_MOUNTED_SMALL));
						else setIcon(Util.getIcon(ICON_DEVICE_PLAYER_UNMOUNTED_SMALL));
						break;
					}
				}
				else if (value instanceof DirectoryNode){
					Directory dir = ((DirectoryNode)value).getDirectory();
					String synchro = dir.getProperty(DIRECTORY_OPTION_SYNCHRO_MODE);
					if ( synchro == null || "y".equals(synchro)){  //means this device is not synchronized //$NON-NLS-1$
						setIcon(Util.getIcon(ICON_DIRECTORY_SYNCHRO));
					}
					else{
						setIcon(Util.getIcon(ICON_DIRECTORY_DESYNCHRO));
					}
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
				TreePath[] tpSelected = jtree.getSelectionModel().getSelectionPaths();
				if ( tpSelected == null){ //nothing selected, can be called during dnd
					return;
				}
				HashSet hsSelectedFiles = new HashSet(100);
				int items = 0;
				long lSize = 0;
				//get all components recursively
				for (int i=0;i<tpSelected.length;i++){
					Object o = tpSelected[i].getLastPathComponent();
					Enumeration e2 = ((DefaultMutableTreeNode)o).depthFirstEnumeration(); //return all childs nodes recursively
					while ( e2.hasMoreElements()){
						DefaultMutableTreeNode node = (DefaultMutableTreeNode)e2.nextElement();
						if (node instanceof FileNode){
							File file = ((FileNode)node).getFile();
							if (hsSelectedFiles.contains(file)){ //don't count the same file several time if user select directory and then files inside
								continue;
							}
							lSize += file.getSize();
							items ++;
							hsSelectedFiles.add(file);
						}
					}
				}
				lSize /= 1048576; //set size in MB
				StringBuffer sbOut = new StringBuffer().append(items).append(Messages.getString("PhysicalTreeView.52")); //$NON-NLS-1$
				if ( lSize>1024){ //more than 1024 MB -> in GB
					sbOut.append(lSize/1024).append('.').append(lSize%1024).append(Messages.getString("PhysicalTreeView.53")); //$NON-NLS-1$
				}
				else{
					sbOut.append(lSize).append(Messages.getString("PhysicalTreeView.54")); //$NON-NLS-1$
				}
				InformationJPanel.getInstance().setSelection(sbOut.toString());
			}
		});
		//Listen for double clic
		MouseListener ml = new MouseAdapter() {
			public void mousePressed(final MouseEvent e) {
				TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
				if (path == null){
				    return;
				}
				if ( e.getClickCount() == 2){
					Object o = path.getLastPathComponent();
					if (o instanceof FileNode){
						File file = ((FileNode)o).getFile();
						if (!file.isScanned()){
							FIFO.getInstance().push(new StackItem(file,ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
						}
						else{
							Messages.showErrorMessage("120",file.getDirectory().getDevice().getName()); //$NON-NLS-1$
						}
					}
					else if (o instanceof PlaylistFileNode){  //double clic on a playlist file
						PlaylistFile plf = ((PlaylistFileNode)o).getPlaylistFile();
						ArrayList alFiles = new ArrayList(10); 
						try{	
							alFiles = plf.getBasicFiles();
						}
						catch(JajukException je){
							Log.error("009",plf.getName(),new Exception()); //$NON-NLS-1$
							Messages.showErrorMessage("009",plf.getName()); //$NON-NLS-1$
						}
						if ( alFiles.size() == 0){ //check playlist file contains accessible tracks
							Messages.showErrorMessage("018");	 //$NON-NLS-1$
						}
						else{
							FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFiles),
									ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
						}
					}
				}
				else if ( jtree.getSelectionCount() > 0 && e.getClickCount() == 1 && e.getButton()==MouseEvent.BUTTON3){  //right clic on a selected node set
					//Only keep files
					paths = jtree.getSelectionModel().getSelectionPaths();
					getInstance().alFiles = new ArrayList(100);
					getInstance().alDirs = new ArrayList(10);
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
								if (!file.isScanned()){
									getInstance().alFiles.add(((FileNode)node).getFile());
								}
								else{
									if (!(o instanceof DeviceNode)){
										Messages.showErrorMessage("120"); //$NON-NLS-1$
										return; //show only one error message
									}
								}
							}
							else if (node instanceof DirectoryNode){
								Directory dir = ((DirectoryNode)node).getDirectory();
								getInstance().alDirs.add(dir);
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
					else if (paths[0].getLastPathComponent() instanceof PlaylistFileNode){
						jmenuPlaylistFile.show(jtree,e.getX(),e.getY());
					}
					else if (paths[0].getLastPathComponent() instanceof DeviceNode){
						if ( paths.length>1){ //operations on devices are mono-target
							return;
						}
						Device device =  ((DeviceNode)paths[0].getLastPathComponent()).getDevice();	
						if ( device.getProperty(DEVICE_OPTION_SYNCHRO_SOURCE) == null){ //if the device is not synchronized
							jmiDevSynchronize.setEnabled(false);
						}
						else{
							jmiDevSynchronize.setEnabled(true);
						}
						jmenuDev.show(jtree,e.getX(),e.getY()); 
					}
				}
			}
		};
		jtree.addMouseListener(ml);
		//Expansion analyse to keep expended state 
		jtree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				Object o = event.getPath().getLastPathComponent(); 
				if (o instanceof DirectoryNode){
					Directory dir = ((DirectoryNode)o).getDirectory(); 
					dir.removeProperty(OPTION_EXPANDED);
				}
				else if (o instanceof DeviceNode){
					Device device = ((DeviceNode)o).getDevice();
					device.removeProperty(OPTION_EXPANDED);
				}
			}
			
			public void treeExpanded(TreeExpansionEvent event) {
				Object o = event.getPath().getLastPathComponent(); 
				if (o instanceof DirectoryNode){
					Directory dir = ((DirectoryNode)o).getDirectory(); 
					dir.removeProperty(OPTION_EXPANDED);
					dir.setProperty(OPTION_EXPANDED,"y"); //$NON-NLS-1$
				}
				else if (o instanceof DeviceNode){
					Device device = ((DeviceNode)o).getDevice();
					device.setProperty(OPTION_EXPANDED,"y"); //$NON-NLS-1$
				}
				
			}
		});
		jtree.setAutoscrolls(true);
		//DND support
		new TreeTransferHandler(jtree, DnDConstants.ACTION_COPY_OR_MOVE,true);
		//tree itself
		jspTree = new JScrollPane(jtree);
		add(jspTree);
		//expand all
		expand();
		
	}
	
	/**Fill the tree */
	public void populateTree(){
		this.transferFocus();
		top.removeAllChildren();
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
			if (directory.shouldBeHidden()){
				continue;
			}
			if (!directory.getName().equals("")){ //device root directory, do not display //$NON-NLS-1$
				if (directory.getParentDirectory().getName().equals("")){  //parent directory is a device //$NON-NLS-1$
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
			else{  //add file at the device root
				DeviceNode deviceNode = DeviceNode.getDeviceNode(directory.getDevice());
				Iterator it = directory.getFiles().iterator();
				while (it.hasNext()){
					deviceNode.add(new FileNode((File)it.next()));
				}
			}
		}
		//add files
		ArrayList alFiles = FileManager.getFiles();
		Collections.sort(alFiles);
		Iterator it3 = alFiles.iterator();
		while (it3.hasNext()){
			File file = (File)it3.next();
			if ( file.shouldBeHidden()){ //should be hiden by option
				continue;
			}
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
			if ( playlistFile.shouldBeHidden()){ //should be hiden by option
				continue;
			}
			DirectoryNode directoryNode = DirectoryNode.getDirectoryNode(playlistFile.getDirectory());
			if (directoryNode != null){
				directoryNode.add(new PlaylistFileNode(playlistFile));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		new Thread(){
			public void run(){
				if (e.getSource() == jmiFilePlay && alFiles.size() > 0 ){
					FIFO.getInstance().push(Util.createStackItems(alFiles,
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
				}
				else if (e.getSource() == jmiFilePush  && alFiles.size() > 0){
					FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFiles),
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),true);
				}
				else if ( alFiles!= null && alFiles.size() > 0 && (e.getSource() == jmiDirPlay || e.getSource() == jmiDevPlay)){  
					FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFiles),
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
				}
				else if (alFiles!= null && alFiles.size() > 0 && (e.getSource() == jmiDirPush || e.getSource() == jmiDevPush)){
					FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFiles),
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),true);
				}
				else if (alFiles!= null && alFiles.size() > 0 && (e.getSource() == jmiDirPlayShuffle || e.getSource() == jmiDevPlayShuffle)){
				    Collections.shuffle(alFiles);
					FIFO.getInstance().push(Util.createStackItems(alFiles,
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
				}
				else if (alFiles!= null && alFiles.size() > 0 && (e.getSource() == jmiDirPlayRepeat || e.getSource() == jmiDevPlayRepeat)){
					FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFiles),true,true),false);
				}
				else if ( e.getSource() == jmiDevMount){
					for (int i=0;i<paths.length;i++){
						Device device = ((DeviceNode)(paths[i].getLastPathComponent())).getDevice();
						try{
							device.mount();
						}
						catch(Exception ex){
							Messages.showErrorMessage("011"); //$NON-NLS-1$
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
							Messages.showErrorMessage("012"); //$NON-NLS-1$
						}
					}
				}
				else if ( e.getSource() == jmiDevRefresh){
					Device device = ((DeviceNode)(paths[0].getLastPathComponent())).getDevice();
					device.refresh(true);
				}
				else if ( e.getSource() == jmiDevSynchronize){
					Device device = ((DeviceNode)(paths[0].getLastPathComponent())).getDevice();
					device.synchronize(true);
				}
				else if (e.getSource() == jmiDevTest){
					Device device = ((DeviceNode)(paths[0].getLastPathComponent())).getDevice();
					if (device.test()){
						Messages.showInfoMessage(Messages.getString("DeviceView.21"),Util.getIcon(ICON_OK)); //$NON-NLS-1$
					}
					else{
						Messages.showInfoMessage(Messages.getString("DeviceView.22"),Util.getIcon(ICON_KO)); //$NON-NLS-1$
					}
				}
				else if ( e.getSource() == jmiDirDesynchro){
					Iterator it = alDirs.iterator();  //iterate on selected dirs and childs recursively
					while ( it.hasNext()){
						Directory dir = (Directory)it.next();
						dir.removeProperty(DIRECTORY_OPTION_SYNCHRO_MODE);
						dir.setProperty(DIRECTORY_OPTION_SYNCHRO_MODE,"n"); //$NON-NLS-1$
					}
					jtree.revalidate();
					jtree.repaint();
				}
				else if ( e.getSource() == jmiDirResynchro){
					Iterator it = alDirs.iterator();  //iterate on selected dirs and childs recursively
					while ( it.hasNext()){
						Directory dir = (Directory)it.next();
						dir.removeProperty(DIRECTORY_OPTION_SYNCHRO_MODE);
					}
					jtree.revalidate();
					jtree.repaint();
				}
				else if ( e.getSource() == jmiPlaylistFilePlay || e.getSource()==jmiPlaylistFilePush || e.getSource() == jmiPlaylistFilePlayShuffle || e.getSource() == jmiPlaylistFilePlayRepeat){
					PlaylistFile plf = ((PlaylistFileNode)paths[0].getLastPathComponent()).getPlaylistFile();
					ArrayList alFiles = new ArrayList(10); 
					try{	
						alFiles = plf.getBasicFiles();
					}
					catch(JajukException je){
						Log.error("009",plf.getName(),new Exception()); //$NON-NLS-1$
						Messages.showErrorMessage("009",plf.getName()); //$NON-NLS-1$
					}
					if ( alFiles.size() == 0){ //check playlist file contains accessible tracks
						Messages.showErrorMessage("018"); //$NON-NLS-1$
					}
					else{ //specific actions
						if ( e.getSource() == jmiPlaylistFilePlay ){
							FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFiles),
									ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
						}
						else if ( e.getSource()==jmiPlaylistFilePush ){
							FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFiles),
									ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),true);
						}
						else if ( e.getSource() == jmiPlaylistFilePlayShuffle ){
							Collections.shuffle(alFiles);
							FIFO.getInstance().push(Util.createStackItems(alFiles,
									ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
						}
						else if ( e.getSource() == jmiPlaylistFilePlayRepeat){
							FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFiles),
									true,true),false);
						}
					}
				}
				else if ( e.getSource() == jmiPlaylistFileDelete){
					PlaylistFile plf = ((PlaylistFileNode)paths[0].getLastPathComponent()).getPlaylistFile();
					plf.delete();
				}
			}
		}.start();
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject) {
		if ( subject.equals(EVENT_DEVICE_MOUNT) || subject.equals(EVENT_DEVICE_UNMOUNT) || subject.equals(EVENT_DEVICE_REFRESH) ) {
			SwingWorker sw = new SwingWorker() {
				public Object  construct(){
					populateTree();
					return null;
				}
				public void finished() {
				    SwingUtilities.updateComponentTreeUI(jtree);
					expand();
					int i = jspTree.getVerticalScrollBar().getValue();
					jspTree.getVerticalScrollBar().setValue(i);
				}
			};
			sw.start();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getID()
	 */
	public String getID() {
	    return "org.jajuk.ui.views.PhysicalTreeView"; //$NON-NLS-1$
	}
	
	/**
	 * Manages auto-expand 
	 *
	 */
	private void expand(){
		for (int i=0;i<jtree.getRowCount();i++){
			Object o = jtree.getPathForRow(i).getLastPathComponent(); 
			if ( o instanceof DeviceNode && ((DeviceNode)o).getDevice().isMounted()  && !((DeviceNode)o).getDevice().isRefreshing() 
					&& !((DeviceNode)o).getDevice().isSynchronizing() ){
				Device device = ((DeviceNode)o).getDevice();
				String sExp = device.getProperty(OPTION_EXPANDED); 
				if ( "y".equals(sExp)){ //$NON-NLS-1$
					jtree.expandRow(i);	
				}
			}
			else if ( o instanceof DirectoryNode){
				Directory dir = ((DirectoryNode)o).getDirectory();
				String sExp = dir.getProperty(OPTION_EXPANDED); 
				if ( "y".equals(sExp)){ //$NON-NLS-1$
					jtree.expandRow(i);	
				}
			}
		}
	}
	
	
}


/**
 * File node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class FileNode extends TransferableTreeNode{
	
	/**
	 * Constructor
	 * @param file
	 */
	public FileNode(File file){
		super(file);
	}
	
	/**
	 * return a string representation of this file node
	 */
	public String toString(){
		return ((File)super.getData()).getName();
	}
	
	/**
	 * @return Returns the file.
	 */
	public File getFile() {
		return (File)super.getData();
	}
	
}

/**
 * Device node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class DeviceNode extends TransferableTreeNode{
	
	/**device -> deviceNode hashmap */
	public static HashMap hmDeviceDeviceNode = new HashMap(100);
	
	/**
	 * Constructor
	 * @param device
	 */
	public DeviceNode(Device device){
		super(device);
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
		return ((Device)super.getData()).getName();
	}
	/**
	 * @return Returns the device.
	 */
	public Device getDevice() {
		return (Device)super.getData();
	}
	
}


/**
 * Directory node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class DirectoryNode  extends TransferableTreeNode{
	
	/**directory -> directoryNode hashmap */
	public static HashMap hmDirectoryDirectoryNode = new HashMap(100);
	
	/**
	 * Constructor
	 * @param Directory
	 */
	public DirectoryNode(Directory directory){
		super(directory);
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
		return ((Directory)getData()).getName();
	}
	/**
	 * @return Returns the directory.
	 */
	public Directory getDirectory() {
		return (Directory)getData();
	}
	
}

/**
 * PlaylistFile node 
 * @author     bflorat
 * @created    29 nov. 2003
 */
class PlaylistFileNode  extends TransferableTreeNode{
	
	/**
	 * Constructor
	 * @param PlaylistFile
	 */
	public PlaylistFileNode(PlaylistFile playlistFile){
		super(playlistFile);
	}
	
	/**
	 * return a string representation of this playlistFile node
	 */
	public String toString(){
		return ((PlaylistFile)super.getData()).getName();
	}
	
	/**
	 * @return Returns the playlist file node.
	 */
	public PlaylistFile getPlaylistFile() {
		return (PlaylistFile)getData();
	}
	
}

