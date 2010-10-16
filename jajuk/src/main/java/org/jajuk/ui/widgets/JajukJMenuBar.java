/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision$
 */
package org.jajuk.ui.widgets;

import static org.jajuk.ui.actions.JajukActions.CONFIGURE_AMBIENCES;
import static org.jajuk.ui.actions.JajukActions.CONFIGURE_DJS;
import static org.jajuk.ui.actions.JajukActions.CONTINUE_MODE;
import static org.jajuk.ui.actions.JajukActions.CUSTOM_PROPERTIES_ADD;
import static org.jajuk.ui.actions.JajukActions.CUSTOM_PROPERTIES_REMOVE;
import static org.jajuk.ui.actions.JajukActions.EXTRA_TAGS_WIZARD;
import static org.jajuk.ui.actions.JajukActions.HELP_REQUIRED;
import static org.jajuk.ui.actions.JajukActions.INTRO_MODE;
import static org.jajuk.ui.actions.JajukActions.OPTIONS;
import static org.jajuk.ui.actions.JajukActions.QUALITY;
import static org.jajuk.ui.actions.JajukActions.REPEAT_ALL_MODE;
import static org.jajuk.ui.actions.JajukActions.REPEAT_MODE;
import static org.jajuk.ui.actions.JajukActions.SHOW_ABOUT;
import static org.jajuk.ui.actions.JajukActions.SHOW_DONATE;
import static org.jajuk.ui.actions.JajukActions.SHOW_TRACES;
import static org.jajuk.ui.actions.JajukActions.SHUFFLE_MODE;
import static org.jajuk.ui.actions.JajukActions.SIMPLE_DEVICE_WIZARD;
import static org.jajuk.ui.actions.JajukActions.TIP_OF_THE_DAY;
import static org.jajuk.ui.actions.JajukActions.VIEW_RESTORE_DEFAULTS;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.core.SessionService;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.ActionUtil;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.perspectives.PerspectiveAdapter;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.ViewFactory;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Jajuk menu bar
 * <p>
 * Singleton.
 */
public final class JajukJMenuBar extends JMenuBar implements Observer {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The singleton. */
  static JajukJMenuBar jjmb = new JajukJMenuBar();

  /** DOCUMENT_ME. */
  JMenu file;

  /** DOCUMENT_ME. */
  JMenuItem jmiFileExit;

  /** DOCUMENT_ME. */
  JMenu views;

  /** DOCUMENT_ME. */
  JMenuItem jmiRestoreDefaultViews;

  /** DOCUMENT_ME. */
  JMenuItem jmiRestoreDefaultViewsAllPerpsectives;

  /** DOCUMENT_ME. */
  JMenu properties;

  /** DOCUMENT_ME. */
  JMenuItem jmiNewProperty;

  /** DOCUMENT_ME. */
  JMenuItem jmiRemoveProperty;

  /** DOCUMENT_ME. */
  JMenu mode;

  /** DOCUMENT_ME. */
  JCheckBoxMenuItem jcbShowPopups;

  /** DOCUMENT_ME. */
  JCheckBoxMenuItem jcbNoneInternetAccess;

  /** DOCUMENT_ME. */
  private final JCheckBoxMenuItem jcbmiRepeat;

  /** DOCUMENT_ME. */
  private final JCheckBoxMenuItem jcbmiShuffle;

  /** DOCUMENT_ME. */
  private final JCheckBoxMenuItem jcbmiContinue;

  /** DOCUMENT_ME. */
  private final JCheckBoxMenuItem jcbmiIntro;

  /** DOCUMENT_ME. */
  private final JCheckBoxMenuItem jcbmiKaraoke;

  /** DOCUMENT_ME. */
  JMenuBar mainmenu;

  /** DOCUMENT_ME. */
  JMenu smart;

  /** DOCUMENT_ME. */
  JMenuItem jmiShuffle;

  /** DOCUMENT_ME. */
  JMenuItem jmiBestof;

  /** DOCUMENT_ME. */
  JMenuItem jmiNovelties;

  /** DOCUMENT_ME. */
  JMenuItem jmiFinishAlbum;

  /** DOCUMENT_ME. */
  JMenu tools;

  /** DOCUMENT_ME. */
  JMenuItem jmiduplicateFinder;

  /** DOCUMENT_ME. */
  JMenuItem jmialarmClock;

  /** DOCUMENT_ME. */
  JMenuItem jmiprepareParty;

  /** DOCUMENT_ME. */
  JMenu configuration;

  /** DOCUMENT_ME. */
  JMenuItem jmiDJ;

  /** DOCUMENT_ME. */
  JMenuItem jmiAmbience;

  /** DOCUMENT_ME. */
  JMenuItem jmiWebradios;

  /** DOCUMENT_ME. */
  JMenuItem jmiWizard;

  /** DOCUMENT_ME. */
  JMenuItem jmiOptions;

  /** DOCUMENT_ME. */
  JCheckBoxMenuItem jmiUnmounted;

  /** DOCUMENT_ME. */
  JMenu help;

  /** DOCUMENT_ME. */
  JMenuItem jmiHelp;

  /** DOCUMENT_ME. */
  JMenuItem jmiTipOfTheDay;

  /** DOCUMENT_ME. */
  JMenuItem jmiQualityAgent;

  /** DOCUMENT_ME. */
  JMenuItem jmiTraces;

  /** DOCUMENT_ME. */
  JMenuItem jmiCheckforUpdates;

  /** DOCUMENT_ME. */
  JMenuItem jmiAbout;

  /** DOCUMENT_ME. */
  JLabel jlUpdate;

  /** DOCUMENT_ME. */
  JButton jbGC;

  /** DOCUMENT_ME. */
  JButton jbCommit;

  /** DOCUMENT_ME. */
  JButton jbSlim;

  /** DOCUMENT_ME. */
  private JajukButton jbFull = null;

  /** DOCUMENT_ME. */
  private final JCheckBoxMenuItem jcbmiRepeatAll;

  /** The jmi activate tags. */
  private JMenuItem jmiActivateTags;

  private JMenuItem jmiDonate;

  /**
   * Instantiates a new jajuk j menu bar.
   */
  private JajukJMenuBar() {
    setAlignmentX(0.0f);
    // File menu
    file = new JMenu(Messages.getString("JajukJMenuBar.0"));

    jmiFileExit = new JMenuItem(ActionManager.getAction(JajukActions.EXIT));
    file.add(jmiFileExit);

    // Properties menu
    properties = new JMenu(Messages.getString("JajukJMenuBar.5"));

    jmiNewProperty = new JMenuItem(ActionManager.getAction(CUSTOM_PROPERTIES_ADD));
    jmiRemoveProperty = new JMenuItem(ActionManager.getAction(CUSTOM_PROPERTIES_REMOVE));
    jmiActivateTags = new JMenuItem(ActionManager.getAction(EXTRA_TAGS_WIZARD));
    properties.add(jmiNewProperty);
    properties.add(jmiRemoveProperty);
    properties.add(jmiActivateTags);

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
    for (final Class<? extends IView> view : ViewFactory.getKnownViews()) {
      JMenuItem jmi = null;
      try {
        jmi = new JMenuItem(view.newInstance().getDesc(), IconLoader.getIcon(JajukIcons.LOGO_FRAME));
      } catch (Exception e1) {
        Log.error(e1);
        continue;
      }
      jmi.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // Simply add the new view in the current perspective
          PerspectiveAdapter current = (PerspectiveAdapter) PerspectiveManager
              .getCurrentPerspective();
          IView newView = ViewFactory.createView(view, current,
              (int) (Integer.MAX_VALUE * UtilSystem.getRandom().nextDouble()));
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

    jcbmiRepeat = new JCheckBoxMenuItem(ActionManager.getAction(REPEAT_MODE));
    jcbmiRepeat.setSelected(Conf.getBoolean(Const.CONF_STATE_REPEAT));
    jcbmiRepeatAll = new JCheckBoxMenuItem(ActionManager.getAction(REPEAT_ALL_MODE));
    jcbmiRepeatAll.setSelected(Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL));
    jcbmiShuffle = new JCheckBoxMenuItem(ActionManager.getAction(SHUFFLE_MODE));
    jcbmiShuffle.setSelected(Conf.getBoolean(Const.CONF_STATE_SHUFFLE));
    jcbmiContinue = new JCheckBoxMenuItem(ActionManager.getAction(CONTINUE_MODE));
    jcbmiContinue.setSelected(Conf.getBoolean(Const.CONF_STATE_CONTINUE));
    jcbmiIntro = new JCheckBoxMenuItem(ActionManager.getAction(INTRO_MODE));
    jcbmiIntro.setSelected(Conf.getBoolean(Const.CONF_STATE_INTRO));
    jcbmiKaraoke = new JCheckBoxMenuItem(ActionManager.getAction(JajukActions.KARAOKE_MODE));
    jcbmiKaraoke.setSelected(Conf.getBoolean(Const.CONF_STATE_KARAOKE));

    mode.add(jcbmiRepeat);
    mode.add(jcbmiRepeatAll);
    mode.add(jcbmiShuffle);
    mode.add(jcbmiContinue);
    mode.add(jcbmiIntro);
    mode.add(jcbmiKaraoke);

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
    jmiduplicateFinder = new JMenuItem(ActionManager.getAction(JajukActions.FIND_DUPLICATE_FILES));
    jmialarmClock = new JMenuItem(ActionManager.getAction(JajukActions.ALARM_CLOCK));
    jmiprepareParty = new JMenuItem(ActionManager.getAction(JajukActions.PREPARE_PARTY));
    tools.add(jmiduplicateFinder);
    tools.add(jmialarmClock);
    tools.add(jmiprepareParty);
    // tools.addSeparator();

    // Configuration menu
    configuration = new JMenu(Messages.getString("JajukJMenuBar.21"));
    jmiDJ = new JMenuItem(ActionManager.getAction(CONFIGURE_DJS));
    // Overwrite default icon
    jmiDJ.setIcon(IconLoader.getIcon(JajukIcons.DIGITAL_DJ_16X16));
    jmiAmbience = new JMenuItem(ActionManager.getAction(CONFIGURE_AMBIENCES));
    jmiWebradios = new JMenuItem(ActionManager.getAction(JajukActions.CONFIGURE_WEBRADIOS));
    jmiWebradios.setIcon(IconLoader.getIcon(JajukIcons.WEBRADIO_16X16));
    jmiWizard = new JMenuItem(ActionManager.getAction(SIMPLE_DEVICE_WIZARD));
    jmiOptions = new JMenuItem(ActionManager.getAction(OPTIONS));

    jmiUnmounted = new JCheckBoxMenuItem(ActionManager.getAction(JajukActions.UNMOUNTED));
    jmiUnmounted.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED));
    jmiUnmounted.putClientProperty(Const.DETAIL_ORIGIN, jmiUnmounted);

    jcbShowPopups = new JCheckBoxMenuItem(Messages.getString("ParameterView.228"));
    jcbShowPopups.setSelected(Conf.getBoolean(Const.CONF_SHOW_POPUPS));
    jcbShowPopups.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Conf.setProperty(Const.CONF_SHOW_POPUPS, Boolean.toString(jcbShowPopups.isSelected()));
        // force parameter view to take this into account
        ObservationManager.notify(new JajukEvent(JajukEvents.PARAMETERS_CHANGE));
      }
    });

    jcbNoneInternetAccess = new JCheckBoxMenuItem(Messages.getString("ParameterView.264"));
    jcbNoneInternetAccess.setToolTipText(Messages.getString("ParameterView.265"));
    jcbNoneInternetAccess.setSelected(Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS));
    jcbNoneInternetAccess.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, Boolean
            .toString(jcbNoneInternetAccess.isSelected()));
        // force parameter view to take this into account
        ObservationManager.notify(new JajukEvent(JajukEvents.PARAMETERS_CHANGE));
      }
    });

    configuration.add(jmiUnmounted);
    configuration.add(jcbShowPopups);
    configuration.add(jcbNoneInternetAccess);
    configuration.addSeparator();
    configuration.add(jmiDJ);
    configuration.add(jmiAmbience);
    configuration.add(jmiWebradios);
    configuration.add(jmiWizard);
    configuration.add(jmiOptions);

    // Help menu
    String helpText = Messages.getString("JajukJMenuBar.14");
    help = new JMenu(ActionUtil.strip(helpText));
    jmiHelp = new JMenuItem(ActionManager.getAction(HELP_REQUIRED));
    jmiDonate = new JMenuItem(ActionManager.getAction(SHOW_DONATE));
    jmiAbout = new JMenuItem(ActionManager.getAction(SHOW_ABOUT));
    jmiTraces = new JMenuItem(ActionManager.getAction(SHOW_TRACES));
    jmiTraces = new JMenuItem(ActionManager.getAction(SHOW_TRACES));
    jmiCheckforUpdates = new JMenuItem(ActionManager.getAction(JajukActions.CHECK_FOR_UPDATES));
    jmiTipOfTheDay = new JMenuItem(ActionManager.getAction(TIP_OF_THE_DAY));

    help.add(jmiHelp);
    help.add(jmiTipOfTheDay);
    // Install this action only if Desktop class is supported, it is used to
    // open default mail client
    if (UtilSystem.isBrowserSupported()) {
      jmiQualityAgent = new JMenuItem(ActionManager.getAction(QUALITY));
      help.add(jmiQualityAgent);
    }
    help.add(jmiTraces);
    help.add(jmiCheckforUpdates);
    help.add(jmiDonate);
    help.add(jmiAbout);

    mainmenu = new JMenuBar();
    mainmenu.add(file);
    mainmenu.add(views);
    mainmenu.add(properties);
    mainmenu.add(mode);
    mainmenu.add(smart);
    mainmenu.add(tools);
    mainmenu.add(configuration);
    mainmenu.add(help);

    // Apply mnemonics (Alt + first char of the menu keystroke)
    applyMnemonics();

    if (SessionService.isTestMode()) {
      jbCommit = new JajukButton(ActionManager.getAction(JajukActions.COMMIT));
      jbGC = new JajukButton(ActionManager.getAction(JajukActions.GC));
    }
    jbSlim = new JajukButton(ActionManager.getAction(JajukActions.SLIM_JAJUK));
    jbFull = new JajukButton(ActionManager.getAction(JajukActions.FULLSCREEN_JAJUK));

    JMenuBar eastmenu = new JMenuBar();
    // only show GC-button in test-mode
    if (SessionService.isTestMode()) {
      eastmenu.add(jbCommit);
      eastmenu.add(jbGC);
    }
    eastmenu.add(jbSlim);
    eastmenu.add(jbFull);

    setLayout(new BorderLayout());
    add(mainmenu, BorderLayout.WEST);
    add(eastmenu, BorderLayout.EAST);

    // Check for new release and display the icon if a new release is available
    SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

      @Override
      public Void doInBackground() {
        UpgradeManager.checkForUpdate();
        return null;
      }

      @Override
      public void done() {
        // add the new release label if required
        if (UpgradeManager.getNewVersionName() != null) {
          jlUpdate = new JLabel(" ", IconLoader.getIcon(JajukIcons.UPDATE_MANAGER),
              SwingConstants.RIGHT);
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
    // Search online for upgrade if the option is set and if the none Internet
    // access option is not set
    if (Conf.getBoolean(Const.CONF_CHECK_FOR_UPDATE)
        && !Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      sw.execute();
    }
    ObservationManager.register(this);
  }

  /**
   * Gets the single instance of JajukJMenuBar.
   * 
   * @return single instance of JajukJMenuBar
   */
  public static JajukJMenuBar getInstance() {
    return jjmb;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    eventSubjectSet.add(JajukEvents.SLIMBAR_VISIBILTY_CHANGED);
    return eventSubjectSet;
  }

  /**
   * Apply all mnemonics for all menus (follow i18n).
   */
  private void applyMnemonics() {
    for (int i = 0; i < mainmenu.getMenuCount(); i++) {
      JMenu menu = mainmenu.getMenu(i);
      if (menu != null && StringUtils.isNotBlank(menu.getText())) {
        String label = menu.getText();
        int mnemonic = label.getBytes()[0];
        menu.setMnemonic(mnemonic);
      }
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
        if (JajukEvents.PARAMETERS_CHANGE.equals(event.getSubject())
            || JajukEvents.SLIMBAR_VISIBILTY_CHANGED.equals(event.getSubject())) {
          jcbShowPopups.setSelected(Conf.getBoolean(Const.CONF_SHOW_POPUPS));
          jmiUnmounted.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED));
          jcbNoneInternetAccess.setSelected(Conf
              .getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS));
        }
      }

    });
  }

  /**
   * Sets the repeat selected.
   * 
   * @param b the new repeat selected
   */
  public void setRepeatSelected(final boolean b) {
    jcbmiRepeat.setSelected(b);
  }

  /**
   * Sets the repeat all selected.
   * 
   * @param b the new repeat all selected
   */
  public void setRepeatAllSelected(final boolean b) {
    jcbmiRepeatAll.setSelected(b);
  }

  /**
   * Sets the shuffle selected.
   * 
   * @param b the new shuffle selected
   */
  public void setShuffleSelected(final boolean b) {
    jcbmiShuffle.setSelected(b);
  }

  /**
   * Sets the continue selected.
   * 
   * @param b the new continue selected
   */
  public void setContinueSelected(final boolean b) {
    jcbmiContinue.setSelected(b);
  }

  /**
   * Sets the intro selected.
   * 
   * @param b the new intro selected
   */
  public void setIntroSelected(final boolean b) {
    jcbmiIntro.setSelected(b);
  }

  /**
   * Sets the karaoke selected.
   * 
   * @param b the new karaoke selected
   */
  public void setKaraokeSelected(final boolean b) {
    jcbmiKaraoke.setSelected(b);
  }

}
