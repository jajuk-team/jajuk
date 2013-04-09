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
 *  
 */
package org.jajuk.ui.widgets;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.bookmark.HistoryItem;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.PlayerStateMediator;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;

/**
 * Ambience + History + search panel used in main jajuk window.
 */
public final class SearchJPanel extends JXPanel implements Observer, ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** Self instance. */
  private static SearchJPanel ijp = new SearchJPanel();
  /** The search box (text field). */
  private SearchBox sbSearch;
  /** the combo-genre history. */
  private SteppedComboBox jcbHistory;
  /** the Ambience selection combo. */
  private AmbienceComboBox ambiencesCombo;

  /**
   * Singleton access.
   * 
   * @return the instance
   */
  public static SearchJPanel getInstance() {
    return ijp;
  }

  // widgets declaration
  /**
   * Instantiates a new search j panel.
   */
  private SearchJPanel() {
    super();
  }

  /**
   * Inits the gui.
   */
  public void initUI() {
    // Instanciate the PlayerStateMediator to listen for player basic controls
    PlayerStateMediator.getInstance();
    // Search
    sbSearch = new SearchBox();
    // History
    jcbHistory = new SteppedComboBox();
    final JLabel jlHistory = new JLabel(IconLoader.getIcon(JajukIcons.HISTORY));
    jlHistory.setToolTipText(Messages.getString("CommandJPanel.0"));
    // We use a combo box model to make sure we get good performances after
    // rebuilding the entire model like after a refresh
    jcbHistory.setModel(new DefaultComboBoxModel(new Vector(History.getInstance().getItems())));
    // None selection because if we start in stop mode, a selection of the
    // first item will not launch the track because the selected item is
    // still the same and no action event is thrown (Java >= 1.6)
    jcbHistory.setSelectedItem(null);
    int iWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2);
    // size of popup
    jcbHistory.setPopupWidth(iWidth);
    jcbHistory.setToolTipText(Messages.getString("CommandJPanel.0"));
    jcbHistory.addActionListener(SearchJPanel.this);
    // Set a custom render to hostory combo in order to show the search icon
    // inside the combobox
    jcbHistory.setRenderer(new BasicComboBoxRenderer() {
      private static final long serialVersionUID = -6943363556191659895L;

      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setIcon(jlHistory.getIcon());
        return this;
      }
    });
    // Ambience combo
    ambiencesCombo = new AmbienceComboBox();
    // Add items
    setLayout(new MigLayout("insets 5 0 4 3,gapx 30", "[grow 20][grow 70][grow 10]"));
    add(ambiencesCombo, "left,gap left 16,growx,width 100::");
    add(jcbHistory, "grow,center");
    add(sbSearch, "right,grow,width 100::");
    // register to player events
    ObservationManager.register(this);
    // Update initial status
    UtilFeatures.updateStatus(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.AMBIENCES_CHANGE);
    eventSubjectSet.add(JajukEvents.AMBIENCES_SELECTION_CHANGE);
    eventSubjectSet.add(JajukEvents.CLEAR_HISTORY);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(final ActionEvent ae) {
    // do not run this in a separate thread because Player actions would die
    // with the thread
    try {
      if (ae.getSource() == jcbHistory) {
        HistoryItem hi;
        hi = History.getInstance().getHistoryItem(jcbHistory.getSelectedIndex());
        if (hi != null) {
          org.jajuk.base.File file = FileManager.getInstance().getFileByID(hi.getFileId());
          if (file != null) {
            try {
              QueueModel.push(new StackItem(file, Conf.getBoolean(Const.CONF_STATE_REPEAT), true),
                  Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
            } catch (JajukException je) {
              // can be thrown if file is null
            }
          } else {
            Messages.showErrorMessage(120);
            jcbHistory.setSelectedItem(null);
          }
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JajukEvents subject = event.getSubject();
        if (JajukEvents.PLAYER_STOP.equals(subject)) {
          // Reset history so user can launch again stopped
          // track (selection must change to throw an ActionEvent)
          jcbHistory.setSelectedIndex(-1);
        } else if (JajukEvents.ZERO.equals(subject)) {
          // Reset history so user can launch again stopped
          // track (selection must change to throw an ActionEvent)
          jcbHistory.setSelectedIndex(-1);
        } else if (JajukEvents.FILE_LAUNCHED.equals(subject)) {
          // Remove history listener, otherwise you'll get a looping
          // event generation
          jcbHistory.removeActionListener(SearchJPanel.this);
          if (jcbHistory.getItemCount() > 0) {
            jcbHistory.setSelectedIndex(0);
            jcbHistory.repaint();
          }
          jcbHistory.addActionListener(SearchJPanel.this);
        } else if (JajukEvents.CLEAR_HISTORY.equals(event.getSubject())) {
          // clear selection bar (data itself is clear
          // from the model by History class)
          jcbHistory.setSelectedItem(null);
        } else if (JajukEvents.AMBIENCES_CHANGE.equals(event.getSubject())
            || JajukEvents.AMBIENCES_SELECTION_CHANGE.equals(event.getSubject())) {
          ambiencesCombo.populateAmbiences();
          updateTooltips();
        }
      }
    });
  }

  /**
   * Update global functions tooltip after a change in ambiences or an ambience
   * selection using the ambience selector.
   */
  private void updateTooltips() {
    // Selected 'Any" ambience
    if (ambiencesCombo.getSelectedIndex() == 1) {
      JajukAction action = ActionManager.getAction(JajukActions.NOVELTIES);
      action.setShortDescription(Messages.getString("JajukWindow.31"));
      action = ActionManager.getAction(JajukActions.BEST_OF);
      action.setShortDescription(Messages.getString("JajukWindow.24"));
      action = ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL);
      action.setShortDescription(Messages.getString("JajukWindow.23"));
    } else {// Selected an ambience
      Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
          ((JLabel) ambiencesCombo.getSelectedItem()).getText());
      JajukAction action = ActionManager.getAction(JajukActions.NOVELTIES);
      action.setShortDescription(Const.HTML + Messages.getString("JajukWindow.31") + Const.P_B
          + ambience.getName() + Const.B_P_HTML);
      action = ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL);
      action.setShortDescription(Const.HTML + Messages.getString("JajukWindow.23") + Const.P_B
          + ambience.getName() + Const.B_P_HTML);
      action = ActionManager.getAction(JajukActions.BEST_OF);
      action.setShortDescription(Const.HTML + Messages.getString("JajukWindow.24") + Const.P_B
          + ambience.getName() + Const.B_P_HTML);
    }
  }
}