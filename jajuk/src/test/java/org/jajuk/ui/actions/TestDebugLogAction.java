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
package org.jajuk.ui.actions;

import java.awt.HeadlessException;

import org.jajuk.JajukTestCase;
import org.jajuk.util.log.Log;

/**
 * DOCUMENT_ME.
 */
public class TestDebugLogAction extends JajukTestCase {

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.actions.DebugLogAction#perform(java.awt.event.ActionEvent)}
   * .
   */
  public void testPerform() {
    // initialize Log
    Log.init();

    DebugLogAction action = new DebugLogAction();
    try {
      action.perform(null); // it doesn't look at the actionevent at all right
      // now...
    } catch (HeadlessException e) {
      // thrown on Hudson/Sonar as they do not support tests that require UI
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.actions.DebugLogAction#DebugLogAction()}.
   */
  public void testDebugLogAction() {
    new DebugLogAction();
  }

}
