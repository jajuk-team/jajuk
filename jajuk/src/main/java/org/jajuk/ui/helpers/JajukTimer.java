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
 *  
 */
package org.jajuk.ui.helpers;

import java.util.Iterator;
import java.util.List;

import org.jajuk.base.File;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.StackItem;

/**
 * This class is a convenient class to sum up the remaining playing time of tracks, mainly for UI
 * <p>
 * Singleton
 * </p>.
 */
public final class JajukTimer {
  /** Self instance. */
  private static JajukTimer timer = new JajukTimer();
  /** Total time to play in secs. */
  private long lTimeToPlay = 0;
  /** A default heartbeat time in ms. */
  public static final int DEFAULT_HEARTBEAT = 800;
  /** The heartbeat for the Track Position Slider Toolbar. */
  public static final int D_MS_HEARTBEAT = 500;

  /**
   * Gets the instance.
   * 
   * @return JajukTimer singleton
   */
  public static JajukTimer getInstance() {
    return timer;
  }

  /**
   * Private constructor.
   */
  private JajukTimer() {
  }

  /**
   * Add time of the given file.
   * 
   * @param file The file to read the duration from.
   */
  public void addTrackTime(File file) {
    if (file != null) {
      lTimeToPlay += file.getTrack().getDuration();
    }
  }

  /**
   * Add time of the given set of files.
   * 
   * @param alFiles The list of StackItems to get the duration from the contained files.
   */
  public void addTrackTime(java.util.List<StackItem> alFiles) {
    Iterator<StackItem> it = alFiles.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      // instanceof also checks for null by definition
      if (o instanceof File) {
        addTrackTime((File) o);
      } else if (o != null) {
        File file = ((StackItem) o).getFile();
        addTrackTime(file);
      }
    }
  }

  /**
   * Remove time of the given file.
   * 
   * @param file The file to read the duration to remove from overall playing time.
   */
  public void removeTrackTime(File file) {
    if (file != null) {
      lTimeToPlay -= file.getTrack().getDuration();
    }
  }

  /**
   * Remove time of the given set of files.
   * 
   * @param alFiles The list of files which duration to remove from the overall playing time.
   */
  public void removeTrackTime(List<File> alFiles) {
    Iterator<File> it = alFiles.iterator();
    while (it.hasNext()) {
      File file = it.next();
      if (file != null) {
        removeTrackTime(it.next());
      }
    }
  }

  /**
   * Gets the current track elapsed time.
   * 
   * @return Current track elapsed time in secs
   */
  public long getCurrentTrackEllapsedTime() {
    return Player.getElapsedTimeMillis() / 1000;
  }

  /**
   * Gets the current track total time.
   * 
   * @return Current track total time in secs
   */
  public long getCurrentTrackTotalTime() {
    return Player.getDurationSec() / 1000;
  }

  /**
   * Gets the total time to play.
   * 
   * @return FIFO total time to be played in secs ( includes current track time
   * to play). Returns -1 if repeat mode
   */
  public long getTotalTimeToPlay() {
    return lTimeToPlay - getCurrentTrackEllapsedTime();
    // total time to play equals total time to play -
    // current track elapsed time
  }

  /**
   * Reset timer.
   */
  public void reset() {
    lTimeToPlay = 0;
  }
}
