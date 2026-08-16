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
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.jajuk.base.Artist;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.lastfm.LastFmInvalidKeyException;
import org.jajuk.services.lastfm.LastFmService;
import org.jajuk.services.lastfm.model.AlbumInfo;
import org.jajuk.services.lastfm.model.AlbumListInfo;
import org.jajuk.services.lastfm.model.ArtistInfo;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.thumbnails.AbstractThumbnail;
import org.jajuk.ui.thumbnails.LastFmAlbumThumbnail;
import org.jajuk.ui.thumbnails.LastFmArtistThumbnail;
import org.jajuk.util.*;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXBusyLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Display Artist bio and albums.
 */
public class ArtistView extends ViewAdapter {
  /** Generated serialVersionUID. */
  @Serial
  private static final long serialVersionUID = 1L;

  private String bio;
  private ArtistInfo artistInfo;
  private AlbumListInfo albums;

  // Current artist name displayed (used for UI checks and equality)
  private String currentArtistName = null;

  // volatile is necessary for thread management between EDT and SwingWorkers
  private volatile String lastProcessedArtist = null;

  /** Currently selected thumb. */
  AbstractThumbnail selectedThumb;

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

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.views.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("ArtistView.0");
  }

  /**
   * Gets the nothing found panel.
   *
   * @return a panel with text explaining why no item has been found
   */
  public JPanel getNothingFoundPanel() {
    JPanel out = new JPanel(new MigLayout("ins 5", "grow"));
    JEditorPane jteNothing = new JEditorPane("text/html", Messages.getString("SuggestionView.7"));
    jteNothing.setBorder(null);
    jteNothing.setEditable(false);
    jteNothing.setOpaque(false);
    jteNothing.setToolTipText(Messages.getString("SuggestionView.7"));
    out.add(jteNothing, "center,grow");
    return out;
  }

  /**
   * Return the result panel for lastFM information.
   *
   * @return the last fm suggestions panel
   */
  JScrollPane createLastFMAlbumsPanel(AlbumListInfo albums) {
    FlowScrollPanel flowPanel = new FlowScrollPanel();
    JScrollPane jsp = new JScrollPane(flowPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    jsp.setBorder(null);
    flowPanel.setScroller(jsp);

    flowPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

    if (albums != null && !albums.getAlbums().isEmpty()) {
      for (AlbumInfo album : albums.getAlbums()) {
        AbstractThumbnail thumb = new LastFmAlbumThumbnail(album);
        thumb.setArtistView(false);
        thumb.populate();
        if (thumb.getIcon() != null) {
          thumb.getIcon().addMouseListener(new ThumbMouseListener());
          flowPanel.add(thumb);
        }
      }
    } else {
      JXBusyLabel busy1 = new JXBusyLabel(new Dimension(50, 50));
      busy1.setBusy(true);
      flowPanel.add(busy1);
    }
    return jsp;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.views.IView#initUI()
   */
  @Override
  public void initUI() {
    // register to player events
    ObservationManager.register(this);
    // by default, show reseted view
    reset();
    // Update initial status
    UtilFeatures.updateStatus(this);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<>();
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
   */
  @Override
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(() -> {
      // If internet access or lastfm is disable, just reset
      if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)
              || !Conf.getBoolean(Const.CONF_LASTFM_INFO)) {
        reset();
        return;
      }
      JajukEvents subject = event.getSubject();
      if (JajukEvents.WEBRADIO_LAUNCHED.equals(subject)
              || JajukEvents.ZERO.equals(event.getSubject())) {
        reset();
      } else if (JajukEvents.FILE_LAUNCHED.equals(subject)) {

        refreshArtist();
      }
    });
  }

  private void refreshArtist() {
    // If no playing track, reset the view
    StackItem currentItem = QueueModel.getCurrentItem();
    if (currentItem == null) {
      reset();
      return;
    }

    Artist artist = currentItem.getFile().getTrack().getArtist();
    String newArtistName = artist.getName();

    // If we already display the artist, leave (avoid redundant updates)
    if (newArtistName.equals(currentArtistName)) {
      return;
    }

    // New artist detected
    currentArtistName = newArtistName;

    // CRITICAL: Update the volatile context BEFORE triggering heavy work.
    // This ensures that if the user switches tracks again quickly,
    // the running task will detect the change via 'lastProcessedArtist'.
    lastProcessedArtist = newArtistName;

    // Display the panel only if the artist is not unknown
    if (!artist.seemsUnknown()) {
      displayArtist();
    } else {
      reset();
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.views.SuggestionView#onPerspectiveSelection()
   */
  @Override
  public void onPerspectiveSelection() {
    // override the suggestion view behavior
  }

  /**
   * Show reseted view (show a message)
   * <p>
   * Must be called from the EDT
   * </p>.
   */
  private void reset() {
    this.lastProcessedArtist = null;
    removeAll();
    setLayout(new MigLayout("ins 5,gapy 5", "[grow]"));
    add(getNothingFoundPanel());
    revalidate();
    repaint();
  }

  public void callWiki() {
    Log.info("Get Wiki for artist: " + lastProcessedArtist);
    final String artistContext = lastProcessedArtist;
    if (artistContext == null || StringUtils.isBlank(artistContext)) return;

    new SwingWorker<Void, Void>() {
      private String fetchedBio;
      private ArtistInfo fetchedArtistInfo;

      @Override
      protected Void doInBackground() {
        // Network calls outside EDT
        fetchedBio = LastFmService.getInstance().getWikiText(artistContext);

        if (!lastProcessedArtist.equals(artistContext)) return null;

        fetchedArtistInfo = LastFmService.getInstance().getArtist(artistContext);

        return null;
      }

      @Override
      protected void done() {
        // Back to EDT onlfy for UI update
        if (!lastProcessedArtist.equals(artistContext)) return;
        if (fetchedArtistInfo == null || StringUtils.isBlank(fetchedArtistInfo.getImageUrl())) return;

        bio = fetchedBio;
        artistInfo = fetchedArtistInfo;

        try {
          // Fetch main Last.fm data (wiki and basic info)
          LastFmArtistThumbnail artistThumb = new LastFmArtistThumbnail(artistInfo);
          artistThumb.setArtistView(true);
          artistThumb.populate();

          JScrollPane jspWiki = createJScrollPaneWiki();
          JScrollPane albumsPanel = createLastFMAlbumsPanel(null);

          removeAll();
          if (jspWiki != null) {
            setLayout(new MigLayout("ins 5,gapy 5", "[grow]", "[grow][20%!][grow]"));
            add(artistThumb, "center,wrap");
            add(jspWiki, "growx,wrap");
            add(albumsPanel, "growx,span,h 200!,wrap");
          } else {
            setLayout(new MigLayout("ins 5,gapy 5", "[grow]"));
            add(artistThumb, "center,wrap");
            add(albumsPanel, "growx,span,h 200!,wrap");
          }
          revalidate();
          repaint();

          // Get Albums covers progressively in background
          downloadAlbumCoversProgressively();

        } catch (Exception e) {
          Log.error(e);
        }
      }
    }.execute();
  }

  public void displayArtist() {
    // Prepare the UI for artist info and albums, show busy indicators while data is fetched
    removeAll();
    JXBusyLabel busy1 = new JXBusyLabel(new Dimension(50, 50));
    busy1.setBusy(true);
    setLayout(new MigLayout("ins 5,gapy 5", "[grow]"));
    add(busy1, "center,wrap");
    revalidate();
    repaint();

    // Get Wiki Info
    SwingUtilities.invokeLater(this::callWiki);
  }

  private JScrollPane createJScrollPaneWiki() {
    JScrollPane jspWiki = null;
    // Check if we have wiki text
    if (StringUtils.isNotBlank(bio)) {

      JTextArea jtaArtistDesc = new JTextArea(bio) {
        @Override
        public Insets getInsets() {
          return new Insets(2, 4, 0, 4);
        }
      };
      jtaArtistDesc.setBorder(null);
      jtaArtistDesc.setEditable(false);
      jtaArtistDesc.setLineWrap(true);
      jtaArtistDesc.setWrapStyleWord(true);
      jtaArtistDesc.setOpaque(false);

      jspWiki = new JScrollPane(jtaArtistDesc);
      jspWiki.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      jspWiki.setBorder(null);
      jspWiki.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    }
    return jspWiki;
  }

  /**
   * Download album cover images progressively in background.
   * Updates the UI as each image is downloaded by recreating album thumbnails.
   */
  private void downloadAlbumCoversProgressively() {
    final String artistContext = lastProcessedArtist;
    if (artistContext == null) return;

    new SwingWorker<Void, Integer>() {
      private static final int REFRESH_EVERY = 2;

      @Override
      protected Void doInBackground() {
        try {
          // Fetch album list metadata
          albums = LastFmService.getInstance().getAlbumList(artistContext, true, 0);
        } catch (LastFmInvalidKeyException e) {
          Log.error(e);
          return null;
        }

        if (!lastProcessedArtist.equals(artistContext)) return null;
        if (albums == null || albums.getAlbums().isEmpty()) return null;

        for (int i = 0; i < albums.getAlbums().size(); i++) {
          AlbumInfo album = albums.getAlbums().get(i);

          // CRITICAL CHECK: Stop immediately if artist changed
          if (!lastProcessedArtist.equals(artistContext)) {
            Log.debug("Album download interrupted: artist changed");
            break;
          }

          String coverUrl = album.getBigCoverURL();
          if (StringUtils.isNotBlank(coverUrl)) {
            try {
              DownloadManager.downloadToCache(new URL(coverUrl));
            } catch (Exception e) {
              Log.error(e);
            }
          }

          // Publish progress every REFRESH_EVERY images
          if ((i + 1) % REFRESH_EVERY == 0 || i == albums.getAlbums().size() - 1) {
            publish(i + 1);
          }
        }
        return null;
      }

      @Override
      protected void process(java.util.List<Integer> chunks) {
        // Recreate the albums panel with fresh thumbnails that have access to cached images
        if (lastProcessedArtist.equals(artistContext)) {
          updateAlbumsPanel();
        }
      }

      @Override
      protected void done() {
        // Final refresh when all images are downloaded
        if (lastProcessedArtist.equals(artistContext)) {
          Log.debug("Album covers downloaded for: " + artistContext);
          updateAlbumsPanel();
        }
      }
    }.execute();
  }

  /**
   * Helper method to update the albums panel with fresh thumbnails.
   * Must be called from EDT.
   */
  private void updateAlbumsPanel() {
    if (albums == null) {
      return;
    }

    // Simply recreate and update the albums scroll pane component
    // Since it's stored in a reference, we can replace it in place
    JScrollPane albumsPanel = createLastFMAlbumsPanel(albums);

    // Find the component in the current layout and replace it
    Component[] comps = getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof JScrollPane scrollPane) {
        Component viewport = scrollPane.getViewport().getView();

        // Check if this is likely an album panel (not the wiki text panel)
        // by checking if it's a FlowScrollPanel
        if (viewport != null && viewport.getClass().getSimpleName().contains("FlowScrollPanel")) {
          // Replace this component
          remove(i);
          add(albumsPanel, "growx,span,h 180!,wrap", i);
          revalidate();
          repaint();
          break;
        }
      }
    }
  }
}
