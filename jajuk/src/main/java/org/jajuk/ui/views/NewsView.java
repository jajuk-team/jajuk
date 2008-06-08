/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision: 3308 $$
 */

package org.jajuk.ui.views;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JFormattedTextField;

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;

/**
 * News view
 * <p>
 * It is a RSS reader as a tree. User can add a new RSS URL from a textfield. He
 * can remove an existing URL from the tree / right click
 * </p>
 */
public class NewsView extends ViewAdapter implements ITechnicalStrings, Observer {

  private static final long serialVersionUID = 1L;

  JFormattedTextField newRss;

  /**
   * Constructor
   * 
   */
  public NewsView() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("NewsView.0");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#populate()
   */
  public void initUI() {
    // subscriptions to events
    ObservationManager.register(NewsView.this);
  }

  public Set<JajukEvents> getRegistrationKeys() {
    HashSet<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.EVENT_PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(Event event) {
    JajukEvents subject = event.getSubject();
    // Make a search after a stop period
    if (subject.equals(JajukEvents.EVENT_PARAMETERS_CHANGE)) {
    }
  }

  /**
   * Perform RSS load
   */
//  private void loadRSS() {
//    Thread t = new Thread() {
//      public void run() {
//        try {
//        } catch (Exception e) {
//          Log.error(e);
//        }
//
//      }
//    };
//    t.setPriority(Thread.MIN_PRIORITY);
//    t.start();
//  }

}
