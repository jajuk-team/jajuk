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
package org.jajuk.base;

import org.jajuk.i18n.Messages;
import org.jajuk.players.IPlayerImpl;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

import java.util.Properties;

import javax.sound.sampled.LineUnavailableException;

import javazoom.jlgui.basicplayer.BasicPlayer;

/**
 * abstract class for music player, independent from real implementation
 */
public class Player implements ITechnicalStrings {

	/** Current file read */
	private static File fCurrent;

	/** Current player used */
	private static IPlayerImpl playerImpl;

	/** Current player used nb 1 */
	private static IPlayerImpl playerImpl1;

	/** Current player used nb 2 */
	private static IPlayerImpl playerImpl2;

	/** Mute flag */
	private static boolean bMute = false;

	/** Paused flag */
	private static boolean bPaused = false;

	/** Playing ? */
	private static boolean bPlaying = false;

	/**
	 * Asynchronous play for specified file with specified time interval
	 * 
	 * @param file
	 *            to play
	 * @param position
	 *            in % of the file length. ex 0.1 for 10%
	 * @param length
	 *            in ms
	 * @return true if play is OK
	 */
	public static boolean play(final File file, final float fPosition, final long length) {
		fCurrent = file;
		try {
			playerImpl = null;
			// Choose the player
			Class cPlayer = file.getTrack().getType().getPlayerClass();
			// player 1 null ?
			if (playerImpl1 == null) {
				playerImpl1 = (IPlayerImpl) cPlayer.newInstance();
				playerImpl = playerImpl1;
			}
			// player 1 not null, test if it is fading
			else if (playerImpl1.getState() != FADING_STATUS) {
				// not fading but different player class, reinstanciate it
				if (!playerImpl1.getClass().equals(cPlayer)) {
					playerImpl1 = (IPlayerImpl) cPlayer.newInstance();
				}
				playerImpl = playerImpl1;
			}
			// player 1 fading, OK, test player 2
			else if (playerImpl2 == null) {
				playerImpl2 = (IPlayerImpl) cPlayer.newInstance();
				playerImpl = playerImpl2;
			}
			// if here, the only normal case is player 1 is fading and
			// player 2 not null and not fading
			else {
				// not fading but different player class, reinstanciate it
				if (!playerImpl2.getClass().equals(cPlayer)) {
					playerImpl2 = (IPlayerImpl) cPlayer.newInstance();
				}
				playerImpl = playerImpl2;
			}
			// Force stopping any playing track playing by this player
			playerImpl.stop();
			bPlaying = true;
			bPaused = false;
			boolean bWaitingLine = true;
			while (bWaitingLine) {
				try {
					if (bMute) {
						playerImpl.play(fCurrent, fPosition, length, 0.0f);
					} else {
						playerImpl.play(fCurrent, fPosition, length, ConfigurationManager
								.getFloat(CONF_VOLUME));
					}
					bWaitingLine = false;
				} catch (Exception bpe) {
					if (!(bpe.getCause() instanceof LineUnavailableException)) {
						throw bpe;
					}
					bWaitingLine = true;
					Log.debug("Line occupied, waiting");
					InformationJPanel.getInstance().setMessage(Messages.getString("Player.0"),
							InformationJPanel.WARNING);
					try {
						// wait for the line
						FIFO.getInstance().wait(WAIT_AFTER_ERROR);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
			return true;
		} catch (final Throwable t) {
			Properties pDetails = new Properties();
			pDetails.put(DETAIL_CURRENT_FILE, file);
			ObservationManager.notifySync(new Event(EventSubject.EVENT_PLAY_ERROR, pDetails));
			Log.error("007", Messages.getString("Player.0") + fCurrent.getAbsolutePath() + "}}", t);
			// process playing error asynchronously to avoid loop problems
			// when cascading errors
			new Thread() {
				public void run() {
					Player.stop(false);
					try {
						Thread.sleep(WAIT_AFTER_ERROR);
					} catch (InterruptedException e) {
						Log.error(e);
					}
					FIFO.getInstance().finished();
				}
			}.start();
			return false;
		}
	}

	/**
	 * Stop the played track
	 * 
	 * @param bAll
	 *            stop fading tracks as well ?
	 */
	public static void stop(boolean bAll) {
		try {
			if (playerImpl == null) { // none current player, leave
				return;
			}
			if (fCurrent != null) {
				if (playerImpl1 != null && (playerImpl1.getState() != FADING_STATUS || bAll)) {
					playerImpl1.stop();
				}
				if (playerImpl2 != null && (playerImpl2.getState() != FADING_STATUS || bAll)) {
					playerImpl2.stop();
				}
				bPaused = false; // cancel any current pause
				bPlaying = false;
			}
		} catch (Exception e) {
			Log.debug(Messages.getString("Error.008") + ":" + fCurrent.getName() + " "
					+ e.getMessage());
		}
	}

	/**
	 * Alternative Mute/unmute the player
	 * 
	 * @throws Exception
	 */
	public static void mute() {
		try {
			Player.bMute = !Player.bMute;
			// notify UI
			ObservationManager.notify(new Event(EventSubject.EVENT_MUTE_STATE));
			if (playerImpl == null) { // none current player, leave
				return;
			}
			if (Player.bMute) {
				if (playerImpl1 != null) {
					playerImpl1.setVolume(0.0f);
				}
				if (playerImpl2 != null) {
					playerImpl2.setVolume(0.0f);
				}
			} else {
				// already muted, unmute it by setting the
				// volume previous mute
				playerImpl.setVolume(ConfigurationManager.getFloat(CONF_VOLUME));
			}

		} catch (Exception e) {
			Log.error(e);
		}
	}

	/**
	 * Mute/unmute the player
	 * 
	 * @param bMute
	 * @throws Exception
	 */
	public static void mute(boolean bMute) {
		try {
			if (playerImpl == null) { // none current player, leave
				return;
			}
			if (bMute) {
				if (playerImpl1 != null) {
					playerImpl1.setVolume(0.0f);
				}
				if (playerImpl2 != null) {
					playerImpl2.setVolume(0.0f);
				}
			} else {
				playerImpl.setVolume(ConfigurationManager.getFloat(CONF_VOLUME));
			}
			Player.bMute = bMute;
		} catch (Exception e) {
			Log.error(e);
		}
	}

	/**
	 * 
	 * @return whether the player is muted or not
	 * @throws Exception
	 */
	public static boolean isMuted() {
		return bMute;
	}

	/**
	 * Set the gain
	 * 
	 * @param fVolume :
	 *            gain from 0 to 1
	 * @throws Exception
	 */
	public static void setVolume(float pVolume) {
		float fVolume = pVolume;
		try {
			ConfigurationManager.setProperty(CONF_VOLUME, Float.toString(fVolume));
			if (playerImpl != null) {
				// check, it can be over 1 for unknown reason
				if (fVolume < 0.0f) {
					fVolume = 0.0f;
				} else if (fVolume > 1.0f) {
					fVolume = 1.0f;
				}
				playerImpl.setVolume(fVolume);
				ObservationManager.notify(new Event(EventSubject.EVENT_VOLUME_CHANGED));
			}
		} catch (Exception e) {
			Log.error(e);
		}
	}

	/**
	 * @return Returns the lTime in ms
	 */
	public static long getElapsedTime() {
		if (playerImpl != null) {
			return playerImpl.getElapsedTime();
		} else {
			return 0;
		}
	}

	/** Pause the player */
	public static void pause() {
		try {
			if (!bPlaying) { // ignore pause when not playing to avoid
				// confusion between two tracks
				return;
			}
			if (playerImpl != null) {
				playerImpl.pause();
			}
			bPaused = true;
		} catch (Exception e) {
			Log.error(e);
		}
	}

	/** resume the player */
	public static void resume() {
		try {
			if (playerImpl == null) { // none current player, leave
				return;
			}
			playerImpl.resume();
			bPaused = false;
		} catch (Exception e) {
			Log.error(e);
		}
	}

	/**
	 * @return whether player is paused
	 */
	public static boolean isPaused() {
		return bPaused;
	}

	/**
	 * Force the bPaused state to allow to cancel a pause without restarting the
	 * current played track (rew for exemple)
	 * 
	 * @param bPaused
	 */
	public static void setPaused(boolean bPaused) {
		Player.bPaused = bPaused;
	}

	/** Seek to a given position in %. ex : 0.2 for 20% */
	public static void seek(float pfPosition) {
		float fPosition = pfPosition;
		if (playerImpl == null) { // none current player, leave
			return;
		}
		// bound seek
		if (fPosition < 0.0f) {
			fPosition = 0.0f;
		} else if (fPosition >= 1.0f) {
			fPosition = 0.99f;
		}
		try {
			Log.debug("Seeking to: " + fPosition);
			playerImpl.seek(fPosition);
		} catch (Exception e) { // we can get some errors in unexpected cases
			Log.debug(e.toString());
		}

	}

	/**
	 * @return position in track in %
	 */
	public static float getCurrentPosition() {
		if (playerImpl != null) {
			return playerImpl.getCurrentPosition();
		} else {
			return 0.0f;
		}
	}

	/**
	 * @return current track length in secs
	 */
	public static long getCurrentLength() {
		if (playerImpl != null) {
			return playerImpl.getCurrentLength();
		} else {
			return 0l;
		}
	}

	/**
	 * @return volume in track in %
	 */
	public static float getCurrentVolume() {
		if (playerImpl != null) {
			return playerImpl.getCurrentVolume();
		} else {
			return 0.0f;
		}
	}

	/**
	 * @return Returns the bPlaying.
	 */
	public static boolean isPlaying() {
		return bPlaying;
	}

	/**
	 * 
	 * @return whether current player is seeking
	 */
	public static boolean isSeeking() {
		return (playerImpl != null && playerImpl.getState() == BasicPlayer.SEEKING);
	}
}
