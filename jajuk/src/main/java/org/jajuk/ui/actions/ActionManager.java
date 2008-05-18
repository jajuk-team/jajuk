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
import static org.jajuk.ui.actions.JajukActions.BEST_OF;
import static org.jajuk.ui.actions.JajukActions.CONFIGURE_AMBIENCES;
import static org.jajuk.ui.actions.JajukActions.CONFIGURE_DJS;
import static org.jajuk.ui.actions.JajukActions.CONTINUE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.actions.JajukActions.COPY;
import static org.jajuk.ui.actions.JajukActions.CUSTOM_PROPERTIES_ADD;
import static org.jajuk.ui.actions.JajukActions.CUSTOM_PROPERTIES_REMOVE;
import static org.jajuk.ui.actions.JajukActions.CUT;
import static org.jajuk.ui.actions.JajukActions.DECREASE_VOLUME;
import static org.jajuk.ui.actions.JajukActions.DELETE;
import static org.jajuk.ui.actions.JajukActions.DJ;
import static org.jajuk.ui.actions.JajukActions.EXIT;
import static org.jajuk.ui.actions.JajukActions.FAST_FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukActions.FIND_DUPLICATE_FILES;
import static org.jajuk.ui.actions.JajukActions.FINISH_ALBUM;
import static org.jajuk.ui.actions.JajukActions.HELP_REQUIRED;
import static org.jajuk.ui.actions.JajukActions.INCREASE_VOLUME;
import static org.jajuk.ui.actions.JajukActions.INTRO_MODE_STATUS_CHANGED;
import static org.jajuk.ui.actions.JajukActions.MUTE_STATE;
import static org.jajuk.ui.actions.JajukActions.NEW_FOLDER;
import static org.jajuk.ui.actions.JajukActions.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.NOVELTIES;
import static org.jajuk.ui.actions.JajukActions.OPTIONS;
import static org.jajuk.ui.actions.JajukActions.PASTE;
import static org.jajuk.ui.actions.JajukActions.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.QUALITY;
import static org.jajuk.ui.actions.JajukActions.REFRESH;
import static org.jajuk.ui.actions.JajukActions.RENAME;
import static org.jajuk.ui.actions.JajukActions.REPEAT_MODE_STATUS_CHANGE;
import static org.jajuk.ui.actions.JajukActions.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukActions.SHOW_ABOUT;
import static org.jajuk.ui.actions.JajukActions.SHOW_TRACES;
import static org.jajuk.ui.actions.JajukActions.SHUFFLE_GLOBAL;
import static org.jajuk.ui.actions.JajukActions.SHUFFLE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.actions.JajukActions.SIMPLE_DEVICE_WIZARD;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;
import static org.jajuk.ui.actions.JajukActions.TIP_OF_THE_DAY;
import static org.jajuk.ui.actions.JajukActions.VIEW_RESTORE_DEFAULTS;

import java.awt.Desktop;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

/**
 * Helper class used to create, store and lookup actions.
 * <p>
 * Singleton
 * </p>
 */
public final class ActionManager {

  private static final EnumMap<JajukActions, ActionBase> map = new EnumMap<JajukActions, ActionBase>(
      JajukActions.class);

  private static final List<KeyStroke> strokeList = new ArrayList<KeyStroke>();

  /** Self instance */
  public static ActionManager self = null;

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
    installAction(REPEAT_MODE_STATUS_CHANGE, new RepeatModeAction(), false);
    installAction(SHUFFLE_MODE_STATUS_CHANGED, new ShuffleModeAction(), false);
    installAction(CONTINUE_MODE_STATUS_CHANGED, new ContinueModeAction(), false);
    installAction(INTRO_MODE_STATUS_CHANGED, new IntroModeAction(), false);

    // CommandJPanel: Special Functions Panel
    installAction(SHUFFLE_GLOBAL, new GlobalRandomAction(), false);
    installAction(BEST_OF, new BestOfAction(), false);
    installAction(DJ, new DJAction(), false);
    installAction(NOVELTIES, new NoveltiesAction(), false);
    installAction(FINISH_ALBUM, new FinishAlbumAction(), false);
    installAction(JajukActions.WEB_RADIO, new WebRadioAction(), false);

    // CommandJPanel: Play Panel
    installAction(PREVIOUS_TRACK, new PreviousTrackAction(), true);
    installAction(NEXT_TRACK, new NextTrackAction(), true);
    installAction(PREVIOUS_ALBUM, new PreviousAlbumAction(), true);
    installAction(NEXT_ALBUM, new NextAlbumAction(), true);
    installAction(REWIND_TRACK, new RewindTrackAction(), true);
    installAction(PLAY_PAUSE_TRACK, new PlayPauseAction(), false);
    installAction(STOP_TRACK, new StopTrackAction(), false);
    installAction(FAST_FORWARD_TRACK, new ForwardTrackAction(), true);
    installAction(JajukActions.INC_RATE, new IncRateAction(), true);

    // CommandJPanel: Volume control
    installAction(DECREASE_VOLUME, new DecreaseVolumeAction(), true);
    installAction(INCREASE_VOLUME, new IncreaseVolumeAction(), true);
    installAction(MUTE_STATE, new MuteAction(), false);

    // JajukJMenuBar: File Menu
    installAction(EXIT, new ExitAction(), false);

    // JajukJMenuBar: views
    installAction(VIEW_RESTORE_DEFAULTS, new RestoreViewsAction(), false);
    installAction(JajukActions.ALL_VIEW_RESTORE_DEFAULTS, new RestoreAllViewsAction(), false);

    // JajukJMenuBar: attributes
    installAction(CUSTOM_PROPERTIES_ADD, new NewPropertyAction(), false);
    installAction(CUSTOM_PROPERTIES_REMOVE, new RemovePropertyAction(), false);

    // JajukJMenuBar: configuration
    installAction(CONFIGURE_DJS, new DJConfigurationAction(), false);
    installAction(JajukActions.CONFIGURE_WEBRADIOS, new WebRadioConfigurationAction(), false);
    installAction(CONFIGURE_AMBIENCES, new AmbienceConfigurationAction(), false);
    installAction(SIMPLE_DEVICE_WIZARD, new SimpleDeviceWizardAction(), false);
    installAction(OPTIONS, new ConfigurationRequiredAction(), false);
    installAction(JajukActions.UNMOUNTED, new HideShowMountedDevicesAction(), false);

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
    installAction(JajukActions.CHECK_FOR_UPDATES, new CheckForUpdateAction(), false);

    // Export
    installAction(JajukActions.CREATE_REPORT, new ReportAction(), false);

    // File Actions
    installAction(CUT, new CutAction(), false);
    installAction(COPY, new CopyAction(), false);
    installAction(DELETE, new DeleteAction(), false);
    installAction(PASTE, new PasteAction(), false);
    installAction(RENAME, new RenameAction(), false);
    installAction(NEW_FOLDER, new NewFolderAction(), false);

    // MISC
    installAction(FIND_DUPLICATE_FILES, new FindDuplicateTracksAction(), false);
    installAction(JajukActions.COPY_TO_CLIPBOARD, new CopyClipboardAction(), false);
    installAction(REFRESH, new RefreshDirectoryAction(), false);
    installAction(ALARM_CLOCK, new AlarmClockAction(), false);
    installAction(JajukActions.SHOW_ALBUM_DETAILS, new ShowAlbumDetailsAction(), false);
    installAction(JajukActions.SLIM_JAJUK, new SlimbarAction(), false);

    // Selection actions
    installAction(JajukActions.SHOW_PROPERTIES, new ShowPropertiesAction(), false);
    installAction(JajukActions.PLAY_SELECTION, new PlaySelectionAction(), false);
    installAction(JajukActions.PLAY_SHUFFLE_SELECTION, new PlayShuffleSelectionAction(), false);
    installAction(JajukActions.PLAY_REPEAT_SELECTION, new PlayRepeatSelectionAction(), false);
    installAction(JajukActions.PUSH_SELECTION, new PushSelectionAction(), false);
    installAction(JajukActions.BOOKMARK_SELECTION, new BookmarkSelectionAction(), false);
    installAction(JajukActions.PLAY_ALBUM_SELECTION, new PlayAlbumSelectionAction(), false);
    installAction(JajukActions.PLAY_AUTHOR_SELECTION, new PlayAuthorSelectionAction(), false);
    installAction(JajukActions.PLAY_DIRECTORY_SELECTION, new PlayDirectorySelectionAction(), false);
    installAction(JajukActions.CDDB_SELECTION, new CDDBSelectionAction(), false);
    installAction(JajukActions.SAVE_AS, new SaveAsAction(), false);
    // Install this action only if Desktop class is supported, it is used to
    // open default web browser
    if (Desktop.isDesktopSupported()) {
      installAction(JajukActions.LAUNCH_IN_BROWSER, new LaunchInBrowserAction(), false);
    }
  }

  /**
   * @param action
   *          The <code>JajukActions</code> to get.
   * @return The <code>ActionBase</code> implementation linked to the
   *         <code>JajukActions</code>.
   */
  public static ActionBase getAction(JajukActions action) {
    ActionBase actionBase = map.get(action);
    if (actionBase == null) {
      throw new ExceptionInInitializerError("No action mapping found for " + action);
    }
    return actionBase;
  }

  /**
   * Installs a new action in the action manager. If <code>removeFromLAF</code>
   * is <code>true</code>, then the keystroke attached to the action will be
   * stored in list. To remove the these keystrokes from the
   * <code>InputMap</code>s of the different components, call
   * {@link #uninstallStrokes()}.
   * 
   * @param name
   *          The name for the action.
   * @param action
   *          The action implementation.
   * @param removeFromLAF
   *          Remove default keystrokes from look and feel.
   */
  private static void installAction(JajukActions name, ActionBase action, boolean removeFromLAF) {
    map.put(name, action);

    if (removeFromLAF) {
      KeyStroke stroke = (KeyStroke) action.getValue(ActionBase.ACCELERATOR_KEY);
      if (stroke != null) {
        strokeList.add(stroke);
      }
    }
  }

  /**
   * Uninstall default keystrokes from different JComponents to allow more
   * globally configured JaJuk keystrokes.
   */
  public static void uninstallStrokes() {
    InputMap tableMap = (InputMap) UIManager.get("Table.ancestorInputMap");
    InputMap treeMap = (InputMap) UIManager.get("Tree.focusInputMap");

    for (KeyStroke stroke : strokeList) {
      tableMap.remove(stroke);
      treeMap.remove(stroke);
    }
  }
}