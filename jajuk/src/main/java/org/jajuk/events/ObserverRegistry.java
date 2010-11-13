/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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

package org.jajuk.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * Registry of Observers for each of the JajukEvents. Used by ObservationManager
 * to handle informing observers about events happening in other objects.
 */
class ObserverRegistry {

  /** The list of Observers per JajukEvents. */
  private final Map<JajukEvents, List<Observer>> hEventComponents = new Hashtable<JajukEvents, List<Observer>>(
      10);

  /** Number of current executions for a given event. */
  private static Map<JajukEvent, Integer> canals = new HashMap<JajukEvent, Integer>(10);

  /**
   * Calls the update method for each observer <br>
   * We manage execution canals to limit the number of concurrent executions for
   * a given event type. This allow to avoid thread number explosion in some
   * error cases
   * 
   * @param event The event to execute
   */
  @SuppressWarnings("unchecked")
  void notifySync(JajukEvent event) {
    // do the synchronization on canals in two parts to do the "update" without
    // holding the lock
    synchronized (canals) {
      int numberOfExecutions = 0;
      if (canals.containsKey(event)) {
        numberOfExecutions = canals.get(event);
      }
      if (numberOfExecutions > Const.MAX_EVENT_EXECUTIONS) {
        Log.warn("Event overflow for : " + event);
        return;
      }
      canals.put(event, numberOfExecutions + 1);
    }

    try {
      JajukEvents subject = event.getSubject();
      List<Observer> observers = hEventComponents.get(subject);
      if (observers == null) {
        return;
      }
      // Iterate on a cloned list to avoid concurrent exceptions
      observers = (List<Observer>) ((ArrayList<Observer>) observers).clone();
      Iterator<Observer> it = observers.iterator();
      while (it.hasNext()) {
        Observer obs = it.next();
        if (obs != null) {
          try {
            obs.update(event);
          } catch (Throwable t) {
            Log.error(t);
          }
        }
      }
    } finally {
      synchronized (canals) {
        int numberOfExecutions = canals.get(event);
        assert (numberOfExecutions > 0);
        canals.put(event, numberOfExecutions - 1);

        // to avoid adding more and more memory via the canals-map, we should remove items when they
        // reach zero again
        // the effect on memory is rather small, but it shows up after some time in memory profiles
        // nevertheless.
        if (canals.get(event) == 0) {
          canals.remove(event);
        }
      }
    }
  }

  /**
   * Register an Observer for an event.
   * 
   * @param subject The event to register for.
   * @param observer The Observer that should be informed about
   * the event as soon as it is reported somewhere else.
   */
  synchronized void register(JajukEvents subject, Observer observer) {
    List<Observer> observers = hEventComponents.get(subject);
    if (observers == null) {
      observers = new ArrayList<Observer>(1);
      hEventComponents.put(subject, observers);
    }
    // Add the observer, if it is a high priority observer, put it first in
    // queue
    if (!observers.contains(observer)) {
      if (observer instanceof HighPriorityObserver) {
        observers.add(0, observer);
      } else {
        observers.add(observer);
      }
    }
  }

  /**
   * Unregister the Observer from an event.
   * 
   * @param subject The event to unregister from.
   * @param observer The Observer that is no longer interested in this event.
   * 
   * @return true if the event was unregistered, false if it was not
   * registered (any more) and thus did not need to be removed
   */
  synchronized boolean unregister(JajukEvents subject, Observer observer) {
    List<Observer> alComponents = hEventComponents.get(subject);
    if (alComponents != null) {
      return alComponents.remove(observer);
    }
    return false;
  }

  /**
   * Remove any registered item. This is mainly used in UnitTests to
   * get a clean state again.
   */
  synchronized public void clear() {
    hEventComponents.clear();
    synchronized (canals) {
      canals.clear();
    }
  }
}
