/*
 *  Jajuk
 *  Copyright (C) 2005 Bertrand Florat
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

import java.util.Properties;

/**
 *  Jajuk event (Observer pattern)
 *
 * @author     bflorat
 * @created    4 mars 2005
 */
public class Event {

	/**
	 * Event subject
	 */
	private String subject;
	
	/**
	 * Event properties
	 */
	private Properties pDetails;
	
	
	/**
	 * Event constructor
	 * @param sSubject
	 * @param pDetails
	 */
	public Event(String subject,Properties pDetails){
		this.subject = subject;
		this.pDetails = pDetails;
	}
	
	/**
	 * Event constructor
	 * @param sSubject
	 */
	public Event(String subject){
		this(subject,null);
	}
	
	
	/**
	 * @return Returns the pDetails.
	 */
	public Properties getDetails() {
		return pDetails;
	}
	/**
	 * @return Returns the sSubject.
	 */
	public String getSubject() {
		return subject;
	}
	
	/**
	 * ToString method
	 */
	public String toString(){
		return subject+" "+pDetails.toString(); //$NON-NLS-1$
	}
}
