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

package org.jajuk.services.events;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;

import org.jajuk.util.EventSubject;
import org.jajuk.util.log.Log;

class ObserverRegistry {
  private Hashtable<EventSubject, ArrayList<Observer>> hEventComponents = new Hashtable<EventSubject, ArrayList<Observer>>(
      10);

  @SuppressWarnings("unchecked")
  synchronized void notifySync(Event event) {
    EventSubject subject = event.getSubject();
    ArrayList<Observer> alComponents = hEventComponents.get(subject);
    if (alComponents == null) {
      return;
    }
    // try to avoid duplicate key exceptions
    alComponents = (ArrayList<Observer>) alComponents.clone();
    Iterator<Observer> it = alComponents.iterator();
    while (it.hasNext()) {
      Observer obs = null;
      try {
        obs = it.next();
        if (obs != null) {
          try {
            obs.update(event);
          } catch (Throwable t) {
            Log.error(t);
          }
        }
      }
      // Concurrent exceptions can occur for unknown reasons
      catch (ConcurrentModificationException ce) {
        ce.printStackTrace();
        Log.debug("Concurrent exception for subject: " + subject + " on observer: " + obs);
      }
    }
  }

  synchronized boolean register(EventSubject subject, Observer observer) {
    ArrayList<Observer> alComponents = hEventComponents.get(subject);
    if (alComponents == null) {
      alComponents = new ArrayList<Observer>(1);
      hEventComponents.put(subject, alComponents);
    }
    if (!alComponents.contains(observer)) {
      return alComponents.add(observer);
    }
    return false;
  }

  synchronized boolean unregister(EventSubject subject, Observer observer) {
    ArrayList alComponents = hEventComponents.get(subject);
    if (alComponents != null) {
      return alComponents.remove(observer);
    }
    return false;
  }
}
