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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.jajuk.base.Track;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Jajuk player implementation based on Mplayer.
 */
public class MPlayerPlayerImpl extends AbstractMPlayerImpl {
    /** Time elapsed in ms. */
    private long lTime = 0;
    /** Actually played time */
    private long actuallyPlayedTimeMillis = 0l;
    private long lastPlayTimeUpdate = System.currentTimeMillis();
    /** Length to be played in secs. */
    private long length;
    /** Starting position. */
    private float fPosition;
    /** Current track estimated total duration in ms. */
    private long lDuration;
    /** Volume when starting fade. */
    private float fadingVolume;
    /** Cross fade duration in ms. */
    int iFadeDuration = 0;
    /** Time track started *. */
    private long dateStart;
    /** Pause time correction *. */
    private long pauseCount = 0;
    private long pauseCountStamp = -1;
    /** Is the play is in error. */
    private boolean bInError = false;
    /** Progress step in ms, do not set less than 300 or 400 to avoid using too much CPU. */
    private static final int PROGRESS_STEP = 500;
    /** Total play time is refreshed every TOTAL_PLAYTIME_UPDATE_INTERVAL times. */
    private static final int TOTAL_PLAYTIME_UPDATE_INTERVAL = 2;
    /** Current file. */
    private org.jajuk.base.File fCurrent;
    /** [Windows only] Force use of shortnames. */
    private boolean bForcedShortnames = false;
    /** English-specific end of file pattern */
    private Pattern patternEndOfFileEnglish = Pattern
            .compile("Exiting\\x2e\\x2e\\x2e.*\\(End of file\\)");
    /** Language-agnostic end of file pattern */
    private Pattern patternEndOfFileGeneric = Pattern.compile(".*\\x2e\\x2e\\x2e.*\\(.*\\)");

    /**
     * Position and elapsed time getter.
     */
    private class PositionThread extends Thread {
        /**
         * Instantiates a new position thread.
         *
         * @param name
         */
        public PositionThread(String name) {
            super(name);
            setDaemon(true);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            int comp = 0;
            lastPlayTimeUpdate = System.currentTimeMillis();
            Track current = fCurrent.getTrack();
            while (!bStop && !bEOF) { // stop this thread
                try {
                    // store elapsed time while the track is paused
                    if (pauseCountStamp > 0) {
                        pauseCount += (System.currentTimeMillis() - pauseCountStamp);
                        pauseCountStamp = -1;
                    }
                    if (bPaused) {
                        pauseCountStamp = System.currentTimeMillis();
                    }
                    if (!bPaused) {
                        // Get current time. Due to https://trac.mplayerhq.hu/ticket/2378, we use a
                        // the less precised % command
                        // for flac files
                        sendCommand("get_time_pos");
                        // Get track length if required. Do not launch "get_time_length" only
                        // once because some fast computer makes mplayer start too fast and
                        // the slave mode is not yet opened so this command is not token into
                        // account.
                        // See bug #1816 (Track length is zero after a restart)
                        if (lDuration == 0) {
                            sendCommand("get_time_length");
                        }
                        // Every 2 time units, increase actual play time. We wait this
                        // delay for perfs and for precision
                        if (comp > 0 && comp % TOTAL_PLAYTIME_UPDATE_INTERVAL == 0) {
                            // Increase actual play time
                            // End of file: increase actual play time to the track
                            // Perf note : this full action takes less much than 1 ms
                            long trackPlaytime = current
                                    .getLongValue(Const.XML_TRACK_TOTAL_PLAYTIME);
                            long newValue = (PROGRESS_STEP * TOTAL_PLAYTIME_UPDATE_INTERVAL / 1000)
                                    + trackPlaytime;
                            current.setProperty(Const.XML_TRACK_TOTAL_PLAYTIME, newValue);
                        }
                    }
                    comp++;
                    Thread.sleep(PROGRESS_STEP);
                } catch (Exception e) {
                    Log.error(e);
                }
            }
        }

        
    }

    /**
     * Reader : read information from mplayer like position.
     */
    private class ReaderThread extends Thread {
        /**
         * Instantiates a new reader thread.
         *
         * @param name
         */
        public ReaderThread(String name) {
            super(name);
            setDaemon(true);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(proc.getInputStream()));
                // While we don't know the mplayer language, patternEndOfFile matches any language
                // end of file pattern : .*... (.*)
                Pattern patternEndOfFile = patternEndOfFileGeneric;
                try {
                    String line = null;
                    while (!bStop) {
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
                        // Very verbose :
                        // Log.debug("Output from MPlayer: " + line);
                        // Detect mplayer language
                        if (line.indexOf("Starting playback") != -1) {
                            patternEndOfFile = patternEndOfFileEnglish;
                        } else if (line.indexOf("ANS_TIME_POSITION") != -1) {
                            // Stream is actually opened now
                            bOpening = false;
                            StringTokenizer st = new StringTokenizer(line, "=");
                            st.nextToken();
                            String value = st.nextToken();
                            try {
                                lTime = (int) (Float.parseFloat(value) * 1000);
                            } catch (NumberFormatException nfe) {
                                Log.error(nfe);
                                lTime = 0l;
                            }
                            pauseCount = 0;
                            pauseCountStamp = -1;
                            // update actually played duration
                            if (lastPlayTimeUpdate > 0 && !bPaused) {
                                actuallyPlayedTimeMillis += (System.currentTimeMillis()
                                        - lastPlayTimeUpdate);
                            }
                            lastPlayTimeUpdate = System.currentTimeMillis();
                            // Store current position for use at next startup
                            UtilFeatures.storePersistedPlayingPosition(getCurrentPosition());
                            // Cross-Fade test
                            if (!bFading && iFadeDuration > 0
                            // Length = 0 for some buggy audio headers
                                    && lDuration > 0
                                    // Does fading time happened ?
                                    && lTime > (lDuration - iFadeDuration)
                                    // Do not fade if the track is very short
                                    && (lDuration > 3 * iFadeDuration)
                                    // Do not fade if bit perfect mode
                                    && !Conf.getBoolean(CONF_BIT_PERFECT)) {
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
                        } else if (line.indexOf("ANS_LENGTH") != -1) {
                            /*
                             * To compute the current track length (used by the information panel to
                             * display remaining time and position), we use the tag duration first
                             * and the mplayer duration then if the tag duration looks wrong
                             * (example : wrongly tagged file or format that doesn't support tags
                             * like wav). Indeed, mplayer duration is sometimes wrong for VBR MP3.
                             */
                            StringTokenizer st = new StringTokenizer(line, "=");
                            st.nextToken();
                            long mplayerDuration = 0l;
                            try {
                                mplayerDuration = (long) (Float.parseFloat(st.nextToken()) * 1000);
                            } catch (NumberFormatException nfe) {
                                Log.error(nfe);
                            }
                            long tagDuration = fCurrent.getTrack().getDuration() * 1000;
                            if (tagDuration <= 0) {
                                lDuration = mplayerDuration;
                            } else {
                                lDuration = tagDuration;
                            }
                        }
                        // End of file
                        else if (patternEndOfFile.matcher(line).matches()) {
                            bEOF = true;
                            // Update track rate if it has been opened
                            if (!bOpening) {
                                fCurrent.getTrack().updateRate();
                                // Force immediate rating refresh (without using the rating manager)
                                ObservationManager.notify(new JajukEvent(JajukEvents.RATE_CHANGED));
                            }
                            // Launch next track
                            try {
                                // Do not launch next track if not opening: it means
                                // that the file is in error (EOF comes
                                // before any play) and the FIFO.finished() is processed by
                                // Player on exception processing
                                if (bOpening) {
                                    bOpening = false;
                                    bInError = true;
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
                } finally {
                    // can reach this point at the end of file
                    in.close();
                }
            } catch (IOException e) {
                Log.error(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.players.AbstractMPlayerImpl#stop()
     */
    @Override
    public void stop() throws Exception {
        // Call generic stop
        super.stop();
        // Update track rate
        fCurrent.getTrack().updateRate();
        // Force immediate rating refresh (without using the rating manager)
        ObservationManager.notify(new JajukEvent(JajukEvents.RATE_CHANGED));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.File, float, long, float)
     */
    @Override
    public void play(org.jajuk.base.File file, float fPosition, long length, float fVolume)
            throws IOException, JajukException {
        this.fVolume = fVolume;
        this.length = length;
        this.fPosition = fPosition;
        this.fCurrent = file;
        this.bitPerfect = Conf.getBoolean(Const.CONF_BIT_PERFECT);
        // Reset all states
        reset();
        // Try to launch mplayer
        int startPos = (int) (fPosition * file.getTrack().getDuration());
        launchMplayer(startPos);
        // If under windows and the launch failed, try once again
        // with other short names configuration (see #1267)
        if (bInError && UtilSystem.isUnderWindows()) {
            bForcedShortnames = true;
            Log.warn("Force shortname filename scheme" + " for : " + file.getAbsolutePath());
            // Reset any state changed by the previous reader thread
            reset();
            launchMplayer(startPos);
            // Disable forced shortnames because the shortnames converter takes a while (2 secs)
            bForcedShortnames = false;
        }
        // Check the file has been property opened
        if (bOpening || bEOF) {
            // try to kill the mplayer process if still alive
            if (proc != null) {
                Log.warn("OOT Mplayer process, try to kill it (Opening: " + bOpening + ", EOF: "
                        + bEOF + ")");
                proc.destroy();
                Log.warn("OK, the process should have been killed now");
            }
            // Notify the problem opening the file
            throw new JajukException(7, Integer.valueOf(MPLAYER_START_TIMEOUT).toString() + " ms");
        }
    }

    /**
     * Reset the player impl to initial state.
     */
    private void reset() {
        this.lTime = 0;
        this.bFading = false;
        this.bInError = false;
        this.bStop = false;
        this.bOpening = true;
        this.bEOF = false;
        this.iFadeDuration = 1000 * Conf.getInt(Const.CONF_FADE_DURATION);
        this.dateStart = System.currentTimeMillis();
        this.pauseCount = 0;
        this.pauseCountStamp = -1;
    }

    /**
     * Launch mplayer.
     * 
     * @param startPositionSec the position in the track when starting in secs (0 means we plat from
     *                         the begining)
     * @throws IOException Signals that an I/O exception has occurred.
     * 
     */
    private void launchMplayer(int startPositionSec) throws IOException {
        // Build the file url. Under windows, we convert path to short
        // version to fix a mplayer bug when reading some pathnames including
        // special characters (see #1267)
        String pathname = fCurrent.getAbsolutePath();
        if (UtilSystem.isUnderWindows() && bForcedShortnames) {
            pathname = UtilSystem.getShortPathNameW(pathname);
        }
        ProcessBuilder pb = new ProcessBuilder(buildCommand(pathname, startPositionSec));
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
        // Try to open the file during several secs
        while (UtilSystem.isRunning(proc) && !bStop && bOpening && !bEOF
                && (System.currentTimeMillis() - time) < MPLAYER_START_TIMEOUT) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.players.IPlayerImpl#getCurrentPosition()
     */
    @Override
    public float getCurrentPosition() {
        if (lDuration == 0) {
            return 0;
        }
        return ((float) lTime) / lDuration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.players.IPlayerImpl#getElapsedTimeMillis()
     */
    @Override
    public long getElapsedTimeMillis() {
        return lTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.services.players.IPlayerImpl#getActuallyPlayedTimeMillis()
     */
    @Override
    public long getActuallyPlayedTimeMillis() {
        return actuallyPlayedTimeMillis;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.players.IPlayerImpl#seek(float) Ogg vorbis seek not yet supported
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
                ActionManager.getAction(JajukActions.PAUSE_RESUME_TRACK).perform(null);
            } catch (Exception e) {
                Log.error(e);
            }
        }
        // save current position
        String command = "seek " + (int) (100 * posValue) + " 1";
        sendCommand(command);
        if (!Conf.getBoolean(CONF_BIT_PERFECT)) {
            setVolume(fVolume); // need this because a seek reset volume
        }
    }

    /**
     * Gets the state.
     *
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
     * @see org.jajuk.players.IPlayerImpl#getDurationSec()
     */
    @Override
    public long getDurationSec() {
        return lDuration;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.players.AbstractMPlayerImpl#play(org.jajuk.base.WebRadio, float)
     */
    @Override
    public void play(WebRadio radio, float volume) {
        // nothing to do here...
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.base.IPlayerImpl#setVolume(float)
     */
    @Override
    public void setVolume(float fVolume) {
        if (!bPaused) {
            super.setVolume(fVolume);
        } else {
            this.fVolume = fVolume;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.players.AbstractMPlayerImpl#resume()
     */
    @Override
    public void resume() throws Exception {
        lastPlayTimeUpdate = System.currentTimeMillis();
        super.resume();
        if (!Conf.getBoolean(CONF_BIT_PERFECT)) {
            setVolume(fVolume);
        }
    }

    /**
     * Force finishing (doesn't stop but only make a FIFO request to switch track) <br>
     * We have to launch the next file from another thread to free the reader thread. Otherwise,
     * finish() calls launches() that call another finishes...
     */
    private void callFinish() {
        // avoid stopping current track (perceptible during
        // player.open() for remote files)
        new Thread("Call to finish") {
            @Override
            public void run() {
                QueueModel.finished();
            }
        }.start();
    }
}
