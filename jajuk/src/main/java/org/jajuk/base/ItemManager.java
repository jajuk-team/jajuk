/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Managers parent class
 */
public abstract class ItemManager {

   /**
   * Maps item classes -> instance, must be a linked map for ordering (mandatory
   * in commited collection)
   */
  private static Map<Class<?>, ItemManager> hmItemManagers = new LinkedHashMap<Class<?>, ItemManager>(
      10);

  /** Maps properties meta information name and object */
  private final Map<String, PropertyMetaInformation> hmPropertiesMetaInformation = new LinkedHashMap<String, PropertyMetaInformation>(
      10);

  /*****************************************************************************
   * Items collection
   * <p>
   * We use a concrete type here and not an upper bounded wildcard type (?
   * extends Item) because this prevent from calling methods in it like
   * put(String,Album)
   * </p>
   ****************************************************************************/

  private final Set<Item> finalItems = new TreeSet<Item>();

  private List<Item> startupItems = new ArrayList<Item>(100);

  private final Map<String, Item> internalMap = new HashMap<String, Item>(100);

  private Collection<Item> items = startupItems;

  /**
   * Item manager default constructor
   */
  ItemManager() {
  }

  /**
   * Switch all item managers to ordered mode See
   * ItemManager.switchToOrderState() for more details
   */
  public static synchronized void switchAllManagersToOrderState() {
    Log.debug("Switching to sorted mode");
    for (ItemManager manager : hmItemManagers.values()) {
      manager.switchToOrderState();
    }
  }

  /**
   * Switch this item manager to order mode This feature allows faster
   * collection loading As collection.xml contains ordered elements, we simply a
   * use an arraylist to store items first, then few seconds after startup and
   * before user could make changes to the collection, we populate a treeset
   * from the arraylist and begin ti use it.
   * 
   */
  public synchronized void switchToOrderState() {
    for (Item item : startupItems) {
      finalItems.add(item);
    }
    items = finalItems;
    // Free startup memory
    startupItems = null;
  }

  /**
   * Registrates a new item manager
   * 
   * @param c
   *          Managed item class
   * @param itemManager
   */
  public static void registerItemManager(Class<?> c, ItemManager itemManager) {
    hmItemManagers.put(c, itemManager);
  }

  /**
   * @return identifier used for XML generation
   */
  public abstract String getLabel();

  /**
   * @param sPropertyName
   * @return meta data for given property
   */
  public PropertyMetaInformation getMetaInformation(String sPropertyName) {
    return hmPropertiesMetaInformation.get(sPropertyName);
  }

  /**
   * Return a human representation for a given property name when we don't now
   * item type we work on. Otherwise, use PropertyMetaInformation.getHumanType
   * 
   * @param s
   * @return
   */
  public static String getHumanType(String sKey) {
    String sOut = sKey;
    if (Messages.contains(Const.PROPERTY_SEPARATOR + sKey)) {
      return Messages.getString(Const.PROPERTY_SEPARATOR + sKey);
    }
    return sOut;
  }

  /** Remove a property * */
  public void removeProperty(String sProperty) {
    PropertyMetaInformation meta = getMetaInformation(sProperty);
    hmPropertiesMetaInformation.remove(sProperty);
    applyRemoveProperty(meta); // remove this property to all items
  }

  /** Remove a custom property to all items for the given manager */
  public synchronized void applyRemoveProperty(PropertyMetaInformation meta) {
    for (Item item : items) {
      item.removeProperty(meta.getName());
    }
  }

  /**
   * Generic method to access to a parametrized list of items
   * 
   * @return the item-parametrized list
   * 
   * protected abstract HashMap<String, Item> getItemsMap();
   */

  /** Add a custom property to all items for the given manager */
  public synchronized void applyNewProperty(PropertyMetaInformation meta) {
    for (Item item : items) {
      item.setProperty(meta.getName(), meta.getDefaultValue());
    }
  }

  /**
   * 
   * @return XML representation of this manager
   */
  public String toXML() {
    StringBuilder sb = new StringBuilder("<").append(getLabel() + ">");
    Iterator<String> it = hmPropertiesMetaInformation.keySet().iterator();
    while (it.hasNext()) {
      String sProperty = it.next();
      PropertyMetaInformation meta = hmPropertiesMetaInformation.get(sProperty);
      sb.append('\n' + meta.toXML());
    }
    return sb.append('\n').toString();
  }

  /**
   * @return properties Meta informations
   */
  public Collection<PropertyMetaInformation> getProperties() {
    return hmPropertiesMetaInformation.values();
  }

  /**
   * @return custom properties Meta informations
   */
  public Collection<PropertyMetaInformation> getCustomProperties() {
    List<PropertyMetaInformation> col = new ArrayList<PropertyMetaInformation>();
    Iterator<PropertyMetaInformation> it = hmPropertiesMetaInformation.values().iterator();
    while (it.hasNext()) {
      PropertyMetaInformation meta = it.next();
      if (meta.isCustom()) {
        col.add(meta);
      }
    }
    return col;
  }

  /**
   * @return visible properties Meta informations
   */
  public Collection<PropertyMetaInformation> getVisibleProperties() {
    List<PropertyMetaInformation> col = new ArrayList<PropertyMetaInformation>();
    Iterator<PropertyMetaInformation> it = hmPropertiesMetaInformation.values().iterator();
    while (it.hasNext()) {
      PropertyMetaInformation meta = it.next();
      if (meta.isVisible()) {
        col.add(meta);
      }
    }
    return col;
  }

  /**
   * Get ItemManager manager with a given attribute name
   * 
   * @param sItem
   * @return
   */
  public static ItemManager getItemManager(String sProperty) {
    if (Const.XML_DEVICE.equals(sProperty)) {
      return DeviceManager.getInstance();
    } else if (Const.XML_TRACK.equals(sProperty)) {
      return TrackManager.getInstance();
    } else if (Const.XML_ALBUM.equals(sProperty)) {
      return AlbumManager.getInstance();
    } else if (Const.XML_AUTHOR.equals(sProperty)) {
      return AuthorManager.getInstance();
    } else if (Const.XML_STYLE.equals(sProperty)) {
      return StyleManager.getInstance();
    } else if (Const.XML_DIRECTORY.equals(sProperty)) {
      return DirectoryManager.getInstance();
    } else if (Const.XML_FILE.equals(sProperty)) {
      return FileManager.getInstance();
    } else if (Const.XML_PLAYLIST_FILE.equals(sProperty)) {
      return PlaylistManager.getInstance();
    } else if (Const.XML_TYPE.equals(sProperty)) {
      return TypeManager.getInstance();
    } else {
      return null;
    }
  }

  /**
   * Get ItemManager manager for given item class
   * 
   * @param class
   * @return associated item manager or null if none was found
   */
  public static ItemManager getItemManager(Class<?> c) {
    return hmItemManagers.get(c);
  }

  /**
   * Return an iteration over item managers
   */
  public static Iterator<ItemManager> getItemManagers() {
    return hmItemManagers.values().iterator();
  }

  /**
   * Perform an cleanup : delete useless items
   */
  @SuppressWarnings("unchecked")
  public synchronized void cleanup() {
    // Prefetch item manager type for performances
    short managerType = 0; // Album
    if (this instanceof AuthorManager) {
      managerType = 1;
    } else if (this instanceof StyleManager) {
      Log.debug("Style cleanup not allowed");
      return;
    } else if (this instanceof YearManager) {
      managerType = 2;
    }
    // build used items set
    List<Item> lItems = new ArrayList<Item>(100);
    ReadOnlyIterator<Track> tracks = TrackManager.getInstance().getTracksIterator();
    while (tracks.hasNext()) {
      Track track = tracks.next();
      switch (managerType) {
      case 0:
        lItems.add(track.getAlbum());
        break;
      case 1:
        lItems.add(track.getAuthor());
        break;
      case 2:
        lItems.add(track.getYear());
        break;
      }
    }
    // Now iterate over this manager items to check if it is present in the
    // items list
    Iterator<Item> it = (Iterator<Item>) getItemsIterator();
    while (it.hasNext()) {
      Item item = it.next();
      // check if this item still maps some tracks
      if (!lItems.contains(item)) {
        it.remove();
      }
    }
  }

  /**
   * Perform a cleanup of all orphan tracks associated with given item
   * 
   * @param item
   *          item whose associated tracks should be checked for cleanup
   * 
   */
  protected synchronized void cleanOrphanTracks(Item item) {
    if (TrackManager.getInstance().getAssociatedTracks(item, false).size() == 0) {
      removeItem(item);
    }
  }

  /** Remove a given item */
  protected synchronized void removeItem(Item item) {
    if (item != null) {
      items.remove(item);
      internalMap.remove(item.getID());
    }
  }

  /**
   * Register a given item
   * 
   * @param item :
   *          the item to add
   */
  protected synchronized void registerItem(Item item) {
    items.add(item);
    internalMap.put(item.getID(), item);
  }

  /**
   * Register a new property
   * 
   * @param meta
   */
  public void registerProperty(PropertyMetaInformation meta) {
    hmPropertiesMetaInformation.put(meta.getName(), meta);
  }

  /**
   * Change any item
   * 
   * @param itemToChange
   * @param sKey
   * @param oValue
   * @param filter:
   *          files we want to deal with
   * @return the changed item
   */
  public static Item changeItem(Item itemToChange, String sKey, Object oValue, Set<File> filter)
      throws JajukException {
    if (Log.isDebugEnabled()) {
      Log.debug("Set " + sKey + "=" + oValue.toString() + " to " + itemToChange);
    }
    Item newItem = itemToChange;
    if (itemToChange instanceof File) {
      File file = (File) itemToChange;
      if (Const.XML_NAME.equals(sKey)) { // file name
        newItem = FileManager.getInstance().changeFileName((File) itemToChange, (String) oValue);
      } else if (Const.XML_TRACK.equals(sKey)) { // track name
        newItem = TrackManager.getInstance().changeTrackName(file.getTrack(), (String) oValue,
            filter);
      } else if (Const.XML_STYLE.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackStyle(file.getTrack(), (String) oValue,
            filter);
      } else if (Const.XML_ALBUM.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackAlbum(file.getTrack(), (String) oValue,
            filter);
      } else if (Const.XML_AUTHOR.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackAuthor(file.getTrack(), (String) oValue,
            filter);
      } else if (Const.XML_TRACK_COMMENT.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackComment(file.getTrack(), (String) oValue,
            filter);
      } else if (Const.XML_TRACK_ORDER.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackOrder(file.getTrack(), (Long) oValue,
            filter);
      } else if (Const.XML_YEAR.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackYear(file.getTrack(),
            String.valueOf(oValue), filter);
      } else if (Const.XML_TRACK_RATE.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackRate(file.getTrack(), (Long) oValue);
      } else { // others properties
        itemToChange.setProperty(sKey, oValue);
      }
      // Get associated track file
      if (newItem instanceof Track) {
        file.setTrack((Track) newItem);
        newItem = file;
      }
    } else if (itemToChange instanceof Playlist) {
      if (Const.XML_NAME.equals(sKey)) { // playlistfile name
        newItem = PlaylistManager.getInstance().changePlaylistFileName((Playlist) itemToChange,
            (String) oValue);
      }
    } else if (itemToChange instanceof Directory) {
      if (!Const.XML_NAME.equals(sKey)) { // file name
        // TBI newItem =
        // DirectoryManager.getInstance().changeDirectoryName((Directory)itemToChange,(String)oValue);
        // } else { // others properties
        itemToChange.setProperty(sKey, oValue);
      }
    } else if (itemToChange instanceof Device) {
      itemToChange.setProperty(sKey, oValue);
    } else if (itemToChange instanceof Track) {
      if (Const.XML_NAME.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackName((Track) itemToChange, (String) oValue,
            filter);
      } else if (Const.XML_STYLE.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackStyle((Track) itemToChange,
            (String) oValue, filter);
      } else if (Const.XML_ALBUM.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackAlbum((Track) itemToChange,
            (String) oValue, filter);
      } else if (Const.XML_AUTHOR.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackAuthor((Track) itemToChange,
            (String) oValue, filter);
      } else if (Const.XML_TRACK_COMMENT.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackComment((Track) itemToChange,
            (String) oValue, filter);
      } else if (Const.XML_TRACK_ORDER.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackOrder((Track) itemToChange, (Long) oValue,
            filter);
      } else if (Const.XML_YEAR.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackYear((Track) itemToChange,
            String.valueOf(oValue), filter);
      } else if (Const.XML_TRACK_RATE.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackRate((Track) itemToChange, (Long) oValue);
      } else { // others properties
        itemToChange.setProperty(sKey, oValue);
      }
    } else if (itemToChange instanceof Album) {
      if (Const.XML_NAME.equals(sKey)) {
        newItem = AlbumManager.getInstance().changeAlbumName((Album) itemToChange, (String) oValue);
      } else { // others properties
        itemToChange.setProperty(sKey, oValue);
      }
    } else if (itemToChange instanceof Author) {
      if (Const.XML_NAME.equals(sKey)) {
        newItem = AuthorManager.getInstance().changeAuthorName((Author) itemToChange,
            (String) oValue);
      } else { // others properties
        itemToChange.setProperty(sKey, oValue);
      }
    } else if (itemToChange instanceof Style) {
      if (Const.XML_NAME.equals(sKey)) {
        newItem = StyleManager.getInstance().changeStyleName((Style) itemToChange, (String) oValue);
      } else { // others properties
        itemToChange.setProperty(sKey, oValue);
      }
    } else if (itemToChange instanceof Year) {
      itemToChange.setProperty(sKey, oValue);
    }
    return newItem;
  }

  /**
   * 
   * @return number of item
   */
  public int getElementCount() {
    return items.size();
  }

  /**
   * @param sID
   *          Item ID
   * @return Item
   */
  public Item getItemByID(String sID) {
    return internalMap.get(sID);
  }

  /**
   * Return a shallow copy of all registrated items
   * 
   * @return a shallow copy of all registrated items
   */
  public synchronized List<? extends Item> getItems() {
    return new ArrayList<Item>(items);
  }

  /**
   * Return a shallow copy of all registrated items filtered using the provided
   * predicate*
   * 
   * @arg predicate : the predicate
   * @return a shallow copy of all registrated items filtered using the provided
   */
  public synchronized List<? extends Item> getFilteredItems(Predicate predicate) {
    ArrayList<Item> itemsCopy = new ArrayList<Item>(items);
    CollectionUtils.filter(itemsCopy, predicate);
    return itemsCopy;
  }

  /*****************************************************************************
   * Return all registrated enumeration CAUTION : do not call remove() on this
   * iterator, you should remove effective items
   ****************************************************************************/
  protected synchronized Iterator<? extends Item> getItemsIterator() {
    return items.iterator();
  }

  /**
   * Clear any entries from this manager
   */
  public synchronized void clear() {
    items.clear();
    internalMap.clear();
  }

  /**
   * Force files sorting after an order change for ie Called to ensure Set
   * sorting contract <br>
   * We remove all items and add them all again to force sorting
   */
  protected synchronized void forceSorting() {
    ArrayList<Item> itemsCopy = new ArrayList<Item>(items);
    for (Item item : itemsCopy) {
      removeItem(item);
    }
    for (Item item : itemsCopy) {
      registerItem(item);
    }
  }

}
