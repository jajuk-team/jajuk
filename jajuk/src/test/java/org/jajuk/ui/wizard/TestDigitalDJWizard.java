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
package org.jajuk.ui.wizard;

import java.awt.HeadlessException;

import org.jajuk.JajukTestCase;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.dj.TransitionDigitalDJ;
import org.qdwizard.Screen;

/**
 * .
 */
public class TestDigitalDJWizard extends JajukTestCase {
  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.ui.wizard.DigitalDJWizard#getPreviousScreen(java.lang.Class)}
   * .
   */
  public void testGetPreviousScreenClassOfQextendsScreen() throws Exception {
    try {
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
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.ui.wizard.DigitalDJWizard#getNextScreen(java.lang.Class)}.
   */
  public void testGetNextScreenClassOfQextendsScreen() throws Exception {
    try {
      DigitalDJWizard wizard = new DigitalDJWizard();
      assertNotNull(wizard.getNextScreen(null));
      // do some dummy things with this panel...
      Screen screen = wizard.getNextScreen(null).newInstance();
      screen.initUI();
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.wizard.DigitalDJWizard#DigitalDJWizard()}.
   */
  public void testDigitalDJWizard() {
    try {
      new DigitalDJWizard();
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test panels action selection.
   * 
   */
  public void testPanelsActionSelection() {
    try {
      // to initialize static data...,
      // TODO: this looks a bit weird in the code of Wizard and Screen, why is
      // it needed?
      new DigitalDJWizard();
      coverScreen(new DigitalDJWizard.ActionSelectionPanel());
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test panels ambience.
   * 
   */
  public void testPanelsAmbience() {
    try {
      // to initialize static data...,
      // TODO: this looks a bit weird in the code of Wizard and Screen, why is
      // it needed?
      new DigitalDJWizard();
      // we need to load ambiences before this will work
      AmbienceManager.getInstance().load();
      coverScreen(new DigitalDJWizard.AmbiencePanel());
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test panels change.
   * 
   */
  public void testPanelsChange() {
    try {
      // to initialize static data...,
      // TODO: this looks a bit weird in the code of Wizard and Screen, why is
      // it needed?
      new DigitalDJWizard();
      coverScreen(new DigitalDJWizard.ChangePanel());
      // also cover the case when a digital dj is registered
      DigitalDJ dj = new TransitionDigitalDJ("1");
      DigitalDJManager.getInstance().register(dj);
      coverScreen(new DigitalDJWizard.ChangePanel());
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test panels general option.
   * 
   */
  public void testPanelsGeneralOption() {
    try {
      // to initialize static data...,
      // TODO: this looks a bit weird in the code of Wizard and Screen, why is
      // it needed?
      new DigitalDJWizard();
      coverScreen(new DigitalDJWizard.GeneralOptionsPanel());
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test panels proportions.
   * 
   */
  public void testPanelsProportions() {
    try {
      // to initialize static data...,
      // TODO: this looks a bit weird in the code of Wizard and Screen, why is
      // it needed?
      new DigitalDJWizard();
      coverScreen(new DigitalDJWizard.ProportionsPanel());
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test panels remove.
   * 
   */
  public void testPanelsRemove() {
    try {
      // to initialize static data...,
      // TODO: this looks a bit weird in the code of Wizard and Screen, why is
      // it needed?
      new DigitalDJWizard();
      DigitalDJ dj = new TransitionDigitalDJ("1");
      DigitalDJManager.getInstance().register(dj);
      coverScreen(new DigitalDJWizard.RemovePanel());
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test panels transition.
   * 
   */
  public void testPanelsTransition() {
    try {
      // to initialize static data...,
      // TODO: this looks a bit weird in the code of Wizard and Screen, why is
      // it needed?
      new DigitalDJWizard();
      coverScreen(new DigitalDJWizard.TransitionsPanel());
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test panels type selection.
   * 
   */
  public void testPanelsTypeSelection() {
    try {
      // to initialize static data...,
      // TODO: this looks a bit weird in the code of Wizard and Screen, why is
      // it needed?
      new DigitalDJWizard();
      coverScreen(new DigitalDJWizard.TypeSelectionPanel());
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Cover screen.
   * 
   *
   * @param screen 
   */
  private void coverScreen(Screen screen) {
    screen.initUI();
    assertNotNull(screen.getDescription());
    assertNotNull(screen.getName());
    // JUnitHelpers.ToStringTest(screen);
  }

  /**
   * Test method for {@link org.jajuk.ui.wizard.DigitalDJWizard#finish()}.
   */
  public void testFinish() {
    // TODO: find out how to test this...
    /*
     * coverScreen(new DigitalDJWizard.TypeSelectionPanel());
     * 
     * DigitalDJWizard wizard = new DigitalDJWizard(); wizard.finish();
     */
  }
}
