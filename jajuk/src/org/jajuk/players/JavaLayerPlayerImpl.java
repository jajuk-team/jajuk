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
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.players;

import java.io.File;
import java.io.FileInputStream;

import javazoom.jl.player.Player;

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

	Player player;
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#play()
	 */
	public void play(String sReference) throws Exception{
		player = new Player(new FileInputStream(new File(sReference))); //$NON-NLS-1$
		player.play();		
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.IPlayerImpl#stop()
	 */
	public void stop() {
		player.close();
	}
	
	public boolean isComplete(){
		return player.isComplete(); 
	}

}
