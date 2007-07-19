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

import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.WebRadio;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Jajuk player implementation based on Mplayer
 */
public class MPlayerPlayerImpl implements IPlayerImpl, ITechnicalStrings {

	/** Time elapsed in ms */
	private long lTime = 0;

	/** Date of last elapsed time update */
	private long lDateLastUpdate = System.currentTimeMillis();

	/** Length to be played in secs */
	private long length;

	/** Starting position */
	private float fPosition;

	/** Stored Volume */
	private float fVolume;

	/** Current track estimated duration in ms */
	private long lDuration;

	/** Cross fade duration in ms */
	int iFadeDuration = 0;

	/** Fading state */
	private volatile boolean bFading = false;

	/** Progress step in ms */
	private static final int PROGRESS_STEP = 300;// need a fast refresh,

	// especially for fading

	/** current file */
	private org.jajuk.base.File fCurrent;

	/** Mplayer process */
	private volatile Process proc;

	/** pause flag * */
	private volatile boolean bPaused = false;

	/** End of file flag * */
	private volatile boolean bEOF = false;

	/** File is opened flag * */
	private volatile boolean bOpening = false;

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
		/** Stop flag */
		volatile boolean bStop = false;

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

		public void stopThread() {
			this.bStop = true;
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
							// force a finished (that doesn't stop but only
							// make a FIFO request to switch track)
							FIFO.getInstance().finished();
						}
						// test end of length for intro mode
						if (length != TO_THE_END && lDuration > 0
						// can be null before getting length
								&& (lTime - (fPosition * lDuration)) > length) {
							// length=-1 means there is no max length
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
							// alert bestof playlist something changed
							FileManager.getInstance().setRateHasChanged(true);
							// if using crossfade, ignore end of file
							if (!bFading) {
								// Benefit from end of file to perform a full gc
								System.gc();
								if (lDuration > 0) {
									// if corrupted file, length=0 and we have
									// not not call finished
									// as it is managed my Player
									FIFO.getInstance().finished();
								}
							} else {
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
		// Start
		String sCommand = "mplayer"; //$NON-NLS-1$
		if (Util.isUnderWindows()) {
			sCommand = Util.getMPlayerWindowsPath();
		} else if (Util.isUnderOSXintel() || Util.isUnderOSXpower()) {
			sCommand = Util.getMPlayerOSXPath();
		}
		Log.debug("Using command: " + sCommand);
		int cacheSize = 1000;
		String sAdditionalArgs = ConfigurationManager.getProperty(CONF_MPLAYER_ARGS);
		String[] cmd = null;
		if (sAdditionalArgs == null || sAdditionalArgs.trim().equals("")) {
			// Use a cache for slow devices
			cmd = new String[] { sCommand, "-quiet", "-slave", "-cache", "" + cacheSize,
					file.getAbsolutePath() };
		} else {
			// Add any additional arguments provided by user
			String[] sArgs = sAdditionalArgs.split(" ");
			// If user already forced a cache value, do not overwrite it
			if (ConfigurationManager.getProperty(CONF_MPLAYER_ARGS).matches(".*-cache.*")) {
				cmd = new String[4 + sArgs.length];
				cmd[0] = sCommand;
				cmd[1] = "-quiet";
				cmd[2] = "-slave";
				for (int i = 0; i < sArgs.length; i++) {
					cmd[3 + i] = sArgs[i];
				}
			} else {
				cmd = new String[4 + sArgs.length];
				cmd[0] = sCommand;
				cmd[1] = "-quiet";
				cmd[2] = "-slave";
				cmd[3] = "-cache";
				cmd[4] = "" + cacheSize;
				for (int i = 0; i < sArgs.length; i++) {
					cmd[5 + i] = sArgs[i];
				}
			}
			cmd[cmd.length - 1] = file.getAbsolutePath();
		}
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Log.debug("Using this Mplayer command: " + Arrays.asList(cmd));
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
		setVolume(fVolume);
		// Get track length
		sendCommand("get_time_length");
		if (fPosition > 0.0f) {
			seek(fPosition);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.IPlayerImpl#stop()
	 */
	public void stop() throws Exception {
		// Kill abruptly the mplayer process (this way, killing is synchronous,
		// and easier than sending a quit command)
		Log.debug("Stop");
		if (proc != null) {
			proc.destroy();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.IPlayerImpl#setVolume(float)
	 */
	public void setVolume(float fVolume) {
		this.fVolume = fVolume;
		Log.debug("Volume=" + (int) (100 * fVolume));
		sendCommand("volume " + (int) (100 * fVolume) + " 2");
	}

	/**
	 * Send a command to mplayer slave
	 * 
	 * @param command
	 */
	private void sendCommand(String command) {
		if (proc != null) {
			PrintStream out = new PrintStream(proc.getOutputStream());
			// Do not use println() : it doesn't work under windows
			out.print(command + '\n');
			out.flush();
		}
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
	 * @return current volume as a float ex: 0.2f
	 */
	public float getCurrentVolume() {
		return fVolume;
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

	/* (non-Javadoc)
	 * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.WebRadio, float)
	 */
	public void play(WebRadio radio, float fVolume) throws Exception {
		// TODO Auto-generated method stub
		
	}
}