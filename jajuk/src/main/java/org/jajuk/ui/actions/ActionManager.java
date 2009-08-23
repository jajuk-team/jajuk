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
 *  $$Revision$$
 */
package org.jajuk.ui.actions;

import static org.jajuk.ui.actions.JajukActions.ALARM_CLOCK;
import static org.jajuk.ui.actions.JajukActions.ALL_VIEW_RESTORE_DEFAULTS;
import static org.jajuk.ui.actions.JajukActions.BAN;
import static org.jajuk.ui.actions.JajukActions.BAN_SELECTION;
import static org.jajuk.ui.actions.JajukActions.BEST_OF;
import static org.jajuk.ui.actions.JajukActions.BOOKMARK_SELECTION;
import static org.jajuk.ui.actions.JajukActions.CDDB_SELECTION;
import static org.jajuk.ui.actions.JajukActions.CHECK_FOR_UPDATES;
import static org.jajuk.ui.actions.JajukActions.CONFIGURE_AMBIENCES;
import static org.jajuk.ui.actions.JajukActions.CONFIGURE_DJS;
import static org.jajuk.ui.actions.JajukActions.CONFIGURE_WEBRADIOS;
import static org.jajuk.ui.actions.JajukActions.CONTINUE_MODE;
import static org.jajuk.ui.actions.JajukActions.COPY;
import static org.jajuk.ui.actions.JajukActions.COPY_TO_CLIPBOARD;
import static org.jajuk.ui.actions.JajukActions.CREATE_REPORT;
import static org.jajuk.ui.actions.JajukActions.CUSTOM_PROPERTIES_ADD;
import static org.jajuk.ui.actions.JajukActions.CUSTOM_PROPERTIES_REMOVE;
import static org.jajuk.ui.actions.JajukActions.CUT;
import static org.jajuk.ui.actions.JajukActions.DECREASE_VOLUME;
import static org.jajuk.ui.actions.JajukActions.DELETE;
import static org.jajuk.ui.actions.JajukActions.DJ;
import static org.jajuk.ui.actions.JajukActions.EXIT;
import static org.jajuk.ui.actions.JajukActions.FIND_DUPLICATE_FILES;
import static org.jajuk.ui.actions.JajukActions.FINISH_ALBUM;
import static org.jajuk.ui.actions.JajukActions.FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukActions.FULLSCREEN_JAJUK;
import static org.jajuk.ui.actions.JajukActions.HELP_REQUIRED;
import static org.jajuk.ui.actions.JajukActions.INCREASE_VOLUME;
import static org.jajuk.ui.actions.JajukActions.INC_RATE;
import static org.jajuk.ui.actions.JajukActions.INTRO_MODE;
import static org.jajuk.ui.actions.JajukActions.LAUNCH_IN_BROWSER;
import static org.jajuk.ui.actions.JajukActions.MUTE_STATE;
import static org.jajuk.ui.actions.JajukActions.NEW_FOLDER;
import static org.jajuk.ui.actions.JajukActions.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.NOVELTIES;
import static org.jajuk.ui.actions.JajukActions.OPTIONS;
import static org.jajuk.ui.actions.JajukActions.PASTE;
import static org.jajuk.ui.actions.JajukActions.PAUSE_RESUME_TRACK;
import static org.jajuk.ui.actions.JajukActions.PLAY_ALBUM_SELECTION;
import static org.jajuk.ui.actions.JajukActions.PLAY_AUTHOR_SELECTION;
import static org.jajuk.ui.actions.JajukActions.PLAY_DIRECTORY_SELECTION;
import static org.jajuk.ui.actions.JajukActions.PLAY_REPEAT_SELECTION;
import static org.jajuk.ui.actions.JajukActions.PLAY_SELECTION;
import static org.jajuk.ui.actions.JajukActions.PLAY_SHUFFLE_SELECTION;
import static org.jajuk.ui.actions.JajukActions.PREFERENCE_ADORE;
import static org.jajuk.ui.actions.JajukActions.PREFERENCE_AVERAGE;
import static org.jajuk.ui.actions.JajukActions.PREFERENCE_HATE;
import static org.jajuk.ui.actions.JajukActions.PREFERENCE_LIKE;
import static org.jajuk.ui.actions.JajukActions.PREFERENCE_LOVE;
import static org.jajuk.ui.actions.JajukActions.PREFERENCE_POOR;
import static org.jajuk.ui.actions.JajukActions.PREFERENCE_UNSET;
import static org.jajuk.ui.actions.JajukActions.PREPARE_PARTY;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.PUSH_FRONT_SELECTION;
import static org.jajuk.ui.actions.JajukActions.PUSH_SELECTION;
import static org.jajuk.ui.actions.JajukActions.QUALITY;
import static org.jajuk.ui.actions.JajukActions.REFRESH;
import static org.jajuk.ui.actions.JajukActions.RENAME;
import static org.jajuk.ui.actions.JajukActions.REPEAT_MODE;
import static org.jajuk.ui.actions.JajukActions.REPLAY_ALBUM;
import static org.jajuk.ui.actions.JajukActions.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukActions.SAVE_AS;
import static org.jajuk.ui.actions.JajukActions.SHOW_ABOUT;
import static org.jajuk.ui.actions.JajukActions.SHOW_ALBUM_DETAILS;
import static org.jajuk.ui.actions.JajukActions.SHOW_PROPERTIES;
import static org.jajuk.ui.actions.JajukActions.SHOW_TRACES;
import static org.jajuk.ui.actions.JajukActions.SHUFFLE_GLOBAL;
import static org.jajuk.ui.actions.JajukActions.SHUFFLE_MODE;
import static org.jajuk.ui.actions.JajukActions.SIMPLE_DEVICE_WIZARD;
import static org.jajuk.ui.actions.JajukActions.SLIM_JAJUK;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;
import static org.jajuk.ui.actions.JajukActions.TIP_OF_THE_DAY;
import static org.jajuk.ui.actions.JajukActions.UNMOUNTED;
import static org.jajuk.ui.actions.JajukActions.UN_BAN_SELECTION;
import static org.jajuk.ui.actions.JajukActions.VIEW_RESTORE_DEFAULTS;
import static org.jajuk.ui.actions.JajukActions.WEB_RADIO;

import java.awt.Desktop;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Helper class used to create, store and lookup actions.
 * <p>
 * Singleton
 * </p>
 */
public final class ActionManager {

  private static final EnumMap<JajukActions, JajukAction> MAP = new EnumMap<JajukActions, JajukAction>(
      JajukActions.class);

  private static final List<KeyStroke> STROKE_LIST = new ArrayList<KeyStroke>();

  /** Self instance */
  private static ActionManager self = null;

  /**
   * 
   * @return singleton
   */
  public static ActionManager getInstance() {
    if (self == null) {
      self = new ActionManager();
    }
    return self;
  }

  private ActionManager() {
    // Private constructor to disallow instantiation.
    // CommandJPanel: Mode Panel
    installAction(REPEAT_MODE, new RepeatModeAction(), true);
    installAction(JajukActions.REPEAT_ALL_MODE, new RepeatAllModeAction(), true);
    installAction(SHUFFLE_MODE, new ShuffleModeAction(), true);
    installAction(CONTINUE_MODE, new ContinueModeAction(), false);
    installAction(INTRO_MODE, new IntroModeAction(), false);
    installAction(JajukActions.KARAOKE_MODE, new KaraokeModeAction(), false);

    // CommandJPanel: Special Functions Panel
    installAction(SHUFFLE_GLOBAL, new GlobalRandomAction(), false);
    installAction(BEST_OF, new BestOfAction(), false);
    installAction(DJ, new DJAction(), false);
    installAction(NOVELTIES, new NoveltiesAction(), false);
    installAction(FINISH_ALBUM, new FinishAlbumAction(), false);
    installAction(WEB_RADIO, new WebRadioAction(), false);

    // CommandJPanel: Play Panel
    installAction(PREVIOUS_TRACK, new PreviousTrackAction(), true);
    installAction(REPLAY_ALBUM, new ReplayAlbumAction(), true);
    installAction(NEXT_TRACK, new NextTrackAction(), true);
    installAction(PREVIOUS_ALBUM, new PreviousAlbumAction(), true);
    installAction(NEXT_ALBUM, new NextAlbumAction(), true);
    installAction(REWIND_TRACK, new RewindTrackAction(), true);
    installAction(PAUSE_RESUME_TRACK, new PlayPauseAction(), true);
    installAction(STOP_TRACK, new StopTrackAction(), true);
    installAction(FORWARD_TRACK, new ForwardTrackAction(), true);
    installAction(INC_RATE, new ChangeTrackPreferenceAction(), true);

    // CommandJPanel: Volume control
    installAction(DECREASE_VOLUME, new DecreaseVolumeAction(), true);
    installAction(INCREASE_VOLUME, new IncreaseVolumeAction(), true);
    installAction(MUTE_STATE, new MuteAction(), true);

    // JajukJMenuBar: File Menu
    installAction(EXIT, new ExitAction(), false);

    // JajukJMenuBar: views
    installAction(VIEW_RESTORE_DEFAULTS, new RestoreViewsAction(), false);
    installAction(ALL_VIEW_RESTORE_DEFAULTS, new RestoreAllViewsAction(), false);

    // JajukJMenuBar: attributes
    installAction(CUSTOM_PROPERTIES_ADD, new NewPropertyAction(), false);
    installAction(CUSTOM_PROPERTIES_REMOVE, new RemovePropertyAction(), false);

    // JajukJMenuBar: configuration
    installAction(CONFIGURE_DJS, new DJConfigurationAction(), false);
    installAction(CONFIGURE_WEBRADIOS, new WebRadioConfigurationAction(), false);
    installAction(CONFIGURE_AMBIENCES, new AmbienceConfigurationAction(), false);
    installAction(SIMPLE_DEVICE_WIZARD, new SimpleDeviceWizardAction(), false);
    installAction(OPTIONS, new ConfigurationRequiredAction(), false);
    installAction(UNMOUNTED, new HideShowMountedDevicesAction(), false);

    // JajukJMenuBar: Help Menu
    installAction(HELP_REQUIRED, new HelpRequiredAction(), false);
    installAction(SHOW_ABOUT, new ShowAboutAction(), false);
    // Install this action only if Desktop class is supported, it is used to
    // open default mail client
    if (Desktop.isDesktopSupported()) {
      installAction(QUALITY, new QualityAction(), false);
    }
    installAction(SHOW_TRACES, new DebugLogAction(), false);
    installAction(TIP_OF_THE_DAY, new TipOfTheDayAction(), false);
    installAction(CHECK_FOR_UPDATES, new CheckForUpdateAction(), false);

    // Export
    installAction(CREATE_REPORT, new ReportAction(), false);

    // File Actions
    installAction(CUT, new CutAction(), false);
    installAction(COPY, new CopyAction(), false);
    installAction(DELETE, new DeleteSelectionAction(), false);
    installAction(PASTE, new PasteAction(), false);
    installAction(RENAME, new RenameAction(), false);
    installAction(NEW_FOLDER, new NewFolderAction(), false);

    // MISC
    installAction(FIND_DUPLICATE_FILES, new FindDuplicateTracksAction(), false);
    installAction(COPY_TO_CLIPBOARD, new CopyClipboardAction(), false);
    installAction(JajukActions.OPEN_EXPLORER, new OpenExplorerAction(), false);
    installAction(REFRESH, new RefreshAction(), false);
    installAction(ALARM_CLOCK, new AlarmClockAction(), false);
    installAction(SHOW_ALBUM_DETAILS, new ShowAlbumDetailsAction(), false);
    installAction(SLIM_JAJUK, new SlimbarAction(), false);
    // Install full screen actions only if supported
    installAction(FULLSCREEN_JAJUK, new FullscreenAction(), false);
    installAction(PREPARE_PARTY, new PreparePartyAction(), false);

    // Selection actions
    installAction(SHOW_PROPERTIES, new ShowPropertiesAction(), true);
    installAction(PLAY_SELECTION, new PlaySelectionAction(), false);
    installAction(PLAY_SHUFFLE_SELECTION, new PlayShuffleSelectionAction(), false);
    installAction(PLAY_REPEAT_SELECTION, new PlayRepeatSelectionAction(), false);
    installAction(PUSH_FRONT_SELECTION, new PushFrontSelectionAction(), false);
    installAction(PUSH_SELECTION, new PushSelectionAction(), false);
    installAction(BOOKMARK_SELECTION, new BookmarkSelectionAction(), false);
    installAction(PLAY_ALBUM_SELECTION, new PlayAlbumSelectionAction(), false);
    installAction(PLAY_AUTHOR_SELECTION, new PlayAuthorSelectionAction(), false);
    installAction(PLAY_DIRECTORY_SELECTION, new PlayDirectorySelectionAction(), false);
    installAction(CDDB_SELECTION, new CDDBSelectionAction(), false);
    installAction(SAVE_AS, new SaveAsAction(), false);

    // Preferences
    installAction(BAN, new BanCurrentAction(), false);
    installAction(BAN_SELECTION, new BanSelectionAction(), false);
    installAction(UN_BAN_SELECTION, new UnBanSelectionAction(), false);
    installAction(PREFERENCE_ADORE, new AdoreSelectionAction(), false);
    installAction(PREFERENCE_LOVE, new LoveSelectionAction(), false);
    installAction(PREFERENCE_LIKE, new LikeSelectionAction(), false);
    installAction(PREFERENCE_AVERAGE, new AverageSelectionAction(), false);
    installAction(PREFERENCE_POOR, new PoorSelectionAction(), false);
    installAction(PREFERENCE_HATE, new HateSelectionAction(), false);
    installAction(PREFERENCE_UNSET, new UnsetPreferenceSelectionAction(), false);
    // Install this action only if Desktop class is supported, it is used to
    // open default web browser
    if (Desktop.isDesktopSupported()) {
      installAction(LAUNCH_IN_BROWSER, new LaunchInBrowserAction(), false);
    }

    // Uninstall Look and feel keystrokes if required
    uninstallStrokes();

  }

  /**
   * @param action
   *          The <code>JajukActions</code> to get.
   * @return The <code>JajukAction</code> implementation linked to the
   *         <code>JajukActions</code>.
   */
  public static JajukAction getAction(JajukActions action) {
    JajukAction actionBase = MAP.get(action);
    if (actionBase == null) {
      throw new ExceptionInInitializerError("No action mapping found for " + action);
    }
    return actionBase;
  }

  /**
   * Installs a new action in the action manager. If <code>removeFromLAF</code>
   * is <code>true</code>, then the keystroke attached to the action will be
   * stored in list. To remove these keystrokes from the <code>InputMap</code>s
   * of the different components, call {@link #uninstallStrokes()}.
   * 
   * @param name
   *          The name for the action.
   * @param action
   *          The action implementation.
   * @param removeFromLAF
   *          Remove default keystrokes from look and feel.
   */
  private static void installAction(JajukActions name, JajukAction action, boolean removeFromLAF) {
    MAP.put(name, action);

    if (removeFromLAF) {
      KeyStroke stroke = (KeyStroke) action.getValue(JajukAction.ACCELERATOR_KEY);
      if (stroke != null) {
        STROKE_LIST.add(stroke);
      }
    }
  }

  /**
   * Uninstall default keystrokes from different JComponents to allow more
   * globally configured JaJuk keystrokes.
   */
  public static void uninstallStrokes() {
    UIDefaults defaults = UIManager.getDefaults();
    for (Object uidefault : defaults.keySet()) {
      if (uidefault instanceof InputMap) {
        InputMap map = (InputMap) uidefault;
        for (KeyStroke stroke : STROKE_LIST) {
          map.remove(stroke);
        }
      }
    }
  }

  /**
   * Enable or disable the action
   * 
   * @param the
   *          action
   * @param enable
   */
  public void enable(JajukAction action, boolean enable) {
    action.enable(enable);
  }
}