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
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;

/**
 * Generic property handler, mother class for all items
 * <p>Note that some properties can be omitted (not in properties object), 
 * in this case, we take default value given in meta infos, this can
 * decrease collection fiel size</p>
 * @author Bertrand Florat 
 * @created 17 oct. 2003
 */
abstract public class PropertyAdapter implements IPropertyable, Serializable,ITechnicalStrings {
    
    /** Item properties, singleton */
    private LinkedHashMap properties;
    /** ID. Ex:1,2,3... */
    protected final String sId;
    /** Name */
    protected final String sName;
    /** "Any" value : concatenation of all properties*/
    protected String sAny =""; //$NON-NLS-1$
    /** Flag for need to refresh any criteria */
    protected boolean bNeedRefresh = true;
    
    /**
     * Constructor
     * @param sId element ID
     * @param sName element name
     */
    PropertyAdapter(final String sId,final String sName){
        this.sId = sId;
        setProperty(XML_ID,sId);
        this.sName = sName;
        setProperty(XML_NAME,sName);
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
    
    /**
     * Equals method, two propertyable are equals if all properties are equals
     */
    public boolean equals(Object other){
        IPropertyable paOther = (IPropertyable)other;
        for (Object oKey : paOther.getProperties().keySet()){
            //if the property is known and equals
            if (!properties.containsKey(oKey) 
                    || properties.get(oKey) == null 
                    || !properties.get(oKey).equals(paOther.getValue((String)oKey))){
                return false;
            }
        }
        return true;
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
    public Object getValue(String sKey) {
        //look at properties to check the given property is known
        if ( !getProperties().containsKey(sKey)){ //no? take property default
            return getDefaultValue(sKey);
        }
        return getProperties().get(sKey); //return property value
    }
    
    
    public long getLongValue(String sKey){
        //look at properties to check the given property is known
        if ( !getProperties().containsKey(sKey)){ //no? take property default
            return (Long)getDefaultValue(sKey);
        }
        return (Long)getProperties().get(sKey); //return property value
    }
    
    
    public double getDoubleValue(String sKey){
        //look at properties to check the given property is known
        if ( !getProperties().containsKey(sKey)){ //no? take property default
            return (Double)getDefaultValue(sKey);
        }
        return (Double)getProperties().get(sKey); //return property value
    }
    
    /**
     * Return String value for String type values. We assume that given property is a String. If you are not sure, use Util.parse method
     */
    public String getStringValue(String sKey){
        //look at properties to check the given property is known
        if ( !getProperties().containsKey(sKey)){ //no? take property default
            return (String)getDefaultValue(sKey);
        }
        return (String)getProperties().get(sKey); //return property value
    }
    
    public boolean getBooleanValue(String sKey){
        //look at properties to check the given property is known
        if ( !getProperties().containsKey(sKey)){ //no? take property default
            return (Boolean)getDefaultValue(sKey);
        }
        return (Boolean)getProperties().get(sKey); //return property value
    }
    
    public Date getDateValue(String sKey){
        //look at properties to check the given property is known
        if ( !getProperties().containsKey(sKey)){ //no? take property default
            return (Date)getDefaultValue(sKey);
        }
        return (Date)getProperties().get(sKey); //return property value}
    }
    
    public Object getDefaultValue(String sKey){
        PropertyMetaInformation meta = getMeta(sKey);
        return meta.getDefaultValue();
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
        if (bNeedRefresh){
            StringBuffer sb = new StringBuffer(100); //$NON-NLS-1$
            LinkedHashMap properties = getProperties();
            Iterator it = properties.keySet().iterator();
            while (it.hasNext()) {
                String sKey = (String) it.next();
                String sValue = getHumanValue(sKey);
                if (sValue != null){
                    PropertyMetaInformation meta = getMeta(sKey);
                    if (!meta.isVisible()){ //computes "any" only on visible items
                        continue;
                    }
                    sb.append(sValue);
                }
                
            }
            this.sAny = sb.toString();
        }
        return sAny;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#setDefaultProperty(java.lang.String, java.lang.String)
     */
    public void populateDefaultProperty(PropertyMetaInformation meta) {
        properties.put(meta.getName(),meta.getDefaultValue());
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
            return ""; //$NON-NLS-1$
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
                try {
                    sValue = Util.format(oValue,meta);
                } catch (Exception e) { //should not occur
                    Log.error(e);
                }
                sValue = Util.formatXML(sValue); //make sure to remove non-XML characters
            }
            sb.append(" "+Util.formatXML(sKey) + "='" + sValue + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
            if (!properties.containsKey(sProperty)){
                String sValue = attributes.getValue(i);
                PropertyMetaInformation meta = getMeta(sProperty);
                try {
                    setProperty(sProperty, Util.parse(sValue,meta.getType(),meta.getFormat()));
                } catch (Exception e) {
                    Log.error("137",sProperty,e); //$NON-NLS-1$
                }    
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
     * Default implementation for this method, simply return standard value
     */
    public String getHumanValue(String sKey) {
        try{
            return Util.format(getValue(sKey),getMeta(sKey));
        }
        catch(Exception e){
            Log.error(e);
            return ""; //$NON-NLS-1$
        }
    }
    
    /**
     * Get formated property if a format is given
     * Implements for now:
     * - date
     * TODO: number(n), florat(n,m)
     * @return formated result
     */
    public String getFormatedValue(String sKey){
        String sOut = null;
        Object o = getValue(sKey);
        PropertyMetaInformation meta = getMeta(sKey);
        if (meta.getFormat() != null){
            if (meta.getType().equals(java.util.Date.class)){
                try{
                    sOut = meta.getFormat().format((Date)o);
                }
                catch(Exception e){ //parsing error
                    Log.error("137",e); //$NON-NLS-1$
                }
            }
        }
        else{ //no format defined
            if (meta.getType().equals(java.util.Date.class)){//if date and no format defined, use default format for locale
                DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT,Locale.getDefault());
                sOut = dateFormatter.format((Date)o);
            }
        }
        return sOut;
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
