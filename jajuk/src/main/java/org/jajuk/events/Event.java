/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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

import java.util.Properties;


/**
 * Jajuk event (Observer pattern)
 */
public class Event {

  /**
   * Event subject
   */
  private JajukEvents subject;

  /**
   * Event properties
   */
  private Properties pDetails;

  /**
   * Event constructor
   * 
   * @param sSubject
   * @param pDetails
   */
  public Event(JajukEvents subject, Properties pDetails) {
    this.subject = subject;
    this.pDetails = pDetails;
  }

  /**
   * Event constructor
   * 
   * @param sSubject
   */
  public Event(JajukEvents subject) {
    this(subject, null);
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
  public JajukEvents getSubject() {
    return subject;
  }

  /**
   * ToString method
   */
  public String toString() {
    return subject + " " + ((pDetails == null) ? "no details" : pDetails.toString());
  }

  /**
   * event equals method
   */
  public boolean equals(Object obj) {
    Event event = (Event) obj;
    boolean bOut = false;
    if (this.subject.equals(event.getSubject())) {
      if (this.pDetails == null && event.pDetails == null) {
        bOut = true;
      } else if (this.pDetails != null && event.pDetails != null
          && this.pDetails.equals(event.getDetails())) {
        bOut = true;
      }
    }
    return bOut;
  }
}
