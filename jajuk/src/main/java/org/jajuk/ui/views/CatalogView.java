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

import ext.FlowScrollPanel;
import ext.SwingWorker;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.ui.helpers.DefaultMouseWheelListener;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.thumbnails.LocalAlbumThumbnail;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.ui.thumbnails.ThumbnailsMaker;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.SteppedComboBox;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Filter;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

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

  JCheckBox jcbShowNoCover;

  JLabel jlSize;

  JSlider jsSize;

  JButton jbRefresh;

  FlowScrollPanel jpItems;

  JScrollPane jsp;

  /** Filter properties */
  ArrayList<PropertyMetaInformation> alFilters;

  /** Sorter properties */
  ArrayList<PropertyMetaInformation> alSorters;

  /** Items* */
  HashSet<LocalAlbumThumbnail> hsItems;

  /** Do search panel need a search */
  private boolean bNeedSearch = false;

  /** Populating flag */
  private boolean bPopulating = false;

  /** Default time in ms before launching a search automatically */
  private static final int WAIT_TIME = 400;

  /** Date last key pressed */
  private long lDateTyped;

  /** Last selected item */
  public LocalAlbumThumbnail item;

  /** Page index */
  private int page = 0;

  private byte[] lock = new byte[0];

  /** Number of page in current selection */
  int iNbPages = 0;

  /** Number of created thumbs, used for garbage collection */
  public int iNbCreatedThumbs = 0;

  /** Utility list used by size selector */
  private ArrayList<String> sizes = new ArrayList<String>(10);

  /** Swing Timer to refresh the component */
  private Timer timerSearch = new Timer(WAIT_TIME, new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (bNeedSearch && !bPopulating && (System.currentTimeMillis() - lDateTyped >= WAIT_TIME)) {
        // reset paging
        page = 0;
        populateCatalog();
        bNeedSearch = false;
      }
    }
  });

  public LocalAlbumThumbnail getSelectedItem() {
    return item;
  }

  /**
   * Constructor
   */
  public CatalogView() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
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

    hsItems = new HashSet<LocalAlbumThumbnail>();

    sizes.add(THUMBNAIL_SIZE_50x50);
    sizes.add(THUMBNAIL_SIZE_100x100);
    sizes.add(THUMBNAIL_SIZE_150x150);
    sizes.add(THUMBNAIL_SIZE_200x200);
    sizes.add(THUMBNAIL_SIZE_250x250);
    sizes.add(THUMBNAIL_SIZE_300x300);

    // --Top (most used) control items
    jpControlTop = new JPanel();
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
    jtfValue.setForeground(new Color(172, 172, 172));
    jtfValue.setBorder(BorderFactory.createLineBorder(Color.BLUE));
    jtfValue.setFont(FontManager.getInstance().getFont(JajukFont.BOLD_XXL));
    jtfValue.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        // Ignore escape press, they can come from popup closing
        if (e.getKeyCode() != KeyEvent.VK_ESCAPE) {
          bNeedSearch = true;
          lDateTyped = System.currentTimeMillis();
        }
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
    jcbShowNoCover = new JCheckBox(Messages.getString("CatalogView.2"));
    jcbShowNoCover.setSelected(ConfigurationManager.getBoolean(CONF_THUMBS_SHOW_WITHOUT_COVER));
    jcbShowNoCover.addActionListener(this);

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
        String size = "" + (50 + 50 * jsSize.getValue()) + "x" + "" + (50 + 50 * jsSize.getValue());
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
    jpControlBottom.setLayout(layoutBottom);
    jpControlBottom.add(jcbShowNoCover, "0,0");
    jpControlBottom.add(jlSize, "1,0");
    jpControlBottom.add(jsSize, "2,0,c,c");
    jpControlBottom.add(jbRefresh, "3,0,r,c");

    // Covers
    jpItems = new FlowScrollPanel();
    Dimension dim = new Dimension(getWidth(), getHeight());
    jpItems.setPreferredSize(dim);
    jsp = new JScrollPane(jpItems, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    jpItems.setScroller(jsp);
    jpItems.setLayout(new FlowLayout(FlowLayout.LEFT));
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

    // Start the timers
    timerSearch.start();
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
    eventSubjectSet.add(EventSubject.EVENT_PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  /**
   * Populate the catalog
   */
  private synchronized void populateCatalog() {
    bPopulating = true;
    jsSize.setEnabled(false);
    jcbFilter.setEnabled(false);
    jcbShowNoCover.setEnabled(false);
    jcbSorter.setEnabled(false);
    jbPrev.setEnabled(false);
    jbNext.setEnabled(false);
    jcbPage.setEnabled(false);
    Util.waiting();
    SwingWorker sw = new SwingWorker() {
      int i = jsp.getVerticalScrollBar().getValue();

      @Override
      public Object construct() {
        // This synchronize fixes a strange race condition when changing
        // thumb size and finished() method is done but all thumb not
        // yet displayed (several times the same album with different
        // size is displayed)
        synchronized (lock) {
          hsItems.clear();
          // remove all devices
          if (jpItems.getComponentCount() > 0) {
            jpItems.removeAll();
          }
          Filter filter = null;
          if (jtfValue.getText().length() > 0) {
            PropertyMetaInformation meta = alFilters.get(jcbFilter.getSelectedIndex());
            filter = new Filter((meta == null) ? null : meta.getName(), jtfValue.getText(), true,
                false);
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
          // Find a matching track for each album and store it for
          // perfs
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
              // TODO: get two tracks of album and compare Author,
              // if
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
                return track2.getDiscoveryDate().compareTo(track1.getDiscoveryDate());
              }
              return 0;
            }
          });

          // Now process each album
          HashSet<Directory> directories = new HashSet<Directory>(albums.size());
          ArrayList<LocalAlbumThumbnail> alItemsToDisplay = new ArrayList<LocalAlbumThumbnail>(
              albums.size());
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
              LocalAlbumThumbnail cover = new LocalAlbumThumbnail(album, getSelectedSize(), true);
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
          jcbPage.removeActionListener(CatalogView.this); // remove
          // action
          // listener
          jcbPage.removeAllItems(); // void it
          for (int i = 0; i < iNbPages; i++) { // add the pages
            jcbPage.addItem(Messages.getString("CatalogView.11") + " " + (i + 1) + "/" + iNbPages);
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
              LocalAlbumThumbnail item = alItemsToDisplay.get(i);
              // populate item (construct UI) only when needed
              item.populate();
              iNbCreatedThumbs++;
              // //invoke garbage collecting to avoid using too
              // much
              // memory
              if (iNbCreatedThumbs % 20 == 0) {
                System.gc();
              }
              item.jlIcon.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                  LocalAlbumThumbnail thumb = (LocalAlbumThumbnail) ((JLabel) e.getSource())
                      .getParent();
                  // Unselect previous thumb
                  if (CatalogView.this.item != null && CatalogView.this.item != thumb) {
                    CatalogView.this.item.setSelected(false);
                  }
                  // Select new thumb
                  thumb.setSelected(true);
                  CatalogView.this.item = thumb;
                }
              });
              if (!item.isNoCover() || (item.isNoCover() && jcbShowNoCover.isSelected())) {
                jpItems.add(item);
              }
            }
          }
        }
        return null;
      }

      @Override
      public void finished() {
        jsp.revalidate();
        jsp.repaint();
        jsp.getVerticalScrollBar().setValue(i);
        jtfValue.requestFocusInWindow();
        jsSize.setEnabled(true);
        jcbFilter.setEnabled(true);
        jcbShowNoCover.setEnabled(true);
        jcbSorter.setEnabled(true);
        jbPrev.setEnabled(true);
        jbNext.setEnabled(true);
        jcbPage.setEnabled(true);
        bPopulating = false;
        Util.stopWaiting();
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
      LocalAlbumThumbnail oldItem = CatalogView.this.item;
      // reset paging
      // page = 0;
      populateCatalog();
      // try to restore previous item
      if (oldItem != null) {
        synchronized (lock) {
          for (LocalAlbumThumbnail item : hsItems) {
            if (((Album) item.getItem()).equals(oldItem.getItem())) {
              CatalogView.this.item = item;
              CatalogView.this.item.setSelected(true);
              break;
            }
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
      ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_50x50);
      ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_100x100);
      ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_150x150);
      ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_200x200);
      ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_250x250);
      ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_300x300);
      Util.waiting();
      SwingWorker sw = new SwingWorker() {
        @Override
        public Object construct() {
          // Launch thumbs creation in another process
          ThumbnailsMaker.launchAllSizes(true);
          return null;
        }

        @Override
        public void finished() {
          // display thumbs
          populateCatalog();
          Util.stopWaiting();
        }
      };
      sw.start();
    } else if (e.getSource() == jcbShowNoCover) {
      ConfigurationManager.setProperty(CONF_THUMBS_SHOW_WITHOUT_COVER, Boolean
          .toString(jcbShowNoCover.isSelected()));
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

  /**
   * @return current thumbs size as selected with the combo
   */
  private int getSelectedSize() {
    return 50 + (50 * jsSize.getValue());
  }

}
