/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 *  $Revision: 3231 $
 */
package org.jajuk.services.players;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
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
 */
public final class FIFO {

  /** Currently played track index */
  private static int index;

  /** Last played track */
  private static StackItem itemLast;

  /** Fifo itself, contains jajuk File objects */
  private static volatile List<StackItem> alFIFO = new ArrayList<StackItem>(50);

  /** Planned tracks */
  private static volatile List<StackItem> alPlanned = new ArrayList<StackItem>(10);

  /** Stop flag* */
  private static volatile boolean bStop = true;

  /** First played file flag* */
  private static boolean bFirstFile = true;

  /** Whether we are currently playing radio */
  private static boolean playingRadio = false;

  /** Current played radio */
  private static WebRadio currentRadio;

  /**
   * No constructor, this class is used statically only
   */
  private FIFO() {
  }

  /**
   * FIFO total reinitialization
   */
  public static void reset() {
    alFIFO.clear();
    alPlanned.clear();
    JajukTimer.getInstance().reset();
    index = 0;
    itemLast = null;
  }

  /**
   * Set given repeat mode to all in FIFO
   * 
   * @param bRepeat
   */
  public static void setRepeatModeToAll(boolean bRepeat) {
    for (StackItem item : alFIFO) {
      item.setRepeat(bRepeat);
    }
  }

  /**
   * Asynchronous version of push (needed to perform long-task out of awt
   * dispatcher thread)
   * 
   * @param alItems
   * @param bAppend
   */
  public static void push(final List<StackItem> alItems, final boolean bAppend) {
    Thread t = new Thread() { // do it in a thread to make UI more
      // reactive
      @Override
      public void run() {
        try {
          UtilGUI.waiting();
          FIFO.pushCommand(alItems, bAppend);
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
   * dispatcher thread)
   * 
   * @param item
   * @param bAppend
   */
  public static void push(final StackItem item, final boolean bAppend) {
    Thread t = new Thread() {
      // do it in a thread to make UI more reactive
      @Override
      public void run() {
        try {
          UtilGUI.waiting();
          pushCommand(item, bAppend);
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
   * Launch a web radio
   * 
   * @param radio
   *          webradio to launch
   */
  public static void launchRadio(WebRadio radio) {
    try {
      UtilGUI.waiting();
      /**
       * Force buttons to opening mode by default, then if they start correctly, a
       * PLAYER_PLAY event will be notified to update to final state. We notify
       * synchronously to make sure the order between these two events will be
       * correct*
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
        // Send an event that a track has been launched
        Properties pDetails = new Properties();
        pDetails.put(Const.DETAIL_CONTENT, radio);
        ObservationManager.notify(new JajukEvent(JajukEvents.WEBRADIO_LAUNCHED, pDetails));
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
   * Push some stack items in the fifo
   * 
   * @param alItems,
   *          list of items to be played
   * @param bAppend
   *          keep previous files or stop them to start a new one ?
   */
  private static void pushCommand(List<StackItem> alItems, boolean bAppend) {
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
                + item.getFile().getDirectory().getDevice().getName()
                + Messages.getString("FIFO.4");
            int i = Messages.getChoice(sMessage, JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            if (i == JOptionPane.YES_OPTION) {
              try {
                item.getFile().getDirectory().getDevice().mount(true);
              } catch (Exception e) {
                it.remove();
                Log.error(e);
                Messages.showErrorMessage(11, item.getFile().getDirectory().getDevice().getName());
                return;
              }
            } else if (i == JOptionPane.NO_OPTION) {
              bNoMount = true; // do not ask again
              it.remove();
            } else if (i == JOptionPane.CANCEL_OPTION) {
              return;
            }
          } else {
            it.remove();
          }
        }
      }
      synchronized (FIFO.class) {
        // test if we have yet some files to consider
        if (alItems.size() == 0) {
          return;
        }
        // OK, stop current track if no append
        if (!bAppend) {
          Player.stop(false);
          alFIFO.clear();
        }
        int pos = 0;
        // If push, not play, add items at the end
        if (bAppend && alFIFO.size() > 0) {
          pos = alFIFO.size();
        }
        // add required tracks in the FIFO
        for (StackItem item : alItems) {
          alFIFO.add(pos, item);
          pos++;
          JajukTimer.getInstance().addTrackTime(item.getFile());
        }
        // Apply repeat mode if required. If we are in repeat mode or if the
        // selection contains at least a single repeated item, all the fifo is
        // repeated. If selection contains no repeated item, the full fifo is
        // unrepeated
        if (containsRepeatedItem(alItems) || Conf.getBoolean(Const.CONF_STATE_REPEAT)) {
          setRepeatModeToAll(true);
        } else {
          setRepeatModeToAll(false);
        }
        // launch track if required
        if (!bAppend || !Player.isPlaying()) {
          // if we have a play or nothing is playing
          index = 0;
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
   * @param items
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
   * @param items
   * @return whether FIFO contains a least one repeated item
   */
  public static boolean containsRepeat() {
    for (StackItem item : alFIFO) {
      if (item.isRepeat()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Push some files in the fifo
   * 
   * @param item,
   *          item to be played
   * @param bAppend
   *          keep previous files or stop them to start a new one ?
   */
  private static void pushCommand(StackItem item, boolean bAppend) {
    List<StackItem> alFiles = new ArrayList<StackItem>(1);
    alFiles.add(item);
    pushCommand(alFiles, bAppend);
  }

  /**
   * Finished method, called by the PlayerImpl when the track is finished or
   * should be finished (in case of intro mode or crass fade)
   */
  public static void finished() {
    try {
      // If no playing item, just leave
      StackItem current = getCurrentItem();
      if (current == null) {
        return;
      }
      Properties details = new Properties();
      details.put(Const.DETAIL_CURRENT_FILE, getCurrentFile());
      ObservationManager.notify(new JajukEvent(JajukEvents.FILE_FINISHED, details));
      if (current.isRepeat()) {
        // if the track was in repeat mode, don't remove it from the
        // fifo but inc index
        // find the next item is in repeat mode if any
        if (index < alFIFO.size() - 1) {
          StackItem itemNext = alFIFO.get(index + 1);
          // if next track is repeat, inc index
          if (itemNext.isRepeat()) {
            index++;
            // no next track in repeat mode, come back to first
            // element in fifo
          } else {
            index = 0;
          }
        } else { // no next element
          index = 0; // come back to first element
        }
      } else if (index < alFIFO.size()) {
        // current track was not in repeat mode, remove it from fifo
        StackItem item = alFIFO.get(index);
        JajukTimer.getInstance().removeTrackTime(item.getFile());
        alFIFO.remove(index); // remove this file from fifo
      }
      if (alFIFO.size() == 0) { // nothing more to play
        // check if we in continue mode
        if (Conf.getBoolean(Const.CONF_STATE_CONTINUE) && itemLast != null) {
          File file = null;
          // if some tracks are planned (can be 0 if planned size=0)
          if (alPlanned.size() != 0) {
            file = alPlanned.get(0).getFile();
            // remove the planned track
            alPlanned.remove(0);
          } else {
            // otherwise, take next track from file manager
            file = FileManager.getInstance().getNextFile(itemLast.getFile());
          }
          if (file != null) {
            // push it, it will be played
            pushCommand(new StackItem(file), false);
          } else {
            // probably end of collection option "restart" off
            JajukTimer.getInstance().reset();
            bStop = true;
            ObservationManager.notify(new JajukEvent(JajukEvents.ZERO));
          }
        } else {
          // no ? just reset UI and leave
          JajukTimer.getInstance().reset();
          bStop = true;
          ObservationManager.notify(new JajukEvent(JajukEvents.ZERO));
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
      // refresh playlist editor
      ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
    }
  }

  /**
   * Launch track at given index in the fifo
   * 
   * @param int
   *          index
   */
  private static void launch() {
    try {
      UtilGUI.waiting();
      File fCurrent = getCurrentFile();
      /**
       * Force buttons to opening mode by default, then if they start correctly, a
       * PLAYER_PLAY event will be notified to update to final state. We notify
       * synchronously to make sure the order between these two events will be
       * correct*
       */
      ObservationManager.notifySync(new JajukEvent(JajukEvents.PLAY_OPENING));

      boolean bPlayOK = false;
      if (bFirstFile && !Conf.getBoolean(Const.CONF_STATE_INTRO)
          && Conf.getString(Const.CONF_STARTUP_MODE).equals(Const.STARTUP_MODE_LAST_KEEP_POS)) {
        // if it is the first played file of the session and we are in
        // startup mode keep position
        float fPos = Conf.getFloat(Const.CONF_STARTUP_LAST_POSITION);
        // play it
        bPlayOK = Player.play(fCurrent, fPos, Const.TO_THE_END);
      } else {
        if (Conf.getBoolean(Const.CONF_STATE_INTRO)) {
          // intro mode enabled
          bPlayOK = Player.play(fCurrent, Float.parseFloat(Conf
              .getString(Const.CONF_OPTIONS_INTRO_BEGIN)) / 100, 1000 * Integer.parseInt(Conf
              .getString(Const.CONF_OPTIONS_INTRO_LENGTH)));
        } else {
          // normal mode
          bPlayOK = Player.play(fCurrent, 0.0f, Const.TO_THE_END);
        }
      }

      if (bPlayOK) {
        // notify to devices like commandJPanel to update UI when the play
        // button has been pressed
        ObservationManager.notify(new JajukEvent(JajukEvents.PLAYER_PLAY));
        Log.debug("Now playing :" + fCurrent);
        // Send an event that a track has been launched
        Properties pDetails = new Properties();
        if (itemLast != null) {
          pDetails.put(Const.DETAIL_OLD, itemLast);
        }
        pDetails.put(Const.DETAIL_CURRENT_FILE_ID, fCurrent.getID());
        pDetails.put(Const.DETAIL_CURRENT_DATE, Long.valueOf(System.currentTimeMillis()));
        ObservationManager.notify(new JajukEvent(JajukEvents.FILE_LAUNCHED, pDetails));
        // save the last played track (even files in error are stored here as
        // we need this for computes next track to launch after an error)
        // We have to set this line here as we make directory change analyze
        // before for cover change
        itemLast = (StackItem) getCurrentItem().clone();
        playingRadio = false;
        bFirstFile = false;
        // add hits number
        fCurrent.getTrack().incHits(); // inc hits number
      } else {
        // Problem launching the track, try next one
        UtilGUI.stopWaiting();
        ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
        try {
          Thread.sleep(Const.WAIT_AFTER_ERROR);
        } catch (InterruptedException e) {
          Log.error(e);
        }
        // save the last played track (even files in error are stored here as
        // we need this for computes next track to launch after an error)
        if (getCurrentItem() != null) {
          itemLast = (StackItem) getCurrentItem().clone();
        } else {
          itemLast = null;
        }
        // We test if user required stop. Must be done here to make a chance to
        // stop before starting a new track
        if (!bStop) {
          FIFO.finished();
        }
      }
    } catch (Throwable t) {// catch even Errors (OutOfMemory for example)
      Log.error(122, t);
    } finally {
      UtilGUI.stopWaiting(); // stop the waiting cursor
    }
  }

  /**
   * Set current index
   * 
   * @param index
   */
  public static void setIndex(int index) {
    FIFO.index = index;
  }

  /**
   * Computes planned tracks
   * 
   * @param bClear :
   *          clear planned tracks stack
   */
  public static void computesPlanned(boolean bClear) {
    // Check if we are in continue mode and we have some tracks in FIFO, if
    // not : no planned tracks
    if (!Conf.getBoolean(Const.CONF_STATE_CONTINUE) || containsRepeat() || alFIFO.size() == 0) {
      alPlanned.clear();
      return;
    }
    if (bClear) {
      alPlanned.clear();
    }
    int iPlannedSize = alPlanned.size();
    int missingPlannedSize = Conf.getInt(Const.CONF_OPTIONS_VISIBLE_PLANNED) - iPlannedSize;

    /*
     * To compute missing planned tracks in shuffle state, we get a global
     * shuffle list and we sub list it. This avoid calling a getShuffle() on
     * file manager file by file because it is very costly
     */
    if (Conf.getBoolean(Const.CONF_STATE_SHUFFLE)) {
      List<File> alFiles = FileManager.getInstance().getGlobalShufflePlaylist();
      // Remove already planned tracks
      for (StackItem item : alPlanned) {
        if (alFiles.contains(item.getFile())) {
          alFiles.remove(item.getFile());
        }
      }
      if (alFiles.size() >= missingPlannedSize) {
        alFiles = alFiles.subList(0, missingPlannedSize - 1);
      }
      List<StackItem> missingPlanned = UtilFeatures.createStackItems(alFiles, Conf
          .getBoolean(Const.CONF_STATE_REPEAT), false);
      for (StackItem item : missingPlanned) {
        item.setPlanned(true);
        alPlanned.add(item);
      }
    } else {
      for (int i = 0; i < missingPlannedSize; i++) {
        @SuppressWarnings("unused")
        StackItem item = null;
        StackItem siLast = null; // last item in fifo or planned
        // if planned stack contains yet some tracks
        if (alPlanned.size() > 0) {
          siLast = alPlanned.get(alPlanned.size() - 1); // last one
        } else if (alFIFO.size() > 0) { // if fifo contains yet some
          // tracks to play
          siLast = alFIFO.get(alFIFO.size() - 1); // last one
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
          alPlanned.add(item);
        } catch (JajukException je) {
          // can be thrown if FileManager return a null file (like when
          // reaching end of collection)
          break;
        }
      }
    }
  }

  /**
   * Clears the fifo, for example when we want to add a group of files stopping
   * previous plays
   * 
   */
  public static void clear() {
    alFIFO.clear();
    alPlanned.clear();
  }

  /**
   * 
   * @return whether the FIFO contains only repeated files
   */
  public static boolean containsOnlyRepeat() {
    for (StackItem item : alFIFO) {
      if (!item.isRepeat()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Get previous track, can add item in first index of FIFO
   * 
   * @return new index of current file
   * @throws Exception
   */
  private static int addPrevious() throws Exception {
    StackItem itemFirst = getItem(0);
    if (itemFirst != null) {
      if (index > 0) { // if we have some repeat files
        index--;
      } else { // we are at the first position
        if (itemFirst.isRepeat()) {
          // restart last repeated item in the loop
          index = getLastRepeatedItem();
        } else {
          // first is not repeated, just insert previous
          // file from collection
          StackItem item = new StackItem(FileManager.getInstance().getPreviousFile(
              (alFIFO.get(0)).getFile()), Conf.getBoolean(Const.CONF_STATE_REPEAT), true);
          alFIFO.add(0, item);
          index = 0;
        }
      }
    }
    return index;
  }

  /**
   * Play previous track
   */
  public static void playPrevious() {
    try {
      bStop = false;
      // if playing, stop all playing players
      if (Player.isPlaying()) {
        Player.stop(true);
      }
      JajukTimer.getInstance().reset();
      JajukTimer.getInstance().addTrackTime(alFIFO);
      addPrevious();
      launch();
    } catch (Exception e) {
      Log.error(e);
    } 
  }

  /**
   * Play previous album
   */
  public static void playPreviousAlbum() {
    try {
      bStop = false;
      // if playing, stop all playing players
      if (Player.isPlaying()) {
        Player.stop(true);
      }
      // we don't support album navigation inside repeated tracks
      if ((getItem(0)).isRepeat()) {
        playPrevious();
        return;
      }
      boolean bOK = false;
      Directory dir = null;
      if (getCurrentFile() != null) {
        dir = getCurrentFile().getDirectory();
      } else {// nothing in FIFO? just leave
        return;
      }
      while (!bOK) {
        int localindex = addPrevious();
        Directory dirTested = null;
        if (alFIFO.get(localindex) == null) {
          return;
        } else {
          File file = alFIFO.get(localindex).getFile();
          dirTested = file.getDirectory();
          if (dir.equals(dirTested)) { // yet in the same album
            continue;
          } else {
            // OK, previous is not in the same directory
            // than current track, now check if it is the
            // FIRST track from this new directory
            if (FileManager.getInstance().isVeryfirstFile(file) ||
            // this was the very first file from collection
                (FileManager.getInstance().getPreviousFile(file) != null && FileManager
                    .getInstance().getPreviousFile(file).getDirectory() != file.getDirectory())) {
              // if true, it was the first track from the dir
              bOK = true;
            }
          }
        }
      }
      launch();
    } catch (Exception e) {
      Log.error(e);
    } 
  }

  /**
   * Play next track in selection
   */
  public static void playNext() {
    try {
      bStop = false;
      // if playing, stop current
      if (Player.isPlaying()) {
        Player.stop(false);
      }
      // force a finish to current track if any
      if (getCurrentFile() != null) { // if stopped, nothing to stop
        finished(); // stop current track
      } else if (itemLast != null) { // try to launch any previous
        // file
        pushCommand(itemLast, false);
      } else { // really nothing? play a shuffle track from collection
        pushCommand(new StackItem(FileManager.getInstance().getShuffleFile(), Conf
            .getBoolean(Const.CONF_STATE_REPEAT), false), false);
      }
    } catch (Exception e) {
      Log.error(e);
    } 
  }

  /**
   * Play next track in selection
   */
  public static void playNextAlbum() {
    try {
      bStop = false;
      // if playing, stop all playing players
      if (Player.isPlaying()) {
        Player.stop(true);
      }
      // we don't support album navigation inside repeated tracks
      if (getItem(0).isRepeat()) {
        playNext();
        return;
      }
      // force a finish to current track if any
      if (getCurrentFile() != null) { // if stopped, nothing to stop
        // ref directory
        Directory dir = getCurrentFile().getDirectory();
        // scan current fifo and try to launch the first track not from
        // this album
        boolean bOK = false;
        while (!bOK && alFIFO.size() > 0) {
          File file = getItem(0).getFile();
          if (file.getDirectory().equals(dir)) {
            remove(0, 0); // remove this file from FIFO, it is
            // from the same album
            continue;
          } else {
            bOK = true;
          }
        }
        if (bOK) {
          // some tracks of other album were already in
          // fifo
          // add a fake album at the top the fifo because the
          // finish will drop first element and we won't
          // drop first track of the next album
          List<StackItem> alFake = new ArrayList<StackItem>(1);
          alFake.add(getItem(0));
          insert(alFake, 0);
          finished(); // stop current track and start the new one
        } else {// void fifo, add next album
          File fileNext = itemLast.getFile();
          do {
            fileNext = FileManager.getInstance().getNextFile(fileNext);
            // look for the next different album
            if (fileNext != null && !fileNext.getDirectory().equals(dir)) {
              pushCommand(new StackItem(fileNext, Conf.getBoolean(Const.CONF_STATE_REPEAT), false),
                  false); // play
              // it
              return;
            }
          } while (fileNext != null);
        }
      } else if (itemLast != null) { // try to launch any previous
        // file
        pushCommand(itemLast, false);
      } else { // really nothing? play a shuffle track from collection
        pushCommand(new StackItem(FileManager.getInstance().getShuffleFile(), Conf
            .getBoolean(Const.CONF_STATE_REPEAT), false), false);
      }
    } catch (Exception e) {
      Log.error(e);
    } 
  }

  /**
   * Get the currently played file or null if no playing file
   * 
   * @return File
   */
  public static File getCurrentFile() {
    StackItem item = getCurrentItem();
    return (item == null) ? null : item.getFile();
  }

  /**
   * Get the currently played stack item or null if no playing item
   * 
   * @return stack item
   */
  public static StackItem getCurrentItem() {
    if (index < alFIFO.size()) {
      return alFIFO.get(index);
    } else {
      return null;
    }
  }

  /**
   * Get an item at given index in FIFO
   * 
   * @param index :
   *          index
   * @return stack item
   */
  public static StackItem getItem(int index) {
    return alFIFO.get(index);
  }

  /**
   * Get index of the last repeated item, -1 if none repeated
   * 
   * @return index
   */
  private static int getLastRepeatedItem() {
    int i = -1;
    Iterator<StackItem> iterator = alFIFO.iterator();
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
   * Return true if none file is playing or planned to play for the given device
   * 
   * @param device
   *          device to unmount
   * @return
   */
  public static boolean canUnmount(Device device) {
    if (isStopped()) { // currently stopped
      return true;
    }
    if (getCurrentFile().getDirectory().getDevice().equals(device)) {
      // is current track on this device?
      return false;
    }
    Iterator<StackItem> it = alFIFO.iterator();
    // are next tracks in fifo on this device?
    while (it.hasNext()) {
      StackItem item = it.next();
      File file = item.getFile();
      if (file.getDirectory().getDevice().equals(device)) {
        return false;
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
    // notify views like commandJPanel to update ui
    ObservationManager.notify(new JajukEvent(JajukEvents.PLAYER_STOP));
  }

  /**
   * 
   * Returns whether FIFO is stopped or not <br>
   * Caution ! the FIFO may be stopped but current track is not void and a web
   * radio can be playing
   * 
   * @return Returns whether FIFO is stopped or not
   * 
   */
  public static boolean isStopped() {
    return bStop;
  }

  /**
   * @return Returns a shallow copy of the fifo
   */
  @SuppressWarnings("unchecked")
  public static List<StackItem> getFIFO() {
    return (List<StackItem>) ((ArrayList<StackItem>) alFIFO).clone();
  }

  /**
   * Not for performance reasons
   * 
   * @return FIFO size (do not use getFIFO().size() for performance reasons)
   */
  public static int getFIFOSize() {
    return alFIFO.size();
  }

  /**
   * Shuffle the FIFO, used when user select the Random mode
   */
  public static void shuffle() {
    if (alFIFO.size() > 1) {
      if (isStopped()) {
        Collections.shuffle(alFIFO, UtilSystem.getRandom());
      } else {
        // Make sure current track is kept to its position
        // so remove it and add it again after shuffling
        alFIFO.remove(0);
        Collections.shuffle(alFIFO, UtilSystem.getRandom());
        alFIFO.add(0, itemLast);
      }
    }
    alPlanned.clear(); // force recomputes planned tracks
  }

  /**
   * Insert a file to play in FIFO at specified position
   * 
   * @param file
   * @param iPos
   */
  public static void insert(StackItem item, int iPos) {
    List<StackItem> alStack = new ArrayList<StackItem>(1);
    alStack.add(item);
    insert(alStack, iPos);
    // refresh queue
    ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));

  }

  /**
   * Insert a file at specified position, any existing item at this position is
   * shifted on the right
   * 
   * @param file
   * @param iPos
   */
  public static void insert(List<StackItem> alFiles, int iPos) {
    if (iPos <= alFIFO.size()) {
      // add in the FIFO, accept a file at
      // size() position to allow increasing
      // FIFO at the end
      alFIFO.addAll(iPos, alFiles);
      JajukTimer.getInstance().addTrackTime(alFiles);
    }
    computesPlanned(false);
    // refresh queue
    ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
  }

  /**
   * Put up an item from given index to index-1
   * 
   * @param index
   */
  public static void up(int index) {
    if (index == 0 || index == alFIFO.size()) {
      // Can't put up first track in queue or
      // first planned track.
      // This should be already made by ui behavior
      return;
    }
    if (index < alFIFO.size()) {
      StackItem item = alFIFO.get(index);
      alFIFO.remove(index); // remove the item
      alFIFO.add(index - 1, item); // add it again above
    } else { // planned track
      StackItem item = alPlanned.get(index - alFIFO.size());
      alFIFO.remove(index - alFIFO.size()); // remove the item
      // add it again above
      alFIFO.add(index - alFIFO.size() - 1, item);
    }
  }

  /**
   * Put down an item from given index to index+1
   * 
   * @param index
   */
  public static void down(int index) {
    if (index == 0 || index == alFIFO.size() - 1 || index == alFIFO.size() + alPlanned.size() - 1) {
      // Can't put down current track, nor last track in FIFO, nor last
      // planned track. This should be already made by ui behavior
      return;
    }
    if (index < alFIFO.size()) {
      StackItem item = alFIFO.get(index);
      alFIFO.remove(index); // remove the item
      alFIFO.add(index + 1, item); // add it again above
    }
  }

  /**
   * Go to given index and launch it
   * 
   * @param index
   */
  public static void goTo(final int pIndex) {
    bStop = false;
    int localindex = pIndex;
    try {
      if (containsRepeatedItem(alFIFO)) {
        // if there are some tracks in repeat, mode
        if (getItem(localindex).isRepeat()) {
          // the selected line is in repeat mode, ok,
          // keep repeat mode and just change index
          FIFO.index = localindex;
        } else {
          // the selected line was not a repeated item,
          // take it as a which to reset repeat mode
          setRepeatModeToAll(false);
          Properties properties = new Properties();
          properties.put(Const.DETAIL_SELECTION, Const.FALSE);
          ObservationManager.notify(new JajukEvent(JajukEvents.REPEAT_MODE_STATUS_CHANGED,
              properties));
          remove(0, localindex - 1);
          localindex = 0;
        }
      } else {
        remove(0, localindex - 1);
        localindex = 0;
      }
      // need to stop before launching! this fix a
      // wrong EOM event in BasicPlayer
      Player.stop(false);
      launch();
    } catch (Exception e) {
      Log.error(e);
    } 
  }

  /**
   * Remove files at specified positions
   * 
   * @param start
   *          index
   * @param stop
   *          index
   */
  public static void remove(int iStart, int iStop) {
    if (iStart <= iStop && iStart >= 0 && iStop < alFIFO.size() + alPlanned.size()) {
      // check size drop items from the end to the beginning
      for (int i = iStop; i >= iStart; i--) {
        // FIFO items
        if (i >= alFIFO.size()) {
          // remove this file from plan
          alPlanned.remove(i - alFIFO.size());
          // complete missing planned tracks
          computesPlanned(false);

        } else { // planned items
          StackItem item = alFIFO.get(i);
          JajukTimer.getInstance().removeTrackTime(item.getFile());
          // remove this file from fifo
          alFIFO.remove(i);
          // Recomputes all planned tracks from last file in fifo
          computesPlanned(true);
        }
      }

    }
  }

  /**
   * 
   * @return Last Stack item in FIFO
   */
  public static StackItem getLast() {
    if (alFIFO.size() == 0) {
      return null;
    }
    return alFIFO.get(alFIFO.size() - 1);
  }

  /**
   * 
   * @return Last played item
   */
  public static StackItem getLastPlayed() {
    return itemLast;
  }

  /**
   * @return Returns the index.
   */
  public static int getIndex() {
    return index;
  }

  /**
   * @return Returns a shallow copy of planned files
   */
  @SuppressWarnings("unchecked")
  public static List<StackItem> getPlanned() {
    return (List<StackItem>) ((ArrayList<StackItem>) alPlanned).clone();
  }

  /**
   * Set the first file flag
   * 
   * @param bFirstFile
   */
  public static void setFirstFile(boolean bFirstFile) {
    FIFO.bFirstFile = bFirstFile;
  }

  /**
   * Clean all references for the given device
   * 
   * @param device:
   *          Device to clean
   */
  @SuppressWarnings("unchecked")
  public static void cleanDevice(Device device) {
    if (alFIFO.size() > 0) {
      List<StackItem> alFIFOCopy = (List<StackItem>) ((ArrayList<StackItem>) alFIFO).clone();
      if (alFIFO.size() > 1) { // keep first item (being played)
        for (int i = 1; i < alFIFO.size(); i++) {
          StackItem item = alFIFO.get(i);
          File file = item.getFile();
          if (file.getDirectory().getDevice().equals(device)) {
            alFIFOCopy.remove(item);
          }
        }
      }
      // Clean FIFO and add again new selection
      clear();
      pushCommand(alFIFOCopy, true);
    }
  }

  /**
   * Store current FIFO as a list
   */
  public static void commit() throws IOException {
    java.io.File file = UtilSystem.getConfFileByPath(Const.FILE_FIFO);
    PrintWriter writer = new PrintWriter(
        new BufferedOutputStream(new FileOutputStream(file, false)));
    int localindex = 0;
    for (StackItem st : alFIFO) {
      if (localindex > 0) {
        // do not store current track (otherwise, it
        // will be duplicate at startup)
        writer.println(st.getFile().getID());
      }
      localindex++;
    }
    writer.flush();
    writer.close();
  }

  /**
   * Return whether a web radio is being played
   * 
   * @return whether a web radio is being played
   */
  public static boolean isPlayingRadio() {
    return FIFO.playingRadio;
  }

  /**
   * Return current web radio if any or null otherwise
   * 
   * @return current web radio if any or null otherwise
   */
  public static WebRadio getCurrentRadio() {
    return FIFO.currentRadio;
  }

  /**
   * Return whether a track is being played
   * 
   * @return whether a track is being played
   */
  public static boolean isPlayingTrack() {
    return !bStop && !isPlayingRadio();
  }

  /**
   * @return a string representation for current played item or stop state
   */
  public static String getCurrentFileTitle() {
    String title = null;
    File file = FIFO.getCurrentFile();
    if (FIFO.isPlayingRadio()) {
      title = FIFO.getCurrentRadio().getName();
    } else if (file != null && !FIFO.isStopped()) {
      title = file.getHTMLFormatText();
    } else {
      title = Messages.getString("JajukWindow.18");
    }
    return title;
  }

  /*
   * Force FIFO cleanup, for example after files deletion
   */
  public static synchronized void clean() {
    Iterator<StackItem> it = alFIFO.iterator();
    while (it.hasNext()) {
      StackItem si = it.next();
      if (FileManager.getInstance().getFileByID(si.getFile().getID()) == null) {
        it.remove();
      }
    }
    computesPlanned(true);
  }

}