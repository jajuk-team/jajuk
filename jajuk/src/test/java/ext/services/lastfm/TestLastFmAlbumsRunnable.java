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
 *  $Revision: 3132 $
 */
package ext.services.lastfm;

import java.awt.Image;
import java.io.File;
import java.util.List;

import javax.swing.SwingUtilities;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Track;
import org.jajuk.util.Const;

/**
 * 
 */
public class TestLastFmAlbumsRunnable extends JajukTestCase {

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbumsRunnable#LastFmAlbumsRunnable(ext.services.lastfm.ContextListener, ext.services.lastfm.LastFmService, ext.services.lastfm.AudioObject, long)}
   * .
   */
  public void testLastFmAlbumsRunnable() {
    new LastFmAlbumsRunnable(null, LastFmService.getInstance(), null, 1);
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbumsRunnable#interrupt()}.
   */
  public void testInterrupt() {
    LastFmAlbumsRunnable runnable = new LastFmAlbumsRunnable(null, LastFmService.getInstance(),
        null, 1);
    runnable.interrupt();
  }

  /**
   * Test method for {@link ext.services.lastfm.LastFmAlbumsRunnable#run()}.
   * 
   * @throws Exception
   */
  public void testRun() throws Exception {
    // first run it normally
    new LastFmAlbumsRunnable(new MyContextListener(), LastFmService.getInstance(),
        new MyAudioObject(), 1).run();

    // then run it in the background
    LastFmAlbumsRunnable runnable = new LastFmAlbumsRunnable(new MyContextListener(), LastFmService
        .getInstance(), new MyAudioObject(), 1);
    SwingUtilities.invokeLater(runnable);

    // and tell it to interrupt at some point
    runnable.interrupt();

    // now wait for it to finish
    JUnitHelpers.clearSwingUtilitiesQueue();
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbumsRunnable#setRetrieveArtistInfo(boolean)}
   * .
   */
  public void testSetRetrieveArtistInfo() {
    LastFmAlbumsRunnable runnable = new LastFmAlbumsRunnable(null, LastFmService.getInstance(),
        null, 1);
    runnable.setRetrieveArtistInfo(true);
  }

  /**
   * Test method for
   * {@link ext.services.lastfm.LastFmAlbumsRunnable#getImageForAudioFile(org.jajuk.base.Track, int, int)}
   * .
   * 
   * @throws Exception
   */
  public void testGetImageForAudioFileNone() throws Exception {
    LastFmAlbumsRunnable.getImageForAudioFile(JUnitHelpers.getTrack(2), 100, 100);
  }

  public void testGetImageForAudioFileNotExists() {
    Track track = JUnitHelpers.getTrack(3);
    track.getAlbum().setProperty(Const.XML_ALBUM_COVER,
        System.getProperty("java.io.tmpdir") + "nonexist"); // don't read covers
    // for
    LastFmAlbumsRunnable.getImageForAudioFile(track, 100, 100);
  }

  public void testGetImageForAudioFileExists() throws Exception {
    Track track = JUnitHelpers.getTrack(3);
    File file = File.createTempFile("test", ".img");
    track.getAlbum().setProperty(Const.XML_ALBUM_COVER, file.getAbsolutePath()); // don't
    // read
    // covers
    // for

    assertNotNull(LastFmAlbumsRunnable.getImageForAudioFile(track, 200, 100));

    // TODO: cleanup does not work on Windows because the file seems to still be
    // used somewhere
    file.delete();
  }

  public void testGetImageForAudioFileExistsMaxSize() throws Exception {
    Track track = JUnitHelpers.getTrack(3);
    File file = File.createTempFile("test", ".img");
    track.getAlbum().setProperty(Const.XML_ALBUM_COVER, file.getAbsolutePath()); // don't
    // read
    // covers
    // for

    assertNotNull(LastFmAlbumsRunnable.getImageForAudioFile(track, 100, 200));

    // TODO: cleanup does not work on Windows because the file seems to still be
    // used somewhere
    file.delete();
  }

  public void testGetImageForAudioFileExistsNoResize() throws Exception {
    Track track = JUnitHelpers.getTrack(3);
    File file = File.createTempFile("test", ".img");
    track.getAlbum().setProperty(Const.XML_ALBUM_COVER, file.getAbsolutePath()); // don't
    // read
    // covers
    // for

    assertNotNull(LastFmAlbumsRunnable.getImageForAudioFile(track, -1, -1));

    // TODO: cleanup does not work on Windows because the file seems to still be
    // used somewhere
    file.delete();
  }

  /**
   * 
   */
  private final class MyContextListener implements ContextListener {
    @Override
    public void setLastArtistRetrieved(String artist, long id) {

    }

    @Override
    public void setLastAlbumRetrieved(String album, long id) {

    }

    @Override
    public void setImage(Image img, AudioObject ao, long id) {

    }

    @Override
    public void setAlbums(List<? extends AlbumInfo> album, long id) {

    }

    @Override
    public void setAlbum(AlbumInfo album, long id) {

    }

    @Override
    public void notifyWikiInfoRetrieved(String wikiText, String wikiURL, long id) {

    }

    @Override
    public void notifyStartRetrievingCovers(long id) {

    }

    @Override
    public void notifyStartRetrievingArtistImages(long id) {

    }

    @Override
    public void notifyFinishGetSimilarArtist(ArtistInfo a, Image img, long id) {

    }

    @Override
    public void notifyCoverRetrieved(AlbumInfo album, Image cover, long id) {

    }

    @Override
    public void notifyArtistImage(Image img, long id) {

    }

    @Override
    public void notifyAlbumRetrieved(AudioObject file, long id) {

    }

    @Override
    public List<AlbumInfo> getAlbums() {

      return null;
    }
  }

  private class MyAudioObject implements AudioObject {

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getAlbum()
     */
    @Override
    public String getAlbum() {

      return "By The Way";
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getAlbumArtist()
     */
    @Override
    public String getAlbumArtist() {

      return "Red Hot Chilli Peppers";
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getArtist()
     */
    @Override
    public String getArtist() {

      return "Red Hot Chilli Peppers";
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getBitrate()
     */
    @Override
    public long getBitrate() {

      return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getComposer()
     */
    @Override
    public String getComposer() {

      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getDiscNumber()
     */
    @Override
    public Integer getDiscNumber() {

      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getDuration()
     */
    @Override
    public long getDuration() {

      return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getFrequency()
     */
    @Override
    public int getFrequency() {

      return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getGenre()
     */
    @Override
    public String getGenre() {

      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getLyrics()
     */
    @Override
    public String getLyrics() {

      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getStars()
     */
    @Override
    public int getStars() {

      return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getTitle()
     */
    @Override
    public String getTitle() {

      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getTitleOrFileName()
     */
    @Override
    public String getTitleOrFileName() {

      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getTrackNumber()
     */
    @Override
    public Integer getTrackNumber() {

      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getUrl()
     */
    @Override
    public String getUrl() {

      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#getYear()
     */
    @Override
    public String getYear() {

      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#isSeekable()
     */
    @Override
    public boolean isSeekable() {

      return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ext.services.lastfm.AudioObject#setStars(int)
     */
    @Override
    public void setStars(int stars) {

    }

  }
}
