/*
 *  Jajuk
 *  Copyright (C) 2006 The Jajuk Team
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
 *  $1.0$
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

class ObserverRegistry {
  private final Map<JajukEvents, List<Observer>> hEventComponents = new Hashtable<JajukEvents, List<Observer>>(
      10);

  /** Number of current executions for a given event */
  private static Map<JajukEvent, Integer> canals = new HashMap<JajukEvent, Integer>(10);

  /**
   * Calls the update method for each observer <br>
   * We manage execution canals to limit the number of concurrent executions for
   * a given event type. This allow to avoid thread number explosion in some
   * error cases
   * 
   * @param event
   *          The event to execute
   */
  @SuppressWarnings("unchecked")
  void notifySync(JajukEvent event) {
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
    JajukEvents subject = event.getSubject();
    List<Observer> observers = hEventComponents.get(subject);
    if (observers == null) {
      return;
    }
    // Iterate on a cloned list to avoid concurrent exceptions
    observers = (List<Observer>) ((ArrayList<Observer>) observers).clone();
    Iterator<Observer> it = observers.iterator();
    while (it.hasNext()) {
      Observer obs = null;
      obs = it.next();
      if (obs != null) {
        try {
          obs.update(event);
        } catch (Throwable t) {
          Log.error(t);
        } finally {
          synchronized (canals) {
            int numberOfExecutions = canals.get(event);
            canals.put(event, numberOfExecutions - 1);
          }
        }
      }
    }
  }

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

  synchronized void unregister(JajukEvents subject, Observer observer) {
    List<Observer> alComponents = hEventComponents.get(subject);
    if (alComponents != null) {
      alComponents.remove(observer);
    }
  }
}
