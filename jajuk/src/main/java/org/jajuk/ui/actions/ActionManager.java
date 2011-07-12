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
 *  $Revision$
 */
package org.jajuk.ui.actions;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.jajuk.util.UtilSystem;

/**
 * Helper class used to create, store and lookup actions.
 * <p>
 * Singleton
 * </p>
 */
public final class ActionManager {

  /** The Constant MAP.  DOCUMENT_ME */
  private static final EnumMap<JajukActions, JajukAction> MAP = new EnumMap<JajukActions, JajukAction>(
      JajukActions.class);

  /** The Constant STROKE_LIST.  DOCUMENT_ME */
  private static final List<KeyStroke> STROKE_LIST = new ArrayList<KeyStroke>();

  /** Self instance. */
  private static ActionManager self = new ActionManager();

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static ActionManager getInstance() {
    return self;
  }

  /**
   * Instantiates a new action manager.
   */
  private ActionManager() {
    // Private constructor to disallow instantiation.
    // CommandJPanel: Mode Panel
    installAction(JajukActions.REPEAT_MODE, new RepeatModeAction(), true);
    installAction(JajukActions.REPEAT_ALL_MODE, new RepeatAllModeAction(), true);
    installAction(JajukActions.SHUFFLE_MODE, new ShuffleModeAction(), true);
    installAction(JajukActions.CONTINUE_MODE, new ContinueModeAction(), false);
    installAction(JajukActions.INTRO_MODE, new IntroModeAction(), false);
    installAction(JajukActions.KARAOKE_MODE, new KaraokeModeAction(), false);

    // CommandJPanel: Special Functions Panel
    installAction(JajukActions.SHUFFLE_GLOBAL, new GlobalRandomAction(), false);
    installAction(JajukActions.BEST_OF, new BestOfAction(), false);
    installAction(JajukActions.DJ, new DJAction(), false);
    installAction(JajukActions.NOVELTIES, new NoveltiesAction(), false);
    installAction(JajukActions.FINISH_ALBUM, new FinishAlbumAction(), false);
    installAction(JajukActions.WEB_RADIO, new WebRadioAction(), false);

    // CommandJPanel: Play Panel
    installAction(JajukActions.PREVIOUS_TRACK, new PreviousTrackAction(), true);
    installAction(JajukActions.REPLAY_ALBUM, new ReplayAlbumAction(), true);
    installAction(JajukActions.NEXT_TRACK, new NextTrackAction(), true);
    installAction(JajukActions.PREVIOUS_ALBUM, new PreviousAlbumAction(), true);
    installAction(JajukActions.NEXT_ALBUM, new NextAlbumAction(), true);
    installAction(JajukActions.REWIND_TRACK, new RewindTrackAction(), true);
    installAction(JajukActions.PAUSE_RESUME_TRACK, new PlayPauseAction(), true);
    installAction(JajukActions.STOP_TRACK, new StopTrackAction(), true);
    installAction(JajukActions.FORWARD_TRACK, new ForwardTrackAction(), true);
    installAction(JajukActions.INC_RATE, new ChangeTrackPreferenceAction(), true);

    // CommandJPanel: Volume control
    installAction(JajukActions.DECREASE_VOLUME, new DecreaseVolumeAction(), true);
    installAction(JajukActions.INCREASE_VOLUME, new IncreaseVolumeAction(), true);
    installAction(JajukActions.MUTE_STATE, new MuteAction(), true);

    // JajukJMenuBar: File Menu
    installAction(JajukActions.EXIT, new ExitAction(), false);

    // JajukJMenuBar: views
    installAction(JajukActions.VIEW_RESTORE_DEFAULTS, new RestoreViewsAction(), false);
    installAction(JajukActions.ALL_VIEW_RESTORE_DEFAULTS, new RestoreAllViewsAction(), false);

    // JajukJMenuBar: attributes
    installAction(JajukActions.CUSTOM_PROPERTIES_ADD, new NewPropertyAction(), false);
    installAction(JajukActions.CUSTOM_PROPERTIES_REMOVE, new RemovePropertyAction(), false);

    // JajukJMenuBar: configuration
    installAction(JajukActions.CONFIGURE_DJS, new DJConfigurationAction(), false);
    installAction(JajukActions.CONFIGURE_WEBRADIOS, new WebRadioConfigurationAction(), false);
    installAction(JajukActions.CONFIGURE_AMBIENCES, new AmbienceConfigurationAction(), false);
    installAction(JajukActions.SIMPLE_DEVICE_WIZARD, new SimpleDeviceWizardAction(), false);
    installAction(JajukActions.OPTIONS, new ConfigurationRequiredAction(), false);
    installAction(JajukActions.UNMOUNTED, new HideShowMountedDevicesAction(), false);

    // JajukJMenuBar: Help Menu
    installAction(JajukActions.HELP_REQUIRED, new HelpRequiredAction(), false);
    installAction(JajukActions.SHOW_DONATE, new ShowDonateAction(), false);
    installAction(JajukActions.SHOW_ABOUT, new ShowAboutAction(), false);
    installAction(JajukActions.EXTRA_TAGS_WIZARD, new ShowActivateTagsAction(), false);
    // Install this action only if Desktop class is supported, it is used to
    // open default mail client
    if (UtilSystem.isBrowserSupported()) {
      installAction(JajukActions.QUALITY, new QualityAction(), false);
    }
    installAction(JajukActions.SHOW_TRACES, new DebugLogAction(), false);
    installAction(JajukActions.TIP_OF_THE_DAY, new TipOfTheDayAction(), false);
    installAction(JajukActions.CHECK_FOR_UPDATES, new CheckForUpdateAction(), false);

    // Export
    installAction(JajukActions.CREATE_REPORT, new ReportAction(), false);

    // File Actions
    installAction(JajukActions.CUT, new CutAction(), false);
    installAction(JajukActions.COPY, new CopyAction(), false);
    installAction(JajukActions.DELETE, new DeleteSelectionAction(), false);
    installAction(JajukActions.PASTE, new PasteAction(), false);
    installAction(JajukActions.RENAME, new RenameAction(), false);
    installAction(JajukActions.NEW_FOLDER, new NewFolderAction(), false);

    // MISC
    installAction(JajukActions.FIND_DUPLICATE_FILES, new FindDuplicateTracksAction(), false);
    installAction(JajukActions.COPY_TO_CLIPBOARD, new CopyClipboardAction(), false);
    installAction(JajukActions.OPEN_EXPLORER, new OpenExplorerAction(), false);
    installAction(JajukActions.REFRESH, new RefreshAction(), false);
    installAction(JajukActions.ALARM_CLOCK, new AlarmClockAction(), false);
    installAction(JajukActions.SHOW_ALBUM_DETAILS, new ShowAlbumDetailsAction(), false);
    installAction(JajukActions.SLIM_JAJUK, new SlimbarAction(), false);
    installAction(JajukActions.COMMIT, new CommitAction(), false);
    installAction(JajukActions.GC, new GCAction(), false);
    installAction(JajukActions.QUEUE_TO_SLIM, new SlimBarQueueAction(), false);
    // Install full screen actions only if supported
    installAction(JajukActions.FULLSCREEN_JAJUK, new FullscreenAction(), false);
    installAction(JajukActions.PREPARE_PARTY, new PreparePartyAction(), false);

    // Selection actions
    installAction(JajukActions.SHOW_PROPERTIES, new ShowPropertiesAction(), true);
    installAction(JajukActions.PLAY_SELECTION, new PlaySelectionAction(), false);
    installAction(JajukActions.PLAY_SHUFFLE_SELECTION, new PlayShuffleSelectionAction(), false);
    installAction(JajukActions.PLAY_REPEAT_SELECTION, new PlayRepeatSelectionAction(), false);
    installAction(JajukActions.PUSH_FRONT_SELECTION, new PushFrontSelectionAction(), false);
    installAction(JajukActions.PUSH_SELECTION, new PushSelectionAction(), false);
    installAction(JajukActions.BOOKMARK_SELECTION, new BookmarkSelectionAction(), false);
    installAction(JajukActions.PLAY_ALBUM_SELECTION, new PlayAlbumSelectionAction(), false);
    installAction(JajukActions.PLAY_ARTIST_SELECTION, new PlayArtistSelectionAction(), false);
    installAction(JajukActions.PLAY_DIRECTORY_SELECTION, new PlayDirectorySelectionAction(), false);
    installAction(JajukActions.CDDB_SELECTION, new CDDBSelectionAction(), false);
    installAction(JajukActions.SAVE_AS, new SaveAsAction(), false);
    installAction(JajukActions.SYNC_TREE_TABLE, new SyncTreeTableAction(), false);
    installAction(JajukActions.SHOW_CURRENTLY_PLAYING, new ShowCurrentlyPlayingAction(), false);

    // Preferences
    installAction(JajukActions.BAN, new BanCurrentAction(), false);
    installAction(JajukActions.BAN_SELECTION, new BanSelectionAction(), false);
    installAction(JajukActions.UN_BAN_SELECTION, new UnBanSelectionAction(), false);
    installAction(JajukActions.PREFERENCE_ADORE, new AdoreSelectionAction(), false);
    installAction(JajukActions.PREFERENCE_LOVE, new LoveSelectionAction(), false);
    installAction(JajukActions.PREFERENCE_LIKE, new LikeSelectionAction(), false);
    installAction(JajukActions.PREFERENCE_AVERAGE, new AverageSelectionAction(), false);
    installAction(JajukActions.PREFERENCE_POOR, new PoorSelectionAction(), false);
    installAction(JajukActions.PREFERENCE_HATE, new HateSelectionAction(), false);
    installAction(JajukActions.PREFERENCE_UNSET, new UnsetPreferenceSelectionAction(), false);
    // Install this action only if Desktop class is supported, it is used to
    // open default web browser
    if (UtilSystem.isBrowserSupported()) {
      installAction(JajukActions.LAUNCH_IN_BROWSER, new LaunchInBrowserAction(), false);
    }

    // Uninstall Look and feel keystrokes if required
    uninstallStrokes();

  }

  /**
   * Gets the action.
   * 
   * @param action The <code>JajukActions</code> to get.
   * 
   * @return The <code>JajukAction</code> implementation linked to the
   * <code>JajukActions</code>.
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
   * @param name The name for the action.
   * @param action The action implementation.
   * @param removeFromLAF Remove default keystrokes from look and feel.
   */
  private static void installAction(JajukActions name, JajukAction action, boolean removeFromLAF) {
    MAP.put(name, action);

    if (removeFromLAF) {
      KeyStroke stroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
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
   * Enable or disable the action.
   *
   * @param action DOCUMENT_ME
   * @param enable DOCUMENT_ME
   */
  public void enable(JajukAction action, boolean enable) {
    action.enable(enable);
  }
}