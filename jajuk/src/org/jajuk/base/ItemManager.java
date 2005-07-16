/*
 *  Jajuk
 *  Copyright (C) 2005 Bertrand Florat
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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.xml.sax.Attributes;

/**
 *  Managers parent class
 *
 * @author     Bertrand Florat
 * @created    20 juin 2005
 */
public abstract class ItemManager implements ITechnicalStrings{

    /**Custom Properties -> format*/
   protected  LinkedHashMap properties;
    /** Map ids and properties, survives to a refresh, is used to recover old properties after refresh */
	protected LinkedHashMap hmIdProperties = new LinkedHashMap(1000);
    /**Items collection**/
    protected LinkedHashMap hmItems = new LinkedHashMap(100);
    /**
     * Constructor
     *
     */
    ItemManager(){
        properties = new LinkedHashMap();
    }
    
    /**
     * @return identifier used for XML generation
     */
    abstract public String getIdentifier();
    
    /**
     * 
     * @param sProperty
     * @return format for given property
     */
    public String getFormat(String sProperty){
        String sFormat = (String)properties.get(sProperty);
        if (sFormat == null){ //occurs for all base properties for ie
            return FORMAT_STRING; //TBI : write a smater method: some base properties are numbers, here it's enough because we just need to make sure it not a boolean
        }
        return sFormat;
    }
    
    /**
     * Restore properties after a refresh if possible
     * @param item
     * @param sId
     */
    public void restorePropertiesAfterRefresh(IPropertyable item,String sId){
	    LinkedHashMap properties = (LinkedHashMap)hmIdProperties.get(sId); 
		if ( properties == null){  //new file
			hmIdProperties.put(sId,item.getProperties());
		}
		else{  //reset properties before refresh
			item.setProperties(properties);
		}
    }
    
    /**
     * Add a property 
     * @param sProperty
     * @param sFormat
     */
    public void addProperty(String sProperty,String sFormat){
        properties.put(Util.formatXML(sProperty),Util.formatXML(sFormat)); //make sure to clean strings for XML compliance
    }
    
    /**Remove a property **/
    public void removeProperty(String sProperty){
        properties.remove(sProperty);
        applyRemoveProperty(sProperty); //remove ths property to all items
    }
    
    /**Add new property to all items for the given manager*/
    public void applyNewProperty(String sProperty){
        Collection items = getItems();
        if (items != null){
            Iterator it = items.iterator();
            while (it.hasNext()){
                IPropertyable item = (IPropertyable)it.next();
                //just initialize void fields
                if (item.getValue(sProperty) != null){
                	continue;
                }
                String sValue = "";
                if (getFormat(sProperty).equals(FORMAT_BOOLEAN)){
                    sValue = FALSE;
                }
                else if (getFormat(sProperty).equals(FORMAT_NUMBER)){
                    sValue = "0";
                }
                item.setProperty(sProperty,sValue);
            }    
        }
    }   
    
    /**Add new property to all items and all custom properties for the given manager*/
    public void applyNewProperties(){
        Iterator it = properties.keySet().iterator();
        while (it.hasNext()){
        	String sProperty = (String)it.next();
        	applyNewProperty(sProperty);
        }
    }   
    
    

    /**Remove a custom property to all items for the given manager*/
    public void applyRemoveProperty(String sProperty) {
        Collection items = getItems();
        if (items != null){
            Iterator it = items.iterator();
            while (it.hasNext()){
                IPropertyable item = (IPropertyable)it.next();
                item.removeProperty(sProperty);
            }    
        }
    }
     
    /**
     * 
     * @return XML representation of this manager
     */
    public String toXML(){
        StringBuffer sb = new StringBuffer("\t<").append(getIdentifier()); //$NON-NLS-1$
        Iterator it = properties.keySet().iterator();
        while (it.hasNext()) {
            String sProperty = (String) it.next();
            String sFormat = (String)properties.get(sProperty);
            sb.append(" "+sProperty + "='" + sFormat + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        sb.append(">\n"); //$NON-NLS-1$
        return sb.toString();
    }
    
    /**
     * 
     * @return custom properties
     */
    public Collection getCustomProperties(){
        return properties.keySet();
    }
  
    /**
     * 
     * @param index
     * @return
     */
    public String getPropertyAtIndex(int index){
        ArrayList al = new ArrayList(properties.keySet());
    	return (String)al.get(index);
    }
    
    /**
     * Set all personnal properties of an XML file for an item manager
     * 
     * @param attributes :
     *                list of attributes for this XML item
     */
    public void populateProperties(Attributes attributes) {
       for (int i = 0; i < attributes.getLength(); i++) {
             addProperty(attributes.getQName(i), attributes.getValue(i));
        }
    }
    
    /**
     *  Get Item manager with a given attribute name   
     * @param sItem
     * @return
     */
    public static ItemManager getItemManager(String sProperty){
        if (XML_DEVICE.equals(sProperty)){
            return DeviceManager.getInstance();
        }
        else if (XML_TRACK.equals(sProperty)){
            return TrackManager.getInstance();
        }
        else if (XML_ALBUM.equals(sProperty)){
            return AlbumManager.getInstance();
        }
        else if (XML_AUTHOR.equals(sProperty)){
            return AuthorManager.getInstance();
        }
        else if (XML_STYLE.equals(sProperty)){
            return StyleManager.getInstance();
        }
        else if (XML_DIRECTORY.equals(sProperty)){
            return DirectoryManager.getInstance();
        }
        else if (XML_FILE.equals(sProperty)){
            return FileManager.getInstance();
        }
        else if (XML_PLAYLIST_FILE.equals(sProperty)){
            return PlaylistFileManager.getInstance();
        }
        else if (XML_PLAYLIST.equals(sProperty)){
            return PlaylistManager.getInstance();
        }
        else if (XML_TYPE.equals(sProperty)){
            return TypeManager.getInstance();
        }
        else{
            return null;
        }
    }
    
    /**
     *  Get Item manager for given item class  
     * @param class
     * @return associated item manager or null if none was found
     */
    public static ItemManager getItemManager(Class c){
        if (c.equals(Device.class)){
            return DeviceManager.getInstance();
        }
        else if (c.equals(Track.class)){
            return TrackManager.getInstance();
        }
        else if (c.equals(Album.class)){
            return AlbumManager.getInstance();
        }
        else if (c.equals(Author.class)){
            return AuthorManager.getInstance();
        }
        else if (c.equals(Style.class)){
            return StyleManager.getInstance();
        }
        else if (c.equals(Directory.class)){
            return DirectoryManager.getInstance();
        }
        else if (c.equals(File.class)){
            return FileManager.getInstance();
        }
        else if (c.equals(PlaylistFile.class)){
            return PlaylistFileManager.getInstance();
        }
        else if (c.equals(Playlist.class)){
            return PlaylistManager.getInstance();
        }
        else if (c.equals(Type.class)){
            return TypeManager.getInstance();
        }
        else{
            return null;
        }
    }
    
    
    /**
     * Perform an cleanup : delete useless items
     */
    public synchronized void cleanup() {
        Iterator it = hmItems.values().iterator();
        while (it.hasNext()) {
            IPropertyable item = (IPropertyable) it.next();
            if ( TrackManager.getInstance().getAssociatedTracks(item).size() == 0){
                it.remove();
            }
        }
    }
    
    /**
     * Perform a cleanup for a given item
     */
    public synchronized void cleanup(IPropertyable item) {
        if ( TrackManager.getInstance().getAssociatedTracks(item).size() == 0){
            hmItems.remove(((PropertyAdapter)item).getId());
        }
    }
    
     /**Return all registred items*/
    public synchronized Collection getItems() {
        return hmItems.values();
    }
    
      /**Return a given item*/
    public synchronized IPropertyable getItem(String sID) {
        return (IPropertyable)hmItems.get(sID);
    }
    
    /**
     * Delete an item
     * @param sId
     */
    public synchronized void remove(String sId){
        hmItems.remove(sId);
    }
    
    /**
     * Post registering code
     * @param item
     */
    public void postRegistering( IPropertyable item){
         //try to recover some properties previous a refresh
        restorePropertiesAfterRefresh(item,((PropertyAdapter)item).getId());
        //apply default custom properties
        applyNewProperties();
    }
    
    /**
     * Change any item
     * @param itemToChange
     * @param sKey
     * @param sValue
     * @return the new item or null if the item is still the same
     */
    public static IPropertyable changeItem(IPropertyable itemToChange,String sKey,String sValue){
        IPropertyable newItem = null;
        if (itemToChange instanceof File){
            File file = (File)itemToChange;
            if (XML_NAME.equals(sKey)){ //file name
                newItem = FileManager.getInstance().changeFileName((File)itemToChange,sValue);
            }
            else if (XML_TRACK.equals(sKey)){ //track name
                newItem = TrackManager.getInstance().changeTrackName(file.getTrack(),sValue);
            }
            else if (XML_STYLE.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackStyle(file.getTrack(),sValue);
            }
            else if (XML_ALBUM.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackAlbum(file.getTrack(),sValue);
            }
            else if (XML_AUTHOR.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackAuthor(file.getTrack(),sValue);
            }
            else if (XML_COMMENT.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackComment(file.getTrack(),sValue);
            }
            else if (XML_TRACK_ORDER.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackOrder(file.getTrack(),sValue);
            }
            else if (XML_TRACK_YEAR.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackYear(file.getTrack(),sValue);
            }
            else if (XML_TRACK_RATE.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackRate(file.getTrack(),sValue);
            }
            else{ //others properties
                itemToChange. setProperty(sKey,sValue);
            }
        }
        else if (itemToChange instanceof Track){
            if (XML_NAME.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackName((Track)itemToChange,sValue);
            }
            else if (XML_STYLE.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackStyle((Track)itemToChange,sValue);
            }
            else if (XML_ALBUM.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackAlbum((Track)itemToChange,sValue);
            }
            else if (XML_AUTHOR.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackAuthor((Track)itemToChange,sValue);
            }
            else if (XML_COMMENT.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackComment((Track)itemToChange,sValue);
            }
            else if (XML_TRACK_ORDER.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackOrder((Track)itemToChange,sValue);
            }
            else if (XML_TRACK_YEAR.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackYear((Track)itemToChange,sValue);
            }
            else if (XML_TRACK_RATE.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackRate((Track)itemToChange,sValue);
            }
            else{ //others properties
                itemToChange. setProperty(sKey,sValue);
            }
        }
        else if (itemToChange instanceof Album){
            if (XML_NAME.equals(sKey)){
                newItem = AlbumManager.getInstance().changeAlbumName((Album)itemToChange,sValue);
            }
            else{ //others properties
                itemToChange. setProperty(sKey,sValue);
            }
        }
        else if (itemToChange instanceof Author){
            if (XML_NAME.equals(sKey)){
                newItem = AuthorManager.getInstance().changeAuthorName((Author)itemToChange,sValue);
            }
            else{ //others properties
                itemToChange. setProperty(sKey,sValue);
            }
        }
        else if (itemToChange instanceof Style){
            if (XML_NAME.equals(sKey)){
                newItem = StyleManager.getInstance().changeStyleName((Style)itemToChange,sValue);
            }
            else{ //others properties
                itemToChange. setProperty(sKey,sValue);
            }
        }
        return newItem;            
    }
    
}