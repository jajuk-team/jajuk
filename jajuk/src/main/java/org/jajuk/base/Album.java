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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;

import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * An Album *
 * <p>
 * Logical item
 */
public class Album extends LogicalItem implements Comparable<Album> {

  private static final long serialVersionUID = 1L;

  private static TrackComparator basicComparator = new TrackComparator(TrackComparator.ALBUM);

  /** For perfs, we cache the associated tracks */
  protected Set<Track> tracks = new TreeSet<Track>(basicComparator);

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
  final public String getLabel() {
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
    if (XML_AUTHOR.equals(sKey)) {
      Author author = getAuthor();
      if (author != null) {
        return author.getName2();
      } else {
        // More than one author, display void string
        return "";
      }
    } else if (XML_ALBUM.equals(sKey)) {
      return getName2();
    } else if (XML_STYLE.equals(sKey)) {
      Style style = getStyle();
      if (style != null) {
        return style.getName2();
      } else {
        // More than one style, display void string
        return "";
      }
    } else if (XML_YEAR.equals(sKey)) {
      Year year = getYear();
      if (year != null) {
        return Long.toString(year.getValue());
      } else {
        return "";
      }
    } else if (XML_TRACK_RATE.equals(sKey)) {
      return Long.toString(getRate());
    } else if (XML_TRACK_LENGTH.equals(sKey)) {
      return Long.toString(getDuration());
    } else if (XML_TRACKS.equals(sKey)) {
      return Integer.toString(getNbOfTracks());
    } else if (XML_TRACK_DISCOVERY_DATE.equals(sKey)) {
      return Util.getLocaleDateFormatter().format(getDiscoveryDate());
    } else if (XML_TRACK_HITS.equals(sKey)) {
      return Long.toString(getHits());
    } else if (XML_ANY.equals(sKey)) {
      return getAny();
    }
    // default
    return super.getHumanValue(sKey);
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
      sb.append(getHumanValue(XML_YEAR));
    }
    sb.append(getHumanValue(XML_TRACK_RATE));
    sb.append(getHumanValue(XML_TRACK_LENGTH));
    sb.append(getHumanValue(XML_TRACKS));
    sb.append(getHumanValue(XML_TRACK_DISCOVERY_DATE));
    sb.append(getHumanValue(XML_TRACK_HITS));
    return sb.toString();
  }

  /**
   * 
   * @return associated best cover file available or null if none
   */
  public File getCoverFile() {
    File fCover = null;
    File fDir = null; // analyzed directory
    // search for local covers in all directories mapping the current track
    // to reach other devices covers and display them together
    Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(this);
    if (tracks.size() == 0) {
      return null;
    }
    // List if directories we have to look in
    HashSet<Directory> dirs = new HashSet<Directory>(2);
    for (Track track : tracks) {
      for (org.jajuk.base.File file : track.getFiles()) {
        if (file.isReady()) {
          // note that hashset ensures directory unicity
          dirs.add(file.getDirectory());
        }
      }
    }
    // look for absolute cover in collection
    for (Directory dir : dirs) {
      String sAbsolut = dir.getStringValue(XML_DIRECTORY_DEFAULT_COVER);
      if (sAbsolut != null && !sAbsolut.trim().equals("")) {
        File fAbsoluteDefault = new File(dir.getAbsolutePath() + '/' + sAbsolut);
        if (fAbsoluteDefault.canRead()) {
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
        if (files[i].canRead() // test file is readable
            && files[i].length() < MAX_COVER_SIZE * 1024) {
          // check size to avoid out of memory errors
          String sExt = Util.getExtension(files[i]);
          if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png")
              || sExt.equalsIgnoreCase("gif")) {
            if (Util.isStandardCover(files[i].getAbsolutePath())) {
              return files[i];
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
        if (files[i].canRead() // test file is readable
            && files[i].length() < MAX_COVER_SIZE * 1024) {
          // check size to avoid out of memory errors
          String sExt = Util.getExtension(files[i]);
          if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png")
              || sExt.equalsIgnoreCase("gif")) {
            return files[i];
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
    return IconLoader.ICON_ALBUM;
  }

  /**
   * @return album average rating
   */
  public long getRate() {
    float rate = 0f;
    int nb = 0;
    for (Track track : tracks) {
      rate += track.getRate();
      nb++;
    }
    return Math.round(rate / nb);
  }

  /**
   * 
   * @param size
   *          size using format width x height
   * @return album thumb for given size
   */
  public ImageIcon getThumbnail(String size) {
    File fCover = Util
        .getConfFileByPath(FILE_THUMBS + '/' + size + '/' + getID() + '.' + EXT_THUMB);
    // Check if thumb already exists
    if (!fCover.exists() || fCover.length() == 0) {
      return IconLoader.noCoversCache.get(size);
    }
    // Create the image using Toolkit and not ImageIO API to be able to
    // flush all the image data
    Image img = Toolkit.getDefaultToolkit().getImage(fCover.getAbsolutePath());
    ImageIcon icon = new ImageIcon(img);
    // Free thumb memory (DO IT AFTER FULL ImageIcon loading, see previosu line)
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
    HashSet<Style> styles = new HashSet<Style>(1);
    for (Track track : tracks) {
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
    HashSet<Author> authors = new HashSet<Author>(1);
    for (Track track : tracks) {
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
    HashSet<Year> years = new HashSet<Year>(1);
    for (Track track : tracks) {
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
    for (Track track : tracks) {
      length += track.getDuration();
    }
    return length;
  }

  /**
   * @return album nb of tracks
   */
  public int getNbOfTracks() {
    return tracks.size();
  }

  /**
   * @return album total nb of hits
   */
  public long getHits() {
    int hits = 0;
    for (Track track : tracks) {
      hits += track.getHits();
    }
    return hits;
  }

  /**
   * @return wether the album contains a least one available track
   */
  public boolean containsReadyFiles() {
    for (Track track : tracks) {
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
    if (tracks.size() > 0) {
      return tracks.iterator().next().getDiscoveryDate();
    } else {
      return null;
    }
  }

  public boolean matches(String property, String pattern) {
    if (Util.isVoid(property) || Util.isVoid(pattern)) {
      return true;
    }
    String sValue = null;
    if (XML_ALBUM.equals(property)) {
      sValue = getName2();
    } else if (XML_STYLE.equals(property)) {
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
   * Reset tracks references
   */
  protected void resetTracks() {
    tracks.clear();
  }

}
