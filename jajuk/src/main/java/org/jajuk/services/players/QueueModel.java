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
package org.jajuk.services.players;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import org.jajuk.base.Album;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Manages playing sequences
 * <p>
 * Avoid to synchronize these methods because they are called very often and AWT
 * dispatcher thread is frozen when JVM execute a static synchronized method,
 * even outside AWT dispatcher thread
 * </p>
 * 
 * General todo-items:
 * 
 * TODO: we catch exceptions a lot in various places here. Why? We should
 * probably rather avoid or handle them correctly
 * 
 * TODO: insert() and push() are quite similar, but implemented differently,
 * they should be combined
 * 
 * TOOD: the queue/planned handling is cumbersome and planned tracks are not
 * correctly handled sometimes, should be refactored into separate class
 */
public final class QueueModel {
  /** Currently played track index or -1 if none playing item. */
  private static volatile int index = -1;
  /** Last played track. */
  static volatile StackItem itemLast;
  /**
   * The Fifo itself, contains jajuk File objects. This also includes an
   * optional bunch of planned tracks which are accessible with separate
   * methods.
   */
  private static volatile QueueList queue = new QueueList();
  /** Stop flag*. */
  private static volatile boolean bStop = true;
  /** First played file flag. */
  private static boolean bFirstFile = true;
  /** Whether we are currently playing radio. */
  private static volatile boolean playingRadio = false;
  /** Current played radio. */
  private static volatile WebRadio currentRadio;
  /** Last played track actually played duration in ms before a stop. */
  private static long lastDuration;
  /** Should be stop after current track playback ?. */
  private static boolean bStopAfter;
  /** Whether a required stop comes from jajuk */
  private static boolean bInternalStop;

  /**
   * Gets the last duration.
   * 
   * @return Last played track actually played duration in ms before a stop
   */
  public static long getLastDuration() {
    return lastDuration;
  }

  /**
   * No constructor, this class is used statically only.
   */
  private QueueModel() {
  }

  /**
   * FIFO total re-initialization.
   * 
   * Do not set itemLast to null as we need to keep this information in some
   * places
   */
  public static void reset() {
    clear();
    JajukTimer.getInstance().reset();
    bStop = true;
    playingRadio = false;
    currentRadio = null;
  }

  /**
   * Clears the fifo, for example when we want to add a group of files
   * stopping previous plays.
   */
  public static void clear() {
    queue.clear();
    index = -1;
    queue.clearPlanned();
  }

  /**
   * Remove current item (it is always removed independently of its album) and all items from the given album just before and after the given
   * index, i.e. remove all tracks before and after the current one that have
   * the same album.
   * 
   * @param index
   *            The index from where to remove.
   * @param album
   *            The album to remove.
   */
  public static void resetAround(int index, Album album) {
    Set<Integer> indexesToRemove = new TreeSet<Integer>();
    // Add provided index, this one is always removed
    indexesToRemove.add(index);
    for (int i = index; i >= 0; i--) {
      if (queue.get(i).getFile().getTrack().getAlbum().equals(album)) {
        indexesToRemove.add(i);
      }
    }
    for (int i = index; i < queue.size(); i++) {
      if (queue.get(i).getFile().getTrack().getAlbum().equals(album)) {
        indexesToRemove.add(i);
      }
    }
    remove(indexesToRemove);
  }

  /**
   * Set given repeat mode to all in FIFO.
   * 
   * @param bRepeat
   *            True, if repeat mode should be turned on, false otherwise.
   */
  public static void setRepeatModeToAll(boolean bRepeat) {
    for (StackItem item : queue) {
      item.setRepeat(bRepeat);
    }
  }

  /**
   * Asynchronous version of push (needed to perform long-task out of awt
   * dispatcher thread).
   * 
   * @param alItems
   *            The list of items to push.
   * @param bKeepPrevious
   *            keep previous files or stop them to start a new one ?
   */
  public static void push(final List<StackItem> alItems, final boolean bKeepPrevious) {
    push(alItems, bKeepPrevious, false);
  }

  /**
   * Asynchronous version of push (needed to perform long-task out of awt
   * dispatcher thread).
   * 
   * @param alItems
   *            The list of items to push.
   * @param bKeepPrevious
   *            keep previous files or stop them to start a new one ?
   * @param bPushNext
   *            whether the selection is added after playing track (mutual
   *            exclusive with simple push)
   */
  public static void push(final List<StackItem> alItems, final boolean bKeepPrevious,
      final boolean bPushNext) {
    Thread t = new Thread("Queue Push Thread") { // do it in a thread to
      // make
      // UI more reactive
      @Override
      public void run() {
        try {
          UtilGUI.waiting();
          pushCommand(alItems, bKeepPrevious, bPushNext);
        } catch (Exception e) {
          Log.error(e);
        } finally {
          // refresh playlist editor
          ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
          UtilGUI.stopWaiting();
        }
      }
    };
    t.setPriority(Thread.MAX_PRIORITY);
    t.start();
  }

  /**
   * Asynchronous version of push (needed to perform long-task out of awt
   * dispatcher thread).
   * 
   * @param item
   *            The item to push.
   * @param bKeepPrevious
   *            keep previous files or stop them to start a new one ?
   */
  public static void push(final StackItem item, final boolean bKeepPrevious) {
    push(item, bKeepPrevious, false);
  }

  /**
   * Asynchronous version of push (needed to perform long-task out of awt
   * dispatcher thread).
   * 
   * @param item
   *            The item to push.
   * @param bKeepPrevious
   *            keep previous files or stop them to start a new one ?
   * @param bPushNext
   *            whether the selection is added after playing track (mutual
   *            exclusive with simple push)
   */
  public static void push(final StackItem item, final boolean bKeepPrevious, final boolean bPushNext) {
    Thread t = new Thread("Queue Push Thread") {
      // do it in a thread to make UI more reactive
      @Override
      public void run() {
        try {
          UtilGUI.waiting();
          pushCommand(item, bKeepPrevious, bPushNext);
        } catch (Exception e) {
          Log.error(e);
        } finally {
          // refresh queue
          ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
          UtilGUI.stopWaiting();
        }
      }
    };
    t.setPriority(Thread.MAX_PRIORITY);
    t.start();
  }

  /**
   * Launch a web radio.
   * 
   * @param radio
   *            webradio to launch
   */
  public static void launchRadio(WebRadio radio) {
    try {
      UtilGUI.waiting();
      /**
       * Force buttons to opening mode by default, then if they start
       * correctly, a PLAYER_PLAY event will be notified to update to
       * final state. We notify synchronously to make sure the order
       * between these two events will be correct*
       */
      ObservationManager.notifySync(new JajukEvent(JajukEvents.PLAY_OPENING));
      currentRadio = radio;
      // Play the stream
      boolean bPlayOK = Player.play(radio);
      if (bPlayOK) { // refresh covers if play is started
        Log.debug("Now playing :" + radio.toString());
        playingRadio = true;
        // Store current radio for next startup
        Conf.setProperty(Const.CONF_DEFAULT_WEB_RADIO, radio.getName());
        // Send an event that a webradio has been launched        
        Properties pDetails = new Properties();
        pDetails.put(Const.DETAIL_CONTENT, radio);
        ObservationManager.notify(new JajukEvent(JajukEvents.WEBRADIO_LAUNCHED, pDetails));
        //If Webradio info had been updated for current station by WebRadioPlayerImpl then notify again with the updated info
        Properties webradioInfoUpdatedEvent = ObservationManager
            .getDetailsLastOccurence(JajukEvents.WEBRADIO_INFO_UPDATED);
        if (webradioInfoUpdatedEvent != null) {
          WebRadio updatedWebRadio = (WebRadio) webradioInfoUpdatedEvent.get(Const.DETAIL_CONTENT);
          if (radio.getName().equals(updatedWebRadio.getName())) {
            ObservationManager.notify(new JajukEvent(JajukEvents.WEBRADIO_INFO_UPDATED,
                webradioInfoUpdatedEvent));
          }
        }
        bStop = false;
      }
    } catch (Throwable t) {// catch even Errors (OutOfMemory for example)
      Log.error(122, t);
      playingRadio = false;
    } finally {
      UtilGUI.stopWaiting(); // stop the waiting cursor
    }
  }

  /**
   * Push some files in the fifo.
   * 
   * @param item
   *            , item to be played
   * @param bKeepPrevious
   *            keep previous files or stop them to start a new one ?
   * @param bPushNext
   *            whether the selection is added after playing track (mutual
   *            exclusive with simple push)
   */
  private static void pushCommand(StackItem item, boolean bKeepPrevious, final boolean bPushNext) {
    List<StackItem> alFiles = new ArrayList<StackItem>(1);
    alFiles.add(item);
    pushCommand(alFiles, bKeepPrevious, bPushNext);
  }

  /**
   * Push some stack items in the fifo.
   * 
   * @param alItems
   *            , list of items to be played
   * @param bKeepPrevious
   *            keep previous files or stop them to start a new one ?
   * @param bPushNext
   *            whether the selection is added in first in queue
   */
  private static void pushCommand(List<StackItem> alItems, boolean bKeepPrevious,
      final boolean bPushNext) {
    try {
      // wake up FIFO if stopped
      bStop = false;
      // first try to mount needed devices
      Iterator<StackItem> it = alItems.iterator();
      boolean bNoMount = false;
      while (it.hasNext()) {
        StackItem item = it.next();
        if (item == null) {
          it.remove();
          break;
        }
        // Do not synchronize this as we will wait for user response
        if (!item.getFile().getDirectory().getDevice().isMounted()) {
          if (!bNoMount) {
            // not mounted, ok let them a chance to mount it:
            final String sMessage = Messages.getString("Error.025") + " ("
                + item.getFile().getDevice().getName() + Messages.getString("FIFO.4");
            int i = Messages.getChoice(sMessage, JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            if (i == JOptionPane.YES_OPTION) {
              try {
                item.getFile().getDevice().mount(true);
              } catch (Exception e) {
                it.remove();
                Log.error(e);
                Messages.showErrorMessage(11, item.getFile().getDevice().getName());
                return;
              }
            } else if (i == JOptionPane.NO_OPTION) {
              bNoMount = true; // do not ask again
              // If only a single track was pushed and user decided not to 
              // mount its device, do not display another error message about void selection
              if (alItems.size() == 1) {
                return;
              }
              it.remove();
            } else if (i == JOptionPane.CANCEL_OPTION) {
              return;
            }
          } else {
            it.remove();
          }
        }
      }
      synchronized (QueueModel.class) {
        // test if we have yet some files to consider
        if (alItems.size() == 0) {
          Messages.showWarningMessage(Messages.getString("Warning.6"));
          return;
        }
        // clear queue if selection contains some repeat items and we are not in repeat all mode
        if (!Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL)) {
          for (StackItem item : alItems) {
            if (item.isRepeat()) {
              clear();
              break;
            }
          }
        }
        // Reset repeat state for all previous items
        setRepeatModeToAll(false);
        // Position of insert into the queue
        int pos = (queue.size() == 0) ? 0 : queue.size();
        // OK, stop current track if no append
        if (!bKeepPrevious && !bPushNext) {
          index = pos;
          Player.stop(false);
        }
        // if push to front, set pos to first item
        else if (bPushNext) {
          pos = index + 1;
        }
        // If push, not play, add items at the end
        else if (bKeepPrevious && queue.size() > 0) {
          pos = queue.size();
        }
        // add required tracks in the FIFO
        for (StackItem item : alItems) {
          if (pos >= queue.size()) {
            queue.add(item);
          } else {
            queue.add(pos, item);
          }
          pos++;
          JajukTimer.getInstance().addTrackTime(item.getFile());
        }
        // Apply repeat mode if required.
        if (Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL)) {
          setRepeatModeToAll(true);
        }
        // launch track if required
        if (!Player.isPlaying()) {
          launch();
        }
        // computes planned tracks
        computesPlanned(true);
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Contains repeated item.
   * 
   * @param items
   *            The items to check for repeat.
   * 
   * @return whether a stack item list contains a least one repeated item
   */
  private static boolean containsRepeatedItem(List<StackItem> items) {
    for (StackItem item : items) {
      if (item.isRepeat()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Contains repeat.
   * 
   * @return whether FIFO contains a least one repeated item
   */
  public static boolean containsRepeat() {
    return queue.containsRepeat();
  }

  /**
   * Finished method, called by the PlayerImpl when the track is finished or
   * should be finished (in case of intro mode or cross fade).
   */
  public static void finished() {
    finished(false);
  }

  /**
   * Finished method, called by the PlayerImpl when the track is finished or
   * should be finished (in case of intro mode, crass fade, previous/next
   * track ...).
   * 
   * @param forceNext
   *            whether to play the next track, even in single repeat.
   */
  public static void finished(boolean forceNext) {
    try {
      // Tell jajuk not to enable fade-out for this kind of stop request
      bInternalStop = true;
      // If no playing item, just leave
      StackItem current = getCurrentItem();
      if (current == null) {
        return;
      }
      notifyFinishedIfRequired();
      computeNewIndex(forceNext, current);
      unsetRepeatModeIfRequired(current);
      // Leave if stop after current track option is set
      if (bStopAfter) {
        bStopAfter = false;
        stopRequest();
        return;
      }
      // Nothing more to play ? check if we are in continue mode
      if (queue.size() == 0 || index >= queue.size()) {
        if (Conf.getBoolean(Const.CONF_STATE_CONTINUE) && itemLast != null) {
          final StackItem item = queue.popNextPlanned();
          final File file;
          // if some tracks are planned (can be 0 if planned size=0)
          if (item != null) {
            file = item.getFile();
          } else {
            // otherwise, take next track from file manager
            file = FileManager.getInstance().getNextFile(itemLast.getFile());
          }
          if (file != null) {
            // push it, it will be played
            pushCommand(new StackItem(file), false, false);
          } else {
            // probably end of collection
            endOfQueueReached();
          }
        } else {
          endOfQueueReached();
          return;
        }
      } else {
        // something more in FIFO
        launch();
      }
      // computes planned tracks
      computesPlanned(false);
    } catch (Exception e) {
      Log.error(e);
    } finally {
      bInternalStop = false;
      // refresh playlist editor
      ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
    }
  }

  /**
   * Drop the repeat properties of single tracks in single repeat mode to make
   *  sure already played tracks are no longer in repeat mode 
   * @param current
   */
  private static void unsetRepeatModeIfRequired(StackItem current) {
    if (Conf.getBoolean(Const.CONF_STATE_REPEAT) && current.isRepeat()) {
      current.setRepeat(false);
    }
  }

  /**
   * Set a new value for the index. Note that this method has to be synchronized to handle concurrency from push
   * @param forceNext
   * @param current
   */
  private static synchronized void computeNewIndex(boolean forceNext, StackItem current) {
    if (Conf.getBoolean(Const.CONF_STATE_SHUFFLE) && queue.size() > 1
    // In repeat mode, shuffle has no effect
        && !Conf.getBoolean(Const.CONF_STATE_REPEAT)) {
      index = UtilSystem.getRandom().nextInt(queue.size() - 1);
    } else if (current.isRepeat()) {
      // if the track was in repeat mode, don't remove it from the
      // fifo but increment the index
      if (index < queue.size() - 1) {
        StackItem itemNext = queue.get(index + 1);
        // if next track is repeat, inc index
        if (itemNext.isRepeat() || forceNext) {
          index++;
        }
      } else { // We reached end of fifo
        StackItem itemNext = queue.get(0);
        // if next track is repeat, inc index, otherwise we keep the
        // current index
        if (itemNext.isRepeat() || forceNext) {
          index = 0;
        }
      }
    } else if (index < queue.size()) {
      index++;
    }
  }

  private static void notifyFinishedIfRequired() {
    if (getPlayingFile() != null) {
      Properties details = new Properties();
      details.put(Const.DETAIL_CURRENT_FILE, getPlayingFile());
      details.put(Const.DETAIL_CONTENT, Player.getActuallyPlayedTimeMillis());
      ObservationManager.notify(new JajukEvent(JajukEvents.FILE_FINISHED, details));
    }
  }

  /**
   * @return the bInternalStop
   */
  public static boolean isInternalStop() {
    return bInternalStop || ExitService.isExiting();
  }

  /**
   * To do when nothing more is to played,.
   */
  private static void endOfQueueReached() {
    stopRequest();
    if (queue.size() > 0) {
      ObservationManager.notify(new JajukEvent(JajukEvents.PLAYER_STOP));
    } else {
      ObservationManager.notify(new JajukEvent(JajukEvents.ZERO));
    }
  }

  /**
   * Launch track at 'index' position in the fifo.
   */
  private static void launch() {
    try {
      // If no track playing at all, set first in queue
      if (index < 0) {
        index = 0;
      }
      UtilGUI.waiting();
      File toPlay = getItem(index).getFile();
      /**
       * Force buttons to opening mode by default, then if they start
       * correctly, a PLAYER_PLAY event will be notified to update to
       * final state. We notify synchronously to make sure the order
       * between these two events will be correct*
       */
      ObservationManager.notifySync(new JajukEvent(JajukEvents.PLAY_OPENING));
      // Check if we are in single repeat mode, transfer it to new
      // launched track
      if (Conf.getBoolean(Const.CONF_STATE_REPEAT)) {
        getCurrentItem().setRepeat(true);
        ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
      }
      boolean bPlayOK = false;
      // bfirstFile flag is used to set a offset (in %) if required (if we
      // are playing the last item at given position)
      // Known limitation : if the last session's last played item is no
      // more available, the offset is applied
      // to another file. We think that it doesn't worth making things
      // more complicated.
      if (bFirstFile && !Conf.getBoolean(Const.CONF_STATE_INTRO)
          && Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_LAST_KEEP_POS)) {
        // if it is the first played file of the session and we are in
        // startup mode keep position
        float fPos = UtilFeatures.readPersistedPlayingPosition();
        // play it
        bPlayOK = Player.play(toPlay, fPos, Const.TO_THE_END);
      } else {
        if (Conf.getBoolean(Const.CONF_STATE_INTRO)) {
          // intro mode enabled
          bPlayOK = Player.play(toPlay,
              Float.parseFloat(Conf.getString(Const.CONF_OPTIONS_INTRO_BEGIN)) / 100,
              1000 * Integer.parseInt(Conf.getString(Const.CONF_OPTIONS_INTRO_LENGTH)));
        } else {
          // normal mode
          bPlayOK = Player.play(toPlay, 0.0f, Const.TO_THE_END);
        }
      }
      if (bPlayOK) {
        // notify to devices like commandJPanel to update UI when the
        // play
        // button has been pressed
        ObservationManager.notify(new JajukEvent(JajukEvents.PLAYER_PLAY));
        Log.debug("Now playing :" + toPlay);
        // Send an event that a track has been launched
        Properties pDetails = new Properties();
        if (itemLast != null) {
          pDetails.put(Const.DETAIL_OLD, itemLast);
        }
        pDetails.put(Const.DETAIL_CURRENT_FILE_ID, toPlay.getID());
        pDetails.put(Const.DETAIL_CURRENT_DATE, Long.valueOf(System.currentTimeMillis()));
        ObservationManager.notify(new JajukEvent(JajukEvents.FILE_LAUNCHED, pDetails));
        // Save the last played track (even files in error are stored
        // here as we need this for computes next track to launch after an
        // error)
        // We have to set this line here as we make directory change
        // analyze before for cover change
        itemLast = (StackItem) getCurrentItem().clone();
        playingRadio = false;
        bFirstFile = false;
        // add hits number
        toPlay.getTrack().incHits(); // inc hits number
        // recalculate the total time left
        JajukTimer.getInstance().reset();
        for (int i = index; i < queue.size(); i++) {
          JajukTimer.getInstance().addTrackTime(queue.get(i).getFile());
        }
      } else {
        // Problem launching the track, try next one
        UtilGUI.stopWaiting();
        ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
        try {
          Thread.sleep(Const.WAIT_AFTER_ERROR);
        } catch (InterruptedException e) {
          Log.error(e);
        }
        // save the last played track (even files in error are stored
        // here as we need this for computes next track to launch after an
        // error)
        if (getCurrentItem() != null) {
          itemLast = (StackItem) getCurrentItem().clone();
        } else {
          itemLast = null;
        }
        // We test if user required stop. Must be done here to make a
        // chance to
        // stop before starting a new track
        if (!bStop) {
          finished();
        }
      }
    } catch (Throwable t) {// catch even Errors (OutOfMemory for example)
      Log.error(122, t);
    } finally {
      UtilGUI.stopWaiting(); // stop the waiting cursor
    }
  }

  /**
   * Computes planned tracks.
   * 
   * @param bClear
   *            : clear planned tracks stack
   */
  public static void computesPlanned(boolean bClear) {
    // Check if we are in continue mode and we have some tracks in FIFO, if
    // not : no planned tracks
    if (!Conf.getBoolean(Const.CONF_STATE_CONTINUE) || containsRepeat() || queue.size() == 0
        || Conf.getBoolean(Const.CONF_STATE_SHUFFLE)) {
      queue.clearPlanned();
      return;
    }
    if (bClear) {
      queue.clearPlanned();
    }
    int missingPlannedSize = Conf.getInt(Const.CONF_OPTIONS_VISIBLE_PLANNED) - queue.sizePlanned();
    for (int i = 0; i < missingPlannedSize; i++) {
      StackItem item = null;
      StackItem siLast = null; // last item in fifo or planned
      // if planned stack contains yet some tracks
      if (queue.sizePlanned() > 0) {
        siLast = queue.getPlanned(queue.sizePlanned() - 1); // last
        // one
      } else if (queue.size() > 0) { // if fifo contains yet some
        // tracks to play
        siLast = queue.get(queue.size() - 1); // last one
      }
      try {
        // if fifo contains yet some tracks to play
        if (siLast != null) {
          item = new StackItem(FileManager.getInstance().getNextFile(siLast.getFile()), false);
        } else { // nothing in fifo, take first files in
          // collection
          List<File> files = FileManager.getInstance().getFiles();
          item = new StackItem(files.get(0), false);
        }
        // Tell it is a planned item
        item.setPlanned(true);
        // add the new item
        queue.addPlanned(item);
      } catch (JajukException je) {
        // can be thrown if FileManager return a null file (like
        // when reaching the end of the collection)
        break;
      }
    }
  }

  /**
   * Contains only repeat.
   * 
   * @return whether the FIFO contains only repeated files
   */
  public static boolean containsOnlyRepeat() {
    return queue.containsOnlyRepeat();
  }

  /**
   * Play previous track.
   */
  public static void playPrevious() {
    try {
      bStop = false;
      // if playing, stop all playing players
      if (Player.isPlaying()) {
        Player.stop(true);
      }
      JajukTimer.getInstance().reset();
      JajukTimer.getInstance().addTrackTime(queue);
      // If we are playing first item, keep index = 0
      if (index > 0) {
        index--;
      }
      launch();
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Play previous album.
   */
  public static void playPreviousAlbum() {
    try {
      if (index <= 0) {
        return;
      }
      bStop = false;
      boolean bOK = false;
      Directory dir = null;
      if (getPlayingFile() != null) {
        dir = getPlayingFile().getDirectory();
      } else {// nothing in FIFO? just leave
        return;
      }
      while (!bOK) {
        // If we are playing first item, keep index = 0
        if (index > 0) {
          index--;
        }
        Directory dirTested = null;
        File file = queue.get(index).getFile();
        dirTested = file.getDirectory();
        if (dir.equals(dirTested)) { // yet in the same album
          continue;
        } else {
          // OK, previous is not in the same directory
          // than current track, now check if it is the
          // FIRST track from this new directory
          if (FileManager.getInstance().isVeryfirstFile(file) ||
          // this was the very first file from collection
              (FileManager.getInstance().getPreviousFile(file) != null && FileManager.getInstance()
                  .getPreviousFile(file).getDirectory() != file.getDirectory())) {
            // if true, it was the first track from the dir
            bOK = true;
          }
        }
      }
      launch();
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Play next track in selection.
   */
  public static void playNext() {
    try {
      bStop = false;
      // if playing, stop current
      if (Player.isPlaying()) {
        Player.stop(true);
      }
      // force a finish to current track if any
      if (getPlayingFile() != null) { // if stopped, nothing to stop
        finished(true); // stop current track
      } else if (itemLast != null) { // try to launch any previous
        // file
        pushCommand(itemLast, false, false);
      } else { // really nothing? play a shuffle track from collection
        File file = FileManager.getInstance().getShuffleFile();
        if (file != null) {
          pushCommand(new StackItem(file, Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), false),
              false, false);
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Play next track in selection.
   */
  public static void playNextAlbum() {
    try {
      bStop = false;
      // if playing, stop all playing players
      if (Player.isPlaying()) {
        Player.stop(true);
      }
      // we don't support album navigation inside repeated tracks
      if (getQueueSize() > 0 && getItem(0).isRepeat()) {
        playNext();
        return;
      }
      int indexFirstItem = -1;
      if (getPlayingFile() != null) {
        // ref directory
        Directory dir = getPlayingFile().getDirectory();
        // scan current fifo and try to launch the first track not from
        // this album
        for (int i = getIndex(); i < queue.size(); i++) {
          File file = getItem(i).getFile();
          if (!file.getDirectory().equals(dir)) {
            indexFirstItem = i;
            break;
          }
        }
      }
      if (indexFirstItem > 0) {
        // some tracks of other album were already in
        // fifo
        // add a fake album at the top the fifo because the
        // finish will drop first element and we won't
        // drop first track of the next album
        goTo(indexFirstItem);
      } else if (itemLast != null) {// void fifo, add next album
        File fileNext = itemLast.getFile();
        fileNext = FileManager.getInstance().getNextAlbumFile(fileNext);
        // Now add the associated album to the
        Album album = fileNext.getTrack().getAlbum();
        List<File> files = UtilFeatures.getPlayableFiles(album);
        List<StackItem> stack = UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(files),
            Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), true);
        // Find index to go to (first index with a file whose dir is
        // different
        // from current one)
        int index = getIndex();
        Directory currentDir = null;
        if (index < queue.size()) {
          currentDir = queue.get(index).getFile().getDirectory();
        }
        while (index < queue.size() && queue.get(index).getFile().getDirectory().equals(currentDir)) {
          index++;
        }
        queue.addAll(index, stack);
        goTo(index);
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Get the currently played file or null if no playing file.
   * 
   * @return File
   */
  public static File getPlayingFile() {
    if (isStopped()) {
      return null;
    }
    StackItem item = getCurrentItem();
    return (item == null) ? null : item.getFile();
  }

  /**
   * Get the currently played file or null if no playing file.
   * 
   * @return File
   */
  public static String getPlayingFileTitle() {
    File file = getPlayingFile();
    if (file != null) {
      String pattern = Conf.getString(Const.CONF_PATTERN_FRAME_TITLE);
      String title = null;
      try {
        title = UtilString.applyPattern(file, pattern, false, false);
      } catch (JajukException e) {
        Log.error(e);
      }
      return title;
    }
    return null;
  }

  /**
   * Get the currently played stack item or null if no playing item.
   * 
   * @return stack item
   */
  public static StackItem getCurrentItem() {
    if (index < queue.size() && index >= 0) {
      return queue.get(index);
    } else {
      return null;
    }
  }

  /**
   * Get an item at given index in FIFO.
   * 
   * @param index
   *            : index
   * 
   * @return stack item
   */
  public static StackItem getItem(int index) {
    return queue.get(index);
  }

  /**
   * Get index of the last repeated item, -1 if none repeated.
   * 
   * @return index
   */
  private static int getLastRepeatedItem() {
    int i = -1;
    Iterator<StackItem> iterator = queue.iterator();
    while (iterator.hasNext()) {
      StackItem item = iterator.next();
      if (item.isRepeat()) {
        i++;
      } else {
        break;
      }
    }
    return i;
  }

  /**
   * Return true if none file is playing or planned to play for the given
   * device.
   * 
   * @param device
   *            device to unmount
   * 
   * @return true, if can unmount
   */
  public static boolean canUnmount(Device device) {
    if (isStopped() || isPlayingRadio()) {
      return true;
    }
    if (getPlayingFile() != null && getPlayingFile().getDirectory().getDevice().equals(device)) {
      // is current track on this device?
      return false;
    }
    // work on a queue copy to avoid the concurrent access issues
    List<StackItem> copy = new ArrayList<StackItem>(queue);
    Iterator<StackItem> it = copy.iterator();
    // are next tracks in fifo on this device?
    while (it.hasNext()) {
      StackItem item = it.next();
      File file = item.getFile();
      if (file.getDirectory().getDevice().equals(device)) {
        queue.remove(item);
      }
    }
    return true;
  }

  /**
   * Stop request.
   */
  public static void stopRequest() {
    // fifo is over ( stop request ) , reinit labels in information panel
    // before exiting
    bStop = true;
    Player.stop(true); // stop player
    // No more playing webradio
    playingRadio = false;
    // notify views like commandJPanel to update ui
    ObservationManager.notify(new JajukEvent(JajukEvents.PLAYER_STOP));
  }

  /**
   * Returns whether FIFO is stopped or not <br>
   * Caution ! the FIFO may be stopped but current track is not void and a web
   * radio can be playing.
   * 
   * @return Returns whether FIFO is stopped or not
   */
  public static boolean isStopped() {
    return bStop;
  }

  /**
   * Gets the queue.
   * 
   * @return Returns a defensive copy of the fifo
   */
  public static List<StackItem> getQueue() {
    return queue.getQueue();
  }

  /**
   * Return queue size.
   * 
   * @return FIFO size (do not use getFIFO().size() for performance reasons)
   */
  public static int getQueueSize() {
    return queue.size();
  }

  /**
   * Insert a file to play in FIFO at specified position.
   * 
   * @param item
   *            the item to insert.
   * @param iPos
   *            The position where the item is inserted.
   */
  public static void insert(StackItem item, int iPos) {
    List<StackItem> alStack = new ArrayList<StackItem>(1);
    alStack.add(item);
    insert(alStack, iPos);
  }

  /**
   * Insert a file at specified position, any existing item at this position
   * is shifted on the right.
   * 
   * @param alFiles
   *            The list of items to insert.
   * @param iPos
   *            The position where the items are inserted.
   */
  public static void insert(List<StackItem> alFiles, int iPos) {
    if (iPos <= queue.size()) {
      // add in the FIFO, accept a file at
      // size() position to allow increasing
      // FIFO at the end
      queue.addAll(iPos, alFiles);
      if (iPos <= index) {
        index += alFiles.size();
      }
      JajukTimer.getInstance().addTrackTime(alFiles);
    }
    computesPlanned(false);
    // refresh queue
    ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
  }

  /**
   * Put up an item from given index to index-1.
   * 
   * @param lIndex
   *            The index to move up in the queue.
   */
  public static void up(int lIndex) {
    if (lIndex == 0 || lIndex >= queue.size()) {
      // Can't put up first track in queue or
      // first planned track.
      // This should be already made by ui behavior
      return;
    }
    if (lIndex < queue.size()) {
      StackItem item = queue.get(lIndex);
      queue.remove(lIndex); // remove the item
      queue.add(lIndex - 1, item); // add it again above
      if (lIndex == index) {
        index--;
      }
    }
  }

  /**
   * Put down an item from given index to index+1.
   * 
   * @param lIndex
   *            The index to move down in the queue.
   */
  public static void down(int lIndex) {
    if (lIndex >= queue.size() - 1) {
      // Can't put down last track in FIFO. This should be already made by
      // ui behavior
      return;
    }
    StackItem item = queue.get(lIndex);
    queue.remove(lIndex); // remove the item
    queue.add(lIndex + 1, item); // add it again above
    if (lIndex == index) {
      index++;
    }
  }

  /**
   * Go to given index and launch it.
   * 
   * @param pIndex
   *            The index to go to in the queue.
   */
  public static void goTo(final int pIndex) {
    bStop = false;
    try {
      index = pIndex;
      // need to stop before launching! this fixes a
      // wrong EOM event in BasicPlayer
      Player.stop(false);
      launch();
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Remove a track at specified index from the queue model.
   * 
   * @param int index
   *            index of the item to remove
   *               
   **/
  public static void remove(final int index) {
    Set<Integer> indexes = new HashSet<Integer>(1);
    indexes.add(index);
    remove(indexes);
  }

  /**
   * Remove files at specified indexes.
     * @param Set<Integer> indexes
   *            set of index to drop. We expect the array to contain integers sorted by ascendent order. The set may be void (a warning is then logged) but not null
   */
  public static void remove(final Set<Integer> initialIndexes) {
    List<Integer> indexes = new ArrayList<Integer>(initialIndexes);
    // controls indexes
    if (indexes.size() == 0) {
      Log.warn("Removal required for a void list of indexes");
      return;
    }
    for (int indexToRemove : indexes) {
      if (indexToRemove < 0 || indexToRemove >= queue.size()) {
        throw new IllegalStateException("Illegal removal index : " + index + " / " + queue.size()
            + " / " + queue.sizePlanned());
      }
    }
    boolean removePlayedTrack = isPlayingTrack() && initialIndexes.contains(QueueModel.index);
    boolean removePlayedTrackThatIsLastInQueue = removePlayedTrack
        && indexes.get(indexes.size() - 1) == (queue.size() - 1);
    StackItem firstPlannedTrack = null;
    List<StackItem> plannedQueue = QueueModel.getPlanned();
    if (plannedQueue.size() > 0) {
      firstPlannedTrack = plannedQueue.get(0);
      firstPlannedTrack.setPlanned(false);
    }
    for (int indexToRemove : indexes) {
      // Remove this file from fifo and recompute current index if required.
      // We have to decrement current index if we drop tracks prior current or 
      // if we dropped the last item.
      queue.remove(indexToRemove);
      if (indexToRemove < index || removePlayedTrackThatIsLastInQueue) {
        index--;
      }
      // Recompute indexes to remove to take into account the offset created by the removal.
      // First indexes may become negative, this is not an issue as they are already processed.
      int comp = 0;
      for (int i : indexes) {
        if (i >= indexToRemove) {
          indexes.set(comp, i - 1);
        }
        comp++;
      }
    }
    // Take launcher actions due to removals
    // If this is the playing track, stop it before dropping it
    // However, we have an issue if we remove the last file of the queue if it is playing : the new first 
    // planned track of the new queue is the removed file itself so we would replay the removed track again. 
    // To avoid this, we store the first planned track and we force it.  
    if (removePlayedTrack) {
      if (removePlayedTrackThatIsLastInQueue) {
        if (firstPlannedTrack != null) { // null is not in continue mode
          push(firstPlannedTrack, false);
        } else {
          stopRequest();
        }
      } else {
        goTo(index);
      }
    }
    // Recomputes all planned tracks from last file in fifo
    computesPlanned(true);
  }

  /**
   * Gets the last.
   * 
   * @return Last Stack item in FIFO
   */
  public static StackItem getLast() {
    if (queue.size() == 0) {
      return null;
    }
    return queue.get(queue.size() - 1);
  }

  /**
   * Gets the last played.
   * 
   * @return Last played item
   */
  public static StackItem getLastPlayed() {
    return itemLast;
  }

  /**
   * Gets the index.
   * 
   * @return Returns the index.
   */
  public static int getIndex() {
    return index;
  }

  /**
   * Gets the count tracks left.
   * 
   * @return the count tracks left
   */
  public static int getCountTracksLeft() {
    if (index == -1) {
      // none playing track
      return queue.size();
    }
    return queue.size() - index;
  }

  /**
   * Gets the planned.
   * 
   * @return Returns a defensive copy of planned files
   */
  public static List<StackItem> getPlanned() {
    return queue.getPlanned();
  }

  /**
   * Store current FIFO as a list.
   * 
   * @throws IOException
   *             Signals that an I/O exception has occurred.
   */
  public static void commit() throws IOException {
    java.io.File out = SessionService.getConfFileByPath(Const.FILE_FIFO + "."
        + Const.FILE_SAVING_FILE_EXTENSION);
    PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(out, false)));
    for (StackItem st : queue) {
      writer.println(st.getFile().getID());
    }
    writer.flush();
    writer.close();
    //Store index
    Conf.setProperty(Const.CONF_STARTUP_QUEUE_INDEX, Integer.toString(index));
    // Override initial file
    java.io.File finalFile = SessionService.getConfFileByPath(Const.FILE_FIFO);
    UtilSystem.saveFileWithRecoverySupport(finalFile);
    Log.debug("Queue commited to : " + finalFile.getAbsolutePath());
  }

  /**
   * Return whether a web radio is being played.
   * 
   * @return whether a web radio is being played
   */
  public static boolean isPlayingRadio() {
    return playingRadio;
  }

  /**
   * Return current web radio if any or null otherwise.
   * 
   * @return current web radio if any or null otherwise
   */
  public static WebRadio getCurrentRadio() {
    return currentRadio;
  }

  /**
   * Return whether a track is being played.
   * 
   * @return whether a track is being played
   */
  public static boolean isPlayingTrack() {
    return !bStop && !isPlayingRadio();
  }

  /**
   * Gets the current file title.
   * 
   * @return a string representation for current played item or stop state
   */
  public static String getCurrentFileTitle() {
    String title = null;
    File file = getPlayingFile();
    if (isPlayingRadio()) {
      Properties webradioInfoUpdatedEvent = ObservationManager
          .getDetailsLastOccurence(JajukEvents.WEBRADIO_INFO_UPDATED);
      // TODO Strange but we experienced NPE here coming from a call from tray/mouseMoved, so we perform sanity check, to be investigated
      if (webradioInfoUpdatedEvent != null) {
        WebRadio updatedWebRadio = (WebRadio) webradioInfoUpdatedEvent.get(Const.DETAIL_CONTENT);
        if (getCurrentRadio().getName().equals(updatedWebRadio.getName())) {
          title = (String) webradioInfoUpdatedEvent.get(Const.CURRENT_RADIO_TRACK);
        } else {
          title = getCurrentRadio().getName();
        }
      } else {
        title = Messages.getString("JajukWindow.18");
      }
    } else if (file != null && !isStopped()) {
      title = file.getHTMLFormatText();
    } else {
      title = Messages.getString("JajukWindow.18");
    }
    return title;
  }

  /**
   * Force FIFO cleanup, for example after files deletion.
   */
  public static synchronized void clean() {
    Iterator<StackItem> it = queue.iterator();
    int i = 0;
    while (it.hasNext()) {
      StackItem si = it.next();
      if (FileManager.getInstance().getFileByID(si.getFile().getID()) == null) {
        it.remove();
        if (i <= index) {
          index--;
        }
      }
      i++;
    }
    computesPlanned(true);
  }

  /**
   * Force FIFO index.
   * 
   * @param index
   *            
   * 
   * @pram index index to set
   */
  public static synchronized void setIndex(int index) {
    QueueModel.index = index;
  }

  /**
   * Sets the stop after.
   * 
   * @param stopAfter Whether we should stop after current track playback
   */
  public static void setStopAfter(boolean stopAfter) {
    QueueModel.bStopAfter = stopAfter;
  }
}
