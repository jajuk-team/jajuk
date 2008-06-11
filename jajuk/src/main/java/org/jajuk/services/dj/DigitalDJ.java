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
 *  $Revision:3266 $ 
 */

package org.jajuk.services.dj;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.util.ITechnicalStrings;

/**
 * Digital DJ
 */
public abstract class DigitalDJ implements ITechnicalStrings, Comparable<DigitalDJ> {

  /** DJ unique ID */
  private String sID;

  /** DJ name */
  protected String sName;

  /** Rating floor */
  protected int iRatingLevel = 0;

  /** Fading duration in sec */
  protected int iFadingDuration = 0;

  /** Track unicity */
  protected boolean bUnicity = false;

  /**
   * Constructor with ID
   * 
   * @param sName
   *          DJ name
   * @param sID
   *          DJ ID
   */
  DigitalDJ(String sID) {
    this.sID = sID;
  }

  /**
   * toString method
   * 
   * @return String representation of this object
   */
  @Override
  public String toString() {
    return "DJ " + sName;
  }

  /**
   * Compare to method, sorted alphaticaly
   * 
   * @param o
   * @return
   */
  public int compareTo(DigitalDJ other) {
    return this.sName.compareTo(other.getName());
  }

  /**
   * @return XML representation of this DJ
   */
  public abstract String toXML();

  /**
   * 
   * @return DJ common parameters
   */
  protected String toXMLGeneralParameters() {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
    sb.append("<" + XML_DJ_DJ + " " + XML_VERSION + "='" + JAJUK_VERSION + "' " + XML_ID + "='"
        + sID + "' " + XML_NAME + "='" + sName + "' " + XML_TYPE + "='" + this.getClass().getName()
        + "'>\n");
    sb.append("\t<" + XML_DJ_GENERAL + " ");
    sb.append(XML_DJ_RATING_LEVEL + "='" + iRatingLevel + "' ");
    sb.append(XML_DJ_UNICITY + "='" + bUnicity + "' ");
    sb.append(XML_DJ_FADE_DURATION + "='" + iFadingDuration + "'/>\n");
    return sb.toString();
  }

  /**
   * Filter by rate and remove duplicates (unicity)
   * 
   * @param files
   */
  void filterFilesByRate(List<File> files) {
    // this set stores already used tracks
    Set<Track> selectedTracks = new HashSet<Track>(files.size());
    // Select by rate if needed
    if (iRatingLevel > 0) {
      Iterator<File> it = files.iterator();
      while (it.hasNext()) {
        File file = it.next();
        if (file.getTrack().getStarsNumber() < iRatingLevel
            || selectedTracks.contains(file.getTrack())) {
          it.remove();
        } else {
          selectedTracks.add(file.getTrack());
        }
      }
    }
  }

  /**
   * 
   * @return DJ name
   */
  public String getName() {
    return sName;
  }

  /**
   * equals method
   * 
   * @return whether two object are equals
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DigitalDJ)) {
      return false;
    }
    String sOtherName = ((DigitalDJ) other).getName();
    return getName().equals(sOtherName);
  }

  @Override
  public int hashCode() {
    // equals only compares on Name, therefore use the same for the hashcode
    return sName.hashCode();
  }

  /**
   * @param name
   */
  public void setName(String name) {
    this.sName = name;
  }

  /**
   * @return DJ fade duration
   */
  public int getFadingDuration() {
    return this.iFadingDuration;
  }

  /**
   * @param fadingDuration
   */
  public void setFadingDuration(int fadingDuration) {
    this.iFadingDuration = fadingDuration;
  }

  /**
   * @return Returns the iRatingFloor.
   */
  public int getRatingLevel() {
    return this.iRatingLevel;
  }

  /**
   * @param ratingFloor
   *          The iRatingFloor to set.
   */
  public void setRatingLevel(int ratingFloor) {
    this.iRatingLevel = ratingFloor;
  }

  /**
   * 
   * @return Generated playlist
   */
  public abstract List<File> generatePlaylist();

  public String getID() {
    return this.sID;
  }

  public boolean isTrackUnicity() {
    return this.bUnicity;
  }

  public void setTrackUnicity(boolean trackUnicity) {
    this.bUnicity = trackUnicity;
  }

}
