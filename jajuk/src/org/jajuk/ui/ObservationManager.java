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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;

/**
 *  This is a mediator managing relationships between subjets and observers 
 * @author     bflorat
 * @created    12 dec. 2003
 */
public class ObservationManager {

	/** one event -> list of components , we use a synchronized collection to avoid unexpected concurrent modifications*/
	static Hashtable hEventComponents = new Hashtable(10);
	
	/**
	 * Register a component for a given subject
	 * @param subject Subject ( event ) to observe
	 * @param jc component to register
	 */
	public static synchronized void register(String subject,JComponent jc){
		Vector  vComponents = (Vector)hEventComponents.get(subject);
		if (vComponents == null){
			vComponents = new Vector(1);
			hEventComponents.put(subject,vComponents);
		}
		vComponents.add(jc);
	}
	
	/**
	 * Unregister a component for a given subject
	 * @param subject Subject ( event ) to observe
	 * @param jc component to deregister
	 */
	public static synchronized void unregister(String subject,JComponent jc){
		Vector vComponents = (Vector)hEventComponents.get(subject);
		if (vComponents == null){
			vComponents.remove(jc);
		}
	}

	/**
	 * Notify all components having registered for the given subject
	 * @param subject
	 */
	public static synchronized void notify(final String subject){
		new Thread(){
			public synchronized void run(){
				Vector vComponents =(Vector)hEventComponents.get(subject); 
				if (vComponents == null){
					return;
				}
				Iterator it = vComponents.iterator();  
				while (it.hasNext()){
					Observer obs = (Observer)it.next();
					obs.update(subject);
				}
			}
		}.start();
				
	}
}
