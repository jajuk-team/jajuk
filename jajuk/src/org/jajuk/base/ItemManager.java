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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.jajuk.util.ITechnicalStrings;

/**
 *  Managers parent class
 *
 * @author     Bertrand Florat
 * @created    20 juin 2005
 */
public abstract class ItemManager implements ITechnicalStrings{

    /** Map ids and properties, survives to a refresh, is used to recover old properties after refresh */
	protected LinkedHashMap hmIdProperties = new LinkedHashMap(1000);
    /**Items collection**/
    protected LinkedHashMap hmItems = new LinkedHashMap(100);
    /**Maps item classes -> instance*/
    static private HashMap hmItemManagers  = new LinkedHashMap(10);
    /**Maps properties meta information name and object*/
    private LinkedHashMap hmPropertiesMetaInformation = new LinkedHashMap(10);
    
    /**
     * Constructor
     */
    ItemManager(){
        super();
    }
    
    /**
     * Registrates a new item manager
     * @param c Managed item class
     * @param itemManager
     */
    public static void registerItemManager(Class c,ItemManager itemManager){
        hmItemManagers.put(c,itemManager);
    }
    
    /**
     * @return identifier used for XML generation
     */
    abstract public String getIdentifier();
    
    /**
     * @param sPropertyName
     * @return meta data for given property
     */
    public PropertyMetaInformation getMetaInformation(String sPropertyName){
        return (PropertyMetaInformation)hmPropertiesMetaInformation.get(sPropertyName);
    }
    
    /**
     * Restore properties after a refresh if possible
     * @param item
     * @param sId
     */
    public void restorePropertiesAfterRefresh(IPropertyable item){
	    String sId = ((PropertyAdapter)item).getId();
        LinkedHashMap properties = (LinkedHashMap)hmIdProperties.get(sId); 
		if ( properties == null){  //new file
			hmIdProperties.put(sId,item.getProperties());
		}
		else{  //reset properties before refresh
			item.setProperties(properties);
		}
    }
    
    /**Remove a property **/
    public void removeProperty(String sProperty){
        PropertyMetaInformation meta = getMetaInformation(sProperty);
        hmPropertiesMetaInformation.remove(sProperty);
        applyRemoveProperty(meta); //remove this property to all items
    }
    
    /*Add new property to all items for the given manager
    public void applyNewProperty(PropertyMetaInformation meta){
        Collection items = getItems();
        if (items != null){
            Iterator it = items.iterator();
            while (it.hasNext()){
                IPropertyable item = (IPropertyable)it.next();
                //just initialize void fields
                item.populateDefaultProperty(meta);
            }    
        }
    }   */
          
    /**Remove a custom property to all items for the given manager*/
    public void applyRemoveProperty(PropertyMetaInformation meta) {
        Collection items = getItems();
        if (items != null){
            Iterator it = items.iterator();
            while (it.hasNext()){
                IPropertyable item = (IPropertyable)it.next();
                item.removeProperty(meta.getName());
            }    
        }
    }
     
    /**
     * 
     * @return XML representation of this manager
     */
    public String toXML(){
        StringBuffer sb = new StringBuffer("\t<").append(getIdentifier()+">"); //$NON-NLS-1$
        Iterator it = hmPropertiesMetaInformation.keySet().iterator();
        while (it.hasNext()) {
            String sProperty = (String) it.next();
            PropertyMetaInformation meta = (PropertyMetaInformation)hmPropertiesMetaInformation.get(sProperty);
            sb.append('\n'+meta.toXML());
        }
        return sb.append('\n').toString();
    }
    
     /**
     * @return properties Meta informations
     */
    public Collection getProperties(){
        return hmPropertiesMetaInformation.values();
    }
    
    /**
     * @return custom properties Meta informations
     */
    public Collection getCustomProperties(){
        ArrayList col = new ArrayList();
        Iterator it = hmPropertiesMetaInformation.values().iterator();
        while (it.hasNext()){
            PropertyMetaInformation meta = (PropertyMetaInformation)it.next();
            if (meta.isCustom()){
                col.add(meta);
            }
        }
        return col;
    }
    
     /**
     * @return visible properties Meta informations
     */
    public Collection getVisibleProperties(){
        ArrayList col = new ArrayList();
        Iterator it = hmPropertiesMetaInformation.values().iterator();
        while (it.hasNext()){
            PropertyMetaInformation meta = (PropertyMetaInformation)it.next();
            if (meta.isVisible()){
                col.add(meta);
            }
        }
        return col;
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
        return (ItemManager)hmItemManagers.get(c);
      }
    
    /**
     * Return an iteration over item managers
     */
    public static Iterator getItemManagers(){
        return hmItemManagers.values().iterator();
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
    public synchronized Collection<IPropertyable> getItems() {
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
     * Register a new property
     * @param meta
     */
    public void registerProperty(PropertyMetaInformation meta){
        hmPropertiesMetaInformation.put(meta.getName(),meta);
    }
     
    /**
     * Change any item
     * @param itemToChange
     * @param sKey
     * @param sValue
     * @return the changed item
     */
    public static IPropertyable changeItem(IPropertyable itemToChange,String sKey,String sValue){
        IPropertyable newItem = itemToChange;;
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
            else if (XML_TRACK_COMMENT.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackComment(file.getTrack(),sValue);
            }
            else if (XML_TRACK_ORDER.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackOrder(file.getTrack(),sValue);
            }
            else if (XML_TRACK_YEAR.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackYear(file.getTrack(),Integer.parseInt(sValue));
            }
            else if (XML_TRACK_RATE.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackRate(file.getTrack(),sValue);
            }
            else{ //others properties
                itemToChange.setProperty(sKey,sValue);
            }
        }
        else if (itemToChange instanceof Directory){
            Directory dir = (Directory)itemToChange;
            if (XML_NAME.equals(sKey)){ //file name
                newItem = DirectoryManager.getInstance().changeDirectoryName((Directory)itemToChange,sValue);
            }
            else{ //others properties
                itemToChange.setProperty(sKey,sValue);
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
            else if (XML_TRACK_COMMENT.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackComment((Track)itemToChange,sValue);
            }
            else if (XML_TRACK_ORDER.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackOrder((Track)itemToChange,sValue);
            }
            else if (XML_TRACK_YEAR.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackYear((Track)itemToChange,Integer.parseInt(sValue));
            }
            else if (XML_TRACK_RATE.equals(sKey)){
                newItem = TrackManager.getInstance().changeTrackRate((Track)itemToChange,sValue);
            }
            else{ //others properties
                itemToChange.setProperty(sKey,sValue);
            }
        }
        else if (itemToChange instanceof Album){
            if (XML_NAME.equals(sKey)){
                newItem = AlbumManager.getInstance().changeAlbumName((Album)itemToChange,sValue);
            }
            else{ //others properties
                itemToChange.setProperty(sKey,sValue);
            }
        }
        else if (itemToChange instanceof Author){
            if (XML_NAME.equals(sKey)){
                newItem = AuthorManager.getInstance().changeAuthorName((Author)itemToChange,sValue);
            }
            else{ //others properties
                itemToChange.setProperty(sKey,sValue);
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