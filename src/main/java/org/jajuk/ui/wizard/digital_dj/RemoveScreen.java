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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.ui.wizard.digital_dj.DigitalDJWizard.Variable;
import org.jajuk.util.Messages;
import org.qdwizard.Screen;

import net.miginfocom.swing.MigLayout;

/**
 * DJ removal.
 */
public class RemoveScreen extends Screen implements ActionListener {
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
    int row = DigitalDJWizard.getWidgetIndex(widgets, (JComponent) e.getSource());
    data.put(Variable.REMOVE, djs.get(row));
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
