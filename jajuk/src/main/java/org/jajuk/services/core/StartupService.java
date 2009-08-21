/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.services.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.jajuk.base.AlbumManager;
import org.jajuk.base.Collection;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.FileManager;
import org.jajuk.base.ItemManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.alarm.AlarmManager;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.dbus.DBusManager;
import org.jajuk.services.players.QueueController;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.ui.thumbnails.ThumbnailsMaker;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Startup facilities
 */
public class StartupService {

  private StartupService() {
    // private constructor to hide it from the outside
  }

  /**
   * Launch initial track at startup
   */
  public static void launchInitialTrack() {
    // List of items to play at startup
    List<org.jajuk.base.File> alToPlay = new ArrayList<org.jajuk.base.File>();
    // File to play
    org.jajuk.base.File fileToPlay = null;
    if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_LAST)
        || Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_LAST_KEEP_POS)
        || Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_FILE)
        || Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_NOTHING)) {
      if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_FILE)) {
        fileToPlay = FileManager.getInstance().getFileByID(Conf.getString(Const.CONF_STARTUP_FILE));
      } else {
        // If we were playing a webradio when leaving, launch it
        if (Conf.getBoolean(Const.CONF_WEBRADIO_WAS_PLAYING)) {
          final WebRadio radio = WebRadioManager.getInstance().getWebRadioByName(
              Conf.getString(Const.CONF_DEFAULT_WEB_RADIO));
          if (radio != null) {
            new Thread("WebRadio launch thread") {
              @Override
              public void run() {
                QueueModel.launchRadio(radio);
              }
            }.start();
          }
          return;
        }
        // last file from beginning or last file keep position
        else if (History.getInstance().getHistory().size() > 0) {
          // make sure user didn't exit jajuk in the stopped state
          // and that history is not void
          fileToPlay = FileManager.getInstance().getFileByID(History.getInstance().getLastFile());
        } else {
          // do not try to launch anything, stay in stop state
          return;
        }
      }
      // Try to mount the file to play
      if (fileToPlay != null) {
        if (!fileToPlay.isReady()) {
          // file exists but is not mounted, just notify the error
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
            QueueModel.setFirstFile(false); // no more first file
          }
        }
      } else {
        // file no more exists
        Messages.getChoice(Messages.getErrorMessage(23), JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE);
        QueueModel.setFirstFile(false);
        // no more first file, we ignore any stored fifo as it may contains
        // others disappeared files
        return;
      }
      // For last tracks playing, add all ready files from last
      // session stored FIFO
      final File fifo = SessionService.getConfFileByPath(Const.FILE_FIFO);
      if (!fifo.exists()) {
        Log.debug("No fifo file");
      } else {
        try {
          final BufferedReader br = new BufferedReader(new FileReader(SessionService
              .getConfFileByPath(Const.FILE_FIFO)));
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
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_SHUFFLE)) {
      alToPlay = FileManager.getInstance().getGlobalShufflePlaylist();
      if (alToPlay.size() > 0) {
        fileToPlay = alToPlay.get(0);
      }
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_BESTOF)) {
      alToPlay = FileManager.getInstance().getGlobalBestofPlaylist();
      if (alToPlay.size() > 0) {
        fileToPlay = alToPlay.get(0);
      }
    } else if (Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_NOVELTIES)) {
      alToPlay = FileManager.getInstance().getGlobalNoveltiesPlaylist();
      if ((alToPlay != null) && (alToPlay.size() > 0)) {
        // shuffle the selection
        Collections.shuffle(alToPlay, UtilSystem.getRandom());
        fileToPlay = alToPlay.get(0);
      } else {
        // Alert user that no novelties have been found
        InformationJPanel.getInstance().setMessage(Messages.getString("Error.127"),
            InformationJPanel.ERROR);
      }
    }
    // Launch selected file

    // If the queue was empty and a file to play is provided, build a new queue
    // with this track alone
    if (alToPlay != null && alToPlay.size() == 0 && fileToPlay != null) {
      alToPlay.add(fileToPlay);
    }

    // find the index of last played track
    if (alToPlay != null && alToPlay.size() > 0 && fileToPlay != null) {
      int index = -1;
      for (int i = 0; i < alToPlay.size(); i++) {
        if (fileToPlay.getID().equals(alToPlay.get(i).getID())) {
          index = i;
          break;
        }
      }

      if (index == -1) {
        if (fileToPlay != null) {
          // Track not stored, push it first
          alToPlay.add(0, fileToPlay);
        }
        index = 0;
      }

      QueueModel.insert(UtilFeatures.createStackItems(alToPlay, false, false), 0);
      QueueModel.setIndex(index);

      if (Conf.getBoolean(Const.CONF_STATE_REPEAT)) {
        if (index < QueueModel.getQueueSize()) {
          QueueModel.getItem(index).setRepeat(true);
        }
      }

      if (Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL)) {
        QueueModel.setRepeatModeToAll(true);
      }

      // do not start playing if do nothing at startup is selected
      if (!Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_NOTHING)) {
        QueueModel.goTo(index);
      }
    }
  }

  /**
   * Asynchronous tasks executed at startup at the same time (for perf)
   */
  public static void startupAsyncAfterCollectionLoad(final boolean bCollectionLoadRecover) {
    Thread startup = new Thread("Startup Async After Collection Load Thread") {
      @Override
      public void run() {
        try {

          // start exit hook
          final ExitService exit = new ExitService();
          exit.setPriority(Thread.MAX_PRIORITY);
          Runtime.getRuntime().addShutdownHook(exit);

          // backup the collection if no parsing error occurred
          if (!bCollectionLoadRecover) {
            UtilSystem.backupFile(SessionService.getConfFileByPath(Const.FILE_COLLECTION), Conf
                .getInt(Const.CONF_BACKUP_SIZE));
          }

          // Register FIFO manager
          QueueController.getInstance();

          // Refresh max album rating
          AlbumManager.getInstance().refreshMaxRating();

          // Sort albums cache. We do it before the sleep because there's a
          // chance that user launch an album as soon as the GUI is painted
          AlbumManager.getInstance().orderCache();

          // Force Thumbnail manager to check for thumbs presence. Must be done
          // before catalog view refresh to avoid useless thumbs creation
          for (int size = 50; size <= 300; size += 50) {
            ThumbnailManager.populateCache(size);
          }

          // try to start up D-Bus support if available. Currently this is only
          // implemented on Linux
          if (UtilSystem.isUnderLinux()) {
            // make sure the singleton is initialized here
            DBusManager.getInstance();
          }

          // Wait few secs to avoid GUI startup perturbations
          Thread.sleep(10000);

          // Switch to sorted mode, must be done before starting auto-refresh
          // thread !
          ItemManager.switchAllManagersToOrderState();

          // Clear covers images cache
          SessionService.clearCache();

          // Launch auto-refresh thread
          DeviceManager.getInstance().startAutoRefreshThread();

          // Start rating manager thread
          RatingManager.getInstance().start();

          // Start alarm clock
          if (Conf.getBoolean(Const.CONF_ALARM_ENABLED)) {
            AlarmManager.getInstance();
          }

          // Force rebuilding thumbs (after an album id hashcode
          // method change for eg)
          if (Collection.getInstance().getWrongRightAlbumIDs().size() > 0) {
            // Launch thumbs creation in another process
            ThumbnailsMaker.launchAllSizes(true);
          }
        } catch (final Exception e) {
          Log.error(e);
        }
      }
    };
    startup.setPriority(Thread.MIN_PRIORITY);
    startup.start();
  }

}
