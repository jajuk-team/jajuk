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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;

/**
 * Generic property handler
 * @author Bertrand Florat 
 * @created 17 oct. 2003
 */
abstract public class PropertyAdapter implements IPropertyable, ITechnicalStrings,Serializable {
	
	/** Item properties, singleton */
	private LinkedHashMap properties;
    
	/** ID. Ex:1,2,3... */
    protected String sId;
    /** Name */
    protected String sName;
    /** "Any" value : concatenation of all properties*/
    protected String sAny ="";
    /** Flag for need to refresh any criteria */
    protected boolean bNeedRefresh = true;
    /**Constructor elements*/
    protected ArrayList alConstructorElements = new ArrayList(5);
 	
    
    /**
     * Constructor
     * @param sId element ID
     * @param sName element name
     */
    PropertyAdapter(String sId,String sName){
        alConstructorElements.add(XML_ID);
    	setId(sId);
        alConstructorElements.add(XML_NAME);
    	setName(sName);
    }
    
    /**
     * @return
     */
    public String getId() {
        return sId;
    }
    
    /**
     * @return
     */
    public String getName() {
        return sName;
    }
    
    /*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Propertyable#getProperties()
	 */
	public LinkedHashMap getProperties() {
		if ( properties == null){
			properties = new LinkedHashMap(10);
		}
		return properties;
	}
	
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Propertyable#getProperty(java.lang.String)
	 */
	public String getValue(String sKey) {
		if ( sKey == null ){
			return null;
		}
		//get property singleton
        LinkedHashMap properties = getProperties();
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
        return properties.containsKey(sKey) 
            && properties.get(sKey) != null 
            && !properties.get(sKey).equals(""); //$NON-NLS-1$
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Propertyable#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String sKey, String sValue) {
	    LinkedHashMap properties = getProperties();
		properties.put(sKey, sValue);
        bNeedRefresh = true; //notice getAny to we need to rebuild Any criteria
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getAny()
     */
    public String getAny(){
        return null;
    }
	
    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#setDefaultProperty(java.lang.String, java.lang.String)
     */
    public void setDefaultProperty(String sKey, String sValue) {
        LinkedHashMap properties = getProperties();
		if ( properties.containsKey(sKey)){
			return;
		}
		setProperty(sKey,sValue);
	}
		
	/**
     * Return an XML representation of this item  
     * @return
     */
    public String toXml() {
        try{
            StringBuffer sb = new StringBuffer("\t\t<").append(getIdentifier()); //$NON-NLS-1$
            sb.append(getPropertiesXml());
            sb.append("/>\n"); //$NON-NLS-1$
            return sb.toString();
        }
        catch(Exception e){ //catch any error here bcause it can prevent collection to commit
            Log.error(e);
            return "";
        }
    }
    
    private String getPropertiesXml() {
        LinkedHashMap properties = getProperties();
		Iterator it = properties.keySet().iterator();
     	StringBuffer sb = new StringBuffer(); //$NON-NLS-1$
		while (it.hasNext()) {
			String sKey = (String) it.next();
			String sValue = Util.formatXML((String)properties.get(sKey));
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
	public void setProperties(LinkedHashMap properties) {
		this.properties = properties;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPropertyable#removeProperty(java.lang.String)
	 */
	public void removeProperty(String sKey) {
        LinkedHashMap properties = getProperties();
		if (properties.containsKey(sKey)){
			properties.remove(sKey);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.IPropertyable#displayProperty()
	 */
	public void displayProperties() {
	}
    
 /**
     * @param id The sId to set.
     */
    public void setId(String id) {
        sId = id;
        setProperty(XML_ID,id);
    }

    /**
     * @param name The sName to set.
     */
    public  void setName(String name) {
        sName = name;
        setProperty(XML_NAME,name);
    }
	
    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#isPropertyEditable()
     */
    abstract public boolean isPropertyEditable(String sProperty);
	
    /**
     * Default implementation for this method, simply return standard value
     */
    public String getHumanValue(String sKey){
        return getValue(sKey);
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#isPropertyConstructorElement(java.lang.String)
     */
    public boolean isPropertyConstructorElement(String sProperty) {
    	return alConstructorElements.contains(sProperty);
	}
    
    /**
     * Clone all properties from a given properties list but not overwrite constructor properties
     * @param propertiesSource
     */
    public void cloneProperties(IPropertyable propertiesSource){
    	Iterator it = propertiesSource.getProperties().keySet().iterator();
    	while (it.hasNext()){
    		String sProperty = (String)it.next();
    		if (!isPropertyConstructorElement(sProperty)){
    			this.properties.put(sProperty,propertiesSource.getValue(sProperty));
    		}
    	}
    }
}
