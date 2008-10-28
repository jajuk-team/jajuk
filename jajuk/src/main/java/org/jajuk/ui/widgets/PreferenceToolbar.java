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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * The evaluation toolbar is displayed in command panel and slimbar
 * <p>
 * It contains the ban button to ban the current track<br>
 * and an evaluation combo box to evaluate current track
 * </p>
 * 
 */
public class PreferenceToolbar extends JajukJToolbar implements Observer {

  private static final long serialVersionUID = 3869208492725759632L;

  JajukButton jbBan;

  JComboBox jcbPreference;

  ActionListener listener;

  public PreferenceToolbar() {
    jbBan = new JajukButton(ActionManager.getAction(JajukActions.BAN));
    // Preference combo:
    /*
     * track preference (from -3 to 3: -3: hate, -2=dislike, -1=ok, +1=like,
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
          setToolTipText(Messages.getString("Preference.8"));
          break;
        case 4:
          setToolTipText(Messages.getString("Preference.3"));
          break;
        case 5:
          setToolTipText(Messages.getString("Preference.2"));
          break;
        case 6:
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
    jcbPreference.addItem(IconLoader.getIcon(JajukIcons.PREFERENCE_UNSET));
    jcbPreference.addItem(IconLoader.getIcon(JajukIcons.PREFERENCE_AVERAGE));
    jcbPreference.addItem(IconLoader.getIcon(JajukIcons.PREFERENCE_POOR));
    jcbPreference.addItem(IconLoader.getIcon(JajukIcons.PREFERENCE_HATE));

    // Set default to unset preference if not playing and to current track value if playing
    if (!FIFO.isStopped()) {
      setPreference(FIFO.getCurrentFile().getTrack().getLongValue(Const.XML_TRACK_PREFERENCE));
    } else {
      jcbPreference.setSelectedIndex(3);
    }

    listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        File file = FIFO.getCurrentFile();
        if (file != null) {
          Track track = file.getTrack();
          try {
            track.setPreference(3 - jcbPreference.getSelectedIndex());
          } catch (JajukException e1) {
            Log.error(e1);
            Messages.showErrorMessage(e1.getCode());
          }
        }
        // Force immediate rating refresh (without using the rating manager)
        ObservationManager.notify(new Event(JajukEvents.RATE_CHANGED));
      }
    };

    jcbPreference.addActionListener(listener);
    add(jbBan);
    add(jcbPreference);
    ObservationManager.register(this);
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.RATE_CHANGED);
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    return eventSubjectSet;
  }

  /**
   * Set right combo selection for given selection
   * 
   * @param preference
   */
  public void setPreference(long preference) {
    jcbPreference.removeActionListener(listener);
    jcbPreference.setSelectedIndex(-1 * (int) preference + 3);
    jcbPreference.addActionListener(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(final Event event) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (JajukEvents.RATE_CHANGED.equals(event.getSubject())) {
          setPreference(FIFO.getCurrentFile().getTrack().getLongValue(Const.XML_TRACK_PREFERENCE));
        } else if (JajukEvents.FILE_LAUNCHED.equals(event.getSubject())) {
          // Update evaluation toolbar
          setEnabled(true);
          setPreference(FIFO.getCurrentFile().getTrack().getLongValue(Const.XML_TRACK_PREFERENCE));
        } else if (JajukEvents.ZERO.equals(event.getSubject())
            || JajukEvents.PLAYER_STOP.equals(event.getSubject())) {
          setEnabled(false);
          setPreference(0);
        }
      }
    });
  }
}
