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
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  Manages playing sequences
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class FIFO implements ITechnicalStrings{
	
	/** Currently played track index*/
	private int index;
	
	/**Last played track*/
	private StackItem itemLast;
	
	/**Fifo itself, contains jajuk File objects */
	private volatile ArrayList alFIFO;
	
	/**Planned tracks */
	private volatile ArrayList alPlanned;
	
	/**Stop flag**/
	private static volatile boolean bStop = false;
	
	/**Self instance*/
	static private FIFO fifo= null; 	
	
	/** Current file position (%) used for pause */
	private int iPosition;
	
	/** First played file flag**/
	private static boolean bFirstFile = true;
	
	/**First file should seek to position flag*/
	private boolean bSeekFirstFile = false;
	
	/**UI reinit flag for perfs, avoid to reinit at each heart beat*/
	private boolean bZero = false;
	
	/**
	 * Singleton access
	 * @return
	 */
	public  static FIFO getInstance(){
		if (fifo == null){
			fifo = new FIFO();
		}
		return fifo;
	}
	
	/**
	 * constructor
	 */
	private FIFO() {
		reset();
	}
	
	/**
	 * Initialisation
	 */
	private void reset(){
		alFIFO = new ArrayList(50);
		alPlanned = new ArrayList(10);
		JajukTimer.getInstance().reset();
		index = 0;
		itemLast = null;
		//register needed events
		ObservationManager.notify(EVENT_ZERO);
	}
	
	/**
	 * Set given repeat mode to all
	 * @param bRepeat
	 */
	public synchronized void setRepeatModeToAll(boolean bRepeat){
		Iterator it = alFIFO.iterator();
		while ( it.hasNext()){
			StackItem item = (StackItem)it.next();
			item.setRepeat(bRepeat);
		}
	}
	
	/**
	 * Asynchronous version of push (needed to perform long-task out of awt dispatcher thread)
	 * @param alItems
	 * @param bAppend
	 */
	public void push(final ArrayList alItems, final boolean bAppend) {
		new Thread(){ //do it in a thread to make UI more reactive
			public void run(){
				try{
					pushCommand(alItems,bAppend);
				}
				catch(Exception e){
					Log.error(e);
				}
			}
		}.start();
	}
	
	/**
	 * Asynchronous version of push (needed to perform long-task out of awt dispatcher thread)
	 * @param item
	 * @param bAppend
	 */
	public void push(final StackItem item, final boolean bAppend) {
		new Thread(){ //do it in a thread to make UI more reactive
			public void run(){
				try{
					pushCommand(item,bAppend);
				}
				catch(Exception e){
					Log.error(e);
				}
			}
		}.start();
	}
	
	
	/**
	 * Push some stack items in the fifo
	 * @param alItems, list of items  to be played
	 * @param bAppend keep previous files or stop them to start a new one ?
	 */
	private synchronized void pushCommand( ArrayList alItems,  boolean bAppend) {
		//wake up FIFO if stopped
		bStop = false;
		//first try to mount needed devices
		Iterator it = alItems.iterator();
		StackItem item = null;
		while (it.hasNext()){
			item = (StackItem)it.next();
			if (item == null){
				it.remove();
				break;
			}
			if ( item.getFile().getDirectory()!=null && !item.getFile().getDirectory().getDevice().isMounted()){  //file is null if it is a BasicFile
				//not mounted, ok let them a chance to mount it:
				String sMessage = Messages.getString("Error.025")+" ("+item.getFile().getDirectory().getDevice().getName()+Messages.getString("FIFO.4"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				int i = JOptionPane.showConfirmDialog(Main.getWindow(),sMessage,Messages.getString("Warning"),JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
				if ( i == JOptionPane.YES_OPTION){
					try{
						item.getFile().getDirectory().getDevice().mount();
					}
					catch(Exception e){
						it.remove();
						Log.error(e);
						Messages.showErrorMessage("011",item.getFile().getDirectory().getDevice().getName()); //$NON-NLS-1$
						return;
					}
				}
				else{
					it.remove();
					return;
				}
			}
		}
		//test if we have yet some files to concidere
		if ( alItems.size() == 0){
			return;
		}
		//ok, stop current track if no append
		if (!bAppend) {
			Player.stop();
			clear();
			JajukTimer.getInstance().reset();
		}
		//add required tracks in the FIFO
		it = alItems.iterator();
		while (it.hasNext()){
			item = (StackItem)it.next();
			if (item.isUserLaunch()){ 
				item.getFile().getTrack().setRate(item.getFile().getTrack().getRate()+2); //inc rate by 2 because it is explicitely selected to be played by user
				FileManager.setRateHasChanged(true); //alert bestof playlist something changed
			}
			//Apply contextual repeat mode but only for concecutive repeat tracks : we can't have a whole between repeated tracks and first track must be repeated
			if (ConfigurationManager.getBoolean(CONF_STATE_REPEAT)){
				//check if last in fifo is repeated
				if (getLast() == null){ //this item will be the first 
					item.setRepeat(true);
				}
				else{ //there are yet some tracks in fifo
					if (getLast().isRepeat()){
						item.setRepeat(true);
					}
					else{
						item.setRepeat(false);	
					}
				}
			}//else, can be repeat (forced repeat) or not
			alFIFO.add(item);
			JajukTimer.getInstance().addTrackTime(item.getFile());
		}
		//lauch track if required
		if (!bAppend || !Player.isPlaying()){ //if we have a play or nothing is playing
			index = 0;
			launch(index);
		}
		// computes planned tracks
		computesPlanned(true);
	}
	
	
	/**
	 * Push some files in the fifo
	 * @param item, item to be played
	 * @param bAppend keep previous files or stop them to start a new one ?
	 */
	private synchronized void pushCommand(StackItem item, boolean bAppend) {
		ArrayList alFiles = new ArrayList(1);
		alFiles.add(item);
		pushCommand(alFiles,bAppend);
	}
	
	/**
	 * Finished method, called by the PlayerImpl when the track is finished
	 *
	 */
	public synchronized void finished(){
		if (getCurrentItem() == null ){
			return;
		}
		if (getCurrentItem().isRepeat()){ //if the track was in repeat mode, don't remove it from the fifo, just inc index
			//find the next item is repeat mode if any
			if ( index+1 < alFIFO.size()){
				StackItem itemNext = (StackItem)alFIFO.get(index+1);
				if (itemNext.isRepeat()){ //if next track is repeat, inc index
					index ++;
				}
				else{ //no next track in repeat mode, come back to first element in fifo
					index = 0;
				}
			}
			else{ //no next element
				index = 0; //come back to first element
			}
		}
		else if( index < alFIFO.size() ){  //current track was not in repeat mode, remove it from fifo
			StackItem item = (StackItem)alFIFO.get(index);
			JajukTimer.getInstance().removeTrackTime(item.getFile()); 
			alFIFO.remove(index);    //remove this file from fifo
		}
		if ( alFIFO.size() == 0){ //nothing more to play
		    if ( ConfigurationManager.getBoolean(CONF_STATE_CONTINUE) && itemLast != null){ //check if we are in continue mode
				File file = null;
				if (alPlanned.size() != 0){ //if some tracks are planned (can be 0 if planned size=0)
					file = ((StackItem)alPlanned.get(0)).getFile();
					alPlanned.remove(0); //remove the planned track
				}
				else{ //otherwise, take next track from file manager
					file = FileManager.getNextFile(itemLast.getFile());  //take next availble file
				}
				if ( file != null){
					pushCommand(new StackItem(file),false); //push it, it will be played
				}
			}
			else{ //no ? just reset UI and leave
				JajukTimer.getInstance().reset();
				ObservationManager.notify(EVENT_ZERO);
				return;
			}
		}
		else{ //something more in FIFO
			launch(index);
		}
		//computes planned tracks
		computesPlanned(false);
	}
	
	/**
	 * Lauch track at given index in the fifo
	 * @param int index
	 */
	private synchronized void launch(int index){
		try{
		    Util.waiting();
			//intro workaround : intro mode is only read at track launch and can't be set during the play
			boolean bIntroEnabled = ConfigurationManager.getBoolean(CONF_STATE_INTRO); //re-read intro mode
			ObservationManager.notify(EVENT_PLAYER_PLAY);  //notify to devices like commandJPanel to update ui when the play button has been pressed
			ConfigurationManager.setProperty(CONF_STATE_WAS_PLAYING,TRUE);  //	set was playing state
			long lOffset = 0; //track offset in secs
			File fCurrent = getCurrentFile();
			if ( itemLast == null || //first track, display cover 
					(ConfigurationManager.getBoolean(CONF_COVERS_SHUFFLE) && ConfigurationManager.getBoolean(CONF_COVERS_CHANGE_AT_EACH_TRACK)) //change cover at each track in shuffle cover mode ? 
					||(itemLast != null && !itemLast.getFile().getDirectory().equals(fCurrent.getDirectory())) ){  //if we are always in the same directory, just leave to save cpu
				ObservationManager.notify(EVENT_COVER_REFRESH); //request update cover 
			}
			itemLast = (StackItem)getCurrentItem().clone(); //save the last played track
			Log.debug("Now playing :"+fCurrent); //$NON-NLS-1$
			//Send an event that a track has been launched
			Properties pDetails = new Properties();
			pDetails.put(DETAIL_CURRENT_FILE_ID,fCurrent.getId());
			pDetails.put(DETAIL_CURRENT_DATE,new Long(System.currentTimeMillis()));
			ObservationManager.notify(EVENT_FILE_LAUNCHED,pDetails);
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
			//add hits number
			fCurrent.getTrack().incHits();  //inc hits number 
			fCurrent.getTrack().incSessionHits();//inc session hits
			fCurrent.getTrack().setRate(fCurrent.getTrack().getRate()+1); //inc rate by 1 because it is played
			FileManager.setRateHasChanged(true);
		} catch (Exception e) {
			Log.error("122", e); //$NON-NLS-1$
		}
	}
	
	
	/**
	 * Set current index
	 * @param index
	 */
	public synchronized void setIndex(int index){
		this.index =  index;
	}
	
	
	
	/**
	 * Computes planned tracks
	 *@param bClear : clear planned tracks stack  
	 */
	public synchronized void computesPlanned(boolean bClear){
		//Check if we are in continue mode and we have some tracks in FIFO, if not : no planned tracks
		if (!ConfigurationManager.getBoolean(CONF_STATE_CONTINUE) || alFIFO.size() == 0){
			alPlanned.clear();
			return;
		}
		if (bClear){
			alPlanned.clear();
		}
		int iPlannedSize = alPlanned.size();
		//  Add required tracks
		for (int i=0; i<(ConfigurationManager.getInt(CONF_OPTIONS_VISIBLE_PLANNED)-iPlannedSize);i++){
			StackItem item = null;
			StackItem siLast = null; //last item in fifo or planned
			// if planned stack contains yet some tracks
			if (alPlanned.size() > 0){
				siLast = (StackItem)alPlanned.get(alPlanned.size()-1); //last one
			}
			else if (alFIFO.size() > 0){ // if fifo contains yet some tracks to play
				siLast = (StackItem)alFIFO.get(alFIFO.size()-1); //last one
			}
			//if random mode, add shuffle tracks
			if (ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE)){
				item = new StackItem(FileManager.getShuffleFile(),false);
			}
			else{
				//if fifo contains yet some tracks to play
				if ( siLast != null){
					item = new StackItem(FileManager.getNextFile(siLast.getFile()),false);  
				}
				else{ //nothing in fifo, take first files in collection
					item = new StackItem((File)FileManager.getFiles().get(i),false);
				}
			}
			//Tell it is a planned item
			item.setPlanned(true);
			//add the new item
			alPlanned.add(item);
		}
	}
	
	/**
	 * Clears the fifo, for example when we want to add a group of files stopping previous plays
	 *
	 */
	public synchronized void clear() {
		alFIFO.clear();
		alPlanned.clear();
	}
	
	/**
	 * 
	 * @return whether the FIFO contains at least one track in repeat mode
	 */
	public synchronized boolean containsRepeat(){
		Iterator it = alFIFO.iterator();
		boolean bRepeat = false;
		while (it.hasNext()){
			StackItem item  = (StackItem)it.next();
			if (item.isRepeat()){
				bRepeat = true;
			}
		}
		return bRepeat;
	}
	
	
	/**
	 * 
	 * @return whether the FIFO contains only repeated files
	 */
	public synchronized boolean containsOnlyRepeat(){
		Iterator it = alFIFO.iterator();
		boolean bOnlyRepeat = true;
		while (it.hasNext()){
			StackItem item  = (StackItem)it.next();
			if (!item.isRepeat()){
				bOnlyRepeat = false;
				break;
			}
		}
		return bOnlyRepeat;
	}
	
	/**
	 * Play previous track
	 */
	public synchronized void playPrevious(){
		StackItem itemFirst = getItem(0);
		if ( itemFirst != null){
			if (index > 0){ //if we have some repeat files
				index --;
				launch(index);
			}
			else{ //we are at the first position
				if (itemFirst.isRepeat()){ //restart last repeated item in the loop
					index = getLastRepeatedItem();
					launch(index);
				}
				else{ //first is not repeated, just insert previous file from collection 
					StackItem item = new StackItem(FileManager.getPreviousFile(itemLast.getFile()),
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true);
						Player.stop();
					JajukTimer.getInstance().reset();
					alFIFO.add(0,item);
					JajukTimer.getInstance().addTrackTime(alFIFO);
					launch(0);
				}
			}
		}
	}
	
	/**
	 * Play next track in selection
	 */
	public synchronized void playNext(){
		//if playing, stop current
		if ( Player.isPlaying()){
			Player.stop();
		}
		//force a finish to current track if any
		if ( getCurrentFile() != null){  //if stopped, nothing to stop
			finished(); //stop current track 
		}
		else if (itemLast !=null ){ //try to launch any previous file
		    pushCommand(itemLast,false);
		}
		else{ //really nothing? play a shuffle track from collection
		    pushCommand(new StackItem(FileManager.getShuffleFile(),
		            ConfigurationManager.getBoolean(CONF_STATE_REPEAT),false),false);
		}
	}
	
	
	
	/**
	 *  Get the currently played  file
	 * @return File
	 **/
	public synchronized File getCurrentFile(){ 
		StackItem item = getCurrentItem();
		return (item==null)?null:item.getFile();
	}
	
	/**
	 *  Get the currently played  stack item
	 * @return stack item
	 **/
	public synchronized StackItem getCurrentItem(){ 
		if (index < alFIFO.size()){
			StackItem item = (StackItem)alFIFO.get(index);
			return item;
		}
		else{
			return null;
		}
	}
	
	
	/**
	 *  Get an item at given index in FIFO
	 * @param index : index
	 * @return stack item
	 **/
	public synchronized StackItem getItem(int index){ 
		if (index < alFIFO.size()){
			StackItem item = (StackItem)alFIFO.get(index);
			return item;
		}
		else{
			return null;
		}
	}
	
	/**
	 *  Get index of the last repeated item, -1 if none repeated 
	 * @return index
	 **/
	public synchronized int getLastRepeatedItem(){ 
		int i = -1;
		Iterator iterator = alFIFO.iterator();
		while (iterator.hasNext()){
			StackItem item = (StackItem)iterator.next();
			if (item.isRepeat()){
				i++;
			}
			else{
				break;
			}
		}
		return i;
	}
	
	
	
	/**
	 * Return true if none file is playing or planned to play for the given device
	 * @param device device to unmount
	 * @return
	 */
	public static boolean canUnmount(Device device){
		if ( fifo == null || !Player.isPlaying() || getInstance().getCurrentFile() == null){ //currently stopped
			return true;
		}
		if (getInstance().getCurrentFile().getDirectory().getDevice().equals(device)){ //is current track  on this device?
			return false;
		}
		Iterator it = getInstance().alFIFO.iterator(); //are next tracks in fifo on this device?
		while (it.hasNext()){
			StackItem item = (StackItem)it.next();
			File file = item.getFile();
			if ( file.getDirectory().getDevice().equals(device)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Stop request. Void the fifo
	 */
	public synchronized void stopRequest() {
		//fifo is over ( stop request ) , reinit labels in information panel before exiting
		bStop = true;
		//	set was playing state if it is not a stop called by jajuk exit 
		if (!Main.isExiting()){
			ConfigurationManager.setProperty(CONF_STATE_WAS_PLAYING,FALSE);
		}
		Player.stop();  //stop player
		reset();  //reinit all variables
		ObservationManager.notify(EVENT_PLAYER_STOP);  //notify to devices like commandJPanel to update ui
	}
	
	/**
	 * @return Returns the bStop.
	 */
	public static boolean isStopped() {
		return bStop;
	}
	
	/**
	 * @return Returns the alFIFO.
	 */
	public synchronized ArrayList getFIFO() {
		return alFIFO;
	}
	
	/**
	 * Shuffle the FIFO, used when user select the Random mode
	 */
	public synchronized void shuffle(){
		Collections.shuffle(alFIFO);
		alPlanned.clear(); //force recomputes planned tracks
	}
	
	/**
	 * Insert a file to play in FIFO at specified position
	 * @param file
	 * @param iPos
	 */
	public synchronized void insert(StackItem item,int iPos){
		ArrayList alStack = new ArrayList(1);
		alStack.add(item);
		insert(alStack,iPos);
	}

	
	/**
	 * Insert a file at specified position
	 * @param file
	 * @param iPos
	 */
	public synchronized void insert(ArrayList alFiles,int iPos){
		if (iPos < alFIFO.size()){  //add in the FIFO
			alFIFO.addAll(iPos,alFiles);
			JajukTimer.getInstance().addTrackTime(alFiles);
		}
		else{ //planned track
			alPlanned.addAll(iPos-alFIFO.size(),alFiles);
		}
	}
	
	/**
	 * Put up an item from given index to index-1
	 * @param index
	 */
	public synchronized void up(int index){
	    if (index == 0 || index == alFIFO.size()){ //Can't put up first track in queue or first planned track. This should be already made by ui behavior
	        return;
	    }
	    if (index < alFIFO.size()){
	        StackItem item = (StackItem)alFIFO.get(index);
	        alFIFO.remove(index); //remove the item
	        alFIFO.add(index-1,item); //add it again above
	    }
	    else{ //planned track
	        StackItem item = (StackItem)alPlanned.get(index-alFIFO.size());
	        alFIFO.remove(index-alFIFO.size()); //remove the item
	        alFIFO.add(index-alFIFO.size()-1,item); //add it again above
	    }
	}

	
	/**
	 * Put down an item from given index to index+1
	 * @param index
	 */
	public synchronized void down(int index){
	    if (index == 0 || index == alFIFO.size()-1 || index == alFIFO.size() + alPlanned.size() -1){ //Can't put down current track, nor last rack in fifo, nor last planned track. This should be already made by ui behavior
	        return;
	    }
	    if (index < alFIFO.size()){
	        StackItem item = (StackItem)alFIFO.get(index);
	        alFIFO.remove(index); //remove the item
	        alFIFO.add(index+1,item); //add it again above
	    }
	    else{ //planned track
	        StackItem item = (StackItem)alPlanned.get(index-alFIFO.size());
	        alFIFO.remove(index-alFIFO.size()); //remove the item
	        alFIFO.add((index-alFIFO.size())+1,item); //add it again above
	    }
	}

	/**
	 * Go to given index and lauch it
	 * @param index
	 */
	public synchronized void goTo(int index){
		if (containsRepeat()){
			// if there are some tracks in repeat, mode
			if (getItem(index).isRepeat()){ //the selected line is in repeat mode, ok, keep repeat mode and just change index
				this.index = index;
			}
			else{ //the selected line was not a repeated item, take it as a which to reset repeat mode
				setRepeatModeToAll(false);
				Properties properties = new Properties();
				properties.put(DETAIL_SELECTION,FALSE);
				ObservationManager.notify(EVENT_REPEAT_MODE_STATUS_CHANGED,properties);
				remove(0,index-1);
				index = 0;
			}
		}
		else{
			remove(0,index-1);
			index = 0;
		}
		launch(index);
	}
	
	/**
	 * Remove files at specified positions
	 * @param start index
	 * @param stop index
	 */
	public synchronized void remove(int iStart,int iStop){
		if (iStart<=iStop && iStart>=0 && iStop < alFIFO.size()+alPlanned.size()){ //check size
			//drop items from the end to the begining
			for (int i=iStop; i>=iStart; i--){
				//FIFO items
				if (i >= alFIFO.size()){
					alPlanned.remove(i-alFIFO.size());    //remove this file from plan
					computesPlanned(false); //complete missing planned tracks
				}
				else{  //planned items
					StackItem item = (StackItem)alFIFO.get(i);
					JajukTimer.getInstance().removeTrackTime(item.getFile()); 
					alFIFO.remove(i);    //remove this file from fifo
					computesPlanned(true);  //Recomputes all planned tracks from last file in fifo 
				}
			}
			
		}
	}
	
	/**
	 * Computes next file to play given current option configuration and FIFO
	 * @return the file to play
	 */
	private File nextTrack(){
		File file = null;
		//next file choice
		file = FileManager.getNextFile(itemLast.getFile());
		return file;
	}
	
	/**
	 * 
	 * @return Last Stack item in FIFO
	 */
	public synchronized StackItem getLast(){
		if (alFIFO.size() == 0){
			return null;
		}
		return (StackItem)alFIFO.get(alFIFO.size()-1);
	}
	
	/**
	 * @return Returns the index.
	 */
	public synchronized int getIndex() {
		return index;
	}
	/**
	 * @return Returns the alPlanned.
	 */
	public synchronized ArrayList getPlanned() {
		return alPlanned;
	}
}
