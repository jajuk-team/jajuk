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
package org.jajuk.ui.views;

import java.awt.Component;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.SearchResult;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.notification.NotificatorTypes;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.DefaultMouseWheelListener;
import org.jajuk.ui.helpers.PatternInputVerifier;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.InformationJPanel.MessageType;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.PathSelector;
import org.jajuk.ui.widgets.SearchBox;
import org.jajuk.ui.widgets.SteppedComboBox;
import org.jajuk.ui.widgets.ToggleLink;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.LocaleManager;
import org.jajuk.util.Messages;
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
public class ParameterView extends ViewAdapter {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** GUI updater. */
  private ParameterViewGUIHelper updateHelper = new ParameterViewGUIHelper(this);
  /** The Constant NOTIFICATOR_PREFIX.  */
  static final String NOTIFICATOR_PREFIX = "Notificator.";
  JTabbedPane jtpMain;
  JTextField jtfHistory;
  JCheckBox jcbManualRatings;
  JButton jbClearHistory;
  JButton jbResetRatings;
  /** Allows to export ratings to a file */
  JButton jbExportRatings;
  /** Allows to import ratings from a file */
  JButton jbImportRatings;
  JButton jbResetPreferences;
  ButtonGroup bgStart;
  JRadioButton jrbNothing;
  JRadioButton jrbLast;
  JRadioButton jrbLastKeepPos;
  JRadioButton jrbShuffle;
  JRadioButton jrbBestof;
  JRadioButton jrbNovelties;
  JRadioButton jrbFile;
  SearchBox sbSearch;
  JCheckBox jcbBeforeDelete;
  JCheckBox jcbBeforeExit;
  JCheckBox jcbBeforeRemoveDevice;
  JCheckBox jcbBeforeDeleteCover;
  JCheckBox jcbBeforeClearingHistory;
  JCheckBox jcbBeforeResetingRatings;
  JCheckBox jcbBeforeRefactorFiles;
  JCheckBox jcbBeforeWritingTag;
  JCheckBox jcbDisplayUnmounted;
  JCheckBox jcbAudioScrobbler;
  JButton jbResetDontShowAgain;
  JLabel jlASUser;
  JTextField jtfASUser;
  JLabel jlASPassword;
  JPasswordField jpfASPassword;
  SteppedComboBox scbLanguage;
  JTextField jtfFrameTitle;
  /** Balloon notifier pattern text field. */
  JTextField jtfBalloonNotifierPattern;
  /** Information pattern textfield. */
  JTextField jtfInformationPattern;
  JLabel jlLAF;
  SteppedComboBox scbLAF;
  SteppedComboBox scbLogLevel;
  JSlider introPosition;
  JSlider introLength;
  JTextField jtfBestofSize;
  JTextField jtfNoveltiesAge;
  JTextField jtfVisiblePlanned;
  JSlider crossFadeDuration;
  JCheckBox jcbDefaultActionClick;
  JCheckBox jcbDefaultActionDrop;
  JLabel jlNotificationType;
  JComboBox jcbNotificationType;
  JCheckBox jcbHotkeys;
  JCheckBox jcbShowVideos;
  JCheckBox jcbPreserveFileDates;
  JCheckBox jcbUseParentDir;
  JFormattedTextField jtfRefactorPattern;
  JTextField jtfAnimationPattern;
  JCheckBox jcbBackup;
  JSlider backupSize;
  JComboBox jcbCollectionEncoding;
  JCheckBox jcbRegexp;
  ButtonGroup bgProxy;
  JCheckBox jcbNoneInternetAccess;
  JRadioButton jcbProxyNone;
  JRadioButton jcbProxyHttp;
  JRadioButton jcbProxySocks;
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
  JCheckBox jcbAutoCover;
  JCheckBox jcbShuffleCover;
  JLabel jlCoverSize;
  JComboBox jcbCoverSize;
  JTextField jtfMPlayerArgs;
  JTextField jtfEnvVariables;
  JTextField jtfMPlayerPath;
  PathSelector psJajukWorkspace;
  JLabel jlCatalogPages;
  JSlider jsCatalogPages;
  JButton jbCatalogRefresh;
  JCheckBox jcbShowPopups;
  JCheckBox jcbShowSystray;
  JCheckBox jcbMinimizeToTray;
  JCheckBox jcbClickTrayAlwaysDisplayWindow;
  JLabel jlFonts;
  JSlider jsFonts;
  JCheckBox jcbEnableLastFMInformation;
  JButton jbOK;
  JButton jbDefault;
  JCheckBox jcbCheckUpdates;
  JCheckBox jcbForceFileDate;
  JSlider jsPerspectiveSize;
  /** VolNorm checkbox. */
  JCheckBox jcbUseVolnorm;
  /** Bit-perfect checkbox. */
  JCheckBox jcbEnableBitPerfect;
  JTextField jtfExplorerPath;
  /** Whether the "theme will be token into account" message has been already displayed. */
  boolean bLAFMessage = false;
  JLabel jlDefaultCoverSearchPattern;
  JTextField jtfDefaultCoverSearchPattern;
  JCheckBox jcbSaveExplorerFriendly;
  JCheckBox jcbDropPlayedTracksFromQueue;
  JCheckBox jcb3dCover;
  JCheckBox jcb3dCoverFS;
  /** Enable Title view animation effect. */
  JCheckBox jcbTitleAnimation;
  /** Splashscreen flag. */
  JCheckBox jcbSplashscreen;
  JButton jbReloadRadiosPreset;

  /**
   * View providing main jajuk configuration GUI. Known in the doc as
   * "Preferences view"
   */
  public ParameterView() {
    super();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("ParameterView.87");
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    final Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  /**
   * Inits the ui history.
   *
   *
   * @return the j panel
   */
  private JPanel initUIHistory() {
    JPanel jpHistory = new JPanel(new MigLayout("insets 10, gapy 15", "[fill][fill]"));
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
    jcbManualRatings = new JCheckBox(Messages.getString("ParameterView.307"));
    jcbManualRatings.setToolTipText(Messages.getString("ParameterView.308"));
    jbClearHistory = new JButton(Messages.getString("ParameterView.3"),
        IconLoader.getIcon(JajukIcons.CLEAR));
    jbClearHistory.setToolTipText(Messages.getString("ParameterView.4"));
    jbClearHistory.addActionListener(updateHelper);
    jbResetRatings = new JButton(Messages.getString("ParameterView.186"),
        IconLoader.getIcon(JajukIcons.CLEAR));
    jbResetRatings.setToolTipText(Messages.getString("ParameterView.187"));
    jbResetRatings.addActionListener(updateHelper);
    jbResetPreferences = new JButton(Messages.getString("ParameterView.249"),
        IconLoader.getIcon(JajukIcons.CLEAR));
    jbResetPreferences.setToolTipText(Messages.getString("ParameterView.250"));
    jbResetPreferences.addActionListener(updateHelper);
    jbExportRatings = new JButton(ActionManager.getAction(JajukActions.EXPORT_RATINGS));
    jbImportRatings = new JButton(ActionManager.getAction(JajukActions.IMPORT_RATINGS));
    JLabel jlHistory = new JLabel(Messages.getString("ParameterView.0"));
    jlHistory.setToolTipText(Messages.getString("ParameterView.2"));
    jpHistory.add(jlHistory);
    jpHistory.add(jtfHistory);
    jpHistory.add(jbClearHistory, "wrap");
    jpHistory.add(jcbManualRatings, "wrap");
    jpHistory.add(jbResetRatings);
    jpHistory.add(jbResetPreferences, "wrap");
    jpHistory.add(jbExportRatings);
    jpHistory.add(jbImportRatings);
    return jpHistory;
  }

  /**
   * Inits the ui startup.
   *
   *
   * @return the jpanel
   */
  private JPanel initUIStartup() {
    JPanel jpStart = new JPanel(new MigLayout("insets 10,gapy 15", "[][grow][]"));
    bgStart = new ButtonGroup();
    jrbNothing = new JRadioButton(Messages.getString("ParameterView.10"));
    jrbNothing.setToolTipText(Messages.getString("ParameterView.11"));
    jrbNothing.addItemListener(updateHelper);
    jrbLast = new JRadioButton(Messages.getString("ParameterView.12"));
    jrbLast.setToolTipText(Messages.getString("ParameterView.13"));
    jrbLast.addItemListener(updateHelper);
    jrbLastKeepPos = new JRadioButton(Messages.getString("ParameterView.135"));
    jrbLastKeepPos.setToolTipText(Messages.getString("ParameterView.136"));
    jrbLastKeepPos.addItemListener(updateHelper);
    jrbShuffle = new JRadioButton(Messages.getString("ParameterView.14"));
    jrbShuffle.setToolTipText(Messages.getString("ParameterView.15"));
    jrbShuffle.addItemListener(updateHelper);
    jrbBestof = new JRadioButton(Messages.getString("ParameterView.131"));
    jrbBestof.setToolTipText(Messages.getString("ParameterView.132"));
    jrbBestof.addItemListener(updateHelper);
    jrbNovelties = new JRadioButton(Messages.getString("ParameterView.133"));
    jrbNovelties.setToolTipText(Messages.getString("ParameterView.134"));
    jrbNovelties.addItemListener(updateHelper);
    jrbFile = new JRadioButton(Messages.getString("ParameterView.16"));
    jrbFile.setToolTipText(Messages.getString("ParameterView.17"));
    jrbFile.addItemListener(updateHelper);
    sbSearch = new SearchBox() {
      private static final long serialVersionUID = 1L;

      @Override
      public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          final SearchResult sr = sbSearch.getResult();
          if (sr.getType() == SearchResultType.FILE) {
            Conf.setProperty(Const.CONF_STARTUP_ITEM, SearchResultType.FILE.name() + '/'
                + sr.getFile().getID());
            sbSearch.setText(sr.getFile().getTrack().getName());
          } else if (sr.getType() == SearchResultType.WEBRADIO) {
            Conf.setProperty(Const.CONF_STARTUP_ITEM, SearchResultType.WEBRADIO.name() + '/'
                + sr.getWebradio().getName());
            sbSearch.setText(sr.getWebradio().getName());
          }
          sbSearch.hidePopup();
        }
      }
    };
    // disabled by default, is enabled only if jrbFile is enabled
    sbSearch.setEnabled(false);
    sbSearch.setToolTipText(Messages.getString("ParameterView.18"));
    bgStart.add(jrbNothing);
    bgStart.add(jrbLast);
    bgStart.add(jrbLastKeepPos);
    bgStart.add(jrbShuffle);
    bgStart.add(jrbBestof);
    bgStart.add(jrbNovelties);
    bgStart.add(jrbFile);
    jpStart.add(jrbNothing, "wrap");
    jpStart.add(jrbLast, "wrap");
    jpStart.add(jrbLastKeepPos, "wrap");
    jpStart.add(jrbShuffle, "wrap");
    jpStart.add(jrbBestof, "wrap");
    jpStart.add(jrbNovelties, "wrap");
    jpStart.add(jrbFile);
    jpStart.add(sbSearch, "grow,wrap"); //NOSONAR
    return jpStart;
  }

  /**
  * Inits the webradios panel.
  *
  *
  * @return the j panel
  */
  private JPanel initWebradios() {
    JPanel jpWebradios = new JPanel(new MigLayout("insets 10, gapy 15"));
    jbReloadRadiosPreset = new JButton(Messages.getString("WebRadioView.10"),
        IconLoader.getIcon(JajukIcons.CLEAR));
    jbReloadRadiosPreset.setToolTipText(Messages.getString("WebRadioView.11"));
    jbReloadRadiosPreset.addActionListener(updateHelper);
    jpWebradios.add(jbReloadRadiosPreset);
    return jpWebradios;
  }

  /**
   * Inits the ui confirmations.
   *
   *
   * @return the jpanel
   */
  private JPanel initUIConfirmations() {
    JPanel jpConfirmations = new JPanel(new MigLayout("insets 10,gapy 15"));
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
    jcbBeforeWritingTag = new JCheckBox(Messages.getString("ParameterView.309"));
    jcbBeforeWritingTag.setToolTipText(Messages.getString("ParameterView.309"));
    jbResetDontShowAgain = new JButton(Messages.getString("ParameterView.310"));
    jbResetDontShowAgain.setToolTipText(Messages.getString("ParameterView.311"));
    jbResetDontShowAgain.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Conf.resetDontShowAgain();
        InformationJPanel.getInstance().setMessage(Messages.getString("Success"),
            MessageType.INFORMATIVE);
      }
    });
    jpConfirmations.add(jcbBeforeDelete, "wrap");
    jpConfirmations.add(jcbBeforeExit, "wrap");
    jpConfirmations.add(jcbBeforeRemoveDevice, "wrap");
    jpConfirmations.add(jcbBeforeDeleteCover, "wrap");
    jpConfirmations.add(jcbBeforeClearingHistory, "wrap");
    jpConfirmations.add(jcbBeforeResetingRatings, "wrap");
    jpConfirmations.add(jcbBeforeRefactorFiles, "wrap");
    jpConfirmations.add(jcbBeforeWritingTag, "wrap");
    jpConfirmations.add(jbResetDontShowAgain);
    return jpConfirmations;
  }

  /**
   * Inits the sound tab.
   *
   * @return the jpanel
   */
  private JPanel initUISound() {
    JPanel jpSound = new JPanel(new MigLayout("insets 10,gapy 15,gapx 10", "[][grow,200:300:300]"));//NOSONAR
    JLabel jlCrossFadeDuration = new JLabel(Messages.getString("ParameterView.190"));
    jlCrossFadeDuration.setToolTipText(Messages.getString("ParameterView.191"));
    crossFadeDuration = new JSlider(0, 30, 0);
    crossFadeDuration.setMajorTickSpacing(10);
    crossFadeDuration.setMinorTickSpacing(1);
    crossFadeDuration.setPaintTicks(true);
    crossFadeDuration.setPaintLabels(true);
    crossFadeDuration.setToolTipText(Messages.getString("ParameterView.191"));
    crossFadeDuration.addMouseWheelListener(new DefaultMouseWheelListener(crossFadeDuration));
    jcbUseVolnorm = new JCheckBox(Messages.getString("ParameterView.262"));
    jcbUseVolnorm.setToolTipText(Messages.getString("ParameterView.263"));
    jcbUseVolnorm.addActionListener(updateHelper);
    jcbEnableBitPerfect = new JCheckBox(Messages.getString("ParameterView.285"));
    jcbEnableBitPerfect.setToolTipText(Messages.getString("ParameterView.286"));
    jcbEnableBitPerfect.addActionListener(updateHelper);
    jpSound.add(jlCrossFadeDuration);
    jpSound.add(crossFadeDuration, "grow,wrap");
    jpSound.add(jcbUseVolnorm, "wrap");
    jpSound.add(jcbEnableBitPerfect);
    return jpSound;
  }

  /**
   * Inits the ui modes.
   *
   * @return the jpanel
   */
  private JPanel initUIModes() {
    // Intro
    // intro position
    introPosition = new JSlider(0, 100, 0);
    introPosition.setMajorTickSpacing(20);
    introPosition.setMinorTickSpacing(10);
    introPosition.setPaintTicks(true);
    introPosition.setPaintLabels(true);
    introPosition.setToolTipText(Messages.getString("ParameterView.60"));
    introPosition.addMouseWheelListener(new DefaultMouseWheelListener(introPosition));
    // intro length
    JLabel jlIntroLength = new JLabel(Messages.getString("ParameterView.61"));
    jlIntroLength.setToolTipText(Messages.getString("ParameterView.62"));
    introLength = new JSlider(0, 30, 20);
    introLength.setMajorTickSpacing(10);
    introLength.setMinorTickSpacing(1);
    introLength.setPaintTicks(true);
    introLength.setPaintLabels(true);
    introLength.setToolTipText(Messages.getString("ParameterView.62"));
    introLength.addMouseWheelListener(new DefaultMouseWheelListener(introLength));
    // Best of size
    JLabel jlBestofSize = new JLabel(Messages.getString("ParameterView.111"));
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
        final JTextField tf = (JTextField) input; //NOSONAR
        final String sText = tf.getText();
        try {
          final int iValue = Integer.parseInt(sText);
          if ((iValue < 1) || (iValue > 100)) {
            jbOK.setEnabled(false);
            return false;
          }
        } catch (final RuntimeException e) {
          return false;
        }
        jbOK.setEnabled(true);
        return true;
      }
    });
    // novelties age
    JLabel jlNoveltiesAge = new JLabel(Messages.getString("ParameterView.129"));
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
        final JTextField tf = (JTextField) input; //NOSONAR
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
    JLabel jlVisiblePlanned = new JLabel(Messages.getString("ParameterView.177"));
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
        final JTextField tf = (JTextField) input;//NOSONAR
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
    // add panels
    JPanel jpModes = new JPanel(new MigLayout("insets 10,gapy 15,gapx 10", "[][grow,200:300:300]"));
    jpModes.add(new JLabel(Messages.getString("ParameterView.59")));
    jpModes.add(introPosition, "grow,wrap");
    jpModes.add(jlIntroLength);
    jpModes.add(introLength, "grow,wrap");
    jpModes.add(jlBestofSize);
    jpModes.add(jtfBestofSize, "grow,wrap");
    jpModes.add(jlNoveltiesAge);
    jpModes.add(jtfNoveltiesAge, "grow,wrap");
    jpModes.add(jlVisiblePlanned);
    jpModes.add(jtfVisiblePlanned, "grow,wrap");
    return jpModes;
  }

  /**
   * Inits the ui options.
   *
   *
   * @return the jpanel
   */
  private JPanel initUIOptions() {
    jcbDisplayUnmounted = new JCheckBox(Messages.getString("JajukJMenuBar.24"));
    jcbDisplayUnmounted.setToolTipText(Messages.getString("ParameterView.35"));
    jcbDefaultActionClick = new JCheckBox(Messages.getString("ParameterView.179"));
    jcbDefaultActionClick.setToolTipText(Messages.getString("ParameterView.180"));
    jcbDefaultActionDrop = new JCheckBox(Messages.getString("ParameterView.181"));
    jcbDefaultActionDrop.setToolTipText(Messages.getString("ParameterView.182"));
    jcbHotkeys = new JCheckBox(Messages.getString("ParameterView.196"));
    jcbHotkeys.addActionListener(updateHelper);
    jcbHotkeys.setToolTipText(Messages.getString("ParameterView.197"));
    // Disable this option if not under windows
    jcbHotkeys.setEnabled(UtilSystem.isUnderWindows());
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
    for (String sDesc : LocaleManager.getLocalesDescs()) {
      scbLanguage.addItem(new JLabel(sDesc, Messages.getIcon(sDesc), SwingConstants.LEFT));
    }
    scbLanguage.setToolTipText(Messages.getString("ParameterView.42"));
    scbLanguage.addActionListener(updateHelper);
    jcbUseParentDir = new JCheckBox(Messages.getString("ParameterView.101"));
    jcbUseParentDir.setToolTipText(Messages.getString("ParameterView.102"));
    jcbDropPlayedTracksFromQueue = new JCheckBox(Messages.getString("ParameterView.266"));
    jcbDropPlayedTracksFromQueue.setToolTipText(Messages.getString("ParameterView.267"));
    jcbShowVideos = new JCheckBox(Messages.getString("ParameterView.301"));
    jcbShowVideos.setToolTipText(Messages.getString("ParameterView.302"));
    jcbPreserveFileDates = new JCheckBox(Messages.getString("ParameterView.305"));
    jcbPreserveFileDates.setToolTipText(Messages.getString("ParameterView.306"));
    JPanel jpOptions = new JPanel(new MigLayout("insets 10, gapy 15, wrap 1"));
    jpOptions.add(new JLabel(Messages.getString("ParameterView.38")), "split 2,gapleft 5");
    jpOptions.add(scbLanguage);
    jpOptions.add(jcbDisplayUnmounted);
    jpOptions.add(jcbDefaultActionClick);
    jpOptions.add(jcbDefaultActionDrop);
    jpOptions.add(jcbHotkeys);
    jpOptions.add(jcbUseParentDir);
    jpOptions.add(jcbDropPlayedTracksFromQueue);
    jpOptions.add(jcbShowVideos);
    jpOptions.add(jcbPreserveFileDates);
    return jpOptions;
  }

  /**
   * Inits the ui patterns.
   *
   *
   * @return the jpanel
   */
  private JPanel initUIPatterns() {
    JPanel patterns = new JPanel(new MigLayout("insets 10, gapy 15, wrap 2", "[][grow]"));
    JLabel jlRefactorPattern = new JLabel(Messages.getString("ParameterView.192"));
    jlRefactorPattern.setToolTipText(Messages.getString("ParameterView.193"));//NOSONAR
    jtfRefactorPattern = new JFormattedTextField();
    jtfRefactorPattern.setToolTipText(Messages.getString("ParameterView.193"));
    jtfRefactorPattern.setInputVerifier(new PatternInputVerifier());
    JLabel jlAnimationPattern = new JLabel(Messages.getString("ParameterView.195"));
    jlAnimationPattern.setToolTipText(Messages.getString("ParameterView.193"));
    jtfAnimationPattern = new JTextField();
    jtfAnimationPattern.setToolTipText(Messages.getString("ParameterView.193"));
    // Frame Title Options
    JLabel jlFrameTitle = new JLabel(Messages.getString("ParameterView.248"));
    jlFrameTitle.setToolTipText(Messages.getString("ParameterView.193"));
    jtfFrameTitle = new JTextField();
    jtfFrameTitle.setToolTipText(Messages.getString("ParameterView.193"));
    // Balloon Notifier pattern
    JLabel jlBalloonNotifierPattern = new JLabel(Messages.getString("ParameterView.277"));
    jlBalloonNotifierPattern.setToolTipText(Messages.getString("ParameterView.278"));
    jtfBalloonNotifierPattern = new JTextField();
    jtfBalloonNotifierPattern.setToolTipText(Messages.getString("ParameterView.278"));
    // Information view pattern
    JLabel jlInformationPattern = new JLabel(Messages.getString("ParameterView.279"));
    jlInformationPattern.setToolTipText(Messages.getString("ParameterView.280"));
    jtfInformationPattern = new JTextField();
    jtfInformationPattern.setToolTipText(Messages.getString("ParameterView.280"));
    patterns.add(jlRefactorPattern);
    patterns.add(jtfRefactorPattern, "grow"); //NOSONAR
    patterns.add(jlAnimationPattern);
    patterns.add(jtfAnimationPattern, "grow");
    patterns.add(jlFrameTitle);
    patterns.add(jtfFrameTitle, "grow");
    patterns.add(jlBalloonNotifierPattern);
    patterns.add(jtfBalloonNotifierPattern, "grow");
    patterns.add(jlInformationPattern);
    patterns.add(jtfInformationPattern, "grow");
    return patterns;
  }

  /**
   * Inits the ui advanced.
   *
   *
   * @return the jpanel
   */
  private JPanel initUIAdvanced() {
    jcbBackup = new JCheckBox(Messages.getString("ParameterView.116"));
    jcbBackup.addActionListener(updateHelper);
    jcbBackup.setToolTipText(Messages.getString("ParameterView.117"));
    backupSize = new JSlider(0, 100);
    backupSize.setMajorTickSpacing(20);
    backupSize.setMinorTickSpacing(10);
    backupSize.setPaintTicks(true);
    backupSize.setPaintLabels(true);
    backupSize.setToolTipText(Messages.getString("ParameterView.119"));
    backupSize.addMouseWheelListener(new DefaultMouseWheelListener(backupSize));
    JLabel jlCollectionEncoding = new JLabel(Messages.getString("ParameterView.120"));
    jlCollectionEncoding.setToolTipText(Messages.getString("ParameterView.121"));
    jcbCollectionEncoding = new JComboBox();
    jcbCollectionEncoding.setToolTipText(Messages.getString("ParameterView.121"));
    jcbRegexp = new JCheckBox(Messages.getString("ParameterView.113"));
    jcbRegexp.setToolTipText(Messages.getString("ParameterView.114"));
    jcbCollectionEncoding.addItem("UTF-8");
    jcbCollectionEncoding.addItem("UTF-16");
    JLabel jlLogLevel = new JLabel(Messages.getString("ParameterView.46"));
    scbLogLevel = new SteppedComboBox();
    scbLogLevel.addItem(Messages.getString("ParameterView.47"));
    scbLogLevel.addItem(Messages.getString("ParameterView.48"));
    scbLogLevel.addItem(Messages.getString("ParameterView.49"));
    scbLogLevel.addItem(Messages.getString("ParameterView.50"));
    scbLogLevel.addItem(Messages.getString("ParameterView.51"));
    scbLogLevel.setToolTipText(Messages.getString("ParameterView.52"));
    JLabel jlMPlayerPath = new JLabel(Messages.getString("ParameterView.242"));
    jlMPlayerPath.setToolTipText(Messages.getString("ParameterView.243"));
    jtfMPlayerPath = new JTextField();
    jtfMPlayerPath.setToolTipText(Messages.getString("ParameterView.243"));
    JLabel jlMPlayerArgs = new JLabel(Messages.getString("ParameterView.205"));
    jlMPlayerArgs.setToolTipText(Messages.getString("ParameterView.206"));
    jtfMPlayerArgs = new JTextField();
    jtfMPlayerArgs.setToolTipText(Messages.getString("ParameterView.206"));
    JLabel jlEnvVariables = new JLabel(Messages.getString("ParameterView.219"));
    jlEnvVariables.setToolTipText(Messages.getString("ParameterView.220"));
    jtfEnvVariables = new JTextField();
    jtfEnvVariables.setToolTipText(Messages.getString("ParameterView.220"));
    JLabel jlJajukWorkspace = new JLabel(Messages.getString("ParameterView.207"));
    jlJajukWorkspace.setToolTipText(Messages.getString("ParameterView.208"));
    // Directory selection
    psJajukWorkspace = new PathSelector(SessionService.getWorkspace());
    psJajukWorkspace.setToolTipText(Messages.getString("ParameterView.208"));
    // If user provided a forced workspace, he can't change it again here
    if (SessionService.isForcedWorkspace()) {
      jlJajukWorkspace.setEnabled(false);
      psJajukWorkspace.setEnabled(false);
    }
    jcbCheckUpdates = new JCheckBox(Messages.getString("ParameterView.234"));
    jcbCheckUpdates.setToolTipText(Messages.getString("ParameterView.234"));
    jcbForceFileDate = new JCheckBox(Messages.getString("ParameterView.244"));
    jcbForceFileDate.setToolTipText(Messages.getString("ParameterView.245"));
    JLabel jlExplorer = new JLabel(Messages.getString("ParameterView.269"));
    jlExplorer.setToolTipText(Messages.getString("ParameterView.270"));
    jtfExplorerPath = new JTextField();
    jtfExplorerPath.setToolTipText(Messages.getString("ParameterView.270"));
    JPanel jpAdvanced = new JPanel(new MigLayout("insets 10,gapy 15, gapx 10", "[][grow][fill]"));
    jpAdvanced.add(jcbBackup);
    jpAdvanced.add(backupSize, "wrap,grow");
    jpAdvanced.add(jlCollectionEncoding);
    jpAdvanced.add(jcbCollectionEncoding, "wrap,grow");
    jpAdvanced.add(jlLogLevel);
    jpAdvanced.add(scbLogLevel, "wrap,grow");
    jpAdvanced.add(jlMPlayerPath);
    jpAdvanced.add(jtfMPlayerPath, "wrap,grow");
    jpAdvanced.add(jlMPlayerArgs);
    jpAdvanced.add(jtfMPlayerArgs, "wrap,grow");
    jpAdvanced.add(jlEnvVariables);
    jpAdvanced.add(jtfEnvVariables, "wrap,grow");
    jpAdvanced.add(jlJajukWorkspace);
    jpAdvanced.add(psJajukWorkspace, "wrap,grow");
    jpAdvanced.add(jlExplorer);
    jpAdvanced.add(jtfExplorerPath, "grow,wrap");
    jpAdvanced.add(jcbRegexp, "wrap");
    jpAdvanced.add(jcbCheckUpdates, "wrap");
    jpAdvanced.add(jcbForceFileDate, "wrap");
    return jpAdvanced;
  }

  /**
   * Inits the ui network.
   *
   *
   * @return the jpanel
   */
  private JPanel initUINetwork() {
    bgProxy = new ButtonGroup();
    jcbProxyNone = new JRadioButton(Messages.getString("ParameterView.236"));
    jcbProxyNone.setToolTipText(Messages.getString("ParameterView.236"));
    jcbProxyNone.addActionListener(updateHelper);
    jcbNoneInternetAccess = new JCheckBox(Messages.getString("ParameterView.264"));
    jcbNoneInternetAccess.setToolTipText(Messages.getString("ParameterView.265"));
    jcbProxyHttp = new JRadioButton(Messages.getString("ParameterView.237"));
    jcbProxyHttp.setToolTipText(Messages.getString("ParameterView.237"));
    jcbProxyHttp.addActionListener(updateHelper);
    jcbProxySocks = new JRadioButton(Messages.getString("ParameterView.238"));
    jcbProxySocks.setToolTipText(Messages.getString("ParameterView.238"));
    jcbProxySocks.addActionListener(updateHelper);
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
        final JTextField tf = (JTextField) input;//NOSONAR
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
    JPanel jpProxy = new JPanel(new MigLayout("insets 10,gapy 15, gapx 10", "[][grow,100:300:300]"));
    jpProxy.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.268")));
    jpProxy.add(jcbProxyNone, "wrap");
    jpProxy.add(jcbProxyHttp, "wrap");
    jpProxy.add(jcbProxySocks, "wrap");
    jpProxy.add(jlProxyHostname);
    jpProxy.add(jtfProxyHostname, "wrap,grow");
    jpProxy.add(jlProxyPort);
    jpProxy.add(jtfProxyPort, "wrap,grow");
    jpProxy.add(jlProxyLogin);
    jpProxy.add(jtfProxyLogin, "wrap,grow");
    jpProxy.add(jlProxyPwd);
    jpProxy.add(jtfProxyPwd, "wrap,grow");
    JPanel jpNetwork = new JPanel(new MigLayout("insets 10,gapy 15, gapx 10", "[grow]"));
    jpNetwork.add(jcbNoneInternetAccess, "wrap");
    jpNetwork.add(jlConnectionTO, "split 2");
    jpNetwork.add(connectionTO, "wrap,grow,width 200!");
    jpNetwork.add(jpProxy, "span");
    return jpNetwork;
  }

  /**
   * Inits the ui last fm.
   *
   *
   * @return the jpanel
   */
  private JPanel initUILastFM() {
    jcbAudioScrobbler = new JCheckBox(Messages.getString("ParameterView.199"));
    jcbAudioScrobbler.setToolTipText(Messages.getString("ParameterView.200"));
    jcbAudioScrobbler.addActionListener(updateHelper);
    jlASUser = new JLabel(Messages.getString("ParameterView.201"));
    jtfASUser = new JTextField();
    jtfASUser.setToolTipText(Messages.getString("ParameterView.202"));
    jlASPassword = new JLabel(Messages.getString("ParameterView.203"));
    jpfASPassword = new JPasswordField();
    jpfASPassword.setToolTipText(Messages.getString("ParameterView.204"));
    jcbEnableLastFMInformation = new JCheckBox(Messages.getString("ParameterView.240"));
    jcbEnableLastFMInformation.setToolTipText(Messages.getString("ParameterView.241"));
    // Add items
    JPanel jpLastFM = new JPanel(new MigLayout("insets 10,gapy 15,gapx 10", "[grow]"));
    jpLastFM.add(jcbEnableLastFMInformation, "wrap");
    jpLastFM.add(jcbAudioScrobbler, "wrap");
    jpLastFM.add(jlASUser);
    jpLastFM.add(jtfASUser, "wrap,grow,width 100:300:300");
    jpLastFM.add(jlASPassword);
    jpLastFM.add(jpfASPassword, "wrap,grow,width 100:300:300");
    return jpLastFM;
  }

  /**
   * Inits the ui covers.
   *
   *
   * @return the jpanel
   */
  private JPanel initUICovers() {
    jcbAutoCover = new JCheckBox(Messages.getString("ParameterView.148"));
    jcbAutoCover.setToolTipText(Messages.getString("ParameterView.149"));
    jcbAutoCover.addActionListener(updateHelper);
    jcbShuffleCover = new JCheckBox(Messages.getString("ParameterView.166"));
    jcbShuffleCover.setToolTipText(Messages.getString("ParameterView.167"));
    jcbShuffleCover.addActionListener(updateHelper);
    jlCoverSize = new JLabel(Messages.getString("ParameterView.150"));
    jlCoverSize.setToolTipText(Messages.getString("ParameterView.151"));
    jcbCoverSize = new JComboBox();
    jcbCoverSize.setToolTipText(Messages.getString("ParameterView.151"));
    jcbCoverSize.addItem(Messages.getString("ParameterView.211"));
    jcbCoverSize.addItem(Messages.getString("ParameterView.212"));
    jcbCoverSize.addItem(Messages.getString("ParameterView.213"));
    jcbCoverSize.addItem(Messages.getString("ParameterView.214"));
    jcbCoverSize.addItem(Messages.getString("ParameterView.215"));
    jcb3dCover = new JCheckBox(Messages.getString("ParameterView.273"));
    jcb3dCover.setToolTipText(Messages.getString("ParameterView.274"));
    jcb3dCoverFS = new JCheckBox(Messages.getString("ParameterView.283"));
    jcb3dCoverFS.setToolTipText(Messages.getString("ParameterView.284"));
    jlDefaultCoverSearchPattern = new JLabel(Messages.getString("ParameterView.256"));
    jlDefaultCoverSearchPattern.setToolTipText(Messages.getString("ParameterView.257"));
    jtfDefaultCoverSearchPattern = new JTextField();
    jtfDefaultCoverSearchPattern.setToolTipText(Messages.getString("ParameterView.257"));
    jcbSaveExplorerFriendly = new JCheckBox(Messages.getString("ParameterView.260"));
    jcbSaveExplorerFriendly.setToolTipText(Messages.getString("ParameterView.261"));
    jcbSaveExplorerFriendly.addActionListener(updateHelper);
    // Add items
    JPanel jpCovers = new JPanel(new MigLayout("insets 10,gapy 15,gapx 10", "[40%][40%]"));
    jpCovers.add(jcbShuffleCover, "wrap");
    jpCovers.add(jcbAutoCover, "wrap");
    jpCovers.add(jcb3dCover, "split 2");
    jpCovers.add(jcb3dCoverFS, "wrap");
    jpCovers.add(jcbSaveExplorerFriendly, "wrap");
    jpCovers.add(jlCoverSize);
    jpCovers.add(jcbCoverSize, "wrap,grow");
    jpCovers.add(jlDefaultCoverSearchPattern);
    jpCovers.add(jtfDefaultCoverSearchPattern, "wrap,grow");
    return jpCovers;
  }

  /**
   * Inits the GUI tab.
   *
   * @return the jpanel
   */
  private JPanel initUIGUI() {
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
      @Override
      public void stateChanged(ChangeEvent e) {
        jsCatalogPages.setToolTipText(Integer.toString(jsCatalogPages.getValue()));
      }
    });
    jcbShowPopups = new JCheckBox(Messages.getString("ParameterView.228"));
    jcbShowPopups.setToolTipText(Messages.getString("ParameterView.292"));
    // Splashscreen
    jcbSplashscreen = new JCheckBox(Messages.getString("ParameterView.290"));
    jcbSplashscreen.setToolTipText(Messages.getString("ParameterView.291"));
    jcbShowSystray = new JCheckBox(Messages.getString("ParameterView.271"));
    // Disable this option if the tray is not supported by the platform
    jcbShowSystray.setEnabled(SystemTray.isSupported());
    // Disable minimize to systray option if unchecked
    jcbShowSystray.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        jcbMinimizeToTray.setEnabled(jcbShowSystray.isSelected());
        jcbClickTrayAlwaysDisplayWindow.setEnabled(jcbShowSystray.isSelected());
        if (!jcbShowSystray.isSelected()) {
          jcbMinimizeToTray.setSelected(false);
          jcbClickTrayAlwaysDisplayWindow.setSelected(false);
        }
      }
    });
    jcbShowSystray.setToolTipText(Messages.getString("ParameterView.272"));
    jcbMinimizeToTray = new JCheckBox(Messages.getString("ParameterView.281"));
    jcbMinimizeToTray.setToolTipText(Messages.getString("ParameterView.282"));
    jcbClickTrayAlwaysDisplayWindow = new JCheckBox(Messages.getString("ParameterView.303"));
    jcbClickTrayAlwaysDisplayWindow.setToolTipText(Messages.getString("ParameterView.304"));
    JLabel jlPerspectiveSize = new JLabel(Messages.getString("ParameterView.246"));
    jsPerspectiveSize = new JSlider(16, 45, Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE));
    jsPerspectiveSize.setSnapToTicks(true);
    jsPerspectiveSize.setMajorTickSpacing(8);
    jsPerspectiveSize.setMinorTickSpacing(1);
    jsPerspectiveSize.setPaintTicks(true);
    jsPerspectiveSize.setPaintLabels(true);
    jsPerspectiveSize.setToolTipText(Messages.getString("ParameterView.246"));
    jbCatalogRefresh = new JajukButton(Messages.getString("CatalogView.19"),
        IconLoader.getIcon(JajukIcons.REFRESH));
    jbCatalogRefresh.setToolTipText(Messages.getString("CatalogView.3"));
    jbCatalogRefresh.addActionListener(updateHelper);
    final JXCollapsiblePane catalogView = new JXCollapsiblePane();
    catalogView.setLayout(new VerticalLayout(10));
    catalogView.setCollapsed(true);
    final ToggleLink toggleCatalog = new ToggleLink(Messages.getString("ParameterView.229"),
        catalogView);
    final JPanel jpCatalogSize = new JPanel();
    jpCatalogSize.setLayout(new HorizontalLayout());
    jpCatalogSize.add(jlCatalogPages);
    jpCatalogSize.add(jsCatalogPages);
    catalogView.add(jpCatalogSize);
    catalogView.add(jbCatalogRefresh);
    //Title view
    jcbTitleAnimation = new JCheckBox(Messages.getString("ParameterView.288"));
    jcbTitleAnimation.setToolTipText(Messages.getString("ParameterView.288"));
    final JXCollapsiblePane titleView = new JXCollapsiblePane();
    titleView.add(jcbTitleAnimation);
    titleView.setCollapsed(true);
    final ToggleLink toggleTitle = new ToggleLink(Messages.getString("ParameterView.289"),
        titleView);
    // Font selector
    jlFonts = new JLabel(Messages.getString("ParameterView.223"));
    jsFonts = new JSlider(8, 16, Conf.getInt(Const.CONF_FONTS_SIZE));
    jsFonts.setSnapToTicks(true);
    jsFonts.setMajorTickSpacing(1);
    jsFonts.setMinorTickSpacing(1);
    jsFonts.setPaintTicks(true);
    jsFonts.setPaintLabels(true);
    jsFonts.setToolTipText(Messages.getString("ParameterView.224"));
    // Notification type
    jlNotificationType = new JLabel(Messages.getString("ParameterView.275"));
    jlNotificationType.setToolTipText(Messages.getString("ParameterView.276"));
    jcbNotificationType = new JComboBox();
    jcbNotificationType.setToolTipText(Messages.getString("ParameterView.276"));
    for (NotificatorTypes type : NotificatorTypes.values()) {
      String notificatorType = Messages.getString(NOTIFICATOR_PREFIX + type);
      jcbNotificationType.addItem(notificatorType);
    }
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
    // Refresh full GUI at each LAF change as a preview
    scbLAF.addActionListener(updateHelper);
    // Add items
    JPanel jpUI = new JPanel(new MigLayout("insets 10,gapx 10,gapy 15"));
    jpUI.add(jcbShowPopups, "wrap");
    jpUI.add(jcbSplashscreen, "wrap");
    jpUI.add(jcbShowSystray, "split 3");
    jpUI.add(jcbMinimizeToTray);
    jpUI.add(jcbClickTrayAlwaysDisplayWindow, "wrap");
    jpUI.add(jlFonts);
    jpUI.add(jsFonts, "wrap,grow");
    jpUI.add(jlNotificationType);
    jpUI.add(jcbNotificationType, "wrap,grow");
    jpUI.add(jlLAF);
    jpUI.add(scbLAF, "wrap,grow");
    jpUI.add(jlPerspectiveSize);
    jpUI.add(jsPerspectiveSize, "wrap,grow");
    jpUI.add(toggleCatalog, "wrap,grow");
    jpUI.add(catalogView, "wrap,grow,span");
    jpUI.add(toggleTitle, "wrap,grow");
    jpUI.add(titleView, "wrap,grow,span");
    return jpUI;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.IView#display()
   */
  @Override
  public void initUI() {
    JPanel jpHistory = initUIHistory();
    JPanel jpStartup = initUIStartup();
    JPanel jpConfirmations = initUIConfirmations();
    JPanel jpModes = initUIModes();
    JPanel jpSound = initUISound();
    JPanel jpOptions = initUIOptions();
    JPanel jpPatterns = initUIPatterns();
    JPanel jpAdvanced = initUIAdvanced();
    JPanel jpNetwork = initUINetwork();
    JPanel jpLastFM = initUILastFM();
    JPanel jpCovers = initUICovers();
    JPanel jpUI = initUIGUI();
    JPanel jpWebradios = initWebradios();
    // --OK/cancel panel
    jbOK = new JButton(Messages.getString("ParameterView.85"), IconLoader.getIcon(JajukIcons.OK));
    jbOK.addActionListener(updateHelper);
    jbDefault = new JButton(Messages.getString("ParameterView.86"),
        IconLoader.getIcon(JajukIcons.DEFAULTS_BIG));
    jbDefault.addActionListener(updateHelper);
    // --Global layout
    // add main panels
    jtpMain = new JTabbedPane(SwingConstants.TOP);
    // ScrollPane without border
    final class JajukJScrollPane extends JScrollPane {
      private static final long serialVersionUID = 4564343623724771988L;

      private JajukJScrollPane(final Component view) {
        super(view);
        setBorder(null);
      }
    }
    jtpMain.addTab(Messages.getString("ParameterView.33"), new JajukJScrollPane(jpOptions));
    jtpMain.addTab(Messages.getString("ParameterView.226"), new JajukJScrollPane(jpModes));
    jtpMain.addTab(Messages.getString("ParameterView.287"), new JajukJScrollPane(jpSound));
    jtpMain.addTab(Messages.getString("ParameterView.225"), new JajukJScrollPane(jpUI));
    jtpMain.addTab(Messages.getString("ParameterView.19"), new JajukJScrollPane(jpStartup));
    jtpMain.addTab(Messages.getString("ParameterView.98"), new JajukJScrollPane(jpPatterns));
    jtpMain.addTab(Messages.getString("ParameterView.8"), new JajukJScrollPane(jpHistory));
    jtpMain.addTab(Messages.getString("ParameterView.235"), new JajukJScrollPane(jpLastFM));
    jtpMain.addTab(Messages.getString("WebRadioView.0"), new JajukJScrollPane(jpWebradios));
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
    jtpMain.addChangeListener(updateHelper);
    setLayout(new MigLayout("insets 10,gapx 10", "[grow]", "[grow][]"));
    add(jtpMain, "wrap,span,grow");
    add(jbOK, "split 2,right,sg group1");
    add(jbDefault, "sg group1");
    // update widgets state
    updateHelper.updateGUIFromConf();
    ObservationManager.register(this);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  @Override
  public void update(final JajukEvent event) {
    final JajukEvents subject = event.getSubject();
    if (JajukEvents.PARAMETERS_CHANGE.equals(subject)) {
      // Ignore this event is thrown by this view itself (to avoid loosing
      // already set options)
      if ((event.getDetails() != null) && (event.getDetails().get(Const.DETAIL_ORIGIN) != null)
          && event.getDetails().get(Const.DETAIL_ORIGIN).equals(this)) {
        return;
      }
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          updateHelper.updateGUIFromConf();
        }
      });
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.Container#removeAll()
   */
  @Override
  public void removeAll() {
    // We have to override removeAll() to work around a memory leak related to SearchBox..
    // make sure that the search box stops waking to free up the reference to the Timer
    sbSearch.close();
    super.removeAll();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.views.ViewAdapter#cleanup()
   */
  @Override
  public void cleanup() {
    // make sure that the search box stops to free up the reference to the Timer
    sbSearch.close();
    super.cleanup();
  }
}
