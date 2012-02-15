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
 *  
 */

package org.jajuk.ui.thumbnails;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukRuntimeException;
import org.jajuk.util.log.Log;

/**
 * Manage thumbnails.
 */
public final class ThumbnailManager {

  /**
   * No instances.
   */
  private ThumbnailManager() {
  }

  /**
   * Delete all thumbs for a given size.
   * 
   * @param size size, eg: Const.THUMBNAIL_SIZE_150x150
   */
  public static void cleanThumbs(String size) {
    File fThumb = SessionService.getConfFileByPath(Const.FILE_THUMBS + '/' + size);
    if (fThumb.exists()) {
      File[] files = fThumb.listFiles();
      for (File file : files) {
        if (!file.getAbsolutePath().matches(".*" + Const.FILE_THUMB_NO_COVER)) {
          try {
            UtilSystem.deleteFile(file);
          } catch (IOException e) {
            Log.error(e);
          }
        }
      }
      // Refresh default cover
      File fDefault = SessionService.getConfFileByPath(Const.FILE_THUMBS + "/" + size + "/"
          + Const.FILE_THUMB_NO_COVER);
      if (fDefault.exists() && !fDefault.delete()) {
        Log.warn("Could not delete " + fDefault.toString());
      }
      try {
        int iSize = Integer.parseInt(new StringTokenizer(size, "x").nextToken());
        createThumbnail(IconLoader.getIcon(JajukIcons.NO_COVER), fDefault, iSize);
      } catch (Exception e) {
        Log.error(e);
      }
    }
    // Reset all thumbs cache
    for (Album album : AlbumManager.getInstance().getAlbums()) {
      cleanThumbs(album);
    }
  }

  /**
   * Delete all thumbs for a given album.
   * 
   * @param album DOCUMENT_ME
   */
  public static void cleanThumbs(Album album) {
    // Now delete thumb files
    for (int size = 50; size <= 300; size += 50) {
      File fThumb = ThumbnailManager.getThumbBySize(album, size);
      if (fThumb.exists()) {
        boolean out = fThumb.delete();
        if (!out) {
          Log.warn("Cannot delete thumb for album: " + album);
        }
      }
      album.setAvailableThumb(size, false);
    }
  }

  /**
   * Reads an image in a file and creates a thumbnail in another file. Will be
   * created if necessary. the thumbnail must be maxDim pixels or less. Thanks
   * Marco Schmidt
   * http://schmidt.devlib.org/java/save-jpeg-thumbnail.html#source
   *
   * @param orig source image
   * @param thumb destination file
   * @param maxDim required size
   * @throws InterruptedException the interrupted exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void createThumbnail(final File orig, final File thumb, final int maxDim)
      throws InterruptedException, IOException {
    // do not use URL object has it can corrupt special paths
    ImageIcon ii = new ImageIcon(orig.getAbsolutePath());
    if (ii.getImageLoadStatus() != MediaTracker.COMPLETE) {
      throw new JajukRuntimeException("Cannot load image: " + orig.getAbsolutePath() + ", load status is: " + ii.getImageLoadStatus());
    }
    createThumbnail(ii, thumb, maxDim);
  }

  /**
   * Reads an image in a file and creates a thumbnail in another file. Use this
   * method to get thumbs from images inside jar files, some bugs in URL
   * encoding makes impossible to create the image from a file. Will be created
   * if necessary. the thumbnail must be maxDim pixels or less. Thanks Marco
   * Schmidt http://schmidt.devlib.org/java/save-jpeg-thumbnail.html#source
   *
   * @param ii DOCUMENT_ME
   * @param thumb destination file (jpg)
   * @param maxDim required size
   * @throws InterruptedException the interrupted exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void createThumbnail(final ImageIcon ii, final File thumb, final int maxDim)
      throws InterruptedException, IOException {
    // Synchronize the file to avoid any concurrency between several threads refreshing the thumb
    // like the catalog view and the artist view.

    // Don't lock the thumb file itself because we have to write in in this method and
    // Windows doesn't support share mode for locks but only exclusive
    File thumbLock = new File(thumb.getAbsolutePath() + ".lock");
    thumbLock.createNewFile();

    synchronized (thumbLock.getAbsolutePath().intern()) {
      // Note that at this point, the image is fully loaded (done in the ImageIcon constructor)
      final Image image = ii.getImage();
      // determine thumbnail size from WIDTH and HEIGHT
      int thumbWidth = maxDim;
      int thumbHeight = maxDim;
      final double thumbRatio = (double) thumbWidth / (double) thumbHeight;
      final int imageWidth = image.getWidth(null);
      final int imageHeight = image.getHeight(null);
      final double imageRatio = (double) imageWidth / (double) imageHeight;
      if (thumbRatio < imageRatio) {
        thumbHeight = (int) (thumbWidth / imageRatio);
      } else {
        thumbWidth = (int) (thumbHeight * imageRatio);
      }
      // draw original image to thumbnail image object and
      // scale it to the new size on-the-fly
      final BufferedImage thumbImage = UtilGUI.toBufferedImage(image, thumbWidth, thumbHeight);
      // save thumbnail image to OUTFILE
      ImageIO.write(thumbImage, UtilSystem.getExtension(thumb), thumb);
      // Free thumb memory
      thumbImage.flush();
    }

  }

  /**
   * Check all thumbs existence for performance reasons.
   * 
   * @param size size of thumbs to be checked
   */
  public static void populateCache(final int size) {
    for (Album album : AlbumManager.getInstance().getAlbums()) {
      File fThumb = ThumbnailManager.getThumbBySize(album, size);
      album.setAvailableThumb(size, fThumb.exists() && fThumb.length() > 0);
    }
  }

  /**
   * Make thumbnail file exists (album id.jpg or.gif or .png) in thumbs
   * directory if it doesn't exist yet
   * 
   * @param album DOCUMENT_ME
   * @param size DOCUMENT_ME
   * 
   * @return whether a new cover has been created
   */
  public static boolean refreshThumbnail(final Album album, final int size) {
    // Check if the thumb is known in cache
    if (album.isThumbAvailable(size)) {
      return false;
    }
    final File fThumb = getThumbBySize(album, size);
    final File fCover = album.findCover();
    if (fCover != null) {
      try {
        createThumbnail(fCover, fThumb, size);
        // Update thumb availability
        album.setAvailableThumb(size, true);
        // Notify the thumb creation
        Properties details = new Properties();
        details.put(Const.DETAIL_CONTENT, album);
        ObservationManager.notify(new JajukEvent(JajukEvents.THUMB_CREATED, details));
        return true;
      } catch (final Exception e) {
        Log.error(e);
      }
    }
    return false; // thumb already exists or source file cannot be read (an exception occurred)
  }

  /**
   * Return thumb file by album and size.
   * 
   * @param album the album
   * @param size the size (like 50)
   * 
   * @return thumb file by album and size
   */
  public static File getThumbBySize(Album album, int size) {
    StringBuilder thumb = new StringBuilder(Const.FILE_THUMBS).append('/').append(size).append('x')
        .append(size).append('/').append(album.getID()).append('.').append(Const.EXT_THUMB);
    return SessionService.getConfFileByPath(thumb.toString());
  }

}
