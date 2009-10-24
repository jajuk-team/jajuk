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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.jajuk.services.core.RatingManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

/**
 * A track
 * <p>
 * Logical item
 */
public class Track extends LogicalItem implements Comparable<Track> {

  /** Track album* */
  private final Album album;

  /** Track style */
  private final Style style;

  /** Track author */
  private final Author author;

  /** Track length */
  private final long length;

  /** Track year */
  private final Year year;

  /** Track type */
  private final Type type;

  /** Track associated files */
  private final List<File> alFiles = new ArrayList<File>(1);

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
   * @param sAlbumArtist
   */
  public Track(String sId, String sName, Album album, Style style, Author author, long length,
      Year year, long lOrder, Type type, long lDiscNumber) {
    super(sId, sName);
    // album
    this.album = album;
    setProperty(Const.XML_ALBUM, album.getID());
    // style
    this.style = style;
    setProperty(Const.XML_STYLE, style.getID());
    // author
    this.author = author;
    setProperty(Const.XML_AUTHOR, author.getID());
    // Length
    this.length = length;
    setProperty(Const.XML_TRACK_LENGTH, length);
    // Type
    this.type = type;
    setProperty(Const.XML_TYPE, type.getID());
    // Year
    this.year = year;
    setProperty(Const.XML_YEAR, year.getID());
    // Order
    setProperty(Const.XML_TRACK_ORDER, lOrder);
    // Order
    setProperty(Const.XML_TRACK_DISC_NUMBER, lDiscNumber);
    // Rate
    setProperty(Const.XML_TRACK_RATE, 0l);
    // Hits
    setProperty(Const.XML_TRACK_HITS, 0l);
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    StringBuilder sOut = new StringBuilder();

    sOut.append("Track[ID=").append(getID()).append(" Name={{").append(getName()).append("}} ")
        .append(album).append(" ").append(style).append(" ").append(author).append(" Length=")
        .append(length).append(" Year=").append(year.getValue()).append(" Rate=").append(getRate())
        .append(" ").append(type).append(" Hits=").append(getHits()).append(" Addition date=")
        .append(getDiscoveryDate()).append(" Comment=").append(getComment()).append(" order=")
        .append(getOrder()).append(" Nb of files=").append(alFiles.size()).append(" Album artist=")
        .append(getAlbumArtist()).append(" Disc=").append(getDiscNumber()).append("]");
    for (int i = 0; i < alFiles.size(); i++) {
      sOut.append('\n').append(alFiles.get(i).toString());
    }
    return sOut.toString();
  }

  /**
   * @return a human representation of all concatenated properties
   */
  @Override
  public String getAny() {
    // rebuild any
    StringBuilder sb = new StringBuilder(100);
    sb.append(super.getAny()); // add all track-based properties
    // Add all files absolute paths
    for (File file : getFiles()) {
      sb.append(file.getAbsolutePath());
    }
    return sb.toString();
  }

  /**
   * Default comparator for tracks, not used for sorting (use TrackComparator
   * for that) But only for storage.
   * 
   * @param other
   *          track to be compared
   * @return comparison result
   */
  public int compareTo(Track otherTrack) {
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
  public List<org.jajuk.base.File> getFiles() {
    return alFiles;
  }

  /**
   * @return all associated files
   * @param filter
   *          files we want to deal with, null means no filter
   */
  public List<org.jajuk.base.File> getFiles(Set<File> filter) {
    List<File> out = new ArrayList<File>(alFiles.size());
    for (File file : alFiles) {
      if (filter == null || filter.contains(file)) {
        out.add(file);
      }
    }
    return out;
  }

  /**
   * @return ready files
   */
  public List<File> getReadyFiles() {
    List<File> alReadyFiles = new ArrayList<File>(alFiles.size());
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
  public List<File> getReadyFiles(Set<File> filter) {
    List<File> alReadyFiles = new ArrayList<File>(alFiles.size());
    for (File file : alFiles) {
      if (file.isReady() && (filter == null || filter.contains(file))) {
        alReadyFiles.add(file);
      }
    }
    return alReadyFiles;
  }

  /**
   * Get sum size of all files this track map to
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
   * @return best file to play for this track or null if none available
   * @param bHideUnmounted
   *          Do we return unmounted files
   */
  public File getPlayeableFile(boolean bHideUnmounted) {
    File fileOut = null;
    final List<File> alMountedFiles = new ArrayList<File>(2);

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
    return getLongValue(Const.XML_TRACK_HITS);
  }

  /**
   * @return
   */
  public String getComment() {
    return getStringValue(Const.XML_TRACK_COMMENT);
  }

  /**
   * Get track number
   * 
   * @return
   */
  public long getOrder() {
    return getLongValue(Const.XML_TRACK_ORDER);
  }

  /**
   * Get disc number
   * 
   * @return
   */
  public long getDiscNumber() {
    return getLongValue(Const.XML_TRACK_DISC_NUMBER);
  }

  /**
   * Get album artist
   * 
   * @return
   */
  public String getAlbumArtist() {
    return album.getAlbumArtist();
  }

  /**
   * Get translated album artist or author if album artist not available
   * 
   * @return
   */
  public String getAlbumArtistOrArtist() {
    return album.getAlbumArtistOrArtist();
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
  @Override
  public long getRate() {
    return getLongValue(Const.XML_TRACK_RATE);
  }

  /**
   * @return the date where the track has been discovered (added into the
   *         collection)
   */
  public Date getDiscoveryDate() {
    return getDateValue(Const.XML_TRACK_DISCOVERY_DATE);
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
   * @param hits
   *          The iHits to set.
   */
  public void setHits(long hits) {
    setProperty(Const.XML_TRACK_HITS, hits);
    // Store max playcount
    if (hits > RatingManager.getMaxPlaycount()) {
      RatingManager.setMaxPlaycount(hits);
    }
  }

  /**
   * Increase playcount number
   */
  public void incHits() {
    long value = getHits() + 1;
    setHits(value);
  }

  /**
   * Set track preference (from -3 to 3: -3: hate, -2=dislike, -1=poor, +1=like,
   * +2=love +3=crazy). The preference is a factor given by the user to increase
   * or decrease a track rate.
   * 
   * @param preference
   *          from -3 to 3
   */
  public void setPreference(long preference) {
    Log.debug("Changed preference of " + getID() + "=" + preference);
    if (preference >= -3l && preference <= 3l) {
      setProperty(Const.XML_TRACK_PREFERENCE, preference);
    } else {
      setProperty(Const.XML_TRACK_PREFERENCE, 0l);
      Log.debug("Out of bounds preference for : " + getID());
    }
    updateRate();
  }

  /**
   * Compute final track rate
   * 
   * @see http://trac.jajuk.info/ticket/1179
   */
  public void updateRate() {
    try {
      // rate contains final rate [0,100]
      long rate = 0;
      // Normalize values to avoid division by zero
      long duration = getDuration();
      long playcount = getHits();
      // Playcount must be > 0 to avoid divisions by zero and log(0) operations
      if (playcount <= 0) {
        playcount = 1;
      }
      float playtimeRate = 0.5f;
      if (duration == 0) {
        // If duration = 0, always set playtimeRate to 0.5
        Log.info("Duration = 0 for: {{" + getName() + "}}. Playtime forced to 0.5");
      } else {
        // Compute playtime rate = total play time / (play count * track length)
        playtimeRate = (float) getLongValue(Const.XML_TRACK_TOTAL_PLAYTIME)
            / (playcount * duration);
      }
      // playtimeRate can be > 1 because of player impl duration computation
      // precision issue or if user seeks back into the track
      // set =1.
      if (playtimeRate > 1) {
        Log.warn("Playtime rate > 1 for: {{" + getName() + "}} value=" + playtimeRate);
        // We reset tpt and hits to
        // make things clear and to avoid increasing the error with time
        setProperty(Const.XML_TRACK_TOTAL_PLAYTIME, duration * playcount);
        playtimeRate = 1f;
      }

      // compute the playcount rate (logarithmic scale to take number of plays
      // into account)
      // playcountRate = ln(track playcount)/ln(max playcount)
      long maxPlayCount = RatingManager.getMaxPlaycount();
      if (maxPlayCount <= 0) {
        maxPlayCount = 1;
      }
      float playcountRate = (float) (Math.log(playcount) / Math.log(maxPlayCount));
      // Intermediate rate is a mix between playtime and playcount rates with
      // factor 0.75 for the first one and 0.25 for the second
      float intermediateRate = (0.75f * playtimeRate) + (0.25f * playcountRate);
      // Final rate is intermediateRate in whish we apply the user preference
      // from
      // -3 (hate) to 3 (adore)
      long preference = getLongValue(Const.XML_TRACK_PREFERENCE);
      long absPreference = Math.abs(preference);
      rate = Math.round(100 * (intermediateRate + (preference + absPreference) / 2)
          / (absPreference + 1));
      // Apply new rate
      setRate(rate);
    } catch (Exception e) {
      // We catch any arithmetic issue here to avoid preventing next track
      // startup
      Log.error(e);
    }
  }

  /**
   * @param rate
   *          The lRate to set.
   */
  protected void setRate(long rate) {
    setProperty(Const.XML_TRACK_RATE, rate);
    RatingManager.setRateHasChanged(true);
  }

  /**
   * @param rate
   *          The lRate to set.
   */
  public void setComment(String sComment) {
    setProperty(Const.XML_TRACK_COMMENT, sComment);
  }

  /**
   * @param additionDate
   *          The sAdditionDate to set.
   */
  public void setDiscoveryDate(Date additionDate) {
    setProperty(Const.XML_TRACK_DISCOVERY_DATE, additionDate);
  }

  /**
   * Return whether this item should be hidden with hide option
   * 
   * @return whether this item should be hidden with hide option
   */
  public boolean shouldBeHidden() {
    if (getPlayeableFile(true) != null || !Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)) {
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
  public final String getLabel() {
    return XML_TRACK;
  }

  /**
   * Get item description
   */
  @Override
  public String getDesc() {
    return Messages.getString("Item_Track") + " : " + getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(String sKey) {
    if (Const.XML_ALBUM.equals(sKey)) {
      Album lAlbum = AlbumManager.getInstance().getAlbumByID(getStringValue(sKey));
      if (lAlbum != null) { // can be null after a fresh change
        return lAlbum.getName2();
      }
      return null;
    } else if (Const.XML_AUTHOR.equals(sKey)) {
      Author lAuthor = AuthorManager.getInstance().getAuthorByID(getStringValue(sKey));
      if (lAuthor != null) { // can be null after a fresh change
        return lAuthor.getName2();
      }
      return null;
    } else if (Const.XML_STYLE.equals(sKey)) {
      Style lStyle = StyleManager.getInstance().getStyleByID(getStringValue(sKey));
      if (lStyle != null) { // can be null after a fresh change
        return lStyle.getName2();
      }
      return null;
    } else if (Const.XML_TRACK_LENGTH.equals(sKey)) {
      return UtilString.formatTimeBySec(length);
    } else if (Const.XML_TYPE.equals(sKey)) {
      return TypeManager.getInstance().getTypeByID(getStringValue(sKey)).getName();
    } else if (Const.XML_YEAR.equals(sKey)) {
      return getStringValue(sKey);
    } else if (Const.XML_FILES.equals(sKey)) {
      final StringBuilder sbOut = new StringBuilder();

      for (final File file : alFiles) {
        sbOut.append(file.getAbsolutePath());
        sbOut.append(',');
      }
      return sbOut.substring(0, sbOut.length() - 1); // remove trailing coma
    } else if (Const.XML_TRACK_DISCOVERY_DATE.equals(sKey)) {
      return UtilString.getLocaleDateFormatter().format(getDiscoveryDate());
    } else if (Const.XML_ANY.equals(sKey)) {
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
    return IconLoader.getIcon(JajukIcons.TRACK);
  }

  /**
   * 
   * @return a list of associated files in format : file1,file2...
   */
  public String getFilesString() {
    StringBuilder sb = new StringBuilder(100);
    for (File file : alFiles) {
      sb.append(file.getName());
      sb.append(',');
    }
    // Remove trailing ','
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }
}
