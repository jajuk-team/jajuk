/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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

package org.jajuk.ui;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JComponent;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.util.log.Log;

/**
 *  This is a mediator managing relationships between subjets and observers 
 * @author     bflorat
 * @created    12 dec. 2003
 */
public class ObservationManager implements ITechnicalStrings{
	
	/** one event -> list of components*/
	static HashMap hEventComponents = new HashMap(10);
	
	/**Map a event string with a property containing all event details*/
	static HashMap hEventDetails = new HashMap(5);
	
	/**
	 * Register a component for a given subject
	 * @param subject Subject ( event ) to observe
	 * @param jc component to register
	 */
	public static void register(String subject,Object obj){
		Log.debug("Register: \""+subject+"\" by: "+obj); //$NON-NLS-1$ //$NON-NLS-2$
		ArrayList alComponents = (ArrayList)hEventComponents.get(subject);
		if (alComponents == null){
			alComponents = new ArrayList(1);
			hEventComponents.put(subject,alComponents);
		}
		alComponents.add(obj);
	}
	
	/**
	 * Unregister a component for a given subject
	 * @param subject Subject ( event ) to observe
	 * @param jc component to deregister
	 */
	public static void unregister(String subject,JComponent jc){
		ArrayList alComponents = (ArrayList)hEventComponents.get(subject);
		if (alComponents == null){
			synchronized(alComponents){
				alComponents.remove(jc);
			}
		}
	}
	
	/**
	 * Notify all components having registered for the given subject
	 * @param subject
	 */
	public static void notify(final String subject){
		notify(subject,false); //asynchronous notification by default to avoid exception throw in the register current thread
	}
	
	/**
	 * Notify synchronously all components having registered for the given subject
	 * @param subject
	 */
	public static void notifySync(final String subject){
		if (!subject.equals(EVENT_HEART_BEAT)){ //do not show this heart beat 
		    Log.debug("Notify: "+subject); //$NON-NLS-1$
		}
		synchronized(hEventComponents){
			ArrayList alComponents =(ArrayList)hEventComponents.get(subject);
			if (alComponents == null){
				return;
			}
			synchronized(alComponents){
				Iterator it = alComponents.iterator();  
				while (it.hasNext()){
				    Observer obs = null;
				    try{
					    obs = (Observer)it.next();
					    if (obs != null){
					        try{
					            obs.update(subject);
					        }
					        catch(Exception e){
					            Log.error(e);
					        }
					    }
					}
					//Concurrent exceptions can occur for unknown reasons 
					catch(ConcurrentModificationException ce){
					    ce.printStackTrace(); 
					    Log.debug("Concurrent exception for subject: "+subject+ " on observer: "+obs);//$NON-NLS-1$ //$NON-NLS-2$ 
					}
				}
			}
		}
	}
	
	
	/**
	 * Notify all components having registered for the given subject asynchronously
	 * @param subject
	 * @param whether the notification is synchronous or not
	 */
	public static void notify(final String subject, boolean bSync){
		if (bSync){
			ObservationManager.notifySync(subject);
		}
		else{
			Thread t = new Thread(){
				public void run(){
					ObservationManager.notifySync(subject);
				}
			};
			t.start();
		}
	}
	
	/**
	 * Notify all components having registered for the given subject and giving some details
	 * @param subject
	 * @param pDetails informations about this event
	 */
	public static void notify(final String subject,Properties pDetails){
		hEventDetails.put(subject,pDetails);
		notify(subject);
	}
	
	/**
	 * Notify all components synchronously having registered for the given subject and giving some details
	 * @param subject
	 * @param pDetails informations about this event
	 */
	public static void notifySync(final String subject,Properties pDetails){
		hEventDetails.put(subject,pDetails);
		notifySync(subject);
	}
	
	
	/**
	 * Return the details for an event, or null if there is no details
	 * @param sEvent event name
	 * @param sDetail Detail name
	 * @return the detail as an object or null if the event or the detail doesn't exist
	 */
	public static Object getDetail(String sEvent,String sDetailName){
		Properties pDetails = (Properties)hEventDetails.get(sEvent);
		if (pDetails != null){
			return pDetails.get(sDetailName);
		}
		return null;
	}
	
	/**
	 * Return the details for an event, or null if there is no details
	 * @param sEvent event name
	 * @return the detaisl or null there are not details
	 */
	public static Object getDetails(String sEvent){
		return (Properties)hEventDetails.get(sEvent);
	}
	
}
