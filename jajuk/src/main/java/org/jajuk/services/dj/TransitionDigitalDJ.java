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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Style;
import org.jajuk.util.Const;
import org.jajuk.util.filters.JajukPredicates;

/**
 * Type description
 */
public class TransitionDigitalDJ extends DigitalDJ {

  /** List of transitions, need to be a list, not a set for offset */
  private List<Transition> transitions;

  /**
   * @param sID
   */
  public TransitionDigitalDJ(String sID) {
    super(sID);
    this.transitions = new ArrayList<Transition>(10);
  }

  /**
   * @return DJ transitions
   */
  public List<Transition> getTransitions() {
    return this.transitions;
  }

  /**
   * Delete a transition at given offset
   * 
   * @param offset
   */
  public void deleteTransition(int offset) {
    this.transitions.remove(offset);
  }

  /**
   * Add a transition
   * 
   * @param transition
   * @param offset
   */
  public void addTransition(Transition transition, int offset) {
    this.transitions.add(offset, transition);
  }

  /**
   * 
   * @param style
   * @return transition mapping this FROM ambience or null if none maps it
   */
  public Transition getTransition(Ambience ambience) {
    for (Transition transition : transitions) {
      if (transition.getFrom().equals(ambience)) {
        return transition;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.DigitalDJ#generatePlaylist()
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<File> generatePlaylist() {
    List<File> out = new ArrayList<File>(500);
    // get a global shuffle selection
    List<File> global = FileManager.getInstance().getGlobalShufflePlaylist();
    // Select by rate if needed
    filterFilesByRate(global);
    // None element, leave
    if (global.size() == 0) {
      return out;
    }
    // Build a ambience -> files map
    Map<Ambience, List<File>> hmAmbienceFiles = getAmbienceFilesList(global);
    // compute number of items to add
    int count = global.size();
    if (!bUnicity && count < Const.MIN_TRACKS_NUMBER_WITHOUT_UNICITY) {
      // under a limit, if collection is too small and no unicity, use
      // several times the same files
      count = Const.MIN_TRACKS_NUMBER_WITHOUT_UNICITY;
    }
    int comp = 1;
    // Start transition applying
    Ambience currentAmbience = transitions.get(0).getFrom();
    while (comp < count) {
      Transition currentTransition = getTransition(currentAmbience);
      int nb = currentTransition.getNbTracks();
      List<File> files = hmAmbienceFiles.get(currentAmbience);
      if (files != null && files.size() >= nb) {
        for (int i = 0; i < nb; i++) {
          // Get a shuffle file from the list
          Collections.shuffle(files);
          File file = files.get(0);
          out.add(file);
          comp++;
          // unicity in selection, remove it from this ambience
          if (bUnicity) {
            files.remove(file);
          }
        }
      } else { // no more tracks for this ambience ? leave
        return out;
      }
      // get next ambience
      currentAmbience = currentTransition.getTo();
    }
    return out;
  }

  /**
   * Returns a map ambience -> set of files
   * 
   * @param global
   *          initial set of files to consider
   * @return a map ambience -> set of files
   */
  @SuppressWarnings("unchecked")
  private Map<Ambience, List<File>> getAmbienceFilesList(List<File> global) {
    // Create a map ambience -> set of files
    Map<Ambience, List<File>> hmAmbienceFiles = new HashMap<Ambience, List<File>>(5);
    // For performance, we find unique ambiences in from and to transitions
    Set<Ambience> ambiences = new HashSet<Ambience>(5);
    for (Transition tr : transitions) {
      ambiences.add(tr.getFrom());
      ambiences.add(tr.getTo());
    }
    // Fill null key
    hmAmbienceFiles.put(null, (List<File>) ((ArrayList<File>) global).clone());
    // Fill all ambiences
    for (Ambience ambience : ambiences) {
      List<File> all = (List<File>) ((ArrayList<File>) global).clone();
      CollectionUtils.filter(all, new JajukPredicates.AmbiencePredicate(ambience));
      hmAmbienceFiles.put(ambience, all);
    }
    return hmAmbienceFiles;
  }

  /**
   * @return ambience associated with a style known in transitions or null if
   *         none
   */
  private Ambience getAmbience(Style style) {
    for (Transition transition : transitions) {
      if (transition.getFrom().getStyles().contains(style)) {
        return transition.getFrom();
      }
    }
    return null;
  }

  /**
   * (non-Javadoc)
   * 
   * @see dj.DigitalDJ#toXML()
   */
  @Override
  public String toXML() {
    StringBuilder sb = new StringBuilder(2000);
    sb.append(toXMLGeneralParameters());
    sb.append("\t<" + Const.XML_DJ_TRANSITIONS + "'>\n");
    for (Transition transition : transitions) {
      sb.append("\t\t<" + Const.XML_DJ_TRANSITION + " " + Const.XML_DJ_FROM + "='"
          + transition.getFrom().toXML() + "' " + Const.XML_DJ_TO + "='"
          + transition.getTo().toXML() + "' " + Const.XML_DJ_NUMBER + "='"
          + transition.getNbTracks() + "'/>\n");
    }
    sb.append("\t</" + Const.XML_DJ_TRANSITIONS + ">\n");
    sb.append("</" + Const.XML_DJ_DJ + ">\n");
    return sb.toString();
  }

  public void setTransitions(List<Transition> transitions) {
    this.transitions = transitions;
  }

}
