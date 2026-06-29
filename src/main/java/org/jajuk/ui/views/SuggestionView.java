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

import java.awt.*;
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
import org.jajuk.services.lastfm.LastFmInvalidKeyException;
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
import org.jajuk.services.lastfm.model.AlbumInfo;
import org.jajuk.services.lastfm.model.AlbumListInfo;
import org.jajuk.services.lastfm.model.ArtistInfo;
import org.jajuk.services.lastfm.LastFmService;
import org.jajuk.services.lastfm.model.SimilarArtistsInfo;
import net.miginfocom.swing.MigLayout;

/**
 * Show suggested albums based on current collection (bestof, novelties) and
 * LAstFM.
 */
public class SuggestionView extends ViewAdapter {
  private JTabbedPane tabs;

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
  /** albums is protected to allow ArtistView to load them */
  protected AlbumListInfo albums;
  private SimilarArtistsInfo similar;
  JXBusyLabel busyLocal1 = new JXBusyLabel();
  JXBusyLabel busyLocal2 = new JXBusyLabel();
  JXBusyLabel busyLocal3 = new JXBusyLabel();
  JXBusyLabel busyLastFM1 = new JXBusyLabel();
  JXBusyLabel busyLastFM2 = new JXBusyLabel();

  // volatile is necessary for thread management between EDT and SwingWorkers
  private volatile String lastProcessedArtist = null;

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

    SwingWorker<Void, Void> sw = new SwingWorker<>() {
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
        if (!albums.isEmpty()) {
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
    if (newArtist.equals(lastProcessedArtist)) {
      return;
    }
    // Refresh immediately context before launching heavy task
    this.lastProcessedArtist = newArtist;

    // Capture the current context locally to compare against the volatile variable later
    final String artistContext = newArtist;

    // Display a busy panel in the mean-time
    SwingUtilities.invokeLater(() -> {
      busyLastFM1.setBusy(true);
      busyLastFM2.setBusy(true);
      tabs.setComponentAt(3, UtilGUI.getCentredPanel(busyLastFM1));
      tabs.setComponentAt(4, UtilGUI.getCentredPanel(busyLastFM2));
    });

    // Use a swing worker as construct takes a lot of time
    // --- WORKER For other albums (Panel 3) ---
    SwingWorker<Void, AlbumInfo> albumWorker = new SwingWorker<>() {
      @Override
      protected Void doInBackground() throws Exception {
        // Check if the artist context has change
        if (!artistContext.equals(lastProcessedArtist)) {
          return null; // Stop everything, new artist is given priority
        }

        albums = LastFmService.getInstance().getAlbumList(artistContext, true, 0);

        // Check post API call
        if (!artistContext.equals(lastProcessedArtist)) {
          return null;
        }

        if (albums != null && !albums.getAlbums().isEmpty()) {
          for (AlbumInfo album : albums.getAlbums()) {
            // Real IMPORTANT check
            if (!artistContext.equals(lastProcessedArtist)) {
              break; // Stopping download loop
            }

            String albumUrl = album.getBigCoverURL();
            if (StringUtils.isNotBlank(albumUrl)) {
              // 1 Download
              URL remote = new URL(albumUrl);
              DownloadManager.downloadToCache(remote);
            }
            // 2 Publish raw data
            publish(album);
          }
        }
        return null;
      }

      @Override
      protected void process(List<AlbumInfo> chunks) {
        // Processed in EDT (Thread UI)
        Component comp = tabs.getComponentAt(3);
        FlowScrollPanel flowPanel = null;

        // Step 1 : Check if the panel is in "Busy" state
        boolean isBusyState = false;
        if (comp instanceof JPanel) {
          JPanel panel = (JPanel) comp;
          // Looking for JXBusyLabel
          for (int i = 0; i < panel.getComponentCount(); i++) {
            if (panel.getComponent(i) instanceof JXBusyLabel) {
              isBusyState = true;
              break;
            }
          }
        }
        if (isBusyState) {
          // Step 2 creation of a new scroll panel
          flowPanel = new FlowScrollPanel();
          flowPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
          JScrollPane jsp = new JScrollPane(flowPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
          jsp.setBorder(null);
          flowPanel.setScroller(jsp);
          tabs.setComponentAt(3, jsp);
        } else {
          // Step 2 get current scroll panel
          JScrollPane jsp = (JScrollPane) comp;
          Component view = jsp.getViewport().getView();
          if (view instanceof FlowScrollPanel) {
            flowPanel = (FlowScrollPanel) view;
          }
        }

        if (flowPanel != null) {
          // Step 3 component creation in UI (EDT)
          for (AlbumInfo album : chunks) {
            AbstractThumbnail thumb = new LastFmAlbumThumbnail(album);
            thumb.setArtistView(false);
            thumb.populate();

            if (thumb.getIcon() != null) {
              thumb.getIcon().addMouseListener(new ThumbMouseListener());
            }
            flowPanel.add(thumb);
          }
          flowPanel.revalidate();
          flowPanel.repaint();
        }
      }

      @Override
      protected void done() {
        busyLastFM1.setBusy(false);
      }
    };

    // --- WORKER for similar artists (Panel 4) ---
    SwingWorker<Void, ArtistInfo> artistWorker = new SwingWorker<>() {
      @Override
      protected Void doInBackground() throws Exception {
        try {
          // Check if the artist context has change
          if (!artistContext.equals(lastProcessedArtist)) {
            return null; // Stop everything, new artist is given priority
          }

          similar = LastFmService.getInstance().getSimilarArtists(artistContext);
          if (similar != null && similar.getArtists() != null && !similar.getArtists().isEmpty()) {
            for (ArtistInfo similarArtist : similar.getArtists()) {
              // Real IMPORTANT check
              if (!artistContext.equals(lastProcessedArtist)) {
                break; // Stopping download loop
              }

              String artistUrl = similarArtist.getImageUrl();
              if (StringUtils.isBlank(artistUrl))
                continue;

              // 1. Heavy task / download
              URL remote = new URL(artistUrl);
              DownloadManager.downloadToCache(remote);

              // 2. Publish raw data
              publish(similarArtist);
            }
          }
        } catch (LastFmInvalidKeyException e) {
          Log.error(e);
        }
        return null;
      }

      @Override
      protected void process(List<ArtistInfo> chunks) {
        // Processed in EDT (Thread UI)

        Component comp = tabs.getComponentAt(4);
        FlowScrollPanel flowPanel = null;
        // Step 1 : Check if the panel is in "Busy" state
        boolean isBusyState = false;
        if (comp instanceof JPanel) {
          JPanel panel = (JPanel) comp;
          // Looking for JXBusyLabel
          for (int i = 0; i < panel.getComponentCount(); i++) {
            if (panel.getComponent(i) instanceof JXBusyLabel) {
              isBusyState = true;
              break;
            }
          }
        }
        if (isBusyState) {
          // Step 2 creation of a new scroll panel
          flowPanel = new FlowScrollPanel();
          flowPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
          JScrollPane jsp = new JScrollPane(flowPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
          jsp.setBorder(null);
          flowPanel.setScroller(jsp);
          tabs.setComponentAt(4, jsp);
        } else {
          // Step 2 get current scroll panel
          JScrollPane jsp = (JScrollPane) comp;
          Component view = jsp.getViewport().getView();
          if (view instanceof FlowScrollPanel) {
            flowPanel = (FlowScrollPanel) view;
          }
        }

        if (flowPanel != null) {
          // Step 3 component creation in UI (EDT)
          for (ArtistInfo similarArtist : chunks) {
            // 3. CRÉATION DU COMPOSANT UI ICI (dans l'EDT)
            AbstractThumbnail thumb = new LastFmArtistThumbnail(similarArtist);
            thumb.setArtistView(false);
            thumb.populate();

            if (thumb.getIcon() != null) {
              thumb.getIcon().addMouseListener(new ThumbMouseListener());
              flowPanel.add(thumb);
            }
          }
          flowPanel.revalidate();
          flowPanel.repaint();
        }
      }

      @Override
      protected void done() {
        busyLastFM2.setBusy(false);
      }
    };

    albumWorker.execute();
    artistWorker.execute();
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
    if (albums != null && !albums.isEmpty()) {
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
      if (albums != null && !albums.getAlbums().isEmpty()) {
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
