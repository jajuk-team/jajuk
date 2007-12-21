/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
package org.jajuk.base.managers;

import static org.jajuk.util.Resources.PROPERTY_SEPARATOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;

import org.jajuk.base.DataSet;
import org.jajuk.base.IItem;
import org.jajuk.base.IManager;
import org.jajuk.base.ItemType;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.items.Album;
import org.jajuk.base.items.Author;
import org.jajuk.base.items.Device;
import org.jajuk.base.items.Directory;
import org.jajuk.base.items.File;
import org.jajuk.base.items.Item;
import org.jajuk.base.items.Playlist;
import org.jajuk.base.items.PlaylistFile;
import org.jajuk.base.items.Style;
import org.jajuk.base.items.Track;
import org.jajuk.base.items.Year;
import org.jajuk.util.Filter;
import org.jajuk.util.Messages;
import org.jajuk.util.Resources.XML;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 *
 */
public class ItemManager<T extends IItem> implements IManager<T> {

  /** Items collection* */
  protected TreeMap<String, T>  items = null;

  /** Manager lock, should be synchronized before any iteration on items */
  protected byte[]              lock  = new byte[0];

  /**
   * Maps item classes -> instance, must be a linked map for ordering (mandatory
   * in commited collection)
   */
  static private LinkedHashMap<Class<? extends IItem>, IManager<? extends IItem>> hmItemManagers = null;

  /** Maps properties meta information name and object */
  private DataSet<MetaProperty> metaProperties  = null;


  /* (non-Javadoc)
   * @see org.jajuk.base.IManager#cleanup()
   */
  public void cleanup() {
    synchronized (getLock()) {
      getItems().clear();
      // build used items set
      final Set<Item> hsItems = new HashSet<Item>(1000);
      for (final Track track : ((TrackManager) ItemType.Track.getManager()).getCarbonItems()) {
        if (this instanceof AlbumManager) {
          hsItems.add(track.getAlbum());
        } else if (this instanceof AuthorManager) {
          hsItems.add(track.getAuthor());
        } else if (this instanceof StyleManager) {
          hsItems.add(track.getStyle());
        }
      }
      final Iterator<? extends IItem> it = getItems().values().iterator();
      while (it.hasNext()) {
        final IItem item = it.next();
        // check if this item still maps some tracks
        if (!hsItems.contains(item)) {
          // For styles, keep it even if none tracj uses it if it is a
          // default style
          if ((this instanceof StyleManager)
              && !((StyleManager) ItemType.Style.getManager()).getStylesList().contains(item.getName())) {
            it.remove();
          }
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IManager#getItems()
   */
  public TreeMap<String, T>        getItems() {
    synchronized (getLock()) {
      if (items == null) {
        items = new TreeMap<String, T>();
      }
      return (items);
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IManager#registerItem(org.jajuk.base.IItem)
   */
  public boolean                  registerItem(final T item) {
    synchronized (getLock()) {
      if (item != null) {
        getItems().put(item.getID(), item);
        return (true);
      }
    }
    return (false);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IManager#getLock()
   */
  public byte[]                   getLock() {
    return (lock);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IManager#getLabel()
   */
  public String                   getLabel() {
    synchronized (getLock()) {
      return ("");
    }
  }

  // --------------copy paste TODO: clean --------------------

  /**
   * Registrates a new item manager
   *
   * @param c
   *          Managed item class
   * @param itemManager
   */
  public static void registerItemManager(final Class<? extends IItem> c, final IManager<? extends IItem> itemManager) {
    getManagers().put(c, itemManager);
  }

  /**
   * @param propertyKey
   * @return meta data for given property
   */
  public MetaProperty getMetaInformation(final String propertyKey) {
    return (getMetaProperties().get(propertyKey));
  }

  /**
   * Return a human representation for a given property name when we don't now
   * item type we work on. Otherwise, use PropertyMetaInformation.getHumanType
   *
   * @param propertyKey
   * @return
   */
  public static String getHumanType(final String propertyKey) {
    final String  key = PROPERTY_SEPARATOR + propertyKey;

    return (Messages.getInstance().contains(key) ? Messages.getString(key) : propertyKey);
  }

  /** Remove a property * */
  public void removeProperty(final String sProperty) {
    final MetaProperty  meta = getMetaInformation(sProperty);

    getMetaProperties().remove(sProperty);
    applyRemoveProperty(meta); // remove this property to all items
  }

  /** Remove a custom property to all items for the given manager */
  @SuppressWarnings("unchecked")
  public void applyRemoveProperty(final MetaProperty meta) {
    synchronized (getLock()) {
      final Collection<T> items = getItems().values();

      for (final T item : items) {
        item.getProperties().remove(meta.getName());
      }
    }
  }

  /** Add a custom property to all items for the given manager */
  public void applyNewProperty(final MetaProperty meta) {
    synchronized (getLock()) {
      final Collection<T> items = getItems().values();

      for (final T item : items) {
        item.getProperties().set(meta.getName(), meta.getDefaultValue());
      }
    }
  }

  /**
   *
   * @return XML representation of this manager
   */
  public String toXML() {
    final StringBuilder sb = new StringBuilder(250);

    sb.append("\t<");
    sb.append(getLabel());
    sb.append(">");
    sb.append('\n');
    for (final MetaProperty mp : getProperties()) {
      sb.append(mp.toXML());
      sb.append('\n');
    }
    return (sb.toString());
  }

  /**
   * @return properties Meta informations
   */
  public Collection<MetaProperty> getProperties() {
    return (new ArrayList<MetaProperty>(getMetaProperties().getStore().values()));
  }

  /**
   * @return custom properties Meta informations
   */
  public Collection<MetaProperty> getCustomMetaProperties() {
    final ArrayList<MetaProperty> selected  = new ArrayList<MetaProperty>(getMetaProperties().getStore().size());

    for (final MetaProperty mp : getProperties()) {
      if (mp.isCustom()) {
        selected.add(mp);
      }
    }
    return (selected);
  }

  /**
   * @return visible properties Meta informations
   */
  public Collection<MetaProperty> getVisibleProperties() {
    final ArrayList<MetaProperty> selected  = new ArrayList<MetaProperty>(getMetaProperties().getStore().size());

    for (final MetaProperty mp : getProperties()) {
      if (mp.isVisible()) {
        selected.add(mp);
      }
    }
    return (selected);
  }

  /**
   * Get ItemManager manager with a given attribute name
   *
   * @param sItem
   * @return
   */
  public static IManager<? extends IItem> getItemManager(final String sProperty) {
    if (XML.DEVICE.equals(sProperty)) {
      return ItemType.Device.getManager();
    } else if (XML.TRACK.equals(sProperty)) {
      return ItemType.Track.getManager();
    } else if (XML.ALBUM.equals(sProperty)) {
      return ItemType.Album.getManager();
    } else if (XML.AUTHOR.equals(sProperty)) {
      return ItemType.Author.getManager();
    } else if (XML.STYLE.equals(sProperty)) {
      return ItemType.Style.getManager();
    } else if (XML.DIRECTORY.equals(sProperty)) {
      return ItemType.Directory.getManager();
    } else if (XML.FILE.equals(sProperty)) {
      return ItemType.File.getManager();
    } else if (XML.PLAYLIST_FILE.equals(sProperty)) {
      return ItemType.Playlist.getManager();
    } else if (XML.PLAYLIST.equals(sProperty)) {
      return ItemType.PlaylistFile.getManager();
    } else if (XML.TYPE.equals(sProperty)) {
      return ItemType.Type.getManager();
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
  public static IManager<? extends IItem> getItemManager(final Class<? extends IItem> c) {
    return getManagers().get(c);
  }

  /**
   * Return an iteration over item managers
   */
  public static Iterator<IManager<? extends IItem>> getItemManagers() {
    return getManagers().values().iterator();
  }

  public static LinkedHashMap<Class<? extends IItem>, IManager<? extends IItem>>  getManagers() {
    if (hmItemManagers == null) {
      hmItemManagers = new LinkedHashMap<Class<? extends IItem>, IManager<? extends IItem>>(10);
    }
    return (hmItemManagers);
  }

  /**
   * Perform a cleanup for a given item
   */
  public void cleanup(final Item item) {
    synchronized (getLock()) {
      if (((TrackManager) ItemType.Track.getManager()).getAssociatedTracks(item).size() == 0) {
        getItems().remove(item.getID());
      }
    }
  }

  /** Return all registred items with filter applied */
  public Collection<T> getItems(final Filter filter) {
    synchronized (getLock()) {
      final Collection<T> items     = getItems().values();
      final ArrayList<T>  filtered;

      if (filter == null) {
        filtered = (ArrayList<T>) items;
      }
      else {
        String  comparator  = null;
        String  checked     = filter.getValue();

        filtered = new ArrayList<T>(items.size());
        if (!filter.isExact()) {
          checked = ".*" + checked + ".*";
        }
        for (final T item : items) {
          // If none property set, the search if global "any"
          if (filter.getProperty() == null) {
            comparator = item.getAny();
          } else {
            if (filter.isHuman()) {
              comparator = item.getHumanValue(filter.getProperty().getName());
            } else {
              comparator = item.getStringValue(filter.getProperty().getName());
            }
          }
          // perform the test
          if (comparator.toLowerCase().matches(checked.toLowerCase())) {
            filtered.add(item);
          }
        }
      }
      return (filtered);
    }
  }

  /** Remove a given item */
  public synchronized void removeItem(final String sID) {
    synchronized (getLock()) {
      getItems().remove(sID);
    }
  }

  /**
   * Register a new property
   *
   * @param meta
   */
  public void registerProperty(final MetaProperty meta) {
    getMetaProperties().set(meta.getName(), meta);
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
  public static IItem changeItem(final IItem itemToChange, final String sKey, final Object oValue, final HashSet filter)
      throws JajukException {
    if (Log.isDebugEnabled()) {
      Log.debug("Set " + sKey + "=" + oValue.toString() + " to " + itemToChange);
    }
    IItem newItem = itemToChange;
    final TrackManager trackManager  = (TrackManager) ItemType.Track.getManager();
    if (itemToChange instanceof File) {
      final File file = (File) itemToChange;
      if (XML.NAME.equals(sKey)) { // file name
        newItem = ((FileManager) ItemType.File.getManager()).changeFileName((File) itemToChange, (String) oValue);
      } else if (XML.TRACK.equals(sKey)) { // track name
        newItem = trackManager.changeTrackName(file.getTrack(), (String) oValue,
            filter);
      } else if (XML.STYLE.equals(sKey)) {
        newItem = trackManager.changeTrackStyle(file.getTrack(), (String) oValue,
            filter);
      } else if (XML.ALBUM.equals(sKey)) {
        newItem = trackManager.changeTrackAlbum(file.getTrack(), (String) oValue,
            filter);
      } else if (XML.AUTHOR.equals(sKey)) {
        newItem = trackManager.changeTrackAuthor(file.getTrack(), (String) oValue,
            filter);
      } else if (XML.TRACK_COMMENT.equals(sKey)) {
        newItem = trackManager.changeTrackComment(file.getTrack(), (String) oValue,
            filter);
      } else if (XML.TRACK_ORDER.equals(sKey)) {
        newItem = trackManager.changeTrackOrder(file.getTrack(), (Long) oValue,
            filter);
      } else if (XML.YEAR.equals(sKey)) {
        newItem = trackManager.changeTrackYear(file.getTrack(),
            String.valueOf(oValue), filter);
      } else if (XML.TRACK_RATE.equals(sKey)) {
        newItem = trackManager.changeTrackRate(file.getTrack(), (Long) oValue);
      } else { // others properties
        itemToChange.getProperties().set(sKey, oValue);
      }
      // Get associated track file
      if (newItem instanceof Track) {
        file.setTrack((Track) newItem);
        newItem = file;
      }
    } else if (itemToChange instanceof PlaylistFile) {
      if (XML.NAME.equals(sKey)) { // playlistfile name
        newItem = ((PlaylistFileManager) ItemType.PlaylistFile.getManager()).changePlaylistFileName(
            (PlaylistFile) itemToChange, (String) oValue);
      }
    } else if (itemToChange instanceof Directory) {
      if (XML.NAME.equals(sKey)) { // file name
        // TBI newItem =
        // ((DirectoryManager) ItemType.Directory.getManager()).changeDirectoryName((Directory)itemToChange,(String)oValue);
      } else { // others properties
        itemToChange.getProperties().set(sKey, oValue);
      }
    } else if (itemToChange instanceof Device) {
      itemToChange.getProperties().set(sKey, oValue);
    } else if (itemToChange instanceof Playlist) {
      itemToChange.getProperties().set(sKey, oValue);
    } else if (itemToChange instanceof Track) {
      if (XML.NAME.equals(sKey)) {
        newItem = trackManager.changeTrackName((Track) itemToChange, (String) oValue,
            filter);
      } else if (XML.STYLE.equals(sKey)) {
        newItem = trackManager.changeTrackStyle((Track) itemToChange,
            (String) oValue, filter);
      } else if (XML.ALBUM.equals(sKey)) {
        newItem = trackManager.changeTrackAlbum((Track) itemToChange,
            (String) oValue, filter);
      } else if (XML.AUTHOR.equals(sKey)) {
        newItem = trackManager.changeTrackAuthor((Track) itemToChange,
            (String) oValue, filter);
      } else if (XML.TRACK_COMMENT.equals(sKey)) {
        newItem = trackManager.changeTrackComment((Track) itemToChange,
            (String) oValue, filter);
      } else if (XML.TRACK_ORDER.equals(sKey)) {
        newItem = trackManager.changeTrackOrder((Track) itemToChange, (Long) oValue,
            filter);
      } else if (XML.YEAR.equals(sKey)) {
        newItem = trackManager.changeTrackYear((Track) itemToChange,
            String.valueOf(oValue), filter);
      } else if (XML.TRACK_RATE.equals(sKey)) {
        newItem = trackManager.changeTrackRate((Track) itemToChange, (Long) oValue);
      } else { // others properties
        itemToChange.getProperties().set(sKey, oValue);
      }
    } else if (itemToChange instanceof Album) {
      if (XML.NAME.equals(sKey)) {
        newItem = ((AlbumManager) ItemType.Album.getManager()).changeAlbumName((Album) itemToChange, (String) oValue);
      } else { // others properties
        itemToChange.getProperties().set(sKey, oValue);
      }
    } else if (itemToChange instanceof Author) {
      if (XML.NAME.equals(sKey)) {
        newItem = ((AuthorManager) ItemType.Author.getManager()).changeAuthorName((Author) itemToChange,
            (String) oValue);
      } else { // others properties
        itemToChange.getProperties().set(sKey, oValue);
      }
    } else if (itemToChange instanceof Style) {
      if (XML.NAME.equals(sKey)) {
        newItem = ((StyleManager) ItemType.Style.getManager()).changeStyleName((Style) itemToChange, (String) oValue);
      } else { // others properties
        itemToChange.getProperties().set(sKey, oValue);
      }
    } else if (itemToChange instanceof Year) {
      itemToChange.getProperties().set(sKey, oValue);
    }
    return newItem;
  }

  /**
   *
   * @return number of item
   */
  public int getElementCount() {
    synchronized (getLock()) {
      return getItems().size();
    }
  }

  /**
   * @param sID
   *          Item ID
   * @return Item
   */
  public T getItemByID(final String sID) {
    synchronized (getLock()) {
      return getItems().get(sID);
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IManager#getMetaProperties()
   */
  public DataSet<MetaProperty>    getMetaProperties() {
    if (metaProperties == null) {
      metaProperties = new DataSet<MetaProperty>();
    }
    return (metaProperties);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IManager#getCarbonItems()
   */
  public Set<T>                   getCarbonItems() {
    final Set<T>                carbonItems = new LinkedHashSet<T>();

    synchronized (getLock()) {
      for (final T item : getItems().values()) {
        carbonItems.add(item);
      }
    }
    return (carbonItems);
  }
}
