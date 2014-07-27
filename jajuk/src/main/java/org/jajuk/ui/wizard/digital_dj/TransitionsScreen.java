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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.Transition;
import org.jajuk.services.dj.TransitionDigitalDJ;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.wizard.GenresSelectionDialog;
import org.jajuk.ui.wizard.digital_dj.DigitalDJWizard.Variable;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.qdwizard.Screen;

/**
 * Transitions panel.
 */
public class TransitionsScreen extends Screen {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** All dynamic widgets. */
  JComponent[][] widgets;
  /** Transitions*. */
  List<Transition> alTransitions;

  @Override
  public String getDescription() {
    return Messages.getString("DigitalDJWizard.52");
  }

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
    if (ActionSelectionScreen.ACTION_CHANGE.equals(data.get(Variable.ACTION))) {
      TransitionDigitalDJ dj = (TransitionDigitalDJ) data.get(Variable.CHANGE);
      alTransitions = (List<Transition>) ((ArrayList<Transition>) dj.getTransitions()).clone();
      data.put(Variable.TRANSITIONS, getCleanedTransitions());
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
          alTransitions.remove(DigitalDJWizard.getWidgetIndex(widgets, (JComponent) ae.getSource()));
          refreshScreen();
          data.put(Variable.TRANSITIONS, getCleanedTransitions());
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
          int row = DigitalDJWizard.getWidgetIndex(widgets, (JComponent) ae.getSource());
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
          int row = DigitalDJWizard.getWidgetIndex(widgets, (JComponent) ae.getSource());
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
          int row = DigitalDJWizard.getWidgetIndex(widgets, (JComponent) ce.getSource());
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
      if (transition.getFrom().getGenres().size() > 0 && transition.getTo().getGenres().size() > 0) {
        // Make sure current delete button is now enabled
        ((JButton) widgets[row][0]).setEnabled(true);
        // Reset wizard error message
        setProblem(null);
        // Fill wizard data
        data.put(Variable.TRANSITIONS, getCleanedTransitions());
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
