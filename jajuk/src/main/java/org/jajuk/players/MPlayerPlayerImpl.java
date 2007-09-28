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
package org.jajuk.players;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;

import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Jajuk player implementation based on Mplayer
 */
public class MPlayerPlayerImpl extends AbstractMPlayerImpl {

	/** Time elapsed in ms */
	private long lTime = 0;

	/** Length to be played in secs */
	private long length;

	/** Starting position */
	private float fPosition;

	/** Current track estimated duration in ms */
	private long lDuration;

	/** Volume when starting fade */
	private float fadingVolume;

	/** Cross fade duration in ms */
	int iFadeDuration = 0;

	/** Progress step in ms */
	private static final int PROGRESS_STEP = 300;// need a fast refresh,

	// especially for fading

	/** current file */
	private org.jajuk.base.File fCurrent;

	/** pause flag * */
	private volatile boolean bPaused = false;

	/** Current position thread */
	private volatile PositionThread position;

	/** Current reader thread */
	private volatile ReaderThread reader;

	/** Inc rating flag */
	private boolean bHasBeenRated = false;

	/**
	 * Position and elapsed time getter
	 */
	private class PositionThread extends Thread {
		public void run() {
			while (!bStop) { // stop this thread when exiting
				try {
					Thread.sleep(PROGRESS_STEP);
					if (!bPaused && !bStop) {
						// a get_percent_pos resumes (mplayer issue)
						sendCommand("get_time_pos");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Reader : read information from mplayer like position
	 */
	private class ReaderThread extends Thread {
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line = null;
				for (; (line = in.readLine()) != null;) {
					if (line.matches(".*ANS_TIME_POSITION.*")) {
						StringTokenizer st = new StringTokenizer(line, "=");
						st.nextToken();
						lTime = (int) (Float.parseFloat(st.nextToken()) * 1000);
						// Store current position for use at next startup
						ConfigurationManager.setProperty(CONF_STARTUP_LAST_POSITION, Float
								.toString(getCurrentPosition()));
						// check if the track get rate increasing level
						// (INC_RATE_TIME secs or intro length)
						if (!bHasBeenRated
								&& (lTime >= (INC_RATE_TIME * 1000) || (length != TO_THE_END && lTime > length))) {
							// inc rate by 1 if file is played at least
							// INC_RATE_TIME secs
							fCurrent.getTrack().setRate(fCurrent.getTrack().getRate() + 1);
							FileManager.getInstance().setRateHasChanged(true);
							// alert bestof playlist something changed
							bHasBeenRated = true;
						}

						// Cross-Fade test
						if (!bFading && iFadeDuration > 0 && lDuration > 0
						// can be null before getting length
								&& lTime > (lDuration - iFadeDuration)) {
							bFading = true;
							fadingVolume = fVolume;
							// force a finished (that doesn't stop but only
							// make a FIFO request to switch track)
							FIFO.getInstance().finished();
						}
						// If fading, decrease sound progressively
						if (bFading) {
							// computes the volume we have to sub to reach zero
							// at last
							// progress()
							float fVolumeStep = fadingVolume
							// we double the refresh period to make sure to
									// reach 0 at the end of iterations because
									// we don't
									// as many mplayer response as queries,
									// tested on 10 & 20 sec of fading
									* ((float) (PROGRESS_STEP * 2) / iFadeDuration);
							float fNewVolume = fVolume - fVolumeStep;
							// decrease volume by n% of initial volume
							if (fNewVolume < 0) {
								fNewVolume = 0;
							}
							try {
								setVolume(fNewVolume);
							} catch (Exception e) {
								Log.error(e);
							}
						}
						// test end of length for intro mode
						if (length != TO_THE_END && lDuration > 0
						// can be null before getting length
								&& (lTime - (fPosition * lDuration)) > length) {
							// length=-1 means there is no max length
							bFading = false;
							FIFO.getInstance().finished();
						}
					} else if (line.matches("ANS_LENGTH.*")) {
						StringTokenizer st = new StringTokenizer(line, "=");
						st.nextToken();
						lDuration = (long) (Float.parseFloat(st.nextToken()) * 1000);
					}
					// EOF
					else if (line.matches("Exiting.*End.*")) {
						bEOF = true;
						bOpening = false;
						// Launch next track
						try {
							// End of file: inc rate by 1 if file is fully
							// played
							fCurrent.getTrack().setRate(fCurrent.getTrack().getRate() + 1);
							// alert best-of playlist something changed
							FileManager.getInstance().setRateHasChanged(true);
							// if using crossfade, ignore end of file
							if (!bFading) {
								// Benefit from end of file to perform a full gc
								System.gc();
								if (lDuration > 0) {
									// if corrupted file, length=0 and we have
									// not not call finished as it is managed by
									// Player
									FIFO.getInstance().finished();
								}
							} else {
								// If fading, next track has already been
								// launched
								bFading = false;
							}
						} catch (Exception e) {
							Log.error(e);
						}
						break;
					}
					// Opening ?
					else if (line.matches(".*Starting playback.*")) {
						bOpening = false;
					}
				}
				// can reach this point at the end of file
				in.close();
				return;
			} catch (Exception e) {
				// A stop causes a steam close exception, so ignore it
				if (!e.getMessage().matches(".*Stream closed")) {
					Log.error(e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.File, float, long,
	 *      float)
	 */
	public void play(org.jajuk.base.File file, float fPosition, long length, float fVolume)
			throws Exception {
		this.lTime = 0;
		this.fVolume = fVolume;
		this.length = length;
		this.fPosition = fPosition;
		this.bFading = false;
		this.fCurrent = file;
		this.bOpening = true;
		this.bHasBeenRated = false;
		this.bEOF = false;
		this.iFadeDuration = 1000 * ConfigurationManager.getInt(CONF_FADE_DURATION);
		ProcessBuilder pb = new ProcessBuilder(buildCommand(file.getAbsolutePath()));
		Log.debug("Using this Mplayer command: " + pb.command());
		// Set all environment variables format: var1=xxx var2=yyy
		try {
			Map<String, String> env = pb.environment();
			StringTokenizer st = new StringTokenizer(ConfigurationManager
					.getProperty(CONF_ENV_VARIABLES), " ");
			while (st.hasMoreTokens()) {
				StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
				env.put(st2.nextToken(), st2.nextToken());
			}
		} catch (Exception e) {
			Log.error(e);
		}
		proc = pb.start();
		if (position == null) {
			position = new PositionThread();
			position.start();
		}
		reader = new ReaderThread();
		reader.start();
		// if opening, wait
		int i = 0;
		while (bOpening && i < 500) {
			try {
				Thread.sleep(10);
				i++;
			} catch (InterruptedException e) {
				Log.error(e);
			}
		}
		// If end of file already reached, it means that file cannot be read
		if (bEOF) {
			throw new JajukException(7);
		}
		// Get track length
		sendCommand("get_time_length");
		if (fPosition > 0.0f) {
			seek(fPosition);
		}
		setVolume(fVolume);
	}

	/**
	 * @return current position as a float ex: 0.2f
	 */
	public float getCurrentPosition() {
		if (lDuration == 0) {
			return 0;
		}
		return ((float) lTime) / lDuration;
	}

	/**
	 * @return Returns the lTime in ms
	 */
	public long getElapsedTime() {
		return lTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.players.IPlayerImpl#pause()
	 */
	public void pause() throws Exception {
		bPaused = true;
		sendCommand("pause");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.players.IPlayerImpl#resume()
	 */
	public void resume() throws Exception {
		bPaused = false;
		sendCommand("pause");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.players.IPlayerImpl#seek(float) Ogg vorbis seek not yet
	 *      supported
	 */
	public void seek(float posValue) {
		// if fading, ignore
		if (bFading) {
			return;
		}
		// save current position
		String command = "seek " + (int) (100 * posValue) + " 1";
		sendCommand(command);
		setVolume(fVolume); // need this because a seek reset volume
	}

	/**
	 * @return player state, -1 if player is null.
	 */
	public int getState() {
		if (bFading) {
			return FADING_STATUS;
		} else {
			return -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.players.IPlayerImpl#getCurrentLength()
	 */
	public long getCurrentLength() {
		return lDuration;
	}

}