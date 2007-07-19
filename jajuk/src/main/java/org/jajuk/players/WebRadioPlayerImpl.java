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
 *  $Revision: 2523 $
 */
package org.jajuk.players;

import org.jajuk.base.FIFO;
import org.jajuk.base.File;
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
 * Jajuk web radio player implementation based on Mplayer
 */
public class WebRadioPlayerImpl implements IPlayerImpl, ITechnicalStrings {

	/** Stored Volume */
	private float fVolume;

	/** Mplayer process */
	private volatile Process proc;

	/** Current reader thread */
	private volatile ReaderThread reader;
	
	/** End of file flag * */
	private volatile boolean bEOF = false;

	/** File is opened flag * */
	private volatile boolean bOpening = false;

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
					}
					else if (line.matches("Exiting.*End.*")) {
						bEOF = true;
						bOpening = false;
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
	public void play(WebRadio radio, float fVolume)
			throws Exception {
		this.fVolume = fVolume;
		this.bOpening = true;
		this.bEOF = false;
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
					radio.getUrl().toExternalForm() };
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
			cmd[cmd.length - 1] = radio.getUrl().toExternalForm();
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
		reader = new ReaderThread();
		reader.start();
		// if opening, wait
		int i = 0;
		while (bOpening && i < 500) {
			try {
				Thread.sleep(100);
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
	 * @return current volume as a float ex: 0.2f
	 */
	public float getCurrentVolume() {
		return fVolume;
	}

	
	/**
	 * @return player state, -1 if player is null.
	 */
	public int getState() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.players.IPlayerImpl#getCurrentLength()
	 */
	public long getCurrentLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.players.IPlayerImpl#getCurrentPosition()
	 */
	public float getCurrentPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.players.IPlayerImpl#getElapsedTime()
	 */
	public long getElapsedTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.players.IPlayerImpl#pause()
	 */
	public void pause() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.File, float, long, float)
	 */
	public void play(File file, float fPosition, long length, float fVolume) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jajuk.players.IPlayerImpl#resume()
	 */
	public void resume() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jajuk.players.IPlayerImpl#seek(float)
	 */
	public void seek(float fPosition) {
		// TODO Auto-generated method stub
		
	}

	}