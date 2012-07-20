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
package org.jajuk.ui.wizard;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.services.bookmark.Bookmarks;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.helpers.DefaultMouseWheelListener;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.LocaleManager;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilPrepareParty;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.DirectoryFilter;
import org.jajuk.util.log.Log;
import org.qdwizard.ClearPoint;
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
  /** For MigLayout. */
  private static final String GROW_WRAP = "grow,wrap";
  /** For MigLayout. */
  private static final String LEFT_WRAP = "left,wrap";
  /** For MigLayout. */
  private static final String LEFT = "left";
  /** Which source to use for the tracks. */
  private static final String KEY_MODE = "MODE";
  /** Which item was selected in the first page of the wizard. */
  private static final String KEY_ITEM = "ITEM";
  /** Where to put the files. */
  private static final String KEY_DEST_PATH = "DEST_PATH";
  /** Max number of tracks to use. */
  private static final String KEY_MAX_TRACKS_ON = "MAXTRACKS_ENABLED";
  /** Key for max. number of track */
  private static final String KEY_MAX_TRACKS = "MAXTRACKS";
  /** Max size to use. */
  private static final String KEY_MAX_SIZE_ON = "MAXSIZE_ENABLED";
  /** Key for max. size of party */
  private static final String KEY_MAX_SIZE = "MAXSIZE";
  /** Max playing length of tracks to use. */
  private static final String KEY_MAX_LENGTH_ON = "MAXLENGTH_ENABLED";
  /** Key for max length of party. */
  private static final String KEY_MAX_LENGTH = "MAXLENGTH";
  /** Max number of tracks to queue. */
  private static final String KEY_ONE_MEDIA_ON = "ONE_MEDIA_ENABLED";
  /** Key for limit to one audio type. */
  private static final String KEY_MEDIA = "ONE_MEDIA";
  /** Key for audio type conversion. */
  private static final String KEY_CONVERT_MEDIA = "CONVERT_MEDIA";
  /** Key for the command to use for audio conversion. */
  private static final String KEY_CONVERT_COMMAND = "CONVERT_COMMAND";
  /** Used to enable replacing characters outside the normal range. */
  private static final String KEY_NORMALIZE_FILENAME_ON = "NORMALIZE_FILENAME";
  /** Ratings level. */
  private static final String KEY_RATINGS_LEVEL = "RATING_LEVEL";
  /** store a temporary playlist that is provided by the PlaylistView without storing it in the PlaylistManager we keep it here to be able to re-display it in the Pages later on  We need to keep it outside the ActionSelectionPanel because the panel is re-created during back-forward operations. */
  private static Playlist tempPlaylist;
  /** Indicator to only restore properties once and not overwrite them again later. */
  private static boolean bPropertiesRestored = false;
  /** Indicates if the PACPL tool for audio conversion is available. */
  private static boolean bPACPLAvailable = false;

  /**
   * The source of the Party.
   */
  private enum Mode {
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
  public PreparePartyWizard(boolean bProvidedPlaylist) {
    super(Messages.getString("PreparePartyWizard.1"), bProvidedPlaylist ? GeneralOptionsPanel.class
        : ActionSelectionPanel.class, null, JajukMainWindow.getInstance(), LocaleManager
        .getLocale(), 800, 550);
    super.setHeaderIcon(IconLoader.getIcon(JajukIcons.PREPARE_PARTY_32X32));
    // check if pacpl can be used, do it every time the dialog starts as the
    // user might have installed it by now
    bPACPLAvailable = UtilPrepareParty.checkPACPL((String) data.get(KEY_CONVERT_COMMAND));
  }

  /**
   * Set the provided playlist so that the first page can be skipped if wanted.
   * 
   * This needs to be done as static method as the Wizard-constructor already
   * needs to have this data available!
   * 
   * @param playlist The playlist to use for the party
   */
  public static void setPlaylist(Playlist playlist) {
    // store playlist and the mode that we are now having
    tempPlaylist = playlist;
    // store the mode and the playlist in the data as well
    data.put(KEY_MODE, Mode.ProvidedPlaylist);
    data.put(KEY_ITEM, playlist.getName());
  }

  /**
   * Return if the specified element is true in the data-map.
   * 
   * @param key The key to look up in the data-object.
   * 
   * @return true if the value was stored as boolean true, false otherwise.
   */
  private static final boolean isTrue(final String key) {
    return data.containsKey(key) && Boolean.TRUE.equals(data.get(key));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.qdwizard.Wizard#finish()
   */
  @Override
  public void finish() {
    // write properties to keep the selected directory
    try {
      storeProperties();
      Conf.commit();
    } catch (IOException e1) {
      Log.error(e1);
    }
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
    final java.io.File destDir = new java.io.File(((String) data.get(KEY_DEST_PATH)), dirName);
    if (!destDir.mkdir()) {
      Log.warn("Could not create destination directory " + destDir);
    }
    Log.debug("Going to copy " + files.size() + " files to directory {{"
        + destDir.getAbsolutePath() + "}}");
    // perform the actual copying
    UtilPrepareParty.copyFiles(files, destDir, isTrue(KEY_NORMALIZE_FILENAME_ON),
        isTrue(KEY_ONE_MEDIA_ON) && isTrue(KEY_CONVERT_MEDIA), (String) data.get(KEY_MEDIA),
        (String) data.get(KEY_CONVERT_COMMAND));
  }

  /**
   * Gets the list of files to copy depending on the current mode.
   * 
   * @return the files
   */
  private List<org.jajuk.base.File> getFiles() {
    List<org.jajuk.base.File> files;
    if (Mode.DJ.equals(data.get(KEY_MODE))) {
      files = UtilPrepareParty.getDJFiles((String) data.get(KEY_ITEM));
    } else if (Mode.Ambience.equals(data.get(KEY_MODE))) {
      files = UtilPrepareParty.getAmbienceFiles((String) data.get(KEY_ITEM));
    } else if (Mode.Playlist.equals(data.get(KEY_MODE))
        || Mode.ProvidedPlaylist.equals(data.get(KEY_MODE))) {
      try {
        files = UtilPrepareParty.getPlaylistFiles((String) data.get(KEY_ITEM), tempPlaylist);
      } catch (JajukException e1) {
        Log.error(e1);
        return null;
      }
    } else if (Mode.Shuffle.equals(data.get(KEY_MODE))) {
      files = UtilPrepareParty.getShuffleFiles();
    } else if (Mode.BestOf.equals(data.get(KEY_MODE))) {
      try {
        files = UtilPrepareParty.getBestOfFiles();
      } catch (JajukException e1) {
        Log.error(e1);
        return null;
      }
    } else if (Mode.Queue.equals(data.get(KEY_MODE))) {
      try {
        files = UtilPrepareParty.getQueueFiles();
      } catch (JajukException e1) {
        Log.error(e1);
        return null;
      }
    } else if (Mode.Bookmarks.equals(data.get(KEY_MODE))) {
      try {
        files = UtilPrepareParty.getBookmarkFiles();
      } catch (JajukException e1) {
        Log.error(e1);
        return null;
      }
    } else if (Mode.Novelties.equals(data.get(KEY_MODE))) {
      try {
        files = UtilPrepareParty.getNoveltiesFiles();
      } catch (JajukException e1) {
        Log.error(e1);
        return null;
      }
    } else {
      throw new IllegalArgumentException("Unknown mode in PreparePartyWizard: "
          + data.get(KEY_MODE));
    }
    // filter by media first
    if (isTrue(KEY_ONE_MEDIA_ON) && !isTrue(KEY_CONVERT_MEDIA)) {
      files = UtilPrepareParty.filterMedia(files, (String) data.get(KEY_MEDIA));
    }
    // then filter out by rating
    if (data.containsKey(KEY_RATINGS_LEVEL)) {
      files = UtilPrepareParty.filterRating(files, (Integer) data.get(KEY_RATINGS_LEVEL));
    }
    // filter max length
    if (isTrue(KEY_MAX_LENGTH_ON)) {
      files = UtilPrepareParty.filterMaxLength(files, (Integer) data.get(KEY_MAX_LENGTH));
    }
    // filter max size
    if (isTrue(KEY_MAX_SIZE_ON)) {
      files = UtilPrepareParty.filterMaxSize(files, (Integer) data.get(KEY_MAX_SIZE));
    }
    // filter max tracks
    if (isTrue(KEY_MAX_TRACKS_ON)) {
      files = UtilPrepareParty.filterMaxTracks(files, (Integer) data.get(KEY_MAX_TRACKS));
    }
    return files;
  }

  /**
   * Stores all the values that are stored in the data-map to the Conf-system.
   */
  private static void storeProperties() {
    storeValue(KEY_MODE);
    storeValue(KEY_ITEM);
    storeValue(KEY_DEST_PATH);
    storeValue(KEY_MAX_TRACKS_ON);
    storeValue(KEY_MAX_TRACKS);
    storeValue(KEY_MAX_SIZE_ON);
    storeValue(KEY_MAX_SIZE);
    storeValue(KEY_MAX_LENGTH_ON);
    storeValue(KEY_MAX_LENGTH);
    storeValue(KEY_ONE_MEDIA_ON);
    storeValue(KEY_MEDIA);
    storeValue(KEY_CONVERT_MEDIA);
    storeValue(KEY_CONVERT_COMMAND);
    storeValue(KEY_NORMALIZE_FILENAME_ON);
    storeValue(KEY_RATINGS_LEVEL);
  }

  /**
   * Store one value as String.
   * 
   * @param key The name of the property to store in the overall configuration
   */
  private static void storeValue(final String key) {
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
  private static void restoreProperties() {
    // only restore once to not overwrite, due to the Wizard implementation we
    // don't know for sure when this is called;
    if (bPropertiesRestored) {
      return;
    }
    bPropertiesRestored = true;
    restoreModeAndItemValue();
    restoreStringValue(KEY_DEST_PATH);
    restoreBooleanValue(KEY_MAX_TRACKS_ON);
    restoreIntValue(KEY_MAX_TRACKS);
    restoreBooleanValue(KEY_MAX_SIZE_ON);
    restoreIntValue(KEY_MAX_SIZE);
    restoreBooleanValue(KEY_MAX_LENGTH_ON);
    restoreIntValue(KEY_MAX_LENGTH);
    restoreBooleanValue(KEY_ONE_MEDIA_ON);
    restoreStringValue(KEY_MEDIA);
    restoreBooleanValue(KEY_CONVERT_MEDIA);
    restoreStringValue(KEY_CONVERT_COMMAND);
    if (StringUtils.isBlank((String) data.get(KEY_CONVERT_COMMAND))) {
      data.put(KEY_CONVERT_COMMAND, "pacpl"); // use default value if none set
      // yet
    }
    restoreBooleanValue(KEY_NORMALIZE_FILENAME_ON);
    restoreIntValue(KEY_RATINGS_LEVEL);
  }

  /**
   * Restore one string value from the configuration.
   * 
   * @param key The key to restore.
   */
  private static void restoreStringValue(final String key) {
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
  private static void restoreIntValue(final String key) {
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
  private static void restoreBooleanValue(final String key) {
    // do nothing if not available yet
    if (Conf.getString(Const.CONF_PREPARE_PARTY + key) == null) {
      return;
    }
    data.put(key, Conf.getBoolean(Const.CONF_PREPARE_PARTY + key));
  }

  /**
   * Restore mode and item values, they may require some special handling.
   */
  private static void restoreModeAndItemValue() {
    String sMode = Conf.getString(Const.CONF_PREPARE_PARTY + KEY_MODE);
    // nothing to do if not set
    if (sMode == null) {
      return;
    }
    try {
      data.put(KEY_MODE, Mode.valueOf(sMode));
    } catch (IllegalArgumentException e) {
      Log.warn("Could not convert mode: " + sMode + ", using default mode: " + Mode.DJ);
      data.put(KEY_MODE, Mode.DJ);
    }
    switch ((Mode) data.get(KEY_MODE)) {
    // restore the value for the ones where we have a selection
    case Ambience:
    case DJ:
    case Playlist:
      data.put(KEY_ITEM, Conf.getString(Const.CONF_PREPARE_PARTY + KEY_ITEM));
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

  /*
   * (non-Javadoc)
   * 
   * @see org.qdwizard.Wizard#getNextScreen(java.lang.Class)
   */
  @Override
  public Class<? extends Screen> getNextScreen(Class<? extends Screen> screen) {
    if (ActionSelectionPanel.class.equals(getCurrentScreen())) {
      return GeneralOptionsPanel.class;
    } else if (GeneralOptionsPanel.class.equals(getCurrentScreen())) {
      return PathSelectionPanel.class;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.qdwizard.Wizard#getPreviousScreen(java.lang.Class)
   */
  @Override
  public Class<? extends Screen> getPreviousScreen(Class<? extends Screen> screen) {
    // there is no "back" if we got a playlist passed in
    if (GeneralOptionsPanel.class.equals(getCurrentScreen())
        && !Mode.ProvidedPlaylist.equals(data.get(KEY_MODE))) {
      return ActionSelectionPanel.class;
    } else if (PathSelectionPanel.class.equals(getCurrentScreen())) {
      return GeneralOptionsPanel.class;
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.qdwizard.Wizard#onCancel()
   */
  @Override
  public boolean onCancel() {
    // this also clears "data", so we need to reset the restore-state
    bPropertiesRestored = false;
    return super.onCancel();
  }

  /**
   * First Panel of the Wizard, it shows a selection of sources where the user can choose one, e.g. DJs, Ambiences, ...
   */
  public static class ActionSelectionPanel extends Screen implements ActionListener, ClearPoint {
    /** Generated serialVersionUID. */
    private static final long serialVersionUID = -6981770030816500259L;
    /** The group for the various sources. */
    private ButtonGroup bgActions;
    /** DJ. */
    private JRadioButton jrbDJ;
    /** DJ. */
    private JComboBox jcbDJ;
    /** Ambience. */
    private JRadioButton jrbAmbience;
    /** Ambience. */
    private JComboBox jcbAmbience;
    /** Playlist. */
    private JRadioButton jrbPlaylist;
    /** Playlist. */
    private JComboBox jcbPlaylist;
    /** Shuffle. */
    private JRadioButton jrbShuffle;
    /** Shuffle. */
    private JRadioButton jrbBestOf;
    /** Novelties. */
    private JRadioButton jrbNovelties;
    /** Queue. */
    private JRadioButton jrbQueue;
    /** Bookmarks. */
    private JRadioButton jrbBookmark;

    /**
     * Create panel UI.
     */
    @Override
    public void initUI() {
      // workaround as the dialog is initialized before the constructor of
      // PreparePartyWizard fully executes
      restoreProperties();
      bgActions = new ButtonGroup();
      jrbDJ = new JRadioButton(Messages.getString("PreparePartyWizard.6"));
      jrbDJ.addActionListener(this);
      // populate DJs
      List<DigitalDJ> djs = DigitalDJManager.getInstance().getDJsSorted();
      jcbDJ = new JComboBox();
      for (DigitalDJ dj : djs) {
        jcbDJ.addItem(dj.getName());
      }
      jcbDJ.addActionListener(this);
      jrbAmbience = new JRadioButton(Messages.getString("PreparePartyWizard.7"));
      jrbAmbience.addActionListener(this);
      List<Ambience> ambiences = AmbienceManager.getInstance().getAmbiences();
      jcbAmbience = new JComboBox();
      for (Ambience amb : ambiences) {
        jcbAmbience.addItem(amb.getName());
      }
      jcbAmbience.addActionListener(this);
      jrbPlaylist = new JRadioButton(Messages.getString("PreparePartyWizard.8"));
      jrbPlaylist.addActionListener(this);
      jcbPlaylist = new JComboBox();
      if (tempPlaylist != null) {
        // check if this is a "temporary" playlist that is provided by the
        // PlaylistView (i.e. not yet stored in PlaylistManager)
        jcbPlaylist.addItem(tempPlaylist.getName());
      }
      List<Playlist> playlists = PlaylistManager.getInstance().getPlaylists();
      for (Playlist pl : playlists) {
        jcbPlaylist.addItem(pl.getName());
      }
      jcbPlaylist.addActionListener(this);
      jrbShuffle = new JRadioButton(Messages.getString("PreparePartyWizard.9"));
      jrbShuffle.addActionListener(this);
      jrbBestOf = new JRadioButton(Messages.getString("PreparePartyWizard.24"));
      jrbBestOf.addActionListener(this);
      jrbNovelties = new JRadioButton(Messages.getString("PreparePartyWizard.25"));
      jrbNovelties.addActionListener(this);
      jrbQueue = new JRadioButton(Messages.getString("PreparePartyWizard.32"));
      jrbQueue.addActionListener(this);
      jrbBookmark = new JRadioButton(Messages.getString("PreparePartyWizard.33"));
      jrbBookmark.addActionListener(this);
      bgActions.add(jrbDJ);
      bgActions.add(jrbAmbience);
      bgActions.add(jrbPlaylist);
      bgActions.add(jrbBestOf);
      bgActions.add(jrbNovelties);
      bgActions.add(jrbQueue);
      bgActions.add(jrbBookmark);
      bgActions.add(jrbShuffle);
      // populate items from the stored static data
      readData();
      // populate the screen
      setLayout(new MigLayout("insets 10,gapx 10,gapy 15", "[][grow]"));
      add(jrbDJ, LEFT);
      add(jcbDJ, GROW_WRAP);
      add(jrbAmbience, LEFT);
      add(jcbAmbience, GROW_WRAP);
      add(jrbPlaylist, LEFT);
      add(jcbPlaylist, GROW_WRAP);
      add(jrbBestOf, LEFT_WRAP);
      add(jrbNovelties, LEFT_WRAP);
      add(jrbQueue, LEFT_WRAP);
      add(jrbBookmark, LEFT_WRAP);
      add(jrbShuffle, LEFT_WRAP);
      // store initial values, done here as well to have them stored if "next"
      // is pressed immediately
      // and there was no data stored before (an hence nothing was read in
      // readData())
      updateData();
    }

    /**
     * Initialize the UI items of the panel with values from the static data
     * object.
     */
    private void readData() {
      if (data.containsKey(KEY_MODE)) {
        // read values set before
        switch ((Mode) data.get(KEY_MODE)) {
        case DJ:
          bgActions.setSelected(jrbDJ.getModel(), true);
          jcbDJ.setSelectedItem(data.get(KEY_ITEM));
          break;
        case Ambience:
          bgActions.setSelected(jrbAmbience.getModel(), true);
          jcbAmbience.setSelectedItem(data.get(KEY_ITEM));
          break;
        case Playlist:
        case ProvidedPlaylist: // we did a "PrepareParty" from a Playlist
          // before, in this case show the Playlist again
          // here
          bgActions.setSelected(jrbPlaylist.getModel(), true);
          jcbPlaylist.setSelectedItem(data.get(KEY_ITEM));
          break;
        case Shuffle:
          bgActions.setSelected(jrbShuffle.getModel(), true);
          // no combo box for shuffle...
          break;
        case BestOf:
          bgActions.setSelected(jrbBestOf.getModel(), true);
          // no combo box for bestof...
          break;
        case Novelties:
          bgActions.setSelected(jrbNovelties.getModel(), true);
          // no combo box for novelties...
          break;
        case Queue:
          bgActions.setSelected(jrbQueue.getModel(), true);
          // no combo box for queue...
          break;
        case Bookmarks:
          bgActions.setSelected(jrbBookmark.getModel(), true);
          // no combo box for bookmarks...
          break;
        default:
          throw new IllegalArgumentException("Unexpected value in switch!");
        }
      } else {
        // no values set yet, select a useful radio button at least
        // select Ambience as default selection if there is no DJ available
        if (jcbDJ.getItemCount() == 0) {
          bgActions.setSelected(jrbAmbience.getModel(), true);
        } else {
          // otherwise select DJ as default option
          bgActions.setSelected(jrbDJ.getModel(), true);
        }
      }
      // finally disable some items if there is nothing in there
      if (jcbDJ.getItemCount() == 0) {
        jrbDJ.setEnabled(false);
        jcbDJ.setEnabled(false);
      }
      // disable Playlist UI if there is no Playlist-Mode already selected by
      // the incoming data...
      if (jcbPlaylist.getItemCount() == 0
          && !(Mode.Playlist.equals(data.get(KEY_MODE)) || Mode.ProvidedPlaylist.equals(data
              .get(KEY_MODE)))) {
        jrbPlaylist.setEnabled(false);
        jcbPlaylist.setEnabled(false);
      }
      // check if we have queue-entries or bookmarks
      if (QueueModel.getQueue().isEmpty()) {
        jrbQueue.setEnabled(false);
      }
      if (Bookmarks.getInstance().getFiles().isEmpty()) {
        jrbBookmark.setEnabled(false);
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      // Update all the values that are needed later
      updateData();
    }

    /**
     * Store the current values from the UI items into the static data object.
     */
    private void updateData() {
      // depending on the selected radio button read the combo box value and set
      // the selected MODE
      if (jrbDJ.isSelected()) {
        data.put(KEY_MODE, Mode.DJ);
        data.put(KEY_ITEM, jcbDJ.getSelectedItem());
      } else if (jrbAmbience.isSelected()) {
        data.put(KEY_MODE, Mode.Ambience);
        data.put(KEY_ITEM, jcbAmbience.getSelectedItem());
      } else if (jrbPlaylist.isSelected()) {
        data.put(KEY_MODE, Mode.Playlist);
        data.put(KEY_ITEM, jcbPlaylist.getSelectedItem());
      } else if (jrbShuffle.isSelected()) {
        data.put(KEY_MODE, Mode.Shuffle);
        data.remove(KEY_ITEM);
      } else if (jrbBestOf.isSelected()) {
        data.put(KEY_MODE, Mode.BestOf);
        data.remove(KEY_ITEM);
      } else if (jrbNovelties.isSelected()) {
        data.put(KEY_MODE, Mode.Novelties);
        data.remove(KEY_ITEM);
      } else if (jrbQueue.isSelected()) {
        data.put(KEY_MODE, Mode.Queue);
        data.remove(KEY_ITEM);
      } else if (jrbBookmark.isSelected()) {
        data.put(KEY_MODE, Mode.Bookmarks);
        data.remove(KEY_ITEM);
      }
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("PreparePartyWizard.3");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("PreparePartyWizard.2");
    }
  }

  /**
   * General options panel.
   */
  public static class GeneralOptionsPanel extends Screen implements ActionListener, ChangeListener,
      ClearPoint, MouseListener {
    /** Constant for MigLayout. */
    private static final String GROW = "grow";
    /** Constant for MigLayout. */
    private static final String GROW_TWO_COL = "[grow][]";
    /** Constant for MigLayout. */
    private static final String LABEL_WIDTH = "width 40:40:";
    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;
    /** Empty value. */
    private static final String NO_VALUE = " ";
    /** Enable limit on number of tracks. */
    private JCheckBox jcbMaxTracks;
    /** The max. number of tracks */
    private JSlider jsMaxTracks;
    /** The max. number of tracks */
    private JLabel jnMaxTracks;
    /** Enable limit on max size. */
    private JCheckBox jcbMaxSize;
    /** Max size (in MB) of party. */
    private JSlider jsMaxSize;
    /** Max size (in MB) of party. */
    private JLabel jnMaxSize;
    /** Enable limit on max playing length. */
    private JCheckBox jcbMaxLength;
    /** Max playing length of party (in minutes). */
    private JSlider jsMaxLength;
    /** Max playing length of party (in minutes). */
    private JLabel jnMaxLength;
    /** Enable limit on specific audio type. */
    private JCheckBox jcbOneMedia;
    /** Limit to one type of audo file. */
    private JComboBox jcbMedia;
    /** Enable conversion to the selected audio type. */
    private JCheckBox jcbConvertMedia;
    /** Audio conversion. */
    private JLabel jlConvertMedia;
    /** Button to configure audio conversion. */
    private JButton jbConvertConfig;
    /** Limit on rate of tracks. */
    private JLabel jlRatingLevel;
    /** The min. number of stars a track needs to have */
    private JSlider jsRatingLevel;
    /** Enable normalizing filenames so they can be stored on windows fileshares. */
    private JCheckBox jcbNormalizeFilename;

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("PreparePartyWizard.5");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("PreparePartyWizard.4");
    }

    /**
     * Create panel UI.
     */
    @Override
    public void initUI() {
      // workaround as the dialog is initialized before the constructor of
      // PreparePartyWizard fully executes
      restoreProperties();
      { // Max Tracks
        jcbMaxTracks = new JCheckBox(Messages.getString("PreparePartyWizard.10"));
        jcbMaxTracks.setToolTipText(Messages.getString("PreparePartyWizard.11"));
        jsMaxTracks = new JSlider(0, 10000, 100);
        jnMaxTracks = new JLabel(NO_VALUE);
        jnMaxTracks.setBorder(new BevelBorder(BevelBorder.LOWERED));
        jnMaxTracks.setHorizontalAlignment(SwingConstants.RIGHT);
        jsMaxTracks.setMajorTickSpacing(100);
        jsMaxTracks.setMinorTickSpacing(10);
        jsMaxTracks.setPaintTicks(false);
        jsMaxTracks.setPaintLabels(false);
        jsMaxTracks.setToolTipText(Messages.getString("PreparePartyWizard.11"));
      }
      { // Max Size
        jcbMaxSize = new JCheckBox(Messages.getString("PreparePartyWizard.12"));
        jcbMaxSize.setToolTipText(Messages.getString("PreparePartyWizard.13"));
        jsMaxSize = new JSlider(0, 10000, 100);
        jnMaxSize = new JLabel(NO_VALUE);
        jnMaxSize.setBorder(new BevelBorder(BevelBorder.LOWERED));
        jnMaxSize.setHorizontalAlignment(SwingConstants.RIGHT);
        jsMaxSize.setMajorTickSpacing(100);
        jsMaxSize.setMinorTickSpacing(10);
        jsMaxSize.setPaintTicks(false);
        jsMaxSize.setPaintLabels(false);
        jsMaxSize.setToolTipText(Messages.getString("PreparePartyWizard.13"));
      }
      { // Max Length
        jcbMaxLength = new JCheckBox(Messages.getString("PreparePartyWizard.14"));
        jcbMaxLength.setToolTipText(Messages.getString("PreparePartyWizard.15"));
        jsMaxLength = new JSlider(0, 10000, 100);
        jnMaxLength = new JLabel(NO_VALUE);
        jnMaxLength.setBorder(new BevelBorder(BevelBorder.LOWERED));
        jnMaxLength.setHorizontalAlignment(JLabel.RIGHT);
        jsMaxLength.setMajorTickSpacing(100);
        jsMaxLength.setMinorTickSpacing(10);
        jsMaxLength.setPaintTicks(false);
        jsMaxLength.setPaintLabels(false);
        jsMaxLength.setToolTipText(Messages.getString("PreparePartyWizard.15"));
      }
      { // Choose Media
        jcbOneMedia = new JCheckBox(Messages.getString("PreparePartyWizard.16"));
        jcbOneMedia.setToolTipText(Messages.getString("PreparePartyWizard.17"));
        jcbMedia = new JComboBox();
        List<Type> types = TypeManager.getInstance().getTypes();
        // sort the list on extension here
        Collections.sort(types, new TypeComparator());
        for (Type type : types) {
          // exclude playlists and web-radios from selection as we cannot copy
          // those.
          if (!type.getExtension().equals(Const.EXT_PLAYLIST)
              && !type.getExtension().equals(Const.EXT_RADIO)) {
            jcbMedia.addItem(type.getExtension());
          }
        }
        jcbMedia.setToolTipText(Messages.getString("PreparePartyWizard.17"));
        jcbConvertMedia = new JCheckBox(Messages.getString("PreparePartyWizard.34"));
        jcbConvertMedia.setToolTipText(Messages.getString("PreparePartyWizard.35"));
        // to show help and allow clicking for viewing the related web-page
        jlConvertMedia = new JLabel(Messages.getString("PreparePartyWizard.37"));
        jbConvertConfig = new JButton(Messages.getString("PreparePartyWizard.40"));
      }
      { // Rating Level
        jlRatingLevel = new JLabel(Messages.getString("DigitalDJWizard.8"));
        jlRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.53"));
        jsRatingLevel = new JSlider(0, 4, 0);
        jsRatingLevel.setMajorTickSpacing(1);
        jsRatingLevel.setMinorTickSpacing(1);
        jsRatingLevel.setPaintTicks(true);
        jsRatingLevel.setSnapToTicks(true);
        jsRatingLevel.setPaintLabels(true);
        jsRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.53"));
      }
      jcbNormalizeFilename = new JCheckBox(Messages.getString("PreparePartyWizard.26"));
      jcbNormalizeFilename.setToolTipText(Messages.getString("PreparePartyWizard.27"));
      // populate the UI items with values from the static data object
      readData();
      // add listeners after reading initial data to not overwrite them with
      // init-state-change actions
      // enable/disable slider depending on checkbox
      jcbMaxTracks.addActionListener(this);
      jsMaxTracks.addMouseWheelListener(new DefaultMouseWheelListener(jsMaxTracks));
      jsMaxTracks.addChangeListener(this);
      // enable/disable slider depending on checkbox
      jcbMaxSize.addActionListener(this);
      jsMaxSize.addMouseWheelListener(new DefaultMouseWheelListener(jsMaxSize));
      jsMaxSize.addChangeListener(this);
      // enable/disable slider depending on checkbox
      jcbMaxLength.addActionListener(this);
      jsMaxLength.addMouseWheelListener(new DefaultMouseWheelListener(jsMaxLength));
      jsMaxLength.addChangeListener(this);
      // enable/disable combobox depending on checkbox
      jcbOneMedia.addActionListener(this);
      jcbMedia.addActionListener(this);
      jcbConvertMedia.addActionListener(this);
      jlConvertMedia.addMouseListener(this);
      jbConvertConfig.addActionListener(this);
      // get informed about rating level slider changes
      jsRatingLevel.addMouseWheelListener(new DefaultMouseWheelListener(jsRatingLevel));
      jsRatingLevel.addChangeListener(this);
      jcbNormalizeFilename.addActionListener(this);
      setLayout(new MigLayout("insets 10,gapx 10,gapy 15", "[][grow]"));
      add(jcbMaxTracks);
      {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", GROW_TWO_COL));
        panel.add(jsMaxTracks, GROW);
        panel.add(jnMaxTracks, LABEL_WIDTH);
        add(panel, GROW_WRAP);
      }
      add(jcbMaxSize);
      {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", GROW_TWO_COL));
        panel.add(jsMaxSize, GROW);
        panel.add(jnMaxSize, LABEL_WIDTH);
        add(panel, GROW_WRAP);
      }
      add(jcbMaxLength);
      {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", GROW_TWO_COL));
        panel.add(jsMaxLength, GROW);
        panel.add(jnMaxLength, LABEL_WIDTH);
        add(panel, GROW_WRAP);
      }
      add(jcbOneMedia);
      add(jcbMedia, GROW_WRAP);
      // dummy-Label to get the CheckBox for "convert" into the second column
      add(new JLabel());
      add(jcbConvertMedia, GROW_WRAP);
      add(new JLabel());
      {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", GROW_TWO_COL));
        panel.add(jlConvertMedia, GROW);
        panel.add(jbConvertConfig);
        add(panel, GROW_WRAP);
      }
      add(jcbNormalizeFilename, GROW_WRAP);
      add(jlRatingLevel);
      add(jsRatingLevel, GROW_WRAP);
      // store initial values and adjust values
      updateData();
    }

    /**
     * Populate the UI items with values from the static data object.
     */
    private void readData() {
      // set the values from the stored data
      // initially these are not set, so we need to query for "containsKey"...
      if (isTrue(KEY_MAX_TRACKS_ON)) {
        jsMaxTracks.setEnabled(true);
        jcbMaxTracks.setSelected(true);
      } else {
        jsMaxTracks.setEnabled(false);
        jcbMaxTracks.setSelected(false);
      }
      if (data.containsKey(KEY_MAX_TRACKS)) {
        jsMaxTracks.setValue((Integer) data.get(KEY_MAX_TRACKS));
      }
      if (isTrue(KEY_MAX_SIZE_ON)) {
        jsMaxSize.setEnabled(true);
        jcbMaxSize.setSelected(true);
      } else {
        jsMaxSize.setEnabled(false);
        jcbMaxSize.setSelected(false);
      }
      if (data.containsKey(KEY_MAX_SIZE)) {
        jsMaxSize.setValue((Integer) data.get(KEY_MAX_SIZE));
      }
      if (isTrue(KEY_MAX_LENGTH_ON)) {
        jsMaxLength.setEnabled(true);
        jcbMaxLength.setSelected(true);
      } else {
        jsMaxLength.setEnabled(false);
        jcbMaxLength.setSelected(false);
      }
      if (data.containsKey(KEY_MAX_LENGTH)) {
        jsMaxLength.setValue((Integer) data.get(KEY_MAX_LENGTH));
      }
      if (isTrue(KEY_ONE_MEDIA_ON)) {
        jcbMedia.setEnabled(true);
        jcbOneMedia.setSelected(true);
        jcbConvertMedia.setEnabled(true);
      } else {
        jcbMedia.setEnabled(false);
        jcbOneMedia.setSelected(false);
        jcbConvertMedia.setEnabled(false);
      }
      // disable media conversion if pacpl is not found
      if (!bPACPLAvailable) {
        jcbConvertMedia.setEnabled(false);
      }
      // don't set Convert to on from data if PACPL became unavailable
      if (isTrue(KEY_CONVERT_MEDIA) && bPACPLAvailable) {
        jcbConvertMedia.setSelected(true);
      } else {
        jcbConvertMedia.setSelected(false);
      }
      if (data.containsKey(KEY_MEDIA)) {
        jcbMedia.setSelectedItem(data.get(KEY_MEDIA));
      } else {
        // default to MP3 initially
        jcbMedia.setSelectedItem("mp3");
      }
      if (data.containsKey(KEY_RATINGS_LEVEL)) {
        jsRatingLevel.setValue((Integer) data.get(KEY_RATINGS_LEVEL));
      }
      if (isTrue(KEY_NORMALIZE_FILENAME_ON)) {
        jcbNormalizeFilename.setSelected(true);
      } else {
        jcbNormalizeFilename.setSelected(false);
      }
    }

    /**
     * Write the data from the UI items to the static data object.
     */
    private void updateData() {
      // store if checkbox is enabled and update the label accordingly
      updateOneItem(jcbMaxTracks, jsMaxTracks, jnMaxTracks, KEY_MAX_TRACKS, KEY_MAX_TRACKS_ON);
      updateOneItem(jcbMaxSize, jsMaxSize, jnMaxSize, KEY_MAX_SIZE, KEY_MAX_SIZE_ON);
      updateOneItem(jcbMaxLength, jsMaxLength, jnMaxLength, KEY_MAX_LENGTH, KEY_MAX_LENGTH_ON);
      if (jcbOneMedia.isSelected()) {
        data.put(KEY_MEDIA, jcbMedia.getSelectedItem());
        data.put(KEY_ONE_MEDIA_ON, Boolean.TRUE);
      } else {
        // keep old value... data.remove(KEY_MEDIA);
        data.put(KEY_ONE_MEDIA_ON, Boolean.FALSE);
      }
      data.put(KEY_CONVERT_MEDIA, jcbConvertMedia.isSelected());
      data.put(KEY_RATINGS_LEVEL, jsRatingLevel.getValue());
      data.put(KEY_NORMALIZE_FILENAME_ON, jcbNormalizeFilename.isSelected());
    }

    /**
     * Helper to handle a checkbox/slider combination. It also updates an
     * associated Label with the value from the Slider.
     * 
     * @param cb The checkbox to check for selected/deselected state
     * @param slider The slider to get the value from
     * @param label The Label to populate with the current value from the Slider.
     * @param key The key in the static data object for the value of the Slider.
     * @param keyOn The key in the static data object to store the enabled/disabled
     * state.
     */
    private void updateOneItem(JCheckBox cb, JSlider slider, JLabel label, String key, String keyOn) {
      if (cb.isSelected()) {
        if (!slider.getValueIsAdjusting()) {
          data.put(key, slider.getValue());
          data.put(keyOn, Boolean.TRUE);
        }
        label.setText(Integer.toString(slider.getValue()));
      } else {
        if (!slider.getValueIsAdjusting()) {
          // keep value... data.remove(key);
          data.put(keyOn, Boolean.FALSE);
        }
        label.setText(NO_VALUE);
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
      // if a checkbox is selected/deselected, enable/disable the
      // sliders/comboboxes accordingly
      if (ae.getSource() == jcbMaxTracks) {
        jsMaxTracks.setEnabled(jcbMaxTracks.isSelected());
      } else if (ae.getSource() == jcbMaxSize) {
        jsMaxSize.setEnabled(jcbMaxSize.isSelected());
      } else if (ae.getSource() == jcbMaxLength) {
        jsMaxLength.setEnabled(jcbMaxLength.isSelected());
      } else if (ae.getSource() == jcbOneMedia) {
        jcbMedia.setEnabled(jcbOneMedia.isSelected());
        jcbConvertMedia.setEnabled(jcbOneMedia.isSelected());
      } else if (ae.getSource() == jbConvertConfig) {
        // create the settings dialog, it will display itself and inform us when
        // the value is changed with "Ok"
        new PreparePartyConvertSettings(new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            // no need for re-checking if the same command is chosen as before
            if (e.getSource().toString().equals(data.get(KEY_CONVERT_COMMAND))) {
              Log.debug("Same pacpl-command as before: " + e.getSource().toString());
              return;
            }
            Log.debug("New pacpl-command: " + e.getSource().toString());
            data.put(KEY_CONVERT_COMMAND, e.getSource().toString());
            // re-check if pacpl can be called now
            bPACPLAvailable = UtilPrepareParty.checkPACPL((String) data.get(KEY_CONVERT_COMMAND));
            // disable media conversion if pacpl is not found
            if (bPACPLAvailable) {
              Log.debug("Updated settings for media conversion allow pacpl to be used.");
              jcbConvertMedia.setEnabled(true);
            } else {
              Log.warn("Updated settings for media conversion do not allow pacpl to be used!");
              jcbConvertMedia.setEnabled(false);
              jcbConvertMedia.setSelected(false);
            }
          }
        }, (String) data.get(KEY_CONVERT_COMMAND), JajukMainWindow.getInstance());
      }
      updateData();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
     * )
     */
    @Override
    public void stateChanged(ChangeEvent ie) {
      // just update the stored static data whenever we receive an interesting
      // event
      if (ie.getSource() == jsMaxTracks) {
        updateData();
      } else if (ie.getSource() == jsMaxSize) {
        updateData();
      } else if (ie.getSource() == jsMaxLength) {
        updateData();
      } else if (ie.getSource() == jcbMedia) {
        updateData();
      } else if (ie.getSource() == jsRatingLevel) {
        updateData();
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {
      if (e.getSource() == jlConvertMedia) {
        try {
          Desktop.getDesktop().browse(
              new URI("http://jajuk.info/index.php/Installing_Perl_Audio_Converter"));
        } catch (IOException ex) {
          Log.error(ex);
        } catch (URISyntaxException ex) {
          Log.error(ex);
        }
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent e) {
      // nothing to do here...
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent e) {
      // nothing to do here...
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
      // nothing to do here...
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
      // nothing to do here...
    }
  }

  /**
   * Panel for selecting the location in the filesystem.
   */
  public static class PathSelectionPanel extends Screen implements ActionListener {
    /** Generated serialVersionUID. */
    private static final long serialVersionUID = -236180699495019177L;
    /** Button for file chooser dialog. */
    JButton jbFileSelection;
    /** The selected file. */
    JLabel jlSelectedFile;
    /** Selected directory. */
    private File fDir;

    /*
     * (non-Javadoc)
     * 
     * @see org.qdwizard.Screen#initUI()
     */
    @Override
    public void initUI() {
      JLabel jlFileSelection = new JLabel(Messages.getString("PreparePartyWizard.20"));
      jbFileSelection = new JButton(IconLoader.getIcon(JajukIcons.OPEN_DIR));
      jbFileSelection.addActionListener(this);
      JLabel jlSelectedFileText = new JLabel(Messages.getString("PreparePartyWizard.21"));
      jlSelectedFile = new JLabel(Messages.getString("FirstTimeWizard.9"));
      jlSelectedFile.setBorder(new BevelBorder(BevelBorder.LOWERED));
      // previous value if available
      if (data.containsKey(KEY_DEST_PATH)) {
        jlSelectedFile.setText((String) data.get(KEY_DEST_PATH));
        // we also can finish the dialog
        setCanFinish(true);
      } else {
        setProblem(Messages.getString("PreparePartyWizard.22"));
        // now we can not finish the dialog
        setCanFinish(false);
      }
      // Add items
      setLayout(new MigLayout("insets 10,gapx 10,gapy 15", "[][grow]"));
      add(jlFileSelection);
      add(jbFileSelection, "wrap,center");
      add(jlSelectedFileText);
      add(jlSelectedFile, "grow,wrap");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      // display a FileChooser
      if (e.getSource() == jbFileSelection) {
        // TODO: for some reason the passing of the existing directory does not
        // work here, seems the implementation in JajukFileChooser does not do
        // this correctly
        final JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(
            DirectoryFilter.getInstance()), fDir);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setDialogTitle(Messages.getString("PreparePartyWizard.22"));
        jfc.setMultiSelectionEnabled(false);
        final int returnVal = jfc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          // retrieve selected directory and update it in all necessary places
          fDir = jfc.getSelectedFile();
          jlSelectedFile.setText(fDir.getAbsolutePath());
          data.put(KEY_DEST_PATH, fDir.getAbsolutePath());
          // we can finish the wizard now
          setProblem(null);
          // now we can finish the dialog
          setCanFinish(true);
        }
      }
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getDescription()
     */
    @Override
    public String getDescription() {
      return Messages.getString("PreparePartyWizard.19");
    }

    /* (non-Javadoc)
     * @see org.qdwizard.Screen#getName()
     */
    @Override
    public String getName() {
      return Messages.getString("PreparePartyWizard.18");
    }
  }

  /**
   * Compare two types.
   */
  private static final class TypeComparator implements Comparator<Type> {
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Type o1, Type o2) {
      // handle null, always equal
      if (o1 == null || o2 == null) {
        return 0;
      }
      // otherwise sort on extension here
      return o1.getExtension().compareTo(o2.getExtension());
    }
  }
}
