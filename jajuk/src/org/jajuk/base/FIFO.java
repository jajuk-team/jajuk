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
package org.jajuk.base;

import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.ObservationManager;
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
	private File fCurrent;
	
	/**Fifo itself, contains jajuk File objects **/
	private volatile ArrayList alFIFO = new ArrayList(50);
	
	/**Stop flag**/
	private volatile boolean bStop = false;
	
	/** Deep time**/
	private final int SLEEP_TIME = 100;
	
	/** Refresh time in ms**/
	private final int REFRESH_TIME = 1000;
	
	/**Self instance*/
	static private FIFO fifo= null; 	
	
	/**True is a track is playing */
	static private boolean bPlaying = false;
	
	/** Forced repeat mode flag*/
	boolean bForcedRepeat = false;
	
	
	/** Current track start date*/
	public long lTrackStart; 
	
	/** Total time in fifo (sec)*/
	private long lTotalTime = 0;
	
	/** Glocal random enabled ? */
	private boolean bGlobalRandom = false;
	
	/** Repeated set */
	private ArrayList alRepeated = new ArrayList(50);
	
	/** Repeated set index */
	private int iRepeatIndex;
	
	/**Current file intro status*/
	private boolean bIntroEnabled = false;
	
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
	 * @param bAuto file is added by the system, not by a user action
	 * @param bForcedRepeat Force repeat mode for selection
	 */
	public synchronized void push(ArrayList alFiles, boolean bAppend,boolean bAuto,boolean bForcedRepeat) {
		Util.waiting();
		this.bForcedRepeat = bForcedRepeat;
		if (!bAuto){
			FIFO.getInstance().setGlobalRandom(false); //global random mode is broken by any push
			if (TRUE.equals(ConfigurationManager.getProperty(CONF_STATE_REPEAT)) || this.bForcedRepeat){  //repeat is on
				alRepeated = alFiles;
				iRepeatIndex = 0;
			}
		}
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
	 * Push some files in the fifo
	 * @param file, file to be played
	 * @param bAppend keep previous files or stop them to start a new one ?
	 * @param bAuto file is added by the system, not by a user action
	 * @param bForcedRepeat Force repeat mode for selection
	 */
	public synchronized void push(File file, boolean bAppend, boolean bAuto,boolean bForcedRepeat) {
		ArrayList alFiles = new ArrayList(1);
		alFiles.add(file);
		push(alFiles,bAppend,bAuto,bForcedRepeat);
	}
	
	
	
	/**
	 * Push some files in the fifo
	 * @param file, file to be played
	 * @param bAppend keep previous files or stop them to start a new one ?
	 * @param bAuto file is added by the system, not by a user action
	 */
	public synchronized void push(File file, boolean bAppend, boolean bAuto) {
		push(file,bAppend,bAuto,false);
	}
	

	/**
	 * Push some files in the fifo
	 * @param alFiles, list of files to be played
	 * @param bAppend keep previous files or stop them to start a new one ?
	 */
	public synchronized void push(ArrayList alFiles, boolean bAppend) {
		push(alFiles,bAppend,false,false);
	}
	
	
	/**
	 * Push one file in the fifo
	 * @param file, file to be played
	 * @param bAppend keep previous files or stop them to start a new one ?
	 */
	public synchronized void push(File file, boolean bAppend) {
		push(file,bAppend,false);
	}
	
	
	/**
	 * Clears the fifo, for example when we want to add a group of files stopping previous plays
	 *
	 */
	public synchronized void clear() {
		alFIFO.clear();
	}
	
	/**
	 * Play previous track
	 */
	public synchronized void playPrevious(){
		push(FileManager.getPreviousFile(fCurrent),false);
	}
	
	/**
	 * Play next track
	 */
	public synchronized void playNext(){
		finished();
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			int i = 0;
			while (!bStop) {
				Thread.sleep(SLEEP_TIME); //sleep to save CPU
				if (bPlaying ){//already playing something
					long length = fCurrent.getTrack().getLength();;
					if ( i%(REFRESH_TIME/SLEEP_TIME) == 0 && length!=0){  //actual refresh less frequent for cpu
						long lTime = System.currentTimeMillis() - lTrackStart;
						if ( bIntroEnabled){
							lTime += (fCurrent.getTrack().getLength()*Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_BEGIN))*10);
						}
						InformationJPanel.getInstance().setCurrentStatusMessage(Util.formatTime(lTime)+" / "+Util.formatTime(fCurrent.getTrack().getLength()*1000));
						InformationJPanel.getInstance().setCurrentStatus((int)((lTime/10)/length));
						InformationJPanel.getInstance().setTotalStatusMessage(Integer.toString((int)(lTotalTime-(lTime/1000)))+"'");
					}
					i++;
					continue; //leave
				}
				if (!bPlaying && alFIFO.size() == 0  ){//empty fifo, lets decide what to do with folowing priorities : global random / repeat / continue
					//intro workaround : intro mode is only read at track lauch and can't be set during the play
					if (ConfigurationManager.getBoolean(CONF_STATE_INTRO)){ //intro mode enabled
						bIntroEnabled = true;
					}
					else{
						bIntroEnabled = false;
					}
					//next file choice
					if ( bGlobalRandom){ //Global random mode
						push(FileManager.getShuffleFile(),false,true);
					}
					else if ( fCurrent!= null && ( TRUE.equals(ConfigurationManager.getProperty(CONF_STATE_REPEAT)) || bForcedRepeat)){ //repeat mode ?
						if (iRepeatIndex == alRepeated.size()){
							iRepeatIndex = 0;
						}
						push((File)alRepeated.get(iRepeatIndex),false,true,bForcedRepeat);
						iRepeatIndex ++;
					}
					else if ( fCurrent!= null && TRUE.equals(ConfigurationManager.getProperty(CONF_STATE_CONTINUE))){ //continue mode ?
						File fileNext = FileManager.getNextFile(fCurrent);
						if ( fileNext != null ){
							push(fileNext,true);	
						}
					}
					else{  //fifo empty and nothing planned to be played, lets re-initialize labels
						if ( i%(REFRESH_TIME/SLEEP_TIME) == 0){  //actual refresh less frequent for cpu
							lTotalTime = 0;
							long lTime = 0;
							InformationJPanel.getInstance().setCurrentStatusMessage(Util.formatTime(0)+" / "+Util.formatTime(0));
							InformationJPanel.getInstance().setCurrentStatus(0);
							InformationJPanel.getInstance().setTotalStatusMessage("0'");
						}
						i++;
						continue; //leave
					}
				}
				if (alFIFO.size() == 0){
					continue;
				}
				synchronized(this){  //lock fifo access when lauching
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
					Player.stop();  //for security, make sure no other track is playing
					ObservationManager.notify(EVENT_COVER_REFRESH); //request update cover 
					if (ConfigurationManager.getBoolean(CONF_STATE_INTRO)){ //intro mode enabled
						Player.play(fCurrent,Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_BEGIN)),Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_LENGTH)));
					}
					else{
						Player.play(fCurrent,-1,-1);  //play it
					}
					lTrackStart = System.currentTimeMillis();
					if ( !(fCurrent instanceof BasicFile)){
						History.getInstance().addItem(fCurrent.getId(),System.currentTimeMillis());
					}
					InformationJPanel.getInstance().setMessage("Now Playing : "+fCurrent.getTrack().getAuthor().getName2()+" / "+fCurrent.getTrack().getAlbum().getName2()+" / "+fCurrent.getTrack().getName(),InformationJPanel.INFORMATIVE);
					InformationJPanel.getInstance().setQuality(fCurrent.getQuality()+" kbps");
				}
			}
		} catch (Exception e) {
			Log.error("122", e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Stopping thread method
	 *
	 */
	public synchronized void stopFIFO() {
		bStop = true;
	}
	
	/**
	 * Finished method, called by the PlayerImpl when the track is finished
	 *
	 */
	public synchronized void finished(){
		bPlaying = false;
		lTotalTime -= fCurrent.getTrack().getLength();
	}
	
	/**
	 *  Get the currently played  file
	 * @return File
	 **/
	public synchronized File getCurrentFile(){
		return fCurrent;
	}
	
	/**
	 * Return true if none file is playing or planned to play for the given device
	 * @param device device to unmount
	 * @return
	 */
	public synchronized boolean canUnmount(Device device){
		if (fCurrent.getDirectory().getDevice().equals(device)){
			return false;
		}
		Iterator it = alFIFO.iterator();
		while (it.hasNext()){
			File file = (File)it.next();
			if ( file.getDirectory().getDevice().equals(device)){
				return false;
			}
		}
		it = alRepeated.iterator();
		while (it.hasNext()){
			File file = (File)it.next();
			if ( file.getDirectory().getDevice().equals(device)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @return Returns the bGlobalRandom.
	 */
	public boolean isGlobalRandom() {
		return bGlobalRandom;
	}

	/**
	 * @param globalRandom The bGlobalRandom to set.
	 */
	public void setGlobalRandom(boolean globalRandom) {
		bGlobalRandom = globalRandom;
	}

	

}
