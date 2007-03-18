/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

package org.jajuk.base;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

/**
 * This is a mediator managing relationships between subjets and observers
 * <p>
 * All notification methods are synchronized to assure event order
 * 
 * @author Bertrand Florat
 * @created 12 dec. 2003
 */
public class ObservationManager implements ITechnicalStrings {

	/** one event -> list of components */
	static ObserverRegistry observerRegistry = new ObserverRegistry();

	/**
	 * Last event for a given subject (used for new objects that just
	 * registrated to this subject)
	 */
	static HashMap<EventSubject, Properties> hLastEventBySubject = new HashMap<EventSubject, Properties>(
			10);

	static volatile Vector<Event> vFIFO = new Vector<Event>(10);

	static private Thread t = new Thread() {
		public void run() {
			while (true) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Log.error(e);
				}
				if (vFIFO.size() > 0) {
					final Event event = vFIFO.get(0);
					vFIFO.remove(0);
					new Thread() { // launch action asynchronously
						public void run() {
							notifySync(event);
						}
					}.start();
				}
			}
		}
	};

	/**
	 * Register a component for a given subject
	 * 
	 * @param subject
	 *            Subject ( event ) to observe
	 * @param jc
	 *            component to register
	 */
	public static synchronized void register(Observer observer) {
		Set<EventSubject> eventSubjectSet = observer.getRegistrationKeys();
		for (EventSubject subject : eventSubjectSet) {
			Log.debug("Register: \"" + subject + "\" by: " + observer); //$NON-NLS-1$ //$NON-NLS-2$
			observerRegistry.register(subject, observer);
		}
	}

	/**
	 * Unregister a component for a given subject
	 * 
	 * @param subject
	 *            Subject ( event ) to observe
	 * @param jc
	 *            component to deregister
	 */
	public static boolean unregister(EventSubject subject, Observer observer) {
		return observerRegistry.unregister(subject, observer);
	}

	/**
	 * Notify all components having registered for the given subject
	 * 
	 * @param subject
	 */
	public static void notify(Event event) {
		notify(event, false); // asynchronous notification by default to avoid
		// exception throw in
		// the register current thread
	}

	/**
	 * Notify synchronously all components having registered for the given
	 * subject
	 * 
	 * @param subject
	 */
	public static void notifySync(Event event) {
		EventSubject subject = event.getSubject();
		Log.debug("Notify: " + subject); //$NON-NLS-1$
		// save last event
		hLastEventBySubject.put(subject, event.getDetails());
		observerRegistry.notifySync(event);
	}

	/**
	 * Return whether the event already occured at least once
	 * 
	 * @param subject
	 * @return
	 */
	public static boolean containsEvent(EventSubject subject) {
		return hLastEventBySubject.containsKey(subject);
	}

	/**
	 * Notify all components having registered for the given subject
	 * asynchronously
	 * 
	 * @param subject
	 * @param whether
	 *            the notification is synchronous or not
	 */
	public static void notify(final Event event, boolean bSync) {
		if (bSync) {
			ObservationManager.notifySync(event);
		} else { // do not launch it in a regular thread because AWT
			// event dispatcher waits thread end to display
			if (!t.isAlive()) {
				t.start();
			}
			vFIFO.add(event); // add event in FIFO fo futur use
		}
	}

	/**
	 * Return the details for last event of the given subject, or null if there
	 * is no details
	 * 
	 * @param sEvent
	 *            event name
	 * @param sDetail
	 *            Detail name
	 * @return the detail as an object or null if the event or the detail
	 *         doesn't exist
	 */
	public static Object getDetailLastOccurence(EventSubject subject,
			String sDetailName) {
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
	 *            event name
	 * @param sDetail
	 *            Detail name
	 * @return the detail as an object or null if the event or the detail
	 *         doesn't exist
	 */
	public static Object getDetail(Event event, String sDetailName) {
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
	 *            event name
	 * @return the detaisl or null there are not details
	 */
	public static Properties getDetailsLastOccurence(EventSubject subject) {
		return hLastEventBySubject.get(subject);
	}
}
