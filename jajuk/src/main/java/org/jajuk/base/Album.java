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

import java.awt.Container;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;

import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * An Album *
 * <p>
 * Logical item
 */
public class Album extends LogicalItem implements Comparable<Album> {

  /**
   * For perfs, we cache the associated tracks. This cache is filled by the
   * TrackManager using the getTracksCache() method
   */
  private List<Track> cache = new ArrayList<Track>(15);

  /**
   * Album constructor
   * 
   * @param id
   * @param sName
   */
  public Album(String sId, String sName) {
    super(sId, sName);
  }

  /**
   * Return album name, dealing with unknown for any language
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
   * toString method
   */
  @Override
  public String toString() {
    return "Album[ID=" + getID() + " Name={{" + getName() + "}}]";
  }

  /**
   * Alphabetical comparator used to display ordered lists
   * 
   * @param other
   *          item to be compared
   * @return comparison result
   */
  public int compareTo(Album otherAlbum) {
    // compare using name and id to differentiate unknown items
    StringBuilder current = new StringBuilder(getName2());
    current.append(getID());
    StringBuilder other = new StringBuilder(otherAlbum.getName2());
    other.append(otherAlbum.getID());
    return current.toString().compareToIgnoreCase(other.toString());
  }

  /**
   * @return whether the album is Unknown or not
   */
  public boolean isUnknown() {
    return this.getName().equals(UNKNOWN_ALBUM);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public final String getLabel() {
    return XML_ALBUM;
  }

  /**
   * Get item description
   */
  @Override
  public String getDesc() {
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
    if (Const.XML_AUTHOR.equals(sKey)) {
      return handleAuthor();
    } else if (Const.XML_ALBUM.equals(sKey)) {
      return getName2();
    } else if (Const.XML_STYLE.equals(sKey)) {
      return handleStyle();
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
   * @return
   */
  private String handleAuthor() {
    Author author = getAuthor();
    if (author != null) {
      return author.getName2();
    } else {
      // More than one author, display void string
      return "";
    }
  }

  /**
   * @return
   */
  private String handleStyle() {
    Style style = getStyle();
    if (style != null) {
      return style.getName2();
    } else {
      // More than one style, display void string
      return "";
    }
  }

  /**
   * @return
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
   * @return a human representation of all concatenated properties
   */
  @Override
  public String getAny() {
    // rebuild any
    StringBuilder sb = new StringBuilder(100);
    sb.append(super.getAny()); // add all album-based properties
    // now add others properties
    Author author = getAuthor();
    if (author != null) {
      sb.append(author.getName2());
    }
    Style style = getStyle();
    if (style != null) {
      sb.append(style.getName2());
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
   * @return associated best cover file available or null if none. The returned
   *         file can not be readable, so use a try/catch around a future access
   */
  public File getCoverFile() {
    File fCover = null;
    File fDir = null; // analyzed directory
    // search for local covers in all directories mapping the current track
    // to reach other devices covers and display them together
    List<Track> lTracks = TrackManager.getInstance().getAssociatedTracks(this);
    if (lTracks.size() == 0) {
      return null;
    }
    // List if directories we have to look in
    Set<Directory> dirs = new HashSet<Directory>(2);
    for (Track track : lTracks) {
      for (org.jajuk.base.File file : track.getFiles()) {
        if (file.isReady()) {
          // note that hashset ensures directory unicity
          dirs.add(file.getDirectory());
        }
      }
    }
    // look for absolute cover in collection
    for (Directory dir : dirs) {
      String sAbsolut = dir.getStringValue(Const.XML_DIRECTORY_DEFAULT_COVER);
      if (!UtilString.isVoid(sAbsolut.trim())) {
        File fAbsoluteDefault = new File(dir.getAbsolutePath() + '/' + sAbsolut);
        if (fAbsoluteDefault.exists()) {
          return fAbsoluteDefault;
        }
      }
    }

    // look for standard cover in collection
    for (Directory dir : dirs) {
      fDir = dir.getFio(); // store this dir
      java.io.File[] files = fDir.listFiles();// null if none file
      // found
      for (int i = 0; files != null && i < files.length; i++) {
        // test file exists, do not use the File.canRead() method: it can be
        // very costly when using a NAS under Windows
        if (files[i].exists() && files[i].length() < MAX_COVER_SIZE * 1024) {
          // check size to avoid out of memory errors
          String sExt = UtilSystem.getExtension(files[i]);
          if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png")
              || sExt.equalsIgnoreCase("gif")) {
            if (UtilFeatures.isStandardCover(files[i])) {
              // Test the image is not corrupted
              try {
                MediaTracker mediaTracker = new MediaTracker(new Container());
                ImageIcon ii = new ImageIcon(files[i].getAbsolutePath());
                mediaTracker.addImage(ii.getImage(), 0);
                mediaTracker.waitForID(0); // wait for image
                if (!mediaTracker.isErrorAny()) {
                  return files[i];
                }
              } catch (Exception e) {
                Log.error(e);
              }
            }
          }
        }
      }
    }
    // none ? OK, return first cover file we find
    for (Directory dir : dirs) {
      fDir = dir.getFio(); // store this dir
      java.io.File[] files = fDir.listFiles();// null if none file
      // found
      for (int i = 0; files != null && i < files.length; i++) {
        // test file exists, do not use the File.canRead() method: it can be
        // very costly when using a NAS under Windows
        if (files[i].exists() && files[i].length() < MAX_COVER_SIZE * 1024) {
          // check size to avoid out of memory errors
          String sExt = UtilSystem.getExtension(files[i]);
          if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png")
              || sExt.equalsIgnoreCase("gif")) {
            // Test the image is not corrupted
            try {
              MediaTracker mediaTracker = new MediaTracker(new Container());
              ImageIcon ii = new ImageIcon(files[i].getAbsolutePath());
              mediaTracker.addImage(ii.getImage(), 0);
              mediaTracker.waitForID(0); // wait for image
              if (!mediaTracker.isErrorAny()) {
                return files[i];
              }
            } catch (Exception e) {
              Log.error(e);
            }
          }
        }
      }
    }
    return fCover;
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
   * @return album average rating
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
   * 
   * @param size
   *          size using format width x height
   * @return album thumb for given size
   */
  public ImageIcon getThumbnail(String size) {
    File fCover = UtilSystem.getConfFileByPath(Const.FILE_THUMBS + '/' + size + '/' + getID() + '.'
        + EXT_THUMB);
    // Check if thumb already exists
    if (!fCover.exists() || fCover.length() == 0) {
      return IconLoader.getNoCoverIcon(size);
    }
    // Create the image using Toolkit and not ImageIO API to be able to
    // flush all the image data
    Image img = Toolkit.getDefaultToolkit().getImage(fCover.getAbsolutePath());
    ImageIcon icon = new ImageIcon(img);
    // Free thumb memory (DO IT AFTER FULL ImageIcon loading, see previous line)
    img.flush();
    // accelerate GC cleanup
    img = null;
    return icon;
  }

  /**
   * 
   * @return style for the album. Return null if the album contains tracks with
   *         different styles
   */
  public Style getStyle() {
    Set<Style> styles = new HashSet<Style>(1);
    for (Track track : cache) {
      styles.add(track.getStyle());
    }
    // If different styles, the album style is null
    if (styles.size() == 1) {
      return styles.iterator().next();
    } else {
      return null;
    }
  }

  /**
   * 
   * @return author for the album. Return null if the album contains tracks with
   *         different authors
   */
  public Author getAuthor() {
    Set<Author> authors = new HashSet<Author>(1);
    for (Track track : cache) {
      authors.add(track.getAuthor());
    }
    // If different Authors, the album Author is null
    if (authors.size() == 1) {
      return authors.iterator().next();
    } else {
      return null;
    }
  }

  /**
   * 
   * @return year for the album. Return null if the album contains tracks with
   *         different years
   */
  public Year getYear() {
    Set<Year> years = new HashSet<Year>(1);
    for (Track track : cache) {
      years.add(track.getYear());
    }
    // If different Authors, the album Author is null
    if (years.size() == 1) {
      return years.iterator().next();
    } else {
      return null;
    }
  }

  /**
   * Return full album length in secs
   */
  public long getDuration() {
    long length = 0;
    for (Track track : cache) {
      length += track.getDuration();
    }
    return length;
  }

  /**
   * @return album nb of tracks
   */
  public int getNbOfTracks() {
    return cache.size();
  }

  /**
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
   * @return First found track discovery date
   */
  public Date getDiscoveryDate() {
    if (cache.size() > 0) {
      return cache.get(0).getDiscoveryDate();
    } else {
      return null;
    }
  }

  public boolean matches(String property, String pattern) {
    if (UtilString.isVoid(property) || UtilString.isVoid(pattern)) {
      return true;
    }
    String sValue = null;
    if (Const.XML_ALBUM.equals(property)) {
      sValue = getName2();
    } else if (Const.XML_STYLE.equals(property)) {
      Style style = getStyle();
      if (style == null) {
        return false;
      }
      sValue = style.getName2();
    }
    if (sValue == null) {
      return false;
    }
    boolean match = false;
    try {
      // do not use regexp matches(<string>) because the string may contain
      // characters to be escaped
      match = (sValue.toLowerCase().indexOf(pattern.toLowerCase()) != -1);
      // test if the item property contains this
      // property value (ignore case)
    } catch (PatternSyntaxException pse) {
      // wrong pattern syntax
      Log.error(pse);
    }
    return match;
  }

  /**
   * Reset tracks cache
   */
  protected void resetTracks() {
    cache.clear();
  }

  /**
   * 
   * @return ordered tracks cache for this album (perf)
   */
  public List<Track> getTracksCache() {
    return this.cache;
  }

  /**
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

}
