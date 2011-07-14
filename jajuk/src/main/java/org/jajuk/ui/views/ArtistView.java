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

import ext.services.lastfm.ArtistInfo;
import ext.services.lastfm.LastFmService;

import java.awt.Dimension;
import java.awt.Insets;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.Artist;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.helpers.TwoStepsDisplayable;
import org.jajuk.ui.thumbnails.LastFmArtistThumbnail;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXBusyLabel;

/**
 * Display Artist bio and albums.
 */
public class ArtistView extends SuggestionView implements TwoStepsDisplayable {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The artist picture + labels. */
  private LastFmArtistThumbnail artistThumb;

  /** The artist bio (from last.fm wiki) */
  private JTextArea jtaArtistDesc;

  /** DOCUMENT_ME. */
  private JScrollPane jspAlbums;

  /** DOCUMENT_ME. */
  private String bio;

  /** DOCUMENT_ME. */
  private ArtistInfo artistInfo;

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
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
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
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
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
          // If we already display the artist, leave
          if (artist.getName().equals(ArtistView.this.artist)) {
            return;
          } else {
            // Display a busy panel in the mean-time
            setLayout(new MigLayout("ins 5", "[grow]", "[grow]"));
            JXBusyLabel busy1 = new JXBusyLabel(new Dimension(50, 50));
            busy1.setBusy(true);
            removeAll();
            add(busy1, "center");
            revalidate();
            repaint();

            ArtistView.this.artist = artist.getName();
            // Display the panel only if the artist is not unknown
            if (!artist.seemsUnknown()) {
              // This is done in a swing worker
              displayArtist();
            } else {
              reset();
            }
          }
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
    ArtistView.this.artist = null;
    removeAll();
    setLayout(new MigLayout("ins 5,gapy 5", "[grow]"));
    add(getNothingFoundPanel());
    revalidate();
    repaint();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.helpers.TwoStepsDisplayable#longCall()
   */
  @Override
  public Object longCall() {
    // Call last.fm wiki
    bio = LastFmService.getInstance().getWikiText(artist);
    artistInfo = LastFmService.getInstance().getArtist(artist);
    // Prefetch artist thumbs
    try {
      preFetchOthersAlbum();
      preFetchSimilarArtists();
    } catch (UnknownHostException e) {
      Log.warn("Could not contact host for loading album information: {{" + e.getMessage() + "}}");
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.helpers.TwoStepsDisplayable#shortCall(java.lang.Object)
   */
  @Override
  public void shortCall(Object in) {
    removeAll();
    jspAlbums = getLastFMSuggestionsPanel(SuggestionType.OTHERS_ALBUMS, true);
    // Artist unknown from last.fm, leave
    if (artistInfo == null
    // If image url is void, last.fm doesn't provide enough data about this
        // artist, we reset the view
        || StringUtils.isBlank(artistInfo.getImageUrl())) {
      reset();
      return;
    }
    artistThumb = new LastFmArtistThumbnail(artistInfo);
    // No known icon next to artist thumb
    artistThumb.setArtistView(true);
    artistThumb.populate();

    jtaArtistDesc = new JTextArea(bio) {
      private static final long serialVersionUID = 9217998016482118852L;

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
