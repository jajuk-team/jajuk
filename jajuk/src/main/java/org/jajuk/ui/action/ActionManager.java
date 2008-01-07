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
package org.jajuk.ui.action;

import static org.jajuk.ui.action.JajukAction.ALARM_CLOCK;
import static org.jajuk.ui.action.JajukAction.BEST_OF;
import static org.jajuk.ui.action.JajukAction.CONFIGURE_AMBIENCES;
import static org.jajuk.ui.action.JajukAction.CONFIGURE_DJS;
import static org.jajuk.ui.action.JajukAction.CONTINUE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.action.JajukAction.COPY;
import static org.jajuk.ui.action.JajukAction.CUSTOM_PROPERTIES_ADD;
import static org.jajuk.ui.action.JajukAction.CUSTOM_PROPERTIES_REMOVE;
import static org.jajuk.ui.action.JajukAction.CUT;
import static org.jajuk.ui.action.JajukAction.DECREASE_VOLUME;
import static org.jajuk.ui.action.JajukAction.DELETE;
import static org.jajuk.ui.action.JajukAction.DJ;
import static org.jajuk.ui.action.JajukAction.EXIT;
import static org.jajuk.ui.action.JajukAction.FAST_FORWARD_TRACK;
import static org.jajuk.ui.action.JajukAction.FIND_DUPLICATE_FILES;
import static org.jajuk.ui.action.JajukAction.FINISH_ALBUM;
import static org.jajuk.ui.action.JajukAction.HELP_REQUIRED;
import static org.jajuk.ui.action.JajukAction.INCREASE_VOLUME;
import static org.jajuk.ui.action.JajukAction.INTRO_MODE_STATUS_CHANGED;
import static org.jajuk.ui.action.JajukAction.MUTE_STATE;
import static org.jajuk.ui.action.JajukAction.NEXT_ALBUM;
import static org.jajuk.ui.action.JajukAction.NEXT_TRACK;
import static org.jajuk.ui.action.JajukAction.NEW_FOLDER;
import static org.jajuk.ui.action.JajukAction.NOVELTIES;
import static org.jajuk.ui.action.JajukAction.OPTIONS;
import static org.jajuk.ui.action.JajukAction.PASTE;
import static org.jajuk.ui.action.JajukAction.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.action.JajukAction.PREVIOUS_ALBUM;
import static org.jajuk.ui.action.JajukAction.PREVIOUS_TRACK;
import static org.jajuk.ui.action.JajukAction.QUALITY;
import static org.jajuk.ui.action.JajukAction.REFRESH;
import static org.jajuk.ui.action.JajukAction.RENAME;
import static org.jajuk.ui.action.JajukAction.REPEAT_MODE_STATUS_CHANGE;
import static org.jajuk.ui.action.JajukAction.REWIND_TRACK;
import static org.jajuk.ui.action.JajukAction.SHOW_ABOUT;
import static org.jajuk.ui.action.JajukAction.SHOW_TRACES;
import static org.jajuk.ui.action.JajukAction.SHUFFLE_GLOBAL;
import static org.jajuk.ui.action.JajukAction.SHUFFLE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.action.JajukAction.STOP_TRACK;
import static org.jajuk.ui.action.JajukAction.TIP_OF_THE_DAY;
import static org.jajuk.ui.action.JajukAction.VIEW_RESTORE_DEFAULTS;
import static org.jajuk.ui.action.JajukAction.WIZARD;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.jajuk.util.Util;

/**
 * Helper class used to create, store and lookup actions.
 * <p>
 * Singleton
 * </p>
 */
public final class ActionManager {

  private static final EnumMap<JajukAction, ActionBase> map = new EnumMap<JajukAction, ActionBase>(
      JajukAction.class);

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
    installAction(JajukAction.WEB_RADIO, new WebRadioAction(), false);

    // CommandJPanel: Play Panel
    installAction(PREVIOUS_TRACK, new PreviousTrackAction(), true);
    installAction(NEXT_TRACK, new NextTrackAction(), true);
    installAction(PREVIOUS_ALBUM, new PreviousAlbumAction(), true);
    installAction(NEXT_ALBUM, new NextAlbumAction(), true);
    installAction(REWIND_TRACK, new RewindTrackAction(), true);
    installAction(PLAY_PAUSE_TRACK, new PlayPauseAction(), false);
    installAction(STOP_TRACK, new StopTrackAction(), false);
    installAction(FAST_FORWARD_TRACK, new ForwardTrackAction(), true);
    installAction(JajukAction.INC_RATE, new IncRateAction(), true);

    // CommandJPanel: Volume control
    installAction(DECREASE_VOLUME, new DecreaseVolumeAction(), true);
    installAction(INCREASE_VOLUME, new IncreaseVolumeAction(), true);
    installAction(MUTE_STATE, new MuteAction(), false);

    // JajukJMenuBar: File Menu
    installAction(EXIT, new ExitAction(), false);

    // JajukJMenuBar: views
    installAction(VIEW_RESTORE_DEFAULTS, new RestoreViewsAction(), false);
    installAction(JajukAction.ALL_VIEW_RESTORE_DEFAULTS, new RestoreAllViewsAction(), false);

    // JajukJMenuBar: attributes
    installAction(CUSTOM_PROPERTIES_ADD, new NewPropertyAction(), false);
    installAction(CUSTOM_PROPERTIES_REMOVE, new RemovePropertyAction(), false);

    // JajukJMenuBar: configuration
    installAction(CONFIGURE_DJS, new DJConfigurationAction(), false);
    installAction(JajukAction.CONFIGURE_WEBRADIOS, new WebRadioConfigurationAction(), false);
    installAction(CONFIGURE_AMBIENCES, new AmbienceConfigurationAction(), false);
    installAction(WIZARD, new WizardAction(), false);
    installAction(OPTIONS, new ConfigurationRequiredAction(), false);
    installAction(JajukAction.UNMOUNTED, new HideShowMountedDevicesAction(), false);

    // JajukJMenuBar: Help Menu
    installAction(HELP_REQUIRED, new HelpRequiredAction(), false);
    installAction(SHOW_ABOUT, new ShowAboutAction(), false);
    // Do not install this action under OSX because it causes a crash as jar is
    // missing
    if (Util.isUnderLinux() || Util.isUnderWindows()) {
      installAction(QUALITY, new QualityAction(), false);
    }
    installAction(SHOW_TRACES, new DebugLogAction(), false);
    installAction(TIP_OF_THE_DAY, new TipOfTheDayAction(), false);
    installAction(JajukAction.CHECK_FOR_UPDATES, new CheckForUpdateAction(), false);

    // Export
    installAction(JajukAction.CREATE_REPORT, new ReportAction(), false);

    // File Actions
    installAction(CUT, new CutAction(), false);
    installAction(COPY, new CopyAction(), false);
    installAction(DELETE, new DeleteAction(), false);
    installAction(PASTE, new PasteAction(), false);
    installAction(RENAME, new RenameAction(), false);
    installAction(NEW_FOLDER, new NewFolderAction(), false);
    
    // MISC
    installAction(FIND_DUPLICATE_FILES, new FindDuplicateFilesAction(), false);
    installAction(JajukAction.COPY_TO_CLIPBOARD, new CopyClipboardAction(), false);
    installAction(REFRESH, new RefreshDirectoryAction(), false);
    installAction(ALARM_CLOCK, new AlarmClockAction(), false);
    installAction(JajukAction.SHOW_ALBUM_DETAILS, new ShowAlbumDetailsAction(), false);
    
    //Selection actions
    installAction(JajukAction.SHOW_PROPERTIES, new ShowPropertiesAction(), false);
    installAction(JajukAction.PLAY_SELECTION, new PlaySelectionAction(), false);
    installAction(JajukAction.PLAY_SHUFFLE_SELECTION, new PlayShuffleSelectionAction(), false);
    installAction(JajukAction.PLAY_REPEAT_SELECTION, new PlayRepeatSelectionAction(), false);
    installAction(JajukAction.PUSH_SELECTION, new PushSelectionAction(), false);
    installAction(JajukAction.BOOKMARK_SELECTION, new BookmarkSelectionAction(), false);
    installAction(JajukAction.PLAY_ALBUM_SELECTION, new PlayAlbumSelectionAction(), false);
    installAction(JajukAction.PLAY_AUTHOR_SELECTION, new PlayAuthorSelectionAction(), false);
    installAction(JajukAction.PLAY_DIRECTORY_SELECTION, new PlayDirectorySelectionAction(), false);
    installAction(JajukAction.CDDB_SELECTION, new CDDBSelectionAction(), false);
       
    // Do not install this action under OSX because it causes a crash as jar is
    // missing
    if (Util.isUnderLinux() || Util.isUnderWindows()) {
      installAction(JajukAction.LAUNCH_IN_BROWSER, new LaunchInBrowserAction(), false);
    }
  }

  /**
   * @param action
   *          The <code>JajukAction</code> to get.
   * @return The <code>ActionBase</code> implementation linked to the
   *         <code>JajukAction</code>.
   */
  public static ActionBase getAction(JajukAction action) {
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
  private static void installAction(JajukAction name, ActionBase action, boolean removeFromLAF) {
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