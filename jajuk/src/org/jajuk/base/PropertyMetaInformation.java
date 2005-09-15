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

import java.text.SimpleDateFormat;
import java.util.Date;

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
    Object oDefaultValue;
    /**This property should be displayed to UI?*/
    boolean bShouldBeDisplayed = true;
    /**Editable?*/
    boolean bEditable = true;
    /**Unique?*/
    boolean bUnique = false;
    
    /**
     * constructor
     * @param sName Property name
     * @param bCustom Is custom proprety
     * @param bConstructor Is constructor property
     * @param bShouldBeDisplayed Does this standard property must be displayed (exp for ie is not)
     * @param bEditable Is this property editable 
     * @param bUnique Is this property value unique
     * @param cType Property type
     * @param sFormat Property format
     * @param sDefaultValue Default value
     */
    public PropertyMetaInformation(String sName,boolean bCustom,boolean bConstructor,
            boolean bShouldBeDisplayed,boolean bEditable,boolean bUnique,Class cType,
            String sFormat,String sDefaultValue){
        this.sName = sName;
        this.bCustom = bCustom;
        this.bConstructor = bConstructor;
        this.bShouldBeDisplayed = bShouldBeDisplayed;
        this.bEditable = bEditable;
        this.bUnique = bUnique;
        this.cType = cType;
        this.sFormat = sFormat;
        //parse default value
        if (cType.equals(Boolean.class)){
            if (sDefaultValue == null){
                sDefaultValue = "false"; //if no default is given, false for booleans
            }
            this.oDefaultValue = Boolean.parseBoolean(sDefaultValue);    
        }
        else if (cType.equals(String.class)){
            if (sDefaultValue == null){
                sDefaultValue = ""; //if no default is given, ""
            }
            this.oDefaultValue = sDefaultValue;
        }
        else if (cType.equals(Long.class)){
            if (sDefaultValue == null){
                sDefaultValue = "0"; //if no default is given, 0
            }
            this.oDefaultValue = Long.parseLong(sDefaultValue);
        }
        else if (cType.equals(Double.class)){
            if (sDefaultValue == null){
                sDefaultValue = "0"; //if no default is given, 0
            }
            this.oDefaultValue = Double.parseDouble(sDefaultValue);
        }
        else if (cType.equals(Date.class) ){
            try {
                this.oDefaultValue = new SimpleDateFormat(sFormat).parse(sDefaultValue);
            } catch (Exception e) {
                //no log: if no format or default date, default value stays null
            }
        }
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
            XML_VISIBLE+"='"+bShouldBeDisplayed+"' "+
            XML_EDITABLE+"='"+bEditable+"' "+
            XML_UNIQUE+"='"+bUnique+"' "+
            XML_TYPE+"='"+cType.getName()+"' "+
            XML_FORMAT+"='"+sFormat+"' "+
            XML_DEFAULT_VALUE+"='"+(oDefaultValue == null ? "null":oDefaultValue)+"'/>";
      
    }

    public Object getDefaultValue() {
        return oDefaultValue;
    }

    /*public void setDefaultValue(String defaultValue) {
        oDefaultValue = defaultValue;
    }*/
    
    public String toString(){
        return "Name="+sName+" Custom="+bCustom+" Constructor="+bConstructor
        +" Type="+cType+" Default="+oDefaultValue+" Format="
        +sFormat+" Editable="+isEditable()+" Visible="+isVisible() 
        +" Unique="+isUnique();
    }

    public boolean isVisible() {
        return bShouldBeDisplayed;
    }

    public boolean isEditable() {
        return bEditable;
    }
    
     public boolean isUnique() {
        return bUnique;
    }
    
}
