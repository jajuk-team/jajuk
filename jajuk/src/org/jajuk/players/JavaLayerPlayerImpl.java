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
import java.io.FileInputStream;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.jajuk.base.FIFO;
import org.jajuk.base.IPlayerImpl;
import org.jajuk.base.ITechnicalStrings;

/**
 *  My class description
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class JavaLayerPlayerImpl implements IPlayerImpl,ITechnicalStrings{

	/**Current player*/
	private AdvancedPlayer player;
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#play()
	 */
	public void play(org.jajuk.base.File file,int iPosition,int iLength) throws Exception{
		player = new AdvancedPlayer(new FileInputStream(new File(file.getAbsolutePath()))); //$NON-NLS-1$
		player.setPlayBackListener(new PlaybackListener() {
			public void playbackFinished(PlaybackEvent pbe){
				FIFO.getInstance().finished();
			}
		});
		if (iPosition < 0){
			player.play();
		}
		else{
			int iFirstFrame = (int)(file.getTrack().getLength()*iPosition*0.41666); // position/100 (%) /1000 (ms) *24 because 1 frame =24ms
			int iLastFrame = (int)(iFirstFrame+(iLength*41.6666));
			player.play(iFirstFrame,iLastFrame);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#stop()
	 */
	public void stop() {
		player.close();
	}
	

}
