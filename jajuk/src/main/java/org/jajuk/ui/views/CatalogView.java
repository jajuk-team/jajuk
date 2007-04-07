/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jajuk.Main;
import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Directory;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.Item;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.DefaultMouseWheelListener;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukButton;
import org.jajuk.ui.SteppedComboBox;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Filter;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import ext.FlowScrollPanel;
import ext.SwingWorker;

/**
 * Catalog view. Displays all default covers by album
 * <p>
 * Catalog perspectives
 * 
 * @author Bertrand Florat
 * @created 01/12/2005
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

	/** Default time in ms before launching a search automaticaly */
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

	/**
	 * Constructor
	 */
	public CatalogView() {
		alFilters = new ArrayList<PropertyMetaInformation>(10);
		alFilters.add(null); // All
		alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_STYLE));
		alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_AUTHOR));
		alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_ALBUM));
		alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_YEAR));
		
		alSorters = new ArrayList<PropertyMetaInformation>(10);
		alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_STYLE));
		alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_AUTHOR));
		alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_ALBUM));
		alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_YEAR));
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
		jpControlTop.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		jpControlTop.setOpaque(false);
		jlSorter = new JLabel(Messages.getString("Sort")); //$NON-NLS-1$
		jcbSorter = new SteppedComboBox();
		jcbSorter.setBorder(Util.getShadowBorder());
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

		jlFilter = new JLabel(Messages.getString("AbstractTableView.0")); //$NON-NLS-1$
		jlContains = new JLabel(Messages.getString("AbstractTableView.7")); //$NON-NLS-1$
		jcbFilter = new SteppedComboBox();
		jcbFilter.setBorder(Util.getShadowBorder());
		jcbFilter.setEditable(false);
		// note that a single album can contains tracks with different authors
		// or styles, we will show it only one
		for (PropertyMetaInformation meta : alFilters) {
			if (meta == null) { // "any" filter
				jcbFilter.addItem(Messages.getString("AbstractTableView.8")); //$NON-NLS-1$
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
		jtfValue.setFont(new Font(
				"dialog", Font.BOLD, ConfigurationManager.getInt(CONF_FONTS_SIZE) + 6)); //$NON-NLS-1$
		Color mediumGray = new Color(172, 172, 172);
		jtfValue.setForeground(mediumGray);

		jtfValue.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				bNeedSearch = true;
				lDateTyped = System.currentTimeMillis();
			}
		});

		JToolBar jtbPage = new JToolBar();
		jtbPage.setFloatable(false);
		jtbPage.setRollover(true);
		jbPrev = new JButton(Util.getIcon(ICON_PREVIOUS));
		jbPrev.setToolTipText(Messages.getString("CatalogView.12"));
		jbPrev.addActionListener(this);
		jbNext = new JButton(Util.getIcon(ICON_NEXT));
		jbNext.setToolTipText(Messages.getString("CatalogView.13"));
		jbNext.addActionListener(this);
		jcbPage = new SteppedComboBox();
		jcbPage.setBorder(Util.getShadowBorder());
		jcbPage.setToolTipText(Messages.getString("CatalogView.14"));
		jcbPage.addActionListener(this);
		jtbPage.add(jbPrev);
		jtbPage.add(jcbPage);
		jtbPage.add(jbNext);

		double sizeControlTop[][] = {
				{ 10, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
						TableLayout.PREFERRED, TableLayout.PREFERRED, 10 }, { 30 } };

		TableLayout layoutTop = new TableLayout(sizeControlTop);
		layoutTop.setHGap(20);
		jpControlTop.setLayout(layoutTop);
		jpControlTop.add(jtbSort, "1,0");//$NON-NLS-1$
		jpControlTop.add(jtbFilter, "2,0");//$NON-NLS-1$
		jpControlTop.add(jlContains, "3,0");//$NON-NLS-1$
		jpControlTop.add(jtfValue, "4,0");//$NON-NLS-1$
		jpControlTop.add(jtbPage, "5,0");//$NON-NLS-1$

		// --Bottom (less used) items
		jcbShow = new JCheckBox(Messages.getString("CatalogView.2")); //$NON-NLS-1$
		jcbShow.setSelected(ConfigurationManager.getBoolean(CONF_THUMBS_SHOW_WITHOUT_COVER));
		jcbShow.addActionListener(this);

		JLabel jlSize = new JLabel(Messages.getString("CatalogView.15"));
		jsSize = new JSlider(0, 5);
		jsSize.setOpaque(false);
		jsSize.setMajorTickSpacing(1);
		jsSize.setMinorTickSpacing(1);
		jsSize.setSnapToTicks(true);
		jsSize.setPaintTicks(true);
		jsSize.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		jsSize.setToolTipText(Messages.getString("CatalogView.4")); //$NON-NLS-1$)
		jsSize.addMouseWheelListener(new DefaultMouseWheelListener(jsSize) {

			@Override
			public void mouseWheelMoved(MouseWheelEvent mwe) {
				ChangeListener cl = jsSize.getChangeListeners()[0];
				//Remove the concurrent change listener
				jsSize.removeChangeListener(cl);
				//Leave user didn't release the move yet
				if (jsSize.getValueIsAdjusting()){
					return;
				}
				super.mouseWheelMoved(mwe);
				//Store size
				ConfigurationManager.setProperty(CONF_THUMBS_SIZE, sizes.get(jsSize.getValue()));
				// display thumbs
				populateCatalog();
				//Add again the change listener
				jsSize.addChangeListener(cl);
			}

		});
		int index = sizes.indexOf(ConfigurationManager.getProperty(CONF_THUMBS_SIZE));
		if (index < 0){
			index = 2; //150x150 if a problem occurs
		}
		jsSize.setValue(index);
		jsSize.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				//Leave user didn't release the move yet
				if (jsSize.getValueIsAdjusting()){
					return;
				}
				//Store size
				ConfigurationManager.setProperty(CONF_THUMBS_SIZE, sizes.get(jsSize.getValue()));
				// display thumbs
				populateCatalog();
			}

		});
		JToolBar jtbSize = new JToolBar();
		jtbSize.setRollover(false);
		jtbSize.setFloatable(false);
		jtbSize.add(jlSize);
		jtbSize.addSeparator();
		jtbSize.add(new JLabel(Util.getIcon(ICON_REMOVE)));
		jtbSize.add(jsSize);
		jtbSize.add(new JLabel(Util.getIcon(ICON_ADD)));
		
		//create a toolbar only for the refresh button to allow rollover feature
		JToolBar jtRefresh = new JToolBar();
		jtRefresh.setRollover(true);
		jbRefresh = new JajukButton(Util.getIcon(ICON_REFRESH));
		jbRefresh.setToolTipText(Messages.getString("CatalogView.3")); //$NON-NLS-1$
		jtRefresh.setBorder(null);
		jtRefresh.setRollover(true);
		jtRefresh.setFloatable(false);
		jbRefresh.addActionListener(this);
		jtRefresh.add(jbRefresh);

		double sizeControlBottom[][] = {
				{ TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 10 },
				{ TableLayout.PREFERRED } };
		TableLayout layoutBottom = new TableLayout(sizeControlBottom);
		layoutBottom.setHGap(20);
		jpControlBottom = new JPanel();
		jpControlBottom.setLayout(layoutBottom);
		jpControlBottom.add(jcbShow, "0,0");//$NON-NLS-1$
		jpControlBottom.add(jtbSize, "1,0");//$NON-NLS-1$
		jpControlBottom.add(jtRefresh, "2,0");//$NON-NLS-1$

		// Covers
		jpItems = new FlowScrollPanel();
		Dimension dim = new Dimension(getWidth(), getHeight());
		jpItems.setPreferredSize(dim);
		jsp = new JScrollPane(jpItems, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setOpaque(false);
		jsp.getViewport().setOpaque(false);
		jpItems.setScroller(jsp);
		jpItems.setLayout(new FlowLayout(FlowLayout.LEFT));
		jpItems.setOpaque(false);
		// Menu items
		// Album menu
		jmenu = new JPopupMenu();
		jmiAlbumPlay = new JMenuItem(Messages.getString("LogicalTreeView.15")); //$NON-NLS-1$
		jmiAlbumPlay.addActionListener(this);
		jmiAlbumPush = new JMenuItem(Messages.getString("LogicalTreeView.16")); //$NON-NLS-1$
		jmiAlbumPush.addActionListener(this);
		jmiAlbumPlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.17")); //$NON-NLS-1$
		jmiAlbumPlayShuffle.addActionListener(this);
		jmiAlbumPlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.18")); //$NON-NLS-1$
		jmiAlbumPlayRepeat.addActionListener(this);
		jmiGetCovers = new JMenuItem(Messages.getString("CatalogView.7")); //$NON-NLS-1$        
		jmiGetCovers.addActionListener(this);
		jmiAlbumProperties = new JMenuItem(Messages.getString("LogicalTreeView.21")); //$NON-NLS-1$
		jmiAlbumProperties.addActionListener(this);
		jmenu.add(jmiAlbumPlay);
		jmenu.add(jmiAlbumPush);
		jmenu.add(jmiAlbumPlayShuffle);
		jmenu.add(jmiAlbumPlayRepeat);
		jmenu.add(jmiGetCovers);
		jmenu.add(jmiAlbumProperties);

		// global layout
		double size[][] = { { TableLayout.FILL },
				{ TableLayout.PREFERRED, 5, TableLayout.FILL, 5, TableLayout.PREFERRED } };
		setLayout(new TableLayout(size));
		add(jpControlTop, "0,0"); //$NON-NLS-1$
		add(jsp, "0,2"); //$NON-NLS-1$
		add(jpControlBottom, "0,4"); //$NON-NLS-1$

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
	 * Make thumbnail file exists (album id.jpg or.gif or .png) in thumbs
	 * directory if it doesn't exist yet
	 * 
	 * @param album
	 * @return whether a new cover has been created
	 */
	private boolean refreshThumbnail(Album album) {
		File fThumb = Util.getConfFileByPath(FILE_THUMBS + '/' + sizes.get(jsSize.getValue())
				+ '/' + album.getId() + '.' + EXT_THUMB);
		File fCover = null;
		if (!fThumb.exists()) {
			// search for local covers in all directories mapping the
			// current track to reach other
			// devices covers and display them together
			Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album);
			if (tracks.size() == 0) {
				return false;
			}
			// take first track found to get associated directories as we
			// assume all tracks for an album are in the same directory
			Track trackCurrent = tracks.iterator().next();
			fCover = trackCurrent.getAlbum().getCoverFile();
			if (fCover == null) {
				try {
					// use void file to store the fact we didn't find a
					// cover, too long to scan again
					fThumb.createNewFile();
				} catch (Exception e) {
					Log.error(e);
				}
			} else {
				try {
					Util.createThumbnail(fCover, fThumb, getSelectedSize());
					InformationJPanel.getInstance().setMessage(Messages.getString("CatalogView.5") //$NON-NLS-1$
							+ ' ' + album.getName2(), InformationJPanel.INFORMATIVE);
					return true;
				} catch (Exception e) {
					// create a void thumb to avoid trying to create again
					// this thumb
					try {
						fThumb.createNewFile();
					} catch (IOException e1) {
						Log.error(e1);
					}
					Log.error(e);
				}
			}
		}
		return false; // thumb already exist

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
										return (int) (track1.getYear() - track2.getYear());
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
									return (int) (track1.getYear() - track2.getYear());
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
									return track1.getAuthor().compareTo(track2.getAuthor());
								}
							} else {
								return (int) (track1.getYear() - track2.getYear());
							}
						case 4: // Discovery date
							return track1.getAdditionDate().compareTo(track2.getAdditionDate());
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
						CatalogItem cover = new CatalogItem(album, sizes.get(jsSize.getValue())
								, anyTrack);
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
		return Messages.getString("CatalogView.0"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == jcbFilter) {
			if (jtfValue.getText().trim().equals("")) { // no need to refresh
				// //$NON-NLS-1$
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
			cleanThumbs(THUMBNAIL_SIZE_50x50); //$NON-NLS-1$
			cleanThumbs(THUMBNAIL_SIZE_100x100); //$NON-NLS-1$
			cleanThumbs(THUMBNAIL_SIZE_150x150); //$NON-NLS-1$
			cleanThumbs(THUMBNAIL_SIZE_200x200); //$NON-NLS-1$
			cleanThumbs(THUMBNAIL_SIZE_250x250); //$NON-NLS-1$
			cleanThumbs(THUMBNAIL_SIZE_300x300); //$NON-NLS-1$
			// display thumbs
			populateCatalog();
		} else if (e.getSource() == jcbShow) {
			ConfigurationManager.setProperty(CONF_THUMBS_SHOW_WITHOUT_COVER, Boolean
					.toString(jcbShow.isSelected()));
			// display thumbs
			populateCatalog();
		}
		 else if (e.getSource() == jbPrev) {
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
				if (!file.getAbsolutePath().matches(".*" + FILE_THUMB_NO_COVER)) { //$NON-NLS-1$
					file.delete();
				}
			}
			// Refresh default cover
			File fDefault = Util.getConfFileByPath(FILE_THUMBS
					+ "/" + size + "/" + FILE_THUMB_NO_COVER); //$NON-NLS-1$ //$NON-NLS-2$
			fDefault.delete();
			try {
				int iSize = Integer.parseInt(new StringTokenizer(size, "x").nextToken()); //$NON-NLS-1$
				Util.createThumbnail(Util.getIcon(IMAGE_NO_COVER), fDefault, iSize);
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

	class CatalogItem extends JPanel implements ITechnicalStrings, ActionListener, MouseListener {

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
			refreshThumbnail(album);
			if (!fCover.exists() || fCover.length() == 0) {
				bNoCover = true;
				this.fCover = Util.getConfFileByPath(FILE_THUMBS + '/' + size + '/'
						+ FILE_THUMB_NO_COVER);
			}
			double[][] dMain = { { TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL },
					{ getSelectedSize()+10, 10, TableLayout.PREFERRED, 5, TableLayout.PREFERRED } };
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
			jpIcon.setOpaque(false);
			addMouseListener(this);
			jpIcon.add(jlIcon, "1,0"); //$NON-NLS-1$
			int iRows = 9 + 3 * (jsSize.getValue());
			Font customFont = new Font("verdana", Font.BOLD, ConfigurationManager
					.getInt(CONF_FONTS_SIZE));
			Color mediumGray = new Color(172, 172, 172);

			// take first track author as author
			jlAuthor = new JTextArea(track.getAuthor().getName2(), 1, iRows);
			jlAuthor.setLineWrap(true);
			jlAuthor.setWrapStyleWord(true);
			jlAuthor.setEditable(false);
			jlAuthor.setOpaque(false);
			jlAuthor.setFont(customFont);
			jlAuthor.setForeground(mediumGray);

			jlAlbum = new JTextArea(album.getName2(), 1, iRows);
			jlAlbum.setLineWrap(true);
			jlAlbum.setWrapStyleWord(true);
			jlAlbum.setEditable(false);
			jlAuthor.setFont(new Font(
					"Dialog", Font.BOLD, ConfigurationManager.getInt(CONF_FONTS_SIZE))); //$NON-NLS-1$
			jlAlbum.setFont(new Font(
					"Dialog", Font.BOLD, ConfigurationManager.getInt(CONF_FONTS_SIZE))); //$NON-NLS-1$
			jlAlbum.setOpaque(false);
			jlAlbum.setFont(customFont);
			jlAlbum.setForeground(mediumGray);

			add(jpIcon, "1,0"); //$NON-NLS-1$
			add(jlAuthor, "1,2"); //$NON-NLS-1$
			add(jlAlbum, "1,4"); //$NON-NLS-1$
			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			setToolTipText("<html>" + track.getAuthor().getName2() + "<br><b>" + album.getName2()
					+ "</b></html>");
			setOpaque(false);
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
				//Show tracks infos to allow user to change year, rate...
				ArrayList<Item> alTracks = new ArrayList<Item>(TrackManager.getInstance().getAssociatedTracks(album));
				new PropertiesWizard(alAlbums,alTracks);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent arg0) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			// remove red border on previous item if different from this one
			if (CatalogView.this.item != null && CatalogView.this.item != this) {
				CatalogView.this.item.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			}
			// add a red border on this item
			setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
			// Right click
			if (e.getButton() == MouseEvent.BUTTON1 && e.getSource() == this) {
				// if second click (item already selected), play
				if (CatalogView.this.item == this) {
					play(false, false, false);
				}
				CatalogView.this.item = this;
				// Left click
			} else if (e.getButton() == MouseEvent.BUTTON3 && e.getSource() == this) {
				CatalogView.this.item = this;
				// Show contextual menu
				jmenu.show(jlIcon, e.getX(), e.getY());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent arg0) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent arg0) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent arg0) {
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

	}

}
