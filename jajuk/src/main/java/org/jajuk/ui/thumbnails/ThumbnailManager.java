/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.ui.thumbnails;

import java.awt.Container;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.jajuk.base.Album;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Manage thumbnails
 */
public class ThumbnailManager implements ITechnicalStrings {

  /** No instances */
  private ThumbnailManager() {
  }

  /**
   * Delete all thumbs for a given size
   * 
   * @param size
   *          size, eg: ITechnicalStrings.THUMBNAIL_SIZE_150x150
   */
  public static void cleanThumbs(String size) {
    File fThumb = Util.getConfFileByPath(FILE_THUMBS + '/' + size);
    if (fThumb.exists()) {
      File[] files = fThumb.listFiles();
      for (File file : files) {
        if (!file.getAbsolutePath().matches(".*" + FILE_THUMB_NO_COVER)) {
          file.delete();
        }
      }
      // Refresh default cover
      File fDefault = Util.getConfFileByPath(FILE_THUMBS + "/" + size + "/" + FILE_THUMB_NO_COVER);
      fDefault.delete();
      try {
        int iSize = Integer.parseInt(new StringTokenizer(size, "x").nextToken());
        createThumbnail(IconLoader.ICON_NO_COVER, fDefault, iSize);
      } catch (Exception e) {
        Log.error(e);
      }
    }
  }

  /**
   * Delete all thumbs for a given album
   * 
   * @param album
   */
  public static void cleanThumbs(Album album) {
    for (int size = 0; size < 6; size++) {
      File fThumb = Util.getConfFileByPath(FILE_THUMBS + '/' + 50 * size + '/' + album.getID()
          + ".jpg");
      if (fThumb.exists()) {
        boolean out = fThumb.delete();
        if (!out) {
          Log.warn("Cannot delete thumb for album: " + album);
        }
      }
    }
  }

  /**
   * Reads an image in a file and creates a thumbnail in another file. Will be
   * created if necessary. the thumbnail must be maxDim pixels or less. Thanks
   * Marco Schmidt
   * http://schmidt.devlib.org/java/save-jpeg-thumbnail.html#source
   * 
   * @param orig
   *          source image
   * @param thumb
   *          destination file (jpg)
   * @param maxDim
   *          required size
   * @throws Exception
   */
  public static void createThumbnail(final File orig, final File thumb, final int maxDim)
      throws Exception {
    // do not use URL object has it can corrupt special paths
    createThumbnail(new ImageIcon(orig.getAbsolutePath()), thumb, maxDim);
  }

  /**
   * Reads an image in a file and creates a thumbnail in another file. Use this
   * method to get thumbs from images inside jar files, some bugs in URL
   * encoding makes impossible to create the image from a file. Will be created
   * if necessary. the thumbnail must be maxDim pixels or less. Thanks Marco
   * Schmidt http://schmidt.devlib.org/java/save-jpeg-thumbnail.html#source
   * 
   * @param orig
   *          source image
   * @param thumb
   *          destination file (jpg)
   * @param maxDim
   *          required size
   * @throws Exception
   */
  public static void createThumbnail(final ImageIcon ii, final File thumb, final int maxDim)
      throws Exception {
    final Image image = ii.getImage();
    // Wait for full image loading
    final MediaTracker mediaTracker = new MediaTracker(new Container());
    mediaTracker.addImage(image, 0);
    mediaTracker.waitForID(0);
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
    final BufferedImage thumbImage = Util.toBufferedImage(image, !(Util.getExtension(thumb)
        .equalsIgnoreCase("jpg")), thumbWidth, thumbHeight);
    // Need alpha only for png and gif files
    // save thumbnail image to OUTFILE
    ImageIO.write(thumbImage, Util.getExtension(thumb), thumb);
    // Free thumb memory
    thumbImage.flush();
  }

  /**
   * Make thumbnail file exists (album id.jpg or.gif or .png) in thumbs
   * directory if it doesn't exist yet
   * 
   * @param album
   * @return whether a new cover has been created
   */
  public static boolean refreshThumbnail(final Album album, final String size) {
    final File fThumb = Util.getConfFileByPath(ITechnicalStrings.FILE_THUMBS + '/' + size + '/'
        + album.getID() + '.' + ITechnicalStrings.EXT_THUMB);
    File fCover = null;
    if (!fThumb.exists()) {
      // search for local covers in all directories mapping the
      // current track to reach other
      // devices covers and display them together
      final Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album);
      if (tracks.size() == 0) {
        return false;
      }
      // take first track found to get associated directories as we
      // assume all tracks for an album are in the same directory
      final Track trackCurrent = tracks.iterator().next();
      fCover = trackCurrent.getAlbum().getCoverFile();
      if (fCover != null) {
        try {
          final int iSize = Integer.parseInt(new StringTokenizer(size, "x").nextToken());
          createThumbnail(fCover, fThumb, iSize);
          return true;
        } catch (final Exception e) {
          Log.error(e);
        }
      }
    }
    return false; // thumb already exist
  }

}
