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
package org.jajuk.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.tags.Tag;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage Tracks.
 */
public final class TrackManager extends ItemManager {
  /** Self instance. */
  private static TrackManager singleton = new TrackManager();
  /** Autocommit flag for tags *. */
  private volatile boolean bAutocommit = true;
  /** Set of tags to commit. */
  private final Set<Tag> tagsToCommit = new HashSet<Tag>(10);
  /** Attic for tracks that have been dropped but may be useful when a file is renamed to restore its properties */
  private final Map<String, Track> attic = new HashMap<String, Track>(0);

  /**
   * No constructor available, only static access.
   */
  private TrackManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, true, false,
        String.class, null));
    // Album
    registerProperty(new PropertyMetaInformation(Const.XML_ALBUM, false, true, true, true, true,
        String.class, null));
    // Genre
    registerProperty(new PropertyMetaInformation(Const.XML_GENRE, false, true, true, true, true,
        String.class, null));
    // Artist
    registerProperty(new PropertyMetaInformation(Const.XML_ARTIST, false, true, true, true, true,
        String.class, null));
    // Album-artist
    registerProperty(new PropertyMetaInformation(Const.XML_ALBUM_ARTIST, false, false, true, true,
        true, String.class, null));
    // Length
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_LENGTH, false, true, true, false,
        false, Long.class, null));
    // Type
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_TYPE, false, true, true, false,
        false, Long.class, null));
    // Year
    registerProperty(new PropertyMetaInformation(Const.XML_YEAR, false, true, true, true, true,
        Long.class, 0));
    // Rate : this is a property computed from preference and total played time,
    // not editable
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_RATE, false, false, true, false,
        true, Long.class, 0));
    // Files. This is now a derivated property, build on demand. Never commited because always null.
    registerProperty(new PropertyMetaInformation(Const.XML_FILES, false, false, true, false, false,
        String.class, null));
    // Hits
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_HITS, false, false, true, false,
        false, Long.class, 0));
    // Addition date
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_DISCOVERY_DATE, false, false,
        true, false, true, Date.class, null));
    // Comment
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_COMMENT, false, false, true, true,
        true, String.class, null));
    // Track order
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_ORDER, false, true, true, true,
        false, Long.class, null));
    // Track disc number
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_DISC_NUMBER, false, true, true,
        true, true, Long.class, null));
    // Track preference factor. This is not editable because when changing
    // preference, others
    // actions must be done (updateRate() and we want user to use contextual
    // menus and commands instead of the properties wizard to set preference)
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_PREFERENCE, false, false, true,
        false, true, Long.class, 0l));
    // Track total playtime
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_TOTAL_PLAYTIME, false, false,
        true, false, false, Long.class, 0l));
    // Track ban status
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_BANNED, false, false, true, true,
        false, Boolean.class, false));
    // Scrobble flag
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_SCROBBLE, false, false, true,
        true, true, Boolean.class, true));
  }

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static TrackManager getInstance() {
    return singleton;
  }

  /**
   * Confirm before actually changing a tag
   * @param track the track to be changed
   * @return whether user accept to actually change a tag
   */
  private boolean confirm(Track track) {
    if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_BEFORE_TAG_WRITE)) {
      final int iResu = Messages.getChoice(Messages.getString("Confirmation_tag_write") + " : \n"
          + track.getFilesString(), JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.INFORMATION_MESSAGE);
      return iResu == JOptionPane.YES_OPTION;
    }
    return true;
  }

  /**
   * Register an Track.
   * 
   * @param sName 
   * @param album 
   * @param genre 
   * @param artist 
   * @param length 
   * @param year 
   * @param lOrder 
   * @param type 
   * @param lDiscNumber 
   * 
   * @return the track
   */
  public Track registerTrack(String sName, Album album, Genre genre, Artist artist, long length,
      Year year, long lOrder, Type type, long lDiscNumber) {
    String sId = createID(sName, album, genre, artist, length, year, lOrder, type, lDiscNumber);
    return registerTrack(sId, sName, album, genre, artist, length, year, lOrder, type, lDiscNumber);
  }

  /**
   * Return hashcode for a track.
   * 
   * @param sName 
   * @param album 
   * @param genre 
   * @param artist 
   * @param length 
   * @param year 
   * @param lOrder 
   * @param type 
   * @param lDiscNumber 
   * 
   * @return the string
   */
  protected static String createID(String sName, Album album, Genre genre, Artist artist,
      long length, Year year, long lOrder, Type type, long lDiscNumber) {
    StringBuilder sb = new StringBuilder(100);
    sb.append(genre.getID()).append(artist.getID()).append(album.getID()).append(sName)
        .append(year.getValue()).append(length).append(lOrder).append(type.getID())
        .append(lDiscNumber);
    // distinguish tracks by type because we can't find best file
    // on different quality levels by format
    return MD5Processor.hash(sb.toString());
  }

  /**
   * Register an Track with a known id.
   *
   * @param sId 
   * @param sName 
   * @param album 
   * @param genre 
   * @param artist 
   * @param length 
   * @param year 
   * @param lOrder 
   * @param type 
   * @param lDiscNumber 
   * @return the track
   */
  public Track registerTrack(String sId, String sName, Album album, Genre genre, Artist artist,
      long length, Year year, long lOrder, Type type, long lDiscNumber) {
    lock.writeLock().lock();
    try {
      // We absolutely need to return the same track if already registrated to
      // avoid duplicates and properties lost
      Track track = getTrackByID(sId);
      if (track != null) {
        return track;
      }
      track = new Track(sId, sName, album, genre, artist, length, year, lOrder, type, lDiscNumber);
      registerItem(track);
      // For performances, add the track to the album cache
      List<Track> cache = track.getAlbum().getTracksCache();
      synchronized (cache) {
        cache.add(track);
      }
      return track;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Commit tags.
   * 
   * @throws JajukException the jajuk exception
   * 
   * @throw an exception if a tag cannot be commited
   */
  public void commit() throws JajukException {
    try {
      lock.writeLock().lock();
      // Iterate over a defensive copy to avoid concurrent issues (note also that
      // several threads can commit at the same time). We synchronize the copy and
      // we drop tags to commit.
      List<Tag> toCommit = null;
      synchronized (tagsToCommit) {
        toCommit = new ArrayList<Tag>(tagsToCommit);
        tagsToCommit.clear();
      }
      for (Tag tag : toCommit) {
        try {
          tag.commit();
        } catch (Exception e) {
          Log.error(e);
          try {
            // If actual tag commit fails, we have to undo changes made in memory for current track
            // The best solution for this complex issue is to force a deep refresh of the directory
            // and to clear the tag cache to force tags reload
            Tag.clearCache();
            Directory dir = null;
            File file = FileManager.getInstance().getFileByPath(tag.getFio().getAbsolutePath());
            if (file != null) {
              dir = file.getDirectory();
            }
            dir.refresh(true);
            // refresh views
            ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
          } catch (Exception e2) {
            Log.error(e2);
          }
          throw new JajukException(104, e);
        }
      }
      // Clear the tag cache after a transaction to
      // avoid memory leaks
      Tag.clearCache();
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change a track album.
   *
   * @param track 
   * @param sNewAlbum 
   * @param filter files we want to deal with
   * @return new track
   * @throws JajukException the jajuk exception
   */
  public Track changeTrackAlbum(Track track, String sNewAlbum, Set<File> filter)
      throws JajukException {
    try {
      lock.writeLock().lock();
      if (!confirm(track)) {
        return track;
      }
      // check there is actually a change
      if (track.getAlbum().getName2().equals(sNewAlbum)) {
        return track;
      }
      List<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (File file : alReady) {
        Tag tag = Tag.getTagForFio(file.getFIO(), false);
        tag.setAlbumName(sNewAlbum);
        if (bAutocommit) {
          tag.commit();
        } else {
          tagsToCommit.add(tag);
        }
      }
      // Remove the track from the old album
      List<Track> cache = track.getAlbum().getTracksCache();
      synchronized (cache) {
        cache.remove(track);
      }
      // if current track album name is changed, notify it
      if (QueueModel.getPlayingFile() != null
          && QueueModel.getPlayingFile().getTrack().getAlbum().equals(track.getAlbum())) {
        ObservationManager.notify(new JajukEvent(JajukEvents.ALBUM_CHANGED));
      }
      // register the new album
      Album newAlbum = AlbumManager.getInstance().registerAlbum(sNewAlbum,
          track.getAlbum().getDiscID());
      Track newTrack = registerTrack(track.getName(), newAlbum, track.getGenre(),
          track.getArtist(), track.getDuration(), track.getYear(), track.getOrder(),
          track.getType(), track.getDiscNumber());
      postChange(track, newTrack, filter);
      // remove this album if no more references
      AlbumManager.getInstance().cleanOrphanTracks(track.getAlbum());
      return newTrack;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change a track artist.
   *
   * @param track 
   * @param sNewArtist 
   * @param filter files we want to deal with
   * @return new track
   * @throws JajukException the jajuk exception
   */
  public Track changeTrackArtist(Track track, String sNewArtist, Set<File> filter)
      throws JajukException {
    try {
      lock.writeLock().lock();
      if (!confirm(track)) {
        return track;
      }
      // check there is actually a change
      if (track.getArtist().getName2().equals(sNewArtist)) {
        return track;
      }
      List<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (final File file : alReady) {
        final Tag tag = Tag.getTagForFio(file.getFIO(), false);
        tag.setArtistName(sNewArtist);
        if (bAutocommit) {
          tag.commit();
        } else {
          tagsToCommit.add(tag);
        }
      }
      // Remove the track from the old album
      List<Track> cache = track.getAlbum().getTracksCache();
      synchronized (cache) {
        cache.remove(track);
      }
      // if current track artist name is changed, notify it
      if (QueueModel.getPlayingFile() != null
          && QueueModel.getPlayingFile().getTrack().getArtist().equals(track.getArtist())) {
        ObservationManager.notify(new JajukEvent(JajukEvents.ARTIST_CHANGED));
      }
      // register the new item
      Artist newArtist = ArtistManager.getInstance().registerArtist(sNewArtist);
      Track newTrack = registerTrack(track.getName(), track.getAlbum(), track.getGenre(),
          newArtist, track.getDuration(), track.getYear(), track.getOrder(), track.getType(),
          track.getDiscNumber());
      postChange(track, newTrack, filter);
      // remove this item if no more references
      ArtistManager.getInstance().cleanOrphanTracks(track.getArtist());
      return newTrack;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change a track genre.
   *
   * @param track 
   * @param sNewGenre 
   * @param filter files we want to deal with
   * @return new track
   * @throws JajukException the jajuk exception
   */
  public Track changeTrackGenre(Track track, String sNewGenre, Set<File> filter)
      throws JajukException {
    try {
      lock.writeLock().lock();
      if (!confirm(track)) {
        return track;
      }
      // check there is actually a change
      if (track.getGenre().getName2().equals(sNewGenre)) {
        return track;
      }
      List<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (final File file : alReady) {
        Tag tag = Tag.getTagForFio(file.getFIO(), false);
        tag.setGenreName(sNewGenre);
        if (bAutocommit) {
          tag.commit();
        } else {
          tagsToCommit.add(tag);
        }
      }
      // Remove the track from the old album
      List<Track> cache = track.getAlbum().getTracksCache();
      synchronized (cache) {
        cache.remove(track);
      }
      // register the new item
      Genre newGenre = GenreManager.getInstance().registerGenre(sNewGenre);
      Track newTrack = registerTrack(track.getName(), track.getAlbum(), newGenre,
          track.getArtist(), track.getDuration(), track.getYear(), track.getOrder(),
          track.getType(), track.getDiscNumber());
      postChange(track, newTrack, filter);
      // remove this item if no more references
      GenreManager.getInstance().cleanOrphanTracks(track.getGenre());
      return newTrack;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change a track year.
   *
   * @param track 
   * @param newItem 
   * @param filter files we want to deal with
   * @return new track or null if wrong format
   * @throws JajukException the jajuk exception
   */
  public Track changeTrackYear(Track track, String newItem, Set<File> filter) throws JajukException {
    try {
      lock.writeLock().lock();
      if (!confirm(track)) {
        return track;
      }
      // check there is actually a change
      if (track.getYear().getName().equals(newItem)) {
        return track;
      }
      long lNewItem = UtilString.fastLongParser(newItem);
      if (lNewItem < 0 || lNewItem > 10000) {
        throw new JajukException(137);
      }
      List<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (final File file : alReady) {
        Tag tag = Tag.getTagForFio(file.getFIO(), false);
        tag.setYear(newItem);
        if (bAutocommit) {
          tag.commit();
        } else {
          tagsToCommit.add(tag);
        }
      }
      // Remove the track from the old album
      List<Track> cache = track.getAlbum().getTracksCache();
      synchronized (cache) {
        cache.remove(track);
      }
      // Register new item
      Year newYear = YearManager.getInstance().registerYear(newItem);
      Track newTrack = registerTrack(track.getName(), track.getAlbum(), track.getGenre(),
          track.getArtist(), track.getDuration(), newYear, track.getOrder(), track.getType(),
          track.getDiscNumber());
      postChange(track, newTrack, filter);
      return newTrack;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change a track comment.
   *
   * @param track 
   * @param sNewItem 
   * @param filter files we want to deal with
   * @return new track or null if wrong format
   * @throws JajukException the jajuk exception
   */
  Track changeTrackComment(Track track, String sNewItem, Set<File> filter) throws JajukException {
    try {
      lock.writeLock().lock();
      if (!confirm(track)) {
        return track;
      }
      // check there is actually a change
      if (track.getComment().equals(sNewItem)) {
        return track;
      }
      List<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (File file : alReady) {
        Tag tag = Tag.getTagForFio(file.getFIO(), false);
        tag.setComment(sNewItem);
        if (bAutocommit) {
          tag.commit();
          // Force files resorting to ensure the sorting consistency
          // Do it here only because the sorting is a long operation already done
          // by the TrackManager.commit() method caller (PropertiesDialog for ie).
          // When called for a table change for ie, the sorting must be done for
          // each change.
          FileManager.getInstance().forceSorting();
        } else {
          tagsToCommit.add(tag);
        }
      }
      track.setComment(sNewItem);
      return track;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change a track rate.
   * 
   * @param track 
   * @param lNew 
   * 
   * @return new track or null if wrong format
   */
  public Track changeTrackRate(Track track, long lNew) {
    try {
      lock.writeLock().lock();
      // No confirmation here as this code is called during startup and is not available from GUI anyway
      // check there is actually a change
      if (track.getRate() == lNew) {
        return track;
      }
      // check format, rate in [0,100]
      if (lNew < 0 || lNew > 100) {
        track.setRate(0l);
        Log.error(137);
      } else {
        track.setRate(lNew);
      }
      return track;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change a track order.
   *
   * @param track 
   * @param lNewOrder 
   * @param filter files we want to deal with
   * @return new track or null if wrong format
   * @throws JajukException the jajuk exception
   */
  public Track changeTrackOrder(Track track, long lNewOrder, Set<File> filter)
      throws JajukException {
    try {
      lock.writeLock().lock();
      if (!confirm(track)) {
        return track;
      }
      // check there is actually a change
      if (track.getOrder() == lNewOrder) {
        return track;
      }
      // check format
      if (lNewOrder < 0) {
        throw new JajukException(137);
      }
      List<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (File file : alReady) {
        Tag tag = Tag.getTagForFio(file.getFIO(), false);
        tag.setOrder(lNewOrder);
        if (bAutocommit) {
          tag.commit();
        } else {
          tagsToCommit.add(tag);
        }
      }
      // Remove the track from the old album
      List<Track> cache = track.getAlbum().getTracksCache();
      synchronized (cache) {
        cache.remove(track);
      }
      Track newTrack = registerTrack(track.getName(), track.getAlbum(), track.getGenre(),
          track.getArtist(), track.getDuration(), track.getYear(), lNewOrder, track.getType(),
          track.getDiscNumber());
      postChange(track, newTrack, filter);
      return newTrack;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
  * Change a generic field.
  * 
  * @param track the track to write against.
  * @param tagFieldKey the tag key
  * @param tagFieldValue the tag value as a string
  * @param filter files we want to deal with
   * 
  * @return the track
  *
  * @throws JajukException if the tag can't be written
  */
  public Track changeTrackField(Track track, String tagFieldKey, String tagFieldValue,
      Set<File> filter) throws JajukException {
    try {
      lock.writeLock().lock();
      if (!confirm(track)) {
        return track;
      }
      List<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles();
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (File file : alReady) {
        Tag tag = Tag.getTagForFio(file.getFIO(), false);
        tag.setTagField(tagFieldKey, tagFieldValue);
        if (bAutocommit) {
          tag.commit();
        } else {
          tagsToCommit.add(tag);
        }
      }
      return track;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change a track name.
   *
   * @param track 
   * @param sNewItem 
   * @param filter files we want to deal with
   * @return new track
   * @throws JajukException the jajuk exception
   */
  public Track changeTrackName(Track track, String sNewItem, Set<File> filter)
      throws JajukException {
    try {
      lock.writeLock().lock();
      if (!confirm(track)) {
        return track;
      }
      // check there is actually a change
      if (track.getName().equals(sNewItem)) {
        return track;
      }
      List<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (File file : alReady) {
        Tag tag = Tag.getTagForFio(file.getFIO(), false);
        tag.setTrackName(sNewItem);
        if (bAutocommit) {
          tag.commit();
        } else {
          tagsToCommit.add(tag);
        }
      }
      // Remove old track from the album
      List<Track> cache = track.getAlbum().getTracksCache();
      synchronized (cache) {
        cache.remove(track);
      }
      Track newTrack = registerTrack(sNewItem, track.getAlbum(), track.getGenre(),
          track.getArtist(), track.getDuration(), track.getYear(), track.getOrder(),
          track.getType(), track.getDiscNumber());
      postChange(track, newTrack, filter);
      // if current track name is changed, notify it
      if (QueueModel.getPlayingFile() != null
          && QueueModel.getPlayingFile().getTrack().equals(track)) {
        ObservationManager.notify(new JajukEvent(JajukEvents.TRACK_CHANGED));
      }
      return newTrack;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change track album artist.
   *
   * @param track 
   * @param sNewItem 
   * @param filter 
   * @return the item
   * @throws JajukException the jajuk exception
   */
  Item changeTrackAlbumArtist(Track track, String sNewItem, Set<File> filter) throws JajukException {
    try {
      lock.writeLock().lock();
      if (!confirm(track)) {
        return track;
      }
      // check there is actually a change
      if (track.getAlbumArtist() != null && track.getAlbumArtist().getName2().equals(sNewItem)) {
        return track;
      }
      List<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (File file : alReady) {
        Tag tag = Tag.getTagForFio(file.getFIO(), false);
        tag.setAlbumArtist(sNewItem);
        if (bAutocommit) {
          tag.commit();
          // Force files resorting to ensure the sorting consistency
          // Do it here only because the sorting is a long operation already done
          // by the TrackManager.commit() method caller (PropertiesDialog for ie).
          // When called for a table change for ie, the sorting must be done for
          // each change.
          FileManager.getInstance().forceSorting();
        } else {
          tagsToCommit.add(tag);
        }
      }
      // register the new item
      AlbumArtist newAlbumArtist = AlbumArtistManager.getInstance().registerAlbumArtist(sNewItem);
      track.setAlbumArtist(newAlbumArtist);
      return track;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Change track disc number.
   *
   * @param track 
   * @param lNewDiscNumber 
   * @param filter 
   * @return the item
   * @throws JajukException the jajuk exception
   */
  public Item changeTrackDiscNumber(Track track, long lNewDiscNumber, Set<File> filter)
      throws JajukException {
    try {
      lock.writeLock().lock();
      if (!confirm(track)) {
        return track;
      }
      // check there is actually a change
      if (track.getDiscNumber() == lNewDiscNumber) {
        return track;
      }
      // check format
      if (lNewDiscNumber < 0) {
        throw new JajukException(137);
      }
      List<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (File file : alReady) {
        Tag tag = Tag.getTagForFio(file.getFIO(), false);
        tag.setDiscNumber(lNewDiscNumber);
        if (bAutocommit) {
          tag.commit();
        } else {
          tagsToCommit.add(tag);
        }
      }
      // Remove the track from the old album
      List<Track> cache = track.getAlbum().getTracksCache();
      synchronized (cache) {
        cache.remove(track);
      }
      // if current track album name is changed, notify it
      if (QueueModel.getPlayingFile() != null
          && QueueModel.getPlayingFile().getTrack().getAlbum().equals(track.getAlbum())) {
        ObservationManager.notify(new JajukEvent(JajukEvents.ALBUM_CHANGED));
      }
      Track newTrack = registerTrack(track.getName(), track.getAlbum(), track.getGenre(),
          track.getArtist(), track.getDuration(), track.getYear(), track.getDiscNumber(),
          track.getType(), lNewDiscNumber);
      postChange(track, newTrack, filter);
      return newTrack;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Update files references. 
   * 
   * @param oldTrack 
   * @param newTrack 
   * @param filter 
   */
  private void updateFilesReferences(Track oldTrack, Track newTrack, Set<File> filter) {
    lock.writeLock().lock();
    try {
      // Reset files property before adding new files
      for (File file : oldTrack.getReadyFiles(filter)) {
        file.setTrack(newTrack);// set new track for the changed file
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Post change. 
   * 
   * @param track 
   * @param newTrack 
   * @param filter 
   */
  private void postChange(Track track, Track newTrack, Set<File> filter) {
    lock.writeLock().lock();
    try {
      // re apply old properties from old item
      newTrack.cloneProperties(track);
      // update files references
      updateFilesReferences(track, newTrack, filter);
      if (track.getFiles().size() == 0) { // normal case: old track has no
        // more associated
        // tracks, remove it
        removeItem(track);// remove old track
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Perform a track cleanup : delete useless items.
   */
  @Override
  public void cleanup() {
    // No need to lock or synchronize, getTracks() is a defensive copy of tracks
    for (Track track : getTracks()) {
      if (track.getFiles().size() == 0) { // no associated file
        removeItem(track);
        continue;
      }
      // Cleanup all files no more attached to a track
      for (File file : track.getFiles()) {
        if (FileManager.getInstance().getFileByID(file.getID()) == null) {
          FileManager.getInstance().removeFile(file);
        }
      }
      if (track.getFiles().size() == 0) { // the track don't map
        // anymore to any physical item, just remove it
        removeItem(track);
      }
    }
  }

  /**
   * Remove a file mapping from a track.
   * 
   * @param file 
   */
  public void removeFile(File file) {
    Track track = file.getTrack();
    lock.writeLock().lock();
    try {
      // Remove file reference
      track.removeFile(file);
      // Put it in the attic 
      attic.put(track.getID(), track);
      // If the track contained a single file, drop it
      if (track.getFiles().size() == 0) {
        // the track don't map
        // anymore to any physical item, just remove it
        removeItem(track);
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Return a track from attic or null if not found
   * @param id the track id to search for
   * @return a track from attic or null if not found
   */
  public Track getTrackFromAttic(String id) {
    return attic.get(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getXMLTag() {
    return Const.XML_TRACKS;
  }

  /**
   * Get ordered tracks list associated with this item
   * <p>
   * This is a defensive copy only
   * </p>
   * .
   * 
   * @param item 
   * @param sorted Whether the output should be sorted on it (actually applied on
   * artists,years and genres because others items are already sorted)
   * 
   * @return the associated tracks
   */
  public List<Track> getAssociatedTracks(Item item, boolean sorted) {
    List<Item> items = new ArrayList<Item>(1);
    items.add(item);
    return getAssociatedTracks(items, sorted);
  }

  /**
   * Get ordered tracks list associated with a list of items (of the same type)
   * <p>
   * This is a defensive copy only
   * </p>
   * .
   *
   * @param items 
   * @param sorted Whether the output should be sorted on it (actually applied on
   * artists,years and genres because others items are already sorted)
   * @return the associated tracks
   */
  @SuppressWarnings("unchecked")
  public List<Track> getAssociatedTracks(List<Item> items, boolean sorted) {
    if (items == null || items.size() == 0) {
      return new ArrayList<Track>(0);
    }
    List<Track> out = new ArrayList<Track>(items.size());
    if (items.get(0) instanceof Album) {
      // check the album cache
      for (Item item : items) {
        List<Track> tracks = ((Album) item).getTracksCache();
        synchronized (tracks) {
          if (tracks.size() > 0) {
            out.addAll(tracks);
          }
        }
      }
      // cache is not sorted correct for albums with more than 1 disc
      if (sorted) {
        // sort Tracks
        Collections.sort(out, new TrackComparator(TrackComparatorType.ORDER));
      }
    }
    // If the item is itself a track, simply return it
    else if (items.get(0) instanceof Track) {
      for (Item item : items) {
        out.add((Track) item);
      }
      if (sorted) {
        Collections.sort(out, new TrackComparator(TrackComparatorType.ALBUM));
      }
    } else if (items.get(0) instanceof File) {
      for (Item item : items) {
        out.add(((File) item).getTrack());
      }
      if (sorted) {
        Collections.sort(out, new TrackComparator(TrackComparatorType.ALBUM));
      }
    } else if (items.get(0) instanceof Directory) {
      for (Item item : items) {
        Directory dir = (Directory) item;
        for (File file : dir.getFilesRecursively()) {
          Track track = file.getTrack();
          // Caution, do not add dups
          if (!out.contains(track)) {
            out.add(file.getTrack());
          }
        }
      }
      if (sorted) {
        Collections.sort(out, new TrackComparator(TrackComparatorType.ORDER));
      }
    } else if (items.get(0) instanceof Playlist) {
      for (Item item : items) {
        Playlist pl = (Playlist) item;
        List<File> files;
        try {
          files = pl.getFiles();
        } catch (JajukException e) {
          Log.warn("Cannot parse playlist : " + pl.getAbsolutePath());
          return out;
        }
        for (File file : files) {
          Track track = file.getTrack();
          // Caution, do not add dups
          if (!out.contains(track)) {
            out.add(file.getTrack());
          }
        }
      }
      if (sorted) {
        Collections.sort(out, new TrackComparator(TrackComparatorType.ALBUM));
      }
    } else if (items.get(0) instanceof Artist) {
      Iterator<Item> tracks = (Iterator<Item>) getItemsIterator();
      while (tracks.hasNext()) {
        Track track = (Track) tracks.next();
        if (items.contains(track.getArtist())) {
          out.add(track);
        }
        // Sort by album
        if (sorted) {
          Collections.sort(out, new TrackComparator(TrackComparatorType.ARTIST_ALBUM));
        }
      }
      return out;
    } else if (items.get(0) instanceof Genre) {
      Iterator<Item> tracks = (Iterator<Item>) getItemsIterator();
      while (tracks.hasNext()) {
        Track track = (Track) tracks.next();
        if (items.contains(track.getGenre())) {
          out.add(track);
        }
        // Sort by genre
        if (sorted) {
          Collections.sort(out, new TrackComparator(TrackComparatorType.GENRE_ARTIST_ALBUM));
        }
      }
    } else if (items.get(0) instanceof Year) {
      Iterator<Item> tracks = (Iterator<Item>) getItemsIterator();
      while (tracks.hasNext()) {
        Track track = (Track) tracks.next();
        if (items.contains(track.getYear())) {
          out.add(track);
        }
        // Sort by year
        if (sorted) {
          Collections.sort(out, new TrackComparator(TrackComparatorType.YEAR_ALBUM));
        }
      }
    }
    return out;
  }

  /**
   * Gets the comparator.
   * 
   * @return the comparator
   */
  public TrackComparator getComparator() {
    return new TrackComparator(
        TrackComparatorType.values()[Conf.getInt(Const.CONF_LOGICAL_TREE_SORT_ORDER)]);
  }

  /**
   * Gets the track by id.
   * 
   * @param sID Item ID
   * 
   * @return item
   */
  public Track getTrackByID(String sID) {
    return (Track) getItemByID(sID);
  }

  /**
   * Gets the tracks.
   * 
   * @return ordered tracks list
   */
  @SuppressWarnings("unchecked")
  public List<Track> getTracks() {
    return (List<Track>) getItems();
  }

  /**
   * Gets the tracks iterator.
   * 
   * @return tracks iterator
   */
  @SuppressWarnings("unchecked")
  public ReadOnlyIterator<Track> getTracksIterator() {
    return new ReadOnlyIterator<Track>((Iterator<Track>) getItemsIterator());
  }

  /**
   * Perform a search in all files names with given criteria.
   * 
   * @param criteria 
   * 
   * @return an ordered list of available files
   */
  public List<SearchResult> search(String criteria) {
    lock.readLock().lock();
    try {
      boolean hide = Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED);
      List<SearchResult> resu = new ArrayList<SearchResult>();
      ReadOnlyIterator<Track> tracks = getTracksIterator();
      while (tracks.hasNext()) {
        Track track = tracks.next();
        File playable = track.getBestFile(hide);
        if (playable != null) {
          String sResu = track.getAny();
          if (sResu.toLowerCase(Locale.getDefault()).indexOf(
              criteria.toLowerCase(Locale.getDefault())) != -1) {
            resu.add(new SearchResult(playable, playable.toStringSearch()));
          }
        }
      }
      return resu;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Checks if is autocommit.
   * 
   * @return autocommit behavior for tags
   */
  public boolean isAutocommit() {
    return this.bAutocommit;
  }

  /**
   * Set autocommit behavior for tags.
   * 
   * @param autocommit should tag changes be commited at each change or on demand ?
   */
  public void setAutocommit(boolean autocommit) {
    this.bAutocommit = autocommit;
  }
}
