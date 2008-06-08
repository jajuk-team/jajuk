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

package org.jajuk.ui.widgets;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;

/**
 * Encapsulates a label with a text and an icon, used for tables
 */
public class IconLabel extends ImageIcon implements Comparable<IconLabel> {

  private static final long serialVersionUID = 3794262035337113611L;

  /** Text */
  private String sText;

  /** Background color */
  private Color cBackground;

  /** Foreground color */
  private Color cForeground;

  /** Font */
  private Font font;

  /** Tooltip */
  private String sTooltip;

  /** Whether this is a integer */
  private boolean bInteger = false;

  /**
   * Constructor
   * 
   * @param icon
   * @param sText
   * @param cBackground
   * @param cForeground
   * @param font
   */
  public IconLabel(ImageIcon icon, String sText, Color cBackground, Color cForeground, Font font,
      String sTooltip) {
    super(icon.getImage());
    this.sText = sText;
    this.cBackground = cBackground;
    this.cForeground = cForeground;
    this.font = font;
    this.sTooltip = sTooltip;
  }

  public IconLabel(ImageIcon icon, String sText) {
    super(icon.getImage());
    this.sText = sText;
  }

  /**
   * @return Returns the sText.
   */
  public String getText() {
    return sText;
  }

  /**
   * @return Returns the cBackground.
   */
  public Color getBackground() {
    return cBackground;
  }

  /**
   * @return Returns the cForeground.
   */
  public Color getForeground() {
    return cForeground;
  }

  /**
   * @return Returns the font.
   */
  public Font getFont() {
    return font;
  }

  /**
   * toString method
   */
  public String toString() {
    return sText;
  }

  /**
   * @return Returns the sTooltip.
   */
  public String getTooltip() {
    return sTooltip;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(T)
   */
  public int compareTo(IconLabel ilOther) {
    if (ilOther.getTooltip() != null && this.getTooltip() != null) {
      if (bInteger) { // is this item represents an integer ?
        long l = Long.parseLong(getTooltip());
        long lOther = Long.parseLong(ilOther.getTooltip());
        return (int) (l - lOther);
      } else { // simply compare tooltip strings
        return ilOther.getTooltip().compareTo(getTooltip());
      }
    } else {
      return 0;
    }
  }

  /**
   * @param integer
   */
  public void setInteger(boolean integer) {
    bInteger = integer;
  }
}
