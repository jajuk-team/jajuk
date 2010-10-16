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
 *  $Revision$
 */

package ext.services.lastfm;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jajuk.util.log.Log;

/**
 * The Class LastFmRunnable.
 */
public class LastFmRunnable implements Runnable {

  /** The interrupted. */
  private volatile boolean interrupted;

  /** The albums runnable. */
  private LastFmAlbumsRunnable albumsRunnable;

  /** The covers runnable. */
  private LastFmCoversRunnable coversRunnable;

  /** The artists runnable. */
  private LastFmSimilarArtistsRunnable artistsRunnable;

  /** The listener. */
  private ContextListener listener;

  /** The service. */
  private LastFmService service;

  /** The audio object. */
  private AudioObject audioObject;

  /** The retrieve artist info. */
  private boolean retrieveArtistInfo = true;

  /** The id. */
  private long id;

  /** The executor service. */
  private ExecutorService executorService;

  /**
   * Instantiates a new audio scrobbler runnable.
   * 
   * @param listener the listener
   * @param service the service
   * @param audioObject the audio object
   * @param id the id
   * @param executorService DOCUMENT_ME
   */
  public LastFmRunnable(ContextListener listener, LastFmService service, AudioObject audioObject,
      long id, ExecutorService executorService) {
    this.listener = listener;
    this.service = service;
    this.audioObject = audioObject;
    this.id = id;
    this.executorService = executorService;
  }

  /**
   * Interrupt.
   */
  public void interrupt() {
    interrupted = true;
    if (albumsRunnable != null) {
      albumsRunnable.interrupt();
    }
    if (coversRunnable != null) {
      coversRunnable.interrupt();
    }
    if (artistsRunnable != null) {
      artistsRunnable.interrupt();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    albumsRunnable = new LastFmAlbumsRunnable(listener, service, audioObject, id);
    albumsRunnable.setRetrieveArtistInfo(retrieveArtistInfo);
    Future<?> albumsRunnableFuture = executorService.submit(albumsRunnable);
    Log.debug("LastFmAlbumsRunnable started with id " + id + " for  " + audioObject.getArtist());
    try {
      albumsRunnableFuture.get();
    } catch (ExecutionException e) {
      Log.error(e);
    } catch (InterruptedException e) {
      Log.debug("albums runnable interrupted");
    }

    if (retrieveArtistInfo && !interrupted) {
      coversRunnable = new LastFmCoversRunnable(listener, service, listener.getAlbums(), id,
          audioObject);
      executorService.submit(coversRunnable);
      Log.debug("LastFmCoversRunnable started with id " + id);

      artistsRunnable = new LastFmSimilarArtistsRunnable(listener, service,
          audioObject.getArtist(), id);
      executorService.submit(artistsRunnable);
      Log.debug("LastFmSimilarArtistsRunnable started with id " + id + " for "
          + audioObject.getArtist());
    }
  }

  /**
   * Sets the retrieve artist info.
   * 
   * @param retrieveArtistInfo the new retrieve artist info
   */
  public void setRetrieveArtistInfo(boolean retrieveArtistInfo) {
    this.retrieveArtistInfo = retrieveArtistInfo;
  }
}
