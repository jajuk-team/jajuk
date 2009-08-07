/*
 *  Jajuk
 *  Copyright (C) 2009 The Jajuk Team
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
 *  $Revision: 4739 $
 */
package org.jajuk.ui.widgets;

import ext.SwingWorker;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult;
import org.jajuk.base.SearchResult.SearchResultType;
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
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.PlayerStateMediator;
import org.jajuk.ui.helpers.FontManager.JajukFont;
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
 * History, search panel
 */
public final class SearchJPanel extends JXPanel implements Observer, ActionListener {

  private static final long serialVersionUID = 1L;

  /** Self instance */
  private static SearchJPanel ijp = null;

  private SearchBox sbSearch;
  private SteppedComboBox jcbHistory;
  private SteppedComboBox ambiencesCombo;

  /** Ambience combo listener */
  class AmbienceListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      // Ambience Configuration
      if (ambiencesCombo.getSelectedIndex() == 0) {
        // display the wizard
        try {
          ActionManager.getAction(JajukActions.CONFIGURE_AMBIENCES).perform(null);
        } catch (Exception e) {
          Log.error(e);
        }
        // Reset combo to last selected item. We do this to avoid to select the
        // "0" item that is not an ambience
        ambiencesCombo.removeActionListener(ambienceListener);
        Ambience defaultAmbience = AmbienceManager.getInstance().getAmbience(
            Conf.getString(Const.CONF_DEFAULT_AMBIENCE));
        if (defaultAmbience != null) {
          for (int i = 0; i < ambiencesCombo.getItemCount(); i++) {
            if (((JLabel) ambiencesCombo.getItemAt(i)).getText().equals(defaultAmbience.getName())) {
              ambiencesCombo.setSelectedIndex(i);
              break;
            }
          }
        } else {
          ambiencesCombo.setSelectedIndex(1);
        }
        ambiencesCombo.addActionListener(ambienceListener);
      }
      // Selected 'Any" ambience
      else if (ambiencesCombo.getSelectedIndex() == 1) {
        // reset default ambience
        Conf.setProperty(Const.CONF_DEFAULT_AMBIENCE, "");
        ObservationManager.notify(new JajukEvent(JajukEvents.AMBIENCES_SELECTION_CHANGE));
      } else {// Selected an ambience
        Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
            ((JLabel) ambiencesCombo.getSelectedItem()).getText());
        Conf.setProperty(Const.CONF_DEFAULT_AMBIENCE, ambience.getID());
        ObservationManager.notify(new JajukEvent(JajukEvents.AMBIENCES_SELECTION_CHANGE));
      }
    }
  }

  /** An instance of the ambience combo listener */
  AmbienceListener ambienceListener;

  /**
   * Singleton access
   * 
   * @return
   */
  public static SearchJPanel getInstance() {
    if (ijp == null) {
      ijp = new SearchJPanel();
    }
    return ijp;
  }

  // widgets declaration

  private SearchJPanel() {
    super();

  }

  public void initUI() {
    // Instanciate the PlayerStateMediator to listen for player basic controls
    PlayerStateMediator.getInstance();

    // Search
    sbSearch = new SearchBox() {
      private static final long serialVersionUID = 1L;

      public void valueChanged(final ListSelectionEvent e) {
        SwingWorker sw = new SwingWorker() {
          @Override
          public Object construct() {
            if (!e.getValueIsAdjusting()) {
              SearchResult sr = sbSearch.getResult(sbSearch.getSelectedIndex());
              try {
                // If user selected a file
                if (sr.getType() == SearchResultType.FILE) {
                  QueueModel.push(new StackItem(sr.getFile(), Conf
                      .getBoolean(Const.CONF_STATE_REPEAT_ALL), true), Conf
                      .getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
                }
                // User selected a web radio
                else if (sr.getType() == SearchResultType.WEBRADIO) {
                  QueueModel.launchRadio(sr.getWebradio());
                }
              } catch (JajukException je) {
                Log.error(je);
              }
            }
            return null;
          }

          @Override
          public void finished() {
            if (!e.getValueIsAdjusting()) {
              sbSearch.hidePopup();
              requestFocusInWindow();
            }
          }
        };
        sw.start();
      }
    };

    // History
    jcbHistory = new SteppedComboBox();
    final JLabel jlHistory = new JLabel(IconLoader.getIcon(JajukIcons.HISTORY));
    jlHistory.setToolTipText(Messages.getString("CommandJPanel.0"));
    // - Increase rating button
    JajukAction actionIncRate = ActionManager.getAction(JajukActions.INC_RATE);
    actionIncRate.setName(null);
    final JPopupMenu jpmIncRating = new JPopupMenu();
    for (int i = 3; i >= -3; i--) {
      final int j = i;
      JMenuItem jmi = new JMenuItem(Integer.toString(i));
      if (Conf.getInt(Const.CONF_INC_RATING) == i) {
        jmi.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      }
      // Store selected value
      jmi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Conf.setProperty(Const.CONF_INC_RATING, "" + j);
        }
      });
      jpmIncRating.add(jmi);
    }
    // we use a combo box model to make sure we get good performances after
    // rebuilding the entire model like after a refresh
    jcbHistory.setModel(new DefaultComboBoxModel(History.getInstance().getHistory()));
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
    ambiencesCombo = new SteppedComboBox();
    iWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 4);
    ambiencesCombo.setPopupWidth(iWidth);
    // size of the combo itself
    ambiencesCombo.setRenderer(new BasicComboBoxRenderer() {
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
    ambiencesCombo.setToolTipText(Messages.getString("DigitalDJWizard.66"));
    populateAmbiences();
    ambienceListener = new AmbienceListener();
    ambiencesCombo.addActionListener(ambienceListener);

    // Add items
    setLayout(new MigLayout("insets 5 0 4 1,gapx 30", "[grow 20][grow 70][grow 10]"));
    add(ambiencesCombo, "left,gap left 16,growx,width 100::");
    add(jcbHistory, "grow,center");
    add(sbSearch, "right,grow,width 100::");

    // register to player events
    ObservationManager.register(this);
    
    // Update initial status
    UtilFeatures.updateStatus(this);
  }

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

  /**
   * Populate ambiences combo
   * 
   */
  void populateAmbiences() {
    ambiencesCombo.removeActionListener(ambienceListener);
    ItemListener[] il = ambiencesCombo.getItemListeners();
    for (ItemListener element : il) {
      ambiencesCombo.removeItemListener(element);
    }
    ambiencesCombo.removeAllItems();
    ambiencesCombo.addItem(new JLabel(Messages.getString("CommandJPanel.19"), IconLoader
        .getIcon(JajukIcons.CONFIGURATION), SwingConstants.LEFT));
    ambiencesCombo.addItem(new JLabel("<html><i>" + Messages.getString("DigitalDJWizard.64")
        + "</i></html>", IconLoader.getIcon(JajukIcons.STYLE), SwingConstants.LEFT));
    // Add available ambiences
    for (final Ambience ambience : AmbienceManager.getInstance().getAmbiences()) {
      ambiencesCombo.addItem(new JLabel(ambience.getName(), IconLoader.getIcon(JajukIcons.STYLE),
          SwingConstants.LEFT));
    }
    // Select right item
    Ambience defaultAmbience = AmbienceManager.getInstance().getAmbience(
        Conf.getString(Const.CONF_DEFAULT_AMBIENCE));
    if (defaultAmbience != null) {
      for (int i = 0; i < ambiencesCombo.getItemCount(); i++) {
        if (((JLabel) ambiencesCombo.getItemAt(i)).getText().equals(defaultAmbience.getName())) {
          ambiencesCombo.setSelectedIndex(i);
          break;
        }
      }
    } else {
      // or "any" ambience
      ambiencesCombo.setSelectedIndex(1);
    }
    ambiencesCombo.addActionListener(ambienceListener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
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
              QueueModel.push(new StackItem(file, Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL),
                  true), Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
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
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
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
          populateAmbiences();
          updateTooltips();
        }
      }
    });
  }

  /**
   * Update global functions tooltip after a change in ambiences or an ambience
   * selection using the ambience selector
   * 
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