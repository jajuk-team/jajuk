/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
 *  http://jajuk.info
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
 *  
 */
package org.jajuk.ui.views;

import ext.FlowScrollPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumComparator;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.helpers.DefaultMouseWheelListener;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.helpers.TwoStepsDisplayable;
import org.jajuk.ui.thumbnails.LocalAlbumThumbnail;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukJToolbar;
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
public class CatalogView extends ViewAdapter implements ActionListener, TwoStepsDisplayable {
  /** Generated serialVersionUID. */
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
  JComboBox jcbShowCover;
  JSlider jsSize;
  FlowScrollPanel jpItems;
  JScrollPane jsp;
  /** Filter properties. */
  List<PropertyMetaInformation> alFilters;
  /** Sorter properties. */
  List<PropertyMetaInformation> alSorters;
  /** Do search panel need a search. */
  private boolean bNeedSearch = false;
  /** Populating flag. */
  private boolean bPopulating = false;
  /** Default time in ms before launching a search automatically. */
  private static final int WAIT_TIME = 600;
  /** Date last key pressed. */
  private long lDateTyped;
  /** Last selected item. */
  private LocalAlbumThumbnail item;
  /** Page index. */
  private int page = 0;
  /** Number of page in current selection. */
  int iNbPages = 0;
  /** Utility list used by size selector. */
  private final List<String> sizes = new ArrayList<String>(10);
  /** Thumbs list *. */
  private List<LocalAlbumThumbnail> thumbs;
  /** Last scrollbar position *. */
  private int scrollPosition;
  /** Swing Timer to refresh the component. */
  private final Timer timerSearch = new Timer(WAIT_TIME, new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      if (bNeedSearch && !bPopulating && (System.currentTimeMillis() - lDateTyped >= WAIT_TIME)) {
        // reset paging
        page = 0;
        populateCatalog();
        bNeedSearch = false;
      }
    }
  });

  /**
   * Gets the selected item.
   *
   * @return the selected item
   */
  public LocalAlbumThumbnail getSelectedItem() {
    return item;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.IView#display()
   */
  @Override
  public void initUI() {
    initMetaInformation();
    sizes.add(THUMBNAIL_SIZE_50X50);
    sizes.add(THUMBNAIL_SIZE_100X100);
    sizes.add(THUMBNAIL_SIZE_150X150);
    sizes.add(THUMBNAIL_SIZE_200X200);
    sizes.add(THUMBNAIL_SIZE_250X250);
    sizes.add(THUMBNAIL_SIZE_300X300);
    // --Top (most used) control items
    jpControlTop = new JPanel();
    jlSorter = new JLabel(Messages.getString("Sort") + " ");
    jcbSorter = new SteppedComboBox();
    jcbSorter.setEditable(false);
    // note that a single album can contains tracks with different artists
    // or genres, we will show it only one
    for (PropertyMetaInformation meta : alSorters) {
      jcbSorter.addItem(meta.getHumanName());
    }
    jcbSorter.setSelectedIndex(Conf.getInt(Const.CONF_THUMBS_SORTER));
    jcbSorter.addActionListener(this);
    jlFilter = new JLabel(Messages.getString("AbstractTableView.0") + " ");
    jlContains = new JLabel("   " + Messages.getString("AbstractTableView.7") + " ");
    jcbFilter = new SteppedComboBox();
    jcbFilter.setEditable(false);
    // note that a single album can contains tracks with different artists
    // or genres, we will show it only one
    for (PropertyMetaInformation meta : alFilters) {
      if (meta == null) { // "any" filter
        jcbFilter.addItem(Messages.getString("AbstractTableView.8"));
      } else {
        jcbFilter.addItem(meta.getHumanName());
      }
    }
    jcbFilter.setSelectedIndex(Conf.getInt(Const.CONF_THUMBS_FILTER));
    jcbFilter.addActionListener(this);
    jtfValue = new JTextField();
    jtfValue.setForeground(new Color(172, 172, 172));
    jtfValue.setMargin(new Insets(0, 3, 0, 0));
    jtfValue.setFont(FontManager.getInstance().getFont(JajukFont.SEARCHBOX));
    jtfValue.addKeyListener(new CatalogViewKeyAdaptor());
    JToolBar jtbPage = new JajukJToolbar();
    jtbPage.setFloatable(false);
    jtbPage.setRollover(true);
    jbPrev = new JButton(IconLoader.getIcon(JajukIcons.PLAYER_PREVIOUS_SMALL));
    jbPrev.setToolTipText(Messages.getString("CatalogView.12"));
    jbPrev.addActionListener(this);
    jbNext = new JButton(IconLoader.getIcon(JajukIcons.PLAYER_NEXT_SMALL));
    jbNext.setToolTipText(Messages.getString("CatalogView.13"));
    jbNext.addActionListener(this);
    jcbPage = new SteppedComboBox();
    jcbPage.setToolTipText(Messages.getString("CatalogView.14"));
    jcbPage.addActionListener(this);
    jtbPage.add(jbPrev);
    jtbPage.add(jcbPage);
    jtbPage.add(jbNext);
    jpControlTop.setLayout(new MigLayout("ins 3", "[grow][grow][grow][grow]"));
    jpControlTop.add(jlFilter, "split 2");
    jpControlTop.add(jcbFilter, "grow");
    jpControlTop.add(jlContains, "split 2");
    jpControlTop.add(jtfValue, "gapright 40,grow,width 100::");
    jpControlTop.add(jlSorter, "split 2");
    jpControlTop.add(jcbSorter, "gapright 40,grow");
    jpControlTop.add(jtbPage, "gapright 5,grow");
    // --Bottom (less used) items
    jcbShowCover = new JComboBox();
    jcbShowCover.addItem(Messages.getString("CatalogView.21"));
    jcbShowCover.addItem(Messages.getString("CatalogView.22"));
    jcbShowCover.addItem(Messages.getString("CatalogView.2"));
    jcbShowCover.setSelectedIndex(Conf.getInt(Const.CONF_THUMBS_SHOW_COVER));
    jcbShowCover.addActionListener(this);
    JLabel jlSize = new JLabel(Messages.getString("CatalogView.15"));
    jsSize = new JSlider(0, 5);
    jsSize.setMajorTickSpacing(1);
    jsSize.setMinorTickSpacing(1);
    jsSize.setSnapToTicks(true);
    jsSize.setPaintTicks(true);
    jsSize.addMouseWheelListener(new CatalogViewMouseWheelListener(jsSize));
    int index = sizes.indexOf(Conf.getString(Const.CONF_THUMBS_SIZE));
    if (index < 0) {
      index = 2; // 150x150 if a problem occurs
    }
    jsSize.setValue(index);
    // compute size string for slider tooltip
    String sizeToDisplay = "" + (50 + 50 * index) + "x" + "" + (50 + 50 * index);
    jsSize.setToolTipText(Messages.getString("CatalogView.4") + " " + sizeToDisplay);
    jsSize.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent arg0) {
        sliderValueChanged();
      }
    });
    jpControlBottom = new JPanel(new MigLayout("gapx 20"));
    jpControlBottom.add(jcbShowCover);
    jpControlBottom.add(jlSize, "split 2");
    jpControlBottom.add(jsSize);
    // Set layout
    initLayout();
    populateCatalog();
    // subscriptions to events
    ObservationManager.register(this);
    // Show facts
    showFacts();
    // Start the timers
    timerSearch.start();
  }

  /**
   * Inits the meta information.
   **/
  private void initMetaInformation() {
    alFilters = new ArrayList<PropertyMetaInformation>(10);
    alFilters.add(null); // All
    alFilters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_GENRE));
    alFilters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_ARTIST));
    alFilters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_ALBUM));
    alFilters.add(TrackManager.getInstance().getMetaInformation(Const.XML_YEAR));
    // please note: this needs to be kept in-sync with what we do in
    // AlbumComparator!
    alSorters = new ArrayList<PropertyMetaInformation>(10);
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_GENRE));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_ARTIST));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_ALBUM));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_YEAR));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_DISCOVERY_DATE));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_RATE));
    alSorters.add(TrackManager.getInstance().getMetaInformation(Const.XML_TRACK_HITS));
  }

  /**
   * Initialize final layout and add main panels.
   */
  private void initLayout() {
    // Remove any busy label
    if (getComponentCount() > 0) {
      removeAll();
    }
    jpItems = new FlowScrollPanel();
    jsp = new JScrollPane(jpItems, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    jsp.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
    jpItems.setScroller(jsp);
    jpItems.setLayout(new FlowLayout(FlowLayout.LEFT));
    // global layout
    setLayout(new MigLayout("", "[grow]", "[][grow][]"));
    add(jpControlTop, "grow,wrap");
    add(jsp, "wrap,grow");
    add(jpControlBottom, "grow");
    revalidate();
    repaint();
  }

  /**
   * Reset the catalog view and show a busy label Must be called from the EDT.
   */
  private void showBusyLabel() {
    if (getComponentCount() > 0) {
      removeAll();
    }
    JXBusyLabel busy = new JXBusyLabel(new Dimension(100, 100));
    busy.setBusy(true);
    setLayout(new MigLayout("", "[grow]", "[grow]"));
    add(busy, "center");
    revalidate();
    repaint();
  }

  /**
   * Show various information in the information panel.
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

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
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
   * <p>
   * Must be called from the EDT.
   */
  private void populateCatalog() {
    // Prevent unwanted view population requests, do not try to synchronize
    // 'this' : too many threads
    if (bPopulating) {
      Log.debug("Already populating the catalog view");
      return;
    }
    bPopulating = true;
    // Store current state
    scrollPosition = jsp.getVerticalScrollBar().getValue();
    thumbs = new ArrayList<LocalAlbumThumbnail>(100);
    // Clear all the view and show a busy label instead
    showBusyLabel();
    // Show the page
    UtilGUI.populate(this);
  }

  /**
   * Compute the catalog page to be displayed.
   * <p>
   * Do *not* call this from the EDT, can take a while to run
   *
   * @return the object
   */
  @Override
  public Object longCall() {
    // Every albums
    List<Album> albums = null;
    // The final album list we will display
    List<Album> pageAlbums = new ArrayList<Album>(Conf.getInt(Const.CONF_CATALOG_PAGE_SIZE));
    try {
      Filter filter = null;
      if (jtfValue.getText().length() > 0) {
        PropertyMetaInformation meta = alFilters.get(jcbFilter.getSelectedIndex());
        filter = new Filter((meta == null) ? null : meta.getName(), jtfValue.getText(), true, false);
      }
      // filter albums matching tracks
      List<Track> alAllTracks = TrackManager.getInstance().getTracks();
      alAllTracks = Filter.filterItems(alAllTracks, filter, Track.class);
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
        if (jcbShowCover.getSelectedIndex() == Const.CATALOG_VIEW_COVER_MODE_WITH
            && !album.containsCover()) {
          itAlbums.remove();
        } else if (jcbShowCover.getSelectedIndex() == Const.CATALOG_VIEW_COVER_MODE_WITHOUT
            && album.containsCover()) {
          itAlbums.remove();
        }
      }
      albums = new ArrayList<Album>(hsAlbums);
      // sort albums
      final int index = jcbSorter.getSelectedIndex();
      Collections.sort(albums, new AlbumComparator(index));
      // Now process each album
      Set<Directory> directories = new HashSet<Directory>(albums.size());
      Iterator<Album> it = albums.iterator();
      while (it.hasNext()) {
        Album album = it.next();
        // if hide unmounted tracks is set, continue
        if (Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)) {
          // test if album contains at least one mounted file
          List<Track> trackset = TrackManager.getInstance().getAssociatedTracks(album, false);
          if (trackset.size() == 0) {
            it.remove();
            continue;
          }
          boolean bOK = false;
          for (Track track : trackset) {
            if (track.getReadyFiles().size() > 0) {
              bOK = true;
              break;
            }
          }
          if (!bOK) {
            it.remove();
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
            directories.add(dir);
          }
        }
      }
      // Force thumbs build if required, this is the longest task of this worker
      // we only keep albums for this page
      // computes the number of pages
      int iSize = Conf.getInt(Const.CONF_CATALOG_PAGE_SIZE);
      if (iSize == 0) {
        iNbPages = 1;
      } else {
        // add one page for trailing items
        iNbPages = albums.size() / iSize + ((albums.size() % iSize == 0) ? 0 : 1);
      }
      // After user changed the number of thumbs on a page, we can be
      // out of bounds exception so make sure to reinit the page index in
      // this case
      if (page >= jcbPage.getItemCount()) {
        page = 0;
      }
      // Add all items
      int max = albums.size(); // upper limit
      if (page < (iNbPages - 1)) {
        // if last page, take simply to total number of
        // items to display
        max = (page + 1) * Conf.getInt(Const.CONF_CATALOG_PAGE_SIZE);
      }
      // Populate each thumb if required (THIS IS LOOOOOONG)
      for (int i = page * Conf.getInt(Const.CONF_CATALOG_PAGE_SIZE); i < max; i++) {
        Album album = albums.get(i);
        pageAlbums.add(album);
        ThumbnailManager.refreshThumbnail(album, getSelectedSize());
      }
    } finally {
      // Make sure to reset the populating flag in case of problem
      // Note that this flag is reseted here and not in the shortCall() method
      // because the shortCall method is not called in case of Exception thrown
      // in the longCall() method.
      bPopulating = false;
    }
    return pageAlbums;
  }

  /**
   * Catalog page display (must be called from the EDT).
   *
   * @param in 
   */
  @Override
  public void shortCall(Object in) {
    @SuppressWarnings("unchecked")
    List<Album> albums = (List<Album>) in;
    if (in == null) {
      stopAllBusyLabels();
      return;
    }
    // Populate each thumb if required (this is short because the thumb should have already been built in the long call )
    for (Album album : albums) {
      final LocalAlbumThumbnail thumb = new LocalAlbumThumbnail(album, getSelectedSize(), true);
      thumb.populate();
      thumbs.add(thumb);
      // restore previous selected item if still set
      if (item != null) {
        if (((Album) thumb.getItem()).equals(item.getItem())) {
          CatalogView.this.item = thumb;
          CatalogView.this.item.setSelected(true);
        }
      }
      // Thumb selection mouse listener
      thumb.getIcon().addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          LocalAlbumThumbnail thumb = (LocalAlbumThumbnail) ((JLabel) e.getSource()).getParent();
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
    // populate page selector
    // remove action listener
    jcbPage.removeActionListener(CatalogView.this);
    jcbPage.removeAllItems(); // void it
    for (int i = 0; i < iNbPages; i++) { // add the pages
      jcbPage.addItem(Messages.getString("CatalogView.11") + " " + (i + 1) + "/" + iNbPages);
      // start at page 1, not 0
    }
    if (iNbPages > 0) {
      jcbPage.setSelectedIndex(page);
      jcbPage.addActionListener(CatalogView.this);
    }
    initLayout();
    for (LocalAlbumThumbnail thumb : thumbs) {
      jpItems.add(thumb);
    }
    // The scrollbar must be set after current EDT work to be
    // effective,
    // so queue it
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        jsp.getVerticalScrollBar().setValue(scrollPosition);
      }
    });
    jtfValue.requestFocusInWindow();
    UtilGUI.stopWaiting();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.DEVICE_REFRESH.equals(subject)
        || JajukEvents.COVER_DEFAULT_CHANGED.equals(subject)
        || JajukEvents.DEVICE_MOUNT.equals(subject) || JajukEvents.DEVICE_UNMOUNT.equals(subject)
        || JajukEvents.PARAMETERS_CHANGE.equals(subject)) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          populateCatalog();
        }
      });
    }
    // In all cases, update the facts
    showFacts();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("CatalogView.0");
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == jcbFilter) {
      if (jtfValue.getText().trim().equals("")) {
        // no need to refresh
        return;
      }
      bNeedSearch = true;
      lDateTyped = System.currentTimeMillis();
      Conf.setProperty(Const.CONF_THUMBS_FILTER, Integer.toString(jcbFilter.getSelectedIndex()));
    } else if (e.getSource() == jcbSorter) {
      bNeedSearch = true;
      lDateTyped = System.currentTimeMillis();
      Conf.setProperty(Const.CONF_THUMBS_SORTER, Integer.toString(jcbSorter.getSelectedIndex()));
    } else if (e.getSource() == jcbShowCover) {
      Conf.setProperty(Const.CONF_THUMBS_SHOW_COVER,
          Integer.toString(jcbShowCover.getSelectedIndex()));
      // Reset page to zero to avoid out of bounds exceptions, when restricting
      // the filter, less pages are available
      page = 0;
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
   * Gets the selected size.
   *
   * @return current thumbs size as selected with the combo
   */
  private int getSelectedSize() {
    return 50 + (50 * jsSize.getValue());
  }

  /**
   * .
   */
  private class CatalogViewKeyAdaptor extends KeyAdapter {
    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
      // Ignore escape press, they can come from popup closing
      if (e.getKeyCode() != KeyEvent.VK_ESCAPE) {
        bNeedSearch = true;
        lDateTyped = System.currentTimeMillis();
      }
    }
  }

  /**
   * .
   */
  private class CatalogViewMouseWheelListener extends DefaultMouseWheelListener {
    /**
     * Instantiates a new catalog view mouse wheel listener.
     *
     * @param js 
     */
    public CatalogViewMouseWheelListener(JSlider js) {
      super(js);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jajuk.ui.helpers.DefaultMouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent
     * )
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
      ChangeListener cl = jsSize.getChangeListeners()[0];
      // Remove the concurrent change listener
      jsSize.removeChangeListener(cl);
      super.mouseWheelMoved(mwe);
      sliderValueChanged();
      // Add again the change listener
      jsSize.addChangeListener(cl);
    }
  }

  /**
   * Factorized code for thumb size change.
   */
  private void sliderValueChanged() {
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

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.views.ViewAdapter#cleanup()
   */
  @Override
  public void cleanup() {
    // make sure the timer is not running any more
    timerSearch.stop();
    // we specifically request the focus for jtfValue, therefore we should make sure that we release
    // that focus to let this be destroyed
    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
    // call the parent class to do more cleanup if necessary
    super.cleanup();
  }
}
