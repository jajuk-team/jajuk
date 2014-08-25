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
package org.jajuk.ui.wizard.digital_dj;

import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceDigitalDJ;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.dj.Proportion;
import org.jajuk.services.dj.ProportionDigitalDJ;
import org.jajuk.services.dj.Transition;
import org.jajuk.services.dj.TransitionDigitalDJ;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;
import org.qdwizard.Wizard;

/**
 * DJ creation wizard.
 */
public class DigitalDJWizard extends Wizard {
  /**
   * List of the variables used in the wizard
   */
  enum Variable {
    /** Wizard action. */
    ACTION,
    /** DJ type variable name. */
    DJ_TYPE,
    /** DJ name variable name. */
    DJ_NAME,
    /** Track unicity. */
    UNICITY,
    /** Ratings level. */
    RATINGS_LEVEL,
    /** Fade duration. */
    FADE_DURATION,
    /** Transitions. */
    TRANSITIONS,
    /** Proportions. */
    PROPORTIONS,
    /** Ambience. */
    AMBIENCE,
    /** DJ to remove. */
    REMOVE,
    /** DJ to change. */
    CHANGE,
    /** Max number of tracks to queue. */
    MAX_TRACKS
  }

  /**
   * Gets the widget index.
   *
   * @param widgets 
   * @param widget 
   * @return index of a given widget row in the widget table
   */
  static int getWidgetIndex(JComponent[][] widgets, JComponent widget) {
    for (int row = 0; row < widgets.length; row++) {
      for (int col = 0; col < widgets[0].length; col++) {
        if (widget.equals(widgets[row][col])) {
          return row;
        }
      }
    }
    return -1;
  }

  @Override
  public Class<? extends org.qdwizard.Screen> getPreviousScreen(
      Class<? extends org.qdwizard.Screen> screen) {
    if (ActionSelectionScreen.class.equals(screen)) {
      return null;
    } else if (TypeSelectionScreen.class.equals(screen)) {
      return ActionSelectionScreen.class;
    } else if (GeneralOptionsScreen.class.equals(screen)) {
      if (ActionSelectionScreen.ACTION_CREATION.equals(data.get(Variable.ACTION))) {
        return TypeSelectionScreen.class;
      } else {
        return ChangeScreen.class;
      }
    } else if (TransitionsScreen.class.equals(screen) || ProportionsScreen.class.equals(screen)
        || AmbiencesScreen.class.equals(screen)) {
      return GeneralOptionsScreen.class;
    } else if (RemoveScreen.class.equals(screen)) {
      return ActionSelectionScreen.class;
    } else if (ChangeScreen.class.equals(screen)) {
      return ActionSelectionScreen.class;
    }
    return null;
  }

  @Override
  public Class<? extends org.qdwizard.Screen> getNextScreen(
      Class<? extends org.qdwizard.Screen> screen) {
    if (ActionSelectionScreen.class.equals(screen)) {
      String sAction = (String) data.get(Variable.ACTION);
      if (ActionSelectionScreen.ACTION_CREATION.equals(sAction)) {
        return TypeSelectionScreen.class;
      } else if (ActionSelectionScreen.ACTION_CHANGE.equals(sAction)) {
        return ChangeScreen.class;
      } else if (ActionSelectionScreen.ACTION_DELETE.equals(sAction)) {
        return RemoveScreen.class;
      }
    } else if (TypeSelectionScreen.class.equals(screen)) {
      return GeneralOptionsScreen.class;
    } else if (GeneralOptionsScreen.class.equals(screen)) {
      String sType = (String) data.get(Variable.DJ_TYPE);
      if (TypeSelectionScreen.DJ_TYPE_AMBIENCE.equals(sType)) {
        return AmbiencesScreen.class;
      } else if (TypeSelectionScreen.DJ_TYPE_PROPORTION.equals(sType)) {
        return ProportionsScreen.class;
      } else if (TypeSelectionScreen.DJ_TYPE_TRANSITION.equals(sType)) {
        return TransitionsScreen.class;
      }
    } else if (RemoveScreen.class.equals(screen)) {
      return null;
    } else if (ChangeScreen.class.equals(screen)) {
      return GeneralOptionsScreen.class;
    }
    return null;
  }

  /**
   * Instantiates a new digital dj wizard.
   */
  public DigitalDJWizard() {
    super(new Wizard.Builder(Messages.getString("DigitalDJWizard.4"), ActionSelectionScreen.class,
        JajukMainWindow.getInstance()).hSize(700).vSize(500)
        .icon(IconLoader.getIcon(JajukIcons.DIGITAL_DJ)));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void finish() {
    DigitalDJ dj = null;
    String sAction = (String) data.get(Variable.ACTION);
    if (ActionSelectionScreen.ACTION_DELETE.equals(sAction)) {
      try {
        DigitalDJManager.getInstance().remove((DigitalDJ) data.get(Variable.REMOVE));
      } catch (IOException e) {
        Log.error(e);
      }
    } else if (ActionSelectionScreen.ACTION_CREATION.equals(sAction)) {
      String sType = (String) data.get(Variable.DJ_TYPE);
      // create a unique ID for this DJ, simply use current time in ms
      String sID = Long.toString(System.currentTimeMillis());
      if (TypeSelectionScreen.DJ_TYPE_AMBIENCE.equals(sType)) {
        Ambience ambience = (Ambience) data.get(Variable.AMBIENCE);
        dj = new AmbienceDigitalDJ(sID);
        ((AmbienceDigitalDJ) dj).setAmbience(ambience);
      } else if (TypeSelectionScreen.DJ_TYPE_PROPORTION.equals(sType)) {
        dj = new ProportionDigitalDJ(sID);
        List<Proportion> proportions = (List<Proportion>) data.get(Variable.PROPORTIONS);
        ((ProportionDigitalDJ) dj).setProportions(proportions);
      } else if (TypeSelectionScreen.DJ_TYPE_TRANSITION.equals(sType)) {
        List<Transition> transitions = (List<Transition>) data.get(Variable.TRANSITIONS);
        dj = new TransitionDigitalDJ(sID);
        ((TransitionDigitalDJ) dj).setTransitions(transitions);
      } else {
        throw new IllegalArgumentException("Unknown type of DJ: " + sType);
      }
      setProperties(dj);
      DigitalDJManager.getInstance().register(dj);
      // commit the DJ right now
      DigitalDJManager.commit(dj);
      // If first DJ, select it as default
      if (DigitalDJManager.getInstance().getDJs().size() == 1) {
        Conf.setProperty(Const.CONF_DEFAULT_DJ, dj.getID());
      }
    } else if (ActionSelectionScreen.ACTION_CHANGE.equals(sAction)) {
      String sType = (String) data.get(Variable.DJ_TYPE);
      dj = (DigitalDJ) data.get(Variable.CHANGE);
      if (TypeSelectionScreen.DJ_TYPE_AMBIENCE.equals(sType)) {
        Ambience ambience = (Ambience) data.get(Variable.AMBIENCE);
        ((AmbienceDigitalDJ) dj).setAmbience(ambience);
      } else if (TypeSelectionScreen.DJ_TYPE_PROPORTION.equals(sType)) {
        List<Proportion> proportions = (List<Proportion>) data.get(Variable.PROPORTIONS);
        ((ProportionDigitalDJ) dj).setProportions(proportions);
      } else if (TypeSelectionScreen.DJ_TYPE_TRANSITION.equals(sType)) {
        List<Transition> transitions = (List<Transition>) data.get(Variable.TRANSITIONS);
        ((TransitionDigitalDJ) dj).setTransitions(transitions);
      }
      setProperties(dj);
      // commit the DJ right now
      DigitalDJManager.commit(dj);
    }
    // Refresh command panel (useful for ie if DJ names changed)
    ObservationManager.notify(new JajukEvent(JajukEvents.DJS_CHANGE));
    InformationJPanel.getInstance().setMessage(Messages.getString("Success"),
        InformationJPanel.MessageType.INFORMATIVE);
  }

  /**
   * Store the properties from the Wizard to the specified DJ.
   * 
   * @param dj The DJ to populate.
   */
  private void setProperties(DigitalDJ dj) {
    String sName = (String) data.get(Variable.DJ_NAME);
    int iFadeDuration = (Integer) data.get(Variable.FADE_DURATION);
    int iRateLevel = (Integer) data.get(Variable.RATINGS_LEVEL);
    boolean bUnicity = (Boolean) data.get(Variable.UNICITY);
    int iMaxTracks = (Integer) data.get(Variable.MAX_TRACKS);
    dj.setName(sName);
    dj.setFadingDuration(iFadeDuration);
    dj.setRatingLevel(iRateLevel);
    dj.setTrackUnicity(bUnicity);
    dj.setMaxTracks(iMaxTracks);
  }
}
