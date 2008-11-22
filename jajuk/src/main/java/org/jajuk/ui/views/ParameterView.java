/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision: 3010 $$
 */

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.jajuk.Main;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.core.RatingManager;
import org.jajuk.services.lastfm.LastFmManager;
import org.jajuk.ui.helpers.DefaultMouseWheelListener;
import org.jajuk.ui.helpers.PatternInputVerifier;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.ui.thumbnails.ThumbnailsMaker;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.PathSelector;
import org.jajuk.ui.widgets.SearchBox;
import org.jajuk.ui.widgets.SteppedComboBox;
import org.jajuk.ui.widgets.ToggleLink;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.VerticalLayout;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.skin.SkinInfo;

/**
 * View used to set Jajuk parameters.
 * <p>
 * Configuration perspective *
 */
public class ParameterView extends ViewAdapter implements ActionListener, ListSelectionListener,
    ItemListener, ChangeListener {

  private static final long serialVersionUID = 1L;

  JTabbedPane jtpMain;

  JPanel jpHistory;

  JLabel jlHistory;

  JTextField jtfHistory;

  JButton jbClearHistory;

  JButton jbResetRatings;

  JButton jbResetPreferences;

  JPanel jpStart;

  JLabel jlStart;

  ButtonGroup bgStart;

  JRadioButton jrbNothing;

  JRadioButton jrbLast;

  JRadioButton jrbLastKeepPos;

  JRadioButton jrbShuffle;

  JRadioButton jrbBestof;

  JRadioButton jrbNovelties;

  JRadioButton jrbFile;

  SearchBox sbSearch;

  JPanel jpConfirmations;

  JCheckBox jcbBeforeDelete;

  JCheckBox jcbBeforeExit;

  JCheckBox jcbBeforeRemoveDevice;

  JCheckBox jcbBeforeDeleteCover;

  JCheckBox jcbBeforeClearingHistory;

  JCheckBox jcbBeforeResetingRatings;

  JCheckBox jcbBeforeRefactorFiles;

  JPanel jpOptions;

  JCheckBox jcbDisplayUnmounted;

  JCheckBox jcbSyncTableTree;

  JCheckBox jcbAudioScrobbler;

  JLabel jlASUser;

  JTextField jtfASUser;

  JLabel jlASPassword;

  JPasswordField jpfASPassword;

  JLabel jlLanguage;

  SteppedComboBox scbLanguage;

  JLabel jlFrameTitle;

  JTextField jtfFrameTitle;

  JLabel jlLAF;

  SteppedComboBox scbLAF;

  JLabel jlLogLevel;

  SteppedComboBox scbLogLevel;

  JLabel jlIntroPosition;

  JSlider introPosition;

  JLabel jlIntroLength;

  JSlider introLength;

  JLabel jlBestofSize;

  JTextField jtfBestofSize;

  JLabel jlNoveltiesAge;

  JTextField jtfNoveltiesAge;

  JLabel jlVisiblePlanned;

  JTextField jtfVisiblePlanned;

  JLabel jlCrossFadeDuration;

  JSlider crossFadeDuration;

  JCheckBox jcbDefaultActionClick;

  JCheckBox jcbDefaultActionDrop;

  JCheckBox jcbShowBaloon;

  JCheckBox jcbHotkeys;

  JPanel jpTags;

  JCheckBox jcbUseParentDir;

  JLabel jlRefactorPattern;

  JFormattedTextField jtfRefactorPattern;

  JLabel jlAnimationPattern;

  JTextField jtfAnimationPattern;

  JPanel jpAdvanced;

  JCheckBox jcbBackup;

  JSlider backupSize;

  JLabel jlCollectionEncoding;

  JComboBox jcbCollectionEncoding;

  JCheckBox jcbRegexp;

  JPanel jpNetwork;

  ButtonGroup bgProxy;

  JCheckBox jcbProxyNone;

  JCheckBox jcbProxyHttp;

  JCheckBox jcbProxySocks;

  JLabel jlProxyHostname;

  JTextField jtfProxyHostname;

  JLabel jlProxyPort;

  JTextField jtfProxyPort;

  JLabel jlProxyLogin;

  JTextField jtfProxyLogin;

  JLabel jlProxyPwd;

  JPasswordField jtfProxyPwd;

  JLabel jlConnectionTO;

  JSlider connectionTO;

  JPanel jpCovers;

  JCheckBox jcbAutoCover;

  JCheckBox jcbShuffleCover;

  JLabel jlCoverSize;

  JComboBox jcbCoverSize;

  JLabel jlMPlayerArgs;

  JTextField jtfMPlayerArgs;

  JLabel jlEnvVariables;

  JTextField jtfEnvVariables;

  JLabel jlMPlayerPath;

  JTextField jtfMPlayerPath;

  JLabel jlJajukWorkspace;

  PathSelector psJajukWorkspace;

  JLabel jlCatalogPages;

  JSlider jsCatalogPages;

  JButton jbCatalogRefresh;

  JCheckBox jcbShowPopups;

  JPanel jpUI;

  JLabel jlFonts;

  JSlider jsFonts;

  JPanel jpLastFM;

  JCheckBox jcbEnableLastFMInformation;

  JPanel jpOKCancel;

  JButton jbOK;

  JButton jbDefault;

  JPanel jpModes;

  JCheckBox jcbCheckUpdates;

  JCheckBox jcbForceFileDate;

  JSlider jsPerspectiveSize;

  private boolean someOptionsAppliedAtNextStartup = false;

  /**
   * whether the "theme will be token into account" message has been already
   * displayed
   */
  boolean bLAFMessage = false;

  /**
   * 
   */
  public ParameterView() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    new Thread() {
      @Override
      public void run() {
        if (e.getSource() == jbClearHistory) {
          // show confirmation message if required
          if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_CLEAR_HISTORY)) {
            final int iResu = Messages.getChoice(Messages.getString("Confirmation_clear_history"),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (iResu != JOptionPane.YES_OPTION) {
              return;
            }
          }
          ObservationManager.notify(new Event(JajukEvents.CLEAR_HISTORY));
        } else if (e.getSource() == jbResetRatings) {
          // show confirmation message if required
          if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_RESET_RATINGS)) {
            final int iResu = Messages.getChoice(Messages.getString("Confirmation_reset_ratings"),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (iResu != JOptionPane.YES_OPTION) {
              return;
            }
          }
          if (!DeviceManager.getInstance().isAnyDeviceRefreshing()) {
            ObservationManager.notify(new Event(JajukEvents.RATE_RESET));
          } else {
            Messages.showErrorMessage(120);
          }
        } else if (e.getSource() == jbResetPreferences) {
          // show confirmation message if required
          if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_RESET_RATINGS)) {
            final int iResu = Messages.getChoice(Messages
                .getString("Confirmation_reset_preferences"), JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (iResu != JOptionPane.YES_OPTION) {
              return;
            }
          }
          if (!DeviceManager.getInstance().isAnyDeviceRefreshing()) {
            ObservationManager.notify(new Event(JajukEvents.PREFERENCES_RESET));
          } else {
            Messages.showErrorMessage(120);
          }
        } else if (e.getSource() == jbOK) {
          applyParameters();
          // Notify any client than wait for parameters updates
          final Properties details = new Properties();
          details.put(Const.DETAIL_ORIGIN, this);
          if (someOptionsAppliedAtNextStartup) {
            // Inform user that some parameters will apply only at next startup
            Messages.showInfoMessage(Messages.getString("ParameterView.198"));
            someOptionsAppliedAtNextStartup = false;
          }
        } else if (e.getSource() == jbDefault) {
          int resu = Messages.getChoice(Messages.getString("Confirmation_defaults"),
              JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
          if (resu == JOptionPane.OK_OPTION) {
            Conf.setDefaultProperties();
            updateSelection();// update UI
            InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.110"),
                InformationJPanel.INFORMATIVE);
            applyParameters();
            Messages.showInfoMessage(Messages.getString("ParameterView.198"));
          }
        } else if (e.getSource() == jcbBackup) {
          // if backup option is unchecked, reset backup size
          if (jcbBackup.isSelected()) {
            backupSize.setEnabled(true);
            backupSize.setValue(Conf.getInt(Const.CONF_BACKUP_SIZE));
          } else {
            backupSize.setEnabled(false);
            backupSize.setValue(0);
          }
        } else if ((e.getSource() == jcbProxyNone) || (e.getSource() == jcbProxyHttp)
            || (e.getSource() == jcbProxySocks)) {
          final boolean bUseProxy = !jcbProxyNone.isSelected();
          jtfProxyHostname.setEnabled(bUseProxy);
          jtfProxyPort.setEnabled(bUseProxy);
          jtfProxyLogin.setEnabled(bUseProxy);
          jtfProxyPwd.setEnabled(bUseProxy);
          jlProxyHostname.setEnabled(bUseProxy);
          jlProxyPort.setEnabled(bUseProxy);
          jlProxyLogin.setEnabled(bUseProxy);
          jlProxyPwd.setEnabled(bUseProxy);
        } else if (e.getSource() == jcbAutoCover) {
          if (jcbAutoCover.isSelected()) {
            jcbCoverSize.setEnabled(true);
            jlCoverSize.setEnabled(true);
          } else {
            jlCoverSize.setEnabled(false);
            jcbCoverSize.setEnabled(false);
          }
        } else if (e.getSource() == jcbAudioScrobbler) {
          if (jcbAudioScrobbler.isSelected()) {
            jlASUser.setEnabled(true);
            jtfASUser.setEnabled(true);
            jlASPassword.setEnabled(true);
            jpfASPassword.setEnabled(true);
          } else {
            jlASUser.setEnabled(false);
            jtfASUser.setEnabled(false);
            jlASPassword.setEnabled(false);
            jpfASPassword.setEnabled(false);
          }
        } else if (e.getSource() == scbLanguage) {
          final String sLocal = Messages.getLocalForDesc(((JLabel) scbLanguage.getSelectedItem())
              .getText());
          final String sPreviousLocal = Messages.getLocale();
          if (!sPreviousLocal.equals(sLocal)) {
            // local has changed
            someOptionsAppliedAtNextStartup = true;
          }
        } else if (e.getSource() == jcbHotkeys) {
          someOptionsAppliedAtNextStartup = true;
        } else if (e.getSource() == jbCatalogRefresh) {
          int resu = Messages.getChoice(Messages.getString("Confirmation_rebuild_thumbs"),
              JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
          if (resu != JOptionPane.YES_OPTION) {
            return;
          }
          ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_50X50);
          ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_100X100);
          ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_150X150);
          ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_200X200);
          ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_250X250);
          ThumbnailManager.cleanThumbs(THUMBNAIL_SIZE_300X300);
          // Display the catalog view voided
          ObservationManager.notify(new Event(JajukEvents.DEVICE_REFRESH));
          // Launch thumbs creation in another process
          ThumbnailsMaker.launchAllSizes(true);
          // Display the catalog view filed
          ObservationManager.notify(new Event(JajukEvents.DEVICE_REFRESH));
        }
      }
    }.start();
  }

  private void applyParameters() {
    // **Read all parameters**
    // Options
    Conf.setProperty(Const.CONF_OPTIONS_HIDE_UNMOUNTED, Boolean.toString(jcbDisplayUnmounted
        .isSelected()));
    Conf.setProperty(Const.CONF_OPTIONS_PUSH_ON_CLICK, Boolean.toString(jcbDefaultActionClick
        .isSelected()));
    Conf.setProperty(Const.CONF_OPTIONS_DEFAULT_ACTION_DROP, Boolean.toString(jcbDefaultActionDrop
        .isSelected()));
    Conf.setProperty(Const.CONF_OPTIONS_SYNC_TABLE_TREE, Boolean.toString(jcbSyncTableTree
        .isSelected()));
    Conf.setProperty(Const.CONF_OPTIONS_HOTKEYS, Boolean.toString(jcbHotkeys.isSelected()));
    Conf.setProperty(Const.CONF_LASTFM_ENABLE, Boolean.toString(jcbAudioScrobbler.isSelected()));
    Conf.setProperty(Const.CONF_LASTFM_INFO, Boolean.toString(jcbEnableLastFMInformation
        .isSelected()));
    Conf.setProperty(Const.CONF_LASTFM_USER, jtfASUser.getText());
    Conf.setProperty(Const.CONF_LASTFM_PASSWORD, UtilString.rot13(new String(jpfASPassword
        .getPassword())));
    final int iLogLevel = scbLogLevel.getSelectedIndex();
    Log.setVerbosity(iLogLevel);
    Conf.setProperty(Const.CONF_OPTIONS_LOG_LEVEL, Integer.toString(iLogLevel));
    Conf.setProperty(Const.CONF_OPTIONS_INTRO_BEGIN, Integer.toString(introPosition.getValue()));
    Conf.setProperty(Const.CONF_OPTIONS_INTRO_LENGTH, Integer.toString(introLength.getValue()));
    Conf
        .setProperty(Const.CONF_TAGS_USE_PARENT_DIR, Boolean.toString(jcbUseParentDir.isSelected()));
    final String sBestofSize = jtfBestofSize.getText();
    if (!sBestofSize.equals("")) {
      Conf.setProperty(Const.CONF_BESTOF_TRACKS_SIZE, sBestofSize);
    }
    final String sLocal = Messages.getLocalForDesc(((JLabel) scbLanguage.getSelectedItem())
        .getText());
    Conf.setProperty(Const.CONF_OPTIONS_LANGUAGE, sLocal);
    // force refresh of bestof files
    RatingManager.setRateHasChanged(true);
    final String sNoveltiesAge = jtfNoveltiesAge.getText();
    if (!sNoveltiesAge.equals("")) {
      Conf.setProperty(Const.CONF_OPTIONS_NOVELTIES_AGE, sNoveltiesAge);
    }
    final String sVisiblePlanned = jtfVisiblePlanned.getText();
    if (!sVisiblePlanned.equals("")) {
      Conf.setProperty(Const.CONF_OPTIONS_VISIBLE_PLANNED, sVisiblePlanned);
    }
    final int oldDuration = Conf.getInt(Const.CONF_FADE_DURATION);
    // Show an hideable message if user set cross fade under linux for sound
    // server information
    if (UtilSystem.isUnderLinux() && (oldDuration == 0)
        && (oldDuration != crossFadeDuration.getValue())) {
      Messages.showHideableWarningMessage(Messages.getString("ParameterView.210"),
          Const.CONF_NOT_SHOW_AGAIN_CROSS_FADE);
    }
    Conf.setProperty(Const.CONF_FADE_DURATION, Integer.toString(crossFadeDuration.getValue()));
    // Startup
    if (jrbNothing.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_NOTHING);
    } else if (jrbLast.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST);
    } else if (jrbLastKeepPos.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST_KEEP_POS);
    } else if (jrbShuffle.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_SHUFFLE);
    } else if (jrbFile.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_FILE);
    } else if (jrbBestof.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_BESTOF);
    } else if (jrbNovelties.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_NOVELTIES);
    }
    // Confirmations
    Conf.setProperty(Const.CONF_CONFIRMATIONS_DELETE_FILE, Boolean.toString(jcbBeforeDelete
        .isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_EXIT, Boolean.toString(jcbBeforeExit.isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_REMOVE_DEVICE, Boolean.toString(jcbBeforeRemoveDevice
        .isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_DELETE_COVER, Boolean.toString(jcbBeforeDeleteCover
        .isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_CLEAR_HISTORY, Boolean
        .toString(jcbBeforeClearingHistory.isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_RESET_RATINGS, Boolean
        .toString(jcbBeforeResetingRatings.isSelected()));
    // History
    final String sHistoryDuration = jtfHistory.getText();
    if (!sHistoryDuration.equals("")) {
      Conf.setProperty(Const.CONF_HISTORY, sHistoryDuration);
    }
    // Patterns
    // Get and check reorg pattern
    final String sPattern = jtfRefactorPattern.getText();
    Conf.setProperty(Const.CONF_REFACTOR_PATTERN, sPattern);
    Conf.setProperty(Const.CONF_ANIMATION_PATTERN, jtfAnimationPattern.getText());
    Conf.setProperty(Const.CONF_FRAME_TITLE_PATTERN, jtfFrameTitle.getText());

    // Advanced
    Conf.setProperty(Const.CONF_BACKUP_SIZE, Integer.toString(backupSize.getValue()));
    Conf.setProperty(Const.CONF_COLLECTION_CHARSET, jcbCollectionEncoding.getSelectedItem()
        .toString());
    Conf.setProperty(Const.CONF_REGEXP, Boolean.toString(jcbRegexp.isSelected()));
    Conf.setProperty(Const.CONF_CHECK_FOR_UPDATE, Boolean.toString(jcbCheckUpdates.isSelected()));
    Conf.setProperty(Const.CONF_FORCE_FILE_DATE, Boolean.toString(jcbForceFileDate.isSelected()));
    // Apply new mplayer path and display a warning message if changed
    final String oldMplayerPath = Conf.getString(Const.CONF_MPLAYER_PATH_FORCED);
    if (!(oldMplayerPath.equals(jtfMPlayerPath.getText()))) {
      this.someOptionsAppliedAtNextStartup = true;
    }
    Conf.setProperty(Const.CONF_MPLAYER_PATH_FORCED, jtfMPlayerPath.getText());
    Conf.setProperty(Const.CONF_MPLAYER_ARGS, jtfMPlayerArgs.getText());
    Conf.setProperty(Const.CONF_ENV_VARIABLES, jtfEnvVariables.getText());
    // UI
    Conf.setProperty(Const.CONF_CATALOG_PAGE_SIZE, Integer.toString(jsCatalogPages.getValue()));
    Conf.setProperty(Const.CONF_SHOW_POPUPS, Boolean.toString(jcbShowPopups.isSelected()));
    final int oldFont = Conf.getInt(Const.CONF_FONTS_SIZE);
    // Display a message if font size changed
    if (oldFont != jsFonts.getValue()) {
      someOptionsAppliedAtNextStartup = true;
    }
    Conf.setProperty(Const.CONF_FONTS_SIZE, Integer.toString(jsFonts.getValue()));

    final int oldPerspectiveSize = Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE);
    // If we perspective size changed and no font message have been already
    // displayed, display a message
    if (oldPerspectiveSize != jsPerspectiveSize.getValue()) {
      someOptionsAppliedAtNextStartup = true;
    }
    Conf.setProperty(Const.CONF_PERSPECTIVE_ICONS_SIZE, Integer.toString(jsPerspectiveSize
        .getValue()));
    // LAF change
    final String oldTheme = Conf.getString(Const.CONF_OPTIONS_LNF);
    Conf.setProperty(Const.CONF_OPTIONS_LNF, (String) scbLAF.getSelectedItem());
    if (!oldTheme.equals(scbLAF.getSelectedItem())) {
      // theme will be applied at next startup
      Messages.showHideableWarningMessage(Messages.getString("ParameterView.233"),
          Const.CONF_NOT_SHOW_AGAIN_LAF_CHANGE);
      bLAFMessage = true;
    }
    // If jajuk home changes, write new path in bootstrap file
    if ((Main.getWorkspace() != null) && !Main.getWorkspace().equals(psJajukWorkspace.getUrl())) {
      // Check workspace directory
      if (!psJajukWorkspace.getUrl().trim().equals("")) {
        // Check workspace presence and create it if required
        try {
          final java.io.File fWorkspace = new java.io.File(psJajukWorkspace.getUrl());
          if (!fWorkspace.exists()) {
            if (!fWorkspace.mkdirs()) {
              Log.warn("Could not create directory " + fWorkspace.toString());
            }
          }
          if (!fWorkspace.canRead()) {
            throw new Exception("Cannot write to proposed Workspace");
          }
        } catch (final Exception e) {
          Messages.showErrorMessage(165);
          return;
        }
      }
      try {
        final String newWorkspace = psJajukWorkspace.getUrl();
        // If target workspace doesn't exist, copy current repository to
        // the new workspace
        // (keep old repository for security and for use
        // by others users in multi-session mode)
        boolean bPreviousPathExist = true;
        // bPreviousPathExist is true if destination workspace already exists,
        // it is then only a workspace switch
        if (!new java.io.File(psJajukWorkspace.getUrl() + '/'
            + (Main.isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk")).exists()) {
          UtilGUI.waiting();
          final java.io.File from = UtilSystem.getConfFileByPath("");
          final java.io.File dest = new java.io.File(newWorkspace + '/'
              + (Main.isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk"));
          UtilSystem.copyRecursively(from, dest);
          bPreviousPathExist = false;
          // Change the workspace so the very last conf (like current track)
          // will be saved directly to target workspace. We don't do this if the
          // workspace already exist to avoir overwritting other configuration.
          Main.setWorkspace(psJajukWorkspace.getUrl());
        }
        // OK, now write down the bootstrap file if
        // everything's OK
        final java.io.File bootstrap = new java.io.File(Const.FILE_BOOTSTRAP);
        final BufferedWriter bw = new BufferedWriter(new FileWriter(bootstrap));
        bw.write(newWorkspace);
        bw.flush();
        bw.close();
        UtilGUI.stopWaiting();
        // Display a warning message and restart Jajuk
        if (bPreviousPathExist) {
          Messages.showInfoMessage(Messages.getString("ParameterView.247"));
        } else {
          Messages.showInfoMessage(Messages.getString("ParameterView.209"));
        }
        // Exit Jajuk
        new Thread() {
          @Override
          public void run() {
            ExitService.exit(0);
          }
        }.start();
      } catch (final Exception e) {
        Messages.showErrorMessage(24);
        Log.debug("Cannot write bootstrap file");
      }
    }

    // Network
    Conf.setProperty(Const.CONF_NETWORK_USE_PROXY, Boolean.toString(!jcbProxyNone.isSelected()));
    if (jcbProxyHttp.isSelected()) {
      Conf.setProperty(Const.CONF_NETWORK_PROXY_TYPE, Const.PROXY_TYPE_HTTP);
    } else if (jcbProxySocks.isSelected()) {
      Conf.setProperty(Const.CONF_NETWORK_PROXY_TYPE, Const.PROXY_TYPE_SOCKS);
    }
    Conf.setProperty(Const.CONF_NETWORK_PROXY_HOSTNAME, jtfProxyHostname.getText());
    Conf.setProperty(Const.CONF_NETWORK_PROXY_PORT, jtfProxyPort.getText());
    Conf.setProperty(Const.CONF_NETWORK_PROXY_LOGIN, jtfProxyLogin.getText());
    Conf.setProperty(Const.CONF_NETWORK_PROXY_PWD, UtilString.rot13(new String(jtfProxyPwd
        .getPassword())));
    Conf.setProperty(Const.CONF_NETWORK_CONNECTION_TO, Integer.toString(connectionTO.getValue()));
    // Force global reload of proxy variables
    DownloadManager.setDefaultProxySettings();
    // Covers
    Conf.setProperty(Const.CONF_COVERS_AUTO_COVER, Boolean.toString(jcbAutoCover.isSelected()));
    Conf.setProperty(Const.CONF_COVERS_SHUFFLE, Boolean.toString(jcbShuffleCover.isSelected()));
    Conf.setProperty(Const.CONF_COVERS_SIZE, Integer.toString(jcbCoverSize.getSelectedIndex()));
    // Force LastFM manager configuration reload
    LastFmManager.getInstance().configure();

    // configuration
    try {
      Conf.commit();
    } catch (final Exception e) {
      Log.error(113, e);
      Messages.showErrorMessage(113);
    }
    // Force a full refresh (useful for catalog view for instance)
    ObservationManager.notify(new Event(JajukEvents.DEVICE_REFRESH));
    // display a message
    InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.109"),
        InformationJPanel.INFORMATIVE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("ParameterView.87");
  }

  public Set<JajukEvents> getRegistrationKeys() {
    final Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
    final double p = TableLayoutConstants.PREFERRED;
    final int iXSeparator = 15;
    final int iYSeparator = 15;

    // Use this common action listener for UI options that need to launch
    // event
    final ActionListener alUI = new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        // Store configuration
        Conf.setProperty(Const.CONF_UI_SHOW_BALLOON, Boolean.toString(jcbShowBaloon.isSelected()));
        Conf.setProperty(Const.CONF_SHOW_POPUPS, Boolean.toString(jcbShowPopups.isSelected()));
        Conf.setProperty(Const.CONF_OPTIONS_SYNC_TABLE_TREE, Boolean.toString(jcbSyncTableTree
            .isSelected()));
        // Launch an event that can be trapped by the tray to
        // synchronize the state
        Properties details = new Properties();
        details.put(Const.DETAIL_ORIGIN, ParameterView.this);
        ObservationManager.notify(new Event(JajukEvents.PARAMETERS_CHANGE, details));
      }

    };

    // --History
    jpHistory = new JPanel();

    final double sizeHistory[][] = { { p, p }, { p, p, p } };
    final TableLayout layoutHistory = new TableLayout(sizeHistory);
    layoutHistory.setHGap(iXSeparator);
    layoutHistory.setVGap(iYSeparator);
    jpHistory.setLayout(layoutHistory);
    jlHistory = new JLabel(Messages.getString("ParameterView.0"));
    jtfHistory = new JTextField();
    jtfHistory.setInputVerifier(new InputVerifier() {
      @Override
      public boolean shouldYieldFocus(final JComponent input) {
        return verify(input);
      }

      @Override
      public boolean verify(final JComponent input) {
        final JTextField tf = (JTextField) input;
        final String sText = tf.getText();
        try {
          final int iValue = Integer.parseInt(sText);
          if (iValue < -1) {
            jbOK.setEnabled(false);
            return false;
          }
        } catch (final Exception e) {
          jbOK.setEnabled(false);
          return false;
        }
        jbOK.setEnabled(true);
        return true;
      }
    });
    jtfHistory.setToolTipText(Messages.getString("ParameterView.2"));
    jbClearHistory = new JButton(Messages.getString("ParameterView.3"), IconLoader
        .getIcon(JajukIcons.CLEAR));
    jbClearHistory.setToolTipText(Messages.getString("ParameterView.4"));
    jbClearHistory.addActionListener(this);

    jbResetRatings = new JButton(Messages.getString("ParameterView.186"), IconLoader
        .getIcon(JajukIcons.CLEAR));
    jbResetRatings.setToolTipText(Messages.getString("ParameterView.187"));
    jbResetRatings.addActionListener(this);

    jbResetPreferences = new JButton(Messages.getString("ParameterView.249"), IconLoader
        .getIcon(JajukIcons.CLEAR));
    jbResetPreferences.setToolTipText(Messages.getString("ParameterView.250"));
    jbResetPreferences.addActionListener(this);

    jpHistory.add(jlHistory, "0,0");
    jpHistory.add(jtfHistory, "1,0");
    jpHistory.add(jbClearHistory, "0,1");
    jpHistory.add(jbResetRatings, "0,2");
    jpHistory.add(jbResetPreferences, "1,2");

    // --Startup
    jpStart = new JPanel();
    final double sizeStart[][] = { { p, p }, { p, p, p, p, p, p, p, p } };
    final TableLayout layoutStartup = new TableLayout(sizeStart);
    layoutStartup.setVGap(iYSeparator);
    layoutStartup.setHGap(iXSeparator);

    jpStart.setLayout(new TableLayout(sizeStart));
    jlStart = new JLabel(Messages.getString("ParameterView.9"));
    bgStart = new ButtonGroup();
    jrbNothing = new JRadioButton(Messages.getString("ParameterView.10"));
    jrbNothing.setToolTipText(Messages.getString("ParameterView.11"));
    jrbNothing.addItemListener(this);
    jrbLast = new JRadioButton(Messages.getString("ParameterView.12"));
    jrbLast.setToolTipText(Messages.getString("ParameterView.13"));
    jrbLast.addItemListener(this);
    jrbLastKeepPos = new JRadioButton(Messages.getString("ParameterView.135"));
    jrbLastKeepPos.setToolTipText(Messages.getString("ParameterView.136"));
    jrbLastKeepPos.addItemListener(this);
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
    // set chosen track in file selection
    final String sFileId = Conf.getString(Const.CONF_STARTUP_FILE);
    if (!"".equals(sFileId)) {
      final File file = FileManager.getInstance().getFileByID(sFileId);
      if (file != null) {
        sbSearch.setText(file.getTrack().getName());
      } else {
        // the file exists no more, remove its id as startup file
        Conf.setProperty(Const.CONF_STARTUP_FILE, "");
      }
    }
    sbSearch.setToolTipText(Messages.getString("ParameterView.18"));
    bgStart.add(jrbNothing);
    bgStart.add(jrbLast);
    bgStart.add(jrbLastKeepPos);
    bgStart.add(jrbShuffle);
    bgStart.add(jrbBestof);
    bgStart.add(jrbNovelties);
    bgStart.add(jrbFile);
    jpStart.add(jlStart, "0,0,1,0");
    jpStart.add(jrbNothing, "0,1,1,1");
    jpStart.add(jrbLast, "0,2,1,2");
    jpStart.add(jrbLastKeepPos, "0,3,1,3");
    jpStart.add(jrbShuffle, "0,4,1,4");
    jpStart.add(jrbBestof, "0,5,1,5");
    jpStart.add(jrbNovelties, "0,6,1,6");
    jpStart.add(jrbFile, "0,7");
    jpStart.add(sbSearch, "1,7");

    // --Confirmations
    jpConfirmations = new JPanel();
    final double sizeConfirmations[][] = { { p }, { p, p, p, p, p, p, p } };

    final TableLayout layoutConfirmation = new TableLayout(sizeConfirmations);
    layoutConfirmation.setVGap(iYSeparator);
    layoutConfirmation.setHGap(iXSeparator);
    jpConfirmations.setLayout(layoutConfirmation);

    jcbBeforeDelete = new JCheckBox(Messages.getString("ParameterView.27"));

    jcbBeforeDelete.setToolTipText(Messages.getString("ParameterView.28"));

    jcbBeforeExit = new JCheckBox(Messages.getString("ParameterView.29"));
    jcbBeforeExit.setToolTipText(Messages.getString("ParameterView.30"));

    jcbBeforeRemoveDevice = new JCheckBox(Messages.getString("ParameterView.164"));
    jcbBeforeRemoveDevice.setToolTipText(Messages.getString("ParameterView.165"));

    jcbBeforeDeleteCover = new JCheckBox(Messages.getString("ParameterView.171"));
    jcbBeforeDeleteCover.setToolTipText(Messages.getString("ParameterView.172"));

    jcbBeforeClearingHistory = new JCheckBox(Messages.getString("ParameterView.188"));
    jcbBeforeClearingHistory.setToolTipText(Messages.getString("ParameterView.188"));

    jcbBeforeResetingRatings = new JCheckBox(Messages.getString("ParameterView.189"));
    jcbBeforeResetingRatings.setToolTipText(Messages.getString("ParameterView.189"));

    jcbBeforeRefactorFiles = new JCheckBox(Messages.getString("ParameterView.194"));
    jcbBeforeRefactorFiles.setToolTipText(Messages.getString("ParameterView.194"));

    jpConfirmations.add(jcbBeforeDelete, "0,0");
    jpConfirmations.add(jcbBeforeExit, "0,1");
    jpConfirmations.add(jcbBeforeRemoveDevice, "0,2");
    jpConfirmations.add(jcbBeforeDeleteCover, "0,3");
    jpConfirmations.add(jcbBeforeClearingHistory, "0,4");
    jpConfirmations.add(jcbBeforeResetingRatings, "0,5");
    jpConfirmations.add(jcbBeforeRefactorFiles, "0,6");

    // -Modes
    jpModes = new JPanel();
    // Intro
    // intro position
    jlIntroPosition = new JLabel(Messages.getString("ParameterView.59"));
    introPosition = new JSlider(0, 100, 0);
    introPosition.setMajorTickSpacing(20);
    introPosition.setMinorTickSpacing(10);
    introPosition.setPaintTicks(true);
    introPosition.setPaintLabels(true);
    introPosition.setToolTipText(Messages.getString("ParameterView.60"));
    introPosition.addMouseWheelListener(new DefaultMouseWheelListener(introPosition));

    // intro length
    jlIntroLength = new JLabel(Messages.getString("ParameterView.61"));
    introLength = new JSlider(0, 30, 20);
    introLength.setMajorTickSpacing(10);
    introLength.setMinorTickSpacing(1);
    introLength.setPaintTicks(true);
    introLength.setPaintLabels(true);
    introLength.setToolTipText(Messages.getString("ParameterView.62"));
    introLength.addMouseWheelListener(new DefaultMouseWheelListener(introLength));

    // best of size
    jlBestofSize = new JLabel(Messages.getString("ParameterView.111"));
    jlBestofSize.setToolTipText(Messages.getString("ParameterView.112"));
    jtfBestofSize = new JTextField(3);
    jtfBestofSize.setToolTipText(Messages.getString("ParameterView.112"));
    jtfBestofSize.setInputVerifier(new InputVerifier() {
      @Override
      public boolean shouldYieldFocus(final JComponent input) {
        return verify(input);
      }

      @Override
      public boolean verify(final JComponent input) {
        final JTextField tf = (JTextField) input;
        final String sText = tf.getText();
        try {
          final int iValue = Integer.parseInt(sText);
          if ((iValue < 1) || (iValue > 100)) {
            jbOK.setEnabled(false);
            return false;
          }
        } catch (final Exception e) {
          return false;
        }
        jbOK.setEnabled(true);
        return true;
      }
    });
    // novelties age
    jlNoveltiesAge = new JLabel(Messages.getString("ParameterView.129"));
    jlNoveltiesAge.setToolTipText(Messages.getString("ParameterView.130"));
    jtfNoveltiesAge = new JTextField(3);
    jtfNoveltiesAge.setToolTipText(Messages.getString("ParameterView.130"));
    jtfNoveltiesAge.setInputVerifier(new InputVerifier() {
      @Override
      public boolean shouldYieldFocus(final JComponent input) {
        return verify(input);
      }

      @Override
      public boolean verify(final JComponent input) {
        final JTextField tf = (JTextField) input;
        final String sText = tf.getText();
        try {
          final int iValue = Integer.parseInt(sText);
          if (iValue < 0) { // if adding age =0, it mean today, no
            // max limit
            jbOK.setEnabled(false);
            return false;
          }
        } catch (final Exception e) {
          return false;
        }
        jbOK.setEnabled(true);
        return true;
      }
    });
    // number of visible tracks
    jlVisiblePlanned = new JLabel(Messages.getString("ParameterView.177"));
    jlVisiblePlanned.setToolTipText(Messages.getString("ParameterView.178"));
    jtfVisiblePlanned = new JTextField(3);
    jtfVisiblePlanned.setToolTipText(Messages.getString("ParameterView.178"));
    jtfVisiblePlanned.setInputVerifier(new InputVerifier() {
      @Override
      public boolean shouldYieldFocus(final JComponent input) {
        return verify(input);
      }

      @Override
      public boolean verify(final JComponent input) {
        final JTextField tf = (JTextField) input;
        final String sText = tf.getText();
        try {
          final int iValue = Integer.parseInt(sText);
          // number of planned tracks between 0 and 100
          if ((iValue < 0) || (iValue > 100)) {
            return false;
          }
        } catch (final Exception e) {
          return false;
        }
        jbOK.setEnabled(true);
        return true;
      }
    });
    jlCrossFadeDuration = new JLabel(Messages.getString("ParameterView.190"));
    jlCrossFadeDuration.setToolTipText(Messages.getString("ParameterView.191"));
    crossFadeDuration = new JSlider(0, 30, 0);
    crossFadeDuration.setMajorTickSpacing(10);
    crossFadeDuration.setMinorTickSpacing(1);
    crossFadeDuration.setPaintTicks(true);
    crossFadeDuration.setPaintLabels(true);
    crossFadeDuration.setToolTipText(Messages.getString("ParameterView.191"));
    crossFadeDuration.addMouseWheelListener(new DefaultMouseWheelListener(crossFadeDuration));

    // add panels
    final double sizeIntro[][] = { { p, p }, { p, p, p, p, p, p } };
    final TableLayout layoutModes = new TableLayout(sizeIntro);
    layoutModes.setVGap(iYSeparator);
    layoutModes.setHGap(iXSeparator);
    jpModes.setLayout(layoutModes);
    jpModes.add(jlIntroPosition, "0,0");
    jpModes.add(introPosition, "1,0");
    jpModes.add(jlIntroLength, "0,1");
    jpModes.add(introLength, "1,1");
    jpModes.add(jlCrossFadeDuration, "0,2");
    jpModes.add(crossFadeDuration, "1,2");
    jpModes.add(jlBestofSize, "0,3");
    jpModes.add(jtfBestofSize, "1,3");
    jpModes.add(jlNoveltiesAge, "0,4");
    jpModes.add(jtfNoveltiesAge, "1,4");
    jpModes.add(jlVisiblePlanned, "0,5");
    jpModes.add(jtfVisiblePlanned, "1,5");

    // --Options
    jpOptions = new JPanel();
    jcbDisplayUnmounted = new JCheckBox(Messages.getString("JajukJMenuBar.24"));
    jcbDisplayUnmounted.setToolTipText(Messages.getString("ParameterView.35"));
    jcbDisplayUnmounted.addActionListener(alUI);

    jcbSyncTableTree = new JCheckBox(Messages.getString("ParameterView.183"));
    jcbSyncTableTree.setToolTipText(Messages.getString("ParameterView.184"));
    jcbSyncTableTree.addActionListener(alUI);

    jcbDefaultActionClick = new JCheckBox(Messages.getString("ParameterView.179"));
    jcbDefaultActionClick.setToolTipText(Messages.getString("ParameterView.180"));

    jcbDefaultActionDrop = new JCheckBox(Messages.getString("ParameterView.181"));
    jcbDefaultActionDrop.setToolTipText(Messages.getString("ParameterView.182"));

    jcbHotkeys = new JCheckBox(Messages.getString("ParameterView.196"));
    jcbHotkeys.addActionListener(this);
    jcbHotkeys.setToolTipText(Messages.getString("ParameterView.197"));
    // Disable this option if not under windows
    jcbHotkeys.setEnabled(UtilSystem.isUnderWindows());

    jlLanguage = new JLabel(Messages.getString("ParameterView.38"));
    scbLanguage = new SteppedComboBox();

    scbLanguage.setRenderer(new BasicComboBoxRenderer() {
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

    scbLanguage.removeAllItems();
    for (String sDesc : Messages.getDescs()) {
      scbLanguage.addItem(new JLabel(sDesc, Messages.getIcon(sDesc), SwingConstants.LEFT));
    }
    scbLanguage.setToolTipText(Messages.getString("ParameterView.42"));
    final JPanel language = new JPanel(new HorizontalLayout(iXSeparator));
    language.add(jlLanguage);
    language.add(scbLanguage);
    scbLanguage.addActionListener(this);

    final double sizeOptions[][] = { { p }, { 20, p, p, p, p, p, p } };
    final TableLayout layoutOption = new TableLayout(sizeOptions);
    layoutOption.setHGap(iXSeparator);
    layoutOption.setVGap(iYSeparator);
    jpOptions.setLayout(layoutOption);

    jcbUseParentDir = new JCheckBox(Messages.getString("ParameterView.101"));
    jcbUseParentDir.setToolTipText(Messages.getString("ParameterView.102"));

    jpOptions.add(language, "0,0");
    jpOptions.add(jcbDisplayUnmounted, "0,1");
    jpOptions.add(jcbDefaultActionClick, "0,2");
    jpOptions.add(jcbDefaultActionDrop, "0,3");
    jpOptions.add(jcbSyncTableTree, "0,4");
    jpOptions.add(jcbHotkeys, "0,5");
    jpOptions.add(jcbUseParentDir, "0,6");

    // --Patterns
    jpTags = new JPanel();
    final double sizeTags[][] = { { p, p }, { p, p, p } };
    final TableLayout layoutTags = new TableLayout(sizeTags);
    layoutTags.setHGap(iXSeparator);
    layoutTags.setVGap(iYSeparator);
    jpTags.setLayout(layoutTags);
    jlRefactorPattern = new JLabel(Messages.getString("ParameterView.192"));
    jlRefactorPattern.setToolTipText(Messages.getString("ParameterView.193"));
    jtfRefactorPattern = new JFormattedTextField();
    jtfRefactorPattern.setToolTipText(Messages.getString("ParameterView.193"));
    jtfRefactorPattern.setInputVerifier(new PatternInputVerifier());
    jlAnimationPattern = new JLabel(Messages.getString("ParameterView.195"));
    jlAnimationPattern.setToolTipText(Messages.getString("ParameterView.193"));
    jtfAnimationPattern = new JTextField();
    jtfAnimationPattern.setToolTipText(Messages.getString("ParameterView.193"));
    // Frame Title Options
    jlFrameTitle = new JLabel(Messages.getString("ParameterView.248"));
    jlFrameTitle.setToolTipText(Messages.getString("ParameterView.193"));
    jtfFrameTitle = new JTextField();
    jtfFrameTitle.setToolTipText(Messages.getString("ParameterView.193"));

    jpTags.add(jlRefactorPattern, "0,0");
    jpTags.add(jtfRefactorPattern, "1,0");
    jpTags.add(jlAnimationPattern, "0,1");
    jpTags.add(jtfAnimationPattern, "1,1");
    jpTags.add(jlFrameTitle, "0,2");
    jpTags.add(jtfFrameTitle, "1,2");

    // --Advanced
    jpAdvanced = new JPanel();
    jcbBackup = new JCheckBox(Messages.getString("ParameterView.116"));
    jcbBackup.addActionListener(this);
    jcbBackup.setToolTipText(Messages.getString("ParameterView.117"));
    backupSize = new JSlider(0, 100);
    backupSize.setMajorTickSpacing(10);
    backupSize.setMinorTickSpacing(10);
    backupSize.setPaintTicks(true);
    backupSize.setPaintLabels(true);
    backupSize.setToolTipText(Messages.getString("ParameterView.119"));
    backupSize.addMouseWheelListener(new DefaultMouseWheelListener(backupSize));
    jlCollectionEncoding = new JLabel(Messages.getString("ParameterView.120"));
    jlCollectionEncoding.setToolTipText(Messages.getString("ParameterView.121"));
    jcbCollectionEncoding = new JComboBox();
    jcbCollectionEncoding.setToolTipText(Messages.getString("ParameterView.121"));
    jcbRegexp = new JCheckBox(Messages.getString("ParameterView.113"));
    jcbRegexp.setSelected(Conf.getBoolean(Const.CONF_REGEXP));
    jcbRegexp.setToolTipText(Messages.getString("ParameterView.114"));
    jcbCollectionEncoding.addItem("UTF-8");
    jcbCollectionEncoding.addItem("UTF-16");
    jlLogLevel = new JLabel(Messages.getString("ParameterView.46"));
    scbLogLevel = new SteppedComboBox();
    scbLogLevel.addItem(Messages.getString("ParameterView.47"));
    scbLogLevel.addItem(Messages.getString("ParameterView.48"));
    scbLogLevel.addItem(Messages.getString("ParameterView.49"));
    scbLogLevel.addItem(Messages.getString("ParameterView.50"));
    scbLogLevel.addItem(Messages.getString("ParameterView.51"));
    scbLogLevel.setToolTipText(Messages.getString("ParameterView.52"));
    jlMPlayerPath = new JLabel(Messages.getString("ParameterView.242"));
    jlMPlayerPath.setToolTipText(Messages.getString("ParameterView.243"));
    jtfMPlayerPath = new JTextField();
    jtfMPlayerPath.setToolTipText(Messages.getString("ParameterView.243"));
    jlMPlayerArgs = new JLabel(Messages.getString("ParameterView.205"));
    jlMPlayerArgs.setToolTipText(Messages.getString("ParameterView.206"));
    jtfMPlayerArgs = new JTextField();
    jtfMPlayerArgs.setToolTipText(Messages.getString("ParameterView.206"));
    jlEnvVariables = new JLabel(Messages.getString("ParameterView.219"));
    jlEnvVariables.setToolTipText(Messages.getString("ParameterView.220"));
    jtfEnvVariables = new JTextField();
    jtfEnvVariables.setToolTipText(Messages.getString("ParameterView.220"));
    jlJajukWorkspace = new JLabel(Messages.getString("ParameterView.207"));
    jlJajukWorkspace.setToolTipText(Messages.getString("ParameterView.208"));
    // Directory selection
    psJajukWorkspace = new PathSelector(Main.getWorkspace());
    psJajukWorkspace.setToolTipText(Messages.getString("ParameterView.208"));
    jcbCheckUpdates = new JCheckBox(Messages.getString("ParameterView.234"));
    jcbCheckUpdates.setToolTipText(Messages.getString("ParameterView.234"));
    jcbCheckUpdates.setSelected(Conf.getBoolean(Const.CONF_CHECK_FOR_UPDATE));
    jcbForceFileDate = new JCheckBox(Messages.getString("ParameterView.244"));
    jcbForceFileDate.setToolTipText(Messages.getString("ParameterView.245"));
    jcbForceFileDate.setSelected(Conf.getBoolean(Const.CONF_FORCE_FILE_DATE));
    final double sizeAdvanced[][] = { { p, p }, { p, p, p, p, p, p, p, p, p, p } };
    final TableLayout layoutAdvanced = new TableLayout(sizeAdvanced);
    layoutAdvanced.setHGap(iXSeparator);
    layoutAdvanced.setVGap(iYSeparator);
    jpAdvanced.setLayout(layoutAdvanced);
    jpAdvanced.add(jcbRegexp, "0,0");
    jpAdvanced.add(jcbBackup, "0,1");
    jpAdvanced.add(backupSize, "1,1");
    jpAdvanced.add(jlCollectionEncoding, "0,2");
    jpAdvanced.add(jcbCollectionEncoding, "1,2");
    jpAdvanced.add(jlLogLevel, "0,3");
    jpAdvanced.add(scbLogLevel, "1,3");
    jpAdvanced.add(jlMPlayerPath, "0,4");
    jpAdvanced.add(jtfMPlayerPath, "1,4");
    jpAdvanced.add(jlMPlayerArgs, "0,5");
    jpAdvanced.add(jtfMPlayerArgs, "1,5");
    jpAdvanced.add(jlEnvVariables, "0,6");
    jpAdvanced.add(jtfEnvVariables, "1,6");
    jpAdvanced.add(jlJajukWorkspace, "0,7");
    jpAdvanced.add(psJajukWorkspace, "1,7");
    jpAdvanced.add(jcbCheckUpdates, "0,8");
    jpAdvanced.add(jcbForceFileDate, "0,9");

    // - Network
    jpNetwork = new JPanel();
    final double sizeNetwork[][] = { { p, p }, { p, p, p, p, p, p, p, p } };
    final TableLayout layoutNetwork = new TableLayout(sizeNetwork);
    layoutNetwork.setHGap(iXSeparator);
    layoutNetwork.setVGap(iYSeparator);
    jpNetwork.setLayout(layoutNetwork);
    bgProxy = new ButtonGroup();
    jcbProxyNone = new JCheckBox(Messages.getString("ParameterView.236"));
    jcbProxyNone.setToolTipText(Messages.getString("ParameterView.236"));
    jcbProxyNone.addActionListener(this);
    jcbProxyHttp = new JCheckBox(Messages.getString("ParameterView.237"));
    jcbProxyHttp.setToolTipText(Messages.getString("ParameterView.237"));
    jcbProxyHttp.addActionListener(this);
    jcbProxySocks = new JCheckBox(Messages.getString("ParameterView.238"));
    jcbProxySocks.setToolTipText(Messages.getString("ParameterView.238"));
    jcbProxySocks.addActionListener(this);
    bgProxy.add(jcbProxyNone);
    bgProxy.add(jcbProxyHttp);
    bgProxy.add(jcbProxySocks);
    jlProxyHostname = new JLabel(Messages.getString("ParameterView.144"));
    jlProxyHostname.setToolTipText(Messages.getString("ParameterView.145"));
    jtfProxyHostname = new JTextField();
    jtfProxyHostname.setToolTipText(Messages.getString("ParameterView.145"));
    jlProxyPort = new JLabel(Messages.getString("ParameterView.146"));
    jlProxyPort.setToolTipText(Messages.getString("ParameterView.147"));
    jtfProxyPort = new JTextField();
    jtfProxyPort.setToolTipText(Messages.getString("ParameterView.147"));
    jtfProxyPort.setInputVerifier(new InputVerifier() {
      @Override
      public boolean shouldYieldFocus(final JComponent input) {
        return verify(input);
      }

      @Override
      public boolean verify(final JComponent input) {
        final JTextField tf = (JTextField) input;
        final String sText = tf.getText();
        try {
          final int iValue = Integer.parseInt(sText);
          if ((iValue < 0) || (iValue > 65535)) {
            // port is between 0 and 65535
            jbOK.setEnabled(false);
            return false;
          }
        } catch (final Exception e) {
          return false;
        }
        jbOK.setEnabled(true);
        return true;
      }
    });
    jlProxyLogin = new JLabel(Messages.getString("ParameterView.142"));
    jlProxyLogin.setToolTipText(Messages.getString("ParameterView.143"));
    jtfProxyLogin = new JTextField();
    jtfProxyLogin.setToolTipText(Messages.getString("ParameterView.143"));
    jlProxyPwd = new JLabel(Messages.getString("ParameterView.239"));
    jlProxyPwd.setToolTipText(Messages.getString("ParameterView.239"));
    jtfProxyPwd = new JPasswordField();
    jtfProxyPwd.setToolTipText(Messages.getString("ParameterView.239"));

    jlConnectionTO = new JLabel(Messages.getString("ParameterView.160"));
    jlConnectionTO.setToolTipText(Messages.getString("ParameterView.160"));
    connectionTO = new JSlider(0, 60);
    connectionTO.setMajorTickSpacing(10);
    connectionTO.setMinorTickSpacing(5);
    connectionTO.setPaintTicks(true);
    connectionTO.setPaintLabels(true);
    connectionTO.setToolTipText(Messages.getString("ParameterView.161"));
    connectionTO.addMouseWheelListener(new DefaultMouseWheelListener(connectionTO));
    // Add items
    jpNetwork.add(jcbProxyNone, "0,0");
    jpNetwork.add(jcbProxyHttp, "0,1");
    jpNetwork.add(jcbProxySocks, "0,2");
    jpNetwork.add(jlProxyHostname, "0,3");
    jpNetwork.add(jtfProxyHostname, "1,3");
    jpNetwork.add(jlProxyPort, "0,4");
    jpNetwork.add(jtfProxyPort, "1,4");
    jpNetwork.add(jlProxyLogin, "0,5");
    jpNetwork.add(jtfProxyLogin, "1,5");
    jpNetwork.add(jlProxyPwd, "0,6");
    jpNetwork.add(jtfProxyPwd, "1,6");
    jpNetwork.add(jlConnectionTO, "0,7");
    jpNetwork.add(connectionTO, "1,7");

    // - Last.FM
    jpLastFM = new JPanel();
    final double sizeLastFM[][] = { { p, 200 }, { p, p, p, p } };
    final TableLayout layoutLastFM = new TableLayout(sizeLastFM);
    layoutLastFM.setHGap(iXSeparator);
    layoutLastFM.setVGap(iYSeparator);
    jpLastFM.setLayout(layoutLastFM);
    jcbAudioScrobbler = new JCheckBox(Messages.getString("ParameterView.199"));
    jcbAudioScrobbler.setToolTipText(Messages.getString("ParameterView.200"));
    jcbAudioScrobbler.addActionListener(this);
    jlASUser = new JLabel(Messages.getString("ParameterView.201"));
    jtfASUser = new JTextField();
    jtfASUser.setToolTipText(Messages.getString("ParameterView.202"));
    jlASPassword = new JLabel(Messages.getString("ParameterView.203"));
    jpfASPassword = new JPasswordField();
    jpfASPassword.setToolTipText(Messages.getString("ParameterView.204"));
    jcbEnableLastFMInformation = new JCheckBox(Messages.getString("ParameterView.240"));
    jcbEnableLastFMInformation.setToolTipText(Messages.getString("ParameterView.241"));
    // Add items
    jpLastFM.add(jcbEnableLastFMInformation, "0,0");
    jpLastFM.add(jcbAudioScrobbler, "0,1");
    jpLastFM.add(jlASUser, "0,2");
    jpLastFM.add(jtfASUser, "1,2");
    jpLastFM.add(jlASPassword, "0,3");
    jpLastFM.add(jpfASPassword, "1,3");

    // - Cover
    jpCovers = new JPanel();
    final double sizeCover[][] = { { p, p }, { p, p, p } };
    final TableLayout layoutCover = new TableLayout(sizeCover);
    layoutCover.setVGap(iYSeparator);
    layoutCover.setHGap(iXSeparator);
    jpCovers.setLayout(layoutCover);
    jcbAutoCover = new JCheckBox(Messages.getString("ParameterView.148"));
    jcbAutoCover.setToolTipText(Messages.getString("ParameterView.149"));
    jcbAutoCover.addActionListener(this);
    jcbShuffleCover = new JCheckBox(Messages.getString("ParameterView.166"));
    jcbShuffleCover.setToolTipText(Messages.getString("ParameterView.167"));
    jcbShuffleCover.addActionListener(this);
    jlCoverSize = new JLabel(Messages.getString("ParameterView.150"));
    jlCoverSize.setToolTipText(Messages.getString("ParameterView.151"));
    jcbCoverSize = new JComboBox();
    jcbCoverSize.setToolTipText(Messages.getString("ParameterView.151"));
    jcbCoverSize.addItem(Messages.getString("ParameterView.211"));
    jcbCoverSize.addItem(Messages.getString("ParameterView.212"));
    jcbCoverSize.addItem(Messages.getString("ParameterView.213"));
    jcbCoverSize.addItem(Messages.getString("ParameterView.214"));
    jcbCoverSize.addItem(Messages.getString("ParameterView.215"));
    // Add items
    jpCovers.add(jcbShuffleCover, "0,0");
    jpCovers.add(jcbAutoCover, "0,1");
    jpCovers.add(jlCoverSize, "0,2");
    jpCovers.add(jcbCoverSize, "1,2");

    // -- User interface --
    jpUI = new JPanel();
    final double sizeUI[][] = { { p, p }, { p, p, p, p, p, p, p, p, p } };
    final TableLayout layoutUI = new TableLayout(sizeUI);
    layoutUI.setHGap(iXSeparator);
    layoutUI.setVGap(iYSeparator);
    jpUI.setLayout(layoutUI);
    // Catalog view
    jlCatalogPages = new JLabel(Messages.getString("ParameterView.221"));
    jlCatalogPages.setToolTipText(Messages.getString("ParameterView.222"));
    jsCatalogPages = new JSlider(0, 1000, Conf.getInt(Const.CONF_CATALOG_PAGE_SIZE));
    jsCatalogPages.setMinorTickSpacing(100);
    jsCatalogPages.setMajorTickSpacing(200);
    jsCatalogPages.setPaintTicks(true);
    jsCatalogPages.setPaintLabels(true);
    jsCatalogPages.setToolTipText(Integer.toString(jsCatalogPages.getValue()));
    jsCatalogPages.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        jsCatalogPages.setToolTipText(Integer.toString(jsCatalogPages.getValue()));
      }
    });
    jcbShowPopups = new JCheckBox(Messages.getString("ParameterView.228"));
    jcbShowPopups.addActionListener(alUI);
    JLabel jlPerspectiveSize = new JLabel(Messages.getString("ParameterView.246"));
    jsPerspectiveSize = new JSlider(16, 45, Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE));
    jsPerspectiveSize.setSnapToTicks(true);
    jsPerspectiveSize.setMajorTickSpacing(8);
    jsPerspectiveSize.setMinorTickSpacing(1);
    jsPerspectiveSize.setPaintTicks(true);
    jsPerspectiveSize.setPaintLabels(true);
    jsPerspectiveSize.setToolTipText(Messages.getString("ParameterView.246"));
    jbCatalogRefresh = new JajukButton(Messages.getString("CatalogView.19"), IconLoader
        .getIcon(JajukIcons.REFRESH));
    jbCatalogRefresh.setToolTipText(Messages.getString("CatalogView.3"));
    jbCatalogRefresh.addActionListener(this);
    final JXCollapsiblePane catalogView = new JXCollapsiblePane();
    catalogView.setLayout(new VerticalLayout(10));
    catalogView.setCollapsed(true);
    final ToggleLink toggle = new ToggleLink(Messages.getString("ParameterView.229"), catalogView);
    final JPanel jpCatalogSize = new JPanel();
    jpCatalogSize.setLayout(new HorizontalLayout());
    jpCatalogSize.add(jlCatalogPages);
    jpCatalogSize.add(jsCatalogPages);
    catalogView.add(jpCatalogSize);
    catalogView.add(jbCatalogRefresh);

    // Font selector
    jlFonts = new JLabel(Messages.getString("ParameterView.223"));
    jsFonts = new JSlider(8, 16, Conf.getInt(Const.CONF_FONTS_SIZE));
    jsFonts.setSnapToTicks(true);
    jsFonts.setMajorTickSpacing(1);
    jsFonts.setMinorTickSpacing(1);
    jsFonts.setPaintTicks(true);
    jsFonts.setPaintLabels(true);
    jsFonts.setToolTipText(Messages.getString("ParameterView.224"));

    // Show Balloon
    jcbShowBaloon = new JCheckBox(Messages.getString("ParameterView.185"));
    jcbShowBaloon.setToolTipText(Messages.getString("ParameterView.185"));
    jcbShowBaloon.addActionListener(alUI);

    // LaF
    jlLAF = new JLabel(Messages.getString("ParameterView.43"));
    jlLAF.setToolTipText(Messages.getString("ParameterView.44"));
    scbLAF = new SteppedComboBox();
    final Map<String, SkinInfo> map = SubstanceLookAndFeel.getAllSkins();
    // Use a tree set to sort themes alphabetically
    final Set<String> themes = new TreeSet<String>(map.keySet());
    // Add each theme to the combo box
    for (final String theme : themes) {
      scbLAF.addItem(theme);
    }
    scbLAF.setToolTipText(Messages.getString("ParameterView.44"));
    // Add items
    jpUI.add(jlFonts, "0,0");
    jpUI.add(jsFonts, "1,0");
    jpUI.add(jlLAF, "0,1");
    jpUI.add(scbLAF, "1,1");
    jpUI.add(jcbShowPopups, "0,4");
    jpUI.add(jcbShowBaloon, "0,5");
    jpUI.add(toggle, "0,6");
    jpUI.add(catalogView, "0,7,1,7");
    jpUI.add(jlPerspectiveSize, "0,8");
    jpUI.add(jsPerspectiveSize, "1,8");

    // --OK/cancel panel
    final Dimension dim = new Dimension(200, 20);
    jpOKCancel = new JPanel();
    jpOKCancel.setLayout(new FlowLayout());
    jbOK = new JButton(Messages.getString("ParameterView.85"), IconLoader.getIcon(JajukIcons.OK));
    jbOK.setPreferredSize(dim);
    jbOK.addActionListener(this);
    jpOKCancel.add(jbOK);
    jbDefault = new JButton(Messages.getString("ParameterView.86"), IconLoader
        .getIcon(JajukIcons.DEFAULTS_BIG));
    jbDefault.setPreferredSize(dim);
    jbDefault.addActionListener(this);
    jpOKCancel.add(jbDefault);

    // --Global layout
    final double size[][] = { { 0.99 }, { 0.9, 0.10 } };
    setLayout(new TableLayout(size));
    // add main panels
    jtpMain = new JTabbedPane(SwingConstants.TOP);
    // ScrollPane without border
    class JajukJScrollPane extends JScrollPane {
      private static final long serialVersionUID = 4564343623724771988L;

      public JajukJScrollPane(final Component view) {
        super(view);
        setBorder(null);
      }
    }
    jtpMain.addTab(Messages.getString("ParameterView.33"), new JajukJScrollPane(jpOptions));
    jtpMain.addTab(Messages.getString("ParameterView.226"), new JajukJScrollPane(jpModes));
    jtpMain.addTab(Messages.getString("ParameterView.225"), new JajukJScrollPane(jpUI));
    jtpMain.addTab(Messages.getString("ParameterView.19"), new JajukJScrollPane(jpStart));
    jtpMain.addTab(Messages.getString("ParameterView.98"), new JajukJScrollPane(jpTags));
    jtpMain.addTab(Messages.getString("ParameterView.8"), new JajukJScrollPane(jpHistory));
    jtpMain.addTab(Messages.getString("ParameterView.235"), new JajukJScrollPane(jpLastFM));
    jtpMain.addTab(Messages.getString("ParameterView.159"), new JajukJScrollPane(jpCovers));
    jtpMain.addTab(Messages.getString("ParameterView.26"), new JajukJScrollPane(jpConfirmations));
    jtpMain.addTab(Messages.getString("ParameterView.139"), new JajukJScrollPane(jpNetwork));
    jtpMain.addTab(Messages.getString("ParameterView.115"), new JajukJScrollPane(jpAdvanced));
    try {
      // Reload stored selected index
      jtpMain.setSelectedIndex(Conf.getInt(Const.CONF_OPTIONS_TAB));
    } catch (final Exception e) {
      // an error can occur if a new release brings or remove tabs
      Log.error(e);
      jtpMain.setSelectedIndex(0);
    }
    jtpMain.addChangeListener(this);
    add(jtpMain, "0,0");
    add(jpOKCancel, "0,1");
    // update widgets state
    updateSelection();
    ObservationManager.register(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged(final ItemEvent e) {
    if (e.getSource() == jrbFile) { // jrbFile has been selected or
      // deselected
      sbSearch.setEnabled(jrbFile.isSelected());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  public void stateChanged(final ChangeEvent e) {
    // when changing tab, store it for future jajuk sessions
    Conf.setProperty(Const.CONF_OPTIONS_TAB, Integer.toString(jtpMain.getSelectedIndex()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(final Event event) {
    final JajukEvents subject = event.getSubject();
    if (JajukEvents.PARAMETERS_CHANGE.equals(subject)) {
      // Ignore this event is thrown by this view itself (to avoid loosing
      // already set options)
      if ((event.getDetails() != null) && (event.getDetails().get(Const.DETAIL_ORIGIN) != null)
          && event.getDetails().get(Const.DETAIL_ORIGIN).equals(this)) {
        return;
      }
      updateSelection();
    }
  }

  /**
   * Set widgets to specified value in options
   */
  private void updateSelection() {
    jtfHistory.setText(Conf.getString(Const.CONF_HISTORY));
    if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_FILE)) {
      jrbFile.setSelected(true);
      sbSearch.setEnabled(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_LAST)) {
      jrbLast.setSelected(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_LAST_KEEP_POS)) {
      jrbLastKeepPos.setSelected(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_NOTHING)) {
      jrbNothing.setSelected(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_SHUFFLE)) {
      jrbShuffle.setSelected(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_BESTOF)) {
      jrbBestof.setSelected(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_NOVELTIES)) {
      jrbNovelties.setSelected(true);
    }
    // Confirmations
    jcbBeforeDelete.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_DELETE_FILE));
    jcbBeforeExit.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_EXIT));
    jcbBeforeRemoveDevice.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_REMOVE_DEVICE));
    jcbBeforeDeleteCover.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_DELETE_COVER));
    jcbBeforeClearingHistory.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_CLEAR_HISTORY));
    jcbBeforeResetingRatings.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_RESET_RATINGS));
    jcbBeforeRefactorFiles.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_REFACTOR_FILES));
    // options
    jcbDisplayUnmounted.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED));
    jcbDefaultActionClick.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
    jcbDefaultActionDrop.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_DEFAULT_ACTION_DROP));
    jcbHotkeys.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_HOTKEYS));

    jcbSyncTableTree.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_SYNC_TABLE_TREE));
    String rightLanguageDesc = Messages
        .getDescForLocal(Conf.getString(Const.CONF_OPTIONS_LANGUAGE));
    // Select the right language
    int index = 0;
    for (String desc : Messages.getDescs()) {
      if (desc.equals(rightLanguageDesc)) {
        scbLanguage.setSelectedIndex(index);
        break;
      }
      index++;
    }
    scbLanguage.addActionListener(this);
    scbLogLevel.setSelectedIndex(Integer.parseInt(Conf.getString(Const.CONF_OPTIONS_LOG_LEVEL)));
    introLength.setValue(Conf.getInt(Const.CONF_OPTIONS_INTRO_LENGTH));
    introPosition.setValue(Conf.getInt(Const.CONF_OPTIONS_INTRO_BEGIN));
    jtfBestofSize.setText(Conf.getString(Const.CONF_BESTOF_TRACKS_SIZE));
    jtfNoveltiesAge.setText(Conf.getString(Const.CONF_OPTIONS_NOVELTIES_AGE));
    jtfVisiblePlanned.setText(Conf.getString(Const.CONF_OPTIONS_VISIBLE_PLANNED));
    crossFadeDuration.setValue(Conf.getInt(Const.CONF_FADE_DURATION));
    jcbUseParentDir.setSelected(Conf.getBoolean(Const.CONF_TAGS_USE_PARENT_DIR));
    // advanced
    final int iBackupSize = Conf.getInt(Const.CONF_BACKUP_SIZE);
    if (iBackupSize <= 0) { // backup size =0 means no backup
      jcbBackup.setSelected(false);
      backupSize.setEnabled(false);
    } else {
      jcbBackup.setSelected(true);
      backupSize.setEnabled(true);
    }
    backupSize.setValue(iBackupSize);
    jcbCollectionEncoding.setSelectedItem(Conf.getString(Const.CONF_COLLECTION_CHARSET));
    jtfRefactorPattern.setText(Conf.getString(Const.CONF_REFACTOR_PATTERN));
    jtfAnimationPattern.setText(Conf.getString(Const.CONF_ANIMATION_PATTERN));
    jtfFrameTitle.setText(Conf.getString(Const.CONF_FRAME_TITLE_PATTERN));
    jtfMPlayerPath.setText(Conf.getString(Const.CONF_MPLAYER_PATH_FORCED));
    jtfMPlayerArgs.setText(Conf.getString(Const.CONF_MPLAYER_ARGS));
    jtfEnvVariables.setText(Conf.getString(Const.CONF_ENV_VARIABLES));
    // network
    final boolean bUseProxy = Conf.getBoolean(Const.CONF_NETWORK_USE_PROXY);
    jcbProxyNone.setSelected(bUseProxy);
    jtfProxyHostname.setText(Conf.getString(Const.CONF_NETWORK_PROXY_HOSTNAME));
    jtfProxyHostname.setEnabled(bUseProxy);
    jlProxyHostname.setEnabled(bUseProxy);
    jtfProxyPort.setText(Conf.getString(Const.CONF_NETWORK_PROXY_PORT));
    jtfProxyPort.setEnabled(bUseProxy);
    jlProxyPort.setEnabled(bUseProxy);
    jtfProxyLogin.setText(Conf.getString(Const.CONF_NETWORK_PROXY_LOGIN));
    jtfProxyLogin.setEnabled(bUseProxy);
    jlProxyLogin.setEnabled(bUseProxy);
    jtfProxyPwd.setText(UtilString.rot13(Conf.getString(Const.CONF_NETWORK_PROXY_PWD)));
    jtfProxyPwd.setEnabled(bUseProxy);
    jlProxyPwd.setEnabled(bUseProxy);
    connectionTO.setValue(Conf.getInt(Const.CONF_NETWORK_CONNECTION_TO));
    if (!Conf.getBoolean(Const.CONF_NETWORK_USE_PROXY)) {
      jcbProxyNone.setSelected(true);
    } else if (Const.PROXY_TYPE_HTTP.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
      jcbProxyHttp.setSelected(true);
    } else if (Const.PROXY_TYPE_SOCKS.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
      jcbProxySocks.setSelected(true);
    }
    // Covers
    jcbAutoCover.setSelected(Conf.getBoolean(Const.CONF_COVERS_AUTO_COVER));
    jlCoverSize.setEnabled(Conf.getBoolean(Const.CONF_COVERS_AUTO_COVER));
    jcbCoverSize.setEnabled(Conf.getBoolean(Const.CONF_COVERS_AUTO_COVER));
    jcbCoverSize.setSelectedIndex(Conf.getInt(Const.CONF_COVERS_SIZE));
    jcbShuffleCover.setSelected(Conf.getBoolean(Const.CONF_COVERS_SHUFFLE));
    jcbAudioScrobbler.setSelected(Conf.getBoolean(Const.CONF_LASTFM_ENABLE));
    jcbEnableLastFMInformation.setSelected(Conf.getBoolean(Const.CONF_LASTFM_INFO));
    jtfASUser.setText(Conf.getString(Const.CONF_LASTFM_USER));
    jpfASPassword.setText(UtilString.rot13(Conf.getString(Const.CONF_LASTFM_PASSWORD)));
    if (!Conf.getBoolean(Const.CONF_LASTFM_ENABLE)) {
      jlASUser.setEnabled(false);
      jtfASUser.setEnabled(false);
      jlASPassword.setEnabled(false);
      jpfASPassword.setEnabled(false);
    }
    // UI
    jcbShowBaloon.setSelected(Conf.getBoolean(Const.CONF_UI_SHOW_BALLOON));
    jcbShowPopups.setSelected(Conf.getBoolean(Const.CONF_SHOW_POPUPS));
    scbLAF.removeActionListener(this);
    scbLAF.setSelectedItem(Conf.getString(Const.CONF_OPTIONS_LNF));
    scbLAF.addActionListener(this);
    jsPerspectiveSize.setValue(Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE));
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  public void valueChanged(final ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {
      final SearchResult sr = sbSearch.getResult(sbSearch.getSelectedIndex());
      sbSearch.setText(sr.getFile().getTrack().getName());
      Conf.setProperty(Const.CONF_STARTUP_FILE, sr.getFile().getID());
      sbSearch.hidePopup();
    }
  }

}
