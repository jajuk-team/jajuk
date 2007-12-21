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
package org.jajuk.base.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;

import javax.swing.ImageIcon;

import org.jajuk.base.ItemType;
import org.jajuk.base.LogicalItem;
import org.jajuk.base.managers.AlbumManager;
import org.jajuk.base.managers.AuthorManager;
import org.jajuk.base.managers.StyleManager;
import org.jajuk.base.managers.TrackManager;
import org.jajuk.base.managers.TypeManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.Resources.ConfKeys;
import org.jajuk.util.Resources.XML;

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
  public Track(final String sId, final String sName, final Album album, final Style style, final Author author, final long length,
      final Year year, final long lOrder, final Type type) {
    super(sId, sName);
    // album
    this.album = album;
    getProperties().set(XML.ALBUM, album.getID());
    // style
    this.style = style;
    getProperties().set(XML.STYLE, style.getID());
    // author
    this.author = author;
    getProperties().set(XML.AUTHOR, author.getID());
    // Length
    this.length = length;
    getProperties().set(XML.TRACK_LENGTH, length);
    // Type
    this.type = type;
    getProperties().set(XML.TYPE, type.getID());
    // Year
    this.year = year;
    getProperties().set(XML.YEAR, year.getID());
    // Order
    getProperties().set(XML.TRACK_ORDER, lOrder);
    // Rate
    getProperties().set(XML.TRACK_RATE, 0l);
    // Hits
    getProperties().set(XML.TRACK_HITS, 0l);
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    String sOut = "Track[ID=" + getID() + " Name={{" + getName() + "}} " + album + " " + style
        + " " + author + " Length=" + length + " Year=" + year.getValue() + " Rate=" + getRate()
        + " " + type + " Hits=" + getHits() + " Addition date=" + getAdditionDate() + " Comment="
        + getComment() + " order=" + getOrder() + " Nb of files=" + alFiles.size() + "]";
    for (int i = 0; i < alFiles.size(); i++) {
      sOut += '\n' + alFiles.get(i).toString();
    }
    return sOut;
  }

  /**
   * @return a human representation of all concatenated properties
   */
  @Override
  public String getAny() {
    // rebuild any
    final StringBuilder sb = new StringBuilder(100);
    sb.append(super.getAny()); // add all files-based properties
    // now add others properties
    sb.append(getName());
    sb.append(getStyle().getName2());
    sb.append(getAuthor().getName2());
    sb.append(getAlbum().getName2());
    sb.append(getDuration());
    sb.append(getRate());
    sb.append(getValue(XML.TRACK_COMMENT));// custom properties now
    sb.append(getValue(XML.TRACK_ORDER));// custom properties now
    // Add all files absolute paths
    for (final File file : getFiles()) {
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
  public int compareTo(final Object o) {
    final Track otherTrack = (Track) o;
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
  public ArrayList<org.jajuk.base.items.File> getFiles() {
    return alFiles;
  }

  /**
   * @return ready files
   */
  public ArrayList<File> getReadyFiles() {
    final ArrayList<File> alReadyFiles = new ArrayList<File>(alFiles.size());
    for (final File file : alFiles) {
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
  public ArrayList<File> getReadyFiles(final HashSet filter) {
    final ArrayList<File> alReadyFiles = new ArrayList<File>(alFiles.size());
    for (final File file : alFiles) {
      if (file.isReady() && ((filter == null) || filter.contains(file))) {
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
        public int compare(final File file1, final File file2) {
          final long lQuality1 = file1.getQuality();
          boolean bMounted1 = file1.isReady();
          final long lQuality2 = file2.getQuality(); // quality for
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
    return getLongValue(XML.TRACK_HITS);
  }

  /**
   * @return
   */
  public String getComment() {
    return getStringValue(XML.TRACK_COMMENT);
  }

  /**
   * Get track number
   *
   * @return
   */
  public long getOrder() {
    return getLongValue(XML.TRACK_ORDER);
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
    return getLongValue(XML.TRACK_RATE);
  }

  /**
   * @return
   */
  public Date getAdditionDate() {
    return getDateValue(XML.TRACK_ADDED);
  }

  /**
   * @return
   */
  public org.jajuk.base.items.Type getTrackType() {
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
  public void addFile(final File file) {
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
  public void removeFile(final File file) {
    alFiles.remove(file);
  }

  /**
   * @param hits
   *          The iHits to set.
   */
  public void setHits(final long hits) {
    getProperties().set(XML.TRACK_HITS, hits);
  }

  public void incHits() {
    setHits(getHits() + 1);
  }

  /**
   * @param rate
   *          The lRate to set.
   */
  public void setRate(final long rate) {
    getProperties().set(XML.TRACK_RATE, rate);
    // Store max rate
    if (rate > ((TrackManager) ItemType.Track.getManager()).getMaxRate()) {
      ((TrackManager) ItemType.Track.getManager()).setMaxRate(rate);
    }
  }

  /**
   * @param rate
   *          The lRate to set.
   */
  public void setComment(final String sComment) {
    getProperties().set(XML.TRACK_COMMENT, sComment);
  }

  /**
   * @param additionDate
   *          The sAdditionDate to set.
   */
  public void setAdditionDate(final Date additionDate) {
    getProperties().set(XML.TRACK_ADDED, additionDate);
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
    if ((getPlayeableFile(true) != null)
        || (ConfigurationManager.getBoolean(ConfKeys.OPTIONS_HIDE_UNMOUNTED) == false)) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  final public String getLabel() {
    return XML.TRACK;
  }

  /**
   * Get item description
   */
  @Override
  public String getDescription() {
    return Messages.getString("Item_Track") + " : " + getName();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(final String sKey) {
    if (XML.ALBUM.equals(sKey)) {
      final Album album = ((AlbumManager) ItemType.Album.getManager()).getAlbumByID(getStringValue(sKey));
      if (album != null) { // can be null after a fresh change
        return album.getName2();

      }
      return null;
    } else if (XML.AUTHOR.equals(sKey)) {
      final Author author = ((AuthorManager) ItemType.Author.getManager()).getAuthorByID(getStringValue(sKey));
      if (author != null) { // can be null after a fresh change
        return author.getName2();
      }
      return null;
    } else if (XML.STYLE.equals(sKey)) {
      final Style style = ((StyleManager) ItemType.Style.getManager()).getStyleByID(getStringValue(sKey));
      if (style != null) { // can be null after a fresh change
        return style.getName2();
      }
      return null;
    } else if (XML.TRACK_LENGTH.equals(sKey)) {
      return Util.formatTimeBySec(length, false);
    } else if (XML.TYPE.equals(sKey)) {
      return (((TypeManager) ItemType.Type.getManager()).getTypeByID(getStringValue(sKey))).getName();
    } else if (XML.YEAR.equals(sKey)) {
      return getStringValue(sKey);
    } else if (XML.FILES.equals(sKey)) {
      final StringBuilder sbOut = new StringBuilder();

      for (final File file : alFiles) {
        sbOut.append(file.getAbsolutePath());
        sbOut.append(',');
      }
      return sbOut.substring(0, sbOut.length() - 1); // remove last
      // ','
    } else if (XML.TRACK_ADDED.equals(sKey)) {
      return Util.getLocaleDateFormatter().format(getAdditionDate());
    } else if (XML.ANY.equals(sKey)) {
      return getAny();
    } else {// default
      return super.getHumanValue(sKey);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIcon()
   */
  @Override
  public ImageIcon getIcon() {
    return IconLoader.ICON_TRACK;
  }

}
