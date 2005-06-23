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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.jajuk.util.MD5Processor;

/**
 * Convenient class to manage styles
 * @author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class StyleManager extends ItemManager {
	/** Styles collection* */
	static HashMap hmStyles = new HashMap(10);
    /**Self instance*/
    static StyleManager singleton;

	/**
	 * No constructor available, only static access
	 */
	private StyleManager() {
		super();
	}

    /**
     * @return singleton
     */
    public static ItemManager getInstance(){
      if (singleton == null){
          singleton = new StyleManager();
      }
        return singleton;
    }

	/**
	 * Register a style
	 * 
	 * @param sName
	 */
	public static synchronized Style registerStyle(String sName) {
		String sId = MD5Processor.hash(sName.trim().toLowerCase());
		return registerStyle(sId, sName);
	}

	/**
	 * Register a style with a known id
	 * 
	 * @param sName
	 */
	public static synchronized Style registerStyle(String sId, String sName) {
		String sIdTest = MD5Processor.hash(sName.trim().toLowerCase());
		 //Hash checkup
        if (!sId.equals(sIdTest)){ //collection corruption, ignore this entry
                return null;
        }
		if (hmStyles.containsKey(sIdTest)) {
			return (Style) hmStyles.get(sIdTest);
		}
		Style style = new Style(sId, sName);
		hmStyles.put(sId, style);
		return style;
	}

	/**
	 * Remove a style
	 * 
	 * @param style
	 *                   id
	 */
	public static synchronized void remove(String sId) {
		hmStyles.remove(sId);
	}
	
	
	/**
	 * Perform a style cleanup : delete useless items
	 *
	 */
	public static synchronized void cleanup(){
		HashSet hs = new HashSet(100);
		Iterator itTracks = TrackManager.getTracks().iterator();
		while (itTracks.hasNext()) {
			Track track = (Track) itTracks.next();
			Style style = track.getStyle();
			hs.add(style);
		}
		Iterator itStyles = hmStyles.values().iterator();
		while (itStyles.hasNext()) {
			Style style = (Style) itStyles.next();
			if ( !hs.contains(style)){
				itStyles.remove();
			}
		}
	}

	/**
	 * Format the Style name to be normalized :
	 * <p>
	 * -no underscores or other non-ascii characters
	 * <p>
	 * -no spaces at the begin and the end
	 * <p>
	 * -All in upper case
	 * <p>
	 * exemple: "ROCK"
	 * 
	 * @param sName
	 * @return
	 */
	private static synchronized String format(String sName) {
		String sOut;
		sOut = sName.trim(); //supress spaces at the begin and the end
		sOut.replace('-', ' '); //move - to space
		sOut.replace('_', ' '); //move _ to space
		sOut = sOut.toUpperCase();
		return sOut;
	}

	/** Return all registred styles */
	public static synchronized ArrayList getStyles() {
		return new ArrayList(hmStyles.values());
	}

	/**
	 * Return style by id
	 * 
	 * @param sId
	 * @return
	 */
	public static synchronized Style getStyle(String sId) {
		return (Style) hmStyles.get(sId);
	}
	
 /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getIdentifier()
     */
    public String getIdentifier() {
        return XML_STYLES;
    }

    /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#applyNewProperty()
     */
    public void applyNewProperty(String sProperty){
        Iterator it = getStyles().iterator();
        while (it.hasNext()){
            IPropertyable item = (IPropertyable)it.next();
            item.setProperty(sProperty,null);
        }
    }
    
     /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#applyRemoveProperty(java.lang.String)
     */
    public void applyRemoveProperty(String sProperty) {
        Iterator it = getStyles().iterator();
        while (it.hasNext()){
            IPropertyable item = (IPropertyable)it.next();
            item.removeProperty(sProperty);
        }
    }

}