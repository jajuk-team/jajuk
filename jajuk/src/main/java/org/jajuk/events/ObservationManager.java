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

package org.jajuk.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jajuk.services.core.ExitService;
import org.jajuk.util.log.Log;

/**
 * This is a mediator managing relationships between subjects and observers
 * <p>
 * All notification methods are synchronized to assure event order.
 */
public final class ObservationManager {

  /** one event -> list of components. */
  static ObserverRegistry observerRegistry = new ObserverRegistry();

  /** Last event for a given subject (used for new objects that just registrated to this subject). */
  static Map<JajukEvents, Properties> hLastEventBySubject = new HashMap<JajukEvents, Properties>(10);

  /** The queue itself. Must be synchronized, so we use a ConcurrentLinkedQueue which is thread-safe */
  static BlockingQueue<JajukEvent> queue = new LinkedBlockingQueue<JajukEvent>();

  /** The observation fifo. */
  private static ObservationManagerThread observationThread;

  /**
   * Empty constructor to avoid instantiating this utility class.
   */
  private ObservationManager() {
  }

  /**
   * Register a component for a list of subjects. This calls the
   * interface @see Observer.getRegistrationKeys() to retrieve
   * a list of events which the Observer is interested in.
   * 
   * @param observer The Observer to register.
   */
  public static synchronized void register(Observer observer) {
    Set<JajukEvents> eventSubjectSet = observer.getRegistrationKeys();
    for (JajukEvents subject : eventSubjectSet) {
      Log.debug("Register: \"" + subject + "\" by: " + observer);
      observerRegistry.register(subject, observer);
    }
  }

  /**
   * Unregister a component for a list of subjects.
   * 
   * @param observer The Observer to unregister.
   * 
   * @see Observer.getRegistrationKeys() is called on the Observer
   * to get the list of events.
   */
  public static synchronized void unregister(Observer observer) {
    Set<JajukEvents> eventSubjectSet = observer.getRegistrationKeys();

    // can return null if no keys are registered
    if (eventSubjectSet == null) {
      return;
    }

    for (JajukEvents subject : eventSubjectSet) {
      boolean bRemoved = observerRegistry.unregister(subject, observer);
      if (bRemoved) {
        Log.debug("Unregister: \"" + subject + "\" from: " + observer);
      }
    }
  }

  /**
   * Notify all components having registered for the given subject.
   * 
   * @param event The event that is triggered including any additional
   * data that is of interest as part of the event.
   */
  public static void notify(JajukEvent event) {
    // asynchronous notification by default to avoid
    // exception throw in the register current thread
    try {
      /*
       * do not launch it in a regular thread because EDT waits thread end to
       * display
       */
      queue.add(event);
      // synchronize here to avoid creating more than one observation manager
      // thread
      synchronized (ObservationManager.class) {
        if (observationThread == null || !observationThread.isAlive()) {
          // If the thread is terminated, a new thread must be instantiated
          // Otherwise an IllegalThreadStateException is thrown
          Log.debug("Observation Manager thread not running, start a new one");
          observationThread = new ObservationManagerThread();
          observationThread.start();
        }
      }
    } catch (Error e) {
      // Make sure to catch any error (Memory or IllegalThreadStateException for
      // ie, this notification musn't stop the current work)
      Log.error(e);
    }
  }

  /**
   * Notify synchronously all components having registered for the given subject.
   * 
   * @param event The event that is triggered including any additional
   * data that is of interest as part of the event.
   */
  public static void notifySync(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    Log.debug("Notify: " + subject);
    // save last event
    hLastEventBySubject.put(subject, event.getDetails());
    observerRegistry.notifySync(event);
  }

  /**
   * Return whether the event already occurred at least once.
   * 
   * @param subject The type of event to check for.
   * 
   * @return true, if contains event, false otherwise.
   */
  public static boolean containsEvent(JajukEvents subject) {
    return hLastEventBySubject.containsKey(subject);
  }

  /**
   * Return the details for last event of the given subject, or null if there is
   * no details.
   * 
   * @param subject The type of event to check for.
   * @param sDetailName The detailed piece of information to fetch.
   * 
   * @return the detail as an object or null if the event or the detail doesn't
   * exist
   */
  public static Object getDetailLastOccurence(JajukEvents subject, String sDetailName) {
    Properties pDetails = hLastEventBySubject.get(subject);
    if (pDetails != null) {
      return pDetails.get(sDetailName);
    }
    return null;
  }

  /**
   * Return the details for an event, or null if there is no details.
   * 
   * @param event The event to retrieve the detail from.
   * @param sDetailName The detailed piece of information to fetch.
   * 
   * @return the detail as an object or null if the event or the detail doesn't
   * exist
   */
  public static Object getDetail(JajukEvent event, String sDetailName) {
    Properties pDetails = event.getDetails();
    if (pDetails != null) {
      return pDetails.get(sDetailName);
    }
    return null;
  }

  /**
   * Return the details for an event, or null if there is no details.
   * 
   * @param subject The event to query for.
   * 
   * @return the details or null there are not details
   */
  public static Properties getDetailsLastOccurence(JajukEvents subject) {
    return hLastEventBySubject.get(subject);
  }

  /**
   * Remove all registered Observers. This is mainly used in Unit Tests
   * to get a clean state again.
   */
  public static void clear() {
    hLastEventBySubject.clear();
    queue.clear();
    observerRegistry.clear();
  }
}

/**
 * Observation manager thread that consumes events asynchronously
 */
class ObservationManagerThread extends Thread {

  ObservationManagerThread() {
    super("Observation Manager Thread");
  }

  @Override
  public void run() {
    // Stop to execute events is thread flag is set or if Jajuk is exiting
    while (!ExitService.isExiting()) {
      try {
        final JajukEvent event = ObservationManager.queue.poll(1000, TimeUnit.MILLISECONDS);
        if (event != null) {
          // launch action asynchronously
          new Thread("Event Executor for: " + event.toString()) {
            @Override
            public void run() {
              ObservationManager.notifySync(event);
            }
          }.start();
        }
        // Make sure to handle any exception or error to avoid the observation
        // system to die. Throwable covers all types of Exceptions/Errors.
      } catch (Throwable e) {
        Log.error(e);
      }
    }
  }
}
