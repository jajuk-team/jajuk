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
import java.util.Random;

import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.WebRadio;
import org.jajuk.services.core.RatingManager;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Manages playing sequences
 * <p>
 * Avoid to synchronize these methods because they are called very often and AWT
 * dispatcher thread is frozen when JVM execute a static syncrhonized method,
 * even ouside AWT dispatcher thread
 * </p>
 */
public class FIFO implements ITechnicalStrings {

  /** Currently played track index */
  private int index;

  /** Last played track */
  private StackItem itemLast;

  /** Fifo itself, contains jajuk File objects */
  private volatile ArrayList<StackItem> alFIFO;

  /** Planned tracks */
  private volatile ArrayList<StackItem> alPlanned;

  /** Stop flag* */
  private static volatile boolean bStop = false;

  /** Self instance */
  static private FIFO fifo = null;

  /** First played file flag* */
  private static boolean bFirstFile = true;

  /** Whether we are currently playing radio */
  private boolean playingRadio = false;

  /** Current played radio */
  private WebRadio currentRadio;

  /**
   * Shared mutex for locking.
   */
  public static final byte[] MUTEX = new byte[0];

  /**
   * Singleton access
   * 
   * @return
   */
  public static FIFO getInstance() {
    if (fifo == null) {
      fifo = new FIFO();
    }
    return fifo;
  }

  /**
   * constructor
   */
  private FIFO() {
    reset();
  }

  /**
   * Initialization
   */
  private void reset() {
    alFIFO = new ArrayList<StackItem>(50);
    alPlanned = new ArrayList<StackItem>(10);
    JajukTimer.getInstance().reset();
    index = 0;
    itemLast = null;
  }

  /**
   * Set given repeat mode to all in FIFO
   * 
   * @param bRepeat
   */
  public void setRepeatModeToAll(boolean bRepeat) {
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
  public void push(final List<StackItem> alItems, final boolean bAppend) {
    Thread t = new Thread() { // do it in a thread to make UI more
      // reactive
      public void run() {
        try {
          Util.waiting();
          pushCommand(alItems, bAppend);
        } catch (Exception e) {
          Log.error(e);
        } finally {
          // refresh playlist editor
          ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
          Util.stopWaiting();
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
  public void push(final StackItem item, final boolean bAppend) {
    Thread t = new Thread() {
      // do it in a thread to make UI more reactive
      public void run() {
        try {
          Util.waiting();
          pushCommand(item, bAppend);
        } catch (Exception e) {
          Log.error(e);
        } finally {
          // refresh playlist editor
          ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
          Util.stopWaiting();
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
  public void launchRadio(WebRadio radio) {
    try {
      Util.waiting();
      currentRadio = radio;
      // Play the stream
      boolean bPlayOK = Player.play(radio);
      if (bPlayOK) { // refresh covers if play is started
        Log.debug("Now playing :" + radio.toString());
        playingRadio = true;
        // Store current radio for next startup
        ConfigurationManager.setProperty(CONF_DEFAULT_WEB_RADIO, radio.getName());
        // Send an event that a track has been launched
        Properties pDetails = new Properties();
        pDetails.put(DETAIL_CONTENT, radio);
        // reset all UI
        ObservationManager.notify(new Event(EventSubject.EVENT_ZERO));
        ObservationManager.notify(new Event(EventSubject.EVENT_WEBRADIO_LAUNCHED, pDetails));
      }
    } catch (Throwable t) {// catch even Errors (OutOfMemory for example)
      Log.error(122, t);
      playingRadio = false;
    } finally {
      Util.stopWaiting(); // stop the waiting cursor
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
  private void pushCommand(List<StackItem> alItems, boolean bAppend) {
    try {
      // wake up FIFO if stopped
      bStop = false;
      // display an error message if selection is void
      if (alItems.size() == 0) {
        // If current ambience is not "all", show selected ambience
        // to alert user he selected it
        if (AmbienceManager.getInstance().getSelectedAmbience() == null) {
          Messages.showWarningMessage(Messages.getString("Error.018"));
        } else {
          Messages.showWarningMessage(Messages.getString("Error.164") + " "
              + AmbienceManager.getInstance().getSelectedAmbience().getName());
        }
      }
      // first try to mount needed devices
      Iterator<StackItem> it = alItems.iterator();
      StackItem item = null;
      boolean bNoMount = false;
      while (it.hasNext()) {
        item = it.next();
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
                item.getFile().getDirectory().getDevice().mount();
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
      synchronized (this) {
        // test if we have yet some files to consider
        if (alItems.size() == 0) {
          return;
        }
        // OK, stop current track if no append
        if (!bAppend) {
          Player.stop(false);
          clear();
          JajukTimer.getInstance().reset();
        }
        // add required tracks in the FIFO
        it = alItems.iterator();
        while (it.hasNext()) {
          item = it.next();
          // Apply contextual repeat mode but only for consecutive
          // repeat tracks : we can't have a whole between
          // repeated tracks and first track must be repeated
          if (ConfigurationManager.getBoolean(CONF_STATE_REPEAT)) {
            // check if last in fifo is repeated
            if (getLast() == null) { // this item will be the
              // first
              item.setRepeat(true);
            } else { // there are yet some tracks in fifo
              if (getLast().isRepeat()) {
                item.setRepeat(true);
              } else {
                item.setRepeat(false);
              }
            }
          }// else, can be repeat (forced repeat) or not
          alFIFO.add(item);
          JajukTimer.getInstance().addTrackTime(item.getFile());
        }
        // launch track if required
        if (!bAppend || !Player.isPlaying()) {
          // if we have a play or nothing is playing
          index = 0;
          launch(index);
        }
        // computes planned tracks
        computesPlanned(true);
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Push some files in the fifo
   * 
   * @param item,
   *          item to be played
   * @param bAppend
   *          keep previous files or stop them to start a new one ?
   */
  private void pushCommand(StackItem item, boolean bAppend) {
    ArrayList<StackItem> alFiles = new ArrayList<StackItem>(1);
    alFiles.add(item);
    pushCommand(alFiles, bAppend);
  }

  /**
   * Finished method, called by the PlayerImpl when the track is finished
   * 
   */
  public void finished() {
    try {
      // If no playing item, just leave
      if (getCurrentItem() == null) {
        return;
      }
      Properties details = new Properties();
      details.put(DETAIL_CURRENT_FILE, getCurrentFile());
      ObservationManager.notify(new Event(EventSubject.EVENT_FILE_FINISHED, details));
      if (getCurrentItem().isRepeat()) {
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
        if (ConfigurationManager.getBoolean(CONF_STATE_CONTINUE) && itemLast != null) {
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
            ObservationManager.notify(new Event(EventSubject.EVENT_ZERO));
          }
        } else {
          // no ? just reset UI and leave
          JajukTimer.getInstance().reset();
          bStop = true;
          ObservationManager.notify(new Event(EventSubject.EVENT_ZERO));
          return;
        }
      } else {
        // something more in FIFO
        launch(index);
      }
      // computes planned tracks
      computesPlanned(false);
    } catch (Exception e) {
      Log.error(e);
    } finally {
      // refresh playlist editor
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
    }
  }

  /**
   * Lauch track at given index in the fifo
   * 
   * @param int
   *          index
   */
  private void launch(int index) {
    try {
      Util.waiting();
      // intro workaround : intro mode is only read at track launch
      // and can't be set during the play
      ConfigurationManager.getBoolean(CONF_STATE_INTRO);
      // notify to devices like commandJPanel to update ui when the play
      // button has been pressed
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYER_PLAY));
      // set was_playing state
      ConfigurationManager.setProperty(CONF_STATE_WAS_PLAYING, TRUE);
      File fCurrent = getCurrentFile();
      boolean bPlayOK = false;
      if (bFirstFile && !ConfigurationManager.getBoolean(CONF_STATE_INTRO)
          && ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST_KEEP_POS)) {
        // if it is the first played file of the session and we are in
        // startup mode keep position
        float fPos = ConfigurationManager.getFloat(CONF_STARTUP_LAST_POSITION);
        // play it
        bPlayOK = Player.play(fCurrent, fPos, TO_THE_END);
      } else {
        if (ConfigurationManager.getBoolean(CONF_STATE_INTRO)) {
          // intro mode enabled
          bPlayOK = Player.play(fCurrent, Float.parseFloat(ConfigurationManager
              .getProperty(CONF_OPTIONS_INTRO_BEGIN)) / 100, 1000 * Integer
              .parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_LENGTH)));
        } else {
          // play it
          bPlayOK = Player.play(fCurrent, 0.0f, TO_THE_END);
        }
      }

      if (bPlayOK) {
        Log.debug("Now playing :" + fCurrent);
        // Send an event that a track has been launched
        Properties pDetails = new Properties();
        pDetails.put(DETAIL_CURRENT_FILE_ID, fCurrent.getID());
        pDetails.put(DETAIL_CURRENT_DATE, new Long(System.currentTimeMillis()));
        ObservationManager.notify(new Event(EventSubject.EVENT_FILE_LAUNCHED, pDetails));
        // all cases for a cover full refresh
        if (itemLast == null // first track, display cover
            // if we are always in the same directory, just leave to
            // save cpu
            || (!itemLast.getFile().getDirectory().equals(fCurrent.getDirectory()))) {
          // clear image cache
          Util.clearCache();
          // request update cover
          ObservationManager.notify(new Event(EventSubject.EVENT_COVER_REFRESH));
        }
        // case just for a cover change without reload
        else if ((ConfigurationManager.getBoolean(CONF_COVERS_SHUFFLE))) {
          // request update cover
          ObservationManager.notify(new Event(EventSubject.EVENT_COVER_CHANGE));
        }
        // save the last played track (even files in error are stored here as
        // we need this for computes next track to launch after an error)
        // We have to set this line here as we make directory change analyse
        // before for cover change
        itemLast = (StackItem) getCurrentItem().clone();
        playingRadio = false;
        bFirstFile = false;
        // add hits number
        fCurrent.getTrack().incHits(); // inc hits number
        fCurrent.getTrack().incSessionHits();// inc session hits
        RatingManager.setRateHasChanged(true);
      } else {
        // Problem launching the track, try next one
        try {
          Thread.sleep(WAIT_AFTER_ERROR);
        } catch (InterruptedException e) {
          Log.error(e);
        }
        // save the last played track (even files in error are stored here as
        // we need this for computes next track to launch after an error)
        itemLast = (StackItem) getCurrentItem().clone();
        FIFO.getInstance().finished();
      }
    } catch (Throwable t) {// catch even Errors (OutOfMemory for exemple)
      Log.error(122, t);
    } finally {
      Util.stopWaiting(); // stop the waiting cursor
    }
  }

  /**
   * Set current index
   * 
   * @param index
   */
  public void setIndex(int index) {
    this.index = index;
  }

  /**
   * Computes planned tracks
   * 
   * @param bClear :
   *          clear planned tracks stack
   */
  public void computesPlanned(boolean bClear) {
    // Check if we are in continue mode and we have some tracks in FIFO, if
    // not : no planned tracks
    if (!ConfigurationManager.getBoolean(CONF_STATE_CONTINUE) || alFIFO.size() == 0) {
      alPlanned.clear();
      return;
    }
    if (bClear) {
      alPlanned.clear();
    }
    int iPlannedSize = alPlanned.size();
    // Add required tracks
    for (int i = 0; i < (ConfigurationManager.getInt(CONF_OPTIONS_VISIBLE_PLANNED) - iPlannedSize); i++) {
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
        // if random mode, add shuffle tracks
        if (ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE)) {
          item = new StackItem(FileManager.getInstance().getShuffleFile(), false);
        } else {
          // if fifo contains yet some tracks to play
          if (siLast != null) {
            item = new StackItem(FileManager.getInstance().getNextFile(siLast.getFile()), false);
          } else { // nothing in fifo, take first files in
            // collection
            File file = (File) FileManager.getInstance().getFiles().toArray()[i];
            item = new StackItem(file, false);
          }
        }
      } catch (JajukException je) {
        // can be thrown if FileManager return a null file (like when
        // reaching end of collection)
        break;
      }
      // Tell it is a planned item
      item.setPlanned(true);
      // add the new item
      alPlanned.add(item);
    }
  }

  /**
   * Clears the fifo, for example when we want to add a group of files stopping
   * previous plays
   * 
   */
  public void clear() {
    alFIFO.clear();
    alPlanned.clear();
  }

  /**
   * @return whether the FIFO contains at least one track in repeat mode
   */
  public boolean containsRepeat() {
    Iterator it = alFIFO.iterator();
    boolean bRepeat = false;
    while (it.hasNext()) {
      StackItem item = (StackItem) it.next();
      if (item.isRepeat()) {
        bRepeat = true;
      }
    }
    return bRepeat;
  }

  /**
   * 
   * @return whether the FIFO contains only repeated files
   */
  public boolean containsOnlyRepeat() {
    Iterator it = alFIFO.iterator();
    boolean bOnlyRepeat = true;
    while (it.hasNext()) {
      StackItem item = (StackItem) it.next();
      if (!item.isRepeat()) {
        bOnlyRepeat = false;
        break;
      }
    }
    return bOnlyRepeat;
  }

  /**
   * Get previous track, can add item in first index of FIFO
   * 
   * @return new index of current file
   * @throws Exception
   */
  private int addPrevious() throws Exception {
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
              (alFIFO.get(0)).getFile()), ConfigurationManager.getBoolean(CONF_STATE_REPEAT), true);
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
  public void playPrevious() {
    try {
      // if playing, stop all playing players
      if (Player.isPlaying()) {
        Player.stop(true);
      }
      JajukTimer.getInstance().reset();
      JajukTimer.getInstance().addTrackTime(alFIFO);
      launch(addPrevious());
    } catch (Exception e) {
      Log.error(e);
    } finally {
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
    }
  }

  /**
   * Play previous album
   */
  public void playPreviousAlbum() {
    try {
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
        int index = addPrevious();
        Directory dirTested = null;
        if (alFIFO.get(index) == null) {
          return;
        } else {
          File file = alFIFO.get(index).getFile();
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
      launch(index);
    } catch (Exception e) {
      Log.error(e);
    } finally {
      // Refresh playlist editor
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
    }
  }

  /**
   * Play next track in selection
   */
  public void playNext() {
    try {
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
        pushCommand(new StackItem(FileManager.getInstance().getShuffleFile(), ConfigurationManager
            .getBoolean(CONF_STATE_REPEAT), false), false);
      }
    } catch (Exception e) {
      Log.error(e);
    } finally {
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
    }
  }

  /**
   * Play next track in selection
   */
  public void playNextAlbum() {
    try {
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
          ArrayList<StackItem> alFake = new ArrayList<StackItem>(1);
          alFake.add(getItem(0));
          insert(alFake, 0);
          finished(); // stop current track and start the new one
        } else {// void fifo, add next album
          File fileNext = itemLast.getFile();
          do {
            fileNext = FileManager.getInstance().getNextFile(fileNext);
            // look for the next different album
            if (fileNext != null && !fileNext.getDirectory().equals(dir)) {
              pushCommand(new StackItem(fileNext, ConfigurationManager
                  .getBoolean(CONF_STATE_REPEAT), false), false); // play
              // it
              return;
            }
          } while (fileNext != null);
        }
      } else if (itemLast != null) { // try to launch any previous
        // file
        pushCommand(itemLast, false);
      } else { // really nothing? play a shuffle track from collection
        pushCommand(new StackItem(FileManager.getInstance().getShuffleFile(), ConfigurationManager
            .getBoolean(CONF_STATE_REPEAT), false), false);
      }
    } catch (Exception e) {
      Log.error(e);
    } finally {
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
    }
  }

  /**
   * Get the currently played file or null if no playing file
   * 
   * @return File
   */
  public File getCurrentFile() {
    StackItem item = getCurrentItem();
    return (item == null) ? null : item.getFile();
  }

  /**
   * Get the currently played stack item or null if no playing item
   * 
   * @return stack item
   */
  public StackItem getCurrentItem() {
    if (index < alFIFO.size()) {
      StackItem item = alFIFO.get(index);
      return item;
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
  public StackItem getItem(int index) {
    return alFIFO.get(index);
  }

  /**
   * Get index of the last repeated item, -1 if none repeated
   * 
   * @return index
   */
  private int getLastRepeatedItem() {
    int i = -1;
    Iterator iterator = alFIFO.iterator();
    while (iterator.hasNext()) {
      StackItem item = (StackItem) iterator.next();
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
    if (fifo == null || !Player.isPlaying() || getInstance().getCurrentFile() == null) { // currently
      // stopped
      return true;
    }
    if (getInstance().getCurrentFile().getDirectory().getDevice().equals(device)) { // is
      // current
      // track
      // on
      // this
      // device?
      return false;
    }
    Iterator it = getInstance().alFIFO.iterator(); // are next tracks in
    // fifo on this device?
    while (it.hasNext()) {
      StackItem item = (StackItem) it.next();
      File file = item.getFile();
      if (file.getDirectory().getDevice().equals(device)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Stop request. Void the fifo
   */
  public void stopRequest() {
    // fifo is over ( stop request ) , reinit labels in information panel
    // before exiting
    bStop = true;
    // set was playing state if it is not a stop called by jajuk exit
    if (!Main.isExiting()) {
      ConfigurationManager.setProperty(CONF_STATE_WAS_PLAYING, FALSE);
    }
    reset(); // reinit all variables
    Player.stop(true); // stop player
    // notify views like commandJPanel to update ui
    ObservationManager.notify(new Event(EventSubject.EVENT_PLAYER_STOP));
    // reset request
    ObservationManager.notify(new Event(EventSubject.EVENT_ZERO));
  }

  /**
   * @return Returns the bStop.
   */
  public static boolean isStopped() {
    return bStop;
  }

  /**
   * @return Returns a shallow copy of the fifo
   */
  @SuppressWarnings("unchecked")
  public ArrayList<StackItem> getFIFO() {
    return (ArrayList<StackItem>) alFIFO.clone();
  }

  /**
   * Not for performance reasons
   * 
   * @return FIFO size (do not use getFIFO().size() for performance reasons)
   */
  public int getFIFOSize() {
    return alFIFO.size();
  }

  /**
   * Shuffle the FIFO, used when user select the Random mode
   */
  public void shuffle() {
    if (alFIFO.size() > 1) {
      // Make sure current track is kept to its position
      // so remove it and add it again after shuffling
      alFIFO.remove(0);
      Collections.shuffle(alFIFO, new Random());
      alFIFO.add(0, itemLast);
    }
    alPlanned.clear(); // force recomputes planned tracks
  }

  /**
   * Insert a file to play in FIFO at specified position
   * 
   * @param file
   * @param iPos
   */
  public void insert(StackItem item, int iPos) {
    ArrayList<StackItem> alStack = new ArrayList<StackItem>(1);
    alStack.add(item);
    insert(alStack, iPos);
  }

  /**
   * Insert a file at specified position, any existing item at this position is
   * shifted on the right
   * 
   * @param file
   * @param iPos
   */
  public void insert(ArrayList<StackItem> alFiles, int iPos) {
    if (iPos <= alFIFO.size()) {
      // add in the FIFO, accept a file at
      // size() position to allow increasing
      // FIFO at the end
      alFIFO.addAll(iPos, alFiles);
      JajukTimer.getInstance().addTrackTime(alFiles);
    }
    computesPlanned(false);
  }

  /**
   * Put up an item from given index to index-1
   * 
   * @param index
   */
  public void up(int index) {
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
  public void down(int index) {
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
  public void goTo(int pIndex) {
    int index = pIndex;
    try {
      if (containsRepeat()) {
        // if there are some tracks in repeat, mode
        if (getItem(index).isRepeat()) {
          // the selected line is in repeat mode, ok,
          // keep repeat mode and just change index
          this.index = index;
        } else {
          // the selected line was not a repeated item,
          // take it as a which to reset repeat mode
          setRepeatModeToAll(false);
          Properties properties = new Properties();
          properties.put(DETAIL_SELECTION, FALSE);
          ObservationManager.notify(new Event(EventSubject.EVENT_REPEAT_MODE_STATUS_CHANGED,
              properties));
          remove(0, index - 1);
          index = 0;
        }
      } else {
        remove(0, index - 1);
        index = 0;
      }
      // need to stop before launching! this fix a
      // wrong EOM event in BasicPlayer
      Player.stop(false);
      launch(index);
    } catch (Exception e) {
      Log.error(e);
    } finally {
      // refresh playlist editor
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
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
  public void remove(int iStart, int iStop) {
    if (iStart <= iStop && iStart >= 0 && iStop < alFIFO.size() + alPlanned.size()) {
      // check size drop items from the end to the begining
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
  public StackItem getLast() {
    if (alFIFO.size() == 0) {
      return null;
    }
    return alFIFO.get(alFIFO.size() - 1);
  }

  /**
   * 
   * @return Last played item
   */
  public StackItem getLastPlayed() {
    return itemLast;
  }

  /**
   * @return Returns the index.
   */
  public int getIndex() {
    return index;
  }

  /**
   * @return Returns a shallow copy of planned files
   */
  @SuppressWarnings("unchecked")
  public ArrayList<StackItem> getPlanned() {
    return (ArrayList<StackItem>) alPlanned.clone();
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
  public void cleanDevice(Device device) {
    if (alFIFO.size() > 0) {
      ArrayList<StackItem> alFIFOCopy = (ArrayList<StackItem>) alFIFO.clone();
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
  public void commit() throws IOException {
    java.io.File file = Util.getConfFileByPath(FILE_FIFO);
    PrintWriter writer = new PrintWriter(
        new BufferedOutputStream(new FileOutputStream(file, false)));
    int index = 0;
    for (StackItem st : alFIFO) {
      if (index > 0) {
        // do not store current track (otherwise, it
        // will be duplicate at startup)
        writer.println(st.getFile().getID());
      }
      index++;
    }
    writer.flush();
    writer.close();
  }

  public boolean isPlayingRadio() {
    return this.playingRadio;
  }

  public WebRadio getCurrentRadio() {
    return this.currentRadio;
  }
}
