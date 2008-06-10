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
 *  $Revision$
 */

package org.jajuk.ui.widgets;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.util.Messages;

/**
 * Allow a user to select a list of styles
 */
public class StylesSelectionDialog extends JajukJDialog implements ActionListener {

  private static final long serialVersionUID = 1L;

  JComboBox jcbAmbiences;

  JList jlist;

  OKCancelPanel okc;

  HashSet<Style> selectedStyles;

  HashSet<Style> disabledStyles;

  Vector<String> list;

  /**
   * @throws HeadlessException
   */
  @SuppressWarnings("unchecked")
  public StylesSelectionDialog(HashSet disabledStyles) throws HeadlessException {
    super();
    this.selectedStyles = new HashSet<Style>();
    this.disabledStyles = disabledStyles;
    setLocationByPlatform(true);
    setTitle(Messages.getString("DigitalDJWizard.14"));
    setModal(true);
    setAlwaysOnTop(true);
    initUI();
    pack();
  }

  /**
   * Set selected item
   * 
   * @param selection
   *          or null to void it
   */
  public void setSelection(Set<Style> selection) {
    if (selection != null) {
      int[] indices = new int[selection.size()];
      // reset all indices to -1 to avoid selecting zero th item
      for (int i = 0; i < selection.size(); i++) {
        indices[i] = -1;
      }
      // find all matching items
      int comp = 0;
      for (int i = 0; i < jlist.getModel().getSize(); i++) {
        String modelStyle = (String) jlist.getModel().getElementAt(i);
        for (Style style : selection) {
          if (style.getName2().equals(modelStyle)) {
            indices[comp] = i;
            comp++;
          }
        }
      }
      // select item in the list
      jlist.setSelectedIndices(indices);
    }
  }

  /**
   * 
   * @return selected styles
   */
  public HashSet<Style> getSelectedStyles() {
    return selectedStyles;
  }

  @SuppressWarnings("unchecked")
  private void initUI() {
    list = (Vector) StyleManager.getInstance().getStylesList().clone();
    // remove disabled items
    if (disabledStyles != null) {
      Iterator it = list.iterator();
      while (it.hasNext()) {
        String testedStyle = (String) it.next();
        for (Style disabledStyle : disabledStyles) {
          if (disabledStyle.getName2().equals(testedStyle)) {
            it.remove();
          }
        }
      }
    }
    // main part of the dialog
    // populate ambience combo
    jcbAmbiences = new JComboBox();
    for (Ambience ambience : AmbienceManager.getInstance().getAmbiences()) {
      jcbAmbiences.addItem(ambience.getName());
    }
    // none ambience selected by default
    jcbAmbiences.setSelectedIndex(-1);
    jcbAmbiences.addActionListener(this);
    JPanel jpAmbiences = new JPanel();
    double[][] layoutCombo = new double[][] { { TableLayout.PREFERRED, 10, TableLayout.FILL },
        { 20 } };
    jpAmbiences.setLayout(new TableLayout(layoutCombo));
    jpAmbiences.add(new JLabel(Messages.getString("DigitalDJWizard.58")), "0,0");
    jpAmbiences.add(jcbAmbiences, "2,0");
    jlist = new JList(list);
    jlist.setLayoutOrientation(JList.VERTICAL_WRAP);
    jlist.setPreferredSize(new Dimension(600, 600));
    jlist.setVisibleRowCount(-1);
    okc = new OKCancelPanel(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == okc.getOKButton()) {
          int[] selection = jlist.getSelectedIndices();
          for (int element : selection) {
            selectedStyles.add(StyleManager.getInstance().getStyleByName(
                (String) jlist.getModel().getElementAt(element)));
          }
        }
        dispose();
      }
    });
    JPanel jp = new JPanel();
    jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
    jp.add(Box.createVerticalStrut(10));
    jp.add(jpAmbiences);
    jp.add(Box.createVerticalStrut(10));
    jp.add(jlist);
    jp.add(okc);
    jp.add(Box.createVerticalStrut(10));
    jp.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    add(new JScrollPane(jp));
    getRootPane().setDefaultButton(okc.getOKButton());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource().equals(jcbAmbiences)) {
      List<Ambience> alAmbiences = new ArrayList<Ambience>(AmbienceManager.getInstance()
          .getAmbiences());
      Ambience ambience = alAmbiences.get(jcbAmbiences.getSelectedIndex());
      // select all styles for this ambience
      setSelection(ambience.getStyles());
    }
  }
}
