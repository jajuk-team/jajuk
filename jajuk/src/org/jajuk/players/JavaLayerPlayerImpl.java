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

import java.io.File;
import java.util.Map;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import org.jajuk.base.FIFO;
import org.jajuk.base.TypeManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  My class description
 *
 * @author     Bertrand Florat
 * @created    12 oct. 2003
 */
public class JavaLayerPlayerImpl implements IPlayerImpl,ITechnicalStrings{
    
    /**Current player*/
    public static BasicPlayer player;
    /**Started */
    private volatile boolean bStarted = false;
    /**Stopped */
    private volatile boolean bStopped = false;
    /**Time elapsed in secs*/
    private static long lTime = 0; //must be static to be correctly accessed by the player listener inner class
    /**Date of last elapsed time update*/
    private static long lDateLastUpdate = System.currentTimeMillis(); 
    /**current track info*/
    private static Map mInfo;
    /**Current position in %*/
    private static float fPos;
    /**Length to be played in secs*/
    static long length;
    /**Stored Volume*/
    static float fVolume;
    /**Seeking flag*/
    static boolean bSeeking = false;
    /**Listener*/
    BasicPlayerListener bpListener =  new JajukBasicPlayerListener();
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPlayerImpl#play()
     */
    public synchronized void play(org.jajuk.base.File file,float fPosition,final long length,final float fVolume) throws Exception{
        try{
            JavaLayerPlayerImpl.fVolume = fVolume;
            JavaLayerPlayerImpl.length = length;
            bStarted = false;
            //create the player if needed
            if (player == null){
                player = new BasicPlayer();
                player.addBasicPlayerListener(bpListener); //set listener
                BasicPlayer.EXTERNAL_BUFFER_SIZE =  32*4000; //set a large buffer to avoid blanks
            }
            //make sure to stop any current player
            player.stop();
            player.open(new File(file.getAbsolutePath())); 
            Util.stopWaiting();
            if (fPosition < 0 && player!=null){  //-1 means we want to play entire file
                player.play();
            }
            else  if(player != null){
                int iFirstFrame = (int)(file.getTrack().getLength()*fPosition*41.666); // (position*fPosition(%))*1000(ms) /24 because 1 frame =24ms
                int iLastFrame = (int)(iFirstFrame+(length/24)); //length(ms)/24
                //test if this is a audio format supporting seeking
                if (Boolean.valueOf(TypeManager.getTypeByExtension(Util.getExtension(file.getIO())).getProperty(TYPE_PROPERTY_SEEK_SUPPORTED)).booleanValue()){
                    seek(fPosition);
                }
                player.play();
            }
        }
        catch(Exception e){  //in case of error, we must stop waiting cursor and set started to true to avoid deadlock in the stop method
            Log.error(e);
            bStarted = true;
            Util.stopWaiting();
            throw e;  //propagate to Player
        }
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPlayerImpl#stop()
     */
    public synchronized void stop() {
        if (player!= null ){
            try {
                player.stop();
            } catch (Exception e) {
                Log.error(e);
            }
        }	
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPlayerImpl#setVolume(float)
     */
    public synchronized void setVolume(float fVolume) throws Exception {
        JavaLayerPlayerImpl.fVolume = fVolume;
        if (player!=null){
            player.setGain(fVolume);
        }
    }
    
    
    /**
     * @return Returns the lTime in ms
     */
    public long getElapsedTime() {
        return lTime;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.players.IPlayerImpl#pause()
     */
    public synchronized void pause() throws Exception{
        if (player!=null){
            player.setGain(0.01f); //set this to avoid starnge sound
            player.pause();
        }
    }
    
    public synchronized void resume() throws Exception{
        if (player!=null){
            setVolume(fVolume); //reset right volume
            player.resume();
        }
    }	
    
    /* (non-Javadoc)
     * @see org.jajuk.players.IPlayerImpl#seek(float)
     * Ogg vorbis seek not yet supported
     */
    public void seek(float posValue) {
        bSeeking = true;
        try{	
            if (mInfo.containsKey("audio.type") && player!=null) { //$NON-NLS-1$
                String type = (String)mInfo.get("audio.type"); //$NON-NLS-1$
                // Seek support for MP3. and WAVE
                if ((type.equalsIgnoreCase("mp3") || type.equalsIgnoreCase("wave"))&& mInfo.containsKey("audio.length.bytes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    long skipBytes =	(long) Math.round(((Integer)mInfo.get("audio.length.bytes")).intValue()* posValue); //$NON-NLS-1$
                    player.seek(skipBytes);
                }
                else{
                    Messages.showErrorMessage("126"); //$NON-NLS-1$
                }
            }
        }
        catch(BasicPlayerException bpe){
            Log.error(bpe);
        }
    }
    
    /**
     * @return current position as a float ex: 0.2f 
     */
    public float getCurrentPosition(){
        return fPos;
    }
    
    /**
     * 
     * @return whether player is seeking
     */
    public boolean isSeeking(){
        return bSeeking;
    }
  
    
    /**
     * 
     *  Jajuk player listener
     *
     * @author     Bertrand Florat
     * @created    4 févr. 2005
     */
    class JajukBasicPlayerListener implements BasicPlayerListener{
        public void opened(Object arg0, Map arg1) {
            JavaLayerPlayerImpl.mInfo = arg1;
        }
        public void display(String msg){        
        }
        /**
         * Called several times by sec
         */
        public void progress(int iBytesread,long lMicroseconds,byte[] bPcmdata,java.util.Map mProperties) {
            if ( (System.currentTimeMillis()-lDateLastUpdate) > 900 ){ //update every 900 ms
                //test end of length
                if ( length != -1 && lMicroseconds/1000 >length && player != null){ //length=-1 means there is no max length
                    try {
                        player.stop();
                        FIFO.getInstance().finished();
                    } catch (BasicPlayerException e) {
                        Log.error(e);
                    }
                }
                //computes read time
                if (mInfo.containsKey("audio.length.bytes")) { //$NON-NLS-1$
                    int byteslength = ((Integer) mInfo.get("audio.length.bytes")).intValue(); //$NON-NLS-1$
                    fPos = (float)iBytesread / byteslength;
                    lTime = (long)(Util.getTimeLengthEstimation(mInfo)*fPos);
                }
                lDateLastUpdate = System.currentTimeMillis();
            }
        }
        
        public void stateUpdated(BasicPlayerEvent bpe) {
            Log.debug("Player state changed: "+bpe); //$NON-NLS-1$
            switch(bpe.getCode()){
            case BasicPlayerEvent.EOM:
                FIFO.getInstance().finished();
            break;
            case BasicPlayerEvent.STOPPED:
                bStopped = true;
            break;
            case BasicPlayerEvent.PLAYING:
                bStopped = false;
                bSeeking = false;
                bStarted = true;
                try{
                    if (player != null){ //can be null if user try to lauch many tracks by next, next...
                        player.setGain(fVolume);
                    }
                } catch (Exception e) {
                    //do nothing, sometimes throws null pointer exception for unknown reason
                }
                Util.stopWaiting(); //stop the waiting cursor
                break;
            }
        }
        
        public void setController(BasicController arg0) {
        }
  
    }
    
}
