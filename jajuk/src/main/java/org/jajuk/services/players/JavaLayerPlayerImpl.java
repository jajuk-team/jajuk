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
package org.jajuk.services.players;

import java.io.File;
import java.util.Map;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.services.core.RatingManager;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Jajuk player implementation based on javazoom BasicPlayer
 */
public class JavaLayerPlayerImpl implements IPlayerImpl, ITechnicalStrings, BasicPlayerListener {

  /** Current player */
  private BasicPlayer player;

  /** Time elapsed in ms */
  private long lTime = 0;

  /** Date of last elapsed time update */
  private long lDateLastUpdate = System.currentTimeMillis();

  /** current track info */
  private Map<String,Object> mPlayingData;

  /** Current position in % */
  private float fPos;

  /** Length to be played in secs */
  private long length;

  /** Stored Volume */
  private float fVolume;

  /** Current track estimated duration in ms */
  private long lDuration;

  /** Cross fade duration in ms */
  int iFadeDuration = 0;

  /** Fading state */
  boolean bFading = false;

  /** Progress step in ms */
  private static final int PROGRESS_STEP = 300;// need a fast refresh,

  // especially for fading

  /** Volume when starting fade */
  private float fadingVolume;

  /** current file */
  private org.jajuk.base.File fCurrent;

  /** Inc rating flag */
  private boolean bHasBeenRated = false;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.File, float, long,
   *      float)
   */
  public void play(org.jajuk.base.File file, float fPosition, long length, float fVolume)
      throws Exception {
    this.fPos = 0;
    this.lTime = 0;
    this.mPlayingData = null;
    this.fVolume = fVolume;
    this.length = length;
    this.bFading = false;
    this.fCurrent = file;
    this.bHasBeenRated = false;
    // Instantiate player is needed
    if (player == null) {
      BasicPlayer.EXTERNAL_BUFFER_SIZE = ConfigurationManager.getInt(CONF_BUFFER_SIZE);
      player = new BasicPlayer();
      player.setLineBufferSize(ConfigurationManager.getInt(CONF_AUDIO_BUFFER_SIZE));
      player.addBasicPlayerListener(this); // set listener
    }
    // make sure to stop any current player
    if (player.getStatus() != BasicPlayer.STOPPED) {
      player.stop();
    }
    player.open(new File(file.getAbsolutePath()));
    if ((fPosition > 0.0f) && 
      // (position*fPosition(%))*1000(ms) /24 because 1 frame =24ms
      // test if this is a audio format supporting seeking
      // Note: fio.getName() is better here as it will do less and not create
      // java.io.File in File
      (TypeManager.getInstance().getTypeByExtension(UtilSystem.getExtension(file.getName()))
          .getBooleanValue(XML_TYPE_SEEK_SUPPORTED))) {
      seek(fPosition);
    }
    player.play();
    setVolume(fVolume);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.IPlayerImpl#stop()
   */
  public void stop() throws Exception {
    bFading = false;
    if (player != null) {
      player.stop();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.IPlayerImpl#setVolume(float)
   */
  public void setVolume(float fVolume) throws Exception {
    this.fVolume = fVolume;
    player.setGain(fVolume * 0.6);
    // limit gain to avoid saturation
  }

  /**
   * @return current position as a float ex: 0.2f
   */
  public float getCurrentPosition() {
    return fPos;
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
    player.pause();
  }

  public void resume() throws Exception {
    player.resume();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#seek(float) Ogg vorbis seek not yet
   *      supported
   */
  public void seek(float pPosValue) {
    float posValue = pPosValue;
    // if fading, ignore
    if (bFading) {
      return;
    }
    // Do not seek to a position too near from the end : it can cause
    // freeze. MAX=98%
    if (posValue > 0.98f) {
      posValue = 0.98f;
    }
    // leave if already seeking
    if (player != null && getState() == BasicPlayer.SEEKING) {
      Log.warn("Already seeking, leaving");
      return;
    }
    if (mPlayingData.containsKey("audio.type") && player != null) {
      Type type = TypeManager.getInstance().getTypeByExtension(
          (String) mPlayingData.get("audio.type"));
      // Seek support for MP3. and WAVE
      if (type != null && type.getBooleanValue(XML_TYPE_SEEK_SUPPORTED)
          && mPlayingData.containsKey("audio.length.bytes")) {
        int iAudioLength = ((Integer) mPlayingData.get("audio.length.bytes")).intValue();
        long skipBytes = Math.round(iAudioLength * posValue);
        try {
          player.seek(skipBytes);
          setVolume(fVolume); // need this because a seek reset volume
        } catch (Exception e) {
          Log.error(e);
          return;
        }
      } else {
        Messages.showErrorMessage(126);
        return;
      }
    }
  }

  /**
   * @return player state, -1 if player is null.
   */
  public int getState() {
    if (bFading) {
      return FADING_STATUS;
    } else if (player != null) {
      return player.getStatus();
    } else {
      return -1;
    }
  }

  /**
   * Opened listener implementation
   */
  @SuppressWarnings("unchecked")
  public void opened(Object arg0, Map arg1) {
    this.mPlayingData = arg1;
    this.lDuration = UtilFeatures.getTimeLengthEstimation(mPlayingData);
  }

  /**
   * Progress listener implementation. Called several times by sec
   */
  @SuppressWarnings("unchecked")
  public void progress(int iBytesread, long lMicroseconds, byte[] bPcmdata,
      Map mProperties) {
    if ((System.currentTimeMillis() - lDateLastUpdate) > PROGRESS_STEP) {
      lDateLastUpdate = System.currentTimeMillis();
      this.iFadeDuration = 1000 * ConfigurationManager.getInt(CONF_FADE_DURATION);
      if (bFading) {
        // computes the volume we have to sub to reach zero at last
        // progress()
        float fVolumeStep = fadingVolume * ((float) 500 / iFadeDuration);
        // divide step by two to make fade softer
        float fNewVolume = fVolume - (fVolumeStep / 2);
        // decrease volume by n% of initial volume
        if (fNewVolume < 0) {
          fNewVolume = 0;
        }
        try {
          setVolume(fNewVolume);
        } catch (Exception e) {
          Log.error(e);
        }
        return;
      }
      // computes read time
      if (mPlayingData.containsKey("audio.length.bytes")) {
        int byteslength = ((Integer) mPlayingData.get("audio.length.bytes")).intValue();
        fPos = (byteslength != 0) ? (float) iBytesread / (float) byteslength : 0;
        ConfigurationManager.setProperty(CONF_STARTUP_LAST_POSITION, Float.toString(fPos));
        lTime = (long) (lDuration * fPos);
      }
      // check if the track get rate increasing level (INC_RATE_TIME
      // secs or intro length)
      if (!bHasBeenRated
          && (lTime >= INC_RATE_TIME * 1000 || (length != TO_THE_END && lTime > length))) {
        // inc rate by 1 if file is played at least INC_RATE_TIME secs
        fCurrent.getTrack().setRate(fCurrent.getTrack().getRate() + 1);
        // Alert rating manager that something changed
        RatingManager.setRateHasChanged(true);
        bHasBeenRated = true;
      }
      // Cross-Fade test
      if (iFadeDuration > 0 && lTime > (lDuration - iFadeDuration)) {
        // if memory is low, we force full gc to avoid blanck during
        // fade
        if (UtilSystem.needFullFC()) {
          Log.debug("Need full gc, no cross fade");
        }
        // Ugly hack to disable cross fade for APE files due to an
        // entagged issue, ape length is 0 so tracks start fading
        // when starting
        else if (fCurrent.getType().equals(TypeManager.getInstance().getTypeByExtension("ape"))) {
          Log.debug("APE file, no cross fade");
        } else {
          bFading = true;
          this.fadingVolume = fVolume;
          // we have to launch the next file from another thread to
          // avoid stopping current track (perceptible during
          // player.open() for remote files)
          new Thread() {
            @Override
            public void run() {
              FIFO.getInstance().finished();
            }
          }.start();
        }
      }
      // Caution: lMicroseconds reset to zero after a seek
      // test end of length for intro mode
      else if (length != TO_THE_END && lTime > length) {
        // length=-1 means there is no max length
        new Thread() {
          @Override
          public void run() {
            FIFO.getInstance().finished();
          }
        }.start();
      }
    }
  }

  /**
   * State updated implementation
   */
  public void stateUpdated(BasicPlayerEvent bpe) {
    if (bpe.getCode() != 10) { // do not trace volume changes
      Log.debug("Player state changed: " + bpe);
    }
    switch (bpe.getCode()) {
    case BasicPlayerEvent.EOM:
      // inc rate by 1 if file is fully played
      fCurrent.getTrack().setRate(fCurrent.getTrack().getRate() + 1);
      // Alert rating manager that something changed
      RatingManager.setRateHasChanged(true);
      if (!bFading) { // if using crossfade, ignore end of file
        System.gc();// Benefit from end of file to perform a full gc
        FIFO.getInstance().finished();
      }
      bFading = false;
      break;
    case BasicPlayerEvent.STOPPED:
      break;
    case BasicPlayerEvent.PLAYING:
      break;
    }
  }

  /**
   * Set controler implementation
   */
  public void setController(BasicController arg0) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#getCurrentLength()
   */
  public long getCurrentLength() {
    return lDuration;
  }

  public int scrobble() {
    return 1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.WebRadio, float)
   */
  public void play(WebRadio radio, float fVolume) throws Exception {
    // not needed right now
  }
}
