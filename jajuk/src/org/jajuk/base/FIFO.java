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
package org.jajuk.base;

import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.util.log.Log;

/**
 *  Manages playing sequences
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class FIFO extends Thread {

	/**Fifo itself, contains jajuk File objects **/
	private static ArrayList alFIFO = new ArrayList(50);
	//TODO implements jajuk file class

	/**Stop flag**/
	private static volatile boolean bStop = false;

	/** Deep time**/
	private static final int SLEEP_TIME = 50;

	/**
	 * constructor
	 */
	public FIFO() {
	}

	/**
	 * Push a file
	 * @param file
	 * @param bAppend keep previous files or stop them to start a new one ?
	 */
	public static synchronized void push(File file) {
		alFIFO.add(file);
	}
	
	/**
	 * Clears the fifo, for example when we want to add a group of files stopping previous plays
	 *
	 */
	public static synchronized void clear() {
		alFIFO.clear();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			while (!bStop) {
				Thread.sleep(SLEEP_TIME); //sleep to save CPU
				if (alFIFO.size()==0){
					continue;
				}
				File fileLast = (File) (alFIFO.get(alFIFO.size() - 1));
				if (Player.isComplete(fileLast.getType())) {
					Log.debug("finished: "+fileLast);
					//OK, finished, lets start a new track
					alFIFO.remove(alFIFO.size() - 1); //remove the ended track 
					if ( alFIFO.size()>0){
						fileLast = (File) (alFIFO.get(alFIFO.size() - 1));
						Log.debug("launches : "+fileLast);
						Player.play(fileLast);
					}
				}
			}
		} catch (Exception e) {
			Log.error("", e);
		}
	}

	public static synchronized void stopFIFO() {
		bStop = true;
	}

}
