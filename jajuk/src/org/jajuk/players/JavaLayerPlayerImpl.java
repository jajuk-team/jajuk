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
 * $Log$
 * Revision 1.3  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 * Revision 1.2  2003/10/17 20:37:18  bflorat
 * 17/10/2003
 *
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.players;

import java.io.File;
import java.io.FileInputStream;

import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.IPlayerImpl;
import org.jajuk.i18n.Messages;
import org.jajuk.util.log.Log;

/**
 *  My class description
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class JavaLayerPlayerImpl implements IPlayerImpl{

	/**Current player*/
	private AdvancedPlayer player;
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#play()
	 */
	public void play(org.jajuk.base.File file) throws Exception{
		player = new AdvancedPlayer(new FileInputStream(new File(file.getAbsolutePath()))); //$NON-NLS-1$
		player.setPlayBackListener(new PlaybackListener() {
			public void playbackFinished(PlaybackEvent pbe){
				FIFO.finished();
			}
		});
		player.play();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#stop()
	 */
	public void stop() {
		player.close();
	}
	

}
