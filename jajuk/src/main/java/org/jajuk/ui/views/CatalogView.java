/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import org.jajuk.Main;
import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Directory;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.Item;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.YearManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CatalogViewTransferHandler;
import org.jajuk.ui.DefaultMouseWheelListener;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukButton;
import org.jajuk.ui.SteppedComboBox;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Filter;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;
import org.jvnet.substance.SubstanceLookAndFeel;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import ext.FlowScrollPanel;
import ext.SwingWorker;

/**
 * Catalog view. Displays all default covers by album
 * <p>
 * Catalog perspectives
 */
public class CatalogView extends ViewAdapter implements Observer, ComponentListener, ActionListener {

	private static final long serialVersionUID = 1L;

	// Top control panel
	JPanel jpControlTop;

	JLabel jlSorter;

	SteppedComboBox jcbSorter;

	JLabel jlFilter;

	SteppedComboBox jcbFilter;

	JLabel jlContains;

	JTextField jtfValue;

	JButton jbPrev;

	JButton jbNext;

	SteppedComboBox jcbPage;

	// Bottom control panel
	JPanel jpControlBottom;

	JCheckBox jcbShow;

	JLabel jlSize;

	JSlider jsSize;

	JButton jbRefresh;

	FlowScrollPanel jpItems;

	JScrollPane jsp;

	JPopupMenu jmenu;

	JMenuItem jmiAlbumPlay;

	JMenuItem jmiAlbumPush;

	JMenuItem jmiAlbumPlayShuffle;

	JMenuItem jmiAlbumPlayRepeat;

	JMenuItem jmiGetCovers;

	JMenuItem jmiAlbumProperties;

	/** Filter properties */
	ArrayList<PropertyMetaInformation> alFilters;

	/** Sorter properties */
	ArrayList<PropertyMetaInformation> alSorters;

	/** Items* */
	HashSet<CatalogItem> hsItems;

	/** Do search panel need a search */
	private boolean bNeedSearch = false;

	/** Populating flag */
	private boolean bPopulating = false;

	/** Default time in ms before launching a search automatically */
	private static final int WAIT_TIME = 400;

	/** Date last key pressed */
	private long lDateTyped;

	/** Last selected item */
	public CatalogItem item;

	/** Page index */
	private int page = 0;

	/** Number of page in current selection */
	int iNbPages = 0;

	/** Number of created thumbs, used for garbage collection */
	public int iNbCreatedThumbs = 0;

	/** Utility list used by size selector */
	private ArrayList<String> sizes = new ArrayList<String>(10);

	/** Current details dialog */
	private JDialog details;

	/** Swing Timer to refresh the component */
	private Timer timer = new Timer(WAIT_TIME, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (bNeedSearch && !bPopulating
					&& (System.currentTimeMillis() - lDateTyped >= WAIT_TIME)) {
				// reset paging
				page = 0;
				populateCatalog();
				bNeedSearch = false;
			}
		}
	});

	public CatalogItem getSelectedItem() {
		return item;
	}

	/**
	 * Constructor
	 */
	public CatalogView() {
		alFilters = new ArrayList<PropertyMetaInformation>(10);
		alFilters.add(null); // All
		alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_STYLE));
		alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_AUTHOR));
		alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_ALBUM));
		alFilters.add(TrackManager.getInstance().getMetaInformation(XML_YEAR));

		alSorters = new ArrayList<PropertyMetaInformation>(10);
		alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_STYLE));
		alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_AUTHOR));
		alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_ALBUM));
		alSorters.add(TrackManager.getInstance().getMetaInformation(XML_YEAR));
		alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_ADDED));

		hsItems = new HashSet<CatalogItem>();

		sizes.add(THUMBNAIL_SIZE_50x50);
		sizes.add(THUMBNAIL_SIZE_100x100);
		sizes.add(THUMBNAIL_SIZE_150x150);
		sizes.add(THUMBNAIL_SIZE_200x200);
		sizes.add(THUMBNAIL_SIZE_250x250);
		sizes.add(THUMBNAIL_SIZE_300x300);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#display()
	 */
	public void initUI() {
		// --Top (most used) control items
		jpControlTop = new JPanel();
		// Hide album popups when entering this area
		jpControlTop.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				if (CatalogView.this.details != null) {
					CatalogView.this.details.dispose();
				}
			}

		});
		jpControlTop.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		jlSorter = new JLabel(Messages.getString("Sort"));
		jcbSorter = new SteppedComboBox();
		jcbSorter.setEditable(false);
		// note that a single album can contains tracks with different authors
		// or styles, we will show it only one
		for (PropertyMetaInformation meta : alSorters) {
			jcbSorter.addItem(meta.getHumanName());
		}
		jcbSorter.setSelectedIndex(ConfigurationManager.getInt(CONF_THUMBS_SORTER));
		jcbSorter.addActionListener(this);
		JToolBar jtbSort = new JToolBar();
		jtbSort.setFloatable(false);
		jtbSort.setRollover(true);
		jtbSort.add(jlSorter);
		jtbSort.addSeparator();
		jtbSort.add(jcbSorter);

		jlFilter = new JLabel(Messages.getString("AbstractTableView.0"));
		jlContains = new JLabel(Messages.getString("AbstractTableView.7"));
		jcbFilter = new SteppedComboBox();
		jcbFilter.setEditable(false);
		// note that a single album can contains tracks with different authors
		// or styles, we will show it only one
		for (PropertyMetaInformation meta : alFilters) {
			if (meta == null) { // "any" filter
				jcbFilter.addItem(Messages.getString("AbstractTableView.8"));
			} else {
				jcbFilter.addItem(meta.getHumanName());
			}
		}
		jcbFilter.setSelectedIndex(ConfigurationManager.getInt(CONF_THUMBS_FILTER));
		jcbFilter.addActionListener(this);
		JToolBar jtbFilter = new JToolBar();
		jtbFilter.setFloatable(false);
		jtbFilter.setRollover(true);
		jtbFilter.add(jlFilter);
		jtbFilter.addSeparator();
		jtbFilter.add(jcbFilter);

		jtfValue = new JTextField(10);
		jtfValue.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		jtfValue.setFont(new Font("dialog", Font.BOLD,
				ConfigurationManager.getInt(CONF_FONTS_SIZE) + 6));

		jtfValue.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				bNeedSearch = true;
				lDateTyped = System.currentTimeMillis();
			}
		});

		JToolBar jtbPage = new JToolBar();
		jtbPage.setFloatable(false);
		jtbPage.setRollover(true);
		jbPrev = new JButton(IconLoader.ICON_PREVIOUS);
		jbPrev.setToolTipText(Messages.getString("CatalogView.12"));
		jbPrev.addActionListener(this);
		jbNext = new JButton(IconLoader.ICON_NEXT);
		jbNext.setToolTipText(Messages.getString("CatalogView.13"));
		jbNext.addActionListener(this);
		jcbPage = new SteppedComboBox();
		jcbPage.setToolTipText(Messages.getString("CatalogView.14"));
		jcbPage.addActionListener(this);
		jtbPage.add(jbPrev);
		jtbPage.add(jcbPage);
		jtbPage.add(jbNext);

		double sizeControlTop[][] = {
				{ 10, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
						TableLayout.PREFERRED, 200, 10 }, { 30 } };

		TableLayout layoutTop = new TableLayout(sizeControlTop);
		layoutTop.setHGap(20);
		jpControlTop.setLayout(layoutTop);
		jpControlTop.add(jtbSort, "1,0");
		jpControlTop.add(jtbFilter, "2,0");
		jpControlTop.add(jlContains, "3,0,r,c");
		jpControlTop.add(jtfValue, "4,0,c,c");
		jpControlTop.add(jtbPage, "5,0");

		// --Bottom (less used) items
		jcbShow = new JCheckBox(Messages.getString("CatalogView.2"));
		jcbShow.setSelected(ConfigurationManager.getBoolean(CONF_THUMBS_SHOW_WITHOUT_COVER));
		jcbShow.addActionListener(this);

		JLabel jlSize = new JLabel(Messages.getString("CatalogView.15"));
		jsSize = new JSlider(0, 5);
		jsSize.setMajorTickSpacing(1);
		jsSize.setMinorTickSpacing(1);
		jsSize.setSnapToTicks(true);
		jsSize.setPaintTicks(true);
		jsSize.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		jsSize.addMouseWheelListener(new DefaultMouseWheelListener(jsSize) {

			@Override
			public void mouseWheelMoved(MouseWheelEvent mwe) {
				ChangeListener cl = jsSize.getChangeListeners()[0];
				// Remove the concurrent change listener
				jsSize.removeChangeListener(cl);
				// Leave user didn't release the move yet
				if (jsSize.getValueIsAdjusting()) {
					return;
				}
				super.mouseWheelMoved(mwe);
				// Store size
				ConfigurationManager.setProperty(CONF_THUMBS_SIZE, sizes.get(jsSize.getValue()));
				// display thumbs
				populateCatalog();
				// Add again the change listener
				jsSize.addChangeListener(cl);
			}

		});
		int index = sizes.indexOf(ConfigurationManager.getProperty(CONF_THUMBS_SIZE));
		if (index < 0) {
			index = 2; // 150x150 if a problem occurs
		}
		jsSize.setValue(index);
		// compute size string for slider tooltip
		String sizeToDisplay = "" + (50 + 50 * index) + "x" + "" + (50 + 50 * index);
		jsSize.setToolTipText(Messages.getString("CatalogView.4") + " " + sizeToDisplay);
		jsSize.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				// Leave user didn't release the move yet
				if (jsSize.getValueIsAdjusting()) {
					return;
				}
				// Store size
				ConfigurationManager.setProperty(CONF_THUMBS_SIZE, sizes.get(jsSize.getValue()));
				// display thumbs
				populateCatalog();
				// Adjust tooltips
				// compute size string for slider tooltip
				String size = "" + (50 + 50 * jsSize.getValue()) + "x" + ""
						+ (50 + 50 * jsSize.getValue());
				jsSize.setToolTipText(Messages.getString("CatalogView.4") + " " + size);
			}

		});

		jbRefresh = new JajukButton(Messages.getString("CatalogView.19"), IconLoader.ICON_REFRESH);
		jbRefresh.setToolTipText(Messages.getString("CatalogView.3"));
		jbRefresh.addActionListener(this);
		double p = TableLayout.PREFERRED;

		double sizeControlBottom[][] = { { p, p, p, TableLayout.FILL, 5 }, { p } };
		TableLayout layoutBottom = new TableLayout(sizeControlBottom);
		layoutBottom.setHGap(20);
		jpControlBottom = new JPanel();
		// Hide album popups when entering this area
		jpControlBottom.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				if (CatalogView.this.details != null) {
					CatalogView.this.details.dispose();
				}
			}

		});
		jpControlBottom.setLayout(layoutBottom);
		jpControlBottom.add(jcbShow, "0,0");
		jpControlBottom.add(jlSize, "1,0");
		jpControlBottom.add(jsSize, "2,0,c,c");
		jpControlBottom.add(jbRefresh, "3,0,r,c");

		// Covers
		jpItems = new FlowScrollPanel();
		Dimension dim = new Dimension(getWidth(), getHeight());
		jpItems.setPreferredSize(dim);
		jpItems.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				if (CatalogView.this.details != null) {
					CatalogView.this.details.dispose();
				}
			}

		});

		jsp = new JScrollPane(jpItems, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jpItems.setScroller(jsp);
		jpItems.setLayout(new FlowLayout(FlowLayout.LEFT));
		// Menu items
		// Album menu
		jmenu = new JPopupMenu();
		jmiAlbumPlay = new JMenuItem(Messages.getString("LogicalTreeView.15"));
		jmiAlbumPlay.addActionListener(this);
		jmiAlbumPush = new JMenuItem(Messages.getString("LogicalTreeView.16"));
		jmiAlbumPush.addActionListener(this);
		jmiAlbumPlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.17"));
		jmiAlbumPlayShuffle.addActionListener(this);
		jmiAlbumPlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.18"));
		jmiAlbumPlayRepeat.addActionListener(this);
		jmiGetCovers = new JMenuItem(Messages.getString("CatalogView.7"));
		jmiGetCovers.addActionListener(this);
		jmiAlbumProperties = new JMenuItem(Messages.getString("LogicalTreeView.21"));
		jmiAlbumProperties.addActionListener(this);
		jmenu.add(jmiAlbumPlay);
		jmenu.add(jmiAlbumPush);
		jmenu.add(jmiAlbumPlayShuffle);
		jmenu.add(jmiAlbumPlayRepeat);
		jmenu.add(jmiGetCovers);
		jmenu.add(jmiAlbumProperties);

		// global layout
		double size[][] = { { TableLayout.FILL },
				{ TableLayout.PREFERRED, 5, TableLayout.FILL, 5, TableLayout.PREFERRED, 5 } };
		setLayout(new TableLayout(size));
		add(jpControlTop, "0,0");
		add(jsp, "0,2");
		add(jpControlBottom, "0,4");

		populateCatalog();

		// subscriptions to events
		ObservationManager.register(this);

		// Show facts
		showFacts();

		// Start the timer
		timer.start();

	}

	/**
	 * Show various information in the information panel
	 * 
	 */
	private void showFacts() {
		// display facts in the information panel
		// n albums
		String sMessage = AlbumManager.getInstance().getAlbums().size() + " "
				+ Messages.getString("CatalogView.16");
		int albumsPerPage = ConfigurationManager.getInt(CONF_CATALOG_PAGE_SIZE);
		// n albums / page
		if (albumsPerPage > 0) {
			sMessage += " - " + albumsPerPage + Messages.getString("CatalogView.17");
		}
		InformationJPanel.getInstance().setSelection(sMessage);
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
		eventSubjectSet.add(EventSubject.EVENT_COVER_DEFAULT_CHANGED);
		return eventSubjectSet;
	}

	/**
	 * Populate the catalog
	 */
	private synchronized void populateCatalog() {
		bPopulating = true;
		jsSize.setEnabled(false);
		jcbFilter.setEnabled(false);
		jcbShow.setEnabled(false);
		jcbSorter.setEnabled(false);
		jbPrev.setEnabled(false);
		jbNext.setEnabled(false);
		jcbPage.setEnabled(false);
		SwingWorker sw = new SwingWorker() {
			@Override
			public Object construct() {
				Util.waiting();
				hsItems.clear();
				// remove all devices
				if (jpItems.getComponentCount() > 0) {
					jpItems.removeAll();
				}
				Filter filter = null;
				if (jtfValue.getText().length() > 0) {
					PropertyMetaInformation meta = alFilters.get(jcbFilter.getSelectedIndex());
					filter = new Filter(meta, jtfValue.getText(), true, false);
				}
				ArrayList<Album> albums = null;
				final HashMap<Album, Track> hmAlbumTrack = new HashMap<Album, Track>();
				// filter albums matching tracks
				Collection<Item> alAllTracks = TrackManager.getInstance().getItems(filter);
				albums = new ArrayList<Album>(alAllTracks.size() / 10);
				// keep matching albums
				for (Item item : alAllTracks) {
					Track track = (Track) item;
					Album album = track.getAlbum();
					if (!albums.contains(album)) {
						albums.add(album);
					}
				}
				// Find a matching track for each album and store it for perfs
				Set<Track> tracks = TrackManager.getInstance().getTracks();
				for (Album album : albums) {
					for (Track track : tracks) {
						if (track.getAlbum().equals(album)) {
							hmAlbumTrack.put(album, track);
							break;
						}
						hmAlbumTrack.put(album, null);
					}
				}

				// sort albums
				final int index = jcbSorter.getSelectedIndex();
				Collections.sort(albums, new Comparator<Album>() {

					public int compare(Album album1, Album album2) {
						// for albums, perform a fast compare
						if (index == 2) {
							return album1.compareTo(album2);
						}
						// get a track for each album
						// TODO: get two tracks of album and compare Author, if
						// !=, set Author to "Various Artist"
						Track track1 = hmAlbumTrack.get(album1);
						Track track2 = hmAlbumTrack.get(album2);

						// check tracks (normally useless)
						if (track1 == null || track2 == null) {
							return 0;
						}
						switch (index) {
						case 0: // style
							// Sort on Genre/Author/Year/Title
							if (track1.getStyle() == track2.getStyle()) {
								if (track1.getAuthor() == track2.getAuthor()) {
									if (track1.getYear() == track2.getYear()) {
										return album1.compareTo(album2);
									} else {
										return track1.getYear().compareTo(track2.getYear());
									}
								} else {
									return track1.getAuthor().compareTo(track2.getAuthor());
								}
							} else {
								return track1.getStyle().compareTo(track2.getStyle());
							}
						case 1: // author
							// Sort on Author/Year/Title
							if (track1.getAuthor() == track2.getAuthor()) {
								if (track1.getYear() == track2.getYear()) {
									return album1.compareTo(album2);
								} else {
									return track1.getYear().compareTo(track2.getYear());
								}
							} else {
								return track1.getAuthor().compareTo(track2.getAuthor());
							}
						case 3: // year
							// Sort on: Year/Author/Title
							if (track1.getYear() == track2.getYear()) {
								if (track1.getAuthor() == track2.getAuthor()) {
									return album1.compareTo(album2);
								} else {
									return track2.getAuthor().compareTo(track1.getAuthor());
								}
							} else {
								return track2.getYear().compareTo(track1.getYear());
							}
						case 4: // Discovery date
							return track2.getAdditionDate().compareTo(track1.getAdditionDate());
						}
						return 0;
					}
				});

				// Now process each album
				HashSet<Directory> directories = new HashSet<Directory>(albums.size());
				ArrayList<CatalogItem> alItemsToDisplay = new ArrayList<CatalogItem>(albums.size());
				for (Object item : albums) {
					Album album = (Album) item;
					// if hide unmounted tracks is set, continue
					if (ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)) {
						// test if album contains at least one mounted file
						tracks = TrackManager.getInstance().getAssociatedTracks(album);
						if (tracks.size() > 0) {
							boolean bOK = false;
							for (Track track : tracks) {
								if (track.getReadyFiles().size() > 0) {
									bOK = true;
									break;
								}
							}
							if (!bOK) {
								continue;
							}
						} else {
							continue;
						}
					}
					// Take first track of album (to get detailed
					// information)
					Track anyTrack = hmAlbumTrack.get(album);
					if (anyTrack != null) {
						// Take the directory of any file of the track
						ArrayList<org.jajuk.base.File> fileList = anyTrack.getFiles();
						if (fileList.size() > 0) {
							Directory dir = fileList.get(0).getDirectory();
							// We want to limit duplicate covers, so we
							// display only one album whose
							// files are in a given directory
							if (directories.contains(dir)) {
								continue;
							}
							directories.add(dir);
						}
						CatalogItem cover = new CatalogItem(album, sizes.get(jsSize.getValue()),
								anyTrack);
						alItemsToDisplay.add(cover);
						// stores information on non-null covers
						hsItems.add(cover);
					}
				}
				// computes the number of pages
				int iSize = ConfigurationManager.getInt(CONF_CATALOG_PAGE_SIZE);
				if (iSize == 0) {
					iNbPages = 1;
				} else {
					iNbPages = alItemsToDisplay.size() / iSize
							+ ((alItemsToDisplay.size() % iSize == 0) ? 0 : 1);
				}
				// add one page for trailing items
				// populate page selector
				jcbPage.removeActionListener(CatalogView.this); // remove action
				// listener
				jcbPage.removeAllItems(); // void it
				for (int i = 0; i < iNbPages; i++) { // add the pages
					jcbPage.addItem(Messages.getString("CatalogView.11") + " " + (i + 1) + "/"
							+ iNbPages);
					// start at page 1, not 0
				}
				if (iNbPages > 0) {
					jcbPage.setSelectedIndex(page);
					jcbPage.addActionListener(CatalogView.this);
					// Add all items
					int max = alItemsToDisplay.size(); // upper limit
					if (page < (iNbPages - 1)) {
						// if last page, take simply to total number of
						// items to display
						max = (page + 1) * ConfigurationManager.getInt(CONF_CATALOG_PAGE_SIZE);
					}
					for (int i = page * ConfigurationManager.getInt(CONF_CATALOG_PAGE_SIZE); i < max; i++) {
						CatalogItem item = alItemsToDisplay.get(i);
						// populate item (construct UI) only when needed
						item.populate();
						if (!item.isNoCover() || (item.isNoCover() && jcbShow.isSelected())) {
							jpItems.add(item);
						}
					}
				}
				Util.stopWaiting();
				return null;
			}

			@Override
			public void finished() {
				jsp.revalidate();
				jsp.repaint();
				jtfValue.requestFocusInWindow();
				jsSize.setEnabled(true);
				jcbFilter.setEnabled(true);
				jcbShow.setEnabled(true);
				jcbSorter.setEnabled(true);
				jbPrev.setEnabled(true);
				jbNext.setEnabled(true);
				jcbPage.setEnabled(true);
				bPopulating = false;
			}
		};
		sw.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(Event event) {
		if (EventSubject.EVENT_DEVICE_REFRESH.equals(event.getSubject())
				|| EventSubject.EVENT_COVER_DEFAULT_CHANGED.equals(event.getSubject())) {
			// save selected item
			CatalogItem oldItem = CatalogView.this.item;
			// reset paging
			page = 0;
			populateCatalog();
			// try to restore previous item
			if (oldItem != null) {
				for (CatalogItem item : hsItems) {
					if (item.getAlbum().equals(oldItem.getAlbum())) {
						CatalogView.this.item = item;
						CatalogView.this.item.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2,
								Color.RED));
						break;
					}
				}
			}
		}
		// In all cases, update the facts
		showFacts();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("CatalogView.0");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == jcbFilter) {
			if (jtfValue.getText().trim().equals("")) { // no need to refresh
				// 
				return;
			}
			bNeedSearch = true;
			lDateTyped = System.currentTimeMillis();
			ConfigurationManager.setProperty(CONF_THUMBS_FILTER, Integer.toString(jcbFilter
					.getSelectedIndex()));
		} else if (e.getSource() == jcbSorter) {
			bNeedSearch = true;
			lDateTyped = System.currentTimeMillis();
			ConfigurationManager.setProperty(CONF_THUMBS_SORTER, Integer.toString(jcbSorter
					.getSelectedIndex()));
		} else if (e.getSource() == jbRefresh) {
			cleanThumbs(THUMBNAIL_SIZE_50x50);
			cleanThumbs(THUMBNAIL_SIZE_100x100);
			cleanThumbs(THUMBNAIL_SIZE_150x150);
			cleanThumbs(THUMBNAIL_SIZE_200x200);
			cleanThumbs(THUMBNAIL_SIZE_250x250);
			cleanThumbs(THUMBNAIL_SIZE_300x300);
			// display thumbs
			populateCatalog();
		} else if (e.getSource() == jcbShow) {
			ConfigurationManager.setProperty(CONF_THUMBS_SHOW_WITHOUT_COVER, Boolean
					.toString(jcbShow.isSelected()));
			// display thumbs
			populateCatalog();
		} else if (e.getSource() == jbPrev) {
			if (page > 0) {
				page--;
			} else {
				page = iNbPages - 1; // go to last
			}
			populateCatalog();
		} else if (e.getSource() == jbNext) {
			if (page < (iNbPages - 1)) {
				page++;
			} else {
				page = 0; // go to first
			}
			populateCatalog();
		} else if (e.getSource() == jcbPage) {
			page = jcbPage.getSelectedIndex();
			populateCatalog();
		} else {
			this.item.actionPerformed(e);
		}
	}

	private void cleanThumbs(String size) {
		File fThumb = Util.getConfFileByPath(FILE_THUMBS + '/' + size);
		if (fThumb.exists()) {
			File[] files = fThumb.listFiles();
			for (File file : files) {
				if (!file.getAbsolutePath().matches(".*" + FILE_THUMB_NO_COVER)) {
					file.delete();
				}
			}
			// Refresh default cover
			File fDefault = Util.getConfFileByPath(FILE_THUMBS + "/" + size + "/"
					+ FILE_THUMB_NO_COVER);
			fDefault.delete();
			try {
				int iSize = Integer.parseInt(new StringTokenizer(size, "x").nextToken());
				Util.createThumbnail(IconLoader.ICON_NO_COVER, fDefault, iSize);
			} catch (Exception e) {
				Log.error(e);
			}
		}
	}

	/**
	 * @return current thumbs size as selected with the combo
	 */
	private int getSelectedSize() {
		return 50 + (50 * jsSize.getValue());
	}

	public class CatalogItem extends JPanel implements ITechnicalStrings, ActionListener,
			Transferable {

		private static final long serialVersionUID = 1L;

		/** Associated album */
		Album album;

		/** Associated track */
		Track track;

		/** Size */
		String size;

		/** Associated file */
		File fCover;

		/** No cover flag */
		boolean bNoCover = false;

		JPanel jpIcon;

		JLabel jlIcon;

		JTextArea jlAuthor;

		JTextArea jlAlbum;

		/** No covers image cache */
		private HashMap<String, ImageIcon> noCoversCache = new HashMap<String, ImageIcon>(10);

		/** Draging flag used to disable simple click behavior */
		private boolean bDragging = false;

		/**
		 * Constructor
		 * 
		 * @param album :
		 *            associated album
		 * @param size :
		 *            size of the thumbnail
		 * @param track:
		 *            any track of the album used to get author.. information
		 *            (perfs)
		 */
		public CatalogItem(Album album, String size, Track track) {
			this.album = album;
			this.size = size;
			this.track = track;
			this.fCover = Util.getConfFileByPath(FILE_THUMBS + '/' + size + '/' + album.getId()
					+ '.' + EXT_THUMB);
		}

		void populate() {
			// create the thumbnail if it doesn't exist
			Util.refreshThumbnail(album, sizes.get(jsSize.getValue()));
			if (!fCover.exists() || fCover.length() == 0) {
				bNoCover = true;
				this.fCover = Util.getConfFileByPath(FILE_THUMBS + '/' + size + '/'
						+ FILE_THUMB_NO_COVER);
			}
			double[][] dMain = { { TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL },
					{ getSelectedSize() + 10, 10, TableLayout.PREFERRED, 5, TableLayout.PREFERRED } };
			setLayout(new TableLayout(dMain));
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			jpIcon = new JPanel();
			jlIcon = new JLabel();
			jlIcon.setBorder(Util.getShadowBorder());
			ImageIcon ii = null;
			String sPath = fCover.getAbsolutePath();
			if (noCoversCache.containsKey(sPath)) {
				ii = noCoversCache.get(sPath);
			} else {
				ii = new ImageIcon(sPath);
				noCoversCache.put(sPath, ii);
			}
			if (!bNoCover) { // avoid flushing no cover thumb: it blinks
				ii.getImage().flush(); // flush image buffer to avoid jre to
				// use old image
			}
			jlIcon.setIcon(ii);
			jpIcon.add(jlIcon, "1,0");
			int iRows = 9 + 3 * (jsSize.getValue());
			Font customFont = new Font("verdana", Font.BOLD, ConfigurationManager
					.getInt(CONF_FONTS_SIZE));
			Color mediumGray = new Color(172, 172, 172);

			// take first track author as author
			jlAuthor = new JTextArea(track.getAuthor().getName2(), 1, iRows);
			jlAuthor.setLineWrap(true);
			jlAuthor.setWrapStyleWord(true);
			jlAuthor.setEditable(false);
			jlAuthor.setFont(customFont);
			jlAuthor.setForeground(mediumGray);
			jlAuthor.setBorder(null);

			jlAlbum = new JTextArea(album.getName2(), 1, iRows);
			jlAlbum.setLineWrap(true);
			jlAlbum.setWrapStyleWord(true);
			jlAlbum.setEditable(false);
			jlAuthor.setFont(new Font("Dialog", Font.BOLD, ConfigurationManager
					.getInt(CONF_FONTS_SIZE)));
			jlAlbum.setFont(new Font("Dialog", Font.BOLD, ConfigurationManager
					.getInt(CONF_FONTS_SIZE)));
			jlAlbum.setFont(customFont);
			jlAlbum.setForeground(mediumGray);
			jlAlbum.setBorder(null);

			add(jpIcon, "1,0");
			add(jlAuthor, "1,2");
			add(jlAlbum, "1,4");
			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			// Add dnd support
			jlIcon.setTransferHandler(new CatalogViewTransferHandler(this));

			jlIcon.addMouseMotionListener(new MouseMotionAdapter() {
			
				public void mouseDragged(MouseEvent e) {
					try {
						System.out.println("here");
						//Notify the mouse listener that we are dragging
						bDragging = true;
						JComponent c = (JComponent) e.getSource();
						TransferHandler handler = c.getTransferHandler();
						handler.exportAsDrag(c, e, TransferHandler.COPY);
					} finally {
						bDragging = false;
					}
				}
			
			});
			
			jlIcon.addMouseListener(new MouseAdapter() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					//Leave if already dragging
					if (bDragging){
						return;
					}
					// remove red border on previous item if different from this
					// one
					if (CatalogView.this.item != null && CatalogView.this.item != CatalogItem.this) {
						CatalogView.this.item
								.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
					}
					// add a red border on this item
					setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
					// Right click
					if (e.getButton() == MouseEvent.BUTTON1
							&& e.getSource() == CatalogItem.this.jlIcon) {
						// if second click (item already selected), play
						if (CatalogView.this.item == CatalogItem.this) {
							play(false, false, false);
						}
						CatalogView.this.item = CatalogItem.this;
						// Left click
					} else if (e.getButton() == MouseEvent.BUTTON3
							&& e.getSource() == CatalogItem.this.jlIcon) {
						CatalogView.this.item = CatalogItem.this;
						// Show contextual menu
						jmenu.show(jlIcon, e.getX(), e.getY());
						// Hide any details frame
						if (CatalogView.this.details != null) {
							CatalogView.this.details.dispose();
						}
					}
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					super.mouseEntered(e);
					// Leave if user unselected the option "Show catalog popups"
					if (!ConfigurationManager.getBoolean(CONF_CATALOG_SHOW_POPUPS)) {
						return;
					}
					// don't show details if the contextual popup menu is
					// visible
					if (jmenu.isVisible()) {
						return;
					}
					// Hide any older details frame
					if (CatalogView.this.details != null) {
						CatalogView.this.details.dispose();
					}
					JDialog dialog = new JDialog();
					dialog.setUndecorated(true);
					dialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
					JXPanel jp = new JXPanel();
					jp.setAlpha(0.8f);
					double[][] size = { { TableLayout.FILL }, { TableLayout.FILL } };
					jp.setLayout(new TableLayout(size));
					final JEditorPane text = new JEditorPane("text/html", track.getAlbum()
							.getAdvancedDescription());
					text.setEditable(false);
					text.setBackground(SubstanceLookAndFeel.getActiveColorScheme().getUltraLightColor());
					text.addHyperlinkListener(new HyperlinkListener() {
						public void hyperlinkUpdate(HyperlinkEvent e) {
							if (e.getEventType() == EventType.ACTIVATED) {
								URL url = e.getURL();
								if (XML_AUTHOR.equals(url.getHost())) {
									ArrayList<Item> items = new ArrayList<Item>(1);
									items.add(AuthorManager.getInstance().getItemByID(
											url.getQuery()));
									new PropertiesWizard(items);
								} else if (XML_STYLE.equals(url.getHost())) {
									ArrayList<Item> items = new ArrayList<Item>(1);
									items.add(StyleManager.getInstance()
											.getItemByID(url.getQuery()));
									new PropertiesWizard(items);
								} else if (XML_YEAR.equals(url.getHost())) {
									ArrayList<Item> items = new ArrayList<Item>(1);
									items
											.add(YearManager.getInstance().getItemByID(
													url.getQuery()));
									new PropertiesWizard(items);
								} else if (XML_TRACK.equals(url.getHost())) {
									ArrayList<Item> items = new ArrayList<Item>(1);
									Track track = (Track) TrackManager.getInstance().getItemByID(
											url.getQuery());
									items.add(track);
									ArrayList<org.jajuk.base.File> toPlay = new ArrayList<org.jajuk.base.File>(
											1);
									toPlay.add(track.getPlayeableFile(true));
									FIFO.getInstance().push(
											Util.createStackItems(Util.applyPlayOption(toPlay),
													ConfigurationManager
															.getBoolean(CONF_STATE_REPEAT), true),
											false);
								}
							}
							// change cursor on entering or leaving hyperlinks
							// This doesn't work under JRE 1.5 (at least under
							// Linux), Sun issue ?
							else if (e.getEventType() == EventType.ENTERED) {
								text.setCursor(Util.LINK_CURSOR);
							} else if (e.getEventType() == EventType.EXITED) {
								text.setCursor(Util.DEFAULT_CURSOR);
							}
						}
					});
					final JScrollPane jspText = new JScrollPane(text);
					jspText.getVerticalScrollBar().setValue(0);
					jp.add(jspText, "0,0");
					dialog.setContentPane(jp);
					// compute dialog position ( note that setRelativeTo is
					// buggy and that we need more advanced positioning)
					int x = (int) jlIcon.getLocationOnScreen().getX()
							+ (int) (0.6 * jlIcon.getWidth());
					// set position at 60 % of the picture
					int y = (int) jlIcon.getLocationOnScreen().getY()
							+ (int) (0.6 * jlIcon.getHeight());
					int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
					int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize()
							.getHeight();
					// Adjust position if details are located outside the screen
					// in x-axis
					if ((x + 500) > screenWidth) {
						x = screenWidth - 510;
					}
					if ((y + 400) > screenHeight) {
						x = (int) jlIcon.getLocationOnScreen().getX()
								+ (int) (0.6 * jlIcon.getWidth());
						if ((x + 500) > screenWidth) {
							x = screenWidth - 510;
						}
						y = (int) jlIcon.getLocationOnScreen().getY()
								+ (int) (0.4 * jlIcon.getHeight()) - 400;
					}
					dialog.setLocation(x, y);
					dialog.setSize(500, 400);
					dialog.setVisible(true);
					CatalogView.this.details = dialog;
					// Force scrollbar to stay on top
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							jspText.getVerticalScrollBar().setValue(0);
						}
					});
				}
			});
		}

		public boolean isNoCover() {
			return bNoCover;
		}

		private void play(boolean bRepeat, boolean bShuffle, boolean bPush) {
			Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album);
			// compute selection
			ArrayList<org.jajuk.base.File> alFilesToPlay = new ArrayList<org.jajuk.base.File>(
					tracks.size());
			Iterator it = tracks.iterator();
			while (it.hasNext()) {
				org.jajuk.base.File file = ((Track) it.next()).getPlayeableFile(false);
				if (file != null) {
					alFilesToPlay.add(file);
				}
			}
			if (bShuffle) {
				Collections.shuffle(alFilesToPlay, new Random());
			}
			FIFO.getInstance().push(Util.createStackItems(alFilesToPlay, bRepeat, true), bPush);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			// Menu items
			if (e.getSource() == jmiAlbumPlay) {
				play(false, false, false);
			} else if (e.getSource() == jmiAlbumPlayRepeat) {
				play(true, false, false);
			} else if (e.getSource() == jmiAlbumPlayShuffle) {
				play(false, true, false);
			} else if (e.getSource() == jmiAlbumPush) {
				play(false, false, true);
			} else if (e.getSource() == jmiGetCovers) {
				new Thread() {
					public void run() {
						JDialog jd = new JDialog(Main.getWindow(), Messages
								.getString("CatalogView.18"));
						org.jajuk.base.File file = null;
						Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album);
						if (tracks.size() > 0) {
							Track track = tracks.iterator().next();
							file = track.getPlayeableFile(false);
						}
						CoverView cv = null;
						if (file != null) {
							cv = new CoverView(file);
							cv.setID("catalog/0");
							cv.initUI();
							jd.add(cv);
							jd.setAlwaysOnTop(true);
							jd.setSize(400, 450);
							jd.setLocationRelativeTo(null);
							jd.setVisible(true);
						} else {
							Messages.showErrorMessage("166");
						}
					}
				}.start();
			} else if (e.getSource() == jmiAlbumProperties) {
				ArrayList<Item> alAlbums = new ArrayList<Item>();
				alAlbums.add(album);
				// Show tracks infos to allow user to change year, rate...
				ArrayList<Item> alTracks = new ArrayList<Item>(TrackManager.getInstance()
						.getAssociatedTracks(album));
				new PropertiesWizard(alAlbums, alTracks);
			}
		}

		public File getCoverFile() {
			return fCover;
		}

		public void setIcon(ImageIcon icon) {
			jlIcon.setIcon(icon);
			// !!! need to flush image because thy read image from a file
			// with same name
			// than previous image and a buffer would display the old image
			icon.getImage().flush();
		}

		/**
		 * @return the album
		 */
		public Album getAlbum() {
			return album;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
		 */
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,
				IOException {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
		 */
		public DataFlavor[] getTransferDataFlavors() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
		 */
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return false;
		}

	}

}
