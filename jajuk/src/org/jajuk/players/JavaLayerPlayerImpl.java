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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.jajuk.base.FIFO;
import org.jajuk.base.IPlayerImpl;
import org.jajuk.base.ITechnicalStrings;
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
	private static AdvancedPlayer player;
	/** Started */
	private volatile boolean bStarted = false;
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#play()
	 */
	public void play(org.jajuk.base.File file,float fPosition,long length) throws Exception{
		try{
			bStarted = false;
			player = new AdvancedPlayer(new BufferedInputStream(new FileInputStream(new File(file.getAbsolutePath())))); 
			player.setPlayBackListener(new PlaybackListener() {
				public void playbackFinished(PlaybackEvent pbe){
					FIFO.getInstance().finished();
				}
				public void playbackStarted(PlaybackEvent pbe){
					bStarted = true;
					new Thread(){  //we have to set mute mode and volume for next track asynchronously because line is not already opened in this method and will not until it is executed. setMute et setVolume methods wait until line is opened
						public void run(){
							Util.setMute(Util.getMute());  //keep the mute mode between tracks	
							Util.setVolume(Util.getVolume()); //keep the volume between tracks
							Util.stopWaiting();
						}
					}.start();
				}
			});
			//FIFO.getInstance().lTrackStart = System.currentTimeMillis();  //time correction ( deleted because of play mode: don't overide old date when lauching again )
			Util.stopWaiting();
			if (fPosition < 0){  //-1 means we want to play entire file
				player.play();
			}
			else{
				int iFirstFrame = (int)(file.getTrack().getLength()*fPosition*41.666); // (position*fPosition(%))*1000(ms) /24 because 1 frame =24ms
				int iLastFrame = (int)(iFirstFrame+(length/24)); //length(ms)/24
				player.play(iFirstFrame,iLastFrame);
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
			player.close();
		}	
	}
	

}
