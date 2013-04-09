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
package ext.services.lastfm;

import java.awt.Image;
import java.util.List;

import javax.swing.SwingUtilities;

import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;

/**
 * .
 */
public class TestLastFmAlbumsRunnable extends JajukTestCase {
  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmAlbumsRunnable#LastFmAlbumsRunnable(ext.services.lastfm.ContextListener, ext.services.lastfm.LastFmService, ext.services.lastfm.AudioObject, long)}
   * .
   */
  public void testLastFmAlbumsRunnable() {
    new LastFmAlbumsRunnable(null, LastFmService.getInstance(), null, 1);
  }

  /**
   * Test method for.
   *
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
   * @throws Exception the exception
   */
  public void testRun() throws Exception {
    // first run it normally
    new LastFmAlbumsRunnable(new MyContextListener(), LastFmService.getInstance(),
        new MyAudioObject(), 1).run();
    // then run it in the background
    LastFmAlbumsRunnable runnable = new LastFmAlbumsRunnable(new MyContextListener(),
        LastFmService.getInstance(), new MyAudioObject(), 1);
    SwingUtilities.invokeLater(runnable);
    // and tell it to interrupt at some point
    runnable.interrupt();
    // now wait for it to finish
    TestHelpers.clearSwingUtilitiesQueue();
  }

  /**
   * Test method for.
   *
   * {@link ext.services.lastfm.LastFmAlbumsRunnable#setRetrieveArtistInfo(boolean)}
   * .
   */
  public void testSetRetrieveArtistInfo() {
    LastFmAlbumsRunnable runnable = new LastFmAlbumsRunnable(null, LastFmService.getInstance(),
        null, 1);
    runnable.setRetrieveArtistInfo(true);
  }

  /**
   * .
   */
  private final class MyContextListener implements ContextListener {
    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#setLastArtistRetrieved(java.lang.String, long)
     */
    @Override
    public void setLastArtistRetrieved(String artist, long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#setLastAlbumRetrieved(java.lang.String, long)
     */
    @Override
    public void setLastAlbumRetrieved(String album, long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#setImage(java.awt.Image, ext.services.lastfm.AudioObject, long)
     */
    @Override
    public void setImage(Image img, AudioObject ao, long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#setAlbums(java.util.List, long)
     */
    @Override
    public void setAlbums(List<? extends AlbumInfo> album, long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#setAlbum(ext.services.lastfm.AlbumInfo, long)
     */
    @Override
    public void setAlbum(AlbumInfo album, long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#notifyWikiInfoRetrieved(java.lang.String, java.lang.String, long)
     */
    @Override
    public void notifyWikiInfoRetrieved(String wikiText, String wikiURL, long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#notifyStartRetrievingCovers(long)
     */
    @Override
    public void notifyStartRetrievingCovers(long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#notifyStartRetrievingArtistImages(long)
     */
    @Override
    public void notifyStartRetrievingArtistImages(long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#notifyFinishGetSimilarArtist(ext.services.lastfm.ArtistInfo, java.awt.Image, long)
     */
    @Override
    public void notifyFinishGetSimilarArtist(ArtistInfo a, Image img, long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#notifyCoverRetrieved(ext.services.lastfm.AlbumInfo, java.awt.Image, long)
     */
    @Override
    public void notifyCoverRetrieved(AlbumInfo album, Image cover, long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#notifyArtistImage(java.awt.Image, long)
     */
    @Override
    public void notifyArtistImage(Image img, long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#notifyAlbumRetrieved(ext.services.lastfm.AudioObject, long)
     */
    @Override
    public void notifyAlbumRetrieved(AudioObject file, long id) {
    }

    /* (non-Javadoc)
     * @see ext.services.lastfm.ContextListener#getAlbums()
     */
    @Override
    public List<AlbumInfo> getAlbums() {
      return null;
    }
  }

  /**
   * .
   */
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
