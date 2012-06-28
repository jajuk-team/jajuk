/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
 *  
 */
package org.jajuk.ui.wizard;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.widgets.OKCancelPanel;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Allow a user to select a list of genres.
 */
public class GenresSelectionDialog extends JajukJDialog implements ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  JComboBox jcbAmbiences;
  JList jlist;
  OKCancelPanel okc;
  Set<Genre> selectedGenres;
  Set<Genre> disabledGenres;
  List<String> list;

  /**
   * The Constructor.
   * 
   * @param disabledGenres 
   */
  public GenresSelectionDialog(Set<Genre> disabledGenres) {
    super();
    this.selectedGenres = new HashSet<Genre>();
    this.disabledGenres = disabledGenres;
    setLocationByPlatform(true);
    setTitle(Messages.getString("DigitalDJWizard.14"));
    setModal(true);
    setAlwaysOnTop(true);
    initUI();
    pack();
  }

  /**
   * Set selected item.
   * 
   * @param selection or null to void it
   */
  public void setSelection(Set<Genre> selection) {
    if (selection != null) {
      int[] indices = new int[selection.size()];
      // reset all indices to -1 to avoid selecting zero th item
      for (int i = 0; i < selection.size(); i++) {
        indices[i] = -1;
      }
      // find all matching items
      int comp = 0;
      for (int i = 0; i < jlist.getModel().getSize(); i++) {
        String modelGenre = (String) jlist.getModel().getElementAt(i);
        for (Genre genre : selection) {
          if (genre.getName2().equals(modelGenre)) {
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
   * Gets the selected genres.
   * 
   * @return selected genres
   */
  public Set<Genre> getSelectedGenres() {
    return selectedGenres;
  }

  /**
   * Inits the ui.
   * 
   */
  @SuppressWarnings("unchecked")
  private void initUI() {
    list = (List<String>) (GenreManager.getInstance().getGenresList()).clone();
    // remove disabled items
    if (disabledGenres != null) {
      Iterator<String> it = list.iterator();
      while (it.hasNext()) {
        String testedGenre = it.next();
        for (Genre disabledGenre : disabledGenres) {
          if (disabledGenre.getName2().equals(testedGenre)) {
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
    setLayout(new MigLayout("insets 15,gapx 10, gapy 15", "[grow]"));
    JLabel jlAmbience = new JLabel(Messages.getString("DigitalDJWizard.58"));
    jlist = new JList(new AbstractListModel() {
      private static final long serialVersionUID = 1L;

      @Override
      public int getSize() {
        return list.size();
      }

      @Override
      public Object getElementAt(int i) {
        return list.get(i);
      }
    });
    jlist.setLayoutOrientation(JList.VERTICAL_WRAP);
    JScrollPane jsp = new JScrollPane(jlist);
    jsp.setPreferredSize(new Dimension(600, 600));
    jlist.setVisibleRowCount(-1);
    okc = new OKCancelPanel(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == okc.getOKButton()) {
          int[] selection = jlist.getSelectedIndices();
          for (int element : selection) {
            String name = (String) jlist.getModel().getElementAt(element);
            Genre genre = GenreManager.getInstance().getGenreByName(name);
            if (genre == null) {
              if (name.equals(Messages.getString(Const.UNKNOWN_GENRE))) {
                Log.warn("Use '" + Const.UNKNOWN_GENRE + "' instead of '" + name);
                selectedGenres.add(GenreManager.getInstance().getGenreByName(Const.UNKNOWN_GENRE));
              } else {
                Log.warn("Could not read genre for name: " + name);
              }
            } else {
              selectedGenres.add(genre);
            }
          }
        }
        dispose();
      }
    });
    add(jlAmbience, "split 2");
    add(jcbAmbiences, "grow,wrap");
    add(jsp, "grow,wrap");
    add(okc, "right,span");
    getRootPane().setDefaultButton(okc.getOKButton());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource().equals(jcbAmbiences)) {
      List<Ambience> alAmbiences = new ArrayList<Ambience>(AmbienceManager.getInstance()
          .getAmbiences());
      Ambience ambience = alAmbiences.get(jcbAmbiences.getSelectedIndex());
      // select all genres for this ambience
      setSelection(ambience.getGenres());
    }
  }
}
