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
    private File fLastOne;
    
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
    
    /**Has previous  been pressed?*/
    private boolean bPrevious = false;
    
    /**UI reinit flag for perfs, avoid to reinit at each heart beat*/
    private boolean bZero = false;
    
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
        fLastOne = null;
        //register needed events
        ObservationManager.notify(EVENT_ZERO);
    }
    
    /**
     * Set given repeat mode to all
     * @param bRepeat
     */
    public void setRepeatModeToAll(boolean bRepeat){
        Iterator it = alFIFO.iterator();
        while ( it.hasNext()){
            StackItem item = (StackItem)it.next();
            item.setRepeat(bRepeat);
        }
    }
    
    /**
     * Push some stack items in the fifo
     * @param alItems, list of items  to be played
     * @param bAppend keep previous files or stop them to start a new one ?
     */
    public void push(ArrayList alItems, boolean bAppend) {
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
        //test if we have yet some files to play
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
            if (ConfigurationManager.getBoolean(CONF_STATE_REPEAT) && getLast() != null && getLast().isRepeat()){
                item.setRepeat(true);
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
        computesPlanned(false);
    }
    
    
    /**
     * Push some files in the fifo
     * @param item, item to be played
     * @param bAppend keep previous files or stop them to start a new one ?
     */
    public  void push(StackItem item, boolean bAppend) {
        ArrayList alFiles = new ArrayList(1);
        alFiles.add(item);
        push(alFiles,bAppend);
    }
    
    /**
     * Finished method, called by the PlayerImpl when the track is finished
     *
     */
    public  void finished(){
        if (getCurrentItem() != null && getCurrentItem().isRepeat()){ //if the track was in repeat mode, don't remove it from the fifo, just inc index
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
            remove(index,index); //remove the track from fifo
        }
        if ( alFIFO.size() == 0){ //nothing more to play
            if ( ConfigurationManager.getBoolean(CONF_STATE_CONTINUE) && fLastOne != null){ //check if we are in continue mode
                File file = FileManager.getNextFile(fLastOne);  //take next availble file
                if ( file != null){
                    push(new StackItem(file),false); //push it, it will be played
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
    private  void launch(int index){
        try{
            Util.waiting();
            //intro workaround : intro mode is only read at track launch and can't be set during the play
            boolean bIntroEnabled = ConfigurationManager.getBoolean(CONF_STATE_INTRO); //re-read intro mode
            ObservationManager.notify(EVENT_PLAYER_PLAY);  //notify to devices like commandJPanel to update ui when the play button has been pressed
            ConfigurationManager.setProperty(CONF_STATE_WAS_PLAYING,TRUE);  //	set was playing state
            long lOffset = 0; //track offset in secs
            File fCurrent = getCurrentFile();
            if ( fLastOne == null || //first track, display cover 
                    (ConfigurationManager.getBoolean(CONF_COVERS_SHUFFLE) && ConfigurationManager.getBoolean(CONF_COVERS_CHANGE_AT_EACH_TRACK)) //change cover at each track in shuffle cover mode ? 
                    ||(fLastOne != null && !fLastOne.getDirectory().equals(fCurrent.getDirectory())) ){  //if we are always in the same directory, just leave to save cpu
                ObservationManager.notify(EVENT_COVER_REFRESH); //request update cover 
            }
            fLastOne = (File)fCurrent.clone(); //save the last played track
            bPrevious = false; //allow insert to be done with right previous file
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
    public void setIndex(int index){
        this.index =  index;
    }
    
    
    
    /**
     * Computes planned tracks
     *@param bClear : clear planned tracks stack  
     */
    public void computesPlanned(boolean bClear){
        //Check if we are in continue mode, if not, no planned tracks
        if (!ConfigurationManager.getBoolean(CONF_STATE_CONTINUE)){
            alPlanned.clear();
            return;
        }
        if (bClear){
            alPlanned.clear();
        }
        //Do we need a new planned item? 
        int iNbVisible = 0; //count number of visible tracks
        Iterator it = alPlanned.iterator();
        while (it.hasNext()){
            StackItem item = (StackItem)it.next();
            if (item.isVisible()){
                iNbVisible ++;
            }
        }
        //  Add required tracks
        for (int i=0; i<(ConfigurationManager.getInt(CONF_OPTIONS_VISIBLE_PLANNED)-iNbVisible);i++){
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
    public  void clear() {
        alFIFO.clear();
        alPlanned.clear();
    }
    
    /**
     * 
     * @return whether the FIFO contains at least one track in repeat mode
     */
    public boolean containsRepeat(){
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
        //if playing, stop current
        if ( Player.isPlaying()){
            Player.stop();
        }
        //force a finish to current track if any
        if ( getCurrentFile() != null){  //if stopped, nothing to stop
            finished(); //stop current track 
        }
    }
    
    
    
    
    /**
     * Play next album in selection
     
     
     REFACTOR / TBI
     public synchronized void playNextAlbum(){
     bNext = true;
     if ( fCurrent != null){  //if stopped, nothing to stop
     Album albumCurrent = fCurrent.getTrack().getAlbum(); //get current track album
     Player.stop();
     //remove next files in the same album
      Iterator it = alFIFO.iterator();
      while (it.hasNext() ){
      File file = (File)it.next();
      if ( file!=null && file.getTrack().getAlbum().equals(albumCurrent)){
      it.remove(); //remove this file because it is in the same album than the current one
      //NBI	        lTotalTime -= file.getTrack().getLength();
       }
       else{
       break; //not the same album? leave
       }
       }
       finished(); //stop current track and let the FIFO to choose the next one
       }
       }*/
    
    
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
    public  StackItem getCurrentItem(){ 
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
    public  StackItem getItem(int index){ 
        if (index < alFIFO.size()){
            StackItem item = (StackItem)alFIFO.get(index);
            return item;
        }
        else{
            return null;
        }
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
            File file = (File)it.next();
            if ( file.getDirectory().getDevice().equals(device)){
                return false;
            }
        }
        //REFACTOR check both FIFO and Planned  
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
     * @param bImmediate immediate play ?
     * @param bKeepLast add again the last track ?
     */
    public  void insert(ArrayList alFiles,int iPos,boolean bImmediate,boolean bKeepLast){
        //	  ok, stop current track
        if (bImmediate) {
            Player.stop();
            JajukTimer.getInstance().reset();
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
            JajukTimer.getInstance().addTrackTime(file);
        }
        //add required tracks
        it = alFiles.iterator();
        int i = 0;
        while (it.hasNext()){
            file = (File)it.next();
            alFIFO.add(index+i,file);
            index++;
            JajukTimer.getInstance().addTrackTime(file);
            i++;
        }
    }
    
    /**
     * Go to given index and lauch it
     * @param index
     */
    public void goTo(int index){
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
     * Remove files in FIFO at specified positions
     * @param start index
     * @param stop index
     */
    public synchronized void remove(int iStart,int iStop){
        if (iStart<=iStop && iStart>=0 && iStop<alFIFO.size()){ //check size
            //drop items from the end to the begining
            for (int i=iStop; i>=iStart; i--){
                StackItem item = (StackItem)alFIFO.get(i);
                JajukTimer.getInstance().removeTrackTime(item.getFile()); 
                alFIFO.remove(i);    //remove this file from fifo
            }
            computesPlanned(true);
        }
    }
    
    /**
     * Computes next file to play given current option configuration and FIFO
     * @return the file to play
     */
    private File nextTrack(){
        File file = null;
        //next file choice
        file = FileManager.getNextFile(fLastOne);
        return file;
    }
    
    /**
     * 
     * @return Last Stack item in FIFO
     */
    public StackItem getLast(){
        if (alFIFO.size() == 0){
            return null;
        }
        return (StackItem)alFIFO.get(alFIFO.size()-1);
    }
    
    /**
     * @return Returns the index.
     */
    public int getIndex() {
        return index;
    }
    /**
     * @return Returns the alPlanned.
     */
    public ArrayList getPlanned() {
          return alPlanned;
    }
}
