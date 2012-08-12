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
package org.jajuk.base;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * A Jajuk property meta information.
 */
public class PropertyMetaInformation {
  /** Property name. */
  private final String sName;
  /** Is property a custom property?. */
  private boolean bCustom = false;
  /** Is property element of associated item constructor? (and so used in the checksum ID hash). */
  private boolean bConstructor = false;
  /** Property Type (java.lang.String for ie) */
  private final Class<?> cType;
  /** Default value (null: no default). */
  private Object oDefaultValue;
  /** This property should be displayed to UI?. */
  private boolean bShouldBeDisplayed = true;
  /** Editable?. */
  private boolean bEditable = true;
  /** Unique?. */
  private boolean bMergeable = false;
  /** Human Type. */
  private String sHumanType;

  /**
  * constructor.
  * 
  * @param sName Property name
  * @param bCustom Is custom property
  * @param bConstructor Is constructor property
  * @param bShouldBeDisplayed Does this standard property must be displayed (exp for ie is not)
  * @param bEditable Is this property editable
  * @param bMergeable Is this property mergeable if we display several items together
  * @param cType Property type
  * @param oDefaultValue Default value
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
          this.oDefaultValue = SessionService.getConfFileByPath("").toURI().toURL();
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
   * Checks if is constructor.
   * 
   * @return true, if is constructor
   */
  public boolean isConstructor() {
    return bConstructor;
  }

  /**
   * Checks if is custom.
   * 
   * @return true, if is custom
   */
  public boolean isCustom() {
    return bCustom;
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public Class<?> getType() {
    return cType;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return sName;
  }

  /**
   * <property name='toto' custom ='true' constructor='true' type='date'
   * format='YYYYMMDD'/>.
   * 
   * @return property meta information XML description
   */
  String toXML() {
    String sDefault = "";
    try {
      if (oDefaultValue != null) {
        sDefault = UtilString.format(oDefaultValue, this, false);
      }
    } catch (Exception e) { // should to occur at this point
      Log.error(e);
    }
    return '<' + Const.XML_PROPERTY + " " + Const.XML_NAME + "='" + UtilString.formatXML(sName)
        + "' " + Const.XML_CUSTOM + "='" + bCustom + "' " + Const.XML_CONSTRUCTOR + "='"
        + bConstructor + "' " + Const.XML_VISIBLE + "='" + bShouldBeDisplayed + "' "
        + Const.XML_EDITABLE + "='" + bEditable + "' " + Const.XML_UNIQUE + "='" + bMergeable
        + "' " + Const.XML_TYPE + "='" + cType.getName() + "' " + Const.XML_DEFAULT_VALUE + "='"
        + UtilString.formatXML(sDefault) + "'/>";
  }

  /**
   * Gets the default value.
   * 
   * @return the default value
   */
  public Object getDefaultValue() {
    return oDefaultValue;
  }

  /*
   * public void setDefaultValue(String defaultValue) { oDefaultValue =
   * defaultValue; }
   */
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Name=" + sName + " Custom=" + bCustom + " Constructor=" + bConstructor + " Type="
        + cType + " Default=" + oDefaultValue + " Editable=" + isEditable() + " Visible="
        + isVisible() + " Mergeable=" + isMergeable();
  }

  /**
   * Checks if is visible.
   * 
   * @return true, if is visible
   */
  public boolean isVisible() {
    return bShouldBeDisplayed;
  }

  /**
   * Checks if is editable.
   * 
   * @return true, if is editable
   */
  public boolean isEditable() {
    return bEditable;
  }

  /**
   * Checks if is mergeable.
   * 
   * @return true, if is mergeable
   */
  public boolean isMergeable() {
    return bMergeable;
  }

  /**
   * Gets the human type.
   * 
   * @return a human representation for a property type
   */
  public String getHumanType() {
    return sHumanType;
  }

  /**
   * Gets the human name.
   * 
   * @return the human name
   */
  public String getHumanName() {
    return Messages.getHumanPropertyName(getName());
  }
}
