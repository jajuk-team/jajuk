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
 *  $Revision:3950 $ 
 */

package org.jajuk.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jajuk.services.core.ExitService;
import org.jajuk.util.log.Log;

/**
 * This is a mediator managing relationships between subjects and observers
 * <p>
 * All notification methods are synchronized to assure event order
 */
public final class ObservationManager {

  /** one event -> list of components */
  static ObserverRegistry observerRegistry = new ObserverRegistry();

  /**
   * Last event for a given subject (used for new objects that just registrated
   * to this subject)
   */
  static Map<JajukEvents, Properties> hLastEventBySubject = new HashMap<JajukEvents, Properties>(10);

  /**
   * The queue itself. Must be synchronized, so we use a ConcurrentLinkedQueue
   * which is tread-safe
   */
  static volatile Queue<JajukEvent> queue = new ConcurrentLinkedQueue<JajukEvent>();

  /**
   * The observation fifo
   */
  private static ObservationManagerThread t;

  /**
   * Empty constructor to avoid instantiating this utility class
   */
  private ObservationManager() {
  }

  /**
   * Register a component for a given subject. This calls the 
   * interface @see Observer.getRegistrationKeys() to retrieve 
   * a list of events which the Observer is interested in.
   * 
   * @param subject
   *          Subject ( event ) to observe
   * @param jc
   *          component to register
   */
  public static synchronized void register(Observer observer) {
    Set<JajukEvents> eventSubjectSet = observer.getRegistrationKeys();
    for (JajukEvents subject : eventSubjectSet) {
      Log.debug("Register: \"" + subject + "\" by: " + observer);
      observerRegistry.register(subject, observer);
    }
  }

  /**
   * Unregister a component for a given subject
   * 
   * @param subject
   *          Subject ( event ) to observe
   * @param jc
   *          component to deregister
   *          
   * TODO: this seems to be not used anywhere right now?! 
   * It would probably make sense to call this when a View is closed
   * and in other places as otherwise we keep references and through 
   * this memory unnecessarily.
   */
  public static void unregister(Observer observer) {
    Set<JajukEvents> eventSubjectSet = observer.getRegistrationKeys();

    // can return null if no keys are registered
    if(eventSubjectSet == null) {
      return;
    }

    for (JajukEvents subject : eventSubjectSet) {
      Log.debug("Unregister: \"" + subject + "\" from: " + observer);
      observerRegistry.unregister(subject, observer);
    }
  }

  /**
   * Notify all components having registered for the given subject
   * 
   * @param subject
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
          if (t == null || !t.isAlive()) {
            // If the thread is terminated, a new thread must be instanciated
            // Otherwise an IllegalThreadStateException is thrown
            Log.debug("Observation Manager thread not running, start a new one");
            t = new ObservationManagerThread();
            t.start();
          }
        }
    } catch (Error e) {
      // Make sure to catch any error (Memory or IllegalThreadStateException for
      // ie, this notification musn't stop the current work)
      Log.error(e);
    }
  }

  /**
   * Notify synchronously all components having registered for the given subject
   * 
   * @param subject
   */
  public static void notifySync(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    Log.debug("Notify: " + subject);
    // save last event
    hLastEventBySubject.put(subject, event.getDetails());
    observerRegistry.notifySync(event);
  }

  /**
   * Return whether the event already occurred at least once
   * 
   * @param subject
   * @return
   */
  public static boolean containsEvent(JajukEvents subject) {
    return hLastEventBySubject.containsKey(subject);
  }

  /**
   * Return the details for last event of the given subject, or null if there is
   * no details
   * 
   * @param sEvent
   *          event name
   * @param sDetail
   *          Detail name
   * @return the detail as an object or null if the event or the detail doesn't
   *         exist
   */
  public static Object getDetailLastOccurence(JajukEvents subject, String sDetailName) {
    Properties pDetails = hLastEventBySubject.get(subject);
    if (pDetails != null) {
      return pDetails.get(sDetailName);
    }
    return null;
  }

  /**
   * Return the details for an event, or null if there is no details
   * 
   * @param sEvent
   *          event name
   * @param sDetail
   *          Detail name
   * @return the detail as an object or null if the event or the detail doesn't
   *         exist
   */
  public static Object getDetail(JajukEvent event, String sDetailName) {
    Properties pDetails = event.getDetails();
    if (pDetails != null) {
      return pDetails.get(sDetailName);
    }
    return null;
  }

  /**
   * Return the details for an event, or null if there is no details
   * 
   * @param sEvent
   *          event name
   * @return the details or null there are not details
   */
  public static Properties getDetailsLastOccurence(JajukEvents subject) {
    return hLastEventBySubject.get(subject);
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
        Thread.sleep(50);
        final JajukEvent event = ObservationManager.queue.poll();
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
