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
package ext;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.jajuk.JajukTestCase;

/**
 * 
 */
public class TestSliderMenuItem extends JajukTestCase {

  /**
   * Test method for {@link ext.SliderMenuItem#SliderMenuItem(int, int, int)}.
   */
  public void testSliderMenuItem() {
    new SliderMenuItem(1, 100, 3);
  }

  /**
   * Test method for {@link ext.SliderMenuItem#processMouseEvent(java.awt.event.MouseEvent, javax.swing.MenuElement[], javax.swing.MenuSelectionManager)}.
   */
  public void testProcessMouseEventMouseEventMenuElementArrayMenuSelectionManager() {
    SliderMenuItem item = new SliderMenuItem(1, 100, 3);
    item.processMouseEvent(new MouseEvent(item, 10, 10, 0, 1, 1, 10, false), null, null);
  }

  /**
   * Test method for {@link ext.SliderMenuItem#processKeyEvent(java.awt.event.KeyEvent, javax.swing.MenuElement[], javax.swing.MenuSelectionManager)}.
   */
  public void testProcessKeyEventKeyEventMenuElementArrayMenuSelectionManager() {
    SliderMenuItem item = new SliderMenuItem(1, 100, 3);
    item.processKeyEvent(new KeyEvent(item, 10, 9, 0, 0, 'a'), null, null);
  }

  /**
   * Test method for {@link ext.SliderMenuItem#menuSelectionChanged(boolean)}.
   */
  public void testMenuSelectionChanged() {
    SliderMenuItem item = new SliderMenuItem(1, 100, 3);
    item.menuSelectionChanged(false);
  }

  /**
   * Test method for {@link ext.SliderMenuItem#getSubElements()}.
   */
  public void testGetSubElements() {
    SliderMenuItem item = new SliderMenuItem(1, 100, 3);
    assertNotNull(item.getSubElements());
    assertEquals(0, item.getSubElements().length);
  }

  /**
   * Test method for {@link ext.SliderMenuItem#getComponent()}.
   */
  public void testGetComponent() {
    SliderMenuItem item = new SliderMenuItem(1, 100, 3);
    assertEquals(item, item.getComponent());
  }
}
