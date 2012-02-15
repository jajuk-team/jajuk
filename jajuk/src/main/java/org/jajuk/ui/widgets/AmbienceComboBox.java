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
package org.jajuk.ui.widgets;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Ambience selection combo box.
 */
public class AmbienceComboBox extends SteppedComboBox {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** An instance of the ambience combo listener. */
  private AmbienceListener ambienceListener;

  /**
   * Ambience combo listener.
   */
  private class AmbienceListener implements ActionListener {

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
      // Ambience Configuration
      if (getSelectedIndex() == 0) {
        // display the wizard
        try {
          ActionManager.getAction(JajukActions.CONFIGURE_AMBIENCES).perform(null);
        } catch (Exception e) {
          Log.error(e);
        }
        // Reset combo to last selected item. We do this to avoid to select the
        // "0" item that is not an ambience
        removeActionListener(ambienceListener);
        Ambience defaultAmbience = AmbienceManager.getInstance().getAmbience(
            Conf.getString(Const.CONF_DEFAULT_AMBIENCE));
        if (defaultAmbience != null) {
          for (int i = 0; i < getItemCount(); i++) {
            if (((JLabel) getItemAt(i)).getText().equals(defaultAmbience.getName())) {
              setSelectedIndex(i);
              break;
            }
          }
        } else {
          setSelectedIndex(1);
        }
        addActionListener(ambienceListener);
      }
      // Selected 'Any" ambience
      else if (getSelectedIndex() == 1) {
        // reset default ambience
        Conf.setProperty(Const.CONF_DEFAULT_AMBIENCE, "");
        ObservationManager.notify(new JajukEvent(JajukEvents.AMBIENCES_SELECTION_CHANGE));
      } else {// Selected an ambience
        Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
            ((JLabel) getSelectedItem()).getText());
        Conf.setProperty(Const.CONF_DEFAULT_AMBIENCE, ambience.getID());
        ObservationManager.notify(new JajukEvent(JajukEvents.AMBIENCES_SELECTION_CHANGE));
      }
    }
  }

  /**
   * Populate ambiences combo.
   */
  void populateAmbiences() {
    removeActionListener(ambienceListener);
    ItemListener[] il = getItemListeners();
    for (ItemListener element : il) {
      removeItemListener(element);
    }
    removeAllItems();
    addItem(new JLabel(Messages.getString("CommandJPanel.19"),
        IconLoader.getIcon(JajukIcons.CONFIGURATION), SwingConstants.LEFT));
    addItem(new JLabel("<html><i>" + Messages.getString("DigitalDJWizard.64") + "</i></html>",
        IconLoader.getIcon(JajukIcons.GENRE), SwingConstants.LEFT));
    // Add available ambiences
    for (final Ambience ambience : AmbienceManager.getInstance().getAmbiences()) {
      addItem(new JLabel(ambience.getName(), IconLoader.getIcon(JajukIcons.GENRE),
          SwingConstants.LEFT));
    }
    // Select right item
    Ambience defaultAmbience = AmbienceManager.getInstance().getAmbience(
        Conf.getString(Const.CONF_DEFAULT_AMBIENCE));
    if (defaultAmbience != null) {
      for (int i = 0; i < getItemCount(); i++) {
        if (((JLabel) getItemAt(i)).getText().equals(defaultAmbience.getName())) {
          setSelectedIndex(i);
          break;
        }
      }
    } else {
      // or "any" ambience
      setSelectedIndex(1);
    }
    addActionListener(ambienceListener);
  }

  /**
   * Instantiates a new ambience combo box.
   */
  public AmbienceComboBox() {
    super();
    // Set size of the popup
    int popupWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 4);
    setPopupWidth(popupWidth);
    setRenderer(new BasicComboBoxRenderer() {
      private static final long serialVersionUID = -6943363556191659895L;

      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        JLabel jl = (JLabel) value;
        setIcon(jl.getIcon());
        setText(jl.getText());
        return this;
      }
    });
    setToolTipText(Messages.getString("DigitalDJWizard.66"));
    populateAmbiences();
    ambienceListener = new AmbienceListener();
    addActionListener(ambienceListener);
  }

}
