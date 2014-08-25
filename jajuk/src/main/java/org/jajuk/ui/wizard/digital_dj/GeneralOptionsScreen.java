/*
 *  Jajuk
 *  Copyright (C) 2003-2014 The Jajuk Team
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
 */
package org.jajuk.ui.wizard.digital_dj;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.ui.helpers.DefaultMouseWheelListener;
import org.jajuk.ui.wizard.digital_dj.DigitalDJWizard.Variable;
import org.jajuk.util.Messages;
import org.qdwizard.Screen;

/**
 * General options panel.
 */
public class GeneralOptionsScreen extends Screen implements ActionListener, CaretListener,
    ChangeListener {
  /** The Constant NO_MAX_TRACKS.   */
  private static final String NO_MAX_TRACKS = "  ";
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  JLabel jlName;
  JTextField jtfName;
  JLabel jlRatingLevel;
  JSlider jsRatingLevel;
  JLabel jlFadeDuration;
  JSlider jsFadeDuration;
  JCheckBox jcbMaxTracks;
  JSlider jsMaxTracks;
  JLabel jnMaxTracks;
  JCheckBox jcbUnicity;

  @Override
  public String getDescription() {
    return Messages.getString("DigitalDJWizard.49");
  }

  @Override
  public String getName() {
    return Messages.getString("DigitalDJWizard.48");
  }

  /**
   * Create panel UI.
   */
  @Override
  public void initUI() {
    if (ActionSelectionScreen.ACTION_CREATION.equals(data.get(Variable.ACTION))) {
      // default values
      data.put(Variable.FADE_DURATION, 10);
      data.put(Variable.RATINGS_LEVEL, 0); // all tracks by default
      data.put(Variable.UNICITY, false);
      data.put(Variable.MAX_TRACKS, -1);
    } else if (ActionSelectionScreen.ACTION_CHANGE.equals(data.get(Variable.ACTION))) {
      // keep existing DJ values
      DigitalDJ dj = (DigitalDJ) data.get(Variable.CHANGE);
      data.put(Variable.FADE_DURATION, dj.getFadingDuration());
      data.put(Variable.RATINGS_LEVEL, dj.getRatingLevel());
      data.put(Variable.UNICITY, dj.isTrackUnicity());
      data.put(Variable.MAX_TRACKS, dj.getMaxTracks());
    }
    jlName = new JLabel(Messages.getString("DigitalDJWizard.6"));
    jtfName = new JTextField();
    jtfName.setToolTipText(Messages.getString("DigitalDJWizard.6"));
    jtfName.addCaretListener(this);
    jtfName.requestFocusInWindow();
    jlRatingLevel = new JLabel(Messages.getString("DigitalDJWizard.8"));
    jlRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.53"));
    jsRatingLevel = new JSlider(0, 4, (Integer) data.get(Variable.RATINGS_LEVEL));
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
    jsFadeDuration = new JSlider(0, 30, (Integer) data.get(Variable.FADE_DURATION));
    jsFadeDuration.addMouseWheelListener(new DefaultMouseWheelListener(jsFadeDuration));
    jsFadeDuration.addChangeListener(this);
    jsFadeDuration.setMajorTickSpacing(10);
    jsFadeDuration.setMinorTickSpacing(1);
    jsFadeDuration.setPaintTicks(true);
    jsFadeDuration.setPaintLabels(true);
    jsFadeDuration.setToolTipText(Messages.getString("DigitalDJWizard.54"));
    // CheckBox for enabling/disabling slider, jsMaxTrack
    int nMaxTracks = (Integer) data.get(Variable.MAX_TRACKS);
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
        (Boolean) data.get(Variable.UNICITY));
    jcbUnicity.setToolTipText(Messages.getString("DigitalDJWizard.55"));
    jcbUnicity.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        data.put(Variable.UNICITY, jcbUnicity.isSelected());
      }
    });
    // DJ change, set default values
    if (ActionSelectionScreen.ACTION_CHANGE.equals(data.get(Variable.ACTION))) {
      DigitalDJ dj = (DigitalDJ) data.get(Variable.CHANGE);
      jtfName.setText(dj.getName());
      jsFadeDuration.setValue((Integer) data.get(Variable.FADE_DURATION));
      jsRatingLevel.setValue((Integer) data.get(Variable.RATINGS_LEVEL));
      jcbUnicity.setSelected((Boolean) data.get(Variable.UNICITY));
      if (((Integer) data.get(Variable.MAX_TRACKS)) != -1) {
        jsMaxTracks.setValue((Integer) data.get(Variable.MAX_TRACKS));
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

  @Override
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == jcbUnicity) {
      data.put(Variable.UNICITY, jcbUnicity.isSelected());
    } else if (ae.getSource() == jcbMaxTracks) {
      jsMaxTracks.setEnabled(jcbMaxTracks.isSelected());
      updateMaxTracks();
    }
  }

  @Override
  public void caretUpdate(CaretEvent ce) {
    if (ce.getSource() == jtfName) {
      data.put(Variable.DJ_NAME, jtfName.getText());
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
        if (ActionSelectionScreen.ACTION_CHANGE.equals(data.get(Variable.ACTION))
            && ((DigitalDJ) data.get(Variable.CHANGE)).getName().equals(sName)) {
          setProblem(null);
          return;
        }
        setProblem(Messages.getString("DigitalDJWizard.42"));
      } else {
        setProblem(null); // no more problem
      }
    }
  }

  @Override
  public void stateChanged(ChangeEvent ie) {
    if (ie.getSource() == jsFadeDuration && !jsFadeDuration.getValueIsAdjusting()) {
      data.put(Variable.FADE_DURATION, jsFadeDuration.getValue());
    } else if (ie.getSource() == jsRatingLevel && !jsRatingLevel.getValueIsAdjusting()) {
      data.put(Variable.RATINGS_LEVEL, jsRatingLevel.getValue());
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
        data.put(Variable.MAX_TRACKS, jsMaxTracks.getValue());
      }
      jnMaxTracks.setText(Integer.toString(jsMaxTracks.getValue()));
    } else {
      if (!jsMaxTracks.getValueIsAdjusting()) {
        data.put(Variable.MAX_TRACKS, -1);
      }
      jnMaxTracks.setText(NO_MAX_TRACKS);
    }
  }
}
