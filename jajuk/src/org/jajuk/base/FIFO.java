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

import org.jajuk.Main;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.views.LogicalPlaylistRepositoryView;
import org.jajuk.ui.views.PhysicalPlaylistEditorView;
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

	/**Cuurently played track */
	private File fCurrent;
	
	/**Fifo itself, contains jajuk File objects **/
	private volatile ArrayList alFIFO;
	
	/**Stop flag**/
	private static volatile boolean bStop;
	
	/** Deep time**/
	private final int SLEEP_TIME = 100;
	
	/** Refresh time in ms**/
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
	private boolean bGlobalRandom;
	
	/** Best of enabled ? */
	private boolean bBestOf;
	
	/** Repeated set */
	private ArrayList alRepeated;
	
	/** Repeated set index */
	private int iRepeatIndex;
	
	/**Current file intro status*/
	private boolean bIntroEnabled;
	
	/**Starter thread*/
	private Thread tStarter;
	
	/** Current file position (%) used for pause */
	private int iPosition;
	
	/** Pause boolean */
	private boolean bPaused;
	
	/** Cumative time (ms) spent in pause mode for current track*/
	long lPauseTime;
	
	/** Date last time a pause was required */
	long lPauseDate;
	
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
		lPauseDate = 0;
		lPauseTime = 0;
		lTime = 0;
		bGlobalRandom = false;
		bBestOf = false;
		alRepeated = new ArrayList(50);
		bIntroEnabled = false;
		bPaused = false;
		fCurrent = null;
	}
	
	/**
	 * Push some files in the fifo
	 * @param alFiles, list of files to be played
	 * @param bAppend keep previous files or stop them to start a new one ?
	 * @param bAuto file is added by the system, not by a user action
	 * @param bForcedRepeat Force repeat mode for selection
	 */
	public synchronized void push(ArrayList alFiles, boolean bAppend,boolean bAuto,boolean bForcedRepeat) {
		this.bForcedRepeat = bForcedRepeat;
		if (!bAuto){
			FIFO.getInstance().setBestof(false); //best of mode is broken by any push
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
				if ( !bAuto){
					file.getTrack().setRate(file.getTrack().getRate()+2); //inc rate by 2 because it is explicitely selected to be played by human
				}
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
		if ( fCurrent != null){
			push(FileManager.getPreviousFile(fCurrent),false);	
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
			File file = FileManager.getFile(History.getInstance().getLastFile());
			file = FileManager.getNextFile(file);
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
				if ( bPaused){
					continue;
				}
				if (bPlaying ){//already playing something
					long length = fCurrent.getTrack().getLength();
					if ( i%(REFRESH_TIME/SLEEP_TIME) == 0 && length!=0){  //actual refresh less frequent for cpu
						PhysicalPlaylistEditorView.getInstance().update(EVENT_PLAYLIST_REFRESH); //alert playlists editors ( queue playlist ) something changed for him
						LogicalPlaylistRepositoryView.getInstance().update(EVENT_PLAYLIST_REFRESH); //alert playlists editors ( queue playlist ) something changed for him
						lTime = (System.currentTimeMillis() - lTrackStart) + lOffset - lPauseTime;
						if ( bIntroEnabled){
							lTime += (fCurrent.getTrack().getLength()*Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_BEGIN))*10);
						}
						InformationJPanel.getInstance().setCurrentStatusMessage(Util.formatTime(lTime)+" / "+Util.formatTime(fCurrent.getTrack().getLength()*1000));
						int iPos = (int)((lTime/10)/length);
						InformationJPanel.getInstance().setCurrentStatus(iPos);
						CommandJPanel.getInstance().setCurrentPosition(iPos);
						InformationJPanel.getInstance().setTotalStatusMessage(Util.formatTimeBySec((int)(lTotalTime-(lTime/1000))));
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
					else if ( bBestOf){ //Best of mode
						push(FileManager.getBestOfFile(),false,true);
					}
					else if ( fCurrent!= null && alRepeated.size()>0 && ( TRUE.equals(ConfigurationManager.getProperty(CONF_STATE_REPEAT)) || bForcedRepeat)){ //repeat mode ?
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
							InformationJPanel.getInstance().setCurrentStatusMessage(Util.formatTime(0)+" / "+Util.formatTime(0));
							InformationJPanel.getInstance().setCurrentStatus(0);
							CommandJPanel.getInstance().setCurrentPosition(0);
							InformationJPanel.getInstance().setTotalStatusMessage("00:00:00");
						}
						i++;
						continue; //leave
					}
				}
				if (alFIFO.size() == 0){
					continue;
				}
				synchronized(this){  //lock fifo access when lauching
					if ( !bPlaying){  //test this to avoid notifying at each launch
						ObservationManager.notify(EVENT_PLAYER_PLAY);  //notify to devices like commandJPanel to update ui
					}
					int index = 0;
					lOffset = 0;
					lPauseTime = 0;
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
						Player.play(fCurrent,Float.parseFloat(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_BEGIN))/100,1000*Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_LENGTH)));
					}
					else{
						Player.play(fCurrent,-1,-1);  //play it
					}
					lTrackStart = System.currentTimeMillis();
					//add hits number
					fCurrent.getTrack().incHits();  //inc hits number 
					fCurrent.getTrack().incSessionHits();//inc session hits
					fCurrent.getTrack().setRate(fCurrent.getTrack().getRate()+1); //inc rate by 1 because it is played
					if ( !(fCurrent instanceof BasicFile)){
						History.getInstance().addItem(fCurrent.getId(),System.currentTimeMillis());
					}
					InformationJPanel.getInstance().setMessage("Now Playing : "+fCurrent.getTrack().getAuthor().getName2()+" / "+fCurrent.getTrack().getAlbum().getName2()+" / "+fCurrent.getTrack().getName(),InformationJPanel.INFORMATIVE);
					Main.jframe.setTitle(fCurrent.getTrack().getName());
					InformationJPanel.getInstance().setQuality(fCurrent.getQuality()+" kbps");
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
		InformationJPanel.getInstance().setCurrentStatusMessage(Util.formatTime(0)+" / "+Util.formatTime(0));
		InformationJPanel.getInstance().setCurrentStatus(0);
		CommandJPanel.getInstance().setCurrentPosition(0);
		InformationJPanel.getInstance().setTotalStatusMessage("00:00:00");
		InformationJPanel.getInstance().setMessage("Ready to play",InformationJPanel.INFORMATIVE);
		InformationJPanel.getInstance().setQuality("");
		Main.jframe.setTitle("Jajuk : Just Another Jukebox | Java Jukebox");
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
	public static boolean canUnmount(Device device){
		if ( fifo== null || !bPlaying){ //currently stopped
			return true;
		}
		if (getInstance().fCurrent.getDirectory().getDevice().equals(device)){
			return false;
		}
		Iterator it = getInstance().alFIFO.iterator();
		while (it.hasNext()){
			File file = (File)it.next();
			if ( file.getDirectory().getDevice().equals(device)){
				return false;
			}
		}
		it = getInstance().alRepeated.iterator();
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
	 * Get current position in %
	 * @return position in % ( ex: 0.1 for 10%)
	 */
	public synchronized float getCurrentPosition(){
		return (float)lTime/(1000*fCurrent.getTrack().getLength());
	}
	
	/**
	 * Move inside a track
	 * @param fPosition position in % of track length. ex: 0.2 for 20%
	 */
	public synchronized void setCurrentPosition(float fPosition){
		float fCurrentPosition = getCurrentPosition();
		long lTrackLength = fCurrent.getTrack().getLength();  //in sec
		if (ConfigurationManager.getBoolean(CONF_STATE_INTRO) ){  //intro mode enabled
			float fBegin = Float.parseFloat(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_BEGIN));
			long length = 1000*Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_LENGTH));
			if (fPosition*lTrackLength<length && fPosition>fBegin){ //check position is compatible with intro bounds options
				Player.stop();
				Player.play(fCurrent,fPosition,(long)(length+(fPosition*lTrackLength)));
			}
		}
		else{//intro mode enabled
			Player.stop();
			Player.play(fCurrent,fPosition,1000*fCurrent.getTrack().getLength());
		}
		lOffset +=  fCurrent.getTrack().getLength()*1000*(fPosition - fCurrentPosition);
	}

	/**
	 * Stop request. Void the fifo
	 */
	public synchronized void stopRequest() {
		bStop = true;
		ObservationManager.notify(EVENT_PLAYLIST_REFRESH); //alert playlists editors ( queue playlist ) something changed for him
	}
	
	/**
	 * Pause request. 
	 */
	public synchronized void pauseRequest() {
		if ( !bPaused ){
			bPaused = true;
			lPauseDate = System.currentTimeMillis();
			Player.stop();
			ObservationManager.notify(EVENT_PLAYER_PAUSE);
		}
		else{
			lPauseTime+=(System.currentTimeMillis()-lPauseDate);
			//restart paused track
			if (ConfigurationManager.getBoolean(CONF_STATE_INTRO)){ //intro mode enabled
				Player.play(fCurrent,getCurrentPosition(),(long)(1000*(1-getCurrentPosition())*Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_LENGTH))));
			}
			else{
				Player.play(fCurrent,getCurrentPosition(),1000*fCurrent.getTrack().getLength());  //play it
			}
			bPaused = false;
			ObservationManager.notify(EVENT_PLAYER_UNPAUSE);
		}
	}

	

	/**
	 * @return Returns the bPaused.
	 */
	public synchronized boolean isPaused() {
		return bPaused;
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
