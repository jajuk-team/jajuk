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
package org.jajuk.services.webradio;

import java.util.Iterator;
import java.util.List;

import org.jajuk.base.ItemManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilSystem;

/**
 * Stores webradios configurated by user
 * <p>
 * Singleton
 * </p>.
 */
public final class WebRadioManager extends ItemManager {
  /** Self instance. */
  private static WebRadioManager self = new WebRadioManager();

  /**
   * Instantiates a new web radio manager.
   */
  private WebRadioManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, true, false,
        String.class, null));
    //Description
    registerProperty(new PropertyMetaInformation(Const.XML_DESC, false, false, true, true, false,
        String.class, null));
    // URL
    registerProperty(new PropertyMetaInformation(Const.XML_URL, false, false, true, true, false,
        String.class, null));
    // Origin
    registerProperty(new PropertyMetaInformation(Const.XML_ORIGIN, false, false, true, false, true,
        String.class, null));
    // Bitrate
    registerProperty(new PropertyMetaInformation(Const.XML_BITRATE, false, false, true, true, true,
        Long.class, null));
    //Frequency
    registerProperty(new PropertyMetaInformation(Const.XML_FREQUENCY, false, false, true, true,
        true, Long.class, null));
    // Keywords
    registerProperty(new PropertyMetaInformation(Const.XML_KEYWORDS, false, false, true, true,
        false, String.class, null));
    // Genre
    registerProperty(new PropertyMetaInformation(Const.XML_GENRE, false, false, true, true, true,
        String.class, null));
  }

  /**
  * Get web radio hashcode (ID).
  * 
  * @param name
  * 
  * @return radio ID
  */
  protected static String createID(String sName) {
    // The webradio id is simply a shuffle number because we want to allow
    // name change. We accept several webradios with the same name;
    // We don't use date or time for this id because time can go backward (winter time)
    // and we could get twice the same number.
    return MD5Processor.hash(Long.toString(UtilSystem.getRandom().nextLong()));
  }

  /**
  * Register a web radio.
  * 
  * @param web radio name
  * 
  * @return the web radio.
  */
  public WebRadio registerWebRadio(String name) {
    String sId = createID(name);
    return registerWebRadio(sId, name);
  }

  /**
  * Register a web radio.
  *
  * @param id
  * @param web radio name
  * 
  * @return the web radio.
  */
  private WebRadio registerWebRadio(String sId, String name) {
    WebRadio radio = getWebRadioByID(sId);
    if (radio != null) {
      return radio;
    }
    radio = new WebRadio(sId, name);
    registerItem(radio);
    return radio;
  }

  /**
   * Gets the radio by id.
   * 
   * @param sID Item ID
   * 
   * @return Element
   */
  WebRadio getWebRadioByID(String sID) {
    return (WebRadio) getItemByID(sID);
  }

  /**
   * Gets the single instance of WebRadioManager.
   * 
   * @return single instance of WebRadioManager
   */
  public static WebRadioManager getInstance() {
    return self;
  }

  /**
  * Gets the first webradio found with the provided name.
  * 
  * @param name web radio name
  * 
  * @return WebRadio for a given name or null if no match
  */
  public WebRadio getWebRadioByName(String name) {
    for (WebRadio radio : getWebRadios()) {
      if (radio.getName().equals(name)) {
        return radio;
      }
    }
    return null;
  }

  /* (non-Javadoc)
  * @see org.jajuk.base.ItemManager#getXMLTag()
  */
  @Override
  public String getXMLTag() {
    // Not used here, webradios have their own storage system.
    return null;
  }

  /**
  * Gets the web radios.
  * 
  * @return webradios list
  */
  @SuppressWarnings("unchecked")
  public List<WebRadio> getWebRadios() {
    return (List<WebRadio>) getItems();
  }

  /**
  * Gets a subset of web radios by Origin.
  * @param origin
  * @return webradios list
  */
  public List<WebRadio> getWebRadiosByOrigin(WebRadioOrigin origin) {
    @SuppressWarnings("unchecked")
    List<WebRadio> radios = (List<WebRadio>) getItems();
    Iterator<WebRadio> itRadios = radios.iterator();
    while (itRadios.hasNext()) {
      WebRadio radio = itRadios.next();
      if (radio.getOrigin() != origin) {
        itRadios.remove();
      }
    }
    return radios;
  }

  /**
   * Gets the web radios iterator.
   * 
   * @return web radios iterator
   */
  @SuppressWarnings("unchecked")
  public ReadOnlyIterator<WebRadio> getWebRadioIterator() {
    return new ReadOnlyIterator<WebRadio>((Iterator<WebRadio>) getItemsIterator());
  }
}
