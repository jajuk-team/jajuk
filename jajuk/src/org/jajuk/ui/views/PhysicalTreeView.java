/*
 * Jajuk Copyright (C) 2003 Bertrand Florat
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

import java.awt.Color;
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
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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

import org.jajuk.base.Bookmarks;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.base.StackItem;
import org.jajuk.base.Track;
import org.jajuk.base.exporters.ExportFileFilter;
import org.jajuk.base.exporters.HTMLExporter;
import org.jajuk.base.exporters.XMLExporter;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CDDBWizard;
import org.jajuk.ui.DeviceWizard;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.PropertiesWizard;
import org.jajuk.ui.TransferableTreeNode;
import org.jajuk.ui.TreeTransferHandler;
import org.jajuk.ui.action.RefactorAction;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import ext.SwingWorker;

/**
 * Physical tree view
 * 
 * @author Bertrand Florat
 * @created 28 nov. 2003
 */
public class PhysicalTreeView extends AbstractTreeView implements
	ActionListener, org.jajuk.base.Observer {

    private static final long serialVersionUID = 1L;

    /** Self instance */
    private static PhysicalTreeView ptv;

    /** Files selection */
    ArrayList<File> alFiles;

    /** Directories selection */
    ArrayList<Directory> alDirs;

    /** Collection export */
    JPopupMenu jmenuCollection;

    JMenuItem jmiCollectionExport;

    JPopupMenu jmenuFile;

    JMenuItem jmiFilePlay;

    JMenuItem jmiFilePush;

    JMenuItem jmiFileCopy;

    JMenuItem jmiFileCut;

    JMenuItem jmiFilePaste;

    JMenuItem jmiFileDelete;

    JMenuItem jmiFileProperties;

    JMenuItem jmiFileAddFavorites;

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

    JMenuItem jmiDirDelete;

    JMenuItem jmiDirProperties;

    JMenuItem jmiDirAddFavorites;

    JMenuItem jmiDirCDDBQuery;

    JMenuItem jmiDirExport;

    JMenuItem jmiDirRefactor;

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

    JMenuItem jmiDevProperties;

    JMenuItem jmiDevCDDBQuery;

    JMenuItem jmiDevConfiguration;

    JMenuItem jmiDevExport;

    JPopupMenu jmenuPlaylistFile;

    JMenuItem jmiPlaylistFilePlay;

    JMenuItem jmiPlaylistFilePush;

    JMenuItem jmiPlaylistFilePlayShuffle;

    JMenuItem jmiPlaylistFilePlayRepeat;

    JMenuItem jmiPlaylistFileCopy;

    JMenuItem jmiPlaylistFileCut;

    JMenuItem jmiPlaylistFilePaste;

    JMenuItem jmiPlaylistFileDelete;

    JMenuItem jmiPlaylistAddFavorites;

    JMenuItem jmiPlaylistFileProperties;

    /**
         * Used to differentiate user action tree collapse from code tree
         * colapse*
         */
    private boolean bAutoCollapse = false;

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
    public PhysicalTreeView() {
	ptv = this;
    }

    public Set<EventSubject> getRegistrationKeys() {
	HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
	eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
	eventSubjectSet.add(EventSubject.EVENT_DEVICE_MOUNT);
	eventSubjectSet.add(EventSubject.EVENT_DEVICE_UNMOUNT);
	eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
	eventSubjectSet.add(EventSubject.EVENT_CDDB_WIZARD);
	return eventSubjectSet;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.jajuk.ui.IView#display()
         */
    public void initUI() {
	// **Menu items**

	// Collection menu
	jmenuCollection = new JPopupMenu();
	// Export
	jmiCollectionExport = new JMenuItem(Messages
		.getString("LogicalTreeView.33")); //$NON-NLS-1$
	jmiCollectionExport.addActionListener(this);
	jmenuCollection.add(jmiCollectionExport);

	// File menu
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
	jmiFileDelete = new JMenuItem(Messages.getString("PhysicalTreeView.7")); //$NON-NLS-1$
	jmiFileDelete.setEnabled(false);
	jmiFileDelete.addActionListener(this);
	jmiFileProperties = new JMenuItem(Messages
		.getString("PhysicalTreeView.9")); //$NON-NLS-1$
	jmiFileProperties.addActionListener(this);
	jmiFileAddFavorites = new JMenuItem(Messages
		.getString("PhysicalTreeView.56")); //$NON-NLS-1$
	jmiFileAddFavorites.addActionListener(this);
	jmenuFile.add(jmiFilePlay);
	jmenuFile.add(jmiFilePush);
	jmenuFile.add(jmiFileCopy);
	jmenuFile.add(jmiFileCut);
	jmenuFile.add(jmiFilePaste);
	jmenuFile.add(jmiFileDelete);
	jmenuFile.add(jmiFileAddFavorites);
	jmenuFile.add(jmiFileProperties);

	// Directory menu
	jmenuDir = new JPopupMenu();
	jmiDirPlay = new JMenuItem(Messages.getString("PhysicalTreeView.10")); //$NON-NLS-1$
	jmiDirPlay.addActionListener(this);
	jmiDirPush = new JMenuItem(Messages.getString("PhysicalTreeView.11")); //$NON-NLS-1$
	jmiDirPush.addActionListener(this);
	jmiDirPlayShuffle = new JMenuItem(Messages
		.getString("PhysicalTreeView.12")); //$NON-NLS-1$
	jmiDirPlayShuffle.addActionListener(this);
	jmiDirPlayRepeat = new JMenuItem(Messages
		.getString("PhysicalTreeView.13")); //$NON-NLS-1$
	jmiDirPlayRepeat.addActionListener(this);
	jmiDirDesynchro = new JMenuItem(Messages
		.getString("PhysicalTreeView.14")); //$NON-NLS-1$
	jmiDirDesynchro.addActionListener(this);
	jmiDirResynchro = new JMenuItem(Messages
		.getString("PhysicalTreeView.15")); //$NON-NLS-1$
	jmiDirResynchro.addActionListener(this);
	jmiDirCreatePlaylist = new JMenuItem(Messages
		.getString("PhysicalTreeView.16")); //$NON-NLS-1$
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
	jmiDirDelete = new JMenuItem(Messages.getString("PhysicalTreeView.21")); //$NON-NLS-1$
	jmiDirDelete.setEnabled(false);
	jmiDirDelete.addActionListener(this);
	jmiDirProperties = new JMenuItem(Messages
		.getString("PhysicalTreeView.23")); //$NON-NLS-1$
	jmiDirProperties.addActionListener(this);
	jmiDirAddFavorites = new JMenuItem(Messages
		.getString("PhysicalTreeView.56")); //$NON-NLS-1$
	jmiDirAddFavorites.addActionListener(this);
	jmiDirCDDBQuery = new JMenuItem(Messages
		.getString("PhysicalTreeView.57")); //$NON-NLS-1$
	jmiDirCDDBQuery.addActionListener(this);
	jmiDirExport = new JMenuItem(Messages.getString("PhysicalTreeView.58")); //$NON-NLS-1$
	jmiDirExport.addActionListener(this);
	jmiDirRefactor = new JMenuItem(Messages
		.getString(("PhysicalTreeView.62"))); //$NON-NLS-1$
	jmiDirRefactor.addActionListener(this);
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
	jmenuDir.add(jmiDirDelete);
	jmenuDir.add(jmiDirAddFavorites);
	jmenuDir.add(jmiDirCDDBQuery);
	jmenuDir.add(jmiDirExport);
	jmenuDir.add(jmiDirRefactor);
	jmenuDir.add(jmiDirProperties);

	// Device menu
	jmenuDev = new JPopupMenu();
	jmiDevPlay = new JMenuItem(Messages.getString("PhysicalTreeView.24")); //$NON-NLS-1$
	jmiDevPlay.addActionListener(this);
	jmiDevPush = new JMenuItem(Messages.getString("PhysicalTreeView.25")); //$NON-NLS-1$
	jmiDevPush.addActionListener(this);
	jmiDevPlayShuffle = new JMenuItem(Messages
		.getString("PhysicalTreeView.26")); //$NON-NLS-1$
	jmiDevPlayShuffle.addActionListener(this);
	jmiDevPlayRepeat = new JMenuItem(Messages
		.getString("PhysicalTreeView.27")); //$NON-NLS-1$
	jmiDevPlayRepeat.addActionListener(this);
	jmiDevMount = new JMenuItem(Messages.getString("PhysicalTreeView.28")); //$NON-NLS-1$
	jmiDevMount.addActionListener(this);
	jmiDevUnmount = new JMenuItem(Messages.getString("PhysicalTreeView.29")); //$NON-NLS-1$
	jmiDevUnmount.addActionListener(this);
	jmiDevRefresh = new JMenuItem(Messages.getString("PhysicalTreeView.30")); //$NON-NLS-1$
	jmiDevRefresh.addActionListener(this);
	jmiDevSynchronize = new JMenuItem(Messages
		.getString("PhysicalTreeView.31")); //$NON-NLS-1$
	jmiDevSynchronize.addActionListener(this);
	jmiDevTest = new JMenuItem(Messages.getString("PhysicalTreeView.32")); //$NON-NLS-1$
	jmiDevTest.addActionListener(this);
	jmiDevCreatePlaylist = new JMenuItem(Messages
		.getString("PhysicalTreeView.33")); //$NON-NLS-1$
	jmiDevCreatePlaylist.setEnabled(false);
	jmiDevCreatePlaylist.addActionListener(this);
	jmiDevProperties = new JMenuItem(Messages
		.getString("PhysicalTreeView.35")); //$NON-NLS-1$
	jmiDevProperties.addActionListener(this);
	jmiDevCDDBQuery = new JMenuItem(Messages
		.getString("PhysicalTreeView.57")); //$NON-NLS-1$
	jmiDevCDDBQuery.addActionListener(this);
	jmiDevConfiguration = new JMenuItem(Messages
		.getString("PhysicalTreeView.55")); //$NON-NLS-1$
	jmiDevConfiguration.addActionListener(this);
	jmiDevExport = new JMenuItem(Messages.getString("PhysicalTreeView.58")); //$NON-NLS-1$
	jmiDevExport.addActionListener(this);
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
	jmenuDev.add(jmiDevConfiguration);
	jmenuDev.add(jmiDevCDDBQuery);
	jmenuDev.add(jmiDevExport);
	jmenuDev.add(jmiDevProperties);

	// Playlist file menu
	// File menu
	jmenuPlaylistFile = new JPopupMenu();
	jmiPlaylistFilePlay = new JMenuItem(Messages
		.getString("PhysicalTreeView.36")); //$NON-NLS-1$
	jmiPlaylistFilePlay.addActionListener(this);
	jmiPlaylistFilePush = new JMenuItem(Messages
		.getString("PhysicalTreeView.37")); //$NON-NLS-1$
	jmiPlaylistFilePush.addActionListener(this);
	jmiPlaylistFilePlayShuffle = new JMenuItem(Messages
		.getString("PhysicalTreeView.38")); //$NON-NLS-1$
	jmiPlaylistFilePlayShuffle.addActionListener(this);
	jmiPlaylistFilePlayRepeat = new JMenuItem(Messages
		.getString("PhysicalTreeView.39")); //$NON-NLS-1$
	jmiPlaylistFilePlayRepeat.addActionListener(this);
	jmiPlaylistFileCopy = new JMenuItem(Messages
		.getString("PhysicalTreeView.40")); //$NON-NLS-1$
	jmiPlaylistFileCopy.setEnabled(false);
	jmiPlaylistFileCopy.addActionListener(this);
	jmiPlaylistFileCut = new JMenuItem(Messages
		.getString("PhysicalTreeView.41")); //$NON-NLS-1$
	jmiPlaylistFileCut.setEnabled(false);
	jmiPlaylistFileCut.addActionListener(this);
	jmiPlaylistFilePaste = new JMenuItem(Messages
		.getString("PhysicalTreeView.42")); //$NON-NLS-1$
	jmiPlaylistFilePaste.setEnabled(false);
	jmiPlaylistFilePaste.addActionListener(this);
	jmiPlaylistFileDelete = new JMenuItem(Messages
		.getString("PhysicalTreeView.44")); //$NON-NLS-1$
	jmiPlaylistFileDelete.addActionListener(this);
	jmiPlaylistAddFavorites = new JMenuItem(Messages
		.getString("PhysicalTreeView.56")); //$NON-NLS-1$
	jmiPlaylistAddFavorites.addActionListener(this);
	jmiPlaylistFileProperties = new JMenuItem(Messages
		.getString("PhysicalTreeView.46")); //$NON-NLS-1$
	jmiPlaylistFileProperties.addActionListener(this);
	jmenuPlaylistFile.add(jmiPlaylistFilePlay);
	jmenuPlaylistFile.add(jmiPlaylistFilePush);
	jmenuPlaylistFile.add(jmiPlaylistFilePlayShuffle);
	jmenuPlaylistFile.add(jmiPlaylistFilePlayRepeat);
	jmenuPlaylistFile.add(jmiPlaylistFileCopy);
	jmenuPlaylistFile.add(jmiPlaylistFileCut);
	jmenuPlaylistFile.add(jmiPlaylistFilePaste);
	jmenuPlaylistFile.add(jmiPlaylistFileDelete);
	jmenuPlaylistFile.add(jmiPlaylistAddFavorites);
	jmenuPlaylistFile.add(jmiPlaylistFileProperties);

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	top = new DefaultMutableTreeNode(Messages
		.getString("PhysicalTreeView.47")); //$NON-NLS-1$
	// Register on the list for subject we are interrested in
	ObservationManager.register(this);

	// fill the tree
	populateTree();

	// create tree
	createTree();

	jtree.setCellRenderer(new DefaultTreeCellRenderer() {
	    private static final long serialVersionUID = 1L;

	    public Component getTreeCellRendererComponent(JTree tree,
		    Object value, boolean sel, boolean expanded, boolean leaf,
		    int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded,
			leaf, row, hasFocus);
		setFont(new Font("Dialog", Font.PLAIN, 10)); //$NON-NLS-1$
		if (value instanceof FileNode) {
		    setBorder(null);
		    File file = ((FileNode) value).getFile();
		    if (Util.getExtension(file.getIO()).equals(
			    TYPE_PROPERTY_TECH_DESC_MP3)) {
			setIcon(Util.getIcon(ICON_TYPE_MP3));
		    } else if (Util.getExtension(file.getIO()).equals(
			    TYPE_PROPERTY_TECH_DESC_OGG)) {
			setIcon(Util.getIcon(ICON_TYPE_OGG));
		    } else if (Util.getExtension(file.getIO()).equals(
			    TYPE_PROPERTY_TECH_DESC_FLAC)) {
			setIcon(Util.getIcon(ICON_TYPE_FLAC));
		    } else if (Util.getExtension(file.getIO()).equals(
			    TYPE_PROPERTY_TECH_DESC_WMA)) {
			setIcon(Util.getIcon(ICON_TYPE_WMA));
		    } else if (Util.getExtension(file.getIO()).equals(
			    TYPE_PROPERTY_TECH_DESC_RAM)) {
			setIcon(Util.getIcon(ICON_TYPE_RAM));
		    } else if (Util.getExtension(file.getIO()).equals(
			    TYPE_PROPERTY_TECH_DESC_AAC)) {
			setIcon(Util.getIcon(ICON_TYPE_AAC));
		    } else {
			setIcon(Util.getIcon(ICON_TYPE_WAV));
		    }
		    File current = FIFO.getInstance().getCurrentFile();
		    if (current != null && file.equals(current)) {
			setFont(new Font("Dialog", Font.BOLD, 10)); //$NON-NLS-1$
			setForeground(Color.DARK_GRAY);
		    }
		} else if (value instanceof PlaylistFileNode) {
		    setBorder(null);
		    setIcon(Util.getIcon(ICON_PLAYLIST_FILE));
		} else if (value instanceof DeviceNode) {
		    setBorder(BorderFactory.createEmptyBorder(2, 0, 3, 0));
		    Device device = ((DeviceNode) value).getDevice();
		    switch ((int) device.getDeviceType()) {
		    case 0:
			if (device.isMounted()) {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_DIRECTORY_MOUNTED_SMALL));
			} else {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_DIRECTORY_UNMOUNTED_SMALL));
			}
			break;
		    case 1:
			if (device.isMounted()) {
			    setIcon(Util.getIcon(ICON_DEVICE_CD_MOUNTED_SMALL));
			} else {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_CD_UNMOUNTED_SMALL));
			}
			break;
		    case 2:
			if (device.isMounted()) {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_NETWORK_DRIVE_MOUNTED_SMALL));
			} else {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED_SMALL));
			}
			break;
		    case 3:
			if (device.isMounted()) {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_EXT_DD_MOUNTED_SMALL));
			} else {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_EXT_DD_UNMOUNTED_SMALL));
			}
			break;
		    case 4:
			if (device.isMounted()) {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_PLAYER_MOUNTED_SMALL));
			} else {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_PLAYER_UNMOUNTED_SMALL));
			}
			break;
		    case 5:
			if (device.isMounted()) {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_REMOTE_MOUNTED_SMALL));
			} else {
			    setIcon(Util
				    .getIcon(ICON_DEVICE_REMOTE_UNMOUNTED_SMALL));
			}
			break;
		    }
		} else if (value instanceof DirectoryNode) {
		    setBorder(null);
		    Directory dir = ((DirectoryNode) value).getDirectory();
		    boolean bSynchro = dir
			    .getBooleanValue(XML_DIRECTORY_SYNCHRONIZED);
		    if (bSynchro) { // means this device is not synchronized
                                        // //$NON-NLS-1$
			setIcon(Util.getIcon(ICON_DIRECTORY_SYNCHRO));
		    } else {
			setIcon(Util.getIcon(ICON_DIRECTORY_DESYNCHRO));
		    }
		}
		return this;
	    }
	});
	DefaultTreeModel treeModel = new DefaultTreeModel(top);
	// Tree model listener to detect changes in the tree structure
	treeModel.addTreeModelListener(new TreeModelListener() {

	    public void treeNodesChanged(TreeModelEvent e) {
		DefaultMutableTreeNode node;
		node = (DefaultMutableTreeNode) (e.getTreePath()
			.getLastPathComponent());

		try {
		    int index = e.getChildIndices()[0];
		    node = (DefaultMutableTreeNode) (node.getChildAt(index));
		} catch (NullPointerException exc) {
		}

	    }

	    public void treeNodesInserted(TreeModelEvent e) {
	    }

	    public void treeNodesRemoved(TreeModelEvent e) {
	    }

	    public void treeStructureChanged(TreeModelEvent e) {
	    }

	});

	// Tree selection listener to detect a selection
	jtree.addTreeSelectionListener(new TreeSelectionListener() {
	    public void valueChanged(TreeSelectionEvent e) {
		paths = jtree.getSelectionModel().getSelectionPaths();
		if (paths == null) { // nothing selected, can be called
                                        // during dnd
		    return;
		}
		HashSet<Item> hsSelectedFiles = new HashSet<Item>(100);
		int items = 0;
		long lSize = 0;
		// get all components recursively
		alSelected = new ArrayList<Item>(paths.length);
		for (int i = 0; i < paths.length; i++) {
		    Object o = paths[i].getLastPathComponent();
		    if (o instanceof TransferableTreeNode) {
			alSelected.add((Item) ((TransferableTreeNode) o)
				.getData());
		    } else {// root node
			alSelected = new ArrayList<Item>(FileManager
				.getInstance().getFiles());
			items = alSelected.size();
			hsSelectedFiles.addAll(alSelected);
			for (Item item : alSelected) {
			    lSize += ((File) item).getSize();
			}
			break;
		    }
		    Enumeration e2 = ((DefaultMutableTreeNode) o)
			    .depthFirstEnumeration(); // return all childs
                                                        // nodes recursively
		    while (e2.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e2
				.nextElement();
			if (node instanceof FileNode) {
			    File file = ((FileNode) node).getFile();
			    if (hsSelectedFiles.contains(file)) { // don't
                                                                        // count
                                                                        // the
                                                                        // same
                                                                        // file
                                                                        // twice
                                                                        // if
                                                                        // user
                                                                        // select
                                                                        // directory
                                                                        // and
                                                                        // then
                                                                        // files
                                                                        // inside
				continue;
			    }
			    lSize += file.getSize();
			    items++;
			    hsSelectedFiles.add(file);
			}
		    }
		}
		lSize /= 1048576; // set size in MB
		StringBuffer sbOut = new StringBuffer().append(items).append(
			Messages.getString("PhysicalTreeView.52")); //$NON-NLS-1$
		if (lSize > 1024) { // more than 1024 MB -> in GB
		    sbOut.append(lSize / 1024).append('.').append(lSize % 1024)
			    .append(Messages.getString("PhysicalTreeView.53")); //$NON-NLS-1$
		} else {
		    sbOut.append(lSize).append(
			    Messages.getString("PhysicalTreeView.54")); //$NON-NLS-1$
		}
		InformationJPanel.getInstance().setSelection(sbOut.toString());
		if (ConfigurationManager
			.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE)) { // if
                                                                        // table
                                                                        // is
                                                                        // synchronized
                                                                        // with
                                                                        // tree,
                                                                        // notify
                                                                        // the
                                                                        // selection
                                                                        // change
		    Properties properties = new Properties();
		    properties.put(DETAIL_SELECTION, hsSelectedFiles);
		    ObservationManager.notify(new Event(
			    EventSubject.EVENT_SYNC_TREE_TABLE, properties));
		}
		// No CDDB on directories without files
		if (alSelected.size() > 0
			&& alSelected.get(0) instanceof Directory) {
		    boolean bShowCDDB = false;
		    for (Item item : alSelected) { // if at least one
                                                        // selected dir contains
                                                        // a file, show option
			Directory dir = (Directory) item;
			if (dir.getFiles().size() > 0) {
			    bShowCDDB = true;
			}
		    }
		    jmiDirCDDBQuery.setEnabled(bShowCDDB);
		}
	    }
	});
	// Listen for double clic
	MouseListener ml = new MouseAdapter() {
	    public void mousePressed(final MouseEvent e) {
		TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
		if (path == null) {
		    return;
		}
		if (e.getClickCount() == 2) {
		    Object o = path.getLastPathComponent();
		    if (o instanceof FileNode) {
			File file = ((FileNode) o).getFile();
			try {
			    FIFO
				    .getInstance()
				    .push(
					    new StackItem(
						    file,
						    ConfigurationManager
							    .getBoolean(CONF_STATE_REPEAT),
						    true),
					    ConfigurationManager
						    .getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
			} catch (JajukException je) {
			    Log.error(je);
			}
		    } else if (o instanceof PlaylistFileNode) { // double clic
                                                                // on a playlist
                                                                // file
			PlaylistFile plf = ((PlaylistFileNode) o)
				.getPlaylistFile();
			ArrayList<File> alFiles = new ArrayList<File>(10);
			try {
			    alFiles = plf.getFiles();
			} catch (JajukException je) {
			    Log.error(je.getCode(), plf.getName(), null); //$NON-NLS-1$
			    Messages.showErrorMessage(je.getCode(), plf
				    .getName()); //$NON-NLS-1$
			    return;
			}
			if (alFiles.size() == 0) { // check playlist file
                                                        // contains accessible
                                                        // tracks
			    Messages.showErrorMessage("018"); //$NON-NLS-1$
			    return;
			} else {
			    FIFO
				    .getInstance()
				    .push(
					    Util
						    .createStackItems(
							    Util
								    .applyPlayOption(alFiles),
							    ConfigurationManager
								    .getBoolean(CONF_STATE_REPEAT),
							    true), false);
			}
		    }
		} else if (e.getClickCount() == 1
			&& e.getButton() == MouseEvent.BUTTON3) { // right
                                                                        // clic
                                                                        // on a
                                                                        // selected
                                                                        // node
                                                                        // set
		    // Right clic behavior identical to konqueror tree:
		    // if none or 1 node is selected, a right click on
                        // another node select it
		    // if more than 1, we keep selection and display a popup
                        // for them
		    if (jtree.getSelectionCount() < 2) {
			jtree.getSelectionModel().setSelectionPath(path);
		    }
		    paths = jtree.getSelectionModel().getSelectionPaths();
		    getInstance().alFiles = new ArrayList<File>(100);
		    getInstance().alDirs = new ArrayList<Directory>(10);
		    // test mix between types ( not allowed )
		    Class c = paths[0].getLastPathComponent().getClass();
		    for (int i = 0; i < paths.length; i++) {
			if (!paths[i].getLastPathComponent().getClass().equals(
				c)) {
			    return;
			}
		    }
		    // Test that all items are mounted or hide menu item
		    // device:mono selection for the moment
		    if (c.equals(DeviceNode.class)) {
			Device device = ((DeviceNode) (paths[0]
				.getLastPathComponent())).getDevice();
			if (device.isMounted()) {
			    jmiDevMount.setEnabled(false);
			    jmiDevUnmount.setEnabled(true);
			} else {
			    jmiDevMount.setEnabled(true);
			    jmiDevUnmount.setEnabled(false);
			}
			final Directory dir = DirectoryManager.getInstance()
				.registerDirectory(device);
			boolean bShowCDDB = false;
			if (dir.getFiles().size() > 0) {
			    bShowCDDB = true;
			}
			jmiDevCDDBQuery.setEnabled(bShowCDDB);
		    }
		    if (c.equals(DirectoryNode.class)) {
			// NBI jmiDirCopy.setEnabled(true);
			// NBI jmiDirCreatePlaylist.setEnabled(true);
			// NBI jmiDirCut.setEnabled(true);
			// NBI jmiDirDelete.setEnabled(true);
			// NBI jmiDirPaste.setEnabled(true);
			for (int i = 0; i < paths.length; i++) {
			    Directory dir = ((DirectoryNode) (paths[i]
				    .getLastPathComponent())).getDirectory();
			    if (!dir.getDevice().isMounted()) {
				// NBI jmiDirCopy.setEnabled(false);
				// NBI jmiDirCreatePlaylist.setEnabled(false);
				// NBI jmiDirCut.setEnabled(false);
				// NBI jmiDirDelete.setEnabled(false);
				// NBI jmiDirPaste.setEnabled(false);
				continue;
			    }
			}
		    }
		    // NBI jmiFileCopy.setEnabled(true);
		    // NBI jmiFileCut.setEnabled(true);
		    // NBI jmiFileDelete.setEnabled(true);
		    // NBI jmiFilePaste.setEnabled(true);
		    if (c.equals(FileNode.class)) {
			for (int i = 0; i < paths.length; i++) {
			    File file = ((FileNode) (paths[i]
				    .getLastPathComponent())).getFile();
			    if (!file.isReady()) {
				// NBI jmiFileCopy.setEnabled(false);
				// NBI jmiFileCut.setEnabled(false);
				// NBI jmiFileDelete.setEnabled(false);
				// NBI jmiFilePaste.setEnabled(false);
				continue;
			    }
			}
		    }
		    // NBI jmiPlaylistFileCopy.setEnabled(true);
		    // NBI jmiPlaylistFileCut.setEnabled(true);
		    jmiPlaylistFileDelete.setEnabled(true);
		    // NBI jmiPlaylistFilePaste.setEnabled(true);
		    if (c.equals(PlaylistFileNode.class)) {
			for (int i = 0; i < paths.length; i++) {
			    PlaylistFile plf = ((PlaylistFileNode) (paths[i]
				    .getLastPathComponent())).getPlaylistFile();
			    if (!plf.isReady()) {
				// NBI jmiPlaylistFileCopy.setEnabled(false);
				// NBI jmiPlaylistFileCut.setEnabled(false);
				jmiPlaylistFileDelete.setEnabled(false);
				// NBI jmiPlaylistFilePaste.setEnabled(false);
				continue;
			    }
			}
		    }

		    // get all components recursively
		    for (int i = 0; i < paths.length; i++) {
			Object o = paths[i].getLastPathComponent();
			Enumeration e2 = ((DefaultMutableTreeNode) o)
				.depthFirstEnumeration(); // return all
                                                                // childs nodes
                                                                // recursively
			while (e2.hasMoreElements()) {
			    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e2
				    .nextElement();
			    if (node instanceof FileNode) {
				getInstance().alFiles.add(((FileNode) node)
					.getFile());
			    } else if (node instanceof DirectoryNode) {
				Directory dir = ((DirectoryNode) node)
					.getDirectory();
				getInstance().alDirs.add(dir);
			    }
			}
		    }
		    // display menus according node type
		    if (paths[0].getLastPathComponent() instanceof FileNode) {
			jmenuFile.show(jtree, e.getX(), e.getY());
		    } else if (paths[0].getLastPathComponent() instanceof DirectoryNode) {
			jmenuDir.show(jtree, e.getX(), e.getY());
		    } else if (paths[0].getLastPathComponent() instanceof PlaylistFileNode) {
			jmenuPlaylistFile.show(jtree, e.getX(), e.getY());
		    } else if (paths[0].getLastPathComponent() instanceof DeviceNode) {
			if (paths.length > 1) { // operations on devices are
                                                // mono-target
			    return;
			}
			Device device = ((DeviceNode) paths[0]
				.getLastPathComponent()).getDevice();
			if (device.getValue(XML_DEVICE_SYNCHRO_SOURCE).equals(
				"")) { // if the device is not synchronized
                                        // //$NON-NLS-1$
			    jmiDevSynchronize.setEnabled(false);
			} else {
			    jmiDevSynchronize.setEnabled(true);
			}
			jmenuDev.show(jtree, e.getX(), e.getY());
		    } else if (paths[0].getLastPathComponent() instanceof DefaultMutableTreeNode) {
			// jmenuCollection.show(jtree, e.getX(), e.getY());
		    }
		}
	    }
	};
	jtree.addMouseListener(ml);
	// Expansion analyse to keep expended state
	jtree.addTreeExpansionListener(new TreeExpansionListener() {
	    public void treeCollapsed(TreeExpansionEvent event) {
		Object o = event.getPath().getLastPathComponent();
		if (o instanceof DirectoryNode && !bAutoCollapse) {
		    Directory dir = ((DirectoryNode) o).getDirectory();
		    dir.removeProperty(XML_EXPANDED);
		} else if (o instanceof DeviceNode && !bAutoCollapse) {
		    Device device = ((DeviceNode) o).getDevice();
		    device.removeProperty(XML_EXPANDED);
		}
	    }

	    public void treeExpanded(TreeExpansionEvent event) {
		Object o = event.getPath().getLastPathComponent();
		if (o instanceof DirectoryNode && !bAutoCollapse) {
		    Directory dir = ((DirectoryNode) o).getDirectory();
		    dir.setProperty(XML_EXPANDED, true); //$NON-NLS-1$
		} else if (o instanceof DeviceNode && !bAutoCollapse) {
		    Device device = ((DeviceNode) o).getDevice();
		    device.setProperty(XML_EXPANDED, true); //$NON-NLS-1$
		}

	    }
	});
	jtree.setAutoscrolls(true);
	// DND support
	new TreeTransferHandler(jtree, DnDConstants.ACTION_COPY_OR_MOVE, true);
	// tree itself
	jspTree = new JScrollPane(jtree);
	add(jspTree);
	// expand all
	expand(true);

    }

    /** Fill the tree */
    public synchronized void populateTree() {
	top.removeAllChildren();
	// add devices
	Iterator<Device> it1 = DeviceManager.getInstance().getDevices()
		.iterator();
	while (it1.hasNext()) {
	    Device device = it1.next();
	    DefaultMutableTreeNode nodeDevice = new DeviceNode(device);
	    top.add(nodeDevice);
	}
	// add directories
	ArrayList<Directory> directories = null;
	directories = new ArrayList<Directory>(DirectoryManager.getInstance()
		.getDirectories());
	Iterator it2 = directories.iterator();
	while (it2.hasNext()) {
	    Directory directory = (Directory) it2.next();
	    if (directory.shouldBeHidden()) {
		continue;
	    }
	    if (directory.getParentDirectory() != null) { // device root
                                                                // directory, do
                                                                // not display
                                                                // //$NON-NLS-1$
		if (directory.getParentDirectory().getName().equals("")) { // parent
                                                                                // directory
                                                                                // is a
                                                                                // device
                                                                                // //$NON-NLS-1$
		    DeviceNode deviceNode = DeviceNode.getDeviceNode(directory
			    .getDevice());
		    if (deviceNode != null) {
			deviceNode.add(new DirectoryNode(directory));
		    }
		} else { // parent directory not root
		    DirectoryNode parentDirectoryNode = DirectoryNode
			    .getDirectoryNode(directory.getParentDirectory());
		    if (parentDirectoryNode != null) { // paranoia check
			parentDirectoryNode.add(new DirectoryNode(directory));
		    }
		}
	    } else { // add file at the device root
		DeviceNode deviceNode = DeviceNode.getDeviceNode(directory
			.getDevice());
		Iterator it = directory.getFiles().iterator();
		while (it.hasNext()) {
		    deviceNode.add(new FileNode((File) it.next()));
		}
		// add playlist files
		it = directory.getPlaylistFiles().iterator();
		while (it.hasNext()) {
		    deviceNode.add(new PlaylistFileNode((PlaylistFile) it
			    .next()));
		}
	    }
	}
	// add files
	ArrayList<File> files = new ArrayList<File>(FileManager.getInstance()
		.getFiles());
	Iterator it3 = files.iterator();
	while (it3.hasNext()) {
	    File file = (File) it3.next();
	    if (file.shouldBeHidden()) { // should be hiden by option
		continue;
	    }
	    DirectoryNode directoryNode = DirectoryNode.getDirectoryNode(file
		    .getDirectory());
	    if (directoryNode != null) {
		directoryNode.add(new FileNode(file));
	    }
	}

	// add playlist files
	ArrayList<PlaylistFile> playlists = new ArrayList<PlaylistFile>(
		PlaylistFileManager.getInstance().getPlaylistFiles());
	Iterator it4 = playlists.iterator();
	while (it4.hasNext()) {
	    PlaylistFile playlistFile = (PlaylistFile) it4.next();
	    if (playlistFile.shouldBeHidden()) { // should be hiden by
                                                        // option
		continue;
	    }
	    DirectoryNode directoryNode = DirectoryNode
		    .getDirectoryNode(playlistFile.getDirectory());
	    if (directoryNode != null) {
		directoryNode.add(new PlaylistFileNode(playlistFile));
	    }
	}

    }

    /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
    public void actionPerformed(final ActionEvent e) {
	// multiple selection on properties(note we handle files and dirs
        // properties later)
	if ((paths.length > 1)
		&& (e.getSource() == jmiDevProperties || e.getSource() == jmiPlaylistFileProperties)) {
	    new PropertiesWizard(alSelected);
	} else if (e.getSource() == jmiFilePlay) {
	    FIFO.getInstance().push(
		    Util.createStackItems(alFiles, ConfigurationManager
			    .getBoolean(CONF_STATE_REPEAT), true), false);
	} else if (e.getSource() == jmiFileAddFavorites) {
	    Bookmarks.getInstance().addFiles(alFiles);
	} else if (e.getSource() == jmiFilePush) {
	    FIFO.getInstance().push(
		    Util.createStackItems(Util.applyPlayOption(alFiles),
			    ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
			    true), true);
	} else if (alFiles != null
		&& (e.getSource() == jmiDirPlay || e.getSource() == jmiDevPlay)) {
	    FIFO.getInstance().push(
		    Util.createStackItems(Util.applyPlayOption(alFiles),
			    ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
			    true), false);
	} else if (alFiles != null && e.getSource() == jmiDirAddFavorites) {
	    Bookmarks.getInstance().addFiles(alFiles);
	} else if (alFiles != null && (e.getSource() == jmiDirCDDBQuery)) {
	    ArrayList<Item> alCDDBTracks = new ArrayList<Item>();
	    for (Item item : alSelected) {
		final Directory dir = (Directory) item;
		Util.waiting();
		for (File file : dir.getFiles()) {
		    alCDDBTracks.add(file.getTrack());
		}
		new CDDBWizard(alCDDBTracks);
	    }
	} else if (alFiles != null && e.getSource() == jmiDevCDDBQuery) {
	    Device device = ((DeviceNode) (paths[0].getLastPathComponent()))
		    .getDevice();
	    final Directory dir = DirectoryManager.getInstance()
		    .registerDirectory(device);
	    Util.waiting();
	    ArrayList<Item> alCDDBTracks = new ArrayList<Item>(100);
	    for (File file : dir.getFiles()) {
		alCDDBTracks.add(file.getTrack());
	    }
	    new CDDBWizard(alCDDBTracks);
	} else if ((alFiles != null && e.getSource() == jmiDirRefactor)) {
	    Util.waiting();
	    for (Item item : alSelected) {
		final Directory dir = (Directory) item;
		Util.waiting();
		new RefactorAction(dir.getFilesRecursively());
	    }
	} else if ((alFiles != null && e.getSource() == jmiDirExport)
		|| (e.getSource() == jmiDevExport || (e.getSource() == jmiCollectionExport))) {
	    // Create filters.
	    ExportFileFilter xmlFilter = new ExportFileFilter(".xml");
	    ExportFileFilter htmlFilter = new ExportFileFilter(".html");
	    // ExportFileFilter pdfFilter = new ExportFileFilter(".pdf");

	    JFileChooser filechooser = new JFileChooser();
	    // Add filters.
	    filechooser.addChoosableFileFilter(xmlFilter);
	    filechooser.addChoosableFileFilter(htmlFilter);
	    // filechooser.addChoosableFileFilter(pdfFilter);

	    filechooser.setCurrentDirectory(new java.io.File(System
		    .getProperty("user.home"))); //$NON-NLS-1$ 
	    filechooser.setDialogTitle(Messages
		    .getString("PhysicalTreeView.58"));
	    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    /*
                 * java.text.DateFormat dateFormat = new
                 * java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                 * java.util.Date date = new java.util.Date(); try {
                 * filechooser.setSelectedFile(new
                 * java.io.File("JajukMusicReport-"+dateFormat.parse(dateFormat.format(date)))); }
                 * catch (java.text.ParseException ex) { Log.error(ex);
                 * filechooser.setSelectedFile(new
                 * java.io.File("JajukMusicReport")); }
                 */

	    int returnVal = filechooser.showSaveDialog(PhysicalTreeView.this);

	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		java.io.File file = filechooser.getSelectedFile();
		String filepath = file.getAbsolutePath();
		String filetypename = Util.getExtension(file);

		if (filetypename.equals("")) {
		    ExportFileFilter filter = (ExportFileFilter) filechooser
			    .getFileFilter();
		    filetypename = filter.getExtension();
		    filepath += "." + filetypename;
		}

		String result = ""; //$NON-NLS-1$

		// If we are exporting to xml...
		if (filetypename.equals("xml")) { //$NON-NLS-1$
		    XMLExporter xmlExporter = XMLExporter.getInstance();

		    // If we are exporting a directory...
		    if (e.getSource() == jmiDirExport) {
			Directory dir = ((DirectoryNode) paths[0]
				.getLastPathComponent()).getDirectory();
			result = xmlExporter.process(dir);
			// Else if we are exporting a device...
		    } else if (e.getSource() == jmiDevExport) {
			Device device = ((DeviceNode) paths[0]
				.getLastPathComponent()).getDevice();
			result = xmlExporter.process(device);
			// Else if we are exporting the entire collection...
		    } else if (e.getSource() == jmiCollectionExport) {
			result = xmlExporter.processCollection(
				XMLExporter.PHYSICAL_COLLECTION, null);
		    }

		    if (result != null) {
			// Save the results.
			if (!xmlExporter.saveToFile(result, filepath)) {
			    Log
				    .error("Could not write out the xml to the specified file.");
			}
		    } else {
			Log.error("Could not create report.");
		    }
		    // Else if we are exporting to html...
		} else if (filetypename.equals("html")
			|| filetypename.equals("htm")) {
		    HTMLExporter htmlExporter = HTMLExporter.getInstance();

		    // If we are exporting a directory...
		    if (e.getSource() == jmiDirExport) {
			Directory dir = ((DirectoryNode) paths[0]
				.getLastPathComponent()).getDirectory();
			result = htmlExporter.process(dir);
			// Else if we are exporting a device...
		    } else if (e.getSource() == jmiDevExport) {
			Device device = ((DeviceNode) paths[0]
				.getLastPathComponent()).getDevice();
			result = htmlExporter.process(device);
			// Else if we are exporting the entire collection...
		    } else if (e.getSource() == jmiCollectionExport) {
			result = htmlExporter.processCollection(
				HTMLExporter.PHYSICAL_COLLECTION, null);
		    }

		    if (result != null) {
			// Save the results.
			if (!htmlExporter.saveToFile(result, filepath)) {
			    Log
				    .error("Could not write out the html to the specified file.");
			}
		    } else {
			Log.error("Could not create report.");
		    }
		    // Else if we are exporting to pdf...
		} /*
                         * else if (filetypename.equals("pdf")) { boolean
                         * bResult = false; PDFExporter pdfExporter =
                         * PDFExporter.getInstance();
                         *  // If we are exporting a directory... if
                         * (e.getSource() == jmiDirExport) { Directory dir =
                         * ((DirectoryNode)paths[0].getLastPathComponent()).getDirectory();
                         * bResult = pdfExporter.process(dir, filepath); }
                         * 
                         * if (bResult == false) { Log.error("Could not create
                         * the pdf file."); } }
                         */
	    }
	} else if (alFiles != null
		&& (e.getSource() == jmiDirPush || e.getSource() == jmiDevPush)) {
	    FIFO.getInstance().push(
		    Util.createStackItems(Util.applyPlayOption(alFiles),
			    ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
			    true), true);
	} else if (alFiles != null
		&& (e.getSource() == jmiDirPlayShuffle || e.getSource() == jmiDevPlayShuffle)) {
	    Collections
		    .shuffle(alFiles, new Random(System.currentTimeMillis()));
	    FIFO.getInstance().push(
		    Util.createStackItems(alFiles, ConfigurationManager
			    .getBoolean(CONF_STATE_REPEAT), true), false);
	} else if (alFiles != null
		&& (e.getSource() == jmiDirPlayRepeat || e.getSource() == jmiDevPlayRepeat)) {
	    FIFO.getInstance().push(
		    Util.createStackItems(Util.applyPlayOption(alFiles), true,
			    true), false);
	} else if (e.getSource() == jmiDevMount) {
	    for (int i = 0; i < paths.length; i++) {
		Device device = ((DeviceNode) (paths[i].getLastPathComponent()))
			.getDevice();
		try {
		    device.mount();
		} catch (Exception ex) {
		    Messages.showErrorMessage("011"); //$NON-NLS-1$
		}
	    }
	} else if (e.getSource() == jmiDevUnmount) {
	    for (int i = 0; i < paths.length; i++) {
		Device device = ((DeviceNode) (paths[i].getLastPathComponent()))
			.getDevice();
		try {
		    device.unmount();
		} catch (Exception ex) {
		    Messages.showErrorMessage("012"); //$NON-NLS-1$
		}
	    }
	} else if (e.getSource() == jmiDevRefresh) {
	    Device device = ((DeviceNode) (paths[0].getLastPathComponent()))
		    .getDevice();
	    device.refresh(true, true); // ask user if he wants to make deep or
                                        // fast scan
	} else if (e.getSource() == jmiDevSynchronize) {
	    Device device = ((DeviceNode) (paths[0].getLastPathComponent()))
		    .getDevice();
	    device.synchronize(true);
	} else if (e.getSource() == jmiDevTest) {
	    new Thread() { // test asynchronously in case of delay (samba
                                // pbm for ie)
		public void run() {
		    Device device = ((DeviceNode) (paths[0]
			    .getLastPathComponent())).getDevice();
		    if (device.test()) {
			Messages
				.showInfoMessage(
					Messages.getString("DeviceView.21"), Util.getIcon(ICON_OK)); //$NON-NLS-1$
		    } else {
			Messages
				.showInfoMessage(
					Messages.getString("DeviceView.22"), Util.getIcon(ICON_KO)); //$NON-NLS-1$
		    }
		}
	    }.start();
	} else if (e.getSource() == jmiDirDesynchro) {
	    Iterator it = alDirs.iterator(); // iterate on selected dirs and
                                                // childs recursively
	    while (it.hasNext()) {
		Directory dir = (Directory) it.next();
		dir.setProperty(XML_DIRECTORY_SYNCHRONIZED, false);
	    }
	    jtree.revalidate();
	    jtree.repaint();
	} else if (e.getSource() == jmiDirResynchro) {
	    Iterator it = alDirs.iterator(); // iterate on selected dirs and
                                                // childs recursively
	    while (it.hasNext()) {
		Directory dir = (Directory) it.next();
		dir.setProperty(XML_DIRECTORY_SYNCHRONIZED, true);
	    }
	    jtree.revalidate();
	    jtree.repaint();
	} else if (e.getSource() == jmiPlaylistFilePlay
		|| e.getSource() == jmiPlaylistFilePush
		|| e.getSource() == jmiPlaylistFilePlayShuffle
		|| e.getSource() == jmiPlaylistFilePlayRepeat) {
	    PlaylistFile plf = ((PlaylistFileNode) paths[0]
		    .getLastPathComponent()).getPlaylistFile();
	    ArrayList<File> alFiles = new ArrayList<File>(10);
	    try {
		alFiles = plf.getFiles();
	    } catch (JajukException je) {
		Log.error(je.getCode(), plf.getName(), null); //$NON-NLS-1$
		Messages.showErrorMessage(je.getCode(), plf.getName()); //$NON-NLS-1$
		return;
	    }
	    if (alFiles.size() == 0) { // check playlist file contains
                                        // accessible tracks
		Messages.showErrorMessage("018"); //$NON-NLS-1$
		return;
	    } else { // specific actions
		if (e.getSource() == jmiPlaylistFilePlay) {
		    FIFO.getInstance().push(
			    Util.createStackItems(
				    Util.applyPlayOption(alFiles),
				    ConfigurationManager
					    .getBoolean(CONF_STATE_REPEAT),
				    true), false);
		} else if (e.getSource() == jmiPlaylistFilePush) {
		    FIFO.getInstance().push(
			    Util.createStackItems(
				    Util.applyPlayOption(alFiles),
				    ConfigurationManager
					    .getBoolean(CONF_STATE_REPEAT),
				    true), true);
		} else if (e.getSource() == jmiPlaylistFilePlayShuffle) {
		    Collections.shuffle(alFiles, new Random(System
			    .currentTimeMillis()));
		    FIFO.getInstance().push(
			    Util.createStackItems(alFiles, ConfigurationManager
				    .getBoolean(CONF_STATE_REPEAT), true),
			    false);
		} else if (e.getSource() == jmiPlaylistFilePlayRepeat) {
		    FIFO.getInstance().push(
			    Util.createStackItems(
				    Util.applyPlayOption(alFiles), true, true),
			    false);
		} else if (e.getSource() == jmiPlaylistAddFavorites) {
		    Bookmarks.getInstance().addFiles(alFiles);
		}
	    }
	} else if (e.getSource() == jmiPlaylistFileDelete) {
	    if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE)) { // file
                                                                                        // delete
                                                                                        // confirmation
		PlaylistFile plf = ((PlaylistFileNode) paths[0]
			.getLastPathComponent()).getPlaylistFile();
		String sFileToDelete = plf.getAbsolutePath(); //$NON-NLS-1$
		String sMessage = Messages.getString("Confirmation_delete") + "\n" + sFileToDelete; //$NON-NLS-1$ //$NON-NLS-2$
		int i = Messages.getChoice(sMessage,
			JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
		if (i == JOptionPane.OK_OPTION) {
		    PlaylistFileManager.getInstance().removePlaylistFile(plf);
		    ObservationManager.notify(new Event(
			    EventSubject.EVENT_DEVICE_REFRESH)); // requires
                                                                        // device
                                                                        // refresh
		}
	    }
	} else if (e.getSource() == jmiDevConfiguration) {
	    Device device = ((DeviceNode) paths[0].getLastPathComponent())
		    .getDevice();
	    DeviceWizard dw = new DeviceWizard();
	    dw.updateWidgets(device);
	    dw.pack();
	    dw.setVisible(true);
	} else if (e.getSource() == jmiFileProperties) {
	    ArrayList<Item> alTracks = new ArrayList<Item>(alSelected.size()); // tracks
                                                                                // items
	    for (Item pa : alSelected) {
		File file = (File) pa;
		alTracks.add(file.getTrack());
	    }
	    new PropertiesWizard(alSelected, alTracks);
	} else if (e.getSource() == jmiDirProperties) {
	    ArrayList<Item> alTracks = new ArrayList<Item>(alSelected.size());
	    for (Item item : alSelected) {
		Directory dir = (Directory) item;
		for (File file : dir.getFilesRecursively()) {
		    Track track = file.getTrack();
		    if (!alTracks.contains(track)) {
			alTracks.add(track);
		    }
		}
	    }
	    new PropertiesWizard(alSelected, alTracks);
	} else if (e.getSource() == jmiDevProperties) {
	    Device device = ((DeviceNode) paths[0].getLastPathComponent())
		    .getDevice();
	    ArrayList<Device> alItems = new ArrayList<Device>(1);
	    alItems.add(device);
	} else if (e.getSource() == jmiPlaylistFileProperties) {
	    PlaylistFile plf = ((PlaylistFileNode) paths[0]
		    .getLastPathComponent()).getPlaylistFile();
	    ArrayList<Item> alItems = new ArrayList<Item>(1);
	    alItems.add(plf);
	    new PropertiesWizard(alItems);
	}
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.jajuk.ui.Observer#update(java.lang.String)
         */
    public void update(Event event) {
	final EventSubject subject = event.getSubject();
	if (subject.equals(EventSubject.EVENT_FILE_LAUNCHED)) { // used for
                                                                // current track
                                                                // display
                                                                // refresh
	    repaint();
	} else if (subject.equals(EventSubject.EVENT_DEVICE_MOUNT)
		|| subject.equals(EventSubject.EVENT_DEVICE_UNMOUNT)
		|| subject.equals(EventSubject.EVENT_DEVICE_REFRESH)) {
	    SwingWorker sw = new SwingWorker() {
		public Object construct() {
		    populateTree();
		    return null;
		}

		public void finished() {
		    SwingUtilities.updateComponentTreeUI(jtree);
		    bAutoCollapse = true;
		    // Do not collapse unmounted devices for this event
                        // (common), we want to keep unmounted devices expanded
		    if (subject.equals(EventSubject.EVENT_DEVICE_REFRESH)) {
			expand(false);
		    } else {
			expand(true);
		    }
		    bAutoCollapse = false;
		    int i = jspTree.getVerticalScrollBar().getValue();
		    jspTree.getVerticalScrollBar().setValue(i);
		}
	    };
	    sw.start();
	}
    }

    /**
         * Manages auto-expand
         * 
         */
    private void expand(boolean bDependsOnMountState) {
	// begin by expanding all needed devices and directory, only after,
        // collapse unmounted devices if required
	for (int i = 0; i < jtree.getRowCount(); i++) {
	    Object o = jtree.getPathForRow(i).getLastPathComponent();
	    if (o instanceof DeviceNode) {
		Device device = ((DeviceNode) o).getDevice();
		boolean bExp = device.getBooleanValue(XML_EXPANDED);
		if (bExp) {
		    jtree.expandRow(i);
		}
	    } else if (o instanceof DirectoryNode) {
		Directory dir = ((DirectoryNode) o).getDirectory();
		boolean bExp = dir.getBooleanValue(XML_EXPANDED);
		if (bExp) { //$NON-NLS-1$
		    jtree.expandRow(i);
		}
	    }
	}
	// Now collapse unmounted devices is needed, we have to do it after
        // expanding previous files
	for (int i = 0; i < jtree.getRowCount(); i++) {
	    Object o = jtree.getPathForRow(i).getLastPathComponent();
	    if (o instanceof DeviceNode) {
		// we want to expand following user selection (exp attribute)
                // except after a mount (force expand) or
		// an unmount (force collapse)
		if (bDependsOnMountState) {
		    if (((DeviceNode) o).getDevice().isMounted()) {
			jtree.expandRow(i);
		    } else {
			jtree.collapseRow(i);
		    }
		}
	    }
	}
    }

}

/**
 * File node
 * 
 * @author Bertrand Florat
 * @created 29 nov. 2003
 */
class FileNode extends TransferableTreeNode {

    private static final long serialVersionUID = 1L;

    /**
         * Constructor
         * 
         * @param file
         */
    public FileNode(File file) {
	super(file);
    }

    /**
         * return a string representation of this file node
         */
    public String toString() {
	return ((File) super.getData()).getName();
    }

    /**
         * @return Returns the file.
         */
    public File getFile() {
	return (File) super.getData();
    }

}

/**
 * Device node
 * 
 * @author Bertrand Florat
 * @created 29 nov. 2003
 */
class DeviceNode extends TransferableTreeNode {

    private static final long serialVersionUID = 1L;

    /** device -> deviceNode hashmap */
    public static HashMap<Device, DeviceNode> hmDeviceDeviceNode = new HashMap<Device, DeviceNode>(
	    100);

    /**
         * Constructor
         * 
         * @param device
         */
    public DeviceNode(Device device) {
	super(device);
	hmDeviceDeviceNode.put(device, this);
    }

    /** Return associated device node */
    public static DeviceNode getDeviceNode(Device device) {
	return hmDeviceDeviceNode.get(device);
    }

    /**
         * return a string representation of this device node
         */
    public String toString() {
	return ((Device) super.getData()).getName();
    }

    /**
         * @return Returns the device.
         */
    public Device getDevice() {
	return (Device) super.getData();
    }

}

/**
 * Directory node
 * 
 * @author Bertrand Florat
 * @created 29 nov. 2003
 */
class DirectoryNode extends TransferableTreeNode {

    /**
         * 
         */
    private static final long serialVersionUID = 1L;

    /** directory -> directoryNode hashmap */
    public static HashMap<Directory, DirectoryNode> hmDirectoryDirectoryNode = new HashMap<Directory, DirectoryNode>(
	    100);

    /**
         * Constructor
         * 
         * @param Directory
         */
    public DirectoryNode(Directory directory) {
	super(directory);
	hmDirectoryDirectoryNode.put(directory, this);
    }

    /** Return associated directory node */
    public static DirectoryNode getDirectoryNode(Directory directory) {
	return hmDirectoryDirectoryNode.get(directory);
    }

    /**
         * return a string representation of this directory node
         */
    public String toString() {
	return ((Directory) getData()).getName();
    }

    /**
         * @return Returns the directory.
         */
    public Directory getDirectory() {
	return (Directory) getData();
    }

}

/**
 * PlaylistFile node
 * 
 * @author Bertrand Florat
 * @created 29 nov. 2003
 */
class PlaylistFileNode extends TransferableTreeNode {

    private static final long serialVersionUID = 1L;

    /**
         * Constructor
         * 
         * @param PlaylistFile
         */
    public PlaylistFileNode(PlaylistFile playlistFile) {
	super(playlistFile);
    }

    /**
         * return a string representation of this playlistFile node
         */
    public String toString() {
	return ((PlaylistFile) super.getData()).getName();
    }

    /**
         * @return Returns the playlist file node.
         */
    public PlaylistFile getPlaylistFile() {
	return (PlaylistFile) getData();
    }

}
