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
package org.jajuk.services.startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Startup facilities for sound engine.
 */
public final class StartupEngineService {
  /** List of items to play at startup. */
  private static List<org.jajuk.base.File> alToPlay = new ArrayList<org.jajuk.base.File>();
  /** File to play. */
  private static org.jajuk.base.File fileToPlay;
  /** Web radio to play. */
  private static WebRadio radio;
  /** Index in the queue of the startup file. */
  private static int index = -1;

  /**
   * Instantiates a new startup engine service.
   */
  private StartupEngineService() {
    // private constructor to hide it from the outside
  }

  /**
   * Launch initial track at startup.
   */
  public static void launchInitialTrack() {
    try {
      String startupMode = Conf.getString(Const.CONF_STARTUP_MODE);
      final File fifo = SessionService.getConfFileByPath(Const.FILE_FIFO);
      // Restore if required
      UtilSystem.recoverFileIfRequired(fifo);
      // User explicitly required nothing to start or he left jajuk stopped
      boolean doNotStartAnything = Const.STARTUP_MODE_NOTHING.equals(startupMode)
          || Conf.getBoolean(Const.CONF_STARTUP_STOPPED)
          //  CONF_STARTUP_ITEM is void at first jajuk session and until user launched an item
          || (Const.STARTUP_MODE_ITEM.equals(startupMode) && StringUtils.isBlank(Conf
              .getString(Const.CONF_STARTUP_ITEM)))
          // Void collection
          || FileManager.getInstance().getElementCount() == 0
          // FIFO void or not exists
          || (!fifo.exists() || fifo.length() == 0);
      // Populate item to be started and load the stored queue
      populateStartupItems();
      // Check that the file to play is not null and try to mount its device if required 
      if (!doNotStartAnything && !isWebradioStartup()) {
        checkFileToPlay();
      }
      // Set the index of the file to play within the queue.
      // We still need to compute index of file to play even if we play nothing or a radio
      // because user may do a "play" and the next file index must be ready.
      // However, we don't need to test file availability with checkFileToPlay() method.
      updateIndex();
      // Push the new queue
      boolean bRepeat = Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL)
          || Conf.getBoolean(Const.CONF_STATE_REPEAT);
      QueueModel.insert(UtilFeatures.createStackItems(alToPlay, bRepeat, false), 0);
      // Force queue index because insert increase it so it would be set to queue size after the insert
      QueueModel.setIndex(index);
      // Start the file or the radio
      // If user leaved jajuk in stopped mode, do nothing
      if (!doNotStartAnything && isWebradioStartup() && radio != null) {
        launchRadio();
      } else if (!doNotStartAnything && fileToPlay != null) {
        launchFile();
      }
    } catch (Exception e) {
      Log.error(e);
      Messages.getChoice(Messages.getErrorMessage(23), JOptionPane.DEFAULT_OPTION,
          JOptionPane.WARNING_MESSAGE);
    }
  }

  /**
   * Launch fileToPlay.
   */
  private static void launchFile() {
    new Thread("Track Startup Thread") {
      @Override
      public void run() {
        QueueModel.goTo(index);
      }
    }.start();
  }

  /**
   * Return whether a webradio startup is required (it may be null if the webradio is unknown by the collection).
   * 
   * @return whether a webradio startup is required
   */
  private static boolean isWebradioStartup() {
    String startupMode = Conf.getString(Const.CONF_STARTUP_MODE);
    if (Conf.getBoolean(Const.CONF_WEBRADIO_WAS_PLAYING)
        && (Const.STARTUP_MODE_LAST_KEEP_POS.equals(startupMode) || Const.STARTUP_MODE_LAST
            .equals(startupMode))) {
      return true;
    }
    if (Const.STARTUP_MODE_ITEM.equals(startupMode)) {
      String conf = Conf.getString(Const.CONF_STARTUP_ITEM);
      if (conf.matches(SearchResultType.WEBRADIO.name() + ".*")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Restore the queue we got at last session exit.
   */
  private static void restoreQueue() {
    final File fifo = SessionService.getConfFileByPath(Const.FILE_FIFO);
    try {
      UtilSystem.recoverFileIfRequired(fifo);
    } catch (IOException e) {
      Log.error(e);
    }
    if (!fifo.exists()) {
      Log.debug("No fifo file");
    } else {
      try {
        final BufferedReader br = new BufferedReader(new FileReader(
            SessionService.getConfFileByPath(Const.FILE_FIFO)));
        try {
          String s = null;
          for (;;) {
            s = br.readLine();
            if (s == null) {
              break;
            }
            final org.jajuk.base.File file = FileManager.getInstance().getFileByID(s);
            if ((file != null) && file.isReady()) {
              alToPlay.add(file);
            }
          }
        } finally {
          br.close();
        }
      } catch (final IOException ioe) {
        Log.error(ioe);
      }
    }
  }

  /**
   * Find item to play and build the queue.
   */
  private static void populateStartupItems() {
    String startupMode = Conf.getString(Const.CONF_STARTUP_MODE);
    Ambience ambience = AmbienceManager.getInstance().getSelectedAmbience();
    // an item (track or radio) has been forced by user
    if (Const.STARTUP_MODE_ITEM.equals(startupMode)) {
      String conf = Conf.getString(Const.CONF_STARTUP_ITEM);
      String item = conf.substring(conf.indexOf('/') + 1, conf.length());
      if (conf.matches(SearchResultType.FILE.name() + ".*")) {
        fileToPlay = FileManager.getInstance().getFileByID(item);
        if (fileToPlay == null) {
          Log.warn("Unknown startup file : " + fileToPlay.getAbsolutePath());
        }
      } else if (conf.matches(SearchResultType.WEBRADIO.name() + ".*")) {
        radio = WebRadioManager.getInstance().getWebRadioByName(item);
        if (radio == null) {
          Log.warn("Unknown startup webradio : " + radio.getName());
        }
      }
    }
    // We play last item
    else if (Const.STARTUP_MODE_LAST.equals(startupMode)
        || Const.STARTUP_MODE_LAST_KEEP_POS.equals(startupMode)) {
      //Restore the queue in these cases
      restoreQueue();
      // If we were playing a webradio when leaving, launch it
      if (Conf.getBoolean(Const.CONF_WEBRADIO_WAS_PLAYING)) {
        radio = WebRadioManager.getInstance().getWebRadioByName(
            Conf.getString(Const.CONF_DEFAULT_WEB_RADIO));
      }
      // last file from beginning or last file keep position
      else {
        index = Conf.getInt(Const.CONF_STARTUP_QUEUE_INDEX);
        if (index >= 0 && index < alToPlay.size()) {
          fileToPlay = alToPlay.get(index);
        }
      }
    } else if (Const.STARTUP_MODE_NOTHING.equals(startupMode)) {
      //Restore the queue in these cases
      restoreQueue();
    }
    // Shuffle mode
    else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_SHUFFLE)) {
      // Filter files by ambience or if none ambience matches, perform a global shuffle 
      // ignoring current ambience
      alToPlay = UtilFeatures.filterByAmbience(
          FileManager.getInstance().getGlobalShufflePlaylist(), ambience);
      if (alToPlay.size() == 0) {
        alToPlay = FileManager.getInstance().getGlobalShufflePlaylist();
      }
      if (alToPlay != null && alToPlay.size() > 0) {
        fileToPlay = alToPlay.get(0);
      }
      // Best of mode
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_BESTOF)) {
      // Filter files by ambience or if none ambience matches, perform a global best-of selection 
      // ignoring current ambience
      alToPlay = UtilFeatures.filterByAmbience(FileManager.getInstance().getGlobalBestofPlaylist(),
          ambience);
      if (alToPlay.size() == 0) {
        alToPlay = FileManager.getInstance().getGlobalBestofPlaylist();
      }
      if (alToPlay != null && alToPlay.size() > 0) {
        fileToPlay = alToPlay.get(0);
      }
      // Novelties mode
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_NOVELTIES)) {
      // Filter files by ambience or if none ambience matches, perform a global novelties selection 
      // ignoring current ambience
      alToPlay = UtilFeatures.filterByAmbience(FileManager.getInstance()
          .getGlobalNoveltiesPlaylist(), ambience);
      if (alToPlay.size() == 0) {
        alToPlay = FileManager.getInstance().getGlobalNoveltiesPlaylist();
      }
      if (alToPlay != null && alToPlay.size() > 0) {
        // shuffle the selection
        Collections.shuffle(alToPlay, UtilSystem.getRandom());
        fileToPlay = alToPlay.get(0);
      } else {
        // Alert user that no novelties have been found
        InformationJPanel.getInstance().setMessage(Messages.getString("Error.127"),
            InformationJPanel.MessageType.ERROR);
      }
    }
    // If the queue was empty and a file to play is provided, build a new queue
    // with this track alone
    if (alToPlay.size() == 0 && fileToPlay != null) {
      alToPlay.add(fileToPlay);
    }
  }

  /**
   * Check that the file can actually be played and handle errors if it's not the case.
   * 
   * @return whether the file can actually be played
   */
  private static boolean checkFileToPlay() {
    boolean result = true;
    // Try to mount the file to play
    if (fileToPlay != null) {
      if (!fileToPlay.isReady()) {
        // File exists but is not mounted, just notify the error
        // without annoying dialog at each startup try to mount
        // device
        Log.debug("Startup file located on an unmounted device" + ", try to mount it");
        try {
          fileToPlay.getDevice().mount(false);
          Log.debug("Mount OK");
        } catch (final Exception e) {
          Log.debug("Mount failed");
          final Properties pDetail = new Properties();
          pDetail.put(Const.DETAIL_CONTENT, fileToPlay);
          pDetail.put(Const.DETAIL_REASON, "10");
          ObservationManager.notify(new JajukEvent(JajukEvents.PLAY_ERROR, pDetail));
          result = false;
        }
      }
    } else {
      // file no more exists
      Messages.getChoice(Messages.getErrorMessage(23), JOptionPane.DEFAULT_OPTION,
          JOptionPane.WARNING_MESSAGE);
      // no more first file, we ignore any stored fifo as it may contains
      // others disappeared files
      result = false;
    }
    return result;
  }

  /**
   * Set index of fileToPlay among alToPlay list.
   */
  private static void updateIndex() {
    // keep index from configuration if already set
    if (index != -1) {
      return;
    }
    // fileToPlay is null if nothing has to be played, then we keep default index value (0)
    if (fileToPlay != null) {
      // find the index of last played track
      index = -1;
      for (int i = 0; i < alToPlay.size(); i++) {
        if (fileToPlay.getID().equals(alToPlay.get(i).getID())) {
          index = i;
          break;
        }
      }
      if (index == -1) {
        // Track not stored, push it first
        alToPlay.add(0, fileToPlay);
        index = 0;
      }
    }
  }

  /**
   * Launch the startup webradio.
   */
  private static void launchRadio() {
    new Thread("WebRadio launch thread") {
      @Override
      public void run() {
        QueueModel.launchRadio(radio);
      }
    }.start();
  }

  /**
   * Auto-Mount required devices.
   */
  public static void autoMount() {
    for (final Device device : DeviceManager.getInstance().getDevices()) {
      if (device.getBooleanValue(Const.XML_DEVICE_AUTO_MOUNT)) {
        try {
          device.mount(false);
        } catch (final Exception e) {
          Log.error(112, device.getName(), e);
          continue;
        }
      }
    }
  }
}
