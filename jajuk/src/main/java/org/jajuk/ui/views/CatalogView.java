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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.ui.helpers.DefaultMouseWheelListener;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.thumbnails.LocalAlbumThumbnail;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukJToolbar;
import org.jajuk.ui.widgets.JajukWindow;
import org.jajuk.ui.widgets.SteppedComboBox;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Filter;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXBusyLabel;

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

  JSlider jsSize;

  FlowScrollPanel jpItems;

  JScrollPane jsp;

  /** Filter properties */
  List<PropertyMetaInformation> alFilters;

  /** Sorter properties */
  List<PropertyMetaInformation> alSorters;

  /** Items* */
  Set<LocalAlbumThumbnail> hsItems;

  /** Do search panel need a search */
  private boolean bNeedSearch = false;

  /** Populating flag */
  private boolean bPopulating = false;

  /** Default time in ms before launching a search automatically */
  private static final int WAIT_TIME = 400;

  /** Date last key pressed */
  private long lDateTyped;

  /** Last selected item */
  private LocalAlbumThumbnail item;

  /** Page index */
  private int page = 0;

  /** Number of page in current selection */
  int iNbPages = 0;

  /** Number of created thumbs, used for garbage collection */
  private int iNbCreatedThumbs = 0;

  /** Utility list used by size selector */
  private final List<String> sizes = new ArrayList<String>(10);

  /** Populating flag */
  private boolean populating = false;

  /** Swing Timer to refresh the component */
  private final Timer timerSearch = new Timer(WAIT_TIME, new ActionListener() {
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
    initMetaInformation();

    hsItems = new HashSet<LocalAlbumThumbnail>();

    sizes.add(THUMBNAIL_SIZE_50X50);
    sizes.add(THUMBNAIL_SIZE_100X100);
    sizes.add(THUMBNAIL_SIZE_150X150);
    sizes.add(THUMBNAIL_SIZE_200X200);
    sizes.add(THUMBNAIL_SIZE_250X250);
    sizes.add(THUMBNAIL_SIZE_300X300);

    // --Top (most used) control items
    jpControlTop = new JPanel();
    jpControlTop.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    jlSorter = new JLabel(Messages.getString("Sort") + " ");
    jcbSorter = new SteppedComboBox();
    jcbSorter.setEditable(false);
    // note that a single album can contains tracks with different authors
    // or styles, we will show it only one
    for (PropertyMetaInformation meta : alSorters) {
      jcbSorter.addItem(meta.getHumanName());
    }
    jcbSorter.setSelectedIndex(Conf.getInt(Const.CONF_THUMBS_SORTER));
    jcbSorter.addActionListener(this);
    JToolBar jtbSort = new JajukJToolbar();
    jtbSort.add(jlSorter);
    jtbSort.add(Box.createHorizontalStrut(5));
    jtbSort.add(jcbSorter);

    jlFilter = new JLabel(Messages.getString("AbstractTableView.0") + " ");
    jlContains = new JLabel("   " + Messages.getString("AbstractTableView.7") + " ");
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
    jcbFilter.setSelectedIndex(Conf.getInt(Const.CONF_THUMBS_FILTER));
    jcbFilter.addActionListener(this);
    jtfValue = new JTextField(5);
    jtfValue.setForeground(new Color(172, 172, 172));
    jtfValue.setBorder(BorderFactory.createLineBorder(Color.BLUE));
    jtfValue.setFont(FontManager.getInstance().getFont(JajukFont.SEARCHBOX));
    jtfValue.addKeyListener(new CatalogViewKeyAdaptor());
    JToolBar jtbFilter = new JajukJToolbar();
    jtbFilter.add(jlFilter);
    jtbFilter.add(Box.createHorizontalStrut(5));
    jtbFilter.add(jcbFilter);
    jtbFilter.add(jlContains);
    jtbFilter.add(jtfValue);

    JToolBar jtbPage = new JajukJToolbar();
    jtbPage.setFloatable(false);
    jtbPage.setRollover(true);
    jbPrev = new JButton(IconLoader.getIcon(JajukIcons.PREVIOUS));
    jbPrev.setToolTipText(Messages.getString("CatalogView.12"));
    jbPrev.addActionListener(this);
    jbNext = new JButton(IconLoader.getIcon(JajukIcons.NEXT));
    jbNext.setToolTipText(Messages.getString("CatalogView.13"));
    jbNext.addActionListener(this);
    jcbPage = new SteppedComboBox();
    jcbPage.setToolTipText(Messages.getString("CatalogView.14"));
    jcbPage.addActionListener(this);
    jtbPage.add(jbPrev);
    jtbPage.add(jcbPage);
    jtbPage.add(jbNext);
    double p = TableLayout.PREFERRED;
    double sizeControlTop[][] = { { 10, p, p, p, 20, p, 10 }, { p } };

    TableLayout layoutTop = new TableLayout(sizeControlTop);
    layoutTop.setHGap(5);
    jpControlTop.setLayout(layoutTop);
    jpControlTop.add(jtbFilter, "1,0,l,c");
    jpControlTop.add(jtbSort, "3,0,l,c");
    jpControlTop.add(jtbPage, "5,0,l,c");

    // --Bottom (less used) items
    jcbShowNoCover = new JCheckBox(Messages.getString("CatalogView.2"));
    jcbShowNoCover.setSelected(Conf.getBoolean(Const.CONF_THUMBS_SHOW_WITHOUT_COVER));
    jcbShowNoCover.addActionListener(this);

    JLabel jlSize = new JLabel(Messages.getString("CatalogView.15"));
    jsSize = new JSlider(0, 5);
    jsSize.setMajorTickSpacing(1);
    jsSize.setMinorTickSpacing(1);
    jsSize.setSnapToTicks(true);
    jsSize.setPaintTicks(true);
    jsSize.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    jsSize.addMouseWheelListener(new CatalogViewMouseWheelListener(jsSize));

    int index = sizes.indexOf(Conf.getString(Const.CONF_THUMBS_SIZE));
    if (index < 0) {
      index = 2; // 150x150 if a problem occurs
    }
    jsSize.setValue(index);
    // compute size string for slider tooltip
    String sizeToDisplay = "" + (50 + 50 * index) + "x" + "" + (50 + 50 * index);
    jsSize.setToolTipText(Messages.getString("CatalogView.4") + " " + sizeToDisplay);
    jsSize.addChangeListener(new CatalogViewChangeListener());

    double sizeControlBottom[][] = { { p, p, p, TableLayout.FILL, 5 }, { p } };
    TableLayout layoutBottom = new TableLayout(sizeControlBottom);
    layoutBottom.setHGap(20);
    jpControlBottom = new JPanel();
    jpControlBottom.setLayout(layoutBottom);
    jpControlBottom.add(jcbShowNoCover, "0,0");
    jpControlBottom.add(jlSize, "1,0");
    jpControlBottom.add(jsSize, "2,0,c,c");

    // Covers
    initCovers();

    populateCatalog();

    // subscriptions to events
    ObservationManager.register(this);

    // Show facts
    showFacts();

    // Start the timers
    timerSearch.start();
  }

  /**
   * 
   */
  private void initMetaInformation() {
    alFilters = new ArrayList<PropertyMetaInformation>(10);
    alFilters.add(null); // All
    alFilters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_STYLE));
    alFilters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_AUTHOR));
    alFilters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_ALBUM));
    alFilters.add(TrackManager.getInstance().getMetaInformation(Const.XML_YEAR));

    alSorters = new ArrayList<PropertyMetaInformation>(10);
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_STYLE));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_AUTHOR));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_ALBUM));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_YEAR));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_DISCOVERY_DATE));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_RATE));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_HITS));
  }

  /**
   * 
   */
  private void initCovers() {
    jpItems = new FlowScrollPanel();
    Dimension dim = new Dimension(getWidth(), getHeight());
    jpItems.setPreferredSize(dim);
    jsp = new JScrollPane(jpItems, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    jsp.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
    jpItems.setScroller(jsp);
    jpItems.setLayout(new FlowLayout(FlowLayout.LEFT));
    // global layout
    double size[][] = { { TableLayout.FILL },
        { TableLayout.PREFERRED, 5, TableLayout.FILL, 5, TableLayout.PREFERRED, 5 } };
    setLayout(new TableLayout(size));
    add(jpControlTop, "0,0,l,c");
    add(jsp, "0,2");
    add(jpControlBottom, "0,4,l,c");
  }

  /**
   * Show various information in the information panel
   * 
   */
  private void showFacts() {
    // display facts in the information panel
    // n albums
    String sMessage = AlbumManager.getInstance().getElementCount() + " "
        + Messages.getString("CatalogView.16");
    int albumsPerPage = Conf.getInt(Const.CONF_CATALOG_PAGE_SIZE);
    // n albums / page
    if (albumsPerPage > 0) {
      sMessage += " - " + albumsPerPage + Messages.getString("CatalogView.17");
    }
    InformationJPanel.getInstance().setSelection(sMessage);
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    eventSubjectSet.add(JajukEvents.DEVICE_MOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_UNMOUNT);
    eventSubjectSet.add(JajukEvents.COVER_DEFAULT_CHANGED);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  /**
   * Populate the catalog
   */
  private void populateCatalog() {
    // Prevent unwanted view population requests, do not try to synchronize
    // 'this' :
    // too many threads
    if (populating) {
      Log.debug("Already populating the catalog view");
      return;
    }
    populating = true;
    new Thread() {
      public void run() {
        try {
          final int value = jsp.getVerticalScrollBar().getValue();
          final List<LocalAlbumThumbnail> thumbs = new ArrayList<LocalAlbumThumbnail>(100);
          // Make sure to execute this in the EDT (can be called from update()
          // method)
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              bPopulating = true;
              jsSize.setEnabled(false);
              jcbFilter.setEnabled(false);
              jcbShowNoCover.setEnabled(false);
              jcbSorter.setEnabled(false);
              jbPrev.setEnabled(false);
              jbNext.setEnabled(false);
              jcbPage.setEnabled(false);
              jtfValue.setEditable(false);
              hsItems.clear();
              // remove all devices
              if (jpItems.getComponentCount() > 0) {
                jpItems.removeAll();
              }
              JXBusyLabel busy = new JXBusyLabel(new Dimension(200, 200));
              int xInset = ((JajukWindow.getInstance().getWidth() - 30) / 2) - 200;
              int yInset = ((JajukWindow.getInstance().getHeight() - 120) / 2) - 200;
              busy.setBorder(new EmptyBorder(yInset, xInset, yInset, xInset));
              busy.setBusy(true);
              jpItems.add(busy);
            }
          });
          Filter filter = null;
          if (jtfValue.getText().length() > 0) {
            PropertyMetaInformation meta = alFilters.get(jcbFilter.getSelectedIndex());
            filter = new Filter((meta == null) ? null : meta.getName(), jtfValue.getText(), true,
                false);
          }
          List<Album> albums = null;
          // filter albums matching tracks
          List<Track> alAllTracks = TrackManager.getInstance().getTracks();
          Filter.filterItems(alAllTracks, filter);
          // keep matching albums
          HashSet<Album> hsAlbums = new HashSet<Album>(alAllTracks.size() / 10);
          for (Item item : alAllTracks) {
            Track track = (Track) item;
            Album album = track.getAlbum();
            hsAlbums.add(album);
          }
          // Remove albums with no cover if required
          Iterator<Album> itAlbums = hsAlbums.iterator();
          while (itAlbums.hasNext()) {
            Album album = itAlbums.next();
            if (!jcbShowNoCover.isSelected() && album.getCoverFile() == null) {
              itAlbums.remove();
            }
          }
          albums = new ArrayList<Album>(hsAlbums);

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
              Track track1 = album1.getAnyTrack();
              Track track2 = album2.getAnyTrack();

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
                  return track1.getYear().compareTo(track2.getYear());
                }
              case 4: // Discovery date
                return track2.getDiscoveryDate().compareTo(track1.getDiscoveryDate());
              case 5: // Rate
                if (album1.getRate() < album2.getRate()) {
                  return 1;
                } else {
                  return 0;
                }
              case 6: // Hits
                if (album1.getHits() < album2.getHits()) {
                  return 1;
                } else {
                  return 0;
                }
              }
              return 0;
            }
          });

          // Now process each album
          Set<Directory> directories = new HashSet<Directory>(albums.size());
          List<LocalAlbumThumbnail> alItemsToDisplay = new ArrayList<LocalAlbumThumbnail>(albums
              .size());
          for (Object it : albums) {
            Album album = (Album) it;
            // if hide unmounted tracks is set, continue
            if (Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)) {
              // test if album contains at least one mounted file
              List<Track> trackset = TrackManager.getInstance().getAssociatedTracks(album, false);
              if (trackset.size() > 0) {
                boolean bOK = false;
                for (Track track : trackset) {
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
            Track anyTrack = album.getAnyTrack();
            if (anyTrack != null) {
              // Take the directory of any file of the track
              List<org.jajuk.base.File> fileList = anyTrack.getFiles();
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
          int iSize = Conf.getInt(Const.CONF_CATALOG_PAGE_SIZE);
          if (iSize == 0) {
            iNbPages = 1;
          } else {
            // add one page for trailing items
            iNbPages = alItemsToDisplay.size() / iSize
                + ((alItemsToDisplay.size() % iSize == 0) ? 0 : 1);
          }
          // populate page selector
          // remove action listener
          jcbPage.removeActionListener(CatalogView.this);
          jcbPage.removeAllItems(); // void it

          for (int i = 0; i < iNbPages; i++) { // add the pages
            jcbPage.addItem(Messages.getString("CatalogView.11") + " " + (i + 1) + "/" + iNbPages);
            // start at page 1, not 0
          }
          if (iNbPages > 0) {
            // After user changed the number of thumbs on a page, we can be out
            // of bounds exception so make sure to reinit the page index in this
            // case
            if (page >= jcbPage.getItemCount()) {
              page = 0;
            }
            jcbPage.setSelectedIndex(page);
            jcbPage.addActionListener(CatalogView.this);
            // Add all items
            int max = alItemsToDisplay.size(); // upper limit
            if (page < (iNbPages - 1)) {
              // if last page, take simply to total number of
              // items to display
              max = (page + 1) * Conf.getInt(Const.CONF_CATALOG_PAGE_SIZE);
            }

            for (int i = page * Conf.getInt(Const.CONF_CATALOG_PAGE_SIZE); i < max; i++) {
              final LocalAlbumThumbnail it = alItemsToDisplay.get(i);
              // UI lazy loading
              it.populate();
              thumbs.add(it);
              iNbCreatedThumbs++;
              it.getIcon().addMouseListener(new MouseAdapter() {
                @Override
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
            }
          }
          // The scrollbar must be set after current EDT work to be effective,
          // so queue it
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              // Display the catalog
              if (jpItems.getComponentCount() > 0) {
                // remove the busy label
                jpItems.removeAll();
              }
              for (LocalAlbumThumbnail thumb : thumbs) {
                jpItems.add(thumb);
              }

              jsp.revalidate();
              jsp.repaint();
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  jsp.getVerticalScrollBar().setValue(value);
                }
              });
              jtfValue.setEditable(true);
              jtfValue.requestFocusInWindow();
              jsSize.setEnabled(true);
              jcbFilter.setEnabled(true);
              jcbShowNoCover.setEnabled(true);
              jcbSorter.setEnabled(true);
              jbPrev.setEnabled(true);
              jbNext.setEnabled(true);
              jcbPage.setEnabled(true);
              bPopulating = false;
              UtilGUI.stopWaiting();
            }
          });
        } finally {
          populating = false;
        }
      }
    }.start();

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.DEVICE_REFRESH.equals(subject)
        || JajukEvents.COVER_DEFAULT_CHANGED.equals(subject)
        || JajukEvents.DEVICE_MOUNT.equals(subject) || JajukEvents.DEVICE_UNMOUNT.equals(subject)
        || JajukEvents.PARAMETERS_CHANGE.equals(subject)) {
      // save selected item
      LocalAlbumThumbnail oldItem = CatalogView.this.item;
      // reset paging
      // page = 0;
      populateCatalog();
      // try to restore previous item
      if (oldItem != null) {
        synchronized (this) {
          for (LocalAlbumThumbnail it : hsItems) {
            if (((Album) it.getItem()).equals(oldItem.getItem())) {
              CatalogView.this.item = it;
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
      Conf.setProperty(Const.CONF_THUMBS_FILTER, Integer.toString(jcbFilter.getSelectedIndex()));
    } else if (e.getSource() == jcbSorter) {
      bNeedSearch = true;
      lDateTyped = System.currentTimeMillis();
      Conf.setProperty(Const.CONF_THUMBS_SORTER, Integer.toString(jcbSorter.getSelectedIndex()));
    } else if (e.getSource() == jcbShowNoCover) {
      Conf.setProperty(Const.CONF_THUMBS_SHOW_WITHOUT_COVER, Boolean.toString(jcbShowNoCover
          .isSelected()));
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

  private class CatalogViewKeyAdaptor extends KeyAdapter {
    @Override
    public void keyReleased(KeyEvent e) {
      // Ignore escape press, they can come from popup closing
      if (e.getKeyCode() != KeyEvent.VK_ESCAPE) {
        bNeedSearch = true;
        lDateTyped = System.currentTimeMillis();
      }
    }
  }

  private class CatalogViewMouseWheelListener extends DefaultMouseWheelListener {

    public CatalogViewMouseWheelListener(JSlider js) {
      super(js);
    }

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
      Conf.setProperty(Const.CONF_THUMBS_SIZE, sizes.get(jsSize.getValue()));
      // display thumbs
      populateCatalog();
      // Add again the change listener
      jsSize.addChangeListener(cl);
    }

  }

  private class CatalogViewChangeListener implements ChangeListener {

    public void stateChanged(ChangeEvent e) {
      // Leave user didn't release the move yet
      if (jsSize.getValueIsAdjusting()) {
        return;
      }
      // Store size
      Conf.setProperty(Const.CONF_THUMBS_SIZE, sizes.get(jsSize.getValue()));
      // display thumbs
      populateCatalog();
      // Adjust tooltips
      // compute size string for slider tooltip
      String size = "" + (50 + 50 * jsSize.getValue()) + "x" + "" + (50 + 50 * jsSize.getValue());
      jsSize.setToolTipText(Messages.getString("CatalogView.4") + " " + size);
    }
  }

}
