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

import org.apache.commons.lang3.StringUtils;
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

import ext.FlowScrollPanel;
import ext.services.lastfm.AlbumInfo;
import ext.services.lastfm.AlbumListInfo;
import ext.services.lastfm.ArtistInfo;
import ext.services.lastfm.LastFmService;
import ext.services.lastfm.SimilarArtistsInfo;
import net.miginfocom.swing.MigLayout;

/**
 * Show suggested albums based on current collection (bestof, novelties) and
 * LAstFM.
 */
@SuppressWarnings("serial")
public class SuggestionView extends ViewAdapter {
  private JTabbedPane tabs;
  protected String artist;

  //Remove tab border, see
  // http://forum.java.sun.com/thread.jspa?threadID=260746&messageID=980405
  static class MyTabbedPaneUI extends javax.swing.plaf.basic.BasicTabbedPaneUI {
    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {
      return new Insets(0, 0, 0, 0);
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
      // nothing to do here...
    }
  }

  enum SuggestionType {
    BEST_OF, NEWEST, RARE, OTHERS_ALBUMS, SIMILAR_ARTISTS
  }

  private int comp = 0;
  List<Album> albumsNewest;
  List<Album> albumsPrefered;
  List<Album> albumsRare;
  /** Currently selected thumb. */
  AbstractThumbnail selectedThumb;
  private AlbumListInfo albums;
  private SimilarArtistsInfo similar;
  JXBusyLabel busyLocal1 = new JXBusyLabel();
  JXBusyLabel busyLocal2 = new JXBusyLabel();
  JXBusyLabel busyLocal3 = new JXBusyLabel();
  JXBusyLabel busyLastFM1 = new JXBusyLabel();
  JXBusyLabel busyLastFM2 = new JXBusyLabel();

  private class ThumbMouseListener extends MouseAdapter {
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

  public SuggestionView() {
    super();
  }

  @Override
  public String getDesc() {
    return Messages.getString("SuggestionView.0");
  }

  @Override
  public void initUI() {
    tabs = new JTabbedPane();
    // Now use the new TabbedPaneUI
    tabs.setUI(new MyTabbedPaneUI());
    // Fill tabs with empty tabs
    tabs.addTab(Messages.getString("SuggestionView.1"),
        UtilGUI.getCentredPanel(new JLabel(Messages.getString("WikipediaView.3"))));
    tabs.addTab(Messages.getString("SuggestionView.2"),
        UtilGUI.getCentredPanel(new JLabel(Messages.getString("WikipediaView.3"))));
    tabs.addTab(Messages.getString("SuggestionView.5"),
        UtilGUI.getCentredPanel(new JLabel(Messages.getString("WikipediaView.3"))));
    tabs.addTab(Messages.getString("SuggestionView.3"),
        new JLabel(Messages.getString("SuggestionView.7")));
    tabs.addTab(Messages.getString("SuggestionView.4"),
        new JLabel(Messages.getString("SuggestionView.7")));
    addTabChangeListener();
    selectTabFromConf();
    refreshLocalCollectionTabs();
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(tabs);
    ObservationManager.register(this);
  }

  private void selectTabFromConf() {
    if (Conf.containsProperty(getClass().getName() + "_"
        + ((getPerspective() == null) ? "solo" : getPerspective().getID()))) {
      int index = Conf.getInt(getClass().getName() + "_"
          + ((getPerspective() == null) ? "solo" : getPerspective().getID()));
      if (index > 0 && index < tabs.getTabCount()) {
        tabs.setSelectedIndex(index);
      }
    }
  }

  private void addTabChangeListener() {
    // Refresh tabs on demand only, add changeListerner after tab creation to
    // avoid the stored tab to be overwriten at startup
    tabs.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent arg0) {
        refreshLastFMCollectionTabs();
        // store the selected tab
        Conf.setProperty(getClass().getName() + "_"
            + ((getPerspective() == null) ? "solo" : getPerspective().getID()),
                Integer.toString(tabs.getSelectedIndex()));
      }
    });
  }

  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<>();
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
    // For some reasons, if we put that code into an invokeLater() call
    // it is executed after the next done() in next swing worker, no clue why
    // As a compromise, we only show busy label when called in EDT (not the case when the
    // call is from an update() )
    if (SwingUtilities.isEventDispatchThread()) {
      busyLocal1.setBusy(true);
      busyLocal2.setBusy(true);
      busyLocal3.setBusy(true);
      // stop all existing busy labels before we add the new ones...
      //stopAllBusyLabels();
      tabs.setComponentAt(0, UtilGUI.getCentredPanel(busyLocal1));
      tabs.setComponentAt(1, UtilGUI.getCentredPanel(busyLocal2));
      tabs.setComponentAt(2, UtilGUI.getCentredPanel(busyLocal3));
    }
    SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
      JScrollPane jsp1;
      JScrollPane jsp2;
      JScrollPane jsp3;

      @Override
      public Void doInBackground() {
        albumsPrefered = AlbumManager.getInstance().getBestOfAlbums(
            Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
        albumsNewest = AlbumManager.getInstance().getNewestAlbums(
            Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
        albumsRare = AlbumManager.getInstance().getRarelyListenAlbums(
            Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
        refreshThumbsForLocalAlbums();
        return null;
      }

      private void refreshThumbsForLocalAlbums() {
        // Refresh thumbs for required albums
        List<Album> albums = new ArrayList<>(10);
        albums.addAll(albumsPrefered);
        albums.addAll(albumsNewest);
        albums.addAll(albumsRare);
        if (albums.size() > 0) {
          for (Album album : albums) {
            // Try creating the thumbnail
            ThumbnailManager.refreshThumbnail(album, 100);
          }
        }
      }

      @Override
      public void done() {
        jsp1 = getLocalSuggestionsPanel(SuggestionType.BEST_OF);
        jsp2 = getLocalSuggestionsPanel(SuggestionType.NEWEST);
        jsp3 = getLocalSuggestionsPanel(SuggestionType.RARE);
        busyLocal1.setBusy(false);
        busyLocal2.setBusy(false);
        busyLocal3.setBusy(false);
        tabs.setComponentAt(0, jsp1);
        tabs.setComponentAt(1, jsp2);
        tabs.setComponentAt(2, jsp3);
      }
    };
    sw.execute();
  }

  /**
   * Refresh last fm collection tabs.
   *
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
      SwingUtilities.invokeLater(() -> {
        tabs.setComponentAt(3, new JLabel(Messages.getString("SuggestionView.7")));
        tabs.setComponentAt(4, new JLabel(Messages.getString("SuggestionView.7")));
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
    SwingUtilities.invokeLater(() -> {
      busyLastFM1.setBusy(true);
      busyLastFM2.setBusy(true);
      tabs.setComponentAt(3, UtilGUI.getCentredPanel(busyLastFM1));
      tabs.setComponentAt(4, UtilGUI.getCentredPanel(busyLastFM2));
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
        busyLastFM1.setBusy(false);
        busyLastFM2.setBusy(false);
        tabs.setComponentAt(3, jsp1);
        tabs.setComponentAt(4, jsp2);
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
        // stop this list of albums if there was another file launched in the meantime
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
        // stop this list of albums if there was another file launched in the meantime, another refresh will take place anyway
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

  @Override
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
      // Change local collection suggestions every 10 track plays
      if (comp % 10 == 0) {
        refreshLocalCollectionTabs();
      }
      comp++;
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
