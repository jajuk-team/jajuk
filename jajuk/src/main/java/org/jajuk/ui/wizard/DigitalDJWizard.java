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
 *  $Revision$
 */
package org.jajuk.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceDigitalDJ;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.dj.Proportion;
import org.jajuk.services.dj.ProportionDigitalDJ;
import org.jajuk.services.dj.Transition;
import org.jajuk.services.dj.TransitionDigitalDJ;
import org.jajuk.ui.helpers.DefaultMouseWheelListener;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.LocaleManager;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;
import org.qdwizard.ClearPoint;
import org.qdwizard.Screen;
import org.qdwizard.Wizard;

/**
 * DJ creation wizard.
 */
public class DigitalDJWizard extends Wizard {

  /** Wizard action. */
  private static final String KEY_ACTION = "ACTION";

  /** DJ type variable name. */
  private static final String KEY_DJ_TYPE = "Type";

  /** DJ name variable name. */
  private static final String KEY_DJ_NAME = "NAME";

  /** Track unicity. */
  private static final String KEY_UNICITY = "UNICITY";

  /** Ratings level. */
  private static final String KEY_RATINGS_LEVEL = "RATING_LEVEL";

  /** Fade duration. */
  private static final String KEY_FADE_DURATION = "FADE_DURATION";

  /** Transitions. */
  private static final String KEY_TRANSITIONS = "TRANSITIONS";

  /** Proportions. */
  private static final String KEY_PROPORTIONS = "PROPORTIONS";

  /** Ambience. */
  private static final String KEY_AMBIENCE = "AMBIENCE";

  /** DJ to remove. */
  private static final String KEY_REMOVE = "REMOVE";

  /** DJ to change. */
  private static final String KEY_CHANGE = "CHANGE";

  /** Max number of tracks to queue. */
  private static final String KEY_MAX_TRACKS = "MAXTRACKS";

  /**
   * DJ type choice.
   */
  public static class TypeSelectionPanel extends Screen implements ActionListener {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Transition DJ code. */
    private static final String DJ_TYPE_TRANSITION = "0";

    /** Proportions DJ code. */
    private static final String DJ_TYPE_PROPORTION = "1";

    /** Ambience DJ code. */
    private static final String DJ_TYPE_AMBIENCE = "2";

    /** DOCUMENT_ME. */
    ButtonGroup bgTypes;

    /** DOCUMENT_ME. */
    JRadioButton jrbTransitions;

    /** DOCUMENT_ME. */
    JRadioButton jrbProp;

    /** DOCUMENT_ME. */
    JRadioButton jrbAmbiance;

    /**
     * Create panel UI.
     */
    @Override
    public void initUI() {
      setLayout(new MigLayout("insets 10,gapx 10,gapy 15"));
      bgTypes = new ButtonGroup();
      jrbTransitions = new JRadioButton(Messages.getString("DigitalDJWizard.1"));
      jrbTransitions.addActionListener(this);
      jrbTransitions.doClick(); // default selection
      jrbProp = new JRadioButton(Messages.getString("DigitalDJWizard.2"));
      jrbProp.addActionListener(this);
      jrbAmbiance = new JRadioButton(Messages.getString("DigitalDJWizard.3"));
      jrbAmbiance.addActionListener(this);
      // can select ambience DJ only if at least one ambience defined
      jrbAmbiance.setEnabled(AmbienceManager.getInstance().getAmbiences().size() > 0);
      bgTypes.add(jrbProp);
      bgTypes.add(jrbTransitions);
      bgTypes.add(jrbAmbiance);
      add(jrbTransitions, "left,wrap");
      add(jrbProp, "left,wrap");
      add(jrbAmbiance, "left,wrap");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */

    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == jrbTransitions) {
        data.put(KEY_DJ_TYPE, DJ_TYPE_TRANSITION);
      } else if (e.getSource() == jrbProp) {
        data.put(KEY_DJ_TYPE, DJ_TYPE_PROPORTION);
      } else if (e.getSource() == jrbAmbiance) {
        data.put(KEY_DJ_TYPE, DJ_TYPE_AMBIENCE);
      }
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("DigitalDJWizard.0");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("DigitalDJWizard.46");
    }
  }

  /**
   * DJ removal.
   */
  public static class RemovePanel extends Screen implements ActionListener {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** DOCUMENT_ME. */
    JComponent[][] widgets;

    /** DOCUMENT_ME. */
    ButtonGroup bgDJS;

    /** DOCUMENT_ME. */
    List<DigitalDJ> djs;

    /**
     * Create panel UI.
     */

    @Override
    public void initUI() {
      djs = new ArrayList<DigitalDJ>(DigitalDJManager.getInstance().getDJs());
      Collections.sort(djs);
      widgets = new JComponent[djs.size()][1];
      // We use an inner panel for scrolling purpose
      JPanel jp = new JPanel();
      jp.setLayout(new MigLayout("insets 0,gapx 0,gapy 10"));
      bgDJS = new ButtonGroup();
      setCanFinish(true);
      int index = 0;
      for (DigitalDJ dj : djs) {
        JRadioButton jrb = new JRadioButton(dj.getName());
        jrb.addActionListener(this);
        bgDJS.add(jrb);
        widgets[index][0] = jrb;
        jp.add(jrb, "left gap 5,wrap");
        index++;
      }
      setProblem(Messages.getString("DigitalDJWizard.40"));
      // select first ambience found
      JRadioButton jrb = (JRadioButton) widgets[0][0];
      jrb.doClick();
      setLayout(new MigLayout("insets 10,gapx 5", "[grow]"));
      JScrollPane jsp = new JScrollPane(jp);
      jsp.setBorder(null);
      add(jsp, "grow");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = getWidgetIndex(widgets, (JComponent) e.getSource());
      data.put(KEY_REMOVE, djs.get(row));
      setProblem(null);
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("DigitalDJWizard.40");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("DigitalDJWizard.51");
    }
  }

  /**
   * DJ Selection for change.
   */
  public static class ChangePanel extends Screen implements ActionListener {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** DOCUMENT_ME. */
    JComponent[][] widgets;

    /** DOCUMENT_ME. */
    ButtonGroup bgDJS;

    /** DOCUMENT_ME. */
    List<DigitalDJ> djs;

    /**
     * Create panel UI.
     */

    @Override
    public void initUI() {
      djs = DigitalDJManager.getInstance().getDJsSorted();
      // We use an inner panel for scrolling purpose
      JPanel jp = new JPanel();
      jp.setLayout(new MigLayout("insets 0,gapx 0,gapy 10"));
      widgets = new JComponent[djs.size()][1];
      setLayout(new MigLayout("insets 10,gapx 10,gapy 15"));
      bgDJS = new ButtonGroup();
      int index = 0;
      for (DigitalDJ dj : djs) {
        JRadioButton jrb = new JRadioButton(dj.getName());
        jrb.addActionListener(this);
        bgDJS.add(jrb);
        widgets[index][0] = jrb;
        jp.add(jrb, "left gap 5,wrap");
        index++;
      }
      // If more than one DJ, select first
      if (djs.size() > 0) {
        JRadioButton jrb = (JRadioButton) widgets[0][0];
        jrb.doClick();
      } else {
        setProblem(Messages.getString("DigitalDJWizard.40"));
      }
      setLayout(new MigLayout("insets 10,gapx 5", "[grow]"));
      JScrollPane jsp = new JScrollPane(jp);
      jsp.setBorder(null);
      add(jsp, "grow");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = getWidgetIndex(widgets, (JComponent) e.getSource());
      // set DJ type useful for screen choice
      DigitalDJ dj = djs.get(row);
      data.put(KEY_CHANGE, dj);
      if (dj instanceof AmbienceDigitalDJ) {
        data.put(KEY_DJ_TYPE, TypeSelectionPanel.DJ_TYPE_AMBIENCE);
      }
      if (dj instanceof ProportionDigitalDJ) {
        data.put(KEY_DJ_TYPE, TypeSelectionPanel.DJ_TYPE_PROPORTION);
      }
      if (dj instanceof TransitionDigitalDJ) {
        data.put(KEY_DJ_TYPE, TypeSelectionPanel.DJ_TYPE_TRANSITION);
      }
      setProblem(null);
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("DigitalDJWizard.44");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("DigitalDJWizard.43");
    }
  }

  /**
   * Action type (new or alter).
   */
  public static class ActionSelectionPanel extends Screen implements ClearPoint, ActionListener {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** NEW code. */
    public static final String ACTION_CREATION = "0";

    /** CHANGE code. */
    public static final String ACTION_CHANGE = "1";

    /** DELETE code. */
    public static final String ACTION_DELETE = "2";

    /** DOCUMENT_ME. */
    ButtonGroup bgActions;

    /** DOCUMENT_ME. */
    JRadioButton jrbNew;

    /** DOCUMENT_ME. */
    JRadioButton jrbChange;

    /** DOCUMENT_ME. */
    JRadioButton jrbDelete;

    /**
     * Create panel UI.
     */
    @Override
    public void initUI() {
      setLayout(new MigLayout("insets 10,gapx 10,gapy 15"));
      bgActions = new ButtonGroup();
      jrbNew = new JRadioButton(Messages.getString("DigitalDJWizard.17"));
      jrbNew.addActionListener(this);
      jrbNew.doClick();
      jrbChange = new JRadioButton(Messages.getString("DigitalDJWizard.18"));
      jrbChange.addActionListener(this);
      jrbDelete = new JRadioButton(Messages.getString("DigitalDJWizard.19"));
      jrbDelete.addActionListener(this);
      // disabled change and remove if none dj
      if (DigitalDJManager.getInstance().getDJs().size() == 0) {
        jrbChange.setEnabled(false);
        jrbDelete.setEnabled(false);
      }
      bgActions.add(jrbNew);
      bgActions.add(jrbChange);
      bgActions.add(jrbDelete);
      add(jrbNew, "left,wrap");
      add(jrbChange, "left,wrap");
      add(jrbDelete, "left,wrap");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */

    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == jrbNew) {
        data.put(KEY_ACTION, ACTION_CREATION);
      } else if (e.getSource() == jrbChange) {
        data.put(KEY_ACTION, ACTION_CHANGE);
      } else if (e.getSource() == jrbDelete) {
        data.put(KEY_ACTION, ACTION_DELETE);
      }
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("DigitalDJWizard.16");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("DigitalDJWizard.45");
    }
  }

  /**
   * General options panel.
   */
  public static class GeneralOptionsPanel extends Screen implements ActionListener, CaretListener,
      ChangeListener {

    /** The Constant NO_MAX_TRACKS.  DOCUMENT_ME */
    private static final String NO_MAX_TRACKS = "  ";

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** DOCUMENT_ME. */
    JLabel jlName;

    /** DOCUMENT_ME. */
    JTextField jtfName;

    /** DOCUMENT_ME. */
    JLabel jlRatingLevel;

    /** DOCUMENT_ME. */
    JSlider jsRatingLevel;

    /** DOCUMENT_ME. */
    JLabel jlFadeDuration;

    /** DOCUMENT_ME. */
    JSlider jsFadeDuration;

    /** DOCUMENT_ME. */
    JCheckBox jcbMaxTracks;

    /** DOCUMENT_ME. */
    JSlider jsMaxTracks;

    /** DOCUMENT_ME. */
    JLabel jnMaxTracks;

    /** DOCUMENT_ME. */
    JCheckBox jcbUnicity;

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("DigitalDJWizard.49");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("DigitalDJWizard.48");
    }

    /**
     * Create panel UI.
     */

    @Override
    public void initUI() {
      if (ActionSelectionPanel.ACTION_CREATION.equals(data.get(KEY_ACTION))) {
        // default values
        data.put(KEY_FADE_DURATION, 10);
        data.put(KEY_RATINGS_LEVEL, 0); // all tracks by default
        data.put(KEY_UNICITY, false);
        data.put(KEY_MAX_TRACKS, -1);
      } else if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))) {
        // keep existing DJ values
        DigitalDJ dj = (DigitalDJ) data.get(KEY_CHANGE);
        data.put(KEY_FADE_DURATION, dj.getFadingDuration());
        data.put(KEY_RATINGS_LEVEL, dj.getRatingLevel());
        data.put(KEY_UNICITY, dj.isTrackUnicity());
        data.put(KEY_MAX_TRACKS, dj.getMaxTracks());
      }
      jlName = new JLabel(Messages.getString("DigitalDJWizard.6"));
      jtfName = new JTextField();
      jtfName.setToolTipText(Messages.getString("DigitalDJWizard.6"));
      jtfName.addCaretListener(this);
      jtfName.requestFocusInWindow();

      jlRatingLevel = new JLabel(Messages.getString("DigitalDJWizard.8"));
      jlRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.53"));
      jsRatingLevel = new JSlider(0, 4, (Integer) data.get(KEY_RATINGS_LEVEL));
      jsRatingLevel.setMajorTickSpacing(1);
      jsRatingLevel.setMinorTickSpacing(1);
      jsRatingLevel.setPaintTicks(true);
      jsRatingLevel.setSnapToTicks(true);
      jsRatingLevel.setPaintLabels(true);
      jsRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.53"));
      jsRatingLevel.addMouseWheelListener(new DefaultMouseWheelListener(jsRatingLevel));
      jsRatingLevel.addChangeListener(this);

      jlFadeDuration = new JLabel(Messages.getString("DigitalDJWizard.9"));
      jlFadeDuration.setToolTipText(Messages.getString("DigitalDJWizard.54"));
      jsFadeDuration = new JSlider(0, 30, (Integer) data.get(KEY_FADE_DURATION));
      jsFadeDuration.addMouseWheelListener(new DefaultMouseWheelListener(jsFadeDuration));
      jsFadeDuration.addChangeListener(this);
      jsFadeDuration.setMajorTickSpacing(10);
      jsFadeDuration.setMinorTickSpacing(1);
      jsFadeDuration.setPaintTicks(true);
      jsFadeDuration.setPaintLabels(true);
      jsFadeDuration.setToolTipText(Messages.getString("DigitalDJWizard.54"));

      // CheckBox for enabling/disabling slider, jsMaxTrack
      int nMaxTracks = (Integer) data.get(KEY_MAX_TRACKS);
      jcbMaxTracks = new JCheckBox(Messages.getString("DigitalDJWizard.67"), nMaxTracks != -1);
      jcbMaxTracks.setToolTipText(Messages.getString("DigitalDJWizard.68"));

      // initialize the slider based if max track is enabled or not
      if (nMaxTracks != -1) {
        jsMaxTracks = new JSlider(0, 5000, nMaxTracks);
        jsMaxTracks.setEnabled(true);
        jnMaxTracks = new JLabel(Integer.toString(nMaxTracks));
      } else {
        jsMaxTracks = new JSlider(0, 5000, 100);
        jsMaxTracks.setEnabled(false);
        jnMaxTracks = new JLabel(NO_MAX_TRACKS);
      }
      jnMaxTracks.setBorder(new BevelBorder(BevelBorder.LOWERED));
      jsMaxTracks.addMouseWheelListener(new DefaultMouseWheelListener(jsMaxTracks));
      jsMaxTracks.addChangeListener(this);
      jsMaxTracks.setMajorTickSpacing(100);
      jsMaxTracks.setMinorTickSpacing(10);
      jsMaxTracks.setPaintTicks(false);
      jsMaxTracks.setPaintLabels(false);
      jsMaxTracks.setToolTipText(Messages.getString("DigitalDJWizard.68"));

      // enable/disable slider depending on checkbox
      jcbMaxTracks.addActionListener(this);

      jcbUnicity = new JCheckBox(Messages.getString("DigitalDJWizard.10"),
          (Boolean) data.get(KEY_UNICITY));
      jcbUnicity.setToolTipText(Messages.getString("DigitalDJWizard.55"));
      jcbUnicity.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          data.put(KEY_UNICITY, jcbUnicity.isSelected());
        }
      });

      // DJ change, set default values
      if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))) {
        DigitalDJ dj = (DigitalDJ) data.get(KEY_CHANGE);
        jtfName.setText(dj.getName());
        jsFadeDuration.setValue((Integer) data.get(KEY_FADE_DURATION));
        jsRatingLevel.setValue((Integer) data.get(KEY_RATINGS_LEVEL));
        jcbUnicity.setSelected((Boolean) data.get(KEY_UNICITY));
        if (((Integer) data.get(KEY_MAX_TRACKS)) != -1) {
          jsMaxTracks.setValue((Integer) data.get(KEY_MAX_TRACKS));
        } else {
          jsMaxTracks.setValue(100);
        }
      } else { // new dj, dj name is required
        setProblem(Messages.getString("DigitalDJWizard.41"));
      }

      setLayout(new MigLayout("insets 10,gapx 10,gapy 15", "[][grow]"));
      add(jlName);
      add(jtfName, "grow,wrap");
      add(jlRatingLevel);
      add(jsRatingLevel, "grow,wrap");
      add(jlFadeDuration);
      add(jsFadeDuration, "grow,wrap");
      add(jcbMaxTracks);
      {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", "[grow][]"));
        panel.add(jsMaxTracks, "grow");
        panel.add(jnMaxTracks);
        add(panel, "grow,wrap");
      }
      add(jcbUnicity, "wrap");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */

    @Override
    public void actionPerformed(ActionEvent ae) {
      if (ae.getSource() == jcbUnicity) {
        data.put(KEY_UNICITY, jcbUnicity.isSelected());
      } else if (ae.getSource() == jcbMaxTracks) {
        jsMaxTracks.setEnabled(jcbMaxTracks.isSelected());
        updateMaxTracks();
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
     */

    @Override
    public void caretUpdate(CaretEvent ce) {
      if (ce.getSource() == jtfName) {
        data.put(KEY_DJ_NAME, jtfName.getText());
        String sName = jtfName.getText();
        // string length = 0
        if (sName.length() == 0) {
          setProblem(Messages.getString("DigitalDJWizard.41"));
        }
        // display an error message if the dj already exists and not in
        // "change" mode
        else if (DigitalDJManager.getInstance().getDJNames().contains(sName)) {
          // if we are in change mode and the name is still the
          // same, no error
          if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))
              && ((DigitalDJ) data.get(KEY_CHANGE)).getName().equals(sName)) {
            setProblem(null);
            return;
          }
          setProblem(Messages.getString("DigitalDJWizard.42"));
        } else {
          setProblem(null); // no more problem
        }
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
     * )
     */

    @Override
    public void stateChanged(ChangeEvent ie) {
      if (ie.getSource() == jsFadeDuration && !jsFadeDuration.getValueIsAdjusting()) {
        data.put(KEY_FADE_DURATION, jsFadeDuration.getValue());
      } else if (ie.getSource() == jsRatingLevel && !jsRatingLevel.getValueIsAdjusting()) {
        data.put(KEY_RATINGS_LEVEL, jsRatingLevel.getValue());
      } else if (ie.getSource() == jsMaxTracks) {
        updateMaxTracks();
      }

    }

    /**
     * Update all items related to the Max Track feature.
     */
    private void updateMaxTracks() {
      // store -1 if checkbox is not enabled and update the label accordingly
      if (jcbMaxTracks.isSelected()) {
        if (!jsMaxTracks.getValueIsAdjusting()) {
          data.put(KEY_MAX_TRACKS, jsMaxTracks.getValue());
        }
        jnMaxTracks.setText(Integer.toString(jsMaxTracks.getValue()));
      } else {
        if (!jsMaxTracks.getValueIsAdjusting()) {
          data.put(KEY_MAX_TRACKS, -1);
        }
        jnMaxTracks.setText(NO_MAX_TRACKS);
      }
    }
  }

  /**
   * Transitions panel.
   */
  public static class TransitionsPanel extends Screen {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** All dynamic widgets. */
    JComponent[][] widgets;

    /** Transitions*. */
    List<Transition> alTransitions;

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("DigitalDJWizard.52");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("DigitalDJWizard.20");
    }

    /**
     * Gets the cleaned transitions.
     * 
     * @return Filled transitions only
     */
    private List<Transition> getCleanedTransitions() {
      List<Transition> out = new ArrayList<Transition>(alTransitions.size());
      for (Transition transition : alTransitions) {
        if (transition.getFrom() != null && transition.getTo() != null
            && transition.getFrom().getGenres().size() > 0
            && transition.getTo().getGenres().size() > 0) {
          out.add(transition);
        }
      }
      return out;
    }

    /**
     * Create panel UI.
     */

    @Override
    @SuppressWarnings("unchecked")
    public void initUI() {
      if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))) {
        TransitionDigitalDJ dj = (TransitionDigitalDJ) data.get(KEY_CHANGE);
        alTransitions = (List<Transition>) ((ArrayList<Transition>) dj.getTransitions()).clone();
        data.put(KEY_TRANSITIONS, getCleanedTransitions());
        // add a void transition
        alTransitions.add(new Transition(Const.DEFAULT_TRANSITION_TRACK_NUMBER));
      } else { // DJ creation
        alTransitions = new ArrayList<Transition>(10);
        // add a void transition
        alTransitions.add(new Transition(Const.DEFAULT_TRANSITION_TRACK_NUMBER));
        setProblem(Messages.getString("DigitalDJWizard.26"));
      }
      setCanFinish(true);
      // set layout
      setLayout(new MigLayout("insets 10,gapx 5", "[grow]"));
      add(getTransitionsPanel(), "grow");
    }

    /**
     * Gets the transitions panel.
     * 
     * @return a panel containing all transitions
     */
    private JScrollPane getTransitionsPanel() {
      widgets = new JComponent[alTransitions.size()][4];
      JPanel out = new JPanel();
      // Delete|FROM list| To list|nb tracks
      // now add all known transitions
      for (int index = 0; index < alTransitions.size(); index++) {
        // Delete button
        JButton jbDelete = new JButton(IconLoader.getIcon(JajukIcons.DELETE));
        jbDelete.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent ae) {
            alTransitions.remove(getWidgetIndex(widgets, (JComponent) ae.getSource()));
            refreshScreen();
            data.put(KEY_TRANSITIONS, getCleanedTransitions());
          }
        });
        // cannot delete if void selection
        if (alTransitions.size() == 1) {
          jbDelete.setEnabled(false);
        }
        jbDelete.setToolTipText(Messages.getString("DigitalDJWizard.21"));
        widgets[index][0] = jbDelete;
        // From genre list
        JButton jbFrom = new JButton(IconLoader.getIcon(JajukIcons.LIST));
        Transition transition = alTransitions.get(index);
        if (transition.getFrom().getGenres().size() > 0) {
          jbFrom.setText(transition.getFromString());
          jbFrom.setToolTipText(transition.getFromString());
        }
        jbFrom.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent ae) {
            int row = getWidgetIndex(widgets, (JComponent) ae.getSource());
            addGenre(row, true);
          }
        });
        jbFrom.setToolTipText(Messages.getString("DigitalDJWizard.22"));
        widgets[index][1] = jbFrom;
        // To genre list
        JButton jbTo = new JButton(IconLoader.getIcon(JajukIcons.LIST));
        if (transition.getTo().getGenres().size() > 0) {
          jbTo.setText(transition.getToString());
          jbTo.setToolTipText(transition.getToString());
        }
        jbTo.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent ae) {
            int row = getWidgetIndex(widgets, (JComponent) ae.getSource());
            addGenre(row, false);
          }
        });
        jbTo.setToolTipText(Messages.getString("DigitalDJWizard.23"));
        widgets[index][2] = jbTo;
        // Nb of tracks
        JSpinner jsNb = new JSpinner(new SpinnerNumberModel(transition.getNbTracks(), 1, 10, 1));
        jsNb.addChangeListener(new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent ce) {
            int row = getWidgetIndex(widgets, (JComponent) ce.getSource());
            int nb = Integer.parseInt(((JSpinner) ce.getSource()).getValue().toString());
            Transition transition = alTransitions.get(row);
            transition.setNb(nb);
          }
        });
        jsNb.setToolTipText(Messages.getString("DigitalDJWizard.24"));
        widgets[index][3] = jsNb;
      }
      // Create layout
      out.setLayout(new MigLayout("insets 5,gapx 10,gapy 10", "[][270!][270!][]"));
      // Create header
      JLabel jlHeader2 = new JLabel(Messages.getString("DigitalDJWizard.22"));
      jlHeader2.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      JLabel jlHeader3 = new JLabel(Messages.getString("DigitalDJWizard.23"));
      jlHeader3.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      JLabel jlHeader4 = new JLabel(Messages.getString("DigitalDJWizard.24"));
      jlHeader4.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      out.add(jlHeader2, "cell 1 0, center");
      out.add(jlHeader3, "cell 2 0,center");
      out.add(jlHeader4, "cell 3 0,center,wrap");
      // Add widgets
      for (int i = 0; i < widgets.length; i++) {
        out.add(widgets[i][0]);
        out.add(widgets[i][1], "grow,width ::270");
        out.add(widgets[i][2], "grow,width ::270");
        out.add(widgets[i][3], "grow,center,wrap");
      }
      JScrollPane jsp = new JScrollPane(out);
      jsp.setBorder(null);
      return jsp;
    }

    /**
     * Add a genre to a transition.
     * 
     * @param row row
     * @param bFrom is it a from button ?
     */
    private void addGenre(int row, boolean bFrom) {
      synchronized (GenreManager.getInstance()) {
        Transition transition = alTransitions.get(row);
        // create list of genres used in existing transitions
        Set<Genre> disabledGenres = new HashSet<Genre>();
        for (int i = 0; i < alTransitions.size(); i++) {
          Transition t = alTransitions.get(i);
          // ignore all genres expect those from current button
          if (bFrom && i != row) {
            disabledGenres.addAll(t.getFrom().getGenres());
          }
        }
        GenresSelectionDialog dialog = new GenresSelectionDialog(disabledGenres);
        if (bFrom) {
          dialog.setSelection(transition.getFrom().getGenres());
        } else {
          dialog.setSelection(transition.getTo().getGenres());
        }
        dialog.setVisible(true);
        Set<Genre> genres = dialog.getSelectedGenres();
        // check if at least one genre has been selected
        if (genres.size() == 0) {
          return;
        }
        String sText = "";
        for (Genre genre : genres) {
          sText += genre.getName2() + ',';
        }
        sText = sText.substring(0, sText.length() - 1);
        int nb = Integer.parseInt(((JSpinner) widgets[row][3]).getValue().toString());
        // Set button text
        if (bFrom) {
          ((JButton) widgets[row][1]).setText(sText);
        } else {
          ((JButton) widgets[row][2]).setText(sText);
        }
        // set selected genre in transition object
        if (bFrom) {
          transition.setFrom(new Ambience(Long.toString(System.currentTimeMillis()), "", genres));
        } else {
          transition.setTo(new Ambience(Long.toString(System.currentTimeMillis()), "", genres));
        }
        // check if the transaction is fully selected now
        if (transition.getFrom().getGenres().size() > 0
            && transition.getTo().getGenres().size() > 0) {
          // Make sure current delete button is now enabled
          ((JButton) widgets[row][0]).setEnabled(true);

          // Reset wizard error message
          setProblem(null);

          // Fill wizard data
          data.put(KEY_TRANSITIONS, getCleanedTransitions());

          // create a new void proportion if needed
          if (!containsVoidItem()) {
            // we duplicate the nb for new row
            alTransitions.add(new Transition(nb));
          }

          // Refresh screen to add a new void row
          refreshScreen();
        }
      }
    }

    /**
     * Contains void item.
     * 
     * @return whether a void item already exist (used to avoid creating several
     * void items)
     */
    private boolean containsVoidItem() {
      for (JComponent[] element : widgets) {
        JButton jbFrom = (JButton) element[1];
        JButton jbTo = (JButton) element[2];
        if (jbFrom.getText().equals("") || jbTo.getText().equals("")) {
          return true;
        }
      }
      return false;
    }

    /**
     * Refresh panel.
     */
    private void refreshScreen() {
      removeAll();
      // refresh panel
      add(getTransitionsPanel(), "grow");
      revalidate();
      repaint();
    }

  }

  /**
   * Proportion panel.
   */
  public static class ProportionsPanel extends Screen {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** All dynamic widgets. */
    JComponent[][] widgets;

    /** Proportions*. */
    List<Proportion> proportions;

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("DigitalDJWizard.50");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("DigitalDJWizard.29");
    }

    /**
     * Create panel UI.
     */

    @Override
    @SuppressWarnings("unchecked")
    public void initUI() {
      if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))) {
        DigitalDJ dj = (DigitalDJ) data.get(KEY_CHANGE);
        proportions = (List<Proportion>) ((ArrayList<Proportion>) ((ProportionDigitalDJ) dj)
            .getProportions()).clone();
        data.put(KEY_PROPORTIONS, getCleanedProportions());
        proportions.add(new Proportion()); // add a void item
      } else {
        proportions = new ArrayList<Proportion>(10);
        proportions.add(new Proportion()); // add a void item
        setProblem(Messages.getString("DigitalDJWizard.30"));
      }
      setCanFinish(true);

      // set layout
      setLayout(new MigLayout("insets 10,gapx 5", "[grow]"));
      add(getProportionsPanel(), "grow");
    }

    /**
     * Gets the cleaned proportions.
     * 
     * @return Filled proportions only
     */
    private List<Proportion> getCleanedProportions() {
      List<Proportion> out = new ArrayList<Proportion>(proportions.size());
      for (Proportion proportion : proportions) {
        if (proportion.getGenres() != null && proportion.getGenres().size() > 0) {
          out.add(proportion);
        }
      }
      return out;
    }

    /**
     * Gets the proportions panel.
     * 
     * @return a panel containing all proportions
     */
    private JScrollPane getProportionsPanel() {
      widgets = new JComponent[proportions.size()][3];
      JPanel out = new JPanel();
      // Delete|Genre list|proportion in %
      // now add all known proportions
      for (int index = 0; index < proportions.size(); index++) {
        // Delete button
        JButton jbDelete = new JButton(IconLoader.getIcon(JajukIcons.DELETE));
        jbDelete.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent ae) {
            proportions.remove(getWidgetIndex(widgets, (JComponent) ae.getSource()));
            data.put(KEY_PROPORTIONS, getCleanedProportions());
            refreshScreen();
          }
        });
        // cannot delete if void selection
        if (proportions.size() == 1) {
          jbDelete.setEnabled(false);
        }
        jbDelete.setToolTipText(Messages.getString("DigitalDJWizard.21"));
        widgets[index][0] = jbDelete;
        // genre list
        JButton jbGenre = new JButton(IconLoader.getIcon(JajukIcons.LIST));
        Proportion proportion = proportions.get(index);
        if (proportion.getGenres() != null) {
          jbGenre.setText(proportion.getGenresDesc());
          jbGenre.setToolTipText(proportion.getGenresDesc());
        }
        jbGenre.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent ae) {
            int row = getWidgetIndex(widgets, (JComponent) ae.getSource());
            addGenre(row);
          }
        });
        jbGenre.setToolTipText(Messages.getString("DigitalDJWizard.27"));
        widgets[index][1] = jbGenre;
        // Proportion
        JSpinner jsNb = new JSpinner(new SpinnerNumberModel(
            (int) (proportion.getProportion() * 100), 1, 100, 1));
        jsNb.addChangeListener(new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent ce) {
            if (getTotalValue() > 100) {
              setProblem(Messages.getString("DigitalDJWizard.59"));
              return;
            } else {
              setProblem(null);
            }
            int row = getWidgetIndex(widgets, (JComponent) ce.getSource());
            int nb = Integer.parseInt(((JSpinner) ce.getSource()).getValue().toString());
            Proportion proportion = proportions.get(row);
            proportion.setProportion(((float) nb) / 100);
          }
        });
        jsNb.setToolTipText(Messages.getString("DigitalDJWizard.28"));
        widgets[index][2] = jsNb;
      }
      // Create layout
      out.setLayout(new MigLayout("insets 5,gapx 10,gapy 10", "[][530!][]"));
      // Create header
      JLabel jlHeader1 = new JLabel(Messages.getString("DigitalDJWizard.27"));
      jlHeader1.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      JLabel jlHeader2 = new JLabel(Messages.getString("DigitalDJWizard.28"));
      jlHeader2.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      out.add(jlHeader1, "cell 1 0, center");
      out.add(jlHeader2, "cell 2 0, center,wrap");
      // Add widgets
      for (int i = 0; i < widgets.length; i++) {
        out.add(widgets[i][0], "left");
        out.add(widgets[i][1], "grow,width ::530");
        out.add(widgets[i][2], "wrap");
      }
      // Display an error message if sum of proportion is > 100%
      if (getTotalValue() > 100) {
        setProblem(Messages.getString("DigitalDJWizard.59"));
      }
      JScrollPane jsp = new JScrollPane(out);
      jsp.setBorder(null);
      return jsp;
    }

    /**
     * Gets the total value.
     * 
     * @return Sum of all proportions
     */
    private int getTotalValue() {
      int total = 0;
      for (JComponent[] element : widgets) {
        JSpinner jsp = (JSpinner) element[2];
        // Only filled proportions are token into account
        JButton jb = (JButton) element[1];
        if (jb.getText() == null || jb.getText().equals("")) {
          continue;
        }
        total += Integer.parseInt(jsp.getValue().toString());
      }
      return total;
    }

    /**
     * Add a genre to a proportion.
     * 
     * @param row row
     */
    private void addGenre(int row) {
      synchronized (GenreManager.getInstance()) {
        Proportion proportion = proportions.get(row);
        // create list of genres used in existing transitions
        Set<Genre> disabledGenres = new HashSet<Genre>();
        for (int i = 0; i < proportions.size(); i++) {
          if (i != row) { // do not exclude current proportion that
            // will be selected
            disabledGenres.addAll(proportions.get(i).getGenres());
          }
        }
        GenresSelectionDialog dialog = new GenresSelectionDialog(disabledGenres);
        dialog.setSelection(proportion.getGenres());
        dialog.setVisible(true);
        Set<Genre> genres = dialog.getSelectedGenres();
        // check if at least one genre has been selected
        if (genres.size() == 0) {
          return;
        }
        // reset genres
        proportion.setGenre(new Ambience());
        String sText = "";
        for (Genre genre : genres) {
          // handle null
          if (genre == null) {
            Log.warn("Could not add genre, got an empty genre from the Wizard Dialog!");
            continue;
          }

          proportion.addGenre(genre);
          sText += genre.getName2() + ',';
        }
        sText = sText.substring(0, sText.length() - 1);
        // Set button text
        ((JButton) widgets[row][1]).setText(sText);
        // check if the proportion is fully selected now
        if (proportion.getGenres().size() > 0) {
          // Make sure current delete button is now enabled
          ((JButton) widgets[row][0]).setEnabled(true);

          // Reset wizard error message
          setProblem(null);

          // Fill wizard data
          data.put(KEY_PROPORTIONS, getCleanedProportions());

          // create a new void proportion if needed
          if (!containsVoidItem()) {
            proportions.add(new Proportion());
          }

          // Refresh screen to add a new void row
          refreshScreen();
        }
      }
    }

    /**
     * Contains void item.
     * 
     * @return whether a void item already exist (used to avoid creating several
     * void items)
     */
    private boolean containsVoidItem() {
      for (JComponent[] element : widgets) {
        JButton jb = (JButton) element[1];
        if (jb.getText().equals("")) {
          return true;
        }
      }
      return false;
    }

    /**
     * Refresh panel.
     */
    private void refreshScreen() {
      removeAll();
      // refresh panel
      add(getProportionsPanel(), "grow");
      revalidate();
      repaint();
    }
  }

  /**
   * Ambience based.
   */
  public static class AmbiencePanel extends Screen implements ActionListener {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** All dynamic widgets. */
    JComponent[][] widgets;

    /** Ambiences*. */
    List<Ambience> ambiences;

    /** DJ*. */
    AmbienceDigitalDJ dj = null;

    /** Selected ambience index. */
    int ambienceIndex = 0;

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("DigitalDJWizard.47");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("DigitalDJWizard.31");
    }

    /**
     * Create panel UI.
     */
    @Override
    public void initUI() {
      // the returned list is sorted by name
      ambiences = AmbienceManager.getInstance().getAmbiences();

      // We need at least one ambience
      if (ambiences.size() == 0) {
        setProblem(Messages.getString("DigitalDJWizard.38"));
      }
      setCanFinish(true);
      // Get DJ
      dj = (AmbienceDigitalDJ) DigitalDJManager.getInstance().getDJByName(
          (String) data.get(KEY_DJ_NAME));

      setLayout(new MigLayout("insets 10,gapx 5", "[grow]"));
      add(getAmbiencesPanel(), "grow");
    }

    /**
     * Gets the ambiences panel.
     * 
     * @return a panel containing all ambiences
     */
    private JScrollPane getAmbiencesPanel() {
      ButtonGroup bg = new ButtonGroup();
      widgets = new JComponent[ambiences.size()][3];
      JPanel out = new JPanel();
      out.setLayout(new MigLayout("insets 0,gapx 10,gapy 10", "[grow]"));
      int index = 0;
      for (Ambience ambience : ambiences) {
        JRadioButton jrb = new JRadioButton(ambience.getName());
        jrb.addActionListener(this);
        bg.add(jrb);
        widgets[index][0] = jrb;
        out.add(jrb, "left gap 5,wrap");
        index++;
      }
      // DJ change, set right ambience
      if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))) {
        DigitalDJ lDJ = (DigitalDJ) data.get(KEY_CHANGE);
        Ambience ambience = ((AmbienceDigitalDJ) lDJ).getAmbience();
        index = 0;
        for (Ambience a : ambiences) {
          if (a.equals(ambience)) {
            JRadioButton jrb = (JRadioButton) widgets[index][0];
            jrb.doClick();// select right ambience, it will set
            // right value into data
            break;
          }
          index++;
        }
      } else {
        // select first ambience found
        JRadioButton jrb = (JRadioButton) widgets[0][0];
        jrb.doClick();
      }
      JScrollPane jsp = new JScrollPane(out);
      jsp.setBorder(null);
      return jsp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      int row = getWidgetIndex(widgets, (JComponent) e.getSource());
      data.put(KEY_AMBIENCE, ambiences.get(row));
      setProblem(null);
    }

  }

  /**
   * Gets the widget index.
   *
   * @param widgets DOCUMENT_ME
   * @param widget DOCUMENT_ME
   * @return index of a given widget row in the widget table
   */
  private static int getWidgetIndex(JComponent[][] widgets, JComponent widget) {
    for (int row = 0; row < widgets.length; row++) {
      for (int col = 0; col < widgets[0].length; col++) {
        if (widget.equals(widgets[row][col])) {
          return row;
        }
      }
    }
    return -1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.wizard.Wizard#getPreviousScreen(java.lang.Class)
   */
  @Override
  public Class<? extends org.qdwizard.Screen> getPreviousScreen(
      Class<? extends org.qdwizard.Screen> screen) {
    if (ActionSelectionPanel.class.equals(getCurrentScreen())) {
      return null;
    } else if (TypeSelectionPanel.class.equals(getCurrentScreen())) {
      return ActionSelectionPanel.class;
    } else if (GeneralOptionsPanel.class.equals(getCurrentScreen())) {
      if (ActionSelectionPanel.ACTION_CREATION.equals(data.get(KEY_ACTION))) {
        return TypeSelectionPanel.class;
      } else {
        return ChangePanel.class;
      }
    } else if (TransitionsPanel.class.equals(getCurrentScreen())
        || ProportionsPanel.class.equals(getCurrentScreen())
        || AmbiencePanel.class.equals(getCurrentScreen())) {
      return GeneralOptionsPanel.class;
    } else if (RemovePanel.class.equals(getCurrentScreen())) {
      return ActionSelectionPanel.class;
    } else if (ChangePanel.class.equals(getCurrentScreen())) {
      return ActionSelectionPanel.class;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.wizard.Wizard#getNextScreen(java.lang.Class)
   */
  @Override
  public Class<? extends org.qdwizard.Screen> getNextScreen(
      Class<? extends org.qdwizard.Screen> screen) {
    if (ActionSelectionPanel.class.equals(getCurrentScreen())) {
      String sAction = (String) data.get(KEY_ACTION);
      if (ActionSelectionPanel.ACTION_CREATION.equals(sAction)) {
        return TypeSelectionPanel.class;
      } else if (ActionSelectionPanel.ACTION_CHANGE.equals(sAction)) {
        return ChangePanel.class;
      } else if (ActionSelectionPanel.ACTION_DELETE.equals(sAction)) {
        return RemovePanel.class;
      }
    } else if (TypeSelectionPanel.class.equals(getCurrentScreen())) {
      return GeneralOptionsPanel.class;
    } else if (GeneralOptionsPanel.class.equals(getCurrentScreen())) {
      String sType = (String) data.get(KEY_DJ_TYPE);
      if (TypeSelectionPanel.DJ_TYPE_AMBIENCE.equals(sType)) {
        return AmbiencePanel.class;
      } else if (TypeSelectionPanel.DJ_TYPE_PROPORTION.equals(sType)) {
        return ProportionsPanel.class;
      } else if (TypeSelectionPanel.DJ_TYPE_TRANSITION.equals(sType)) {
        return TransitionsPanel.class;
      }
    } else if (RemovePanel.class.equals(getCurrentScreen())) {
      return null;
    } else if (ChangePanel.class.equals(getCurrentScreen())) {
      return GeneralOptionsPanel.class;
    }
    return null;
  }

  /**
   * Instantiates a new digital dj wizard.
   */
  public DigitalDJWizard() {
    super(Messages.getString("DigitalDJWizard.4"), ActionSelectionPanel.class, null,
        JajukMainWindow.getInstance(), LocaleManager.getLocale());
    super.setHeaderIcon(IconLoader.getIcon(JajukIcons.DIGITAL_DJ));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.wizard.Wizard#finish()
   */

  @SuppressWarnings("unchecked")
  @Override
  public void finish() {
    DigitalDJ dj = null;
    String sAction = (String) data.get(KEY_ACTION);
    if (ActionSelectionPanel.ACTION_DELETE.equals(sAction)) {
      try {
        DigitalDJManager.getInstance().remove((DigitalDJ) data.get(KEY_REMOVE));
      } catch (IOException e) {
        Log.error(e);
      }
    } else if (ActionSelectionPanel.ACTION_CREATION.equals(sAction)) {
      String sType = (String) data.get(KEY_DJ_TYPE);
      // create a unique ID for this DJ, simply use current time in ms
      String sID = Long.toString(System.currentTimeMillis());
      if (TypeSelectionPanel.DJ_TYPE_AMBIENCE.equals(sType)) {
        Ambience ambience = (Ambience) data.get(KEY_AMBIENCE);
        dj = new AmbienceDigitalDJ(sID);
        ((AmbienceDigitalDJ) dj).setAmbience(ambience);
      } else if (TypeSelectionPanel.DJ_TYPE_PROPORTION.equals(sType)) {
        dj = new ProportionDigitalDJ(sID);
        List<Proportion> proportions = (List<Proportion>) data.get(KEY_PROPORTIONS);
        ((ProportionDigitalDJ) dj).setProportions(proportions);
      } else if (TypeSelectionPanel.DJ_TYPE_TRANSITION.equals(sType)) {
        List<Transition> transitions = (List<Transition>) data.get(KEY_TRANSITIONS);
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
    } else if (ActionSelectionPanel.ACTION_CHANGE.equals(sAction)) {
      String sType = (String) data.get(KEY_DJ_TYPE);
      dj = (DigitalDJ) data.get(KEY_CHANGE);
      if (TypeSelectionPanel.DJ_TYPE_AMBIENCE.equals(sType)) {
        Ambience ambience = (Ambience) data.get(KEY_AMBIENCE);
        ((AmbienceDigitalDJ) dj).setAmbience(ambience);
      } else if (TypeSelectionPanel.DJ_TYPE_PROPORTION.equals(sType)) {
        List<Proportion> proportions = (List<Proportion>) data.get(KEY_PROPORTIONS);
        ((ProportionDigitalDJ) dj).setProportions(proportions);
      } else if (TypeSelectionPanel.DJ_TYPE_TRANSITION.equals(sType)) {
        List<Transition> transitions = (List<Transition>) data.get(KEY_TRANSITIONS);
        ((TransitionDigitalDJ) dj).setTransitions(transitions);
      }
      setProperties(dj);
      // commit the DJ right now
      DigitalDJManager.commit(dj);
    }
    // Refresh command panel (useful for ie if DJ names changed)
    ObservationManager.notify(new JajukEvent(JajukEvents.DJS_CHANGE));
  }

  /**
   * Store the properties from the Wizard to the specified DJ.
   * 
   * @param dj The DJ to populate.
   */
  private void setProperties(DigitalDJ dj) {
    String sName = (String) data.get(KEY_DJ_NAME);
    int iFadeDuration = (Integer) data.get(KEY_FADE_DURATION);
    int iRateLevel = (Integer) data.get(KEY_RATINGS_LEVEL);
    boolean bUnicity = (Boolean) data.get(KEY_UNICITY);
    int iMaxTracks = (Integer) data.get(KEY_MAX_TRACKS);
    dj.setName(sName);
    dj.setFadingDuration(iFadeDuration);
    dj.setRatingLevel(iRateLevel);
    dj.setTrackUnicity(bUnicity);
    dj.setMaxTracks(iMaxTracks);
  }
}
