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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.xml.sax.Attributes;

/**
 * Generic property handler
 * @author Bertrand Florat 
 * @created 17 oct. 2003
 */
public class PropertyAdapter implements IPropertyable, ITechnicalStrings,Serializable {
	
	/** Item properties, singleton */
	private Properties properties;
	
	/**
	 * Property adapter constructor
	 */
	public PropertyAdapter() {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Propertyable#getProperties()
	 */
	public Properties getProperties() {
		if ( properties == null){
			properties = new Properties();
		}
		return properties;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Propertyable#getProperty(java.lang.String)
	 */
	public String getProperty(String sKey) {
		if ( sKey == null ){
			return null;
		}
		//get property singleton
		Properties properties = getProperties();
		//test standard properties
		if ( sKey.equals(Messages.getString("PhysicalTableView.7"))){ //Track //$NON-NLS-1$
			if ( this instanceof File ){
				return ((File)this).getTrack().getName();
			}
			else if (this instanceof Track){ 
				return ((Track)this).getName();
			}
			else{
				return null;
			}
		}
		else if ( sKey.equals(Messages.getString("PhysicalTableView.8"))){ //Album //$NON-NLS-1$
			if ( this instanceof File ){
				return ((File)this).getTrack().getAlbum().getName2();
			}
			else if (this instanceof Track){ 
				return ((Track)this).getAlbum().getName2();
			}
			else{
				return null;
			}
		}
		else if ( sKey.equals(Messages.getString("PhysicalTableView.9"))){ //Author //$NON-NLS-1$
			if ( this instanceof File ){
				return ((File)this).getTrack().getAuthor().getName2();
			}
			else if (this instanceof Track){ 
				return ((Track)this).getAuthor().getName2();
			}
			else{
				return null;
			}
		}
		else if ( sKey.equals(Messages.getString("PhysicalTableView.10"))){ //Length //$NON-NLS-1$
			if ( this instanceof File ){
				return Long.toString(((File)this).getTrack().getLength());
			}
			else if (this instanceof Track){ 
				return Long.toString(((Track)this).getLength());
			}
			else{
				return null;
			}
		}
		else if ( sKey.equals(Messages.getString("PhysicalTableView.11"))){ //Style //$NON-NLS-1$
			if ( this instanceof File ){
				return ((File)this).getTrack().getStyle().getName2();
			}
			else if (this instanceof Track){ 
				return ((Track)this).getStyle().getName2();
			}
			else{
				return null;
			}
		}
		else if ( sKey.equals(Messages.getString("PhysicalTableView.12"))){ //Device, only physical  //$NON-NLS-1$
			if ( this instanceof File ){
				return ((File)this).getDirectory().getDevice().getName();
			}
			else{
				return null;
			}
		}
		else if ( sKey.equals(Messages.getString("PhysicalTableView.13"))){ //File, only physical  //$NON-NLS-1$
			if ( this instanceof File ){
				return ((File)this).getName();
			}
			else{
				return null;
			}
		}
		else if ( sKey.equals(Messages.getString("PhysicalTableView.14"))){ //Rate //$NON-NLS-1$
			if ( this instanceof File ){
				return Long.toString(((File)this).getTrack().getRate());
			}
			else if (this instanceof Track){ 
				return Long.toString(((Track)this).getRate());
			}
			else{
				return null;
			}
		}
		//must be a property
		if ( !properties.containsKey(sKey)){ //no more? return null
			return null;
		}
		return (String) properties.get(sKey); //return property value
	}
	
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.base.Propertyable#containsKey(java.lang.String)
     */
    public boolean containsProperty(String sKey) {
        return properties.containsKey(sKey) && !properties.get(sKey).equals("");
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Propertyable#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String sKey, String sValue) {
		//JRE properties doesn't accept null value
		if (sValue == null){
			sValue = ""; //$NON-NLS-1$
		}
		Properties properties = getProperties();
		//Using standard attributes is forbidden
		for (int i=0;i<XML_RESERVED_ATTRIBUTE_NAMES.length;i++){
			if (sKey.equals(XML_RESERVED_ATTRIBUTE_NAMES[i])){
				Messages.showErrorMessage("110")	; //$NON-NLS-1$
				return;
			}
		}
		properties.put(sKey, sValue);
	}
	
	public void setDefaultProperty(String sKey, String sValue) {
		Properties properties = getProperties();
		if ( properties.containsKey(sKey)){
			return;
		}
		setProperty(sKey,sValue);
	}
	
	
	public String getPropertiesXml() {
		Properties properties = getProperties();
		Enumeration e = properties.propertyNames();
		StringBuffer sb = new StringBuffer(""); //$NON-NLS-1$
		while (e.hasMoreElements()) {
			String sKey = (String) e.nextElement();
			String sValue = Util.formatXML(properties.getProperty(sKey));
			sb.append(" "+sKey + "='" + sValue + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return sb.toString();
	}
	
	/**
	 * Set all personnal properties of an XML file for an item
	 * 
	 * @param attributes :
	 *                list of attributes for this XML item
	 * @param index :
	 *                index of the first non-standard attribute
	 */
	public void populateProperties(Attributes attributes, int index) {
		if (attributes.getLength() >= index) { //found some properties
			for (int i = index; i < attributes.getLength(); i++) {
				setProperty(attributes.getQName(i), attributes.getValue(i));
			}
		}
	}
	
	/**
	 * @param properties The properties to set.
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPropertyable#removeProperty(java.lang.String)
	 */
	public void removeProperty(String sKey) {
		Properties properties = getProperties();
		if (properties.containsKey(sKey)){
			properties.remove(sKey);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPropertyable#displayProperty()
	 */
	public void displayProperties() {
	}
	
	
	
}
