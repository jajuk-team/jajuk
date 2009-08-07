/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.wizard;

import junit.framework.TestCase;

import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.dj.TransitionDigitalDJ;
import org.qdwizard.Screen;

/**
 * 
 */
public class TestDigitalDJWizard extends TestCase {
  /**
   * Test method for
   * {@link org.jajuk.ui.wizard.DigitalDJWizard#getPreviousScreen(java.lang.Class)}
   * .
   */

  public void testGetPreviousScreenClassOfQextendsScreen() throws Exception {
    DigitalDJWizard wizard = new DigitalDJWizard();
    assertNull(wizard.getPreviousScreen(null));
    assertNotNull(wizard.getNextScreen(null));

    // do some dummy things with this panel...
    Screen screen = wizard.getNextScreen(null).newInstance();
    screen.initUI();
    assertNotNull(screen.getDescription());
    assertNotNull(screen.getName());

    assertNull(wizard.getPreviousScreen(null)); // always null until
                                                // "actionPerformed"
  }

  /**
   * Test method for
   * {@link org.jajuk.ui.wizard.DigitalDJWizard#getNextScreen(java.lang.Class)}.
   */

  public void testGetNextScreenClassOfQextendsScreen() throws Exception {
    DigitalDJWizard wizard = new DigitalDJWizard();
    assertNotNull(wizard.getNextScreen(null));

    // do some dummy things with this panel...
    Screen screen = wizard.getNextScreen(null).newInstance();
    screen.initUI();

  }

  /**
   * Test method for
   * {@link org.jajuk.ui.wizard.DigitalDJWizard#DigitalDJWizard()}.
   */

  public void testDigitalDJWizard() {
    new DigitalDJWizard();
  }

  public void testPanelsActionSelection() {
    coverScreen(new DigitalDJWizard.ActionSelectionPanel());
  }
  
  public void testPanelsAmbience() {
    // we need to load ambiences before this will work
    AmbienceManager.getInstance().load();
    
    coverScreen(new DigitalDJWizard.AmbiencePanel());
  }
  
  public void testPanelsChange() {
    coverScreen(new DigitalDJWizard.ChangePanel());

    // also cover the case when a digital dj is registered
    DigitalDJ dj = new TransitionDigitalDJ("1");
    DigitalDJManager.getInstance().register(dj);
    
    coverScreen(new DigitalDJWizard.ChangePanel());
  }
  
  public void testPanelsGeneralOption() {
    coverScreen(new DigitalDJWizard.GeneralOptionsPanel());
  }
  
  public void testPanelsPropoertions() {
    coverScreen(new DigitalDJWizard.ProportionsPanel());
  }
  
  public void testPanelsRemove() {
    DigitalDJ dj = new TransitionDigitalDJ("1");
    DigitalDJManager.getInstance().register(dj);
    
    coverScreen(new DigitalDJWizard.RemovePanel());
  }
  
  public void testPanelsTransition() {
    coverScreen(new DigitalDJWizard.TransitionsPanel());
  }
  
  public void testPanelsTypeSelection() {
    coverScreen(new DigitalDJWizard.TypeSelectionPanel());
  }

  private void coverScreen(Screen screen) {
    screen.initUI();
    assertNotNull(screen.getDescription());
    assertNotNull(screen.getName());
    //JUnitHelpers.ToStringTest(screen);
  }


  /**
   * Test method for {@link org.jajuk.ui.wizard.DigitalDJWizard#finish()}.
   */

  public void testFinish() {
    // TODO: find out how to test this...
    /*
    coverScreen(new DigitalDJWizard.TypeSelectionPanel());

      DigitalDJWizard wizard = new DigitalDJWizard(); 
      wizard.finish();
   */
  }

}
