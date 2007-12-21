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

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import org.jajuk.base.ItemType;
import org.jajuk.base.PhysicalItem;
import org.jajuk.base.managers.DirectoryManager;
import org.jajuk.base.managers.TypeManager;
import org.jajuk.util.CollectionUtil;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.UrlImageIcon;
import org.jajuk.util.Util;
import org.jajuk.util.Resources.ConfKeys;
import org.jajuk.util.Resources.XML;
import org.jajuk.util.collections.aggregators.JajukFileAggregator;
import org.jajuk.util.log.Log;

/**
 * A music file to be played
 * <p>
 * Physical item
 */
public class File extends PhysicalItem implements Comparable<File> {

  private static final long serialVersionUID = 1L;

  public static String aggregate(final java.util.Collection<File> c, final String delimiter) {
    return (CollectionUtil.join(c, new JajukFileAggregator(delimiter)));
  }

  /** Parent directory */
  protected final Directory directory;

  /** Associated track */
  protected Track track;

  /** IO file associated with this file */
  private java.io.File fio;

  /**
   * File instanciation
   *
   * @param sId
   * @param sName
   * @param directory
   * @param track
   * @param lSize
   * @param sQuality
   */
  public File(final String sId, final String sName, final Directory directory, final Track track,
      final long lSize, final long lQuality) {
    super(sId, sName);
    this.directory = directory;
    getProperties().set(XML.DIRECTORY, directory.getID().intern());
    this.track = track;
    getProperties().set(XML.TRACK, track.getID());
    getProperties().set(XML.SIZE, lSize);
    getProperties().set(XML.QUALITY, lQuality);
  }

  /**
   * Alphabetical comparator used to display ordered lists of files
   * <p>
   * Sort ignoring cases but different items with different cases should be
   * distinct before being added into bidimap
   * </p>
   *
   * @param other
   *          file to be compared
   * @return comparaison result
   */
  public int compareTo(final File otherFile) {
    // Perf: leave if directories are equals
    if (otherFile.equals(this)) {
      return 0;
    }
    // Begin by comparing file parent directory for perf
    if (directory.equals(otherFile.getDirectory())) {
      // If both files are in the same directory, sort by track order
      final int iOrder = (int) getTrack().getOrder();
      final int iOrderOther = (int) otherFile.getTrack().getOrder();
      if (iOrder != iOrderOther) {
        return iOrder - iOrderOther;
      }
      // if same order too, simply compare file names
      final String sAbs = getName();
      final String sOtherAbs = otherFile.getName();
      // never return 0 here, because bidimap needs to distinct items
      final int comp = sAbs.compareToIgnoreCase(sOtherAbs);
      if (comp == 0) {
        return sAbs.compareTo(sOtherAbs);
      }
      return comp;
    } else {
      // Files are in different directories, sort by parent directory
      return getDirectory().compareTo(otherFile.getDirectory());
    }
  }

  /**
   * Return absolute file path name
   *
   * @return String
   */
  public String getAbsolutePath() {
    final StringBuilder sbOut = new StringBuilder(getDevice().getUrl()).append(
        getDirectory().getRelativePath()).append(java.io.File.separatorChar).append(getName());
    return sbOut.toString();
  }

  /**
   * @return a human representation of all concatenated properties
   */
  @Override
  public String getAny() {
    // rebuild any
    final StringBuilder sb = new StringBuilder(100);
    final File file = this;
    final Track track = file.getTrack();
    sb.append(super.getAny()); // add all files-based properties
    // now add others properties
    sb.append(file.getDirectory().getDevice().getName());
    sb.append(track.getName());
    sb.append(track.getStyle().getName2());
    sb.append(track.getAuthor().getName2());
    sb.append(track.getAlbum().getName2());
    sb.append(track.getDuration());
    sb.append(track.getRate());
    sb.append(track.getValue(XML.TRACK_COMMENT));// custom properties now
    sb.append(track.getValue(XML.TRACK_ORDER));// custom properties now
    return sb.toString();
  }

  /**
   * Get item description
   */
  @Override
  public String getDescription() {
    return Messages.getString("Item_File") + " : " + getName();
  }

  /**
   * @return associated device
   */
  public Device getDevice() {
    return directory.getDevice();
  }

  /**
   * @return
   */
  public Directory getDirectory() {
    return directory;
  }

  /**
   * @return associated type
   */
  public Type getFileType() {
    final String extension = Util.getExtension(getIO());
    if (extension != null) {
      return ((TypeManager) ItemType.Type.getManager()).getTypeByExtension(extension);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
   */
  @Override
  public String getHumanValue(final String sKey) {
    if (XML.DIRECTORY.equals(sKey)) {
      final Directory dParent = ((DirectoryManager) ItemType.Directory.getManager())
          .getDirectoryByID(getStringValue(sKey));
      return dParent.getFio().getAbsolutePath();
    } else if (XML.TRACK.equals(sKey)) {
      return getTrack().getName();
    } else if (XML.SIZE.equals(sKey)) {
      return (getSize() / 1048576) + Messages.getString("FilesTreeView.54");
    } else if (XML.QUALITY.equals(sKey)) {
      return getQuality() + Messages.getString("FIFO.13");
    } else if (XML.ALBUM.equals(sKey)) {
      return getTrack().getAlbum().getName2();
    } else if (XML.STYLE.equals(sKey)) {
      return getTrack().getStyle().getName2();
    } else if (XML.AUTHOR.equals(sKey)) {
      return getTrack().getAuthor().getName2();
    } else if (XML.TRACK_LENGTH.equals(sKey)) {
      return Util.formatTimeBySec(getTrack().getDuration(), false);
    } else if (XML.TRACK_RATE.equals(sKey)) {
      return Long.toString(getTrack().getRate());
    } else if (XML.DEVICE.equals(sKey)) {
      return getDirectory().getDevice().getName();
    } else if (XML.ANY.equals(sKey)) {
      return getAny();
    } else {
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
    ImageIcon icon = null;
    final String ext = Util.getExtension(getIO());
    final Type type = ((TypeManager) ItemType.Type.getManager()).getTypeByExtension(ext);
    // Find associated icon with this type
    URL iconUrl = null;
    String sIcon;
    if (type != null) {
      sIcon = (String) type.getProperties().get(XML.TYPE_ICON);
      try {
        iconUrl = new URL(sIcon);
      } catch (final MalformedURLException e) {
        Log.error(e);
      }
    }
    if (iconUrl == null) {
      icon = IconLoader.ICON_TYPE_WAV;
    } else {
      icon = new UrlImageIcon(iconUrl);
    }
    return icon;
  }

  /**
   * Return Io file associated with this file
   *
   * @return
   */
  public java.io.File getIO() {
    if (fio == null) {
      fio = new java.io.File(getAbsolutePath());
    }
    return fio;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  final public String getLabel() {
    return XML.FILE;
  }

  /**
   * @return
   */
  public long getQuality() {
    return getLongValue(XML.QUALITY);
  }

  /**
   * @return
   */
  public long getSize() {
    return getLongValue(XML.SIZE);
  }

  /**
   * @return
   */
  public Track getTrack() {
    return track;
  }

  /**
   * Return true is the specified directory is an ancestor for this file
   *
   * @param directory
   * @return
   */
  public boolean hasAncestor(final Directory directory) {
    Directory dirTested = getDirectory();
    while (true) {
      if (dirTested.equals(directory)) {
        return true;
      } else {
        dirTested = dirTested.getParentDirectory();
        if (dirTested == null) {
          return false;
        }
      }
    }
  }

  /**
   * Return true if the file can be accessed right now
   *
   * @return true the file can be accessed right now
   */
  public boolean isReady() {
    if (getDirectory().getDevice().isMounted()) {
      return true;
    }
    return false;
  }

  /**
   * Return true if the file is currently refreshed or synchronized
   *
   * @return true if the file is currently refreshed or synchronized
   */
  public boolean isScanned() {
    if (getDirectory().getDevice().isRefreshing() || getDirectory().getDevice().isSynchronizing()) {
      return true;
    }
    return false;
  }

  /** Reset pre-calculated paths* */
  protected void reset() {
    // sAbs = null;
    fio = null;
  }

  /**
   * @param track
   *          The track to set.
   */
  public void setTrack(final Track track) {
    this.track = track;
    getProperties().set(XML.TRACK, track.getID());
  }

  /**
   * Return whether this item should be hidden with hide option
   *
   * @return whether this item should be hidden with hide option
   */
  public boolean shouldBeHidden() {
    if (getDirectory().getDevice().isMounted()
        || (ConfigurationManager.getBoolean(ConfKeys.OPTIONS_HIDE_UNMOUNTED) == false)) {
      return false;
    }
    return true;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "File[ID=" + getID() + " Name={{" + getName() + "}} Dir=" + directory + " Size="
        + getSize() + " Quality=" + getQuality() + "]";
  }

  /**
   * String representation as displayed in a search result
   */
  public String toStringSearch() {
    final StringBuilder sb = new StringBuilder(track.getStyle().getName2()).append('/').append(
        track.getAuthor().getName2()).append('/').append(track.getAlbum().getName2()).append('/')
        .append(track.getName()).append(" [").append(directory.getName()).append('/').append(
            getName()).append(']');
    return sb.toString();
  }
}
