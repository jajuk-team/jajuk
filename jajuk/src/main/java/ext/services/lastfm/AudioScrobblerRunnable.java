/**
 * aTunes 1.6.6
 * Copyright (C) 2006-2007 Alex Aranda (fleax) alex@atunes.org
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package ext.services.lastfm;

import org.jajuk.base.Track;
import org.jajuk.util.log.Log;

public class AudioScrobblerRunnable implements Runnable {

	private boolean interrupted;

	private AudioScrobblerAlbumsRunnable albumsRunnable;

	private AudioScrobblerCoversRunnable coversRunnable;

	private AudioScrobblerSimilarArtistsRunnable artistsRunnable;

	private AudioScrobblerListener listener;

	private AudioScrobblerService service;

	private Track track;

	private boolean retrieveArtistInfo = true;

	public AudioScrobblerRunnable(AudioScrobblerListener listener, AudioScrobblerService service,
			Track track) {
		this.listener = listener;
		this.service = service;
		this.track = track;
	}

	public void run() {
		albumsRunnable = new AudioScrobblerAlbumsRunnable(listener, service, track);
		albumsRunnable.setRetrieveArtistInfo(retrieveArtistInfo);
		Thread albumsInfoThread = new Thread(albumsRunnable);
		albumsInfoThread.start();
		try {
			albumsInfoThread.join();
		} catch (InterruptedException e) {
			Log.error(e);
		}

		if (retrieveArtistInfo) {
			coversRunnable = new AudioScrobblerCoversRunnable(listener, service, listener
					.getAlbums());
			Thread coversThread = new Thread(coversRunnable);
			coversThread.start();

			artistsRunnable = new AudioScrobblerSimilarArtistsRunnable(listener, service, track
					.getAuthor().getName2());
			Thread artistsThread = new Thread(artistsRunnable);
			artistsThread.start();

			try {
				if (!interrupted)
					coversThread.join();
				if (!interrupted)
					artistsThread.join();
			} catch (InterruptedException e) {
				Log.error(e);
			}
		}
	}

	public void interrupt() {
		interrupted = true;
		if (albumsRunnable != null)
			albumsRunnable.interrupt();
		if (coversRunnable != null)
			coversRunnable.interrupt();
		if (artistsRunnable != null)
			artistsRunnable.interrupt();
	}

	public void setRetrieveArtistInfo(boolean retrieveArtistInfo) {
		this.retrieveArtistInfo = retrieveArtistInfo;
	}
}
