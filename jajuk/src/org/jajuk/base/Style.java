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
import java.util.Collections;
import java.util.Iterator;

import org.jajuk.i18n.Messages;

/**
 * A music style ( jazz, rock...)
 * <p>
 * Logical item
 * @author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class Style extends PropertyAdapter implements Comparable{

	/** Authors for this style */
	private ArrayList alAuthors = new ArrayList(10);
  
	/**
     * Style constructor
     * 
     * @param id
     * @param sName
     */
	public Style(String sId, String sName) {
        super(sId,sName);
	}

/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getIdentifier()
     */
    public String getIdentifier() {
        return XML_STYLE;
    }
    
   
	/**
	 * Return style name, dealing with unkwnown for any language
	 * @return author name
	 */
	public String getName2() {
		String sOut = getName();
		if (sOut.equals(UNKNOWN_STYLE)){ 
			sOut = Messages.getString(UNKNOWN_STYLE);
		}
		return sOut;
	}

	/**
	 * toString method
	 */
	public String toString() {
		return "Style[ID=" + sId + " Name=" + getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
		
	/**
	 * Equal method to check two styles are identical
	 * 
	 * @param otherStyle
	 * @return
	 */
	public boolean equals(Object otherStyle) {
		return this.getId().equals(((Style)otherStyle).getId());
	}
	
	/**
	 * hashcode ( used by the equals method )
	 */
	public int hashCode(){
		return getId().hashCode();
	}


	/**
	 * @return
	 */
	public ArrayList getAuthors() {
		return alAuthors;
	}

	/**
	 * @param album
	 */
	public void addAuthor(Author author) {
		alAuthors.add(author);
	}

	/**
	 *Alphabetical comparator used to display ordered lists
	 *@param other item to be compared
	 *@return comparaison result 
	 */
	public int compareTo(Object o){
		Style otherStyle = (Style)o;
		return  getName2().compareToIgnoreCase(otherStyle.getName2());
	}
	
	/**
	 * @return Number of tracks for this style from the collection
	 */
	public int getCount(){
		int i = 0;
		Iterator it = TrackManager.getTracks().iterator();
		while ( it.hasNext()){
			Track track = (Track)it.next();
			if ( this.equals(track.getStyle())) {
				i++;
			}
		}
		return i;
	}
	
	
	/**
	 * return tracks associated with this item
	 * @return tracks associated with this item
	 */
	public ArrayList getTracks() {
		ArrayList alTracks = new ArrayList(100);
		Iterator it = TrackManager.getTracks().iterator();
		while ( it.hasNext()){
			Track track = (Track)it.next();
			if ( track != null && track.getStyle().equals(this)){
				alTracks.add(track);
			}
		}
		Collections.sort(alTracks);
		return alTracks;
	}
	
	/**
	 * @return whether the style is Unknown or not
	 */
	public boolean isUnknown(){
	    return this.getName().equals(UNKNOWN_STYLE); 
   }

    /**
     * Get item description
     */
    public String getDesc(){
        return "<HTML><b>"+Messages.getString("LogicalTreeView.5")+" : "+getName()+"</b><HTML>";
    }
  
/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#isPropertyEditable()
     */
    public boolean isPropertyEditable(String sProperty){
        if (XML_ID.equals(sProperty)){
            return false;
        }
        else if (XML_NAME.equals(sProperty)){
            return true;
        }
        else if (XML_EXPANDED.equals(sProperty)){
            return true;
        }
        else{
            return true;
        }
    }    
    
}
