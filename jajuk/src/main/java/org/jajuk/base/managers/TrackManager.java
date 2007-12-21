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

package org.jajuk.base.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ItemType;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.Observer;
import org.jajuk.base.SearchResult;
import org.jajuk.base.Tag;
import org.jajuk.base.TrackComparator;
import org.jajuk.base.items.Album;
import org.jajuk.base.items.Author;
import org.jajuk.base.items.File;
import org.jajuk.base.items.Item;
import org.jajuk.base.items.Style;
import org.jajuk.base.items.Track;
import org.jajuk.base.items.Type;
import org.jajuk.base.items.Year;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.Resources.ConfKeys;
import org.jajuk.util.Resources.Details;
import org.jajuk.util.Resources.XML;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;

/**
 * Convenient class to manage Tracks
 *
 * @TODO Refactor this error detection system (isChangePbm)
 */
public class TrackManager extends ItemManager<Track> implements Observer {
  /** Self instance */
  private static TrackManager singleton;

  /** Unmounted tracks flag */
  boolean bChangePbm = false;

  /** Max rate */
  private long lMaxRate = 0l;

  /**
   * No constructor available, only static access
   */
  public TrackManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new MetaProperty(XML.ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new MetaProperty(XML.NAME, false, true, true, true, false,
        String.class, null));
    // Album
    registerProperty(new MetaProperty(XML.ALBUM, false, true, true, true, true,
        String.class, null));
    // Style
    registerProperty(new MetaProperty(XML.STYLE, false, true, true, true, true,
        String.class, null));
    // Author
    registerProperty(new MetaProperty(XML.AUTHOR, false, true, true, true, true,
        String.class, null));
    // Length
    registerProperty(new MetaProperty(XML.TRACK_LENGTH, false, true, true, false, false,
        Long.class, null));
    // Type
    registerProperty(new MetaProperty(XML.TRACK_TYPE, false, true, true, false, false,
        Long.class, null));
    // Year
    registerProperty(new MetaProperty(XML.YEAR, false, true, true, true, true,
        Long.class, 0));
    // Rate
    registerProperty(new MetaProperty(XML.TRACK_RATE, false, false, true, true, true,
        Long.class, 0));
    // Files
    registerProperty(new MetaProperty(XML.FILES, false, false, true, false, false,
        String.class, null));
    // Hits
    registerProperty(new MetaProperty(XML.TRACK_HITS, false, false, true, false, false,
        Long.class, 0));
    // Addition date
    registerProperty(new MetaProperty(XML.TRACK_ADDED, false, false, true, false, false,
        Date.class, null));
    // Comment
    registerProperty(new MetaProperty(XML.TRACK_COMMENT, false, false, true, true, true,
        String.class, null));
    // Track order
    registerProperty(new MetaProperty(XML.TRACK_ORDER, false, true, true, true, false,
        Long.class, null));
    // ---subscriptions---
    ObservationManager.register(this);
  }

  public Set<EventSubject> getRegistrationKeys() {
    return Collections.singleton(EventSubject.EVENT_FILE_NAME_CHANGED);
  }

  /**
   * Register an Track
   */
  public synchronized Track registerTrack(final String sName, final Album album, final Style style, final Author author,
      final long length, final Year year, final long lOrder, final Type type) {
    final String sId = createID(sName, album, style, author, length, year, lOrder, type);
    return registerTrack(sId, sName, album, style, author, length, year, lOrder, type);
  }

  /**
   * Return hashcode for a track
   *
   * @param track
   * @return
   */
  public static String createID(final String sName, final Album album, final Style style, final Author author,
      final long length, final Year year, final long lOrder, final Type type) {
    final StringBuilder sb = new StringBuilder(100);
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
  public Track registerTrack(final String sId, final String sName, final Album album, final Style style, final Author author,
      final long length, final Year year, final long lOrder, final Type type) {
    synchronized (getLock()) {
      Track track = getItems().get(sId);
      if (track != null) {
        return track;
      }
      track = new Track(sId, sName, album, style, author, length, year, lOrder, type);
      getItems().put(sId, track);
      return track;
    }
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
  public Track changeTrackAlbum(final Track track, final String sNewAlbum, final HashSet filter)
      throws JajukException {
    synchronized (getLock()) {
      // check there is actually a change
      if (track.getAlbum().getName2().equals(sNewAlbum)) {
        return track;
      }
      ArrayList<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (final File file : alReady) {
        final Tag tag = new Tag(file.getIO());
        tag.setAlbumName(sNewAlbum);
        tag.commit();
      }
      // if current track album name is changed, notify it
      if ((FIFO.getInstance().getCurrentFile() != null)
          && FIFO.getInstance().getCurrentFile().getTrack().getAlbum().equals(track.getAlbum())) {
        ObservationManager.notify(new Event(EventSubject.EVENT_ALBUM_CHANGED));
      }
      // register the new album
      final Album newAlbum = ((AlbumManager) ItemType.Album.getManager()).registerAlbum(sNewAlbum);
      final Track newTrack = registerTrack(track.getName(), newAlbum, track.getStyle(),
          track.getAuthor(), track.getDuration(), track.getYear(), track.getOrder(), track
              .getTrackType());
      postChange(track, newTrack, filter);
      // remove this album if no more references
      ((AlbumManager) ItemType.Album.getManager()).cleanup(track.getAlbum());
      return newTrack;
    }
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
  public Track changeTrackAuthor(final Track track, final String sNewAuthor, final HashSet filter)
      throws JajukException {
    synchronized (getLock()) {
      // check there is actually a change
      if (track.getAuthor().getName2().equals(sNewAuthor)) {
        return track;
      }
      ArrayList<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (final File file : alReady) {
        final Tag tag = new Tag(file.getIO());

        tag.setAuthorName(sNewAuthor);
        tag.commit();
      }
      // if current track author name is changed, notify it
      if ((FIFO.getInstance().getCurrentFile() != null)
          && FIFO.getInstance().getCurrentFile().getTrack().getAuthor().equals(track.getAuthor())) {
        ObservationManager.notify(new Event(EventSubject.EVENT_AUTHOR_CHANGED));
      }
      // register the new item
      final Author newAuthor = ((AuthorManager) ItemType.Author.getManager()).registerAuthor(sNewAuthor);
      final Track newTrack = registerTrack(track.getName(), track.getAlbum(), track.getStyle(),
          newAuthor, track.getDuration(), track.getYear(), track.getOrder(), track.getTrackType());
      postChange(track, newTrack, filter);
      // remove this item if no more references
      ((AuthorManager) ItemType.Author.getManager()).cleanup(track.getAuthor());
      return newTrack;
    }
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
  public Track changeTrackStyle(final Track track, final String sNewStyle, final HashSet filter)
      throws JajukException {
    synchronized (getLock()) {
      // check there is actually a change

      if (track.getStyle().getName2().equals(sNewStyle)) {
        return track;
      }
      ArrayList<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (final File file : alReady) {
        final Tag tag = new Tag(file.getIO());

        tag.setStyleName(sNewStyle);
        tag.commit();
      }
      // register the new item
      final Style newStyle = ((StyleManager) ItemType.Style.getManager()).registerStyle(sNewStyle);
      final Track newTrack = registerTrack(track.getName(), track.getAlbum(), newStyle,
          track.getAuthor(), track.getDuration(), track.getYear(), track.getOrder(), track
              .getTrackType());
      postChange(track, newTrack, filter);
      // remove this item if no more references
      ((StyleManager) ItemType.Style.getManager()).cleanup(track.getStyle());
      return newTrack;
    }
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
  public Track changeTrackYear(final Track track, final String newItem, final HashSet filter) throws JajukException {
    synchronized (getLock()) {
      // check there is actually a change
      if (track.getYear().getName().equals(newItem)) {
        return track;
      }
      final long lNewItem = Long.parseLong(newItem);
      if ((lNewItem < 0) || (lNewItem > 10000)) {
        Messages.showErrorMessage(137);
        throw new JajukException(137);
      }
      ArrayList<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (final File file : alReady) {
        final Tag tag = new Tag(file.getIO());

        tag.setYear(newItem);
        tag.commit();
      }
      // Register new item
      final Year newYear = ((YearManager) ItemType.Year.getManager()).registerYear(newItem);
      final Track newTrack = registerTrack(track.getName(), track.getAlbum(), track.getStyle(), track
          .getAuthor(), track.getDuration(), newYear, track.getOrder(), track.getTrackType());
      postChange(track, newTrack, filter);
      return newTrack;
    }
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
  public Track changeTrackComment(final Track track, final String sNewItem, final HashSet filter)
      throws JajukException {
    synchronized (getLock()) {
      // check there is actually a change
      if (track.getComment().equals(sNewItem)) {
        return track;
      }
      ArrayList<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (final File file : alReady) {
        final Tag tag = new Tag(file.getIO());
        tag.setComment(sNewItem);
        tag.commit();
      }
      track.setComment(sNewItem);
      return track;
    }
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
  public Track changeTrackRate(final Track track, final long lNew) throws JajukException {
    synchronized (getLock()) {
      // check there is actually a change
      if (track.getRate() == lNew) {
        return track;
      }
      // check format
      if (lNew < 0) {
        Messages.showErrorMessage(137);
        throw new JajukException(137);
      }
      track.setRate(lNew);
      return track;
    }
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
   * @return new track or null if wronf format
   */
  public Track changeTrackOrder(final Track track, final long lNewOrder, final HashSet filter) throws JajukException {
    synchronized (getLock()) {
      // check there is actually a change
      if (track.getOrder() == lNewOrder) {
        return track;
      }
      // check format
      if (lNewOrder < 0) {
        Messages.showErrorMessage(137);
        return null;
      }
      ArrayList<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (final File file : alReady) {
        final Tag tag = new Tag(file.getIO());
        tag.setOrder(lNewOrder);
        tag.commit();
      }
      final Track newTrack = registerTrack(track.getName(), track.getAlbum(), track.getStyle(), track
          .getAuthor(), track.getDuration(), track.getYear(), lNewOrder, track.getTrackType());
      postChange(track, newTrack, filter);
      return newTrack;
    }
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
  public Track changeTrackName(final Track track, final String sNewItem, final HashSet filter) throws JajukException {
    synchronized (getLock()) {
      // check there is actually a change
      if (track.getName().equals(sNewItem)) {
        return track;
      }
      ArrayList<File> alReady = null;
      // check if files are accessible
      alReady = track.getReadyFiles(filter);
      if (alReady.size() == 0) {
        throw new NoneAccessibleFileException(10);
      }
      // change tag in files
      for (final File file : alReady) {
        final Tag tag = new Tag(file.getIO());
        tag.setTrackName(sNewItem);
        tag.commit();
      }
      final Track newTrack = registerTrack(sNewItem, track.getAlbum(), track.getStyle(), track
          .getAuthor(), track.getDuration(), track.getYear(), track.getOrder(), track.getTrackType());
      postChange(track, newTrack, filter);
      // if current track name is changed, notify it
      if ((FIFO.getInstance().getCurrentFile() != null)
          && FIFO.getInstance().getCurrentFile().getTrack().equals(track)) {
        ObservationManager.notify(new Event(EventSubject.EVENT_TRACK_CHANGED));
      }
      return newTrack;
    }
  }

  private void updateFilesReferences(final Track oldTrack, final Track newTrack, final HashSet filter) {
    synchronized (getLock()) {
      // Reset files property before adding new files
      for (final File file : oldTrack.getReadyFiles(filter)) {
        file.setTrack(newTrack);// set new track for the changed file
        newTrack.addFile(file); // add changed file
        oldTrack.removeFile(file); // remove file from old track
      }
    }
  }

  private void postChange(final Track track, final Track newTrack, final HashSet filter) {
    synchronized (getLock()) {
      // re apply old properties from old item
      newTrack.cloneProperties(track);
      // update files references
      updateFilesReferences(track, newTrack, filter);
      if (track.getFiles().size() == 0) { // normal case: old track has no
        // more associated
        // tracks, remove it
        removeItem(track.getID());// remove old track
        bChangePbm = false;
      } else { // some files have not been changed because located on
        // unmounted devices
        bChangePbm = true;
      }
    }
  }

  /**
   * Perform a track cleanup : delete useless items
   */
  @Override
  public void cleanup() {
    synchronized (getLock()) {
      final Iterator itTracks = getItems().values().iterator();
      while (itTracks.hasNext()) {
        final Track track = (Track) itTracks.next();
        if (track.getFiles().size() == 0) { // no associated file
          itTracks.remove();
          continue;
        }
        final Iterator itFiles = track.getFiles().iterator();
        while (itFiles.hasNext()) {
          final org.jajuk.base.items.File file = (org.jajuk.base.items.File) itFiles.next();
          if (((FileManager) ItemType.File.getManager()).getFileByID(file.getID()) == null) {
            itFiles.remove();// no? remove it from the track
          }
        }
        if (track.getFiles().size() == 0) { // the track don't map
          // anymore to any
          // physical
          // item, just remove it
          itTracks.remove();
        }
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
    return XML.TRACKS;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(final Event event) {
    final EventSubject subject = event.getSubject();
    if (EventSubject.EVENT_FILE_NAME_CHANGED.equals(subject)) {
      final Properties properties = event.getDetails();
      final File fNew = (File) properties.get(Details.NEW);
      final File fileOld = (File) properties.get(Details.OLD);
      final Track track = fileOld.getTrack();
      track.removeFile(fileOld);
      track.addFile(fNew);
    }
  }

  /**
   * Get ordered tracks associated with this item
   *
   * @param item
   * @return
   */
  public Set<Track> getAssociatedTracks(final Item item) {
    synchronized (getLock()) {
      final Set<Track> out = new TreeSet<Track>(new TrackComparator(TrackComparator.ALBUM));
      for (final Object item2 : getItems().values()) {
        final Track track = (Track) item2;
        if (((item instanceof Album) && track.getAlbum().equals(item))
            || ((item instanceof Author) && track.getAuthor().equals(item))
            || ((item instanceof Year) && track.getYear().equals(item))
            || ((item instanceof Style) && track.getStyle().equals(item))) {
          out.add(track);
        }
      }
      return out;
    }
  }

  public boolean isChangePbm() {
    synchronized (getLock()) {
      return bChangePbm;
    }
  }

  public TrackComparator getComparator() {
    return new TrackComparator(ConfigurationManager.getInt(ConfKeys.LOGICAL_TREE_SORT_ORDER));
  }

  /**
   * @return maximum rating between all tracks
   */
  public long getMaxRate() {
    return lMaxRate;
  }

  /**
   * Set max rate
   */
  public void setMaxRate(final long lRate) {
    lMaxRate = lRate;
  }

  /**
   * @param sID
   *          Item ID
   * @return item
   */
  public Track getTrackByID(final String sID) {
    synchronized (getLock()) {
      return getItems().get(sID);
    }
  }

  /**
   *
   * @return unsorted tracks list
   */
  public ArrayList<Track> getTracksAsList() {
    final ArrayList<Track> tracks = new ArrayList<Track>(getItems().size());
    synchronized (getLock()) {
      for (final Track item : getItems().values()) {
        tracks.add(item);
      }
    }
    return tracks;
  }

  /**
   * Perform a search in all files names with given criteria
   *
   * @param sCriteria
   * @return a tree set of available files
   */
  public TreeSet<SearchResult> search(final String criteria) {
    synchronized (getLock()) {
      final TreeSet<SearchResult> tsResu = new TreeSet<SearchResult>();
      final Iterator<Track> it = getItems().values().iterator();
      while (it.hasNext()) {
        final Track track = it.next();
        final File playable = track.getPlayeableFile(ConfigurationManager
            .getBoolean(ConfKeys.OPTIONS_HIDE_UNMOUNTED));
        if (playable != null) {
          final String sResu = track.getAny();
          if (sResu.toLowerCase().indexOf(criteria.toLowerCase()) != -1) {
            tsResu.add(new SearchResult(playable, playable.toStringSearch()));
          }
        }
      }
      return tsResu;
    }
  }
}
