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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.views;

import java.awt.ItemSelectable;
import java.awt.event.ItemListener;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * 
 */
public class TestParameterView extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.ui.views.ParameterView#ParameterView()}.
   */
  public void testParameterView() {
    new ParameterView();
  }

  /**
   * Test method for
   * {@link org.jajuk.ui.views.ParameterView#actionPerformed(java.awt.event.ActionEvent)}
   * .
   * 
   * @throws Exception
   */
  public void testActionPerformed() throws Exception {
    ParameterView view = new ParameterView();
    view.initUI();

    JUnitHelpers.waitForAllWorkToFinishAndCleanup();

    // make sure the logger is initialized
    Log.init();
  
  }

  /**
   * Test method for {@link org.jajuk.ui.views.ParameterView#getDesc()}.
   */
  public void testGetDesc() {
    ParameterView view = new ParameterView();
    assertTrue(StringUtils.isNotBlank(view.getDesc()));
  }

  /**
   * Test method for
   * {@link org.jajuk.ui.views.ParameterView#getRegistrationKeys()}.
   */
  public void testGetRegistrationKeys() {
    ParameterView view = new ParameterView();
    Set<JajukEvents> set = view.getRegistrationKeys();
    assertTrue(set.toString(), set.contains(JajukEvents.PARAMETERS_CHANGE));
  }

  /**
   * Test method for {@link org.jajuk.ui.views.ParameterView#initUI()}.
   */
  public void testInitUI() {
    ParameterView view = new ParameterView();
    view.initUI();
  }

  /**
   * Test method for
   * {@link org.jajuk.ui.views.ParameterView#itemStateChanged(java.awt.event.ItemEvent)}
   * .
   */
  public void testItemStateChanged() {
    ParameterView view = new ParameterView();
    view.initUI();
  }


  /**
   * Test method for
   * {@link org.jajuk.ui.views.ParameterView#update(org.jajuk.events.JajukEvent)}
   * .
   * 
   * @throws Exception
   */
  public void testUpdateJajukEvent() throws Exception {
    ParameterView view = new ParameterView();
    view.initUI();

    // once without details
    view.update(new JajukEvent(JajukEvents.PARAMETERS_CHANGE, null));

    // wait for invokeLater to finish();
    JUnitHelpers.clearSwingUtilitiesQueue();

    // then with details
    Properties prop = new Properties();
    prop.put(Const.DETAIL_ORIGIN, view);
    view.update(new JajukEvent(JajukEvents.PARAMETERS_CHANGE, prop));

    // wait for invokeLater to finish();
    JUnitHelpers.clearSwingUtilitiesQueue();
  }

  /**
   * 
   */
  private final class MyItemSelectable implements ItemSelectable {
    @Override
    public void removeItemListener(ItemListener l) {

    }

    @Override
    public Object[] getSelectedObjects() {

      return null;
    }

    @Override
    public void addItemListener(ItemListener l) {

    }
  }

}
