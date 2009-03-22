/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.services.players;

import java.util.HashSet;
import java.util.Set;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;

/**
 * FIFO convenient facilities <singleton>
 */
public final class FIFOManager implements Observer {

  private static FIFOManager self;

  private FIFOManager() {
  }

  public static FIFOManager getInstance() {
    if (self == null) {
      self = new FIFOManager();
      ObservationManager.register(self);
    }
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> keys = new HashSet<JajukEvents>();
    keys.add(JajukEvents.DEVICE_REFRESH);
    return keys;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.Event)
   */
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    // In case of device refresh, we force fifo cleanup, for ie to remove
    // deleted files
    if (JajukEvents.DEVICE_REFRESH.equals(subject)) {
      FIFO.clean();
    }
  }

}
