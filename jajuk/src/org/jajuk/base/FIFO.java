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
 * Revision 1.6  2003/11/18 18:58:07  bflorat
 * 18/11/2003
 *
 * Revision 1.5  2003/11/16 17:57:18  bflorat
 * 16/11/2003
 *
 * Revision 1.4  2003/10/21 20:43:06  bflorat
 * TechnicalStrings to ITechnicalStrings according to coding convention
 *
 * Revision 1.3  2003/10/21 20:37:54  bflorat
 * 21/10/2003
 *
 * Revision 1.2  2003/10/17 20:36:45  bflorat
 * 17/10/2003
 *
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.base;

import java.util.ArrayList;

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.log.Log;

/**
 *  Manages playing sequences
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class FIFO extends Thread implements ITechnicalStrings{

	/**Cuurently played track */
	private static File fCurrent;

	/**Fifo itself, contains jajuk File objects **/
	private static ArrayList alFIFO = new ArrayList(50);

	/**Stop flag**/
	private static volatile boolean bStop = false;

	/** Deep time**/
	private static final int SLEEP_TIME = 50;
	
	/**Self instance*/
		static private FIFO fifo= null; 	
	
	
		/**
		 * Singleton access
		 * @return
		 */
		public static FIFO getInstance(){
			if (fifo == null){
				fifo = new FIFO();
			}
			return fifo;
		}

	/**
	 * constructor
	 */
	private FIFO() {
	}

	/**
	 * Push some files in the fifo
	 * @param alFiles, list of files to be played
	 * @param bAppend keep previous files or stop them to start a new one ?
	 */
	public static synchronized void push(ArrayList alFiles, boolean bAppend) {
		if (!bAppend) {
			Player.stop();
			fCurrent = null;
			clear();
		}
		alFIFO.addAll(alFiles);
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
	public synchronized void run() {
		try {
			while (!bStop) {
				Thread.sleep(SLEEP_TIME); //sleep to save CPU
				if (fCurrent != null
					|| alFIFO.size() == 0) {//already playing something or empty fifo
					continue; //leave
				}
				int index = 0;
				if (ConfigurationManager.getProperty(CONF_STATE_SHUFFLE).equals("true")){
					index = (int)(Math.random() * alFIFO.size());
					fCurrent = (File) (alFIFO.get(index));//take the first file in the fifo
				}
				else{
					index = 0;
					fCurrent = (File) (alFIFO.get(index));//take the first file in the fifo
				}
				alFIFO.remove(index);//remove it from todo list;
				Log.debug("Now playing :"+fCurrent); //$NON-NLS-1$
				Player.play(fCurrent);  //play it
			}
		} catch (Exception e) {
			Log.error("", e); //$NON-NLS-1$
		}
	}

	/**
	 * Stopping thread method
	 *
	 */
	public static synchronized void stopFIFO() {
		bStop = true;
	}
	
	/**
	 * Finished method, called by the PlayerImpl when the track is finished
	 *
	 */
	public static synchronized void finished(){
		Log.debug(fCurrent+ " is finished"); //$NON-NLS-1$
		fCurrent = null;
	}
	
	/**
	 *  Get the currently played  file
	 * @return File
	 **/
	public static synchronized File getCurrentFile(){
		return fCurrent;
	}

}
