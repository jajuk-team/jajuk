/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 *
 */

package org.jajuk.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.error.JajukException;

/**
 * Convenient class to manage Albums
 * 
 */
public class AlbumManager extends ItemManager implements Observer {
  /** Self instance */
  private static AlbumManager singleton;

  /** Album max rating */
  private long maxRate = 0l;

  int comp = 0;

  /**
   * No constructor available, only static access
   */
  private AlbumManager() {
    super();
    // register properties
    // ID
    registerProperty(new PropertyMetaInformation(XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(XML_NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(XML_EXPANDED, false, false, false, false, true,
        Boolean.class, false));
    // Register events
    ObservationManager.register(this);
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    return eventSubjectSet;
  }

  /**
   * @return singleton
   */
  public static AlbumManager getInstance() {
    if (singleton == null) {
      singleton = new AlbumManager();
    }
    return singleton;
  }

  /**
   * Register an Album
   * 
   * @param sName
   */
  public Album registerAlbum(String sName) {
    String sId = createID(sName);
    return registerAlbum(sId, sName);
  }

  /**
   * Return hashcode for this item
   * 
   * @param sName
   *          item name
   * @return ItemManager ID
   */
  protected static String createID(String sName) {
    return MD5Processor.hash(sName);
  }

  /**
   * Register an Album with a known id
   * 
   * @param sName
   */
  public Album registerAlbum(String sId, String sName) {
    synchronized (TrackManager.getInstance().getLock()) {
      Album album = (Album) hmItems.get(sId);
      if (album != null) {
        return album;
      }
      album = new Album(sId, sName);
      hmItems.put(sId, album);
      return album;
    }
  }

  /**
   * Change the item
   * 
   * @param old
   * @param sNewName
   * @return new album
   */
  public Album changeAlbumName(Album old, String sNewName) throws JajukException {
    // check there is actually a change
    if (old.getName2().equals(sNewName)) {
      return old;
    }
    Album newItem = registerAlbum(sNewName);
    // re apply old properties from old item
    newItem.cloneProperties(old);
    // update tracks
    for (Track track : TrackManager.getInstance().getTracks()) {
      if (track.getAlbum().equals(old)) {
        TrackManager.getInstance().changeTrackAlbum(track, sNewName, null);
      }
    }
    // if current track album name is changed, notify it
    if (FIFO.getInstance().getCurrentFile() != null
        && FIFO.getInstance().getCurrentFile().getTrack().getAlbum().equals(old)) {
      ObservationManager.notify(new Event(EventSubject.EVENT_ALBUM_CHANGED));
    }
    return newItem;
  }

  /**
   * Format the album name to be normalized :
   * <p>
   * -no underscores or other non-ascii characters
   * <p>
   * -no spaces at the begin and the end
   * <p>
   * -All in lower cas expect first letter of first word
   * <p>
   * exemple: "My album title"
   * 
   * @param sName
   * @return
   */
  public static synchronized String format(String sName) {
    String sOut;
    sOut = sName.trim(); // suppress spaces at the begin and the end
    sOut = sOut.replace('-', ' '); // move - to space
    sOut = sOut.replace('_', ' '); // move _ to space
    char c = sOut.charAt(0);
    StringBuilder sb = new StringBuilder(sOut);
    sb.setCharAt(0, Character.toUpperCase(c));
    return sb.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  public String getLabel() {
    return XML_ALBUMS;
  }

  /**
   * @param sID
   *          Item ID
   * @return Element
   */
  public Album getAlbumByID(String sID) {
    synchronized (getLock()) {
      return (Album) hmItems.get(sID);
    }
  }

  /**
   * 
   * @return albums list
   */
  public Set<Album> getAlbums() {
    Set<Album> albumSet = new LinkedHashSet<Album>();
    synchronized (getLock()) {
      for (Item item : getItems()) {
        albumSet.add((Album) item);
      }
    }
    return albumSet;
  }

  /**
   * Get albums associated with this item
   * 
   * @param item
   * @return
   */
  public Set<Album> getAssociatedAlbums(Item item) {
    synchronized (AlbumManager.getInstance().getLock()) {
      Set<Album> out = new TreeSet<Album>();
      // If item is a track, return albums containing this track
      if (item instanceof Track) {
        // we can return as a track has only one album
        if (item != null) {
          out.add(((Track) item).getAlbum());
        }
      } else {
        Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item);
        for (Track track : tracks) {
          out.add(track.getAlbum());
        }
      }
      return out;
    }
  }

  /**
   * Return top albums based on the average of each album rating
   * 
   * @param bHideUnmounted
   *          if true, unmounted albums are not chosen
   * @param iNbBestofAlbums
   *          nb of items to return
   * @return top albums, can be less items than required according to nb of
   *         available albums
   */
  public List<Album> getBestOfAlbums(boolean bHideUnmounted, int iNbBestofAlbums) {
    // create a temporary table to remove unmounted albums
    // We consider an album as mounted if a least one track is mounted
    // This hashmap contains album-> album rates
    final HashMap<Album, Float> cacheRate = new HashMap<Album, Float>(AlbumManager.getInstance()
        .getElementCount());
    // This hashmap contains album-> nb of tracks already taken into account
    // for average
    HashMap<Album, Integer> cacheNb = new HashMap<Album, Integer>(AlbumManager.getInstance()
        .getElementCount());
    for (Track track : TrackManager.getInstance().getTracks()) {
      if (track.getPlayeableFile(bHideUnmounted) != null) {
        float newRate = 0f;
        Integer nb = cacheNb.get(track.getAlbum());
        if (nb == null) {
          nb = 0;
        }
        Float previousRate = cacheRate.get(track.getAlbum());
        if (previousRate == null) {
          newRate = track.getRate();
        } else {
          newRate = ((previousRate * nb) + track.getRate()) / (nb + 1);
        }
        cacheNb.put(track.getAlbum(), nb + 1);
        cacheRate.put(track.getAlbum(), newRate);
      }
    }
    // Now sort albums by rating
    ArrayList<Album> sortedAlbums = new ArrayList<Album>(cacheRate.keySet());
    Collections.sort(sortedAlbums, new Comparator<Album>() {
      public int compare(Album o1, Album o2) {
        // lowest first
        return (int) (cacheRate.get(o1) - cacheRate.get(o2));
      }
    });
    return getTopAlbums(sortedAlbums, iNbBestofAlbums);
  }

  /**
   * Return newest albums
   * 
   * @param bHideUnmounted
   *          if true, unmounted albums are not chosen
   * @param iNb
   *          nb of items to return
   * @return newest albums
   */
  public List<Album> getNewestAlbums(boolean bHideUnmounted, int iNb) {
    // create a temporary table to remove unmounted albums
    // We consider an album as mounted if a least one track is mounted
    // This hashmap contains album-> discovery date
    final HashMap<Album, Date> cache = new HashMap<Album, Date>(AlbumManager.getInstance()
        .getElementCount());
    for (Track track : TrackManager.getInstance().getTracks()) {
      if (track.getPlayeableFile(bHideUnmounted) != null) {
        cache.put(track.getAlbum(), track.getDiscoveryDate());
      }
    }
    // Now sort albums by discovery date
    ArrayList<Album> sortedAlbums = new ArrayList<Album>(cache.keySet());
    Collections.sort(sortedAlbums, new Comparator<Album>() {
      public int compare(Album o1, Album o2) {
        return cache.get(o1).compareTo(cache.get(o2));
      }
    });
    return getTopAlbums(sortedAlbums, iNb);
  }

  /**
   * Return rarely listen albums
   * 
   * @param bHideUnmounted
   *          if true, unmounted albums are not chosen
   * @param iNb
   *          nb of items to return
   * @return top albums, can be less items than required according to nb of
   *         available albums
   */
  public List<Album> getRarelyListenAlbums(boolean bHideUnmounted, int iNb) {
    // create a temporary table to remove unmounted albums
    // We consider an album as mounted if a least one track is mounted
    // This hashmap contains album-> album hits (each track hit average)
    final HashMap<Album, Float> cache = new HashMap<Album, Float>(AlbumManager.getInstance()
        .getElementCount());
    // This hashmap contains album-> nb of tracks already taken into account
    // for average
    HashMap<Album, Integer> cacheNb = new HashMap<Album, Integer>(AlbumManager.getInstance()
        .getElementCount());
    for (Track track : TrackManager.getInstance().getTracks()) {
      if (track.getPlayeableFile(bHideUnmounted) != null) {
        float newHits = 0f;
        Integer nb = cacheNb.get(track.getAlbum());
        if (nb == null) {
          nb = 0;
        }
        Float previousRate = cache.get(track.getAlbum());
        if (previousRate == null) {
          newHits = track.getHits();
        } else {
          newHits = ((previousRate * nb) + track.getHits()) / (nb + 1);
        }
        cacheNb.put(track.getAlbum(), nb + 1);
        cache.put(track.getAlbum(), newHits);
      }
    }
    // Now sort albums by rating
    ArrayList<Album> sortedAlbums = new ArrayList<Album>(cache.keySet());
    Collections.sort(sortedAlbums, new Comparator<Album>() {
      public int compare(Album o1, Album o2) {
        // We inverte comparaison as we want lowest scores
        return (int) (cache.get(o2) - cache.get(o1));
      }
    });
    return getTopAlbums(sortedAlbums, iNb);
  }

  /**
   * Convenient method to keep top albums (used by getBestof, newest... albums)
   * 
   * @param sortedAlbums
   *          sorted albums according desired criteria, size >= iNb
   * @param iNb
   *          Number of albums to return
   * @return a nicely sorted / shuffled list of albums or a void list of none
   *         available albums
   */
  private List<Album> getTopAlbums(List<Album> sortedAlbums, int iNb) {
    // Keep only 3 * desired size or less if not enough available albums
    int size = 3 * iNb;
    if (sortedAlbums.size() <= size) {
      size = sortedAlbums.size() - 1;
    }
    // Leave if none album so far
    if (sortedAlbums.size() == 0) {
      return new ArrayList<Album>();
    }
    List<Album> sublist = sortedAlbums.subList(sortedAlbums.size() - (1 + size), sortedAlbums
        .size() - 1);
    // Shuffle the result
    Collections.shuffle(sublist);
    // The result is a sublist of shuffled albums, if we have less
    // albums than required, take max size possible
    List<Album> out = sublist.subList(0, (size >= iNb) ? iNb : size);
    return out;
  }

  /**
   * 
   * @return max rating for an album
   */
  public long getMaxRate() {
    return this.maxRate;
  }

  /**
   * Force to refresh the album max rating, it is not done soon as it is pretty
   * CPU consumming and we don't need a track by track rating precision
   */
  public void refreshMaxRating() {
    // create a temporary table to remove unmounted albums
    // We consider an album as mounted if a least one track is mounted
    // This hashmap contains album-> album rates
    final HashMap<Album, Float> cacheRate = new HashMap<Album, Float>(AlbumManager.getInstance()
        .getElementCount());
    // This hashmap contains album-> nb of tracks already taken into account
    // for average
    HashMap<Album, Integer> cacheNb = new HashMap<Album, Integer>(AlbumManager.getInstance()
        .getElementCount());
    for (Track track : TrackManager.getInstance().getTracks()) {
      if (track.getPlayeableFile(ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)) != null) {
        float newRate = 0f;
        Integer nb = cacheNb.get(track.getAlbum());
        if (nb == null) {
          nb = 0;
        }
        Float previousRate = cacheRate.get(track.getAlbum());
        if (previousRate == null) {
          newRate = track.getRate();
        } else {
          newRate = ((previousRate * nb) + track.getRate()) / (nb + 1);
        }
        cacheNb.put(track.getAlbum(), nb + 1);
        cacheRate.put(track.getAlbum(), newRate);
      }
    }
    // OK, now keep only the highest score
    for (Album album : cacheRate.keySet()) {
      long value = Math.round(cacheRate.get(album));
      if (value > maxRate) {
        maxRate = value;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(Event event) {
    if (event.getSubject() == EventSubject.EVENT_FILE_LAUNCHED) {
      // Compute album max rating every 10 tracks launches
      if (comp % 10 == 0) {
        refreshMaxRating();
      }
    }
  }

  /**
   * 
   * @param name
   * @return associated album (case insensitive) or null if no match
   */
  public Album getAlbumByName(String name) {
    Album out = null;
    for (Album album : getAlbums()) {
      if (album.getName().trim().toLowerCase().indexOf(name.trim().toLowerCase()) != -1) {
        out = album;
        break;
      }
    }
    return out;
  }

}
