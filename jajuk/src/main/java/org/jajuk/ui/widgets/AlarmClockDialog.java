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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jajuk.Main;
import org.jajuk.base.SearchResult;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;

/**
 * Alarm Clock Dialog window
 */
public class AlarmClockDialog extends JDialog implements ActionListener, ItemListener,
    ITechnicalStrings, ListSelectionListener {
  private static final long serialVersionUID = 1L;

  JPanel jpAlarmClock;

  JPanel jpFields;

  JPanel jpChoices;

  JPanel jpOKCancel;

  JPanel jpAction;

  ButtonGroup bgChoices;

  JButton jbOK;

  JButton jbCancel;

  JLabel jlTime;

  JLabel jlChoice;

  JLabel jlSeparator1;

  JLabel jlSeparator2;

  JLabel jlAlarmAction;

  JRadioButton jrbShuffle;

  JRadioButton jrbBestof;

  JRadioButton jrbNovelties;

  JRadioButton jrbFile;

  JTextField jtfHour;

  JTextField jtfMinutes;

  JTextField jtfSeconds;

  JCheckBox jcbDaily;

  SteppedComboBox scbAlarmOption;

  JPanel jpMessage;

  JCheckBox jcbMessage;

  JTextField jtfMessage;

  SearchBox sbSearch;

  boolean choice;

  public AlarmClockDialog() {
    jlTime = new JLabel(Messages.getString("AlarmDialog.0"));

    jtfHour = new JTextField(2);
    jtfHour.setToolTipText(Messages.getString("AlarmDialog.1"));
    jtfMinutes = new JTextField(2);
    jtfMinutes.setToolTipText(Messages.getString("AlarmDialog.2"));
    jtfSeconds = new JTextField(2);
    jtfSeconds.setToolTipText(Messages.getString("AlarmDialog.3"));
    jlSeparator1 = new JLabel(":");
    jlSeparator2 = new JLabel(":");
    jcbDaily = new JCheckBox(Messages.getString("AlarmDialog.8"));
    jcbDaily.setToolTipText(Messages.getString("AlarmDialog.9"));
    jcbDaily.addActionListener(this);

    jpFields = new JPanel();
    jpFields.add(jlTime);
    jpFields.add(jtfHour);
    jpFields.add(jlSeparator1);
    jpFields.add(jtfMinutes);
    jpFields.add(jlSeparator2);
    jpFields.add(jtfSeconds);
    jpFields.add(jcbDaily);
    jpFields.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    jpAction = new JPanel();
    jlAlarmAction = new JLabel(Messages.getString("AlarmDialog.4"));
    scbAlarmOption = new SteppedComboBox();
    scbAlarmOption.addItem(ITechnicalStrings.ALARM_START_MODE);
    scbAlarmOption.addItem(ITechnicalStrings.ALARM_STOP_MODE);
    scbAlarmOption.setToolTipText(Messages.getString("AlarmDialog.5"));
    scbAlarmOption.addActionListener(this);
    jpAction.add(jlAlarmAction);
    jpAction.add(scbAlarmOption);
    jpAction.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    // scbAlarmOption.addActionListener(this);

    jpMessage = new JPanel();
    final double p = TableLayoutConstants.PREFERRED;
    final double sizeMessage[][] = { { 100, 300 }, { p } };
    final TableLayout layoutMessage = new TableLayout(sizeMessage);
    layoutMessage.setVGap(20);
    layoutMessage.setHGap(20);
    jpMessage.setLayout(layoutMessage);
    jcbMessage = new JCheckBox(Messages.getString("AlarmDialog.6"));
    jcbMessage.setToolTipText(Messages.getString("AlarmDialog.7"));
    jcbMessage.addActionListener(this);
    jtfMessage = new JTextField(20);
    jtfMessage.setToolTipText(Messages.getString("AlarmDialog.7"));
    jtfMessage.setEnabled(false);
    jpMessage.add(jcbMessage, "0,0");
    jpMessage.add(jtfMessage, "1,0");
    jpMessage.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 5));

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
    final double sizeAlarmPanel[][] = { { 500 }, { p, p, p, p, p } };
    final TableLayout layoutAlarmPanel = new TableLayout(sizeAlarmPanel);
    layoutStartup.setVGap(20);
    layoutStartup.setHGap(20);
    jpAlarmClock.setLayout(layoutAlarmPanel);
    jpAlarmClock.add(jpFields, "0,0");
    jpAlarmClock.add(jpAction, "0,1");
    jpAlarmClock.add(jpMessage, "0,2");
    jpAlarmClock.add(jpChoices, "0,3");
    jpAlarmClock.add(jpOKCancel, "0,4");

    jpAlarmClock.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    setTitle("Set Alarm Time");
    setMinimumSize(new Dimension(250, 100));
    setContentPane(jpAlarmClock);
    setModal(true);
    setAlwaysOnTop(true);
    pack();
    setLocationRelativeTo(Main.getWindow());
    setVisible(true);
  }

  public void actionPerformed(final ActionEvent e) {
    ConfigurationManager.setProperty(CONF_ALARM_ACTION, "" + scbAlarmOption.getSelectedItem());
    if (ConfigurationManager.getProperty(ITechnicalStrings.CONF_ALARM_ACTION).equals(
        ITechnicalStrings.ALARM_START_MODE)) {
      jlChoice.setEnabled(true);
      jrbShuffle.setEnabled(true);
      jrbBestof.setEnabled(true);
      jrbNovelties.setEnabled(true);
      jrbFile.setEnabled(true);
      sbSearch.setEnabled(true);
    } else {
      jlChoice.setEnabled(false);
      jrbShuffle.setEnabled(false);
      jrbBestof.setEnabled(false);
      jrbNovelties.setEnabled(false);
      jrbFile.setEnabled(false);
      sbSearch.setEnabled(false);
    }
    if (e.getSource() == jcbMessage) {
      if (jcbMessage.isSelected())
        jtfMessage.setEnabled(true);
      else {
        jtfMessage.setEnabled(false);
        ConfigurationManager.setProperty(ALARM_MESSAGE, "");
      }
    } else if (e.getSource() == jbOK) {
      updateParameters();
      choice = true;
      dispose();
    } else if (e.getSource() == jbCancel) {
      choice = false;
      dispose();
    }
  }

  public void itemStateChanged(final ItemEvent e) {
    if (e.getSource() == jrbFile) {
      sbSearch.setEnabled(jrbFile.isSelected());
    }
  }

  public void valueChanged(final ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {
      final SearchResult sr = sbSearch.alResults.get(sbSearch.jlist.getSelectedIndex());
      sbSearch.setText(sr.getFile().getTrack().getName());
      ConfigurationManager.setProperty(ITechnicalStrings.CONF_ALARM_FILE, sr.getFile().getID());
      sbSearch.popup.hide();
    }
  }

  public void updateParameters() {
    ConfigurationManager.setProperty(ALARM_TIME_HOUR, "" + jtfHour.getText());
    ConfigurationManager.setProperty(ALARM_TIME_MINUTES, "" + jtfMinutes.getText());
    ConfigurationManager.setProperty(ALARM_TIME_SECONDS, "" + jtfSeconds.getText());
    ConfigurationManager.setProperty(ALARM_MESSAGE, "" + jtfMessage.getText());
    if (jcbDaily.isSelected())
      ConfigurationManager.setProperty(CONF_ALARM_DAILY, "" + true);
    else {
      ConfigurationManager.setProperty(CONF_ALARM_DAILY, "" + false);
    }
    if (jrbShuffle.isSelected()) {
      ConfigurationManager.setProperty(ITechnicalStrings.CONF_ALARM_MODE,
          ITechnicalStrings.STARTUP_MODE_SHUFFLE);
    } else if (jrbFile.isSelected()) {
      ConfigurationManager.setProperty(ITechnicalStrings.CONF_ALARM_MODE,
          ITechnicalStrings.STARTUP_MODE_FILE);
    } else if (jrbBestof.isSelected()) {
      ConfigurationManager.setProperty(ITechnicalStrings.CONF_ALARM_MODE,
          ITechnicalStrings.STARTUP_MODE_BESTOF);
    } else if (jrbNovelties.isSelected()) {
      ConfigurationManager.setProperty(ITechnicalStrings.CONF_ALARM_MODE,
          ITechnicalStrings.STARTUP_MODE_NOVELTIES);
    }
  }

  public boolean getChoice() {
    return choice;
  }
}
