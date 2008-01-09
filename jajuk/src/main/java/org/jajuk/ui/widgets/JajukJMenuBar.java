/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 *  $Revision$
 */
package org.jajuk.ui.widgets;

import static org.jajuk.ui.actions.JajukAction.CONFIGURE_AMBIENCES;
import static org.jajuk.ui.actions.JajukAction.CONFIGURE_DJS;
import static org.jajuk.ui.actions.JajukAction.CONTINUE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.actions.JajukAction.CUSTOM_PROPERTIES_ADD;
import static org.jajuk.ui.actions.JajukAction.CUSTOM_PROPERTIES_REMOVE;
import static org.jajuk.ui.actions.JajukAction.HELP_REQUIRED;
import static org.jajuk.ui.actions.JajukAction.INTRO_MODE_STATUS_CHANGED;
import static org.jajuk.ui.actions.JajukAction.OPTIONS;
import static org.jajuk.ui.actions.JajukAction.QUALITY;
import static org.jajuk.ui.actions.JajukAction.REPEAT_MODE_STATUS_CHANGE;
import static org.jajuk.ui.actions.JajukAction.SHOW_ABOUT;
import static org.jajuk.ui.actions.JajukAction.SHOW_TRACES;
import static org.jajuk.ui.actions.JajukAction.SHUFFLE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.actions.JajukAction.TIP_OF_THE_DAY;
import static org.jajuk.ui.actions.JajukAction.VIEW_RESTORE_DEFAULTS;
import static org.jajuk.ui.actions.JajukAction.WIZARD;

import com.sun.java.help.impl.SwingWorker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.jajuk.services.alarm.AlarmThread;
import org.jajuk.services.alarm.AlarmThreadManager;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.ActionUtil;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.perspectives.PerspectiveAdapter;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.ViewFactory;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Jajuk menu bar
 * <p>
 * Singleton
 */
public class JajukJMenuBar extends JMenuBar implements ITechnicalStrings, MouseMotionListener,
    Observer {

  private static final long serialVersionUID = 1L;

  static JajukJMenuBar jjmb;

  JMenu file;

  JMenuItem jmiFileOpen;

  JajukFileChooser jfchooser;

  JMenuItem jmiFileExit;

  JMenu views;

  JMenuItem jmiRestoreDefaultViews;

  JMenuItem jmiRestoreDefaultViewsAllPerpsectives;

  JMenu properties;

  JMenuItem jmiNewProperty;

  JMenuItem jmiRemoveProperty;

  JMenu mode;

  JCheckBoxMenuItem jcbShowPopups;

  public JCheckBoxMenuItem jcbmiRepeat;

  public JCheckBoxMenuItem jcbmiShuffle;

  public JCheckBoxMenuItem jcbmiContinue;

  public JCheckBoxMenuItem jcbmiIntro;

  JMenu tools;

  JMenuItem jmiduplicateFinder;

  JMenuItem jmialarmClock;

  JMenu configuration;

  JMenuItem jmiDJ;

  JMenuItem jmiAmbience;

  JMenuItem jmiWebradios;

  JMenuItem jmiWizard;

  JMenuItem jmiOptions;

  JCheckBoxMenuItem jmiUnmounted;

  JMenu help;

  JMenuItem jmiHelp;

  JMenuItem jmiTipOfTheDay;

  JMenuItem jmiQualityAgent;

  JMenuItem jmiTraces;

  JMenuItem jmiCheckforUpdates;

  JMenuItem jmiAbout;

  JMenu jmReminders;

  JLabel jlUpdate;

  /** Hashmap JCheckBoxMenuItem -> associated view */
  public HashMap hmCheckboxView = new HashMap(10);

  private JajukJMenuBar() {
    setAlignmentX(0.0f);
    // File menu
    file = new JMenu(Messages.getString("JajukJMenuBar.0"));

    jmiFileExit = new JMenuItem(ActionManager.getAction(JajukAction.EXIT));
    file.add(jmiFileExit);

    // Properties menu
    properties = new JMenu(Messages.getString("JajukJMenuBar.5"));
    jmiNewProperty = new JMenuItem(ActionManager.getAction(CUSTOM_PROPERTIES_ADD));
    jmiRemoveProperty = new JMenuItem(ActionManager.getAction(CUSTOM_PROPERTIES_REMOVE));
    properties.add(jmiNewProperty);
    properties.add(jmiRemoveProperty);

    // View menu
    views = new JMenu(Messages.getString("JajukJMenuBar.8"));
    jmiRestoreDefaultViews = new JMenuItem(ActionManager.getAction(VIEW_RESTORE_DEFAULTS));
    jmiRestoreDefaultViewsAllPerpsectives = new JMenuItem(ActionManager
        .getAction(JajukAction.ALL_VIEW_RESTORE_DEFAULTS));

    views.add(jmiRestoreDefaultViews);
    views.add(jmiRestoreDefaultViewsAllPerpsectives);
    views.addSeparator();
    // Add the list of available views parsed in XML files at startup
    JMenu jmViews = new JMenu(Messages.getString("JajukJMenuBar.25"));
    for (final Class view : ViewFactory.getKnownViews()) {
      JMenuItem jmi = null;
      try {
        jmi = new JMenuItem(((IView) view.newInstance()).getDesc(), IconLoader.ICON_LOGO_FRAME);
      } catch (Exception e1) {
        Log.error(e1);
      }
      jmi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // Simply add the new view in the current perspective
          PerspectiveAdapter current = (PerspectiveAdapter) PerspectiveManager
              .getCurrentPerspective();
          IView newView = ViewFactory.createView(view, current);
          newView.initUI();
          newView.setIsPopulated(true);
          current.addDockable(newView);
        }
      });
      jmViews.add(jmi);
    }
    views.add(jmViews);

    // Mode menu
    String modeText = Messages.getString("JajukJMenuBar.9");
    mode = new JMenu(ActionUtil.strip(modeText));
    mode.setMnemonic(ActionUtil.getMnemonic(modeText));

    jcbmiRepeat = new JCheckBoxMenuItem(ActionManager.getAction(REPEAT_MODE_STATUS_CHANGE));
    jcbmiRepeat.setSelected(ConfigurationManager.getBoolean(CONF_STATE_REPEAT));
    jcbmiShuffle = new JCheckBoxMenuItem(ActionManager.getAction(SHUFFLE_MODE_STATUS_CHANGED));
    jcbmiShuffle.setSelected(ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE));
    jcbmiContinue = new JCheckBoxMenuItem(ActionManager.getAction(CONTINUE_MODE_STATUS_CHANGED));
    jcbmiContinue.setSelected(ConfigurationManager.getBoolean(CONF_STATE_CONTINUE));
    jcbmiIntro = new JCheckBoxMenuItem(ActionManager.getAction(INTRO_MODE_STATUS_CHANGED));
    jcbmiIntro.setSelected(ConfigurationManager.getBoolean(CONF_STATE_INTRO));

    jmiUnmounted = new JCheckBoxMenuItem(ActionManager.getAction(JajukAction.UNMOUNTED));
    jmiUnmounted.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED));
    jmiUnmounted.putClientProperty(DETAIL_ORIGIN, jmiUnmounted);
    
    jcbShowPopups = new JCheckBoxMenuItem(Messages.getString("ParameterView.228"));
    jcbShowPopups.setSelected(ConfigurationManager.getBoolean(CONF_SHOW_POPUPS));
    jcbShowPopups.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ConfigurationManager.setProperty(CONF_SHOW_POPUPS, Boolean.toString(jcbShowPopups
            .isSelected()));
        // force parameter view to take this into account
        ObservationManager.notify(new Event(EventSubject.EVENT_PARAMETERS_CHANGE));
      }
    });

    mode.add(jmiUnmounted);
    mode.add(jcbShowPopups);
    mode.addSeparator();
    mode.add(jcbmiRepeat);
    mode.add(jcbmiShuffle);
    mode.add(jcbmiContinue);
    mode.add(jcbmiIntro);

    // Tools Menu
    tools = new JMenu(Messages.getString("JajukJMenuBar.28"));
    jmiduplicateFinder = new JMenuItem(ActionManager.getAction(JajukAction.FIND_DUPLICATE_FILES));
    jmialarmClock = new JMenuItem(ActionManager.getAction(JajukAction.ALARM_CLOCK));
    jmReminders = new JMenu(Messages.getString("AlarmClock.1"));
    jmReminders.addMouseMotionListener(this);
    for (final AlarmThread alarm : AlarmThreadManager.getInstance().getAllAlarms()) {
      JMenuItem jma = new JMenuItem(alarm.getAlarmTime(), IconLoader.ICON_ALARM);
      jmReminders.add(jma);
      jmReminders.addSeparator();
    }

    tools.add(jmiduplicateFinder);
    tools.add(jmialarmClock);
    tools.addSeparator();
    tools.add(jmReminders);

    // Configuration menu
    configuration = new JMenu(Messages.getString("JajukJMenuBar.21"));
    jmiDJ = new JMenuItem(ActionManager.getAction(CONFIGURE_DJS));
    // Overwrite default icon
    jmiDJ.setIcon(IconLoader.ICON_DIGITAL_DJ_16x16);
    jmiAmbience = new JMenuItem(ActionManager.getAction(CONFIGURE_AMBIENCES));
    jmiWebradios = new JMenuItem(ActionManager.getAction(JajukAction.CONFIGURE_WEBRADIOS));
    jmiWebradios.setIcon(IconLoader.ICON_WEBRADIO_16x16);
    jmiWizard = new JMenuItem(ActionManager.getAction(WIZARD));
    jmiOptions = new JMenuItem(ActionManager.getAction(OPTIONS));
    configuration.add(jmiOptions);
    configuration.add(jmiDJ);
    configuration.add(jmiAmbience);
    configuration.add(jmiWebradios);
    configuration.add(jmiWizard);

    // Help menu
    String helpText = Messages.getString("JajukJMenuBar.14");
    help = new JMenu(ActionUtil.strip(helpText));
    help.setMnemonic(ActionUtil.getMnemonic(helpText));
    jmiHelp = new JMenuItem(ActionManager.getAction(HELP_REQUIRED));
    jmiAbout = new JMenuItem(ActionManager.getAction(SHOW_ABOUT));
    jmiTraces = new JMenuItem(ActionManager.getAction(SHOW_TRACES));
    jmiTraces = new JMenuItem(ActionManager.getAction(SHOW_TRACES));
    jmiCheckforUpdates = new JMenuItem(ActionManager.getAction(JajukAction.CHECK_FOR_UPDATES));
    jmiTipOfTheDay = new JMenuItem(ActionManager.getAction(TIP_OF_THE_DAY));

    help.add(jmiHelp);
    help.add(jmiTipOfTheDay);
    // this works only for Linux and Windows
    if (Util.isUnderLinux() || Util.isUnderWindows()) {
      jmiQualityAgent = new JMenuItem(ActionManager.getAction(QUALITY));
      help.add(jmiQualityAgent);
    }
    help.add(jmiTraces);
    help.add(jmiCheckforUpdates);
    help.add(jmiAbout);

    add(file);
    add(views);
    add(properties);
    add(mode);
    add(tools);
    add(configuration);
    add(help);

    // Check for new release and display the icon if a new release is available
    SwingWorker sw = new SwingWorker() {

      @Override
      public Object construct() {
        UpgradeManager.checkForUpdate();
        return null;
      }

      public void finished() {
        // add the new release label if required
        if (UpgradeManager.getNewVersionName() != null) {
          jlUpdate = new JLabel(" ", IconLoader.ICON_UPDATE_MANAGER, JLabel.HORIZONTAL);
          String newRelease = UpgradeManager.getNewVersionName();
          if (newRelease != null) {
            jlUpdate.setToolTipText(Messages.getString("UpdateManager.0") + newRelease
                + Messages.getString("UpdateManager.1"));
          }
          add(Box.createHorizontalGlue());
          add(jlUpdate);
        }
      }

    };
    if (ConfigurationManager.getBoolean(CONF_CHECK_FOR_UPDATE)) {
      sw.start();
    }
    ObservationManager.register(this);
  }

  static public synchronized JajukJMenuBar getInstance() {
    if (jjmb == null) {
      jjmb = new JajukJMenuBar();
    }
    return jjmb;
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  public void mouseMoved(MouseEvent e) {
    jmReminders.removeAll();
    if (AlarmThreadManager.getInstance().getAllAlarms().size() == 0)
      jmReminders.add(Messages.getString("AlarmClock.2"));
    else {
      for (final AlarmThread alarm : AlarmThreadManager.getInstance().getAllAlarms()) {
        JMenuItem jma = new JMenuItem(alarm.getAlarmText(), IconLoader.ICON_ALARM);
        jma.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            int iResu = Messages.getChoice(Messages.getString("Confirmation_alarm_stop"),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (iResu != JOptionPane.YES_OPTION) {
              return;
            }
            AlarmThreadManager.getInstance().stopAlarm(alarm);
          }
        });
        jmReminders.add(jma);
      }
    }
  }

  public void mouseDragged(MouseEvent e) {
    mouseMoved(e);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(Event event) {
    if (EventSubject.EVENT_PARAMETERS_CHANGE.equals(event.getSubject())) {
      jcbShowPopups.setSelected(ConfigurationManager.getBoolean(CONF_SHOW_POPUPS));
      jmiUnmounted.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED));
    }
  }
}
