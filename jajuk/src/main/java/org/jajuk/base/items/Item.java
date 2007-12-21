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
package org.jajuk.base.items;

import static org.jajuk.util.Resources.XML.ID;
import static org.jajuk.util.Resources.XML.NAME;
import static org.jajuk.util.Resources.XML.TYPE;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import javax.swing.ImageIcon;

import org.jajuk.base.DataSet;
import org.jajuk.base.IItem;
import org.jajuk.base.IManager;
import org.jajuk.base.ItemType;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.managers.ItemManager;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;

/**
 *
 */
public class Item implements IItem, Serializable {

  /** Serial ID                                                          */
  private static final long serialVersionUID = -6291513375439465295L;

  /** ManagedItem's type                                                 */
  private ItemType type = null;
  /** ManagedItem's ID                    (cached for CPU conservation)  */
  private String id = null;
  /** ManagedItem's name identifier       (cached for CPU conservation)  */
  private String name = null;
  /** ManagedItem's label                 (cached for CPU conservation)  */
  private String label = null;
  /** ManagedItem's full-text description (cached for CPU conservation)  */
  private String description = null;
  /** ManagedItem's properties                                           */
  private DataSet<Object> properties = null;

  /**
   * Default constructor for ItemItems
   *
   * @param id
   * @param name
   */
  public Item(final String id, final String name) {
    this(id, name, ItemType.Item);
  }

  /**
   * Standard Constructor.
   * This one should be called by the subclasses constructors.
   *
   * @param id
   * @parem name
   * @param type
   */
  public Item(final String id, final String name, final ItemType type) {
    setID(id);
    setName(name);
    this.type = type;
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#getID()
   */
  public String getID() {
    return (id);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#getLabel()
   */
  public String getLabel() {
    return ("");
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#getName()
   */
  public String getName() {
    return (name);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#getType()
   */
  public long getType() {
    return ((Long) getProperties().get(TYPE));
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#getItemType()
   */
  public ItemType getItemType() {
    return (type);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#getManager()
   */
  public IManager<? extends IItem> getManager() {
    return (type.getManager());
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#getProperties()
   */
  public DataSet<Object> getProperties() {
    if (properties == null) {
      properties = new DataSet<Object>();
    }
    return (properties);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#getIcon()
   */
  public ImageIcon getIcon() {
    return (null);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#getDescription()
   */
  public String getDescription() {
    return (description);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#setDescription(java.lang.String)
   */
  public String setDescription(final String description) {
    final String formerDesc = this.description;

    this.description = description;
    return (formerDesc);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#setID(java.lang.String)
   */
  public String setID(final String id) {
    this.id = id;
    return ((String) (getProperties().set(ID, id)));
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#setLabel(java.lang.String)
   */
  public String setLabel(final String label) {
    final String formerLabel = this.label;

    this.label = label;
    return (formerLabel);
  }

  /* (non-Javadoc)
   * @see org.jajuk.base.IItem#setName(java.lang.String)
   */
  public String setName(final String name) {
    this.name = name;
    return ((String) (getProperties().set(NAME, name)));
  }

  /**
   * Item hashcode (used by the equals method)
   */
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (getID().hashCode());
  }

  /**
   * equals() method to check that 2 items are identical
   *
   * @param item
   *                item to compare the current instance to
   * @return
   *                boolean identity status
   */
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object item) {
    return ((item instanceof Item) && getID().equals(((Item) item).getID()));
  }

  /**
   * @param source
   * @return
   */
  public static String generateID(final String source) {
    return (MD5Processor.hash(source));
  }

  // ---------------- copy & paste - TODO: clean ---------------

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getProperty(java.lang.String)
   */
  public Object getValue(final String sKey) {
    final Object out = getProperties().get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return getDefaultValue(sKey);
    }
    return out;
  }

  public long getLongValue(final String sKey) {
    final Long out = (Long) getProperties().get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return (Long) getDefaultValue(sKey);
    }
    return out;
  }

  public double getDoubleValue(final String sKey) {
    final Double out = (Double) getProperties().get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return (Double) getDefaultValue(sKey);
    }
    return out;
  }

  /**
   * Return String value for String type values. We assume that given property
   * is a String. If you are not sure, use Util.parse method
   */
  public String getStringValue(final String sKey) {
    final String out = (String) getProperties().get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return (String) getDefaultValue(sKey);
    }
    return out;
  }

  public boolean getBooleanValue(final String sKey) {
    final Boolean out = (Boolean) getProperties().get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return (Boolean) getDefaultValue(sKey);
    }
    return out;
  }

  public Date getDateValue(final String sKey) {
    final Date out = (Date) getProperties().get(sKey);
    // look at properties to check the given property is known
    if (out == null) {
      // no? take property default
      return (Date) getDefaultValue(sKey);
    }
    return out;
  }

  public Object getDefaultValue(final String sKey) {
    final MetaProperty meta = getMeta(sKey);

    return meta.getDefaultValue();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#getAny()
   */
  public String getAny() {
    final StringBuilder sb = new StringBuilder(100);
    final DataSet<Object> properties = getProperties();
    final Iterator it = properties.getStore().keySet().iterator();
    while (it.hasNext()) {
      final String sKey = (String) it.next();
      final String sValue = getHumanValue(sKey);
      if (sValue != null) {
        final MetaProperty meta = getMeta(sKey);
        if (!meta.isVisible()) { // computes "any" only on
          // visible items
          continue;
        }
        sb.append(sValue);
      }

    }
    return sb.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#setDefaultProperty(java.lang.String,
   *      java.lang.String)
   */
  public void populateDefaultProperty(final MetaProperty meta) {
    getProperties().set(meta.getName(), meta.getDefaultValue());
  }

  /**
   * Return an XML representation of this item
   *
   * @return
   */
  public String toXml() {
    try {
      final StringBuilder sb = new StringBuilder("\t\t<").append(getLabel());

      sb.append(getPropertiesXml());
      sb.append("/>\n");
      return sb.toString();
    } catch (final Exception e) {
      // catch any error here because it can prevent
      // collection to commit
      Log.error(e);
      return "";
    }
  }

  /**
   *
   * @return XML representation for item properties
   */
  private String getPropertiesXml() {
    final DataSet<Object> properties = getProperties();
    final StringBuilder sb = new StringBuilder();
    for (final String sKey : properties.getStore().keySet()) {
      String sValue = null;
      final Object oValue = properties.get(sKey);
      if (oValue != null) {
        final MetaProperty meta = getMeta(sKey);
        try {
          sValue = Util.format(oValue, meta, false);
        } catch (final Exception e) { // should not occur
          Log.error(e);
        }
        sValue = Util.formatXML(sValue); // make sure to remove
        // non-XML characters
      }
      sb.append(' ');
      sb.append(Util.formatXML(sKey));
      sb.append("='");
      sb.append(sValue);
      sb.append("'");
    }
    return sb.toString();
  }

  /**
   * Set all personnal properties of an XML file for an item (doesn't overwrite
   * existing properties for perfs)
   *
   * @param attributes :
   *          list of attributes for this XML item
   */
  public void populateProperties(final Attributes attributes) {
    final DataSet<Object> properties = getProperties();
    final int size = attributes.getLength();

    for (int i = 0; i < size; i++) {
      final String sProperty = attributes.getQName(i);

      if (!properties.contains(sProperty)) {
        final String sValue = attributes.getValue(i);
        final MetaProperty meta = getMeta(sProperty);
        try {
          if (meta != null) {
            properties.set(sProperty, Util.parse(sValue, meta.getType()));
          }
        } catch (final Exception e) {
          Log.error(137, sProperty, e);
        }
      }
    }
  }

  /**
   * @param properties
   *          The properties to set.
   */
  public void setProperties(final DataSet<Object> properties) {
    this.properties = properties;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#removeProperty(java.lang.String)
   */
  public void removeProperty(final String sKey) {
    final DataSet<Object> properties = getProperties();

    if (properties.contains(sKey)) {
      properties.remove(sKey);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Item#displayProperty()
   */
  public void displayProperties() {
  }

  /**
   * Default implementation for this method, simply return standard value
   */
  public String getHumanValue(final String sKey) {
    try {
      return Util.format(getValue(sKey), getMeta(sKey), true);
    } catch (final Exception e) {
      Log.error(e);
      return "";
    }
  }

  /**
   * @param sProperty
   *          Property name
   * @return Meta information for current item and given property name
   */
  public MetaProperty getMeta(final String sProperty) {
    return (ItemManager.getItemManager(this.getClass()).getMetaProperties().get(sProperty));
  }

  /**
   * Clone all properties from a given properties list but not overwrite
   * constructor properties
   *
   * @param propertiesSource
   */
  public void cloneProperties(final Item propertiesSource) {
    for (final String key : propertiesSource.getProperties().getStore().keySet()) {
      if (!getMeta(key).isConstructor()) {
        getProperties().set(key, propertiesSource.getValue(key));
      }
    }
  }

}
