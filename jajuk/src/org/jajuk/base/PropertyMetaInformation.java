/*
 *  Jajuk
 *  Copyright (C) 2004 bflorat
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

import org.jajuk.util.ITechnicalStrings;


/**
 *  A Jajuk property meta information
 * @author     Bertrand Florat
 * @created    14 août 2005
 */
public class PropertyMetaInformation implements ITechnicalStrings{
    
    /**Property name*/
    private String sName;
    /**Is property a custom property?*/
    private boolean bCustom = false;
    /**Is property element of associated item constructor? (and so used in the checksum ID hash)*/
    private boolean bConstructor = false;
    /**Property Type (java.lang.String for ie)*/
    private Class cType;
    /**Format like YYYYmmDD for a date for ie or number of digits for an Integer*/
    String sFormat;
    /**Default value (null: no default)*/
    String sDefaultValue;
    /**This property should be displayed to UI?*/
    boolean bShouldBeDisplayed = true;
    /**Editable?*/
    boolean bEditable = true;
    
    /**
     * @param sName
     * @param bCustom
     * @param bConstructor
     * @param cType
     * @param sFormat
     * @param oDefaultValue
     */
    public PropertyMetaInformation(String sName,boolean bCustom,boolean bConstructor,
            boolean bShouldBeDisplayed,boolean bEditable,Class cType,
            String sFormat,String sDefaultValue){
        this.sName = sName;
        this.bCustom = bCustom;
        this.bConstructor = bConstructor;
        this.bShouldBeDisplayed = bShouldBeDisplayed;
        this.bEditable = bEditable;
        this.cType = cType;
        this.sFormat = sFormat;
        this.sDefaultValue = sDefaultValue;
    }
    
    /**
     * @param sName
     * @param bCustom
     * @param bConstructor
     * @param cType
     * @param oDefaultValue
     */
    public PropertyMetaInformation(String sName,boolean bCustom,boolean bConstructor,
            boolean bShouldBeDisplayed,boolean bEditable,Class cType,String sDefaultValue){
        this(sName,bCustom,bConstructor,bShouldBeDisplayed,bEditable,cType,null,sDefaultValue);
    }

    /**
     * @param sName
     * @param bCustom
     * @param bConstructor
     * @param cType
     */
    public PropertyMetaInformation(String sName,boolean bCustom,boolean bConstructor,
           boolean bShouldBeDisplayed,boolean bEditable,Class cType){
        this(sName,bCustom,bConstructor,bShouldBeDisplayed,bEditable,cType,null);
    }

    /**
     * @return
     */
    public String getFormat() {
        return sFormat;
    }

    /**
     * @param format
     */
    public void setFormat(String format) {
        sFormat = format;
    }

    /**
     * @return
     */
    public boolean isConstructor() {
        return bConstructor;
    }

    /**
     * @return
     */
    public boolean isCustom() {
        return bCustom;
    }

    /**
     * @return
     */
    public Class getType() {
        return cType;
    }

    /**
     * @return
     */
    public String getName() {
        return sName;
    }
    
    /**
     * <property name='toto' custom ='true' constructor='true'  type='date' format='YYYYMMDD'/>
     * @return property meta information XML description
     */
    public String toXML(){
        return "\t\t<"+XML_PROPERTY+" "+XML_NAME+"='"+sName+"' "+
            XML_CUSTOM+"='"+ bCustom+"' "+
            XML_CONSTRUCTOR+"='"+bConstructor+"' "+
            XML_DISPLAY+"='"+bShouldBeDisplayed+"' "+
            XML_TYPE+"='"+cType.getName()+"' "+
            XML_FORMAT+"='"+sFormat+"' "+
            XML_DEFAULT_VALUE+"='"+sDefaultValue+"'/>";
      
    }

    public String getDefaultValue() {
        return sDefaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        sDefaultValue = defaultValue;
    }
    
    public String toString(){
        return "Name="+sName+" Custom="+bCustom+" Constructor="+bConstructor+" Type="+cType+" Default="+sDefaultValue+" Format="+sFormat;
    }

    public boolean isVisible() {
        return bShouldBeDisplayed;
    }

    public boolean isEditable() {
        return bEditable;
    }
    
}
