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
 */
package org.jajuk.ui.wizard.digital_dj;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.ui.wizard.digital_dj.DigitalDJWizard.Variable;
import org.jajuk.util.Messages;
import org.qdwizard.ClearPoint;
import org.qdwizard.Screen;

/**
 * Action type (new or alter).
 */
@ClearPoint
public class ActionSelectionScreen extends Screen implements ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** NEW code. */
  public static final String ACTION_CREATION = "0";
  /** CHANGE code. */
  public static final String ACTION_CHANGE = "1";
  /** DELETE code. */
  public static final String ACTION_DELETE = "2";
  ButtonGroup bgActions;
  JRadioButton jrbNew;
  JRadioButton jrbChange;
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

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == jrbNew) {
      data.put(Variable.ACTION, ACTION_CREATION);
    } else if (e.getSource() == jrbChange) {
      data.put(Variable.ACTION, ACTION_CHANGE);
    } else if (e.getSource() == jrbDelete) {
      data.put(Variable.ACTION, ACTION_DELETE);
    }
  }

  @Override
  public String getDescription() {
    return Messages.getString("DigitalDJWizard.16");
  }

  @Override
  public String getName() {
    return Messages.getString("DigitalDJWizard.45");
  }
}
