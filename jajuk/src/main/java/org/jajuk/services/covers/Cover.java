/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.services.covers;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import javax.swing.ImageIcon;

import org.jajuk.services.core.SessionService;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * A cover, encapsulates URL, files and manages cover priority to display.
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
   * TAG_COVER : tag cover located inside the audio files as a tag
   * </p>
   * <p>
   * STANDARD COVER : cover located on the disk with a obvious name (cover.png,
   * front.png...)
   * </p>
   * <p>
   * REMOTE_COVER : cover from the web (HTTP protocol)
   * </p>
   * <p>
   * SELECTED_COVER : local or tag cover selected by user as the default local cover to
   * display in thumbs..
   * </p>
   */
  public enum CoverType {
    NO_COVER, REMOTE_COVER, LOCAL_COVER,
    // cover stored in the tag of a file
    TAG_COVER, STANDARD_COVER, SELECTED_COVER
  }

  /** Cover URL*. */
  private final URL url;
  /** Cover Type. */
  private final CoverType type;
  /** Associated file. */
  private File file;
  /** Default cover image. */
  private static final ImageIcon DEFAULT_COVER_ICON = UtilGUI.getImage(IMAGES_SPLASHSCREEN);

  /**
   * Constructor for remote covers.
   *
   * @param url 
   * @param type 
   */
  public Cover(final URL url, final CoverType type) {
    this.url = url;
    this.type = type;
    // only remote and no_cover are created by URL (file:// for no_cover, the
    // image is inside the jajuk jar)
    if (type == CoverType.REMOTE_COVER || type == CoverType.NO_COVER) {
      this.file = SessionService.getCachePath(url);
    }
  }

  /**
   * Constructor for local covers.
   *
   * @param localFile 
   * @param type 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Cover(final File localFile, final CoverType type) throws IOException {
    this.type = type;
    this.file = localFile;
    this.url = new URL("file://" + file.getAbsolutePath());
  }

  /*
   * (non-Javadoc) The priority order is : SELECTED > STANDARD > TAG > LOCAL > REMOTE > NO_COVER
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Cover cOther) {
    // should be able to handle null
    if (cOther == null) {
      return -1;
    }
    // We leverage the enum ordering for comparison
    // If both covers are standard covers, order according to FILE_DEFAULT_COVER values order
    int comparison = getType().ordinal() - cOther.getType().ordinal();
    if (comparison != 0 || getType() != CoverType.STANDARD_COVER) {
      return comparison;
    } else {
      // Compute for the current file and the other the associated index in the FILE_DEFAULT_COVER
      // pattern list
      String fileName = file.getName();
      String fileNameNoExtension = fileName.substring(0, fileName.lastIndexOf('.'));
      int fileIndex = 0;
      String fileNameOther = cOther.getFile().getName();
      String fileNameOtherNoExtension = fileNameOther.substring(0, fileNameOther.lastIndexOf('.'));
      int fileOtherIndex = 0;
      Scanner s = new Scanner(Conf.getString(Const.FILE_DEFAULT_COVER)).useDelimiter(";");
      for (int i = 0; s.hasNext(); i++) {
        String pattern = s.next();
        if (fileNameNoExtension.matches(".*" + pattern + ".*")) {
          fileIndex = i;
          // We keep the index of the first found matching pattern if the file matches
          // several file patterns (like xxx_font_jajuk.jpg)
          break;
        }
      }
      s = new Scanner(Conf.getString(Const.FILE_DEFAULT_COVER)).useDelimiter(";");
      for (int i = 0; s.hasNext(); i++) {
        String pattern = s.next();
        if (fileNameOtherNoExtension.matches(".*" + pattern + ".*")) {
          fileOtherIndex = i;
          break;
        }
      }
      return fileOtherIndex - fileIndex;
    }
  }

  /**
   * Gets the type.
   * 
   * @return Returns the type.
   */
  public CoverType getType() {
    return type;
  }

  /**
   * Gets the url.
   * 
   * @return Returns the sURL.
   */
  public URL getURL() {
    return url;
  }

  /**
   * Return cover image size in kilobyte.
   * 
   * @return the size
   */
  public String getSize() {
    int iSize = (int) (Math.ceil(((double) file.length()) / 1024));
    return Integer.toString(iSize);
  }

  /**
   * Gets the image.
   *
   * @return Returns the image.
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   * @throws JajukException the jajuk exception
   */
  public Image getImage() throws IOException, InterruptedException, JajukException {
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
      MediaTracker tracker = new MediaTracker(JajukMainWindow.getInstance());
      tracker.addImage(image, 1);
      tracker.waitForAll();
      tracker.removeImage(image);
      // If image cannot be correctly loaded, throw an exception
      if (tracker.getErrorsAny() != null && tracker.getErrorsAny().length > 0) {
        throw new JajukException(9, getFile().getAbsolutePath());
      }
    }
    Log.debug("Loaded {{" + url + "}} in  " + (System.currentTimeMillis() - l) + " ms");
    return image;
  }

  /**
   * toString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return "Type=" + type + " URL={{" + url + "}}";
  }

  /**
   * Equals needed for consistency for sorting.
   * 
   * @param o 
   * 
   * @return true, if equals
   */
  @Override
  public boolean equals(Object o) {
    // this also handles null by definition
    if (!(o instanceof Cover)) {
      return false;
    }
    // we have an item of type Cover, so we can cast it safely
    Cover cOther = (Cover) o;
    return url.toString().equals(cOther.getURL().toString());
  }

  /**
   * Hash code.
   * 
   * @return object hashcode
   */
  @Override
  public int hashCode() {
    try {
      return this.url.toURI().hashCode() + type.ordinal();
    } catch (URISyntaxException e) {
      Log.warn("Found invalid URL: {{" + url.toString() + "}}");
      return 0;
    }
  }

  /**
   * Gets the file.
   * 
   * @return the file
   */
  public File getFile() {
    return this.file;
  }
}
