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
package org.jajuk.services.lastfm;

import java.awt.Image;
import java.util.List;

import javax.swing.SwingUtilities;

import junit.framework.TestCase;

import org.jajuk.TestHelpers;
import org.jajuk.services.lastfm.model.*;

/**
 * .
 */
public class TestLastFmAlbumsRunnable extends TestCase {
  /**
   * Test method for.
   *
   * {@link LastFmAlbumsRunnable#LastFmAlbumsRunnable(ContextListener, LastFmService, AudioObject, long)}
   * .
   */
  public void testLastFmAlbumsRunnable() {
    new LastFmAlbumsRunnable(null, LastFmService.getInstance(), null, 1);
  }

  /**
   * Test method for.
   *
   * {@link LastFmAlbumsRunnable#interrupt()}.
   */
  public void testInterrupt() {
    LastFmAlbumsRunnable runnable = new LastFmAlbumsRunnable(null, LastFmService.getInstance(),
        null, 1);
    runnable.interrupt();
  }

  /**
   * Test method for {@link LastFmAlbumsRunnable#run()}.
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
   * {@link LastFmAlbumsRunnable#setRetrieveArtistInfo(boolean)}
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
     * @see org.jajuk.services.lastfm.model.ContextListener#setLastArtistRetrieved(java.lang.String, long)
     */
    @Override
    public void setLastArtistRetrieved(String artist, long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#setLastAlbumRetrieved(java.lang.String, long)
     */
    @Override
    public void setLastAlbumRetrieved(String album, long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#setImage(java.awt.Image, org.jajuk.services.lastfm.model.AudioObject, long)
     */
    @Override
    public void setImage(Image img, AudioObject ao, long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#setAlbums(java.util.List, long)
     */
    @Override
    public void setAlbums(List<? extends AlbumInfo> album, long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#setAlbum(org.jajuk.services.lastfm.model.AlbumInfo, long)
     */
    @Override
    public void setAlbum(AlbumInfo album, long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#notifyWikiInfoRetrieved(java.lang.String, java.lang.String, long)
     */
    @Override
    public void notifyWikiInfoRetrieved(String wikiText, String wikiURL, long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#notifyStartRetrievingCovers(long)
     */
    @Override
    public void notifyStartRetrievingCovers(long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#notifyStartRetrievingArtistImages(long)
     */
    @Override
    public void notifyStartRetrievingArtistImages(long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#notifyFinishGetSimilarArtist(org.jajuk.services.lastfm.model.ArtistInfo, java.awt.Image, long)
     */
    @Override
    public void notifyFinishGetSimilarArtist(ArtistInfo a, Image img, long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#notifyCoverRetrieved(org.jajuk.services.lastfm.model.AlbumInfo, java.awt.Image, long)
     */
    @Override
    public void notifyCoverRetrieved(AlbumInfo album, Image cover, long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#notifyArtistImage(java.awt.Image, long)
     */
    @Override
    public void notifyArtistImage(Image img, long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#notifyAlbumRetrieved(org.jajuk.services.lastfm.model.AudioObject, long)
     */
    @Override
    public void notifyAlbumRetrieved(AudioObject file, long id) {
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.lastfm.model.ContextListener#getAlbums()
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
     * @see org.jajuk.services.lastfm.model.AudioObject#getAlbum()
     */
    @Override
    public String getAlbum() {
      return "By The Way";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getAlbumArtist()
     */
    @Override
    public String getAlbumArtist() {
      return "Red Hot Chili Peppers";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getArtist()
     */
    @Override
    public String getArtist() {
      return "Red Hot Chilli Peppers";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getBitrate()
     */
    @Override
    public long getBitrate() {
      return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getComposer()
     */
    @Override
    public String getComposer() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getDiscNumber()
     */
    @Override
    public Integer getDiscNumber() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getDuration()
     */
    @Override
    public long getDuration() {
      return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getFrequency()
     */
    @Override
    public int getFrequency() {
      return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getGenre()
     */
    @Override
    public String getGenre() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getLyrics()
     */
    @Override
    public String getLyrics() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getStars()
     */
    @Override
    public int getStars() {
      return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getTitle()
     */
    @Override
    public String getTitle() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getTitleOrFileName()
     */
    @Override
    public String getTitleOrFileName() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getTrackNumber()
     */
    @Override
    public Integer getTrackNumber() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getUrl()
     */
    @Override
    public String getUrl() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#getYear()
     */
    @Override
    public String getYear() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#isSeekable()
     */
    @Override
    public boolean isSeekable() {
      return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.lastfm.model.AudioObject#setStars(int)
     */
    @Override
    public void setStars(int stars) {
    }
  }
}
