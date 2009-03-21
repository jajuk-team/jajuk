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

package org.jajuk.services.covers;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;

import org.jajuk.ui.widgets.JajukWindow;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * A cover, encapsulates URL, files and manages cover priority to display
 */
public class Cover implements Comparable<Cover>, Const {

  /**
   * Covers type enumeration.
   * <p>
   * NO_COVER : default jajuk cover displayed when no other is available
   * </p>
   * <p>
   * LOCAL_COVER : cover located on the disk and not a standard one
   * </p>
   * <p>
   * STANDARD COVER : cover located on the disk with a obvious name (cover.png,
   * front.png...)
   * </p>
   * <p>
   * REMOTE_COVER : cover from the web (HTTP protocol)
   * </p>
   * <p>
   * SELECTED_COVER : local cover selected by user as the default local cover to
   * display in thumbs..
   * </p>
   */
  public enum CoverType {
    NO_COVER, REMOTE_COVER, LOCAL_COVER, STANDARD_COVER, SELECTED_COVER
  }

  /** Cover URL* */
  private final URL url;

  /** Cover Type */
  private final CoverType type;

  /** Associated file */
  private File file;

  /** Default cover image */
  private static final ImageIcon DEFAULT_COVER_ICON = UtilGUI.getImage(IMAGES_SPLASHSCREEN);

  /**
   * Constructor for remote covers
   * 
   * @param sUrl
   *          cover url : http url
   * @param type
   */
  public Cover(final URL url, final CoverType type) throws Exception {
    this.url = url;
    this.type = type;
    // only remote and no_cover are created by URL (file:// for no_cover, the
    // image is inside the jajuk jar)
    if (type == CoverType.REMOTE_COVER || type == CoverType.NO_COVER) {
      this.file = UtilSystem.getCachePath(url);
    }
  }

  /**
   * Constructor for local covers
   * 
   * @param file
   *          cover file
   * @param type
   */
  public Cover(final File localFile, final CoverType type) throws Exception {
    this.type = type;
    this.file = localFile;
    this.url = new URL("file://" + file.getAbsolutePath());
  }

  /*
   * (non-Javadoc) The priority order is : SELECTED > STANDARD > LOCAL > REMOTE >
   * NO_COVER
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Cover cOther) {
    // We leverage the enum ordering for comparison
    return getType().ordinal() - cOther.getType().ordinal();
  }

  /**
   * @return Returns the type.
   */
  public CoverType getType() {
    return type;
  }

  /**
   * @return Returns the sURL.
   */
  public URL getURL() {
    return url;
  }

  /**
   * Return cover image size
   * 
   * @return
   */
  public String getSize() {
    int iSize = (int) (Math.ceil(((double) file.length()) / 1024));
    return Integer.toString(iSize);
  }

  /**
   * @return Returns the image.
   */
  public Image getImage() throws Exception {
    // default cover image is cached in memory for perfs
    if (getType() == CoverType.NO_COVER) {
      return DEFAULT_COVER_ICON.getImage();
    }
    long l = System.currentTimeMillis();
    if (!file.exists() || file.length() == 0) {
      this.file = DownloadManager.downloadToCache(url);
    }
    Image image = null;
    synchronized (Cover.class) {
      image = Toolkit.getDefaultToolkit().getImage(getFile().getAbsolutePath());
      MediaTracker tracker = new MediaTracker(JajukWindow.getInstance());
      tracker.addImage(image, 1);
      tracker.waitForAll();
    }
    Log.debug("Loaded {{" + url + "}} in  " + (System.currentTimeMillis() - l) + " ms");
    return image;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "Type=" + type + " URL=" + url;
  }

  /**
   * Equals needed for consistency for sorting
   */
  @Override
  public boolean equals(Object o) {
    // this also handles null by definition
    if (!(o instanceof Cover)) {
      return false;
    }

    // we have an item of type Cover, so we can cast it safely
    Cover cOther = (Cover) o;

    // check type
    if (getType() != cOther.getType()) {
      return false;
    }

    // From here, types are equals
    return url.equals(cOther.getURL());
  }

  /**
   * 
   * @return object hashcode
   */
  @Override
  public int hashCode() {
    return this.url.hashCode() + type.ordinal();
  }

  public File getFile() {
    return this.file;
  }

}
