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
import java.util.List;
import java.util.StringTokenizer;

import org.jajuk.base.Track;
import org.jajuk.util.log.Log;

public class AudioScrobblerAlbumsRunnable implements Runnable {

  private AudioScrobblerListener listener;
  private AudioScrobblerService service;
  private Track track;

  private boolean interrupted;

  private boolean retrieveArtistInfo = true;

  protected AudioScrobblerAlbumsRunnable(AudioScrobblerListener listener,
      AudioScrobblerService service, Track track) {
    this.listener = listener;
    this.service = service;
    this.track = track;
  }

  public void run() {
    listener.setLastAlbumRetrieved(null);
    listener.setLastArtistRetrieved(null);

    AudioScrobblerAlbum album = null;
    List<AudioScrobblerAlbum> albums = null;
    if (!interrupted) {
      album = service.getAlbum(track.getAuthor().getName2(), track.getAlbum().getName2());
      if (album != null) {
        listener.setAlbum(album);
        Image image = service.getImage(album);
        listener.setImage(image);
        if (image != null) {
          listener.savePicture(image, track);
        }
      }
    }
    if (album != null && !interrupted) {
      listener.notifyAlbumRetrieved(track);
    }

    try {
      Thread.sleep(1000); // Wait a second to prevent IP banning
    } catch (InterruptedException e) {
      Log.error(e);
    }

    // If we have to retrieve artist info do it. If not, get previous retrieved
    // albums list
    if (retrieveArtistInfo) {
      if (!interrupted) {
        if (!track.getAuthor().isUnknown()) {
          albums = service.getAlbumList(track.getAuthor().getName2());
        }
        if (albums == null) {
          interrupted = true;
        }
        listener.setAlbums(albums);
      }
    } else {
      albums = listener.getAlbums();
    }

    if (album == null && albums != null && !interrupted) {
      // Try to find an album which fits
      AudioScrobblerAlbum auxAlbum = null;
      int i = 0;
      while (!interrupted && auxAlbum == null && i < albums.size()) {
        AudioScrobblerAlbum a = albums.get(i);
        StringTokenizer st = new StringTokenizer(a.getTitle(), " ");
        boolean matches = true;
        int tokensAnalyzed = 0;
        while (st.hasMoreTokens() && matches) {
          String t = st.nextToken();
          if (forbiddenToken(t)) { // Ignore album if contains forbidden chars
            matches = false;
            break;
          }
          if (!validToken(t)) { // Ignore tokens without alphanumerics
            if (tokensAnalyzed == 0 && !st.hasMoreTokens()) {// Only this token
              matches = false;
            }
            else {
              continue;
            }
          }
          if (!track.getAlbum().getName2().toLowerCase().contains(t.toLowerCase())) {
            matches = false;
          }
          tokensAnalyzed++;
        }
        if (matches) {
          auxAlbum = a;
        }
        i++;
      }
      if (!interrupted && auxAlbum != null) {
        auxAlbum = service.getAlbum(auxAlbum.getArtist(), auxAlbum.getTitle());
        if (auxAlbum != null) {
          listener.setAlbum(auxAlbum);
          Image image = service.getImage(auxAlbum);
          listener.setImage(image);
          if (image != null) {
            listener.savePicture(image, track);
          }
        }
      }
      if (!interrupted && auxAlbum != null) {
        listener.notifyAlbumRetrieved(track);
      }
    }
  }

  private boolean forbiddenToken(String t) {
    return t.contains("/");
  }

  private boolean validToken(String t) {
    return t.matches("[A-Za-z]+");
    // t.contains("(") || t.contains(")")
  }

  protected void interrupt() {
    interrupted = true;
  }

  public void setRetrieveArtistInfo(boolean retrieveArtistInfo) {
    this.retrieveArtistInfo = retrieveArtistInfo;
  }
}
