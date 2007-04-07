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

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Bookmarks;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.StackItem;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.TransferableTreeNode;
import org.jajuk.ui.TreeTransferHandler;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.ui.wizard.CDDBWizard;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import info.clearthought.layout.TableLayout;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
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

import ext.SwingWorker;

/**
 * Logical tree view
 * 
 * @author Bertrand Florat
 * @created 28 nov. 2003
 */
public class LogicalTreeView extends AbstractTreeView implements ActionListener, Observer {

	private static final long serialVersionUID = 1L;

	/** Self instance */
	private static LogicalTreeView ltv;

	static final int SORT_BY_STYLE = 0;

	static final int SORT_BY_AUTHOR = 1;

	static final int SORT_BY_ALBUM = 2;

	static final int SORT_BY_YEAR = 3;

	/** Track selection */
	ArrayList<Track> alTracks;

	JPopupMenu jmenuCollection;

	ButtonGroup btCollection;

	JRadioButtonMenuItem jmiCollectionStyle;

	JRadioButtonMenuItem jmiCollectionAuthor;

	JRadioButtonMenuItem jmiCollectionAlbum;

	JRadioButtonMenuItem jmiCollectionYear;

	JMenuItem jmiCollectionReport;

	JLabel jlSort;

	JComboBox jcbSort;

	JPopupMenu jmenuStyle;

	JMenuItem jmiStylePlay;

	JMenuItem jmiStylePush;

	JMenuItem jmiStylePlayShuffle;

	JMenuItem jmiStylePlayRepeat;

	JMenuItem jmiStyleDelete;

	JMenuItem jmiStyleAddFavorite;

	JMenuItem jmiStyleReport;

	JMenuItem jmiStyleProperties;

	JPopupMenu jmenuAuthor;

	JMenuItem jmiAuthorPlay;

	JMenuItem jmiAuthorPush;

	JMenuItem jmiAuthorPlayShuffle;

	JMenuItem jmiAuthorPlayRepeat;

	JMenuItem jmiAuthorDelete;

	JMenuItem jmiAuthorAddFavorite;

	JMenuItem jmiAuthorReport;

	JMenuItem jmiAuthorProperties;

	JPopupMenu jmenuAlbum;

	JMenuItem jmiAlbumPlay;

	JMenuItem jmiAlbumPush;

	JMenuItem jmiAlbumPlayShuffle;

	JMenuItem jmiAlbumPlayRepeat;

	JMenuItem jmiAlbumDelete;

	JMenuItem jmiAlbumAddFavorite;

	JMenuItem jmiAlbumReport;

	JMenuItem jmiAlbumCDDBWizard;

	JMenuItem jmiAlbumProperties;

	JPopupMenu jmenuYear;

	JMenuItem jmiYearPlay;

	JMenuItem jmiYearPush;

	JMenuItem jmiYearPlayShuffle;

	JMenuItem jmiYearPlayRepeat;

	JMenuItem jmiYearDelete;

	JMenuItem jmiYearAddFavorite;

	JMenuItem jmiYearProperties;

	JPopupMenu jmenuTrack;

	JMenuItem jmiTrackPlay;

	JMenuItem jmiTrackPush;

	JMenuItem jmiTrackDelete;

	JMenuItem jmiTrackAddFavorite;

	JMenuItem jmiTrackProperties;

	private int iSortOrder = SORT_BY_STYLE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("LogicalTreeView.0"); //$NON-NLS-1$
	}

	/** Return singleton */
	public static synchronized LogicalTreeView getInstance() {
		if (ltv == null) {
			ltv = new LogicalTreeView();
		}
		return ltv;
	}

	/** Constructor */
	public LogicalTreeView() {
		ltv = this;
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_MOUNT);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_UNMOUNT);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
		return eventSubjectSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#display()
	 */
	public void initUI() {
		// init sort order
		iSortOrder = ConfigurationManager.getInt(CONF_LOGICAL_TREE_SORT_ORDER);

		// **Menu items**

		// ComboBox fort sort
		double[][] dSizeSort = { { 5, TableLayout.PREFERRED, 5, TableLayout.FILL },
				{ TableLayout.PREFERRED } };
		JPanel jpsort = new JPanel();
		jpsort.setLayout(new TableLayout(dSizeSort));
		jlSort = new JLabel(Messages.getString("Sort")); //$NON-NLS-1$
		jlSort.setOpaque(false);
		jcbSort = new JComboBox();
		jcbSort.addItem(Messages.getString("Property_style")); //$NON-NLS-1$
		jcbSort.addItem(Messages.getString("Property_author")); //$NON-NLS-1$
		jcbSort.addItem(Messages.getString("Property_album"));
		jcbSort.addItem(Messages.getString("Property_year"));
		jcbSort.setSelectedIndex(iSortOrder);
		jcbSort.setActionCommand(EventSubject.EVENT_LOGICAL_TREE_SORT.toString());
		jcbSort.addActionListener(this);
		jpsort.add(jlSort, "1,0"); //$NON-NLS-1$
		jpsort.add(jcbSort, "3,0"); //$NON-NLS-1$

		// Collection menu
		jmenuCollection = new JPopupMenu();
		btCollection = new ButtonGroup();
		// Style
		jmiCollectionStyle = new JRadioButtonMenuItem(Messages.getString("Property_style")); //$NON-NLS-1$
		jmiCollectionStyle.addActionListener(this);
		jmiCollectionStyle.setActionCommand(EventSubject.EVENT_LOGICAL_TREE_SORT.toString());
		if (ConfigurationManager.getInt(CONF_LOGICAL_TREE_SORT_ORDER) == 0) {
			jmiCollectionStyle.setSelected(true);
		}
		// Author
		jmiCollectionAuthor = new JRadioButtonMenuItem(Messages.getString("Property_author")); //$NON-NLS-1$
		jmiCollectionAuthor.addActionListener(this);
		jmiCollectionAuthor.setActionCommand(EventSubject.EVENT_LOGICAL_TREE_SORT.toString());
		if (ConfigurationManager.getInt(CONF_LOGICAL_TREE_SORT_ORDER) == 1) {
			jmiCollectionAuthor.setSelected(true);
		}
		// Album
		jmiCollectionAlbum = new JRadioButtonMenuItem(Messages.getString("Property_album")); //$NON-NLS-1$
		jmiCollectionAlbum.addActionListener(this);
		jmiCollectionAlbum.setActionCommand(EventSubject.EVENT_LOGICAL_TREE_SORT.toString());
		if (ConfigurationManager.getInt(CONF_LOGICAL_TREE_SORT_ORDER) == 2) {
			jmiCollectionAlbum.setSelected(true);
		}
		// Year
		jmiCollectionYear = new JRadioButtonMenuItem(Messages.getString("Property_year")); //$NON-NLS-1$
		jmiCollectionYear.addActionListener(this);
		jmiCollectionYear.setActionCommand(EventSubject.EVENT_LOGICAL_TREE_SORT.toString());
		if (ConfigurationManager.getInt(CONF_LOGICAL_TREE_SORT_ORDER) == 3) {
			jmiCollectionYear.setSelected(true);
		}
		// Export
		jmiCollectionReport = new JMenuItem(Messages.getString("LogicalTreeView.33")); //$NON-NLS-1$
		jmiCollectionReport.addActionListener(this);

		btCollection.add(jmiCollectionStyle);
		btCollection.add(jmiCollectionAuthor);
		btCollection.add(jmiCollectionAlbum);
		btCollection.add(jmiCollectionYear);
		jmenuCollection.add(new JLabel(Messages.getString("Sort"))); //$NON-NLS-1$
		jmenuCollection.add(jmiCollectionStyle);
		jmenuCollection.add(jmiCollectionAuthor);
		jmenuCollection.add(jmiCollectionAlbum);
		jmenuCollection.add(jmiCollectionYear);
		jmenuCollection.addSeparator();
		jmenuCollection.add(jmiCollectionReport);

		// Style menu
		jmenuStyle = new JPopupMenu();
		jmiStylePlay = new JMenuItem(Messages.getString("LogicalTreeView.1")); //$NON-NLS-1$
		jmiStylePlay.addActionListener(this);
		jmiStylePush = new JMenuItem(Messages.getString("LogicalTreeView.2")); //$NON-NLS-1$
		jmiStylePush.addActionListener(this);
		jmiStylePlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.3")); //$NON-NLS-1$
		jmiStylePlayShuffle.addActionListener(this);
		jmiStylePlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.4")); //$NON-NLS-1$
		jmiStylePlayRepeat.addActionListener(this);
		jmiStyleDelete = new JMenuItem(Messages.getString("LogicalTreeView.5")); //$NON-NLS-1$
		jmiStyleDelete.setEnabled(false);
		jmiStyleDelete.addActionListener(this);
		jmiStyleAddFavorite = new JMenuItem(Messages.getString("LogicalTreeView.32")); //$NON-NLS-1$
		jmiStyleAddFavorite.addActionListener(this);
		Action actionReportStyle = ActionManager.getAction(JajukAction.CREATE_REPORT);
		actionReportStyle.putValue(DETAIL_ORIGIN, "style");
		actionReportStyle.putValue(DETAIL_SELECTION, alSelected);
		jmiStyleReport = new JMenuItem(actionReportStyle);
		jmiStyleProperties = new JMenuItem(Messages.getString("LogicalTreeView.7")); //$NON-NLS-1$
		jmiStyleProperties.addActionListener(this);
		jmenuStyle.add(jmiStylePlay);
		jmenuStyle.add(jmiStylePush);
		jmenuStyle.add(jmiStylePlayShuffle);
		jmenuStyle.add(jmiStylePlayRepeat);
		jmenuStyle.add(jmiStyleDelete);
		jmenuStyle.add(jmiStyleAddFavorite);
		jmenuStyle.add(jmiStyleReport);
		jmenuStyle.add(jmiStyleProperties);

		// Author menu
		jmenuAuthor = new JPopupMenu();
		jmiAuthorPlay = new JMenuItem(Messages.getString("LogicalTreeView.8")); //$NON-NLS-1$
		jmiAuthorPlay.addActionListener(this);
		jmiAuthorPush = new JMenuItem(Messages.getString("LogicalTreeView.9")); //$NON-NLS-1$
		jmiAuthorPush.addActionListener(this);
		jmiAuthorPlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.10")); //$NON-NLS-1$
		jmiAuthorPlayShuffle.addActionListener(this);
		jmiAuthorPlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.11")); //$NON-NLS-1$
		jmiAuthorPlayRepeat.addActionListener(this);
		jmiAuthorDelete = new JMenuItem(Messages.getString("LogicalTreeView.12")); //$NON-NLS-1$
		jmiAuthorDelete.setEnabled(false);
		jmiAuthorDelete.addActionListener(this);
		jmiAuthorAddFavorite = new JMenuItem(Messages.getString("LogicalTreeView.32")); //$NON-NLS-1$       
		jmiAuthorAddFavorite.addActionListener(this);
		Action actionReportAuthor = ActionManager.getAction(JajukAction.CREATE_REPORT);
		actionReportAuthor.putValue(DETAIL_ORIGIN, "author");
		actionReportAuthor.putValue(DETAIL_SELECTION, alSelected);
		jmiAuthorReport = new JMenuItem(actionReportAuthor);
		jmiAuthorProperties = new JMenuItem(Messages.getString("LogicalTreeView.14")); //$NON-NLS-1$
		jmiAuthorProperties.addActionListener(this);
		jmenuAuthor.add(jmiAuthorPlay);
		jmenuAuthor.add(jmiAuthorPush);
		jmenuAuthor.add(jmiAuthorPlayShuffle);
		jmenuAuthor.add(jmiAuthorPlayRepeat);
		jmenuAuthor.add(jmiAuthorDelete);
		jmenuAuthor.add(jmiAuthorAddFavorite);
		jmenuAuthor.add(jmiAuthorReport);
		jmenuAuthor.add(jmiAuthorProperties);

		// Album menu
		jmenuAlbum = new JPopupMenu();
		jmiAlbumPlay = new JMenuItem(Messages.getString("LogicalTreeView.15")); //$NON-NLS-1$
		jmiAlbumPlay.addActionListener(this);
		jmiAlbumPush = new JMenuItem(Messages.getString("LogicalTreeView.16")); //$NON-NLS-1$
		jmiAlbumPush.addActionListener(this);
		jmiAlbumPlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.17")); //$NON-NLS-1$
		jmiAlbumPlayShuffle.addActionListener(this);
		jmiAlbumPlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.18")); //$NON-NLS-1$
		jmiAlbumPlayRepeat.addActionListener(this);
		jmiAlbumDelete = new JMenuItem(Messages.getString("LogicalTreeView.19")); //$NON-NLS-1$
		jmiAlbumDelete.setEnabled(false);
		jmiAlbumDelete.addActionListener(this);
		jmiAlbumAddFavorite = new JMenuItem(Messages.getString("LogicalTreeView.32")); //$NON-NLS-1$        
		jmiAlbumAddFavorite.addActionListener(this);
		Action actionReportAlbum = ActionManager.getAction(JajukAction.CREATE_REPORT);
		actionReportAlbum.putValue(DETAIL_ORIGIN, "Album");
		actionReportAlbum.putValue(DETAIL_SELECTION, alSelected);
		jmiAlbumReport = new JMenuItem(actionReportAlbum);
		jmiAlbumCDDBWizard = new JMenuItem(Messages.getString("LogicalTreeView.34")); //$NON-NLS-1$
		jmiAlbumCDDBWizard.addActionListener(this);
		jmiAlbumProperties = new JMenuItem(Messages.getString("LogicalTreeView.21")); //$NON-NLS-1$
		jmiAlbumProperties.addActionListener(this);
		jmenuAlbum.add(jmiAlbumPlay);
		jmenuAlbum.add(jmiAlbumPush);
		jmenuAlbum.add(jmiAlbumPlayShuffle);
		jmenuAlbum.add(jmiAlbumPlayRepeat);
		jmenuAlbum.add(jmiAlbumDelete);
		jmenuAlbum.add(jmiAlbumAddFavorite);
		jmenuAlbum.add(jmiAlbumCDDBWizard);
		jmenuAlbum.add(jmiAlbumReport);
		jmenuAlbum.add(jmiAlbumProperties);

		// Year menu
		jmenuYear = new JPopupMenu();
		jmiYearPlay = new JMenuItem(Messages.getString("LogicalTreeView.15")); //$NON-NLS-1$
		jmiYearPlay.addActionListener(this);
		jmiYearPush = new JMenuItem(Messages.getString("LogicalTreeView.16")); //$NON-NLS-1$
		jmiYearPush.addActionListener(this);
		jmiYearPlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.17")); //$NON-NLS-1$
		jmiYearPlayShuffle.addActionListener(this);
		jmiYearPlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.18")); //$NON-NLS-1$
		jmiYearPlayRepeat.addActionListener(this);
		jmiYearDelete = new JMenuItem(Messages.getString("LogicalTreeView.19")); //$NON-NLS-1$
		jmiYearDelete.setEnabled(false);
		jmiYearDelete.addActionListener(this);
		jmiYearAddFavorite = new JMenuItem(Messages.getString("LogicalTreeView.32")); //$NON-NLS-1$        
		jmiYearAddFavorite.addActionListener(this);
		jmiYearProperties = new JMenuItem(Messages.getString("LogicalTreeView.21")); //$NON-NLS-1$
		jmiYearProperties.addActionListener(this);
		jmenuYear.add(jmiYearPlay);
		jmenuYear.add(jmiYearPush);
		jmenuYear.add(jmiYearPlayShuffle);
		jmenuYear.add(jmiYearPlayRepeat);
		jmenuYear.add(jmiYearDelete);
		jmenuYear.add(jmiYearAddFavorite);
		jmenuYear.add(jmiYearProperties);

		// Track menu
		jmenuTrack = new JPopupMenu();
		jmiTrackPlay = new JMenuItem(Messages.getString("LogicalTreeView.22")); //$NON-NLS-1$
		jmiTrackPlay.addActionListener(this);
		jmiTrackPush = new JMenuItem(Messages.getString("LogicalTreeView.23")); //$NON-NLS-1$
		jmiTrackPush.addActionListener(this);
		jmiTrackDelete = new JMenuItem(Messages.getString("LogicalTreeView.24")); //$NON-NLS-1$
		jmiTrackDelete.setEnabled(false);
		jmiTrackDelete.addActionListener(this);
		jmiTrackAddFavorite = new JMenuItem(Messages.getString("LogicalTreeView.32")); //$NON-NLS-1$
		jmiTrackAddFavorite.addActionListener(this);
		jmiTrackProperties = new JMenuItem(Messages.getString("LogicalTreeView.26")); //$NON-NLS-1$
		jmiTrackProperties.addActionListener(this);
		jmenuTrack.add(jmiTrackPlay);
		jmenuTrack.add(jmiTrackPush);
		jmenuTrack.add(jmiTrackDelete);
		jmenuTrack.add(jmiTrackAddFavorite);
		jmenuTrack.add(jmiTrackProperties);

		top = new DefaultMutableTreeNode(Messages.getString("LogicalTreeView.27")); //$NON-NLS-1$

		// Register on the list for subject we are interested in
		ObservationManager.register(this);
		// populate the tree
		switch (iSortOrder) {
		case SORT_BY_STYLE:
			populateTreeByStyle();
			break;
		case SORT_BY_AUTHOR:
			populateTreeByAuthor();
			break;
		case SORT_BY_ALBUM:
			populateTreeByAlbum();
			break;
		case SORT_BY_YEAR:
			populateTreeByYear();
			break;
		}
		// create tree
		createTree();
		jtree.setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
					boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				setFont(new Font("Dialog", Font.PLAIN, ConfigurationManager.getInt(CONF_FONTS_SIZE))); //$NON-NLS-1$
				if (value instanceof StyleNode) {
					setIcon(Util.getIcon(ICON_STYLE));
				} else if (value instanceof AuthorNode) {
					setIcon(Util.getIcon(ICON_AUTHOR));
				} else if (value instanceof YearNode) {
					setIcon(Util.getIcon(ICON_YEAR));
				} else if (value instanceof AlbumNode) {
					setIcon(Util.getIcon(ICON_ALBUM));
				} else if (value instanceof TrackNode) {
					setIcon(Util.getIcon(ICON_TRACK));
					Track track = ((TrackNode) value).getTrack();
					File current = FIFO.getInstance().getCurrentFile();
					if (current != null && track.equals(current.getTrack())) {
						setFont(new Font(
								"Dialog", Font.BOLD, ConfigurationManager.getInt(CONF_FONTS_SIZE))); //$NON-NLS-1$
						setForeground(new Color(200, 70, 10));
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
				node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());

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
				TreePath[] tpSelected = jtree.getSelectionModel().getSelectionPaths();
				if (tpSelected == null) {
					return;
				}
				HashSet<Track> hsSelectedTracks = new HashSet<Track>(100);
				int items = 0;
				// get all components recursively
				alSelected = new ArrayList<Item>(tpSelected.length);
				for (int i = 0; i < tpSelected.length; i++) {
					Object o = tpSelected[i].getLastPathComponent();
					if (o instanceof TransferableTreeNode) {
						alSelected.add((Item) ((TransferableTreeNode) o).getData());
					} else { // collection node
						alSelected = new ArrayList<Item>(TrackManager.getInstance().getTracks());
						items = alSelected.size();
						for (Item item : alSelected) {
							hsSelectedTracks.add((Track) item);
						}
						break;
					}
					// return all child nodes recursively
					Enumeration e2 = ((DefaultMutableTreeNode) o).depthFirstEnumeration();
					while (e2.hasMoreElements()) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) e2.nextElement();
						if (node instanceof TrackNode) {
							Track track = ((TrackNode) node).getTrack();
							if (hsSelectedTracks.contains(track)) {
								// don't count the same track several time if
								// user
								// select directory and then tracks inside
								continue;
							}
							items++;
							hsSelectedTracks.add(track);
						}
					}
				}
				StringBuffer sbOut = new StringBuffer().append(items).append(
						Messages.getString("LogicalTreeView.31")); //$NON-NLS-1$
				InformationJPanel.getInstance().setSelection(sbOut.toString());
				if (ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE)) {
					// if table is synchronized with tree, notify the
					// selection change
					Properties properties = new Properties();
					properties.put(DETAIL_SELECTION, hsSelectedTracks);
					ObservationManager.notify(new Event(EventSubject.EVENT_SYNC_TREE_TABLE,
							properties));
				}
			}
		});
		// Listen for double click
		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
				if (path == null) {
					return;
				}
				if (e.getClickCount() == 2) {
					Object o = path.getLastPathComponent();
					if (o instanceof TrackNode) {
						Track track = ((TrackNode) o).getTrack();
						File file = track.getPlayeableFile(false);
						if (file != null) {
							try {
								FIFO.getInstance().push(
										new StackItem(file, ConfigurationManager
												.getBoolean(CONF_STATE_REPEAT), true),
										ConfigurationManager
												.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
							} catch (JajukException je) {
								Log.error(je);
							}
						} else {
							Messages.showErrorMessage("010", track.getName()); //$NON-NLS-1$
						}
					}
				} else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
					// right clic on a selected node set right clic behavior
					// identical to konqueror tree:
					// if none or 1 node is selected, a right click on
					// another
					// node select it. if more than 1, we keep selection and
					// display a popup for them
					if (jtree.getSelectionCount() < 2) {
						jtree.getSelectionModel().setSelectionPath(path);
					}
					paths = jtree.getSelectionModel().getSelectionPaths();
					getInstance().alTracks = new ArrayList<Track>(100);
					// test mix between types ( not allowed )
					String sClass = paths[0].getLastPathComponent().getClass().toString();
					for (int i = 0; i < paths.length; i++) {
						if (!paths[i].getLastPathComponent().getClass().toString().equals(sClass)) {
							return;
						}
					}
					// get all components recursively
					for (int i = 0; i < paths.length; i++) {
						Object o = paths[i].getLastPathComponent();
						Enumeration e2 = ((DefaultMutableTreeNode) o).depthFirstEnumeration(); // return
						// all
						// childs
						// nodes recursively
						while (e2.hasMoreElements()) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) e2.nextElement();
							if (node instanceof TrackNode) {
								Track track = ((TrackNode) node).getTrack();
								if (track.getPlayeableFile(false) != null) {
									getInstance().alTracks.add(((TrackNode) node).getTrack());
								}
							}
						}
					}
					// display menus according node type
					if (paths[0].getLastPathComponent() instanceof TrackNode) {
						jmenuTrack.show(jtree, e.getX(), e.getY());
					} else if (paths[0].getLastPathComponent() instanceof AlbumNode) {
						jmenuAlbum.show(jtree, e.getX(), e.getY());
					} else if (paths[0].getLastPathComponent() instanceof AuthorNode) {
						jmenuAuthor.show(jtree, e.getX(), e.getY());
					} else if (paths[0].getLastPathComponent() instanceof StyleNode) {
						jmenuStyle.show(jtree, e.getX(), e.getY());
					} else if (paths[0].getLastPathComponent() instanceof DefaultMutableTreeNode) {
						jmenuCollection.show(jtree, e.getX(), e.getY());
					}
				}
			}
		};
		jtree.addMouseListener(ml);
		// Expansion analyze to keep expended state
		jtree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				Object o = event.getPath().getLastPathComponent();
				if (o instanceof StyleNode) {
					Style style = ((StyleNode) o).getStyle();
					style.removeProperty(XML_EXPANDED);
				} else if (o instanceof AuthorNode) {
					Author author = ((AuthorNode) o).getAuthor();
					author.removeProperty(XML_EXPANDED);
				} else if (o instanceof AlbumNode) {
					Album album = ((AlbumNode) o).getAlbum();
					album.removeProperty(XML_EXPANDED);
				}
			}

			public void treeExpanded(TreeExpansionEvent event) {
				Object o = event.getPath().getLastPathComponent();
				if (o instanceof StyleNode) {
					Style style = ((StyleNode) o).getStyle();
					style.setProperty(XML_EXPANDED, true); //$NON-NLS-1$
				} else if (o instanceof AuthorNode) {
					Author author = ((AuthorNode) o).getAuthor();
					author.setProperty(XML_EXPANDED, true); //$NON-NLS-1$
				} else if (o instanceof AlbumNode) {
					Album album = ((AlbumNode) o).getAlbum();
					album.setProperty(XML_EXPANDED, true); //$NON-NLS-1$
				}
			}
		});
		jtree.setAutoscrolls(true);
		// DND support
		new TreeTransferHandler(jtree, DnDConstants.ACTION_COPY_OR_MOVE, true);
		jspTree = new JScrollPane(jtree);
		double[][] dSize = { { TableLayout.FILL },
				{ 5, TableLayout.PREFERRED, 5, TableLayout.FILL } };
		setLayout(new TableLayout(dSize));
		add(jpsort, "0,1"); //$NON-NLS-1$
		add(jspTree, "0,3"); //$NON-NLS-1$
		expand();
	}

	/** Fill the tree */

	public void populateTree() {
		switch (iSortOrder) {
		case SORT_BY_STYLE:
			populateTreeByStyle();
			break;
		case SORT_BY_AUTHOR:
			populateTreeByAuthor();
			break;
		case SORT_BY_ALBUM:
			populateTreeByAlbum();
			break;
		case SORT_BY_YEAR:
			populateTreeByYear();
			break;
		}
	}

	public void populateTreeByStyle() {
		// delete previous tree
		top.removeAllChildren();
		ArrayList<Track> tracks = TrackManager.getInstance().getTracksAsList();
		Collections.sort(tracks, TrackManager.getInstance().getComparator());
		for (Track track : tracks) {
			if (!track.shouldBeHidden()) {
				StyleNode styleNode = null;
				Style style = track.getStyle();
				AuthorNode authorNode = null;
				Author author = track.getAuthor();
				AlbumNode albumNode = null;
				Album album = track.getAlbum();

				// create style
				Enumeration e = top.children();
				boolean b = false;
				while (e.hasMoreElements()) { // check the style doesn't
					// already exist
					StyleNode sn = (StyleNode) e.nextElement();
					if (sn.getStyle().equals(style)) {
						b = true;
						styleNode = sn;
						break;
					}
				}
				if (!b) {
					styleNode = new StyleNode(style);
					top.add(styleNode);
				}
				// create author
				if (styleNode != null) {
					e = styleNode.children();
				} else {
					continue;
				}
				b = false;
				while (e.hasMoreElements()) { // check if the author doesn't
					// already exist
					AuthorNode an = (AuthorNode) e.nextElement();
					if (an.getAuthor().equals(author)) {
						b = true;
						authorNode = an;
						break;
					}
				}
				if (!b) {
					authorNode = new AuthorNode(author);
					styleNode.add(authorNode);
				}
				// create album
				if (authorNode != null) {
					e = authorNode.children();
				} else {
					continue;
				}
				b = false;
				while (e.hasMoreElements()) { // check if the album doesn't
					// already exist
					AlbumNode an = (AlbumNode) e.nextElement();
					if (an.getAlbum().equals(album)) {
						b = true;
						albumNode = an;
						break;
					}
				}
				if (!b) {
					albumNode = new AlbumNode(album);
					authorNode.add(albumNode);
				}
				// create track
				assert albumNode != null;
				albumNode.add(new TrackNode(track));
			}
		}
	}

	/** Fill the tree */
	public void populateTreeByAuthor() {
		// delete previous tree
		top.removeAllChildren();
		ArrayList<Track> tracks = TrackManager.getInstance().getTracksAsList();
		Collections.sort(tracks, TrackManager.getInstance().getComparator());
		for (Track track : tracks) {
			if (!track.shouldBeHidden()) {
				AuthorNode authorNode = null;
				Author author = track.getAuthor();
				AlbumNode albumNode = null;
				Album album = track.getAlbum();

				// create author
				Enumeration e = top.children();
				boolean b = false;
				while (e.hasMoreElements()) { // check if the author doesn't
					// already exist
					AuthorNode an = (AuthorNode) e.nextElement();
					if (an.getAuthor().equals(author)) {
						b = true;
						authorNode = an;
						break;
					}
				}
				if (!b) {
					authorNode = new AuthorNode(author);
					top.add(authorNode);
				}
				// create album
				if (authorNode != null) {
					e = authorNode.children();
				} else {
					continue;
				}
				b = false;
				while (e.hasMoreElements()) { // check if the album doesn't
					// already exist
					AlbumNode an = (AlbumNode) e.nextElement();
					if (an.getAlbum().equals(album)) {
						b = true;
						albumNode = an;
						break;
					}
				}
				if (!b) {
					albumNode = new AlbumNode(album);
					authorNode.add(albumNode);
				}
				// create track
				if (albumNode != null) {
					albumNode.add(new TrackNode(track));
				}
			}
		}

	}

	/** Fill the tree */
	public void populateTreeByYear() {
		// delete previous tree
		top.removeAllChildren();
		ArrayList<Track> tracks = TrackManager.getInstance().getTracksAsList();
		Collections.sort(tracks, TrackManager.getInstance().getComparator());
		for (Track track : tracks) {
			if (!track.shouldBeHidden()) {
				YearNode yearNode = null;
				String year = Long.toString(track.getYear());
				AlbumNode albumNode = null;
				Album album = track.getAlbum();

				// create Year
				Enumeration e = top.children();
				boolean b = false;
				// check if the author doesn't already exist
				while (e.hasMoreElements()) {
					YearNode yn = (YearNode) e.nextElement();
					if (yn.getYear().equals(year)) {
						b = true;
						yearNode = yn;
						break;
					}
				}
				if (!b) {
					yearNode = new YearNode(year);
					top.add(yearNode);
				}
				// create album
				if (yearNode != null) {
					e = yearNode.children();
				} else {
					continue;
				}
				b = false;
				while (e.hasMoreElements()) { // check if the album doesn't
					// already exist
					AlbumNode an = (AlbumNode) e.nextElement();
					if (an.getAlbum().equals(album)) {
						b = true;
						albumNode = an;
						break;
					}
				}
				if (!b) {
					albumNode = new AlbumNode(album);
					yearNode.add(albumNode);
				}
				// create track
				if (albumNode != null) {
					albumNode.add(new TrackNode(track));
				}
			}
		}

	}

	/** Fill the tree */
	public void populateTreeByAlbum() {
		// delete previous tree
		top.removeAllChildren();
		ArrayList<Track> tracks = TrackManager.getInstance().getTracksAsList();
		Collections.sort(tracks, TrackManager.getInstance().getComparator());
		for (Track track : tracks) {
			if (!track.shouldBeHidden()) {
				AlbumNode albumNode = null;
				Album album = track.getAlbum();

				// create album
				Enumeration e = top.children();
				boolean b = false;
				while (e.hasMoreElements()) { // check if the album doesn't
					// already exist
					AlbumNode an = (AlbumNode) e.nextElement();
					if (an.getAlbum().equals(album)) {
						b = true;
						albumNode = an;
						break;
					}
				}
				if (!b) {
					albumNode = new AlbumNode(album);
					top.add(albumNode);
				}
				// create track
				if (albumNode != null) {
					albumNode.add(new TrackNode(track));
				}
			}
		}
	}

	/**
	 * Create a Misc node
	 */
	@SuppressWarnings("unchecked")
	public void cleanTree() {
		AuthorNode amisc = new AuthorNode(AuthorManager.getInstance().registerAuthor("Misc")); //$NON-NLS-1$

		DefaultMutableTreeNode authorNode = new DefaultMutableTreeNode();
		DefaultMutableTreeNode albumNode = new DefaultMutableTreeNode();
		AlbumNode misc;
		Enumeration eAuthor = top.children();

		while (eAuthor.hasMoreElements()) {
			authorNode = (AuthorNode) eAuthor.nextElement();
			misc = new AlbumNode(AlbumManager.getInstance().registerAlbum("Misc")); //$NON-NLS-1$

			for (Enumeration<AlbumNode> eAlbum = authorNode.children(); eAlbum.hasMoreElements();) {
				albumNode = eAlbum.nextElement();
				if (albumNode.getChildCount() < MIN_TRACKS_NUMBER) {
					while (albumNode.getChildCount() > 0) {

					}
				}
			}
			authorNode.remove(albumNode);
			if (misc.getChildCount() > 0) {
				authorNode.add(misc);
			}
			/*
			 * if (authorNode.getChildCount() == 1 &&
			 * authorNode.getNextNode().equals(misc)) { amisc.add(authorNode); }
			 */
		}
		if (amisc.getChildCount() > 0) {
			top.add(amisc);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		new Thread() {
			public void run() {
				if (e.getSource() == jmiStyleProperties) {
					ArrayList<Item> alTracks = new ArrayList<Item>(1000);
					for (Item item : alSelected) {
						Style style = (Style) item;
						alTracks.addAll(style.getTracksRecursively());
					}
					new PropertiesWizard(alSelected, alTracks);
				} else if (e.getSource() == jmiAuthorProperties) {
					ArrayList<Item> alTracks = new ArrayList<Item>(100);
					for (Item item : alSelected) {
						Author author = (Author) item;
						alTracks.addAll(TrackManager.getInstance().getAssociatedTracks(author));
					}
					new PropertiesWizard(alSelected, alTracks);
				} else if (e.getSource() == jmiAlbumProperties) {
					ArrayList<Item> alTracks = new ArrayList<Item>(10);
					for (Item item : alSelected) {
						Album album = (Album) item;
						alTracks.addAll(TrackManager.getInstance().getAssociatedTracks(album));
					}
					new PropertiesWizard(alSelected, alTracks);
				} else if (e.getSource() == jmiTrackProperties) {
					new PropertiesWizard(alSelected);
					// Sorting
				} else if (e.getSource() == jmiAlbumCDDBWizard) {
					ArrayList<Item> alTracks = new ArrayList<Item>(100);
					for (Item item : alSelected) {
						Album album = (Album) item;
						alTracks.addAll(TrackManager.getInstance().getAssociatedTracks(album));
					}
					Util.waiting();
					new CDDBWizard(alTracks);
				} else if (e.getActionCommand().equals(
						EventSubject.EVENT_LOGICAL_TREE_SORT.toString())) {
					Util.waiting();
					iSortOrder = 0;
					if (e.getSource() == jcbSort) {
						iSortOrder = jcbSort.getSelectedIndex();
					} else if (e.getSource() == jmiCollectionStyle) {
						iSortOrder = 0;
					} else if (e.getSource() == jmiCollectionAuthor) {
						iSortOrder = 1;
					} else if (e.getSource() == jmiCollectionAlbum) {
						iSortOrder = 2;
					}
					// make sure to update combo and popup items state if
					// used popup menu
					jcbSort.removeActionListener(LogicalTreeView.this);
					jcbSort.setSelectedIndex(iSortOrder);
					jcbSort.addActionListener(LogicalTreeView.this);
					// popup
					jmiCollectionStyle.removeActionListener(LogicalTreeView.this);
					jmiCollectionAuthor.removeActionListener(LogicalTreeView.this);
					jmiCollectionAlbum.removeActionListener(LogicalTreeView.this);
					switch (iSortOrder) {
					case 0:
						jmiCollectionStyle.setSelected(true);
						break;
					case 1:
						jmiCollectionAuthor.setSelected(true);
						break;
					case 2:
						jmiCollectionAlbum.setSelected(true);
						break;
					}
					jmiCollectionStyle.addActionListener(LogicalTreeView.this);
					jmiCollectionAuthor.addActionListener(LogicalTreeView.this);
					jmiCollectionAlbum.addActionListener(LogicalTreeView.this);

					ConfigurationManager.setProperty(CONF_LOGICAL_TREE_SORT_ORDER, Integer
							.toString(iSortOrder));
					// refresh comparator
					TrackManager.getInstance().setComparator(
							new TrackComparator(ConfigurationManager
									.getInt(CONF_LOGICAL_TREE_SORT_ORDER)));
					populateTree();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							SwingUtilities.updateComponentTreeUI(jtree);
							Util.stopWaiting();
						}
					});
				} else {
					// compute selection
					ArrayList<File> alFilesToPlay = new ArrayList<File>(alTracks.size());
					Iterator it = alTracks.iterator();
					while (it.hasNext()) {
						File file = ((Track) it.next()).getPlayeableFile(false);
						if (file != null) {
							alFilesToPlay.add(file);
						}
					}
					if (alFilesToPlay.size() == 0) {
						Messages.showErrorMessage("018"); //$NON-NLS-1$
						return;
					}
					if ((e.getSource() == jmiTrackPlay || e.getSource() == jmiAlbumPlay
							|| e.getSource() == jmiAuthorPlay || e.getSource() == jmiStylePlay)) {
						FIFO.getInstance().push(
								Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
										ConfigurationManager.getBoolean(CONF_STATE_REPEAT), true),
								false);
					} else if ((e.getSource() == jmiTrackPush || e.getSource() == jmiAlbumPush
							|| e.getSource() == jmiAuthorPush || e.getSource() == jmiStylePush)) {
						FIFO.getInstance().push(
								Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
										ConfigurationManager.getBoolean(CONF_STATE_REPEAT), true),
								true);
					} else if ((e.getSource() == jmiAlbumPlayShuffle
							|| e.getSource() == jmiAuthorPlayShuffle || e.getSource() == jmiStylePlayShuffle)) {
						Collections.shuffle(alFilesToPlay, new Random());
						FIFO.getInstance().push(
								Util.createStackItems(alFilesToPlay, ConfigurationManager
										.getBoolean(CONF_STATE_REPEAT), true), false);
					} else if ((e.getSource() == jmiAlbumPlayRepeat
							|| e.getSource() == jmiAuthorPlayRepeat || e.getSource() == jmiStylePlayRepeat)) {
						FIFO.getInstance().push(
								Util.createStackItems(Util.applyPlayOption(alFilesToPlay), true,
										true), false);
					} else if ((e.getSource() == jmiStyleAddFavorite
							|| e.getSource() == jmiAlbumAddFavorite
							|| e.getSource() == jmiAuthorAddFavorite || e.getSource() == jmiTrackAddFavorite)) {
						Bookmarks.getInstance().addFiles(alFilesToPlay);
					} else if ((e.getSource() == jmiAlbumDelete || e.getSource() == jmiAuthorDelete
							|| e.getSource() == jmiStyleDelete || e.getSource() == jmiTrackDelete)) {
						// TBI
					}
				}
			}
		}.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(Event event) {
		EventSubject subject = event.getSubject();
		if (subject.equals(EventSubject.EVENT_FILE_LAUNCHED)) {
			// used for current track display refresh
			repaint();
		} else if (subject.equals(EventSubject.EVENT_DEVICE_MOUNT)
				|| subject.equals(EventSubject.EVENT_DEVICE_UNMOUNT)) {
			SwingWorker sw = new SwingWorker() {
				public Object construct() {
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
		} else if (subject.equals(EventSubject.EVENT_DEVICE_REFRESH)) {
			SwingWorker sw = new SwingWorker() {
				public Object construct() {
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

	/**
	 * Manages auto-expand
	 * 
	 */
	public void expand() {
		// expand all
		for (int i = 0; i < jtree.getRowCount(); i++) {
			Object o = jtree.getPathForRow(i).getLastPathComponent();
			if (o instanceof StyleNode) {
				Style style = ((StyleNode) o).getStyle();
				boolean bExp = style.getBooleanValue(XML_EXPANDED);
				if (bExp) { //$NON-NLS-1$
					jtree.expandRow(i);
				}
			} else if (o instanceof AuthorNode) {
				Author author = ((AuthorNode) o).getAuthor();
				boolean bExp = author.getBooleanValue(XML_EXPANDED);
				if (bExp) { //$NON-NLS-1$
					jtree.expandRow(i);
				}
			} else if (o instanceof AlbumNode) {
				Album album = ((AlbumNode) o).getAlbum();
				boolean bExp = album.getBooleanValue(XML_EXPANDED);
				if (bExp) { //$NON-NLS-1$
					jtree.expandRow(i);
				}
			}
		}
	}

	/**
	 * @return Returns the alTracks.
	 */
	public ArrayList getTrackSelection() {
		return alTracks;
	}

	/**
	 * 
	 * @return Logical tree sort order.
	 *         <p>
	 *         0: Style
	 *         </p>
	 *         <p>
	 *         1: author
	 *         </p>
	 *         <p>
	 *         2: Album
	 *         <p>
	 *         3: Year
	 *         </p>
	 */
	public int getSortOrder() {
		return iSortOrder;
	}

}

/**
 * Style node
 * 
 * @author Bertrand Florat
 * @created 29 nov. 2003
 */
class StyleNode extends TransferableTreeNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param track
	 */
	public StyleNode(Style track) {
		super(track);
	}

	/**
	 * return a string representation of this track node
	 */
	public String toString() {
		return ((Style) super.getData()).getName2();
	}

	/**
	 * @return Returns the track.
	 */
	public Style getStyle() {
		return (Style) super.getData();
	}
}

/**
 * Author node
 * 
 * @author Bertrand Florat
 * @created 29 nov. 2003
 */
class AuthorNode extends TransferableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param author
	 */
	public AuthorNode(Author author) {
		super(author);
	}

	/**
	 * return a string representation of this author node
	 */
	public String toString() {
		return ((Author) super.getData()).getName2();
	}

	/**
	 * @return Returns the author.
	 */
	public Author getAuthor() {
		return (Author) super.getData();
	}

}

/**
 * Year node
 * 
 * @author Bertrand Florat
 * @created 29 nov. 2003
 */
class YearNode extends TransferableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param author
	 */
	public YearNode(String year) {
		super(year);
	}

	/**
	 * return a string representation of this author node
	 */
	public String toString() {
		return ((String) super.getData());
	}

	/**
	 * @return Returns the year.
	 */
	public String getYear() {
		return (String) super.getData();
	}

}

/**
 * Album node
 * 
 * @author Bertrand Florat
 * @created 29 nov. 2003
 */
class AlbumNode extends TransferableTreeNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param album
	 */
	public AlbumNode(Album album) {
		super(album);
	}

	/**
	 * return a string representation of this album node
	 */
	public String toString() {
		return ((Album) super.getData()).getName2();
	}

	/**
	 * @return Returns the album.
	 */
	public Album getAlbum() {
		return (Album) super.getData();
	}
}

/**
 * Track node
 * 
 * @author Bertrand Florat
 * @created 29 nov. 2003
 */
class TrackNode extends TransferableTreeNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param track
	 */
	public TrackNode(Track track) {
		super(track);
	}

	/**
	 * return a string representation of this track node
	 */
	public String toString() {
		return ((Track) super.getData()).getName();
	}

	/**
	 * @return Returns the track.
	 */
	public Track getTrack() {
		return (Track) super.getData();
	}
}
