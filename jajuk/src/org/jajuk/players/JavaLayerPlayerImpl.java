/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.base.TypeManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  My class description
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class JavaLayerPlayerImpl implements IPlayerImpl,ITechnicalStrings{
	
	/**Current player*/
	public static BasicPlayer player;
	/**Started */
	private volatile boolean bStarted = false;
	/**Time elapsed in secs*/
	private long lTime = 0;
	/**Date of last elapsed time update*/
	private long lDateLastUpdate = System.currentTimeMillis(); 
	/**current track info*/
	private Map mInfo;
	/**Current position in %*/
	float fPos;
	/**Length to be played in secs*/
	long length;
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#play()
	 */
	public void play(org.jajuk.base.File file,float fPosition,final long length,final boolean bMuted,final float fVolume) throws Exception{
		try{
			this.length = length;
			bStarted = false;
			player = new BasicPlayer();
			player.addBasicPlayerListener( new BasicPlayerListener() {
				public void opened(Object arg0, Map arg1) {
					mInfo = arg1;
				}
				public void display(String msg){		
				}
				/**
				 * Called several times by sec
				 */
				public void progress(int iBytesread,long lMicroseconds,byte[] bPcmdata,java.util.Map mProperties) {
					if ( (System.currentTimeMillis()-lDateLastUpdate) > 900 ){ //update every 900 ms
						//test end of length
						if ( length != -1 && lMicroseconds/1000 > length){ //length=-1 means there is no max length
							try {
								player.stop();
								FIFO.getInstance().finished();
							} catch (BasicPlayerException e) {
								Log.error(e);
							}
						}
						//computes read time
						if (mInfo.containsKey("audio.length.bytes")) { //$NON-NLS-1$
							int byteslength = ((Integer) mInfo.get("audio.length.bytes")).intValue(); //$NON-NLS-1$
							fPos = (float)iBytesread / byteslength;
							lTime = (long)(Util.getTimeLengthEstimation(mInfo)*fPos);
						}
						lDateLastUpdate = System.currentTimeMillis();
					}
				}
				
				public void stateUpdated(BasicPlayerEvent bpe) {
					switch(bpe.getCode()){
					case BasicPlayerEvent.EOM:
						FIFO.getInstance().finished();
						break;
					case BasicPlayerEvent.PLAYING:
						bStarted = true;
						new Thread(){  //we have to set mute mode and volume for next track asynchronously because line is not already opened in this method and will not until it is executed. setMute et setVolume methods wait until line is opened
							public void run(){
								try{
									if ( bMuted){
										mute();
									}
									else {
										setVolume(fVolume);
									}
								} catch (Exception e) {
									Log.error(e);
								}
								Util.stopWaiting(); //stop the waiting cursor
							}
						}.start();
						break;
					}
				}
				
				public void setController(BasicController arg0) {
				}
			});
			player.open(new File(file.getAbsolutePath())); 
			
			Util.stopWaiting();
			if (fPosition < 0){  //-1 means we want to play entire file
				player.play();
			}
			else{
				int iFirstFrame = (int)(file.getTrack().getLength()*fPosition*41.666); // (position*fPosition(%))*1000(ms) /24 because 1 frame =24ms
				int iLastFrame = (int)(iFirstFrame+(length/24)); //length(ms)/24
				//test if this is a audio format supporting seeking
				if (Boolean.valueOf(TypeManager.getTypeByExtension(Util.getExtension(file.getIO())).getProperty(TYPE_PROPERTY_SEEK_SUPPORTED)).booleanValue()){
					seek(fPosition);
				}
				player.play();
			}
		}
		catch(Exception e){  //in case of error, we must stop waiting cursor and set started to true to avoid deadlock in the stop method
			bStarted = true;
			Util.stopWaiting();
			throw e;  //propagate to Player
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#stop()
	 */
	public void stop() {
		while (!bStarted){  //To avoid strange behaviors in the fifo, we have to wait the track is really started before stoping it 
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Log.error(e);
			}
		}
		if (player!= null ){
			try {
				player.stop();
			} catch (BasicPlayerException e) {
				Log.error(e);
			}
		}	
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#mute()
	 */
	public void mute() throws Exception {
		player.setGain(0.0f);
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#setVolume(float)
	 */
	public void setVolume(float fVolume) throws Exception {
		player.setGain(fVolume);
	}
	
	
	/**
	 * @return Returns the lTime in ms
	 */
	public long getElapsedTime() {
		return lTime;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.players.IPlayerImpl#pause()
	 */
	public void pause() throws Exception{
		player.pause();
	}
	
	public void resume() throws Exception{
		player.resume();
	}	
	
	/* (non-Javadoc)
	 * @see org.jajuk.players.IPlayerImpl#seek(float)
	 * Ogg vorbis seek not yet supported
	 */
	public  void seek(float posValue) {
		try{	
			if (mInfo.containsKey("audio.type")) { //$NON-NLS-1$
				String type = (String)mInfo.get("audio.type"); //$NON-NLS-1$
				// Seek support for MP3. and WAVE
				if ((type.equalsIgnoreCase("mp3") || type.equalsIgnoreCase("wave"))&& mInfo.containsKey("audio.length.bytes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					long skipBytes =	(long) Math.round(((Integer)mInfo.get("audio.length.bytes")).intValue()* posValue); //$NON-NLS-1$
					player.seek(skipBytes);
				}
				else{
					Messages.showErrorMessage("126"); //$NON-NLS-1$
				}
			}
		}
		catch(BasicPlayerException bpe){
			Log.error(bpe);
		}
	}

	
	public float getCurrentPosition(){
		return fPos;
	}
	
	
}
