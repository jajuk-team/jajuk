/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.widgets;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * The evaluation toolbar is displayed in command panel and slimbar
 * <p>
 * It contains the ban button to ban the current track<br>
 * and an evaluation combo box to evaluate current track
 * </p>
 * 
 */
public class EvaluationToolbar extends JajukJToolbar {

  private static final long serialVersionUID = 3869208492725759632L;

  JajukButton jbBan;

  JComboBox jcbPreference;

  public EvaluationToolbar() {
    jbBan = new JajukButton(ActionManager.getAction(JajukActions.BAN));
    // Preference combo:
    /*
     * track preference (from -3 to 3: -3: hate, -2=dislike, -1=poor, +1=like,
     * +2=love +3=crazy). The preference is a factor given by the user to
     * increase or decrease a track rate.
     */
    jcbPreference = new JComboBox();
    // Add tooltips on combo items
    jcbPreference.setRenderer(new BasicComboBoxRenderer() {
      private static final long serialVersionUID = -6943363556191659895L;

      @Override
      public Component getListCellRendererComponent(final JList list, final Object value,
          final int index, final boolean isSelected, final boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        switch (index) {
        case 0:
          setToolTipText(Messages.getString("Preference.6"));
          break;
        case 1:
          setToolTipText(Messages.getString("Preference.5"));
          break;
        case 2:
          setToolTipText(Messages.getString("Preference.4"));
          break;
        case 3:
          setToolTipText(Messages.getString("Preference.3"));
          break;
        case 4:
          setToolTipText(Messages.getString("Preference.2"));
          break;
        case 5:
          setToolTipText(Messages.getString("Preference.1"));
          break;
        }
        setBorder(new EmptyBorder(0, 3, 0, 3));
        return this;
      }
    });
    jcbPreference.setMinimumSize(new Dimension(40, 0));
    jcbPreference.setPreferredSize(new Dimension(40, 0));
    jcbPreference.setToolTipText(Messages.getString("Preference.0"));

    jcbPreference.addItem(IconLoader.getIcon(JajukIcons.PREFERENCE_ADORE));
    jcbPreference.addItem(IconLoader.getIcon(JajukIcons.PREFERENCE_LOVE));
    jcbPreference.addItem(IconLoader.getIcon(JajukIcons.PREFERENCE_LIKE));
    jcbPreference.addItem(IconLoader.getIcon(JajukIcons.PREFERENCE_DONTLIKEMUCH));
    jcbPreference.addItem(IconLoader.getIcon(JajukIcons.PREFERENCE_DONTLIKE));
    jcbPreference.addItem(IconLoader.getIcon(JajukIcons.PREFERENCE_HATE));

    add(jbBan);
    add(jcbPreference);
  }

}
