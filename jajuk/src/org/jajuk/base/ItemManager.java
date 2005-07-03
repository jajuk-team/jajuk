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
import java.util.Iterator;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.SequentialMap;
import org.jajuk.util.Util;
import org.xml.sax.Attributes;

/**
 *  Managers parent class
 *
 * @author     Bertrand Florat
 * @created    20 juin 2005
 */
public abstract class ItemManager implements ITechnicalStrings{

    /**Property to format*/
    SequentialMap smProperties;
       
    /**
     * Constructor
     *
     */
    ItemManager(){
        smProperties = new SequentialMap();
    }
    
    /**
     * 
     * @return identifier used for XML generation
     */
    abstract public String getIdentifier();
    
    /**
     * 
     * @param sProperty
     * @return format for given property
     */
    public String getFormat(String sProperty){
        return (String)smProperties.get(sProperty);
    }
    
    /**
     * Add a property 
     * @param sProperty
     * @param sFormat
     */
    public void addProperty(String sProperty,String sFormat){
        smProperties.put(Util.formatXML(sProperty),Util.formatXML(sFormat)); //make sure to clean strings for XML compliance
    }
    
    /**Remove a property **/
    public void removeProperty(String sProperty){
        smProperties.remove(sProperty);
        applyRemoveProperty(sProperty); //remove ths property to all items
    }
    
    /**Add new property to all items for the given manager*/
    public void applyNewProperty(String sProperty){
        ArrayList alItems = getItems();
        if (alItems != null){
            Iterator it = alItems.iterator();
            while (it.hasNext()){
                IPropertyable item = (IPropertyable)it.next();
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

    /**Remove a custom property to all items for the given manager*/
    public void applyRemoveProperty(String sProperty) {
        ArrayList alItems = getItems();
        if (alItems != null){
            Iterator it = alItems.iterator();
            while (it.hasNext()){
                IPropertyable item = (IPropertyable)it.next();
                item.removeProperty(sProperty);
            }    
        }
    }
    
    /**
     * 
     * @return items for given item manager
     */
    public ArrayList getItems(){
        ArrayList alItems = null;
        if (this instanceof AlbumManager){
            alItems = AlbumManager.getAlbums();
        }
        else if (this instanceof AuthorManager){
            alItems = AuthorManager.getAuthors();
        }
        else if (this instanceof DeviceManager){
            alItems = DeviceManager.getDevicesList();
        }
        else if (this instanceof DirectoryManager){
            alItems = DirectoryManager.getDirectories();
        }
        else if (this instanceof FileManager){
            alItems = FileManager.getFiles();
        }
        else if (this instanceof PlaylistFileManager){
            alItems = PlaylistFileManager.getPlaylistFiles();
        }
        else if (this instanceof PlaylistManager){
            alItems = PlaylistManager.getPlaylists();
        }
        else if (this instanceof StyleManager){
            alItems = StyleManager.getStyles();
        }
        else if (this instanceof TrackManager){
            alItems = TrackManager.getTracks();
        }
        else if (this instanceof TypeManager){
            alItems = TypeManager.getTypes();
        }
        return alItems;
    }
    
    /**
     * 
     * @return XML representation of this manager
     */
    public String toXML(){
        StringBuffer sb = new StringBuffer("\t<").append(getIdentifier()); //$NON-NLS-1$
        Iterator it = smProperties.keys().iterator();
        while (it.hasNext()) {
            String sProperty = (String) it.next();
            String sFormat = (String)smProperties.get(sProperty);
            sb.append(" "+sProperty + "='" + sFormat + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        sb.append(">\n"); //$NON-NLS-1$
        return sb.toString();
    }
    
    public ArrayList getCustomProperties(){
        return (ArrayList)smProperties.keys();
    }
  
    public String getPropertyAtIndex(int index){
        return smProperties.getPropertyAt(index);
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
     *  Get Item with a given attribute name and ID   
     * @param sItem
     * @param sID
     * @return
     */
    public static IPropertyable getItemByID(String sItem,String sID){
        if (XML_DEVICE.equals(sItem)){
            return DeviceManager.getDevice(sID);
        }
        else if (XML_TRACK.equals(sItem)){
            return TrackManager.getTrack(sID);
        }
        else if (XML_ALBUM.equals(sItem)){
            return AlbumManager.getAlbum(sID);
        }
        else if (XML_AUTHOR.equals(sItem)){
            return AuthorManager.getAuthor(sID);
        }
        else if (XML_STYLE.equals(sItem)){
            return StyleManager.getStyle(sID);
        }
        else if (XML_DIRECTORY.equals(sItem)){
            return DirectoryManager.getDirectory(sID);
        }
        else if (XML_FILE.equals(sItem)){
            return FileManager.getFileById(sID);
        }
        else if (XML_PLAYLIST_FILE.equals(sItem)){
            return PlaylistFileManager.getPlaylistFile(sID);
        }
        else if (XML_PLAYLIST.equals(sItem)){
            return PlaylistManager.getPlaylist(sID);
        }
        else if (XML_TYPE.equals(sItem)){
            return TypeManager.getType(sID);
        }
        else{
            return null;
        }
    }
    
}
