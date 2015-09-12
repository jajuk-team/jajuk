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

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.ui.wizard.digital_dj.DigitalDJWizard.Variable;
import org.jajuk.util.Messages;
import org.qdwizard.Screen;

/**
 * DJ type choice.
 */
public class TypeSelectionScreen extends Screen implements ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** Transition DJ code. */
  static final String DJ_TYPE_TRANSITION = "0";
  /** Proportions DJ code. */
  static final String DJ_TYPE_PROPORTION = "1";
  /** Ambience DJ code. */
  static final String DJ_TYPE_AMBIENCE = "2";
  ButtonGroup bgTypes;
  JRadioButton jrbTransitions;
  JRadioButton jrbProp;
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

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == jrbTransitions) {
      data.put(Variable.DJ_TYPE, DJ_TYPE_TRANSITION);
    } else if (e.getSource() == jrbProp) {
      data.put(Variable.DJ_TYPE, DJ_TYPE_PROPORTION);
    } else if (e.getSource() == jrbAmbiance) {
      data.put(Variable.DJ_TYPE, DJ_TYPE_AMBIENCE);
    }
  }

  @Override
  public String getDescription() {
    return Messages.getString("DigitalDJWizard.0");
  }

  @Override
  public String getName() {
    return Messages.getString("DigitalDJWizard.46");
  }
}
