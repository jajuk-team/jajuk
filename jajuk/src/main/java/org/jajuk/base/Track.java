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

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.ImageIcon;

/**
 * A track
 * <p>
 * Logical item
 */
public class Track extends LogicalItem implements Comparable {

  private static final long serialVersionUID = 1L;

  /** Track album* */
  private final Album album;

  /** Track style */
  private final Style style;

  /** Track author in sec */
  private final Author author;

  /** Track length */
  private final long length;

  /** Track year */
  private final Year year;

  /** Track type */
  private final Type type;

  /** Track associated files */
  private ArrayList<File> alFiles = new ArrayList<File>(1);

  /** Number of hits for current jajuk session */
  private int iSessionHits = 0;

  /**
   * Track constructor
   * 
   * @param sId
   * @param sName
   * @param album
   * @param style
   * @param author
   * @param length
   * @param sYear
   * @param type
   * @param sAdditionDate
   */
  public Track(String sId, String sName, Album album, Style style, Author author, long length,
      Year year, long lOrder, Type type) {
    super(sId, sName);
    // album
    this.album = album;
    setProperty(XML_ALBUM, album.getID());
    // style
    this.style = style;
    setProperty(XML_STYLE, style.getID());
    // author
    this.author = author;
    setProperty(XML_AUTHOR, author.getID());
    // Length
    this.length = length;
    setProperty(XML_TRACK_LENGTH, length);
    // Type
    this.type = type;
    setProperty(XML_TYPE, type.getID());
    // Year
    this.year = year;
    setProperty(XML_YEAR, year.getID());
    // Order
    setProperty(XML_TRACK_ORDER, lOrder);
    // Rate
    setProperty(XML_TRACK_RATE, 0l);
    // Hits
    setProperty(XML_TRACK_HITS, 0l);
  }

  /**
   * toString method
   */
  public String toString() {
    String sOut = "Track[ID=" + getID() + " Name={{" + getName() + "}} " + album + " " + style
        + " " + author + " Length=" + length + " Year=" + year.getValue() + " Rate=" + getRate()
        + " " + type + " Hits=" + getHits() + " Addition date=" + getDiscoveryDate() + " Comment="
        + getComment() + " order=" + getOrder() + " Nb of files=" + alFiles.size() + "]";
    for (int i = 0; i < alFiles.size(); i++) {
      sOut += '\n' + alFiles.get(i).toString();
    }
    return sOut;
  }

  /**
   * @return a human representation of all concatenated properties
   */
  public String getAny() {
    // rebuild any
    StringBuilder sb = new StringBuilder(100);
    sb.append(super.getAny()); // add all files-based properties
    // now add others properties
    sb.append(getName());
    sb.append(getStyle().getName2());
    sb.append(getAuthor().getName2());
    sb.append(getAlbum().getName2());
    sb.append(getDuration());
    sb.append(getRate());
    sb.append(getValue(XML_TRACK_COMMENT));// custom properties now
    sb.append(getValue(XML_TRACK_ORDER));// custom properties now
    // Add all files absolute paths
    for (File file : getFiles()) {
      sb.append(file.getAbsolutePath());
    }
    return sb.toString();
  }

  /**
   * Default comparator for tracks, not used for sorting (use TrackComparator
   * for that) But only for storage. We must make sure of unicity inside bidimap
   * 
   * @param other
   *          track to be compared
   * @return comparaison result
   */
  public int compareTo(Object o) {
    Track otherTrack = (Track) o;
    return getID().compareTo(otherTrack.getID());
  }

  /**
   * @return
   */
  public Album getAlbum() {
    return album;
  }

  /**
   * @return all associated files
   */
  public ArrayList<org.jajuk.base.File> getFiles() {
    return alFiles;
  }

  /**
   * @return ready files
   */
  public ArrayList<File> getReadyFiles() {
    ArrayList<File> alReadyFiles = new ArrayList<File>(alFiles.size());
    for (File file : alFiles) {
      if (file.isReady()) {
        alReadyFiles.add(file);
      }
    }
    return alReadyFiles;
  }

  /**
   * @return ready files with given filter
   * @param filter
   *          files we want to deal with, null means no filter
   */
  public ArrayList<File> getReadyFiles(HashSet filter) {
    ArrayList<File> alReadyFiles = new ArrayList<File>(alFiles.size());
    for (File file : alFiles) {
      if (file.isReady() && (filter == null || filter.contains(file))) {
        alReadyFiles.add(file);
      }
    }
    return alReadyFiles;
  }

  /**
   * Get additionned size of all files this track map to
   * 
   * @return the total size
   */
  public long getTotalSize() {
    long l = 0;

    for (final File file : alFiles) {
      l += file.getSize();
    }
    return l;
  }

  /**
   * @return best file to play for this track
   * @param bHideUnmounted
   *          Do we return unmounted files
   */
  public File getPlayeableFile(boolean bHideUnmounted) {
    File fileOut = null;
    final ArrayList<File> alMountedFiles = new ArrayList<File>(2);

    // firstly, filter mounted files if needed
    for (final File file : alFiles) {
      if (!bHideUnmounted || file.isReady()) {
        alMountedFiles.add(file);
      }
    }
    if (alMountedFiles.size() == 1) {
      fileOut = alMountedFiles.get(0);
    } else if (alMountedFiles.size() > 0) {
      // then keep best quality and mounted first
      Collections.sort(alMountedFiles, new Comparator<File>() {
        public int compare(File file1, File file2) {
          long lQuality1 = file1.getQuality();
          boolean bMounted1 = file1.isReady();
          long lQuality2 = file2.getQuality(); // quality for
          // out file
          boolean bMounted2 = file2.isReady();
          if (bMounted1 && !bMounted2) {// first item mounted,
            // not second
            return 1;
          } else if (!bMounted1 && bMounted2) { // second
            // mounted, not
            // the first
            return -1;
          } else { // both mounted or unmounted, compare quality
            return (int) (lQuality1 - lQuality2);
          }
        }
      });
      fileOut = alMountedFiles.get(alMountedFiles.size() - 1);
    }
    return fileOut;
  }

  /**
   * @return
   */
  public long getHits() {
    return getLongValue(XML_TRACK_HITS);
  }

  /**
   * @return
   */
  public String getComment() {
    return getStringValue(XML_TRACK_COMMENT);
  }

  /**
   * Get track number
   * 
   * @return
   */
  public long getOrder() {
    return getLongValue(XML_TRACK_ORDER);
  }

  /**
   * @return
   */
  public Year getYear() {
    return year;
  }

  /**
   * @return length in sec
   */
  public long getDuration() {
    return length;
  }

  /**
   * @return
   */
  public long getRate() {
    return getLongValue(XML_TRACK_RATE);
  }

  /**
   * @return the date where the track has been discovered (added into the
   *         collection)
   */
  public Date getDiscoveryDate() {
    return getDateValue(XML_TRACK_ADDED);
  }

  /**
   * @return
   */
  public Type getType() {
    return type;
  }

  /**
   * @return
   */
  public Author getAuthor() {
    return author;
  }

  /**
   * @return
   */
  public Style getStyle() {
    return style;
  }

  /**
   * Add an associated file
   * 
   * @param file
   */
  public void addFile(File file) {
    // make sure a file will be referenced by only one track (first found)
    if (!alFiles.contains(file) && file.getTrack().equals(this)) {
      alFiles.add(file);
    }
  }

  /**
   * Remove an associated file
   * 
   * @param file
   */
  public void removeFile(File file) {
    alFiles.remove(file);
  }

  /**
   * @param hits
   *          The iHits to set.
   */
  public void setHits(long hits) {
    setProperty(XML_TRACK_HITS, hits);
  }

  public void incHits() {
    setHits(getHits() + 1);
  }

  /**
   * @param rate
   *          The lRate to set.
   */
  public void setRate(long rate) {
    setProperty(XML_TRACK_RATE, rate);
    // Store max rate
    if (rate > TrackManager.getInstance().getMaxRate()) {
      TrackManager.getInstance().setMaxRate(rate);
    }
  }

  /**
   * @param rate
   *          The lRate to set.
   */
  public void setComment(String sComment) {
    setProperty(XML_TRACK_COMMENT, sComment);
  }

  /**
   * @param additionDate
   *          The sAdditionDate to set.
   */
  public void setDiscoveryDate(Date additionDate) {
    setProperty(XML_TRACK_ADDED, additionDate);
  }

  /**
   * @return Returns the iSessionHits.
   */
  public int getSessionHits() {
    return iSessionHits;
  }

  /**
   * @param sessionHits
   *          The iSessionHits to inc.
   */
  public void incSessionHits() {
    iSessionHits++;
  }

  /**
   * Return whether this item should be hidden with hide option
   * 
   * @return whether this item should be hidden with hide option
   */
  public boolean shouldBeHidden() {
    if (getPlayeableFile(true) != null
        || ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED) == false) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  final public String getLabel() {
    return XML_TRACK;
  }

  /**
   * Get item description
   */
  public String getDesc() {
    return Messages.getString("Item_Track") + " : " + getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  public String getHumanValue(String sKey) {
    if (XML_ALBUM.equals(sKey)) {
      Album album = AlbumManager.getInstance().getAlbumByID(getStringValue(sKey));
      if (album != null) { // can be null after a fresh change
        return album.getName2();

      }
      return null;
    } else if (XML_AUTHOR.equals(sKey)) {
      Author author = AuthorManager.getInstance().getAuthorByID(getStringValue(sKey));
      if (author != null) { // can be null after a fresh change
        return author.getName2();
      }
      return null;
    } else if (XML_STYLE.equals(sKey)) {
      Style style = StyleManager.getInstance().getStyleByID(getStringValue(sKey));
      if (style != null) { // can be null after a fresh change
        return style.getName2();
      }
      return null;
    } else if (XML_TRACK_LENGTH.equals(sKey)) {
      return Util.formatTimeBySec(length, false);
    } else if (XML_TYPE.equals(sKey)) {
      return (TypeManager.getInstance().getTypeByID(getStringValue(sKey))).getName();
    } else if (XML_YEAR.equals(sKey)) {
      return getStringValue(sKey);
    } else if (XML_FILES.equals(sKey)) {
      final StringBuilder sbOut = new StringBuilder();

      for (final File file : alFiles) {
        sbOut.append(file.getAbsolutePath());
        sbOut.append(',');
      }
      return sbOut.substring(0, sbOut.length() - 1); // remove last
      // ','
    } else if (XML_TRACK_ADDED.equals(sKey)) {
      return Util.getLocaleDateFormatter().format(getDiscoveryDate());
    } else if (XML_ANY.equals(sKey)) {
      return getAny();
    } else {// default
      return super.getHumanValue(sKey);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIconRepresentation()
   */
  @Override
  public ImageIcon getIconRepresentation() {
    return IconLoader.ICON_TRACK;
  }

}
