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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;

/**
 * Generic property handler, mother class for all items
 * @author Bertrand Florat 
 * @created 17 oct. 2003
 */
abstract public class PropertyAdapter implements IPropertyable, Serializable,ITechnicalStrings {
	
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
       
    /**
     * Constructor
     * @param sId element ID
     * @param sName element name
     */
    PropertyAdapter(String sId,String sName){
      setId(sId);
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
	public void setProperty(String sKey, Object oValue) {
	    LinkedHashMap properties = getProperties();
		properties.put(sKey, oValue);
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
    public void setDefaultProperty(PropertyMetaInformation meta) {
        //Supported types: long,double,string,date, boolean, class
        Object oValue = "";
        String sDefault = meta.getDefaultValue();
        if (meta.getType() == Long.class){
            if (sDefault == null){
                oValue = 0l;
            }
            else{
                oValue = new Long(meta.getDefaultValue());
            }
        }
        else if (meta.getType() == Double.class){
            if (sDefault == null){
                oValue = 0;
            }
            else{
                oValue = new Integer(meta.getDefaultValue());
            }
        }
        else if (meta.getType() == String.class){
            if (sDefault == null){
                oValue = "";
            }
            else{
                oValue = meta.getDefaultValue();
            }
        }
        else if (meta.getType() == Boolean.class){
            if (sDefault == null){
                oValue = false;
            }
            else{
                oValue = new Boolean(meta.getDefaultValue());
            }
        }
        else if (meta.getType() == Class.class){
            if (sDefault == null){
                oValue = null;
            }
            else{
                oValue = meta.getDefaultValue();
            }
        }
        else if (meta.getType() == Date.class){
            if (sDefault == null){
                oValue = new Date();
            }
            else{
                try {
                    oValue = new SimpleDateFormat(meta.getFormat()) .parseObject(meta.getDefaultValue());
                }
                catch (ParseException e) {
                    Log.error(e);
                }
            }
        }
        properties.put(meta.getName(),oValue);
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
    
    /**
     * 
     * @return XML representation for item properties
     */
    private String getPropertiesXml() {
        LinkedHashMap properties = getProperties();
		Iterator it = properties.keySet().iterator();
     	StringBuffer sb = new StringBuffer(); //$NON-NLS-1$
		while (it.hasNext()) {
			String sKey = (String) it.next();
            String sValue = null;
            Object oValue = properties.get(sKey);
            if (oValue != null){
                PropertyMetaInformation meta = getMeta(sKey);
                if (meta.getType().equals(Date.class)){ //if date, transform to string with format
                    sValue = new SimpleDateFormat(meta.getFormat()).format(oValue); //PERF!
                }
                else if (meta.getType().equals(Class.class)){ //if Class, just take class name
                    sValue = ((Class)oValue).getName();
                }
                else{
                    sValue = oValue.toString(); //transform int, long, String.. to String    
                }
                sValue = Util.formatXML(sValue); //make sure to remove non-XML characters
            }
			sb.append(" "+sKey + "='" + sValue + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return sb.toString();
	}
	
    /**
     * Set all personnal properties of an XML file for an item (doesn't overwrite existing properties for perfs)
     * 
     * @param attributes :
     *                list of attributes for this XML item
     */
    public void populateProperties(Attributes attributes) {
        for (int i =0 ; i < attributes.getLength(); i++) {
            String sProperty = attributes.getQName(i);
            if (properties.containsKey(sProperty)){
                setProperty(sProperty, attributes.getValue(i));    
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
    
    /**
     * @param sProperty Property name
     * @return Meta information for current item and given property name
     */
    public PropertyMetaInformation getMeta(String sProperty){
        return ItemManager.getItemManager(this.getClass()).getMetaInformation(sProperty);
    }
    
    /**
     * Clone all properties from a given properties list but not overwrite constructor properties
     * @param propertiesSource
     */
    public void cloneProperties(IPropertyable propertiesSource){
    	Iterator it = propertiesSource.getProperties().keySet().iterator();
    	while (it.hasNext()){
    		String sProperty = (String)it.next();
            if (!getMeta(sProperty).isConstructor()){
    			this.properties.put(sProperty,propertiesSource.getValue(sProperty));
    		}
    	}
    }
}
