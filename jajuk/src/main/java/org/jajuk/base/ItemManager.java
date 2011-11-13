/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
 *  http://jajuk.info
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jajuk.services.tags.Tag;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Managers parent class.
 */
public abstract class ItemManager {

  /** Maps item classes -> instance, must be a linked map for ordering (mandatory in commited collection). */
  private static Map<Class<?>, ItemManager> hmItemManagers = new LinkedHashMap<Class<?>, ItemManager>(
      10);

  /** Maps properties meta information name and object. */
  private final Map<String, PropertyMetaInformation> hmPropertiesMetaInformation = new LinkedHashMap<String, PropertyMetaInformation>(
      10);

  /** The Lock. */
  ReadWriteLock lock = new ReentrantReadWriteLock();

  /** Use an array list during startup which is faster during loading the collection. */
  private List<Item> startupItems = new ArrayList<Item>(100);

  /** Stores the items by ID to have quick access if necessary. */
  private final Map<String, Item> internalMap = new HashMap<String, Item>(100);

  /** Collection pointer : at the beginning point to the ArrayList, later this is replaced by a TreeSet to have correct ordering. */
  private Collection<Item> items = startupItems;

  /**
   * Item manager default constructor.
   */
  public ItemManager() {
  }

  /**
   * Switch all item managers to ordered mode See
   * ItemManager.switchToOrderState() for more details
   */
  public static void switchAllManagersToOrderState() {
    Log.debug("Switching to sorted mode");
    for (ItemManager manager : hmItemManagers.values()) {
      manager.switchToOrderState();
    }
  }

  /**
   * Switch this item manager to order mode This feature allows faster
   * collection loading As collection.xml contains ordered elements, we simply a
   * use an ArrayList to store items first, then few seconds after startup and
   * before user could make changes to the collection, we populate a TreeSet
   * from the ArrayList and begin to use it.
   */
  public void switchToOrderState() {
    lock.writeLock().lock();
    try {
      // populate a new TreeSet with the startup-items
      if (startupItems != null) {
        items = new TreeSet<Item>(startupItems);

        // Free startup memory
        startupItems = null;
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Registers a new item manager.
   *
   * @param c Managed item class
   * @param itemManager DOCUMENT_ME
   */
  public static void registerItemManager(Class<?> c, ItemManager itemManager) {
    hmItemManagers.put(c, itemManager);
  }

  /**
   * Gets the XML tag.
   * 
   * @return identifier used for XML generation
   */
  public abstract String getXMLTag();

  /**
   * Gets the meta information.
   *
   * @param sPropertyName DOCUMENT_ME
   *
   * @return meta data for given property
   */
  public PropertyMetaInformation getMetaInformation(String sPropertyName) {
    return hmPropertiesMetaInformation.get(sPropertyName);
  }

  /**
   * Remove a property *.
   *
   * @param sProperty DOCUMENT_ME
   */
  public void removeProperty(String sProperty) {
    PropertyMetaInformation meta = getMetaInformation(sProperty);
    hmPropertiesMetaInformation.remove(sProperty);
    applyRemoveProperty(meta); // remove this property from all items
  }

  /**
   * Remove a custom property from all items for the given manager.
   *
   * @param meta DOCUMENT_ME
   */
  void applyRemoveProperty(PropertyMetaInformation meta) {
    lock.readLock().lock();
    try {
      for (Item item : items) {
        item.removeProperty(meta.getName());
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Generic method to access to a parameterized list of items.
   *
   * @param meta DOCUMENT_ME
   *
   * @return the item-parameterized list
   *
   * protected abstract HashMap<String, Item> getItemsMap();
   */

  /** Add a custom property to all items for the given manager */
  public void applyNewProperty(PropertyMetaInformation meta) {
    lock.readLock().lock();
    try {
      for (Item item : items) {
        item.setProperty(meta.getName(), meta.getDefaultValue());
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Attention, this method does not return a full XML, but rather an excerpt
   * that is then completed in Collection.commit()!
   *
   * @return (partial) XML representation of this manager
   */
  String toXML() {
    StringBuilder sb = new StringBuilder("<").append(getXMLTag() + ">");
    Iterator<String> it = hmPropertiesMetaInformation.keySet().iterator();
    while (it.hasNext()) {
      String sProperty = it.next();
      PropertyMetaInformation meta = hmPropertiesMetaInformation.get(sProperty);
      sb.append('\n' + meta.toXML());
    }
    return sb.append('\n').toString();
  }

  /**
   * Return the associated read write lock.
   *
   * @return the associated read write lock
   */
  public ReadWriteLock getLock() {
    return lock;
  }

  /**
   * Format the item name to be normalized :
   * <p>
   * -no underscores or other non-ascii characters
   * <p>
   * -no spaces at the begin and the end
   * <p>
   * -All in lower case expect first letter of first word
   * <p>
   * example: "My artist".
   *
   * @param sName The name to format.
   *
   * @return the string
   *
   * TODO: the "all lowercase" part is not done currently, should this be changed??
   */
  public static String format(String sName) {
    String sOut;
    sOut = sName.trim(); // suppress spaces at the begin and the end
    sOut = sOut.replace('-', ' '); // move - to space
    sOut = sOut.replace('_', ' '); // move _ to space
    char c = sOut.charAt(0);
    StringBuilder sb = new StringBuilder(sOut);
    sb.setCharAt(0, Character.toUpperCase(c));
    return sb.toString();
  }

  /**
   * Gets the properties.
   *
   * @return properties Meta informations
   */
  public Collection<PropertyMetaInformation> getProperties() {
    return hmPropertiesMetaInformation.values();
  }

  /**
   * Gets the custom properties including activated extra tags.
   *
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
   * Gets the custom properties without the activated extra tags.
   *
   * @return custom properties Meta informations
   */
  public Collection<PropertyMetaInformation> getUserCustomProperties() {
    List<PropertyMetaInformation> col = new ArrayList<PropertyMetaInformation>();
    Iterator<PropertyMetaInformation> it = hmPropertiesMetaInformation.values().iterator();
    while (it.hasNext()) {
      PropertyMetaInformation meta = it.next();
      if (meta.isCustom() && !Tag.getActivatedExtraTags().contains(meta.getName())) {
        col.add(meta);
      }
    }
    return col;
  }

  /**
   * Gets the visible properties.
   *
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
   * Get the manager from a given attribute name.
   *
   * @param sProperty The property to compare.
   *
   * @return an ItemManager if one is found for the property or null if none
   * found.
   */
  public static ItemManager getItemManager(String sProperty) {
    if (Const.XML_DEVICE.equals(sProperty)) {
      return DeviceManager.getInstance();
    } else if (Const.XML_TRACK.equals(sProperty)) {
      return TrackManager.getInstance();
    } else if (Const.XML_ALBUM.equals(sProperty)) {
      return AlbumManager.getInstance();
    } else if (Const.XML_ARTIST.equals(sProperty)) {
      return ArtistManager.getInstance();
    } else if (Const.XML_ALBUM_ARTIST.equals(sProperty)) {
      return AlbumArtistManager.getInstance();
    } else if (Const.XML_GENRE.equals(sProperty)) {
      return GenreManager.getInstance();
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
   * Get ItemManager manager for given item class.
   *
   * @param c DOCUMENT_ME
   *
   * @return associated item manager or null if none was found
   */
  public static ItemManager getItemManager(Class<?> c) {
    return hmItemManagers.get(c);
  }

  /**
   * Perform cleanup : delete useless items.
   */
  @SuppressWarnings("unchecked")
  public void cleanup() {
    lock.writeLock().lock();
    try {

      // Prefetch item manager type for performances
      short managerType = 0; // Album
      if (this instanceof ArtistManager) {
        managerType = 1;
      } else if (this instanceof GenreManager) {
        Log.debug("Genre cleanup not allowed");
        return;
      } else if (this instanceof YearManager) {
        managerType = 2;
      } else if (this instanceof AlbumArtistManager) {
        managerType = 3;
      }
      // build used items set
      List<Item> lItems = new ArrayList<Item>(100);
      List<Track> tracks = TrackManager.getInstance().getTracks();
      for (Track track : tracks) {
        switch (managerType) {
        case 0:
          lItems.add(track.getAlbum());
          break;
        case 1:
          lItems.add(track.getArtist());
          break;
        case 2:
          lItems.add(track.getYear());
          break;
        case 3:
          lItems.add(track.getAlbumArtist());
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
          internalMap.remove(item.getID());
        }
      }
    } finally {
      lock.writeLock().unlock();
    }

  }

  /**
   * Perform a cleanup of all orphan tracks associated with given item.
   *
   * @param item item whose associated tracks should be checked for cleanup
   */
  protected void cleanOrphanTracks(Item item) {
    if (TrackManager.getInstance().getAssociatedTracks(item, false).isEmpty()) {
      removeItem(item);
    }
  }

  /**
   * Remove a given item.
   *
   * @param item DOCUMENT_ME
   */
  public void removeItem(Item item) {
    lock.writeLock().lock();
    try {
      if (item != null) {
        items.remove(item);
        internalMap.remove(item.getID());
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Register a given item.
   *
   * @param item : the item to add
   */
  protected void registerItem(Item item) {
    lock.writeLock().lock();
    try {
      items.add(item);
      internalMap.put(item.getID(), item);
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Register a new property.
   *
   * @param meta DOCUMENT_ME
   */
  public void registerProperty(PropertyMetaInformation meta) {
    hmPropertiesMetaInformation.put(meta.getName(), meta);
  }

  /**
   * Change any item.
   *
   * @param itemToChange DOCUMENT_ME
   * @param sKey DOCUMENT_ME
   * @param oValue DOCUMENT_ME
   * @param filter : files we want to deal with
   *
   * @return the changed item
   *
   * @throws JajukException the jajuk exception
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
      } else if (Const.XML_GENRE.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackGenre(file.getTrack(), (String) oValue,
            filter);
      } else if (Const.XML_ALBUM.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackAlbum(file.getTrack(), (String) oValue,
            filter);
      } else if (Const.XML_ARTIST.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackArtist(file.getTrack(), (String) oValue,
            filter);
      } else if (Const.XML_TRACK_COMMENT.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackComment(file.getTrack(), (String) oValue,
            filter);
      } else if (Const.XML_TRACK_ORDER.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackOrder(file.getTrack(), (Long) oValue,
            filter);
      } else if (Const.XML_ALBUM_ARTIST.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackAlbumArtist(file.getTrack(),
            (String) oValue, filter);
      } else if (Const.XML_YEAR.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackYear(file.getTrack(),
            String.valueOf(oValue), filter);
      } else if (Const.XML_TRACK_RATE.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackRate(file.getTrack(), (Long) oValue);
      } else { // others properties
        // check if this key is known for files
        if (file.getMeta(sKey) != null) {
          itemToChange.setProperty(sKey, oValue);
        }
        // Unknown ? check if it is a track custom property
        else if (file.getTrack().getMeta(sKey) != null) {
          file.getTrack().setProperty(sKey, oValue);
        }
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
      } else if (Const.XML_GENRE.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackGenre((Track) itemToChange,
            (String) oValue, filter);
      } else if (Const.XML_ALBUM.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackAlbum((Track) itemToChange,
            (String) oValue, filter);
      } else if (Const.XML_ARTIST.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackArtist((Track) itemToChange,
            (String) oValue, filter);
      } else if (Const.XML_ALBUM_ARTIST.equals(sKey)) {
        newItem = TrackManager.getInstance().changeTrackAlbumArtist((Track) itemToChange,
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
    } else if (itemToChange instanceof Artist) {
      if (Const.XML_NAME.equals(sKey)) {
        newItem = ArtistManager.getInstance().changeArtistName((Artist) itemToChange,
            (String) oValue);
      } else { // others properties
        itemToChange.setProperty(sKey, oValue);
      }
    } else if (itemToChange instanceof Genre) {
      if (Const.XML_NAME.equals(sKey)) {
        newItem = GenreManager.getInstance().changeGenreName((Genre) itemToChange, (String) oValue);
      } else { // others properties
        itemToChange.setProperty(sKey, oValue);
      }
    } else if (itemToChange instanceof Year) {
      itemToChange.setProperty(sKey, oValue);
    }
    return newItem;
  }

  /**
   * Gets the element count.
   *
   * @return number of item
   */
  public int getElementCount() {
    return items.size();
  }

  /**
   * Gets the item by id.
   *
   * @param sID Item ID
   *
   * @return Item
   */
  public Item getItemByID(String sID) {
    return internalMap.get(sID);
  }

  /**
   * Return a copy of all registered items. The resulting list can be used without
   * need of locking.
   *
   * @return a copy of all registered items
   */
  public List<? extends Item> getItems() {
    // getItems() creates a copy of the list of items and thus iterates over the current list of items
    // therefore a ConcurrentModifcationException could be triggered if we do not lock while actually
    // doing the copying. Usage of the list afterwards is save without locking
    lock.readLock().lock();
    try {
      return new ArrayList<Item>(items);
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
  * ***************************************************************************
  * Return all registered enumeration CAUTION : do not call remove() on this
  * iterator, you would effectively remove items instead of using regular
  * removeItem() primitive
  * **************************************************************************.
  *
  * @return the items iterator
  */
  protected Iterator<? extends Item> getItemsIterator() {
    return items.iterator();
  }

  /**
   * Clear any entries from this manager.
   */
  public void clear() {
    lock.writeLock().lock();
    try {
      items.clear();
      internalMap.clear();
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Force files sorting after an order change, i.e. Called to ensure Set
   * sorting contract <br>
   * We remove all items and add them all again to force sorting
   */
  public void forceSorting() {
    lock.writeLock().lock();
    try {
      // first create a copy
      ArrayList<Item> itemsCopy = new ArrayList<Item>(items);

      // then remove all elements
      clear();

      // and then re-add all items again to make them correctly sorted again
      for (Item item : itemsCopy) {
        registerItem(item);
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Basic implementation for item hashcode computation.
   *
   * @param sName item name
   *
   * @return ItemManager ID
   */
  protected static String createID(String sName) {
    return MD5Processor.hash(sName);
  }
}
