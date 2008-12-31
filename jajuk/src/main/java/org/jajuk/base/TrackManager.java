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
 */

package org.jajuk.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.tags.Tag;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;
import org.jajuk.util.log.Log;

/**
 * Convenient class to manage Tracks
 * 
 */
public final class TrackManager extends ItemManager implements Observer {
  /** Self instance */
  private static TrackManager singleton;

  /**
   * Number of tracks that cannot be fully removed as it still contains files on
   * unmounted devices
   */
  private static int nbFilesRemaining = 0;

  /** Autocommit flag for tags * */
  private boolean bAutocommit = true;

  /** Set of tags to commit */
  private Set<Tag> tagsToCommit = new HashSet<Tag>(10);

  /**
   * No constructor available, only static access
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
    // Style
    registerProperty(new PropertyMetaInformation(Const.XML_STYLE, false, true, true, true, true,
        String.class, null));
    // Author
    registerProperty(new PropertyMetaInformation(Const.XML_AUTHOR, false, true, true, true, true,
        String.class, null));
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
    // Files
    registerProperty(new PropertyMetaInformation(Const.XML_FILES, false, false, true, false, false,
        String.class, null));
    // Hits
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_HITS, false, false, true, false,
        false, Long.class, 0));
    // Addition date
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_DISCOVERY_DATE, false, false,
        true, true, true, Date.class, null));
    // Comment
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_COMMENT, false, false, true, true,
        true, String.class, null));
    // Track order
    registerProperty(new PropertyMetaInformation(Const.XML_TRACK_ORDER, false, true, true, true,
        false, Long.class, null));
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

    // ---subscriptions---
    ObservationManager.register(this);
  }

  public Set<JajukEvents> getRegistrationKeys() {
    return Collections.singleton(JajukEvents.FILE_NAME_CHANGED);
  }

  /**
   * @return singleton
   */
  public static TrackManager getInstance() {
    if (singleton == null) {
      singleton = new TrackManager();
    }
    return singleton;
  }

  /**
   * Register an Track
   */
  public synchronized Track registerTrack(String sName, Album album, Style style, Author author,
      long length, Year year, long lOrder, Type type) {
    String sId = createID(sName, album, style, author, length, year, lOrder, type);
    return registerTrack(sId, sName, album, style, author, length, year, lOrder, type);
  }

  /**
   * Return hashcode for a track
   * 
   * @param track
   * @return
   */
  protected static String createID(String sName, Album album, Style style, Author author,
      long length, Year year, long lOrder, Type type) {
    StringBuilder sb = new StringBuilder(100);
    sb.append(style.getID()).append(author.getID()).append(album.getID()).append(sName).append(
        year.getValue()).append(length).append(lOrder).append(type.getID());
    // distinguish tracks by type because we can't find best file
    // on different quality levels by format
    return MD5Processor.hash(sb.toString());
  }

  /**
   * Register an Track with a known id
   * 
   * @param sName
   */
  public synchronized Track registerTrack(String sId, String sName, Album album, Style style,
      Author author, long length, Year year, long lOrder, Type type) {
    // We absolutely need to return the same track if already registrated to
    // avoid duplicates and properties lost
    Track track = getTrackByID(sId);
    if (track != null) {
      return track;
    }
    track = new Track(sId, sName, album, style, author, length, year, lOrder, type);
    registerItem(track);
    // For performances, add the track to the album cache
    album.getTracksCache().add(track);
    return track;
  }

  /**
   * Commit tags
   * 
   * @throw an exception is tag cannot be commited
   */
  public void commit() throws Exception {
    Iterator<Tag> it = tagsToCommit.iterator();
    while (it.hasNext()) {
      Tag tag = null;
      try {
        tag = it.next();
        tag.commit();
      } catch (Exception e) {
        Log.error(e);
      } finally {
        it.remove();
      }
    }
    // Clear the tag cache before and after a transaction to
    // avoid memory leaks
    Tag.clearCache();
  }

  /**
   * Change a track album
   * 
   * @param old
   *          track
   * @param new
   *          album name
   * @param filter
   *          files we want to deal with
   * @return new track
   */
  public synchronized Track changeTrackAlbum(Track track, String sNewAlbum, Set<File> filter)
      throws JajukException {
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
      Tag tag = Tag.getTagForFio(file.getFIO());
      tag.setAlbumName(sNewAlbum);
      if (bAutocommit) {
        tag.commit();
      } else {
        tagsToCommit.add(tag);
      }
    }
    // Remove the track from the old album
    track.getAlbum().getTracksCache().remove(track);
    // if current track album name is changed, notify it
    if (FIFO.getCurrentFile() != null
        && FIFO.getCurrentFile().getTrack().getAlbum().equals(track.getAlbum())) {
      ObservationManager.notify(new Event(JajukEvents.ALBUM_CHANGED));
    }
    // register the new album
    Album newAlbum = AlbumManager.getInstance().registerAlbum(sNewAlbum);
    Track newTrack = registerTrack(track.getName(), newAlbum, track.getStyle(), track.getAuthor(),
        track.getDuration(), track.getYear(), track.getOrder(), track.getType());
    postChange(track, newTrack, filter);
    // remove this album if no more references
    AlbumManager.getInstance().cleanup(track.getAlbum());
    return newTrack;
  }

  /**
   * Change a track author
   * 
   * @param old
   *          track
   * @param new
   *          author name
   * @param filter
   *          files we want to deal with
   * @return new track
   */
  public synchronized Track changeTrackAuthor(Track track, String sNewAuthor, Set<File> filter)
      throws JajukException {
    // check there is actually a change
    if (track.getAuthor().getName2().equals(sNewAuthor)) {
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
      final Tag tag = Tag.getTagForFio(file.getFIO());

      tag.setAuthorName(sNewAuthor);
      if (bAutocommit) {
        tag.commit();
      } else {
        tagsToCommit.add(tag);
      }
    }
    // Remove the track from the old album
    track.getAlbum().getTracksCache().remove(track);

    // if current track author name is changed, notify it
    if (FIFO.getCurrentFile() != null
        && FIFO.getCurrentFile().getTrack().getAuthor().equals(track.getAuthor())) {
      ObservationManager.notify(new Event(JajukEvents.AUTHOR_CHANGED));
    }
    // register the new item
    Author newAuthor = AuthorManager.getInstance().registerAuthor(sNewAuthor);
    Track newTrack = registerTrack(track.getName(), track.getAlbum(), track.getStyle(), newAuthor,
        track.getDuration(), track.getYear(), track.getOrder(), track.getType());
    postChange(track, newTrack, filter);
    // remove this item if no more references
    AuthorManager.getInstance().cleanup(track.getAuthor());
    return newTrack;
  }

  /**
   * Change a track style
   * 
   * @param old
   *          item
   * @param new
   *          item name
   * @param filter
   *          files we want to deal with
   * @return new track
   */
  public synchronized Track changeTrackStyle(Track track, String sNewStyle, Set<File> filter)
      throws JajukException {
    // check there is actually a change

    if (track.getStyle().getName2().equals(sNewStyle)) {
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
      Tag tag = Tag.getTagForFio(file.getFIO());

      tag.setStyleName(sNewStyle);
      if (bAutocommit) {
        tag.commit();
      } else {
        tagsToCommit.add(tag);
      }
    }
    // Remove the track from the old album
    track.getAlbum().getTracksCache().remove(track);

    // register the new item
    Style newStyle = StyleManager.getInstance().registerStyle(sNewStyle);
    Track newTrack = registerTrack(track.getName(), track.getAlbum(), newStyle, track.getAuthor(),
        track.getDuration(), track.getYear(), track.getOrder(), track.getType());
    postChange(track, newTrack, filter);
    // remove this item if no more references
    StyleManager.getInstance().cleanup(track.getStyle());
    return newTrack;
  }

  /**
   * Change a track year
   * 
   * @param old
   *          item
   * @param new
   *          item name
   * @param filter
   *          files we want to deal with
   * @return new track or null if wrong format
   */
  public synchronized Track changeTrackYear(Track track, String newItem, Set<File> filter)
      throws JajukException {
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
      Tag tag = Tag.getTagForFio(file.getFIO());

      tag.setYear(newItem);
      if (bAutocommit) {
        tag.commit();
      } else {
        tagsToCommit.add(tag);
      }
    }
    // Remove the track from the old album
    track.getAlbum().getTracksCache().remove(track);

    // Register new item
    Year newYear = YearManager.getInstance().registerYear(newItem);
    Track newTrack = registerTrack(track.getName(), track.getAlbum(), track.getStyle(), track
        .getAuthor(), track.getDuration(), newYear, track.getOrder(), track.getType());
    postChange(track, newTrack, filter);
    return newTrack;
  }

  /**
   * Change a track comment
   * 
   * @param old
   *          item
   * @param new
   *          item name
   * @param filter
   *          files we want to deal with
   * @return new track or null if wronf format
   */
  public synchronized Track changeTrackComment(Track track, String sNewItem, Set<File> filter)
      throws JajukException {
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
      Tag tag = Tag.getTagForFio(file.getFIO());
      tag.setComment(sNewItem);
      if (bAutocommit) {
        tag.commit();
      } else {
        tagsToCommit.add(tag);
      }
    }
    // Remove the track from the old album
    track.getAlbum().getTracksCache().remove(track);

    track.setComment(sNewItem);
    return track;
  }

  /**
   * Change a track rate
   * 
   * @param old
   *          item
   * @param new
   *          item name
   * @return new track or null if wrong format
   */
  public synchronized Track changeTrackRate(Track track, long lNew) {
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
  }

  /**
   * Change a track order
   * 
   * @param old
   *          item
   * @param new
   *          item order
   * @param filter
   *          files we want to deal with
   * @return new track or null if wrong format
   */
  public synchronized Track changeTrackOrder(Track track, long lNewOrder, Set<File> filter)
      throws JajukException {
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
      Tag tag = Tag.getTagForFio(file.getFIO());
      tag.setOrder(lNewOrder);
      if (bAutocommit) {
        tag.commit();
      } else {
        tagsToCommit.add(tag);
      }
    }

    // Remove the track from the old album
    track.getAlbum().getTracksCache().remove(track);

    Track newTrack = registerTrack(track.getName(), track.getAlbum(), track.getStyle(), track
        .getAuthor(), track.getDuration(), track.getYear(), lNewOrder, track.getType());
    postChange(track, newTrack, filter);
    return newTrack;
  }

  /**
   * Change a track name
   * 
   * @param old
   *          item
   * @param new
   *          item name
   * @param filter
   *          files we want to deal with
   * @return new track
   */
  public synchronized Track changeTrackName(Track track, String sNewItem, Set<File> filter)
      throws JajukException {
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
      Tag tag = Tag.getTagForFio(file.getFIO());
      tag.setTrackName(sNewItem);
      if (bAutocommit) {
        tag.commit();
      } else {
        tagsToCommit.add(tag);
      }
    }

    // Remove old track from the album
    track.getAlbum().getTracksCache().remove(track);

    Track newTrack = registerTrack(sNewItem, track.getAlbum(), track.getStyle(), track.getAuthor(),
        track.getDuration(), track.getYear(), track.getOrder(), track.getType());
    postChange(track, newTrack, filter);
    // if current track name is changed, notify it
    if (FIFO.getCurrentFile() != null && FIFO.getCurrentFile().getTrack().equals(track)) {
      ObservationManager.notify(new Event(JajukEvents.TRACK_CHANGED));
    }
    return newTrack;
  }

  private synchronized void updateFilesReferences(Track oldTrack, Track newTrack, Set<File> filter) {
    // Reset files property before adding new files
    for (File file : oldTrack.getReadyFiles(filter)) {
      file.setTrack(newTrack);// set new track for the changed file
      newTrack.addFile(file); // add changed file
      oldTrack.removeFile(file); // remove file from old track
    }
  }

  private synchronized void postChange(Track track, Track newTrack, Set<File> filter) {
    // re apply old properties from old item
    newTrack.cloneProperties(track);
    // update files references
    updateFilesReferences(track, newTrack, filter);
    if (track.getFiles().size() == 0) { // normal case: old track has no
      // more associated
      // tracks, remove it
      removeItem(track);// remove old track
    } else { // some files have not been changed because located on
      // unmounted devices
      nbFilesRemaining++;
    }
  }

  /**
   * Perform a track cleanup : delete useless items
   */
  @Override
  @SuppressWarnings("unchecked")
  public synchronized void cleanup() {
    for (Track track : getTracks()) {
      if (track.getFiles().size() == 0) { // no associated file
        removeItem(track);
        continue;
      }
      // Cleanup all files no more attached to a track
      // We use files shallow copy to avoid indirect concurrency exception
      for (File file : new ArrayList<File>(track.getFiles())) {
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

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return Const.XML_TRACKS;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(Event event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.FILE_NAME_CHANGED.equals(subject)) {
      Properties properties = event.getDetails();
      File fNew = (File) properties.get(Const.DETAIL_NEW);
      File fileOld = (File) properties.get(Const.DETAIL_OLD);
      Track track = fileOld.getTrack();
      track.removeFile(fileOld);
      track.addFile(fNew);
    }
  }

  /**
   * Get ordered tracks list associated with this item
   * <p>
   * This is a shallow copy only
   * </p>
   * 
   * @param item
   * @return
   */
  @SuppressWarnings("unchecked")
  public synchronized List<Track> getAssociatedTracks(Item item) {
    if (item instanceof Album) {
      // check the album cache
      List<Track> tracks = ((Album) item).getTracksCache();
      if (tracks.size() > 0) {
        return tracks;
      }
    }
    // If the item is itself a track, simply return it
    if (item instanceof Track) {
      List<Track> out = new ArrayList<Track>(1);
      out.add((Track) item);
      return out;
    } else if (item instanceof File) {
      List<Track> out = new ArrayList<Track>(1);
      out.add(((File) item).getTrack());
      return out;
    } else if (item instanceof Directory) {
      Directory dir = (Directory) item;
      List<Track> out = new ArrayList<Track>(dir.getFiles().size());
      for (File file : dir.getFilesRecursively()) {
        Track track = file.getTrack();
        // Caution, do not add dups
        if (!out.contains(track)) {
          out.add(file.getTrack());
        }
      }
      return out;
    } else if (item instanceof Playlist) {
      Playlist pl = (Playlist) item;
      List<File> files;
      try {
        files = pl.getFiles();
      } catch (JajukException e) {
        Log.error(e);
        return null;
      }
      List<Track> out = new ArrayList<Track>(files.size());
      for (File file : files) {
        Track track = file.getTrack();
        // Caution, do not add dups
        if (!out.contains(track)) {
          out.add(file.getTrack());
        }
      }
      return out;
    }
    List<Track> out = new ArrayList<Track>(10);
    Iterator<Item> items = (Iterator<Item>) getItemsIterator();
    while (items.hasNext()) {
      Track track = (Track) items.next();
      if ((item instanceof Author && track.getAuthor().equals(item))
          || (item instanceof Year && track.getYear().equals(item))
          || (item instanceof Style && track.getStyle().equals(item))) {
        // Note: no need to check dups here
        out.add(track);
      }
    }
    return out;
  }

  public TrackComparator getComparator() {
    return new TrackComparator(TrackComparatorType.values()[Conf
        .getInt(Const.CONF_LOGICAL_TREE_SORT_ORDER)]);
  }

  /**
   * @param sID
   *          Item ID
   * @return item
   */
  public Track getTrackByID(String sID) {
    return (Track) getItemByID(sID);
  }

  /**
   * 
   * @return ordered tracks list
   */
  @SuppressWarnings("unchecked")
  public synchronized List<Track> getTracks() {
    return (List<Track>) getItems();
  }

  /**
   * 
   * @return tracks iterator
   */
  @SuppressWarnings("unchecked")
  public synchronized ReadOnlyIterator<Track> getTracksIterator() {
    return new ReadOnlyIterator<Track>((Iterator<Track>) getItemsIterator());
  }

  /**
   * Perform a search in all files names with given criteria
   * 
   * @param sCriteria
   * @return an ordered list of available files
   */
  public List<SearchResult> search(String criteria) {
    boolean hide = Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED);
    List<SearchResult> resu = new ArrayList<SearchResult>();
    ReadOnlyIterator<Track> tracks = getTracksIterator();
    while (tracks.hasNext()) {
      Track track = tracks.next();
      File playable = track.getPlayeableFile(hide);
      if (playable != null) {
        String sResu = track.getAny();
        if (sResu.toLowerCase().indexOf(criteria.toLowerCase()) != -1) {
          resu.add(new SearchResult(playable, playable.toStringSearch()));
        }
      }
    }
    return resu;
  }

  public static int getFilesRemaining() {
    return nbFilesRemaining;
  }

  public static void resetFilesRemaining() {
    TrackManager.nbFilesRemaining = 0;
  }

  /**
   * 
   * @return autocommit behavior for tags
   */
  public boolean isAutocommit() {
    return this.bAutocommit;
  }

  /**
   * Set autocommit behavior for tags
   * 
   * @param autocommit
   */
  public void setAutocommit(boolean autocommit) {
    this.bAutocommit = autocommit;
  }
}
