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
package org.jajuk.services.players;

import java.io.PrintStream;
import java.util.ArrayList;

import org.jajuk.base.File;
import org.jajuk.base.WebRadio;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Mplayer player implementation
 */
abstract public class AbstractMPlayerImpl implements IPlayerImpl, ITechnicalStrings {

  /** Stored Volume */
  float fVolume;

  /** Mplayer process */
  volatile Process proc;

  /** End of file flag * */
  volatile boolean bEOF = false;

  /** File is opened flag * */
  volatile boolean bOpening = false;

  /** Stop position thread flag */
  volatile boolean bStop = false;

  /** Fading state */
  volatile boolean bFading = false;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.IPlayerImpl#stop()
   */
  public void stop() throws Exception {
    bFading = false;
    this.bStop = true;
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
    sendCommand("volume " + (int) (100 * fVolume) + " 2");
    // Not not log this when fading, generates too mush logs
    if (!bFading) {
      Log.debug("Set Volume= " + (int) (100 * fVolume) + " %");
    }
  }

  /**
   * Send a command to mplayer slave
   * 
   * @param command
   */
  protected void sendCommand(String command) {
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
   * Build the mplayer command line
   * 
   * @param url
   *          to play
   * @return command line as a String array
   */
  ArrayList<String> buildCommand(String url) {
    String sCommand = "mplayer"; //$NON-NLS-1$
    // Use any forced mplayer path
    String forced = ConfigurationManager.getProperty(CONF_MPLAYER_PATH_FORCED);
    if (!Util.isVoid(forced)) {
      sCommand = forced;
    } else {
      if (Util.isUnderWindows()) {
        sCommand = Util.getMPlayerWindowsPath().getAbsolutePath();
      } else if (Util.isUnderOSXintel() || Util.isUnderOSXpower()) {
        sCommand = Util.getMPlayerOSXPath();
      }
    }
    String sAdditionalArgs = ConfigurationManager.getProperty(CONF_MPLAYER_ARGS);
    // Build command
    ArrayList<String> cmd = new ArrayList<String>(10);
    cmd.add(sCommand);
    // quiet: less traces
    cmd.add("-quiet");
    // slave: slave mode (control with stdin)
    cmd.add("-slave");
    // -af volume: Use volnorm to limit gain to max
    // If mute, use -200db otherwise, use a linear scale
    cmd.add("-af");
    cmd.add("volume=" + ((fVolume == 0) ? -200 : ((int) (25 * fVolume) - 20)));
    // -softvol : use soft mixer, allows to set volume only to this mplayer
    // instance, not others programs
    cmd.add("-softvol");
    // Define a cache. It is useful to avoid sound gliches but also to
    // overide a local mplayer large cache configuration in
    // ~/.mplayer/config file. User can set a large cache for video for ie.
    String cacheSize = "500";
    // 500Kb, mplayer starts before the cache is filled up
    if (!ConfigurationManager.getProperty(CONF_MPLAYER_ARGS).matches(".*-cache.*")) {
      // If user already forced a cache value, do not overwrite it
      cmd.add("-cache");
      cmd.add(cacheSize);
    }
    if (!Util.isVoid(sAdditionalArgs)) {
      // Add any additional arguments provided by user
      String[] sArgs = sAdditionalArgs.split(" ");
      for (int i = 0; i < sArgs.length; i++) {
        cmd.add(sArgs[i]);
      }
    }
    cmd.add(url);
    return cmd;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#getCurrentLength()
   */
  public long getCurrentLength() {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#getCurrentPosition()
   */
  public float getCurrentPosition() {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#getElapsedTime()
   */
  public long getElapsedTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#pause()
   */
  public void pause() throws Exception {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.File, float, long,
   *      float)
   */
  public abstract void play(File file, float fPosition, long length, float fVolume)
      throws Exception;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.WebRadio, float)
   */
  public abstract void play(WebRadio radio, float fVolume) throws Exception;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#resume()
   */
  public void resume() throws Exception {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#seek(float)
   */
  public void seek(float fPosition) {
    // TODO Auto-generated method stub

  }

  /**
   * @return player state, -1 if player is null.
   */
  public int getState() {
    return -1;
  }

}