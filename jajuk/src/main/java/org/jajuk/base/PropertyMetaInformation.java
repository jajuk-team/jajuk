/*
 *  Jajuk
 *  Copyright (C) 2004 The Jajuk Team
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * A Jajuk property meta information
 */
public class PropertyMetaInformation implements Const {

  /** Property name */
  private String sName;

  /** Is property a custom property? */
  private boolean bCustom = false;

  /**
   * Is property element of associated item constructor? (and so used in the
   * checksum ID hash)
   */
  private boolean bConstructor = false;

  /** Property Type (java.lang.String for ie) */
  private Class<?> cType;

  /** Default value (null: no default) */
  Object oDefaultValue;

  /** This property should be displayed to UI? */
  boolean bShouldBeDisplayed = true;

  /** Editable? */
  boolean bEditable = true;

  /** Unique? */
  boolean bMergeable = false;

  /** Human Type */
  private String sHumanType;

  /** Today */
  public static final Date TODAY = new Date();

  /**
   * constructor
   * 
   * @param sName
   *          Property name
   * @param bCustom
   *          Is custom property
   * @param bConstructor
   *          Is constructor property
   * @param bShouldBeDisplayed
   *          Does this standard property must be displayed (exp for ie is not)
   * @param bEditable
   *          Is this property editable
   * @param bMergeable
   *          Is this property mergeable if we display several items together
   * @param cType
   *          Property type
   * @param oDefaultValue
   *          Default value
   */
  public PropertyMetaInformation(String sName, boolean bCustom, boolean bConstructor,
      boolean bShouldBeDisplayed, boolean bEditable, boolean bMergeable, Class<?> cType,
      Object oDefaultValue) {
    this.sName = sName;
    this.bCustom = bCustom;
    this.bConstructor = bConstructor;
    this.bShouldBeDisplayed = bShouldBeDisplayed;
    this.bEditable = bEditable;
    this.bMergeable = bMergeable;
    this.cType = cType;
    this.oDefaultValue = oDefaultValue;
    if (cType.equals(Boolean.class)) {
      if (oDefaultValue == null) {
        this.oDefaultValue = Boolean.FALSE; // if no default is
        // given, false for
        // booleans
      }
      this.sHumanType = Messages.getString("Property_Format_Boolean");
    } else if (cType.equals(String.class)) {
      if (oDefaultValue == null) {
        this.oDefaultValue = ""; // if no default is given, ""
      }
      this.sHumanType = Messages.getString("Property_Format_String");
    } else if (cType.equals(Long.class)) {
      if (oDefaultValue == null) {
        this.oDefaultValue = 0l; // if no default is given, 0
      }
      this.sHumanType = Messages.getString("Property_Format_Number");
    } else if (cType.equals(Double.class)) {
      if (oDefaultValue == null) {
        this.oDefaultValue = 0.0d; // if no default is given, 0.0
      }
      this.sHumanType = Messages.getString("Property_Format_Float");
    } else if (cType.equals(Date.class)) {
      // date default
      if (oDefaultValue == null) {
        this.oDefaultValue = UtilSystem.TODAY;
      } else {
        this.oDefaultValue = oDefaultValue;
      }
      this.sHumanType = Messages.getString("Property_Format_Date");
    } else if (cType.equals(URL.class)) {
      // URL default
      if (oDefaultValue == null) {
        try {
          this.oDefaultValue = UtilSystem.getConfFileByPath("").toURI().toURL();
        } catch (MalformedURLException e) {
          Log.error(e);
        }
      } else {
        this.oDefaultValue = oDefaultValue;
      }
      this.sHumanType = Messages.getString("Property_Format_URL");
    } else if (cType.equals(Class.class)) {
      this.oDefaultValue = Object.class;
    } else { // class not supported
      Log.debug("Class not supported !!!");
    }
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
  public Class<?> getType() {
    return cType;
  }

  /**
   * @return
   */
  public String getName() {
    return sName;
  }

  /**
   * <property name='toto' custom ='true' constructor='true' type='date'
   * format='YYYYMMDD'/>
   * 
   * @return property meta information XML description
   */
  public String toXML() {
    String sDefault = "";
    try {
      if (oDefaultValue != null) {
        sDefault = UtilString.format(oDefaultValue, this, false);
      }
    } catch (Exception e) { // should to occur at this point
      Log.error(e);
    }
    return '<' + XML_PROPERTY + " " + XML_NAME + "='" + UtilString.formatXML(sName) + "' "
        + XML_CUSTOM + "='" + bCustom + "' " + XML_CONSTRUCTOR + "='" + bConstructor + "' "
        + XML_VISIBLE + "='" + bShouldBeDisplayed + "' " + XML_EDITABLE + "='" + bEditable + "' "
        + XML_UNIQUE + "='" + bMergeable + "' " + XML_TYPE + "='" + cType.getName() + "' "
        + XML_DEFAULT_VALUE + "='" + UtilString.formatXML(sDefault) + "'/>";
  }

  public Object getDefaultValue() {
    return oDefaultValue;
  }

  /*
   * public void setDefaultValue(String defaultValue) { oDefaultValue =
   * defaultValue; }
   */

  @Override
  public String toString() {
    return "Name=" + sName + " Custom=" + bCustom + " Constructor=" + bConstructor + " Type="
        + cType + " Default=" + oDefaultValue + " Editable=" + isEditable() + " Visible="
        + isVisible() + " Mergeable=" + isMergeable();
  }

  public boolean isVisible() {
    return bShouldBeDisplayed;
  }

  public boolean isEditable() {
    return bEditable;
  }

  public boolean isMergeable() {
    return bMergeable;
  }

  /**
   * 
   * @return a human representation for a property type
   */
  public String getHumanType() {
    return sHumanType;
  }

  public String getHumanName() {
    return Messages.contains("Property_" + getName()) ? Messages.getString("Property_" + getName())
        : getName();
  }

}
