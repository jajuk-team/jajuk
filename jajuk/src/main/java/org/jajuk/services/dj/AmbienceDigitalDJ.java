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

package org.jajuk.services.dj;

import java.util.ArrayList;
import java.util.List;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;

/**
 * Ambience DJ.
 */
public class AmbienceDigitalDJ extends DigitalDJ {

  /** Used ambience. */
  private Ambience ambience;

  /**
   * The Constructor.
   * 
   * @param sID 
   */
  public AmbienceDigitalDJ(String sID) {
    super(sID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.DigitalDJ#generatePlaylist()
   */
  @Override
  public List<File> generatePlaylist() {
    if (ambience == null) { // can be null if ambience has been removed
      Messages.showErrorMessage(159);
      return new ArrayList<File>();
    }
    List<File> out = getSequence();
    if (!bUnicity && out.size() > 0) {
      while (out.size() < Const.MIN_TRACKS_NUMBER_WITHOUT_UNICITY) {
        out.addAll(getSequence());
      }
    }
    return out;
  }

  /**
   * Gets the sequence.
   * 
   * @return a single loop sequence
   */
  private List<File> getSequence() {
    List<File> out = new ArrayList<File>(100);
    // Get a shuffle selection
    List<File> files = FileManager.getInstance().getGlobalShufflePlaylist();
    // Keep only right genres and check for unicity
    for (File file : files) {
      if (ambience.getGenres().contains(file.getTrack().getGenre())) {
        out.add(file);
      }
    }
    // Select by rate if needed
    filterFilesByRate(out);

    // finally ensure that we don't select more than the max number of tracks
    filterFilesByMaxTrack(out);

    return out;
  }

  /**
   * Gets the ambience.
   * 
   * @return Ambience
   */
  public Ambience getAmbience() {
    return this.ambience;
  }

  /**
   * (non-Javadoc).
   * 
   * @return the string
   * 
   * @see dj.DigitalDJ#toXML()
   */
  @Override
  public String toXML() {
    StringBuilder sb = new StringBuilder(2000);
    sb.append(toXMLGeneralParameters());
    sb.append("\t<" + Const.XML_DJ_AMBIENCE + " " + Const.XML_DJ_VALUE + "='");
    sb.append((ambience == null ? "" : ambience.getID()) + "'/>\n");
    sb.append("</" + Const.XML_DJ_DJ + ">\n");
    return sb.toString();
  }

  /**
   * Sets the ambience.
   * 
   * @param ambience the new ambience
   */
  public void setAmbience(Ambience ambience) {
    this.ambience = ambience;
  }

}