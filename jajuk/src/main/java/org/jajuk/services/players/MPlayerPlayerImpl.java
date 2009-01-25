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
 *  $Revision:3266 $
 */
package org.jajuk.services.players;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;

import org.jajuk.base.Track;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
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

  /**
   * Progress step in ms, do not set less than 300 or 400 to avoid using too
   * much CPU
   */
  private static final int PROGRESS_STEP = 500;

  /**
   * Total play time is refreshed every TOTAL_PLAYTIME_UPDATE_INTERVAL times
   */
  private static final int TOTAL_PLAYTIME_UPDATE_INTERVAL = 2;

  /** current file */
  private org.jajuk.base.File fCurrent;

  /**
   * Position and elapsed time getter
   */
  private class PositionThread extends Thread {
    public PositionThread(String name) {
      super(name);
    }

    @Override
    public void run() {
      int comp = 0;
      Track current = fCurrent.getTrack();
      while (!bStop && !bEOF) { // stop this thread
        try {
          if (!bPaused) {
            // a get_percent_pos resumes (mplayer issue)
            sendCommand("get_time_pos");
            // every 2 time units, increase actual play time. We wait this
            // delay for perfs and for precision
            if (comp > 0 && comp % TOTAL_PLAYTIME_UPDATE_INTERVAL == 0) {
              // Increase actual play time
              // End of file: increase actual play time to the track
              // Perf note : this full action takes less much than 1 ms
              long trackPlaytime = current.getLongValue(Const.XML_TRACK_TOTAL_PLAYTIME);
              long newValue = (PROGRESS_STEP * TOTAL_PLAYTIME_UPDATE_INTERVAL / 1000)
                  + trackPlaytime;
              current.setProperty(Const.XML_TRACK_TOTAL_PLAYTIME, newValue);
            }
            comp++;
          }
          Thread.sleep(PROGRESS_STEP);
        } catch (Exception e) {
          Log.error(e);
        }
      }
    }
  }

  /**
   * Reader : read information from mplayer like position
   */
  private class ReaderThread extends Thread {
    public ReaderThread(String name) {
      super(name);
    }

    @Override
    public void run() {
      try {
        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line = null;
        for (; !bStop;) {
          try {
            line = in.readLine();
            if (line == null) {
              break;
            }
          } catch (IOException ieo) {
            Log.debug("Stream closed");
            // Thrown in readLine() when killing the track (in intro mode for
            // ie)
            break;
          }
          // Very verbose : Log.debug("Output from MPlayer: " + line);
          if (line.matches(".*ANS_TIME_POSITION.*")) {
            // Stream is actually opened now
            bOpening = false;

            StringTokenizer st = new StringTokenizer(line, "=");
            st.nextToken();
            lTime = (int) (Float.parseFloat(st.nextToken()) * 1000);
            // Store current position for use at next startup
            Conf
                .setProperty(Const.CONF_STARTUP_LAST_POSITION, Float.toString(getCurrentPosition()));
            // Cross-Fade test
            if (!bFading && iFadeDuration > 0
            // Length = 0 for some buggy audio headers
                && lDuration > 0
                // Does fading time happened ?
                && lTime > (lDuration - iFadeDuration)
                // Do not fade if the track is very short
                && (lDuration > 3 * iFadeDuration)) {
              bFading = true;
              fadingVolume = fVolume;
              // Call finish (do not leave thread to allow cross fading)
              callFinish();
            }
            // If fading, decrease sound progressively
            if (bFading) {
              // computes the volume we have to sub to reach zero
              // at last progress()
              float fVolumeStep = fadingVolume
              // we double the refresh period to make sure to
                  // reach 0 at the end of iterations because
                  // we don't as many mplayer response as queries,
                  // tested on 10 & 20 sec of fading
                  * ((float) PROGRESS_STEP / iFadeDuration);
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
            // Test end of length for intro mode
            // Length=-1 means there is no max length
            if (length != TO_THE_END
            // Duration = 0 in rare case due to header issue
                && lDuration > 0
                // Is intro length fully played ?
                && (lTime - (fPosition * lDuration)) > length) {
              // No fading in intro mode
              bFading = false;
              // Call finish and terminate current thread
              callFinish();
              return;
            }
          } else if (line.matches("ANS_LENGTH.*")) {
            StringTokenizer st = new StringTokenizer(line, "=");
            st.nextToken();
            lDuration = (long) (Float.parseFloat(st.nextToken()) * 1000);
          }
          // End of file
          else if (line.matches(".*\\x2e\\x2e\\x2e.*\\(.*\\).*")) {
            bEOF = true;
            // Update track rate
            fCurrent.getTrack().updateRate();
            
            // Launch next track
            try {
              // Do not launch next track if not opening: it means
              // that the file is in error (EOF comes
              // before any play) and the FIFO.finished() is processed by
              // Player on exception processing
              if (bOpening) {
                bOpening = false;
                break;
              }

              // If fading, ignore end of file
              if (!bFading) {
                // Call finish and terminate current thread
                callFinish();
                return;
              } else {
                // If fading, next track has already been launched
                bFading = false;
              }
            } catch (Exception e) {
              Log.error(e);
            }
            break;
          }
        }
        // can reach this point at the end of file
        in.close();
      } catch (IOException e) {
        Log.error(e);
      }
    }
  }

  @Override
  public void stop() throws Exception {
    // Call generic stop
    super.stop();

    // Update track rate
    fCurrent.getTrack().updateRate();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.File, float, long,
   *      float)
   */
  @Override
  public void play(org.jajuk.base.File file, float fPosition, long length, float fVolume)
      throws Exception {
    this.lTime = 0;
    this.fVolume = fVolume;
    this.length = length;
    this.fPosition = fPosition;
    this.bFading = false;
    this.bStop = false;
    this.fCurrent = file;
    this.bOpening = true;
    this.bEOF = false;
    this.iFadeDuration = 1000 * Conf.getInt(Const.CONF_FADE_DURATION);
    ProcessBuilder pb = new ProcessBuilder(buildCommand(file.getAbsolutePath()));
    Log.debug("Using this Mplayer command: {{" + pb.command() + "}}");
    // Set all environment variables format: var1=xxx var2=yyy
    try {
      Map<String, String> env = pb.environment();
      StringTokenizer st = new StringTokenizer(Conf.getString(Const.CONF_ENV_VARIABLES), " ");
      while (st.hasMoreTokens()) {
        StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
        env.put(st2.nextToken(), st2.nextToken());
      }
    } catch (Exception e) {
      Log.error(e);
    }
    // Start mplayer
    proc = pb.start();
    // start mplayer replies reader thread
    new ReaderThread("MPlayer reader thread").start();
    // start writer to mplayer thread
    new PositionThread("MPlayer writer thread").start();
    // if opening, wait
    long time = System.currentTimeMillis();
    // Try to open the file during 30 secs
    while (!bStop && bOpening && !bEOF
        && (System.currentTimeMillis() - time) < MPLAYER_START_TIMEOUT) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Log.error(e);
      }
    }

    // Check the file has been property opened
    if (!bOpening && !bEOF) {
      if (fPosition > 0.0f) {
        seek(fPosition);
      }
      // Get track length
      sendCommand("get_time_length");
    } else {
      // try to kill the mplayer process if still alive
      if (proc != null) {
        Log.warn("OOT Mplayer process, try to kill it");
        proc.destroy();
        Log.warn("OK, the process should have been killed now");
      }
      // Notify the problem opening the file
      throw new JajukException(7, Integer.valueOf(MPLAYER_START_TIMEOUT).toString() + " ms");
    }
  }

  /**
   * @return current position as a float ex: 0.2f
   */
  @Override
  public float getCurrentPosition() {
    if (lDuration == 0) {
      return 0;
    }
    return ((float) lTime) / lDuration;
  }

  /**
   * @return Returns the lTime in ms
   */
  @Override
  public long getElapsedTime() {
    return lTime;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#seek(float) Ogg vorbis seek not yet
   *      supported
   */
  @Override
  public void seek(float posValue) {
    // if fading, ignore
    if (bFading) {
      return;
    }
    // Make sure to reset pause. Indeed, mplayer has a bug that resume
    // playing after a volume change or a seek. We must make sure that
    // the jajuk state is coherent with the mplayer one
    if (Player.isPaused()) {
      try {
        ActionManager.getAction(JajukActions.PLAY_PAUSE_TRACK).perform(null);
      } catch (Exception e) {
        Log.error(e);
      }
    }
    // save current position
    String command = "seek " + (int) (100 * posValue) + " 1";
    sendCommand(command);
    setVolume(fVolume); // need this because a seek reset volume
  }

  /**
   * @return player state, -1 if player is null.
   */
  @Override
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
  @Override
  public long getCurrentLength() {
    return lDuration;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.AbstractMPlayerImpl#play(org.jajuk.base.WebRadio,
   *      float)
   */
  @Override
  public void play(WebRadio radio, float volume) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.IPlayerImpl#setVolume(float)
   */
  public void setVolume(float fVolume) {
    super.setVolume(fVolume);
    // Make sure to reset pause. Indeed, mplayer has a bug that resume
    // playing after a volume change or a seek. We must make sure that
    // the jajuk state is coherent with the mplayer one
    if (Player.isPaused()) {
      try {
        ActionManager.getAction(JajukActions.PLAY_PAUSE_TRACK).perform(null);
      } catch (Exception e) {
        Log.error(e);
      }
    }
  }

  /**
   * Force finishing (doesn't stop but only make a FIFO request to switch track)
   * <br>
   * We have to launch the next file from another thread to free the reader
   * thread. Otherwise, finish() calls launches() that call another finishes...
   */
  private void callFinish() {
    // avoid stopping current track (perceptible during
    // player.open() for remote files)
    new Thread("Call to finish") {
      @Override
      public void run() {
        FIFO.finished();
      }
    }.start();

  }
}