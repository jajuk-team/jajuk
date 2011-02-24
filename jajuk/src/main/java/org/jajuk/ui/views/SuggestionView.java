/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
 *  $Revision$
 */

package org.jajuk.ui.views;

import ext.FlowScrollPanel;
import ext.services.lastfm.AlbumInfo;
import ext.services.lastfm.AlbumListInfo;
import ext.services.lastfm.ArtistInfo;
import ext.services.lastfm.LastFmService;
import ext.services.lastfm.SimilarArtistsInfo;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.File;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.thumbnails.AbstractThumbnail;
import org.jajuk.ui.thumbnails.LastFmAlbumThumbnail;
import org.jajuk.ui.thumbnails.LastFmArtistThumbnail;
import org.jajuk.ui.thumbnails.LocalAlbumThumbnail;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXBusyLabel;

/**
 * Show suggested albums based on current collection (bestof, novelties) and
 * LAstFM.
 */
public class SuggestionView extends ViewAdapter {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  private JTabbedPane tabs;

  /** DOCUMENT_ME. */
  protected String artist;

  /**
   * DOCUMENT_ME.
   */
  enum SuggestionType {

    /** DOCUMENT_ME. */
    BEST_OF,

    /** DOCUMENT_ME. */
    NEWEST,

    /** DOCUMENT_ME. */
    RARE,

    /** DOCUMENT_ME. */
    OTHERS_ALBUMS,

    /** DOCUMENT_ME. */
    SIMILAR_ARTISTS
  }

  /** DOCUMENT_ME. */
  JScrollPane jpBestof;

  /** DOCUMENT_ME. */
  JScrollPane jpNewest;

  /** DOCUMENT_ME. */
  JScrollPane jpRare;

  /** DOCUMENT_ME. */
  JScrollPane jpOthersAlbums;

  /** DOCUMENT_ME. */
  JScrollPane jpSimilarArtists;

  /** DOCUMENT_ME. */
  private int comp = 0;

  /** DOCUMENT_ME. */
  List<Album> albumsNewest;

  /** DOCUMENT_ME. */
  List<Album> albumsPrefered;

  /** DOCUMENT_ME. */
  List<Album> albumsRare;

  /** Currently selected thumb. */
  AbstractThumbnail selectedThumb;

  /** DOCUMENT_ME. */
  private AlbumListInfo albums;

  /** DOCUMENT_ME. */
  private SimilarArtistsInfo similar;

  /**
   * DOCUMENT_ME.
   */
  class ThumbMouseListener extends MouseAdapter {

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
      AbstractThumbnail thumb = (AbstractThumbnail) ((JLabel) e.getSource()).getParent();
      // remove red border on previous item if
      // different from this one
      if (selectedThumb != null && selectedThumb != thumb) {
        selectedThumb.setSelected(false);
      }
      // select the new selected thumb
      thumb.setSelected(true);
      selectedThumb = thumb;
    }
  }

  /**
   * Instantiates a new suggestion view.
   */
  public SuggestionView() {
    super();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.views.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("SuggestionView.0");
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.views.IView#populate()
   */
  @Override
  public void initUI() {
    tabs = new JTabbedPane();
    // Remove tab border, see
    // http://forum.java.sun.com/thread.jspa?threadID=260746&messageID=980405
    class MyTabbedPaneUI extends javax.swing.plaf.basic.BasicTabbedPaneUI {
      @Override
      protected Insets getContentBorderInsets(int tabPlacement) {
        return new Insets(0, 0, 0, 0);
      }

      @Override
      protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // nothing to do here...
      }
    }
    // Now use the new TabbedPaneUI
    tabs.setUI(new MyTabbedPaneUI());

    // Fill tabs with empty tabs
    tabs.addTab(Messages.getString("SuggestionView.1"), UtilGUI.getCentredPanel(new JLabel(Messages
        .getString("WikipediaView.3"))));
    tabs.addTab(Messages.getString("SuggestionView.2"), UtilGUI.getCentredPanel(new JLabel(Messages
        .getString("WikipediaView.3"))));
    tabs.addTab(Messages.getString("SuggestionView.5"), UtilGUI.getCentredPanel(new JLabel(Messages
        .getString("WikipediaView.3"))));
    tabs.addTab(Messages.getString("SuggestionView.3"), new JLabel(Messages
        .getString("SuggestionView.7")));
    tabs.addTab(Messages.getString("SuggestionView.4"), new JLabel(Messages
        .getString("SuggestionView.7")));

    // Refresh tabs on demand only, add changelisterner after tab creation to
    // avoid that the stored tab is overwrited at startup
    tabs.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent arg0) {
        refreshLastFMCollectionTabs();
        // store the selected tab
        Conf.setProperty(getClass().getName() + "_"
            + ((getPerspective() == null) ? "solo" : getPerspective().getID()), Integer.toString(
            tabs.getSelectedIndex()).toString());
      }
    });

    if (Conf.containsProperty(getClass().getName() + "_"
        + ((getPerspective() == null) ? "solo" : getPerspective().getID()))) {
      int index = Conf.getInt(getClass().getName() + "_"
          + ((getPerspective() == null) ? "solo" : getPerspective().getID()));
      if (index > 0 && index < tabs.getTabCount()) {
        tabs.setSelectedIndex(index);
      }
    }

    // Add panels
    refreshLocalCollectionTabs();
    // Add tabs
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(tabs);
    // Look for events
    ObservationManager.register(this);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    eventSubjectSet.add(JajukEvents.COVER_DEFAULT_CHANGED);
    eventSubjectSet.add(JajukEvents.SUGGESTIONS_REFRESH);
    return eventSubjectSet;
  }

  /**
   * Refresh local thumbs.
   */
  private void refreshLocalCollectionTabs() {
    // Display a busy panel in the mean-time
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JXBusyLabel busy1 = new JXBusyLabel();
        busy1.setBusy(true);
        JXBusyLabel busy2 = new JXBusyLabel();
        busy2.setBusy(true);
        JXBusyLabel busy3 = new JXBusyLabel();
        busy3.setBusy(true);
        // stop all existing busy labels before we add the new ones...
        stopAllBusyLabels();
        tabs.setComponentAt(0, UtilGUI.getCentredPanel(busy1));
        tabs.setComponentAt(1, UtilGUI.getCentredPanel(busy2));
        tabs.setComponentAt(2, UtilGUI.getCentredPanel(busy3));
      }
    });

    SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
      JScrollPane jsp1;
      JScrollPane jsp2;
      JScrollPane jsp3;

      @Override
      public Void doInBackground() {
        // Refresh thumbs for required albums
        List<Album> albums = new ArrayList<Album>(10);
        albumsPrefered = AlbumManager.getInstance().getBestOfAlbums(
            Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
        albumsNewest = AlbumManager.getInstance().getNewestAlbums(
            Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
        albumsRare = AlbumManager.getInstance().getRarelyListenAlbums(
            Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
        albums.addAll(albumsPrefered);
        albums.addAll(albumsNewest);
        albums.addAll(albumsRare);
        if (albums.size() > 0) {
          for (Album album : albums) {
            // Try creating the thumbnail
            ThumbnailManager.refreshThumbnail(album, 100);
          }
        }
        return null;
      }

      @Override
      public void done() {
        jsp1 = getLocalSuggestionsPanel(SuggestionType.BEST_OF);
        jsp2 = getLocalSuggestionsPanel(SuggestionType.NEWEST);
        jsp3 = getLocalSuggestionsPanel(SuggestionType.RARE);
        // If panel is void, add a void panel as a null object keeps
        // previous element
        stopAllBusyLabels();
        tabs.setComponentAt(0, (jsp1 == null) ? new JPanel() : jsp1);
        tabs.setComponentAt(1, (jsp2 == null) ? new JPanel() : jsp2);
        tabs.setComponentAt(2, (jsp3 == null) ? new JPanel() : jsp3);
      }
    };
    sw.execute();
  }

  /**
   * Refresh last fm collection tabs.
   * DOCUMENT_ME
   */
  private void refreshLastFMCollectionTabs() {
    String newArtist = null;
    File current = QueueModel.getPlayingFile();
    if (current != null) {
      newArtist = current.getTrack().getArtist().getName2();
    }
    // if none track playing
    if (current == null
    // Last.FM infos is disable
        || !Conf.getBoolean(Const.CONF_LASTFM_INFO)
        // None internet access option is set
        || Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)
        // If unknown artist
        || (newArtist == null || newArtist.equals(Messages.getString(UNKNOWN_ARTIST)))) {
      // Set empty panels
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          stopAllBusyLabels();
          tabs.setComponentAt(3, new JLabel(Messages.getString("SuggestionView.7")));
          tabs.setComponentAt(4, new JLabel(Messages.getString("SuggestionView.7")));
        }
      });
      return;
    }
    // Check if artist changed, otherwise, just leave
    if (newArtist.equals(this.artist)) {
      return;
    }
    // Save current artist
    artist = newArtist;
    // Display a busy panel in the mean-time
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JXBusyLabel busy1 = new JXBusyLabel();
        busy1.setBusy(true);
        JXBusyLabel busy2 = new JXBusyLabel();
        busy2.setBusy(true);
        // stop all existing busy labels before we add the new ones...
        stopAllBusyLabels();
        tabs.setComponentAt(3, UtilGUI.getCentredPanel(busy1));
        tabs.setComponentAt(4, UtilGUI.getCentredPanel(busy2));
      }
    });
    // Use a swing worker as construct takes a lot of time
    SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
      JScrollPane jsp1;

      JScrollPane jsp2;

      @Override
      public Void doInBackground() {
        try {
          // Fetch last.fm calls and downloads covers
          preFetchOthersAlbum();
          preFetchSimilarArtists();
        } catch (Exception e) {
          Log.error(e);
        }
        return null;
      }

      @Override
      public void done() {
        jsp1 = getLastFMSuggestionsPanel(SuggestionType.OTHERS_ALBUMS, false);
        jsp2 = getLastFMSuggestionsPanel(SuggestionType.SIMILAR_ARTISTS, false);
        stopAllBusyLabels();
        tabs.setComponentAt(3, (jsp1 == null) ? new JPanel() : jsp1);
        tabs.setComponentAt(4, (jsp2 == null) ? new JPanel() : jsp2);
      }

    };
    sw.execute();
  }

  /**
   * Pre-load other album (done outside the EDT).
   *
   * @throws Exception the exception
   */
  void preFetchOthersAlbum() throws Exception {
    albums = LastFmService.getInstance().getAlbumList(artist, true, 0);
    // Perform images downloads and caching
    if (albums != null && albums.getAlbums().size() > 0) {
      for (AlbumInfo album : albums.getAlbums()) {
        String albumUrl = album.getBigCoverURL();
        if (StringUtils.isBlank(albumUrl)) {
          continue;
        }
        // Download thumb
        URL remote = new URL(albumUrl);
        // Download image and store file reference (to generate the
        // popup thumb for ie)
        DownloadManager.downloadToCache(remote);
      }
    }
  }

  /**
   * Pre-load other album (done outside the EDT).
   *
   * @throws Exception the exception
   */
  void preFetchSimilarArtists() throws Exception {
    // Perform last.fm calls
    similar = LastFmService.getInstance().getSimilarArtists(artist);
    // artists is null for void (unknown) similar artists
    if (similar != null && similar.getArtists() != null) {
      List<ArtistInfo> artists = similar.getArtists();
      for (ArtistInfo similarArtist : artists) {
        String artistUrl = similarArtist.getImageUrl();
        if (StringUtils.isBlank(artistUrl)) {
          continue;
        }
        // Download thumb
        URL remote = new URL(artistUrl);
        // Download the picture and store file reference (to
        // generate the popup thumb for ie)
        DownloadManager.downloadToCache(remote);
      }
    }
  }

  /**
   * Return the result panel for local albums.
   *
   * @param type DOCUMENT_ME
   *
   * @return the local suggestions panel
   */
  JScrollPane getLocalSuggestionsPanel(SuggestionType type) {
    FlowScrollPanel out = new FlowScrollPanel();
    out.setLayout(new FlowLayout(FlowLayout.LEFT));
    JScrollPane jsp = new JScrollPane(out, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    jsp.setBorder(null);
    out.setScroller(jsp);
    List<Album> albums = null;
    if (type == SuggestionType.BEST_OF) {
      albums = albumsPrefered;
    } else if (type == SuggestionType.NEWEST) {
      albums = albumsNewest;
    } else if (type == SuggestionType.RARE) {
      albums = albumsRare;
    }
    if (albums != null && albums.size() > 0) {
      for (Album album : albums) {
        LocalAlbumThumbnail thumb = new LocalAlbumThumbnail(album, 100, false);
        thumb.populate();
        thumb.getIcon().addMouseListener(new ThumbMouseListener());
        out.add(thumb);
      }
    } else {
      out.add(UtilGUI.getCentredPanel(new JLabel(Messages.getString("WikipediaView.3"))));
    }
    return jsp;
  }

  /**
   * Return the result panel for lastFM information.
   *
   * @param type DOCUMENT_ME
   * @param artistView DOCUMENT_ME
   *
   * @return the last fm suggestions panel
   */
  JScrollPane getLastFMSuggestionsPanel(SuggestionType type, boolean artistView) {
    FlowScrollPanel flowPanel = new FlowScrollPanel();
    JScrollPane jsp = new JScrollPane(flowPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    jsp.setBorder(null);
    flowPanel.setScroller(jsp);
    flowPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    if (type == SuggestionType.OTHERS_ALBUMS) {
      if (albums != null && albums.getAlbums().size() > 0) {
        for (AlbumInfo album : albums.getAlbums()) {
          AbstractThumbnail thumb = new LastFmAlbumThumbnail(album);
          thumb.setArtistView(artistView);
          thumb.populate();
          if (thumb.getIcon() != null) {
            thumb.getIcon().addMouseListener(new ThumbMouseListener());
            flowPanel.add(thumb);
          }
        }

      }
      // No result found
      else {
        return new JScrollPane(getNothingFoundPanel());
      }
    } else if (type == SuggestionType.SIMILAR_ARTISTS) {
      if (similar != null) {
        List<ArtistInfo> artists = similar.getArtists();
        for (ArtistInfo similarArtist : artists) {
          AbstractThumbnail thumb = new LastFmArtistThumbnail(similarArtist);
          thumb.setArtistView(artistView);
          thumb.populate();
          if (thumb.getIcon() != null) {
            thumb.getIcon().addMouseListener(new ThumbMouseListener());
            flowPanel.add(thumb);
          }
        }
      }
      // No result found
      else {
        return new JScrollPane(getNothingFoundPanel());
      }
    }
    return jsp;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public void update(JajukEvent event) {
    synchronized (SuggestionView.class) {
      JajukEvents subject = event.getSubject();
      if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
        comp++;
        // Change local collection suggestions every 10 track plays
        if (comp % 10 == 0) {
          refreshLocalCollectionTabs();
        }
        // update last.fm panels
        refreshLastFMCollectionTabs();
      } else if (subject.equals(JajukEvents.PARAMETERS_CHANGE) && isLastFMTabsVisible()) {
        // The show/hide unmounted may have changed, refresh local
        // collection panels
        refreshLastFMCollectionTabs();
      } else if (subject.equals(JajukEvents.COVER_DEFAULT_CHANGED)
          || subject.equals(JajukEvents.SUGGESTIONS_REFRESH)) {
        // New default cover, refresh the view
        refreshLocalCollectionTabs();
      }
    }
  }

  /**
   * [Perf].
   *
   * @return whether LastFM tabs are visible or not
   */
  private boolean isLastFMTabsVisible() {
    // Refresh artists only if user selected similar artists or albums tab
    return (tabs.getSelectedIndex() == 3 || tabs.getSelectedIndex() == 4)
    // Check this view perspective is visible
        && PerspectiveManager.getCurrentPerspective().equals(this.getPerspective());
  }

  /**
   * Refresh lastFM tabs on perspective selection if tabs visible.
   */
  @Override
  public void onPerspectiveSelection() {
    refreshLastFMCollectionTabs();
  }

  /**
   * Gets the nothing found panel.
   *
   * @return a panel with text explaining why no item has been found
   */
  JPanel getNothingFoundPanel() {
    JPanel out = new JPanel(new MigLayout("ins 5", "grow"));
    JEditorPane jteNothing = new JEditorPane("text/html", Messages.getString("SuggestionView.7"));
    jteNothing.setBorder(null);
    jteNothing.setEditable(false);
    jteNothing.setOpaque(false);
    jteNothing.setToolTipText(Messages.getString("SuggestionView.7"));
    out.add(jteNothing, "center,grow");
    return out;
  }
}
