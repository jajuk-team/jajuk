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
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.jajuk.services.dj.AmbienceDigitalDJ;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.dj.ProportionDigitalDJ;
import org.jajuk.services.dj.TransitionDigitalDJ;
import org.jajuk.ui.wizard.digital_dj.DigitalDJWizard.Variable;
import org.jajuk.util.Messages;
import org.qdwizard.Screen;

import net.miginfocom.swing.MigLayout;

/**
 * DJ Selection for change.
 */
public class ChangeScreen extends Screen implements ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  JComponent[][] widgets;
  ButtonGroup bgDJS;
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

  @Override
  public void actionPerformed(ActionEvent e) {
    int row = DigitalDJWizard.getWidgetIndex(widgets, (JComponent) e.getSource());
    // set DJ type useful for screen choice
    DigitalDJ dj = djs.get(row);
    data.put(Variable.CHANGE, dj);
    if (dj instanceof AmbienceDigitalDJ) {
      data.put(Variable.DJ_TYPE, TypeSelectionScreen.DJ_TYPE_AMBIENCE);
    }
    if (dj instanceof ProportionDigitalDJ) {
      data.put(Variable.DJ_TYPE, TypeSelectionScreen.DJ_TYPE_PROPORTION);
    }
    if (dj instanceof TransitionDigitalDJ) {
      data.put(Variable.DJ_TYPE, TypeSelectionScreen.DJ_TYPE_TRANSITION);
    }
    setProblem(null);
  }

  @Override
  public String getDescription() {
    return Messages.getString("DigitalDJWizard.44");
  }

  @Override
  public String getName() {
    return Messages.getString("DigitalDJWizard.43");
  }
}
