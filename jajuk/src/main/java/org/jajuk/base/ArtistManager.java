/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

package org.jajuk.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Const;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.error.JajukException;

/**
 * Convenient class to manage artists.
 */
public final class ArtistManager extends ItemManager {

  /** Self instance. */
  private static ArtistManager singleton = new ArtistManager();

  /** List of all known artists */
  private Vector<String> artistsList = new Vector<String>(100); // NOPMD

  /**
   * No constructor available, only static access.
   * Not private to allow AlbumArtistManager extends
   */
  ArtistManager() {
    super();
    // register properties
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(Const.XML_EXPANDED, false, false, false, false,
        true, Boolean.class, false));
  }

  /**
   * Gets the instance.
   *
   * @return singleton
   */
  public static ArtistManager getInstance() {
    return singleton;
  }

  /**
   * Register an artist.
   *
   * @param sName The name of the artist to search for.
   *
   * @return the artist
   */
  public Artist registerArtist(String sName) {
    String sId = createID(sName);
    return registerArtist(sId, sName);
  }

  /**
   * Register an artist with a known id.
   *
   * @param sName The name of the new artist.
   * @param sId the ID of the new artist.
   *
   * @return the artist
   */
  public synchronized Artist registerArtist(String sId, String sName) {
    Artist artist = getArtistByID(sId);
    // if we have this artist already, simply return the existing one
    if (artist != null) {
      return artist;
    }
    artist = new Artist(sId, sName);
    registerItem(artist);
    // add it in genres list if new
    if (!artistsList.contains(sName)) {
      artistsList.add(artist.getName2());
      // Sort items ignoring case
      Collections.sort(artistsList, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          return o1.compareToIgnoreCase(o2);
        }
      });
    }

    return artist;
  }

  /**
   * Change the item name.
   *
   * @param old The name of the artist to update.
   * @param sNewName The new name of the artist.
   *
   * @return The new Album-Instance.
   *
   * @throws JajukException Thrown if adjusting the name fails for some reason.
   */
  public Artist changeArtistName(Artist old, String sNewName) throws JajukException {
    synchronized (TrackManager.getInstance()) {
      // check there is actually a change
      if (old.getName2().equals(sNewName)) {
        return old;
      }

      // find out if the QueueModel is playing this track before we change the track!
      boolean queueNeedsUpdate = false;
      if (QueueModel.getPlayingFile() != null
          && QueueModel.getPlayingFile().getTrack().getArtist().equals(old)) {
        queueNeedsUpdate = true;
      }

      Artist newItem = registerArtist(sNewName);
      // re apply old properties from old item
      newItem.cloneProperties(old);

      // update tracks
      for (Track track : TrackManager.getInstance().getTracks()) {
        if (track.getArtist().equals(old)) {
          TrackManager.getInstance().changeTrackArtist(track, sNewName, null);
        }
      }

      // if current track artist name is changed, notify it
      if (queueNeedsUpdate) {
        ObservationManager.notify(new JajukEvent(JajukEvents.ARTIST_CHANGED));
      }

      return newItem;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return Const.XML_ARTISTS;
  }

  /**
   * Gets the artists list.
   *
   * @return artists as a string list (used for artists combos)
   */
  public static Vector<String> getArtistsList() {
    return getInstance().artistsList;
  }

  /**
   * Gets the artist by id.
   *
   * @param sID Item ID
   *
   * @return Element
   */
  public Artist getArtistByID(String sID) {
    return (Artist) getItemByID(sID);
  }

  /**
   * Gets the artists.
   *
   * @return ordered albums list
   */
  @SuppressWarnings("unchecked")
  public List<Artist> getArtists() {
    return (List<Artist>) getItems();
  }

  /**
   * Gets the artists iterator.
   *
   * @return artists iterator
   */
  @SuppressWarnings("unchecked")
  public synchronized ReadOnlyIterator<Artist> getArtistsIterator() {
    return new ReadOnlyIterator<Artist>((Iterator<Artist>) getItemsIterator());
  }

  /**
   * Get ordered list of artists associated with this item.
   *
   * @param item The artist item to look for.
   *
   * @return the associated artists
   */
  public synchronized List<Artist> getAssociatedArtists(Item item) {
    List<Artist> out;
    if (item instanceof Track) {
      out = new ArrayList<Artist>(1);
      out.add(((Track) item).getArtist());
    } else {
      // [Perf] If item is a track, just return its artist
      // Use a set to avoid dups
      Set<Artist> artistSet = new HashSet<Artist>();

      List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item, true);
      for (Track track : tracks) {
        artistSet.add(track.getArtist());
      }
      out = new ArrayList<Artist>(artistSet);
      Collections.sort(out);
    }
    return out;
  }

  /**
   * Gets the artist by name.
   *
   * @param name The name of the artist.
   *
   * @return associated artist (case insensitive) or null if no match
   */
  public Artist getArtistByName(String name) {
    Artist out = null;
    for (ReadOnlyIterator<Artist> it = getArtistsIterator(); it.hasNext();) {
      Artist artist = it.next();
      if (artist.getName().equals(name)) {
        out = artist;
        break;
      }
    }
    return out;
  }
}
