/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

import java.io.File;
import java.util.Map;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import org.jajuk.base.FIFO;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;


/**
 * Jajuk player implementation based on javazoom BasicPlayer
 * 
 * @author Bertrand Florat
 * @created 12 oct. 2003
 */
public class JavaLayerPlayerImpl implements IPlayerImpl, ITechnicalStrings, BasicPlayerListener {

    /** Current player */
    private BasicPlayer player;

    /** Time elapsed in secs */
    private long lTime = 0;

    /** Date of last elapsed time update */
    private long lDateLastUpdate = System.currentTimeMillis();

    /** current track info */
    private Map mPlayingData;

    /** Current position in % */
    private float fPos;

    /** Length to be played in secs */
    private long length;

    /** Stored Volume */
    private float fVolume;

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.File, float, long, float)
     */
    public synchronized void play(org.jajuk.base.File file, float fPosition, long length,
        float fVolume) throws Exception {
        this.fVolume = fVolume;
        this.length = length;
        // instanciate player is needed
       if (player == null ) {
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
        if (fPosition > 0.0f) {
            // if we don't start at the begining of file, seek to this point
            int iFirstFrame = (int) (file.getTrack().getLength() * fPosition * 41.666);
            // (position*fPosition(%))*1000(ms) /24 because 1 frame =24ms
            // test if this is a audio format supporting seeking
            if (Boolean.valueOf(
                    TypeManager.getTypeByExtension(Util.getExtension(file.getIO())).getProperty(
                            TYPE_PROPERTY_SEEK_SUPPORTED)).booleanValue()) {
                seek(fPosition);
            }
        }
        player.play();
        player.setGain(fVolume);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.base.IPlayerImpl#stop()
     */
    public synchronized void stop() throws Exception {
        player.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.base.IPlayerImpl#setVolume(float)
     */
    public synchronized void setVolume(float fVolume) throws Exception {
        this.fVolume = fVolume;
        player.setGain(fVolume);
    }

    /**
     * @return current position as a float ex: 0.2f
     */
    public float getCurrentPosition() {
        return fPos;
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
    public synchronized void pause() throws Exception {
        player.pause();
    }

    public synchronized void resume() throws Exception {
        player.resume();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.players.IPlayerImpl#seek(float) Ogg vorbis seek not yet supported
     */
    public void seek(float posValue) {
        //Do not seek to a position too near from the end : it can cause freeze. MAX=98%
        if (posValue>0.98f){
            posValue = 0.98f;
        }
        // leave if already seeking
        if (player != null && getState() == BasicPlayer.SEEKING) {
            Log.debug("Already seeking, leaving"); //$NON-NLS-1$
            return;
        }
        if (mPlayingData.containsKey("audio.type") && player != null) { //$NON-NLS-1$
            Type type = TypeManager.getTypeByTechDesc((String) mPlayingData.get("audio.type")); //$NON-NLS-1$
            // Seek support for MP3. and WAVE
            if (Boolean.valueOf(type.getProperty(TYPE_PROPERTY_SEEK_SUPPORTED)).booleanValue()
                    && mPlayingData.containsKey("audio.length.bytes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                int iAudioLength = ((Integer) mPlayingData.get("audio.length.bytes")).intValue(); //$NON-NLS-1$
                long skipBytes = (long) Math.round(iAudioLength * posValue); //$NON-NLS-1$
                try {
                    player.seek(skipBytes);
                } catch (BasicPlayerException e) {
                    Log.error(e);
                }
            } else {
                Messages.showErrorMessage("126"); //$NON-NLS-1$
            }
        }
    }

    /**
     * @return player state, -1 if player is null.
     */
    public int getState() {
        if (player != null) {
            return player.getStatus();
        } else {
            return -1;
        }
    }

    /**
     * Opened listener implementation
     */
    public void opened(Object arg0, Map arg1) {
        this.mPlayingData = arg1;
    }

    /**
     * Progress listener implementation. Called several times by sec
     */
    public void progress(int iBytesread, long lMicroseconds, byte[] bPcmdata,
            java.util.Map mProperties) {
        if ((System.currentTimeMillis() - lDateLastUpdate) > 900) { 
            //  update every 900 ms
            lDateLastUpdate = System.currentTimeMillis();
            // computes read time
            if (mPlayingData.containsKey("audio.length.bytes")) { //$NON-NLS-1$
                int byteslength = ((Integer) mPlayingData.get("audio.length.bytes")).intValue(); //$NON-NLS-1$
                fPos = (byteslength != 0) ? (float) iBytesread / (float) byteslength : 0;
                ConfigurationManager.setProperty(CONF_STARTUP_LAST_POSITION, Float.toString(fPos));
                lTime = (long) (Util.getTimeLengthEstimation(mPlayingData) * fPos);
            }
            // test end of length
            if (length != TO_THE_END && lMicroseconds / 1000 > length) {
                // length=-1 means there is no max length
                try {
                    player.stop();
                    FIFO.getInstance().finished();
                } catch (BasicPlayerException e) {
                    Log.error(e);
                }
            }
        }
    }

    /**
     * State updated implementation
     */
    public void stateUpdated(BasicPlayerEvent bpe) {
        Log.debug("Player state changed: " + bpe); //$NON-NLS-1$
        switch (bpe.getCode()) {
        case BasicPlayerEvent.EOM:
            FIFO.getInstance().finished();
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

}
