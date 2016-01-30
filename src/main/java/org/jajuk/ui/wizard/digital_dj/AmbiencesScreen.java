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

import net.miginfocom.swing.MigLayout;

import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceDigitalDJ;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.ui.wizard.digital_dj.DigitalDJWizard.Variable;
import org.jajuk.util.Messages;
import org.qdwizard.Screen;

/**
 * Ambience based.
 */
public class AmbiencesScreen extends Screen implements ActionListener {
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

  @Override
  public String getDescription() {
    return Messages.getString("DigitalDJWizard.58");
  }

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
        (String) data.get(Variable.DJ_NAME));
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
    if (ActionSelectionScreen.ACTION_CHANGE.equals(data.get(Variable.ACTION))) {
      DigitalDJ lDJ = (DigitalDJ) data.get(Variable.CHANGE);
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

  @Override
  public void actionPerformed(ActionEvent e) {
    int row = DigitalDJWizard.getWidgetIndex(widgets, (JComponent) e.getSource());
    data.put(Variable.AMBIENCE, ambiences.get(row));
    setProblem(null);
  }
}
