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

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.jajuk.base.Artist;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.lastfm.LastFmService;
import org.jajuk.services.lastfm.model.AlbumInfo;
import org.jajuk.services.lastfm.model.ArtistInfo;
import org.jajuk.services.lastfm.model.SimilarArtistsInfo;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.helpers.TwoStepsDisplayable;
import org.jajuk.ui.thumbnails.LastFmArtistThumbnail;
import org.jajuk.util.*;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXBusyLabel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serial;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * Display Artist bio and albums.
 */
public class ArtistView extends SuggestionView implements TwoStepsDisplayable {
  /** Generated serialVersionUID. */
  @Serial
  private static final long serialVersionUID = 1L;

  private String bio;
  private ArtistInfo artistInfo;

  // Current artist name displayed (used for UI checks and equality)
  private String currentArtistName = null;

  // volatile is necessary for thread management between EDT and SwingWorkers
  private volatile String lastProcessedArtist = null;

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.views.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("ArtistView.0");
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

  /**
   * Build the GUI for a given artist
   * <p>
   * Must be called from the EDT
   * </p>.
   */
  private void displayArtist() {
    UtilGUI.populate(this);
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

        // Display a busy panel in the mean-time
        setLayout(new MigLayout("ins 5", "[grow]", "[grow]"));
        JXBusyLabel busy1 = new JXBusyLabel(new Dimension(50, 50));
        busy1.setBusy(true);
        removeAll();
        add(busy1, "center");
        revalidate();
        repaint();

        // CRITICAL: Update the volatile context BEFORE triggering heavy work.
        // This ensures that if the user switches tracks again quickly,
        // the running task will detect the change via 'lastProcessedArtist'.
        lastProcessedArtist = newArtistName;

        // Display the panel only if the artist is not unknown
        if (!artist.seemsUnknown()) {
          // This is done in a swing worker
          displayArtist();
        } else {
          reset();
        }

      }
    });
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

  /* (non-Javadoc)
   * @see org.jajuk.ui.helpers.TwoStepsDisplayable#longCall()
   */
  @Override
  public Object longCall() {
    // Capture the current context locally to compare against the volatile variable later
    String artistContext = lastProcessedArtist;

    // Basic validation
    if (artistContext == null || StringUtils.isBlank(artistContext)) {
      return null;
    }

    try {
      // 1. Fetch main Last.fm data (wiki and basic info)
      bio = LastFmService.getInstance().getWikiText(artistContext);

      // Verify context is still valid after API calls
      if (!lastProcessedArtist.equals(artistContext)) {
        return null;
      }

      artistInfo = LastFmService.getInstance().getArtist(artistContext);

      // Abort if no data available
      if (artistInfo == null || StringUtils.isBlank(artistInfo.getImageUrl())) {
        return null;
      }

      // Verify context is still valid after API calls
      if (!lastProcessedArtist.equals(artistContext)) {
        return null;
      }

      // 2. Download cover images for other albums
      albums = LastFmService.getInstance().getAlbumList(artistContext, true, 0);
      if (albums != null && !albums.getAlbums().isEmpty()) {
        for (AlbumInfo album : albums.getAlbums()) {
          // CRITICAL CHECK: Stop immediately if artist changed
          if (!lastProcessedArtist.equals(artistContext)) {
            break;
          }

          String coverUrl = album.getBigCoverURL();
          if (StringUtils.isNotBlank(coverUrl)) {
            URL remote = new URL(coverUrl);
            DownloadManager.downloadToCache(remote);
          }
        }
      }

      // Verify context again before moving to similar artists
      if (!lastProcessedArtist.equals(artistContext)) {
        return null;
      }

      // 3. Download cover images for similar artists
      SimilarArtistsInfo similarArtists = LastFmService.getInstance().getSimilarArtists(artistContext);
      if (similarArtists != null && similarArtists.getArtists() != null) {
        for (ArtistInfo similar : similarArtists.getArtists()) {
          // CRITICAL CHECK: Stop immediately if artist changed
          if (!lastProcessedArtist.equals(artistContext)) {
            break;
          }

          String similarUrl = similar.getImageUrl();
          if (StringUtils.isNotBlank(similarUrl)) {
            URL remote = new URL(similarUrl);
            DownloadManager.downloadToCache(remote);
          }
        }
      }

    } catch (UnknownHostException e) {
      Log.warn("Could not contact host for loading album information: " + e.getMessage());
    } catch (IOException e) {
      if (e.getMessage().contains(" 403 ")) {
        // Server returned error while fetching images
        Log.warn("Server returned an error while fetching images: " + e.getMessage());
      } else {
        // Other exception
        Log.error(e);
      }
    } catch (Exception e) {
      Log.error(e);
    }

    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.helpers.TwoStepsDisplayable#longCall()

  @Override
  public Object longCall() {
    // Call last.fm wiki
    bio = LastFmService.getInstance().getWikiText(artist);
    artistInfo = LastFmService.getInstance().getArtist(artist);
    // Prefetch artist thumbs
    try {
      preFetchOthersAlbum();
      // TODO too slow
      preFetchSimilarArtists();
    } catch (UnknownHostException e) {
      Log.warn("Could not contact host for loading album information: {{" + e.getMessage() + "}}");
    } catch (IOException e) {
      if (e.getMessage().contains(" 403 ")) {
        // server responded with code "forbidden"
        Log.warn("Server returned an error while fetching images: " + e.getMessage());
      } else {
        // other exception
        Log.error(e);
      }
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }
   */

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.helpers.TwoStepsDisplayable#shortCall(java.lang.Object)
   */
  @Override
  public void shortCall(Object in) {
    removeAll();
    JScrollPane jspAlbums = getLastFMSuggestionsPanel(SuggestionType.OTHERS_ALBUMS, true);
    // Artist unknown from last.fm, leav    JScrollPane jspAlbums = getLastFMSuggestionsPanel(SuggestionType.OTHERS_ALBUMS, true);e
    if (artistInfo == null
            // If image url is void, last.fm doesn't provide enough data about this
            // artist, we reset the view
            || StringUtils.isBlank(artistInfo.getImageUrl())) {
      reset();
      return;
    }
    // The artist picture + labels
    LastFmArtistThumbnail artistThumb = new LastFmArtistThumbnail(artistInfo);
    // No known icon next to artist thumb
    artistThumb.setArtistView(true);
    artistThumb.populate();
    // We set the margin this way, setMargin() doesn't work due to
    // existing border
    // The artist bio (from last.fm wiki)
    JTextArea jtaArtistDesc = new JTextArea(bio) {
      // We set the margin this way, setMargin() doesn't work due to
      // existing border
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
    JScrollPane jspWiki = new JScrollPane(jtaArtistDesc);
    jspWiki.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    jspWiki.setBorder(null);
    jspWiki.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    // Add items, layout is different according wiki text availability
    if (StringUtils.isNotBlank(jtaArtistDesc.getText())) {
      setLayout(new MigLayout("ins 5,gapy 5", "[grow]", "[grow][20%!][grow]"));
      add(artistThumb, "center,wrap");
      // don't add the textarea if no wiki text available
      add(jspWiki, "growx,wrap");
      add(jspAlbums, "grow,wrap");
    } else {
      setLayout(new MigLayout("ins 5,gapy 5", "[grow]"));
      add(artistThumb, "center,wrap");
      // don't add the textarea if no wiki text available
      add(jspAlbums, "grow,wrap");
    }
    revalidate();
    repaint();
  }
}
