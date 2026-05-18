/*
 * aTunes 1.14.0 code adapted by Jajuk team
 * 
 * Original copyright notice bellow : 
 * 
 * Copyright (C) 2006-2009 Alex Aranda, Sylvain Gaudard, Thomas Beckers and contributors
 *
 * See http://www.atunes.org/wiki/index.php?title=Contributing for information about contributors
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package org.jajuk.services.lastfm.model;

import java.awt.Image;

import javax.swing.SwingUtilities;

import org.jajuk.services.lastfm.LastFmService;
import org.apache.commons.lang3.StringUtils;
import org.jajuk.util.Messages;

/**
 * The Class LastFmSimilarArtistsRunnable.
 */
public class LastFmSimilarArtistsRunnable implements Runnable {
  /** The listener. */
  private final ContextListener listener;
  /** The service. */
  private final LastFmService service;
  /** The artist. */
  private final String artist;
  /** The interrupted. */
  private volatile boolean interrupted;
  /** The id. */
  private final long id;

  /**
   * Instantiates a new audio scrobbler similar artists runnable.
   * 
   * @param listener the listener
   * @param service the service
   * @param artist the artist
   * @param id the id
   */
  public LastFmSimilarArtistsRunnable(ContextListener listener, LastFmService service,
                                      String artist, long id) {
    this.listener = listener;
    this.service = service;
    this.artist = artist;
    this.id = id;
  }

  /**
   * Interrupt.
   */
  public void interrupt() {
    interrupted = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    if (!interrupted && StringUtils.isNotBlank(artist)
        && !artist.equalsIgnoreCase(Messages.getString("unknown_artist"))) {
      SimilarArtistsInfo similarArtistsInfo = service.getSimilarArtists(artist);
      if (!interrupted && similarArtistsInfo != null) {
        SwingUtilities.invokeLater(() -> listener.notifyStartRetrievingArtistImages(id));
        final ArtistInfo artistInfo = service.getArtist(artist);
        if (artistInfo != null) {
          final Image artistImage = service.getImage(artistInfo);
          if (!interrupted && artistImage != null) {
            SwingUtilities.invokeLater(() -> listener.notifyArtistImage(artistImage, id));
          }
        }
        for (ArtistInfo a : similarArtistsInfo.getArtists()) {
          if (interrupted) {
            break;
          }
          final Image img = service.getImage(a);
          SwingUtilities.invokeLater(() -> listener.notifyFinishGetSimilarArtist(a, img, id));
        }
      }
    }
  }
}
