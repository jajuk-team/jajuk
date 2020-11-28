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
package org.jajuk.services.dj;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.ui.helpers.StarsHelper;
import org.jajuk.util.Const;

/**
 * Digital DJ.
 */
public abstract class DigitalDJ implements Comparable<DigitalDJ> {
  /** DJ unique ID. */
  private final String sID;
  /** DJ name. */
  protected String sName;
  /** Rating floor. */
  protected int iRatingLevel = 0;
  /** Fading duration in sec. */
  protected int iFadingDuration = 0;
  /** Track unicity. */
  protected boolean bUnicity = false;
  private int iMaxTracks = -1;

  /**
   * Constructor with ID.
   *
   * @param sID DJ ID
   */
  DigitalDJ(String sID) {
    this.sID = sID;
  }

  /**
   * toString method.
   *
   * @return String representation of this object
   */
  @Override
  public String toString() {
    return "DJ " + sName;
  }

  /**
   * Compare to method, sorted alphaticaly.
   *
   * @return the int
   */
  @Override
  public int compareTo(DigitalDJ other) {
    //
    if (other == null) {
      return -1;
    }
    return this.sName.compareTo(other.getName());
  }

  /**
   * To xml.
   *
   * @return XML representation of this DJ
   */
  public abstract String toXML();

  /**
   * To xml general parameters.
   *
   * @return DJ common parameters
   */
  protected String toXMLGeneralParameters() {
    return "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<" + Const.XML_DJ_DJ + " " + Const.XML_VERSION + "='" + Const.JAJUK_VERSION + "' "
            + Const.XML_ID + "='" + sID + "' " + Const.XML_NAME + "='" + sName + "' " + Const.XML_TYPE
            + "='" + this.getClass().getName() + "'>\n" +
            "\t<" + Const.XML_DJ_GENERAL + " " +
            Const.XML_DJ_RATING_LEVEL + "='" + iRatingLevel + "' " +
            Const.XML_DJ_UNICITY + "='" + bUnicity + "' " +
            Const.XML_DJ_FADE_DURATION + "='" + iFadingDuration + "' " +
            Const.XML_DJ_MAX_TRACKS + "='" + iMaxTracks + "'/>\n";
  }

  /**
   * Filter by rate and remove duplicates (unicity).
   */
  void filterFilesByRate(List<File> files) {
    // this set stores already used tracks
    Set<Track> selectedTracks = new HashSet<>(files.size());
    // Select by rate if needed
    if (iRatingLevel > 0) {
      Iterator<File> it = files.iterator();
      while (it.hasNext()) {
        File file = it.next();
        if (StarsHelper.getStarsNumber(file.getTrack()) < iRatingLevel
            || selectedTracks.contains(file.getTrack())) {
          it.remove();
        } else {
          selectedTracks.add(file.getTrack());
        }
      }
    }
  }

  /**
   * Filter files by max track.
   */
  void filterFilesByMaxTrack(List<File> files) {
    // cut off some tracks if less are selected for queuing
    if (iMaxTracks > 0) {
      // return without any copying if we have less entries than max
      if (iMaxTracks > files.size()) {
        return;
      }
      // remove until we have less than max tracks
      while (files.size() > iMaxTracks) {
        files.remove(files.size() - 1);
      }
    }
  }

  /**
   * Gets the name.
   *
   * @return DJ name
   */
  public String getName() {
    return sName;
  }

  /**
   * equals method.
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

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    // equals only compares on Name, therefore use the same for the hashcode
    return sName.hashCode();
  }

  /**
   * Sets the name.
   */
  public void setName(String name) {
    this.sName = name;
  }

  /**
   * Gets the fading duration.
   *
   * @return DJ fade duration
   */
  public int getFadingDuration() {
    return this.iFadingDuration;
  }

  /**
   * Sets the fading duration.
   */
  public void setFadingDuration(int fadingDuration) {
    this.iFadingDuration = fadingDuration;
  }

  /**
   * Gets the rating level.
   *
   * @return Returns the iRatingFloor.
   */
  public int getRatingLevel() {
    return this.iRatingLevel;
  }

  /**
   * Sets the rating level.
   *
   * @param ratingFloor The iRatingFloor to set.
   */
  public void setRatingLevel(int ratingFloor) {
    this.iRatingLevel = ratingFloor;
  }

  /**
   * Generate playlist.
   *
   * @return Generated playlist
   */
  public abstract List<File> generatePlaylist();

  /**
   * Gets the iD.
   *
   * @return the iD
   */
  public String getID() {
    return this.sID;
  }

  /**
   * Checks if is track unicity.
   *
   * @return true, if is track unicity
   */
  public boolean isTrackUnicity() {
    return this.bUnicity;
  }

  /**
   * Sets the track unicity.
   *
   * @param trackUnicity the new track unicity
   */
  public void setTrackUnicity(boolean trackUnicity) {
    this.bUnicity = trackUnicity;
  }

  /**
   * Gets the max tracks.
   *
   * @return The configured number of max tracks to queue for this DJ. -1
   * denotes infinity.
   */
  public int getMaxTracks() {
    return this.iMaxTracks;
  }

  /**
   * Set the new max number of tracks to queue.
   *
   * @param iMaxTracks The new max number of tracks to queue for this DJ. -1 for infinity
   */
  public void setMaxTracks(int iMaxTracks) {
    this.iMaxTracks = iMaxTracks;
  }
}
