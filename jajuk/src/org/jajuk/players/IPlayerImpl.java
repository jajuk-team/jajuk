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

import org.jajuk.base.File;

/**
 *  Minimum methods required for all Player implementations
 *
 * @author     Bertrand Florat
 * @created    12 oct. 2003
 */
public interface IPlayerImpl {
		
	/**
	 * Launches player
	 * @param file : jajuk file to be played
	 * @param fPosition position in % of the file
	 * @param length length to play in ms or TO_THE_END of you want to play to the end of the current file
	 * @param bMuted mute state
	 * @param fVolume volume
	 * @throws Exception
	 */	
	public void play(File file,float fPosition,long length,float fVolume) throws Exception;

	/**
	 * Stop current player
	 * @throws Exception
	 */
	public void stop() throws Exception;
	
	
	/**
	 * Set the gain
	 * @param fVolume : gain from 0 to 1
	 * @throws Exception
	 */
	public void setVolume(float fVolume) throws Exception;	
	
	/**
	 * @return elapsed time (ms) for this player
	 */
	public long getElapsedTime();
	
	/**Pause the player*/
	public void pause() throws Exception;
	
	/**Resume the player*/
	public void resume() throws Exception;
	
	/**Seek to a given position in %. ex : 0.2 for 20% */
	public void seek(float fPosition);
	
	/**Return track position in %*/
	public float getCurrentPosition();
	
    /**Return volume in %*/
    public float getCurrentVolume();
    
    /**Return player state */
	public int getState();
    
    
}
