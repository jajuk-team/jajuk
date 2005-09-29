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

package org.jajuk.base;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.jajuk.i18n.Messages;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.error.JajukException;

/**
 * Convenient class to manage Tracks
 * @author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class TrackManager extends ItemManager implements Observer{
    /**Self instance*/
    private static TrackManager singleton;
   
	/**
	 * No constructor available, only static access
	 */
	private TrackManager() {
		super();
         //---register properties---
        //ID
        registerProperty(new PropertyMetaInformation(XML_ID,false,true,false,false,false,String.class,null,null));
        //Name
        registerProperty(new PropertyMetaInformation(XML_NAME,false,true,true,true,false,String.class,null,null));
		//Album
        registerProperty(new PropertyMetaInformation(XML_ALBUM,false,true,true,true,true,String.class,null,null));
        //Style
        registerProperty(new PropertyMetaInformation(XML_STYLE,false,true,true,true,true,String.class,null,null));
        //Author
        registerProperty(new PropertyMetaInformation(XML_AUTHOR,false,true,true,true,true,String.class,null,null));
        //Length
        registerProperty(new PropertyMetaInformation(XML_TRACK_LENGTH,false,true,true,false,false,Long.class,null,null));
        //Type
        registerProperty(new PropertyMetaInformation(XML_TRACK_TYPE,false,true,true,false,false,Long.class,null,null));
        //Year
        registerProperty(new PropertyMetaInformation(XML_TRACK_YEAR,false,true,true,true,true,Long.class,null,0));
        //Rate
        registerProperty(new PropertyMetaInformation(XML_TRACK_RATE,false,false,true,true,true,Long.class,null,0));
        //Files
        registerProperty(new PropertyMetaInformation(XML_FILES,false,false,true,false,false,String.class,null,null));
        //Hits
        registerProperty(new PropertyMetaInformation(XML_TRACK_HITS,false,false,true,false,false,Long.class,null,0));
        //Addition date
        registerProperty(new PropertyMetaInformation(XML_TRACK_ADDED,false,false,true,false,false,Date.class,new SimpleDateFormat(ADDITION_DATE_FORMAT),null));
        //Comment
        registerProperty(new PropertyMetaInformation(XML_TRACK_COMMENT,false,false,true,true,true,String.class,null,null));
        //Track order
        registerProperty(new PropertyMetaInformation(XML_TRACK_ORDER,false,false,true,true,false,Long.class,null,null));
        //---subscriptions---
        ObservationManager.register(EVENT_FILE_NAME_CHANGED,this);
	}
    
    /**
     * @return singleton
     */
    public static TrackManager getInstance(){
      if (singleton == null){
          singleton = new TrackManager();
      }
        return singleton;
    }

	/**
	 * Register an Track
	 * 
	 * @param sName
	 */
	public synchronized Track registerTrack(String sName, Album album, Style style, Author author, long length, long lYear, Type type) {
		String sId = MD5Processor.hash(style.getName() + author.getName() +album.getName() + lYear + length + type.getName() + sName);
		return registerTrack(sId, sName, album, style, author, length, lYear, type);
	}
    
    /**
     * Register an Track with a known id
     * 
     * @param sName
     */
    public synchronized Track registerTrack(String sId, String sName, Album album, Style style, Author author, long length, long lYear, Type type) {
        if (!hmItems.containsKey(sId)) {
            Date dAdditionDate = new Date();
            Track track = new Track(sId, sName, album, style, author, length, lYear, type);
            track.setAdditionDate(dAdditionDate);
            hmItems.put(sId, track);
            return track;
        }
        else{
            return (Track)hmItems.get(sId);
        }
    }
    
    /**
     * Change a track album 
     * @param old track
     * @param new album name
     * @return new track
     *
     */
    public synchronized Track changeTrackAlbum(Track track,String sNewAlbum)  throws JajukException{
        //register the new album
    	Album newAlbum = AlbumManager.getInstance().registerAlbum(sNewAlbum);
        //reset previous properties like exp
    	newAlbum.cloneProperties(track.getAlbum());
        Track newTrack = registerTrack(track.getName(),newAlbum,track.getStyle(),track.getAuthor(),track.getLength(),
        		track.getYear(),track.getType());
        //re apply old properties from old item
        newTrack.cloneProperties(track);
        //Reset files property before adding new files
        newTrack.removeProperty(XML_FILES);
       //change tag in files
        Iterator it = track.getFiles().iterator();
        while (it.hasNext()){
            File file = (File)it.next();
            file.setTrack(newTrack);
            newTrack.addFile(file);
            Tag tag = new Tag(file.getIO());
            tag.setAlbumName(newAlbum.getName2());
            tag.commit();
        }
        TrackManager.getInstance().restorePropertiesAfterRefresh(newTrack);
        remove(track.getId());//remove old reference
    	AlbumManager.getInstance().cleanup(track.getAlbum()); //remove this album if no more references
    	return newTrack;
    }
    
 /**
     * Change a track author 
     * @param old track
     * @param new author name
     * @return new track
     */
    public synchronized Track changeTrackAuthor(Track track,String sNewAuthor)  throws JajukException{
        //register the new item
        Author newAuthor = AuthorManager.getInstance().registerAuthor(sNewAuthor);
        //reset previous properties like exp
        newAuthor.cloneProperties(track.getAuthor());
        Track newTrack = registerTrack(track.getName(),track.getAlbum(),track.getStyle(),
            newAuthor,track.getLength(),track.getYear(),track.getType());
        //re apply old properties from old item
        newTrack.cloneProperties(track);
        //Reset files property before adding new files
        newTrack.removeProperty(XML_FILES);
       //change tag in files
        Iterator it = track.getFiles().iterator();
        while (it.hasNext()){
            File file = (File)it.next();
            file.setTrack(newTrack);
            newTrack.addFile(file);
            Tag tag = new Tag(file.getIO());
            tag.setAuthorName(newAuthor.getName2());
            tag.commit();
        }
        TrackManager.getInstance().restorePropertiesAfterRefresh(newTrack);
        remove(track.getId());//remove old reference
        AuthorManager.getInstance().cleanup(track.getAuthor()); //remove this item if no more references
        return newTrack;
    }

     /**
     * Change a track style 
     * @param old item
     * @param new item name
     * @return new track
     */
    public synchronized Track changeTrackStyle(Track track,String sNewItem) throws JajukException{
        //register the new item
        Style newStyle = StyleManager.getInstance().registerStyle(sNewItem);
        //reset previous properties like exp
        newStyle.cloneProperties(track.getAuthor());
        Track newTrack = registerTrack(track.getName(),track.getAlbum(),newStyle,
            track.getAuthor(),track.getLength(),track.getYear(),track.getType());
        //re apply old properties from old item
        newTrack.cloneProperties(track);
        //Reset files property before adding new files
        newTrack.removeProperty(XML_FILES);
       //change tag in files
        Iterator it = track.getFiles().iterator();
        while (it.hasNext()){
            File file = (File)it.next();
            file.setTrack(newTrack);
            newTrack.addFile(file);
            Tag tag = new Tag(file.getIO());
            tag.setStyleName(newStyle.getName2());
            tag.commit();
        }
        TrackManager.getInstance().restorePropertiesAfterRefresh(newTrack);
        remove(track.getId());//remove old reference
        StyleManager.getInstance().cleanup(track.getStyle()); //remove this item if no more references
        return newTrack;
    }
    
    /**
     * Change a track style 
     * @param old item
     * @param new item name
     * @return new track or null if wronf format
     */
    public synchronized Track changeTrackYear(Track track,long lNewItem)  throws JajukException{
        if (lNewItem <0 || lNewItem > 10000){ //jajuk supports years till year 10000 !
            Messages.showErrorMessage("137");
            throw new JajukException("137");
        }
        Track newTrack = registerTrack(track.getName(),track.getAlbum(),track.getStyle(),
                track.getAuthor(),track.getLength(),lNewItem,track.getType());
        //re apply old properties from old item
        newTrack.cloneProperties(track);
        //Reset files property before adding new files
        newTrack.removeProperty(XML_FILES);
        //change tag in files
        Iterator it = track.getFiles().iterator();
        while (it.hasNext()){
            File file = (File)it.next();
            file.setTrack(newTrack);
            newTrack.addFile(file);
            Tag tag = new Tag(file.getIO());
            tag.setYear(lNewItem);
            tag.commit();
        }
        TrackManager.getInstance().restorePropertiesAfterRefresh(newTrack);
        remove(track.getId());//remove old reference
        return newTrack;
    }
    
     /**
     * Change a track comment 
     * @param old item
     * @param new item name
     * @return new track or null if wronf format
     */
    public synchronized Track changeTrackComment(Track track,String sNewItem)  throws JajukException{
       track.setComment(sNewItem);
        //change tag in files
        Iterator it = track.getFiles().iterator();
        while (it.hasNext()){
            File file = (File)it.next();
            Tag tag = new Tag(file.getIO());
            tag.setComment(sNewItem);
            tag.commit();
        }
        return track;
    }
    
    
      /**
     * Change a track comment 
     * @param old item
     * @param new item name
     * @return new track or null if wrong format
     */
    public synchronized Track changeTrackRate(Track track,long lNew)  throws JajukException{
        //check format
        if (lNew <0 ){
            Messages.showErrorMessage("137");
            throw new JajukException("137");
        }
        track.setRate(lNew);
        return track;
    }
    
     /**
     * Change a track order 
     * @param old item
     * @param new item order
     * @return new track or null if wronf format
     */
    public synchronized Track changeTrackOrder(Track track,long lNewOrder) throws JajukException{
        //check format
        if (lNewOrder <0){
            Messages.showErrorMessage("137");
            return null;
        }
        track.setOrder(lNewOrder);
        //change tag in files
        Iterator it = track.getFiles().iterator();
        while (it.hasNext()){
            File file = (File)it.next();
            Tag tag = new Tag(file.getIO());
            tag.setOrder(lNewOrder);
            tag.commit();
        }
        return track;
    }
    
     /**
     * Change a track name 
     * @param old item
     * @param new item name
     * @return new track
     */
    public synchronized Track changeTrackName(Track track,String sNewItem) throws JajukException{
        Track newTrack = registerTrack(sNewItem,track.getAlbum(),track.getStyle(),
            track.getAuthor(),track.getLength(),track.getYear(),track.getType());
        //re apply old properties from old item
        newTrack.cloneProperties(track);
       //change tag in files
        Iterator it = track.getFiles().iterator();
        while (it.hasNext()){
            File file = (File)it.next();
            file.setTrack(newTrack);
            newTrack.addFile(file);
            Tag tag = new Tag(file.getIO());
            tag.setTrackName(newTrack.getName());
            tag.commit();
        }
        TrackManager.getInstance().restorePropertiesAfterRefresh(newTrack);
        remove(track.getId());//remove old reference
        return newTrack;
    }
    
	/**
	 * Perform a track cleanup : delete useless items
	 *  
	 */
	public synchronized void cleanup() {
		Iterator itTracks = hmItems.values().iterator();
		while (itTracks.hasNext()) {
			Track track = (Track) itTracks.next();
			if ( track.getFiles().size() == 0){ //no associated file
				itTracks.remove();
				continue;
			}
			Iterator itFiles = track.getFiles().iterator();
			while (itFiles.hasNext()) {
				org.jajuk.base.File file = (org.jajuk.base.File) itFiles.next();
				if ((File)FileManager.getInstance().getItem(file.getId()) == null) { //test if the file exists in the main file repository
					itFiles.remove();//no? remove it from the track
				}
			}
			if (track.getFiles().size() == 0) { //the track don't map anymore to any physical item, just remove it
				itTracks.remove();
			}
		}
	}

	
	/** Return sorted registred Tracks */
		public synchronized ArrayList getSortedTracks() {
			ArrayList alTracks = new ArrayList(hmItems.values());
			Collections.sort(alTracks);
			return alTracks;
		}


	/**
	 * Format the tracks names to be normalized :
	 * <p>
	 * -no underscores or other non-ascii characters
	 * <p>
	 * -no spaces at the begin and the end
	 * <p>
	 * -All in lower cas expect first letter of first word
	 * <p>
	 * exemple: "My track title"
	 * 
	 * @param sName
	 * @return
	 */
	private synchronized String format(String sName) {
		String sOut;
		sOut = sName.trim(); //supress spaces at the begin and the end
		sOut.replace('-', ' '); //move - to space
		sOut.replace('_', ' '); //move _ to space
		char c = sOut.charAt(0);
		sOut = sOut.toLowerCase();
		StringBuffer sb = new StringBuffer(sOut);
		sb.setCharAt(0, Character.toUpperCase(c));
		return sb.toString();
	}
    
    /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_TRACKS;
    }    
      
    /* (non-Javadoc)
     * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
     */
    public void update(Event event) {
        String subject = event.getSubject();
        if (EVENT_FILE_NAME_CHANGED.equals(subject)){
            Properties properties = event.getDetails();
            File fNew = (File)properties.get(DETAIL_NEW);
            File fileOld = (File)properties.get(DETAIL_OLD);
            Track track =fileOld.getTrack(); 
            track.removeFile(fileOld);
            track.addFile(fNew);
        }
    }
    
    /**
     * Get all tracks associated with this track
     * @param item
     * @return
     */
    public ArrayList  getAssociatedTracks(IPropertyable item){
        ArrayList alOut = new ArrayList(100);
        Iterator it = hmItems.values().iterator();
        while (it.hasNext()){ //scan each track
            Track track = (Track)it.next();
            if ( (item instanceof Album &&  track.getAlbum().equals(item))
                    || (item instanceof Author &&  track.getAuthor().equals(item))
                    || (item instanceof Style &&  track.getStyle().equals(item)) ){
                alOut.add(track);
            }
        }
        return alOut;
    }
    
}