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
package org.jajuk.base;

import java.util.Properties;

import org.jajuk.players.IPlayerImpl;
import org.jajuk.ui.ObservationManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

/**
 *  abstract class for music player, independent from real implementation
 *
 * @author     Bertrand Florat
 * @created    12 oct. 2003
 */
public class Player implements ITechnicalStrings{

	/**Current file read*/
	private static File fCurrent;
	/**Current player used*/
	private static IPlayerImpl pCurrentPlayerImpl;
	/** Lock to ensure 2 players can be launched*/
	private static final byte[] bLock = new byte[0];
	/**Mute flag */
	private static boolean bMute = false;
	/**Paused flag*/
	private static boolean bPaused = false;
	/**Playing ?*/
	private static boolean bPlaying = false;
	
	
	/**
	 * Asynchronous play for specified file with specified time interval
	 * @param file to play
	 * @param position in % of the file length. ex 0.1 for 10%
	 * @param length in ms 
	 */
	public static synchronized void play(final File file,final float fPosition,final long length) {
		fCurrent = file;
		pCurrentPlayerImpl = file.getTrack().getType().getPlayerImpl();
		Thread thread = new Thread() {
			public void run() {
				try {
				    synchronized(bLock){  //ultimate concurrency protection
				        bPlaying = true;
				        if (bMute){
				            pCurrentPlayerImpl.play(fCurrent,fPosition,length,0.0f);
				        }
				        else{
				            pCurrentPlayerImpl.play(fCurrent,fPosition,length,ConfigurationManager.getFloat(CONF_VOLUME));
				        }
				    }
				} catch (Exception e) {
					Properties pDetails = new Properties();
					pDetails.put(DETAIL_CURRENT_FILE,file);
				    ObservationManager.notifySync(EVENT_PLAY_ERROR,pDetails); //notify the error 
					Log.error("007",fCurrent.getAbsolutePath(), e); //$NON-NLS-1$
					Player.stop();
					FIFO.getInstance().finished();
				}			
			}
		};
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
	
	/**
	 * Stop the played track
	 * @param type
	 */
	public static synchronized void stop() {
		try {
			if (fCurrent!=null){
			    fCurrent.getTrack().getType().getPlayerImpl().stop();
				setPaused(false); //cancel any current pause
				bPlaying = false;
			}
		} catch (Exception e) {
			Log.error("008",fCurrent.getName(),e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Alternative Mute/unmute the player
	 * @throws Exception
	 */
	public static void mute() {
		try {
			if (pCurrentPlayerImpl!=null){
				if (Player.bMute){ //already muted, unmute it by setting the volume previous mute
					pCurrentPlayerImpl.setVolume(ConfigurationManager.getFloat(CONF_VOLUME));
				}
				else{
				    pCurrentPlayerImpl.setVolume(0.01f);//do that to avoid strange sound 
				    pCurrentPlayerImpl.setVolume(0.0f);
				}
				Player.bMute = !Player.bMute;
				//notify UI
				ObservationManager.notify(EVENT_MUTE_STATE);
			}
		} catch (Exception e) {
			Log.error(e); 
		}
	}
	
	
	/**
	 * Mute/unmute the player
	 * @param bMute
	 * @throws Exception
	 */
	public static void mute(boolean bMute) {
		try {
			if (pCurrentPlayerImpl!=null){
				if (bMute){
				    pCurrentPlayerImpl.setVolume(0.0f);
				}
				else{
				    pCurrentPlayerImpl.setVolume(ConfigurationManager.getFloat(CONF_VOLUME));
				}
				Player.bMute = bMute;
			}
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
	 * @param fVolume : gain from 0 to 1
	 * @throws Exception
	 */
	public static void setVolume(float fVolume){
		try {
			ConfigurationManager.setProperty(CONF_VOLUME,Float.toString(fVolume));
			if (pCurrentPlayerImpl!=null){
				pCurrentPlayerImpl.setVolume(fVolume);
			}
		} catch (Exception e) {
			Log.error(e); 
		}	
	}
	
	
	/**
	 * @return Returns the lTime in ms
	 */
	public static long getElapsedTime() {
		if (pCurrentPlayerImpl != null){
		    return pCurrentPlayerImpl.getElapsedTime();
		}
		else{
		    return 0;
		}
	}
	
	/**Pause the player*/
	public static void pause() {
		 try {
		     if (!bPlaying){ //ignore pause when not playing to avoid confusion between two tracks
		         return;
		     }
		     if (pCurrentPlayerImpl != null){
		         pCurrentPlayerImpl.pause();
			}
		    bPaused = true;
		} catch (Exception e) {
			Log.error(e); 
		}
	}
	
	/**resume the player*/
	public static void resume(){
		try {
		    if (pCurrentPlayerImpl != null){
		        pCurrentPlayerImpl.resume();
			}
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
	 * Force the bPaused state to allow to cancel a pause without restarting the current played track (rew for exemple)  
	 * @param bPaused
	 */
	public static void setPaused(boolean bPaused){
	    Player.bPaused = bPaused;
	}
	
	/**Seek to a given position in %. ex : 0.2 for 20% */
	public static void seek(float fPosition){
	    //check if we are yet seeking
	    if (pCurrentPlayerImpl !=null && !pCurrentPlayerImpl.isSeeking()){
	        // bound seek
	        if (fPosition < 0.0f){
	            fPosition = 0.0f;
	        }
	        else if (fPosition >= 1.0f){
	            fPosition = 0.99f;
	        }
	        try{
	            pCurrentPlayerImpl.seek(fPosition);
	        }
	        catch(Exception e){ //we can get some errors in unexpected cases
	            Log.debug(e.toString());
	        }
	    }
	}
	
	/**
	 * @return position in track in %
	 */
	public static float getCurrentPosition(){
	    if (pCurrentPlayerImpl != null){
	        return pCurrentPlayerImpl.getCurrentPosition();
		}
	    else{
	        return 0.0f;
	    }
	
	    
	}
    /**
     * @return Returns the bPlaying.
     */
    public static boolean isPlaying() {
        return bPlaying;
    }
}
