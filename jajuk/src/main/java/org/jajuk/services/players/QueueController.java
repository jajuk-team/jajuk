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
 *  $Revision$
 */
package org.jajuk.services.players;

import java.util.HashSet;
import java.util.Set;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;

/**
 * FIFO convenient facilities <singleton>.
 */
public final class QueueController implements Observer {

  /** DOCUMENT_ME. */
  private static QueueController self = new QueueController();

  // Register this item, do not do this in the constructor as the instance is not yet available
  static {
    ObservationManager.register(self);
  }

  /**
   * Gets the single instance of QueueController.
   * 
   * @return single instance of QueueController
   */
  public static QueueController getInstance() {
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
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
  @Override
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    // In case of device refresh, we force fifo cleanup, for ie to remove
    // deleted files
    if (JajukEvents.DEVICE_REFRESH.equals(subject)) {
      QueueModel.clean();
    }
  }

}
