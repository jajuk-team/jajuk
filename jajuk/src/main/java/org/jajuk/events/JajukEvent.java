/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.events;

import java.util.Properties;

/**
 * Jajuk event (Observer pattern).
 */
public class JajukEvent {
  /** Event subject. */
  private final JajukEvents subject;
  /** Event properties. */
  private Properties pDetails;

  /**
   * Event constructor.
   *
   * @param subject 
   * @param pDetails 
   */
  public JajukEvent(JajukEvents subject, Properties pDetails) {
    this.subject = subject;
    if (pDetails != null) {
      this.pDetails = pDetails;
    }
  }

  /**
   * Event constructor.
   * 
   * @param subject 
   */
  public JajukEvent(JajukEvents subject) {
    this(subject, null);
  }

  /**
   * Gets the details.
   * 
   * @return Returns the pDetails.
   */
  public Properties getDetails() {
    return pDetails;
  }

  /**
   * Gets the subject.
   * 
   * @return Returns the sSubject.
   */
  public JajukEvents getSubject() {
    return subject;
  }

  /**
   * ToString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    // Do not display details, that can cause severe performance issue
    if (pDetails == null) {
      return subject + " no details";
    } else {
      return subject.toString();
    }
  }

  /**
   * event equals method.
   * 
   * @param obj 
   * 
   * @return true, if equals
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JajukEvent)) {
      return false;
    }
    JajukEvent event = (JajukEvent) obj;
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

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    // very simple implementation, needs to be revisited if this object is used
    // heavily in HashMaps/HashSets
    // for now just combine name and id to a hashcode
    // idea taken from
    // http://www.geocities.com/technofundo/tech/java/equalhash.html
    int hash = 7;
    hash = 31 * hash + subject.hashCode();
    if (pDetails != null) {
      // Use only properties size, not hashcode because it is too heavy for large selection
      // in properties and causes concurrent modification exceptions
      hash = 31 * hash + pDetails.size();
    }
    return hash;
  }
}
