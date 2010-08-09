/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
package org.jajuk.services.startup;

import org.jajuk.base.AlbumManager;
import org.jajuk.base.Collection;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.ItemManager;
import org.jajuk.services.alarm.AlarmManager;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.core.RatingManager;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.dbus.DBusManager;
import org.jajuk.services.lastfm.LastFmManager;
import org.jajuk.services.players.QueueController;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.ui.thumbnails.ThumbnailsMaker;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Startup facilities of asynchronous tasks
 * <p>Called after collection loading<p>.
 */
public class StartupAsyncService {

  /**
   * Instantiates a new startup async service.
   */
  private StartupAsyncService() {
    // private constructor to hide it from the outside
  }

  /**
   * Asynchronous tasks executed at startup at the same time (for perf).
   * 
   * @param bCollectionLoadRecover DOCUMENT_ME
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
            try {
              DBusManager.connect();
            } catch (Exception e) {
              // Make sure to catch this error propertly, otherwise the rest of the initalization is
              // not done
              Log.error(e);
            }
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

          // Submit any LastFM submission cache
          if (Conf.getBoolean(Const.CONF_LASTFM_ENABLE)) {
            LastFmManager.getInstance().submitCache();
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
