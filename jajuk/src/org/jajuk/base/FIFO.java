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
import java.util.Properties;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
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
public class FIFO implements ITechnicalStrings,Runnable{

    /** Currently played track*/
    private File fCurrent;

    /**
     * Last played track
     */
    private File fLastOne;

    /**
     * Fifo itself, contains jajuk File objects
     */
    private volatile ArrayList alFIFO;

	
	/**Stop flag**/
	private static volatile boolean bStop;

    /**
     * Deep time
     */
    private final int SLEEP_TIME = 100;

    /**
     * Refresh time in ms
     */
    private final int REFRESH_TIME = 1000;

		/**Self instance*/
	static private FIFO fifo= null; 	
	
	/**True if a track is playing */
	static private boolean bPlaying;
	
	/** Forced repeat mode flag*/
	boolean bForcedRepeat;
	
	/** Current track start date*/
	public long lTrackStart; 
	
	/** Total time in fifo (sec)*/
	private long lTotalTime;
	
	/** Offset since begin in ms*/
	long lOffset;
	
	/** Current play time in ms*/
	long lTime;
	
	/** Glocal random enabled ? */
	private boolean bGlobalRandom = false;
	
	/** Best of enabled ? */
	private boolean bBestOf = false;
	
	/** Novelties enabled ? */
	private boolean bNovelties = false;

    /**Repeated set  */
    private ArrayList alRepeated;

	
	/** Repeated set index */
	private int iRepeatIndex;
	
	/**Current file intro status*/
	private boolean bIntroEnabled;
	
	/**Starter thread*/
	private Thread tStarter;
	
	/** Current file position (%) used for pause */
	private int iPosition;
	
	/** First played file flag**/
	private static boolean bFirstFile = true;
	
	/**First file should seek to position flag*/
	private boolean bSeekFirstFile = false;
		
	/**
	 * Singleton access
	 * @return
	 */
	public synchronized static FIFO getInstance(){
		if (fifo == null){
			fifo = new FIFO();
		}
		return fifo;
	}
	
	/**
	 * constructor
	 */
	private FIFO() {
		if ( ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST_KEEP_POS)){
		    bSeekFirstFile = true;
		}
		init();
		tStarter = new Thread(this);
		tStarter.start();
	}
	
	/**
	 * Initialisation
	 */
	private void init(){
		alFIFO = new ArrayList(50);
		bStop = false;
		bPlaying = false;
		bForcedRepeat = false;
		lTotalTime = 0;
		lOffset = 0;
		lTime = 0;
		bGlobalRandom = false;
		bBestOf = false;
		alRepeated = new ArrayList(50);
		bIntroEnabled = false;
		fCurrent = null;
		fLastOne = null;
	}
	
	/**
	 * Push some files in the fifo
	 * @param alFiles, list of files to be played
	 * @param bAppend keep previous files or stop them to start a new one ?
	 * @param bAuto file is added by the system, not by a user action
	 * @param bForcedRepeat Force repeat mode for selection
	 */
	public synchronized void push(ArrayList alFiles, boolean bAppend,boolean bAuto,boolean bForcedRepeat) {
		//if fifo is down, restart it
	    if (fifo == null){
	        fifo = getInstance();
	    }
	    this.bForcedRepeat = bForcedRepeat;
		//first try to mount needed devices
		Iterator it = alFiles.iterator();
		File file = null;
		while (it.hasNext()){
			file = (File)it.next();
			if (file == null){
				it.remove();
				break;
			}
			if ( file.getDirectory()!=null && !file.getDirectory().getDevice().isMounted()){  //file is null if it is a BasicFile
				//not mounted, ok let them a chance to mount it:
				String sMessage = Messages.getString("Error.025")+" ("+file.getDirectory().getDevice().getName()+Messages.getString("FIFO.4"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				int i = JOptionPane.showConfirmDialog(Main.getWindow(),sMessage,Messages.getString("Warning"),JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
				if ( i == JOptionPane.YES_OPTION){
					try{
						file.getDirectory().getDevice().mount();
					}
					catch(Exception e){
						it.remove();
						Log.error(e);
						Messages.showErrorMessage("011",file.getDirectory().getDevice().getName()); //$NON-NLS-1$
						return;
					}
				}
				else{
					it.remove();
					return;
				}
			}
		}
		//test if we have yet some files to play
		if ( alFiles.size() == 0){
			return;
		}
		//set repeat and other env. 
		if (!bAuto){
			FIFO.getInstance().setBestof(false); //best of mode is broken by any push
			FIFO.getInstance().setGlobalRandom(false); //global random mode is broken by any push
			if (TRUE.equals(ConfigurationManager.getProperty(CONF_STATE_REPEAT)) || this.bForcedRepeat){  //repeat is on
				alRepeated = alFiles;
				iRepeatIndex = 0;
			}
		}
		//ok, stop current track
		if (!bAppend) {
			Player.stop();
			bPlaying = false;
			clear();
			lTotalTime = 0;
		}
		//add required tracks
		it = alFiles.iterator();
		while (it.hasNext()){
			file = (File)it.next();
			if ( !bAuto){
				file.getTrack().setRate(file.getTrack().getRate()+2); //inc rate by 2 because it is explicitely selected to be played by human
				FileManager.setRateHasChanged(true); //alert bestof playlist something changed
			}
			alFIFO.add(file);
			lTotalTime += file.getTrack().getLength();
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
		if ( fLastOne != null){
			push(FileManager.getPreviousFile(fLastOne),false);	
		}
		else{
			File file = FileManager.getFile(History.getInstance().getLastFile());
			file = FileManager.getPreviousFile(file);
			if ( file != null && file.isReady()){
				FIFO.getInstance().push(file,false);
			}
		}
	}
	
	/**
	 * Play next track
	 */
	public synchronized void playNext(){
		if ( fCurrent != null){  //if stopped, nothing to stop
			finished();
		}
		else{
			File file = FileManager.getNextFile(fLastOne);
			if ( file != null && file.isReady()){
				FIFO.getInstance().push(file,false);
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			int i = 0;
			while (!bStop) {
				Thread.sleep(SLEEP_TIME); //sleep to save CPU
				if ( Player.isPaused()){
					continue;
				}
				if (bPlaying ){//already playing something
					long length = fCurrent.getTrack().getLength();
					if ( i%(REFRESH_TIME/SLEEP_TIME) == 0 ){  //actual refresh less frequent for cpu
						lTime = Player.getElapsedTime();
						int iPos = (length!=0)?(int)((lTime/10)/length):0;  //if length=0, pos is always 0 to avoid division by zero
						ConfigurationManager.setProperty(CONF_STARTUP_LAST_POSITION,Float.toString(((float)iPos)/100)); //store current position
						//lauch some messages to information and command panels
						Properties pDetails = new Properties();
						pDetails.put(DETAIL_CURRENT_POSITION,new Integer(iPos));
						pDetails.put(DETAIL_TOTAL,Util.formatTimeBySec((int)(lTotalTime-(lTime/1000)),false));
					    pDetails.put(DETAIL_CURRENT_STATUS_MESSAGE,Util.formatTime(lTime)+" / "+Util.formatTime(fCurrent.getTrack().getLength()*1000)); //$NON-NLS-1$
						ObservationManager.notify(EVENT_HEART_BEAT,pDetails);
					}
					i++;
					continue; //leave
				}
				if (!bPlaying && alFIFO.size() == 0  ){//empty fifo, lets decide what to do with folowing priorities : global random / repeat / continue
					//next file choice
					if ( bGlobalRandom){ //Global random mode
						push(FileManager.getShuffleFile(),false,true);
					}
					else if ( bBestOf){ //Best of mode
						push(FileManager.getBestOfFile(),false,true);
					}
					else if ( bBestOf){ //Novelties mode
						push(FileManager.getNoveltyFile(),false,true);
					}
					else if ( fLastOne!= null && alRepeated.size()>0 && ( TRUE.equals(ConfigurationManager.getProperty(CONF_STATE_REPEAT)) || bForcedRepeat)){ //repeat mode ?
						if (iRepeatIndex == alRepeated.size()){
							iRepeatIndex = 0;
						}
						push((File)alRepeated.get(iRepeatIndex),false,true,bForcedRepeat);
						iRepeatIndex ++;
					}
					else if ( fLastOne!= null && TRUE.equals(ConfigurationManager.getProperty(CONF_STATE_CONTINUE))){ //continue mode ?
						File fileNext = FileManager.getNextFile(fLastOne);
						if ( fileNext != null ){
							push(fileNext,true);	
						}
					}
					else{  //fifo empty and nothing planned to be played, lets re-initialize labels
						if ( i%(REFRESH_TIME/SLEEP_TIME) == 0){  //actual refresh less frequent for cpu
							lTotalTime = 0;
						}
						i++;
						continue; //leave
					}
				}
				if (alFIFO.size() == 0){
					continue;
				}
				synchronized(this){  //lock fifo access when launching
					Util.waiting();
					//intro workaround : intro mode is only read at track launch and can't be set during the play
					bIntroEnabled = ConfigurationManager.getBoolean(CONF_STATE_INTRO); //re-read intro mode
					if ( !bPlaying){  //test this to avoid notifying at each launch
						ObservationManager.notify(EVENT_PLAYER_PLAY);  //notify to devices like commandJPanel to update ui when the play button has been pressed
					}
					int index = 0;
					lOffset = 0;
					if (ConfigurationManager.getProperty(CONF_STATE_SHUFFLE).equals(TRUE)){
						index = (int)(Math.random() * alFIFO.size());
						fCurrent = (File) (alFIFO.get(index));//take the first file in the fifo
					}
					else{
						index = 0;
						fCurrent = (File) (alFIFO.get(index));//take the first file in the fifo
					}
					fLastOne = (File)fCurrent.clone(); //save the last played track
					alFIFO.remove(index);//remove it from todo list;
					Log.debug("Now playing :"+fCurrent); //$NON-NLS-1$
					bPlaying = true;
					Player.stop();  //for security, make sure no other track is playing
					ObservationManager.notify(EVENT_COVER_REFRESH); //request update cover 
					if (ConfigurationManager.getBoolean(CONF_STATE_INTRO)){ //intro mode enabled
						Player.play(fCurrent,Float.parseFloat(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_BEGIN))/100,1000*Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_LENGTH)));
					}
					else{
						if (bFirstFile && bSeekFirstFile){ //if it is the first played file and we are in startup mode keep position
						    float fPos = ConfigurationManager.getFloat(CONF_STARTUP_LAST_POSITION);
						    Player.play(fCurrent,fPos,-1);  //play it
						}
						else{
						    Player.play(fCurrent,-1,-1);  //play it
						}
					}
					bFirstFile = false;
				   lTrackStart = System.currentTimeMillis();
					//add hits number
					fCurrent.getTrack().incHits();  //inc hits number 
					fCurrent.getTrack().incSessionHits();//inc session hits
					fCurrent.getTrack().setRate(fCurrent.getTrack().getRate()+1); //inc rate by 1 because it is played
					FileManager.setRateHasChanged(true);
					if ( !(fCurrent instanceof BasicFile)){
						Properties pDetails = new Properties();
						pDetails.put(DETAIL_CURRENT_FILE_ID,fCurrent.getId());
						pDetails.put(DETAIL_CURRENT_DATE,new Long(System.currentTimeMillis()));
					    ObservationManager.notify(EVENT_FILE_LAUNCHED,pDetails);
					}
				}
			}
			//fifo is over ( stop request ) , reinit labels in information panel before exiting
			reset();  //reset ui
			Player.stop();  //stop player
			fifo = null; //delete singleton
			init();  //reinit all variables
			ObservationManager.notify(EVENT_PLAYER_STOP);  //notify to devices like commandJPanel to update ui
		} catch (Exception e) {
		    Log.error("122", e); //$NON-NLS-1$
		    fifo = null; //delete singleton
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
	 * Reset all ui indicators ( when stopping for instance )
	 */
	private synchronized void reset(){
		lTotalTime = 0;
		ObservationManager.notify(EVENT_ZERO);
	}
	
	/**
	 * Finished method, called by the PlayerImpl when the track is finished
	 *
	 */
	public synchronized void finished(){
		bPlaying = false;
		if ( fCurrent != null){ 
			lTotalTime -= fCurrent.getTrack().getLength();
		}
		fCurrent = null;
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
	public static boolean canUnmount(Device device){
		if ( fifo == null || !bPlaying || getInstance().fCurrent == null){ //currently stopped
			return true;
		}
		if (getInstance().fCurrent.getDirectory().getDevice().equals(device)){ //is current track  on this device?
			return false;
		}
		Iterator it = getInstance().alFIFO.iterator(); //are next tracks in fifo on this device?
		while (it.hasNext()){
			File file = (File)it.next();
			if ( file.getDirectory().getDevice().equals(device)){
				return false;
			}
		}
		it = getInstance().alRepeated.iterator();  //are repeat fifo tracks on this device ?
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
	 * @return Returns the bestof mode.
	 */
	public boolean isBestof() {
		return bBestOf;
	}
	
	/**
	 * @return Returns the novelties mode.
	 */
	public boolean isNovelties() {
		return bNovelties;
	}
	
	/**
	 * @param globalRandom The bGlobalRandom to set.
	 */
	public void setGlobalRandom(boolean globalRandom) {
		bGlobalRandom = globalRandom;
	}
	
	/**
	 * @param bestof The bestof mode set.
	 */
	public void setBestof(boolean bBestOf) {
		this.bBestOf = bBestOf;
	}
	
	/**
	 * @param bestof The novelties mode set.
	 */
	public void setNovelties(boolean bNovelties){
		this.bNovelties = bNovelties;
	}
	
	/**
	 * Force the repeat mode without using push, used to loop on the current track
	 * @param file file to loop on
	 */
	public void forceRepeat(File file){
	    alRepeated.clear();
	    alRepeated.add(file);
	}
	/**
	 * Stop request. Void the fifo
	 */
	public synchronized void stopRequest() {
		bStop = true;
	}
	
	
	/**
	 * @return Returns the bStop.
	 */
	public static boolean isStopped() {
		return bStop;
	}
	
	/**
	 * @return Returns the play time in ms.
	 */
	public long getCurrentPlayTime() {
		return lTime;
	}
	
	/**
	 * @return Returns the alFIFO.
	 */
	public synchronized ArrayList getFIFO() {
		return alFIFO;
	}
	
}
