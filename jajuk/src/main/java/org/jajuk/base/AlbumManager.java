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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.error.JajukException;

/**
 * Convenient class to manage Albums.
 */
public final class AlbumManager extends ItemManager implements Observer {

  /** Self instance. */
  private static AlbumManager singleton = new AlbumManager();

  /** Album max rating. */
  private long maxRate = 0l;

  /** DOCUMENT_ME. */
  private int comp = 0;

  /**
   * No constructor available, only static access.
   */
  private AlbumManager() {
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
    // Cover path
    registerProperty(new PropertyMetaInformation(Const.XML_ALBUM_COVER, false, false, false, false,
        false, String.class, null));
    // Disc id
    registerProperty(new PropertyMetaInformation(Const.XML_ALBUM_DISC_ID, false, true, true, false,
        false, Long.class, -1l));
    // Register events
    ObservationManager.register(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    return eventSubjectSet;
  }

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static AlbumManager getInstance() {
    return singleton;
  }

  /**
   * Return hashcode for this item.
   * 
   * @param sName item name
   * @param discId DOCUMENT_ME
   * 
   * @return ItemManager ID
   */
  protected static String createID(String sName, long discId) {
    return MD5Processor.hash(sName + discId);
  }

  /**
   * Register an Album with a known id.
   * 
   * @param sName DOCUMENT_ME
   * @param sId DOCUMENT_ME
   * @param discID DOCUMENT_ME
   * 
   * @return the album
   */
  public Album registerAlbum(String sId, String sName, long discID) {
    Album album = getAlbumByID(sId);
    if (album != null) {
      return album;
    }
    album = new Album(sId, sName, discID);
    registerItem(album);
    return album;
  }

  /**
   * Register an Album.
   * 
   * @param sName DOCUMENT_ME
   * @param sAlbumArtist DOCUMENT_ME
   * @param discID DOCUMENT_ME
   * 
   * @return the album
   */
  public Album registerAlbum(String sName, long discID) {
    String sId = createID(sName, discID);
    return registerAlbum(sId, sName, discID);
  }

  /**
   * Change the item.
   * 
   * @param old DOCUMENT_ME
   * @param sNewName DOCUMENT_ME
   * 
   * @return new album
   * 
   * @throws JajukException the jajuk exception
   */
  public Album changeAlbumName(Album old, String sNewName) throws JajukException {
    // check there is actually a change
    if (old.getName2().equals(sNewName)) {
      return old;
    }

    // check up front as later the state of the track is already changed
    boolean bQueueUpdateRequired = false;
    if (QueueModel.getPlayingFile() != null
        && QueueModel.getPlayingFile().getTrack().getAlbum().equals(old)) {
      bQueueUpdateRequired = true;
    }

    Album newItem = registerAlbum(sNewName, old.getDiscID());
    // re apply old properties from old item
    newItem.cloneProperties(old);

    // update tracks
    for (Track track : TrackManager.getInstance().getTracks()) {
      if (track.getAlbum().equals(old)) {
        TrackManager.getInstance().changeTrackAlbum(track, sNewName, null);
      }
    }

    // if current track album name is changed, notify it
    if (bQueueUpdateRequired) {
      ObservationManager.notify(new JajukEvent(JajukEvents.ALBUM_CHANGED));
    }

    // remove old item
    removeItem(old);

    return newItem;
  }

  /**
   * Format the album name to be normalized :
   * <p>
   * -no underscores or other non-ASCII characters
   * <p>
   * -no spaces at the begin and the end
   * <p>
   * -All in lower case expect first letter of first word
   * <p>
   * example: "My album title".
   * 
   * @param sName The name to format.
   * 
   * @return The formatted string.
   */
  public static String format(String sName) {
    String sOut = sName.trim(); // suppress spaces at the begin and the end
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
  @Override
  public String getLabel() {
    return Const.XML_ALBUMS;
  }

  /**
   * Gets the album by id.
   * 
   * @param sID Item ID
   * 
   * @return Element
   */
  public Album getAlbumByID(String sID) {
    return (Album) getItemByID(sID);
  }

  /**
   * Gets the albums.
   * 
   * @return ordered albums list
   */
  @SuppressWarnings("unchecked")
  public List<Album> getAlbums() {
    return (List<Album>) getItems();
  }

  /**
   * Gets the albums iterator.
   * 
   * @return albums iterator
   */
  @SuppressWarnings("unchecked")
  public ReadOnlyIterator<Album> getAlbumsIterator() {
    return new ReadOnlyIterator<Album>((Iterator<Album>) getItemsIterator());
  }

  /**
   * Get sorted list of albums associated with this item.
   * 
   * @param item DOCUMENT_ME
   * 
   * @return a list of item, void list if no result
   */
  public List<Album> getAssociatedAlbums(Item item) {
    List<Album> out;
    // [Perf] If item is a track, just return its album
    if (item instanceof Track) {
      out = new ArrayList<Album>(1);
      out.add(((Track) item).getAlbum());
    } else {
      try {
        lock.readLock().lock();
        ReadOnlyIterator<Album> albums = getAlbumsIterator();
        // Use a set to avoid dups
        Set<Album> albumSet = new HashSet<Album>();
        while (albums.hasNext()) {
          Album album = albums.next();
          List<Track> cache = album.getTracksCache();
          synchronized (cache) {
            for (Track track : cache) {
              if (item instanceof Artist && track.getArtist().equals(item)) {
                albumSet.add(album);
              } else if (item instanceof Genre && track.getGenre().equals(item)) {
                albumSet.add(album);
              }
              if (item instanceof Year && track.getYear().equals(item)) {
                albumSet.add(album);
              }
            }
          }
        }
        out = new ArrayList<Album>(albumSet);
        Collections.sort(out);
      } finally {
        lock.readLock().unlock();
      }
    }
    return out;

  }

  /**
   * Return sorted top albums based on the average of each album rating.
   * 
   * @param bHideUnmounted if true, unmounted albums are not chosen
   * @param iNbBestofAlbums nb of items to return
   * 
   * @return top albums, can be less items than required according to nb of
   * available albums
   */
  public List<Album> getBestOfAlbums(boolean bHideUnmounted, int iNbBestofAlbums) {
    lock.readLock().lock();
    try {
      // Create a temporary table to remove unmounted albums
      // We consider an album as mounted if a least one track is mounted
      // This hashmap contains album-> album rates
      final Map<Album, Float> cacheRate = new HashMap<Album, Float>(AlbumManager.getInstance()
          .getElementCount());
      ReadOnlyIterator<Album> it = AlbumManager.getInstance().getAlbumsIterator();
      while (it.hasNext()) {
        Album album = it.next();
        if (!bHideUnmounted || album.containsReadyFiles()) {
          cacheRate.put(album, (float) album.getRate());
        }
      }
      // Now sort albums by rating
      List<Album> sortedAlbums = new ArrayList<Album>(cacheRate.keySet());
      Collections.sort(sortedAlbums, new Comparator<Album>() {
        public int compare(Album o1, Album o2) {
          // lowest first
          return (int) (cacheRate.get(o1) - cacheRate.get(o2));
        }
      });
      return getTopAlbums(sortedAlbums, iNbBestofAlbums);
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Return ordered list of newest albums.
   * 
   * @param bHideUnmounted if true, unmounted albums are not chosen
   * @param iNb nb of items to return
   * 
   * @return newest albums
   */
  public List<Album> getNewestAlbums(boolean bHideUnmounted, int iNb) {
    lock.readLock().lock();
    try {
      // create a temporary table to remove unmounted albums
      // We consider an album as mounted if a least one track is mounted
      // This hashmap contains album-> discovery date
      final Map<Album, Date> cache = new HashMap<Album, Date>(AlbumManager.getInstance()
          .getElementCount());
      ReadOnlyIterator<Track> it = TrackManager.getInstance().getTracksIterator();
      while (it.hasNext()) {
        Track track = it.next();
        if (track.getBestFile(bHideUnmounted) != null) {
          cache.put(track.getAlbum(), track.getDiscoveryDate());
        }
      }
      // Now sort albums by discovery date
      List<Album> sortedAlbums = new ArrayList<Album>(cache.keySet());
      Collections.sort(sortedAlbums, new Comparator<Album>() {
        public int compare(Album o1, Album o2) {
          return cache.get(o1).compareTo(cache.get(o2));
        }
      });
      return getTopAlbums(sortedAlbums, iNb);
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Return ordered rarely listen albums list.
   * 
   * @param bHideUnmounted if true, unmounted albums are not chosen
   * @param iNb nb of items to return
   * 
   * @return top albums, can be less items than required according to nb of
   * available albums
   */
  public List<Album> getRarelyListenAlbums(boolean bHideUnmounted, int iNb) {
    lock.readLock().lock();
    try {
      // create a temporary table to remove unmounted albums
      // We consider an album as mounted if a least one track is mounted
      // This hashmap contains album-> album hits (each track hit average)
      final Map<Album, Float> cache = new HashMap<Album, Float>(AlbumManager.getInstance()
          .getElementCount());
      // This hashmap contains album-> nb of tracks already taken into account
      // for average
      Map<Album, Integer> cacheNb = new HashMap<Album, Integer>(AlbumManager.getInstance()
          .getElementCount());
      ReadOnlyIterator<Track> it = TrackManager.getInstance().getTracksIterator();
      while (it.hasNext()) {
        Track track = it.next();
        if (track.getBestFile(bHideUnmounted) != null) {
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
      List<Album> sortedAlbums = new ArrayList<Album>(cache.keySet());
      Collections.sort(sortedAlbums, new Comparator<Album>() {
        public int compare(Album o1, Album o2) {
          // We inverse comparison as we want lowest scores
          return (int) (cache.get(o2) - cache.get(o1));
        }
      });
      return getTopAlbums(sortedAlbums, iNb);
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Convenient method to keep top albums (used by getBestof, newest... albums)
   * 
   * @param sortedAlbums sorted albums according desired criteria, size >= iNb
   * @param iNb Number of albums to return
   * 
   * @return a nicely sorted / shuffled list of albums or a void list of none
   * available albums
   */
  private List<Album> getTopAlbums(List<Album> sortedAlbums, int iNb) {
    // Keep only 3 * desired size or less if not enough available albums
    int size = 2 * iNb;
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
    return sublist.subList(0, (size >= iNb) ? iNb : size);
  }

  /**
   * Gets the max rate.
   * 
   * @return max rating for an album
   */
  public long getMaxRate() {
    return this.maxRate;
  }

  /**
   * Force to refresh the album max rating, it is not done soon as it is pretty
   * CPU consuming and we don't need a track by track rating precision.
   */
  public void refreshMaxRating() {
    // create a temporary table to remove unmounted albums
    // We consider an album as mounted if a least one track is mounted
    // This hashmap contains album-> album rates
    final Map<Album, Float> cacheRate = new HashMap<Album, Float>(AlbumManager.getInstance()
        .getElementCount());
    for (Album album : AlbumManager.getInstance().getAlbums()) {
      cacheRate.put(album, (float) album.getRate());
    }
    // OK, now keep only the highest score
    for (Map.Entry<Album, Float> album : cacheRate.entrySet()) {
      long value = Math.round(album.getValue());
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
  public void update(JajukEvent event) {
    if (event.getSubject() == JajukEvents.FILE_LAUNCHED) {
      // Compute album max rating every 10 tracks launches
      if (comp % 10 == 0) {
        refreshMaxRating();
      }
      comp++;
    }
  }

  /**
   * Gets the album by name.
   * 
   * @param name DOCUMENT_ME
   * 
   * @return associated album (case insensitive) or null if no match
   */
  public Album getAlbumByName(String name) {
    lock.readLock().lock();
    try {
      Album out = null;
      for (ReadOnlyIterator<Album> it = getAlbumsIterator(); it.hasNext();) {
        Album album = it.next();
        if (album.getName().equalsIgnoreCase(name)) {
          out = album;
          break;
        }
      }
      return out;
    } finally {
      lock.readLock().unlock();
    }

  }

  /**
   * Specialize switchToOrderState, here we sort the album cache in addition.
   */
  public void orderCache() {
    // read lock, not write lock because we need a write lock only when performing 
    // structural changes to items collection 
    for (Album album : getAlbums()) {
      List<Track> cache = album.getTracksCache();
      synchronized (cache) {
        Collections.sort(cache, new TrackComparator(TrackComparatorType.ALBUM));
      }
    }
  }

  /**
   * Reset cached cover url for every album
   */
  public void resetCoverCache() {
    for (Album album : getAlbums()) {
      album.setProperty(Const.XML_ALBUM_COVER, "");
    }
  }

}
