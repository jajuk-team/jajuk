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
* $Revision$
 */
package org.jajuk.base;

import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.ui.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
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
	
	/** Refresh time in ms**/
	private static final int REFRESH_TIME = 1000;
	
	/**Self instance*/
	static private FIFO fifo= null; 	
	
	/**True is a track is playing */
	static private boolean bPlaying = false;
	
	/** Current track start date*/
	private static long lTrackStart; 
	
	/** Total time in fifo (ms)*/
	private static long lTotalTime = 0;
	
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
			bPlaying = false;
			clear();
			lTotalTime = 0;
		}
		Iterator it = alFiles.iterator();
		while (it.hasNext()){
			File file = (File)it.next();
			if (file != null){
				alFIFO.add(file);	
				lTotalTime += file.getTrack().getLength();
			}
		}
	}
	
	/**
	 * Push one file in the fifo
	 * @param file, file to be played
	 * @param bAppend keep previous files or stop them to start a new one ?
	 */
	public static synchronized void push(File file, boolean bAppend) {
		ArrayList al = new ArrayList(1);
		al.add(file);
		push(al,bAppend);
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
			int i = 0;
			while (!bStop) {
				Thread.sleep(SLEEP_TIME); //sleep to save CPU
				if (bPlaying ){//already playing something
					if ( i%(REFRESH_TIME/SLEEP_TIME) == 0){  //actual refresh less frequent for cpu
						long lTime = System.currentTimeMillis() - lTrackStart;
						InformationJPanel.getInstance().setCurrentStatusMessage(Util.formatTime(lTime)+" / "+Util.formatTime(fCurrent.getTrack().getLength()*1000));
						InformationJPanel.getInstance().setCurrentStatus((int)((lTime/10)/fCurrent.getTrack().getLength()));
						InformationJPanel.getInstance().setTotalStatusMessage(Integer.toString((int)(lTotalTime-(lTime/1000)))+"'");
					}
					i++;
					continue; //leave
				}
				if (!bPlaying && alFIFO.size() == 0 && fCurrent!= null && TRUE.equals(ConfigurationManager.getProperty(CONF_STATE_CONTINUE))){ //empty fifo
					File fileNext = FileManager.getNextFile(fCurrent);
					if ( fileNext != null ){
						push(FileManager.getNextFile(fCurrent),true);	
					}
				}
				if (alFIFO.size() == 0){
					continue;
				}
				int index = 0;
				if (ConfigurationManager.getProperty(CONF_STATE_SHUFFLE).equals(TRUE)){
					index = (int)(Math.random() * alFIFO.size());
					fCurrent = (File) (alFIFO.get(index));//take the first file in the fifo
				}
				else{
					index = 0;
					fCurrent = (File) (alFIFO.get(index));//take the first file in the fifo
				}
				alFIFO.remove(index);//remove it from todo list;
				Log.debug("Now playing :"+fCurrent); //$NON-NLS-1$
				bPlaying = true;
				Player.play(fCurrent);  //play it
				lTrackStart = System.currentTimeMillis();
				History.getInstance().addItem(fCurrent.getId(),System.currentTimeMillis());
				InformationJPanel.getInstance().setMessage("Now Playing : "+fCurrent.getTrack().getAuthor().getName2()+" / "+fCurrent.getTrack().getName(),InformationJPanel.INFORMATIVE);
				InformationJPanel.getInstance().setQuality(fCurrent.getQuality()+" kbps");
			}
		} catch (Exception e) {
			Log.error("122", e); //$NON-NLS-1$
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
		bPlaying = false;
		lTotalTime -= fCurrent.getTrack().getLength();
	}
	
	/**
	 *  Get the currently played  file
	 * @return File
	 **/
	public static synchronized File getCurrentFile(){
		return fCurrent;
	}
	
}
