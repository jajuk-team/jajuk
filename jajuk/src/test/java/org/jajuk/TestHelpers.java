/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Artist;
import org.jajuk.base.ArtistManager;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.base.Year;
import org.jajuk.base.YearManager;
import org.jajuk.services.players.MPlayerPlayerImpl;
import org.jajuk.services.tags.JAudioTaggerTagImpl;

/**
 * 
 */
public class TestHelpers extends JajukTestCase {

  Exception exc = null;
  boolean finished = false;

  public void testClearSwingUtilitiesQueue() throws Exception {
    // verify test-helper to clear the Swing Utilities Queue

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(1000);

          finished = true;
        } catch (InterruptedException e) {
          exc = e;
        }
      }
    });

    long now = System.currentTimeMillis();

    // after this method returns we expect the above invokeLater to be finished
    JUnitHelpers.clearSwingUtilitiesQueue();

    // now the finished needs to be true
    assertTrue("Elapsed time(ms): " + (System.currentTimeMillis() - now), finished);
  }

  private final int COUNT = 10;
  private AtomicInteger count = new AtomicInteger(0);

  public void testClearSwingUtilitiesQueueMultiple() throws Exception {
    // verify test-helper to clear the Swing Utilities Queue

    for (int i = 0; i < COUNT; i++) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(1000 / COUNT);

            count.incrementAndGet();
          } catch (InterruptedException e) {
            exc = e;
          }
        }
      });
    }

    long now = System.currentTimeMillis();

    // after this method returns we expect the above invokeLater to be finished
    JUnitHelpers.clearSwingUtilitiesQueue();

    // now the finished needs to be true
    assertTrue(
        "Elapsed time(ms): " + (System.currentTimeMillis() - now) + " Count: " + count.get(), count
            .get() == COUNT);
  }

  /**
   * Return a mock file for testing purposes
   * 
   * @return a mock file for testing purposes
   */
  public static File getMockFile() {
    Type type = TypeManager.getInstance().registerType("mp3", "mp3", MPlayerPlayerImpl.class,
        JAudioTaggerTagImpl.class);
    Album album = AlbumManager.getInstance().registerAlbum("album name", "album artist", 2222l);
    Genre genre = GenreManager.getInstance().registerGenre("genre name");
    Artist artist = ArtistManager.getInstance().registerArtist("artist name");
    Year year = YearManager.getInstance().registerYear("2000");
    Track track = TrackManager.getInstance().registerTrack("track name", album, genre, artist, 12l,
        year, 1l, type, 1l);
    Device device = DeviceManager.getInstance().registerDevice("device name", Device.Type.FILES_CD,
        "/tmp");
    Directory dir = DirectoryManager.getInstance().registerDirectory(device);
    File file = FileManager.getInstance().registerFile("file name", dir, track, 12, 128);
    return file;
  }
}
