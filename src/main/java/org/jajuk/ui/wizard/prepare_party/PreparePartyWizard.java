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
package org.jajuk.ui.wizard.prepare_party;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.jajuk.base.Playlist;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.LocaleManager;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilPrepareParty;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.qdwizard.Screen;
import org.qdwizard.Wizard;

/**
 * Wizard to select a set of files and write them to a separate directory
 * outside of the collection in order to use them in a MP3 device or any other
 * media player.
 *
 * TODO: progress bar is not done yet
 *
 * TODO: a "cancel" button in the progress bar would be nice to let the user
 * cancel if he finds out that too many were selected
 */
public class PreparePartyWizard extends Wizard {
  /** Wizard data*/
  enum Variable {
    /** Which source to use for the tracks. */
    MODE,
    /** Which item was selected in the first page of the wizard. */
    ITEM,
    /** Where to put the files. */
    DEST_PATH,
    /** Whether there is a track number max */
    MAXTRACKS_ENABLED,
    /** Key for max. number of track */
    MAXTRACKS,
    /** Whether there is a size max. */
    MAXSIZE_ENABLED,
    /** Key for max. size of party */
    MAXSIZE,
    /** Whether there is a length max.*/
    MAXLENGTH_ENABLED,
    /** Key for max length of party. */
    MAXLENGTH,
    /** Whether we limit conversion to one format. */
    ONE_MEDIA_ENABLED,
    /** Key for limit to one audio type. */
    ONE_MEDIA,
    /** Key for audio type conversion. */
    CONVERT_MEDIA,
    /** Key for the command to use for audio conversion. */
    CONVERT_COMMAND,
    /** Used to enable replacing characters outside the normal range. */
    NORMALIZE_FILENAME,
    /** Ratings level. */
    RATING_LEVEL,
    /** store a temporary playlist that is provided by the PlaylistView without storing it in the PlaylistManager we keep it
     * here to be able to re-display it in the Pages later on
     * We need to keep it outside the ActionSelectionPanel because the panel is re-created during back-forward operations. */
    TEMP_PLAYLIST
  }

  /**
   * The source of the Party.
   */
  enum Mode {
    /** Use one of the available DJs. */
    DJ,
    /** Use one of hte available Ambiences. */
    Ambience,
    /** Use random tracks from all available track. */
    Shuffle,
    /** Use a playlist. */
    Playlist,
    /** Use songs from the BestOf list. */
    BestOf,
    /** Use songs from the Novelties list. */
    Novelties,
    /** Use songs from the current play queue. */
    Queue,
    /** Use the available bookmarks. */
    Bookmarks,
    /** Special mode for when the dialog is invoked with a newly created playlist. */
    ProvidedPlaylist
  }

  /**
   * Default constructor that lets the user choose where the tracks are taken
   * from.
   *
   * @param bProvidedPlaylist Indicates that a playlist was provided to the dialog and thus the
   * first page is not displayed
   */
  public PreparePartyWizard(boolean bProvidedPlaylist, Playlist playlist) {
    super(new Wizard.Builder(Messages.getString("PreparePartyWizard.1"),
        bProvidedPlaylist ? PreparePartyWizardGeneralOptionsScreen.class
            : PreparePartyWizardActionSelectionScreen.class, JajukMainWindow.getInstance())
        .hSize(800).vSize(600).locale(LocaleManager.getLocale())
        .icon(IconLoader.getIcon(JajukIcons.PREPARE_PARTY_32X32)));
    if (playlist != null) {
      setPlaylist(playlist);
    }
    restoreProperties();
  }

  /**
   * Set the provided playlist so that the first page can be skipped if wanted.
   *
   * This needs to be done as static method as the Wizard-constructor already
   * needs to have this data available!
   *
   * @param playlist The playlist to use for the party
   */
  public void setPlaylist(Playlist playlist) {
    // store playlist and the mode that we are now having
    data.put(Variable.TEMP_PLAYLIST, playlist);
    // store the mode and the playlist in the data as well
    data.put(Variable.MODE, Mode.ProvidedPlaylist);
    data.put(Variable.ITEM, playlist.getName());
  }

  @Override
  public void finish() {
    // write properties to keep the selected directory
    storeProperties();
    // retrieve the full list of files according to the selected mode
    List<org.jajuk.base.File> files = getFiles();
    if (files == null) {
      return;
    }
    // define the target directory
    final Date curDate = new Date();
    // Do not use ':' character in destination directory, it's
    // forbidden under Windows
    final SimpleDateFormat stamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault());
    final String dirName = "Party-" + stamp.format(curDate);
    final java.io.File destDir = new java.io.File(((String) data.get(Variable.DEST_PATH)), dirName);
    if (!destDir.mkdir()) {
      Log.warn("Could not create destination directory " + destDir);
    }
    Log.debug("Going to copy " + files.size() + " files to directory {{"
        + destDir.getAbsolutePath() + "}}");
    // perform the actual copying
    UtilPrepareParty.copyFiles(files, destDir, isTrue(Variable.NORMALIZE_FILENAME),
        isTrue(Variable.ONE_MEDIA_ENABLED) && isTrue(Variable.CONVERT_MEDIA),
        (String) data.get(Variable.ONE_MEDIA), (String) data.get(Variable.CONVERT_COMMAND));
  }

  /**
   * Return if the specified element is true in the data-map.
   *
   * @param key The key to look up in the data-object.
   *
   * @return true if the value was stored as boolean true, false otherwise.
   */
  private boolean isTrue(final Variable key) {
    return data.containsKey(key) && Boolean.TRUE.equals(data.get(key));
  }

  /**
   * Gets the list of files to copy depending on the current mode.
   *
   * @return the files
   */
  private List<org.jajuk.base.File> getFiles() {
    List<org.jajuk.base.File> files;
    if (Mode.DJ.equals(data.get(Variable.MODE))) {
      files = UtilPrepareParty.getDJFiles((String) data.get(Variable.ITEM));
    } else if (Mode.Ambience.equals(data.get(Variable.MODE))) {
      files = UtilPrepareParty.getAmbienceFiles((String) data.get(Variable.ITEM));
    } else if (Mode.Playlist.equals(data.get(Variable.MODE))
        || Mode.ProvidedPlaylist.equals(data.get(Variable.MODE))) {
      try {
        Playlist tempPlaylist = (Playlist) data.get(Variable.TEMP_PLAYLIST);
        files = UtilPrepareParty.getPlaylistFiles((String) data.get(Variable.ITEM), tempPlaylist);
      } catch (JajukException e1) {
        Log.error(e1);
        return null;
      }
    } else if (Mode.Shuffle.equals(data.get(Variable.MODE))) {
      files = UtilPrepareParty.getShuffleFiles();
    } else if (Mode.BestOf.equals(data.get(Variable.MODE))) {
      try {
        files = UtilPrepareParty.getBestOfFiles();
      } catch (JajukException e1) {
        Log.error(e1);
        return null;
      }
    } else if (Mode.Queue.equals(data.get(Variable.MODE))) {
      try {
        files = UtilPrepareParty.getQueueFiles();
      } catch (JajukException e1) {
        Log.error(e1);
        return null;
      }
    } else if (Mode.Bookmarks.equals(data.get(Variable.MODE))) {
      try {
        files = UtilPrepareParty.getBookmarkFiles();
      } catch (JajukException e1) {
        Log.error(e1);
        return null;
      }
    } else if (Mode.Novelties.equals(data.get(Variable.MODE))) {
      try {
        files = UtilPrepareParty.getNoveltiesFiles();
      } catch (JajukException e1) {
        Log.error(e1);
        return null;
      }
    } else {
      throw new IllegalArgumentException("Unknown mode in PreparePartyWizard: "
          + data.get(Variable.MODE));
    }
    // filter by media first
    if (isTrue(Variable.ONE_MEDIA_ENABLED) && !isTrue(Variable.CONVERT_MEDIA)) {
      files = UtilPrepareParty.filterMedia(files, (String) data.get(Variable.ONE_MEDIA));
    }
    // then filter out by rating
    if (data.containsKey(Variable.RATING_LEVEL)) {
      files = UtilPrepareParty.filterRating(files, (Integer) data.get(Variable.RATING_LEVEL));
    }
    // filter max length
    if (isTrue(Variable.MAXLENGTH_ENABLED)) {
      files = UtilPrepareParty.filterMaxLength(files, (Integer) data.get(Variable.MAXLENGTH));
    }
    // filter max size
    if (isTrue(Variable.MAXSIZE_ENABLED)) {
      files = UtilPrepareParty.filterMaxSize(files, (Integer) data.get(Variable.MAXSIZE));
    }
    // filter max tracks
    if (isTrue(Variable.MAXTRACKS_ENABLED)) {
      files = UtilPrepareParty.filterMaxTracks(files, (Integer) data.get(Variable.MAXTRACKS));
    }
    return files;
  }

  /**
   * Stores all the values that are stored in the data-map to the Conf-system.
   */
  private void storeProperties() {
    storeValue(Variable.MODE);
    storeValue(Variable.ITEM);
    storeValue(Variable.DEST_PATH);
    storeValue(Variable.MAXTRACKS_ENABLED);
    storeValue(Variable.MAXTRACKS);
    storeValue(Variable.MAXSIZE_ENABLED);
    storeValue(Variable.MAXSIZE);
    storeValue(Variable.MAXLENGTH_ENABLED);
    storeValue(Variable.MAXLENGTH);
    storeValue(Variable.ONE_MEDIA_ENABLED);
    storeValue(Variable.ONE_MEDIA);
    storeValue(Variable.CONVERT_MEDIA);
    storeValue(Variable.CONVERT_COMMAND);
    storeValue(Variable.NORMALIZE_FILENAME);
    storeValue(Variable.RATING_LEVEL);
  }

  /**
   * Store one value as String.
   *
   * @param key The name of the property to store in the overall configuration
   */
  private void storeValue(final Variable key) {
    // nothing to do?
    if (data.get(key) == null) {
      return;
    }
    Conf.setProperty(Const.CONF_PREPARE_PARTY + key, data.get(key).toString());
  }

  /**
   * Restore all the values that are potentially stored in the configuration
   * system.
   */
  private void restoreProperties() {
    restoreModeAndItemValue();
    restoreStringValue(Variable.DEST_PATH);
    restoreBooleanValue(Variable.MAXTRACKS_ENABLED);
    restoreIntValue(Variable.MAXTRACKS);
    restoreBooleanValue(Variable.MAXSIZE_ENABLED);
    restoreIntValue(Variable.MAXSIZE);
    restoreBooleanValue(Variable.MAXLENGTH_ENABLED);
    restoreIntValue(Variable.MAXLENGTH);
    restoreBooleanValue(Variable.ONE_MEDIA_ENABLED);
    restoreStringValue(Variable.ONE_MEDIA);
    restoreBooleanValue(Variable.CONVERT_MEDIA);
    restoreStringValue(Variable.CONVERT_COMMAND);
    if (StringUtils.isBlank((String) data.get(Variable.CONVERT_COMMAND))) {
      data.put(Variable.CONVERT_COMMAND, "pacpl"); // use default value if none set
      // yet
    }
    restoreBooleanValue(Variable.NORMALIZE_FILENAME);
    restoreIntValue(Variable.RATING_LEVEL);
  }

  /**
   * Restore one string value from the configuration.
   *
   * @param key The key to restore.
   */
  private void restoreStringValue(final Variable key) {
    String sValue = Conf.getString(Const.CONF_PREPARE_PARTY + key);
    // nothing to do if not set
    if (sValue == null) {
      return;
    }
    data.put(key, sValue);
  }

  /**
   * Restore one integer value from the configuration.
   *
   * @param key The key to restore.
   */
  private void restoreIntValue(final Variable key) {
    // do nothing if not available yet
    if (Conf.getString(Const.CONF_PREPARE_PARTY + key) == null) {
      return;
    }
    data.put(key, Conf.getInt(Const.CONF_PREPARE_PARTY + key));
  }

  /**
   * Restore one boolean value from the configuration.
   *
   * @param key The key to restore.
   */
  private void restoreBooleanValue(final Variable key) {
    // do nothing if not available yet
    if (Conf.getString(Const.CONF_PREPARE_PARTY + key) == null) {
      return;
    }
    data.put(key, Conf.getBoolean(Const.CONF_PREPARE_PARTY + key));
  }

  /**
   * Restore mode and item values, they may require some special handling.
   */
  private void restoreModeAndItemValue() {
    String sMode = Conf.getString(Const.CONF_PREPARE_PARTY + Variable.MODE);
    // nothing to do if not set
    if (sMode == null) {
      return;
    }
    try {
      data.put(Variable.MODE, Mode.valueOf(sMode));
    } catch (IllegalArgumentException e) {
      Log.warn("Could not convert mode: " + sMode + ", using default mode: " + Mode.DJ);
      data.put(Variable.MODE, Mode.DJ);
    }
    switch ((Mode) data.get(Variable.MODE)) {
    // restore the value for the ones where we have a selection
    case Ambience:
    case DJ:
    case Playlist:
      data.put(Variable.ITEM, Conf.getString(Const.CONF_PREPARE_PARTY + Variable.ITEM));
      break;
    // nothing to do
    case BestOf:
    case Bookmarks:
    case Shuffle:
    case Novelties:
    case Queue:
      // we usually are not able to restore this, therefore don't do anything
    case ProvidedPlaylist:
    default:
      break;
    }
  }

  @Override
  public Class<? extends Screen> getNextScreen(Class<? extends Screen> screen) {
    if (PreparePartyWizardActionSelectionScreen.class.equals(screen)) {
      return PreparePartyWizardGeneralOptionsScreen.class;
    } else if (PreparePartyWizardGeneralOptionsScreen.class.equals(screen)) {
      return PreparePartyWizardPathSelectionScreen.class;
    }
    return null;
  }

  @Override
  public Class<? extends Screen> getPreviousScreen(Class<? extends Screen> screen) {
    // there is no "back" if we got a playlist passed in
    if (PreparePartyWizardGeneralOptionsScreen.class.equals(screen)
        && !Mode.ProvidedPlaylist.equals(data.get(Variable.MODE))) {
      return PreparePartyWizardActionSelectionScreen.class;
    } else if (PreparePartyWizardPathSelectionScreen.class.equals(screen)) {
      return PreparePartyWizardGeneralOptionsScreen.class;
    }
    return null;
  }
}
