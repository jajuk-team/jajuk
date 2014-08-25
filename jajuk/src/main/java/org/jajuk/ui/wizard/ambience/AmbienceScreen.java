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
package org.jajuk.ui.wizard.ambience;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Genre;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceDigitalDJ;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.wizard.GenresSelectionDialog;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.qdwizard.Screen;

public class AmbienceScreen extends Screen implements ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** All dynamic widgets. */
  private JComponent[][] widgets;
  private JButton jbNew;
  private JButton jbDelete;
  private JButton jbDefaults;
  /** DJ*. */
  private AmbienceDigitalDJ dj = null;
  /** Selected ambience index. */
  private int ambienceIndex = 0;

  @Override
  public void actionPerformed(final ActionEvent ae) {
    if (ae.getSource() == jbNew) {
      // create a void ambience
      AmbienceWizard.ambiences.add(new Ambience(Long.toString(System.currentTimeMillis()), ""));
      Collections.sort(AmbienceWizard.ambiences);
      // refresh screen
      refreshScreen();
      // select new row
      final JRadioButton jrb = (JRadioButton) widgets[AmbienceWizard.ambiences.size() - 1][0];
      jrb.setSelected(true);
      ambienceIndex = AmbienceWizard.ambiences.size() - 1;
      setProblem(Messages.getString("DigitalDJWizard.39"));
      jbNew.setEnabled(false);
      jbDelete.setEnabled(true);
      final JTextField jtf = (JTextField) widgets[ambienceIndex][1];
      jtf.requestFocusInWindow();
    } else if (ae.getSource() == jbDelete) {
      final Ambience ambience = AmbienceWizard.ambiences.get(ambienceIndex);
      AmbienceWizard.ambiences.remove(ambience);
      AmbienceManager.getInstance().removeAmbience(ambience.getID());
      if (AmbienceManager.getInstance().getAmbiences().size() == 0) {
        jbDelete.setEnabled(false);
      }
      if (ambienceIndex > 0) {
        ambienceIndex--;
        final JRadioButton jrb = (JRadioButton) widgets[ambienceIndex][0];
        jrb.setSelected(true);
      }
      // refresh screen
      refreshScreen();
    } else if (ae.getSource() == jbDefaults) {
      AmbienceManager.getInstance().createDefaultAmbiences();
      AmbienceWizard.ambiences = new ArrayList<Ambience>(AmbienceManager.getInstance()
          .getAmbiences());
      Collections.sort(AmbienceWizard.ambiences);
      // refresh screen
      refreshScreen();
    }
    // in all cases, notify command panel
    ObservationManager.notify(new JajukEvent(JajukEvents.AMBIENCES_CHANGE));
  }

  /**
   * Gets the widget index.
   *
   * @param widgets 
   * @param widget 
   * @return index of a given widget row in the widget table
   */
  private int getWidgetIndex(final JComponent[][] widgets, final JComponent widget) {
    for (int row = 0; row < widgets.length; row++) {
      for (int col = 0; col < widgets[0].length; col++) {
        if (widget.equals(widgets[row][col])) {
          return row;
        }
      }
    }
    return -1;
  }

  /**
   * Add a genre to a proportion.
   * 
   * @param row row
   */
  private void addGenre(final int row) {
    final Ambience ambience = AmbienceWizard.ambiences.get(row);
    // create list of genres used in current selection
    final GenresSelectionDialog dialog = new GenresSelectionDialog(null);
    dialog.setSelection(ambience.getGenres());
    dialog.setVisible(true);
    final Set<Genre> genres = dialog.getSelectedGenres();
    // check if at least one genre has been selected
    if (genres.size() == 0) {
      return;
    }
    StringBuilder sText = new StringBuilder();
    // reset old genres
    ambience.setGenres(new HashSet<Genre>(10));
    for (final Genre genre : genres) {
      ambience.addGenre(genre);
      sText.append(genre.getName2()).append(',');
    }
    sText.deleteCharAt(sText.length() - 1);
    // Set button text
    ((JButton) widgets[row][2]).setText(sText.toString());
    // if we have ambience name and some genres, register the
    // ambience
    if ((ambience.getName().length() > 0) && (ambience.getGenres().size() > 0)) {
      // no more error message if at least one ambience
      setProblem(null);
      jbNew.setEnabled(true);
    }
  }

  @Override
  public String getDescription() {
    return Messages.getString("DigitalDJWizard.47");
  }

  @Override
  public String getName() {
    return Messages.getString("DigitalDJWizard.57");
  }

  /**
   * Gets the panel.
   * 
   * @return a panel containing all items
   */
  private JScrollPane getPanel() {
    widgets = new JComponent[AmbienceWizard.ambiences.size()][3];
    final JPanel out = new JPanel();
    // Delete|Genre name|genres list
    final ButtonGroup group = new ButtonGroup();
    // now add all ambiences
    for (int index = 0; index < AmbienceWizard.ambiences.size(); index++) {
      // Ambience name
      final JTextField jtfName = new JTextField();
      jtfName.setText(AmbienceWizard.ambiences.get(index).getName());
      jtfName.addCaretListener(new CaretListener() {
        @Override
        public void caretUpdate(final CaretEvent arg0) {
          final int index = getWidgetIndex(widgets, (JComponent) arg0.getSource());
          final String s = jtfName.getText();
          // Check this name is not already token
          for (int i = 0; i < widgets.length; i++) {
            if (i == index) {
              continue;
            }
            final JTextField jtf = (JTextField) widgets[i][1];
            if (jtf.getText().equals(s)) {
              setProblem(Messages.getString("DigitalDJWizard.60"));
              return;
            }
          }
          // reset previous problems
          if ((s.length() == 0) || (((JButton) widgets[index][2]).getText().length() == 0)) {
            setProblem(Messages.getString("DigitalDJWizard.39"));
          } else {
            setProblem(null);
          }
          final JButton jb = (JButton) widgets[index][2];
          final Ambience ambience = AmbienceWizard.ambiences.get(index);
          ambience.setName(s);
          jb.setEnabled(s.length() > 0);
        }
      });
      jtfName.setToolTipText(Messages.getString("DigitalDJWizard.36"));
      widgets[index][1] = jtfName;
      // radio button
      final JRadioButton jrbAmbience = new JRadioButton();
      group.add(jrbAmbience);
      jrbAmbience.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent ae) {
          ((JTextField) widgets[getWidgetIndex(widgets, jrbAmbience)][1]).getText();
          ambienceIndex = getWidgetIndex(widgets, jrbAmbience);
        }
      });
      widgets[index][0] = jrbAmbience;
      if (index == ambienceIndex) {
        jrbAmbience.setSelected(true);
      }
      final Ambience ambience = AmbienceWizard.ambiences.get(index);
      // genre list
      final JButton jbGenre = new JButton(IconLoader.getIcon(JajukIcons.GENRE));
      if (ambience.getName().length() == 0) {
        jbGenre.setEnabled(false);
      }
      if ((ambience.getGenres() != null) && (ambience.getGenres().size() > 0)) {
        jbGenre.setText(ambience.getGenresDesc());
        jbGenre.setToolTipText(ambience.getGenresDesc());
      }
      jbGenre.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent ae) {
          final int row = getWidgetIndex(widgets, (JComponent) ae.getSource());
          addGenre(row);
          // refresh ambience (force an action event)
          final JRadioButton jrb = (JRadioButton) widgets[row][0];
          jrb.doClick();
        }
      });
      jbGenre.setToolTipText(Messages.getString("DigitalDJWizard.27"));
      widgets[index][2] = jbGenre;
    }
    // Create layout
    out.setLayout(new MigLayout("insets 5,gapx 5", "[][][grow]"));
    // Create header
    final JLabel jlHeader1 = new JLabel(Messages.getString("DigitalDJWizard.37"));
    jlHeader1.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
    final JLabel jlHeader2 = new JLabel(Messages.getString("DigitalDJWizard.27"));
    jlHeader2.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
    out.add(jlHeader1, "center,span 2");
    out.add(jlHeader2, "center,wrap");
    // Add widgets
    for (int i = 0; i < widgets.length; i++) {
      out.add(widgets[i][0], "grow,center,width 25!");
      out.add(widgets[i][1], "grow,center,width 120!");
      out.add(widgets[i][2], "center,grow,wrap,width 270:270");
    }
    final JScrollPane jsp = new JScrollPane(out);
    // select first ambiance found
    if (AmbienceWizard.ambiences.size() > 0) {
      final JRadioButton jrb = (JRadioButton) widgets[0][0];
      jrb.doClick();
    }
    return jsp;
  }

  /**
   * Create panel UI.
   */
  @Override
  public void initUI() {
    AmbienceWizard.ambiences = new ArrayList<Ambience>(AmbienceManager.getInstance().getAmbiences());
    Collections.sort(AmbienceWizard.ambiences);
    setCanFinish(true);
    jbNew = new JButton(Messages.getString("DigitalDJWizard.32"),
        IconLoader.getIcon(JajukIcons.NEW));
    jbNew.addActionListener(this);
    jbNew.setToolTipText(Messages.getString("DigitalDJWizard.33"));
    jbDelete = new JButton(Messages.getString("DigitalDJWizard.34"),
        IconLoader.getIcon(JajukIcons.DELETE));
    jbDelete.addActionListener(this);
    jbDelete.setToolTipText(Messages.getString("DigitalDJWizard.35"));
    jbDefaults = new JButton(Messages.getString("DigitalDJWizard.62"),
        IconLoader.getIcon(JajukIcons.DEFAULTS));
    jbDefaults.addActionListener(this);
    jbDefaults.setToolTipText(Messages.getString("DigitalDJWizard.63"));
    // Add items
    refreshScreen();
  }

  /**
   * Refresh panel.
   */
  private void refreshScreen() {
    setLayout(new MigLayout("insets 5,gapy 15", "[center,grow]"));
    removeAll();
    // refresh panel
    add(getPanel(), "grow,wrap");
    add(jbNew, "split 3");
    add(jbDelete);
    add(jbDefaults);
    revalidate();
    repaint();
  }
}
