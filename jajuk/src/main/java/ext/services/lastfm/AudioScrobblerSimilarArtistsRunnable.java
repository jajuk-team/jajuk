/**
 * aTunes 1.6.6
 * Copyright (C) 2006-2007 Alex Aranda (fleax) alex@atunes.org
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

package ext.services.lastfm;

import java.awt.Image;

import org.jajuk.util.ITechnicalStrings;

public class AudioScrobblerSimilarArtistsRunnable implements Runnable, ITechnicalStrings {

  private AudioScrobblerListener listener;

  private AudioScrobblerService service;

  private String artist;

  private boolean interrupted;

  public AudioScrobblerSimilarArtistsRunnable(AudioScrobblerListener listener,
      AudioScrobblerService service, String artist) {
    this.listener = listener;
    this.service = service;
    this.artist = artist;
  }

  public void run() {
    if (!interrupted && artist != null && !UNKNOWN_AUTHOR.equals(artist)) {
      AudioScrobblerSimilarArtists artists = service.getSimilarArtists(artist);

      if (!interrupted && artists != null) {
        Image artistImage = service.getImage(artists);
        if (!interrupted && artistImage != null)
          listener.notifyArtistImage(artistImage);

        for (int i = 0; i < artists.getArtists().size(); i++) {
          Image img = null;
          AudioScrobblerArtist a = artists.getArtists().get(i);
          if (!interrupted)
            img = service.getImage(a);

          if (!interrupted)
            listener.notifyFinishGetSimilarArtist(a, img);
        }
      }
    }
  }

  protected void interrupt() {
    interrupted = true;
  }

}
