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
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.Proportion;
import org.jajuk.services.dj.ProportionDigitalDJ;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.wizard.GenresSelectionDialog;
import org.jajuk.ui.wizard.digital_dj.DigitalDJWizard.Variable;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;
import org.qdwizard.Screen;

/**
 * Proportion panel.
 */
public class ProportionsScreen extends Screen {
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
    if (ActionSelectionScreen.ACTION_CHANGE.equals(data.get(Variable.ACTION))) {
      DigitalDJ dj = (DigitalDJ) data.get(Variable.CHANGE);
      proportions = (List<Proportion>) ((ArrayList<Proportion>) ((ProportionDigitalDJ) dj)
          .getProportions()).clone();
      data.put(Variable.PROPORTIONS, getCleanedProportions());
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
          proportions.remove(DigitalDJWizard.getWidgetIndex(widgets, (JComponent) ae.getSource()));
          data.put(Variable.PROPORTIONS, getCleanedProportions());
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
          int row = DigitalDJWizard.getWidgetIndex(widgets, (JComponent) ae.getSource());
          addGenre(row);
        }
      });
      jbGenre.setToolTipText(Messages.getString("DigitalDJWizard.27"));
      widgets[index][1] = jbGenre;
      // Proportion
      JSpinner jsNb = new JSpinner(new SpinnerNumberModel((int) (proportion.getProportion() * 100),
          1, 100, 1));
      jsNb.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent ce) {
          if (getTotalValue() > 100) {
            setProblem(Messages.getString("DigitalDJWizard.59"));
            return;
          } else {
            setProblem(null);
          }
          int row = DigitalDJWizard.getWidgetIndex(widgets, (JComponent) ce.getSource());
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
        data.put(Variable.PROPORTIONS, getCleanedProportions());
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
