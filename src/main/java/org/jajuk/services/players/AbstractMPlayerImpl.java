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

import org.apache.commons.lang3.StringUtils;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mplayer player implementation.
 */
public abstract class AbstractMPlayerImpl implements IPlayerImpl, Const {
  /** pause flag *. */
  protected boolean bPaused = false;
  /** Stored Volume. */
  float fVolume;
  /** Mplayer process. */
  Process proc;
  /** End of file flag *. */
  boolean bEOF = false;
  /** File is opened flag *. */
  boolean bOpening = false;
  /** Stop position thread flag. */
  boolean bStop = false;
  /** Fading state. */
  boolean bFading = false;
  /** Whether the track has been started in bitperfect mode **/
  boolean bitPerfect = false;

  /*
   *
   * Kill abruptly the mplayer process (this way, killing is synchronous, and
   * easier than sending a quit command). Do not try to send a 'quit' command to
   * mplayer because then, it's not possible to differentiate end of file from
   * forced quit and the fifo will comes out of control
   */
  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#stop()
   */
  @Override
  public void stop() throws Exception {
    bFading = false;
    this.bStop = true;
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
  @Override
  public void setVolume(float fVolume) {
    this.fVolume = fVolume;
    // Fix for a issue under Linux (at least with pulseaudio) : if a track is started in bitperfect mode (no volume specified), then
    // the mode is unset when the same track is playing. When the fade out occurs, the volume commands sent to mplayer are propagated for some reasons
    // directly to the pulsaudio mixer and the next track sound volume is affected (muted most of times).
    if (bitPerfect) {
      Log.warn("This track was started in bit-perfect mode, even if the mode has been disabled, it can apply only to next track");
      return;
    }
    sendCommand("volume " + (int) (100 * fVolume) + " 2");
    // Not not log this when fading, generates too much logs
    if (!bFading) {
      Log.debug("Set Volume= " + (int) (100 * fVolume) + " %");
    }
  }

  /**
   * Send a command to mplayer slave.
   */
  protected void sendCommand(String command) {
    if (proc != null) {
      PrintStream out = new PrintStream(proc.getOutputStream());
      // Do not use println() : it doesn't work under windows
      out.print(command + '\n');
      out.flush();
      // don't close out here otherwise the output stream of the Process
      // will be closed as well and subsequent sendCommand() calls will silently
      // fail!!
    }
  }

  /*
  * (non-Javadoc)
  *
  * @see org.jajuk.players.IPlayerImpl#getCurrentVolume()
  */
  @Override
  public float getCurrentVolume() {
    return fVolume;
  }

  /**
   * Build the mplayer command line.
   *
   * @param url the url to play
   * @param startPositionSec the position in the track when starting in secs (0 means we plat from the begining)
   *
   * @return command line as a String array
   */
  List<String> buildCommand(String url, int startPositionSec) {
    String sCommand = "mplayer";
    // Use any forced mplayer path
    String forced = Conf.getString(Const.CONF_MPLAYER_PATH_FORCED);
    if (!StringUtils.isBlank(forced)) {
      sCommand = forced;
    } else {
      if (UtilSystem.isUnderWindows()) {
        sCommand = UtilSystem.getMPlayerWindowsPath().getAbsolutePath();
      } else if (UtilSystem.isUnderOSX()) {
        sCommand = UtilSystem.getMPlayerOSXPath().getAbsolutePath();
      }
    }
    String sAdditionalArgs = Conf.getString(Const.CONF_MPLAYER_ARGS);
    // Build command
    List<String> cmd = new ArrayList<>(10);
    cmd.add(sCommand);
    // -novideo is required by https://trac.mplayerhq.hu/ticket/2378
    cmd.add("-novideo");
    // Start at given position
    cmd.add("-ss");
    cmd.add(Integer.toString(startPositionSec));
    // quiet: less traces
    cmd.add("-quiet");
    // slave: slave mode (control with stdin)
    cmd.add("-slave");
    // No af options if bit perfect is enabled
    if (!Conf.getBoolean(CONF_BIT_PERFECT)) {
      // -af volume: Use volnorm to limit gain to max
      // If mute, use -200db otherwise, use a linear scale
      cmd.add("-af");
      cmd.add(buildAudioFilters());
      // -softvol : use soft mixer, allows to set volume only to this mplayer
      // instance, not others programs
      cmd.add("-softvol");
    }
    // Define a cache. It is useful to avoid sound gliches but also to
    // overide a local mplayer large cache configuration in
    // ~/.mplayer/config file. User can set a large cache for video for ie.
    String cacheSize = "500";
    // 500Kb, mplayer starts before the cache is filled up
    if (!Conf.getString(Const.CONF_MPLAYER_ARGS).matches(".*-cache.*")) {
      // If user already forced a cache value, do not overwrite it
      cmd.add("-cache");
      cmd.add(cacheSize);
    }
    if (!StringUtils.isBlank(sAdditionalArgs)) {
      // Add any additional arguments provided by user
      String[] sArgs = sAdditionalArgs.split(" ");
      Collections.addAll(cmd, sArgs);
    }
    // If it is a playlist, add the -playlist option, must be the last option
    // because options after -playlist are ignored (see mplayer man page).
    // Moreover, we only use this option if we are about to play line-based stream like m3u or the playback will fail.
    if (url.matches(".*://.*")
        && (url.toLowerCase().endsWith(".m3u") || url.toLowerCase().endsWith(".asx") || url
            .toLowerCase().endsWith(".pls"))) {
      cmd.add("-playlist");
    }
    cmd.add(url);
    return cmd;
  }

  /**
   * Build the -af audio filters command part.
   *
   * @return the string
   */
  private String buildAudioFilters() {
    // Audio filters syntax : -af
    // <filter1[=parameter1:parameter2:...],filter2,...>
    // Add -volnorm (audio normalization) if option is set
    StringBuilder audiofilters = new StringBuilder();
    if (Conf.getBoolean(CONF_USE_VOLNORM)) {
      audiofilters.append("volnorm,");
    }
    // gain = -200 = mute
    int volume = -200;
    if (fVolume != 0) {
      // Gain = 10 * log(fVolume)
      volume = (int) (10 * Math.log(fVolume));
    }
    audiofilters.append("volume=").append(volume);
    // Add karaoke state if required
    if (Conf.getBoolean(CONF_STATE_KARAOKE)) {
      audiofilters.append(",karaoke");
    }
    return audiofilters.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.players.IPlayerImpl#getDurationSec()
   */
  @Override
  public long getDurationSec() {
    return 0;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.players.IPlayerImpl#getCurrentPosition()
   */
  @Override
  public float getCurrentPosition() {
    return 0;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.players.IPlayerImpl#getElapsedTimeMillis()
   */
  @Override
  public long getElapsedTimeMillis() {
    return 0;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.players.IPlayerImpl#seek(float)
   */
  @Override
  public void seek(float fPosition) {
    // required by interface, but nothing to do here...
  }

  /**
   * Gets the state.
   *
   * @return player state, -1 if player is null.
   */
  @Override
  public int getState() {
    return -1;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.players.IPlayerImpl#pause()
   */
  @Override
  public void pause() {
    bPaused = true;
    sendCommand("pause");
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.players.IPlayerImpl#resume()
   */
  @Override
  public void resume() throws Exception {
    // This test is required because in case of volume change, mplayer is
    // already resumed and we don't want to send another pause command
    if (bPaused) {
      bPaused = false;
      sendCommand("pause");
    }
  }
}
