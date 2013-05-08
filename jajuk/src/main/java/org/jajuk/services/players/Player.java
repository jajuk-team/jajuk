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
package org.jajuk.services.players;

import java.util.Properties;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JOptionPane;

import javazoom.jlgui.basicplayer.BasicPlayer;

import org.jajuk.base.File;
import org.jajuk.base.TypeManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukRuntimeException;
import org.jajuk.util.log.Log;

/**
 * abstract class for music player, independent from real implementation.
 */
public final class Player {
  /** The Constant PLAYER_0.   */
  private static final String PLAYER_0 = "Player.0";
  /** Current file read. */
  private static File fCurrent;
  /** Current player used. */
  private static IPlayerImpl playerImpl;
  /** Current player used nb 1. */
  private static IPlayerImpl playerImpl1;
  /** Current player used nb 2. */
  private static IPlayerImpl playerImpl2;
  /** Mute flag. */
  private static boolean bMute = false;
  /** Paused flag. */
  private static boolean bPaused = false;
  /** Playing ?. */
  private static boolean bPlaying = false;

  /**
   * private constructor to avoid instantiating utility class.
   */
  private Player() {
  }

  /**
   * Asynchronous play for specified file with specified time interval.
   *
   * @param file to play
   * @param fPosition 
   * @param length in ms
   * @return true if play is OK
   */
  public static boolean play(final File file, final float fPosition, final long length) {
    if (file == null) {
      throw new IllegalArgumentException("Cannot play empty file.");
    }
    // Check if the file is on a mounted device, should be tested before but not always, see #1915
    if (!file.getDevice().isMounted()) {
      // not mounted, ok let them a chance to mount it:
      final String sMessage = Messages.getString("Error.025") + " ("
          + file.getDirectory().getDevice().getName() + Messages.getString("FIFO.4");
      int i = Messages.getChoice(sMessage, JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.INFORMATION_MESSAGE);
      if (i == JOptionPane.YES_OPTION) {
        try {
          file.getDevice().mount(true);
        } catch (Exception e) {
          Log.error(e);
          Messages.showErrorMessage(11, file.getDevice().getName());
          throw new JajukRuntimeException("Device not mounted");
        }
      } else { // "cancel" or "no"
        throw new JajukRuntimeException("Device not mounted");
      }
    }
    fCurrent = file;
    try {
      // Choose the player
      Class<IPlayerImpl> cPlayer = file.getTrack().getType().getPlayerClass();
      // player 1 null ?
      if (playerImpl1 == null) {
        playerImpl1 = cPlayer.newInstance();
        playerImpl = playerImpl1;
      }
      // player 1 not null, test if it is fading
      else if (playerImpl1.getState() != Const.FADING_STATUS) {
        // stop it
        playerImpl1.stop();
        playerImpl1 = cPlayer.newInstance();
        playerImpl = playerImpl1;
      }
      // player 1 fading, test player 2
      else if (playerImpl2 == null) {
        playerImpl2 = cPlayer.newInstance();
        playerImpl = playerImpl2;
      }
      // if here, the only normal case is player 1 is fading and
      // player 2 not null and not fading
      else {
        // stop it
        playerImpl2.stop();
        playerImpl2 = cPlayer.newInstance();
        playerImpl = playerImpl2;
      }
      bPlaying = true;
      bPaused = false;
      boolean bWaitingLine = true;
      while (bWaitingLine) {
        try {
          if (bMute) {
            playerImpl.play(fCurrent, fPosition, length, 0.0f);
          } else {
            playerImpl.play(fCurrent, fPosition, length, Conf.getFloat(Const.CONF_VOLUME));
          }
          bWaitingLine = false;
        } catch (Exception bpe) {
          if (!(bpe.getCause() instanceof LineUnavailableException)) {
            throw bpe;
          }
          bWaitingLine = true;
          Log.debug("Line occupied, waiting");
          InformationJPanel.getInstance().setMessage(Messages.getString(PLAYER_0),
              InformationJPanel.MessageType.WARNING);
          // wait for the line
          QueueModel.class.wait(Const.WAIT_AFTER_ERROR);
        }
      }
      // Save playing state    
      Conf.setProperty(Const.CONF_STARTUP_STOPPED, "false");
      return true;
    } catch (final Throwable t) {
      Properties pDetails = new Properties();
      pDetails.put(Const.DETAIL_CONTENT, file);
      ObservationManager.notifySync(new JajukEvent(JajukEvents.PLAY_ERROR, pDetails));
      Log.error(7, Messages.getString(PLAYER_0) + "{{" + fCurrent.getName() + "}}", t);
      return false;
    }
  }

  /**
   * Play a web radio stream.
   * 
   * @param radio 
   * 
   * @return true, if play
   */
  public static boolean play(WebRadio radio) {
    try {
      // check mplayer availability
      if (TypeManager.getInstance().getTypeByExtension(Const.EXT_RADIO) == null) {
        Messages.showWarningMessage(Messages.getString("Warning.4"));
        return false;
      }
      // Choose the player
      Class<IPlayerImpl> cPlayer = TypeManager.getInstance().getTypeByExtension(Const.EXT_RADIO)
          .getPlayerClass();
      // Stop all streams
      stop(true);
      playerImpl1 = cPlayer.newInstance();
      playerImpl = playerImpl1;
      bPlaying = true;
      bPaused = false;
      boolean bWaitingLine = true;
      while (bWaitingLine) {
        try {
          if (bMute) {
            playerImpl.play(radio, 0.0f);
          } else {
            playerImpl.play(radio, Conf.getFloat(Const.CONF_VOLUME));
          }
          bWaitingLine = false;
        } catch (Exception bpe) {
          if (!(bpe.getCause() instanceof LineUnavailableException)) {
            throw bpe;
          }
          bWaitingLine = true;
          Log.debug("Line occupied, waiting");
          InformationJPanel.getInstance().setMessage(Messages.getString(PLAYER_0),
              InformationJPanel.MessageType.WARNING);
          try {
            // wait for the line
            QueueModel.class.wait(Const.WAIT_AFTER_ERROR);
          } catch (InterruptedException e1) {
            Log.error(e1);
          }
        }
      }
      // Save playing state    
      Conf.setProperty(Const.CONF_STARTUP_STOPPED, "false");
      return true;
    } catch (final Throwable t) {
      Properties pDetails = new Properties();
      pDetails.put(Const.DETAIL_CONTENT, radio);
      ObservationManager.notifySync(new JajukEvent(JajukEvents.PLAY_ERROR, pDetails));
      Log.error(7, Messages.getString(PLAYER_0) + radio.getUrl() + "}}", t);
      return false;
    }
  }

  /**
   * Stop the played track.
   * 
   * @param bAll stop fading tracks as well ?
   */
  public static void stop(boolean bAll) {
    try {
      if (Conf.getBoolean(Const.CONF_FADE_OUT) && isPlaying()
          && !Conf.getBoolean(Const.CONF_BIT_PERFECT) && !QueueModel.isInternalStop() && !isMuted()) {
        fadeOut();
      }
      if (playerImpl1 != null && (playerImpl1.getState() != Const.FADING_STATUS || bAll)) {
        playerImpl1.stop();
        playerImpl1 = null;
      }
      if (playerImpl2 != null && (playerImpl2.getState() != Const.FADING_STATUS || bAll)) {
        playerImpl2.stop();
        playerImpl2 = null;
      }
      bPaused = false; // cancel any current pause
      bPlaying = false;
    } catch (Exception e) {
      Log.debug(Messages.getString("Error.008"), e);
    }
  }

  /**
   * Alternative Mute/unmute the player.
   *
   */
  public static void mute() {
    // Ignore mute changes if Bit-perfect mode is enabled
    // See code should not normally be called because we
    // disable associated GUI
    if (Conf.getBoolean(Const.CONF_BIT_PERFECT)) {
      Log.warn("Bit-perfect option enabled, software mutes ignored");
      return;
    }
    Player.bMute = !Player.bMute;
    mute(Player.bMute);
  }

  /**
   * Mute/unmute the player.
   *
   * @param pMute 
   */
  public static void mute(boolean pMute) {
    // Ignore mute changes if Bit-perfect mode is enabled
    // See code should not normally be called because we
    // disable associated GUI
    if (Conf.getBoolean(Const.CONF_BIT_PERFECT)) {
      Log.warn("Bit-perfect option enabled, software mutes ignored");
      return;
    }
    try {
      if (playerImpl == null) { // none current player, leave
        return;
      }
      if (pMute) {
        if (playerImpl1 != null) {
          playerImpl1.setVolume(0.0f);
        }
        if (playerImpl2 != null) {
          playerImpl2.setVolume(0.0f);
        }
      } else {
        playerImpl.setVolume(Conf.getFloat(Const.CONF_VOLUME));
      }
      Player.bMute = pMute;
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Checks if is muted.
   *
   * @return whether the player is muted or not
   */
  public static boolean isMuted() {
    return bMute;
  }

  /**
   * Set the gain.
   *
   * @param pVolume 
   */
  public static void setVolume(float pVolume) {
    // Ignore volume changes if Bit-perfect mode is enabled
    // See code should not normally be called because we
    // disable associated GUI
    if (Conf.getBoolean(Const.CONF_BIT_PERFECT)) {
      Log.warn("Bit-perfect option enabled, software volume changes ignored");
      return;
    }
    float fVolume = pVolume;
    try {
      // if user move the volume slider, unmute
      if (isMuted()) {
        mute(false);
      }
      // check, it can be over 1 when moving sliders
      if (pVolume < 0.0f) {
        fVolume = 0.0f;
      } else if (pVolume > 1.0f) {
        fVolume = 1.0f;
      }
      if (playerImpl != null) {
        playerImpl.setVolume(fVolume);
      }
      // Store the volume
      Conf.setProperty(Const.CONF_VOLUME, Float.toString(fVolume));
      // Require all GUI (like volume sliders) to update
      ObservationManager.notify(new JajukEvent(JajukEvents.VOLUME_CHANGED));
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Gets the elapsed time.
   * 
   * @return Returns the lTime in ms
   */
  public static long getElapsedTimeMillis() {
    if (playerImpl != null) {
      return playerImpl.getElapsedTimeMillis();
    } else {
      return 0;
    }
  }

  /**
   * Gets the elapsed time.
   * 
   * @return Returns the lTime in ms
   */
  public static long getActuallyPlayedTimeMillis() {
    if (playerImpl != null) {
      return playerImpl.getActuallyPlayedTimeMillis();
    } else {
      return 0;
    }
  }

  /**
   * Pause the player.
   */
  public static void pause() {
    try {
      if (!bPlaying) { // ignore pause when not playing to avoid
        // confusion between two tracks
        return;
      }
      if (playerImpl != null) {
        if (Conf.getBoolean(Const.CONF_FADE_OUT) && !Conf.getBoolean(Const.CONF_BIT_PERFECT)
            && !QueueModel.isInternalStop() && !isMuted()) {
          fadeOut();
        }
        playerImpl.pause();
      }
      bPaused = true;
      ObservationManager.notify(new JajukEvent(JajukEvents.PLAYER_PAUSE));
    } catch (Exception e) {
      Log.error(e);
    }
  }

  private static void fadeOut() throws Exception {
    float initialVolume = playerImpl.getCurrentVolume();
    if (initialVolume > 0) {
      //Fade out
      float steps = 5;
      int totalTimeMillis = 500;
      for (int i = 1; i <= steps; i++) {
        float newVolume = initialVolume * (steps - i) / steps;
        playerImpl.setVolume(newVolume);
        Thread.sleep((int) (totalTimeMillis / (steps - 1)));
      }
    }
  }

  /**
   * resume the player.
   */
  public static void resume() {
    try {
      if (playerImpl == null) { // none current player, leave
        return;
      }
      if (!isMuted()) {
        playerImpl.setVolume(Conf.getFloat(Const.CONF_VOLUME));
      }
      // If we are playing a webradio, we stop and restart because players 
      // like mplayer can't deal with resuming a stream (it cant restart under 
      // mplayer when the resume is done after the end of the buffer)
      if (QueueModel.isPlayingRadio()) {
        WebRadio radio = QueueModel.getCurrentRadio();
        stop(true);
        play(radio);
      } else {
        playerImpl.resume();
      }
      bPaused = false;
      ObservationManager.notify(new JajukEvent(JajukEvents.PLAYER_RESUME));
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Checks if is paused.
   * 
   * @return whether player is paused
   */
  public static boolean isPaused() {
    return bPaused;
  }

  /**
   * Seek to a given position in %. ex : 0.2 for 20%
   * 
   * @param pfPosition 
   */
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
   * Gets the current position.
   * 
   * @return position in track in % [0;1]
   */
  public static float getCurrentPosition() {
    if (playerImpl != null) {
      return playerImpl.getCurrentPosition();
    } else {
      return 0.0f;
    }
  }

  /**
   * Gets the current track total duration (secs).
   * 
   * @return current track total duration in secs
   */
  public static long getDurationSec() {
    if (playerImpl != null) {
      return playerImpl.getDurationSec();
    } else {
      return 0l;
    }
  }

  /**
   * Gets the current volume.
   * 
   * @return volume in track in %, ex : 0.2 for 20%
   */
  public static float getCurrentVolume() {
    if (playerImpl != null) {
      return playerImpl.getCurrentVolume();
    } else {
      return Conf.getFloat(Const.CONF_VOLUME);
    }
  }

  /**
   * Checks if is playing.
   * 
   * @return Returns the bPlaying.
   */
  public static boolean isPlaying() {
    return bPlaying;
  }

  /**
   * Checks if is seeking.
   * 
   * @return whether current player is seeking
   */
  public static boolean isSeeking() {
    return (playerImpl != null && playerImpl.getState() == BasicPlayer.SEEKING);
  }
}
