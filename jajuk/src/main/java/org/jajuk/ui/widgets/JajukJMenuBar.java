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

import static org.jajuk.ui.actions.JajukActions.CONFIGURE_AMBIENCES;
import static org.jajuk.ui.actions.JajukActions.CONFIGURE_DJS;
import static org.jajuk.ui.actions.JajukActions.CONTINUE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.actions.JajukActions.CUSTOM_PROPERTIES_ADD;
import static org.jajuk.ui.actions.JajukActions.CUSTOM_PROPERTIES_REMOVE;
import static org.jajuk.ui.actions.JajukActions.HELP_REQUIRED;
import static org.jajuk.ui.actions.JajukActions.INTRO_MODE_STATUS_CHANGED;
import static org.jajuk.ui.actions.JajukActions.OPTIONS;
import static org.jajuk.ui.actions.JajukActions.QUALITY;
import static org.jajuk.ui.actions.JajukActions.REPEAT_MODE_STATUS_CHANGE;
import static org.jajuk.ui.actions.JajukActions.SHOW_ABOUT;
import static org.jajuk.ui.actions.JajukActions.SHOW_TRACES;
import static org.jajuk.ui.actions.JajukActions.SHUFFLE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.actions.JajukActions.SIMPLE_DEVICE_WIZARD;
import static org.jajuk.ui.actions.JajukActions.TIP_OF_THE_DAY;
import static org.jajuk.ui.actions.JajukActions.VIEW_RESTORE_DEFAULTS;

import com.sun.java.help.impl.SwingWorker;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.alarm.Alarm;
import org.jajuk.services.alarm.AlarmManager;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.ActionUtil;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.perspectives.PerspectiveAdapter;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.ViewFactory;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
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

  JCheckBoxMenuItem jcbSyncTableTree;

  public JCheckBoxMenuItem jcbmiRepeat;

  public JCheckBoxMenuItem jcbmiShuffle;

  public JCheckBoxMenuItem jcbmiContinue;

  public JCheckBoxMenuItem jcbmiIntro;

  JMenu smart;

  JMenuItem jmiShuffle;

  JMenuItem jmiBestof;

  JMenuItem jmiNovelties;

  JMenuItem jmiFinishAlbum;

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

  JButton jbSlim;

  /** Hashmap JCheckBoxMenuItem -> associated view */
  public HashMap hmCheckboxView = new HashMap(10);

  private JajukJMenuBar() {
    setOpaque(true);
    setAlignmentX(0.0f);
    // File menu
    file = new JMenu(Messages.getString("JajukJMenuBar.0"));

    jmiFileExit = new JMenuItem(ActionManager.getAction(JajukActions.EXIT));
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
        .getAction(JajukActions.ALL_VIEW_RESTORE_DEFAULTS));

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

    mode.add(jcbmiRepeat);
    mode.add(jcbmiShuffle);
    mode.add(jcbmiContinue);
    mode.add(jcbmiIntro);

    // Smart Menu
    smart = new JMenu(Messages.getString("JajukJMenuBar.29"));
    jmiShuffle = new SizedJMenuItem(ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL));
    jmiBestof = new SizedJMenuItem(ActionManager.getAction(JajukActions.BEST_OF));
    jmiNovelties = new SizedJMenuItem(ActionManager.getAction(JajukActions.NOVELTIES));
    jmiFinishAlbum = new SizedJMenuItem(ActionManager.getAction(JajukActions.FINISH_ALBUM));

    smart.add(jmiShuffle);
    smart.add(jmiBestof);
    smart.add(jmiNovelties);
    smart.add(jmiFinishAlbum);

    // Tools Menu
    tools = new JMenu(Messages.getString("JajukJMenuBar.28"));
    tools.addMouseMotionListener(this);
    jmiduplicateFinder = new JMenuItem(ActionManager.getAction(JajukActions.FIND_DUPLICATE_FILES));
    jmialarmClock = new JMenuItem(ActionManager.getAction(JajukActions.ALARM_CLOCK));
    jmReminders = new JMenu(Messages.getString("AlarmClock.1"));
    jmReminders.addMouseMotionListener(this);
    for (final Alarm alarm : AlarmManager.getInstance().getAllAlarms()) {
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
    jmiWebradios = new JMenuItem(ActionManager.getAction(JajukActions.CONFIGURE_WEBRADIOS));
    jmiWebradios.setIcon(IconLoader.ICON_WEBRADIO_16x16);
    jmiWizard = new JMenuItem(ActionManager.getAction(SIMPLE_DEVICE_WIZARD));
    jmiOptions = new JMenuItem(ActionManager.getAction(OPTIONS));

    jmiUnmounted = new JCheckBoxMenuItem(ActionManager.getAction(JajukActions.UNMOUNTED));
    jmiUnmounted.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED));
    jmiUnmounted.putClientProperty(DETAIL_ORIGIN, jmiUnmounted);

    jcbShowPopups = new JCheckBoxMenuItem(Messages.getString("ParameterView.228"));
    jcbShowPopups.setSelected(ConfigurationManager.getBoolean(CONF_SHOW_POPUPS));
    jcbShowPopups.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ConfigurationManager.setProperty(CONF_SHOW_POPUPS, Boolean.toString(jcbShowPopups
            .isSelected()));
        // force parameter view to take this into account
        ObservationManager.notify(new Event(JajukEvents.EVENT_PARAMETERS_CHANGE));
      }
    });

    jcbSyncTableTree = new JCheckBoxMenuItem(Messages.getString("ParameterView.183"));
    jcbSyncTableTree.setToolTipText(Messages.getString("ParameterView.184"));
    jcbSyncTableTree.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE));
    jcbSyncTableTree.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ConfigurationManager.setProperty(CONF_OPTIONS_SYNC_TABLE_TREE, Boolean
            .toString(jcbSyncTableTree.isSelected()));
        // force parameter view to take this into account
        ObservationManager.notify(new Event(JajukEvents.EVENT_PARAMETERS_CHANGE));
      }
    });

    configuration.add(jmiUnmounted);
    configuration.add(jcbShowPopups);
    configuration.add(jcbSyncTableTree);
    configuration.addSeparator();
    configuration.add(jmiDJ);
    configuration.add(jmiAmbience);
    configuration.add(jmiWebradios);
    configuration.add(jmiWizard);
    configuration.add(jmiOptions);

    // Help menu
    String helpText = Messages.getString("JajukJMenuBar.14");
    help = new JMenu(ActionUtil.strip(helpText));
    help.setMnemonic(ActionUtil.getMnemonic(helpText));
    jmiHelp = new JMenuItem(ActionManager.getAction(HELP_REQUIRED));
    jmiAbout = new JMenuItem(ActionManager.getAction(SHOW_ABOUT));
    jmiTraces = new JMenuItem(ActionManager.getAction(SHOW_TRACES));
    jmiTraces = new JMenuItem(ActionManager.getAction(SHOW_TRACES));
    jmiCheckforUpdates = new JMenuItem(ActionManager.getAction(JajukActions.CHECK_FOR_UPDATES));
    jmiTipOfTheDay = new JMenuItem(ActionManager.getAction(TIP_OF_THE_DAY));

    help.add(jmiHelp);
    help.add(jmiTipOfTheDay);
    // Install this action only if Desktop class is supported, it is used to
    // open default mail client
    if (Desktop.isDesktopSupported()) {
      jmiQualityAgent = new JMenuItem(ActionManager.getAction(QUALITY));
      help.add(jmiQualityAgent);
    }
    help.add(jmiTraces);
    help.add(jmiCheckforUpdates);
    help.add(jmiAbout);

    JMenuBar mainmenu = new JMenuBar();
    mainmenu.add(file);
    mainmenu.add(views);
    mainmenu.add(properties);
    mainmenu.add(mode);
    mainmenu.add(smart);
    mainmenu.add(tools);
    mainmenu.add(configuration);
    mainmenu.add(help);

    jbSlim = new JajukButton(ActionManager.getAction(JajukActions.SLIM_JAJUK));

    setLayout(new BorderLayout());
    add(mainmenu, BorderLayout.WEST);
    add(jbSlim, BorderLayout.EAST);

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
          jlUpdate = new JLabel(" ", IconLoader.ICON_UPDATE_MANAGER, JLabel.RIGHT);
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

  public Set<JajukEvents> getRegistrationKeys() {
    HashSet<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.EVENT_PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  public void mouseMoved(MouseEvent e) {
    if (e.getSource() == jmReminders) {
      jmReminders.removeAll();
      if (AlarmManager.getInstance().getAllAlarms().size() == 0)
        jmReminders.add(Messages.getString("AlarmClock.2"));
      else {
        for (final Alarm alarm : AlarmManager.getInstance().getAllAlarms()) {
          JMenuItem jma = new JMenuItem(alarm.getAlarmText(), IconLoader.ICON_ALARM);
          jma.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
              int iResu = Messages.getChoice(Messages.getString("Confirmation_alarm_stop"),
                  JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
              if (iResu != JOptionPane.YES_OPTION) {
                return;
              }
              AlarmManager.getInstance().stopAlarm(alarm);
            }
          });
          jmReminders.add(jma);
        }
      }
    }
    tools.repaint();
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
    if (JajukEvents.EVENT_PARAMETERS_CHANGE.equals(event.getSubject())) {
      jcbShowPopups.setSelected(ConfigurationManager.getBoolean(CONF_SHOW_POPUPS));
      jmiUnmounted.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED));
      jcbSyncTableTree.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE));
    }
  }
}
