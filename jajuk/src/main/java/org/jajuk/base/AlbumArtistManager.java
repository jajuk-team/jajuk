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
 *  $Revision: 5579 $
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
 * Convenient class to manage album-artists.
 */
public final class AlbumArtistManager extends ItemManager {

  /** Self instance. */
  private static AlbumArtistManager singleton = new AlbumArtistManager();

  /** List of all known album-artists */
  private static Vector<String> albumArtistsList = new Vector<String>(100); // NOPMD

  /**
   * No constructor available, only static access.
   */
  private AlbumArtistManager() {
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
  public static AlbumArtistManager getInstance() {
    return singleton;
  }

  /**
   * Register an albumArtist.
   *
   * @param sName The name of the albumArtist to search for.
   *
   * @return the albumArtist
   */
  public AlbumArtist registerAlbumArtist(String sName) {
    String sId = createID(sName);
    return registerAlbumArtist(sId, sName);
  }

  /**
   * Register an albumArtist with a known id.
   *
   * @param sName The name of the new albumArtist.
   * @param sId the ID of the new albumArtist.
   *
   * @return the albumArtist
   */
  public synchronized AlbumArtist registerAlbumArtist(String sId, String sName) {
    AlbumArtist albumArtist = getAlbumArtistByID(sId);
    // if we have this albumArtist already, simply return the existing one
    if (albumArtist != null) {
      return albumArtist;
    }
    albumArtist = new AlbumArtist(sId, sName);
    registerItem(albumArtist);
    // add it in genres list if new
    if (!albumArtistsList.contains(sName)) {
      albumArtistsList.add(albumArtist.getName2());
      // Sort items ignoring case
      Collections.sort(albumArtistsList, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          return o1.compareToIgnoreCase(o2);
        }
      });
    }

    return albumArtist;
  }

  /**
   * Change the item name.
   *
   * @param old The name of the albumArtist to update.
   * @param sNewName The new name of the albumArtist.
   *
   * @return The new Album-Instance.
   *
   * @throws JajukException Thrown if adjusting the name fails for some reason.
   */
  public AlbumArtist changeAlbumArtistName(AlbumArtist old, String sNewName) throws JajukException {
    synchronized (TrackManager.getInstance()) {
      // check there is actually a change
      if (old.getName2().equals(sNewName)) {
        return old;
      }

      // find out if the QueueModel is playing this track before we change the track!
      boolean queueNeedsUpdate = false;
      if (QueueModel.getPlayingFile() != null
          && QueueModel.getPlayingFile().getTrack().getAlbumArtist().equals(old)) {
        queueNeedsUpdate = true;
      }

      AlbumArtist newItem = registerAlbumArtist(sNewName);
      // re apply old properties from old item
      newItem.cloneProperties(old);

      // update tracks
      for (Track track : TrackManager.getInstance().getTracks()) {
        if (track.getAlbumArtist().equals(old)) {
          TrackManager.getInstance().changeTrackAlbumArtist(track, sNewName, null);
        }
      }
      // if current track albumArtist name is changed, notify it
      if (queueNeedsUpdate) {
        // We use the same event than for artists to keep things simple
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
    return Const.XML_ALBUM_ARTISTS;
  }

  /**
   * Gets the albumArtists list.
   *
   * @return albumArtists as a string list (used for albumArtists combos)
   */
  public static Vector<String> getAlbumArtistsList() {
    return albumArtistsList;
  }

  /**
   * Gets the albumArtist by id.
   *
   * @param sID Item ID
   *
   * @return Element
   */
  public AlbumArtist getAlbumArtistByID(String sID) {
    return (AlbumArtist) getItemByID(sID);
  }

  /**
   * Gets the albumArtists.
   *
   * @return ordered albums list
   */
  @SuppressWarnings("unchecked")
  public List<AlbumArtist> getAlbumArtists() {
    return (List<AlbumArtist>) getItems();
  }

  /**
   * Gets the albumArtists iterator.
   *
   * @return albumArtists iterator
   */
  @SuppressWarnings("unchecked")
  public synchronized ReadOnlyIterator<AlbumArtist> getAlbumArtistsIterator() {
    return new ReadOnlyIterator<AlbumArtist>((Iterator<AlbumArtist>) getItemsIterator());
  }

  /**
   * Get ordered list of albumArtists associated with this item.
   *
   * @param item The albumArtist-item to look for.
   *
   * @return the associated albumArtists
   */
  public synchronized List<AlbumArtist> getAssociatedAlbumArtists(Item item) {
    List<AlbumArtist> out;
    if (item instanceof Track) {
      out = new ArrayList<AlbumArtist>(1);
      out.add(((Track) item).getAlbumArtist());
    } else {
      // [Perf] If item is a track, just return its album-artist
      // Use a set to avoid dups
      Set<AlbumArtist> albumArtistSet = new HashSet<AlbumArtist>();

      List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item, true);
      for (Track track : tracks) {
        albumArtistSet.add(track.getAlbumArtist());
      }
      out = new ArrayList<AlbumArtist>(albumArtistSet);
      Collections.sort(out);
    }
    return out;
  }

  /**
   * Gets the albumArtist by name.
   *
   * @param name The name of the albumArtist.
   *
   * @return associated albumArtist (case insensitive) or null if no match
   */
  public AlbumArtist getAlbumArtistByName(String name) {
    AlbumArtist out = null;
    for (ReadOnlyIterator<AlbumArtist> it = getAlbumArtistsIterator(); it.hasNext();) {
      AlbumArtist albumArtist = it.next();
      if (albumArtist.getName().equals(name)) {
        out = albumArtist;
        break;
      }
    }
    return out;
  }
}
