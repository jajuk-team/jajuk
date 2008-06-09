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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Style;
import org.jajuk.util.UtilFeatures;

/**
 * Type description
 */
public class TransitionDigitalDJ extends DigitalDJ {

  /** List of transitions, need to be a list, not a set for offset */
  private List<Transition> transitions;

  /** Startup style* */
  private Style startupStyle;

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
    // Sort tracks by FROM ambience (set of styles)
    Map<Ambience, List<File>> hmAmbienceFiles = new HashMap<Ambience, List<File>>(100);
    // This list contains all files not yet sorted (used for null key)
    List<File> alFilesToSort = (List<File>) ((ArrayList<File>) global).clone();
    for (Transition tr : transitions) {
      Ambience from = null;
      if (tr != null) {
        from = tr.getFrom();
      }
      List<File> files = new ArrayList<File>(100);
      for (File file : global) {
        if (from != null && from.getStyles().contains(file.getTrack().getStyle())) {
          files.add(file);
          alFilesToSort.remove(file);
        }
      }
      hmAmbienceFiles.put(from, files);
      Ambience to = null;
      if (tr != null) {
        to = tr.getTo();
      }
      files = new ArrayList(100);
      for (File file : global) {
        if (to != null && to.getStyles().contains(file.getTrack().getStyle())) {
          files.add(file);
          alFilesToSort.remove(file);
        }
      }
      hmAmbienceFiles.put(to, files);
    }
    // fill null key
    hmAmbienceFiles.put(null, alFilesToSort);
    // Get first track
    for (File file : global) {
      if (file.getTrack().getStyle().equals(startupStyle)) {
        out.add(file);
        break;
      }
    }
    // none matching track? add a shuffle file
    if (out.size() == 0) {
      out.add((File) UtilFeatures.getShuffleItem(global));
    }
    // compute number of items to add
    int items = global.size() - 1; // by default, collection size (minus
    // one already added)
    if (!bUnicity && items < MIN_TRACKS_NUMBER_WITHOUT_UNICITY) {
      // under a limit, if collection is too small and no unicity, use
      // several times the same files
      items = MIN_TRACKS_NUMBER_WITHOUT_UNICITY;
    }
    // initialize current ambience with firs track ambience (can be null for
    // unsorted tracks)
    Ambience currentAmbience = getAmbience(out.get(0).getTrack().getStyle());
    int comp = 1; // item compt
    boolean bFirstTrack = true; // flag used to remove one track to
    // selection
    // start transition applying
    while (comp < items) {
      int nb = 1;
      Transition currentTransition = getTransition(currentAmbience);
      if (currentTransition != null) {
        nb = currentTransition.getNbTracks();
        if (bFirstTrack) {
          nb--;
          bFirstTrack = false;
        }
      }
      List<File> files = hmAmbienceFiles.get(currentAmbience);
      if (files != null && files.size() > nb) {
        for (int i = 0; i < nb; i++) {
          File file = (File) UtilFeatures.getShuffleItem(files);
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
      if (currentTransition != null) {
        currentAmbience = currentTransition.getTo();
      } else {
        return out;
      }
    }
    return out;
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
    sb.append("\t<" + XML_DJ_TRANSITIONS + " " + XML_DJ_STARTUP_STYLE + "='"
        + getStartupStyle().getID() + "'>\n");
    for (Transition transition : transitions) {
      sb.append("\t\t<" + XML_DJ_TRANSITION + " " + XML_DJ_FROM + "='"
          + transition.getFrom().toXML() + "' " + XML_DJ_TO + "='" + transition.getTo().toXML()
          + "' " + XML_DJ_NUMBER + "='" + transition.getNbTracks() + "'/>\n");
    }
    sb.append("\t</" + XML_DJ_TRANSITIONS + ">\n");
    sb.append("</" + XML_DJ_DJ + ">\n");
    return sb.toString();
  }

  public Style getStartupStyle() {
    return this.startupStyle;
  }

  public void setStartupStyle(Style startupStyle) {
    this.startupStyle = startupStyle;
  }

  public void setTransitions(List<Transition> transitions) {
    this.transitions = transitions;
  }

}
