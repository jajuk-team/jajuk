/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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

import java.util.Set;

/**
 * GoF Observer pattern Observer.
 */
public interface Observer {

  /**
   * Action to be done when receiving an event with this ID.
   * 
   * @param event The JajukEvent maps a subject and details of the event
   */
  void update(JajukEvent event);

  /**
   * Used by @see ObservationManager to retrieve all the events on which
   * the Observer wants to listen to.
   * 
   * @return A set of JajukEvents on which the Observer would like to listen.
   */
  Set<JajukEvents> getRegistrationKeys();
}
