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
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  Manages playing sequences
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class FIFO implements ITechnicalStrings,Runnable,Observer{

    /** Currently played track*/
    private File fCurrent;

    /**Last played track*/
    private File fLastOne;

    /**Fifo itself, contains jajuk File objects */
    private volatile ArrayList alFIFO;
	
	/**Stop flag**/
	private static volatile boolean bStop;

    /**Deep time*/
    private final int SLEEP_TIME = 100;

    /**Refresh time in ms*/
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
	
	/**Has next been pressed?*/
	private boolean bNext = false;
	
	/**Has previous  been pressed?*/
	private boolean bPrevious = false;
	
	/**UI reinit flag for perfs, avoid to reinit at each heart beat*/
	private boolean bZero = false;
	
	/**Forced track to repeat*/
	private File fForcedRepeat = null;
	
	/**Play error flag, used to make a pause*/
	private boolean bError = false;
	
	
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
		fForcedRepeat = null;
		//register needed events
		ObservationManager.register(EVENT_SPECIAL_MODE,this);
		ObservationManager.register(EVENT_PLAY_ERROR,this);
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
		if (bPrevious){
		    return;
		}
	    bPrevious = true;
	    File file = null;
	    ArrayList alToPlay = new ArrayList(1);
		if ( fLastOne != null){
			file = FileManager.getPreviousFile(fLastOne);
		}
		else{ //called at startup with nothing, then user presses previous
			file = FileManager.getFile(History.getInstance().getLastFile());
		}
	   if ( file != null && file.isReady()){
		    alToPlay.add(file);
		}
		FIFO.getInstance().insert(alToPlay,0,true,true);
	}
		
	/**
	 * Play next track in selection
	 */
	public synchronized void playNext(){
        bNext = true;
	    if ( fCurrent != null){  //if stopped, nothing to stop
			Player.stop();
		    finished(); //stop current track and let the FIFO to choose the next one
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
				if (bError){
				    Thread.sleep(2000); //sleep to let user to read error message
				    bError = false;
				}
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
				if  (!bPlaying && alFIFO.size() == 0  ){//empty fifo, lets decide what to do with folowing priorities : global random / repeat / continue
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
					    push(alRepeated,false,true,bForcedRepeat);
					}
					else if ( fLastOne!= null && (TRUE.equals(ConfigurationManager.getProperty(CONF_STATE_CONTINUE)) || bNext )){ //continue mode ?
						//a next button overwrites a non-continue mode
					    bNext = false;
					    File fileNext = FileManager.getNextFile(fLastOne);
						if ( fileNext != null ){
							push(fileNext,true);	
						}
					}
					else{  //fifo empty and nothing planned to be played, lets re-initialize labels
						if ( !bZero){
							lTotalTime = 0;
							ObservationManager.notify(EVENT_ZERO);
							bZero = true;
						}
						i++;
						continue; //leave
					}
				}
				if (alFIFO.size() == 0){
					continue;
				}
				synchronized(this){  //lock fifo access when launching
					bZero = false;
				    Util.waiting();
					//intro workaround : intro mode is only read at track launch and can't be set during the play
					bIntroEnabled = ConfigurationManager.getBoolean(CONF_STATE_INTRO); //re-read intro mode
					if ( !bPlaying){  //test this to avoid notifying at each launch
						ObservationManager.notify(EVENT_PLAYER_PLAY);  //notify to devices like commandJPanel to update ui when the play button has been pressed
						//	set was playing state
						ConfigurationManager.setProperty(CONF_STATE_WAS_PLAYING,TRUE);
					}
					int index = 0;
					lOffset = 0;
					fCurrent = (File) (alFIFO.get(index));//take the first file in the fifo
					fLastOne = (File)fCurrent.clone(); //save the last played track
					bPrevious = false; //allow insert to be done with right previous file
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
					Properties pDetails = new Properties();
					pDetails.put(DETAIL_CURRENT_FILE_ID,fCurrent.getId());
					pDetails.put(DETAIL_CURRENT_DATE,new Long(System.currentTimeMillis()));
				    ObservationManager.notify(EVENT_FILE_LAUNCHED,pDetails);
				}
			}
			//fifo is over ( stop request ) , reinit labels in information panel before exiting
			//	set was playing state if it is not a stop called by jajuk exit 
			if (!Main.isExiting()){
			    ConfigurationManager.setProperty(CONF_STATE_WAS_PLAYING,FALSE);
			}
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
		//insert a forced repeat track at first FIFO position. Note this mode is special because it allow to repeat over one file even with others tracks in the FIFO
		if (fForcedRepeat != null){
		    ArrayList alForcedRepeat = new ArrayList(1);
		    alForcedRepeat.add(fForcedRepeat);
		    insert(alForcedRepeat,0,false,false);
		};
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
	   fForcedRepeat = file;
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

	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject) {
		if (subject.equals(EVENT_SPECIAL_MODE)){
			ArrayList alToPlay = null; 
			String sMode = (String)ObservationManager.getDetail(EVENT_SPECIAL_MODE,DETAIL_SPECIAL_MODE);
			if (sMode == null){
				return;
			}
			//bestof selection
			if (DETAIL_SPECIAL_MODE_BESTOF.equals(sMode)){
				alToPlay = (ArrayList)ObservationManager.getDetail(EVENT_SPECIAL_MODE,DETAIL_SELECTION);
				if (alToPlay != null){
					setGlobalRandom(false); //break global random mode if set
					setNovelties(false); //break novelties mode if set
					setBestof(true);
					push(alToPlay,false,true,false);
				}
			}
			//novelties selection
			else if (DETAIL_SPECIAL_MODE_NOVELTIES.equals(sMode)){
				alToPlay = (ArrayList)ObservationManager.getDetail(EVENT_SPECIAL_MODE,DETAIL_SELECTION);
				if (alToPlay != null){
					setGlobalRandom(false); //break global random mode if set
					setBestof(false); //break best of mode if set
					setNovelties(true);
					push(alToPlay,false,true,false);
				}
			}
			//Global shuffle selection
			else if (DETAIL_SPECIAL_MODE_SHUFFLE.equals(sMode)){
				alToPlay = (ArrayList)ObservationManager.getDetail(EVENT_SPECIAL_MODE,DETAIL_SELECTION);
				if (alToPlay != null){
					setBestof(false); //break best of mode if set
					setNovelties(false); //break novelties mode if set
					setGlobalRandom(true);
					push(alToPlay,false,true,false);
				}
			}
			//Come back to normal selection
			else if (DETAIL_SPECIAL_MODE_NORMAL.equals(sMode)){
				clear();
			}
		}
		else if (subject.equals(EVENT_PLAY_ERROR)){
		    bError = true;
		}	
	}
	
	/**
	 * Shuffle the FIFO, used when user select the Random mode
	 */
	public synchronized void shuffle(){
		Collections.shuffle(alFIFO);
	}
	
	/**
	 * Insert a file to play in FIFO at specified position
	 * @param file
	 * @param iPos
	 * @param bImmediate immediate play ?
	 * @param bKeepLast add again the last track ?
	 */
	public synchronized void insert(ArrayList alFiles,int iPos,boolean bImmediate,boolean bKeepLast){
	    //	  ok, stop current track
	    if (bImmediate) {
	        Player.stop();
	        bPlaying = false;
	        lTotalTime = 0;
	    }
	    //re-add current track if any
	    if (bKeepLast && fLastOne != null){
	        alFIFO.add(0,fLastOne);
	    }
	    File file = null;
	    int index = 0;
	    //reset total time
	    Iterator it = alFIFO.iterator();
	    while (it.hasNext()){
	        file = (File)it.next();
	        lTotalTime += file.getTrack().getLength();
	    }
	    //add required tracks
	    it = alFiles.iterator();
	    int i = 0;
	    while (it.hasNext()){
	        file = (File)it.next();
	        alFIFO.add(index+i,file);
	        index++;
	        lTotalTime += file.getTrack().getLength();
	        i++;
	    }
	}
	
	
}
