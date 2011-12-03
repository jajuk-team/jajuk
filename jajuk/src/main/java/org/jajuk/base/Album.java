/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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

import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.services.covers.Cover;
import org.jajuk.services.tags.Tag;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.ImageFilter;
import org.jajuk.util.log.Log;

/**
 * An Album *
 * <p>
 * Logical item.
 */
public class Album extends LogicalItem implements Comparable<Album> {

  /** For perfs, we cache the associated tracks. This cache is filled by the TrackManager using the getTracksCache() method */
  private final List<Track> cache = new ArrayList<Track>(15);

  /** This array stores thumbnail presence for all the available size (performance) By default all booleans are false. */
  private boolean[] availableTumbs;

  /**
   * Album constructor.
   *
   * @param sId DOCUMENT_ME
   * @param sName DOCUMENT_ME
   * @param discID DOCUMENT_ME
   */
  Album(String sId, String sName, long discID) {
    super(sId, sName);
    setProperty(Const.XML_ALBUM_DISC_ID, discID);
  }

  /**
   * Gets the disc id.
   * 
   * @return the discID
   */
  public long getDiscID() {
    return getLongValue(Const.XML_ALBUM_DISC_ID);
  }

  /**
   * Return album name, dealing with unknown for any language.
   * 
   * @return album name
   */
  public String getName2() {
    String sOut = getName();
    if (sOut.equals(UNKNOWN_ALBUM)) {
      sOut = Messages.getString(UNKNOWN_ALBUM);
    }
    return sOut;
  }

  /**
   * toString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return "Album[ID=" + getID() + " Name={{" + getName() + "}}" + " disk ID={{" + getDiscID()
        + "}}]";
  }

  /**
   * Alphabetical comparator on the name
   * <p>
   * Used to display ordered lists.
   * 
   * @param otherAlbum DOCUMENT_ME
   * 
   * @return comparison result
   */
  @Override
  public int compareTo(Album otherAlbum) {
    if (otherAlbum == null) {
      return -1;
    }

    // compare using name and id to differentiate unknown items
    StringBuilder current = new StringBuilder(getName2());
    current.append(getID());
    StringBuilder other = new StringBuilder(otherAlbum.getName2());
    other.append(otherAlbum.getID());
    return current.toString().compareToIgnoreCase(other.toString());
  }

  /**
   * Return whether this item is strictly unknown : contains no tag.
   *
   * @return whether this item is Unknown or not
   */
  public boolean isUnknown() {
    return this.getName().equals(UNKNOWN_ALBUM);
  }

  /**
   * Return whether this item seems unknown (fuzzy search).
   *
   * @return whether this item seems unknown
   */
  public boolean seemsUnknown() {
    return isUnknown() || "unknown".equalsIgnoreCase(getName())
        || Messages.getString(UNKNOWN_ALBUM).equalsIgnoreCase(getName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public final String getXMLTag() {
    return XML_ALBUM;
  }

 /* (non-Javadoc)
   * @see org.jajuk.base.Item#getTitle()
   */
  @Override
  public String getTitle() {
    return Messages.getString("Item_Album") + " : " + getName2();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(String sKey) {
    // We compute here all pseudo keys (non album real attributes) that can be
    // required on an album
    if (Const.XML_ARTIST.equals(sKey)) {
      return handleArtist();
    } else if (Const.XML_ALBUM.equals(sKey)) {
      return getName2();
    } else if (Const.XML_GENRE.equals(sKey)) {
      return handleGenre();
    } else if (Const.XML_YEAR.equals(sKey)) {
      return handleYear();
    } else if (Const.XML_TRACK_RATE.equals(sKey)) {
      return Long.toString(getRate());
    } else if (Const.XML_TRACK_LENGTH.equals(sKey)) {
      return Long.toString(getDuration());
    } else if (Const.XML_TRACKS.equals(sKey)) {
      return Integer.toString(getNbOfTracks());
    } else if (Const.XML_TRACK_DISCOVERY_DATE.equals(sKey)) {
      return UtilString.getLocaleDateFormatter().format(getDiscoveryDate());
    } else if (Const.XML_TRACK_HITS.equals(sKey)) {
      return Long.toString(getHits());
    } else if (Const.XML_ANY.equals(sKey)) {
      return getAny();
    }
    // default
    return super.getHumanValue(sKey);
  }

  /**
   * Handle artist.
   * DOCUMENT_ME
   * 
   * @return the string
   */
  private String handleArtist() {
    Artist artist = getArtist();
    if (artist != null) {
      return artist.getName2();
    } else {
      // More than one artist, display void string
      return "";
    }
  }

  /**
   * Handle genre.
   * DOCUMENT_ME
   * 
   * @return the string
   */
  private String handleGenre() {
    Genre genre = getGenre();
    if (genre != null) {
      return genre.getName2();
    } else {
      // More than one genre, display void string
      return "";
    }
  }

  /**
   * Handle year.
   * DOCUMENT_ME
   * 
   * @return the string
   */
  private String handleYear() {
    Year year = getYear();
    if (year != null) {
      return Long.toString(year.getValue());
    } else {
      return "";
    }
  }

  /**
   * Gets the any.
   * 
   * @return a human representation of all concatenated properties
   */
  @Override
  public String getAny() {
    // rebuild any
    StringBuilder sb = new StringBuilder(100);
    sb.append(super.getAny()); // add all album-based properties
    // now add others properties
    Artist artist = getArtist();
    if (artist != null) {
      sb.append(artist.getName2());
    }
    // Try to add album artist
    Track first = null;
    List<Track> cache = getTracksCache();
    synchronized (cache) {
      first = cache.get(0);
    }
    // (every track maps at minimum an "unknown artist" album artist
    if (first.getAlbumArtist() != null) {
      sb.append(first.getAlbumArtist().getName2());
    }

    Genre genre = getGenre();
    if (genre != null) {
      sb.append(genre.getName2());
    }
    Year year = getYear();
    if (year != null) {
      sb.append(getHumanValue(Const.XML_YEAR));
    }
    sb.append(getHumanValue(Const.XML_TRACK_RATE));
    sb.append(getHumanValue(Const.XML_TRACK_LENGTH));
    sb.append(getHumanValue(Const.XML_TRACKS));
    sb.append(getHumanValue(Const.XML_TRACK_DISCOVERY_DATE));
    sb.append(getHumanValue(Const.XML_TRACK_HITS));
    return sb.toString();
  }

  /**
   * Gets the best associated cover as a file.
   * <p>Can be a long action</p>
   * 
   * @return Associated best cover file available or null if none. The returned
   * file is not guarantee to exist, so use a try/catch around a future access to this method.
   */
  public File findCover() {
    // first check if we have a selected cover that still exists
    String selectedCoverPath = getStringValue(XML_ALBUM_SELECTED_COVER);
    if (StringUtils.isNotBlank(selectedCoverPath) && new File(selectedCoverPath).exists()) {
      // If user-selected cover is available, just return its path
      return new File(selectedCoverPath);
    } 

    // otherwise check if the "discovered cover" is set to "none"
    String discoveredCoverPath = getStringValue(XML_ALBUM_DISCOVERED_COVER);
    if (StringUtils.isNotBlank(discoveredCoverPath)
        && COVER_NONE.equals(discoveredCoverPath)) {
      return null;
    } 

    // now check if the "discovdered cover" is available
    if (StringUtils.isNotBlank(discoveredCoverPath)) {
      // Check if discovered cover still exist. There is an overhead
      // drawback but otherwise, the album's cover
      // property may be stuck to an old device's cover url.
      // Moreover, cover tags are extracted to cache directory so they are 
      // Regularly dropped.
      Device device = DeviceManager.getInstance().getDeviceByPath(new File(discoveredCoverPath));
      // If the device is not mounted, do not perform this existence check up
      if (device != null) {
        if(device.isMounted()) {
          if (new File(discoveredCoverPath).exists()) {
            return new File(discoveredCoverPath);
          }
        } else {
          return new File(discoveredCoverPath);
        }
      } else if (new File(discoveredCoverPath).exists()) {
        return new File(discoveredCoverPath);
      }
    }

    // None cover yet set or it is no more accessible.
    // Search for local covers in all directories mapping the current track
    // to reach other devices covers and display them together
    List<Track> lTracks = cache;
    if (lTracks.size() == 0) {
      setProperty(XML_ALBUM_DISCOVERED_COVER, COVER_NONE);
      return null;
    }
    // List at directories we have to look in
    Set<Directory> dirs = new HashSet<Directory>(2);
    for (Track track : lTracks) {
      for (org.jajuk.base.File file : track.getReadyFiles()) {
        // note that hashset ensures directory unicity
        dirs.add(file.getDirectory());
      }
    }
    // If none available dir, we can't search for cover for now (may be better
    // next time when at least one device will be mounted)
    if (dirs.size() == 0) {
      return null;
    }

    // look for tag cover
    File cover = findTagCover();

    // none ? look for standard cover in collection
    if (cover == null) {
      cover = findCoverFile(dirs, true);
    }

    // none ? OK, return first cover file we find
    if (cover == null) {
      cover = findCoverFile(dirs, false);
    }

    // [PERF] Still nothing ? ok, set no cover to avoid further searches 
    if (cover == null) {
      setProperty(XML_ALBUM_DISCOVERED_COVER, COVER_NONE);
    } else { //[PERF] if we found a cover, we store it to avoid further covers 
      // searches including a full tags picture extraction  
      setProperty(XML_ALBUM_DISCOVERED_COVER, cover.getAbsolutePath());
    }
    return cover;
  }

  /**
   * Return whether this album owns a cover (this method doesn't check
   * cover file existence). 
   * @return whether this album owns a cover.
   */
  public boolean containsCover() {
    String discoveredCoverPath = getStringValue(XML_ALBUM_DISCOVERED_COVER);
    return !StringUtils.isBlank(discoveredCoverPath) && !discoveredCoverPath.equals(COVER_NONE);
  }

  /**
   * Return a tag cover file from given directories. If a cover tags are found, 
   * they are extracted to the cache directory. 
   * 
   * @return a tag cover file or null if none.
   */
  private File findTagCover() {
    //Make sure to sort the cache
    List<Track> sortedTracks = new ArrayList<Track>(cache);
    Collections.sort(sortedTracks, new TrackComparator(TrackComparatorType.ALBUM));

    for (Track track : sortedTracks) {
      for (org.jajuk.base.File file : track.getReadyFiles()) {
        try {
          Tag tag = new Tag(file.getFIO(), false);
          List<Cover> covers = tag.getCovers();
          if (covers.size() > 0) {
            return covers.get(0).getFile();
          }
        } catch (JajukException e1) {
          Log.error(e1);
        }
      }
    }
    return null;
  }

  /**
   * Return a cover file matching criteria or null.
   * 
   * @param dirs : list of directories to search in
   * @param onlyStandardCovers to we consider only standard covers ?
   * 
   * @return a cover file matching criteria or null
   */
  private File findCoverFile(Set<Directory> dirs, boolean onlyStandardCovers) {
    JajukFileFilter filter = new JajukFileFilter(ImageFilter.getInstance());
    for (Directory dir : dirs) {
      File fDir = dir.getFio(); // store this dir
      java.io.File[] files = fDir.listFiles();// null if none file
      // found
      for (int i = 0; files != null && i < files.length; i++) {
        if (files[i].exists()
        // check size to avoid out of memory errors
            && files[i].length() < MAX_COVER_SIZE * 1024
            // Is it an image ?
            && filter.accept(files[i])) {
          // Filter standard view if required
          if (onlyStandardCovers && !UtilFeatures.isStandardCover(files[i])) {
            continue;
          }

          // Test the image is not corrupted
          try {
            ImageIcon ii = new ImageIcon(files[i].getAbsolutePath());
            // Note that at this point, the image is fully loaded (done in the ImageIcon
            // constructor)
            if (ii.getImageLoadStatus() == MediaTracker.COMPLETE) {
              return files[i];
            } else {
              Log.debug("Problem loading: " + files[i].getAbsolutePath());
            }
          } catch (Exception e) {
            Log.error(e);
          }
        }
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIconRepresentation()
   */
  @Override
  public ImageIcon getIconRepresentation() {
    return IconLoader.getIcon(JajukIcons.ALBUM);
  }

  /**
   * Gets the rate.
   * 
   * @return album rating
   */
  @Override
  public long getRate() {
    long rate = 0;
    for (Track track : cache) {
      rate += track.getRate();
    }
    return rate;
  }

  /**
   * Gets the thumbnail.
   * 
   * @param size size using format width x height
   * 
   * @return album thumb for given size
   */
  public ImageIcon getThumbnail(int size) {
    File fCover = ThumbnailManager.getThumbBySize(this, size);
    // Check if thumb already exists
    if (!fCover.exists() || fCover.length() == 0) {
      return IconLoader.getNoCoverIcon(size);
    }
    BufferedImage img = null;
    try {
      img = ImageIO.read(new File(fCover.getAbsolutePath()));
    } catch (IOException e) {
      Log.error(e);
    }
    // can be null now if an error occurred, we reported a error to the log
    // already...
    if (img == null) {
      return null;
    }

    ImageIcon icon = new ImageIcon(img);
    // Free thumb memory (DO IT AFTER FULL ImageIcon loading)
    img.flush();

    return icon;
  }

  /**
   * Gets the genre.
   * 
   * @return genre for the album. Return null if the album contains tracks with
   * different genres
   */
  public Genre getGenre() {
    Set<Genre> genres = new HashSet<Genre>(1);
    for (Track track : cache) {
      genres.add(track.getGenre());
    }
    // If different genres, the album genre is null
    if (genres.size() == 1) {
      return genres.iterator().next();
    } else {
      return null;
    }
  }

  /**
   * Gets the artist.
   * 
   * @return artist for the album. <br>
   * Return null if the album contains tracks with different artists
   */
  public Artist getArtist() {
    if (cache.size() == 0) {
      return null;
    }
    Artist first = cache.get(0).getArtist();
    for (Track track : cache) {
      if (!track.getArtist().equals(first)) {
        return null;
      }
    }
    return first;
  }

  /**
   * Gets the artist or the album artist if not available
   * 
   * <u>Used algorithm is following :
   * <li>If none available tags : return "unknown artist"</li>
   * <li>If the album contains tracks with different artists, display the first album artist found if any</li>
   * <li>In this case, if no album artist is available, display the first artist found</li>
   * </u>.
   * 
   * @return artist for the album. <br>
   * Return Always an artist, eventually a "Unknown Artist" one
   */
  public String getArtistOrALbumArtist() {
    // no track => no artist
    if (cache.size() == 0) {
      return Const.UNKNOWN_ARTIST;
    }

    Artist artist = getArtist();
    if (artist != null && !artist.isUnknown()) {
      return artist.getName();
    } else {
      Track first = cache.get(0);
      AlbumArtist albumArtist = first.getAlbumArtist();
      if (!albumArtist.isUnknown()) {
        return albumArtist.getName();
      } else {
        return first.getArtist().getName();
      }
    }
  }

  /**
   * Gets the year.
   * 
   * @return year for the album. Return null if the album contains tracks with
   * different years
   */
  public Year getYear() {
    Set<Year> years = new HashSet<Year>(1);
    for (Track track : cache) {
      years.add(track.getYear());
    }
    // If different Artists, the album Artist is null
    if (years.size() == 1) {
      return years.iterator().next();
    } else {
      return null;
    }
  }

  /**
   * Return full album length in secs.
   * 
   * @return the duration
   */
  public long getDuration() {
    long length = 0;
    for (Track track : cache) {
      length += track.getDuration();
    }
    return length;
  }

  /**
   * Gets the nb of tracks.
   * 
   * @return album nb of tracks
   */
  public int getNbOfTracks() {
    return cache.size();
  }

  /**
   * Gets the hits.
   * 
   * @return album total nb of hits
   */
  public long getHits() {
    int hits = 0;
    for (Track track : cache) {
      hits += track.getHits();
    }
    return hits;
  }

  /**
   * Contains ready files.
   * 
   * @return whether the album contains a least one available track
   */
  public boolean containsReadyFiles() {
    for (Track track : cache) {
      if (track.getReadyFiles().size() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the discovery date.
   * 
   * @return First found track discovery date
   */
  public Date getDiscoveryDate() {
    if (cache.size() > 0) {
      return cache.get(0).getDiscoveryDate();
    } else {
      return null;
    }
  }

  /**
   * Gets the tracks cache.
   * 
   * @return ordered tracks cache for this album (perf)
   */
  public List<Track> getTracksCache() {
    return this.cache;
  }

  /**
   * Gets the any track.
   * 
   * @return a track from this album
   */
  public Track getAnyTrack() {
    if (cache.size() == 0) {
      return null;
    } else {
      return cache.get(0);
    }
  }

  /**
   * Set that the thumb for given size is available.
   * 
   * @param size (thumb size like 50)
   * @param available DOCUMENT_ME
   */
  public void setAvailableThumb(int size, boolean available) {
    if (availableTumbs == null) {
      availableTumbs = new boolean[6];
    }
    availableTumbs[size / 50 - 1] = available;
  }

  /**
   * Return whether a thumb is available for given size.
   * 
   * @param size (thumb size like 50)
   * 
   * @return whether a thumb is available for given size
   */
  public boolean isThumbAvailable(int size) {
    // Lazy loading of thumb availability (for all sizes)
    if (availableTumbs == null) {
      availableTumbs = new boolean[6];
      for (int i = 50; i <= 300; i += 50) {
        File fThumb = ThumbnailManager.getThumbBySize(this, i);
        setAvailableThumb(i, fThumb.exists() && fThumb.length() > 0);
      }
    }
    return availableTumbs[size / 50 - 1];
  }

}
