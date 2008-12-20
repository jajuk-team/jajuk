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
 *  $Revision: 3156 $
 */

package org.jajuk.ui.widgets;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.alarm.AlarmManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Alarm Clock Dialog window
 */
public class AlarmClockDialog extends JDialog implements ActionListener, ItemListener, Const,
    ListSelectionListener {
  private static final long serialVersionUID = 1L;

  private JPanel jpAlarmClock;

  private JPanel jpFields;

  private JPanel jpChoices;

  private JPanel jpOKCancel;

  private JPanel jpAction;

  private ButtonGroup bgChoices;

  private JButton jbOK;

  private JButton jbCancel;

  private JCheckBox jcbTime;

  private JLabel jlChoice;

  private JLabel jlAlarmAction;

  private JRadioButton jrbShuffle;

  private JRadioButton jrbBestof;

  private JRadioButton jrbNovelties;

  private JRadioButton jrbFile;

  private JTextField jtfHour;

  private JTextField jtfMinutes;

  private JTextField jtfSeconds;

  private JComboBox jcbAlarmAction;

  private SearchBox sbSearch;

  private SearchResult sr;

  public AlarmClockDialog() {
    jcbTime = new JCheckBox(Messages.getString("AlarmDialog.0"));
    jcbTime.addActionListener(this);

    jtfHour = new JTextField(2);
    jtfHour.setToolTipText(Messages.getString("AlarmDialog.1"));
    jtfMinutes = new JTextField(2);
    jtfMinutes.setToolTipText(Messages.getString("AlarmDialog.2"));
    jtfSeconds = new JTextField(2);
    jtfSeconds.setToolTipText(Messages.getString("AlarmDialog.3"));

    jpFields = new JPanel();
    jpFields.add(jcbTime);
    jpFields.add(jtfHour);
    jpFields.add(new JLabel(":"));
    jpFields.add(jtfMinutes);
    jpFields.add(new JLabel(":"));
    jpFields.add(jtfSeconds);
    jpFields.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    jpAction = new JPanel();
    jlAlarmAction = new JLabel(Messages.getString("AlarmDialog.4"));
    jcbAlarmAction = new JComboBox();
    jcbAlarmAction.addItem(Const.ALARM_START_MODE);
    jcbAlarmAction.addItem(Const.ALARM_STOP_MODE);
    jcbAlarmAction.setToolTipText(Messages.getString("AlarmDialog.5"));
    jcbAlarmAction.addActionListener(this);
    jpAction.add(jlAlarmAction);
    jpAction.add(jcbAlarmAction);
    jpAction.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    final double p = TableLayoutConstants.PREFERRED;
    final double sizeMessage[][] = { { 100, 300 }, { p } };
    final TableLayout layoutMessage = new TableLayout(sizeMessage);
    layoutMessage.setVGap(20);
    layoutMessage.setHGap(20);

    jpChoices = new JPanel();
    final double sizeStart[][] = { { 150, 200 }, { p, p, p, p, p, p } };
    final TableLayout layoutStartup = new TableLayout(sizeStart);
    layoutStartup.setVGap(20);
    layoutStartup.setHGap(20);
    jpChoices.setLayout(layoutStartup);
    jlChoice = new JLabel(Messages.getString("ParameterView.9"));
    jrbShuffle = new JRadioButton(Messages.getString("ParameterView.14"));
    jrbShuffle.setToolTipText(Messages.getString("ParameterView.15"));
    jrbShuffle.addItemListener(this);
    jrbBestof = new JRadioButton(Messages.getString("ParameterView.131"));
    jrbBestof.setToolTipText(Messages.getString("ParameterView.132"));
    jrbBestof.addItemListener(this);
    jrbNovelties = new JRadioButton(Messages.getString("ParameterView.133"));
    jrbNovelties.setToolTipText(Messages.getString("ParameterView.134"));
    jrbNovelties.addItemListener(this);
    jrbFile = new JRadioButton(Messages.getString("ParameterView.16"));
    jrbFile.setToolTipText(Messages.getString("ParameterView.17"));
    jrbFile.addItemListener(this);
    sbSearch = new SearchBox(this);
    // disabled by default, is enabled only if jrbFile is enabled
    sbSearch.setEnabled(false);
    sbSearch.setToolTipText(Messages.getString("ParameterView.18"));

    bgChoices = new ButtonGroup();
    bgChoices.add(jrbShuffle);
    bgChoices.add(jrbBestof);
    bgChoices.add(jrbNovelties);
    bgChoices.add(jrbFile);

    jpChoices.add(jlChoice, "0,0,1,0");
    jpChoices.add(jrbShuffle, "0,1,1,1");
    jpChoices.add(jrbBestof, "0,2,1,2");
    jpChoices.add(jrbNovelties, "0,3,1,3");
    jpChoices.add(jrbFile, "0,4");
    jpChoices.add(sbSearch, "1,4");
    jpChoices.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    jrbShuffle.setSelected(true);

    jpOKCancel = new JPanel();
    jpOKCancel.setLayout(new FlowLayout());
    jbOK = new JButton(Messages.getString("Ok"));
    jbOK.addActionListener(this);
    jpOKCancel.add(jbOK);
    jbCancel = new JButton(Messages.getString("Cancel"));
    jbCancel.addActionListener(this);
    jpOKCancel.add(jbCancel);
    jpOKCancel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    jpAlarmClock = new JPanel(new FlowLayout());
    double sizeAlarmPanel[][] = { { 500 }, { p, p, p, p } };
    TableLayout layoutAlarmPanel = new TableLayout(sizeAlarmPanel);
    layoutStartup.setVGap(20);
    layoutStartup.setHGap(20);
    jpAlarmClock.setLayout(layoutAlarmPanel);
    jpAlarmClock.add(jpFields, "0,0");
    jpAlarmClock.add(jpAction, "0,1");
    jpAlarmClock.add(jpChoices, "0,2");
    jpAlarmClock.add(jpOKCancel, "0,3");

    jpAlarmClock.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Reload on GUI saved values
    loadValues();

    setTitle(Messages.getString("AlarmClock.0"));
    setMinimumSize(new Dimension(250, 100));
    setContentPane(jpAlarmClock);

    setModal(true);
    pack();
    setLocationRelativeTo(JajukWindow.getInstance());
    setVisible(true);
  }

  public void actionPerformed(final ActionEvent e) {
    boolean playAction = (jcbAlarmAction.getSelectedIndex() == 0);
    if (e.getSource() == jcbAlarmAction) {
      jlChoice.setEnabled(playAction);
      jrbShuffle.setEnabled(playAction);
      jrbBestof.setEnabled(playAction);
      jrbNovelties.setEnabled(playAction);
      jrbFile.setEnabled(playAction);
      sbSearch.setEnabled(playAction);
    } else if (e.getSource() == jbOK) {
        saveValues();
    } else if (e.getSource() == jbCancel) {
      dispose();
    } else if (e.getSource() == jcbTime) {
      // Enable/ disable all widgets is user enables or disables the entire
      // alarm
      boolean enabled = jcbTime.isSelected();
      jtfHour.setEnabled(enabled);
      jtfMinutes.setEnabled(enabled);
      jtfSeconds.setEnabled(enabled);
      jcbAlarmAction.setEnabled(enabled);
      jlChoice.setEnabled(enabled && playAction);
      jrbShuffle.setEnabled(enabled && playAction);
      jrbBestof.setEnabled(enabled && playAction);
      jrbNovelties.setEnabled(enabled && playAction);
      jrbFile.setEnabled(enabled && playAction);
      sbSearch.setEnabled(enabled && playAction);
    }
  }

  public void itemStateChanged(final ItemEvent e) {
    if (e.getSource() == jrbFile) {
      sbSearch.setEnabled(jrbFile.isSelected());
    }
  }

  public void valueChanged(final ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {
      sr = sbSearch.getResult(sbSearch.getSelectedIndex());
      sbSearch.setText(sr.getFile().getTrack().getName());
      sbSearch.hidePopup();
    }
  }

  /**
   * Store GUI values to persisted values
   */
  public void saveValues() {
    // Parse the final alarm value
    Calendar cal = Calendar.getInstance();
    try {
      cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(jtfHour.getText()));
      cal.set(Calendar.MINUTE, Integer.parseInt(jtfMinutes.getText()));
      cal.set(Calendar.SECOND, Integer.parseInt(jtfSeconds.getText()));
    } catch (Exception e) {
      Log.error(e);
      Messages.showErrorMessage(177);
      return;
    }
    // Store values
    Conf.setProperty(Const.CONF_ALARM_ENABLED, ((Boolean) jcbTime.isSelected()).toString());
    Conf.setProperty(Const.CONF_ALARM_ACTION, jcbAlarmAction.getSelectedItem().toString());
    Conf.setProperty(CONF_ALARM_TIME_HOUR, jtfHour.getText());
    Conf.setProperty(CONF_ALARM_TIME_MINUTES, jtfMinutes.getText());
    Conf.setProperty(CONF_ALARM_TIME_SECONDS, jtfSeconds.getText());
    if (jrbShuffle.isSelected()) {
      Conf.setProperty(Const.CONF_ALARM_MODE, Const.STARTUP_MODE_SHUFFLE);
    } else if (jrbFile.isSelected()) {
      Conf.setProperty(Const.CONF_ALARM_MODE, Const.STARTUP_MODE_FILE);
      // sr = null means none search occurred in this session
      if (sr != null) {
        Conf.setProperty(Const.CONF_ALARM_FILE, sr.getFile().getID());
      }
    } else if (jrbBestof.isSelected()) {
      Conf.setProperty(Const.CONF_ALARM_MODE, Const.STARTUP_MODE_BESTOF);
    } else if (jrbNovelties.isSelected()) {
      Conf.setProperty(Const.CONF_ALARM_MODE, Const.STARTUP_MODE_NOVELTIES);
    }
    // Store properties in case of
    try {
      Conf.commit();
    } catch (Exception e) {
      Log.error(e);
    }
    // Close the window
    dispose();
    // Notify the Alarm manager
    ObservationManager.notify(new Event(JajukEvents.ALARMS_CHANGE));
    // Display a message
    Messages.showInfoMessage(Messages.getString("Success"));
    // Start manager up
    AlarmManager.getInstance();
  }

  /**
   * Load persisted values to GUI
   */
  private void loadValues() {
    jcbTime.setSelected(Conf.getBoolean(CONF_ALARM_ENABLED));
    jtfHour.setText(Conf.getString(CONF_ALARM_TIME_HOUR));
    jtfMinutes.setText(Conf.getString(CONF_ALARM_TIME_MINUTES));
    jtfSeconds.setText(Conf.getString(CONF_ALARM_TIME_SECONDS));
    // Alarm mode (play/stop)
    if (ALARM_START_MODE.equals(Conf.getString(CONF_ALARM_ACTION))) {
      jcbAlarmAction.setSelectedIndex(0);
    } else if (ALARM_STOP_MODE.equals(Conf.getString(CONF_ALARM_ACTION))) {
      jcbAlarmAction.setSelectedIndex(1);
    }
    // Alarm action
    if (Const.STARTUP_MODE_BESTOF.equals(Conf.getString(CONF_ALARM_MODE))) {
      jrbBestof.setSelected(true);
    } else if (Const.STARTUP_MODE_NOVELTIES.equals(Conf.getString(CONF_ALARM_MODE))) {
      jrbNovelties.setSelected(true);
    } else if (Const.STARTUP_MODE_FILE.equals(Conf.getString(CONF_ALARM_MODE))) {
      jrbFile.setSelected(true);
      String fileName = FileManager.getInstance()
          .getFileByID(Conf.getString(Const.CONF_ALARM_FILE)).getName();
      sbSearch.setText(fileName);
    } else if (Const.STARTUP_MODE_SHUFFLE.equals(Conf.getString(CONF_ALARM_MODE))) {
      jrbShuffle.setSelected(true);
    }
    // Force an an action event to update enable state of widgets
    actionPerformed(new ActionEvent(jcbTime, 0, null));
  }

}
