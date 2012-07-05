/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.ui.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.core.RatingManager;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.notification.NotificatorTypes;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.LocaleManager;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Helper class containing GUI update code from and to configuration.
 */
public class ParameterViewGUIHelper implements ActionListener, ItemListener, ChangeListener {
  /** Associated Parameter view. */
  ParameterView pv;
  /** Do some updates require a restart ?. */
  boolean someOptionsAppliedAtNextStartup = false;

  /**
   * Default constructor.
   *
   * @param pv the associated parameter view
   */
  ParameterViewGUIHelper(ParameterView pv) {
    this.pv = pv;
  }

  /**
  * Apply parameters from GUI to configuration.
  */
  void updateConfFromGUI() {
    // Options
    updateConfFromGUIOptions();
    // Startup
    updateConfFromGUIStartup();
    // Confirmations
    updateConfFromGUIConfirmation();
    // History
    updateConfFromGUIHistory();
    // Patterns
    updateConfFromGUIPatterns();
    // Advanced
    updateConfFromGUIAdvanced();
    // GUI
    updateConfFromGUIGUI();
    // If jajuk home changes, write new path in bootstrap file
    handleWorkspaceChange();
    // Network
    updateConfFromGUINetwork();
    // Covers
    updateConfFromGUICover();
    // configuration
    try {
      Conf.commit();
    } catch (final Exception e) {
      Log.error(113, e);
      Messages.showErrorMessage(113);
    }
    // Force a full refresh (useful for catalog view for instance)
    ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
    // display a message
    InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.109"),
        InformationJPanel.MessageType.INFORMATIVE);
  }

  /**
   * Set widgets to specified value in options.
   */
  void updateGUIFromConf() {
    // History
    updateGUIFromConfHistory();
    // Confirmations
    updateGUIFromConfConfirmations();
    // Options
    updateGUIFromConfOptions();
    // Advanced
    updateGUIFromConfAdvanced();
    //Startup
    updateGUIFromConfStartup();
    // Network
    updateGUIFromConfNetwork();
    // Covers
    updateGUIFromConfCovers();
    // UI
    updateGUIFromConfGUI();
  }

  /**
   * Update history tab.
   *
   */
  private void updateGUIFromConfHistory() {
    pv.jtfHistory.setText(Conf.getString(Const.CONF_HISTORY));
    pv.jcbManualRatings.setSelected(Conf.getBoolean(Const.CONF_MANUAL_RATINGS));
  }

  /**
   * Update Confirmations tab.
   *
   */
  private void updateGUIFromConfConfirmations() {
    pv.jcbBeforeDelete.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_DELETE_FILE));
    pv.jcbBeforeExit.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_EXIT));
    pv.jcbBeforeRemoveDevice.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_REMOVE_DEVICE));
    pv.jcbBeforeDeleteCover.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_DELETE_COVER));
    pv.jcbBeforeClearingHistory
        .setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_CLEAR_HISTORY));
    pv.jcbBeforeResetingRatings
        .setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_RESET_RATINGS));
    pv.jcbBeforeRefactorFiles.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_REFACTOR_FILES));
    pv.jcbBeforeWritingTag.setSelected(Conf.getBoolean(Const.CONF_CONFIRMATIONS_BEFORE_TAG_WRITE));
  }

  /**
   * Update "Options", "LastFM" and "Modes" tabs.
   */
  private void updateGUIFromConfOptions() {
    pv.jcbDisplayUnmounted.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED));
    pv.jcbDefaultActionClick.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
    pv.jcbDefaultActionDrop.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_DROP));
    pv.jcbHotkeys.setSelected(Conf.getBoolean(Const.CONF_OPTIONS_HOTKEYS));
    String rightLanguageDesc = LocaleManager.getDescForLocale(Conf
        .getString(Const.CONF_OPTIONS_LANGUAGE));
    // Select the right language
    int index = 0;
    for (String desc : LocaleManager.getLocalesDescs()) {
      if (desc.equals(rightLanguageDesc)) {
        pv.scbLanguage.setSelectedIndex(index);
        break;
      }
      index++;
    }
    pv.scbLanguage.addActionListener(this);
    pv.scbLogLevel.setSelectedIndex(Integer.parseInt(Conf.getString(Const.CONF_OPTIONS_LOG_LEVEL)));
    pv.introLength.setValue(Conf.getInt(Const.CONF_OPTIONS_INTRO_LENGTH));
    pv.introPosition.setValue(Conf.getInt(Const.CONF_OPTIONS_INTRO_BEGIN));
    pv.jtfBestofSize.setText(Conf.getString(Const.CONF_BESTOF_TRACKS_SIZE));
    pv.jtfNoveltiesAge.setText(Conf.getString(Const.CONF_OPTIONS_NOVELTIES_AGE));
    pv.jtfVisiblePlanned.setText(Conf.getString(Const.CONF_OPTIONS_VISIBLE_PLANNED));
    pv.crossFadeDuration.setValue(Conf.getInt(Const.CONF_FADE_DURATION));
    pv.jcbUseParentDir.setSelected(Conf.getBoolean(Const.CONF_TAGS_USE_PARENT_DIR));
    pv.jcbDropPlayedTracksFromQueue.setSelected(Conf
        .getBoolean(Const.CONF_DROP_PLAYED_TRACKS_FROM_QUEUE));
    pv.jcbUseVolnorm.setSelected(Conf.getBoolean(Const.CONF_USE_VOLNORM));
    pv.jcbEnableBitPerfect.setSelected(Conf.getBoolean(Const.CONF_BIT_PERFECT));
    // Disable features incompatible with Bit-perfect mode
    pv.jcbUseVolnorm.setEnabled(!pv.jcbEnableBitPerfect.isSelected());
    pv.crossFadeDuration.setEnabled(!pv.jcbEnableBitPerfect.isSelected());
    pv.jcbShowVideos.setSelected(Conf.getBoolean(Const.CONF_SHOW_VIDEOS));
    pv.jcbPreserveFileDates.setSelected(Conf.getBoolean(Const.CONF_PRESERVE_FILE_DATES));
  }

  /**
   * Update Start-up tab.
   */
  private void updateGUIFromConfStartup() {
    if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_ITEM)) {
      pv.jrbFile.setSelected(true);
      pv.sbSearch.setEnabled(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_LAST)) {
      pv.jrbLast.setSelected(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_LAST_KEEP_POS)) {
      pv.jrbLastKeepPos.setSelected(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_NOTHING)) {
      pv.jrbNothing.setSelected(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_SHUFFLE)) {
      pv.jrbShuffle.setSelected(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_BESTOF)) {
      pv.jrbBestof.setSelected(true);
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_NOVELTIES)) {
      pv.jrbNovelties.setSelected(true);
    }
    // set chosen track in file selection
    String conf = Conf.getString(Const.CONF_STARTUP_ITEM);
    String item = conf.substring(conf.indexOf('/') + 1, conf.length());
    if (!StringUtils.isBlank(item)) {
      if (conf.matches(SearchResultType.FILE.name() + ".*")) {
        File file = FileManager.getInstance().getFileByID(item);
        if (file != null) {
          pv.sbSearch.setText(file.getTrack().getName());
        } else {
          // the file exists no more, remove its id as startup file
          Conf.setProperty(Const.CONF_STARTUP_ITEM, "");
        }
      } else if (conf.matches(SearchResultType.WEBRADIO.name() + ".*")) {
        WebRadio radio = WebRadioManager.getInstance().getWebRadioByName(item);
        if (radio != null) {
          pv.sbSearch.setText(radio.getName());
        } else {
          // the file exists no more, remove its id as startup file
          Conf.setProperty(Const.CONF_STARTUP_ITEM, "");
        }
      }
    } else {
      pv.sbSearch.setText("");
    }
  }

  /**
   * Update advanced tab.
   *
   */
  private void updateGUIFromConfAdvanced() {
    final int backupSize = Conf.getInt(Const.CONF_BACKUP_SIZE);
    if (backupSize <= 0) { // backup size =0 means no backup
      pv.jcbBackup.setSelected(false);
      pv.backupSize.setEnabled(false);
    } else {
      pv.jcbBackup.setSelected(true);
      pv.backupSize.setEnabled(true);
    }
    pv.backupSize.setValue(backupSize);
    pv.jcbCollectionEncoding.setSelectedItem(Conf.getString(Const.CONF_COLLECTION_CHARSET));
    pv.jtfRefactorPattern.setText(Conf.getString(Const.CONF_PATTERN_REFACTOR));
    pv.jtfAnimationPattern.setText(Conf.getString(Const.CONF_PATTERN_ANIMATION));
    pv.jtfFrameTitle.setText(Conf.getString(Const.CONF_PATTERN_FRAME_TITLE));
    pv.jtfBalloonNotifierPattern.setText(Conf.getString(Const.CONF_PATTERN_BALLOON_NOTIFIER));
    pv.jtfInformationPattern.setText(Conf.getString(Const.CONF_PATTERN_INFORMATION));
    pv.jtfMPlayerPath.setText(Conf.getString(Const.CONF_MPLAYER_PATH_FORCED));
    pv.jtfMPlayerArgs.setText(Conf.getString(Const.CONF_MPLAYER_ARGS));
    pv.jtfEnvVariables.setText(Conf.getString(Const.CONF_ENV_VARIABLES));
    pv.jtfExplorerPath.setText(Conf.getString(Const.CONF_EXPLORER_PATH));
    pv.jcbRegexp.setSelected(Conf.getBoolean(Const.CONF_REGEXP));
    pv.jcbCheckUpdates.setSelected(Conf.getBoolean(Const.CONF_CHECK_FOR_UPDATE));
    pv.jcbForceFileDate.setSelected(Conf.getBoolean(Const.CONF_FORCE_FILE_DATE));
  }

  /**
   * Update selection network.
   *
   */
  private void updateGUIFromConfNetwork() {
    pv.jcbNoneInternetAccess.setSelected(Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS));
    final boolean bUseProxy = Conf.getBoolean(Const.CONF_NETWORK_USE_PROXY);
    pv.jcbProxyNone.setSelected(bUseProxy);
    pv.jtfProxyHostname.setText(Conf.getString(Const.CONF_NETWORK_PROXY_HOSTNAME));
    pv.jtfProxyHostname.setEnabled(bUseProxy);
    pv.jlProxyHostname.setEnabled(bUseProxy);
    pv.jtfProxyPort.setText(Conf.getString(Const.CONF_NETWORK_PROXY_PORT));
    pv.jtfProxyPort.setEnabled(bUseProxy);
    pv.jlProxyPort.setEnabled(bUseProxy);
    pv.jtfProxyLogin.setText(Conf.getString(Const.CONF_NETWORK_PROXY_LOGIN));
    pv.jtfProxyLogin.setEnabled(bUseProxy);
    pv.jlProxyLogin.setEnabled(bUseProxy);
    pv.jtfProxyPwd.setText(UtilString.rot13(Conf.getString(Const.CONF_NETWORK_PROXY_PWD)));
    pv.jtfProxyPwd.setEnabled(bUseProxy);
    pv.jlProxyPwd.setEnabled(bUseProxy);
    pv.connectionTO.setValue(Conf.getInt(Const.CONF_NETWORK_CONNECTION_TO));
    if (!Conf.getBoolean(Const.CONF_NETWORK_USE_PROXY)) {
      pv.jcbProxyNone.setSelected(true);
    } else if (Const.PROXY_TYPE_HTTP.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
      pv.jcbProxyHttp.setSelected(true);
    } else if (Const.PROXY_TYPE_SOCKS.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
      pv.jcbProxySocks.setSelected(true);
    }
  }

  /**
   * Update selection covers.
   *
   */
  private void updateGUIFromConfCovers() {
    pv.jcbAutoCover.setSelected(Conf.getBoolean(Const.CONF_COVERS_AUTO_COVER));
    pv.jlCoverSize.setEnabled(Conf.getBoolean(Const.CONF_COVERS_AUTO_COVER));
    pv.jcb3dCover.setSelected(Conf.getBoolean(Const.CONF_COVERS_MIRROW_COVER));
    pv.jcb3dCoverFS.setSelected(Conf.getBoolean(Const.CONF_COVERS_MIRROW_COVER_FS_MODE));
    pv.jcbCoverSize.setEnabled(Conf.getBoolean(Const.CONF_COVERS_AUTO_COVER));
    pv.jcbCoverSize.setSelectedIndex(Conf.getInt(Const.CONF_COVERS_SIZE));
    pv.jcbShuffleCover.setSelected(Conf.getBoolean(Const.CONF_COVERS_SHUFFLE));
    pv.jcbSaveExplorerFriendly.setSelected(Conf
        .getBoolean(Const.CONF_COVERS_SAVE_EXPLORER_FRIENDLY));
    pv.jtfDefaultCoverSearchPattern.setText(Conf.getString(Const.FILE_DEFAULT_COVER));
    pv.jcbAudioScrobbler.setSelected(Conf.getBoolean(Const.CONF_LASTFM_AUDIOSCROBBLER_ENABLE));
    pv.jcbEnableLastFMInformation.setSelected(Conf.getBoolean(Const.CONF_LASTFM_INFO));
    pv.jtfASUser.setText(Conf.getString(Const.CONF_LASTFM_USER));
    pv.jpfASPassword.setText(UtilString.rot13(Conf.getString(Const.CONF_LASTFM_PASSWORD)));
    if (!Conf.getBoolean(Const.CONF_LASTFM_AUDIOSCROBBLER_ENABLE)) {
      pv.jlASUser.setEnabled(false);
      pv.jtfASUser.setEnabled(false);
      pv.jlASPassword.setEnabled(false);
      pv.jpfASPassword.setEnabled(false);
    }
  }

  /**
   * Update GUI tab.
   */
  private void updateGUIFromConfGUI() {
    String notificatorType = Messages.getString(ParameterView.NOTIFICATOR_PREFIX
        + Conf.getString(Const.CONF_UI_NOTIFICATOR_TYPE));
    pv.jcbNotificationType.setSelectedItem(notificatorType);
    pv.jcbShowSystray.setSelected(Conf.getBoolean(Const.CONF_SHOW_SYSTRAY));
    pv.jcbMinimizeToTray.setSelected(Conf.getBoolean(Const.CONF_MINIMIZE_TO_TRAY));
    pv.jcbClickTrayAlwaysDisplayWindow.setSelected(Conf
        .getBoolean(Const.CONF_TRAY_CLICK_DISPLAY_WINDOW));
    pv.jcbSplashscreen.setSelected(Conf.getBoolean(Const.CONF_SPLASH_SCREEN));
    pv.scbLAF.removeActionListener(this);
    pv.scbLAF.setSelectedItem(Conf.getString(Const.CONF_OPTIONS_LNF));
    pv.scbLAF.addActionListener(this);
    pv.jsPerspectiveSize.setValue(Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE));
    pv.jcbTitleAnimation.setSelected(Conf.getBoolean(Const.CONF_TITLE_ANIMATION));
  }

  /**
  * Apply parameters options.
  * Options for "Options", "LastFM", "Sound" and "Modes" tabs
  *
  */
  private void updateConfFromGUIOptions() {
    Conf.setProperty(Const.CONF_OPTIONS_HIDE_UNMOUNTED,
        Boolean.toString(pv.jcbDisplayUnmounted.isSelected()));
    Conf.setProperty(Const.CONF_OPTIONS_PUSH_ON_CLICK,
        Boolean.toString(pv.jcbDefaultActionClick.isSelected()));
    Conf.setProperty(Const.CONF_OPTIONS_PUSH_ON_DROP,
        Boolean.toString(pv.jcbDefaultActionDrop.isSelected()));
    Conf.setProperty(Const.CONF_OPTIONS_HOTKEYS, Boolean.toString(pv.jcbHotkeys.isSelected()));
    Conf.setProperty(Const.CONF_LASTFM_AUDIOSCROBBLER_ENABLE,
        Boolean.toString(pv.jcbAudioScrobbler.isSelected()));
    Conf.setProperty(Const.CONF_LASTFM_INFO,
        Boolean.toString(pv.jcbEnableLastFMInformation.isSelected()));
    Conf.setProperty(Const.CONF_LASTFM_USER, pv.jtfASUser.getText());
    Conf.setProperty(Const.CONF_LASTFM_PASSWORD,
        UtilString.rot13(new String(pv.jpfASPassword.getPassword())));
    final int iLogLevel = pv.scbLogLevel.getSelectedIndex();
    Log.setVerbosity(iLogLevel);
    Conf.setProperty(Const.CONF_OPTIONS_LOG_LEVEL, Integer.toString(iLogLevel));
    Conf.setProperty(Const.CONF_OPTIONS_INTRO_BEGIN, Integer.toString(pv.introPosition.getValue()));
    Conf.setProperty(Const.CONF_OPTIONS_INTRO_LENGTH, Integer.toString(pv.introLength.getValue()));
    Conf.setProperty(Const.CONF_TAGS_USE_PARENT_DIR,
        Boolean.toString(pv.jcbUseParentDir.isSelected()));
    Conf.setProperty(Const.CONF_DROP_PLAYED_TRACKS_FROM_QUEUE,
        Boolean.toString(pv.jcbDropPlayedTracksFromQueue.isSelected()));
    final String sBestofSize = pv.jtfBestofSize.getText();
    if (!sBestofSize.isEmpty()) {
      Conf.setProperty(Const.CONF_BESTOF_TRACKS_SIZE, sBestofSize);
    }
    Locale locale = LocaleManager.getLocaleForDesc(((JLabel) pv.scbLanguage.getSelectedItem())
        .getText());
    final String sLocal = locale.getLanguage();
    Conf.setProperty(Const.CONF_OPTIONS_LANGUAGE, sLocal);
    // force refresh of bestof files
    RatingManager.setRateHasChanged(true);
    final String sNoveltiesAge = pv.jtfNoveltiesAge.getText();
    if (!sNoveltiesAge.isEmpty()) {
      Conf.setProperty(Const.CONF_OPTIONS_NOVELTIES_AGE, sNoveltiesAge);
    }
    final String sVisiblePlanned = pv.jtfVisiblePlanned.getText();
    if (!sVisiblePlanned.isEmpty()) {
      Conf.setProperty(Const.CONF_OPTIONS_VISIBLE_PLANNED, sVisiblePlanned);
    }
    final int oldDuration = Conf.getInt(Const.CONF_FADE_DURATION);
    // Show an hideable message if user set cross fade under linux for sound
    // server information
    if (UtilSystem.isUnderLinux() && (oldDuration == 0)
        && (oldDuration != pv.crossFadeDuration.getValue())) {
      Messages.showHideableWarningMessage(Messages.getString("ParameterView.210"),
          Const.CONF_NOT_SHOW_AGAIN_CROSS_FADE);
    }
    Conf.setProperty(Const.CONF_FADE_DURATION, Integer.toString(pv.crossFadeDuration.getValue()));
    Conf.setProperty(Const.CONF_USE_VOLNORM, Boolean.toString(pv.jcbUseVolnorm.isSelected()));
    Conf.setProperty(Const.CONF_BIT_PERFECT, Boolean.toString(pv.jcbEnableBitPerfect.isSelected()));
    boolean oldShowVideo = Conf.getBoolean(Const.CONF_SHOW_VIDEOS);
    if (oldShowVideo != pv.jcbShowVideos.isSelected()) {
      this.someOptionsAppliedAtNextStartup = true;
    }
    Conf.setProperty(Const.CONF_SHOW_VIDEOS, Boolean.toString(pv.jcbShowVideos.isSelected()));
    Conf.setProperty(Const.CONF_PRESERVE_FILE_DATES,
        Boolean.toString(pv.jcbPreserveFileDates.isSelected()));
  }

  /**
   * Apply parameters startup.
   *
   */
  private void updateConfFromGUIStartup() {
    if (pv.jrbNothing.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_NOTHING);
    } else if (pv.jrbLast.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST);
    } else if (pv.jrbLastKeepPos.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_LAST_KEEP_POS);
    } else if (pv.jrbShuffle.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_SHUFFLE);
    } else if (pv.jrbFile.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_ITEM);
    } else if (pv.jrbBestof.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_BESTOF);
    } else if (pv.jrbNovelties.isSelected()) {
      Conf.setProperty(Const.CONF_STARTUP_MODE, Const.STARTUP_MODE_NOVELTIES);
    }
  }

  /**
   * Apply parameters confirmation.
   *
   */
  private void updateConfFromGUIConfirmation() {
    Conf.setProperty(Const.CONF_CONFIRMATIONS_DELETE_FILE,
        Boolean.toString(pv.jcbBeforeDelete.isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_EXIT, Boolean.toString(pv.jcbBeforeExit.isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_REMOVE_DEVICE,
        Boolean.toString(pv.jcbBeforeRemoveDevice.isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_DELETE_COVER,
        Boolean.toString(pv.jcbBeforeDeleteCover.isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_CLEAR_HISTORY,
        Boolean.toString(pv.jcbBeforeClearingHistory.isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_RESET_RATINGS,
        Boolean.toString(pv.jcbBeforeResetingRatings.isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_REFACTOR_FILES,
        Boolean.toString(pv.jcbBeforeRefactorFiles.isSelected()));
    Conf.setProperty(Const.CONF_CONFIRMATIONS_BEFORE_TAG_WRITE,
        Boolean.toString(pv.jcbBeforeWritingTag.isSelected()));
  }

  /**
   * Apply parameters history.
   *
   */
  private void updateConfFromGUIHistory() {
    final String sHistoryDuration = pv.jtfHistory.getText();
    if (!sHistoryDuration.isEmpty()) {
      Conf.setProperty(Const.CONF_HISTORY, sHistoryDuration);
    }
    boolean oldManualValue = Conf.getBoolean(Const.CONF_MANUAL_RATINGS);
    if (pv.jcbManualRatings.isSelected() != oldManualValue) {
      Conf.setProperty(Const.CONF_MANUAL_RATINGS,
          Boolean.toString(pv.jcbManualRatings.isSelected()));
      ObservationManager.notify(new JajukEvent(JajukEvents.RATING_MODE_CHANGED));
    }
  }

  /**
   * Apply parameters patterns.
   *
   */
  private void updateConfFromGUIPatterns() {
    Conf.setProperty(Const.CONF_PATTERN_REFACTOR, pv.jtfRefactorPattern.getText());
    Conf.setProperty(Const.CONF_PATTERN_ANIMATION, pv.jtfAnimationPattern.getText());
    Conf.setProperty(Const.CONF_PATTERN_FRAME_TITLE, pv.jtfFrameTitle.getText());
    Conf.setProperty(Const.CONF_PATTERN_BALLOON_NOTIFIER, pv.jtfBalloonNotifierPattern.getText());
    Conf.setProperty(Const.CONF_PATTERN_INFORMATION, pv.jtfInformationPattern.getText());
  }

  /**
   * Apply parameters advanced.
   *
   */
  private void updateConfFromGUIAdvanced() {
    Conf.setProperty(Const.CONF_BACKUP_SIZE, Integer.toString(pv.backupSize.getValue()));
    Conf.setProperty(Const.CONF_COLLECTION_CHARSET, pv.jcbCollectionEncoding.getSelectedItem()
        .toString());
    Conf.setProperty(Const.CONF_REGEXP, Boolean.toString(pv.jcbRegexp.isSelected()));
    Conf.setProperty(Const.CONF_CHECK_FOR_UPDATE, Boolean.toString(pv.jcbCheckUpdates.isSelected()));
    Conf.setProperty(Const.CONF_FORCE_FILE_DATE, Boolean.toString(pv.jcbForceFileDate.isSelected()));
    // Apply new mplayer path and display a warning message if changed
    final String oldMplayerPath = Conf.getString(Const.CONF_MPLAYER_PATH_FORCED);
    if (!(oldMplayerPath.equals(pv.jtfMPlayerPath.getText()))) {
      this.someOptionsAppliedAtNextStartup = true;
    }
    Conf.setProperty(Const.CONF_MPLAYER_PATH_FORCED, pv.jtfMPlayerPath.getText());
    Conf.setProperty(Const.CONF_MPLAYER_ARGS, pv.jtfMPlayerArgs.getText());
    Conf.setProperty(Const.CONF_ENV_VARIABLES, pv.jtfEnvVariables.getText());
    Conf.setProperty(Const.CONF_EXPLORER_PATH, pv.jtfExplorerPath.getText());
  }

  /**
   * Apply parameters gui.
   *
   */
  private void updateConfFromGUIGUI() {
    Conf.setProperty(Const.CONF_CATALOG_PAGE_SIZE, Integer.toString(pv.jsCatalogPages.getValue()));
    Conf.setProperty(Const.CONF_SHOW_POPUPS, Boolean.toString(pv.jcbShowPopups.isSelected()));
    Conf.setProperty(Const.CONF_SPLASH_SCREEN, Boolean.toString(pv.jcbSplashscreen.isSelected()));
    final int oldFont = Conf.getInt(Const.CONF_FONTS_SIZE);
    // Display a message if font size changed
    if (oldFont != pv.jsFonts.getValue()) {
      this.someOptionsAppliedAtNextStartup = true;
    }
    Conf.setProperty(Const.CONF_FONTS_SIZE, Integer.toString(pv.jsFonts.getValue()));
    // Notificator type
    String notificatorTypeDisplayed = (String) pv.jcbNotificationType.getSelectedItem();
    for (NotificatorTypes notificatorType : NotificatorTypes.values()) {
      if (Messages.getString(ParameterView.NOTIFICATOR_PREFIX + notificatorType).equals(
          notificatorTypeDisplayed)) {
        Conf.setProperty(Const.CONF_UI_NOTIFICATOR_TYPE, notificatorType.name());
      }
    }
    // Message if show systray is changed
    final boolean bOldShowSystray = Conf.getBoolean(Const.CONF_SHOW_SYSTRAY);
    if (bOldShowSystray != pv.jcbShowSystray.isSelected()) {
      this.someOptionsAppliedAtNextStartup = true;
    }
    Conf.setProperty(Const.CONF_SHOW_SYSTRAY, Boolean.toString(pv.jcbShowSystray.isSelected()));
    Conf.setProperty(Const.CONF_TITLE_ANIMATION,
        Boolean.toString(pv.jcbTitleAnimation.isSelected()));
    // Minimize to tray
    Conf.setProperty(Const.CONF_MINIMIZE_TO_TRAY,
        Boolean.toString(pv.jcbMinimizeToTray.isSelected()));
    Conf.setProperty(Const.CONF_TRAY_CLICK_DISPLAY_WINDOW,
        Boolean.toString(pv.jcbClickTrayAlwaysDisplayWindow.isSelected()));
    final int oldPerspectiveSize = Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE);
    // If we perspective size changed and no font message have been already
    // displayed, display a message
    if (oldPerspectiveSize != pv.jsPerspectiveSize.getValue()) {
      this.someOptionsAppliedAtNextStartup = true;
    }
    Conf.setProperty(Const.CONF_PERSPECTIVE_ICONS_SIZE,
        Integer.toString(pv.jsPerspectiveSize.getValue()));
    // LAF change
    final String oldTheme = Conf.getString(Const.CONF_OPTIONS_LNF);
    Conf.setProperty(Const.CONF_OPTIONS_LNF, (String) pv.scbLAF.getSelectedItem());
    if (!oldTheme.equals(pv.scbLAF.getSelectedItem())) {
      // theme will be applied at next startup
      Messages.showHideableWarningMessage(Messages.getString("ParameterView.233"),
          Const.CONF_NOT_SHOW_AGAIN_LAF_CHANGE);
      pv.bLAFMessage = true;
    }
  }

  /**
  * Apply parameters network.
  *
  */
  private void updateConfFromGUINetwork() {
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS,
        Boolean.toString(pv.jcbNoneInternetAccess.isSelected()));
    Conf.setProperty(Const.CONF_NETWORK_USE_PROXY, Boolean.toString(!pv.jcbProxyNone.isSelected()));
    if (pv.jcbProxyHttp.isSelected()) {
      Conf.setProperty(Const.CONF_NETWORK_PROXY_TYPE, Const.PROXY_TYPE_HTTP);
    } else if (pv.jcbProxySocks.isSelected()) {
      Conf.setProperty(Const.CONF_NETWORK_PROXY_TYPE, Const.PROXY_TYPE_SOCKS);
    }
    Conf.setProperty(Const.CONF_NETWORK_PROXY_HOSTNAME, pv.jtfProxyHostname.getText());
    Conf.setProperty(Const.CONF_NETWORK_PROXY_PORT, pv.jtfProxyPort.getText());
    Conf.setProperty(Const.CONF_NETWORK_PROXY_LOGIN, pv.jtfProxyLogin.getText());
    Conf.setProperty(Const.CONF_NETWORK_PROXY_PWD,
        UtilString.rot13(new String(pv.jtfProxyPwd.getPassword())));
    Conf.setProperty(Const.CONF_NETWORK_CONNECTION_TO, Integer.toString(pv.connectionTO.getValue()));
    // Force global reload of proxy variables
    DownloadManager.setDefaultProxySettings();
  }

  /**
   * Apply parameters cover.
   *
   */
  private void updateConfFromGUICover() {
    Conf.setProperty(Const.CONF_COVERS_MIRROW_COVER, Boolean.toString(pv.jcb3dCover.isSelected()));
    Conf.setProperty(Const.CONF_COVERS_MIRROW_COVER_FS_MODE,
        Boolean.toString(pv.jcb3dCoverFS.isSelected()));
    ObservationManager.notify(new JajukEvent(JajukEvents.COVER_NEED_REFRESH));
    Conf.setProperty(Const.CONF_COVERS_AUTO_COVER, Boolean.toString(pv.jcbAutoCover.isSelected()));
    Conf.setProperty(Const.CONF_COVERS_SHUFFLE, Boolean.toString(pv.jcbShuffleCover.isSelected()));
    Conf.setProperty(Const.CONF_COVERS_SAVE_EXPLORER_FRIENDLY,
        Boolean.toString(pv.jcbSaveExplorerFriendly.isSelected()));
    Conf.setProperty(Const.CONF_COVERS_SIZE, Integer.toString(pv.jcbCoverSize.getSelectedIndex()));
    Conf.setProperty(Const.FILE_DEFAULT_COVER, pv.jtfDefaultCoverSearchPattern.getText());
  }

  /**
  * Handle workspace change.
  *
  */
  private void handleWorkspaceChange() {
    if ((SessionService.getWorkspace() != null)
        && !SessionService.getWorkspace().equals(pv.psJajukWorkspace.getUrl())) {
      // Check workspace directory
      if (!pv.psJajukWorkspace.getUrl().trim().isEmpty()) {
        // Check workspace presence and create it if required
        final java.io.File fWorkspace = new java.io.File(pv.psJajukWorkspace.getUrl());
        if (!fWorkspace.exists() && !fWorkspace.mkdirs()) {
          Log.warn("Could not create directory " + fWorkspace.toString());
        }
        if (!fWorkspace.canRead()) {
          Messages.showErrorMessage(165);
          return;
        }
      }
      try {
        final String newWorkspace = pv.psJajukWorkspace.getUrl();
        // If target workspace doesn't exist, copy current repository to
        // the new workspace
        // (keep old repository for security and for use
        // by others users in multi-session mode)
        boolean bPreviousPathExist = true;
        // bPreviousPathExist is true if destination workspace already
        // exists,
        // it is then only a workspace switch
        if (!new java.io.File(pv.psJajukWorkspace.getUrl() + '/'
            + (SessionService.isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk"))
            .exists()) {
          UtilGUI.waiting();
          final java.io.File from = SessionService.getConfFileByPath("");
          final java.io.File dest = new java.io.File(newWorkspace + '/'
              + (SessionService.isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk"));
          // Remove the session file to avoid getting a message when
          // switching to new workspace
          java.io.File session = SessionService.getSessionIdFile();
          session.delete();
          UtilSystem.copyRecursively(from, dest);
          bPreviousPathExist = false;
        }
        // Change the workspace so the very last conf (like current
        // track)
        // will be saved directly to target workspace. We don't do
        // this if the workspace already exist to avoid overwriting other
        // configuration.
        SessionService.setWorkspace(pv.psJajukWorkspace.getUrl());
        //Commit the bootstrap file
        SessionService.commitBootstrapFile();
        UtilGUI.stopWaiting();
        // Display a warning message and restart Jajuk
        if (bPreviousPathExist) {
          Messages.getChoice(Messages.getString("ParameterView.247"), JOptionPane.DEFAULT_OPTION,
              JOptionPane.INFORMATION_MESSAGE);
        } else {
          Messages.getChoice(Messages.getString("ParameterView.209"), JOptionPane.DEFAULT_OPTION,
              JOptionPane.INFORMATION_MESSAGE);
        }
        // Exit Jajuk
        try {
          ActionManager.getAction(JajukActions.EXIT).perform(null);
        } catch (Exception e1) {
          Log.error(e1);
        }
      } catch (final Exception e) {
        Messages.showErrorMessage(24);
        Log.error(e);
      }
    }
  }

  /*
  * (non-Javadoc)
  *
  * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
  */
  @Override
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == pv.jbClearHistory) {
      // show confirmation message if required
      if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_CLEAR_HISTORY)) {
        final int iResu = Messages.getChoice(Messages.getString("Confirmation_clear_history"),
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (iResu != JOptionPane.YES_OPTION) {
          return;
        }
      }
      ObservationManager.notify(new JajukEvent(JajukEvents.CLEAR_HISTORY));
    } else if (e.getSource() == pv.scbLAF) {
      // Refresh full GUI at each LAF change as a preview
      UtilGUI.setupSubstanceLookAndFeel((String) pv.scbLAF.getSelectedItem());
      UtilGUI.updateAllUIs();
    } else if (e.getSource() == pv.jbResetRatings) {
      // show confirmation message if required
      if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_RESET_RATINGS)) {
        final int iResu = Messages.getChoice(Messages.getString("Confirmation_reset_ratings"),
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (iResu != JOptionPane.YES_OPTION) {
          return;
        }
      }
      ObservationManager.notify(new JajukEvent(JajukEvents.RATE_RESET));
    } else if (e.getSource() == pv.jbResetPreferences) {
      // show confirmation message if required
      if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_RESET_RATINGS)) {
        final int iResu = Messages.getChoice(Messages.getString("Confirmation_reset_preferences"),
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (iResu != JOptionPane.YES_OPTION) {
          return;
        }
      }
      if (!DeviceManager.getInstance().isAnyDeviceRefreshing()) {
        ObservationManager.notify(new JajukEvent(JajukEvents.PREFERENCES_RESET));
      } else {
        Messages.showErrorMessage(120);
      }
    } else if (e.getSource() == pv.jbOK) {
      updateConfFromGUI();
      // Notify any client than wait for parameters updates
      final Properties details = new Properties();
      details.put(Const.DETAIL_ORIGIN, this);
      ObservationManager.notify(new JajukEvent(JajukEvents.PARAMETERS_CHANGE, details));
      if (someOptionsAppliedAtNextStartup) {
        // Inform user that some parameters will apply only at
        // next startup
        Messages.showInfoMessage(Messages.getString("ParameterView.198"));
        someOptionsAppliedAtNextStartup = false;
      }
      // Update Mute state according to bit-perfect mode
      ActionManager.getAction(JajukActions.MUTE_STATE).setEnabled(
          !Conf.getBoolean(Const.CONF_BIT_PERFECT));
    } else if (e.getSource() == pv.jbDefault) {
      int resu = Messages.getChoice(Messages.getString("Confirmation_defaults"),
          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
      if (resu == JOptionPane.OK_OPTION) {
        Conf.setDefaultProperties();
        updateGUIFromConf();// update UI
        InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.110"),
            InformationJPanel.MessageType.INFORMATIVE);
        updateConfFromGUI();
        Messages.showInfoMessage(Messages.getString("ParameterView.198"));
      }
    } else if (e.getSource() == pv.jcbBackup) {
      // if backup option is unchecked, reset backup size
      if (pv.jcbBackup.isSelected()) {
        pv.backupSize.setEnabled(true);
        pv.backupSize.setValue(Conf.getInt(Const.CONF_BACKUP_SIZE));
      } else {
        pv.backupSize.setEnabled(false);
        pv.backupSize.setValue(0);
      }
    } else if ((e.getSource() == pv.jcbProxyNone) || (e.getSource() == pv.jcbProxyHttp)
        || (e.getSource() == pv.jcbProxySocks)) {
      final boolean bUseProxy = !pv.jcbProxyNone.isSelected();
      pv.jtfProxyHostname.setEnabled(bUseProxy);
      pv.jtfProxyPort.setEnabled(bUseProxy);
      pv.jtfProxyLogin.setEnabled(bUseProxy);
      pv.jtfProxyPwd.setEnabled(bUseProxy);
      pv.jlProxyHostname.setEnabled(bUseProxy);
      pv.jlProxyPort.setEnabled(bUseProxy);
      pv.jlProxyLogin.setEnabled(bUseProxy);
      pv.jlProxyPwd.setEnabled(bUseProxy);
    } else if (e.getSource() == pv.jcbAutoCover) {
      if (pv.jcbAutoCover.isSelected()) {
        pv.jcbCoverSize.setEnabled(true);
        pv.jlCoverSize.setEnabled(true);
      } else {
        pv.jlCoverSize.setEnabled(false);
        pv.jcbCoverSize.setEnabled(false);
      }
    } else if (e.getSource() == pv.jcbAudioScrobbler) {
      if (pv.jcbAudioScrobbler.isSelected()) {
        pv.jlASUser.setEnabled(true);
        pv.jtfASUser.setEnabled(true);
        pv.jlASPassword.setEnabled(true);
        pv.jpfASPassword.setEnabled(true);
      } else {
        pv.jlASUser.setEnabled(false);
        pv.jtfASUser.setEnabled(false);
        pv.jlASPassword.setEnabled(false);
        pv.jpfASPassword.setEnabled(false);
      }
    } else if (e.getSource() == pv.scbLanguage) {
      Locale locale = LocaleManager.getLocaleForDesc(((JLabel) pv.scbLanguage.getSelectedItem())
          .getText());
      final String sLocal = locale.getLanguage();
      final String sPreviousLocal = LocaleManager.getLocale().getLanguage();
      if (!sPreviousLocal.equals(sLocal)) {
        // local has changed
        someOptionsAppliedAtNextStartup = true;
      }
    } else if (e.getSource() == pv.jcbHotkeys) {
      someOptionsAppliedAtNextStartup = true;
    } else if (e.getSource() == pv.jbCatalogRefresh) {
      new Thread("Parameter Catalog refresh Thread") {
        @Override
        public void run() {
          UtilGUI.waiting();
          // Force albums to search for new covers
          AlbumManager.getInstance().resetCoverCache();
          // Clean thumbs
          ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_50X50);
          ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_100X100);
          ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_150X150);
          ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_200X200);
          ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_250X250);
          ThumbnailManager.cleanThumbs(Const.THUMBNAIL_SIZE_300X300);
          UtilGUI.stopWaiting();
          // For catalog view's update
          ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
          // Display a message
          Messages.showInfoMessage(Messages.getString("Success"));
        }
      }.start();
    }
    // Bit-perfect and audio normalization/cross fade options are mutually exclusive
    else if (e.getSource().equals(pv.jcbEnableBitPerfect)) {
      pv.jcbUseVolnorm.setEnabled(!pv.jcbEnableBitPerfect.isSelected());
      if (pv.jcbUseVolnorm.isSelected() && pv.jcbEnableBitPerfect.isSelected()) {
        pv.jcbUseVolnorm.setSelected(false);
      }
      pv.crossFadeDuration.setEnabled(!pv.jcbEnableBitPerfect.isSelected());
    }
  }

  /*
  * (non-Javadoc)
  *
  * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
  */
  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getSource() == pv.jrbFile) { // jrbFile has been selected or
      // deselected
      pv.sbSearch.setEnabled(pv.jrbFile.isSelected());
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent )
   */
  @Override
  public void stateChanged(final ChangeEvent e) {
    // when changing tab, store it for future jajuk sessions
    Conf.setProperty(Const.CONF_OPTIONS_TAB, Integer.toString(pv.jtpMain.getSelectedIndex()));
  }
}
