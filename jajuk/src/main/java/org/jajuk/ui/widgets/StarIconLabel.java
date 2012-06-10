/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.ui.widgets;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;

/**
 * Icon Label supporting rating stars.
 */
public class StarIconLabel extends IconLabel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Number of stars. */
  private int starsNumber = -1;

  /** Rate *. */
  private int rate = 0;

  /**
   * Gets the rate.
   * 
   * @return the rate
   */
  public long getRate() {
    return this.rate;
  }

  /**
   * Gets the stars number.
   * 
   * @return the starsNumber
   */
  public int getStarsNumber() {
    return this.starsNumber;
  }

  /**
   * The Constructor.
   * 
   * @param icon 
   * @param sText 
   * @param cBackground 
   * @param cForeground 
   * @param font 
   * @param rate 
   * @param starNumber 
   */
  public StarIconLabel(ImageIcon icon, String sText, Color cBackground, Color cForeground,
      Font font, int rate, int starNumber) {
    super(icon, sText, cBackground, cForeground, font, Long.toString(rate));
    this.rate = rate;
    this.starsNumber = starNumber;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(T)
   */
  @Override
  public int compareTo(IconLabel ilOther) {
    StarIconLabel silOther = (StarIconLabel) ilOther;
    // star Number == -1 means banned track, we want banned track to be the lowest
    // level in sorting but still sorted according rating between banned tracks
    if (starsNumber != -1 && silOther.getStarsNumber() != -1) {
      return (int) (rate - silOther.getRate());
    } else {
      // current track banned, the other not
      if (starsNumber == -1 && silOther.getStarsNumber() != -1) {
        return -1;
        // Other track banned, current not
      } else if (starsNumber != -1 && silOther.getStarsNumber() == -1) {
        return 1;
      }
      // Both are banned tracks
      else {
        return (int) (rate - silOther.getRate());
      }
    }
  }

}
