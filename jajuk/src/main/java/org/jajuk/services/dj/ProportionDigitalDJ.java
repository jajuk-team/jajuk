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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.util.Const;
import org.jajuk.util.UtilSystem;

/**
 * A proportion (10% JAZZ, 20% ROCK...) digital DJ
 */
public class ProportionDigitalDJ extends DigitalDJ {
  /** Set of proportions. */
  private List<Proportion> proportions;

  /**
   * The Constructor.
   * 
   * @param sID 
   */
  public ProportionDigitalDJ(String sID) {
    super(sID);
    this.proportions = new ArrayList<Proportion>(10);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.DigitalDJ#generatePlaylist()
   */
  @Override
  public List<File> generatePlaylist() {
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
    Map<Proportion, List<File>> list = new HashMap<Proportion, List<File>>(10);
    // get a global shuffle selection, we will keep only tracks with wanted
    // genres
    List<File> global = FileManager.getInstance().getGlobalShufflePlaylist();
    // Select by rate if needed
    filterFilesByRate(global);
    for (File file : global) {
      for (Proportion prop : proportions) {
        if (prop.getGenres().contains(file.getTrack().getGenre())) {
          List<File> files = list.get(prop);
          if (files == null) { // not yet file list
            files = new ArrayList<File>(100);
            list.put(prop, files);
          }
          files.add(file);
        }
      }
    }
    // check if all properties are represented
    if (list.size() < proportions.size()) {
      return out; // return void list
    }
    // now, keep the smallest list before applying proportion
    Proportion minProp = null;
    int iMinSize = 0;
    float fTotal = 0;
    for (Entry<Proportion, List<File>> prop : list.entrySet()) {
      fTotal += prop.getKey().getProportion();
      List<File> files = prop.getValue();
      // keep proportion with smallest number of files
      if (minProp == null || files.size() < iMinSize) {
        minProp = prop.getKey();
        iMinSize = files.size();
      }
    }
    // apply proportions
    for (Entry<Proportion, List<File>> prop : list.entrySet()) {
      List<File> files = prop.getValue();
      out.addAll(files.subList(0, (int) (iMinSize * prop.getKey().getProportion())));
    }
    // complete this shuffle files if total sum < 100%
    if (fTotal < 1.0) {
      int iNbAdditional = (int) ((1.0 - fTotal) * iMinSize);
      for (int i = 0; i < iNbAdditional; i++) {
        out.add(global.get((int) (Math.random() * global.size())));
      }
    }
    // shuffle selection
    Collections.shuffle(out, UtilSystem.getRandom());
    // finally ensure that we don't select more than the max number of tracks
    filterFilesByMaxTrack(out);
    return out;
  }

  /**
   * Gets the proportions.
   * 
   * @return Proportions
   */
  public List<Proportion> getProportions() {
    return this.proportions;
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
    sb.append("\t<" + Const.XML_DJ_PROPORTIONS + ">\n");
    for (Proportion proportion : proportions) {
      String genresDesc = "";
      for (Genre genre : proportion.getGenres()) {
        genresDesc += genre.getID() + ',';
      }
      // remove trailing coma
      genresDesc = genresDesc.substring(0, genresDesc.length() - 1);
      sb.append("\t\t<" + Const.XML_DJ_PROPORTION + " " + Const.XML_DJ_GENRES + "='" + genresDesc
          + "' " + Const.XML_DJ_VALUE + "='" + proportion.getProportion() + "'/>\n");
    }
    sb.append("\t</" + Const.XML_DJ_PROPORTIONS + ">\n");
    sb.append("</" + Const.XML_DJ_DJ + ">\n");
    return sb.toString();
  }

  /**
   * Sets the proportions.
   * 
   * @param proportions the new proportions
   */
  public void setProportions(List<Proportion> proportions) {
    this.proportions = proportions;
  }
}
