/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.base;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jajuk.services.core.PersistenceService;
import org.jajuk.services.core.PersistenceService.Urgency;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Const;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;

/**
 * Generic property handler, parent class for all items
 * <p>
 * Note that some properties can be omitted (not in properties object), in this
 * case, we take default value given in meta infos, this can decrease collection
 * file size
 * </p>.
 */
public abstract class Item implements Const {
  /** We cache the ID to avoid getting it from properties for CPU performance reasons. */
  private String sID;
  /** We cache the name to avoid getting it from properties for CPU performance reasons. */
  String name;
  /** Item properties, singleton use very high load factor as this size will not change often. */
  private Map<String, Object> properties = new HashMap<String, Object>(2, 1f);
  /** Cache-string which holds the filter-string for the default "any"-Searches, this is filled during the first search and 
   * cleaned on all points where the properties are adjusted. */
  private String any = null;
  private static final List<String> lowPriorityCollectionProperties = Lists.asList(XML_TRACK_HITS,
      new String[] { XML_TRACK_TOTAL_PLAYTIME, XML_EXPANDED, XML_ALBUM_DISCOVERED_COVER,
          XML_TRACK_RATE, XML_ORIGIN });

  /**
   * Constructor.
   * 
   * @param sId element ID
   * @param sName element name
   */
  Item(final String sId, final String sName) {
    this.sID = sId;
    setProperty(Const.XML_ID, sId);
    this.name = sName;
    setProperty(Const.XML_NAME, sName);
  }

  /**
   * Gets the iD.
   * 
   * @return the iD
   */
  public String getID() {
    return this.sID;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Set a new name
   * @param newName
   */
  void setName(String newName) {
    this.name = newName;
    setProperty(XML_NAME, newName);
    notifyCollectionChange(XML_NAME);
  }

  /**
   * Item hashcode (used by the equals method) See
   * http://www.geocities.com/technofundo/tech/java/equalhash.html
   * 
   * Note that the hashCode is already cached in String class, no need to do it again.
   * 
   * @return the int
   */
  @Override
  public int hashCode() {
    return getID().hashCode();
  }

  /**
   * Get item title (HTML) used in some dialogs.
   * 
   * @return item description
   */
  public abstract String getTitle();

  /**
   * Equal method to check two items are identical.
   * 
   * @param otherItem 
   * 
   * @return true, if equals
   */
  @Override
  public boolean equals(Object otherItem) {
    // this also catches null
    if (!(otherItem instanceof Item)) {
      return false;
    }
    // [Perf] We can compare with an == operator here because
    // all ID are stored into String intern() buffer
    return getID() == ((Item) otherItem).getID();
  }

  /**
   * Get a defensive copy of all the item properties.
   * 
   * @return a defensive copy of all the item properties
   */
  public Map<String, Object> getProperties() {
    return new HashMap<String, Object>(properties);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getProperty(java.lang.String)
   */
  /**
   * Gets the value.
   * 
   * @param sKey 
   * 
   * @return the value
   */
  public Object getValue(String sKey) {
    Object out = properties.get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return getDefaultValue(sKey);
    }
    return out;
  }

  /**
   * Gets the long value.
   * 
   * @param sKey 
   * 
   * @return the long value
   */
  public long getLongValue(String sKey) {
    Long out = (Long) properties.get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return (Long) getDefaultValue(sKey);
    }
    return out;
  }

  /**
   * Gets the double value.
   * 
   * @param sKey 
   * 
   * @return the double value
   */
  public double getDoubleValue(String sKey) {
    Double out = (Double) properties.get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return (Double) getDefaultValue(sKey);
    }
    return out;
  }

  /**
   * Return String value for String type values. We assume that given property
   * is a String.
   * 
   * @param sKey 
   * 
   * @return the string value
   */
  public String getStringValue(String sKey) {
    String out = (String) properties.get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return (String) getDefaultValue(sKey);
    }
    return out;
  }

  /**
   * Gets the boolean value.
   * 
   * @param sKey 
   * 
   * @return the boolean value
   */
  public boolean getBooleanValue(String sKey) {
    Boolean out = (Boolean) properties.get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return (Boolean) getDefaultValue(sKey);
    }
    return out;
  }

  /**
   * Gets the date value.
   * 
   * @param sKey 
   * 
   * @return the date value
   */
  public Date getDateValue(String sKey) {
    Date out = (Date) properties.get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return (Date) getDefaultValue(sKey);
    }
    return out;
  }

  /**
   * Gets the default value.
   * 
   * @param sKey 
   * 
   * @return the default value
   */
  private Object getDefaultValue(String sKey) {
    PropertyMetaInformation meta = getMeta(sKey);
    return meta.getDefaultValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#containsKey(java.lang.String)
   */
  /**
   * Contains property.
   * 
   * 
   * @param sKey 
   * 
   * @return true if...
   */
  public boolean containsProperty(String sKey) {
    return properties.containsKey(sKey) && properties.get(sKey) != null
        && !properties.get(sKey).equals("");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#setProperty(java.lang.String, java.lang.String)
   */
  /**
   * Sets the property.
   * 
   * 
   * @param sKey 
   * @param oValue 
   */
  public final void setProperty(String sKey, Object oValue) {
    // reset cached value
    any = null;
    properties.put(sKey, oValue);
    notifyCollectionChange(sKey);
  }

  private final void notifyCollectionChange(String key) {
    // Ignore this if the persistence service is not yet started to speed up startup
    if (!PersistenceService.getInstance().isStarted()) {
      return;
    }
    // SmartPlaylist are not persisted
    if (this instanceof SmartPlaylist) {
      return;
    }
    // Webradios are stored outside the collection file and are persisted separately
    if (this instanceof WebRadio) {
      PersistenceService.getInstance().tagRadiosChanged();
    } else {
      // Some properties like the track total play time is incremented very often and we don't want to commit 
      // collection that soon so we set a low urgency to its commit
      if (lowPriorityCollectionProperties.contains(key)) {
        PersistenceService.getInstance().tagCollectionChanged(Urgency.LOW);
      } else {
        // On the contrary, we want others changes to be commited ASAP
        PersistenceService.getInstance().tagCollectionChanged(Urgency.HIGH);
      }
    }
  }

  /**
   * Gets the any.
   * 
   * @return the any
   */
  public String getAny() {
    if (any != null) {
      return any;
    }
    StringBuilder sb = new StringBuilder(100);
    Iterator<String> it = properties.keySet().iterator();
    while (it.hasNext()) {
      String sKey = it.next();
      String sValue = getHumanValue(sKey);
      if (sValue != null) {
        PropertyMetaInformation meta = getMeta(sKey);
        if (!meta.isVisible()) { // computes "any" only on
          // visible items
          continue;
        }
        sb.append(sValue);
      }
    }
    any = sb.toString();
    return any;
  }

  /**
   * Return an XML representation of this item.
   * 
   * @return the string
   */
  String toXml() {
    try {
      StringBuilder sb = new StringBuilder("<").append(getXMLTag());
      sb.append(getPropertiesXml());
      sb.append("/>\n");
      return sb.toString();
    } catch (Exception e) {
      // catch any error here because it can prevent
      // collection to commit
      Log.error(e);
      return "";
    }
  }

  /**
   * Gets the XML tag.
   * 
   * @return an identifier used to generate XML representation of this item
   */
  public abstract String getXMLTag();

  /**
   * Gets the properties xml.
   * 
   * @return XML representation for item properties
   */
  private String getPropertiesXml() {
    StringBuilder sb = new StringBuilder();
    for (String sKey : properties.keySet()) {
      String sValue = null;
      Object oValue = properties.get(sKey);
      if (oValue != null) {
        PropertyMetaInformation meta = getMeta(sKey);
        // The meta can be null for unknown reason, see #1226
        if (meta == null) {
          Log.warn("Null meta for: " + sKey);
          continue;
        }
        try {
          sValue = UtilString.format(oValue, meta, false);
          // make sure to remove
          // non-XML characters
          sValue = UtilString.formatXML(sValue);
          sb.append(' ');
          sb.append(UtilString.formatXML(sKey));
          sb.append("='");
          sb.append(sValue);
          sb.append("'");
        } catch (Exception e) { // should not occur
          Log.debug("Key=" + sKey + " Meta=" + meta);
          Log.error(e);
        }
      }
    }
    return sb.toString();
  }

  /**
   * Set all personal properties of an XML file for an item (doesn't overwrite
   * existing properties for perfs).
   * 
   * @param attributes :
   * list of attributes for this XML item
   */
  public void populateProperties(Attributes attributes) {
    for (int i = 0; i < attributes.getLength(); i++) {
      String sProperty = attributes.getQName(i);
      if (!properties.containsKey(sProperty)) {
        String sValue = attributes.getValue(i);
        PropertyMetaInformation meta = getMeta(sProperty);
        try {
          if (meta != null) {
            // small memory optimization: there are some properties that we do not automatically intern during collection loading, 
            // therefore do it manually here to not have the strings duplicated.
            // This is currently useful for "ALBUM_ARTIST" and for Const.NONE Cover in Albums
            // measured gain: aprox. 1MB for 25k tracks 
            if (Const.XML_ALBUM_ARTIST.equals(sProperty) || Const.COVER_NONE.equals(sValue)) {
              setProperty(sProperty, UtilString.parse(sValue.intern(), meta.getType()));
            } else {
              setProperty(sProperty, UtilString.parse(sValue, meta.getType()));
            }
          }
        } catch (Exception e) {
          Log.error(137, sProperty, e);
        }
      }
    }
    // remove cached value
    any = null;
  }

  /**
   * Sets the properties.
   * 
   * @param properties The properties to set.
   */
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
    // remove cached value
    any = null;
    notifyCollectionChange(null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#removeProperty(java.lang.String)
   */
  /**
   * Removes the property.
   * 
   * 
   * @param sKey 
   */
  public void removeProperty(String sKey) {
    properties.remove(sKey);
    // remove cached value
    any = null;
    notifyCollectionChange(null);
  }

  /**
   * Default implementation for this method, simply return standard value.
   * 
   * @param sKey 
   * 
   * @return the human value
   */
  public String getHumanValue(String sKey) {
    try {
      return UtilString.format(getValue(sKey), getMeta(sKey), true);
    } catch (Exception e) {
      Log.error(e);
      return "";
    }
  }

  /**
   * Gets the meta.
   * 
   * @param sProperty Property name
   * 
   * @return Meta information for current item and given property name
   */
  public PropertyMetaInformation getMeta(String sProperty) {
    return ItemManager.getItemManager(this.getClass()).getMetaInformation(sProperty);
  }

  /**
   * Clone all properties from a given properties list but not overwrite
   * constructor properties.
   * 
   * @param propertiesSource 
   */
  void cloneProperties(Item propertiesSource) {
    Iterator<String> it = propertiesSource.getProperties().keySet().iterator();
    while (it.hasNext()) {
      String sProperty = it.next();
      if (!getMeta(sProperty).isConstructor()) {
        this.properties.put(sProperty, propertiesSource.getValue(sProperty));
      }
    }
    // reset cached value
    any = null;
  }

  /**
   * Gets the icon representation.
   * 
   * @return an icon representation for this item or null if none available
   */
  public abstract ImageIcon getIconRepresentation();

  /**
   * Item rate. Should be overwritten by sub classes
   * 
   * @return item rate if item supports rating or -1 otherwise
   */
  public long getRate() {
    return -1;
  }
}
