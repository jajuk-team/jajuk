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
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JComponent;

/**
 *  This is a mediator managing relationships between subjets and observers 
 *
 * @author     bflorat
 * @created    12 déc. 2003
 */
public class ObservationManager {

	/** one event -> list of components */
	static HashMap hmEventComponents = new HashMap(10);
	
	/**
	 * Register a component for a given subject
	 * @param subject Subject ( event ) to observe
	 * @param jc component to register
	 */
	public static synchronized void register(String subject,JComponent jc){
		ArrayList alComponents = (ArrayList)hmEventComponents.get(subject);
		if (alComponents == null){
			alComponents = new ArrayList(1);
			hmEventComponents.put(subject,alComponents);
		}
		alComponents.add(jc);
	}
	
	/**
	 * Deregister a component for a given subject
	 * @param subject Subject ( event ) to observe
	 * @param jc component to deregister
	 */
	public static synchronized void deregister(String subject,JComponent jc){
		ArrayList alComponents = (ArrayList)hmEventComponents.get(subject);
		if (alComponents == null){
			alComponents.remove(jc);
		}
	}

	/**
	 * Notify all components having registered for the given subject
	 * @param subject
	 */
	public static synchronized void notify(String subject){
		ArrayList alComponents =(ArrayList)hmEventComponents.get(subject); 
		if (alComponents == null){
			return;
		}
		Iterator it = alComponents.iterator();  
		while (it.hasNext()){
			((Observer)it.next()).update(subject);
		}
	}
}
